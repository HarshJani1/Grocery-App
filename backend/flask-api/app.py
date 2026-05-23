from flask import Flask, request, jsonify
import pickle
import re
import nltk
from nltk.stem import PorterStemmer
from nltk.corpus import stopwords
import logging
from logging.handlers import RotatingFileHandler
from waitress import serve
import pandas as pd
from mlxtend.frequent_patterns import apriori, association_rules
from mlxtend.preprocessing import TransactionEncoder
import py_eureka_client.eureka_client as eureka_client
import threading
import traceback
import os
from datetime import datetime
import pytz
import pika
import json
import uuid
import time

# ==================== LOGGING SETUP ====================
IST = pytz.timezone("Asia/Kolkata")


class ISTFormatter(logging.Formatter):
    """Custom formatter that uses Asia/Kolkata timezone for all timestamps."""

    def formatTime(self, record, datefmt=None):
        dt = datetime.fromtimestamp(record.created, tz=IST)
        if datefmt:
            return dt.strftime(datefmt)
        return dt.strftime("%Y-%m-%d %H:%M:%S.") + f"{int(dt.microsecond / 1000):03d}"

    def format(self, record):
        # Add status_code if not present
        if not hasattr(record, "status_code"):
            record.status_code = "N/A"
        return super().format(record)


LOG_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "logs")
os.makedirs(LOG_DIR, exist_ok=True)
LOG_FILE = os.path.join(LOG_DIR, "flask-api.log")

LOG_FORMAT = (
    "%(asctime)s [%(levelname)-5s] [%(threadName)s] [%(name)s] "
    "- %(message)s | status=%(status_code)s"
)

# Create the IST formatter
ist_formatter = ISTFormatter(LOG_FORMAT)

# File handler with rotation
file_handler = RotatingFileHandler(
    LOG_FILE, maxBytes=10 * 1024 * 1024, backupCount=30, encoding="utf-8"
)
file_handler.setLevel(logging.DEBUG)
file_handler.setFormatter(ist_formatter)

# Console handler
console_handler = logging.StreamHandler()
console_handler.setLevel(logging.INFO)
console_handler.setFormatter(ist_formatter)

# Configure root logger
root_logger = logging.getLogger()
root_logger.setLevel(logging.DEBUG)
root_logger.addHandler(file_handler)
root_logger.addHandler(console_handler)

# Remove default handlers to prevent duplicate logs
for handler in root_logger.handlers[:]:
    if isinstance(handler, logging.StreamHandler) and handler not in [file_handler, console_handler]:
        root_logger.removeHandler(handler)

logger = logging.getLogger("SERVICE-ML")


def log_info(message, status_code="N/A"):
    logger.info(message, extra={"status_code": status_code})


def log_warn(message, status_code="N/A"):
    logger.warning(message, extra={"status_code": status_code})


def log_error(message, status_code="N/A", exc_info=False):
    logger.error(message, extra={"status_code": status_code}, exc_info=exc_info)


def log_debug(message, status_code="N/A"):
    logger.debug(message, extra={"status_code": status_code})


# ==================== APP INIT ====================
app = Flask(__name__)

EUREKA_SERVER = "http://localhost:8761/eureka"
SERVICE_PORT = 5001

log_info("Initializing Eureka client registration", "N/A")
eureka_client.init(
    eureka_server=EUREKA_SERVER,
    app_name="SERVICE-ML",
    instance_port=SERVICE_PORT,
    instance_host="localhost"
)
log_info("Eureka client registered successfully | serviceName=SERVICE-ML | port={}".format(SERVICE_PORT), "200")

# ==================== NLTK ====================
try:
    nltk.data.find("corpora/stopwords")
    log_debug("NLTK stopwords corpus found")
except LookupError:
    log_info("Downloading NLTK stopwords corpus")
    nltk.download("stopwords")
    log_info("NLTK stopwords corpus downloaded successfully")

ps = PorterStemmer()
STOPWORDS = set(stopwords.words("english")) - {"not"}

# ==================== SENTIMENT ====================
log_info("Loading sentiment model and vectorizer")
try:
    with open("models/svm_model.pkl", "rb") as f:
        SENTIMENT_MODEL = pickle.load(f)
    log_info("Sentiment SVM model loaded successfully")
except Exception as e:
    log_error("Failed to load sentiment model | error={}".format(str(e)), "500", exc_info=True)
    raise

try:
    with open("models/tfidf_vectorizer.pkl", "rb") as f:
        VECTORIZER = pickle.load(f)
    log_info("TF-IDF vectorizer loaded successfully")
except Exception as e:
    log_error("Failed to load TF-IDF vectorizer | error={}".format(str(e)), "500", exc_info=True)
    raise


def preprocess_text(text: str) -> str:
    text = re.sub(r"[^a-zA-Z']", " ", text)
    text = re.sub(r"\s+", " ", text).strip().lower()
    return " ".join(
        ps.stem(w) for w in text.split()
        if w not in STOPWORDS and len(w) > 2
    )

# ==================== RECOMMENDATION ====================
RECOMMENDATION_MAP = {}
MODEL_READY = False

def init_recommendation_model():
    global RECOMMENDATION_MAP, MODEL_READY
    try:
        log_info("Building recommendation model...")
        df = pd.read_csv("data.csv", header=None)
        transactions = df.apply(lambda r: r.dropna().tolist(), axis=1).tolist()
        log_debug("Transactions loaded | count={}".format(len(transactions)))

        te = TransactionEncoder()
        encoded = te.fit(transactions).transform(transactions)
        df_enc = pd.DataFrame(encoded, columns=te.columns_)

        freq = apriori(df_enc, min_support=0.01, use_colnames=True)
        log_debug("Frequent itemsets found | count={}".format(len(freq)))

        rules = association_rules(freq, metric="lift", min_threshold=1)
        log_debug("Association rules generated | count={}".format(len(rules)))

        rec = {}
        for _, row in rules.iterrows():
            for a in row["antecedents"]:
                rec.setdefault(a.lower(), set()).update(
                    c.lower() for c in row["consequents"]
                )

        RECOMMENDATION_MAP = rec
        MODEL_READY = True
        log_info("Recommendation model READY | products={}".format(len(rec)), "200")

    except Exception as e:
        log_error("Recommendation init failed | error={}".format(str(e)), "500", exc_info=True)

threading.Thread(target=init_recommendation_model, daemon=True).start()

# ==================== RABBITMQ & ASYNC TASK SETUP ====================
RABBITMQ_HOST = os.environ.get("RABBITMQ_HOST", "localhost")
QUEUE_NAME = "ml_analysis_queue"
TASK_RESULTS = {}

def publish_message(message: dict) -> bool:
    try:
        connection = pika.BlockingConnection(
            pika.ConnectionParameters(host=RABBITMQ_HOST, heartbeat=600, blocked_connection_timeout=300)
        )
        channel = connection.channel()
        channel.queue_declare(queue=QUEUE_NAME, durable=True)
        channel.basic_publish(
            exchange="",
            routing_key=QUEUE_NAME,
            body=json.dumps(message),
            properties=pika.BasicProperties(
                delivery_mode=2,  # make message durable
            )
        )
        connection.close()
        return True
    except Exception as e:
        log_error("Failed to publish message to RabbitMQ | error={}".format(str(e)), exc_info=True)
        return False

def start_consumer():
    def consume():
        while True:
            try:
                log_info("Starting RabbitMQ consumer thread...")
                connection = pika.BlockingConnection(
                    pika.ConnectionParameters(host=RABBITMQ_HOST, heartbeat=600, blocked_connection_timeout=300)
                )
                channel = connection.channel()
                channel.queue_declare(queue=QUEUE_NAME, durable=True)
                channel.basic_qos(prefetch_count=1)

                def callback(ch, method, properties, body):
                    try:
                        data = json.loads(body.decode("utf-8"))
                        task_id = data.get("task_id")
                        text = data.get("text")
                        
                        log_info("Consumer started processing task_id={}".format(task_id))
                        TASK_RESULTS[task_id] = {"status": "processing"}

                        processed = preprocess_text(text)
                        if not processed:
                            TASK_RESULTS[task_id] = {"status": "failed", "error": "Preprocessing failed"}
                            log_warn("Consumer task_id={} failed: preprocessing returned empty text".format(task_id))
                        else:
                            X = VECTORIZER.transform([processed]).toarray()
                            pred = int(SENTIMENT_MODEL.predict(X)[0])
                            TASK_RESULTS[task_id] = {"status": "completed", "sentiment": pred}
                            log_info("Consumer task_id={} completed | sentiment={}".format(task_id, pred))

                    except Exception as ex:
                        log_error("Error in consumer callback: {}".format(str(ex)), exc_info=True)
                        if 'task_id' in locals():
                            TASK_RESULTS[task_id] = {"status": "failed", "error": str(ex)}
                    finally:
                        ch.basic_ack(delivery_tag=method.delivery_tag)

                channel.basic_consume(queue=QUEUE_NAME, on_message_callback=callback)
                channel.start_consuming()
            except pika.exceptions.AMQPConnectionError as err:
                log_warn("RabbitMQ connection lost in consumer thread, retrying in 5 seconds... error={}".format(str(err)))
                time.sleep(5)
            except Exception as e:
                log_error("Unexpected error in RabbitMQ consumer: {}, retrying in 5 seconds...".format(str(e)), exc_info=True)
                time.sleep(5)

    consumer_thread = threading.Thread(target=consume, daemon=True)
    consumer_thread.start()

start_consumer()

# ==================== ROUTES ====================
@app.route("/products/analyze", methods=["POST"])
def analyze():
    log_info("POST /products/analyze - Sentiment analysis request received")
    try:
        data = request.get_json(force=True)
        text = data.get("text", "").strip()

        if not text:
            log_warn("POST /products/analyze - Empty text received", "400")
            return jsonify({"error": "Text is required"}), 400

        task_id = str(uuid.uuid4())
        TASK_RESULTS[task_id] = {"status": "queued"}

        message = {
            "task_id": task_id,
            "text": text
        }

        if publish_message(message):
            log_info("POST /products/analyze - Task queued successfully | task_id={}".format(task_id), "202")
            return jsonify({"task_id": task_id, "status": "queued"}), 202
        else:
            TASK_RESULTS[task_id] = {"status": "failed", "error": "Queueing failed"}
            log_error("POST /products/analyze - Failed to queue task | task_id={}".format(task_id), "500")
            return jsonify({"error": "Queueing failed"}), 500

    except Exception as e:
        log_error(
            "POST /products/analyze - Analysis failed | error={}\n{}".format(str(e), traceback.format_exc()),
            "500",
            exc_info=True
        )
        return jsonify({"error": "Internal server error"}), 500


@app.route("/products/analyze/result/<task_id>", methods=["GET"])
def get_result(task_id):
    log_info("GET /products/analyze/result/{} - Fetching result".format(task_id))
    result = TASK_RESULTS.get(task_id)
    if not result:
        log_warn("GET /products/analyze/result/{} - Task not found".format(task_id), "404")
        return jsonify({"error": "Task not found"}), 404
    
    log_info("GET /products/analyze/result/{} - Result fetched | status={}".format(task_id, result["status"]), "200")
    return jsonify(result)


@app.route("/products/recommend/<product>", methods=["GET"])
def recommend(product):
    log_info("GET /products/recommend/{} - Recommendation request received".format(product))
    if not MODEL_READY:
        log_warn("GET /products/recommend/{} - Model still loading".format(product), "503")
        return jsonify({"status": "loading"}), 503

    recommendations = list(RECOMMENDATION_MAP.get(product.lower(), []))
    log_info(
        "GET /products/recommend/{} - Recommendations fetched | count={}".format(product, len(recommendations)),
        "200"
    )
    return jsonify({
        "recommendations": recommendations
    })

@app.route("/health", methods=["GET"])
def health():
    log_debug("GET /health - Health check | modelReady={}".format(MODEL_READY), "200")
    return jsonify({"status": "UP", "model_ready": MODEL_READY})

# ==================== MAIN ====================
if __name__ == "__main__":
    log_info("SERVICE-ML starting on port {}".format(SERVICE_PORT), "N/A")
    serve(app, host="0.0.0.0", port=SERVICE_PORT)

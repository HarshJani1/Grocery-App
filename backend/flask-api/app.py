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

        log_debug("POST /products/analyze - Processing text | length={}".format(len(text)))
        processed = preprocess_text(text)
        if not processed:
            log_warn("POST /products/analyze - Preprocessing returned empty result | originalLength={}".format(len(text)), "400")
            return jsonify({"error": "Preprocessing failed"}), 400

        # ✅ FIXED LINE (correct variable + dense conversion)
        X = VECTORIZER.transform([processed]).toarray()

        pred = int(SENTIMENT_MODEL.predict(X)[0])

        log_info("POST /products/analyze - Sentiment analysis completed | sentiment={} | textLength={}".format(pred, len(text)), "200")
        return jsonify({"sentiment": pred})

    except Exception as e:
        log_error(
            "POST /products/analyze - Analysis failed | error={}\n{}".format(str(e), traceback.format_exc()),
            "500",
            exc_info=True
        )
        return jsonify({"error": "Internal server error"}), 500


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

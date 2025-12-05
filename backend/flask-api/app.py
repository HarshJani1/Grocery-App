from flask import Flask, request, jsonify
import pickle
import re
import nltk
from nltk.stem import PorterStemmer
from nltk.corpus import stopwords
import logging
from waitress import serve
import pandas as pd
from mlxtend.frequent_patterns import apriori, association_rules
from mlxtend.preprocessing import TransactionEncoder

import py_eureka_client.eureka_client as eureka_client

# Initialize Flask app
app = Flask(__name__)

EUREKA_SERVER = "http://localhost:8761/eureka"
eureka_client.init(
    eureka_server=EUREKA_SERVER,
    app_name="SERVICE-ML",
    instance_host="192.168.1.4",
    instance_port=5001
)

# Logging setup
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Load NLTK data
try:
    nltk.data.find('corpora/stopwords')
except LookupError:
    nltk.download('stopwords')

# Initialize stemmer and stopwords
ps = PorterStemmer()
all_stopwords = set(stopwords.words('english')) - {'not'}

# Load ML model for sentiment analysis
try:
    with open('models/svm_model.pkl', 'rb') as model_file:
        loaded_model = pickle.load(model_file)
    with open('models/tfidf_vectorizer.pkl', 'rb') as vectorizer_file:
        loaded_vectorizer = pickle.load(vectorizer_file)
    logger.info("Sentiment analysis models loaded successfully.")
except Exception as e:
    logger.error(f"Error loading sentiment model files: {str(e)}")
    raise RuntimeError("Failed to load sentiment analysis model files") from e

# Sentiment Preprocessing
def preprocess_text(text):
    try:
        text = re.sub(r"[^a-zA-Z']", ' ', text)
        text = re.sub(r"\s+", ' ', text).strip().lower()
        tokens = text.split()
        processed_tokens = [
            ps.stem(word)
            for word in tokens
            if word not in all_stopwords and len(word) > 2
        ]
        return ' '.join(processed_tokens)
    except Exception as e:
        logger.error(f"Text preprocessing failed: {str(e)}")
        return ""

@app.route('/products/analyze', methods=['POST'])
def analyze_sentiment():
    try:
        if not request.is_json:
            return jsonify({'error': 'Request must be JSON'}), 400

        data = request.get_json()
        if 'text' not in data or not isinstance(data['text'], str):
            return jsonify({'error': 'Invalid or missing text field'}), 400

        text = data['text'].strip()
        if len(text) < 3:
            return jsonify({'error': 'Text must be at least 3 characters'}), 400

        processed_text = preprocess_text(text)
        if not processed_text:
            return jsonify({'error': 'Failed to process text'}), 400

        transformed_text = loaded_vectorizer.transform([processed_text]).toarray()
        prediction = loaded_model.predict(transformed_text)

        if prediction.size == 0:
            raise ValueError("Empty prediction result")

        sentiment = int(prediction[0])
        return jsonify({
            'sentiment': sentiment,
            'processed_text': processed_text,
            'original_text': text
        })

    except Exception as e:
        logger.error(f"Sentiment analysis error: {str(e)}")
        return jsonify({
            'error': 'Internal server error',
            'message': str(e)
        }), 500

# Recommendation System
def load_data():
    file_path = "data.csv"
    df = pd.read_csv(file_path, header=None)
    transactions = df.apply(lambda row: row.dropna().tolist(), axis=1).tolist()
    return transactions

def train_model(transactions):
    te = TransactionEncoder()
    te_ary = te.fit(transactions).transform(transactions)
    df_encoded = pd.DataFrame(te_ary, columns=te.columns_)
    frequent_itemsets = apriori(df_encoded, min_support=0.01, use_colnames=True)
    rules = association_rules(frequent_itemsets, metric="lift", min_threshold=1)
    return rules

try:
    transactions = load_data()
    rules = train_model(transactions)
    logger.info("Recommendation model loaded successfully.")
except Exception as e:
    logger.error(f"Error loading recommendation data/model: {str(e)}")
    rules = pd.DataFrame()

@app.route("/products/recommend/<product>", methods=["GET"])
def recommend(product):
    if rules.empty:
        return jsonify({"error": "Recommendation model not loaded"}), 500

    try:
        related_rules = rules[rules['antecedents'].apply(lambda x: product in x)]
        recommendations = set()
        for _, row in related_rules.iterrows():
            recommendations.update(row['consequents'])
        return jsonify({"recommendations": list(recommendations)})
    except Exception as e:
        logger.error(f"Recommendation error: {str(e)}")
        return jsonify({"error": "Failed to generate recommendations", "message": str(e)}), 500

@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({'status': 'healthy'})

if __name__ == '__main__':
    logger.info("Starting unified Flask app on port 5001...")
    serve(app, host='0.0.0.0', port=5001)

import sys
import os
import threading
from unittest.mock import MagicMock, patch

# Configure mocks BEFORE importing app.py to prevent real connections/long loads
mock_eureka = MagicMock()
sys.modules['py_eureka_client'] = MagicMock()
sys.modules['py_eureka_client.eureka_client'] = mock_eureka

# Mock pika (RabbitMQ)
mock_pika = MagicMock()
mock_connection = MagicMock()
mock_channel = MagicMock()

# Make start_consuming block the daemon thread safely without spin-looping
def mock_start_consuming():
    threading.Event().wait()

mock_channel.start_consuming = mock_start_consuming
mock_connection.channel.return_value = mock_channel
mock_pika.BlockingConnection.return_value = mock_connection
sys.modules['pika'] = mock_pika

# Mock nltk to avoid downloading/filesystem check errors
mock_nltk = MagicMock()
sys.modules['nltk'] = mock_nltk
sys.modules['nltk.stem'] = MagicMock()
sys.modules['nltk.corpus'] = MagicMock()

# Mock pickle loading to avoid loading 37MB model in tests
import pickle
mock_sentiment_model = MagicMock()
mock_vectorizer = MagicMock()

# Mock the model's predictions
mock_sentiment_model.predict.return_value = [1] # default to positive sentiment

load_count = 0
def dummy_load(f):
    global load_count
    load_count += 1
    if load_count == 1:
        return mock_sentiment_model
    return mock_vectorizer

pickle.load = dummy_load

# Now we import app
from app import app, TASK_RESULTS, RECOMMENDATION_MAP

import pytest

@pytest.fixture
def client():
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client

def test_health_endpoint(client):
    """Test the GET /health endpoint."""
    response = client.get('/health')
    assert response.status_code == 200
    json_data = response.get_json()
    assert json_data['status'] == 'UP'
    assert 'model_ready' in json_data

def test_recommend_endpoint_not_ready(client):
    """Test GET /products/recommend/<product> when model is not ready."""
    import app as app_module
    app_module.MODEL_READY = False
    
    response = client.get('/products/recommend/apple')
    assert response.status_code == 503
    assert response.get_json() == {"status": "loading"}

def test_recommend_endpoint_ready(client):
    """Test GET /products/recommend/<product> when model is ready."""
    import app as app_module
    app_module.MODEL_READY = True
    app_module.RECOMMENDATION_MAP = {
        "apple": {"banana", "orange"}
    }
    
    response = client.get('/products/recommend/apple')
    assert response.status_code == 200
    json_data = response.get_json()
    assert "recommendations" in json_data
    # Convert list to set to avoid ordering issues in assertion
    assert set(json_data["recommendations"]) == {"banana", "orange"}

def test_analyze_empty_text(client):
    """Test POST /products/analyze with missing or empty text."""
    response = client.post('/products/analyze', json={})
    assert response.status_code == 400
    assert response.get_json() == {"error": "Text is required"}

    response = client.post('/products/analyze', json={"text": "   "})
    assert response.status_code == 400
    assert response.get_json() == {"error": "Text is required"}

@patch('app.publish_message')
def test_analyze_success(mock_publish, client):
    """Test POST /products/analyze successfully queuing a task."""
    mock_publish.return_value = True
    
    response = client.post('/products/analyze', json={"text": "This product is great!"})
    assert response.status_code == 202
    json_data = response.get_json()
    assert "task_id" in json_data
    assert json_data["status"] == "queued"
    
    task_id = json_data["task_id"]
    assert TASK_RESULTS[task_id] == {"status": "queued"}

@patch('app.publish_message')
def test_analyze_queue_failure(mock_publish, client):
    """Test POST /products/analyze handling queue publication failure."""
    mock_publish.return_value = False
    
    response = client.post('/products/analyze', json={"text": "This product is great!"})
    assert response.status_code == 500
    assert response.get_json() == {"error": "Queueing failed"}

def test_get_result_not_found(client):
    """Test GET /products/analyze/result/<task_id> with an unknown task ID."""
    response = client.get('/products/analyze/result/non-existent-uuid')
    assert response.status_code == 404
    assert response.get_json() == {"error": "Task not found"}

def test_get_result_found(client):
    """Test GET /products/analyze/result/<task_id> with an existing task ID."""
    task_id = "test-task-123"
    TASK_RESULTS[task_id] = {"status": "completed", "sentiment": 1}
    
    response = client.get(f'/products/analyze/result/{task_id}')
    assert response.status_code == 200
    assert response.get_json() == {"status": "completed", "sentiment": 1}

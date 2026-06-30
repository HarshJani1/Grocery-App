# ─── Runtime ─────────────────────────────────────────────────────────────────
FROM python:3.11-slim
WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc \
    && rm -rf /var/lib/apt/lists/*

# Install Python dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Pre-download NLTK stopwords at build time to avoid runtime downloads
RUN python -c "import nltk; nltk.download('stopwords', quiet=True)"

# Copy application files and trained models
COPY app.py .
COPY data.csv .
COPY models/ ./models/

# Create a non-root user
RUN useradd -m appuser && chown -R appuser /app
USER appuser

EXPOSE 5001

CMD ["python", "app.py"]

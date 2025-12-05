import React, { useState } from "react";
import axios from "axios";
import { Link } from 'react-router-dom';
import './ProductCard1.css';
import { useAuth } from "../../context/AuthContext";

const ProductCard1 = ({ product }) => {
  const [review, setReview] = useState("");
  const [positiveReviews, setPositiveReviews] = useState(product.positiveReviews || 0);
  const [negativeReviews, setNegativeReviews] = useState(product.negativeReviews || 0);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const { user } = useAuth();
  const token = user?.token;

  const handleSubmit = async (id) => {
    if (!review.trim()) return;
    if (!token) {
      setError("You must be logged in to submit reviews.");
      return;
    }

    try {
      setLoading(true);
      const res = await axios.post(
        `http://localhost:8765/service-ml/products/analyze`,
        { text: review },
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      const sentiment = res?.data?.sentiment ?? res?.data;
      if (sentiment === "1" || sentiment === 1 || sentiment === true) {
        setPositiveReviews(prev => prev + 1);
      } else {
        setNegativeReviews(prev => prev + 1);
      }
      setReview("");
      setError("");
    } catch (err) {
      console.error("Error Response:", err);
      setError("Error submitting review");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="product-card">
      <Link
        key={product.id}
        to={`/product/${product.id}`}
        style={{ textDecoration: 'none', color: 'inherit' }}
      >
        <h2>{product.name}</h2>
      </Link>
      <p className="description">{product.description}</p>
      <div className="price">${product.price}</div>

      <input
        type="text"
        placeholder="Write a review..."
        value={review}
        onChange={(e) => setReview(e.target.value)}
      />

      <button
        onClick={() => handleSubmit(product.id)}
        disabled={loading}
      >
        {loading ? "Submitting..." : "Submit"}
      </button>

      <div className="reviews">
        <p>👍 {positiveReviews}</p>
        <p>👎 {negativeReviews}</p>
      </div>

      {error && <p className="error">{error}</p>}
    </div>
  );
};

export default ProductCard1;

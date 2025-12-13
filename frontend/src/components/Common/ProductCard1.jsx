import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import { Link } from "react-router-dom";
import "./ProductCard1.css";
import { useAuth } from "../../context/AuthContext";

const ProductCard1 = ({ product }) => {
  const { user } = useAuth();
  const token = user?.token;

  const [review, setReview] = useState("");
  const [positiveReviews, setPositiveReviews] = useState(product.positiveReviews || 0);
  const [negativeReviews, setNegativeReviews] = useState(product.negativeReviews || 0);
  const [quantity, setQuantity] = useState(1);
  const [imageUrl, setImageUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const urlRef = useRef(null);

  useEffect(() => {
    if (!product?.id || !token) return;

    let cancelled = false;

    const fetchImage = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8765/service-product/products/getImage/${product.id}`,
          {
            responseType: "blob",
            headers: { Authorization: `Bearer ${token}` }
          }
        );

        if (cancelled) return;

        const url = URL.createObjectURL(res.data);
        if (urlRef.current) URL.revokeObjectURL(urlRef.current);
        urlRef.current = url;
        setImageUrl(url);
      } catch {
        setImageUrl(null);
      }
    };

    fetchImage();
    return () => {
      cancelled = true;
      if (urlRef.current) URL.revokeObjectURL(urlRef.current);
    };
  }, [product?.id, token]);

  const handleAddToCart = async () => {
    if (!token) {
      setError("Please login to add items.");
      return;
    }

    try {
      setLoading(true);
      await axios.post(
        "http://localhost:8765/service-cart/cart/add",
        { productName: product.name, quantity },
        { headers: { Authorization: `Bearer ${token}` } }
      );
      setQuantity(1);
      setError("");
    } catch {
      setError("Failed to add to cart");
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitReview = async () => {
    if (!review.trim()) return;

    try {
      setLoading(true);
      const res = await axios.post(
        "http://localhost:8765/service-ml/products/analyze",
        { text: review },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const sentiment = res.data.sentiment;
      sentiment ? setPositiveReviews(p => p + 1) : setNegativeReviews(n => n + 1);
      setReview("");
    } catch {
      setError("Review submission failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="product-card">
      <div className="image-wrapper">
        {imageUrl ? (
          <img src={imageUrl} alt={product.name} />
        ) : (
          <div className="image-loader"></div>
        )}
      </div>

      <Link to={`/product/${product.id}`} className="product-title">
        {product.name}
      </Link>

      <p className="description">{product.description}</p>
      <div className="price">${product.price}</div>

      <input
        type="text"
        placeholder="Write a review..."
        value={review}
        onChange={e => setReview(e.target.value)}
      />

      <button className="primary-btn" onClick={handleSubmitReview} disabled={loading}>
        {loading ? "Submitting..." : "Submit Review"}
      </button>

      <div className="review-count">
        <span>👍 {positiveReviews}</span>
        <span>👎 {negativeReviews}</span>
      </div>

      <div className="cart-row">
        <input
          type="number"
          min="1"
          max="50"
          value={quantity}
          onChange={e => setQuantity(Number(e.target.value))}
        />
        <button className="primary-btn" onClick={handleAddToCart} disabled={loading}>
          {loading ? "Adding..." : "Add to Cart"}
        </button>
      </div>

      {error && <p className="error">{error}</p>}
    </div>
  );
};

export default ProductCard1;

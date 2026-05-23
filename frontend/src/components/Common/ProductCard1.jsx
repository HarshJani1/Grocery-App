import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import { Link } from "react-router-dom";
import "./ProductCard1.css";
import { useAuth } from "../../context/AuthContext";

const ProductCard1 = ({ product }) => {
  const { user } = useAuth();
  const token = user?.token;

  const [review, setReview] = useState("");
  const [positive, setPositive] = useState(product.positiveReviews || 0);
  const [negative, setNegative] = useState(product.negativeReviews || 0);
  const [quantity, setQuantity] = useState(1);
  const [imageUrl, setImageUrl] = useState(null);
  const [loading, setLoading] = useState(false);

  const urlRef = useRef(null);

  useEffect(() => {
    if (!product?.id || !token) return;

    const fetchImage = async () => {
      const res = await axios.get(
        `http://localhost:8765/service-product/products/getImage/${product.id}`,
        {
          responseType: "blob",
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      const url = URL.createObjectURL(res.data);
      if (urlRef.current) URL.revokeObjectURL(urlRef.current);
      urlRef.current = url;
      setImageUrl(url);
    };

    fetchImage();

    return () => {
      if (urlRef.current) URL.revokeObjectURL(urlRef.current);
    };
  }, [product?.id, token]);

  const submitReview = async () => {
    if (!review.trim()) return;

    setLoading(true);
    try {
      const res = await axios.post(
        "http://localhost:8765/service-ml/products/analyze",
        { text: review },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      const taskId = res.data.task_id;
      if (!taskId) {
        setLoading(false);
        return;
      }

      const pollInterval = setInterval(async () => {
        try {
          const pollRes = await axios.get(
            `http://localhost:8765/service-ml/products/analyze/result/${taskId}`,
            { headers: { Authorization: `Bearer ${token}` } }
          );

          if (pollRes.data.status === "completed") {
            clearInterval(pollInterval);
            pollRes.data.sentiment ? setPositive((p) => p + 1) : setNegative((n) => n + 1);
            setReview("");
            setLoading(false);
          } else if (pollRes.data.status === "failed") {
            clearInterval(pollInterval);
            setLoading(false);
          }
        } catch (pollErr) {
          clearInterval(pollInterval);
          setLoading(false);
        }
      }, 500);
    } catch (err) {
      setLoading(false);
    }
  };

  const addToCart = async () => {
    setLoading(true);
    await axios.post(
      "http://localhost:8765/service-cart/cart/add",
      { productName: product.name, quantity },
      { headers: { Authorization: `Bearer ${token}` } }
    );
    setLoading(false);
  };

  return (
    <div className="product-card">
      <div className="product-image">
        {imageUrl ? <img src={imageUrl} alt={product.name} /> : <div className="img-skeleton" />}
      </div>

      <div className="product-body">
        <Link to={`/product/${product.id}`} className="product-name">
          {product.name}
        </Link>

        <p className="product-desc">{product.description}</p>

        <div className="product-price">${product.price}</div>

        <div className="review-box">
          <input
            type="text"
            placeholder="Write a review..."
            value={review}
            onChange={(e) => setReview(e.target.value)}
          />
          <button onClick={submitReview} disabled={loading}>
            Post
          </button>
        </div>

        <div className="review-stats">
          <span>👍 {positive}</span>
          <span>👎 {negative}</span>
        </div>

        <div className="cart-box">
          <input
            type="number"
            min="1"
            max="50"
            value={quantity}
            onChange={(e) => setQuantity(+e.target.value)}
          />
          <button onClick={addToCart} disabled={loading}>
            {loading ? "Adding..." : "Add to Cart"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ProductCard1;

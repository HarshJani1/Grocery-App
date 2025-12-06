import React, { useEffect, useState, useRef } from "react";
import axios from "axios";
import './ProductCard.css';
import { useAuth } from '../../context/AuthContext';

const ProductCard = ({ product }) => {
  const { user } = useAuth();
  const token = user?.token ?? null;

  const [imageUrl, setImageUrl] = useState(null);
  const urlRef = useRef(null);

  useEffect(() => {
    if (!product?.id || !token) {
      setImageUrl(null);
      return;
    }

    let cancelled = false;

    const fetchImage = async () => {
      try {
        const res = await axios.get(
          `http://localhost:8765/service-product/products/getImage/${product.id}`,
          {
            responseType: "blob",
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );

        if (cancelled) return;

        const url = URL.createObjectURL(res.data);
        // revoke previous if any
        if (urlRef.current) {
          try { URL.revokeObjectURL(urlRef.current); } catch (_) {}
        }
        urlRef.current = url;
        setImageUrl(url);
      } catch (e) {
        console.error(`Error fetching image for product ${product.id}:`, e);
        setImageUrl(null);
      }
    };

    fetchImage();

    return () => {
      cancelled = true;
      if (urlRef.current) {
        try { URL.revokeObjectURL(urlRef.current); } catch (_) {}
        urlRef.current = null;
      }
    };
  }, [product?.id, token]);

  return (
    <div className="product-card">
      <div className="images">
        {imageUrl ? (
          <img
            src={imageUrl}
            alt={product?.name ?? `product-${product?.id}`}
            style={{ maxWidth: '100%', maxHeight: 200, objectFit: 'cover' }}
          />
        ) : (
          <div className="image-placeholder">Loading image...</div>
        )}
      </div>

      <h2>{product.name}</h2>
      <p className="description">{product.description}</p>
      <div className="price">${product.price}</div>
      <div className="review-row">
        <span className="positive">👍 {product.likes}</span>
        <span className="negative">👎 {product.dislikes}</span>
      </div>
    </div>
  );
};

export default ProductCard;

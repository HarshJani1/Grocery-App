import { useEffect, useState, useRef } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import ProductCard1 from "../components/Common/ProductCard1";
import "./FullPage.css";
import Navbar from "../components/Common/Navbar";
import { useAuth } from "../context/AuthContext";

const FullPage = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { user } = useAuth();
  const token = user?.token;

  const [product, setProduct] = useState(null);
  const [productList, setProductList] = useState([]);
  const [recommended, setRecommended] = useState([]);
  const [imageUrl, setImageUrl] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(false);
  const [pageLoading, setPageLoading] = useState(true);
  const [error, setError] = useState("");

  const imgRef = useRef(null);

  useEffect(() => {
    if (!token) {
      navigate("/login");
      return;
    }

    const fetchAll = async () => {
      try {
        setPageLoading(true);

        const [productRes, listRes, imageRes] = await Promise.all([
          axios.get(
            `http://localhost:8765/service-product/products/${id}`,
            { headers: { Authorization: `Bearer ${token}` } }
          ),
          axios.get(
            "http://localhost:8765/service-product/products",
            { headers: { Authorization: `Bearer ${token}` } }
          ),
          axios.get(
            `http://localhost:8765/service-product/products/getImage/${id}`,
            {
              responseType: "blob",
              headers: { Authorization: `Bearer ${token}` },
            }
          ),
        ]);

        const productData =
          productRes?.data?.data ?? productRes?.data ?? null;
        setProduct(productData);

        const listPayload =
          listRes?.data?.data ?? listRes?.data ?? [];
        setProductList(Array.isArray(listPayload) ? listPayload : []);

        const url = URL.createObjectURL(imageRes.data);
        if (imgRef.current) URL.revokeObjectURL(imgRef.current);
        imgRef.current = url;
        setImageUrl(url);
      } catch (err) {
        console.error(err);
      } finally {
        setPageLoading(false);
      }
    };

    fetchAll();

    return () => {
      if (imgRef.current) URL.revokeObjectURL(imgRef.current);
    };
  }, [id, token]);

  useEffect(() => {
    const fetchRecommendations = async () => {
      if (!product?.name) return;
      try {
        const res = await axios.get(
          `http://localhost:8765/service-ml/products/recommend/${encodeURIComponent(
            product.name
          )}`
        );
        const recs = res?.data?.recommendations ?? res?.data ?? [];
        setRecommended(Array.isArray(recs) ? recs : []);
      } catch {
        setRecommended([]);
      }
    };
    fetchRecommendations();
  }, [product]);

  const handleAddToCart = async () => {
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

  const recommendedProducts = productList.filter((p) =>
    recommended.includes(p.name)
  );

  if (pageLoading) {
    return (
      <>
        <Navbar />
        <div className="fullpage-loader" />
      </>
    );
  }

  if (!product) return null;

  return (
    <>
      <Navbar />
      <div className="fullpage-container">
        <div className="product-detail-card">
          <div className="image-section">
            {imageUrl ? <img src={imageUrl} alt={product.name} /> : <div className="img-skeleton" />}
          </div>

          <div className="info-section">
            <h1>{product.name}</h1>
            <p className="description">{product.description}</p>
            <p className="price">${product.price}</p>

            <div className="reviews">
              <span>👍 {product.positiveReviews}</span>
              <span>👎 {product.negativeReviews}</span>
            </div>

            <div className="cart-row">
              <input
                type="number"
                min="1"
                max="50"
                value={quantity}
                onChange={(e) => setQuantity(Number(e.target.value))}
              />
              <button onClick={handleAddToCart} disabled={loading}>
                {loading ? "Adding..." : "Add to Cart"}
              </button>
            </div>

            {error && <p className="error">{error}</p>}
          </div>
        </div>

        <section className="recommend-section">
          <h2>Recommended Products</h2>

          {recommendedProducts.length > 0 ? (
            <div className="recommend-grid">
              {recommendedProducts.map((item) => (
                <ProductCard1 key={item.id} product={item} />
              ))}
            </div>
          ) : (
            <p className="empty">No recommended products</p>
          )}
        </section>
      </div>
    </>
  );
};

export default FullPage;

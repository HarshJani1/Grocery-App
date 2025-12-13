import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import axios from "axios";
import Navbar from "../components/Common/Navbar";
import CartItem from "../components/Common/CartItem";
import { useNavigate } from "react-router-dom";
import "./Cart.css";

const Cart = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const token = user?.token;

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);

  const fetchProducts = async () => {
    if (!token) return;
    try {
      setLoading(true);
      const res = await axios.get(
        "http://localhost:8765/service-cart/cart/get",
        { headers: { Authorization: `Bearer ${token}` } }
      );
      const payload = res?.data?.data ?? res?.data ?? [];
      setProducts(Array.isArray(payload) ? payload : []);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const total = products.reduce(
    (sum, item) => sum + item.quantity * item.price,
    0
  );

  const handleClear = async () => {
    try {
      setLoading(true);
      await axios.post(
        "http://localhost:8765/service-cart/cart/clear",
        {},
        { headers: { Authorization: `Bearer ${token}` } }
      );
      await fetchProducts();
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!token) navigate("/login");
    fetchProducts();
  }, [token]);

  return (
    <>
      <Navbar />
      <div className="cart-page">
        <h2>Your Cart</h2>

        {loading ? (
          <div className="cart-loader" />
        ) : products.length === 0 ? (
          <p className="empty-cart">Your cart is empty</p>
        ) : (
          <>
            <div className="cart-grid">
              {products.map((product) => (
                <CartItem key={product.id} product={product} />
              ))}
            </div>

            <div className="cart-summary">
              <h3>Total: ₹{total.toFixed(2)}</h3>
              <button onClick={handleClear} disabled={loading}>
                {loading ? "Processing..." : "Order Now"}
              </button>
            </div>
          </>
        )}
      </div>
    </>
  );
};

export default Cart;

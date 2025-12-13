import { useEffect, useState } from "react";
import axios from "axios";
import ProductCard1 from "../components/Common/ProductCard1";
import "./Home.css";
import Navbar from "../components/Common/Navbar";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

const Home = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const token = user?.token;

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!token) {
      navigate("/login");
      return;
    }

    const fetchProducts = async () => {
      try {
        setLoading(true);
        const res = await axios.get(
          "http://localhost:8765/service-product/products",
          {
            headers: { Authorization: `Bearer ${token}` },
          }
        );
        const payload = res?.data?.data ?? res?.data ?? [];
        setProducts(Array.isArray(payload) ? payload : []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, [token]);

  return (
    <>
      <Navbar />
      <div className="home-page">
        <h1>Our Products</h1>

        {loading ? (
          <div className="home-loader" />
        ) : (
          <div className="products-grid">
            {products.map((product) => (
              <ProductCard1 key={product.id} product={product} />
            ))}
          </div>
        )}
      </div>
    </>
  );
};

export default Home;

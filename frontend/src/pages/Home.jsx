import { useEffect, useState } from 'react';
import axios from 'axios';
import ProductCard1 from '../components/Common/ProductCard1';
import './Home.css';
import Navbar from '../components/Common/Navbar';
import { useAuth } from "../context/AuthContext";

const Home = () => {
  const { user } = useAuth();
  const token = user?.token;

  const [products, setProducts] = useState([]);

  useEffect(() => {
    if (!token) return;

    const fetchProducts = async () => {
      try {
        const res = await axios.get(
          "http://localhost:8765/service-product/products",
          {
            headers: { Authorization: `Bearer ${token}` }
          }
        );
        console.log("API RESPONSE:", res.data);
        const payload = res?.data?.data ?? res?.data ?? [];
        setProducts(Array.isArray(payload) ? payload : []);
      } catch (error) {
        console.error("Failed to fetch products:", error);
      }
    };
    fetchProducts();
  }, [token]);

  return (
    <div>
      <Navbar />
      <div className="home-page">
        <h1>Our Products</h1>
        <div className="products-grid">
          {products.map(product => (
            <ProductCard1 key={product.id} product={product} />
          ))}
        </div>
      </div>
    </div>
  );
};

export default Home;

import React, { useEffect, useState } from "react";
import axios from "axios";
import ProductForm from "./ProductForm";
import ProductCard from "../Common/ProductCard";
import "./AdminPanel.css";
import { useAuth } from "../../context/AuthContext";
import { useNavigate } from "react-router-dom";

const AdminPanel = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  const token = user?.token ?? null;
  const role = user?.role ?? null;

  const [products, setProducts] = useState([]);
  const [editingProduct, setEditingProduct] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!token || role !== "ADMIN") {
      navigate("/login");
      return;
    }
    fetchProducts();
  }, [token, role]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const res = await axios.get(
        "http://localhost:8765/service-product/products",
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

  const deleteProduct = async (id) => {
    try {
      await axios.delete(
        "http://localhost:8765/service-product/products/delete",
        {
          data: id,
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      setProducts((prev) => prev.filter((p) => p.id !== id));
    } catch (err) {
      console.error(err);
    }
  };

  if (!token) return null;

  return (
    <div className="admin-panel">
      <h1>Admin Dashboard</h1>

      <ProductForm
        fetchProducts={fetchProducts}
        editingProduct={editingProduct}
        setEditingProduct={setEditingProduct}
      />

      {loading ? (
        <div className="admin-loader" />
      ) : (
        <div className="products-grid">
          {products.map((product) => (
            <div key={product.id} className="admin-product-card">
              <ProductCard product={product} />
              <div className="admin-actions">
                <button
                  className="btn btn--primary"
                  onClick={() => setEditingProduct(product)}
                >
                  Edit
                </button>
                <button
                  className="btn btn--danger"
                  onClick={() => deleteProduct(product.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default AdminPanel;

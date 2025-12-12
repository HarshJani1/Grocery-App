import React, { useEffect, useState } from 'react';
import axios from 'axios';
import ProductForm from './ProductForm';
import ProductCard from '../Common/ProductCard';
import './AdminPanel.css';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const AdminPanel = () => {
  const nav = useNavigate();
  const { user } = useAuth();
  const token = user?.token ?? null;
  const role = user?.role ?? null;

  const [products, setProducts] = useState([]);
  const [editingProduct, setEditingProduct] = useState(null);

  useEffect(() => {
    if (!token || role != "ADMIN") nav('/login');

    const fetchProducts = async () => {
      try {
        const res = await axios.get(
          'http://localhost:8765/service-product/products',
          { headers: { Authorization: `Bearer ${token}` } }
        );
        const payload = res?.data?.data ?? res?.data ?? [];
        setProducts(Array.isArray(payload) ? payload : []);
        console.log(res.data.data);
      } catch (error) {
        console.error('Error fetching products (AdminPanel):', error);
      }
    };
    
    fetchProducts();
  }, [token]);

  const deleteProduct = async (id) => {
    if (!token) {
      console.error('deleteProduct: missing token');
      return;
    }
    try {
      await axios.delete(
        "http://localhost:8765/service-product/products/delete",
        {
          data: id,
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        }
      );

      setProducts(prev => prev.filter(p => p.id !== id));
    } catch (error) {
      console.error('Error deleting product:', error);
    }
  };

  const updateProduct = async (id, updatedData) => {
    if (!token) {
      console.error('updateProduct: missing token');
      return;
    }
    try {
      await axios.put("http://localhost:8765/service-product/products/update",
        {
          data: id,
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        });
      const res = await axios.get('http://localhost:8765/service-product/products', {
        headers: { Authorization: `Bearer ${token}` },
      });
      const payload = res?.data?.data ?? res?.data ?? [];
      setProducts(Array.isArray(payload) ? payload : []);
    } catch (error) {
      console.error('Error updating product:', error);
    }
  };

  if (!token) {
    return (
      <div className="admin-panel">
        <h1>Admin Dashboard</h1>
        <p style={{ color: 'orangered' }}>You are not authenticated. Please login to access admin features.</p>
      </div>
    );
  }

  return (
    <div className="admin-panel">
      <h1>Admin Dashboard</h1>

      <ProductForm
        fetchProducts={async () => {
          try {
            const res = await axios.get('http://localhost:8765/service-product/products', {
              headers: { Authorization: `Bearer ${token}` },
            });
            const payload = res?.data?.data ?? res?.data ?? [];
            setProducts(Array.isArray(payload) ? payload : []);
            console.log(res);
          } catch (err) {
            console.error('fetchProducts (ProductForm callback):', err);
          }
        }}
        editingProduct={editingProduct}
        setEditingProduct={setEditingProduct}
      />

      <div className="products-grid">
        {products.map((product) => (
          <div key={product.id} className="admin-product-card">
            <div class="images"></div>
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
    </div>
  );
};

export default AdminPanel;

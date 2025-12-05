import { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../../context/AuthContext';
import './ProductForm.css';

const ProductForm = ({ fetchProducts, editingProduct, setEditingProduct }) => {
  const { user } = useAuth();
  const token = user?.token ?? null;

  const [formData, setFormData] = useState({
    name: '',
    price: '',
    description: ''
  });
  const [image, setImage] = useState(null);

  useEffect(() => {
    if (editingProduct) {
      setFormData({
        name: editingProduct.name || '',
        description: editingProduct.description || '',
        price: editingProduct.price || ''
      });
      setImage(null);
    } else {
      setFormData({ name: '', price: '', description: '' });
      setImage(null);
    }
  }, [editingProduct]);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!token) {
      console.error('ProductForm: missing token');
      return;
    }

    const data = new FormData();
    data.append('name', formData.name);
    data.append('price', String(formData.price));
    data.append('description', formData.description);
    if (image) data.append('image', image);

    try {
      const url = editingProduct
        ? `http://localhost:8765/service-product/products/update`
        : `http://localhost:8765/service-product/products/create`;

      if (editingProduct) {
        // use PUT for updates
        await axios.put(url, data, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
      } else {
        await axios.post(url, data, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });
      }

      if (fetchProducts) await fetchProducts();
      setFormData({ name: '', price: '', description: '' });
      setImage(null);
      setEditingProduct(null);
    } catch (err) {
      console.error('Upload error:', err);
      console.error('Response data:', err.response?.data);
      console.error('Response status:', err.response?.status);
      console.error('Request headers:', err.config?.headers);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="product-form">
      <input
        type="text"
        placeholder="Product Name"
        value={formData.name}
        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
        required
      />

      <input
        type="text"
        placeholder="Price"
        value={formData.price}
        onChange={(e) => setFormData({ ...formData, price: e.target.value })}
        required
      />

      <textarea
        placeholder="Description"
        value={formData.description}
        onChange={(e) => setFormData({ ...formData, description: e.target.value })}
        required
      />

      <input
        type="file"
        accept="image/*"
        onChange={(e) => {
          setImage(e.target.files && e.target.files[0] ? e.target.files[0] : null);
        }}
      />

      <button type="submit">
        {editingProduct ? 'Update Product' : 'Add Product'}
      </button>
    </form>
  );
};

export default ProductForm;

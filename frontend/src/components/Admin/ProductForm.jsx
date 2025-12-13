import { useState, useEffect } from "react";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";
import "./ProductForm.css";

const ProductForm = ({ fetchProducts, editingProduct, setEditingProduct }) => {
  const { user } = useAuth();
  const token = user?.token ?? null;

  const [formData, setFormData] = useState({
    name: "",
    price: "",
    description: "",
  });
  const [image, setImage] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (editingProduct) {
      setFormData({
        name: editingProduct.name || "",
        price: editingProduct.price || "",
        description: editingProduct.description || "",
      });
      setImage(null);
    } else {
      setFormData({ name: "", price: "", description: "" });
      setImage(null);
    }
  }, [editingProduct]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!token) return;

    const data = new FormData();
    data.append("name", formData.name.trim());
    data.append("price", String(formData.price));
    data.append("description", formData.description.trim());
    if (image) data.append("image", image);

    try {
      setLoading(true);
      const url = editingProduct
        ? "http://localhost:8765/service-product/products/update"
        : "http://localhost:8765/service-product/products/create";

      const method = editingProduct ? axios.put : axios.post;

      await method(url, data, {
        headers: { Authorization: `Bearer ${token}` },
      });

      await fetchProducts?.();
      setFormData({ name: "", price: "", description: "" });
      setImage(null);
      setEditingProduct(null);
    } catch (err) {
      console.error("Product save error:", err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="product-form">
      <h2>{editingProduct ? "Edit Product" : "Add Product"}</h2>

      <input
        type="text"
        placeholder="Product Name"
        value={formData.name}
        onChange={(e) =>
          setFormData({ ...formData, name: e.target.value })
        }
        required
      />

      <input
        type="number"
        min="0"
        step="0.01"
        placeholder="Price"
        value={formData.price}
        onChange={(e) =>
          setFormData({ ...formData, price: e.target.value })
        }
        required
      />

      <textarea
        placeholder="Description"
        value={formData.description}
        onChange={(e) =>
          setFormData({ ...formData, description: e.target.value })
        }
        required
      />

      <input
        type="file"
        accept="image/*"
        onChange={(e) =>
          setImage(e.target.files?.[0] || null)
        }
      />

      <button type="submit" disabled={loading}>
        {loading
          ? editingProduct
            ? "Updating..."
            : "Adding..."
          : editingProduct
          ? "Update Product"
          : "Add Product"}
      </button>
    </form>
  );
};

export default ProductForm;

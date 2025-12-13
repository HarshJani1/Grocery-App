import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import Navbar from "../components/Common/Navbar";
import "./UserProfile.css";

const UserProfile = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const token = user?.token;

  const [form, setForm] = useState({
    username: user?.username || "",
    address: user?.address || "",
    phoneNumber: user?.phoneNumber || "",
    email: user?.email || "",
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const phoneRegex = /^[0-9]{10}$/;

  useEffect(() => {
    if (!token) navigate("/login");
  }, [token]);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: "" });
  };

  const validate = () => {
    const newErrors = {};

    if (!form.username.trim()) newErrors.username = "Name is required";
    if (!emailRegex.test(form.email)) newErrors.email = "Invalid email";
    if (!phoneRegex.test(form.phoneNumber))
      newErrors.phoneNumber = "Phone must be 10 digits";
    if (!form.address.trim()) newErrors.address = "Address is required";

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleUpdate = async () => {
    if (!validate()) return;

    try {
      setLoading(true);
      await axios.put(
        "http://localhost:8765/service-user/users/update",
        form,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        }
      );
      alert("Profile updated successfully");
    } catch (err) {
      alert("Update failed");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    try {
      setLoading(true);
      await axios.delete(
        "http://localhost:8765/service-user/users/delete",
        {
          headers: { Authorization: `Bearer ${token}` },
          data: { email: form.email },
        }
      );
      logout();
      navigate("/login");
    } catch {
      alert("Delete failed");
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <>
      <Navbar />
      <div className="profile-container">
        <div className="profile-card">
          <h2>User Profile</h2>

          <div className="input-group">
            <input
              name="username"
              value={form.username}
              onChange={handleChange}
              placeholder="Name"
            />
            {errors.username && <span className="error">{errors.username}</span>}
          </div>

          <div className="input-group">
            <input
              name="address"
              value={form.address}
              onChange={handleChange}
              placeholder="Address"
            />
            {errors.address && <span className="error">{errors.address}</span>}
          </div>

          <div className="input-group">
            <input
              name="phoneNumber"
              value={form.phoneNumber}
              onChange={handleChange}
              placeholder="Phone Number"
            />
            {errors.phoneNumber && (
              <span className="error">{errors.phoneNumber}</span>
            )}
          </div>

          <div className="input-group">
            <input
              name="email"
              value={form.email}
              onChange={handleChange}
              placeholder="Email"
            />
            {errors.email && <span className="error">{errors.email}</span>}
          </div>

          <button onClick={handleUpdate} disabled={loading}>
            {loading ? "Updating..." : "Update Profile"}
          </button>

          <button
            className="danger"
            onClick={handleDelete}
            disabled={loading}
          >
            Delete Profile
          </button>

          <button className="secondary" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>
    </>
  );
};

export default UserProfile;

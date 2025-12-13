import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import "./Signup.css";

const Signup = () => {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    phoneNumber: "",
    address: "",
  });

  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  /* ---------------- REGEX RULES ---------------- */
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  const passwordRegex =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&]).{8,}$/;
  const phoneRegex = /^[0-9]{10}$/;

  const validate = () => {
    const newErrors = {};

    if (!formData.username.trim()) {
      newErrors.username = "Name is required";
    }

    if (!emailRegex.test(formData.email)) {
      newErrors.email = "Invalid email format";
    }

    if (!passwordRegex.test(formData.password)) {
      newErrors.password =
        "Password must be 8+ chars, include uppercase, lowercase, number & special character";
    }

    if (!phoneRegex.test(formData.phoneNumber)) {
      newErrors.phoneNumber = "Phone number must be 10 digits";
    }

    if (!formData.address.trim()) {
      newErrors.address = "Address is required";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: "" });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      setLoading(true);
      await axios.post(
        "http://localhost:8765/service-auth/auth/register",
        formData
      );
      navigate("/login");
    } catch (error) {
      setErrors({
        api: error.response?.data?.message || "Signup failed",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="signup-container">
      <form onSubmit={handleSubmit} className="signup-card">
        <h1>Create Account</h1>

        <div className="input-group">
          <input
            type="text"
            name="username"
            placeholder="Full Name"
            onChange={handleChange}
          />
          {errors.username && <span className="error">{errors.username}</span>}
        </div>

        <div className="input-group">
          <input
            type="text"
            name="email"
            placeholder="Email"
            onChange={handleChange}
          />
          {errors.email && <span className="error">{errors.email}</span>}
        </div>

        <div className="input-group">
          <input
            type="password"
            name="password"
            placeholder="Password"
            onChange={handleChange}
          />
          {errors.password && <span className="error">{errors.password}</span>}
        </div>

        <div className="input-group">
          <input
            type="text"
            name="phoneNumber"
            placeholder="Phone Number"
            onChange={handleChange}
          />
          {errors.phoneNumber && (
            <span className="error">{errors.phoneNumber}</span>
          )}
        </div>

        <div className="input-group">
          <input
            type="text"
            name="address"
            placeholder="Address"
            onChange={handleChange}
          />
          {errors.address && <span className="error">{errors.address}</span>}
        </div>

        {errors.api && <p className="error center">{errors.api}</p>}

        <button type="submit" disabled={loading}>
          {loading ? <span className="loader" /> : "Sign Up"}
        </button>

        <p className="redirect">
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </form>
    </div>
  );
};

export default Signup;

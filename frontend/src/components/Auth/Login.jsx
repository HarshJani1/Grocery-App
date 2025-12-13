import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import { useAuth } from "../../context/AuthContext";
import "./Login.css";

const Login = () => {
  const [formData, setFormData] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const { login } = useAuth();

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setErrors({ ...errors, [e.target.name]: "" });
  };

  const validate = () => {
    const newErrors = {};

    if (!emailRegex.test(formData.email)) {
      newErrors.email = "Invalid email format";
    }

    if (!formData.password.trim()) {
      newErrors.password = "Password is required";
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) return;

    try {
      setLoading(true);
      const response = await axios.post(
        "http://localhost:8765/service-auth/auth/token",
        formData
      );

      const userData = response.data.data;
      login(userData);
      navigate(userData.role === "ADMIN" ? "/admin" : "/home");
    } catch (error) {
      setErrors({
        api: error.response?.data?.message || "Invalid email or password",
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <form onSubmit={handleSubmit} className="login-card">
        <h1>Login</h1>

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
            autoComplete="off"
          />
          {errors.password && <span className="error">{errors.password}</span>}
        </div>

        {errors.api && <p className="error center">{errors.api}</p>}

        <button type="submit" disabled={loading}>
          {loading ? <span className="loader" /> : "Login"}
        </button>

        <p className="redirect">
          Don’t have an account? <Link to="/">Signup</Link>
        </p>
      </form>
    </div>
  );
};

export default Login;

import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import {useAuth} from "../../context/AuthContext";
import './Login.css';

const Login = () => {
  const [formData, setFormData] = useState({ email: "", password: "" });
  const navigate = useNavigate();

  const { login } = useAuth();
  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post(
        "http://localhost:8765/service-auth/auth/token",
        formData
      );
      const userData = response.data.data;
      console.log(userData);
      login(userData);
      navigate(userData.role === "ADMIN" ? "/admin" : "/home");

    } catch (error) {
      console.error("Login error:", error.response?.data || error.message);
    }
  };

  return (
    <div className="login-container">
      <h1 >Login</h1>
      <form onSubmit={handleSubmit} className="login-form">
        <input
          type="email"
          name="email"
          placeholder="Email"
          onChange={handleChange}
          className="border p-2"
          required
        />
        <input
          type="text"
          name="password"
          placeholder="Password"
          onChange={handleChange}
          className="border p-2"
          autoComplete="off"
          required
        />
        <button type="submit" className="bg-blue-500 text-white p-2">
          Login
        </button>
      </form>
      <p className="mt-4">
        Don’t have an account?{" "}
        <Link to="/" className="signup-link">
          Signup
        </Link>
      </p>
    </div>
  );
};

export default Login;

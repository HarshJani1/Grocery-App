import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import './Signup.css';

const Signup = () => {
  const [formData, setFormData] = useState({ username: "", email: "", password: "",phoneNumber: "",address: "" });
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {

      await axios.post("http://localhost:8765/service-auth/auth/register", formData);

      navigate("/login");
    } catch (error) {
      console.error("Signup error:", error.response?.data || error.message);
    }
  };

  return (
    <div className="signup-container">
      <h1 >Signup</h1>
      <form onSubmit={handleSubmit} className="signup-form">
        <input
          type="text"
          name="username"
          placeholder="Name"
          onChange={handleChange}
          className="border p-2"
        />
        <input
          type="email"
          name="email"
          placeholder="Email"
          onChange={handleChange}
          className="border p-2"
        />
        <input
          type="text"
          name="password"
          placeholder="Password"
          onChange={handleChange}
          className="border p-2"
        />
        <input 
          type="number"
          name="phoneNumber"
          placeholder="phone number"
          onChange={handleChange}
          className="border p-2"
        />
        <input 
          type="text"
          name="address"
          placeholder="Address"
          onChange={handleChange}
          className="border p-2"
        />
        <button type="submit" className="bg-blue-500 text-white p-2">
          Sign Up
        </button>
      </form>
      <p className="mt-4">
        Already have an account?{" "}
        <Link to="/login">
          Login
        </Link>
      </p>
    </div>
  );
};

export default Signup;

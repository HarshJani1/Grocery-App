import React, { useState } from "react";
import "./ContactUs.css";
import Navbar from "../components/Common/Navbar";

const ContactUs = () => {
  const [loading, setLoading] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    setLoading(true);
    setTimeout(() => setLoading(false), 1200);
  };

  return (
    <>
      <Navbar />
      <div className="contact-us-container">
        <h1>Contact Us</h1>

        <div className="contact-grid">
          <div className="contact-card">
            <h3>📍 Address</h3>
            <p>123 Web Street, Silicon Valley, CA 94043</p>
          </div>

          <div className="contact-card">
            <h3>📞 Phone</h3>
            <p>+1 (123) 456-7890</p>
          </div>

          <div className="contact-card">
            <h3>📧 Email</h3>
            <p>contact@yourcompany.com</p>
          </div>
        </div>

        <div className="contact-form-section">
          <h2>Send Us a Message</h2>
          <form onSubmit={handleSubmit}>
            <input type="text" placeholder="Your Name" required />
            <input
              type="email"
              placeholder="Your Email"
              required
              pattern="^[^\s@]+@[^\s@]+\.[^\s@]+$"
            />
            <textarea placeholder="Your Message" rows="5" required />
            <button type="submit" disabled={loading}>
              {loading ? "Sending..." : "Send Message"}
            </button>
          </form>
        </div>
      </div>
    </>
  );
};

export default ContactUs;

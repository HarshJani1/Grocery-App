import React from "react";
import "./AboutUs.css";
import Navbar from "../components/Common/Navbar";

const AboutUs = () => {
  return (
    <>
      <Navbar />
      <div className="about-us-container">
        <h1>About Us</h1>

        <div className="about-grid">
          <section className="about-card">
            <h2>Who We Are</h2>
            <p>
              Welcome to FreshBasket! We’re a team of food lovers and tech
              enthusiasts focused on delivering fresh groceries right to your
              doorstep with speed, quality, and care.
            </p>
          </section>

          <section className="about-card">
            <h2>Our Story</h2>
            <p>
              Founded in 2022, FreshBasket started with a simple idea — make
              grocery shopping effortless. Today, we proudly serve households
              with reliability and freshness.
            </p>
          </section>

          <section className="about-card">
            <h2>What We Do</h2>
            <p>
              From sourcing premium products to ensuring fast delivery, we focus
              on creating a seamless and delightful shopping experience.
            </p>
          </section>

          <section className="about-card">
            <h2>Our Values</h2>
            <ul>
              <li>🛒 Quality Products</li>
              <li>🚚 Fast & Reliable Delivery</li>
              <li>😊 Customer First</li>
              <li>🌿 Sustainability</li>
            </ul>
          </section>
        </div>
      </div>
    </>
  );
};

export default AboutUs;

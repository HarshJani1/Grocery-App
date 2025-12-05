import React from 'react';
import './AboutUs.css';
import Navbar from '../components/Common/Navbar';

const AboutUs = () => {
    return (
        <div>
            <Navbar />
            <div className="about-us-container">
                <h1>About Us</h1>
                <section className="about-section">
                    <h2>Who We Are</h2>
                    <p>
                        Welcome to FreshBasket! We’re a dedicated team of food lovers, tech enthusiasts, and service experts focused on bringing the freshest groceries to your doorstep. Our mission is to make grocery shopping simple, convenient, and affordable through smart technology and thoughtful service.
                    </p>
                </section>

                <section className="about-section">
                    <h2>Our Story</h2>
                    <p>
                        Founded in 2022, our journey began with a simple goal — to make fresh, quality groceries easily accessible to everyone. Since then, we've grown into a passionate team, proudly serving households across the region with convenience, care, and a commitment to freshness.
                    </p>
                </section>

                <section className="about-section">
                    <h2>What We Do</h2>
                    <p>
                        We specialize in delivering fresh groceries, fast delivery, and a seamless shopping experience. From sourcing quality produce to ensuring timely doorstep delivery, we handle it all with a customer-first approach.
                    </p>
                </section>

                <section className="about-section">
                    <h2>Our Values</h2>
                    <ul>
                        <li>🛒 Quality</li>
                        <li>🚚 Reliability</li>
                        <li>😊 Customer Satisfaction</li>
                        <li>🌿 Freshness & Sustainability</li>
                    </ul>
                </section>
            </div>
        </div>
    );
};

export default AboutUs;

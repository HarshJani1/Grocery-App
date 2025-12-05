import React from 'react';
import './ContactUs.css';
import Navbar from '../components/Common/Navbar';

const ContactUs = () => {
    return (
        <div>
            <Navbar />
            <div className="contact-us-container">
                <h1>Contact Us</h1>

                <section className="contact-section">
                    <h2>📫 Get in Touch</h2>
                    <p>
                        Have questions, feedback, or just want to say hello? We'd love to hear from you!
                    </p>
                </section>

                <section className="contact-section">
                    <h2>📍 Our Office</h2>
                    <p>123 Web Street, Silicon Valley, CA 94043</p>
                </section>

                <section className="contact-section">
                    <h2>📞 Phone</h2>
                    <p>+1 (123) 456-7890</p>
                </section>

                <section className="contact-section">
                    <h2>📧 Email</h2>
                    <p>contact@yourcompany.com</p>
                </section>

                <section className="contact-form-section">
                    <h2>📝 Send Us a Message</h2>
                    <form>
                        <input type="text" placeholder="Your Name" required />
                        <input type="email" placeholder="Your Email" required />
                        <textarea placeholder="Your Message" rows="5" required></textarea>
                        <button type="submit">Send Message</button>
                    </form>
                </section>
            </div>
        </div>
    );
};

export default ContactUs;

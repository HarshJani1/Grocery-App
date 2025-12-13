import React, { useState } from "react";
import { Link, NavLink } from "react-router-dom";
import "./Navbar.css";
import { FaUser } from "react-icons/fa";

const Navbar = () => {
  const [isMobile, setIsMobile] = useState(false);

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <div className="navbar-logo">
          <Link to="/">FreshBasket</Link>
        </div>

        <ul
          className={isMobile ? "nav-links-mobile" : "nav-links"}
          onClick={() => setIsMobile(false)}
        >
          <li>
            <NavLink to="/home">Home</NavLink>
          </li>
          <li>
            <NavLink to="/cart">Cart</NavLink>
          </li>
          <li>
            <NavLink to="/aboutus">About Us</NavLink>
          </li>
          <li>
            <NavLink to="/contactus">Contact</NavLink>
          </li>
          <li className="profile-icon">
            <NavLink to="/userProfile">
              <FaUser />
            </NavLink>
          </li>
        </ul>

        <button
          className="mobile-menu-icon"
          onClick={() => setIsMobile(!isMobile)}
          aria-label="Toggle navigation menu"
        >
          {isMobile ? "✕" : "☰"}
        </button>
      </div>
    </nav>
  );
};

export default Navbar;

import React, { useState, useEffect, useRef } from "react";
import axios from "axios";
import { Link } from 'react-router-dom';
import { useAuth } from "../../context/AuthContext";
const Cart = ({product}) => {

    return (
        <div>
            <Link
                key={product.id}
                to={`/product/${product.id}`}
                style={{ textDecoration: 'none', color: 'inherit' }}
            >
                <h2>{product.name}</h2>
            </Link>
            <div>
                {product.price}
            </div>
            <div>
                {product.quantity}
            </div>
            <div>
                ${product.price*(product.quantity)}
            </div>
        </div>
    );
};

export default Cart;
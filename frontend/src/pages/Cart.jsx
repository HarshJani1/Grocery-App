import { useEffect, useState } from 'react';
import { useAuth } from "../context/AuthContext";
import axios from 'axios';
import Navbar from '../components/Common/Navbar';
import CartItem from '../components/Common/CartItem'
import { useNavigate } from 'react-router-dom';

const Cart = () => {
    const nav = useNavigate();
    const { user } = useAuth();
    const token = user?.token;

    const [products, setProducts] = useState([]);

    const fetchProducts = async () => {
        if (!token) return;

        try {
            const res = await axios.get(
                "http://localhost:8765/service-cart/cart/get",
                { headers: { Authorization: `Bearer ${token}` } }
            );
            const payload = res?.data?.data ?? res?.data ?? [];
            setProducts(Array.isArray(payload) ? payload : []);
        } catch (error) {
            console.error("Failed to fetch products:", error);
        }
    };

    const total = products.reduce(
        (sum, item) => sum + (item.quantity * item.price),
        0
    );

    const handleClear = async () => {
        try {
            const res = await axios.post(
                "http://localhost:8765/service-cart/cart/clear",
                {},
                { headers: { Authorization: `Bearer ${token}` } }
            );

            console.log(res.data);

            await fetchProducts();     // refresh cart UI
        } catch (error) {
            console.error("Failed to clear cart:", error);
        }
    };

    useEffect(() => {
        if(!token) nav('/login');
        fetchProducts();
    }, [token]);

    return (
        <div>
            <Navbar />
            <div>
                <h2>Cart Items</h2>

                <div className="products-grid">
                    {products.map(product => (
                        <CartItem key={product.id} product={product} />
                    ))}
                </div>

                <h3>Grand Total: ₹{total}</h3>

                <button onClick={handleClear}>Order Now</button>
            </div>
        </div>
    );
};

export default Cart;

import { useEffect, useState } from 'react';
import { useAuth } from "../context/AuthContext";
import axios from 'axios';
import ProductCard1 from '../components/Common/ProductCard1';
import Navbar from '../components/Common/Navbar';
import CartItem from '../components/Common/CartItem'
const Cart = () => {
    const { user } = useAuth();
    const token = user?.token;

    const [products, setProducts] = useState([]);

    useEffect(() => {
        if (!token) return;

        const fetchProducts = async () => {
            try {
                const res = await axios.get(
                    "http://localhost:8765/service-cart/cart/get",
                    {
                        headers: { Authorization: `Bearer ${token}` }
                    }
                );
                console.log("API RESPONSE:", res.data);
                const payload = res?.data?.data ?? res?.data ?? [];
                setProducts(Array.isArray(payload) ? payload : []);
            } catch (error) {
                console.error("Failed to fetch products:", error);
            }
        };
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
            </div>
        </div>
    );
};

export default Cart;
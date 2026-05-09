import { Link } from "react-router-dom";
import "./CartItem.css";

const CartItem = ({ product }) => {
  return (
    <div className="cart-item">
      <div className="cart-item-info">
        <p>
          {product.name}
        </p>
        <p className="cart-item-qty">Qty: {product.quantity}</p>
      </div>

      <div className="cart-item-price">
        <span>₹{product.price}</span>
        <strong>₹{(product.price * product.quantity).toFixed(2)}</strong>
      </div>
    </div>
  );
};

export default CartItem;

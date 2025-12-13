import { Link } from 'react-router-dom';
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
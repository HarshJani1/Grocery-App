import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import ProductCard1 from '../components/Common/ProductCard1';
import './FullPage.css';
import Navbar from '../components/Common/Navbar';
import { useAuth } from "../context/AuthContext";
import { useNavigate } from 'react-router-dom';

const FullPage = () => {
  const nav = useNavigate();
  const { user } = useAuth();
  const token = user?.token;
  const { id } = useParams();

  const [product, setProduct] = useState(null);
  const [imageUrl, setImageUrl] = useState([]);
  const [productList, setProductList] = useState([]);
  const [recommended, setRecommended] = useState([]);

  useEffect(() => {
    if (!token) nav('/login');

    const fetchImage = async () => {
            try {
                const res = await axios.get(
                    `http://localhost:8765/service-product/products/getImage/${id}`,
                    {
                        responseType: "blob",
                        headers: {
                            Authorization: `Bearer ${token}`
                        }
                    }
                );

                const url = URL.createObjectURL(res.data);
                setImageUrl(url);
            } catch (e) {
                console.error("Error fetching image:", e);
            }
        };


        fetchImage();



    const fetchProduct = async () => {
      try {
        const res = await axios.get(`http://localhost:8765/service-product/products/${id}`, {
          headers: { Authorization: `Bearer ${token}` }
        });
        setProduct(res?.data?.data ?? res?.data ?? null);

        const res1 = await axios.get("http://localhost:8765/service-product/products", {
          headers: { Authorization: `Bearer ${token}` }
        });
        const payload = res1?.data?.data ?? res1?.data ?? [];
        setProductList(Array.isArray(payload) ? payload : []);
      } catch (err) {
        console.error("Failed to fetch product:", err);
      }
    };

    fetchProduct();
  }, [id, token]);

  useEffect(() => {
    const fetchRecommendations = async () => {
      try {
        if (!product?.name) return;

        const res3 = await axios.get(
          `http://localhost:8765/service-ml/products/recommend/${encodeURIComponent(product.name)}`
        );

        const recs = res3?.data?.recommendations ?? res3?.data ?? [];
        setRecommended(Array.isArray(recs) ? recs : []);
      } catch (err) {
        console.error("Failed to fetch recommendations:", err);
        setRecommended([]);
      }
    };

    fetchRecommendations();
  }, [product]);

  const recommendedProducts = Array.isArray(recommended)
    ? productList.filter(p => recommended.includes(p.name))
    : [];

  if (!product) return <div className="text-white text-center mt-10">Loading...</div>;

  return (
    <div>
      <Navbar />
      <div className="fullpage-container">
        <div>
          <img
                    src={imageUrl}
                    alt={product.name}
                    style={{
                      width: "50%",
                      height: "10%",
                      objectFit: "cover",
                      padding: "10px",
                      margin: "12%",
                    }}
                  />
          <section className="mb-12 pt-16">
            <h1 className="text-3xl font-bold text-violet-400 mb-2">{product.name}</h1>
            <h4 className="text-lg text-gray-300 mb-4">{product.description}</h4>
            <p className="text-blue-400 font-semibold text-lg mb-2">💰 Price: ${product.price}</p>

            <div className="text-sm text-gray-400 mt-2">
              <p>👍 Positive Reviews: {product.positiveReviews}</p>
              <p>👎 Negative Reviews: {product.negativeReviews}</p>
            </div>
          </section>

          <section>
            <h2 className="text-2xl font-semibold text-violet-300 mb-4">🧠 Recommended Products</h2>

            {recommendedProducts.length > 0 ? (
              <div className="grid">
                {recommendedProducts.map(item => (
                  <ProductCard1 key={item._id} product={item} />
                ))}
              </div>
            ) : (
              <p className="text-gray-500">No recommended products found</p>
            )}
          </section>
        </div>
      </div>
    </div>
  );
};

export default FullPage;

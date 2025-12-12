import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import axios from "axios";

const UserProfile = () => {
    const nav = useNavigate();
    const { user } = useAuth();
    const token = user?.token;
    const { logout } = useAuth();
    const [form, setForm] = useState({
        username: user?.username || "",
        address: user?.address || "",
        phoneNumber: user?.phoneNumber || "",
        email: user?.email || "",
    });

    const handleLogout =() =>{
        logout();
        nav('/login');
    };

    useEffect(() => {
        if (!token) nav("/login");
    }, [token]);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleUpdate = async () => {
        try {
            const res = await axios.put(
                "http://localhost:8765/service-user/users/update",
                form,
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                        "Content-Type": "application/json",
                    },
                }
            );

            console.log("UPDATE SUCCESS:", res.data);
            alert("Profile updated successfully!");
        } catch (err) {
            console.error("UPDATE FAILED:", err);
            alert("Update failed. Check console.");
        }
    };

    const handleDelete = async () => {
        try {
            const res = await axios.delete(
                "http://localhost:8765/service-user/users/delete",
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                    data: { email: form.email }, // backend usually needs identifier
                }
            );

            console.log("DELETE SUCCESS:", res.data);
            alert("Profile deleted successfully!");

            localStorage.removeItem("user");
            nav("/login");
        } catch (err) {
            console.error("DELETE FAILED:", err);
            alert("Delete failed. Check console.");
        }
    };

    return (
        <>
            <h2>User Profile</h2>

            <input
                name="username"
                value={form.username}
                onChange={handleChange}
                placeholder="Name"
            />

            <input
                name="address"
                value={form.address}
                onChange={handleChange}
                placeholder="Address"
            />

            <input
                name="phoneNumber"
                value={form.phoneNumber}
                onChange={handleChange}
                placeholder="Phone Number"
            />

            <input
                name="email"
                value={form.email}
                onChange={handleChange}
                placeholder="Email"
            />

            <button onClick={handleUpdate}>Update Profile</button>

            <button
                style={{ background: "red", color: "white", marginLeft: "10px" }}
                onClick={handleDelete}
            >
                Delete Profile
            </button>
            <div>
                <button onClick={handleLogout}>Logout</button>
            </div>
        </>
    );
};

export default UserProfile;

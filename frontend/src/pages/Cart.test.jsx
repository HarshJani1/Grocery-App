import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { vi, describe, it, expect, beforeEach } from "vitest";
import axios from "axios";
import Cart from "./Cart";

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock useAuth
const mockUser = { token: "valid-jwt-token" };
vi.mock("../context/AuthContext", () => ({
  useAuth: () => ({
    user: mockUser,
  }),
}));

// Mock Navbar and CartItem
vi.mock("../components/Common/Navbar", () => ({
  default: () => <div data-testid="navbar">Mock Navbar</div>,
}));

vi.mock("../components/Common/CartItem", () => ({
  default: ({ product }) => (
    <div data-testid={`cart-item-${product.id}`}>
      {product.name} - {product.quantity} x ₹{product.price}
    </div>
  ),
}));

// Mock axios
vi.mock("axios");

describe("Cart Page", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("fetches and displays cart products", async () => {
    const mockCartItems = [
      { id: 1, name: "Apple", price: 10.0, quantity: 2 },
      { id: 2, name: "Milk", price: 30.0, quantity: 1 },
    ];
    axios.get.mockResolvedValueOnce({ data: { data: mockCartItems } });

    render(
      <MemoryRouter>
        <Cart />
      </MemoryRouter>
    );

    expect(screen.getByTestId("navbar")).toBeInTheDocument();

    // Verify it loads products and displays them using CartItem mock
    await waitFor(() => {
      expect(screen.getByTestId("cart-item-1")).toHaveTextContent("Apple - 2 x ₹10");
      expect(screen.getByTestId("cart-item-2")).toHaveTextContent("Milk - 1 x ₹30");
    });

    // Total should be 2*10 + 1*30 = ₹50.00
    expect(screen.getByRole("heading", { name: "Total: ₹50.00" })).toBeInTheDocument();
  });

  it("displays empty cart message if user has no items in the cart", async () => {
    axios.get.mockResolvedValueOnce({ data: { data: [] } });

    render(
      <MemoryRouter>
        <Cart />
      </MemoryRouter>
    );

    expect(await screen.findByText("Your cart is empty")).toBeInTheDocument();
  });

  it("handles Order Now (Clear Cart) button click successfully", async () => {
    const mockCartItems = [
      { id: 1, name: "Apple", price: 10.0, quantity: 2 },
    ];
    axios.get.mockResolvedValueOnce({ data: { data: mockCartItems } });
    axios.post.mockResolvedValueOnce({ data: { message: "Cart Cleared" } });
    axios.get.mockResolvedValueOnce({ data: { data: [] } }); // subsequent fetch after clearing

    render(
      <MemoryRouter>
        <Cart />
      </MemoryRouter>
    );

    // Wait for the cart items to render
    await screen.findByTestId("cart-item-1");

    const orderButton = screen.getByRole("button", { name: "Order Now" });
    fireEvent.click(orderButton);

    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        "http://localhost:8765/service-cart/cart/clear",
        {},
        { headers: { Authorization: "Bearer valid-jwt-token" } }
      );
      expect(screen.getByText("Your cart is empty")).toBeInTheDocument();
    });
  });
});

import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { vi, describe, it, expect, beforeEach } from "vitest";
import axios from "axios";
import Login from "./Login";

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock useAuth context
const mockLogin = vi.fn();
vi.mock("../../context/AuthContext", () => ({
  useAuth: () => ({
    login: mockLogin,
  }),
}));

// Mock axios
vi.mock("axios");

describe("Login Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders email and password inputs and submit button", () => {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    expect(screen.getByPlaceholderText("Email")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Password")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Login" })).toBeInTheDocument();
  });

  it("shows validation error for invalid email format", async () => {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Email"), {
      target: { value: "invalid-email" },
    });
    fireEvent.change(screen.getByPlaceholderText("Password"), {
      target: { value: "password123" },
    });

    fireEvent.click(screen.getByRole("button", { name: "Login" }));

    expect(await screen.findByText("Invalid email format")).toBeInTheDocument();
  });

  it("shows validation error when password is empty", async () => {
    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Email"), {
      target: { value: "test@example.com" },
    });

    fireEvent.click(screen.getByRole("button", { name: "Login" }));

    expect(await screen.findByText("Password is required")).toBeInTheDocument();
  });

  it("calls API and navigates to /home on successful user login", async () => {
    const mockUserResponse = {
      data: {
        data: {
          email: "test@example.com",
          role: "USER",
          token: "mock-jwt-token.part2.part3",
        },
      },
    };
    axios.post.mockResolvedValueOnce(mockUserResponse);

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Email"), {
      target: { value: "test@example.com" },
    });
    fireEvent.change(screen.getByPlaceholderText("Password"), {
      target: { value: "password123" },
    });

    fireEvent.click(screen.getByRole("button", { name: "Login" }));

    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        "http://localhost:8765/service-auth/auth/token",
        { email: "test@example.com", password: "password123" }
      );
      expect(mockLogin).toHaveBeenCalledWith(mockUserResponse.data.data);
      expect(mockNavigate).toHaveBeenCalledWith("/home");
    });
  });

  it("calls API and navigates to /admin on successful admin login", async () => {
    const mockAdminResponse = {
      data: {
        data: {
          email: "admin@example.com",
          role: "ADMIN",
          token: "mock-jwt-token.part2.part3",
        },
      },
    };
    axios.post.mockResolvedValueOnce(mockAdminResponse);

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Email"), {
      target: { value: "admin@example.com" },
    });
    fireEvent.change(screen.getByPlaceholderText("Password"), {
      target: { value: "password123" },
    });

    fireEvent.click(screen.getByRole("button", { name: "Login" }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/admin");
    });
  });

  it("shows API error message on login failure", async () => {
    axios.post.mockRejectedValueOnce({
      response: {
        data: {
          message: "Unauthorized request",
        },
      },
    });

    render(
      <MemoryRouter>
        <Login />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Email"), {
      target: { value: "test@example.com" },
    });
    fireEvent.change(screen.getByPlaceholderText("Password"), {
      target: { value: "wrongpass" },
    });

    fireEvent.click(screen.getByRole("button", { name: "Login" }));

    expect(await screen.findByText("Unauthorized request")).toBeInTheDocument();
  });
});

import React from "react";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { vi, describe, it, expect, beforeEach } from "vitest";
import axios from "axios";
import Signup from "./Signup";

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const actual = await vi.importActual("react-router-dom");
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock axios
vi.mock("axios");

describe("Signup Component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders all form input fields and signup button", () => {
    render(
      <MemoryRouter>
        <Signup />
      </MemoryRouter>
    );

    expect(screen.getByPlaceholderText("Full Name")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Email")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Password")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Phone Number")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Address")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Sign Up" })).toBeInTheDocument();
  });

  it("shows validation error for empty inputs", async () => {
    render(
      <MemoryRouter>
        <Signup />
      </MemoryRouter>
    );

    fireEvent.click(screen.getByRole("button", { name: "Sign Up" }));

    expect(await screen.findByText("Name is required")).toBeInTheDocument();
    expect(screen.getByText("Invalid email format")).toBeInTheDocument();
    expect(screen.getByText("Phone number must be 10 digits")).toBeInTheDocument();
    expect(screen.getByText("Address is required")).toBeInTheDocument();
  });

  it("shows validation error for invalid password format", async () => {
    render(
      <MemoryRouter>
        <Signup />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Password"), {
      target: { value: "short" },
    });

    fireEvent.click(screen.getByRole("button", { name: "Sign Up" }));

    expect(
      await screen.findByText(
        "Password must be 8+ chars, include uppercase, lowercase, number & special character"
      )
    ).toBeInTheDocument();
  });

  it("calls register API and navigates to /login on successful signup", async () => {
    axios.post.mockResolvedValueOnce({ data: { message: "Success" } });

    render(
      <MemoryRouter>
        <Signup />
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Full Name"), {
      target: { value: "John Doe" },
    });
    fireEvent.change(screen.getByPlaceholderText("Email"), {
      target: { value: "john@example.com" },
    });
    fireEvent.change(screen.getByPlaceholderText("Password"), {
      target: { value: "Secret123!" },
    });
    fireEvent.change(screen.getByPlaceholderText("Phone Number"), {
      target: { value: "1234567890" },
    });
    fireEvent.change(screen.getByPlaceholderText("Address"), {
      target: { value: "123 Main St" },
    });

    fireEvent.click(screen.getByRole("button", { name: "Sign Up" }));

    await waitFor(() => {
      expect(axios.post).toHaveBeenCalledWith(
        "http://localhost:8765/service-auth/auth/register",
        {
          username: "John Doe",
          email: "john@example.com",
          password: "Secret123!",
          phoneNumber: "1234567890",
          address: "123 Main St",
        }
      );
      expect(mockNavigate).toHaveBeenCalledWith("/login");
    });
  });

  it("shows API errors if signup fails", async () => {
    axios.post.mockRejectedValueOnce({
      response: {
        data: {
          message: "Email already exists",
        },
      },
    });

    render(
      <MemoryRouter>
        <Signup />
      </MemoryRouter>
    );

    // Provide valid inputs to bypass local validation
    fireEvent.change(screen.getByPlaceholderText("Full Name"), {
      target: { value: "John Doe" },
    });
    fireEvent.change(screen.getByPlaceholderText("Email"), {
      target: { value: "john@example.com" },
    });
    fireEvent.change(screen.getByPlaceholderText("Password"), {
      target: { value: "Secret123!" },
    });
    fireEvent.change(screen.getByPlaceholderText("Phone Number"), {
      target: { value: "1234567890" },
    });
    fireEvent.change(screen.getByPlaceholderText("Address"), {
      target: { value: "123 Main St" },
    });

    fireEvent.click(screen.getByRole("button", { name: "Sign Up" }));

    expect(await screen.findByText("Email already exists")).toBeInTheDocument();
  });
});

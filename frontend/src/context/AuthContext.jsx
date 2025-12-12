import React, { createContext, useContext, useEffect, useState } from 'react';

const AuthContext = createContext();

const isJwt = (s) => typeof s === 'string' && s.split('.').length === 3;

const normalizeUser = (raw) => {
  if (!raw) return null;

  if (typeof raw === 'string') {
    if (isJwt(raw)) return { token: raw };
    try {
      raw = JSON.parse(raw);
    } catch {
      return null;
    }
  }
  if (raw.token && isJwt(raw.token)) return { ...raw, token: raw.token };

  if (raw.data && isJwt(raw.data)) return { ...raw, token: raw.data };
  if (raw.accessToken && isJwt(raw.accessToken)) return { ...raw, token: raw.accessToken };

  if (typeof raw === 'object') {
    for (const k of Object.keys(raw)) {
      if (typeof raw[k] === 'string' && isJwt(raw[k])) {
        return { ...raw, token: raw[k] };
      }
    }
  }

  return null;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  useEffect(() => {
    try {
      const savedRaw = localStorage.getItem('user');
      const normalized = normalizeUser(savedRaw ? JSON.parse(savedRaw) : null) || normalizeUser(savedRaw);
      if (normalized) setUser(normalized);
    } catch (err) {
      console.warn('AuthProvider: failed to read saved user', err);
    }
  }, []);

  const login = (payload) => {
    const normalized = normalizeUser(payload);
    if (!normalized) {
      console.error('AuthProvider.login: payload does not contain a valid JWT', payload);
      return;
    }
    localStorage.setItem('user', JSON.stringify(normalized));
    setUser(normalized);
  };

  const logout = () => {
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

import React, { createContext, useState, useContext, useEffect } from 'react';
import * as api from '../services/api';

const AuthContext = createContext();

export const useAuth = () => {
  return useContext(AuthContext);
};

const getInitialAuthData = () => {
  const storedData = localStorage.getItem('authData');
  if (storedData) { 
    try {
      return JSON.parse(storedData);
    } catch (e) {
      console.error('Failed to parse authData, clearing storage:', e);
      localStorage.removeItem('authData');
      return null;
    }
  }
  return null; 
};

export const AuthProvider = ({ children }) => {
  const [authData, setAuthData] = useState(getInitialAuthData());

  useEffect(() => {
    const handleStorageChange = (e) => {
      if (e.key === 'authData') {
        setAuthData(getInitialAuthData());
      }
    };

    window.addEventListener('storage', handleStorageChange);
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);
  const login = async (username, password) => {
    const { token, role, username: loggedInUsername } = await api.login(
      username,
      password
    );
    const users = await api.getUsers(token);
    const currentUser = users.find(
      (user) => user.username === loggedInUsername
    );
    const userId = currentUser ? currentUser.id : null;
    const data = { token, role, userId, username: loggedInUsername };
    setAuthData(data);
    localStorage.setItem('authData', JSON.stringify(data));
  };

  const logout = () => {
    setAuthData(null);
    localStorage.removeItem('authData');
  };

  const value = {
    ...authData,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

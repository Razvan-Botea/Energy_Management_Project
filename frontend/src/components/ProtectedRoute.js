import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Navigate } from 'react-router-dom';

// This component checks if a user is logged in and has the correct role
const ProtectedRoute = ({ children, role }) => {
  const auth = useAuth();

  if (!auth?.token) {
    // Not logged in
    return <Navigate to="/login" replace />;
  }

  if (role && auth.role !== role) {
    // Logged in, but wrong role
    return <Navigate to="/" replace />;
  }

  // Logged in and has correct role (or no role required)
  return children;
};

export default ProtectedRoute;

import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Navigate } from 'react-router-dom';

function Home() {
  const auth = useAuth();

  if (!auth?.token) {
    return <Navigate to="/login" replace />;
  }

  if (auth.role === 'ADMIN') {
    return <Navigate to="/admin" replace />;
  }

  if (auth.role === 'USER') {
    return <Navigate to="/dashboard" replace />;
  }

  return <Navigate to="/login" replace />;
}

export default Home;

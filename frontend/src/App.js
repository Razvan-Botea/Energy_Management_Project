import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import './App.css'; // Import our stylesheet

import LoginPage from './pages/LoginPage';
import Home from './pages/Home';
import AdminDashboard from './pages/AdminDashboard';
import UserDashboard from './pages/UserDashboard';
import ProtectedRoute from './components/ProtectedRoute';

const Layout = ({ children }) => {
  const auth = useAuth();
  return (
    <>
      {auth?.token && (
        <nav>
          <h1>My IoT App</h1>
          <span>
            Logged in as <strong>{auth.username}</strong> ({auth.role})
          </span>
          <button onClick={auth.logout}>Logout</button>
        </nav>
      )}
      <main>{children}</main>
    </>
  );
};

function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/login" element={<LoginPage />} />

        {/* Home page redirects based on role */}
        <Route path="/" element={<Home />} />

        {/* Admin Page */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute role="ADMIN">
              <AdminDashboard />
            </ProtectedRoute>
          }
        />

        {/* User (Client) Page */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute role="USER">
              <UserDashboard />
            </ProtectedRoute>
          }
        />

        {/* Fallback route */}
        <Route path="*" element={<Home />} />
      </Routes>
    </Layout>
  );
}

export default App;

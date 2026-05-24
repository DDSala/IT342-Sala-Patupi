import React, { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';

import Login from './features/authentication/Login';
import Register from './features/authentication/Register';
import Profile from './features/authentication/Profile';

import Dashboard from './features/appointments/Dashboard'; 
import AdminDashboard from './features/appointments/AdminDashboard'; 
import AdminHistory from './features/appointments/AdminHistory'; 

import AdminCustomer from './features/services/AdminCustomer'; 
import Barbers from './features/services/Barbers';


import ProtectedRoute from './common/components/ProtectedRoute';
function App() {
  useEffect(() => {
    const handleTabClose = () => {
      sessionStorage.clear();
      localStorage.removeItem('user');
    };
    window.addEventListener('beforeunload', handleTabClose);
    return () => window.removeEventListener('beforeunload', handleTabClose);
  }, []);

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* ADMIN ONLY (Role 1) */}
        <Route path="/admin" element={
          <ProtectedRoute allowedRoles={[1]}>
            <AdminDashboard />
          </ProtectedRoute>
        } />
        
        <Route path="/barbers" element={
          <ProtectedRoute allowedRoles={[1]}>
            <Barbers />
          </ProtectedRoute>
        } />

        <Route path="/admin-customers" element={
          <ProtectedRoute allowedRoles={[1]}>
            <AdminCustomer />
          </ProtectedRoute>
        } />

        <Route path="/admin-history" element={
          <ProtectedRoute allowedRoles={[1]}>
            <AdminHistory />
          </ProtectedRoute>
        } />

        {/* BARBERS & CUSTOMERS (Role 2, 3) */}
        <Route path="/dashboard" element={
          <ProtectedRoute allowedRoles={[2, 3]}>
            <Dashboard />
          </ProtectedRoute>
        } /> 

        <Route path="/profile" element={
          <ProtectedRoute allowedRoles={[2, 3]}>
            <Profile />
          </ProtectedRoute>
        } /> 



        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </Router>
  );
}

export default App;
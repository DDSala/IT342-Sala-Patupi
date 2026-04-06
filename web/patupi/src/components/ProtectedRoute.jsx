import React from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const savedUser = sessionStorage.getItem('user'); 
  
  if (!savedUser) {
    return <Navigate to="/login" replace />;
  }

  const user = JSON.parse(savedUser);

  if (allowedRoles && !allowedRoles.includes(user.roleId)) {
   
    if (user.roleId === 1) return <Navigate to="/admin" replace />;
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};


export default ProtectedRoute;
import React, { useState, useEffect } from 'react';
import "./register.css";
import { Link, useNavigate } from 'react-router-dom';

const Register = () => {
  const [formData, setFormData] = useState({
    fullName: '',
    email: '',
    address: '',
    password: '',
    confirmPassword: ''
  });
  const [loading, setLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState(''); 
  const navigate = useNavigate();

  useEffect(() => {
    const loggedInUser = localStorage.getItem('user');
    if (loggedInUser) {
      navigate('/dashboard', { replace: true });
    }
  }, [navigate]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    if (errorMessage) setErrorMessage(''); 
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (formData.password !== formData.confirmPassword) {
      setErrorMessage("Passwords do not match!");
      return;
    }

    setLoading(true);
    try {
      const response = await fetch('http://localhost:8080/api/auth/register-profile', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          fullName: formData.fullName,
          email: formData.email.trim(),
          address: formData.address, 
          password: formData.password,
          roleId: 3 
        }),
      });

      const result = await response.json();

      if (response.ok) {
        setIsSuccess(true);
        setTimeout(() => navigate('/login'), 3000);
      } else {
        throw new Error(result.message || "Registration failed");
      }
    } catch (err) {
      setErrorMessage(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (isSuccess) {
    return (
      <div className="auth-container-centered">
        <div className="auth-card success-card">
          <div className="success-icon">✓</div>
          <h1 style={{ color: '#d4af37' }}>Welcome, {formData.fullName.split(' ')}!</h1>
          <p style={{ color: '#d1d1ca', textAlign: 'center' }}>Account created successfully.</p>
          <div className="loading-bar-container"><div className="loading-bar-progress"></div></div>
        </div>
      </div>
    );
  }

  return (
    <div className="auth-container-centered">
      <h1>Create Account</h1>
      <div className="auth-card">
        {errorMessage && (
          <div className="error-banner">
            <span className="error-icon">!</span>
            {errorMessage}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Full Name</label>
            <div className={`input-wrapper ${errorMessage.includes('name') ? 'input-error' : ''}`}>
              <input name="fullName" type="text" placeholder="John Doe" onChange={handleChange} required />
            </div>
          </div>
          
          <div className="form-group">
            <label>Email Address</label>
            <div className={`input-wrapper ${errorMessage.includes('Email') ? 'input-error' : ''}`}>
              <input name="email" type="email" placeholder="email@example.com" onChange={handleChange} required />
            </div>
          </div>

          {/* NEW ADDRESS FIELD */}
          <div className="form-group">
            <label>Location / Address</label>
            <div className="input-wrapper">
              <input name="address" type="text" placeholder="Cebu City, Philippines" onChange={handleChange} required />
            </div>
          </div>

          <div className="form-group">
            <label>Password</label>
            <div className="input-wrapper">
              <input name="password" type="password" placeholder="••••••••" onChange={handleChange} required />
            </div>
          </div>

          <div className="form-group">
            <label>Confirm Password</label>
            <div className="input-wrapper">
              <input name="confirmPassword" type="password" placeholder="••••••••" onChange={handleChange} required />
            </div>
          </div>

          <button type="submit" className="gold-btn" disabled={loading}>
            {loading ? 'Processing...' : 'Create Account →'}
          </button>
        </form>
        
        <div className="back-to-login-container">
          Already have an account? <Link to="/login" className="gold-link">Back to Login</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
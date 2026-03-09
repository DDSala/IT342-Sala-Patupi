import React, { useState } from 'react';
import '../css/login.css';
import { Link, useNavigate } from 'react-router-dom';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false); 
  const [errorMessage, setErrorMessage] = useState('');
  const [userName, setUserName] = useState(''); 
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage('');

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.trim(), password: password }),
      });

      const result = await response.json();

      if (response.ok) {
        setUserName(result.fullName.split(' ')[0]); 
        setIsSuccess(true); 
        localStorage.setItem('user', JSON.stringify(result));
        
        setTimeout(() => {
          navigate('/dashboard');
        }, 2200);
      } else {
        throw new Error(result.message || "Invalid credentials");
      }
    } catch (err) {
      setErrorMessage(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (isSuccess) {
    return (
      <div className="success-screen-overlay">
        <div className="success-message-box">
          <div className="gold-check-circle">✓</div>
          <h1 className="gold-text-fade">Welcome Back, {userName}</h1>
          <p className="subtitle-fade">Preparing your grooming station...</p>
          <div className="premium-progress-bar">
            <div className="progress-fill"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="split-screen">
      <div className="image-pane">
        <h2>Precision in every cut.</h2>
        <p className="subtitle" style={{color: '#d1d1ca'}}>Book your next session with the masters of grooming.</p>
      </div>
      <div className="form-pane">
        <div className="brand-logo">✂</div>
        <h1>Patupi</h1>
        <p className="subtitle">Welcome back! Please enter your details.</p>

        {errorMessage && (
          <div className="error-banner-login">
            <span className="error-icon">!</span>
            {errorMessage}
          </div>
        )}

        <form onSubmit={handleLogin}>
          <div className="form-group">
            <label>Email Address</label>
            <div className={`input-wrapper ${errorMessage ? 'input-error-glow' : ''}`}>
              <input type="email" placeholder="name@example.com" onChange={(e) => setEmail(e.target.value)} required />
            </div>
          </div>
          <div className="form-group">
            <label>Password</label>
            <div className={`input-wrapper ${errorMessage ? 'input-error-glow' : ''}`}>
              <input type="password" placeholder="••••••••" onChange={(e) => setPassword(e.target.value)} required />
            </div>
          </div>
          <button type="submit" className="gold-btn" disabled={loading}>
            {loading ? 'Verifying...' : 'Login to Account'}
          </button>
        </form>
        
        <div className="divider"><span>OR CONTINUE WITH</span></div>
        <div className="social-buttons">
          <button className="social-btn" type="button">Google</button>
          <button className="social-btn" type="button">Facebook</button>
        </div>
        <p className="subtitle" style={{marginTop: '32px'}}>
          Don't have an account? <Link to="/register" style={{color: '#d4af37', fontWeight: '600'}}>Sign up for free</Link>
        </p>
      </div>
    </div>
  );
};

export default Login;
import React, { useState, useEffect } from 'react';
import '../css/login.css';
import { Link, useNavigate } from 'react-router-dom';
import { GoogleLogin } from '@react-oauth/google';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState(''); 
  const [tempEmail, setTempEmail] = useState(''); 
  const [showOtpInput, setShowOtpInput] = useState(false); 
  
  const [loading, setLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false); 
  const [errorMessage, setErrorMessage] = useState('');
  const [userName, setUserName] = useState(''); 
  const navigate = useNavigate();

  useEffect(() => {
   
    const loggedInUser = sessionStorage.getItem('user');
    if (loggedInUser) {
      const user = JSON.parse(loggedInUser);
      navigate(user.roleId === 1 ? '/admin' : '/dashboard', { replace: true });
    }


  }, [navigate]);

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
      if (response.ok) processLoginSuccess(result);
      else throw new Error(result.message || "Invalid credentials");
    } catch (err) {
      setErrorMessage(err.message);
    } finally {
      setLoading(false);
    }
  };


  /*Google Login*/
  const handleGoogleLogin = async (credential) => {
    setLoading(true);
    setErrorMessage('');
    try {
      const response = await fetch('http://localhost:8080/api/auth/google', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token: credential }),
      });

      const result = await response.json();

      if (response.ok) {
        if (result.status === "PENDING_OTP") {
          setTempEmail(result.email);
          setShowOtpInput(true);
        } else {
          processLoginSuccess(result);
        }
      } else {
        throw new Error(result.message || "Google authentication failed");
      }
    } catch (err) {
      setErrorMessage(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleOtpVerify = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMessage('');
    try {
      const response = await fetch('http://localhost:8080/api/auth/verify-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: tempEmail, otp: otp }),
      });

      const result = await response.json();
      if (response.ok) processLoginSuccess(result);
      else throw new Error(result.message || "Invalid or expired OTP");
    } catch (err) {
      setErrorMessage(err.message);
    } finally {
      setLoading(false);
    }
  };

  const processLoginSuccess = (userData) => {
    const nameToDisplay = userData.fullName ? userData.fullName.split(' ') : 'User';
    setUserName(nameToDisplay); 
    setIsSuccess(true); 
    
    
    sessionStorage.setItem('user', JSON.stringify(userData));
    
    setTimeout(() => {
      navigate(userData.roleId === 1 ? '/admin' : '/dashboard', { replace: true });
    }, 2200);
  };

  if (isSuccess) {
    return (
      <div className="success-screen-overlay">
        <div className="success-message-box">
          <div className="gold-check-circle">✓</div>
          <h1 className="gold-text-fade">Welcome Back, {userName}</h1>
          <div className="premium-progress-bar"><div className="progress-fill"></div></div>
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
        
        {!showOtpInput ? (
          <>
            <p className="subtitle">Welcome back! Please enter your details.</p>

            {errorMessage && (
              <div className="error-banner-login">
                <span className="error-icon">!</span>{errorMessage}
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
                {loading ? 'Verifying...' : 'Login'}
              </button>
            </form>
            
            <div className="divider"><span>OR CONTINUE WITH</span></div>
            <div className="social-buttons">
              <GoogleLogin
                onSuccess={res => handleGoogleLogin(res.credential)}
                onError={() => setErrorMessage("Google Login Failed")}
                useOneTap theme="filled_black" shape="pill" width="100%"
              />
            </div>
          </>
        ) : (
          <div className="otp-container">
            <p className="subtitle">Verification code sent to <strong>{tempEmail}</strong></p>
            
            {errorMessage && (
              <div className="error-banner-login"><span className="error-icon">!</span>{errorMessage}</div>
            )}

            <form onSubmit={handleOtpVerify}>
              <div className="form-group">
                <label>Enter 6-Digit Code</label>
                <div className="input-wrapper">
                  <input 
                    type="text" 
                    maxLength="6" 
                    placeholder="123456" 
                    style={{ textAlign: 'center', letterSpacing: '8px', fontSize: '24px' }}
                    onChange={(e) => setOtp(e.target.value)} 
                    required 
                  />
                </div>
              </div>
              <button type="submit" className="gold-btn" disabled={loading}>
                {loading ? 'Confirming...' : 'Verify & Continue'}
              </button>
              <button 
                type="button" 
                className="text-btn" 
                onClick={() => setShowOtpInput(false)} 
                style={{marginTop: '15px', background: 'none', border: 'none', color: '#888', cursor: 'pointer'}}
              >
                ← Back to Login
              </button>
            </form>
          </div>
        )}

        <p className="subtitle" style={{marginTop: '32px'}}>
          Don't have an account? <Link to="/register" style={{color: '#d4af37', fontWeight: '600'}}>Sign up for free</Link>
        </p>
      </div>
    </div>
  );
};

export default Login;
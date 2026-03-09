import React from 'react';
import '../css/login.css';
import { Link } from 'react-router-dom';

const Login = () => {
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
        <form>
          <div className="form-group">
            <label>Email Address</label>
            <div className="input-wrapper"><input type="email" placeholder="name@example.com" /></div>
          </div>
          <div className="form-group">
            <label>Password</label>
            <div className="input-wrapper"><input type="password" placeholder="••••••••" /></div>
          </div>
          <button type="submit" className="gold-btn">Login to Account</button>
        </form>
        <div className="divider"><span>OR CONTINUE WITH</span></div>
        <div className="social-buttons">
          <button className="social-btn">Google</button>
          <button className="social-btn">Facebook</button>
        </div>
        <p className="subtitle" style={{marginTop: '32px'}}>
          Don't have an account? <Link to="/register" style={{color: '#d4af37', fontWeight: '600'}}>Sign up for free</Link>
        </p>
      </div>
    </div>
  );
};

export default Login;
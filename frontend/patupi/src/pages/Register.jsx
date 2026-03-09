import React from 'react';
import '../css/register.css';
import { Link } from 'react-router-dom';

const Register = () => {
  return (
    <div className="auth-container-centered">
      <h1 style={{textAlign: 'center'}}>Create Account</h1>
      <p className="subtitle" style={{textAlign: 'center'}}>Join Patupi for a premium grooming experience</p>
      
      <div className="auth-card">
        <form>
          <div className="form-group">
            <label>Full Name</label>
            <div className="input-wrapper">
              <input type="text" placeholder="John Doe" />
            </div>
          </div>

          <div className="form-group">
            <label>Email Address</label>
            <div className="input-wrapper">
              <input type="email" placeholder="email@example.com" />
            </div>
          </div>

          <div className="form-group">
            <label>Password</label>
            <div className="input-wrapper">
              <input type="password" placeholder="••••••••" />
            </div>
          </div>

          {/* ADDED: Confirm Password Field */}
          <div className="form-group">
            <label>Confirm Password</label>
            <div className="input-wrapper">
              <input type="password" placeholder="••••••••" />
            </div>
          </div>

          <button type="submit" className="gold-btn">
            Create Account →
          </button>
        </form>

        <div className="divider"><span>OR</span></div>

        <p style={{textAlign: 'center', fontSize: '0.9rem', color: '#a1a19a'}}>
          Already have an account? <Link to="/login" className="gold-text">Back to Login</Link>
        </p>
      </div>

      <p className="legal-text">
        By creating an account, you agree to our Terms of Service and Privacy Policy.
      </p>
    </div>
  );
};

export default Register;
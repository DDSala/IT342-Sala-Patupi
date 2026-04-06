import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import '../css/profile.css';

const Profile = () => {
  const navigate = useNavigate();
  const [isEditing, setIsEditing] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");

  const [user, setUser] = useState({
    userId: null,
    fullName: "",
    email: "",
    address: "",
    roleId: null
  });

 
  const [tempUser, setTempUser] = useState(null);

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });

  useEffect(() => {
   
    const savedUser = sessionStorage.getItem('user');
    if (savedUser) {
      setUser(JSON.parse(savedUser));
    } else {
      navigate('/login');
    }
  }, [navigate]);

  const triggerSuccess = (msg) => {
    setSuccessMessage(msg);
    setTimeout(() => setSuccessMessage(""), 1000);
  };

  const handleEditToggle = () => {
    setTempUser({ ...user }); 
    setIsEditing(true);
  };

  const handleCancelEdit = () => {
    setUser({ ...tempUser }); 
    setIsEditing(false);
  };

 
  const handleSaveProfile = async () => {
    if (!user.userId) {
      alert("Error: User ID not found.");
      return;
    }

    setIsSaving(true);
    try {
      const response = await fetch(`http://localhost:8080/api/users/${user.userId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(user), 
      });

      if (response.ok) {
        const updatedUser = await response.json();
        setUser(updatedUser);
   
        sessionStorage.setItem('user', JSON.stringify(updatedUser)); 
        setIsEditing(false);
        triggerSuccess("Profile updated successfully!");
      } else {
        alert("Failed to update profile in database.");
      }
    } catch (error) {
      console.error("Fetch Error:", error);
    } finally {
      setIsSaving(false);
    }
  };

  // UPDATE PASSWORD
  const handlePasswordUpdate = async (e) => {
    e.preventDefault();
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      alert("Passwords do not match!");
      return;
    }

    setIsSaving(true);
    try {
      const response = await fetch(`http://localhost:8080/api/users/${user.userId}/password`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          currentPassword: passwordData.currentPassword,
          newPassword: passwordData.newPassword
        }),
      });

      if (response.ok) {
        setShowPasswordModal(false);
        setPasswordData({ currentPassword: '', newPassword: '', confirmPassword: '' });
        triggerSuccess("Password updated successfully!");
      } else {
        const errorData = await response.json();
        alert(errorData.message || "Incorrect current password.");
      }
    } catch (error) {
      console.error("Auth Error:", error);
    } finally {
      setIsSaving(false);
    }
  };

  const handleLogout = () => {
    
    sessionStorage.removeItem('user');
    navigate('/login', { replace: true });
  };

  const getInitial = (name) => name ? name.charAt(0).toUpperCase() : "?";

  return (
    <div className="profile-container">
      <aside className="sidebar">
        <div className="brand-header">
          <div className="brand-icon-box">✂</div>
          <div className="brand-text-stack">
            <span className="brand-name">Patupi</span>
            <span className="brand-sub">PROFILE</span>
          </div>
        </div>
        <nav className="sidebar-nav">
          <Link to="/dashboard" className="nav-item">Home</Link>
          <Link to="/profile" className="nav-item active">Profile</Link>
        </nav>
        <button className="logout-btn" onClick={() => setShowLogoutModal(true)}>↪ Sign-out</button>
      </aside>

      <main className="profile-main">
        <header className="profile-top-bar">
          <h2 className="page-title">Customer Profile</h2>
        </header>

        <section className="profile-card hero">
          <div className="avatar-wrapper">
            <div className="avatar-circle">{getInitial(user.fullName)}</div>
            <div className="edit-badge-small">✎</div>
          </div>
          <h1 className="hero-fullname">{user.fullName}</h1>
          <div className="hero-actions">
            {isEditing ? (
              <>
                <button className="m-btn-cancel" onClick={handleCancelEdit} disabled={isSaving}>Cancel</button>
                <button className="btn-edit-profile save-mode" onClick={handleSaveProfile} disabled={isSaving}>
                  {isSaving ? <span className="spinner"></span> : "Save Changes"}
                </button>
              </>
            ) : (
              <button className="btn-edit-profile" onClick={handleEditToggle}>Edit Profile</button>
            )}
          </div>
        </section>

        <section className="profile-card details">
          <div className="card-top"><h3><span>📄</span> Account Details</h3></div>
          <div className="details-info-grid">
            <div className="info-item">
              <label>FULL NAME</label>
              {isEditing ? <input className="edit-input" value={user.fullName} onChange={(e) => setUser({...user, fullName: e.target.value})} /> : <p>{user.fullName}</p>}
            </div>
            <div className="info-item">
              <label>EMAIL ADDRESS</label>
              {isEditing ? <input className="edit-input" value={user.email} onChange={(e) => setUser({...user, email: e.target.value})} /> : <p>{user.email}</p>}
            </div>
            <div className="info-item">
              <label>LOCATION (ADDRESS)</label>
              {isEditing ? <input className="edit-input" value={user.address} onChange={(e) => setUser({...user, address: e.target.value})} /> : <p>{user.address || "No Address Set"}</p>}
            </div>
          </div>
        </section>

        <section className="profile-card security">
          <div className="card-top"><h3><span>🛡️</span> Security & Privacy</h3></div>
          <div className="security-row">
            <div className="sec-info"><span className="sec-icon">🔑</span><p>Change Password</p></div>
            <button className="update-pwd-btn" onClick={() => setShowPasswordModal(true)}>Update Password</button>
          </div>
        </section>
      </main>

      {successMessage && (
        <div className="toast-overlay">
          <div className="success-toast">
            <div className="check-icon-circle">✓</div>
            <p>{successMessage}</p>
          </div>
        </div>
      )}

      {showPasswordModal && (
        <div className="modal-overlay">
          <div className="modal-box password-modal">
            <h3>Update Password</h3>
            <form onSubmit={handlePasswordUpdate}>
              <div className="input-group">
                <label>Current Password</label>
                <input type="password" required onChange={(e) => setPasswordData({...passwordData, currentPassword: e.target.value})} />
              </div>
              <div className="input-group">
                <label>New Password</label>
                <input type="password" required onChange={(e) => setPasswordData({...passwordData, newPassword: e.target.value})} />
              </div>
              <div className="input-group">
                <label>Confirm New Password</label>
                <input type="password" required onChange={(e) => setPasswordData({...passwordData, confirmPassword: e.target.value})} />
              </div>
              <div className="modal-btns">
                <button type="button" className="m-btn-cancel" onClick={() => setShowPasswordModal(false)}>Cancel</button>
                <button type="submit" className="m-btn-confirm" disabled={isSaving}>
                  {isSaving ? "Updating..." : "Update"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showLogoutModal && (
        <div className="modal-overlay active">
          <div className="modal-box logout-animation">
            <h3>Confirm Logout</h3>
            <p>Ready to head out?</p>
            <div className="modal-btns">
              <button className="m-btn-cancel" onClick={() => setShowLogoutModal(false)}>Stay</button>
              <button className="m-btn-confirm" onClick={handleLogout}>Logout</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  
};

export default Profile;
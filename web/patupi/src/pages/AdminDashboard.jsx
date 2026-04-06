import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { 
  Sun, 
  Cloud, 
  CloudRain, 
  Bell, 
  Scissors, 
  Trash2, 
  LogOut, 
  AlertCircle,
  Users, 
  ClipboardList, 
  History,
  UserCheck
} from 'lucide-react';
import '../css/admin-dashboard.css';

const AdminDashboard = () => {
  const navigate = useNavigate();
  
  const [stats, setStats] = useState({ pending: 0, active: 0, total: 0 });
  const [appointments, setAppointments] = useState([]); 
  const [barbers, setBarbers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [selectedAppt, setSelectedAppt] = useState(null);
  const [selectedBarberId, setSelectedBarberId] = useState('');
  const [adminName, setAdminName] = useState('Admin');
  const [weather, setWeather] = useState({ temp: '--', code: 0 });

  useEffect(() => {
    const loggedInUser = sessionStorage.getItem('user');
    
    if (!loggedInUser) {
      navigate('/login', { replace: true });
      return;
    }

    const user = JSON.parse(loggedInUser);
    if (Number(user.roleId) !== 1) {
      navigate('/dashboard', { replace: true });
      return;
    }

    // Set Admin Name from session
    const firstName = user.fullName ? user.fullName.split(' ') : 'Admin';
    setAdminName(firstName);

    fetchAdminData();
    fetchBarbers();
    fetchWeather();

    const interval = setInterval(() => {
      fetchAdminData();
    }, 30000); 

    return () => clearInterval(interval);
  }, [navigate]);

  const fetchBarbers = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/users/all');
      setBarbers(res.data.filter(u => Number(u.roleId) === 2));
    } catch (err) {
      console.error("Failed to fetch barbers:", err);
    }
  };

  const fetchWeather = async () => {
    try {
      const res = await axios.get(
        'https://api.open-meteo.com/v1/forecast?latitude=10.3157&longitude=123.8854&current_weather=true'
      );
      setWeather({
        temp: Math.round(res.data.current_weather.temperature),
        code: res.data.current_weather.weathercode
      });
    } catch (err) {
      console.error("Weather failed:", err);
    }
  };

  
  const fetchAdminData = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/appointments/all');
      const data = res.data;

      const pending = data.filter(a => a.status === 'PENDING').length;
      const active = data.filter(a => a.status === 'CONFIRMED').length;
      const total = data.length;
      setStats({ pending, active, total });

      const activeQueue = data.filter(
        a => a.status === 'PENDING' || a.status === 'CONFIRMED'
      );

      const sorted = activeQueue.sort((a, b) => {
        if (!a.scheduledAt) return 1;
        if (!b.scheduledAt) return -1;
        return new Date(a.scheduledAt) - new Date(b.scheduledAt);
      });

      setAppointments(sorted);
      setLoading(false);
    } catch (err) {
      console.error("Dashboard Fetch Error:", err);
      setLoading(false);
    }
  };

const handleAssignSubmit = async () => {
  if (!selectedBarberId || !selectedAppt) return alert("Please select a barber.");
  
  try {
    
    await axios.put(`http://localhost:8080/api/appointments/${selectedAppt.appointmentId}/assign`, {
      barberId: selectedBarberId
    });
    
    setShowAssignModal(false);
    setSelectedBarberId('');
    setSelectedAppt(null);
    fetchAdminData();
  } catch (err) {
    alert("Assignment failed: " + (err.response?.data?.message || "Server Error"));
  }
};

  const handleDelete = async (id) => {
    if (window.confirm("Permanently delete this appointment?")) {
      try {
        await axios.delete(`http://localhost:8080/api/appointments/${id}`);
        
        setAppointments(prev => prev.filter(appt => appt.id !== id));
        fetchAdminData(); 
      } catch (err) {
        console.error("Delete Error:", err);
        alert("Delete failed. Ensure the backend DeleteMapping is implemented.");
      }
    }
  };

  const getWeatherIcon = () => {
    const code = weather.code;
    if (code === 0) return <Sun size={18} className="text-yellow-500" />;
    if (code >= 1 && code <= 3) return <Cloud size={18} className="text-gray-400" />;
    if (code >= 51) return <CloudRain size={18} className="text-blue-400" />;
    return <Sun size={18} />;
  };

  return (
    <div className="admin-container">
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <div className="admin-logo-box"><Scissors size={18} /></div>
          <span className="admin-brand-text">Patupi Admin</span>
        </div>
        
        <nav className="admin-nav">
          <div className="admin-nav-item active" onClick={() => navigate('/admin')}>
            <ClipboardList size={16} style={{marginRight: '10px'}} /> Dashboard
          </div>
          <div className="admin-nav-item" onClick={() => navigate('/barbers')}>
            <Users size={16} style={{marginRight: '10px'}} /> Barbers
          </div>
          <div className="admin-nav-item" onClick={() => navigate('/admin-customers')}>
            <Users size={16} style={{marginRight: '10px'}} /> Customers
          </div>
          <div className="admin-nav-item" onClick={() => navigate('/admin-history')}>
            <History size={16} style={{marginRight: '10px'}} /> History
          </div>
        </nav>

        <button className="admin-logout" onClick={() => setShowLogoutModal(true)}>
          <LogOut size={16} style={{marginRight: '8px'}} /> Sign Out
        </button>
      </aside>

      <main className="admin-content">
        <header className="admin-top-bar">
          <div>
            <h2>Admin Dashboard</h2>
            <p className="admin-welcome">Welcome back, {adminName}</p>
          </div>
          <div className="admin-meta">
            <div className="admin-weather-chip">{getWeatherIcon()}<span>{weather.temp}°C</span></div>
            <div className="admin-notif-bell"><Bell size={20} /><span className="bell-dot"></span></div>
            <div className="admin-date-display">
              {new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
            </div>
          </div>
        </header>

        <div className="admin-stats-grid">
          <div className="admin-stat-card"><label>Pending</label><h3>{stats.pending}</h3></div>
          <div className="admin-stat-card active-card">
            <label>Active</label>
            <h3>{stats.active < 10 ? `0${stats.active}` : stats.active}</h3>
            <div className="stat-check">✓</div>
          </div>
          <div className="admin-stat-card"><label>Total Bookings</label><h3>{stats.total}</h3></div>
        </div>

        <section className="admin-section-card">
          <div className="section-title">The Queue</div>
          <div className="table-container">
            <table className="queue-table">
              <thead>
                <tr>
                  <th>CUSTOMER</th>
                  <th>BARBER</th>
                  <th>SERVICE</th>
                  <th>DESCRIPTION</th>
                  <th>DATE / TIME</th>
                  <th>STATUS</th>
                  <th>ACTION</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr><td colSpan="7" className="empty-row">Loading appointments...</td></tr>
                ) : appointments.length > 0 ? (
                  appointments.map((item) => (
                    <tr key={item.appointmentId}>
                      <td><span className="cust-name">{item.customerName || "Guest"}</span></td>
                      <td>
                        {item.status?.toUpperCase() === 'CONFIRMED' ? (
                          <span className="assigned-barber-tag">
                            <UserCheck size={12} style={{ marginRight: '6px' }} />
                            {item.barberName || "Assigned"}
                          </span>
                        ) : (
                          <span className="text-muted italic">Waiting...</span>
                        )}
                      </td>
                      <td><span className="cust-service">{item.service || "Standard"}</span></td>
                      <td className="text-muted truncate-cell">{item.description || "—"}</td>
                      <td>
                        {item.scheduledAt ? (
                          <div className="time-stack">
                            <div className="date-sub">{new Date(item.scheduledAt).toLocaleDateString()}</div>
                            <div className="time-highlight">
                              {new Date(item.scheduledAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                            </div>
                          </div>
                        ) : "Not Set"}
                      </td>
                      <td><span className={`status-badge ${item.status?.toLowerCase()}`}>{item.status}</span></td>
                      <td>
                        <div style={{ display: 'flex', gap: '8px' }}>
                          {item.status?.toUpperCase() === 'PENDING' && (
                            <button 
                              className="btn-assign-action" 
                              onClick={() => { setSelectedAppt(item); setShowAssignModal(true); }}
                            >
                              <div className="icon-wrapper">
                                <UserCheck size={16} />
                                <span className="button-text">Assign</span>
                              </div>
                            </button>
                          )}
<button className="btn-delete-admin" onClick={() => handleDelete(item.appointmentId)}>
                            <Trash2 size={14} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr><td colSpan="7" className="empty-row">No active appointments in queue.</td></tr>
                )}
              </tbody>
            </table>
          </div>
        </section>
      </main>

      {/* ASSIGN BARBER MODAL */}
      {showAssignModal && (
        <div className="modern-modal-overlay">
          <div className="modern-modal-container assign-modal-premium">
            <div className="modal-header-accent"></div>
            <div className="modal-content-wrapper">
              <div className="modal-icon-header">
                <Scissors size={28} className="gold-text" />
                <h3>Barber Assignment</h3>
              </div>
              <div className="appointment-mini-card">
                <p className="label">RESERVED FOR</p>
                <p className="value">{selectedAppt?.customerName}</p>
                <p className="label" style={{marginTop: '10px'}}>SERVICE</p>
                <p className="value">{selectedAppt?.service}</p>
              </div>
              <div className="selection-group">
                <label>Select Professional Barber</label>
                <div className="custom-select-wrapper">
                  <select 
                    value={selectedBarberId}
                    onChange={(e) => setSelectedBarberId(e.target.value)}
                    className="premium-select"
                  >
                    <option value="">Choose from available staff...</option>
                    {barbers.map(b => (
                      <option key={b.userId} value={b.userId}>
                        💈 {b.fullName}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="modal-footer-btns">
                <button className="btn-cancel-flat" onClick={() => setShowAssignModal(false)}>Discard</button>
                <button className="btn-confirm-gold" onClick={handleAssignSubmit}>Confirm</button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* LOGOUT MODAL */}
      {showLogoutModal && (
        <div className="modern-modal-overlay">
          <div className="modern-modal-container">
            <div className="modal-icon-container glow-warning">
              <AlertCircle className="icon-warning" color="#ff9800" size={32} />
            </div>
            <h3>Confirm Logout</h3>
            <p>Are you sure you want to exit the management portal?</p>
            <div className="modal-action-row">
              <button className="btn-modal-outline" onClick={() => setShowLogoutModal(false)}>Nevermind</button>
              <button className="btn-modal-premium" onClick={() => { sessionStorage.clear(); navigate('/login'); }}>Sign Out</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  
};

export default AdminDashboard;
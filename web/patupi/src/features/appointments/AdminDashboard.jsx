import React, { useState, useEffect, useRef } from 'react';
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
  UserCheck,
  X,
  Calendar,
  CheckCircle,
  XCircle
} from 'lucide-react';
import "./admin-dashboard.css";

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

  // --- Real-Time Notifications & Quick Polling Systems ---
  const [toastNotification, setToastNotification] = useState(null);
  const [adminNotifHistory, setAdminNotifHistory] = useState([]);
  const [isNotifDrawerOpen, setIsNotifDrawerOpen] = useState(false);
  
  const previousFullDataRef = useRef([]);
  const isInitialLoadRef = useRef(true);

  const [deleteModal, setDeleteModal] = useState({ isOpen: false, targetId: null });
  const [isDeleting, setIsDeleting] = useState(false);

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

    const firstName = user.fullName ? user.fullName.split(' ') : 'Admin';
    setAdminName(firstName);

    // Initial sequence
    fetchAdminData();
    fetchBarbers();
    fetchWeather();

    // High performance background polling interval (5 seconds matching customer channel)
    const pollInterval = setInterval(() => {
      fetchAdminData();
    }, 5000); 

    return () => clearInterval(pollInterval);
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

  const triggerAdminToast = (type, title, message) => {
    const trackingNotif = {
      id: Date.now(),
      type, // 'BOOKING' | 'CANCELLATION'
      title,
      message,
      timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    };
    setToastNotification(trackingNotif);
    setAdminNotifHistory(prev => [trackingNotif, ...prev]);

    // Self cleaning countdown auto-dismiss
    setTimeout(() => {
      setToastNotification(current => current?.id === trackingNotif.id ? null : current);
    }, 6000);
  };

  const fetchAdminData = async () => {
    try {
      const res = await axios.get('http://localhost:8080/api/appointments/all');
      const incomingRawData = res.data;

      // Evaluate states ONLY after system initial synchronization lock is true
      if (!isInitialLoadRef.current) {
        const structuralCachedData = previousFullDataRef.current;

        // 1. Scan for Newly Booked Sessions (FIXED: Added id lookup matching backend models)
        incomingRawData.forEach(newAppt => {
          const matchExisted = structuralCachedData.find(old => 
            (old.id || old.appointmentId) === (newAppt.id || newAppt.appointmentId)
          );
          if (!matchExisted) {
            triggerAdminToast(
              'BOOKING',
              'New Appointment! 💈',
              `${newAppt.customerName || 'Guest'} Has booked a new appointment.`
            );
          }
        });

        // 2. Scan for Live Cancelled Alerts (FIXED: Added id lookup matching backend models)
        incomingRawData.forEach(updatedAppt => {
          const previouslyExisted = structuralCachedData.find(old => 
            (old.id || old.appointmentId) === (updatedAppt.id || updatedAppt.appointmentId)
          );
          if (previouslyExisted && previouslyExisted.status !== 'CANCELLED' && updatedAppt.status === 'CANCELLED') {
            triggerAdminToast(
              'CANCELLATION',
              'Booking Cancelled ⚠️',
              `${updatedAppt.customerName || 'Guest'} Cancelled Their Appointment.`
            );
          }
        });
      } else {
        isInitialLoadRef.current = false;
      }

      // Save latest collection frame snapshot state
      previousFullDataRef.current = incomingRawData;

      const pending = incomingRawData.filter(a => a.status === 'PENDING').length;
      const active = incomingRawData.filter(a => a.status === 'CONFIRMED' || a.status === 'IN_PROGRESS' || a.status === 'IN PROGRESS').length;
      const total = incomingRawData.length;
      setStats({ pending, active, total });

      const activeQueue = incomingRawData.filter(a => {
        const statusClean = a.status?.toUpperCase() || '';
        return statusClean === 'PENDING' || 
               statusClean === 'CONFIRMED' || 
               statusClean === 'IN_PROGRESS' || 
               statusClean === 'IN PROGRESS';
      });

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
    
    // FIXED: Safely read database field ID target property from object reference
    const targetAppointmentId = selectedAppt.id || selectedAppt.appointmentId;

    try {
      await axios.put(`http://localhost:8080/api/appointments/${targetAppointmentId}/assign`, {
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

  const handleDelete = async () => {
    const { targetId } = deleteModal;
    setIsDeleting(true);
    try {
      await axios.delete(`http://localhost:8080/api/appointments/${targetId}`);
      // FIXED: Adjusted local UI filtering array conditions to wipe row out safely
      setAppointments(prev => prev.filter(appt => (appt.id || appt.appointmentId) !== targetId));
      setDeleteModal({ isOpen: false, targetId: null });
    } catch (err) {
      console.error("Delete Error:", err);
      alert("Delete failed.");
    } finally {
      setIsDeleting(false);
    }
  };

  const getWeatherIcon = () => {
    const code = weather.code;
    if (code === 0) return <Sun size={18} className="text-yellow-500" />;
    if (code >= 1 && code <= 3) return <Cloud size={18} className="text-gray-400" />;
    if (code >= 51) return <CloudRain size={18} className="text-blue-400" />;
    return <Sun size={18} />;
  };

  const getAvailableBarbersOptions = () => {
    const busyBarberIdsFromQueue = appointments
      .filter(appt => {
        const statusClean = appt.status?.toUpperCase() || '';
        return statusClean === 'IN_PROGRESS' || statusClean === 'IN PROGRESS';
      })
      .map(appt => Number(appt.barberId))
      .filter(id => !isNaN(id));

    return barbers.filter(barber => {
      const isBusyInQueue = busyBarberIdsFromQueue.includes(Number(barber.userId));
      const isProfileBusyStatus = barber.barberProfile?.status?.toLowerCase() === 'busy' || barber.status?.toLowerCase() === 'busy';
      
      return !isBusyInQueue && !isProfileBusyStatus;
    });
  };

  return (
    <div className="admin-container">
      
      {/* Dynamic Pop-up Dropdown Toast Banner */}
      {toastNotification && (
        <div 
          className={`realtime-notification-toast admin-toast ${toastNotification.type === 'CANCELLATION' ? 'toast-cancel-border' : 'toast-book-border'}`} 
          onClick={() => setIsNotifDrawerOpen(true)}
        >
          <div className="toast-accent"></div>
          <div className="toast-content-body">
            <h5>{toastNotification.title}</h5>
            <p>{toastNotification.message}</p>
          </div>
          <button className="toast-close-btn" onClick={(e) => { e.stopPropagation(); setToastNotification(null); }}>×</button>
        </div>
      )}

      {/* Historical Notification Panel Drawer Menu */}
      <div className={`notification-drawer-overlay ${isNotifDrawerOpen ? 'drawer-visible' : ''}`} onClick={() => setIsNotifDrawerOpen(false)}>
        <div className="notification-drawer" onClick={(e) => e.stopPropagation()}>
          <div className="drawer-header">
            <div className="header-title-flex">
              <Bell size={18} className="gold-text" />
              <h4>Admin Notifications</h4>
            </div>
            <button className="drawer-close-icon" onClick={() => setIsNotifDrawerOpen(false)}><X size={20}/></button>
          </div>
          <div className="drawer-scroll-content">
            {adminNotifHistory.length === 0 ? (
              <div className="drawer-empty-state">
                <p>No new live queue changes have occurred inside this current execution panel frame.</p>
              </div>
            ) : (
              adminNotifHistory.map(item => (
                <div key={item.id} className={`drawer-notif-card ${item.type === 'CANCELLATION' ? 'notif-card-cancel' : 'notif-card-book'}`}>
                  <div className="drawer-card-meta-row">
                    <span className="notif-time">{item.timestamp}</span>
                    {item.type === 'CANCELLATION' ? <XCircle size={14} color="#ff5252" /> : <CheckCircle size={14} color="#D4AF37" />}
                  </div>
                  <h5>{item.title}</h5>
                  <p>{item.message}</p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      <aside className="admin-sidebar">
        <div className="admin-brand">
          <div className="admin-logo-box"><Scissors size={18} /></div>
          <span className="admin-brand-text">Patupi Admin</span>
        </div>
        
        <nav className="admin-nav">
          <div className="admin-nav-item active" onClick={() => navigate('/admin')} style={{ cursor: 'pointer' }}>
            <ClipboardList size={16} style={{marginRight: '10px'}} /> Dashboard
          </div>
          <div className="admin-nav-item" onClick={() => navigate('/barbers')} style={{ cursor: 'pointer' }}>
            <Users size={16} style={{marginRight: '10px'}} /> Barbers
          </div>
          <div className="admin-nav-item" onClick={() => navigate('/admin-customers')} style={{ cursor: 'pointer' }}>
            <Users size={16} style={{marginRight: '10px'}} /> Customers
          </div>
          <div className="admin-nav-item" onClick={() => navigate('/admin-history')} style={{ cursor: 'pointer' }}>
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
            
            <div 
              className={`admin-notif-bell ${adminNotifHistory.length > 0 ? 'unread-dots' : ''} ${toastNotification ? 'pulse-bell' : ''}`}
              onClick={() => setIsNotifDrawerOpen(true)}
              style={{ cursor: 'pointer' }}
            >
              <Bell size={20} />
              {adminNotifHistory.length > 0 && <span className="bell-dot"></span>}
            </div>

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
                  appointments.map((item) => {
                    const cleanStatus = item.status?.toUpperCase() || '';
                    // FIXED: Mapped fallback row key value to support backend layout maps 
                    const uniqueRowId = item.id || item.appointmentId;
                    
                    return (
                      <tr key={uniqueRowId}>
                        <td><span className="cust-name">{item.customerName || "Guest"}</span></td>
                        <td>
                          {cleanStatus === 'CONFIRMED' || cleanStatus === 'IN_PROGRESS' || cleanStatus === 'IN PROGRESS' ? (
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
                        <td>
                          <span className={`status-badge ${item.status?.toLowerCase().replace('_', '-')}`}>
                            {item.status}
                          </span>
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: '8px' }}>
                            {cleanStatus === 'PENDING' && (
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
                            <button 
                              className="btn-delete-admin" 
                              onClick={() => setDeleteModal({ isOpen: true, targetId: uniqueRowId })}
                            >
                              <Trash2 size={14} />
                            </button>
                          </div>
                        </td>
                      </tr>
                    );
                  })
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
                    {getAvailableBarbersOptions().map(b => (
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

      {/* DELETE MODAL */}
      {deleteModal.isOpen && (
        <div className="modern-modal-overlay">
          <div className="modern-modal-container">
            <div className="modal-icon-container glow-warning">
              <AlertCircle color="#EF4444" size={32} />
            </div>
            <h3>Delete Appointment?</h3>
            <p>This action is permanent and cannot be undone.</p>
            <div className="modal-action-row">
              <button 
                className="btn-modal-outline" 
                onClick={() => !isDeleting && setDeleteModal({ isOpen: false, targetId: null })}
                disabled={isDeleting}
              >
                Cancel
              </button>
              <button 
                className="btn-modal-premium" 
                style={{ background: "#EF4444" }}
                onClick={handleDelete}
                disabled={isDeleting}
              >
                {isDeleting ? "Deleting..." : "Delete"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
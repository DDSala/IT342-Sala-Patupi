import React, { useEffect, useState, useCallback, useRef } from 'react';
import "./dashboard.css";
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';
import { 
  Calendar, 
  Clock, 
  Ticket, 
  Bell, 
  Sun, 
  Cloud, 
  CloudRain,
  Scissors, 
  Lock, 
  ChevronRight, 
  XCircle,
  AlertTriangle,
  User,
  X
} from 'lucide-react'; 

import BookingWizard from "./BookingWizard";
import AppointmentDetailsModal from "./AppointmentDetailsModal";

const Dashboard = () => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isBookingOpen, setIsBookingOpen] = useState(false);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [isNotificationDrawerOpen, setIsNotificationDrawerOpen] = useState(false);
  const [appointments, setAppointments] = useState([]);
  const [weather, setWeather] = useState({ temp: '--', code: 0 });
  
  // --- Modals, Notifications, & Cancellation Handling States ---
  const [notification, setNotification] = useState(null);
  const [notificationHistory, setNotificationHistory] = useState([]);
  const [cancelModal, setCancelModal] = useState({ isOpen: false, targetId: null });
  const [isCancelling, setIsCancelling] = useState(false);
  const [isLogoutModalOpen, setIsLogoutModalOpen] = useState(false);

  const navigate = useNavigate();
  const previousAppointmentsRef = useRef([]);

  // --- Weather Logic (Cebu City: 10.31, 123.88) ---
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
      console.error("Weather fetch failed:", err);
      setWeather({ temp: 28, code: 0 }); 
    }
  };

  const getWeatherIcon = () => {
    const code = weather.code;
    if (code === 0) return <Sun size={16} className="mr-1 text-yellow-500" />;
    if (code >= 1 && code <= 3) return <Cloud size={16} className="mr-1 text-gray-400" />;
    if (code >= 51) return <CloudRain size={16} className="mr-1 text-blue-400" />;
    return <Sun size={16} className="mr-1" />;
  };

  // --- Data Fetching & Notification Evaluation Loop ---
  const fetchUserData = useCallback(async (userId, isAutoPoll = false) => {
    try {
      const res = await axios.get(`http://localhost:8080/api/appointments/customer/${userId}`);
      const sorted = res.data.sort((a, b) => new Date(b.scheduledAt) - new Date(a.scheduledAt));
      
      if (isAutoPoll && previousAppointmentsRef.current.length > 0) {
        const oldPending = previousAppointmentsRef.current.find(app => app.status === 'PENDING');
        const oldPendingId = oldPending ? (oldPending.id || oldPending.appointmentId) : null;
        
        const currentMatch = sorted.find(app => (app.id || app.appointmentId) === oldPendingId);
        
        if (oldPending && currentMatch && currentMatch.status === 'CONFIRMED') {
          const newNotif = {
            id: Date.now(),
            title: "Barber Assigned! 🎉",
            message: `A barber has been assigned to your Appointment.`,
            timestamp: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
          };
          
          setNotification(newNotif);
          setNotificationHistory(prev => [newNotif, ...prev]);
          
          setTimeout(() => setNotification(null), 6000);
        }
      }

      setAppointments(sorted);
      previousAppointmentsRef.current = sorted;
    } catch (err) {
      console.error("Could not fetch appointments:", err);
    }
  }, []);

  // --- Initialization & Background Engine Context ---
  useEffect(() => {
    const loggedInUser = sessionStorage.getItem('user');
    
    if (loggedInUser) {
      const userData = JSON.parse(loggedInUser);
      setUser(userData);
      setLoading(false);
      
      const targetUid = userData.userId || userData.id;
      
      fetchUserData(targetUid, false);
      fetchWeather();

      const pollTimer = setInterval(() => {
        fetchUserData(targetUid, true);
      }, 5000);

      window.history.pushState(null, null, window.location.pathname);
      const handleBackButton = () => {
        window.history.pushState(null, null, window.location.pathname);
        setIsLogoutModalOpen(true); 
      };

      window.addEventListener('popstate', handleBackButton);
      return () => {
        window.removeEventListener('popstate', handleBackButton);
        clearInterval(pollTimer);
      };
    } else {
      navigate('/login');
    }
  }, [navigate, fetchUserData]);

  const openCancelConfirmation = (appointmentId) => {
    setCancelModal({ isOpen: true, targetId: appointmentId });
  };

  const executeCancellation = async () => {
    const appointmentId = cancelModal.targetId;
    if (!appointmentId) return;
    
    setIsCancelling(true);
    try {
      const userId = user.userId || user.id;
      await axios.put(
        `http://localhost:8080/api/appointments/${appointmentId}/cancel?customerId=${userId}`,
        {} 
      );
      setCancelModal({ isOpen: false, targetId: null });
      setIsCancelling(false);
      await fetchUserData(userId, false); 
    } catch (err) {
      console.error("Cancellation failed:", err);
      setIsCancelling(false);
    }
  };

  if (!user || loading) return <div className="loading-screen">Loading Patupi...</div>;

  const activeTicket = appointments.find(app => {
    const statusClean = app.status?.toUpperCase() || '';
    return ['CONFIRMED', 'PENDING', 'DRAFT', 'IN_PROGRESS', 'IN PROGRESS'].includes(statusClean);
  });
  
  // Show all completed or canceled historical records in the log table
  const history = appointments.filter(app => ['COMPLETED', 'CANCELLED'].includes(app.status?.toUpperCase()));

  return (
    <div className="dashboard-wrapper">
      
      {/* Dynamic Pop-up Dropdown Toast Banner */}
      {notification && (
        <div className="realtime-notification-toast" onClick={() => setIsNotificationDrawerOpen(true)}>
          <div className="toast-accent"></div>
          <div className="toast-content-body">
            <h5>{notification.title}</h5>
            <p>{notification.message}</p>
          </div>
          <button className="toast-close-btn" onClick={(e) => { e.stopPropagation(); setNotification(null); }}>×</button>
        </div>
      )}

      {/* Historical Notification Panel Drawer Menu */}
      <div className={`notification-drawer-overlay ${isNotificationDrawerOpen ? 'drawer-visible' : ''}`} onClick={() => setIsNotificationDrawerOpen(false)}>
        <div className="notification-drawer" onClick={(e) => e.stopPropagation()}>
          <div className="drawer-header">
            <div className="header-title-flex">
              <Bell size={18} className="gold-text" />
              <h4>Notification History</h4>
            </div>
            <button className="drawer-close-icon" onClick={() => setIsNotificationDrawerOpen(false)}><X size={20}/></button>
          </div>
          <div className="drawer-scroll-content">
            {notificationHistory.length === 0 ? (
              <div className="drawer-empty-state">
                <p>No historical updates found yet. You're completely up to date.</p>
              </div>
            ) : (
              notificationHistory.map(item => (
                <div key={item.id} className="drawer-notif-card">
                  <span className="notif-time">{item.timestamp}</span>
                  <h5>{item.title}</h5>
                  <p>{item.message}</p>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Sidebar Navigation */}
      <aside className="sidebar">
        <div className="brand-header">
          <div className="brand-icon-box"><Scissors size={20} color="#121212"/></div>
          <div className="brand-text-stack">
            <span className="brand-name">Patupi</span>
            <span className="brand-sub">DASHBOARD</span>
          </div>
        </div>
        <nav className="sidebar-nav">
          <Link to="/dashboard" className="nav-item active">Home</Link>
          <Link to="/profile" className="nav-item">Profile</Link>
        </nav>
        
        <div className="sidebar-footer-actions">
          {!activeTicket ? (
            <button className="book-now-sidebar" onClick={() => setIsBookingOpen(true)}>
              + Book Now
            </button>
          ) : (
            <div className="sidebar-status-box">
              <Lock size={14} color="#D4AF37" />
              <span>Booking Locked</span>
            </div>
          )}
        </div>
      </aside>

      {/* Main Panel Content Area */}
      <main className="main-content">
        <header className="content-header">
          <div className="header-right">
            <span className="weather">
              {getWeatherIcon()} {weather.temp}°C
            </span>
            <span 
              className={`notifications ${notificationHistory.length > 0 ? 'unread-dots' : ''} ${notification ? 'pulse-bell' : ''}`}
              onClick={() => setIsNotificationDrawerOpen(true)}
            >
              <Bell size={18}/>
            </span>
            <div 
              className="user-profile-nav-btn" 
              onClick={() => navigate('/profile')}
              role="button"
              tabIndex={0}
              aria-label="View Profile"
            >
              <span className="user-name">
                {user.fullName ? user.fullName.split(' ') : 'Guest'}
              </span>
              <div className="user-avatar-circle">
                {user.fullName ? user.fullName.charAt(0).toUpperCase() : <User size={14}/>}
              </div>
            </div>
          </div>
        </header>

        {/* Active Appointment Section */}
        <section className="dashboard-section">
          <h3 className="section-label">
            <Ticket size={18} className="inline mr-2"/> Active Ticket
          </h3>
          
          {activeTicket ? (
            <div className="premium-ticket-card">
              <div className="ticket-accent-bar"></div>
              <div className="ticket-main-content">
                <div className="ticket-status-row">
                  <span className={`status-badge ${activeTicket.status.toLowerCase().replace('_', '-')}`}>
                    {activeTicket.status}
                  </span>
                  <Scissors size={16} className="muted-icon" />
                </div>

                <h4 className="ticket-title">
                  {activeTicket.serviceName || activeTicket.description || "Premium Grooming Session"}
                </h4>

                <div className="ticket-meta-grid">
                  <div className="meta-item">
                    <Calendar size={14} className="meta-icon" />
                    <span>{new Date(activeTicket.scheduledAt).toLocaleDateString()}</span>
                  </div>
                  <div className="meta-item">
                    <Clock size={14} className="meta-icon" />
                    <span>{new Date(activeTicket.scheduledAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</span>
                  </div>
                </div>
              </div>

              <div className="ticket-action-area">
                <button className="view-details-btn" onClick={() => setIsDetailsOpen(true)}>
                  View Details <ChevronRight size={14} />
                </button>
                {['PENDING', 'DRAFT', 'CONFIRMED'].includes(activeTicket.status?.toUpperCase()) && (
                  <button 
                    className="cancel-ticket-btn" 
                    onClick={() => openCancelConfirmation(activeTicket.id || activeTicket.appointmentId)}
                  >
                    <XCircle size={14} /> Cancel
                  </button>
                )}
              </div>
            </div>
          ) : (
            <div className="ticket-card-empty glass-panel">
              <div className="empty-message-container">
                <p>No active sessions found. Ready for a new look?</p>
                <button className="text-link-gold" onClick={() => setIsBookingOpen(true)}>
                  Book your appointment now
                </button>
              </div>
            </div>
          )}
        </section>

        {/* Full-Width Recent Activity Section (Redundant Active Table Completely Removed) */}
        <div className="dashboard-grid single-column-flow">
          <div className="grid-card glass-panel full-width-card">
            <div className="card-header-flex"><h4>Recent Activity Logs</h4></div>
            <div className="table-wrapper">
              <table className="data-table-minimal">
                <thead>
                  <tr><th>DATE</th><th>TIME</th><th>SERVICE</th><th>STATUS</th></tr>
                </thead>
                <tbody>
                  {history.length === 0 ? (
                    <tr>
                      <td colSpan="4" style={{ textAlign: 'center', padding: '2rem', color: '#888' }}>
                        No historical apppointments found on record.
                      </td>
                    </tr>
                  ) : (
                    history.slice(0, 5).map((app) => (
                      <tr key={app.id || app.appointmentId}>
                        <td>{new Date(app.scheduledAt).toLocaleDateString()}</td>
                        <td>{new Date(app.scheduledAt).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}</td>
                        <td className="desc-cell">{app.serviceName || app.description || "Service"}</td>
                        <td className={`status-cell-dim ${app.status.toLowerCase().replace('_', '-')}`}>{app.status}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <footer className="dashboard-footer-simple">
          <p>© 2026 Patupi Premium Barbering. Stay Sharp.</p>
        </footer>
      </main>

      {/* --- Premium Custom Cancellation Dialog Overlay --- */}
      {cancelModal.isOpen && (
        <div className="premium-modal-overlay">
          <div className="premium-modal-box">
            <div className="modal-warning-icon">
              <AlertTriangle size={32} color="#D4AF37" />
            </div>
            <h3>Cancel Appointment?</h3>
            <p>This action cannot be undone. Your reserved time slot will be released back to the queue immediately.</p>
            <div className="modal-actions-wrapper">
              <button 
                className="modal-btn-dismiss" 
                onClick={() => !isCancelling && setCancelModal({ isOpen: false, targetId: null })}
                disabled={isCancelling}
              >
                Keep Booking
              </button>
              <button 
                className={`modal-btn-confirm-cancel ${isCancelling ? 'loading-active' : ''}`} 
                onClick={executeCancellation}
                disabled={isCancelling}
              >
                {isCancelling ? (
                  <span className="premium-loader-row">
                    <span className="spinner-dot"></span>
                    Cancelling...
                  </span>
                ) : (
                  "Confirm Cancellation"
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Back button pop blocker interceptor modal state */}
      {isLogoutModalOpen && (
        <div className="premium-modal-overlay">
          <div className="premium-modal-box">
            <div className="modal-warning-icon">
              <AlertTriangle size={32} color="#EF4444" />
            </div>
            <h3>Exit Session?</h3>
            <p>Are you sure you want to log out of your Patupi user dashboard session?</p>
            <div className="modal-actions-wrapper">
              <button className="modal-btn-dismiss" onClick={() => setIsLogoutModalOpen(false)}>Stay Connected</button>
              <button className="modal-btn-confirm-cancel" style={{ background: '#EF4444' }} onClick={() => { sessionStorage.clear(); navigate('/login'); }}>Sign Out</button>
            </div>
          </div>
        </div>
      )}

      {/* Booking Wizard Injector Context */}
      <BookingWizard 
        isOpen={isBookingOpen} 
        onClose={() => {
          setIsBookingOpen(false);
          fetchUserData(user.userId || user.id, false);
        }} 
        customerId={user.userId || user.id}
      />

      {/* Modal Inspector Context Card Details */}
      <AppointmentDetailsModal 
        isOpen={isDetailsOpen} 
        onClose={() => setIsDetailsOpen(false)} 
        appointment={activeTicket} 
      />
    </div>
  );
};

export default Dashboard;
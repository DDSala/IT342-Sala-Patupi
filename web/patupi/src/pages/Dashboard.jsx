import React, { useEffect, useState, useCallback } from 'react';
import '../css/dashboard.css';
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
  XCircle 
} from 'lucide-react'; 
import BookingWizard from '../components/BookingSystem/BookingWizard';
import AppointmentDetailsModal from '../components/BookingSystem/AppointmentDetailsModal';

const Dashboard = () => {
  // --- States ---
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [isBookingOpen, setIsBookingOpen] = useState(false);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [appointments, setAppointments] = useState([]);
  const [weather, setWeather] = useState({ temp: '--', code: 0 });

  const navigate = useNavigate();

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
      setWeather({ temp: 28, code: 0 }); // Fallback
    }
  };

  const getWeatherIcon = () => {
    const code = weather.code;
    if (code === 0) return <Sun size={16} className="mr-1 text-yellow-500" />;
    if (code >= 1 && code <= 3) return <Cloud size={16} className="mr-1 text-gray-400" />;
    if (code >= 51) return <CloudRain size={16} className="mr-1 text-blue-400" />;
    return <Sun size={16} className="mr-1" />;
  };

  // --- Data Fetching ---
  const fetchUserData = useCallback(async (userId) => {
    try {
      const res = await axios.get(`http://localhost:8080/api/appointments/customer/${userId}`);
      const sorted = res.data.sort((a, b) => new Date(b.scheduledAt) - new Date(a.scheduledAt));
      setAppointments(sorted);
    } catch (err) {
      console.error("Could not fetch appointments:", err);
    }
  }, []);

  useEffect(() => {
    const loggedInUser = sessionStorage.getItem('user');
    
    if (loggedInUser) {
      const userData = JSON.parse(loggedInUser);
      setUser(userData);
      setLoading(false);
      
      // Fetch initial data
      fetchUserData(userData.userId || userData.id);
      fetchWeather();

      // Handle Logout/Back Navigation
      window.history.pushState(null, null, window.location.pathname);
      const handleBackButton = () => {
        window.history.pushState(null, null, window.location.pathname);
        if (window.confirm("Do you want to logout?")) {
          sessionStorage.removeItem('user');
          navigate('/login', { replace: true });
        }
      };

      window.addEventListener('popstate', handleBackButton);
      return () => window.removeEventListener('popstate', handleBackButton);
    } else {
      navigate('/login');
    }
  }, [navigate, fetchUserData]);

  // --- Handlers ---
  const handleCancel = async (appointmentId) => {
    if (!appointmentId) return;
    if (window.confirm("Are you sure you want to cancel this appointment?")) {
      try {
        const userId = user.userId || user.id;
        await axios.put(
          `http://localhost:8080/api/appointments/${appointmentId}/cancel?customerId=${userId}`,
          {} 
        );
        await fetchUserData(userId); 
      } catch (err) {
        console.error("Cancellation failed:", err);
      }
    }
  };

  if (!user || loading) return <div className="loading-screen">Loading Patupi...</div>;

  // --- View Logic ---
  const activeTicket = appointments.find(app => 
    ['CONFIRMED', 'PENDING', 'DRAFT'].includes(app.status)
  );
  
  const history = appointments.filter(app => ['COMPLETED', 'CANCELLED'].includes(app.status));
  const upcoming = appointments.filter(app => !['COMPLETED', 'CANCELLED'].includes(app.status));

  return (
    <div className="dashboard-wrapper">
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
      </aside>

      {/* Main Panel */}
      <main className="main-content">
        <header className="content-header">
          <div className="header-right">
            {/* Real-time Weather Display */}
            <span className="weather">
              {getWeatherIcon()} {weather.temp}°C
            </span>
            <span className="notifications"><Bell size={18}/></span>
            <div className="user-profile-nav" onClick={() => navigate('/profile')}>
              <span className="user-name">
                {user.fullName ? user.fullName.split(' ') : 'Guest'}
              </span>
              <div className="user-avatar-circle">
                {user.fullName ? user.fullName.charAt(0) : 'U'}
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
                  <span className={`status-badge ${activeTicket.status.toLowerCase()}`}>
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
                <button 
                  className="cancel-ticket-btn" 
                  onClick={() => handleCancel(activeTicket.appointmentId)}
                >
                  <XCircle size={14} /> Cancel
                </button>
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

        {/* Upcoming & History Tables */}
        <div className="dashboard-grid">
          <div className="grid-card glass-panel">
            <div className="card-header-flex"><h4>Upcoming</h4></div>
            <div className="table-wrapper">
              <table className="data-table-minimal">
                <thead>
                  <tr><th>DATE</th><th>TIME</th><th>DESCRIPTION</th><th>STATUS</th></tr>
                </thead>
                <tbody>
                  {upcoming.slice(0, 3).map((app) => (
                    <tr key={app.appointmentId}>
                      <td>{new Date(app.scheduledAt).toLocaleDateString()}</td>
                      <td>{new Date(app.scheduledAt).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}</td>
                      <td className="desc-cell">{app.serviceName || app.description || "Grooming"}</td>
                      <td className="gold-text">{app.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="grid-card glass-panel">
            <div className="card-header-flex"><h4>History</h4></div>
            <div className="table-wrapper">
              <table className="data-table-minimal">
                <thead>
                  <tr><th>DATE</th><th>TIME</th><th>DESCRIPTION</th><th>RATING</th><th>STATUS</th></tr>
                </thead>
                <tbody>
                  {history.slice(0, 3).map((app) => (
                    <tr key={app.appointmentId}>
                      <td>{new Date(app.scheduledAt).toLocaleDateString()}</td>
                      <td>{new Date(app.scheduledAt).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}</td>
                      <td className="desc-cell">{app.serviceName || app.description || "Service"}</td>
                      <td>
                        {app.status === 'COMPLETED' ? (
                          <span className="rating-badge">Check App</span>
                        ) : (
                          <span className="muted-dash">—</span>
                        )}
                      </td>
                      <td className={`status-cell-dim ${app.status.toLowerCase()}`}>{app.status}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <footer className="dashboard-footer-simple">
          <p>© 2026 Patupi Premium Barbering. Stay Sharp.</p>
        </footer>
      </main>

      {/* Modals */}
      <BookingWizard 
        isOpen={isBookingOpen} 
        onClose={() => {
          setIsBookingOpen(false);
          fetchUserData(user.userId || user.id);
        }} 
        customerId={user.userId || user.id}
      />

      <AppointmentDetailsModal 
        isOpen={isDetailsOpen} 
        onClose={() => setIsDetailsOpen(false)} 
        appointment={activeTicket} 
      />
    </div>
  );
};

export default Dashboard;
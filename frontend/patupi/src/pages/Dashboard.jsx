import React from 'react';
import '../css/dashboard.css';

const Dashboard = () => {
  return (
    <div className="dashboard-wrapper">
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="brand-logo">✂</div>
          <div className="brand-text">
            <span className="brand-name">Patupi</span>
            <span className="brand-sub">DASHBOARD</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          <a href="/dashboard" className="nav-item active">
            <span className="icon">⊞</span> Dashboard
          </a>
          <a href="/appointments" className="nav-item">
            <span className="icon">📅</span> Appointments
          </a>
        </nav>

        <button className="book-now-btn">+ Book Now</button>
      </aside>

      <main className="main-content">
        <header className="content-header">
          <div className="header-stats">
            <span className="weather">☀️ 28°C</span>
            <span className="notifications">🔔</span>
          </div>
          <div className="user-profile">
            <span className="user-name">Wally</span>
            <div className="user-avatar">W</div>
          </div>
        </header>

        <section className="active-ticket-section">
          <h3>🎫 Active Ticket</h3>
          <div className="ticket-card">
            <div className="ticket-image">
              <img src="https://images.unsplash.com/photo-1585747860715-2ba37e788b70?q=80&w=2074&auto=format&fit=crop" alt="Barber Shop" />
            </div>
            <div className="ticket-info">
              <div className="status-badge">● Status: Assigned</div>
              <h2>Full Service Grooming</h2>
              <p className="barber-assigned">Barber: <span className="gold-text">Leo the Legend</span></p>
              
              <div className="ticket-footer">
                <div className="location-info">
                  <span>📅 Today, Oct 24</span>
                  <span>📍 Downtown Hub</span>
                </div>
                <button className="cancel-btn">⊗ Cancel Appointment</button>
              </div>
            </div>
            <div className="estimated-start">
              <span className="label">ESTIMATED START</span>
              <span className="time">14:30</span>
            </div>
          </div>
        </section>


        <div className="dashboard-grid">

          <div className="grid-card">
            <div className="card-header">
              <h4>📅 Upcoming Appointments</h4>
              <button className="view-all">View All</button>
            </div>
            <table className="data-table">
              <thead>
                <tr>
                  <th>DATE</th>
                  <th>TIME</th>
                  <th>BARBER</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Oct 24, 2023</td>
                  <td>02:30 PM</td>
                  <td><div className="table-user"><span className="avatar-sm">LL</span> Leo Legend</div></td>
                </tr>
              </tbody>
            </table>
          </div>


          <div className="grid-card">
            <div className="card-header">
              <h4>📋 Service History</h4>
              <button className="view-all">Download PDF</button>
            </div>
            <table className="data-table">
              <thead>
                <tr>
                  <th>DATE</th>
                  <th>STYLE</th>
                  <th>RATING</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td>Oct 10, 2023</td>
                  <td>Skin Fade</td>
                  <td className="gold-text">★★★★★</td>
                </tr>
                <tr>
                  <td>Sep 24, 2023</td>
                  <td>Classic Taper</td>
                  <td className="gold-text">★★★★★</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <footer className="dashboard-footer">
          <p>© 2023 Patupi Premium Barbering. All rights reserved.</p>
          <div className="footer-links">
            <a href="#">PRIVACY POLICY</a>
            <a href="#">TERMS OF SERVICE</a>
            <a href="#">SUPPORT</a>
          </div>
        </footer>
      </main>
    </div>
  );
};

export default Dashboard;
import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { Scissors, LogOut, Users, ClipboardList, History, Search, AlertCircle } from 'lucide-react';
import '../css/admin-history.css';

const AdminHistory = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [history, setHistory] = useState([]);
    const [searchTerm, setSearchTerm] = useState('');
    const [showLogoutModal, setShowLogoutModal] = useState(false);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchHistory();
    }, []);

    const fetchHistory = async () => {
        try {
            const res = await axios.get('http://localhost:8080/api/appointments/all');
            setHistory(res.data);
            setLoading(false);
        } catch (err) { 
            console.error("History Fetch Error:", err); 
            setLoading(false);
        }
    };

    // FILTER & SORT LOGIC
    const filtered = history
        .filter(h => {
            // 1. Only show COMPLETED or CANCELLED
            const isHistoryStatus = h.status === 'COMPLETED' || h.status === 'CANCELLED';
            
            // 2. Search filter
            const matchesSearch = 
                h.customerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                h.barberName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                h.service?.toLowerCase().includes(searchTerm.toLowerCase());

            return isHistoryStatus && matchesSearch;
        })
        // 3. SORT: Newest appointments at the top (Descending)
        .sort((a, b) => new Date(b.scheduledAt) - new Date(a.scheduledAt));

    return (
        <div className="admin-container">
            <aside className="admin-sidebar">
                <div className="admin-brand">
                    <div className="admin-logo-box"><Scissors size={18} /></div>
                    <span className="admin-brand-text">Patupi Admin</span>
                </div>
                <nav className="admin-nav">
                    <div 
                        className={`admin-nav-item ${location.pathname === '/admin' ? 'active' : ''}`} 
                        onClick={() => navigate('/admin')} 
                        style={{ cursor: 'pointer' }}
                    >
                        <ClipboardList size={16} style={{marginRight: '10px'}} /> Dashboard
                    </div>
                    
                    <div 
                        className={`admin-nav-item ${location.pathname === '/barbers' ? 'active' : ''}`} 
                        onClick={() => navigate('/barbers')} 
                        style={{ cursor: 'pointer' }}
                    >
                        <Users size={16} style={{marginRight: '10px'}} /> Barbers
                    </div>

                    <div 
                        className={`admin-nav-item ${location.pathname === '/admin-customers' ? 'active' : ''}`} 
                        onClick={() => navigate('/admin-customers')} 
                        style={{ cursor: 'pointer' }}
                    >
                        <Users size={16} style={{marginRight: '10px'}} /> Customers
                    </div>
                    
                    <div 
                        className={`admin-nav-item ${location.pathname === '/admin-history' ? 'active' : ''}`} 
                        onClick={() => navigate('/admin-history')} 
                        style={{ cursor: 'pointer' }}
                    >
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
                        <h2>Appointment History</h2>
                    </div>
                    <div className="history-search-wrapper">
                        <Search size={16} className="search-icon-inside" />
                        <input 
                            type="text" 
                            placeholder="Search history..." 
                            className="history-search-input"
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                        />
                    </div>
                </header>

                <div className="admin-section-card">
                    <table className="queue-table">
                        <thead>
                            <tr>
                                <th>CUSTOMER</th>
                                <th>BARBER</th>
                                <th>SERVICE</th>
                                <th>PAID AMOUNT</th>
                                <th>RATING</th>
                                <th>STATUS</th>
                                <th>DATE & TIME</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <tr><td colSpan="7" className="empty-row">Loading archive...</td></tr>
                            ) : filtered.length > 0 ? (
                                filtered.map((h) => (
                                    <tr key={h.appointmentId}>
                                        <td><span className="cust-name">{h.customerName}</span></td>
                                        <td>{h.barberName || "—"}</td>
                                        <td className="service-gold">{h.service}</td>
                                        <td>₱{parseFloat(h.totalAmount || 0).toFixed(2)}</td>
                                        <td>{h.rating ? `${h.rating} ★` : "—"}</td>
                                        <td>
                                            <span className={`status-badge ${h.status?.toLowerCase()}`}>
                                                {h.status}
                                            </span>
                                        </td>
                                        {/* REWRITTEN DATE COLUMN */}
                                        <td>
                                            <div className="time-stack">
                                                <div className="date-sub">
                                                    {new Date(h.scheduledAt).toLocaleDateString('en-US', { 
                                                        month: 'short', 
                                                        day: 'numeric', 
                                                        year: 'numeric' 
                                                    })}
                                                </div>
                                                <div className="time-highlight">
                                                    {new Date(h.scheduledAt).toLocaleTimeString([], { 
                                                        hour: '2-digit', 
                                                        minute: '2-digit' 
                                                    })}
                                                </div>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="7" className="empty-row">No history records found.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </main>

            {showLogoutModal && (
                <div className="modern-modal-overlay">
                    <div className="modern-modal-container">
                        <div className="modal-icon-container glow-warning">
                            <AlertCircle className="icon-warning" color="#ff9800" size={32} />
                        </div>
                        <h3>Confirm Logout</h3>
                        <p>Are you sure you want to exit the management portal?</p>
                        <div className="modal-action-row">
                            <button className="btn-modal-outline" onClick={() => setShowLogoutModal(false)}>
                                Nevermind
                            </button>
                            <button className="btn-modal-premium" onClick={() => { sessionStorage.clear(); navigate('/login'); }}>
                                Sign Out
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminHistory;
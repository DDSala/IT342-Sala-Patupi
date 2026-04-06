import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { Scissors, LogOut, Users, ClipboardList, History, Edit2, Ban, AlertCircle, Trash2 } from 'lucide-react';
import '../css/admin-customer.css';

const AdminCustomer = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [customers, setCustomers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showLogoutModal, setShowLogoutModal] = useState(false);

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
        fetchCustomers();
    }, [navigate]);

    const fetchCustomers = async () => {
        try {
            const res = await axios.get('http://localhost:8080/api/users/all');
            
            setCustomers(res.data.filter(user => Number(user.roleId) === 3));
            setLoading(false);
        } catch (err) {
            console.error("Fetch error:", err);
            setLoading(false);
        }
    };

    
    const handleDelete = async (id) => {
        if (window.confirm("Are you sure you want to permanently delete this customer account?")) {
            try {
                await axios.delete(`http://localhost:8080/api/users/${id}`);
                
                fetchCustomers();
            } catch (err) {
                console.error("Delete failed:", err);
                alert("Failed to delete customer. They may have active appointments.");
            }
        }
    };

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
                        <h2 className="directory-title">Customer Directory</h2>
                    </div>
                </header>

                <section className="admin-section-card">
                    <table className="queue-table">
                        <thead>
                            <tr>
                                <th>CUSTOMER NAME</th>
                                <th>EMAIL ADDRESS</th>
                                <th>STATUS</th>
                                <th>ACTIONS</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <tr><td colSpan="4" className="empty-row">Loading...</td></tr>
                            ) : customers.length > 0 ? (
                                customers.map((c) => (
                                    <tr key={c.userId}>
                                        <td><span className="cust-name">{c.fullName}</span></td>
                                        <td className="time-highlight">{c.email}</td>
                                        <td>
                                            <div className="dot-status confirmed">
                                                <span className="status-dot-green"></span> Active
                                            </div>
                                        </td>
                                        <td>
                                            <div className="action-buttons-flex">
                                                <Edit2 size={16} className="icon-btn-gray" title="Edit Customer" />
                                                {/* TRASH ICON WITH DELETE LOGIC */}
                                                <Trash2 
                                                    size={16} 
                                                    className="icon-btn-red" 
                                                    style={{ cursor: 'pointer' }}
                                                    onClick={() => handleDelete(c.userId)}
                                                    title="Delete Customer"
                                                />
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr><td colSpan="4" className="empty-row">No customers found.</td></tr>
                            )}
                        </tbody>
                    </table>
                </section>
            </main>

            {/* SIGN OUT MODAL */}
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

export default AdminCustomer;
import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import axios from 'axios';
import { Scissors, LogOut, Users, ClipboardList, History, AlertCircle, Trash2 } from 'lucide-react';
import "./admin-customer.css";

const AdminCustomer = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [customers, setCustomers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [showLogoutModal, setShowLogoutModal] = useState(false);

    // Premium overlay state match tracking properties
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
        fetchCustomers();

        // Background poller task runner matching your barbers management frequency
        const pollerInterval = setInterval(() => {
            fetchCustomers();
        }, 4000);

        return () => clearInterval(pollerInterval);
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

    const handleDeleteConfirm = async () => {
        const { targetId } = deleteModal;
        setIsDeleting(true);
        try {
            await axios.delete(`http://localhost:8080/api/users/${targetId}`);
            
            // Instantly drop row entity out of live view collection tracking properties
            setCustomers(prev => prev.filter(c => c.userId !== targetId));
            setDeleteModal({ isOpen: false, targetId: null });
        } catch (err) {
            console.error("Delete failed:", err);
            alert("Failed to delete customer. They may have active appointments.");
        } finally {
            setIsDeleting(false);
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
                                <th>ACTIONS</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? (
                                <tr><td colSpan="3" className="empty-row">Loading...</td></tr>
                            ) : customers.length > 0 ? (
                                customers.map((c) => {
                                    return (
                                        <tr key={c.userId}>
                                            <td><span className="cust-name">{c.fullName}</span></td>
                                            <td>{c.email}</td>
                                            <td>
                                                <div className="action-buttons-flex" style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                                                    <button 
                                                        className="btn-delete-admin"
                                                        onClick={() => setDeleteModal({ isOpen: true, targetId: c.userId })}
                                                        title="Delete Customer"
                                                    >
                                                        <Trash2 size={16} />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })
                            ) : (
                                <tr><td colSpan="3" className="empty-row">No customers found.</td></tr>
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

            {/* PREMIUM DELETE CONFIRMATION MODAL */}
            {deleteModal.isOpen && (
                <div className="modern-modal-overlay">
                    <div className="modern-modal-container">
                        <div className="modal-icon-container glow-warning">
                            <AlertCircle color="#EF4444" size={32} />
                        </div>
                        <h3>Remove Customer Account?</h3>
                        <p>This action is permanent and will remove them from the active record directory.</p>
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
                                onClick={handleDeleteConfirm}
                                disabled={isDeleting}
                            >
                                {isDeleting ? "Removing..." : "Remove"}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminCustomer;
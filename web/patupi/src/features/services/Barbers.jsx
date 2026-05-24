import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom'; 
import { Scissors, ClipboardList, Users, History, LogOut, Trash2, Edit2, AlertCircle } from 'lucide-react';
import './barbers.css';     

const BASE_API_URL = 'http://localhost:8080/api/users';

const Barbers = () => {
    const navigate = useNavigate();
    const location = useLocation(); 
    
    const [barbers, setBarbers] = useState([]);
    const [showModal, setShowModal] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [currentBarberId, setCurrentBarberId] = useState(null);
    const [showLogoutModal, setShowLogoutModal] = useState(false); 
    const [formData, setFormData] = useState({
        firstName: '', lastName: '', email: '', address: ''
    });

    // Premium overlay state match tracking properties
    const [deleteModal, setDeleteModal] = useState({ isOpen: false, targetId: null });
    const [isDeleting, setIsDeleting] = useState(false);

    // Unified fetch handler optimized to prevent memory state updates on unmounted trees
    const fetchBarberRoster = useCallback(async (isSilent = false) => {
        try {
            const response = await fetch(`${BASE_API_URL}/barbers`);
            if (response.ok) {
                const data = await response.json();
                setBarbers(data);
            }
        } catch (error) {
            if (!isSilent) {
                console.error("Roster retrieval operation rejected:", error);
            }
        }
    }, []);

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

        // Initial loading paint
        fetchBarberRoster(false);

        // Background poller task runner
        const pollerInterval = setInterval(() => {
            fetchBarberRoster(true);
        }, 4000);

        return () => clearInterval(pollerInterval);
    }, [navigate, fetchBarberRoster]);

    // Safe background API processing matching dashboard flow
    const handleDeleteConfirm = async () => {
        const { targetId } = deleteModal;
        setIsDeleting(true);
        try {
            const response = await fetch(`${BASE_API_URL}/${targetId}`, {
                method: 'DELETE'
            });
            if (response.ok) {
                setBarbers(prev => prev.filter(b => (b.userId || b.id) !== targetId));
                setDeleteModal({ isOpen: false, targetId: null });
            }
        } catch (error) {
            console.error("Delete operation rejected:", error);
            alert("Delete failed.");
        } finally {
            setIsDeleting(false);
        }
    };

    const handleEditClick = (barber) => {
        setIsEditing(true);
        setCurrentBarberId(barber.userId || barber.id);
        
        // FIXED BUG: Restored item index target reference lookup on string splitting array
        const nameParts = barber.fullName ? barber.fullName.trim().split(/\s+/) : [];
        const parsedFirstName = nameParts || ''; 
        const parsedLastName = nameParts.slice(1).join(' ') || '';
        
        setFormData({
            firstName: parsedFirstName,
            lastName: parsedLastName,
            email: barber.email || '',
            address: barber.address || ''
        });
        setShowModal(true);
    };

    const handleSave = async (e) => {
        e.preventDefault();
        
        const payload = {
            fullName: `${formData.firstName.trim()} ${formData.lastName.trim()}`,
            email: formData.email.trim(),
            address: formData.address.trim(),
            firstName: formData.firstName.trim(),
            lastName: formData.lastName.trim(),
            status: isEditing ? undefined : 'Unavailable'
        };

        const url = isEditing 
            ? `${BASE_API_URL}/${currentBarberId}`
            : `${BASE_API_URL}/register-barber`;
        
        try {
            const response = await fetch(url, {
                method: isEditing ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                closeModal();
                fetchBarberRoster(true);
            }
        } catch (error) {
            console.error("Profile mutation processing dropped:", error);
        }
    };

    const closeModal = () => {
        setShowModal(false);
        setIsEditing(false);
        setCurrentBarberId(null);
        setFormData({ firstName: '', lastName: '', email: '', address: '' });
    };

    const resolveBarberStatus = (barberObj) => {
        if (barberObj.barberProfile && barberObj.barberProfile.status) {
            return barberObj.barberProfile.status;
        }
        if (barberObj.status && barberObj.status.toUpperCase() !== 'ACTIVE') {
            return barberObj.status;
        }
        return "Unavailable"; 
    };

    const getStatusClassModifier = (statusText) => {
        const standard = statusText ? statusText.toLowerCase() : '';
        if (standard === 'available') return 'status-active';
        if (standard === 'busy') return 'status-busy';
        return 'status-unavailable';
    };

    return (
        <div className="admin-container">
            <aside className="admin-sidebar">
                <div className="admin-brand">
                    <div className="admin-logo-box"><Scissors size={18} /></div>
                    <span className="admin-brand-text">Patupi Admin</span>
                </div>
                <nav className="admin-nav">
                    <div className={`admin-nav-item ${location.pathname === '/admin' ? 'active' : ''}`} onClick={() => navigate('/admin')} style={{ cursor: 'pointer' }}>
                        <ClipboardList size={16} style={{marginRight: '10px'}} /> Dashboard
                    </div>
                    <div className={`admin-nav-item ${location.pathname === '/barbers' ? 'active' : ''}`} onClick={() => navigate('/barbers')} style={{ cursor: 'pointer' }}>
                        <Users size={16} style={{marginRight: '10px'}} /> Barbers
                    </div>
                    <div className={`admin-nav-item ${location.pathname === '/admin-customers' ? 'active' : ''}`} onClick={() => navigate('/admin-customers')} style={{ cursor: 'pointer' }}>
                        <Users size={16} style={{marginRight: '10px'}} /> Customers
                    </div>
                    <div className={`admin-nav-item ${location.pathname === '/admin-history' ? 'active' : ''}`} onClick={() => navigate('/admin-history')} style={{ cursor: 'pointer' }}>
                        <History size={16} style={{marginRight: '10px'}} /> History
                    </div>
                </nav>
                <button className="admin-logout" onClick={() => setShowLogoutModal(true)}>
                    <LogOut size={16} style={{marginRight: '8px'}} /> Sign Out
                </button>
            </aside>

            <main className="admin-content">
                <header className="admin-top-bar">
                    <div><h2>Barber Management</h2></div>
                    <button className="add-barber-btn-premium" onClick={() => setShowModal(true)}>+ Add New Barber</button>
                </header>

                <section className="admin-section-card">
                    <table className="queue-table">
                        <thead>
                            <tr>
                                <th>FULL NAME</th>
                                <th>EMAIL</th>
                                <th>ADDRESS</th>
                                <th>STATUS</th>
                                <th>ACTION</th>
                            </tr>
                        </thead>
                        <tbody>
                            {barbers.length > 0 ? barbers.map((b) => {
                                const currentStatus = resolveBarberStatus(b);
                                const uniqueRowId = b.userId || b.id;
                                return (
                                    <tr key={uniqueRowId}>
                                        <td><span className="cust-name">{b.fullName}</span></td>
                                        <td>{b.email}</td>
                                        <td className="text-muted">{b.address}</td>
                                        <td>
                                            <span className={`status-badge ${getStatusClassModifier(currentStatus)}`}>
                                                {currentStatus}
                                            </span>
                                        </td>
                                        <td>
                                            {/* 🌟 FIXED: Added 'alignItems: "center"' to perfectly square up the sibling heights */}
                                            <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                                                <Edit2 
                                                    size={16} 
                                                    className="action-icon-edit" 
                                                    style={{ cursor: 'pointer' }} 
                                                    onClick={() => handleEditClick(b)} 
                                                />
                                                
                                                <button 
                                                    className="btn-delete-admin"
                                                    onClick={() => setDeleteModal({ isOpen: true, targetId: uniqueRowId })}
                                                >
                                                    <Trash2 size={16} />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                );
                            }) : (
                                <tr><td colSpan="5" className="empty-row">No barbers found.</td></tr>
                            )}
                        </tbody>
                    </table>
                </section>
            </main>

            {/* REGISTER/EDIT MODAL */}
            {showModal && (
                <div className="barber-modal-overlay">
                    <div className="barber-modal-box">
                        <h2>{isEditing ? 'Update Barber' : 'Register New Barber'}</h2>
                        <form onSubmit={handleSave}>
                            <div className="input-row">
                                <div className="input-group">
                                    <label>First Name</label>
                                    <input type="text" value={formData.firstName} onChange={(e) => setFormData({...formData, firstName: e.target.value})} required />
                                </div>
                                <div className="input-group">
                                    <label>Last Name</label>
                                    <input type="text" value={formData.lastName} onChange={(e) => setFormData({...formData, lastName: e.target.value})} required />
                                </div>
                            </div>
                            <div className="input-group">
                                <label>Email</label>
                                <input type="email" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} required disabled={isEditing} />
                            </div>
                            <div className="input-group">
                                <label>Address</label>
                                <input type="text" value={formData.address} onChange={(e) => setFormData({...formData, address: e.target.value})} required />
                            </div>
                            <div className="modal-footer">
                                <button type="button" className="btn-cancel" onClick={closeModal}>Cancel</button>
                                <button type="submit" className="btn-save">{isEditing ? 'Update Changes' : 'Save Barber'}</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

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
                            <button className="btn-modal-outline" onClick={() => setShowLogoutModal(false)}>Nevermind</button>
                            <button className="btn-modal-premium" onClick={() => { sessionStorage.clear(); navigate('/login'); }}>Sign Out</button>
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
                        <h3>Remove Barber?</h3>
                        <p>This action is permanent and will remove them from the active roster.</p>
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

export default Barbers;
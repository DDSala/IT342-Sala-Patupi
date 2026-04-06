import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom'; 
import { Scissors, ClipboardList, Users, History, LogOut, Trash2, Edit2, AlertCircle } from 'lucide-react';
import '../css/barbers.css'; 

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
        fetchBarbers();
    }, [navigate]);

    const fetchBarbers = async () => {
        try {
            const response = await fetch('http://localhost:8080/api/users/barbers');
            const data = await response.json();
            setBarbers(data);
        } catch (error) {
            console.error("Error fetching:", error);
        }
    };

    const handleDelete = async (id) => {
        if (window.confirm("Remove this barber from the roster?")) {
            try {
                const response = await fetch(`http://localhost:8080/api/users/${id}`, {
                    method: 'DELETE'
                });
                if (response.ok) fetchBarbers();
            } catch (error) {
                console.error("Delete error:", error);
            }
        }
    };

    const handleEditClick = (barber) => {
        setIsEditing(true);
        setCurrentBarberId(barber.userId);
        const nameParts = barber.fullName.split(' ');
        setFormData({
            firstName: nameParts || '',
            lastName: nameParts.slice(1).join(' ') || '',
            email: barber.email,
            address: barber.address
        });
        setShowModal(true);
    };

    const handleSave = async (e) => {
        e.preventDefault();
        const payload = {
            fullName: `${formData.firstName} ${formData.lastName}`,
            email: formData.email,
            address: formData.address,
            firstName: formData.firstName,
            lastName: formData.lastName
        };

        const url = isEditing 
            ? `http://localhost:8080/api/users/${currentBarberId}`
            : 'http://localhost:8080/api/users/register-barber';
        
        try {
            const response = await fetch(url, {
                method: isEditing ? 'PUT' : 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                closeModal();
                fetchBarbers();
            }
        } catch (error) {
            console.error("Save failed:", error);
        }
    };

    const closeModal = () => {
        setShowModal(false);
        setIsEditing(false);
        setFormData({ firstName: '', lastName: '', email: '', address: '' });
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
                        <h2>Barber Management</h2>
                    </div>
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
                            {barbers.length > 0 ? barbers.map((b) => (
                                <tr key={b.userId}>
                                    <td><span className="cust-name">{b.fullName}</span></td>
                                    <td>{b.email}</td>
                                    <td className="text-muted">{b.address}</td>
                                    <td><span className="status-badge confirmed">Available</span></td>
                                    <td>
                                        <div style={{ display: 'flex', gap: '12px' }}>
                                            <Edit2 size={16} className="action-icon-edit" onClick={() => handleEditClick(b)} />
                                            <Trash2 size={16} className="action-icon-delete" onClick={() => handleDelete(b.userId)} />
                                        </div>
                                    </td>
                                </tr>
                            )) : (
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
                                <div className="input-group"><label>First Name</label><input type="text" value={formData.firstName} onChange={(e) => setFormData({...formData, firstName: e.target.value})} required /></div>
                                <div className="input-group"><label>Last Name</label><input type="text" value={formData.lastName} onChange={(e) => setFormData({...formData, lastName: e.target.value})} required /></div>
                            </div>
                            <div className="input-group"><label>Email</label><input type="email" value={formData.email} onChange={(e) => setFormData({...formData, email: e.target.value})} required /></div>
                            <div className="input-group"><label>Address</label><input type="text" value={formData.address} onChange={(e) => setFormData({...formData, address: e.target.value})} required /></div>
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

export default Barbers;
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Scissors, ArrowRight } from 'lucide-react';

const Step1Reference = ({ onNext, customerId }) => {
    const [services, setServices] = useState([]);
    const [selectedService, setSelectedService] = useState(null);
    const [description, setDescription] = useState('');
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        axios.get('http://localhost:8080/api/services')
            .then(res => {
                if (res.data) setServices(res.data);
            })
            .catch(err => console.error("Error fetching services:", err));
    }, []);

const handleNext = async () => {
    setLoading(true);

    let finalId = customerId;
    if (!finalId) {
        const sessionUser = JSON.parse(sessionStorage.getItem('user'));
        finalId = sessionUser?.userId || sessionUser?.id;
    }

    if (!finalId) {
        alert("Booking Error: User session not found. Please re-login.");
        setLoading(false);
        return;
    }

    try {
        const payload = {
            customerId: finalId,
            description: description || "No description provided",
            serviceId: selectedService?.service_id || null
        };

        const res = await axios.post('http://localhost:8080/api/appointments/step1', payload);

        if (res.data && res.data.appointmentId) {
            onNext(res.data.appointmentId, { 
                service: selectedService, 
                description: description 
            });
        }
        } catch (err) {
            console.error("Booking Error:", err.response?.data || err.message);
            alert("Failed to start booking. Check if server is running and database is connected.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="step-container">
            <h3 className="gold-text">STYLE SELECTION</h3>
            <p className="subtitle-text">Select a service from our menu or describe your custom look.</p>

            <div className="services-grid">
                {services.length > 0 ? (
                    services.map(s => (
                        <div 
                            key={s.service_id}
                            className={`service-card glass-panel ${selectedService?.service_id === s.service_id ? 'active' : ''}`}
                            onClick={() => setSelectedService(s)}
                        >
                            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', width: '100%' }}>
                                <Scissors size={16} />
                                <span className="service-price">₱{s.base_price}</span>
                            </div>
                            <p className="service-name" style={{ marginTop: '8px' }}>{s.name}</p>
                        </div>
                    ))
                ) : (
                    <p style={{ color: '#666', gridColumn: 'span 2' }}>Loading services...</p>
                )}
            </div>

            <div className="input-section" style={{ marginTop: '10px' }}>
                <label className="input-label">Additional Instructions</label>
                <textarea 
                    placeholder="E.g., High fade, keep the top long, or any specific requests..." 
                    className="glass-input-premium"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    style={{ minHeight: '100px', resize: 'none' }}
                />
            </div>

            <div className="button-group">
                <div />
                <button 
                    onClick={handleNext} 
                    className="gold-button" 
                    disabled={loading || (!selectedService && !description.trim())}
                >
                    {loading ? "Initializing..." : "Choose Schedule"} <ArrowRight size={18} />
                </button>
            </div>
        </div>
    );
};

export default Step1Reference;
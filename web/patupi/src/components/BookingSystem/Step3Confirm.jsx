import React, { useState } from 'react';
import axios from 'axios';
import { CheckCircle, ArrowLeft } from 'lucide-react';

const Step3Confirm = ({ appointmentId, data, onBack, onComplete }) => {
    const [loading, setLoading] = useState(false);

    const handleConfirm = async () => {
        if (!appointmentId) {
            alert("Session lost. Please restart the booking.");
            return;
        }

        setLoading(true);
try {
    const payload = {
    serviceId: data.service?.service_id || null, 
    scheduledAt: `${data.date}T${data.time}`     
};

  
    if (!payload.serviceId) {
        alert("Please go back and select a service.");
        setLoading(false);
        return;
    }

    await axios.put(`http://localhost:8080/api/appointments/confirm/${appointmentId}`, payload);
    onComplete(); 
} catch (err) {
            console.error("Confirmation error:", err.response?.data || err.message);
            alert("Could not finish booking. Check if the service exists in the database.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="step-container confirm-step">
            <h3 className="gold-text">REVIEW BOOKING</h3>
            
            <div className="summary-box glass-panel">
                <div className="summary-item">
                    <span>Service:</span>
                    <p>{data.service?.name || "Custom Style"}</p>
                </div>
                <div className="summary-item">
                    <span>When:</span>
                    <p>{data.displayDate} at {data.displayTime}</p>
                </div>
                <div className="summary-item total-row">
                    <span>Price:</span>
                    <p className="gold-text">
                        {data.service?.base_price ? `₱${data.service.base_price}` : "To be quoted"}
                    </p>
                </div>
            </div>

            {data.description && (
                <div className="note-box glass-panel">
                    <small className="label">Your Request:</small>
                    <p>"{data.description}"</p>
                </div>
            )}
            
            <div className="button-group">
                <button 
                    onClick={onBack} 
                    className="back-button" 
                    disabled={loading}
                >
                    <ArrowLeft size={16}/> Back
                </button>
                
                <button 
                    onClick={handleConfirm} 
                    className="gold-button" 
                    disabled={loading}
                >
                    {loading ? "Processing..." : "Confirm & Book"} <CheckCircle size={18} />
                </button>
            </div>
        </div>
    );

    
};

export default Step3Confirm;
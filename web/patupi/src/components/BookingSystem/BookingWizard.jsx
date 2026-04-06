import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Step1Reference from './Step1Reference'; 
import Step2Schedule from './Step2Schedule';
import Step3Confirm from './Step3Confirm';
import BookingFinal from './BookingFinal';
import './Booking.css';

const BookingWizard = ({ isOpen, onClose }) => {
    const [step, setStep] = useState(1);
    const [appointmentId, setAppointmentId] = useState(null);
    const [user, setUser] = useState(null);
    const [bookingData, setBookingData] = useState({
        description: '',
        service: null, 
        date: null,
        time: null,
        displayDate: '',
        displayTime: ''
    });


useEffect(() => {
    if (isOpen) {
    
        const loggedInUser = sessionStorage.getItem('user'); 
        if (loggedInUser) {
            setUser(JSON.parse(loggedInUser));
        }
    }
}, [isOpen]);

    if (!isOpen) return null;

    const currentCustomerId = user?.userId || user?.id;

    const handleStep1Complete = (id, data) => {
        setAppointmentId(id);
        setBookingData(prev => ({ ...prev, ...data }));
        setStep(2);
    };

    const handleStep2Complete = (data) => {
        setBookingData(prev => ({ ...prev, ...data }));
        setStep(3);
    };

    const handleBookingSuccess = () => {
      
        setStep(4); 
    };

    const handleCancel = async () => {
      
        if (appointmentId && step < 4) {
            try {
                await axios.delete(`http://localhost:8080/api/appointments/${appointmentId}`);
            } catch (err) {
                console.warn("Clean-up failed:", err);
            }
        }
        handleReset();
    };

    const handleReset = () => {
        setStep(1);
        setAppointmentId(null);
        setBookingData({
            description: '',
            service: null,
            date: null,
            time: null,
            displayDate: '',
            displayTime: ''
        });
        onClose();
    };

    return (
        <div className="modal-overlay">
            <div className={`booking-card glass-panel ${step === 4 ? 'final-view' : ''}`}>
                
                {/* Hide sidebar on the final success screen*/}
                {step < 4 && (
                    <div className="wizard-sidebar">
                        <div className="sidebar-header">
                            <span className="gold-text">PATUPI</span>
                            <h2>Step {step} of 3</h2>
                        </div>
                        
                        <nav className="step-indicator">
                            <div className={`step-item ${step >= 1 ? 'active' : ''}`}>
                                <div className="step-number">1</div>
                                <div className="step-label">Style</div>
                            </div>
                            <div className={`step-item ${step >= 2 ? 'active' : ''}`}>
                                <div className="step-number">2</div>
                                <div className="step-label">Schedule</div>
                            </div>
                            <div className={`step-item ${step >= 3 ? 'active' : ''}`}>
                                <div className="step-number">3</div>
                                <div className="step-label">Confirm</div>
                            </div>
                        </nav>
                        
                        <button className="back-button" onClick={handleCancel} style={{marginTop: 'auto'}}>
                            Cancel
                        </button>
                    </div>
                )}

                <div className="wizard-content">
                    {step === 1 && (
                        <Step1Reference 
                            customerId={currentCustomerId} 
                            onNext={handleStep1Complete} 
                        />
                    )}
                    
                    {step === 2 && (
                        <Step2Schedule 
                            onNext={handleStep2Complete}
                            onBack={() => setStep(1)}
                        />
                    )}
                    
                    {step === 3 && (
                        <Step3Confirm 
                            appointmentId={appointmentId}
                            data={bookingData} 
                            onBack={() => setStep(2)}
                            onComplete={handleBookingSuccess} 
                        />
                    )}

                    {step === 4 && (
                        <BookingFinal 
                            data={bookingData} 
                            onComplete={handleReset} 
                        />
                    )}
                </div>
            </div>
        </div>
    );
};

export default BookingWizard;
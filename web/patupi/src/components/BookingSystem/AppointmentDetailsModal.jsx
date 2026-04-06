import React from 'react';
import { Calendar, Clock, Scissors, Info, DollarSign, Target, XCircle } from 'lucide-react';
import './AppointmentDetails.css';

const AppointmentDetailsModal = ({ isOpen, onClose, appointment, onCancel }) => {
  if (!isOpen || !appointment) return null;

  
  const displayService = appointment.serviceName || appointment.service?.name || "Service Details";
  
  
  const rawPrice = appointment.totalAmount || appointment.base_price || 0;

 
  const displayId = appointment.appointmentId || appointment.id || 0;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="premium-details-card" onClick={(e) => e.stopPropagation()}>
        
        <div className="ticket-top-edge"></div>

        <div className="modal-header">
          <div className="header-title">
            <div className="brand-logo-mini">
              <Scissors size={16} color="#121212"/>
            </div>
            <span>Session Details</span>
          </div>
        </div>

        <div className="modal-body">
          
          {/* SERVICE SECTION */}
          <div className="info-block service-focus">
            <div className="block-label-flex">
              <Target size={14} className="icon-gold" />
              <label>Service Selected</label>
            </div>
            <div className="service-header">
              <h2 className="service-name-text">{displayService}</h2>
              <code className="reference-badge">
                PT-{displayId.toString().padStart(4, '0')}
              </code>
            </div>
            <p className="service-desc-text">
              {appointment.description || "Professional grooming session."}
            </p>
          </div>

          {/* DATE & TIME GRID */}
          <div className="details-data-grid">
            <div className="data-item">
              <Calendar size={18} className="icon-gold" />
              <div className="data-stack">
                <label>Date</label>
                <span>
                  {appointment.scheduledAt 
                    ? new Date(appointment.scheduledAt).toLocaleDateString('en-US', { 
                        month: 'long', day: 'numeric', year: 'numeric' 
                      })
                    : "Not Scheduled"}
                </span>
              </div>
            </div>


            <div className="data-item">
              <Clock size={18} className="icon-gold" />
              <div className="data-stack">
                <label>Time Slot</label>
                <span>
                  {appointment.scheduledAt 
                    ? new Date(appointment.scheduledAt).toLocaleTimeString([], { 
                        hour: '2-digit', minute: '2-digit' 
                      })
                    : "TBD"}
                </span>
              </div>
            </div>
          </div>


          {/* SUMMARY / AMOUNT SECTION */}
          <div className="info-block receipt-summary">
            <div className="block-label-flex">
              <DollarSign size={14} className="icon-gold" />
              <label>Payment Summary</label>
            </div>
            
            <div className="summary-row">
              <span className="summary-label">Total Amount</span>
              <span className="total-value-text">
                PHP {Number(rawPrice).toLocaleString(undefined, { 
                  minimumFractionDigits: 2, 
                  maximumFractionDigits: 2 
                })}
              </span>
            </div>
            
            <div className="summary-row">
              <span className="summary-label">Booking Status</span>
              <span className={`status-badge-flat ${appointment.status?.toLowerCase()}`}>
                {appointment.status || "PENDING"}
              </span>
            </div>
          </div>

          <div className="modal-policy-notice">
            <Info size={16} />
            <p>Please arrive 5 minutes early. Late arrivals may be subject to rescheduling.</p>
          </div>
        </div>

        <div className="modal-actions-area">
          <button className="done-action-btn" onClick={onClose}>
            Close Ticket
          </button>
        </div>
      </div>
    </div>
  );
};

export default AppointmentDetailsModal;
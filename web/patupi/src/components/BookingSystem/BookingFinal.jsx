import React from 'react';
import { CheckCircle2, Calendar, Clock, Info } from 'lucide-react';



const BookingFinal = ({ data, onComplete }) => {
    return (
        <div className="final-screen-wrapper">
            <div className="success-circle">
                <CheckCircle2 size={40} color="#000" strokeWidth={2.5} />
            </div>

            <h2 className="gold-text success-title">Booking Confirmed!</h2>
            <p className="subtitle-text center-text">
                Your premium grooming experience is now secured. We look forward to seeing you.
            </p>

            <div className="final-details-card glass-panel">
                <div className="detail-row">
                    <div className="icon-box-muted"><Calendar size={18} color="#D4AF37" /></div>
                    <div>
                        <span className="detail-label">DATE</span>
                        <p className="detail-value">{data.displayDate || "April 9, 2026"}</p>
                    </div>
                </div>

                <div className="detail-row">
                    <div className="icon-box-muted"><Clock size={18} color="#D4AF37" /></div>
                    <div>
                        <span className="detail-label">TIME</span>
                        <p className="detail-value">{data.displayTime || "02:00 PM"}</p>
                    </div>
                </div>

                <div className="detail-divider"></div>

                <div className="detail-row">
                    <div className="icon-box-muted"><Info size={18} color="#D4AF37" /></div>
                    <div>
                        <span className="detail-label">STATUS</span>
                        <p className="status-subtext">A professional barber will be assigned soon</p>
                    </div>
                </div>
            </div>

            <div className="final-actions">
                <button className="gold-button w-100" onClick={onComplete}>
                    Back to Dashboard
                </button>
            </div>

            <p className="support-text">
                Need to change your booking? <span className="gold-link">Contact support</span>
            </p>
        </div>
    );


};

export default BookingFinal;
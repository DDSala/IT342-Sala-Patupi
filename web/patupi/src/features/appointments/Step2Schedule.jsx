import React, { useState } from 'react';
import DatePicker from 'react-datepicker';
import "react-datepicker/dist/react-datepicker.css";
import { Calendar, Clock, ArrowRight, ArrowLeft } from 'lucide-react';

const Step2Schedule = ({ onNext, onBack }) => {
    const [startDate, setStartDate] = useState(null);
    const [selectedTime, setSelectedTime] = useState('');

    // Grouped Time Slots
    const timeSlots = {
        Morning: [
            "09:00 AM",
            "10:00 AM",
            "11:00 AM",
        ],
        Afternoon: [
            "01:00 PM",
            "02:00 PM",
            "03:00 PM",
        ],
        Evening: [
            "04:00 PM",
            "05:00 PM",
            "06:00 PM",
        ]
    };

    const handleNext = () => {
        if (!startDate || !selectedTime) {
            return alert("Please pick a date and a time slot.");
        }

        const yyyy = startDate.getFullYear();
        const mm = String(startDate.getMonth() + 1).padStart(2, '0');
        const dd = String(startDate.getDate()).padStart(2, '0');

        // Convert selected time to 24-hour format
        const [time, modifier] = selectedTime.split(' ');
        let [hours, minutes] = time.split(':');

        let hoursInt = parseInt(hours, 10);

        if (modifier === 'PM' && hoursInt !== 12) {
            hoursInt += 12;
        }

        if (modifier === 'AM' && hoursInt === 12) {
            hoursInt = 0;
        }

        const finalTime = `${hoursInt
            .toString()
            .padStart(2, '0')}:${minutes}:00`;

        onNext({
            date: `${yyyy}-${mm}-${dd}`,
            time: finalTime,
            displayDate: startDate.toLocaleDateString('en-US', {
                month: 'long',
                day: 'numeric',
                year: 'numeric'
            }),
            displayTime: selectedTime
        });
    };

    return (
        <div className="step-container">
            <h3 className="gold-text">PICK A SCHEDULE</h3>

            {/* Date Picker */}
            <div className="input-section">
                <label className="input-label">
                    <Calendar size={16} /> Select Date
                </label>

                <div className="datepicker-wrapper">
                    <DatePicker
                        selected={startDate}
                        onChange={(date) => setStartDate(date)}
                        minDate={new Date()}
                        placeholderText="Click to view calendar"
                        className="glass-input-premium calendar-trigger"
                        dateFormat="MMMM d, yyyy"
                    />
                </div>
            </div>

            {/* Time Slots */}
            <div className="input-section">
                <label className="input-label">
                    <Clock size={16} /> Available Times
                </label>

                <div className="time-sections">

                    {Object.entries(timeSlots).map(([section, times]) => (
                        <div
                            key={section}
                            style={{ marginBottom: '20px' }}
                        >

                            {/* Section Header */}
                            <h4
                                style={{
                                    marginBottom: '10px',
                                    color: '#d4af37',
                                    fontSize: '14px',
                                    fontWeight: '600'
                                }}
                            >
                                {section}
                            </h4>

                            {/* Time Grid */}
                            <div
                                className="time-grid"
                                style={{
                                    display: 'grid',
                                    gridTemplateColumns: 'repeat(3, 1fr)',
                                    gap: '8px'
                                }}
                            >
                                {times.map((t) => (
                                    <button
                                        key={t}
                                        className={`time-chip ${selectedTime === t ? 'active' : ''}`}
                                        onClick={() => setSelectedTime(t)}
                                    >
                                        {t}
                                    </button>
                                ))}
                            </div>
                        </div>
                    ))}

                </div>
            </div>

            {/* Navigation Buttons */}
            <div
                className="button-group"
                style={{ marginTop: '20px' }}
            >
                <button
                    onClick={onBack}
                    className="back-button"
                >
                    <ArrowLeft size={16} />
                    Back
                </button>

                <button
                    onClick={handleNext}
                    className="gold-button"
                >
                    Next
                    <ArrowRight size={16} />
                </button>
            </div>
        </div>
    );
};

export default Step2Schedule;
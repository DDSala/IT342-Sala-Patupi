Accomplishments:

1.) Improved Login Functionality: Improved LoginActivity and activity_login.xml.
2.) Session Management: Implemented SharedPreferences to securely store user data, allowing the app to "remember" who is logged in.

3.) Partial Implementation of the Mobile Dashboard - Partially Implemented Dashboard Functionality.
The Dashboard can now do the following for now:
- Active Ticket Display: The dashboard automatically detects and highlights the user's current session (Confirmed, Pending, or Draft).

- Web-to-Mobile Sync: Any booking made in the Web Portal now instantly populates in the Mobile Dashboard.

- Mobile Cancellation: Users can now cancel active tickets directly from their phones. This includes the customerId verification logic to ensure security.

- Recent Activity Table: Implement the clean, organized history section that lists previous appointments.

- Live Weather Integration: Integrated the Open-Meteo API to display real-time weather data for Cebu City (10.31°N, 123.88°E) without requiring an API key.


4.) Partial Implementation of the Mobile Profile Page.
- User Identification: The page dynamically displays the user's Full Name and Email Address pulled from the local session.

- Navigation Control: Fully functional "Back" button to return to the Dashboard and a "Logout" feature that clears the session and redirects to the Login screen.

- UI/UX: Established a premium dark-themed aesthetic in activity_profile.xml to match the brand identity.


Next Steps
- Completely Implement the Main Transaction Functionality which is the Appointment Booking.
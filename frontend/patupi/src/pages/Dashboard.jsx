import React, { useEffect, useState } from 'react';
import { supabase } from '../supabaseClient';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
    const [user, setUser] = useState(null);
    const navigate = useNavigate();

    useEffect(() => {
        // Check if a user is actually logged in
        const getUser = async () => {
            const { data: { user } } = await supabase.auth.getUser();
            if (!user) {
                navigate('/login'); // Redirect to login if no session found
            } else {
                setUser(user);
            }
        };
        getUser();
    }, [navigate]);

    const handleLogout = async () => {
        await supabase.auth.signOut();
        navigate('/login');
    };

    if (!user) return <div className="bg-charcoal min-h-screen"></div>;

    return (
        <div className="min-h-screen bg-charcoal text-white p-8">
            <nav className="flex justify-between items-center mb-12 border-b border-gray-800 pb-6">
                <h1 className="text-2xl font-bold text-gold">Patupi Dashboard</h1>
                <button 
                    onClick={handleLogout}
                    className="text-sm bg-red-900/20 text-red-400 px-4 py-2 rounded-lg border border-red-900/50 hover:bg-red-900/40 transition-all"
                >
                    Logout
                </button>
            </nav>

            <main className="max-w-4xl mx-auto">
                <div className="bg-[#2a2a22] p-8 rounded-2xl border border-gray-800 shadow-xl">
                    <h2 className="text-3xl font-semibold mb-4">Welcome back, {user.email}!</h2>
                    <p className="text-gray-400 mb-8">Ready for your next premium grooming session?</p>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="p-6 bg-inputBg rounded-xl border border-gray-700 hover:border-gold cursor-pointer transition-all">
                            <h3 className="text-gold font-bold mb-2">My Appointments</h3>
                            <p className="text-sm text-gray-500">View your upcoming barber schedules.</p>
                        </div>
                        <div className="p-6 bg-inputBg rounded-xl border border-gray-700 hover:border-gold cursor-pointer transition-all">
                            <h3 className="text-gold font-bold mb-2">Book Now</h3>
                            <p className="text-sm text-gray-500">Find a barber and schedule a cut.</p>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
};

export default Dashboard;
import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import * as api from '../services/api';
// --- FIX: Remove unused SockJS/Stomp imports since we use @stomp/stompjs now ---
import { Client } from '@stomp/stompjs';
import ChatComponent from '../components/ChatComponent';

// Import Recharts components
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';

function UserDashboard() {
  const auth = useAuth();
  
  // Data State
  const [devices, setDevices] = useState([]);
  const [chartData, setChartData] = useState([]);
  
  // Selection State
  const [selectedDeviceId, setSelectedDeviceId] = useState('');
  const [selectedDate, setSelectedDate] = useState('');
  
  // UI State
  const [loadingDevices, setLoadingDevices] = useState(true);
  const [loadingChart, setLoadingChart] = useState(false);
  const [error, setError] = useState('');
  
  // Notification State
  const [notifications, setNotifications] = useState([]);

  // --- 1. WebSocket for ALERTS ---
  useEffect(() => {
    if (!auth.userId) return;

    const client = new Client({
        brokerURL: 'ws://localhost:8000/ws', 
        reconnectDelay: 5000,
        onConnect: () => {
            client.subscribe(`/topic/alerts/${auth.userId}`, (message) => {
                const alert = JSON.parse(message.body);
                setNotifications(prev => [alert, ...prev]);
                
                setTimeout(() => {
                    setNotifications(prev => prev.filter(n => n !== alert));
                }, 5000);
            });
        },
        onStompError: (frame) => {
            console.error('Broker reported error: ' + frame.headers['message']);
            console.error('Additional details: ' + frame.body);
        }
    });

    client.activate();

    return () => {
        client.deactivate();
    };
  }, [auth.userId]); 

  // --- 2. Fetch Users Devices on Load ---
  useEffect(() => {
    const fetchDevices = async () => {
      try {
        setLoadingDevices(true);
        const userDevices = await api.getDevicesForUser(auth.token, auth.userId);
        setDevices(userDevices);
        if (userDevices.length > 0) setSelectedDeviceId(userDevices[0].id);
      } catch (err) {
        console.error('Failed to fetch devices', err);
        setError('Could not load devices.');
      }
      setLoadingDevices(false);
    };
    if (auth.token && auth.userId) fetchDevices();
  }, [auth.token, auth.userId]);

  // --- 3. Fetch Chart Data ---
  useEffect(() => {
    const fetchMonitoringData = async () => {
      if (!selectedDeviceId || !selectedDate) return;

      try {
        setLoadingChart(true);
        setError('');
        
        const dateObj = new Date(selectedDate);
        const offset = dateObj.getTimezoneOffset() * 60000;
        const timestamp = dateObj.getTime() + offset;

        const data = await api.getEnergyConsumption(auth.token, selectedDeviceId, timestamp);
        
        const formattedData = data.map(item => {
          const date = new Date(item.timestamp);
          const hourLabel = date.getHours().toString().padStart(2, '0') + ":00";
          return { time: hourLabel, energy: item.hourlyConsumption };
        });
        formattedData.sort((a, b) => a.time.localeCompare(b.time));
        setChartData(formattedData);
      } catch (err) {
        console.error('Error fetching chart data', err);
        setChartData([]);
      }
      setLoadingChart(false);
    };
    fetchMonitoringData();
  }, [selectedDeviceId, selectedDate, auth.token]);

  const handleDeviceChange = (e) => setSelectedDeviceId(e.target.value);
  const handleDateChange = (e) => setSelectedDate(e.target.value);

  if (loadingDevices) return <p>Loading dashboard...</p>;

  return (
    <div className="page-container" style={{ padding: '20px', position: 'relative' }}>
      
      {/* --- NOTIFICATION AREA --- */}
      <div className="notification-container" style={{ 
          position: 'fixed', top: '80px', right: '20px', zIndex: 999, width: '300px' 
      }}>
        {notifications.map((notif, index) => (
            <div key={index} style={{ 
                backgroundColor: '#dc3545', color: 'white', padding: '15px', 
                marginBottom: '10px', borderRadius: '5px', boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
                animation: 'fadeIn 0.5s'
            }}>
                <strong>⚠️ Overconsumption Alert:</strong><br/>
                {notif.message}
            </div>
        ))}
      </div>

      <h2>Energy Dashboard</h2>
      {error && <p className="error-text" style={{color: 'red'}}>{error}</p>}

      {/* --- Controls Section --- */}
      <div className="controls" style={{ 
        display: 'flex', gap: '20px', marginBottom: '30px', padding: '20px',
        backgroundColor: '#f5f5f5', borderRadius: '8px'
      }}>
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <label style={{ marginBottom: '5px', fontWeight: 'bold' }}>Select Device:</label>
          <select value={selectedDeviceId} onChange={handleDeviceChange} style={{ padding: '8px', minWidth: '200px' }}>
            {devices.map(device => (
              <option key={device.id} value={device.id}>{device.name}</option>
            ))}
          </select>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <label style={{ marginBottom: '5px', fontWeight: 'bold' }}>Select Date:</label>
          <input type="date" value={selectedDate} onChange={handleDateChange} style={{ padding: '8px' }}/>
        </div>
      </div>

      {/* --- Chart Section --- */}
      <div className="chart-container" style={{ height: '400px', width: '100%' }}>
        {!selectedDate ? (
          <p>Please select a date to view consumption.</p>
        ) : loadingChart ? (
          <p>Loading chart data...</p>
        ) : chartData.length === 0 ? (
          <p>No energy data found for this device on this date.</p>
        ) : (
          <ResponsiveContainer width="100%" height="100%">
            <BarChart data={chartData} margin={{ top: 5, right: 30, left: 20, bottom: 5 }}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="time" />
              <YAxis label={{ value: 'Energy (kWh)', angle: -90, position: 'insideLeft' }} />
              <Tooltip />
              <Legend />
              <Bar dataKey="energy" fill="#8884d8" name="Hourly Consumption" />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>

      {/* --- CHAT COMPONENT --- */}
      <ChatComponent />
    </div>
  );
}

export default UserDashboard;

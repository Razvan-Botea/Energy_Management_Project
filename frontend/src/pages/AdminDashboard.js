import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import * as api from '../services/api';

// --- Reusable User Form ---
const UserForm = ({ onSubmit, defaultValues = {} }) => {
  const [username, setUsername] = useState(defaultValues.username || '');
  const [password, setPassword] = useState('');
  const [name, setName] = useState(defaultValues.name || '');
  const [role, setRole] = useState(defaultValues.role || 'USER');

  const handleSubmit = (e) => {
    e.preventDefault();
    const userData = { username, name, role };
    if (password) {
      userData.password = password;
    }
    onSubmit(userData);
    if (!defaultValues.id) {
      setUsername('');
      setPassword('');
      setName('');
      setRole('USER');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="form-container">
      <h4>{defaultValues.id ? 'Update User' : 'Create User'}</h4>
      <div>
        <label>Username</label>
        <input
          type="text"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
      </div>
      <div>
        <label>Password {!defaultValues.id && '(Required for create)'}</label>
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder={defaultValues.id ? 'Set new password (optional)' : ''}
        />
      </div>
      <div>
        <label>Full Name</label>
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
      </div>
      <div>
        <label>Role</label>
        <select value={role} onChange={(e) => setRole(e.target.value)}>
          <option value="USER">USER</option>
          <option value="ADMIN">ADMIN</option>
        </select>
      </div>
      <button type="submit">{defaultValues.id ? 'Update' : 'Create'}</button>
    </form>
  );
};

// --- NEW: Reusable Device Form ---
const DeviceForm = ({ onSubmit, users, defaultValues = {} }) => {
  const [name, setName] = useState(defaultValues.name || '');
  const [address, setAddress] = useState(defaultValues.address || '');
  const [maxConsumption, setMaxConsumption] = useState(
    defaultValues.maximumConsumption || ''
  );
  const [userId, setUserId] = useState(defaultValues.userId || '');

  const handleSubmit = (e) => {
    e.preventDefault();
    const deviceData = {
      name,
      address,
      maximumConsumption: parseFloat(maxConsumption),
      userId: userId || null, // Send null if unassigned
    };
    onSubmit(deviceData);
    if (!defaultValues.id) {
      setName('');
      setAddress('');
      setMaxConsumption('');
      setUserId('');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="form-container">
      <h4>{defaultValues.id ? 'Update Device' : 'Create Device'}</h4>
      <div>
        <label>Device Name</label>
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
      </div>
      <div>
        <label>Address (e.g., Living Room)</label>
        <input
          type="text"
          value={address}
          onChange={(e) => setAddress(e.target.value)}
          required
        />
      </div>
      <div>
        <label>Max Consumption (W)</label>
        <input
          type="number"
          value={maxConsumption}
          onChange={(e) => setMaxConsumption(e.target.value)}
          required
        />
      </div>
      <div>
        <label>Assign to User (Optional)</label>
        <select value={userId} onChange={(e) => setUserId(e.target.value)}>
          <option value="">-- Unassigned --</option>
          {users
            .filter((u) => u.role === 'USER')
            .map((user) => (
              <option key={user.id} value={user.id}>
                {user.name} ({user.username})
              </option>
            ))}
        </select>
      </div>
      <button type="submit">{defaultValues.id ? 'Update' : 'Create'}</button>
    </form>
  );
};

// --- Main Admin Dashboard Component ---
function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [devices, setDevices] = useState([]);
  const [editingUser, setEditingUser] = useState(null);
  const [editingDevice, setEditingDevice] = useState(null); // <-- NEW STATE
  const auth = useAuth();

  // Fetch all data on load
  const fetchData = async () => {
    try {
      const [userList, deviceList] = await Promise.all([
        api.getUsers(auth.token),
        api.getDevices(auth.token),
      ]);
      setUsers(userList);
      setDevices(deviceList);
    } catch (error) {
      console.error('Failed to fetch data', error);
      if (error.message.includes('401')) {
        auth.logout(); // Token might be expired, log out
      }
    }
  };

  useEffect(() => {
    if (auth.token) {
      fetchData();
    }
  }, [auth.token]);

  // --- User Handlers (Unchanged) ---
  const handleCreateUser = async (user) => {
    try {
      await api.createUser(auth.token, user);
      fetchData();
    } catch (error) {
      alert(`Error creating user: ${error.message}`);
    }
  };

  const handleUpdateUser = async (user) => {
    try {
      await api.updateUser(auth.token, editingUser.id, user);
      setEditingUser(null);
      fetchData();
    } catch (error) {
      alert(`Error updating user: ${error.message}`);
    }
  };

  const handleDeleteUser = async (userId) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      try {
        await api.deleteUser(auth.token, userId);
        fetchData();
      } catch (error) {
        alert(`Error deleting user: ${error.message}`);
      }
    }
  };

  // --- NEW: Device Handlers ---
  const handleCreateDevice = async (device) => {
    try {
      await api.createDevice(auth.token, device);
      fetchData(); // Refresh data
    } catch (error) {
      alert(`Error creating device: ${error.message}`);
    }
  };

  const handleUpdateDevice = async (device) => {
    try {
      await api.updateDevice(auth.token, editingDevice.id, device);
      setEditingDevice(null); // Close the form
      fetchData(); // Refresh data
    } catch (error) {
      alert(`Error updating device: ${error.message}`);
    }
  };

  const handleDeleteDevice = async (deviceId) => {
    if (window.confirm('Are you sure you want to delete this device?')) {
      try {
        await api.deleteDevice(auth.token, deviceId);
        fetchData(); // Refresh data
      } catch (error) {
        alert(`Error deleting device: ${error.message}`);
      }
    }
  };

  // This handler is for the dropdown in the list (quick-assign)
  // It's different from the form's update
  const handleAssignDevice = async (deviceId, newUserId) => {
    const device = devices.find((d) => d.id === deviceId);
    if (!device) return;

    // We must send the *full* DTO for the PUT request, not just the userId
    const updatedDevice = {
      name: device.name,
      address: device.address,
      maximumConsumption: device.maximumConsumption,
      userId: newUserId || null, // Send null if "Assign to..." is selected
    };

    try {
      await api.updateDevice(auth.token, deviceId, updatedDevice);
      fetchData(); // Refresh list to show new assignment
    } catch (error) {
      alert(`Error assigning device: ${error.message}`);
    }
  };

  // --- Main Render ---
  return (
    <div className="page-container">
      <h2>Admin Dashboard</h2>
      <hr />

      {/* --- User Management Section (Unchanged) --- */}
      <h3>User Management</h3>
      {editingUser ? (
        <>
          <UserForm
            onSubmit={handleUpdateUser}
            defaultValues={editingUser}
          />
          <button
            onClick={() => setEditingUser(null)}
            style={{ marginTop: '10px' }}
          >
            Cancel Update
          </button>
        </>
      ) : (
        <UserForm onSubmit={handleCreateUser} />
      )}
      <ul className="item-list">
        {users.map((user) => (
          <li key={user.id}>
            <div className="item-info">
              <strong>{user.name}</strong> ({user.username})
              <br />
              Role: {user.role} | ID: {user.id}
            </div>
            <div className="item-actions">
              <button onClick={() => setEditingUser(user)}>Update</button>
              <button
                className="delete-btn"
                onClick={() => handleDeleteUser(user.id)}
              >
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>

      <hr />

      {/* --- Device Management Section (NEW & UPDATED) --- */}
      <h3>Device Management</h3>
      {editingDevice ? (
        <>
          <DeviceForm
            onSubmit={handleUpdateDevice}
            defaultValues={editingDevice}
            users={users}
          />
          <button
            onClick={() => setEditingDevice(null)}
            style={{ marginTop: '10px' }}
          >
            Cancel Update
          </button>
        </>
      ) : (
        <DeviceForm onSubmit={handleCreateDevice} users={users} />
      )}

      <ul className="item-list">
        {devices.map((device) => (
          <li key={device.id}>
            <div className="item-info">
              <strong>{device.name}</strong> ({device.address})
              <br />
              Max: {device.maximumConsumption}W | Assigned to:{' '}
              {users.find((u) => u.id === device.userId)?.name || (
                <em>None</em>
              )}
            </div>
            <div className="item-actions">
              <select
                value={device.userId || ''}
                onChange={(e) => handleAssignDevice(device.id, e.target.value)}
              >
                <option value="">-- Re-assign --</option>
                {users
                  .filter((u) => u.role === 'USER')
                  .map((user) => (
                    <option key={user.id} value={user.id}>
                      {user.name}
                    </option>
                  ))}
              </select>
              <button onClick={() => setEditingDevice(device)}>Update</button>
              <button
                className="delete-btn"
                onClick={() => handleDeleteDevice(device.id)}
              >
                Delete
              </button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}

export default AdminDashboard;

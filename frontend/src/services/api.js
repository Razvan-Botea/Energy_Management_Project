const API_URL = 'http://localhost:8000/api';

const handleResponse = async (response) => {
  const text = await response.text();

  if (!response.ok) {
    let errorData = { message: `Error: ${response.status}` };
    try {
      if (text) {
        errorData = JSON.parse(text);
      }
    } catch (e) {
    }
    throw new Error(errorData.message || `Error: ${response.status}`);
  }

  if (text.length === 0) {
    return;
  }

  return JSON.parse(text);
};

const getAuthHeaders = (token) => {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };
};

export const login = (username, password) => {
  return fetch(`${API_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, password }),
  }).then(handleResponse);
};

export const getUsers = (token) => {
  return fetch(`${API_URL}/users`, {
    headers: getAuthHeaders(token),
  }).then(handleResponse);
};

export const createUser = (token, user) => {
  return fetch(`${API_URL}/users`, {
    method: 'POST',
    headers: getAuthHeaders(token),
    body: JSON.stringify(user),
  }).then(handleResponse);
};

export const updateUser = (token, id, user) => {
  return fetch(`${API_URL}/users/${id}`, {
    method: 'PUT',
    headers: getAuthHeaders(token),
    body: JSON.stringify(user),
  }); 
};

export const deleteUser = (token, id) => {
  return fetch(`${API_URL}/users/${id}`, {
    method: 'DELETE',
    headers: getAuthHeaders(token),
  }).then(handleResponse);
};

export const getDevices = (token) => {
  return fetch(`${API_URL}/devices`, {
    headers: getAuthHeaders(token),
  }).then(handleResponse);
};

export const getDevicesForUser = (token, userId) => {
  return fetch(`${API_URL}/devices/user/${userId}`, {
    headers: getAuthHeaders(token),
  }).then(handleResponse);
};

export const createDevice = (token, device) => {
  return fetch(`${API_URL}/devices`, {
    method: 'POST',
    headers: getAuthHeaders(token),
    body: JSON.stringify(device),
  });
};

export const updateDevice = (token, id, device) => {
  return fetch(`${API_URL}/devices/${id}`, {
    method: 'PUT',
    headers: getAuthHeaders(token),
    body: JSON.stringify(device),
  });
};

export const deleteDevice = (token, id) => {
  return fetch(`${API_URL}/devices/${id}`, {
    method: 'DELETE',
    headers: getAuthHeaders(token),
  }).then(handleResponse);
};

export const getEnergyConsumption = (token, deviceId, dateTimestamp) => {
  return fetch(`${API_URL}/monitoring/${deviceId}/${dateTimestamp}`, {
    headers: getAuthHeaders(token),
  }).then(handleResponse);
};

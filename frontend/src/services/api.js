const API_URL = 'http://localhost:8000/api';

// Helper function to handle fetch responses
const handleResponse = async (response) => {
  // Get the response as text first
  const text = await response.text();

  if (!response.ok) {
    // If it's an error, try to parse the text as a JSON error message
    let errorData = { message: `Error: ${response.status}` };
    try {
      if (text) {
        errorData = JSON.parse(text);
      }
    } catch (e) {
      // Ignore if error response wasn't JSON
    }
    throw new Error(errorData.message || `Error: ${response.status}`);
  }

  // If the response is OK but the text is empty (like on create or delete),
  // just return successfully without parsing.
  if (text.length === 0) {
    return;
  }

  // If we're here, the response is OK and has text, so parse it.
  return JSON.parse(text);
};

// Creates the auth header
const getAuthHeaders = (token) => {
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`,
  };
};

// --- Auth Service ---
export const login = (username, password) => {
  return fetch(`${API_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, password }),
  }).then(handleResponse);
};

// --- User Service ---
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
  }); // PUT might not return content
};

export const deleteUser = (token, id) => {
  return fetch(`${API_URL}/users/${id}`, {
    method: 'DELETE',
    headers: getAuthHeaders(token),
  }).then(handleResponse);
};

// --- Device Service ---
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

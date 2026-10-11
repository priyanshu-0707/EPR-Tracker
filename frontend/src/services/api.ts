import axios from 'axios';

let authToken: string | null = null;

export const setAuthTokenInMemory = (token: string | null) => {
  authToken = token;
};

export const getAuthTokenInMemory = (): string | null => {
  return authToken;
};

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL !== undefined ? import.meta.env.VITE_API_BASE_URL : '',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  if (authToken) {
    config.headers.Authorization = `Bearer ${authToken}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    return Promise.reject(error);
  }
);

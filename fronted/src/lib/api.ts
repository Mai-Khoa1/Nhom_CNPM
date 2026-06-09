import axios from 'axios';

// Base URL for the Spring Boot backend
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle 401 errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Auth API
export const authAPI = {
  login: (email: string, password: string) =>
    api.post('/auth/login', { email, password }),
  register: (username: string, email: string, password: string) =>
    api.post('/auth/register', { username, email, password }),
};

// Horses API
export const horsesAPI = {
  getAll: () => api.get('/horses'),
  getById: (id: number) => api.get(`/horses/${id}`),
  create: (horse: HorsePayload) => api.post('/horses', horse),
  update: (id: number, horse: HorsePayload) => api.put(`/horses/${id}`, horse),
  delete: (id: number) => api.delete(`/horses/${id}`),
  getByStatus: (status: string) => api.get(`/horses/status/${status}`),
};

// Jockeys API
export const jockeysAPI = {
  getAll: () => api.get('/jockeys'),
  getById: (id: number) => api.get(`/jockeys/${id}`),
  create: (jockey: JockeyPayload) => api.post('/jockeys', jockey),
  update: (id: number, jockey: JockeyPayload) => api.put(`/jockeys/${id}`, jockey),
  delete: (id: number) => api.delete(`/jockeys/${id}`),
};

// Schedules API
export const schedulesAPI = {
  getAll: () => api.get('/schedules'),
  getById: (id: number) => api.get(`/schedules/${id}`),
  create: (schedule: SchedulePayload) => api.post('/schedules', schedule),
  update: (id: number, schedule: SchedulePayload) => api.put(`/schedules/${id}`, schedule),
  delete: (id: number) => api.delete(`/schedules/${id}`),
  getByStatus: (status: string) => api.get(`/schedules/status/${status}`),
};

// Results API
export const resultsAPI = {
  getAll: () => api.get('/results'),
  getById: (id: number) => api.get(`/results/${id}`),
  create: (result: ResultPayload) => api.post('/results', result),
  update: (id: number, result: ResultPayload) => api.put(`/results/${id}`, result),
  delete: (id: number) => api.delete(`/results/${id}`),
};

// Types
export interface HorsePayload {
  name: string;
  breed: string;
  age: number;
  speed: number;
  owner?: string;
  status?: string;
  notes?: string;
}

export interface JockeyPayload {
  name: string;
  country?: string;
  wins?: number;
  races?: number;
  bio?: string;
}

export interface SchedulePayload {
  name: string;
  raceDate: string;
  venue?: string;
  horseCount?: number;
  prize?: string;
  status?: string;
}

export interface ResultPayload {
  raceName: string;
  raceDate: string;
  venue?: string;
  winner?: string;
  jockey?: string;
  time?: string;
  prize?: string;
}

export interface Horse {
  id: number;
  name: string;
  breed: string;
  age: number;
  speed: number;
  owner: string;
  status: string;
  notes: string;
  createdAt: string;
  updatedAt: string;
}

export interface Jockey {
  id: number;
  name: string;
  country: string;
  wins: number;
  races: number;
  bio: string;
  createdAt: string;
  updatedAt: string;
}

export interface Schedule {
  id: number;
  name: string;
  raceDate: string;
  venue: string;
  horseCount: number;
  prize: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface Result {
  id: number;
  raceName: string;
  raceDate: string;
  venue: string;
  winner: string;
  jockey: string;
  time: string;
  prize: string;
  createdAt: string;
  updatedAt: string;
}

export default api;
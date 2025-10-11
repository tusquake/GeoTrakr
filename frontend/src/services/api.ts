import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

// Create axios instance
export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add token
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

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// Types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  fullName: string;
}

export interface Asset {
  id: number;
  name: string;
  type: 'VEHICLE' | 'PERSON' | 'DEVICE' | 'PACKAGE';
  description?: string;
  currentLatitude?: number;
  currentLongitude?: number;
  lastUpdate?: string;
  active: boolean;
}

export interface Geofence {
  id: number;
  name: string;
  description?: string;
  type: 'CIRCULAR' | 'POLYGONAL';
  centerLatitude?: number;
  centerLongitude?: number;
  radius?: number;
  polygonCoordinates?: string;
  alertType: 'ENTRY' | 'EXIT' | 'BOTH';
  active: boolean;
}

export interface GeofenceEvent {
  id: number;
  asset: Asset;
  geofence: Geofence;
  eventType: 'ENTER' | 'EXIT';
  latitude: number;
  longitude: number;
  timestamp: string;
  notificationSent: boolean;
}

export interface LocationUpdate {
  assetId: number;
  latitude: number;
  longitude: number;
  timestamp?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface Statistics {
  totalEntries: number;
  totalExits: number;
  totalEvents: number;
  mostActiveAssets: [number, number][];
}

// Auth API
export const authApi = {
  login: (data: LoginRequest) => 
    api.post<ApiResponse<{ token: string; username: string; role: string }>>('/auth/login', data),
  
  register: (data: RegisterRequest) => 
    api.post<ApiResponse<any>>('/auth/register', data),
};

// Assets API
export const assetsApi = {
  getAll: () => 
    api.get<ApiResponse<Asset[]>>('/assets'),
  
  getById: (id: number) => 
    api.get<ApiResponse<Asset>>(`/assets/${id}`),
  
  create: (data: Partial<Asset>) => 
    api.post<ApiResponse<Asset>>('/assets', data),
  
  update: (id: number, data: Partial<Asset>) => 
    api.put<ApiResponse<Asset>>(`/assets/${id}`, data),
  
  delete: (id: number) => 
    api.delete<ApiResponse<void>>(`/assets/${id}`),
};

// Geofences API
export const geofencesApi = {
  getAll: () => 
    api.get<ApiResponse<Geofence[]>>('/geofences'),
  
  getById: (id: number) => 
    api.get<ApiResponse<Geofence>>(`/geofences/${id}`),
  
  create: (data: Partial<Geofence>) => 
    api.post<ApiResponse<Geofence>>('/geofences', data),
  
  update: (id: number, data: Partial<Geofence>) => 
    api.put<ApiResponse<Geofence>>(`/geofences/${id}`, data),
  
  delete: (id: number) => 
    api.delete<ApiResponse<void>>(`/geofences/${id}`),
};

// Location API
export const locationApi = {
  updateLocation: (data: LocationUpdate) => 
    api.post<ApiResponse<GeofenceEvent[]>>('/location/update', data),
  
  getAssetLocation: (assetId: number) => 
    api.get<ApiResponse<Asset>>(`/location/asset/${assetId}`),
  
  getAllLocations: () => 
    api.get<ApiResponse<Asset[]>>('/location/all'),
};

// Events API
export const eventsApi = {
  getAll: () => 
    api.get<ApiResponse<GeofenceEvent[]>>('/events'),
  
  getByAsset: (assetId: number) => 
    api.get<ApiResponse<GeofenceEvent[]>>(`/events/asset/${assetId}`),
  
  getByGeofence: (geofenceId: number) => 
    api.get<ApiResponse<GeofenceEvent[]>>(`/events/geofence/${geofenceId}`),
  
  getByDateRange: (start: string, end: string) => 
    api.get<ApiResponse<GeofenceEvent[]>>('/events/date-range', {
      params: { start, end }
    }),
  
  getStatistics: (start: string, end: string) => 
    api.get<ApiResponse<Statistics>>('/events/statistics', {
      params: { start, end }
    }),
};
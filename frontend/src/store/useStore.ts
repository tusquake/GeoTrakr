import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface User {
  username: string;
  role: string;
  token: string;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  login: (user: User) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isAuthenticated: false,
      login: (user) => {
        localStorage.setItem('token', user.token);
        set({ user, isAuthenticated: true });
      },
      logout: () => {
        localStorage.removeItem('token');
        set({ user: null, isAuthenticated: false });
      },
    }),
    {
      name: 'auth-storage',
    }
  )
);

interface UIState {
  sidebarOpen: boolean;
  selectedAsset: number | null;
  selectedGeofence: number | null;
  toggleSidebar: () => void;
  selectAsset: (id: number | null) => void;
  selectGeofence: (id: number | null) => void;
}

export const useUIStore = create<UIState>((set) => ({
  sidebarOpen: true,
  selectedAsset: null,
  selectedGeofence: null,
  toggleSidebar: () => set((state) => ({ sidebarOpen: !state.sidebarOpen })),
  selectAsset: (id) => set({ selectedAsset: id }),
  selectGeofence: (id) => set({ selectedGeofence: id }),
}));
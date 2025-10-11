import { Link, useLocation, useNavigate } from 'react-router-dom';
import { MapPin, LayoutDashboard, Package, MapPinned, Activity, LogOut, Menu, X, User } from 'lucide-react';
import { useAuthStore, useUIStore } from '../store/useStore';

const menuItems = [
  { path: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { path: '/assets', icon: Package, label: 'Assets' },
  { path: '/geofences', icon: MapPinned, label: 'Geofences' },
  { path: '/events', icon: Activity, label: 'Events' },
];

export default function Layout({ children }: { children: React.ReactNode }) {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const { sidebarOpen, toggleSidebar } = useUIStore();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-background">
      {/* Mobile Menu Button */}
      <button
        onClick={toggleSidebar}
        className="lg:hidden fixed top-4 left-4 z-50 p-2 bg-card border border-border rounded-lg"
      >
        {sidebarOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
      </button>

      {/* Sidebar */}
      <aside
        className={`fixed top-0 left-0 h-full bg-card border-r border-border transition-all duration-300 z-40 ${
          sidebarOpen ? 'w-64' : 'w-0 lg:w-20'
        }`}
      >
        <div className={`flex flex-col h-full ${sidebarOpen ? 'p-4' : 'p-2 items-center'}`}>
          {/* Logo */}
          <div className={`flex items-center gap-3 mb-8 ${sidebarOpen ? 'px-2' : 'justify-center'}`}>
            <div className="p-2 bg-primary/20 rounded-lg">
              <MapPin className="w-6 h-6 text-primary" />
            </div>
            {sidebarOpen && <span className="text-xl font-bold">GeoTrackr</span>}
          </div>

          {/* Navigation */}
          <nav className="flex-1 space-y-2">
            {menuItems.map((item) => {
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex items-center gap-3 px-3 py-3 rounded-lg transition-colors ${
                    isActive
                      ? 'bg-primary text-primary-foreground'
                      : 'hover:bg-muted'
                  } ${!sidebarOpen && 'justify-center'}`}
                  title={!sidebarOpen ? item.label : ''}
                >
                  <item.icon className="w-5 h-5 flex-shrink-0" />
                  {sidebarOpen && <span>{item.label}</span>}
                </Link>
              );
            })}
          </nav>

          {/* User Section */}
          <div className="pt-4 border-t border-border">
            <div className={`flex items-center gap-3 px-3 py-2 ${!sidebarOpen && 'justify-center'}`}>
              <div className="p-2 bg-primary/20 rounded-full">
                <User className="w-4 h-4 text-primary" />
              </div>
              {sidebarOpen && (
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium truncate">{user?.username}</p>
                  <p className="text-xs text-muted-foreground">{user?.role}</p>
                </div>
              )}
            </div>

            <button
              onClick={handleLogout}
              className={`w-full flex items-center gap-3 px-3 py-3 rounded-lg hover:bg-destructive/10 text-destructive transition-colors mt-2 ${
                !sidebarOpen && 'justify-center'
              }`}
              title={!sidebarOpen ? 'Logout' : ''}
            >
              <LogOut className="w-5 h-5 flex-shrink-0" />
              {sidebarOpen && <span>Logout</span>}
            </button>
          </div>
        </div>
      </aside>

      {/* Mobile Overlay */}
      {sidebarOpen && (
        <div
          className="lg:hidden fixed inset-0 bg-black/50 z-30"
          onClick={toggleSidebar}
        />
      )}

      {/* Main Content */}
      <main
        className={`transition-all duration-300 ${
          sidebarOpen ? 'lg:ml-64' : 'lg:ml-20'
        }`}
      >
        <div className="min-h-screen">
          {children}
        </div>
      </main>
    </div>
  );
}
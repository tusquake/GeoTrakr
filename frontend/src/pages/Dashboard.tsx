import { useQuery } from '@tanstack/react-query';
import { format } from 'date-fns';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import { Activity, Map as MapIcon, Package, TrendingUp, Users } from 'lucide-react';
import { useEffect, useState } from 'react';
import { Circle, MapContainer, Marker, Polygon, Popup, TileLayer } from 'react-leaflet';
import { toast } from 'sonner';
import { useWebSocket } from '../hooks/useWebSocket';
import { assetsApi, eventsApi, geofencesApi, type Asset, type Geofence } from '../services/api';
import { useUIStore } from '../store/useStore';

// Fix Leaflet default marker icon
delete (L.Icon.Default.prototype as any)._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

const StatCard = ({ icon: Icon, title, value, trend, color }: any) => (
  <div className="bg-card border border-border rounded-lg p-6 hover:shadow-lg transition-all">
    <div className="flex items-center justify-between">
      <div>
        <p className="text-sm text-muted-foreground mb-1">{title}</p>
        <h3 className="text-3xl font-bold">{value}</h3>
        {trend && (
          <p className="text-xs text-green-500 mt-2 flex items-center gap-1">
            <TrendingUp className="w-3 h-3" />
            {trend}
          </p>
        )}
      </div>
      <div className={`p-3 rounded-full ${color}`}>
        <Icon className="w-6 h-6" />
      </div>
    </div>
  </div>
);

export default function Dashboard() {
  const [mapKey, setMapKey] = useState(0);
  const [lastUpdateTime, setLastUpdateTime] = useState(new Date());

  const {
    isConnected,
    locationUpdates,
    geofenceEvents
  } = useWebSocket();

  const { selectedAsset, selectAsset } = useUIStore();

  const { data: assetsData, refetch: refetchAssets } = useQuery({
    queryKey: ['assets'],
    queryFn: async () => {
      const response = await assetsApi.getAll();
      return response.data.data;
    },
  });

  // Handle real-time location updates
  useEffect(() => {
    if (locationUpdates.length > 0) {
      const latestUpdate = locationUpdates[locationUpdates.length - 1];
      console.log('🔄 New location update:', latestUpdate);

      // Refetch assets to get latest positions from backend
      refetchAssets();
      
      // Update last update time
      setLastUpdateTime(new Date());
      
      // Force map re-render to show new position
      setMapKey(prev => prev + 1);

      // Show notification for location update
      toast.info(`📍 ${latestUpdate.assetName} location updated`, {
        description: `Lat: ${latestUpdate.latitude.toFixed(4)}, Lon: ${latestUpdate.longitude.toFixed(4)}`,
        duration: 3000,
      });
    }
  }, [locationUpdates, refetchAssets]);

  // Handle real-time geofence events with notifications
  useEffect(() => {
    if (geofenceEvents.length > 0) {
      const latestEvent = geofenceEvents[geofenceEvents.length - 1];
      console.log('🚨 New geofence event:', latestEvent);

      if (latestEvent.eventType === 'ENTER') {
        toast.success(`✅ ${latestEvent.assetName} entered ${latestEvent.geofenceName}`, {
          description: `at ${format(new Date(latestEvent.timestamp), 'HH:mm:ss')}`,
          duration: 5000,
        });
      } else {
        toast.info(`⬅️ ${latestEvent.assetName} exited ${latestEvent.geofenceName}`, {
          description: `at ${format(new Date(latestEvent.timestamp), 'HH:mm:ss')}`,
          duration: 5000,
        });
      }

      // Vibrate on mobile if supported
      if ('vibrate' in navigator) {
        navigator.vibrate(200);
      }
    }
  }, [geofenceEvents]);

  const { data: geofencesData } = useQuery({
    queryKey: ['geofences'],
    queryFn: async () => {
      const response = await geofencesApi.getAll();
      return response.data.data;
    },
  });

  const { data: statsData } = useQuery({
    queryKey: ['statistics'],
    queryFn: async () => {
      const end = new Date().toISOString();
      const start = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString();
      const response = await eventsApi.getStatistics(start, end);
      return response.data.data;
    },
  });

  const assets = assetsData || [];
  const geofences = geofencesData || [];
  const stats = statsData || { totalEntries: 0, totalExits: 0, totalEvents: 0 };

  const activeAssets = assets.filter((a: Asset) => a.active);
  const defaultCenter: [number, number] = [22.5726, 88.3639];

  return (
    <div className="p-6 space-y-6">

      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Dashboard</h1>
          <div className="flex items-center gap-2">
            <p className="text-muted-foreground">Real-time geofencing monitoring</p>
            <div className={`flex items-center gap-1 text-xs font-semibold px-2 py-1 rounded-full ${
              isConnected 
                ? 'text-green-600 bg-green-100 dark:bg-green-900/30' 
                : 'text-red-600 bg-red-100 dark:bg-red-900/30'
            }`}>
              <div className={`w-2 h-2 rounded-full ${
                isConnected ? 'bg-green-500 animate-pulse' : 'bg-red-500'
              }`} />
              {isConnected ? 'Live' : 'Disconnected'}
            </div>
          </div>
        </div>
        <div className="text-sm text-muted-foreground">
          <div>Last updated: {format(lastUpdateTime, 'MMM dd, yyyy HH:mm:ss')}</div>
          {locationUpdates.length > 0 && (
            <div className="text-xs text-green-600 mt-1">
              {locationUpdates.length} location updates received
            </div>
          )}
        </div>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          icon={Users}
          title="Active Assets"
          value={activeAssets.length}
          trend="+12% from last week"
          color="bg-blue-500/10 text-blue-500"
        />
        <StatCard
          icon={MapIcon}
          title="Geofences"
          value={geofences.length}
          color="bg-purple-500/10 text-purple-500"
        />
        <StatCard
          icon={Activity}
          title="Events (7d)"
          value={stats.totalEvents}
          trend="+8% from last week"
          color="bg-green-500/10 text-green-500"
        />
        <StatCard
          icon={Package}
          title="Entries (7d)"
          value={stats.totalEntries}
          color="bg-orange-500/10 text-orange-500"
        />
      </div>

      {/* Map and Assets Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Map */}
        <div className="lg:col-span-2 bg-card border border-border rounded-lg overflow-hidden h-[600px]">
          <MapContainer
            key={mapKey}
            center={defaultCenter}
            zoom={13}
            className="h-full w-full"
          >
            <TileLayer
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
            />

            {/* Render Geofences */}
            {geofences.map((geofence: Geofence) => {
              if (geofence.type === 'CIRCULAR' && geofence.centerLatitude && geofence.centerLongitude && geofence.radius) {
                return (
                  <Circle
                    key={geofence.id}
                    center={[geofence.centerLatitude, geofence.centerLongitude]}
                    radius={geofence.radius}
                    pathOptions={{
                      color: '#3b82f6',
                      fillColor: '#3b82f6',
                      fillOpacity: 0.2,
                    }}
                  >
                    <Popup>
                      <div className="text-sm">
                        <h3 className="font-bold">{geofence.name}</h3>
                        <p className="text-xs text-muted-foreground">{geofence.description}</p>
                        <p className="text-xs mt-1">Radius: {geofence.radius}m</p>
                      </div>
                    </Popup>
                  </Circle>
                );
              }

              if (geofence.type === 'POLYGONAL' && geofence.polygonCoordinates) {
                try {
                  const coords = JSON.parse(geofence.polygonCoordinates);
                  return (
                    <Polygon
                      key={geofence.id}
                      positions={coords.map((c: number[]) => [c[0], c[1]])}
                      pathOptions={{
                        color: '#8b5cf6',
                        fillColor: '#8b5cf6',
                        fillOpacity: 0.2,
                      }}
                    >
                      <Popup>
                        <div className="text-sm">
                          <h3 className="font-bold">{geofence.name}</h3>
                          <p className="text-xs text-muted-foreground">{geofence.description}</p>
                        </div>
                      </Popup>
                    </Polygon>
                  );
                } catch (e) {
                  console.error('Invalid polygon coordinates', e);
                }
              }
              return null;
            })}

            {/* Render Assets */}
            {activeAssets.map((asset: Asset) => {
              if (asset.currentLatitude && asset.currentLongitude) {
                const iconColor = selectedAsset === asset.id ? '#ef4444' : '#10b981';
                const customIcon = L.divIcon({
                  className: 'custom-marker',
                  html: `<div style="background-color: ${iconColor}; width: 24px; height: 24px; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
                  iconSize: [24, 24],
                  iconAnchor: [12, 12],
                });

                return (
                  <Marker
                    key={asset.id}
                    position={[asset.currentLatitude, asset.currentLongitude]}
                    icon={customIcon}
                    eventHandlers={{
                      click: () => selectAsset(asset.id),
                    }}
                  >
                    <Popup>
                      <div className="text-sm">
                        <h3 className="font-bold">{asset.name}</h3>
                        <p className="text-xs text-muted-foreground">{asset.type}</p>
                        <p className="text-xs mt-1">{asset.description}</p>
                        {asset.lastUpdate && (
                          <p className="text-xs mt-1 text-green-500">
                            Updated: {format(new Date(asset.lastUpdate), 'HH:mm:ss')}
                          </p>
                        )}
                        <div className="text-xs mt-2 font-mono text-muted-foreground">
                          {asset.currentLatitude.toFixed(6)}, {asset.currentLongitude.toFixed(6)}
                        </div>
                      </div>
                    </Popup>
                  </Marker>
                );
              }
              return null;
            })}
          </MapContainer>
        </div>

        {/* Assets List */}
        <div className="bg-card border border-border rounded-lg p-4">
          <h2 className="text-xl font-bold mb-4">Active Assets</h2>
          <div className="space-y-2 max-h-[550px] overflow-y-auto scrollbar-hide">
            {activeAssets.map((asset: Asset) => (
              <div
                key={asset.id}
                onClick={() => selectAsset(asset.id)}
                className={`p-3 rounded-lg cursor-pointer transition-all ${
                  selectedAsset === asset.id
                    ? 'bg-primary/20 border border-primary'
                    : 'bg-muted hover:bg-muted/80'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <h3 className="font-semibold text-sm">{asset.name}</h3>
                    <p className="text-xs text-muted-foreground">{asset.type}</p>
                    {asset.lastUpdate && (
                      <p className="text-xs text-green-500 mt-1 flex items-center gap-1">
                        <span className="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse"></span>
                        {format(new Date(asset.lastUpdate), 'HH:mm:ss')}
                      </p>
                    )}
                  </div>
                  <div className={`w-2 h-2 rounded-full ${
                    asset.currentLatitude && asset.currentLongitude
                      ? 'bg-green-500 animate-pulse'
                      : 'bg-gray-500'
                  }`} />
                </div>
                {asset.currentLatitude && asset.currentLongitude && (
                  <div className="mt-2 text-xs text-muted-foreground font-mono">
                    {asset.currentLatitude.toFixed(4)}, {asset.currentLongitude.toFixed(4)}
                  </div>
                )}
              </div>
            ))}
            {activeAssets.length === 0 && (
              <div className="text-center text-muted-foreground py-8">
                No active assets
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Recent Events Section */}
      {geofenceEvents.length > 0 && (
        <div className="bg-card border border-border rounded-lg p-6">
          <h2 className="text-xl font-bold mb-4">Recent Geofence Events</h2>
          <div className="space-y-2 max-h-[300px] overflow-y-auto">
            {geofenceEvents.slice().reverse().slice(0, 10).map((event, index) => (
              <div
                key={`${event.eventId}-${index}`}
                className={`p-3 rounded-lg border-l-4 ${
                  event.eventType === 'ENTER'
                    ? 'bg-green-50 dark:bg-green-900/20 border-green-500'
                    : 'bg-orange-50 dark:bg-orange-900/20 border-orange-500'
                }`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex-1">
                    <p className="font-semibold text-sm">
                      {event.eventType === 'ENTER' ? '✅ Entered' : '⬅️ Exited'}: {event.geofenceName}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      Asset: {event.assetName}
                    </p>
                  </div>
                  <div className="text-xs text-muted-foreground">
                    {format(new Date(event.timestamp), 'HH:mm:ss')}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
import { useEffect, useRef, useState } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

interface LocationUpdate {
  assetId: number;
  assetName: string;
  assetType: string;
  latitude: number;
  longitude: number;
  timestamp: string;
  speed?: number;
  heading?: number;
  satellites?: number;
  accuracy?: number;
}

interface GeofenceEventMessage {
  eventId: number;
  assetId: number;
  assetName: string;
  geofenceId: number;
  geofenceName: string;
  eventType: 'ENTER' | 'EXIT';
  latitude: number;
  longitude: number;
  timestamp: string;
  message: string;
}

interface UseWebSocketReturn {
  isConnected: boolean;
  locationUpdates: LocationUpdate[];
  geofenceEvents: GeofenceEventMessage[];
  sendLocation: (location: any) => void;
  disconnect: () => void;
}

export const useWebSocket = (): UseWebSocketReturn => {
  const [isConnected, setIsConnected] = useState(false);
  const [locationUpdates, setLocationUpdates] = useState<LocationUpdate[]>([]);
  const [geofenceEvents, setGeofenceEvents] = useState<GeofenceEventMessage[]>([]);

  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(
        `${import.meta.env.VITE_API_URL.replace('/api', '')}/ws/location`
      ),

      connectHeaders: {
      },

      debug: (str) => {
        console.log('STOMP Debug:', str);
      },

      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    client.onConnect = () => {
      console.log('WebSocket connected....');
      setIsConnected(true);

      client.subscribe('/topic/location/all', (message) => {
        try {
          const update: LocationUpdate = JSON.parse(message.body);
          console.log('📍 Location update received:', update);

          setLocationUpdates((prev) => [...prev.slice(-99), update]);
        } catch (error) {
          console.error('Error parsing location update:', error);
        }
      });

      client.subscribe('/topic/geofence/events', (message) => {
        try {
          const event: GeofenceEventMessage = JSON.parse(message.body);
          console.log('🚨 Geofence event received:', event);

          setGeofenceEvents((prev) => [...prev.slice(-99), event]);
        } catch (error) {
          console.error('Error parsing geofence event:', error);
        }
      });

      client.subscribe('/topic/gps/status', (message) => {
        try {
          const status = JSON.parse(message.body);
          console.log('📡 GPS status update:', status);
        } catch (error) {
          console.error('Error parsing GPS status:', error);
        }
      });
    };

    client.onStompError = (frame) => {
      console.error('STOMP error:', frame.headers['message']);
      console.error('Error details:', frame.body);
      setIsConnected(false);
    };

    client.onWebSocketError = (event) => {
      console.error('WebSocket error:', event);
      setIsConnected(false);
    };

    client.onDisconnect = () => {
      console.log('WebSocket disconnected');
      setIsConnected(false);
    };

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, []);

  const sendLocation = (location: any) => {
    if (clientRef.current && isConnected) {
      clientRef.current.publish({
        destination: '/app/location/update',
        body: JSON.stringify(location),
      });
    } else {
      console.warn('WebSocket not connected, cannot send location');
    }
  };

  const disconnect = () => {
    if (clientRef.current) {
      clientRef.current.deactivate();
    }
  };

  return {
    isConnected,
    locationUpdates,
    geofenceEvents,
    sendLocation,
    disconnect,
  };
};

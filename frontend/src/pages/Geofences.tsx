import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Plus, Edit, Trash2, MapPin, Radius } from 'lucide-react';
import { geofencesApi, type Geofence } from '../services/api';
import { toast } from 'sonner';

export default function Geofences() {
  const [showModal, setShowModal] = useState(false);
  const [editingGeofence, setEditingGeofence] = useState<Geofence | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    type: 'CIRCULAR' as Geofence['type'],
    centerLatitude: 22.5726,
    centerLongitude: 88.3639,
    radius: 500,
    polygonCoordinates: '',
    alertType: 'BOTH' as Geofence['alertType'],
  });

  const queryClient = useQueryClient();

  const { data: geofencesData, isLoading } = useQuery({
    queryKey: ['geofences'],
    queryFn: async () => {
      const response = await geofencesApi.getAll();
      return response.data.data;
    },
  });

  const createMutation = useMutation({
    mutationFn: geofencesApi.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['geofences'] });
      toast.success('Geofence created successfully!');
      handleCloseModal();
    },
    onError: () => {
      toast.error('Failed to create geofence');
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Partial<Geofence> }) =>
      geofencesApi.update(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['geofences'] });
      toast.success('Geofence updated successfully!');
      handleCloseModal();
    },
    onError: () => {
      toast.error('Failed to update geofence');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: geofencesApi.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['geofences'] });
      toast.success('Geofence deleted successfully!');
    },
    onError: () => {
      toast.error('Failed to delete geofence');
    },
  });

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingGeofence(null);
    setFormData({
      name: '',
      description: '',
      type: 'CIRCULAR',
      centerLatitude: 22.5726,
      centerLongitude: 88.3639,
      radius: 500,
      polygonCoordinates: '',
      alertType: 'BOTH',
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (editingGeofence) {
      updateMutation.mutate({ id: editingGeofence.id, data: formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleEdit = (geofence: Geofence) => {
    setEditingGeofence(geofence);
    setFormData({
      name: geofence.name,
      description: geofence.description || '',
      type: geofence.type,
      centerLatitude: geofence.centerLatitude || 22.5726,
      centerLongitude: geofence.centerLongitude || 88.3639,
      radius: geofence.radius || 500,
      polygonCoordinates: geofence.polygonCoordinates || '',
      alertType: geofence.alertType,
    });
    setShowModal(true);
  };

  const geofences = geofencesData || [];

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Geofences</h1>
          <p className="text-muted-foreground">Define and manage geofenced zones</p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="flex items-center gap-2 bg-primary text-primary-foreground px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors"
        >
          <Plus className="w-4 h-4" />
          Add Geofence
        </button>
      </div>

      {/* Geofences Grid */}
      {isLoading ? (
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {geofences.map((geofence: Geofence) => (
            <div
              key={geofence.id}
              className="bg-card border border-border rounded-lg p-4 hover:shadow-lg transition-all"
            >
              <div className="flex items-start justify-between mb-3">
                <div className="flex items-center gap-3">
                  <div className={`p-2 rounded-lg ${
                    geofence.type === 'CIRCULAR'
                      ? 'bg-blue-500/10 text-blue-500'
                      : 'bg-purple-500/10 text-purple-500'
                  }`}>
                    {geofence.type === 'CIRCULAR' ? (
                      <Radius className="w-5 h-5" />
                    ) : (
                      <MapPin className="w-5 h-5" />
                    )}
                  </div>
                  <div>
                    <h3 className="font-semibold">{geofence.name}</h3>
                    <p className="text-xs text-muted-foreground">{geofence.type}</p>
                  </div>
                </div>
                <div className={`w-2 h-2 rounded-full ${
                  geofence.active ? 'bg-green-500' : 'bg-gray-500'
                }`} />
              </div>

              {geofence.description && (
                <p className="text-sm text-muted-foreground mb-3 line-clamp-2">
                  {geofence.description}
                </p>
              )}

              <div className="space-y-2 mb-3">
                {geofence.type === 'CIRCULAR' && (
                  <>
                    <div className="bg-muted rounded p-2">
                      <p className="text-xs text-muted-foreground">Center</p>
                      <p className="text-xs font-mono">
                        {geofence.centerLatitude?.toFixed(4)}, {geofence.centerLongitude?.toFixed(4)}
                      </p>
                    </div>
                    <div className="bg-muted rounded p-2">
                      <p className="text-xs text-muted-foreground">Radius</p>
                      <p className="text-xs font-semibold">{geofence.radius} meters</p>
                    </div>
                  </>
                )}

                <div className="bg-muted rounded p-2">
                  <p className="text-xs text-muted-foreground">Alert Type</p>
                  <p className="text-xs font-semibold">{geofence.alertType}</p>
                </div>
              </div>

              <div className="flex gap-2">
                <button
                  onClick={() => handleEdit(geofence)}
                  className="flex-1 flex items-center justify-center gap-1 bg-secondary text-secondary-foreground px-3 py-2 rounded text-sm hover:bg-secondary/80 transition-colors"
                >
                  <Edit className="w-3 h-3" />
                  Edit
                </button>
                <button
                  onClick={() => {
                    if (confirm('Are you sure you want to delete this geofence?')) {
                      deleteMutation.mutate(geofence.id);
                    }
                  }}
                  className="flex items-center justify-center gap-1 bg-destructive/10 text-destructive px-3 py-2 rounded text-sm hover:bg-destructive/20 transition-colors"
                >
                  <Trash2 className="w-3 h-3" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {geofences.length === 0 && !isLoading && (
        <div className="text-center py-16">
          <MapPin className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
          <h3 className="text-xl font-semibold mb-2">No geofences yet</h3>
          <p className="text-muted-foreground mb-4">Create your first geofence zone</p>
          <button
            onClick={() => setShowModal(true)}
            className="inline-flex items-center gap-2 bg-primary text-primary-foreground px-4 py-2 rounded-lg hover:bg-primary/90"
          >
            <Plus className="w-4 h-4" />
            Add Geofence
          </button>
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4 overflow-y-auto">
          <div className="bg-card border border-border rounded-lg p-6 w-full max-w-2xl my-8">
            <h2 className="text-2xl font-bold mb-4">
              {editingGeofence ? 'Edit Geofence' : 'Create Geofence'}
            </h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-2">Name</label>
                  <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="w-full bg-input border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium mb-2">Type</label>
                  <select
                    value={formData.type}
                    onChange={(e) => setFormData({ ...formData, type: e.target.value as Geofence['type'] })}
                    className="w-full bg-input border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
                  >
                    <option value="CIRCULAR">Circular</option>
                    <option value="POLYGONAL">Polygonal</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium mb-2">Description</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full bg-input border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
                  rows={2}
                />
              </div>

              {formData.type === 'CIRCULAR' && (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div>
                    <label className="block text-sm font-medium mb-2">Latitude</label>
                    <input
                      type="number"
                      step="0.0001"
                      value={formData.centerLatitude}
                      onChange={(e) => setFormData({ ...formData, centerLatitude: parseFloat(e.target.value) })}
                      className="w-full bg-input border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-2">Longitude</label>
                    <input
                      type="number"
                      step="0.0001"
                      value={formData.centerLongitude}
                      onChange={(e) => setFormData({ ...formData, centerLongitude: parseFloat(e.target.value) })}
                      className="w-full bg-input border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-2">Radius (m)</label>
                    <input
                      type="number"
                      value={formData.radius}
                      onChange={(e) => setFormData({ ...formData, radius: parseInt(e.target.value) })}
                      className="w-full bg-input border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
                      required
                      min="1"
                    />
                  </div>
                </div>
              )}

              {formData.type === 'POLYGONAL' && (
                <div>
                  <label className="block text-sm font-medium mb-2">
                    Polygon Coordinates (JSON array)
                  </label>
                  <textarea
                    value={formData.polygonCoordinates}
                    onChange={(e) => setFormData({ ...formData, polygonCoordinates: e.target.value })}
                    className="w-full bg-input border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary font-mono text-sm"
                    rows={3}
                    placeholder='[[22.5700,88.3600],[22.5750,88.3600],[22.5750,88.3680],[22.5700,88.3680]]'
                    required
                  />
                  <p className="text-xs text-muted-foreground mt-1">
                    Format: [[lat,lon],[lat,lon],...]
                  </p>
                </div>
              )}

              <div>
                <label className="block text-sm font-medium mb-2">Alert Type</label>
                <select
                  value={formData.alertType}
                  onChange={(e) => setFormData({ ...formData, alertType: e.target.value as Geofence['alertType'] })}
                  className="w-full bg-input border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
                >
                  <option value="ENTRY">Entry Only</option>
                  <option value="EXIT">Exit Only</option>
                  <option value="BOTH">Both Entry & Exit</option>
                </select>
              </div>

              <div className="flex gap-2 pt-4">
                <button
                  type="button"
                  onClick={handleCloseModal}
                  className="flex-1 bg-secondary text-secondary-foreground px-4 py-2 rounded hover:bg-secondary/80"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="flex-1 bg-primary text-primary-foreground px-4 py-2 rounded hover:bg-primary/90"
                  disabled={createMutation.isPending || updateMutation.isPending}
                >
                  {createMutation.isPending || updateMutation.isPending ? 'Saving...' : 'Save'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
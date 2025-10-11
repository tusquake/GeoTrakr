import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Car, Edit, Navigation, Package, Plus, Smartphone, Trash2, User } from 'lucide-react';
import { useState } from 'react';
import { toast } from 'sonner';
import { assetsApi, type Asset } from '../services/api';

const AssetTypeIcon = ({ type }: { type: string }) => {
    switch (type) {
        case 'VEHICLE': return <Car className="w-5 h-5" />;
        case 'PERSON': return <User className="w-5 h-5" />;
        case 'DEVICE': return <Smartphone className="w-5 h-5" />;
        case 'PACKAGE': return <Package className="w-5 h-5" />;
        default: return <Navigation className="w-5 h-5" />;
    }
};

export default function Assets() {
    const [showModal, setShowModal] = useState(false);
    const [editingAsset, setEditingAsset] = useState<Asset | null>(null);
    const [formData, setFormData] = useState({
        name: '',
        type: 'VEHICLE' as Asset['type'],
        description: '',
    });

    const queryClient = useQueryClient();

    const { data: assetsData, isLoading } = useQuery({
        queryKey: ['assets'],
        queryFn: async () => {
            const response = await assetsApi.getAll();
            return response.data.data;
        },
    });

    const createMutation = useMutation({
        mutationFn: assetsApi.create,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['assets'] });
            toast.success('Asset created successfully!');
            handleCloseModal();
        },
        onError: () => {
            toast.error('Failed to create asset');
        },
    });

    const updateMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: Partial<Asset> }) =>
            assetsApi.update(id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['assets'] });
            toast.success('Asset updated successfully!');
            handleCloseModal();
        },
        onError: () => {
            toast.error('Failed to update asset');
        },
    });

    const deleteMutation = useMutation({
        mutationFn: assetsApi.delete,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['assets'] });
            toast.success('Asset deleted successfully!');
        },
        onError: () => {
            toast.error('Failed to delete asset');
        },
    });

    const handleCloseModal = () => {
        setShowModal(false);
        setEditingAsset(null);
        setFormData({ name: '', type: 'VEHICLE', description: '' });
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (editingAsset) {
            updateMutation.mutate({ id: editingAsset.id, data: formData });
        } else {
            createMutation.mutate(formData);
        }
    };

    const handleEdit = (asset: Asset) => {
        setEditingAsset(asset);
        setFormData({
            name: asset.name,
            type: asset.type,
            description: asset.description || '',
        });
        setShowModal(true);
    };

    const assets = assetsData || [];

    return (
        <div className="p-6 space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold">Assets</h1>
                    <p className="text-muted-foreground">Manage your tracked assets</p>
                </div>
                <button
                    onClick={() => setShowModal(true)}
                    className="flex items-center gap-2 bg-primary text-primary-foreground px-4 py-2 rounded-lg hover:bg-primary/90 transition-colors"
                >
                    <Plus className="w-4 h-4" />
                    Add Asset
                </button>
            </div>

            {/* Assets Grid */}
            {isLoading ? (
                <div className="flex items-center justify-center h-64">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                    {assets.map((asset: Asset) => (
                        <div
                            key={asset.id}
                            className="bg-card border border-border rounded-lg p-4 hover:shadow-lg transition-all"
                        >
                            <div className="flex items-start justify-between mb-3">
                                <div className="flex items-center gap-3">
                                    <div className="p-2 bg-primary/10 rounded-lg text-primary">
                                        <AssetTypeIcon type={asset.type} />
                                    </div>
                                    <div>
                                        <h3 className="font-semibold">{asset.name}</h3>
                                        <p className="text-xs text-muted-foreground">{asset.type}</p>
                                    </div>
                                </div>
                                <div className={`w-2 h-2 rounded-full ${asset.active ? 'bg-green-500' : 'bg-gray-500'
                                    }`} />
                            </div>

                            {asset.description && (
                                <p className="text-sm text-muted-foreground mb-3 line-clamp-2">
                                    {asset.description}
                                </p>
                            )}

                            {asset.currentLatitude && asset.currentLongitude && (
                                <div className="bg-muted rounded p-2 mb-3">
                                    <p className="text-xs text-muted-foreground">Current Location</p>
                                    <p className="text-xs font-mono">
                                        {asset.currentLatitude.toFixed(4)}, {asset.currentLongitude.toFixed(4)}
                                    </p>
                                </div>
                            )}

                            <div className="flex gap-2">
                                <button
                                    onClick={() => handleEdit(asset)}
                                    className="flex-1 flex items-center justify-center gap-1 bg-secondary text-secondary-foreground px-3 py-2 rounded text-sm hover:bg-secondary/80 transition-colors"
                                >
                                    <Edit className="w-3 h-3" />
                                    Edit
                                </button>
                                <button
                                    onClick={() => {
                                        if (confirm('Are you sure you want to delete this asset?')) {
                                            deleteMutation.mutate(asset.id);
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

            {assets.length === 0 && !isLoading && (
                <div className="text-center py-16">
                    <Package className="w-16 h-16 text-muted-foreground mx-auto mb-4" />
                    <h3 className="text-xl font-semibold mb-2">No assets yet</h3>
                    <p className="text-muted-foreground mb-4">Get started by creating your first asset</p>
                    <button
                        onClick={() => setShowModal(true)}
                        className="inline-flex items-center gap-2 bg-primary text-primary-foreground px-4 py-2 rounded-lg hover:bg-primary/90"
                    >
                        <Plus className="w-4 h-4" />
                        Add Asset
                    </button>
                </div>
            )}

            {/* Modal */}
            {showModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                    <div className="bg-card border border-border rounded-lg p-6 w-full max-w-md">
                        <h2 className="text-2xl font-bold mb-4">
                            {editingAsset ? 'Edit Asset' : 'Create Asset'}
                        </h2>
                        <form onSubmit={handleSubmit} className="space-y-4">
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
                                    onChange={(e) => setFormData({ ...formData, type: e.target.value as Asset['type'] })}
                                    className="w-full bg-input border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
                                >
                                    <option value="VEHICLE">Vehicle</option>
                                    <option value="PERSON">Person</option>
                                    <option value="DEVICE">Device</option>
                                    <option value="PACKAGE">Package</option>
                                </select>
                            </div>

                            <div>
                                <label className="block text-sm font-medium mb-2">Description</label>
                                <textarea
                                    value={formData.description}
                                    onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                                    className="w-full bg-input border border-border rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-primary"
                                    rows={3}
                                />
                            </div>

                            <div className="flex gap-2">
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
import { useQuery } from '@tanstack/react-query';
import { format, subDays } from 'date-fns';
import { Activity, ArrowDownCircle, ArrowUpCircle } from 'lucide-react';
import { useState, useMemo } from 'react';
import { Bar, BarChart, CartesianGrid, Cell, Line, LineChart, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { eventsApi, type GeofenceEvent } from '../services/api';

const COLORS = ['#3b82f6', '#8b5cf6', '#10b981', '#f59e0b', '#ef4444'];

export default function Events() {
    const [dateRange, setDateRange] = useState(7);

    // Memoize dates to prevent unnecessary re-renders
    const { startDate, endDate } = useMemo(() => ({
        startDate: subDays(new Date(), dateRange).toISOString(),
        endDate: new Date().toISOString()
    }), [dateRange]);

    const { data: eventsData } = useQuery({
        queryKey: ['events', startDate, endDate],
        queryFn: async () => {
            const response = await eventsApi.getByDateRange(startDate, endDate);
            console.log('Events response:', response.data.data);
            return response.data.data;
        },
    });

    const { data: statsData } = useQuery({
        queryKey: ['statistics', startDate, endDate],
        queryFn: async () => {
            const response = await eventsApi.getStatistics(startDate, endDate);
            return response.data.data;
        },
    });

    const events: GeofenceEvent[] = eventsData || [];
    const stats = statsData || { totalEntries: 0, totalExits: 0, totalEvents: 0, mostActiveAssets: [] };

    // Process data for charts - memoized to prevent unnecessary recalculations
    const { chartData, pieData, topAssets } = useMemo(() => {
        const dailyEvents = events.reduce((acc: any, event) => {
            const date = format(new Date(event.timestamp), 'MMM dd');
            if (!acc[date]) {
                acc[date] = { date, entries: 0, exits: 0 };
            }
            if (event.eventType === 'ENTER') {
                acc[date].entries++;
            } else {
                acc[date].exits++;
            }
            return acc;
        }, {});

        const processedChartData = Object.values(dailyEvents);

        const processedPieData = [
            { name: 'Entries', value: stats.totalEntries },
            { name: 'Exits', value: stats.totalExits },
        ];

        const processedTopAssets = stats.mostActiveAssets.slice(0, 5).map((item: any) => ({
            id: item[0],
            count: item[1],
        }));

        return {
            chartData: processedChartData,
            pieData: processedPieData,
            topAssets: processedTopAssets
        };
    }, [events, stats]);

    return (
        <div className="p-6 space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-3xl font-bold">Events & Analytics</h1>
                    <p className="text-muted-foreground">Geofence event history and insights</p>
                </div>
                <div className="flex gap-2">
                    {[7, 14, 30].map((days) => (
                        <button
                            key={days}
                            onClick={() => setDateRange(days)}
                            className={`px-4 py-2 rounded-lg transition-colors ${dateRange === days
                                ? 'bg-primary text-primary-foreground'
                                : 'bg-secondary text-secondary-foreground hover:bg-secondary/80'
                                }`}
                        >
                            {days}D
                        </button>
                    ))}
                </div>
            </div>

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="bg-card border border-border rounded-lg p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-muted-foreground mb-1">Total Events</p>
                            <h3 className="text-3xl font-bold">{stats.totalEvents}</h3>
                        </div>
                        <div className="p-3 rounded-full bg-blue-500/10 text-blue-500">
                            <Activity className="w-6 h-6" />
                        </div>
                    </div>
                </div>

                <div className="bg-card border border-border rounded-lg p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-muted-foreground mb-1">Total Entries</p>
                            <h3 className="text-3xl font-bold text-green-500">{stats.totalEntries}</h3>
                        </div>
                        <div className="p-3 rounded-full bg-green-500/10 text-green-500">
                            <ArrowUpCircle className="w-6 h-6" />
                        </div>
                    </div>
                </div>

                <div className="bg-card border border-border rounded-lg p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm text-muted-foreground mb-1">Total Exits</p>
                            <h3 className="text-3xl font-bold text-orange-500">{stats.totalExits}</h3>
                        </div>
                        <div className="p-3 rounded-full bg-orange-500/10 text-orange-500">
                            <ArrowDownCircle className="w-6 h-6" />
                        </div>
                    </div>
                </div>
            </div>

            {/* Charts Grid */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                {/* Daily Activity Chart */}
                <div className="bg-card border border-border rounded-lg p-6">
                    <h2 className="text-xl font-bold mb-4">Daily Activity</h2>
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={chartData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                            <XAxis dataKey="date" stroke="#94a3b8" />
                            <YAxis stroke="#94a3b8" />
                            <Tooltip
                                contentStyle={{
                                    backgroundColor: '#1e293b',
                                    border: '1px solid #334155',
                                    borderRadius: '8px',
                                }}
                            />
                            <Bar dataKey="entries" fill="#10b981" name="Entries" />
                            <Bar dataKey="exits" fill="#f59e0b" name="Exits" />
                        </BarChart>
                    </ResponsiveContainer>
                </div>

                {/* Entry/Exit Distribution */}
                <div className="bg-card border border-border rounded-lg p-6">
                    <h2 className="text-xl font-bold mb-4">Event Distribution</h2>
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={pieData}
                                cx="50%"
                                cy="50%"
                                labelLine={false}
                                label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                                outerRadius={80}
                                fill="#8884d8"
                                dataKey="value"
                            >
                                {pieData.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Pie>
                            <Tooltip
                                contentStyle={{
                                    backgroundColor: '#1e293b',
                                    border: '1px solid #334155',
                                    borderRadius: '8px',
                                }}
                            />
                        </PieChart>
                    </ResponsiveContainer>
                </div>

                {/* Trend Chart */}
                <div className="bg-card border border-border rounded-lg p-6">
                    <h2 className="text-xl font-bold mb-4">Event Trends</h2>
                    <ResponsiveContainer width="100%" height={300}>
                        <LineChart data={chartData}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                            <XAxis dataKey="date" stroke="#94a3b8" />
                            <YAxis stroke="#94a3b8" />
                            <Tooltip
                                contentStyle={{
                                    backgroundColor: '#1e293b',
                                    border: '1px solid #334155',
                                    borderRadius: '8px',
                                }}
                            />
                            <Line type="monotone" dataKey="entries" stroke="#10b981" strokeWidth={2} />
                            <Line type="monotone" dataKey="exits" stroke="#f59e0b" strokeWidth={2} />
                        </LineChart>
                    </ResponsiveContainer>
                </div>

                {/* Most Active Assets */}
                <div className="bg-card border border-border rounded-lg p-6">
                    <h2 className="text-xl font-bold mb-4">Most Active Assets</h2>
                    <div className="space-y-3">
                        {topAssets.map((asset, index) => (
                            <div key={asset.id} className="flex items-center gap-3">
                                <div className={`w-8 h-8 rounded-full flex items-center justify-center font-bold ${index === 0 ? 'bg-yellow-500/20 text-yellow-500' :
                                    index === 1 ? 'bg-gray-400/20 text-gray-400' :
                                        index === 2 ? 'bg-orange-600/20 text-orange-600' :
                                            'bg-blue-500/20 text-blue-500'
                                    }`}>
                                    {index + 1}
                                </div>
                                <div className="flex-1">
                                    <div className="flex items-center justify-between mb-1">
                                        <span className="text-sm font-medium">Asset #{asset.id}</span>
                                        <span className="text-sm text-muted-foreground">{asset.count} events</span>
                                    </div>
                                    <div className="w-full bg-muted rounded-full h-2">
                                        <div
                                            className="bg-primary h-2 rounded-full transition-all"
                                            style={{ width: `${(asset.count / topAssets[0].count) * 100}%` }}
                                        />
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>

            {/* Recent Events Table */}
            <div className="bg-card border border-border rounded-lg p-6">
                <h2 className="text-xl font-bold mb-4">Recent Events</h2>
                <div className="overflow-x-auto">
                    <table className="w-full">
                        <thead>
                            <tr className="border-b border-border">
                                <th className="text-left py-3 px-4">Timestamp</th>
                                <th className="text-left py-3 px-4">Asset</th>
                                <th className="text-left py-3 px-4">Geofence</th>
                                <th className="text-left py-3 px-4">Event</th>
                                <th className="text-left py-3 px-4">Location</th>
                            </tr>
                        </thead>
                        <tbody>
                            {events.slice(0, 10).map((event) => (
                                <tr key={event.id} className="border-b border-border hover:bg-muted/50">
                                    <td className="py-3 px-4 text-sm">
                                        {format(new Date(event.timestamp), 'MMM dd, HH:mm:ss')}
                                    </td>
                                    <td className="py-3 px-4 text-sm">{event.asset.name}</td>
                                    <td className="py-3 px-4 text-sm">{event.geofence.name}</td>
                                    <td className="py-3 px-4">
                                        <span className={`inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs ${event.eventType === 'ENTER'
                                            ? 'bg-green-500/20 text-green-500'
                                            : 'bg-orange-500/20 text-orange-500'
                                            }`}>
                                            {event.eventType === 'ENTER' ? (
                                                <ArrowUpCircle className="w-3 h-3" />
                                            ) : (
                                                <ArrowDownCircle className="w-3 h-3" />
                                            )}
                                            {event.eventType}
                                        </span>
                                    </td>
                                    <td className="py-3 px-4 text-sm font-mono">
                                        {event.latitude.toFixed(4)}, {event.longitude.toFixed(4)}
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
}
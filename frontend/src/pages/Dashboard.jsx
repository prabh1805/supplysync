import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const menuItems = [
  { name: 'Overview', icon: '◻️', active: true },
  { name: 'Products', icon: '📦' },
  { name: 'Inventory', icon: '🏭' },
  { name: 'Suppliers', icon: '🤝' },
  { name: 'Orders', icon: '📋' },
  { name: 'Shipments', icon: '🚚' },
  { name: 'Team', icon: '👥' },
  { name: 'Notifications', icon: '🔔' },
  { name: 'Settings', icon: '⚙️' },
];

export default function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Sidebar */}
      <aside className="w-60 bg-white border-r border-gray-200 flex flex-col">
        <div className="p-5 border-b border-gray-100">
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 bg-indigo-600 rounded-md flex items-center justify-center">
              <span className="text-white text-xs font-bold">S</span>
            </div>
            <span className="text-sm font-bold text-gray-900">SupplySync</span>
          </div>
          <p className="text-xs text-gray-400 mt-1.5 ml-9">{localStorage.getItem('subdomain') || 'workspace'}</p>
        </div>

        <nav className="flex-1 p-3 space-y-0.5">
          {menuItems.map((item) => (
            <button
              key={item.name}
              className={`w-full flex items-center gap-3 px-3 py-2 rounded-md text-sm transition-colors ${
                item.active
                  ? 'bg-indigo-50 text-indigo-700 font-medium'
                  : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
              }`}
            >
              <span className="text-base">{item.icon}</span>
              {item.name}
            </button>
          ))}
        </nav>

        <div className="p-4 border-t border-gray-100">
          <div className="flex items-center gap-3">
            <div className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-700 flex items-center justify-center text-sm font-bold">
              {user?.fullName?.charAt(0) || 'U'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-gray-900 truncate">{user?.fullName}</p>
              <p className="text-xs text-gray-400 truncate">{user?.role?.replace('_', ' ')}</p>
            </div>
          </div>
          <button onClick={handleLogout} className="w-full mt-3 py-1.5 text-xs text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-md transition-colors">
            Sign out
          </button>
        </div>
      </aside>

      {/* Main */}
      <main className="flex-1 p-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Overview</h1>
            <p className="text-sm text-gray-500 mt-0.5">Welcome back, {user?.fullName?.split(' ')[0]}</p>
          </div>
          <button className="bg-indigo-600 text-white text-sm px-4 py-2 rounded-lg hover:bg-indigo-700 transition-colors font-medium">
            + New Order
          </button>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-4 gap-5 mb-8">
          {[
            { label: 'Total Products', value: '—', change: '+12%', up: true },
            { label: 'Active Orders', value: '—', change: '+8%', up: true },
            { label: 'In Transit', value: '—', change: '3 carriers', up: null },
            { label: 'Low Stock', value: '—', change: 'Needs attention', up: false },
          ].map((stat) => (
            <div key={stat.label} className="bg-white rounded-xl p-5 border border-gray-200">
              <p className="text-xs text-gray-500 uppercase tracking-wide font-medium">{stat.label}</p>
              <p className="text-2xl font-bold text-gray-900 mt-2">{stat.value}</p>
              <p className={`text-xs mt-1 ${stat.up === true ? 'text-emerald-600' : stat.up === false ? 'text-amber-600' : 'text-gray-400'}`}>
                {stat.change}
              </p>
            </div>
          ))}
        </div>

        {/* Recent activity */}
        <div className="grid grid-cols-2 gap-6">
          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-semibold text-gray-900">Recent Orders</h3>
              <button className="text-xs text-indigo-600 hover:text-indigo-700 font-medium">View all</button>
            </div>
            <div className="space-y-3">
              <p className="text-sm text-gray-400 text-center py-8">No orders yet. Create your first order.</p>
            </div>
          </div>

          <div className="bg-white rounded-xl border border-gray-200 p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-sm font-semibold text-gray-900">Low Stock Alerts</h3>
              <button className="text-xs text-indigo-600 hover:text-indigo-700 font-medium">View all</button>
            </div>
            <div className="space-y-3">
              <p className="text-sm text-gray-400 text-center py-8">No alerts. All stock levels are healthy.</p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}

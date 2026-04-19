import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

const menuItems = [
  { name: 'Dashboard', icon: '📊', active: true },
  { name: 'Products', icon: '📦' },
  { name: 'Inventory', icon: '🏭' },
  { name: 'Suppliers', icon: '🤝' },
  { name: 'Orders', icon: '📋' },
  { name: 'Shipments', icon: '🚚' },
  { name: 'Team', icon: '👥' },
  { name: 'Notifications', icon: '🔔' },
];

const stats = [
  { label: 'Total Products', value: '—', color: 'from-blue-500 to-blue-600' },
  { label: 'Active Orders', value: '—', color: 'from-emerald-500 to-emerald-600' },
  { label: 'Low Stock Items', value: '—', color: 'from-amber-500 to-amber-600' },
  { label: 'Pending Shipments', value: '—', color: 'from-purple-500 to-purple-600' },
];

export default function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-slate-950 flex">
      {/* Sidebar */}
      <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col">
        <div className="p-6 border-b border-slate-800">
          <h1 className="text-xl font-bold text-white">SupplySync</h1>
          <p className="text-xs text-slate-400 mt-1">{localStorage.getItem('subdomain') || 'tenant'}.supplysync.com</p>
        </div>

        <nav className="flex-1 p-4 space-y-1">
          {menuItems.map((item) => (
            <button
              key={item.name}
              className={`w-full flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm transition-colors ${
                item.active
                  ? 'bg-blue-600/20 text-blue-400'
                  : 'text-slate-400 hover:bg-slate-800 hover:text-white'
              }`}
            >
              <span>{item.icon}</span>
              {item.name}
            </button>
          ))}
        </nav>

        <div className="p-4 border-t border-slate-800">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-9 h-9 rounded-full bg-blue-600 flex items-center justify-center text-white text-sm font-bold">
              {user?.fullName?.charAt(0) || 'U'}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-white truncate">{user?.fullName}</p>
              <p className="text-xs text-slate-400 truncate">{user?.role}</p>
            </div>
          </div>
          <button
            onClick={handleLogout}
            className="w-full py-2 text-sm text-slate-400 hover:text-red-400 hover:bg-red-500/10 rounded-lg transition-colors"
          >
            Sign Out
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 p-8">
        <div className="mb-8">
          <h2 className="text-2xl font-bold text-white">Welcome back, {user?.fullName?.split(' ')[0]}</h2>
          <p className="text-slate-400 mt-1">Here's what's happening with your supply chain</p>
        </div>

        {/* Stats grid */}
        <div className="grid grid-cols-4 gap-6 mb-8">
          {stats.map((stat) => (
            <div key={stat.label} className="bg-slate-900 border border-slate-800 rounded-xl p-6">
              <p className="text-sm text-slate-400">{stat.label}</p>
              <p className={`text-3xl font-bold mt-2 bg-gradient-to-r ${stat.color} bg-clip-text text-transparent`}>
                {stat.value}
              </p>
            </div>
          ))}
        </div>

        {/* Quick actions */}
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-6">
          <h3 className="text-lg font-semibold text-white mb-4">Quick Actions</h3>
          <div className="grid grid-cols-3 gap-4">
            <button className="p-4 bg-slate-800 hover:bg-slate-700 rounded-lg text-left transition-colors">
              <span className="text-2xl">➕</span>
              <p className="text-white font-medium mt-2">Add Product</p>
              <p className="text-slate-400 text-sm">Add a new product to catalog</p>
            </button>
            <button className="p-4 bg-slate-800 hover:bg-slate-700 rounded-lg text-left transition-colors">
              <span className="text-2xl">📝</span>
              <p className="text-white font-medium mt-2">Create Order</p>
              <p className="text-slate-400 text-sm">Place a purchase order</p>
            </button>
            <button className="p-4 bg-slate-800 hover:bg-slate-700 rounded-lg text-left transition-colors">
              <span className="text-2xl">👥</span>
              <p className="text-white font-medium mt-2">Invite Team</p>
              <p className="text-slate-400 text-sm">Add members to your org</p>
            </button>
          </div>
        </div>
      </main>
    </div>
  );
}

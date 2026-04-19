import { Link } from 'react-router-dom';

export default function Landing() {
  return (
    <div className="min-h-screen bg-white">
      {/* Navbar */}
      <nav className="border-b border-gray-100 px-8 py-4 flex items-center justify-between max-w-7xl mx-auto">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 bg-indigo-600 rounded-lg flex items-center justify-center">
            <span className="text-white font-bold text-sm">S</span>
          </div>
          <span className="text-xl font-bold text-gray-900">SupplySync</span>
        </div>
        <div className="flex items-center gap-6">
          <a href="#features" className="text-sm text-gray-600 hover:text-gray-900">Features</a>
          <a href="#how" className="text-sm text-gray-600 hover:text-gray-900">How it works</a>
          <Link to="/login" className="text-sm text-gray-600 hover:text-gray-900">Sign in</Link>
          <Link to="/register" className="text-sm bg-indigo-600 text-white px-4 py-2 rounded-lg hover:bg-indigo-700 transition-colors">
            Get Started
          </Link>
        </div>
      </nav>

      {/* Hero */}
      <section className="max-w-7xl mx-auto px-8 pt-20 pb-32">
        <div className="max-w-3xl">
          <div className="inline-flex items-center gap-2 bg-indigo-50 text-indigo-700 text-sm font-medium px-3 py-1 rounded-full mb-6">
            <span className="w-2 h-2 bg-indigo-500 rounded-full"></span>
            Now with multi-tenant isolation
          </div>
          <h1 className="text-6xl font-bold text-gray-900 leading-tight tracking-tight">
            Supply chain visibility,{' '}
            <span className="text-indigo-600">simplified.</span>
          </h1>
          <p className="text-xl text-gray-500 mt-6 leading-relaxed max-w-2xl">
            Manage products, inventory, suppliers, and orders across your entire supply chain. 
            One platform for your whole team — from procurement to delivery.
          </p>
          <div className="flex items-center gap-4 mt-10">
            <Link to="/register" className="bg-indigo-600 text-white px-8 py-3.5 rounded-lg text-sm font-semibold hover:bg-indigo-700 transition-colors shadow-lg shadow-indigo-200">
              Start free trial
            </Link>
            <a href="#how" className="text-gray-600 px-6 py-3.5 rounded-lg text-sm font-semibold hover:bg-gray-50 border border-gray-200 transition-colors">
              See how it works →
            </a>
          </div>
        </div>

        {/* Dashboard preview */}
        <div className="mt-20 rounded-2xl border border-gray-200 shadow-2xl shadow-gray-200/50 overflow-hidden">
          <div className="bg-gray-50 border-b border-gray-200 px-6 py-3 flex items-center gap-2">
            <div className="w-3 h-3 rounded-full bg-red-400"></div>
            <div className="w-3 h-3 rounded-full bg-yellow-400"></div>
            <div className="w-3 h-3 rounded-full bg-green-400"></div>
          </div>
          <div className="bg-white p-8">
            <div className="grid grid-cols-4 gap-4 mb-6">
              <div className="bg-gray-50 rounded-xl p-5 border border-gray-100">
                <p className="text-xs text-gray-500 uppercase tracking-wide">Products</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">2,847</p>
                <p className="text-xs text-emerald-600 mt-1">↑ 12% this month</p>
              </div>
              <div className="bg-gray-50 rounded-xl p-5 border border-gray-100">
                <p className="text-xs text-gray-500 uppercase tracking-wide">Active Orders</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">156</p>
                <p className="text-xs text-emerald-600 mt-1">↑ 8% this week</p>
              </div>
              <div className="bg-gray-50 rounded-xl p-5 border border-gray-100">
                <p className="text-xs text-gray-500 uppercase tracking-wide">In Transit</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">43</p>
                <p className="text-xs text-gray-500 mt-1">Across 3 carriers</p>
              </div>
              <div className="bg-gray-50 rounded-xl p-5 border border-gray-100">
                <p className="text-xs text-gray-500 uppercase tracking-wide">Low Stock</p>
                <p className="text-2xl font-bold text-amber-600 mt-1">7</p>
                <p className="text-xs text-amber-600 mt-1">Needs attention</p>
              </div>
            </div>
            <div className="bg-gray-50 rounded-xl p-5 border border-gray-100 h-32 flex items-center justify-center">
              <p className="text-gray-400 text-sm">Order activity chart</p>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section id="features" className="bg-gray-50 py-24">
        <div className="max-w-7xl mx-auto px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-gray-900">Everything you need to manage your supply chain</h2>
            <p className="text-gray-500 mt-3 max-w-xl mx-auto">From product catalog to delivery tracking — all in one platform with complete data isolation per organization.</p>
          </div>
          <div className="grid grid-cols-3 gap-8">
            {[
              { icon: '📦', title: 'Product Management', desc: 'Manage your entire product catalog with SKUs, pricing, and categories.' },
              { icon: '🏭', title: 'Inventory Tracking', desc: 'Real-time stock levels across multiple warehouses with low-stock alerts.' },
              { icon: '🤝', title: 'Supplier Management', desc: 'Track suppliers, ratings, and contracts in one place.' },
              { icon: '📋', title: 'Order Management', desc: 'Purchase and sales orders with full lifecycle tracking.' },
              { icon: '🚚', title: 'Shipment Tracking', desc: 'Track deliveries from pickup to delivery with carrier integration.' },
              { icon: '🔒', title: 'Multi-Tenant Security', desc: 'Complete data isolation. Your data is yours — always.' },
            ].map((f) => (
              <div key={f.title} className="bg-white rounded-xl p-6 border border-gray-100 hover:shadow-lg hover:border-gray-200 transition-all">
                <span className="text-3xl">{f.icon}</span>
                <h3 className="text-lg font-semibold text-gray-900 mt-4">{f.title}</h3>
                <p className="text-gray-500 text-sm mt-2 leading-relaxed">{f.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* How it works */}
      <section id="how" className="py-24">
        <div className="max-w-7xl mx-auto px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold text-gray-900">Get started in 3 steps</h2>
          </div>
          <div className="grid grid-cols-3 gap-12">
            {[
              { step: '01', title: 'Create your organization', desc: 'Sign up with your company details. Your isolated workspace is created instantly.' },
              { step: '02', title: 'Add your team', desc: 'Invite warehouse managers, procurement leads, and logistics coordinators.' },
              { step: '03', title: 'Start managing', desc: 'Add products, track inventory, create orders, and monitor shipments.' },
            ].map((s) => (
              <div key={s.step} className="text-center">
                <div className="w-12 h-12 bg-indigo-100 text-indigo-600 rounded-full flex items-center justify-center text-sm font-bold mx-auto">
                  {s.step}
                </div>
                <h3 className="text-lg font-semibold text-gray-900 mt-4">{s.title}</h3>
                <p className="text-gray-500 text-sm mt-2">{s.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="bg-indigo-600 py-16">
        <div className="max-w-7xl mx-auto px-8 text-center">
          <h2 className="text-3xl font-bold text-white">Ready to streamline your supply chain?</h2>
          <p className="text-indigo-200 mt-3">Start your free trial today. No credit card required.</p>
          <Link to="/register" className="inline-block mt-8 bg-white text-indigo-600 px-8 py-3.5 rounded-lg text-sm font-semibold hover:bg-indigo-50 transition-colors">
            Get Started Free
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-gray-100 py-8">
        <div className="max-w-7xl mx-auto px-8 flex items-center justify-between">
          <p className="text-sm text-gray-400">© 2026 SupplySync. All rights reserved.</p>
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 bg-indigo-600 rounded flex items-center justify-center">
              <span className="text-white text-xs font-bold">S</span>
            </div>
            <span className="text-sm font-medium text-gray-600">SupplySync</span>
          </div>
        </div>
      </footer>
    </div>
  );
}

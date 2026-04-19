import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { onboard } from '../api/auth';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const [step, setStep] = useState(1);
  const [form, setForm] = useState({
    companyName: '', subdomain: '',
    firstName: '', lastName: '',
    email: '', password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { loginUser } = useAuth();

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleNext = (e) => {
    e.preventDefault();
    if (!form.companyName || !form.subdomain) {
      setError('Please fill in all fields');
      return;
    }
    setError('');
    setStep(2);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const res = await onboard(form);
      localStorage.setItem('tenantId', res.data.tenantId);
      localStorage.setItem('subdomain', res.data.subdomain);
      loginUser(res.data);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-blue-950 to-slate-900 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-white tracking-tight">SupplySync</h1>
          <p className="text-blue-300 mt-2">Set up your organization</p>
        </div>

        {/* Step indicator */}
        <div className="flex items-center justify-center mb-8 gap-3">
          <div className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold ${step >= 1 ? 'bg-blue-600 text-white' : 'bg-white/10 text-gray-400'}`}>1</div>
          <div className={`w-16 h-0.5 ${step >= 2 ? 'bg-blue-600' : 'bg-white/10'}`} />
          <div className={`w-10 h-10 rounded-full flex items-center justify-center text-sm font-bold ${step >= 2 ? 'bg-blue-600 text-white' : 'bg-white/10 text-gray-400'}`}>2</div>
        </div>

        <div className="bg-white/10 backdrop-blur-lg rounded-2xl p-8 shadow-2xl border border-white/10">
          {error && (
            <div className="bg-red-500/20 border border-red-500/50 text-red-200 px-4 py-3 rounded-lg mb-4 text-sm">
              {error}
            </div>
          )}

          {step === 1 && (
            <form onSubmit={handleNext} className="space-y-4">
              <h2 className="text-2xl font-semibold text-white mb-2">Company Details</h2>
              <p className="text-gray-400 text-sm mb-4">Tell us about your organization</p>
              <div>
                <label className="block text-sm font-medium text-blue-200 mb-1">Company Name</label>
                <input
                  className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="Acme Corp"
                  value={form.companyName}
                  onChange={update('companyName')}
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-blue-200 mb-1">Subdomain</label>
                <div className="flex items-center">
                  <input
                    className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-l-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="acme"
                    value={form.subdomain}
                    onChange={update('subdomain')}
                    required
                  />
                  <span className="px-4 py-3 bg-white/5 border border-l-0 border-white/10 rounded-r-lg text-gray-400 text-sm">.supplysync.com</span>
                </div>
              </div>
              <button type="submit" className="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition-colors mt-2">
                Continue
              </button>
            </form>
          )}

          {step === 2 && (
            <form onSubmit={handleSubmit} className="space-y-4">
              <h2 className="text-2xl font-semibold text-white mb-2">Admin Account</h2>
              <p className="text-gray-400 text-sm mb-4">Create the first admin for {form.companyName}</p>
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-sm font-medium text-blue-200 mb-1">First Name</label>
                  <input
                    className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="John"
                    value={form.firstName}
                    onChange={update('firstName')}
                    required
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-blue-200 mb-1">Last Name</label>
                  <input
                    className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="Doe"
                    value={form.lastName}
                    onChange={update('lastName')}
                    required
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-blue-200 mb-1">Email</label>
                <input
                  type="email"
                  className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="john@acme.com"
                  value={form.email}
                  onChange={update('email')}
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-blue-200 mb-1">Password</label>
                <input
                  type="password"
                  className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-lg text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="••••••••"
                  value={form.password}
                  onChange={update('password')}
                  required
                />
              </div>
              <div className="flex gap-3 mt-2">
                <button type="button" onClick={() => setStep(1)} className="flex-1 py-3 bg-white/10 hover:bg-white/20 text-white font-semibold rounded-lg transition-colors">
                  Back
                </button>
                <button type="submit" disabled={loading} className="flex-1 py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-lg transition-colors disabled:opacity-50">
                  {loading ? 'Creating...' : 'Create Account'}
                </button>
              </div>
            </form>
          )}

          <p className="text-center text-gray-400 mt-6 text-sm">
            Already have an account?{' '}
            <Link to="/login" className="text-blue-400 hover:text-blue-300 font-medium">
              Sign In
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}

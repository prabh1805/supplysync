import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { onboard } from '../api/auth';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const [form, setForm] = useState({
    companyName: '', subdomain: '',
    firstName: '', lastName: '',
    email: '', password: ''
  });
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { loginUser } = useAuth();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const res = await onboard(form);
      // store tenant info along with auth
      localStorage.setItem('tenantId', res.data.tenantId);
      localStorage.setItem('subdomain', res.data.subdomain);
      loginUser(res.data);
      navigate('/dashboard');
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    }
  };

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>SupplySync</h1>
        <p style={styles.subtitle}>Set up your company</p>
        {error && <p style={styles.error}>{error}</p>}
        <form onSubmit={handleSubmit}>
          <h3 style={styles.section}>Company Details</h3>
          <input style={styles.input} placeholder="Company Name" value={form.companyName} onChange={update('companyName')} required />
          <input style={styles.input} placeholder="Subdomain (e.g. acme)" value={form.subdomain} onChange={update('subdomain')} required />

          <h3 style={styles.section}>Admin Account</h3>
          <div style={styles.row}>
            <input style={styles.halfInput} placeholder="First Name" value={form.firstName} onChange={update('firstName')} required />
            <input style={styles.halfInput} placeholder="Last Name" value={form.lastName} onChange={update('lastName')} required />
          </div>
          <input style={styles.input} type="email" placeholder="Email" value={form.email} onChange={update('email')} required />
          <input style={styles.input} type="password" placeholder="Password" value={form.password} onChange={update('password')} required />

          <button style={styles.button} type="submit">Create Account</button>
        </form>
        <p style={styles.link}>
          Already have an account? <Link to="/login">Sign In</Link>
        </p>
      </div>
    </div>
  );
}

const styles = {
  container: { display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', background: '#f0f2f5' },
  card: { background: '#fff', padding: '40px', borderRadius: '8px', boxShadow: '0 2px 10px rgba(0,0,0,0.1)', width: '440px' },
  title: { margin: 0, fontSize: '28px', textAlign: 'center', color: '#1a1a2e' },
  subtitle: { textAlign: 'center', color: '#666', marginBottom: '24px' },
  section: { margin: '16px 0 8px', color: '#1a1a2e', fontSize: '14px', fontWeight: '600' },
  input: { width: '100%', padding: '12px', marginBottom: '12px', border: '1px solid #ddd', borderRadius: '6px', fontSize: '14px', boxSizing: 'border-box' },
  halfInput: { width: '48%', padding: '12px', marginBottom: '12px', border: '1px solid #ddd', borderRadius: '6px', fontSize: '14px', boxSizing: 'border-box' },
  row: { display: 'flex', justifyContent: 'space-between' },
  button: { width: '100%', padding: '12px', background: '#1a1a2e', color: '#fff', border: 'none', borderRadius: '6px', fontSize: '16px', cursor: 'pointer', marginTop: '8px' },
  error: { color: '#e74c3c', textAlign: 'center', marginBottom: '16px' },
  link: { textAlign: 'center', marginTop: '16px', color: '#666' },
};

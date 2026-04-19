import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function Dashboard() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div style={styles.container}>
      <nav style={styles.nav}>
        <h2 style={styles.logo}>SupplySync</h2>
        <div style={styles.navRight}>
          <span style={styles.userName}>{user?.fullName}</span>
          <span style={styles.role}>{user?.role}</span>
          <button style={styles.logoutBtn} onClick={handleLogout}>Logout</button>
        </div>
      </nav>
      <div style={styles.content}>
        <h1>Welcome, {user?.fullName}</h1>
        <p>Role: {user?.role}</p>
        <p>Email: {user?.email}</p>
        <div style={styles.grid}>
          <div style={styles.card}>
            <h3>Products</h3>
            <p>Manage your product catalog</p>
          </div>
          <div style={styles.card}>
            <h3>Inventory</h3>
            <p>Track stock levels</p>
          </div>
          <div style={styles.card}>
            <h3>Suppliers</h3>
            <p>Manage supplier relationships</p>
          </div>
          <div style={styles.card}>
            <h3>Orders</h3>
            <p>Purchase and sales orders</p>
          </div>
          <div style={styles.card}>
            <h3>Shipments</h3>
            <p>Track deliveries</p>
          </div>
          <div style={styles.card}>
            <h3>Notifications</h3>
            <p>Alerts and updates</p>
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: { minHeight: '100vh', background: '#f0f2f5' },
  nav: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '16px 32px', background: '#1a1a2e', color: '#fff' },
  logo: { margin: 0 },
  navRight: { display: 'flex', alignItems: 'center', gap: '16px' },
  userName: { fontWeight: 'bold' },
  role: { background: '#2d2d44', padding: '4px 12px', borderRadius: '12px', fontSize: '12px' },
  logoutBtn: { padding: '8px 16px', background: '#e74c3c', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer' },
  content: { padding: '32px' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '20px', marginTop: '24px' },
  card: { background: '#fff', padding: '24px', borderRadius: '8px', boxShadow: '0 2px 8px rgba(0,0,0,0.08)', cursor: 'pointer' },
};

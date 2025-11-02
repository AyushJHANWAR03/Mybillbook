import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { reportsAPI } from '../services/api';
import '../styles/Dashboard.css';

function Dashboard({ onLogout }) {
  const navigate = useNavigate();
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    loadSummary();
  }, []);

  const loadSummary = async () => {
    try {
      const response = await reportsAPI.getSummary(userId);
      setSummary(response.data);
    } catch (error) {
      console.error('Failed to load summary:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <div className="dashboard-loading">Loading...</div>;
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>📊 Dashboard</h1>
        <button onClick={onLogout} className="logout-button">Logout</button>
      </header>

      {summary && (
        <div className="dashboard-content">
          <div className="stats-grid">
            <div className="stat-card">
              <h3>Total Invoices</h3>
              <p className="stat-value">{summary.totalInvoices}</p>
            </div>
            <div className="stat-card">
              <h3>Total Payments</h3>
              <p className="stat-value">{summary.totalPayments}</p>
            </div>
            <div className="stat-card success">
              <h3>Reconciled</h3>
              <p className="stat-value">{summary.reconciledInvoices}</p>
            </div>
            <div className="stat-card warning">
              <h3>Pending</h3>
              <p className="stat-value">{summary.pendingInvoices}</p>
            </div>
            <div className="stat-card">
              <h3>Total Revenue</h3>
              <p className="stat-value">₹{summary.totalRevenue.toLocaleString()}</p>
            </div>
            <div className="stat-card">
              <h3>Pending Revenue</h3>
              <p className="stat-value">₹{summary.pendingRevenue.toLocaleString()}</p>
            </div>
            <div className="stat-card ai">
              <h3>AI Accuracy</h3>
              <p className="stat-value">{(summary.aiAccuracy * 100).toFixed(0)}%</p>
            </div>
            <div className="stat-card">
              <h3>Unreconciled Payments</h3>
              <p className="stat-value">{summary.unreconciledPayments}</p>
            </div>
          </div>

          <div className="dashboard-actions">
            <h2>Quick Actions</h2>
            <div className="action-buttons">
              <button onClick={() => navigate('/upload-invoices')} className="action-btn primary">
                Upload Invoices
              </button>
              <button onClick={() => navigate('/upload-payments')} className="action-btn primary">
                Upload Payments
              </button>
              <button onClick={() => navigate('/reconciliation')} className="action-btn star">
                AI Reconciliation
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default Dashboard;

import { useState, useEffect } from 'react';
import { paymentAPI } from '../services/api';
import '../styles/UploadPayments.css';

function UploadPayments() {
  const [payments, setPayments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [formData, setFormData] = useState({
    amount: '',
    paymentDate: '',
    paymentMode: 'UPI',
    remark: '',
  });
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    loadPayments();
  }, []);

  const loadPayments = async () => {
    try {
      const response = await paymentAPI.getAll(userId);
      setPayments(response.data);
    } catch (error) {
      console.error('Failed to load payments:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setMessage({ type: '', text: '' });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.amount || !formData.paymentDate || !formData.paymentMode) {
      setMessage({ type: 'error', text: 'Amount, date, and payment mode are required' });
      return;
    }

    if (parseFloat(formData.amount) <= 0) {
      setMessage({ type: 'error', text: 'Amount must be greater than 0' });
      return;
    }

    setUploading(true);
    setMessage({ type: '', text: '' });

    try {
      await paymentAPI.upload(userId, [
        {
          amount: parseFloat(formData.amount),
          paymentDate: formData.paymentDate,
          paymentMode: formData.paymentMode,
          remark: formData.remark || '',
        },
      ]);

      setMessage({ type: 'success', text: 'Payment uploaded successfully!' });
      setFormData({
        amount: '',
        paymentDate: '',
        paymentMode: 'UPI',
        remark: '',
      });
      loadPayments();
    } catch (error) {
      console.error('Upload error:', error);
      setMessage({
        type: 'error',
        text: error.response?.data?.message || 'Failed to upload payment',
      });
    } finally {
      setUploading(false);
    }
  };

  const getStatusBadge = (status) => {
    return status === 'UNRECONCILED' ? (
      <span className="status-badge status-unreconciled">UNRECONCILED</span>
    ) : (
      <span className="status-badge status-reconciled">RECONCILED</span>
    );
  };

  const getPaymentModeBadge = (mode) => {
    const modeClasses = {
      UPI: 'mode-upi',
      CASH: 'mode-cash',
      CARD: 'mode-card',
      BANK_TRANSFER: 'mode-bank',
    };
    return <span className={`mode-badge ${modeClasses[mode]}`}>{mode.replace('_', ' ')}</span>;
  };

  return (
    <div className="upload-payments">
      <div className="upload-header">
        <h1>Upload Payments</h1>
      </div>

      <div className="upload-container">
        <div className="upload-form-section">
          <h2>Add New Payment</h2>
          <form onSubmit={handleSubmit} className="upload-form">
            <div className="form-group">
              <label htmlFor="amount">Amount (₹)</label>
              <input
                type="number"
                id="amount"
                name="amount"
                value={formData.amount}
                onChange={handleChange}
                placeholder="10000"
                min="0"
                step="0.01"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="paymentDate">Payment Date</label>
              <input
                type="date"
                id="paymentDate"
                name="paymentDate"
                value={formData.paymentDate}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="paymentMode">Payment Mode</label>
              <select
                id="paymentMode"
                name="paymentMode"
                value={formData.paymentMode}
                onChange={handleChange}
                required
              >
                <option value="UPI">UPI</option>
                <option value="CASH">Cash</option>
                <option value="CARD">Card</option>
                <option value="BANK_TRANSFER">Bank Transfer</option>
              </select>
            </div>

            <div className="form-group">
              <label htmlFor="remark">Remark (Optional)</label>
              <textarea
                id="remark"
                name="remark"
                value={formData.remark}
                onChange={handleChange}
                placeholder="invoice 104, amit payment, etc."
                rows="3"
              />
              <small className="hint">
                Tip: Add messy remarks like "inv 104", "amit bill" to test AI matching!
              </small>
            </div>

            {message.text && (
              <div className={`message ${message.type}`}>{message.text}</div>
            )}

            <button type="submit" className="submit-button" disabled={uploading}>
              {uploading ? 'Uploading...' : 'Upload Payment'}
            </button>
          </form>
        </div>

        <div className="payments-list-section">
          <h2>All Payments ({payments.length})</h2>
          {loading ? (
            <div className="loading">Loading payments...</div>
          ) : payments.length === 0 ? (
            <div className="empty-state">No payments yet. Add your first payment!</div>
          ) : (
            <div className="payments-table-container">
              <table className="payments-table">
                <thead>
                  <tr>
                    <th>Amount</th>
                    <th>Date</th>
                    <th>Mode</th>
                    <th>Remark</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {payments.map((payment) => (
                    <tr key={payment.id}>
                      <td className="amount">₹{payment.amount.toLocaleString()}</td>
                      <td>{new Date(payment.paymentDate).toLocaleDateString()}</td>
                      <td>{getPaymentModeBadge(payment.paymentMode)}</td>
                      <td className="remark">{payment.remark || '-'}</td>
                      <td>{getStatusBadge(payment.status)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

export default UploadPayments;

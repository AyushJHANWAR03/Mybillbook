import { useState, useEffect } from 'react';
import { invoiceAPI } from '../services/api';
import '../styles/UploadInvoices.css';

function UploadInvoices() {
  const [invoices, setInvoices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [formData, setFormData] = useState({
    invoiceNumber: '',
    customerName: '',
    totalAmount: '',
    invoiceDate: '',
  });
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    loadInvoices();
  }, []);

  const loadInvoices = async () => {
    try {
      const response = await invoiceAPI.getAll(userId);
      setInvoices(response.data);
    } catch (error) {
      console.error('Failed to load invoices:', error);
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

    if (!formData.invoiceNumber || !formData.customerName || !formData.totalAmount || !formData.invoiceDate) {
      setMessage({ type: 'error', text: 'All fields are required' });
      return;
    }

    if (parseFloat(formData.totalAmount) <= 0) {
      setMessage({ type: 'error', text: 'Amount must be greater than 0' });
      return;
    }

    setUploading(true);
    setMessage({ type: '', text: '' });

    try {
      await invoiceAPI.upload(userId, [
        {
          invoiceNumber: formData.invoiceNumber,
          customerName: formData.customerName,
          totalAmount: parseFloat(formData.totalAmount),
          invoiceDate: formData.invoiceDate,
        },
      ]);

      setMessage({ type: 'success', text: 'Invoice uploaded successfully!' });
      setFormData({
        invoiceNumber: '',
        customerName: '',
        totalAmount: '',
        invoiceDate: '',
      });
      loadInvoices();
    } catch (error) {
      console.error('Upload error:', error);
      setMessage({
        type: 'error',
        text: error.response?.data?.message || 'Failed to upload invoice',
      });
    } finally {
      setUploading(false);
    }
  };

  const getStatusBadge = (status) => {
    const statusClasses = {
      UNPAID: 'status-unpaid',
      PARTIALLY_PAID: 'status-partial',
      FULLY_PAID: 'status-paid',
    };
    return <span className={`status-badge ${statusClasses[status]}`}>{status.replace('_', ' ')}</span>;
  };

  return (
    <div className="upload-invoices">
      <div className="upload-header">
        <h1>Upload Invoices</h1>
      </div>

      <div className="upload-container">
        <div className="upload-form-section">
          <h2>Add New Invoice</h2>
          <form onSubmit={handleSubmit} className="upload-form">
            <div className="form-group">
              <label htmlFor="invoiceNumber">Invoice Number</label>
              <input
                type="text"
                id="invoiceNumber"
                name="invoiceNumber"
                value={formData.invoiceNumber}
                onChange={handleChange}
                placeholder="INV001"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="customerName">Customer Name</label>
              <input
                type="text"
                id="customerName"
                name="customerName"
                value={formData.customerName}
                onChange={handleChange}
                placeholder="Suresh Traders"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="totalAmount">Total Amount (₹)</label>
              <input
                type="number"
                id="totalAmount"
                name="totalAmount"
                value={formData.totalAmount}
                onChange={handleChange}
                placeholder="10000"
                min="0"
                step="0.01"
                required
              />
            </div>

            <div className="form-group">
              <label htmlFor="invoiceDate">Invoice Date</label>
              <input
                type="date"
                id="invoiceDate"
                name="invoiceDate"
                value={formData.invoiceDate}
                onChange={handleChange}
                required
              />
            </div>

            {message.text && (
              <div className={`message ${message.type}`}>{message.text}</div>
            )}

            <button type="submit" className="submit-button" disabled={uploading}>
              {uploading ? 'Uploading...' : 'Upload Invoice'}
            </button>
          </form>
        </div>

        <div className="invoices-list-section">
          <h2>All Invoices ({invoices.length})</h2>
          {loading ? (
            <div className="loading">Loading invoices...</div>
          ) : invoices.length === 0 ? (
            <div className="empty-state">No invoices yet. Add your first invoice!</div>
          ) : (
            <div className="invoices-table-container">
              <table className="invoices-table">
                <thead>
                  <tr>
                    <th>Invoice #</th>
                    <th>Customer</th>
                    <th>Total</th>
                    <th>Pending</th>
                    <th>Date</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {invoices.map((invoice) => (
                    <tr key={invoice.id}>
                      <td className="invoice-number">{invoice.invoiceNumber}</td>
                      <td>{invoice.customerName}</td>
                      <td className="amount">₹{invoice.totalAmount.toLocaleString()}</td>
                      <td className="amount">₹{invoice.pendingAmount.toLocaleString()}</td>
                      <td>{new Date(invoice.invoiceDate).toLocaleDateString()}</td>
                      <td>{getStatusBadge(invoice.status)}</td>
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

export default UploadInvoices;

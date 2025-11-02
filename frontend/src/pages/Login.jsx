import { useState } from 'react';
import { authAPI } from '../services/api';
import '../styles/Login.css';

function Login({ onLogin }) {
  const [formData, setFormData] = useState({
    mobileNumber: '',
    name: '',
    businessName: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!formData.mobileNumber || !formData.name || !formData.businessName) {
      setError('All fields are required');
      return;
    }

    if (formData.mobileNumber.length !== 10) {
      setError('Mobile number must be 10 digits');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await authAPI.login(formData);
      console.log('Login response:', response.data);

      if (response.data.userId) {
        onLogin(response.data.userId);
      } else {
        setError('Login failed. No userId received.');
      }
    } catch (err) {
      console.error('Login error:', err);
      setError(err.response?.data?.message || 'Failed to login. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <div className="login-header">
          <h1>🤖 AI Invoice Reconciliation</h1>
          <p>Smart reconciliation for small businesses</p>
        </div>

        <form onSubmit={handleSubmit} className="login-form">
          <div className="form-group">
            <label htmlFor="mobileNumber">Mobile Number</label>
            <input
              type="tel"
              id="mobileNumber"
              name="mobileNumber"
              value={formData.mobileNumber}
              onChange={handleChange}
              placeholder="9876543210"
              maxLength="10"
              pattern="[0-9]{10}"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="name">Your Name</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Ramesh Kumar"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="businessName">Business Name</label>
            <input
              type="text"
              id="businessName"
              name="businessName"
              value={formData.businessName}
              onChange={handleChange}
              placeholder="Ramesh Medical Store"
              required
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button
            type="submit"
            className="login-button"
            disabled={loading}
          >
            {loading ? 'Logging in...' : 'Login / Sign Up'}
          </button>

          <button
            type="button"
            className="demo-button"
            onClick={() => {
              setFormData({
                mobileNumber: '9876543210',
                name: 'Ramesh Kumar',
                businessName: 'Ramesh Medical Store',
              });
              setError('');
            }}
          >
            Fill Demo Credentials
          </button>
        </form>

        <div className="login-footer">
          <p>Demo Application for FloBiz/myBillBook</p>
        </div>
      </div>
    </div>
  );
}

export default Login;

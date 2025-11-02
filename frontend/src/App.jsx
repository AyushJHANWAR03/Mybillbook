import { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import UploadInvoices from './pages/UploadInvoices';
import UploadPayments from './pages/UploadPayments';
import AIReconciliation from './pages/AIReconciliation';
import './App.css';

function App() {
  const [userId, setUserId] = useState(localStorage.getItem('userId'));

  const handleLogin = (id) => {
    localStorage.setItem('userId', id);
    setUserId(id);
  };

  const handleLogout = () => {
    localStorage.removeItem('userId');
    setUserId(null);
  };

  return (
    <Router>
      <Routes>
        <Route
          path="/login"
          element={userId ? <Navigate to="/dashboard" /> : <Login onLogin={handleLogin} />}
        />
        <Route
          path="/dashboard"
          element={userId ? <Dashboard onLogout={handleLogout} /> : <Navigate to="/login" />}
        />
        <Route
          path="/upload-invoices"
          element={userId ? <UploadInvoices /> : <Navigate to="/login" />}
        />
        <Route
          path="/upload-payments"
          element={userId ? <UploadPayments /> : <Navigate to="/login" />}
        />
        <Route
          path="/reconciliation"
          element={userId ? <AIReconciliation /> : <Navigate to="/login" />}
        />
        <Route path="/" element={<Navigate to={userId ? "/dashboard" : "/login"} />} />
      </Routes>
    </Router>
  );
}

export default App;

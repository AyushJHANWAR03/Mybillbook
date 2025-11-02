import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const authAPI = {
  login: (data) => api.post('/auth/login', data),
};

export const invoiceAPI = {
  upload: (userId, invoices) => api.post(`/invoices/upload?userId=${userId}`, invoices),
  getAll: (userId) => api.get(`/invoices/all?userId=${userId}`),
  getByStatus: (userId, status) => api.get(`/invoices?userId=${userId}&status=${status}`),
};

export const paymentAPI = {
  upload: (userId, payments) => api.post(`/payments/upload?userId=${userId}`, payments),
  getAll: (userId) => api.get(`/payments/all?userId=${userId}`),
  getByStatus: (userId, status) => api.get(`/payments?userId=${userId}&status=${status}`),
};

export const reconciliationAPI = {
  run: (userId) => api.post(`/reconciliation/run?userId=${userId}`),
  getSuggestions: (userId) => api.get(`/reconciliation/suggestions?userId=${userId}`),
  confirm: (suggestionId, userId) => api.post(`/reconciliation/confirm/${suggestionId}?userId=${userId}`),
  reject: (suggestionId) => api.post(`/reconciliation/reject/${suggestionId}`),
  bulkConfirm: (userId, suggestionIds) => api.post(`/reconciliation/bulk-confirm?userId=${userId}`, suggestionIds),
  bulkConfirmHighConfidence: (userId, minConfidence) =>
    api.post(`/reconciliation/bulk-confirm-high-confidence?userId=${userId}&minConfidence=${minConfidence}`),
};

export const reportsAPI = {
  getSummary: (userId) => api.get(`/reports/summary?userId=${userId}`),
};

export default api;

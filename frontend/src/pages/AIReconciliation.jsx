import { useState, useEffect } from 'react';
import { reconciliationAPI } from '../services/api';
import '../styles/AIReconciliation.css';

function AIReconciliation() {
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [running, setRunning] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });
  const [processing, setProcessing] = useState({});
  const userId = localStorage.getItem('userId');

  useEffect(() => {
    loadSuggestions();
  }, []);

  const loadSuggestions = async () => {
    try {
      const response = await reconciliationAPI.getSuggestions(userId);
      setSuggestions(response.data);
    } catch (error) {
      console.error('Failed to load suggestions:', error);
    } finally {
      setLoading(false);
    }
  };

  const runAIReconciliation = async () => {
    setRunning(true);
    setMessage({ type: '', text: '' });

    try {
      const response = await reconciliationAPI.run(userId);
      setMessage({
        type: 'success',
        text: `AI reconciliation completed! Generated ${response.data.suggestionsGenerated} suggestions.`,
      });
      loadSuggestions();
    } catch (error) {
      console.error('Reconciliation error:', error);
      setMessage({
        type: 'error',
        text: error.response?.data?.message || 'Failed to run AI reconciliation',
      });
    } finally {
      setRunning(false);
    }
  };

  const handleConfirm = async (suggestionId) => {
    setProcessing({ ...processing, [suggestionId]: 'confirming' });

    try {
      await reconciliationAPI.confirm(suggestionId, userId);
      setMessage({ type: 'success', text: 'Match confirmed successfully! Refreshing...' });
      // Wait a moment then reload to get fresh data
      setTimeout(() => {
        window.location.reload();
      }, 1000);
    } catch (error) {
      console.error('Confirm error:', error);
      setMessage({ type: 'error', text: 'Failed to confirm match' });
      setProcessing({ ...processing, [suggestionId]: null });
    }
  };

  const handleReject = async (suggestionId) => {
    setProcessing({ ...processing, [suggestionId]: 'rejecting' });

    try {
      await reconciliationAPI.reject(suggestionId);
      setMessage({ type: 'success', text: 'Match rejected' });
      loadSuggestions();
    } catch (error) {
      console.error('Reject error:', error);
      setMessage({ type: 'error', text: 'Failed to reject match' });
    } finally {
      setProcessing({ ...processing, [suggestionId]: null });
    }
  };

  const handleBulkConfirmHighConfidence = async () => {
    setRunning(true);
    setMessage({ type: '', text: '' });

    try {
      const response = await reconciliationAPI.bulkConfirmHighConfidence(userId, 0.90);
      setMessage({
        type: 'success',
        text: `Bulk confirmed ${response.data.confirmed} high-confidence matches! Refreshing...`,
      });
      // Wait a moment then reload to get fresh data
      setTimeout(() => {
        window.location.reload();
      }, 1500);
    } catch (error) {
      console.error('Bulk confirm error:', error);
      setMessage({ type: 'error', text: 'Failed to bulk confirm' });
      setRunning(false);
    }
  };

  const getConfidenceClass = (confidence) => {
    if (confidence >= 0.90) return 'confidence-high';
    if (confidence >= 0.70) return 'confidence-medium';
    return 'confidence-low';
  };

  const getConfidenceLabel = (confidence) => {
    if (confidence >= 0.90) return 'HIGH';
    if (confidence >= 0.70) return 'MEDIUM';
    return 'LOW';
  };

  const pendingSuggestions = suggestions.filter((s) => s.status === 'PENDING');

  return (
    <div className="ai-reconciliation">
      <div className="reconciliation-header">
        <div>
          <h1>AI Reconciliation</h1>
          <p>Intelligent invoice-payment matching powered by OpenAI</p>
        </div>
        <div className="header-actions">
          <button
            onClick={runAIReconciliation}
            className="btn-run-ai"
            disabled={running}
          >
            {running ? 'Running AI...' : 'Run AI Reconciliation'}
          </button>
          {pendingSuggestions.length > 0 && (
            <button
              onClick={handleBulkConfirmHighConfidence}
              className="btn-bulk-confirm"
              disabled={running}
            >
              Bulk Confirm (≥90%)
            </button>
          )}
        </div>
      </div>

      <div className="reconciliation-content">
        {message.text && (
          <div className={`message ${message.type}`}>{message.text}</div>
        )}

        {loading ? (
          <div className="loading-state">
            <div className="spinner"></div>
            <p>Loading AI suggestions...</p>
          </div>
        ) : pendingSuggestions.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🤖</div>
            <h2>No Pending Suggestions</h2>
            <p>Run AI reconciliation to generate intelligent payment-invoice matches!</p>
            <button onClick={runAIReconciliation} className="btn-run-ai-large">
              Run AI Reconciliation
            </button>
          </div>
        ) : (
          <div className="suggestions-grid">
            {pendingSuggestions.map((suggestion) => (
              <div key={suggestion.id} className="suggestion-card">
                <div className="card-header">
                  <div
                    className={`confidence-badge ${getConfidenceClass(
                      suggestion.confidence
                    )}`}
                  >
                    <span className="confidence-label">
                      {getConfidenceLabel(suggestion.confidence)} CONFIDENCE
                    </span>
                    <span className="confidence-value">
                      {(suggestion.confidence * 100).toFixed(0)}%
                    </span>
                  </div>
                </div>

                <div className="card-body">
                  <div className="match-section">
                    <div className="payment-info">
                      <h3>Payment</h3>
                      <div className="info-item">
                        <span className="label">Amount:</span>
                        <span className="value amount">
                          ₹{suggestion.payment.amount.toLocaleString()}
                        </span>
                      </div>
                      <div className="info-item">
                        <span className="label">Date:</span>
                        <span className="value">
                          {new Date(suggestion.payment.paymentDate).toLocaleDateString()}
                        </span>
                      </div>
                      <div className="info-item">
                        <span className="label">Mode:</span>
                        <span className="value mode">{suggestion.payment.paymentMode}</span>
                      </div>
                      <div className="info-item">
                        <span className="label">Remark:</span>
                        <span className="value remark">
                          "{suggestion.payment.remark || 'No remark'}"
                        </span>
                      </div>
                    </div>

                    <div className="match-arrow">→</div>

                    <div className="invoice-info">
                      <h3>Invoice</h3>
                      <div className="info-item">
                        <span className="label">Invoice #:</span>
                        <span className="value invoice-number">
                          {suggestion.invoice.invoiceNumber}
                        </span>
                      </div>
                      <div className="info-item">
                        <span className="label">Customer:</span>
                        <span className="value">{suggestion.invoice.customerName}</span>
                      </div>
                      <div className="info-item">
                        <span className="label">Total:</span>
                        <span className="value">
                          ₹{suggestion.invoice.totalAmount.toLocaleString()}
                        </span>
                      </div>
                      <div className="info-item">
                        <span className="label">Pending:</span>
                        <span className="value amount">
                          ₹{suggestion.invoice.pendingAmount.toLocaleString()}
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className="reasoning-section">
                    <h4>AI Reasoning</h4>
                    <p className="reasoning-text">{suggestion.reasoning}</p>
                    <div className="ai-model-tag">Model: {suggestion.aiModel}</div>
                  </div>
                </div>

                <div className="card-actions">
                  <button
                    onClick={() => handleReject(suggestion.id)}
                    className="btn-reject"
                    disabled={processing[suggestion.id]}
                  >
                    {processing[suggestion.id] === 'rejecting' ? 'Rejecting...' : 'Reject'}
                  </button>
                  <button
                    onClick={() => handleConfirm(suggestion.id)}
                    className="btn-confirm"
                    disabled={processing[suggestion.id]}
                  >
                    {processing[suggestion.id] === 'confirming'
                      ? 'Confirming...'
                      : 'Confirm Match'}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default AIReconciliation;

import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Input } from '../components/ui/Input';
import { Button } from '../components/ui/Button';

export const AuthPage: React.FC = () => {
  const [isLogin, setIsLogin] = useState(true);
  const { login, register } = useAuth();
  const navigate = useNavigate();

  // Form states
  const [companyName, setCompanyName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [state, setState] = useState('Maharashtra');
  const [cpcbReg, setCpcbReg] = useState('');

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      if (isLogin) {
        await login({ email, password });
      } else {
        await register({
          companyName,
          email,
          state,
          password,
          cpcbRegistrationNumber: cpcbReg || undefined,
        });
      }
      navigate('/');
    } catch (err: unknown) {
      if (typeof err === 'object' && err !== null && 'response' in err) {
        const errorResponse = (err as { response?: { data?: { message?: string } } }).response;
        setError(errorResponse?.data?.message || 'Authentication failed. Please verify credentials.');
      } else {
        setError('Authentication service unavailable. Please try again.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-bg flex flex-col justify-center items-center p-4">
      <div className="w-full max-w-md bg-surface border border-border rounded-card p-6 md:p-8 shadow-2xl">
        {/* Branding */}
        <div className="text-center mb-6">
          <div className="inline-flex w-12 h-12 rounded-card bg-blue/15 border border-blue/40 items-center justify-center text-blue font-mono font-bold text-lg mb-2">
            EPR
          </div>
          <h2 className="text-lg font-bold text-text">EPR Compliance Tracker</h2>
          <p className="text-xs text-text-muted mt-1">
            CPCB E-Waste Rules 2022 Legal Obligations
          </p>
        </div>

        {/* Tab switch */}
        <div className="flex border-b border-border mb-6">
          <button
            onClick={() => {
              setIsLogin(true);
              setError(null);
            }}
            className={`flex-1 pb-2 text-xs font-semibold text-center border-b-2 transition-colors ${
              isLogin ? 'border-blue text-blue' : 'border-transparent text-text-muted hover:text-text'
            }`}
          >
            Sign In
          </button>
          <button
            onClick={() => {
              setIsLogin(false);
              setError(null);
            }}
            className={`flex-1 pb-2 text-xs font-semibold text-center border-b-2 transition-colors ${
              !isLogin ? 'border-blue text-blue' : 'border-transparent text-text-muted hover:text-text'
            }`}
          >
            Register Brand
          </button>
        </div>

        {error && (
          <div className="p-3 mb-4 rounded-input bg-red/10 border border-red/30 text-red text-xs font-mono">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="flex flex-col gap-4">
          {!isLogin && (
            <>
              <Input
                label="Company / Brand Name"
                placeholder="e.g. boAt Lifestyle"
                value={companyName}
                onChange={(e) => setCompanyName(e.target.value)}
                required
              />
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-medium text-text-muted">Registered State / UT</label>
                <select
                  value={state}
                  onChange={(e) => setState(e.target.value)}
                  className="w-full bg-surface-2 border border-border rounded-input px-3 py-2 text-sm text-text focus:outline-none focus:border-blue"
                >
                  <option value="Maharashtra">Maharashtra</option>
                  <option value="Delhi">Delhi</option>
                  <option value="Karnataka">Karnataka</option>
                  <option value="Tamil Nadu">Tamil Nadu</option>
                  <option value="Telangana">Telangana</option>
                  <option value="Uttar Pradesh">Uttar Pradesh</option>
                  <option value="Gujarat">Gujarat</option>
                  <option value="Rajasthan">Rajasthan</option>
                  <option value="West Bengal">West Bengal</option>
                  <option value="Haryana">Haryana</option>
                </select>
              </div>
              <Input
                label="CPCB Registration Number (Optional)"
                placeholder="e.g. CPCB/EPR/2023/1029"
                value={cpcbReg}
                onChange={(e) => setCpcbReg(e.target.value)}
              />
            </>
          )}

          <Input
            label="Official Business Email"
            type="email"
            placeholder="compliance@enterprise.com"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />

          <Input
            label="Password"
            type="password"
            placeholder="••••••••"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />

          <Button type="submit" variant="primary" isLoading={isLoading} className="w-full mt-2">
            {isLogin ? 'Access Compliance Dashboard' : 'Create Enterprise Account'}
          </Button>
        </form>
      </div>
    </div>
  );
};

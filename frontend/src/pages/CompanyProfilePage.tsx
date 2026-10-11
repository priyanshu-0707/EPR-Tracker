import React, { useEffect, useState } from 'react';
import { companyService } from '../services';
import { CompanyProfile } from '../types';
import { Button } from '../components/ui/Button';
import { Input } from '../components/ui/Input';
import { Skeleton } from '../components/ui/Skeleton';
import { Modal } from '../components/ui/Modal';
import { useAuth } from '../context/AuthContext';

export const CompanyProfilePage: React.FC = () => {
  const [profile, setProfile] = useState<CompanyProfile | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isUpdating, setIsUpdating] = useState(false);
  const [updateSuccess, setUpdateSuccess] = useState(false);

  // Form states
  const [companyName, setCompanyName] = useState('');
  const [state, setState] = useState('');
  const [cpcbReg, setCpcbReg] = useState('');

  // Delete modal
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [confirmDeleteInput, setConfirmDeleteInput] = useState('');
  const { logout } = useAuth();

  useEffect(() => {
    fetchProfile();
  }, []);

  const fetchProfile = async () => {
    try {
      setIsLoading(true);
      const res = await companyService.getProfile();
      setProfile(res);
      setCompanyName(res.companyName);
      setState(res.state);
      setCpcbReg(res.cpcbRegistrationNumber || '');
    } catch {
      // Handled
    } finally {
      setIsLoading(false);
    }
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setIsUpdating(true);
      const updated = await companyService.updateProfile({
        companyName,
        state,
        cpcbRegistrationNumber: cpcbReg,
      });
      setProfile(updated);
      setUpdateSuccess(true);
      setTimeout(() => setUpdateSuccess(false), 3000);
    } catch {
      alert('Unable to update company profile.');
    } finally {
      setIsUpdating(false);
    }
  };

  const handleDeleteAccount = async () => {
    if (confirmDeleteInput !== profile?.companyName) {
      alert('Company name confirmation does not match.');
      return;
    }
    try {
      await companyService.deleteAccount();
      logout();
    } catch {
      alert('Failed to delete account.');
    }
  };

  if (isLoading) {
    return (
      <div className="p-4 md:p-6 flex flex-col gap-6">
        <Skeleton className="h-8 w-60" />
        <Skeleton className="h-64 w-full rounded-card" />
      </div>
    );
  }

  return (
    <div className="p-4 md:p-6 flex flex-col gap-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-bold text-text">Enterprise Profile & CPCB Details</h2>
        <p className="text-xs text-text-muted mt-0.5">
          Legal registration profile filed with Central Pollution Control Board
        </p>
      </div>

      {updateSuccess && (
        <div className="p-3 bg-green/10 border border-green/30 text-green rounded-card text-xs font-mono">
          ✓ Enterprise profile credentials updated successfully.
        </div>
      )}

      {/* Profile Form */}
      <div className="p-5 bg-surface border border-border rounded-card">
        <h3 className="text-sm font-semibold text-text mb-4">Official Entity Information</h3>
        <form onSubmit={handleUpdate} className="flex flex-col gap-4">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="Corporate Entity Name"
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
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Input
              label="CPCB Registration Number"
              value={cpcbReg}
              onChange={(e) => setCpcbReg(e.target.value)}
              placeholder="e.g. CPCB/EPR/2023/1029"
            />

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-medium text-text-muted">Login Account Email</label>
              <input
                disabled
                value={profile?.email || ''}
                className="w-full bg-surface border border-border/40 rounded-input px-3 py-2 text-sm text-text-dim cursor-not-allowed font-mono"
              />
            </div>
          </div>

          <div className="flex justify-end mt-2">
            <Button type="submit" variant="primary" isLoading={isUpdating}>
              Save Profile Changes
            </Button>
          </div>
        </form>
      </div>

      {/* Danger Zone */}
      <div className="p-5 bg-surface border border-red/30 rounded-card">
        <h3 className="text-sm font-semibold text-red mb-1">Danger Zone</h3>
        <p className="text-xs text-text-muted mb-4">
          Permanently delete this company profile, registered product inventory, and historical compliance records.
        </p>

        <Button
          variant="danger"
          size="sm"
          onClick={() => setIsDeleteModalOpen(true)}
        >
          Delete Enterprise Account
        </Button>
      </div>

      {/* Delete Confirmation Modal */}
      <Modal
        isOpen={isDeleteModalOpen}
        onClose={() => setIsDeleteModalOpen(false)}
        title="Confirm Account Deletion"
      >
        <div className="flex flex-col gap-3">
          <p className="text-xs text-text-muted leading-relaxed">
            This action cannot be undone. To permanently delete your company records, please type your exact company name <strong className="text-text font-mono font-bold">{profile?.companyName}</strong> below:
          </p>
          <Input
            value={confirmDeleteInput}
            onChange={(e) => setConfirmDeleteInput(e.target.value)}
            placeholder="Type company name to confirm"
          />
          <div className="flex justify-end gap-2 mt-4">
            <Button
              variant="secondary"
              size="sm"
              onClick={() => setIsDeleteModalOpen(false)}
            >
              Cancel
            </Button>
            <Button
              variant="danger"
              size="sm"
              disabled={confirmDeleteInput !== profile?.companyName}
              onClick={handleDeleteAccount}
            >
              Permanently Delete
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
};

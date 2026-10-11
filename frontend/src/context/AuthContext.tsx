import React, { createContext, useContext, useState, ReactNode } from 'react';
import { AuthUser, LoginPayload, RegisterPayload } from '../types';
import { authService } from '../services';

interface AuthContextType {
  user: AuthUser | null;
  token: string | null;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<AuthUser | null>(null);

  const login = async (payload: LoginPayload) => {
    const authData = await authService.login(payload);
    setUser(authData);
  };

  const register = async (payload: RegisterPayload) => {
    const authData = await authService.register(payload);
    setUser(authData);
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token: user ? user.token : null,
        login,
        register,
        logout,
        isAuthenticated: !!user,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

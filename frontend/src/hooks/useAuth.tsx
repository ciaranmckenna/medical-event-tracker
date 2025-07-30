import { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { authService } from '../services/auth/authService';
import type { User, LoginRequest, RegisterRequest } from '../types/api';

interface AuthContextType {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  hasRole: (role: string) => boolean;
  hasAnyRole: (roles: string[]) => boolean;
  isPrimaryUser: () => boolean;
  isSecondaryUser: () => boolean;
  isAdmin: () => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    initializeAuth();
  }, []);

  const initializeAuth = async () => {
    setIsLoading(true);
    try {
      // Check if user has valid stored token
      if (authService.isAuthenticated()) {
        const token = authService.getStoredToken();
        if (token) {
          // Validate token with backend
          const isValid = await authService.validateToken();
          if (isValid) {
            const userData = authService.getStoredUser();
            setUser(userData);
          } else {
            // Token invalid, clear auth data
            await authService.logout();
            setUser(null);
          }
        }
      }
    } catch (error) {
      console.error('Failed to initialize auth:', error);
      await authService.logout();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (credentials: LoginRequest) => {
    try {
      const response = await authService.login(credentials);
      setUser(response.user);
    } catch (error) {
      throw error; // Let the login component handle the error
    }
  };

  const register = async (userData: RegisterRequest) => {
    try {
      const response = await authService.register(userData);
      setUser(response.user);
    } catch (error) {
      throw error; // Let the register component handle the error
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      setUser(null);
    }
  };

  const hasRole = (role: string): boolean => {
    return user?.role === role;
  };

  const hasAnyRole = (roles: string[]): boolean => {
    return user ? roles.includes(user.role) : false;
  };

  const isPrimaryUser = (): boolean => {
    return hasRole('PRIMARY_USER');
  };

  const isSecondaryUser = (): boolean => {
    return hasRole('SECONDARY_USER');
  };

  const isAdmin = (): boolean => {
    return hasRole('ADMIN');
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    register,
    logout,
    hasRole,
    hasAnyRole,
    isPrimaryUser,
    isSecondaryUser,
    isAdmin
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
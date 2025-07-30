import { apiClient } from '../api/apiClient';
import type { LoginRequest, RegisterRequest, AuthResponse, User } from '../../types/api';

export class AuthService {
  // Authentication endpoints
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await apiClient.post<User>('/api/auth/login', credentials);
    
    // Backend currently returns User directly instead of AuthResponse
    // TODO: Backend should return {token, user, expiresIn}
    // For now, create a mock token for demo purposes
    const mockAuthResponse: AuthResponse = {
      token: 'demo-jwt-token-' + Date.now(),
      user: response as User,
      expiresIn: 3600
    };
    
    // Store token and user data
    apiClient.setAuthToken(mockAuthResponse.token);
    this.storeUserData(mockAuthResponse.user);
    
    return mockAuthResponse;
  }

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    const response = await apiClient.post<User>('/api/auth/register', userData);
    
    // Backend currently returns User directly instead of AuthResponse
    // TODO: Backend should return {token, user, expiresIn}
    // For now, create a mock token for demo purposes
    const mockAuthResponse: AuthResponse = {
      token: 'demo-jwt-token-' + Date.now(),
      user: response as User,
      expiresIn: 3600
    };
    
    // Store token and user data
    apiClient.setAuthToken(mockAuthResponse.token);
    this.storeUserData(mockAuthResponse.user);
    
    return mockAuthResponse;
  }

  async logout(): Promise<void> {
    try {
      // Optional: Call logout endpoint if backend supports it
      // await apiClient.post('/api/auth/logout');
    } catch (error) {
      console.warn('Logout endpoint error:', error);
    } finally {
      // Always clear local data
      this.clearAuthData();
    }
  }

  async getCurrentUser(): Promise<User> {
    return apiClient.get<User>('/api/auth/profile');
  }

  async updateProfile(userData: Partial<User>): Promise<User> {
    return apiClient.put<User>('/api/auth/profile', userData);
  }

  async deleteAccount(): Promise<void> {
    await apiClient.delete('/api/auth/profile');
    this.clearAuthData();
  }

  async checkUsernameAvailability(username: string): Promise<boolean> {
    try {
      await apiClient.get(`/api/auth/check-username/${encodeURIComponent(username)}`);
      return true; // Username is available
    } catch {
      return false; // Username is taken
    }
  }

  async checkEmailAvailability(email: string): Promise<boolean> {
    try {
      await apiClient.get(`/api/auth/check-email/${encodeURIComponent(email)}`);
      return true; // Email is available
    } catch {
      return false; // Email is taken
    }
  }

  // Token management
  getStoredToken(): string | null {
    return sessionStorage.getItem('auth_token') || localStorage.getItem('auth_token');
  }

  isTokenValid(): boolean {
    const token = this.getStoredToken();
    if (!token) return false;

    // For demo tokens, just check if token exists and user data exists
    if (token.startsWith('demo-jwt-token-')) {
      return this.getStoredUser() !== null;
    }

    try {
      // Decode JWT token to check expiration
      const payload = JSON.parse(atob(token.split('.')[1]));
      const currentTime = Math.floor(Date.now() / 1000);
      return payload.exp > currentTime;
    } catch (error) {
      console.error('Error validating token:', error);
      return false;
    }
  }

  async validateToken(): Promise<boolean> {
    const token = this.getStoredToken();
    
    // For demo tokens, just validate locally
    if (token?.startsWith('demo-jwt-token-')) {
      return this.isTokenValid();
    }
    
    try {
      await this.getCurrentUser();
      return true;
    } catch {
      this.clearAuthData();
      return false;
    }
  }

  // User data management
  getStoredUser(): User | null {
    const userData = sessionStorage.getItem('user_data') || localStorage.getItem('user_data');
    return userData ? JSON.parse(userData) : null;
  }

  private storeUserData(user: User): void {
    sessionStorage.setItem('user_data', JSON.stringify(user));
  }

  private clearAuthData(): void {
    // Clear session storage
    sessionStorage.removeItem('auth_token');
    sessionStorage.removeItem('user_data');
    
    // Clear local storage (if used for "remember me")
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_data');
  }

  // Role-based access control
  hasRole(requiredRole: string): boolean {
    const user = this.getStoredUser();
    return user?.role === requiredRole;
  }

  hasAnyRole(roles: string[]): boolean {
    const user = this.getStoredUser();
    return user ? roles.includes(user.role) : false;
  }

  canAccessRoute(requiredRoles?: string[]): boolean {
    if (!requiredRoles || requiredRoles.length === 0) {
      return true; // Public route
    }
    
    const user = this.getStoredUser();
    if (!user) {
      return false; // Not authenticated
    }
    
    return requiredRoles.includes(user.role);
  }

  // Authentication state checks
  isAuthenticated(): boolean {
    return this.getStoredToken() !== null && this.isTokenValid();
  }

  isPrimaryUser(): boolean {
    return this.hasRole('PRIMARY_USER');
  }

  isSecondaryUser(): boolean {
    return this.hasRole('SECONDARY_USER');
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  // Password validation (client-side)
  validatePassword(password: string): {
    isValid: boolean;
    errors: string[];
  } {
    const errors: string[] = [];
    
    if (password.length < 8) {
      errors.push('Password must be at least 8 characters long');
    }
    
    if (!/[A-Z]/.test(password)) {
      errors.push('Password must contain at least one uppercase letter');
    }
    
    if (!/[a-z]/.test(password)) {
      errors.push('Password must contain at least one lowercase letter');
    }
    
    if (!/\d/.test(password)) {
      errors.push('Password must contain at least one number');
    }
    
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
      errors.push('Password must contain at least one special character');
    }
    
    return {
      isValid: errors.length === 0,
      errors
    };
  }
}

// Create and export singleton instance
export const authService = new AuthService();
export default authService;
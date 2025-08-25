import { apiClient } from '../api/apiClient';
import type { 
  LoginRequest, 
  RegisterRequest, 
  AuthResponse, 
  UserProfileResponse 
} from '../../types/medical';
import { sanitizeEmail, sanitizePatientName } from '../../utils/sanitization';

export class AuthService {
  // Authentication endpoints
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    try {
      // Sanitize input before sending to backend
      const sanitizedCredentials = {
        usernameOrEmail: credentials.usernameOrEmail.trim(),
        password: credentials.password
      };

      console.log('[AuthService] Attempting login with:', { usernameOrEmail: sanitizedCredentials.usernameOrEmail });
      
      const response = await apiClient.post<AuthResponse>('/api/auth/login', sanitizedCredentials);
      
      console.log('[AuthService] Login response received:', {
        id: response.id,
        username: response.username,
        token: response.token ? 'present' : 'missing',
        tokenType: response.tokenType,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        role: response.role
      });
      
      console.log('[AuthService] Setting auth token...');
      // Store token and user data
      apiClient.setAuthToken(response.token);
      console.log('[AuthService] Auth token set successfully');
      
      console.log('[AuthService] Creating user profile...');
      
      // Check for missing fields
      if (!response.email) console.warn('[AuthService] Missing email in response');
      if (!response.firstName) console.warn('[AuthService] Missing firstName in response');
      if (!response.lastName) console.warn('[AuthService] Missing lastName in response');
      if (!response.role) console.warn('[AuthService] Missing role in response');
      
      // Convert backend response to frontend format with proper type handling
      const userProfile: UserProfileResponse = {
        id: String(response.id), // Convert UUID to string
        username: response.username,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        role: response.role,
        enabled: true, // Backend doesn't return this in auth response
        createdAt: '', // Backend doesn't return this in auth response
        updatedAt: '' // Backend doesn't return this in auth response
      };
      
      console.log('[AuthService] Storing user data:', userProfile);
      this.storeUserData(userProfile);
      console.log('[AuthService] User data stored successfully');
      
      console.log('[AuthService] Login completed successfully');
      return response;
    } catch (error: any) {
      console.error('[AuthService] Login error:', {
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data,
        message: error.message,
        url: error.config?.url
      });
      
      // Handle secure error messages from backend
      const message = error.response?.data?.message || 'Login failed. Please check your credentials.';
      throw new Error(message);
    }
  }

  async register(userData: RegisterRequest): Promise<AuthResponse> {
    try {
      // Sanitize input before sending to backend
      const sanitizedData = {
        username: userData.username.trim(),
        email: sanitizeEmail(userData.email),
        password: userData.password,
        firstName: sanitizePatientName(userData.firstName),
        lastName: sanitizePatientName(userData.lastName)
      };

      const response = await apiClient.post<AuthResponse>('/api/auth/register', sanitizedData);
      
      // Store token and user data
      apiClient.setAuthToken(response.token);
      
      // Convert backend response to frontend format with proper type handling
      const userProfile: UserProfileResponse = {
        id: String(response.id), // Convert UUID to string
        username: response.username,
        email: response.email,
        firstName: response.firstName,
        lastName: response.lastName,
        role: response.role,
        enabled: true, // Backend doesn't return this in auth response
        createdAt: '', // Backend doesn't return this in auth response
        updatedAt: '' // Backend doesn't return this in auth response
      };
      
      this.storeUserData(userProfile);
      
      return response;
    } catch (error: any) {
      // Handle secure error messages from backend
      const message = error.response?.data?.message || 'Registration failed. Please try again.';
      throw new Error(message);
    }
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

  async getCurrentUser(): Promise<UserProfileResponse> {
    try {
      return await apiClient.get<UserProfileResponse>('/api/auth/profile');
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to fetch user profile';
      throw new Error(message);
    }
  }

  async updateProfile(userData: Partial<RegisterRequest>): Promise<UserProfileResponse> {
    try {
      // Sanitize input data
      const sanitizedData: any = {};
      if (userData.firstName) sanitizedData.firstName = sanitizePatientName(userData.firstName);
      if (userData.lastName) sanitizedData.lastName = sanitizePatientName(userData.lastName);
      if (userData.email) sanitizedData.email = sanitizeEmail(userData.email);
      if (userData.username) sanitizedData.username = userData.username.trim();

      return await apiClient.put<UserProfileResponse>('/api/auth/profile', sanitizedData);
    } catch (error: any) {
      const message = error.response?.data?.message || 'Failed to update profile';
      throw new Error(message);
    }
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
  getStoredUser(): UserProfileResponse | null {
    const userData = sessionStorage.getItem('user_data') || localStorage.getItem('user_data');
    return userData ? JSON.parse(userData) : null;
  }

  private storeUserData(user: UserProfileResponse): void {
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
import axios from 'axios';
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';

class ApiClient {
  private client: AxiosInstance;
  private baseURL: string;

  constructor() {
    this.baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: Number(import.meta.env.VITE_API_TIMEOUT) || 10000,
      headers: {
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    // Request interceptor
    this.client.interceptors.request.use(
      (config) => {
        const token = this.getAuthToken();
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }

        // Add CSRF token if available
        const csrfToken = this.getCsrfToken();
        if (csrfToken) {
          config.headers['X-CSRF-Token'] = csrfToken;
        }

        // Log request in development
        if (import.meta.env.VITE_DEBUG_MODE === 'true') {
          console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, config.data);
        }

        return config;
      },
      (error) => {
        console.error('[API Request Error]', error);
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.client.interceptors.response.use(
      (response: AxiosResponse) => {
        // Log response in development
        if (import.meta.env.VITE_DEBUG_MODE === 'true') {
          console.log(`[API Response] ${response.config.method?.toUpperCase()} ${response.config.url}`, response.data);
        }
        return response;
      },
      (error: AxiosError) => {
        this.handleResponseError(error);
        return Promise.reject(error);
      }
    );
  }

  private handleResponseError(error: AxiosError): void {
    if (error.response?.status === 401) {
      // Token expired or invalid - backend now returns secure error messages
      this.clearAuthToken();
      // Use dynamic import to avoid circular dependency
      import('../../pages/auth/LoginPage').then(() => {
        window.location.href = '/login';
      });
    } else if (error.response?.status === 403) {
      // Insufficient permissions - backend provides secure error message
      const message = error.response?.data?.message || 'Access denied. Insufficient permissions.';
      console.error('[API Error]', message);
      // Could dispatch to toast notification system
    } else if (error.response?.status >= 500) {
      // Server error - backend now returns secure error messages
      const message = error.response?.data?.message || 'Internal server error occurred';
      console.error('[API Error]', message);
    } else if (error.response?.status === 400) {
      // Bad request - backend validation errors are now sanitized
      const message = error.response?.data?.message || 'Invalid request data';
      console.error('[API Error]', message);
    }

    // Log all errors in development (but not sensitive data)
    if (import.meta.env.VITE_DEBUG_MODE === 'true') {
      console.error('[API Error]', {
        status: error.response?.status,
        message: error.response?.data?.message || error.message,
        url: error.config?.url,
        method: error.config?.method
      });
    }
  }

  private getAuthToken(): string | null {
    return sessionStorage.getItem('auth_token') || localStorage.getItem('auth_token');
  }

  private clearAuthToken(): void {
    sessionStorage.removeItem('auth_token');
    localStorage.removeItem('auth_token');
  }

  private getCsrfToken(): string | null {
    return document.querySelector('meta[name="csrf-token"]')?.getAttribute('content') || null;
  }

  // HTTP Methods
  async get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.get<T>(url, config);
    return response.data;
  }

  async post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.post<T>(url, data, config);
    return response.data;
  }

  async put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.put<T>(url, data, config);
    return response.data;
  }

  async patch<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.patch<T>(url, data, config);
    return response.data;
  }

  async delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    const response = await this.client.delete<T>(url, config);
    return response.data;
  }

  // Utility methods
  setAuthToken(token: string): void {
    sessionStorage.setItem('auth_token', token);
  }

  getBaseURL(): string {
    return this.baseURL;
  }

  // Health check
  async healthCheck(): Promise<{ status: string; timestamp: string }> {
    return this.get('/actuator/health');
  }
}

// Create and export singleton instance
export const apiClient = new ApiClient();
export default apiClient;
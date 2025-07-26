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
      // Token expired or invalid
      this.clearAuthToken();
      // Redirect to login page
      window.location.href = '/auth/login';
    } else if (error.response?.status === 403) {
      // Insufficient permissions
      console.error('[API Error] Insufficient permissions');
    } else if (error.response?.status >= 500) {
      // Server error
      console.error('[API Error] Server error', error.response.data);
    }

    // Log all errors in development
    if (import.meta.env.VITE_DEBUG_MODE === 'true') {
      console.error('[API Error]', {
        status: error.response?.status,
        message: error.message,
        data: error.response?.data,
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
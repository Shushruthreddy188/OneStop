import axios from 'axios';
import { config } from '../config';

/**
 * Shared axios instance. All requests go through the API Gateway.
 *
 * A request interceptor attaches the JWT once auth lands in Milestone 2;
 * for now it is a no-op placeholder.
 */
export const apiClient = axios.create({
  baseURL: config.apiBaseUrl,
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((cfg) => {
  const token = localStorage.getItem('onestop.accessToken');
  if (token) {
    cfg.headers.Authorization = `Bearer ${token}`;
  }
  return cfg;
});

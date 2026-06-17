export const ENV = {
  API_URL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api/v1',
  WS_URL: import.meta.env.VITE_WS_URL ?? 'http://localhost:8080/ws',
  APP_NAME: import.meta.env.VITE_APP_NAME ?? 'Horse Racing Tournament',
};

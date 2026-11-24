import axios from 'axios';

const getBaseUrl = () => {
  const rawUrl = import.meta.env.VITE_API_BASE_URL || '/api';
  const trimmed = rawUrl.trim().replace(/\/+$/, '');
  if (trimmed === '/api' || trimmed.endsWith('/api')) {
    return trimmed;
  }
  return `${trimmed}/api`;
};

// In-memory cache for fast tab navigation and deduplicating in-flight GET requests
const memoryCache = new Map();
const inFlightRequests = new Map();

// Default TTL configs (in milliseconds)
const CACHE_CONFIGS = [
  { pattern: /^\/categories/, ttl: 30000 },
  { pattern: /^\/suppliers/, ttl: 30000 },
  { pattern: /^\/notifications/, ttl: 15000 },
  { pattern: /^\/dashboard/, ttl: 10000 },
];

const getCacheTtl = (url) => {
  for (const config of CACHE_CONFIGS) {
    if (config.pattern.test(url)) {
      return config.ttl;
    }
  }
  return 0; // Don't cache by default unless matched
};

// Create an Axios instance pointing to the proxied back-end URL
const api = axios.create({
  baseURL: getBaseUrl(),
  headers: {
    'Content-Type': 'application/json',
  },
});

// Cache invalidation helper
api.clearCache = () => {
  memoryCache.clear();
  inFlightRequests.clear();
};

// Request Interceptor: Inject JWT token into headers if logged in
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }

    // Invalidate cache on mutations (POST, PUT, DELETE, PATCH)
    const method = (config.method || 'get').toLowerCase();
    if (['post', 'put', 'delete', 'patch'].includes(method)) {
      memoryCache.clear();
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Handle errors globally (e.g. 401 / 403 authorization failures)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      const { status } = error.response;
      if (status === 401) {
        api.clearCache();
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
      }
    }
    return Promise.reject(error);
  }
);

// Transparent caching wrapper on api.get
const originalGet = api.get.bind(api);
api.get = function (url, config = {}) {
  // If skipCache is requested, bypass cache completely
  if (config.skipCache) {
    return originalGet(url, config);
  }

  const cacheKey = `${url}__${JSON.stringify(config.params || {})}`;
  const now = Date.now();
  const cached = memoryCache.get(cacheKey);

  if (cached && now - cached.timestamp < cached.ttl) {
    return Promise.resolve(cached.response);
  }

  // Deduplicate identical in-flight GET requests
  if (inFlightRequests.has(cacheKey)) {
    return inFlightRequests.get(cacheKey);
  }

  const ttl = config.ttl !== undefined ? config.ttl : getCacheTtl(url);

  const requestPromise = originalGet(url, config)
    .then((response) => {
      inFlightRequests.delete(cacheKey);
      if (ttl > 0 && response && response.status === 200) {
        memoryCache.set(cacheKey, { response, timestamp: Date.now(), ttl });
      }
      return response;
    })
    .catch((err) => {
      inFlightRequests.delete(cacheKey);
      return Promise.reject(err);
    });

  inFlightRequests.set(cacheKey, requestPromise);
  return requestPromise;
};

export default api;

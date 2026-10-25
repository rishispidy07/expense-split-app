// ── Backend API base URL ──────────────────────────────────────────────────
// Served by the backend (local dev): leave the default '' (same origin).
// Frontend hosted separately (e.g. Netlify): set the public backend URL, e.g.
//   window.API_BASE_URL = 'https://xxxx.trycloudflare.com';
// or add <meta name="api-base" content="https://xxxx.trycloudflare.com"> to the page.
if (!window.API_BASE_URL) {
    const meta = document.querySelector('meta[name="api-base"]');
    if (meta && meta.content) window.API_BASE_URL = meta.content;
}
window.API_BASE_URL = window.API_BASE_URL || '';

const API = {
    baseUrl: window.API_BASE_URL,

    getToken() {
        return localStorage.getItem('token');
    },

    setToken(token) {
        localStorage.setItem('token', token);
    },

    clearToken() {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    },

    getUser() {
        try {
            const u = localStorage.getItem('user');
            return u ? JSON.parse(u) : null;
        } catch (e) {
            localStorage.removeItem('user');
            return null;
        }
    },

    setUser(user) {
        localStorage.setItem('user', JSON.stringify(user));
    },

    isLoggedIn() {
        return !!this.getToken();
    },

    requireAuth() {
        if (!this.isLoggedIn()) {
            window.location.href = '/login.html';
            return false;
        }
        return true;
    },

    async request(method, path, body) {
        const headers = { 'Content-Type': 'application/json' };
        const token = this.getToken();
        if (token && path !== '/api/auth/login' && path !== '/api/auth/register') {
            headers['Authorization'] = 'Bearer ' + token;
        }

        const options = { method, headers };
        if (body) options.body = JSON.stringify(body);

        try {
            const res = await fetch(this.baseUrl + path, options);
            const text = await res.text();
            let data;
            try {
                data = JSON.parse(text);
            } catch (e) {
                data = { success: false, message: 'Server error' };
            }
            return data;
        } catch (err) {
            console.error('API Error:', err);
            return { success: false, message: 'Network error. Please try again.' };
        }
    },

    get(path) { return this.request('GET', path); },
    post(path, body) { return this.request('POST', path, body); },
    put(path, body) { return this.request('PUT', path, body); },
    del(path) { return this.request('DELETE', path); }
};

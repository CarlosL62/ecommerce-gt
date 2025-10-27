import axios from "axios";

export const http = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
});

if (import.meta?.env?.DEV) {
  console.info("[HTTP] baseURL:", http.defaults.baseURL);
}

// Add token to headers automatically
http.interceptors.request.use((config) => {
    const token =
        localStorage.getItem("token") ||
        sessionStorage.getItem("token") ||
        localStorage.getItem("jwt") ||
        sessionStorage.getItem("jwt");
    if (token) config.headers.Authorization = `Bearer ${token}`;
    // Avoid Ngrok warning page in free mode
    config.headers["ngrok-skip-browser-warning"] = "true";
    return config;
});

// Optional: global error logging
http.interceptors.response.use(
    (res) => res,
    (err) => {
        const msg =
            err?.response?.data?.message ||
            err?.response?.data ||
            err?.message ||
            "Unexpected error";
        console.error("HTTP Error:", msg);
        return Promise.reject(err);
    }
);

export function authHeaders() {
    const t =
        localStorage.getItem("token") ||
        sessionStorage.getItem("token") ||
        localStorage.getItem("jwt") ||
        sessionStorage.getItem("jwt");
    return t ? { Authorization: `Bearer ${t}` } : {};
}

export default http;
import { defineStore } from "pinia";
import http from "../api/http";

export const useAuthStore = defineStore("auth", {
    state: () => ({
        token: localStorage.getItem("token") || null,
        user: null, // { id, name, email, role, active }
        loading: false,
        error: null,
    }),
    getters: {
        isAuthenticated: (s) => !!s.token,
        role: (s) => s.user?.role || null,
        name: (s) => s.user?.name || "",
    },
    actions: {
        async register({ name, email, password }) {
            this.loading = true;
            this.error = null;
            try {
                await http.post("/api/auth/register", { name, email, password });
            } catch (e) {
                this.error = e.response?.data?.message || "Error al registrar";
                throw e;
            } finally {
                this.loading = false;
            }
        },

        async login({ email, password }) {
            this.loading = true;
            this.error = null;
            try {
                const { data } = await http.post("/api/auth/login", { email, password });
                this.token = data.token;
                localStorage.setItem("token", this.token);
                await this.me();
            } catch (e) {
                this.error = e.response?.data?.message || "Credenciales inválidas";
                throw e;
            } finally {
                this.loading = false;
            }
        },

        async me() {
            if (!this.token) { this.user = null; return; }
            try {
                const { data } = await http.get("/api/auth/me");
                this.user = data;
            } catch {
                this.logout();
            }
        },

        logout() {
            this.token = null;
            this.user = null;
            localStorage.removeItem("token");
        },
    },
});
<script setup>
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "./stores/auth";

// Use the centralized Pinia auth store instead of local refs.
const router = useRouter();
const auth = useAuthStore();

// Single logout action that clears store + localStorage and redirects.
function logout() {
  auth.logout();
  router.push({ name: "login" });
}

// On mount, if there's a token (persisted) but no user loaded yet, fetch /me once.
onMounted(() => {
  if (auth.token && !auth.user) {
    auth.me();
  }
});

// Keep multiple tabs in sync: when token changes in another tab, update this one.
window.addEventListener("storage", (e) => {
  if (e.key === "token") {
    if (e.newValue) {
      auth.token = e.newValue;
      auth.me();
    } else {
      logout();
    }
  }
});
</script>

<template>
  <nav style="display:flex; gap:1rem; padding:.75rem; border-bottom:1px solid #ddd; align-items:center;">
    <router-link to="/">Inicio</router-link>

    <!-- Public links when not authenticated -->
    <template v-if="!auth.isAuthenticated">
      <router-link to="/login">Login</router-link>
      <router-link to="/register">Registro</router-link>
    </template>

    <!-- Private links when authenticated -->
    <template v-else>
      <!-- Admin-only link -->
      <router-link v-if="auth.role === 'ADMIN'" to="/admin/users">Admin: Usuarios</router-link>

      <span style="margin-left:auto;">
        <template v-if="auth.user">
          Logged in as <strong>{{ auth.name }}</strong> ({{ auth.role }})
        </template>
        <button @click="logout" style="margin-left:.75rem; padding:.4rem .7rem;">Cerrar sesión</button>
      </span>
    </template>
  </nav>

  <router-view />
</template>
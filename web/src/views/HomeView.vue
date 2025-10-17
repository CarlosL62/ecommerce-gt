<script setup>
// Use the centralized Pinia auth store instead of manual fetch/token handling
import { onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const auth = useAuthStore();

// Derived/computed state for simpler template bindings
const me = computed(() => auth.user);
const loading = computed(() => auth.loading);
const error = computed(() => auth.error);

// On mount, hydrate user once if a token exists but user is not yet loaded
onMounted(async () => {
  if (auth.token && !auth.user) {
    try {
      await auth.me();
    } catch {
      // If token is invalid/expired, store will clear it on me() failure
      router.replace({ name: "login", query: { redirect: "/" } });
    }
  }
});

// Single logout action routed through the store
function logout() {
  auth.logout();
  router.push({ name: "login" });
}
</script>

<template>
  <div class="wrap">
    <h1>Inicio</h1>
    <p v-if="loading">Cargando…</p>
    <p v-else-if="error" class="error">{{ error }}</p>
    <template v-else>
      <p v-if="me">Bienvenido, <strong>{{ me.name }}</strong> — Rol: <strong>{{ me.role }}</strong></p>
      <button @click="logout">Cerrar sesión</button>
    </template>
  </div>
</template>

<style scoped>
.wrap { max-width: 720px; margin: 2rem auto; display: grid; gap: 1rem; }
.error { color: #b00020; }
button { padding: .6rem .9rem; }
</style>
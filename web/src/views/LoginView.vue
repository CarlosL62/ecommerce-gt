<script setup>
// Using Pinia store + Axios instead of manual fetch
import { ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

// Local form state (kept minimal)
const email = ref("");
const password = ref("");

// Read flag to show "registered" banner when redirected from Register
const justRegistered = route.query.registered === "1";

// Submit handler: call store.login -> persist token -> redirect
async function login() {
  try {
    await auth.login({ email: email.value, password: password.value });
    router.push(route.query.redirect || "/");
  } catch {
    // auth.error already populated by the store
  }
}
</script>

<template>
  <div class="wrap">
    <h1>Ingresar</h1>

    <p v-if="justRegistered" class="ok">
      Cuenta creada correctamente. Ahora inicia sesión.
    </p>

    <form @submit.prevent="login">
      <label>Email <input v-model.trim="email" type="email" required /></label>
      <label>Contraseña <input v-model="password" type="password" required /></label>
      <button :disabled="auth.loading">{{ auth.loading ? "Ingresando..." : "Entrar" }}</button>
      <p v-if="auth.error" class="error">{{ auth.error }}</p>
    </form>

    <p>¿No tienes cuenta? <router-link to="/register">Regístrate</router-link></p>
  </div>
</template>

<style scoped>
.wrap { max-width: 420px; margin: 2rem auto; display: grid; gap: 1rem; }
form { display: grid; gap: .75rem; }
input, button { padding: .6rem; }
.error { color: #b00020; }
.ok { color: #0a7a28; background: #e6f6ea; padding: .5rem .75rem; border-radius: .375rem; }
</style>
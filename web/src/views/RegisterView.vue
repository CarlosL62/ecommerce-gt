<script setup>
// Using the Pinia auth store to handle register logic and error state
import { ref } from "vue";
import { useRouter } from "vue-router";
import { useAuthStore } from "../stores/auth";

const router = useRouter();
const auth = useAuthStore();

// Local reactive variables for the form
const name = ref("");
const email = ref("");
const password = ref("");

// Basic client-side validation before calling backend
function validate() {
  if (!name.value || !email.value || !password.value) {
    return "Todos los campos son obligatorios";
  }
  if (password.value.length < 6) {
    return "La contraseña debe tener al menos 6 caracteres";
  }
  return null;
}

// Submit handler: performs validation, then calls the store register action
async function register() {
  const err = validate();
  if (err) {
    auth.error = err; // single error source for UI
    return;
  }

  try {
    await auth.register({
      name: name.value,
      email: email.value,
      password: password.value,
    });
    // On success, redirect to login with success flag in query
    router.push({ name: "login", query: { registered: "1" } });
  } catch {
    // auth.error already contains backend error (handled by store)
  }
}
</script>

<template>
  <div class="wrap">
    <h1>Registro</h1>
    <form @submit.prevent="register">
      <label>
        Nombre
        <input v-model.trim="name" type="text" required />
      </label>

      <label>
        Email
        <input v-model.trim="email" type="email" required />
      </label>

      <label>
        Contraseña
        <input v-model="password" type="password" minlength="6" required />
      </label>

      <button :disabled="auth.loading">{{ auth.loading ? "Creando..." : "Crear cuenta" }}</button>

      <p v-if="auth.error" class="error">{{ auth.error }}</p>
    </form>

    <p>¿Ya tienes cuenta? <router-link to="/login">Inicia sesión</router-link></p>
  </div>
</template>

<style scoped>
.wrap { max-width: 420px; margin: 2rem auto; display: grid; gap: 1rem; }
form { display: grid; gap: .75rem; }
label { display: grid; gap: .25rem; font-size: .95rem; }
input { padding: .6rem; border: 1px solid #ddd; border-radius: .375rem; }
button { padding: .6rem .9rem; border: 0; border-radius: .375rem; cursor: pointer; }
.error { color: #b00020; }
</style>
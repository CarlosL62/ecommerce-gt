<script setup>
// App shell with a full‑width top bar. Uses Pinia auth store for state.
import {onMounted} from "vue";
import {useRouter} from "vue-router";
import {useAuthStore} from "./stores/auth";

const router = useRouter();
const auth = useAuthStore();

function logout() {
  auth.logout();
  router.push({name: "login"});
}

onMounted(() => {
  // Hydrate user once if there is a token but no user in memory
  if (auth.token && !auth.user) {
    auth.me();
  }
});

// Keep multiple tabs in sync with storage events (token added/removed)
window.addEventListener("storage", (e) => {
  if (e.key === "token") {
    if (e.newValue) {
      auth.token = e.newValue;
      auth.me();
    } else {
      auth.logout();
      router.push({name: "login"});
    }
  }
});
</script>

<template>
  <header class="appbar">
    <div class="appbar__left">
      <span class="brand">ECommerceGT</span>

      <nav class="nav">
        <!-- Public links (only when not authenticated) -->
        <template v-if="!auth.isAuthenticated">
          <router-link class="nav__link" to="/login">Login</router-link>
          <router-link class="nav__link" to="/register">Registro</router-link>
        </template>

        <!-- Private links: role-specific top bar -->
        <template v-else>
          <!-- ADMIN: show admin children routes directly -->
          <template v-if="auth.role === 'ADMIN'">
            <router-link class="nav__link" :to="{ name: 'admin-users' }">Empleados</router-link>
            <router-link class="nav__link" :to="{ name: 'admin-reports' }">Reportes</router-link>
          </template>

          <!-- USER: show user children routes directly -->
          <template v-else-if="auth.role === 'COMMON'">
            <router-link class="nav__link" :to="{ name: 'user-catalog' }">Catálogo</router-link>
            <router-link class="nav__link" :to="{ name: 'user-cart' }">Mi carrito</router-link>
            <router-link class="nav__link" :to="{ name: 'user-orders' }">Mis órdenes</router-link>
            <router-link class="nav__link" :to="{ name: 'user-publish' }">Mis productos</router-link>
          </template>

          <!-- MODERATOR: show moderation tools -->
          <template v-else-if="auth.role === 'MODERATOR'">
            <router-link class="nav__link" :to="{ name: 'mod-review' }">Revisión</router-link>
            <router-link class="nav__link" :to="{ name: 'mod-approved' }">Aprobados</router-link>
            <router-link class="nav__link" :to="{ name: 'mod-rejected' }">Rechazados</router-link>
            <router-link class="nav__link" :to="{ name: 'mod-users' }">Usuarios</router-link>
          </template>

          <!-- LOGISTICS: shipment workflow -->
          <template v-else-if="auth.role === 'LOGISTICS'">
            <router-link class="nav__link" :to="{ name: 'log-orders' }">Ordenes</router-link>
          </template>
        </template>
      </nav>
    </div>

    <div class="appbar__right">
      <template v-if="auth.isAuthenticated">
        <span class="who">{{ auth.name }} ({{ auth.role }})</span>
        <button class="btn" @click="logout">Cerrar sesión</button>
      </template>
    </div>
  </header>

  <main class="page">
    <router-view/>
  </main>
</template>

<style scoped>
/* --- Top application bar (full-width, stable, pro look) --- */
.appbar {
  position: sticky;
  top: 0;
  z-index: 50;
  width: 100%;
  background: #ffffff;
  border-bottom: 1px solid #eaeaea;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: .75rem 1rem;
  box-shadow: 0 0 0 1px rgba(0, 0, 0, .02), 0 1px 2px rgba(0, 0, 0, .04);
}

.appbar__left {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.appbar__right {
  display: flex;
  align-items: center;
  gap: .75rem;
}

.brand {
  font-weight: 700;
  letter-spacing: .2px;
  text-decoration: none;
  color: #111827;
}

.nav {
  display: flex;
  gap: .5rem;
}

.nav__link {
  padding: .38rem .66rem;
  border-radius: .375rem;
  text-decoration: none;
  color: #111827;
  transition: background-color .15s ease;
}

.nav__link:hover {
  background: #f3f4f6;
}

.nav__link.router-link-active {
  background: #eef2ff;
  color: #3730a3;
}

/* --- Right area (who + action) --- */
.who {
  font-size: .95rem;
  color: #374151;
}

.btn {
  padding: .44rem .8rem;
  border: none;
  background: #ef4444; /* red-500 */
  color: #ffffff;
  border-radius: .375rem;
  cursor: pointer;
  font-weight: 600;
  letter-spacing: .2px;
  box-shadow: 0 1px 1px rgba(0, 0, 0, .06);
  transition: background-color .15s ease, transform .05s ease;
}

.btn:hover {
  background: #dc2626; /* red-600 */
}

.btn:active {
  transform: translateY(1px);
}

/* --- Page area occupies the rest of the viewport --- */
.page {
  min-height: calc(100vh - 56px); /* ensures full-viewport feel under the appbar */
  padding: 1rem;
}

/* --- Small screens: keep the bar compact without jumping elements --- */
@media (max-width: 640px) {
  .brand {
    font-size: 1rem;
  }

  .nav__link {
    padding: .35rem .55rem;
  }

  .who {
    display: none;
  }

  /* keep it clean on very small devices */
}
</style>

<style>
/* Global reset to ensure the top bar spans the full viewport width */
html, body, #app {
  height: 100%;
}

body {
  margin: 0;
}

*, *::before, *::after {
  box-sizing: border-box;
}

/* Override Vite default that centers #app with max-width */
#app {
  max-width: none !important;
  padding: 0 !important;
  width: 100% !important;
}

/* Ensure the app bar truly bleeds edge-to-edge */
.appbar {
  left: 0;
  right: 0;
}
</style>
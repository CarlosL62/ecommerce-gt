<script setup>
/**
 * ModeratorLayout
 * - Provides a top navigation bar for moderator tools
 * - Hosts child routes via <router-view>
 * - Keeps styles scoped and consistent with the rest of the app
 */
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <section class="modwrap">
    <!-- Content -->
    <main class="content">
      <RouterView />
    </main>
  </section>
</template>

<style scoped>
.modwrap {
  display: grid;
  grid-template-rows: auto 1fr;
  min-height: 100vh;
  background: #f3f4f6; /* light gray app background */
}

/* Top bar */
.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: .75rem;
  padding: .75rem 1rem;
  background: #111827;       /* dark slate */
  color: #ffffff;
  border-bottom: 1px solid #0b1220;
}
.brand {
  display: inline-flex;
  align-items: center;
  gap: .5rem;
}
.brand .logo {
  font-size: 1.25rem;
  line-height: 1;
}
.brand h1 {
  font-size: 1.1rem;
  margin: 0;
  font-weight: 800;
}

/* Nav */
.nav {
  display: inline-flex;
  align-items: center;
  gap: .5rem;
}
.navlink {
  display: inline-flex;
  align-items: center;
  gap: .5rem;
  padding: .4rem .7rem;
  border-radius: .5rem;
  text-decoration: none;
  color: #e5e7eb;            /* gray-200 */
  border: 1px solid transparent;
  transition: background .2s ease, color .2s ease, border-color .2s ease;
}
.navlink:hover {
  background: #1f2937;       /* hover slate */
  color: #ffffff;
  border-color: #374151;
}
.navlink.active {
  background: #0ea5e9;       /* sky-500 */
  color: #0b1220;            /* deep text for contrast */
  border-color: #0ea5e9;
  font-weight: 800;
}

/* Content area */
.content {
  padding: 1rem;
  max-width: 1200px;
  margin: 0 auto;
}
</style>
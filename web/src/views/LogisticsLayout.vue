<script setup>
import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'
const auth = useAuthStore()
const user = computed(() => auth.user)
</script>

<template>
  <header class="topbar">
    <h1>Logística</h1>
    <div class="spacer" />
    <div class="who">{{ user?.name || user?.email }}</div>
  </header>
  <main class="page">
    <RouterView />
  </main>
</template>

<style scoped>
.topbar { display:flex; align-items:center; gap:1rem; padding:.75rem 1rem; border-bottom:1px solid #e5e7eb; }
.spacer { flex:1 }
.page { padding: 1rem; }
</style>
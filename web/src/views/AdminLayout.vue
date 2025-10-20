<script setup>
// Admin layout with a top navigation bar and nested <router-view/>
// Links target the admin child routes (users/reports)
</script>

<template>
  <div class="admin-layout">

    <main class="content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.admin-layout { display: grid; grid-template-rows: auto 1fr; min-height: 100vh; }
.content { padding: 1rem 1.25rem; min-height: 0; }
</style>
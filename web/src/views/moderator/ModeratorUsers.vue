<script setup>
/**
 * ModeratorUsers (list-only)
 * - Lists sellers (COMMON users)
 * - Allows to suspend / activate directly from the table
 */
import { ref, onMounted } from 'vue'
import http from '../../api/http'


const loading = ref(false)
const working = ref({})
const errorMsg = ref('')
const users = ref([])

async function load() {
  loading.value = true; errorMsg.value = ''
  try {
    const { data } = await http.get('/api/moderation/users', { params: { role: 'COMMON' } })
    users.value = Array.isArray(data) ? data : (Array.isArray(data?.items) ? data.items : [])
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || e?.message || 'No se pudieron cargar los usuarios'
  } finally {
    loading.value = false
  }
}

async function suspend(id) {
  working.value[id] = true; errorMsg.value = ''
  try {
    await http.patch(`/api/moderation/users/${id}/suspend`)
    await load()
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || e?.message || 'No se pudo suspender el usuario'
  } finally {
    working.value[id] = false
  }
}

async function activate(id) {
  working.value[id] = true; errorMsg.value = ''
  try {
    await http.patch(`/api/moderation/users/${id}/activate`)
    await load()
  } catch (e) {
    errorMsg.value = e?.response?.data?.message || e?.message || 'No se pudo activar el usuario'
  } finally {
    working.value[id] = false
  }
}

onMounted(load)
</script>

<template>
  <section class="wrap">
    <header class="head">
      <h2>Usuarios (sólo vendedores)</h2>
      <button class="btn" :disabled="loading" @click="load">Refrescar</button>
    </header>

    <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
    <p v-else-if="loading" class="muted">Cargando…</p>
    <p v-else-if="!users.length" class="muted">No hay usuarios para mostrar.</p>

    <table v-else class="table">
      <thead>
        <tr>
          <th>ID</th>
          <th>Nombre</th>
          <th>Email</th>
          <th>Rol</th>
          <th>Estado</th>
          <th>Acciones</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in users" :key="u.id">
          <td>{{ u.id }}</td>
          <td>{{ u.name }}</td>
          <td>{{ u.email }}</td>
          <td>{{ u.role }}</td>
          <td>
            <span :class="['badge', u.active ? 'on' : 'off']">{{ u.active ? 'Activo' : 'Suspendido' }}</span>
          </td>
          <td class="actions">
            <button v-if="u.active" class="warn" :disabled="working[u.id]" @click="suspend(u.id)">Suspender</button>
            <button v-else class="ok" :disabled="working[u.id]" @click="activate(u.id)">Activar</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>

<style scoped>
.wrap { display:grid; gap:1rem; }
.head { display:flex; align-items:center; justify-content:space-between; }
.btn { padding:.5rem .75rem; border-radius:.5rem; border:1px solid #d1d5db; background:#fff; cursor:pointer; }
.muted { color:#6b7280; }
.err { color:#b91c1c; white-space: pre-wrap; }
.table { width:100%; border-collapse: collapse; background:#fff; border:1px solid #e5e7eb; border-radius:.5rem; overflow:hidden; }
.table th, .table td { padding:.5rem .6rem; border-bottom:1px solid #f3f4f6; text-align:left; }
.table thead th { background:#f9fafb; font-weight:800; }
.badge { display:inline-block; padding:.15rem .5rem; border-radius:999px; font-size:.75rem; }
.badge.on { background:#dcfce7; color:#166534; border:1px solid #86efac; }
.badge.off { background:#fee2e2; color:#991b1b; border:1px solid #fecaca; }
.actions { display:flex; gap:.5rem; }
.warn { background:#fca5a5; color:#7f1d1d; border:1px solid #fecaca; padding:.35rem .6rem; border-radius:.5rem; cursor:pointer; }
.ok { background:#a7f3d0; color:#064e3b; border:1px solid #86efac; padding:.35rem .6rem; border-radius:.5rem; cursor:pointer; }
</style>
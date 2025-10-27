<script setup>
/**
 * ModeratorReview
 * - Lists products with status IN_REVIEW
 * - Actions: Approve, Reject
 */
import { ref, onMounted } from 'vue'
const API = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'

// Fallback placeholder for broken/missing images
const PLACEHOLDER = "data:image/svg+xml;utf8,\
<svg xmlns='http://www.w3.org/2000/svg' width='800' height='600' viewBox='0 0 800 600'>\
  <rect width='100%' height='100%' fill='%23f3f4f6'/>\
  <text x='50%' y='50%' dominant-baseline='middle' text-anchor='middle' fill='%239ca3af' font-size='24' font-family='system-ui, -apple-system, Segoe UI, Roboto, Ubuntu, Cantarell, Noto Sans, Helvetica, Arial'>Sin imagen</text>\
</svg>";

function onImgErr(e) {
  if (e && e.target) e.target.src = PLACEHOLDER;
}

function getToken() {
  return (
    localStorage.getItem('token') ||
    sessionStorage.getItem('token') ||
    localStorage.getItem('jwt') ||
    sessionStorage.getItem('jwt') ||
    ''
  )
}

function authHeaders() {
  const t = getToken()
  return {
    'Content-Type': 'application/json',
    ...(t ? { 'Authorization': `Bearer ${t}` } : {})
  }
}

const loading = ref(false)
const errorMsg = ref('')
const items = ref([])

if (!getToken()) {
  errorMsg.value = 'No auth token found. Please log in as MODERATOR.'
}

async function load() {
  loading.value = true
  errorMsg.value = ''
  try {
    const res = await fetch(`${API}/api/moderation/products?status=IN_REVIEW`, { headers: authHeaders() })
    if (!res.ok) {
      const body = await res.text()
      throw new Error(`HTTP ${res.status} - ${body || 'Access denied / Invalid request'}`)
    }
    items.value = await res.json()
  } catch (e) {
    errorMsg.value = String(e)
  } finally {
    loading.value = false
  }
}

async function approve(id) {
  try {
    const res = await fetch(`${API}/api/moderation/products/${id}/approve`, { method: 'PATCH', headers: authHeaders() })
    if (!res.ok) {
      const body = await res.text()
      throw new Error(`HTTP ${res.status} - ${body || 'Access denied / Invalid request'}`)
    }
    await load()
  } catch (e) { errorMsg.value = String(e) }
}

async function reject(id) {
  try {
    const res = await fetch(`${API}/api/moderation/products/${id}/reject`, { method: 'PATCH', headers: authHeaders() })
    if (!res.ok) {
      const body = await res.text()
      throw new Error(`HTTP ${res.status} - ${body || 'Access denied / Invalid request'}`)
    }
    await load()
  } catch (e) { errorMsg.value = String(e) }
}

onMounted(load)
</script>

<template>
  <section class="wrap">
    <header class="head">
      <h2>Revisión pendiente</h2>
      <button class="btn" :disabled="loading" @click="load">Refrescar</button>
    </header>

    <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
    <p v-else-if="loading" class="muted">Cargando…</p>
    <p v-else-if="!items.length" class="muted">No hay productos en revisión.</p>

    <ul v-else class="grid">
      <li v-for="p in items" :key="p.id" class="card">
        <img class="thumb" :src="p.imageUrl || PLACEHOLDER" alt="Imagen del producto" @error="onImgErr" />
        <div class="body">
          <h3>{{ p.name }}</h3>
          <p class="desc">{{ p.description }}</p>
          <p class="meta">
            <span class="pill">{{ p.category }}</span>
            <span class="pill">{{ p.condition }}</span>
            <span class="pill">Q {{ Number(p.price).toFixed(2) }}</span>
            <span class="pill">Stock: {{ p.stock }}</span>
          </p>
        </div>
        <div class="actions">
          <button class="approve" @click="approve(p.id)">Aprobar</button>
          <button class="reject" @click="reject(p.id)">Rechazar</button>
        </div>
      </li>
    </ul>
  </section>
</template>
<style scoped>
.wrap { display: grid; gap: 1rem; }
.head { display:flex; align-items:center; justify-content:space-between; }
.btn { padding:.5rem .75rem; border-radius:.5rem; border:1px solid #d1d5db; background:#fff; cursor:pointer; }
.muted { color:#6b7280; }
.err { color: #b91c1c; white-space: pre-wrap; }
.grid { display:grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: .75rem; }
.card { display:grid; grid-template-rows: auto auto auto; background:#fff; border:1px solid #e5e7eb; border-radius:.75rem; overflow:hidden; }
.thumb { width: 100%; height: 180px; object-fit: cover; display: block; background: #f3f4f6; }
.body { padding:.75rem; }
.desc { color:#1f2937; margin:.25rem 0 .5rem; display:-webkit-box; -webkit-line-clamp:4; -webkit-box-orient:vertical; overflow:hidden; }
.meta { display:flex; flex-wrap:wrap; gap:.35rem; }
.pill { background:#f3f4f6; border:1px solid #e5e7eb; padding:.15rem .5rem; border-radius:999px; font-size:.75rem; }
.actions { display:flex; gap:.5rem; padding:.75rem; border-top:1px solid #f3f4f6; }
.approve { background:#10b981; color:#092; border:none; padding:.45rem .7rem; border-radius:.5rem; cursor:pointer; }
.reject { background:#ef4444; color:#fff; border:none; padding:.45rem .7rem; border-radius:.5rem; cursor:pointer; }

.body h3 { margin: 0 0 .25rem; line-height: 1.25; }
</style>
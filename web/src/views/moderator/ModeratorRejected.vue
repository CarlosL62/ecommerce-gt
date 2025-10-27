<script setup>
/** Lists rejected products; allows to reopen to review */
import { ref, onMounted } from 'vue'
const API = import.meta.env.VITE_API_BASE ?? 'http://localhost:8080'

const PLACEHOLDER = '/img/placeholder.png'
const items = ref([])
const loading = ref(false)
const errorMsg = ref('')

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

if (!getToken()) {
  errorMsg.value = 'No auth token found. Please log in as MODERATOR.'
}

async function load() {
  loading.value = true; errorMsg.value = ''
  try {
    const res = await fetch(`${API}/api/moderation/products?status=REJECTED`, { headers: authHeaders() })
    if (!res.ok) {
      const body = await res.text()
      throw new Error(`HTTP ${res.status} - ${body || 'Access denied / Invalid request'}`)
    }
    items.value = await res.json()
  } catch (e) { errorMsg.value = String(e) } finally { loading.value = false }
}

async function reopen(id) {
  try {
    const res = await fetch(`${API}/api/moderation/products/${id}/reopen`, { method: 'PATCH', headers: authHeaders() })
    if (!res.ok) {
      const body = await res.text()
      throw new Error(`HTTP ${res.status} - ${body || 'Access denied / Invalid request'}`)
    }
    await load()
  } catch (e) { errorMsg.value = String(e) }
}

function onImgErr(e) {
  e.target.src = PLACEHOLDER
}

onMounted(load)
</script>

<template>
  <section class="wrap">
    <header class="head">
      <h2>Rechazados</h2>
      <button class="btn" :disabled="loading" @click="load">Refrescar</button>
    </header>

    <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
    <p v-else-if="loading" class="muted">Cargando…</p>
    <p v-else-if="!items.length" class="muted">No hay productos rechazados.</p>

    <div v-else class="grid">
      <article v-for="p in items" :key="p.id" class="card">
        <img class="thumb" :src="p.imageUrl || PLACEHOLDER" alt="Imagen del producto" @error="onImgErr" />
        <div class="body">
          <h3>{{ p.name }}</h3>
          <p class="desc">{{ p.description }}</p>
          <p class="price">Q {{ Number(p.price).toFixed(2) }}</p>
          <button class="link" @click="reopen(p.id)">Reabrir revisión</button>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.wrap { display: grid; gap: 1rem; }
.head { display: flex; align-items: center; justify-content: space-between; }
.btn { padding: .5rem .75rem; border-radius: .5rem; border: 1px solid #d1d5db; background: #fff; cursor: pointer; }
.muted { color: #6b7280; }
.err { color: #b91c1c; white-space: pre-wrap; }
.grid { display: grid; gap: 1rem; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); }
.card { display: grid; grid-template-rows: auto auto; border: 1px solid #e5e7eb; border-radius: .5rem; overflow: hidden; background: #fff; }
.thumb { width: 100%; height: 180px; object-fit: cover; background: #f3f4f6; }
.body { padding: .75rem; display: flex; flex-direction: column; gap: .25rem; }
.body h3 { font-weight: 700; margin: 0; color: #111827; }
.desc { color: #1f2937; -webkit-line-clamp: 3; -webkit-box-orient: vertical; display: -webkit-box; overflow: hidden; }
.price { font-weight: 600; color: #374151; }
.link { align-self: start; background: transparent; border: 1px solid #e5e7eb; padding: .3rem .6rem; border-radius: .5rem; cursor: pointer; }
</style>
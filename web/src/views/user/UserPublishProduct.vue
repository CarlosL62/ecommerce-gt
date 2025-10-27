<script setup>
// User product portal:
// - Lists current user's products (/api/products/mine)
// - Floating action button to create a new product
// - Reuses the same editor for create/edit (edit calls PUT /api/products/{id})
// - Shows friendly feedback messages

import { ref, reactive, computed, onMounted } from 'vue'

// ---- config ----
const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080'

// ---- options ----
const conditions = [
  { label: 'Nuevo', value: 'NEW' },
  { label: 'Usado', value: 'USED' },
]
const categories = [
  { label: 'Tecnología', value: 'TECHNOLOGY' },
  { label: 'Hogar', value: 'HOME' },
  { label: 'Académico', value: 'ACADEMIC' },
  { label: 'Personal', value: 'PERSONAL' },
  { label: 'Decoración', value: 'DECORATION' },
  { label: 'Otro', value: 'OTHER' },
]

// ---- list state ----
const items = ref([])               // products of current user
const listLoading = ref(false)
const listError = ref('')

// ---- editor state ----
const showEditor = ref(false)
const mode = ref('create')          // 'create' | 'edit'
const editingId = ref(null)

const form = reactive({
  name: '',
  description: '',
  imageUrl: '',
  price: '',
  stock: 1,
  condition: 'NEW',
  category: 'TECHNOLOGY',
})

const loading = ref(false)
const apiError = ref('')
const apiOk = ref('')

// ---- validation ----
function isPositiveNumber(v) {
  if (v === '' || v === null || v === undefined) return false
  const n = Number(v)
  return Number.isFinite(n) && n > 0
}
function isIntGe1(v) {
  if (v === '' || v === null || v === undefined) return false
  const n = Number(v)
  return Number.isInteger(n) && n >= 1
}

const errors = computed(() => {
  const e = {}
  if (!form.name || form.name.trim().length < 3) e.name = 'Mínimo 3 caracteres'
  else if (form.name.trim().length > 80) e.name = 'Máximo 80 caracteres'

  if (!form.description || form.description.trim().length < 10) e.description = 'Mínimo 10 caracteres'
  else if (form.description.trim().length > 500) e.description = 'Máximo 500 caracteres'

  if (!form.imageUrl || form.imageUrl.trim().length === 0) e.imageUrl = 'Obligatorio'

  if (!isPositiveNumber(form.price)) e.price = 'Debe ser un número mayor a 0'
  if (!isIntGe1(form.stock)) e.stock = 'Debe ser un entero mayor o igual a 1'
  if (!form.condition) e.condition = 'Obligatorio'
  if (!form.category) e.category = 'Obligatorio'
  return e
})

const isValid = computed(() => Object.keys(errors.value).length === 0)

function resetForm() {
  form.name = ''
  form.description = ''
  form.imageUrl = ''
  form.price = ''
  form.stock = 1
  form.condition = 'NEW'
  form.category = 'TECHNOLOGY'
  apiError.value = ''
  apiOk.value = ''
  editingId.value = null
}

function openCreate() {
  mode.value = 'create'
  resetForm()
  showEditor.value = true
}
function openEdit(item) {
  mode.value = 'edit'
  editingId.value = item.id
  form.name = item.name
  form.description = item.description
  form.imageUrl = item.imageUrl
  form.price = item.price
  form.stock = item.stock
  form.condition = item.condition
  form.category = item.category
  apiError.value = ''
  apiOk.value = ''
  showEditor.value = true
}
function closeEditor() {
  showEditor.value = false
}

// ---- API helpers ----
function getToken() {
  return localStorage.getItem('token') || ''
}
async function apiGet(url) {
  const res = await fetch(url, {
    headers: { 'Authorization': `Bearer ${getToken()}` }
  })
  const ct = res.headers.get('Content-Type') || ''
  const isJson = ct.includes('application/json')
  const data = isJson ? await res.json() : await res.text()
  if (!res.ok) {
    const err = new Error(isJson ? (data.message || JSON.stringify(data)) : String(data))
    err.status = res.status
    throw err
  }
  return data
}
async function apiSend(url, method, payload) {
  const res = await fetch(url, {
    method,
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${getToken()}`
    },
    body: JSON.stringify(payload)
  })
  const ct = res.headers.get('Content-Type') || ''
  const isJson = ct.includes('application/json')
  const data = isJson ? await res.json() : await res.text()
  if (!res.ok) throw new Error(isJson ? (data.message || JSON.stringify(data)) : String(data))
  return data
}

// ---- list loader ----
async function loadMine() {
  listError.value = ''
  listLoading.value = true
  try {
    const data = await apiGet(`${API_BASE}/api/products/mine`)
    items.value = Array.isArray(data) ? data : []
  } catch (e) {
    // If unauthorized/forbidden/not found, show empty list instead of error
    if (e && (e.status === 401 || e.status === 403 || e.status === 404)) {
      items.value = []
      listError.value = ''
    } else {
      listError.value = e?.message || 'Error al cargar productos'
    }
  } finally {
    listLoading.value = false
  }
}

// ---- submit (create/edit) ----
async function onSubmit() {
  apiError.value = ''
  apiOk.value = ''
  if (!isValid.value) {
    apiError.value = 'Revisa los campos marcados.'
    return
  }
  const payload = {
    name: form.name.trim(),
    description: form.description.trim(),
    imageUrl: form.imageUrl.trim(),
    price: Number(form.price),
    stock: Number(form.stock),
    condition: form.condition,
    category: form.category,
  }
  loading.value = true
  try {
    if (mode.value === 'create') {
      await apiSend(`${API_BASE}/api/products`, 'POST', payload)
      apiOk.value = 'Producto enviado a revisión.'
    } else {
      // NOTE: backend PUT /api/products/{id} must exist; front is ready for it.
      await apiSend(`${API_BASE}/api/products/${editingId.value}`, 'PUT', payload)
      apiOk.value = 'Producto actualizado (en revisión).'
    }
    await loadMine()
    closeEditor()
  } catch (err) {
    apiError.value = err?.message || 'Error al guardar.'
  } finally {
    loading.value = false
  }
}

onMounted(loadMine)
</script>

<template>
  <section class="wrap">
    <header class="header">
      <h1>Mis productos</h1>
      <button class="fab" title="Nuevo producto" @click="openCreate">＋</button>
    </header>

    <!-- list -->
    <div v-if="listLoading" class="muted">Cargando…</div>
    <div v-else-if="listError" class="err">⚠️ {{ listError }}</div>

    <div v-else class="grid-cards">
      <div v-if="items.length === 0" class="empty">
        No hay productos todavía.
      </div>
      <article v-for="it in items" :key="it.id" class="card item">
        <img class="thumb" :src="it.imageUrl" alt="" />
        <div class="meta">
          <div class="row">
            <h3 class="name">{{ it.name }}</h3>
            <span class="price">Q {{ Number(it.price).toFixed(2) }}</span>
          </div>
          <div class="row">
            <span class="badge" :data-status="it.status">{{ it.status }}</span>
            <small class="stock">Stock: {{ it.stock }}</small>
          </div>
        </div>
        <button class="icon edit" title="Editar" @click="openEdit(it)">✏️</button>
      </article>
    </div>

    <!-- editor modal/drawer -->
    <div class="overlay" v-if="showEditor" @click.self="closeEditor">
      <div class="drawer">
        <header class="drawer-head">
          <h2>{{ mode === 'create' ? 'Publicar producto' : 'Editar producto' }}</h2>
          <button class="icon close" @click="closeEditor">✕</button>
        </header>

        <div class="grid">
          <label>
            <span>Nombre</span>
            <input class="input" v-model.trim="form.name" type="text" placeholder="Ej. Camiseta edición limitada" />
            <small v-if="errors.name" class="err">{{ errors.name }}</small>
          </label>

          <label class="span-2">
            <span>Descripción</span>
            <textarea class="input" v-model.trim="form.description" rows="4" placeholder="Describe el producto" />
            <small v-if="errors.description" class="err">{{ errors.description }}</small>
          </label>

          <label class="span-2">
            <span>URL de imagen</span>
            <input class="input" v-model.trim="form.imageUrl" type="url" placeholder="https://..." />
            <small v-if="errors.imageUrl" class="err">{{ errors.imageUrl }}</small>
          </label>

          <label>
            <span>Precio</span>
            <input class="input" v-model="form.price" inputmode="decimal" type="number" step="0.01" min="0.01" placeholder="0.00" />
            <small v-if="errors.price" class="err">{{ errors.price }}</small>
          </label>

          <label>
            <span>Stock</span>
            <input class="input" v-model="form.stock" inputmode="numeric" type="number" step="1" min="1" placeholder="1" />
            <small v-if="errors.stock" class="err">{{ errors.stock }}</small>
          </label>

          <label>
            <span>Estado</span>
            <select class="input" v-model="form.condition">
              <option v-for="c in conditions" :key="c.value" :value="c.value">{{ c.label }}</option>
            </select>
            <small v-if="errors.condition" class="err">{{ errors.condition }}</small>
          </label>

          <label>
            <span>Categoría</span>
            <select class="input" v-model="form.category">
              <option v-for="c in categories" :key="c.value" :value="c.value">{{ c.label }}</option>
            </select>
            <small v-if="errors.category" class="err">{{ errors.category }}</small>
          </label>
        </div>

        <div class="actions">
          <button class="btn subtle" type="button" :disabled="loading" @click="resetForm">Limpiar</button>
          <button class="btn primary" type="button" :disabled="!isValid || loading" @click="onSubmit">
            {{ loading ? (mode === 'create' ? 'Publicando…' : 'Guardando…') : (mode === 'create' ? 'Publicar' : 'Guardar cambios') }}
          </button>
        </div>

        <p v-if="apiOk" class="ok">✅ {{ apiOk }}</p>
        <p v-if="apiError" class="err">⚠️ {{ apiError }}</p>
      </div>
    </div>
  </section>
</template>

<style scoped>
/* Layout */
.wrap { display: grid; gap: 1rem; }
.header { display: flex; align-items: center; justify-content: space-between; }
.header h1 { font-size: 2rem; margin: .5rem 0; }

/* Cards grid */
.grid-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}
.card {
  border: 1px solid #e5e7eb;
  border-radius: .75rem;
  background: #ffffff;
  box-shadow: 0 1px 2px rgba(0,0,0,.06);
}
.item { position: relative; display: grid; grid-template-columns: 120px 1fr auto; gap: .75rem; padding: .75rem; }
.thumb { width: 120px; height: 90px; object-fit: cover; border-radius: .5rem; border: 1px solid #e5e7eb; background: #f3f4f6; }
.meta { display: grid; gap: .25rem; }
.row { display: flex; align-items: center; justify-content: space-between; gap: .5rem; }
.name { font-size: 1rem; font-weight: 700; color: #111827; }
.price { font-weight: 700; color: #0f766e; } /* teal-ish */
.badge { font-size: .75rem; padding: .2rem .5rem; border-radius: 999px; border: 1px solid #e5e7eb; color: #374151; background: #f9fafb; }
.badge[data-status="IN_REVIEW"] { color: #a16207; background: #fef3c7; border-color: #f59e0b; }
.badge[data-status="APPROVED"]  { color: #065f46; background: #d1fae5; border-color: #10b981; }
.badge[data-status="REJECTED"]  { color: #7f1d1d; background: #fee2e2; border-color: #ef4444; }
.stock { color: #6b7280; }

/* Action buttons */
.icon { border: 1px solid #e5e7eb; background: #fff; color: #111827; border-radius: .5rem; padding: .4rem .5rem; cursor: pointer; }
.icon.edit:hover { background: #f3f4f6; }
.icon.close { font-weight: 700; }
.edit { align-self: start; }

/* Floating action button */
.fab {
  width: 44px; height: 44px;
  border-radius: 999px;
  border: none;
  background: #3b82f6;
  color: #fff;
  font-size: 1.4rem;
  display: inline-grid; place-items: center;
  box-shadow: 0 6px 14px rgba(59,130,246,.3);
  cursor: pointer;
}
.fab:hover { background: #2563eb; }

/* Drawer/Modal */
.overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,.45);
  display: grid; place-items: center;
}
.drawer {
  width: min(900px, 92vw);
  max-height: 86vh;
  overflow: auto;
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: .9rem;
  padding: 1rem;
  box-shadow: 0 20px 70px rgba(0,0,0,.25);
}
.drawer-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: .5rem; }

/* Editor form (same styles as before) */
.grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: .8rem 1rem; }
.span-2 { grid-column: span 2; }
label { display: grid; gap: .35rem; }
label > span { font-weight: 600; color: #111827; }
.input {
  padding: .6rem .7rem;
  border: 1px solid #d1d5db;
  border-radius: .5rem;
  background: #ffffff;
  color: #111827;
}
.input:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,.2); }
.actions { display: flex; gap: .5rem; justify-content: end; margin-top: .5rem; }
.btn {
  padding: .52rem .9rem;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  color: #111827;
  border-radius: .5rem;
  cursor: pointer;
  font-weight: 600;
}
.btn.subtle { background: #f9fafb; }
.btn.primary { background: #3b82f6; color: #fff; border-color: #2563eb; }
.btn:disabled { opacity: .6; cursor: not-allowed; }

.ok { color: #16a34a; font-weight: 600; }
.err { color: #dc2626; font-weight: 600; }

.empty { color: #6b7280; border: 1px dashed #d1d5db; border-radius: .75rem; padding: 1.25rem; background: #fff; }

@media (max-width: 860px) {
  .grid-cards { grid-template-columns: 1fr; }
  .item { grid-template-columns: 100px 1fr auto; }
}
</style>
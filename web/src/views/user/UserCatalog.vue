<script setup>
// User Catalog
// - Loads approved products from /api/catalog (requires token if your backend protects it)
// - Shows product cards with image, name, price, condition
// - Details modal with full description and Add to cart
// - Add to cart posts to /api/cart/items { productId, qty }

import { ref, reactive, computed, onMounted } from 'vue'
import http from '../../api/http'

// --- state ---
const products = ref([])
const loading = ref(false)
const error = ref('')

const showDetails = ref(false)
const current = ref(null) // selected product
const qty = ref(1)

const feedback = reactive({ ok: '', err: '' })

async function loadCatalog() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await http.get('/api/products/catalog')
    const list = Array.isArray(data) ? data : []
    products.value = list
  } catch (e) {
    if (e && (e.response?.status === 401 || e.response?.status === 403)) {
      products.value = []
      error.value = 'Inicia sesión para ver el catálogo.'
    } else {
      error.value = e?.response?.data?.message || e?.message || 'Error al cargar catálogo'
    }
  } finally {
    loading.value = false
  }
}

function clampQtyToStock() {
  const p = current.value
  if (!p) return
  if (qty.value < 1) qty.value = 1
  if (p.stock > 0 && qty.value > p.stock) qty.value = p.stock
}

function openDetails(p) {
  current.value = p
  qty.value = 1
  feedback.ok = ''
  feedback.err = ''
  showDetails.value = true
  setTimeout(clampQtyToStock, 0)
}
function closeDetails() {
  showDetails.value = false
}

const canAdd = computed(() => {
  const p = current.value
  const q = Number(qty.value) || 0
  return !!p && p.stock > 0 && q >= 1 && q <= p.stock
})

async function addToCart() {
  const p = current.value
  const q = Number(qty.value) || 0
  if (!p || p.stock <= 0 || q < 1 || q > p.stock) {
    feedback.err = 'Cantidad inválida o sin stock.'
    return
  }
  feedback.ok = ''
  feedback.err = ''
  try {
    await http.post('/api/cart/items', {
      productId: p.id,
      qty: q
    })
    feedback.ok = 'Producto agregado al carrito.'
  } catch (e) {
    feedback.err = e?.response?.data?.message || e?.message || 'No se pudo agregar al carrito.'
  }
}

onMounted(loadCatalog)
</script>

<template>
  <section class="wrap">
    <header class="header">
      <h1>Catálogo</h1>
    </header>

    <div v-if="loading" class="muted">Cargando…</div>
    <div v-else-if="error" class="err">⚠️ {{ error }}</div>

    <div v-else class="grid-cards">
      <div v-if="products.length === 0" class="empty">No hay productos disponibles.</div>

      <article v-for="p in products" :key="p.id" class="card item" @click="openDetails(p)">
        <img class="thumb" :src="p.imageUrl" alt="" />
        <div class="meta">
          <h3 class="name">{{ p.name }}</h3>
          <p class="desc">{{ p.description }}</p>
          <div class="row">
            <span class="price">Q {{ Number(p.price).toFixed(2) }}</span>
            <div class="chips">
              <span class="chip">{{ p.condition === 'NEW' ? 'Nuevo' : 'Usado' }}</span>
              <span class="chip" :class="{ danger: p.stock === 0 }">{{ p.stock === 0 ? 'Agotado' : `Stock: ${p.stock}` }}</span>
            </div>
          </div>
        </div>
      </article>
    </div>

    <!-- Details modal -->
    <div class="overlay" v-if="showDetails" @click.self="closeDetails">
      <div class="drawer">
        <header class="drawer-head">
          <h2>{{ current?.name }}</h2>
          <button class="icon close" @click="closeDetails">✕</button>
        </header>
        <div class="detail">
          <img class="big" :src="current?.imageUrl" alt="" />
          <div class="info">
            <p class="desc">{{ current?.description }}</p>
            <p class="muted">Categoría: {{ current?.category }}</p>
            <p class="muted">Estado: {{ current?.condition === 'NEW' ? 'Nuevo' : 'Usado' }}</p>
            <p class="stock">Stock disponible: {{ current?.stock }}</p>
            <p class="price">Q {{ current ? Number(current.price).toFixed(2) : '0.00' }}</p>

            <div class="qty">
              <label>
                <span>Cantidad</span>
                <input
                  class="input"
                  v-model.number="qty"
                  type="number"
                  min="1"
                  :max="current?.stock || 1"
                  step="1"
                  @input="clampQtyToStock"
                />
              </label>
              <button class="btn primary" :disabled="!canAdd" @click="addToCart">{{ current?.stock > 0 ? 'Agregar al carrito' : 'Sin stock' }}</button>
            </div>

            <p v-if="feedback.ok" class="ok">✅ {{ feedback.ok }}</p>
            <p v-if="feedback.err" class="err">⚠️ {{ feedback.err }}</p>
          </div>
        </div>
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
.grid-cards { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 1rem; }
.card { border: 1px solid #e5e7eb; border-radius: .75rem; background: #ffffff; box-shadow: 0 1px 2px rgba(0,0,0,.06); cursor: pointer; }
.item { display: grid; grid-template-columns: 180px 1fr; gap: .75rem; padding: .75rem; }
.thumb { width: 180px; height: 130px; object-fit: cover; border-radius: .5rem; border: 1px solid #e5e7eb; background: #f3f4f6; }
.meta { display: grid; gap: .4rem; }
.name { font-size: 1.05rem; font-weight: 700; color: #111827; }
.desc { color: #4b5563; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }
.row { display: flex; align-items: center; justify-content: space-between; gap: .5rem; margin-top: .25rem; }
.chips { display: inline-flex; gap: .35rem; align-items: center; }
.price { font-weight: 800; color: #0f766e; }
.chip { border: 1px solid #e5e7eb; background: #f9fafb; color: #374151; padding: .2rem .5rem; border-radius: 999px; font-size: .75rem; }

.chip.danger { border-color: #fecaca; background: #fee2e2; color: #991b1b; }

/* Modal */
.overlay { position: fixed; inset: 0; background: rgba(0,0,0,.45); display: grid; place-items: center; }
.drawer { width: min(980px, 92vw); max-height: 86vh; overflow: auto; background: #ffffff; border: 1px solid #e5e7eb; border-radius: .9rem; padding: 1rem; box-shadow: 0 20px 70px rgba(0,0,0,.25); }
.drawer-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: .5rem; }
.detail { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
.big { width: 100%; height: 320px; object-fit: cover; border-radius: .5rem; border: 1px solid #e5e7eb; background: #f3f4f6; }
.info { display: grid; gap: .5rem; }
.stock { color: #6b7280; }

/* Inputs + buttons */
.input { padding: .6rem .7rem; border: 1px solid #d1d5db; border-radius: .5rem; background: #ffffff; color: #111827; }
.input:focus { outline: none; border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,.2); }
.qty { display: flex; align-items: end; gap: .75rem; margin-top: .5rem; }
.btn { padding: .52rem .9rem; border: 1px solid #e5e7eb; background: #ffffff; color: #111827; border-radius: .5rem; cursor: pointer; font-weight: 600; }
.btn.primary { background: #3b82f6; color: #fff; border-color: #2563eb; }
.btn:disabled { opacity: .6; cursor: not-allowed; }

/* Feedback + empty */
.ok { color: #16a34a; font-weight: 600; }
.err { color: #dc2626; font-weight: 600; }
.muted { color: #6b7280; }
.empty { color: #6b7280; border: 1px dashed #d1d5db; border-radius: .75rem; padding: 1.25rem; background: #fff; }

@media (max-width: 1024px) { .grid-cards { grid-template-columns: repeat(2, minmax(0, 1fr)); } .item { grid-template-columns: 140px 1fr; } .thumb { width: 140px; height: 100px; } }
@media (max-width: 680px)  { .grid-cards { grid-template-columns: 1fr; } .detail { grid-template-columns: 1fr; } }
</style>
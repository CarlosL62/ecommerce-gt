<script setup>
// User Orders
// - Loads current user's orders from /api/orders/mine
// - Shows status, date, total
// - Expand row to see line items
// - Friendly empty/error states

import { ref, reactive, onMounted, computed } from 'vue'
import http from '../../api/http'

// --- helpers ---
function asMoney (n) {
  const num = Number(n || 0)
  return `Q ${num.toFixed(2)}`
}
function fmtDate (iso) {
  try {
    const d = new Date(iso)
    if (Number.isNaN(d.getTime())) return iso || ''
    return d.toLocaleString()
  } catch { return iso || '' }
}

// --- state ---
const loading = ref(false)
const error = ref('')
const orders = ref([]) // [{ id, createdAt, status, total, items:[{product:{name,imageUrl,price}, qty, lineTotal}]}]
const expanded = reactive({}) // { [orderId]: boolean }

const hasOrders = computed(() => Array.isArray(orders.value) && orders.value.length > 0)

// --- load ---
async function loadOrders () {
  loading.value = true
  error.value = ''
  try {
    const { data } = await http.get('/api/orders/mine')
    orders.value = Array.isArray(data) ? data : (Array.isArray(data?.orders) ? data.orders : [])
  } catch (e) {
    const status = e?.response?.status
    if (status === 401 || status === 403 || status === 404) {
      orders.value = []
      error.value = ''
    } else {
      error.value = e?.response?.data?.message || e?.message || 'Error al cargar tus pedidos'
    }
  } finally {
    loading.value = false
  }
}

function toggle (id) {
  expanded[id] = !expanded[id]
}

onMounted(loadOrders)
</script>

<template>
  <section class="wrap">
    <header class="head">
      <h1>Mis pedidos</h1>
      <button class="btn subtle" @click="loadOrders">Refrescar</button>
    </header>

    <div v-if="loading" class="muted">Cargando…</div>
    <div v-else-if="error" class="err">⚠️ {{ error }}</div>

    <div v-else>
      <div v-if="!hasOrders" class="empty">
        Aún no tienes pedidos.
      </div>

      <div v-else class="list">
        <article v-for="o in orders" :key="o.id" class="card order">
          <header class="o-head" @click="toggle(o.id)">
            <div class="left">
              <h3 class="oid">#{{ o.id }}</h3>
              <span class="odate">{{ fmtDate(o.createdAt || o.created_at) }}</span>
            </div>
            <div class="right">
              <span class="badge" :data-status="o.status">{{ o.status }}</span>
              <span class="ototal">{{ asMoney(o.subtotal ?? o.total ?? o.amount ?? 0) }}</span>
              <button class="icon chevron" :aria-expanded="!!expanded[o.id]">{{ expanded[o.id] ? '▴' : '▾' }}</button>
            </div>
          </header>

          <div v-if="expanded[o.id]" class="o-body">
            <table class="lines">
              <thead>
                <tr>
                  <th style="width:72px;">Imagen</th>
                  <th>Producto</th>
                  <th class="num">Precio</th>
                  <th class="num">Cant.</th>
                  <th class="num">Subtotal</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(it, idx) in (o.items || o.lines || [])" :key="idx">
                  <td><img class="thumb" :src="it.product?.imageUrl || it.imageUrl" alt="" /></td>
                  <td class="pname">{{ it.product?.name || it.name }}</td>
                  <td class="num">{{ asMoney(it.unitPrice ?? it.product?.price ?? it.price ?? 0) }}</td>
                  <td class="num">{{ it.qty ?? it.quantity ?? 0 }}</td>
                  <td class="num">{{ asMoney(it.lineTotal ?? (Number(it.unitPrice ?? it.product?.price ?? it.price ?? 0) * Number(it.qty ?? it.quantity ?? 0))) }}</td>
                </tr>
              </tbody>
              <tfoot>
                <tr>
                  <td colspan="4" class="num label">Total</td>
                  <td class="num value">{{ asMoney(o.subtotal ?? o.total ?? o.amount ?? 0) }}</td>
                </tr>
              </tfoot>
            </table>
            <p class="hint">Recuerda: el vendedor tiene hasta 5 días para realizar la entrega.</p>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<style scoped>
.wrap { display: grid; gap: 1rem; }
.head { display: flex; align-items: center; gap: .75rem; }
.head h1 { font-size: 2rem; margin: 0; }
.btn { padding: .52rem .9rem; border: 1px solid #e5e7eb; background: #ffffff; color: #111827; border-radius: .5rem; cursor: pointer; font-weight: 600; }
.btn.subtle { background: #f9fafb; }

.list { display: grid; gap: .75rem; }
.card { border: 1px solid #e5e7eb; border-radius: .75rem; background: #ffffff; }
.order { overflow: hidden; }
.o-head {
  display: grid; grid-template-columns: 1fr auto; gap: .5rem;
  padding: .75rem 1rem; align-items: center; cursor: pointer;
}
.left { display: flex; align-items: baseline; gap: .75rem; }
.oid { margin: 0; font-size: 1.05rem; font-weight: 800; color: #111827; }
.odate { color: #6b7280; }
.right { display: inline-flex; align-items: center; gap: .6rem; }
.badge { font-size: .75rem; padding: .2rem .5rem; border-radius: 999px; border: 1px solid #e5e7eb; color: #374151; background: #f9fafb; }
.badge[data-status="PLACED"] { color: #1f2937; background: #e5e7eb; border-color: #9ca3af; }
.badge[data-status="SHIPPED"] { color: #065f46; background: #d1fae5; border-color: #10b981; }
.badge[data-status="DELIVERED"] { color: #1e40af; background: #dbeafe; border-color: #60a5fa; }
.ototal { font-weight: 900; color: #0f766e; }
.icon.chevron { border: none; background: transparent; color: #374151; cursor: pointer; }

.o-body { border-top: 1px solid #e5e7eb; padding: .75rem 1rem 1rem; display: grid; gap: .75rem; }
.lines { width: 100%; border-collapse: collapse; }
.lines th, .lines td { padding: .5rem; border-bottom: 1px solid #f3f4f6; }
.lines thead th { text-align: left; color: #6b7280; font-weight: 600; }
.lines .num { text-align: right; }
.thumb { width: 64px; height: 48px; object-fit: cover; border-radius: .35rem; border: 1px solid #e5e7eb; background: #f3f4f6; }
.label { color: #6b7280; }
.value { font-weight: 900; color: #111827; }
.hint { color: #6b7280; margin: .25rem 0 0; }

.muted { color: #6b7280; }
.empty { color: #6b7280; border: 1px dashed #d1d5db; border-radius: .75rem; padding: 1.25rem; background: #fff; }
</style>
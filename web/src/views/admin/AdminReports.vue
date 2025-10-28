<script setup>
// Admin-only reports dashboard
import { ref, onMounted } from 'vue'
import http from '@/api/http'

// --- date filters ---
const today = new Date()
const pad = (n) => String(n).padStart(2, '0')
const toISODate = (d) => `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`

const to = ref(toISODate(today))
const from = ref(toISODate(new Date(Date.now() - 29*24*60*60*1000))) // last 30 days

// --- limits ---
const limitTopProducts = ref(10)
const limitTopSpend = ref(5)
const limitTopSellers = ref(5)
const limitTopOrders = ref(10)
const limitActiveListings = ref(10)

// --- data ---
const topProducts = ref([])
const topCustomersSpend = ref([])
const topSellersUnits = ref([])
const topCustomersOrders = ref([])
const topActiveListings = ref([])

// --- helpers ---
async function fetchJSON(url, params={}) {
  try {
    const { data } = await http.get(url, { params })
    return data
  } catch (err) {
    console.error('Report error:', err?.response?.data || err?.message)
    return []
  }
}

async function loadAll() {
  await Promise.all([
    loadTopProducts(),
    loadTopCustomersSpend(),
    loadTopSellersUnits(),
    loadTopCustomersOrders(),
    loadTopActiveListings()
  ])
}

async function loadTopProducts() {
  topProducts.value = await fetchJSON('/api/reports/top-products', {
    from: from.value, to: to.value, limit: limitTopProducts.value
  })
}
async function loadTopCustomersSpend() {
  topCustomersSpend.value = await fetchJSON('/api/reports/top-customers-spend', {
    from: from.value, to: to.value, limit: limitTopSpend.value
  })
}
async function loadTopSellersUnits() {
  topSellersUnits.value = await fetchJSON('/api/reports/top-sellers-units', {
    from: from.value, to: to.value, limit: limitTopSellers.value
  })
}
async function loadTopCustomersOrders() {
  topCustomersOrders.value = await fetchJSON('/api/reports/top-customers-orders', {
    from: from.value, to: to.value, limit: limitTopOrders.value
  })
}
async function loadTopActiveListings() {
  topActiveListings.value = await fetchJSON('/api/reports/top-sellers-active-listings', {
    limit: limitActiveListings.value
  })
}

onMounted(loadAll)
</script>

<template>
  <div class="reports">
    <h1>Reportes</h1>

    <section class="filters">
      <div class="row">
        <label>
          Desde
          <input type="date" v-model="from">
        </label>
        <label>
          Hasta
          <input type="date" v-model="to">
        </label>
        <button class="btn" @click="loadAll">Actualizar</button>
      </div>
    </section>

    <!-- Top 10 productos más vendidos -->
    <section class="card">
      <div class="card-header">
        <h2>Top productos por unidades</h2>
        <div class="controls">
          <label>Límite <input type="number" min="1" max="50" v-model.number="limitTopProducts"></label>
          <button class="btn" @click="loadTopProducts">Refrescar</button>
        </div>
      </div>
      <table class="table">
        <thead>
        <tr>
          <th>#</th>
          <th>Producto</th>
          <th>Unidades</th>
          <th>Ingresos (Q)</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(r,i) in topProducts" :key="r.productId">
          <td>{{ i+1 }}</td>
          <td>{{ r.productName }}</td>
          <td>{{ r.unitsSold }}</td>
          <td>{{ Number(r.revenue ?? 0).toFixed(2) }}</td>
        </tr>
        <tr v-if="!topProducts.length"><td colspan="4" class="empty">Sin datos</td></tr>
        </tbody>
      </table>
    </section>

    <!-- Top 5 clientes por gasto -->
    <section class="card">
      <div class="card-header">
        <h2>Top clientes por gasto</h2>
        <div class="controls">
          <label>Límite <input type="number" min="1" max="50" v-model.number="limitTopSpend"></label>
          <button class="btn" @click="loadTopCustomersSpend">Refrescar</button>
        </div>
      </div>
      <table class="table">
        <thead>
        <tr>
          <th>#</th>
          <th>Cliente</th>
          <th>Órdenes</th>
          <th>Items</th>
          <th>Gastado (Q)</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(r,i) in topCustomersSpend" :key="r.customerId">
          <td>{{ i+1 }}</td>
          <td>{{ r.customerName }}</td>
          <td>{{ r.ordersCount }}</td>
          <td>{{ r.itemsCount }}</td>
          <td>{{ Number(r.totalSpent ?? 0).toFixed(2) }}</td>
        </tr>
        <tr v-if="!topCustomersSpend.length"><td colspan="5" class="empty">Sin datos</td></tr>
        </tbody>
      </table>
    </section>

    <!-- Top 5 vendedores -->
    <section class="card">
      <div class="card-header">
        <h2>Top vendedores por unidades</h2>
        <div class="controls">
          <label>Límite <input type="number" min="1" max="50" v-model.number="limitTopSellers"></label>
          <button class="btn" @click="loadTopSellersUnits">Refrescar</button>
        </div>
      </div>
      <table class="table">
        <thead>
        <tr>
          <th>#</th>
          <th>Vendedor</th>
          <th>Unidades vendidas</th>
          <th>Ingresos (Q)</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(r,i) in topSellersUnits" :key="r.sellerId">
          <td>{{ i+1 }}</td>
          <td>{{ r.sellerName }}</td>
          <td>{{ r.itemsSold }}</td>
          <td>{{ Number(r.revenue ?? 0).toFixed(2) }}</td>
        </tr>
        <tr v-if="!topSellersUnits.length"><td colspan="4" class="empty">Sin datos</td></tr>
        </tbody>
      </table>
    </section>

    <!-- Top clientes por # pedidos -->
    <section class="card">
      <div class="card-header">
        <h2>Top clientes por # pedidos</h2>
        <div class="controls">
          <label>Límite <input type="number" min="1" max="50" v-model.number="limitTopOrders"></label>
          <button class="btn" @click="loadTopCustomersOrders">Refrescar</button>
        </div>
      </div>
      <table class="table">
        <thead>
        <tr>
          <th>#</th>
          <th>Cliente</th>
          <th>Pedidos</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(r,i) in topCustomersOrders" :key="r.customerId">
          <td>{{ i+1 }}</td>
          <td>{{ r.customerName }}</td>
          <td>{{ r.ordersCount }}</td>
        </tr>
        <tr v-if="!topCustomersOrders.length"><td colspan="3" class="empty">Sin datos</td></tr>
        </tbody>
      </table>
    </section>

    <!-- Top vendedores con más listados -->
    <section class="card">
      <div class="card-header">
        <h2>Top vendedores por listados activos</h2>
        <div class="controls">
          <label>Límite <input type="number" min="1" max="50" v-model.number="limitActiveListings"></label>
          <button class="btn" @click="loadTopActiveListings">Refrescar</button>
        </div>
      </div>
      <table class="table">
        <thead>
        <tr>
          <th>#</th>
          <th>Vendedor</th>
          <th>Listados activos</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(r,i) in topActiveListings" :key="r.sellerId">
          <td>{{ i+1 }}</td>
          <td>{{ r.sellerName }}</td>
          <td>{{ r.activeProducts }}</td>
        </tr>
        <tr v-if="!topActiveListings.length"><td colspan="3" class="empty">Sin datos</td></tr>
        </tbody>
      </table>
    </section>
  </div>
</template>

<style scoped>
.reports { padding: 1rem; }
.filters .row { display: flex; gap: 1rem; align-items: end; margin-bottom: 1rem; flex-wrap: wrap; }
label { display: inline-flex; flex-direction: column; font-size: .95rem; gap: .25rem; }
input[type="date"], input[type="number"] { padding: .4rem .6rem; border: 1px solid #ddd; border-radius: 6px; }
.btn { padding: .45rem .8rem; border: none; border-radius: 6px; cursor: pointer; }
.card { background: #fff; border: 1px solid #eee; border-radius: 8px; padding: .5rem .75rem; margin-bottom: 1rem; }
.card-header { display: flex; align-items: center; justify-content: space-between; gap: .5rem; }
.card-header h2 { margin: 0; font-size: 1.05rem; }
.controls { display: inline-flex; gap: .5rem; align-items: center; }
.table { width: 100%; border-collapse: collapse; }
.table th, .table td { padding: .5rem .6rem; border-bottom: 1px solid #eee; text-align: left; }
.table th { background: #fafafa; }
.empty { text-align: center; color: #999; }
</style>
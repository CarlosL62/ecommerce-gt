<script setup>
import { ref, onMounted, watch } from 'vue'
import http from '../../api/http'

const tab = ref('PLACED')              // 'PLACED' | 'SHIPPED'
const loading = ref(false)
const error = ref('')
const orders = ref([])

async function load() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await http.get('/api/orders', { params: { status: tab.value } })
    orders.value = Array.isArray(data) ? data : (Array.isArray(data?.orders) ? data.orders : [])
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || 'No se pudieron cargar las órdenes'
    orders.value = []
  } finally {
    loading.value = false
  }
}

async function ship(id) {
  try {
    await http.patch(`/api/orders/${id}/ship`)
    await load()
  } catch (e) {
    alert(e?.response?.data?.message || 'No se pudo marcar como enviado')
  }
}

async function deliver(id) {
  try {
    await http.patch(`/api/orders/${id}/deliver`)
    await load()
  } catch (e) {
    alert(e?.response?.data?.message || 'No se pudo marcar como entregado')
  }
}

onMounted(load)
watch(tab, load)
</script>

<template>
  <section class="head">
    <div class="tabs">
      <button :class="{active: tab==='PLACED'}" @click="tab='PLACED'">Pendientes de envío</button>
      <button :class="{active: tab==='SHIPPED'}" @click="tab='SHIPPED'">En tránsito</button>
    </div>
  </section>

  <p v-if="error" class="err">⚠️ {{ error }}</p>
  <p v-if="!loading && !error && orders.length===0">No hay órdenes.</p>

  <table v-if="orders.length">
    <thead>
    <tr>
      <th>ID</th><th>Fecha</th><th>Estado</th><th>Subtotal</th><th>Items</th><th>Acciones</th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="o in orders" :key="o.id">
      <td>#{{ o.id }}</td>
      <td>{{ o.createdAt?.slice(0,10) }}</td>
      <td><span class="badge" :data-status="o.status">{{ o.status }}</span></td>
      <td>Q {{ Number(o.subtotal ?? 0).toFixed(2) }}</td>
      <td>{{ o.items?.length || 0 }}</td>
      <td class="actions">
        <button v-if="o.status==='PLACED'" @click="ship(o.id)">Marcar enviado</button>
        <button v-if="o.status==='SHIPPED'" @click="deliver(o.id)">Marcar entregado</button>
      </td>
    </tr>
    </tbody>
  </table>
</template>

<style scoped>
.head { display:flex; align-items:center; justify-content:space-between; margin-bottom: .75rem; }
.tabs { display:flex; gap:.5rem; }
.tabs button { padding:.4rem .7rem; border:1px solid #e5e7eb; border-radius:.5rem; background:#fff; cursor:pointer; }
.tabs button.active { background:#111827; color:#fff; border-color:#111827; }
.err { color:#b91c1c; margin:.5rem 0; }
table { width:100%; border-collapse: collapse; }
th, td { padding:.5rem .6rem; border-top:1px solid #e5e7eb; font-size:.95rem; }
.badge[data-status="PLACED"] { color:#1f2937; background:#e5e7eb; border:1px solid #9ca3af; border-radius:.5rem; padding:.15rem .4rem; }
.badge[data-status="SHIPPED"] { color:#065f46; background:#d1fae5; border:1px solid #10b981; border-radius:.5rem; padding:.15rem .4rem; }
.badge[data-status="DELIVERED"] { color:#1e40af; background:#dbeafe; border:1px solid #60a5fa; border-radius:.5rem; padding:.15rem .4rem; }
.actions { display:flex; gap:.4rem; }
.actions button { padding:.35rem .6rem; border-radius:.5rem; border:1px solid #e5e7eb; background:#fff; cursor:pointer; }
</style>
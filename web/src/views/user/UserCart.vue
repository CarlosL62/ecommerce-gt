<script setup>
// User Cart
// - Loads cart items from /api/cart
// - Update quantity (PATCH /api/cart/items/{id})
// - Remove single item (DELETE /api/cart/items/{id})
// - Clear cart (DELETE /api/cart)
// - Proceed to checkout (POST /api/orders/checkout)
// Notes: If some endpoints are not ready yet, the UI will show friendly errors.

import { ref, reactive, computed, onMounted } from 'vue'
import http from '../../api/http'

// ---- helpers ----
function asMoney (n) {
  const num = Number(n || 0)
  return `Q ${num.toFixed(2)}`
}

// ---- state ----
const loading = ref(false)
const error = ref('')
const items = ref([]) // [{ id, product: {id,name,imageUrl,price}, qty, lineTotal }]
const working = reactive({}) // track per-item loading state
const globalMsg = reactive({ ok: '', err: '' })

// ---- checkout modal state ----
const showCheckout = ref(false)
const cards = ref([])
const selectedCardId = ref(null)
const paying = ref(false)
const payErr = ref('')

// new card form model
const newCard = reactive({ cardHolder: '', cardNumber: '', brand: 'VISA', save: false })

// ---- derived totals ----
const subtotal = computed(() =>
  items.value.reduce((acc, it) => acc + Number(it.lineTotal || (Number(it.product?.price || 0) * Number(it.qty || 0))), 0)
)
const shipping = computed(() => 0) // free for project scope
const total = computed(() => subtotal.value + shipping.value)

// ---- load cart ----
async function loadCart () {
  loading.value = true
  error.value = ''
  globalMsg.ok = ''
  globalMsg.err = ''
  try {
    const { data } = await http.get('/api/cart')
    if (Array.isArray(data)) {
      items.value = data
    } else if (data && Array.isArray(data.items)) {
      items.value = data.items
    } else {
      items.value = []
    }
  } catch (e) {
    if (e && (e.status === 401 || e.status === 403 || e.status === 404)) {
      items.value = []
      error.value = ''
    } else {
      error.value = e?.message || 'Error al cargar el carrito'
    }
  } finally {
    loading.value = false
  }
}

// ---- item actions ----
async function updateQty (it, newQty) {
  globalMsg.ok = ''
  globalMsg.err = ''
  const q = Number(newQty)
  if (!Number.isInteger(q) || q < 1) return
  working[it.id] = true
  try {
    // PATCH preferred for partial update; fallback to PUT if your backend uses it
    await http.patch(`/api/cart/items/${it.id}`, { qty: q })
    // Optimistic UI: update local item and recalc line total
    it.qty = q
    const unit = Number(it.product?.price || 0)
    it.lineTotal = unit * q
  } catch (e) {
    globalMsg.err = e?.message || 'No se pudo actualizar la cantidad'
  } finally {
    working[it.id] = false
  }
}

async function removeItem (it) {
  globalMsg.ok = ''
  globalMsg.err = ''
  working[it.id] = true
  try {
    await http.delete(`/api/cart/items/${it.id}`)
    items.value = items.value.filter(x => x.id !== it.id)
    globalMsg.ok = 'Producto eliminado del carrito.'
  } catch (e) {
    globalMsg.err = e?.message || 'No se pudo eliminar el producto'
  } finally {
    working[it.id] = false
  }
}

async function clearCart () {
  globalMsg.ok = ''
  globalMsg.err = ''
  try {
    await http.delete('/api/cart')
    items.value = []
    globalMsg.ok = 'Carrito limpiado.'
  } catch (e) {
    globalMsg.err = e?.message || 'No se pudo limpiar el carrito'
  }
}

async function checkout () {
  globalMsg.ok = ''
  globalMsg.err = ''
  payErr.value = ''
  if (items.value.length === 0) {
    globalMsg.err = 'El carrito está vacío.'
    return
  }
  try {
    // Load saved cards and open modal
    const { data } = await http.get('/api/cards')
    cards.value = data
    selectedCardId.value = cards.value?.[0]?.id ?? null
    showCheckout.value = true
  } catch (e) {
    // If cards endpoint is not ready, still open modal for new card
    console.warn('Cards load failed', e)
    cards.value = []
    selectedCardId.value = null
    showCheckout.value = true
  }
}

async function payWithSaved () {
  payErr.value = ''
  if (!selectedCardId.value) { payErr.value = 'Selecciona una tarjeta.'; return }
  paying.value = true
  try {
    await http.post('/api/orders/checkout', { savedCardId: Number(selectedCardId.value) })
    globalMsg.ok = 'Pedido realizado con éxito.'
    items.value = []
    showCheckout.value = false
  } catch (e) {
    payErr.value = e?.message || 'No se pudo completar el pago'
  } finally {
    paying.value = false
  }
}

async function payWithNew () {
  payErr.value = ''
  if (!newCard.cardHolder || !newCard.cardNumber) {
    payErr.value = 'Completa titular y número de tarjeta.'
    return
  }
  paying.value = true
  try {
    await http.post('/api/orders/checkout', { cardHolder: newCard.cardHolder, cardNumber: newCard.cardNumber, brand: newCard.brand, save: !!newCard.save })
    globalMsg.ok = 'Pedido realizado con éxito.'
    items.value = []
    showCheckout.value = false
  } catch (e) {
    payErr.value = e?.message || 'No se pudo completar el pago'
  } finally {
    paying.value = false
  }
}

onMounted(loadCart)
</script>

<template>
  <section class="wrap">
    <header class="head">
      <h1>Carrito de compras</h1>
      <div class="spacer"/>
      <button class="btn subtle" :disabled="items.length===0" @click="clearCart">Vaciar carrito</button>
    </header>

    <div v-if="loading" class="muted">Cargando…</div>
    <div v-else-if="error" class="err">⚠️ {{ error }}</div>

    <div v-else class="layout">
      <!-- items list -->
      <div class="list">
        <div v-if="items.length === 0" class="empty">
          Tu carrito está vacío.
        </div>

        <article v-for="it in items" :key="it.id" class="row">
          <img class="thumb" :src="it.product?.imageUrl" alt="" />
          <div class="info">
            <h3 class="name">{{ it.product?.name }}</h3>
            <p class="price-unit">Unitario: {{ asMoney(it.product?.price) }}</p>
            <div class="qty">
              <button class="qbtn" :disabled="working[it.id] || it.qty<=1" @click="updateQty(it, it.qty-1)">−</button>
              <input class="qinput" :disabled="working[it.id]" type="number" min="1" step="1" v-model.number="it.qty" @change="updateQty(it, it.qty)" />
              <button class="qbtn" :disabled="working[it.id]" @click="updateQty(it, it.qty+1)">＋</button>
            </div>
          </div>
          <div class="totals">
            <div class="line">{{ asMoney(it.lineTotal || (Number(it.product?.price || 0) * Number(it.qty || 0))) }}</div>
            <button class="rm" :disabled="working[it.id]" @click="removeItem(it)">Eliminar</button>
          </div>
        </article>
      </div>

      <!-- summary -->
      <aside class="summary">
        <h2>Resumen</h2>
        <div class="sum-row"><span>Subtotal</span><span>{{ asMoney(subtotal) }}</span></div>
        <div class="sum-row"><span>Envío</span><span>{{ asMoney(shipping) }}</span></div>
        <div class="sum-row total"><span>Total</span><span>{{ asMoney(total) }}</span></div>
        <button class="btn primary" :disabled="items.length===0" @click="checkout">Pagar</button>
        <p v-if="globalMsg.ok" class="ok">✅ {{ globalMsg.ok }}</p>
        <p v-if="globalMsg.err" class="err">⚠️ {{ globalMsg.err }}</p>
      </aside>

      <div v-if="showCheckout" class="modal">
        <div class="sheet">
          <header class="sheet-head">
            <h3>Finalizar compra</h3>
            <button class="x" @click="showCheckout=false">✕</button>
          </header>

          <div class="sheet-body">
            <div v-if="cards.length">
              <h4>Tarjetas guardadas</h4>
              <select v-model="selectedCardId">
                <option v-for="c in cards" :key="c.id" :value="c.id">
                  {{ c.label ? c.label + ' — ' : '' }}{{ c.brand }} •••• {{ String(c.cardNumber).slice(-4) }} ({{ c.cardHolder }})
                </option>
              </select>
              <button class="btn primary" :disabled="paying" @click="payWithSaved">Pagar con tarjeta seleccionada</button>
              <hr />
            </div>

            <h4>Usar nueva tarjeta</h4>
            <div class="grid2">
              <label>
                <span>Titular</span>
                <input v-model="newCard.cardHolder" type="text" placeholder="Nombre en la tarjeta" />
              </label>
              <label>
                <span>Número</span>
                <input v-model="newCard.cardNumber" type="text" placeholder="4111 1111 1111 1111" />
              </label>
              <label>
                <span>Marca</span>
                <select v-model="newCard.brand">
                  <option>VISA</option>
                  <option>MASTERCARD</option>
                  <option>AMEX</option>
                  <option>CARD</option>
                </select>
              </label>
              <label class="chk">
                <input type="checkbox" v-model="newCard.save" /> Guardar esta tarjeta
              </label>
            </div>

            <div class="actions">
              <button class="btn primary" :disabled="paying" @click="payWithNew">Pagar con nueva tarjeta</button>
              <button class="btn" :disabled="paying" @click="showCheckout=false">Cancelar</button>
            </div>

            <p v-if="payErr" class="err">⚠️ {{ payErr }}</p>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.wrap { display: grid; gap: 1rem; }
.head { display: flex; align-items: center; gap: .75rem; }
.head h1 { font-size: 2rem; margin: 0; }
.spacer { flex: 1; }

.layout { display: grid; grid-template-columns: 1fr 320px; gap: 1rem; align-items: start; }
.list { display: grid; gap: .75rem; }
.empty { color: #6b7280; border: 1px dashed #d1d5db; border-radius: .75rem; padding: 1.25rem; background: #fff; }

.row {
  display: grid; grid-template-columns: 96px 1fr auto; gap: .75rem;
  border: 1px solid #e5e7eb; border-radius: .75rem; background: #fff; padding: .75rem;
}
.thumb { width: 96px; height: 96px; object-fit: cover; border-radius: .5rem; border: 1px solid #e5e7eb; background: #f3f4f6; }
.info { display: grid; gap: .35rem; align-content: start; }
.name { margin: 0; font-size: 1.05rem; font-weight: 700; color: #111827; }
.price-unit { color: #6b7280; }

.qty { display: inline-flex; align-items: center; gap: .35rem; }
.qbtn { width: 28px; height: 28px; border-radius: .4rem; border: 1px solid #e5e7eb; background: #fff; cursor: pointer; }
.qinput { width: 64px; padding: .4rem .5rem; border: 1px solid #d1d5db; border-radius: .4rem; }

.totals { display: grid; gap: .35rem; align-content: start; justify-items: end; }
.line { font-weight: 800; color: #0f766e; }
.rm { border: none; background: transparent; color: #dc2626; cursor: pointer; }

.summary {
  border: 1px solid #e5e7eb; border-radius: .75rem; background: #fff; padding: 1rem;
  display: grid; gap: .6rem;
}
.summary h2 { margin: 0 0 .5rem 0; }
.sum-row { display: flex; align-items: center; justify-content: space-between; }
.sum-row.total { font-weight: 800; }
.btn { padding: .52rem .9rem; border: 1px solid #e5e7eb; background: #ffffff; color: #111827; border-radius: .5rem; cursor: pointer; font-weight: 600; }
.btn.primary { background: #3b82f6; color: #fff; border-color: #2563eb; }
.btn.subtle { background: #f9fafb; }
.btn:disabled { opacity: .6; cursor: not-allowed; }

.ok { color: #16a34a; font-weight: 600; }
.err { color: #dc2626; font-weight: 600; }
.muted { color: #6b7280; }

@media (max-width: 980px) {
  .layout { grid-template-columns: 1fr; }
}

/* checkout modal */
.modal { position: fixed; inset: 0; background: rgba(0,0,0,.4); display: grid; place-items: center; z-index: 50; }
.sheet { width: min(720px, 92vw); background: #fff; border: 1px solid #e5e7eb; border-radius: .75rem; overflow: hidden; }
.sheet-head { display: flex; align-items: center; justify-content: space-between; padding: .75rem 1rem; border-bottom: 1px solid #e5e7eb; }
.sheet-body { display: grid; gap: .75rem; padding: 1rem; }
.x { border: none; background: transparent; font-size: 1.1rem; cursor: pointer; }
.grid2 { display: grid; grid-template-columns: 1fr 1fr; gap: .75rem; }
label { display: grid; gap: .25rem; font-size: .92rem; }
label.chk { grid-column: 1 / -1; display: flex; align-items: center; gap: .5rem; }
input, select { border: 1px solid #d1d5db; border-radius: .5rem; padding: .5rem .6rem; }
.actions { display: flex; gap: .5rem; }
hr { border: 0; border-top: 1px solid #e5e7eb; margin: .75rem 0; }
</style>
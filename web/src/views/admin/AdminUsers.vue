<script setup>
// Admin Users management page
// - Lists users and filters by active status
// - Allows suspend/reactivate actions
// - Allows creating "workers" (ADMIN / MODERATOR / LOGISTICS)

import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import http from "../../api/http";
import { useAuthStore } from "../../stores/auth";

const router = useRouter();
const auth = useAuthStore();

// ----------- state -----------
const users = ref([]);
const loading = ref(false);
const error = ref("");
const actionLoading = ref(null); // holds userId during row action

// filter: 'all' | 'true' | 'false'
const filterActive = ref("all");

// create worker form state
const wName = ref("");
const wEmail = ref("");
const wPass = ref("");
const wRole = ref("MODERATOR");
const creating = ref(false);

// edit dialog state
const editing = ref(false);
const saving = ref(false);
const editTarget = ref(null); // original user object
const eName = ref("");
const eEmail = ref("");
const eRole = ref("");

function openEdit(u) {
  error.value = "";
  editTarget.value = u;
  eName.value = u.name;
  eEmail.value = u.email;
  eRole.value = u.role;
  editing.value = true;
}

async function saveEdit() {
  if (!editTarget.value) return;
  saving.value = true;
  error.value = "";
  try {
    const payload = {};
    if (eName.value && eName.value !== editTarget.value.name) payload.name = eName.value;
    if (eEmail.value && eEmail.value !== editTarget.value.email) payload.email = eEmail.value;
    if (eRole.value && eRole.value !== editTarget.value.role) payload.role = eRole.value;

    if (Object.keys(payload).length === 0) {
      editing.value = false; // nothing changed
      return;
    }

    const { data } = await http.patch(`/api/admin/users/${editTarget.value.id}`, payload);
    // update row locally
    const idx = users.value.findIndex(u => u.id === editTarget.value.id);
    if (idx !== -1) users.value[idx] = data;
    editing.value = false;
    await fetchUsers(); // ensure fresh state
  } catch (e) {
    error.value = e?.response?.data?.message || "No se pudo actualizar el usuario";
  } finally {
    saving.value = false;
  }
}

function cancelEdit() {
  editing.value = false;
}

// ----------- helpers -----------
async function ensureAdmin() {
  // hydrate user if needed
  if (auth.token && !auth.user) {
    await auth.me();
  }
  if (auth.role !== "ADMIN") {
    router.replace({ name: "home" });
    throw new Error("Not authorized");
  }
}

async function fetchUsers() {
  error.value = "";
  loading.value = true;
  try {
    const params = {};
    if (filterActive.value === "true") params.active = true;
    if (filterActive.value === "false") params.active = false;
    const { data } = await http.get("/api/admin/users", { params });
    users.value = data;
  } catch (e) {
    error.value = e?.response?.data?.message || "No se pudo cargar la lista de empleados";
  } finally {
    loading.value = false;
  }
}

async function suspendUser(id) {
  actionLoading.value = id;
  error.value = "";
  try {
    const { data } = await http.patch(`/api/admin/users/${id}/suspend`);
    const idx = users.value.findIndex((u) => u.id === id);
    if (idx !== -1) users.value[idx] = data;
    await fetchUsers();
  } catch (e) {
    error.value = e?.response?.data?.message || "No se pudo suspender el usuario";
  } finally {
    actionLoading.value = null;
  }
}

async function reactivateUser(id) {
  actionLoading.value = id;
  error.value = "";
  try {
    const { data } = await http.patch(`/api/admin/users/${id}/activate`);
    const idx = users.value.findIndex((u) => u.id === id);
    if (idx !== -1) users.value[idx] = data;
    await fetchUsers();
  } catch (e) {
    error.value = e?.response?.data?.message || "No se pudo reactivar el usuario";
  } finally {
    actionLoading.value = null;
  }
}

async function createWorker() {
  error.value = "";
  creating.value = true;
  try {
    if (!wName.value || !wEmail.value || !wPass.value) {
      throw new Error("Nombre, email y contraseña son obligatorios");
    }
    await http.post("/api/admin/users", {
      name: wName.value,
      email: wEmail.value,
      password: wPass.value,
      role: wRole.value,
    });
    // reset form and refresh list
    wName.value = "";
    wEmail.value = "";
    wPass.value = "";
    wRole.value = "MODERATOR";
    await fetchUsers();
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || "No se pudo crear el trabajador";
  } finally {
    creating.value = false;
  }
}

onMounted(async () => {
  try {
    await ensureAdmin();
    await fetchUsers();
  } catch {
    // ensureAdmin already redirected on failure
  }
});
</script>

<template>
  <div class="wrap">
    <h1>Administración de Empleados</h1>

    <!-- create worker card -->
    <section class="card">
      <h2>Crear trabajador</h2>
      <div class="grid">
        <input v-model.trim="wName" type="text" placeholder="Nombre" />
        <input v-model.trim="wEmail" type="email" placeholder="Email" />
        <input v-model="wPass" type="password" placeholder="Contraseña" />
        <select v-model="wRole">
          <option value="ADMIN">ADMIN</option>
          <option value="MODERATOR">MODERATOR</option>
          <option value="LOGISTICS">LOGISTICS</option>
        </select>
        <button class="btn" @click="createWorker" :disabled="creating">
          {{ creating ? "Creando..." : "Crear" }}
        </button>
      </div>
    </section>

    <!-- toolbar -->
    <section class="toolbar">
      <label>
        Estado:
        <select v-model="filterActive" @change="fetchUsers">
          <option value="all">Todos</option>
          <option value="true">Activos</option>
          <option value="false">Suspendidos</option>
        </select>
      </label>
      <button class="btn" @click="fetchUsers" :disabled="loading">
        {{ loading ? "Cargando..." : "Refrescar" }}
      </button>
    </section>

    <p v-if="error" class="error">{{ error }}</p>

    <!-- users table -->
    <div v-if="users.length" class="table-wrap">
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Nombre</th>
            <th>Email</th>
            <th>Rol</th>
            <th>Activo</th>
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
              <span :class="['badge', u.active ? 'on' : 'off']">
                {{ u.active ? 'Sí' : 'No' }}
              </span>
            </td>
            <td>
              <div class="row-actions">
                <button
                  class="btn edit"
                  @click="openEdit(u)"
                  :disabled="actionLoading === u.id"
                  title="Editar usuario"
                >
                  ✏️ Editar
                </button>

                <button
                  v-if="u.active"
                  class="btn warn"
                  @click="suspendUser(u.id)"
                  :disabled="actionLoading === u.id"
                >
                  {{ actionLoading === u.id ? '...' : 'Suspender' }}
                </button>
                <button
                  v-else
                  class="btn success"
                  @click="reactivateUser(u.id)"
                  :disabled="actionLoading === u.id"
                >
                  {{ actionLoading === u.id ? '...' : 'Reactivar' }}
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <p v-else-if="!loading">No hay empleados para mostrar.</p>

    <!-- edit dialog (lightweight overlay) -->
    <div v-if="editing" class="overlay" @click.self="cancelEdit">
      <div class="dialog">
        <h3>Editar usuario</h3>
        <div class="form">
          <label>Nombre
            <input class="input" v-model.trim="eName" type="text" />
          </label>
          <label>Email
            <input class="input" v-model.trim="eEmail" type="email" />
          </label>
          <label>Rol
            <select class="input" v-model="eRole">
              <option value="ADMIN">ADMIN</option>
              <option value="MODERATOR">MODERATOR</option>
              <option value="LOGISTICS">LOGISTICS</option>
              <option value="COMMON">COMMON</option>
            </select>
          </label>
        </div>
        <div class="actions">
          <button class="btn subtle" @click="cancelEdit">Cancelar</button>
          <button class="btn" :disabled="saving" @click="saveEdit">{{ saving ? 'Guardando...' : 'Guardar' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
h1 {
  font-size: 3rem;
  font-weight: 700;
  color: #ffffff; /* darker slate color for contrast */
  margin-bottom: .25rem;
}

.wrap { display: grid; gap: 1rem; color: #111827; }

.card { border: 1px solid #eee; border-radius: .5rem; padding: 1rem; display: grid; gap: .75rem; background: #fff; }
.card h2 { margin: 0; font-size: 1.05rem; }
.grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: .5rem; }
.grid input, .grid select { padding: .5rem .6rem; border: 1px solid #ddd; border-radius: .375rem; }
.grid .btn { grid-column: span 1; }

.toolbar { display: flex; gap: .75rem; align-items: center; }

.table-wrap { overflow: auto; border: 1px solid #eee; border-radius: .5rem; background: #fff; }
.table { width: 100%; border-collapse: collapse; }
.table th, .table td { padding: .6rem .7rem; border-bottom: 1px solid #f1f1f1; text-align: left; }
.table th, .table td { color: #111827; }
.table th { background: #f3f4f6; color: #111827; }

.badge { padding: .15rem .45rem; border-radius: .35rem; font-size: .85rem; }
.badge.on { background: #e6f6ea; color: #0a7a28; }
.badge.off { background: #fdeaea; color: #8a001a; }

.btn {
  padding: .5rem .9rem;
  border: 1px solid #d1d5db;
  border-radius: .5rem;
  cursor: pointer;
  font-weight: 600;
  color: #ffffff;
  background-color: #3b82f6; /* blue-500 */
  transition: background-color .15s ease, transform .05s ease;
}
.btn:hover {
  background-color: #2563eb; /* blue-600 */
}
.btn:active {
  transform: translateY(1px);
}
.btn[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
}
.btn.warn {
  background-color: #f97316; /* orange-500 */
  border-color: #ea580c;
}
.btn.warn:hover {
  background-color: #ea580c; /* orange-600 */
}
.btn.success {
  background-color: #16a34a; /* green-600 */
  border-color: #15803d;
}
.btn.success:hover {
  background-color: #15803d; /* green-700 */
}

.error { color: #b00020; }

@media (max-width: 920px) {
  .grid { grid-template-columns: 1fr 1fr; }
}
@media (max-width: 520px) {
  .grid { grid-template-columns: 1fr; }
}

.row-actions { display: flex; gap: .5rem; align-items: center; }

.btn.neutral {
  background-color: #6b7280; /* gray-500 */
  border-color: #4b5563;
}
.btn.neutral:hover {
  background-color: #4b5563; /* gray-600 */
}

/* overlay dialog */
.overlay {
  position: fixed; inset: 0;
  background: rgba(0,0,0,.35);
  display: grid; place-items: center;
  z-index: 1000;
}
.dialog {
  width: min(520px, 92vw);
  background: #ffffff;
  color: #111827;
  border: 1px solid #e5e7eb;
  border-radius: .75rem;
  box-shadow: 0 10px 30px rgba(0,0,0,.25);
  padding: 1rem;
  display: grid; gap: 1rem;
}
.dialog h3 {
  margin: 0; font-size: 1.15rem;
}
.dialog .form {
  display: grid; gap: .75rem;
}
.dialog label {
  display: grid; gap: .35rem; font-size: .95rem;
}
.dialog .input {
  padding: .6rem .7rem;
  border: 1px solid #d1d5db;
  border-radius: .5rem;
}
.dialog .actions {
  display: flex; gap: .5rem; justify-content: end;
}
</style>
 .link {
   background: transparent;
   color: #1d4ed8; /* blue-700 */
   border: none;
   padding: 0;
   font: inherit;
   text-decoration: underline;
   cursor: pointer;
 }
 .link:hover { color: #1e40af; /* blue-800 */ }

 .btn.edit {
   background-color: #6366f1; /* indigo-500 */
   border-color: #4f46e5; /* indigo-600 */
 }
 .btn.edit:hover { background-color: #4f46e5; }
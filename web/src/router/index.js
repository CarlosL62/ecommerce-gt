import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "../stores/auth";

// Lazy-loaded route components
const HomeView = () => import("../views/HomeView.vue");
const LoginView = () => import("../views/LoginView.vue");
const RegisterView = () => import("../views/RegisterView.vue");
const AdminLayout = () => import("../views/AdminLayout.vue");
const AdminUsers = () => import("../views/admin/AdminUsers.vue");
const AdminReports = () => import("../views/admin/AdminReports.vue");

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: "/", name: "home", component: HomeView, meta: { requiresAuth: true } },
    { path: "/login", name: "login", component: LoginView, meta: { guest: true } },
    { path: "/register", name: "register", component: RegisterView, meta: { guest: true } },

    // Admin parent layout and children
    {
      path: "/admin",
      component: AdminLayout,
      meta: { requiresAuth: true, role: "ADMIN" },
      children: [
        { path: "users", name: "admin-users", component: AdminUsers },
        { path: "reports", name: "admin-reports", component: AdminReports },
        { path: "", redirect: { name: "admin-users" } },
      ],
    },
  ],
});

// Global navigation guard for auth and roles
router.beforeEach(async (to) => {
  const auth = useAuthStore();

  // Check authentication
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: "login", query: { redirect: to.fullPath } };
  }
  if (to.meta.guest && auth.isAuthenticated) {
    return { name: "home" };
  }

  // Check role if required
  if (to.meta.role) {
    if (!auth.user && auth.token) {
      await auth.me(); // Fetch user if not loaded yet
    }
    if (auth.role !== to.meta.role) {
      return { name: "home" };
    }
  }
});

export default router;
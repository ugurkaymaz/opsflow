import { createRouter, createWebHistory } from 'vue-router'

import DashboardView from '../views/DashboardView.vue'
import OperationsView from '../views/OperationsView.vue'
import RecordsView from '../views/RecordsView.vue'
import ReportsView from '../views/ReportsView.vue'
import LoginView from '../views/LoginView.vue'

import { isAuthenticated } from '../services/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),

  routes: [
    {
      path: '/',
      redirect: '/dashboard',
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: {
        guestOnly: true,
      },
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/operations',
      name: 'operations',
      component: OperationsView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/records',
      name: 'records',
      component: RecordsView,
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/reports',
      name: 'reports',
      component: ReportsView,
      meta: {
        requiresAuth: true,
      },
    },
  ],
})

router.beforeEach((to) => {
  const authenticated = isAuthenticated()

  if (to.meta.requiresAuth && !authenticated) {
    return {
      name: 'login',
    }
  }

  if (to.meta.guestOnly && authenticated) {
    return {
      name: 'dashboard',
    }
  }

  return true
})

export default router

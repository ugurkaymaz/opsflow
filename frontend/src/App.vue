<script setup>
import { computed } from 'vue'
import {
  RouterLink,
  RouterView,
  useRoute,
  useRouter,
} from 'vue-router'
import {
  getCurrentUser,
  removeToken,
} from './services/auth'

const route = useRoute()
const router = useRouter()

const isLoginPage = computed(
  () => route.name === 'login'
)

const currentUser = computed(() => {
  if (isLoginPage.value) {
    return null
  }

  return getCurrentUser()
})

const userInitial = computed(() => {
  const name = currentUser.value?.name

  if (!name) {
    return 'U'
  }

  return name.charAt(0).toUpperCase()
})

async function handleLogout() {
  removeToken()

  await router.replace('/login')
}
</script>

<template>
  <RouterView v-if="isLoginPage" />

  <div
    v-else
    class="app-layout"
  >
    <aside class="sidebar">
      <div>
        <div class="brand">
          <h2>OpsFlow</h2>
        </div>

        <nav class="navigation">
          <RouterLink to="/dashboard">
            Dashboard
          </RouterLink>

          <RouterLink to="/operations">
            Operations
          </RouterLink>

          <RouterLink to="/records">
            Records
          </RouterLink>

          <RouterLink to="/reports">
            Reports
          </RouterLink>
        </nav>
      </div>

      <div class="sidebar-footer">
        <div
          v-if="currentUser"
          class="user-profile"
        >
          <div class="user-avatar">
            {{ userInitial }}
          </div>

          <div class="user-details">
            <div class="user-name">
              {{ currentUser.name }}
            </div>

            <div class="user-email">
              {{ currentUser.email }}
            </div>

            <span class="user-role">
              {{ currentUser.role }}
            </span>
          </div>
        </div>

        <button
          class="logout-button"
          type="button"
          @click="handleLogout"
        >
          <svg
            class="logout-icon"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <path
              d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"
            />
            <polyline
              points="16 17 21 12 16 7"
            />
            <line
              x1="21"
              y1="12"
              x2="9"
              y2="12"
            />
          </svg>

          <span>Logout</span>
        </button>
      </div>
    </aside>

    <section class="content">
      <RouterView />
    </section>
  </div>
</template>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  font-family: Arial, sans-serif;
}

.sidebar {
  box-sizing: border-box;
  width: 230px;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 24px 16px 18px;
  background: #1f2937;
  color: white;
}

.brand {
  margin-bottom: 32px;
  padding: 0 10px;
}

.brand h2 {
  margin: 0;
  color: #ffffff;
  font-size: 22px;
  font-weight: 700;
}

.navigation {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.navigation a {
  padding: 12px 14px;
  border-radius: 6px;
  color: #d1d5db;
  text-decoration: none;
  transition:
    background 0.15s ease,
    color 0.15s ease;
}

.navigation a:hover {
  background: #374151;
  color: white;
}

.navigation a.router-link-active {
  background: #4b5563;
  color: white;
}

.sidebar-footer {
  padding-top: 18px;
  border-top: 1px solid #374151;
}

.user-profile {
  display: flex;
  align-items: flex-start;
  gap: 11px;
  padding: 0 6px 16px;
}

.user-avatar {
  display: flex;
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: #374151;
  color: #ffffff;
  font-size: 15px;
  font-weight: 700;
}

.user-details {
  min-width: 0;
  flex: 1;
}

.user-name {
  overflow: hidden;
  margin-bottom: 3px;
  color: #f9fafb;
  font-size: 14px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-email {
  overflow: hidden;
  margin-bottom: 7px;
  color: #9ca3af;
  font-size: 11px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-role {
  display: inline-block;
  padding: 2px 7px;
  border-radius: 999px;
  background: #374151;
  color: #d1d5db;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.logout-button {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 9px;
  padding: 10px 12px;
  border: 0;
  border-radius: 6px;
  background: transparent;
  color: #9ca3af;
  font: inherit;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
  transition:
    background 0.15s ease,
    color 0.15s ease;
}

.logout-button:hover {
  background: #374151;
  color: #ffffff;
}

.logout-icon {
  width: 17px;
  height: 17px;
  flex-shrink: 0;
}

.content {
  flex: 1;
  min-width: 0;
  padding: 32px;
  background: #f5f7fa;
}
</style>

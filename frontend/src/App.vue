<script setup>
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="app-layout">
    <aside class="sidebar">
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
  width: 230px;
  padding: 24px 16px;
  background: #1f2937;
  color: white;
}

.brand {
  margin-bottom: 32px;
}

.brand h2 {
  margin: 0;
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
}

.navigation a:hover {
  background: #374151;
  color: white;
}

.navigation a.router-link-active {
  background: #4b5563;
  color: white;
}

.content {
  flex: 1;
  min-width: 0;
  padding: 32px;
  background: #f5f7fa;
}
</style>

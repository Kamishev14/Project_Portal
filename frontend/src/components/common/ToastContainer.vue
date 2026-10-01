<template>
  <div class="toast-wrapper">
    <div
      v-for="toast in toastStore.toasts"
      :key="toast.id"
      class="toast"
      :data-kind="toast.type"
    >
      {{ toast.message }}
    </div>
  </div>
</template>

<script setup>
import { useToastStore } from '@/stores/toast'

const toastStore = useToastStore()
</script>

<style scoped>
.toast-wrapper {
  position: fixed;
  right: 20px;
  top: 20px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.toast {
  min-width: 220px;
  max-width: 320px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 0.85rem;
  color: white;
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.28);
}

.toast[data-kind='success'] {
  background: #166534;
  border: 1px solid #22c55e;
}

.toast[data-kind='error'] {
  background: #7f1d1d;
  border: 1px solid #f87171;
}

.toast[data-kind='info'] {
  background: #1d4ed8;
  border: 1px solid #60a5fa;
}
</style>
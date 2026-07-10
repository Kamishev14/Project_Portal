<template>
  <span :class="badgeClass">{{ label }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, required: true }
})

const map = {
  PENDING:            { cls: 'badge-pending',  label: '⏳ Pending' },
  APPROVED:           { cls: 'badge-approved', label: '✅ Approved' },
  DECLINED:           { cls: 'badge-declined', label: '❌ Declined' },
  REVISION_REQUESTED: { cls: 'badge-revision', label: '🔄 Revision Requested' }
}

const badgeClass = computed(() => map[props.status]?.cls ?? 'badge-pending')
const label      = computed(() => map[props.status]?.label ?? props.status)
</script>

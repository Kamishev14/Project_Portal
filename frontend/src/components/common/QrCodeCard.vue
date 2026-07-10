<template>
  <div class="card flex flex-col items-center gap-4">
    <div class="text-center">
      <p class="text-sm text-gray-500 font-medium uppercase tracking-wide">Queue Number</p>
      <p class="text-5xl font-bold text-primary-600 mt-1">#{{ project.queueNumber }}</p>
    </div>

    <!-- QR code — encodes the visitor URL so scanning joins the queue -->
    <div class="relative">
      <img
        :src="qrUrl"
        alt="Scan to track your queue position"
        class="w-44 h-44 rounded-lg border border-gray-200"
      />
      <div class="absolute -bottom-2 left-1/2 -translate-x-1/2 bg-gray-1000 border border-gray-200 rounded-full px-2 py-0.5 text-xs text-gray-500 whitespace-nowrap">
        scan to join queue
      </div>
    </div>

    <div class="w-full space-y-2 pt-2">
      <!-- Visitor link — same as what's in the QR -->
      <a
        :href="project.qrqVisitorUrl"
        target="_blank"
        rel="noopener noreferrer"
        class="flex items-center justify-between w-full px-3 py-2 bg-primary-50 rounded-lg hover:bg-primary-100 transition-colors group"
      >
        <div>
          <p class="text-xs font-medium text-primary-700">Track my position</p>
          <p class="text-xs text-primary-500 truncate max-w-[180px]">{{ project.qrqVisitorUrl }}</p>
        </div>
        <span class="text-primary-400 group-hover:text-primary-600 text-sm">↗</span>
      </a>

      <!-- Status board link — for the professor / display screen -->
      <a
        :href="project.qrqStatusUrl"
        target="_blank"
        rel="noopener noreferrer"
        class="flex items-center justify-between w-full px-3 py-2 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors group"
      >
        <div>
          <p class="text-xs font-medium text-gray-600">Queue display board</p>
          <p class="text-xs text-gray-400 truncate max-w-[180px]">{{ project.qrqStatusUrl }}</p>
        </div>
        <span class="text-gray-400 group-hover:text-gray-600 text-sm">↗</span>
      </a>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { projectApi } from '@/api'

const props = defineProps({
  project: { type: Object, required: true }
})

// The QR image endpoint — backend generates PNG with visitor URL encoded inside
const qrUrl = computed(() => projectApi.getQrUrl(props.project.id))
</script>

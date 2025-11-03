<template>
  <span class="channel-badge" :class="`channel-${normalized}`">
    <i class="icon" />
    <slot>{{ label || normalized }}</slot>
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  channel: { type: String, default: 'inbox' },
  label: { type: String, default: '' }
})

const normalized = computed(() => {
  const v = (props.channel || 'inbox').toLowerCase()
  return v === 'inapp' ? 'inbox' : v
})
const label = computed(() => props.label)
</script>

<style scoped>
.channel-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  background: #f8fafc;
  color: #0f172a;
  border: 1px dashed #e2e8f0;
}
.channel-badge .icon {
  width: 8px;
  height: 8px;
  border-radius: 2px;
  background: currentColor;
}
.channel-inbox { color: #0ea5e9; background: #f0f9ff; border-color: #bae6fd; }
.channel-email { color: #22c55e; background: #ecfdf5; border-color: #bbf7d0; }
.channel-sms { color: #f59e0b; background: #fffbeb; border-color: #fde68a; }
.channel-webhook { color: #a855f7; background: #f5f3ff; border-color: #e9d5ff; }
</style>



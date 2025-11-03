<template>
  <div v-if="show" class="confirm-overlay" @click.self="onCancel">
    <div class="confirm-dialog" :class="{ enter: show }">
      <header>
        <h3>{{ title }}</h3>
      </header>
      <section>
        <p class="message">{{ message }}</p>
        <slot />
      </section>
      <footer>
        <button class="btn ghost" @click="onCancel">取消</button>
        <button class="btn danger" @click="onConfirm">确认</button>
      </footer>
    </div>
  </div>
</template>

<script setup>
defineProps({
  show: { type: Boolean, default: false },
  title: { type: String, default: '请确认' },
  message: { type: String, default: '' }
})

const emit = defineEmits(['confirm', 'cancel'])

function onConfirm() { emit('confirm') }
function onCancel() { emit('cancel') }
</script>

<style scoped>
.confirm-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.45);
  display: grid;
  place-items: center;
  z-index: 1000;
  animation: fadeIn 120ms ease-out;
}
.confirm-dialog {
  width: 420px;
  max-width: 92vw;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(2,6,23,.16), 0 2px 10px rgba(2,6,23,.08);
  overflow: hidden;
  transform: translateY(8px) scale(.98);
  opacity: 0;
  animation: popIn 160ms ease-out forwards;
}
.confirm-dialog header { padding: 16px 18px 0; }
.confirm-dialog h3 { margin: 0; font-size: 16px; letter-spacing: .2px; }
.confirm-dialog section { padding: 10px 18px 0; }
.confirm-dialog .message { color: #475569; line-height: 1.6; }
.confirm-dialog footer { padding: 16px 18px 18px; display:flex; gap: 10px; justify-content: flex-end; }
.btn { height: 34px; padding: 0 14px; border-radius: 10px; border: 1px solid transparent; background: #0ea5e9; color: #fff; font-weight: 600; letter-spacing: .2px; cursor: pointer; transition: transform .12s ease, box-shadow .12s ease; }
.btn:hover { transform: translateY(-1px); box-shadow: 0 6px 16px rgba(14,165,233,.25); }
.btn.ghost { background: #f8fafc; color: #0f172a; border-color: #e2e8f0; }
.btn.ghost:hover { box-shadow: 0 4px 12px rgba(2,6,23,.08); }
.btn.danger { background: #ef4444; }
.btn.danger:hover { box-shadow: 0 6px 16px rgba(239,68,68,.25); }

@keyframes popIn { to { transform: translateY(0) scale(1); opacity: 1; } }
@keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
</style>



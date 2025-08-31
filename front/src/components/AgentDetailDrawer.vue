<template>
  <div class="drawer">
    <div class="panel glass">
      <div class="head">
        <div class="title">
          <span class="dot" :style="{background: agent.color}"></span>
          {{ agent.name }}
        </div>
        <button class="close" @click="$emit('close')">×</button>
      </div>
      <div class="status">
        <span class="status-dot" :class="statusClass"></span>
        <span class="s">{{ statusText }}</span>
      </div>
      <div class="logs">
        <div v-for="(line, idx) in agent.log" :key="idx" class="log">{{ line }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({ agent: { type: Object, required: true } })

const statusText = computed(()=>{
  return { idle:'待处理', queue:'排队中', running:'进行中', done:'完成', fail:'失败' }[props.agent.status] || '未知'
})
const statusClass = computed(()=>{
  return { idle:'status-wait', queue:'status-wait', running:'status-proc', done:'status-done', fail:'status-fail' }[props.agent.status] || 'status-wait'
})
</script>

<style scoped>
.drawer{
  position:fixed; inset:0; display:grid; place-items:center; z-index:30;
  background: radial-gradient(800px 600px at 70% 20%, rgba(15,22,48,.65), rgba(7,11,22,.75));
}
.panel{width:min(560px, 92vw); max-height:80vh; overflow:auto; padding:16px; border-radius:16px}
.head{display:flex; align-items:center; justify-content:space-between}
.title{font-weight:700; display:flex; align-items:center; gap:8px}
.dot{display:inline-block; width:12px; height:12px; border-radius:50%}
.close{font-size:22px; line-height:1; width:36px; height:36px; border-radius:10px; background:var(--glass); color:var(--text)}
.status{margin:8px 0 12px 0; color:var(--muted)}
.logs{display:flex; flex-direction:column; gap:8px}
.log{padding:10px; border-radius:10px; background:rgba(255,255,255,.03); border:1px solid rgba(255,255,255,.06)}
</style>

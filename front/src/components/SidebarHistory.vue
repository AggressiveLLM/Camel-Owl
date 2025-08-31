<template>
  <div class="history">
    <div class="title">历史追溯</div>

    <div v-if="!history.length" class="empty">
      还没有任务记录，提交一个任务试试 →
    </div>

    <div class="list">
      <button v-for="item in history" :key="item.id" class="row" @click="$emit('select', item)">
        <div class="row-top">
          <span class="time">{{ item.time }}</span>
          <span class="tag">共 {{ item.agents.length }} Agents</span>
        </div>
        <div class="task">{{ item.task }}</div>
      </button>
    </div>
  </div>
</template>

<script setup>
defineProps({ history: { type: Array, default: () => [] } })
</script>

<style scoped>
.history{display:flex;flex-direction:column;gap:10px;min-height:0}
.title{font-weight:700;margin-bottom:4px}
.empty{color:var(--muted);font-size:13px;padding:10px 0}
.list{display:flex;flex-direction:column;gap:8px;overflow:auto}
.row{
  text-align:left;
  padding:10px;border-radius:12px;background:var(--glass);
  border:1px solid rgba(255,255,255,.06);
}
.row:hover{border-color: rgba(77,179,255,.35)}
.row-top{display:flex;justify-content:space-between;align-items:center;margin-bottom:6px}
.time{font-size:12px;color:var(--muted)}
.task{font-size:14px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
</style>

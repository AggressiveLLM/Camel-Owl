<template>
  <div style="width: 100%; height: 100%; z-index: 100;">
    <!-- 左侧侧边栏 -->
    <div style="width: 19%; height: 96vh; float: left; margin-top: 2vh; margin-left: 1%;">
      <aside class="sidebar glass" style="height: 100%;" :class="{ collapsed: sidebarCollapsed }">
        <div class="brand">
          <!-- 折叠按钮 -->
          <button class="toggle-btn" @click="toggleSidebar">☰</button>
          <div class="logo">OWL</div>
          <div class="desc" v-if="!sidebarCollapsed">多智能体任务中心</div>
        </div>

        <!-- 历史任务列表组件 -->
        <SidebarHistory :history="history" @select="loadFromHistory" v-if="!sidebarCollapsed" />

        <div class="footer" v-if="!sidebarCollapsed">
          <div class="tag">区块实时可视化</div>
        </div>
      </aside>
    </div>

    <!-- 右侧主要内容 -->
    <main class="main" style="width: 78%; height: 96vh; float: left; margin-top: 2vh; margin-left: 1%;">
      <!-- 立方体任务可视化 -->
      <section class="stage glass shadow-glow">
        <ParticleCube ref="cube" :agents="agents" :taskPulse="taskPulse" @agent-click="openAgentDetail" />
      </section>

      <!-- 任务输入区 -->
      <section class="input-area glass">
        <form @submit.prevent="dispatchTask">
          <input v-model="userInput" class="input" placeholder="在此输入任务指令，例如：整理销售数据并生成周报..." />
          <div class="actions">
            <button class="action-btn" type="submit">提交任务</button>
            <span class="hint">回车提交 · 派发到各 Agent</span>
          </div>
        </form>
      </section>
    </main>

    <!-- ------------------------ Agent 详情抽屉 ------------------------ -->
    <AgentDetailDrawer v-if="activeAgent" :agent="activeAgent" @close="activeAgent = null" />

    <!-- 通知栏 -->
    <div class="panel glass" v-if="showModal">
      <!-- <div class="panel glass"> -->
      <div class="modal-content">
        <h2 class="modal-title">任务输出</h2>
        <div class="modal-body">
          <p>{{ modalContent }}</p>
           <!-- <p>11111111111111111111111111111111111111111111111111111111111122222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222233333333333333333333333333333333333333333333333333333333333333333111111111111111111111111111111111111111111111111111111111111222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222333333333333333333333333333333333333333333333333333333333333333331111111111111111111111111111111111111111111111111111111111112222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222223333333333333333333333333333333333333333333333333333333333333333311111111111111111111111111111111111111111111111111111111111122222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222233333333333333333333333333333333333333333333333333333333333333333111111111111111111111111111111111111111111111111111111111111222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222333333333333333333333333333333333333333333333333333333333333333331111111111111111111111111111111111111111111111111111111111112222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222223333333333333333333333333333333333333333333333333333333333333333311111111111111111111111111111111111111111111111111111111111122222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222233333333333333333333333333333333333333333333333333333333333333333111111111111111111111111111111111111111111111111111111111111222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222333333333333333333333333333333333333333333333333333333333333333331111111111111111111111111111111111111111111111111111111111112222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222223333333333333333333333333333333333333333333333333333333333333333311111111111111111111111111111111111111111111111111111111111122222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222233333333333333333333333333333333333333333333333333333333333333333</p> -->
        </div>
          <!-- <button @click="showModal = false" class="modal-close-btn">&times;</button> -->
          <div class="fork_button" @click="showModal = false" style="width: 40px; height: 40px; border-radius: 20px; background-color: rgb(29, 187, 255); margin: auto;">
            <img src="../src/assets/fork_icon.png" alt="" style="width: 15px; height: 15px; margin: 12.5px auto;">
          </div>
        </div>
      </div>
    </div>

</template>








<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import SidebarHistory from './components/SidebarHistory.vue'
import ParticleCube from './components/ParticleCube.vue'
import AgentDetailDrawer from './components/AgentDetailDrawer.vue'

/* -------------------- 响应式数据 -------------------- */
const userInput = ref('')      // 用户输入任务指令
const taskPulse = ref(0)       // 触发立方体脉冲动画
const history = ref([])        // 保存历史任务
const activeAgent = ref(null)  // 当前点击的 Agent
const sidebarCollapsed = ref(false) // 侧边栏折叠状态
const showModal = ref(false)       // 控制任务输出弹框显示
const modalContent = ref('')       // 弹框显示内容

// 初始化 10 个 Agent 数据
const agents = reactive(
  Array.from({ length: 10 }).map((_, i) => ({
    id: i + 1,
    name: 'Agent ' + (i + 1),
    status: 'idle', // idle | queue | running | done | fail
    log: [],
    color: '#4db3ff',
  }))
)

/* -------------------- 辅助方法 -------------------- */

// 历史任务入栈
// function pushHistory(task, result) {
//   history.value.unshift({
//     id: Date.now(),
//     time: new Date().toLocaleString(),
//     task, result,
//     agents: JSON.parse(JSON.stringify(agents))
//   })
//   if (history.value.length > 100) history.value.pop()
// }

// 模拟 Agent 执行任务动画
function simulateAgent(agent, task) {
  agent.status = 'queue'
  agent.log.push('接收任务：' + task)
  setTimeout(() => {
    agent.status = 'running'
    agent.color = '#ffd166'
    agent.log.push('分析&思考中...')
  }, 400 + Math.random() * 600)

  const total = 2000 + Math.random() * 2200
  setTimeout(() => {
    const ok = Math.random() > 0.08
    agent.status = ok ? 'done' : 'fail'
    agent.color = ok ? '#45ffa2' : '#ff5c7a'
    agent.log.push(ok ? '执行完成 ✅' : '执行失败 ❌')
  }, total)
}

// 打开 Agent 详情抽屉
function openAgentDetail(agent) { activeAgent.value = agent }

// 从历史任务加载
function loadFromHistory(item) {
  alert('已加载历史任务：\n' + item.task + '\n\n你可以在左侧继续查看详细记录。')
}

// 切换侧边栏折叠
function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
  setTimeout(() => window.dispatchEvent(new Event('resize')), 320)
}

/* -------------------- 提交任务 -------------------- */
async function dispatchTask() {
  if (!userInput.value.trim()) return
  const task = userInput.value.trim()

  // 触发立方体动画
  taskPulse.value = Math.random()

  // 本地 Agent 状态动画
  agents.forEach(a => {
    a.status = 'idle'
    a.color = '#4db3ff'
    a.log = []
    simulateAgent(a, task)
  })

  try {

    // 清空输入框
    userInput.value = ''
    // 并发调用后端两个接口
    const [res1, res2] = await Promise.all([
      fetch('/api/tasks', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title: task })
      }),
      fetch('/api/owl/run', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title: task })
      })
    ])
    
    // 处理 res1 (/api/tasks)
    let result1
    if (res1.headers.get('content-type')?.includes('application/json')) {
      const data1 = await res1.json()
      result1 = `任务已保存 (id=${data1.id || '未知'})`
    } else {
      result1 = await res1.text()
    }

    // 处理 res2 (/api/owl/run)
    let result2
    if (res2.headers.get('content-type')?.includes('application/json')) {
      const data2 = await res2.json()
      result2 = data2.stdout || data2.stderr || 'Python 执行但无输出'
    } else {
      result2 = await res2.text()
    }

    modalContent.value = `任务保存状态:\n${result1}\n\nPython 输出:\n${result2}`

    showModal.value = true

  } catch (err) {
    console.error('接口调用失败:', err)
    modalContent.value = '接口调用失败，请查看控制台'
    showModal.value = true
    // pushHistory(task, '接口调用失败')
  }

  
}


</script>

<style scoped>
.panel {
  width: 30%;
  height: 74vh;
  padding: 16px;
  border-radius: 16px;
  display: flex;
  margin-left: 35%;
  margin-top: 13vh;
  justify-content: center;
  align-items: center;
  position: fixed;
  z-index: 1000;
}

.modal-content {
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  border-radius: 12px;
  padding: 20px;
  color: white;
  text-align: center;
  position: relative;
  display: flex;
  flex-direction: column;
}

.modal-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 15px;
}

.modal-body {
  white-space: pre-wrap;  /* 自动换行 */
  word-wrap: break-word;  /* 遇到长单词时也换行 */
  font-family: 'Courier New', Courier, monospace;
  font-size: 1rem;
  color: #e0e0e0;
  max-height: 70%; /* 限制最大高度 */
  overflow-y: auto; /* 如果内容超出高度，则显示滚动条 */
  
  /* 美化滚动条 */
  scrollbar-width: thin; /* Firefox */
  scrollbar-color: rgb(29, 187, 255) #333; /* 滚动条颜色：滑块#00ffcc，背景#333 */
}

/* 美化滚动条（Webkit） */
.modal-body::-webkit-scrollbar {
  width: 8px; /* 滚动条宽度 */
}

.modal-body::-webkit-scrollbar-thumb {
  background-color: rgb(29, 187, 255); /* 滑块颜色 */
  border-radius: 4px;
}

.modal-body::-webkit-scrollbar-track {
  background-color: #333; /* 背景颜色 */
}

.fork_button {
  cursor: pointer;
}
</style>

<template>
    <div class="layout" :class="{ collapsed: sidebarCollapsed }">
      <!-- ------------------------ 侧边栏 ------------------------ -->
      <aside class="sidebar glass" :class="{ collapsed: sidebarCollapsed }">
        <div class="brand">
          <!-- 折叠按钮 -->
          <button class="toggle-btn" @click="toggleSidebar">☰</button>
          <div class="logo">OWL</div>
          <div class="desc" v-if="!sidebarCollapsed">多智能体任务中心</div>
        </div>

        <!-- 历史任务列表组件 -->
        <SidebarHistory :history="history" @select="loadFromHistory" v-if="!sidebarCollapsed" />

        <div class="footer" v-if="!sidebarCollapsed">
          <div class="tag">实时可视化 · 科技感</div>
        </div>
      </aside>

      <!-- ------------------------ 主内容区 ------------------------ -->
      <main class="main">
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

      <Transition name="fade">
        <div v-if="showModal" class="fixed inset-0 flex justify-center items-center z-50">

          <div class="absolute inset-0 bg-black/60 backdrop-blur-sm"></div>


          <div
            class="relative w-3/4 max-w-xl p-6 rounded-2xl border-2 border-cyan-400 border-opacity-50 shadow-[0_0_20px_cyan] animate-fade-in">
            <h2 class="text-2xl font-bold text-cyan-400 mb-4 tracking-wide">任务输出</h2>


            <div class="absolute inset-0 z-0 overflow-hidden rounded-2xl">
              <div class="particle" v-for="n in 50" :key="n"></div>
            </div>


            <div
              class="relative z-10 max-h-96 overflow-y-auto font-mono text-sm text-white whitespace-pre-wrap p-2 bg-black/50 rounded-lg">
              {{ modalContent }}
            </div>


            <button @click="showModal = false"
              class="absolute top-3 right-3 text-cyan-400 hover:text-red-500 text-xl font-bold">&times;</button>
          </div>
        </div>
      </Transition>




      <!-- ------------------------ Agent 详情抽屉 ------------------------ -->
      <AgentDetailDrawer v-if="activeAgent" :agent="activeAgent" @close="activeAgent = null" />

    </div>

</template>








<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import ParticleCube from './components/ParticleCube.vue'
import SidebarHistory from './components/SidebarHistory.vue'
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
function pushHistory(task, result) {
  history.value.unshift({
    id: Date.now(),
    time: new Date().toLocaleString(),
    task, result,
    agents: JSON.parse(JSON.stringify(agents))
  })
  if (history.value.length > 100) history.value.pop()
}

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
    pushHistory(task, '接口调用失败')
  }

  // 清空输入框
  userInput.value = ''
}


</script>

<style>
.panel {
  width: 60%;
  max-height: 40%;
  margin-left: 20%;
  margin-top: 30%;
  overflow: auto;
  padding: 16px;
  border-radius: 16px;
}

.head {
  display: flex;
  align-items: center;
  justify-content: space-between
}

.title {
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px
}

.dot {
  display: inline-block;
  width: 12px;
  height: 12px;
  border-radius: 50%
}

.close {
  font-size: 22px;
  line-height: 1;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--glass);
  color: var(--text)
}

.status {
  margin: 8px 0 12px 0;
  color: var(--muted)
}

.logs {
  display: flex;
  flex-direction: column;
  gap: 8px
}

.log {
  padding: 10px;
  border-radius: 10px;
  background: rgba(255, 255, 255, .03);
  border: 1px solid rgba(255, 255, 255, .06)
}
</style>

<template>
  <div ref="container" class="wrap" @mousemove="onHover">
    <div class="hud">
      <div class="legend">
        <span class="status-dot status-wait"></span>待处理
        <span class="status-dot status-proc"></span>进行中
        <span class="status-dot status-done"></span>完成
        <span class="status-dot status-fail"></span>失败
      </div>
    </div>

    <div v-for="a in agents" :key="a.id"
         class="tooltip"
         :class="{show: hoverAgent && hoverAgent.id===a.id}"
         :style="tooltipStyle">
      {{ a.name }} · {{ statusText(a.status) }}
    </div>
  </div>
</template>

<script setup>
import * as THREE from 'three'
import { EffectComposer } from 'three/examples/jsm/postprocessing/EffectComposer.js'
import { RenderPass } from 'three/examples/jsm/postprocessing/RenderPass.js'
import { UnrealBloomPass } from 'three/examples/jsm/postprocessing/UnrealBloomPass.js'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'

const props = defineProps({
  agents: { type: Array, default: () => [] },
  taskPulse: { type: Number, default: 0 }
})
const emit = defineEmits(['agent-click'])

const container = ref(null)
let renderer, scene, camera, frameId
let mainGroup, points, pointsMat, orbitGroup, linkLinesGroup
let raycaster, mouseN = new THREE.Vector2()
let composer, controls
let shockwaves = [] // 能量波数组
const hoverAgent = ref(null)
const tooltipStyle = ref({ left:'-9999px', top:'-9999px' })
const couriers = [] // 沿连线奔跑的小粒子

function statusText(s){return {idle:'待处理', queue:'待处理', running:'进行中', done:'完成', fail:'失败'}[s] || '未知'}

function buildMainCube(){
  mainGroup = new THREE.Group()

  // 粒子立方体
  const cubeSize = 14
  const density = 26
  const geo = new THREE.BufferGeometry()
  const positions = []
  const colors = []
  for (let x=0; x<density; x++){
    for (let y=0; y<density; y++){
      for (let z=0; z<density; z++){
        const fx = (x/(density-1)-.5)*cubeSize
        const fy = (y/(density-1)-.5)*cubeSize
        const fz = (z/(density-1)-.5)*cubeSize
        const edge = (x===0||y===0||z===0||x===density-1||y===density-1||z===density-1)
        if (edge || Math.random()<.08){
          positions.push(fx,fy,fz)
          const c = new THREE.Color(0x5cc3ff)
          colors.push(c.r, c.g, c.b)
        }
      }
    }
  }
  geo.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3))
  geo.setAttribute('color', new THREE.Float32BufferAttribute(colors, 3))

  pointsMat = new THREE.PointsMaterial({
    size: .16,
    vertexColors: true,
    transparent: true,
    opacity: .95,
    depthWrite: false,
    blending: THREE.AdditiveBlending
  })
  points = new THREE.Points(geo, pointsMat)
  mainGroup.add(points)

  // 外框线
  const edges = new THREE.EdgesGeometry(new THREE.BoxGeometry(cubeSize, cubeSize, cubeSize))
  const edgeLines = new THREE.LineSegments(edges, new THREE.LineBasicMaterial({ color: 0x4db3ff, transparent:true, opacity:.35 }))
  mainGroup.add(edgeLines)

  // 中心脉冲球
  const core = new THREE.Mesh(
    new THREE.SphereGeometry(1.6, 32, 32),
    new THREE.MeshBasicMaterial({ color: 0x72c8ff, transparent:true, opacity:.55 })
  )
  mainGroup.add(core)

  scene.add(mainGroup)
}

function buildAgents(){
  orbitGroup = new THREE.Group()
  const R = 18
  const n = props.agents.length
  props.agents.forEach((a,i)=>{
    const angle = i/n * Math.PI*2
    const small = new THREE.Group()
    small.userData.agent = a

    // 内部粒子团（参考风格）
    const clusterGeo = new THREE.BufferGeometry()
    const pts = []
    for(let j=0;j<140;j++){
      const vx = (Math.random()-.5)*2.2
      const vy = (Math.random()-.5)*2.2
      const vz = (Math.random()-.5)*2.2
      pts.push(new THREE.Vector3(vx,vy,vz))
    }
    clusterGeo.setFromPoints(pts)
    const cluster = new THREE.Points(clusterGeo, new THREE.PointsMaterial({
      size:.2, color:0x4db3ff, transparent:true, opacity:.95, blending:THREE.AdditiveBlending
    }))
    small.add(cluster)

    // 外框
    const edges = new THREE.EdgesGeometry(new THREE.BoxGeometry(2.8,2.8,2.8))
    const edgeLines = new THREE.LineSegments(edges, new THREE.LineBasicMaterial({ color: 0x4db3ff, transparent:true, opacity:.35 }))
    small.add(edgeLines)

    const x = Math.cos(angle)*R
    const z = Math.sin(angle)*R
    small.position.set(x, 0, z)
    orbitGroup.add(small)
  })
  scene.add(orbitGroup)

  // 连线 & 物流粒子
  linkLinesGroup = new THREE.Group()
  props.agents.forEach((a,i)=>{
    const lineGeo = new THREE.BufferGeometry().setFromPoints([
      new THREE.Vector3(0,0,0),
      orbitGroup.children[i].position.clone()
    ])
    const line = new THREE.Line(lineGeo, new THREE.LineBasicMaterial({
      color: 0x4db3ff, transparent:true, opacity:.28
    }))
    linkLinesGroup.add(line)

    // 沿线移动的“快递粒子”
    const courier = new THREE.Mesh(
      new THREE.SphereGeometry(0.15, 8, 8),
      new THREE.MeshBasicMaterial({ color: 0x9bd8ff, transparent:true, opacity:.9 })
    )
    courier.position.set(0,0,0)
    scene.add(courier)
    couriers.push({
      mesh: courier,
      target: orbitGroup.children[i].position.clone(),
      t: Math.random()
    })
  })
  scene.add(linkLinesGroup)
}

function spawnShockwave(){
  // 从主立方体中心扩散的环
  const ring = new THREE.Mesh(
    new THREE.RingGeometry(0.1, 0.2, 64),
    new THREE.MeshBasicMaterial({ color: 0x4db3ff, transparent:true, opacity:.8, side:THREE.DoubleSide })
  )
  ring.rotation.x = -Math.PI/2
  ring.position.set(0,0,0)
  scene.add(ring)
  shockwaves.push({ mesh: ring, life: 0 })
}

function onHover(e){
  mouseN.x = (e.offsetX / container.value.clientWidth) * 2 - 1
  mouseN.y = -(e.offsetY / container.value.clientHeight) * 2 + 1
  tooltipStyle.value = { left: e.clientX+'px', top: e.clientY-12+'px' }
}

function setup(){
  scene = new THREE.Scene()
  scene.fog = new THREE.FogExp2(0x0b1020, 0.03)

  camera = new THREE.PerspectiveCamera(55, container.value.clientWidth / container.value.clientHeight, 0.1, 300)
  camera.position.set(0, 6, 30)

  renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true })
  renderer.setPixelRatio(Math.min(2, window.devicePixelRatio))
  renderer.setSize(container.value.clientWidth, container.value.clientHeight)
  container.value.appendChild(renderer.domElement)

  // 后处理-泛光
  composer = new EffectComposer(renderer)
  const renderPass = new RenderPass(scene, camera)
  const bloomPass = new UnrealBloomPass(new THREE.Vector2(container.value.clientWidth, container.value.clientHeight), 0.7, 0.9, 0.1)
  composer.addPass(renderPass)
  composer.addPass(bloomPass)

  // 背景星云
  const bg = new THREE.Points(
    new THREE.BufferGeometry().setFromPoints(Array.from({length: 1200}).map(()=>{
      const v = new THREE.Vector3(
        (Math.random()-.5)*140,
        (Math.random()-.3)*90,
        (Math.random()-.5)*140)
      return v
    })),
    new THREE.PointsMaterial({ size: .7, color: 0x274a7a, transparent:true, opacity:.6 })
  )
  scene.add(bg)

  // 控制器
  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.autoRotate = true
  controls.autoRotateSpeed = 0.6
  controls.minDistance = 16
  controls.maxDistance = 80

  raycaster = new THREE.Raycaster()

  buildMainCube()
  buildAgents()
  animate()
  window.addEventListener('resize', onResize)
  container.value.addEventListener('click', onClick)
}

function onResize(){
  if(!container.value) return
  camera.aspect = container.value.clientWidth / container.value.clientHeight
  camera.updateProjectionMatrix()
  renderer.setSize(container.value.clientWidth, container.value.clientHeight)
  composer.setSize(container.value.clientWidth, container.value.clientHeight)
}

function animate(){
  frameId = requestAnimationFrame(animate)

  // 主体缓慢旋转
  if(mainGroup){
    mainGroup.rotation.y += 0.002
    mainGroup.rotation.x += 0.001
    const t = performance.now()*0.001
    const pos = points.geometry.attributes.position
    for (let i=0; i<pos.count; i++){
      const ix = i*3
      pos.array[ix+0] += Math.sin(t + i*.13)*0.0016
      pos.array[ix+1] += Math.sin(t*1.2 + i*.15)*0.0016
      pos.array[ix+2] += Math.cos(t + i*.11)*0.0016
    }
    pos.needsUpdate = true
  }

  // Agents 轻盈旋转 + 颜色状态跟随
  if(orbitGroup){
    orbitGroup.rotation.y -= 0.003
    orbitGroup.children.forEach((g, idx)=>{
      g.rotation.x -= 0.01
      g.rotation.y += 0.02
      const a = g.userData.agent
      const edges = g.children[1] // lineSegments
      const color = new THREE.Color(a.color || '#4db3ff')
      edges.material.color.lerp(color, 0.18)
      edges.material.opacity = a.status==='running'? 0.6 : 0.35
      // 状态闪烁效果（进行中）
      if(a.status==='running'){
        const pulse = (Math.sin(performance.now()*0.01)+1)/2
        g.children[0].material.size = 0.2 + pulse*0.08
      } else {
        g.children[0].material.size = 0.2
      }
    })
  }

  // hover 检测
  if(orbitGroup){
    raycaster.setFromCamera(mouseN, camera)
    const intersects = raycaster.intersectObjects(orbitGroup.children.map(g=>g.children[1])) // 用线框检测
    hoverAgent.value = intersects.length ? intersects[0].object.parent.userData.agent : null
  }

  // 更新连线
  if(linkLinesGroup && orbitGroup){
    linkLinesGroup.children.forEach((line, idx)=>{
      const p = orbitGroup.children[idx].position.clone()
      line.geometry.setFromPoints([new THREE.Vector3(0,0,0), p])
    })
  }

  // 快递粒子沿线移动
  couriers.forEach(c=>{
    c.t += 0.01
    if(c.t>1) c.t = 0
    const pos = new THREE.Vector3().lerpVectors(new THREE.Vector3(0,0,0), c.target, c.t)
    c.mesh.position.copy(pos)
    // 轻微闪烁
    const k = (Math.sin(performance.now()*0.02)+1)/2
    c.mesh.material.opacity = 0.6 + 0.4*k
  })

  // 能量冲击波更新
  shockwaves = shockwaves.filter(sw=>{
    sw.life += 0.02
    sw.mesh.scale.setScalar(1 + sw.life*18)
    sw.mesh.material.opacity = Math.max(0, 0.8 - sw.life*0.8)
    if(sw.life>=1){
      scene.remove(sw.mesh)
      return false
    }
    return true
  })

  controls.update()
  composer.render()
}

function onClick(){
  if(hoverAgent.value){
    emit('agent-click', hoverAgent.value)
  }
}

watch(()=>props.taskPulse, (v)=>{
  // 主立方体：粒子加速+泛光变色
  if(!points) return
  const oriSize = pointsMat.size
  const oriOpacity = pointsMat.opacity
  const target = { size: .4, opacity: 1 }

  const start = performance.now()
  function tween(){
    const k = Math.min(1, (performance.now()-start)/450)
    pointsMat.size = oriSize + (target.size-oriSize)*k
    pointsMat.opacity = oriOpacity + (target.opacity-oriOpacity)*k
    if(k<1) requestAnimationFrame(tween)
    else{
      const s2 = performance.now()
      function back(){
        const p = Math.min(1,(performance.now()-s2)/1000)
        pointsMat.size = target.size + (oriSize-target.size)*p
        pointsMat.opacity = target.opacity + (oriOpacity-target.opacity)*p
        if(p<1) requestAnimationFrame(back)
      }
      back()
    }
  }
  tween()

  // 触发 2~3 个能量冲击波
  const waves = 2 + Math.floor(Math.random()*2)
  for(let i=0;i<waves;i++){
    setTimeout(spawnShockwave, i*120)
  }
})

onMounted(()=>{
  setup()
})
onBeforeUnmount(()=>{
  cancelAnimationFrame(frameId)
  window.removeEventListener('resize', onResize)
  container.value?.removeEventListener('click', onClick)
  renderer?.dispose?.()
  composer?.dispose?.()
})
</script>

<style scoped>
.wrap{position:absolute; inset:0}
.hud{position:absolute; top:10px; right:10px; z-index:10}
.legend{font-size:12px; display:flex; gap:10px; align-items:center; background:rgba(7,11,22,.45); padding:8px 10px; border:1px solid rgba(255,255,255,.08); border-radius:10px}
</style>

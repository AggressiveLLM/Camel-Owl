# 项目概述

本项目是一个基于 **Vue.js** 前端、 **Spring Boot** 后端与 **Python FastAPI** 服务的多层架构应用。该系统旨在实现任务的管理、调度和执行，用户可以通过前端页面提交任务，后端服务负责调度和执行任务，并将相关请求转发给后端 Python 服务，最终通过 Python 调用 **OWL** 和 **CAMEL** 模型，完成任务的智能分析与处理。

## 系统架构

### 前端 (Vue.js)
- 使用 **Vue.js** 构建用户界面，提供动态交互和实时任务可视化。
- 前端负责接收用户输入的任务指令，展示任务的实时状态和执行结果。
- 使用了 **ParticleCube** 组件进行任务状态的视觉呈现，以增强用户体验。

### 后端 (Spring Boot)
- 后端使用 **Spring Boot** 作为服务端框架，提供 RESTful API 接口。
- 主要负责接收来自前端的任务请求，进行数据处理、任务调度，并将任务转发给 **Python FastAPI** 服务。
- 后端将任务指令和相关数据通过 HTTP 请求发送至 Python FastAPI 服务，获取执行结果后再返回给前端进行展示。

### 后端转发至 Python FastAPI
- **Spring Boot** 后端与 **Python FastAPI** 服务进行集成，后端通过 HTTP 请求转发数据到 Python 服务端。
- Python 服务负责调用 **OWL** 和 **CAMEL** 模型进行任务处理和智能分析，返回处理结果。

### Python FastAPI
- **Python FastAPI** 提供了处理逻辑和接口，负责接收任务请求，调用 **OWL** 和 **CAMEL** 模型进行数据分析和计算。
- 模型的输出结果通过 FastAPI 接口返回给 Spring Boot 后端，再由后端返回给前端进行展示。

## 主要功能

- **任务提交与管理**：用户可以在前端页面输入任务指令，提交至后端进行处理。
- **任务调度与转发**：后端接收任务请求，将任务数据通过 HTTP 请求转发给 Python FastAPI 服务进行执行。
- **实时反馈与展示**：通过前端页面的动态效果实时展示任务的状态和处理进度，确保用户对任务执行状态的实时了解。
- **任务结果可视化**：结合 **ParticleCube** 组件，通过动画和颜色变化展示任务的执行状态，并根据任务执行结果进行颜色变化和粒子动画的调整。

## 系统流程

1. **用户输入任务指令**：前端页面允许用户输入任务指令，并通过 **Vue.js** 发送请求到后端。
2. **后端处理请求并转发**：Spring Boot 后端接收任务请求，将任务数据通过 HTTP 请求转发给 Python FastAPI 服务。
3. **Python 处理任务**：Python FastAPI 接收到任务请求后，调用 **OWL** 和 **CAMEL** 模型进行任务处理，返回执行结果。
4. **前端展示结果**：前端根据后端返回的结果更新页面，展示任务的执行状态和最终输出，确保用户清晰地看到任务处理过程和结果。

## CAMEL & OWL: 多智能体框架

### 🐪 CAMEL (Communicative Agents for "Mind" Exploration of Large Scale)

**CAMEL** 是一个开源的 **多智能体框架**，由 [camel-ai](https://github.com/camel-ai) 开发，旨在为大规模的智能体模拟、任务协作和环境交互提供基础设施。其特点包括：

- 支持 **角色扮演** 和 **交互式模拟**。
- 集成 **工具和记忆**，可以用于多智能体系统的研究。
- 适用于 **大规模智能体系统的扩展性研究**。

👉 **CAMEL** 是为构建灵活且可扩展的智能体系统提供的基础框架。

### 🦉 OWL (Optimized Workforce Learning)

**OWL** 是一个基于 **CAMEL** 框架构建的 **工作流自动化框架**，专注于多个智能体之间的任务分解、协调和执行。其核心特性包括：

- **模块化设计**：包括规划器（Planner）、协调器（Coordinator）和工作者（Worker）三个模块。
- 集成 **模型上下文协议 (MCP)** 用于工具使用。
- 通过 **GAIA** 任务进行基准测试，展示了强大的性能。

👉 **OWL** 是一个基于 **CAMEL** 的实际应用层，提供自动化任务工作流，利用 CAMEL 的智能体框架来实现任务自动化。

### 🔗 CAMEL 和 OWL 的关系

- **CAMEL** 是多智能体系统的基础基础设施。
- **OWL** 是构建在 CAMEL 基础上的任务自动化系统。

**CAMEL** 提供了智能体基础设施，**OWL** 则提供了实际的任务自动化工作流，是研究平台与实践工具的结合。

### 📚 参考资料

- [CAMEL GitHub](https://github.com/camel-ai/camel)
- [OWL GitHub](https://github.com/camel-ai/owl)
- [CAMEL-AI 官方网站](https://www.camel-ai.org/)

## 技术栈

- **前端**：Vue.js, HTML5, CSS3, JavaScript
- **后端**：Spring Boot, Java, RESTful API
- **Python 服务**：FastAPI, Python, OWL 模型, CAMEL 模型


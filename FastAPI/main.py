from fastapi import FastAPI
from pydantic import BaseModel
import subprocess
import uvicorn
import logging

# 初始化日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI()

# 定义请求体模型
class Task(BaseModel):
    title: str

@app.post("/run")
def run_owl(task: Task):
    """
    执行 Python 脚本，并将 stdout/stderr/returncode 返回前端
    """
    try:
        logger.info(f"[RUN] 收到任务: {task.title}")

        # 构建命令
        bash_cmd = (
            "cd /root/WorkPlace/owl && "
            "source .venv/bin/activate && "
            "cd examples && "
            f"python run_deepseek_zh.py \"{task.title}\""
        )
        logger.info(f"[RUN] 执行命令: {bash_cmd}")

        # 运行子进程
        result = subprocess.run(
            bash_cmd,
            shell=True,
            capture_output=True,
            text=True,
            executable="/bin/bash"  # 指定 bash
        )

        logger.info(f"[RUN] 返回码: {result.returncode}")
        logger.info(f"[RUN] stdout:\n{result.stdout}")
        logger.error(f"[RUN] stderr:\n{result.stderr}")

        
        print("2:" , result.stdout)

        # 返回给前端完整信息
        return {
            "title": task.title,
            "returncode": result.returncode,
            "stdout": result.stdout,
            "stderr": result.stderr
        }

    except Exception as e:
        logger.error(f"[RUN] 异常: {e}")
        return {
            "title": task.title,
            "error": str(e)
        }

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8001)

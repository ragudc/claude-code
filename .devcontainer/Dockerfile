# Base: Node.js 20 (LTS) sobre Debian Bookworm
FROM node:20-bookworm-slim

# Evitar prompts interactivos en apt
ENV DEBIAN_FRONTEND=noninteractive

# ─── Dependencias del sistema ───────────────────────────────────────────────
RUN apt-get update && apt-get install -y \
    # Herramientas generales
    curl \
    git \
    bash \
    zsh \
    sudo \
    unzip \
    # Docker CLI (para correr docker-compose desde el contenedor)
    docker.io \
    docker-compose \
    # Python 3 + pip para el backend
    python3 \
    python3-pip \
    python3-venv \
    # Herramientas de red (útiles para debugging)
    iputils-ping \
    # Soporte para el sandbox de Claude Code en Linux
    bubblewrap \
    socat \
    # ripgrep (usado por Claude Code)
    ripgrep \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# ─── UV — Package manager de Python (más rápido que pip) ────────────────────
RUN curl -LsSf https://astral.sh/uv/install.sh | sh
ENV PATH="/root/.cargo/bin:/root/.local/bin:${PATH}"

# ─── Yarn (para el Frontend Next.js) ────────────────────────────────────────
RUN corepack enable && corepack prepare yarn@stable --activate

# ─── Claude Code CLI ─────────────────────────────────────────────────────────
# IMPORTANTE: WORKDIR debe estar definido antes de instalar para evitar
# que el instalador escanee todo el filesystem
WORKDIR /workspace
RUN npm install -g @anthropic-ai/claude-code

# ─── Dar permisos al usuario node para usar Docker y sudo ───────────────────
RUN usermod -aG docker node \
    && echo "node ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

# ─── Configurar PATH para el usuario node ───────────────────────────────────
USER node
ENV HOME=/home/node
ENV PATH="${HOME}/.local/bin:${PATH}"

# Instalar UV también para el usuario node
RUN curl -LsSf https://astral.sh/uv/install.sh | sh

WORKDIR /workspace

#!/bin/bash
# Script que corre una vez al crear el devcontainer

set -e

echo "🚀 Configurando Platziflix Dev Container..."

# ─── Frontend: instalar dependencias ────────────────────────────────────────
if [ -d "/workspace/Frontend" ]; then
    echo "📦 Instalando dependencias del Frontend..."
    cd /workspace/Frontend
    yarn install
    cd /workspace
fi

# ─── Backend: instalar dependencias con UV ──────────────────────────────────
if [ -d "/workspace/Backend" ]; then
    echo "🐍 Instalando dependencias del Backend..."
    cd /workspace/Backend
    uv sync
    cd /workspace
fi

echo ""
echo "✅ Devcontainer listo."
echo ""
echo "Comandos disponibles:"
echo "  claude              → Iniciar Claude Code CLI"
echo "  cd Backend && make start  → Levantar API + PostgreSQL (Docker Compose)"
echo "  cd Frontend && yarn dev   → Levantar Frontend (puerto 3000)"

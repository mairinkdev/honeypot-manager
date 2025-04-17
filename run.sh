#!/bin/bash

# Cores para formatação
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}================================${NC}"
echo -e "${GREEN}     HONEYPOT MANAGER          ${NC}"
echo -e "${BLUE}================================${NC}"

# Verifica se o Docker está instalado
if ! [ -x "$(command -v docker)" ]; then
  echo -e "${RED}Erro: Docker não está instalado.${NC}" >&2
  exit 1
fi

if ! [ -x "$(command -v docker-compose)" ]; then
  echo -e "${RED}Erro: Docker Compose não está instalado.${NC}" >&2
  exit 1
fi

# Menu de opções
echo -e "${YELLOW}[1]${NC} Iniciar todos os serviços"
echo -e "${YELLOW}[2]${NC} Parar todos os serviços"
echo -e "${YELLOW}[3]${NC} Ver logs"
echo -e "${YELLOW}[4]${NC} Reconstruir e iniciar"
echo -e "${YELLOW}[5]${NC} Sair"
echo

read -p "Escolha uma opção: " option

case $option in
  1)
    echo -e "${BLUE}Iniciando serviços...${NC}"
    docker-compose up -d
    
    echo -e "${GREEN}Serviços iniciados!${NC}"
    echo -e "Frontend: ${BLUE}http://localhost${NC}"
    echo -e "Backend API: ${BLUE}http://localhost:8080/api/attacks${NC}"
    echo -e "H2 Console: ${BLUE}http://localhost:8080/h2-console${NC}"
    ;;
  2)
    echo -e "${BLUE}Parando serviços...${NC}"
    docker-compose down
    echo -e "${GREEN}Serviços parados!${NC}"
    ;;
  3)
    echo -e "${BLUE}Exibindo logs... (Ctrl+C para sair)${NC}"
    docker-compose logs -f
    ;;
  4)
    echo -e "${BLUE}Reconstruindo e iniciando serviços...${NC}"
    docker-compose down
    docker-compose up -d --build
    
    echo -e "${GREEN}Serviços reconstruídos e iniciados!${NC}"
    echo -e "Frontend: ${BLUE}http://localhost${NC}"
    echo -e "Backend API: ${BLUE}http://localhost:8080/api/attacks${NC}"
    echo -e "H2 Console: ${BLUE}http://localhost:8080/h2-console${NC}"
    ;;
  5)
    echo -e "${GREEN}Saindo...${NC}"
    exit 0
    ;;
  *)
    echo -e "${RED}Opção inválida!${NC}"
    exit 1
    ;;
esac 
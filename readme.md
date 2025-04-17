# Honeypot Manager

Sistema avançado de gerenciamento de honeypots distribuídos em servidores, com detecção e análise de ataques em tempo real.

## 📋 Recursos

- **Backend**: API REST + WebSocket em Spring Boot
- **Frontend**: Dashboard React com gráficos e visualizações em tempo real
- **Agente**: Cliente Java multi-protocolo para instalação em servidores alvo
- **Containerização**: Docker e Docker Compose para fácil implantação
- **Banco de Dados**: H2 para armazenamento de logs de ataques

## 🏗️ Arquitetura

![Arquitetura](https://i.imgur.com/mVqCXsX.png)

O sistema é composto por:

- **Agentes Honeypot**: Simulam vários serviços (SSH, HTTP, FTP, SMTP, MySQL)
- **Backend API**: Recebe e armazena informações de ataques
- **WebSocket**: Fornece atualizações em tempo real para o frontend
- **Dashboard**: Interface de usuário para visualizar e analisar ataques

## 🐝 Protocolos suportados pelo Honeypot

- **SSH** (porta 2222)
- **HTTP** (porta 8080)
- **FTP** (porta 21)
- **SMTP** (porta 25)
- **MySQL** (porta 3306)

## 🚀 Como executar

### Execução manual (desenvolvimento)

#### Backend
```bash
cd backend
mvn spring-boot:run
```

O backend ficará disponível em `http://localhost:8081`

#### Agente
```bash
cd agent
mvn compile exec:java
```

#### Frontend
```bash
cd frontend
npm install
npm start
```

O frontend ficará disponível em `http://localhost:3000`

### Usando Docker (alternativa)

```bash
# Construir e iniciar todos os serviços
docker-compose up -d

# Ver logs
docker-compose logs -f
```

O sistema estará disponível em:
- **Frontend**: http://localhost
- **Backend API**: http://localhost:8081/api/attacks
- **Console H2**: http://localhost:8081/h2-console

## 📱 Como testar

1. Inicie todos os componentes (backend, agente e frontend)
2. Acesse o frontend em http://localhost:3000
3. Simule ataques usando um dos comandos:

```powershell
# Simular ataque SSH
Test-NetConnection -ComputerName localhost -Port 2222

# Simular ataque HTTP
Test-NetConnection -ComputerName localhost -Port 8080

# Simular ataque FTP
Test-NetConnection -ComputerName localhost -Port 21
```

Você verá os ataques aparecendo automaticamente no dashboard.

Também pode criar ataques de teste diretamente pela API:
```
http://localhost:8081/api/test-attack
```

## 📊 Dashboard

O dashboard fornece:
- Visualização em tempo real dos ataques
- Estatísticas gerais
- Lista detalhada dos ataques registrados
- Identificação de protocolos e origens dos ataques

## 🔧 Configuração e Personalização

### Personalizar portas do honeypot

Edite a lista `PORTS` no arquivo `agent/src/main/java/com/mairink/honeypot/HoneypotAgent.java`.

### Mudar banco de dados

Configure as propriedades no arquivo `backend/src/main/resources/application.properties`.

## 🔒 Segurança

Importante: Este projeto contém simuladores de serviços que podem ser considerados vulneráveis por design. Use apenas em ambientes controlados e para fins educacionais.

## 📁 Estrutura do Projeto

```
honeypot-manager/
├── backend/           # API Spring Boot
├── agent/             # Agente Honeypot Java  
├── frontend/          # Dashboard React
├── docker-compose.yml # Configuração Docker
└── README.md          # Este arquivo
```

## 🛠️ Tecnologias Utilizadas

- **Backend**: Java 17, Spring Boot, JPA, WebSocket, H2 Database
- **Frontend**: React, Material-UI, Axios, SockJS 
- **Agente**: Java 11, Socket Programming, Multi-threading
- **DevOps**: Docker, Docker Compose

## 📈 Valor para Portfólio

Este projeto demonstra diversas habilidades técnicas:
- Desenvolvimento full-stack com Java e React
- Programação concorrente e multi-threading
- Comunicação em tempo real com WebSockets
- Arquitetura de sistemas distribuídos
- Implementação de honeypots para cibersegurança
- Containerização com Docker

## 📝 Licença

Este projeto está licenciado sob a licença MIT.

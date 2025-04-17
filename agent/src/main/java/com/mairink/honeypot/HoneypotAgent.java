package com.mairink.honeypot;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.google.gson.Gson;

public class HoneypotAgent {

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static final String BACKEND_URL = "http://localhost:8081/api/attacks";

    // Portas de honeypot para diferentes serviços
    private static final int[] PORTS = {
        2222,  // SSH Fake
        8080,  // HTTP Fake
        21,    // FTP Fake
        25,    // SMTP Fake
        3306   // MySQL Fake
    };

    public static void main(String[] args) {
        System.out.println("[*] Honeypot Agent iniciando...");

        // Iniciar um listener para cada porta
        for (int port : PORTS) {
            startListener(port);
        }

        System.out.println("[*] Honeypot Agent rodando em múltiplas portas");
    }

    private static void startListener(int port) {
        // Verificar se a porta está disponível antes de tentar usá-la
        try {
            new ServerSocket(port).close();
        } catch (IOException e) {
            System.err.println("[!] Porta " + port + " não está disponível. Ignorando.");
            return;
        }

        executor.submit(() -> {
            String protocol = getProtocolForPort(port);
            System.out.println("[*] Iniciando honeypot " + protocol + " na porta " + port);

            try (ServerSocket serverSocket = new ServerSocket(port)) {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket socket = serverSocket.accept();
                        handleConnection(socket, port, protocol);
                    } catch (IOException e) {
                        System.err.println("[x] Erro ao aceitar conexão na porta " + port + ": " + e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.err.println("[x] Erro ao iniciar honeypot na porta " + port + ": " + e.getMessage());
            }
        });
    }

    private static void handleConnection(Socket socket, int port, String protocol) {
        try {
            InetAddress remote = socket.getInetAddress();
            int remotePort = socket.getPort();

            System.out.println("[!] Conexão " + protocol + " detectada de " + remote.getHostAddress() + ":" + remotePort);

            // Capturar o payload dependendo do protocolo
            String payload = capturePayload(socket, protocol);

            AttackLog log = new AttackLog();
            log.sourceIp = remote.getHostAddress();
            log.sourcePort = remotePort;
            log.targetPort = port;
            log.protocol = protocol;
            log.payload = payload;
            log.timestamp = LocalDateTime.now().toString();

            sendToBackend(log);

            // Enviar resposta apropriada dependendo do protocolo
            sendResponse(socket, protocol);

            socket.close();
        } catch (IOException e) {
            System.err.println("[x] Erro ao processar conexão: " + e.getMessage());
        }
    }

    private static String capturePayload(Socket socket, String protocol) {
        StringBuilder payload = new StringBuilder();
        
        try (InputStream in = socket.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            
            // Timeout para leitura
            socket.setSoTimeout(5000);
            
            String line;
            int maxLines = 10; // Limite de linhas para capturar
            
            switch (protocol) {
                case "HTTP":
                    // Captura headers HTTP
                    while ((line = reader.readLine()) != null && maxLines-- > 0) {
                        payload.append(line).append("\\n");
                        if (line.isEmpty()) break; // Header HTTP termina com linha em branco
                    }
                    break;
                    
                case "FTP":
                    // Envia banner FTP e captura comando
                    sendString(socket, "220 FTP Server ready.\r\n");
                    line = reader.readLine();
                    if (line != null) {
                        payload.append(line);
                    }
                    break;
                    
                case "SMTP":
                    // Envia banner SMTP e captura comando
                    sendString(socket, "220 SMTP Server ready\r\n");
                    line = reader.readLine();
                    if (line != null) {
                        payload.append(line);
                    }
                    break;
                    
                case "MySQL":
                    // Só captura bytes iniciais
                    byte[] buffer = new byte[128];
                    int read = in.read(buffer);
                    if (read > 0) {
                        payload.append("Binary data: ").append(bytesToHex(buffer, 0, read));
                    }
                    break;
                    
                case "SSH":
                default:
                    // Para SSH e outros, tenta ler o que vier
                    try {
                        while ((line = reader.readLine()) != null && maxLines-- > 0) {
                            payload.append(line).append("\\n");
                        }
                    } catch (Exception e) {
                        // Pode haver timeouts ou erros ao ler protocolos binários
                        byte[] binaryBuffer = new byte[128];
                        try {
                            int binaryRead = in.read(binaryBuffer);
                            if (binaryRead > 0) {
                                payload.append("Binary data: ").append(bytesToHex(binaryBuffer, 0, binaryRead));
                            }
                        } catch (Exception ex) {
                            // Ignora
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            return "(Erro ao capturar payload: " + e.getMessage() + ")";
        }
        
        return payload.length() > 0 ? payload.toString() : "(vazio)";
    }

    private static void sendResponse(Socket socket, String protocol) {
        try {
            switch (protocol) {
                case "HTTP":
                    String httpResponse = "HTTP/1.1 200 OK\r\n" +
                                         "Content-Type: text/html\r\n" +
                                         "Connection: close\r\n" +
                                         "\r\n" +
                                         "<html><body><h1>It works!</h1></body></html>";
                    sendString(socket, httpResponse);
                    break;
                case "SSH":
                    // SSH tem um handshake complexo, só mandamos algo básico
                    sendString(socket, "SSH-2.0-OpenSSH_7.4\r\n");
                    break;
                case "FTP":
                    // Já enviamos o banner no capturePayload
                    break;
                case "SMTP":
                    // Já enviamos o banner no capturePayload
                    break;
                case "MySQL":
                    // MySQL tem um protocolo complexo, não implementamos
                    break;
            }
        } catch (IOException e) {
            System.err.println("[x] Erro ao enviar resposta: " + e.getMessage());
        }
    }

    private static void sendString(Socket socket, String str) throws IOException {
        OutputStream out = socket.getOutputStream();
        out.write(str.getBytes());
        out.flush();
    }

    private static String getProtocolForPort(int port) {
        switch (port) {
            case 21: return "FTP";
            case 22:
            case 2222: return "SSH";
            case 25: return "SMTP";
            case 80:
            case 8080: return "HTTP";
            case 3306: return "MySQL";
            default: return "TCP";
        }
    }

    private static void sendToBackend(AttackLog log) {
        try {
            System.out.println("[DEBUG] Tentando enviar dados para: " + BACKEND_URL);
            System.out.println("[DEBUG] Dados a enviar: " + new Gson().toJson(log));
            
            URL url = new URL(BACKEND_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");

            String json = new Gson().toJson(log);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
                System.out.println("[DEBUG] Dados enviados com sucesso");
            }

            int code = con.getResponseCode();
            System.out.println("[>] Enviado para o backend. Resposta HTTP: " + code);
            
            // Ler a resposta para debug
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println("[DEBUG] Resposta do backend: " + response.toString());
            }

        } catch (Exception e) {
            System.err.println("[x] Falha ao enviar dados: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String bytesToHex(byte[] bytes, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = offset; i < offset + length && i < bytes.length; i++) {
            sb.append(String.format("%02x", bytes[i] & 0xff));
        }
        return sb.toString();
    }

    static class AttackLog {
        String sourceIp;
        Integer sourcePort;
        Integer targetPort;
        String protocol;
        String payload;
        String timestamp;
    }
} 
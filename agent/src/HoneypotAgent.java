import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import com.google.gson.Gson;

public class HoneypotAgent {

    private static final int PORT = 2222;
    private static final String BACKEND_URL = "http://localhost:8080/api/attacks";

    public static void main(String[] args) {
        System.out.println("[*] Honeypot Agent rodando na porta " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                InetAddress remote = socket.getInetAddress();
                int remotePort = socket.getPort();

                System.out.println("[!] Conexão detectada de " + remote.getHostAddress());

                AttackLog log = new AttackLog();
                log.sourceIp = remote.getHostAddress();
                log.sourcePort = remotePort;
                log.targetPort = PORT;
                log.protocol = "TCP";
                log.payload = "(simulado)";
                log.timestamp = LocalDateTime.now().toString();

                sendToBackend(log);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendToBackend(AttackLog log) {
        try {
            URL url = new URL(BACKEND_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");

            String json = new Gson().toJson(log);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes());
            }

            int code = con.getResponseCode();
            System.out.println("[>] Enviado para o backend. Resposta HTTP: " + code);

        } catch (Exception e) {
            System.err.println("[x] Falha ao enviar dados: " + e.getMessage());
        }
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

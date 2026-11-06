package com.poli.client;

import com.poli.common.Protocol;

import javax.swing.SwingUtilities;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** Socket del cliente: envía líneas al servidor y pasa lo que llega a un Listener. */
public class ServerConnection {

    
    public interface Listener {
        void onUsers(List<String> users);

        void onMessage(String sender, String text);

        void onError(String error);

        void onDisconnected();
    }

    private static final int CONNECT_TIMEOUT_MS = 3000;

    private final Socket socket = new Socket();
    private final Listener listener;
    private PrintWriter out;
    private volatile boolean closedByUser;

    public ServerConnection(Listener listener) {
        this.listener = listener;
    }

    public void connect(String host, int port, String username) throws IOException {
        socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        out.println(Protocol.join(Protocol.LOGIN, username));

        // readLine() bloquea hasta que el servidor envía algo, o cierra la conexión.
        Thread reader = new Thread(() -> listen(in));
        reader.setDaemon(true);
        reader.start();
    }

    public void sendMessage(String recipient, String text) {
        out.println(Protocol.join(Protocol.MSG, recipient, text));
    }

    public void close() {
        closedByUser = true;
        out.println(Protocol.LOGOUT);
        try {
            socket.close();
        } catch (IOException ignored) {
            // Already closed.
        }
    }

    private void listen(BufferedReader in) {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                handle(Protocol.split(line));
            }
        } catch (IOException e) {
            // Sin conexión.
        }
        if (!closedByUser) {
            SwingUtilities.invokeLater(listener::onDisconnected);
        }
    }

    private void handle(String[] parts) {
        switch (parts[0]) {
            case Protocol.USERS -> {
                List<String> users = parts.length < 2 || parts[1].isEmpty()
                        ? List.of()
                        : Arrays.asList(parts[1].split(Protocol.USER_SEPARATOR));
                SwingUtilities.invokeLater(() -> listener.onUsers(users));
            }
            case Protocol.MSG -> {
                if (parts.length == 3) {
                    SwingUtilities.invokeLater(() -> listener.onMessage(parts[1], parts[2]));
                }
            }
            case Protocol.ERROR -> {
                String error = parts.length > 1 ? parts[1] : "Error desconocido";
                SwingUtilities.invokeLater(() -> listener.onError(error));
            }
            default -> {
                // Línea desconocida: ignorar.
            }
        }
    }
}

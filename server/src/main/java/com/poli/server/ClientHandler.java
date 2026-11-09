package com.poli.server;

import com.poli.common.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Se conecta con un cliente. */
public class ClientHandler implements Runnable {

    /** Cada usuario conectado se agrega a este map. */
    private static final Map<String, ClientHandler> CLIENTS = new ConcurrentHashMap<>();

    private final Socket socket;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8),
                     true);

            if (!login(in.readLine())) {
                return;
            }

            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = Protocol.split(line);
                if (parts[0].equals(Protocol.LOGOUT)) {
                    break;
                }
                if (parts[0].equals(Protocol.MSG) && parts.length == 3) {
                    System.out.println("Recibido del client: "+ line);
                    sendPrivateMessage(parts[1], parts[2]);
                } else {
                    send(Protocol.join(Protocol.ERROR, "Mensaje no válido"));
                }
            }
        } catch (IOException e) {
            // Si el cliente cierra la ventana o pierde la conexión.
        } finally {
            if (username != null) {
                CLIENTS.remove(username);
                System.out.println(username + " se desconectó");
                broadcastUsers();
            }
        }
    }

    /** La primera línea debe ser LOGIN|name, con un nombre que nadie más esté usando. */
    private boolean login(String line) {
        if (line == null) {
            return false;
        }
        String[] parts = Protocol.split(line);
        if (!parts[0].equals(Protocol.LOGIN) || parts.length != 2 || !isValidName(parts[1])) {
            send(Protocol.join(Protocol.ERROR, "Nombre de usuario no válido"));
            return false;
        }
        String name = parts[1].trim();
        // putIfAbsent usa la atomicidad: 2 usuarios no pueden tener el mismo nombre.
        if (CLIENTS.putIfAbsent(name, this) != null) {
            send(Protocol.join(Protocol.ERROR, "El usuario " + name + " ya está conectado"));
            return false;
        }
        username = name;
        System.out.println(username + " se conectó");
        broadcastUsers();
        return true;
    }

    private static boolean isValidName(String name) {
        return !name.isBlank() && !name.contains(Protocol.USER_SEPARATOR);
    }

    /** Entrega el mensaje privado al destinatario. No los muestra a los demás usuarios */
    private void sendPrivateMessage(String recipient, String text) {
        ClientHandler target = CLIENTS.get(recipient);
        if (target == null) {
            send(Protocol.join(Protocol.ERROR, "El usuario " + recipient + " no está conectado"));
            return;
        }
        target.send(Protocol.join(Protocol.MSG, username, text));
    }

    /** Muestra la lista de usuarios en línea a todos los usuarios */
    private static void broadcastUsers() {
        for (ClientHandler client : CLIENTS.values()) {
            String others = String.join(Protocol.USER_SEPARATOR,
                    CLIENTS.keySet().stream().filter(name -> !name.equals(client.username)).sorted().toList());
            client.send(Protocol.join(Protocol.USERS, others));
        }
    }

    /** Método Synchronized para que no se sobreescriba el mensaje de los usuarios */
    private synchronized void send(String line) {
        System.out.println("Enviando al cliente: " + line);
        out.println(line);
    }
}

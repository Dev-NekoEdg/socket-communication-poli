package com.poli.server;

import com.poli.common.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerApplication {

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : Config.SERVER_PORT;

        // Crea el socket del servidor y escucha conexiones entrantes.
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor escuchando en el puerto " + port);

            while (true) {
                // El método accept() bloquea hasta que un cliente se conecta.
                Socket socket = serverSocket.accept();

                // Crea un nuevo hilo para manejar la conexión del cliente.
                new Thread(new ClientHandler(socket)).start();
            }
        }
    }
}

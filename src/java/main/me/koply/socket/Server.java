package me.koply.socket;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class Server {

    private final ServerSocket serverSocket;
    public final static Logger log = Logger.getLogger("SERVER LOG: ");

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer() {
        try {
            log.info("Server " + serverSocket.getLocalPort() + " portunda aktif. Yeni bağlantı bekleniyor...");
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                log.info("Yeni kullanıcı giriş yaptı!");

                ClientHandler clientHandler = new ClientHandler(socket);
                log.info("'" + clientHandler.getUsername() + "' kullanıcı adı ile bağlantı kuruldu.");

                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(12345);
        Server server = new Server(serverSocket);
        server.startServer();
    }

}

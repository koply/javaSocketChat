package me.koply.socket;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    private final Socket socket;

    private BufferedReader socketReader;
    private BufferedWriter socketWriter;

    private String username;
    public String getUsername() { return username; }

    public ClientHandler(Socket socket) {
        this.socket = socket;

        try {
            this.socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            this.username = socketReader.readLine();

            clientHandlers.add(this);
            broadcastMessage("SERVER: " + username + " joined!");
        } catch (IOException e) {
            closeHandler();
        }
    }

    public boolean isConnected() {
        return socket.isConnected() && socketWriter != null && socketReader != null;
    }

    public String getState() {
        return isConnected() ? "CONNECTED" : "DISCONNECTED";
    }

    public void closeHandler() {
        clientHandlers.remove(this);

        try {
            broadcastMessage("SERVER: " + username + " has left the chat!");
            if (socketReader != null) {
                socketReader.close();
                socketReader = null;
            }
            if (socketWriter != null) {
                socketWriter.close();
                socketWriter = null;
            }
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    // ------- running state ---------
    // Server commands:
    // #sel'NICK -> selects current client with nick
    // #cur -> current selection
    // #lis -> lists all clients with nicks

    private ClientHandler selectedClient;

    @Override
    public void run() {
        while (socket.isConnected()) {
            try {
                String receivedMessage = socketReader.readLine();

                if (receivedMessage.equals("#lis")) {
                    listClients();
                } else if (receivedMessage.startsWith("#sel")) {
                    String selectedNick = receivedMessage.substring(4);
                    boolean ok = false;
                    for (ClientHandler handler : clientHandlers) {
                        if (handler.getUsername().equals(selectedNick) && handler.isConnected()) {
                            selectedClient = handler;
                            send("Selected client's nick: " + selectedClient.getUsername());
                            ok = true;
                            break;
                        }
                    }
                    if (!ok) {
                        send("Client named as '" + selectedNick + "' is not found.");
                    }
                } else if (receivedMessage.equals("#cur")) {
                    if (selectedClient == null) {
                        send("You don't select any client.");
                    } else {
                        send("Selected client is '" + selectedClient.getUsername() + "'. State: " + selectedClient.getState());
                    }
                } else {
                    broadcastMessage(receivedMessage);

                }
            } catch (IOException e) {
                closeHandler();
                break;
            }
        }
    }

    // sends the message to other clients
    public void broadcastMessage(String messageToSend) throws IOException {
        for (ClientHandler handler : clientHandlers) {
            if (handler.username.equals(username)) {
                continue;
            }

            handler.socketWriter.write(messageToSend);
            handler.socketWriter.newLine();
            handler.socketWriter.flush();
        }
    }

    public void send(String msg) {
        try {
            socketWriter.write(msg);
            socketWriter.newLine();
            socketWriter.flush();
        } catch (Exception ex) {
            Server.log.warning("An error occured while sending a message:");
            ex.printStackTrace();
        }
    }

    public void listClients() {
        StringBuilder sb = new StringBuilder("Connected clients are here: ");
        for (ClientHandler handler : clientHandlers) {
            sb.append(handler.getUsername());
            if (handler.equals(this)) sb.append(" (you)");
            sb.append(" - ");
        }
        send(sb.toString());
    }



}

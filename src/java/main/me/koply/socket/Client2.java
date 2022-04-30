package me.koply.socket;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client2 {

    private Socket socket;
    private BufferedReader socketReader;
    private BufferedWriter socketWriter;
    private String username;

    public Client2(Socket socket, String username) {
        try {
            this.socket = socket;
            this.username = username;
            this.socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Initially send the username of the client.
            send(username);
        } catch (IOException e) {
            // Gracefully close everything.
            closeEverything();
        }
    }

    // Helper method to close everything so you don't have to repeat yourself.
    public void closeEverything() {
        try {
            if (socketReader != null) {
                socketReader.close();
            }
            if (socketWriter != null) {
                socketWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void consoleSender() {
        // Create a scanner for user input.
        // While there is still a connection with the server, continue to scan the terminal and then send the message.
        while (socket.isConnected()) {
            String message = scanner.nextLine();
            try {
                send(username + ": " + message);
            } catch (IOException ex) {
                ex.printStackTrace();
                closeEverything();
            }
        }
    }

    public void send(String msg) throws IOException {
        socketWriter.write(msg);
        socketWriter.newLine();
        socketWriter.flush();
    }

    // Listening for a message is blocking so need a separate thread for that.
    public void listenServer() {
        new Thread(() -> {
            String msgFromGroupChat;
            // While there is still a connection with the server, continue to listen for messages on a separate thread.
            while (socket.isConnected()) {
                try {
                    // Get the messages sent from other users and print it to the console.
                    msgFromGroupChat = socketReader.readLine();
                    System.out.println(msgFromGroupChat);
                } catch (IOException e) {
                    // Close everything gracefully.
                    closeEverything();
                }
            }
        }).start();
    }

    public static final Scanner scanner = new Scanner(System.in);
    // Run the program.
    public static void main(String[] args) throws IOException {

        // Get a username for the user and a socket connection.
        System.out.print("Enter your username for the group chat: ");
        String username = scanner.nextLine();
        // Create a socket to connect to the server.
        Socket socket = new Socket("localhost", 12345);

        // Pass the socket and give the client a username.
        Client2 client = new Client2(socket, username);
        // Infinite loop to read and send messages.
        System.out.println("11");
        client.listenServer();
        System.out.println("123");
        client.consoleSender();
    }
}

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

// A client sends messages to the server, the server spawns a thread to communicate with the client.
// Each communication with a client is added to an array list so any message sent gets sent to every other client
// by looping through it.

public class Client {

    // A client has a socket to connect to the server and a reader and writer to receive and send messages respectively.
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private String contactPersonName;

    public Client(Socket socket, String username,String contactPersonName) {
        try {
            this.socket = socket;
            this.username = username;
            this.contactPersonName = contactPersonName;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter= new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            // İlk olarak kullanıcı adı gönderiliyor
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            bufferedWriter.write(contactPersonName);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            // input için scanner oluşturuldu.
            Scanner scanner = new Scanner(System.in);

            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();
                bufferedWriter.write(username + ": " + messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;

                while (socket.isConnected()) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        // Close everything gracefully.
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {

        //! Kullanıcı adını alma
        Scanner scanner = new Scanner(System.in);
        System.out.print("Tek kullanımlık isim giriniz: ");
        String username = scanner.nextLine();

        System.out.print("Bağlanılacak kişi kullanıcı adı girin:");
        String contactPersonName = scanner.nextLine();

        //! Soket Bağlantımız
        Socket socket = new Socket("localhost", 1234);

        //! Constructor 'a soket ve kullanıcı adı parametrelerini gir.
        Client client = new Client(socket, username,contactPersonName);

        //! Sonsuz döngüde mesaj okuma ve yazma işlemi.
        client.listenForMessage();
        client.sendMessage();
    }
}

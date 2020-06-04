import Model.Message;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Server extends JFrame {

    private JPanel serverPanel;
    private JTextArea txtConnectedClients;
    private JButton btnStopServer;
    private JButton btnStartServer;
    private JTextArea txtServerLog;
    private JButton btnRefreshList;

    static ArrayList<ClientHandler> ar = new ArrayList<>();
    static List<String> connectedClients = new ArrayList<>();
    static int i = 0;

    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                new Server();
            }
        });
    }

    public Server() {

        /*Create a server directory to store files*/
        File file = new File("serverDirectory");
        if (!file.exists()) {

            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {

                System.out.println("Directory already exists!");
            }
        }

        btnStartServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {

                            connectToServer();
                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        btnRefreshList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                printList();
            }
        });

        btnStopServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                /*Terminate Server process*/
                System.exit(0);
            }
        });

        setTitle("Server");
        setContentPane(serverPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void connectToServer() throws IOException {

        ServerSocket serverSocket = new ServerSocket(31313);

        txtServerLog.append("Server is listening on port 313131...\n\n");

        while (true) {

            Socket socket = null;

            try {

                /*Accepting newly connected socket connections*/
                socket = serverSocket.accept();

                ObjectInputStream oips = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream oops = new ObjectOutputStream(socket.getOutputStream());

                Message message = (Message) oips.readObject();
                connectedClients.add(message.getSender());
                printList();

                ClientHandler clientHandler = new ClientHandler(oips, oops, socket, message.getSender());
                Thread thread = new Thread(clientHandler);

                ar.add(clientHandler);
                thread.start();

                i++;
            } catch (Exception e) {

                /*Close the socket if something goes wrong*/
                assert socket != null;
                socket.close();

                /*Print error log to the console*/
                e.printStackTrace();
            }
        }
    }

    public void printList() {

        if(SwingUtilities.isEventDispatchThread()) {

            txtConnectedClients.setText(String.join("\n", connectedClients));

        } else {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {

                    txtConnectedClients.setText(String.join("\n", connectedClients));
                }
            });
        }
    }
}

class ClientHandler implements Runnable {

    private ObjectInputStream oips;
    private ObjectOutputStream oops;
    private Socket socket;
    private String clientName;
    private boolean threadState = true;

    public ClientHandler(ObjectInputStream oips, ObjectOutputStream oops, Socket socket, String clientName) {

        this.oips = oips;
        this.oops = oops;
        this.socket = socket;
        this.clientName = clientName;
    }

    @Override
    public void run() {

        Message message;
        String senderName;

        while(threadState) {

            try {

                message = (Message) oips.readObject();

                senderName = message.getSender();
                byte[] file = message.getFile();
                String absFileName = message.getFileName();

                int index = absFileName.lastIndexOf("\\");
                String fileName = absFileName.substring(index + 1);

                try(FileOutputStream fileOutputStream = new FileOutputStream("serverDirectory/" + fileName)) {

                    fileOutputStream.write(file);

                    for(ClientHandler client: Server.ar) {

                        if(!client.clientName.equals(senderName)) {

                            byte[] outFile = Files.readAllBytes(Paths.get("serverDirectory/" + fileName));
                            Message newMessage = new Message();
                            newMessage.setSender(senderName);
                            newMessage.setFileName(fileName);
                            newMessage.setFile(outFile);

                            client.oops.writeObject(newMessage);
                        }
                    }
                } catch (IOException e) {

                    e.printStackTrace();
                }

            } catch (Exception e) {

                Server.connectedClients.remove(clientName);

                stopThread();

                e.printStackTrace();
            }
        }
    }

    public void stopThread() {

        threadState = false;
    }
}

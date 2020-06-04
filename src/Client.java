import Model.Message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.file.*;

public class Client extends JFrame {

    private JTextArea txtClientLog;
    private JButton btnOpenDir;
    private JButton btnDisconnect;
    private JLabel lblWelcome;
    private JPanel clientPanel;

    private String clientName;

    private Socket socket;
    private ObjectInputStream oips;
    private ObjectOutputStream oops;

    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {

                new Client();
            }
        });
    }

    public Client() {

        JFrame frame = new JFrame("Input username");
        clientName = JOptionPane.showInputDialog(frame, "Enter a username");

        if (clientName.isEmpty()) {

            JOptionPane.showMessageDialog(null, "Please enter a username to connect", "No username!", JOptionPane.WARNING_MESSAGE);

        } else {

            File file = new File(clientName.trim());

            if (!file.exists()) {

                if (file.mkdir()) {

                    System.out.println("Root client directory created...");

                    File sentDirectory = new File(clientName.trim() + "/sentDirectory");
                    File receivedDirectory = new File(clientName.trim() + "/receivedDirectory");

                    if (!sentDirectory.exists() && !receivedDirectory.exists()) {

                        if (sentDirectory.mkdir() && receivedDirectory.mkdir()) {

                            System.out.println("Sub-directories created...");

                            connectToServer(clientName);
                            lblWelcome.setText("Welcome, " + clientName);
                            watchDirectory();
                        }
                    }
                }
            } else {

                System.out.println("Root directory exists...");
            }
        }

        btnOpenDir.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {

                    Desktop.getDesktop().open(new File(clientName.trim() + "/receivedDirectory"));

                } catch (IOException ioException) {

                    ioException.printStackTrace();
                }
            }
        });

        btnDisconnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                System.exit(0);
            }
        });

        setTitle("Client");
        setContentPane(clientPanel);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /*Establishing connection to the server*/
    private void connectToServer(String clientName) {

        try {

            socket = new Socket("localhost", 31313);

            oops = new ObjectOutputStream(socket.getOutputStream());

            Message message = new Message();

            message.setSender(clientName.trim());
            oops.writeObject(message);
            oops.flush();

        } catch (IOException ioException) {

            ioException.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error in connection, check your connection!", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
    }

    private void watchDirectory() {

        /*Watching the file sending directory for changes. File is pushed to the server if any change is observed*/
        Thread scanFiles = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    WatchService watchService = FileSystems.getDefault().newWatchService();
                    Path path = Paths.get(clientName.trim() + "/sentDirectory");
                    path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

                    WatchKey key;
                    while ((key = watchService.take()) != null) {

                        for (WatchEvent<?> event : key.pollEvents()) {

                            byte b[] = Files.readAllBytes(Paths.get(clientName.trim() + "/sentDirectory/" + event.context().toString()));

                            Message message = new Message();
                            message.setSender(clientName.trim());
                            message.setFileName(event.context().toString());
                            message.setFile(b);

                            oops.writeObject(message);
                            oops.flush();

                            txtClientLog.append("File " + event.context().toString() + " has been sent to the server...\n");
                        }

                        key.reset();
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        });

        /*Checking if any new files are coming from the server*/
        Thread receiveFiles = new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    oips = new ObjectInputStream(socket.getInputStream());

                } catch (IOException e) {

                    e.printStackTrace();
                }

                while(true) {

                    try {

                        Message message = (Message) oips.readObject();

                        String fileName = message.getFileName();
                        String senderName = message.getSender();

                        byte[] file = message.getFile();

                        if(!checkIfThere(fileName)) {

                            txtClientLog.append("A new file " + fileName + " from " + senderName + " is received...\n");
                            try (FileOutputStream fileOutputStream = new FileOutputStream(clientName.trim() + "/receivedDirectory/" + fileName)) {

                                fileOutputStream.write(file);
                            } catch (Exception e) {

                                e.printStackTrace();
                            }
                        } else {

                            if(showInvalidationNotice(fileName, senderName)) {

                                txtClientLog.append("An update to file " + fileName + " from " + senderName + " has been received...\n");
                                try (FileOutputStream fileOutputStream = new FileOutputStream(clientName.trim() + "/receivedDirectory/" + fileName)) {

                                    /*Writing the file into the directory*/
                                    fileOutputStream.write(file);
                                }
                                catch (IOException e) {

                                    /*Logging exceptions*/
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception e) {

                        e.printStackTrace();
                    }
                }
            }
        });

        scanFiles.start();
        receiveFiles.start();
    }

    /*Show an invalidation notice, to notify the user that an update to the file is available*/
    private boolean showInvalidationNotice(String fileName, String senderName) {

        int dialogButton = JOptionPane.YES_NO_OPTION;
        int dialogResult = JOptionPane.showConfirmDialog(this, "An update to the file " + fileName + " from " + senderName + " is available, would you like to download it?", "Update available!", dialogButton);
        if(dialogResult == 0) {

            return true;

        } else {

            return false;
        }
    }

    /*Check if given file exists in the directory*/
    private boolean checkIfThere(String fileName) {

        File folder = new File(clientName.trim() + "/receivedDirectory/");
        File[] listOfFiles = folder.listFiles();

        boolean result = false;

        for (File file : listOfFiles) {

            if (file.getName().equals(fileName)) {

                result = true;
            }
        }

        return result;
    }
}

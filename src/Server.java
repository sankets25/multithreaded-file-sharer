import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Server extends JFrame {

    private JPanel serverPanel;
    private JTextArea txtServerLog;
    private JTextArea txtConnectedClients;
    private JButton btnStopServer;
    private JButton btnStartServer;

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

    public static void main(String[] args) {

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Server();
            }
        });
    }
}

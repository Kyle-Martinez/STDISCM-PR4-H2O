import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class OxygenClient {
    private static int nO;
    private static ArrayList<String> oxygenClientLogs = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String serverIP = "localhost";
    private static final int serverPort = 12345;
    private static Socket socket;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of oxygen atoms:");
        nO = scanner.nextInt();
        scanner.close();

        try {
            socket = new Socket(serverIP, serverPort);
            sendIdentification();
            bondingListener();
            createRequests();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendIdentification() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Oxygen");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void bondingListener() {
        new Thread(() -> {
            try {
                DataInputStream in = new DataInputStream(socket.getInputStream());

                while (true) {
                    String id = in.readUTF();
                    String log = id + ", bonded, " + sdf.format(new Date());
                    oxygenClientLogs.add(log);
                    System.out.println(log);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void createRequests() {
        for (int i = 0; i < nO; i++) {
            String id = "O" + i;
            String log = id + ", request, " + sdf.format(new Date());
            oxygenClientLogs.add(log);
            System.out.println(log);
            sendRequest(id);
        }
    }

    private static void sendRequest(String id) {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(id);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class HydrogenClient {
    private static int nH;
    private static ArrayList<String> hydrogenClientLogs = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
    private static final String serverIP = "localhost";
    private static final int serverPort = 12345;
    private static Socket socket;
    private static int requestCount = 0;
    private static int bondCount = 0;
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of hydrogen atoms:");
        nH = scanner.nextInt();
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

    private static void logsToFile() throws IOException{
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter("HydrogenClientLogs.txt"));
        for (int i = 0; i < hydrogenClientLogs.size(); i++) {
            outputWriter.write(hydrogenClientLogs.get(i));
            outputWriter.newLine();
        }
        outputWriter.flush();  
        outputWriter.close();
    }

    private static void sendIdentification() {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Hydrogen");
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
                    synchronized (hydrogenClientLogs) {
                        hydrogenClientLogs.add(log);
                        System.out.println(log);
                        bondCount++;
                        if (bondCount == nH){
                            try {
                                Thread.sleep(1000); // wait for 1 second
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("Log size: " + hydrogenClientLogs.size() + " - Closing socket.)");
                try {
                    socket.close();
                    logsToFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void createRequests() {
        for (int i = 0; i < nH; i++) {
            String id = "H" + i;
            String log = id + ", request, " + sdf.format(new Date());
            synchronized (hydrogenClientLogs) {
                hydrogenClientLogs.add(log);
            }
            // hydrogenClientLogs.add(log);
            System.out.println(log);
            sendRequest(id);
            requestCount++;
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

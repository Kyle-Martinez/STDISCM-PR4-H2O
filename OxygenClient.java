import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class OxygenClient {
    private static int nO;
    private static ArrayList<String> oxygenClientLogs = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
    private static final String serverIP = "localhost";
    private static final int serverPort = 12345;
    private static Socket socket;
    private static int requestCount = 0;
    private static int bondCount = 0;
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
            out.writeInt(nO);
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
                    synchronized (oxygenClientLogs) {
                        oxygenClientLogs.add(log);
                        System.out.println(log);
                        bondCount++;
                        if (bondCount == nO) {
                            try {
                                Thread.sleep(1000); 
                                calculateAndPrintTimeDifference(); 
                                logsToFile(); 
                            } catch (InterruptedException | IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("Log size: " + oxygenClientLogs.size() + " - Closing socket.)");
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
        for (int i = 0; i < nO; i++) {
            String id = "O" + i;
            String log = id + ", request, " + sdf.format(new Date());
            synchronized (oxygenClientLogs) {
                oxygenClientLogs.add(log);
            }
            // oxygenClientLogs.add(log);
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

    private static void logsToFile() throws IOException{
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter("OxygenClientLogs.txt"));
        for (int i = 0; i < oxygenClientLogs.size(); i++) {
            outputWriter.write(oxygenClientLogs.get(i));
            outputWriter.newLine();
        }
        outputWriter.flush();  
        outputWriter.close();
    }

    private static void calculateAndPrintTimeDifference() throws ParseException {
        if (oxygenClientLogs.isEmpty()) return;

        String firstLog = oxygenClientLogs.stream()
                .filter(log -> log.contains(", request, "))
                .findFirst()
                .orElse(null);

        String lastLog = oxygenClientLogs.stream()
                .filter(log -> log.contains(", bonded, "))
                .reduce((first, second) -> second)
                .orElse(null);

        if (firstLog == null || lastLog == null) return;

        Date firstTimestamp = sdf.parse(firstLog.split(", ")[2]);
        Date lastTimestamp = sdf.parse(lastLog.split(", ")[2]);

        long difference = lastTimestamp.getTime() - firstTimestamp.getTime();

        System.out.println("Time difference: " + difference + " ms");
    }
}

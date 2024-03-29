import java.net.*;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;

public class Server {
    private static ArrayList<String> serverLogs = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final int PORT = 12345;
    private static final int BOND_THRESHOLD = 2;
    private static Map<String, Socket> clients = new HashMap<>();
    private static List<String> hydrogenQueue = new ArrayList<>();
    private static List<String> oxygenQueue = new ArrayList<>();
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Thread bondWaterThread = new Thread(Server::bondWater);
            bondWaterThread.start();
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                String clientName = in.readUTF();
                clients.put(clientName, clientSocket);
                System.out.println("Connected to client: " + clientName);
    
                if (clientName.equals("Hydrogen")) {
                    Thread hydrogenThread = new Thread(() -> receiveRequests(clients.get("Hydrogen"), "Hydrogen"));
                    hydrogenThread.start();
                } else if (clientName.equals("Oxygen")) {
                    Thread oxygenThread = new Thread(() -> receiveRequests(clients.get("Oxygen"), "Oxygen"));
                    oxygenThread.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveRequests(Socket client, String element) {
        try {
            DataInputStream in = new DataInputStream(client.getInputStream());
            while (true) {
                String id = in.readUTF();
                String log = id + ", request, " + sdf.format(new Date());
                serverLogs.add(log);
                if (element.equals("Hydrogen")) {
                    synchronized (hydrogenQueue) {
                        hydrogenQueue.add(id);
                    }
                } else {
                    synchronized (oxygenQueue) {
                        oxygenQueue.add(id);
                    }
                }
                System.out.println(log);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void bondWater(){
        while (true) {
            boolean isBondAvailble;
            synchronized (hydrogenQueue) {
                synchronized (oxygenQueue) {
                    isBondAvailble = hydrogenQueue.size() >= BOND_THRESHOLD && oxygenQueue.size() >= 1;
                    if (isBondAvailble){
                        System.out.println("Hydrogen queue: " + hydrogenQueue.size());
                        System.out.println("Oxygen queue: " + oxygenQueue.size());
                    }
                }
            }
            if (isBondAvailble) {
                synchronized (hydrogenQueue) {
                    synchronized (oxygenQueue) {
                        String h1 = hydrogenQueue.remove(0);
                        sendMessage(clients.get("Hydrogen"), h1);
                        serverLogs.add(h1 + ", bonded, " + sdf.format(new Date()));
                        String h2 = hydrogenQueue.remove(0);
                        sendMessage(clients.get("Hydrogen"), h2);
                        serverLogs.add(h2 + ", bonded, " + sdf.format(new Date()));
                        String o = oxygenQueue.remove(0);
                        sendMessage(clients.get("Oxygen"), o);
                        serverLogs.add(o + ", bonded, " + sdf.format(new Date()));
                    }
                }
            }
        }
    }
    private static void sendMessage(Socket client, String message) {
        try {
            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

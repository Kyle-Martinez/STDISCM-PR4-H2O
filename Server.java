import java.net.*;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;

public class Server {
    private static ArrayList<String> serverLogs = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
    private static final int PORT = 12345;
    private static final int BOND_THRESHOLD = 2;
    private static Map<String, Socket> clients = new HashMap<>();
    private static List<String> hydrogenQueue = new ArrayList<>();
    private static List<String> oxygenQueue = new ArrayList<>();
    private static int nH = 0;
    private static int nO = 0;
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            Thread bondWaterThread = new Thread(Server::bondWater);
            bondWaterThread.start();
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                DataInputStream in = new DataInputStream(clientSocket.getInputStream());
                String clientName = in.readUTF();
                int nElement = in.readInt();
                clients.put(clientName, clientSocket);
                System.out.println("Connected to client: " + clientName);
    
                if (clientName.equals("Hydrogen")) {
                    Thread hydrogenThread = new Thread(() -> receiveRequests(clients.get("Hydrogen"), "Hydrogen"));
                    nH = nElement;
                    hydrogenThread.start();
                } else if (clientName.equals("Oxygen")) {
                    Thread oxygenThread = new Thread(() -> receiveRequests(clients.get("Oxygen"), "Oxygen"));
                    nO = nElement;
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
                synchronized (serverLogs) {
                    serverLogs.add(log);
                }
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
        }  catch (EOFException e) {
            System.out.println(element + " Client disconnected");
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
                }
            }
            if (isBondAvailble) {
                synchronized (hydrogenQueue) {
                    synchronized (oxygenQueue) {
                        synchronized (serverLogs) {
                            String h1 = hydrogenQueue.remove(0);
                            sendMessage(clients.get("Hydrogen"), h1);
                            String log1 = h1 + ", bonded, " + sdf.format(new Date());
                            serverLogs.add(log1);
                            System.out.println(log1);
                            String h2 = hydrogenQueue.remove(0);
                            sendMessage(clients.get("Hydrogen"), h2);
                            String log2 = h2 + ", bonded, " + sdf.format(new Date());
                            serverLogs.add(log2);
                            System.out.println(log2);
                            String o = oxygenQueue.remove(0);
                            sendMessage(clients.get("Oxygen"), o);
                            String log3 = o + ", bonded, " + sdf.format(new Date());
                            serverLogs.add(log3);
                            System.out.println(log3);
                            if (serverLogs.size() == (nH * 2) + (nO * 2)) {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    logsToFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void logsToFile() throws IOException{
        BufferedWriter outputWriter = null;
        outputWriter = new BufferedWriter(new FileWriter("ServerLogs.txt"));
        for (int i = 0; i < serverLogs.size(); i++) {
            outputWriter.write(serverLogs.get(i));
            outputWriter.newLine();
        }
        outputWriter.flush();  
        outputWriter.close();
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

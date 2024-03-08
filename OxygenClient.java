import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class OxygenClient {
    private static int nO;
    private static ArrayList<String> OxygenClientLogs = new ArrayList<>();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of oxygen atoms:");
        nO = scanner.nextInt();
        scanner.close();

        createRequests();
    }

    private static void createRequests() {
        for (int i = 0; i < nO; i++) {
            String id = "O" + i;
            String log = id + ", request, " + sdf.format(new Date());
            System.out.println();
            OxygenClientLogs.add(log);
            sendRequest(id);
        }
    }

    private static void sendRequest(String id) {
        // TODO: Add code to send request to server
    }
}

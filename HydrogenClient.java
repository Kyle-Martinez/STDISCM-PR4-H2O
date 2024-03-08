import java.util.Scanner;

public class HydrogenClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of hydrogen atoms:");
        int nH = scanner.nextInt();
        scanner.close();
    }
}

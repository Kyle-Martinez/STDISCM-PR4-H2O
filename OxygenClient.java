import java.util.Scanner;

public class OxygenClient {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the number of oxygen atoms:");
        int nO = scanner.nextInt();
        scanner.close();
    }
}

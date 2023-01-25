import java.util.Scanner;
public class CountRepeatedDigits {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter an integer:");
        int input = sc.nextInt();

        if (input < 0) {
            System.out.println("It's a negative integer");
        }
        else if (input == 0) {
            System.out.println("It's a zero");
        }
        else {
            int[] counts = new int[10];
            for (int i = input; i > 0; i /= 10) {
                int digit = i % 10;
                counts[digit]++;
            }
            System.out.println("Digit - Count");
            for (int i = 0; i < 10; i++) {
                if (counts[i] > 1) {
                    System.out.println(i + " - " + counts[i]);
                }

            }
        }
    }
}
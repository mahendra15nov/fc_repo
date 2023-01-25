import java.util.Scanner;

public class Praneeth {

    public static void palindrome(String str){
        String reverse = "";
        int length = str.length();
        for (int i = length - 1; i >= 0; i--) {
            reverse = reverse + str.charAt(i);
        }
        if (str.equals(reverse)) {
            System.out.println(str + " is a palindrome.");
        }
        else {
            System.out.println(str + " is not a palindrome.");
        }
    }
    public static void count(int input){
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

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String name = sc.next();
        int number = sc.nextInt();
        palindrome(name);
        count(number);
    }
}

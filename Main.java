import java.util.Scanner;
import java.util.InputMismatchException;

/**
 * Main Class - Entry Point for Theme Park Resort Management System
 * OOP Concepts Demonstrated:
 * - Abstraction: PaymentFramework abstract class with template method
 * - Inheritance: CreditCardPayment and OnlinePayment extend PaymentFramework
 * - Encapsulation: Protected fields, controlled access
 * - Polymorphism: processInvoice() behaves differently per subclass
 */
public class Main {

    private static final String VERSION = "4.0.0 (SQLite)";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Ensure database is initialized on startup
        Repository.getInstance();
        displayWelcomeBanner();

        int choice;
        boolean running = true;

        do {
            displayMainMenu();
            choice = getValidIntInput("Enter your choice: ", 0, 9);

            switch (choice) {
                case 1:
                    handleRegistration();
                    break;

                case 2:
                    visitorLogIn.login(scanner);
                    break;

                case 9:
                    adminSystem.showAdminLogin(scanner);
                    break;

                case 0:
                    System.out.println("\n+==============================================================+");
                    System.out.println("|  Thank you for visiting!                                     |");
                    System.out.println("|  Have a wonderful day!                                       |");
                    System.out.println("+==============================================================+");
                    running = false;
                    break;

                default:
                    System.out.println("\n[!] Invalid choice. Please try again.");
                    pauseScreen();
            }

        } while (running);

        scanner.close();
    }

    private static void displayWelcomeBanner() {
        System.out.println("+==============================================================+");
        System.out.println("|                                                              |");
        System.out.println("|           THEME PARK RESORT MANAGEMENT SYSTEM                |");
        System.out.println("|                                                              |");
        System.out.println("|              Your Gateway to Magical Moments!                |");
        System.out.println("|                                                              |");
        System.out.println("|                                                              |");
        System.out.println("|                                                              |");
        System.out.println("|                                                              |");
        System.out.println("+==============================================================+");
        System.out.println();
    }

    private static void displayMainMenu() {
        System.out.println("\n+--------------------------------------------------------------+");
        System.out.println("|         MAIN MENU                                            |");
        System.out.println("+--------------------------------------------------------------+");
        System.out.println("|  [1] Sign Up (New Customer)                                  |");
        System.out.println("|  [2] Log In (Returning Customer)                             |");
        System.out.println("|  [9] Staff Login                                             |");
        System.out.println("|  [0] Exit                                                    |");
        System.out.println("+--------------------------------------------------------------+");
    }

    private static void handleRegistration() {
        int registrationChoice;
        boolean inRegistrationMenu = true;

        do {
            System.out.println("\n+--------------------------------------------------------------+");
            System.out.println("|     NEW CUSTOMER REGISTRATION                                |");
            System.out.println("+--------------------------------------------------------------+");
            System.out.println("|  [1] Register New Account                                    |");
            System.out.println("|  [2] Read Age Verification Info                              |");
            System.out.println("|  [0] Back to Main Menu                                       |");
            System.out.println("+--------------------------------------------------------------+");

            registrationChoice = getValidIntInput("Enter your choice: ", 0, 2);

            switch (registrationChoice) {
                case 1:
                    handleAgeVerification();
                    break;

                case 2:
                    visitorRegistration.readAgeVerification(scanner);
                    break;

                case 0:
                    System.out.println("\nReturning to main menu...");
                    inRegistrationMenu = false;
                    break;

                default:
                    System.out.println("\n[!] Invalid choice. Please try again.");
            }

        } while (inRegistrationMenu);
    }

    private static void handleAgeVerification() {
        int age = getValidIntInput("\nPlease enter your age: ", 0, 120);

        if (age >= 16) {
            visitorRegistration vr = new visitorRegistration();
            vr.register(age, scanner);
        } else {
            System.out.println("\n+==============================================================+");
            System.out.println("|  * AGE RESTRICTION NOTICE                                    |");
            System.out.println("|                                                              |");
            System.out.println("|  Sorry, customers under 16 years old cannot register         |");
            System.out.println("|  independently. Please register with a parent or guardian.   |");
            System.out.println("|                                                              |");
            System.out.println("+==============================================================+");
            pauseScreen();
        }
    }

    public static int getValidIntInput(String prompt, int min, int max) {
        int input = -1;
        boolean valid = false;

        while (!valid) {
            System.out.print(prompt);
            try {
                input = scanner.nextInt();
                scanner.nextLine();

                if (input >= min && input <= max) {
                    valid = true;
                } else {
                    System.out.println("[!] Please enter a number between " + min + " and " + max + ".");
                }
            } catch (InputMismatchException e) {
                System.out.println("[!] Invalid input. Please enter a valid number.");
                scanner.nextLine();
            }
        }
        return input;
    }

    public static String getValidStringInput(String prompt) {
        String input = "";

        while (input.trim().isEmpty()) {
            System.out.print(prompt);
            input = scanner.nextLine();

            if (input.trim().isEmpty()) {
                System.out.println("[!] This field cannot be empty. Please try again.");
            }
        }
        return input.trim();
    }

    public static void pauseScreen() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    public static Scanner getScanner() {
        return scanner;
    }
}

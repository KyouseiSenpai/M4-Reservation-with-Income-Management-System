import java.util.Scanner;

 // Visitor Login Class - Handles customer login and ID recovery

public class visitorLogIn {

    private static final Repository repo = Repository.getInstance();
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int MIN_CUSTOMER_ID = 10000;
    private static final int MAX_CUSTOMER_ID = 99999;

    public static void login(Scanner sc) {
        int loginChoice;
        boolean inLoginMenu = true;

        do {
            displayLoginMenu();
            loginChoice = getValidIntInput(sc, "Enter your choice: ", 0, 2);

            switch (loginChoice) {
                case 1:
                    handleCustomerLogin(sc);
                    break;

                case 2:
                    handleForgotCustomerID(sc);
                    break;

                case 0:
                    System.out.println("\nReturning to main menu...");
                    inLoginMenu = false;
                    break;

                default:
                    System.out.println("\n[!] Invalid choice. Please try again.");
            }

        } while (inLoginMenu);
    }

// Display Login Menu
    private static void displayLoginMenu() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                        CUSTOMER LOGIN                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                              ║");
        System.out.println("║  [1] Login with Customer ID                                  ║");
        System.out.println("║  [2] I forgot my Customer ID                                 ║");
        System.out.println("║  [0] Back to Main Menu                                       ║");
        System.out.println("║                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

// Handle customer login with ID
    private static void handleCustomerLogin(Scanner sc) {
        int attempts = 0;
        boolean loggedIn = false;

        while (attempts < MAX_LOGIN_ATTEMPTS && !loggedIn) {
            System.out.println("\n─────────────────────────────────");
            System.out.println("Login Attempt " + (attempts + 1) + " of " + MAX_LOGIN_ATTEMPTS);
            System.out.println("─────────────────────────────────");
            
            int loginID = getValidIntInput(sc, "\nPlease enter your Customer ID: ", MIN_CUSTOMER_ID, MAX_CUSTOMER_ID);

            System.out.println("\nVerifying your credentials...");
            
            int result = repo.findCustomerByID(loginID);

            if (result != -1) {
                System.out.println("\n Login Successful!");
                System.out.println("Welcome back to Theme Park Resort!");
                loggedIn = true;
                customerPortal.portalMenu(result, sc);
            } else {
                attempts++;
                System.out.println("\nCustomer ID not found.");
                
                if (attempts < MAX_LOGIN_ATTEMPTS) {
                    System.out.println("Please try again or select 'I forgot my Customer ID' from the menu.");
                } else {
                    System.out.println("\nMaximum login attempts reached.");
                    System.out.println("Please use the 'Forgot Customer ID' option or contact support.");
                }
            }
        }
    }

//Handle forgot customer ID recovery
    private static void handleForgotCustomerID(Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    CUSTOMER ID RECOVERY                      ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  Please provide the following information to retrieve        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        String forgotFullName = getValidStringInput(sc, "\nEnter your Full Name: ");
        String forgotContactNumber = getValidStringInput(sc, "Enter your registered Contact Number: ");

        System.out.println("\nSearching customer records...");
        
        // Add small delay for realism
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int foundID = repo.findCustomerByDetails(forgotFullName, forgotContactNumber);

        if (foundID != -1) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                     CUSTOMER RECORD FOUND!                   ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║                                                              ║");
            System.out.println("║  Your Customer ID is: " + padRight(String.valueOf(foundID), 35) + "║");
            System.out.println("║                                                              ║");
            System.out.println("║     Please keep your Customer ID for future transactions.    ║");
            System.out.println("║                                                              ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            handlePostRecoveryMenu(sc);
        } else {
            System.out.println("\nCustomer Record Not Found");
            System.out.println("\nThe information provided does not match any records in our system.");
            System.out.println("Please check your information and try again.");
            
            handleRecoveryFailureMenu(sc);
        }
    }


//Handle menu after successful ID recovery
    private static void handlePostRecoveryMenu(Scanner sc) {
        boolean inMenu = true;
        
        while (inMenu) {
            System.out.println("\n┌─────────────────────────────────┐");
            System.out.println("│  What would you like to do?     │");
            System.out.println("├─────────────────────────────────┤");
            System.out.println("│  [1] Proceed to Log In          │");
            System.out.println("│  [0] Back to Main Menu          │");
            System.out.println("└─────────────────────────────────┘");
            
            int choice = getValidIntInput(sc, "Enter your choice: ", 0, 1);

            switch (choice) {
                case 1:
                    handleCustomerLogin(sc);
                    inMenu = false;
                    break;
                case 0:
                    System.out.println("\nReturning to main menu...");
                    inMenu = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid choice.");
            }
        }
    }

// Handle menu after failed ID recovery
    private static void handleRecoveryFailureMenu(Scanner sc) {
        boolean inMenu = true;
        
        while (inMenu) {
            System.out.println("\n┌─────────────────────────────────┐");
            System.out.println("│  What would you like to do?     │");
            System.out.println("├─────────────────────────────────┤");
            System.out.println("│  [1] Try again                  │");
            System.out.println("│  [2] Register as a new customer │");
            System.out.println("│  [0] Back to Main Menu          │");
            System.out.println("└─────────────────────────────────┘");
            
            int forgotChoice = getValidIntInput(sc, "Enter your choice: ", 0, 2);

            switch (forgotChoice) {
                case 1:
                    handleForgotCustomerID(sc);
                    inMenu = false;
                    break;

                case 2:
                    System.out.println("\nRedirecting to registration...");
                    visitorRegistration vr = new visitorRegistration();
                    // Get age first for proper validation
                    int age = getValidIntInput(sc, "Please enter your age: ", 0, 120);
                    if (age >= 18) {
                        vr.register(age, sc);
                    } else {
                        System.out.println("\n[!] You must be at least 18 years old to register independently.");
                    }
                    inMenu = false;
                    break;

                case 0:
                    System.out.println("\nReturning to main menu...");
                    inMenu = false;
                    break;

                default:
                    System.out.println("\n[!] Invalid choice.");
            }
        }
    }

// Get valid integer input within range
    private static int getValidIntInput(Scanner sc, String prompt, int min, int max) {
        int input = -1;
        boolean valid = false;
        
        while (!valid) {
            System.out.print(prompt);
            try {
                input = sc.nextInt();
                sc.nextLine(); // Consume newline
                
                if (input >= min && input <= max) {
                    valid = true;
                } else {
                    System.out.println("[!] Please enter a number between " + min + " and " + max + ".");
                }
            } catch (Exception e) {
                System.out.println("[!] Invalid input. Please enter a valid number.");
                sc.nextLine(); // Clear invalid input
            }
        }
        
        return input;
    }

// Get valid string input (non-empty)
    private static String getValidStringInput(Scanner sc, String prompt) {
        String input = "";
        
        while (input.trim().isEmpty()) {
            System.out.print(prompt);
            input = sc.nextLine();
            
            if (input.trim().isEmpty()) {
                System.out.println("[!] This field cannot be empty. Please try again.");
            }
        }
        
        return input.trim();
    }
    
// Pad string to the right
    private static String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() > n) s = s.substring(0, n - 3) + "...";
        return String.format("%-" + n + "s", s);
    }
}

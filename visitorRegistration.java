import java.time.LocalDate;
import java.util.Scanner;

// Visitor Registration Class - Handles new customer registration
public class visitorRegistration {

    private static final Repository repo = Repository.getInstance();
    private static final int MIN_REGISTRATION_AGE = 18;
    private static final int MAX_REGISTRATION_AGE = 120;

// Register a new customer
    public void register(int age, Scanner sc) {
        try {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                   NEW CUSTOMER REGISTRATION                  ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            System.out.println("\nPlease fill in the following information:\n");

            // Get customer details with validation
            String fullName = getValidName(sc);
            String contactNumber = getValidContactNumber(sc);
            
            // Confirm details
            System.out.println("\n┌─────────────────────────────────┐");
            System.out.println("│      CONFIRM YOUR DETAILS       │");
            System.out.println("├─────────────────────────────────┤");
            System.out.println("│  Name:    " + padRight(fullName, 25) + "│");
            System.out.println("│  Contact: " + padRight(contactNumber, 25) + "│");
            System.out.println("│  Age:     " + padRight(String.valueOf(age), 25) + "│");
            System.out.println("└─────────────────────────────────┘");
            
            System.out.print("\nAre these details correct? [1] Yes [2] No [0] Cancel: ");
            int confirm = getValidIntInput(sc, "", 0, 2);
            
            if (confirm == 0) {
                System.out.println("\nRegistration cancelled. Returning to menu...");
                return;
            }
            if (confirm != 1) {
                System.out.println("\nRegistration cancelled. Please try again.");
                return;
            }

            // Generate customer ID and save
            int newID = repo.generateCustomerID();
            boolean saved = repo.saveCustomer(newID, fullName, contactNumber, age);
            
            if (!saved) {
                System.out.println("\nRegistration failed. Please try again later.");
                return;
            }

            // Display success message
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                  REGISTRATION SUCCESSFUL!                    ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║                                                              ║");
            System.out.println("║  Your Customer ID is: " + padRight(String.valueOf(newID), 35) + "║");
            System.out.println("║                                                              ║");
            System.out.println("║        IMPORTANT: Please remember your Customer ID!          ║");
            System.out.println("║         You'll need it for all future transactions.          ║");
            System.out.println("║                                                              ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");

            // Offer membership upgrade
            membership.membershipUpgrade(newID, sc);

            // Post-registration menu
            handlePostRegistrationMenu(newID, sc);

        } catch (Exception e) {
            System.out.println("\nAn error occurred during registration: " + e.getMessage());
            System.out.println("Please try again or contact support.");
        }
    }

// Read age verification information
    public static void readAgeVerification(Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    AGE VERIFICATION POLICY                   ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                              ║");
        System.out.println("║  The system checks the age of customers during registration. ║");
        System.out.println("║                                                              ║");
        System.out.println("║  • Customers 18 years and older:                             ║");
        System.out.println("║    Can register independently                                ║");
        System.out.println("║                                                              ║");
        System.out.println("║  • Customers under 18 years old:                             ║");
        System.out.println("║    Must be accompanied by a parent or guardian               ║");
        System.out.println("║    for registration                                          ║");
        System.out.println("║                                                              ║");
        System.out.println("║  This policy ensures the safety and compliance of all        ║");
        System.out.println("║  visitors to our Theme Park Resort.                          ║");
        System.out.println("║                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        System.out.println("\n[1] Back to Registration Menu");
        System.out.println("[0] Back to Main Menu");
        
        int choice = getValidIntInput(sc, "Enter your choice: ", 0, 1);
        
        if (choice == 0) {
            System.out.println("\nReturning to main menu...");
        }
        // If choice is 1, just return to the calling menu
    }

// Handle post-registration menu options
    private void handlePostRegistrationMenu(int customerID, Scanner sc) {
        int regChoice;
        boolean inMenu = true;
        
        do {
            System.out.println("\n┌─────────────────────────────────┐");
            System.out.println("│      REGISTRATION COMPLETE!     │");
            System.out.println("├─────────────────────────────────┤");
            System.out.println("│    What would you like to do?   │");
            System.out.println("│                                 │");
            System.out.println("│      [1] Proceed to Log In      │");
            System.out.println("│      [0] Exit to Main Menu      │");
            System.out.println("└─────────────────────────────────┘");
            
            regChoice = getValidIntInput(sc, "\nEnter your choice: ", 0, 1);

            switch (regChoice) {
                case 1:
                    System.out.println("\nProceeding to Log In Menu...");
                    visitorLogIn.login(sc);
                    inMenu = false;
                    break;

                case 0:
                    System.out.println("\nReturning to main menu...");
                    inMenu = false;
                    break;

                default:
                    System.out.println("\n[!] Invalid choice. Please try again.");
            }
        } while (inMenu);
    }

// Get valid name input
    private String getValidName(Scanner sc) {
        String name;
        boolean valid = false;
        
        do {
            System.out.print("Enter your full name: ");
            name = sc.nextLine().trim();
            
            if (name.isEmpty()) {
                System.out.println("[!] Name cannot be empty. Please try again.");
            } else if (name.length() < 2) {
                System.out.println("[!] Name must be at least 2 characters long.");
            } else if (!name.matches("^[a-zA-Z\\s'-]+$")) {
                System.out.println("[!] Name can only contain letters, spaces, hyphens, and apostrophes.");
            } else {
                valid = true;
            }
        } while (!valid);
        
        return name;
    }


// Get valid contact number input
    private String getValidContactNumber(Scanner sc) {
        String contact;
        boolean valid = false;
        
        do {
            System.out.print("Enter your contact number: ");
            contact = sc.nextLine().trim();
            
            // Remove common separators
            String cleanedContact = contact.replaceAll("[\\s\\-\\(\\)]", "");
            
            if (contact.isEmpty()) {
                System.out.println("[!] Contact number cannot be empty.");
            } else if (!cleanedContact.matches("^\\+?[0-9]{7,15}$")) {
                System.out.println("[!] Please enter a valid contact number (7-15 digits).");
            } else {
                valid = true;
            }
        } while (!valid);
        
        return contact;
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
    
// Pad string to the right with spaces
    private String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() > n) s = s.substring(0, n - 3) + "...";
        return String.format("%-" + n + "s", s);
    }
}

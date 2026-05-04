/**
 * Appointment System Class - Handles appointment scheduling
 */

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class appointmentSystem {
    private static final Repository repo = Repository.getInstance();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Pick an appointment date
     * @param sc Scanner instance
     * @return Selected date string or null if cancelled
     */
    public static String pickDate(Scanner sc) {
        String[] dates = new String[6];
        String[] dayNames = new String[6];
        int found = 0;
        LocalDate day = LocalDate.now().plusDays(1);

        // Find next 6 weekdays (excluding Sundays)
        while (found < 6) {
            if (day.getDayOfWeek() != DayOfWeek.SUNDAY) {
                dates[found] = day.format(DATE_FORMATTER);
                dayNames[found] = day.getDayOfWeek().toString();
                found++;
            }
            day = day.plusDays(1);
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              📅 SELECT APPOINTMENT DATE                      ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        for (int i = 0; i < 6; i++) {
            System.out.printf("║  [%d] %s (%s)%n", i + 1, dates[i], dayNames[i]);
        }

        System.out.println("║  [7] Custom date (YYYY-MM-DD)");
        System.out.println("║  [0] Cancel / Go Back");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        int choice = getValidIntInput(sc, "Choose: ", 0, 7);

        if (choice == 0) {
            return null;
        }

        if (choice == 7) {
            return getCustomDate(sc);
        }

        return dates[choice - 1];
    }

    /**
     * Get custom date from user with validation
     * @param sc Scanner instance
     * @return Valid date string or null if invalid
     */
    private static String getCustomDate(Scanner sc) {
        System.out.print("\nEnter date (YYYY-MM-DD): ");
        String dateStr = sc.nextLine().trim();

        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            LocalDate today = LocalDate.now();
            LocalDate maxDate = today.plusMonths(3);

            if (date.isBefore(today)) {
                System.out.println("❌ Cannot book appointments in the past.");
                return null;
            }

            if (date.isAfter(maxDate)) {
                System.out.println("❌ Cannot book appointments more than 3 months in advance.");
                return null;
            }

            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                System.out.println("❌ Sorry, we're closed on Sundays.");
                return null;
            }

            return dateStr;

        } catch (DateTimeParseException e) {
            System.out.println("❌ Invalid date format. Please use YYYY-MM-DD.");
            return null;
        }
    }

    /**
     * Save an appointment - delegates to Repository SQLite
     * @param customerID The customer ID
     * @param date The appointment date
     * @param status The appointment status
     */
    public static void saveAppointment(int customerID, String date, String status) {
        repo.saveAppointmentRecord(customerID, date, status);
    }

    /**
     * Show appointments for a customer - reads from SQLite
     * @param customerID The customer ID
     */
    public static void showAppointments(int customerID) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                  📅 MY APPOINTMENTS                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        boolean found = false;
        int num = 1;

        // Read from SQLite database via Repository
        String[][] dbAppts = repo.getAppointments(customerID);
        if (dbAppts != null) {
            for (String[] appt : dbAppts) {
                if (!"CANCELLED".equals(appt[1])) {
                    System.out.printf("  [%d] %s - %s%n", num++, appt[0], appt[1]);
                    found = true;
                }
            }
        }

        if (!found) {
            System.out.println("\n  No appointments found.");
        }
    }

    /**
     * Cancel an appointment - updates SQLite via Repository
     * @param customerID The customer ID
     * @param num The appointment number to cancel
     * @return true if cancelled successfully
     */
    public static boolean cancelAppointment(int customerID, int num) {
        int count = 0;

        // Query database appointments from SQLite
        String[][] dbAppts = repo.getAppointments(customerID);
        if (dbAppts != null) {
            for (String[] appt : dbAppts) {
                if (!"CANCELLED".equals(appt[1])) {
                    count++;
                    if (count == num) {
                        repo.cancelAppointment(customerID, appt[0]);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Move an appointment to a new date - updates SQLite via Repository
     * @param customerID The customer ID
     * @param num The appointment number to move
     * @param newDate The new date
     * @return true if moved successfully
     */
    public static boolean moveAppointment(int customerID, int num, String newDate) {
        if (newDate == null) return false;

        int count = 0;

        // Query database appointments from SQLite
        String[][] dbAppts = repo.getAppointments(customerID);
        if (dbAppts != null) {
            for (String[] appt : dbAppts) {
                if (!"CANCELLED".equals(appt[1])) {
                    count++;
                    if (count == num) {
                        repo.moveAppointment(customerID, appt[0], newDate);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Get valid integer input
     */
    private static int getValidIntInput(Scanner sc, String prompt, int min, int max) {
        System.out.print(prompt);
        int input = -1;
        boolean valid = false;

        while (!valid) {
            try {
                input = sc.nextInt();
                sc.nextLine();

                if (input >= min && input <= max) {
                    valid = true;
                } else {
                    System.out.print("[!] Please enter a number between " + min + " and " + max + ": ");
                }
            } catch (Exception e) {
                System.out.print("[!] Invalid input. Please enter a number: ");
                sc.nextLine();
            }
        }

        return input;
    }
}

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// Queue Checker Class - Manages customer queue system
public class QueueChecker {

    private final Repository repo;
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isShutdown;

    public QueueChecker() {
        this.repo = Repository.getInstance();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "QueueChecker-Thread");
            t.setDaemon(true);
            return t;
        });
        this.isShutdown = new AtomicBoolean(false);
    }

    // Check in customer to a class/queue
    public void checkInClass(int customerID, String className) {
        if (isShutdown.get()) {
            System.out.println("\nQueue system is shutting down.");
            return;
        }

        String membership = repo.getMembershipType(customerID);
        int position = repo.getQueuePosition(membership);

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      CHECK-IN CONFIRMED                      ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Activity: %-50s ║%n", className);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        if ("VIP".equalsIgnoreCase(membership)) {
            System.out.println("║                                                              ║");
            System.out.println("║                         VIP ACCESS                           ║");
            System.out.println("║                                                              ║");
            System.out.println("║               Proceeding directly - No queue!                ║");
            System.out.println("║                                                              ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            repo.saveQueueEntry(customerID, className, "VIP", 0, "ACTIVE");
        } else {
            System.out.println("║                                                              ║");
            System.out.println("║                        REGULAR QUEUE                         ║");
            System.out.println("║                                                              ║");
            System.out.printf("║         Your Position: %d%n", position);
            System.out.printf("║         Estimated Wait: %d minutes%n", position);
            System.out.println("║                                                              ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            repo.saveQueueEntry(customerID, className, "REGULAR", position, "WAITING");
            startQueueTimer(customerID, position);
        }
    }

// Start queue timer for regular customers
    private void startQueueTimer(int customerID, int position) {
        if (isShutdown.get()) return;

        scheduler.schedule(() -> {
            if (!isShutdown.get()) {
                repo.updateQueueStatus(customerID, "READY");
                System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
                System.out.printf("║  🔔 Customer %d - Your activity is ready!%n", customerID);
                System.out.println("╚══════════════════════════════════════════════════════════════╝");
            }
        }, Math.min(position, 30), TimeUnit.MINUTES); // Cap at 30 minutes for demo
    }

// Show queue status for a customer
    public void showQueueStatus(int customerID) {
        String membership = repo.getMembershipType(customerID);
        
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                          QUEUE STATUS                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        if ("VIP".equalsIgnoreCase(membership)) {
            System.out.println("║                                                              ║");
            System.out.println("║                         VIP Member                           ║");
            System.out.println("║            Priority Access Enabled - No Queue!               ║");
            System.out.println("║                                                              ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            return;
        }

        String[] queueData = repo.getQueueStatus(customerID);
        if (queueData != null) {
            String className = queueData[0];
            String status = queueData[1];
            int position = Integer.parseInt(queueData[2]);

            if ("WAITING".equals(status) || "ACTIVE".equals(status) || "READY".equals(status)) {
                System.out.printf("║  Activity: %s%n", className);
                System.out.printf("║  Position: %d%n", position);
                System.out.printf("║  Status:   %s%n", status);
                
                if ("WAITING".equals(status)) {
                    System.out.printf("║  Estimated wait: %d minutes%n", position);
                } else if ("READY".equals(status)) {
                    System.out.println("║  Your activity is ready! Proceed now!");
                }
            } else {
                System.out.println("║  No active queue entries.");
            }
        } else {
            System.out.println("║  No active queue entries.");
            System.out.println("║  Check in at an activity to join a queue.");
        }
        
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

// Shutdown the queue checker
    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}

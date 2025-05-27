import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Billing {
    private int billNumber;
    private String patientId;
    private String patientName;
    private double amount;
    private String status;
    private LocalDate issuedDate;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String description;
    private List<BillItem> items = new ArrayList<>();

    // Constructor for creating new bills
    public Billing(String patientId, String patientName, String description) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.description = description;
        this.amount = 0.0;
        this.status = "Pending";
        this.issuedDate = LocalDate.now();
    }

    // Constructor for loading from database
    public Billing(int billNumber, String patientId, String patientName, double amount,
                   String status, LocalDate issuedDate, LocalDate paymentDate,
                   String paymentMethod, String description) {
        this.billNumber = billNumber;
        this.patientId = patientId;
        this.patientName = patientName;
        this.amount = amount;
        this.status = status;
        this.issuedDate = issuedDate;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.description = description;
    }

    // Getters
    public int getBillNumber() { return billNumber; }
    public String getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public double getAmount() { return amount; }
    public String getStatus() { return status; }
    public LocalDate getIssuedDate() { return issuedDate; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getDescription() { return description; }
    public List<BillItem> getItems() { return items; }

    // Setters
    public void setBillNumber(int billNumber) { this.billNumber = billNumber; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setStatus(String status) { this.status = status; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public void addItem(BillItem item) {
        items.add(item);
        amount += item.getAmount();
    }

    public void printBill() {
        System.out.println("\n=== Medical Bill ===");
        System.out.println("Bill #: " + billNumber);
        System.out.println("Patient: " + patientName + " (" + patientId + ")");
        System.out.println("Date: " + issuedDate);
        System.out.println("Status: " + status);
        if (paymentMethod != null) {
            System.out.println("Payment Method: " + paymentMethod);
        }
        System.out.println("\nServices and Items:");

        for (BillItem item : items) {
            System.out.printf("- %-30s EGP%.2f%n", item.getDescription(), item.getAmount());
        }

        System.out.printf("%nTotal Amount Due: EGP%.2f%n", amount);
    }
}
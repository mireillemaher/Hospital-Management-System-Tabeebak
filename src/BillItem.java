public class BillItem {
    private String description;
    private double amount;

    public BillItem(String description, double amount) {
        this.description = description;
        this.amount = amount;
    }

    public String getDescription() { return description; }
    public double getAmount() { return amount; }
}

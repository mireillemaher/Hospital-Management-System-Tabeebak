import java.util.Date;

public class AvailableSlot {
    private int slotId;
    private String doctorId;
    private String slotDate;

    public AvailableSlot(int slotId, String doctorId, String slotDate) {
        this.slotId = slotId;
        this.doctorId = doctorId;
        this.slotDate = slotDate;
    }

    // Getters
    public int getSlotId() { return slotId; }
    public String getDoctorId() { return doctorId; }
    public String getSlotDate() { return slotDate; }

    @Override
    public String toString() {
        return String.format("Slot ID: %d, Doctor: %s, Date: %s",
                slotId, doctorId, slotDate);
    }
}
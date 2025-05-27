import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

public class TimeSlot {
    private LocalDate date;
    private LocalTime startTime;
    private int durationMinutes;
    private String status;
    private Time endTime;

    public TimeSlot(LocalDate date, LocalTime startTime, int durationMinutes, String status) {
        this.date = date;
        this.startTime = startTime;
        this.durationMinutes = durationMinutes;
        this.status = status;
    }

//    public TimeSlot(LocalDate date, LocalTime startTime, int durationMinutes) {
//        this.date = date;
//        this.startTime = startTime;
//        this.durationMinutes = durationMinutes;
//    }

    public TimeSlot(LocalDate slotDate, LocalTime startTime, Time endTime, String status) {
        this.date = slotDate;
        this.startTime = startTime;
        this.status = status;
        this.endTime = endTime;
    }

    // Getters
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return startTime.plusMinutes(durationMinutes); }
    public int getDurationMinutes() { return durationMinutes; }
    public String getStatus() { return status; }
    public Time getGivenEndTime(){ return endTime; }

    // For table display
    public String getDateString() { return date.toString(); }
    public String getTimeRange() { return startTime + " - " + getEndTime(); }
}
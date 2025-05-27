import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Appointment {
    private SimpleIntegerProperty appointmentId;
    private  StringProperty date;
    private  StringProperty time;
    private  StringProperty doctor;
    private final StringProperty status;
    private  StringProperty reason;

    public Appointment(int id, String date, String timeSlot, String doctor, String status, String reason) {
        this.appointmentId = new SimpleIntegerProperty(id);  // Fixed: Set the actual ID
        this.date = new SimpleStringProperty(date);
        this.time = new SimpleStringProperty(timeSlot);
        this.doctor = new SimpleStringProperty(doctor);
        this.status = new SimpleStringProperty(status);
        this.reason = new SimpleStringProperty(reason);
    }

//    public Appointment(String appointmentDate, String timeSlot, String patientName, String status) {
//        this.appointmentId = new SimpleIntegerProperty();
//        this.date = new SimpleStringProperty(appointmentDate);
//        this.time = new SimpleStringProperty(timeSlot);
//        this.doctor = new SimpleStringProperty(patientName);
//        this.status = new SimpleStringProperty(status);
//        this.reason = new SimpleStringProperty("");
//    }

    public int getAppointmentId() { return appointmentId.get(); }


    // Property getters
    public StringProperty dateProperty() { return date; }
    public StringProperty timeProperty() { return time; }
    public StringProperty doctorProperty() { return doctor; }
    public StringProperty statusProperty() { return status; }
    public StringProperty reasonProperty() { return reason; }

    // Regular getters
    public String getDate() { return date.get(); }
    public String getTime() { return time.get(); }
    public String getDoctor() { return doctor.get(); }
    public String getStatus() { return status.get(); }
    public String getReason() { return reason.get(); }

    public String getDoctorName() {
        return doctor.get();
    }


    public String get(String doctor) {
        return doctor;
    }

    private String patientId;
    private String doctorId;
    private String appointmentDate;

    public Appointment(int appointmentId, String patientId, String doctorId,
                       String appointmentDate, String status) {
        this.appointmentId = new SimpleIntegerProperty(appointmentId);  // Fixed: Set the actual ID
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.status = new SimpleStringProperty(status);
    }

    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getAppointmentDate() { return appointmentDate; }
}
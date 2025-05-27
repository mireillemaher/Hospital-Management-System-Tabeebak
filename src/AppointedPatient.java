import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

public class AppointedPatient {
    private final SimpleStringProperty fullName;
    private final SimpleStringProperty gender;
    private final SimpleStringProperty contactNumber;
    private final SimpleStringProperty appointmentDate;
    private final SimpleStringProperty reason;
    private final StringProperty appointmentTime;  // New field for time


    public AppointedPatient(String fullName, String gender, String contactNumber, LocalDate appointmentDate,String appointmentTime ,String reason) {
        this.fullName = new SimpleStringProperty(fullName);
        this.gender = new SimpleStringProperty(gender);
        this.contactNumber = new SimpleStringProperty(contactNumber);
        this.appointmentDate = new SimpleStringProperty(appointmentDate != null ? appointmentDate.toString() : "");
        this.appointmentTime = new SimpleStringProperty(appointmentTime);
        this.reason = new SimpleStringProperty(reason != null ? reason : "");
    }



    public String getAppointmentTime() {
        return appointmentTime.get();
    }

    public StringProperty appointmentTimeProperty() {
        return appointmentTime;
    }

    // Getters for JavaFX properties
    public String getFullName() { return fullName.get(); }
    public SimpleStringProperty fullNameProperty() { return fullName; }

    public String getGender() { return gender.get(); }
    public SimpleStringProperty genderProperty() { return gender; }

    public String getContactNumber() { return contactNumber.get(); }
    public SimpleStringProperty contactNumberProperty() { return contactNumber; }

    public String getAppointmentDate() { return appointmentDate.get(); }
    public SimpleStringProperty appointmentDateProperty() { return appointmentDate; }

    public String getReason() { return reason.get(); }
    public SimpleStringProperty reasonProperty() { return reason; }

    public String getId() {
        return fullName.get();
    }
}
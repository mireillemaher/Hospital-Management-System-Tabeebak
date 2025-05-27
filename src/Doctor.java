import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Doctor {
    private String id;
    private String name;
    private Specialization specialization;
    private List<Appointment> availableApts;
    private Date date;
    private String password;

    // Constructor
    public Doctor(String id, String name, Specialization specialization, List<Appointment> availableApts) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
        this.availableApts = availableApts;
        this.date = new Date();
    }
    public Doctor(String id, String name, Specialization specialization) {
        this.id = id;
        this.name = name;
        this.specialization = specialization;
    }

    public Doctor(String doctorID, String firstName, String lastName, String specialization, String password) {
        this.id = doctorID;
        this.name = firstName + " " + lastName;
        this.specialization = Specialization.valueOf(specialization);
        this.date = new Date();
        this.password = password;


    }

    public Doctor(String doctorId, String fullName, String specialization) {
        this.id = doctorId;
        this.name = fullName;
        this.specialization = Specialization.valueOf(specialization);
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setSpecialization(Specialization specialization) {
        this.specialization = specialization;
    }
    public void setAvailableApts(List<Appointment> availableApts) {
        this.availableApts = availableApts;
    }

    // Getters
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public Specialization getSpecialization() {
        return specialization;
    }
    public List<Appointment> getAvailableApts() {
        return availableApts;
    }
    public Date getDate() {
        return date;
    }

    // Methods
    @Override
    public String toString() {
        return "Doctor{" +
                "ID='" + id + '\'' +
                ", Name='" + name + '\'' +
                ", Specialization='" + specialization + '\'' +
                ", AvailableAppointments=" + availableApts +
                ", Date=" + date +
                '}';
    }

    public void addAppts(Appointment appt) {
        availableApts.add(appt);
    }

    public String getFullName() {
        return name;
    }

    public String getDoctorId() {
        return id;
    }
}

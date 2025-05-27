import java.util.*;

public class Patient {
    private String id;
    private String firstName;
    private String lastName;
    private String dob;
    private String gender;
    private String contactNumber;
    private String email;
    private String address;
    private String password;
    private List<MedicalRecord> medicalRecords;
    private String patientId;

    // Constructor for DoctorDashboardController
    public Patient(String id, String fullName, String gender, String contactNumber, String dob, String address) {
        this.id = id;
        // Split fullName into firstName and lastName
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] names = fullName.trim().split("\\s+", 2);
            this.firstName = names[0];
            this.lastName = names.length > 1 ? names[1] : "";
        } else {
            this.firstName = "";
            this.lastName = "";
        }
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.dob = dob;
        this.address = address;
        this.email = "";
        this.password = "";
        this.medicalRecords = new ArrayList<>();
    }

    // Existing constructors
    public Patient(String id, String firstName, String lastName, String dob,
                   String gender, String contactNumber, String email,
                   String address, String password) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.email = email;
        this.address = address;
        this.password = password;
        this.medicalRecords = new ArrayList<>();
    }

    public Patient(String patientId, String firstName, String lastName, String lastVisit) {
        this.id = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = lastVisit;
        this.medicalRecords = new ArrayList<>();
    }

    public Patient(String patientId, String firstName, String lastName) {
        this.id = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.medicalRecords = new ArrayList<>();
    }

    public Patient(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }



    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setDob(String dob) { this.dob = dob; }
    public void setGender(String gender) { this.gender = gender; }
    public void setContactNumber(String phoneNumber) {
        this.contactNumber = phoneNumber;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setAddress(String address) { this.address = address; }
    public void setMedicalRecords(List<MedicalRecord> medicalRecords) {
        this.medicalRecords = medicalRecords;
    }
    public void setPassword(String password) { this.password = password; }

    // Getters
    public String getId() {
        return id;
    }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
    public String getDob() { return dob; }
    public String getGender() { return gender; }
    public String getContactNumber() {
        return contactNumber;
    }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getPassword() { return password; }
    public List<MedicalRecord> getMedicalRecords() {
        return medicalRecords;
    }
    public String getPatientId() {
        return id;
    }
    @Override
    public String toString() {
        return this.patientId + " - " + this.firstName + " " + this.lastName;
    }

    public List<Appointment> viewAppointments(Specialization specialization, List<Doctor> doctors) {
        List<Appointment> availableAppts = new ArrayList<>();
        for (Doctor doctor : doctors) {
            if (doctor.getSpecialization().equals(specialization)) {
                for (Appointment appointment : doctor.getAvailableApts()) {
                    if (appointment.getStatus().equals("Free")) {
                        availableAppts.add(appointment);
                    }
                }
            }
        }
        return availableAppts;
    }
}
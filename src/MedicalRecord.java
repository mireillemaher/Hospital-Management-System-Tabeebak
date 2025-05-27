import javafx.beans.property.*;

import java.time.LocalDate;


public class MedicalRecord {
    // Static ID counter
    private static int nextId = 1;
    private  SimpleStringProperty patientName;
    private  SimpleStringProperty prescription;
    private  SimpleStringProperty testResults;
    private  SimpleObjectProperty<LocalDate> recordDate;
    private  SimpleObjectProperty<LocalDate> appointmentDate;
    // Core identifiers
    private final IntegerProperty recordId = new SimpleIntegerProperty();
    private final StringProperty patientId = new SimpleStringProperty();
    private final StringProperty doctorId = new SimpleStringProperty();
    private final SimpleStringProperty diagnosis;


    // Medical information

    private final StringProperty medicalCondition = new SimpleStringProperty();

    // Dates

    // Constructors

    private  SimpleStringProperty doctorName;


    public MedicalRecord(String doctorName, String diagnosis, String prescription, String testResults) {
        this.doctorName = new SimpleStringProperty(doctorName);
        this.diagnosis = new SimpleStringProperty(diagnosis);
        this.prescription = new SimpleStringProperty(prescription);
        this.testResults = new SimpleStringProperty(testResults);
    }


    public String getDoctorName() {
        return doctorName.get();
    }


    public StringProperty patientNameProperty() {
        return patientName;
    }
    public String getPatientName() {
        return patientName.get();
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate.get();
    }

    public SimpleStringProperty getDiagnosis() {
        return diagnosis;
    }

    public String getPrescription() {
        return prescription.get();
    }

    public String getTestResults() {
        return testResults.get();
    }


    public MedicalRecord(String patientName, LocalDate appointmentDate,
                         String diagnosis, String prescription,
                         String testResults, LocalDate recordDate) {
        this.patientName = new SimpleStringProperty(patientName);
        this.diagnosis = new SimpleStringProperty(diagnosis);
        this.prescription = new SimpleStringProperty(prescription);
        this.testResults = new SimpleStringProperty(testResults);
        this.recordDate = new SimpleObjectProperty<>(recordDate);
        this.appointmentDate = new SimpleObjectProperty<>(appointmentDate);
    }

    public SimpleObjectProperty<LocalDate> getRecordDate() { return recordDate; }

    public String getFormattedDate() {
        return String.valueOf(recordDate.get());
    }




    // Property accessors

    public String getPatientId() { return patientId.get(); }
    public void setPatientId(String patientId) { this.patientId.set(patientId); }



    public SimpleStringProperty diagnosisProperty() { return diagnosis; }







    // Collection management methods


    // Utility methods
    public String getFormattedRecordDate() {
        return recordDate.get() != null ? recordDate.get().toString() : "N/A";
    }


    public String get(String diagnosis) {
        return diagnosis;
    }
}
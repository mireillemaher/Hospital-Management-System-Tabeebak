import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DoctorDashboardController {

    // Database connection
    Connection connection;
    String currentDoctorId;

    // Patient Management Components
    @FXML
    private TableView<AppointedPatient> patientTable;
    @FXML
    private TableColumn<Patient, String> patientIdColumn;
    @FXML
    private TableColumn<Patient, String> patientNameColumn;
    @FXML
    private TableColumn<Patient, String> patientGenderColumn;
    @FXML
    private TableColumn<Patient, String> patientContactColumn;

    // Appointment Management Components
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private TextField daysField;
    @FXML
    private TextField sessionsField;
    @FXML
    private TextField durationField;
    @FXML
    private TextField breakField;
    @FXML
    private ComboBox<String> startTimeCombo;
    @FXML
    private TableView<TimeSlot> generatedSlotsTable;
    @FXML
    private TableColumn<TimeSlot, String> slotDateColumn;
    @FXML
    private TableColumn<TimeSlot, String> slotTimeColumn;
    @FXML
    private TableColumn<TimeSlot, Integer> slotDurationColumn;
    @FXML
    private Button generateSlotsButton;
    @FXML
    private Button saveSlotsButton;
    @FXML
    private Button clearSlotsButton;
    @FXML
    TableView<TimeSlot> availableSlotsTable;
    @FXML
    private TableColumn<TimeSlot, String> availableDateColumn;
    @FXML
    private TableColumn<TimeSlot, String> availableTimeColumn;
    @FXML
    private TableColumn<TimeSlot, String> availableStatusColumn;
    @FXML
    private Button refreshSlotsButton;
    @FXML
    private Button deleteSelectedSlotsButton;

    // Medical Records Components
    @FXML
    public TableView<MedicalRecord> medicalRecordsTable;
    @FXML
    private TableColumn<MedicalRecord, String> recordPatientColumn;
    @FXML
    private TableColumn<MedicalRecord, String> recordDiagnosisColumn;
    @FXML
    private TableColumn<MedicalRecord, String> recordDateColumn;
    @FXML
    ComboBox<String> patientComboBox;
    @FXML
    private DatePicker recordDatePicker;
    @FXML
    private TextArea diagnosisTextArea;
    @FXML
    private TextArea prescriptionTextArea;
    @FXML
    private TextArea testResultsTextArea;
    @FXML
    private Button clearRecordButton;
    @FXML
    private Button saveRecordButton;
    @FXML
    private ComboBox<LocalDate> appointmentComboBox;


    // Inventory Management Components
    @FXML
    public TableView<InventoryItem> inventoryTable;
    @FXML
    public TableColumn<InventoryItem, String> itemIdColumn;
    @FXML
    public TableColumn<InventoryItem, String> itemNameColumn;
    @FXML
    public TableColumn<InventoryItem, String> itemCategoryColumn;
    @FXML
    public TableColumn<InventoryItem, Integer> itemQuantityColumn;
    @FXML
    public TableColumn<InventoryItem, Double> itemPriceColumn;
    @FXML
    private ComboBox<String> categoryFilterComboBox;
    @FXML
    private TextField inventorySearchField;
    @FXML
    private Button addInventoryButton;
    @FXML
    private Button updateInventoryButton;
    @FXML
    private Button deleteInventoryButton;
    @FXML
    private TableView<Patient> patientTableForInventory;
    @FXML
    private TableColumn<Patient, String> patientIdColumn1;
    @FXML
    private TableColumn<Patient, String> patientNameColumn1;
    @FXML
    private TextArea prescriptionNotesArea;
    @FXML
    private Button addToPatientButton;
    private String doctorId;
    @FXML private TextField invname;
    @FXML private TextField catname;
    @FXML private TextField quanname;
    @FXML private TextField pricename;
    @FXML
    private TextField searchinv;
    @FXML
    private Button searchButton1;
    @FXML
    private Button loadPatientsButton;
    @FXML
    private TextField doctorIdField;

    @FXML
    private void handleSetDoctorId() {
        String enteredId = doctorIdField.getText().trim();
        if (enteredId.isEmpty()) {
            showAlert("Error", "Please enter your doctor ID");
            return;
        }
        this.currentDoctorId = enteredId;
        System.out.println("[DEBUG] Doctor ID set to: " + currentDoctorId);
        showAlert("Success", "Doctor ID set successfully");

        // Reload data after setting the Doctor ID
        loadPatientsForDoctor();
        loadMedicalRecords();
        loadAvailableSlots();
        loadData();
    }

    /* @FXML
     public void initialize(String doctorId) {
         try {
             this.connection = DatabaseHelper.connect();
             this.currentDoctorId = doctorId;
             if (this.connection == null || this.connection.isClosed()) {
                 throw new SQLException("Could not establish database connection");
             }


             System.out.println("[DEBUG] Initializing controller");
             System.out.println("[DEBUG] patientTable: " + (patientTable != null ? "not null" : "null"));
             System.out.println("[DEBUG] Columns: id=" + (patientIdColumn != null ? "not null" : "null") +
                     ", name=" + (patientNameColumn != null ? "not null" : "null") +
                     ", gender=" + (patientGenderColumn != null ? "not null" : "null") +
                     ", contact=" + (patientContactColumn != null ? "not null" : "null"));

             // Initialize columns
             initializeColumns();

             // Initialize UI components
             initializeUI();
             if (currentDoctorId != null && !currentDoctorId.isEmpty()) {
                 loadPatientsForDoctor();  // Load patients first
                 loadMedicalRecords();     // Then load records
                 loadAvailableSlots();     // And other data
             }


             // Load data only if doctorId is set
             if (currentDoctorId != null) {
                 System.out.println("[DEBUG] Loading data for doctor: " + currentDoctorId);
                 loadData();
             }
         } catch (SQLException e) {
             showAlert("Fatal Error", "Database connection failed: " + e.getMessage());
             Platform.exit();
         }

         initializeInventoryTab();
     }

     // Add method to set doctorId explicitly
     public void setDoctorId() {
         this.currentDoctorId = doctorId;
         if (doctorId != null) {
             System.out.println("[DEBUG] DoctorID set to: " + doctorId);
             loadData();
         }
     }

     private void initializeColumns() {
         System.out.println("[DEBUG] Initializing patient table columns...");
         if (patientTable == null || patientIdColumn == null || patientNameColumn == null ||
                 patientGenderColumn == null || patientContactColumn == null) {
             System.err.println("[ERROR] Table or columns not injected");
             System.out.println("[DEBUG] patientTable: " + patientTable);
             System.out.println("[DEBUG] patientIdColumn: " + patientIdColumn);
             System.out.println("[DEBUG] patientNameColumn: " + patientNameColumn);
             System.out.println("[DEBUG] patientGenderColumn: " + patientGenderColumn);
             System.out.println("[DEBUG] patientContactColumn: " + patientContactColumn);
             return;
         }

         patientIdColumn.setCellValueFactory(cellData -> {
             Patient p = cellData.getValue();
             String value = p != null ? p.getId() : "";
             System.out.println("[DEBUG] Rendering ID: " + value);
             return new SimpleStringProperty(value);
         });
         patientNameColumn.setCellValueFactory(cellData -> {
             Patient p = cellData.getValue();
             String value = p != null ? p.getFullName() : "";
             System.out.println("[DEBUG] Rendering Name: " + value);
             return new SimpleStringProperty(value);
         });
         patientGenderColumn.setCellValueFactory(cellData -> {
             Patient p = cellData.getValue();
             String value = p != null ? p.getGender() : "";
             System.out.println("[DEBUG] Rendering Gender: " + value);
             return new SimpleStringProperty(value);
         });
         patientContactColumn.setCellValueFactory(cellData -> {
             Patient p = cellData.getValue();
             String value = p != null ? p.getContactNumber() : "";
             System.out.println("[DEBUG] Rendering Contact: " + value);
             return new SimpleStringProperty(value);
         });

         patientIdColumn.setPrefWidth(150);
         patientNameColumn.setPrefWidth(200);
         patientGenderColumn.setPrefWidth(100);
         patientContactColumn.setPrefWidth(150);

         patientTable.setFixedCellSize(25.0);
         patientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
         patientTable.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-font-size: 14px;");

         System.out.println("[DEBUG] Column count: " + patientTable.getColumns().size());
         patientTable.getColumns().forEach(col ->
                 System.out.println("[DEBUG] Column: " + col.getText() +
                         ", Width: " + col.getWidth() +
                         ", CellValueFactory: " + col.getCellValueFactory()));
     }


     private void initializeUI() {
         // Initialize patient table
 //        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
 //        patientNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
 //        patientGenderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
 //        patientContactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

         // Initialize appointment components
         initializeTimeComboBoxes();
         setupNumericFields();

         loadPatientsForDoctor(); // Add this line


         // Generated slots table
         slotDateColumn.setCellValueFactory(cellData ->
                 new SimpleStringProperty(cellData.getValue().getDateString()));
         slotTimeColumn.setCellValueFactory(cellData ->
                 new SimpleStringProperty(cellData.getValue().getTimeRange()));
         slotDurationColumn.setCellValueFactory(cellData ->
                 new SimpleIntegerProperty(cellData.getValue().getDurationMinutes()).asObject());

         // Available slots table
         availableDateColumn.setCellValueFactory(cellData ->
                 new SimpleStringProperty(cellData.getValue().getDateString()));
         availableTimeColumn.setCellValueFactory(cellData ->
                 new SimpleStringProperty(cellData.getValue().getTimeRange()));
         availableStatusColumn.setCellValueFactory(cellData ->
                 new SimpleStringProperty(cellData.getValue().getStatus()));

         // Medical records table
         recordPatientColumn.setCellValueFactory(cellData -> cellData.getValue().patientNameProperty());
 //        recordDiagnosisColumn.setCellValueFactory(cellData -> cellData.getValue().diagnosisProperty());
         recordDateColumn.setCellValueFactory(cellData ->
                 new SimpleStringProperty(cellData.getValue().getFormattedRecordDate()));

         // Inventory table
         itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
         itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
         itemCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
         itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
         itemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

         // Patient table for inventory
         patientIdColumn1.setCellValueFactory(new PropertyValueFactory<>("id"));
         patientNameColumn1.setCellValueFactory(new PropertyValueFactory<>("fullName"));

         // Set button actions
         generateSlotsButton.setOnAction(event -> handleGenerateSlots());
         saveSlotsButton.setOnAction(event -> handleSaveSlots());
         clearSlotsButton.setOnAction(event -> generatedSlotsTable.getItems().clear());
         refreshSlotsButton.setOnAction(event -> loadAvailableSlots());
         deleteSelectedSlotsButton.setOnAction(event -> handleDeleteSelectedSlots());

         saveRecordButton.setOnAction(event -> handleAddMedicalRecord());
         clearRecordButton.setOnAction(event -> clearMedicalRecordForm());

         addToPatientButton.setOnAction(event -> handlePrescribeItem());
         addInventoryButton.setOnAction(event -> handleAddInventoryItem());
         updateInventoryButton.setOnAction(event -> handleUpdateInventoryItem());
         deleteInventoryButton.setOnAction(event -> handleDeleteInventoryItem());

     }

     private void setupNumericFields() {
         setupNumericField(daysField, 1);
         setupNumericField(sessionsField, 1);
         setupNumericField(durationField, 15);
         setupNumericField(breakField, 0);
     }

     private void setupNumericField(TextField field, int minValue) {
         field.textProperty().addListener((observable, oldValue, newValue) -> {
             if (!newValue.matches("\\d*")) {
                 field.setText(newValue.replaceAll("[^\\d]", ""));
             }
             if (!field.getText().isEmpty() && Integer.parseInt(field.getText()) < minValue) {
                 field.setText(String.valueOf(minValue));
             }
         });
     }

     private void initializeTimeComboBoxes() {
         ObservableList<String> timeSlots = FXCollections.observableArrayList();
         for (int hour = 8; hour <= 17; hour++) {
             for (int minute = 0; minute < 60; minute += 30) {
                 timeSlots.add(String.format("%02d:%02d", hour, minute));
             }
         }
         startTimeCombo.setItems(timeSlots);
         startTimeCombo.setValue("08:00");
     }
 */
    private void loadData() {
        handleShowAppointedPatients();
        loadAvailableSlots();
        loadMedicalRecords();
        loadInventory();
        handleRefreshPatients();
    }

    @FXML
    boolean handleShowAppointedPatients() {
        System.out.println("[DEBUG] 1. Starting patient load for doctor: " + currentDoctorId);

        // 1. Verify database connection
        try {
            if (connection == null || connection.isClosed()) {
                System.out.println("[DEBUG] 2. Re-establishing database connection");
                connection = DatabaseHelper.connect();
            }
            System.out.println("[DEBUG] 3. Connection valid: " + connection.isValid(2));
        } catch (SQLException e) {
            System.err.println("[ERROR] Connection check failed:");
            e.printStackTrace();
            showAlert("Error", "Failed to connect to the database: " + e.getMessage());
            return false;
        }

        // 2. Modified query to join with available_slots
        String query = "SELECT p.first_name, p.last_name, p.gender, p.contact_number, " +
                "a.appointment_date, a.reason, avs.start_time " +  // Added start_time
                "FROM patients p " +
                "JOIN appointments a ON p.patient_id = a.patient_id " +
                "LEFT JOIN available_slots avs ON a.time_slot = avs.slot_id " +  // Join with available_slots
                "WHERE a.DoctorID = ? AND a.status = 'Scheduled' " +
                "ORDER BY a.appointment_date, avs.start_time, p.last_name, p.first_name";  // Added time sorting

        System.out.println("[DEBUG] 4. Query: " + query.replace("?", currentDoctorId));

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, currentDoctorId);

            System.out.println("[DEBUG] 5. Executing query...");
            ResultSet rs = stmt.executeQuery();

            // 3. Process results with row counting
            ObservableList<AppointedPatient> patients = FXCollections.observableArrayList();
            int rowCount = 0;

            System.out.println("[DEBUG] 6. Processing result set...");
            while (rs.next()) {
                rowCount++;

                // Get and format the start time
                String timeDisplay = "N/A";
                Time startTime = rs.getTime("start_time");
                if (startTime != null) {
                    timeDisplay = startTime.toLocalTime()
                            .format(DateTimeFormatter.ofPattern("h:mm a"));
                }

                AppointedPatient p = new AppointedPatient(
                        rs.getString("first_name") + " " + rs.getString("last_name"),
                        rs.getString("gender"),
                        rs.getString("contact_number"),
                        rs.getDate("appointment_date") != null ? rs.getDate("appointment_date").toLocalDate() : null,
                        timeDisplay,  // This should be the formatted time
                        rs.getString("reason")  // This should be the reason
                );

                System.out.println("[DEBUG] 7. Found appointment: " + p.getFullName() +
                        " on " + p.getAppointmentDate() +
                        " at " + timeDisplay +
                        " for " + p.getReason());
                patients.add(p);
            }
            System.out.println("[DEBUG] 8. Total appointments found: " + rowCount);

            // 4. Update UI with verification
            Platform.runLater(() -> {
                System.out.println("[DEBUG] 9. Setting " + patients.size() + " appointments to table");
                patientTable.getItems().clear();
                patientTable.setItems(patients);

                // Adjust column styling if needed
                patientTable.getColumns().forEach(col -> {
                    col.setStyle("-fx-alignment: CENTER_LEFT; -fx-text-fill: black;");
                    // Make time column right-aligned for better readability
                    if ("Time".equals(col.getText())) {
                        col.setStyle("-fx-alignment: CENTER_RIGHT; -fx-text-fill: black;");
                    }
                });

                patientTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
                patientTable.setFixedCellSize(40.0);
                patientTable.refresh();

                System.out.println("[DEBUG] 10. Column count: " + patientTable.getColumns().size());
                patientTable.getColumns().forEach(col ->
                        System.out.println("  Column: " + col.getText()));

                if (patients.isEmpty()) {
                    showAlert("Information", "No appointments found for this doctor");
                } else {
                    System.out.println("[DEBUG] 11. Appointments should be visible now");
                }
            });

        } catch (SQLException e) {
            System.err.println("[ERROR] Database error:");
            e.printStackTrace();
            showAlert("Database Error", "Failed to fetch appointments: " + e.getMessage());
        }
        return false;
    }

    @FXML
    private void handleRefreshPatients() {
        if (currentDoctorId != null) {
            handleShowAppointedPatients();
        } else {
            showAlert("Error", "No doctor selected");
        }
    }

    // Appointment Management
    @FXML
    private void handleGenerateSlots() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            if (startDate == null) {
                showAlert("Error", "Please select a start date");
                return;
            }

            int days = Integer.parseInt(daysField.getText());
            int sessions = Integer.parseInt(sessionsField.getText());
            int duration = Integer.parseInt(durationField.getText());
            int breakTime = Integer.parseInt(breakField.getText());

            // Fixed start time at 9:00 AM
            LocalTime startTime = LocalTime.of(9, 0);
            LocalTime endOfDay = LocalTime.of(17, 0); // 5 PM

            ObservableList<TimeSlot> slots = FXCollections.observableArrayList();

            for (int day = 0; day < days; day++) {
                LocalDate currentDate = startDate.plusDays(day);
                LocalTime currentTime = startTime;
                int sessionsCreated = 0;

                while (sessionsCreated < sessions) {
                    LocalTime endTime = currentTime.plusMinutes(duration);

                    // Stop if next slot would be after 5 PM
                    if (endTime.isAfter(endOfDay)) {
                        break;
                    }

                    slots.add(new TimeSlot(
                            currentDate,
                            currentTime,
                            duration,
                            "Available"
                    ));

                    sessionsCreated++;
                    currentTime = endTime.plusMinutes(breakTime);
                }
            }

            // Set the generated slots to the generatedSlotsTable
            generatedSlotsTable.setItems(slots);

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers in all fields");
        }
    }

    @FXML
    private void handleSaveSlots() {
        // 1. Validate DoctorID exists
        if (currentDoctorId == null || currentDoctorId.isEmpty()) {
            showAlert("Error", "Please set your doctor ID first");
            return;
        }
        System.out.println("[DEBUG] Current DoctorID: " + currentDoctorId); // Add this line

        // 2. Validate slots exist
        ObservableList<TimeSlot> slots = generatedSlotsTable.getItems();
        if (slots == null || slots.isEmpty()) {
            showAlert("Error", "No slots to save");
            return;
        }

        // 3. Ensure database connection
        try {
            if (connection == null || connection.isClosed()) {
                connection = DatabaseHelper.connect();
                if (connection == null || connection.isClosed()) {
                    showAlert("Error", "Database connection unavailable");
                    return;
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Connection failed: " + e.getMessage());
            return;
        }

        // 4. Save slots
        try {
            connection.setAutoCommit(false);
            String sql = "INSERT INTO available_slots (DoctorID, slot_date, start_time, end_time, duration_minutes) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (TimeSlot slot : slots) {
                    stmt.setString(1, String.valueOf(currentDoctorId)); // This was failing before
                    stmt.setDate(2, Date.valueOf(slot.getDate()));
                    stmt.setTime(3, Time.valueOf(slot.getStartTime()));
                    stmt.setTime(4, Time.valueOf(slot.getEndTime()));
                    stmt.setInt(5, slot.getDurationMinutes());
                    stmt.addBatch();
                }

                int[] results = stmt.executeBatch();
                connection.commit();

                int savedCount = Arrays.stream(results).sum();
                showAlert("Success", savedCount + " slots saved successfully");
                loadAvailableSlots();
                generatedSlotsTable.getItems().clear();
            }
        } catch (SQLException e) {
            try {
                if (connection != null) connection.rollback();
            } catch (SQLException ex) {
                System.err.println("Rollback failed: " + ex.getMessage());
            }
            showAlert("Database Error", "Save failed: " + e.getMessage());
        } finally {
            try {
                if (connection != null) connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Auto-commit reset failed: " + e.getMessage());
            }
        }
    }

    public boolean loadAvailableSlots() {
        try {
            String query = "SELECT slot_date, start_time, end_time, " +
                    "CASE WHEN slot_id IN (SELECT slot_id FROM appointments) THEN 'Booked' ELSE 'Available' END as status " +
                    "FROM available_slots WHERE DoctorID = ?";

            ObservableList<TimeSlot> slots = FXCollections.observableArrayList();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, String.valueOf(currentDoctorId));
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    slots.add(new TimeSlot(
                            rs.getDate("slot_date").toLocalDate(),
                            rs.getTime("start_time").toLocalTime(),
                            Time.valueOf(rs.getTime("end_time").toLocalTime()),
                            rs.getString("status")
                    ));
                }
            }
            availableSlotsTable.setItems(slots);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load available slots: " + e.getMessage());
            return false;
        }
        return true;
    }
/*
    private void handleDeleteSelectedSlots() {
        TimeSlot selectedSlot = availableSlotsTable.getSelectionModel().getSelectedItem();
        if (selectedSlot == null) {
            showAlert("Error", "Please select a slot to delete");
            return;
        }

        try {
            String query = "DELETE FROM available_slots WHERE DoctorID = ? AND slot_date = ? AND start_time = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, String.valueOf(currentDoctorId));
                stmt.setDate(2, Date.valueOf(selectedSlot.getDate()));
                stmt.setTime(3, Time.valueOf(selectedSlot.getStartTime()));

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert("Success", "Slot deleted successfully");
                    loadAvailableSlots();
                }
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to delete slot: " + e.getMessage());
        }
    }*/

    // Medical Records


    //    private void setupMedicalRecordsTab() {
//        System.out.println("[DEBUG] Setting up Medical Records tab");
//        loadPatientsForDoctor();
//
//        // Ensure the ComboBox is interactive
//        patientComboBox.setDisable(false);
//        patientComboBox.setEditable(false); // Optional: Prevent manual text input
//        patientComboBox.setFocusTraversable(true);
//
//        // Set up the event handler for patient selection
//        patientComboBox.setOnAction(e -> {
//            System.out.println("[DEBUG] Patient selected in ComboBox: " + patientComboBox.getValue());
//            loadPatientAppointments();
//        });
//
//        System.out.println("[DEBUG] patientComboBox event handler set: " + (patientComboBox.getOnAction() != null));
//    }
    boolean loadPatientsForDoctor() {
        if (currentDoctorId == null || currentDoctorId.isEmpty()) {
            showAlert("Error", "No doctor selected");
            return false;
        }

        String query = "SELECT DISTINCT p.patient_id, p.first_name, p.last_name " +
                "FROM patients p " +
                "JOIN appointments a ON p.patient_id = a.patient_id " +
                "WHERE a.DoctorID = ? " +
                "ORDER BY p.last_name, p.first_name";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, currentDoctorId);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> patientNames = FXCollections.observableArrayList();

            while (rs.next()) {
                String patientId = rs.getString("patient_id");
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                patientNames.add(fullName);
            }

            Platform.runLater(() -> {
                patientComboBox.setItems(patientNames);
                if (!patientNames.isEmpty()) {
                    patientComboBox.getSelectionModel().selectFirst();
                }
            });
        } catch (SQLException e) {
            showAlert("Error", "Failed to load patients: " + e.getMessage());
            return false;
        }
        return true;
    }

    @FXML
    private void handleFindAppointments() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showAlert("Error", "Please enter both first and last name");
            return;
        }

        loadPatientAppointments(firstName, lastName);
    }

//    private Map<String, String> patientIdMap = new HashMap<>();

    public boolean loadPatientAppointments(String firstName, String lastName) {
        String query = "SELECT a.appointment_id, a.appointment_date " +
                "FROM appointments a " +
                "JOIN patients p ON a.patient_id = p.patient_id " +
                "WHERE a.DoctorID = ? AND p.first_name = ? AND p.last_name = ? " +
                "AND a.status = 'Scheduled' " +  // Only scheduled appointments
                "ORDER BY a.appointment_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, currentDoctorId);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);

            ResultSet rs = stmt.executeQuery();

            ObservableList<LocalDate> appointmentDates = FXCollections.observableArrayList();
            appointmentIdMap.clear();

            while (rs.next()) {
                LocalDate date = rs.getDate("appointment_date").toLocalDate();
                appointmentDates.add(date);
                appointmentIdMap.put(date, rs.getInt("appointment_id"));
            }

            Platform.runLater(() -> {
                appointmentComboBox.setItems(appointmentDates);
                if (!appointmentDates.isEmpty()) {
                    appointmentComboBox.getSelectionModel().selectFirst();
                } else {
                    showAlert("Information", "No scheduled appointments found for " + firstName + " " + lastName);
                }
            });
        } catch (SQLException e) {
            showAlert("Error", "Failed to load appointments: " + e.getMessage());
            return false;
        }
        return true;
    }

    @FXML
    private void handleShowAllRecords() {
        String query = "SELECT p.first_name, p.last_name, " +
                "a.appointment_date, mr.diagnosis, mr.prescription, " +
                "mr.test_results, mr.record_date " +
                "FROM medicalrecords mr " +
                "JOIN patients p ON mr.patient_id = p.patient_id " +
                "LEFT JOIN appointments a ON mr.record_id = a.appointment_id " +
                "WHERE mr.DoctorID = ? " +
                "ORDER BY mr.record_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, currentDoctorId);
            ResultSet rs = stmt.executeQuery();

            ObservableList<MedicalRecord> records = FXCollections.observableArrayList();
            while (rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                LocalDate appointmentDate = rs.getDate("appointment_date") != null ?
                        rs.getDate("appointment_date").toLocalDate() : null;
                String diagnosis = rs.getString("diagnosis");
                String prescription = rs.getString("prescription");
                String testResults = rs.getString("test_results");
                LocalDate recordDate = rs.getDate("record_date").toLocalDate();

                records.add(new MedicalRecord(
                        fullName,
                        appointmentDate,
                        diagnosis,
                        prescription,
                        testResults,
                        recordDate
                ));
            }
            medicalRecordsTable.setItems(records);
            medicalRecordsTable.refresh();
        } catch (SQLException e) {
            showAlert("Error", "Failed to load medical records: " + e.getMessage());
        }
    }

    ;

    @FXML
    private void handleAddMedicalRecord() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        LocalDate selectedAppointment = appointmentComboBox.getValue();
        String diagnosis = diagnosisTextArea.getText().trim();
        String prescription = prescriptionTextArea.getText().trim();
        String testResults = testResultsTextArea.getText().trim();
        String feeText = consultationFeeField.getText().trim();

        // Validate inputs
        if (firstName.isEmpty() || lastName.isEmpty()) {
            showAlert("Error", "Please enter patient's first and last name");
            return;
        }

        double consultationFee = 0.0;
        try {
            if (!feeText.isEmpty()) {
                consultationFee = Double.parseDouble(feeText);
                if (consultationFee < 0) {
                    showAlert("Error", "Consultation fee cannot be negative");
                    return;
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid consultation fee");
            return;
        }

        try {
            // Get patient ID first
            List<Patient> patients = findPatientsByName(firstName, lastName);
            if (patients.isEmpty()) {
                showAlert("Error", "Patient not found");
                return;
            }
            String patientId = patients.get(0).getPatientId();

            // Then proceed with saving the record
            String query = "INSERT INTO medicalrecords " +
                    "(patient_id, DoctorID, diagnosis, prescription, test_results, first_name, last_name, record_date, consultation_fee) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, patientId);
                stmt.setString(2, currentDoctorId);
                stmt.setString(3, diagnosis);
                stmt.setString(4, prescription);
                stmt.setString(5, testResults);
                stmt.setString(6, firstName);
                stmt.setString(7, lastName);
                stmt.setDate(8, Date.valueOf(LocalDate.now()));
                stmt.setDouble(9, consultationFee);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    // Also add to billing if fee > 0
                    if (consultationFee > 0) {
                        addConsultationToBilling(patientId, patients.get(0).getFullName(), consultationFee);
                    }

                    showAlert("Success", "Medical record saved successfully!");
                    clearMedicalRecordForm();
                    loadMedicalRecords();
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to save medical record: " + e.getMessage());
        }
    }

    public boolean addConsultationToBilling(String patientId, String patientName, double fee) throws SQLException {
        try {
            connection.setAutoCommit(false);

            // 1. Check for existing pending bill
            int billId;
            String checkBillSql = "SELECT bill_id FROM bills WHERE patient_id = ? AND status = 'Pending' LIMIT 1";
            try (PreparedStatement stmt = connection.prepareStatement(checkBillSql)) {
                stmt.setString(1, patientId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    billId = rs.getInt("bill_id");
                } else {
                    // Create new bill if none exists
                    String createBillSql = "INSERT INTO bills (patient_id, patient_name, status, created_at) " +
                            "VALUES (?, ?, 'Pending', CURRENT_TIMESTAMP)";
                    try (PreparedStatement createStmt = connection.prepareStatement(createBillSql,
                            Statement.RETURN_GENERATED_KEYS)) {
                        createStmt.setString(1, patientId);
                        createStmt.setString(2, patientName);
                        createStmt.executeUpdate();
                        ResultSet generatedKeys = createStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            billId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Failed to create bill");
                        }
                    }
                }
            }

            // 2. Add consultation fee to bill_items
            String billItemSql = "INSERT INTO bill_items (bill_id, item_type, descript, quantity, unit_price, amount) " +
                    "VALUES (?, 'Consultation', 'Doctor Consultation Fee', 1, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(billItemSql)) {
                stmt.setInt(1, billId);
                stmt.setDouble(2, fee);
                stmt.setDouble(3, fee);
                stmt.executeUpdate();
            }

            // 3. Update bill total
            String updateBillSql = "UPDATE bills SET total_amount = " +
                    "(SELECT SUM(amount) FROM bill_items WHERE bill_id = ?) " +
                    "WHERE bill_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateBillSql)) {
                stmt.setInt(1, billId);
                stmt.setInt(2, billId);
                stmt.executeUpdate();
            }

            connection.commit();
        } finally {
            connection.setAutoCommit(true);
        }
        return true;
    }


    public List<Patient> findPatientsByName(String firstName, String lastName) throws SQLException {
        String query = "SELECT patient_id, first_name, last_name FROM patients WHERE first_name = ? AND last_name = ?";
        List<Patient> patients = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                patients.add(new Patient(
                        rs.getString("patient_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name")
                ));
            }
        }
        return patients;
    }

    public boolean patientExists(String firstName, String lastName) throws SQLException {
        return !findPatientsByName(firstName, lastName).isEmpty();
    }

    public boolean loadMedicalRecords() {
        String query = "SELECT mr.record_id, p.first_name, p.last_name, " +
                "a.appointment_date, mr.diagnosis, mr.prescription, " +
                "mr.test_results, mr.record_date " +
                "FROM medicalrecords mr " +
                "JOIN patients p ON mr.patient_id = p.patient_id " +
                "LEFT JOIN appointments a ON mr.record_id = a.appointment_id " +
                "WHERE mr.DoctorID = ? " +
                "ORDER BY mr.record_date DESC";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, currentDoctorId);
            ResultSet rs = stmt.executeQuery();

            ObservableList<MedicalRecord> records = FXCollections.observableArrayList();
            while (rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name");
                LocalDate appointmentDate = rs.getDate("appointment_date") != null ?
                        rs.getDate("appointment_date").toLocalDate() : null;
                String diagnosis = rs.getString("diagnosis");
                String prescription = rs.getString("prescription");
                String testResults = rs.getString("test_results");
                LocalDate recordDate = rs.getDate("record_date").toLocalDate();

                records.add(new MedicalRecord(
                        fullName,
                        appointmentDate,
                        diagnosis,
                        prescription,
                        testResults,
                        recordDate
                ));
            }
            medicalRecordsTable.setItems(records);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load medical records: " + e.getMessage());
            return false;
        }
        return true;
    }

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private Button findAppointmentsButton;
    private Map<LocalDate, Integer> appointmentIdMap = new HashMap<>();

    @FXML
    private void clearMedicalRecordForm() {
        firstNameField.clear();
        lastNameField.clear();
        appointmentComboBox.getItems().clear();
        diagnosisTextArea.clear();
        prescriptionTextArea.clear();
        testResultsTextArea.clear();
        consultationFeeField.clear();
    }

    @FXML
    private void handleGenerateBill() {
        // Get patient from form fields
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty()) {
            showAlert("Error", "Please enter patient's first and last name");
            return;
        }

        try {
            // Find patient in database
            List<Patient> patients = findPatientsByName(firstName, lastName);
            if (patients.isEmpty()) {
                showAlert("Error", "No patient found with name: " + firstName + " " + lastName);
                return;
            }

            Patient selectedPatient = patients.get(0);

            // Get all bill items for the patient's pending bill
            String sql = "SELECT bi.item_type, bi.descript, bi.quantity, bi.unit_price, bi.amount " +
                    "FROM bill_items bi " +
                    "JOIN bills b ON bi.bill_id = b.bill_id " +
                    "WHERE b.patient_id = ? AND b.status = 'Pending'";

            StringBuilder billContent = new StringBuilder();
            double total = 0.0;

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, selectedPatient.getId());
                ResultSet rs = stmt.executeQuery();

                // Build bill header
                billContent.append("MEDICAL BILL\n");
                billContent.append("----------------------------------------\n");
                billContent.append("Patient: ").append(selectedPatient.getFullName()).append("\n");
                billContent.append("Date: ").append(LocalDate.now()).append("\n\n");

                // Build bill header row
                billContent.append(String.format("%-15s %-25s %-8s %-10s %-10s\n",
                        "TYPE", "DESCRIPTION", "QTY", "PRICE", "AMOUNT"));
                billContent.append("------------------------------------------------------------\n");

                // Add bill items
                while (rs.next()) {
                    String itemType = rs.getString("item_type");
                    String description = rs.getString("descript");
                    int quantity = rs.getInt("quantity");
                    double unitPrice = rs.getDouble("unit_price");
                    double amount = rs.getDouble("amount");

                    billContent.append(String.format("%-15s %-25s %-8d EGP%-9.2f EGP%-9.2f\n",
                            itemType, description, quantity, unitPrice, amount));
                    total += amount;
                }

                // Add total
                billContent.append("\nTOTAL: EGP").append(String.format("%.2f", total));
            }

            // Show bill in a dialog
            TextArea billTextArea = new TextArea(billContent.toString());
            billTextArea.setEditable(false);
            billTextArea.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");

            Alert billAlert = new Alert(Alert.AlertType.INFORMATION);
            billAlert.setTitle("Patient Bill");
            billAlert.setHeaderText("Bill for " + selectedPatient.getFullName());
            billAlert.getDialogPane().setContent(billTextArea);
            billAlert.getDialogPane().setMinWidth(600);
            billAlert.showAndWait();

////            // Payment options
////            ButtonType payCashButton = new ButtonType("Cash Payment", ButtonBar.ButtonData.OK_DONE);
////            ButtonType payCardButton = new ButtonType("Card Payment", ButtonBar.ButtonData.OK_DONE);
////            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
////
////            Alert paymentAlert = new Alert(Alert.AlertType.CONFIRMATION);
////            paymentAlert.setTitle("Payment Method");
////            paymentAlert.setHeaderText("Select payment method for " + selectedPatient.getFullName());
////            paymentAlert.getButtonTypes().setAll(payCashButton, payCardButton, cancelButton);
////
////            Optional<ButtonType> result = paymentAlert.showAndWait();
////            if (result.isPresent() && result.get() != cancelButton) {
////                String paymentMethod = (result.get() == payCashButton) ? "CASH" : "CARD";
////
////                try {
////                    // Mark bill as paid
////                    String updateSql = "UPDATE bills SET status = 'Paid', payment_method = ?, " +
////                            "payment_date = CURRENT_TIMESTAMP " +
////                            "WHERE patient_id = ? AND status = 'Pending'";
////
////                    try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
////                        stmt.setString(1, paymentMethod);
////                        stmt.setString(2, selectedPatient.getId());
////                        int updated = stmt.executeUpdate();
////
////                        if (updated > 0) {
////                            // Generate receipt
////                            generateReceipt(selectedPatient.getId(), paymentMethod);
////                            showAlert("Success", "Payment recorded successfully!");
////                        } else {
////                            showAlert("Warning", "No pending bill found for this patient");
////                        }
////                    }
////                } catch (SQLException e) {
//////                    showAlert("Error", "Failed to record payment: " + e.getMessage());
////                }
//            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to generate bill: " + e.getMessage());
            e.printStackTrace();
        }
    }

   /* private void generateReceipt(String patientId, String paymentMethod) throws SQLException {
        String sql = "SELECT b.bill_id, p.first_name, p.last_name, b.total_amount, " +
                "b.payment_date, bi.descript, bi.amount " +
                "FROM bills b " +
                "JOIN patients p ON b.patient_id = p.patient_id " +
                "JOIN bill_items bi ON b.bill_id = bi.bill_id " +
                "WHERE b.patient_id = ? AND b.status = 'Paid' " +
                "ORDER BY b.payment_date DESC LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, patientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                StringBuilder receipt = new StringBuilder();
                receipt.append("MEDICAL RECEIPT\n");
                receipt.append("----------------------------------------\n");
                receipt.append("Patient: ").append(rs.getString("first_name")).append(" ")
                        .append(rs.getString("last_name")).append("\n");
                receipt.append("Date: ").append(rs.getTimestamp("payment_date")).append("\n");
                receipt.append("Payment Method: ").append(paymentMethod).append("\n");
                receipt.append("Receipt #: ").append(rs.getInt("bill_id")).append("\n\n");
                receipt.append("ITEMS:\n");

                do {
                    receipt.append("- ").append(rs.getString("descript"))
                            .append(": EGP").append(rs.getDouble("amount")).append("\n");
                } while (rs.next());

                receipt.append("\nTOTAL: EGP").append(rs.getDouble("total_amount"));
                receipt.append("\n\nThank you for your payment!");

                // Show receipt
                TextArea receiptArea = new TextArea(receipt.toString());
                receiptArea.setEditable(false);
                receiptArea.setStyle("-fx-font-family: monospace; -fx-font-size: 14px;");

                Alert receiptAlert = new Alert(Alert.AlertType.INFORMATION);
                receiptAlert.setTitle("Payment Receipt");
                receiptAlert.setHeaderText("Payment Confirmation");
                receiptAlert.getDialogPane().setContent(receiptArea);
                receiptAlert.getDialogPane().setMinWidth(500);
                receiptAlert.showAndWait();
            }
        }
    }
*/


    @FXML
    private TextField consultationFeeField;



//    private void loadMedicalRecords() {
//        try {
//            String query = "SELECT m.*, p.first_name, p.last_name FROM medicalrecords m " +
//                    "JOIN patients p ON m.patient_id = p.patient_id " +
//                    "WHERE m.DoctorID = ?";
//
//            ObservableList<MedicalRecord> records = FXCollections.observableArrayList();
//            try (PreparedStatement stmt = connection.prepareStatement(query)) {
//                stmt.setString(1, String.valueOf(currentDoctorId));
//                ResultSet rs = stmt.executeQuery();
//
//                while (rs.next()) {
//                    records.add(new MedicalRecord(
//                            rs.getString("first_name") + " " + rs.getString("last_name"),
//                            rs.getString("diagnosis"),
//                            rs.getString("prescription"),
//                            rs.getString("test_results"),
//                            rs.getDate("record_date").toLocalDate().atStartOfDay()
//                    ));
//                }
//            }
//            medicalRecordsTable.setItems(records);
//        } catch (SQLException e) {
//            showAlert("Database Error", "Failed to load medical records: " + e.getMessage());
//        }
//    }

    // Inventory Management


    // Add these instance variables to your existing controller
    public ObservableList<InventoryItem> inventoryData = FXCollections.observableArrayList();
/*
    // Add these methods to your existing controller
    private void initializeInventoryTab() {
        // 1. Initialize columns with PropertyValueFactory (simpler approach)
        // Initialize inventory table columns
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        itemCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        itemQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        itemPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        // 2. Initialize with empty list
        inventoryTable.setItems(inventoryData);

        loadInventory();
        loadPatientsForPrescription();

        searchButton1.setOnAction(event -> handleSearchInventory());
        deleteInventoryButton.setOnAction(event -> handleDeleteInventoryItem());

        patientIdColumn1.setCellValueFactory(new PropertyValueFactory<>("id"));
        patientNameColumn1.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        addToPatientButton.setOnAction(event -> handlePrescribeItem());
        loadPatientsButton.setOnAction(event -> handleLoadPatientsForPrescription());

    }*/

    @FXML
    private void handleLoadInventory() {
        try {
            String query = "SELECT * FROM inventory ORDER BY name";

            try (Connection conn = DatabaseHelper.connect();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                ObservableList<InventoryItem> inventoryItems = FXCollections.observableArrayList();

                while (rs.next()) {
                    inventoryItems.add(new InventoryItem(
                            rs.getString("item_id"),
                            rs.getString("name"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getString("category")
                    ));
                }

                Platform.runLater(() -> {
                    inventoryTable.setItems(inventoryItems);
                    inventoryTable.refresh();
                });

            } catch (SQLException e) {
                showAlert("Error", "Failed to load inventory: " + e.getMessage());
            }
        } catch (Exception e) {
            showAlert("Error", "Error loading inventory: " + e.getMessage());
        }
    }


    public boolean loadInventory() {
        try {
            inventoryData.clear();
            inventoryData.addAll(new InventoryDBHandler(DatabaseHelper.connect()).getAllInventory());
            inventoryTable.setItems(inventoryData);
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load inventory: " + e.getMessage());
            return false;
        }
        return true;
    }


    @FXML
    private void handleAddInventoryItem() {
        try {
            // Validate inputs
            if (invname.getText().isEmpty() || catname.getText().isEmpty() ||
                    quanname.getText().isEmpty() || pricename.getText().isEmpty()) {
                showAlert("Error", "Please fill in all fields");
                return;
            }

            // Parse numeric values
            int quantity = Integer.parseInt(quanname.getText());
            double price = Double.parseDouble(pricename.getText());

            if (quantity <= 0 || price <= 0) {
                showAlert("Error", "Quantity and price must be positive numbers");
                return;
            }

            // Get the next available ID
            String nextId = String.valueOf(getNextInventoryId());

            // Create new inventory item
            InventoryItem newItem = new InventoryItem(
                    nextId,
                    invname.getText(),
                    catname.getText(),
                    quantity,
                    price
            );

            // Add to database
            try (Connection conn = DatabaseHelper.connect()) {
                String sql = "INSERT INTO inventory (item_id, name, category, quantity, unit_price) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, newItem.getItemId());
                    pstmt.setString(2, newItem.getName());
                    pstmt.setString(3, newItem.getCategory());
                    pstmt.setInt(4, newItem.getQuantity());
                    pstmt.setDouble(5, newItem.getUnitPrice());
                    pstmt.executeUpdate();
                }
            }

            // Add to table and refresh
            inventoryTable.getItems().add(newItem);
            inventoryTable.refresh();

            // Clear form
            clearInventoryForm();

            showAlert("Success", "Item added successfully with ID: " + nextId);

        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter valid numbers for quantity and price");
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to add item: " + e.getMessage());
        }
    }

    // Helper method to get next available ID
    public String getNextInventoryId() throws SQLException {
        try (Connection conn = DatabaseHelper.connect()) {
            // Get the maximum current ID
            String sql = "SELECT MAX(CAST(SUBSTRING(item_id, 2) AS UNSIGNED)) FROM inventory";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int maxId = rs.getInt(1);
                    return "I"+  (maxId + 1); // Returns I1, I2, etc.
                }
            }
        }
        return "0"; // Default if no items exist
    }

    @FXML
    private void handleSearchInventory() {
        String searchTerm = searchinv.getText().trim();

        if (searchTerm.isEmpty()) {
            // If search field is empty, show all items
            handleLoadInventory();
            return;
        }

        try {
            String query = "SELECT * FROM inventory WHERE name LIKE ? ORDER BY name";

            try (Connection conn = DatabaseHelper.connect();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {

                pstmt.setString(1, "%" + searchTerm + "%");
                ResultSet rs = pstmt.executeQuery();

                ObservableList<InventoryItem> searchResults = FXCollections.observableArrayList();

                while (rs.next()) {
                    searchResults.add(new InventoryItem(
                            rs.getString("item_id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price")
                    ));
                }

                Platform.runLater(() -> {
                    inventoryTable.setItems(searchResults);
                    inventoryTable.refresh();
                });

                if (searchResults.isEmpty()) {
                    showAlert("Information", "No items found matching '" + searchTerm + "'");
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to search inventory: " + e.getMessage());
        }
    }

    // Helper method to clear the form
    private void clearInventoryForm() {
        invname.clear();
        catname.clear();
        quanname.clear();
        pricename.clear();
    }


    @FXML
    private void handleDeleteInventoryItem() {
        InventoryItem selectedItem = inventoryTable.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            showAlert("Error", "Please select an item to delete");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Inventory Item");
        confirmation.setContentText("Do you want to delete this item completely or just reduce quantity by 1?\n" +
                "Item: " + selectedItem.getName() + " (Qty: " + selectedItem.getQuantity() + ")");

        // Create custom buttons
        ButtonType deleteCompletelyButton = new ButtonType("Delete Completely");
        ButtonType reduceByOneButton = new ButtonType("Reduce by 1");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmation.getButtonTypes().setAll(deleteCompletelyButton, reduceByOneButton, cancelButton);

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent()) {
            try {
                if (result.get() == deleteCompletelyButton) {
                    // Delete item completely
                    deleteItemCompletely(selectedItem);
                } else if (result.get() == reduceByOneButton) {
                    // Reduce quantity by 1
                    reduceQuantityByOne(selectedItem);
                }
            } catch (SQLException e) {
                showAlert("Database Error", "Failed to update inventory: " + e.getMessage());
            }
        }
    }

    public boolean deleteItemCompletely(InventoryItem item) throws SQLException {
        try (Connection conn = DatabaseHelper.connect()) {
            String sql = "DELETE FROM inventory WHERE item_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, item.getItemId());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    Platform.runLater(() -> inventoryTable.getItems().remove(item));
                    showAlert("Success", "Item deleted completely");
                    return true;
                }
            }
        }
        return false;
    }
    //wrapper
    public void removeItemFromTable(InventoryItem item) {
        if (Platform.isFxApplicationThread()) {
            inventoryTable.getItems().remove(item);
        } else {
            Platform.runLater(() -> inventoryTable.getItems().remove(item));
        }
    }


    public boolean reduceQuantityByOne(InventoryItem item) throws SQLException {
        if (item.getQuantity() <= 1) {
            // If quantity is 1, delete the item instead
            deleteItemCompletely(item);
            return true;
        }

        try (Connection conn = DatabaseHelper.connect()) {
            String sql = "UPDATE inventory SET quantity = quantity - 1 WHERE item_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, item.getItemId());
                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    // Update the local item quantity
                    item.setQuantity(item.getQuantity() - 1);
                    inventoryTable.refresh();
                    showAlert("Success", "Item quantity reduced by 1. New quantity: " + item.getQuantity());
                    return true;
                }
            }
        }
        return false;
    }


    @FXML
    private void handlePrescribeItem() {
        try {
            // 1. Get selections
            InventoryItem selectedItem = inventoryTable.getSelectionModel().getSelectedItem();
            Patient selectedPatient = patientTableForInventory.getSelectionModel().getSelectedItem();

            if (selectedItem == null || selectedPatient == null) {
                showAlert("Error", "Please select both an item and a patient");
                return;
            }

            // 2. Create prescription dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Prescribe Item");
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            TextField quantityField = new TextField("1");
            quantityField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (!newVal.matches("\\d*")) {
                    quantityField.setText(newVal.replaceAll("[^\\d]", ""));
                }
            });

            TextArea notesArea = new TextArea();
            grid.addRow(0, new Label("Quantity:"), quantityField);
            grid.addRow(1, new Label("Notes:"), notesArea);
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            // 3. Process prescription
            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                int quantity = Integer.parseInt(quantityField.getText());

                if (quantity <= 0) {
                    showAlert("Error", "Quantity must be positive");
                    return;
                }

                if (quantity > selectedItem.getQuantity()) {
                    showAlert("Error", "Not enough stock available");
                    return;
                }

                try (Connection conn = DatabaseHelper.connect()) {
                    conn.setAutoCommit(false); // Start transaction

                    try {
                        int billId = getOrCreateBill(conn, selectedPatient);
                        addItemToBill(conn, billId, selectedItem, quantity, notesArea.getText());
                        updateInventory(conn, selectedItem, quantity);
                        updateBillTotal(conn, billId);
                        conn.commit();
                        selectedItem.setQuantity(selectedItem.getQuantity() - quantity);
                        inventoryTable.refresh();
                        askToGenerateBill(selectedPatient.getId());
                    } catch (SQLException e) {
                        conn.rollback();
                        throw e;
                    } finally {
                        conn.setAutoCommit(true);
                    }
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid quantity");
        } catch (Exception e) {
            showAlert("Error", "Failed to prescribe item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getOrCreateBill(Connection conn, Patient patient) throws SQLException {
        String checkBillSql = "SELECT bill_id FROM bills WHERE patient_id = ? AND status = 'Pending' LIMIT 1";
        try (PreparedStatement stmt = conn.prepareStatement(checkBillSql)) {
            stmt.setString(1, patient.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("bill_id");
            }
        }

        String createBillSql = "INSERT INTO bills (patient_id, patient_name, status) VALUES (?, ?, 'Pending')";
        try (PreparedStatement stmt = conn.prepareStatement(createBillSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, patient.getId());
            stmt.setString(2, patient.getFullName());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        throw new SQLException("Failed to create bill");
    }

    public boolean addItemToBill(Connection conn, int billId, InventoryItem item, int quantity, String notes) throws SQLException {
        String sql = "INSERT INTO bill_items (bill_id, item_type, descript, quantity, unit_price, amount) " +
                "VALUES (?, 'MEDICATION', ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            stmt.setString(2, "Prescribed: " + item.getName() + (notes.isEmpty() ? "" : " (" + notes + ")"));
            stmt.setInt(3, quantity);
            stmt.setDouble(4, item.getUnitPrice());
            stmt.setDouble(5, quantity * item.getUnitPrice());
            stmt.executeUpdate();
        }
        catch(SQLException e) {
            showAlert("Database Error", "Failed to add item to bill: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean updateInventory(Connection conn, InventoryItem item, int quantity) throws SQLException {
        String sql = "UPDATE inventory SET quantity = quantity - ? WHERE item_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setString(2, item.getItemId());
            stmt.executeUpdate();
        }catch(SQLException e) {
            showAlert("Database Error", "Failed to update inventory: " + e.getMessage());
            return false;
        }
        return true;
    }

    public boolean updateBillTotal(Connection conn, int billId) throws SQLException {
        String sql = "UPDATE bills SET total_amount = " +
                "(SELECT SUM(amount) FROM bill_items WHERE bill_id = ?) " +
                "WHERE bill_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            stmt.setInt(2, billId);
            stmt.executeUpdate();
        }catch(SQLException e) {
            showAlert("Database Error", "Failed to update bill total: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void askToGenerateBill(String patientId) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Bill Generated");
        alert.setHeaderText("Item added to patient's bill");
        alert.setContentText("Would you like to view and send the bill now?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            generateAndSendBill(patientId);
        }
    }

    public boolean generateAndSendBill(String patientId) {
        try {
            String sql = "SELECT b.bill_id, b.patient_name, b.total_amount, " +
                    "bi.descript, bi.quantity, bi.unit_price, bi.amount " +
                    "FROM bills b " +
                    "JOIN bill_items bi ON b.bill_id = bi.bill_id " +
                    "WHERE b.patient_id = ? AND b.status = 'Pending'";

            StringBuilder billContent = new StringBuilder();
            double total = 0.0;
            String patientName = "";

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, patientId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    patientName = rs.getString("patient_name");
                    billContent.append("MEDICAL BILL\n");
                    billContent.append("Patient: ").append(patientName).append("\n");
                    billContent.append("Date: ").append(LocalDate.now()).append("\n\n");
                    billContent.append(String.format("%-30s %-10s %-10s %-10s\n",
                            "ITEM", "QTY", "PRICE", "AMOUNT"));
                    billContent.append("--------------------------------------------------\n");

                    do {
                        String item = rs.getString("descript");
                        int qty = rs.getInt("quantity");
                        double price = rs.getDouble("unit_price");
                        double amount = rs.getDouble("amount");

                        billContent.append(String.format("%-30s %-10d EGP%-9.2f EGP%-9.2f\n",
                                item, qty, price, amount));
                        total = rs.getDouble("total_amount");
                    } while (rs.next());

                    billContent.append("\nTOTAL: EGP").append(String.format("%.2f", total));
                }
            }

            // Show bill to doctor
            TextArea billText = new TextArea(billContent.toString());
            billText.setEditable(false);
            billText.setStyle("-fx-font-family: monospace;");

            Alert billAlert = new Alert(Alert.AlertType.CONFIRMATION);
            billAlert.setTitle("Review Bill");
            billAlert.setHeaderText("Bill for " + patientName);
            billAlert.getDialogPane().setContent(billText);

            ButtonType sendButton = new ButtonType("Send to Patient");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            billAlert.getButtonTypes().setAll(sendButton, cancelButton);

            Optional<ButtonType> result = billAlert.showAndWait();
            if (result.isPresent() && result.get() == sendButton) {
                showAlert("Success", "Bill has been sent to patient. They can now view and pay it in their account.");
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to generate bill: " + e.getMessage());
            return false;
        }
        return true;
    }

    @FXML
    private void handleLoadPatientsForPrescription() {
        try {
            String query = "SELECT patient_id, first_name, last_name FROM patients ORDER BY created_at";

            try (Connection conn = DatabaseHelper.connect();
                 PreparedStatement pstmt = conn.prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                ObservableList<Patient> patients = FXCollections.observableArrayList();

                while (rs.next()) {
                    patients.add(new Patient(
                            rs.getString("patient_id"),
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    ));
                }

                Platform.runLater(() -> {
                    patientTableForInventory.setItems(patients);
                    patientTableForInventory.refresh();
                    System.out.println("Loaded " + patients.size() + " patients"); // Debug output

                    // Show success message if no errors
                    if (!patients.isEmpty()) {
                        showAlert("Success", "Patients loaded successfully");
                    } else {
                        showAlert("Information", "No patients found in database");
                    }
                });

            }
        } catch (SQLException e) {
            Platform.runLater(() -> {
                showAlert("Error", "Failed to load patients: " + e.getMessage());
                e.printStackTrace();
            });
        }
    }

    private void showAlert(String title, String message) {
        if (!Platform.isFxApplicationThread()) return;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
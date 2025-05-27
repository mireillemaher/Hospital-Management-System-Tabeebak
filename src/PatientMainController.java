import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PatientMainController {
    // Dashboard Components
    @FXML private Label welcomeLabel;
    @FXML private Label patientNameLabel;
    @FXML private Label nextAppointmentLabel;
    @FXML private Label recentVisitsLabel;

    // Appointment Booking
    @FXML private ComboBox<String> doctorComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> timeSlotComboBox;
    @FXML private TextArea reasonTextArea;

    // History Tab
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, String> dateColumn;
    @FXML private TableColumn<Appointment, String> doctorColumn;
    @FXML private TableColumn<Appointment, String> timeColumn;
    @FXML private TableColumn<Appointment, String> statusColumn;
    @FXML private TableColumn<Appointment, String> reasonColumn;

    // Navigation
    @FXML private TabPane tabPane;
    @FXML private Tab historyTab;

    private Patient currentPatient;
    private Connection connection;
    private final ObservableList<Appointment> appointments = FXCollections.observableArrayList();
    private final Map<String, String> doctorIdMap = new HashMap<>();

    public void initData(Patient patient) {
        this.currentPatient = patient;
        try {
            connection = DatabaseHelper.connect();
            initializeColumns();
            loadPatientData();
            welcomeLabel.setText("Patient: " + patient.getFullName());
            patientNameLabel.setText("Welcome, " + patient.getFirstName() + " " + patient.getLastName());
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to connect: " + e.getMessage());
        }
    }

    @FXML private Tab medicalRecordsTab;
    @FXML private TableView<MedicalRecord> medicalRecordsTable;
    @FXML private TableColumn<MedicalRecord, String> recordDateColumn;
    @FXML private TableColumn<MedicalRecord, String> diagnosisColumn;
    @FXML private TextArea detailedRecordView;
    private final ObservableList<MedicalRecord> medicalRecords = FXCollections.observableArrayList();

    void initializeColumns() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date")); // Not "appointmentDate"
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("doctor"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("time"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        reasonColumn.setCellValueFactory(new PropertyValueFactory<>("reason"));

        // Add selection listener
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        diagnosisColumn.setCellValueFactory(new PropertyValueFactory<>("diagnosis"));
        prescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("prescription"));
        testResultsColumn.setCellValueFactory(new PropertyValueFactory<>("testResults"));
        // Add action column with delete button
        TableColumn<Appointment, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(param -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");
            {
                cancelBtn.getStyleClass().add("danger-button");
                cancelBtn.setOnAction(event -> {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    handleCancelAppointment(appointment);
                });
            }



            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Appointment appointment = getTableView().getItems().get(getIndex());
                    cancelBtn.setDisable(!appointment.getStatus().equals("Scheduled"));
                    setGraphic(cancelBtn);
                }
            }
        });
        appointmentsTable.getColumns().add(actionCol);
    }
    @FXML
    private void initialize() {
        // Add listeners to refresh time slots when doctor or date changes
        doctorComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldDoctor, newDoctor) -> {
            System.out.println("[DEBUG] Doctor changed to: " + newDoctor);
            loadAvailableTimeSlots();
        });

        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            System.out.println("[DEBUG] Date changed to: " + newDate);
            loadAvailableTimeSlots();
        });
    }

    void loadPatientData() {
        loadUpcomingAppointment();
        loadAppointmentHistory();
        loadMedicalRecords(); // Add this line
        loadDoctors();
        initializeDatePicker();
        // Only call loadAvailableTimeSlots if a doctor is selected
        if (doctorComboBox.getValue() != null) {
            loadAvailableTimeSlots();
        } else {
            System.out.println("[DEBUG] Skipping initial loadAvailableTimeSlots call; waiting for doctor selection.");
        }
    }

    void loadUpcomingAppointment() {
        String query = "SELECT a.appointment_date, avs.start_time, d.first_name " +
                "FROM appointments a " +
                "JOIN doctors d ON a.DoctorID = d.DoctorID " +
                "LEFT JOIN available_slots avs ON a.time_slot = avs.slot_id " +  // This is the crucial join
                "WHERE a.patient_id = ? AND a.status = 'Scheduled' " +
                "AND a.appointment_date >= CURDATE() " +
                "ORDER BY a.appointment_date ASC, avs.start_time ASC " +  // Added time sorting
                "LIMIT 1";


        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, currentPatient.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDate date = rs.getDate("appointment_date").toLocalDate();

                String time = rs.getString("start_time");
                String doctor = rs.getString("first_name");
                nextAppointmentLabel.setText(String.format("%s\n%s at %s",
                        doctor,
                        date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                        time));
            } else {
                nextAppointmentLabel.setText("No upcoming appointments");
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to load upcoming appointment: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewBills() {
        try {
            String sql = "SELECT b.bill_id, b.patient_name, b.total_amount, b.status, " +
                    "b.payment_method, b.payment_date, b.created_at " +
                    "FROM bills b " +
                    "WHERE b.patient_id = ? " +
                    "ORDER BY b.status, b.payment_date DESC";

            ObservableList<Billing> bills = FXCollections.observableArrayList();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, currentPatient.getId());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    bills.add(new Billing(
                            rs.getInt("bill_id"),
                            currentPatient.getId(),
                            rs.getString("patient_name"),
                            rs.getDouble("total_amount"),
                            rs.getString("status"),
                            rs.getDate("created_at").toLocalDate(),
                            rs.getDate("payment_date") != null ? rs.getDate("payment_date").toLocalDate() : null,
                            rs.getString("payment_method"),
                            "Medical Services"
                    ));
                }
            }

            // Show bills in a dialog
            TableView<Billing> billTable = new TableView<>();

            TableColumn<Billing, Integer> idCol = new TableColumn<>("Bill ID");
            idCol.setCellValueFactory(new PropertyValueFactory<>("billNumber"));

            TableColumn<Billing, String> nameCol = new TableColumn<>("Patient");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("patientName"));

            TableColumn<Billing, Double> amountCol = new TableColumn<>("Amount");
            amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
            amountCol.setCellFactory(col -> new TableCell<Billing, Double>() {
                @Override protected void updateItem(Double amount, boolean empty) {
                    super.updateItem(amount, empty);
                    if (empty || amount == null) {
                        setText(null);
                    } else {
                        setText(String.format("EGP%.2f", amount));
                    }
                }
            });

            TableColumn<Billing, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

            TableColumn<Billing, LocalDate> dateCol = new TableColumn<>("Issued Date");
            dateCol.setCellValueFactory(new PropertyValueFactory<>("issuedDate"));

            billTable.getColumns().addAll(idCol, nameCol, amountCol, statusCol, dateCol);
            billTable.setItems(bills);

            // Add payment button for pending bills
            TableColumn<Billing, Void> actionCol = new TableColumn<>("Action");
            actionCol.setCellFactory(param -> new TableCell<>() {
                private final Button payButton = new Button("Pay");
                private final Button detailsButton = new Button("Details");
                private final HBox buttons = new HBox(5, detailsButton, payButton);

                {
                    payButton.setOnAction(event -> {
                        Billing bill = getTableView().getItems().get(getIndex());
                        handlePayBill(bill);
                    });

                    detailsButton.setOnAction(event -> {
                        Billing bill = getTableView().getItems().get(getIndex());
                        showBillDetails(bill);
                    });

                    buttons.setAlignment(Pos.CENTER);
                }

                @Override protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        Billing bill = getTableView().getItems().get(getIndex());
                        payButton.setDisable(!bill.getStatus().equals("Pending"));
                        setGraphic(buttons);
                    }
                }
            });
            billTable.getColumns().add(actionCol);

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Your Bills");
            dialog.getDialogPane().setContent(billTable);
            dialog.getDialogPane().setPrefSize(800, 600);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.showAndWait();

        } catch (SQLException e) {
            showAlert("Error", "Failed to load bills: " + e.getMessage());
        }
    }

    private void showBillDetails(Billing bill) {
        try {
            // Load bill items
            String sql = "SELECT item_type, descript, quantity, unit_price, amount " +
                    "FROM bill_items WHERE bill_id = ?";

            StringBuilder details = new StringBuilder();
            details.append("=== Bill Details ===\n");
            details.append("Bill #: ").append(bill.getBillNumber()).append("\n");
            details.append("Patient: ").append(bill.getPatientName()).append("\n");
            details.append("Status: ").append(bill.getStatus()).append("\n");
            details.append("Issued Date: ").append(bill.getIssuedDate()).append("\n");

            if (bill.getPaymentMethod() != null) {
                details.append("Payment Method: ").append(bill.getPaymentMethod()).append("\n");
                details.append("Payment Date: ").append(bill.getPaymentDate()).append("\n");
            }

            details.append("\nItems:\n");

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, bill.getBillNumber());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    details.append(String.format("- %-30s %2d x EGP%6.2f = EGP%7.2f%n",
                            rs.getString("descript"),
                            rs.getInt("quantity"),
                            rs.getDouble("unit_price"),
                            rs.getDouble("amount")));
                }
            }

            details.append("\nTotal: EGP").append(String.format("%.2f", bill.getAmount()));

            TextArea textArea = new TextArea(details.toString());
            textArea.setEditable(false);
            textArea.setStyle("-fx-font-family: monospace;");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Bill Details");
            alert.setHeaderText("Details for Bill #" + bill.getBillNumber());
            alert.getDialogPane().setContent(textArea);
            alert.getDialogPane().setPrefSize(600, 400);
            alert.showAndWait();

        } catch (SQLException e) {
            showAlert("Error", "Failed to load bill details: " + e.getMessage());
        }
    }

    private void handlePayBill(Billing bill) {
        Alert paymentAlert = new Alert(Alert.AlertType.CONFIRMATION);
        paymentAlert.setTitle("Payment");
        paymentAlert.setHeaderText("Pay Bill #" + bill.getBillNumber() + " (EGP" + bill.getAmount() + ")");
        paymentAlert.setContentText("Select payment method:");

        ButtonType cashButton = new ButtonType("Cash");
        ButtonType cardButton = new ButtonType("Credit Card");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        paymentAlert.getButtonTypes().setAll(cashButton, cardButton, cancelButton);

        Optional<ButtonType> result = paymentAlert.showAndWait();
        if (result.isPresent() && result.get() != cancelButton) {
            String method = result.get() == cashButton ? "CASH" : "CARD";
            processPayment(bill, method);
        }
    }

    private void processPayment(Billing bill, String method) {
        try {
            String sql = "UPDATE bills SET status = 'Paid', payment_method = ?, " +
                    "payment_date = ? WHERE bill_id = ?";  // Changed to parameterized date

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, method);

                // Use java.sql.Date instead of string
                stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));

                stmt.setInt(3, bill.getBillNumber());
                int updated = stmt.executeUpdate();

                if (updated > 0) {
                    bill.setStatus("Paid");
                    bill.setPaymentMethod(method);
                    bill.setPaymentDate(LocalDate.now());

                    generateReceipt(bill);
                    showAlert("Success", "Payment processed successfully!");
                } else {
                    showAlert("Error", "Bill not found or already paid");
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Failed to process payment: " + e.getMessage());
        }
    }

    private void generateReceipt(Billing bill) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("=== Payment Receipt ===\n");
        receipt.append("Receipt #: ").append(bill.getBillNumber()).append("\n");
        receipt.append("Date: ").append(LocalDate.now()).append("\n");
        receipt.append("Patient: ").append(bill.getPatientName()).append("\n");
        receipt.append("Payment Method: ").append(bill.getPaymentMethod()).append("\n\n");
        receipt.append("Paid Amount: EGP").append(String.format("%.2f", bill.getAmount())).append("\n");

        // In a real system, you would:
        // 1. Save this receipt to database
        // 2. Optionally email it to patient
        // 3. Here we'll just show it

        TextArea receiptArea = new TextArea(receipt.toString());
        receiptArea.setEditable(false);
        receiptArea.setStyle("-fx-font-family: monospace;");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Payment Receipt");
        alert.setHeaderText("Receipt for Bill #" + bill.getBillNumber());
        alert.getDialogPane().setContent(receiptArea);
        alert.showAndWait();
    }



    @FXML
    public void loadAppointmentHistory() {
        String query = "SELECT a.appointment_id, a.appointment_date, a.status, " +
                "asl.start_time, asl.end_time, d.first_name AS doctor_name, a.reason " +
                "FROM appointments a " +
                "JOIN doctors d ON a.DoctorID = d.DoctorID " +
                "LEFT JOIN available_slots asl ON a.time_slot = asl.slot_id " +
                "WHERE a.patient_id = ?  and status = 'Scheduled'" +
                "ORDER BY a.status";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, currentPatient.getId());
            ResultSet rs = stmt.executeQuery();

            appointments.clear();

            while (rs.next()) {
                int id = rs.getInt("appointment_id");
                LocalDate date = rs.getDate("appointment_date").toLocalDate();
                String status = rs.getString("status");
                // Fetch and format the time slot
                LocalTime startTime = rs.getTime("start_time") != null ? rs.getTime("start_time").toLocalTime() : null;
                LocalTime endTime = rs.getTime("end_time") != null ? rs.getTime("end_time").toLocalTime() : null;
                String time = (startTime != null && endTime != null) ?
                        startTime.format(DateTimeFormatter.ofPattern("hh:mm a")) + " - " +
                                endTime.format(DateTimeFormatter.ofPattern("hh:mm a")) : "N/A";
                String doctor = rs.getString("doctor_name");
                String reason = rs.getString("reason") != null ? rs.getString("reason") : "Not specified";

                appointments.add(new Appointment(
                        id,
                        date.format(DateTimeFormatter.ISO_DATE),
                        time,
                        doctor,
                        status,
                        reason
                ));
            }

            appointmentsTable.setItems(appointments);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load appointment history: " + e.getMessage());
        }
    }

    private void loadDoctors() {
        String query = "SELECT DoctorID, first_name, specialization FROM doctors";
        doctorComboBox.getItems().clear();
        doctorIdMap.clear();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String doctorId = rs.getString("DoctorID");
                String name = rs.getString("first_name");
                String specialization = rs.getString("specialization");
                String displayText = name + " (" + specialization + ")";

                doctorComboBox.getItems().add(displayText);
                doctorIdMap.put(displayText, doctorId);
            }

            // Set default selection to the first doctor (if available)
            if (!doctorComboBox.getItems().isEmpty()) {
                doctorComboBox.setValue(doctorComboBox.getItems().get(0));
                System.out.println("[DEBUG] Default doctor selected: " + doctorComboBox.getValue());
            } else {
                System.out.println("[DEBUG] No doctors available to select.");
            }

        } catch (SQLException e) {
            showAlert("Error", "Failed to load doctors: " + e.getMessage());
        }
    }
    private void initializeDatePicker() {
        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(date.isBefore(LocalDate.now()));
            }
        });
        datePicker.setValue(LocalDate.now());
        System.out.println("[DEBUG] DatePicker initialized with default date: " + datePicker.getValue());
    }

    private final Map<String, Integer> timeStringToSlotIdMap = new HashMap<>();

    private void loadAvailableTimeSlots() {
        // Clear previous slots and the mapping
        timeSlotComboBox.getItems().clear();
        timeStringToSlotIdMap.clear();
        System.out.println("[DEBUG] Cleared time slots and map.");

        // Get selections
        String selectedDoctorDisplay = doctorComboBox.getValue();
        LocalDate selectedDate = datePicker.getValue();

        // Ensure both doctor and date are selected
        if (selectedDoctorDisplay == null || selectedDate == null) {
            System.out.println("[DEBUG] Doctor or Date not selected. Cannot load time slots.");
            timeSlotComboBox.setPromptText("Select Doctor & Date");
            return; // Exit if either is missing
        }

        // Get the actual DoctorID
        String doctorId = doctorIdMap.get(selectedDoctorDisplay);
        if (doctorId == null) {
            System.err.println("[ERROR] Could not find DoctorID for: " + selectedDoctorDisplay);
            showAlert("Error", "Internal error: Could not find selected doctor's ID.");
            return;
        }

        System.out.println("[DEBUG] Loading slots for DoctorID: " + doctorId + " on Date: " + selectedDate);

        // Query to get slots from available_slots that are NOT booked in appointments
        String query = "SELECT avs.slot_id, avs.start_time, avs.end_time " +
                "FROM available_slots avs " +
                "WHERE avs.DoctorID = ? AND avs.slot_date = ? " +
                "AND NOT EXISTS (" + // Use NOT EXISTS for potentially better performance than NOT IN
                "  SELECT 1 FROM appointments a " +
                "  WHERE a.time_slot = avs.slot_id " +
                "  AND a.status = 'Scheduled'" +
                ") " +
                "ORDER BY avs.start_time";

        try {
            // Ensure connection is valid
            if (connection == null || connection.isClosed()) {
                System.out.println("[DEBUG] Re-establishing database connection for loading slots.");
                connection = DatabaseHelper.connect();
            }

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, doctorId);
                stmt.setDate(2, java.sql.Date.valueOf(selectedDate)); // Use java.sql.Date

                System.out.println("[DEBUG] Executing query: " + stmt.toString());
                ResultSet rs = stmt.executeQuery();

                ObservableList<String> timeSlots = FXCollections.observableArrayList();
                int slotCount = 0;

                while (rs.next()) {
                    slotCount++;
                    int slotId = rs.getInt("slot_id");
                    LocalTime startTime = rs.getTime("start_time").toLocalTime();
                    LocalTime endTime = rs.getTime("end_time").toLocalTime();

                    // Format the time slot string for display
                    String formattedSlot = startTime +
                            " - " +
                            endTime;

                    timeSlots.add(formattedSlot);
                    timeStringToSlotIdMap.put(formattedSlot, slotId); // Store mapping

                    System.out.println("[DEBUG] Found available slot: ID=" + slotId + ", Time=" + formattedSlot);
                }

                System.out.println("[DEBUG] Total available slots found: " + slotCount);

                // Update ComboBox
                if (timeSlots.isEmpty()) {
                    timeSlotComboBox.setPromptText("No slots available");
                    timeSlotComboBox.getItems().clear(); // Ensure it's visually empty
                    // Optionally show an info alert, but prompt text might be sufficient
                    // showAlert("Information", "No available time slots for " + selectedDoctorDisplay + " on " +
                    //         selectedDate.format(DATE_FORMATTER_DISPLAY));
                } else {
                    timeSlotComboBox.setItems(timeSlots);
                    timeSlotComboBox.setPromptText("Select a Time Slot"); // Set prompt for when nothing is selected yet
                    System.out.println("[DEBUG] Time slots loaded into ComboBox.");
                }

            } // PreparedStatement and ResultSet try-with-resources

        } catch (SQLException e) {
            System.err.println("[ERROR] Database error loading available time slots:");
            e.printStackTrace();
            showAlert("Database Error", "Failed to load available time slots: " + e.getMessage());
        }
    }

    @FXML
    private void handleBookAppointment() {
        if (validateAppointmentForm()) {
            String selectedDoctor = doctorComboBox.getValue();
            String doctorId = doctorIdMap.get(selectedDoctor);
            String date = datePicker.getValue().format(DateTimeFormatter.ISO_DATE);
            String timeDisplay = timeSlotComboBox.getValue(); // This is the displayed string like "09:00 - 10:00"
            String reason = reasonTextArea.getText();

            // Get the actual slot_id from our mapping
            Integer slotId = timeStringToSlotIdMap.get(timeDisplay);
            if (slotId == null) {
                showAlert("Error", "Invalid time slot selection");
                return;
            }

            String query = "INSERT INTO appointments (patient_id, DoctorID, appointment_date, " +
                    "time_slot, status, reason) VALUES (?, ?, ?, ?, 'Scheduled', ?)";

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, currentPatient.getId());
                stmt.setString(2, doctorId);
                stmt.setString(3, date);
                stmt.setInt(4, slotId); // Now using the slot_id instead of the formatted string
                stmt.setString(5, reason);

                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    showAlert("Success", "Appointment booked successfully!");
                    clearAppointmentForm();
                    loadPatientData();

                    // Refresh the available slots to remove the booked one
                    loadAvailableTimeSlots();
                }
            } catch (SQLException e) {
                showAlert("Error", "Failed to book appointment: " + e.getMessage());
            }
        }
    }

    private void handleCancelAppointment(Appointment appointment) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Cancellation");
        confirmation.setHeaderText("Cancel Appointment");
        confirmation.setContentText("Are you sure you want to cancel appointment with " +
                appointment.getDoctor() + " on " + appointment.getDate() + "?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                System.out.println("[DEBUG] Attempting to cancel appointment ID: " + appointment.getAppointmentId());

                boolean success = cancelAppointment(appointment.getAppointmentId());

                if (success) {
                    System.out.println("[DEBUG] Cancellation successful");
                    showAlert("Success", "Appointment canceled successfully!");
                } else {
                    System.out.println("[DEBUG] No rows affected - appointment may not exist or already canceled");
                    showAlert("Information", "No changes made. Appointment was either already canceled or doesn't exist.");
                }
            } catch (SQLException e) {
                System.err.println("[ERROR] Database exception during cancellation:");
                e.printStackTrace();
                showAlert("Error", "Failed to cancel appointment: " + e.getMessage());
            }
        }
    }

    private boolean cancelAppointment(int appointmentId) throws SQLException {
        // More robust query that checks for schedulable status
        String sql = "UPDATE appointments SET status = 'Canceled' " +
                "WHERE appointment_id = ? AND status IN ('Scheduled', 'Pending') ";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);

            System.out.println("[DEBUG] Executing: " + pstmt.toString());
            int rowsAffected = pstmt.executeUpdate();

            return rowsAffected > 0;
        }
    }

    private boolean validateAppointmentForm() {
        if (doctorComboBox.getSelectionModel().isEmpty()) {
            showAlert("Error", "Please select a doctor");
            return false;
        }
        if (datePicker.getValue() == null) {
            showAlert("Error", "Please select a date");
            return false;
        }
        if (timeSlotComboBox.getSelectionModel().isEmpty()) {
            showAlert("Error", "Please select a time slot");
            return false;
        }
        if (reasonTextArea.getText().trim().isEmpty()) {
            showAlert("Error", "Please enter a reason for the appointment");
            return false;
        }
        return true;
    }

    private void clearAppointmentForm() {
        doctorComboBox.getSelectionModel().clearSelection();
        datePicker.setValue(LocalDate.now());
        timeSlotComboBox.getSelectionModel().clearSelection();
        reasonTextArea.clear();
    }

    @FXML private TextArea recordDetailsArea;  // Make sure this matches your FXML
    @FXML private TableColumn<MedicalRecord, String> prescriptionColumn;
    @FXML private TableColumn<MedicalRecord, String> testResultsColumn;

    @FXML
    private void loadMedicalRecords() {
        try {
            String query = "SELECT d.first_name AS doctor_name, " +
                    "mr.diagnosis, mr.prescription, mr.test_results " +
                    "FROM medicalrecords mr " +
                    "JOIN doctors d ON mr.DoctorID = d.DoctorID " +
                    "WHERE mr.patient_id = ? "+
                    "ORDER BY mr.record_date DESC";


            ObservableList<MedicalRecord> records = FXCollections.observableArrayList();

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, currentPatient.getId());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String doctor = rs.getString("doctor_name") != null ?
                            rs.getString("doctor_name") : "Unknown Doctor";
                    String diagnosis = rs.getString("diagnosis") != null ?
                            rs.getString("diagnosis") : "No diagnosis recorded";
                    String prescription = rs.getString("prescription") != null ?
                            rs.getString("prescription") : "No prescription";
                    String testResults = rs.getString("test_results") != null ?
                            rs.getString("test_results") : "No test results";

                    records.add(new MedicalRecord(
                            doctor,
                            diagnosis,
                            prescription,
                            testResults
                    ));
                }
            }

            medicalRecordsTable.setItems(records);

            // Add selection listener

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load medical records: " + e.getMessage());
        }
        }

    private void showRecordDetails(MedicalRecord record) {
        if (record != null) {
            detailedRecordView.setText(
                    "Appointment Date: " + record.getAppointmentDate() + "\n" +
                            "Doctor: " + record.getDoctorName() + "\n\n" +
                            "Diagnosis:\n" + record.getDiagnosis() + "\n\n" +
                            "Prescription:\n" + record.getPrescription() + "\n\n" +
                            "Test Results:\n" + record.getTestResults()
            );
        } else {
            detailedRecordView.clear();
        }
    }

    @FXML
    private void handleSignOut() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            switchToScene("patient_login.fxml");
        } catch (SQLException e) {
            showAlert("Error", "Failed to close database connection");
        }
    }

    private void switchToScene(String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);  // Or set to your preferred full-screen mode
        } catch (IOException e) {
            showAlert("Error", "Failed to load view: " + fxmlFile);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void showAppointmentHistory() {
        tabPane.getSelectionModel().select(historyTab);
        appointmentsTable.requestFocus();
    }

}
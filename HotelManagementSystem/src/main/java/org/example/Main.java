package org.example;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.text.ParseException;
import java.util.List;
import java.util.Scanner;

import static org.example.Receptionist.loginReceptionist;
import static org.example.Receptionist.registerReceptionist;
import static org.example.Room.readRoomsFromDatabase;

public class Main {
    public static void main(String[] args) throws ParseException {

        JFrame selectionFrame = new JFrame("Hotel Management System - User Selection");

        JLabel selectionLabel = new JLabel("Select User Type", SwingConstants.CENTER);
        JButton managerButton = new JButton("Manager");
        JButton receptionistButton = new JButton("Receptionist");

        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new GridLayout(3, 1, 10, 10));
        selectionPanel.add(selectionLabel);
        selectionPanel.add(managerButton);
        selectionPanel.add(receptionistButton);

        selectionFrame.add(selectionPanel);
        selectionFrame.setSize(400, 200);
        selectionFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        selectionFrame.setVisible(true);

        managerButton.addActionListener(e -> {
            selectionFrame.dispose();
            showManagerLogin();
        });

        receptionistButton.addActionListener(e -> {
            selectionFrame.dispose();
            showReceptionistLogin();
        });

    }


    //receptionist
    private static void showReceptionistLogin() {
        JFrame receptionistLoginFrame = new JFrame("Receptionist Login");

        JLabel label = new JLabel("Receptionist Login / Register", SwingConstants.CENTER);
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(usernameField);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        receptionistLoginFrame.add(label, BorderLayout.NORTH);
        receptionistLoginFrame.add(inputPanel, BorderLayout.CENTER);
        receptionistLoginFrame.add(buttonPanel, BorderLayout.SOUTH);

        receptionistLoginFrame.setSize(400, 200);
        receptionistLoginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        receptionistLoginFrame.setVisible(true);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            Receptionist receptionist = Receptionist.loginReceptionist(username, password);
            if (receptionist != null) {
                JOptionPane.showMessageDialog(receptionistLoginFrame, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                receptionistLoginFrame.dispose();
                createReceptionistDashboard(receptionist);
            } else {
                JOptionPane.showMessageDialog(receptionistLoginFrame, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            JTextField usernameFieldReg = new JTextField();
            JPasswordField passwordFieldReg = new JPasswordField();
            JTextField nameField = new JTextField();
            JTextField contactField = new JTextField();

            JPanel registerPanel = new JPanel(new GridLayout(5, 2));
            registerPanel.add(new JLabel("Name:"));
            registerPanel.add(nameField);
            registerPanel.add(new JLabel("Contact Info:"));
            registerPanel.add(contactField);
            registerPanel.add(new JLabel("Username:"));
            registerPanel.add(usernameFieldReg);
            registerPanel.add(new JLabel("Password:"));
            registerPanel.add(passwordFieldReg);

            int result = JOptionPane.showConfirmDialog(
                    receptionistLoginFrame,
                    registerPanel,
                    "Register Receptionist",
                    JOptionPane.OK_CANCEL_OPTION
            );

            if (result == JOptionPane.OK_OPTION) {
                String name = nameField.getText();
                String contact = contactField.getText();
                String username = usernameFieldReg.getText();
                String password = new String(passwordFieldReg.getPassword());

                try {
                    Receptionist.registerReceptionist(username, password, name, contact);
                    JOptionPane.showMessageDialog(receptionistLoginFrame, "Registration Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    showReceptionistLogin();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(receptionistLoginFrame, "Error during registration.", "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            }
        });

    }

    private static void createReceptionistDashboard(Receptionist receptionist) {
        JFrame frame = new JFrame("Receptionist Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 1, 10, 10));

        JButton manageResidentsButton = new JButton("Manage Residents");
        JButton calcCostButton = new JButton("Calculate Resident Cost");
        JButton addBookingButton = new JButton("Add Booking");
        JButton logoutButton = new JButton("Logout");

        panel.add(manageResidentsButton);
        panel.add(calcCostButton);
        panel.add(addBookingButton);
        panel.add(logoutButton);

        frame.add(panel);
        frame.setVisible(true);

        ArrayList<Resident> residentsList = Resident.readResidentsFromDatabase();
        ArrayList<Booking> bookingList = BookingHandler.readBookingsFromDatabase();
        ArrayList<Room> roomsList = Room.readRoomsFromDatabase();

        manageResidentsButton.addActionListener(e -> createManageResidentsForm(receptionist, residentsList, roomsList, bookingList));

        calcCostButton.addActionListener(e -> receptionist.createCalcCostForm(receptionist, bookingList, residentsList));

        addBookingButton.addActionListener(e -> {
            try {
                receptionist.addBooking(receptionist, roomsList, bookingList, residentsList);
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            }
        });

        logoutButton.addActionListener(e -> {
            frame.dispose();
            System.out.println("Logged out successfully.");
        });
    }

    private static void createManageResidentsForm(Receptionist receptionist, ArrayList<Resident> residentsList, ArrayList<Room> roomsList, ArrayList<Booking> bookingList) {
        JFrame manageFrame = new JFrame("Manage Residents");
        manageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        manageFrame.setSize(600, 400);

        JPanel managePanel = new JPanel(new GridLayout(6, 2, 10, 10));

        // Create form fields
        JTextField residentIdField = new JTextField();
        JTextField residentNameField = new JTextField();
        JTextField residentPhoneNumberField = new JTextField();

        // Add fields to the panel
        managePanel.add(new JLabel("Resident ID:"));
        managePanel.add(residentIdField);
        managePanel.add(new JLabel("Resident Name:"));
        managePanel.add(residentNameField);
        managePanel.add(new JLabel("Resident Phone Number:"));
        managePanel.add(residentPhoneNumberField);

        // Submit buttons
        JButton addButton = new JButton("Add Resident");
        JButton editButton = new JButton("Edit Resident");
        JButton deleteButton = new JButton("Delete Resident");
        JButton exitButton = new JButton("Exit");

        managePanel.add(addButton);
        managePanel.add(editButton);
        managePanel.add(deleteButton);
        managePanel.add(exitButton);

        manageFrame.add(managePanel);
        manageFrame.setVisible(true);

        addButton.addActionListener(e -> {
            try {
                String name = residentNameField.getText();
                String phoneNumber = residentPhoneNumberField.getText();
                if (!name.isEmpty() && !phoneNumber.isEmpty()) {
                    receptionist.addResidentGUI(residentsList, name, phoneNumber);
                    JOptionPane.showMessageDialog(manageFrame, "Resident added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(manageFrame, "Name and phone number cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(manageFrame, "Failed to add resident. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        deleteButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(residentIdField.getText());
                receptionist.deleteResidentGUI(residentsList, id);
                JOptionPane.showMessageDialog(manageFrame, "Resident deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(manageFrame, "Failed to delete resident. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        editButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(residentIdField.getText());
                Resident resident = receptionist.findResidentById(residentsList, id);
                if (resident == null) {
                    JOptionPane.showMessageDialog(manageFrame, "Resident with ID " + id + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String[] options = {"Personal Info", "Booking Details"};
                int choice = JOptionPane.showOptionDialog(manageFrame, "What do you want to change?", "Edit Resident",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

                switch (choice) {
                    case 0: // Personal Info
                        editPersonalInfoGUI(receptionist, resident, manageFrame);
                        break;
                    case 1: // Booking Details
                        editBookingDetailsGUI(receptionist, resident, bookingList, roomsList, manageFrame);
                        break;
                    default:
                        JOptionPane.showMessageDialog(manageFrame, "Invalid choice.", "Error", JOptionPane.ERROR_MESSAGE);
                        break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(manageFrame, "Failed to edit resident. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        exitButton.addActionListener(e -> manageFrame.dispose());
    }

    private static void editPersonalInfoGUI(Receptionist receptionist, Resident resident, JFrame parentFrame) {
        JFrame editFrame = new JFrame("Edit Personal Info");
        editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editFrame.setSize(400, 300);

        JPanel editPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        JTextField newNameField = new JTextField();
        JTextField newPhoneNumberField = new JTextField();

        editPanel.add(new JLabel("New Name:"));
        editPanel.add(newNameField);
        editPanel.add(new JLabel("New Phone Number:"));
        editPanel.add(newPhoneNumberField);

        JButton saveButton = new JButton("Save");
        editPanel.add(new JLabel(""));
        editPanel.add(saveButton);

        editFrame.add(editPanel);
        editFrame.setVisible(true);

        saveButton.addActionListener(e -> {
            String newName = newNameField.getText();
            String newPhoneNumber = newPhoneNumberField.getText();
            try {
                receptionist.editPersonalInfoGUI(resident, newName, newPhoneNumber);
                JOptionPane.showMessageDialog(editFrame, "Resident details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                editFrame.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(editFrame, "Failed to update resident details. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void editBookingDetailsGUI(Receptionist receptionist, Resident resident, ArrayList<Booking> bookingsList, ArrayList<Room> roomsList, JFrame parentFrame) {
        JFrame editFrame = new JFrame("Edit Booking Details");
        editFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editFrame.setSize(400, 300);

        JPanel editPanel = new JPanel(new GridLayout(5, 2, 10, 10));

        JTextField newRoomTypeField = new JTextField();
        JTextField newBoardTypeField = new JTextField();
        JTextField newCheckInField = new JTextField();
        JTextField newCheckOutField = new JTextField();

        editPanel.add(new JLabel("New Room Type:"));
        editPanel.add(newRoomTypeField);
        editPanel.add(new JLabel("New Board Type:"));
        editPanel.add(newBoardTypeField);
        editPanel.add(new JLabel("New Check-In Date:"));
        editPanel.add(newCheckInField);
        editPanel.add(new JLabel("New Check-Out Date:"));
        editPanel.add(newCheckOutField);

        JButton saveButton = new JButton("Save");
        editPanel.add(new JLabel(""));
        editPanel.add(saveButton);

        editFrame.add(editPanel);
        editFrame.setVisible(true);

        saveButton.addActionListener(e -> {
            String newRoomType = newRoomTypeField.getText();
            String newBoardType = newBoardTypeField.getText();
            String newCheckInDate = newCheckInField.getText();
            String newCheckOutDate = newCheckOutField.getText();
            try {
                receptionist.editBookingDetailsGUI(resident, bookingsList, roomsList, newRoomType, newBoardType, newCheckInDate, newCheckOutDate);
                JOptionPane.showMessageDialog(editFrame, "Booking details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                editFrame.dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(editFrame, "Failed to update booking details. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    //manager

    private static void showManagerLogin() {
        JFrame managerLoginFrame = new JFrame("Manager Login");

        JLabel label = new JLabel("Manager Login", SwingConstants.CENTER);
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton recoveryButton = new JButton("Recover Password");

        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.add(new JLabel("Username:"));
        inputPanel.add(usernameField);
        inputPanel.add(new JLabel("Password:"));
        inputPanel.add(passwordField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(loginButton);
        buttonPanel.add(recoveryButton);

        managerLoginFrame.add(label, BorderLayout.NORTH);
        managerLoginFrame.add(inputPanel, BorderLayout.CENTER);
        managerLoginFrame.add(buttonPanel, BorderLayout.SOUTH);

        managerLoginFrame.setSize(400, 200);
        managerLoginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        managerLoginFrame.setVisible(true);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputUsername = usernameField.getText();
                String inputPassword = new String(passwordField.getPassword());

                Manager manager = Manager.getmanager();
                if (manager.validateLogin(inputUsername, inputPassword)) {
                    JOptionPane.showMessageDialog(managerLoginFrame, "Login Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    managerLoginFrame.dispose();
                    showDashboard(manager);
                } else {
                    int choice = JOptionPane.showConfirmDialog(
                            managerLoginFrame,
                            "Invalid Username or Password. Would you like to recover your password?",
                            "Login Failed",
                            JOptionPane.YES_NO_OPTION
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        manager.recoverPassword(managerLoginFrame);
                    }
                }
            }
        });

        recoveryButton.addActionListener(e -> {
            String adminEmail = JOptionPane.showInputDialog(managerLoginFrame, "Enter registered email for password recovery:");
            if (adminEmail != null && !adminEmail.isEmpty()) {
                PasswordRecoveryService passwordRecoveryService = new PasswordRecoveryProxy();
                passwordRecoveryService.recoverPassword(adminEmail);
            }
        });
    }

    private static void showDashboard(Manager manager) {
        JFrame dashboardFrame = new JFrame("Manager Dashboard");
        dashboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboardFrame.setSize(500, 300);
        dashboardFrame.setLayout(new GridLayout(4, 1, 10, 10));  // Adjusted for 4 buttons

        JButton viewResidentsButton = new JButton("View Resident Information");
        JButton trackIncomeButton = new JButton("Track Hotel Income");
        JButton roomStatusButton = new JButton("Room Status Monitoring");
        JButton viewWorkersButton = new JButton("View Worker Details");
        JButton manageWorkersButton = new JButton("Manage Workers");
        ArrayList<Worker> workersList = Manager.getWorkersList();

        manageWorkersButton.addActionListener(e -> {

            showWorkerManagementWindow(manager);

        });
        dashboardFrame.add(manageWorkersButton);


        viewResidentsButton.addActionListener(e -> {

            ArrayList<Resident> residentList = Resident.readResidentsFromDatabase();
            ArrayList<Booking> bookingsList = BookingHandler.readBookingsFromDatabase();

            // Call the viewResidents method from the Manager class to display in a GUI
            manager.viewResidents(residentList, bookingsList);
        });

        // View Worker Details
        viewWorkersButton.addActionListener(e -> {
            showWorkerDetails(workersList, dashboardFrame);

        });

        // Room Status Monitoring
        roomStatusButton.addActionListener(e -> {
            ArrayList<Room> roomList = Room.readRoomsFromDatabase(); // Replace with actual method to fetch rooms
            manager.roomMonitoring(roomList); // Call the method from Manager class
        });

        // Track Hotel Income
        JComboBox<String> timeFrameComboBox = new JComboBox<>(new String[]{"Weekly", "Monthly", "Yearly"});
        timeFrameComboBox.setSelectedIndex(0); // Default selection

        trackIncomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create a panel with ComboBox and an action button
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                JLabel label = new JLabel("Select Income Period:");
                panel.add(label);
                panel.add(timeFrameComboBox);  // Add ComboBox to panel

                // Button to confirm income calculation
                JButton calculateIncomeButton = new JButton("Calculate Income");
                panel.add(calculateIncomeButton);

                // Show the panel in a dialog
                int option = JOptionPane.showConfirmDialog(dashboardFrame, panel, "Choose Time Period",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                // If the user selects OK (confirms the action)
                if (option == JOptionPane.OK_OPTION) {
                    ArrayList<Booking> bookingsList = BookingHandler.readBookingsFromDatabase(); // Fetch bookings from the database
                    String selectedTimeFrame = (String) timeFrameComboBox.getSelectedItem();
                    double totalIncome = 0;

                    // Calculate income based on the selected time frame
                    if ("Weekly".equals(selectedTimeFrame)) {
                        totalIncome = manager.getTotalIncomeForWeek(bookingsList);
                    } else if ("Monthly".equals(selectedTimeFrame)) {
                        totalIncome = manager.getTotalIncomeForMonth(bookingsList);
                    } else if ("Yearly".equals(selectedTimeFrame)) {
                        totalIncome = manager.getTotalIncomeForYear(bookingsList);
                    }

                    // Display the income information
                    JOptionPane.showMessageDialog(dashboardFrame, "Hotel Income (" + selectedTimeFrame + "):\n" +
                                    "Total Income: $" + totalIncome,
                            "Hotel Income Report", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        });

        // Add buttons to the dashboard frame
        dashboardFrame.add(viewResidentsButton);
        dashboardFrame.add(trackIncomeButton);
        dashboardFrame.add(roomStatusButton);
        dashboardFrame.add(viewWorkersButton);  // Add the new button for worker details

        dashboardFrame.setVisible(true);
    }

    private static void showWorkerManagementWindow(Manager manager) {
        JFrame workerManagementFrame = new JFrame("Manage Workers");
        workerManagementFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        workerManagementFrame.setSize(400, 300);
        workerManagementFrame.setLayout(new GridLayout(4, 1, 10, 10));

        // Add Button
        JButton addWorkerButton = new JButton("Add Worker");
        addWorkerButton.addActionListener(e -> {
            // Open Add Worker dialog
            showAddWorkerDialog(manager);
        });

        // Edit Button
        JButton editWorkerButton = new JButton("Edit Worker");
        editWorkerButton.addActionListener(e -> {
            // Open Edit Worker dialog
            showEditWorkerDialog(manager);
        });

        // Delete Button
        JButton deleteWorkerButton = new JButton("Delete Worker");
        deleteWorkerButton.addActionListener(e -> {
            // Open Delete Worker dialog
            showDeleteWorkerDialog(manager);
        });


        // Add buttons to the frame
        workerManagementFrame.add(addWorkerButton);
        workerManagementFrame.add(editWorkerButton);
        workerManagementFrame.add(deleteWorkerButton);
        workerManagementFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                manager.saveWorkers(); // Save workers to the database
            }
        });

        workerManagementFrame.setVisible(true);
    }

    private static void showAddWorkerDialog(Manager manager) {
        // Create a panel with form fields
        JPanel panel = new JPanel(new GridLayout(5, 2));
        JTextField nameField = new JTextField();
        JTextField contactInfoField = new JTextField();
        JTextField salaryField = new JTextField();
        JTextField jobTitleField = new JTextField();
        JButton addButton = new JButton("Add Worker");

        // Add labels and text fields to the panel
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Contact Info:"));
        panel.add(contactInfoField);
        panel.add(new JLabel("Salary:"));
        panel.add(salaryField);
        panel.add(new JLabel("Job Title:"));
        panel.add(jobTitleField);
        panel.add(addButton);

        // Action listener for the Add button
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String contactInfo = contactInfoField.getText();
            double salary = Double.parseDouble(salaryField.getText());
            String jobTitle = jobTitleField.getText();

            // Add the worker to the manager
            manager.addWorker(name, contactInfo, salary, jobTitle);

            // Inform the user the worker was added
            JOptionPane.showMessageDialog(panel, "Worker Added Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Optionally, clear fields after adding the worker
            nameField.setText("");
            contactInfoField.setText("");
            salaryField.setText("");
            jobTitleField.setText("");
        });

        // Show the dialog as a confirmation panel
        JOptionPane.showOptionDialog(null, panel, "Add Worker", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
    }

    private static void showEditWorkerDialog(Manager manager) {
        // Create a panel with form fields
        JPanel panel = new JPanel(new GridLayout(6, 2));
        JTextField workerIdField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField contactInfoField = new JTextField();
        JTextField salaryField = new JTextField();
        JTextField jobTitleField = new JTextField();
        JButton editButton = new JButton("Edit Worker");

        // Add labels and text fields to the panel
        panel.add(new JLabel("Enter Worker ID:"));
        panel.add(workerIdField);
        panel.add(new JLabel("New Name (Leave empty to skip):"));
        panel.add(nameField);
        panel.add(new JLabel("New Contact Info (Leave empty to skip):"));
        panel.add(contactInfoField);
        panel.add(new JLabel("New Salary (Leave empty to skip):"));
        panel.add(salaryField);
        panel.add(new JLabel("New Job Title (Leave empty to skip):"));
        panel.add(jobTitleField);
        panel.add(editButton);

        // Action listener for the Edit button
        editButton.addActionListener(e -> {
            try {
                int workerId = Integer.parseInt(workerIdField.getText());
                String name = nameField.getText();
                String contactInfo = contactInfoField.getText();
                Double salary = salaryField.getText().isEmpty() ? null : Double.parseDouble(salaryField.getText());
                String jobTitle = jobTitleField.getText();

                // Edit the worker's details
                manager.editWorker(workerId, name, contactInfo, salary, jobTitle);

                // Inform the user that the worker was edited
                JOptionPane.showMessageDialog(panel, "Worker Updated Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Optionally, clear fields after editing the worker
                workerIdField.setText("");
                nameField.setText("");
                contactInfoField.setText("");
                salaryField.setText("");
                jobTitleField.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid input. Please check the fields.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Worker not found or error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Show the dialog as a confirmation panel
        JOptionPane.showOptionDialog(null, panel, "Edit Worker", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
    }

    private static void showDeleteWorkerDialog(Manager manager) {
        // Create a panel with form fields
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField workerIdField = new JTextField();
        JButton deleteButton = new JButton("Delete Worker");

        // Add labels and text fields to the panel
        panel.add(new JLabel("Enter Worker ID to Delete:"));
        panel.add(workerIdField);
        panel.add(deleteButton);

        // Action listener for the Delete button
        deleteButton.addActionListener(e -> {
            try {
                int workerId = Integer.parseInt(workerIdField.getText());

                // Call the manager's deleteWorker method
                manager.deleteWorker(workerId);

                // Inform the user that the worker was deleted
                JOptionPane.showMessageDialog(panel, "Worker Deleted Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

                // Optionally, clear the field after deletion
                workerIdField.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "Invalid Worker ID. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error deleting worker. Worker may not exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Show the dialog as a confirmation panel
        JOptionPane.showOptionDialog(null, panel, "Delete Worker", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, new Object[]{}, null);
    }

    private static void showWorkerDetails(ArrayList<Worker> workersList, JFrame parentFrame) {
        // Create a new JFrame to display worker details
        JFrame workerDetailsFrame = new JFrame("Worker Details");
        workerDetailsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        workerDetailsFrame.setSize(600, 400);
        workerDetailsFrame.setLayout(new BorderLayout());

        // Define the column names for the table
        String[] columns = {"Worker ID", "Name", "Contact Info", "Salary", "Job Title"};

        // Create a 2D array to hold the worker data
        String[][] data = new String[workersList.size()][columns.length];

        // Populate the data array with worker details
        for (int i = 0; i < workersList.size(); i++) {
            Worker worker = workersList.get(i);
            data[i][0] = String.valueOf(worker.getWorkerId());
            data[i][1] = worker.getName();
            data[i][2] = worker.getContactInfo();
            data[i][3] = String.valueOf(worker.getSalary());
            data[i][4] = worker.getJobTitle();
        }

        // Create a JTable to display the worker data
        JTable workerTable = new JTable(data, columns);

        // Make the table scrollable
        JScrollPane tableScrollPane = new JScrollPane(workerTable);

        // Add the table to the frame
        workerDetailsFrame.add(tableScrollPane, BorderLayout.CENTER);

        // Display the worker details frame
        workerDetailsFrame.setVisible(true);
    }
}














package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Manager {
    public static ArrayList<Worker> workersList;

    // Database connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/hotel";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "samanour";

    private String username;
    private String password;
    private String registeredEmail;

    private static Manager admin = new Manager();
    static {
        try {
            workersList = Worker.loadWorkersFromDatabase();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    private Manager() {
        loadManagerData();

    }

    // Singleton instance
    public static Manager getmanager() {
        return admin;
    }
    public static ArrayList<Worker> getWorkersList() {
        return workersList;
    }

    // Validate login credentials
    public boolean validateLogin(String inputUsername, String inputPassword) {
        if (inputUsername.equals(username) && inputPassword.equals(password)) {
            return true;
        } else {
            System.out.println("Invalid credentials.");
            return false;
        }
    }

    public boolean recoverPassword(JFrame parentFrame) {
        String inputEmail = JOptionPane.showInputDialog(parentFrame, "Enter your registered email for password recovery:");

        if (inputEmail != null && inputEmail.equals(registeredEmail)) {
            String newPassword = JOptionPane.showInputDialog(parentFrame, "Enter your new password:");
            if (newPassword != null && !newPassword.isEmpty()) {
                setPassword(newPassword);
                JOptionPane.showMessageDialog(parentFrame, "Password successfully updated!", "Success", JOptionPane.INFORMATION_MESSAGE);
                return true;
            } else {
                JOptionPane.showMessageDialog(parentFrame, "Password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(parentFrame, "Email does not match our records.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }

    // Set a new password and save it to the database
    public void setPassword(String newPassword) {
        this.password = newPassword;
        updatePasswordInDatabase(newPassword);
    }

    // Load manager details from the database
    private void loadManagerData() {
        String query = "SELECT username, password, email FROM Manager WHERE id = 1";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                this.username = rs.getString("username");
                this.password = rs.getString("password");
                this.registeredEmail = rs.getString("email");
            } else {
                System.err.println("No manager data found in the database.");
            }
        } catch (SQLException e) {
            System.err.println("Error loading manager data: " + e.getMessage());
        }
    }

    // Update password in the database
    private void updatePasswordInDatabase(String newPassword) {
        String updateQuery = "UPDATE Manager SET password = ? WHERE id = 1";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setString(1, newPassword);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
        }
    }


    public void manageWorkers() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Choose an option:");
            System.out.println("1. Add Worker");
            System.out.println("2. Edit Worker");
            System.out.println("3. Delete Worker");
            System.out.println("4. Exit");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.print("Enter Name: ");
                    String name = scanner.nextLine();
                    System.out.print("Enter Contact Info: ");
                    String contactInfo = scanner.nextLine();
                    System.out.print("Enter Salary: ");
                    double salary = scanner.nextDouble();
                    scanner.nextLine();
                    System.out.print("Enter Job Title: ");
                    String jobTitle = scanner.nextLine();
                    addWorker(name, contactInfo, salary, jobTitle);
                    break;
                case 2:
                    System.out.print("Enter Worker ID to Edit: ");
                    int workerIdToEdit = scanner.nextInt();
                    scanner.nextLine();


                    System.out.print("Enter New Name (or press Enter to skip): ");
                    String newName = scanner.nextLine();
                    System.out.print("Enter New Contact Info (or press Enter to skip): ");
                    String newContactInfo = scanner.nextLine();
                    System.out.print("Enter New Salary (or press Enter to skip): ");
                    String salaryStr = scanner.nextLine();
                    Double newSalary = salaryStr.isEmpty() ? null : Double.parseDouble(salaryStr);
                    System.out.print("Enter New Job Title (or press Enter to skip): ");
                    String newJobTitle = scanner.nextLine();
                    editWorker(workerIdToEdit, newName, newContactInfo, newSalary, newJobTitle);
                    break;
                case 3:
                    System.out.print("Enter Worker ID to Delete: ");
                    int workerIdToDelete = scanner.nextInt();
                    scanner.nextLine();
                    deleteWorker(workerIdToDelete);
                    break;
                case 4:
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice. Please select a valid option.");
            }
        }
    }

    public void addWorker(String name, String contactInfo, double salary, String jobTitle) {
        Worker newWorker = new Worker(name, contactInfo, salary, jobTitle);
        workersList.add(newWorker);
    }

    public void deleteWorker(int workerId) {
        int removedIndex = -1;
        for (int i = 0; i < workersList.size(); i++) {
            if (workersList.get(i).workerId == workerId) {
                removedIndex = i;
                System.out.println("Worker found at index: " + i);
                break;
            }
        }
        if (removedIndex != -1) {
            workersList.remove(removedIndex);
            System.out.println("Worker deleted!");
        } else {
            System.out.println("Worker with ID " + workerId + " not found.");
        }
    }

    public void editWorker(int workerId, String name, String contactInfo, Double salary, String jobTitle) {
        boolean found = false;
        for (Worker worker : workersList) {
            if (worker.workerId == workerId) {
                found = true;
                if (!name.isEmpty()) { // Only update if a new name is provided
                    worker.setName(name);
                }
                if (!contactInfo.isEmpty()) { // Only update if new contact info is provided
                    worker.setContactInfo(contactInfo);
                }
                if (salary != null) { // Only update if a new salary is provided
                    worker.setSalary(salary);
                }
                if (!jobTitle.isEmpty()) { // Only update if a new job title is provided
                    worker.setJobTitle(jobTitle);
                }
                System.out.println("Worker updated successfully!");
                break;
            }
        }
        if (!found) {
            System.out.println("Worker with ID " + workerId + " not found.");
        }
    }


    public static void saveWorkers() {
        try {
            Worker.saveWorkersToDatabase(workersList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void viewWorkers() {
        System.out.println("** Worker List **");
        for (Worker worker : workersList) {
            System.out.println("ID: " + worker.workerId);
            System.out.println("Name: " + worker.name);
            System.out.println("Contact Info: " + worker.contactInfo);
            System.out.println("Salary: " + worker.salary);
            System.out.println("Job Title: " + worker.jobTitle);
            System.out.println("---------------");
        }
    }
    public void viewResidents(ArrayList<Resident> residentList, ArrayList<Booking> bookingsList) {
        // Create a frame to display the resident information
        JFrame residentFrame = new JFrame("Resident Information");
        residentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        residentFrame.setSize(600, 400);

        // Create a JTextArea to display the resident and booking information
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false); // Make the text area non-editable
        textArea.setLineWrap(true);  // Wrap lines if they are too long
        textArea.setWrapStyleWord(true); // Wrap at word boundaries
        JScrollPane scrollPane = new JScrollPane(textArea); // Add the text area to a scroll pane

        // Format the text to show the resident and their booking information
        StringBuilder sb = new StringBuilder();
        for (Resident resident : residentList) {
            sb.append("Resident ID: ").append(resident.getResidentId()).append("\n");
            sb.append("Name: ").append(resident.getName()).append("\n");
            sb.append("Phone Number: ").append(resident.getPhoneNumber()).append("\n");

            boolean hasBookings = false;
            for (Booking booking : bookingsList) {
                if (resident.getResidentId() == booking.getCustomerId()) {
                    sb.append("Booking: ").append(booking.getDescription()).append("\n");
                    hasBookings = true;
                }
            }

            if (!hasBookings) {
                sb.append("No bookings found.\n");
            }

            sb.append("---------------\n");
        }

        // Set the formatted text in the JTextArea
        textArea.setText(sb.toString());

        // Add the scroll pane containing the text area to the frame
        residentFrame.add(scrollPane, BorderLayout.CENTER);
        residentFrame.setVisible(true);
    }

    public static void viewRooms(ArrayList<Room> roomsList) {
        if (roomsList.isEmpty()) {
            System.out.println("No rooms available.");
        } else {
            System.out.println("Room Information:");
            for (Room room : roomsList) {
                System.out.println(room);
            }
        }
    }
    public double getTotalIncomeForWeek(ArrayList<Booking> bookingsList) {
        return calculateTotalIncome(new Date(), 7,bookingsList); // 7 days for a week
    }

    // Calculate total income for the past month
    public double getTotalIncomeForMonth(ArrayList<Booking> bookingsList) {
        return calculateTotalIncome(new Date(), 30,bookingsList); // 30 days for a month
    }

    // Calculate total income for the past year
    public double getTotalIncomeForYear(ArrayList<Booking> bookingsList) {
        return calculateTotalIncome(new Date(), 365,bookingsList); // 365 days for a year
    }

    // Calculate total income based on the reference date and time frame
    private double calculateTotalIncome(Date referenceDate, int daysInTimeFrame,ArrayList<Booking> bookingsList) {
        double totalIncome = 0.0;
        for (Booking booking : bookingsList) {
            if (isBookingInTimeFrame(booking, referenceDate, daysInTimeFrame)) {
                totalIncome += booking.getCost();
            }
        }
        return totalIncome;
    }


     boolean isBookingInTimeFrame(Booking booking, Date referenceDate, int daysInTimeFrame) {
        Date checkInDate = booking.getCheckIn();

        // Calculate the difference in days between the reference date and check-in date
        long differenceInMillis = referenceDate.getTime() - checkInDate.getTime();
        long differenceInDays = TimeUnit.MILLISECONDS.toDays(differenceInMillis);

        // Debug output to track the difference
        System.out.println("Booking Check-In: " + checkInDate + ", Reference Date: " + referenceDate);
        System.out.println("Difference in Days: " + differenceInDays);

        return differenceInDays >= 0 && differenceInDays <= daysInTimeFrame;}

    public String getRegisteredEmail() {
        return registeredEmail;
    }

    public void roomMonitoring(ArrayList<Room> roomList) {
        String[] options = {"All rooms", "Single rooms", "Double rooms", "Triple rooms"};
        JComboBox<String> roomTypeComboBox = new JComboBox<>(options);
        JButton viewButton = new JButton("View");
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);

        JPanel panel = new JPanel();
        panel.add(new JLabel("View rooms by:"));
        panel.add(roomTypeComboBox);
        panel.add(viewButton);

        // Create a window to display the room options
        JFrame frame = new JFrame("Room Status Monitoring");
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.NORTH);
        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        viewButton.addActionListener(e -> {
            String selectedOption = (String) roomTypeComboBox.getSelectedItem();
            textArea.setText(""); // Clear previous results

            switch (selectedOption) {
                case "All rooms":
                    for (Room room : roomList) {
                        textArea.append("Room ID: " + room.RoomId + ", Type: " + room.roomType + ", Availability: " + (room.isAvailable ? "Yes" : "No") + "\n");
                    }
                    break;

                case "Single rooms":
                    for (Room room : roomList) {
                        if (room.roomType.equals("Single")) {
                            textArea.append("Room ID: " + room.RoomId + ", Availability: " + (room.isAvailable ? "Yes" : "No") + "\n");
                        }
                    }
                    break;

                case "Double rooms":
                    for (Room room : roomList) {
                        if (room.roomType.equals("Double")) {
                            textArea.append("Room ID: " + room.RoomId + ", Availability: " + (room.isAvailable ? "Yes" : "No") + "\n");
                        }
                    }
                    break;

                case "Triple rooms":
                    for (Room room : roomList) {
                        if (room.roomType.equals("Triple")) {
                            textArea.append("Room ID: " + room.RoomId + ", Availability: " + (room.isAvailable ? "Yes" : "No") + "\n");
                        }
                    }
                    break;

                default:
                    textArea.append("Invalid selection.\n");
                    break;
            }

            // Display message if no rooms match the criteria
            if (textArea.getText().isEmpty()) {
                textArea.setText("No rooms match the selected criteria.");
            }
        });

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
    }

}
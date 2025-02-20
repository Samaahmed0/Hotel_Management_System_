package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.text.ParseException;

public class Receptionist extends Worker {

    String receptionistUserName;
    String receptionistPassword;

    Scanner scanner = new Scanner(System.in);
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public Receptionist(int id, String name, String contactInfo, String username, String password) {
        super(id, name, contactInfo, 40000, "receptionist");
        receptionistUserName = username;
        receptionistPassword = password;
    }


    public static void registerReceptionist(String username, String password, String name, String contactInfo) throws SQLException {
        String sql = "INSERT INTO worker (name, contact_info, salary, job_title, username, password) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotel", "root", "samanour");
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, name);
            preparedStatement.setString(2, contactInfo);
            preparedStatement.setDouble(3, 40000);
            preparedStatement.setString(4, "receptionist");
            preparedStatement.setString(5, username);
            preparedStatement.setString(6, password);
            preparedStatement.executeUpdate();
            System.out.println("Receptionist registered successfully.");
            Worker newReceptionist = new Worker(name, contactInfo, 40000, "receptionist");
            Manager.workersList.add(newReceptionist);


        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error registering receptionist.");
        }
    }

    public static Receptionist loginReceptionist(String username, String password) {
        String sql = "SELECT * FROM worker WHERE username = ? AND password = ? AND job_title = 'receptionist'";

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotel", "root", "samanour");
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String contactInfo = resultSet.getString("contact_info");
                double salary = resultSet.getDouble("salary");

                System.out.println("Login successful!");

                // Create and return a Receptionist object
                return new Receptionist(id, name, contactInfo, username, password);

            } else {
                System.out.println("Invalid username or password, or you are not a receptionist.");
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error during login.");
            return null;
        }
    }


    void addResident(ArrayList<Resident> residentsList, ArrayList<Room> roomsList, ArrayList<Booking> bookingList) throws ParseException {
        boolean addMoreResidents = true;
        while (addMoreResidents) {
            System.out.print("Enter resident name: ");
            String name = scanner.nextLine();
            System.out.print("Enter resident phone number: ");
            String phone_number = scanner.nextLine();
            Resident r = new Resident(name, phone_number);
            residentsList.add(r);
            Resident.writeResidentsToDatabase(residentsList);
            System.out.print("Do you want to add another resident? (y/n): ");
            String addAnotherResident = scanner.nextLine();
            if (!addAnotherResident.equalsIgnoreCase("y")) {
                addMoreResidents = false;
            }
        }

    }


    public void addBooking(Receptionist receptionist, ArrayList<Room> roomsList, ArrayList<Booking> bookingList, ArrayList<Resident> residentsList) throws ParseException {
        JFrame bookingFrame = new JFrame("Add Booking");
        bookingFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        bookingFrame.setSize(500, 400);

        JPanel bookingPanel = new JPanel(new GridLayout(9, 2, 10, 10));

        // Create form fields
        JTextField residentIdField = new JTextField();
        JTextField roomTypeField = new JTextField();
        JTextField boardTypeField = new JTextField();
        JTextField checkInDateField = new JTextField();
        JTextField checkOutDateField = new JTextField();
        JCheckBox spaCheckBox = new JCheckBox("Add Spa");
        JCheckBox wifiCheckBox = new JCheckBox("Add Wifi");
        JCheckBox parkingCheckBox = new JCheckBox("Add Parking");

        // Add fields to the panel
        bookingPanel.add(new JLabel("Resident ID:"));
        bookingPanel.add(residentIdField);
        bookingPanel.add(new JLabel("Room Type:"));
        bookingPanel.add(roomTypeField);
        bookingPanel.add(new JLabel("Board Type:"));
        bookingPanel.add(boardTypeField);
        bookingPanel.add(new JLabel("Check-In Date (dd/MM/yyyy):"));
        bookingPanel.add(checkInDateField);
        bookingPanel.add(new JLabel("Check-Out Date (dd/MM/yyyy):"));
        bookingPanel.add(checkOutDateField);
        bookingPanel.add(new JLabel(""));
        bookingPanel.add(spaCheckBox);
        bookingPanel.add(new JLabel(""));
        bookingPanel.add(wifiCheckBox);
        bookingPanel.add(new JLabel(""));
        bookingPanel.add(parkingCheckBox);

        // Submit button
        JButton submitButton = new JButton("Add Booking");
        bookingPanel.add(new JLabel(""));
        bookingPanel.add(submitButton);

        bookingFrame.add(bookingPanel);
        bookingFrame.setVisible(true);

        submitButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(residentIdField.getText());
                Resident r = receptionist.findResidentById(residentsList, id);
                if (r == null) {
                    JOptionPane.showMessageDialog(bookingFrame, "Resident with ID " + id + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String type = roomTypeField.getText();
                int roomId = receptionist.roomAssigment(roomsList, type);
                if (roomId != -1) {
                    String boardType = boardTypeField.getText();
                    Date checkInDate = receptionist.dateFormat.parse(checkInDateField.getText());
                    Date checkOutDate = receptionist.dateFormat.parse(checkOutDateField.getText());

                    Booking baseBooking = new BookingBuilder()
                            .setCustomerId(r.residentId)
                            .setRoomId(roomId)
                            .setRoomType(type)
                            .setBoardType(boardType)
                            .setCheckIn(checkInDate)
                            .setCheckOut(checkOutDate)
                            .setbaseCost()
                            .build();

                    if (spaCheckBox.isSelected()) {
                        baseBooking = new SpaDecorator(baseBooking);
                    }
                    if (wifiCheckBox.isSelected()) {
                        baseBooking = new WiFiDecorator(baseBooking);
                    }
                    if (parkingCheckBox.isSelected()) {
                        baseBooking = new ParkingDecorator(baseBooking);
                    }

                    JOptionPane.showMessageDialog(bookingFrame, baseBooking.getDescription() + "\nTotal Cost: $" + baseBooking.getCost());

                    bookingList.add(baseBooking);
                    BookingHandler.writeBookingsToDatabase(bookingList); // Save the booking to the database

                    JOptionPane.showMessageDialog(bookingFrame, "Booking added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    bookingFrame.dispose(); // Close the booking form

                } else {
                    JOptionPane.showMessageDialog(bookingFrame, "No rooms are available.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(bookingFrame, "Failed to add booking. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public int roomAssigment(ArrayList<Room> roomsList, String type) {
        ArrayList<Integer> availableRooms = new ArrayList<>();
        int roomId = 1;
        for (Room room : roomsList) {
            if (room.isAvailable && room.roomType.equals(type)) {
                availableRooms.add(room.RoomId);
            }
        }
        if (availableRooms.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No rooms are available.", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            boolean validRoom = false;

            do {
                String roomIdStr = JOptionPane.showInputDialog(null, "Choose the room id: " + availableRooms);
                roomId = Integer.parseInt(roomIdStr);

                if (availableRooms.contains(roomId)) {
                    validRoom = true;
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid room ID. Please choose a valid room ID from the list: " + availableRooms, "Error", JOptionPane.ERROR_MESSAGE);
                }
            } while (!validRoom);

            for (Room room : roomsList) {
                if (room.RoomId == roomId) {
                    room.isAvailable = false;
                    break;
                }
            }
        }
        Room.writeRoomsToDatabase(roomsList);
        return roomId;
    }

    static void createCalcCostForm(Receptionist receptionist, ArrayList<Booking> bookingList, ArrayList<Resident> residentsList) {
        JFrame costFrame = new JFrame("Calculate Resident Cost");
        costFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        costFrame.setSize(400, 200);

        JPanel costPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        // Create form fields
        JTextField residentIdField = new JTextField();

        // Add fields to the panel
        costPanel.add(new JLabel("Resident ID:"));
        costPanel.add(residentIdField);

        // Submit button
        JButton calculateButton = new JButton("Calculate Cost");
        costPanel.add(new JLabel(""));
        costPanel.add(calculateButton);

        costFrame.add(costPanel);
        costFrame.setVisible(true);

        calculateButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(residentIdField.getText());
                Resident r = receptionist.findResidentById(residentsList, id);
                if (r == null) {
                    JOptionPane.showMessageDialog(costFrame, "Resident with ID " + id + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                boolean bookingFound = false;
                StringBuilder bookingDetails = new StringBuilder();
                for (Booking booking : bookingList) {
                    if (booking.getCustomerId() == id) {
                        bookingFound = true;
                        bookingDetails.append(booking.getDescription()).append("\nTotal Cost: $").append(booking.getCost()).append("\n\n");
                    }
                }

                if (bookingFound) {
                    JOptionPane.showMessageDialog(costFrame, bookingDetails.toString(), "Booking Details", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(costFrame, "This Resident has no bookings yet!", "Information", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(costFrame, "Failed to calculate cost. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    Resident findResidentById(ArrayList<Resident> residentsList, int residentId) {
        for (Resident resident : residentsList) {
            if (resident.getResidentId() == residentId) {
                return resident;
            }
        }
        return null;
    }

    public void editResidentInfo(ArrayList<Resident> residentsList, ArrayList<Booking> bookingsList, ArrayList<Room> roomsList) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Resident ID to edit: ");
        int residentId = scanner.nextInt();
        scanner.nextLine();

        Resident resident = findResidentById(residentsList, residentId);
        if (resident == null) {
            System.out.println("Resident with ID " + residentId + " not found.");
            return;
        }

        System.out.println("What do you want to change?");
        System.out.println("1. Personal Info");
        System.out.println("2. Booking Details");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                editPersonalInfo(resident, scanner);
                break;
            case 2:
                editBookingDetails(resident, bookingsList, roomsList, scanner);
                break;
            default:
                System.out.println("Invalid choice.");
                break;
        }
    }

    private void editPersonalInfo(Resident resident, Scanner scanner) {
        System.out.println("Editing Personal Information (leave blank to skip):");
        System.out.print("Enter new name: ");
        String newName = scanner.nextLine();
        System.out.print("Enter new phone number: ");
        String newPhoneNumber = scanner.nextLine();

        // Update ArrayList fields
        if (!newName.isBlank()) {
            resident.name = newName;
        }
        if (!newPhoneNumber.isBlank()) {
            resident.phone_number = newPhoneNumber;
        }

        // Update the database
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotel", "root", "samanour")) {
            String updateQuery = "UPDATE residents SET " +
                    "name = COALESCE(NULLIF(?, ''), name), " +
                    "phone_number = COALESCE(NULLIF(?, ''), phone_number) " +
                    "WHERE residentId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
                pstmt.setString(1, newName);
                pstmt.setString(2, newPhoneNumber);
                pstmt.setInt(3, resident.getResidentId());
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Resident details updated successfully.");
                } else {
                    System.out.println("Failed to update resident in the database.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editBookingDetails(Resident resident, ArrayList<Booking> bookingsList, ArrayList<Room> roomsList, Scanner scanner) {
        BasicBooking basicBooking = null;
        int newRoomId = 0;

        for (Booking booking : bookingsList) {
            if (booking.getCustomerId() == resident.residentId) {
                basicBooking = BasicBooking.getBasicBooking(booking);
            }
        }

        if (basicBooking != null) {
            System.out.println("Editing Booking Information (leave blank to skip):");
            System.out.print("Enter new room type: ");
            String newRoomType = scanner.nextLine();
            boolean roomChanged = false;
            if (!newRoomType.isBlank()) {
                int oldRoomId = basicBooking.getRoomId();
                newRoomId = roomAssigment(roomsList, newRoomType);
                basicBooking.setroomId(newRoomId);
                roomChanged = true;

                for (Room room : roomsList) {
                    if (room.RoomId == oldRoomId) {
                        room.isAvailable = true;
                        break;
                    }
                }
                basicBooking.setRoomType(newRoomType);
            }

            System.out.print("Enter new board type: ");
            String newBoardType = scanner.nextLine();
            System.out.print("Enter new check-in date (yyyy-MM-dd): ");
            String newCheckInStr = scanner.nextLine();
            System.out.print("Enter new check-out date (yyyy-MM-dd): ");
            String newCheckOutStr = scanner.nextLine();

            if (!newBoardType.isBlank()) {
                basicBooking.setBoardType(newBoardType);
            }
            if (!newCheckInStr.isBlank()) {
                java.sql.Date newCheckInSqlDate = java.sql.Date.valueOf(newCheckInStr);
                basicBooking.setCheckIn(newCheckInSqlDate);
            }
            if (!newCheckOutStr.isBlank()) {
                java.sql.Date newCheckOutSqlDate = java.sql.Date.valueOf(newCheckOutStr);
                basicBooking.setCheckOut(newCheckOutSqlDate);
            }

            basicBooking.calcCost();

            // Update the booking in the database
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotel", "root", "samanour")) {
                String updateBookingQuery = "UPDATE bookings SET " +
                        (roomChanged ? "room_id = ?, " : "") +
                        "room_type = COALESCE(NULLIF(?, ''), room_type), " +
                        "board_type = COALESCE(NULLIF(?, ''), board_type), " +
                        "check_in = COALESCE(NULLIF(?, ''), check_in), " +
                        "check_out = COALESCE(NULLIF(?, ''), check_out) " +
                        "WHERE booking_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(updateBookingQuery)) {
                    pstmt.setInt(1, newRoomId);
                    pstmt.setString(2, newRoomType);
                    pstmt.setString(3, newBoardType);
                    pstmt.setDate(4, newCheckInStr.isBlank() ? null : java.sql.Date.valueOf(newCheckInStr));
                    pstmt.setDate(5, newCheckOutStr.isBlank() ? null : java.sql.Date.valueOf(newCheckOutStr));
                    pstmt.setInt(6, basicBooking.bookingId);

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Booking details updated successfully.");
                    } else {
                        System.out.println("Failed to update booking in the database.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    void addResidentGUI(ArrayList<Resident> residentsList, String name, String phoneNumber) {
        Resident r = new Resident(name, phoneNumber);
        residentsList.add(r);
        Resident.writeResidentsToDatabase(residentsList);
    }

    void deleteResidentGUI(ArrayList<Resident> residentsList, int id) {
        boolean found = false;

        // Delete from the database
        String sql = "DELETE FROM residents WHERE residentId = ?";
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotel", "root", "samanour");
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                // Remove from ArrayList
                for (int i = 0; i < residentsList.size(); i++) {
                    if (residentsList.get(i).residentId == id) {
                        residentsList.remove(i);
                        found = true;
                        break;
                    }
                }
                System.out.println("Resident deleted successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (!found) {
            System.out.println("Resident with ID " + id + " not found in the list.");
        }
    }


    void editPersonalInfoGUI(Resident resident, String newName, String newPhoneNumber) {
        if (!newName.isBlank()) {
            resident.name = newName;
        }
        if (!newPhoneNumber.isBlank()) {
            resident.phone_number = newPhoneNumber;
        }

        // Update the database
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotel", "root", "samanour")) {
            String updateQuery = "UPDATE residents SET " +
                    "name = COALESCE(NULLIF(?, ''), name), " +
                    "phone_number = COALESCE(NULLIF(?, ''), phone_number) " +
                    "WHERE residentId = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(updateQuery)) {
                pstmt.setString(1, newName);
                pstmt.setString(2, newPhoneNumber);
                pstmt.setInt(3, resident.getResidentId());
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Resident details updated successfully.");
                } else {
                    System.out.println("Failed to update resident in the database.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void editBookingDetailsGUI(Resident resident, ArrayList<Booking> bookingsList, ArrayList<Room> roomsList, String newRoomType, String newBoardType, String newCheckInDate, String newCheckOutDate) {
        BasicBooking basicBooking = null;
        int newRoomId = 0;

        for (Booking booking : bookingsList) {
            if (booking.getCustomerId() == resident.residentId) {
                basicBooking = BasicBooking.getBasicBooking(booking);
            }
        }

        if (basicBooking != null) {
            boolean roomChanged = false;
            if (!newRoomType.isBlank()) {
                int oldRoomId = basicBooking.getRoomId();
                newRoomId = roomAssigment(roomsList, newRoomType);
                basicBooking.setroomId(newRoomId);
                roomChanged = true;

                for (Room room : roomsList) {
                    if (room.RoomId == oldRoomId) {
                        room.isAvailable = true;
                        break;
                    }
                }
                basicBooking.setRoomType(newRoomType);
            }

            if (!newBoardType.isBlank()) {
                basicBooking.setBoardType(newBoardType);
            }
            if (!newCheckInDate.isBlank()) {
                java.sql.Date newCheckInSqlDate = java.sql.Date.valueOf(newCheckInDate);
                basicBooking.setCheckIn(newCheckInSqlDate);
            }
            if (!newCheckOutDate.isBlank()) {
                java.sql.Date newCheckOutSqlDate = java.sql.Date.valueOf(newCheckOutDate);
                basicBooking.setCheckOut(newCheckOutSqlDate);
            }

            basicBooking.calcCost();

            // Update the booking in the database
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotel", "root", "samanour")) {
                String updateBookingQuery = "UPDATE bookings SET " +
                        (roomChanged ? "room_id = ?, " : "") +
                        "room_type = COALESCE(NULLIF(?, ''), room_type), " +
                        "board_type = COALESCE(NULLIF(?, ''), board_type), " +
                        "check_in = COALESCE(NULLIF(?, ''), check_in), " +
                        "check_out = COALESCE(NULLIF(?, ''), check_out) " +
                        "WHERE booking_id = ?";
                try (PreparedStatement pstmt = connection.prepareStatement(updateBookingQuery)) {
                    int paramIndex = 1;
                    if (roomChanged) {
                        pstmt.setInt(paramIndex++, newRoomId);
                    }
                    pstmt.setString(paramIndex++, newRoomType);
                    pstmt.setString(paramIndex++, newBoardType);
                    pstmt.setDate(paramIndex++, newCheckInDate.isBlank() ? null : java.sql.Date.valueOf(newCheckInDate));
                    pstmt.setDate(paramIndex++, newCheckOutDate.isBlank() ? null : java.sql.Date.valueOf(newCheckOutDate));
                    pstmt.setInt(paramIndex, basicBooking.bookingId);

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Booking details updated successfully.");
                    } else {
                        System.out.println("Failed to update booking in the database.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}


















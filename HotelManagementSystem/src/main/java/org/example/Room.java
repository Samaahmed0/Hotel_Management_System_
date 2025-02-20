package org.example;

import java.sql.*;
import java.util.ArrayList;

public class Room {
    int RoomId;
    String roomType; // e.g., "Single", "Double", "Triple"
    boolean isAvailable; // true if the room is available, false if occupied

    public Room(int roomNumber, String roomType, boolean isAvailable) {
        this.RoomId = roomNumber;
        this.roomType = roomType;
        this.isAvailable = isAvailable;
    }

    public int getRoomId() {
        return RoomId;
    }

    public String getRoomType() {
        return roomType;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    @Override
    public String toString() {
        return "Room Number: " + RoomId +
                ", Type: " + roomType +
                ", Available: " + (isAvailable ? "Yes" : "No");
    }

    // Database credentials
    static final String URL = "jdbc:mysql://localhost:3306/hotel";
    static final String USER = "root";
    static final String PASSWORD = "samanour";

    public static ArrayList<Room> readRoomsFromDatabase() {
        ArrayList<Room> roomsList = new ArrayList<>();
        String sql = "SELECT RoomId, roomType, isAvailable FROM rooms";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                int roomId = resultSet.getInt("RoomId");
                String roomType = resultSet.getString("roomType");
                boolean isAvailable = resultSet.getBoolean("isAvailable");

                Room room = new Room(roomId, roomType, isAvailable);
                roomsList.add(room);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error reading rooms from the database.");
        }

        return roomsList;
    }


    public static void writeRoomsToDatabase(ArrayList<Room> roomsList) {
        String sql = "INSERT INTO rooms (RoomId, roomType, isAvailable) " +
                "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE roomType = ?, isAvailable = ?";

        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            for (Room room : roomsList) {
                preparedStatement.setInt(1, room.getRoomId());
                preparedStatement.setString(2, room.getRoomType());
                preparedStatement.setBoolean(3, room.isAvailable());
                preparedStatement.setString(4, room.getRoomType());
                preparedStatement.setBoolean(5, room.isAvailable());

                preparedStatement.executeUpdate();
            }

            System.out.println("Rooms successfully written to the database.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error writing rooms to the database.");
        }

    }
}

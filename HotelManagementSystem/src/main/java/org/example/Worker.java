package org.example;

import java.sql.*;
import java.util.ArrayList;

public class Worker {
    int workerId;
    String name;
    String contactInfo;
    double salary;
    String jobTitle;

    public Worker(String name, String contactInfo, double salary, String jobTitle) {
        this.name = name;
        this.contactInfo = contactInfo;
        this.salary = salary;
        this.jobTitle = jobTitle;
    }

    public Worker(int workerId, String name, String contactInfo, double salary, String jobTitle) {
        this.workerId = workerId;
        this.name = name;
        this.contactInfo = contactInfo;
        this.salary = salary;
        this.jobTitle = jobTitle;
    }

    public void setWorkerId(int newWorkerId) {
        this.workerId = newWorkerId;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public double getSalary() {
        return salary;
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public static ArrayList<Worker> loadWorkersFromDatabase() throws SQLException {
        ArrayList<Worker> workersList = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotel", "root", "samanour");
            String query = "SELECT id, name, contact_info, salary, job_title FROM worker";
            statement = connection.createStatement();
            resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String contactInfo = resultSet.getString("contact_info");
                double salary = resultSet.getDouble("salary");
                String jobTitle = resultSet.getString("job_title");

                Worker worker = new Worker(id, name, contactInfo, salary, jobTitle);
                workersList.add(worker);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (resultSet != null) try { resultSet.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (statement != null) try { statement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (connection != null) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return workersList;
    }

    public static void saveWorkersToDatabase(ArrayList<Worker> workersList) throws SQLException {
        Connection connection = null;
        PreparedStatement fetchIdsStatement = null;
        ResultSet resultSet = null;
        PreparedStatement deleteStatement = null;
        PreparedStatement upsertStatement = null;

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/hotel", "root", "samanour");
            connection.setAutoCommit(false);

            // Fetch the current maximum worker ID from the database
            int maxWorkerId = getMaxWorkerId(connection);

            // Step 1: Retrieve all worker IDs from the database
            String fetchIdsQuery = "SELECT id FROM worker";
            fetchIdsStatement = connection.prepareStatement(fetchIdsQuery);
            resultSet = fetchIdsStatement.executeQuery();

            ArrayList<Integer> databaseIds = new ArrayList<>();
            while (resultSet.next()) {
                databaseIds.add(resultSet.getInt("id"));
            }

            // Step 2: Get IDs of workers currently in the workersList
            ArrayList<Integer> currentWorkerIds = new ArrayList<>();
            for (Worker worker : workersList) {
                if (worker.workerId == 0) {
                    worker.setWorkerId(++maxWorkerId); // Generate a new worker ID
                }
                currentWorkerIds.add(worker.getWorkerId());
            }

            // Step 3: Find IDs to delete (in database but not in workersList)
            databaseIds.removeAll(currentWorkerIds);

            // Step 4: Delete workers with these IDs
            if (!databaseIds.isEmpty()) {
                String deleteQuery = "DELETE FROM worker WHERE id = ?";
                deleteStatement = connection.prepareStatement(deleteQuery);

                for (int id : databaseIds) {
                    deleteStatement.setInt(1, id);
                    deleteStatement.addBatch();
                }

                deleteStatement.executeBatch();
            }

            // Step 5: Insert or update workers
            String upsertQuery = "INSERT INTO worker (id, name, contact_info, salary, job_title) " +
                    "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                    "name = VALUES(name), contact_info = VALUES(contact_info), salary = VALUES(salary), " +
                    "job_title = VALUES(job_title)";
            upsertStatement = connection.prepareStatement(upsertQuery);

            for (Worker worker : workersList) {
                upsertStatement.setInt(1, worker.getWorkerId());
                upsertStatement.setString(2, worker.getName());
                upsertStatement.setString(3, worker.getContactInfo());
                upsertStatement.setDouble(4, worker.getSalary());
                upsertStatement.setString(5, worker.getJobTitle());
                upsertStatement.addBatch();
            }

            upsertStatement.executeBatch();
            connection.commit();

        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
            throw e;
        } finally {
            if (resultSet != null) try { resultSet.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (fetchIdsStatement != null) try { fetchIdsStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (deleteStatement != null) try { deleteStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (upsertStatement != null) try { upsertStatement.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (connection != null) try { connection.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private static int getMaxWorkerId(Connection connection) throws SQLException {
        String query = "SELECT MAX(id) AS max_id FROM worker";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            if (resultSet.next()) {
                return resultSet.getInt("max_id");
            } else {
                return 0; // No workers in the database
            }
        }
    }
}

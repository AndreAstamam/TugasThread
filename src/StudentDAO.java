import java.sql.*;
import java.util.*;

public class StudentDAO {

    public void insert(String nim, String name) throws SQLException {
        String sql = "INSERT INTO students(nim, name) VALUES(?, ?)";
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);                 // transaction
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, nim);
                ps.setString(2, name);
                ps.executeUpdate();
                Thread.sleep(1000);                 // simulate slow work so "Saving..." is visible
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw new SQLException(e.getMessage(), e);
            }
        }
    }

    public List<Object[]> findAll() throws SQLException {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT id, nim, name FROM students ORDER BY id";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                rows.add(new Object[]{rs.getInt("id"), rs.getString("nim"), rs.getString("name")});
        }
        return rows;
    }

    public void update(int id, String nim, String name) throws SQLException {
        String sql = "UPDATE students SET nim=?, name=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nim);
            ps.setString(2, name);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
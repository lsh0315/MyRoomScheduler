import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

public class ScheduleDAO {
    // DB 연결 주소
    private static final String URL = "jdbc:sqlite:scheduler.db";

    // 1. 일정 저장하기 (INSERT)
    public boolean addSchedule(String user, String time, String content) {
        String sql = "INSERT INTO SCHEDULES(USER_NAME, TIME_TXT, CONTENT) VALUES(?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, time);
            pstmt.setString(3, content);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. 모든 일정 가져오기 (SELECT)
    public ArrayList<Vector<String>> getAllSchedules() {
        ArrayList<Vector<String>> list = new ArrayList<>();

        // ID 컬럼도 같이 가져오도록 수정
        String sql = "SELECT ID, USER_NAME, TIME_TXT, CONTENT FROM SCHEDULES ORDER BY ID DESC";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Vector<String> row = new Vector<>();
                row.add(String.valueOf(rs.getInt("ID"))); // ID (삭제할 때 필요!)
                row.add(rs.getString("USER_NAME"));
                row.add(rs.getString("TIME_TXT"));
                row.add(rs.getString("CONTENT"));
                list.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 3. 일정 삭제하기 (DELETE)
    public boolean deleteSchedule(String id) {
        String sql = "DELETE FROM SCHEDULES WHERE ID = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(id)); // 문자를 숫자로 변환해서 삭제

            int result = pstmt.executeUpdate();
            return result > 0; // 성공하면 true 반환
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
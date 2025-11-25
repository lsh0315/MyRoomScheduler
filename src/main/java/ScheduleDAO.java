import java.sql.*;
import java.util.ArrayList;
import java.util.Vector;

public class ScheduleDAO {
    // DB 연결 주소 (Main이랑 똑같음)
    private static final String URL = "jdbc:sqlite:scheduler.db";

    // 1. 일정 저장하기 (INSERT)
    public boolean addSchedule(String user, String time, String content) {
        String sql = "INSERT INTO SCHEDULES(USER_NAME, TIME_TXT, CONTENT) VALUES(?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 물음표(?) 자리에 데이터 채워넣기
            pstmt.setString(1, user);
            pstmt.setString(2, time);
            pstmt.setString(3, content);

            pstmt.executeUpdate(); // 실행!
            return true; // 성공
        } catch (SQLException e) {
            e.printStackTrace();
            return false; // 실패
        }
    }

    // 2. 모든 일정 가져오기 (SELECT)
    public ArrayList<Vector<String>> getAllSchedules() {
        ArrayList<Vector<String>> list = new ArrayList<>();
        String sql = "SELECT USER_NAME, TIME_TXT, CONTENT FROM SCHEDULES ORDER BY ID DESC"; // 최신순 조회

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Vector<String> row = new Vector<>();
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
}
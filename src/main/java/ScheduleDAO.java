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
    // 4. [새로 추가] 시간 중복 검사 로직
    public boolean isOverlapping(String newTimeTxt) {
        // 1. 입력받은 "14:00~16:00"을 "~" 기준으로 쪼갭니다.
        String[] newTimes = newTimeTxt.split("~");
        if (newTimes.length != 2) {
            return false; // 형식이 이상하면 일단 겹침 검사 통과 (UI에서 막을 예정)
        }

        String newStart = newTimes[0].trim(); // "14:00"
        String newEnd = newTimes[1].trim();   // "16:00"

        String sql = "SELECT TIME_TXT FROM SCHEDULES";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // DB에 저장된 모든 일정을 하나씩 꺼내서 비교합니다.
            while (rs.next()) {
                String existingTimeTxt = rs.getString("TIME_TXT");
                String[] existingTimes = existingTimeTxt.split("~");

                if (existingTimes.length == 2) {
                    String existingStart = existingTimes[0].trim();
                    String existingEnd = existingTimes[1].trim();

                    // ★ 핵심 수학 로직: (새 시작 < 기존 종료) AND (새 종료 > 기존 시작)
                    // 글자(String)도 compareTo()를 쓰면 시간 크기를 비교할 수 있습니다!
                    if (newStart.compareTo(existingEnd) < 0 && newEnd.compareTo(existingStart) > 0) {
                        return true; // 겹치는 시간이 발견됨! (true 반환)
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // 다 뒤져봤는데 안 겹치면 통과! (false 반환)
    }
}
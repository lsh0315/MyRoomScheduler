import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        // 1. DB 연결 준비
        // "jdbc:sqlite:파일명" -> 이 이름으로 프로젝트 폴더에 파일 생성
        String url = "jdbc:sqlite:scheduler.db";

        // 2. 연결 시도 (try-catch는 예외 처리의 기본이라고 함..)
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("🎉 축하합니다! DB 연결에 성공했습니다.");
                System.out.println("생성된 DB 파일 위치: " + url);

                // --- [여기부터 추가/수정된 부분] ---
                createTable(conn); // 테이블 체크

                // 윈도우 창 실행
                // Swing UI는 안전하게 이벤트 스레드에서 실행하는 것이 정석이라고 함..
                javax.swing.SwingUtilities.invokeLater(() -> {
                    new SchedulerUI();
                });
                // -------------------------------

                // 3. 테이블 만들기 테스트 (아까 설계한 사용자 테이블)
                createTable(conn);
            }
        } catch (SQLException e) {
            System.out.println("❌ DB 연결 실패...");
            System.out.println("에러 내용: " + e.getMessage());
        }
    }

    // 테이블 만드는 함수(메인이 너무 길어져서 따로 뺌)
    public static void createTable(Connection conn) {
        // 일정을 저장할 'SCHEDULES' 테이블 생성
        String sql = "CREATE TABLE IF NOT EXISTS SCHEDULES ("
                + "ID INTEGER PRIMARY KEY AUTOINCREMENT, " // 고유 번호 (자동생성)
                + "USER_NAME TEXT, " // 누가
                + "TIME_TXT TEXT, "  // 언제
                + "CONTENT TEXT"     // 무엇을
                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ 'SCHEDULES' 테이블 준비 완료!");
        } catch (SQLException e) {
            System.out.println("⚠️ 테이블 생성 오류: " + e.getMessage());
        }
    }
}
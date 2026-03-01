import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class SchedulerUI extends JFrame {

    private DefaultTableModel tableModel;
    private JTable scheduleTable;
    private JComboBox<String> userCombo;
    private JComboBox<String> startTimeCombo;  //시작 시간과 종료 시간을 고를 수 있는 드롭다운
    private JComboBox<String> endTimeCombo;
    private JTextField contentField;

    // 일꾼(DAO) 불러오기
    private ScheduleDAO dao = new ScheduleDAO();

    public SchedulerUI() {
        setTitle("형과 나의 방구석 스케줄러 Ver 1.1 (삭제 기능 추가)");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();

        // 창 켜지자마자 저장된 데이터 불러오기
        loadDatabaseData();

        setVisible(true);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 1. 표 설정 (ID 컬럼 추가됨!)
        // 삭제를 하려면 DB의 고유 번호(ID)가 꼭 필요해서 맨 앞에 "ID"를 넣었습니다.
        String[] columnNames = {"ID", "누가(User)", "시간(Time)", "할 일(Content)"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        scheduleTable = new JTable(tableModel);
        scheduleTable.setRowHeight(30);
        scheduleTable.setFont(new Font("SansSerif", Font.PLAIN, 14));

// ★ [비법 소스] 0번째 컬럼(ID)을 아예 안 보이게 숨기기
        scheduleTable.getColumnModel().getColumn(0).setMinWidth(0);
        scheduleTable.getColumnModel().getColumn(0).setMaxWidth(0);
        scheduleTable.getColumnModel().getColumn(0).setWidth(0);

        add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        // ID 컬럼은 사용자에게 별로 안 중요하니까 너비를 좁게 설정 (50픽셀)
        scheduleTable.getColumnModel().getColumn(0).setPreferredWidth(50);

        add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        // 2. 입력 패널
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(Color.LIGHT_GRAY);
        inputPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        userCombo = new JComboBox<>(new String[]{"나", "형"});
        inputPanel.add(userCombo);

        inputPanel.add(new JLabel("시간:"));
        // 00:00 부터 23:30 까지 30분 단위로 시간 목록 만들기
        String[] timeOptions = new String[48];
        for (int i = 0; i < 24; i++) {
            timeOptions[i * 2] = String.format("%02d:00", i);
            timeOptions[i * 2 + 1] = String.format("%02d:30", i);
        }

        startTimeCombo = new JComboBox<>(timeOptions);
        startTimeCombo.setSelectedItem("14:00"); // 기본값 세팅

        endTimeCombo = new JComboBox<>(timeOptions);
        endTimeCombo.setSelectedItem("16:00");   // 기본값 세팅

        inputPanel.add(startTimeCombo);
        inputPanel.add(new JLabel("~"));
        inputPanel.add(endTimeCombo);

        inputPanel.add(new JLabel("할 일:"));
        contentField = new JTextField(20);
        inputPanel.add(contentField);

        // [등록 버튼]
        JButton addButton = new JButton("일정 등록");
        addButton.setBackground(new Color(70, 130, 180)); // 파란색
        addButton.setForeground(Color.WHITE);
        addButton.setOpaque(true);
        addButton.setBorderPainted(false);
        addButton.addActionListener(e -> addSchedule());
        inputPanel.add(addButton);

        // [삭제 버튼] - ★여기가 새로 추가된 부분★
        JButton deleteButton = new JButton("일정 삭제");
        deleteButton.setBackground(Color.RED); // 빨간색
        deleteButton.setForeground(Color.WHITE); // 글씨는 흰색

        // 맥북용 설정
        deleteButton.setOpaque(true);
        deleteButton.setBorderPainted(false);

        // 삭제 버튼 누르면 deleteSchedule() 실행
        deleteButton.addActionListener(e -> deleteSchedule());
        inputPanel.add(deleteButton);

        add(inputPanel, BorderLayout.SOUTH);
    }

    // 1. 일정 등록 함수 (중복 검사 기능 추가됨!)
    private void addSchedule() {
        String user = (String) userCombo.getSelectedItem();
        // 드롭다운에서 선택한 시작 시간과 종료 시간을 가져와서 중간에 "~"를 붙여 합칩니다!
        String time = startTimeCombo.getSelectedItem() + "~" + endTimeCombo.getSelectedItem();
        String content = contentField.getText();

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "할 일을 입력해주세요!");
            return;
        }

        // ★ [새로 추가된 핵심] DB에 넣기 전에 중복 검사 먼저 실행!
        if (dao.isOverlapping(time)) {
            // 겹치면 경고창 띄우고 함수 종료 (저장 안 함)
            JOptionPane.showMessageDialog(this, "❌ 앗! 그 시간엔 이미 방을 사용 중입니다.\n다른 시간을 선택해주세요.", "예약 불가", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 안 겹치면 원래대로 저장 진행!
        boolean isSuccess = dao.addSchedule(user, time, content);

        if (isSuccess) {
            contentField.setText(""); // 입력창 비우기
            loadDatabaseData();       // 화면 새로고침
            JOptionPane.showMessageDialog(this, "✅ 예약 성공!");
        } else {
            JOptionPane.showMessageDialog(this, "저장 실패! 에러를 확인하세요.");
        }
    }

    // 2. 일정 삭제 함수 - ★새로 추가된 함수★
    private void deleteSchedule() {
        // 표에서 선택된 줄(Row) 번호 가져오기
        int selectedRow = scheduleTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "삭제할 일정을 먼저 선택해주세요!");
            return;
        }

        // 선택된 줄의 0번째 칸(ID)에 있는 값 가져오기
        // 표에 있는 값은 Object 타입이라 String으로 변환 필요
        String id = String.valueOf(scheduleTable.getValueAt(selectedRow, 0));

        // 진짜 삭제할지 물어보기
        int confirm = JOptionPane.showConfirmDialog(this,
                "정말 이 일정을 삭제하시겠습니까?", "삭제 확인", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // DB에서 삭제
            boolean isDeleted = dao.deleteSchedule(id);

            if (isDeleted) {
                // 삭제 성공하면 화면 목록 새로고침
                loadDatabaseData();
                JOptionPane.showMessageDialog(this, "삭제되었습니다.");
            } else {
                JOptionPane.showMessageDialog(this, "삭제 실패! DB 에러.");
            }
        }
    }

    // 3. 불러오기 함수
    private void loadDatabaseData() {
        // 기존 표에 있는 내용 싹 비우기 (안 비우면 계속 쌓임)
        tableModel.setRowCount(0);

        // DB에서 데이터 가져오기
        var list = dao.getAllSchedules();

        // 표에 채워넣기
        for (Vector<String> row : list) {
            tableModel.addRow(row);
        }
    }
}
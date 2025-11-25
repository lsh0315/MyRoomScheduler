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
    private JTextField timeField;
    private JTextField contentField;

    // 일꾼(DAO) 불러오기
    private ScheduleDAO dao = new ScheduleDAO();

    public SchedulerUI() {
        setTitle("형과 나의 방구석 스케줄러 Ver 1.0");
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

        // 표 설정
        String[] columnNames = {"누가(User)", "시간(Time)", "할 일(Content)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        scheduleTable = new JTable(tableModel);
        scheduleTable.setRowHeight(30);
        scheduleTable.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        add(new JScrollPane(scheduleTable), BorderLayout.CENTER);

        // 입력 패널
        JPanel inputPanel = new JPanel();
        inputPanel.setBackground(Color.LIGHT_GRAY);
        inputPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        userCombo = new JComboBox<>(new String[]{"나", "형"});
        inputPanel.add(userCombo);

        inputPanel.add(new JLabel("시간:"));
        timeField = new JTextField(10);
        timeField.setText("14:00~16:00");
        inputPanel.add(timeField);

        inputPanel.add(new JLabel("할 일:"));
        contentField = new JTextField(20);
        inputPanel.add(contentField);

        JButton addButton = new JButton("일정 등록");
        addButton.setBackground(new Color(70, 130, 180));
        addButton.setForeground(Color.WHITE);

        // 버튼 클릭 이벤트
        addButton.addActionListener(e -> addSchedule());

        inputPanel.add(addButton);
        add(inputPanel, BorderLayout.SOUTH);
    }

    // 1. 일정 등록 함수 (DB에 저장)
    private void addSchedule() {
        String user = (String) userCombo.getSelectedItem();
        String time = timeField.getText();
        String content = contentField.getText();

        if (content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "할 일을 입력해주세요!");
            return;
        }

        // 일꾼(DAO)에게 DB 저장 시키기
        boolean isSuccess = dao.addSchedule(user, time, content);

        if (isSuccess) {
            // 저장이 성공하면 화면에도 한 줄 추가
            tableModel.addRow(new String[]{user, time, content});
            contentField.setText(""); // 입력창 비우기
        } else {
            JOptionPane.showMessageDialog(this, "저장 실패! 에러를 확인하세요.");
        }
    }

    // 2. 불러오기 함수 (프로그램 켤 때 실행)
    private void loadDatabaseData() {
        // 일꾼한테 데이터 다 가져오라고 시키기
        var list = dao.getAllSchedules();

        // 가져온 거 표에 하나씩 채워넣기
        for (Vector<String> row : list) {
            tableModel.addRow(row);
        }
    }
}
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

public class TimerApp extends JFrame {
    private JLabel timerLabel;
    private JButton toggleButton;
    private boolean isDarkMode = false;
    private List<LocalTime> alarmTimes = new ArrayList<>();
    private List<Boolean> alarmStatus = new ArrayList<>();
    private ImageIcon alarmIconOn;
    private ImageIcon alarmIconOff;
    private Timer notificationTimer;

    public TimerApp() {
        loadIcons();
        setupFrame();
        setupUIComponents();
        setupSystemTray();
        startTimer();
    }

    private void loadIcons() {
        try {
            alarmIconOn = new ImageIcon(
                    new ImageIcon("Imagens/alarm_on.png").getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
            alarmIconOff = new ImageIcon(
                    new ImageIcon("Imagens/alarm_off.png").getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
            setIconImage(Toolkit.getDefaultToolkit().getImage("Imagens/app_icon.jpg"));
        } catch (Exception e) {
            System.err.println("Erro ao carregar ícones: " + e.getMessage());
        }
    }

    private void setupFrame() {
        setTitle("Temporizador");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void setupUIComponents() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new GridBagLayout());
        add(panel, BorderLayout.CENTER);

        timerLabel = new JLabel(getCurrentTime(), SwingConstants.CENTER);
        timerLabel.setFont(new Font("Verdana", Font.BOLD, 72));
        timerLabel.setForeground(Color.BLACK);
        panel.add(timerLabel, createGbc(0, 0, 20));

        toggleButton = new JButton("Desligado");
        toggleButton.setFont(new Font("Verdana", Font.PLAIN, 24));
        toggleButton.setPreferredSize(new Dimension(200, 75));
        toggleButton.setBackground(Color.WHITE);
        toggleButton.setBorder(new RoundedBorder(20));
        toggleButton.setFocusPainted(false);
        toggleButton.addActionListener(e -> toggleNotifications());
        panel.add(toggleButton, createGbc(0, 1, 20));

        setupTopPanel();
    }

    private GridBagConstraints createGbc(int x, int y, int topInset) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.insets = new Insets(topInset, 0, 20, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        return gbc;
    }

    private void setupTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setBackground(Color.WHITE);
        topPanel.setPreferredSize(new Dimension(400, 50));
        topPanel.setLayout(new BorderLayout());

        JLabel bellLabel = new JLabel(new ImageIcon(
                new ImageIcon("Imagens/bell.png").getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        topPanel.add(bellLabel, BorderLayout.WEST);

        JLabel darkModeLabel = new JLabel(new ImageIcon(
                new ImageIcon("Imagens/dark_mode.png").getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
        topPanel.add(darkModeLabel, BorderLayout.EAST);
        darkModeLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                toggleDarkMode();
            }
        });

        bellLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                abrirAbaAlarmes();
            }
        });

        add(topPanel, BorderLayout.NORTH);
    }

    private void startTimer() {
        Timer timer = new Timer( 50 * 60 *1000, e -> {
            updateTimer();
            checkAlarms();
        });
        timer.start();
    }

    private String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    private void updateTimer() {
        timerLabel.setText(getCurrentTime());
    }

    private void toggleDarkMode() {
        isDarkMode = !isDarkMode;
        Color bgColor = isDarkMode ? Color.BLACK : Color.WHITE;
        Color fgColor = isDarkMode ? Color.WHITE : Color.BLACK;
        getContentPane().setBackground(bgColor);
        timerLabel.setForeground(fgColor);
        toggleButton.setBackground(isDarkMode ? Color.DARK_GRAY : Color.WHITE);
        toggleButton.setForeground(fgColor);
    }

    private void abrirAbaAlarmes() {
        JFrame alarmFrame = new JFrame("Alarmes");
        alarmFrame.setSize(400, 300);
        alarmFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        alarmFrame.setLayout(new BorderLayout());

        JPanel alarmPanel = new JPanel();
        alarmPanel.setBackground(Color.WHITE);
        alarmPanel.setLayout(new BoxLayout(alarmPanel, BoxLayout.Y_AXIS));

        for (int i = 0; i < alarmTimes.size(); i++) {
            alarmPanel.add(createAlarmPanel(alarmTimes.get(i), i));
        }

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JLabel pencilLabel = new JLabel(new ImageIcon(
                new ImageIcon("Imagens/pencil.png").getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));
        JLabel plusLabel = new JLabel(new ImageIcon(
                new ImageIcon("Imagens/plus.png").getImage().getScaledInstance(24, 24, Image.SCALE_SMOOTH)));

        buttonPanel.add(pencilLabel);
        buttonPanel.add(plusLabel);

        plusLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                adicionarAlarme();
                alarmPanel.add(createAlarmPanel(alarmTimes.get(alarmTimes.size() - 1), alarmTimes.size() - 1), 0);
                alarmPanel.revalidate();
                alarmPanel.repaint();
            }
        });

        pencilLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                editarAlarme();
            }
        });

        alarmFrame.add(new JScrollPane(alarmPanel), BorderLayout.CENTER);
        alarmFrame.add(buttonPanel, BorderLayout.SOUTH);
        alarmFrame.setVisible(true);
    }

    private JPanel createAlarmPanel(LocalTime alarmTime, int index) {
        JPanel alarmPanel = new JPanel();
        alarmPanel.setLayout(new BorderLayout());
        alarmPanel.setBackground(Color.WHITE);

        JLabel timeLabel = new JLabel(alarmTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        timeLabel.setFont(new Font("Verdana", Font.BOLD, 24));
        alarmPanel.add(timeLabel, BorderLayout.CENTER);

        JButton alarmButton = new JButton(alarmStatus.get(index) ? alarmIconOn : alarmIconOff);
        alarmButton.setPreferredSize(new Dimension(40, 40));
        alarmButton.setBorderPainted(false);
        alarmButton.setFocusPainted(false);
        alarmButton.setContentAreaFilled(false);
        alarmButton.addActionListener(e -> toggleAlarm(index, alarmButton));
        alarmPanel.add(alarmButton, BorderLayout.EAST);

        alarmPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        return alarmPanel;
    }

    private void toggleAlarm(int index, JButton alarmButton) {
        alarmStatus.set(index, !alarmStatus.get(index));
        alarmButton.setIcon(alarmStatus.get(index) ? alarmIconOn : alarmIconOff);
    }

    private void adicionarAlarme() {
        JPanel timePanel = new JPanel();
        timePanel.setLayout(new GridLayout(2, 2));
        timePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 23, 1));
        JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

        timePanel.add(new JLabel("Hora:"));
        timePanel.add(hourSpinner);
        timePanel.add(new JLabel("Minuto:"));
        timePanel.add(minuteSpinner);

        JOptionPane.showMessageDialog(this, timePanel, "Defina o horário do alarme", JOptionPane.PLAIN_MESSAGE);

        int hour = (Integer) hourSpinner.getValue();
        int minute = (Integer) minuteSpinner.getValue();
        LocalTime alarmTime = LocalTime.of(hour, minute);
        alarmTimes.add(alarmTime);
        alarmStatus.add(true); // Novo alarme está ativo por padrão
    }

    private void editarAlarme() {
        int index = JOptionPane.showOptionDialog(this, "Selecione o alarme para editar:", "Editar Alarme",
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, alarmTimes.toArray(), null);
        if (index >= 0) {
            JPanel timePanel = new JPanel();
            timePanel.setLayout(new GridLayout(2, 2));
            JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(alarmTimes.get(index).getHour(), 0, 23, 1));
            JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(alarmTimes.get(index).getMinute(), 0, 59, 1));

            timePanel.add(new JLabel("Hora:"));
            timePanel.add(hourSpinner);
            timePanel.add(new JLabel("Minuto:"));
            timePanel.add(minuteSpinner);

            JOptionPane.showMessageDialog(this, timePanel, "Atualizar horário do alarme", JOptionPane.PLAIN_MESSAGE);

            int hour = (Integer) hourSpinner.getValue();
            int minute = (Integer) minuteSpinner.getValue();
            LocalTime alarmTime = LocalTime.of(hour, minute);
            alarmTimes.set(index, alarmTime);
        }
    }

    private void toggleNotifications() {
        if (toggleButton.getText().equals("Ligado")) {
            toggleButton.setText("Desligado");
            stopNotifications();
        } else {
            toggleButton.setText("Ligado");
            startNotifications();
        }
    }

    private void startNotifications() {
        String[] palavras = {
                "Beba Agua!", "Nao esqueça de beber Agua!", "Hora de se hidratar!", "Lembre-se de tomar Agua!"
        };
        Random random = new Random();
        notificationTimer = new Timer(5000, e -> {
            String randomPalavra = palavras[random.nextInt(palavras.length)];
            sendNativeNotification("Notificação", randomPalavra);
        });
        notificationTimer.start();
    }

    private void stopNotifications() {
        if (notificationTimer != null) {
            notificationTimer.stop();
        }
    }

    private void sendNativeNotification(String title, String message) {
        if (!SystemTray.isSupported()) {
            System.err.println("System tray is not supported!");
            return;
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage("Imagens/app_icon.jpg");

        TrayIcon trayIcon = new TrayIcon(image, "Beber Agua");
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void checkAlarms() {
        LocalTime now = LocalTime.now();
        for (int i = 0; i < alarmTimes.size(); i++) {
            if (alarmStatus.get(i) && alarmTimes.get(i).getHour() == now.getHour()
                    && alarmTimes.get(i).getMinute() == now.getMinute()) {
                sendNativeNotification("Alarme",
                        "Alarme " + alarmTimes.get(i).format(DateTimeFormatter.ofPattern("HH:mm")) + " tocando!");
                alarmStatus.set(i, false); // Desativa o alarme após notificar
            }
        }
    }

    private void setupSystemTray() {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage("Imagens/app_icon.jpg");
            TrayIcon trayIcon = new TrayIcon(image, "Beber Agua");
            trayIcon.setImageAutoSize(true);

            PopupMenu popup = new PopupMenu();
            MenuItem restoreItem = new MenuItem("Restaurar");
            restoreItem.addActionListener(e -> {
                setVisible(true);
                setExtendedState(JFrame.NORMAL);
            });

            MenuItem exitItem = new MenuItem("Sair");
            exitItem.addActionListener(e -> System.exit(0));

            popup.add(restoreItem);
            popup.add(exitItem);
            trayIcon.setPopupMenu(popup);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                e.printStackTrace();
            }

            addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowIconified(java.awt.event.WindowEvent e) {
                    setVisible(false);
                }
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TimerApp app = new TimerApp();
            app.setVisible(true);
        });
    }

    static class RoundedBorder extends AbstractBorder {
        private int radius;

        RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius + 1, this.radius + 1, this.radius + 2, this.radius);
        }
    }
}

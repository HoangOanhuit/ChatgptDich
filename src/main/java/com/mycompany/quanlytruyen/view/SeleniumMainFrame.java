package com.mycompany.quanlytruyen.view;

import com.mycompany.quanlytruyen.service.SeleniumClaudeService;
import com.mycompany.quanlytruyen.database.DatabaseManager;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.TitledBorder;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * IMPROVED SeleniumMainFrame with:
 * - Input validation
 * - Better error messages
 * - Pause/Resume functionality
 * - Progress tracking
 * - Configurable retry settings
 */
public class SeleniumMainFrame extends JFrame {
    private JTextField userDataDirField;
    private JTextField profileNameField;
    private JTextField projectUrlField;
    private JButton initBrowserBtn;
    private JButton loginCheckBtn;
    private JTextArea translationRulesArea;
    private JTextArea characterTableArea;
    private JList<String> chapterList;
    private DefaultListModel<String> chapterListModel;
    private JButton selectChaptersBtn;
    private JButton startTranslateBtn;
    private JButton pauseBtn;
    private JProgressBar progressBar;
    private JTextArea logArea;
    private JLabel statusLabel;
    
    // Retry settings
    private JSpinner maxRetriesSpinner;
    private JSpinner retryDelaySpinner;
    
    private List<File> selectedChapterFiles;
    private SeleniumClaudeService seleniumService;
    private DatabaseManager dbManager;
    private boolean browserReady = false;
    private volatile boolean isPaused = false;
    private volatile boolean isCancelled = false;
    private SwingWorker<Void, String> currentWorker;
    
    public SeleniumMainFrame() {
        initComponents();
        seleniumService = new SeleniumClaudeService();
        dbManager = new DatabaseManager();
        
        try {
            dbManager.initDatabase();
            log("✓ Database đã khởi tạo");
        } catch (Exception e) {
            log("✗ Lỗi khởi tạo database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void initComponents() {
        setTitle("Dịch Truyện với Selenium + Claude Web (Enhanced)");
        setSize(1400, 900);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        JPanel setupPanel = createBrowserSetupPanel();
        JPanel configPanel = createConfigPanel();
        JPanel inputPanel = createInputPanel();
        JPanel bottomPanel = createBottomPanel();
        
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(setupPanel, BorderLayout.NORTH);
        topPanel.add(configPanel, BorderLayout.SOUTH);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        selectedChapterFiles = new ArrayList<>();
    }
    
    // ==================== PANELS ====================
    
    private JPanel createBrowserSetupPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Bước 1: Cấu hình Chrome Profile"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // User Data Dir
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Chrome User Data Dir:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        userDataDirField = new JTextField(getDefaultUserDataDir());
        panel.add(userDataDirField, gbc);
        
        gbc.gridx = 2; gbc.weightx = 0;
        JButton browseBtn = new JButton("Browse");
        browseBtn.addActionListener(e -> browseUserDataDir());
        panel.add(browseBtn, gbc);
        
        // Profile Name
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Profile Name:"), gbc);
        
        gbc.gridx = 1;
        profileNameField = new JTextField("Default");
        panel.add(profileNameField, gbc);
        
        gbc.gridx = 2;
        JButton helpBtn = new JButton("?");
        helpBtn.addActionListener(e -> showProfileHelp());
        panel.add(helpBtn, gbc);
        
        // Project URL
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Claude Project URL (tùy chọn):"), gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        projectUrlField = new JTextField();
        projectUrlField.setToolTipText("https://claude.ai/project/...");
        panel.add(projectUrlField, gbc);
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 3;
        initBrowserBtn = new JButton("🚀 Khởi động Chrome");
        initBrowserBtn.setFont(new Font("Arial", Font.BOLD, 14));
        initBrowserBtn.addActionListener(e -> initBrowser());
        panel.add(initBrowserBtn, gbc);
        
        gbc.gridy = 4;
        loginCheckBtn = new JButton("✓ Kiểm tra đăng nhập");
        loginCheckBtn.setEnabled(false);
        loginCheckBtn.addActionListener(e -> checkLogin());
        panel.add(loginCheckBtn, gbc);
        
        // Status
        gbc.gridy = 5;
        statusLabel = new JLabel("Chưa khởi động browser", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(Color.RED);
        panel.add(statusLabel, gbc);
        
        return panel;
    }
    
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("Cấu hình Retry"));
        
        panel.add(new JLabel("Max Retries:"));
        maxRetriesSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 10, 1));
        panel.add(maxRetriesSpinner);
        
        panel.add(Box.createHorizontalStrut(20));
        
        panel.add(new JLabel("Retry Delay (s):"));
        retryDelaySpinner = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        panel.add(retryDelaySpinner);
        
        JButton applyBtn = new JButton("Áp dụng");
        applyBtn.addActionListener(e -> applyRetryConfig());
        panel.add(applyBtn);
        
        return panel;
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 0));
        panel.setBorder(BorderFactory.createTitledBorder("Bước 2: Chuẩn bị dữ liệu"));
        
        // Translation Rules
        JPanel rulesPanel = new JPanel(new BorderLayout(5, 5));
        rulesPanel.add(new JLabel("Yêu cầu dịch:"), BorderLayout.NORTH);
        translationRulesArea = new JTextArea();
        translationRulesArea.setLineWrap(true);
        rulesPanel.add(new JScrollPane(translationRulesArea), BorderLayout.CENTER);
        
        JButton loadRulesBtn = new JButton("Load file");
        loadRulesBtn.addActionListener(e -> loadTextFile(translationRulesArea));
        rulesPanel.add(loadRulesBtn, BorderLayout.SOUTH);
        
        // Character Table
        JPanel charPanel = new JPanel(new BorderLayout(5, 5));
        charPanel.add(new JLabel("Bảng tên:"), BorderLayout.NORTH);
        characterTableArea = new JTextArea();
        characterTableArea.setLineWrap(true);
        charPanel.add(new JScrollPane(characterTableArea), BorderLayout.CENTER);
        
        JButton loadCharBtn = new JButton("Load file");
        loadCharBtn.addActionListener(e -> loadTextFile(characterTableArea));
        charPanel.add(loadCharBtn, BorderLayout.SOUTH);
        
        // Chapters
        JPanel chaptersPanel = new JPanel(new BorderLayout(5, 5));
        chaptersPanel.add(new JLabel("Danh sách chương:"), BorderLayout.NORTH);
        chapterListModel = new DefaultListModel<>();
        chapterList = new JList<>(chapterListModel);
        chaptersPanel.add(new JScrollPane(chapterList), BorderLayout.CENTER);
        
        JPanel chapterBtnPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        selectChaptersBtn = new JButton("Chọn chương");
        selectChaptersBtn.addActionListener(e -> selectChapters());
        chapterBtnPanel.add(selectChaptersBtn);
        
        JButton clearBtn = new JButton("Xóa danh sách");
        clearBtn.addActionListener(e -> {
            chapterListModel.clear();
            selectedChapterFiles.clear();
            log("✓ Đã xóa danh sách chương");
        });
        chapterBtnPanel.add(clearBtn);
        
        chaptersPanel.add(chapterBtnPanel, BorderLayout.SOUTH);
        
        panel.add(rulesPanel);
        panel.add(charPanel);
        panel.add(chaptersPanel);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Bước 3: Dịch tự động"));
        
        // Control buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        
        startTranslateBtn = new JButton("▶️ BẮT ĐẦU DỊCH");
        startTranslateBtn.setFont(new Font("Arial", Font.BOLD, 16));
        startTranslateBtn.setEnabled(false);
        startTranslateBtn.addActionListener(e -> startAutoTranslation());
        btnPanel.add(startTranslateBtn);
        
        pauseBtn = new JButton("⏸️ TẠM DỪNG");
        pauseBtn.setFont(new Font("Arial", Font.BOLD, 16));
        pauseBtn.setEnabled(false);
        pauseBtn.addActionListener(e -> togglePause());
        btnPanel.add(pauseBtn);
        
        JButton cancelBtn = new JButton("⏹️ HỦY BỎ");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 16));
        cancelBtn.addActionListener(e -> cancelTranslation());
        btnPanel.add(cancelBtn);
        
        panel.add(btnPanel, BorderLayout.NORTH);
        
        // Progress
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        panel.add(progressBar, BorderLayout.CENTER);
        
        // Log
        logArea = new JTextArea(10, 100);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        panel.add(logScroll, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // ==================== HELPER METHODS ====================
    
    private String getDefaultUserDataDir() {
        String os = System.getProperty("os.name").toLowerCase();
        String user = System.getProperty("user.name");
        
        if (os.contains("win")) {
            return "C:\\Users\\" + user + "\\AppData\\Local\\Google\\Chrome\\User Data";
        } else if (os.contains("mac")) {
            return "/Users/" + user + "/Library/Application Support/Google/Chrome";
        } else {
            return "/home/" + user + "/.config/google-chrome";
        }
    }
    
    private void browseUserDataDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Chọn Chrome User Data Directory");
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            userDataDirField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void showProfileHelp() {
        String help = "CÁCH TÌM CHROME PROFILE:\n\n" +
                     "1. Mở Chrome\n" +
                     "2. Gõ vào thanh địa chỉ: chrome://version/\n" +
                     "3. Tìm dòng 'Profile Path'\n" +
                     "4. Ví dụ:\n" +
                     "   Path: C:\\Users\\Admin\\AppData\\Local\\Google\\Chrome\\User Data\\Default\n" +
                     "   → User Data Dir: C:\\Users\\Admin\\AppData\\Local\\Google\\Chrome\\User Data\n" +
                     "   → Profile Name: Default\n\n" +
                     "Các profile thường gặp:\n" +
                     "- Default (mặc định)\n" +
                     "- Profile 1, Profile 2, ... (profile phụ)\n\n" +
                     "LƯU Ý: Phải đăng nhập Claude trong profile đó trước!";
        
        JOptionPane.showMessageDialog(this, help, "Hướng dẫn Profile", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void applyRetryConfig() {
        int maxRetries = (Integer) maxRetriesSpinner.getValue();
        int retryDelay = (Integer) retryDelaySpinner.getValue();
        
        seleniumService.setMaxRetries(maxRetries);
        seleniumService.setRetryDelay(retryDelay);
        
        log("✓ Đã cập nhật: Max Retries = " + maxRetries + ", Retry Delay = " + retryDelay + "s");
    }
    
    private void loadTextFile(JTextArea targetArea) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text files", "txt", "csv"));
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                String content = Files.readString(chooser.getSelectedFile().toPath());
                targetArea.setText(content);
                log("✓ Đã load: " + chooser.getSelectedFile().getName());
            } catch (Exception e) {
                showError("Lỗi đọc file", e);
            }
        }
    }
    
    private void selectChapters() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Chapter files (C*.txt)", "txt"));
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedChapterFiles.clear();
            chapterListModel.clear();
            
            for (File file : chooser.getSelectedFiles()) {
                if (file.getName().matches("C\\d+\\.txt")) {
                    selectedChapterFiles.add(file);
                    chapterListModel.addElement(file.getName());
                } else {
                    log("⚠ Bỏ qua file không đúng format: " + file.getName());
                }
            }
            
            // Sort by chapter number
            selectedChapterFiles.sort((f1, f2) -> {
                int num1 = extractChapterNumber(f1.getName());
                int num2 = extractChapterNumber(f2.getName());
                return Integer.compare(num1, num2);
            });
            
            // Update list display
            chapterListModel.clear();
            for (File f : selectedChapterFiles) {
                chapterListModel.addElement(f.getName());
            }
            
            log("✓ Đã chọn " + selectedChapterFiles.size() + " chương");
        }
    }
    
    private int extractChapterNumber(String filename) {
        try {
            return Integer.parseInt(filename.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    // ==================== VALIDATION ====================
    
    private boolean validateInputs() {
        // Check browser
        if (!browserReady) {
            showError("Lỗi", new Exception("Vui lòng khởi động Chrome trước!"));
            return false;
        }
        
        // Check chapters
        if (selectedChapterFiles.isEmpty()) {
            showError("Lỗi", new Exception("Chưa chọn chương nào để dịch!"));
            return false;
        }
        
        // Check project or rules
        String projectUrl = projectUrlField.getText().trim();
        boolean usingProject = !projectUrl.isEmpty();
        
        if (!usingProject) {
            if (translationRulesArea.getText().trim().isEmpty()) {
                int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Chưa có yêu cầu dịch và không dùng Project!\n" +
                    "Bạn có chắc muốn tiếp tục?",
                    "Cảnh báo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (choice != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    // ==================== BROWSER CONTROL ====================
    
    private void initBrowser() {
        String userDataDir = userDataDirField.getText().trim();
        String profile = profileNameField.getText().trim();
        
        // Validate inputs
        if (!validateBrowserConfig(userDataDir, profile)) {
            return;
        }
        
        // Ask to kill Chrome
        int killChoice = JOptionPane.showConfirmDialog(
            this,
            "Cần đóng tất cả Chrome để khởi động với automation.\n" +
            "Đóng Chrome ngay bây giờ?",
            "Đóng Chrome",
            JOptionPane.YES_NO_CANCEL_OPTION
        );
        
        if (killChoice == JOptionPane.CANCEL_OPTION) {
            return;
        }
        
        if (killChoice == JOptionPane.YES_OPTION) {
            killChromeProcesses();
        }
        
        initBrowserBtn.setEnabled(false);
        statusLabel.setText("Đang khởi động Chrome...");
        
        new Thread(() -> {
            try {
                // Set project URL if provided
                String projectUrl = projectUrlField.getText().trim();
                if (!projectUrl.isEmpty()) {
                    seleniumService.setProjectUrl(projectUrl);
                } else {
                    seleniumService.setProjectUrl(null);                    
                }
                
                seleniumService.initWithProfile(userDataDir, profile);
                
                SwingUtilities.invokeLater(() -> {
                    browserReady = true;
                    loginCheckBtn.setEnabled(true);
                    statusLabel.setText("Chrome đã khởi động! Hãy kiểm tra đăng nhập.");
                    statusLabel.setForeground(Color.BLUE);
                    log("✓ Chrome đã khởi động thành công");
                    
                    // Auto check login
                    checkLogin();
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    showError("Lỗi khởi động Chrome", e);
                    initBrowserBtn.setEnabled(true);
                    statusLabel.setText("Lỗi khởi động!");
                    statusLabel.setForeground(Color.RED);
                });
            }
        }).start();
    }

    private boolean validateBrowserConfig(String userDataDir, String profile) {
        if (userDataDir.isEmpty() || profile.isEmpty()) {
            showError("Lỗi", new Exception("Vui lòng điền đầy đủ thông tin!"));
            return false;
        }

        try {
            Path userDataPath = Paths.get(userDataDir);
            if (!Files.exists(userDataPath) || !Files.isDirectory(userDataPath)) {
                showError("Lỗi", new Exception("User Data Dir không tồn tại hoặc không phải thư mục!"));
                return false;
            }

            Path profilePath = userDataPath.resolve(profile);
            if (!Files.exists(profilePath) || !Files.isDirectory(profilePath)) {
                showError("Lỗi", new Exception("Không tìm thấy profile '" + profile + "' trong User Data Dir!"));
                return false;
            }
        } catch (InvalidPathException e) {
            showError("Lỗi", new Exception("Đường dẫn User Data Dir không hợp lệ!"));
            return false;
        }

        return true;
    }    
    private void killChromeProcesses() {
        try {
            log("Đang đóng Chrome processes...");
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                Runtime.getRuntime().exec("taskkill /F /IM chrome.exe /T");
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec("pkill -9 'Google Chrome'");
            } else {
                Runtime.getRuntime().exec("pkill -9 chrome");
            }
            
            Thread.sleep(2000);
            log("✓ Đã đóng Chrome");
        } catch (Exception e) {
            log("⚠ Không thể kill Chrome tự động: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Vui lòng đóng Chrome thủ công!",
                "Cảnh báo",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void checkLogin() {
        loginCheckBtn.setEnabled(false);
        statusLabel.setText("Đang kiểm tra đăng nhập...");
        
        new Thread(() -> {
            boolean loggedIn = seleniumService.checkLogin();
            
            SwingUtilities.invokeLater(() -> {
                if (loggedIn) {
                    statusLabel.setText("✓ ĐÃ ĐĂNG NHẬP - Sẵn sàng dịch!");
                    statusLabel.setForeground(new Color(0, 150, 0));
                    startTranslateBtn.setEnabled(true);
                    log("✓ Đã đăng nhập Claude thành công!");
                } else {
                    statusLabel.setText("✗ CHƯA ĐĂNG NHẬP");
                    statusLabel.setForeground(Color.RED);
                    log("✗ Chưa đăng nhập!");
                    
                    int result = JOptionPane.showConfirmDialog(
                        this,
                        "Vui lòng đăng nhập Claude trong trình duyệt.\n" +
                        "Nhấn OK khi đã đăng nhập xong.",
                        "Cần đăng nhập",
                        JOptionPane.OK_CANCEL_OPTION
                    );
                    
                    if (result == JOptionPane.OK_OPTION) {
                        checkLogin(); // Retry
                    }
                }
                loginCheckBtn.setEnabled(true);
            });
        }).start();
    }
    
    // ==================== TRANSLATION CONTROL ====================
    
    private void startAutoTranslation() {
        // Validate
        if (!validateInputs()) {
            return;
        }
        
        String projectUrl = projectUrlField.getText().trim();
        boolean usingProject = !projectUrl.isEmpty();
        String mode = usingProject ? "Claude Project" : "conversation thường";
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            String.format(
                "Bắt đầu dịch %d chương với %s?\n" +
                "Max retries: %d, Retry delay: %ds",
                selectedChapterFiles.size(),
                mode,
                (Integer) maxRetriesSpinner.getValue(),
                (Integer) retryDelaySpinner.getValue()
            ),
            "Xác nhận",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Reset flags
        isPaused = false;
        isCancelled = false;
        
        // Disable buttons
        startTranslateBtn.setEnabled(false);
        selectChaptersBtn.setEnabled(false);
        initBrowserBtn.setEnabled(false);
        pauseBtn.setEnabled(true);
        
        // Clear log
        logArea.setText("");
        
        // Start worker
        currentWorker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                String rules = translationRulesArea.getText();
                String charTable = characterTableArea.getText();
                int total = selectedChapterFiles.size();
                int success = 0;
                int failed = 0;
                
                publish("=== BẮT ĐẦU DỊCH ===");
                publish("Mode: " + mode);
                publish("Tổng số chương: " + total);
                publish("Max retries: " + seleniumService);
                publish("");
                
                for (int i = 0; i < total && !isCancelled; i++) {
                    // Check pause
                    while (isPaused && !isCancelled) {
                        Thread.sleep(500);
                    }
                    
                    if (isCancelled) break;
                    
                    File chapterFile = selectedChapterFiles.get(i);
                    String chapterName = chapterFile.getName();
                    int chapterNum = extractChapterNumber(chapterName);
                    
                    publish(String.format("\n[%d/%d] Đang dịch: %s", i+1, total, chapterName));
                    
                    try {
                        String content = Files.readString(chapterFile.toPath());
                        publish("  → Độ dài: " + content.length() + " ký tự");
                        
                        String translation = seleniumService.translate(content, rules, charTable);
                        
                        dbManager.saveTranslation(chapterNum, content, translation);
                        
                        publish("  ✓ Hoàn thành! Bản dịch: " + translation.length() + " ký tự");
                        success++;
                        
                        int progress = (int) (((i + 1) * 100.0) / total);
                        setProgress(progress);
                        
                        if (i < total - 1) {
                            publish("  ⏳ Chờ 3 giây...");
                            Thread.sleep(3000);
                        }
                        
                    } catch (Exception ex) {
                        publish("  ✗ LỖI: " + ex.getMessage());
                        ex.printStackTrace();
                        failed++;
                        
                        // Screenshot
                        seleniumService.takeScreenshot(
                            "error_chapter_" + chapterNum + "_" + System.currentTimeMillis() + ".png"
                        );
                    }
                }
                
                publish("\n=== KẾT THÚC ===");
                publish("Thành công: " + success + "/" + total);
                publish("Thất bại: " + failed + "/" + total);
                if (isCancelled) {
                    publish("(Đã bị hủy bởi người dùng)");
                }
                
                return null;
            }
            
            @Override
            protected void process(List<String> chunks) {
                for (String msg : chunks) {
                    log(msg);
                }
            }
            
            @Override
            protected void done() {
                startTranslateBtn.setEnabled(true);
                selectChaptersBtn.setEnabled(true);
                initBrowserBtn.setEnabled(true);
                pauseBtn.setEnabled(false);
                pauseBtn.setText("⏸️ TẠM DỪNG");
                progressBar.setValue(100);
                
                String message = isCancelled ? "Đã hủy quá trình dịch!" : "Hoàn thành!";
                JOptionPane.showMessageDialog(
                    SeleniumMainFrame.this,
                    message,
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        };
        
        currentWorker.execute();
    }
    
    private void togglePause() {
        isPaused = !isPaused;
        
        if (isPaused) {
            pauseBtn.setText("▶️ TIẾP TỤC");
            log("\n⏸️ ĐÃ TẠM DỪNG - Nhấn 'Tiếp tục' để tiếp tục dịch");
        } else {
            pauseBtn.setText("⏸️ TẠM DỪNG");
            log("\n▶️ TIẾP TỤC DỊCH...");
        }
    }
    
    private void cancelTranslation() {
        if (currentWorker != null && !currentWorker.isDone()) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn hủy quá trình dịch?",
                "Xác nhận hủy",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                isCancelled = true;
                isPaused = false; // Unpause to let worker exit
                log("\n🛑 ĐANG HỦY...");
            }
        }
    }
    
    // ==================== UTILITY ====================
    
    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
    
    private void showError(String title, Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        log("✗ " + title + ": " + message);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        if (seleniumService != null) {
            seleniumService.close();
        }
        if (dbManager != null) {
            try {
                dbManager.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    // ==================== MAIN ====================
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            SeleniumMainFrame frame = new SeleniumMainFrame();
            frame.setVisible(true);
            
            JOptionPane.showMessageDialog(
                frame,
                "HƯỚNG DẪN SỬ DỤNG (ENHANCED VERSION):\n\n" +
                "BƯỚC 1: CẤU HÌNH CHROME\n" +
                "- Nhập User Data Dir và Profile Name\n" +
                "- (Tùy chọn) Nhập Claude Project URL\n" +
                "- Nhấn 'Khởi động Chrome'\n\n" +
                "BƯỚC 2: KIỂM TRA ĐĂNG NHẬP\n" +
                "- Nhấn 'Kiểm tra đăng nhập'\n" +
                "- Đăng nhập thủ công nếu cần\n\n" +
                "BƯỚC 3: CHUẨN BỊ DỮ LIỆU\n" +
                "- Load yêu cầu dịch & bảng tên (hoặc dùng Project)\n" +
                "- Chọn các chương cần dịch\n\n" +
                "BƯỚC 4: DỊCH\n" +
                "- Cấu hình retry nếu cần\n" +
                "- Nhấn 'Bắt đầu dịch'\n" +
                "- Có thể Tạm dừng/Hủy bất cứ lúc nào\n\n" +
                "TÍNH NĂNG MỚI:\n" +
                "✓ Retry tự động khi lỗi\n" +
                "✓ Validation đầu vào\n" +
                "✓ Pause/Resume\n" +
                "✓ Screenshot khi lỗi\n" +
                "✓ Error messages rõ ràng hơn",
                "Chào mừng",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }
}
package com.mycompany.quanlytruyen.view.translate;

import com.mycompany.quanlytruyen.config.AppConfig;
import com.mycompany.quanlytruyen.dao.ChapterDao;
import com.mycompany.quanlytruyen.utils.UIUtils;
import com.mycompany.quanlytruyen.utils.TextFieldKeyboardUtils;
import com.mycompany.quanlytruyen.dao.DataSourceFactory;
import com.mycompany.quanlytruyen.model.ProgressEvent;
import com.mycompany.quanlytruyen.service.AIClient;
import com.mycompany.quanlytruyen.service.DeepSeekClient;
import com.mycompany.quanlytruyen.service.FileService;
import com.mycompany.quanlytruyen.service.OpenAIClient;
import com.mycompany.quanlytruyen.service.TranslationService;
import com.mycompany.quanlytruyen.utils.CsvNameTableFormatter;
import com.mycompany.quanlytruyen.utils.FileUtils;
import com.mycompany.quanlytruyen.view.ResumeDialog;
import com.mycompany.quanlytruyen.dao.BookDao;
import com.mycompany.quanlytruyen.model.Book;
import javax.swing.text.JTextComponent;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.sql.DataSource;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class QLDich extends javax.swing.JPanel {
    
    private static final String CONFIG_API_KEY_MESSAGE = "API Key đã được load từ config";
    private static final String PROVIDER_CHATGPT = "chatgpt";
    private static final String PROVIDER_CLAUDE = "claude";
    private static final String PROVIDER_DEEPSEEK = "deepseek";
    private static final String PROVIDER_GROK = "grok";
    private static final String PROVIDER_GEMINI = "gemini";

    // Configuration and Services
    private AppConfig config;
    private FileService fileService;
    private TranslationService translationService;
    private ExecutorService backgroundExecutor;
    

    // Translation Data
    private List<File> selectedChapterFiles;
    private String guidelines = "";
    private String nameTable = "";
    private String[] availableModels;
    private String selectedModel = "";
    private String selectedProvider = PROVIDER_DEEPSEEK;
    private Map<String, ProviderInfo> providerInfoMap;
    private Map<String, String> providerApiKeys;
    private Map<String, String> providerModels;
    private String apiKey = "";
    
    // UI Components for progress
    private JProgressBar progressBar;
    private JTextArea logArea;
    private JScrollPane logScrollPane;
    private JLabel statusLabel;
    private JButton btnCancel;
    private JPanel progressPanel;

    // Button management
    private List<JButton> controlButtons;
    private Map<JButton, Color> originalButtonColors;    
    
    // State tracking
    private boolean isTranslating = false;
    private long currentBookId = -1;

    public QLDich() {
        initComponents();
        initializeEnhancements();
        setupEventHandlers();
        setupDragAndDrop();
        loadConfiguration();
        loadBooksIntoCombo();
        setupBookSelectionHandler();
        setupKeyboardShortcuts();
    }

    private void setupKeyboardShortcuts() {
     // Setup keyboard shortcuts cho tất cả text fields
     TextFieldKeyboardUtils.setupEnhancedTextComponent(txtapiKey);
     TextFieldKeyboardUtils.setupEnhancedTextComponent(txtapiVersion);
     TextFieldKeyboardUtils.setupEnhancedTextComponent(txtBangTen);
     //TextFieldKeyboardUtils.setupEnhancedTextComponent(txtDich);
    } 

    private void prepareControlButtons() {
        controlButtons = new ArrayList<>();
        originalButtonColors = new HashMap<>();

        controlButtons.add(btnYeuCau1);
        controlButtons.add(btnRefresh1);
        controlButtons.add(btnChuong);
        controlButtons.add(btnRefresh3);
        controlButtons.add(btnBangTen);
        controlButtons.add(btnRefresh2);
        controlButtons.add(btnResume);
        controlButtons.add(btnStart);

        for (JButton btn : controlButtons) {
            originalButtonColors.put(btn, btn.getBackground());
        }
    }    
    /**
     * Setup drag and drop functionality for all text fields
     */
    private void setupDragAndDrop() {
        // Setup drag and drop for guidelines file (txtYeuCau1)
        setupFileDropTarget(txtYeuCau1, "guidelines", 
            "Kéo thả file yêu cầu dịch (.txt) vào đây", 
            this::handleGuidelinesFileDrop);
        
        // Setup drag and drop for name table file (txtBangTen)
        setupFileDropTarget(txtBangTen, "nametable", 
            "Kéo thả file bảng tên (.txt/.csv) vào đây", 
            this::handleNameTableFileDrop);
        
        // Setup drag and drop for chapter files (txtDanhSachChuong)
        setupMultipleFileDropTarget(txtDanhSachChuong, "chapters", 
            "Kéo thả các file chương (C1.txt, C2.txt, ...) vào đây", 
            this::handleChapterFilesDrop);
    }

    /**
     * Setup drop target for single file
     */
    private void setupFileDropTarget(JTextField textField, String type, String hintText, FileDropHandler handler) {
        // Set hint text
        if (textField.getText().isEmpty() || textField.getForeground().equals(Color.GRAY)) {
            textField.setText(hintText);
            textField.setForeground(Color.GRAY);
            textField.setFont(textField.getFont().deriveFont(Font.ITALIC));
        }
        
        // Create drop target
        new DropTarget(textField, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (isDragValid(dtde)) {
                    textField.setBackground(new Color(230, 255, 230)); // Light green
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    textField.setBackground(new Color(255, 230, 230)); // Light red
                    dtde.rejectDrag();
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                // Keep the visual feedback
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
                // No action needed
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                textField.setBackground(Color.WHITE); // Reset background
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                textField.setBackground(Color.WHITE); // Reset background
                
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    
                    Transferable transferable = dtde.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        
                        if (!files.isEmpty()) {
                            File file = files.get(0); // Take first file for single file drop
                            handler.handleFileDrop(file);
                        }
                    }
                    
                    dtde.dropComplete(true);
                } catch (Exception e) {
                    logMessage("Lỗi khi xử lý file drag & drop: " + e.getMessage());
                    dtde.dropComplete(false);
                }
            }
        });
        
        // Add focus listener to clear hint text
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(hintText) && textField.getForeground().equals(Color.GRAY)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                    textField.setFont(textField.getFont().deriveFont(Font.PLAIN));
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().trim().isEmpty()) {
                    textField.setText(hintText);
                    textField.setForeground(Color.GRAY);
                    textField.setFont(textField.getFont().deriveFont(Font.ITALIC));
                }
            }
        });
    }

    /**
     * Setup drop target for multiple files
     */
    private void setupMultipleFileDropTarget(JTextComponent textField, String type, String hintText, MultipleFileDropHandler handler) {
        // Set hint text
        if (textField.getText().isEmpty() || textField.getForeground().equals(Color.GRAY)) {
            textField.setText(hintText);
            textField.setForeground(Color.GRAY);
            textField.setFont(textField.getFont().deriveFont(Font.ITALIC));
        }
        
        // Create drop target
        new DropTarget(textField, new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (isDragValid(dtde)) {
                    textField.setBackground(new Color(230, 255, 230)); // Light green
                    dtde.acceptDrag(DnDConstants.ACTION_COPY);
                } else {
                    textField.setBackground(new Color(255, 230, 230)); // Light red
                    dtde.rejectDrag();
                }
            }

            @Override
            public void dragOver(DropTargetDragEvent dtde) {
                // Keep the visual feedback
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent dtde) {
                // No action needed
            }

            @Override
            public void dragExit(DropTargetEvent dte) {
                textField.setBackground(Color.WHITE); // Reset background
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {
                textField.setBackground(Color.WHITE); // Reset background
                
                try {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY);
                    
                    Transferable transferable = dtde.getTransferable();
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        @SuppressWarnings("unchecked")
                        List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                        
                        if (!files.isEmpty()) {
                            handler.handleFilesDrop(files);
                        }
                    }
                    
                    dtde.dropComplete(true);
                } catch (Exception e) {
                    logMessage("Lỗi khi xử lý files drag & drop: " + e.getMessage());
                    dtde.dropComplete(false);
                }
            }
        });
        
        // Add focus listener to clear hint text
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(hintText) && textField.getForeground().equals(Color.GRAY)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                    textField.setFont(textField.getFont().deriveFont(Font.PLAIN));
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().trim().isEmpty()) {
                    textField.setText(hintText);
                    textField.setForeground(Color.GRAY);
                    textField.setFont(textField.getFont().deriveFont(Font.ITALIC));
                }
            }
        });
    }

    /**
     * Check if drag operation contains valid files
     */
    private boolean isDragValid(DropTargetDragEvent dtde) {
        return dtde.getTransferable().isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    /**
     * Handle guidelines file drop
     */
    private void handleGuidelinesFileDrop(File file) {
        try {
            // Validate file
            if (!file.exists() || !file.isFile()) {
                showError("File không tồn tại: " + file.getName());
                return;
            }

            if (!file.getName().toLowerCase().endsWith(".txt")) {
                showError("File yêu cầu dịch phải là file .txt");
                return;
            }

            // Read file content
            guidelines = fileService.readTextFile(file);
            
            // Update UI
            txtYeuCau1.setText("✓ " + file.getName() + " (" + guidelines.length() + " ký tự)");
            txtYeuCau1.setForeground(new Color(0, 128, 0)); // Dark green
            txtYeuCau1.setFont(txtYeuCau1.getFont().deriveFont(Font.PLAIN));
            
            //logMessage("✓ Đã load file yêu cầu: " + file.getName());
            showInfo("Đã load file yêu cầu dịch: " + file.getName());
            
        } catch (IOException e) {
            showError("Không thể đọc file yêu cầu: " + e.getMessage());
        }
    }

    /**
     * Handle name table file drop
     */
    private void handleNameTableFileDrop(File file) {
        try {
            // Validate file
            if (!file.exists() || !file.isFile()) {
                showError("File không tồn tại: " + file.getName());
                return;
            }

            String fileName = file.getName().toLowerCase();
            if (!fileName.endsWith(".txt") && !fileName.endsWith(".csv")) {
                showError("File bảng tên phải là file .txt hoặc .csv");
                return;
            }

            // Read and process file content
            String rawContent = fileService.readTextFile(file);
            if (fileName.endsWith(".csv")) {
                nameTable = CsvNameTableFormatter.toBlock(rawContent, true);
            } else {
                nameTable = rawContent;
            }
            
            // Update UI
            txtBangTen.setText("✓ " + file.getName() + " (" + nameTable.length() + " ký tự)");
            txtBangTen.setForeground(new Color(0, 128, 0)); // Dark green
            txtBangTen.setFont(txtBangTen.getFont().deriveFont(Font.PLAIN));
            
            //logMessage("✓ Đã load file bảng tên: " + file.getName());
            showInfo("Đã load file bảng tên: " + file.getName());
            
        } catch (IOException e) {
            showError("Không thể đọc file bảng tên: " + e.getMessage());
        }
    }

    /**
     * Handle chapter files drop
     */
    private void handleChapterFilesDrop(List<File> files) {
        try {
            // Filter valid chapter files
            List<File> validChapterFiles = new ArrayList<>();
            List<String> invalidFiles = new ArrayList<>();
            
            for (File file : files) {
                if (file.isFile() && FileUtils.isValidChapterFile(file)) {
                    validChapterFiles.add(file);
                } else {
                    invalidFiles.add(file.getName());
                }
            }
            
            // Show invalid files warning if any
            if (!invalidFiles.isEmpty()) {
                showWarning("Các file không hợp lệ sẽ bị bỏ qua:\n" + String.join("\n", invalidFiles));
            }
            
            if (validChapterFiles.isEmpty()) {
                showError("Không có file chương hợp lệ nào được tìm thấy.\n" +
                         "File chương phải có tên dạng: C1.txt, C2.txt, C3.txt, ...");
                return;
            }
            
            // Validate chapter files
            FileService.FileValidationResult validation = fileService.validateChapterFiles(validChapterFiles);
            
            if (!validation.isValid()) {
                showError("Files chương không hợp lệ:\n" + validation.getErrorsAsString());
                return;
            }
            
            if (validation.hasWarnings()) {
                int result = JOptionPane.showConfirmDialog(this,
                    "Có cảnh báo:\n" + validation.getWarningsAsString() + "\n\nTiếp tục?",
                    "Cảnh báo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Set selected chapter files
            selectedChapterFiles = validation.getValidFiles();

            // Update UI - display all uploaded filenames
            String fileNames = selectedChapterFiles.stream()
                    .map(File::getName)
                    .collect(Collectors.joining("\n"));
            txtDanhSachChuong.setText(fileNames);
            txtDanhSachChuong.setForeground(new Color(0, 128, 0)); // Dark green
            txtDanhSachChuong.setFont(txtDanhSachChuong.getFont().deriveFont(Font.PLAIN));
            txtDanhSachChuong.setCaretPosition(0);            
            
            //logMessage("✓ Đã load " + selectedChapterFiles.size() + " file chương");
            showInfo("Đã load " + selectedChapterFiles.size() + " file chương hợp lệ");
            
            // Update cost estimate
            updateCostEstimate();
            
        } catch (Exception e) {
            showError("Lỗi khi xử lý files chương: " + e.getMessage());
        }
    }

    // ===================================================================
    // ENHANCED UI METHODS - Modified to work with drag & drop
    // ===================================================================

    /**
     * Enhanced file selection methods that work alongside drag & drop
     */
    public void selectGuidelinesFile() {
        File file = fileService.selectTextFile(this, "Chọn file yêu cầu dịch");
        if (file != null) {
            handleGuidelinesFileDrop(file);
        }
    }
    
    public void selectNameTableFile() {
        File file = fileService.selectTextFile(this, "Chọn file bảng tên");
        if (file != null) {
            handleNameTableFileDrop(file);
        }
    }
    
    public void selectChapterFiles() {
        List<File> files = fileService.selectChapterFiles(this);
        if (!files.isEmpty()) {
            handleChapterFilesDrop(files);
        }
    }

    /**
     * Enhanced clear functionality for text fields
     */
    private void clearGuidelinesField() {
        guidelines = "";
        txtYeuCau1.setText("Kéo thả file yêu cầu dịch (.txt) vào đây");
        txtYeuCau1.setForeground(Color.GRAY);
        txtYeuCau1.setFont(txtYeuCau1.getFont().deriveFont(Font.ITALIC));
        logMessage("Đã xóa file yêu cầu dịch");
    }

    private void clearNameTableField() {
        nameTable = "";
        txtBangTen.setText("Kéo thả file bảng tên (.txt/.csv) vào đây");
        txtBangTen.setForeground(Color.GRAY);
        txtBangTen.setFont(txtBangTen.getFont().deriveFont(Font.ITALIC));
        logMessage("Đã xóa file bảng tên");
    }



    private void onBookSelected() {
        Book selected = (Book) cbTruyen.getSelectedItem();
        if (selected != null) {
            currentBookId = selected.getId();
            guidelines = selected.getGuidelines() != null ? selected.getGuidelines() : "";
            nameTable = selected.getNameTable() != null ? selected.getNameTable() : "";
            updateGuidelinesFieldDisplay();
            updateNameTableFieldDisplay();
        }
    }
    private void clearChapterFilesField() {
        selectedChapterFiles = null;
        txtDanhSachChuong.setText("Kéo thả các file chương (C1.txt, C2.txt, ...) vào đây");
        txtDanhSachChuong.setForeground(Color.GRAY);
        txtDanhSachChuong.setFont(txtDanhSachChuong.getFont().deriveFont(Font.ITALIC));
        logMessage("Đã xóa danh sách files chương");
    }

    // ===================================================================
    // FUNCTIONAL INTERFACES FOR DRAG & DROP HANDLERS
    // ===================================================================

    @FunctionalInterface
    private interface FileDropHandler {
        void handleFileDrop(File file);
    }

    @FunctionalInterface
    private interface MultipleFileDropHandler {
        void handleFilesDrop(List<File> files);
    }
    
        private static class ProviderInfo {
        final String id;
        final String displayName;
        final Supplier<String[]> modelsSupplier;
        final Supplier<String> defaultModelSupplier;
        final String versionPlaceholder;
        final String apiKeyPlaceholder;
        final boolean apiSupported;

        ProviderInfo(String id, String displayName,
                     Supplier<String[]> modelsSupplier,
                     Supplier<String> defaultModelSupplier,
                     String versionPlaceholder,
                     String apiKeyPlaceholder,
                     boolean apiSupported) {
            this.id = id;
            this.displayName = displayName;
            this.modelsSupplier = modelsSupplier;
            this.defaultModelSupplier = defaultModelSupplier;
            this.versionPlaceholder = versionPlaceholder;
            this.apiKeyPlaceholder = apiKeyPlaceholder;
            this.apiSupported = apiSupported;
        }

        String[] getSuggestedModels() {
            if (modelsSupplier == null) {
                return new String[0];
            }
            String[] models = modelsSupplier.get();
            return models != null ? models : new String[0];
        }

        String getDefaultModel() {
            if (defaultModelSupplier == null) {
                return "";
            }
            String defaultModel = defaultModelSupplier.get();
            return defaultModel != null ? defaultModel : "";
        }
    }

    // ===================================================================
    // EXISTING METHODS - Keep all the existing functionality
    // ===================================================================
/*
    private void initializeEnhancements() {
        config = AppConfig.getInstance();
        fileService = new FileService();
        backgroundExecutor = Executors.newSingleThreadExecutor();
        setupProviderCombo();
        ProviderInfo initialProvider = getSelectedProviderInfo();
        if (initialProvider != null) {
            applyProviderSelection(initialProvider, true);
        }  
        
        initializeProviderConfigs();
        // Setup progress tracking UI
        setupProgressUI();
        
        // Configure existing text fields for better UX
        setupTextFieldBehavior();
        
        // Load initial values
        loadInitialValues();
        
        UIUtils.enableTextComponentShortcuts(this);
    }
*/
    private void initializeEnhancements() {
        config = AppConfig.getInstance();
        fileService = new FileService();
        backgroundExecutor = Executors.newSingleThreadExecutor();

        // Initialize provider configurations - THÊM DÒNG NÀY
        initializeProviderConfigs();

        // Setup progress tracking UI
        setupProgressUI();

        // Setup provider combo box - THÊM DÒNG NÀY
        setupProviderCombo();

        // Configure existing text fields for better UX
        setupTextFieldBehavior();

        // Prepare control buttons - THÊM DÒNG NÀY NẾU CHƯA CÓ
        prepareControlButtons();

        // Load initial values
        loadInitialValues();

        UIUtils.enableTextComponentShortcuts(this);
    }        
    
    private void initializeProviderConfigs() {
        providerInfoMap = new LinkedHashMap<>();
        providerApiKeys = new HashMap<>();
        providerModels = new HashMap<>();

        providerInfoMap.put(PROVIDER_CHATGPT, new ProviderInfo(
            PROVIDER_CHATGPT,
            "ChatGPT",
            () -> config.getAvailableModels(),
            () -> config.getDefaultModel(),
            "Nhập phiên bản ChatGPT (VD: gpt-4o, gpt-4-turbo)",
            "Nhập OpenAI API Key (sk-...)",
            true
        ));

        providerInfoMap.put(PROVIDER_CLAUDE, new ProviderInfo(
            PROVIDER_CLAUDE,
            "Claude",
            () -> new String[]{"claude-3-5-sonnet", "claude-3-opus"},
            () -> "claude-3-5-sonnet",
            "Nhập phiên bản Claude (VD: claude-3-5-sonnet)",
            "Nhập Claude API Key (sk-ant-...)",
            false
        ));

        providerInfoMap.put(PROVIDER_DEEPSEEK, new ProviderInfo(
            PROVIDER_DEEPSEEK,
            "DeepSeek",
            () -> new String[]{"deepseek-reasoner", "deepseek-coder"},
            () -> "deepseek-reasoner",
            "Nhập phiên bản DeepSeek (VD: deepseek-chat)",
            "Nhập DeepSeek API Key",
            true
        ));

        providerInfoMap.put(PROVIDER_GROK, new ProviderInfo(
            PROVIDER_GROK,
            "Grok",
            () -> new String[]{"grok-beta"},
            () -> "grok-beta",
            "Nhập phiên bản Grok (VD: grok-beta)",
            "Nhập Grok API Key",
            false
        ));

        providerInfoMap.put(PROVIDER_GEMINI, new ProviderInfo(
            PROVIDER_GEMINI,
            "Gemini",
            () -> new String[]{"gemini-1.5-pro", "gemini-1.5-flash"},
            () -> "gemini-1.5-pro",
            "Nhập phiên bản Gemini (VD: gemini-1.5-pro)",
            "Nhập Gemini API Key",
            false
        ));
    }

    private void setupProviderCombo() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (ProviderInfo info : providerInfoMap.values()) {
            model.addElement(info.displayName);
        }
        cbapi.setModel(model);
        ProviderInfo info = getSelectedProviderInfo();
        if (info != null) {
            cbapi.setSelectedItem(info.displayName);
        }
    }

    private ProviderInfo getSelectedProviderInfo() {
        return providerInfoMap.get(selectedProvider);
    }

    private ProviderInfo getProviderInfoByDisplayName(String displayName) {
        if (displayName == null) {
            return null;
        }
        for (ProviderInfo info : providerInfoMap.values()) {
            if (info.displayName.equals(displayName)) {
                return info;
            }
        }
        return null;
    }

    private void applyProviderSelection(ProviderInfo info, boolean initialSelection) {
        if (info == null) {
            return;
        }

        availableModels = info.getSuggestedModels();
        updatePlaceholderText(txtapiVersion, info.versionPlaceholder);
        updatePlaceholderText(txtapiKey, info.apiKeyPlaceholder);

        String storedModel = providerModels.get(info.id);
        if (storedModel == null || storedModel.trim().isEmpty()) {
            storedModel = info.getDefaultModel();
        }

        if (storedModel != null && !storedModel.trim().isEmpty()) {
            selectedModel = storedModel;
            setActualText(txtapiVersion, storedModel);
            txtapiVersion.setForeground(Color.BLACK);
            providerModels.put(info.id, storedModel);
        } else {
            selectedModel = "";
            setPlaceholderText(txtapiVersion, info.versionPlaceholder);
        }

        String storedKey = providerApiKeys.getOrDefault(info.id, "");
        apiKey = storedKey != null ? storedKey : "";

        if (!apiKey.isEmpty()) {
            setActualText(txtapiKey, apiKey);
            txtapiKey.setForeground(Color.BLACK);
        } else {
            setPlaceholderText(txtapiKey, info.apiKeyPlaceholder);
        }
        txtapiKey.setBackground(Color.WHITE);

        if (!initialSelection) {
            logMessage("Đã chuyển sang nhà cung cấp: " + info.displayName);
            if (availableModels.length > 0) {
                logMessage("Gợi ý model: " + String.join(", ", availableModels));
            }
            if (!info.apiSupported) {
                logMessage(info.displayName + " hiện chưa được hỗ trợ dịch tự động.");
            }
            updateCostEstimate();
        }
    }

    private void updatePlaceholderText(JTextField textField, String placeholder) {
        if (textField == null || placeholder == null) {
            return;
        }
        boolean shouldApply = isPlaceholderActive(textField) || getActualText(textField).isEmpty();
        textField.putClientProperty("placeholder", placeholder);
        if (shouldApply) {
            setPlaceholderText(textField, placeholder);
        }
    }

    private boolean isPlaceholderActive(JTextField textField) {
        String placeholder = (String) textField.getClientProperty("placeholder");
        if (placeholder == null) {
            return false;
        }
        return isPlaceholderActive(textField, placeholder);
    }

    private void storeCurrentProviderValues() {
        ProviderInfo info = getSelectedProviderInfo();
        if (info == null) {
            return;
        }

        String currentModel = getActualText(txtapiVersion).trim();
        if (!currentModel.isEmpty()) {
            providerModels.put(info.id, currentModel);
        }

        String currentKey = getActualText(txtapiKey).trim();
        if (!currentKey.isEmpty() && !CONFIG_API_KEY_MESSAGE.equals(currentKey)) {
            providerApiKeys.put(info.id, currentKey);
        }
    }

    private boolean isProviderSupportedForTranslation() {
        ProviderInfo info = getSelectedProviderInfo();
        return info != null && info.apiSupported;
    }

    private AIClient createClientForProvider() {
        switch (selectedProvider) {
            case PROVIDER_CHATGPT:
                return new OpenAIClient(apiKey);
            case PROVIDER_DEEPSEEK:
                return new DeepSeekClient(apiKey);
            default:
                ProviderInfo info = getSelectedProviderInfo();
                String name = info != null ? info.displayName : selectedProvider;
                throw new UnsupportedOperationException(
                    "Nhà cung cấp " + name + " chưa được hỗ trợ");
        }
    }
    
    private void setupProgressUI() {
        // Create progress panel
        progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Tiến trình dịch", 
            TitledBorder.LEFT, TitledBorder.TOP));
        progressPanel.setPreferredSize(new Dimension(400, 200));
        
        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Sẵn sàng");
        
        // Status label
        statusLabel = new JLabel("Trạng thái: Sẵn sàng");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        // Cancel button
        btnCancel = new JButton("Hủy");
        btnCancel.setVisible(false);
        btnCancel.setBackground(new Color(255, 102, 102));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.addActionListener(e -> cancelTranslation());
        
        // Log area
        logArea = new JTextArea(8, 40);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        logArea.setBackground(new Color(248, 248, 248));
        logScrollPane = new JScrollPane(logArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Layout progress panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(progressBar, BorderLayout.CENTER);
        topPanel.add(btnCancel, BorderLayout.EAST);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(statusLabel);
        
        progressPanel.add(topPanel, BorderLayout.NORTH);
        progressPanel.add(statusPanel, BorderLayout.CENTER);
        progressPanel.add(logScrollPane, BorderLayout.SOUTH);
        
        // Add progress panel to main panel
        add(progressPanel);
    }
    
    private void setupTextFieldBehavior() {
        ProviderInfo providerInfo = getSelectedProviderInfo();
        String versionPlaceholder = providerInfo != null
            ? providerInfo.versionPlaceholder
            : "Nhập phiên bản AI";
        String apiKeyPlaceholder = providerInfo != null
            ? providerInfo.apiKeyPlaceholder
            : "Nhập API Key";        

        // Setup model field
        addPlaceholderBehavior(txtapiVersion, versionPlaceholder);

        // Add model validation
        txtapiVersion.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateAndSetModel();
            }
        });

        // Add action listener for Enter key
        txtapiVersion.addActionListener(e -> validateAndSetModel());

        // Setup API key field
        addPlaceholderBehavior(txtapiKey, apiKeyPlaceholder);

        // Add API key validation
        txtapiKey.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateAndSetApiKey();
            }
        });

        // Add action listener for Enter key
        txtapiKey.addActionListener(e -> validateAndSetApiKey());
                txtapiKey.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                String placeholder = (String) txtapiKey.getClientProperty("placeholder");
                if (placeholder != null && isPlaceholderActive(txtapiKey, placeholder)) {
                    clearPlaceholderText(txtapiKey);
                }
            }
        });
/*
        // Enable standard copy/paste shortcuts explicitly
        enableStandardShortcuts(txtChatgpt);
        enableStandardShortcuts(txtChatgpt1);
    }

    private void enableStandardShortcuts(JTextField textField) {
        // Ensure standard key bindings are active
        InputMap inputMap = textField.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = textField.getActionMap();

        // Copy, Paste, Cut, Select All shortcuts
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK), "copy");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK), "paste");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK), "cut");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), "select-all");
        */
    }

    private void addPlaceholderBehavior(JTextField textField, String placeholder) {
        textField.putClientProperty("placeholder", placeholder);

        if (textField.getText().trim().isEmpty()) {
            setPlaceholderText(textField, placeholder);
        }

        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (isPlaceholderActive(textField, placeholder)) {
                    clearPlaceholderText(textField);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().trim().isEmpty()) {
                    setPlaceholderText(textField, placeholder);
                }
            }
        });
    }

    private boolean isPlaceholderActive(JTextField textField, String placeholder) {
        return placeholder.equals(textField.getText()) && 
               textField.getForeground().equals(Color.GRAY);
    }

    private void setPlaceholderText(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);
        textField.setFont(textField.getFont().deriveFont(Font.ITALIC));
    }

    private void clearPlaceholderText(JTextField textField) {
        textField.setText("");
        textField.setForeground(Color.BLACK);
        textField.setFont(textField.getFont().deriveFont(Font.PLAIN));
    }

    private String getActualText(JTextField textField) {
        String placeholder = (String) textField.getClientProperty("placeholder");
        if (isPlaceholderActive(textField, placeholder)) {
            return "";
        }
        return textField.getText();
    }

    private void setActualText(JTextField textField, String text) {
        String placeholder = (String) textField.getClientProperty("placeholder");

        if (text == null || text.trim().isEmpty()) {
            setPlaceholderText(textField, placeholder);
        } else {
            textField.setText(text);
            textField.setForeground(Color.BLACK);
            textField.setFont(textField.getFont().deriveFont(Font.PLAIN));
        }
    }
    
    private void validateAndSetModel() {
        ProviderInfo info = getSelectedProviderInfo();
        String providerId = info != null ? info.id : selectedProvider;
        String modelText = getActualText(txtapiVersion).trim();

        if (modelText.isEmpty()) {
            String defaultModel = info != null ? info.getDefaultModel() : "";
            if (defaultModel != null && !defaultModel.isEmpty()) {
                selectedModel = defaultModel;
                setActualText(txtapiVersion, selectedModel);
                txtapiVersion.setForeground(Color.BLACK);
                providerModels.put(providerId, selectedModel);
                logMessage("Sử dụng model mặc định: " + selectedModel);
            } else {
                selectedModel = "";
                if (info != null) {
                    setPlaceholderText(txtapiVersion, info.versionPlaceholder);
                }
            }
            if (isProviderSupportedForTranslation()) {
                updateCostEstimate();
            }
            return;
        }

        if (isValidModel(modelText)) {
            selectedModel = modelText;
            providerModels.put(providerId, selectedModel);
            txtapiVersion.setForeground(Color.BLACK);
            if (!isKnownModel(modelText)) {
                logMessage("Model tùy chỉnh: " + selectedModel);
            } else {
                logMessage("Model được chọn: " + selectedModel);
            }
            if (isProviderSupportedForTranslation()) {
                updateCostEstimate();
            }
        } else {
            txtapiVersion.setForeground(Color.RED);
            String defaultModel = info != null ? info.getDefaultModel() : "";
            if (defaultModel != null && !defaultModel.isEmpty()) {
                showWarning("Model không hợp lệ. Sử dụng mặc định: " + defaultModel);
                selectedModel = defaultModel;
                setActualText(txtapiVersion, selectedModel);
                txtapiVersion.setForeground(Color.BLACK);
                providerModels.put(providerId, selectedModel);
            } else {
                showWarning("Model không hợp lệ. Vui lòng nhập lại.");
            }
        }
    }
    
    private void validateAndSetApiKey() {
        ProviderInfo info = getSelectedProviderInfo();
        String providerId = info != null ? info.id : selectedProvider;
        String keyText = getActualText(txtapiKey).trim();

        if (CONFIG_API_KEY_MESSAGE.equals(keyText)) {
            txtapiKey.setForeground(Color.BLUE);
            return;
        }

        if (keyText.isEmpty()) {
            apiKey = "";
            return;
        }

        if (isValidApiKey(keyText)) {
            apiKey = keyText;
            txtapiKey.setForeground(Color.BLACK);
            providerApiKeys.put(providerId, apiKey);
            String providerName = info != null ? info.displayName : "API";
            logMessage("API Key " + providerName + " đã được cập nhật");
            if (isProviderSupportedForTranslation()) {
                testApiConnection();
            }
        } else {
            txtapiKey.setForeground(Color.RED);
            String hint = getApiKeyValidationHint(info);
            showWarning("API Key không hợp lệ" + (hint.isEmpty() ? "." : ": " + hint));
            apiKey = "";
        }
    }
    
    private void testApiConnection() {
        if (apiKey.isEmpty()) return;
        
        backgroundExecutor.submit(() -> {
            try {
                AIClient testClient = createClientForProvider();
                boolean connected = testClient.testConnection();
                
                SwingUtilities.invokeLater(() -> {
                    if (connected) {
                        logMessage("✓ Kết nối API thành công");
                        txtapiKey.setBackground(new Color(230, 255, 230));
                    } else {
                        logMessage("✗ Không thể kết nối API");
                        txtapiKey.setBackground(new Color(255, 230, 230));
                    }
                });
            } catch (UnsupportedOperationException e) {
                SwingUtilities.invokeLater(() -> logMessage("✗ " + e.getMessage()));                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    logMessage("✗ Lỗi kết nối API: " + e.getMessage());
                    txtapiKey.setBackground(new Color(255, 230, 230));
                });
            }
        });
    }

    private boolean isKnownModel(String model) {
        if (availableModels == null) {
            return false;
        }
        for (String m : availableModels) {
            if (model.equals(m) || model.startsWith(m + "-")) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isValidModel(String model) {
        if (model == null || model.isEmpty()) {
            return false;
        }
        switch (selectedProvider) {
            case PROVIDER_CHATGPT:
                return model.startsWith("gpt-");
            case PROVIDER_CLAUDE:
                return model.startsWith("claude");
            case PROVIDER_DEEPSEEK:
                return model.startsWith("deepseek");
            case PROVIDER_GROK:
                return model.startsWith("grok");
            case PROVIDER_GEMINI:
                return model.startsWith("gemini");
            default:
                return true;
        }
    }
    
    private boolean isValidApiKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        switch (selectedProvider) {
            case PROVIDER_CHATGPT:
                return key.startsWith("sk-") && key.length() > 20;
            case PROVIDER_CLAUDE:
                return key.startsWith("sk-ant-") && key.length() > 20;
            case PROVIDER_DEEPSEEK:
                return key.length() > 20;
            case PROVIDER_GROK:
                return key.startsWith("gsk_") && key.length() > 20;
            case PROVIDER_GEMINI:
                return key.startsWith("AI") && key.length() > 10;
            default:
                return key.length() > 10;
        }
    }

    private String getApiKeyValidationHint(ProviderInfo info) {
        if (info == null) {
            return "";
        }
        switch (info.id) {
            case PROVIDER_CHATGPT:
                return "OpenAI key thường bắt đầu bằng 'sk-' và có ít nhất 20 ký tự.";
            case PROVIDER_CLAUDE:
                return "Claude key thường bắt đầu bằng 'sk-ant-'.";
            case PROVIDER_DEEPSEEK:
                return "DeepSeek key phải có độ dài tối thiểu 20 ký tự.";
            case PROVIDER_GROK:
                return "Grok key thường bắt đầu bằng 'gsk_'.";
            case PROVIDER_GEMINI:
                return "Gemini key thường bắt đầu bằng 'AI'.";
            default:
                return "";
        }
    }
    
        private void loadConfiguration() {
        try {
            String configApiKey = config.getOpenAIApiKey();
            if (!configApiKey.isEmpty()) {
                providerApiKeys.put(PROVIDER_CHATGPT, configApiKey);
                if (PROVIDER_CHATGPT.equals(selectedProvider)) {
                    apiKey = configApiKey;
                    setActualText(txtapiKey, CONFIG_API_KEY_MESSAGE);
                    txtapiKey.setForeground(Color.BLUE);
                    txtapiKey.setBackground(Color.WHITE);
                }
            }

            String configModel = config.getOpenAIModel();
            if (!configModel.isEmpty()) {
                providerModels.put(PROVIDER_CHATGPT, configModel);
            }

            if (PROVIDER_CHATGPT.equals(selectedProvider)) {
                if (configModel.isEmpty() || !isKnownModel(configModel)) {
                    selectedModel = config.getDefaultModel();
                    setActualText(txtapiVersion, selectedModel);
                    txtapiVersion.setForeground(Color.BLACK);
                    providerModels.put(PROVIDER_CHATGPT, selectedModel);
                    logMessage("Model từ config không hợp lệ, sử dụng mặc định: " + selectedModel);
                } else {
                    selectedModel = configModel;
                    setActualText(txtapiVersion, configModel);
                    txtapiVersion.setForeground(Color.BLACK);
                }
            }

        } catch (Exception e) {
            logMessage("Không thể load configuration: " + e.getMessage());
        }
    }
    
    private void loadInitialValues() {
        setTranslationUIState(false);
        logMessage("QLDich panel đã sẵn sàng");
        ProviderInfo info = getSelectedProviderInfo();
        if (info != null) {
            logMessage("Nhà cung cấp mặc định: " + info.displayName);
            if (availableModels != null && availableModels.length > 0) {
                logMessage("Các model gợi ý: " + String.join(", ", availableModels));
            }
            if (PROVIDER_CHATGPT.equals(info.id)) {
                logMessage("Model mặc định: " + selectedModel);
            } else if (!info.apiSupported) {
                logMessage(info.displayName + " hiện chưa được hỗ trợ dịch tự động.");
            }
        }

        // Show help message
        logMessage("Hướng dẫn:");
        logMessage("1. Nhập API Key và chọn model");
        logMessage("2. Kéo thả file yêu cầu dịch hoặc nhấn button để chọn");
        logMessage("3. Kéo thả file bảng tên hoặc nhấn button để chọn");
        logMessage("4. Kéo thả các file chương hoặc nhấn button để chọn");
        logMessage("5. Nhấn Start để bắt đầu dịch");
    }
    
    private void setupEventHandlers() {
        // Connect existing buttons to their action methods
        btnYeuCau1.addActionListener(e -> selectGuidelinesFile());
        btnBangTen.addActionListener(e -> selectNameTableFile());
        btnChuong.addActionListener(e -> selectChapterFiles());
        btnStart.addActionListener(e -> startTranslation());
        btnResume.addActionListener(e -> resumeTranslation());
        
        cbTruyen.addActionListener(e -> onBookSelected());
        cbapi.addActionListener(e -> onProviderSelectionChanged());
        // Add context menu for clear functionality
        setupContextMenus();
    }
    
    private void onProviderSelectionChanged() {
        storeCurrentProviderValues();
        ProviderInfo info = getProviderInfoByDisplayName((String) cbapi.getSelectedItem());
        if (info == null) {
            return;
        }
        selectedProvider = info.id;
        applyProviderSelection(info, false);
    } 
    
    /**
     * Setup context menus for text fields to allow clearing
     */
    private void setupContextMenus() {
        // Context menu for guidelines field
        JPopupMenu guidelinesMenu = new JPopupMenu();
        JMenuItem clearGuidelines = new JMenuItem("Xóa file yêu cầu");
        clearGuidelines.addActionListener(e -> clearGuidelinesField());
        guidelinesMenu.add(clearGuidelines);
        txtYeuCau1.setComponentPopupMenu(guidelinesMenu);
        
        // Context menu for name table field
        JPopupMenu nameTableMenu = new JPopupMenu();
        JMenuItem clearNameTable = new JMenuItem("Xóa file bảng tên");
        clearNameTable.addActionListener(e -> clearNameTableField());
        nameTableMenu.add(clearNameTable);
        txtBangTen.setComponentPopupMenu(nameTableMenu);
        
        // Context menu for chapter files field
        JPopupMenu chapterFilesMenu = new JPopupMenu();
        JMenuItem clearChapterFiles = new JMenuItem("Xóa danh sách files");
        clearChapterFiles.addActionListener(e -> clearChapterFilesField());
        chapterFilesMenu.add(clearChapterFiles);
        txtDanhSachChuong.setComponentPopupMenu(chapterFilesMenu);
    }
    private void loadBooksIntoCombo() {
        try {
            DataSource ds = createDataSource();
            BookDao bookDao = new BookDao(ds);
            List<Book> books = bookDao.getAllBooks();
            javax.swing.JComboBox combo = (javax.swing.JComboBox) cbTruyen;
            combo.removeAllItems();
            for (Book b : books) {
                combo.addItem(b);
            }
            combo.setRenderer(new javax.swing.DefaultListCellRenderer() {
                @Override
                public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Book) {
                        Book book = (Book) value;
                        setText(book.getId() + " - " + book.getTitle());
                    }
                    return this;
                }
            });
        } catch (Exception e) {
            logMessage("Không thể tải danh sách truyện: " + e.getMessage());
        }
    }

    private void setupBookSelectionHandler() {
        ((javax.swing.JComboBox) cbTruyen).addActionListener(e -> {
            Object selected = ((javax.swing.JComboBox) cbTruyen).getSelectedItem();
            if (selected instanceof Book) {
                Book book = (Book) selected;
                currentBookId = book.getId();
                guidelines = book.getGuidelines() != null ? book.getGuidelines() : "";
                nameTable = book.getNameTable() != null ? book.getNameTable() : "";
                updateGuidelinesFieldDisplay();
                updateNameTableFieldDisplay();
            }
        });
    }

    private void updateGuidelinesFieldDisplay() {
        if (guidelines != null && !guidelines.isEmpty()) {
            txtYeuCau1.setText("DB (" + guidelines.length() + " ký tự)");
            txtYeuCau1.setForeground(new Color(0, 128, 0));
            txtYeuCau1.setFont(txtYeuCau1.getFont().deriveFont(Font.PLAIN));
        } else {
            txtYeuCau1.setText("Kéo thả file yêu cầu dịch (.txt) vào đây");
            txtYeuCau1.setForeground(Color.GRAY);
            txtYeuCau1.setFont(txtYeuCau1.getFont().deriveFont(Font.ITALIC));
        }
    }

    private void updateNameTableFieldDisplay() {
        if (nameTable != null && !nameTable.isEmpty()) {
            txtBangTen.setText("DB (" + nameTable.length() + " ký tự)");
            txtBangTen.setForeground(new Color(0, 128, 0));
            txtBangTen.setFont(txtBangTen.getFont().deriveFont(Font.PLAIN));
        } else {
            txtBangTen.setText("Kéo thả file bảng tên (.txt/.csv) vào đây");
            txtBangTen.setForeground(Color.GRAY);
            txtBangTen.setFont(txtBangTen.getFont().deriveFont(Font.ITALIC));
        }
    }    
    /**
     * Start translation process
     */
    private void startTranslation() {
        // Validate inputs
        if (!validateInputsForTranslation()) {
            return;
        }
        ProviderInfo providerInfo = getSelectedProviderInfo();
        String providerName = providerInfo != null ? providerInfo.displayName : selectedProvider;        
        
        // Show confirmation
        int result = JOptionPane.showConfirmDialog(this,
            "Bắt đầu dịch " + selectedChapterFiles.size() + " chương?\n" +
            "Nhà cung cấp: " + providerName + "\n" +
            "Model: " + selectedModel + "\n" +
            "Ước tính chi phí: $" + String.format("%.3f", estimateCost()),
            "Xác nhận dịch", JOptionPane.YES_NO_OPTION);
        
        if (result != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Start translationi
        setTranslationUIState(true);
        
        backgroundExecutor.submit(() -> {
            try {
                // Initialize services
                initializeTranslationServices();
                
                // Create book record
                ChapterDao chapterDao = new ChapterDao(createDataSource());
                //currentBookId = chapterDao.ensureBook("Translation_" + System.currentTimeMillis());
                if (currentBookId <= 0) {
                    currentBookId = chapterDao.ensureBook("Translation_" + System.currentTimeMillis());
                }                
                // Update book metadata
                chapterDao.updateBookMetadata(currentBookId, guidelines, nameTable, selectedModel);
                
                // Start translation
                translationService.processChapters(
                    currentBookId,
                    selectedChapterFiles,
                    guidelines,
                    nameTable,
                    selectedModel,
                    this::onProgressUpdate
                );
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    onProgressUpdate(new ProgressEvent(0, 0, 
                        "Lỗi: " + e.getMessage(), 
                        ProgressEvent.ProgressStatus.ERROR, null, e));
                    setTranslationUIState(false);
                });
            }
        });
    }
    
    /**
     * Resume translation process
     */
    private void resumeTranslation() {
        if (!isProviderSupportedForTranslation()) {
            ProviderInfo info = getSelectedProviderInfo();
            String providerName = info != null ? info.displayName : selectedProvider;
            showWarning(providerName + " chưa được hỗ trợ dịch tự động. Vui lòng chọn nhà cung cấp khác.");
            return;
        }

        if (apiKey.isEmpty()) {
            showError("Vui lòng nhập API Key trước khi resume");
            txtapiKey.requestFocus();
            return;
        }

        try {
            DataSource dataSource = createDataSource();
            ChapterDao chapterDao = new ChapterDao(dataSource);
            
            // Show resume dialog
            ChapterDao.ResumableBook selectedBook = ResumeDialog.showDialog((Frame) SwingUtilities.getWindowAncestor(this), chapterDao);
            
            if (selectedBook == null) {
                return; // User cancelled
            }
            
            currentBookId = selectedBook.id;
            guidelines = selectedBook.guidelines != null ? selectedBook.guidelines : "";
            nameTable = selectedBook.nameTable != null ? selectedBook.nameTable : "";
            selectedModel = selectedBook.modelUsed != null ? selectedBook.modelUsed : config.getDefaultModel();
            
            
            providerModels.put(selectedProvider, selectedModel);


            setTranslationUIState(true);
            
            backgroundExecutor.submit(() -> {
                try {
                    if (translationService == null) {
                        initializeTranslationServices();
                    }
                    
                    translationService.resumeTranslation(
                        currentBookId,
                        guidelines,
                        nameTable,
                        selectedModel,
                        this::onProgressUpdate
                    );
                    
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        onProgressUpdate(new ProgressEvent(0, 0, 
                            "Lỗi resume: " + e.getMessage(), 
                            ProgressEvent.ProgressStatus.ERROR, null, e));
                        setTranslationUIState(false);
                    });
                }
            });
            
        } catch (Exception e) {
            showError("Không thể mở dialog resume: " + e.getMessage());
        }
    }    
    /**
     * Cancel ongoing translation
     */
    private void cancelTranslation() {
        if (translationService != null) {
            translationService.cancelTranslation();
            logMessage("Đang hủy quá trình dịch...");
        }
    }
    
    /**
     * Validate inputs before starting translation
     */
    private boolean validateInputsForTranslation() {
        if (!isProviderSupportedForTranslation()) {
            ProviderInfo info = getSelectedProviderInfo();
            String providerName = info != null ? info.displayName : selectedProvider;
            showWarning(providerName + " chưa được hỗ trợ dịch tự động. Vui lòng chọn nhà cung cấp khác.");
            return false;
        }

        if (apiKey.isEmpty()) {
            showError("Vui lòng nhập API Key");
            txtapiKey.requestFocus();
            return false;
        }
        

        if (selectedModel == null || selectedModel.trim().isEmpty()) {
            showError("Vui lòng nhập model cho nhà cung cấp đã chọn");
            txtapiVersion.requestFocus();
            return false;
        }

        if (selectedChapterFiles == null || selectedChapterFiles.isEmpty()) {
            showError("Vui lòng chọn các file chương cần dịch");
            return false;
        }
        
        if (guidelines.trim().isEmpty()) {
            showError("Vui lòng chọn file yêu cầu dịch");
            return false;
        }
        
        return true;
    }
    
    /**
     * Initialize translation services
     */
    private void initializeTranslationServices() throws Exception {
        DataSource dataSource = createDataSource();
        AIClient aiClient;
        try {
            aiClient = createClientForProvider();
        } catch (UnsupportedOperationException e) {
            throw new Exception(e.getMessage(), e);
        }
        ChapterDao chapterDao = new ChapterDao(dataSource);
        
        // Test API connection
        if (!aiClient.testConnection()) {
            ProviderInfo info = getSelectedProviderInfo();
            String providerName = info != null ? info.displayName : "API";
            throw new Exception("Không thể kết nối tới " + providerName + ". Kiểm tra API key.");
        }

        translationService = new TranslationService(aiClient, chapterDao);
    }
    
    /**
     * Create database data source
     */
    private DataSource createDataSource() {
        return DataSourceFactory.create(
            config.getDatabaseUrl(), 
            config.getDatabaseUser(), 
            config.getDatabasePassword()
        );
    }
    
    /**
     * Estimate cost for translation
     */
    private double estimateCost() {
        if (!isProviderSupportedForTranslation() ||
            selectedChapterFiles == null || selectedChapterFiles.isEmpty() || apiKey.isEmpty()) {
            return 0.0;
        }
        
        try {
            AIClient tempClient = createClientForProvider();
            int totalLength = 0;
            for (File file : selectedChapterFiles) {
                String content = fileService.readTextFile(file);
                totalLength += content.length();
            }
            return tempClient.calculateEstimatedCost(totalLength, selectedModel);
        } catch (UnsupportedOperationException e) {
            return 0.0;
        } catch (Exception e) {
            return 0.0; // Return 0 if cannot estimate
        }
    }
    
    /**
     * Update cost estimate display
     */
    private void updateCostEstimate() {
        if (!isProviderSupportedForTranslation()) {
            return;
        }

        if (selectedChapterFiles != null && !selectedChapterFiles.isEmpty()) {
            double cost = estimateCost();
            ProviderInfo info = getSelectedProviderInfo();
            String providerName = info != null ? info.displayName : selectedProvider;
            logMessage(String.format("Ước tính chi phí với %s (%s): $%.3f",
                selectedModel, providerName, cost));
        }
    }
    
    /**
     * Handle progress updates
     */
    private void onProgressUpdate(ProgressEvent event) {
        SwingUtilities.invokeLater(() -> {
            // Update progress bar
            if (event.getTotal() > 0) {
                int percent = (int) event.getProgressPercentage();
                progressBar.setValue(percent);
                progressBar.setString(event.getCompleted() + "/" + event.getTotal() + " (" + percent + "%)");
            }
            
            // Update status
            statusLabel.setText("Trạng thái: " + event.getStatus().toString());
            
            // Log message
            logMessage(String.format("[%s] %s", 
                event.getTimestamp().toString().substring(11, 19), 
                event.getMessage()));
            
            // Handle completion/error
            if (event.isCompleted() || event.hasError()) {
                setTranslationUIState(false);
                
                if (event.isCompleted()) {
                    showInfo("Dịch hoàn tất!");
                } else if (event.hasError()) {
                    showError("Có lỗi xảy ra: " + event.getMessage());
                }
            }
        });
    }
    
    /**
     * Set UI state for translation mode
     */
    private void setTranslationUIState(boolean translating) {
        isTranslating = translating;
        btnCancel.setVisible(translating);
        progressBar.setVisible(translating);
        
        // Disable input fields during translation
        txtapiKey.setEnabled(!translating);
        txtapiVersion.setEnabled(!translating);
        cbapi.setEnabled(!translating);

        if (controlButtons != null) {
            for (JButton btn : controlButtons) {
                btn.setEnabled(!translating);
                Color original = originalButtonColors.get(btn);
                if (translating) {
                    btn.setBackground(Color.LIGHT_GRAY);
                } else if (original != null) {
                    btn.setBackground(original);
                }
            }
        }        
        
        if (!translating) {
            progressBar.setValue(0);
            progressBar.setString("Sẵn sàng");
            statusLabel.setText("Trạng thái: Sẵn sàng");
        }
    }
    
    // Utility methods
    private void logMessage(String message) {
        if (logArea != null) {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
        logMessage("ERROR: " + message);
    }
    
    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
        logMessage("WARNING: " + message);
    }
    
    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        logMessage("INFO: " + message);
    }
    
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (backgroundExecutor != null && !backgroundExecutor.isShutdown()) {
            backgroundExecutor.shutdown();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        bangTenPanel1 = new javax.swing.JPanel();
        txtapiVersion = new javax.swing.JTextField();
        txtapiKey = new javax.swing.JTextField();
        cbapi = new javax.swing.JComboBox<>();
        jPanel7 = new javax.swing.JPanel();
        btnResume = new javax.swing.JButton();
        btnStart = new javax.swing.JButton();
        yeuCauPanel6 = new javax.swing.JPanel();
        btnChuong = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDanhSachChuong = new javax.swing.JTextArea();
        btnRefresh3 = new javax.swing.JButton();
        bangTenPanel = new javax.swing.JPanel();
        btnBangTen = new javax.swing.JButton();
        txtBangTen = new javax.swing.JTextField();
        btnRefresh2 = new javax.swing.JButton();
        yeuCauPanel1 = new javax.swing.JPanel();
        btnYeuCau1 = new javax.swing.JButton();
        txtYeuCau1 = new javax.swing.JTextField();
        btnRefresh1 = new javax.swing.JButton();
        cbTruyen = new javax.swing.JComboBox<>();

        jPanel2.setBackground(new java.awt.Color(41, 39, 74));
        jPanel2.setForeground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Lava Devanagari", 0, 24)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Dịch");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(22, Short.MAX_VALUE))
        );

        txtapiVersion.setForeground(new java.awt.Color(153, 153, 153));
        txtapiVersion.setText("deepseek-reasoner");
        txtapiVersion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtapiVersionActionPerformed(evt);
            }
        });

        txtapiKey.setForeground(new java.awt.Color(153, 153, 153));
        txtapiKey.setText("Nhập API");
        txtapiKey.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtapiKeyActionPerformed(evt);
            }
        });

        cbapi.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "ChatGPT", "Claude", "DeepSeek", "Grok", "Gemini"}));

        javax.swing.GroupLayout bangTenPanel1Layout = new javax.swing.GroupLayout(bangTenPanel1);
        bangTenPanel1.setLayout(bangTenPanel1Layout);
        bangTenPanel1Layout.setHorizontalGroup(
            bangTenPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bangTenPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bangTenPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtapiKey, javax.swing.GroupLayout.PREFERRED_SIZE, 330, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbapi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtapiVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(34, Short.MAX_VALUE))
        );
        bangTenPanel1Layout.setVerticalGroup(
            bangTenPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bangTenPanel1Layout.createSequentialGroup()
                .addComponent(cbapi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtapiVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtapiKey, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(11, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(bangTenPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 527, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(0, 15, Short.MAX_VALUE)
                .addComponent(bangTenPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        btnResume.setBackground(new java.awt.Color(255, 204, 102));
        btnResume.setFont(new java.awt.Font("Helvetica Neue", 1, 16)); // NOI18N
        btnResume.setForeground(new java.awt.Color(255, 255, 255));
        btnResume.setText("Resume");

        btnStart.setBackground(new java.awt.Color(0, 204, 51));
        btnStart.setFont(new java.awt.Font("Helvetica Neue", 1, 16)); // NOI18N
        btnStart.setForeground(new java.awt.Color(255, 255, 255));
        btnStart.setText("Start");

        btnChuong.setBackground(new java.awt.Color(255, 153, 51));
        btnChuong.setForeground(new java.awt.Color(255, 255, 255));
        btnChuong.setText("Các Chương");
        btnChuong.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChuongActionPerformed(evt);
            }
        });

        txtDanhSachChuong.setBackground(new java.awt.Color(242, 242, 242));
        txtDanhSachChuong.setColumns(20);
        txtDanhSachChuong.setForeground(new java.awt.Color(153, 153, 153));
        txtDanhSachChuong.setRows(5);
        txtDanhSachChuong.setText("Danh Sách Các Chương");
        jScrollPane1.setViewportView(txtDanhSachChuong);

        btnRefresh3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/refresh.png"))); // NOI18N
        btnRefresh3.setText("jButton1");

        btnBangTen.setBackground(new java.awt.Color(153, 153, 255));
        btnBangTen.setForeground(new java.awt.Color(255, 255, 255));
        btnBangTen.setText("Bảng Tên");
        btnBangTen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBangTenActionPerformed(evt);
            }
        });

        txtBangTen.setEditable(false);
        txtBangTen.setForeground(new java.awt.Color(153, 153, 153));
        txtBangTen.setText("(File Bảng Tên)");
        txtBangTen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBangTenActionPerformed(evt);
            }
        });

        btnRefresh2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/refresh.png"))); // NOI18N
        btnRefresh2.setText("jButton1");
        btnRefresh2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefresh2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout bangTenPanelLayout = new javax.swing.GroupLayout(bangTenPanel);
        bangTenPanel.setLayout(bangTenPanelLayout);
        bangTenPanelLayout.setHorizontalGroup(
            bangTenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bangTenPanelLayout.createSequentialGroup()
                .addComponent(txtBangTen)
                .addGap(18, 18, 18)
                .addComponent(btnBangTen, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        bangTenPanelLayout.setVerticalGroup(
            bangTenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bangTenPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(bangTenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(bangTenPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnBangTen)
                        .addComponent(btnRefresh2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(txtBangTen, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnYeuCau1.setBackground(new java.awt.Color(255, 153, 204));
        btnYeuCau1.setForeground(new java.awt.Color(255, 255, 255));
        btnYeuCau1.setText("Yêu Cầu");
        btnYeuCau1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnYeuCau1ActionPerformed(evt);
            }
        });

        txtYeuCau1.setEditable(false);
        txtYeuCau1.setForeground(new java.awt.Color(153, 153, 153));
        txtYeuCau1.setText("(File Yêu Cầu)");
        txtYeuCau1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtYeuCau1ActionPerformed(evt);
            }
        });

        btnRefresh1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icon/refresh.png"))); // NOI18N
        btnRefresh1.setText("jButton1");

        javax.swing.GroupLayout yeuCauPanel1Layout = new javax.swing.GroupLayout(yeuCauPanel1);
        yeuCauPanel1.setLayout(yeuCauPanel1Layout);
        yeuCauPanel1Layout.setHorizontalGroup(
            yeuCauPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(yeuCauPanel1Layout.createSequentialGroup()
                .addComponent(txtYeuCau1)
                .addGap(12, 12, 12)
                .addComponent(btnYeuCau1, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRefresh1, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        yeuCauPanel1Layout.setVerticalGroup(
            yeuCauPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(yeuCauPanel1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(yeuCauPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnYeuCau1)
                    .addComponent(txtYeuCau1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnRefresh1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cbTruyen.setModel(new javax.swing.DefaultComboBoxModel<>());

        javax.swing.GroupLayout yeuCauPanel6Layout = new javax.swing.GroupLayout(yeuCauPanel6);
        yeuCauPanel6.setLayout(yeuCauPanel6Layout);
        yeuCauPanel6Layout.setHorizontalGroup(
            yeuCauPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(bangTenPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(yeuCauPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbTruyen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(432, Short.MAX_VALUE))
            .addGroup(yeuCauPanel6Layout.createSequentialGroup()
                .addGroup(yeuCauPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(yeuCauPanel6Layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnChuong, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRefresh3, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(yeuCauPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        yeuCauPanel6Layout.setVerticalGroup(
            yeuCauPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, yeuCauPanel6Layout.createSequentialGroup()
                .addContainerGap(14, Short.MAX_VALUE)
                .addComponent(cbTruyen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(yeuCauPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bangTenPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(yeuCauPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(yeuCauPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnRefresh3, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(btnChuong))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(63, 63, 63)
                .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65)
                .addComponent(btnResume, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(230, Short.MAX_VALUE))
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(yeuCauPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(yeuCauPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnStart, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnResume, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(210, Short.MAX_VALUE))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnYeuCau1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnYeuCau1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnYeuCau1ActionPerformed

    private void txtYeuCau1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtYeuCau1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtYeuCau1ActionPerformed

    private void btnChuongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChuongActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnChuongActionPerformed

    private void btnBangTenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBangTenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnBangTenActionPerformed

    private void txtBangTenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtBangTenActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtBangTenActionPerformed

    private void btnRefresh2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefresh2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnRefresh2ActionPerformed

    private void txtapiKeyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtapiKeyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtapiKeyActionPerformed

    private void txtapiVersionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtapiVersionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtapiVersionActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bangTenPanel;
    private javax.swing.JPanel bangTenPanel1;
    private javax.swing.JButton btnBangTen;
    private javax.swing.JButton btnChuong;
    private javax.swing.JButton btnRefresh1;
    private javax.swing.JButton btnRefresh2;
    private javax.swing.JButton btnRefresh3;
    private javax.swing.JButton btnResume;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnYeuCau1;
    private javax.swing.JComboBox<String> cbTruyen;
    private javax.swing.JComboBox<String> cbapi;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtBangTen;
    private javax.swing.JTextArea txtDanhSachChuong;
    private javax.swing.JTextField txtYeuCau1;
    private javax.swing.JTextField txtapiKey;
    private javax.swing.JTextField txtapiVersion;
    private javax.swing.JPanel yeuCauPanel1;
    private javax.swing.JPanel yeuCauPanel6;
    // End of variables declaration//GEN-END:variables
}

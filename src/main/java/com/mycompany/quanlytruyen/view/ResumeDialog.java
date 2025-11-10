package com.mycompany.quanlytruyen.view;

import com.mycompany.quanlytruyen.dao.ChapterDao;
import com.mycompany.quanlytruyen.utils.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ResumeDialog extends JDialog {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    private ChapterDao chapterDao;
    private ChapterDao.ResumableBook selectedBook;
    private boolean confirmed = false;
    
    // UI Components
    private JList<ChapterDao.ResumableBook> bookList;
    private DefaultListModel<ChapterDao.ResumableBook> listModel;
    private JTextArea detailsArea;
    private JButton btnResume;
    private JButton btnCancel;
    private JButton btnRefresh;
    private JLabel statusLabel;

    public ResumeDialog(Frame parent, ChapterDao chapterDao) {
        super(parent, "Resume Translation", true);
        this.chapterDao = chapterDao;
        initComponents();
        UIUtils.enableTextComponentShortcuts(this);
        loadResumableBooks();
        setupEventHandlers();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(700, 500);
        setLocationRelativeTo(getParent());

        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setBorder(new EmptyBorder(10, 10, 5, 10));
        JLabel titleLabel = new JLabel("Chọn dự án để tiếp tục dịch:");
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        titlePanel.add(titleLabel);

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        // Left panel - Book list
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setPreferredSize(new Dimension(350, 0));

        // List header
        JPanel listHeaderPanel = new JPanel(new BorderLayout());
        JLabel listLabel = new JLabel("Danh sách dự án:");
        btnRefresh = new JButton("Làm mới");
        btnRefresh.setPreferredSize(new Dimension(80, 25));
        listHeaderPanel.add(listLabel, BorderLayout.WEST);
        listHeaderPanel.add(btnRefresh, BorderLayout.EAST);

        // Book list
        listModel = new DefaultListModel<>();
        bookList = new JList<>(listModel);
        bookList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookList.setCellRenderer(new BookListCellRenderer());
        
        JScrollPane listScrollPane = new JScrollPane(bookList);
        listScrollPane.setPreferredSize(new Dimension(350, 300));

        leftPanel.add(listHeaderPanel, BorderLayout.NORTH);
        leftPanel.add(listScrollPane, BorderLayout.CENTER);

        // Right panel - Details
        JPanel rightPanel = new JPanel(new BorderLayout());
        
        JLabel detailsLabel = new JLabel("Chi tiết dự án:");
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailsArea.setBackground(new Color(248, 248, 248));
        detailsArea.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        JScrollPane detailsScrollPane = new JScrollPane(detailsArea);
        
        rightPanel.add(detailsLabel, BorderLayout.NORTH);
        rightPanel.add(detailsScrollPane, BorderLayout.CENTER);

        // Add panels to content
        contentPanel.add(leftPanel, BorderLayout.WEST);
        contentPanel.add(rightPanel, BorderLayout.CENTER);

        // Bottom panel - Status and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(5, 10, 10, 10));

        // Status
        statusLabel = new JLabel("Sẵn sàng");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancel = new JButton("Hủy");
        btnResume = new JButton("Tiếp tục dịch");
        btnResume.setEnabled(false);
        
        // Style buttons
        btnResume.setBackground(new Color(0, 204, 51));
        btnResume.setForeground(Color.WHITE);
        btnResume.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        
        buttonPanel.add(btnCancel);
        buttonPanel.add(btnResume);

        bottomPanel.add(statusLabel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // Add all panels to dialog
        add(titlePanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // Book list selection
        bookList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                ChapterDao.ResumableBook selected = bookList.getSelectedValue();
                if (selected != null) {
                    selectedBook = selected;
                    updateBookDetails(selected);
                    btnResume.setEnabled(true);
                } else {
                    selectedBook = null;
                    detailsArea.setText("");
                    btnResume.setEnabled(false);
                }
            }
        });

        // Double click to resume
        bookList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if (selectedBook != null) {
                        resumeTranslation();
                    }
                }
            }
        });

        // Buttons
        btnRefresh.addActionListener(e -> loadResumableBooks());
        btnResume.addActionListener(e -> resumeTranslation());
        btnCancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        // Close dialog on ESC
        getRootPane().registerKeyboardAction(
            e -> {
                confirmed = false;
                dispose();
            },
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void loadResumableBooks() {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Đang tải danh sách...");
            btnRefresh.setEnabled(false);
        });

        // Load in background thread
        new SwingWorker<List<ChapterDao.ResumableBook>, Void>() {
            @Override
            protected List<ChapterDao.ResumableBook> doInBackground() throws Exception {
                return chapterDao.getResumableBooks();
            }

            @Override
            protected void done() {
                try {
                    List<ChapterDao.ResumableBook> books = get();
                    
                    SwingUtilities.invokeLater(() -> {
                        listModel.clear();
                        for (ChapterDao.ResumableBook book : books) {
                            listModel.addElement(book);
                        }
                        
                        if (books.isEmpty()) {
                            statusLabel.setText("Không có dự án nào cần tiếp tục");
                        } else {
                            statusLabel.setText("Tìm thấy " + books.size() + " dự án");
                        }
                        
                        btnRefresh.setEnabled(true);
                    });
                    
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Lỗi: " + e.getMessage());
                        btnRefresh.setEnabled(true);
                        JOptionPane.showMessageDialog(ResumeDialog.this,
                            "Không thể tải danh sách dự án:\n" + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    });
                }
            }
        }.execute();
    }

    private void updateBookDetails(ChapterDao.ResumableBook book) {
        try {
            // Get pending chapters for details
            List<ChapterDao.ResumeChapter> pendingChapters = chapterDao.listPendingOrErrorChapters(book.id);
            
            StringBuilder details = new StringBuilder();
            details.append("TÊN DỰ ÁN: ").append(book.title).append("\n");
            details.append("MODEL: ").append(book.modelUsed).append("\n");
            details.append("TẠO LÚC: ").append(book.createdAt.format(DATE_FORMATTER)).append("\n");
            
            if (book.lastResumedAt != null) {
                details.append("RESUME CUỐI: ").append(book.lastResumedAt.format(DATE_FORMATTER)).append("\n");
            }
            
            details.append("\n--- THỐNG KÊ ---\n");
            details.append("Tổng chương: ").append(book.totalChapters).append("\n");
            details.append("Đã hoàn thành: ").append(book.completedChapters).append("\n");
            details.append("Đang chờ: ").append(book.pendingChapters).append("\n");
            details.append("Lỗi: ").append(book.errorChapters).append("\n");
            details.append("Tiến độ: ").append(String.format("%.1f%%", book.getCompletionPercentage())).append("\n");
            
            details.append("\n--- CÁC CHƯƠNG CẦN DỊCH ---\n");
            if (pendingChapters.isEmpty()) {
                details.append("Không có chương nào cần dịch\n");
            } else {
                for (ChapterDao.ResumeChapter chapter : pendingChapters) {
                    details.append("Chương ").append(chapter.chapterNumber);
                    details.append(" (").append(chapter.status).append(")");
                    
                    if (!chapter.hasSourceContent()) {
                        details.append(" - THIẾU SOURCE");
                    }
                    
                    if (chapter.retryCount > 0) {
                        details.append(" - Đã thử ").append(chapter.retryCount).append(" lần");
                    }
                    
                    if (chapter.errorMessage != null && !chapter.errorMessage.trim().isEmpty()) {
                        details.append("\n  Lỗi: ").append(chapter.errorMessage);
                    }
                    
                    details.append("\n");
                }
            }
            
            // Show guidelines and name table if available
            if (book.guidelines != null && !book.guidelines.trim().isEmpty()) {
                details.append("\n--- YÊU CẦU DỊCH ---\n");
                String guidelines = book.guidelines.length() > 200 ? 
                    book.guidelines.substring(0, 200) + "..." : book.guidelines;
                details.append(guidelines).append("\n");
            }
            
            if (book.nameTable != null && !book.nameTable.trim().isEmpty()) {
                details.append("\n--- BẢNG TÊN ---\n");
                String nameTable = book.nameTable.length() > 200 ? 
                    book.nameTable.substring(0, 200) + "..." : book.nameTable;
                details.append(nameTable).append("\n");
            }
            
            detailsArea.setText(details.toString());
            detailsArea.setCaretPosition(0); // Scroll to top
            
        } catch (Exception e) {
            detailsArea.setText("Lỗi khi tải chi tiết: " + e.getMessage());
        }
    }

    private void resumeTranslation() {
        if (selectedBook == null) return;
        
        // Confirm resume
        int result = JOptionPane.showConfirmDialog(this,
            "Tiếp tục dịch dự án:\n" + selectedBook.title + "\n\n" +
            "Cần dịch: " + selectedBook.getIncompleteChapters() + " chương\n" +
            "Model: " + selectedBook.modelUsed + "\n\n" +
            "Bạn có muốn tiếp tục?",
            "Xác nhận Resume",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (result == JOptionPane.YES_OPTION) {
            confirmed = true;
            dispose();
        }
    }

    // Getters
    public boolean isConfirmed() {
        return confirmed;
    }

    public ChapterDao.ResumableBook getSelectedBook() {
        return selectedBook;
    }

    // Custom cell renderer for book list
    private static class BookListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof ChapterDao.ResumableBook book) {
                // Create multi-line display
                String html = String.format(
                    "<html><b>%s</b><br>" +
                    "<small>%d/%d chương (%.1f%%) - %d cần dịch</small><br>" +
                    "<small style='color: gray;'>%s - %s</small></html>",
                    book.title,
                    book.completedChapters, book.totalChapters, book.getCompletionPercentage(),
                    book.getIncompleteChapters(),
                    book.modelUsed,
                    book.createdAt.format(DATE_FORMATTER)
                );
                setText(html);
                
                // Set background color based on status
                if (!isSelected) {
                    if (book.errorChapters > 0) {
                        setBackground(new Color(255, 245, 245)); // Light red for errors
                    } else if (book.pendingChapters > 0) {
                        setBackground(new Color(245, 255, 245)); // Light green for pending
                    }
                }
            }
            
            return this;
        }
        
        @Override
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            return new Dimension(d.width, d.height + 20); // Add padding for multi-line
        }
    }

    // Static method to show dialog and get result
    public static ChapterDao.ResumableBook showDialog(Frame parent, ChapterDao chapterDao) {
        ResumeDialog dialog = new ResumeDialog(parent, chapterDao);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            return dialog.getSelectedBook();
        }
        return null;
    }
}
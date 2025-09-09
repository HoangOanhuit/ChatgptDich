package com.mycompany.quanlyquanan.utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import com.github.lgooddatepicker.components.DatePicker;

/**
 * Utility class for applying consistent styling to form components
 * @author macos
 */
public class ComponentStyleUtil {

    // Common styling properties
    public static final Font COMMON_FONT = new Font("Helvetica Neue", Font.PLAIN, 14);
    public static final Color BORDER_COLOR = new Color(200, 200, 200);
    public static final Color FOCUS_BORDER_COLOR = new Color(100, 149, 237);
    public static final Color TEXT_COLOR = new Color(51, 51, 51);
    public static final int BORDER_THICKNESS = 1;
    public static final int PADDING = 8;

    public static final Font MAIN_BUTTON_FONT = new Font("Helvetica Neue", Font.BOLD, 14);
    public static final int MAIN_BUTTON_RADIUS = 20;
    public static final int MAIN_BUTTON_THICKNESS = 1;
    public static final int MAIN_BUTTON_HEIGHT = 40;
    public static final int MAIN_BUTTON_MIN_WIDTH = 120;
    public static final int MAIN_BUTTON_PADDING = 8;
    public static final Color MAIN_PRIMARY_BG_PRESSED = new Color(255, 153, 153);
    public static final Color MAIN_PRIMARY_BG_HOVER = new Color(255, 120, 120);
    public static final Color MAIN_PRIMARY_BG = new Color(230, 100, 100);
    public static final Color MAIN_PRIMARY_TEXT = Color.WHITE;

    // Các thuộc tính cho button secondary
    public static final Font SECONDARY_BUTTON_FONT = new Font("Helvetica Neue", Font.BOLD, 14);
    public static final int SECONDARY_BUTTON_RADIUS = 20;
    public static final int SECONDARY_BUTTON_THICKNESS = 1;
    public static final int SECONDARY_BUTTON_HEIGHT = 40;
    public static final int SECONDARY_BUTTON_PADDING = 8;
    public static final Color SECONDARY_BG = new Color(232, 229, 229);
    public static final Color SECONDARY_BG_HOVER = new Color(195, 190, 190);
    public static final Color SECONDARY_BG_PRESSED = new Color(160, 160, 160);
    public static final Color SECONDARY_TEXT = Color.BLACK;

    public static void stylePasswordField(JPasswordField jpass) {
        jpass.setFont(COMMON_FONT);
        jpass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS),
            BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));
        jpass.setBackground(Color.WHITE);
        jpass.setForeground(TEXT_COLOR);

        // Add focus listener for border color change
        jpass.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                jpass.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FOCUS_BORDER_COLOR, BORDER_THICKNESS + 1),
                    BorderFactory.createEmptyBorder(PADDING - 1, PADDING - 1, PADDING - 1, PADDING - 1)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                jpass.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS),
                    BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
                ));
            }
        });
    }



    /**
     * Custom ScrollBar UI
     */
    public static class ScrollBarCustomUI extends BasicScrollBarUI {
        private final int THUMB_SIZE = 4;

        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(197, 195, 195);
            trackColor = new Color(240, 240, 240);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (!scrollbar.isEnabled() || thumbBounds.width > thumbBounds.height && scrollbar.getOrientation() == Adjustable.VERTICAL) {
                return; // tránh vẽ lỗi
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(isDragging ? new Color(120, 120, 120) :
                    (isThumbRollover() ? new Color(170, 170, 170) : thumbColor));

            g2.fillRoundRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height, 10, 10);
            g2.dispose();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(trackColor);
            g2.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            g2.dispose();
        }

        @Override
        protected Dimension getMinimumThumbSize() {
            return new Dimension(THUMB_SIZE, THUMB_SIZE);
        }
    }

    /**
     * Apply consistent styling to JScrollPane
     */
    public static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS),
                BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));

        // Apply custom UI cho scrollbar
        scrollPane.getVerticalScrollBar().setUI(new ScrollBarCustomUI());
        scrollPane.getHorizontalScrollBar().setUI(new ScrollBarCustomUI());
    }

    /**
     * Apply consistent styling to JTextField
     */
    public static void styleTextField(JTextField textField) {
        textField.setFont(COMMON_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS),
            BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
        ));
        textField.setBackground(Color.WHITE);
        textField.setForeground(TEXT_COLOR);

        // Add focus listener for border color change
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FOCUS_BORDER_COLOR, BORDER_THICKNESS + 1),
                    BorderFactory.createEmptyBorder(PADDING - 1, PADDING - 1, PADDING - 1, PADDING - 1)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                textField.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS),
                    BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
                ));
            }
        });
    }



    /**
     * Apply consistent styling to JTextArea
     */
    public static void styleTextArea(JTextArea textArea) {
        textArea.setFont(COMMON_FONT);
        textArea.setBackground(Color.WHITE);
        textArea.setForeground(TEXT_COLOR);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = (JScrollPane) textArea.getParent().getParent();
        styleScrollPane(scrollPane); // dùng chung hàm styleScrollPane()

        // Add focus listener for border color change
        textArea.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                scrollPane.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(FOCUS_BORDER_COLOR, BORDER_THICKNESS + 1),
                        BorderFactory.createEmptyBorder(PADDING - 1, PADDING - 1, PADDING - 1, PADDING - 1)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                scrollPane.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS),
                        BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING)
                ));
            }
        });
    }

    /**
     * Apply consistent styling to DatePicker
     */
    public static void styleDatePicker(DatePicker datePicker) {
        datePicker.setFont(COMMON_FONT);
        datePicker.getComponentDateTextField().setFont(COMMON_FONT);
        datePicker.getComponentDateTextField().setBackground(Color.WHITE);
        datePicker.getComponentDateTextField().setForeground(TEXT_COLOR);

        // Style the border
        datePicker.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        // Add focus listener for border color change
        datePicker.getComponentDateTextField().addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                datePicker.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(FOCUS_BORDER_COLOR, BORDER_THICKNESS + 1),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
                ));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                datePicker.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS),
                    BorderFactory.createEmptyBorder(0, 0, 0, 0)
                ));
            }
        });
    }

    /**
     * Apply consistent styling to JComboBox, including custom scrollbar for popup
     */
    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(new Color(255, 255, 255));
        comboBox.setFont(new Font("Helvetica Neue", Font.PLAIN, 13));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
        comboBox.setForeground(new Color(60, 60, 60));
//        comboBox.setBorder(new EmptyBorder(0, 0, 0, 0)); // Set padding for the combo box
        comboBox.setOpaque(true);
        comboBox.setPreferredSize(new Dimension(100, 32));

        // Customize scrollbar in popup
        Object comp = comboBox.getUI().getAccessibleChild(comboBox, 0);
        if (comp instanceof JPopupMenu) {
            JPopupMenu popup = (JPopupMenu) comp;
            JScrollPane scrollPane = null;
            for (Component c : popup.getComponents()) {
                if (c instanceof JScrollPane) {
                    scrollPane = (JScrollPane) c;
                    break;
                }
            }
            if (scrollPane != null) {
                JScrollBar vBar = scrollPane.getVerticalScrollBar();
                if (vBar != null) vBar.setUI(new ScrollBarCustomUI());
                JScrollBar hBar = scrollPane.getHorizontalScrollBar();
                if (hBar != null) hBar.setUI(new ScrollBarCustomUI());
            }
        }
    }

    /**
     * Apply consistent styling to multiple JTextFields
     */
    public static void styleTextFields(JTextField... textFields) {
        for (JTextField textField : textFields) {
            styleTextField(textField);
        }
    }

    /**
     * Apply consistent styling to multiple DatePickers
     */
    public static void styleDatePickers(DatePicker... datePickers) {
        for (DatePicker datePicker : datePickers) {
            styleDatePicker(datePicker);
        }
    }

    /**
     * Style search text field: rounded corners, gray border, padding, font
     */
    public static void styleSearchTextField(JTextField textField) {
        textField.setFont(COMMON_FONT);
        textField.setBackground(Color.WHITE);
        textField.setForeground(TEXT_COLOR);
        textField.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        textField.setOpaque(false);

        textField.setUI(new javax.swing.plaf.basic.BasicTextFieldUI() {
            @Override
            protected void paintSafely(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                int width = textField.getWidth();
                int height = textField.getHeight();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, width, height, 20, 20);
                g2.setColor(new Color(180, 180, 180)); // gray border
                g2.setStroke(new BasicStroke(1));
                g2.drawRoundRect(0, 0, width - 1, height - 1, 20, 20);
                g2.dispose();
                super.paintSafely(g);
            }
        });
    }

    /**
     * Style JTable giống MyTable: màu grid, chiều cao dòng, header, border
     */
    public static void styleTable(JTable table) {
        table.setShowHorizontalLines(true);
        table.setRowHeight(40);
        table.setBorder(BorderFactory.createEmptyBorder());
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(getTableHeaderRenderer());

        // Sửa màu nền khi select row
        table.setSelectionBackground(new Color(248, 196, 196)); // Màu đỏ nhạt, có thể đổi theo ý muốn
        table.setSelectionForeground(Color.BLACK); // Màu chữ khi select

        // chỉnh size column ID
        if (table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setPreferredWidth(50); // Cột ID
            table.getColumnModel().getColumn(0).setMaxWidth(50);
            table.getColumnModel().getColumn(1).setPreferredWidth(150); // Cột Name
            table.getColumnModel().getColumn(1).setMaxWidth(200);
            // Các cột còn lại để tự động co giãn
        }
    }

    /**
     * Renderer cho TableHeader giống MyTableHeader
     */
    public static TableCellRenderer getTableHeaderRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel header = new JLabel(value == null ? "" : value.toString());
                header.setOpaque(true);
                header.setBackground(new Color(220, 220, 220));
                header.setFont(new Font("Helvetica Neue", Font.BOLD, 14));
                header.setHorizontalAlignment(JLabel.CENTER);
                header.setForeground(new Color(4, 4, 4));
                header.setBorder(new EmptyBorder(10, 5, 10, 5));
                header.setPreferredSize(new Dimension(100, 45));
                return header;
            }
        };
    }

    /**
     * Style JScrollPane chứa table giống MyTable.fixTable
     */
    public static void styleTableScrollPane(JScrollPane scroll) {
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setVerticalScrollBar(new JScrollBar());
        scroll.setBorder(null);
        JPanel p = new JPanel();
        scroll.setCorner(JScrollPane.UPPER_RIGHT_CORNER, p);
        scroll.setBorder(new EmptyBorder(5, 10, 5, 10));
    }

    /**
     * Enable sorting on table headers
     */
    public static void enableTableSorting(JTable table) {
        table.setAutoCreateRowSorter(true);
    }
    /**
     * Style JButton giống MyButtonMain
     */
    public static void styleMainButton(JButton button) {
        button.setFont(MAIN_BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true); // Sửa lại từ false thành true để button vẽ nền
        button.setContentAreaFilled(true); // Sửa lại từ false thành true để button vẽ nền
        
        FontMetrics fm = button.getFontMetrics(MAIN_BUTTON_FONT);
        int textWidth = fm.stringWidth(button.getText());
        int paddingWidth = MAIN_BUTTON_PADDING * 4; // left/right padding set via EmptyBorder
        int width = Math.max(Math.max(button.getPreferredSize().width, textWidth + paddingWidth), MAIN_BUTTON_MIN_WIDTH);
        Dimension size = new Dimension(width, MAIN_BUTTON_HEIGHT);
        button.setPreferredSize(size);
        button.setMinimumSize(size);
        
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(MAIN_BUTTON_PADDING, MAIN_BUTTON_PADDING * 2, MAIN_BUTTON_PADDING, MAIN_BUTTON_PADDING * 2));
        button.setForeground(MAIN_PRIMARY_TEXT);

        // Mouse effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(MAIN_PRIMARY_BG_HOVER);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(MAIN_PRIMARY_BG);
                }
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(MAIN_PRIMARY_BG_PRESSED);
                }
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(button.getBounds().contains(e.getPoint()) ? MAIN_PRIMARY_BG_HOVER : MAIN_PRIMARY_BG);
                }
            }
        });
        button.setBackground(MAIN_PRIMARY_BG);

    }

    /**
     * Vẽ lại button giống MyButtonMain (bo góc, border, shadow)
     */
    public static void paintMainButton(Graphics g, JButton button) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw shadow (optional)
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(2, 4, button.getWidth() - 4, button.getHeight() - 4, MAIN_BUTTON_RADIUS, MAIN_BUTTON_RADIUS);

        // Draw button background
        g2.setColor(button.getBackground());
        g2.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), MAIN_BUTTON_RADIUS, MAIN_BUTTON_RADIUS);

        // Draw border
        g2.setColor(MAIN_PRIMARY_BG);
        g2.setStroke(new BasicStroke(MAIN_BUTTON_THICKNESS));
        g2.drawRoundRect(0, 0, button.getWidth() - 1, button.getHeight() - 1, MAIN_BUTTON_RADIUS, MAIN_BUTTON_RADIUS);

        g2.dispose();
    }

    // dùng hàm này trong paintComponent của JButton override lại
    // ví dụ:
    /*
    @Override
    protected void paintComponent(Graphics g) {
        ComponentStyleUtil.paintMainButton(g, this);
        super.paintComponent(g);



     */

    /**
     * Style JButton giống MyButtonSecond
     */
    public static void styleSecondButton(JButton button) {
        button.setFont(SECONDARY_BUTTON_FONT);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setPreferredSize(new Dimension(120, SECONDARY_BUTTON_HEIGHT));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(SECONDARY_BUTTON_PADDING, SECONDARY_BUTTON_PADDING * 2, SECONDARY_BUTTON_PADDING, SECONDARY_BUTTON_PADDING * 2));
        button.setForeground(SECONDARY_TEXT);

        // Mouse effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(SECONDARY_BG_HOVER);
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(SECONDARY_BG);
                }
            }
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(SECONDARY_BG_PRESSED);
                }
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(button.getBounds().contains(e.getPoint()) ? SECONDARY_BG_HOVER : SECONDARY_BG);
                }
            }
        });
        button.setBackground(SECONDARY_BG);
    }

    /**
     * Vẽ lại button giống MyButtonSecond (bo góc, border, shadow)
     */
    public static void paintSecondButton(Graphics g, JButton button) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw shadow (optional)
        g2.setColor(new Color(0, 0, 0, 20));
        g2.fillRoundRect(2, 4, button.getWidth() - 4, button.getHeight() - 4, SECONDARY_BUTTON_RADIUS, SECONDARY_BUTTON_RADIUS);

        // Draw button background
        g2.setColor(button.getBackground());
        g2.fillRoundRect(0, 0, button.getWidth(), button.getHeight(), SECONDARY_BUTTON_RADIUS, SECONDARY_BUTTON_RADIUS);

        // Draw border
        g2.setColor(new Color(230, 100, 100));
        g2.setStroke(new BasicStroke(SECONDARY_BUTTON_THICKNESS));
        g2.drawRoundRect(0, 0, button.getWidth() - 1, button.getHeight() - 1, SECONDARY_BUTTON_RADIUS, SECONDARY_BUTTON_RADIUS);

        g2.dispose();
    }



}

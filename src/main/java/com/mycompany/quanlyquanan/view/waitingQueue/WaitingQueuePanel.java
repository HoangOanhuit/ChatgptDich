/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlyquanan.view.waitingQueue;

import com.mycompany.quanlyquanan.controller.TableController;
import com.mycompany.quanlyquanan.controller.WaitingQueueController;
import com.mycompany.quanlyquanan.model.WaitingQueue;
import com.mycompany.quanlyquanan.service.FPTTTSService;
import com.mycompany.quanlyquanan.service.QueueAutoAssignWaitingCustomersService;
import com.mycompany.quanlyquanan.service.QueueAutoCancelService;
import com.mycompany.quanlyquanan.service.WaitingQueueService;
import com.mycompany.quanlyquanan.utils.ComponentStyleUtil;
import com.mycompany.quanlyquanan.utils.RedisSubscriber;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import redis.clients.jedis.JedisPubSub;

/**
 *
 * @author Admin
 */
public class WaitingQueuePanel extends javax.swing.JPanel {

    private WaitingQueueController controller = new WaitingQueueController();
    private WaitingQueueService service = new WaitingQueueService();
    private FPTTTSService voice = new FPTTTSService();

    /**
     * Creates new form WaitingQueuePanel
     */
    public WaitingQueuePanel() {
        initComponents();

        RedisSubscriber.addListener("waitingQueue", msg -> {
            SwingUtilities.invokeLater(() -> {
                loadData(); // gọi reload lại bảng
            });
        });
        JedisPubSub listener = null;

        RedisSubscriber.subscribe(listener, "waitingQueue", "cancel_oi_channel1", "done_oi_channel");

        loadData();

        QueueAutoCancelService autoCancelService = new QueueAutoCancelService(controller);
        autoCancelService.start();
        
        QueueAutoAssignWaitingCustomersService autoAssign = new QueueAutoAssignWaitingCustomersService(controller);
        autoAssign.startAutoAssign();

        jTable1.setAutoCreateRowSorter(true);

        jTable1.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof LocalDateTime) {
                    Duration duration = Duration.between((LocalDateTime) value, LocalDateTime.now());
                    setText(String.format("%02d:%02d:%02d",
                            duration.toHours(),
                            duration.toMinutesPart(),
                            duration.toSecondsPart()));
                    setFont(getFont().deriveFont(Font.BOLD)); // chữ đậm
                } else {
                    setText("");
                }
            }
        });

        // Timer update real-time
        new javax.swing.Timer(1000, e -> jTable1.repaint()).start();

        ComponentStyleUtil.styleTableScrollPane(jScrollPane1);
        ComponentStyleUtil.styleTable(jTable1);
        ComponentStyleUtil.styleSearchTextField(ipSearch);
        ComponentStyleUtil.styleMainButton(btnCof);
        
        
    }

    TableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            String status = value != null ? value.toString() : "";

            // In đậm
            c.setFont(c.getFont().deriveFont(Font.BOLD));

            // Đổi màu chữ theo trạng thái
            switch (status) {
                case "w-checkin":
                    c.setForeground(Color.GREEN.darker());
                    break;
                case "canceled":
                    c.setForeground(Color.GRAY);
                    break;
                case "waiting":
                    c.setForeground(new Color(255, 140, 0)); // cam
                    break;
                case "merged":
                    c.setForeground(Color.MAGENTA.darker());
                    break;
                default:
                    c.setForeground(Color.BLACK);
                    break;
            }

            if (isSelected) {
                c.setForeground(table.getSelectionForeground());
            }

            return c;
        }
    };

    private void setNotifiedAtRenderer(JTable table, int columnIndex) {
        table.getColumnModel().getColumn(columnIndex).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof LocalDateTime) {
                    Duration duration = Duration.between((LocalDateTime) value, LocalDateTime.now());
                    setText(String.format("%02d:%02d:%02d",
                            duration.toHours(),
                            duration.toMinutesPart(),
                            duration.toSecondsPart()));

                    setFont(getFont().deriveFont(Font.BOLD));
                    // Nếu quá 15 phút thì tô đỏ (20 s)
                    if (duration.toSeconds() > 20) {
                        setForeground(Color.RED);
                    } else {
                        setForeground(Color.BLACK);
                    }
                } else {
                    setText("");
                }
            }
        });
    }

    private void resetAdd() {
        ipName.setText("");
        ipPhone.setText("");
        ipGuestCount.setText("");

    }

    public void loadTableData(List<WaitingQueue> queues) {

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        for (WaitingQueue q : queues) {
            String status = q.getStatus();

            Object requestedAt = null;
            Object notifiedAt = null;

            // Nếu status KHÔNG phải seated hoặc canceled thì mới set giá trị
            if (!"seated".equalsIgnoreCase(status) && !"canceled".equalsIgnoreCase(status)) {
                requestedAt = q.getRequestedAt();
                notifiedAt = q.getNotifiedAt();
            }

            model.addRow(new Object[]{
                q.getId(),
                q.getQueueNumber(),
                q.getCustomerName(),
                q.getPhone(),
                q.getGuestCount(),
                q.getStatus(),
                q.getSuggestedTables(),
                requestedAt,
                notifiedAt
            });
        }

        jTable1.getColumnModel().getColumn(0).setMinWidth(0);
        jTable1.getColumnModel().getColumn(0).setMaxWidth(0);
        jTable1.getColumnModel().getColumn(0).setWidth(0);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(0);

        // set renderer cho cột status
        jTable1.getColumnModel().getColumn(5).setCellRenderer(statusRenderer);

        // set renderer cho cột notifiedAt
        setNotifiedAtRenderer(jTable1, 8);

    }

    public void loadData() {

        ipStt.setText("%d".formatted(controller.getLatestQueueNumberToday() + 1));
        List<WaitingQueue> queues = controller.getAll();
        loadTableData(queues);

    }

    public void loadData(List<WaitingQueue> queue) {
        ipStt.setText("%d".formatted(controller.getLatestQueueNumberToday() + 1));
        loadTableData(queue);
    }

    private List<Integer> getSelectedTableIds() {
        int[] selectedRows = jTable1.getSelectedRows();
        List<Integer> ids = new ArrayList<>();

        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một khách");
            return ids; // trả về list rỗng
        }

        for (int row : selectedRows) {
            int tableId = (int) jTable1.getValueAt(row, 0); // cột 0 là ID
            ids.add(tableId);
        }

        return ids;
    }

    private Integer getSelectedTableId() {
        int selectedRow = jTable1.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một khách");
            return null; // không chọn thì trả null
        }

        return (Integer) jTable1.getValueAt(selectedRow, 0); // cột 0 là ID
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolNav = new javax.swing.JPanel();
        ipSearch = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        btnDelete = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        btnSuggest = new javax.swing.JButton();
        btnCheckin = new javax.swing.JButton();
        btnVoice = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        ipName = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        ipPhone = new javax.swing.JTextField();
        ipGuestCount = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        ipStt = new javax.swing.JLabel();
        btnCof = new javax.swing.JButton();

        setBackground(new java.awt.Color(244, 244, 244));
        setPreferredSize(new java.awt.Dimension(1281, 695));

        toolNav.setBackground(new java.awt.Color(255, 255, 255));
        toolNav.setPreferredSize(new java.awt.Dimension(1300, 100));

        ipSearch.setPreferredSize(new java.awt.Dimension(200, 40));
        ipSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipSearchActionPerformed(evt);
            }
        });
        ipSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ipSearchKeyPressed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel2.setText("Quản lý hàng đợi");
        jLabel2.setPreferredSize(new java.awt.Dimension(200, 22));

        btnDelete.setBackground(new java.awt.Color(255, 242, 242));
        btnDelete.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/delete-40.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.setBorder(null);
        btnDelete.setBorderPainted(false);
        btnDelete.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setOpaque(true);
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnDeleteMouseClicked(evt);
            }
        });
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        btnEdit.setBackground(new java.awt.Color(255, 242, 242));
        btnEdit.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/pencil-40.png"))); // NOI18N
        btnEdit.setText("Edit");
        btnEdit.setBorder(null);
        btnEdit.setBorderPainted(false);
        btnEdit.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnEdit.setFocusable(false);
        btnEdit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEdit.setOpaque(true);
        btnEdit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnEdit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnEditMouseClicked(evt);
            }
        });
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnSearch.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/search-40.png"))); // NOI18N
        btnSearch.setBorder(null);
        btnSearch.setBorderPainted(false);
        btnSearch.setContentAreaFilled(false);
        btnSearch.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSearch.setFocusPainted(false);
        btnSearch.setFocusable(false);
        btnSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSearch.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSearchMouseClicked(evt);
            }
        });
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        btnCancel.setBackground(new java.awt.Color(244, 244, 244));
        btnCancel.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/Cancel-40-p.png"))); // NOI18N
        btnCancel.setText("Cancel");
        btnCancel.setBorder(null);
        btnCancel.setBorderPainted(false);
        btnCancel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCancel.setFocusable(false);
        btnCancel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCancel.setOpaque(true);
        btnCancel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCancel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCancelMouseClicked(evt);
            }
        });
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        btnSuggest.setBackground(new java.awt.Color(255, 242, 242));
        btnSuggest.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnSuggest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/Round-Table-40-p.png"))); // NOI18N
        btnSuggest.setText("Tìm bàn");
        btnSuggest.setBorder(null);
        btnSuggest.setBorderPainted(false);
        btnSuggest.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSuggest.setFocusable(false);
        btnSuggest.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSuggest.setOpaque(true);
        btnSuggest.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSuggest.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSuggestMouseClicked(evt);
            }
        });
        btnSuggest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSuggestActionPerformed(evt);
            }
        });

        btnCheckin.setBackground(new java.awt.Color(244, 244, 244));
        btnCheckin.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnCheckin.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/Checkin-40.png"))); // NOI18N
        btnCheckin.setText("Check in");
        btnCheckin.setBorder(null);
        btnCheckin.setBorderPainted(false);
        btnCheckin.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnCheckin.setFocusable(false);
        btnCheckin.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCheckin.setOpaque(true);
        btnCheckin.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCheckin.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCheckinMouseClicked(evt);
            }
        });
        btnCheckin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCheckinActionPerformed(evt);
            }
        });

        btnVoice.setBackground(new java.awt.Color(255, 242, 242));
        btnVoice.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnVoice.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/Voice-40-p.png"))); // NOI18N
        btnVoice.setText("Thông báo");
        btnVoice.setBorder(null);
        btnVoice.setBorderPainted(false);
        btnVoice.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnVoice.setFocusable(false);
        btnVoice.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnVoice.setOpaque(true);
        btnVoice.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnVoice.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnVoiceMouseClicked(evt);
            }
        });
        btnVoice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnVoiceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout toolNavLayout = new javax.swing.GroupLayout(toolNav);
        toolNav.setLayout(toolNavLayout);
        toolNavLayout.setHorizontalGroup(
            toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolNavLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCheckin, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSuggest, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnVoice, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ipSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch)
                .addGap(16, 16, 16))
        );
        toolNavLayout.setVerticalGroup(
            toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolNavLayout.createSequentialGroup()
                .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(toolNavLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSuggest, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnCheckin, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnVoice, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(toolNavLayout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(toolNavLayout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnSearch)
                            .addComponent(ipSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Id", "Number", "Customer", "Phone", "HeadCount", "Status", "Suggested", "Waiting", "Notified at"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Lấy số");
        jPanel4.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 20, 62, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Số người:");
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 180, -1, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setText("Họ tên :");
        jPanel4.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, -1, -1));
        jPanel4.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 50, 166, 20));

        ipName.setText("Nguyen thi tèo");
        ipName.setMaximumSize(new java.awt.Dimension(64, 22));
        ipName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipNameActionPerformed(evt);
            }
        });
        jPanel4.add(ipName, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 100, 130, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("SDT:");
        jPanel4.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, 45, -1));

        ipPhone.setText("0123456789");
        ipPhone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipPhoneActionPerformed(evt);
            }
        });
        jPanel4.add(ipPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 140, 130, -1));

        ipGuestCount.setText("12");
        ipGuestCount.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipGuestCountActionPerformed(evt);
            }
        });
        jPanel4.add(ipGuestCount, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 180, 130, -1));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("STT :");
        jPanel4.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 70, -1, -1));

        ipStt.setBackground(new java.awt.Color(0, 255, 0));
        ipStt.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        ipStt.setForeground(new java.awt.Color(0, 102, 0));
        ipStt.setText("12");
        jPanel4.add(ipStt, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 70, -1, -1));

        btnCof.setBackground(new java.awt.Color(0, 153, 0));
        btnCof.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnCof.setForeground(new java.awt.Color(255, 255, 255));
        btnCof.setText("Xác Nhận");
        btnCof.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCofActionPerformed(evt);
            }
        });
        jPanel4.add(btnCof, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 220, 120, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolNav, javax.swing.GroupLayout.DEFAULT_SIZE, 1281, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 256, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jScrollPane1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 292, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ipSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipSearchActionPerformed

    private void ipSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ipSearchKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            String keyword = ipSearch.getText().trim();
            if (keyword.isEmpty()) {
                loadData(); // Nếu rỗng thì load lại toàn bộ
            } else {
                List<WaitingQueue> searchResults = controller.search(keyword);
                loadData(searchResults);
            }

        }
    }//GEN-LAST:event_ipSearchKeyPressed

    private void btnDeleteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDeleteMouseClicked

    }//GEN-LAST:event_btnDeleteMouseClicked

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        //        // TODO add your handling code here:
        List<Integer> selectedIds = getSelectedTableIds();
        if (selectedIds.isEmpty()) {
            return;
        }

        int firstId = selectedIds.get(0);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn xóa " + selectedIds.size() + " bàn?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            controller.delete(firstId);
            loadData();
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnEditMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnEditMouseClicked

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
//         TODO add your handling code here:
        List<Integer> selectedIds = getSelectedTableIds();

        if (selectedIds.isEmpty()) {
            return; // không chọn bàn nào
        }

        // Chỉ lấy bàn đầu tiên để edit
        int firstId = selectedIds.get(0);

        WaitingQueue queue = controller.getById(firstId).orElse(null);

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        EditWaitingQueueDialog editDialog = new EditWaitingQueueDialog(parentFrame, true, this, queue);
        editDialog.setLocationRelativeTo(parentFrame);
        editDialog.setVisible(true);
    }//GEN-LAST:event_btnEditActionPerformed

    private void btnSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSearchMouseClicked

        // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchMouseClicked

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String keyword = ipSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadData(); // Nếu rỗng thì load lại toàn bộ
        } else {
            List<WaitingQueue> searchResults = controller.search(keyword);
            loadData(searchResults);
        }

        // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnCancelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCancelMouseClicked
        List<Integer> selectedIds = getSelectedTableIds();

        if (selectedIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một khách để cancel");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn cancel " + selectedIds.size() + " khách?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        selectedIds.forEach(id -> controller.getById(id).ifPresent(queue -> {
            queue.setStatus("canceled");
            controller.updateStatus(queue.getId(), queue.getStatus());
        }));

        loadData();


    }//GEN-LAST:event_btnCancelMouseClicked

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnSuggestMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSuggestMouseClicked

        // TODO add your handling code here:
    }//GEN-LAST:event_btnSuggestMouseClicked

    private void btnSuggestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuggestActionPerformed

        Integer selectedId = getSelectedTableId();

        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách trong danh sách chờ");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn tìm bàn cho khách?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        controller.getById(selectedId).ifPresent(queue -> {

            if (!queue.getStatus().equals("waiting")) {
                JOptionPane.showMessageDialog(this, "Khách hàng không trong hàng đợi");
                return;
            }

            String suggestedTable = controller.suggestForWaitingCustomer(queue.getGuestCount());

            if (suggestedTable == null) {
                JOptionPane.showMessageDialog(this, "Không có bàn phù hợp");
                return;
            }

            queue.setSuggestedTables(suggestedTable);
            try {
                controller.updateSuggestTables(queue.getId(), suggestedTable);
            } catch (SQLException ex) {
                System.getLogger(WaitingQueuePanel.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
            }

            JOptionPane.showMessageDialog(this,
                    "Gợi ý bàn cho khách " + queue.getCustomerName() + ": " + suggestedTable);

            loadData();
        });

        // TODO add your handling code here:
    }//GEN-LAST:event_btnSuggestActionPerformed

    private void ipNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipNameActionPerformed

    private void ipPhoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipPhoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipPhoneActionPerformed

    private void ipGuestCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipGuestCountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipGuestCountActionPerformed

    private void btnCofActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCofActionPerformed
        try {
            String name = ipName.getText().trim();
            String phone = ipPhone.getText().trim();
            int guestCount = Integer.parseInt(ipGuestCount.getText().trim());
            int stt = Integer.parseInt(ipStt.getText());

            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin!");
                return;
            }

            // Tạo object WaitingQueue mới
            WaitingQueue newQueue = new WaitingQueue();
            newQueue.setCustomerName(name);
            newQueue.setPhone(phone);
            newQueue.setGuestCount(guestCount);
            newQueue.setQueueNumber(stt);
            newQueue.setStatus("waiting");
            newQueue.setRequestedAt(LocalDateTime.now());

            // Gọi controller để thêm
            controller.addWaitingQueue(newQueue);

            JOptionPane.showMessageDialog(this, "Đã thêm khách vào hàng đợi!");

            // Cập nhật số thứ tự kế tiếp
            int sttNext = controller.getLatestQueueNumberToday() + 1;
            ipStt.setText(String.valueOf(sttNext));

            // Reset input
            resetAdd();

            // Refresh JTable
            loadData();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số khách phải là số hợp lệ!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + e.getMessage());
        }
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCofActionPerformed

    private void btnCheckinMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCheckinMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCheckinMouseClicked

    private void btnCheckinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCheckinActionPerformed

        List<Integer> selectedIds = getSelectedTableIds();

        if (selectedIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một khách để checkin");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn checkin " + selectedIds.size() + " khách?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        TableController tableController = new TableController();

        for (Integer id : selectedIds) {
            Optional<WaitingQueue> optQueue = controller.getById(id);
            if (optQueue.isEmpty()) {
                continue;
            }

            WaitingQueue queue = optQueue.get();

            // kiểm tra trạng thái w-checkin
            if (!queue.getStatus().equalsIgnoreCase("w-checkin")) {
                JOptionPane.showMessageDialog(this, "Khách  " + queue.getCustomerName() + " không đủ điều kiện checkin");
                continue; // bỏ qua khách này
            }

            if (queue.getSuggestedTables() == null || queue.getSuggestedTables().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khách " + queue.getCustomerName() + " chưa có bàn phù hợp");
                continue;
            }

            queue.setStatus("seated");
            controller.updateStatus(queue.getId(), queue.getStatus());

            String[] tableNames = queue.getSuggestedTables().split("\\+");
            for (String name : tableNames) {
                String trimmed = name.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        tableController.updateStatusByName(trimmed, "holding");
                    } catch (SQLException ex) {
                        System.getLogger(WaitingQueuePanel.class.getName())
                                .log(System.Logger.Level.ERROR, "Lỗi khi cập nhật bàn: " + trimmed, ex);
                    }
                }
            }
        }

        loadData();


    }//GEN-LAST:event_btnCheckinActionPerformed

    private void btnVoiceMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnVoiceMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnVoiceMouseClicked

    private void btnVoiceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnVoiceActionPerformed
        // TODO add your handling code here:
        Integer selectedId = getSelectedTableId();

        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một khách trong danh sách chờ");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn phát thông báo cho khách?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        controller.getById(selectedId).ifPresent(queue -> {

            if (!queue.getStatus().equals("w-checkin")) {
                JOptionPane.showMessageDialog(this, "Khách hàng không hợp lệ");
                return;
            }

            if (queue.getSuggestedTables() == null || queue.getSuggestedTables().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Khách " + queue.getCustomerName() + " chưa có bàn phù hợp");
                return;
            }

            try {
                   voice.speak("Mời khách hàng"+queue.getCustomerName()+"số thứ tự" +queue.getQueueNumber()+"vào nhận bàn" );
            } catch (Exception e) {
                e.printStackTrace();
            }

        });


    }//GEN-LAST:event_btnVoiceActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnCheckin;
    private javax.swing.JButton btnCof;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSuggest;
    private javax.swing.JButton btnVoice;
    private javax.swing.JTextField ipGuestCount;
    private javax.swing.JTextField ipName;
    private javax.swing.JTextField ipPhone;
    private javax.swing.JTextField ipSearch;
    private javax.swing.JLabel ipStt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel toolNav;
    // End of variables declaration//GEN-END:variables
}

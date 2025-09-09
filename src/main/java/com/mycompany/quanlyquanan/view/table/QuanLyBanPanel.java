/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlyquanan.view.table;

import com.mycompany.quanlyquanan.controller.TableController;
import com.mycompany.quanlyquanan.model.Employee;
import com.mycompany.quanlyquanan.model.Table;
import com.mycompany.quanlyquanan.service.TableService;
import com.mycompany.quanlyquanan.utils.ComponentStyleUtil;
import com.mycompany.quanlyquanan.utils.RedisSubscriber;
import com.mycompany.quanlyquanan.view.*;
import com.mycompany.quanlyquanan.view.Employee.CreateEmployeeDialog;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import redis.clients.jedis.JedisPubSub;

/**
 *
 * @author Admin
 */
public class QuanLyBanPanel extends javax.swing.JPanel {

    private TableController controller = new TableController();
    private TableService service = new TableService();

    /**
     * Creates new form QuanLyBanPanel
     */
    public QuanLyBanPanel() {
        
          RedisSubscriber.addListener("tableChanel", msg -> {
            SwingUtilities.invokeLater(() -> {
                loadTableData(); // gọi reload lại bảng
            });
        });
        JedisPubSub listener = null;

        RedisSubscriber.subscribe(listener, "tableChanel", "cancel_oi_channel1", "done_oi_channel");
        initComponents();
        loadTableData();
        setupSelectionTracking();
        
         jTable1.setAutoCreateRowSorter(true);

        jTable1.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
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
        ComponentStyleUtil.styleTableScrollPane(jScrollPane1);
        ComponentStyleUtil.styleTable(jTable1);
        ComponentStyleUtil.styleSearchTextField(ipSearch);
        ComponentStyleUtil.styleMainButton(btnSugg);
        // Timer update real-time
        new javax.swing.Timer(1000, e -> jTable1.repaint()).start();
        
        
   
    }

    public void refresh() {

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
                case "available":
                    c.setForeground(Color.GREEN.darker());
                    break;
                case "serving":
                    c.setForeground(Color.BLUE);
                    break;
                case "waiting_payment":
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

    public void loadTableData() {
 
        List<Table> tables = controller.getAllWithGroupCapacity();

        // Xóa dữ liệu cũ trong bảng
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        // Duyệt danh sách và thêm vào bảng
        for (Table t : tables) {
            model.addRow(new Object[]{
                t.getId(),
                t.getName(),
                t.getGroupCapacity() == null ? t.getCapacity() : (t.getCapacity() + " -> " + t.getGroupCapacity()),
                t.getStatus(),
                t.isCanMerge(),
                t.getLocationGroup(),
                t.getServeStartTime(),
                t.getEmployeeId(),
                t.getEmployeeName() != null ? t.getEmployeeName() : "—",
                t.getMergedIntoId(),
                t.getMergedIntoTableName() != null ? t.getMergedIntoTableName() : "—"
            });
        }

        jTable1.getColumnModel().getColumn(3).setCellRenderer(statusRenderer);
    }

    public void loadTableData(List<Table> tables) {

        // Xóa dữ liệu cũ trong bảng
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);

        // Duyệt danh sách và thêm vào bảng
        for (Table t : tables) {
            model.addRow(new Object[]{
                t.getId(),
                t.getName(),
                t.getGroupCapacity() == null ? t.getCapacity() : (t.getCapacity() + " -> " + t.getGroupCapacity()),
                t.getStatus(),
                t.isCanMerge(),
                t.getLocationGroup(),
                t.getServeStartTime(),
                t.getEmployeeId(),
                t.getEmployeeName() != null ? t.getEmployeeName() : "—",
                t.getMergedIntoId(),
                t.getMergedIntoTableName() != null ? t.getMergedIntoTableName() : "—"
            });
        }

        jTable1.getColumnModel().getColumn(3).setCellRenderer(statusRenderer);
    }

    private List<Integer> getSelectedTableIds() {
        int[] selectedRows = jTable1.getSelectedRows();
        List<Integer> ids = new ArrayList<>();

        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một bàn");
            return ids; // trả về list rỗng
        }

        for (int row : selectedRows) {
            int tableId = (int) jTable1.getValueAt(row, 0); // cột 0 là ID
            ids.add(tableId);
        }

        return ids;
    }

    private final java.util.List<Integer> selectedOrder = new java.util.ArrayList<>();

    private void setupSelectionTracking() {
        // cho phép chọn nhiều hàng, chọn theo hàng
        jTable1.setRowSelectionAllowed(true);
        jTable1.setColumnSelectionAllowed(false);
        jTable1.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // nếu có sorter (ví dụ setAutoCreateRowSorter(true)) thì PHẢI convert view -> model
        jTable1.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return; // bỏ event trung gian
            }
            // Tập hiện tại các ID đang được chọn
            java.util.Set<Integer> current = new java.util.LinkedHashSet<>();

            int[] viewRows = jTable1.getSelectedRows();
            for (int viewRow : viewRows) {
                if (viewRow < 0) {
                    continue;
                }
                int modelRow = jTable1.convertRowIndexToModel(viewRow);

                Object idObj = jTable1.getModel().getValueAt(modelRow, 0); // cột 0 là ID
                if (idObj == null) {
                    continue;
                }

                int id = (idObj instanceof Number)
                        ? ((Number) idObj).intValue()
                        : Integer.parseInt(idObj.toString());

                current.add(id);

                // thêm mới vào cuối list để bảo toàn "thứ tự chọn"
                if (!selectedOrder.contains(id)) {
                    selectedOrder.add(id);
                }
            }

            // loại bỏ các ID đã bị bỏ chọn
            selectedOrder.removeIf(id -> !current.contains(id));
            // Debug nếu cần
            // System.out.println("Order: " + selectedOrder);
        });
    }

    public java.util.List<Integer> getSelectedTableIdsInClickOrder() {
        return new java.util.ArrayList<>(selectedOrder); // trả bản copy
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
        btnAdd = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        btnReset = new javax.swing.JButton();
        btnMerge = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        ipSugg = new javax.swing.JTextField();
        btnSugg = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        ipSuggResult = new javax.swing.JTextArea();

        setBackground(new java.awt.Color(244, 244, 244));

        toolNav.setBackground(new java.awt.Color(255, 255, 255));
        toolNav.setPreferredSize(new java.awt.Dimension(1050, 100));

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
        jLabel2.setText("Quản lý Bàn");
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

        btnAdd.setBackground(new java.awt.Color(255, 242, 242));
        btnAdd.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/plus-40.png"))); // NOI18N
        btnAdd.setText("Add");
        btnAdd.setBorder(null);
        btnAdd.setBorderPainted(false);
        btnAdd.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnAdd.setFocusable(false);
        btnAdd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAdd.setOpaque(true);
        btnAdd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAdd.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnAddMouseClicked(evt);
            }
        });
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
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

        btnReset.setBackground(new java.awt.Color(244, 244, 244));
        btnReset.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnReset.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/reset-password-40.png"))); // NOI18N
        btnReset.setText("Reset");
        btnReset.setBorder(null);
        btnReset.setBorderPainted(false);
        btnReset.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnReset.setFocusable(false);
        btnReset.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReset.setOpaque(true);
        btnReset.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnReset.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnResetMouseClicked(evt);
            }
        });
        btnReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnResetActionPerformed(evt);
            }
        });

        btnMerge.setBackground(new java.awt.Color(244, 244, 244));
        btnMerge.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnMerge.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/merge-vertical-607.png"))); // NOI18N
        btnMerge.setText("Ghép bàn");
        btnMerge.setBorder(null);
        btnMerge.setBorderPainted(false);
        btnMerge.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnMerge.setFocusable(false);
        btnMerge.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnMerge.setOpaque(true);
        btnMerge.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnMerge.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnMergeMouseClicked(evt);
            }
        });
        btnMerge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMergeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout toolNavLayout = new javax.swing.GroupLayout(toolNav);
        toolNav.setLayout(toolNavLayout);
        toolNavLayout.setHorizontalGroup(
            toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolNavLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnMerge, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 112, Short.MAX_VALUE)
                .addComponent(ipSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch)
                .addGap(16, 16, 16))
        );
        toolNavLayout.setVerticalGroup(
            toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolNavLayout.createSequentialGroup()
                .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(toolNavLayout.createSequentialGroup()
                        .addGap(36, 36, 36)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(toolNavLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnMerge, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, toolNavLayout.createSequentialGroup()
                .addGap(0, 29, Short.MAX_VALUE)
                .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnSearch)
                    .addComponent(ipSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(31, 31, 31))
        );

        jScrollPane1.setOpaque(false);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(1100, 500));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Name", "Capacity", "Status", "Can Merge", "Location", "Serve Time", "Emp Id", "Empl Name", "Parent Id", "Parent Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class, java.lang.Object.class, java.lang.Integer.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setColumnSelectionAllowed(true);
        jScrollPane1.setViewportView(jTable1);
        jTable1.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));
        jPanel5.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("Gợi ý bàn ");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setText("Số người");

        ipSugg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipSuggActionPerformed(evt);
            }
        });

        btnSugg.setBackground(new java.awt.Color(254, 249, 249));
        btnSugg.setText("Tìm");
        btnSugg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSuggActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(89, 89, 89)
                .addComponent(jLabel8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel11)
                .addGap(22, 22, 22)
                .addComponent(ipSugg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(22, 22, 22)
                .addComponent(btnSugg, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(33, 33, 33))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(ipSugg, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSugg))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("Kết quả :");

        ipSuggResult.setEditable(false);
        ipSuggResult.setBackground(new java.awt.Color(255, 255, 255));
        ipSuggResult.setColumns(20);
        ipSuggResult.setRows(5);
        ipSuggResult.setText("Chưa có dữ liệu");
        ipSuggResult.setFocusable(false);
        ipSuggResult.setMargin(new java.awt.Insets(6, 12, 6, 6));
        ipSuggResult.setRequestFocusEnabled(false);
        jScrollPane5.setViewportView(ipSuggResult);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 1047, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolNav, javax.swing.GroupLayout.DEFAULT_SIZE, 1079, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ipSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipSearchActionPerformed

    private void ipSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ipSearchKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            String keyword = ipSearch.getText().trim();
            if (keyword.isEmpty()) {
                loadTableData(); // Nếu rỗng thì load lại toàn bộ
            } else {
                List<Table> searchResults = controller.searchWithGroupCapacity(keyword);
                loadTableData(searchResults);
            }

        }
    }//GEN-LAST:event_ipSearchKeyPressed

    private void btnDeleteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnDeleteMouseClicked

    }//GEN-LAST:event_btnDeleteMouseClicked

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        // TODO add your handling code here:
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
            controller.deleteTable(firstId);
            loadTableData();
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnAddMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddMouseClicked
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        CreateTableDialog createTableDialog = new CreateTableDialog(parentFrame, true, this);
        createTableDialog.setLocationRelativeTo(parentFrame);
        createTableDialog.setVisible(true);

    }//GEN-LAST:event_btnAddMouseClicked

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnEditMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditMouseClicked
        // TODO add your handling code here:

    }//GEN-LAST:event_btnEditMouseClicked

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        List<Integer> selectedIds = getSelectedTableIds();

        if (selectedIds.isEmpty()) {
            return; // không chọn bàn nào
        }

        // Chỉ lấy bàn đầu tiên để edit
        int firstId = selectedIds.get(0);

        Table table = controller.getTableById(firstId);

        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        EditTableDialog editDialog = new EditTableDialog(parentFrame, true, this, table);
        editDialog.setLocationRelativeTo(parentFrame);
        editDialog.setVisible(true);

    }//GEN-LAST:event_btnEditActionPerformed

    private void btnSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSearchMouseClicked

        // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchMouseClicked

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        String keyword = ipSearch.getText().trim();
        if (keyword.isEmpty()) {
            loadTableData(); // Nếu rỗng thì load lại toàn bộ
        } else {
            List<Table> searchResults = controller.searchWithGroupCapacity(keyword);
            loadTableData(searchResults);
        }

        // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnResetMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnResetMouseClicked
        List<Integer> selectedIds = getSelectedTableIds();

        if (selectedIds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một bàn để reset");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn reset " + selectedIds.size() + " bàn?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            for (Integer tableId : selectedIds) {
                Table table = controller.getTableById(tableId);
                if (table != null) {
                    table.setStatus("available");
                    table.setServeStartTime(null);
                    table.setEmployeeId(null);

                    if (table.getMergedIntoId() != null) {
                        table.setCanMerge(true);
                        table.setMergedIntoId(null);
                    }

                    controller.updateTable(table);
                }
            }

            loadTableData();
        }

        // TODO add your handling code here:
    }//GEN-LAST:event_btnResetMouseClicked

    private void btnResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnResetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnResetActionPerformed

    private void btnMergeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnMergeMouseClicked

        // TODO add your handling code here:
    }//GEN-LAST:event_btnMergeMouseClicked

    private void btnMergeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMergeActionPerformed
        List<Integer> selectedIds = getSelectedTableIdsInClickOrder();
        System.out.println(selectedIds);
        if (selectedIds.isEmpty()) {
            return; // không chọn bàn nào
        }

        if (selectedIds.size() < 2) {
            JOptionPane.showMessageDialog(this, "Chọn ít nhất 2 bàn để ghép.");
            return;
        }
        int rootId = selectedIds.get(0);
        List<Integer> childIds = selectedIds.subList(1, selectedIds.size());

        System.out.println(childIds);
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn ghép " + selectedIds.size() + " bàn?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {

                service.mergeTables(rootId, childIds, null);
                JOptionPane.showMessageDialog(this, "Ghép bàn thành công.");
                loadTableData(); // reload view
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ghép bàn thất bại: " + ex.getMessage());
            }

        }

        // TODO add your handling code here:
    }//GEN-LAST:event_btnMergeActionPerformed

    private void ipSuggActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipSuggActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipSuggActionPerformed

    private void btnSuggActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSuggActionPerformed
        String ipCustomer = ipSugg.getText();

        try {

            int customer = Integer.parseInt(ipCustomer);
            StringBuilder sb = controller.suggestTablesString(customer);

            ipSuggResult.setText(sb.toString());

        } catch (NumberFormatException ex) {

            JOptionPane.showMessageDialog(this, "Vui lòng nhập một số hợp lệ.", "Lỗi", JOptionPane.ERROR_MESSAGE);

        }

        // TODO add your handling code here:
    }//GEN-LAST:event_btnSuggActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnMerge;
    private javax.swing.JButton btnReset;
    private javax.swing.JButton btnSearch;
    private javax.swing.JButton btnSugg;
    private javax.swing.JTextField ipSearch;
    private javax.swing.JTextField ipSugg;
    private javax.swing.JTextArea ipSuggResult;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel toolNav;
    // End of variables declaration//GEN-END:variables
}

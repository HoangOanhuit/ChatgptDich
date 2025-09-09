/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlyquanan.view.table;

import com.mycompany.quanlyquanan.controller.TableController;
import com.mycompany.quanlyquanan.model.Table;
import com.mycompany.quanlyquanan.service.TableService;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Admin
 */
public class DieuPhoiBanPanel extends javax.swing.JPanel {

    private TableController controller = new TableController();
    private TableService service = new TableService();
    

    /**
     * Creates new form DieuPhoiBan
     */
    public DieuPhoiBanPanel() {
        initComponents();
        loadTableData();
        setupSelectionTracking();
        
        jTableData.setAutoCreateRowSorter(true);
        
        jTableData.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
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
        new javax.swing.Timer(1000, e -> jTableData.repaint()).start();

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
        // Lấy dữ liệu từ Controller (hoặc Service)
        List<Table> tables = controller.getAllWithGroupCapacity();

        // Xóa dữ liệu cũ trong bảng
        DefaultTableModel model = (DefaultTableModel) jTableData.getModel();
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
                t.getMergedIntoId(),
            
            });
        }

        jTableData.getColumnModel().getColumn(3).setCellRenderer(statusRenderer);
    }

    public void loadTableData(List<Table> tables) {

        // Xóa dữ liệu cũ trong bảng
        DefaultTableModel model = (DefaultTableModel) jTableData.getModel();
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
                t.getMergedIntoId(),
                
            });
        }

        jTableData.getColumnModel().getColumn(3).setCellRenderer(statusRenderer);
    }
    
    
    
    
     private List<Integer> getSelectedTableIds() {
        int[] selectedRows = jTableData.getSelectedRows();
        List<Integer> ids = new ArrayList<>();

        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn ít nhất một bàn");
            return ids; // trả về list rỗng
        }

        for (int row : selectedRows) {
            int tableId = (int) jTableData.getValueAt(row, 0); // cột 0 là ID
            ids.add(tableId);
        }

        return ids;
    }

    private final java.util.List<Integer> selectedOrder = new java.util.ArrayList<>();

    private void setupSelectionTracking() {
        // cho phép chọn nhiều hàng, chọn theo hàng
        jTableData.setRowSelectionAllowed(true);
        jTableData.setColumnSelectionAllowed(false);
        jTableData.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // nếu có sorter (ví dụ setAutoCreateRowSorter(true)) thì PHẢI convert view -> model
        jTableData.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return; // bỏ event trung gian
            }
            // Tập hiện tại các ID đang được chọn
            java.util.Set<Integer> current = new java.util.LinkedHashSet<>();

            int[] viewRows = jTableData.getSelectedRows();
            for (int viewRow : viewRows) {
                if (viewRow < 0) {
                    continue;
                }
                int modelRow = jTableData.convertRowIndexToModel(viewRow);

                Object idObj = jTableData.getModel().getValueAt(modelRow, 0); // cột 0 là ID
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

        jPanel5 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        ipSugg = new javax.swing.JTextField();
        btnSugg = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        ipSuggResult = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jTextField2 = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
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
        jTableData = new javax.swing.JTable();
        jScrollPane6 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();

        jPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("Gợi ý bàn ");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel11.setText("Số người");

        ipSugg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ipSuggActionPerformed(evt);
            }
        });

        btnSugg.setBackground(new java.awt.Color(204, 255, 255));
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
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(89, 89, 89)
                        .addComponent(jLabel8))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(jLabel11)
                        .addGap(22, 22, 22)
                        .addComponent(ipSugg, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSugg, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(28, Short.MAX_VALUE))
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
                .addContainerGap(28, Short.MAX_VALUE))
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
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
        );

        jPanel4.setBackground(new java.awt.Color(255, 255, 255));
        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Lấy số");
        jPanel4.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 10, 62, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Số người:");
        jPanel4.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(35, 134, -1, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel9.setText("Họ tên :");
        jPanel4.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(35, 66, -1, -1));
        jPanel4.add(jSeparator1, new org.netbeans.lib.awtextra.AbsoluteConstraints(35, 37, 166, 20));

        jTextField2.setText("Nguyen thi tèo");
        jTextField2.setMaximumSize(new java.awt.Dimension(64, 22));
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });
        jPanel4.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 63, 130, -1));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel10.setText("SDT:");
        jPanel4.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(35, 100, 45, -1));

        jTextField3.setText("0123456789");
        jTextField3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField3ActionPerformed(evt);
            }
        });
        jPanel4.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 97, -1, -1));

        jTextField4.setText("12");
        jTextField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField4ActionPerformed(evt);
            }
        });
        jPanel4.add(jTextField4, new org.netbeans.lib.awtextra.AbsoluteConstraints(107, 131, -1, -1));

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Name", "STT", "Status", "Sugess", "Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(jTable4);

        toolNav.setBackground(new java.awt.Color(255, 255, 255));

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
        jLabel2.setText("Điều Phối Bàn");

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
        btnSearch.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        btnSearch.setFocusable(false);
        btnSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSearch.setOpaque(true);
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
                .addGap(26, 26, 26)
                .addComponent(jLabel2)
                .addGap(51, 51, 51)
                .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 226, Short.MAX_VALUE)
                .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnMerge, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(119, 119, 119)
                .addComponent(ipSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
        );
        toolNavLayout.setVerticalGroup(
            toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(toolNavLayout.createSequentialGroup()
                .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(toolNavLayout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(jLabel2))
                    .addGroup(toolNavLayout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(btnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(toolNavLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnEdit, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(toolNavLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addComponent(ipSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnReset, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnMerge, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTableData.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Id", "Name", "Capacity", "Status", "Can Merge", "Location", "Serve Time", "Parent"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTableData.setColumnSelectionAllowed(true);
        jScrollPane1.setViewportView(jTableData);
        jTableData.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        if (jTableData.getColumnModel().getColumnCount() > 0) {
            jTableData.getColumnModel().getColumn(0).setPreferredWidth(25);
        }

        jTable5.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Name", "STT", "Status", "Sugess", "Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane6.setViewportView(jTable5);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolNav, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 559, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(17, 17, 17))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(314, 314, 314)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 246, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(720, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 587, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(41, 41, 41)
                                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(31, 31, 31)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addGap(299, 299, 299)
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGap(16, 16, 16)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ipSuggActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipSuggActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipSuggActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField4ActionPerformed

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
//        // TODO add your handling code here:
//        List<Integer> selectedIds = getSelectedTableIds();
//        if (selectedIds.isEmpty()) {
//            return;
//        }
//
//        int firstId = selectedIds.get(0);
//
//        int confirm = JOptionPane.showConfirmDialog(
//            this,
//            "Bạn có chắc muốn xóa " + selectedIds.size() + " bàn?",
//            "Xác nhận",
//            JOptionPane.YES_NO_OPTION
//        );
//
//        if (confirm == JOptionPane.YES_OPTION) {
//            controller.deleteTable(firstId);
//            loadTableData();
//        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnAddMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnAddMouseClicked
//        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
//        CreateTableDialog createTableDialog = new CreateTableDialog(parentFrame, true, this);
//        createTableDialog.setLocationRelativeTo(parentFrame);
//        createTableDialog.setVisible(true);
    }//GEN-LAST:event_btnAddMouseClicked

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAddActionPerformed

    private void btnEditMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnEditMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_btnEditMouseClicked

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
//        List<Integer> selectedIds = getSelectedTableIds();
//
//        if (selectedIds.isEmpty()) {
//            return; // không chọn bàn nào
//        }
//
//        // Chỉ lấy bàn đầu tiên để edit
//        int firstId = selectedIds.get(0);
//
//        Table table = controller.getTableById(firstId);
//
//        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
//        EditTableDialog editDialog = new EditTableDialog(parentFrame, true, this, table);
//        editDialog.setLocationRelativeTo(parentFrame);
//        editDialog.setVisible(true);
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
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable5;
    private javax.swing.JTable jTableData;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JPanel toolNav;
    // End of variables declaration//GEN-END:variables
}

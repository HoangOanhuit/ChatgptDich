/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package com.mycompany.quanlyquanan.view.Employee;

import com.mycompany.quanlyquanan.controller.EmployeeController;
import com.mycompany.quanlyquanan.model.Employee;
import com.mycompany.quanlyquanan.service.EmployeeService;
import com.mycompany.quanlyquanan.service.MailerService;
import com.mycompany.quanlyquanan.utils.ComponentStyleUtil;
import com.mycompany.quanlyquanan.utils.HashUtil;
import com.mycompany.quanlyquanan.utils.PasswordGeneratorUtil;
import com.mycompany.quanlyquanan.view.RoundedPanel;
import com.mycompany.quanlyquanan.view.RoundedButton;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.BorderLayout;

/**
 *
 * @author Admin
 */
public class QuanLyNhanVienPanel extends javax.swing.JPanel {

    private EmployeeService employeeService;
    private EmployeeController controller = new EmployeeController();
    private Employee selectedEmp;

    /**
     * Creates new form QuanLyNhanVienPanel
     */
    public QuanLyNhanVienPanel() {
        initComponents();
        jTable1.setAutoCreateRowSorter(true);
        List<Employee> employees = controller.getAll();
        loadTable(employees);
        addTableClickListener();

        ComponentStyleUtil.styleTableScrollPane(jScrollPane1);
        ComponentStyleUtil.styleTable(jTable1);
        ComponentStyleUtil.styleSearchTextField(ipSearch);
        style();
        
    }
    
    public void refresh(){
        List<Employee> employees = controller.getAll();
        loadTable(employees);
    }

    private void style() {
        // Rebuild layout to keep profile panel on the left and
        // allow other components to resize with the window
        removeAll();
        setLayout(new BorderLayout());

        jPanel2.removeAll();
        jPanel2.setLayout(new BorderLayout());
        jPanel2.add(toolNav, BorderLayout.NORTH);
        jPanel2.add(jScrollPane1, BorderLayout.CENTER);
        jPanel2.add(pnProfile, BorderLayout.WEST);
        add(jPanel2, BorderLayout.CENTER);        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                applyResponsiveStyle();
            }
        });
        SwingUtilities.invokeLater(() -> {
            Window window = SwingUtilities.getWindowAncestor(QuanLyNhanVienPanel.this);
            if (window != null) {
                window.addComponentListener(new ComponentAdapter() {
                    @Override
                    public void componentResized(ComponentEvent e) {
                        applyResponsiveStyle();
                    }
                });
            }
            applyResponsiveStyle();
        });
    }

    private void applyResponsiveStyle() {
        int width = getParent() != null ? getParent().getWidth() : getWidth();
        if (width < 900) {
            pnProfile.setVisible(false);
            ipSearch.setPreferredSize(new java.awt.Dimension(150, ipSearch.getPreferredSize().height));
        } else {
            pnProfile.setVisible(true);
            ipSearch.setPreferredSize(new java.awt.Dimension(200, ipSearch.getPreferredSize().height));
        }
        pnProfile.revalidate();
        pnProfile.repaint();
        toolNav.revalidate();
        toolNav.repaint();
    }   

    private void setProfileData(Employee emp) {
        ipName.setText(emp.getName());
        ipPhone.setText(emp.getPhone());
        ipEmail.setText(emp.getEmail());
        ipAdd.setText(emp.getAddress());
        ipPosition.setText(emp.getPosition());
        ipRole.setText(emp.getRole());
        ipId.setText(String.valueOf(emp.getId()));

        // Hiển thị ảnh
        if (emp.getAvatar() != null && !emp.getAvatar().isEmpty()) {
            ImageIcon icon = new ImageIcon(emp.getAvatar());
            Image img = icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
            ipAvatar.setIcon(new ImageIcon(img));
        } else {
            ImageIcon icon = new ImageIcon("assets/images/employee/default.png");
            Image img = icon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
            ipAvatar.setIcon(new ImageIcon(img));

        }
    }

 

    
    private void addTableClickListener() {
        jTable1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = jTable1.getSelectedRow();
                if (row != -1) {
                    int id = (int) jTable1.getValueAt(row, 0); // Lấy ID từ cột 0
                    selectedEmp = controller.getById(id);
                    if (selectedEmp != null) {
                        setProfileData(selectedEmp);
                    }
                }
            }
        });
    }

    protected void loadTable(List<Employee> employees) {

        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        for (Employee emp : employees) {
            model.addRow(new Object[]{
                emp.getId(),
                emp.getName(),
                emp.getUsername(),
                emp.getEmail(),
                emp.getRole(),
                emp.isActive()
            });
        }

        if (jTable1.getRowCount() > 0) {
            jTable1.setRowSelectionInterval(0, 0); // Chọn dòng đầu tiên
            int id = (int) jTable1.getValueAt(0, 0);
            selectedEmp = controller.getById(id);
            if (selectedEmp != null) {
                setProfileData(selectedEmp);
            }
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

        jPanel3 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        toolNav = new javax.swing.JPanel();
        ipSearch = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jBBan = new javax.swing.JButton();
        jBBan1 = new javax.swing.JButton();
        jBBan2 = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        jBBan5 = new javax.swing.JButton();
        jBBan6 = new javax.swing.JButton();
        jBBan7 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        pnProfile = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        ipAvatar = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        ipId = new javax.swing.JLabel();
        ipName = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        ipPosition = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        ipRole = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        ipPhone = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        ipAdd = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        ipEmail = new javax.swing.JLabel();

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setBackground(new java.awt.Color(244, 244, 244));
        setPreferredSize(new java.awt.Dimension(1050, 678));

        jPanel2.setBackground(new java.awt.Color(244, 244, 244));
        jPanel2.setPreferredSize(new java.awt.Dimension(1100, 672));

        toolNav.setBackground(new java.awt.Color(255, 255, 255));
        toolNav.setPreferredSize(new java.awt.Dimension(1100, 100));

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
        jLabel2.setText("Quản lý nhân viên");
        jLabel2.setPreferredSize(new java.awt.Dimension(200, 22));

        jBBan.setBackground(new java.awt.Color(255, 242, 242));
        jBBan.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBBan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/delete-40.png"))); // NOI18N
        jBBan.setText("Delete");
        jBBan.setBorder(null);
        jBBan.setBorderPainted(false);
        jBBan.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jBBan.setFocusable(false);
        jBBan.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBBan.setOpaque(true);
        jBBan.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBBan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jBBanMouseClicked(evt);
            }
        });
        jBBan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBBanActionPerformed(evt);
            }
        });

        jBBan1.setBackground(new java.awt.Color(255, 242, 242));
        jBBan1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBBan1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/plus-40.png"))); // NOI18N
        jBBan1.setText("Add");
        jBBan1.setBorder(null);
        jBBan1.setBorderPainted(false);
        jBBan1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jBBan1.setFocusable(false);
        jBBan1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBBan1.setOpaque(true);
        jBBan1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBBan1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jBBan1MouseClicked(evt);
            }
        });
        jBBan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBBan1ActionPerformed(evt);
            }
        });

        jBBan2.setBackground(new java.awt.Color(255, 242, 242));
        jBBan2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBBan2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/pencil-40.png"))); // NOI18N
        jBBan2.setText("Edit");
        jBBan2.setBorder(null);
        jBBan2.setBorderPainted(false);
        jBBan2.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jBBan2.setFocusable(false);
        jBBan2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBBan2.setOpaque(true);
        jBBan2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBBan2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jBBan2MouseClicked(evt);
            }
        });
        jBBan2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBBan2ActionPerformed(evt);
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

        jBBan5.setBackground(new java.awt.Color(244, 244, 244));
        jBBan5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBBan5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/reset-password-40.png"))); // NOI18N
        jBBan5.setText("Reset");
        jBBan5.setBorder(null);
        jBBan5.setBorderPainted(false);
        jBBan5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jBBan5.setFocusable(false);
        jBBan5.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBBan5.setOpaque(true);
        jBBan5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBBan5.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jBBan5MouseClicked(evt);
            }
        });
        jBBan5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBBan5ActionPerformed(evt);
            }
        });

        jBBan6.setBackground(new java.awt.Color(244, 244, 244));
        jBBan6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBBan6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/delete-user-40.png"))); // NOI18N
        jBBan6.setText("Deactivate");
        jBBan6.setBorder(null);
        jBBan6.setBorderPainted(false);
        jBBan6.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jBBan6.setFocusable(false);
        jBBan6.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBBan6.setOpaque(true);
        jBBan6.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBBan6.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jBBan6MouseClicked(evt);
            }
        });
        jBBan6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBBan6ActionPerformed(evt);
            }
        });

        jBBan7.setBackground(new java.awt.Color(244, 244, 244));
        jBBan7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jBBan7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/verified-account-40.png"))); // NOI18N
        jBBan7.setText("Activate");
        jBBan7.setBorder(null);
        jBBan7.setBorderPainted(false);
        jBBan7.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jBBan7.setFocusable(false);
        jBBan7.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jBBan7.setOpaque(true);
        jBBan7.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jBBan7.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jBBan7MouseClicked(evt);
            }
        });
        jBBan7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBBan7ActionPerformed(evt);
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
                .addComponent(jBBan1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jBBan2, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jBBan, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jBBan5, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jBBan7, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jBBan6, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 38, Short.MAX_VALUE)
                .addComponent(ipSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSearch)
                .addGap(24, 24, 24))
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
                            .addComponent(jBBan1, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBBan2, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBBan, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBBan5, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBBan6, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jBBan7, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(toolNavLayout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addGroup(toolNavLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnSearch)
                                    .addComponent(ipSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                .addContainerGap(10, Short.MAX_VALUE))
        );

        jScrollPane1.setPreferredSize(new java.awt.Dimension(462, 650));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                { new Integer(1), "Nguyên van a ", "Tk", "NguyênVana@gmail.com ", "Nhân viên",  new Boolean(true)},
                { new Integer(2), "Nguyên van a ", "pol", "NguyênVana@gmail.com ", "Nhân viên",  new Boolean(false)},
                { new Integer(3), "Nguyên van a ", "36", null, "Nhân viên",  new Boolean(true)}
            },
            new String [] {
                "id", "Họ tên", "User Name", "Email", "Role", "Active"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setPreferredSize(new java.awt.Dimension(405, 560));
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setMaxWidth(30);
        }

        pnProfile.setBackground(new java.awt.Color(255, 255, 255));
        pnProfile.setMinimumSize(new java.awt.Dimension(351, 448));
        pnProfile.setPreferredSize(new java.awt.Dimension(379, 650));
        pnProfile.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel11.setText("Profile");
        pnProfile.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 30, -1, -1));

        ipAvatar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/employee/coala-160.png"))); // NOI18N
        pnProfile.add(ipAvatar, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 80, 163, 175));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel13.setText("ID:");
        pnProfile.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 280, -1, -1));

        ipId.setText("01");
        ipId.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ipIdMouseClicked(evt);
            }
        });
        pnProfile.add(ipId, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 280, -1, -1));

        ipName.setText("nguyẽn văn thị Ngàn");
        pnProfile.add(ipName, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 280, -1, -1));

        jLabel17.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel17.setText("Họ tên:");
        pnProfile.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 280, -1, -1));

        jLabel23.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel23.setText("Vị trí:");
        pnProfile.add(jLabel23, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 310, -1, -1));

        ipPosition.setText("Nhân viên");
        pnProfile.add(ipPosition, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 310, -1, -1));

        jLabel25.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel25.setText("Role:");
        pnProfile.add(jLabel25, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 310, -1, -1));

        ipRole.setText("Nhân viên");
        pnProfile.add(ipRole, new org.netbeans.lib.awtextra.AbsoluteConstraints(260, 310, -1, -1));

        jPanel1.setBackground(new java.awt.Color(255, 242, 242));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel18.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel18.setText("Điện thoại: ");
        jPanel1.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 15, -1, -1));

        ipPhone.setText("0123456789");
        jPanel1.add(ipPhone, new org.netbeans.lib.awtextra.AbsoluteConstraints(106, 15, -1, -1));

        jLabel15.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel15.setText("Địa chỉ:");
        jPanel1.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 46, -1, -1));

        ipAdd.setText("HCM");
        jPanel1.add(ipAdd, new org.netbeans.lib.awtextra.AbsoluteConstraints(106, 46, -1, -1));

        jLabel21.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel21.setText("Email:");
        jPanel1.add(jLabel21, new org.netbeans.lib.awtextra.AbsoluteConstraints(23, 77, -1, -1));

        ipEmail.setText("ádsadsadasdasdsadsa");
        jPanel1.add(ipEmail, new org.netbeans.lib.awtextra.AbsoluteConstraints(106, 77, -1, -1));

        pnProfile.add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 360, 310, 110));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(pnProfile, javax.swing.GroupLayout.PREFERRED_SIZE, 422, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(toolNav, javax.swing.GroupLayout.PREFERRED_SIZE, 1108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(toolNav, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 566, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void ipSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ipSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ipSearchActionPerformed

    private void jBBanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBBanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBBanActionPerformed

    private void jBBan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBBan1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBBan1ActionPerformed

    private void jBBan2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBBan2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBBan2ActionPerformed

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchActionPerformed

    private void jBBan5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBBan5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBBan5ActionPerformed

    private void jBBan6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBBan6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBBan6ActionPerformed

    private void jBBan7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBBan7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jBBan7ActionPerformed

    private void jBBan1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBBan1MouseClicked

        // TODO add your handling code here:
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        CreateEmployeeDialog createEDialog = new CreateEmployeeDialog(parentFrame, true, this);
        createEDialog.setLocationRelativeTo(parentFrame);
        createEDialog.setVisible(true);
    }//GEN-LAST:event_jBBan1MouseClicked

    private void jBBan2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBBan2MouseClicked
        // TODO add your handling code here:
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        EditEmployeeDialog editEDialog = new EditEmployeeDialog(parentFrame, true, this, selectedEmp);
        editEDialog.setLocationRelativeTo(parentFrame);
        editEDialog.setVisible(true);
    }//GEN-LAST:event_jBBan2MouseClicked

    private void jBBanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBBanMouseClicked
        // TODO add your handling code here:
        int choice = JOptionPane.showConfirmDialog(QuanLyNhanVienPanel.this,
                "Bạn có chắc muốn xóa nhân viên này?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            boolean deleted = controller.delete(selectedEmp.getId());

            if (deleted) {
                JOptionPane.showMessageDialog(
                        this,
                        "Xóa nhân viên thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                List<Employee> employees = controller.getAll();
                loadTable(employees); // cập nhật lại bảng sau khi xóa
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Xóa thất bại. Vui lòng thử lại.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }//GEN-LAST:event_jBBanMouseClicked

    private void jBBan7MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBBan7MouseClicked
        if (selectedEmp.isActive()) {
            JOptionPane.showMessageDialog(this, "Tài khoản đang được kích hoạt");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(QuanLyNhanVienPanel.this,
                "Bạn có chắc muốn kích hoạt tài khoản này?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            Employee em = new Employee();
            em.setId(selectedEmp.getId());
            em.setActive(true);
            boolean updated = controller.updateStatus(em);

            if (updated) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật nhân viên thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                List<Employee> employees = controller.getAll();
                loadTable(employees); // cập nhật lại bảng sau khi xóa
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật thất bại. Vui lòng thử lại.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        // TODO add your handling code here:
    }//GEN-LAST:event_jBBan7MouseClicked

    private void jBBan6MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBBan6MouseClicked
        // TODO add your handling code here:
        if (!selectedEmp.isActive()) {
            JOptionPane.showMessageDialog(this, "Tài khoản đã được hủy");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(QuanLyNhanVienPanel.this,
                "Bạn có chắc muốn hủy tài khoản này?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            Employee em = new Employee();
            em.setId(selectedEmp.getId());
            em.setActive(false);

            boolean updated = controller.updateStatus(em);

            if (updated) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật nhân viên thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                List<Employee> employees = controller.getAll();
                loadTable(employees); // cập nhật lại bảng sau khi xóa
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "Cập nhật thất bại. Vui lòng thử lại.",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

    }//GEN-LAST:event_jBBan6MouseClicked

    private void btnSearchMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSearchMouseClicked
        String keyword = ipSearch.getText().trim();
        if (keyword.isEmpty()) {
            List<Employee> employees = controller.getAll();
            loadTable(employees); // Nếu rỗng thì load lại toàn bộ
        } else {
            List<Employee> searchResults = controller.search(keyword);
            loadTable(searchResults);
        }

        // TODO add your handling code here:
    }//GEN-LAST:event_btnSearchMouseClicked

    private void ipSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ipSearchKeyPressed
        // TODO add your handling code here:
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            String keyword = ipSearch.getText().trim();
            if (keyword.isEmpty()) {
                List<Employee> employees = controller.getAll();
                loadTable(employees); // Nếu rỗng thì load lại toàn bộ
            } else {
                List<Employee> searchResults = controller.search(keyword);
                loadTable(searchResults);
            }

        }
    }//GEN-LAST:event_ipSearchKeyPressed

    private void jBBan5MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jBBan5MouseClicked

        int choice = JOptionPane.showConfirmDialog(QuanLyNhanVienPanel.this,
                "Bạn có chắc muốn reset mật khẩu tài khoản này?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            try {
                String passOrg = PasswordGeneratorUtil.generatePassword();
                String pass = HashUtil.hashPassword(passOrg);
                selectedEmp.setPass(pass);

                if (controller.updatePass(selectedEmp)) {
                    String email = selectedEmp.getEmail();

                    SwingWorker<Void, Void> worker = new SwingWorker<>() {
                        @Override
                        protected Void doInBackground() {
                            try {
                                MailerService.getInstance().sendPasswordResetMail(email, "Cấp Lại Mật Khẩu", passOrg);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(QuanLyNhanVienPanel.this, "Lỗi khi gửi email: " + ex.getMessage());
                            }
                            return null;
                        }

                        @Override
                        protected void done() {
                            JOptionPane.showMessageDialog(QuanLyNhanVienPanel.this, "Đã gửi mật khẩu mới tới email: " + email);
                        }
                    };
                    worker.execute();
                } else {
                    JOptionPane.showMessageDialog(QuanLyNhanVienPanel.this, "Cập nhật mật khẩu thất bại.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(QuanLyNhanVienPanel.this, "Đã xảy ra lỗi: " + e.getMessage());
            }

        }

        // TODO add your handling code here:
    }//GEN-LAST:event_jBBan5MouseClicked

    private void ipIdMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ipIdMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_ipIdMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSearch;
    private javax.swing.JLabel ipAdd;
    private javax.swing.JLabel ipAvatar;
    private javax.swing.JLabel ipEmail;
    private javax.swing.JLabel ipId;
    private javax.swing.JLabel ipName;
    private javax.swing.JLabel ipPhone;
    private javax.swing.JLabel ipPosition;
    private javax.swing.JLabel ipRole;
    private javax.swing.JTextField ipSearch;
    private javax.swing.JButton jBBan;
    private javax.swing.JButton jBBan1;
    private javax.swing.JButton jBBan2;
    private javax.swing.JButton jBBan5;
    private javax.swing.JButton jBBan6;
    private javax.swing.JButton jBBan7;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel pnProfile;
    private javax.swing.JPanel toolNav;
    // End of variables declaration//GEN-END:variables
}

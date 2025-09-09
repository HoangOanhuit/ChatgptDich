package com.mycompany.quanlyquanan.view.VoucherMgt;


public class ActionButtons extends javax.swing.JPanel {
    public MyButtonRound getDeleteButton() {
        return deleteButton;
    }

    public void setDeleteButton(MyButtonRound deleteButton) {
        this.deleteButton = deleteButton;
    }

    public MyButtonRound getEditButton() {
        return editButton;
    }

    public void setEditButton(MyButtonRound editButton) {
        this.editButton = editButton;
    }

    public ActionButtons() {
        initComponents();
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        deleteButton = new com.mycompany.quanlyquanan.view.VoucherMgt.MyButtonRound();
        editButton = new com.mycompany.quanlyquanan.view.VoucherMgt.MyButtonRound();

        setBackground(new java.awt.Color(255, 255, 255));

        deleteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/delete.png"))); // NOI18N
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        editButton.setBackground(new java.awt.Color(211, 226, 241));
        editButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/assets/images/icons/edit.png"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(editButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_deleteButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.mycompany.quanlyquanan.view.VoucherMgt.MyButtonRound deleteButton;
    private com.mycompany.quanlyquanan.view.VoucherMgt.MyButtonRound editButton;
    // End of variables declaration//GEN-END:variables
}

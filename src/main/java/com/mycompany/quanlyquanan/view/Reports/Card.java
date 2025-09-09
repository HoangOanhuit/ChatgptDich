
package com.mycompany.quanlyquanan.view.Reports;

import com.mycompany.quanlyquanan.model.ModelCard;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;

public class Card extends javax.swing.JPanel {
    private double valueData;
    private Color moneyColor;
    public Color getColorGradient() {
        return colorGradient;
    }

    public void setColorGradient(Color colorGradient) {
        this.colorGradient = colorGradient;
    }

    private Color colorGradient;

    public Card() {
        initComponents();
        setOpaque(false);
        setBackground(new Color(112, 69, 246));
        colorGradient = new Color(221, 75, 87);

    }

    public void setData(ModelCard data) {
        DecimalFormat df = new DecimalFormat("#,##0.##");
        title.setText(data.getTitle());
        value.setText(df.format(data.getValues()));
        icon.setIcon(data.getIcon());
        this.valueData = data.getValues();

    }

    public double getValue() {
        return valueData;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        title = new javax.swing.JLabel();
        value = new javax.swing.JLabel();
        icon = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setToolTipText("");
        setPreferredSize(new java.awt.Dimension(245, 133));
        setRequestFocusEnabled(false);

        title.setBackground(new java.awt.Color(214, 217, 223));
        title.setFont(new java.awt.Font("Helvetica Neue", 1, 18)); // NOI18N
        title.setText("Title");

        value.setFont(new java.awt.Font("Helvetica Neue", 1, 24)); // NOI18N
        value.setForeground(new java.awt.Color(0, 51, 255));
        value.setText("Title");

        icon.setFont(new java.awt.Font("Helvetica Neue", 0, 10)); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(value, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                    .addComponent(title, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(28, 28, 28)
                .addComponent(icon, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(title, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(value)
                .addContainerGap(26, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(icon, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    int borderHeight = 8;
    GradientPaint bottomBorderColor = new GradientPaint(
            0, getHeight() - borderHeight, new Color(112, 69, 246), // purple start
            getWidth(), getHeight(), new Color(221, 75, 87) // pink end
    ); //set default color
    
//    private GradientPaint gradient = new GradientPaint(
//        0, getHeight() - borderHeight, new Color(112, 69, 246), // purple start
//        getWidth(), getHeight(), new Color(221, 75, 87) // pink end
//    );


    public void setBottomBorderColor(GradientPaint gradientColor) {
        this.bottomBorderColor = gradientColor;
        repaint();
    }
  
    public void setMoneycolor(Color color){
//        this.moneyColor = color;
        value.setForeground(color);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Fill background with white
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Draw gradient border at the bottom
        int borderHeight = 4;
        GradientPaint gradient = new GradientPaint(
                0, getHeight() - borderHeight, bottomBorderColor.getColor1(), // purple start
                getWidth(), getHeight(), bottomBorderColor.getColor2() // pink end
        );

        g2.setPaint(gradient);
        g2.fillRect(0, getHeight() - borderHeight, getWidth(), borderHeight);

        super.paintComponent(grphcs);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel icon;
    private javax.swing.JLabel title;
    private javax.swing.JLabel value;
    // End of variables declaration//GEN-END:variables
}

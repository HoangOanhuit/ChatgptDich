/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.quanlyquanan.view.Order;

/**
 *
 * @author Tyler
 */
import com.mycompany.quanlyquanan.model.Dish;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class LinePanel extends JPanel {
    private Dish dish;
    private JTextField quantityField;
    private JButton plus;
    private JButton minus;
    private JButton delete;
    
    public LinePanel(Dish dish, JPanel rightPanel, List<DishOrder> ol, Map<DishOrder, LinePanel> pl, JButton confirmBtn, JButton deleteAllBtn) {
        this.dish = dish;
        this.quantityField = new JTextField("1", 3);
        this.plus = new JButton("+");
        this.minus = new JButton("-");
        this.delete = new JButton("x");
        quantityField.setHorizontalAlignment(JTextField.CENTER);
        this.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        plus.addActionListener(e -> {
            int qty = Integer.parseInt(quantityField.getText());
            quantityField.setText(String.valueOf(qty + 1));
            setDishQuantity(dish, ol, qty + 1);
        });
        
        minus.addActionListener(e -> {
            int qty = Integer.parseInt(quantityField.getText());
            if(qty > 1) {
                quantityField.setText(String.valueOf(qty - 1));
                setDishQuantity(dish, ol, qty - 1);
            }
            else {
                rightPanel.remove(this);
                rightPanel.revalidate();
                rightPanel.repaint();
                removeDishQuantity(dish, ol);
                removeLinePanel(dish, pl);
                if (ol.isEmpty()) {
                    confirmBtn.setEnabled(false);
                    deleteAllBtn.setEnabled(false);
                }
            }
        });
        
        delete.addActionListener(e -> {
            rightPanel.remove(this);
            rightPanel.revalidate();
            rightPanel.repaint();
            removeDishQuantity(dish, ol);
            removeLinePanel(dish, pl);
            if (ol.isEmpty()) {
                confirmBtn.setEnabled(false);
                deleteAllBtn.setEnabled(false);
            }
        });
        
        add(new JLabel(dish.getName()));
        add(minus);
        add(quantityField);
        add(plus);
        add(delete);
    }
    
    public int getQuantity(){
        return Integer.parseInt(quantityField.getText());
    }
    
    public Dish getDish() {
        return dish;
    }
    
    public void setQuantity(int qty) {
        quantityField.setText(String.valueOf(qty));
    }
    
    private Integer getDishQuantity(Dish targetDish, List<DishOrder> ol) {
        for(DishOrder dishMap : ol) {
            if(dishMap.getDish().equals(targetDish))
                return dishMap.getQuantity();
        }
        
        return null;
    }
    
    private void setDishQuantity(Dish targetDish, List<DishOrder> ol, int qty) {
        for(DishOrder dishMap : ol) {
            if(dishMap.getDish().equals(targetDish))
                dishMap.setQuantity(qty);
        }
    }
    
    private void removeDishQuantity(Dish targetDish, List<DishOrder> ol) {
        Iterator<DishOrder> iterator = ol.iterator();
        while(iterator.hasNext()) {
            DishOrder dishOrder = iterator.next();
            if(dishOrder.getDish().equals(targetDish)) {
                iterator.remove();
            }
        }
    }
    
    private void removeLinePanel(Dish targetDish, Map<DishOrder, LinePanel> pl) {
        Iterator<Map.Entry<DishOrder, LinePanel>> iterator = pl.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<DishOrder, LinePanel> entry = iterator.next();
            if (entry.getKey().getDish().equals(targetDish)) {
                iterator.remove(); // removes the entry from the map
            }
        }
    }

    public JTextField getQuantityField() {
        return quantityField;
    }

    public JButton getPlus() {
        return plus;
    }

    public JButton getMinus() {
        return minus;
    }

    public JButton getDelete() {
        return delete;
    }
    
    
}

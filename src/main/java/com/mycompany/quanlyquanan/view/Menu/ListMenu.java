package com.mycompany.quanlyquanan.view.Menu;

import com.mycompany.quanlyquanan.model.ModelMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ListMenu<E extends Object> extends JList<E> {

    private final DefaultListModel listModel;
    private int selectedIndex = -1;
    private EventMenuSelected event;
    private int overIndex = -1;

    // Tạo một instance MenuItem duy nhất để tái sử dụng
    private final MenuItem renderer = new MenuItem(new ModelMenu("", ""));

    public void addEventMenuSelected(EventMenuSelected event) {
        this.event = event;
    }

    public ListMenu() {
        listModel = new DefaultListModel();
        setModel(listModel);
        setOpaque(false); // Đảm bảo JList cũng trong suốt

        setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof ModelMenu) {
                    ModelMenu data = (ModelMenu) value;

                    // Tái sử dụng renderer thay vì tạo mới
                    renderer.setData(data);
                    renderer.setSelected(selectedIndex == index);
                    renderer.setOver(overIndex == index);
                    return renderer;

                } else {
                    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int index = locationToIndex(e.getPoint());
                if (index >= 0 && index < listModel.size()) {
                    selectedIndex = index;
                    setSelectedIndex(selectedIndex);
                    repaint();

                    //  Gọi callback khi click
                    if (event != null) {
                        event.selected(selectedIndex);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent me) {
                overIndex = -1;
                repaint();
            }
        }
        );
    }

    public void addItem(ModelMenu item) {
        listModel.addElement(item);
    }

    @Override
    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
        super.setSelectedIndex(index); // để JList highlight đúng
        repaint();

        if (event != null && index >= 0 && index < listModel.size()) {
            event.selected(index);
        }
    }
}

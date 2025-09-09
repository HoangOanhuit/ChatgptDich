package com.mycompany.quanlyquanan.model;

import javax.swing.*;

public class ModelMenu {
    String icon;
    String name;
    public ModelMenu() {

    }
    public ModelMenu(String icon, String name) {
        this.icon = icon;
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Icon toIcon(){
        return new ImageIcon(getClass().getResource("/assets/images/icons/" + icon + ".png"));
    }

}

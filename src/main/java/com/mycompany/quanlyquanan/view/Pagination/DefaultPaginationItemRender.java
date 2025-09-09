package com.mycompany.quanlyquanan.view.Pagination;

import javax.swing.*;

public class DefaultPaginationItemRender implements PaginationItemRender {
    @Override
    public JButton createPaginationItem(Object value, boolean isPrevious, boolean isNext,boolean isFirst, boolean isLast, boolean enable) {
        JButton cmd = createButton(value, isPrevious, isNext, isFirst,isLast, enable);
        if (isPrevious) {
            Object icon = createPreviousIcon();
            if (icon != null) {
                if (icon instanceof Icon) {
                    cmd.setIcon((Icon) icon);
                } else {
                    cmd.setText(icon.toString());
                }
            }
        } else if (isNext) {
            Object icon = createNextIcon();
            if (icon != null) {
                if (icon instanceof Icon) {
                    cmd.setIcon((Icon) icon);
                } else {
                    cmd.setText(icon.toString());
                }
            }
        } else {
            cmd.setText(value.toString());
        }
        if (!enable) {
            cmd.setFocusable(false);
        }
        return cmd;
    }

    @Override
    public JButton createButton(Object value, boolean isPrevious, boolean isNext, boolean isFirst, boolean isLast,boolean enable) {
        return new JButton();
    }

    @Override
    public Object createPreviousIcon() {
        return "Previous";
    }

    @Override
    public Object createNextIcon() {
        return "Next";
    }

    @Override
    public Object createFirstIcon() {
        return "First";
    }
    @Override
    public Object createLastIcon() {
        return "Last";
    }
}

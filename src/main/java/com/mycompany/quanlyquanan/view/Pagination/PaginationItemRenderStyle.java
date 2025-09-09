package com.mycompany.quanlyquanan.view.Pagination;

import javax.swing.*;

public class PaginationItemRenderStyle extends DefaultPaginationItemRender {
    @Override
    public JButton createButton(Object value, boolean isPrevious, boolean isNext,boolean isFirst, boolean isLast, boolean enable) {
        JButton button = super.createButton(value, isPrevious, isNext, isFirst, isLast,enable);
        button.setUI(new ButtonUI());
        return button;
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

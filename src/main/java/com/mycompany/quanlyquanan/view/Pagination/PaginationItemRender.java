package com.mycompany.quanlyquanan.view.Pagination;

import javax.swing.*;

public interface PaginationItemRender {
    JButton createPaginationItem(Object value, boolean isPrevious, boolean isNext, boolean isFirst, boolean isLast, boolean enable);

    JButton createButton(Object value, boolean isPrevious, boolean isNext, boolean isFirst, boolean isLast, boolean enable);

    Object createPreviousIcon();

    Object createNextIcon();

    Object createFirstIcon();

    Object createLastIcon();
}

package com.mycompany.quanlyquanan.view.Pagination;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Pagination extends javax.swing.JPanel {
    public PaginationItemRender getPaginationItemRender() {
        return paginationItemRender;
    }

    public void setPaginationItemRender(PaginationItemRender paginationItemRender) {
        this.paginationItemRender = paginationItemRender;
        changePage(page.getCurrent(), page.getTotalPage());
    }

    private PaginationItemRender paginationItemRender;
    private List<EventPagination> events = new ArrayList<>();
    private Page page;

    public Pagination() {
        init();
    }

    private void init() {
        paginationItemRender = new DefaultPaginationItemRender();
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));
        setPagegination(1, 1);
    }

    private void runEvent() {
        for (EventPagination event : events) {
            event.pageChanged(page.getCurrent());
        }
    }

    private boolean isEnable(Object item) {
        return (item instanceof Page.BreakLabel || Integer.valueOf(item.toString()) != page.getCurrent());
    }

    public void addEventPagination(EventPagination event) {
        events.add(event);
    }

    public void setPagegination(int current, int totalPage) {
        if (current > totalPage) {
            current = totalPage;
        }
        if (page == null || (page.getCurrent() != current || page.getTotalPage() != totalPage)) {
            changePage(current, totalPage);
        }
    }

    private void changePage(int current, int totalPage) {
        page = new Page(current, current > 1, current < totalPage,  current == 1, current == totalPage, getSimpleItems(current, totalPage), totalPage);
        removeAll();
        refresh();

        final int finalTotalPage = totalPage;
        final int finalCurrent = current;

        // Set layout with 3px horizontal gap between buttons
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        // First button
        JButton cmdFirst = new com.mycompany.quanlyquanan.view.VoucherMgt.MyButtonSecond();
        cmdFirst.setText("First");
        cmdFirst.setEnabled(page.isPrevious());
        cmdFirst.addActionListener(e -> {
            if (finalCurrent > 1) {
                setPagegination(1, finalTotalPage);
                runEvent();
            }
        });
        add(cmdFirst);
        add(Box.createHorizontalStrut(3));

        // Previous button
        JButton cmdPrev = new com.mycompany.quanlyquanan.view.VoucherMgt.MyButtonSecond();
        cmdPrev.setText("Previous");
        cmdPrev.setEnabled(page.isPrevious());
        cmdPrev.addActionListener(e -> {
            if (finalCurrent > 1) {
                setPagegination(finalCurrent - 1, finalTotalPage);
                runEvent();
            }
        });
        add(cmdPrev);
        add(Box.createHorizontalStrut(3));

        // Page numbers logic
        int start, end;
        if (finalTotalPage <= 3) {
            start = 1;
            end = finalTotalPage;
        } else if (finalCurrent == 1) {
            start = 1;
            end = 3;
        } else if (finalCurrent == finalTotalPage) {
            start = Math.max(1, finalTotalPage - 2);
            end = finalTotalPage;
        } else {
            start = finalCurrent - 1;
            end = finalCurrent + 1;
        }
        for (int i = start; i <= end; i++) {
            final int pageNum = i;
            JButton cmd = paginationItemRender.createPaginationItem(pageNum, false, false, false, false, pageNum != finalCurrent);
            if (pageNum == finalCurrent) {
                cmd.setSelected(true);
            }
            cmd.addActionListener(e -> {
                if (!cmd.isSelected()) {
                    setPagegination(pageNum, finalTotalPage);
                    runEvent();
                }
            });
            add(cmd);
            if (i < end) add(Box.createHorizontalStrut(3));
        }

        // Next button
        JButton cmdNext = new com.mycompany.quanlyquanan.view.VoucherMgt.MyButtonSecond();
        cmdNext.setText("Next");
        cmdNext.setEnabled(page.isNext());
        cmdNext.addActionListener(e -> {
            if (finalCurrent < finalTotalPage) {
                setPagegination(finalCurrent + 1, finalTotalPage);
                runEvent();
            }
        });
        add(Box.createHorizontalStrut(3));
        add(cmdNext);
        add(Box.createHorizontalStrut(3));

        // Last button
        JButton cmdLast = new com.mycompany.quanlyquanan.view.VoucherMgt.MyButtonSecond();
        cmdLast.setText("Last");
        cmdLast.setEnabled(page.isNext());
        cmdLast.addActionListener(e -> {
            if (finalCurrent < finalTotalPage) {
                setPagegination(finalTotalPage, finalTotalPage);
                runEvent();
            }
        });
        add(cmdLast);
    }

    // Helper to get only 3 page numbers: current-1, current, current+1
    private List<Object> getSimpleItems(int current, int totalPage) {
        List<Object> items = new ArrayList<>();
        int start, end;
        if (totalPage <= 3) {
            start = 1;
            end = totalPage;
        } else if (current == 1) {
            start = 1;
            end = 3;
        } else if (current == totalPage) {
            start = Math.max(1, totalPage - 2);
            end = totalPage;
        } else {
            start = current - 1;
            end = current + 1;
        }
        for (int i = start; i <= end; i++) {
            items.add(i);
        }
        return items;
    }

    private void refresh() {
        repaint();
        revalidate();
    }

    private Page paginate(int current, int max) {

        boolean prev = current > 1;
        boolean next = current < max;
        boolean first = current == 1;
        boolean last = current == max;
        List<Object> items = new ArrayList<>();
        items.add(1);
        if (current == 1 && max == 1) {
            return new Page(current, prev, next, first, last,items, max);
        }
        int r = 2;
        int r1 = current - r;
        int r2 = current + r;
        if (current > 4) {
            items.add(new Page.BreakLabel((r1 > 2 ? r1 : 2) - 1));
        }
        for (int i = r1 > 2 ? r1 : 2; i <= Math.min(max, r2); i++) {
            items.add(i);
        }
        if (r2 + 1 < max) {
            items.add(new Page.BreakLabel(Integer.valueOf(items.get(items.size() - 1).toString()) + 1));
        }
        if (r2 < max) {
            items.add(max);
        }
        return new Page(current, prev, next, first,last, items, max);
    }

}

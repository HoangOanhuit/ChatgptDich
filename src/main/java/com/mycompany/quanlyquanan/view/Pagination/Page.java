package com.mycompany.quanlyquanan.view.Pagination;

import java.util.List;

public class Page {
    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public boolean isPrevious() {
        return previous;
    }

    public void setPrevious(boolean previous) {
        this.previous = previous;
    }

    public boolean isNext() {
        return next;
    }

    public void setNext(boolean next) {
        this.next = next;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }

    public List<Object> getItems() {
        return items;
    }

    public void setItems(List<Object> items) {
        this.items = items;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public Page(int current, boolean previous, boolean next, boolean first, boolean last, List<Object> items, int totalPage) {
        this.current = current;
        this.previous = previous;
        this.next = next;
        this.first = first;
        this.last = last;
        this.items = items;
        this.totalPage = totalPage;
    }

    public Page() {
    }

    private int current;
    private boolean previous;
    private boolean next;
    private boolean first;
    private boolean last;
    private List<Object> items;
    private int totalPage;

    @Override
    public String toString() {
        return "current: " + current + "\n" + previous + " " + items.toString() + " " + next;
    }

    public static class BreakLabel {

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public BreakLabel(int page) {
            this.page = page;
        }

        public BreakLabel() {
        }

        private int page;

        @Override
        public String toString() {
            return "...";
        }
    }
}

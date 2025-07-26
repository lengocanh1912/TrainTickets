package t3h.edu.vn.traintickets.dto;

import java.util.List;

public class DatatableResponse<Entity> {

    List<Entity> data;

    int total;// Tổng số phần tử
    int totalPage;// tổng số trang
    int perpage;// Số phần tử trên 1 trang

    int page;
    String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public DatatableResponse() {
    }

    public DatatableResponse(List<Entity> data, int total, int totalPage, int perpage, int page) {
        this.data = data;
        this.total = total;
        this.totalPage = totalPage;
        this.perpage = perpage;
        this.page = page;
    }

    public List<Entity> getData() {
        return data;
    }

    public void setData(List<Entity> data) {
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getPerpage() {
        return perpage;
    }

    public void setPerpage(int perpage) {
        this.perpage = perpage;
    }
}


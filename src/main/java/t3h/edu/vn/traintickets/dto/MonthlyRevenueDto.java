package t3h.edu.vn.traintickets.dto;

public class MonthlyRevenueDto {
    private int month;
    private float revenue;

    public MonthlyRevenueDto(int month, float revenue) {
        this.month = month;
        this.revenue = revenue;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setRevenue(float revenue) {
        this.revenue = revenue;
    }

    public int getMonth() { return month; }
    public float getRevenue() { return revenue; }
}


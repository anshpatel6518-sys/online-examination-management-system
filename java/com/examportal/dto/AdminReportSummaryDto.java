package com.examportal.dto;

import java.util.ArrayList;
import java.util.List;

public class AdminReportSummaryDto {

    private int totalAttempts;
    private int passCount;
    private int failCount;
    private double passPercentage;
    private double averageScore;
    private List<AdminReportItemDto> items = new ArrayList<>();

    public AdminReportSummaryDto() {
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getPassCount() {
        return passCount;
    }

    public void setPassCount(int passCount) {
        this.passCount = passCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }

    public double getPassPercentage() {
        return passPercentage;
    }

    public void setPassPercentage(double passPercentage) {
        this.passPercentage = passPercentage;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public List<AdminReportItemDto> getItems() {
        return items;
    }

    public void setItems(List<AdminReportItemDto> items) {
        this.items = items;
    }
}

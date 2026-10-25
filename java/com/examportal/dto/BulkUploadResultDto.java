package com.examportal.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO summarizing the outcome of a bulk question CSV upload.
 */
public class BulkUploadResultDto {

    private int totalRows;
    private int addedCount;
    private int rejectedCount;
    private List<RowError> errors = new ArrayList<>();

    public BulkUploadResultDto() {
    }

    public void addSuccess() {
        this.addedCount++;
        this.totalRows++;
    }

    public void addError(int rowNumber, String reason) {
        this.rejectedCount++;
        this.totalRows++;
        this.errors.add(new RowError(rowNumber, reason));
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getAddedCount() {
        return addedCount;
    }

    public void setAddedCount(int addedCount) {
        this.addedCount = addedCount;
    }

    public int getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(int rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public List<RowError> getErrors() {
        return errors;
    }

    public void setErrors(List<RowError> errors) {
        this.errors = errors;
    }

    /**
     * Inner class representing an error for a specific row.
     */
    public static class RowError {
        private int rowNumber;
        private String reason;

        public RowError(int rowNumber, String reason) {
            this.rowNumber = rowNumber;
            this.reason = reason;
        }

        public int getRowNumber() {
            return rowNumber;
        }

        public String getReason() {
            return reason;
        }
    }
}

package com.studdict.mobile.model;

public class EBookLoanInfo {
    private Long loanId;
    private Long ebookId;
    private String title;
    private String author;
    private boolean returned;

    public Long getLoanId() { return loanId; }
    public Long getEbookId() { return ebookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isReturned() { return returned; }
}

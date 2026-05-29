package com.studdict.model;

public class EBookLoanDTO {

    private Long loanId;
    private Long ebookId;
    private String title;
    private String author;

    public EBookLoanDTO() {}

    public EBookLoanDTO(Long loanId, Long ebookId, String title, String author) {
        this.loanId = loanId;
        this.ebookId = ebookId;
        this.title = title;
        this.author = author;
    }

    public Long getLoanId() { return loanId; }
    public Long getEbookId() { return ebookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
}

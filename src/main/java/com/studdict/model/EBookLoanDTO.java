package com.studdict.model;

public class EBookLoanDTO {

    private Long loanId;
    private Long ebookId;
    private String title;
    private String author;
    // true αν ο δανεισμός έχει ήδη επιστραφεί/λήξει (isActive = false).
    private boolean returned;

    public EBookLoanDTO() {}

    public EBookLoanDTO(Long loanId, Long ebookId, String title, String author) {
        this(loanId, ebookId, title, author, false);
    }

    public EBookLoanDTO(Long loanId, Long ebookId, String title, String author, boolean returned) {
        this.loanId = loanId;
        this.ebookId = ebookId;
        this.title = title;
        this.author = author;
        this.returned = returned;
    }

    public Long getLoanId() { return loanId; }
    public Long getEbookId() { return ebookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isReturned() { return returned; }
}

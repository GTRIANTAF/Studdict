package com.studdict.model;

import java.util.List;

/**
 * UC7 βήμα 7 — περιεχόμενο ενός E-book χωρισμένο σε σελίδες, ώστε ο E-book Reader
 * να μπορεί να εμφανίσει το βιβλίο σελίδα-σελίδα.
 */
public class EBookContentDTO {

    private Long ebookId;
    private String title;
    private String author;
    private List<String> pages;

    public EBookContentDTO() {}

    public EBookContentDTO(Long ebookId, String title, String author, List<String> pages) {
        this.ebookId = ebookId;
        this.title = title;
        this.author = author;
        this.pages = pages;
    }

    public Long getEbookId() { return ebookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public List<String> getPages() { return pages; }
}

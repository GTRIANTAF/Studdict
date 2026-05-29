package com.studdict.mobile.model;

import java.util.List;

/** UC7: a book's content split into pages, used by the e-book reader. */
public class EBookContent {
    private Long ebookId;
    private String title;
    private String author;
    private List<String> pages;

    public Long getEbookId() { return ebookId; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public List<String> getPages() { return pages; }
}
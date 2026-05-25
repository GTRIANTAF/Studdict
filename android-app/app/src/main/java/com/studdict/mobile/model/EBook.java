package com.studdict.mobile.model;

public class EBook {
    private Long id;
    private String title;
    private String author;
    private String content;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getContent() { return content; }

    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setContent(String content) { this.content = content; }
}

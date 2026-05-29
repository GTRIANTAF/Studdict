package com.studdict.model;

public class EBookCatalogDTO {

    private Long id;
    private String title;
    private String author;
    private String category;
    private boolean available;

    public EBookCatalogDTO() {}

    public EBookCatalogDTO(Long id, String title, String author, String category, boolean available) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.available = available;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public boolean isAvailable() { return available; }
}

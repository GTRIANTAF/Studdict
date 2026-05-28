package com.studdict.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "ebooks")
public class EBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ebook_id")
    private Long eBookId;

    private String title;
    private String author;
    private String isbn;
    private String category;

    @JsonIgnore
    @OneToMany(mappedBy = "ebook", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EBookLicense> licenses = new ArrayList<>();

    public EBook() {}

    public Long geteBookId() {
        return eBookId;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getCategory() {
        return category;
    }

    public List<EBookLicense> getLicenses() {
        return licenses;
    }

    public void setLicenses(List<EBookLicense> licenses) {
        this.licenses = licenses;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void seteBookId(Long eBookId) {
        this.eBookId = eBookId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }


}
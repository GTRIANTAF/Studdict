package com.studdict.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "ebook", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EBookLicense> licenses = new ArrayList<>();

    public EBook() {}

    // Add Getters and Setters
}
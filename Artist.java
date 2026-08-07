package com.example.musiclibrarydb.sqlite.model;

public class Artist {
    private long id;
    private String name;
    private Genre genre;

    public Artist() {
    }

    public Artist(String name, Genre genre) {
        this.name = name;
        this.genre = genre;
    }

    public Artist(long id, String name, Genre genre) {
        this.id = id;
        this.name = name;
        this.genre = genre;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }
}

package com.example.musiclibrarydb.sqlite.model;

public class Song {
    private long id;
    private String title;
    private Artist artist;
    private Genre genre;

    public Song() {
    }

    public Song(String title, Artist artist, Genre genre) {
        this.title = title;
        this.artist = artist;
        this.genre = genre;
    }

    public Song(long id, String title, Artist artist, Genre genre) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.genre = genre;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }
}

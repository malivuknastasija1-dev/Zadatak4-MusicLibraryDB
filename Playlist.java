package com.example.musiclibrarydb.sqlite.model;

import java.util.ArrayList;
public class Playlist {
    private long id;
    private String title;
    private User user;
    private ArrayList<Song> songs;

    public Playlist() {
        this.songs = new ArrayList<>();
    }

    public Playlist(String title, User user) {
        this.title = title;
        this.user = user;
        this.songs = new ArrayList<>();
    }

    public Playlist(long id, String title, User user) {
        this.id = id;
        this.title = title;
        this.user = user;
        this.songs = new ArrayList<>();
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }

    public void addSong(Song song) {
        this.songs.add(song);
    }
}

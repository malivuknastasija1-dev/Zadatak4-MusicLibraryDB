package com.example.musiclibrarydb.sqlite.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import com.example.musiclibrarydb.sqlite.model.Artist;
import com.example.musiclibrarydb.sqlite.model.Genre;
import com.example.musiclibrarydb.sqlite.model.Playlist;
import com.example.musiclibrarydb.sqlite.model.Song;
import com.example.musiclibrarydb.sqlite.model.User;

public class DatabaseHelper extends SQLiteOpenHelper{
    private SQLiteDatabase db;

    private static final String LOG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MusicLibraryDBManager";

    private static final String TABLE_USERS = "users";
    private static final String TABLE_GENRES = "genres";
    private static final String TABLE_ARTISTS = "artists";
    private static final String TABLE_SONGS = "songs";
    private static final String TABLE_PLAYLISTS = "playlists";
    private static final String TABLE_PLAYLIST_SONGS = "playlist_songs";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_TITLE = "title";

    // USERS Tabela
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    // ARTISTS Tabela
    private static final String KEY_GENRE_ID = "genre_id";

    // SONGS Tabela
    private static final String KEY_ARTIST_ID = "artist_id";

    // PLAYLISTS Tabela
    private static final String KEY_USER_ID = "user_id";

    // PLAYLIST_SONGS Tabela
    private static final String KEY_PLAYLIST_ID = "playlist_id";
    private static final String KEY_SONG_ID = "song_id";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_USERS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_USERNAME + " TEXT," + KEY_PASSWORD + " TEXT" + ")";

    private static final String CREATE_TABLE_GENRES = "CREATE TABLE IF NOT EXISTS "
            + TABLE_GENRES + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_NAME + " TEXT" + ")";

    private static final String CREATE_TABLE_ARTISTS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_ARTISTS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_NAME + " TEXT," + KEY_GENRE_ID + " INTEGER" + ")";

    private static final String CREATE_TABLE_SONGS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_SONGS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_TITLE + " TEXT," + KEY_ARTIST_ID + " INTEGER,"
            + KEY_GENRE_ID + " INTEGER" + ")";

    private static final String CREATE_TABLE_PLAYLISTS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_PLAYLISTS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_TITLE + " TEXT," + KEY_USER_ID + " INTEGER" + ")";

    private static final String CREATE_TABLE_PLAYLIST_SONGS = "CREATE TABLE IF NOT EXISTS "
            + TABLE_PLAYLIST_SONGS + "(" + KEY_ID + " INTEGER PRIMARY KEY,"
            + KEY_PLAYLIST_ID + " INTEGER," + KEY_SONG_ID + " INTEGER" + ")";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_GENRES);
        db.execSQL(CREATE_TABLE_ARTISTS);
        db.execSQL(CREATE_TABLE_SONGS);
        db.execSQL(CREATE_TABLE_PLAYLISTS);
        db.execSQL(CREATE_TABLE_PLAYLIST_SONGS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        onCreate(db);
    }

    public void createTables() {
        if (db == null || !db.isOpen()) {
            db = getWritableDatabase();
        }
        onCreate(db);
    }

    public void dropTables() {
        if (db == null || !db.isOpen()) {
            db = getWritableDatabase();
        }
        dropTables(db);
    }

    private void dropTables(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLIST_SONGS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYLISTS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_ARTISTS);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_GENRES);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
    }

    public void closeDB() {
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    //          USER        //
    public User loginOrRegister(String username, String password) throws Exception {
        String selectQuery = "SELECT * FROM " + TABLE_USERS + " WHERE "
                + KEY_USERNAME + " = ?";
        Cursor c = db.rawQuery(selectQuery, new String[]{username});

        if (c != null && c.moveToFirst()) {
            //sign in
            String dbPassword = c.getString(c.getColumnIndexOrThrow(KEY_PASSWORD));

            if (!dbPassword.equals(password)) {
                c.close();
                throw new Exception("Pogrešna lozinka za korisnika " + username + "!");
            }

            User user = new User();
            user.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
            user.setUsername(c.getString(c.getColumnIndexOrThrow(KEY_USERNAME)));
            user.setPassword(dbPassword);
            c.close();
            return user;
        } else {
            //registration
            if (c != null) c.close();
            ContentValues values = new ContentValues();
            values.put(KEY_USERNAME, username);
            values.put(KEY_PASSWORD, password);

            long id = db.insert(TABLE_USERS, null, values);
            return new User(id, username, password);
        }
    }

    //      GENRES      //
    public long createGenre(Genre genre) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, genre.getName());
        long id = db.insert(TABLE_GENRES, null, values);
        genre.setId(id);
        return id;
    }

    public Genre getGenre(long id) {
        String selectQuery = "SELECT * FROM " + TABLE_GENRES + " WHERE " + KEY_ID + " = " + id;
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.moveToFirst()) {
            Genre g = new Genre();
            g.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
            g.setName(c.getString(c.getColumnIndexOrThrow(KEY_NAME)));
            c.close();
            return g;
        }
        return null;
    }

    public ArrayList<Genre> getAllGenres() {
        ArrayList<Genre> genres = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GENRES;
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                Genre g = new Genre();
                g.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
                g.setName(c.getString(c.getColumnIndexOrThrow(KEY_NAME)));
                genres.add(g);
            } while (c.moveToNext());
        }
        c.close();
        return genres;
    }

    public int updateGenre(Genre genre) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, genre.getName());
        return db.update(TABLE_GENRES, values, KEY_ID + " = ?", new String[]{String.valueOf(genre.getId())});
    }

    public boolean existsGenre(String name) {
        String query = "SELECT 1 FROM " + TABLE_GENRES + " WHERE " + KEY_NAME + " LIKE ?";
        Cursor cursor = db.rawQuery(query, new String[]{name});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public ArrayList<Genre> searchGenresByText(String text) {
        ArrayList<Genre> genres = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_GENRES + " WHERE " + KEY_NAME + " LIKE ?";
        Cursor c = db.rawQuery(selectQuery, new String[]{"%" + text.trim() + "%"});

        if (c.moveToFirst()) {
            do {
                Genre g = new Genre();
                g.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
                g.setName(c.getString(c.getColumnIndexOrThrow(KEY_NAME)));
                genres.add(g);
            } while (c.moveToNext());
        }
        c.close();
        return genres;
    }

    public void deleteGenre(long genreId) {
        String deletePlaylistSongs = "DELETE FROM " + TABLE_PLAYLIST_SONGS +
                " WHERE " + KEY_SONG_ID + " IN (SELECT " + KEY_ID + " FROM " + TABLE_SONGS + " WHERE " + KEY_GENRE_ID + " = ?)";
        db.execSQL(deletePlaylistSongs, new String[]{String.valueOf(genreId)});
        db.delete(TABLE_SONGS, KEY_GENRE_ID + " = ?", new String[]{String.valueOf(genreId)});
        db.delete(TABLE_ARTISTS, KEY_GENRE_ID + " = ?", new String[]{String.valueOf(genreId)});
        db.delete(TABLE_GENRES, KEY_ID + " = ?", new String[]{String.valueOf(genreId)});
    }

    //      ARTISTS     //
    public long createArtist(Artist artist) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, artist.getName());
        values.put(KEY_GENRE_ID, artist.getGenre().getId());
        long id = db.insert(TABLE_ARTISTS, null, values);
        artist.setId(id);
        return id;
    }

    public Artist getArtist(long id) {
        String selectQuery = "SELECT * FROM " + TABLE_ARTISTS + " WHERE " + KEY_ID + " = " + id;
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.moveToFirst()) {
            Artist a = new Artist();
            a.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
            a.setName(c.getString(c.getColumnIndexOrThrow(KEY_NAME)));
            long genreId = c.getLong(c.getColumnIndexOrThrow(KEY_GENRE_ID));
            a.setGenre(getGenre(genreId));
            c.close();
            return a;
        }
        return null;
    }

    public ArrayList<Artist> getAllArtists() {
        ArrayList<Artist> artists = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ARTISTS;
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                Artist a = new Artist();
                a.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
                a.setName(c.getString(c.getColumnIndexOrThrow(KEY_NAME)));
                long genreId = c.getLong(c.getColumnIndexOrThrow(KEY_GENRE_ID));
                a.setGenre(getGenre(genreId));
                artists.add(a);
            } while (c.moveToNext());
        }
        c.close();
        return artists;
    }

    public ArrayList<Artist> getArtistsByGenre(long genreId) {
        ArrayList<Artist> artists = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ARTISTS + " WHERE " + KEY_GENRE_ID + " = ?";
        Cursor c = db.rawQuery(selectQuery, new String[]{String.valueOf(genreId)});
        if (c.moveToFirst()) {
            do {
                Artist a = new Artist();
                a.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
                a.setName(c.getString(c.getColumnIndexOrThrow(KEY_NAME)));
                a.setGenre(getGenre(genreId));
                artists.add(a);
            } while (c.moveToNext());
        }
        c.close();
        return artists;
    }

    public int updateArtist(Artist artist) {
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, artist.getName());
        values.put(KEY_GENRE_ID, artist.getGenre().getId());
        return db.update(TABLE_ARTISTS, values, KEY_ID + " = ?", new String[]{String.valueOf(artist.getId())});
    }

    public boolean existsArtist(String name, long genreId) {
        String query = "SELECT 1 FROM " + TABLE_ARTISTS + " WHERE " + KEY_NAME + " LIKE ? AND " + KEY_GENRE_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name, String.valueOf(genreId)});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public ArrayList<Artist> searchArtistsByText(String text) {
        ArrayList<Artist> artists = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_ARTISTS + " WHERE " + KEY_NAME + " LIKE ?";
        Cursor c = db.rawQuery(selectQuery, new String[]{"%" + text.trim() + "%"});

        if (c.moveToFirst()) {
            do {
                Artist a = new Artist();
                a.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
                a.setName(c.getString(c.getColumnIndexOrThrow(KEY_NAME)));
                a.setGenre(getGenre(c.getLong(c.getColumnIndexOrThrow(KEY_GENRE_ID))));
                artists.add(a);
            } while (c.moveToNext());
        }
        c.close();
        return artists;
    }

    public void deleteArtist(long artistId) {
        String deletePlaylistSongs = "DELETE FROM " + TABLE_PLAYLIST_SONGS +
                " WHERE " + KEY_SONG_ID + " IN (SELECT " + KEY_ID + " FROM " + TABLE_SONGS + " WHERE " + KEY_ARTIST_ID + " = ?)";
        db.execSQL(deletePlaylistSongs, new String[]{String.valueOf(artistId)});
        db.delete(TABLE_SONGS, KEY_ARTIST_ID + " = ?", new String[]{String.valueOf(artistId)});
        db.delete(TABLE_ARTISTS, KEY_ID + " = ?", new String[]{String.valueOf(artistId)});
    }

    //      SONGS       //
    public long createSong(Song song) {
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, song.getTitle());
        values.put(KEY_ARTIST_ID, song.getArtist().getId());
        values.put(KEY_GENRE_ID, song.getGenre().getId());
        long id = db.insert(TABLE_SONGS, null, values);
        song.setId(id);
        return id;
    }

    public Song getSong(long id) {
        String selectQuery = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_ID + " = " + id;
        Cursor c = db.rawQuery(selectQuery, null);
        if (c != null && c.moveToFirst()) {
            Song s = new Song();
            s.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
            s.setTitle(c.getString(c.getColumnIndexOrThrow(KEY_TITLE)));
            s.setArtist(getArtist(c.getLong(c.getColumnIndexOrThrow(KEY_ARTIST_ID))));
            s.setGenre(getGenre(c.getLong(c.getColumnIndexOrThrow(KEY_GENRE_ID))));
            c.close();
            return s;
        }
        return null;
    }

    public ArrayList<Song> getAllSongs() {
        ArrayList<Song> songs = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SONGS;
        Cursor c = db.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            do {
                Song s = new Song();
                s.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
                s.setTitle(c.getString(c.getColumnIndexOrThrow(KEY_TITLE)));
                s.setArtist(getArtist(c.getLong(c.getColumnIndexOrThrow(KEY_ARTIST_ID))));
                s.setGenre(getGenre(c.getLong(c.getColumnIndexOrThrow(KEY_GENRE_ID))));
                songs.add(s);
            } while (c.moveToNext());
        }
        c.close();
        return songs;
    }

    public ArrayList<Song> searchSongs(long artistId, long genreId) {
        ArrayList<Song> songs = new ArrayList<>();
        StringBuilder selectQuery = new StringBuilder("SELECT * FROM " + TABLE_SONGS + " WHERE 1=1");
        ArrayList<String> args = new ArrayList<>();

        if (artistId > 0) {
            selectQuery.append(" AND ").append(KEY_ARTIST_ID).append(" = ?");
            args.add(String.valueOf(artistId));
        }

        if (genreId > 0) {
            selectQuery.append(" AND ").append(KEY_GENRE_ID).append(" = ?");
            args.add(String.valueOf(genreId));
        }

        Cursor c = db.rawQuery(selectQuery.toString(), args.toArray(new String[0]));
        if (c.moveToFirst()) {
            do {
                Song s = new Song();
                s.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
                s.setTitle(c.getString(c.getColumnIndexOrThrow(KEY_TITLE)));
                s.setArtist(getArtist(c.getLong(c.getColumnIndexOrThrow(KEY_ARTIST_ID))));
                s.setGenre(getGenre(c.getLong(c.getColumnIndexOrThrow(KEY_GENRE_ID))));
                songs.add(s);
            } while (c.moveToNext());
        }
        c.close();
        return songs;
    }

    public int updateSong(Song song) {
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, song.getTitle());

        if (song.getArtist() != null) {
            values.put(KEY_ARTIST_ID, song.getArtist().getId());
        }
        if (song.getGenre() != null) {
            values.put(KEY_GENRE_ID, song.getGenre().getId());
        }

        return db.update(TABLE_SONGS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(song.getId())});
    }

    public boolean existsSong(String title, long artistId) {
        String query = "SELECT 1 FROM " + TABLE_SONGS + " WHERE " + KEY_TITLE + " LIKE ? AND " + KEY_ARTIST_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{title, String.valueOf(artistId)});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public ArrayList<Song> searchSongsByText(String text) {
        ArrayList<Song> songs = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_SONGS + " WHERE " + KEY_TITLE + " LIKE ?";
        Cursor c = db.rawQuery(selectQuery, new String[]{"%" + text.trim() + "%"});

        if (c.moveToFirst()) {
            do {
                Song s = new Song();
                s.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
                s.setTitle(c.getString(c.getColumnIndexOrThrow(KEY_TITLE)));
                s.setArtist(getArtist(c.getLong(c.getColumnIndexOrThrow(KEY_ARTIST_ID))));
                s.setGenre(getGenre(c.getLong(c.getColumnIndexOrThrow(KEY_GENRE_ID))));
                songs.add(s);
            } while (c.moveToNext());
        }
        c.close();
        return songs;
    }

    public void deleteSong(long songId) {
        db.delete(TABLE_PLAYLIST_SONGS, KEY_SONG_ID + " = ?", new String[]{String.valueOf(songId)});
        db.delete(TABLE_SONGS, KEY_ID + " = ?", new String[]{String.valueOf(songId)});
    }

    //      PLAYLISTS       //
    public long createPlaylist(Playlist playlist) {
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, playlist.getTitle());
        values.put(KEY_USER_ID, playlist.getUser().getId());
        long playlistId = db.insert(TABLE_PLAYLISTS, null, values);
        playlist.setId(playlistId);
        return playlistId;
    }

    public int updatePlaylist(Playlist playlist) {
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, playlist.getTitle());
        return db.update(TABLE_PLAYLISTS, values, KEY_ID + " = ?",
                new String[]{String.valueOf(playlist.getId())});
    }

    public boolean existsPlaylist(String title, long userId) {
        String query = "SELECT 1 FROM " + TABLE_PLAYLISTS + " WHERE " + KEY_TITLE + " LIKE ? AND " + KEY_USER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{title, String.valueOf(userId)});
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    public ArrayList<Playlist> searchPlaylistsByText(String text, long userId) {
        ArrayList<Playlist> playlists = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PLAYLISTS + " WHERE " + KEY_TITLE + " LIKE ? AND " + KEY_USER_ID + " = ?";
        Cursor c = db.rawQuery(selectQuery, new String[]{"%" + text.trim() + "%", String.valueOf(userId)});

        if (c.moveToFirst()) {
            do {
                Playlist p = new Playlist();
                p.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
                p.setTitle(c.getString(c.getColumnIndexOrThrow(KEY_TITLE)));
                p.setSongs(getSongsForPlaylist(p.getId()));
                playlists.add(p);
            } while (c.moveToNext());
        }
        c.close();
        return playlists;
    }

    public void addSongToPlaylist(long playlistId, long songId) {
        ContentValues values = new ContentValues();
        values.put(KEY_PLAYLIST_ID, playlistId);
        values.put(KEY_SONG_ID, songId);
        db.insert(TABLE_PLAYLIST_SONGS, null, values);
    }

    public void removeSongFromPlaylist(long playlistId, long songId) {
        db.delete(TABLE_PLAYLIST_SONGS, KEY_PLAYLIST_ID + " = ? AND " + KEY_SONG_ID + " = ?",
                new String[]{String.valueOf(playlistId), String.valueOf(songId)});
    }

    public ArrayList<Playlist> getUserPlaylists(User user) {
        ArrayList<Playlist> playlists = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_PLAYLISTS + " WHERE " + KEY_USER_ID + " = ?";
        Cursor c = db.rawQuery(selectQuery, new String[]{String.valueOf(user.getId())});
        if (c.moveToFirst()) {
            do {
                Playlist p = new Playlist();
                p.setId(c.getLong(c.getColumnIndexOrThrow(KEY_ID)));
                p.setTitle(c.getString(c.getColumnIndexOrThrow(KEY_TITLE)));
                p.setUser(user);

                // Učitavamo i sve pesme sa te plejliste
                p.setSongs(getSongsForPlaylist(p.getId()));
                playlists.add(p);
            } while (c.moveToNext());
        }
        c.close();
        return playlists;
    }

    public ArrayList<Song> getSongsForPlaylist(long playlistId) {
        ArrayList<Song> songs = new ArrayList<>();
        String selectQuery = "SELECT s." + KEY_ID + " FROM " + TABLE_SONGS + " s, "
                + TABLE_PLAYLIST_SONGS + " ps WHERE ps." + KEY_PLAYLIST_ID + " = ? AND ps."
                + KEY_SONG_ID + " = s." + KEY_ID;
        Cursor c = db.rawQuery(selectQuery, new String[]{String.valueOf(playlistId)});
        if (c.moveToFirst()) {
            do {
                long songId = c.getLong(c.getColumnIndexOrThrow(KEY_ID));
                songs.add(getSong(songId));
            } while (c.moveToNext());
        }
        c.close();
        return songs;
    }

    public void deletePlaylist(long playlistId) {
        db.delete(TABLE_PLAYLIST_SONGS, KEY_PLAYLIST_ID + " = ?", new String[]{String.valueOf(playlistId)});
        db.delete(TABLE_PLAYLISTS, KEY_ID + " = ?", new String[]{String.valueOf(playlistId)});
    }

}


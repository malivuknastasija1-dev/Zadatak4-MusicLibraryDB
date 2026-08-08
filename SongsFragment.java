package com.example.musiclibrarydb;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import com.example.musiclibrarydb.sqlite.helper.DatabaseHelper;
import com.example.musiclibrarydb.sqlite.model.Artist;
import com.example.musiclibrarydb.sqlite.model.Genre;
import com.example.musiclibrarydb.sqlite.model.Song;

public class SongsFragment extends Fragment {

    private EditText songTitleEditText;
    private Spinner artistSpinner, genreSpinner;
    private Spinner filterArtistSpinner, filterGenreSpinner;
    private Button addSongButton, searchButton, resetSearchButton;
    private ListView songsListView;

    private DatabaseHelper dbHelper;

    private ArrayList<Artist> artistList = new ArrayList<>();
    private ArrayList<Genre> genreList = new ArrayList<>();
    private ArrayList<Song> songList = new ArrayList<>();

    private ArrayList<String> artistNames = new ArrayList<>();
    private ArrayList<String> genreNames = new ArrayList<>();
    private ArrayList<String> filterArtistNames = new ArrayList<>();
    private ArrayList<String> filterGenreNames = new ArrayList<>();

    private ArrayList<String> songDisplayList = new ArrayList<>();
    private ArrayAdapter<String> songAdapter;

    public SongsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);

        songTitleEditText = view.findViewById(R.id.songTitleEditText);
        artistSpinner = view.findViewById(R.id.artistSpinner);
        genreSpinner = view.findViewById(R.id.genreSpinner);
        filterArtistSpinner = view.findViewById(R.id.filterArtistSpinner);
        filterGenreSpinner = view.findViewById(R.id.filterGenreSpinner);
        addSongButton = view.findViewById(R.id.addSongButton);
        searchButton = view.findViewById(R.id.searchButton);
        resetSearchButton = view.findViewById(R.id.resetSearchButton);
        songsListView = view.findViewById(R.id.songsListView);

        EditText searchQueryEditText = view.findViewById(R.id.searchQueryEditText);
        Button searchQueryButton = view.findViewById(R.id.searchQueryButton);

        if (getActivity() != null) {
            dbHelper = ((MainActivity) getActivity()).getDbHelper();
        }

        songAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, songDisplayList);
        songsListView.setAdapter(songAdapter);

        loadSpinnersData();
        loadSongsFromDatabase();

        addSongButton.setOnClickListener(v -> {
            String title = songTitleEditText.getText().toString().trim();
            int artistPos = artistSpinner.getSelectedItemPosition();
            int genrePos = genreSpinner.getSelectedItemPosition();

            if (title.isEmpty()) {
                Toast.makeText(getActivity(), "Unesite naziv pesme!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (artistList.isEmpty() || genreList.isEmpty()) {
                Toast.makeText(getActivity(), "Morate imati bar jednog izvođača i žanr!", Toast.LENGTH_SHORT).show();
                return;
            }

            Artist selectedArtist = artistList.get(artistPos);
            Genre selectedGenre = genreList.get(genrePos);

            if (dbHelper.existsSong(title, selectedArtist.getId())) {
                Toast.makeText(getActivity(), "Ova pesma za izabranog izvođača već postoji!", Toast.LENGTH_SHORT).show();
                return;
            }

            Song newSong = new Song(title, selectedArtist, selectedGenre);
            dbHelper.createSong(newSong);

            songTitleEditText.setText("");
            Toast.makeText(getActivity(), "Pesma uspešno dodata!", Toast.LENGTH_SHORT).show();
            loadSongsFromDatabase();
        });

        if (searchQueryButton != null && searchQueryEditText != null) {
            searchQueryButton.setOnClickListener(v -> {
                String query = searchQueryEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    songList = dbHelper.searchSongsByText(query);
                    updateSongListView(songList);
                } else {
                    loadSongsFromDatabase();
                }
            });
        }

        searchButton.setOnClickListener(v -> {
            long artistId = 0;
            long genreId = 0;

            int artistPos = filterArtistSpinner.getSelectedItemPosition();
            int genrePos = filterGenreSpinner.getSelectedItemPosition();

            if (artistPos > 0 && artistPos - 1 < artistList.size()) {
                artistId = artistList.get(artistPos - 1).getId();
            }

            if (genrePos > 0 && genrePos - 1 < genreList.size()) {
                genreId = genreList.get(genrePos - 1).getId();
            }

            songList = dbHelper.searchSongs(artistId, genreId);
            updateSongListView(songList);
        });

        resetSearchButton.setOnClickListener(v -> {
            filterArtistSpinner.setSelection(0);
            filterGenreSpinner.setSelection(0);
            if (searchQueryEditText != null) searchQueryEditText.setText("");
            loadSongsFromDatabase();
        });

        songsListView.setOnItemClickListener((parent, view1, position, id) -> {
            Song selectedSong = songList.get(position);
            showSongOptionsDialog(selectedSong);
        });

        return view;
    }

    private void loadSpinnersData() {
        artistList = dbHelper.getAllArtists();
        genreList = dbHelper.getAllGenres();

        artistNames.clear();
        filterArtistNames.clear();
        filterArtistNames.add("Svi izvođači");
        for (Artist a : artistList) {
            artistNames.add(a.getName());
            filterArtistNames.add(a.getName());
        }

        genreNames.clear();
        filterGenreNames.clear();
        filterGenreNames.add("Svi žanrovi");
        for (Genre g : genreList) {
            genreNames.add(g.getName());
            filterGenreNames.add(g.getName());
        }

        ArrayAdapter<String> artistAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, artistNames);
        ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, genreNames);

        ArrayAdapter<String> filterArtistAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterArtistNames);
        ArrayAdapter<String> filterGenreAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterGenreNames);

        artistSpinner.setAdapter(artistAdapter);
        genreSpinner.setAdapter(genreAdapter);

        filterArtistSpinner.setAdapter(filterArtistAdapter);
        filterGenreSpinner.setAdapter(filterGenreAdapter);
    }

    private void loadSongsFromDatabase() {
        songList = dbHelper.getAllSongs();
        updateSongListView(songList);
    }

    private void updateSongListView(ArrayList<Song> songs) {
        songDisplayList.clear();
        for (Song s : songs) {
            String artistName = (s.getArtist() != null) ? s.getArtist().getName() : "Nepoznat";
            String genreName = (s.getGenre() != null) ? s.getGenre().getName() : "Nepoznat";
            songDisplayList.add(s.getTitle() + " - " + artistName + " [" + genreName + "]");
        }
        songAdapter.notifyDataSetChanged();
    }

    private void showSongOptionsDialog(Song song) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Pesma: " + song.getTitle());
        builder.setMessage("Izaberite akciju:");

        builder.setPositiveButton("Izmeni", (dialog, which) -> showEditSongDialog(song));

        builder.setNegativeButton("Obriši", (dialog, which) -> {
            dbHelper.deleteSong(song.getId());
            Toast.makeText(getActivity(), "Pesma obrisana!", Toast.LENGTH_SHORT).show();
            loadSongsFromDatabase();
        });

        builder.setNeutralButton("Otkaži", null);
        builder.show();
    }

    private void showEditSongDialog(Song song) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Izmena naziva pesme");

        final EditText input = new EditText(requireContext());
        input.setText(song.getTitle());
        input.setPadding(32, 16, 32, 16);
        builder.setView(input);

        builder.setPositiveButton("Sačuvaj", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                song.setTitle(newTitle);
                if (dbHelper != null) {
                    dbHelper.updateSong(song);
                    Toast.makeText(getActivity(), "Pesma uspešno izmenjena!", Toast.LENGTH_SHORT).show();
                    loadSongsFromDatabase();
                }
            } else {
                Toast.makeText(getActivity(), "Naziv pesme ne može biti prazan!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }
}

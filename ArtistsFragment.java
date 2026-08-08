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

public class ArtistsFragment extends Fragment {

    private EditText artistNameEditText;
    private Spinner genreSpinner, filterGenreSpinner;
    private Button addArtistButton, filterArtistButton, resetArtistFilterButton;
    private ListView artistsListView;

    private DatabaseHelper dbHelper;

    private ArrayList<Genre> genreList = new ArrayList<>();
    private ArrayList<Artist> artistList = new ArrayList<>();
    private ArrayList<String> artistDisplayList = new ArrayList<>();

    private ArrayAdapter<String> artistAdapter;
    private ArrayAdapter<String> genreSpinnerAdapter;
    private ArrayAdapter<String> filterGenreSpinnerAdapter;

    private ArrayList<String> genreNamesList = new ArrayList<>();
    private ArrayList<String> filterGenreNamesList = new ArrayList<>();

    public ArtistsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_artists, container, false);

        artistNameEditText = view.findViewById(R.id.artistNameEditText);
        genreSpinner = view.findViewById(R.id.genreSpinner);
        filterGenreSpinner = view.findViewById(R.id.filterGenreSpinner);
        addArtistButton = view.findViewById(R.id.addArtistButton);
        filterArtistButton = view.findViewById(R.id.filterArtistButton);
        resetArtistFilterButton = view.findViewById(R.id.resetArtistFilterButton);
        artistsListView = view.findViewById(R.id.artistsListView);

        EditText searchQueryEditText = view.findViewById(R.id.searchQueryEditText);
        Button searchQueryButton = view.findViewById(R.id.searchQueryButton);

        if (getActivity() != null) {
            dbHelper = ((MainActivity) getActivity()).getDbHelper();
        }

        artistAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, artistDisplayList);
        artistsListView.setAdapter(artistAdapter);

        genreSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, genreNamesList);
        genreSpinner.setAdapter(genreSpinnerAdapter);

        filterGenreSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, filterGenreNamesList);
        filterGenreSpinner.setAdapter(filterGenreSpinnerAdapter);

        loadGenresToSpinner();
        loadArtistsFromDatabase();

        addArtistButton.setOnClickListener(v -> {
            String name = artistNameEditText.getText().toString().trim();
            int selectedGenrePosition = genreSpinner.getSelectedItemPosition();

            if (name.isEmpty()) {
                Toast.makeText(getActivity(), "Unesite ime izvođača!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (genreList.isEmpty() || selectedGenrePosition < 0) {
                Toast.makeText(getActivity(), "Prvo morate dodati makar jedan žanr!", Toast.LENGTH_SHORT).show();
                return;
            }

            Genre selectedGenre = genreList.get(selectedGenrePosition);

            if (dbHelper.existsArtist(name, selectedGenre.getId())) {
                Toast.makeText(getActivity(), "Ovaj izvođač već postoji u izabranom žanru!", Toast.LENGTH_SHORT).show();
                return;
            }

            Artist newArtist = new Artist(name, selectedGenre);
            dbHelper.createArtist(newArtist);
            artistNameEditText.setText("");
            Toast.makeText(getActivity(), "Izvođač uspešno dodat!", Toast.LENGTH_SHORT).show();
            loadArtistsFromDatabase();
        });

        if (searchQueryButton != null && searchQueryEditText != null) {
            searchQueryButton.setOnClickListener(v -> {
                String query = searchQueryEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    artistList = dbHelper.searchArtistsByText(query);
                    updateArtistListView(artistList);
                } else {
                    loadArtistsFromDatabase();
                }
            });
        }

        filterArtistButton.setOnClickListener(v -> {
            int selectedPos = filterGenreSpinner.getSelectedItemPosition();
            if (selectedPos > 0 && selectedPos - 1 < genreList.size()) {
                long selectedGenreId = genreList.get(selectedPos - 1).getId();
                artistList = dbHelper.getArtistsByGenre(selectedGenreId);
            } else {
                artistList = dbHelper.getAllArtists();
            }
            updateArtistListView(artistList);
        });

        resetArtistFilterButton.setOnClickListener(v -> {
            filterGenreSpinner.setSelection(0);
            if (searchQueryEditText != null) searchQueryEditText.setText("");
            loadArtistsFromDatabase();
        });

        artistsListView.setOnItemClickListener((parent, view1, position, id) -> {
            Artist selectedArtist = artistList.get(position);
            showArtistOptionsDialog(selectedArtist);
        });

        return view;
    }

    private void loadGenresToSpinner() {
        genreList = dbHelper.getAllGenres();

        genreNamesList.clear();
        filterGenreNamesList.clear();

        filterGenreNamesList.add("Svi žanrovi");

        for (Genre g : genreList) {
            genreNamesList.add(g.getName());
            filterGenreNamesList.add(g.getName());
        }

        genreSpinnerAdapter.notifyDataSetChanged();
        filterGenreSpinnerAdapter.notifyDataSetChanged();
    }

    private void loadArtistsFromDatabase() {
        artistList = dbHelper.getAllArtists();
        updateArtistListView(artistList);
    }

    private void updateArtistListView(ArrayList<Artist> artists) {
        artistDisplayList.clear();
        for (Artist a : artists) {
            String genreName = (a.getGenre() != null) ? a.getGenre().getName() : "Bez žanra";
            artistDisplayList.add(a.getName() + " (" + genreName + ")");
        }
        artistAdapter.notifyDataSetChanged();
    }

    private void showArtistOptionsDialog(Artist artist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Izvođač: " + artist.getName());
        builder.setMessage("Izaberite akciju:");

        builder.setPositiveButton("Izmeni", (dialog, which) -> showEditArtistDialog(artist));

        builder.setNegativeButton("Obriši", (dialog, which) -> {
            dbHelper.deleteArtist(artist.getId());
            Toast.makeText(getActivity(), "Izvođač obrisan!", Toast.LENGTH_SHORT).show();
            loadArtistsFromDatabase();
        });

        builder.setNeutralButton("Otkaži", null);
        builder.show();
    }

    private void showEditArtistDialog(Artist artist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Izmena izvođača");

        final EditText input = new EditText(requireContext());
        input.setText(artist.getName());
        input.setPadding(32, 16, 32, 16);
        builder.setView(input);

        builder.setPositiveButton("Sačuvaj", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                artist.setName(newName);
                dbHelper.updateArtist(artist);
                Toast.makeText(getActivity(), "Izmene sačuvane!", Toast.LENGTH_SHORT).show();
                loadArtistsFromDatabase();
            } else {
                Toast.makeText(getActivity(), "Ime ne može biti prazno!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }
}

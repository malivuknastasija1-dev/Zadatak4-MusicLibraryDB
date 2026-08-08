package com.example.musiclibrarydb;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import com.example.musiclibrarydb.sqlite.helper.DatabaseHelper;
import com.example.musiclibrarydb.sqlite.model.Genre;

public class GenreFragment extends Fragment {

    private EditText genreNameEditText;
    private Button addGenreButton;
    private ListView genresListView;

    private DatabaseHelper dbHelper;
    private ArrayList<Genre> genreList = new ArrayList<>();
    private ArrayList<String> genreNamesList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    public GenreFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_genres, container, false);

        genreNameEditText = view.findViewById(R.id.genreNameEditText);
        addGenreButton = view.findViewById(R.id.addGenreButton);
        genresListView = view.findViewById(R.id.genresListView);

        // Elementi za Wildcard pretragu
        EditText searchQueryEditText = view.findViewById(R.id.searchQueryEditText);
        Button searchQueryButton = view.findViewById(R.id.searchQueryButton);

        if (getActivity() != null) {
            dbHelper = ((MainActivity) getActivity()).getDbHelper();
        }

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, genreNamesList);
        genresListView.setAdapter(adapter);

        loadGenresFromDatabase();

        // 1. Dodavanje novog žanra
        addGenreButton.setOnClickListener(v -> {
            String name = genreNameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getActivity(), "Unesite naziv žanra!", Toast.LENGTH_SHORT).show();
                return;
            }

            // PROVERA DUPLIKATA
            if (dbHelper.existsGenre(name)) {
                Toast.makeText(getActivity(), "Žanr sa ovim nazivom već postoji!", Toast.LENGTH_SHORT).show();
                return;
            }

            Genre newGenre = new Genre(name);
            dbHelper.createGenre(newGenre);
            genreNameEditText.setText("");
            Toast.makeText(getActivity(), "Žanr uspešno dodat!", Toast.LENGTH_SHORT).show();
            loadGenresFromDatabase();
        });

        // 2. WILDCARD PRETRAGA ŽANROVA PO TEKSTU
        if (searchQueryButton != null && searchQueryEditText != null) {
            searchQueryButton.setOnClickListener(v -> {
                String query = searchQueryEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    genreList = dbHelper.searchGenresByText(query);
                    updateGenreListView(genreList);
                } else {
                    loadGenresFromDatabase();
                }
            });
        }

        // 3. Klik na žanr u listi -> Dijalog za izmenu/brisanje
        genresListView.setOnItemClickListener((parent, view1, position, id) -> {
            Genre selectedGenre = genreList.get(position);
            showGenreOptionsDialog(selectedGenre);
        });

        return view;
    }

    private void loadGenresFromDatabase() {
        genreList = dbHelper.getAllGenres();
        updateGenreListView(genreList);
    }

    private void updateGenreListView(ArrayList<Genre> genres) {
        genreNamesList.clear();
        for (Genre g : genres) {
            genreNamesList.add(g.getName());
        }
        adapter.notifyDataSetChanged();
    }

    private void showGenreOptionsDialog(Genre genre) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Žanr: " + genre.getName());
        builder.setMessage("Izaberite akciju koju želite da izvršite:");

        builder.setPositiveButton("Izmeni", (dialog, which) -> showEditGenreDialog(genre));

        builder.setNegativeButton("Obriši", (dialog, which) -> {
            dbHelper.deleteGenre(genre.getId());
            Toast.makeText(getActivity(), "Žanr obrisan!", Toast.LENGTH_SHORT).show();
            loadGenresFromDatabase();
        });

        builder.setNeutralButton("Otkaži", null);
        builder.show();
    }

    private void showEditGenreDialog(Genre genre) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Izmena žanra");

        final EditText input = new EditText(requireContext());
        input.setText(genre.getName());
        input.setPadding(32, 16, 32, 16);
        builder.setView(input);

        builder.setPositiveButton("Sačuvaj", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                genre.setName(newName);
                dbHelper.updateGenre(genre);
                Toast.makeText(getActivity(), "Izmene sačuvane!", Toast.LENGTH_SHORT).show();
                loadGenresFromDatabase();
            } else {
                Toast.makeText(getActivity(), "Naziv žanra ne može biti prazan!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }
}
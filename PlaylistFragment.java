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
import com.example.musiclibrarydb.sqlite.model.Playlist;
import com.example.musiclibrarydb.sqlite.model.Song;
import com.example.musiclibrarydb.sqlite.model.User;

public class PlaylistFragment extends Fragment {

    private EditText playlistTitleEditText;
    private Button createPlaylistButton, addSongToPlaylistButton;
    private Spinner playlistsSpinner, songsSpinner;
    private ListView playlistsListView;

    private DatabaseHelper dbHelper;
    private User currentUser;

    private ArrayList<Playlist> userPlaylists = new ArrayList<>();
    private ArrayList<Song> allSongs = new ArrayList<>();

    private ArrayList<String> playlistNames = new ArrayList<>();
    private ArrayList<String> songNames = new ArrayList<>();
    private ArrayList<String> playlistDisplayList = new ArrayList<>();

    private ArrayAdapter<String> playlistListAdapter;
    private ArrayAdapter<String> playlistsSpinnerAdapter;
    private ArrayAdapter<String> songsSpinnerAdapter;

    public PlaylistFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);

        playlistTitleEditText = view.findViewById(R.id.playlistTitleEditText);
        createPlaylistButton = view.findViewById(R.id.createPlaylistButton);
        addSongToPlaylistButton = view.findViewById(R.id.addSongToPlaylistButton);
        playlistsSpinner = view.findViewById(R.id.playlistsSpinner);
        songsSpinner = view.findViewById(R.id.songsSpinner);
        playlistsListView = view.findViewById(R.id.playlistsListView);

        EditText searchQueryEditText = view.findViewById(R.id.searchQueryEditText);
        Button searchQueryButton = view.findViewById(R.id.searchQueryButton);

        if (getActivity() != null) {
            MainActivity mainActivity = (MainActivity) getActivity();
            dbHelper = mainActivity.getDbHelper();
            currentUser = mainActivity.getCurrentUser();
        }

        playlistListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, playlistDisplayList);
        playlistsListView.setAdapter(playlistListAdapter);

        playlistsSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, playlistNames);
        playlistsSpinner.setAdapter(playlistsSpinnerAdapter);

        songsSpinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, songNames);
        songsSpinner.setAdapter(songsSpinnerAdapter);

        loadData();

        createPlaylistButton.setOnClickListener(v -> {
            String title = playlistTitleEditText.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(getActivity(), "Unesite naziv plejliste!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUser == null) {
                Toast.makeText(getActivity(), "Greška: Korisnik nije ulogovan!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.existsPlaylist(title, currentUser.getId())) {
                Toast.makeText(getActivity(), "Već imate plejlistu sa ovim nazivom!", Toast.LENGTH_SHORT).show();
                return;
            }

            Playlist newPlaylist = new Playlist(title, currentUser);
            dbHelper.createPlaylist(newPlaylist);

            playlistTitleEditText.setText("");
            Toast.makeText(getActivity(), "Plejlista uspešno kreirana!", Toast.LENGTH_SHORT).show();
            loadData();
        });

        addSongToPlaylistButton.setOnClickListener(v -> {
            if (userPlaylists.isEmpty() || allSongs.isEmpty()) {
                Toast.makeText(getActivity(), "Morate imati bar jednu plejlistu i jednu pesmu!", Toast.LENGTH_SHORT).show();
                return;
            }

            Playlist selectedPlaylist = userPlaylists.get(playlistsSpinner.getSelectedItemPosition());
            Song selectedSong = allSongs.get(songsSpinner.getSelectedItemPosition());

            dbHelper.addSongToPlaylist(selectedPlaylist.getId(), selectedSong.getId());
            Toast.makeText(getActivity(), "Pesma dodata na plejlistu!", Toast.LENGTH_SHORT).show();
            loadData();
        });

        playlistsListView.setOnItemClickListener((parent, view1, position, id) -> {
            Playlist selectedPlaylist = userPlaylists.get(position);
            showPlaylistMeniDialog(selectedPlaylist);
        });

        searchQueryButton.setOnClickListener(v -> {
            String query = searchQueryEditText.getText().toString().trim();
            if (!query.isEmpty() && currentUser != null) {
                userPlaylists = dbHelper.searchPlaylistsByText(query, currentUser.getId());
                updatePlaylistListView(userPlaylists);
            } else {
                loadData();
            }
        });

        return view;
    }

    private void loadData() {
        if (currentUser == null) return;

        userPlaylists = dbHelper.getUserPlaylists(currentUser);
        allSongs = dbHelper.getAllSongs();

        playlistNames.clear();
        for (Playlist p : userPlaylists) {
            playlistNames.add(p.getTitle());
        }

        songNames.clear();
        for (Song s : allSongs) {
            songNames.add(s.getTitle());
        }

        updatePlaylistListView(userPlaylists);
        playlistsSpinnerAdapter.notifyDataSetChanged();
        songsSpinnerAdapter.notifyDataSetChanged();
    }

    private void updatePlaylistListView(ArrayList<Playlist> playlists) {
        playlistDisplayList.clear();
        for (Playlist p : playlists) {
            int numSongs = (p.getSongs() != null) ? p.getSongs().size() : 0;
            playlistDisplayList.add(p.getTitle() + " (" + numSongs + " pesama)");
        }
        playlistListAdapter.notifyDataSetChanged();
    }

    private void showPlaylistMeniDialog(Playlist playlist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Plejlista: " + playlist.getTitle());
        builder.setMessage("Izaberite akciju koju želite da izvršite:");

        builder.setPositiveButton("Izmeni naziv", (dialog, which) -> showEditPlaylistTitleDialog(playlist));

        builder.setNeutralButton("Pesme na plejlisti", (dialog, which) -> showRemoveSongsDialog(playlist));

        builder.setNegativeButton("Obriši", (dialog, which) -> {
            dbHelper.deletePlaylist(playlist.getId());
            Toast.makeText(getActivity(), "Plejlista obrisana!", Toast.LENGTH_SHORT).show();
            loadData();
        });

        builder.show();
    }

    private void showEditPlaylistTitleDialog(Playlist playlist) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Izmena naziva plejliste");

        final EditText input = new EditText(requireContext());
        input.setText(playlist.getTitle());
        input.setPadding(32, 16, 32, 16);
        builder.setView(input);

        builder.setPositiveButton("Sačuvaj", (dialog, which) -> {
            String newTitle = input.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                playlist.setTitle(newTitle);
                dbHelper.updatePlaylist(playlist);
                Toast.makeText(getActivity(), "Naziv uspešno promenjen!", Toast.LENGTH_SHORT).show();
                loadData();
            } else {
                Toast.makeText(getActivity(), "Naziv ne može biti prazan!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Otkaži", null);
        builder.show();
    }

    private void showRemoveSongsDialog(Playlist playlist) {
        ArrayList<Song> songsInPlaylist = playlist.getSongs();

        if (songsInPlaylist == null || songsInPlaylist.isEmpty()) {
            Toast.makeText(getActivity(), "Ova plejlista trenutno nema dodatih pesama.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] songTitles = new String[songsInPlaylist.size()];
        for (int i = 0; i < songsInPlaylist.size(); i++) {
            songTitles[i] = songsInPlaylist.get(i).getTitle();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Kliknite na pesmu da je uklonite:");

        builder.setItems(songTitles, (dialog, which) -> {
            Song songToRemove = songsInPlaylist.get(which);
            dbHelper.removeSongFromPlaylist(playlist.getId(), songToRemove.getId());
            Toast.makeText(getActivity(), "Pesma \"" + songToRemove.getTitle() + "\" uklonjena sa plejliste!", Toast.LENGTH_SHORT).show();
            loadData();
        });

        builder.setNegativeButton("Zatvori", null);
        builder.show();
    }
}

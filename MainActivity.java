package com.example.musiclibrarydb;

import android.os.Bundle;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.musiclibrarydb.sqlite.helper.DatabaseHelper;
import com.example.musiclibrarydb.sqlite.model.User;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private User currentUser = null;

    private LinearLayout userHeaderLayout;
    private LinearLayout navigationLayout;
    private TextView loggedInUserTextView;
    private Button logoutButton;

    private Button navGenresButton;
    private Button navArtistsButton;
    private Button navSongsButton;
    private Button navPlaylistsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        userHeaderLayout = findViewById(R.id.userHeaderLayout);
        navigationLayout = findViewById(R.id.navigationLayout);
        loggedInUserTextView = findViewById(R.id.loggedInUserTextView);
        logoutButton = findViewById(R.id.logoutButton);

        navGenresButton = findViewById(R.id.navGenresButton);
        navArtistsButton = findViewById(R.id.navArtistsButton);
        navSongsButton = findViewById(R.id.navSongsButton);
        navPlaylistsButton = findViewById(R.id.navPlaylistsButton);

        navGenresButton.setOnClickListener(v -> loadFragment(new GenreFragment()));
        navArtistsButton.setOnClickListener(v -> loadFragment(new ArtistsFragment()));
        navSongsButton.setOnClickListener(v -> loadFragment(new SongsFragment()));
        navPlaylistsButton.setOnClickListener(v -> loadFragment(new PlaylistsFragment()));
        logoutButton.setOnClickListener(v -> onUserLoggedOut());

        loadFragment(new LoginFragment());
    }

    public void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    public void onUserLoggedIn(User user) {
        this.currentUser = user;
        loggedInUserTextView.setText("Korisnik: " + user.getUsername());

        userHeaderLayout.setVisibility(View.VISIBLE);
        navigationLayout.setVisibility(View.VISIBLE);

        loadFragment(new GenreFragment());
        Toast.makeText(this, "Dobrodošli, " + user.getUsername() + "!", Toast.LENGTH_SHORT).show();
    }

    public void onUserLoggedOut() {
        this.currentUser = null;

        userHeaderLayout.setVisibility(View.GONE);
        navigationLayout.setVisibility(View.GONE);

        loadFragment(new LoginFragment());
        Toast.makeText(this, "Uspešno ste se odjavili.", Toast.LENGTH_SHORT).show();
    }

    public DatabaseHelper getDbHelper() {
        return dbHelper;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.closeDB();
        }
    }
}













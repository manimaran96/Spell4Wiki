package com.manimaran.wikiaudio;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.manimaran.wikiaudio.model.Words;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] list = getResources().getStringArray(R.array.words);

        List<Words> wordsList = new ArrayList<>();
        for(String s : list)
            wordsList.add(new Words(s, "",false));

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        WordsAdapter mAdapter = new WordsAdapter(wordsList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_refresh:
                Toast.makeText(this, "Words selected", Toast.LENGTH_SHORT).show();
                break;
            // action with ID action_logout was selected
            case R.id.action_logout:
                Toast.makeText(this, "Logout selected", Toast.LENGTH_SHORT).show();
                logout();
                break;
            // action with ID action_settings was selected
            case R.id.action_settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }

        return true;
    }

    private void logout() {
        //  Write to shared preferences
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(
                getString(R.string.pref_file_key),
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.pref_is_logged_in), false);
        editor.apply();

        ServiceGenerator.clearCookies();

        Intent intent = new Intent(getApplicationContext(),
                LoginActivity.class);
        startActivity(intent);
        finish();
    }

}

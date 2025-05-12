package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;

import com.example.myapplication.ui.GameActivity.GameMainActivity;
import com.example.myapplication.ui.MainScreen.MainScreenActivity;
import com.example.myapplication.ui.ProgressActivity.ProgressActivity;
import com.example.myapplication.ui.ProgressActivity.ProgressActivityFragment;
import com.example.myapplication.ui.ProgressActivity.ProgressViewModel;
import com.example.myapplication.ui.StartScreen.StartScreenActivity;
import com.example.myapplication.ui.StartScreen.StartScreenModel;
import com.example.myapplication.ui.WelcomeActivity.WelcomeActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //binding = ActivityMainBinding.inflate(getLayoutInflater());
        //setContentView(binding.getRoot());
/*
        // Initialize the StartScreenModel
        StartScreenModel startScreenModel = new StartScreenModel();
        //ProgressViewModel progressViewModel = new ProgressViewModel();
        // Create an instance of the StartScreenFragment
        StartScreenFragment startScreenFragment = new StartScreenFragment();
        //ProgressActivityFragment progressActivityFragment = new ProgressActivityFragment();
        // Add the fragment to the activity's layout
       // Intent intent = new Intent(this, ProgressActivity.class);
       // startActivity(intent);

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(binding.fragmentContainer.getId(), startScreenFragment);
            fragmentTransaction.commit();
        }

    }


 */
        Intent intent = new Intent(MainActivity.this, GameMainActivity.class);
        startActivity(intent);
    }
}
package com.luciantig.parkingapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.luciantig.parkingapp.R
import com.luciantig.parkingapp.repositories.FirebaseRepository
import com.luciantig.parkingapp.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var viewModel : MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val firebaseRepository = FirebaseRepository()
        val viewModelProviderFactory = MainViewModelProviderFactory(application, firebaseRepository)

        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(MainViewModel::class.java)

        /*appBarLayout.setExpanded(false, false);
        appBarLayout.visibility = View.GONE;*/
        setSupportActionBar(toolbar)
        bottomNavigationView.setupWithNavController(navHostFragment.findNavController())
        bottomNavigationView.setOnNavigationItemReselectedListener {  }

        navHostFragment.findNavController()
            .addOnDestinationChangedListener{ _, destination, _ ->
                when(destination.id){
                    R.id.mapFragment, R.id.accountFragment, R.id.messageFragment -> {
                        bottomNavigationView.visibility = View.VISIBLE
                        appBarLayout.setExpanded(false, false)
                        appBarLayout.visibility = View.GONE
                    }
                    else -> bottomNavigationView.visibility = View.GONE
                }
            }
    }
}
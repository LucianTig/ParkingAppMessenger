package com.luciantig.parkingapp.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.luciantig.parkingapp.repositories.FirebaseRepository
import com.luciantig.parkingapp.ui.viewmodels.MainViewModel

class MainViewModelProviderFactory(
    val app: Application,
    val  firebaseRepository: FirebaseRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(app, firebaseRepository) as T
    }
}
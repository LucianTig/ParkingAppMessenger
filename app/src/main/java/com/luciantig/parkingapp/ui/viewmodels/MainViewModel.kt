package com.luciantig.parkingapp.ui.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.google.android.gms.maps.model.LatLng
import com.luciantig.parkingapp.models.ChatMessage
import com.luciantig.parkingapp.models.User
import com.luciantig.parkingapp.others.Resource
import com.luciantig.parkingapp.repositories.FirebaseRepository
import kotlinx.coroutines.launch

class MainViewModel(
    app: Application,
    val firebaseRepository: FirebaseRepository
) :AndroidViewModel(app) {

    var loginResponse: LiveData<Resource<User>>
    var registerResponse: LiveData<Resource<User>>
    var otherUserIdLiveData : MutableLiveData<String>
    var userInfoLiveData : MutableLiveData<User>
    var userListLiveData : MutableLiveData<MutableList<User>>
    var userChatMessageListLiveData : MutableLiveData<MutableList<ChatMessage>>
    var carSavedPostionLiveData : MutableLiveData<LatLng>


    init {
        loginResponse = firebaseRepository.provideLiveDataLogin()
        registerResponse = firebaseRepository.provideLiveDataRegister()
        otherUserIdLiveData = firebaseRepository.provideLiveDataOtherUserId()
        userInfoLiveData = firebaseRepository.provideUserInfoLiveData()
        userListLiveData = firebaseRepository.provideUserListLiveData()
        userChatMessageListLiveData = firebaseRepository.provideChatMessageListListLiveData()
        carSavedPostionLiveData = firebaseRepository.provideCarSavedPostionLiveData()

    }

    fun userIsLogged() = firebaseRepository.userIsLogged()

    fun userLogout() = firebaseRepository.userLogout()

    fun login(email: String, password: String) = viewModelScope.launch {
        loginResponse = firebaseRepository.login(email, password)
    }

    fun register(email: String, password: String, username: String, carId: String, token: String) = viewModelScope.launch {
        registerResponse = firebaseRepository.register(email, password, username, carId, token)
    }

    fun addUserCarLocation(latLng: LatLng, country: String, city: String, street: String) = viewModelScope.launch {
        firebaseRepository.addUserCarLocation(latLng, country, city, street)
    }
}
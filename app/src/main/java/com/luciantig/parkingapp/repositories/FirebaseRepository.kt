package com.luciantig.parkingapp.repositories

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.luciantig.parkingapp.models.ChatMessage
import com.luciantig.parkingapp.models.User
import com.luciantig.parkingapp.others.Constants.LOGIN_SUCCESS
import com.luciantig.parkingapp.others.Constants.REGISTER_EMPTY_TEXT_BOX
import com.luciantig.parkingapp.others.Constants.REGISTER_SUCCESS
import com.luciantig.parkingapp.others.Resource
import com.luciantig.parkingapp.services.FirebaseService
import android.content.SharedPreferences


class FirebaseRepository() : LiveData<FirebaseUser?>(){

    private val auth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore
    private val TAG = "FirebaseRepository"
    private var authenticatedUserMutableLiveDataLogin : MutableLiveData<Resource<User>> = MutableLiveData()
    private var authenticatedUserMutableLiveDataRegister : MutableLiveData<Resource<User>> = MutableLiveData()
    private var otherUserIdLiveData : MutableLiveData<String> = MutableLiveData()
    private var userInfoLiveData : MutableLiveData<User> = MutableLiveData()
    private var userListLiveData : MutableLiveData<MutableList<User>> = MutableLiveData()
    private var chatMessageListLiveData : MutableLiveData<MutableList<ChatMessage>> = MutableLiveData()
    private var carSavedPostionLiveData : MutableLiveData<LatLng> = MutableLiveData()

    private lateinit var uid : String

    private val chatChannelsCollectionRef = firestore.collection("chatChannels")
    private lateinit var currentUserDocRef : DocumentReference


    fun provideLiveDataLogin() = authenticatedUserMutableLiveDataLogin
    fun provideLiveDataRegister() = authenticatedUserMutableLiveDataRegister
    fun provideLiveDataOtherUserId() = otherUserIdLiveData
    fun provideUserInfoLiveData() = userInfoLiveData
    fun provideUserListLiveData() = userListLiveData
    fun provideChatMessageListListLiveData() = chatMessageListLiveData
    fun provideCarSavedPostionLiveData() = carSavedPostionLiveData

    fun userIsLogged() : Boolean{
        if(auth.currentUser != null){
            uid = auth.currentUser!!.uid
            currentUserDocRef = firestore.collection("users").document(uid)
        }
        return auth.currentUser != null
    }

    fun userLogout() : Boolean{
        auth.signOut()
        return true
    }

    fun user(): FirebaseUser? = auth.currentUser

    fun login(email: String, password: String): MutableLiveData<Resource<User>>{
        var firebaseUser: FirebaseUser?
        authenticatedUserMutableLiveDataLogin.postValue(Resource.Loading())
        if (email.isNotEmpty() && password.isNotEmpty()) {

            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                    if(it.isSuccessful) {
                        firebaseUser = auth.currentUser
                        uid = firebaseUser!!.uid
                        val user = User(uid, email, "unknow", "unknow")
                        authenticatedUserMutableLiveDataLogin.postValue(Resource.Success(LOGIN_SUCCESS,user))
                        Log.d(TAG, "UID = $uid")
                    }else{
                        authenticatedUserMutableLiveDataLogin.postValue(Resource.Error(it.exception.toString()))
                    }
                }
        }else{
            authenticatedUserMutableLiveDataLogin.postValue(Resource.Error(REGISTER_EMPTY_TEXT_BOX))
        }
        return authenticatedUserMutableLiveDataLogin
    }


    fun register(email: String, password: String, username: String, carId: String, token: String): MutableLiveData<Resource<User>> {
        var firebaseUser: FirebaseUser?
        authenticatedUserMutableLiveDataRegister.postValue(Resource.Loading())
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(){
                if(it.isSuccessful){
                    firebaseUser = auth.currentUser
                    uid = firebaseUser!!.uid

                    val user = User(uid, username, carId, email, token)
                    firestore.collection("users").document(uid).set(user)
                    authenticatedUserMutableLiveDataRegister.postValue(Resource.Success(REGISTER_SUCCESS,user))
                    Log.d(TAG, "UID = $uid")
                }else{
                    Log.d("Fierbase Repository", it.exception.toString())
                    authenticatedUserMutableLiveDataRegister.postValue(Resource.Error(it.exception.toString()))
                }
            }
        }else{
            authenticatedUserMutableLiveDataRegister.postValue(Resource.Error(REGISTER_EMPTY_TEXT_BOX))
        }
        return authenticatedUserMutableLiveDataRegister
    }

    fun addUserCarLocation(latLng: LatLng, country: String, city: String, street: String){

        if (!this::uid.isInitialized){
            uid = auth.currentUser!!.uid
        }

        val documentReference: DocumentReference =
            firestore.collection("users").document(uid)
        documentReference.update("latitude", latLng.latitude)
        documentReference.update("longitude", latLng.longitude)
        documentReference.update("country", country)
        documentReference.update("street", street)
        documentReference.update("city", city)

    }

    fun userInformationRealtimeDatabase(){
        currentUserDocRef.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Log.d(TAG, it.toString())
                return@addSnapshotListener
            }
            querySnapshot?.let {
                val uid = it.getString("uid")
                val username = it.getString("username")
                val carNumber = it.getString("carNumber")
                val email =  it.getString("email")
                val token = it.getString("token")
                userInfoLiveData.postValue(User(uid, username!!, carNumber!!, email!!, token!!))
            }
        }
    }

    fun findAUser(carId: String){
        firestore.collection("users").whereEqualTo("carNumber", carId).get().addOnCompleteListener {

            if(it.result?.documents?.size != 0) {
                Log.d(TAG, "UID found: " + it.result?.documents?.get(0)?.get("uid"))
                otherUserIdLiveData.postValue(it.result?.documents?.get(0)?.get("uid").toString())
            }else{
                otherUserIdLiveData.postValue("")
            }
        }
    }

    fun findUserCar(){
        currentUserDocRef.get().addOnCompleteListener {
            if(it.result != null){
                if(it.result?.getDouble("latitude") != null && it.result?.getDouble("longitude") != null) {
                    carSavedPostionLiveData.postValue(LatLng(it.result?.getDouble("latitude")!!,it.result?.getDouble("longitude")!!))
                }else{
                    carSavedPostionLiveData.postValue(LatLng(-1.0,-1.0))

                }
            }
        }
    }

    fun getOrCreateChatChannel(otherUserId: String, otherCarId: String, userCarId: String) {

        firestore.collection("users")
            .document(uid).collection("engagedChatChannels").document(otherUserId).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    return@addOnSuccessListener
                }
                /*chatChannelsCollectionRef.document(otherCarId + userCarId).collection("messages").add(
                    ChatMessage("Ce faci tu oare?", Calendar.getInstance().time, "dasd","dsad","dsad"))*/

                var channelid: String = if(otherCarId > userCarId)
                    otherCarId + userCarId
                else
                    userCarId + otherCarId


                currentUserDocRef
                    .collection("engagedChatChannels")
                    .document(otherUserId)
                    .set(mapOf("channelId" to channelid))

                firestore.collection("users").document(otherUserId)
                    .collection("engagedChatChannels")
                    .document(uid)
                    .set(mapOf("channelId" to channelid))
            }
    }

    fun sendChatMessage(message: ChatMessage, channelId: String) {

        Log.d(TAG, "ChannelID for ChatChannels = $channelId")
        chatChannelsCollectionRef
            .document(channelId)
            .collection("messages")
            .add(
                message
            )
    }

    fun addUserListener(){
        currentUserDocRef.collection("engagedChatChannels").addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Log.d(TAG, it.toString())
                return@addSnapshotListener
            }
            val mutableList : MutableList<User> = mutableListOf()
            querySnapshot?.let { it ->
                for (document in it.documents){
                    val userMessageUid = document.id
                    Log.d(TAG, "User engaged to chat UID =$userMessageUid")
                    firestore.collection("users").document(userMessageUid).get().addOnCompleteListener {documentSnapshot ->
                        if(documentSnapshot.result != null){
                            val user = User(userMessageUid, documentSnapshot!!.result?.getString("username")!!, documentSnapshot!!.result?.getString("carNumber")!!, documentSnapshot!!.result?.getString("email")!!, documentSnapshot!!.result?.getString("token")!!)
                            Log.d(TAG, "User engaged to chat found =${user.username}, ${user.email}, ${user.carNumber} ")
                            mutableList.add(user)
                            userListLiveData.postValue(mutableList)
                        }
                    }
                }
            }
        }
    }

    fun addChatMessageListener(channelId: String){

        chatChannelsCollectionRef.document(channelId).collection("messages").orderBy("timestamp").addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Log.d(TAG, it.toString())
                return@addSnapshotListener
            }
            val mutableList : MutableList<ChatMessage> = mutableListOf()

            querySnapshot?.let {
                for(document in it){
                    Log.d(TAG, document.id + document.data["text"])

                    val chatMessage = ChatMessage(document.data["text"].toString(), document.getDate("timestamp")!!, document.data["senderId"].toString(), document.data["recipientId"].toString(), document.data["senderName"].toString())
                    mutableList.add(chatMessage)
                    chatMessageListLiveData.postValue(mutableList)
                }
            }
        }
    }

}
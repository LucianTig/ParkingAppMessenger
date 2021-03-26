package com.luciantig.parkingapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.text.set
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.luciantig.parkingapp.R
import com.luciantig.parkingapp.adapters.MessageAdapter
import com.luciantig.parkingapp.api.RetrofitInstance
import com.luciantig.parkingapp.models.ChatMessage
import com.luciantig.parkingapp.models.NotificationData
import com.luciantig.parkingapp.models.PushNotification
import com.luciantig.parkingapp.models.User
import com.luciantig.parkingapp.ui.MainActivity
import com.luciantig.parkingapp.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_message_content.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*

class MessageChatFragment : Fragment(R.layout.fragment_message_content) {

    private lateinit var viewModel: MainViewModel

    private lateinit var messageAdapter: MessageAdapter

    private var currentUser : User? = null

    val args :  MessageChatFragmentArgs by navArgs()

    val TAG = "MessageChatFragment"

    lateinit var message : String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel

        val otherUser = args.user
        currentUser = args.currentUser
        setupRecyclerView()

        var channelid: String = if(otherUser.uid!! > currentUser?.uid!!)
            otherUser.uid + currentUser?.uid
        else
            currentUser?.uid + otherUser.uid


        viewModel.firebaseRepository.addChatMessageListener(channelid)

        viewModel.userChatMessageListLiveData.observe(viewLifecycleOwner, Observer{
            messageAdapter.submitList(it.toList())

            Log.d(TAG, "item count from observer " + it.size)
            recycler_view_messages.apply {
                recycler_view_messages.smoothScrollToPosition(it.size - 1)
            }

        })

        imageView_send.setOnClickListener {
            message = editText_message.text.toString()
            editText_message.text.clear()
            viewModel.firebaseRepository.sendChatMessage(ChatMessage(message, Calendar.getInstance().time, currentUser?.uid.toString(),
                otherUser.uid, currentUser!!.username), channelid)


            PushNotification(
                NotificationData(currentUser!!.username, message),
                otherUser.token!!
            ).also {
                sendNotification(it)
            }


        }
    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try{
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d(TAG, "Repsonse: ${Gson().toJson(response)}")
            }else{
                Log.e(TAG, response.errorBody().toString())
            }

        }catch (e: Exception){
            Log.e(TAG, e.toString())
        }
    }

    private fun setupRecyclerView() = recycler_view_messages.apply {
        messageAdapter = MessageAdapter(currentUser!!)
        adapter = messageAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }
}
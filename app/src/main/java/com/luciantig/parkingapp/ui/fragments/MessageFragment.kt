package com.luciantig.parkingapp.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.luciantig.parkingapp.R
import com.luciantig.parkingapp.adapters.MessageAdapter
import com.luciantig.parkingapp.adapters.UserAdapter
import com.luciantig.parkingapp.models.ChatMessage
import com.luciantig.parkingapp.models.User
import com.luciantig.parkingapp.ui.MainActivity
import com.luciantig.parkingapp.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.fragment_message.*
import kotlinx.android.synthetic.main.fragment_message_content.*
import java.util.*

class MessageFragment : Fragment(R.layout.fragment_message) {

    private lateinit var viewModel: MainViewModel
    private val TAG = "MessageFragment"
    private var user : User? = null
    lateinit var etCarIdString: String
    lateinit var message: String

    private lateinit var userAdapter: UserAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = (activity as MainActivity).viewModel
        setupRecyclerView()

        

        userAdapter.setOnItemClickListener {
            Log.d(TAG, "from setOnClickListener")
            val bundle = Bundle().apply {
                putSerializable("user", it)
                putSerializable("currentUser", user)
            }
            findNavController().navigate(
                R.id.action_messageFragment_to_messageChatFragment,
                bundle
            )
        }

        viewModel.firebaseRepository.userInformationRealtimeDatabase()
        viewModel.firebaseRepository.addUserListener()

        viewModel.userListLiveData.observe(viewLifecycleOwner, Observer{
            Log.d(TAG, "in userListLiveData observer")
            for(a in it){
                Log.d(TAG, a.username)
            }
            userAdapter.submitList(it.toList())
        })

        viewModel.userInfoLiveData.observe(viewLifecycleOwner, Observer{
            user=it
        })


        buttonCarId.setOnClickListener{
            etCarIdString = etCarId.text.toString()
            message = etMessage.text.toString()
            viewModel.firebaseRepository.findAUser(etCarIdString)
        }


        viewModel.otherUserIdLiveData.observe(viewLifecycleOwner, Observer {
            if(it == ""){
                Toast.makeText(requireContext(), "This user don't have an account on this app.", Toast.LENGTH_SHORT).show()
            }else{
                //Log.d(TAG,  "User = " + user?.username + user?.carNumber)
                val currentUserUid = user!!.uid.toString()
                var channelid: String = if(it > currentUserUid)
                    it + currentUserUid
                else
                    currentUserUid + it

                if(this::etCarIdString.isInitialized && this::message.isInitialized){
                    viewModel.firebaseRepository.getOrCreateChatChannel(it, etCarIdString, user!!.carNumber )
                    viewModel.firebaseRepository.sendChatMessage(ChatMessage(message, Calendar.getInstance().time, user?.uid.toString(), it, user!!.username), channelid)
                }

            }
        })
    }

    private fun setupRecyclerView() = recycler_view_users.apply {
        userAdapter = UserAdapter()
        adapter = userAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

}
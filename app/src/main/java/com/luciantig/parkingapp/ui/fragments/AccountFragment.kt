package com.luciantig.parkingapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.luciantig.parkingapp.R
import com.luciantig.parkingapp.models.User
import com.luciantig.parkingapp.ui.MainActivity
import com.luciantig.parkingapp.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment : Fragment(R.layout.fragment_account) {

    lateinit var viewModel: MainViewModel
    private var user : User? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel =(activity as MainActivity).viewModel

        button_log_out.setOnClickListener{
            viewModel.userLogout()
            findNavController().popBackStack()
        }

        viewModel.firebaseRepository.userInformationRealtimeDatabase()

        viewModel.userInfoLiveData.observe(viewLifecycleOwner, Observer{
            user=it
            etName.setText(it.username)
            tvCar.text = it.carNumber
            tvEmail.text = it.email

        })

    }

}
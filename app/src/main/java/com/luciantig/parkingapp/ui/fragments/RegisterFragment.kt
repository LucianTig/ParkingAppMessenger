package com.luciantig.parkingapp.ui.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.fragment.findNavController
import com.google.firebase.iid.FirebaseInstanceId
import com.luciantig.parkingapp.R
import com.luciantig.parkingapp.models.User
import com.luciantig.parkingapp.others.Resource
import com.luciantig.parkingapp.services.FirebaseService
import com.luciantig.parkingapp.ui.MainActivity
import com.luciantig.parkingapp.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_register.*

class RegisterFragment : Fragment(R.layout.fragment_register) {

    lateinit var viewModel: MainViewModel

    val TAG = "RegisterFragment"

    companion object {
        const val REGISTER_SUCCESSFUL: String = "REGISTER_SUCCESSFUL"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =(activity as MainActivity).viewModel

        viewModel.registerResponse?.observe(viewLifecycleOwner, Observer {response ->
            when(response){
                is Resource.Success -> {
                    hideProgressBar()
                    findNavController().popBackStack()

                }
                is Resource.Error ->{
                    hideProgressBar()
                    Toast.makeText(activity, "A login error occured, pleas try again!", Toast.LENGTH_LONG).show()
                }
                is Resource.Loading ->{
                    showProgressBar()
                }
            }
        })

        var token: String? = null
        FirebaseService.sharedPref = context?.getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
            FirebaseService.token = it.token
            token = it.token
        }

        Log.d(TAG, "Token is : " + token)
        btnRegister.setOnClickListener{
            viewModel.register(etEmailRegister.text.toString(), etPasswordRegister.text.toString(), etPersonName.text.toString(), etCarNumber.text.toString(), token!!)
        }
    }

    private fun hideProgressBar(){
        registerProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar(){
        registerProgressBar.visibility = View.VISIBLE
    }
}
package com.luciantig.parkingapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.luciantig.parkingapp.R
import com.luciantig.parkingapp.others.Resource
import com.luciantig.parkingapp.ui.MainActivity
import com.luciantig.parkingapp.ui.viewmodels.MainViewModel
import kotlinx.android.synthetic.main.fragment_login.*

class LoginFragment : Fragment(R.layout.fragment_login) {

    lateinit var viewModel: MainViewModel

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        viewModel =(activity as MainActivity).viewModel

        navController = findNavController()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            activity?.moveTaskToBack(true);
            activity?.finish();
        }

        viewModel.loginResponse.observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is Resource.Success -> {
                    hideProgressBar()
                    navController.popBackStack()
                }
                is Resource.Error ->{
                    hideProgressBar()
                    Toast.makeText(activity, response.message, Toast.LENGTH_LONG).show()
                }
                is Resource.Loading ->{
                    showProgressBar()
                }
            }
        })

        logInButton.setOnClickListener{
            viewModel.login(etEmail.text.toString(), etPassword.text.toString())
        }

        registerButton.setOnClickListener{
            navController.navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }


    private fun hideProgressBar(){
        loginProgressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar(){
        loginProgressBar.visibility = View.VISIBLE
    }

}
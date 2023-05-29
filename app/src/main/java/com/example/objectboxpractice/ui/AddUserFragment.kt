package com.example.objectboxpractice.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.objectboxpractice.base.BaseFragment
import com.example.objectboxpractice.databinding.FragmentAdduserBinding
import com.example.objectboxpractice.entity.User
import com.example.objectboxpractice.entity.User_
import com.example.objectboxpractice.util.CrudType
import dagger.hilt.android.AndroidEntryPoint
import io.objectbox.Box
import io.objectbox.BoxStore
import javax.inject.Inject

@AndroidEntryPoint
class AddUserFragment : BaseFragment<FragmentAdduserBinding>() {
    private val args: AddUserFragmentArgs by navArgs()
    private lateinit var navController: NavController

    @Inject
    lateinit var boxStore: BoxStore

    override val bindingCallBack: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAdduserBinding
        get() = FragmentAdduserBinding::inflate

    override val bindViews: FragmentAdduserBinding.() -> Unit
        get() = {
            navController = findNavController()
            requireActivity().actionBar?.setDisplayHomeAsUpEnabled(true)
            val userBox = boxStore.boxFor(User::class.java)

            val username = args.username
            binding.tietUserName.setText(username)

            cvAdd.setOnClickListener {
                when (args.crudType) {
                    CrudType.CREATE -> {
                        addUser(userBox)
                        navController.popBackStack()
                    }
                    CrudType.UPDATE -> {
                        updateUser(userBox, username)
                        navController.popBackStack()
                    }
                }
            }
            onBackPressed()
        }

    private fun addUser(userBox: Box<User>) {
        val userName = binding.tietUserName.text.toString()
        val user = User(0, userName)
        userBox.put(user)
    }

    private fun updateUser(userBox: Box<User>, username: String) {
        val userName = binding.tietUserName.text.toString()
        val query = userBox.query(User_.name.equal(username))
        val foundUsers = query.build().find()

        userBox.store.runInTx {
            for (_user in foundUsers) {
                _user.name = userName
                userBox.put(_user)
            }
        }

        query.close()
    }

    private fun onBackPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navController.popBackStack()
                }
            })
    }
}
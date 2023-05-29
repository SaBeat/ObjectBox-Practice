package com.example.objectboxpractice.ui

import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.objectboxpractice.R
import com.example.objectboxpractice.base.BaseFragment
import com.example.objectboxpractice.databinding.FragmentAddUserBinding
import com.example.objectboxpractice.entity.User
import com.example.objectboxpractice.entity.User_
import com.example.objectboxpractice.util.CrudType
import dagger.hilt.android.AndroidEntryPoint
import io.objectbox.Box
import io.objectbox.BoxStore
import javax.inject.Inject

@AndroidEntryPoint
class AddUserFragment : BaseFragment<FragmentAddUserBinding>() {
    private val args: AddUserFragmentArgs by navArgs()
    private lateinit var navController: NavController
    private lateinit var menuHost: MenuHost

    @Inject
    lateinit var boxStore: BoxStore

    override val bindingCallBack: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAddUserBinding
        get() = FragmentAddUserBinding::inflate

    override val bindViews: FragmentAddUserBinding.() -> Unit
        get() = {
            setActionBar()
            navController = findNavController()
            menuHost = requireActivity()

            val userBox = boxStore.boxFor(User::class.java)
            val username = args.username
            binding.tietUserName.setText(username)

            when (args.crudType) {
                CrudType.CREATE -> {
                    txtAdd.text = getString(R.string.add)
                    tilUserName.setHint(R.string.add_user)
                }
                CrudType.UPDATE -> {
                    txtAdd.text = getString(R.string.update)
                    tilUserName.setHint(R.string.update_user)
                }
            }

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
            addUserMenu()
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

    private fun setActionBar(){
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.title = getString(R.string.add_user_fragment)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun addUserMenu(){
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        navController.popBackStack()
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}
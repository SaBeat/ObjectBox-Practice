package com.example.objectboxpractice.ui

import android.util.Log
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.objectboxpractice.R
import com.example.objectboxpractice.base.BaseFragment
import com.example.objectboxpractice.base.RecyclerListAdapter
import com.example.objectboxpractice.databinding.FragmentHomeBinding
import com.example.objectboxpractice.databinding.LayoutUsersItemBinding
import com.example.objectboxpractice.entity.User
import com.example.objectboxpractice.entity.User_
import com.example.objectboxpractice.util.Alerts
import com.example.objectboxpractice.util.CrudType
import dagger.hilt.android.AndroidEntryPoint
import io.objectbox.Box
import io.objectbox.BoxStore
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private lateinit var navController: NavController
    private lateinit var userBox: Box<User>
    private lateinit var menuHost: MenuHost

    companion object {
        private const val TAG = "DEBUG"
    }

    @Inject
    lateinit var boxStore: BoxStore

    override val bindingCallBack: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding
        get() = FragmentHomeBinding::inflate

    override val bindViews: FragmentHomeBinding.() -> Unit
        get() = {
            setActionBar()
            navController = findNavController()
            menuHost = requireActivity()
            userBox = boxStore.boxFor(User::class.java)

            homeMenu()
            getAllUsers()
            searchUser(searchBar)

        }

    private fun getAllUsers() {
        val userList = userBox.all
        if (binding.rvHome.adapter == null)
            binding.rvHome.adapter = usersAdapter
        usersAdapter.submitList(userList)
    }

    private fun searchUser(search: SearchView) {
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                val query = userBox.query(User_.name.contains(newText))
                val foundUsers = query.build().find()
                usersAdapter.submitList(foundUsers)
                query.close()
                return true
            }
        })
    }

    private fun deleteUser(id: Long) {
        val userToDelete = userBox.get(id)
        val query = userBox.query().equal(User_.id, id)

        if (userToDelete != null) {
            userBox.remove(userToDelete)
            getAllUsers()
            Log.i(TAG, "User with ID $id deleted successfully.")
        } else {
            Log.i(TAG, "User with ID $id not found.")
        }
        query.close()
    }

    private fun deleteAllUsers() {
        userBox.removeAll()
        getAllUsers()
    }

    private val usersAdapter by lazy {
        RecyclerListAdapter<LayoutUsersItemBinding, User>(
            onInflate = LayoutUsersItemBinding::inflate,
            onBind = { binding, user, _ ->
                binding.apply {
                    txtId.text = user.id.toString()
                    txtName.text = user.name
                    imgDelete.setOnClickListener {
                        deleteUser(user.id)
                    }
                    root.setOnClickListener {
                        val action = HomeFragmentDirections.actionHomeFragmentToAddUserFragment(
                            CrudType.UPDATE,
                            user
                        )
                        navController.navigate(action)
                    }
                }
            }
        )
    }

    private fun setActionBar() {
        val actionBar = (activity as? AppCompatActivity)?.supportActionBar
        actionBar?.title = getString(R.string.home_fragment)
        actionBar?.setDisplayHomeAsUpEnabled(false)
        actionBar?.setDisplayShowHomeEnabled(false)
    }

    private fun homeMenu() {
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.menu_add -> {
                        val action =
                            HomeFragmentDirections.actionHomeFragmentToAddUserFragment(
                                CrudType.CREATE,
                                User(0,"")
                            )
                        navController.navigate(action)
                        true
                    }
                    R.id.menu_delete -> {
                        Alerts.deleteUsersDialog(
                            requireContext(),
                            getString(R.string.alert),
                            getString(R.string.delete_all_users)
                        ) {
                            deleteAllUsers()
                        }

                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}
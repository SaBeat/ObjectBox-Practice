package com.example.objectboxpractice.ui

import android.util.Log
import android.view.*
import android.widget.SearchView
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
import io.objectbox.BoxStore
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private lateinit var navController: NavController
    companion object {
        private const val TAG = "DEBUG"
    }

    @Inject
    lateinit var boxStore: BoxStore

    override val bindingCallBack: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeBinding
        get() = FragmentHomeBinding::inflate

    override val bindViews: FragmentHomeBinding.() -> Unit
        get() = {
            navController = findNavController()
            setHasOptionsMenu(true)
            getAllUsers()
            searchUser(searchBar)
        }

    private fun getAllUsers() {
        val userBox = boxStore.boxFor(User::class.java)
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
                val userBox = boxStore.boxFor(User::class.java)
                User_.name.name.lowercase()
                val query = userBox.query(User_.name.contains(newText))
                val foundUsers = query.build().find()
                usersAdapter.submitList(foundUsers)
                query.close()
                return true
            }
        })
    }

    private fun deleteUser(id: Long) {
        val userBox = boxStore.boxFor(User::class.java)
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

    private fun deleteAllUsers(){
        val userBox = boxStore.boxFor(User::class.java)
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
                        val action  = HomeFragmentDirections.actionHomeFragmentToAddUserFragment(CrudType.UPDATE,user.name ?: "")
                        navController.navigate(action)
                    }
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add -> {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToAddUserFragment(CrudType.CREATE,"")
                navController.navigate(action)
                true
            }
            R.id.menu_delete -> {
                Alerts.deleteUsersDialog(requireContext(),getString(R.string.alert),getString(R.string.delete_all_users)){
                    deleteAllUsers()
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
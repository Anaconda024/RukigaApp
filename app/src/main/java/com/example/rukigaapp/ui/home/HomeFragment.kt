package com.example.rukigaapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.rukigaapp.R
import com.example.rukigaapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Crucial for Fragment's options menu
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupBottomNavigation()

        return root
    }
    private fun setupBottomNavigation() {
        val bottomNav = binding.bottomNavigation // or find by ID

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom_nav_quiz -> {
                    Log.d("BottomNav", "Navigating to quiz...")
                    try {
                        findNavController().navigate(R.id.action_nav_home_to_nav_quiz)
                    } catch (e: Exception) {
                        Log.e("BottomNav", "Error navigating to quiz", e)
                    }
                    true
                }
                R.id.bottom_nav_learn -> {
                    findNavController().navigate(R.id.action_nav_home_to_nav_library)
                    true
                }
                R.id.bottom_nav_profile -> {
                    // Handle profile navigation
                    true
                }
                else -> false
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
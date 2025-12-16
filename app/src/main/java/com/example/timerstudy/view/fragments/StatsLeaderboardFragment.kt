package com.example.timerstudy.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.timerstudy.databinding.FragmentStatsLeaderboardBinding
import com.google.android.material.tabs.TabLayoutMediator

class StatsLeaderboardFragment : Fragment() {
    private var _binding: FragmentStatsLeaderboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "LeaderBoard"
                1 -> "Statistics"
                else -> ""
            }
        }.attach()

        // Mặc định hiển thị tab Leaderboard (position 0)
        binding.viewPager.currentItem = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> LeaderboardFragment()
                1 -> StatisticsFragment()
                else -> LeaderboardFragment()
            }
        }
    }
}

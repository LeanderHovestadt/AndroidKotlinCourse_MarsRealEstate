package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import com.udacity.asteroidradar.domain.Asteroid

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        val activity = requireActivity()
        ViewModelProvider(
            this,
            MainViewModel.Factory(activity.application)
        ).get(MainViewModel::class.java)
    }

    private var viewModelAdapter: AsteroidsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        viewModelAdapter = AsteroidsAdapter(AsteroidListener { asteroid ->
            this.findNavController().navigate(MainFragmentDirections.actionShowDetail(asteroid))
        })
        binding.asteroidRecycler.adapter = viewModelAdapter

        viewModel.shownAsteroidsFilter.observe(viewLifecycleOwner,  {
            reloadAsteroids()
        })



        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.asteroids.observe(viewLifecycleOwner, {
            reloadAsteroids()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_week_asteroids -> viewModel.updateFilter(ShownAsteroidsFilter.SHOW_WEEK_ASTEROIDS)
            R.id.show_today_asteroids -> viewModel.updateFilter(ShownAsteroidsFilter.SHOW_TODAY_ASTEROIDS)
            else -> viewModel.updateFilter(ShownAsteroidsFilter.SHOW_SAVED_ASTEROIDS)
        }
        return true
    }

    private fun reloadAsteroids() {
        when(viewModel.shownAsteroidsFilter.value) {
            ShownAsteroidsFilter.SHOW_TODAY_ASTEROIDS -> {
                viewModelAdapter?.submitList(viewModel.asteroidsByToday.value)
            }
            ShownAsteroidsFilter.SHOW_WEEK_ASTEROIDS -> {
                viewModelAdapter?.submitList(viewModel.asteroidsUntilEndDate.value)
            }
            ShownAsteroidsFilter.SHOW_SAVED_ASTEROIDS -> {
                viewModelAdapter?.submitList(viewModel.asteroids.value)
            }
        }
    }
}

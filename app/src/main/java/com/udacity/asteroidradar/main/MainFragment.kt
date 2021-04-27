package com.udacity.asteroidradar.main

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import timber.log.Timber

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
        Timber.i("onCreateView called")
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        viewModelAdapter = AsteroidsAdapter(AsteroidListener { asteroid ->
            this.findNavController().navigate(MainFragmentDirections.actionShowDetail(asteroid))
        })
        binding.asteroidRecycler.adapter = viewModelAdapter

        viewModel.asteroids.observe(viewLifecycleOwner, {
            Timber.i("calling reloadAsteroids due to asteroids update")
            if (!viewModel.shownAsteroidsFilter.hasObservers()) {
                viewModel.shownAsteroidsFilter.observe(viewLifecycleOwner, {
                    Timber.i("calling reloadAsteroids due to shownAsteroidsFilter update")
                    reloadAsteroids()
                })
            } else {
                reloadAsteroids()
            }
        })

        viewModel.showSnackbarEvent.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                Timber.i("Showing SnackbarEvent...")
                Snackbar.make(
                    requireActivity().findViewById(R.id.asteroid_recycler),
                    getString(R.string.no_asteroids_could_be_shown),
                    Snackbar.LENGTH_LONG // How long to display the message.
                ).show()
                viewModel.doneShowSnackbarEvent()
            }
        })

        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Timber.i("onViewCreated called")
        super.onViewCreated(view, savedInstanceState)
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
        Timber.i("reloadAsteroids called")
        viewModelAdapter?.submitList(viewModel.getAsteroids())
    }
}

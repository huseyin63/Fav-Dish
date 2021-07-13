package com.example.favdish.view.fragment.allDishes

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.favdish.R
import com.example.favdish.application.App
import com.example.favdish.databinding.FragmentAllDishesBinding
import com.example.favdish.view.activites.AddUpdateDishActivity
import com.example.favdish.view.adapter.FavDishAdapter
import com.example.favdish.viewmodel.FavDishViewModel
import com.example.favdish.viewmodel.FavDishViewModelFactory


class AllDishesFragment : Fragment() {


    private var _binding: FragmentAllDishesBinding? = null
    private val binding get() = _binding!!


    private val favDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as App).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllDishesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerview.layoutManager = GridLayoutManager(context, 2)
        val adapter = FavDishAdapter()
        binding.recyclerview.adapter = adapter

        favDishViewModel.allDishList.observe(viewLifecycleOwner) { dishes ->
            dishes.let {
                adapter.submitList(it)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_add -> {
                startActivity(Intent(requireActivity(), AddUpdateDishActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
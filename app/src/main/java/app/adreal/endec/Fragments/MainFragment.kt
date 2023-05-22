package app.adreal.endec.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import app.adreal.endec.Encryption.Encryption
import app.adreal.endec.R
import app.adreal.endec.ViewModel.MainViewModel
import app.adreal.endec.databinding.FragmentMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainFragment : Fragment() {

    private val binding by lazy {
        FragmentMainBinding.inflate(layoutInflater)
    }

    private val mainViewModel by lazy{
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mainViewModel.generateArgonBasedAesKey()

        binding.settings.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
        }

        return binding.root
    }
}
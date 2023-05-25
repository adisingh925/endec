package app.adreal.endec.Fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import app.adreal.endec.Constants
import app.adreal.endec.File.File
import app.adreal.endec.R
import app.adreal.endec.RecyclerView.MainAdapter
import app.adreal.endec.ViewModel.MainViewModel
import app.adreal.endec.databinding.FragmentMainBinding


class MainFragment : Fragment(), MainAdapter.OnItemClickListener {

    companion object {
        const val PICKFILE_REQUEST_CODE = 10
    }

    private val binding by lazy {
        FragmentMainBinding.inflate(layoutInflater)
    }

    private val mainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val contentResolver by lazy {
        context?.contentResolver
    }

    private val adapter by lazy{
        MainAdapter(requireContext(),this)
    }

    private val recyclerView by lazy{
        binding.recyclerView
    }

    @RequiresApi(VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        initRecyclerView()

        mainViewModel.filesData.observe(viewLifecycleOwner){
            adapter.setData(it)
        }

        binding.settings.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
        }

        binding.encrypt.setOnClickListener {
            openExplorer()
        }

        return binding.root
    }

    private fun initRecyclerView(){
        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(context,Constants.RECYCLERVIEW_SPAN_COUNT)
    }

    private fun openExplorer() {
        val intent = Intent()
            .setType(Constants.PICKER_ID)
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(intent, PICKFILE_REQUEST_CODE)
    }

    @RequiresApi(VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICKFILE_REQUEST_CODE) {
            data?.data?.also { uri ->
                File().readFile(uri,contentResolver!!,requireContext())
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onItemClick(fileName : String) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                FileProvider.getUriForFile(
                    requireContext(),
                    (context?.packageName) + ".provider",
                    java.io.File(Constants.getFilesDirectoryPath(requireContext()),fileName)
                ),
                Constants.PICKER_ID
            ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        startActivity(intent)
    }
}
package app.adreal.endec.Fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import app.adreal.endec.Constants
import app.adreal.endec.Model.File
import app.adreal.endec.RecyclerView.MainAdapter
import app.adreal.endec.ViewModel.MainViewModel
import app.adreal.endec.databinding.FragmentCategoriesBinding

class Categories : Fragment(), MainAdapter.OnItemClickListener {

    private val binding by lazy{
        FragmentCategoriesBinding.inflate(layoutInflater)
    }

    private val adapter by lazy{
        MainAdapter(requireContext(),this)
    }

    private val mainViewModel by lazy{
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val recyclerView by lazy{
        binding.categoriesRecyclerView
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        initRecyclerView()

        mainViewModel.readAllForExtension(returnType()).observe(viewLifecycleOwner){
            adapter.setData(it)
            adapter.notifyDataSetChanged()
        }

        return binding.root
    }

    private fun initRecyclerView() {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(fileData: File) {
        Constants.openFile(requireContext(),fileData)
    }

    private fun returnType() : List<String>{
        return when(arguments?.getString("type")){
            "image" -> Constants.imageMimeTypes
            "music" -> Constants.audioMimeTypes
            "video" -> Constants.videoMimeTypes
            "document" -> Constants.documentMimeTypes
            "application" -> Constants.apkMimeTypes
            "miscellaneous" -> Constants.imageMimeTypes
            else -> Constants.imageMimeTypes
        }
    }
}
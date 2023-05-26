package app.adreal.endec.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.adreal.endec.Constants
import app.adreal.endec.Encryption.Encryption
import app.adreal.endec.File.File
import app.adreal.endec.R
import app.adreal.endec.RecyclerView.MainAdapter
import app.adreal.endec.ViewModel.MainViewModel
import app.adreal.endec.databinding.FragmentMainBinding
import com.google.android.material.snackbar.Snackbar


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
            if(mainViewModel.filesList.isEmpty()){
                adapter.notifyItemRangeInserted(0,it.size)
            }else if(it.size != mainViewModel.filesList.size){
                adapter.notifyItemInserted(0)
                adapter.notifyItemRangeChanged(1,it.size)
            }

            mainViewModel.filesList = it as MutableList<app.adreal.endec.Model.File>
        }

        binding.settings.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
        }

        binding.encrypt.setOnClickListener {
            openExplorer()
        }

        binding.decrypt.setOnClickListener{

        }

        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedData = mainViewModel.filesList[viewHolder.adapterPosition]
                val position = viewHolder.adapterPosition
                mainViewModel.filesList.removeAt(viewHolder.adapterPosition)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                mainViewModel.delete(deletedData)

                Snackbar.make(binding.root, "Deleted " + deletedData.fileName, Snackbar.LENGTH_LONG)
                    .setAction(
                        "Undo"
                    ) {
                        mainViewModel.filesList.add(position,deletedData)
                        adapter.setData(mainViewModel.filesList)
                        adapter.notifyItemInserted(position)
                        mainViewModel.add(deletedData)
                    }.show()
            }
        }).attachToRecyclerView(recyclerView)

        return binding.root
    }

    private fun initRecyclerView(){
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
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

    @RequiresApi(VERSION_CODES.O)
    override fun onItemClick(fileData : app.adreal.endec.Model.File) {
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(
                FileProvider.getUriForFile(
                    requireContext(),
                    (context?.packageName) + Constants.PROVIDER,
                    java.io.File(File().createTempFile(requireContext(), fileData))
                ),
                Constants.PICKER_ID
            ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).setPackage("com.google.android.apps.photos")
        startActivity(intent)
    }

    fun showPopup(v: View, context: Context) {
        val popup = PopupMenu(context, v)
        val inflater: MenuInflater = popup.menuInflater
        inflater.inflate(R.menu.menu, popup.menu)
        popup.show()
    }
}
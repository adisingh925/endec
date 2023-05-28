package app.adreal.endec.Fragments

import android.app.Activity
import android.content.Intent
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.adreal.endec.Constants
import app.adreal.endec.File.File
import app.adreal.endec.R
import app.adreal.endec.RecyclerView.MainAdapter
import app.adreal.endec.ViewModel.MainViewModel
import app.adreal.endec.databinding.FragmentMainBinding
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class MainFragment : Fragment(), MainAdapter.OnItemClickListener {

    companion object {
        const val ENCRYPT_REQUEST_CODE = 10
        const val DECRYPT_REQUEST_CODE = 20
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

    private val adapter by lazy {
        MainAdapter(requireContext(), this)
    }

    private val recyclerView by lazy {
        binding.recyclerView
    }

    @RequiresApi(VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        initRecyclerView()

        mainViewModel.filesData.observe(viewLifecycleOwner) {
            binding.noFiles.isVisible = it.isEmpty()
            adapter.setData(it)
            adapter.notifyDataSetChanged()
            mainViewModel.filesList = it as MutableList<app.adreal.endec.Model.File>
        }

        binding.settings.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
        }

        binding.encrypt.setOnClickListener {
            openExplorer(ENCRYPT_REQUEST_CODE)
        }

        binding.decrypt.setOnClickListener {
            openExplorer(DECRYPT_REQUEST_CODE)
        }

        binding.row1cardView1.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_categories, createBundle("music"))
        }

        binding.row1cardView2.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_categories, createBundle("video"))
        }

        binding.row2cardView1.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_categories, createBundle("image"))
        }

        binding.row2cardView2.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_categories, createBundle("document"))
        }

        binding.row3cardView1.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_categories, createBundle("application"))
        }

        binding.row3cardView2.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_categories, createBundle("miscellaneous"))
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
                mainViewModel.delete(deletedData)

                val snackbar = Snackbar.make(
                    binding.root,
                    "Deleted " + deletedData.fileName,
                    Snackbar.LENGTH_LONG
                )

                snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onShown(transientBottomBar: Snackbar?) {
                        super.onShown(transientBottomBar)
                    }

                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                        if(event == 2 || event == 4){
                            File().deleteFile(requireContext(), deletedData)
                        }
                    }
                })

                snackbar.setAction(
                    "Undo"
                ) {
                    mainViewModel.add(deletedData)
                }.show()
            }
        }).attachToRecyclerView(recyclerView)

        return binding.root
    }

    private fun initRecyclerView() {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun openExplorer(requestCode : Int) {
        val intent = Intent()
            .setType(Constants.PICKER_ID)
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(intent, requestCode)
    }

    @RequiresApi(VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == ENCRYPT_REQUEST_CODE) {
            data?.data?.also { uri ->
                File().readFile(uri, contentResolver!!, requireContext())
            }
        }else if(resultCode == Activity.RESULT_OK && requestCode == DECRYPT_REQUEST_CODE){

        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @RequiresApi(VERSION_CODES.O)
    override fun onItemClick(fileData: app.adreal.endec.Model.File) {
        Constants.openFile(requireContext(), fileData)
    }

    private fun createBundle(type: String): Bundle {
        val x = Bundle()
        x.putString("type",type)
        x.putString("heading",type)
        return x
    }
}
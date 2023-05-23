package app.adreal.endec.Fragments

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Files
import android.provider.MediaStore.getMediaUri
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import app.adreal.endec.R
import app.adreal.endec.ViewModel.MainViewModel
import app.adreal.endec.databinding.FragmentMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class MainFragment : Fragment() {

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mainViewModel.generateArgonBasedAesKey()

        mainViewModel.onKeyChange.observe(viewLifecycleOwner) {
            binding.key.text = it
        }

        binding.settings.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_settingsFragment)
        }

        binding.encrypt.setOnClickListener {
            openExplorer()
        }

        return binding.root
    }

    private fun openExplorer() {
        val pickFile = Intent(
            Intent.ACTION_PICK
        )
        pickFile.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "*/*")
        pickFile.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        pickFile.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        startActivityForResult(pickFile, PICKFILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICKFILE_REQUEST_CODE) {
            data?.data?.also { uri ->
                Log.d("MIME Type", contentResolver?.getType(uri).toString())
                readFile(uri)
                dumpImageMetaData(uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun dumpImageMetaData(uri: Uri) {
        val cursor: Cursor? = contentResolver?.query(uri, null, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val displayName: String =
                    it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                Log.d("File Data", "Display Name: $displayName")
                val sizeIndex: Int = it.getColumnIndex(OpenableColumns.SIZE)
                val size: String = if (!it.isNull(sizeIndex)) {
                    it.getString(sizeIndex)
                } else {
                    "Unknown"
                }
                Log.d("File data", "Size: $size")
            }
        }
    }

    private fun readFile(uri : Uri){
        val file = File(uri.path.toString())
    }
}
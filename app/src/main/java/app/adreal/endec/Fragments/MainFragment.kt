package app.adreal.endec.Fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
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

        app.adreal.endec.File.File().createOrGetFile(requireContext(),"test.txt", "hello".encodeToByteArray())

        CoroutineScope(Dispatchers.IO).launch {
            val decryptedData = mainViewModel.decrypt(mainViewModel.encryptData("hello".encodeToByteArray()))
            Log.d("decrypted Data", decryptedData.decodeToString())
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
        val intent = Intent()
            .setType("*/*")
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(intent, PICKFILE_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICKFILE_REQUEST_CODE) {
            data?.data?.also { uri ->
                contentResolver?.let { app.adreal.endec.File.File().getMIMEType(uri, it) }
                contentResolver?.let { app.adreal.endec.File.File().dumpImageMetaData(uri, it) }
                readFile(uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun readFile(uri: Uri): ByteArray? {
        val inputStream = contentResolver?.openInputStream(uri)
        val data = inputStream?.readBytes()
        inputStream?.close()
        return data
    }
}
package app.adreal.endec.ViewModel

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.adreal.endec.Encryption.Encryption
import app.adreal.endec.SharedPreferences.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Base64

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val AES_KEY = "aesKey"
    }

    val onKeyChange = MutableLiveData<String>()

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateArgonBasedAesKey() {
        viewModelScope.launch(Dispatchers.IO) {
            storeArgonAESKey(
                Encryption(getApplication<Application>().applicationContext).generateKeyFromArgon().encoded.decodeToString()
            )
        }
    }

    private fun storeArgonAESKey(aesKey: String) {
        SharedPreferences.write(AES_KEY, aesKey)
        onKeyChange.postValue(aesKey)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptData(data : ByteArray) : ByteArray{
        return Encryption(getApplication<Application>().applicationContext).encryptUsingSymmetricKey(data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decrypt(data : ByteArray) : ByteArray{
        return Encryption(getApplication<Application>().applicationContext).decryptUsingSymmetricKey(data)
    }
}
package app.adreal.endec.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.adreal.endec.Encryption.Encryption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    fun generateArgonBasedAesKey(){
        viewModelScope.launch(Dispatchers.IO) {
            Encryption().generateKeyFromArgon()
        }
    }
}
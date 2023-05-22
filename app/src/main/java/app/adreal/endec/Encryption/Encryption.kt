package app.adreal.endec.Encryption

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2KtResult
import com.lambdapioneer.argon2kt.Argon2Mode
import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class Encryption {

    companion object {
        const val AES_KEY_LENGTH = 256
        const val ENCRYPTION_ALGORITHM = "AES"
    }

    private val argon2 by lazy {
        Argon2Kt()
    }

    private fun generateArgon2Hash(
        passwordByteArray: ByteArray,
        saltByteArray: ByteArray
    ): Argon2KtResult {
        return argon2.hash(
            mode = Argon2Mode.ARGON2_I,
            password = passwordByteArray,
            salt = saltByteArray,
            tCostInIterations = 5,
            mCostInKibibyte = 65536
        )
    }

    private fun generateAESKeyFromHash(hash: String): SecretKey {
        val keyBytes: ByteArray = hash.substring(0, AES_KEY_LENGTH / 8).toByteArray()
        return SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM)
    }

    fun generateKeyFromArgon() {
        val aesKey = generateAESKeyFromHash(
            generateArgon2Hash(
                "password".encodeToByteArray(),
                "f1nd1ngn3m0".encodeToByteArray()
            ).rawHashAsHexadecimal(true)
        )
    }
}
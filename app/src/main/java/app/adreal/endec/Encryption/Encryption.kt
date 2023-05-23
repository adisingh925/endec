package app.adreal.endec.Encryption

import android.content.Context
import android.os.Build
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import app.adreal.endec.Constants
import app.adreal.endec.R
import app.adreal.endec.SharedPreferences.SharedPreferences
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2KtResult
import com.lambdapioneer.argon2kt.Argon2Mode
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Encryption(private val context: Context) {

    companion object {
        const val AES_KEY_LENGTH = 256
        const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE_AES = KeyProperties.BLOCK_MODE_CBC
        private const val PADDING_AES = KeyProperties.ENCRYPTION_PADDING_PKCS7
        private const val TRANSFORMATION_AES = "$ENCRYPTION_ALGORITHM/$BLOCK_MODE_AES/$PADDING_AES"
    }

    private val argon2 by lazy {
        Argon2Kt()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptUsingSymmetricKey(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION_AES)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), getIV())
        return cipher.doFinal(data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decryptUsingSymmetricKey(data : ByteArray) : ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION_AES)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), getIV())
        return cipher.doFinal(data)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getSecretKey() : SecretKey{
        return SecretKeySpec(SharedPreferences.read(Constants.AES_KEY,generateKeyFromArgon().encoded.decodeToString()).toString().encodeToByteArray(), ENCRYPTION_ALGORITHM)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getIV() : IvParameterSpec{
        return IvParameterSpec(SharedPreferences.read(context.resources.getString(R.string.iv),context.getString(R.string.default_iv)).toString().encodeToByteArray())
    }

    private fun generateArgon2Hash(
        argonMode: Argon2Mode,
        passwordByteArray: ByteArray,
        saltByteArray: ByteArray,
        memoryCost: Int,
        iteration: Int,
    ): Argon2KtResult {
        return argon2.hash(
            mode = argonMode,
            password = passwordByteArray,
            salt = saltByteArray,
            tCostInIterations = iteration,
            mCostInKibibyte = memoryCost
        )
    }

    private fun generateAESKeyFromHash(hash: String): SecretKey {
        val keyBytes: ByteArray = hash.substring(0, AES_KEY_LENGTH / 8).toByteArray()
        return SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM)
    }

    fun generateKeyFromArgon(): SecretKey {
        return generateAESKeyFromHash(
            generateArgon2Hash(
                getArgonMode(),
                getPassword(),
                getSalt(),
                getMemoryCost(),
                getIteration()
            ).rawHashAsHexadecimal(true)
        )
    }

    private fun getPassword(): ByteArray {
        return SharedPreferences.read(
            context.resources.getString(R.string.password),
            context.resources.getString(R.string.default_password)
        ).toString().encodeToByteArray()
    }

    private fun getSalt(): ByteArray {
        return SharedPreferences.read(
            context.resources.getString(R.string.salt),
            context.resources.getString(R.string.default_salt)
        ).toString().encodeToByteArray()
    }

    private fun getIteration(): Int {
        return SharedPreferences.read(
            context.resources.getString(R.string.iteration),
            context.resources.getInteger(R.integer.default_iteration)
        ).toString().toInt()
    }

    private fun getMemoryCost(): Int {
        return SharedPreferences.read(
            context.resources.getString(R.string.memory_cost),
            context.resources.getInteger(R.integer.default_memory_cost)
        ).toString().toInt()
    }

    private fun getArgonMode() : Argon2Mode{
        return when (SharedPreferences.read(
            context.resources.getString(R.string.mode),
            context.resources.getString(R.string.default_argon_mode)
        ).toString().toInt()) {
            0 -> Argon2Mode.ARGON2_D
            1 -> Argon2Mode.ARGON2_I
            2 -> Argon2Mode.ARGON2_ID
            else -> Argon2Mode.ARGON2_D
        }
    }
}
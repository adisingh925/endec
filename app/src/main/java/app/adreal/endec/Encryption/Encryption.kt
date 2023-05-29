package app.adreal.endec.Encryption

import android.content.Context
import android.os.Build
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import app.adreal.endec.Model.KDFParameters
import app.adreal.endec.R
import app.adreal.endec.SharedPreferences.SharedPreferences
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2KtResult
import com.lambdapioneer.argon2kt.Argon2Mode
import com.lambdapioneer.argon2kt.Argon2Version
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
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
    fun encryptUsingSymmetricKey(fos: FileOutputStream, cipher: Cipher): CipherOutputStream {
        return CipherOutputStream(fos, cipher)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun decryptUsingSymmetricKey(fis: FileInputStream, cipher: Cipher): CipherInputStream {
        return CipherInputStream(fis, cipher)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCipher(
        mode: Int, argonParameters: KDFParameters = KDFParameters(
            SharedPreferences.read(
                context.resources.getString(R.string.salt),
                context.resources.getString(R.string.default_salt)
            ).toString(),
            SharedPreferences.read(
                context.resources.getString(R.string.iteration),
                context.resources.getInteger(R.integer.default_iteration)
            ).toString(),
            SharedPreferences.read(
                context.resources.getString(R.string.memory_cost),
                context.resources.getInteger(R.integer.default_memory_cost)
            ).toString(),
            SharedPreferences.read(
                context.resources.getString(R.string.hash),
                context.resources.getInteger(R.integer.min_hash_length)
            ).toString(),
            SharedPreferences.read(
                context.resources.getString(R.string.version),
                context.resources.getString(R.string.default_argon_version)
            ).toString(),
            SharedPreferences.read(
                context.resources.getString(R.string.mode),
                context.resources.getString(R.string.default_argon_mode)
            ).toString(),
            SharedPreferences.read(
                context.resources.getString(R.string.parallelism),
                context.resources.getInteger(R.integer.default_parallelism_value)
            ).toString()
        )
    ): Cipher {
        val cipher = Cipher.getInstance(TRANSFORMATION_AES)
        cipher.init(
            mode,
            generateKeyFromArgon(
                argonParameters.mode.toInt(),
                argonParameters.salt.encodeToByteArray(),
                argonParameters.memoryCost.toInt(),
                argonParameters.iteration.toInt(),
                argonParameters.version.toInt(),
                argonParameters.hashLength.toInt(),
                argonParameters.parallelism.toInt()
            ),
            getIV()
        )
        return cipher
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getIV(): IvParameterSpec {
        return IvParameterSpec(
            SharedPreferences.read(
                context.resources.getString(R.string.iv),
                context.getString(R.string.default_iv)
            ).toString().encodeToByteArray()
        )
    }

    private fun generateArgon2Hash(
        argonMode: Argon2Mode,
        passwordByteArray: ByteArray,
        saltByteArray: ByteArray,
        memoryCost: Int,
        iteration: Int,
        version: Argon2Version,
        hashLength: Int,
        parallelism: Int
    ): Argon2KtResult {
        return argon2.hash(
            mode = argonMode,
            password = passwordByteArray,
            salt = saltByteArray,
            tCostInIterations = iteration,
            mCostInKibibyte = memoryCost,
            version = version,
            hashLengthInBytes = hashLength,
            parallelism = parallelism
        )
    }

    private fun generateAESKeyFromHash(hash: String): SecretKey {
        val keyBytes: ByteArray = hash.substring(0, AES_KEY_LENGTH / 8).toByteArray()
        return SecretKeySpec(keyBytes, ENCRYPTION_ALGORITHM)
    }

    private fun generateKeyFromArgon(
        argon2Mode: Int,
        salt: ByteArray,
        memoryCost: Int,
        iteration: Int,
        argon2Version: Int,
        hashLength: Int,
        parallelism: Int
    ): SecretKey {
        Log.d(
            "data",
            argon2Mode.toString() + " " + salt.decodeToString() + " " + memoryCost.toString() + " " + iteration.toString() + " " + argon2Version.toString() + " " + hashLength.toString() + " " + parallelism.toString()
        )
        val x = generateAESKeyFromHash(
            generateArgon2Hash(
                getArgonMode(argon2Mode),
                getPassword(),
                salt,
                memoryCost,
                iteration,
                getArgonVersion(argon2Version),
                hashLength,
                parallelism
            ).rawHashAsHexadecimal(true)
        )

        Log.d("key",x.encoded.decodeToString())

        return x
    }

    private fun getPassword(): ByteArray {
        return SharedPreferences.read(
            context.resources.getString(R.string.password),
            context.resources.getString(R.string.default_password)
        ).toString().encodeToByteArray()
    }

    private fun getSalt(
        salt: ByteArray = SharedPreferences.read(
            context.resources.getString(R.string.salt),
            context.resources.getString(R.string.default_salt)
        ).toString().encodeToByteArray()
    ): ByteArray {
        return salt
    }

    private fun getIteration(
        iteration: Int = SharedPreferences.read(
            context.resources.getString(R.string.iteration),
            context.resources.getInteger(R.integer.default_iteration)
        ).toString().toInt()
    ): Int {
        return iteration
    }

    private fun getMemoryCost(
        memoryCost: Int = SharedPreferences.read(
            context.resources.getString(R.string.memory_cost),
            context.resources.getInteger(R.integer.default_memory_cost)
        ).toString().toInt()
    ): Int {
        return memoryCost
    }

    private fun getArgonMode(
        mode: Int = SharedPreferences.read(
            context.resources.getString(R.string.mode),
            context.resources.getString(R.string.default_argon_mode)
        ).toString().toInt()
    ): Argon2Mode {
        return when (mode) {
            1 -> Argon2Mode.ARGON2_D
            2 -> Argon2Mode.ARGON2_I
            3 -> Argon2Mode.ARGON2_ID
            else -> Argon2Mode.ARGON2_ID
        }
    }

    private fun getArgonVersion(
        argon2Version: Int = SharedPreferences.read(
            context.resources.getString(R.string.version),
            context.resources.getString(R.string.default_argon_version)
        ).toString().toInt()
    ): Argon2Version {
        return when (argon2Version) {
            1 -> Argon2Version.V10
            2 -> Argon2Version.V13
            else -> Argon2Version.V13
        }
    }

    private fun getHashLength(
        hashLength: Int = SharedPreferences.read(
            context.resources.getString(R.string.hash),
            context.resources.getInteger(R.integer.min_hash_length)
        ).toString().toInt()
    ): Int {
        return hashLength
    }

    private fun getParallelismValue(
        parallelism: Int = SharedPreferences.read(
            context.resources.getString(R.string.parallelism),
            context.resources.getInteger(R.integer.default_parallelism_value)
        ).toString().toInt()
    ): Int {
        return parallelism
    }
}
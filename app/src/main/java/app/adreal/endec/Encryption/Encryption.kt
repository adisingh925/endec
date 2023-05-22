package app.adreal.endec.Encryption

import android.content.Context
import app.adreal.endec.R
import app.adreal.endec.SharedPreferences.SharedPreferences
import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2KtResult
import com.lambdapioneer.argon2kt.Argon2Mode
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class Encryption(private val context: Context) {

    companion object {
        const val AES_KEY_LENGTH = 256
        const val ENCRYPTION_ALGORITHM = "AES"
        const val PASSWORD = "password"
        const val SALT = "salt"
        const val ITERATION = "iteration"
        const val MEMORY_COST = "memory_cost"
    }

    private val argon2 by lazy {
        Argon2Kt()
    }

    private fun generateArgon2Hash(
        passwordByteArray: ByteArray,
        saltByteArray: ByteArray,
        memoryCost : Int,
        iteration : Int,
    ): Argon2KtResult {
        return argon2.hash(
            mode = Argon2Mode.ARGON2_I,
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
                getPassword(),
                getSalt(),
                getMemoryCost(),
                getIteration()
            ).rawHashAsHexadecimal(true)
        )
    }

    private fun getPassword(): ByteArray {
        return SharedPreferences.read(PASSWORD, context.resources.getString(R.string.default_password)).toString().encodeToByteArray()
    }

    private fun getSalt(): ByteArray {
        return SharedPreferences.read(SALT, context.resources.getString(R.string.default_salt)).toString().encodeToByteArray()
    }

    private fun getIteration() : Int{
        return SharedPreferences.read(ITERATION,context.resources.getInteger(R.integer.default_iteration)).toString().toInt()
    }

    private fun getMemoryCost() : Int{
        return SharedPreferences.read(MEMORY_COST,context.resources.getInteger(R.integer.default_memory_cost)).toString().toInt()
    }
}
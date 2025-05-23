package net.sharksystem.sharknetmessengerandroid.sharknet

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import net.sharksystem.asap.ASAPSecurityException
import net.sharksystem.asap.crypto.InMemoASAPKeyStore
import net.sharksystem.asap.pki.ASAPCertificate
import net.sharksystem.asap.utils.DateTimeHelper
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStoreException
import java.util.Calendar
import androidx.core.content.edit

// TODO: Refactor this class to use background coroutineS for key generation and loading, as well as DataStore access
class AndroidASAPKeyStoreNew : InMemoASAPKeyStore {

    companion object {
        const val SN_ANDROID_DEFAULT_SIGNING_ALGORITHM: String = "SHA256withRSA/PSS"

        // SharedPreferences constants
        private const val PREFS_NAME = "shark_net_keystore"
        private const val KEY_KEYPAIR_CREATION_TIME = "ASAPCertificatesKeyPairCreationTime"

        const val KEYSTORE_NAME: String = "AndroidKeyStore"
        const val KEYSTORE_OWNER_ALIAS: String = "ASAPCertificatesKeysOwner"
        const val DEFAULT_KEYSTORE_PWD: String = "asap4ever"
        const val KEY_SIZE: Int = 2048
        val ANY_PURPOSE: Int = KeyProperties.PURPOSE_ENCRYPT or
                KeyProperties.PURPOSE_DECRYPT or KeyProperties.PURPOSE_SIGN or
                KeyProperties.PURPOSE_VERIFY
        const val KEYSTORE_PWD: String = "ASAPCertificatesKeyStorePWD"

        var creationTime = DateTimeHelper.TIME_NOT_SET

        // Gilt das selbe wie für den context? (Siehe Kommentar weiter unten)
        var keyStore: KeyStore? = null

        /*
        * A static field will leak contexts.
        * Non-static inner classes have an implicit reference to their outer class. If that outer class is for example a Fragment or Activity, then this reference means that the long-running handler/loader/task will hold a reference to the activity which prevents it from getting garbage collected.
        *
        * Similarly, direct field references to activities and fragments from these longer running instances can cause leaks.
        *
        *ViewModel classes should never point to Views or non-application Contexts.
        */
        // lateinit var context: Context
    }

    private var context: Context

    @Throws(ASAPSecurityException::class)
    constructor(context: Context, peerID: String) : super(peerID) {
        this.context = context.applicationContext // Use applicationContext to prevent leaks
        try {
            this.reloadKeys(context)
        } catch (e: ASAPSecurityException) {
            this.generateKeyPair()
        }
    }

    private fun getLogStart(): String {
        return this.javaClass.simpleName
    }

    @Throws(KeyStoreException::class)
    private fun getKeyStore(): KeyStore {
        if (Companion.keyStore == null) {
            try {
                Companion.keyStore = KeyStore.getInstance(Companion.KEYSTORE_NAME).apply {
                    load(null)
                }
            } catch (e: Exception) {
                throw KeyStoreException(e.localizedMessage)
            }
        }
        return Companion.keyStore!!
    }

    @Throws(ASAPSecurityException::class)
    private fun reloadKeys(ctx: Context) {
        Log.d(this.getLogStart(), "reload private keys from android key storage")

        // Load the key pair from Android KeyStore
        try {
            val keyStore = getKeyStore()
            val privateKeyEntry = keyStore.getEntry(Companion.KEYSTORE_OWNER_ALIAS, null) as? KeyStore.PrivateKeyEntry
                ?: throw ASAPSecurityException("no keys stored")

            // Extract keys from entry
            val keyPair = KeyPair(
                privateKeyEntry.certificate.publicKey,
                privateKeyEntry.privateKey
            )

            Log.d(this.getLogStart(), "key pair reloaded from android key storage")
            super.setKeyPair(keyPair)
        } catch (e: Exception) {
            Log.d(this.getLogStart(), e.localizedMessage ?: "Unknown error")
            throw ASAPSecurityException("error when reloading key pair: ", e)
        }

        // Load key creation timestamp from SharedPreferences
        if (Companion.creationTime == DateTimeHelper.TIME_NOT_SET) {
            val sharedPrefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            Companion.creationTime = sharedPrefs.getLong(KEY_KEYPAIR_CREATION_TIME, DateTimeHelper.TIME_NOT_SET)
        }
    }

    private fun setCreationTime(ctx: Context, time: Long) {
        Log.d(
            this.getLogStart(),
            "set new creation time: " + DateTimeHelper.long2DateString(time)
        )
        Companion.creationTime = time

        val sharedPrefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit{
            putLong(KEY_KEYPAIR_CREATION_TIME, time)
        }
    }

    @Throws(ASAPSecurityException::class)
    override fun generateKeyPair() {
        try {
            // Setup key validity period
            val start = Calendar.getInstance().apply {
                add(Calendar.DATE, -1)
            }
            val end = Calendar.getInstance().apply {
                add(Calendar.YEAR, ASAPCertificate.DEFAULT_CERTIFICATE_VALIDITY_IN_YEARS)
            }
            // Log key validity period
            Log.d(getLogStart(), "create key pair valid from ${DateTimeHelper.long2DateString(start.timeInMillis)} " +
                    "to ${DateTimeHelper.long2DateString(end.timeInMillis)}")

            // Generate key pair using Android KeyStore
            val keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, Companion.KEYSTORE_NAME
            )

            // Configure key properties
            val keySpec = KeyGenParameterSpec.Builder(Companion.KEYSTORE_OWNER_ALIAS, Companion.ANY_PURPOSE)
                .setRandomizedEncryptionRequired(false)
                .setDigests(
                    KeyProperties.DIGEST_NONE, KeyProperties.DIGEST_MD5,
                    KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA224,
                    KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384,
                    KeyProperties.DIGEST_SHA512
                )
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
                .setEncryptionPaddings(
                    KeyProperties.ENCRYPTION_PADDING_NONE,
                    KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1,
                    KeyProperties.ENCRYPTION_PADDING_RSA_OAEP
                )
                .setUserAuthenticationRequired(false)
                .setKeyValidityStart(start.time)
                .setKeyValidityEnd(end.time)
                .setKeySize(Companion.KEY_SIZE)
                .build()

            // Generate and store the key pair
            keyPairGenerator.initialize(keySpec)
            val keyPair = keyPairGenerator.generateKeyPair()
            setKeyPair(keyPair)
            setCreationTime(context, System.currentTimeMillis())
        } catch (e: Exception) {
            val text = "problems when generating key pair: ${e.message}"
            Log.d(getLogStart(), text)
            throw ASAPSecurityException(text)
        }
    }

    /*
    override fun generateSymmetricKey(): SecretKey? {
        TODO("Not yet implemented")
    }

    override fun setMementoTarget(extraData: ExtraData?) {
        TODO("Not yet implemented")
    }

    override fun isOwner(peerID: CharSequence?): Boolean {
        TODO("Not yet implemented")
    }

    override fun getOwner(): CharSequence? {
        TODO("Not yet implemented")
    }

    override fun getPrivateKey(): PrivateKey? {
        TODO("Not yet implemented")
    }

    override fun getPublicKey(): PublicKey? {
        TODO("Not yet implemented")
    }
    */

    override fun getKeysCreationTime(): Long {
        return Companion.creationTime
    }

    override fun getAsymmetricEncryptionAlgorithm(): String? {
        return DEFAULT_ASYMMETRIC_ENCRYPTION_ALGORITHM
    }

    override fun getAsymmetricSigningAlgorithm(): String? {
        return DEFAULT_ASYMMETRIC_SIGNATURE_ALGORITHM
    }

    override fun getSymmetricEncryptionAlgorithm(): String? {
        return DEFAULT_SYMMETRIC_ENCRYPTION_ALGORITHM
    }

    override fun getSymmetricKeyType(): String? {
        return DEFAULT_SYMMETRIC_KEY_TYPE
    }

    override fun getSymmetricKeyLen(): Int {
        return DEFAULT_SYMMETRIC_KEY_SIZE
    }
}

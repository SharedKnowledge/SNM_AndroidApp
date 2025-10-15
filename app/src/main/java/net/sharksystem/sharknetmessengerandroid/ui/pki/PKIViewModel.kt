package net.sharksystem.sharknetmessengerandroid.ui.pki

import android.util.Log
import net.sharksystem.asap.persons.PersonValues
import net.sharksystem.asap.pki.ASAPCertificate
import net.sharksystem.pki.SharkPKIComponent
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp.Companion.singleton

open class PKIViewModel {
    open var pkiComponent: SharkPKIComponent? = null
        protected set
    open var ownerName: String = ""
        protected set
    open var ownerID: String = ""
        protected set
    open var numberOfPersons: Int = 0
        protected set
    open var isLoading: Boolean = true
        protected set
    open var keyCreationTime: Long = 0
        protected set
    open var isAutoCredentialSending: Boolean = false
        protected set
    open var knownPersons: MutableList<PersonValues> = mutableListOf()
        protected set
    open var totalCertificates: Int = 0
        protected set
    open var errorMessage: String = ""
        protected set

    init {
        loadPKIData()
    }

    private fun loadPKIData() {
        try {
            singleton?.let { app ->
                pkiComponent = app.getPeer()
                    .getComponent(SharkPKIComponent::class.java) as SharkPKIComponent?

                pkiComponent?.let { pki ->
                    ownerName = pki.ownerName.toString()
                    ownerID = pki.ownerID.toString()
                    numberOfPersons = pki.numberOfPersons
                    keyCreationTime = pki.keysCreationTime

                    knownPersons = mutableListOf()
                    for (i in 0 until numberOfPersons) {
                        try {
                            pki.getPersonValuesByPosition(i)?.let { person ->
                                knownPersons.add(person)
                            }
                        } catch (e: Exception) {
                            Log.w("SharkDebug", "Could not load person at position $i", e)
                        }
                    }

                    try {
                        val certificates = pki.certificates
                        totalCertificates = certificates?.size ?: 0
                    } catch (e: Exception) {
                        Log.w("SharkDebug", "Could not count certificates", e)
                        totalCertificates = 0
                    }

                    errorMessage = ""
                } ?: run {
                    errorMessage = "PKI Component not available"
                }
            } ?: run {
                errorMessage = "SharkNet not initialized"
            }
        } catch (e: Exception) {
            errorMessage = "Error loading PKI data: ${e.message}"
            Log.e("SharkDebug", "Error loading PKI data", e)
        } finally {
            isLoading = false
        }
    }

    fun generateNewKeyPair() {
        try {
            pkiComponent?.let { pki ->
                pki.createNewKeyPair()
                refresh()
            }
        } catch (e: Exception) {
            Log.e("SharkDebug", "Error generating new key pair", e)
            errorMessage = "Failed to generate new key pair: ${e.message}"
        }
    }

    // TODO: Use the setBehaviour() method instead of sending the credentials message directly
    // setAutoCredentialBehavior() in SettingsUiState.kt is already done. It just needs to be called.
    fun sendCredentialMessage() {
        try {
            pkiComponent?.sendTransientCredentialMessage()
        } catch (e: Exception) {
            Log.e("SharkDebug", "Error sending credential message", e)
            errorMessage = "Failed to send credential message: ${e.message}"
        }
    }

    fun setAutoCredentialBehavior(enabled: Boolean) {
        try {
            pkiComponent?.let { pki ->
                pki.setBehaviour(
                    SharkPKIComponent.BEHAVIOUR_SEND_CREDENTIAL_FIRST_ENCOUNTER,
                    enabled
                )
                this.isAutoCredentialSending = enabled
            }
        } catch (e: Exception) {
            Log.e("SharkDebug", "Error setting credential behavior", e)
            errorMessage = "Failed to set credential behavior: ${e.message}"
        }
    }

    fun setSigningFailureRate(personID: String?, failureRate: Int) {
        try {
            pkiComponent?.setSigningFailureRate(personID, failureRate)
        } catch (e: Exception) {
            Log.e("SharkDebug", "Error setting signing failure rate", e)
            errorMessage = "Failed to set signing failure rate: ${e.message}"
        }
    }

    open fun getInitialGlobalSigningFailureRate(): Int {
        return try {
            if (pkiComponent != null && knownPersons.isNotEmpty()) {
                val firstPerson = knownPersons.first()
                val personId = firstPerson.userID?.toString() ?: ""
                if (personId.isNotEmpty()) {
                    getPersonSigningFailureRate(personId)
                } else {
                    DEFAULT_FAILURE_RATE
                }
            } else {
                DEFAULT_FAILURE_RATE
            }
        } catch (e: Exception) {
            Log.w("SharkDebug", "Could not get initial global signing failure rate", e)
            DEFAULT_FAILURE_RATE
        }
    }

    fun setAllPeersSigningFailureRate(failureRate: Int) {
        try {
            pkiComponent?.let { pki ->
                if (knownPersons.isNotEmpty()) {
                    knownPersons.forEach { person ->
                        val personId = person.userID?.toString() ?: ""
                        if (personId.isNotEmpty()) {
                            pki.setSigningFailureRate(personId, failureRate)
                        }
                    }
                    Log.i(
                        "SharkDebug",
                        "Set signing failure rate to $failureRate for all ${knownPersons.size} peers"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("SharkDebug", "Error setting signing failure rate for all peers", e)
            errorMessage = "Failed to set signing failure rate for all peers: ${e.message}"
        }
    }

    fun getPersonSigningFailureRate(personID: String?): Int {
        return try {
            pkiComponent?.getSigningFailureRate(personID) ?: DEFAULT_FAILURE_RATE
        } catch (e: Exception) {
            Log.w("SharkDebug", "Could not get signing failure rate for $personID", e)
            DEFAULT_FAILURE_RATE
        }
    }

    /**
     * Save the current PKI state to persistent storage
     * Is this even necessary?
     */
    fun saveMemento() {
        try {
            pkiComponent?.saveMemento()
        } catch (e: Exception) {
            Log.e("SharkDebug", "Error saving memento", e)
            errorMessage = "Failed to save settings: ${e.message}"
        }
    }

    fun getCertificatesByPerson(personID: String?): Collection<ASAPCertificate?> {
        return try {
            pkiComponent?.getCertificatesBySubject(personID) ?: emptyList()
        } catch (e: Exception) {
            Log.w("SharkDebug", "Could not get certificates for $personID", e)
            emptyList()
        }
    }

    open fun getIdentityAssurance(personID: String?): Int {
        return try {
            pkiComponent?.getIdentityAssurance(personID) ?: MAX_UNCERTAINTY
        } catch (e: Exception) {
            Log.w("SharkDebug", "Could not get identity assurance for $personID", e)
            MAX_UNCERTAINTY // Maximum uncertainty
        }
    }

    fun hasError(): Boolean = errorMessage.isNotEmpty()

    /**
     * Refresh the PKI data
     */
    fun refresh() {
        isLoading = true
        errorMessage = ""
        loadPKIData()
    }

    companion object {
        private const val DEFAULT_FAILURE_RATE = 5
        private const val MAX_UNCERTAINTY = 10
    }
}
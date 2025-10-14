package net.sharksystem.sharknetmessengerandroid.ui.certificates

import android.util.Log
import net.sharksystem.pki.SharkPKIComponent
import net.sharksystem.sharknetmessengerandroid.sharknet.SharkNetApp
import net.sharksystem.asap.pki.ASAPCertificate
import java.text.SimpleDateFormat
import java.util.*

data class CertificateItem(
    val certificate: ASAPCertificate,
    val subjectName: String,
    val subjectId: String,
    val issuerName: String,
    val issuerId: String,
    val validSince: String,
    val isIssuedByMe: Boolean,
    val isIssuedForMe: Boolean
)

class CertificatesViewModel {
    private var pkiComponent: SharkPKIComponent? = null
    private var ownerID: String = ""

    var isLoading: Boolean = true
        private set

    var errorMessage: String = ""
        private set

    var allCertificates: List<CertificateItem> = emptyList()
        private set

    var filteredCertificates: List<CertificateItem> = emptyList()
        private set

    var totalCertificates: Int = 0
        private set

    var certificatesIssuedByMe: Int = 0
        private set

    var certificatesIssuedForMe: Int = 0
        private set

    init {
        loadCertificates()
    }

    private fun loadCertificates() {
        try {
            if (SharkNetApp.singleton != null) {
                pkiComponent = SharkNetApp.singleton
                    ?.getPeer()
                    ?.getComponent(SharkPKIComponent::class.java) as? SharkPKIComponent

                if (pkiComponent != null) {
                    ownerID = pkiComponent!!.ownerID.toString()

                    val certificates = pkiComponent!!.certificates
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

                    allCertificates = certificates.map { cert ->
                        val subjectId = cert.subjectID.toString()
                        val issuerId = cert.issuerID.toString()
                        val isIssuedByMe = issuerId == ownerID
                        val isIssuedForMe = subjectId == ownerID

                        // Get subject name from person values
                        val subjectName = try {
                            if (isIssuedForMe) {
                                pkiComponent!!.ownerName.toString()
                            } else {
                                pkiComponent!!.getPersonValuesByID(subjectId).name?.toString() ?: "Unknown"
                            }
                        } catch (_: Exception) {
                            "Unknown"
                        }

                        // Get issuer name from person values
                        val issuerName = try {
                            if (isIssuedByMe) {
                                pkiComponent!!.ownerName.toString()
                            } else {
                                pkiComponent!!.getPersonValuesByID(issuerId).name?.toString() ?: "Unknown"
                            }
                        } catch (_: Exception) {
                            "Unknown"
                        }

                        val validSinceDate = dateFormat.format(cert.validSince.time)

                        CertificateItem(
                            certificate = cert,
                            subjectName = subjectName,
                            subjectId = subjectId,
                            issuerName = issuerName,
                            issuerId = issuerId,
                            validSince = validSinceDate,
                            isIssuedByMe = isIssuedByMe,
                            isIssuedForMe = isIssuedForMe
                        )
                    }.sortedByDescending { it.certificate.validSince }

                    filteredCertificates = allCertificates
                    totalCertificates = allCertificates.size
                    certificatesIssuedByMe = allCertificates.count { it.isIssuedByMe }
                    certificatesIssuedForMe = allCertificates.count { it.isIssuedForMe }

                    errorMessage = ""
                } else {
                    errorMessage = "PKI Component not available"
                }
            } else {
                errorMessage = "SharkNet not initialized"
            }
        } catch (e: Exception) {
            errorMessage = "Error loading certificates: ${e.message}"
            Log.e("SharkDebug", "Error loading certificates", e)
        } finally {
            isLoading = false
        }
    }

    fun filterCertificates(filter: CertificateFilter) {
        filteredCertificates = when (filter) {
            CertificateFilter.ALL -> allCertificates
            CertificateFilter.ISSUED_BY_ME -> allCertificates.filter { it.isIssuedByMe }
            CertificateFilter.ISSUED_FOR_ME -> allCertificates.filter { it.isIssuedForMe }
        }
    }

    fun refresh() {
        isLoading = true
        errorMessage = ""
        loadCertificates()
    }

    fun hasError(): Boolean = errorMessage.isNotEmpty()
}

enum class CertificateFilter {
    ALL,
    ISSUED_BY_ME,
    ISSUED_FOR_ME
}

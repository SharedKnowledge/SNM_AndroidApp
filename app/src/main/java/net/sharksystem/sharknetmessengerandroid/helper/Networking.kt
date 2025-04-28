package net.sharksystem.sharknetmessengerandroid.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import java.net.Inet4Address

/**
 * Hilfsklasse zur Netzwerkkommunikation.
 *
 * Diese Klasse stellt Methoden zur Verfügung, um Netzwerkdetails wie z.B.
 * die lokale IPv4-Adresse des Geräts zu ermitteln.
 */
class Networking {
    /**
     * Gibt die lokale IPv4-Adresse des aktuellen Netzwerkes zurück.
     *
     * @param context Der Context der Anwendung (z.B. `Activity` oder `Application`).
     * @return Die lokale IP-Adresse als String (z.B. "192.168.0.101"),
     *         oder ein Fehlertext, falls keine Verbindung besteht oder keine IP gefunden wurde.
     */

    fun getLocalIpAddress(context: Context): String {
        // Zugriff auf den ConnectivityManager, um Netzwerkdetails zu erhalten
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Holt das aktuell aktive Netzwerk (z.B. WLAN oder mobile Daten)
        val network = connectivityManager.activeNetwork ?: return "Keine Verbindung"
        // Holt die Netzwerkeigenschaften des aktiven Netzwerks
        val linkProperties: LinkProperties = connectivityManager.getLinkProperties(network) ?: return "Keine IP"
        // Gibt das erste linkAdresses Objekt zurück
        val ipAddress = linkProperties.linkAddresses
            //lambda Bedingung, also gibt erstes zurück, dass Bedingung erfüllt und wenn keins, dann null
            .firstOrNull { it.address is Inet4Address && !it.address.isLoopbackAddress }
            ?.address

        return ipAddress?.hostAddress ?: "Keine IP gefunden" //wenn null, das nach : ausführen
    }
}

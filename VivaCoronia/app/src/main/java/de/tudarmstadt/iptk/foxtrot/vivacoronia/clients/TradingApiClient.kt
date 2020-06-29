package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.location.Location
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer

object TradingApiClient : ApiBaseClient() {
    private val dummyList = listOf(
        Offer("Klopapier 10 Rollen", 3, 39.90, Location(""), "4-lagiges Klopapier, vergoldet und mit Duftstoffen versehen"),
        Offer("Zahnpasta", 1, 3.30, Location(""), "Sorgt für strahlend weiße Zähne"),
        Offer("Weizenmehl 500g", 2, 2.22, Location(""), "Jeder hat es, keiner braucht es. Ich auch nicht, daher verkaufe ich will ich es los werden"),
        Offer("Schutzmaske", 25, 300.00, Location(""), "Ich habe vor der Pandemie alle Schutzmasken aufgekauft! Huehuehuehue!"),
        Offer("Klopapier 10 Rollen", 3, 39.90, Location(""), "4-lagiges Klopapier, vergoldet und mit Duftstoffen versehen"),
        Offer("Zahnpasta", 1, 3.30, Location(""), "Sorgt für strahlend weiße Zähne"),
        Offer("Weizenmehl 500g", 2, 2.22, Location(""), "Jeder hat es, keiner braucht es. Ich auch nicht, daher verkaufe ich will ich es los werden"),
        Offer("Schutzmaske", 25, 300.00, Location(""), "Ich habe vor der Pandemie alle Schutzmasken aufgekauft! Huehuehuehue!"),
        Offer("Klopapier 10 Rollen", 3, 39.90, Location(""), "4-lagiges Klopapier, vergoldet und mit Duftstoffen versehen"),
        Offer("Zahnpasta", 1, 3.30, Location(""), "Sorgt für strahlend weiße Zähne"),
        Offer("Weizenmehl 500g", 2, 2.22, Location(""), "Jeder hat es, keiner braucht es. Ich auch nicht, daher verkaufe ich will ich es los werden"),
        Offer("Schutzmaske", 25, 300.00, Location(""), "Ich habe vor der Pandemie alle Schutzmasken aufgekauft! Huehuehuehue!"),
        Offer("Klopapier 10 Rollen", 3, 39.90, Location(""), "4-lagiges Klopapier, vergoldet und mit Duftstoffen versehen"),
        Offer("Zahnpasta", 1, 3.30, Location(""), "Sorgt für strahlend weiße Zähne"),
        Offer("Weizenmehl 500g", 2, 2.22, Location(""), "Jeder hat es, keiner braucht es. Ich auch nicht, daher verkaufe ich will ich es los werden"),
        Offer("Schutzmaske", 25, 300.00, Location(""), "Ich habe vor der Pandemie alle Schutzmasken aufgekauft! Huehuehuehue!")
    )

    fun getMyOffers(): List<Offer> {
        return dummyList
    }
}
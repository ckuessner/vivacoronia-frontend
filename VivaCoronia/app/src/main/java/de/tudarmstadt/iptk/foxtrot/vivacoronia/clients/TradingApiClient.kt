package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.location.Location
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer

object TradingApiClient : ApiBaseClient() {
    private val dummyList = mutableListOf(
        Offer("Klopapier 10 Rollen", 3, 39.90, Location(""), "4-lagiges Klopapier, vergoldet und mit Duftstoffen versehen", "1"),
        Offer("Zahnpasta", 1, 3.30, Location(""), "Sorgt für strahlend weiße Zähne", "2"),
        Offer("Weizenmehl 500g", 2, 2.22, Location(""), "Jeder hat es, keiner braucht es. Ich auch nicht, daher verkaufe ich will ich es los werden", "3"),
        Offer("Schutzmaske", 25, 300.00, Location(""), "Ich habe vor der Pandemie alle Schutzmasken aufgekauft! Huehuehuehue!", "4"),
        Offer("Klopapier 10 Rollen", 3, 39.90, Location(""), "4-lagiges Klopapier, vergoldet und mit Duftstoffen versehen", "5"),
        Offer("Zahnpasta", 1, 3.30, Location(""), "Sorgt für strahlend weiße Zähne", "6"),
        Offer("Weizenmehl 500g", 2, 2.22, Location(""), "Jeder hat es, keiner braucht es. Ich auch nicht, daher verkaufe ich will ich es los werden", "7"),
        Offer("Schutzmaske", 25, 300.00, Location(""), "Ich habe vor der Pandemie alle Schutzmasken aufgekauft! Huehuehuehue!", "8"),
        Offer("Klopapier 10 Rollen", 3, 39.90, Location(""), "4-lagiges Klopapier, vergoldet und mit Duftstoffen versehen", "9"),
        Offer("Zahnpasta", 1, 3.30, Location(""), "Sorgt für strahlend weiße Zähne", "10"),
        Offer("Weizenmehl 500g", 2, 2.22, Location(""), "Jeder hat es, keiner braucht es. Ich auch nicht, daher verkaufe ich will ich es los werden", "11"),
        Offer("Schutzmaske", 25, 300.00, Location(""), "Ich habe vor der Pandemie alle Schutzmasken aufgekauft! Huehuehuehue!", "12"),
        Offer("Klopapier 10 Rollen", 3, 39.90, Location(""), "4-lagiges Klopapier, vergoldet und mit Duftstoffen versehen", "13"),
        Offer("Zahnpasta", 1, 3.30, Location(""), "Sorgt für strahlend weiße Zähne", "14"),
        Offer("Weizenmehl 500g", 2, 2.22, Location(""), "Jeder hat es, keiner braucht es. Ich auch nicht, daher verkaufe ich will ich es los werden", "15"),
        Offer("Schutzmaske", 25, 300.00, Location(""), "Ich habe vor der Pandemie alle Schutzmasken aufgekauft! Huehuehuehue!", "16")
    )

    fun getMyOffers(): MutableList<Offer> {
        return dummyList // TODO
    }

    fun deleteOffer(id: String): Boolean {
        return true // TODO
        /*return dummyList.removeIf { offer ->
            offer.id == id
        }*/
    }
}
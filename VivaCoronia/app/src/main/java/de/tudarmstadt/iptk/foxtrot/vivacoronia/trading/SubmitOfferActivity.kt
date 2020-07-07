package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.content.ContextCompat.startActivity
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Category
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer

private const val ARG_OFFER = "offer"


class AddOfferActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_offer)

        val passedOffer: Offer? = intent.getParcelableExtra(ARG_OFFER)

        val submitButton = findViewById<Button>(R.id.submit_offer)
        if (passedOffer != null) {
            submitButton.text = resources.getString(R.string.save)
        }

        val offer = passedOffer ?: Offer()
        val fragment = OfferDetailFragment.newInstance(offer)
        submitButton.setOnClickListener {
            TradingApiClient.putOffer(fragment.getOffer())
            finish()
        }

        supportFragmentManager.beginTransaction().replace(R.id.offer_detail_container, fragment).addToBackStack(null).commit()
    }

    companion object {
        @JvmStatic
        fun start(context: Context, offer: Offer?) {
            val intent = Intent(context, AddOfferActivity::class.java)
            if (offer != null)
                intent.putExtra(ARG_OFFER, offer)
            startActivity(context, intent, null)
            /*val bundle = Bundle()
            bundle.putParcelable(ARG_OFFER, offer)
            startActivity(context, Intent(context, AddOfferActivity::class.java), bundle)*/
        }

        var categories = listOf<Category>()
    }
}
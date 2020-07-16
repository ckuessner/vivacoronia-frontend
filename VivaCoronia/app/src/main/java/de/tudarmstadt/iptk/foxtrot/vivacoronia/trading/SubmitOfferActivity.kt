package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val ARG_OFFER = "offer"


class SubmitOfferActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_offer)

        val passedOffer: Offer? = intent.getParcelableExtra(ARG_OFFER)

        val submitButton = findViewById<Button>(R.id.submit_offer)
        if (passedOffer != null) {
            submitButton.text = resources.getString(R.string.save)
            title = resources.getString(R.string.edit_offer)
        }

        val offer = passedOffer ?: Offer()
        val fragment = OfferDetailFragment.newInstance(offer)
        submitButton.setOnClickListener {
            submitButton.isEnabled = false
            GlobalScope.launch {
                try {
                    val result = TradingApiClient.putOffer(fragment.getOffer(), this@SubmitOfferActivity)
                    if (result != null)
                        runOnUiThread { finish() }
                    else
                        runOnUiThread {
                            Toast.makeText(this@SubmitOfferActivity,"Something went wrong. Please check your input and try again.", Toast.LENGTH_SHORT).show()
                            submitButton.isEnabled = true
                        }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@SubmitOfferActivity, "Oops, something went wrong. Please check your input and internet connection.", Toast.LENGTH_SHORT).show()
                        submitButton.isEnabled = true
                    }
                }
            }
        }

        supportFragmentManager.beginTransaction().replace(R.id.offer_detail_container, fragment).commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        @JvmStatic
        fun start(context: Context, offer: Offer?) {
            val intent = Intent(context, SubmitOfferActivity::class.java)
            if (offer != null)
                intent.putExtra(ARG_OFFER, offer)
            startActivity(context, intent, null)
            /*val bundle = Bundle()
            bundle.putParcelable(ARG_OFFER, offer)
            startActivity(context, Intent(context, AddOfferActivity::class.java), bundle)*/
        }
    }
}
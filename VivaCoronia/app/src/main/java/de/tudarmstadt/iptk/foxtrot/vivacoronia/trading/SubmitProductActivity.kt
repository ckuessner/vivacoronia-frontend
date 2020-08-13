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
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.BaseProduct
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Need
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.needs.NeedDetailFragment
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.offers.OfferDetailFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val ARG_OFFER = "offer"
private const val ARG_SHOW_OFFER = "showOffer"

class SubmitProductActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_offer)

        val passedOffer: Offer? = intent.getParcelableExtra(ARG_OFFER)
        val showOffer = intent.getBooleanExtra(ARG_SHOW_OFFER, true)

        val submitButton = findViewById<Button>(R.id.submit_offer)
        if (!showOffer) {
            submitButton.text = getString(R.string.add_need)
            title = getString(R.string.add_need)
        }
        if (passedOffer != null) {
            submitButton.text = resources.getString(R.string.save)
            title = resources.getString(R.string.edit_offer)
        }

        val offer = passedOffer ?: Offer()

        val fragment: ProductDetailFragment<ProductViewModel>
        fragment = if (showOffer) OfferDetailFragment.newInstance(
            offer
        ) as ProductDetailFragment<ProductViewModel>
        else NeedDetailFragment() as ProductDetailFragment<ProductViewModel>

        submitButton.setOnClickListener {
            submitButton.isEnabled = false
            GlobalScope.launch {
                try {
                    val result: BaseProduct?
                    if (showOffer) {
                        result = TradingApiClient.putOffer(fragment.getProduct() as Offer, this@SubmitProductActivity)
                    }
                    else {
                        result = TradingApiClient.putNeed(fragment.getProduct() as Need, this@SubmitProductActivity)
                    }
                    if (result != null)
                        runOnUiThread { finish() }
                    else
                        runOnUiThread {
                            Toast.makeText(this@SubmitProductActivity, R.string.input_error, Toast.LENGTH_SHORT).show()
                            submitButton.isEnabled = true
                        }
                } catch (e: Exception) {
                    Log.e(TAG, "Error editing or adding offer: ", e)
                    runOnUiThread {
                        Toast.makeText(this@SubmitProductActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show()
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
        private const val TAG = "SubmitOfferActivity"
        @JvmStatic
        fun start(context: Context, offer: Offer?, showOffer: Boolean) {
            val intent = Intent(context, SubmitProductActivity::class.java)
            if (offer != null)
                intent.putExtra(ARG_OFFER, offer)
                intent.putExtra(ARG_SHOW_OFFER, showOffer)
            startActivity(context, intent, null)
        }
    }
}
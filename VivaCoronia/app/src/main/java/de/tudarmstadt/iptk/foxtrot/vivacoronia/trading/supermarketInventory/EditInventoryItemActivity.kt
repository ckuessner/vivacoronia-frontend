package de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.supermarketInventory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.TradingApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.InventoryItem
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception

private const val ARG_ITEM = "item"
private const val ARG_NEW = "new"
private const val ARG_NEW_SUPERMARKET = "newSupermarket"

class EditInventoryItemActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_inventory)

        val passedItem: InventoryItem = intent.getParcelableExtra(ARG_ITEM)!!
        val newItem: Boolean = intent.getBooleanExtra(ARG_NEW, false)
        val newSupermarket: Boolean = intent.getBooleanExtra(ARG_NEW_SUPERMARKET, false)

        val submitButton = findViewById<Button>(R.id.submitInventoryItem)
        submitButton.text = resources.getString(R.string.save)
        title = "Edit Supermarket Inventory Item"
        val fragment = SupermarketInventoryEditFragment.newInstance(passedItem, newItem, newSupermarket)

        submitButton.setOnClickListener {
            GlobalScope.launch {
                try {
                    runOnUiThread{
                        val fragmentReturn = fragment.getItem() ?: return@runOnUiThread
                        submitButton.isEnabled = false
                        val item = fragmentReturn[0] as InventoryItem
                        val localNewItem = fragmentReturn[1] as Boolean
                        val localNewSupermarket = fragmentReturn[2] as Boolean
                        TradingApiClient.putInventoryItem(item, localNewItem, localNewSupermarket, item.availability, this@EditInventoryItemActivity)
                        finish()
                    }
                }
                catch (e: Exception) {
                    Log.e(TAG, "Error editing or adding inventory item: ", e)
                    runOnUiThread {
                        Toast.makeText(this@EditInventoryItemActivity, R.string.unknown_error, Toast.LENGTH_SHORT).show()
                        submitButton.isEnabled = true
                    }
                }
            }
        }

        supportFragmentManager.beginTransaction().replace(R.id.inventory_detail_container, fragment).commit()
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

    companion object  {
        private const val TAG = "EditItemActivity"
        @JvmStatic
        fun start(context: Context, inventoryItem: InventoryItem?, newItem: Boolean, newSupermarket: Boolean) {
            val intent = Intent(context, EditInventoryItemActivity::class.java)
            if(inventoryItem != null){
                intent.putExtra(ARG_ITEM, inventoryItem)
            }
            intent.putExtra(ARG_NEW, newItem)
            intent.putExtra(ARG_NEW_SUPERMARKET, newSupermarket)
            startActivity(context, intent, null)
        }
    }
}
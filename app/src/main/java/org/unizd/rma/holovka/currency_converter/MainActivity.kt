package org.unizd.rma.holovka.currency_converter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var btnShowDetails: Button
    private lateinit var btnConvert: Button
    private lateinit var etAmount: EditText
    private lateinit var spinnerBaseCurrency: Spinner
    private lateinit var spinnerTargetCurrency: Spinner
    private lateinit var tvConvertedAmount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_converter)

        btnShowDetails = findViewById(R.id.btnShowDetails)
        btnConvert = findViewById(R.id.btnConvert)
        etAmount = findViewById(R.id.etAmount)
        spinnerBaseCurrency = findViewById(R.id.spinnerBaseCurrency)
        spinnerTargetCurrency = findViewById(R.id.spinnerTargetCurrency)
        tvConvertedAmount = findViewById(R.id.tvConvertedAmount)

        val btnBack: Button = findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        btnShowDetails.setOnClickListener {
            val baseCurrency = spinnerBaseCurrency.selectedItem.toString()
            val targetCurrency = spinnerTargetCurrency.selectedItem.toString()

            val intent = Intent(this, CurrencyDetailActivity::class.java).apply {
                putExtra("BASE_CURRENCY", baseCurrency)
                putExtra("TARGET_CURRENCY", targetCurrency)
            }

            startActivity(intent)
        }

        fetchSupportedCurrencies()

        btnConvert.setOnClickListener {
            val amount = etAmount.text.toString().toDoubleOrNull()
            if (amount == null) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val baseCurrency = spinnerBaseCurrency.selectedItem.toString()
            val targetCurrency = spinnerTargetCurrency.selectedItem.toString()
            convertCurrency(amount, baseCurrency, targetCurrency)
        }
    }

    private fun fetchSupportedCurrencies() {
        val apiKey = "cur_live_3Z3xTyqRKyXnjRRwHOjhyq4yYTLAOpHNtzvVgTSa"  // Replace with your actual API key
        val apiUrl = "https://api.currencyapi.com/v3/currencies?apikey=$apiKey"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("apikey", apiKey)

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonData = JSONObject(response)
                    val currencyData = jsonData.getJSONObject("data")

                    val currencies = mutableListOf<String>()
                    val iterator = currencyData.keys()
                    while (iterator.hasNext()) {
                        val key = iterator.next()
                        currencies.add(key)
                    }

                    currencies.sort()

                    runOnUiThread {
                        val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, currencies)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerBaseCurrency.adapter = adapter
                        spinnerTargetCurrency.adapter = adapter
                    }
                } else {
                    Log.e("MainActivity", "HTTP error: $responseCode")
                }
            } catch (e: IOException) {
                Log.e("MainActivity", "Network error: ${e.message}")
            }
        }
    }

    private fun convertCurrency(amount: Double, baseCurrency: String, targetCurrency: String) {
        val apiKey = "cur_live_3Z3xTyqRKyXnjRRwHOjhyq4yYTLAOpHNtzvVgTSa"  // Replace with your actual API key
        val apiUrl = "https://api.currencyapi.com/v3/latest?apikey=$apiKey&base_currency=$baseCurrency"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("apikey", apiKey)

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonData = JSONObject(response)
                    val rates = jsonData.getJSONObject("data")
                    val conversionRate = rates.getJSONObject(targetCurrency).getDouble("value")
                    val convertedAmount = conversionRate * amount

                    runOnUiThread {
                        tvConvertedAmount.text = getString(R.string.converted_amount, convertedAmount, targetCurrency)
                    }
                } else {
                    Log.e("MainActivity", "HTTP error: $responseCode")
                }
            } catch (e: IOException) {
                Log.e("MainActivity", "Network error: ${e.message}")
            }
        }
    }
}

package org.unizd.rma.holovka.currency_converter

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class CurrencyDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_detail)

        val baseCurrency = intent.getStringExtra("BASE_CURRENCY")
        val targetCurrency = intent.getStringExtra("TARGET_CURRENCY")

        fetchAndDisplayCurrencyDetails(baseCurrency, targetCurrency)

        val btnBackToMain: Button = findViewById(R.id.btnBackToMain)
        btnBackToMain.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun fetchAndDisplayCurrencyDetails(baseCurrency: String?, targetCurrency: String?) {
        if (baseCurrency != null && targetCurrency != null) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val apiKey = "cur_live_3Z3xTyqRKyXnjRRwHOjhyq4yYTLAOpHNtzvVgTSa"
                    val apiUrl = "https://api.currencyapi.com/v3/currencies?apikey=$apiKey"

                    val url = URL(apiUrl)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("apikey", apiKey)

                    val responseCode = connection.responseCode
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val jsonData = JSONObject(response)
                        val currencyData = jsonData.getJSONObject("data")

                        val baseCurrencyDetail = currencyData.getJSONObject(baseCurrency)
                        val targetCurrencyDetail = currencyData.getJSONObject(targetCurrency)

                        runOnUiThread {
                            updateTextViews(baseCurrencyDetail, "Base")
                            updateTextViews(targetCurrencyDetail, "Target")
                        }
                    } else {
                        println("HTTP error: $responseCode")
                    }
                } catch (e: IOException) {
                    println("Network error: ${e.message}")
                }
            }
        }
    }


private fun updateTextViews(currencyDetail: JSONObject, labelPrefix: String) {
        val tvSymbol: TextView = findViewById(R.id.tvSymbol)
        val tvName: TextView = findViewById(R.id.tvName)
        val tvSymbolNative: TextView = findViewById(R.id.tvSymbolNative)
        val tvDecimalDigits: TextView = findViewById(R.id.tvDecimalDigits)
        val tvRounding: TextView = findViewById(R.id.tvRounding)
        val tvCode: TextView = findViewById(R.id.tvCode)
        val tvNamePlural: TextView = findViewById(R.id.tvNamePlural)
        val tvType: TextView = findViewById(R.id.tvType)
        val tvCountries: TextView = findViewById(R.id.tvCountries)

        tvSymbol.text = "$labelPrefix Symbol: ${currencyDetail.getString("symbol")}"
        tvName.text = "$labelPrefix Name: ${currencyDetail.getString("name")}"
        tvSymbolNative.text = "$labelPrefix Symbol Native: ${currencyDetail.getString("symbol_native")}"
        tvDecimalDigits.text = "$labelPrefix Decimal Digits: ${currencyDetail.getInt("decimal_digits")}"
        tvRounding.text = "$labelPrefix Rounding: ${currencyDetail.getInt("rounding")}"
        tvCode.text = "$labelPrefix Code: ${currencyDetail.getString("code")}"
        tvNamePlural.text = "$labelPrefix Name Plural: ${currencyDetail.getString("name_plural")}"
        tvType.text = "$labelPrefix Type: ${currencyDetail.getString("type")}"
        val countriesArray = currencyDetail.getJSONArray("countries")
        val countriesList = (0 until countriesArray.length()).map { countriesArray.getString(it) }
        tvCountries.text = "$labelPrefix Countries: ${countriesList.joinToString()}"
    }

}

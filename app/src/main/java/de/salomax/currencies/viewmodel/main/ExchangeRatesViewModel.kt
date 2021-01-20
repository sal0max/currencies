package de.salomax.currencies.viewmodel.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import de.salomax.currencies.model.ExchangeRates
import de.salomax.currencies.model.Rate
import de.salomax.currencies.repository.Database
import de.salomax.currencies.repository.ExchangeRatesRepository
import java.time.*

class ExchangeRatesViewModel(application: Application) : AndroidViewModel(application) {

    private var repository: ExchangeRatesRepository = ExchangeRatesRepository(application)

    private var dbLiveItems: LiveData<ExchangeRates?>
    private val liveError = repository.getError()

    /*
     * items =======================================================================================
     */

    init {
        // only update if data is old: https://www.ecb.europa.eu/stats/policy_and_exchange_rates/euro_reference_exchange_rates/html/index.en.html
        // "The reference rates are usually updated around 16:00 CET on every working day, except on
        // TARGET closing days. They are based on a regular daily concertation procedure between
        // central banks across Europe, which normally takes place at 14:15 CET."
        val currentTime = LocalDateTime.now(ZoneId.of("CET"))
        val cachedDate = Database.getInstance(application).getDate()
        dbLiveItems =
            when {
                // first run: fetch data
                cachedDate == null -> repository.getExchangeRates()
                // also fetch if stored date is before the current date (only on weekdays)...
                cachedDate
                    .plusDays(1)
                    .atTime(15, 59)
                    .isBefore(currentTime) -> repository.getExchangeRates()
                // else just use the cached value
                else -> Database.getInstance(application).getExchangeRates()
            }
    }

    fun getExchangeRate(): LiveData<ExchangeRates?> {
        val liveItems = MediatorLiveData<ExchangeRates?>()
        liveItems.addSource(dbLiveItems) { exchangeRates ->
            exchangeRates?.let {
                liveItems.value = ExchangeRates(
                    exchangeRates.base,
                    exchangeRates.date,
                    exchangeRates.rates.toMutableList().apply {
                        // pegged to USD
                        it.rates.find { it.code == "USD" }?.value?.let { usd ->
                            // middle east
                            add(Rate("AED", usd * 3.6725f))    // United Arab Emirates dirham
                            add(Rate("BHD", usd * 0.376f))     // Bahraini dinar
                            add(Rate("JOD", usd * 0.709f))     // Jordanian dinar
                            add(Rate("LBP", usd * 1507.5f))    // Lebanese pound
                            add(Rate("OMR", usd * 2.6008f))    // Omani rial
                            add(Rate("QAR", usd * 3.64f))      // Qatari riyal
                            add(Rate("SAR", usd * 3.75f))      // Saudi riyal
                            // caribbean
                            add(Rate("AWG", usd * 1.79f))      // Aruban florin
                            add(Rate("BBD", usd * 2f))         // Barbadian dollar
                            add(Rate("BSD", usd))              // Bahamian dollar
                            add(Rate("BZD", usd * 1.97f))      // Belize dollar
                            add(Rate("CUC", usd * 1f))         // Cuban convertible peso
                            add(Rate("XCD", usd * 2.7f))       // Eastern Caribbean dollar (Antigua and Barbuda/Dominica/Grenada/Saint Kitts and Nevis/Saint Lucia/and Saint Vincent and the Grenadines/Anguilla/Montserrat)
                        }
                        // pegged to EUR
                        it.rates.find { it.code == "EUR" }?.value?.let { eur ->
                            add(Rate("BAM", eur * 1.95583f))   // Bosnia and Herzegovina convertible mark
                            //add(Rate("XAF", eur * 655.957f)) // Central African CFA franc (Cameroon/Central African Republic/Chad/Republic of the Congo/Equatorial Guinea/Gabon)
                            //add(Rate("XOF", eur * 655.957f)) // West African CFA franc (Benin/Burkina Faso/Côte d'Ivoire/Guinea-Bissau/Mali/Niger/Senegal/Togo)
                        }
                        // pegged to DKK
                        it.rates.find { it.code == "DKK" }?.value?.let { dkk ->
                            add(Rate("FOK", dkk))              // Faroese króna (same as Danish krone)
                        }
                        // pegged to INR
                        it.rates.find { it.code == "INR" }?.value?.let { inr ->
                            add(Rate("NPR", inr * 1.6f))       // Nepalese rupee
                        }
                        // pegged to ZAR
                        it.rates.find { it.code == "ZAR" }?.value?.let { zar ->
                            add(Rate("NAD", zar))              // Namibian dollar
                        }
                    }.sortedBy { rate -> rate.code }.toList()
                )
            }
        }
        return liveItems
    }

    /*
     * error =======================================================================================
     */

    fun getError(): LiveData<String?> {
        return liveError
    }

}

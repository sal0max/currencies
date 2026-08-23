package de.salomax.currencies.model.adapter

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.salomax.currencies.model.ApiProvider
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.Rate
import de.salomax.currencies.model.Timeline
import de.salomax.currencies.model.provider.NationalBankKazakhstan
import java.io.InputStream
import java.time.LocalDate

class NationalBankKazakhstanTimelineAdapter(
    private val base: Currency,
    private val symbol: Currency
) {

    fun parse(inputStream: InputStream): Timeline {
        val html = inputStream.bufferedReader().use { it.readText() }
        val json = html.substringAfter(JSON_PREFIX, "").substringBefore(JSON_SUFFIX, "")
        val graph = if (json.isEmpty()) null else JSON_ADAPTER.fromJson(json)?.rates
        val datasets = graph?.datasets?.associateBy { it.label }.orEmpty()
        val rates = sortedMapOf<LocalDate, Rate>()

        graph?.labels?.forEachIndexed { index, dateText ->
            val baseValue = normalizedValue(base, datasets, index)
            val symbolValue = normalizedValue(symbol, datasets, index)
            if (baseValue != null && symbolValue != null) {
                NationalBankKazakhstanRateConverter.cross(baseValue, symbolValue)?.let {
                    rates[LocalDate.parse(dateText)] = Rate(symbol, it)
                }
            }
        }

        return Timeline(
            success = rates.isNotEmpty(),
            error = if (rates.isEmpty()) "No data found." else null,
            base = base.iso4217Alpha(),
            startDate = rates.keys.firstOrNull(),
            endDate = rates.keys.lastOrNull(),
            rates = rates,
            provider = ApiProvider.NATIONAL_BANK_KAZAKHSTAN
        )
    }

    private fun normalizedValue(
        requestedCurrency: Currency,
        datasets: Map<String, NationalBankKazakhstanGraphDataset>,
        index: Int
    ): Float? {
        if (requestedCurrency == Currency.KZT) return 1f
        val parameterCurrency = if (requestedCurrency == Currency.FOK) Currency.DKK else requestedCurrency
        val metadata = NationalBankKazakhstan.currencyMetadata[parameterCurrency] ?: return null
        val rawValue = datasets[parameterCurrency.iso4217Alpha()]?.data?.getOrNull(index) ?: return null
        return NationalBankKazakhstanRateConverter.normalize(metadata.quantity, rawValue)
    }

    companion object {
        private const val JSON_PREFIX = "window[\"sharedData\"]="
        private const val JSON_SUFFIX = ";window[\"sharedDataNamespace\"]"
        private val JSON_ADAPTER = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
            .adapter(NationalBankKazakhstanGraphPage::class.java)
    }
}

internal object NationalBankKazakhstanRateConverter {
    fun normalize(quantity: Float, quotedValue: Float): Float? =
        if (quotedValue == 0f) null else quantity / quotedValue

    fun cross(baseValue: Float, symbolValue: Float): Float? =
        if (baseValue == 0f) null else symbolValue / baseValue
}

@JsonClass(generateAdapter = true)
internal data class NationalBankKazakhstanGraphPage(
    val rates: NationalBankKazakhstanGraphRates?
)

@JsonClass(generateAdapter = true)
internal data class NationalBankKazakhstanGraphRates(
    val labels: List<String>,
    val datasets: List<NationalBankKazakhstanGraphDataset>
)

@JsonClass(generateAdapter = true)
internal data class NationalBankKazakhstanGraphDataset(
    val label: String,
    val data: List<Float?>
)

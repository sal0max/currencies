package de.salomax.currencies.viewmodel.timeline

import android.app.Application
import android.text.Spanned
import android.text.SpannedString
import androidx.core.text.HtmlCompat
import androidx.lifecycle.*
import de.salomax.currencies.model.Timeline
import de.salomax.currencies.repository.ExchangeRatesRepository
import androidx.lifecycle.ViewModel

import androidx.lifecycle.ViewModelProvider
import de.salomax.currencies.R
import de.salomax.currencies.model.Currency
import de.salomax.currencies.model.Rate
import java.time.LocalDate

class TimelineViewModel(
    private val app: Application,
    private var base: Currency,
    private var target: Currency
) : AndroidViewModel(app) {

    class Factory(
        private val mApplication: Application,
        private val base: Currency,
        private val target: Currency
    ) :
        ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TimelineViewModel(mApplication, base, target) as T
        }
    }

    enum class Period {
        WEEK, MONTH, YEAR
    }

    private var repository: ExchangeRatesRepository = ExchangeRatesRepository(app)

    // week/month/year
    private val periodLiveData = MutableLiveData(Period.YEAR)
    // currently selected date
    private val scrubDateLiveData = MutableLiveData<LocalDate?>()
    // error
    private val errorLiveData = repository.getError()
    // updating
    private var isUpdating = repository.isUpdating()

    private val dbLiveItems: LiveData<Timeline?> by lazy {
        MediatorLiveData<Timeline?>().apply {
            var timeline: Timeline? = null
            var startDate: LocalDate? = null

            fun update() {
                this.value = timeline?.copy(
                    startDate = startDate,
                    rates = timeline?.rates?.filter { entries ->
                        !entries.key.isBefore(startDate)
                    }
                )
            }

            // 1y timeline data - always call api - hard to find a decent caching strategy
            addSource(repository.getTimeline(base, target)) {
                timeline = it
                update()
            }

            // selected time period
            addSource(periodLiveData) {
                startDate = when (it) {
                    Period.WEEK -> LocalDate.now().minusDays(7)
                    Period.MONTH -> LocalDate.now().minusMonths(1)
                    else -> LocalDate.now().minusYears(1)
                }
                update()
            }
        }
    }

    /*
     * getters for the various values ==============================================================
     */

    fun getTitle(): LiveData<Spanned> {
        return Transformations.map(dbLiveItems) {
            if (it == null)
                SpannedString("")
            else
                HtmlCompat.fromHtml(
                    app.getString(
                        R.string.activity_timeline_title,
                        base.iso4217Alpha(),
                        target.iso4217Alpha()
                    ),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
        }
    }

    fun toggleCurrencies() {
        val tmp = base
        base = target
        target = tmp
        // call the api -- timeline live data is auto-updated everywhere where it is used
        repository.getTimeline(base, target)
    }

    fun getProvider(): LiveData<CharSequence?> {
        return Transformations.map(dbLiveItems) {
            it?.provider?.getName(getApplication())
        }
    }

    fun getRates(): LiveData<Map<LocalDate, Rate>?> {
        return Transformations.map(dbLiveItems) {
            it?.rates
        }
    }

    fun getRateCurrent(): LiveData<Map.Entry<LocalDate, Rate?>?> {
        return Transformations.map(dbLiveItems) {
            it?.rates?.entries?.last()
        }
    }

    fun getRatePast(): LiveData<Map.Entry<LocalDate, Rate?>?> {
        return MediatorLiveData<Map.Entry<LocalDate, Rate?>?>().apply {
            var date: LocalDate? = null
            var rates: Set<Map.Entry<LocalDate, Rate?>>? = null

            fun update() {
                this.value = if (date != null)
                    rates?.find { it.key == date }
                else
                    rates?.first()
            }

            addSource(dbLiveItems) {
                rates = it?.rates?.entries
                update()
            }

            addSource(scrubDateLiveData) {
                date = it
                update()
            }
        }
    }

    fun getRatesDifferencePercent(): LiveData<Float?> {
        return MediatorLiveData<Float?>().apply {
            var scrubDate: LocalDate? = null
            var rates: Set<Map.Entry<LocalDate, Rate?>>? = null

            fun update() {
                val past =  if (scrubDate != null)
                    rates?.find { it.key == scrubDate }?.value
                else
                    rates?.first()?.value
                val current = rates?.last()?.value

                val ratePast = past?.value
                val rateCurrent = current?.value
                this.value = if (ratePast != null && rateCurrent != null) {
                    val percentage = (rateCurrent - ratePast) / rateCurrent * 100
                    if (percentage.isFinite()) percentage else null
                } else {
                    null
                }
            }

            addSource(dbLiveItems) {
                rates = it?.rates?.entries
                update()
            }

            addSource(scrubDateLiveData) {
                scrubDate = it
                update()
            }
        }
    }

    fun getRatesAverage(): LiveData<Rate?> {
        return MediatorLiveData<Rate?>().apply {
            var scrubDate: LocalDate? = null
            var rates: Set<Map.Entry<LocalDate, Rate?>>? = null

            fun update() {
                this.value = rates
                    ?.filter { map ->
                        scrubDate?.let { !map.key.isBefore(it) } ?: true
                    }
                    ?.map { entry ->
                        entry.value
                    }
                    ?.map { rate -> rate?.value ?: 0f }
                    ?.average()
                    ?.let { average -> Rate(target, average.toFloat()) }
            }

            addSource(dbLiveItems) {
                rates = it?.rates?.entries
                update()
            }

            addSource(scrubDateLiveData) {
                scrubDate = it
                update()
            }
        }
    }

    fun getRatesMin(): LiveData<Pair<Rate?, LocalDate?>> {
        return MediatorLiveData<Pair<Rate?, LocalDate?>>().apply {
            var scrubDate: LocalDate? = null
            var rates: Set<Map.Entry<LocalDate, Rate?>>? = null

            fun update() {
                val min = rates
                    ?.filter { map ->
                        scrubDate?.let { !map.key.isBefore(it) } ?: true
                    }
                    ?.map { entry ->
                        entry.value
                    }
                    ?.map { rate -> rate?.value ?: 0f }
                    ?.minOrNull()
                    ?.let { min -> Rate(target, min) }
                val date = rates
                    ?.filter { map ->
                        scrubDate?.let { !map.key.isBefore(it) } ?: true
                    }
                    ?.findLast { entry -> entry.value
                        ?.value == min?.value
                    }
                    ?.key
                this.value = Pair(min, date)
            }

            addSource(dbLiveItems) {
                rates = it?.rates?.entries
                update()
            }

            addSource(scrubDateLiveData) {
                scrubDate = it
                update()
            }
        }
    }

    fun getRatesMax(): LiveData<Pair<Rate?, LocalDate?>> {
        return MediatorLiveData<Pair<Rate?, LocalDate?>>().apply {
            var scrubDate: LocalDate? = null
            var rates: Set<Map.Entry<LocalDate, Rate?>>? = null

            fun update() {
                val max = rates
                    ?.filter { map ->
                        scrubDate?.let { !map.key.isBefore(it) } ?: true
                    }
                    ?.map { entry ->
                        entry.value
                    }
                    ?.map { rate -> rate?.value ?: 0f }
                    ?.maxOrNull()
                    ?.let { max -> Rate(target, max) }
                val date = rates
                    ?.filter { map ->
                        scrubDate?.let { !map.key.isBefore(it) } ?: true
                    }
                    ?.findLast { entry -> entry.value
                        ?.value == max?.value
                    }
                    ?.key
                this.value = Pair(max, date)
            }

            addSource(dbLiveItems) {
                rates = it?.rates?.entries
                update()
            }

            addSource(scrubDateLiveData) {
                scrubDate = it
                update()
            }
        }
    }

    fun setTimePeriod(period: Period) {
        periodLiveData.postValue(period)
    }

    fun setPastDate(date: LocalDate?) {
        scrubDateLiveData.postValue(date)
    }

    /*
     * error =======================================================================================
     */

    fun getError(): LiveData<String?> = errorLiveData

    fun isUpdating(): LiveData<Boolean> = isUpdating

}

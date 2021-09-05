package de.salomax.currencies.viewmodel.timeline

import android.app.Application
import androidx.lifecycle.*
import de.salomax.currencies.model.Timeline
import de.salomax.currencies.repository.ExchangeRatesRepository
import androidx.lifecycle.ViewModel

import androidx.lifecycle.ViewModelProvider
import de.salomax.currencies.model.Rate
import de.salomax.currencies.repository.Database
import java.time.LocalDate
import java.time.ZoneId

class TimelineViewModel(
    ctx: Application,
    private val base: String,
    private val symbol: String
) : AndroidViewModel(ctx) {

    class Factory(
        private val mApplication: Application,
        private val base: String,
        private val symbol: String
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TimelineViewModel(mApplication, base, symbol) as T
        }
    }

    enum class Period {
        WEEK, MONTH, YEAR
    }

    private var repository: ExchangeRatesRepository = ExchangeRatesRepository(ctx)

    private val periodLiveData: MutableLiveData<Period> by lazy {
        MutableLiveData<Period>(Period.YEAR)
    }

    private val scrubDateLiveData: MutableLiveData<LocalDate?> by lazy {
        MutableLiveData<LocalDate?>(null)
    }

    private val dbLiveItems: LiveData<Timeline?> by lazy {
        val cachedDate = Database.getInstance(ctx).getTimelineAge(base, symbol)
        val currentDate = LocalDate.now(ZoneId.of("UTC"))

        MediatorLiveData<Timeline?>().apply {
            var timeline: Timeline? = null
            var startDate: LocalDate? = null

            // cache, if data is missing or outdated
            if (cachedDate == null || cachedDate.isBefore(currentDate))
                repository.getTimeline(base, symbol)

            fun update() {
                this.value = timeline?.copy(
                    startDate = startDate,
                    rates = timeline?.rates?.filter { entries ->
                        !entries.key.isBefore(startDate)
                    }
                )
            }

            // 1y timeline data (from cache)
            addSource(Database.getInstance(ctx).getTimeline(base, symbol)) {
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

    private val liveError = repository.getError()
    private var isUpdating: LiveData<Boolean> = repository.isUpdating()

    /*
     * getters for the various values ==============================================================
     */

    fun getRates(): LiveData<Map<LocalDate, Rate?>?> {
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
                    ?.let { average -> Rate(symbol, average.toFloat()) }
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
                    ?.let { min -> Rate(symbol, min) }
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
                    ?.let { max -> Rate(symbol, max) }
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

    fun getError(): LiveData<String?> = liveError

    fun isUpdating(): LiveData<Boolean> = isUpdating

}

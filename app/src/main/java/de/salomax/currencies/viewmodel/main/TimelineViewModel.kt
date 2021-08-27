package de.salomax.currencies.viewmodel.main

import android.app.Application
import androidx.lifecycle.*
import de.salomax.currencies.model.Timeline
import de.salomax.currencies.repository.ExchangeRatesRepository
import androidx.lifecycle.ViewModel

import androidx.lifecycle.ViewModelProvider
import de.salomax.currencies.model.Rate
import java.time.LocalDate

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

            // 1y timeline data
            addSource(repository.getTimeline(base, symbol)) {
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
     * items =======================================================================================
     */

    fun getRates(): LiveData<Map<LocalDate, List<Rate>>?> {
        return Transformations.map(dbLiveItems) {
            it?.rates
        }
    }

    fun getRateCurrent(): LiveData<Map.Entry<LocalDate, List<Rate>>?> {
        return Transformations.map(dbLiveItems) {
            it?.rates?.entries?.last()
        }
    }

    fun getRatePast(): LiveData<Map.Entry<LocalDate, List<Rate>>?> {
        return MediatorLiveData<Map.Entry<LocalDate, List<Rate>>?>().apply {
            var date: LocalDate? = null
            var rates: Set<Map.Entry<LocalDate, List<Rate>>>? = null

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
            var rates: Set<Map.Entry<LocalDate, List<Rate>>>? = null

            fun update() {
                val past =  if (scrubDate != null)
                    rates?.find { it.key == scrubDate }?.value?.find { x -> x.code == symbol }
                else
                    rates?.first()?.value?.find { x -> x.code == symbol }
                val current = rates?.last()?.value?.find { x -> x.code == symbol }

                val ratePast = past?.value
                val rateCurrent = current?.value
                this.value = if (ratePast != null && rateCurrent != null) {
                    (rateCurrent - ratePast) / rateCurrent * 100
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
        // TODO ?
        return Transformations.map(dbLiveItems) {
            it
                ?.rates
                ?.entries
                ?.map { entry -> entry.value
                    .find { x -> x.code == symbol }
                }
                ?.map { rate -> rate?.value ?: 0f }
                ?.average()
                ?.let { average -> Rate(symbol, average.toFloat()) }
        }
    }

    fun getRatesMin(): LiveData<Pair<Rate?, LocalDate?>> {
        // TODO ?
        return Transformations.map(dbLiveItems) {
            val min = it
                ?.rates
                ?.entries
                ?.map { entry -> entry.value
                    .find { x -> x.code == symbol }
                }
                ?.map { rate -> rate?.value ?: 0f }
                ?.minOrNull()
                ?.let { min -> Rate(symbol, min) }
            val date = it
                ?.rates
                ?.entries
                ?.findLast { entry -> entry.value
                    .find { x -> x.code == symbol }
                    ?.value == min?.value
                }
                ?.key
            Pair(min, date)
        }
    }

    fun getRatesMax(): LiveData<Pair<Rate?, LocalDate?>> {
        // TODO ?
        return Transformations.map(dbLiveItems) {
            val max = it
                ?.rates
                ?.entries
                ?.map { entry -> entry.value
                    .find { x -> x.code == symbol }
                }
                ?.map { rate -> rate?.value ?: 0f }
                ?.maxOrNull()
                ?.let { max -> Rate(symbol, max) }
            val date =  it
                ?.rates
                ?.entries
                ?.findLast { entry -> entry.value
                    .find { x -> x.code == symbol }
                    ?.value == max?.value
                }
                ?.key
            Pair(max, date)
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

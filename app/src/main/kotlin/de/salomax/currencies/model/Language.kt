package de.salomax.currencies.model

import android.content.Context
import de.salomax.currencies.R

enum class Language(
    val iso: String,
    private val nameNative: String?,
    private val nameLocalized: Int
) {
    SYSTEM("system", null, R.string.system_default),
    IN("in", "Bahasa Indonesia", R.string.language_in),
    CA("ca", "Català", R.string.language_ca),
    CS("cs", "Čeština", R.string.language_cs),
    DE("de", "Deutsch", R.string.language_de),
    EN("en", "English", R.string.language_en),
    ES("es", "Español", R.string.language_es),
    EL("el", "Ελληνικά", R.string.language_el),
    FR("fr", "Français", R.string.language_fr),
    HR("hr", "Hrvatski", R.string.language_hr),
    IS("is", "Íslenska", R.string.language_is),
    IT("it", "Italiano", R.string.language_it),
    HU("hu", "Magyar", R.string.language_hu),
    NL("nl", "Nederlands", R.string.language_nl),
    NB("nb", "Norsk", R.string.language_nb),
    PL("pl", "Polski", R.string.language_pl),
    PT_BR("pt_BR", "Português (Brasil)", R.string.language_pt_BR),
    SV("sv", "Svenska", R.string.language_sv),
    VI("vi", "Tiếng Việt", R.string.language_vi),
    TR("tr", "Türkçe", R.string.language_tr),
    RU("ru", "Русский", R.string.language_ru),
    UK("uk", "Українська", R.string.language_uk),
    BG("bg", "Български", R.string.language_bg),
    BN("bn", "বাংলা", R.string.language_bn),
    ZH_CN("zh_CN", "简体中文", R.string.language_zh_CN),
    AR("ar", "اَلْعَرَبِيَّة", R.string.language_ar),
    IW("iw", "עִבְרִית", R.string.language_iw);

    companion object {
        private val isoMapping: Map<String, Language> = values().associateBy(Language::iso)
        fun byIso(isoValue: String?): Language? =
            // direct match: e.g. de <-> de or e.g. pt_BR <-> pt_BR
            isoMapping[isoValue]
            // either the resource string has no country, or the given locale has none:
            // use only language without country
                ?: isoMapping.mapKeys { it.key.substringBefore("_") }[isoValue?.substringBefore("_")]
    }

    fun nativeName(context: Context): String = when (this) {
        SYSTEM -> context.getString(R.string.system_default)
        else -> this.nameNative as String
    }

    fun localizedName(context: Context): String =
        this.nameLocalized.let { context.getString(it) }

}

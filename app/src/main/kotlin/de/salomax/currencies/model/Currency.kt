package de.salomax.currencies.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.squareup.moshi.JsonClass
import de.salomax.currencies.R

@Suppress("unused")
@JsonClass(generateAdapter = false) // see https://stackoverflow.com/a/64085370/421140
enum class Currency(
    private val iso4217Alpha: String,  // USD
    private val iso4217Numeric: Int?,  // 840
    private val symbol: String?,       // $
    private val fullName: Int,         // US dollar
    private val flag: Int?             // vector drawable: star-spangled banner
) {
    AED("AED", 784,  "د.إ",  R.string.name_aed, R.drawable.flag_ae),
    AFN("AFN", 971,  "؋",    R.string.name_afn, R.drawable.flag_af),
    ALL("ALL", 8,    "L",    R.string.name_all, R.drawable.flag_al),
    AMD("AMD", 51,   "֏",    R.string.name_amd, R.drawable.flag_am),
    ANG("ANG", 532,  "ƒ",    R.string.name_ang, R.drawable.flag_nl), // Dutch flag: on 10 October 2010, the Netherlands Antilles was dissolved into Curaçao, Sint Maarten and the three public bodies of the Caribbean Netherlands.
    AOA("AOA", 973,  "Kz",   R.string.name_aoa, R.drawable.flag_ao),
    ARS("ARS", 32,   "$",    R.string.name_ars, R.drawable.flag_ar),
    AUD("AUD", 36,   "$",    R.string.name_aud, R.drawable.flag_au),
    AWG("AWG", 533,  "Afl.", R.string.name_awg, R.drawable.flag_aw),
    AZN("AZN", 944,  "₼",    R.string.name_azn, R.drawable.flag_az),
    BAM("BAM", 977,  "KM",   R.string.name_bam, R.drawable.flag_ba),
    BBD("BBD", 52,   "$",    R.string.name_bbd, R.drawable.flag_bb),
    BDT("BDT", 50,   "৳",    R.string.name_bdt, R.drawable.flag_bd),
    BGN("BGN", 975,  "лв",   R.string.name_bgn, R.drawable.flag_bg),
    BHD("BHD", 48,   ".د.ب", R.string.name_bhd, R.drawable.flag_bh),
    BIF("BIF", 108,  "Fr",   R.string.name_bif, R.drawable.flag_bi),
    BMD("BMD", 60,   "$",    R.string.name_bmd, R.drawable.flag_bm),
    BND("BND", 96,   "$",    R.string.name_bnd, R.drawable.flag_bn),
    BOB("BOB", 68,   "Bs.",  R.string.name_bob, R.drawable.flag_bo),
    BRL("BRL", 986,  "R$",   R.string.name_brl, R.drawable.flag_br),
    BSD("BSD", 44,   "$",    R.string.name_bsd, R.drawable.flag_bs),
    BTC("BTC", null, "₿",    R.string.name_btc, null),
    BTN("BTN", 64,   "Nu.",  R.string.name_btn, R.drawable.flag_bt),
    BWP("BWP", 72,   "P",    R.string.name_bwp, R.drawable.flag_bw),
    BYN("BYN", 933,  "Br",   R.string.name_byn, R.drawable.flag_by),
    BZD("BZD", 84,   "$",    R.string.name_bzd, R.drawable.flag_bz),
    CAD("CAD", 124,  "$",    R.string.name_cad, R.drawable.flag_ca),
    CDF("CDF", 976,  "Fr",   R.string.name_cdf, R.drawable.flag_cd),
    CHF("CHF", 756,  "Fr.",  R.string.name_chf, R.drawable.flag_ch),
    CLF("CLF", 990,  null,   R.string.name_clf, R.drawable.flag_cl),
    CLP("CLP", 152,  "$",    R.string.name_clp, R.drawable.flag_cl),
    CNH("CNH", null, "¥",    R.string.name_cnh, R.drawable.flag_cn),
    CNY("CNY", 156,  "¥",    R.string.name_cny, R.drawable.flag_cn),
    COP("COP", 170,  "$",    R.string.name_cop, R.drawable.flag_co),
    CRC("CRC", 188,  "₡",    R.string.name_crc, R.drawable.flag_cr),
    CUC("CUC", 931,  "$",    R.string.name_cuc, R.drawable.flag_cu),
    CUP("CUP", 192,  "$",    R.string.name_cup, R.drawable.flag_cu),
    CVE("CVE", 132,  "$",    R.string.name_cve, R.drawable.flag_cv),
    CZK("CZK", 203,  "Kč",   R.string.name_czk, R.drawable.flag_cz),
    DJF("DJF", 262,  "Fr",   R.string.name_djf, R.drawable.flag_dj),
    DKK("DKK", 208,  "kr",   R.string.name_dkk, R.drawable.flag_dk),
    DOP("DOP", 214,  "RD$",  R.string.name_dop, R.drawable.flag_do),
    DZD("DZD", 12,   "د.ج",  R.string.name_dzd, R.drawable.flag_dz),
    EGP("EGP", 818,  "ج.م",  R.string.name_egp, R.drawable.flag_eg),
    ERN("ERN", 232,  "Nfk",  R.string.name_ern, R.drawable.flag_er),
    ETB("ETB", 230,  "Br",   R.string.name_etb, R.drawable.flag_et),
    EUR("EUR", 978,  "€",    R.string.name_eur, R.drawable.flag_eu),
    FJD("FJD", 242,  "$",    R.string.name_fjd, R.drawable.flag_fj),
    FKP("FKP", 238,  "£",    R.string.name_fkp, R.drawable.flag_fk),
    FOK("FOK", null, "kr",   R.string.name_fok, R.drawable.flag_fo),
    GBP("GBP", 826,  "£",    R.string.name_gbp, R.drawable.flag_gb),
    GEL("GEL", 981,  "₾",    R.string.name_gel, R.drawable.flag_ge),
    GGP("GGP", null, "£",    R.string.name_ggp, R.drawable.flag_gg),
    GHS("GHS", 936,  "₵",    R.string.name_ghs, R.drawable.flag_gh),
    GIP("GIP", 292,  "£",    R.string.name_gip, R.drawable.flag_gi),
    GMD("GMD", 270,  "D",    R.string.name_gmd, R.drawable.flag_gm),
    GNF("GNF", 324,  "Fr",   R.string.name_gnf, R.drawable.flag_gn),
    GTQ("GTQ", 320,  "Q",    R.string.name_gtq, R.drawable.flag_gt),
    GYD("GYD", 328,  "$",    R.string.name_gyd, R.drawable.flag_gy),
    HKD("HKD", 344,  "$",    R.string.name_hkd, R.drawable.flag_hk),
    HNL("HNL", 340,  "L",    R.string.name_hnl, R.drawable.flag_hn),
    HRK("HRK", 191,  "kn",   R.string.name_hrk, R.drawable.flag_hr),
    HTG("HTG", 332,  "G",    R.string.name_htg, R.drawable.flag_ht),
    HUF("HUF", 348,  "Ft",   R.string.name_huf, R.drawable.flag_hu),
    IDR("IDR", 360,  "Rp",   R.string.name_idr, R.drawable.flag_id),
    ILS("ILS", 376,  "₪",    R.string.name_ils, R.drawable.flag_il),
    IMP("IMP", null, "£",    R.string.name_imp, R.drawable.flag_im),
    INR("INR", 356,  "₹",    R.string.name_inr, R.drawable.flag_in),
    IQD("IQD", 368,  "ع.د",  R.string.name_iqd, R.drawable.flag_iq),
    IRR("IRR", 364,  "﷼",    R.string.name_irr, R.drawable.flag_ir),
    ISK("ISK", 352,  "kr",   R.string.name_isk, R.drawable.flag_is),
    JEP("JEP", null, "£",    R.string.name_jep, R.drawable.flag_je),
    JMD("JMD", 388,  "$",    R.string.name_jmd, R.drawable.flag_jm),
    JOD("JOD", 400,  "د.أ",  R.string.name_jod, R.drawable.flag_jo),
    JPY("JPY", 392,  "¥",    R.string.name_jpy, R.drawable.flag_jp),
    KES("KES", 404,  "Sh",   R.string.name_kes, R.drawable.flag_ke),
    KGS("KGS", 417,  "С̲",    R.string.name_kgs, R.drawable.flag_kg),
    KHR("KHR", 116,  "៛",    R.string.name_khr, R.drawable.flag_kh),
    KMF("KMF", 174,  "Fr",   R.string.name_kmf, R.drawable.flag_km),
    KPW("KPW", 408,  "₩",    R.string.name_kpw, R.drawable.flag_kp),
    KRW("KRW", 410,  "₩",    R.string.name_krw, R.drawable.flag_kr),
    KWD("KWD", 414,  "د.ك",  R.string.name_kwd, R.drawable.flag_kw),
    KYD("KYD", 136,  "$",    R.string.name_kyd, R.drawable.flag_ky),
    KZT("KZT", 398,  "₸",    R.string.name_kzt, R.drawable.flag_kz),
    LAK("LAK", 418,  "₭",    R.string.name_lak, R.drawable.flag_la),
    LBP("LBP", 422,  "ل.ل.", R.string.name_lbp, R.drawable.flag_lb),
    LKR("LKR", 144,  "Rs",   R.string.name_lkr, R.drawable.flag_lk),
    LRD("LRD", 430,  "$",    R.string.name_lrd, R.drawable.flag_lr),
    LSL("LSL", 426,  "L",    R.string.name_lsl, R.drawable.flag_ls),
    LYD("LYD", 434,  "ل.د",  R.string.name_lyd, R.drawable.flag_ly),
    MAD("MAD", 504,  "د.م.", R.string.name_mad, R.drawable.flag_ma),
    MDL("MDL", 498,  "L",    R.string.name_mdl, R.drawable.flag_md),
    MGA("MGA", 969,  "Ar",   R.string.name_mga, R.drawable.flag_mg),
    MKD("MKD", 807,  "ден",  R.string.name_mkd, R.drawable.flag_mk),
    MMK("MMK", 104,  "Ks",   R.string.name_mmk, R.drawable.flag_mm),
    MNT("MNT", 496,  "₮",    R.string.name_mnt, R.drawable.flag_mn),
    MOP("MOP", 446,  "MOP$", R.string.name_mop, R.drawable.flag_mo),
    MRO("MRO", 478,  "UM",   R.string.name_mro, R.drawable.flag_mr),
    MRU("MRU", 929,  "UM",   R.string.name_mru, R.drawable.flag_mr),
    MUR("MUR", 480,  "₨",    R.string.name_mur, R.drawable.flag_mu),
    MVR("MVR", 462,  ".ރ",   R.string.name_mvr, R.drawable.flag_mv),
    MWK("MWK", 454,  "MK",   R.string.name_mwk, R.drawable.flag_mw),
    MXN("MXN", 484,  "$",    R.string.name_mxn, R.drawable.flag_mx),
    MYR("MYR", 458,  "RM",   R.string.name_myr, R.drawable.flag_my),
    MZN("MZN", 943,  "MT",   R.string.name_mzn, R.drawable.flag_mz),
    NAD("NAD", 516,  "$",    R.string.name_nad, R.drawable.flag_na),
    NGN("NGN", 566,  "₦",    R.string.name_ngn, R.drawable.flag_ng),
    NIO("NIO", 558,  "C$",   R.string.name_nio, R.drawable.flag_ni),
    NOK("NOK", 578,  "kr",   R.string.name_nok, R.drawable.flag_no),
    NPR("NPR", 524,  "रु",    R.string.name_npr, R.drawable.flag_np),
    NZD("NZD", 554,  "$",    R.string.name_nzd, R.drawable.flag_nz),
    OMR("OMR", 512,  "ر.ع.", R.string.name_omr, R.drawable.flag_om),
    PAB("PAB", 590,  "B/.",  R.string.name_pab, R.drawable.flag_pa),
    PEN("PEN", 604,  "S/",   R.string.name_pen, R.drawable.flag_pe),
    PGK("PGK", 598,  "K",    R.string.name_pgk, R.drawable.flag_pg),
    PHP("PHP", 608,  "₱",    R.string.name_php, R.drawable.flag_ph),
    PKR("PKR", 586,  "₨",    R.string.name_pkr, R.drawable.flag_pk),
    PLN("PLN", 985,  "zł",   R.string.name_pln, R.drawable.flag_pl),
    PYG("PYG", 600,  "₲",    R.string.name_pyg, R.drawable.flag_py),
    QAR("QAR", 634,  "ر.ق",  R.string.name_qar, R.drawable.flag_qa),
    RON("RON", 946,  "lei",  R.string.name_ron, R.drawable.flag_ro),
    RSD("RSD", 941,  "дин.", R.string.name_rsd, R.drawable.flag_rs),
    RUB("RUB", 643,  "₽",    R.string.name_rub, R.drawable.flag_ru),
    RWF("RWF", 646,  "Fr",   R.string.name_rwf, R.drawable.flag_rw),
    SAR("SAR", 682,  "ر.س",  R.string.name_sar, R.drawable.flag_sa),
    SBD("SBD", 90, " $",     R.string.name_sbd, R.drawable.flag_sb),
    SCR("SCR", 690,  "₨",    R.string.name_scr, R.drawable.flag_sc),
    SDG("SDG", 938,  "ج.س.", R.string.name_sdg, R.drawable.flag_sd),
    SEK("SEK", 752,  "kr",   R.string.name_sek, R.drawable.flag_se),
    SGD("SGD", 702,  "$",    R.string.name_sgd, R.drawable.flag_sg),
    SHP("SHP", 654,  "£",    R.string.name_shp, R.drawable.flag_sh),
    SLE("SLE", 925,  "Le",   R.string.name_sle, R.drawable.flag_sl),
    SLL("SLL", 694,  "Le",   R.string.name_sll, R.drawable.flag_sl),
    SOS("SOS", 706,  "Sh",   R.string.name_sos, R.drawable.flag_so),
    SRD("SRD", 968,  "$",    R.string.name_srd, R.drawable.flag_sr),
    SSP("SSP", 728,  "£",    R.string.name_ssp, R.drawable.flag_ss),
    STD("STD", 678,  "Db",   R.string.name_std, R.drawable.flag_st),
    STN("STN", 930,  "Db",   R.string.name_stn, R.drawable.flag_st),
    SVC("SVC", 222,  "₡",    R.string.name_svc, R.drawable.flag_sv),
    SYP("SYP", 760,  "ل.س",  R.string.name_syp, R.drawable.flag_sy),
    SZL("SZL", 748,  "L",    R.string.name_szl, R.drawable.flag_sz),
    THB("THB", 764,  "฿",    R.string.name_thb, R.drawable.flag_th),
    TJS("TJS", 972,  "SM",   R.string.name_tjs, R.drawable.flag_tj),
    TMT("TMT", 934,  "m",    R.string.name_tmt, R.drawable.flag_tm),
    TND("TND", 788,  "د.ت",  R.string.name_tnd, R.drawable.flag_tn),
    TOP("TOP", 776,  "T$",   R.string.name_top, R.drawable.flag_to),
    TRY("TRY", 949,  "₺",    R.string.name_try, R.drawable.flag_tr),
    TTD("TTD", 780,  "$",    R.string.name_ttd, R.drawable.flag_tt),
    TWD("TWD", 901,  "$",    R.string.name_twd, R.drawable.flag_tw),
    TZS("TZS", 834,  "Sh",   R.string.name_tzs, R.drawable.flag_tz),
    UAH("UAH", 980,  "₴",    R.string.name_uah, R.drawable.flag_ua),
    UGX("UGX", 800,  "Sh",   R.string.name_ugx, R.drawable.flag_ug),
    USD("USD", 840,  "$",    R.string.name_usd, R.drawable.flag_us),
    UYU("UYU", 858,  "$",    R.string.name_uyu, R.drawable.flag_uy),
    UZS("UZS", 860,  "сўм",  R.string.name_uzs, R.drawable.flag_uz),
    VEF("VEF", 937,  "Bs.",  R.string.name_vef, R.drawable.flag_ve),
    VES("VES", 928,  "Bs.",  R.string.name_ves, R.drawable.flag_ve),
    VND("VND", 704,  "₫",    R.string.name_vnd, R.drawable.flag_vn),
    VUV("VUV", 548,  "Vt",   R.string.name_vuv, R.drawable.flag_vu),
    WST("WST", 882,  "T",    R.string.name_wst, R.drawable.flag_ws),
    XAF("XAF", 950,  "Fr",   R.string.name_xaf, null),
    XAG("XAG", 961,  null,   R.string.name_xag, null),
    XAU("XAU", 959,  null,   R.string.name_xau, null),
    XCD("XCD", 951,  "$",    R.string.name_xcd, null),
    XDR("XDR", 960,  null,   R.string.name_xdr, null),
    XOF("XOF", 952,  "Fr",   R.string.name_xof, null),
    XPD("XPD", 964,  null,   R.string.name_xpd, null),
    XPF("XPF", 953,  "₣",    R.string.name_xpf, null),
    XPT("XPT", 962,  null,   R.string.name_xpt, null),
    YER("YER", 886,  "ر.ي",  R.string.name_yer, R.drawable.flag_ye),
    ZAR("ZAR", 710,  "R",    R.string.name_zar, R.drawable.flag_za),
    ZMW("ZMW", 967,  "ZK",   R.string.name_zmw, R.drawable.flag_zm),
    ZWL("ZWL", 932,  "$",    R.string.name_zwl, R.drawable.flag_zw);

    companion object {
        fun fromString(value: String): Currency? = values().firstOrNull { it.iso4217Alpha == value }
    }

    /**
     * https://en.wikipedia.org/wiki/ISO_4217#Alpha_codes
     * e.g. USD
     */
    fun iso4217Alpha(): String {
        return this.iso4217Alpha
    }

    /**
     * https://en.wikipedia.org/wiki/ISO_4217#Numeric_codes
     * e.g. 840 for USD
     */
    @Suppress("unused")
    fun iso4217Numeric(): Int? {
        return this.iso4217Numeric
    }

    /**
     * e.g. US dollar (localized) for USD
     */
    fun fullName(context: Context): String {
        return context.getString(this.fullName)
    }

    /**
     * e.g. star-spangled banner for USD
     */
    fun flag(context: Context): Drawable {
        return ContextCompat.getDrawable(context, this.flag ?: R.drawable.flag_unknown)!!
    }

    /**
     * https://en.wikipedia.org/wiki/Currency_symbol
     * e.g. $ for USD
     */
    fun symbol(): String? {
        return this.symbol
            ?.let { if (it.hasRtlChar()) it.wrapLtr() else it }
    }

    /**
     * https://en.wikipedia.org/wiki/Bidirectional_text#Table_of_possible_BiDi_character_types
     */
    private fun String.wrapLtr(): String {
        // isolate (recommended, but too new - FSI + PDI)
        // return "\u2067" + this + "\u2069"
        // embedding (discouraged - LRE + PDF)
        return "\u202A" + this + "\u202C"
    }

    private fun String.hasRtlChar(): Boolean {
        return this.any { it.directionality == CharDirectionality.RIGHT_TO_LEFT_ARABIC }
    }

}

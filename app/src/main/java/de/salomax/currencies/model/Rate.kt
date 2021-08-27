package de.salomax.currencies.model

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import de.salomax.currencies.R

@JsonClass(generateAdapter = true)
data class Rate(
    @field:Json(name = "name") val code: String,
    @field:Json(name = "value") val value: Float
) {

    fun getName(context: Context): String? {
        return when (code) {
            "AED" -> context.getString(R.string.name_aed)
            "AFN" -> context.getString(R.string.name_afn)
            "ALL" -> context.getString(R.string.name_all)
            "AMD" -> context.getString(R.string.name_amd)
            "ANG" -> context.getString(R.string.name_ang)
            "AOA" -> context.getString(R.string.name_aoa)
            "ARS" -> context.getString(R.string.name_ars)
            "AUD" -> context.getString(R.string.name_aud)
            "AWG" -> context.getString(R.string.name_awg)
            "AZN" -> context.getString(R.string.name_azn)
            "BAM" -> context.getString(R.string.name_bam)
            "BBD" -> context.getString(R.string.name_bbd)
            "BDT" -> context.getString(R.string.name_bdt)
            "BGN" -> context.getString(R.string.name_bgn)
            "BHD" -> context.getString(R.string.name_bhd)
            "BIF" -> context.getString(R.string.name_bif)
            "BMD" -> context.getString(R.string.name_bmd)
            "BND" -> context.getString(R.string.name_bnd)
            "BOB" -> context.getString(R.string.name_bob)
            "BRL" -> context.getString(R.string.name_brl)
            "BSD" -> context.getString(R.string.name_bsd)
            "BTC" -> context.getString(R.string.name_btc)
            "BTN" -> context.getString(R.string.name_btn)
            "BWP" -> context.getString(R.string.name_bwp)
            "BYN" -> context.getString(R.string.name_byn)
            "BZD" -> context.getString(R.string.name_bzd)
            "CAD" -> context.getString(R.string.name_cad)
            "CDF" -> context.getString(R.string.name_cdf)
            "CHF" -> context.getString(R.string.name_chf)
            "CLF" -> context.getString(R.string.name_clf)
            "CLP" -> context.getString(R.string.name_clp)
            "CNH" -> context.getString(R.string.name_cnh)
            "CNY" -> context.getString(R.string.name_cny)
            "COP" -> context.getString(R.string.name_cop)
            "CRC" -> context.getString(R.string.name_crc)
            "CUC" -> context.getString(R.string.name_cuc)
            "CUP" -> context.getString(R.string.name_cup)
            "CVE" -> context.getString(R.string.name_cve)
            "CZK" -> context.getString(R.string.name_czk)
            "DJF" -> context.getString(R.string.name_djf)
            "DKK" -> context.getString(R.string.name_dkk)
            "DOP" -> context.getString(R.string.name_dop)
            "DZD" -> context.getString(R.string.name_dzd)
            "EGP" -> context.getString(R.string.name_egp)
            "ERN" -> context.getString(R.string.name_ern)
            "ETB" -> context.getString(R.string.name_etb)
            "EUR" -> context.getString(R.string.name_eur)
            "FJD" -> context.getString(R.string.name_fjd)
            "FKP" -> context.getString(R.string.name_fkp)
            "FOK" -> context.getString(R.string.name_fok)
            "GBP" -> context.getString(R.string.name_gbp)
            "GEL" -> context.getString(R.string.name_gel)
            "GGP" -> context.getString(R.string.name_ggp)
            "GHS" -> context.getString(R.string.name_ghs)
            "GIP" -> context.getString(R.string.name_gip)
            "GMD" -> context.getString(R.string.name_gmd)
            "GNF" -> context.getString(R.string.name_gnf)
            "GTQ" -> context.getString(R.string.name_gtq)
            "GYD" -> context.getString(R.string.name_gyd)
            "HKD" -> context.getString(R.string.name_hkd)
            "HNL" -> context.getString(R.string.name_hnl)
            "HRK" -> context.getString(R.string.name_hrk)
            "HTG" -> context.getString(R.string.name_htg)
            "HUF" -> context.getString(R.string.name_huf)
            "IDR" -> context.getString(R.string.name_idr)
            "ILS" -> context.getString(R.string.name_ils)
            "IMP" -> context.getString(R.string.name_imp)
            "INR" -> context.getString(R.string.name_inr)
            "IQD" -> context.getString(R.string.name_iqd)
            "IRR" -> context.getString(R.string.name_irr)
            "ISK" -> context.getString(R.string.name_isk)
            "JEP" -> context.getString(R.string.name_jep)
            "JMD" -> context.getString(R.string.name_jmd)
            "JOD" -> context.getString(R.string.name_jod)
            "JPY" -> context.getString(R.string.name_jpy)
            "KES" -> context.getString(R.string.name_kes)
            "KGS" -> context.getString(R.string.name_kgs)
            "KHR" -> context.getString(R.string.name_khr)
            "KMF" -> context.getString(R.string.name_kmf)
            "KPW" -> context.getString(R.string.name_kpw)
            "KRW" -> context.getString(R.string.name_krw)
            "KWD" -> context.getString(R.string.name_kwd)
            "KYD" -> context.getString(R.string.name_kyd)
            "KZT" -> context.getString(R.string.name_kzt)
            "LAK" -> context.getString(R.string.name_lak)
            "LBP" -> context.getString(R.string.name_lbp)
            "LKR" -> context.getString(R.string.name_lkr)
            "LRD" -> context.getString(R.string.name_lrd)
            "LSL" -> context.getString(R.string.name_lsl)
            "LYD" -> context.getString(R.string.name_lyd)
            "MAD" -> context.getString(R.string.name_mad)
            "MDL" -> context.getString(R.string.name_mdl)
            "MGA" -> context.getString(R.string.name_mga)
            "MKD" -> context.getString(R.string.name_mkd)
            "MMK" -> context.getString(R.string.name_mmk)
            "MNT" -> context.getString(R.string.name_mnt)
            "MOP" -> context.getString(R.string.name_mop)
            "MRO" -> context.getString(R.string.name_mro)
            "MRU" -> context.getString(R.string.name_mru)
            "MUR" -> context.getString(R.string.name_mur)
            "MVR" -> context.getString(R.string.name_mvr)
            "MWK" -> context.getString(R.string.name_mwk)
            "MXN" -> context.getString(R.string.name_mxn)
            "MYR" -> context.getString(R.string.name_myr)
            "MZN" -> context.getString(R.string.name_mzn)
            "NAD" -> context.getString(R.string.name_nad)
            "NGN" -> context.getString(R.string.name_ngn)
            "NIO" -> context.getString(R.string.name_nio)
            "NOK" -> context.getString(R.string.name_nok)
            "NPR" -> context.getString(R.string.name_npr)
            "NZD" -> context.getString(R.string.name_nzd)
            "OMR" -> context.getString(R.string.name_omr)
            "PAB" -> context.getString(R.string.name_pab)
            "PEN" -> context.getString(R.string.name_pen)
            "PGK" -> context.getString(R.string.name_pgk)
            "PHP" -> context.getString(R.string.name_php)
            "PKR" -> context.getString(R.string.name_pkr)
            "PLN" -> context.getString(R.string.name_pln)
            "PYG" -> context.getString(R.string.name_pyg)
            "QAR" -> context.getString(R.string.name_qar)
            "RON" -> context.getString(R.string.name_ron)
            "RSD" -> context.getString(R.string.name_rsd)
            "RUB" -> context.getString(R.string.name_rub)
            "RWF" -> context.getString(R.string.name_rwf)
            "SAR" -> context.getString(R.string.name_sar)
            "SBD" -> context.getString(R.string.name_sbd)
            "SCR" -> context.getString(R.string.name_scr)
            "SDG" -> context.getString(R.string.name_sdg)
            "SEK" -> context.getString(R.string.name_sek)
            "SGD" -> context.getString(R.string.name_sgd)
            "SHP" -> context.getString(R.string.name_shp)
            "SLL" -> context.getString(R.string.name_sll)
            "SOS" -> context.getString(R.string.name_sos)
            "SRD" -> context.getString(R.string.name_srd)
            "SSP" -> context.getString(R.string.name_ssp)
            "STD" -> context.getString(R.string.name_std)
            "STN" -> context.getString(R.string.name_stn)
            "SVC" -> context.getString(R.string.name_svc)
            "SYP" -> context.getString(R.string.name_syp)
            "SZL" -> context.getString(R.string.name_szl)
            "THB" -> context.getString(R.string.name_thb)
            "TJS" -> context.getString(R.string.name_tjs)
            "TMT" -> context.getString(R.string.name_tmt)
            "TND" -> context.getString(R.string.name_tnd)
            "TOP" -> context.getString(R.string.name_top)
            "TRY" -> context.getString(R.string.name_try)
            "TTD" -> context.getString(R.string.name_ttd)
            "TWD" -> context.getString(R.string.name_twd)
            "TZS" -> context.getString(R.string.name_tzs)
            "UAH" -> context.getString(R.string.name_uah)
            "UGX" -> context.getString(R.string.name_ugx)
            "USD" -> context.getString(R.string.name_usd)
            "UYU" -> context.getString(R.string.name_uyu)
            "UZS" -> context.getString(R.string.name_uzs)
            "VEF" -> context.getString(R.string.name_vef)
            "VES" -> context.getString(R.string.name_ves)
            "VND" -> context.getString(R.string.name_vnd)
            "VUV" -> context.getString(R.string.name_vuv)
            "WST" -> context.getString(R.string.name_wst)
            "XAF" -> context.getString(R.string.name_xaf)
            "XAG" -> context.getString(R.string.name_xag)
            "XAU" -> context.getString(R.string.name_xau)
            "XCD" -> context.getString(R.string.name_xcd)
            "XDR" -> context.getString(R.string.name_xdr)
            "XOF" -> context.getString(R.string.name_xof)
            "XPD" -> context.getString(R.string.name_xpd)
            "XPF" -> context.getString(R.string.name_xpf)
            "XPT" -> context.getString(R.string.name_xpt)
            "YER" -> context.getString(R.string.name_yer)
            "ZAR" -> context.getString(R.string.name_zar)
            "ZMW" -> context.getString(R.string.name_zmw)
            "ZWL" -> context.getString(R.string.name_zwl)
            else -> null
        }
    }

    fun getFlag(context: Context): Drawable? {
        return when (code) {
            "AED" -> ContextCompat.getDrawable(context, R.drawable.flag_ae)
            "AFN" -> ContextCompat.getDrawable(context, R.drawable.flag_af)
            "ALL" -> ContextCompat.getDrawable(context, R.drawable.flag_al)
            "AMD" -> ContextCompat.getDrawable(context, R.drawable.flag_am)
            "ANG" -> ContextCompat.getDrawable(context, R.drawable.flag_nl) // On 10 October 2010, the Netherlands Antilles was dissolved into Curaçao, Sint Maarten and the three public bodies of the Caribbean Netherlands.
            "AOA" -> ContextCompat.getDrawable(context, R.drawable.flag_ao)
            "ARS" -> ContextCompat.getDrawable(context, R.drawable.flag_ar)
            "AUD" -> ContextCompat.getDrawable(context, R.drawable.flag_au)
            "AWG" -> ContextCompat.getDrawable(context, R.drawable.flag_aw)
            "AZN" -> ContextCompat.getDrawable(context, R.drawable.flag_az)
            "BAM" -> ContextCompat.getDrawable(context, R.drawable.flag_ba)
            "BBD" -> ContextCompat.getDrawable(context, R.drawable.flag_bb)
            "BDT" -> ContextCompat.getDrawable(context, R.drawable.flag_bd)
            "BGN" -> ContextCompat.getDrawable(context, R.drawable.flag_bg)
            "BHD" -> ContextCompat.getDrawable(context, R.drawable.flag_bh)
            "BIF" -> ContextCompat.getDrawable(context, R.drawable.flag_bi)
            "BMD" -> ContextCompat.getDrawable(context, R.drawable.flag_bm)
            "BND" -> ContextCompat.getDrawable(context, R.drawable.flag_bn)
            "BOB" -> ContextCompat.getDrawable(context, R.drawable.flag_bo)
            "BRL" -> ContextCompat.getDrawable(context, R.drawable.flag_br)
            "BSD" -> ContextCompat.getDrawable(context, R.drawable.flag_bs)
            "BTN" -> ContextCompat.getDrawable(context, R.drawable.flag_bt)
            "BWP" -> ContextCompat.getDrawable(context, R.drawable.flag_bw)
            "BYN" -> ContextCompat.getDrawable(context, R.drawable.flag_by)
            "BZD" -> ContextCompat.getDrawable(context, R.drawable.flag_bz)
            "CAD" -> ContextCompat.getDrawable(context, R.drawable.flag_ca)
            "CDF" -> ContextCompat.getDrawable(context, R.drawable.flag_cd)
            "CHF" -> ContextCompat.getDrawable(context, R.drawable.flag_ch)
            "CLF" -> ContextCompat.getDrawable(context, R.drawable.flag_cl)
            "CLP" -> ContextCompat.getDrawable(context, R.drawable.flag_cl)
            "CNH" -> ContextCompat.getDrawable(context, R.drawable.flag_cn)
            "CNY" -> ContextCompat.getDrawable(context, R.drawable.flag_cn)
            "COP" -> ContextCompat.getDrawable(context, R.drawable.flag_co)
            "CRC" -> ContextCompat.getDrawable(context, R.drawable.flag_cr)
            "CUC" -> ContextCompat.getDrawable(context, R.drawable.flag_cu)
            "CUP" -> ContextCompat.getDrawable(context, R.drawable.flag_cu)
            "CVE" -> ContextCompat.getDrawable(context, R.drawable.flag_cv)
            "CZK" -> ContextCompat.getDrawable(context, R.drawable.flag_cz)
            "DJF" -> ContextCompat.getDrawable(context, R.drawable.flag_dj)
            "DKK" -> ContextCompat.getDrawable(context, R.drawable.flag_dk)
            "DOP" -> ContextCompat.getDrawable(context, R.drawable.flag_do)
            "DZD" -> ContextCompat.getDrawable(context, R.drawable.flag_dz)
            "EGP" -> ContextCompat.getDrawable(context, R.drawable.flag_eg)
            "ERN" -> ContextCompat.getDrawable(context, R.drawable.flag_er)
            "ETB" -> ContextCompat.getDrawable(context, R.drawable.flag_et)
            "EUR" -> ContextCompat.getDrawable(context, R.drawable.flag_eu)
            "FJD" -> ContextCompat.getDrawable(context, R.drawable.flag_fj)
            "FKP" -> ContextCompat.getDrawable(context, R.drawable.flag_fk)
            "FOK" -> ContextCompat.getDrawable(context, R.drawable.flag_fo)
            "GBP" -> ContextCompat.getDrawable(context, R.drawable.flag_gb)
            "GEL" -> ContextCompat.getDrawable(context, R.drawable.flag_ge)
            "GGP" -> ContextCompat.getDrawable(context, R.drawable.flag_gg)
            "GHS" -> ContextCompat.getDrawable(context, R.drawable.flag_gh)
            "GIP" -> ContextCompat.getDrawable(context, R.drawable.flag_gi)
            "GMD" -> ContextCompat.getDrawable(context, R.drawable.flag_gm)
            "GNF" -> ContextCompat.getDrawable(context, R.drawable.flag_gn)
            "GTQ" -> ContextCompat.getDrawable(context, R.drawable.flag_gt)
            "GYD" -> ContextCompat.getDrawable(context, R.drawable.flag_gy)
            "HKD" -> ContextCompat.getDrawable(context, R.drawable.flag_hk)
            "HNL" -> ContextCompat.getDrawable(context, R.drawable.flag_hn)
            "HRK" -> ContextCompat.getDrawable(context, R.drawable.flag_hr)
            "HTG" -> ContextCompat.getDrawable(context, R.drawable.flag_ht)
            "HUF" -> ContextCompat.getDrawable(context, R.drawable.flag_hu)
            "IDR" -> ContextCompat.getDrawable(context, R.drawable.flag_id)
            "ILS" -> ContextCompat.getDrawable(context, R.drawable.flag_il)
            "IMP" -> ContextCompat.getDrawable(context, R.drawable.flag_im)
            "INR" -> ContextCompat.getDrawable(context, R.drawable.flag_in)
            "IQD" -> ContextCompat.getDrawable(context, R.drawable.flag_iq)
            "IRR" -> ContextCompat.getDrawable(context, R.drawable.flag_ir)
            "ISK" -> ContextCompat.getDrawable(context, R.drawable.flag_is)
            "JEP" -> ContextCompat.getDrawable(context, R.drawable.flag_je)
            "JMD" -> ContextCompat.getDrawable(context, R.drawable.flag_jm)
            "JOD" -> ContextCompat.getDrawable(context, R.drawable.flag_jo)
            "JPY" -> ContextCompat.getDrawable(context, R.drawable.flag_jp)
            "KES" -> ContextCompat.getDrawable(context, R.drawable.flag_ke)
            "KGS" -> ContextCompat.getDrawable(context, R.drawable.flag_kg)
            "KHR" -> ContextCompat.getDrawable(context, R.drawable.flag_kh)
            "KMF" -> ContextCompat.getDrawable(context, R.drawable.flag_km)
            "KPW" -> ContextCompat.getDrawable(context, R.drawable.flag_kp)
            "KRW" -> ContextCompat.getDrawable(context, R.drawable.flag_kr)
            "KWD" -> ContextCompat.getDrawable(context, R.drawable.flag_kw)
            "KYD" -> ContextCompat.getDrawable(context, R.drawable.flag_ky)
            "KZT" -> ContextCompat.getDrawable(context, R.drawable.flag_kz)
            "LAK" -> ContextCompat.getDrawable(context, R.drawable.flag_la)
            "LBP" -> ContextCompat.getDrawable(context, R.drawable.flag_lb)
            "LKR" -> ContextCompat.getDrawable(context, R.drawable.flag_lk)
            "LRD" -> ContextCompat.getDrawable(context, R.drawable.flag_lr)
            "LSL" -> ContextCompat.getDrawable(context, R.drawable.flag_ls)
            "LYD" -> ContextCompat.getDrawable(context, R.drawable.flag_ly)
            "MAD" -> ContextCompat.getDrawable(context, R.drawable.flag_ma)
            "MDL" -> ContextCompat.getDrawable(context, R.drawable.flag_md)
            "MGA" -> ContextCompat.getDrawable(context, R.drawable.flag_mg)
            "MKD" -> ContextCompat.getDrawable(context, R.drawable.flag_mk)
            "MMK" -> ContextCompat.getDrawable(context, R.drawable.flag_mm)
            "MNT" -> ContextCompat.getDrawable(context, R.drawable.flag_mn)
            "MOP" -> ContextCompat.getDrawable(context, R.drawable.flag_mo)
            "MRO" -> ContextCompat.getDrawable(context, R.drawable.flag_mr)
            "MRU" -> ContextCompat.getDrawable(context, R.drawable.flag_mr)
            "MUR" -> ContextCompat.getDrawable(context, R.drawable.flag_mu)
            "MVR" -> ContextCompat.getDrawable(context, R.drawable.flag_mv)
            "MWK" -> ContextCompat.getDrawable(context, R.drawable.flag_mw)
            "MXN" -> ContextCompat.getDrawable(context, R.drawable.flag_mx)
            "MYR" -> ContextCompat.getDrawable(context, R.drawable.flag_my)
            "MZN" -> ContextCompat.getDrawable(context, R.drawable.flag_mz)
            "NAD" -> ContextCompat.getDrawable(context, R.drawable.flag_na)
            "NGN" -> ContextCompat.getDrawable(context, R.drawable.flag_ng)
            "NIO" -> ContextCompat.getDrawable(context, R.drawable.flag_ni)
            "NOK" -> ContextCompat.getDrawable(context, R.drawable.flag_no)
            "NPR" -> ContextCompat.getDrawable(context, R.drawable.flag_np)
            "NZD" -> ContextCompat.getDrawable(context, R.drawable.flag_nz)
            "OMR" -> ContextCompat.getDrawable(context, R.drawable.flag_om)
            "PAB" -> ContextCompat.getDrawable(context, R.drawable.flag_pa)
            "PEN" -> ContextCompat.getDrawable(context, R.drawable.flag_pe)
            "PGK" -> ContextCompat.getDrawable(context, R.drawable.flag_pg)
            "PHP" -> ContextCompat.getDrawable(context, R.drawable.flag_ph)
            "PKR" -> ContextCompat.getDrawable(context, R.drawable.flag_pk)
            "PLN" -> ContextCompat.getDrawable(context, R.drawable.flag_pl)
            "PYG" -> ContextCompat.getDrawable(context, R.drawable.flag_py)
            "QAR" -> ContextCompat.getDrawable(context, R.drawable.flag_qa)
            "RON" -> ContextCompat.getDrawable(context, R.drawable.flag_ro)
            "RSD" -> ContextCompat.getDrawable(context, R.drawable.flag_rs)
            "RUB" -> ContextCompat.getDrawable(context, R.drawable.flag_ru)
            "RWF" -> ContextCompat.getDrawable(context, R.drawable.flag_rw)
            "SAR" -> ContextCompat.getDrawable(context, R.drawable.flag_sa)
            "SBD" -> ContextCompat.getDrawable(context, R.drawable.flag_sb)
            "SCR" -> ContextCompat.getDrawable(context, R.drawable.flag_sc)
            "SDG" -> ContextCompat.getDrawable(context, R.drawable.flag_sd)
            "SEK" -> ContextCompat.getDrawable(context, R.drawable.flag_se)
            "SGD" -> ContextCompat.getDrawable(context, R.drawable.flag_sg)
            "SHP" -> ContextCompat.getDrawable(context, R.drawable.flag_sh)
            "SLL" -> ContextCompat.getDrawable(context, R.drawable.flag_sl)
            "SOS" -> ContextCompat.getDrawable(context, R.drawable.flag_so)
            "SRD" -> ContextCompat.getDrawable(context, R.drawable.flag_sr)
            "SSP" -> ContextCompat.getDrawable(context, R.drawable.flag_ss)
            "STD" -> ContextCompat.getDrawable(context, R.drawable.flag_st)
            "STN" -> ContextCompat.getDrawable(context, R.drawable.flag_st)
            "SVC" -> ContextCompat.getDrawable(context, R.drawable.flag_sv)
            "SYP" -> ContextCompat.getDrawable(context, R.drawable.flag_sy)
            "SZL" -> ContextCompat.getDrawable(context, R.drawable.flag_sz)
            "THB" -> ContextCompat.getDrawable(context, R.drawable.flag_th)
            "TJS" -> ContextCompat.getDrawable(context, R.drawable.flag_tj)
            "TMT" -> ContextCompat.getDrawable(context, R.drawable.flag_tm)
            "TND" -> ContextCompat.getDrawable(context, R.drawable.flag_tn)
            "TOP" -> ContextCompat.getDrawable(context, R.drawable.flag_to)
            "TRY" -> ContextCompat.getDrawable(context, R.drawable.flag_tr)
            "TTD" -> ContextCompat.getDrawable(context, R.drawable.flag_tt)
            "TWD" -> ContextCompat.getDrawable(context, R.drawable.flag_tw)
            "TZS" -> ContextCompat.getDrawable(context, R.drawable.flag_tz)
            "UAH" -> ContextCompat.getDrawable(context, R.drawable.flag_ua)
            "UGX" -> ContextCompat.getDrawable(context, R.drawable.flag_ug)
            "USD" -> ContextCompat.getDrawable(context, R.drawable.flag_us)
            "UYU" -> ContextCompat.getDrawable(context, R.drawable.flag_uy)
            "UZS" -> ContextCompat.getDrawable(context, R.drawable.flag_uz)
            "VEF" -> ContextCompat.getDrawable(context, R.drawable.flag_ve)
            "VES" -> ContextCompat.getDrawable(context, R.drawable.flag_ve)
            "VND" -> ContextCompat.getDrawable(context, R.drawable.flag_vn)
            "VUV" -> ContextCompat.getDrawable(context, R.drawable.flag_vu)
            "WST" -> ContextCompat.getDrawable(context, R.drawable.flag_ws)
            "YER" -> ContextCompat.getDrawable(context, R.drawable.flag_ye)
            "ZAR" -> ContextCompat.getDrawable(context, R.drawable.flag_za)
            "ZMW" -> ContextCompat.getDrawable(context, R.drawable.flag_zm)
            "ZWL" -> ContextCompat.getDrawable(context, R.drawable.flag_zw)
            else -> ContextCompat.getDrawable(context, R.drawable.flag_unknown)
        }
    }

    /**
     * returns the proper currency symbol of a given rate
     */
    fun getCurrencySymbol(): String? {
        return when (code) {
            "AED" -> "د.إ"
            "AFN" -> "؋"
            "ALL" -> "L"
            "AMD" -> "֏"
            "ANG" -> "ƒ"
            "AOA" -> "Kz"
            "ARS" -> "$"
            "AUD" -> "$"
            "AWG" -> "Afl."
            "AZN" -> "₼"
            "BAM" -> "KM"
            "BBD" -> "$"
            "BDT" -> "৳"
            "BGN" -> "лв"
            "BHD" -> ".د.ب"
            "BIF" -> "Fr"
            "BMD" -> "$"
            "BND" -> "$"
            "BOB" -> "Bs."
            "BRL" -> "R$"
            "BSD" -> "$"
            "BTC" -> "₿"
            "BTN" -> "Nu."
            "BWP" -> "P"
            "BYN" -> "Br"
            "BZD" -> "$"
            "CAD" -> "$"
            "CDF" -> "Fr"
            "CHF" -> "Fr."
            "CLP" -> "$"
            "CNY" -> "¥"
            "COP" -> "$"
            "CRC" -> "₡"
            "CUC" -> "$"
            "CUP" -> "$"
            "CVE" -> "$"
            "CZK" -> "Kč"
            "DJF" -> "Fr"
            "DKK" -> "kr"
            "DOP" -> "RD$"
            "DZD" -> "د.ج"
            "EGP" -> "ج.م"
            "ERN" -> "Nfk"
            "ETB" -> "Br"
            "EUR" -> "€"
            "FJD" -> "$"
            "FKP" -> "£"
            "FOK" -> "kr"
            "GBP" -> "£"
            "GEL" -> "₾"
            "GGP" -> "£"
            "GHS" -> "₵"
            "GIP" -> "£"
            "GMD" -> "D"
            "GNF" -> "Fr"
            "GTQ" -> "Q"
            "GYD" -> "$"
            "HKD" -> "$"
            "HNL" -> "L"
            "HRK" -> "kn"
            "HTG" -> "G"
            "HUF" -> "Ft"
            "IDR" -> "Rp"
            "ILS" -> "₪"
            "IMP" -> "£"
            "INR" -> "₹"
            "IQD" -> "ع.د"
            "IRR" -> "﷼"
            "ISK" -> "kr"
            "JEP" -> "£"
            "JMD" -> "$"
            "JOD" -> "د.أ"
            "JPY" -> "¥"
            "KES" -> "Sh"
            "KGS" -> "С̲"
            "KHR" -> "៛"
            "KMF" -> "Fr"
            "KPW" -> "₩"
            "KRW" -> "₩"
            "KWD" -> "د.ك"
            "KYD" -> "$"
            "KZT" -> "₸"
            "LAK" -> "₭"
            "LBP" -> "ل.ل."
            "LKR" -> "Rs"
            "LRD" -> "$"
            "LSL" -> "L"
            "LYD" -> "ل.د"
            "MAD" -> "د.م."
            "MDL" -> "L"
            "MGA" -> "Ar"
            "MKD" -> "ден"
            "MMK" -> "Ks"
            "MNT" -> "₮"
            "MOP" -> "MOP$"
            "MRU" -> "UM"
            "MUR" -> "₨"
            "MVR" -> ".ރ"
            "MWK" -> "MK"
            "MXN" -> "$"
            "MYR" -> "RM"
            "MZN" -> "MT"
            "NAD" -> "$"
            "NGN" -> "₦"
            "NIO" -> "C$"
            "NOK" -> "kr"
            "NPR" -> "रु"
            "NZD" -> "$"
            "OMR" -> "ر.ع."
            "PAB" -> "B/."
            "PEN" -> "S/"
            "PGK" -> "K"
            "PHP" -> "₱"
            "PKR" -> "₨"
            "PLN" -> "zł"
            "PYG" -> "₲"
            "QAR" -> "ر.ق"
            "RON" -> "lei"
            "RSD" -> "дин."
            "RUB" -> "₽"
            "RWF" -> "Fr"
            "SAR" -> "ر.س"
            "SBD" -> "$"
            "SCR" -> "₨"
            "SDG" -> "ج.س."
            "SEK" -> "kr"
            "SGD" -> "$"
            "SHP" -> "£"
            "SLL" -> "Le"
            "SOS" -> "Sh"
            "SRD" -> "$"
            "SSP" -> "£"
            "STN" -> "Db"
            "SYP" -> "ل.س"
            "SZL" -> "L"
            "THB" -> "฿"
            "TJS" -> "SM"
            "TMT" -> "m"
            "TND" -> "د.ت"
            "TOP" -> "T$"
            "TRY" -> "₺"
            "TTD" -> "$"
            "TWD" -> "$"
            "TZS" -> "Sh"
            "UAH" -> "₴"
            "UGX" -> "Sh"
            "USD" -> "$"
            "UYU" -> "$"
            "UZS" -> "сўм"
            "VES" -> "Bs."
            "VND" -> "₫"
            "VUV" -> "Vt"
            "WST" -> "T"
            "XAF" -> "Fr"
            "XCD" -> "$"
            "XOF" -> "Fr"
            "XPF" -> "₣"
            "YER" -> "ر.ي"
            "ZAR" -> "R"
            "ZMW" -> "ZK"
            "ZWL" -> "$"
            else -> null
        }
    }

}

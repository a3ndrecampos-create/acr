package com.qrscangera.app.utils

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Centraliza todo o carregamento e exibição de anúncios do AdMob.
 *
 * >>> IDs DE TESTE <<<
 * Todos os IDs abaixo são os IDs de teste OFICIAIS do Google e funcionam em qualquer app
 * sem violar as políticas do AdMob. Antes de publicar, troque cada um pelo ID real gerado
 * no seu console do AdMob (console em https://apps.admob.com).
 */
object AdsManager {

    // >>> TROCAR pelo Ad Unit ID real do BANNER da tela "Gerar" <<<
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/9214589741"

    // >>> TROCAR pelo Ad Unit ID real do INTERSTITIAL (exibido a cada 3 escaneamentos) <<<
    private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    // >>> TROCAR pelo Ad Unit ID real do REWARDED (remover marca d'água do QR gerado) <<<
    private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var scanCountSinceLastAd = 0

    fun initialize(context: Context) {
        MobileAds.initialize(context)
        loadInterstitial(context)
        loadRewarded(context)
    }

    private fun loadInterstitial(context: Context) {
        InterstitialAd.load(
            context, INTERSTITIAL_AD_UNIT_ID, AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    private fun loadRewarded(context: Context) {
        RewardedAd.load(
            context, REWARDED_AD_UNIT_ID, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    /**
     * Chame após cada escaneamento concluído. Mostra o interstitial a cada 3 escaneamentos,
     * pulando o primeiro uso do app para não irritar o usuário novo.
     */
    fun onScanCompleted(activity: Activity) {
        if (com.qrscangera.app.billing.BillingManager.isPro.value) return // Pro não vê anúncios
        scanCountSinceLastAd++
        if (scanCountSinceLastAd >= 3) {
            scanCountSinceLastAd = 0
            val ad = interstitialAd
            if (ad != null) {
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        interstitialAd = null
                        loadInterstitial(activity)
                    }
                }
                ad.show(activity)
            } else {
                loadInterstitial(activity)
            }
        }
    }

    /** Mostra o rewarded ad para liberar a remoção da marca d'água/logo personalizado. */
    fun showRewardedForWatermarkRemoval(activity: Activity, onRewarded: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            loadRewarded(activity)
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewarded(activity)
            }
        }
        ad.show(activity) { onRewarded() }
    }
}

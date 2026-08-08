package com.qrscangera.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Controla as duas formas de virar Pro (desbloqueia Gerar + Histórico e remove os anúncios):
 * assinatura mensal OU compra única vitalícia. O usuário escolhe uma das duas na tela de
 * upgrade; qualquer uma das duas ativas já libera o app inteiro.
 *
 * >>> TROCAR <<< os IDs abaixo pelos IDs exatos criados no Play Console:
 * - Monetização > Produtos no app        -> produto tipo "in-app" (PRO_LIFETIME_PRODUCT_ID)
 * - Monetização > Produtos de assinatura -> produto tipo "assinatura" (PRO_MONTHLY_PRODUCT_ID)
 * Produtos só ficam disponíveis pra teste depois do primeiro upload do app (mesmo em teste interno).
 */
object BillingManager {

    // >>> TROCAR pelo ID real do produto de assinatura mensal <<<
    const val PRO_MONTHLY_PRODUCT_ID = "qrscangera_pro_monthly"

    // >>> TROCAR pelo ID real do produto de compra única (vitalício) <<<
    const val PRO_LIFETIME_PRODUCT_ID = "qrscangera_pro_lifetime"

    private var billingClient: BillingClient? = null
    private val productDetailsById = mutableMapOf<String, ProductDetails>()

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro

    private val _monthlyPriceLabel = MutableStateFlow<String?>(null)
    val monthlyPriceLabel: StateFlow<String?> = _monthlyPriceLabel

    private val _lifetimePriceLabel = MutableStateFlow<String?>(null)
    val lifetimePriceLabel: StateFlow<String?> = _lifetimePriceLabel

    fun initialize(context: Context) {
        if (billingClient != null) return

        val purchasesListener = PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                purchases.forEach { handlePurchase(it) }
            }
        }

        billingClient = BillingClient.newBuilder(context)
            .setListener(purchasesListener)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProductDetails()
                    restorePurchases()
                }
            }
            override fun onBillingServiceDisconnected() {
                // A própria Billing Library tenta reconectar sozinha quando necessário.
            }
        })
    }

    private fun queryProductDetails() {
        val products = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRO_MONTHLY_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRO_LIFETIME_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder().setProductList(products).build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                productDetailsList.forEach { details -> productDetailsById[details.productId] = details }

                productDetailsById[PRO_LIFETIME_PRODUCT_ID]?.let {
                    _lifetimePriceLabel.value = it.oneTimePurchaseOfferDetails?.formattedPrice
                }
                productDetailsById[PRO_MONTHLY_PRODUCT_ID]?.let { details ->
                    // Pega o preço do primeiro "offer" válido do plano base da assinatura
                    _monthlyPriceLabel.value = details.subscriptionOfferDetails
                        ?.firstOrNull()
                        ?.pricingPhases?.pricingPhaseList?.firstOrNull()
                        ?.formattedPrice
                }
            }
        }
    }

    /** Abre o fluxo de compra nativo do Google Play para o produto (mensal ou vitalício). */
    fun launchPurchase(activity: Activity, productId: String) {
        val details = productDetailsById[productId] ?: return

        val paramsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)

        // Assinaturas precisam do "offer token" do plano base; compra única não usa isso.
        if (productId == PRO_MONTHLY_PRODUCT_ID) {
            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return
            paramsBuilder.setOfferToken(offerToken)
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(paramsBuilder.build()))
            .build()
        billingClient?.launchBillingFlow(activity, flowParams)
    }

    /** Restaura o Pro automaticamente ao abrir o app (assinatura ativa ou compra vitalícia). */
    private fun restorePurchases() {
        listOf(BillingClient.ProductType.INAPP, BillingClient.ProductType.SUBS).forEach { type ->
            val params = QueryPurchasesParams.newBuilder().setProductType(type).build()
            billingClient?.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases.forEach { handlePurchase(it) }
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        val isProProduct = purchase.products.any { it == PRO_MONTHLY_PRODUCT_ID || it == PRO_LIFETIME_PRODUCT_ID }
        if (isProProduct && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            _isPro.value = true
            if (!purchase.isAcknowledged) {
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient?.acknowledgePurchase(ackParams) { }
            }
        }
    }
}

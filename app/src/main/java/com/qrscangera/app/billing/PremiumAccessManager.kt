package com.qrscangera.app.billing

import android.content.Context
import com.qrscangera.app.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** De onde veio a liberação do Premium neste aparelho agora - só pra exibir a mensagem certa na UI. */
enum class PremiumSource { CLOSED_TESTING, FREE_LAUNCH, EARLY_ADOPTER, TRIAL, PURCHASED, NONE }

/**
 * Única fonte de verdade sobre "este usuário tem acesso Premium agora?", combinando (nessa
 * ordem de prioridade):
 * 1. Build de teste fechado (BuildConfig.IS_CLOSED_TESTING) -> sempre libera, nunca expira
 * 2. Lançamento 100% grátis (LaunchModeManager.FREE_LAUNCH_MODE) -> libera todo mundo agora
 * 3. "Early adopter" (instalou durante o lançamento grátis) -> continua liberado pra sempre,
 *    mesmo depois que o Premium for ligado numa atualização futura
 * 4. Trial de 7 dias (usuários novos, depois que o Premium for ligado) -> libera enquanto ativo
 * 5. Compra real (BillingManager.isPro) -> libera pra sempre
 *
 * O resto do app (gate das abas, anúncios) deve consultar SEMPRE esta classe, nunca o
 * BillingManager/TrialManager/LaunchModeManager diretamente - assim a regra fica num lugar só.
 */
object PremiumAccessManager {

    private val _hasAccess = MutableStateFlow(BuildConfig.IS_CLOSED_TESTING || LaunchModeManager.FREE_LAUNCH_MODE)
    val hasAccess: StateFlow<Boolean> = _hasAccess

    private val _source = MutableStateFlow(
        when {
            BuildConfig.IS_CLOSED_TESTING -> PremiumSource.CLOSED_TESTING
            LaunchModeManager.FREE_LAUNCH_MODE -> PremiumSource.FREE_LAUNCH
            else -> PremiumSource.NONE
        }
    )
    val source: StateFlow<PremiumSource> = _source

    fun initialize(context: Context) {
        if (BuildConfig.IS_CLOSED_TESTING) {
            // Build exclusivo da faixa de teste fechado: libera tudo, sem trial, sem expirar.
            _hasAccess.value = true
            _source.value = PremiumSource.CLOSED_TESTING
            return
        }

        // Se estiver rodando em lançamento grátis, marca este aparelho como early adopter
        // (efeito só aparece de verdade numa atualização futura, quando FREE_LAUNCH_MODE virar false).
        LaunchModeManager.markEarlyAdopterIfApplicable(context)

        TrialManager.ensureStarted(context)
        recompute(context)
    }

    /** Chame de novo sempre que BillingManager.isPro mudar (compra concluída, restaurada, etc). */
    fun recompute(context: Context) {
        if (BuildConfig.IS_CLOSED_TESTING) return // já garantido acima, nunca muda

        if (LaunchModeManager.FREE_LAUNCH_MODE) {
            _hasAccess.value = true
            _source.value = PremiumSource.FREE_LAUNCH
            return
        }

        val earlyAdopter = LaunchModeManager.isEarlyAdopter(context)
        val purchased = BillingManager.isPro.value
        val trialActive = TrialManager.isTrialActive(context)

        _hasAccess.value = earlyAdopter || purchased || trialActive
        _source.value = when {
            earlyAdopter -> PremiumSource.EARLY_ADOPTER
            purchased -> PremiumSource.PURCHASED
            trialActive -> PremiumSource.TRIAL
            else -> PremiumSource.NONE
        }
    }
}

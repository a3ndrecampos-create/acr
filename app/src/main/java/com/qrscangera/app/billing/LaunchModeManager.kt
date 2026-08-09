package com.qrscangera.app.billing

import android.content.Context

/**
 * Controla o lançamento inicial 100% grátis do app (sem paywall, sem anúncios) e o
 * "presente" de quem instalou nessa fase: mesmo depois que o Premium for ligado numa
 * atualização futura, esses primeiros usuários continuam com acesso completo pra sempre.
 *
 * >>> QUANDO FOR LIGAR O PREMIUM <<<
 * Troque FREE_LAUNCH_MODE pra `false` nesta linha, suba uma nova versão (versionCode maior)
 * e pronto: quem já tinha o app instalado antes dessa atualização continua com tudo
 * liberado (graças ao "early adopter" salvo no aparelho); só quem instalar DEPOIS da
 * atualização passa a ver o trial de 7 dias / paywall normal.
 */
object LaunchModeManager {

    // >>> TROCAR pra false quando for lançar a versão paga <<<
    const val FREE_LAUNCH_MODE = true

    private const val PREFS_NAME = "qrscangera_launch"
    private const val KEY_EARLY_ADOPTER = "is_early_adopter"

    /**
     * Chame uma vez, cedo (junto do resto da inicialização). Se o app estiver rodando em
     * modo de lançamento grátis, marca este aparelho como "early adopter" pra sempre -
     * mesmo depois que FREE_LAUNCH_MODE virar false numa atualização futura.
     */
    fun markEarlyAdopterIfApplicable(context: Context) {
        if (!FREE_LAUNCH_MODE) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_EARLY_ADOPTER, false)) {
            prefs.edit().putBoolean(KEY_EARLY_ADOPTER, true).apply()
        }
    }

    fun isEarlyAdopter(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_EARLY_ADOPTER, false)
}

package com.qrscangera.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qrscangera.app.ui.theme.BrandPurple

/**
 * Tela de "paywall" mostrada nas abas Gerar/Histórico quando o usuário ainda está na
 * versão Free (que só tem o leitor de QR Code). Oferece as duas formas de virar Pro:
 * assinatura mensal (opção principal) ou compra única vitalícia (opção secundária).
 */
@Composable
fun ProUpsellCard(
    featureName: String,
    monthlyPriceLabel: String?,
    lifetimePriceLabel: String?,
    onSubscribeMonthlyClick: () -> Unit,
    onBuyLifetimeClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.WorkspacePremium,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = BrandPurple
        )
        Spacer(Modifier.height(16.dp))
        Text("$featureName é um recurso Pro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))

        BenefitLine("Gerar QR Codes ilimitados, com cor e logo personalizados")
        BenefitLine("Histórico completo de leituras e gerações")
        BenefitLine("Sem anúncios")

        Spacer(Modifier.height(28.dp))
        Button(onClick = onSubscribeMonthlyClick, modifier = Modifier.fillMaxWidth()) {
            Text(if (monthlyPriceLabel != null) "Assinar • $monthlyPriceLabel/mês" else "Assinar mensalmente")
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onBuyLifetimeClick, modifier = Modifier.fillMaxWidth()) {
            Text(if (lifetimePriceLabel != null) "Ou comprar vitalício por $lifetimePriceLabel" else "Ou comprar vitalício")
        }

        Spacer(Modifier.height(24.dp))
        // Banner do AdMob - como Gerar/Histórico são exclusivos do Pro, é aqui (na tela de
        // paywall que o usuário Free vê) que o banner realmente aparece pra ele.
        BannerAdView()
    }
}

@Composable
private fun BenefitLine(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
    ) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

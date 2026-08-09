# QR Scan & Gera

App Android nativo (Kotlin + Jetpack Compose) para ler e gerar QR Codes, com tema
azul/roxo, dark/light mode automático, histórico local e anúncios AdMob.

## Como abrir
1. Abra a pasta `qrscangera` no Android Studio (Arquivo > Abrir).
2. Deixe o Studio sincronizar o Gradle (ele mesmo gera o `gradle-wrapper.jar` que falta).
3. Rode em um dispositivo/emulador com câmera para testar o escaneamento.

## Versão Free x Pro
- **Free**: só a aba "Escanear" funciona (leitor de QR Code), com anúncios (banner na tela
  de upgrade + interstitial a cada 3 escaneamentos).
- **Pro** (compra única, `qrscangera_pro_unlock`): desbloqueia "Gerar" e "Histórico" e
  remove todos os anúncios. Implementado com Google Play Billing em `billing/BillingManager.kt`.
- **Antes de publicar**: crie o produto de compra única no Play Console (Monetização >
  Produtos no app) com o ID exato `qrscangera_pro_unlock` (ou troque a constante
  `PRO_PRODUCT_ID` em `BillingManager.kt` pelo ID que você usar). Produtos de IAP só ficam
  disponíveis para teste depois do primeiro upload do app (mesmo em teste interno).

## Lançamento 100% grátis (fase atual)
`LaunchModeManager.FREE_LAUNCH_MODE = true` está ligado: todo mundo tem acesso completo
(Gerar + Histórico) e **sem anúncios**, sem trial, sem paywall - é um app free normal por
enquanto. Quem instalar nessa fase fica marcado como "early adopter" no aparelho e
continua com tudo liberado pra sempre, mesmo depois.

**Quando for lançar a versão paga**: abra `billing/LaunchModeManager.kt`, troque
`FREE_LAUNCH_MODE` pra `false`, suba uma nova versão (`versionCode` maior). A partir
daí: quem já tinha o app antes continua liberado (early adopter); quem instalar depois
passa pelo trial de 7 dias e, depois, pelo paywall normal (mensal/vitalício + anúncios).
Não precisa mexer em mais nada - o resto do sistema (trial, compra, anúncios) já está
pronto e só "acorda" quando esse interruptor virar false.

## Teste fechado x Produção (variantes de build)
O app tem duas variantes ("product flavors"), escolhidas na hora de compilar - não é uma
configuração que muda sozinha ou que o usuário consegue alterar:

- **`production`**: regra comercial real. Usuário instala grátis, ganha 7 dias de acesso
  Premium (Gerar + Histórico + sem anúncios), depois disso essas telas voltam a pedir
  assinatura/compra. É o que vai pra faixa de **Produção** do Play Console.
- **`closedTesting`**: Premium sempre liberado, sem trial, sem expirar - pra quem está
  testando o app na faixa de **Teste fechado** do Play Console conseguir ver 100% das
  telas. **Nunca envie esta variante pra Produção.**

Cada variante gera um `.aab` com nome diferente (`app-production-release.aab` e
`app-closedTesting-release.aab`), então não tem como confundir os dois arquivos na hora
de subir no Play Console. Localmente, pra gerar cada uma: `./gradlew bundleProductionRelease`
ou `./gradlew bundleClosedTestingRelease` (o Android Studio também deixa escolher a
"Build Variant" numa aba própria).

**Limitação importante**: o Android não tem uma forma nativa/confiável de o app detectar
sozinho "este instalador específico é um testador do Play Console" em tempo real - por
isso a separação é feita em tempo de compilação (build flavor), não em tempo de execução.
Isso segue exatamente a recomendação da própria Google para esse cenário.

Onde cada regra fica implementada, se precisar mexer:
- `billing/TrialManager.kt` - conta os 7 dias (guardado localmente no aparelho)
- `billing/PremiumAccessManager.kt` - combina teste fechado + trial + compra real numa
  única resposta (`hasAccess`) que o resto do app usa
- `billing/BillingManager.kt` - só cuida da compra real (Google Play Billing), não sabe
  nada sobre trial ou teste fechado

## Antes de publicar
- **AdMob**: troque os IDs de teste em `AndroidManifest.xml` (meta-data `APPLICATION_ID`)
  e em `utils/AdsManager.kt` (banner, interstitial, rewarded) pelos IDs reais do seu
  console em https://apps.admob.com. Todos estão marcados com `>>> TROCAR <<<`.
- **Fonte Poppins/Inter**: baixe os `.ttf` em https://fonts.google.com/specimen/Poppins,
  coloque em `app/src/main/res/font/` e ajuste `ui/theme/Type.kt` (instruções no
  próprio arquivo). Por padrão o app usa a fonte do sistema para não depender de
  arquivos externos no build.
- **Ícone do app**: os drawables em `res/drawable/ic_launcher_*.xml` são um placeholder
  simples; substitua pelo logo definitivo (pode usar o Image Asset Studio do Android
  Studio: botão direito em `res` > New > Image Asset).

## Estrutura
```
ui/screens      -> ScanScreen, GenerateScreen, HistoryScreen
ui/components   -> câmera, bottom sheet de resultado, chips, seletor de cor, banner
ui/theme        -> cores, tipografia, tema claro/escuro
viewmodel       -> um ViewModel por tela (MVVM)
data            -> Room (histórico) + parser de conteúdo de QR (wifi/vcard)
utils           -> gerador de QR (ZXing customizado), payload Pix, AdMob, vibração, salvar/compartilhar imagem
```

## Observações técnicas
- A leitura usa CameraX + ML Kit Barcode Scanning; a geração usa o ZXing só para
  calcular a matriz de módulos — o desenho (cor, cantos arredondados, logo) é feito
  manualmente em `QrCodeGenerator.kt`.
- O QR Code do tipo Pix gera o payload "Copia e Cola" real (padrão EMV do Banco
  Central, com CRC16), pronto para ser pago em qualquer banco.
- Conectar a Wi-Fi automaticamente não é mais permitido pelo Android a partir da
  versão 10 sem confirmação do usuário; por isso o app copia a senha e abre as
  configurações de Wi-Fi do sistema.

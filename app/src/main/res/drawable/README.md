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

package com.achmadichzan.rangkum.domain.model

data class VoskModelConfig(
    val code: String,
    val name: String,
    val url: String,
    val folderName: String,
    val size: String
)

enum class ModelStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    READY,
    ACTIVE
}

val AVAILABLE_MODELS = listOf(
    VoskModelConfig(
        code = "ar",
        name = "Arabic (Medium)",
        url = "https://alphacephei.com/vosk/models/vosk-model-ar-mgb2-0.4.zip",
        folderName = "vosk-model-ar-mgb2-0.4",
        size = "318MB"
    ),
    VoskModelConfig(
        code = "ar-tn",
        name = "Arabic Tunisian (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-ar-tn-0.1-linto.zip",
        folderName = "vosk-model-small-ar-tn-0.1-linto",
        size = "158MB"
    ),
    VoskModelConfig(
        code = "br",
        name = "Breton (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-br-0.8.zip",
        folderName = "vosk-model-br-0.8",
        size = "70MB"
    ),
    VoskModelConfig(
        code = "ca",
        name = "Catalan (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-ca-0.4.zip",
        folderName = "vosk-model-small-ca-0.4",
        size = "42MB"
    ),
    VoskModelConfig(
        code = "cn",
        name = "Chinese (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip",
        folderName = "vosk-model-small-cn-0.22",
        size = "42MB"
    ),
    VoskModelConfig(
        code = "cs",
        name = "Czech (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-cs-0.4-rhasspy.zip",
        folderName = "vosk-model-small-cs-0.4-rhasspy",
        size = "44MB"
    ),
    VoskModelConfig(
        code = "en",
        name = "English (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip",
        folderName = "vosk-model-small-en-us-0.15",
        size = "40MB"
    ),
    VoskModelConfig(
        code = "eo",
        name = "Esperanto (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-eo-0.42.zip",
        folderName = "vosk-model-small-eo-0.42",
        size = "42MB"
    ),
    VoskModelConfig(
        code = "fa",
        name = "Farsi (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-fa-0.42.zip",
        folderName = "vosk-model-small-fa-0.42",
        size = "53MB"
    ),
    VoskModelConfig(
        code = "tl-ph",
        name = "Filipino (Medium)",
        url = "https://alphacephei.com/vosk/models/vosk-model-tl-ph-generic-0.6.zip",
        folderName = "vosk-model-tl-ph-generic-0.6",
        size = "320MB"
    ),
    VoskModelConfig(
        code = "fr",
        name = "French (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-fr-0.22.zip",
        folderName = "vosk-model-small-fr-0.22",
        size = "41MB"
    ),
    VoskModelConfig(
        code = "de",
        name = "German (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-de-0.15.zip",
        folderName = "vosk-model-small-de-0.15",
        size = "45MB"
    ),
    VoskModelConfig(
        code = "gu",
        name = "Gujarati (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-gu-0.42.zip",
        folderName = "vosk-model-small-gu-0.42",
        size = "100MB"
    ),
    VoskModelConfig(
        code = "hi",
        name = "Hindi (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-hi-0.22.zip",
        folderName = "vosk-model-small-hi-0.22",
        size = "42MB"
    ),
    VoskModelConfig(
        code = "en-in",
        name = "Indian English (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-en-in-0.4.zip",
        folderName = "vosk-model-small-en-in-0.4",
        size = "36MB"
    ),
    VoskModelConfig(
        code = "it",
        name = "Italian (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-it-0.22.zip",
        folderName = "vosk-model-small-it-0.22",
        size = "48MB"
    ),
    VoskModelConfig(
        code = "ja",
        name = "Japanese (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-ja-0.22.zip",
        folderName = "vosk-model-small-ja-0.22",
        size = "48MB"
    ),
    VoskModelConfig(
        code = "kz",
        name = "Kazakh (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-kz-0.42.zip",
        folderName = "vosk-model-small-kz-0.42",
        size = "58MB"
    ),
    VoskModelConfig(
        code = "ko",
        name = "Korean (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-ko-0.22.zip",
        folderName = "vosk-model-small-ko-0.22",
        size = "82MB"
    ),
    VoskModelConfig(
        code = "ky",
        name = "Kyrgyz (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-ky-0.42.zip",
        folderName = "vosk-model-small-ky-0.42",
        size = "49MB"
    ),
    VoskModelConfig(
        code = "pl",
        name = "Polish (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-pl-0.22.zip",
        folderName = "vosk-model-small-pl-0.22",
        size = "50MB"
    ),
    VoskModelConfig(
        code = "pt",
        name = "Portuguese/Brazilian Portuguese (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-pt-0.3.zip",
        folderName = "vosk-model-small-pt-0.3",
        size = "31MB"
    ),
    VoskModelConfig(
        code = "ru",
        name = "Russian (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip",
        folderName = "vosk-model-small-ru-0.22",
        size = "45MB"
    ),
    VoskModelConfig(
        code = "es",
        name = "Spanish (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip",
        folderName = "vosk-model-small-es-0.42",
        size = "39MB"
    ),
    VoskModelConfig(
        code = "sv",
        name = "Swedish (Medium)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-sv-rhasspy-0.15.zip",
        folderName = "vosk-model-small-sv-rhasspy-0.15",
        size = "289MB"
    ),
    VoskModelConfig(
        code = "tg",
        name = "Tajik (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-tg-0.22.zip",
        folderName = "vosk-model-small-tg-0.22",
        size = "50MB"
    ),
    VoskModelConfig(
        code = "te",
        name = "Telugu (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-te-0.42.zip",
        folderName = "vosk-model-small-te-0.42",
        size = "58MB"
    ),
    VoskModelConfig(
        code = "tr",
        name = "Turkish (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-tr-0.3.zip",
        folderName = "vosk-model-small-tr-0.3",
        size = "35MB"
    ),
    VoskModelConfig(
        code = "uk",
        name = "Ukrainian (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-uk-v3-small.zip",
        folderName = "vosk-model-small-uk-v3-small",
        size = "133MB"
    ),
    VoskModelConfig(
        code = "uz",
        name = "Uzbek (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-uz-0.22.zip",
        folderName = "vosk-model-small-uz-0.22",
        size = "49MB"
    ),
    VoskModelConfig(
        code = "vn",
        name = "Vietnamese (Small)",
        url = "https://alphacephei.com/vosk/models/vosk-model-small-vn-0.4.zip",
        folderName = "vosk-model-small-vn-0.4",
        size = "32MB"
    )
)
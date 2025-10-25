package com.example.proyecto_presin_arterial;

public final class ApiConfig {
    private ApiConfig() {}

    // Webhook de n8n
    // TEST → cuando usas "Listen for test event"
    public static final String WEBHOOK_URL_TEST =
            "https://primary-production-53510.up.railway.app/webhook-test/pacientes/data";

    // PROD → cuando activas el workflow
    public static final String WEBHOOK_URL_PROD =
            "https://primary-production-53510.up.railway.app/webhook/pacientes/data";

    // Si quieres cambiar entre TEST/PROD en un solo lugar:
    public static final boolean USE_TEST = true; // ← pon false para PROD

    public static String webhookUrl() {
        return USE_TEST ? WEBHOOK_URL_TEST : WEBHOOK_URL_PROD;
    }

    // Timeouts (segundos)
    public static final int CONNECT_TIMEOUT_SEC = 15;
    public static final int READ_TIMEOUT_SEC = 20;
    public static final int WRITE_TIMEOUT_SEC = 20;
}

package com.example.proyecto_presin_arterial;

import android.content.Context;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Cliente HTTP para enviar datos a un Webhook de n8n.
 * Incluye:
 *  - sendPaciente(...)                -> evento de registro de paciente
 *  - sendMedicionPresion(...)         -> evento de medición "120/80" (simple)
 *  - sendMeasurement(...)             -> compatibilidad (sistólica/diastólica/pulso/temp)
 *  - sendVitalWatchMeasurement(...)   -> **RECOMENDADO**: payload completo que n8n espera
 */
public class WebhookClient {

    /** Callback para reportar éxito o error. */
    public interface WebhookCallback {
        void onSuccess(int code, String body);
        void onFailure(Exception e);
    }

    private final OkHttpClient http;
    private final String webhookUrl;
    @Nullable private final String bearerToken;

    private static final MediaType MEDIA_JSON =
            MediaType.parse("application/json; charset=utf-8");

    /**
     * Crea un WebhookClient (sin token).
     * @param context   No se almacena; firma compatible
     * @param webhookUrl URL completa del webhook
     */
    public WebhookClient(Context context, String webhookUrl) {
        this(context, webhookUrl, null);

    }

    /** Crea un WebhookClient con token Bearer opcional. */
    public WebhookClient(Context context, String webhookUrl, @Nullable String bearerToken) {
        this.webhookUrl = webhookUrl;
        this.bearerToken = bearerToken;


        this.http = new OkHttpClient.Builder()
                .callTimeout(25, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .protocols(java.util.Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1))
                .build();
    }

    // =========================
    //  MÉTODOS PÚBLICOS
    // =========================

    /** Envía un REGISTRO DE PACIENTE a n8n. */
    public void sendPaciente(String nombre,
                             int    edad,
                             String genero,
                             String contacto,
                             String enfermedades,
                             String medicamentos,
                             String fechaIso,
                             @Nullable WebhookCallback callback) {

        JSONObject body = new JSONObject();
        try {
            body.put("tipo", "paciente");
            body.put("nombre", nombre);
            body.put("edad",   edad);
            body.put("genero", genero);
            body.put("contacto", contacto);
            body.put("enfermedades", enfermedades);
            body.put("medicamentos", medicamentos);
            body.put("fecha",  fechaIso);
        } catch (JSONException e) {
            if (callback != null) callback.onFailure(e);
            return;
        }
        postJson(body, callback);
    }

    /** Envía una MEDICIÓN de presión al n8n (ej. "120/80"). */
    public void sendMedicionPresion(String nombre,
                                    String presionTexto,  // "120/80"
                                    String fechaIso,
                                    @Nullable WebhookCallback callback) {

        JSONObject body = new JSONObject();
        try {
            body.put("tipo", "medicion");
            body.put("nombre", nombre);
            body.put("presion", presionTexto);
            body.put("fecha",  fechaIso);
        } catch (JSONException e) {
            if (callback != null) callback.onFailure(e);
            return;
        }
        postJson(body, callback);
    }

    /** Envía una MEDICIÓN con campos separados (sistólica/diastólica/pulso/temp). */
    public void sendMeasurement(String nombre,
                                int sistolica,
                                int diastolica,
                                int pulso,
                                double temp,
                                String fechaIso,
                                @Nullable WebhookCallback callback) {

        JSONObject body = new JSONObject();
        try {
            body.put("tipo", "measurement"); // etiqueta para distinguir si quisieras
            body.put("nombre", nombre);
            body.put("sistolica", sistolica);
            body.put("diastolica", diastolica);
            body.put("pulso", pulso);
            body.put("temp", temp);
            body.put("fecha", fechaIso);
        } catch (JSONException e) {
            if (callback != null) callback.onFailure(e);
            return;
        }
        postJson(body, callback);
    }

    /**
     * 🚀 RECOMENDADO: Envía el payload COMPLETO que tu flujo de n8n espera.
     * Coincide con el nodo Code/IF del workflow (clave: "presion_arterial").
     */
    public void sendVitalWatchMeasurement(
            String nombre,
            int edad,
            String genero,
            String contacto,
            String enfermedades,
            String medicamentos,
            String presionArterial,    // "120/80" (tal cual)
            String fechaIso,           // opcional para trazabilidad
            @Nullable WebhookCallback callback
    ) {
        JSONObject body = new JSONObject();
        try {
            body.put("nombre", nombre);
            body.put("edad", edad);
            body.put("genero", genero);
            body.put("contacto", contacto);
            body.put("enfermedades", enfermedades);
            body.put("medicamentos", medicamentos);
            body.put("presion_arterial", presionArterial); // <- CLAVE EXACTA
            body.put("fecha", fechaIso);
        } catch (JSONException e) {
            if (callback != null) callback.onFailure(e);
            return;
        }
        postJson(body, callback);
    }

    // =========================
    //  Helper común de envío
    // =========================
    private void postJson(JSONObject body, @Nullable WebhookCallback callback) {
        Request.Builder rb = new Request.Builder()
                .url(webhookUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json");

        if (bearerToken != null && !bearerToken.isEmpty()) {
            rb.addHeader("Authorization", "Bearer " + bearerToken);
        }

        Request req = rb.post(RequestBody.create(body.toString(), MEDIA_JSON)).build();

        http.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) callback.onFailure(e);
            }
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    String respBody = response.body() != null ? response.body().string() : "";
                    if (response.isSuccessful()) {
                        if (callback != null) callback.onSuccess(response.code(), respBody);
                    } else {
                        if (callback != null) {
                            callback.onFailure(new IOException("HTTP " + response.code() + ": " + respBody));
                        }
                    }
                } catch (Exception e) {
                    if (callback != null) callback.onFailure(e);
                } finally {
                    response.close();
                }
            }
        });
    }

    /** Cierra recursos HTTP cuando sales de la Activity. */
    public void shutdown() {
        try {
            http.dispatcher().executorService().shutdown();
            http.connectionPool().evictAll();
        } catch (Exception ignore) { }
    }
}

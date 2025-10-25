package com.example.proyecto_presin_arterial;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Orquesta los envíos al webhook de n8n.
 * - enviarMedicionCompleta(String presionRaw): normaliza "120/80", toma datos del paciente guardados en formulario y envía el payload completo (el que tu flujo espera).
 * - enviarAltaPaciente(PacienteN8n p): opcional, manda el alta de paciente.
 */
public class VitalWatchRepository {

    private final Context ctx;
    private final WebhookClient client;

    public VitalWatchRepository(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        // Usa TEST o PROD según ApiConfig.USE_TEST
        this.client = new WebhookClient(this.ctx, ApiConfig.webhookUrl());
    }

    /**
     * Envía la medición completa al webhook (payload que n8n espera).
     * Lee los datos del paciente desde SharedPreferences (guardados en formulario).
     */
    public void enviarMedicionCompleta(String presionRaw) {
        // 1) Normaliza presión
        String presion = BPUtils.normalize(presionRaw);
        if (presion.isEmpty()) {
            toast("Presión inválida. Usa formato 120/80");
            return;
        }

        // 2) Recupera datos del paciente almacenados en formulario
        SharedPreferences prefs = ctx.getSharedPreferences("registro_paciente", Context.MODE_PRIVATE);
        String nombre   = prefs.getString("last_nombre", "");
        String edadStr  = prefs.getString("last_edad", "0");
        String genero   = prefs.getString("last_genero", "");
        String contacto = prefs.getString("last_contacto", "");
        String enf      = prefs.getString("last_enfermedades", "");
        String med      = prefs.getString("last_medicamentos", "");

        int edad;
        try { edad = Integer.parseInt(edadStr); } catch (Exception e) { edad = 0; }

        if (nombre.isEmpty()) {
            toast("Completa primero el registro del paciente en el formulario");
            return;
        }

        // 3) Fecha ISO
        String fechaIso = isoNowUtc();

        // 4) Llamada al Webhook (payload completo con presion_arterial)
        client.sendVitalWatchMeasurement(
                nombre, edad, genero, contacto, enf, med,
                presion, fechaIso,
                new WebhookClient.WebhookCallback() {
                    @Override public void onSuccess(int code, String body) {
                        runOnUi(() -> toast("Medición enviada ✅"));
                    }
                    @Override public void onFailure(Exception e) {
                        runOnUi(() -> toast("No se pudo enviar a n8n: " + (e.getMessage()==null?"Error":e.getMessage())));
                    }
                }
        );
    }

    /**
     * (Opcional) Envía el alta del paciente al webhook.
     * Útil si quieres que n8n respalde también la ficha sin medición.
     */
    public void enviarAltaPaciente(PacienteN8n p) {
        if (p == null || p.nombre == null || p.nombre.trim().isEmpty()) {
            toast("Paciente inválido");
            return;
        }
        String fechaIso = isoNowUtc();

        client.sendPaciente(
                p.nombre, p.edad, p.genero, p.contacto, p.enfermedades, p.medicamentos, fechaIso,
                new WebhookClient.WebhookCallback() {
                    @Override public void onSuccess(int code, String body) {
                        runOnUi(() -> toast("Paciente enviado ✅"));
                    }
                    @Override public void onFailure(Exception e) {
                        runOnUi(() -> toast("Error al enviar paciente: " + (e.getMessage()==null?"Error":e.getMessage())));
                    }
                }
        );
    }

    // ---------------- helpers ----------------

    private void toast(String s) {
        Toast.makeText(ctx, s, Toast.LENGTH_LONG).show();
    }

    private void runOnUi(Runnable r) {
        android.os.Handler h = new android.os.Handler(ctx.getMainLooper());
        h.post(r);
    }

    private String isoNowUtc() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(new java.util.Date());
    }
}

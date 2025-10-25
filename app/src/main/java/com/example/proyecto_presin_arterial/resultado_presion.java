package com.example.proyecto_presin_arterial;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;

public class resultado_presion extends AppCompatActivity {

    private static final String WEBHOOK_URL_TEST =
            "https://primary-production-53510.up.railway.app/webhook-test/pacientes/data";
    private static final String WEBHOOK_URL_PROD =
            "https://primary-production-53510.up.railway.app/webhook/pacientes/data";

    private EditText etPresion;
    private TextView tvResultado, tvRecomendaciones;
    private Button btnAnalizarPresion, btnLlamarEmergencias, btnVolver;

    private WebhookClient webhookClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resultado_presion);

        // (Opcional) Cargar API_KEY si la usas
        try {
            java.util.Properties properties = new java.util.Properties();
            java.io.InputStream inputStream = getAssets().open("keys.properties");
            properties.load(inputStream);
            String API_KEY = properties.getProperty("GROQ_API_KEY");
        } catch (Exception ignored) { }

        // UI
        etPresion = findViewById(R.id.etPresion);
        btnAnalizarPresion = findViewById(R.id.btnAnalizarPresion);
        tvResultado = findViewById(R.id.tvResultado);
        tvRecomendaciones = findViewById(R.id.tvRecomendaciones);
        btnLlamarEmergencias = findViewById(R.id.btnLlamarEmergencias);
        btnVolver = findViewById(R.id.btnVolver);

        tvRecomendaciones.setVisibility(View.GONE);
        btnLlamarEmergencias.setVisibility(View.GONE);

        btnVolver.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnLlamarEmergencias.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:911"));
            startActivity(intent);
        });

        // 🔗 Usa TEST mientras escuchas eventos en n8n; cambia a PROD cuando actives el workflow
        webhookClient = new WebhookClient(this, WEBHOOK_URL_TEST);
        // webhookClient = new WebhookClient(this, WEBHOOK_URL_PROD);

        btnAnalizarPresion.setOnClickListener(v -> {
            final String presion = etPresion.getText().toString().trim();
            if (presion.isEmpty()) {
                tvResultado.setText("Por favor, ingresa la presión arterial (formato 120/80).");
                tvRecomendaciones.setVisibility(View.GONE);
                btnLlamarEmergencias.setVisibility(View.GONE);
                return;
            }

            String resultado;
            String recomendaciones;
            boolean mostrarBotonEmergencia;
            String categoria;

            try {
                String[] partes = presion.split("/");
                final int sistolica = Integer.parseInt(partes[0].trim());
                final int diastolica = Integer.parseInt(partes[1].trim());

                if (sistolica < 90 || diastolica < 60) {
                    resultado = "Presión baja";
                    categoria = "Baja";
                    recomendaciones = "Toma agua, recuéstate y eleva las piernas. Si hay síntomas graves, llama a emergencias.";
                    mostrarBotonEmergencia = true;
                } else if (sistolica >= 140 || diastolica >= 90) {
                    resultado = "Presión alta";
                    categoria = "Alta";
                    recomendaciones = "Relájate y siéntate. Evita el estrés y consulta a tu médico. Si hay síntomas graves, llama a emergencias.";
                    mostrarBotonEmergencia = true;
                } else if (sistolica >= 120 && sistolica <= 139) {
                    resultado = "Presión normal (prehipertensión)";
                    categoria = "Prehipertensión";
                    recomendaciones = "Mantén hábitos saludables y monitorea tu presión.";
                    mostrarBotonEmergencia = false;
                } else {
                    resultado = "Presión normal";
                    categoria = "Normal";
                    recomendaciones = "¡Bien! Mantén hábitos saludables y monitorea tu presión.";
                    mostrarBotonEmergencia = false;
                }

                // Guardar en DB local
                guardarPresionArterial(presion, categoria);

            } catch (Exception e) {
                resultado = "Formato incorrecto. Usa el formato 120/80";
                recomendaciones = "";
                mostrarBotonEmergencia = false;
                categoria = "No válida";
            }

            final String finalResultado = resultado;
            final String finalRecomendaciones = recomendaciones;
            final boolean finalMostrarBotonEmergencia = mostrarBotonEmergencia;

            // Actualiza UI
            runOnUiThread(() -> {
                tvResultado.setText(finalResultado);
                tvRecomendaciones.setText(finalRecomendaciones);
                tvRecomendaciones.setVisibility(View.VISIBLE);
                btnLlamarEmergencias.setVisibility(finalMostrarBotonEmergencia ? View.VISIBLE : View.GONE);
            });

            // ========= Envío a n8n (payload completo) =========
            final SharedPreferences prefs = getSharedPreferences("registro_paciente", MODE_PRIVATE);
            final String nombrePaciente   = prefs.getString("last_nombre", "Paciente");
            final String edadStr          = prefs.getString("last_edad", "0");
            final String generoPaciente   = prefs.getString("last_genero", "");
            final String contactoPaciente = prefs.getString("last_contacto", "");
            final String enfPaciente      = prefs.getString("last_enfermedades", "");
            final String medPaciente      = prefs.getString("last_medicamentos", "");

            final int edadPaciente;
            try { edadPaciente = Integer.parseInt(edadStr); } catch (Exception ex) { return; }

            final String fechaIso = isoNowUtc();

            // Usa el método recomendado que coincide con tu flujo de n8n
            webhookClient.sendVitalWatchMeasurement(
                    nombrePaciente,
                    edadPaciente,
                    generoPaciente,
                    contactoPaciente,
                    enfPaciente,
                    medPaciente,
                    presion,        // <- "120/80" (campo clave: presion_arterial)
                    fechaIso,
                    new WebhookClient.WebhookCallback() {
                        @Override
                        public void onSuccess(int code, String body) {
                            runOnUiThread(() ->
                                    android.widget.Toast.makeText(
                                            resultado_presion.this,
                                            "Medición enviada a n8n",
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show()
                            );
                        }

                        @Override
                        public void onFailure(Exception e) {
                            final String msg = (e.getMessage() == null) ? "Error desconocido" : e.getMessage();
                            runOnUiThread(() ->
                                    android.widget.Toast.makeText(
                                            resultado_presion.this,
                                            "No se pudo enviar a n8n: " + msg,
                                            android.widget.Toast.LENGTH_LONG
                                    ).show()
                            );
                        }
                    }
            );
            // ================================================
        });
    }

    // Guarda presión y categoría en SQLite
    private void guardarPresionArterial(String presion, String categoria) {
        try {
            android.content.SharedPreferences prefs = getSharedPreferences("registro_paciente", MODE_PRIVATE);
            final String nombre = prefs.getString("last_nombre", "");

            if (!nombre.isEmpty()) {
                PacienteDbHelper dbHelper = new PacienteDbHelper(this);

                android.content.ContentValues values = new android.content.ContentValues();
                values.put(PacienteDbHelper.COLUMN_PRESION, presion);
                values.put(PacienteDbHelper.COLUMN_CATEGORIA_PRESION, categoria);

                String whereClause = PacienteDbHelper.COLUMN_NOMBRE + " = ?";
                String[] whereArgs = { nombre };

                int rowsUpdated = dbHelper.getWritableDatabase().update(
                        PacienteDbHelper.TABLE_NAME,
                        values,
                        whereClause,
                        whereArgs
                );

                if (rowsUpdated == 0) {
                    android.database.Cursor cursor = dbHelper.getReadableDatabase().query(
                            PacienteDbHelper.TABLE_NAME,
                            new String[]{ PacienteDbHelper.COLUMN_ID },
                            PacienteDbHelper.COLUMN_NOMBRE + " = ?",
                            new String[]{ nombre },
                            null, null,
                            PacienteDbHelper.COLUMN_ID + " DESC",
                            "1"
                    );
                    if (cursor.moveToFirst()) {
                        long id = cursor.getLong(cursor.getColumnIndexOrThrow(PacienteDbHelper.COLUMN_ID));
                        dbHelper.getWritableDatabase().update(
                                PacienteDbHelper.TABLE_NAME,
                                values,
                                PacienteDbHelper.COLUMN_ID + " = ?",
                                new String[]{ String.valueOf(id) }
                        );
                    }
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String isoNowUtc() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(new java.util.Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webhookClient != null) webhookClient.shutdown();
    }
}

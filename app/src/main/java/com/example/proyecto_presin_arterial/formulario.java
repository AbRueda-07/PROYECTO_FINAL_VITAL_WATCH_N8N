package com.example.proyecto_presin_arterial;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class formulario extends AppCompatActivity {

    // 🔗 URLs del Webhook de n8n
    // TEST (cuando usas "Listen for test event"):
    private static final String WEBHOOK_URL_TEST =
            "https://primary-production-53510.up.railway.app/webhook-test/pacientes/data";
    // PRODUCCIÓN (workflow ACTIVO):
    private static final String WEBHOOK_URL_PROD =
            "https://primary-production-53510.up.railway.app/webhook/pacientes/data";

    private EditText etNombre, etEdad, etContacto, etEnfermedades, etMedicamentos;
    private RadioGroup rgGenero;
    private Button btnGuardar;

    private WebhookClient webhookClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_formulario);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Init UI
        etNombre = findViewById(R.id.etNombre);
        etEdad = findViewById(R.id.etEdad);
        rgGenero = findViewById(R.id.rgGenero);
        etContacto = findViewById(R.id.etContacto);
        etEnfermedades = findViewById(R.id.etEnfermedades);
        etMedicamentos = findViewById(R.id.etMedicamentos);
        btnGuardar = findViewById(R.id.btnGuardar);

        // 🔗 Usa TEST mientras pruebas; cambia a PROD cuando actives el workflow
        webhookClient = new WebhookClient(this, WEBHOOK_URL_TEST);
        // webhookClient = new WebhookClient(this, WEBHOOK_URL_PROD);

        btnGuardar.setOnClickListener(v -> guardarYEnviar());
    }

    private void guardarYEnviar() {
        final String nombre = etNombre.getText().toString().trim();
        final String edadStr = etEdad.getText().toString().trim();
        final String contacto = etContacto.getText().toString().trim();
        final String enfermedades = etEnfermedades.getText().toString().trim();
        final String medicamentos = etMedicamentos.getText().toString().trim();

        int selectedGeneroId = rgGenero.getCheckedRadioButtonId();
        final String genero;
        if (selectedGeneroId != -1) {
            RadioButton rbGenero = findViewById(selectedGeneroId);
            genero = rbGenero.getText().toString().trim();
        } else {
            genero = "";
        }

        // ✅ Validaciones
        if (nombre.isEmpty()) { toast("El campo nombre es obligatorio"); return; }
        if (edadStr.isEmpty()) { toast("El campo edad es obligatorio"); return; }
        if (genero.isEmpty()) { toast("Selecciona un género"); return; }
        if (contacto.isEmpty()) { toast("El campo contacto es obligatorio"); return; }
        if (enfermedades.isEmpty()) { toast("El campo enfermedades es obligatorio"); return; }
        if (medicamentos.isEmpty()) { toast("El campo medicamentos es obligatorio"); return; }

        final int edad;
        try {
            edad = Integer.parseInt(edadStr);
        } catch (NumberFormatException e) {
            toast("Edad inválida");
            return;
        }

        // ⏱️ Anti-spam: 15 min si los datos son idénticos
        SharedPreferences prefs = getSharedPreferences("registro_paciente", MODE_PRIVATE);
        String lastNombre = prefs.getString("last_nombre", "");
        String lastEdad = prefs.getString("last_edad", "");
        String lastGenero = prefs.getString("last_genero", "");
        String lastContacto = prefs.getString("last_contacto", "");
        String lastEnfermedades = prefs.getString("last_enfermedades", "");
        String lastMedicamentos = prefs.getString("last_medicamentos", "");
        long ultimoRegistro = prefs.getLong("ultimo_registro", 0);
        final long ahora = System.currentTimeMillis();

        boolean mismosDatos = nombre.equals(lastNombre)
                && edadStr.equals(lastEdad)
                && genero.equals(lastGenero)
                && contacto.equals(lastContacto)
                && enfermedades.equals(lastEnfermedades)
                && medicamentos.equals(lastMedicamentos);

        if (mismosDatos && ultimoRegistro != 0 && (ahora - ultimoRegistro) < 15 * 60 * 1000) {
            toast("Espera 15 minutos antes de registrar nuevamente estos mismos datos");
            return;
        }

        // 💾 Guardar en SQLite
        PacienteDbHelper dbHelper = new PacienteDbHelper(this);
        ContentValues values = new ContentValues();
        values.put(PacienteDbHelper.COLUMN_NOMBRE, nombre);
        values.put(PacienteDbHelper.COLUMN_EDAD, edad);
        values.put(PacienteDbHelper.COLUMN_GENERO, genero);
        values.put(PacienteDbHelper.COLUMN_CONTACTO, contacto);
        values.put(PacienteDbHelper.COLUMN_ENFERMEDADES, enfermedades);
        values.put(PacienteDbHelper.COLUMN_MEDICAMENTOS, medicamentos);

        long newRowId = dbHelper.getWritableDatabase().insert(PacienteDbHelper.TABLE_NAME, null, values);
        if (newRowId == -1) {
            toast("Error al guardar");
            return;
        }

        // 🧠 Persistir último registro para reuso y anti-spam
        prefs.edit()
                .putString("last_nombre", nombre)
                .putString("last_edad", edadStr)
                .putString("last_genero", genero)
                .putString("last_contacto", contacto)
                .putString("last_enfermedades", enfermedades)
                .putString("last_medicamentos", medicamentos)
                .putLong("ultimo_registro", ahora)
                .apply();

        // 🗓️ Fecha ISO 8601 (UTC)
        final String fechaIso = isoNowUtc();

        // 🚀 Enviar REGISTRO DE PACIENTE a n8n (opcional pero recomendado)
        webhookClient.sendPaciente(
                nombre,           // nombre
                edad,             // edad
                genero,           // genero
                contacto,         // contacto
                enfermedades,     // enfermedades
                medicamentos,     // medicamentos
                fechaIso,         // fecha
                new WebhookClient.WebhookCallback() {
                    @Override
                    public void onSuccess(int code, String body) {
                        runOnUiThread(() -> {
                            toast("Paciente enviado a n8n");
                            irAResultado(nombre, edadStr, genero, contacto, enfermedades, medicamentos);
                        });
                    }
                    @Override
                    public void onFailure(Exception e) {
                        runOnUiThread(() -> {
                            String msg = e.getMessage() == null ? "Error desconocido" : e.getMessage();
                            if (msg.contains("HTTP 404")) {
                                toast("Webhook no registrado (404). En test: pulsa 'Listen for test event'. En prod: activa el workflow y usa /webhook/");
                            } else {
                                toast("Error al enviar a n8n: " + msg);
                            }
                            // Navegar igual para no bloquear UX
                            irAResultado(nombre, edadStr, genero, contacto, enfermedades, medicamentos);
                        });
                    }
                }
        );

        // 👉 Si prefieres NO enviar aquí y solo enviar al medir la presión,
        // comenta el bloque anterior y deja solo la navegación a resultado:
        // irAResultado(nombre, edadStr, genero, contacto, enfermedades, medicamentos);
    }

    private void irAResultado(String nombre, String edad, String genero, String contacto,
                              String enfermedades, String medicamentos) {
        Intent intent = new Intent(this, resultado_presion.class);
        intent.putExtra("nombre", nombre);
        intent.putExtra("edad", edad);
        intent.putExtra("genero", genero);
        intent.putExtra("contacto", contacto);
        intent.putExtra("enfermedades", enfermedades);
        intent.putExtra("medicamentos", medicamentos);
        startActivity(intent);
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    // Fecha ISO 8601 en UTC
    private String isoNowUtc() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webhookClient != null) webhookClient.shutdown();
    }
}

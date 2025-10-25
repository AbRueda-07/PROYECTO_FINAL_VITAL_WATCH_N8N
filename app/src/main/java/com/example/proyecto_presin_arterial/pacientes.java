package com.example.proyecto_presin_arterial;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.database.Cursor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.text.Editable; // 👈 IMPORTANTE: Añade esta línea
import android.text.TextWatcher; // 👈 IMPORTANTE: Añade esta línea

public class pacientes extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etBuscar;
    private Button btnVolver;
    private PacienteAdapter adapter;
    private List<Map<String, String>> pacientesList = new ArrayList<>();
    private PacienteDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pacientes);

        // Inicializar vistas
        recyclerView = findViewById(R.id.recyclerViewPacientes);
        etBuscar = findViewById(R.id.etBuscarPaciente);
        btnVolver = findViewById(R.id.btnVolver);
        dbHelper = new PacienteDbHelper(this);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PacienteAdapter(pacientesList, this::mostrarDetallesPaciente);
        recyclerView.setAdapter(adapter);

        // Cargar pacientes
        cargarPacientes("");

        // Buscador en tiempo real - CORREGIDO
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No necesitamos hacer nada aquí
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarPacientes(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No necesitamos hacer nada aquí
            }
        });

        // Botón Volver al inicio
        btnVolver.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void cargarPacientes(String filtro) {
        pacientesList.clear();

        String[] projection = {
                PacienteDbHelper.COLUMN_ID,
                PacienteDbHelper.COLUMN_NOMBRE,
                PacienteDbHelper.COLUMN_EDAD,
                PacienteDbHelper.COLUMN_GENERO,
                PacienteDbHelper.COLUMN_CONTACTO,
                PacienteDbHelper.COLUMN_ENFERMEDADES,
                PacienteDbHelper.COLUMN_MEDICAMENTOS,
                PacienteDbHelper.COLUMN_PRESION,
                PacienteDbHelper.COLUMN_CATEGORIA_PRESION // ✅ Asegúrate de incluir esta línea
        };

        String selection = null;
        String[] selectionArgs = null;

        if (!filtro.isEmpty()) {
            selection = PacienteDbHelper.COLUMN_NOMBRE + " LIKE ?";
            selectionArgs = new String[]{"%" + filtro + "%"};
        }

        Cursor cursor = dbHelper.getReadableDatabase().query(
                PacienteDbHelper.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null, null, PacienteDbHelper.COLUMN_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                Map<String, String> map = new HashMap<>();
                map.put("id", cursor.getString(cursor.getColumnIndexOrThrow(PacienteDbHelper.COLUMN_ID)));
                map.put("nombre", cursor.getString(cursor.getColumnIndexOrThrow(PacienteDbHelper.COLUMN_NOMBRE)));
                map.put("edad", cursor.getString(cursor.getColumnIndexOrThrow(PacienteDbHelper.COLUMN_EDAD)));
                map.put("genero", cursor.getString(cursor.getColumnIndexOrThrow(PacienteDbHelper.COLUMN_GENERO)));
                map.put("contacto", cursor.getString(cursor.getColumnIndexOrThrow(PacienteDbHelper.COLUMN_CONTACTO)));
                map.put("enfermedades", cursor.getString(cursor.getColumnIndexOrThrow(PacienteDbHelper.COLUMN_ENFERMEDADES)));
                map.put("medicamentos", cursor.getString(cursor.getColumnIndexOrThrow(PacienteDbHelper.COLUMN_MEDICAMENTOS)));

                // Manejar la columna de presión
                String presion = cursor.getString(cursor.getColumnIndexOrThrow(PacienteDbHelper.COLUMN_PRESION));
                map.put("presion", presion != null ? presion : "No registrada");

                // ✅ NUEVO: Manejar la columna de categoría de presión
                String categoria = cursor.getString(cursor.getColumnIndexOrThrow(PacienteDbHelper.COLUMN_CATEGORIA_PRESION));
                map.put("categoria_presion", categoria != null ? categoria : "No registrada");

                pacientesList.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();

        adapter.updateList(pacientesList);
    }

    private void mostrarDetallesPaciente(Map<String, String> paciente) {
        StringBuilder detalles = new StringBuilder();
        detalles.append("ID: ").append(paciente.get("id")).append("\n\n");
        detalles.append("👤 Nombre: ").append(paciente.get("nombre")).append("\n");
        detalles.append("🎂 Edad: ").append(paciente.get("edad")).append("\n");
        detalles.append("⚧️ Género: ").append(paciente.get("genero")).append("\n");
        detalles.append("📞 Contacto: ").append(paciente.get("contacto")).append("\n");
        detalles.append("🏥 Enfermedades: ").append(paciente.get("enfermedades")).append("\n");
        detalles.append("💊 Medicamentos: ").append(paciente.get("medicamentos")).append("\n");
        detalles.append("📊 Presión Arterial: ").append(paciente.get("presion")).append("\n");
        detalles.append("📈 Categoría: ").append(paciente.get("categoria_presion")); // ✅ Agregado

        new android.app.AlertDialog.Builder(this)
                .setTitle("Detalles del Paciente")
                .setMessage(detalles.toString())
                .setPositiveButton("Cerrar", null)
                .show();
    }
}
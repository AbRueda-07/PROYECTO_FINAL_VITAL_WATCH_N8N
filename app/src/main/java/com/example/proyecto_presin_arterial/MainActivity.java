package com.example.proyecto_presin_arterial;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Aplicar padding para evitar que el contenido quede debajo de la barra de estado
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Animación suave al cargar
        ImageView logo = findViewById(R.id.imageViewLogo);
        logo.animate().scaleX(1.1f).scaleY(1.1f).setDuration(800).withEndAction(() -> {
            logo.animate().scaleX(1.0f).scaleY(1.0f).setDuration(800).start();
        }).start();

        // Botón Comenzar
        Button btnComenzar = findViewById(R.id.button);
        btnComenzar.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        Intent intent = new Intent(MainActivity.this, formulario.class);
                        startActivity(intent);
                    }).start();
        });

        // Botón Mostrar Pacientes
        Button btnMostrarPacientes = findViewById(R.id.btnMostrarPacientes);
        btnMostrarPacientes.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start();
                        Intent intent = new Intent(MainActivity.this, pacientes.class);
                        startActivity(intent);
                    }).start();
        });
    }
}
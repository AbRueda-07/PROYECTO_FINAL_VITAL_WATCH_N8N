package com.example.proyecto_presin_arterial;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PacienteDbHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "pacientes.db";
    public static final int DATABASE_VERSION = 3; // Actualizado de 2 a 3
    public static final String TABLE_NAME = "paciente";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_EDAD = "edad";
    public static final String COLUMN_GENERO = "genero";
    public static final String COLUMN_CONTACTO = "contacto";
    public static final String COLUMN_ENFERMEDADES = "enfermedades";
    public static final String COLUMN_MEDICAMENTOS = "medicamentos";
    public static final String COLUMN_PRESION = "presion_arterial";
    public static final String COLUMN_CATEGORIA_PRESION = "categoria_presion"; // ✅ NUEVA COLUMNA

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT NOT NULL, " +
                    COLUMN_EDAD + " INTEGER, " +
                    COLUMN_GENERO + " TEXT, " +
                    COLUMN_CONTACTO + " TEXT, " +
                    COLUMN_ENFERMEDADES + " TEXT, " +
                    COLUMN_MEDICAMENTOS + " TEXT, " +
                    COLUMN_PRESION + " TEXT, " + // ✅ Agregado a la creación de tabla
                    COLUMN_CATEGORIA_PRESION + " TEXT)"; // ✅ NUEVA COLUMNA

    public PacienteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_PRESION + " TEXT");
        }
        if (oldVersion < 3) { // ✅ NUEVA ACTUALIZACIÓN
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_CATEGORIA_PRESION + " TEXT");
        }
    }
}
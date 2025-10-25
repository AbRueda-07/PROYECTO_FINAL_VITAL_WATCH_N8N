package com.example.proyecto_presin_arterial;

public class PacienteN8n {
    public String nombre;
    public int edad;
    public String genero;
    public String contacto;
    public String enfermedades;
    public String medicamentos;

    public PacienteN8n() {}

    public PacienteN8n(String nombre, int edad, String genero, String contacto,
                       String enfermedades, String medicamentos) {
        this.nombre = nombre;
        this.edad = edad;
        this.genero = genero;
        this.contacto = contacto;
        this.enfermedades = enfermedades;
        this.medicamentos = medicamentos;
    }
}

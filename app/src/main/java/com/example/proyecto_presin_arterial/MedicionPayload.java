package com.example.proyecto_presin_arterial;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Modelo que representa el cuerpo JSON que se envía al flujo de n8n (VitalWatch).
 * Incluye toda la información que tu flujo espera:
 * nombre, edad, género, contacto, presión arterial, categoría, enfermedades, medicamentos y timestamp.
 */
public class MedicionPayload {

    public String nombre;
    public int edad;
    public String genero;
    public String contacto;
    public String presion_arterial;
    public int sistolica;
    public int diastolica;
    public String categoria_presion;
    public String enfermedades;
    public String medicamentos;
    public String timestamp;

    // Constructor principal
    public MedicionPayload(String nombre, int edad, String genero, String contacto,
                           String presion, String enfermedades, String medicamentos, String fechaIso) {

        this.nombre = nombre;
        this.edad = edad;
        this.genero = genero;
        this.contacto = contacto;
        this.presion_arterial = BPUtils.normalize(presion);

        // Parsear la presión arterial (si es válida)
        if (!this.presion_arterial.isEmpty() && this.presion_arterial.contains("/")) {
            try {
                String[] partes = this.presion_arterial.split("/");
                this.sistolica = Integer.parseInt(partes[0].trim());
                this.diastolica = Integer.parseInt(partes[1].trim());
                this.categoria_presion = BPUtils.categorize(this.sistolica, this.diastolica);
            } catch (Exception e) {
                this.sistolica = 0;
                this.diastolica = 0;
                this.categoria_presion = "No válida";
            }
        } else {
            this.sistolica = 0;
            this.diastolica = 0;
            this.categoria_presion = "No válida";
        }

        this.enfermedades = enfermedades;
        this.medicamentos = medicamentos;
        this.timestamp = fechaIso;
    }

    /**
     * Convierte el objeto en un JSONObject (listo para enviar con OkHttp o n8n).
     */
    public JSONObject toJson() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("nombre", nombre);
            obj.put("edad", edad);
            obj.put("genero", genero);
            obj.put("contacto", contacto);
            obj.put("presion_arterial", presion_arterial);
            obj.put("sistolica", sistolica);
            obj.put("diastolica", diastolica);
            obj.put("categoria_presion", categoria_presion);
            obj.put("enfermedades", enfermedades);
            obj.put("medicamentos", medicamentos);
            obj.put("timestamp", timestamp);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public String toString() {
        return "MedicionPayload{" +
                "nombre='" + nombre + '\'' +
                ", edad=" + edad +
                ", genero='" + genero + '\'' +
                ", contacto='" + contacto + '\'' +
                ", presion_arterial='" + presion_arterial + '\'' +
                ", sistolica=" + sistolica +
                ", diastolica=" + diastolica +
                ", categoria_presion='" + categoria_presion + '\'' +
                ", enfermedades='" + enfermedades + '\'' +
                ", medicamentos='" + medicamentos + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}

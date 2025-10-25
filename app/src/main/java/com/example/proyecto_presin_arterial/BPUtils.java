package com.example.proyecto_presin_arterial;

public final class BPUtils {
    private BPUtils(){}

    /** Normaliza "120/80" (acepta "120 / 80"). Devuelve "" si no es válido. */
    public static String normalize(String raw) {
        if (raw == null) return "";
        String t = raw.trim().replaceAll("\\s*", "");
        if (!t.matches("^\\d{2,3}/\\d{2,3}$")) return "";
        String[] p = t.split("/");
        int s = parseSafe(p[0]);
        int d = parseSafe(p[1]);
        if (s < 60 || s > 260 || d < 30 || d > 180) return "";
        return s + "/" + d;
    }

    /** Categoriza presión de forma simple (coincide con tu Code en n8n). */
    public static String categorize(int sistolica, int diastolica) {
        if (sistolica < 90 || diastolica < 60) return "Baja";
        if (sistolica >= 140 || diastolica >= 90) return "Alta";
        if ((sistolica >= 120 && sistolica <= 139) || (diastolica >= 80 && diastolica <= 89))
            return "Prehipertensión";
        return "Normal";
    }

    private static int parseSafe(String n) {
        try { return Integer.parseInt(n); } catch (Exception e) { return -1; }
    }
}

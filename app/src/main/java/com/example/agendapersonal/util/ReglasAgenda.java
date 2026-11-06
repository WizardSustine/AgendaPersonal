package com.example.agendapersonal.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;

/** Reglas de calendario independientes de la interfaz y del almacenamiento. */
public final class ReglasAgenda {
    private ReglasAgenda() {}
    public static String estado(String fecha, String especial, LocalDate hoy) {
        if ("FINALIZADO".equals(especial) || "CANCELADO".equals(especial)
                || "POSTERGADO".equals(especial)) return especial;
        if (fecha == null || fecha.isEmpty()) return "SIN FECHA";
        try {
            long dias = ChronoUnit.DAYS.between(hoy, LocalDate.parse(fecha.substring(0, 10)));
            if (dias < 0) return "ATRASADO";
            if (dias == 0) return "CRITICO";
            if (dias <= 7) return "URGENTE";
            if (dias <= 30) return "PENDIENTE";
            return "MUCHO";
        } catch (RuntimeException e) { return "FECHA INVÁLIDA"; }
    }
    public static String validarValor(String tipo, String valor) {
        if (valor == null || valor.isEmpty()) return null;
        try {
            switch (tipo) {
                case "fecha": LocalDate.parse(valor); break;
                case "hora": LocalTime.parse(valor); break;
                case "fechaHora": LocalDateTime.parse(valor.replace(' ', 'T')); break;
                case "entero": if (Integer.parseInt(valor) < 0) return "Ingresá un número igual o mayor que cero"; break;
                case "decimal":
                    BigDecimal n = new BigDecimal(valor.replace(',', '.'));
                    if (n.signum() < 0 || !Double.isFinite(n.doubleValue())) return "Ingresá un importe o cantidad válido, igual o mayor que cero";
                    break;
            }
            return null;
        } catch (RuntimeException e) {
            if (tipo.equals("fecha")) return "Usá una fecha válida: AAAA-MM-DD";
            if (tipo.equals("hora")) return "Usá una hora válida: HH:mm";
            if (tipo.equals("fechaHora")) return "Usá fecha y hora válidas: AAAA-MM-DDTHH:mm";
            return "Ingresá un número válido";
        }
    }
    public static int prioridad(String p) {
        String[] niveles = {"ALTISIMA", "ALTA", "MEDIA", "BAJA", "BAJISIMA"};
        for (int i=0;i<niveles.length;i++) if(niveles[i].equals(p)) return i;
        return 2;
    }
}

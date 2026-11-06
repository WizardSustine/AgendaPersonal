
package com.example.agendapersonal.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class EstiloApp {

    private EstiloApp() {}

    public static final int FONDO = Color.rgb(250, 247, 244);
    public static final int SUPERFICIE = Color.WHITE;
    public static final int PRIMARIO = Color.rgb(104, 69, 119);
    public static final int PRIMARIO_OSCURO = Color.rgb(75, 47, 88);
    public static final int PRIMARIO_CLARO = Color.rgb(237, 224, 242);
    public static final int TEXTO = Color.rgb(47, 39, 54);
    public static final int SECUNDARIO = Color.rgb(119, 109, 122);
    public static final int BORDE = Color.rgb(226, 219, 226);
    public static final int VERDE = Color.rgb(77, 155, 91);
    public static final int ROJO = Color.rgb(204, 81, 83);
    public static final int AMARILLO = Color.rgb(215, 157, 53);
    public static final int AZUL = Color.rgb(65, 119, 203);

    public static int dp(Context context, float valor) {
        return Math.round(
                TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        valor,
                        context.getResources().getDisplayMetrics()
                )
        );
    }

    public static GradientDrawable fondo(
            int color,
            float radio,
            int colorBorde,
            Context context
    ) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radio));

        if (colorBorde != Color.TRANSPARENT) {
            drawable.setStroke(dp(context, 1), colorBorde);
        }

        return drawable;
    }

    public static TextView texto(
            Context context,
            String contenido,
            float tamaño,
            boolean negrita
    ) {
        TextView vista = new TextView(context);
        vista.setText(contenido);
        vista.setTextSize(tamaño);
        vista.setTextColor(Tema.color(context, com.example.agendapersonal.R.attr.agenda_text));
        vista.setGravity(Gravity.CENTER_VERTICAL);

        if (negrita) {
            vista.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }

        return vista;
    }

    public static TextView boton(
            Context context,
            String contenido
    ) {
        TextView vista = texto(context, contenido, 15, true);

        vista.setTextColor(Tema.color(context, com.example.agendapersonal.R.attr.agenda_button_text));
        vista.setGravity(Gravity.CENTER);
        vista.setPadding(
                dp(context, 18),
                dp(context, 13),
                dp(context, 18),
                dp(context, 13)
        );

        vista.setBackground(
                fondo(Tema.color(context, com.example.agendapersonal.R.attr.agenda_button), 14, Tema.color(context, com.example.agendapersonal.R.attr.agenda_button_border), context)
        );

        vista.setClickable(true);
        vista.setFocusable(true);
        return vista;
    }

    public static LinearLayout tarjeta(Context context) {
        LinearLayout tarjeta = new LinearLayout(context);
        tarjeta.setOrientation(LinearLayout.VERTICAL);

        int padding = dp(context, 16);
        tarjeta.setPadding(padding, padding, padding, padding);

        tarjeta.setBackground(
                fondo(Tema.color(context, com.example.agendapersonal.R.attr.agenda_surface), 20, Tema.color(context, com.example.agendapersonal.R.attr.agenda_border), context)
        );

        tarjeta.setElevation(dp(context, 2));
        return tarjeta;
    }

    public static LinearLayout.LayoutParams margen(
            Context context,
            int inferior
    ) {
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.bottomMargin = dp(context, inferior);
        return params;
    }
}

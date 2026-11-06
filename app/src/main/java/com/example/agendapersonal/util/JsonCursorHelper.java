package com.example.agendapersonal.util;

import android.database.Cursor;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

public final class JsonCursorHelper {

    private JsonCursorHelper() {
        // Clase utilitaria: no necesita instancias.
    }

    public static JSONObject toJsonObject(Cursor cursor)
            throws JSONException {

        if (cursor == null || cursor.isClosed()) {
            throw new IllegalArgumentException(
                    "El Cursor no puede ser nulo ni estar cerrado."
            );
        }

        JSONObject json = new JSONObject();

        for (int i = 0; i < cursor.getColumnCount(); i++) {

            String nombreColumna = cursor.getColumnName(i);

            if (cursor.isNull(i)) {
                json.put(nombreColumna, JSONObject.NULL);
                continue;
            }

            switch (cursor.getType(i)) {

                case Cursor.FIELD_TYPE_INTEGER:
                    json.put(nombreColumna, cursor.getLong(i));
                    break;

                case Cursor.FIELD_TYPE_FLOAT:
                    json.put(nombreColumna, cursor.getDouble(i));
                    break;

                case Cursor.FIELD_TYPE_STRING:
                    json.put(nombreColumna, cursor.getString(i));
                    break;

                case Cursor.FIELD_TYPE_BLOB:
                    json.put(
                            nombreColumna,
                            Base64.encodeToString(
                                    cursor.getBlob(i),
                                    Base64.NO_WRAP
                            )
                    );
                    break;

                case Cursor.FIELD_TYPE_NULL:
                    json.put(nombreColumna, JSONObject.NULL);
                    break;

                default:
                    throw new JSONException(
                            "Tipo de dato SQLite desconocido en la columna: "
                                    + nombreColumna
                    );
            }
        }

        return json;
    }
}

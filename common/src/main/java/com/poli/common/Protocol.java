package com.poli.common;

public final class Protocol {

    // Indica que el cliente se conecta con un nombre de usuario.
    public static final String LOGIN = "LOGIN";
    // Indica que el cliente envía un mensaje privado a otro usuario.
    public static final String MSG = "MSG";
    // Indica que el cliente se desconectó.
    public static final String LOGOUT = "LOGOUT";
    // Indica la lista de usuarios para los mensajes.
    public static final String USERS = "USERS";
    // Indica Error.
    public static final String ERROR = "ERROR";

    public static final String SEPARATOR = "|";
    public static final String USER_SEPARATOR = ",";

    private Protocol() {
    }

    /** Crea un mensaje para el socket, e.g. join(MSG, "poli01", "hola") -> "MSG|poli01|hola". */
    public static String join(String... parts) {
        return String.join(SEPARATOR, parts);
    }

    /** Divide la líne en 3 partes, para obtener el tipo de mensaje y el texto. */
    public static String[] split(String line) {
        return line.split("\\|", 3);
    }
}

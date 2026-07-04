package certificados;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Equivalente ao parser.py
 */
public class Parser {

    private static final Pattern PADRAO_CPF =
            Pattern.compile("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}");

    /** Equivalente a extrair_cpf() */
    public static String extrairCpf(String texto) {
        Matcher resultado = PADRAO_CPF.matcher(texto);

        if (resultado.find()) {
            return resultado.group().replace(".", "").replace("-", "");
        }

        return null;
    }

    /** Equivalente a identificar_tipo_pagina() */
    public static String identificarTipoPagina(String texto) {
        if (texto.contains("Certifico que") || texto.contains("CPF")) {
            return "FRENTE";
        }

        if (texto.contains("O(a) referido(a) aluno(a)")) {
            return "VERSO";
        }

        return "DESCONHECIDO";
    }

    /** Equivalente a extrair_nome_frente() */
    public static String extrairNomeFrente(String texto) {
        String inicio = "Certifico que";
        String fim = ", inscrita no CPF";

        if (texto.contains(inicio) && texto.contains(fim)) {
            String trecho = texto.substring(texto.indexOf(inicio) + inicio.length());

            if (!trecho.contains(fim)) {
                return null;
            }

            return trecho.substring(0, trecho.indexOf(fim)).strip();
        }

        return null;
    }

    /** Equivalente a extrair_nome_verso() */
    public static String extrairNomeVerso(String texto) {
        String inicio = "O(a) referido(a) aluno(a)";

        if (!texto.contains(inicio)) {
            return null;
        }

        String trecho = texto.substring(texto.indexOf(inicio) + inicio.length());

        if (!trecho.contains(",")) {
            return null;
        }

        return trecho.substring(0, trecho.indexOf(",")).strip();
    }
}

package certificados;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Equivalente ao utils.py
 */
public class Utils {

    private static final Set<String> PALAVRAS_MINUSCULAS =
            Set.of("da", "de", "do", "das", "dos", "e");

    /** Equivalente a formatar_nome() */
    public static String formatarNome(String nome) {
        String[] palavras = nome.toLowerCase().split("\\s+");
        List<String> resultado = new ArrayList<>();

        for (String palavra : palavras) {
            if (palavra.isEmpty()) continue;

            if (PALAVRAS_MINUSCULAS.contains(palavra)) {
                resultado.add(palavra);
            } else {
                resultado.add(Character.toUpperCase(palavra.charAt(0)) + palavra.substring(1));
            }
        }

        return String.join(" ", resultado);
    }

    /** Equivalente a normalizar_nome() */
    public static String normalizarNome(String nome) {
        return nome.toLowerCase().replaceAll("\\s+", " ").strip();
    }

    /**
     * Equivalente ao fuzz.ratio() do rapidfuzz.
     * Retorna a similaridade entre duas strings de 0 a 100,
     * calculada a partir da distancia de Levenshtein.
     */
    public static int similaridade(String a, String b) {
        int maxLen = Math.max(a.length(), b.length());
        if (maxLen == 0) return 100;

        int distancia = levenshtein(a, b);
        return (int) Math.round((1.0 - (double) distancia / maxLen) * 100);
    }

    /** Distancia de Levenshtein: numero minimo de edicoes para transformar a em b */
    private static int levenshtein(String a, String b) {
        int[] anterior = new int[b.length() + 1];
        int[] atual = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) {
            anterior[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            atual[0] = i;

            for (int j = 1; j <= b.length(); j++) {
                int custo = (a.charAt(i - 1) == b.charAt(j - 1)) ? 0 : 1;
                atual[j] = Math.min(
                        Math.min(atual[j - 1] + 1, anterior[j] + 1),
                        anterior[j - 1] + custo
                );
            }

            int[] temp = anterior;
            anterior = atual;
            atual = temp;
        }

        return anterior[b.length()];
    }
}

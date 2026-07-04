package certificados;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Equivalente ao main.py
 *
 * pypdf (PdfReader/PdfWriter)  ->  Apache PDFBox (PDDocument)
 * rapidfuzz (fuzz.ratio)       ->  Utils.similaridade()
 *
 * Os callbacks enviar_mensagem e atualizar_progresso viram
 * Consumer<String> e Consumer<Integer>.
 */
public class Main {

    /** Guarda uma pagina de verso encontrada: (numero_pagina, texto) */
    private record PaginaVerso(int numeroPagina, String texto) {}

    public static void processarCertificados(String arquivoPdf,
                                             Consumer<String> enviarMensagem,
                                             Consumer<Integer> atualizarProgresso) throws Exception {
        atualizarProgresso.accept(0);

        // try-with-resources: fecha o PDF automaticamente (como o "with" do Python)
        try (PDDocument reader = Loader.loadPDF(new File(arquivoPdf))) {

            Map<String, Aluno> alunos = new LinkedHashMap<>();
            List<PaginaVerso> versos = new ArrayList<>();

            PDFTextStripper stripper = new PDFTextStripper();
            int totalPaginas = reader.getNumberOfPages();

            // No PDFBox as paginas comecam em 1 no stripper,
            // mas em 0 no getPage(). Guardamos o indice 0-based.
            for (int numeroPagina = 0; numeroPagina < totalPaginas; numeroPagina++) {
                stripper.setStartPage(numeroPagina + 1);
                stripper.setEndPage(numeroPagina + 1);
                String texto = stripper.getText(reader);

                String tipo = Parser.identificarTipoPagina(texto);

                if (tipo.equals("FRENTE")) {
                    String nome = Parser.extrairNomeFrente(texto);
                    String cpf = Parser.extrairCpf(texto);

                    if (nome != null && cpf != null) {
                        Aluno aluno = new Aluno(
                                Utils.formatarNome(nome),
                                Utils.normalizarNome(nome),
                                cpf,
                                numeroPagina
                        );
                        alunos.put(cpf, aluno);
                    }

                } else if (tipo.equals("VERSO")) {
                    versos.add(new PaginaVerso(numeroPagina, texto));
                }
            }

            // Associa cada verso ao aluno com nome mais parecido (fuzzy matching)
            for (PaginaVerso verso : versos) {
                String nomeVerso = Parser.extrairNomeVerso(verso.texto());

                if (nomeVerso == null) {
                    continue;
                }

                String nomeComparacao = Utils.normalizarNome(nomeVerso);

                String melhorCpf = null;
                int maiorSimilaridade = 0;

                for (Map.Entry<String, Aluno> entrada : alunos.entrySet()) {
                    int similaridade = Utils.similaridade(nomeComparacao, entrada.getValue().nomeComparacao);

                    if (similaridade > maiorSimilaridade) {
                        maiorSimilaridade = similaridade;
                        melhorCpf = entrada.getKey();
                    }
                }

                if (maiorSimilaridade >= 85) {
                    alunos.get(melhorCpf).verso = verso.numeroPagina();
                }
            }

            enviarMensagem.accept("");
            enviarMensagem.accept("Analisando PDF...");
            enviarMensagem.accept("");
            enviarMensagem.accept(alunos.size() + " alunos encontrados.");
            enviarMensagem.accept("");

            List<Aluno> alunosOrdenados = new ArrayList<>(alunos.values());
            alunosOrdenados.sort(Comparator.comparing(aluno -> aluno.nome));

            for (Aluno aluno : alunosOrdenados) {
                enviarMensagem.accept(aluno.nome);
                enviarMensagem.accept("CPF: " + aluno.cpf);
                enviarMensagem.accept("");
            }

            enviarMensagem.accept("Criando certificados...");
            enviarMensagem.accept("");

            File pastaSaida = new File("certificados_processados");
            pastaSaida.mkdirs(); // equivalente ao os.makedirs(exist_ok=True)

            int indice = 0;
            for (Aluno aluno : alunosOrdenados) {
                indice++;
                int progresso = (int) (((double) indice / alunos.size()) * 100);
                atualizarProgresso.accept(progresso);

                if (aluno.frente == null) {
                    continue;
                }

                if (aluno.verso == null) {
                    enviarMensagem.accept("[AVISO] " + aluno.nome + " nao possui verso.");
                    continue;
                }

                try (PDDocument writer = new PDDocument()) {
                    writer.importPage(reader.getPage(aluno.frente));
                    writer.importPage(reader.getPage(aluno.verso));

                    // Equivalente ao writer.encrypt(cpf): protege o PDF com senha
                    AccessPermission permissoes = new AccessPermission();
                    StandardProtectionPolicy politica =
                            new StandardProtectionPolicy(aluno.cpf, aluno.cpf, permissoes);
                    politica.setEncryptionKeyLength(128);
                    writer.protect(politica);

                    File caminho = new File(pastaSaida, aluno.nome + ".pdf");
                    writer.save(caminho);
                }

                enviarMensagem.accept("[OK] " + aluno.nome + ".pdf");
            }

            atualizarProgresso.accept(100);
            enviarMensagem.accept("");
            enviarMensagem.accept("Processo finalizado!");
            enviarMensagem.accept("");
        }
    }

    /** Equivalente ao if __name__ == "__main__": roda no terminal, sem interface */
    public static void main(String[] args) throws Exception {
        processarCertificados(
                "certificados.pdf",
                texto -> System.out.println(texto),
                progresso -> {}
        );
    }
}

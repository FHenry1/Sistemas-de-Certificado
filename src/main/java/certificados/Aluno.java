package certificados;

/**
 * Em Python era um dicionario:
 * {"nome": ..., "nome_comparacao": ..., "cpf": ..., "frente": ..., "verso": ...}
 * Em Java, criamos uma classe para representar esses dados.
 */
public class Aluno {
    public String nome;
    public String nomeComparacao;
    public String cpf;
    public Integer frente; // indice da pagina frente (null se nao tiver)
    public Integer verso;  // indice da pagina verso (null se nao tiver)

    public Aluno(String nome, String nomeComparacao, String cpf, Integer frente) {
        this.nome = nome;
        this.nomeComparacao = nomeComparacao;
        this.cpf = cpf;
        this.frente = frente;
        this.verso = null;
    }
}

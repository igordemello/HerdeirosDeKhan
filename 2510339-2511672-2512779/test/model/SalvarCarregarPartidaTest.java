package model;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da 4ª iteração (parte do Igor): o Jogo junta o salvar e o carregar
 * das partes do Rafael e do Arthur num arquivo só, com a seção [PARTIDA]
 * (cores, vez e turnos que faltam para o fim).
 *
 * Partida com o mapa completo e semente fixa. Jogadores, nesta ordem:
 * AMARELO (Ogedei), AZUL (Altani) e ROXO (Tolui). O arquivo é escrito num
 * StringWriter e lido de um StringReader, sem criar arquivos no disco.
 */
public class SalvarCarregarPartidaTest {

	private Jogo jogo;

	@Before
	public void setUp() {
		jogo = Jogo.novaPartida(Arrays.asList(Cor.AMARELO, Cor.AZUL, Cor.ROXO),
				FabricaTabuleiro.criarTabuleiroPadrao(), new Random(7));
		for (String cor : jogo.getCores()) {
			jogo.escolherConselheiroSecreto(cor, jogo.getCartasSecretasParaEscolher(cor).get(0));
		}
	}

	private static String salvar(Jogo j) {
		StringWriter texto = new StringWriter();
		PrintWriter saida = new PrintWriter(texto);
		j.salvar(saida);
		saida.flush();
		return texto.toString();
	}

	/** Carrega o texto como se fosse o arquivo, num mapa novo e com outra semente. */
	private static Jogo carregar(String texto) throws IOException {
		return Jogo.carregar(new StringReader(texto), FabricaTabuleiro.criarTabuleiroPadrao(), new Random(99));
	}

	/**
	 * Objetivo: verificar a ida e volta da partida inteira pelo Jogo.
	 * Cenário: AMARELO ativa uma coluna, gasta uma moeda e passa a vez; a
	 * partida é salva no começo do turno de AZUL e carregada.
	 * Resultado esperado: o arquivo é texto ASCII puro (enunciado); a partida
	 * carregada passa a ser a instância (Singleton), tem as mesmas cores, a
	 * vez é de AZUL e salvar de novo dá exatamente o mesmo texto.
	 */
	@Test
	public void idaEVoltaDaPartidaInteira() throws IOException {
		assertTrue(jogo.ativarColuna("VERMELHA"));
		assertTrue(jogo.gastarTributoDaVez("MOEDA", 1));
		jogo.passarVez();
		String texto = salvar(jogo);
		assertTrue("o arquivo tem de ser ASCII puro", texto.matches("\\p{ASCII}*"));

		Jogo outro = carregar(texto);
		assertSame(outro, Jogo.getInstancia());
		assertNotSame(jogo, outro);
		assertEquals(jogo.getCores(), outro.getCores());
		assertEquals("AZUL", outro.getJogadorDaVez());
		assertEquals(texto, salvar(outro));
		assertTrue(outro.podeSalvar());
	}

	/**
	 * Objetivo: verificar que só se salva no começo do turno.
	 * Cenário: partida nova (os conselheiros secretos já escolhidos); AMARELO
	 * ativa uma coluna; depois passa a vez.
	 * Resultado esperado: antes da ativação pode salvar; depois dela não
	 * pode, e salvar lança IllegalStateException sem criar o arquivo; com a
	 * vez passada, AZUL pode salvar.
	 */
	@Test
	public void soSalvaNoComecoDoTurno() throws IOException {
		assertTrue("escolher o conselheiro é preparação, não jogada", jogo.podeSalvar());
		assertTrue(jogo.ativarColuna("VERMELHA"));
		assertFalse(jogo.podeSalvar());

		File arquivo = new File("nao-deve-existir.txt");
		try {
			jogo.salvar(arquivo);
			fail("não deveria salvar no meio do turno");
		} catch (IllegalStateException esperado) {
		}
		assertFalse(arquivo.exists());

		jogo.passarVez();
		assertTrue(jogo.podeSalvar());
	}

	/**
	 * Objetivo: verificar a contagem do fim de jogo (pág. 14) e que ela vai
	 * para o arquivo.
	 * Cenário: na vez de AZUL (2º a jogar), AMARELO chega a 14 votos (limite
	 * para 3 jogadores) e AZUL passa a vez; a partida é salva e carregada; na
	 * partida carregada todos passam a vez.
	 * Resultado esperado: faltam 4 turnos (ROXO termina a rodada e todos
	 * jogam mais uma vez), também na partida carregada; a partida acaba no
	 * 4º passe, e não antes.
	 */
	@Test
	public void fimDeJogoContaOsTurnosEVaiParaOArquivo() throws IOException {
		jogo.passarVez();
		jogo.getJogador(Cor.AMARELO).ganharVotos(14);
		assertEquals(-1, jogo.getTurnosAteOFim());
		jogo.passarVez();
		assertEquals(4, jogo.getTurnosAteOFim());

		Jogo outro = carregar(salvar(jogo));
		assertEquals(4, outro.getTurnosAteOFim());
		for (int i = 1; i <= 3; i++) {
			outro.passarVez();
			assertFalse("passe " + i, outro.partidaAcabou());
		}
		outro.passarVez();
		assertTrue(outro.partidaAcabou());
	}

	/**
	 * Objetivo: verificar que um arquivo inválido é recusado sem estragar a
	 * partida em andamento.
	 * Entrada: (1) cabeçalho de outro jogo; (2) arquivo sem a seção
	 * [PARTIDA]; (3) vez de um 4º jogador que não existe; (4) cor inválida.
	 * Resultado esperado: IOException nos quatro casos, e a instância
	 * continua sendo a partida que já existia.
	 */
	@Test
	public void arquivoInvalidoNaoTrocaAPartida() {
		String texto = salvar(jogo);
		String[] errados = { "OUTRO JOGO\n[PARTIDA]\n", texto.replace("[PARTIDA]", "[NADA]"),
				texto.replace("vez=0", "vez=3"), texto.replace("cores=AMARELO", "cores=PRETO") };
		for (String errado : errados) {
			try {
				carregar(errado);
				fail("deveria recusar: " + errado.substring(0, 30));
			} catch (IOException esperado) {
			}
			assertSame(jogo, Jogo.getInstancia());
		}
	}
}

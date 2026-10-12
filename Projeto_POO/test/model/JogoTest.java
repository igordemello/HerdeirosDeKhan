package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes de integração da API do Model ({@link Jogo}): movimentação dos
 * peões, fim de jogo e definição do vencedor.
 *
 * Os testes usam um mapa pequeno, montado aqui mesmo, para que cada
 * resultado esperado possa ser conferido à mão:
 *
 * <pre>
 *   R1 ---- KARAKORUM ---- C1 (dupla) ---- P1 ---- P2
 * </pre>
 *
 * Jogadores, nesta ordem: AZUL (Altani, 2 peões), VERDE (Jochi) e ROXO
 * (Tolui). Depois da preparação todos os peões estão em Karakorum.
 */
public class JogoTest {
	private static final String K = Tabuleiro.ID_KARAKORUM;

	private Tabuleiro tabuleiro;
	private Jogo jogo;

	@Before
	public void criarPartida() {
		tabuleiro = new Tabuleiro();
		tabuleiro.adicionarParada("R1", TipoParada.SIMPLES, Regiao.RUSSIA);
		tabuleiro.adicionarParada("C1", TipoParada.DUPLA, Regiao.CHINA);
		tabuleiro.adicionarParada("P1", TipoParada.SIMPLES, Regiao.PERSIA);
		tabuleiro.adicionarParada("P2", TipoParada.SIMPLES, Regiao.PERSIA);
		tabuleiro.ligarParadas("R1", K);
		tabuleiro.ligarParadas(K, "C1");
		tabuleiro.ligarParadas("C1", "P1");
		tabuleiro.ligarParadas("P1", "P2");
		jogo = new Jogo(Arrays.asList(Cor.AZUL, Cor.VERDE, Cor.ROXO), tabuleiro, new Random(42));
	}

	/**
	 * Objetivo: verificar que o peão só anda por rotas existentes e no
	 * máximo o número de movimentos que o jogador tem.
	 *
	 * Cenário: o peão 1 de AZUL, em Karakorum, tenta ir até P2 passando por
	 * C1 (C1 e P2 não são vizinhas), depois até P1 por C1 com 1 movimento e,
	 * por fim, até P1 com 2 movimentos.
	 *
	 * Resultado esperado: as duas primeiras tentativas são recusadas e o
	 * peão continua em Karakorum; a última é aceita e o peão sai de
	 * Karakorum e fica em P1.
	 */
	@Test
	public void moverPeaoRespeitaRotasEQuantidadeDeMovimentos() {
		Peao azul1 = new Peao(Cor.AZUL, 1);

		assertFalse("C1 e P2 não são vizinhas", jogo.moverPeao("AZUL", 1, Arrays.asList("C1", "P2"), 5));
		assertFalse("P1 fica a 2 movimentos", jogo.moverPeao("AZUL", 1, Arrays.asList("C1", "P1"), 1));
		assertSame(tabuleiro.getKarakorum(), tabuleiro.getParadaDoPeao(azul1));

		assertTrue(jogo.moverPeao("AZUL", 1, Arrays.asList("C1", "P1"), 2));
		assertEquals("P1", tabuleiro.getParadaDoPeao(azul1).getId());
		assertEquals(3, tabuleiro.contarPeoes(K));
		assertEquals(1, tabuleiro.contarPeoes("P1"));
	}

	/**
	 * Objetivo: verificar as regras de passagem (pág. 7): o peão pode passar
	 * por uma parada ocupada, mas não pode terminar nela, e pular um yurt
	 * próprio não gasta movimento.
	 *
	 * Cenário: VERDE vai para P1 (parada simples) e AZUL tem um yurt em C1.
	 * O peão 1 de AZUL tenta terminar em P1; ROXO tenta ir até P2 com 2
	 * movimentos; o peão 1 de AZUL tenta ir até P2 com 2 movimentos.
	 *
	 * Resultado esperado: AZUL não pode terminar em P1; ROXO não chega a P2,
	 * pois precisaria de 3 movimentos; AZUL chega a P2 com 2, porque o yurt
	 * dele em C1 é pulado de graça e P1 é só atravessada.
	 */
	@Test
	public void peaoPassaPorParadaOcupadaEPulaYurtProprio() {
		assertTrue(jogo.moverPeao("VERDE", 1, Arrays.asList("C1", "P1"), 2));
		tabuleiro.getParada("C1").adicionarYurt(Cor.AZUL);

		assertFalse("P1 já está ocupada", jogo.moverPeao("AZUL", 1, Arrays.asList("C1", "P1"), 5));
		assertFalse("O yurt em C1 não é de ROXO", jogo.moverPeao("ROXO", 1, Arrays.asList("C1", "P1", "P2"), 2));

		assertTrue(jogo.moverPeao("AZUL", 1, Arrays.asList("C1", "P1", "P2"), 2));
		assertEquals("P2", tabuleiro.getParadaDoPeao(new Peao(Cor.AZUL, 1)).getId());
		assertEquals("VERDE continua em P1", 1, tabuleiro.contarPeoes("P1"));
	}

	/**
	 * Objetivo: verificar o disparo do fim de jogo e a definição do
	 * vencedor, incluindo o desempate pela distância até Karakorum.
	 *
	 * Cenário: AZUL e VERDE com 5 votos e ROXO com 3 (total 13); depois ROXO
	 * ganha mais 1 voto (total 14, o limite para 3 jogadores). Por fim os
	 * peões de AZUL vão para P2 (distância 3) e C1 (distância 1) e VERDE vai
	 * para P1 (distância 2).
	 *
	 * Resultado esperado: com 13 votos o fim não foi disparado; com 14 foi.
	 * Com todos em Karakorum, AZUL e VERDE compartilham a vitória; depois
	 * dos movimentos AZUL vence sozinho, pois vale o peão dele mais perto
	 * de Karakorum.
	 */
	@Test
	public void vencedorTemMaisVotosEDesempataPelaDistanciaAKarakorum() {
		jogo.getJogador(Cor.AZUL).ganharVotos(5);
		jogo.getJogador(Cor.VERDE).ganharVotos(5);
		jogo.getJogador(Cor.ROXO).ganharVotos(3);

		assertFalse(jogo.fimDeJogoDisparado());
		assertEquals(Arrays.asList("AZUL", "VERDE"), jogo.getVencedores());

		jogo.getJogador(Cor.ROXO).ganharVotos(1);
		assertTrue(jogo.fimDeJogoDisparado());

		assertTrue(jogo.moverPeao("AZUL", 1, Arrays.asList("C1", "P1", "P2"), 3));
		assertTrue(jogo.moverPeao("AZUL", 2, Arrays.asList("C1"), 1));
		assertTrue(jogo.moverPeao("VERDE", 1, Arrays.asList("C1", "P1"), 2));
		assertEquals(Arrays.asList("AZUL"), jogo.getVencedores());
	}
}

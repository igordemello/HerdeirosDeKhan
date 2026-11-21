package model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/** Testes unitários da classe {@link GerenciadorJogadores}. */
public class GerenciadorJogadoresTest {

	/**
	 * Caso de teste 7 — Recursos iniciais com 5 jogadores.
	 * Objetivo: verificar a distribuição de moedas da preparação (1 moeda
	 * para o 1º e o 2º a jogar, 2 para os demais) e os demais recursos.
	 * Entrada: cores VERDE, AZUL, ROXO, AMARELO, VERMELHO, nessa ordem.
	 * Resultado esperado: moedas 1, 1, 2, 2, 2; todos com 12 yurts, 4 fichas
	 * de ativação, 0 espadas, 0 yurts de tributo e 0 votos.
	 */
	@Test
	public void moedasIniciaisDependemDaOrdemDeJogo() {
		GerenciadorJogadores g = new GerenciadorJogadores(
				Arrays.asList(Cor.VERDE, Cor.AZUL, Cor.ROXO, Cor.AMARELO, Cor.VERMELHO));
		List<Jogador> ordem = g.getJogadoresEmOrdem();
		int[] moedasEsperadas = { 1, 1, 2, 2, 2 };

		assertEquals(5, ordem.size());
		for (int i = 0; i < ordem.size(); i++) {
			Jogador j = ordem.get(i);
			assertEquals("moedas do jogador " + i, moedasEsperadas[i], j.getQuantidadeTributo(TipoTributo.MOEDA));
			assertEquals(12, j.getYurtsDisponiveis());
			assertEquals(4, j.getFichasAtivacaoDisponiveis());
			assertEquals(0, j.getQuantidadeTributo(TipoTributo.ESPADA));
			assertEquals(0, j.getQuantidadeTributo(TipoTributo.YURT));
			assertEquals(0, j.getVotos());
		}
		assertSame(g.getJogador(Cor.VERDE), g.getPrimeiroJogador());
	}

	/**
	 * Caso de teste 8 — Validação do número de jogadores e das cores.
	 * Objetivo: verificar que o jogo só aceita de 2 a 5 jogadores e que duas
	 * pessoas não podem escolher a mesma cor.
	 * Entrada: 1 jogador; 6 cores (com repetição); 3 jogadores com cor repetida.
	 * Resultado esperado: IllegalArgumentException nos três casos.
	 */
	@Test
	public void numeroDeJogadoresInvalidoOuCorRepetidaSaoRecusados() {
		assertConstrucaoFalha(Arrays.asList(Cor.AZUL));
		assertConstrucaoFalha(Arrays.asList(Cor.AZUL, Cor.VERMELHO, Cor.VERDE, Cor.AMARELO, Cor.ROXO, Cor.AZUL));
		assertConstrucaoFalha(Arrays.asList(Cor.AZUL, Cor.VERDE, Cor.AZUL));
	}

	private void assertConstrucaoFalha(List<Cor> cores) {
		try {
			new GerenciadorJogadores(cores);
			fail("Deveria recusar: " + cores);
		} catch (IllegalArgumentException e) {
			// esperado
		}
	}

	/**
	 * Caso de teste 9 — Passagem de vez em sentido horário.
	 * Objetivo: verificar que a vez segue a ordem de jogo e volta ao
	 * primeiro jogador depois do último.
	 * Entrada: 3 jogadores (AMARELO, ROXO, AZUL); passarVez() 3 vezes.
	 * Resultado esperado: AMARELO -> ROXO -> AZUL -> AMARELO.
	 */
	@Test
	public void passarVezVoltaAoPrimeiroDepoisDoUltimo() {
		GerenciadorJogadores g = new GerenciadorJogadores(Arrays.asList(Cor.AMARELO, Cor.ROXO, Cor.AZUL));
		assertEquals(Cor.AMARELO, g.getJogadorDaVez().getCor());
		assertEquals(Cor.ROXO, g.passarVez().getCor());
		assertEquals(Cor.AZUL, g.passarVez().getCor());
		assertEquals(Cor.AMARELO, g.passarVez().getCor());
	}
}

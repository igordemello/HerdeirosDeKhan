package model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/** Testes do tabuleiro do jogador e da ativação de colunas pela API {@link Jogo}. */
public class TabuleiroJogadorTest {

	private TabuleiroJogador tab;

	@Before
	public void setUp() {
		tab = new TabuleiroJogador(Herdeiro.TOLUI);
	}

	/**
	 * Objetivo: verificar que uma coluna com ficha não pode ser ativada de
	 * novo e que a coluna branca devolve todas as fichas.
	 * Entrada: ativar AMARELA, VERDE, AMARELA de novo e depois BRANCA.
	 * Resultado esperado: 2 fichas livres após as duas primeiras; a 2ª
	 * ativação da AMARELA retorna false; após a BRANCA, 4 fichas livres e
	 * nenhuma coluna ocupada.
	 */
	@Test
	public void colunaBrancaRecuperaTodasAsFichas() {
		assertTrue(tab.ativar(ColunaJogador.AMARELA));
		assertTrue(tab.ativar(ColunaJogador.VERDE));
		assertEquals(2, tab.getFichasDisponiveis());
		assertFalse(tab.ativar(ColunaJogador.AMARELA));

		assertTrue(tab.ativar(ColunaJogador.BRANCA));
		assertEquals(4, tab.getFichasDisponiveis());
		for (ColunaJogador c : ColunaJogador.values()) {
			assertFalse(tab.colunaOcupada(c));
		}
	}

	/**
	 * Objetivo: verificar que os cavalos da coluna somam a base e as
	 * melhorias, e que a coluna aceita no máximo 4 melhorias.
	 * Entrada: VERDE de Tolui (2 cavalos) recebe DOIS_CAVALOS, PUNHO e mais 3 melhorias.
	 * Resultado esperado: 4 cavalos e 2 punhos; a 5ª melhoria é recusada.
	 */
	@Test
	public void cavalosSomamBaseEMelhorias() {
		assertEquals(2, tab.getCavalos(ColunaJogador.VERDE));
		assertTrue(tab.adicionarMelhoria(ColunaJogador.VERDE, TipoMelhoria.DOIS_CAVALOS));
		assertTrue(tab.adicionarMelhoria(ColunaJogador.VERDE, TipoMelhoria.PUNHO));
		assertEquals(4, tab.getCavalos(ColunaJogador.VERDE));
		assertEquals(2, tab.getPunhos(ColunaJogador.VERDE));

		assertTrue(tab.adicionarMelhoria(ColunaJogador.VERDE, TipoMelhoria.MOEDA_VIRTUAL));
		assertTrue(tab.adicionarMelhoria(ColunaJogador.VERDE, TipoMelhoria.YURT_VIRTUAL));
		assertFalse(tab.adicionarMelhoria(ColunaJogador.VERDE, TipoMelhoria.DOIS_CAVALOS));
		assertEquals(4, tab.getCavalos(ColunaJogador.VERDE));
	}

	/**
	 * Objetivo: verificar pela API que ativar uma coluna define os
	 * movimentos do turno, que só se ativa uma coluna por turno e que passar
	 * a vez zera o estado do turno.
	 * Entrada: partida AZUL x VERDE; AZUL ativa VERDE, tenta AMARELA; passa a vez.
	 * Resultado esperado: 2 movimentos; 2ª ativação recusada; depois de
	 * passar a vez, 0 movimentos, nenhuma coluna ativada e a coluna VERDE
	 * de AZUL continua ocupada.
	 */
	@Test
	public void ativarColunaDefineMovimentosUmaVezPorTurno() {
		Jogo jogo = new Jogo(Arrays.asList(Cor.AZUL, Cor.VERDE), new Tabuleiro(), new Random(1));
		assertTrue(jogo.ativarColuna("VERDE"));
		assertEquals(2, jogo.getMovimentosDaVez());
		assertEquals("VERDE", jogo.getColunaAtivadaNaVez());
		assertFalse(jogo.ativarColuna("AMARELA"));

		jogo.definirMovimentosDaVez(5);
		assertEquals(5, jogo.getMovimentosDaVez());

		assertEquals("VERDE", jogo.passarVez());
		assertEquals(0, jogo.getMovimentosDaVez());
		assertNull(jogo.getColunaAtivadaNaVez());
		assertTrue(jogo.colunaOcupada("AZUL", "VERDE"));
		assertTrue(jogo.podeAtivarColuna("VERDE")); // a coluna do jogador VERDE está livre
	}

	/**
	 * Objetivo: verificar que as ações da base mudam conforme o herdeiro,
	 * como nos tabuleiros reais.
	 * Entrada: coluna VERMELHA de Tolui, Altani e Ogedei.
	 * Resultado esperado: Tolui 1 cavalo (sem Khan), Altani 1 cavalo e Khan,
	 * Ogedei 2 cavalos; a BRANCA tem 2 cavalos e Khan em todos.
	 */
	@Test
	public void acoesDaBaseDependemDoHerdeiro() {
		TabuleiroJogador altani = new TabuleiroJogador(Herdeiro.ALTANI);
		TabuleiroJogador ogedei = new TabuleiroJogador(Herdeiro.OGEDEI);

		assertEquals(1, tab.getCavalos(ColunaJogador.VERMELHA));
		assertFalse(tab.usaKhan(ColunaJogador.VERMELHA));
		assertEquals(1, altani.getCavalos(ColunaJogador.VERMELHA));
		assertTrue(altani.usaKhan(ColunaJogador.VERMELHA));
		assertEquals(2, ogedei.getCavalos(ColunaJogador.VERMELHA));

		for (TabuleiroJogador t : new TabuleiroJogador[] { tab, altani, ogedei }) {
			assertEquals(2, t.getCavalos(ColunaJogador.BRANCA));
			assertTrue(t.usaKhan(ColunaJogador.BRANCA));
		}
	}

	/**
	 * Objetivo: verificar a regra de cor das melhorias e o id usado pela View.
	 * Entrada: peça CAV verde na coluna VERMELHA, depois na VERDE e na BRANCA.
	 * Resultado esperado: recusada na VERMELHA; aceita na VERDE e na BRANCA,
	 * com id "CAV-V"; tesouro virtual de ferro só existe vermelho.
	 */
	@Test
	public void corDaMelhoriaPrecisaCombinarComAColuna() {
		PecaMelhoria cavalosVerde = new PecaMelhoria(TipoMelhoria.DOIS_CAVALOS, ColunaJogador.VERDE);
		assertEquals("CAV-V", cavalosVerde.getId());

		assertFalse(tab.adicionarMelhoria(ColunaJogador.VERMELHA, cavalosVerde));
		assertTrue(tab.adicionarMelhoria(ColunaJogador.VERDE, cavalosVerde));
		assertTrue(tab.adicionarMelhoria(ColunaJogador.BRANCA, cavalosVerde));
		assertEquals(4, tab.getCavalos(ColunaJogador.BRANCA));

		try {
			new PecaMelhoria(TipoMelhoria.TESOURO_VIRTUAL_FERRO, ColunaJogador.VERDE);
			fail("O tesouro virtual de ferro só existe vermelho");
		} catch (IllegalArgumentException e) {
			assertEquals("MOB-R", new PecaMelhoria(TipoMelhoria.TESOURO_VIRTUAL_FERRO, ColunaJogador.VERMELHA).getId());
		}
	}
}

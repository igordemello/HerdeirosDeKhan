package model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/** Testes unitários da classe {@link DistribuidorCartas}. */
public class DistribuidorCartasTest {

	private GerenciadorJogadores gerenciador;
	private DistribuidorCartas distribuidor;
	private List<Jogador> jogadores;

	@Before
	public void setUp() {
		gerenciador = new GerenciadorJogadores(
				Arrays.asList(Cor.AZUL, Cor.VERMELHO, Cor.VERDE, Cor.AMARELO, Cor.ROXO));
		jogadores = gerenciador.getJogadoresEmOrdem();
		distribuidor = new DistribuidorCartas(new Random(2026));
	}

	/**
	 * Caso de teste 10 — Preparação das cartas com 5 jogadores.
	 * Objetivo: verificar que cada jogador recebe 2 cartas secretas, que
	 * nenhuma carta é dada a dois jogadores e que 4 cartas abertas distintas
	 * são reveladas, sobrando o restante nos baralhos.
	 * Entrada: 5 jogadores; prepararCartas().
	 * Resultado esperado: 2 cartas por jogador; 10 cartas secretas
	 * distintas no total; baralho secreto vazio; 4 cartas abertas
	 * distintas reveladas; 10 cartas abertas não usadas no baralho.
	 */
	@Test
	public void preparacaoDaDuasSecretasPorJogadorERevelaQuatroAbertas() {
		distribuidor.prepararCartas(jogadores);

		Set<Integer> idsDistribuidos = new HashSet<Integer>();
		for (Jogador j : jogadores) {
			assertEquals(2, j.getCartasSecretasRecebidas().size());
			for (CartaConselheiroSecreto c : j.getCartasSecretasRecebidas()) {
				assertTrue("Carta repetida: " + c, idsDistribuidos.add(c.getId()));
			}
		}
		assertEquals(10, idsDistribuidos.size());
		assertTrue(distribuidor.getBaralhoSecreto().estaVazio());

		Set<TipoConselheiroAberto> tiposRevelados = new HashSet<TipoConselheiroAberto>();
		for (CartaConselheiroAberto c : distribuidor.getCartasAbertasDisponiveis()) {
			tiposRevelados.add(c.getTipo());
		}
		assertEquals(4, tiposRevelados.size());
		assertEquals(10, distribuidor.getBaralhoAberto().tamanho());
	}

	/**
	 * Caso de teste 11 — Preparação não pode ser repetida.
	 * Objetivo: garantir que as cartas não são distribuídas duas vezes.
	 * Entrada: prepararCartas() chamado duas vezes.
	 * Resultado esperado: IllegalStateException na 2ª chamada, sem alterar
	 * as mãos dos jogadores.
	 */
	@Test
	public void prepararDuasVezesLancaExcecao() {
		distribuidor.prepararCartas(jogadores);
		try {
			distribuidor.prepararCartas(jogadores);
			fail("Deveria lançar IllegalStateException");
		} catch (IllegalStateException e) {
			for (Jogador j : jogadores) {
				assertEquals(2, j.getCartasSecretasRecebidas().size());
			}
		}
	}

	/**
	 * Caso de teste 12 — Descarte das cartas secretas não escolhidas.
	 * Objetivo: verificar que, depois que todos escolhem, cada um fica com
	 * 1 carta e as não escolhidas vão para o descarte do baralho secreto.
	 * Entrada: 5 jogadores escolhem a 1ª carta da sua mão.
	 * Resultado esperado: todos com conselheiro escolhido; 5 cartas no
	 * descarte, nenhuma delas igual a um conselheiro escolhido.
	 */
	@Test
	public void cartasSecretasNaoEscolhidasVaoParaODescarte() {
		distribuidor.prepararCartas(jogadores);
		assertFalse(distribuidor.todosEscolheramConselheiroSecreto(jogadores));

		Set<CartaConselheiroSecreto> escolhidas = new HashSet<CartaConselheiroSecreto>();
		for (Jogador j : jogadores) {
			int id = j.getCartasSecretasRecebidas().iterator().next().getId();
			distribuidor.escolherConselheiroSecreto(j, id);
			escolhidas.add(j.getConselheiroSecreto());
		}

		assertTrue(distribuidor.todosEscolheramConselheiroSecreto(jogadores));
		List<CartaConselheiroSecreto> descarte = distribuidor.getBaralhoSecreto().getDescarte();
		assertEquals(5, descarte.size());
		for (CartaConselheiroSecreto c : descarte) {
			assertFalse(escolhidas.contains(c));
		}
	}

	/**
	 * Caso de teste 13 — Entrega de carta de conselheiro aberto.
	 * Objetivo: verificar que o primeiro jogador a cumprir a demanda recebe
	 * a carta e 1 voto, que nenhuma carta nova é revelada e que um segundo
	 * jogador não pode mais receber a mesma carta.
	 * Entrada: AZUL cumpre uma carta revelada; depois VERMELHO tenta a mesma.
	 * Resultado esperado: AZUL com 1 voto e 1 carta; 3 cartas abertas
	 * disponíveis e baralho aberto inalterado (10); VERMELHO recebe false
	 * e continua com 0 votos.
	 */
	@Test
	public void apenasOPrimeiroACumprirRecebeACartaAberta() {
		distribuidor.prepararCartas(jogadores);
		Jogador azul = gerenciador.getJogador(Cor.AZUL);
		Jogador vermelho = gerenciador.getJogador(Cor.VERMELHO);
		CartaConselheiroAberto carta = distribuidor.getCartasAbertasDisponiveis().iterator().next();

		assertSame(carta, distribuidor.getCartaAbertaDisponivel(carta.getTipo()));
		assertTrue(distribuidor.entregarCartaAberta(azul, carta.getId()));
		assertEquals(1, azul.getVotos());
		assertTrue(azul.getCartasAbertasConquistadas().contains(carta));
		assertEquals(3, distribuidor.getCartasAbertasDisponiveis().size());
		assertEquals(10, distribuidor.getBaralhoAberto().tamanho());
		assertNull(distribuidor.getCartaAbertaDisponivel(carta.getTipo()));

		assertFalse(distribuidor.entregarCartaAberta(vermelho, carta.getId()));
		assertEquals(0, vermelho.getVotos());
	}
}

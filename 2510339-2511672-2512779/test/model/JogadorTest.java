package model;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

/** Testes unitários da classe {@link Jogador}. */
public class JogadorTest {

	private Jogador jogador;

	@Before
	public void setUp() {
		jogador = new Jogador(Cor.AZUL);
	}

	/**
	 * Caso de teste 3 — Gasto de tributo sem saldo suficiente.
	 * Objetivo: verificar que um gasto maior que o saldo é recusado sem
	 * alterar o estado, e que um gasto válido desconta o valor correto.
	 * Entrada: jogador com 3 espadas tenta gastar 4 e depois 2.
	 * Resultado esperado: 1º gasto retorna false e mantém 3 espadas;
	 * 2º retorna true e deixa 1 espada; outros tributos ficam em 0.
	 */
	@Test
	public void gastarTributoSemSaldoNaoAlteraEstado() {
		jogador.adicionarTributo(TipoTributo.ESPADA, 3);

		assertFalse(jogador.gastarTributo(TipoTributo.ESPADA, 4));
		assertEquals(3, jogador.getQuantidadeTributo(TipoTributo.ESPADA));

		assertTrue(jogador.gastarTributo(TipoTributo.ESPADA, 2));
		assertEquals(1, jogador.getQuantidadeTributo(TipoTributo.ESPADA));
		assertEquals(0, jogador.getQuantidadeTributo(TipoTributo.MOEDA));
	}

	/**
	 * Caso de teste 4 — Escolha do conselheiro secreto.
	 * Objetivo: verificar que, das 2 cartas recebidas, o jogador fica só com
	 * a escolhida e a outra é devolvida para descarte; e que não é possível
	 * escolher novamente nem escolher uma carta que não possui.
	 * Entrada: jogador recebe Viajante (#1) e Soldado (#2) e escolhe #2.
	 * Resultado esperado: conselheiro = Soldado; devolvida = [Viajante];
	 * mão de cartas recebidas vazia; nova escolha lança IllegalStateException;
	 * escolher #99 (antes de escolher) lança IllegalArgumentException.
	 */
	@Test
	public void escolherConselheiroSecretoMantemUmaEDevolveAOutra() {
		CartaConselheiroSecreto viajante = new CartaConselheiroSecreto(1, TipoConselheiroSecreto.VIAJANTE);
		CartaConselheiroSecreto soldado = new CartaConselheiroSecreto(2, TipoConselheiroSecreto.SOLDADO);
		jogador.receberCartaSecreta(viajante);
		jogador.receberCartaSecreta(soldado);

		try {
			jogador.escolherConselheiroSecreto(99);
			fail("Deveria recusar carta que o jogador não possui");
		} catch (IllegalArgumentException e) {
			assertFalse(jogador.jaEscolheuConselheiroSecreto());
		}

		Collection<CartaConselheiroSecreto> devolvidas = jogador.escolherConselheiroSecreto(2);
		assertSame(soldado, jogador.getConselheiroSecreto());
		assertEquals(1, devolvidas.size());
		assertTrue(devolvidas.contains(viajante));
		assertTrue(jogador.getCartasSecretasRecebidas().isEmpty());

		try {
			jogador.escolherConselheiroSecreto(1);
			fail("Não deveria permitir uma segunda escolha");
		} catch (IllegalStateException e) {
			assertSame(soldado, jogador.getConselheiroSecreto());
		}
	}

	/**
	 * Caso de teste 5 — Votos do conselheiro secreto no fim do jogo.
	 * Objetivo: verificar o cálculo de 0 a 3 votos pelos limiares da carta,
	 * incluindo valores exatamente nos limiares e acima do último.
	 * Entrada: Viajante (3/6/8) com 2, 3, 7, 8 e 20; Sacerdote (1/2/4) com 0 e 3.
	 * Resultado esperado: Viajante 0, 1, 2, 3, 3; Sacerdote 0 e 2.
	 */
	@Test
	public void calcularVotosDoConselheiroSecretoRespeitaLimiares() {
		CartaConselheiroSecreto viajante = new CartaConselheiroSecreto(1, TipoConselheiroSecreto.VIAJANTE);
		assertEquals(0, viajante.calcularVotos(2));
		assertEquals(1, viajante.calcularVotos(3));
		assertEquals(2, viajante.calcularVotos(7));
		assertEquals(3, viajante.calcularVotos(8));
		assertEquals(3, viajante.calcularVotos(20));

		CartaConselheiroSecreto sacerdote = new CartaConselheiroSecreto(4, TipoConselheiroSecreto.SACERDOTE);
		assertEquals(0, sacerdote.calcularVotos(0));
		assertEquals(2, sacerdote.calcularVotos(3));
	}

	/**
	 * Caso de teste 6 — Fim das peças de yurt.
	 * Objetivo: verificar que o jogador começa com 12 yurts e não pode
	 * retirar mais depois que eles acabam.
	 * Entrada: 13 chamadas a retirarYurt().
	 * Resultado esperado: as 12 primeiras retornam true, a 13ª false; restam 0.
	 */
	@Test
	public void retirarYurtFalhaQuandoAsPecasAcabam() {
		assertEquals(Jogador.YURTS_INICIAIS, jogador.getYurtsDisponiveis());
		for (int i = 0; i < Jogador.YURTS_INICIAIS; i++) {
			assertTrue(jogador.retirarYurt());
		}
		assertFalse(jogador.retirarYurt());
		assertEquals(0, jogador.getYurtsDisponiveis());
	}
}

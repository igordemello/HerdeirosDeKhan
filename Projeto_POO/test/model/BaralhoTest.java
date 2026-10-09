package model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/** Testes unitários da classe {@link Baralho}. */
public class BaralhoTest {

	private List<CartaConselheiroSecreto> cartas;

	@Before
	public void setUp() {
		cartas = new ArrayList<CartaConselheiroSecreto>();
		int id = 1;
		for (TipoConselheiroSecreto tipo : TipoConselheiroSecreto.values()) {
			cartas.add(new CartaConselheiroSecreto(id++, tipo));
		}
	}

	/**
	 * Caso de teste 1 — Embaralhamento reprodutível e completo.
	 * Objetivo: verificar que embaralhar não perde nem duplica cartas e que,
	 * com a mesma semente, a ordem resultante é a mesma.
	 * Entrada: dois baralhos com as 10 cartas secretas e Random(42).
	 * Resultado esperado: mesma ordem nos dois baralhos; 10 cartas distintas.
	 */
	@Test
	public void embaralharComMesmaSementeGeraMesmaOrdemSemPerderCartas() {
		Baralho<CartaConselheiroSecreto> b1 = new Baralho<CartaConselheiroSecreto>(cartas, new Random(42));
		Baralho<CartaConselheiroSecreto> b2 = new Baralho<CartaConselheiroSecreto>(cartas, new Random(42));
		b1.embaralhar();
		b2.embaralhar();

		assertEquals(b1.getMonte(), b2.getMonte());
		assertEquals(10, b1.tamanho());
		assertTrue(b1.getMonte().containsAll(cartas));
	}

	/**
	 * Caso de teste 2 — Compra até esvaziar o baralho.
	 * Objetivo: verificar que comprar retira a carta do topo, diminui o
	 * tamanho do monte e que comprar além do disponível é tratado.
	 * Entrada: baralho com 10 cartas; compra de 8, depois de 3.
	 * Resultado esperado: restam 2 cartas; pedir 3 lança
	 * IllegalStateException e não altera o monte; após comprar as 2
	 * restantes, comprar() devolve null.
	 */
	@Test
	public void comprarRetiraDoTopoEFalhaQuandoNaoHaCartasSuficientes() {
		Baralho<CartaConselheiroSecreto> b = new Baralho<CartaConselheiroSecreto>(cartas, new Random(1));
		CartaConselheiroSecreto topo = b.getMonte().get(b.tamanho() - 1);

		List<CartaConselheiroSecreto> compradas = b.comprar(8);
		assertSame(topo, compradas.get(0));
		assertEquals(2, b.tamanho());

		try {
			b.comprar(3);
			fail("Deveria lançar IllegalStateException");
		} catch (IllegalStateException e) {
			assertEquals(2, b.tamanho());
		}

		assertNotNull(b.comprar());
		assertNotNull(b.comprar());
		assertTrue(b.estaVazio());
		assertNull(b.comprar());
	}
}

package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.junit.Test;

/**
 * Testes da classe {@link Parada}: capacidade de yurts conforme o tipo da
 * parada.
 *
 * Assume a associação cor -> herdeiro de {@link Cor} (AZUL = Altani,
 * ROXO = Tolui, VERDE = Jochi).
 */
public class ParadaTest {

	/**
	 * Objetivo: verificar quantos yurts cada tipo de parada aceita.
	 *
	 * Cenário: uma parada simples, uma dupla e Karakorum.
	 *
	 * Resultado esperado: a simples aceita 1 yurt; a dupla aceita 2, mesmo
	 * do mesmo jogador, e recusa o 3º; Karakorum não aceita yurt comum, mas
	 * aceita o yurt inicial de Tolui (uma única vez) e de mais ninguém.
	 */
	@Test
	public void yurtsRespeitamCapacidadeDoTipoDeParada() {
		Parada simples = new Parada("R1", TipoParada.SIMPLES, Regiao.RUSSIA);
		simples.adicionarYurt(Cor.AZUL);
		assertFalse(simples.podeReceberYurt());

		Parada dupla = new Parada("C1", TipoParada.DUPLA, Regiao.CHINA);
		dupla.adicionarYurt(Cor.AZUL);
		assertTrue("Parada dupla aceita um 2º yurt", dupla.podeReceberYurt());
		dupla.adicionarYurt(Cor.AZUL);
		assertEquals(2, dupla.contarYurts(Cor.AZUL));
		assertFalse(dupla.podeReceberYurt());
		try {
			dupla.adicionarYurt(Cor.VERDE);
			fail("Parada dupla não aceita um 3º yurt");
		} catch (IllegalStateException esperada) {
			// esperado
		}

		Parada karakorum = new Parada(Tabuleiro.ID_KARAKORUM, TipoParada.KARAKORUM, null);
		assertFalse(karakorum.podeReceberYurt());
		try {
			karakorum.adicionarYurtDeTolui(Cor.AZUL);
			fail("Apenas Tolui coloca yurt em Karakorum");
		} catch (IllegalArgumentException esperada) {
			// esperado
		}
		karakorum.adicionarYurtDeTolui(Cor.ROXO);
		assertEquals(Arrays.asList(Cor.ROXO), karakorum.getYurts());
		try {
			karakorum.adicionarYurtDeTolui(Cor.ROXO);
			fail("Tolui coloca apenas 1 yurt em Karakorum");
		} catch (IllegalStateException esperada) {
			// esperado
		}
	}
}

package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da classe {@link Tabuleiro}: consultas de vizinhança, construção de
 * yurts e preparação.
 *
 * Os testes usam um mapa pequeno, montado aqui mesmo, para não depender da
 * transcrição do tabuleiro real e para que cada resultado esperado possa
 * ser conferido à mão:
 *
 * <pre>
 *   KIEV                 PEQUIM
 *    |                     |
 *   R1 ---- KARAKORUM ---- C1 (dupla) ---- P1
 *
 *   PV_ESPADA: adjacente a R1 e a KARAKORUM
 *   PV_KHAN (moeda, província do Khan): adjacente a C1;
 *            setas para PV_ESPADA e PV_YURT
 *   PV_YURT:   adjacente a C1 e a P1
 * </pre>
 *
 * Os testes assumem a associação cor -> herdeiro definida em {@link Cor}
 * (AZUL = Altani, ROXO = Tolui, VERDE = Jochi).
 */
public class TabuleiroTest {
	private static final String K = Tabuleiro.ID_KARAKORUM;

	private Tabuleiro tabuleiro;

	@Before
	public void montarMapaDeTeste() {
		tabuleiro = new Tabuleiro();
		tabuleiro.adicionarParada("R1", TipoParada.SIMPLES, Regiao.RUSSIA);
		tabuleiro.adicionarParada("C1", TipoParada.DUPLA, Regiao.CHINA);
		tabuleiro.adicionarParada("P1", TipoParada.SIMPLES, Regiao.PERSIA);
		tabuleiro.ligarParadas("R1", K);
		tabuleiro.ligarParadas(K, "C1");
		tabuleiro.ligarParadas("C1", "P1");
		tabuleiro.ligarParadaACidade("R1", NomeCidade.KIEV);
		tabuleiro.ligarParadaACidade("C1", NomeCidade.PEQUIM);

		tabuleiro.adicionarProvincia("PV_ESPADA", TipoTributo.ESPADA, false);
		tabuleiro.adicionarProvincia("PV_KHAN", TipoTributo.MOEDA, true);
		tabuleiro.adicionarProvincia("PV_YURT", TipoTributo.YURT, false);
		tabuleiro.ligarParadaAProvincia("R1", "PV_ESPADA");
		tabuleiro.ligarParadaAProvincia(K, "PV_ESPADA");
		tabuleiro.ligarParadaAProvincia("C1", "PV_KHAN");
		tabuleiro.ligarParadaAProvincia("C1", "PV_YURT");
		tabuleiro.ligarParadaAProvincia("P1", "PV_YURT");
		tabuleiro.definirAlvoDoKhan("PV_KHAN", "PV_ESPADA");
		tabuleiro.definirAlvoDoKhan("PV_KHAN", "PV_YURT");
	}

	/**
	 * Objetivo: verificar as consultas de vizinhança do mapa.
	 *
	 * Cenário: mapa de teste. R1 e C1 só se ligam passando por Karakorum.
	 *
	 * Resultado esperado: a rota R1-Karakorum vale nos dois sentidos; R1 e
	 * C1 não são vizinhas; Karakorum tem exatamente R1 e C1 como vizinhas;
	 * as províncias e cidades adjacentes são as registradas, e a ligação
	 * parada-cidade também é vista pelo lado da cidade.
	 */
	@Test
	public void vizinhancaEhSimetricaENaoTransitiva() {
		assertTrue(tabuleiro.saoVizinhas("R1", K));
		assertTrue(tabuleiro.saoVizinhas(K, "R1"));
		assertFalse("R1 e C1 só se ligam através de Karakorum", tabuleiro.saoVizinhas("R1", "C1"));

		assertEquals(conjunto("R1", "C1"), idsDasParadas(tabuleiro.getVizinhas(K)));
		assertEquals(conjunto("PV_KHAN", "PV_YURT"),
				idsDasProvincias(tabuleiro.getProvinciasAdjacentes("C1")));

		Collection<Cidade> cidadesDeR1 = tabuleiro.getCidadesAdjacentes("R1");
		assertEquals(1, cidadesDeR1.size());
		assertSame(tabuleiro.getCidade(NomeCidade.KIEV), cidadesDeR1.iterator().next());
		assertEquals(conjunto("R1"),
				idsDasParadas(tabuleiro.getCidade(NomeCidade.KIEV).getParadasAdjacentes()));
	}

	/**
	 * Objetivo: verificar que construir um yurt gasta a reserva do jogador
	 * apenas quando a construção é aceita, e que os yurts são contados na
	 * região certa.
	 *
	 * Cenário: AZUL constrói em R1 (simples) e duas vezes em C1 (dupla);
	 * VERDE tenta construir em R1 (já ocupada) e em Karakorum.
	 *
	 * Resultado esperado: AZUL fica com 3 yurts a menos e tem 1 na Rússia,
	 * 2 na China e 0 na Pérsia; as tentativas recusadas de VERDE não gastam
	 * nada da reserva dele.
	 */
	@Test
	public void construirYurtGastaReservaSoQuandoAceitoEContaPorRegiao() {
		Jogador azul = new Jogador(Cor.AZUL);
		Jogador verde = new Jogador(Cor.VERDE);

		assertTrue(tabuleiro.construirYurt(azul, "R1"));
		assertTrue(tabuleiro.construirYurt(azul, "C1"));
		assertTrue(tabuleiro.construirYurt(azul, "C1"));
		assertFalse(tabuleiro.construirYurt(verde, "R1"));
		assertFalse(tabuleiro.construirYurt(verde, K));

		assertEquals(Arrays.asList(Cor.AZUL), tabuleiro.getDonosDosYurts("R1"));
		assertEquals(1, tabuleiro.contarYurtsNaRegiao(Cor.AZUL, Regiao.RUSSIA));
		assertEquals(2, tabuleiro.contarYurtsNaRegiao(Cor.AZUL, Regiao.CHINA));
		assertEquals(0, tabuleiro.contarYurtsNaRegiao(Cor.AZUL, Regiao.PERSIA));
		assertEquals(3, tabuleiro.contarYurtsNoMapa(Cor.AZUL));

		assertEquals(Jogador.YURTS_INICIAIS - 3, azul.getYurtsDisponiveis());
		assertEquals(Jogador.YURTS_INICIAIS, verde.getYurtsDisponiveis());
	}

	/**
	 * Objetivo: verificar a preparação do tabuleiro.
	 *
	 * Cenário: 3 jogadores (AZUL/Altani, ROXO/Tolui, VERDE/Jochi), gerador
	 * aleatório com semente fixa.
	 *
	 * Resultado esperado: 4 peões em Karakorum (Altani tem 2); 1 yurt de
	 * Tolui em Karakorum, que conta no mapa mas em nenhuma região, e a
	 * reserva dele cai para 11; cada província com 1 peça de tributo;
	 * 3 cidades disponíveis e 6 na pilha. Preparar de novo é proibido.
	 */
	@Test
	public void preparacaoColocaPeoesYurtDeToluiTributosECidades() {
		List<Jogador> jogadores = new GerenciadorJogadores(
				Arrays.asList(Cor.AZUL, Cor.ROXO, Cor.VERDE)).getJogadoresEmOrdem();
		Jogador tolui = jogadores.get(1);

		tabuleiro.preparar(jogadores, new Random(42));

		assertEquals(4, tabuleiro.contarPeoes(K));
		assertSame(tabuleiro.getKarakorum(), tabuleiro.getParadaDoPeao(new Peao(Cor.AZUL, 2)));

		assertEquals(Arrays.asList(Cor.ROXO), tabuleiro.getDonosDosYurts(K));
		assertEquals(Jogador.YURTS_INICIAIS - 1, tolui.getYurtsDisponiveis());
		assertEquals(1, tabuleiro.contarYurtsNoMapa(Cor.ROXO));
		for (Regiao regiao : Regiao.values()) {
			assertEquals("Yurt de Karakorum não conta para " + regiao.getNome(), 0,
					tabuleiro.contarYurtsNaRegiao(Cor.ROXO, regiao));
		}

		for (Provincia provincia : tabuleiro.getProvincias()) {
			assertEquals(provincia.getId(), 1, provincia.getPecasDeTributo());
		}

		assertEquals(Tabuleiro.CIDADES_DISPONIVEIS, tabuleiro.getCidadesDisponiveis().size());
		assertEquals(NomeCidade.values().length - Tabuleiro.CIDADES_DISPONIVEIS,
				tabuleiro.getNumeroDeCidadesNaPilha());

		try {
			tabuleiro.preparar(jogadores, new Random(42));
			fail("Preparar o tabuleiro duas vezes deveria lançar IllegalStateException");
		} catch (IllegalStateException esperada) {
			// esperado
		}
	}

	// --------------------------------------------------------------- apoio

	private static Set<String> conjunto(String... ids) {
		return new HashSet<String>(Arrays.asList(ids));
	}

	private static Set<String> idsDasParadas(Collection<Parada> paradas) {
		Set<String> ids = new HashSet<String>();
		for (Parada p : paradas) {
			ids.add(p.getId());
		}
		return ids;
	}

	private static Set<String> idsDasProvincias(Collection<Provincia> provincias) {
		Set<String> ids = new HashSet<String>();
		for (Provincia p : provincias) {
			ids.add(p.getId());
		}
		return ids;
	}
}

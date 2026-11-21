package model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da classe {@link Tabuleiro}: preparação do mapa para a partida.
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
}

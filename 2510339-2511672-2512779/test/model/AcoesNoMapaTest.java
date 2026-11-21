package model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/*
 * Testes das ações no mapa (3ª iteração): tributos, yurts, ataques, Khan e
 * as habilidades dos herdeiros que mexem no mapa.
 *
 * Usa o mapa completo com semente fixa. AZUL é Altani e joga primeiro,
 * VERDE é Jochi. Na cartela da Altani a coluna VERMELHA tem 1 cavalo,
 * 2 punhos e o Khan, e a AMARELA tem 1 cavalo, 1 punho e 1 moeda virtual.
 * Todo mundo começa em Karakorum, que é vizinha das províncias PV04 (yurt),
 * PV05 (moeda), PV06 (espada) e PV09 (yurt), cada uma com 1 peça.
 */
public class AcoesNoMapaTest {

	private Jogo jogo;
	private Jogador azul;
	private Tabuleiro tabuleiro;

	@Before
	public void setUp() {
		tabuleiro = FabricaTabuleiro.criarTabuleiroPadrao();
		jogo = Jogo.novaPartida(Arrays.asList(Cor.AZUL, Cor.VERDE), tabuleiro, new Random(7));
		azul = jogo.getJogador(Cor.AZUL);
	}

	/*
	 * Objetivo: cada punho pega 1 tributo de província vizinha de uma parada
	 * do turno (a parada de onde o peão não saiu também vale).
	 * Entrada: AZUL ativa a VERMELHA (2 punhos) em Karakorum, pega de PV06 e
	 * de PV04 e depois tenta PV05 e PV01.
	 * Resultado esperado: ganha 1 espada e 1 yurt; PV06 vazia não deixa pegar
	 * de novo; sem punho PV05 é recusada; PV01 não é vizinha.
	 */
	@Test
	public void cadaPunhoPegaUmaPecaDeProvinciaAdjacente() {
		assertFalse("sem coluna ativada não tem punho", jogo.podePegarTributo("PV06"));
		assertTrue(jogo.ativarColuna("VERMELHA"));
		assertEquals(2, jogo.getPunhosDaVez());
		assertFalse(jogo.podePegarTributo("PV01"));

		assertTrue(jogo.pegarTributo("PV06"));
		assertEquals(1, azul.getQuantidadeTributo(TipoTributo.ESPADA));
		assertEquals(0, jogo.getTributosNaProvincia("PV06"));
		assertFalse("província vazia", jogo.pegarTributo("PV06"));

		assertTrue(jogo.pegarTributo("PV04"));
		assertEquals(1, azul.getQuantidadeTributo(TipoTributo.YURT));
		assertEquals(0, jogo.getPunhosDaVez());
		assertFalse("acabaram os punhos", jogo.pegarTributo("PV05"));
		assertEquals(1, jogo.getTributosNaProvincia("PV05"));
	}

	/*
	 * Objetivo: yurt só em parada do turno, nunca em Karakorum, 1 por parada
	 * simples, pagando 1 peça de yurt.
	 * Entrada: AZUL com 2 peças de yurt tenta Karakorum, vai pra R08, constrói
	 * 2 vezes lá e tenta R09.
	 * Resultado esperado: Karakorum não; R08 aceita 1 (reserva cai pra 11);
	 * o 2º yurt em R08 e o de R09 são recusados.
	 */
	@Test
	public void yurtSoEmParadaDoTurnoERespeitandoCapacidade() {
		azul.adicionarTributo(TipoTributo.YURT, 2);
		assertFalse(jogo.podeConstruirYurt("KARAKORUM"));

		jogo.definirMovimentosDaVez(1);
		assertFalse(jogo.moverPeaoDaVezAte(1, "R08").isEmpty());
		assertTrue(jogo.construirYurt("R08"));
		assertEquals(Arrays.asList("AZUL"), jogo.getYurtsNaParada("R08"));
		assertEquals(Jogador.YURTS_INICIAIS - 1, azul.getYurtsDisponiveis());
		assertEquals(1, azul.getQuantidadeTributo(TipoTributo.YURT));

		assertFalse("parada simples só tem 1 yurt", jogo.construirYurt("R08"));
		assertFalse("o peão não passou por R09", jogo.construirYurt("R09"));
		assertEquals(1, azul.getQuantidadeTributo(TipoTributo.YURT));
	}

	/*
	 * Objetivo: o 1º tesouro da cidade no turno custa 1 espada e os outros 2;
	 * quem pega o último conquista a cidade e outra é revelada.
	 * Entrada: AZUL com 7 espadas para do lado de uma cidade e pega os 4
	 * tesouros.
	 * Resultado esperado: gasta 1+2+2+2 = 7, fica com 4 tesouros, a cidade
	 * fica dele com o yurt (reserva 11) e continuam 3 cidades disponíveis,
	 * todas com 4 tesouros.
	 */
	@Test
	public void atacarCustaUmaEDepoisDuasEspadasEConquistaNoUltimo() {
		Cidade alvo = tabuleiro.getCidadesDisponiveis().iterator().next();
		String id = alvo.getNome().name();
		jogo.definirMovimentosDaVez(20);
		assertFalse(jogo.moverPeaoDaVezAte(1, vizinhaLivre(alvo).getId()).isEmpty());

		azul.adicionarTributo(TipoTributo.ESPADA, 7);
		assertEquals(1, jogo.getCustoDeAtaque(id));
		assertTrue(jogo.atacarCidade(id, jogo.getTesourosDaCidade(id).get(0)));
		assertEquals(6, azul.getQuantidadeTributo(TipoTributo.ESPADA));
		assertEquals(2, jogo.getCustoDeAtaque(id));

		while (!jogo.getTesourosDaCidade(id).isEmpty()) {
			assertTrue(jogo.atacarCidade(id, jogo.getTesourosDaCidade(id).get(0)));
		}
		assertEquals(0, azul.getQuantidadeTributo(TipoTributo.ESPADA));
		assertEquals(4, azul.getTotalTesouros());
		assertEquals("CONQUISTADA", jogo.getEstadoDaCidade(id));
		assertEquals("AZUL", jogo.getConquistadorDaCidade(id));
		assertTrue(jogo.cidadeTemYurt(id));
		assertEquals(Jogador.YURTS_INICIAIS - 1, azul.getYurtsDisponiveis());

		assertEquals(3, tabuleiro.getCidadesDisponiveis().size());
		for (Cidade c : tabuleiro.getCidadesDisponiveis()) {
			assertEquals(4, c.getTesouros().size());
		}
		assertEquals(5, jogo.getCidadesNaPilha());
	}

	/*
	 * Objetivo: o Khan só é usado com coluna que tem o Khan, põe 1 peça na
	 * província e nas 2 das setas e não pode ficar no mesmo lugar.
	 * Entrada: AZUL ativa a AMARELA (sem Khan) e tenta usar; VERDE ativa a
	 * VERDE (com Khan) e põe em PV05; AZUL ativa a VERMELHA e tenta PV05 de
	 * novo.
	 * Resultado esperado: sem Khan na coluna nada acontece; PV05, PV04 e PV06
	 * vão pra 2 peças; na vez de AZUL, PV05 é recusada e PV11 aceita.
	 */
	@Test
	public void khanNaProvinciaDaTributosEPrecisaMudarDeLugar() {
		assertTrue(jogo.ativarColuna("AMARELA"));
		assertFalse(jogo.khanPendente());
		assertFalse(jogo.colocarKhan("PV05"));
		assertNull(jogo.getLugarDoKhan());

		jogo.passarVez(); // VERDE (Jochi)
		assertTrue(jogo.ativarColuna("VERDE"));
		assertTrue(jogo.khanPendente());
		assertTrue(jogo.colocarKhan("PV05"));
		assertEquals("PV05", jogo.getLugarDoKhan());
		assertEquals(2, jogo.getTributosNaProvincia("PV05"));
		assertEquals(2, jogo.getTributosNaProvincia("PV04"));
		assertEquals(2, jogo.getTributosNaProvincia("PV06"));
		assertEquals(1, jogo.getTributosNaProvincia("PV09"));
		assertFalse(jogo.khanPendente());
		assertFalse("só uma vez por turno", jogo.colocarKhan("PV11"));

		jogo.passarVez(); // AZUL
		assertTrue(jogo.ativarColuna("VERMELHA"));
		assertFalse("não pode deixar onde está", jogo.colocarKhan("PV05"));
		assertTrue(jogo.colocarKhan("PV11"));
	}

	/*
	 * Objetivo: Altani só move e age com um peão por turno.
	 * Entrada: o peão 1 vai pra R08 e depois tenta mover o peão 2; no próximo
	 * turno dela move o peão 2 e tenta o 1.
	 * Resultado esperado: o peão que não foi escolhido não sai do lugar.
	 */
	@Test
	public void altaniMoveEAgeComUmSoPeaoPorTurno() {
		jogo.definirMovimentosDaVez(6);
		assertFalse(jogo.moverPeaoDaVezAte(1, "R08").isEmpty());
		assertTrue(jogo.moverPeaoDaVezAte(2, "P05").isEmpty());
		assertEquals("KARAKORUM", jogo.getParadaDoPeao("AZUL", 2));

		jogo.passarVez();
		jogo.passarVez(); // AZUL de novo
		jogo.definirMovimentosDaVez(6);
		assertFalse(jogo.moverPeaoDaVezAte(2, "P05").isEmpty());
		assertTrue(jogo.moverPeaoDaVezAte(1, "R09").isEmpty());
	}

	/*
	 * Objetivo: Tolui constrói yurt nas paradas vizinhas sem passar por elas.
	 * Entrada: ROXO (Tolui) em Karakorum com peça de yurt tenta R08 (vizinha)
	 * e R09 (longe); depois AZUL (Altani) tenta R11 (vizinha).
	 * Resultado esperado: Tolui constrói em R08 e não em R09; Altani não
	 * consegue em R11.
	 */
	@Test
	public void toluiConstroiYurtNasParadasVizinhas() {
		Tabuleiro t = FabricaTabuleiro.criarTabuleiroPadrao();
		Jogo j = Jogo.novaPartida(Arrays.asList(Cor.ROXO, Cor.AZUL), t, new Random(3));
		j.getJogador(Cor.ROXO).adicionarTributo(TipoTributo.YURT, 2);
		assertTrue(j.podeConstruirYurt("R08"));
		assertFalse(j.podeConstruirYurt("R09"));
		assertTrue(j.construirYurt("R08"));
		assertEquals(Arrays.asList("ROXO"), j.getYurtsNaParada("R08"));

		j.passarVez();
		j.getJogador(Cor.AZUL).adicionarTributo(TipoTributo.YURT, 1);
		assertFalse(j.podeConstruirYurt("R11"));
	}

	/*
	 * Objetivo: Ogedei paga 1 moeda por um tesouro de qualquer cidade, 1 vez
	 * por turno, menos o último; e isso não conta como ataque.
	 * Entrada: AMARELO (Ogedei) com 2 moedas em Karakorum pega um tesouro,
	 * tenta outro no mesmo turno e, no turno seguinte, tenta o último de uma
	 * cidade.
	 * Resultado esperado: gasta 1 moeda e fica com o tesouro; o 2º é
	 * recusado; o último também.
	 */
	@Test
	public void ogedeiPegaTesouroComMoeda() {
		Tabuleiro t = FabricaTabuleiro.criarTabuleiroPadrao();
		Jogo j = Jogo.novaPartida(Arrays.asList(Cor.AMARELO, Cor.AZUL), t, new Random(5));
		Jogador ogedei = j.getJogador(Cor.AMARELO);
		ogedei.adicionarTributo(TipoTributo.MOEDA, 1); // já tinha 1
		Cidade c = t.getCidadesDisponiveis().iterator().next();
		String id = c.getNome().name();
		String tesouro = j.getTesourosDaCidade(id).get(0);

		assertFalse("de Karakorum não dá pra atacar", j.podeAtacar(id, tesouro));
		assertTrue(j.pegarTesouroComMoeda(id, tesouro));
		assertEquals(1, ogedei.getQuantidadeTributo(TipoTributo.MOEDA));
		assertEquals(1, ogedei.getTotalTesouros());
		assertEquals(3, j.getTesourosDaCidade(id).size());
		assertEquals("não conta como ataque", 1, j.getCustoDeAtaque(id));
		assertFalse("1 vez por turno", j.pegarTesouroComMoeda(id, j.getTesourosDaCidade(id).get(0)));

		j.passarVez();
		j.passarVez();
		while (c.getTesouros().size() > 1) {
			c.retirarTesouro(c.getTesouros().get(0));
		}
		assertFalse("não pode o último", j.pegarTesouroComMoeda(id, j.getTesourosDaCidade(id).get(0)));
	}

	/*
	 * Objetivo: Chagatai troca 1 cavalo por 1 espada, 1 vez por turno.
	 * Entrada: VERMELHO (Chagatai) tenta sem coluna; ativa a VERDE (1 cavalo
	 * na cartela dele) e troca; tenta de novo.
	 * Resultado esperado: sem movimento não troca; depois fica com 0
	 * movimentos e 1 espada; a 2ª troca é recusada.
	 */
	@Test
	public void chagataiTrocaCavaloPorEspada() {
		Tabuleiro t = FabricaTabuleiro.criarTabuleiroPadrao();
		Jogo j = Jogo.novaPartida(Arrays.asList(Cor.VERMELHO, Cor.AZUL), t, new Random(5));
		assertFalse(j.trocarCavaloPorEspada());
		assertTrue(j.ativarColuna("VERDE"));
		assertEquals(1, j.getMovimentosDaVez());
		assertTrue(j.trocarCavaloPorEspada());
		assertEquals(0, j.getMovimentosDaVez());
		assertEquals(1, j.getJogador(Cor.VERMELHO).getQuantidadeTributo(TipoTributo.ESPADA));
		j.definirMovimentosDaVez(2);
		assertFalse("1 vez por turno", j.trocarCavaloPorEspada());
	}

	// parada vizinha da cidade onde o peão pode parar (a de menor id, pra
	// o teste dar sempre o mesmo resultado)
	private static Parada vizinhaLivre(Cidade cidade) {
		List<Parada> vizinhas = new ArrayList<Parada>(cidade.getParadasAdjacentes());
		Collections.sort(vizinhas, new Comparator<Parada>() {
			@Override
			public int compare(Parada a, Parada b) {
				return a.getId().compareTo(b.getId());
			}
		});
		for (Parada p : vizinhas) {
			if (p.podeReceberPeao()) {
				return p;
			}
		}
		throw new IllegalStateException("Nenhuma parada livre ao lado de " + cidade);
	}
}

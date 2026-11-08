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
	 * Objetivo: os punhos extras do bônus somam com os da coluna.
	 * Entrada: AZUL usa o bônus "2 punhos" e ativa a AMARELA (1 punho).
	 * Resultado esperado: 3 punhos, e depois de 3 tributos não sobra nenhum.
	 */
	@Test
	public void bonusDePunhosSomaAosDaColuna() {
		azul.receberBonus(TipoBonus.DOIS_PUNHOS);
		assertTrue(jogo.usarBonus("DOIS_PUNHOS"));
		assertTrue(jogo.ativarColuna("AMARELA"));
		assertEquals(3, jogo.getPunhosDaVez());
		assertTrue(jogo.pegarTributo("PV04"));
		assertTrue(jogo.pegarTributo("PV05"));
		assertTrue(jogo.pegarTributo("PV09"));
		assertEquals(0, jogo.getPunhosDaVez());
		assertEquals(0, jogo.getPunhosExtrasDaVez());
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
	 * Objetivo: na preparação as 3 cidades reveladas recebem 4 tesouros.
	 * Entrada: partida nova.
	 * Resultado esperado: 3 cidades DISPONIVEL com 4 tesouros e 6 NA_PILHA
	 * sem nenhum.
	 */
	@Test
	public void cidadesReveladasComecamComQuatroTesouros() {
		int disponiveis = 0;
		for (String id : jogo.getCidades()) {
			if ("DISPONIVEL".equals(jogo.getEstadoDaCidade(id))) {
				disponiveis++;
				assertEquals(4, jogo.getTesourosDaCidade(id).size());
			} else {
				assertEquals("NA_PILHA", jogo.getEstadoDaCidade(id));
				assertTrue(jogo.getTesourosDaCidade(id).isEmpty());
			}
		}
		assertEquals(3, disponiveis);
		assertEquals(6, jogo.getCidadesNaPilha());
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
	 * Objetivo: não dá pra atacar de longe nem sem espada suficiente.
	 * Entrada: AZUL com 2 espadas tenta atacar de Karakorum (não é vizinha de
	 * cidade); depois, do lado da cidade, pega 1 tesouro e tenta o 2º.
	 * Resultado esperado: os ataques impossíveis não gastam espada nem tiram
	 * tesouro.
	 */
	@Test
	public void ataqueRecusadoLongeDaCidadeOuSemEspadas() {
		Cidade alvo = tabuleiro.getCidadesDisponiveis().iterator().next();
		String id = alvo.getNome().name();
		String tesouro = jogo.getTesourosDaCidade(id).get(0);
		azul.adicionarTributo(TipoTributo.ESPADA, 2);
		assertFalse("Karakorum não é vizinha de cidade", jogo.atacarCidade(id, tesouro));

		jogo.definirMovimentosDaVez(20);
		jogo.moverPeaoDaVezAte(1, vizinhaLivre(alvo).getId());
		assertTrue(jogo.atacarCidade(id, tesouro));
		assertFalse("o 2º custa 2 espadas", jogo.atacarCidade(id, jogo.getTesourosDaCidade(id).get(0)));
		assertEquals(1, azul.getQuantidadeTributo(TipoTributo.ESPADA));
		assertEquals(3, jogo.getTesourosDaCidade(id).size());
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
	 * Objetivo: província não passa de 3 peças quando o Khan vem.
	 * Entrada: PV05 já com 3 peças e o Khan vai pra PV05.
	 * Resultado esperado: PV05 fica com 3 e PV06 (seta) ganha 1.
	 */
	@Test
	public void khanRespeitaLimiteDeTresPecas() {
		tabuleiro.getProvincia("PV05").adicionarTributo();
		tabuleiro.getProvincia("PV05").adicionarTributo();
		assertTrue(jogo.ativarColuna("VERMELHA"));
		assertTrue(jogo.colocarKhan("PV05"));
		assertEquals(3, jogo.getTributosNaProvincia("PV05"));
		assertEquals(2, jogo.getTributosNaProvincia("PV06"));
	}

	/*
	 * Objetivo: Khan na área de melhorias dá a moeda/espada dos ícones que
	 * estão descobertos e completa só os espaços vazios.
	 * Entrada: AZUL (5 moedas) ativa a VERMELHA, compra 2 melhorias (os 2
	 * espaços da esquerda ficam vazios, aparecendo o ícone da moeda) e põe o
	 * Khan lá.
	 * Resultado esperado: +1 moeda, nenhuma espada, 5 melhorias de novo e as
	 * 3 antigas continuam à direita.
	 */
	@Test
	public void khanNaAreaDeMelhoriasDaMoedaEReporVazios() {
		azul.adicionarTributo(TipoTributo.MOEDA, 4); // já tinha 1
		assertTrue(jogo.ativarColuna("VERMELHA"));
		assertTrue(jogo.comprarMelhoria(0));
		assertTrue(jogo.comprarMelhoria(0));
		List<String> restantes = new ArrayList<String>(jogo.getMelhoriasDisponiveis());
		assertEquals(3, restantes.size());
		assertTrue(jogo.espacoDeMelhoriaVazio(1));
		assertFalse(jogo.espacoDeMelhoriaVazio(3));

		assertTrue(jogo.colocarKhan(Jogo.KHAN_NA_AREA_DE_MELHORIAS));
		assertEquals(2, azul.getQuantidadeTributo(TipoTributo.MOEDA)); // 5 - 4 + 1
		assertEquals(0, azul.getQuantidadeTributo(TipoTributo.ESPADA));
		List<String> agora = jogo.getMelhoriasDisponiveis();
		assertEquals(5, agora.size());
		assertEquals(restantes, agora.subList(2, 5));
		assertFalse(jogo.espacoDeMelhoriaVazio(0));
	}

	/*
	 * Objetivo: Khan só põe peça enquanto tiver na reserva (a caixa tem 10
	 * espadas, 10 yurts e 15 moedas).
	 * Entrada: 4 espadas nas províncias e as outras 6 com AZUL; o Khan vai
	 * pra PV05 (setas pra PV04 e PV06).
	 * Resultado esperado: reserva de espada 0; PV06 continua com 1 e PV05 e
	 * PV04 vão pra 2.
	 */
	@Test
	public void khanRespeitaAReservaDeTributos() {
		azul.adicionarTributo(TipoTributo.ESPADA, 6);
		assertEquals(0, jogo.getTributosNaReserva("ESPADA"));
		assertTrue(jogo.ativarColuna("VERMELHA"));
		assertTrue(jogo.colocarKhan("PV05"));
		assertEquals(1, jogo.getTributosNaProvincia("PV06"));
		assertEquals(2, jogo.getTributosNaProvincia("PV05"));
		assertEquals(2, jogo.getTributosNaProvincia("PV04"));
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
	 * Objetivo: na Altani a primeira ação também escolhe o peão do turno.
	 * Entrada: os 2 peões em Karakorum; AZUL pega tributo de PV04 e depois
	 * tenta mover o peão 2 e o peão 1.
	 * Resultado esperado: o peão 1 vira o do turno e o 2 não anda.
	 */
	@Test
	public void altaniPrimeiraAcaoEscolheOPeao() {
		assertTrue(jogo.ativarColuna("VERMELHA"));
		assertTrue(jogo.pegarTributo("PV04"));
		jogo.definirMovimentosDaVez(3);
		assertTrue(jogo.moverPeaoDaVezAte(2, "P05").isEmpty());
		assertFalse(jogo.moverPeaoDaVezAte(1, "P05").isEmpty());
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

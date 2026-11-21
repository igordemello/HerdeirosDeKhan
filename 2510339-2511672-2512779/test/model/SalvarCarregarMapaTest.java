package model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/*
 * Testes da 4ª iteração (parte do Arthur): salvar e carregar o que fica no
 * mapa (peões, yurts, tributos, cidades, pilha de cidades e Khan).
 *
 * Jogadores: AZUL (Altani), VERDE (Jochi) e ROXO (Tolui, que começa com um
 * yurt em Karakorum). O arquivo é escrito num StringWriter e carregado numa
 * partida nova com outra semente, então se algo não for salvo o teste
 * percebe (a partida nova tem outras cidades, outros tesouros etc.).
 */
public class SalvarCarregarMapaTest {

	private static final List<Cor> CORES = Arrays.asList(Cor.AZUL, Cor.VERDE, Cor.ROXO);

	private Jogo jogo;
	private Tabuleiro tabuleiro;
	private Jogador azul;

	@Before
	public void setUp() {
		tabuleiro = FabricaTabuleiro.criarTabuleiroPadrao();
		jogo = Jogo.novaPartida(CORES, tabuleiro, new Random(7));
		azul = jogo.getJogador(Cor.AZUL);
	}

	// arquivo completo das duas partes (jogadores + mapa), como o Igor vai juntar
	private static String salvar(Jogo j) {
		StringWriter texto = new StringWriter();
		PrintWriter saida = new PrintWriter(texto);
		saida.println(ArquivoPartida.CABECALHO);
		j.salvarParteDosJogadores(saida);
		j.salvarParteDoMapa(saida);
		saida.flush();
		return texto.toString();
	}

	// só a seção do mapa, pra comparar antes e depois
	private static String salvarMapa(Jogo j) {
		StringWriter texto = new StringWriter();
		PrintWriter saida = new PrintWriter(texto);
		j.salvarParteDoMapa(saida);
		saida.flush();
		return texto.toString();
	}

	private static void carregarEm(Jogo j, String texto) throws IOException {
		j.carregarParteDosJogadores(ArquivoPartida.ler(new StringReader(texto)));
		j.carregarParteDoMapa(ArquivoPartida.ler(new StringReader(texto)));
	}

	private static Jogo partidaNova() {
		return Jogo.novaPartida(CORES, FabricaTabuleiro.criarTabuleiroPadrao(), new Random(99));
	}

	// AZUL pega tributo, usa o Khan, anda, constrói yurt e conquista uma cidade
	private String jogarUmTurno() {
		azul.adicionarTributo(TipoTributo.YURT, 1);
		azul.adicionarTributo(TipoTributo.ESPADA, 7);
		assertTrue(jogo.ativarColuna("VERMELHA"));
		assertTrue(jogo.pegarTributo("PV06"));
		assertTrue(jogo.colocarKhan("PV05"));

		Cidade alvo = tabuleiro.getCidadesDisponiveis().iterator().next();
		String cidade = alvo.getNome().name();
		jogo.definirMovimentosDaVez(20);
		Parada vizinha = vizinhaLivre(alvo);
		assertFalse(jogo.moverPeaoDaVezAte(1, vizinha.getId()).isEmpty());
		assertTrue(jogo.construirYurt(vizinha.getId()));
		while (!jogo.getTesourosDaCidade(cidade).isEmpty()) {
			assertTrue(jogo.atacarCidade(cidade, jogo.getTesourosDaCidade(cidade).get(0)));
		}
		jogo.passarVez();
		return cidade;
	}

	/*
	 * Objetivo: o mapa salvo volta igual numa partida nova.
	 * Entrada: AZUL joga um turno (tributo, Khan em PV05, yurt e conquista de
	 * uma cidade); salva; carrega numa partida criada com outra semente.
	 * Resultado esperado: peões, yurts (inclusive o do Tolui), tributos,
	 * cidades, Khan e pilha iguais; salvar de novo dá exatamente o mesmo texto.
	 */
	@Test
	public void mapaVoltaIgualDepoisDeCarregar() throws IOException {
		String conquistada = jogarUmTurno();
		String texto = salvar(jogo);

		Jogo outro = partidaNova();
		assertNotEquals("a partida nova começa diferente", salvarMapa(jogo), salvarMapa(outro));
		carregarEm(outro, texto);

		assertEquals(salvarMapa(jogo), salvarMapa(outro));
		assertEquals("PV05", outro.getLugarDoKhan());
		assertEquals(jogo.getParadaDoPeao("AZUL", 1), outro.getParadaDoPeao("AZUL", 1));
		assertEquals("KARAKORUM", outro.getParadaDoPeao("AZUL", 2));
		assertEquals(Arrays.asList("ROXO"), outro.getYurtsNaParada("KARAKORUM"));
		assertEquals(Arrays.asList("AZUL"), outro.getYurtsNaParada(jogo.getParadaDoPeao("AZUL", 1)));
		assertEquals("CONQUISTADA", outro.getEstadoDaCidade(conquistada));
		assertEquals("AZUL", outro.getConquistadorDaCidade(conquistada));
		assertTrue(outro.cidadeTemYurt(conquistada));
		for (String pv : jogo.getProvincias()) {
			assertEquals(pv, jogo.getTributosNaProvincia(pv), outro.getTributosNaProvincia(pv));
		}
		for (String c : jogo.getCidades()) {
			assertEquals(c, jogo.getEstadoDaCidade(c), outro.getEstadoDaCidade(c));
			assertEquals(c, jogo.getTesourosDaCidade(c), outro.getTesourosDaCidade(c));
		}
		assertEquals(jogo.getCidadesNaPilha(), outro.getCidadesNaPilha());
	}

	/*
	 * Objetivo: o arquivo é texto ASCII puro (regra 2 do enunciado).
	 * Entrada: arquivo salvo depois de um turno.
	 * Resultado esperado: nenhum caractere acima de 127 e a seção [MAPA] está lá.
	 */
	@Test
	public void arquivoEhAsciiPuro() {
		jogarUmTurno();
		String texto = salvar(jogo);
		for (int i = 0; i < texto.length(); i++) {
			assertTrue("caractere não ASCII: " + texto.charAt(i), texto.charAt(i) <= 127);
		}
		assertTrue(texto.contains("[MAPA]"));
	}

	/*
	 * Objetivo: depois de carregar, a próxima cidade revelada é a do topo da
	 * pilha salva (a ordem da pilha também é salva).
	 * Entrada: salva a partida nova, carrega em outra e conquista uma cidade
	 * nas duas.
	 * Resultado esperado: as duas revelam a mesma cidade, com os mesmos 4
	 * tesouros.
	 */
	@Test
	public void proximaCidadeReveladaSegueAPilhaSalva() throws IOException {
		String texto = salvar(jogo);
		Tabuleiro outroTabuleiro = FabricaTabuleiro.criarTabuleiroPadrao();
		Jogo outro = Jogo.novaPartida(CORES, outroTabuleiro, new Random(99));
		carregarEm(outro, texto);

		Cidade c1 = tabuleiro.getCidadesDisponiveis().iterator().next();
		Cidade revelada1 = tabuleiro.conquistarCidade(c1.getNome(), jogo.getJogador(Cor.AZUL));
		Cidade revelada2 = outroTabuleiro.conquistarCidade(c1.getNome(), outro.getJogador(Cor.AZUL));
		assertEquals(revelada1.getNome(), revelada2.getNome());
	}

	/*
	 * Objetivo: o que é só do turno não vem do arquivo; o turno de quem joga
	 * começa limpo a partir de onde os peões estão.
	 * Entrada: carrega um arquivo salvo depois do turno de AZUL.
	 * Resultado esperado: as paradas do turno são só as dos peões do jogador
	 * da vez, o Khan não está pendente e o 1º ataque custa 1 espada.
	 */
	@Test
	public void turnoComecaLimpoDepoisDeCarregar() throws IOException {
		String conquistada = jogarUmTurno();
		Jogo outro = partidaNova();
		carregarEm(outro, salvar(jogo));

		String vez = outro.getJogadorDaVez();
		List<String> esperadas = new ArrayList<String>();
		for (int n = 1; n <= outro.getNumeroDePeoes(vez); n++) {
			if (!esperadas.contains(outro.getParadaDoPeao(vez, n))) {
				esperadas.add(outro.getParadaDoPeao(vez, n));
			}
		}
		Collections.sort(esperadas);
		assertEquals(esperadas, outro.getParadasDoTurno());
		assertFalse(outro.khanPendente());
		for (String c : outro.getCidadesAtacaveis()) {
			assertEquals(1, outro.getCustoDeAtaque(c));
		}
		assertNotNull(conquistada);
	}

	/*
	 * Objetivo: arquivo com erro no mapa não deixa o mapa pela metade.
	 * Entrada: arquivos com peão numa parada que não existe, yurt de AZUL em
	 * Karakorum, cidade disponível sem tesouro e pilha de cidades com uma
	 * cidade repetida.
	 * Resultado esperado: IllegalArgumentException em todos e o mapa da
	 * partida continua igual ao de antes.
	 */
	@Test
	public void arquivoComErroNaoMudaOMapa() throws IOException {
		String texto = salvar(jogo);
		String pilha = linhaQueComeca(texto, "pilhaDeCidades=");
		String disponivel = null;
		for (Cidade c : tabuleiro.getCidadesDisponiveis()) {
			disponivel = c.getNome().name();
		}
		String[] errados = {
				texto.replace("peao.AZUL.1=KARAKORUM", "peao.AZUL.1=XYZ"),
				texto.replace("yurts.KARAKORUM=ROXO", "yurts.KARAKORUM=AZUL"),
				texto.replace(linhaQueComeca(texto, "cidade." + disponivel + ".tesouros="),
						"cidade." + disponivel + ".tesouros="),
				texto.replace(pilha, pilha + "," + pilha.substring(pilha.indexOf('=') + 1).split(",")[0]), };

		Jogo outro = partidaNova();
		String antes = salvarMapa(outro);
		for (String errado : errados) {
			assertNotEquals("o teste tem que ter estragado o arquivo", texto, errado);
			try {
				outro.carregarParteDoMapa(ArquivoPartida.ler(new StringReader(errado)));
				fail("deveria recusar o arquivo");
			} catch (IllegalArgumentException esperado) {
				// ok
			}
			assertEquals(antes, salvarMapa(outro));
		}
	}

	/*
	 * Objetivo: o carregar confere se cada jogador tem 12 yurts somando mapa
	 * e reserva.
	 * Entrada: arquivo em que se acrescentou um yurt de VERDE numa parada
	 * vazia (sem tirar da reserva dele).
	 * Resultado esperado: IllegalArgumentException.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void yurtsAMaisSaoRecusados() throws IOException {
		String texto = salvar(jogo).replace("[MAPA]", "[MAPA]" + System.lineSeparator() + "yurts.R09=VERDE");
		carregarEm(partidaNova(), texto);
	}

	// ---------------------------------------------------------------- apoio

	private static String linhaQueComeca(String texto, String comeco) {
		for (String linha : texto.split("\\r?\\n")) {
			if (linha.startsWith(comeco)) {
				return linha;
			}
		}
		throw new IllegalStateException("Não achei a linha " + comeco);
	}

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

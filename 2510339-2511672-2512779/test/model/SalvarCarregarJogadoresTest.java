package model;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da 4ª iteração (parte do Rafael): salvar e carregar os jogadores,
 * os tabuleiros dos jogadores, as cartas, o mercado de melhorias, a reserva
 * de tesouros e os conselheiros; e a habilidade do Jochi.
 *
 * Partida com o mapa completo e semente fixa. Jogadores: AZUL (Altani) e
 * VERDE (Jochi). O arquivo é escrito num StringWriter e lido de um
 * StringReader, sem criar arquivos no disco.
 */
public class SalvarCarregarJogadoresTest {

	private Jogo jogo;
	private Jogador azul;
	private Jogador verde;

	@Before
	public void setUp() {
		jogo = Jogo.novaPartida(Arrays.asList(Cor.AZUL, Cor.VERDE), FabricaTabuleiro.criarTabuleiroPadrao(),
				new Random(7));
		azul = jogo.getJogador(Cor.AZUL);
		verde = jogo.getJogador(Cor.VERDE);
	}

	/** Escreve as seções da parte dos jogadores, com o cabeçalho, num texto. */
	private static String salvar(Jogo j) {
		StringWriter texto = new StringWriter();
		PrintWriter saida = new PrintWriter(texto);
		saida.println(ArquivoPartida.CABECALHO);
		j.salvarParteDosJogadores(saida);
		saida.flush();
		return texto.toString();
	}

	/** Cria outra partida (semente diferente) e carrega o texto nela. */
	private static Jogo carregar(String texto) throws IOException {
		Jogo outro = Jogo.novaPartida(Arrays.asList(Cor.AZUL, Cor.VERDE), FabricaTabuleiro.criarTabuleiroPadrao(),
				new Random(99));
		outro.carregarParteDosJogadores(ArquivoPartida.ler(new StringReader(texto)));
		return outro;
	}

	/** Mexe em quase tudo que a parte dos jogadores guarda. */
	private void jogarUmPouco() {
		jogo.escolherMelhoriaInicial(1);
		jogo.escolherConselheiroSecreto("AZUL", jogo.getCartasSecretasParaEscolher("AZUL").get(0));
		jogo.escolherConselheiroSecreto("VERDE", jogo.getCartasSecretasParaEscolher("VERDE").get(1));
		azul.adicionarTributo(TipoTributo.MOEDA, 3);
		azul.adicionarTributo(TipoTributo.ESPADA, 2);
		azul.adicionarTesouro(TipoTesouro.SEDA, 2);
		azul.receberBonus(TipoBonus.VOTO);
		azul.receberBonus(TipoBonus.DUAS_MOEDAS);
		azul.usarBonus(TipoBonus.VOTO);
		azul.ganharVotos(1);
		assertTrue(jogo.comprarMelhoria(0));
		azul.getTabuleiro().ativar(ColunaJogador.VERMELHA);
		CartaConselheiroAberto aberta = jogo.getDistribuidor().getCartasAbertasDisponiveis().iterator().next();
		jogo.getDistribuidor().entregarCartaAberta(verde, aberta.getId());
		jogo.getReservaTesouros().comprar(3);
		Conselheiro r11 = jogo.getConselho().getConselheiro("R11");
		azul.receberBonus(r11.registrarEntrega());
		jogo.passarVez();
	}

	/**
	 * Objetivo: verificar a ida e volta completa da parte dos jogadores.
	 * Entrada: uma partida com compras, bônus, cartas, entregas e fichas;
	 * salva, carregada numa partida criada com outra semente e salva de novo.
	 * Resultado esperado: o segundo arquivo é igual ao primeiro, e os dados
	 * lidos pela Façade são os mesmos.
	 */
	@Test
	public void idaEVoltaDaPartidaInteira() throws IOException {
		jogarUmPouco();
		String primeiro = salvar(jogo);

		Jogo outro = carregar(primeiro);
		assertEquals(primeiro, salvar(outro));

		assertEquals(jogo.getVotos("AZUL"), outro.getVotos("AZUL"));
		assertEquals(jogo.getVotos("VERDE"), outro.getVotos("VERDE"));
		assertEquals(jogo.getTributo("AZUL", "ESPADA"), outro.getTributo("AZUL", "ESPADA"));
		assertEquals(jogo.getTesouro("AZUL", "SEDA"), outro.getTesouro("AZUL", "SEDA"));
		assertEquals(jogo.getBonusDisponiveis("AZUL"), outro.getBonusDisponiveis("AZUL"));
		assertEquals(jogo.getConselheiroSecreto("VERDE"), outro.getConselheiroSecreto("VERDE"));
		assertEquals(jogo.getCartasAbertasConquistadas("VERDE"), outro.getCartasAbertasConquistadas("VERDE"));
		assertEquals(jogo.getCartasAbertasDisponiveis(), outro.getCartasAbertasDisponiveis());
		assertEquals(jogo.getMelhoriasDisponiveis(), outro.getMelhoriasDisponiveis());
		assertEquals(jogo.getTamanhoPilhaMelhorias(), outro.getTamanhoPilhaMelhorias());
		assertEquals(jogo.getBonusDoConselheiro("R11"), outro.getBonusDoConselheiro("R11"));
		for (String coluna : jogo.getColunas()) {
			assertEquals(jogo.colunaOcupada("AZUL", coluna), outro.colunaOcupada("AZUL", coluna));
			assertEquals(jogo.getIdsDasMelhoriasDaColuna("AZUL", coluna), outro.getIdsDasMelhoriasDaColuna("AZUL", coluna));
			assertEquals(jogo.getIdsDasMelhoriasDaColuna("VERDE", coluna),
					outro.getIdsDasMelhoriasDaColuna("VERDE", coluna));
		}
	}

	/**
	 * Objetivo: verificar que a ordem da pilha de melhorias e da reserva de
	 * tesouros é mantida (a próxima compra às cegas tem de ser a mesma peça).
	 * Entrada: partida salva e carregada; nas duas, compra do topo da pilha
	 * e 3 tesouros da reserva.
	 * Resultado esperado: as mesmas peças e os mesmos tesouros.
	 */
	@Test
	public void ordemDaPilhaEDaReservaEhMantida() throws IOException {
		jogo.escolherMelhoriaInicial(0);
		Jogo outro = carregar(salvar(jogo));
		Jogo original = jogo;

		assertEquals(original.getMercado().verTopo().getId(), outro.getMercado().verTopo().getId());
		assertEquals(original.getReservaTesouros().comprar(3), outro.getReservaTesouros().comprar(3));
	}

	/**
	 * Objetivo: verificar que uma partida salva antes da escolha dos
	 * conselheiros secretos volta com as 2 cartas para escolher.
	 * Entrada: partida recém-criada, salva e carregada.
	 * Resultado esperado: as mesmas 2 cartas; nenhum conselheiro escolhido;
	 * depois de carregar, a escolha funciona.
	 */
	@Test
	public void cartasSecretasAindaNaoEscolhidasVoltam() throws IOException {
		Jogo outro = carregar(salvar(jogo));
		assertEquals(jogo.getCartasSecretasParaEscolher("AZUL"), outro.getCartasSecretasParaEscolher("AZUL"));
		assertNull(outro.getConselheiroSecreto("AZUL"));
		String escolhida = outro.getCartasSecretasParaEscolher("AZUL").get(0);
		outro.escolherConselheiroSecreto("AZUL", escolhida);
		assertEquals(escolhida, outro.getConselheiroSecreto("AZUL"));
	}

	/**
	 * Objetivo: verificar que o arquivo é ASCII puro e tem as seções
	 * combinadas.
	 * Entrada: partida com algumas jogadas, salva.
	 * Resultado esperado: nenhum caractere acima de 127; seções [JOGADOR
	 * AZUL], [JOGADOR VERDE], [MERCADO], [RESERVA], [CONSELHEIROS] e [CARTAS].
	 */
	@Test
	public void arquivoEhAsciiETemAsSecoes() throws IOException {
		jogarUmPouco();
		String texto = salvar(jogo);
		for (char c : texto.toCharArray()) {
			assertTrue("caractere não ASCII: " + c, c <= 127);
		}
		Map<String, Map<String, String>> secoes = ArquivoPartida.ler(new StringReader(texto));
		for (String s : Arrays.asList("JOGADOR AZUL", "JOGADOR VERDE", "MERCADO", "RESERVA", "CONSELHEIROS",
				"CARTAS")) {
			assertTrue("falta [" + s + "]", secoes.containsKey(s));
		}
	}

	/**
	 * Objetivo: verificar que arquivos errados são recusados.
	 * Entrada: (1) cabeçalho de outro jogo; (2) arquivo sem a seção
	 * [MERCADO]; (3) arquivo com uma melhoria a mais no mercado.
	 * Resultado esperado: IOException no 1º e IllegalArgumentException nos
	 * outros.
	 */
	@Test
	public void arquivoErradoEhRecusado() throws IOException {
		try {
			ArquivoPartida.ler(new StringReader("OUTRO JOGO\n[MERCADO]\n"));
			fail("cabeçalho errado deveria ser recusado");
		} catch (IOException esperado) {
		}
		String texto = salvar(jogo);
		String semMercado = texto.replace("[MERCADO]", "[NADA]");
		try {
			carregar(semMercado);
			fail("falta de seção deveria ser recusada");
		} catch (IllegalArgumentException esperado) {
		}
		String pecaAMais = texto.replaceFirst("\npilha=", "\npilha=CAV-V,");
		try {
			carregar(pecaAMais);
			fail("melhoria a mais deveria ser recusada");
		} catch (IllegalArgumentException esperado) {
		}
	}

	// --------------------------------------------------- habilidade do Jochi

	/**
	 * Objetivo: verificar a preparação do Jochi: 2 melhorias ao acaso, uma
	 * vai para a coluna branca e a outra volta para a pilha.
	 * Entrada: partida com VERDE (Jochi); escolhe a 2ª peça.
	 * Resultado esperado: 2 peças para escolher; depois da escolha, a peça
	 * está na coluna branca, nada fica pendente e a pilha recebe a outra.
	 */
	@Test
	public void jochiComecaComUmaMelhoriaNaColunaBranca() {
		assertEquals("VERDE", jogo.getCorComMelhoriaInicialParaEscolher());
		assertEquals(2, jogo.getMelhoriasIniciaisParaEscolher().size());
		String escolhida = jogo.getMelhoriasIniciaisParaEscolher().get(1);
		int pilha = jogo.getTamanhoPilhaMelhorias();

		assertTrue(jogo.escolherMelhoriaInicial(1));
		assertEquals(Arrays.asList(escolhida), jogo.getIdsDasMelhoriasDaColuna("VERDE", "BRANCA"));
		assertNull(jogo.getCorComMelhoriaInicialParaEscolher());
		assertEquals(pilha + 1, jogo.getTamanhoPilhaMelhorias());
		assertFalse(jogo.escolherMelhoriaInicial(0));
	}

	/**
	 * Objetivo: verificar que só o Jochi pode pôr uma melhoria numa coluna
	 * de outra cor.
	 * Entrada: uma peça disponível que não é amarela; AZUL tenta pô-la na
	 * coluna AMARELA; depois VERDE (Jochi) faz o mesmo.
	 * Resultado esperado: recusado para AZUL; aceito para VERDE.
	 */
	@Test
	public void soOJochiIgnoraACorDaMelhoria() {
		jogo.escolherMelhoriaInicial(0);
		int indice = indiceDeUmaPecaQueNaoE(ColunaJogador.AMARELA);
		String id = jogo.getMelhoriasDisponiveis().get(indice);

		azul.adicionarTributo(TipoTributo.MOEDA, 5);
		assertFalse(jogo.jogadorDaVezIgnoraCorDasMelhorias());
		assertFalse(jogo.podeComprarMelhoria(indice, "AMARELA"));

		jogo.passarVez();
		verde.adicionarTributo(TipoTributo.MOEDA, 2);
		assertTrue(jogo.jogadorDaVezIgnoraCorDasMelhorias());
		assertTrue(jogo.comprarMelhoria(indice, "AMARELA"));
		assertEquals(Arrays.asList(id), jogo.getIdsDasMelhoriasDaColuna("VERDE", "AMARELA"));
	}

	/**
	 * Objetivo: verificar que o Jochi não ganha melhoria grátis pela 1ª
	 * linha completa (o espaço da coluna branca já está ocupado), mas ganha
	 * pela 2ª.
	 * Entrada: VERDE com 1 peça na branca completa a 1ª e depois a 2ª linha.
	 * Resultado esperado: 0 grátis pendentes na 1ª linha; 1 na 2ª.
	 */
	@Test
	public void jochiNaoGanhaGratisPelaPrimeiraLinha() {
		jogo.escolherMelhoriaInicial(0);
		jogo.passarVez();
		TabuleiroJogador tab = verde.getTabuleiro();
		tab.adicionarMelhoria(ColunaJogador.VERMELHA, TipoMelhoria.PUNHO);
		tab.adicionarMelhoria(ColunaJogador.VERDE, TipoMelhoria.PUNHO);
		verde.adicionarTributo(TipoTributo.MOEDA, 4);

		assertTrue(jogo.comprarMelhoria(0, "AMARELA")); // completa a 1ª linha
		assertEquals(1, tab.getLinhasCompletas());
		assertEquals(0, jogo.getMelhoriasGratisPendentes());

		tab.adicionarMelhoria(ColunaJogador.VERMELHA, TipoMelhoria.PUNHO);
		tab.adicionarMelhoria(ColunaJogador.VERDE, TipoMelhoria.PUNHO);
		assertTrue(jogo.comprarMelhoria(0, "AMARELA")); // completa a 2ª linha
		assertEquals(1, jogo.getMelhoriasGratisPendentes());
	}

	/**
	 * Objetivo: verificar que a escolha pendente do Jochi também é salva.
	 * Entrada: partida salva antes de o Jochi escolher.
	 * Resultado esperado: a partida carregada tem as mesmas 2 peças para
	 * escolher.
	 */
	@Test
	public void escolhaPendenteDoJochiEhSalva() throws IOException {
		Jogo outro = carregar(salvar(jogo));
		assertEquals("VERDE", outro.getCorComMelhoriaInicialParaEscolher());
		assertEquals(jogo.getMelhoriasIniciaisParaEscolher(), outro.getMelhoriasIniciaisParaEscolher());
	}

	private int indiceDeUmaPecaQueNaoE(ColunaJogador cor) {
		for (int i = 0; i < jogo.getMelhoriasDisponiveis().size(); i++) {
			if (jogo.getMercado().verDisponivel(i).getCor() != cor) {
				return i;
			}
		}
		throw new AssertionError("todas as peças disponíveis são " + cor);
	}
}

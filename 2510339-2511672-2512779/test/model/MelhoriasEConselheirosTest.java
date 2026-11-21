package model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * Testes da 3ª iteração (parte do Rafael): compra de melhorias, peças
 * virtuais, entregas aos conselheiros, bônus, cartas de conselheiro e
 * pontuação de fim de jogo.
 *
 * Partida com o mapa completo e semente fixa. Jogadores: AZUL (Altani,
 * começa com 1 moeda) e VERDE (Jochi). No tabuleiro de Altani, a coluna
 * VERMELHA tem 1 cavalo, 2 punhos e o Khan; a VERDE, 2 cavalos e 1 punho; a
 * AMARELA, 1 cavalo, 1 punho e 1 moeda virtual. Todos começam em Karakorum,
 * vizinha do conselheiro R11 (pede 2 ferros).
 */
public class MelhoriasEConselheirosTest {

	private Jogo jogo;
	private Jogador azul;
	private Jogador verde;
	private Tabuleiro tabuleiro;

	@Before
	public void setUp() {
		tabuleiro = FabricaTabuleiro.criarTabuleiroPadrao();
		jogo = Jogo.novaPartida(Arrays.asList(Cor.AZUL, Cor.VERDE), tabuleiro, new Random(7));
		azul = jogo.getJogador(Cor.AZUL);
		verde = jogo.getJogador(Cor.VERDE);
	}

	private static ColunaJogador colunaDoId(String id) {
		String letra = id.substring(id.indexOf('-') + 1);
		return letra.equals("A") ? ColunaJogador.AMARELA
				: letra.equals("R") ? ColunaJogador.VERMELHA : ColunaJogador.VERDE;
	}

	/**
	 * Objetivo: verificar que a compra custa 2 moedas e coloca a peça na
	 * coluna da cor dela, sem repor as disponíveis.
	 * Entrada: AZUL com 2 moedas compra a 1ª melhoria disponível.
	 * Resultado esperado: 0 moedas; a peça está na coluna da sua cor; restam
	 * 4 disponíveis; uma segunda compra é recusada por falta de moedas.
	 */
	@Test
	public void compraCustaDuasMoedasEVaiParaAColunaDaCor() {
		azul.adicionarTributo(TipoTributo.MOEDA, 1); // 1 inicial + 1 = 2
		String id = jogo.getMelhoriasDisponiveis().get(0);

		assertTrue(jogo.comprarMelhoria(0));
		assertEquals(0, azul.getQuantidadeTributo(TipoTributo.MOEDA));
		assertEquals(Arrays.asList(id), jogo.getIdsDasMelhoriasDaColuna("AZUL", colunaDoId(id).name()));
		assertEquals(4, jogo.getMelhoriasDisponiveis().size());
		assertFalse(jogo.podeComprarMelhoria(0));
	}

	/**
	 * Objetivo: verificar que completar uma linha dá uma melhoria grátis na
	 * coluna branca, de qualquer cor.
	 * Entrada: AZUL já tem melhoria nas duas colunas coloridas que não são da
	 * cor da 1ª disponível; compra a 1ª disponível e escolhe a grátis do topo.
	 * Resultado esperado: 1 melhoria grátis pendente; depois da escolha, a
	 * coluna branca tem 1 peça e não há mais pendência.
	 */
	@Test
	public void linhaCompletaDaMelhoriaGratisNaColunaBranca() {
		ColunaJogador corDaCompra = colunaDoId(jogo.getMelhoriasDisponiveis().get(0));
		for (ColunaJogador c : ColunaJogador.values()) {
			if (c.ehColorida() && c != corDaCompra) {
				azul.getTabuleiro().adicionarMelhoria(c, TipoMelhoria.YURT_VIRTUAL);
			}
		}
		azul.adicionarTributo(TipoTributo.MOEDA, 1);

		assertTrue(jogo.comprarMelhoria(0));
		assertEquals(1, jogo.getMelhoriasGratisPendentes());
		assertFalse("não compra com melhoria grátis pendente", jogo.podeComprarMelhoria(0));

		assertTrue(jogo.escolherMelhoriaGratis(-1));
		assertEquals(0, jogo.getMelhoriasGratisPendentes());
		assertEquals(1, jogo.getIdsDasMelhoriasDaColuna("AZUL", "BRANCA").size());
	}

	/**
	 * Objetivo: verificar a entrega a um conselheiro: só depois de parar nele,
	 * só com o pedido exato, e a primeira entrega leva a peça de bônus.
	 * Entrada: AZUL tem 4 ferros; tenta entregar antes de se mover; anda até
	 * R11; entrega [FERRO, FERRO] duas vezes; tenta entregar [FERRO, PELE].
	 * Resultado esperado: a entrega antes do movimento é recusada; cada
	 * entrega dá 1 voto; o bônus vem só na primeira; o pedido errado é
	 * recusado.
	 */
	@Test
	public void entregaDaVotoEBonusSoNaPrimeira() {
		azul.adicionarTesouro(TipoTesouro.FERRO, 4);
		azul.adicionarTesouro(TipoTesouro.PELE, 1);
		List<String> doisFerros = Arrays.asList("FERRO", "FERRO");
		String bonus = jogo.getBonusDoConselheiro("R11");
		assertNotNull(bonus);

		assertFalse(jogo.podeEntregar("R11", doisFerros));

		jogo.definirMovimentosDaVez(1);
		assertFalse(jogo.moverPeaoDaVezAte(1, "R11").isEmpty());
		assertEquals(Arrays.asList("R11"), jogo.getConselheirosAlcancaveis());

		int votos = azul.getVotos();
		assertTrue(jogo.entregar("R11", doisFerros));
		assertEquals(Arrays.asList(bonus), jogo.getBonusDisponiveis("AZUL"));
		assertNull(jogo.getBonusDoConselheiro("R11"));
		assertTrue(jogo.entregar("R11", doisFerros));
		assertEquals(1, jogo.getBonusDisponiveis("AZUL").size());
		assertEquals(0, azul.getQuantidadeTesouro(TipoTesouro.FERRO));
		assertTrue(azul.getVotos() >= votos + 2); // + cartas abertas que tenha cumprido

		assertFalse(jogo.entregar("R11", Arrays.asList("FERRO", "PELE")));
	}

	/**
	 * Objetivo: verificar que as cartas abertas são entregues sozinhas a
	 * quem cumpre a demanda, com 1 voto, e saem do jogo.
	 * Entrada: AZUL recebe 4 espadas, 5 tesouros diferentes e 3 bônus; o
	 * jogo confere as cartas reveladas.
	 * Resultado esperado: cada carta revelada cuja demanda AZUL cumpre vai
	 * para ele (1 voto cada) e sai das disponíveis.
	 */
	@Test
	public void cartasAbertasSaoConquistadasAutomaticamente() {
		azul.adicionarTributo(TipoTributo.ESPADA, 4);
		for (TipoTesouro t : TipoTesouro.values()) {
			azul.adicionarTesouro(t, 1);
		}
		azul.receberBonus(TipoBonus.VOTO);
		azul.receberBonus(TipoBonus.DOIS_PUNHOS);
		azul.receberBonus(TipoBonus.DUAS_MOEDAS);

		int esperadas = 0;
		for (String carta : jogo.getCartasAbertasDisponiveis()) {
			if (Apuracao.cumpre(TipoConselheiroAberto.valueOf(carta), azul, tabuleiro, 0,
					java.util.Collections.<TipoTesouro>emptyList())) {
				esperadas++;
			}
		}
		int disponiveisAntes = jogo.getCartasAbertasDisponiveis().size();

		List<String> conquistadas = jogo.verificarCartasAbertas();
		assertEquals(esperadas, conquistadas.size());
		assertEquals(esperadas, azul.getVotos());
		assertEquals(disponiveisAntes - esperadas, jogo.getCartasAbertasDisponiveis().size());
		java.util.Collections.sort(conquistadas);
		assertEquals(conquistadas, jogo.getCartasAbertasConquistadas("AZUL"));
	}

	/**
	 * Objetivo: verificar as contagens dos conselheiros secretos Viajante e
	 * Mensageiro e os votos da carta.
	 * Entrada: AZUL constrói yurts em R08, R09 e R10 (vizinhas) e em R11
	 * (parada dupla, ligada a R10).
	 * Resultado esperado: maior grupo conectado = 4; 1 parada dupla com yurt.
	 */
	@Test
	public void contagensDosConselheirosSecretos() {
		for (String id : new String[] { "R08", "R09", "R10", "R11" }) {
			assertTrue(tabuleiro.construirYurt(azul, id));
		}
		assertEquals(4, Apuracao.quantidadeSecreto(TipoConselheiroSecreto.VIAJANTE, azul, tabuleiro));
		assertEquals(1, Apuracao.quantidadeSecreto(TipoConselheiroSecreto.MENSAGEIRO, azul, tabuleiro));
		assertEquals(1, TipoConselheiroSecreto.VIAJANTE.calcularVotos(4));
	}

	/**
	 * Objetivo: verificar a pontuação de influência por região (págs. 14-15).
	 * Entrada: na Rússia, AZUL tem 2 yurts e VERDE 1; AZUL tem 2 melhorias na
	 * coluna AMARELA (Rússia) e VERDE tem 1.
	 * Resultado esperado: AZUL (1º, 2 melhorias) = 2 votos; VERDE (2º, 1
	 * melhoria) = 1 voto; com empate em 1º, os dois ficam com os votos de 1º.
	 */
	@Test
	public void influenciaPorRegiao() {
		tabuleiro.construirYurt(azul, "R01");
		tabuleiro.construirYurt(azul, "R02");
		tabuleiro.construirYurt(verde, "R03");
		azul.getTabuleiro().adicionarMelhoria(ColunaJogador.AMARELA, TipoMelhoria.PUNHO);
		azul.getTabuleiro().adicionarMelhoria(ColunaJogador.AMARELA, TipoMelhoria.MOEDA_VIRTUAL);
		verde.getTabuleiro().adicionarMelhoria(ColunaJogador.AMARELA, TipoMelhoria.PUNHO);

		assertEquals(2, jogo.getVotosDeInfluencia("AZUL"));
		assertEquals(1, jogo.getVotosDeInfluencia("VERDE"));

		tabuleiro.construirYurt(verde, "R05"); // empate 2 x 2: os dois em 1º
		assertEquals(2, jogo.getVotosDeInfluencia("AZUL"));
		assertEquals(1, jogo.getVotosDeInfluencia("VERDE")); // 1º com 1 melhoria = 1 voto
	}

}

package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * API pública do Model: é a única classe do pacote que o View e o Controller
 * podem usar. Ela junta os jogadores, o tabuleiro e as cartas, e só expõe
 * tipos simples (String, int, boolean), já que as classes do jogo não são
 * públicas. As cores são passadas pelo nome ("AZUL", "VERMELHO", ...).
 *
 * Na 3ª iteração esta classe vai virar o Singleton/Façade pedido no
 * enunciado.
 */
public class Jogo {
	/** Votos da ficha branca que disparam o fim do jogo para 2/3/4/5 jogadores (pág. 14). */
	private static final int[] VOTOS_PARA_O_FIM = { 10, 14, 16, 17 };

	private final GerenciadorJogadores gerenciador;
	private final Tabuleiro tabuleiro;
	private final DistribuidorCartas distribuidor;

	/** Coluna ativada pelo jogador da vez neste turno ({@code null} se nenhuma). */
	private ColunaJogador colunaAtivadaNaVez = null;
	/** Movimentos (cavalos) que o jogador da vez tem neste turno. */
	private int movimentosDaVez = 0;

	// --- melhorias, tesouros, conselheiros e bônus (parte do Rafael) ---
	/** Preço de uma melhoria, em moedas (pág. 11). */
	private static final int PRECO_MELHORIA = 2;
	/** Marca, no pedido de um conselheiro, um tesouro de qualquer tipo. */
	public static final String QUALQUER_TESOURO = "QUALQUER";

	private final MercadoMelhorias mercado;
	private final ReservaTesouros reservaTesouros;
	private final Conselho conselho = new Conselho();
	/** Paradas em que o jogador da vez esteve neste turno (para as entregas). */
	private final Set<String> paradasVisitadasNaVez = new HashSet<String>();
	/** Peças de tributo virtuais já usadas neste turno. */
	private final Map<TipoTributo, Integer> virtuaisUsados = new EnumMap<TipoTributo, Integer>(TipoTributo.class);
	/** Tesouros virtuais já usados neste turno. */
	private final List<TipoTesouro> tesourosVirtuaisUsados = new ArrayList<TipoTesouro>();
	/** Tesouros tirados pelo bônus de 3 tesouros, esperando a escolha. */
	private final List<TipoTesouro> tesourosDoBonusParaEscolher = new ArrayList<TipoTesouro>();
	private int melhoriasGratisPendentes = 0;
	private int punhosExtrasDaVez = 0;

	// --- ações no mapa (tributos, yurts, ataques, Khan) ---
	private static final int TESOUROS_POR_CIDADE = 4;
	/** Lugar do Khan fora das províncias. */
	public static final String KHAN_NA_AREA_DE_MELHORIAS = Tabuleiro.AREA_DE_MELHORIAS;
	private final TurnoNoMapa turnoNoMapa = new TurnoNoMapa();

	/**
	 * Cria e prepara uma partida: jogadores, tabuleiro e cartas.
	 *
	 * @param coresEmOrdemDeJogo nomes das cores escolhidas, na ordem de jogo
	 */
	public Jogo(List<String> coresEmOrdemDeJogo) {
		this(converterCores(coresEmOrdemDeJogo), FabricaTabuleiro.criarTabuleiroPadrao(), new Random());
	}

	/** Usado pelos testes, com um mapa pequeno e semente fixa. */
	Jogo(List<Cor> cores, Tabuleiro tabuleiro, Random aleatorio) {
		this.gerenciador = new GerenciadorJogadores(cores);
		this.tabuleiro = tabuleiro;
		this.distribuidor = new DistribuidorCartas(aleatorio);
		tabuleiro.preparar(gerenciador.getJogadoresEmOrdem(), aleatorio);
		distribuidor.prepararCartas(gerenciador.getJogadoresEmOrdem());
		this.mercado = new MercadoMelhorias(aleatorio);
		this.reservaTesouros = new ReservaTesouros(aleatorio);
		conselho.distribuirBonus(aleatorio);
		for (TipoTributo t : TipoTributo.values()) {
			virtuaisUsados.put(t, 0);
		}
		prepararTesourosDasCidades();
		turnoNoMapa.iniciar(jogadorDaVez(), tabuleiro);
	}

	// ------------------------------------------------------------------ vez

	public String getJogadorDaVez() {
		return gerenciador.getJogadorDaVez().getCor().name();
	}

	/** Cores dos jogadores da partida, na ordem de jogo. */
	public List<String> getCores() {
		List<String> cores = new ArrayList<String>();
		for (Jogador j : gerenciador.getJogadoresEmOrdem()) {
			cores.add(j.getCor().name());
		}
		return cores;
	}

	/** Passa a vez e retorna a cor do próximo jogador. */
	public String passarVez() {
		encerrarTurnoDaVez();
		colunaAtivadaNaVez = null;
		movimentosDaVez = 0;
		Jogador proximo = gerenciador.passarVez();
		turnoNoMapa.iniciar(proximo, tabuleiro);
		mudouEstado();
		return proximo.getCor().name();
	}

	// ------------------------------------------------- tabuleiro do jogador

	/** Nomes das colunas do tabuleiro do jogador, da esquerda para a direita (VERMELHA, VERDE, AMARELA, BRANCA). */
	public List<String> getColunas() {
		List<String> nomes = new ArrayList<String>();
		for (ColunaJogador c : ColunaJogador.values()) {
			nomes.add(c.name());
		}
		return nomes;
	}

	/** Herdeiro do jogador da cor dada (ex.: "Tolui"); usado pela View para achar a imagem. */
	public String getHerdeiro(String cor) {
		return jogador(cor).getHerdeiro().getNome();
	}

	/** Se a coluna do jogador tem uma ficha de ativação colocada. */
	public boolean colunaOcupada(String cor, String coluna) {
		return jogador(cor).getTabuleiro().colunaOcupada(coluna(coluna));
	}

	public int getFichasAtivacaoDisponiveis(String cor) {
		return jogador(cor).getFichasAtivacaoDisponiveis();
	}

	/** Cavalos da coluna (base + melhorias) = movimentos se ela for ativada. */
	public int getCavalosDaColuna(String cor, String coluna) {
		return jogador(cor).getTabuleiro().getCavalos(coluna(coluna));
	}

	/** Punhos da coluna (base + melhorias) = peças de tributo que podem ser pegas. */
	public int getPunhosDaColuna(String cor, String coluna) {
		return jogador(cor).getTabuleiro().getPunhos(coluna(coluna));
	}

	/** Melhorias da coluna, da mais baixa para a mais alta (ex.: "DOIS_CAVALOS"). */
	public List<String> getMelhoriasDaColuna(String cor, String coluna) {
		List<String> nomes = new ArrayList<String>();
		for (PecaMelhoria m : jogador(cor).getTabuleiro().getMelhorias(coluna(coluna))) {
			nomes.add(m.getTipo().name());
		}
		return nomes;
	}

	/**
	 * Ids das peças de melhoria da coluna, da mais baixa para a mais alta
	 * (ex.: "CAV-V"). A View usa o id para achar a imagem da peça.
	 */
	public List<String> getIdsDasMelhoriasDaColuna(String cor, String coluna) {
		List<String> ids = new ArrayList<String>();
		for (PecaMelhoria m : jogador(cor).getTabuleiro().getMelhorias(coluna(coluna))) {
			ids.add(m.getId());
		}
		return ids;
	}

	/** Se a coluna do tabuleiro do jogador tem o ícone do Khan (usar o Khan é obrigatório). */
	public boolean colunaUsaKhan(String cor, String coluna) {
		return jogador(cor).getTabuleiro().usaKhan(coluna(coluna));
	}

	/** Moedas virtuais impressas na base da coluna do tabuleiro do jogador. */
	public int getMoedasVirtuaisDaColuna(String cor, String coluna) {
		return jogador(cor).getTabuleiro().getAcoesIniciais(coluna(coluna)).getMoedasVirtuais();
	}

	/**
	 * Se o jogador da vez pode ativar a coluna agora: ele ainda não ativou
	 * nenhuma coluna neste turno e a coluna está livre.
	 */
	public boolean podeAtivarColuna(String coluna) {
		return colunaAtivadaNaVez == null
				&& gerenciador.getJogadorDaVez().getTabuleiro().podeAtivar(coluna(coluna));
	}

	/**
	 * O jogador da vez coloca uma ficha de ativação na coluna (pág. 6). O
	 * número de movimentos do turno passa a ser o número de cavalos da
	 * coluna. A coluna branca devolve todas as fichas (pág. 9).
	 *
	 * @return {@code false} se ele já ativou uma coluna neste turno ou se a
	 *         coluna está ocupada (nada é alterado)
	 */
	public boolean ativarColuna(String coluna) {
		ColunaJogador c = coluna(coluna);
		if (!podeAtivarColuna(coluna)) {
			return false;
		}
		TabuleiroJogador tab = gerenciador.getJogadorDaVez().getTabuleiro();
		movimentosDaVez = tab.getCavalos(c); // conta antes de recuperar as fichas
		tab.ativar(c);
		colunaAtivadaNaVez = c;
		return true;
	}

	/** Coluna ativada neste turno, ou {@code null} se nenhuma foi ativada. */
	public String getColunaAtivadaNaVez() {
		return colunaAtivadaNaVez == null ? null : colunaAtivadaNaVez.name();
	}

	/** Movimentos do jogador da vez neste turno (o equivalente aos dados). */
	public int getMovimentosDaVez() {
		return movimentosDaVez;
	}

	/**
	 * Define à mão os movimentos do turno, para testes (como escolher o valor
	 * dos dados no enunciado).
	 */
	public void definirMovimentosDaVez(int movimentos) {
		if (movimentos < 0) {
			throw new IllegalArgumentException("Movimentos negativos: " + movimentos);
		}
		movimentosDaVez = movimentos;
	}

	// ------------------------------------------------------------ movimento

	/**
	 * Move um peão do jogador pelo caminho dado (pág. 7).
	 *
	 * @param numeroPeao 1 (Altani também tem o peão 2)
	 * @param caminho    ids das paradas percorridas; a última é o destino
	 * @param movimentos movimentos (cavalos) que o jogador tem neste turno
	 * @return {@code false} se o movimento não respeita as regras
	 */
	public boolean moverPeao(String cor, int numeroPeao, List<String> caminho, int movimentos) {
		return tabuleiro.moverPeao(new Peao(cor(cor), numeroPeao), caminho, movimentos);
	}

	/**
	 * Move um peão até a parada clicada; o caminho é procurado pelo Model
	 * (ver {@link Tabuleiro#moverPeaoAte}).
	 *
	 * @param idDestino  id da parada clicada (ex.: "R05", "KARAKORUM")
	 * @param movimentos movimentos (cavalos) disponíveis
	 * @return ids das paradas em que o peão esteve, da origem ao destino (sem
	 *         os yurts próprios pulados); lista vazia se o movimento não é
	 *         possível, e nesse caso nada muda
	 */
	public List<String> moverPeaoAte(String cor, int numeroPeao, String idDestino, int movimentos) {
		return tabuleiro.moverPeaoAte(new Peao(cor(cor), numeroPeao), idDestino, movimentos);
	}

	/** Se o peão consegue chegar à parada com esses movimentos (nada muda). */
	public boolean podeMoverPeaoAte(String cor, int numeroPeao, String idDestino, int movimentos) {
		return tabuleiro.podeMoverPeaoAte(new Peao(cor(cor), numeroPeao), idDestino, movimentos);
	}

	/**
	 * Move um peão do jogador da vez até a parada clicada, usando os
	 * movimentos do turno ({@link #getMovimentosDaVez()}). Os movimentos
	 * gastos são descontados: quem visitou n paradas além da origem gastou n.
	 *
	 * @return ids das paradas em que o peão esteve; lista vazia se não é possível
	 */
	public List<String> moverPeaoDaVezAte(int numeroPeao, String idDestino) {
		if (!turnoNoMapa.podeUsarPeao(numeroPeao)) {
			return new ArrayList<String>(); // Altani já usou o outro peão neste turno
		}
		Cor corDaVez = gerenciador.getJogadorDaVez().getCor();
		List<String> visitadas = tabuleiro.moverPeaoAte(new Peao(corDaVez, numeroPeao), idDestino, movimentosDaVez);
		if (!visitadas.isEmpty()) {
			movimentosDaVez -= visitadas.size() - 1;
			// paradas feitas no movimento (a de origem não conta), para as entregas
			paradasVisitadasNaVez.addAll(visitadas.subList(1, visitadas.size()));
			turnoNoMapa.registrarMovimento(numeroPeao, visitadas); // aqui a origem conta
			mudouEstado();
		}
		return visitadas;
	}

	/** Id da parada onde está o peão (para a View desenhá-lo), ou {@code null}. */
	public String getParadaDoPeao(String cor, int numeroPeao) {
		Parada parada = tabuleiro.getParadaDoPeao(new Peao(cor(cor), numeroPeao));
		return parada == null ? null : parada.getId();
	}

	/** Quantos peões o jogador tem (Altani tem 2). */
	public int getNumeroDePeoes(String cor) {
		return cor(cor).getHerdeiro().getNumeroDePeoes();
	}

	/** Ids de todas as paradas do mapa. */
	public List<String> getParadas() {
		List<String> ids = new ArrayList<String>();
		for (Parada parada : tabuleiro.getParadas()) {
			ids.add(parada.getId());
		}
		return ids;
	}

	// =====================================================================
	// Tabuleiro do jogador, melhorias, tesouros, conselheiros e cartas
	// (parte do Rafael na 3ª iteração)
	// =====================================================================

	// ------------------------------------------------ recursos do jogador

	/** Votos do jogador na trilha do Kurultai. */
	public int getVotos(String cor) {
		return jogador(cor).getVotos();
	}

	/** Peças de tributo que o jogador tem ("ESPADA", "YURT" ou "MOEDA"). */
	public int getTributo(String cor, String tipo) {
		return jogador(cor).getQuantidadeTributo(TipoTributo.valueOf(tipo));
	}

	/** Tipos de tesouro, na ordem em que a View deve mostrá-los. */
	public List<String> getTiposDeTesouro() {
		List<String> tipos = new ArrayList<String>();
		for (TipoTesouro t : TipoTesouro.values()) {
			tipos.add(t.name());
		}
		return tipos;
	}

	public int getTesouro(String cor, String tipo) {
		return jogador(cor).getQuantidadeTesouro(TipoTesouro.valueOf(tipo));
	}

	/** Tesouros ainda não entregues (desempate do vencedor, pág. 16). */
	public int getTotalTesouros(String cor) {
		return jogador(cor).getTotalTesouros();
	}

	/** Yurts que o jogador ainda tem na reserva. */
	public int getYurtsNaReserva(String cor) {
		return jogador(cor).getYurtsDisponiveis();
	}

	/**
	 * Peças de tributo do tipo que o jogador da vez pode gastar agora: as
	 * reais mais as virtuais da coluna ativada que ainda não foram usadas
	 * neste turno (pág. 16). As ações de atacar e construir também devem usar
	 * este método e {@link #gastarTributoDaVez}.
	 */
	public int getTributoDisponivelDaVez(String tipo) {
		TipoTributo t = TipoTributo.valueOf(tipo);
		return jogadorDaVez().getQuantidadeTributo(t) + virtuaisRestantes(t);
	}

	/**
	 * Gasta peças de tributo do jogador da vez, usando primeiro as virtuais
	 * da coluna ativada. Se não houver o suficiente, nada muda.
	 */
	public boolean gastarTributoDaVez(String tipo, int quantidade) {
		return gastarTributo(TipoTributo.valueOf(tipo), quantidade);
	}

	private boolean gastarTributo(TipoTributo tipo, int quantidade) {
		if (quantidade < 0) {
			throw new IllegalArgumentException("Quantidade negativa");
		}
		int virtuais = Math.min(virtuaisRestantes(tipo), quantidade);
		int reais = quantidade - virtuais;
		if (!jogadorDaVez().gastarTributo(tipo, reais)) {
			return false;
		}
		virtuaisUsados.put(tipo, virtuaisUsados.get(tipo) + virtuais);
		return true;
	}

	private int virtuaisRestantes(TipoTributo tipo) {
		if (colunaAtivadaNaVez == null) {
			return 0;
		}
		int total = jogadorDaVez().getTabuleiro().getTributosVirtuais(colunaAtivadaNaVez, tipo);
		return Math.max(0, total - virtuaisUsados.get(tipo));
	}

	/** Tesouros virtuais da coluna ativada ainda não usados neste turno. */
	private List<TipoTesouro> tesourosVirtuaisRestantes() {
		List<TipoTesouro> restantes = new ArrayList<TipoTesouro>();
		if (colunaAtivadaNaVez != null) {
			restantes.addAll(jogadorDaVez().getTabuleiro().getTesourosVirtuais(colunaAtivadaNaVez));
			for (TipoTesouro usado : tesourosVirtuaisUsados) {
				restantes.remove(usado);
			}
		}
		return restantes;
	}

	/** Nomes dos tesouros virtuais que o jogador da vez ainda pode usar neste turno. */
	public List<String> getTesourosVirtuaisDaVez() {
		List<String> nomes = new ArrayList<String>();
		for (TipoTesouro t : tesourosVirtuaisRestantes()) {
			nomes.add(t.name());
		}
		return nomes;
	}

	/** Ações extras de pegar tributo dadas por peças de bônus neste turno. */
	public int getPunhosExtrasDaVez() {
		return punhosExtrasDaVez;
	}

	/** Gasta uma ação extra de pegar tributo (para a ação de pegar tributos). */
	public boolean usarPunhoExtraDaVez() {
		if (punhosExtrasDaVez == 0) {
			return false;
		}
		punhosExtrasDaVez--;
		return true;
	}

	// ------------------------------------------------------------ melhorias

	/** Ids das melhorias viradas para cima (ex.: "CAV-V"), da esquerda para a direita. */
	public List<String> getMelhoriasDisponiveis() {
		List<String> ids = new ArrayList<String>();
		for (PecaMelhoria m : mercado.getDisponiveis()) {
			ids.add(m.getId());
		}
		return ids;
	}

	/** Peças na pilha de melhorias (compra às cegas). */
	public int getTamanhoPilhaMelhorias() {
		return mercado.getTamanhoDaPilha();
	}

	/** Preço de uma melhoria, em moedas (pág. 11). */
	public int getPrecoMelhoria() {
		return PRECO_MELHORIA;
	}

	/**
	 * Se o jogador da vez pode comprar a melhoria: tem 2 moedas (contando as
	 * virtuais), não tem melhoria grátis para escolher antes e a coluna da cor
	 * da peça tem espaço.
	 *
	 * @param indice posição em {@link #getMelhoriasDisponiveis()}, ou -1 para
	 *               comprar a do topo da pilha (aleatória)
	 */
	public boolean podeComprarMelhoria(int indice) {
		if (melhoriasGratisPendentes > 0 || getTributoDisponivelDaVez("MOEDA") < PRECO_MELHORIA) {
			return false;
		}
		TabuleiroJogador tab = jogadorDaVez().getTabuleiro();
		if (indice == -1) {
			if (mercado.verTopo() == null) {
				return false;
			}
			for (ColunaJogador c : ColunaJogador.values()) {
				if (c.ehColorida() && tab.temEspaco(c)) {
					return true;
				}
			}
			return false;
		}
		PecaMelhoria peca = mercado.verDisponivel(indice);
		return peca != null && tab.temEspaco(peca.getCor());
	}

	/**
	 * Compra uma melhoria com 2 moedas e a coloca na coluna da cor dela
	 * (pág. 11). Se a compra completar uma linha de 3 melhorias, o jogador
	 * ganha uma melhoria grátis para a coluna branca, escolhida depois com
	 * {@link #escolherMelhoriaGratis(int)}. A melhoria comprada só vale a
	 * partir do próximo turno.
	 *
	 * @param indice posição em {@link #getMelhoriasDisponiveis()}, ou -1 para o topo da pilha
	 * @return {@code false} se a compra não é possível (nada muda); se a peça
	 *         do topo não couber no tabuleiro, ela volta para o fundo da pilha
	 */
	public boolean comprarMelhoria(int indice) {
		if (!podeComprarMelhoria(indice)) {
			return false;
		}
		TabuleiroJogador tab = jogadorDaVez().getTabuleiro();
		PecaMelhoria peca;
		if (indice == -1) {
			peca = mercado.retirarDoTopo();
			if (!tab.temEspaco(peca.getCor())) {
				mercado.devolverAoFundo(peca);
				return false;
			}
		} else {
			peca = mercado.retirarDisponivel(indice);
		}
		gastarTributo(TipoTributo.MOEDA, PRECO_MELHORIA);
		int linhasAntes = tab.getLinhasCompletas();
		tab.adicionarMelhoria(peca.getCor(), peca);
		melhoriasGratisPendentes += tab.getLinhasCompletas() - linhasAntes;
		verificarCartasAbertas();
		mudouEstado();
		return true;
	}

	/** Melhorias grátis que o jogador da vez ainda precisa escolher para a coluna branca. */
	public int getMelhoriasGratisPendentes() {
		return melhoriasGratisPendentes;
	}

	/**
	 * Escolhe a melhoria grátis ganha ao completar uma linha e a coloca na
	 * coluna branca, que aceita qualquer cor (pág. 12).
	 *
	 * @param indice posição em {@link #getMelhoriasDisponiveis()}, ou -1 para o topo da pilha
	 */
	public boolean escolherMelhoriaGratis(int indice) {
		if (melhoriasGratisPendentes == 0) {
			return false;
		}
		PecaMelhoria peca;
		if (indice == -1) {
			if (mercado.verTopo() == null) {
				return false;
			}
			peca = mercado.retirarDoTopo();
		} else {
			if (mercado.verDisponivel(indice) == null) {
				return false;
			}
			peca = mercado.retirarDisponivel(indice);
		}
		jogadorDaVez().getTabuleiro().adicionarMelhoria(ColunaJogador.BRANCA, peca);
		melhoriasGratisPendentes--;
		verificarCartasAbertas();
		mudouEstado();
		return true;
	}

	/**
	 * Renova as melhorias disponíveis (quando o Khan é colocado na área de
	 * melhorias): as viradas para cima vão para o fundo da pilha e 5 novas
	 * são reveladas.
	 */
	public void renovarMelhorias() {
		mercado.renovar();
		mudouEstado();
	}

	// ------------------------------------------------------- conselheiros

	/** Ids das paradas com conselheiro (as 6 duplas e Karakorum). */
	public List<String> getConselheiros() {
		List<String> ids = new ArrayList<String>();
		for (Conselheiro c : conselho.getConselheiros()) {
			ids.add(c.getIdParada());
		}
		java.util.Collections.sort(ids);
		return ids;
	}

	/**
	 * Tesouros que o conselheiro pede numa entrega (ex.: ["SEDA", "SEDA"]).
	 * Para o conselheiro que aceita qualquer tipo, a lista traz "QUALQUER"
	 * uma vez para cada tesouro pedido.
	 */
	public List<String> getPedidoDoConselheiro(String idParada) {
		Conselheiro c = conselheiro(idParada);
		List<String> pedido = new ArrayList<String>();
		if (c.aceitaQualquerTipo()) {
			for (int i = 0; i < c.getTamanhoDaEntrega(); i++) {
				pedido.add(QUALQUER_TESOURO);
			}
		} else {
			for (Map.Entry<TipoTesouro, Integer> e : c.getPedido().entrySet()) {
				for (int i = 0; i < e.getValue(); i++) {
					pedido.add(e.getKey().name());
				}
			}
		}
		return pedido;
	}

	public int getVotosPorEntrega(String idParada) {
		return conselheiro(idParada).getVotosPorEntrega();
	}

	/** Peça de bônus que ainda está sobre o conselheiro, ou {@code null}. */
	public String getBonusDoConselheiro(String idParada) {
		TipoBonus b = conselheiro(idParada).getBonus();
		return b == null ? null : b.name();
	}

	/**
	 * Conselheiros em que o jogador da vez fez uma parada neste turno e
	 * portanto pode fazer entregas (pág. 12).
	 */
	public List<String> getConselheirosAlcancaveis() {
		List<String> ids = new ArrayList<String>();
		for (String id : getConselheiros()) {
			if (fezParadaNoConselheiro(id)) {
				ids.add(id);
			}
		}
		return ids;
	}

	/**
	 * Se o jogador da vez pode entregar estes tesouros ao conselheiro: fez
	 * parada nele neste turno, a lista é exatamente o pedido do conselheiro e
	 * ele tem os tesouros (os virtuais da coluna ativada também valem, um
	 * tesouro virtual por peça e por turno).
	 */
	public boolean podeEntregar(String idParada, List<String> tesouros) {
		return planejarEntrega(idParada, tesouros) != null;
	}

	/**
	 * Entrega os tesouros ao conselheiro: o jogador ganha os votos da entrega
	 * e, se for a primeira entrega a esse conselheiro, a peça de bônus que
	 * está sobre ele (págs. 12-13 e 17).
	 *
	 * @return {@code false} se a entrega não é possível (nada muda)
	 */
	public boolean entregar(String idParada, List<String> tesouros) {
		List<TipoTesouro> virtuais = planejarEntrega(idParada, tesouros);
		if (virtuais == null) {
			return false;
		}
		Jogador j = jogadorDaVez();
		List<TipoTesouro> reais = converterTesouros(tesouros);
		for (TipoTesouro v : virtuais) {
			reais.remove(v);
		}
		for (TipoTesouro t : reais) {
			j.removerTesouro(t, 1);
		}
		tesourosVirtuaisUsados.addAll(virtuais);
		turnoNoMapa.usarBase(java.util.Collections.singletonList(tabuleiro.getParada(idParada)));
		Conselheiro c = conselheiro(idParada);
		j.ganharVotos(c.getVotosPorEntrega());
		TipoBonus bonus = c.registrarEntrega();
		if (bonus != null) {
			j.receberBonus(bonus);
		}
		verificarCartasAbertas();
		mudouEstado();
		return true;
	}

	/**
	 * Uma entrega possível para o conselheiro com os tesouros que o jogador
	 * da vez tem, ou lista vazia se não há nenhuma. Para o conselheiro que
	 * aceita qualquer tipo, usa primeiro os tesouros que o jogador tem mais.
	 */
	public List<String> sugerirEntrega(String idParada) {
		Conselheiro c = conselheiro(idParada);
		List<String> sugestao = new ArrayList<String>();
		if (!c.aceitaQualquerTipo()) {
			sugestao = getPedidoDoConselheiro(idParada);
		} else {
			Map<TipoTesouro, Integer> disponivel = tesourosDisponiveisDaVez();
			for (int i = 0; i < c.getTamanhoDaEntrega(); i++) {
				TipoTesouro maior = null;
				for (TipoTesouro t : TipoTesouro.values()) {
					if (disponivel.get(t) > 0 && (maior == null || disponivel.get(t) > disponivel.get(maior))) {
						maior = t;
					}
				}
				if (maior == null) {
					return new ArrayList<String>();
				}
				disponivel.put(maior, disponivel.get(maior) - 1);
				sugestao.add(maior.name());
			}
		}
		return podeEntregar(idParada, sugestao) ? sugestao : new ArrayList<String>();
	}

	/** Reais + virtuais da vez, por tipo. */
	private Map<TipoTesouro, Integer> tesourosDisponiveisDaVez() {
		Map<TipoTesouro, Integer> m = new EnumMap<TipoTesouro, Integer>(TipoTesouro.class);
		for (TipoTesouro t : TipoTesouro.values()) {
			m.put(t, jogadorDaVez().getQuantidadeTesouro(t));
		}
		for (TipoTesouro v : tesourosVirtuaisRestantes()) {
			m.put(v, m.get(v) + 1);
		}
		return m;
	}

	/**
	 * Confere uma entrega e decide quais tesouros virtuais ela usa (os
	 * virtuais só entram quando faltam tesouros reais).
	 *
	 * @return os virtuais usados, ou {@code null} se a entrega não é possível
	 */
	private List<TipoTesouro> planejarEntrega(String idParada, List<String> nomes) {
		Conselheiro c = conselho.getConselheiro(idParada);
		if (c == null || nomes == null || !fezParadaNoConselheiro(idParada)) {
			return null;
		}
		List<TipoTesouro> tesouros;
		try {
			tesouros = converterTesouros(nomes);
		} catch (IllegalArgumentException e) {
			return null;
		}
		if (!c.aceita(tesouros)) {
			return null;
		}
		Jogador j = jogadorDaVez();
		List<TipoTesouro> virtuaisLivres = tesourosVirtuaisRestantes();
		List<TipoTesouro> usados = new ArrayList<TipoTesouro>();
		for (TipoTesouro t : TipoTesouro.values()) {
			int pedidos = 0;
			for (TipoTesouro x : tesouros) {
				if (x == t) {
					pedidos++;
				}
			}
			int faltam = pedidos - j.getQuantidadeTesouro(t);
			while (faltam > 0) {
				if (!virtuaisLivres.remove(t)) {
					return null;
				}
				usados.add(t);
				faltam--;
			}
		}
		return usados;
	}

	// a segunda condição é por causa da Altani: o peão que ela não escolheu
	// no turno não pode entregar tesouro
	private boolean fezParadaNoConselheiro(String idParada) {
		return paradasVisitadasNaVez.contains(idParada) && turnoNoMapa.esteveEm(idParada);
	}

	private Conselheiro conselheiro(String idParada) {
		Conselheiro c = conselho.getConselheiro(idParada);
		if (c == null) {
			throw new IllegalArgumentException("A parada " + idParada + " não tem conselheiro");
		}
		return c;
	}

	private static List<TipoTesouro> converterTesouros(List<String> nomes) {
		List<TipoTesouro> lista = new ArrayList<TipoTesouro>();
		for (String n : nomes) {
			lista.add(TipoTesouro.valueOf(n));
		}
		return lista;
	}

	// ------------------------------------------------------- peças de bônus

	/** Peças de bônus que o jogador tem e ainda não usou (ex.: "DUAS_ESPADAS"). */
	public List<String> getBonusDisponiveis(String cor) {
		List<String> nomes = new ArrayList<String>();
		for (TipoBonus b : jogador(cor).getBonusDisponiveis()) {
			nomes.add(b.name());
		}
		return nomes;
	}

	public String getDescricaoBonus(String tipo) {
		return TipoBonus.valueOf(tipo).getDescricao();
	}

	/**
	 * O jogador da vez usa uma peça de bônus (pág. 17). A de 3 tesouros tira
	 * 3 tesouros da pilha; o jogador escolhe 1 com
	 * {@link #escolherTesouroDoBonus(String)}.
	 *
	 * @return {@code false} se ele não tem a peça ou ainda precisa escolher o
	 *         tesouro de um bônus anterior
	 */
	public boolean usarBonus(String tipo) {
		TipoBonus b = TipoBonus.valueOf(tipo);
		Jogador j = jogadorDaVez();
		if (!tesourosDoBonusParaEscolher.isEmpty() || !j.usarBonus(b)) {
			return false;
		}
		switch (b) {
		case VOTO:
			j.ganharVotos(1);
			break;
		case DUAS_ESPADAS:
			j.adicionarTributo(TipoTributo.ESPADA, 2);
			break;
		case DUAS_MOEDAS:
			j.adicionarTributo(TipoTributo.MOEDA, 2);
			break;
		case DOIS_YURTS:
			j.adicionarTributo(TipoTributo.YURT, 2);
			break;
		case TRES_MOVIMENTOS_E_PUNHO:
			movimentosDaVez += 3;
			punhosExtrasDaVez += 1;
			break;
		case DOIS_PUNHOS:
			punhosExtrasDaVez += 2;
			break;
		case TRES_TESOUROS:
			tesourosDoBonusParaEscolher.addAll(reservaTesouros.comprar(3));
			break;
		default:
			throw new IllegalStateException("Bônus desconhecido: " + b);
		}
		verificarCartasAbertas();
		mudouEstado();
		return true;
	}

	/** Tesouros tirados pelo bônus de 3 tesouros, esperando a escolha do jogador. */
	public List<String> getTesourosDoBonusParaEscolher() {
		List<String> nomes = new ArrayList<String>();
		for (TipoTesouro t : tesourosDoBonusParaEscolher) {
			nomes.add(t.name());
		}
		return nomes;
	}

	/** Fica com um dos tesouros do bônus; os outros são descartados (pág. 17). */
	public boolean escolherTesouroDoBonus(String tipo) {
		TipoTesouro t = TipoTesouro.valueOf(tipo);
		if (!tesourosDoBonusParaEscolher.contains(t)) {
			return false;
		}
		jogadorDaVez().adicionarTesouro(t, 1);
		tesourosDoBonusParaEscolher.clear();
		verificarCartasAbertas();
		mudouEstado();
		return true;
	}

	// ----------------------------------------------- cartas de conselheiro

	/** As 2 cartas secretas que o jogador recebeu e ainda não escolheu (ex.: "VIAJANTE"). */
	public List<String> getCartasSecretasParaEscolher(String cor) {
		List<String> nomes = new ArrayList<String>();
		for (CartaConselheiroSecreto c : jogador(cor).getCartasSecretasRecebidas()) {
			nomes.add(c.getTipo().name());
		}
		java.util.Collections.sort(nomes);
		return nomes;
	}

	/** O jogador fica com uma das 2 cartas secretas e descarta a outra (pág. 6). */
	public void escolherConselheiroSecreto(String cor, String tipo) {
		Jogador j = jogador(cor);
		TipoConselheiroSecreto t = TipoConselheiroSecreto.valueOf(tipo);
		for (CartaConselheiroSecreto c : j.getCartasSecretasRecebidas()) {
			if (c.getTipo() == t) {
				distribuidor.escolherConselheiroSecreto(j, c.getId());
				mudouEstado();
				return;
			}
		}
		throw new IllegalArgumentException("O jogador " + cor + " não tem a carta " + tipo);
	}

	/** Se todos os jogadores já escolheram o conselheiro secreto. */
	public boolean todosEscolheramConselheiroSecreto() {
		return distribuidor.todosEscolheramConselheiroSecreto(gerenciador.getJogadoresEmOrdem());
	}

	/** Conselheiro secreto escolhido pelo jogador, ou {@code null}. */
	public String getConselheiroSecreto(String cor) {
		CartaConselheiroSecreto c = jogador(cor).getConselheiroSecreto();
		return c == null ? null : c.getTipo().name();
	}

	public String getNomeCartaSecreta(String tipo) {
		return TipoConselheiroSecreto.valueOf(tipo).getNome();
	}

	public String getDescricaoCartaSecreta(String tipo) {
		return TipoConselheiroSecreto.valueOf(tipo).getDescricao();
	}

	/** Os 3 limiares da carta secreta (quantidade para 1, 2 e 3 votos). */
	public List<Integer> getLimiaresCartaSecreta(String tipo) {
		TipoConselheiroSecreto t = TipoConselheiroSecreto.valueOf(tipo);
		List<Integer> limiares = new ArrayList<Integer>();
		for (int v = 1; v <= 3; v++) {
			limiares.add(t.getLimiar(v));
		}
		return limiares;
	}

	/** Quanto o jogador já atingiu na condição do seu conselheiro secreto (0 se não escolheu). */
	public int getProgressoConselheiroSecreto(String cor) {
		Jogador j = jogador(cor);
		CartaConselheiroSecreto c = j.getConselheiroSecreto();
		return c == null ? 0 : Apuracao.quantidadeSecreto(c.getTipo(), j, tabuleiro);
	}

	/** Votos de fim de jogo do conselheiro secreto (0 a 3, pág. 19). */
	public int getVotosDoConselheiroSecreto(String cor) {
		Jogador j = jogador(cor);
		CartaConselheiroSecreto c = j.getConselheiroSecreto();
		return c == null ? 0 : c.calcularVotos(Apuracao.quantidadeSecreto(c.getTipo(), j, tabuleiro));
	}

	/** Votos de fim de jogo pela influência nas 3 regiões (págs. 14-15). */
	public int getVotosDeInfluencia(String cor) {
		return Apuracao.votosDeInfluencia(jogador(cor), gerenciador.getJogadoresEmOrdem(), tabuleiro);
	}

	/** Votos do Kurultai + influência + conselheiro secreto: o total do fim de jogo. */
	public int getVotosFinais(String cor) {
		return getVotos(cor) + getVotosDeInfluencia(cor) + getVotosDoConselheiroSecreto(cor);
	}

	/** Cartas de conselheiro aberto reveladas e ainda disponíveis (ex.: "ESPADAS_4"). */
	public List<String> getCartasAbertasDisponiveis() {
		List<String> nomes = new ArrayList<String>();
		for (CartaConselheiroAberto c : distribuidor.getCartasAbertasDisponiveis()) {
			nomes.add(c.getTipo().name());
		}
		java.util.Collections.sort(nomes);
		return nomes;
	}

	/** Cartas de conselheiro aberto que o jogador já conquistou. */
	public List<String> getCartasAbertasConquistadas(String cor) {
		List<String> nomes = new ArrayList<String>();
		for (CartaConselheiroAberto c : jogador(cor).getCartasAbertasConquistadas()) {
			nomes.add(c.getTipo().name());
		}
		java.util.Collections.sort(nomes);
		return nomes;
	}

	public String getDescricaoCartaAberta(String tipo) {
		return TipoConselheiroAberto.valueOf(tipo).getDescricao();
	}

	/** Número grande impresso na carta aberta (a quantidade exigida). */
	public int getQuantidadeCartaAberta(String tipo) {
		return TipoConselheiroAberto.valueOf(tipo).getQuantidade();
	}

	/**
	 * Confere se o jogador da vez cumpriu a demanda de alguma carta de
	 * conselheiro aberto revelada; se sim, ele fica com a carta e ganha 1 voto
	 * (págs. 13 e 18). É chamado depois das ações deste trecho da API; as
	 * outras ações (atacar, construir, pegar tributo) também devem chamá-lo.
	 *
	 * @return as cartas conquistadas agora
	 */
	public List<String> verificarCartasAbertas() {
		Jogador j = jogadorDaVez();
		List<String> conquistadas = new ArrayList<String>();
		for (CartaConselheiroAberto carta : new ArrayList<CartaConselheiroAberto>(
				distribuidor.getCartasAbertasDisponiveis())) {
			if (Apuracao.cumpre(carta.getTipo(), j, tabuleiro, virtuaisRestantes(TipoTributo.ESPADA),
					tesourosVirtuaisRestantes()) && distribuidor.entregarCartaAberta(j, carta.getId())) {
				conquistadas.add(carta.getTipo().name());
			}
		}
		if (!conquistadas.isEmpty()) {
			mudouEstado();
		}
		return conquistadas;
	}

	// =====================================================================
	// Ações no mapa: tributos, yurts, ataques, Khan e habilidades
	// =====================================================================

	// Paradas onde o peão da vez já esteve no turno (contando a de onde saiu).
	// É a partir delas que ele pega tributo, constrói yurt e ataca.
	public List<String> getParadasDoTurno() {
		List<String> ids = new ArrayList<String>(turnoNoMapa.getParadasDasAcoes());
		Collections.sort(ids);
		return ids;
	}

	// ----------------------------------------------------------- tributos

	public List<String> getProvincias() {
		List<String> ids = new ArrayList<String>();
		for (Provincia p : tabuleiro.getProvincias()) {
			ids.add(p.getId());
		}
		Collections.sort(ids);
		return ids;
	}

	/** "ESPADA", "YURT" ou "MOEDA". */
	public String getTipoTributoDaProvincia(String idProvincia) {
		return tabuleiro.getProvincia(idProvincia).getTipoTributo().name();
	}

	public int getTributosNaProvincia(String idProvincia) {
		return tabuleiro.getProvincia(idProvincia).getPecasDeTributo();
	}

	/** Punhos que ainda sobram no turno (os da coluna + os extras dos bônus). */
	public int getPunhosDaVez() {
		return punhosDaColunaRestantes() + punhosExtrasDaVez;
	}

	private int punhosDaColunaRestantes() {
		if (colunaAtivadaNaVez == null) {
			return 0;
		}
		int total = jogadorDaVez().getTabuleiro().getPunhos(colunaAtivadaNaVez);
		return Math.max(0, total - turnoNoMapa.getPunhosUsados());
	}

	// precisa ter punho, a província não pode estar vazia e tem que ser
	// vizinha de alguma parada onde o peão passou no turno
	public boolean podePegarTributo(String idProvincia) {
		Provincia p = tabuleiro.getProvincia(idProvincia);
		return getPunhosDaVez() > 0 && !p.estaVazia() && turnoNoMapa.esteveEmAlguma(p.getParadasAdjacentes());
	}

	public List<String> getProvinciasAlcancaveis() {
		List<String> ids = new ArrayList<String>();
		for (String id : getProvincias()) {
			if (podePegarTributo(id)) {
				ids.add(id);
			}
		}
		return ids;
	}

	/** Pega 1 peça da província gastando 1 punho. Retorna false se não puder. */
	public boolean pegarTributo(String idProvincia) {
		if (!podePegarTributo(idProvincia)) {
			return false;
		}
		Provincia p = tabuleiro.getProvincia(idProvincia);
		// gasta primeiro os punhos da coluna, depois os do bônus
		if (punhosDaColunaRestantes() > 0) {
			turnoNoMapa.usarPunho();
		} else {
			usarPunhoExtraDaVez();
		}
		turnoNoMapa.usarBase(p.getParadasAdjacentes());
		p.retirarTributo(1);
		jogadorDaVez().adicionarTributo(p.getTipoTributo(), 1);
		verificarCartasAbertas();
		mudouEstado();
		return true;
	}

	// -------------------------------------------------------------- yurts

	/** Cores dos donos dos yurts da parada. */
	public List<String> getYurtsNaParada(String idParada) {
		List<String> cores = new ArrayList<String>();
		for (Cor c : tabuleiro.getDonosDosYurts(idParada)) {
			cores.add(c.name());
		}
		return cores;
	}

	// O peão tem que ter passado pela parada (ou ser vizinha, no caso do
	// Tolui), ela tem que ter espaço (Karakorum não aceita) e o jogador
	// precisa de um yurt na reserva e de uma peça de yurt pra pagar.
	public boolean podeConstruirYurt(String idParada) {
		Parada parada = tabuleiro.getParada(idParada);
		return (turnoNoMapa.esteveEm(idParada) || vizinhaDoPeaoDeTolui(idParada)) && parada.podeReceberYurt()
				&& jogadorDaVez().getYurtsDisponiveis() > 0 && getTributoDisponivelDaVez("YURT") > 0;
	}

	// habilidade do Tolui: pode construir nas paradas vizinhas de onde ele está
	private boolean vizinhaDoPeaoDeTolui(String idParada) {
		Jogador j = jogadorDaVez();
		if (!j.getHerdeiro().constroiYurtAdjacente()) {
			return false;
		}
		Parada atual = tabuleiro.getParadaDoPeao(new Peao(j.getCor(), 1));
		return atual != null && atual.ehVizinhaDe(idParada);
	}

	public List<String> getParadasParaYurt() {
		List<String> ids = new ArrayList<String>();
		for (String id : getParadas()) {
			if (podeConstruirYurt(id)) {
				ids.add(id);
			}
		}
		Collections.sort(ids);
		return ids;
	}

	/** Constrói um yurt na parada pagando 1 peça de yurt (a virtual vai primeiro). */
	public boolean construirYurt(String idParada) {
		if (!podeConstruirYurt(idParada)) {
			return false;
		}
		Jogador j = jogadorDaVez();
		if (!tabuleiro.construirYurt(j, idParada)) {
			return false;
		}
		gastarTributo(TipoTributo.YURT, 1);
		if (turnoNoMapa.esteveEm(idParada)) {
			turnoNoMapa.usarBase(Collections.singletonList(tabuleiro.getParada(idParada)));
		}
		verificarCartasAbertas();
		mudouEstado();
		return true;
	}

	// ------------------------------------------------------------- cidades

	public List<String> getCidades() {
		List<String> ids = new ArrayList<String>();
		for (NomeCidade n : NomeCidade.values()) {
			ids.add(n.name());
		}
		Collections.sort(ids);
		return ids;
	}

	/** Nome com acento, pra mostrar na tela (ex.: "Bagdá"). */
	public String getNomeDaCidade(String idCidade) {
		return nomeCidade(idCidade).getNome();
	}

	/** "NA_PILHA", "DISPONIVEL" ou "CONQUISTADA". */
	public String getEstadoDaCidade(String idCidade) {
		return cidade(idCidade).getEstado().name();
	}

	public List<String> getTesourosDaCidade(String idCidade) {
		List<String> nomes = new ArrayList<String>();
		for (TipoTesouro t : cidade(idCidade).getTesouros()) {
			nomes.add(t.name());
		}
		return nomes;
	}

	/** Cor de quem conquistou, ou null. */
	public String getConquistadorDaCidade(String idCidade) {
		Cor c = cidade(idCidade).getConquistador();
		return c == null ? null : c.name();
	}

	// o conquistador pode ter ficado sem yurt pra colocar na cidade
	public boolean cidadeTemYurt(String idCidade) {
		Cidade c = cidade(idCidade);
		return c.getConquistador() != null && c.temYurtDe(c.getConquistador());
	}

	public int getCidadesNaPilha() {
		return tabuleiro.getNumeroDeCidadesNaPilha();
	}

	/** Quantas espadas custa o próximo tesouro dessa cidade neste turno (1 ou 2). */
	public int getCustoDeAtaque(String idCidade) {
		return turnoNoMapa.custoDoAtaque(nomeCidade(idCidade));
	}

	// a cidade tem que estar disponível e ter o tesouro, o peão tem que ter
	// parado do lado dela no turno e o jogador precisa das espadas
	public boolean podeAtacar(String idCidade, String tesouro) {
		Cidade c = cidade(idCidade);
		TipoTesouro t = tesouroOuNulo(tesouro);
		if (t == null) {
			return false;
		}
		return c.getEstado() == Cidade.Estado.DISPONIVEL && c.temTesouro(t)
				&& turnoNoMapa.esteveEmAlguma(c.getParadasAdjacentes())
				&& getTributoDisponivelDaVez("ESPADA") >= turnoNoMapa.custoDoAtaque(c.getNome());
	}

	public List<String> getCidadesAtacaveis() {
		List<String> ids = new ArrayList<String>();
		for (String id : getCidades()) {
			for (String t : getTesourosDaCidade(id)) {
				if (podeAtacar(id, t)) {
					ids.add(id);
					break;
				}
			}
		}
		return ids;
	}

	/**
	 * Ataca a cidade e pega o tesouro escolhido. O primeiro tesouro da cidade
	 * no turno custa 1 espada e os outros custam 2. Quem pega o último
	 * conquista a cidade e uma nova é revelada com 4 tesouros.
	 */
	public boolean atacarCidade(String idCidade, String tesouro) {
		if (!podeAtacar(idCidade, tesouro)) {
			return false;
		}
		Cidade c = cidade(idCidade);
		TipoTesouro t = TipoTesouro.valueOf(tesouro);
		Jogador j = jogadorDaVez();
		gastarTributo(TipoTributo.ESPADA, turnoNoMapa.custoDoAtaque(c.getNome()));
		turnoNoMapa.registrarAtaque(c.getNome());
		turnoNoMapa.usarBase(c.getParadasAdjacentes());
		c.retirarTesouro(t);
		j.adicionarTesouro(t, 1);
		if (!c.temTesouros()) {
			Cidade revelada = tabuleiro.conquistarCidade(c.getNome(), j);
			if (revelada != null) { // null quando a pilha de cidades acabou
				revelada.colocarTesouros(reservaTesouros.comprar(TESOUROS_POR_CIDADE));
			}
		}
		verificarCartasAbertas();
		mudouEstado();
		return true;
	}

	// preparação: 4 tesouros em cada cidade que começa revelada
	private void prepararTesourosDasCidades() {
		for (NomeCidade n : NomeCidade.values()) {
			Cidade c = tabuleiro.getCidade(n);
			if (c.getEstado() == Cidade.Estado.DISPONIVEL && !c.temTesouros()) {
				c.colocarTesouros(reservaTesouros.comprar(TESOUROS_POR_CIDADE));
			}
		}
	}

	private Cidade cidade(String idCidade) {
		return tabuleiro.getCidade(nomeCidade(idCidade));
	}

	private static NomeCidade nomeCidade(String idCidade) {
		if (idCidade == null) {
			throw new IllegalArgumentException("Cidade nula");
		}
		return NomeCidade.valueOf(idCidade);
	}

	private static TipoTesouro tesouroOuNulo(String nome) {
		try {
			return TipoTesouro.valueOf(nome);
		} catch (RuntimeException e) {
			return null;
		}
	}

	// ---------------------------------------------------------------- Khan

	/** Id da província onde o Khan está, "MELHORIAS", ou null antes da 1ª vez. */
	public String getLugarDoKhan() {
		return tabuleiro.getLugarDoKhan();
	}

	/** As 4 províncias do Khan e a área de melhorias. */
	public List<String> getLugaresDoKhan() {
		return tabuleiro.getLugaresDoKhan();
	}

	// A coluna ativada tem o Khan e ele ainda não foi usado. Como usar o Khan
	// é obrigatório, o Controller não deixa passar a vez enquanto isso for true.
	public boolean khanPendente() {
		return colunaAtivadaNaVez != null && jogadorDaVez().getTabuleiro().usaKhan(colunaAtivadaNaVez)
				&& !turnoNoMapa.khanUsado();
	}

	// não pode deixar o Khan onde ele já está
	public boolean podeColocarKhan(String lugar) {
		return khanPendente() && tabuleiro.podeMoverKhanPara(lugar);
	}

	/**
	 * Coloca o Khan. Numa província do Khan, ela e as 2 das setas ganham uma
	 * peça de tributo cada. Na área de melhorias, o jogador ganha a moeda e/ou
	 * a espada dos ícones que estiverem descobertos e os espaços vazios
	 * recebem melhorias novas.
	 */
	public boolean colocarKhan(String lugar) {
		if (!podeColocarKhan(lugar)) {
			return false;
		}
		if (KHAN_NA_AREA_DE_MELHORIAS.equals(lugar)) {
			// tem que ver os ícones antes de repor as melhorias
			boolean ganhaMoeda = mercado.espacoVazio(MercadoMelhorias.ESPACO_DA_MOEDA);
			boolean ganhaEspada = mercado.espacoVazio(MercadoMelhorias.ESPACO_DA_ESPADA);
			tabuleiro.moverKhan(lugar);
			if (ganhaMoeda && reservaDe(TipoTributo.MOEDA) > 0) {
				jogadorDaVez().adicionarTributo(TipoTributo.MOEDA, 1);
			}
			if (ganhaEspada && reservaDe(TipoTributo.ESPADA) > 0) {
				jogadorDaVez().adicionarTributo(TipoTributo.ESPADA, 1);
			}
			mercado.reporVazios();
		} else {
			tabuleiro.moverKhan(lugar);
			for (Provincia p : tabuleiro.getProvinciasDoKhan(lugar)) {
				if (reservaDe(p.getTipoTributo()) > 0) { // se acabou a peça na caixa, não coloca
					p.adicionarTributo();
				}
			}
		}
		turnoNoMapa.registrarKhan();
		verificarCartasAbertas();
		mudouEstado();
		return true;
	}

	// ------------------------------------------- reserva de peças de tributo

	// a caixa do jogo vem com 10 espadas, 10 yurts e 15 moedas
	private static final Map<TipoTributo, Integer> TRIBUTOS_NA_CAIXA = new EnumMap<TipoTributo, Integer>(
			TipoTributo.class);
	static {
		TRIBUTOS_NA_CAIXA.put(TipoTributo.ESPADA, 10);
		TRIBUTOS_NA_CAIXA.put(TipoTributo.YURT, 10);
		TRIBUTOS_NA_CAIXA.put(TipoTributo.MOEDA, 15);
	}

	/** Peças desse tipo que sobraram na reserva, do lado do tabuleiro. */
	public int getTributosNaReserva(String tipo) {
		return reservaDe(TipoTributo.valueOf(tipo));
	}

	// Não guardamos a reserva num atributo: ela é o total da caixa menos o que
	// está nas províncias e na mão dos jogadores. Assim a peça gasta "volta"
	// sozinha pra reserva.
	private int reservaDe(TipoTributo tipo) {
		int foraDaReserva = 0;
		for (Provincia p : tabuleiro.getProvincias()) {
			if (p.getTipoTributo() == tipo) {
				foraDaReserva += p.getPecasDeTributo();
			}
		}
		for (Jogador j : gerenciador.getJogadoresEmOrdem()) {
			foraDaReserva += j.getQuantidadeTributo(tipo);
		}
		return Math.max(0, TRIBUTOS_NA_CAIXA.get(tipo) - foraDaReserva);
	}

	// ----------------------------------------------- habilidades (cartelas)

	// Ogedei: 1x por turno paga 1 moeda (pode ser virtual) e pega um tesouro
	// de qualquer cidade, menos o último. Não precisa estar do lado da cidade
	// e não conta como ataque.
	public boolean podePegarTesouroComMoeda(String idCidade, String tesouro) {
		Cidade c = cidade(idCidade);
		TipoTesouro t = tesouroOuNulo(tesouro);
		if (t == null) {
			return false;
		}
		return jogadorDaVez().getHerdeiro().compraTesouroComMoeda() && !turnoNoMapa.habilidadeUsada()
				&& c.getEstado() == Cidade.Estado.DISPONIVEL && c.getTesouros().size() >= 2 && c.temTesouro(t)
				&& getTributoDisponivelDaVez("MOEDA") >= 1;
	}

	public boolean pegarTesouroComMoeda(String idCidade, String tesouro) {
		if (!podePegarTesouroComMoeda(idCidade, tesouro)) {
			return false;
		}
		TipoTesouro t = TipoTesouro.valueOf(tesouro);
		gastarTributo(TipoTributo.MOEDA, 1);
		cidade(idCidade).retirarTesouro(t);
		jogadorDaVez().adicionarTesouro(t, 1);
		turnoNoMapa.registrarHabilidade();
		verificarCartasAbertas();
		mudouEstado();
		return true;
	}

	// Chagatai: 1x por turno deixa de usar 1 cavalo e ganha 1 espada
	// (a espada é de verdade, pode guardar pra depois)
	public boolean podeTrocarCavaloPorEspada() {
		return jogadorDaVez().getHerdeiro().trocaCavaloPorEspada() && !turnoNoMapa.habilidadeUsada()
				&& movimentosDaVez >= 1 && reservaDe(TipoTributo.ESPADA) > 0;
	}

	public boolean trocarCavaloPorEspada() {
		if (!podeTrocarCavaloPorEspada()) {
			return false;
		}
		movimentosDaVez--;
		jogadorDaVez().adicionarTributo(TipoTributo.ESPADA, 1);
		turnoNoMapa.registrarHabilidade();
		verificarCartasAbertas();
		mudouEstado();
		return true;
	}

	/** Usado pela View pra saber se mostra o botão do Chagatai. */
	public boolean jogadorDaVezTrocaCavaloPorEspada() {
		return jogadorDaVez().getHerdeiro().trocaCavaloPorEspada();
	}

	/** Espaço 0 a 4 da área de melhorias (da esquerda pra direita) está vazio? */
	public boolean espacoDeMelhoriaVazio(int espaco) {
		return mercado.espacoVazio(espaco);
	}

	// ----------------------------------------------------------- apoio

	private Jogador jogadorDaVez() {
		return gerenciador.getJogadorDaVez();
	}

	/** Fim do turno do jogador da vez: zera o que só vale durante o turno. */
	private void encerrarTurnoDaVez() {
		Jogador j = jogadorDaVez();
		while (melhoriasGratisPendentes > 0 && escolherMelhoriaGratis(-1)) {
			// melhoria grátis não escolhida: vai a do topo da pilha
		}
		melhoriasGratisPendentes = 0;
		if (!tesourosDoBonusParaEscolher.isEmpty()) {
			j.adicionarTesouro(tesourosDoBonusParaEscolher.get(0), 1);
			tesourosDoBonusParaEscolher.clear();
		}
		j.getTabuleiro().encerrarTurno();
		for (TipoTributo t : TipoTributo.values()) {
			virtuaisUsados.put(t, 0);
		}
		tesourosVirtuaisUsados.clear();
		paradasVisitadasNaVez.clear();
		punhosExtrasDaVez = 0;
	}

	/**
	 * Ponto único de aviso de mudança de estado. Quando as interfaces do
	 * Observer existirem, este método deve notificar os observadores.
	 */
	private void mudouEstado() {
		// será ligado ao Observer (parte do Igor)
	}

	// ------------------------------------------------------- fim e vencedor

	/**
	 * Fim de jogo disparado (pág. 14): a ficha branca, que soma os votos de
	 * todos os jogadores, chegou a 10/14/16/17 para 2/3/4/5 jogadores, ou
	 * todas as cidades foram conquistadas. Depois disso o Controller termina
	 * a rodada atual e faz mais uma rodada completa.
	 */
	public boolean fimDeJogoDisparado() {
		int total = 0;
		for (Jogador j : gerenciador.getJogadoresEmOrdem()) {
			total += j.getVotos();
		}
		int limite = VOTOS_PARA_O_FIM[gerenciador.getNumeroDeJogadores() - GerenciadorJogadores.MIN_JOGADORES];
		return total >= limite || tabuleiro.todasAsCidadesConquistadas();
	}

	/**
	 * Vencedores da partida (pág. 16): quem tem mais votos. Se houver empate,
	 * vence o empatado mais próximo de Karakorum; se ainda assim houver
	 * empate, todos eles compartilham a vitória.
	 *
	 * TODO: incluir os votos de final de jogo (influência nas regiões e
	 * conselheiro secreto) e o desempate por tesouros não entregues quando
	 * melhorias e tesouros forem implementados.
	 *
	 * @return as cores dos vencedores (mais de uma só em caso de empate)
	 */
	public List<String> getVencedores() {
		List<Jogador> empatados = new ArrayList<Jogador>();
		int maisVotos = -1;
		for (Jogador j : gerenciador.getJogadoresEmOrdem()) {
			if (j.getVotos() > maisVotos) {
				maisVotos = j.getVotos();
				empatados.clear();
			}
			if (j.getVotos() == maisVotos) {
				empatados.add(j);
			}
		}

		List<String> vencedores = new ArrayList<String>();
		int menorDistancia = Integer.MAX_VALUE;
		for (Jogador j : empatados) {
			int distancia = distanciaAteKarakorum(j);
			if (distancia < menorDistancia) {
				menorDistancia = distancia;
				vencedores.clear();
			}
			if (distancia == menorDistancia) {
				vencedores.add(j.getCor().name());
			}
		}
		return vencedores;
	}

	// ----------------------------------------------------------------- apoio

	/** Usado pelos testes. */
	Jogador getJogador(Cor cor) {
		return gerenciador.getJogador(cor);
	}

	/** Distância do peão do jogador mais perto de Karakorum (Altani tem 2). */
	private int distanciaAteKarakorum(Jogador jogador) {
		int menor = Integer.MAX_VALUE;
		for (int n = 1; n <= jogador.getHerdeiro().getNumeroDePeoes(); n++) {
			Parada parada = tabuleiro.getParadaDoPeao(new Peao(jogador.getCor(), n));
			menor = Math.min(menor, tabuleiro.distanciaAteKarakorum(parada.getId()));
		}
		return menor;
	}

	private Jogador jogador(String nomeCor) {
		Jogador j = gerenciador.getJogador(cor(nomeCor));
		if (j == null) {
			throw new IllegalArgumentException("A cor " + nomeCor + " não está na partida");
		}
		return j;
	}

	private static ColunaJogador coluna(String nome) {
		if (nome == null) {
			throw new IllegalArgumentException("Coluna nula");
		}
		return ColunaJogador.valueOf(nome); // lança IllegalArgumentException se não existir
	}

	private static Cor cor(String nome) {
		if (nome == null) {
			throw new IllegalArgumentException("Cor nula");
		}
		return Cor.valueOf(nome); // lança IllegalArgumentException se a cor não existir
	}

	private static List<Cor> converterCores(List<String> nomes) {
		if (nomes == null) {
			throw new IllegalArgumentException("Lista de cores nula");
		}
		List<Cor> cores = new ArrayList<Cor>();
		for (String nome : nomes) {
			cores.add(cor(nome));
		}
		return cores;
	}
}

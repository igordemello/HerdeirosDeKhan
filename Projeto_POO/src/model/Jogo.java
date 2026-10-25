package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
	}

	// ------------------------------------------------------------------ vez

	public String getJogadorDaVez() {
		return gerenciador.getJogadorDaVez().getCor().name();
	}

	/** Passa a vez e retorna a cor do próximo jogador. */
	public String passarVez() {
		colunaAtivadaNaVez = null;
		movimentosDaVez = 0;
		return gerenciador.passarVez().getCor().name();
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
		Cor corDaVez = gerenciador.getJogadorDaVez().getCor();
		List<String> visitadas = tabuleiro.moverPeaoAte(new Peao(corDaVez, numeroPeao), idDestino, movimentosDaVez);
		if (!visitadas.isEmpty()) {
			movimentosDaVez -= visitadas.size() - 1;
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

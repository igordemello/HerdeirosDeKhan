package model;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Um jogador da partida (um herdeiro do Khan).
 *
 * Guarda os recursos que pertencem ao jogador: peças de tributo, yurts ainda
 * não colocados, fichas de ativação, votos no Kurultai e as cartas de
 * conselheiro (secretas e abertas).
 */
class Jogador {
	static final int YURTS_INICIAIS = 12;
	static final int FICHAS_ATIVACAO_INICIAIS = TabuleiroJogador.FICHAS_ATIVACAO;
	static final int CARTAS_SECRETAS_INICIAIS = 2;

	private final Cor cor;
	private final Herdeiro herdeiro;

	private int yurtsDisponiveis = YURTS_INICIAIS;
	/** Tabuleiro do jogador: colunas, fichas de ativação e melhorias. */
	private final TabuleiroJogador tabuleiro;
	private int votos = 0;

	private final Map<TipoTributo, Integer> tributos = new EnumMap<TipoTributo, Integer>(TipoTributo.class);
	/** Tesouros que o jogador tem e ainda não entregou. */
	private final Map<TipoTesouro, Integer> tesouros = new EnumMap<TipoTesouro, Integer>(TipoTesouro.class);
	/** Peças de bônus recebidas e ainda não usadas. */
	private final List<TipoBonus> bonusDisponiveis = new ArrayList<TipoBonus>();
	/** Peças de bônus já usadas (viradas para o verso, pág. 17). */
	private final List<TipoBonus> bonusUsados = new ArrayList<TipoBonus>();

	/** Cartas secretas recebidas na preparação, ainda não escolhidas (chave = id). */
	private final Map<Integer, CartaConselheiroSecreto> cartasSecretasRecebidas =
			new HashMap<Integer, CartaConselheiroSecreto>();
	/** Carta secreta escolhida para ficar até o fim do jogo. */
	private CartaConselheiroSecreto conselheiroSecreto;
	/** Cartas de conselheiro aberto cuja demanda o jogador cumpriu (chave = id). */
	private final Map<Integer, CartaConselheiroAberto> cartasAbertasConquistadas =
			new HashMap<Integer, CartaConselheiroAberto>();

	Jogador(Cor cor) {
		if (cor == null) {
			throw new IllegalArgumentException("Cor do jogador não pode ser nula");
		}
		this.cor = cor;
		this.herdeiro = cor.getHerdeiro();
		this.tabuleiro = new TabuleiroJogador(herdeiro);
		for (TipoTributo t : TipoTributo.values()) {
			tributos.put(t, 0);
		}
		for (TipoTesouro t : TipoTesouro.values()) {
			tesouros.put(t, 0);
		}
	}

	Cor getCor() {
		return cor;
	}

	Herdeiro getHerdeiro() {
		return herdeiro;
	}

	// ---------------------------------------------------------------- tributos

	int getQuantidadeTributo(TipoTributo tipo) {
		return tributos.get(tipo);
	}

	void adicionarTributo(TipoTributo tipo, int quantidade) {
		if (tipo == null || quantidade < 0) {
			throw new IllegalArgumentException("Tributo inválido");
		}
		tributos.put(tipo, tributos.get(tipo) + quantidade);
	}

	/**
	 * Gasta peças de tributo. Se o jogador não tiver peças suficientes, nada
	 * é alterado.
	 *
	 * @return {@code true} se o gasto foi feito
	 */
	boolean gastarTributo(TipoTributo tipo, int quantidade) {
		if (tipo == null || quantidade < 0) {
			throw new IllegalArgumentException("Tributo inválido");
		}
		int atual = tributos.get(tipo);
		if (atual < quantidade) {
			return false;
		}
		tributos.put(tipo, atual - quantidade);
		return true;
	}

	// ------------------------------------------------------------------- yurts

	int getYurtsDisponiveis() {
		return yurtsDisponiveis;
	}

	/**
	 * Retira um yurt da reserva do jogador (para ser colocado no tabuleiro).
	 *
	 * @return {@code false} se as peças de yurt do jogador acabaram
	 */
	boolean retirarYurt() {
		if (yurtsDisponiveis == 0) {
			return false;
		}
		yurtsDisponiveis--;
		return true;
	}

	// ------------------------------------------------- tabuleiro do jogador

	TabuleiroJogador getTabuleiro() {
		return tabuleiro;
	}

	/** Fichas de ativação que ainda não estão em nenhuma coluna. */
	int getFichasAtivacaoDisponiveis() {
		return tabuleiro.getFichasDisponiveis();
	}

	// ------------------------------------------------------------------- votos

	int getVotos() {
		return votos;
	}

	void ganharVotos(int quantidade) {
		if (quantidade < 0) {
			throw new IllegalArgumentException("Quantidade de votos negativa");
		}
		votos += quantidade;
	}

	// --------------------------------------------------------------- tesouros

	int getQuantidadeTesouro(TipoTesouro tipo) {
		return tesouros.get(tipo);
	}

	/** Total de tesouros não entregues (critério de desempate, pág. 16). */
	int getTotalTesouros() {
		int total = 0;
		for (int n : tesouros.values()) {
			total += n;
		}
		return total;
	}

	/** Quantos tipos diferentes de tesouro o jogador tem. */
	int getTiposDeTesouro() {
		int tipos = 0;
		for (int n : tesouros.values()) {
			if (n > 0) {
				tipos++;
			}
		}
		return tipos;
	}

	void adicionarTesouro(TipoTesouro tipo, int quantidade) {
		if (tipo == null || quantidade < 0) {
			throw new IllegalArgumentException("Tesouro inválido");
		}
		tesouros.put(tipo, tesouros.get(tipo) + quantidade);
	}

	/**
	 * Tira tesouros do jogador. Se ele não tiver o suficiente, nada muda.
	 *
	 * @return {@code true} se os tesouros foram retirados
	 */
	boolean removerTesouro(TipoTesouro tipo, int quantidade) {
		if (tipo == null || quantidade < 0) {
			throw new IllegalArgumentException("Tesouro inválido");
		}
		int atual = tesouros.get(tipo);
		if (atual < quantidade) {
			return false;
		}
		tesouros.put(tipo, atual - quantidade);
		return true;
	}

	// ---------------------------------------------------------- peças de bônus

	void receberBonus(TipoBonus bonus) {
		if (bonus == null) {
			throw new IllegalArgumentException("Bônus nulo");
		}
		bonusDisponiveis.add(bonus);
	}

	List<TipoBonus> getBonusDisponiveis() {
		return Collections.unmodifiableList(bonusDisponiveis);
	}

	/** Todas as peças de bônus que o jogador já recebeu, usadas ou não. */
	int getTotalBonusRecebidos() {
		return bonusDisponiveis.size() + bonusUsados.size();
	}

	/**
	 * Marca uma peça de bônus como usada.
	 *
	 * @return {@code false} se o jogador não tem essa peça para usar
	 */
	boolean usarBonus(TipoBonus bonus) {
		if (!bonusDisponiveis.remove(bonus)) {
			return false;
		}
		bonusUsados.add(bonus);
		return true;
	}

	// ------------------------------------------------- cartas de conselheiro

	/** Recebe uma carta de conselheiro secreto na preparação do jogo. */
	void receberCartaSecreta(CartaConselheiroSecreto carta) {
		if (carta == null) {
			throw new IllegalArgumentException("Carta nula");
		}
		if (conselheiroSecreto != null) {
			throw new IllegalStateException("O jogador já escolheu seu conselheiro secreto");
		}
		cartasSecretasRecebidas.put(carta.getId(), carta);
	}

	Collection<CartaConselheiroSecreto> getCartasSecretasRecebidas() {
		return Collections.unmodifiableCollection(cartasSecretasRecebidas.values());
	}

	/**
	 * Escolhe a carta secreta a manter. As demais cartas recebidas saem da
	 * mão do jogador e são devolvidas para que sejam descartadas.
	 *
	 * @return as cartas não escolhidas
	 */
	Collection<CartaConselheiroSecreto> escolherConselheiroSecreto(int idCarta) {
		if (conselheiroSecreto != null) {
			throw new IllegalStateException("O jogador já escolheu seu conselheiro secreto");
		}
		CartaConselheiroSecreto escolhida = cartasSecretasRecebidas.remove(idCarta);
		if (escolhida == null) {
			throw new IllegalArgumentException("O jogador não possui a carta secreta #" + idCarta);
		}
		conselheiroSecreto = escolhida;
		Collection<CartaConselheiroSecreto> descartadas =
				new ArrayList<CartaConselheiroSecreto>(cartasSecretasRecebidas.values());
		cartasSecretasRecebidas.clear();
		return descartadas;
	}

	CartaConselheiroSecreto getConselheiroSecreto() {
		return conselheiroSecreto;
	}

	boolean jaEscolheuConselheiroSecreto() {
		return conselheiroSecreto != null;
	}

	/** Recebe uma carta de conselheiro aberto e o voto correspondente. */
	void conquistarCartaAberta(CartaConselheiroAberto carta) {
		if (carta == null) {
			throw new IllegalArgumentException("Carta nula");
		}
		cartasAbertasConquistadas.put(carta.getId(), carta);
		ganharVotos(CartaConselheiroAberto.VOTOS_CONCEDIDOS);
	}

	Collection<CartaConselheiroAberto> getCartasAbertasConquistadas() {
		return Collections.unmodifiableCollection(cartasAbertasConquistadas.values());
	}

	// --------------------------------------------- salvar e carregar (4ª it.)

	/**
	 * Escreve os dados do jogador (recursos, peças de bônus, cartas e o
	 * tabuleiro dele) na seção {@code [JOGADOR <COR>]}, que quem chama já
	 * abriu. Só nomes de enum e números, para o arquivo ficar em ASCII.
	 */
	void salvar(PrintWriter saida) {
		ArquivoPartida.linha(saida, "herdeiro", herdeiro.name());
		List<String> lista = new ArrayList<String>();
		for (TipoTributo t : TipoTributo.values()) {
			lista.add(t.name() + ":" + tributos.get(t));
		}
		ArquivoPartida.linha(saida, "tributos", ArquivoPartida.juntar(lista));
		lista.clear();
		for (TipoTesouro t : TipoTesouro.values()) {
			lista.add(t.name() + ":" + tesouros.get(t));
		}
		ArquivoPartida.linha(saida, "tesouros", ArquivoPartida.juntar(lista));
		ArquivoPartida.linha(saida, "votos", votos);
		ArquivoPartida.linha(saida, "yurts", yurtsDisponiveis);
		ArquivoPartida.linha(saida, "bonus", ArquivoPartida.juntar(bonusDisponiveis));
		ArquivoPartida.linha(saida, "bonusUsados", ArquivoPartida.juntar(bonusUsados));

		lista.clear();
		for (CartaConselheiroSecreto c : cartasSecretasRecebidas.values()) {
			lista.add(c.getTipo().name());
		}
		java.util.Collections.sort(lista);
		ArquivoPartida.linha(saida, "secretasParaEscolher", ArquivoPartida.juntar(lista));
		ArquivoPartida.linha(saida, "secreto", conselheiroSecreto == null ? "" : conselheiroSecreto.getTipo().name());
		lista.clear();
		for (CartaConselheiroAberto c : cartasAbertasConquistadas.values()) {
			lista.add(c.getTipo().name());
		}
		java.util.Collections.sort(lista);
		ArquivoPartida.linha(saida, "abertas", ArquivoPartida.juntar(lista));
		tabuleiro.salvar(saida);
	}

	/**
	 * Restaura o jogador a partir da sua seção. As cartas são pegas do
	 * distribuidor da partida, pelo tipo. Os votos vêm direto do arquivo
	 * (as cartas abertas não dão o voto de novo).
	 */
	void carregar(Map<String, String> secao, DistribuidorCartas cartas) {
		if (!ArquivoPartida.valor(secao, "herdeiro").equals(herdeiro.name())) {
			throw new IllegalArgumentException("O herdeiro salvo não é o da cor " + cor);
		}
		Map<String, Integer> pares = ArquivoPartida.pares(secao, "tributos");
		for (TipoTributo t : TipoTributo.values()) {
			tributos.put(t, naoNegativo(pares, t.name()));
		}
		pares = ArquivoPartida.pares(secao, "tesouros");
		for (TipoTesouro t : TipoTesouro.values()) {
			tesouros.put(t, naoNegativo(pares, t.name()));
		}
		votos = ArquivoPartida.inteiro(secao, "votos");
		yurtsDisponiveis = ArquivoPartida.inteiro(secao, "yurts");
		if (votos < 0 || yurtsDisponiveis < 0 || yurtsDisponiveis > YURTS_INICIAIS) {
			throw new IllegalArgumentException("Votos ou yurts inválidos para " + cor);
		}
		bonusDisponiveis.clear();
		for (String b : ArquivoPartida.lista(secao, "bonus")) {
			bonusDisponiveis.add(TipoBonus.valueOf(b));
		}
		bonusUsados.clear();
		for (String b : ArquivoPartida.lista(secao, "bonusUsados")) {
			bonusUsados.add(TipoBonus.valueOf(b));
		}

		cartasSecretasRecebidas.clear();
		for (String t : ArquivoPartida.lista(secao, "secretasParaEscolher")) {
			CartaConselheiroSecreto c = cartas.getCartaSecreta(TipoConselheiroSecreto.valueOf(t));
			cartasSecretasRecebidas.put(c.getId(), c);
		}
		String secreto = ArquivoPartida.valor(secao, "secreto");
		conselheiroSecreto = secreto.isEmpty() ? null
				: cartas.getCartaSecreta(TipoConselheiroSecreto.valueOf(secreto));
		if ((conselheiroSecreto == null) == cartasSecretasRecebidas.isEmpty()) {
			throw new IllegalArgumentException(
					"O jogador " + cor + " precisa ter um conselheiro secreto ou cartas para escolher");
		}
		cartasAbertasConquistadas.clear();
		for (String t : ArquivoPartida.lista(secao, "abertas")) {
			CartaConselheiroAberto c = cartas.getCartaAberta(TipoConselheiroAberto.valueOf(t));
			cartasAbertasConquistadas.put(c.getId(), c);
		}
		tabuleiro.carregar(secao);
	}

	private static int naoNegativo(Map<String, Integer> pares, String nome) {
		Integer n = pares.get(nome);
		if (n == null || n < 0) {
			throw new IllegalArgumentException("Quantidade inválida de " + nome);
		}
		return n;
	}

	@Override
	public String toString() {
		return "Jogador " + cor + " (" + herdeiro.getNome() + ")";
	}
}

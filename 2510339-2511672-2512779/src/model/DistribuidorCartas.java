package model;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/*
 * Baralhos de conselheiro. Na preparação (págs. 3-4) embaralha as 10
 * secretas e dá 2 pra cada um, e embaralha as 14 abertas e revela 4 (as
 * outras ficam fora da partida). Cada jogador fica com 1 secreta e descarta
 * a outra (pág. 6).
 *
 * Durante o jogo o primeiro a cumprir uma carta aberta fica com ela e ganha
 * 1 voto, e não aparece outra no lugar (págs. 13 e 18).
 */
class DistribuidorCartas {
	static final int CARTAS_ABERTAS_REVELADAS = 4;

	private final Baralho<CartaConselheiroSecreto> baralhoSecreto;
	private final Baralho<CartaConselheiroAberto> baralhoAberto;

	// Cartas de conselheiro aberto reveladas e ainda disponíveis (chave = id).
	private final Map<Integer, CartaConselheiroAberto> cartasAbertasReveladas =
			new HashMap<Integer, CartaConselheiroAberto>();

	private boolean preparado = false;

	// todas as cartas pelo tipo, pra achar a carta certa ao carregar
	private final Map<TipoConselheiroSecreto, CartaConselheiroSecreto> secretasPorTipo =
			new HashMap<TipoConselheiroSecreto, CartaConselheiroSecreto>();
	private final Map<TipoConselheiroAberto, CartaConselheiroAberto> abertasPorTipo =
			new HashMap<TipoConselheiroAberto, CartaConselheiroAberto>();

	DistribuidorCartas(Random aleatorio) {
		if (aleatorio == null) {
			throw new IllegalArgumentException("Gerador aleatório nulo");
		}
		List<CartaConselheiroSecreto> secretas = criarCartasSecretas();
		List<CartaConselheiroAberto> abertas = criarCartasAbertas();
		for (CartaConselheiroSecreto c : secretas) {
			secretasPorTipo.put(c.getTipo(), c);
		}
		for (CartaConselheiroAberto c : abertas) {
			abertasPorTipo.put(c.getTipo(), c);
		}
		this.baralhoSecreto = new Baralho<CartaConselheiroSecreto>(secretas, aleatorio);
		this.baralhoAberto = new Baralho<CartaConselheiroAberto>(abertas, aleatorio);
	}

	DistribuidorCartas() {
		this(new Random());
	}

	private static List<CartaConselheiroSecreto> criarCartasSecretas() {
		List<CartaConselheiroSecreto> cartas = new ArrayList<CartaConselheiroSecreto>();
		int id = 1;
		for (TipoConselheiroSecreto tipo : TipoConselheiroSecreto.values()) {
			cartas.add(new CartaConselheiroSecreto(id++, tipo));
		}
		return cartas;
	}

	private static List<CartaConselheiroAberto> criarCartasAbertas() {
		List<CartaConselheiroAberto> cartas = new ArrayList<CartaConselheiroAberto>();
		int id = 101; // ids distintos dos das cartas secretas
		for (TipoConselheiroAberto tipo : TipoConselheiroAberto.values()) {
			cartas.add(new CartaConselheiroAberto(id++, tipo));
		}
		return cartas;
	}

	// preparação

	void prepararCartas(Collection<Jogador> jogadores) {
		if (preparado) {
			throw new IllegalStateException("As cartas já foram distribuídas");
		}
		if (jogadores == null || jogadores.size() < GerenciadorJogadores.MIN_JOGADORES
				|| jogadores.size() > GerenciadorJogadores.MAX_JOGADORES) {
			throw new IllegalArgumentException("Número de jogadores inválido");
		}
		baralhoSecreto.embaralhar();
		baralhoAberto.embaralhar();

		for (Jogador jogador : jogadores) {
			for (CartaConselheiroSecreto carta : baralhoSecreto.comprar(Jogador.CARTAS_SECRETAS_INICIAIS)) {
				jogador.receberCartaSecreta(carta);
			}
		}
		for (CartaConselheiroAberto carta : baralhoAberto.comprar(CARTAS_ABERTAS_REVELADAS)) {
			cartasAbertasReveladas.put(carta.getId(), carta);
		}
		preparado = true;
	}

	// a carta que ele não quis vai pro descarte
	void escolherConselheiroSecreto(Jogador jogador, int idCarta) {
		if (jogador == null) {
			throw new IllegalArgumentException("Jogador nulo");
		}
		for (CartaConselheiroSecreto descartada : jogador.escolherConselheiroSecreto(idCarta)) {
			baralhoSecreto.descartar(descartada);
		}
	}

	// distribuição durante o jogo

	// Dá a carta aberta pra quem cumpriu (quem confere a demanda é a Apuracao,
	// chamada pelo Jogo). Só o primeiro leva: false se ela já saiu.
	boolean entregarCartaAberta(Jogador jogador, int idCarta) {
		if (jogador == null) {
			throw new IllegalArgumentException("Jogador nulo");
		}
		CartaConselheiroAberto carta = cartasAbertasReveladas.remove(idCarta);
		if (carta == null) {
			return false;
		}
		jogador.conquistarCartaAberta(carta); // ganha 1 voto; nenhuma carta nova é revelada
		return true;
	}

	CartaConselheiroAberto getCartaAbertaDisponivel(TipoConselheiroAberto tipo) {
		for (CartaConselheiroAberto carta : cartasAbertasReveladas.values()) {
			if (carta.getTipo() == tipo) {
				return carta;
			}
		}
		return null;
	}

	Collection<CartaConselheiroAberto> getCartasAbertasDisponiveis() {
		return Collections.unmodifiableCollection(cartasAbertasReveladas.values());
	}

	boolean todosEscolheramConselheiroSecreto(Collection<Jogador> jogadores) {
		for (Jogador j : jogadores) {
			if (!j.jaEscolheuConselheiroSecreto()) {
				return false;
			}
		}
		return true;
	}

	boolean isPreparado() {
		return preparado;
	}

	Baralho<CartaConselheiroSecreto> getBaralhoSecreto() {
		return baralhoSecreto;
	}

	Baralho<CartaConselheiroAberto> getBaralhoAberto() {
		return baralhoAberto;
	}

	// salvar e carregar (4ª it.)

	CartaConselheiroSecreto getCartaSecreta(TipoConselheiroSecreto tipo) {
		return secretasPorTipo.get(tipo);
	}

	CartaConselheiroAberto getCartaAberta(TipoConselheiroAberto tipo) {
		return abertasPorTipo.get(tipo);
	}

	// Seção [CARTAS]: abertas ainda na mesa e o descarte das secretas. As cartas
	// de cada jogador vão na seção dele. Os montes não precisam ir: depois da
	// preparação ninguém compra mais deles, e ao carregar a gente recalcula.
	void salvar(PrintWriter saida) {
		List<String> nomes = new ArrayList<String>();
		for (CartaConselheiroAberto c : cartasAbertasReveladas.values()) {
			nomes.add(c.getTipo().name());
		}
		Collections.sort(nomes);
		ArquivoPartida.linha(saida, "abertasDisponiveis", ArquivoPartida.juntar(nomes));
		nomes = new ArrayList<String>();
		for (CartaConselheiroSecreto c : baralhoSecreto.getDescarte()) {
			nomes.add(c.getTipo().name());
		}
		ArquivoPartida.linha(saida, "descarteSecretas", ArquivoPartida.juntar(nomes));
	}

	// Tem que vir depois de carregar os jogadores, porque os montes são o que
	// sobra das cartas que não estão com ninguém. Carta repetida em dois
	// lugares dá IllegalArgumentException.
	void carregar(Map<String, String> secao, Collection<Jogador> jogadores) {
		List<CartaConselheiroSecreto> descarte = new ArrayList<CartaConselheiroSecreto>();
		for (String t : ArquivoPartida.lista(secao, "descarteSecretas")) {
			descarte.add(getCartaSecreta(TipoConselheiroSecreto.valueOf(t)));
		}
		cartasAbertasReveladas.clear();
		for (String t : ArquivoPartida.lista(secao, "abertasDisponiveis")) {
			CartaConselheiroAberto c = getCartaAberta(TipoConselheiroAberto.valueOf(t));
			cartasAbertasReveladas.put(c.getId(), c);
		}

		List<Carta> usadas = new ArrayList<Carta>(descarte);
		usadas.addAll(cartasAbertasReveladas.values());
		for (Jogador j : jogadores) {
			usadas.addAll(j.getCartasSecretasRecebidas());
			if (j.getConselheiroSecreto() != null) {
				usadas.add(j.getConselheiroSecreto());
			}
			usadas.addAll(j.getCartasAbertasConquistadas());
		}
		if (new java.util.HashSet<Carta>(usadas).size() != usadas.size()) {
			throw new IllegalArgumentException("Uma carta de conselheiro aparece em dois lugares no arquivo");
		}

		List<CartaConselheiroSecreto> monteSecreto = new ArrayList<CartaConselheiroSecreto>();
		for (TipoConselheiroSecreto t : TipoConselheiroSecreto.values()) {
			if (!usadas.contains(secretasPorTipo.get(t))) {
				monteSecreto.add(secretasPorTipo.get(t));
			}
		}
		List<CartaConselheiroAberto> monteAberto = new ArrayList<CartaConselheiroAberto>();
		for (TipoConselheiroAberto t : TipoConselheiroAberto.values()) {
			if (!usadas.contains(abertasPorTipo.get(t))) {
				monteAberto.add(abertasPorTipo.get(t));
			}
		}
		baralhoSecreto.restaurar(monteSecreto, descarte);
		baralhoAberto.restaurar(monteAberto, new ArrayList<CartaConselheiroAberto>());
		preparado = true;
	}
}

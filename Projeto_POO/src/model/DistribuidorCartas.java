package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Controla os baralhos de conselheiros e a distribuição das cartas.
 *
 * Preparação do jogo (págs. 3-4 e 6):
 * <ul>
 * <li>embaralha as 10 cartas de conselheiro secreto e dá 2 a cada jogador;</li>
 * <li>embaralha as 14 cartas de conselheiro aberto e revela 4; as demais não
 * são usadas na partida.</li>
 * </ul>
 * Antes da primeira rodada cada jogador escolhe 1 carta secreta e descarta a
 * outra.
 *
 * Distribuição ao longo do jogo (págs. 13 e 18): o primeiro jogador a
 * cumprir a demanda de uma carta de conselheiro aberto revelada fica com ela
 * e ganha 1 voto. Nenhuma carta nova é revelada no lugar.
 */
class DistribuidorCartas {
	static final int CARTAS_ABERTAS_REVELADAS = 4;

	private final Baralho<CartaConselheiroSecreto> baralhoSecreto;
	private final Baralho<CartaConselheiroAberto> baralhoAberto;

	/** Cartas de conselheiro aberto reveladas e ainda disponíveis (chave = id). */
	private final Map<Integer, CartaConselheiroAberto> cartasAbertasReveladas =
			new HashMap<Integer, CartaConselheiroAberto>();

	private boolean preparado = false;

	DistribuidorCartas(Random aleatorio) {
		if (aleatorio == null) {
			throw new IllegalArgumentException("Gerador aleatório nulo");
		}
		this.baralhoSecreto = new Baralho<CartaConselheiroSecreto>(criarCartasSecretas(), aleatorio);
		this.baralhoAberto = new Baralho<CartaConselheiroAberto>(criarCartasAbertas(), aleatorio);
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

	// ------------------------------------------------------------ preparação

	/**
	 * Faz a preparação inicial das cartas: embaralha os dois baralhos, dá 2
	 * cartas secretas a cada jogador e revela 4 cartas de conselheiro aberto.
	 */
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

	/**
	 * O jogador escolhe a carta secreta que vai manter; a outra vai para o
	 * descarte do baralho secreto.
	 */
	void escolherConselheiroSecreto(Jogador jogador, int idCarta) {
		if (jogador == null) {
			throw new IllegalArgumentException("Jogador nulo");
		}
		for (CartaConselheiroSecreto descartada : jogador.escolherConselheiroSecreto(idCarta)) {
			baralhoSecreto.descartar(descartada);
		}
	}

	// ------------------------------------------- distribuição durante o jogo

	/**
	 * Entrega uma carta de conselheiro aberto ao jogador que cumpriu sua
	 * demanda (a verificação da demanda depende do estado do tabuleiro e é
	 * feita por quem chama). Apenas o primeiro a cumprir recebe a carta.
	 *
	 * @return {@code true} se o jogador recebeu a carta e o voto;
	 *         {@code false} se a carta não está mais disponível
	 */
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

	/** Busca, entre as cartas abertas disponíveis, a de um determinado tipo. */
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
}

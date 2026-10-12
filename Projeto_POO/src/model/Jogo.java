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
		return gerenciador.passarVez().getCor().name();
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

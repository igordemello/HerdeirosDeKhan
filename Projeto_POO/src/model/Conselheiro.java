package model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Um dos 7 conselheiros do mapa (págs. 12-13): 6 nas paradas duplas e 1 em
 * Karakorum. Quem faz uma parada no conselheiro pode entregar a ele o
 * conjunto de tesouros que ele pede e ganha votos por entrega. A primeira
 * entrega também dá a peça de bônus que está sobre o conselheiro.
 *
 * Há dois tipos de pedido: tesouros específicos (ex.: 2 sedas; em
 * Karakorum, 1 ferro, 1 pele e 1 boi) ou uma quantidade de tesouros de
 * qualquer tipo (3, no conselheiro de baixo).
 */
class Conselheiro {
	private final String idParada;
	/** Tesouros específicos pedidos; vazio quando o pedido é de qualquer tipo. */
	private final Map<TipoTesouro, Integer> pedido;
	/** Quantos tesouros de qualquer tipo são pedidos (0 se o pedido é específico). */
	private final int quantidadeQualquer;
	private final int votosPorEntrega;
	private TipoBonus bonus;
	private int entregas = 0;

	private Conselheiro(String idParada, Map<TipoTesouro, Integer> pedido, int quantidadeQualquer,
			int votosPorEntrega) {
		this.idParada = idParada;
		this.pedido = pedido;
		this.quantidadeQualquer = quantidadeQualquer;
		this.votosPorEntrega = votosPorEntrega;
	}

	/** Conselheiro que pede tesouros específicos (podem se repetir na lista). */
	static Conselheiro comPedido(String idParada, int votosPorEntrega, List<TipoTesouro> tesouros) {
		if (tesouros == null || tesouros.isEmpty()) {
			throw new IllegalArgumentException("Pedido vazio");
		}
		Map<TipoTesouro, Integer> pedido = new EnumMap<TipoTesouro, Integer>(TipoTesouro.class);
		for (TipoTesouro t : tesouros) {
			Integer atual = pedido.get(t);
			pedido.put(t, atual == null ? 1 : atual + 1);
		}
		return new Conselheiro(idParada, pedido, 0, votosPorEntrega);
	}

	/** Conselheiro que aceita uma quantidade de tesouros de qualquer tipo. */
	static Conselheiro deQualquerTipo(String idParada, int votosPorEntrega, int quantidade) {
		if (quantidade <= 0) {
			throw new IllegalArgumentException("Quantidade inválida");
		}
		return new Conselheiro(idParada, new EnumMap<TipoTesouro, Integer>(TipoTesouro.class), quantidade,
				votosPorEntrega);
	}

	String getIdParada() {
		return idParada;
	}

	int getVotosPorEntrega() {
		return votosPorEntrega;
	}

	boolean aceitaQualquerTipo() {
		return quantidadeQualquer > 0;
	}

	/** Pedido específico (vazio se aceita qualquer tipo). */
	Map<TipoTesouro, Integer> getPedido() {
		return Collections.unmodifiableMap(pedido);
	}

	/** Quantidade total de tesouros de uma entrega. */
	int getTamanhoDaEntrega() {
		if (aceitaQualquerTipo()) {
			return quantidadeQualquer;
		}
		int total = 0;
		for (int n : pedido.values()) {
			total += n;
		}
		return total;
	}

	/**
	 * Se a lista de tesouros é exatamente uma entrega válida para este
	 * conselheiro.
	 */
	boolean aceita(List<TipoTesouro> tesouros) {
		if (tesouros == null || tesouros.size() != getTamanhoDaEntrega()) {
			return false;
		}
		if (aceitaQualquerTipo()) {
			return true;
		}
		Map<TipoTesouro, Integer> contagem = new EnumMap<TipoTesouro, Integer>(TipoTesouro.class);
		for (TipoTesouro t : tesouros) {
			Integer atual = contagem.get(t);
			contagem.put(t, atual == null ? 1 : atual + 1);
		}
		return contagem.equals(pedido);
	}

	TipoBonus getBonus() {
		return bonus;
	}

	void colocarBonus(TipoBonus bonus) {
		this.bonus = bonus;
	}

	/**
	 * Registra uma entrega.
	 *
	 * @return a peça de bônus, se esta foi a primeira entrega e ainda havia
	 *         peça; senão {@code null}
	 */
	TipoBonus registrarEntrega() {
		entregas++;
		TipoBonus ganho = bonus;
		bonus = null;
		return ganho;
	}

	int getNumeroDeEntregas() {
		return entregas;
	}
}

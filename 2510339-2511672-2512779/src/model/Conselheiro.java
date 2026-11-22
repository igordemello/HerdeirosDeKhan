package model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/*
 * Conselheiro do mapa (págs. 12-13): 6 nas paradas duplas e 1 em Karakorum.
 * Quem para nele pode entregar os tesouros que ele pede e ganha votos; a
 * primeira entrega leva também a peça de bônus. O pedido ou é de tesouros
 * certos (2 sedas, ou 1 ferro + 1 pele + 1 boi em Karakorum) ou é de N
 * tesouros quaisquer (3, no conselheiro de baixo).
 */
class Conselheiro {
	private final String idParada;
	// vazio quando aceita qualquer tipo
	private final Map<TipoTesouro, Integer> pedido;
	// 0 quando o pedido é de tesouros certos
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

	Map<TipoTesouro, Integer> getPedido() {
		return Collections.unmodifiableMap(pedido);
	}

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

	// a lista tem que ser exatamente o pedido
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

	// devolve a peça de bônus na primeira entrega, e null nas outras
	TipoBonus registrarEntrega() {
		entregas++;
		TipoBonus ganho = bonus;
		bonus = null;
		return ganho;
	}

	int getNumeroDeEntregas() {
		return entregas;
	}

	// usado ao carregar uma partida
	void restaurar(TipoBonus bonus, int entregas) {
		if (entregas < 0) {
			throw new IllegalArgumentException("Número de entregas negativo");
		}
		this.bonus = bonus;
		this.entregas = entregas;
	}
}

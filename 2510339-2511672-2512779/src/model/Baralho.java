package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/*
 * Baralho genérico: o monte (virado pra baixo, o topo é o fim da lista) e o
 * descarte. Aqui é lista e não HashMap porque a ordem importa (embaralhar e
 * comprar do topo).
 */
class Baralho<T extends Carta> {
	private final List<T> monte = new ArrayList<T>();
	private final List<T> descarte = new ArrayList<T>();
	private final Random aleatorio;

	Baralho(Collection<T> cartas, Random aleatorio) {
		if (cartas == null || aleatorio == null) {
			throw new IllegalArgumentException("Cartas e gerador aleatório não podem ser nulos");
		}
		monte.addAll(cartas);
		this.aleatorio = aleatorio;
	}

	void embaralhar() {
		Collections.shuffle(monte, aleatorio);
	}

	// null se o monte acabou
	T comprar() {
		if (monte.isEmpty()) {
			return null;
		}
		return monte.remove(monte.size() - 1);
	}

	// se não tiver cartas suficientes lança IllegalStateException
	List<T> comprar(int quantidade) {
		if (quantidade < 0) {
			throw new IllegalArgumentException("Quantidade negativa: " + quantidade);
		}
		if (quantidade > monte.size()) {
			throw new IllegalStateException("Cartas insuficientes no baralho: pedidas " + quantidade
					+ ", disponíveis " + monte.size());
		}
		List<T> compradas = new ArrayList<T>();
		for (int i = 0; i < quantidade; i++) {
			compradas.add(comprar());
		}
		return compradas;
	}

	void descartar(T carta) {
		if (carta == null) {
			throw new IllegalArgumentException("Carta nula");
		}
		descarte.add(carta);
	}

	int tamanho() {
		return monte.size();
	}

	boolean estaVazio() {
		return monte.isEmpty();
	}

	List<T> getDescarte() {
		return Collections.unmodifiableList(descarte);
	}

	// usado ao carregar; o monte vem do fundo pro topo
	void restaurar(List<T> novoMonte, List<T> novoDescarte) {
		monte.clear();
		monte.addAll(novoMonte);
		descarte.clear();
		descarte.addAll(novoDescarte);
	}

	// o último é o topo
	List<T> getMonte() {
		return Collections.unmodifiableList(monte);
	}
}

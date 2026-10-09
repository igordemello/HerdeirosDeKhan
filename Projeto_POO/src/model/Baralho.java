package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Baralho genérico de cartas: um monte (cartas viradas para baixo, o topo é o
 * fim da lista) e uma pilha de descarte.
 *
 * Aqui é usada uma lista em vez de HashMap porque a ordem das cartas importa
 * (embaralhar e comprar do topo).
 *
 * @param <T> tipo de carta guardado no baralho
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

	/** Embaralha as cartas do monte. */
	void embaralhar() {
		Collections.shuffle(monte, aleatorio);
	}

	/**
	 * Compra a carta do topo do monte.
	 *
	 * @return a carta comprada, ou {@code null} se o monte estiver vazio
	 */
	T comprar() {
		if (monte.isEmpty()) {
			return null;
		}
		return monte.remove(monte.size() - 1);
	}

	/**
	 * Compra {@code quantidade} cartas do topo do monte.
	 *
	 * @throws IllegalStateException se não houver cartas suficientes
	 */
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

	/** Coloca uma carta na pilha de descarte. */
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

	/** Cópia somente-leitura da pilha de descarte. */
	List<T> getDescarte() {
		return Collections.unmodifiableList(descarte);
	}

	/** Cópia somente-leitura do monte (o último elemento é o topo). */
	List<T> getMonte() {
		return Collections.unmodifiableList(monte);
	}
}

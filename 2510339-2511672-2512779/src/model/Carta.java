package model;

/*
 * Carta genérica. O id é a chave nos HashMaps do jogador e do distribuidor.
 * É abstrata pra dar pra criar outros tipos de carta depois (aberto/fechado).
 */
abstract class Carta {
	private final int id;

	protected Carta(int id) {
		this.id = id;
	}

	int getId() {
		return id;
	}

	// Nome legível da carta (ex.: "Viajante").
	abstract String getNome();

	// Texto que descreve a condição da carta.
	abstract String getDescricao();

	@Override
	public String toString() {
		return getNome() + " (#" + id + ")";
	}
}

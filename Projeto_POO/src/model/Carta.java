package model;

/**
 * Carta genérica do jogo. Cada carta tem um identificador único,
 * usado como chave nas coleções (HashMap) de jogadores e do distribuidor.
 *
 * É abstrata para permitir a extensão por novos tipos de carta
 * (princípio aberto/fechado).
 */
abstract class Carta {
	private final int id;

	protected Carta(int id) {
		this.id = id;
	}

	int getId() {
		return id;
	}

	/** Nome legível da carta (ex.: "Viajante"). */
	abstract String getNome();

	/** Texto que descreve a condição da carta. */
	abstract String getDescricao();

	@Override
	public String toString() {
		return getNome() + " (#" + id + ")";
	}
}

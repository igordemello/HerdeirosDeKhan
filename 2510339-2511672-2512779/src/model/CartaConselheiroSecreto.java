package model;

/**
 * Carta de conselheiro secreto. Cada jogador recebe 2 no início da partida
 * e escolhe 1 para manter; no final ela dá de 0 a 3 votos.
 */
class CartaConselheiroSecreto extends Carta {
	private final TipoConselheiroSecreto tipo;

	CartaConselheiroSecreto(int id, TipoConselheiroSecreto tipo) {
		super(id);
		if (tipo == null) {
			throw new IllegalArgumentException("Tipo da carta não pode ser nulo");
		}
		this.tipo = tipo;
	}

	TipoConselheiroSecreto getTipo() {
		return tipo;
	}

	@Override
	String getNome() {
		return tipo.getNome();
	}

	@Override
	String getDescricao() {
		return tipo.getDescricao() + " (" + tipo.getLimiar(1) + "/" + tipo.getLimiar(2) + "/"
				+ tipo.getLimiar(3) + " -> 1/2/3 votos)";
	}

	/** Votos de final de jogo concedidos para a quantidade atingida. */
	int calcularVotos(int quantidadeAtingida) {
		return tipo.calcularVotos(quantidadeAtingida);
	}
}

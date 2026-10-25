package model;

/**
 * Uma peça de melhoria: o tipo e a cor de fundo da peça.
 *
 * O id (ex.: "CAV-V") identifica a imagem da peça, para que a View a
 * recupere em tempo constante, como pede o enunciado.
 */
final class PecaMelhoria {
	private final TipoMelhoria tipo;
	private final ColunaJogador cor;

	PecaMelhoria(TipoMelhoria tipo, ColunaJogador cor) {
		if (tipo == null || cor == null) {
			throw new IllegalArgumentException("Tipo e cor da melhoria não podem ser nulos");
		}
		if (!cor.ehColorida()) {
			throw new IllegalArgumentException("As peças de melhoria são vermelhas, verdes ou amarelas");
		}
		if (tipo.getCorFixa() != null && tipo.getCorFixa() != cor) {
			throw new IllegalArgumentException(tipo + " só existe na cor " + tipo.getCorFixa());
		}
		this.tipo = tipo;
		this.cor = cor;
	}

	TipoMelhoria getTipo() {
		return tipo;
	}

	ColunaJogador getCor() {
		return cor;
	}

	/** Identificador da peça, igual ao nome da imagem (ex.: "CAV-V"). */
	String getId() {
		return tipo.getSigla() + "-" + cor.getSigla();
	}

	@Override
	public String toString() {
		return getId();
	}
}

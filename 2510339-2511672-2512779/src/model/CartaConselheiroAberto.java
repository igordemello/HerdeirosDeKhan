package model;

/**
 * Carta de conselheiro aberto. Quatro delas são reveladas na preparação; quem
 * cumprir primeiro a demanda de uma delas ganha 1 voto e fica com a carta.
 */
class CartaConselheiroAberto extends Carta {
	/** Votos concedidos a quem cumpre a demanda da carta. */
	static final int VOTOS_CONCEDIDOS = 1;

	private final TipoConselheiroAberto tipo;

	CartaConselheiroAberto(int id, TipoConselheiroAberto tipo) {
		super(id);
		if (tipo == null) {
			throw new IllegalArgumentException("Tipo da carta não pode ser nulo");
		}
		this.tipo = tipo;
	}

	TipoConselheiroAberto getTipo() {
		return tipo;
	}

	@Override
	String getNome() {
		return tipo.name();
	}

	@Override
	String getDescricao() {
		return tipo.getDescricao();
	}
}

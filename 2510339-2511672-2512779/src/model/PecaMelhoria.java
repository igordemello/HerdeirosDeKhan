package model;

import java.util.ArrayList;
import java.util.List;

/*
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

	// Identificador da peça, igual ao nome da imagem (ex.: "CAV-V").
	String getId() {
		return tipo.getSigla() + "-" + cor.getSigla();
	}

	// usado ao carregar; id que não existe dá IllegalArgumentException
	static PecaMelhoria deId(String id) {
		int traco = id == null ? -1 : id.indexOf('-');
		if (traco < 0) {
			throw new IllegalArgumentException("Id de melhoria inválido: " + id);
		}
		String sigla = id.substring(0, traco);
		String letra = id.substring(traco + 1);
		TipoMelhoria tipo = null;
		for (TipoMelhoria t : TipoMelhoria.values()) {
			if (t.getSigla().equals(sigla)) {
				tipo = t;
			}
		}
		ColunaJogador cor = null;
		for (ColunaJogador c : ColunaJogador.values()) {
			if (letra.equals(c.getSigla())) {
				cor = c;
			}
		}
		if (tipo == null || cor == null) {
			throw new IllegalArgumentException("Id de melhoria inválido: " + id);
		}
		return new PecaMelhoria(tipo, cor);
	}

	static List<PecaMelhoria> deIds(List<String> ids) {
		List<PecaMelhoria> pecas = new ArrayList<PecaMelhoria>();
		for (String id : ids) {
			pecas.add(deId(id));
		}
		return pecas;
	}

	static List<String> ids(List<PecaMelhoria> pecas) {
		List<String> ids = new ArrayList<String>();
		for (PecaMelhoria p : pecas) {
			ids.add(p.getId());
		}
		return ids;
	}

	@Override
	public String toString() {
		return getId();
	}
}

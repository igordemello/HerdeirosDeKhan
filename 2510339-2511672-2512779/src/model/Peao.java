package model;

/**
 * Peão de um jogador no mapa.
 *
 * Não basta identificar o peão pela cor porque Altani joga com 2 peões. Por isso cada peão tem a cor do dono e um número (1 ou 2).
 * Dois objetos Peao com a mesma cor e o mesmo número representam o mesmo
 * peão físico, o que permite usá-lo como chave de HashMap.
 */
final class Peao {
	private final Cor cor;
	private final int numero;

	Peao(Cor cor, int numero) {
		if (cor == null) {
			throw new IllegalArgumentException("Cor do peão não pode ser nula");
		}
		int maximo = cor.getHerdeiro().getNumeroDePeoes();
		if (numero < 1 || numero > maximo) {
			throw new IllegalArgumentException("O jogador " + cor + " tem peões de 1 a " + maximo
					+ " (recebido: " + numero + ")");
		}
		this.cor = cor;
		this.numero = numero;
	}

	Cor getCor() {
		return cor;
	}

	int getNumero() {
		return numero;
	}

	@Override
	public boolean equals(Object outro) {
		if (this == outro) {
			return true;
		}
		if (!(outro instanceof Peao)) {
			return false;
		}
		Peao p = (Peao) outro;
		return cor == p.cor && numero == p.numero;
	}

	@Override
	public int hashCode() {
		return 31 * cor.hashCode() + numero;
	}

	@Override
	public String toString() {
		return "Peão " + cor + " #" + numero;
	}
}

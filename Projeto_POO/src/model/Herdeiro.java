package model;

/**
 * Herdeiros do Khan. Cada tabuleiro de jogador representa um herdeiro,
 * que possui uma habilidade especial (ver "Habilidades Especiais", pág. 20).
 *
 * As habilidades em si serão tratadas em iterações futuras; aqui ficam
 * apenas os dados que afetam a preparação do jogo.
 */
enum Herdeiro {
	ALTANI("Altani", 2),   // Altani joga com 2 peões (por isso o jogo tem 6 peões)
	CHAGATAI("Chagatai", 1),
	JOCHI("Jochi", 1),
	OGEDEI("Ogedei", 1),
	TOLUI("Tolui", 1);     // Tolui coloca 1 yurt em Karakorum no início do jogo

	private final String nome;
	private final int numeroDePeoes;

	Herdeiro(String nome, int numeroDePeoes) {
		this.nome = nome;
		this.numeroDePeoes = numeroDePeoes;
	}

	String getNome() {
		return nome;
	}

	int getNumeroDePeoes() {
		return numeroDePeoes;
	}

	/** Tolui começa o jogo com 1 yurt já colocado em Karakorum. */
	boolean comecaComYurtEmKarakorum() {
		return this == TOLUI;
	}
}

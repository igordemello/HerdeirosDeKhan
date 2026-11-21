package model;

/**
 * Herdeiros do Khan. Cada tabuleiro de jogador representa um herdeiro,
 * que possui uma habilidade especial (ver "Habilidades Especiais", pág. 20).
 *
 * Aqui ficam os dados das habilidades; as regras em si são aplicadas pelo
 * {@link Jogo}, pelo {@link TabuleiroJogador} e pelo mapa.
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

	// Habilidades que vêm escritas nas cartelas e mexem nas ações do mapa.

	// Altani: por turno só um dos 2 peões anda e faz ações
	boolean usaUmPeaoPorTurno() {
		return this == ALTANI;
	}

	// Tolui: pode construir yurt nas paradas vizinhas de onde está
	boolean constroiYurtAdjacente() {
		return this == TOLUI;
	}

	// Ogedei: 1x por turno paga 1 moeda por um tesouro de qualquer cidade (menos o último)
	boolean compraTesouroComMoeda() {
		return this == OGEDEI;
	}

	// Chagatai: 1x por turno troca 1 cavalo (movimento) por 1 espada
	boolean trocaCavaloPorEspada() {
		return this == CHAGATAI;
	}

	// Jochi: no início do jogo pega 2 melhorias ao acaso e coloca uma delas na
	// coluna branca; não precisa seguir a cor das peças de melhoria.
	boolean comecaComMelhoriaNaColunaBranca() {
		return this == JOCHI;
	}

	boolean ignoraCorDasMelhorias() {
		return this == JOCHI;
	}
}

package model;

/*
 * As 3 regiões do tabuleiro principal. Karakorum, no centro, não
 * pertence a nenhuma região.
 *
 * No fim do jogo, quem tem mais yurts (e o 2º com mais) em cada região ganha
 * votos conforme as melhorias na coluna associada
 */
enum Regiao {
	RUSSIA("Rússia"),
	CHINA("China"),
	PERSIA("Pérsia");

	private final String nome;

	Regiao(String nome) {
		this.nome = nome;
	}

	String getNome() {
		return nome;
	}
}

package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tabuleiro de um jogador (págs. 6, 9 e 11-12): as 4 colunas, as fichas de
 * ativação colocadas nelas e as melhorias de cada coluna.
 *
 * Regras:
 * <ul>
 * <li>no seu turno o jogador coloca uma ficha numa coluna livre e faz as ações
 * dela; a ficha fica na coluna até ser recuperada;</li>
 * <li>ao ativar a coluna branca, o jogador recupera todas as fichas,
 * inclusive a que acabou de usar;</li>
 * <li>o número de movimentos do turno é o número de cavalos da coluna
 * ativada (base + melhorias);</li>
 * <li>as melhorias são colocadas de baixo para cima, no máximo 4 por coluna.</li>
 * </ul>
 */
class TabuleiroJogador {
	static final int FICHAS_ATIVACAO = 4;
	static final int MAX_MELHORIAS_POR_COLUNA = 4;

	/** Colunas com uma ficha de ativação colocada. */
	private final Map<ColunaJogador, Boolean> ocupada = new HashMap<ColunaJogador, Boolean>();
	/** Melhorias de cada coluna, da mais baixa para a mais alta. */
	private final Map<ColunaJogador, List<TipoMelhoria>> melhorias = new HashMap<ColunaJogador, List<TipoMelhoria>>();

	TabuleiroJogador() {
		for (ColunaJogador c : ColunaJogador.values()) {
			ocupada.put(c, false);
			melhorias.put(c, new ArrayList<TipoMelhoria>());
		}
	}

	// ---------------------------------------------------- fichas de ativação

	boolean colunaOcupada(ColunaJogador coluna) {
		return ocupada.get(verificar(coluna));
	}

	/** A coluna pode ser ativada se ainda não tem ficha. */
	boolean podeAtivar(ColunaJogador coluna) {
		return !colunaOcupada(coluna);
	}

	/**
	 * Coloca uma ficha de ativação na coluna. Se for a coluna branca, todas
	 * as fichas voltam para o jogador.
	 *
	 * @return {@code false} se a coluna já tem ficha (nada é alterado)
	 */
	boolean ativar(ColunaJogador coluna) {
		if (!podeAtivar(coluna)) {
			return false;
		}
		if (coluna.recuperaFichas()) {
			recuperarFichas();
		} else {
			ocupada.put(coluna, true);
		}
		return true;
	}

	/** Recupera todas as fichas de ativação. */
	void recuperarFichas() {
		for (ColunaJogador c : ColunaJogador.values()) {
			ocupada.put(c, false);
		}
	}

	int getFichasDisponiveis() {
		int usadas = 0;
		for (ColunaJogador c : ColunaJogador.values()) {
			if (ocupada.get(c)) {
				usadas++;
			}
		}
		return FICHAS_ATIVACAO - usadas;
	}

	// ------------------------------------------------------- ações da coluna

	/** Cavalos da coluna = movimentos do turno em que ela for ativada. */
	int getCavalos(ColunaJogador coluna) {
		int total = verificar(coluna).getCavalosIniciais();
		for (TipoMelhoria m : melhorias.get(coluna)) {
			total += m.getCavalos();
		}
		return total;
	}

	/** Punhos da coluna = quantas peças de tributo o jogador pode pegar. */
	int getPunhos(ColunaJogador coluna) {
		int total = verificar(coluna).getPunhosIniciais();
		for (TipoMelhoria m : melhorias.get(coluna)) {
			total += m.getPunhos();
		}
		return total;
	}

	// ------------------------------------------------------------- melhorias

	/**
	 * Coloca uma melhoria no espaço livre mais baixo da coluna.
	 *
	 * @return {@code false} se a coluna já está cheia
	 */
	boolean adicionarMelhoria(ColunaJogador coluna, TipoMelhoria melhoria) {
		if (melhoria == null) {
			throw new IllegalArgumentException("Melhoria nula");
		}
		List<TipoMelhoria> lista = melhorias.get(verificar(coluna));
		if (lista.size() >= MAX_MELHORIAS_POR_COLUNA) {
			return false;
		}
		lista.add(melhoria);
		return true;
	}

	List<TipoMelhoria> getMelhorias(ColunaJogador coluna) {
		return Collections.unmodifiableList(melhorias.get(verificar(coluna)));
	}

	private static ColunaJogador verificar(ColunaJogador coluna) {
		if (coluna == null) {
			throw new IllegalArgumentException("Coluna nula");
		}
		return coluna;
	}
}

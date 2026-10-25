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
 * <li>as melhorias são colocadas de baixo para cima, no máximo 4 por coluna;
 * numa coluna colorida a peça tem de ser da cor dela, na branca vale
 * qualquer cor (pág. 11-12).</li>
 * </ul>
 */
class TabuleiroJogador {
	static final int FICHAS_ATIVACAO = 4;
	static final int MAX_MELHORIAS_POR_COLUNA = 4;

	/**
	 * Ações da base de cada coluna, por herdeiro, na ordem de
	 * {@link ColunaJogador} (vermelha, verde, amarela, branca). Transcrito das
	 * imagens dos tabuleiros disponibilizadas no EAD.
	 */
	private static final Map<Herdeiro, AcoesColuna[]> ACOES_POR_HERDEIRO = new HashMap<Herdeiro, AcoesColuna[]>();
	static {
		ACOES_POR_HERDEIRO.put(Herdeiro.ALTANI, new AcoesColuna[] { AcoesColuna.CAVALO_DOIS_PUNHOS_KHAN,
				AcoesColuna.DOIS_CAVALOS_PUNHO, AcoesColuna.CAVALO_PUNHO_MOEDA, AcoesColuna.BRANCA });
		ACOES_POR_HERDEIRO.put(Herdeiro.JOCHI, new AcoesColuna[] { AcoesColuna.CAVALO_PUNHO_MOEDA,
				AcoesColuna.CAVALO_DOIS_PUNHOS_KHAN, AcoesColuna.DOIS_CAVALOS_PUNHO, AcoesColuna.BRANCA });
		ACOES_POR_HERDEIRO.put(Herdeiro.CHAGATAI, new AcoesColuna[] { AcoesColuna.CAVALO_DOIS_PUNHOS_KHAN,
				AcoesColuna.CAVALO_PUNHO_MOEDA, AcoesColuna.DOIS_CAVALOS_PUNHO, AcoesColuna.BRANCA });
		ACOES_POR_HERDEIRO.put(Herdeiro.TOLUI, new AcoesColuna[] { AcoesColuna.CAVALO_PUNHO_MOEDA,
				AcoesColuna.DOIS_CAVALOS_PUNHO, AcoesColuna.CAVALO_DOIS_PUNHOS_KHAN, AcoesColuna.BRANCA });
		ACOES_POR_HERDEIRO.put(Herdeiro.OGEDEI, new AcoesColuna[] { AcoesColuna.DOIS_CAVALOS_PUNHO,
				AcoesColuna.CAVALO_PUNHO_MOEDA, AcoesColuna.CAVALO_DOIS_PUNHOS_KHAN, AcoesColuna.BRANCA });
	}

	private final Herdeiro herdeiro;
	/** Colunas com uma ficha de ativação colocada. */
	private final Map<ColunaJogador, Boolean> ocupada = new HashMap<ColunaJogador, Boolean>();
	/** Melhorias de cada coluna, da mais baixa para a mais alta. */
	private final Map<ColunaJogador, List<PecaMelhoria>> melhorias = new HashMap<ColunaJogador, List<PecaMelhoria>>();

	TabuleiroJogador(Herdeiro herdeiro) {
		if (herdeiro == null) {
			throw new IllegalArgumentException("Herdeiro nulo");
		}
		this.herdeiro = herdeiro;
		for (ColunaJogador c : ColunaJogador.values()) {
			ocupada.put(c, false);
			melhorias.put(c, new ArrayList<PecaMelhoria>());
		}
	}

	Herdeiro getHerdeiro() {
		return herdeiro;
	}

	/** Ações impressas na base da coluna, neste tabuleiro. */
	AcoesColuna getAcoesIniciais(ColunaJogador coluna) {
		return ACOES_POR_HERDEIRO.get(herdeiro)[verificar(coluna).ordinal()];
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
		int total = getAcoesIniciais(coluna).getCavalos();
		for (PecaMelhoria m : melhorias.get(coluna)) {
			total += m.getTipo().getCavalos();
		}
		return total;
	}

	/** Punhos da coluna = quantas peças de tributo o jogador pode pegar. */
	int getPunhos(ColunaJogador coluna) {
		int total = getAcoesIniciais(coluna).getPunhos();
		for (PecaMelhoria m : melhorias.get(coluna)) {
			total += m.getTipo().getPunhos();
		}
		return total;
	}

	/** Se a coluna tem o ícone do Khan neste tabuleiro. */
	boolean usaKhan(ColunaJogador coluna) {
		return getAcoesIniciais(coluna).usaKhan();
	}

	// ------------------------------------------------------------- melhorias

	/**
	 * Coloca uma peça de melhoria no espaço livre mais baixo da coluna.
	 *
	 * @return {@code false} se a coluna já está cheia ou se a cor da peça não
	 *         combina com a coluna (nada é alterado)
	 */
	boolean adicionarMelhoria(ColunaJogador coluna, PecaMelhoria peca) {
		if (peca == null) {
			throw new IllegalArgumentException("Melhoria nula");
		}
		List<PecaMelhoria> lista = melhorias.get(verificar(coluna));
		if (lista.size() >= MAX_MELHORIAS_POR_COLUNA) {
			return false;
		}
		if (coluna.ehColorida() && peca.getCor() != coluna) {
			return false;
		}
		lista.add(peca);
		return true;
	}

	/**
	 * Atalho para colunas coloridas: a peça tem a cor da coluna (ou a cor
	 * fixa, no caso dos tesouros virtuais).
	 */
	boolean adicionarMelhoria(ColunaJogador coluna, TipoMelhoria tipo) {
		ColunaJogador cor = tipo.getCorFixa() != null ? tipo.getCorFixa() : verificar(coluna);
		if (!cor.ehColorida()) {
			throw new IllegalArgumentException("Informe a cor da peça para a coluna branca");
		}
		return adicionarMelhoria(coluna, new PecaMelhoria(tipo, cor));
	}

	List<PecaMelhoria> getMelhorias(ColunaJogador coluna) {
		return Collections.unmodifiableList(melhorias.get(verificar(coluna)));
	}

	private static ColunaJogador verificar(ColunaJogador coluna) {
		if (coluna == null) {
			throw new IllegalArgumentException("Coluna nula");
		}
		return coluna;
	}
}

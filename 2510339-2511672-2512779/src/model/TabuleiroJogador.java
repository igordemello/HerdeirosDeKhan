package model;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Tabuleiro de um jogador (págs. 6, 9 e 11-12): as 4 colunas, as fichas de
 * ativação e as melhorias de cada coluna.
 *
 * No turno o jogador põe uma ficha numa coluna livre e faz as ações dela. A
 * ficha só sai quando ele ativa a coluna branca, que devolve todas (até a
 * que acabou de usar). Os movimentos do turno são os cavalos da coluna
 * (base + melhorias). As melhorias entram de baixo pra cima, no máximo 4 por
 * coluna; nas coloridas a peça tem que ser da mesma cor, na branca vale
 * qualquer uma. O Jochi pode ignorar a cor.
 */
class TabuleiroJogador {
	static final int FICHAS_ATIVACAO = 4;
	static final int MAX_MELHORIAS_POR_COLUNA = 4;

	// Ações impressas na base das colunas de cada herdeiro, na ordem vermelha,
	// verde, amarela, branca. Copiamos olhando as cartelas do EAD.
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
	private final Map<ColunaJogador, Boolean> ocupada = new HashMap<ColunaJogador, Boolean>();
	// Melhorias de cada coluna, da mais baixa para a mais alta.
	private final Map<ColunaJogador, List<PecaMelhoria>> melhorias = new HashMap<ColunaJogador, List<PecaMelhoria>>();
	// peças colocadas neste turno, que só valem a partir do próximo (pág. 12)
	private final Set<PecaMelhoria> novasNoTurno = new HashSet<PecaMelhoria>();

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

	AcoesColuna getAcoesIniciais(ColunaJogador coluna) {
		return ACOES_POR_HERDEIRO.get(herdeiro)[verificar(coluna).ordinal()];
	}

	// fichas de ativação

	boolean colunaOcupada(ColunaJogador coluna) {
		return ocupada.get(verificar(coluna));
	}

	boolean podeAtivar(ColunaJogador coluna) {
		return !colunaOcupada(coluna);
	}

	// Se for a branca, em vez de ocupar ela devolve todas as fichas.
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

	// ações da coluna

	// cavalos = movimentos do turno (as melhorias novas ainda não contam)
	int getCavalos(ColunaJogador coluna) {
		int total = getAcoesIniciais(coluna).getCavalos();
		for (PecaMelhoria m : melhoriasUsaveis(coluna)) {
			total += m.getTipo().getCavalos();
		}
		return total;
	}

	// punhos = quantos tributos ele pode pegar
	int getPunhos(ColunaJogador coluna) {
		int total = getAcoesIniciais(coluna).getPunhos();
		for (PecaMelhoria m : melhoriasUsaveis(coluna)) {
			total += m.getTipo().getPunhos();
		}
		return total;
	}

	// Tributos virtuais que a coluna dá quando é ativada: a moeda impressa na
	// base (só existe moeda na base) + as melhorias virtuais (pág. 16).
	int getTributosVirtuais(ColunaJogador coluna, TipoTributo tipo) {
		int total = tipo == TipoTributo.MOEDA ? getAcoesIniciais(coluna).getMoedasVirtuais() : 0;
		for (PecaMelhoria m : melhoriasUsaveis(coluna)) {
			if (m.getTipo().getTributoVirtual() == tipo) {
				total++;
			}
		}
		return total;
	}

	List<TipoTesouro> getTesourosVirtuais(ColunaJogador coluna) {
		List<TipoTesouro> lista = new ArrayList<TipoTesouro>();
		for (PecaMelhoria m : melhoriasUsaveis(coluna)) {
			if (m.getTipo().getTesouroVirtual() != null) {
				lista.add(m.getTipo().getTesouroVirtual());
			}
		}
		return lista;
	}

	private List<PecaMelhoria> melhoriasUsaveis(ColunaJogador coluna) {
		List<PecaMelhoria> lista = new ArrayList<PecaMelhoria>();
		for (PecaMelhoria m : melhorias.get(verificar(coluna))) {
			if (!novasNoTurno.contains(m)) {
				lista.add(m);
			}
		}
		return lista;
	}

	boolean usaKhan(ColunaJogador coluna) {
		return getAcoesIniciais(coluna).usaKhan();
	}

	// melhorias

	// Põe a peça no espaço livre mais baixo. Devolve false se a coluna está
	// cheia ou se a cor não bate.
	boolean adicionarMelhoria(ColunaJogador coluna, PecaMelhoria peca) {
		if (peca == null) {
			throw new IllegalArgumentException("Melhoria nula");
		}
		List<PecaMelhoria> lista = melhorias.get(verificar(coluna));
		if (lista.size() >= MAX_MELHORIAS_POR_COLUNA) {
			return false;
		}
		if (coluna.ehColorida() && peca.getCor() != coluna && !herdeiro.ignoraCorDasMelhorias()) {
			return false;
		}
		lista.add(peca);
		novasNoTurno.add(peca);
		return true;
	}

	// as melhorias compradas no turno passam a valer
	void encerrarTurno() {
		novasNoTurno.clear();
	}

	boolean temEspaco(ColunaJogador coluna) {
		return melhorias.get(verificar(coluna)).size() < MAX_MELHORIAS_POR_COLUNA;
	}

	// Linha completa = as 3 coloridas têm melhoria naquela altura. Cada uma dá
	// uma melhoria grátis na coluna branca (pág. 12).
	int getLinhasCompletas() {
		int menor = MAX_MELHORIAS_POR_COLUNA;
		for (ColunaJogador c : ColunaJogador.values()) {
			if (c.ehColorida()) {
				menor = Math.min(menor, melhorias.get(c).size());
			}
		}
		return menor;
	}

	int getTotalMelhorias() {
		int total = 0;
		for (List<PecaMelhoria> lista : melhorias.values()) {
			total += lista.size();
		}
		return total;
	}

	List<PecaMelhoria> getTodasAsMelhorias() {
		List<PecaMelhoria> todas = new ArrayList<PecaMelhoria>();
		for (ColunaJogador c : ColunaJogador.values()) {
			todas.addAll(melhorias.get(c));
		}
		return todas;
	}

	// atalho pros testes: cria a peça com a cor da coluna (ou a cor fixa dos
	// tesouros virtuais)
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

	// salvar e carregar (4ª it.)

	// Escreve as colunas com ficha e as melhorias de cada coluna, de baixo pra
	// cima. Como só se salva entre turnos, não precisa guardar as peças novas.
	void salvar(PrintWriter saida) {
		List<String> comFicha = new ArrayList<String>();
		for (ColunaJogador c : ColunaJogador.values()) {
			if (ocupada.get(c)) {
				comFicha.add(c.name());
			}
		}
		ArquivoPartida.linha(saida, "fichas", ArquivoPartida.juntar(comFicha));
		for (ColunaJogador c : ColunaJogador.values()) {
			ArquivoPartida.linha(saida, "melhorias." + c.name(), ArquivoPartida.juntar(PecaMelhoria.ids(melhorias.get(c))));
		}
	}

	void carregar(Map<String, String> secao) {
		for (ColunaJogador c : ColunaJogador.values()) {
			ocupada.put(c, false);
			melhorias.get(c).clear();
		}
		novasNoTurno.clear();
		for (String nome : ArquivoPartida.lista(secao, "fichas")) {
			ColunaJogador c = ColunaJogador.valueOf(nome);
			if (c.recuperaFichas()) {
				throw new IllegalArgumentException("A coluna branca nunca fica com ficha");
			}
			ocupada.put(c, true);
		}
		for (ColunaJogador c : ColunaJogador.values()) {
			List<PecaMelhoria> pecas = PecaMelhoria.deIds(ArquivoPartida.lista(secao, "melhorias." + c.name()));
			if (pecas.size() > MAX_MELHORIAS_POR_COLUNA) {
				throw new IllegalArgumentException("Coluna " + c + " com melhorias demais");
			}
			melhorias.get(c).addAll(pecas);
		}
	}

	private static ColunaJogador verificar(ColunaJogador coluna) {
		if (coluna == null) {
			throw new IllegalArgumentException("Coluna nula");
		}
		return coluna;
	}
}

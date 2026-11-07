package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * Guarda o que o jogador da vez já fez no mapa durante o turno. O Jogo
 * precisa disso para saber:
 *  - de quais paradas o peão pode pegar tributo, construir yurt e atacar
 *    (onde ele esteve no turno, contando a parada de onde saiu, mas não as
 *    paradas com yurt próprio que ele pulou);
 *  - quantos punhos da coluna já foram gastos;
 *  - quantos tesouros já tirou de cada cidade (1º custa 1 espada, os outros 2);
 *  - se já usou o Khan e a habilidade do herdeiro (Chagatai e Ogedei).
 *
 * As paradas ficam separadas por peão por causa da Altani, que tem 2 peões
 * mas só pode usar um por turno. O primeiro que se mexer (ou que fizer uma
 * ação) vira o peão do turno.
 */
class TurnoNoMapa {
	private final Map<Integer, Set<String>> paradasPorPeao = new HashMap<Integer, Set<String>>();
	private Integer peaoDoTurno = null; // null enquanto nenhum peão foi usado
	private int punhosUsados = 0;
	private final Map<NomeCidade, Integer> tesourosTomados = new EnumMap<NomeCidade, Integer>(NomeCidade.class);
	private boolean khanUsado = false;
	private boolean habilidadeUsada = false;

	/** Zera tudo e anota onde estão os peões do jogador no começo do turno. */
	void iniciar(Jogador jogador, Tabuleiro tabuleiro) {
		if (jogador == null || tabuleiro == null) {
			throw new IllegalArgumentException("Jogador e tabuleiro não podem ser nulos");
		}
		paradasPorPeao.clear();
		peaoDoTurno = null;
		punhosUsados = 0;
		tesourosTomados.clear();
		khanUsado = false;
		habilidadeUsada = false;
		for (int n = 1; n <= jogador.getHerdeiro().getNumeroDePeoes(); n++) {
			Set<String> paradas = new HashSet<String>();
			Parada inicio = tabuleiro.getParadaDoPeao(new Peao(jogador.getCor(), n));
			if (inicio != null) {
				paradas.add(inicio.getId());
			}
			paradasPorPeao.put(n, paradas);
		}
	}

	// ------------------------------------------------------------- paradas

	void registrarMovimento(int numeroPeao, Collection<String> visitadas) {
		if (!podeUsarPeao(numeroPeao)) {
			throw new IllegalStateException("Neste turno só o peão " + peaoDoTurno + " pode se mover");
		}
		Set<String> paradas = paradasPorPeao.get(numeroPeao);
		if (paradas == null) {
			paradas = new HashSet<String>();
			paradasPorPeao.put(numeroPeao, paradas);
		}
		paradas.addAll(visitadas);
		peaoDoTurno = numeroPeao;
	}

	boolean podeUsarPeao(int numeroPeao) {
		return peaoDoTurno == null || peaoDoTurno == numeroPeao;
	}

	/** Paradas que servem de base para as ações agora. */
	Set<String> getParadasDasAcoes() {
		if (peaoDoTurno != null) {
			return Collections.unmodifiableSet(paradasPorPeao.get(peaoDoTurno));
		}
		// nenhum peão escolhido ainda: vale a parada de qualquer um deles
		Set<String> todas = new HashSet<String>();
		for (Set<String> paradas : paradasPorPeao.values()) {
			todas.addAll(paradas);
		}
		return Collections.unmodifiableSet(todas);
	}

	boolean esteveEm(String idParada) {
		return getParadasDasAcoes().contains(idParada);
	}

	boolean esteveEmAlguma(Collection<Parada> paradas) {
		for (Parada p : paradas) {
			if (esteveEm(p.getId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Chamado quando uma ação usa uma dessas paradas. Se o peão do turno ainda
	 * não foi escolhido, fica sendo o primeiro peão que passou por uma delas.
	 */
	void usarBase(Collection<Parada> paradas) {
		if (peaoDoTurno != null) {
			return;
		}
		List<Integer> numeros = new ArrayList<Integer>(paradasPorPeao.keySet());
		Collections.sort(numeros);
		for (int n : numeros) {
			for (Parada p : paradas) {
				if (paradasPorPeao.get(n).contains(p.getId())) {
					peaoDoTurno = n;
					return;
				}
			}
		}
	}

	// ------------------------------------------------------- punhos e ataques

	int getPunhosUsados() {
		return punhosUsados;
	}

	void usarPunho() {
		punhosUsados++;
	}

	int custoDoAtaque(NomeCidade cidade) {
		Integer tomados = tesourosTomados.get(cidade);
		return tomados == null || tomados == 0 ? 1 : 2;
	}

	void registrarAtaque(NomeCidade cidade) {
		Integer tomados = tesourosTomados.get(cidade);
		tesourosTomados.put(cidade, tomados == null ? 1 : tomados + 1);
	}

	// --------------------------------------------------- Khan e habilidade

	boolean khanUsado() {
		return khanUsado;
	}

	void registrarKhan() {
		khanUsado = true;
	}

	boolean habilidadeUsada() {
		return habilidadeUsada;
	}

	void registrarHabilidade() {
		habilidadeUsada = true;
	}
}

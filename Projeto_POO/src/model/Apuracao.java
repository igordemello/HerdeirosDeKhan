package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Contagens usadas pelas cartas de conselheiro e pela pontuação de fim de
 * jogo. Junta o que o jogador tem (tributos, tesouros, melhorias, bônus) com
 * o que está no mapa (yurts, cidades, províncias).
 */
final class Apuracao {

	/** Coluna do tabuleiro do jogador associada a cada região (pág. 15). */
	private static final Map<Regiao, ColunaJogador> COLUNA_DA_REGIAO = new HashMap<Regiao, ColunaJogador>();
	static {
		COLUNA_DA_REGIAO.put(Regiao.RUSSIA, ColunaJogador.AMARELA);
		COLUNA_DA_REGIAO.put(Regiao.PERSIA, ColunaJogador.VERDE);
		COLUNA_DA_REGIAO.put(Regiao.CHINA, ColunaJogador.VERMELHA);
	}

	/** Votos de influência por número de melhorias (0 a 4) na coluna da região (pág. 15). */
	private static final int[] VOTOS_MAIS_YURTS = { 1, 1, 2, 2, 3 };
	private static final int[] VOTOS_SEGUNDO_MAIS_YURTS = { 0, 1, 1, 2, 2 };

	private Apuracao() {
	}

	static ColunaJogador colunaDaRegiao(Regiao regiao) {
		return COLUNA_DA_REGIAO.get(regiao);
	}

	// ============================================== conselheiros secretos

	/**
	 * Quantidade que o jogador atingiu na condição do conselheiro secreto
	 * (pág. 19); os votos saem de {@link TipoConselheiroSecreto#calcularVotos}.
	 */
	static int quantidadeSecreto(TipoConselheiroSecreto tipo, Jogador jogador, Tabuleiro tabuleiro) {
		Cor cor = jogador.getCor();
		TabuleiroJogador tab = jogador.getTabuleiro();
		switch (tipo) {
		case VIAJANTE:
			return maiorGrupoDeYurtsConectados(cor, tabuleiro);
		case SOLDADO:
			return tab.getTotalMelhorias();
		case COLECIONADOR:
			return contarPorSimbolo(tab).size();
		case SACERDOTE: {
			int pares = 0;
			for (int n : contarPorSimbolo(tab).values()) {
				pares += n / 2;
			}
			return pares;
		}
		case GOVERNADOR: {
			int regioes = 0;
			for (Regiao r : Regiao.values()) {
				if (tabuleiro.contarYurtsNaRegiao(cor, r) >= 2
						&& tab.getMelhorias(colunaDaRegiao(r)).size() >= 2) {
					regioes++;
				}
			}
			return regioes;
		}
		case BAJULADOR: {
			int provincias = 0;
			for (Provincia p : tabuleiro.getProvincias()) {
				if (p.isDoKhan() && yurtsEmVolta(cor, p.getParadasAdjacentes()) >= 2) {
					provincias++;
				}
			}
			return provincias;
		}
		case PREFEITO:
			return contarCidadesComYurtsAdjacentes(cor, tabuleiro, 2);
		case CIDADAO:
			return contarCidadesComYurtsAdjacentes(cor, tabuleiro, 1);
		case CAVALEIRO: {
			int n = 0;
			for (PecaMelhoria m : tab.getTodasAsMelhorias()) {
				if (m.getTipo() == TipoMelhoria.DOIS_CAVALOS || m.getTipo() == TipoMelhoria.PUNHO) {
					n++;
				}
			}
			return n;
		}
		case MENSAGEIRO: {
			int n = 0;
			for (Parada p : tabuleiro.getParadas()) {
				if (p.getTipo() == TipoParada.DUPLA && p.temYurtDe(cor)) {
					n++;
				}
			}
			return n;
		}
		default:
			throw new IllegalArgumentException("Tipo desconhecido: " + tipo);
		}
	}

	/**
	 * Melhorias agrupadas por símbolo. Os 5 tesouros virtuais contam como um
	 * símbolo só ("tesouro virtual").
	 */
	private static Map<String, Integer> contarPorSimbolo(TabuleiroJogador tab) {
		Map<String, Integer> contagem = new HashMap<String, Integer>();
		for (PecaMelhoria m : tab.getTodasAsMelhorias()) {
			String simbolo = m.getTipo().getTesouroVirtual() != null ? "TESOURO_VIRTUAL" : m.getTipo().name();
			Integer atual = contagem.get(simbolo);
			contagem.put(simbolo, atual == null ? 1 : atual + 1);
		}
		return contagem;
	}

	private static int yurtsEmVolta(Cor cor, Collection<Parada> paradas) {
		int n = 0;
		for (Parada p : paradas) {
			n += p.contarYurts(cor);
		}
		return n;
	}

	private static int contarCidadesComYurtsAdjacentes(Cor cor, Tabuleiro tabuleiro, int minimo) {
		int n = 0;
		for (NomeCidade nome : NomeCidade.values()) {
			Cidade cidade = tabuleiro.getCidade(nome);
			if (cidade != null && cidade.contarYurtsAdjacentes(cor) >= minimo) {
				n++;
			}
		}
		return n;
	}

	/** Tamanho do maior grupo de paradas vizinhas que têm yurt do jogador. */
	static int maiorGrupoDeYurtsConectados(Cor cor, Tabuleiro tabuleiro) {
		Set<Parada> visitadas = new HashSet<Parada>();
		int maior = 0;
		for (Parada inicio : tabuleiro.getParadas()) {
			if (!inicio.temYurtDe(cor) || visitadas.contains(inicio)) {
				continue;
			}
			int tamanho = 0;
			Queue<Parada> fila = new LinkedList<Parada>();
			fila.add(inicio);
			visitadas.add(inicio);
			while (!fila.isEmpty()) {
				Parada atual = fila.remove();
				tamanho += atual.contarYurts(cor);
				for (Parada vizinha : atual.getVizinhas()) {
					if (vizinha.temYurtDe(cor) && visitadas.add(vizinha)) {
						fila.add(vizinha);
					}
				}
			}
			maior = Math.max(maior, tamanho);
		}
		return maior;
	}

	// ================================================ conselheiros abertos

	/**
	 * Se o jogador cumpre a demanda da carta de conselheiro aberto (pág. 18).
	 *
	 * @param espadasVirtuais e tesourosVirtuais: o que as melhorias da coluna
	 *                        ativada valem neste turno (0 e lista vazia para
	 *                        quem não é o jogador da vez)
	 */
	static boolean cumpre(TipoConselheiroAberto tipo, Jogador jogador, Tabuleiro tabuleiro, int espadasVirtuais,
			List<TipoTesouro> tesourosVirtuais) {
		Cor cor = jogador.getCor();
		TabuleiroJogador tab = jogador.getTabuleiro();
		int q = tipo.getQuantidade();
		switch (tipo) {
		case ESPADAS_4:
			return jogador.getQuantidadeTributo(TipoTributo.ESPADA) + espadasVirtuais >= q;
		case YURTS_NO_MAPA_6:
		case YURTS_NO_MAPA_10:
			return tabuleiro.contarYurtsNoMapa(cor) >= q;
		case TESOUROS_4:
			return jogador.getTotalTesouros() + tesourosVirtuais.size() >= q;
		case TESOUROS_DIFERENTES_5: {
			Set<TipoTesouro> tipos = new HashSet<TipoTesouro>(tesourosVirtuais);
			for (TipoTesouro t : TipoTesouro.values()) {
				if (jogador.getQuantidadeTesouro(t) > 0) {
					tipos.add(t);
				}
			}
			return tipos.size() >= q;
		}
		case YURT_EM_CADA_REGIAO_1:
		case YURTS_EM_CADA_REGIAO_2:
			for (Regiao r : Regiao.values()) {
				if (tabuleiro.contarYurtsNaRegiao(cor, r) < q) {
					return false;
				}
			}
			return true;
		case PECAS_BONUS_2:
		case PECAS_BONUS_3:
			return jogador.getTotalBonusRecebidos() >= q;
		case CIDADES_CONQUISTADAS_2:
		case CIDADES_CONQUISTADAS_3:
			return contarCidadesConquistadas(cor, tabuleiro) >= q;
		case LINHA_DE_MELHORIAS:
			return tab.getLinhasCompletas() >= 1;
		case DUAS_COLUNAS_COM_2_MELHORIAS: {
			int colunas = 0;
			for (ColunaJogador c : ColunaJogador.values()) {
				if (tab.getMelhorias(c).size() >= 2) {
					colunas++;
				}
			}
			return colunas >= 2;
		}
		case QUATRO_MELHORIAS_EM_UMA_COLUNA:
			for (ColunaJogador c : ColunaJogador.values()) {
				if (tab.getMelhorias(c).size() >= q) {
					return true;
				}
			}
			return false;
		default:
			throw new IllegalArgumentException("Tipo desconhecido: " + tipo);
		}
	}

	static int contarCidadesConquistadas(Cor cor, Tabuleiro tabuleiro) {
		int n = 0;
		for (NomeCidade nome : NomeCidade.values()) {
			Cidade cidade = tabuleiro.getCidade(nome);
			if (cidade != null && cor == cidade.getConquistador()) {
				n++;
			}
		}
		return n;
	}

	// ========================================================= influência

	/**
	 * Votos de influência do jogador nas 3 regiões (págs. 14-15): quem tem
	 * mais yurts numa região e o 2º com mais recebem votos conforme o número
	 * de melhorias na coluna associada. Empate em 1º: todos ficam em 1º e
	 * ninguém fica em 2º. Empate em 2º: todos ficam em 2º. Quem não tem yurt
	 * na região não pontua nela.
	 */
	static int votosDeInfluencia(Jogador jogador, Collection<Jogador> todos, Tabuleiro tabuleiro) {
		int total = 0;
		for (Regiao regiao : Regiao.values()) {
			int meus = tabuleiro.contarYurtsNaRegiao(jogador.getCor(), regiao);
			if (meus == 0) {
				continue;
			}
			List<Integer> outros = new ArrayList<Integer>();
			for (Jogador j : todos) {
				if (j != jogador) {
					outros.add(tabuleiro.contarYurtsNaRegiao(j.getCor(), regiao));
				}
			}
			int maiorOutro = 0;
			for (int n : outros) {
				maiorOutro = Math.max(maiorOutro, n);
			}
			int melhorias = Math.min(4, jogador.getTabuleiro().getMelhorias(colunaDaRegiao(regiao)).size());
			if (meus >= maiorOutro) {
				total += VOTOS_MAIS_YURTS[melhorias];
				continue;
			}
			// estou abaixo do 1º; sou 2º se ninguém além dos 1ºs tem mais que eu
			// e o 1º lugar não foi dividido
			int empatadosNoPrimeiro = 0;
			for (int n : outros) {
				if (n == maiorOutro) {
					empatadosNoPrimeiro++;
				}
			}
			boolean segundo = empatadosNoPrimeiro == 1;
			for (int n : outros) {
				if (n != maiorOutro && n > meus) {
					segundo = false;
				}
			}
			if (segundo) {
				total += VOTOS_SEGUNDO_MAIS_YURTS[melhorias];
			}
		}
		return total;
	}
}

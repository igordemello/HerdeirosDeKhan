package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Formato do arquivo de partida salva (4ª iteração, Capítulo 16).
 *
 * O arquivo é texto ASCII puro, dividido em seções. Cada seção começa com
 * uma linha {@code [NOME]} e tem uma linha {@code chave=valor} por dado;
 * listas são separadas por vírgula e pares por dois-pontos:
 *
 * <pre>
 * HERDEIROS_DO_KHAN 1
 * [JOGADOR AZUL]
 * tributos=ESPADA:2,YURT:0,MOEDA:3
 * melhorias.VERDE=CAV-V,MAO-V
 * </pre>
 *
 * Só se salva antes de um jogador começar o turno, então nada do que vale
 * só durante o turno vai para o arquivo. Cada parte do Model escreve e lê as
 * suas próprias seções; aqui ficam só as funções comuns a todas.
 */
final class ArquivoPartida {
	static final String CABECALHO = "HERDEIROS_DO_KHAN 1";

	private ArquivoPartida() {
	}

	// --------------------------------------------------------------- escrita

	static void secao(PrintWriter saida, String nome) {
		saida.println("[" + nome + "]");
	}

	static void linha(PrintWriter saida, String chave, Object valor) {
		String texto = chave + "=" + valor;
		for (int i = 0; i < texto.length(); i++) {
			if (texto.charAt(i) > 127) {
				throw new IllegalArgumentException("O arquivo tem de ser ASCII puro: " + texto);
			}
		}
		saida.println(texto);
	}

	/** Junta os itens da lista com vírgula (lista vazia = texto vazio). */
	static String juntar(List<?> itens) {
		StringBuilder sb = new StringBuilder();
		for (Object o : itens) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append(o);
		}
		return sb.toString();
	}

	// ---------------------------------------------------------------- leitura

	/**
	 * Lê o arquivo inteiro e separa as seções.
	 *
	 * @return seção -> (chave -> valor)
	 * @throws IOException se a leitura falhar ou o arquivo não for de partida
	 */
	static Map<String, Map<String, String>> ler(Reader entrada) throws IOException {
		BufferedReader leitor = new BufferedReader(entrada);
		String primeira = leitor.readLine();
		if (primeira == null || !primeira.trim().equals(CABECALHO)) {
			throw new IOException("O arquivo não é uma partida salva de Herdeiros do Khan");
		}
		Map<String, Map<String, String>> secoes = new HashMap<String, Map<String, String>>();
		Map<String, String> atual = null;
		String linha;
		int numero = 1;
		while ((linha = leitor.readLine()) != null) {
			numero++;
			linha = linha.trim();
			if (linha.isEmpty() || linha.startsWith("#")) {
				continue;
			}
			if (linha.startsWith("[") && linha.endsWith("]")) {
				atual = new HashMap<String, String>();
				secoes.put(linha.substring(1, linha.length() - 1), atual);
				continue;
			}
			int igual = linha.indexOf('=');
			if (atual == null || igual < 0) {
				throw new IOException("Linha " + numero + " inválida: " + linha);
			}
			atual.put(linha.substring(0, igual).trim(), linha.substring(igual + 1).trim());
		}
		return secoes;
	}

	/** A seção pedida; se ela não existir, o arquivo está incompleto. */
	static Map<String, String> secaoObrigatoria(Map<String, Map<String, String>> secoes, String nome) {
		Map<String, String> s = secoes.get(nome);
		if (s == null) {
			throw new IllegalArgumentException("Falta a seção [" + nome + "] no arquivo");
		}
		return s;
	}

	/** Valor obrigatório da seção. */
	static String valor(Map<String, String> secao, String chave) {
		String v = secao.get(chave);
		if (v == null) {
			throw new IllegalArgumentException("Falta a chave '" + chave + "' no arquivo");
		}
		return v;
	}

	static int inteiro(Map<String, String> secao, String chave) {
		try {
			return Integer.parseInt(valor(secao, chave));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Número inválido em '" + chave + "'");
		}
	}

	/** Lista separada por vírgula; chave ausente ou vazia = lista vazia. */
	static List<String> lista(Map<String, String> secao, String chave) {
		String v = secao.get(chave);
		if (v == null || v.isEmpty()) {
			return new ArrayList<String>();
		}
		List<String> itens = new ArrayList<String>(Arrays.asList(v.split(",")));
		for (int i = 0; i < itens.size(); i++) {
			itens.set(i, itens.get(i).trim());
		}
		return itens;
	}

	/** Lista de pares "NOME:numero" (ex.: "ESPADA:2,MOEDA:3"). */
	static Map<String, Integer> pares(Map<String, String> secao, String chave) {
		Map<String, Integer> mapa = new HashMap<String, Integer>();
		for (String item : lista(secao, chave)) {
			int doisPontos = item.indexOf(':');
			if (doisPontos < 0) {
				throw new IllegalArgumentException("Par inválido em '" + chave + "': " + item);
			}
			try {
				mapa.put(item.substring(0, doisPontos), Integer.parseInt(item.substring(doisPontos + 1)));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Número inválido em '" + chave + "': " + item);
			}
		}
		return mapa;
	}
}

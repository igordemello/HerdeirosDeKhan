package view;

import java.util.HashMap;
import java.util.Map;

/**
 * Nomes legíveis, em português, dos identificadores que o Model devolve
 * (tipos de tesouro, tributo, bônus, melhorias e conselheiros).
 */
final class Textos {
	private static final Map<String, String> TESOUROS = new HashMap<String, String>();
	private static final Map<String, String> TRIBUTOS = new HashMap<String, String>();
	private static final Map<String, String> BONUS = new HashMap<String, String>();
	private static final Map<String, String> SIGLAS_MELHORIA = new HashMap<String, String>();
	private static final Map<String, String> CORES_MELHORIA = new HashMap<String, String>();
	private static final Map<String, String> CONSELHEIROS = new HashMap<String, String>();

	static {
		TESOUROS.put("BOI", "Boi");
		TESOUROS.put("ESPECIARIA", "Especiaria");
		TESOUROS.put("FERRO", "Ferro");
		TESOUROS.put("PELE", "Pele");
		TESOUROS.put("SEDA", "Seda");
		TESOUROS.put("QUALQUER", "Qualquer");

		TRIBUTOS.put("ESPADA", "Espadas");
		TRIBUTOS.put("YURT", "Yurts");
		TRIBUTOS.put("MOEDA", "Moedas");

		BONUS.put("VOTO", "1 voto");
		BONUS.put("DUAS_ESPADAS", "2 espadas");
		BONUS.put("DUAS_MOEDAS", "2 moedas");
		BONUS.put("DOIS_YURTS", "2 yurts");
		BONUS.put("TRES_MOVIMENTOS_E_PUNHO", "3 movim. + punho");
		BONUS.put("DOIS_PUNHOS", "2 punhos");
		BONUS.put("TRES_TESOUROS", "3 tesouros");

		SIGLAS_MELHORIA.put("MOE", "Moeda virtual");
		SIGLAS_MELHORIA.put("ESP", "Espada virtual");
		SIGLAS_MELHORIA.put("CAS", "Yurt virtual");
		SIGLAS_MELHORIA.put("MAO", "Punho");
		SIGLAS_MELHORIA.put("CAV", "Dois cavalos");
		SIGLAS_MELHORIA.put("BOI", "Boi virtual");
		SIGLAS_MELHORIA.put("CUI", "Especiaria virtual");
		SIGLAS_MELHORIA.put("MOB", "Ferro virtual");
		SIGLAS_MELHORIA.put("MOP", "Pele virtual");
		SIGLAS_MELHORIA.put("PER", "Seda virtual");
		CORES_MELHORIA.put("A", "amarela");
		CORES_MELHORIA.put("R", "vermelha");
		CORES_MELHORIA.put("V", "verde");

		CONSELHEIROS.put("R04", "Norte da Rússia");
		CONSELHEIROS.put("R11", "Leste da Rússia");
		CONSELHEIROS.put("P01", "Oeste da Pérsia");
		CONSELHEIROS.put("P08", "Sul da Pérsia");
		CONSELHEIROS.put("C05", "Leste da China");
		CONSELHEIROS.put("C09", "Sul da China");
		CONSELHEIROS.put("KARAKORUM", "Karakorum");
	}

	private Textos() {
	}

	static String tesouro(String tipo) {
		return padrao(TESOUROS.get(tipo), tipo);
	}

	static String tributo(String tipo) {
		return padrao(TRIBUTOS.get(tipo), tipo);
	}

	static String bonus(String tipo) {
		return padrao(BONUS.get(tipo), tipo);
	}

	/** Nome de uma peça de melhoria pelo id (ex.: "CAV-V" → "Dois cavalos (verde)"). */
	static String melhoria(String id) {
		int traco = id.indexOf('-');
		if (traco < 0) {
			return id;
		}
		return padrao(SIGLAS_MELHORIA.get(id.substring(0, traco)), id) + " ("
				+ padrao(CORES_MELHORIA.get(id.substring(traco + 1)), "") + ")";
	}

	static String conselheiro(String idParada) {
		return padrao(CONSELHEIROS.get(idParada), idParada);
	}

	private static String padrao(String valor, String reserva) {
		return valor == null ? reserva : valor;
	}
}

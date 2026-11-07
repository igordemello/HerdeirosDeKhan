package view;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

/*
 * Onde desenhar as peças das ações no mapa, em pixels da imagem original do
 * tabuleiro (2905 x 2919), igual à CoordenadasParadas. Medimos na imagem:
 *  - províncias: o ícone de tributo impresso nela (as peças ficam em cima);
 *  - cidades: o meio do desenho da cidade (tesouros e yurt do conquistador);
 *  - Khan: a cabeça do Khan nas 4 províncias dele e o ícone na área de
 *    melhorias;
 *  - conselheiros: o quadrado da peça de bônus;
 *  - área de melhorias: o meio de cada um dos 5 espaços.
 * Na hora de desenhar é só multiplicar pela escala do drawImage().
 */
final class CoordenadasMapa {
	// tamanhos das peças, também em pixels da imagem original
	static final int LADO_TRIBUTO = 120; // tem que cobrir o desenho da espada, que é o maior
	static final int LADO_TESOURO = 92;
	static final int ESPACO_TESOURO = 8;
	static final int LADO_YURT = 82;
	static final int LADO_YURT_CIDADE = 130;
	static final int LADO_KHAN = 210;
	static final int LADO_BONUS = 168;
	static final int LADO_MELHORIA = 104;

	private static final Map<String, Point> PROVINCIAS = new HashMap<String, Point>();
	private static final Map<String, Point> CIDADES = new HashMap<String, Point>();
	private static final Map<String, Point> LUGARES_DO_KHAN = new HashMap<String, Point>();
	private static final Map<String, Point> CONSELHEIROS = new HashMap<String, Point>();
	private static final Point[] ESPACOS_MELHORIA = { new Point(298, 2805), new Point(560, 2805),
			new Point(823, 2805), new Point(1085, 2805), new Point(1355, 2805) };

	static {
		PROVINCIAS.put("PV01", new Point(621, 420));
		PROVINCIAS.put("PV02", new Point(860, 350));
		PROVINCIAS.put("PV03", new Point(798, 934));
		PROVINCIAS.put("PV04", new Point(1245, 919));
		PROVINCIAS.put("PV05", new Point(1961, 748));
		PROVINCIAS.put("PV06", new Point(2153, 1034));
		PROVINCIAS.put("PV07", new Point(524, 1206));
		PROVINCIAS.put("PV08", new Point(608, 1658));
		PROVINCIAS.put("PV09", new Point(1521, 1626));
		PROVINCIAS.put("PV10", new Point(2392, 1553));
		PROVINCIAS.put("PV11", new Point(2061, 1827));
		PROVINCIAS.put("PV12", new Point(1690, 1916));

		CIDADES.put("KIEV", new Point(290, 347));
		CIDADES.put("MOSCOU", new Point(1081, 597));
		CIDADES.put("SARAI", new Point(1739, 403));
		CIDADES.put("SAMARCANDA", new Point(806, 1355));
		CIDADES.put("BAGDA", new Point(266, 1674));
		CIDADES.put("CABUL", new Point(1258, 2013));
		CIDADES.put("KAESONG", new Point(2553, 823));
		CIDADES.put("PEQUIM", new Point(2070, 1430));
		CIDADES.put("CANTAO", new Point(2448, 2206));

		LUGARES_DO_KHAN.put("PV01", new Point(629, 597));
		LUGARES_DO_KHAN.put("PV05", new Point(1752, 903));
		LUGARES_DO_KHAN.put("PV08", new Point(802, 1731));
		LUGARES_DO_KHAN.put("PV11", new Point(2134, 2053));
		LUGARES_DO_KHAN.put("MELHORIAS", new Point(100, 2802));

		CONSELHEIROS.put("R04", new Point(1383, 161));
		CONSELHEIROS.put("R11", new Point(2418, 286));
		CONSELHEIROS.put("P01", new Point(231, 1026));
		CONSELHEIROS.put("P08", new Point(716, 2087));
		CONSELHEIROS.put("C05", new Point(2755, 1688));
		CONSELHEIROS.put("C09", new Point(1695, 2394));
		CONSELHEIROS.put("KARAKORUM", new Point(1440, 1388));
	}

	private CoordenadasMapa() {
	}

	static Point getProvincia(String id) {
		return PROVINCIAS.get(id);
	}

	static Point getCidade(String id) {
		return CIDADES.get(id);
	}

	static Point getLugarDoKhan(String lugar) {
		return LUGARES_DO_KHAN.get(lugar);
	}

	static Point getConselheiro(String idParada) {
		return CONSELHEIROS.get(idParada);
	}

	static Point getEspacoMelhoria(int espaco) {
		return ESPACOS_MELHORIA[espaco];
	}

	static int getNumeroDeEspacosMelhoria() {
		return ESPACOS_MELHORIA.length;
	}

	// Os tesouros ficam numa grade 2x2 no meio da cidade. Devolve o canto
	// de cima à esquerda do tesouro i (0 a 3).
	static Point getTesouro(String idCidade, int i) {
		Point c = CIDADES.get(idCidade);
		int passo = LADO_TESOURO + ESPACO_TESOURO;
		int x0 = c.x - passo + ESPACO_TESOURO / 2;
		int y0 = c.y - passo + ESPACO_TESOURO / 2;
		return new Point(x0 + (i % 2) * passo, y0 + (i / 2) * passo);
	}

	// ---- cliques (x e y já em pixels da imagem original)

	static String provinciaEm(double x, double y) {
		return maisPerto(PROVINCIAS, x, y, LADO_TRIBUTO * 0.75);
	}

	static String lugarDoKhanEm(double x, double y) {
		// um pouco maior que o desenho, senão clicar em cima do anel laranja não pegava
		// (não dá pra aumentar muito: em PV01 a cabeça fica perto do tributo e da R03)
		return maisPerto(LUGARES_DO_KHAN, x, y, LADO_KHAN / 2.0 + 10);
	}

	static String cidadeEm(double x, double y) {
		return maisPerto(CIDADES, x, y, LADO_TESOURO + ESPACO_TESOURO + 20);
	}

	/** Posição (0 a 3) do tesouro clicado na cidade, ou -1. */
	static int tesouroEm(String idCidade, double x, double y) {
		for (int i = 0; i < 4; i++) {
			Point p = getTesouro(idCidade, i);
			if (x >= p.x && x <= p.x + LADO_TESOURO && y >= p.y && y <= p.y + LADO_TESOURO) {
				return i;
			}
		}
		return -1;
	}

	private static String maisPerto(Map<String, Point> pontos, double x, double y, double raio) {
		String achado = null;
		double menor = Double.MAX_VALUE;
		for (Map.Entry<String, Point> e : pontos.entrySet()) {
			double d = e.getValue().distance(x, y);
			if (d <= raio && d < menor) {
				menor = d;
				achado = e.getKey();
			}
		}
		return achado;
	}
}

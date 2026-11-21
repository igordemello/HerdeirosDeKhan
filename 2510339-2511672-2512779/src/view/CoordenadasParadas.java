package view;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Posição (x, y), em pixels, do centro de cada parada na imagem do tabuleiro
 * principal (tabuleiro.jpg do EAD, 2905 x 2919 pixels). Os ids são os mesmos
 * do Model (R01..R11, P01..P11, C01..C11 e KARAKORUM).
 *
 * As coordenadas foram medidas na imagem original. Como a janela tem no
 * máximo 1400 x 1050, a imagem é desenhada reduzida: basta multiplicar as
 * coordenadas pela escala usada no {@code drawImage()}
 * ({@code larguraDesenhada / LARGURA_IMAGEM}).
 *
 * Usada para desenhar os peões e para saber em qual parada o jogador clicou.
 */
public final class CoordenadasParadas {
	public static final int LARGURA_IMAGEM = 2905;
	public static final int ALTURA_IMAGEM = 2919;

	/** Raio de clique, na imagem original, de uma parada simples. */
	private static final int RAIO_SIMPLES = 60;
	/** Uma parada dupla tem 2 círculos; o ponto guardado fica entre eles. */
	private static final int RAIO_DUPLA = 115;
	/** Karakorum é o brasão do centro (cerca de 470 x 490 pixels). */
	private static final int RAIO_KARAKORUM = 220;

	private static final Map<String, Point> COORDENADAS = new HashMap<String, Point>();
	private static final Set<String> DUPLAS = new HashSet<String>();

	static {
		// Rússia
		COORDENADAS.put("R01", new Point(126, 539));
		COORDENADAS.put("R02", new Point(489, 255));
		COORDENADAS.put("R03", new Point(468, 536));
		COORDENADAS.put("R04", new Point(890, 211));    // dupla: (852,172) e (927,250)
		COORDENADAS.put("R05", new Point(854, 554));
		COORDENADAS.put("R06", new Point(1274, 497));
		COORDENADAS.put("R07", new Point(1083, 845));
		COORDENADAS.put("R08", new Point(1558, 671));
		COORDENADAS.put("R09", new Point(1753, 228));
		COORDENADAS.put("R10", new Point(1891, 592));
		COORDENADAS.put("R11", new Point(2236, 496));   // dupla: (2182,493) e (2291,499)
		// Pérsia
		COORDENADAS.put("P01", new Point(462, 901));    // dupla: (471,846) e (453,956)
		COORDENADAS.put("P02", new Point(840, 1192));
		COORDENADAS.put("P03", new Point(571, 1411));
		COORDENADAS.put("P04", new Point(215, 1492));
		COORDENADAS.put("P05", new Point(958, 1555));
		COORDENADAS.put("P06", new Point(451, 1644));
		COORDENADAS.put("P07", new Point(179, 1913));
		COORDENADAS.put("P08", new Point(726, 1887));   // dupla: (672,1877) e (780,1897)
		COORDENADAS.put("P09", new Point(1273, 1821));
		COORDENADAS.put("P10", new Point(1474, 2057));
		COORDENADAS.put("P11", new Point(1213, 2237));
		// China
		COORDENADAS.put("C01", new Point(2497, 659));
		COORDENADAS.put("C02", new Point(2773, 886));
		COORDENADAS.put("C03", new Point(2525, 1075));
		COORDENADAS.put("C04", new Point(2141, 1255));
		COORDENADAS.put("C05", new Point(2556, 1555));  // dupla: (2556,1500) e (2556,1610)
		COORDENADAS.put("C06", new Point(1891, 1607));
		COORDENADAS.put("C07", new Point(2171, 1678));
		COORDENADAS.put("C08", new Point(2525, 2031));
		COORDENADAS.put("C09", new Point(1830, 2223));  // dupla: (1774,2211) e (1887,2235)
		COORDENADAS.put("C10", new Point(2235, 2231));
		COORDENADAS.put("C11", new Point(2589, 2410));
		// capital
		COORDENADAS.put("KARAKORUM", new Point(1437, 1292));

		DUPLAS.add("R04");
		DUPLAS.add("R11");
		DUPLAS.add("P01");
		DUPLAS.add("P08");
		DUPLAS.add("C05");
		DUPLAS.add("C09");
	}

	private CoordenadasParadas() {
	}

	/** Cópia da tabela id -> ponto, em pixels da imagem original. */
	public static Map<String, Point> getCoordenadas() {
		Map<String, Point> copia = new HashMap<String, Point>();
		for (Map.Entry<String, Point> e : COORDENADAS.entrySet()) {
			copia.put(e.getKey(), new Point(e.getValue()));
		}
		return copia;
	}

	/** Centro da parada na imagem desenhada com a escala dada (ou {@code null}). */
	public static Point getPonto(String idParada, double escala) {
		Point p = COORDENADAS.get(idParada);
		if (p == null) {
			return null;
		}
		return new Point((int) Math.round(p.x * escala), (int) Math.round(p.y * escala));
	}

	/**
	 * Parada clicada. As coordenadas do clique devem ser relativas ao canto
	 * superior esquerdo da imagem desenhada.
	 *
	 * @param escala larguraDesenhada / LARGURA_IMAGEM
	 * @return o id da parada, ou {@code null} se o clique não foi numa parada
	 */
	public static String paradaEm(int x, int y, double escala) {
		double xImagem = x / escala;
		double yImagem = y / escala;
		String maisPerto = null;
		double menorDistancia = Double.MAX_VALUE;
		for (Map.Entry<String, Point> e : COORDENADAS.entrySet()) {
			double distancia = e.getValue().distance(xImagem, yImagem);
			if (distancia <= raio(e.getKey()) && distancia < menorDistancia) {
				menorDistancia = distancia;
				maisPerto = e.getKey();
			}
		}
		return maisPerto;
	}

	private static int raio(String idParada) {
		if ("KARAKORUM".equals(idParada)) {
			return RAIO_KARAKORUM;
		}
		return DUPLAS.contains(idParada) ? RAIO_DUPLA : RAIO_SIMPLES;
	}
}

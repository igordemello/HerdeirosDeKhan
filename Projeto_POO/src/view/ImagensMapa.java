package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/*
 * Imagens das peças do mapa que não vieram nos zips do EAD:
 *  - yurt de cada jogador: desenhamos com Java2D numa BufferedImage da cor
 *    do jogador e guardamos num HashMap (cria uma vez só por cor);
 *  - peão do Khan: imagens/pecas/KHAN.png, que recortamos da cabeça do Khan
 *    impressa no próprio tabuleiro.
 * O resto (tributos, tesouros, bônus, melhorias) usa as imagens do EAD que
 * já são carregadas pela ImagensTabuleiroJogador.
 */
final class ImagensMapa {
	static final String PASTA_PECAS = "imagens/pecas/";
	private static final int LADO_IMAGEM_YURT = 128;

	// mesmas cores do fundo da área lateral
	private static final Map<String, Color> CORES = new HashMap<String, Color>();
	static {
		CORES.put("AZUL", new Color(36, 92, 196));
		CORES.put("VERMELHO", new Color(196, 38, 38));
		CORES.put("VERDE", new Color(34, 140, 64));
		CORES.put("AMARELO", new Color(236, 196, 40));
		CORES.put("ROXO", new Color(118, 58, 168));
	}

	private static final Map<String, Image> YURTS = new HashMap<String, Image>();
	private static Image khan;
	private static boolean khanProcurado = false;

	private ImagensMapa() {
	}

	static Color corDoJogador(String cor) {
		Color c = CORES.get(cor);
		return c == null ? Color.GRAY : c;
	}

	static Image getYurt(String cor) {
		Image img = YURTS.get(cor);
		if (img == null) {
			img = criarYurt(corDoJogador(cor));
			YURTS.put(cor, img);
		}
		return img;
	}

	/** null se não achar o arquivo. */
	static Image getKhan() {
		if (!khanProcurado) {
			khan = ImagensTabuleiroJogador.carregarArquivo(PASTA_PECAS, "KHAN");
			khanProcurado = true;
		}
		return khan;
	}

	// desenha um yurt simples: paredes, teto redondo e a porta
	private static Image criarYurt(Color cor) {
		int l = LADO_IMAGEM_YURT;
		BufferedImage img = new BufferedImage(l, l, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Color escura = ImagensTabuleiroJogador.misturar(cor, Color.BLACK, 0.45);
		Color clara = ImagensTabuleiroJogador.misturar(cor, Color.WHITE, 0.35);

		// sombra no chão
		g.setColor(new Color(0, 0, 0, 90));
		g.fill(new Ellipse2D.Double(l * 0.10, l * 0.80, l * 0.84, l * 0.16));

		RoundRectangle2D paredes = new RoundRectangle2D.Double(l * 0.14, l * 0.46, l * 0.72, l * 0.42, 18, 18);
		g.setPaint(new GradientPaint(0, (float) (l * 0.46), cor, 0, (float) (l * 0.88), escura));
		g.fill(paredes);

		GeneralPath teto = new GeneralPath();
		teto.moveTo(l * 0.08, l * 0.52);
		teto.quadTo(l * 0.20, l * 0.16, l * 0.50, l * 0.10);
		teto.quadTo(l * 0.80, l * 0.16, l * 0.92, l * 0.52);
		teto.closePath();
		g.setPaint(new GradientPaint(0, (float) (l * 0.10), clara, 0, (float) (l * 0.52), cor));
		g.fill(teto);

		g.setColor(escura);
		g.fill(new RoundRectangle2D.Double(l * 0.10, l * 0.48, l * 0.80, l * 0.07, 6, 6)); // faixa
		g.fill(new Ellipse2D.Double(l * 0.43, l * 0.06, l * 0.14, l * 0.08)); // topo

		g.setColor(new Color(50, 30, 15));
		g.fill(new RoundRectangle2D.Double(l * 0.41, l * 0.60, l * 0.18, l * 0.28, 6, 6)); // porta

		g.setStroke(new BasicStroke(3.5f));
		g.setColor(new Color(25, 20, 20));
		g.draw(teto);
		g.draw(paredes);
		g.dispose();
		return img;
	}
}

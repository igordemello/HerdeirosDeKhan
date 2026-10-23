package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Imagens dos tabuleiros de jogador, uma por herdeiro, guardadas num HashMap
 * para que a View as recupere em tempo constante.
 *
 * A classe procura primeiro as imagens disponibilizadas no EAD, na pasta
 * {@code imagens/tabuleiros/} do projeto, com o nome do herdeiro em minúsculas
 * (ex.: {@code imagens/tabuleiros/tolui.png}). Se não encontrar, monta uma
 * imagem provisória do tabuleiro com Java2D, para que o jogo funcione
 * mesmo sem os arquivos. Em ambos os casos a View exibe a imagem com
 * {@code drawImage()}.
 *
 * As constantes de geometria dizem onde ficam as colunas dentro da imagem,
 * em frações da largura e da altura, e valem para os dois casos. Se as
 * imagens do EAD tiverem outro enquadramento, basta ajustá-las.
 */
final class ImagensTabuleiroJogador {
	/** Pasta das imagens, relativa à pasta do projeto no Eclipse. */
	static final String PASTA = "imagens/tabuleiros/";
	private static final String[] EXTENSOES = { ".png", ".jpg", ".jpeg", ".gif" };

	/** Proporção largura/altura do tabuleiro do jogador. */
	static final double PROPORCAO = 1510.0 / 820.0;

	/** Início (x) de cada coluna, em fração da largura da imagem. */
	static final double[] X_COLUNA = { 0.268, 0.440, 0.613, 0.805 };
	/** Largura de uma coluna, em fração da largura da imagem. */
	static final double LARGURA_COLUNA = 0.149;
	/** Topo (y) dos espaços de melhoria, de baixo (índice 0) para cima. */
	static final double[] Y_ESPACO = { 0.580, 0.415, 0.250, 0.091 };
	/** Topo (y) da linha de ações iniciais (base das colunas). */
	static final double Y_BASE = 0.744;
	/** Altura de uma linha, em fração da altura da imagem. */
	static final double ALTURA_LINHA = 0.14;

	/** Cor de fundo de cada coluna na imagem provisória. */
	private static final Color[] FUNDO_COLUNA = { new Color(245, 214, 92), new Color(120, 190, 110),
			new Color(214, 92, 80), new Color(245, 245, 245) };

	private static final Map<String, Image> CACHE = new HashMap<String, Image>();

	private ImagensTabuleiroJogador() {
	}

	/**
	 * Imagem do tabuleiro do herdeiro dado.
	 *
	 * @param herdeiro  nome do herdeiro, como devolvido por {@code Jogo.getHerdeiro}
	 * @param descricoesBase texto das ações iniciais de cada coluna (só para a imagem provisória)
	 */
	static Image getImagem(String herdeiro, String[] descricoesBase) {
		Image img = CACHE.get(herdeiro);
		if (img == null) {
			img = carregarArquivo(herdeiro);
			if (img == null) {
				img = criarImagemProvisoria(herdeiro, descricoesBase);
			}
			CACHE.put(herdeiro, img);
		}
		return img;
	}

	/** Se a imagem do herdeiro veio de um arquivo (e não é a provisória). */
	static boolean temArquivo(String herdeiro) {
		return carregarArquivo(herdeiro) != null;
	}

	private static Image carregarArquivo(String herdeiro) {
		String base = herdeiro.toLowerCase();
		for (String ext : EXTENSOES) {
			try {
				File arquivo = new File(PASTA + base + ext);
				if (arquivo.isFile()) {
					return ImageIO.read(arquivo);
				}
				URL recurso = ImagensTabuleiroJogador.class.getResource("/" + PASTA + base + ext);
				if (recurso != null) {
					return ImageIO.read(recurso);
				}
			} catch (IOException e) {
				System.err.println("Não foi possível ler a imagem de " + herdeiro + ": " + e.getMessage());
			}
		}
		return null;
	}

	/** Desenha, com Java2D, uma imagem provisória do tabuleiro do jogador. */
	private static Image criarImagemProvisoria(String herdeiro, String[] descricoesBase) {
		int w = 920;
		int h = (int) Math.round(w / PROPORCAO);
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// moldura e fundo
		g.setPaint(new GradientPaint(0, 0, new Color(28, 70, 120), 0, h, new Color(16, 42, 78)));
		g.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 28, 28));
		g.setColor(new Color(200, 225, 245));
		g.setStroke(new BasicStroke(3f));
		g.draw(new RoundRectangle2D.Double(4, 4, w - 9, h - 9, 24, 24));

		// área do herdeiro
		g.setFont(new Font(Font.SERIF, Font.BOLD | Font.ITALIC, 34));
		g.setColor(Color.WHITE);
		g.drawString(herdeiro, 26, (int) (h * 0.88));
		g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
		g.setColor(new Color(200, 225, 245));
		g.drawString("Herdeiro", 28, (int) (h * 0.12));

		g.setStroke(new BasicStroke(2f));
		for (int c = 0; c < X_COLUNA.length; c++) {
			double x = X_COLUNA[c] * w;
			double cw = LARGURA_COLUNA * w;
			double lh = ALTURA_LINHA * h;
			Color fundo = FUNDO_COLUNA[c];

			// espaços de melhoria (com a seta para cima)
			for (double yFrac : Y_ESPACO) {
				double y = yFrac * h;
				g.setColor(misturar(fundo, Color.WHITE, 0.55));
				g.fill(new RoundRectangle2D.Double(x, y, cw, lh, 10, 10));
				g.setColor(misturar(fundo, Color.BLACK, 0.25));
				g.draw(new RoundRectangle2D.Double(x, y, cw, lh, 10, 10));
				desenharSeta(g, x + cw / 2, y + lh * 0.2, lh * 0.6);
			}

			// base com as ações iniciais
			double y = Y_BASE * h;
			g.setColor(fundo);
			g.fill(new RoundRectangle2D.Double(x, y, cw, lh, 10, 10));
			g.setColor(misturar(fundo, Color.BLACK, 0.4));
			g.draw(new RoundRectangle2D.Double(x, y, cw, lh, 10, 10));
			if (descricoesBase != null && c < descricoesBase.length) {
				desenharTextoCentralizado(g, descricoesBase[c], x, y, cw, lh, Color.BLACK);
			}
		}
		g.dispose();
		return img;
	}

	private static void desenharSeta(Graphics2D g, double cx, double topo, double altura) {
		int[] xs = { (int) cx, (int) (cx + altura * 0.35), (int) (cx + altura * 0.12), (int) (cx + altura * 0.12),
				(int) (cx - altura * 0.12), (int) (cx - altura * 0.12), (int) (cx - altura * 0.35) };
		int[] ys = { (int) topo, (int) (topo + altura * 0.45), (int) (topo + altura * 0.45), (int) (topo + altura),
				(int) (topo + altura), (int) (topo + altura * 0.45), (int) (topo + altura * 0.45) };
		g.setColor(new Color(255, 255, 255, 170));
		g.fillPolygon(xs, ys, xs.length);
	}

	/** Escreve um texto de várias linhas (separadas por '\n') centralizado no retângulo. */
	static void desenharTextoCentralizado(Graphics2D g, String texto, double x, double y, double w, double h,
			Color cor) {
		String[] linhas = texto.split("\n");
		int tamanho = (int) Math.max(7, Math.min(26, h / (linhas.length + 0.4)));
		g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamanho));
		FontMetrics fm = g.getFontMetrics();
		while (tamanho > 7 && maiorLargura(fm, linhas) > w * 0.92) { // diminui até caber na largura
			tamanho--;
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, tamanho));
			fm = g.getFontMetrics();
		}
		int alturaTotal = fm.getHeight() * linhas.length;
		int yTexto = (int) (y + (h - alturaTotal) / 2 + fm.getAscent());
		g.setColor(cor);
		for (String linha : linhas) {
			int largura = fm.stringWidth(linha);
			g.drawString(linha, (int) (x + (w - largura) / 2), yTexto);
			yTexto += fm.getHeight();
		}
	}

	private static int maiorLargura(FontMetrics fm, String[] linhas) {
		int maior = 0;
		for (String linha : linhas) {
			maior = Math.max(maior, fm.stringWidth(linha));
		}
		return maior;
	}

	static Color misturar(Color a, Color b, double t) {
		return new Color((int) (a.getRed() * (1 - t) + b.getRed() * t),
				(int) (a.getGreen() * (1 - t) + b.getGreen() * t),
				(int) (a.getBlue() * (1 - t) + b.getBlue() * t));
	}
}

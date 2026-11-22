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

/*
 * Carrega as imagens (as do EAD) e guarda em HashMaps, pra View pegar em
 * tempo constante pelo nome que vem do Model:
 *   imagens/tabuleiros/tolui.jpg   tabuleiro de cada herdeiro
 *   imagens/fichas/azul.jpg        ficha de ativação de cada cor
 *   imagens/tributos/, tesouros/, bonus/   pelo nome do tipo (ex.: SEDA.jpg)
 *   imagens/melhorias/CAV-V.jpg    pelo id da peça
 *
 * Se faltar o tabuleiro de um herdeiro, desenhamos um quebra-galho com
 * Java2D. Se faltar outra imagem, devolve null e quem desenha põe uma forma
 * simples no lugar. Tudo aparece na tela com drawImage().
 *
 * As constantes de geometria são a posição das colunas dentro da cartela,
 * em fração da largura/altura.
 */
final class ImagensTabuleiroJogador {
	// pastas relativas à pasta do projeto no Eclipse
	static final String PASTA = "imagens/tabuleiros/";
	static final String PASTA_FICHAS = "imagens/fichas/";
	static final String PASTA_MELHORIAS = "imagens/melhorias/";
	static final String PASTA_TRIBUTOS = "imagens/tributos/";
	static final String PASTA_TESOUROS = "imagens/tesouros/";
	static final String PASTA_BONUS = "imagens/bonus/";
	static final String PASTA_CARTAS = "imagens/cartas/";
	private static final String[] EXTENSOES = { ".png", ".jpg", ".jpeg", ".gif" };

	// Proporção largura/altura do tabuleiro do jogador.
	static final double PROPORCAO = 1600.0 / 873.0;

	// Início (x) de cada coluna, em fração da largura da imagem.
	static final double[] X_COLUNA = { 0.2625, 0.439, 0.616, 0.811 };
	// Largura de uma coluna, em fração da largura da imagem.
	static final double LARGURA_COLUNA = 0.150;
	// Topo (y) dos espaços de melhoria, de baixo (índice 0) para cima.
	static final double[] Y_ESPACO = { 0.590, 0.422, 0.258, 0.094 };
	// Topo (y) da linha de ações iniciais (base das colunas).
	static final double Y_BASE = 0.759;
	// Altura de uma linha, em fração da altura da imagem.
	static final double ALTURA_LINHA = 0.147;

	// cores das colunas no tabuleiro quebra-galho (vermelha, verde, amarela, branca)
	private static final Color[] FUNDO_COLUNA = { new Color(232, 112, 92), new Color(150, 196, 96),
			new Color(240, 206, 70), new Color(245, 245, 245) };

	private static final Map<String, Image> CACHE = new HashMap<String, Image>();
	private static final Map<String, Image> CACHE_FICHAS = new HashMap<String, Image>();
	private static final Map<String, Image> CACHE_MELHORIAS = new HashMap<String, Image>();
	private static final Map<String, Image> CACHE_TRIBUTOS = new HashMap<String, Image>();
	private static final Map<String, Image> CACHE_TESOUROS = new HashMap<String, Image>();
	private static final Map<String, Image> CACHE_BONUS = new HashMap<String, Image>();
	private static final Map<String, Image> CACHE_CARTAS = new HashMap<String, Image>();
	// marca no cache o que não foi achado, pra não procurar no disco toda hora
	private static final Image AUSENTE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

	private ImagensTabuleiroJogador() {
	}

	// Tabuleiro do herdeiro (nome como vem do Jogo.getHerdeiro). descricoesBase
	// só é usado se precisar desenhar o quebra-galho.
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

	// ficha da cor ("AZUL", ...) ou null
	static Image getFicha(String cor) {
		return buscar(CACHE_FICHAS, PASTA_FICHAS, cor.toLowerCase());
	}

	// melhoria pelo id ("CAV-V", ...) ou null
	static Image getMelhoria(String id) {
		return buscar(CACHE_MELHORIAS, PASTA_MELHORIAS, id);
	}

	static Image getTributo(String tipo) {
		return buscar(CACHE_TRIBUTOS, PASTA_TRIBUTOS, tipo);
	}

	static Image getTesouro(String tipo) {
		return buscar(CACHE_TESOUROS, PASTA_TESOUROS, tipo);
	}

	static Image getCarta(String tipo) {
		return buscar(CACHE_CARTAS, PASTA_CARTAS, tipo);
	}

	static Image getBonus(String tipo) {
		return buscar(CACHE_BONUS, PASTA_BONUS, tipo);
	}

	private static Image buscar(Map<String, Image> cache, String pasta, String nome) {
		Image img = cache.get(nome);
		if (img == null) {
			img = carregarArquivo(pasta, nome);
			cache.put(nome, img == null ? AUSENTE : img);
		}
		return img == AUSENTE ? null : img;
	}

	private static Image carregarArquivo(String herdeiro) {
		return carregarArquivo(PASTA, herdeiro.toLowerCase());
	}

	// procura no disco e, se não achar, no classpath (pra funcionar também num .jar)
	static Image carregarArquivo(String pasta, String nome) {
		for (String ext : EXTENSOES) {
			try {
				File arquivo = new File(pasta + nome + ext);
				if (arquivo.isFile()) {
					return ImageIO.read(arquivo);
				}
				URL recurso = ImagensTabuleiroJogador.class.getResource("/" + pasta + nome + ext);
				if (recurso != null) {
					return ImageIO.read(recurso);
				}
			} catch (IOException e) {
				System.err.println("Não foi possível ler a imagem " + pasta + nome + ext + ": " + e.getMessage());
			}
		}
		return null;
	}

	// quebra-galho com Java2D quando a cartela não foi achada
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

	// texto com '\n' centralizado no retângulo
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

package view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * Carta de conselheiro desenhada com Java2D (os arquivos do EAD não trazem
 * imagens das cartas). A carta secreta mostra os 3 limiares de votos; a
 * aberta mostra em destaque o número exigido.
 */
class ComponenteCarta extends JComponent {
	private static final long serialVersionUID = 1L;

	static final int LARGURA = 168;
	static final int ALTURA = 228;

	private static final Color PAPEL = new Color(246, 236, 210);
	private static final Color BORDA = new Color(140, 104, 52);
	private static final Color TINTA = new Color(52, 38, 22);
	private static final Color CABECALHO_SECRETO = new Color(64, 42, 110);
	private static final Color CABECALHO_ABERTO = new Color(22, 98, 104);

	private final boolean secreta;
	private final String titulo;
	private final String descricao;
	private final String destaque;
	private final List<String> linhas;
	private final String rodape;

	/** Carta secreta: título, condição, limiares e (opcional) progresso. */
	static ComponenteCarta secreta(String titulo, String descricao, List<Integer> limiares, String rodape) {
		List<String> linhas = new ArrayList<String>();
		for (int i = 0; i < limiares.size(); i++) {
			linhas.add(limiares.get(i) + "  →  " + (i + 1) + (i == 0 ? " voto" : " votos"));
		}
		return new ComponenteCarta(true, titulo, descricao, null, linhas, rodape);
	}

	/** Carta aberta: número exigido em destaque e a condição. */
	static ComponenteCarta aberta(int quantidade, String descricao, String rodape) {
		return new ComponenteCarta(false, "Conselheiro aberto", descricao, String.valueOf(quantidade),
				new ArrayList<String>(), rodape);
	}

	private ComponenteCarta(boolean secreta, String titulo, String descricao, String destaque, List<String> linhas,
			String rodape) {
		this.secreta = secreta;
		this.titulo = titulo;
		this.descricao = descricao;
		this.destaque = destaque;
		this.linhas = linhas;
		this.rodape = rodape;
		setPreferredSize(new Dimension(LARGURA, ALTURA));
		setToolTipText(descricao);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		int w = getWidth() - 4;
		int h = getHeight() - 4;

		g2.setColor(new Color(0, 0, 0, 50));
		g2.fill(new RoundRectangle2D.Double(4, 4, w, h, 16, 16));
		g2.setPaint(new GradientPaint(0, 0, PAPEL, 0, h, PAPEL.darker()));
		g2.fill(new RoundRectangle2D.Double(0, 0, w, h, 16, 16));
		g2.setColor(BORDA);
		g2.setStroke(new BasicStroke(2f));
		g2.draw(new RoundRectangle2D.Double(1, 1, w - 2, h - 2, 16, 16));

		// faixa do título
		Color cabecalho = secreta ? CABECALHO_SECRETO : CABECALHO_ABERTO;
		g2.setColor(cabecalho);
		g2.fill(new RoundRectangle2D.Double(8, 8, w - 16, 30, 10, 10));
		int tamanho = 16;
		g2.setFont(new Font(Font.SERIF, Font.BOLD | Font.ITALIC, tamanho));
		FontMetrics fm = g2.getFontMetrics();
		while (fm.stringWidth(titulo) > w - 28 && tamanho > 9) { // diminui até caber na faixa
			g2.setFont(new Font(Font.SERIF, Font.BOLD | Font.ITALIC, --tamanho));
			fm = g2.getFontMetrics();
		}
		g2.setColor(Color.WHITE);
		g2.drawString(titulo, (w - fm.stringWidth(titulo)) / 2, 8 + (30 - fm.getHeight()) / 2 + fm.getAscent());

		int y = 50;
		if (destaque != null) {
			g2.setFont(new Font(Font.SERIF, Font.BOLD, 54));
			fm = g2.getFontMetrics();
			g2.setColor(cabecalho);
			g2.drawString(destaque, (w - fm.stringWidth(destaque)) / 2, y + fm.getAscent() - 6);
			y += fm.getHeight() - 4;
		}

		g2.setColor(TINTA);
		g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		y = escreverQuebrando(g2, descricao, 12, y, w - 24);

		if (!linhas.isEmpty()) {
			y += 8;
			g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
			fm = g2.getFontMetrics();
			for (String linha : linhas) {
				y += fm.getHeight();
				g2.drawString(linha, (w - fm.stringWidth(linha)) / 2, y);
			}
		}

		if (rodape != null) {
			g2.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
			fm = g2.getFontMetrics();
			g2.setColor(cabecalho);
			int yr = h - 10 - fm.getDescent();
			List<String> partes = quebrar(rodape, fm, w - 20);
			yr -= (partes.size() - 1) * fm.getHeight();
			for (String p : partes) {
				g2.drawString(p, (w - fm.stringWidth(p)) / 2, yr);
				yr += fm.getHeight();
			}
		}
		g2.dispose();
	}

	private static int escreverQuebrando(Graphics2D g2, String texto, int x, int y, int largura) {
		FontMetrics fm = g2.getFontMetrics();
		for (String linha : quebrar(texto, fm, largura)) {
			y += fm.getHeight();
			g2.drawString(linha, x + (largura - fm.stringWidth(linha)) / 2, y);
		}
		return y;
	}

	private static List<String> quebrar(String texto, FontMetrics fm, int largura) {
		List<String> linhas = new ArrayList<String>();
		StringBuilder atual = new StringBuilder();
		for (String palavra : texto.split(" ")) {
			String tentativa = atual.length() == 0 ? palavra : atual + " " + palavra;
			if (fm.stringWidth(tentativa) > largura && atual.length() > 0) {
				linhas.add(atual.toString());
				atual = new StringBuilder(palavra);
			} else {
				atual = new StringBuilder(tentativa);
			}
		}
		if (atual.length() > 0) {
			linhas.add(atual.toString());
		}
		return linhas;
	}
}

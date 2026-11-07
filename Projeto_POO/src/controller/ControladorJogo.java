package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import model.Jogo;
import view.JanelaPrincipal;
import view.PainelTabuleiro;

/*
 * Controla a jogada.
 *
 * O jogador ativa uma coluna na área lateral e depois, na ordem que quiser,
 * clica no tabuleiro:
 *  - numa parada: move o peão
 *  - num tributo: pega a peça
 *  - num tesouro: ataca a cidade
 *  - num lugar do Khan: coloca o Khan
 *  - botão direito numa parada: constrói yurt
 *  - botão direito num tesouro: habilidade do Ogedei
 * O botão "Passar a vez" termina o turno (fica travado enquanto o Khan for
 * obrigatório e não tiver sido usado).
 *
 * Quem diz se a ação vale é o Model; aqui só chamamos e mostramos o
 * resultado na área lateral. Os tratadores de evento são classes internas
 * porque o enunciado proíbe lambda nos eventos.
 */
public class ControladorJogo {
	private final Jogo jogo;
	private final JanelaPrincipal janela;

	public ControladorJogo(Jogo jogo, JanelaPrincipal janela) {
		this.jogo = jogo;
		this.janela = janela;
		janela.getPainelTabuleiro().addMouseListener(new TratadorCliqueNoTabuleiro());
		janela.getBotaoPassarVez().addActionListener(new TratadorPassarVez());
		janela.getBotaoHabilidade().addActionListener(new TratadorHabilidade());
		janela.getPainelLateral().addPropertyChangeListener("movimentos", new TratadorMovimentos());
	}

	// ------------------------------------------------------- ações no mapa

	// A ordem dos testes segue o que está desenhado por cima: Khan (só se
	// estiver pendente), tesouro, tributo e por último a parada.
	void cliqueEsquerdo(int x, int y) {
		PainelTabuleiro painel = janela.getPainelTabuleiro();
		String lugar = painel.lugarDoKhanEm(x, y);
		if (lugar != null && jogo.khanPendente()) {
			colocarKhan(lugar);
			return;
		}
		String cidade = painel.cidadeEm(x, y);
		if (cidade != null) {
			String tesouro = painel.tesouroEm(cidade, x, y);
			if (tesouro != null) {
				atacar(cidade, tesouro);
				return;
			}
		}
		String provincia = painel.provinciaEm(x, y);
		if (provincia != null) {
			pegarTributo(provincia);
			return;
		}
		String parada = painel.paradaEm(x, y);
		if (parada != null) {
			paradaClicada(parada);
		}
	}

	void cliqueDireito(int x, int y) {
		PainelTabuleiro painel = janela.getPainelTabuleiro();
		String cidade = painel.cidadeEm(x, y);
		String tesouro = cidade == null ? null : painel.tesouroEm(cidade, x, y);
		if (tesouro != null) {
			if (jogo.pegarTesouroComMoeda(cidade, tesouro)) {
				janela.mostrarMensagem("Ogedei pagou 1 moeda por " + tesouro.toLowerCase() + " de "
						+ jogo.getNomeDaCidade(cidade) + ".");
			} else {
				janela.mostrarMensagem("Pegar tesouro com moeda: só Ogedei, 1x por turno, não o último.");
			}
			janela.atualizar();
			return;
		}
		String parada = painel.paradaEm(x, y);
		if (parada != null) {
			construirYurt(parada);
		}
	}

	void trocarCavaloPorEspada() {
		if (jogo.trocarCavaloPorEspada()) {
			janela.mostrarMensagem("Chagatai trocou 1 cavalo por 1 espada.");
		} else {
			janela.mostrarMensagem("Troca só 1x por turno e com um movimento sobrando.");
		}
		janela.atualizar();
	}

	// Move o peão até a parada (na Altani tenta o peão 1 e depois o 2). Se não
	// der pra mover, por exemplo clicando onde o peão já está, tenta construir
	// um yurt ali. Mover não passa mais a vez.
	void paradaClicada(String idParada) {
		String cor = jogo.getJogadorDaVez();
		for (int n = 1; n <= jogo.getNumeroDePeoes(cor); n++) {
			if (!jogo.moverPeaoDaVezAte(n, idParada).isEmpty()) {
				janela.mostrarMensagem("Peão movido para " + idParada + ".");
				janela.atualizar();
				return;
			}
		}
		if (jogo.podeConstruirYurt(idParada)) {
			construirYurt(idParada);
		}
	}

	private void pegarTributo(String provincia) {
		String tipo = jogo.getTipoTributoDaProvincia(provincia).toLowerCase();
		if (jogo.pegarTributo(provincia)) {
			janela.mostrarMensagem("Pegou 1 " + tipo + " de " + provincia + ".");
		} else if (jogo.getPunhosDaVez() == 0) {
			janela.mostrarMensagem("Sem punhos para pegar tributo.");
		} else {
			janela.mostrarMensagem(provincia + " vazia ou longe das paradas do turno.");
		}
		janela.atualizar();
	}

	private void construirYurt(String parada) {
		if (jogo.construirYurt(parada)) {
			janela.mostrarMensagem("Yurt construído em " + parada + ".");
		} else {
			janela.mostrarMensagem("Não dá para construir yurt em " + parada + ".");
		}
		janela.atualizar();
	}

	private void atacar(String cidade, String tesouro) {
		int custo = jogo.getCustoDeAtaque(cidade);
		if (jogo.atacarCidade(cidade, tesouro)) {
			String texto = "Tomou " + tesouro.toLowerCase() + " de " + jogo.getNomeDaCidade(cidade) + " ("
					+ custo + (custo == 1 ? " espada)." : " espadas).");
			if ("CONQUISTADA".equals(jogo.getEstadoDaCidade(cidade))) {
				texto += " Cidade conquistada!";
			}
			janela.mostrarMensagem(texto);
		} else {
			janela.mostrarMensagem("Ataque recusado: precisa de " + custo + (custo == 1 ? " espada" : " espadas")
					+ " e de parada vizinha.");
		}
		janela.atualizar();
	}

	private void colocarKhan(String lugar) {
		if (jogo.colocarKhan(lugar)) {
			janela.mostrarMensagem(Jogo.KHAN_NA_AREA_DE_MELHORIAS.equals(lugar)
					? "Khan na área de melhorias: novas melhorias reveladas."
					: "Khan em " + lugar + ": tributos acrescentados.");
		} else {
			janela.mostrarMensagem("O Khan não pode ficar onde já está.");
		}
		janela.atualizar();
	}

	// ----------------------------------------------------------------- vez

	void passarVez() {
		if (jogo.khanPendente()) {
			janela.mostrarMensagem("Use o Khan antes de passar a vez.");
			janela.atualizar();
			return;
		}
		jogo.passarVez();
		janela.mostrarMensagem("");
		janela.atualizar();
	}

	// ------------------------------------------------- tratadores de eventos

	private class TratadorCliqueNoTabuleiro extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isRightMouseButton(e)) {
				cliqueDireito(e.getX(), e.getY());
			} else if (SwingUtilities.isLeftMouseButton(e)) {
				cliqueEsquerdo(e.getX(), e.getY());
			}
		}
	}

	private class TratadorPassarVez implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			passarVez();
		}
	}

	private class TratadorHabilidade implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			trocarCavaloPorEspada();
		}
	}

	// a área lateral avisa quando os movimentos mudam (coluna, bônus ou combo de teste)
	private class TratadorMovimentos implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			janela.atualizar();
		}
	}

	/** Abre o jogo com uma partida de 5 jogadores (as janelas iniciais ficam para depois). */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Jogo jogo = new Jogo(Arrays.asList("VERMELHO", "AZUL", "AMARELO", "VERDE", "ROXO"));
				JanelaPrincipal janela = new JanelaPrincipal(jogo);
				new ControladorJogo(jogo, janela);
				janela.setVisible(true);
			}
		});
	}
}

package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.SwingUtilities;

import model.Jogo;
import view.JanelaFimDeJogo;
import view.JanelaPrincipal;
import view.PainelTabuleiro;
import view.SeletorPartida;

/*
 * Controla a sequência de uma partida.
 *
 * Antes da primeira rodada, cada jogador escolhe o seu conselheiro secreto.
 * No turno, o jogador ativa uma coluna na área lateral e depois, na ordem que
 * quiser, clica no tabuleiro:
 *  - numa parada: move o peão
 *  - num tributo: pega a peça
 *  - num tesouro: ataca a cidade
 *  - num lugar do Khan: coloca o Khan
 *  - botão direito numa parada: constrói yurt
 *  - botão direito num tesouro: habilidade do Ogedei
 * O botão "Passar a vez" termina o turno, mas só depois de o jogador ativar
 * uma coluna e de usar o Khan, quando a coluna obrigar.
 *
 * Quando o fim do jogo é disparado (pág. 14), a rodada atual termina, todos
 * jogam mais uma vez e a partida acaba com a janela de resultado, que
 * pergunta se jogam de novo ou fecham o programa.
 *
 * O botão "Salvar" grava a partida num arquivo, mas só no começo do turno,
 * antes de o jogador da vez fazer qualquer jogada (4ª iteração).
 *
 * Quem diz se a ação vale é o Model; aqui só chamamos e mostramos uma
 * mensagem. A tela não é redesenhada aqui: o Model avisa os observadores
 * (padrão Observer). Os tratadores de evento são classes internas porque o
 * enunciado proíbe lambda nos eventos.
 */
public class ControladorJogo {
	private final Jogo jogo = Jogo.getInstancia();
	private final JanelaPrincipal janela;

	public ControladorJogo(JanelaPrincipal janela) {
		this.janela = janela;
		janela.getPainelTabuleiro().addMouseListener(new TratadorCliqueNoTabuleiro());
		janela.getBotaoPassarVez().addActionListener(new TratadorPassarVez());
		janela.getBotaoHabilidade().addActionListener(new TratadorHabilidade());
		janela.getBotaoSalvar().addActionListener(new TratadorSalvar());
	}

	/**
	 * Mostra o tabuleiro e faz a escolha dos conselheiros secretos (pág. 6).
	 * Numa partida carregada eles já foram escolhidos, então ninguém é
	 * perguntado de novo.
	 */
	public void iniciar() {
		janela.setVisible(true);
		janela.escolherConselheirosSecretos();
		String aviso = avisoDoFim();
		janela.mostrarMensagem("Vez de " + jogo.getJogadorDaVez() + "." + (aviso.isEmpty() ? "" : " " + aviso));
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
	}

	// Move o peão até a parada (na Altani tenta o peão 1 e depois o 2). Se não
	// der pra mover, por exemplo clicando onde o peão já está, tenta construir
	// um yurt ali. Mover não passa a vez.
	void paradaClicada(String idParada) {
		String cor = jogo.getJogadorDaVez();
		for (int n = 1; n <= jogo.getNumeroDePeoes(cor); n++) {
			if (!jogo.moverPeaoDaVezAte(n, idParada).isEmpty()) {
				janela.mostrarMensagem("Peão movido para " + idParada + ".");
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
	}

	private void construirYurt(String parada) {
		if (jogo.construirYurt(parada)) {
			janela.mostrarMensagem("Yurt construído em " + parada + ".");
		} else {
			janela.mostrarMensagem("Não dá para construir yurt em " + parada + ".");
		}
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
	}

	private void colocarKhan(String lugar) {
		if (jogo.colocarKhan(lugar)) {
			janela.mostrarMensagem(Jogo.KHAN_NA_AREA_DE_MELHORIAS.equals(lugar)
					? "Khan na área de melhorias: novas melhorias reveladas."
					: "Khan em " + lugar + ": tributos acrescentados.");
		} else {
			janela.mostrarMensagem("O Khan não pode ficar onde já está.");
		}
	}

	// ------------------------------------------------------ vez e fim de jogo

	void passarVez() {
		if (jogo.getColunaAtivadaNaVez() == null) {
			janela.mostrarMensagem("Ative uma coluna antes de passar a vez.");
			return;
		}
		if (jogo.khanPendente()) {
			janela.mostrarMensagem("Use o Khan antes de passar a vez.");
			return;
		}
		jogo.passarVez(); // o Model conta os turnos que faltam depois do fim disparado
		if (jogo.partidaAcabou()) {
			encerrarPartida();
		} else {
			janela.mostrarMensagem(avisoDoFim());
		}
	}

	private String avisoDoFim() {
		int turnos = jogo.getTurnosAteOFim();
		return turnos > 0 ? "Fim de jogo disparado: faltam " + turnos + " turno(s)." : "";
	}

	/**
	 * Mostra o resultado e pergunta se jogam de novo (reabre a escolha dos
	 * personagens) ou fecham o programa.
	 */
	private void encerrarPartida() {
		JanelaFimDeJogo fim = new JanelaFimDeJogo(janela, jogo);
		fim.setVisible(true); // modal: espera a resposta
		janela.dispose();
		if (fim.querJogarDeNovo()) {
			new ControladorInicio().mostrarEscolhaDosJogadores();
		} else {
			System.exit(0);
		}
	}

	/** "Salvar": só no começo do turno; o arquivo é escolhido no JFileChooser. */
	void salvar() {
		if (!jogo.podeSalvar()) {
			janela.mostrarMensagem("Só dá para salvar no começo do turno, antes de qualquer jogada.");
			return;
		}
		File arquivo = SeletorPartida.escolherParaSalvar(janela);
		if (arquivo == null) {
			return; // cancelou
		}
		try {
			jogo.salvar(arquivo);
			janela.mostrarMensagem("Partida salva em " + arquivo.getName() + ".");
		} catch (IOException e) {
			janela.mostrarMensagem("Não deu para salvar: " + e.getMessage());
		}
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

	private class TratadorSalvar implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			salvar();
		}
	}

	private class TratadorHabilidade implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			trocarCavaloPorEspada();
		}
	}
}

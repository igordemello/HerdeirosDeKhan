package view;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/*
 * Escolha do arquivo da partida salva com o JFileChooser (4ª iteração), usada
 * pelos botões "Salvar" e "Continuar partida". O nome e a pasta são escolha
 * do usuário; só o tipo (.txt) é pré-definido, como pede o enunciado.
 */
public class SeletorPartida {
	private static final String EXTENSAO = "txt";

	private SeletorPartida() {
	}

	// Arquivo escolhido para continuar a partida, ou null se cancelou.
	public static File escolherParaAbrir(Component pai) {
		JFileChooser seletor = criar("Continuar partida");
		if (seletor.showOpenDialog(pai) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		return seletor.getSelectedFile();
	}

	// Arquivo onde salvar a partida, ou null se cancelou. Põe o .txt
	// se faltar e pergunta antes de substituir um arquivo que já existe.
	public static File escolherParaSalvar(Component pai) {
		JFileChooser seletor = criar("Salvar partida");
		if (seletor.showSaveDialog(pai) != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File arquivo = seletor.getSelectedFile();
		if (!arquivo.getName().toLowerCase().endsWith("." + EXTENSAO)) {
			arquivo = new File(arquivo.getParentFile(), arquivo.getName() + "." + EXTENSAO);
		}
		if (arquivo.exists() && JOptionPane.showConfirmDialog(pai, "O arquivo " + arquivo.getName()
				+ " já existe. Substituir?", "Salvar partida", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
			return null;
		}
		return arquivo;
	}

	private static JFileChooser criar(String titulo) {
		JFileChooser seletor = new JFileChooser();
		seletor.setDialogTitle(titulo);
		seletor.setFileFilter(new FileNameExtensionFilter("Partida de Herdeiros do Khan (*.txt)", EXTENSAO));
		return seletor;
	}
}

import pt.iscte.guitoo.Color;
import pt.iscte.guitoo.StandardColor;
import pt.iscte.guitoo.board.Board;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

record Position(int line, int col) {

}

public class View {
	Board board;
	Lógica model;

	View(Lógica model) {
		this.model = model;
		board = new Board("Damas", model.size, model.size, 60);
		board.setBackgroundProvider(this::background);
		board.setIconProvider(this::icon);
		board.addMouseListener(this::click);
		board.addAction("Random", this::randomButton);
		board.addAction("New", this::newButton);
		board.addAction("Save", this::saveButton);
		board.addAction("Load", this::loadButton);
		board.setTitle("Brancas Jogam");
		board.addMouseListener(this::Title);
		model.selectedPiece = null;
		model.initialBoardPositions();
	}

	void Title(int line, int col) {
		if (model.whiteTurn) {
			board.setTitle("Brancas Jogam");
		} else {
			board.setTitle("Pretas Jogam");
		}
	}

	void start() {
		board.open();
	}

	public static void main(String[] args) {
		Lógica gameModel = new Lógica(8, 12);
		View gui = new View(gameModel);
		gui.start();
	}

	Color background(int line, int col) {
		if (!model.gameOver() && model.selectedPiece != null && model.selectedPiece.line() == line && model.selectedPiece.col() == col) {
			return StandardColor.YELLOW;
		}
		if ((line + col) % 2 != 0) {
			return StandardColor.BLACK;
		}
		return StandardColor.WHITE;
	}

	String icon(int line, int col) {
		if (model.boardPositions[line][col] == 1) {
			return "black.png";
		}
		if (model.boardPositions[line][col] == 2) {
			return "white.png";
		}
		return null;
	}

	void click(int line, int col) {
		if (!model.gameOver()) {
			if (model.selectedPiece != null) {
				if (model.isValidMove(model.selectedPiece.line(), model.selectedPiece.col(), line, col)) {
					model.movePiece(model.selectedPiece.line(), model.selectedPiece.col(), line, col);
					model.whiteTurn = !model.whiteTurn;
					if (!model.hasPieces(2)) {
						board.showMessage("As peças pretas venceram! Não há mais peças brancas no tabuleiro.");
						return;
					}

					if (!model.hasPieces(1)) {
						board.showMessage("As peças brancas venceram! Não há mais peças pretas no tabuleiro.");
						return;
					}

					if (!model.canMove(2)) {
						board.showMessage("As peças pretas venceram! As peças brancas não podem mais se mover.");
						return;
					}

					if (!model.canMove(1)) {
						board.showMessage("As peças brancas venceram! As peças pretas não podem mais se mover.");
						return;
					}
				}
				model.selectedPiece = null;
			} else if (model.boardPositions[line][col] != 0 && model.isCorrectTurn(line, col)) {
				if (model.hasMandatoryCapture()) {
					if (model.canCapture(line, col)) {
						model.selectedPiece = new Position(line, col);//
					}
				} else{
					model.selectedPiece = new Position(line, col);
				}
					
			}
			model.updateIcons();
		}else {
			board.showMessage("O jogo já acabou. Se pretende continuar a joga feche esta aba e clique no new!");
		}
	}

	private int randomMoveCounter = 0;
	private final int RANDOM_MOVE_LIMIT = 1000;

	void randomButton() {
		if (!model.gameOver()) {

			if (randomMoveCounter >= RANDOM_MOVE_LIMIT) {
				board.showMessage("O botão Random atingiu o limite de 1000 movimentos.");
				return;
			}

			// Realiza um movimento aleatório
			Random random = new Random();
			boolean moveMade = false;

			for (int attempts = 0; attempts < 1000; attempts++) {
				int startLine = random.nextInt(model.size);
				int startCol = random.nextInt(model.size);

				if (model.boardPositions[startLine][startCol] != 0 && model.isCorrectTurn(startLine, startCol)) {
					int piece = model.boardPositions[startLine][startCol];
					int direction = (piece == 1) ? 1 : -1;
					int opponent = (piece == 1) ? 2 : 1;

					int[][] possibleMoves = { { startLine + direction, startCol - 1 },
							{ startLine + direction, startCol + 1 } };

					int[][] possibleCaptures = {
							{ startLine + 2 * direction, startCol - 2, startLine + direction, startCol - 1 },
							{ startLine + 2 * direction, startCol + 2, startLine + direction, startCol + 1 } };

					for (int[] capture : possibleCaptures) {
						int toLine = capture[0];
						int toCol = capture[1];
						int midLine = capture[2];
						int midCol = capture[3];

						if (toLine >= 0 && toLine < model.boardPositions.length && toCol >= 0
								&& toCol < model.boardPositions[0].length
								&& model.boardPositions[midLine][midCol] == opponent
								&& model.boardPositions[toLine][toCol] == 0) {
							model.movePiece(startLine, startCol, toLine, toCol);
							moveMade = true;
							break;
						}
					}

					if (!moveMade && !model.hasMandatoryCapture()) {
						for (int[] move : possibleMoves) {
							int toLine = move[0], toCol = move[1];

							if (toLine >= 0 && toLine < model.boardPositions.length && toCol >= 0
									&& toCol < model.boardPositions[0].length
									&& model.boardPositions[toLine][toCol] == 0) {
								model.movePiece(startLine, startCol, toLine, toCol);
								moveMade = true;
								break;
							}
						}
					}
				}

				if (moveMade) {
					randomMoveCounter++;
					model.whiteTurn = !model.whiteTurn;
					if (!model.hasPieces(2)) {
						board.showMessage("As peças pretas venceram! Não há mais peças brancas no tabuleiro.");
						return;
					}

					if (!model.hasPieces(1)) {
						board.showMessage("As peças brancas venceram! Não há mais peças pretas no tabuleiro.");
						return;
					}

					if (!model.canMove(2)) {
						board.showMessage("As peças pretas venceram! As peças brancas não podem mais se mover.");
						return;
					}

					if (!model.canMove(1)) {
						board.showMessage("As peças brancas venceram! As peças pretas não podem mais se mover.");
						return;
					}
					// Alterna o turno
					model.updateIcons();
					Title(0, 0);
					return;
				}
			}

			randomMoveCounter++;
			if (randomMoveCounter >= RANDOM_MOVE_LIMIT) {
				board.showMessage("O botão Random atingiu o limite de 1000 movimentos.");
			} else if (model.gameOver()) {
				board.showMessage("O jogo já acabou. Se pretende continuar a joga feche esta aba e clique no new!");
			}
		} else {
			board.showMessage("O jogo já acabou. Se pretende continuar a joga feche esta aba e clique no new!");
		}
	}

	void newButton() {
		int size = board.promptInt("De que tamanho deseja o seu novo tabuleiro?");
		int numPieces = board.promptInt("Quantas peças quer no seu jogo por jogador?");
		if (size < 0 && numPieces < 0) {
			throw new IllegalArgumentException("Dimensões não são válidas");
		} else {
			View newGame = new View(new Lógica(size, numPieces));
			newGame.start();
		}
	}

	void saveButton() {
		try {
			String fileName = board.promptText("Dê um nome ao seu projeto") + ".txt";
			boolean exists = (new File(fileName)).exists();
			if (exists) { // se existir
				board.showMessage("Já um ficheiro com esse nome!");
				int choice = board.promptInt("Escolha:" + "\n" + "\n" + "1 - Escreve por cima" + "\n"
						+ "2 - Escolhe outro código" + "\n" + "‎");

				if (choice == 1) {
					exists = !exists;
				}
				if (choice == 2) {
					saveButton();
				}
			}
			if (!exists) {
				PrintWriter writer = new PrintWriter(new File(fileName));
				writer.println(model.size);
				writer.println(model.numPieces);

				for (int line = 0; line < model.size; line++) {
					for (int col = 0; col < model.size; col++) {
						writer.println(model.boardPositions[line][col] + " ");
					}
					writer.println();
				}
				writer.println(model.whiteTurn ? "Branco" : "Preto");

				writer.close();
				board.showMessage("Jogo salvo!");
			}
		} catch (FileNotFoundException e) {
			board.showMessage("Erro a escrever no ficheiro ");
		}
	}

	void loadButton() {
		try {
			String fileName = board.promptText("Nome do ficheiro que deseja baixar") + ".txt";
			Scanner scanner = new Scanner(new File(fileName));
			int size = scanner.nextInt();
			int numPieces = scanner.nextInt();
			Lógica newModel = new Lógica(size, numPieces);

			for (int line = 0; line < size; line++) {
				for (int col = 0; col < size; col++) {
					newModel.boardPositions[line][col] = scanner.nextInt();
				}
			}

			String currentPlayer = scanner.next();
			newModel.whiteTurn = currentPlayer.equals("Branco");

			scanner.close();

			View newView = new View(newModel); // cria uma nova interface
			newView.board.setTitle((model.whiteTurn) ? "Brancas Jogam" : "Pretas Jogam");
			newView.start();

		} catch (FileNotFoundException e) {
			board.showMessage("error reading the file");
		}

	}
}
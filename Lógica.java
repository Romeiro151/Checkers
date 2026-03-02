public class Lógica {

	Position selectedPiece;
	Position EatenBlackPiece;
	boolean whiteTurn = true;
	public int size;
	public int numPieces;
	int[][] boardPositions;
	boolean[][] possibleMoves;

	Lógica(int size, int numPieces) {
	    this.size = size; // Correctly initialize size
	    this.numPieces = numPieces;
	    this.boardPositions = new int[size][size]; // Correct initialization after size is set
	    this.possibleMoves = new boolean[size][size];
	    initialBoardPositions();
	}

	
	public void initialBoardPositions() {
		int pieces=numPieces*2;
		for (int line = 0; line < size; line++) {
			for (int col = 0; col < size; col++) {
				if (((line + col) % 2 != 0 ) && (pieces!=0)) {
					boardPositions[line][col] = 1;
					pieces--;
				}
				if (((line + col) % 2 != 0)&& (pieces!=0)) {
					boardPositions[size-line-1][size-col-1] = 2;
					pieces--;
				}
			}
		}
	}
	
	private void placeBlackIcon(int line, int col) {
		boardPositions[line][col] = 1; // Garantir que o preto é 1
	}

	private void placeWhiteIcon(int line, int col) {
		boardPositions[line][col] = 2; // Garantir que o branco é 2
	}

	private void removeIcon(int line, int col) {
		boardPositions[line][col] = 0; //peça desaparece se for 0
	}
	
	boolean isCorrectTurn(int line, int col) {
		return (whiteTurn && boardPositions[line][col] == 2) || (!whiteTurn && boardPositions[line][col] == 1);
	}
	
	boolean canCapture(int line, int col) {
		int piece = boardPositions[line][col];
		int direction = (piece == 1) ? 1 : -1; // Peças pretas vão para baixo (+1), brancas para cima (-1)
		int opponent = (piece == 1) ? 2 : 1; // se a peça for preta o adversário é branco e vice-versa

		// Permitir capturar apenas para frente
		int[][] directions = { { direction * 2, -2 }, { direction * 2, 2 } }; 
		for (int[] dir : directions) { // itera os vetores da matriz directions sendo dir[0] a posição 0 do vetor.
			int newLine = line + dir[0]; 
			int newCol = col + dir[1];
			int eatenLine = line + dir[0] / 2;
			int eatenCol = col + dir[1] / 2;

			if (newLine >= 0 && newLine < size && newCol >= 0 && newCol < size) { 
				if (boardPositions[eatenLine][eatenCol] == opponent && boardPositions[newLine][newCol] == 0) { 
					return true;
				}
			}
		}
		return false;
	}
	
	boolean hasMandatoryCapture() { //percorre todas as linhas e colunas e verifica se todas as peça está no terno certo e pode comer. Se puder torna-se obrigatório comer, proibindo qualquer outra johgada
		for (int line = 0; line < size; line++) {
			for (int col = 0; col < size; col++) {
				if (isCorrectTurn(line,col)) {
					if (canCapture(line, col)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	void movePiece(int fromLine, int fromCol, int toLine, int toCol) {
		boardPositions[toLine][toCol] = boardPositions[fromLine][fromCol]; // reseta o valor da peça selecionada para 0
		boardPositions[fromLine][fromCol] = 0;

		// Captura (remover peça intermediária)
		if (Math.abs(fromLine - toLine) == 2) {
			int eatenLine = (fromLine + toLine) / 2;
			int eatenCol = (fromCol + toCol) / 2;
			boardPositions[eatenLine][eatenCol] = 0;
		}
	}

	boolean isValidMove(int fromLine, int fromCol, int toLine, int toCol) {//verifica mais uma vez se a caputra é possivél ou se não o movimento.
		int piece = boardPositions[fromLine][fromCol];//cria uma variável piece para a peça selecionada
		int direction = (piece == 1) ? 1 : -1;
		int opponent = (piece == 1) ? 2 : 1;

		// Captura
		if (Math.abs(fromLine - toLine) == 2 && Math.abs(fromCol - toCol) == 2) {//verifica se entre a peça selecionada e o local onde vai acabar depois de comer tem 2 casas diferentes, ou seja um espaço no meio.
			int eatenLine = (fromLine + toLine) / 2; 
			int eatenCol = (fromCol + toCol) / 2;

			if (boardPositions[eatenLine][eatenCol] == opponent && boardPositions[toLine][toCol] == 0
					&& (toLine - fromLine == 2 * direction)) { //se a peça comida for de cor contrária à selecionada, a posição para comer estiver vazia e se a linha onde vais comer for 2 a baixo (se for preta direction=1) ou for 2 a cima (se for brancaa direction=-1)
				boardPositions[eatenLine][eatenCol] = 0; // Remove a peça capturada
				return true;
			}
		}

		// Movimento normal
		return !hasMandatoryCapture() && Math.abs(fromCol - toCol) == 1 && toLine - fromLine == direction // se não tiver captura obrigatória e o a peça só andar uma casa para os lados e andar na direção certa pode se mover
				&& boardPositions[toLine][toCol] == 0;
	}

	void updateIcons() { //atualizar o tabuleiro depois de cada movimento (verificar que a peça não anda para trás depois de comer)
		for (int line = 0; line < size; line++) {
			for (int col = 0; col < size; col++) {
				if (boardPositions[line][col] == 1) {
					placeBlackIcon(line, col);
				} else if (boardPositions[line][col] == 2) {
					placeWhiteIcon(line, col);
				} else {
					removeIcon(line, col);
				}
			}
		}
	}

	// Métodos auxiliares
	boolean hasPieces(int pieceType) { // Método para verificar se há peças de um determinado tipo no tabuleiro.
		for (int[] line : boardPositions) { // Percorre cada linha do tabuleiro.
			for (int col : line) { // Percorre cada célula da linha atual.
				if (col == pieceType) {
					return true; 
				}
			}
		}
		return false;
	}

	boolean canMove(int pieceType) { // Método para verificar se há movimentos válidos para um determinado tipo de peça.
		for (int line = 0; line < size; line++) {
			for (int col = 0; col < size; col++) { 
				if (boardPositions[line][col] == pieceType) { 
					int direction = (pieceType == 1) ? 1 : -1; 
					int[][] possibleMoves = { { line + direction, col - 1 }, { line + direction, col + 1 } }; // Define os movimentos simples possíveis (diagonais para frente).
					int[][] possibleCaptures = { // Define os movimentos de captura possíveis (saltos sobre peças adversárias).
							{ line + 2 * direction, col - 2, line + direction, col - 1 }, // Captura para a esquerda. primeiras 2 do vetor para comer e os outros 2 para as peças comidas
							{ line + 2 * direction, col + 2, line + direction, col + 1 }  // Captura para a direita. primeiras 2 do vetor para comer e os outros 2 para as peças comidas
					};

					// Verificar capturas
					for (int[] capture : possibleCaptures) { // Itera sobre os movimentos de captura possíveis.
						int toLine = capture[0];
						int toCol = capture[1];  
						int eatenLine = capture[2];
						int eatenCol = capture[3]; 
						if (toLine >= 0 && toLine < size 
								&& toCol >= 0 && toCol < size // Verifica se a coluna de destino está dentro dos limites.
								&& boardPositions[eatenLine][eatenCol] == (pieceType == 1 ? 2 : 1) // Verifica uma peça adversária.
								&& boardPositions[toLine][toCol] == 0) { // Verifica  destino está vazia.
							return true;
						}
					}

					for (int[] move : possibleMoves) { // Itera sobre os movimentos simples possíveis.
						int toLine = move[0];
						int toCol = move[1]; 
						if (toLine >= 0 && toLine < boardPositions.length
								&& toCol >= 0 && toCol < boardPositions[0].length 
								&& boardPositions[toLine][toCol] == 0) {
							return true;
						}
					}
				}
			}
		}
		return false; 
	}
	boolean gameOver() {
		return (!hasPieces(1)||!hasPieces(2)||!canMove(1)||!canMove(2));
	}
	
}
	
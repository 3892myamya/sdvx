package myamya.other.solver.slither;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class KurohouiSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private final int[][] numbers;
		// 外周に属する位置情報
		private final Set<Position> wallPosSet;

		public Masu[][] getMasu() {
			return masu;
		}

		public int[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new int[height][width];
			wallPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					numbers[yIndex][xIndex] = -1;
					Position wallPosCand = new Position(yIndex, xIndex);
					if (isWallPos(wallPosCand)) {
						wallPosSet.add(new Position(yIndex, xIndex));
					}
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == '.') {
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							numbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
						}
						index++;
					}
				}
			}
		}

		/**
		 * そのposが外周であるかを返す。
		 */
		private boolean isWallPos(Position pos) {
			return pos.getyIndex() == 0 || pos.getxIndex() == 0 || pos.getyIndex() == getYLength() - 1
					|| pos.getxIndex() == getXLength() - 1;
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			numbers = other.numbers;
			wallPosSet = other.wallPosSet;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE && numbers[yIndex][xIndex] != -1) {
						String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(capacityStr);
						if (index >= 0) {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(capacityStr);
						}
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 自身と違う色のマスが周りに何マスあるか調べる。
		 * 矛盾したらfalseを返す。
		 */
		public boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != -1
							&& (numbers[yIndex][xIndex] == 2 || masu[yIndex][xIndex] != Masu.SPACE)) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu masuUp = yIndex == 0 ? Masu.BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.BLACK : masu[yIndex][xIndex - 1];
						if (masuUp == Masu.BLACK) {
							blackCnt++;
						} else if (masuUp == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuRight == Masu.BLACK) {
							blackCnt++;
						} else if (masuRight == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuDown == Masu.BLACK) {
							blackCnt++;
						} else if (masuDown == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuLeft == Masu.BLACK) {
							blackCnt++;
						} else if (masuLeft == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masu[yIndex][xIndex] == Masu.BLACK || numbers[yIndex][xIndex] == 2) {
							if (numbers[yIndex][xIndex] < whiteCnt) {
								// 白マス過剰
								return false;
							}
							if (numbers[yIndex][xIndex] == whiteCnt) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.BLACK;
								}
							}
							if (numbers[yIndex][xIndex] > 4 - blackCnt) {
								// 白マス不足
								return false;
							}
							if (numbers[yIndex][xIndex] == 4 - blackCnt) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
								}
							}
						}
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK || numbers[yIndex][xIndex] == 2) {
							if (numbers[yIndex][xIndex] < blackCnt) {
								// 黒マス過剰
								return false;
							}
							if (numbers[yIndex][xIndex] == blackCnt) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
								}
							}
							if (numbers[yIndex][xIndex] > 4 - whiteCnt) {
								// 黒マス不足
								return false;
							}
							if (numbers[yIndex][xIndex] == 4 - whiteCnt) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.BLACK;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 置くとオセロ初期配置になるマスを違う色でぬる。
		 * 既にオセロ初期配置ができている場合falseを返す。
		 */
		public boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.NOT_BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.BLACK) {
						masu[yIndex][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.SPACE
							&& masu4 == Masu.BLACK) {
						masu[yIndex + 1][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
					}
				}
			}
			return true;
		}

		/**
		 * 白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectWhiteSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			Position typicalWhitePos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						whitePosSet.add(whitePos);
						if (typicalWhitePos == null) {
							typicalWhitePos = whitePos;
						}
					}
				}
			}
			if (typicalWhitePos == null) {
				return true;
			} else {
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				setContinueNotBlackPosSet(typicalWhitePos, continuePosSet);
				whitePosSet.removeAll(continuePosSet);
				return whitePosSet.isEmpty();
			}
		}

		/**
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
		 */
		private void setContinueNotBlackPosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueNotBlackPosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueNotBlackPosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueNotBlackPosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueNotBlackPosSet(nextPos, continuePosSet);
				}
			}
		}

		/**
		 * 黒マスがひとつながりにならない場合Falseを返す。
		 * ただし、外周はひとつながりになってるものと考える。
		 */
		public boolean connectWallBlackSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			Position typicalBlackPos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position blackPos = new Position(yIndex, xIndex);
						blackPosSet.add(blackPos);
						if (typicalBlackPos == null && yIndex != 0 && xIndex != 0 && yIndex != getYLength() - 1
								&& xIndex != getXLength() - 1) {
							typicalBlackPos = blackPos;
						}
					}
				}
			}
			if (typicalBlackPos == null) {
				return true;
			} else {
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalBlackPos);
				setWallContinueNotWhitePosSet(typicalBlackPos, continuePosSet);
				blackPosSet.removeAll(continuePosSet);
				return blackPosSet.isEmpty();
			}
		}

		/**
		 * posを起点に上下左右に白確定でないマスを無制限につなげていく。
		 * ただし、外周はひとつながりになってるものと考える。
		 */
		private void setWallContinueNotWhitePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					if (isWallPos(nextPos)) {
						for (Position wallPos : wallPosSet) {
							if (!continuePosSet.contains(wallPos)
									&& masu[wallPos.getyIndex()][wallPos.getxIndex()] != Masu.NOT_BLACK) {
								continuePosSet.add(wallPos);
								setWallContinueNotWhitePosSet(wallPos, continuePosSet);
							}
						}
					} else {
						continuePosSet.add(nextPos);
						setWallContinueNotWhitePosSet(nextPos, continuePosSet);
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					if (isWallPos(nextPos)) {
						for (Position wallPos : wallPosSet) {
							if (!continuePosSet.contains(wallPos)
									&& masu[wallPos.getyIndex()][wallPos.getxIndex()] != Masu.NOT_BLACK) {
								continuePosSet.add(wallPos);
								setWallContinueNotWhitePosSet(wallPos, continuePosSet);
							}
						}
					} else {
						continuePosSet.add(nextPos);
						setWallContinueNotWhitePosSet(nextPos, continuePosSet);
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					if (isWallPos(nextPos)) {
						for (Position wallPos : wallPosSet) {
							if (!continuePosSet.contains(wallPos)
									&& masu[wallPos.getyIndex()][wallPos.getxIndex()] != Masu.NOT_BLACK) {
								continuePosSet.add(wallPos);
								setWallContinueNotWhitePosSet(wallPos, continuePosSet);
							}
						}
					} else {
						continuePosSet.add(nextPos);
						setWallContinueNotWhitePosSet(nextPos, continuePosSet);
					}
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					if (isWallPos(nextPos)) {
						for (Position wallPos : wallPosSet) {
							if (!continuePosSet.contains(wallPos)
									&& masu[wallPos.getyIndex()][wallPos.getxIndex()] != Masu.NOT_BLACK) {
								continuePosSet.add(wallPos);
								setWallContinueNotWhitePosSet(wallPos, continuePosSet);
							}
						}
					} else {
						continuePosSet.add(nextPos);
						setWallContinueNotWhitePosSet(nextPos, continuePosSet);
					}
				}
			}
		}

		/**
		 * フィールドに1つは白マスが必要。
		 */
		private boolean finalSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] != Masu.BLACK) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
			if (!connectWhiteSolve()) {
				return false;
			}
			if (!connectWallBlackSolve()) {
				return false;
			}
			if (!finalSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;

	public KurohouiSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = ""; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new KurohouiSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		int difficulty = 0;
		long start = System.nanoTime();
		while (!field.isSolved()) {
			int recursiveCnt = 0;
			while (!field.isSolved() && recursiveCnt < 4) {
				System.out.println(field);
				difficulty = difficulty < recursiveCnt ? recursiveCnt : difficulty;
				if (!candSolve(field, recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 4 && !field.isSolved()) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (difficulty));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByVal(difficulty).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private static boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
		return true;
	}

	/**
	 * 1つのマスに対する仮置き調査
	 */
	private static boolean oneCandSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.masu[yIndex][xIndex] = Masu.BLACK;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.masu[yIndex][xIndex] = Masu.NOT_BLACK;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.masu = virtual2.masu;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
		}
		return true;
	}
}
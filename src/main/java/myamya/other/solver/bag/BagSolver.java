package myamya.other.solver.bag;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class BagSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private final Integer[][] numbers;
		// 外周に属する位置情報
		private final Set<Position> wallPosSet;

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[][] getNumbers() {
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
			numbers = new Integer[height][width];
			wallPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					Position wallPosCand = new Position(yIndex, xIndex);
					if (isWallPos(wallPosCand)) {
						wallPosSet.add(new Position(yIndex, xIndex));
					}
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int capacity;
					if (ch == '-') {
						capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
						i++;
						i++;
					} else if (ch == '+') {
						capacity = Integer.parseInt(
								"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
								16);
						i++;
						i++;
						i++;
					} else {
						capacity = Integer.parseInt(String.valueOf(ch), 16);
					}
					Position pos = new Position(index / getXLength(), index % getXLength());
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					index++;
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
					if (numbers[yIndex][xIndex] != null) {
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
					if (numbers[yIndex][xIndex] != null) {
						int upSpaceCnt = 0;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (masu[targetY][xIndex] == Masu.BLACK) {
								break;
							}
							upSpaceCnt++;
						}
						int rightSpaceCnt = 0;
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (masu[yIndex][targetX] == Masu.BLACK) {
								break;
							}
							rightSpaceCnt++;
						}
						int downSpaceCnt = 0;
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (masu[targetY][xIndex] == Masu.BLACK) {
								break;
							}
							downSpaceCnt++;
						}
						int leftSpaceCnt = 0;
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (masu[yIndex][targetX] == Masu.BLACK) {
								break;
							}
							leftSpaceCnt++;
						}
						int aroundSpaceCnt = 1 + upSpaceCnt + rightSpaceCnt + downSpaceCnt + leftSpaceCnt;
						if (aroundSpaceCnt < numbers[yIndex][xIndex]) {
							return false;
						} else {
							int fixedWhiteUp = numbers[yIndex][xIndex]
									- (1 + rightSpaceCnt + downSpaceCnt + leftSpaceCnt);
							int fixedWhiteRight = numbers[yIndex][xIndex]
									- (1 + upSpaceCnt + downSpaceCnt + leftSpaceCnt);
							int fixedWhiteDown = numbers[yIndex][xIndex]
									- (1 + upSpaceCnt + rightSpaceCnt + leftSpaceCnt);
							int fixedWhitetLeft = numbers[yIndex][xIndex]
									- (1 + upSpaceCnt + rightSpaceCnt + downSpaceCnt);
							if (fixedWhiteUp > 0) {
								for (int i = 1; i <= fixedWhiteUp; i++) {
									if (masu[yIndex - i][xIndex] == Masu.SPACE) {
										masu[yIndex - i][xIndex] = Masu.NOT_BLACK;
									}
								}
							}
							if (fixedWhiteRight > 0) {
								for (int i = 1; i <= fixedWhiteRight; i++) {
									if (masu[yIndex][xIndex + i] == Masu.SPACE) {
										masu[yIndex][xIndex + i] = Masu.NOT_BLACK;
									}
								}
							}
							if (fixedWhiteDown > 0) {
								for (int i = 1; i <= fixedWhiteDown; i++) {
									if (masu[yIndex + i][xIndex] == Masu.SPACE) {
										masu[yIndex + i][xIndex] = Masu.NOT_BLACK;
									}
								}
							}
							if (fixedWhitetLeft > 0) {
								for (int i = 1; i <= fixedWhitetLeft; i++) {
									if (masu[yIndex][xIndex - i] == Masu.SPACE) {
										masu[yIndex][xIndex - i] = Masu.NOT_BLACK;
									}
								}
							}
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int upWhiteCnt = 0;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (masu[targetY][xIndex] != Masu.NOT_BLACK) {
								break;
							}
							upWhiteCnt++;
						}
						int rightWhiteCnt = 0;
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (masu[yIndex][targetX] != Masu.NOT_BLACK) {
								break;
							}
							rightWhiteCnt++;
						}
						int downWhiteCnt = 0;
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (masu[targetY][xIndex] != Masu.NOT_BLACK) {
								break;
							}
							downWhiteCnt++;
						}
						int leftWhiteCnt = 0;
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (masu[yIndex][targetX] != Masu.NOT_BLACK) {
								break;
							}
							leftWhiteCnt++;
						}
						int aroundWhiteCnt = 1 + upWhiteCnt + rightWhiteCnt + downWhiteCnt + leftWhiteCnt;
						if (aroundWhiteCnt > numbers[yIndex][xIndex]) {
							return false;
						} else if (aroundWhiteCnt == numbers[yIndex][xIndex]) {
							if (yIndex - upWhiteCnt - 1 >= 0) {
								masu[yIndex - upWhiteCnt - 1][xIndex] = Masu.BLACK;
							}
							if (xIndex + rightWhiteCnt + 1 < getXLength()) {
								masu[yIndex][xIndex + rightWhiteCnt + 1] = Masu.BLACK;
							}
							if (yIndex + downWhiteCnt + 1 < getYLength()) {
								masu[yIndex + downWhiteCnt + 1][xIndex] = Masu.BLACK;
							}
							if (xIndex - leftWhiteCnt - 1 >= 0) {
								masu[yIndex][xIndex - leftWhiteCnt - 1] = Masu.BLACK;
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
	private int count = 0;

	public BagSolver(int height, int width, String param) {
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
		System.out.println(new BagSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				if (!candSolve(field, recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count * 4));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 4).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					count++;
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
	private boolean oneCandSolve(Field field, int yIndex, int xIndex, int recursive) {
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
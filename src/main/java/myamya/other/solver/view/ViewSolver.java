package myamya.other.solver.view;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class ViewSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 初期確定数字の位置情報
		private final Set<Position> fixedPosSet;
		// 数字の情報
		private Integer[][] numbers;

		public Masu[][] getMasu() {
			return masu;
		}

		public Set<Position> getFixedPosSet() {
			return fixedPosSet;
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
			fixedPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
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
					if (ch == '.') {
						Position pos = new Position(index / getXLength(), index % getXLength());
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						fixedPosSet.add(pos);
					} else {
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
						fixedPosSet.add(pos);
					}
					index++;
				}
			}
		}

		public Field(Field other) {
			fixedPosSet = other.fixedPosSet;
			masu = new Masu[other.getYLength()][other.getXLength()];
			numbers = new Integer[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
				}
			}
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						} else if (numbers[yIndex][xIndex] == -1) {
							sb.append("？");
						} else {
							String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(capacityStr);
							if (index >= 0) {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							} else {
								sb.append(capacityStr);
							}
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
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!countSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!nextSolve()) {
					return false;
				}
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 白マスの前後左右にある黒マスおよび黒マス候補の数をカウントする。
		 * 超過や不足が確定したらfalseを返す。
		 */
		private boolean countSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						int blackCnt = 0;
						int spaceCnt = 0;
						boolean blackContinue = true;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (masu[targetY][xIndex] == Masu.NOT_BLACK) {
								break;
							}
							if (masu[targetY][xIndex] == Masu.SPACE) {
								blackContinue = false;
							}
							if (blackContinue) {
								blackCnt++;
							}
							spaceCnt++;
						}
						blackContinue = true;
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (masu[yIndex][targetX] == Masu.NOT_BLACK) {
								break;
							}
							if (masu[yIndex][targetX] == Masu.SPACE) {
								blackContinue = false;
							}
							if (blackContinue) {
								blackCnt++;
							}
							spaceCnt++;
						}
						blackContinue = true;
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (masu[targetY][xIndex] == Masu.NOT_BLACK) {
								break;
							}
							if (masu[targetY][xIndex] == Masu.SPACE) {
								blackContinue = false;
							}
							if (blackContinue) {
								blackCnt++;
							}
							spaceCnt++;
						}
						blackContinue = true;
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (masu[yIndex][targetX] == Masu.NOT_BLACK) {
								break;
							}
							if (masu[yIndex][targetX] == Masu.SPACE) {
								blackContinue = false;
							}
							if (blackContinue) {
								blackCnt++;
							}
							spaceCnt++;
						}
						if (numbers[yIndex][xIndex] == null) {
							if (blackCnt == spaceCnt) {
								// 数字が確定
								numbers[yIndex][xIndex] = blackCnt;
							}
						} else {
							if (numbers[yIndex][xIndex] < blackCnt) {
								// 黒マス超過
								return false;
							}
							if (numbers[yIndex][xIndex] > spaceCnt) {
								// 黒マス不足
								return false;
							}
							if (numbers[yIndex][xIndex] == blackCnt) {
								// これ以上黒マスは伸びない
								for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
									if (masu[targetY][xIndex] == Masu.NOT_BLACK) {
										break;
									}
									if (masu[targetY][xIndex] == Masu.SPACE) {
										masu[targetY][xIndex] = Masu.NOT_BLACK;
										break;
									}
								}
								for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
									if (masu[yIndex][targetX] == Masu.NOT_BLACK) {
										break;
									}
									if (masu[yIndex][targetX] == Masu.SPACE) {
										masu[yIndex][targetX] = Masu.NOT_BLACK;
										break;
									}
								}
								for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
									if (masu[targetY][xIndex] == Masu.NOT_BLACK) {
										break;
									}
									if (masu[targetY][xIndex] == Masu.SPACE) {
										masu[targetY][xIndex] = Masu.NOT_BLACK;
										break;
									}
								}
								for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
									if (masu[yIndex][targetX] == Masu.NOT_BLACK) {
										break;
									}
									if (masu[yIndex][targetX] == Masu.SPACE) {
										masu[yIndex][targetX] = Masu.NOT_BLACK;
										break;
									}
								}
							}
							if (numbers[yIndex][xIndex] == spaceCnt) {
								// 伸ばせるだけ黒マスを伸ばす
								for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
									if (masu[targetY][xIndex] == Masu.NOT_BLACK) {
										break;
									}
									if (masu[targetY][xIndex] == Masu.SPACE) {
										masu[targetY][xIndex] = Masu.BLACK;
									}
								}
								for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
									if (masu[yIndex][targetX] == Masu.NOT_BLACK) {
										break;
									}
									if (masu[yIndex][targetX] == Masu.SPACE) {
										masu[yIndex][targetX] = Masu.BLACK;
									}
								}
								for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
									if (masu[targetY][xIndex] == Masu.NOT_BLACK) {
										break;
									}
									if (masu[targetY][xIndex] == Masu.SPACE) {
										masu[targetY][xIndex] = Masu.BLACK;
									}
								}
								for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
									if (masu[yIndex][targetX] == Masu.NOT_BLACK) {
										break;
									}
									if (masu[yIndex][targetX] == Masu.SPACE) {
										masu[yIndex][targetX] = Masu.BLACK;
									}
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 確定した数字がある場合、自身の前後左右のマスに同じ数字があってはならない。
		 */
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK && numbers[yIndex][xIndex] != null) {
						Integer number = numbers[yIndex][xIndex];
						Integer numberUp = yIndex == 0 ? null
								: masu[yIndex - 1][xIndex] == Masu.BLACK ? null : numbers[yIndex - 1][xIndex];
						Integer numberRight = xIndex == getXLength() - 1 ? null
								: masu[yIndex][xIndex + 1] == Masu.BLACK ? null : numbers[yIndex][xIndex + 1];
						Integer numberDown = yIndex == getYLength() - 1 ? null
								: masu[yIndex + 1][xIndex] == Masu.BLACK ? null : numbers[yIndex + 1][xIndex];
						Integer numberLeft = xIndex == 0 ? null
								: masu[yIndex][xIndex - 1] == Masu.BLACK ? null : numbers[yIndex][xIndex - 1];
						if (number == numberUp || number == numberRight
								|| number == numberDown || number == numberLeft) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスがひとつながりにならない場合Falseを返す。
		 * 今までのロジックより高速に動きます。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
							whitePosSet.add(whitePos);
							setContinuePosSet(whitePos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(whitePos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
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
	private int count;

	public ViewSolver(int height, int width, String param) {
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
		System.out.println(new ViewSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 3));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 3).toString();
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
			field.numbers = virtual2.numbers;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.numbers = virtual.numbers;
		}
		//		else {
		//			// どちらにしても理論
		//			for (int y = 0; y < field.getYLength(); y++) {
		//				for (int x = 0; x < field.getXLength(); x++) {
		//					if (virtual2.masu[y][x] == virtual.masu[y][x]) {
		//						field.masu[y][x] = virtual.masu[y][x];
		//					}
		//				}
		//			}
		//		}
		return true;
	}
}
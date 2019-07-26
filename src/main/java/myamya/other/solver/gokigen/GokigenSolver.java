package myamya.other.solver.gokigen;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class GokigenSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報。ここでは黒=＼、白=／とする
		private Masu[][] masu;
		// 数字の情報。外壁も考慮するため注意
		private final Integer[][] extraNumbers;

		public Masu[][] getMasu() {
			return masu;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			extraNumbers = new Integer[height + 1][width + 1];
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / (getXLength() + 1), index % (getXLength() + 1));
				if (ch == '.') {
					extraNumbers[pos.getyIndex()][pos.getxIndex()] = -1;
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							extraNumbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							extraNumbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							extraNumbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
						}
						index++;
					}
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			extraNumbers = other.extraNumbers;
		}

		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (extraNumbers[yIndex][xIndex] == null) {
						sb.append("□");
					} else if (extraNumbers[yIndex][xIndex] == -1) {
						sb.append("○");
					} else {
						sb.append(FULL_NUMS.substring(extraNumbers[yIndex][xIndex],
								extraNumbers[yIndex][xIndex] + 1));
					}
					if (xIndex != getXLength()) {
						sb.append("　");
					}
				}
				sb.append(System.lineSeparator());
				if (yIndex != getYLength()) {
					sb.append("　");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(masu[yIndex][xIndex] == Masu.BLACK ? "＼"
								: masu[yIndex][xIndex] == Masu.NOT_BLACK ? "／" : "　");
						sb.append("　");
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
		 * 数字指定のあるマスの周囲の自分に向いてる個数を数え、確定する箇所は埋める。
		 * 矛盾したらfalseを返す。
		 */
		public boolean aroundSolve() {
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (extraNumbers[yIndex][xIndex] != null && extraNumbers[yIndex][xIndex] != -1) {
						int yesCnt = 0;
						int noCnt = 0;
						Masu masuUpRight = yIndex == 0 || xIndex == getXLength() ? Masu.BLACK
								: masu[yIndex - 1][xIndex];
						if (masuUpRight == Masu.BLACK) {
							noCnt++;
						} else if (masuUpRight == Masu.NOT_BLACK) {
							yesCnt++;
						}
						Masu masuRightDown = xIndex == getXLength() || yIndex == getYLength() ? Masu.NOT_BLACK
								: masu[yIndex][xIndex];
						if (masuRightDown == Masu.BLACK) {
							yesCnt++;
						} else if (masuRightDown == Masu.NOT_BLACK) {
							noCnt++;
						}
						Masu masuDownLeft = yIndex == getYLength() || xIndex == 0 ? Masu.BLACK
								: masu[yIndex][xIndex - 1];
						if (masuDownLeft == Masu.BLACK) {
							noCnt++;
						} else if (masuDownLeft == Masu.NOT_BLACK) {
							yesCnt++;
						}
						Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex - 1];
						if (masuLeftUp == Masu.BLACK) {
							yesCnt++;
						} else if (masuLeftUp == Masu.NOT_BLACK) {
							noCnt++;
						}
						if (extraNumbers[yIndex][xIndex] < yesCnt) {
							// 自分向き過剰
							return false;
						}
						if (extraNumbers[yIndex][xIndex] > 4 - noCnt) {
							// 自分向き不足
							return false;
						}
						if (extraNumbers[yIndex][xIndex] == yesCnt) {
							if (masuUpRight == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.BLACK;
							}
							if (masuRightDown == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
							if (masuDownLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.BLACK;
							}
							if (masuLeftUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
						if (extraNumbers[yIndex][xIndex] == 4 - noCnt) {
							if (masuUpRight == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masuRightDown == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
							if (masuDownLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
							if (masuLeftUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex - 1] = Masu.BLACK;
							}
						}

					}
				}
			}
			return true;
		}

		/**
		 * 確定斜めがループしたらfalseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> resolvedPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (!resolvedPosSet.contains(pos)) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						if (!setContinuePosSet(pos, continuePosSet, null)) {
							return false;
						} else {
							resolvedPosSet.addAll(continuePosSet);
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に斜めにつなぐ。元のマスに戻ってきたらfalseを返す。
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			// 右上
			if (pos.getyIndex() != 0 && pos.getxIndex() != getXLength()
					&& masu[pos.getyIndex() - 1][pos.getxIndex()] == Masu.NOT_BLACK && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				} else {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.DOWN)) {
						return false;
					}
				}
			}
			// 右下
			if (pos.getyIndex() != getYLength() && pos.getxIndex() != getXLength()
					&& masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() + 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				} else {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.LEFT)) {
						return false;
					}
				}
			}
			// 左下
			if (pos.getyIndex() != getYLength() && pos.getxIndex() != 0
					&& masu[pos.getyIndex()][pos.getxIndex() - 1] == Masu.NOT_BLACK && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				} else {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.UP)) {
						return false;
					}
				}
			}
			// 左上
			if (pos.getyIndex() != 0 && pos.getxIndex() != 0
					&& masu[pos.getyIndex() - 1][pos.getxIndex() - 1] == Masu.BLACK && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() - 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				} else {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
					}
				}
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
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!aroundSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}
	}

	private final Field field;
	private int count;

	public GokigenSolver(int height, int width, String param) {
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
		System.out.println(new GokigenSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 5).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					//					// 周囲に空白が少ない＆ヒントが多い個所を優先して調査
					//					Masu masuUp = yIndex == 0 ? Masu.BLACK
					//							: field.masu[yIndex - 1][xIndex];
					//					Masu masuRight = xIndex == field.getXLength() - 1 ? Masu.BLACK
					//							: field.masu[yIndex][xIndex + 1];
					//					Masu masuDown = yIndex == field.getYLength() - 1 ? Masu.BLACK
					//							: field.masu[yIndex + 1][xIndex];
					//					Masu masuLeft = xIndex == 0 ? Masu.BLACK
					//							: field.masu[yIndex][xIndex - 1];
					//					int whiteCnt = 0;
					//					if (masuUp == Masu.SPACE) {
					//						whiteCnt++;
					//					}
					//					if (masuRight == Masu.SPACE) {
					//						whiteCnt++;
					//					}
					//					if (masuDown == Masu.SPACE) {
					//						whiteCnt++;
					//					}
					//					if (masuLeft == Masu.SPACE) {
					//						whiteCnt++;
					//					}
					//					Integer numberUpRight = field.extraNumbers[yIndex][xIndex + 1];
					//					Integer numberRightDown = field.extraNumbers[yIndex + 1][xIndex + 1];
					//					Integer numberDownLeft = field.extraNumbers[yIndex + 1][xIndex];
					//					Integer numberLeftUp = field.extraNumbers[yIndex][xIndex];
					//					int noHintCnt = 0;
					//					if (numberUpRight == null || numberUpRight == -1) {
					//						noHintCnt++;
					//					}
					//					if (numberRightDown == null || numberRightDown == -1) {
					//						noHintCnt++;
					//					}
					//					if (numberDownLeft == null || numberDownLeft == -1) {
					//						noHintCnt++;
					//					}
					//					if (numberLeftUp == null || numberLeftUp == -1) {
					//						noHintCnt++;
					//					}
					//					if (noHintCnt + whiteCnt > 6) {
					//						// 4だと解けない問題が出ることがある。5と6はかなり微妙で問題により速度差が出る。
					//						continue;
					//					}
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

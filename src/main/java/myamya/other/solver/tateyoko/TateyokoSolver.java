package myamya.other.solver.tateyoko;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class TateyokoSolver implements Solver {

	public static class Field {
		// マスの情報
		// ここでは、白マスを─、黒マスを│として扱う。
		private Masu[][] masu;
		// 数字の情報
		private final Integer[][] numbers;
		// 黒マスの位置
		private final Set<Position> blackPosSet;

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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			blackPosSet = new HashSet<>();
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == 'n') {
					// 白マス1つ
					index++;
				} else if (ch == 'i') {
					// 連続白マス
					i++;
					int interval = Character.getNumericValue(param.charAt(i));
					if (interval != -1) {
						index = index + interval;
					}
				} else if (ch == 'o' || ch == 'p' || ch == 'q' || ch == 'r' || ch == 's' || ch == 'x') {
					// 黒マス1つ
					blackPosSet.add(pos);
					if (ch == 'x') {
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else if (ch == 'o') {
						numbers[pos.getyIndex()][pos.getxIndex()] = 0;
					} else if (ch == 'p') {
						numbers[pos.getyIndex()][pos.getxIndex()] = 1;
					} else if (ch == 'q') {
						numbers[pos.getyIndex()][pos.getxIndex()] = 2;
					} else if (ch == 'r') {
						numbers[pos.getyIndex()][pos.getxIndex()] = 3;
					} else if (ch == 's') {
						numbers[pos.getyIndex()][pos.getxIndex()] = 4;
					}
					index++;
				} else {
					// 数字マス
					//16 - 255は '-'
					int capacity;
					if (ch == '.') {
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			blackPosSet = other.blackPosSet;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";
		private static final String FULL_NUMS_KANJI = "×ＡＢＣＤ";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE && numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						} else if (numbers[yIndex][xIndex] == -1) {
							sb.append("？");
						} else {
							String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(capacityStr);
							if (index >= 0) {
								if (blackPosSet.contains(new Position(yIndex, xIndex))) {
									sb.append(FULL_NUMS_KANJI.substring(index / 2, index / 2 + 1));
								} else {
									sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
								}
							} else {
								sb.append(capacityStr);
							}
						}
					} else {
						sb.append(masu[yIndex][xIndex] == Masu.BLACK ? "│"
								: masu[yIndex][xIndex] == Masu.NOT_BLACK ? "─" : "　");
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
			if (!numberSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 黒マスの数字は横に隣接する白マス+縦に隣接する黒マスの数を示す。
		 * 白マスの数字は横に連続する白マスか縦に連続する黒マスの数を示す
		 * 矛盾する場合falseを返す。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						if (blackPosSet.contains(new Position(yIndex, xIndex))) {
							int yesCnt = 0;
							int noCnt = 0;
							Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK
									: blackPosSet.contains(new Position(yIndex - 1, xIndex)) ? Masu.NOT_BLACK
											: masu[yIndex - 1][xIndex];
							if (masuUp == Masu.BLACK) {
								yesCnt++;
							} else if (masuUp == Masu.NOT_BLACK) {
								noCnt++;
							}
							Masu masuRight = xIndex == getXLength() - 1 ? Masu.BLACK
									: blackPosSet.contains(new Position(yIndex, xIndex + 1)) ? Masu.BLACK
											: masu[yIndex][xIndex + 1];
							if (masuRight == Masu.BLACK) {
								noCnt++;
							} else if (masuRight == Masu.NOT_BLACK) {
								yesCnt++;
							}
							Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK
									: blackPosSet.contains(new Position(yIndex + 1, xIndex)) ? Masu.NOT_BLACK
											: masu[yIndex + 1][xIndex];
							if (masuDown == Masu.BLACK) {
								yesCnt++;
							} else if (masuDown == Masu.NOT_BLACK) {
								noCnt++;
							}
							Masu masuLeft = xIndex == 0 ? Masu.BLACK
									: blackPosSet.contains(new Position(yIndex, xIndex - 1)) ? Masu.BLACK
											: masu[yIndex][xIndex - 1];
							if (masuLeft == Masu.BLACK) {
								noCnt++;
							} else if (masuLeft == Masu.NOT_BLACK) {
								yesCnt++;
							}
							if (numbers[yIndex][xIndex] < yesCnt) {
								// 自分向き過剰
								return false;
							}
							if (numbers[yIndex][xIndex] > 4 - noCnt) {
								// 自分向き不足
								return false;
							}
							if (numbers[yIndex][xIndex] == yesCnt) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.BLACK;
								}
							}
							if (numbers[yIndex][xIndex] == 4 - noCnt) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
								}
							}
						} else {
							int canConnect = numbers[yIndex][xIndex] - 1;
							int fixUpCnt = 0;
							int upCnt = 0;
							boolean fixUpContinue = true;
							boolean fixUpReachNumber = false;
							for (int candY = yIndex - 1; candY >= 0; candY--) {
								if (masu[candY][xIndex] == Masu.NOT_BLACK
										|| blackPosSet.contains(new Position(candY, xIndex))) {
									break;
								}
								if (masu[candY][xIndex] == Masu.BLACK && fixUpContinue) {
									fixUpCnt++;
									if (numbers[candY][xIndex] != null) {
										fixUpReachNumber = true;
									}
								} else {
									fixUpContinue = false;
								}
								upCnt++;
							}
							int fixRightCnt = 0;
							int rightCnt = 0;
							boolean fixRightContinue = true;
							boolean fixRightReachNumber = false;
							for (int candX = xIndex + 1; candX < getXLength(); candX++) {
								if (masu[yIndex][candX] == Masu.BLACK
										|| blackPosSet.contains(new Position(yIndex, candX))) {
									break;
								}
								if (masu[yIndex][candX] == Masu.NOT_BLACK && fixRightContinue) {
									fixRightCnt++;
									if (numbers[yIndex][candX] != null) {
										fixRightReachNumber = true;
									}
								} else {
									fixRightContinue = false;
								}
								rightCnt++;
							}
							int fixDownCnt = 0;
							int downCnt = 0;
							boolean fixDownContinue = true;
							boolean fixDownReachNumber = false;
							for (int candY = yIndex + 1; candY < getYLength(); candY++) {
								if (masu[candY][xIndex] == Masu.NOT_BLACK
										|| blackPosSet.contains(new Position(candY, xIndex))) {
									break;
								}
								if (masu[candY][xIndex] == Masu.BLACK && fixDownContinue) {
									fixDownCnt++;
									if (numbers[candY][xIndex] != null) {
										fixDownReachNumber = true;
									}
								} else {
									fixDownContinue = false;
								}
								downCnt++;
							}
							int fixLeftCnt = 0;
							int leftCnt = 0;
							boolean fixLeftContinue = true;
							boolean fixLeftReachNumber = false;
							for (int candX = xIndex - 1; candX >= 0; candX--) {
								if (masu[yIndex][candX] == Masu.BLACK
										|| blackPosSet.contains(new Position(yIndex, candX))) {
									break;
								}
								if (masu[yIndex][candX] == Masu.NOT_BLACK && fixLeftContinue) {
									fixLeftCnt++;
									if (numbers[yIndex][candX] != null) {
										fixLeftReachNumber = true;
									}
								} else {
									fixLeftContinue = false;
								}
								leftCnt++;
							}
							// 必ず縦につながる数
							int varticalFix = fixUpCnt + fixDownCnt;
							// 必ず横につながる数
							int horizonalFix = fixRightCnt + fixLeftCnt;
							// 縦につながることができる数
							int varticalCapacity = upCnt + downCnt;
							// 横につながることができる数
							int horizonalCapacity = rightCnt + leftCnt;
							// 縦につながると別の数字とぶつかる
							boolean varticalReachNumber = fixUpReachNumber || fixDownReachNumber;
							// 横につながると別の数字とぶつかる
							boolean horizonalReachNumber = fixRightReachNumber || fixLeftReachNumber;
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								if (canConnect > varticalCapacity
										&& canConnect > horizonalCapacity) {
									return false;
								}
								if (canConnect < varticalFix
										&& canConnect < horizonalFix) {
									return false;
								}
								if (varticalReachNumber
										&& horizonalReachNumber) {
									return false;
								}
								if (canConnect > horizonalCapacity || canConnect < horizonalFix
										|| horizonalReachNumber) {
									masu[yIndex][xIndex] = Masu.BLACK;
								}
								if (canConnect > varticalCapacity || canConnect < varticalFix || varticalReachNumber) {
									masu[yIndex][xIndex] = Masu.NOT_BLACK;
								}
							}
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								if (canConnect > varticalCapacity || canConnect < varticalFix || varticalReachNumber) {
									return false;
								}
								// 伸びる方向の確定処理
								int up = canConnect - downCnt;
								int cnt = 0;
								while (true) {
									if (cnt >= up) {
										break;
									}
									masu[yIndex - 1 - cnt][xIndex] = Masu.BLACK;
									cnt++;
								}
								int down = canConnect - upCnt;
								cnt = 0;
								while (true) {
									if (cnt >= down) {
										break;
									}
									masu[yIndex + 1 + cnt][xIndex] = Masu.BLACK;
									cnt++;
								}
							} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								if (canConnect > horizonalCapacity || canConnect < horizonalFix
										|| horizonalReachNumber) {
									return false;
								}
								// 伸びる方向の確定処理
								int right = canConnect - leftCnt;
								int cnt = 0;
								while (true) {
									if (cnt >= right) {
										break;
									}
									masu[yIndex][xIndex + 1 + cnt] = Masu.NOT_BLACK;
									cnt++;
								}
								int left = canConnect - rightCnt;
								cnt = 0;
								while (true) {
									if (cnt >= left) {
										break;
									}
									masu[yIndex][xIndex - 1 - cnt] = Masu.NOT_BLACK;
									cnt++;
								}

							}
						}
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE && !blackPosSet.contains(new Position(yIndex, xIndex))) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count;

	public TateyokoSolver(int height, int width, String param) {
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
		System.out.println(new TateyokoSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				System.out.println(field);
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				if (!candSolve(field, recursiveCnt)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				System.out.println(field);
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
				if (field.masu[yIndex][xIndex] == Masu.SPACE
						&& !field.blackPosSet.contains(new Position(yIndex, xIndex))) {
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
package myamya.other.solver.walllogic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class WalllogicSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の候補情報
		// ここでは、0何も置かない、1↑、2→、3↓、4←として扱う。
		protected List<Integer>[][] numbersCand;
		// 固定数字の情報
		private final Integer[][] numbers;
		// 黒マスの位置
		private final Set<Position> blackPosSet;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int i = 0; i <= 4; i++) {
						numbersCand[yIndex][xIndex].add(i);
					}
				}
			}
			blackPosSet = new HashSet<>();
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
						numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else if (ch == '+') {
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
						blackPosSet.add(pos);
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			blackPosSet = other.blackPosSet;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (blackPosSet.contains(new Position(yIndex, xIndex))) {
						sb.append("■");
					} else if (numbers[yIndex][xIndex] != null) {
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
					} else if (numbersCand[yIndex][xIndex].isEmpty()) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						int num = numbersCand[yIndex][xIndex].get(0);
						sb.append(num == 1 ? "↑" : num == 2 ? "→" : num == 3 ? "↓" : num == 4 ? "←" : "・");
					} else {
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
					sb.append(numbersCand[yIndex][xIndex].size());
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
			if (!aloneSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 伸ばしても数字につながらない方向を消す。
		 */
		private boolean aloneSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						boolean notUp = true;
						boolean notRight = true;
						boolean notDown = true;
						boolean notLeft = true;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (numbers[targetY][xIndex] != null) {
								notDown = false;
								break;
							}
							if (!numbersCand[targetY][xIndex].contains(3)) {
								break;
							}
						}
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (numbers[yIndex][targetX] != null) {
								notLeft = false;
								break;
							}
							if (!numbersCand[yIndex][targetX].contains(4)) {
								break;
							}
						}
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (numbers[targetY][xIndex] != null) {
								notUp = false;
								break;
							}
							if (!numbersCand[targetY][xIndex].contains(1)) {
								break;
							}
						}
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (numbers[yIndex][targetX] != null) {
								notRight = false;
								break;
							}
							if (!numbersCand[yIndex][targetX].contains(2)) {
								break;
							}
						}
						if (notUp) {
							numbersCand[yIndex][xIndex].remove(new Integer(1));
						}
						if (notRight) {
							numbersCand[yIndex][xIndex].remove(new Integer(2));
						}
						if (notDown) {
							numbersCand[yIndex][xIndex].remove(new Integer(3));
						}
						if (notLeft) {
							numbersCand[yIndex][xIndex].remove(new Integer(4));
						}
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字は縦につながるマスと横につながるマスの合計になる。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						int upSpaceCnt = 0;
						int rightSpaceCnt = 0;
						int downSpaceCnt = 0;
						int leftSpaceCnt = 0;

						int upWhiteCnt = 0;
						int rightWhiteCnt = 0;
						int downWhiteCnt = 0;
						int leftWhiteCnt = 0;

						boolean upCounting = true;
						boolean rightCounting = true;
						boolean downCounting = true;
						boolean leftCounting = true;

						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (!numbersCand[targetY][xIndex].contains(1)) {
								break;
							}
							if (numbersCand[targetY][xIndex].size() != 1) {
								upCounting = false;
							}
							if (upCounting) {
								upWhiteCnt++;
							}
							upSpaceCnt++;
						}
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (!numbersCand[yIndex][targetX].contains(2)) {
								break;
							}
							if (numbersCand[yIndex][targetX].size() != 1) {
								rightCounting = false;
							}
							if (rightCounting) {
								rightWhiteCnt++;
							}
							rightSpaceCnt++;
						}
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (!numbersCand[targetY][xIndex].contains(3)) {
								break;
							}
							if (numbersCand[targetY][xIndex].size() != 1) {
								downCounting = false;
							}
							if (downCounting) {
								downWhiteCnt++;
							}
							downSpaceCnt++;
						}
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (!numbersCand[yIndex][targetX].contains(4)) {
								break;
							}
							if (numbersCand[yIndex][targetX].size() != 1) {
								leftCounting = false;
							}
							if (leftCounting) {
								leftWhiteCnt++;
							}
							leftSpaceCnt++;
						}
						int aroundSpaceCount = upSpaceCnt + downSpaceCnt + rightSpaceCnt + leftSpaceCnt;
						int aroundWhiteCnt = upWhiteCnt + downWhiteCnt + rightWhiteCnt + leftWhiteCnt;
						if (aroundSpaceCount < numbers[yIndex][xIndex]) {
							// 足りない
							return false;
						}
						if (aroundWhiteCnt > numbers[yIndex][xIndex]) {
							// 伸ばしすぎ
							return false;
						}
						// 確実に伸ばす数
						int fixedWhiteUp = numbers[yIndex][xIndex]
								- (rightSpaceCnt + downSpaceCnt + leftSpaceCnt);
						int fixedWhiteRight = numbers[yIndex][xIndex]
								- (upSpaceCnt + downSpaceCnt + leftSpaceCnt);
						int fixedWhiteDown = numbers[yIndex][xIndex]
								- (upSpaceCnt + rightSpaceCnt + leftSpaceCnt);
						int fixedWhiteLeft = numbers[yIndex][xIndex]
								- (upSpaceCnt + rightSpaceCnt + downSpaceCnt);
						if (fixedWhiteUp > 0) {
							for (int i = 1; i <= fixedWhiteUp; i++) {
								numbersCand[yIndex - i][xIndex].remove(new Integer(0));
								numbersCand[yIndex - i][xIndex].remove(new Integer(2));
								numbersCand[yIndex - i][xIndex].remove(new Integer(3));
								numbersCand[yIndex - i][xIndex].remove(new Integer(4));
								if (numbersCand[yIndex - i][xIndex].isEmpty()) {
									return false;
								}
							}
						}
						if (fixedWhiteRight > 0) {
							for (int i = 1; i <= fixedWhiteRight; i++) {
								numbersCand[yIndex][xIndex + i].remove(new Integer(0));
								numbersCand[yIndex][xIndex + i].remove(new Integer(1));
								numbersCand[yIndex][xIndex + i].remove(new Integer(3));
								numbersCand[yIndex][xIndex + i].remove(new Integer(4));
								if (numbersCand[yIndex][xIndex + i].isEmpty()) {
									return false;
								}
							}
						}
						if (fixedWhiteDown > 0) {
							for (int i = 1; i <= fixedWhiteDown; i++) {
								numbersCand[yIndex + i][xIndex].remove(new Integer(0));
								numbersCand[yIndex + i][xIndex].remove(new Integer(1));
								numbersCand[yIndex + i][xIndex].remove(new Integer(2));
								numbersCand[yIndex + i][xIndex].remove(new Integer(4));
								if (numbersCand[yIndex + i][xIndex].isEmpty()) {
									return false;
								}

							}
						}
						if (fixedWhiteLeft > 0) {
							for (int i = 1; i <= fixedWhiteLeft; i++) {
								numbersCand[yIndex][xIndex - i].remove(new Integer(0));
								numbersCand[yIndex][xIndex - i].remove(new Integer(1));
								numbersCand[yIndex][xIndex - i].remove(new Integer(2));
								numbersCand[yIndex][xIndex - i].remove(new Integer(3));
								if (numbersCand[yIndex][xIndex - i].isEmpty()) {
									return false;
								}
							}
						}
						if (aroundWhiteCnt == numbers[yIndex][xIndex]) {
							if (yIndex - upWhiteCnt - 1 >= 0) {
								numbersCand[yIndex - upWhiteCnt - 1][xIndex].remove(new Integer(1));
								if (numbersCand[yIndex - upWhiteCnt - 1][xIndex].isEmpty()) {
									return false;
								}
							}
							if (xIndex + rightWhiteCnt + 1 < getXLength()) {
								numbersCand[yIndex][xIndex + rightWhiteCnt + 1].remove(new Integer(2));
								if (numbersCand[yIndex][xIndex + rightWhiteCnt + 1].isEmpty()) {
									return false;
								}
							}
							if (yIndex + downWhiteCnt + 1 < getYLength()) {
								numbersCand[yIndex + downWhiteCnt + 1][xIndex].remove(new Integer(3));
								if (numbersCand[yIndex + downWhiteCnt + 1][xIndex].isEmpty()) {
									return false;
								}
							}
							if (xIndex - leftWhiteCnt - 1 >= 0) {
								numbersCand[yIndex][xIndex - leftWhiteCnt - 1].remove(new Integer(4));
								if (numbersCand[yIndex][xIndex - leftWhiteCnt - 1].isEmpty()) {
									return false;
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
					if (numbersCand[yIndex][xIndex].size() != 1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count;

	public WalllogicSolver(int height, int width, String param) {
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
		System.out.println(new WalllogicSolver(height, width, param).solve());
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
			if (field.getStateDump().equals(befStr)) {
				if (!candSolve(field, 0)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				if (field.getStateDump().equals(befStr)) {
					if (!candSolve(field, 1)) {
						System.out.println(field);
						return "問題に矛盾がある可能性があります。途中経過を返します。";
					}
					if (field.getStateDump().equals(befStr)) {
						if (!candSolve(field, 2)) {
							System.out.println(field);
							return "問題に矛盾がある可能性があります。途中経過を返します。";
						}
						if (field.getStateDump().equals(befStr)) {
							System.out.println(field);
							return "解けませんでした。途中経過を返します。";
						}
					}
				}
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count * 20));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 20).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		while (true) {
			String befStr = field.getStateDump();
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.numbersCand[yIndex][xIndex].size() != 1) {
						for (Iterator<Integer> iterator = field.numbersCand[yIndex][xIndex].iterator(); iterator
								.hasNext();) {
							count++;
							int oneCand = iterator.next();
							Field virtual = new Field(field);
							virtual.numbersCand[yIndex][xIndex].clear();
							virtual.numbersCand[yIndex][xIndex].add(oneCand);
							boolean arrowCand = virtual.solveAndCheck();
							if (arrowCand && recursive > 0) {
								arrowCand = candSolve(virtual, recursive - 1);
							}
							if (!arrowCand) {
								iterator.remove();
							}
						}
						if (field.numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
					if (field.numbersCand[yIndex][xIndex].size() == 1) {
						if (!field.solveAndCheck()) {
							return false;
						}
					}
				}
			}
			if (field.getStateDump().equals(befStr)) {
				break;
			}
		}
		return true;
	}
}
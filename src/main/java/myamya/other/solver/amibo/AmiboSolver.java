package myamya.other.solver.amibo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class AmiboSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 固定数字(表示用)
		private final Integer[][] numbers;
		// マスの候補情報
		// 0=なし、1=縦棒、2=横棒、3=十字
		protected List<Integer>[][] masuCand;

		public List<Integer>[][] getNumbersCand() {
			return masuCand;
		}

		public int getYLength() {
			return masuCand.length;
		}

		public int getXLength() {
			return masuCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			masuCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masuCand[yIndex][xIndex] = new ArrayList<>();
					for (int number = 0; number <= 3; number++) {
						masuCand[yIndex][xIndex].add(number);
					}
				}
			}
			String useAlphabet = ALPHABET;
			int readPos = 0;
			int index = 0;
			if (param.charAt(0) == '-') {
				readPos++;
				useAlphabet = ALPHABET_FROM_G;
			}
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = useAlphabet.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int num;
					if (ch == '.') {
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else {
						if (ch == '-') {
							num = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							num = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							num = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
						masuCand[pos.getyIndex()][pos.getxIndex()].clear();
						masuCand[pos.getyIndex()][pos.getxIndex()].add(0);
					}
					index++;
				}
			}

		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			masuCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masuCand[yIndex][xIndex] = new ArrayList<>(other.masuCand[yIndex][xIndex]);
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
						String numStr = String.valueOf(numbers[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numStr);
						if (index == -1) {
							sb.append(numStr);
						} else {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						}
					} else {
						if (masuCand[yIndex][xIndex].size() == 0) {
							sb.append("×");
						} else if (masuCand[yIndex][xIndex].size() == 1) {
							int number = masuCand[yIndex][xIndex].get(0);
							sb.append(number == 0 ? "・"
									: number == 1 ? "│" : number == 2 ? "─" : number == 3 ? "┼" : "　");
						} else {
							sb.append("　");
						}
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
					sb.append(masuCand[yIndex][xIndex].size());
				}
			}
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!numberSolve()) {
				return false;
			}
			if (!sameSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!loopSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 棒でループができてはならない。
		 */
		private boolean loopSolve() {
			// TODO 自動生成されたメソッド・スタブ
			return true;
		}

		/**
		 * 棒は必ず同じ長さの棒と交わる。
		 */
		private boolean sameSolve() {
			// TODO 自動生成されたメソッド・スタブ
			return true;
		}

		/**
		 * ○にはただ一つの棒がつながる。0本だったり2本以上つながる場合はfalseを返す。
		 * また、1本つなぐつなぎ方が1箇所しかなければ他の候補は消す。
		 */
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						Masu masuUp, masuRight, masuDown, masuLeft;
						if (yIndex == 0) {
							masuUp = Masu.NOT_BLACK;
						} else {
							List<Integer> oneCand = masuCand[yIndex - 1][xIndex];
							boolean canConnect = false, canCat = false;
							if (oneCand.contains(0) || oneCand.contains(2)) {
								canCat = true;
							}
							if (oneCand.contains(1) || oneCand.contains(3)) {
								canConnect = true;
							}
							if (canCat && canConnect) {
								masuUp = Masu.SPACE;
							} else if (canCat && !canConnect) {
								masuUp = Masu.NOT_BLACK;
							} else if (!canCat && canConnect) {
								masuUp = Masu.BLACK;
							} else {
								return false;
							}
						}
						if (xIndex == getXLength() - 1) {
							masuRight = Masu.NOT_BLACK;
						} else {
							List<Integer> oneCand = masuCand[yIndex][xIndex + 1];
							boolean canConnect = false, canCat = false;
							if (oneCand.contains(0) || oneCand.contains(1)) {
								canCat = true;
							}
							if (oneCand.contains(2) || oneCand.contains(3)) {
								canConnect = true;
							}
							if (canCat && canConnect) {
								masuRight = Masu.SPACE;
							} else if (canCat && !canConnect) {
								masuRight = Masu.NOT_BLACK;
							} else if (!canCat && canConnect) {
								masuRight = Masu.BLACK;
							} else {
								return false;
							}
						}
						if (yIndex == getYLength() - 1) {
							masuDown = Masu.NOT_BLACK;
						} else {
							List<Integer> oneCand = masuCand[yIndex + 1][xIndex];
							boolean canConnect = false, canCat = false;
							if (oneCand.contains(0) || oneCand.contains(2)) {
								canCat = true;
							}
							if (oneCand.contains(1) || oneCand.contains(3)) {
								canConnect = true;
							}
							if (canCat && canConnect) {
								masuDown = Masu.SPACE;
							} else if (canCat && !canConnect) {
								masuDown = Masu.NOT_BLACK;
							} else if (!canCat && canConnect) {
								masuDown = Masu.BLACK;
							} else {
								return false;
							}
						}
						if (xIndex == 0) {
							masuLeft = Masu.NOT_BLACK;
						} else {
							List<Integer> oneCand = masuCand[yIndex][xIndex - 1];
							boolean canConnect = false, canCat = false;
							if (oneCand.contains(0) || oneCand.contains(1)) {
								canCat = true;
							}
							if (oneCand.contains(2) || oneCand.contains(3)) {
								canConnect = true;
							}
							if (canCat && canConnect) {
								masuLeft = Masu.SPACE;
							} else if (canCat && !canConnect) {
								masuLeft = Masu.NOT_BLACK;
							} else if (!canCat && canConnect) {
								masuLeft = Masu.BLACK;
							} else {
								return false;
							}
						}
						// 一つもつながらない
						if (masuUp == Masu.NOT_BLACK && masuRight == Masu.NOT_BLACK && masuDown == Masu.NOT_BLACK
								&& masuLeft == Masu.NOT_BLACK) {
							return false;
						}
						// 複数つながる
						if ((masuUp == Masu.BLACK && masuRight == Masu.BLACK) ||
								(masuUp == Masu.BLACK && masuDown == Masu.BLACK)
								|| (masuUp == Masu.BLACK && masuLeft == Masu.BLACK)
								|| (masuRight == Masu.BLACK && masuDown == Masu.BLACK)
								|| (masuRight == Masu.BLACK && masuLeft == Masu.BLACK)
								|| (masuDown == Masu.BLACK && masuLeft == Masu.BLACK)) {
							return false;
						}
						// 1箇所つながってたら3箇所はつながらない
						if (masuUp == Masu.BLACK || masuRight == Masu.BLACK || masuDown == Masu.BLACK
								|| masuLeft == Masu.BLACK) {
							if (masuUp == Masu.SPACE) {
								masuCand[yIndex - 1][xIndex].remove(new Integer(1));
								masuCand[yIndex - 1][xIndex].remove(new Integer(3));
							}
							if (masuRight == Masu.SPACE) {
								masuCand[yIndex][xIndex + 1].remove(new Integer(2));
								masuCand[yIndex][xIndex + 1].remove(new Integer(3));
							}
							if (masuDown == Masu.SPACE) {
								masuCand[yIndex + 1][xIndex].remove(new Integer(1));
								masuCand[yIndex + 1][xIndex].remove(new Integer(3));
							}
							if (masuLeft == Masu.SPACE) {
								masuCand[yIndex][xIndex - 1].remove(new Integer(2));
								masuCand[yIndex][xIndex - 1].remove(new Integer(3));
							}
						}
						// 3箇所つながってなかったら1箇所はつながる
						if (masuUp == Masu.SPACE && masuRight == Masu.NOT_BLACK && masuDown == Masu.NOT_BLACK
								&& masuLeft == Masu.NOT_BLACK) {
							masuCand[yIndex - 1][xIndex].remove(new Integer(0));
							masuCand[yIndex - 1][xIndex].remove(new Integer(2));
						}
						if (masuUp == Masu.NOT_BLACK && masuRight == Masu.SPACE && masuDown == Masu.NOT_BLACK
								&& masuLeft == Masu.NOT_BLACK) {
							masuCand[yIndex][xIndex + 1].remove(new Integer(0));
							masuCand[yIndex][xIndex + 1].remove(new Integer(1));
						}
						if (masuUp == Masu.NOT_BLACK && masuRight == Masu.NOT_BLACK && masuDown == Masu.SPACE
								&& masuLeft == Masu.NOT_BLACK) {
							masuCand[yIndex + 1][xIndex].remove(new Integer(0));
							masuCand[yIndex + 1][xIndex].remove(new Integer(2));
						}
						if (masuUp == Masu.NOT_BLACK && masuRight == Masu.NOT_BLACK && masuDown == Masu.NOT_BLACK
								&& masuLeft == Masu.SPACE) {
							masuCand[yIndex][xIndex - 1].remove(new Integer(0));
							masuCand[yIndex][xIndex - 1].remove(new Integer(1));
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字付きの丸はその数だけ直線が伸びる。
		 * 伸ばせない場合はfalseを返す。
		 * 伸ばせる方向が確定した場合は伸ばす。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						int number = numbers[yIndex][xIndex];

						int upBlackCnt = 0;
						int upSpaceCnt = 0;
						boolean blackContinue = true;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (!masuCand[targetY][xIndex].contains(1) && !masuCand[targetY][xIndex].contains(3)) {
								break;
							}
							if (masuCand[targetY][xIndex].contains(0) || masuCand[targetY][xIndex].contains(2)) {
								blackContinue = false;
							}
							if (blackContinue) {
								upBlackCnt++;
							}
							upSpaceCnt++;
						}
						int rightBlackCnt = 0;
						int rightSpaceCnt = 0;
						blackContinue = true;
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (!masuCand[yIndex][targetX].contains(2) && !masuCand[yIndex][targetX].contains(3)) {
								break;
							}
							if (masuCand[yIndex][targetX].contains(0) || masuCand[yIndex][targetX].contains(1)) {
								blackContinue = false;
							}
							if (blackContinue) {
								rightBlackCnt++;
							}
							rightSpaceCnt++;
						}
						int downBlackCnt = 0;
						int downSpaceCnt = 0;
						blackContinue = true;
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (!masuCand[targetY][xIndex].contains(1) && !masuCand[targetY][xIndex].contains(3)) {
								break;
							}
							if (masuCand[targetY][xIndex].contains(0) || masuCand[targetY][xIndex].contains(2)) {
								blackContinue = false;
							}
							if (blackContinue) {
								downBlackCnt++;
							}
							downSpaceCnt++;
						}
						int leftBlackCnt = 0;
						int leftSpaceCnt = 0;
						blackContinue = true;
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (!masuCand[yIndex][targetX].contains(2) && !masuCand[yIndex][targetX].contains(3)) {
								break;
							}
							if (masuCand[yIndex][targetX].contains(0) || masuCand[yIndex][targetX].contains(1)) {
								blackContinue = false;
							}
							if (blackContinue) {
								leftBlackCnt++;
							}
							leftSpaceCnt++;
						}
						// 伸びる棒が長すぎる
						if (number < upBlackCnt || number < rightBlackCnt || number < downBlackCnt
								|| number < leftBlackCnt) {
							return false;
						}
						// 棒が伸びなさすぎる
						if (number > upSpaceCnt && number > rightSpaceCnt && number > downSpaceCnt
								&& number > leftSpaceCnt) {
							return false;
						}
						// 伸びる方向が確定
						if (number > rightSpaceCnt && number > downSpaceCnt
								&& number > leftSpaceCnt) {
							for (int targetY = yIndex - 1; targetY >= yIndex - number; targetY--) {
								masuCand[targetY][xIndex].remove(new Integer(0));
								masuCand[targetY][xIndex].remove(new Integer(2));
								if (masuCand[targetY][xIndex].isEmpty()) {
									return false;
								}
							}
							if (yIndex - number - 1 >= 0) {
								masuCand[yIndex - number - 1][xIndex].remove(new Integer(1));
								masuCand[yIndex - number - 1][xIndex].remove(new Integer(3));
								if (masuCand[yIndex - number - 1][xIndex].isEmpty()) {
									return false;
								}
							}
						}
						if (number > upSpaceCnt && number > downSpaceCnt
								&& number > leftSpaceCnt) {
							for (int targetX = xIndex + 1; targetX <= xIndex + number; targetX++) {
								masuCand[yIndex][targetX].remove(new Integer(0));
								masuCand[yIndex][targetX].remove(new Integer(1));
								if (masuCand[yIndex][targetX].isEmpty()) {
									return false;
								}
							}
							if (xIndex + number + 1 < getXLength()) {
								masuCand[yIndex][xIndex + number + 1].remove(new Integer(2));
								masuCand[yIndex][xIndex + number + 1].remove(new Integer(3));
								if (masuCand[yIndex][xIndex + number + 1].isEmpty()) {
									return false;
								}
							}
						}
						if (number > upSpaceCnt && number > rightSpaceCnt
								&& number > leftSpaceCnt) {
							for (int targetY = yIndex + 1; targetY <= yIndex + number; targetY++) {
								masuCand[targetY][xIndex].remove(new Integer(0));
								masuCand[targetY][xIndex].remove(new Integer(2));
								if (masuCand[targetY][xIndex].isEmpty()) {
									return false;
								}
							}
							if (yIndex + number + 1 < getYLength()) {
								masuCand[yIndex + number + 1][xIndex].remove(new Integer(1));
								masuCand[yIndex + number + 1][xIndex].remove(new Integer(3));
								if (masuCand[yIndex + number + 1][xIndex].isEmpty()) {
									return false;
								}
							}
						}
						if (number > upSpaceCnt && number > rightSpaceCnt && number > downSpaceCnt) {
							for (int targetX = xIndex - 1; targetX >= xIndex - number; targetX--) {
								masuCand[yIndex][targetX].remove(new Integer(0));
								masuCand[yIndex][targetX].remove(new Integer(1));
								if (masuCand[yIndex][targetX].isEmpty()) {
									return false;
								}
							}
							if (xIndex - number - 1 >= 0) {
								masuCand[yIndex][xIndex - number - 1].remove(new Integer(2));
								masuCand[yIndex][xIndex - number - 1].remove(new Integer(3));
								if (masuCand[yIndex][xIndex - number - 1].isEmpty()) {
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
					if (masuCand[yIndex][xIndex].size() != 1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public AmiboSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "http://pzv.jp/p.html?amibo/8/8/a3c.d.d..k4d.j.b.b.b5f.c"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new AmiboSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 10));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 10).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masuCand[yIndex][xIndex].size() != 1) {
					for (Iterator<Integer> iterator = field.masuCand[yIndex][xIndex].iterator(); iterator
							.hasNext();) {
						count++;
						int oneCand = iterator.next();
						Field virtual = new Field(field);
						virtual.masuCand[yIndex][xIndex].clear();
						virtual.masuCand[yIndex][xIndex].add(oneCand);
						boolean arrowCand = virtual.solveAndCheck();
						if (arrowCand && recursive > 0) {
							arrowCand = candSolve(virtual, recursive - 1);
						}
						if (!arrowCand) {
							iterator.remove();
						}
					}
					if (field.masuCand[yIndex][xIndex].size() == 0) {
						return false;
					}
				}
				if (field.masuCand[yIndex][xIndex].size() == 1) {
					if (!field.solveAndCheck()) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
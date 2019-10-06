package myamya.other.solver.building;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Solver;

public class BuildingSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の候補情報
		protected List<Integer>[][] numbersCand;

		// ヒント情報。
		protected Integer[] upHints;
		protected Integer[] downHints;
		protected Integer[] leftHints;
		protected Integer[] rightHints;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			numbersCand = new ArrayList[height][width];
			upHints = new Integer[width];
			downHints = new Integer[width];
			leftHints = new Integer[height];
			rightHints = new Integer[height];
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
						//
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
						if (index < width) {
							upHints[index] = capacity;
						} else if (index < width * 2) {
							downHints[index - width] = capacity;
						} else if (index < (width * 2 + height)) {
							leftHints[index - width * 2] = capacity;
						} else {
							rightHints[index - (width * 2 + height)] = capacity;
						}
					}
					index++;
				}
			}
			// 縦横の数字により、初期候補数字を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int i = 1; i <= getMaxNumCand(); i++) {
						numbersCand[yIndex][xIndex].add(i);
					}
				}
			}
		}

		public int getMaxNumCand() {
			return getYLength() > getXLength() ? getYLength() : getXLength();
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			upHints = other.upHints;
			downHints = other.downHints;
			leftHints = other.leftHints;
			rightHints = other.rightHints;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					String numStr = String.valueOf(upHints[xIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == 0) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
			}
			sb.append("　");
			sb.append(System.lineSeparator());
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				sb.append("↓");
			}
			sb.append("　");
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					String numStr = String.valueOf(leftHints[yIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == 0) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
				sb.append("→");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
						int index = HALF_NUMS.indexOf(numStr);
						if (index == 0) {
							sb.append("・");
						} else {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						}
					} else if (numbersCand[yIndex][xIndex].size() == 2) {
						sb.append(numbersCand[yIndex][xIndex].get(0));
						sb.append(numbersCand[yIndex][xIndex].get(1));
					} else {
						sb.append("　");
					}
				}
				sb.append("←");
				if (rightHints[yIndex] != null) {
					String numStr = String.valueOf(rightHints[yIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == 0) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
				sb.append(System.lineSeparator());
			}
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				sb.append("↑");
			}
			sb.append("　");
			sb.append(System.lineSeparator());
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (downHints[xIndex] != null) {
					String numStr = String.valueOf(downHints[xIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == 0) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
			}
			sb.append("　");
			sb.append(System.lineSeparator());

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
		 * 同じ列・行にいる数字を候補から除外する。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() != 1) {
						for (int targetY = 0; targetY < getYLength(); targetY++) {
							if (numbersCand[targetY][xIndex].size() == 1 && yIndex != targetY) {
								if (numbersCand[yIndex][xIndex].remove((numbersCand[targetY][xIndex]).get(0))) {
								}
							}
						}
						for (int targetX = 0; targetX < getXLength(); targetX++) {
							if (numbersCand[yIndex][targetX].size() == 1 && xIndex != targetX) {
								if (numbersCand[yIndex][xIndex].remove((numbersCand[yIndex][targetX]).get(0))) {
								}
							}
						}
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
					if (numbersCand[yIndex][xIndex].size() != 1) {
						for (int cand : numbersCand[yIndex][xIndex]) {
							boolean isHiddenSingle = true;
							if (getYLength() >= getXLength()) {
								for (int targetY = 0; targetY < getYLength(); targetY++) {
									if (yIndex != targetY) {
										if (numbersCand[targetY][xIndex].contains(cand)) {
											isHiddenSingle = false;
											break;
										}
									}
								}
								if (isHiddenSingle) {
									numbersCand[yIndex][xIndex].clear();
									numbersCand[yIndex][xIndex].add(cand);
									break;
								}
							}
							isHiddenSingle = true;
							if (getXLength() >= getYLength()) {
								for (int targetX = 0; targetX < getXLength(); targetX++) {
									if (xIndex != targetX) {
										if (numbersCand[yIndex][targetX].contains(cand)) {
											isHiddenSingle = false;
											break;
										}
									}
								}
								if (isHiddenSingle) {
									numbersCand[yIndex][xIndex].clear();
									numbersCand[yIndex][xIndex].add(cand);
									break;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 外周数字を見て明らかに破綻している場合falseを返す。
		 */
		private boolean hintSolve() {
			for (int x = 0; x < getXLength(); x++) {
				Integer upHint = upHints[x];
				if (upHint != null) {
					int fixMaxHeight = 0;
					int minSeeCnt = 0;
					int maxSeeCnt = 0;
					for (int y = 0; y < getYLength(); y++) {
						if (numbersCand[y][x].size() == 1) {
							if (fixMaxHeight < numbersCand[y][x].get(0)) {
								fixMaxHeight = numbersCand[y][x].get(0);
								minSeeCnt++;
								maxSeeCnt++;
							}
						} else {
							// 1ではなくなった時点で抜ける。
							if (fixMaxHeight < getYLength()) {
								// 最大の高さのビルが登場していなければ1足す
								minSeeCnt++;
							}
							// 残りマス数か許容高さのうち少ないほうを足す
							int masu = getYLength() - y;
							int height = getMaxNumCand() - fixMaxHeight;
							maxSeeCnt = maxSeeCnt + (masu < height ? masu : height);
							break;
						}
					}
					if (minSeeCnt > upHint || maxSeeCnt < upHint) {
						return false;
					}
				}
				Integer downHint = downHints[x];
				if (downHint != null) {
					int fixMaxHeight = 0;
					int minSeeCnt = 0;
					int maxSeeCnt = 0;
					for (int y = getYLength() - 1; y >= 0; y--) {
						if (numbersCand[y][x].size() == 1) {
							if (fixMaxHeight < numbersCand[y][x].get(0)) {
								fixMaxHeight = numbersCand[y][x].get(0);
								minSeeCnt++;
								maxSeeCnt++;
							}
						} else {
							// 1ではなくなった時点で抜ける。
							if (fixMaxHeight < getYLength()) {
								// 最大の高さのビルが登場していなければ1足す
								minSeeCnt++;
							}
							// 残りマス数か許容高さのうち少ないほうを足す
							int masu = y + 1;
							int height = getMaxNumCand() - fixMaxHeight;
							maxSeeCnt = maxSeeCnt + (masu < height ? masu : height);
							break;
						}
					}
					if (minSeeCnt > downHint || maxSeeCnt < downHint) {
						return false;
					}
				}
			}
			for (int y = 0; y < getYLength(); y++) {
				Integer leftHint = leftHints[y];
				if (leftHint != null) {
					int fixMaxHeight = 0;
					int minSeeCnt = 0;
					int maxSeeCnt = 0;
					for (int x = 0; x < getXLength(); x++) {
						if (numbersCand[y][x].size() == 1) {
							if (fixMaxHeight < numbersCand[y][x].get(0)) {
								fixMaxHeight = numbersCand[y][x].get(0);
								minSeeCnt++;
								maxSeeCnt++;
							}
						} else {
							// 1ではなくなった時点で抜ける。
							if (fixMaxHeight < getXLength()) {
								// 最大の高さのビルが登場していなければ1足す
								minSeeCnt++;
							}
							// 残りマス数か許容高さのうち少ないほうを足す
							int masu = getXLength() - x;
							int height = getMaxNumCand() - fixMaxHeight;
							maxSeeCnt = maxSeeCnt + (masu < height ? masu : height);
							break;
						}
					}
					if (minSeeCnt > leftHint || maxSeeCnt < leftHint) {
						return false;
					}
				}
				Integer rightHint = rightHints[y];
				if (rightHint != null) {
					int fixMaxHeight = 0;
					int minSeeCnt = 0;
					int maxSeeCnt = 0;
					for (int x = getXLength() - 1; x >= 0; x--) {
						if (numbersCand[y][x].size() == 1) {
							if (fixMaxHeight < numbersCand[y][x].get(0)) {
								fixMaxHeight = numbersCand[y][x].get(0);
								minSeeCnt++;
								maxSeeCnt++;
							}
						} else {
							// 1ではなくなった時点で抜ける。
							if (fixMaxHeight < getXLength()) {
								// 最大の高さのビルが登場していなければ1足す
								minSeeCnt++;
							}
							// 残りマス数か許容高さのうち少ないほうを足す
							int masu = x + 1;
							int height = getMaxNumCand() - fixMaxHeight;
							maxSeeCnt = maxSeeCnt + (masu < height ? masu : height);
							break;
						}
					}
					if (minSeeCnt > rightHint || maxSeeCnt < rightHint) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!hintSolve()) {
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
					if (numbersCand[yIndex][xIndex].size() != 1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public BuildingSolver(int height, int width, String param) {
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
		System.out.println(new BuildingSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		System.out.println(field);
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
		System.out.println("難易度:" + (count));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count).toString();
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
							//							Field virtual2 = new Field(field);
							//							virtual2.numbersCand[yIndex][xIndex].remove(new Integer(oneCand));
							//							boolean notArrowCand = virtual2.solveAndCheck();
							//							if (notArrowCand && recursive > 0) {
							//								notArrowCand = candSolve(virtual2, recursive - 1);
							//							}
							//							if (!notArrowCand) {
							//								field.numbersCand[yIndex][xIndex].clear();
							//								field.numbersCand[yIndex][xIndex].add(oneCand);
							//								break;
							//							}
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
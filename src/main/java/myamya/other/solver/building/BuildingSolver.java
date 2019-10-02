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
					for (int i = 1; i <= getYLength() || i <= getXLength(); i++) {
						numbersCand[yIndex][xIndex].add(i);
					}
				}
			}
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
		 * 外周数字は、その方向から順に数字を見て、そこまでの最大数字よりも
		 * 大きくなった回数を示す。それを踏まえたうえで候補の除外を行う。
		 */
		private boolean hintSolve() {
			for (int x = 0; x < upHints.length; x++) {
				Integer upHint = upHints[x];
				if (upHint != null) {
					int minmaxHeight = 0;
					int maxmaxHeight = 0;
					int seeCnt = 0;
					int notSeeCnt = 0;
					for (int y = 0; y < numbersCand[x].length; y++) {
						List<Integer> oneCand = numbersCand[x][y];
						if (upHint == seeCnt) {
							// これ以上見えてはいけない場合、maxHeightを超える候補を消す。
							for (Iterator<Integer> iterator = oneCand.iterator(); iterator.hasNext();) {
								int cand = (Integer) iterator.next();
								if (cand > maxmaxHeight) {
									iterator.remove();
								}
							}
							if (oneCand.isEmpty()) {
								return false;
							}
						}
						if (upHint == getYLength() - notSeeCnt) {
							// ここ以降全部見えなくていはいけない場合、maxheightに満たない候補を消す。
							for (Iterator<Integer> iterator = oneCand.iterator(); iterator.hasNext();) {
								int cand = (Integer) iterator.next();
								if (cand < minmaxHeight) {
									iterator.remove();
								}
							}
							if (oneCand.isEmpty()) {
								return false;
							}
						}
						// そのマスの候補の一番小さい値が現在の最大値を上回った場合、
						// 確定で見えると判定し、最大値を上書きする。
						if (minmaxHeight < oneCand.get(0)) {
							minmaxHeight = oneCand.get(0);
							maxmaxHeight = oneCand.get(oneCand.size() - 1);
							seeCnt++;
						} else if (minmaxHeight > oneCand.get(oneCand.size() - 1)) {
							// そのマスの候補の一番大きい値が現在の最大値を下回った場合、
							// 確定で見えないと判定する
							notSeeCnt++;
						}
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
		String url = "https://puzz.link/p?building/6/6/h512m32l256g"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new BuildingSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 50));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 50).toString();
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
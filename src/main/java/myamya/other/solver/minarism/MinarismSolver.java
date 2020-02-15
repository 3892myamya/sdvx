package myamya.other.solver.minarism;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class MinarismSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_I = "ijklmnopqrstuvwxyz";

		// 数字の候補情報
		protected List<Integer>[][] numbersCand;
		// 数字情報
		// -2、-1は不等号(左上向き、右下向き)
		private final int[][] yokoWallNum;
		private final int[][] tateWallNum;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public int[][] getYokoWall() {
			return yokoWallNum;
		}

		public int[][] getTateWall() {
			return tateWallNum;
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
			// パラメータを解釈して壁の有無を入れる
			yokoWallNum = new int[height][width - 1];
			tateWallNum = new int[height - 1][width];
			int readPos = 0;
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_I.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int num;
					if (ch == '.') {
						//
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
						} else if (ch == 'g') {
							num = -2;
						} else if (ch == 'h') {
							num = -1;
						} else {
							num = Integer.parseInt(String.valueOf(ch), 16);
						}
						if (index / (getXLength() - 1) < getYLength()) {
							Position pos = new Position(index / (getXLength() - 1), index % (getXLength() - 1));
							yokoWallNum[pos.getyIndex()][pos.getxIndex()] = (num);
						} else {
							Position pos = new Position((index - (getXLength() - 1) * getYLength()) / getXLength(),
									(index - (getXLength() - 1) * getYLength()) % getXLength());
							tateWallNum[pos.getyIndex()][pos.getxIndex()] = (num);
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
			yokoWallNum = other.yokoWallNum;
			tateWallNum = other.tateWallNum;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
						int index = HALF_NUMS.indexOf(numStr);
						if (index >= 0) {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(numStr);
						}
					} else {
						sb.append("　");
					}
					if (xIndex != getXLength() - 1) {
						String numStr = String.valueOf(yokoWallNum[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numStr);
						if (index >= 0) {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(numStr);
						}
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						String numStr = String.valueOf(tateWallNum[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numStr);
						if (index >= 0) {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(numStr);
						}
						if (xIndex != getXLength() - 1) {
							sb.append("□");
						}
					}
					sb.append("□");
					sb.append(System.lineSeparator());
				}
			}
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
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
		 * 周辺数字の条件を満たさない数字を候補から除外する。
		 */
		private boolean aroundSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int upWallNum = yIndex == 0 ? 0 : tateWallNum[yIndex - 1][xIndex];
					int rightWallNum = xIndex == getXLength() - 1 ? 0 : yokoWallNum[yIndex][xIndex];
					int downWallNum = yIndex == getYLength() - 1 ? 0 : tateWallNum[yIndex][xIndex];
					int leftWallNum = xIndex == 0 ? 0 : yokoWallNum[yIndex][xIndex - 1];
					if (upWallNum == -2) {
						for (Iterator<Integer> iterator = numbersCand[yIndex - 1][xIndex].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target >= numbersCand[yIndex][xIndex].get(numbersCand[yIndex][xIndex].size() - 1)) {
								iterator.remove();
							}
						}
						if (numbersCand[yIndex - 1][xIndex].size() == 0) {
							return false;
						}
					} else if (upWallNum == -1) {
						for (Iterator<Integer> iterator = numbersCand[yIndex - 1][xIndex].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target <= numbersCand[yIndex][xIndex].get(0)) {
								iterator.remove();
							}
						}
						if (numbersCand[yIndex - 1][xIndex].size() == 0) {
							return false;
						}
					} else if (upWallNum != 0) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : numbersCand[yIndex][xIndex]) {
							retainCand.add(cand + upWallNum);
							retainCand.add(cand - upWallNum);
						}
						numbersCand[yIndex - 1][xIndex].retainAll(retainCand);
						if (numbersCand[yIndex - 1][xIndex].size() == 0) {
							return false;
						}
					}

					if (rightWallNum == -2) {
						for (Iterator<Integer> iterator = numbersCand[yIndex][xIndex + 1].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target <= numbersCand[yIndex][xIndex].get(0)) {
								iterator.remove();
							}
						}
						if (numbersCand[yIndex][xIndex + 1].size() == 0) {
							return false;
						}
					} else if (rightWallNum == -1) {
						for (Iterator<Integer> iterator = numbersCand[yIndex][xIndex + 1].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target >= numbersCand[yIndex][xIndex].get(numbersCand[yIndex][xIndex].size() - 1)) {
								iterator.remove();
							}
						}
						if (numbersCand[yIndex][xIndex + 1].size() == 0) {
							return false;
						}

					} else if (rightWallNum != 0) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : numbersCand[yIndex][xIndex]) {
							retainCand.add(cand + rightWallNum);
							retainCand.add(cand - rightWallNum);
						}
						numbersCand[yIndex][xIndex + 1].retainAll(retainCand);
						if (numbersCand[yIndex][xIndex + 1].size() == 0) {
							return false;
						}
					}

					if (downWallNum == -2) {
						for (Iterator<Integer> iterator = numbersCand[yIndex + 1][xIndex].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target <= numbersCand[yIndex][xIndex].get(0)) {
								iterator.remove();
								if (numbersCand[yIndex + 1][xIndex].size() == 0) {
									return false;
								}
							}
						}
					} else if (downWallNum == -1) {
						for (Iterator<Integer> iterator = numbersCand[yIndex + 1][xIndex].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target >= numbersCand[yIndex][xIndex].get(numbersCand[yIndex][xIndex].size() - 1)) {
								iterator.remove();
							}
							if (numbersCand[yIndex + 1][xIndex].size() == 0) {
								return false;
							}
						}

					} else if (downWallNum != 0) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : numbersCand[yIndex][xIndex]) {
							retainCand.add(cand + downWallNum);
							retainCand.add(cand - downWallNum);
						}
						numbersCand[yIndex + 1][xIndex].retainAll(retainCand);
						if (numbersCand[yIndex + 1][xIndex].size() == 0) {
							return false;
						}
					}

					if (leftWallNum == -2) {
						for (Iterator<Integer> iterator = numbersCand[yIndex][xIndex - 1].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target >= numbersCand[yIndex][xIndex].get(numbersCand[yIndex][xIndex].size() - 1)) {
								iterator.remove();
							}
							if (numbersCand[yIndex][xIndex - 1].size() == 0) {
								return false;
							}
						}
					} else if (leftWallNum == -1) {
						for (Iterator<Integer> iterator = numbersCand[yIndex][xIndex - 1].iterator(); iterator
								.hasNext();) {
							Integer target = (Integer) iterator.next();
							if (target <= numbersCand[yIndex][xIndex].get(0)) {
								iterator.remove();
							}
							if (numbersCand[yIndex][xIndex - 1].size() == 0) {
								return false;
							}
						}
					} else if (leftWallNum != 0) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : numbersCand[yIndex][xIndex]) {
							retainCand.add(cand + leftWallNum);
							retainCand.add(cand - leftWallNum);
						}
						numbersCand[yIndex][xIndex - 1].retainAll(retainCand);
						if (numbersCand[yIndex][xIndex - 1].size() == 0) {
							return false;
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
			if (!aroundSolve()) {
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

	public MinarismSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "http://pzv.jp/p.html?minarism/7/7/jhhphhjhgj12jhgjhgphgjhihihighigihihk4n3kgigihihgihigig"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new MinarismSolver(height, width, param).solve());
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
package myamya.other.solver.doppelblock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class DoppelblockSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の候補情報
		protected List<Integer>[][] numbersCand;

		// ヒント情報。
		protected Integer[] upHints;
		protected Integer[] leftHints;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public Integer[] getUpHints() {
			return upHints;
		}

		public Integer[] getLeftHints() {
			return leftHints;
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
			leftHints = new Integer[height];
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
						} else {
							leftHints[index - width] = capacity;
						}
					}
					index++;
				}
			}
			// 縦横の数字により、初期候補数字を決定
			// 0から最大長-2まで
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int i = 0; i <= getMaxNumCand() - 2; i++) {
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
			leftHints = other.leftHints;
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
					if (index == -1) {
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
					if (index == -1) {
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
							sb.append("■");
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
		 * 同じ列・行にいる数字を候補から除外する。0は2個入る。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() != 1) {
						int zeroCnt = 0;
						for (int targetY = 0; targetY < getYLength(); targetY++) {
							if (numbersCand[targetY][xIndex].size() == 1 && yIndex != targetY) {
								if ((numbersCand[targetY][xIndex]).get(0) == 0) {
									if (zeroCnt == 0) {
										zeroCnt++;
									} else {
										numbersCand[yIndex][xIndex].remove((numbersCand[targetY][xIndex]).get(0));
									}
								} else {
									numbersCand[yIndex][xIndex].remove((numbersCand[targetY][xIndex]).get(0));
								}
							}
						}
						zeroCnt = 0;
						for (int targetX = 0; targetX < getXLength(); targetX++) {
							if (numbersCand[yIndex][targetX].size() == 1 && xIndex != targetX) {
								if ((numbersCand[yIndex][targetX]).get(0) == 0) {
									if (zeroCnt == 0) {
										zeroCnt++;
									} else {
										numbersCand[yIndex][xIndex].remove((numbersCand[yIndex][targetX]).get(0));
									}
								} else {
									numbersCand[yIndex][xIndex].remove((numbersCand[yIndex][targetX]).get(0));
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
								int zeroCnt = 0;
								for (int targetY = 0; targetY < getYLength(); targetY++) {
									if (yIndex != targetY) {
										if (numbersCand[targetY][xIndex].contains(cand)) {
											if (cand == 0) {
												if (zeroCnt == 0) {
													zeroCnt++;
												} else {
													isHiddenSingle = false;
													break;
												}
											} else {
												isHiddenSingle = false;
												break;
											}
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
								int zeroCnt = 0;
								for (int targetX = 0; targetX < getXLength(); targetX++) {
									if (xIndex != targetX) {
										if (numbersCand[yIndex][targetX].contains(cand)) {
											if (cand == 0) {
												if (zeroCnt == 0) {
													zeroCnt++;
												} else {
													isHiddenSingle = false;
													break;
												}
											} else {
												isHiddenSingle = false;
												break;
											}
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
		 * 外周数字は、0に挟まれるマスの合計値となる。
		 * TODO 0が2箇所決まってからでないと使えないので遅い。難しい問題が増えたら何とかする
		 */
		private boolean hintSolve() {
			for (int x = 0; x < getXLength(); x++) {
				Integer upHint = upHints[x];
				if (upHint != null) {
					int y1 = -1;
					int y2 = -1;
					for (int y = 0; y < getYLength(); y++) {
						if (numbersCand[y][x].size() == 1 && numbersCand[y][x].get(0) == 0) {
							if (y1 == -1) {
								y1 = y;
							} else if (y2 == -1) {
								y2 = y;
							} else {
								return false;
							}
						}
					}
					if (y1 != -1 && y2 != -1) {
						List<Position> member = new ArrayList<>();
						for (int y = y1 + 1; y < y2; y++) {
							member.add(new Position(y, x));
						}
						if (member.isEmpty() && (upHint != null && upHint != 0)) {
							return false;
						}
						for (Position pos : member) {
							// 自分以外のグループ内のマス
							List<Position> otherMember = new ArrayList<>(member);
							otherMember.remove(pos);
							for (Iterator<Integer> iterator = numbersCand[pos.getyIndex()][pos.getxIndex()]
									.iterator(); iterator
											.hasNext();) {
								// 自分の数字を決めたときに数字の合計を満たす組み合わせがあるか。
								int oneCand = iterator.next();
								Set<Integer> useNumber = new HashSet<>();
								useNumber.add(oneCand);
								if (!setCandNum(upHint, otherMember, useNumber, oneCand)) {
									// なければその数字を消す
									iterator.remove();
								}
							}
							if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 0) {
								return false;
							}
						}
					}
				}
			}
			for (int y = 0; y < getYLength(); y++) {
				Integer leftHint = leftHints[y];
				if (leftHint != null) {
					int x1 = -1;
					int x2 = -1;
					for (int x = 0; x < getXLength(); x++) {
						if (numbersCand[y][x].size() == 1 && numbersCand[y][x].get(0) == 0) {
							if (x1 == -1) {
								x1 = x;
							} else if (x2 == -1) {
								x2 = x;
							} else {
								return false;
							}
						}
					}
					if (x1 != -1 && x2 != -1) {
						List<Position> member = new ArrayList<>();
						for (int x = x1 + 1; x < x2; x++) {
							member.add(new Position(y, x));
						}
						if (member.isEmpty() && (leftHint != null && leftHint != 0)) {
							return false;
						}
						for (Position pos : member) {
							// 自分以外のグループ内のマス
							List<Position> otherMember = new ArrayList<>(member);
							otherMember.remove(pos);
							for (Iterator<Integer> iterator = numbersCand[pos.getyIndex()][pos.getxIndex()]
									.iterator(); iterator
											.hasNext();) {
								// 自分の数字を決めたときに数字の合計を満たす組み合わせがあるか。
								int oneCand = iterator.next();
								Set<Integer> useNumber = new HashSet<>();
								useNumber.add(oneCand);
								if (!setCandNum(leftHint, otherMember, useNumber, oneCand)) {
									// なければその数字を消す
									iterator.remove();
								}
							}
							if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 0) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * member+1のサイズになったときにsumを満たせる候補があればtrueを、なければfalseを返す。
		 */
		private boolean setCandNum(int cnt, List<Position> member, Set<Integer> useNumber, int sum) {
			if (useNumber.size() == member.size() + 1) {
				if (sum == cnt) {
					return true;
				}
			} else {
				Position pos = member.get(useNumber.size() - 1);
				for (int oneCand : numbersCand[pos.getyIndex()][pos.getxIndex()]) {
					if (!useNumber.contains(oneCand)) {
						// 同一数字除外
						useNumber.add(oneCand);
						if (setCandNum(cnt, member, useNumber, sum + oneCand)) {
							return true;
						}
						useNumber.remove(oneCand);
					}
				}
			}
			return false;
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

	public DoppelblockSolver(int height, int width, String param) {
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
		System.out.println(new DoppelblockSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 12));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 12).toString();
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
						count++;
						for (Iterator<Integer> iterator = field.numbersCand[yIndex][xIndex].iterator(); iterator
								.hasNext();) {
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
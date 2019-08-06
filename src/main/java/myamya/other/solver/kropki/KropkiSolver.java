package myamya.other.solver.kropki;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class KropkiSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の候補情報
		protected List<Integer>[][] numbersCand;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final Wall[][] tateWall;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
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
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			int readPos = 0;
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int bitInfo = Character.getNumericValue(ch);
				int pos1 = bitInfo / 9 % 3;
				int pos2 = bitInfo / 3 % 3;
				int pos3 = bitInfo % 3;
				boolean isTate = (index >= getYLength() * (getXLength() - 1));
				int useIndex = isTate ? index - (getYLength() * (getXLength() - 1)) : index;
				if (!isTate) {
					yokoWall[useIndex / (getXLength() - 1)][useIndex % (getXLength() - 1)] = pos1 == 0 ? Wall.SPACE
							: pos1 == 1 ? Wall.NOT_EXISTS : Wall.EXISTS;
				} else {
					tateWall[useIndex / getXLength()][useIndex % getXLength()] = pos1 == 0 ? Wall.SPACE
							: pos1 == 1 ? Wall.NOT_EXISTS : Wall.EXISTS;
				}
				index++;
				if (index >= (getYLength() * (getXLength() - 1)) + ((getYLength() - 1) * getXLength())) {
					break;
				}
				isTate = (index >= getYLength() * (getXLength() - 1));
				useIndex = isTate ? index - (getYLength() * (getXLength() - 1)) : index;
				if (!isTate) {
					yokoWall[useIndex / (getXLength() - 1)][useIndex % (getXLength() - 1)] = pos2 == 0 ? Wall.SPACE
							: pos2 == 1 ? Wall.NOT_EXISTS : Wall.EXISTS;
				} else {
					tateWall[useIndex / getXLength()][useIndex % getXLength()] = pos2 == 0 ? Wall.SPACE
							: pos2 == 1 ? Wall.NOT_EXISTS : Wall.EXISTS;
				}
				index++;
				if (index >= (getYLength() * (getXLength() - 1)) + ((getYLength() - 1) * getXLength())) {
					break;
				}
				isTate = (index >= getYLength() * (getXLength() - 1));
				useIndex = isTate ? index - (getYLength() * (getXLength() - 1)) : index;
				if (!isTate) {
					yokoWall[useIndex / (getXLength() - 1)][useIndex % (getXLength() - 1)] = pos3 == 0 ? Wall.SPACE
							: pos3 == 1 ? Wall.NOT_EXISTS : Wall.EXISTS;
				} else {
					tateWall[useIndex / getXLength()][useIndex % getXLength()] = pos3 == 0 ? Wall.SPACE
							: pos3 == 1 ? Wall.NOT_EXISTS : Wall.EXISTS;
				}
				index++;
				if (index >= (getYLength() * (getXLength() - 1)) + ((getYLength() - 1) * getXLength())) {
					break;
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
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
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
						sb.append(yokoWall[yIndex][xIndex] == Wall.EXISTS ? "●"
								: yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS ? "○" : "　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex] == Wall.EXISTS ? "●"
								: tateWall[yIndex][xIndex] == Wall.NOT_EXISTS ? "○" : "　");
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
					List<Integer> upCands = yIndex == 0 ? null : numbersCand[yIndex - 1][xIndex];
					List<Integer> rightCands = xIndex == getXLength() - 1 ? null
							: numbersCand[yIndex][xIndex + 1];
					List<Integer> downCands = yIndex == getYLength() - 1 ? null
							: numbersCand[yIndex + 1][xIndex];
					List<Integer> leftCands = xIndex == 0 ? null : numbersCand[yIndex][xIndex - 1];
					Wall upWall = yIndex == 0 ? null : tateWall[yIndex - 1][xIndex];
					Wall rightWall = xIndex == getXLength() - 1 ? null : yokoWall[yIndex][xIndex];
					Wall downWall = yIndex == getYLength() - 1 ? null : tateWall[yIndex][xIndex];
					Wall leftWall = xIndex == 0 ? null : yokoWall[yIndex][xIndex - 1];
					if (upWall == Wall.EXISTS) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : upCands) {
							retainCand.add(cand * 2);
							if (cand % 2 == 0) {
								retainCand.add(cand / 2);
							}
						}
						numbersCand[yIndex][xIndex].retainAll(retainCand);
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					} else if (upWall == Wall.NOT_EXISTS) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : upCands) {
							retainCand.add(cand + 1);
							retainCand.add(cand - 1);
						}
						numbersCand[yIndex][xIndex].retainAll(retainCand);
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					} else if (upWall == Wall.SPACE) {
						Set<Integer> removeCand = new HashSet<>();
						for (Integer cand : upCands) {
							Set<Integer> oneCand = new HashSet<>();
							oneCand.add(cand * 2);
							if (cand % 2 == 0) {
								oneCand.add(cand / 2);
							}
							oneCand.add(cand + 1);
							oneCand.add(cand - 1);
							if (removeCand.isEmpty()) {
								removeCand.addAll(oneCand);
							} else {
								removeCand.retainAll(oneCand);
								if (removeCand.isEmpty()) {
									break;
								}
							}
						}
						numbersCand[yIndex][xIndex].removeAll(removeCand);
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
					if (rightWall == Wall.EXISTS) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : rightCands) {
							retainCand.add(cand * 2);
							if (cand % 2 == 0) {
								retainCand.add(cand / 2);
							}
						}
						numbersCand[yIndex][xIndex].retainAll(retainCand);
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					} else if (rightWall == Wall.NOT_EXISTS) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : rightCands) {
							retainCand.add(cand + 1);
							retainCand.add(cand - 1);
						}
						numbersCand[yIndex][xIndex].retainAll(retainCand);
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					} else if (rightWall == Wall.SPACE) {
						Set<Integer> removeCand = new HashSet<>();
						for (Integer cand : rightCands) {
							Set<Integer> oneCand = new HashSet<>();
							oneCand.add(cand * 2);
							if (cand % 2 == 0) {
								oneCand.add(cand / 2);
							}
							oneCand.add(cand + 1);
							oneCand.add(cand - 1);
							if (removeCand.isEmpty()) {
								removeCand.addAll(oneCand);
							} else {
								removeCand.retainAll(oneCand);
								if (removeCand.isEmpty()) {
									break;
								}
							}
						}
						numbersCand[yIndex][xIndex].removeAll(removeCand);
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
					if (downWall == Wall.EXISTS) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : downCands) {
							retainCand.add(cand * 2);
							if (cand % 2 == 0) {
								retainCand.add(cand / 2);
							}
						}
						numbersCand[yIndex][xIndex].retainAll(retainCand);
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					} else if (downWall == Wall.NOT_EXISTS) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : downCands) {
							retainCand.add(cand + 1);
							retainCand.add(cand - 1);
						}
						numbersCand[yIndex][xIndex].retainAll(retainCand);
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					} else if (downWall == Wall.SPACE) {
						Set<Integer> removeCand = new HashSet<>();
						for (Integer cand : downCands) {
							Set<Integer> oneCand = new HashSet<>();
							oneCand.add(cand * 2);
							if (cand % 2 == 0) {
								oneCand.add(cand / 2);
							}
							oneCand.add(cand + 1);
							oneCand.add(cand - 1);
							if (removeCand.isEmpty()) {
								removeCand.addAll(oneCand);
							} else {
								removeCand.retainAll(oneCand);
								if (removeCand.isEmpty()) {
									break;
								}
							}
						}
						numbersCand[yIndex][xIndex].removeAll(removeCand);
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
					if (leftWall == Wall.EXISTS) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : leftCands) {
							retainCand.add(cand * 2);
							if (cand % 2 == 0) {
								retainCand.add(cand / 2);
							}
						}
						numbersCand[yIndex][xIndex].retainAll(retainCand);
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					} else if (leftWall == Wall.NOT_EXISTS) {
						Set<Integer> retainCand = new HashSet<>();
						for (Integer cand : leftCands) {
							retainCand.add(cand + 1);
							retainCand.add(cand - 1);
						}
						numbersCand[yIndex][xIndex].retainAll(retainCand);
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					} else if (leftWall == Wall.SPACE) {
						Set<Integer> removeCand = new HashSet<>();
						for (Integer cand : leftCands) {
							Set<Integer> oneCand = new HashSet<>();
							oneCand.add(cand * 2);
							if (cand % 2 == 0) {
								oneCand.add(cand / 2);
							}
							oneCand.add(cand + 1);
							oneCand.add(cand - 1);
							if (removeCand.isEmpty()) {
								removeCand.addAll(oneCand);
							} else {
								removeCand.retainAll(oneCand);
								if (removeCand.isEmpty()) {
									break;
								}
							}
						}
						numbersCand[yIndex][xIndex].removeAll(removeCand);
						if (numbersCand[yIndex][xIndex].size() == 0) {
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

	public KropkiSolver(int height, int width, String param) {
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
		System.out.println(new KropkiSolver(height, width, param).solve());
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
						}
						if (field.numbersCand[yIndex][xIndex].size() == 0) {
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
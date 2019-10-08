package myamya.other.solver.hashikake;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class HashikakeSolverNew implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final int BLACK_CNT = 1;

		// 数字の情報
		private final Integer[][] numbers;
		// 橋のかけ方の候補
		private Map<Position, Map<Position, Set<Integer>>> candidates;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public Map<Position, Map<Position, Set<Integer>>> getCandidates() {
			return candidates;
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = Character.getNumericValue(ch) - 15;
				if (interval > 0) {
					index = index + interval;
				} else {
					Position pos = new Position(index / getXLength(), index % getXLength());
					if (ch == '.') {
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else {
						int capacity = Integer.parseInt(String.valueOf(ch), 16);
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
			// 橋をかける方法の候補を作成
			candidates = new HashMap<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						Map<Position, Set<Integer>> oneCandidates = new HashMap<>();
						for (int i = 1; yIndex - i >= 0; i++) {
							if (numbers[yIndex - i][xIndex] != null) {
								Set<Integer> wkSet = new HashSet<>();
								wkSet.add(0);
								wkSet.add(1);
								wkSet.add(2);
								oneCandidates.put(new Position(yIndex - i, xIndex), wkSet);
								break;
							}
						}
						for (int i = 1; xIndex + i <= getXLength() - 1; i++) {
							if (numbers[yIndex][xIndex + i] != null) {
								Set<Integer> wkSet = new HashSet<>();
								wkSet.add(0);
								wkSet.add(1);
								wkSet.add(2);
								oneCandidates.put(new Position(yIndex, xIndex + i), wkSet);
								break;
							}
						}
						for (int i = 1; yIndex + i <= getYLength() - 1; i++) {
							if (numbers[yIndex + i][xIndex] != null) {
								Set<Integer> wkSet = new HashSet<>();
								wkSet.add(0);
								wkSet.add(1);
								wkSet.add(2);
								oneCandidates.put(new Position(yIndex + i, xIndex), wkSet);
								break;
							}
						}
						for (int i = 1; xIndex - i >= 0; i++) {
							if (numbers[yIndex][xIndex - i] != null) {
								Set<Integer> wkSet = new HashSet<>();
								wkSet.add(0);
								wkSet.add(1);
								wkSet.add(2);
								oneCandidates.put(new Position(yIndex, xIndex - i), wkSet);
								break;
							}
						}
						candidates.put(new Position(yIndex, xIndex), oneCandidates);
					}
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			candidates = new HashMap<>();
			for (Entry<Position, Map<Position, Set<Integer>>> entry : other.candidates.entrySet()) {
				Map<Position, Set<Integer>> wkMap = new HashMap<>();
				for (Entry<Position, Set<Integer>> innerEntry : entry.getValue().entrySet()) {
					wkMap.put(innerEntry.getKey(), new HashSet<>(innerEntry.getValue()));
				}
				candidates.put(entry.getKey(), wkMap);
			}
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
					if (numbers[yIndex][xIndex] != null) {
						String numStr = String.valueOf(numbers[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numStr);
						if (index >= 0) {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(numStr);
						}
					} else {
						sb.append("　");
						for (Entry<Position, Map<Position, Set<Integer>>> entry : candidates.entrySet()) {
							for (Entry<Position, Set<Integer>> innerEntry : entry.getValue().entrySet()) {
								if (innerEntry.getValue().size() == 1) {
									Position fixedFrom = entry.getKey();
									Position fixedTo = innerEntry.getKey();
									if (isCross(fixedFrom, fixedTo, new Position(yIndex, xIndex),
											new Position(yIndex, xIndex))) {
										sb.setLength(sb.length() - 1);
										sb.append(innerEntry.getValue());
										break;
									}
								}
							}
						}
					}
					if (xIndex != getXLength() - 1) {
						sb.append("　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append("　");
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
			for (Map<Position, Set<Integer>> oneCandidates : candidates.values()) {
				for (Entry<Position, Set<Integer>> e : oneCandidates.entrySet()) {
					sb.append(e.getValue().size());
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
			if (!hashiSolve()) {
				return false;
			}
			if (!connectSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 数字はそこから伸びる橋の数を表す。矛盾する場合はfalseを返す。
		 */
		private boolean countSolve() {
			return true;
		}

		/**
		 * かけることを確定している橋があるとき、その橋と交差する候補を消す
		 * 消した結果、候補がなくなった場合はfalseを返す。
		 */
		private boolean hashiSolve() {
			for (Entry<Position, Map<Position, Set<Integer>>> entry : candidates.entrySet()) {
				for (Entry<Position, Set<Integer>> innerEntry : entry.getValue().entrySet()) {
					Position fixedFrom = entry.getKey();
					Position fixedTo = innerEntry.getKey();
					if (!innerEntry.getValue().contains(0)) {
						for (Entry<Position, Map<Position, Set<Integer>>> target : candidates.entrySet()) {
							Position targetFrom = target.getKey();
							if (!fixedFrom.equals(targetFrom)) {
								for (Entry<Position, Set<Integer>> targetInnerEntry : target.getValue().entrySet()) {
									Position targetTo = targetInnerEntry.getKey();
									if (isCross(fixedFrom, fixedTo, targetFrom, targetTo)) {
										targetInnerEntry.getValue().remove(1);
										targetInnerEntry.getValue().remove(2);
									}
								}
							}
							if (target.getValue().size() == 0) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 2つの座標がクロスしてるか調べる
		 */
		private boolean isCross(Position fixedFrom, Position fixedTo, Position targetFrom, Position targetTo) {
			int minY = fixedFrom.getyIndex() < fixedTo.getyIndex() ? fixedFrom.getyIndex() : fixedTo.getyIndex();
			int maxY = fixedFrom.getyIndex() < fixedTo.getyIndex() ? fixedTo.getyIndex() : fixedFrom.getyIndex();
			int minX = fixedFrom.getxIndex() < fixedTo.getxIndex() ? fixedFrom.getxIndex() : fixedTo.getxIndex();
			int maxX = fixedFrom.getxIndex() < fixedTo.getxIndex() ? fixedTo.getxIndex() : fixedFrom.getxIndex();
			if (targetFrom.getyIndex() < minY && targetTo.getyIndex() < minY) {
				return false;
			}
			if (targetFrom.getyIndex() > maxY && targetTo.getyIndex() > maxY) {
				return false;
			}
			if (targetFrom.getxIndex() < minX && targetTo.getxIndex() < minX) {
				return false;
			}
			if (targetFrom.getxIndex() > maxX && targetTo.getxIndex() > maxX) {
				return false;
			}
			return true;
		}

		/**
		 * 数字をできるだけつなぎ、全数字マスが回収できるか調査する。
		 * 回収できない数字があったらfalseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> connectPosSet = new HashSet<>();
			for (Entry<Position, Map<Position, Set<Integer>>> candidate : candidates.entrySet()) {
				for (Entry<Position, Set<Integer>> innerEntry : candidate.getValue().entrySet()) {
					if (connectPosSet.size() == 0) {
						connectPosSet.add(innerEntry.getKey());
						setContinuePosSet(innerEntry.getKey(), connectPosSet);
					} else {
						if (!connectPosSet.contains(innerEntry.getKey())) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に橋なし確定でない位置をつないでいく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			Map<Position, Set<Integer>> oneCand = candidates.get(pos);
			for (Entry<Position, Set<Integer>> candidate : oneCand.entrySet()) {
				if ((candidate.getValue().contains(1) || candidate.getValue().contains(2))
						&& !continuePosSet.contains(candidate.getKey())) {
					continuePosSet.add(candidate.getKey());
					setContinuePosSet(candidate.getKey(), continuePosSet);
				}
			}
		}

		public boolean isSolved() {
			for (Map<Position, Set<Integer>> oneCandidates : candidates.values()) {
				for (Entry<Position, Set<Integer>> e : oneCandidates.entrySet()) {
					if (e.getValue().size() != 1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}
	}

	private final Field field;
	private int count = 0;

	public HashikakeSolverNew(int height, int width, String param) {
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
		System.out.println(new HashikakeSolverNew(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 3));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 3).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		while (true) {
			String befStr = field.getStateDump();
			for (Entry<Position, Map<Position, Set<Integer>>> entry : field.candidates.entrySet()) {
				for (Entry<Position, Set<Integer>> innerEntry : entry.getValue().entrySet()) {
					if (innerEntry.getValue().size() != 1) {
						for (Iterator<Integer> iterator = innerEntry.getValue().iterator(); iterator
								.hasNext();) {
							count++;
							Integer oneCand = iterator.next();
							Field virtual = new Field(field);
							virtual.candidates.get(entry.getKey()).get(innerEntry.getKey()).clear();
							virtual.candidates.get(entry.getKey()).get(innerEntry.getKey()).add(oneCand);
							boolean arrowCand = virtual.solveAndCheck();
							if (arrowCand && recursive > 0) {
								arrowCand = candSolve(virtual, recursive - 1);
							}
							if (!arrowCand) {
								iterator.remove();
							}
						}
						if (innerEntry.getValue().size() == 0) {
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
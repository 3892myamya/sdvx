package myamya.other.solver.herugolf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class HerugolfSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_I = "ijklmnopqrstuvwxyz";

		// 固定数字(表示用)
		private final Integer[][] numbers;
		// 移動の仕方の候補
		private Map<Position, List<List<Position>>> candidates;
		// 池のマス
		private final boolean[][] pond;
		// ゴール
		private final boolean[][] hole;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public boolean[][] getPond() {
			return pond;
		}

		public boolean[][] getHole() {
			return hole;
		}

		public Map<Position, List<List<Position>>> getCandidates() {
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
			pond = new boolean[height][width];
			hole = new boolean[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					pond[yIndex][xIndex] = false;
					hole[yIndex][xIndex] = false;
				}
			}
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * (getXLength())) - 1) {
					if (mod >= 0) {
						Position pos = new Position((cnt - mod + 0) / (getXLength()), (cnt - mod + 0) % (getXLength()));
						if (bit / 16 % 2 == 1) {
							pond[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
					if (mod >= 1) {
						Position pos = new Position((cnt - mod + 1) / (getXLength()), (cnt - mod + 1) % (getXLength()));
						if (bit / 8 % 2 == 1) {
							pond[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
					if (mod >= 2) {
						Position pos = new Position((cnt - mod + 2) / (getXLength()), (cnt - mod + 2) % (getXLength()));
						if (bit / 4 % 2 == 1) {
							pond[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
					if (mod >= 3) {
						Position pos = new Position((cnt - mod + 3) / (getXLength()), (cnt - mod + 3) % (getXLength()));
						if (bit / 2 % 2 == 1) {
							pond[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
					if (mod >= 4) {
						Position pos = new Position((cnt - mod + 4) / (getXLength()), (cnt - mod + 4) % (getXLength()));
						if (bit / 1 % 2 == 1) {
							pond[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
				}
			}
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
					Position pos = new Position(index / getXLength(), index % getXLength());
					if (ch == '.') {
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else if (ch == 'h') {
						hole[pos.getyIndex()][pos.getxIndex()] = true;
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
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
					}
					index++;
				}
			}
			// 移動方法の候補を作成
			candidates = new HashMap<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int cnt = numbers[yIndex][xIndex];
						Position pos = new Position(yIndex, xIndex);
						List<List<Position>> oneCandidateList = new ArrayList<>();
						List<Position> oneCandidate = new ArrayList<>();
						oneCandidate.add(pos);
						setCandidate(cnt, pos, oneCandidateList, oneCandidate);
						candidates.put(pos, oneCandidateList);
					}
				}
			}
			System.out.println("移動候補作成負荷：" + cnt * 5);
		}

		int cnt = 0;

		private void setCandidate(int cnt, Position pos, List<List<Position>> oneCandidateList,
				List<Position> oneCandidate) {
			this.cnt++;
			if (pos.getyIndex() - cnt >= 0) {
				Position nextPos = new Position(pos.getyIndex() - cnt, pos.getxIndex());
				if (!pond[nextPos.getyIndex()][nextPos.getxIndex()]) {
					boolean cross = false;
					// 自分の線とのクロス判定
					for (int i = 0; i < oneCandidate.size() - 1; i++) {
						if (i == oneCandidate.size() - 2) {
							// 最後は線判定だと確実にクロスになってしまうので点で判定
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), nextPos, nextPos)) {
								cross = true;
								break;
							}
						} else {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), pos, nextPos)) {
								cross = true;
								break;
							}
						}
					}
					if (!cross) {
						// 数字またはホールとのクロス判定
						for (int yIndex = pos.getyIndex() - 1; yIndex > nextPos.getyIndex(); yIndex--) {
							if (numbers[yIndex][pos.getxIndex()] != null
									|| hole[yIndex][pos.getxIndex()]) {
								cross = true;
								break;
							}
						}
						if (!cross) {
							oneCandidate.add(nextPos);
							if (hole[nextPos.getyIndex()][nextPos.getxIndex()]) {
								oneCandidateList.add(new ArrayList<>(oneCandidate));
							} else if (cnt > 1) {
								setCandidate(cnt - 1, nextPos, oneCandidateList, oneCandidate);
							}
							oneCandidate.remove(nextPos);
						}
					}
				}
			}
			if (pos.getxIndex() + cnt <= getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + cnt);
				if (!pond[nextPos.getyIndex()][nextPos.getxIndex()]) {
					boolean cross = false;
					for (int i = 0; i < oneCandidate.size() - 1; i++) {
						if (i == oneCandidate.size() - 2) {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), nextPos, nextPos)) {
								cross = true;
								break;
							}
						} else {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), pos, nextPos)) {
								cross = true;
								break;
							}
						}
					}
					if (!cross) {
						for (int xIndex = pos.getxIndex() + 1; xIndex < nextPos.getxIndex(); xIndex++) {
							if (numbers[pos.getyIndex()][xIndex] != null
									|| hole[pos.getyIndex()][xIndex]) {
								cross = true;
								break;
							}
						}
						if (!cross) {
							oneCandidate.add(nextPos);
							if (hole[nextPos.getyIndex()][nextPos.getxIndex()]) {
								oneCandidateList.add(new ArrayList<>(oneCandidate));
							} else if (cnt > 1) {
								setCandidate(cnt - 1, nextPos, oneCandidateList, oneCandidate);
							}
							oneCandidate.remove(nextPos);
						}
					}
				}
			}
			if (pos.getyIndex() + cnt <= getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + cnt, pos.getxIndex());
				if (!pond[nextPos.getyIndex()][nextPos.getxIndex()]) {
					boolean cross = false;
					for (int i = 0; i < oneCandidate.size() - 1; i++) {
						if (i == oneCandidate.size() - 2) {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), nextPos, nextPos)) {
								cross = true;
								break;
							}
						} else {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), pos, nextPos)) {
								cross = true;
								break;
							}
						}
					}
					if (!cross) {
						for (int yIndex = pos.getyIndex() + 1; yIndex < nextPos.getyIndex(); yIndex++) {
							if (numbers[yIndex][pos.getxIndex()] != null
									|| hole[yIndex][pos.getxIndex()]) {
								cross = true;
								break;
							}
						}
						if (!cross) {
							oneCandidate.add(nextPos);
							if (hole[nextPos.getyIndex()][nextPos.getxIndex()]) {
								oneCandidateList.add(new ArrayList<>(oneCandidate));
							} else if (cnt > 1) {
								setCandidate(cnt - 1, nextPos, oneCandidateList, oneCandidate);
							}
							oneCandidate.remove(nextPos);
						}
					}
				}
			}
			if (pos.getxIndex() - cnt >= 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - cnt);
				if (!pond[nextPos.getyIndex()][nextPos.getxIndex()]) {
					boolean cross = false;
					for (int i = 0; i < oneCandidate.size() - 1; i++) {
						if (i == oneCandidate.size() - 2) {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), nextPos, nextPos)) {
								cross = true;
								break;
							}
						} else {
							if (isCross(oneCandidate.get(i), oneCandidate.get(i + 1), pos, nextPos)) {
								cross = true;
								break;
							}
						}
					}
					if (!cross) {
						for (int xIndex = pos.getxIndex() - 1; xIndex > nextPos.getxIndex(); xIndex--) {
							if (numbers[pos.getyIndex()][xIndex] != null
									|| hole[pos.getyIndex()][xIndex]) {
								cross = true;
								break;
							}
						}
						if (!cross) {
							oneCandidate.add(nextPos);
							if (hole[nextPos.getyIndex()][nextPos.getxIndex()]) {
								oneCandidateList.add(new ArrayList<>(oneCandidate));
							} else if (cnt > 1) {
								setCandidate(cnt - 1, nextPos, oneCandidateList, oneCandidate);
							}
							oneCandidate.remove(nextPos);
						}
					}
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			pond = other.pond;
			hole = other.hole;
			candidates = new HashMap<>();
			for (Entry<Position, List<List<Position>>> entry : other.candidates.entrySet()) {
				candidates.put(entry.getKey(), new ArrayList<>(entry.getValue()));
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
						if (index >= 0) {
							if (index == 0) {
								sb.append("○");
							} else {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							}
						} else {
							sb.append(numStr);
						}
					} else {
						if (hole[yIndex][xIndex]) {
							sb.append("Ｈ");
						} else if (pond[yIndex][xIndex]) {
							sb.append("□");
						} else {
							sb.append("　");
						}
						for (Entry<Position, List<List<Position>>> entry : candidates.entrySet()) {
							// TODO 候補が複数あっても、途中まで同じだったらそこまで表示したい
							if (entry.getValue().size() == 1) {
								List<Position> fixedList = entry.getValue().get(0);
								for (int i = 0; i < fixedList.size() - 1; i++) {
									Position fixedFrom = fixedList.get(i);
									Position fixedTo = fixedList.get(i + 1);
									if (fixedTo.equals(new Position(yIndex, xIndex))) {
										sb.setLength(sb.length() - 1);
										if (i == fixedList.size() - 2) {
											sb.append("○");
										} else {
											sb.append("・");
										}
									} else if (isCross(fixedFrom, fixedTo, new Position(yIndex, xIndex),
											new Position(yIndex, xIndex))) {
										if (fixedTo.getyIndex() < fixedFrom.getyIndex()) {
											if (sb.charAt(sb.length() - 1) != '・') {
												sb.setLength(sb.length() - 1);
												sb.append("↑");
											}
										} else if (fixedTo.getxIndex() > fixedFrom.getxIndex()) {
											if (sb.charAt(sb.length() - 1) != '・') {
												sb.setLength(sb.length() - 1);
												sb.append("→");
											}
										} else if (fixedTo.getyIndex() > fixedFrom.getyIndex()) {
											if (sb.charAt(sb.length() - 1) != '・') {
												sb.setLength(sb.length() - 1);
												sb.append("↓");
											}
										} else if (fixedTo.getxIndex() < fixedFrom.getxIndex()) {
											if (sb.charAt(sb.length() - 1) != '・') {
												sb.setLength(sb.length() - 1);
												sb.append("←");
											}
										}
									}
								}
							}
						}
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (List<List<Position>> oneCandidates : candidates.values()) {
				sb.append(oneCandidates.size());
			}
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!moveSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 移動候補が確定している数字があるとき、その数字と交差したり、同一の部屋に入る移動候補を消す。
		 * 消した結果、移動できない数字ができたときはfalseを返す。
		 */
		private boolean moveSolve() {
			for (Entry<Position, List<List<Position>>> entry : candidates.entrySet()) {
				if (entry.getValue().size() == 1) {
					List<Position> fixedList = entry.getValue().get(0);
					for (int i = 0; i < fixedList.size() - 1; i++) {
						Position fixedFrom = fixedList.get(i);
						Position fixedTo = fixedList.get(i + 1);
						for (Entry<Position, List<List<Position>>> target : candidates.entrySet()) {
							if (!entry.getKey().equals(target.getKey())) {
								for (Iterator<List<Position>> iterator = target.getValue().iterator(); iterator
										.hasNext();) {
									List<Position> targetList = iterator.next();
									for (int j = 0; j < targetList.size() - 1; j++) {
										Position targetFrom = targetList.get(j);
										Position targetTo = targetList.get(j + 1);
										if (isCross(fixedFrom, fixedTo, targetFrom, targetTo)) {
											iterator.remove();
											break;
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

		public boolean isSolved() {
			for (List<List<Position>> oneCandidates : candidates.values()) {
				if (oneCandidates.size() != 1) {
					return false;
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public HerugolfSolver(int height, int width, String param) {
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
		System.out.println(new HerugolfSolver(height, width, param).solve());
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
						if (!candSolve(field, 999)) {
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
		System.out.println("難易度:" + (count * 25 + field.cnt * 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 25 + field.cnt * 5).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		while (true) {
			String befStr = field.getStateDump();
			for (Entry<Position, List<List<Position>>> entry : field.candidates.entrySet()) {
				if (entry.getValue().size() != 1) {
					for (Iterator<List<Position>> iterator = entry.getValue().iterator(); iterator
							.hasNext();) {
						count++;
						List<Position> oneCand = iterator.next();
						Field virtual = new Field(field);
						virtual.candidates.get(entry.getKey()).clear();
						virtual.candidates.get(entry.getKey()).add(oneCand);
						boolean arrowCand = virtual.solveAndCheck();
						if (arrowCand && recursive > 0) {
							arrowCand = candSolve(virtual, recursive - 1);
						}
						if (!arrowCand) {
							iterator.remove();
						}
					}
					if (entry.getValue().size() == 0) {
						return false;
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
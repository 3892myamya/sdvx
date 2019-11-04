package myamya.other.solver.wblink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class WblinkSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_I = "ijklmnopqrstuvwxyz";

		// 白
		private final boolean[][] white;
		// 黒
		private final boolean[][] black;
		// 線の引き方の候補
		private Map<Position, Set<Position>> candidates;

		public boolean[][] getWhite() {
			return white;
		}

		public boolean[][] getBlack() {
			return black;
		}

		public Map<Position, Set<Position>> getCandidates() {
			return candidates;
		}

		public int getYLength() {
			return white.length;
		}

		public int getXLength() {
			return white[0].length;
		}

		public Field(int height, int width, String param) {
			white = new boolean[height][width];
			black = new boolean[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					white[yIndex][xIndex] = false;
					black[yIndex][xIndex] = false;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int bitInfo = Character.getNumericValue(ch);
				int pos1 = bitInfo / 9 % 3;
				int pos2 = bitInfo / 3 % 3;
				int pos3 = bitInfo % 3;
				if (index / getXLength() < getYLength()) {
					if (pos1 == 1) {
						white[index / getXLength()][index % getXLength()] = true;
					} else if (pos1 == 2) {
						black[index / getXLength()][index % getXLength()] = true;
					}
				}
				index++;
				if (index / getXLength() < getYLength()) {
					if (pos2 == 1) {
						white[index / getXLength()][index % getXLength()] = true;
					} else if (pos2 == 2) {
						black[index / getXLength()][index % getXLength()] = true;
					}
				}
				index++;
				if (index / getXLength() < getYLength()) {
					if (pos3 == 1) {
						white[index / getXLength()][index % getXLength()] = true;
					} else if (pos3 == 2) {
						black[index / getXLength()][index % getXLength()] = true;
					}

				}
				index++;
			}
			// 移動方法の候補を作成
			candidates = new HashMap<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (white[yIndex][xIndex]) {
						Set<Position> oneCandidates = new HashSet<>();
						for (int i = 1; yIndex - i >= 0; i++) {
							if (white[yIndex - i][xIndex]) {
								break;
							}
							if (black[yIndex - i][xIndex]) {
								oneCandidates.add(new Position(yIndex - i, xIndex));
								break;
							}
						}
						for (int i = 1; xIndex + i <= getXLength() - 1; i++) {
							if (white[yIndex][xIndex + i]) {
								break;
							}
							if (black[yIndex][xIndex + i]) {
								oneCandidates.add(new Position(yIndex, xIndex + i));
								break;
							}
						}
						for (int i = 1; yIndex + i <= getYLength() - 1; i++) {
							if (white[yIndex + i][xIndex]) {
								break;
							}
							if (black[yIndex + i][xIndex]) {
								oneCandidates.add(new Position(yIndex + i, xIndex));
								break;
							}
						}
						for (int i = 1; xIndex - i >= 0; i++) {
							if (white[yIndex][xIndex - i]) {
								break;
							}
							if (black[yIndex][xIndex - i]) {
								oneCandidates.add(new Position(yIndex, xIndex - i));
								break;
							}
						}
						candidates.put(new Position(yIndex, xIndex), oneCandidates);
					}
				}
			}
		}

		public Field(Field other) {
			white = other.white;
			black = other.black;
			candidates = new HashMap<>();
			for (Entry<Position, Set<Position>> entry : other.candidates.entrySet()) {
				candidates.put(entry.getKey(), new HashSet<>(entry.getValue()));
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (white[yIndex][xIndex]) {
						sb.append("○");
					} else {
						if (black[yIndex][xIndex]) {
							sb.append("●");
						} else {
							sb.append("　");
						}
						for (Entry<Position, Set<Position>> entry : candidates.entrySet()) {
							if (entry.getValue().size() == 1) {
								Position fixedFrom = entry.getKey();
								Position fixedTo = new ArrayList<>(entry.getValue()).get(0);
								if (fixedTo.equals(new Position(yIndex, xIndex))) {
									break;
								} else if (isCross(fixedFrom, fixedTo, new Position(yIndex, xIndex),
										new Position(yIndex, xIndex))) {
									if (fixedTo.getyIndex() < fixedFrom.getyIndex()) {
										sb.setLength(sb.length() - 1);
										sb.append("│");
									} else if (fixedTo.getxIndex() > fixedFrom.getxIndex()) {
										sb.setLength(sb.length() - 1);
										sb.append("─");
									} else if (fixedTo.getyIndex() > fixedFrom.getyIndex()) {
										sb.setLength(sb.length() - 1);
										sb.append("│");
									} else if (fixedTo.getxIndex() < fixedFrom.getxIndex()) {
										sb.setLength(sb.length() - 1);
										sb.append("─");
									}
									break;
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
			for (Set<Position> oneCandidates : candidates.values()) {
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
		 * 移動候補が確定している数字があるとき、その数字と交差する移動候補を消す。
		 * 消した結果、移動できない数字ができたときはfalseを返す。
		 */
		private boolean moveSolve() {
			for (Entry<Position, Set<Position>> entry : candidates.entrySet()) {
				if (entry.getValue().size() == 1) {
					Position fixedFrom = entry.getKey();
					Position fixedTo = new ArrayList<>(entry.getValue()).get(0);
					for (Entry<Position, Set<Position>> target : candidates.entrySet()) {
						Position targetFrom = target.getKey();
						if (!fixedFrom.equals(targetFrom)) {
							for (Iterator<Position> iterator = target.getValue().iterator(); iterator
									.hasNext();) {
								Position targetTo = iterator.next();
								if (isCross(fixedFrom, fixedTo, targetFrom, targetTo)) {
									iterator.remove();
								}
							}
						}
						if (target.getValue().size() == 0) {
							return false;
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
			Set<Position> fixed = new HashSet<>();
			for (Set<Position> oneCandidates : candidates.values()) {
				if (oneCandidates.size() != 1) {
					return false;
				} else {
					fixed.add(new ArrayList<>(oneCandidates).get(0));
				}
			}
			// 黒マスが回収しきれてない場合解けていない。
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (black[yIndex][xIndex]) {
						fixed.remove(new Position(yIndex, xIndex));
					}
				}
			}
			if (!fixed.isEmpty()) {
				return false;
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public WblinkSolver(int height, int width, String param) {
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
		System.out.println(new WblinkSolver(height, width, param).solve());
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
			for (Entry<Position, Set<Position>> entry : field.candidates.entrySet()) {
				if (entry.getValue().size() != 1) {
					for (Iterator<Position> iterator = entry.getValue().iterator(); iterator
							.hasNext();) {
						count++;
						Position oneCand = iterator.next();
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
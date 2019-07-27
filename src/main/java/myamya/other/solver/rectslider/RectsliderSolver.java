package myamya.other.solver.rectslider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class RectsliderSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 固定数字(表示用)
		private final Integer[][] numbers;
		// 移動の仕方の候補
		private Map<Position, Set<Position>> candidates;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public Map<Position, Set<Position>> getCandidates() {
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
			int readPos = 0;
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int num;
					Position pos = new Position(index / getXLength(), index % getXLength());
					if (ch == '.') {
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
						Set<Position> oneCandidates = new HashSet<>();
						if (numbers[yIndex][xIndex] == -1) {
							for (int i = 0; yIndex - i >= 0; i++) {
								oneCandidates.add(new Position(yIndex - i, xIndex));
							}
							for (int i = 0; xIndex + i <= getXLength() - 1; i++) {
								oneCandidates.add(new Position(yIndex, xIndex + i));
							}
							for (int i = 0; yIndex + i <= getYLength() - 1; i++) {
								oneCandidates.add(new Position(yIndex + i, xIndex));
							}
							for (int i = 0; xIndex - i >= 0; i++) {
								oneCandidates.add(new Position(yIndex, xIndex - i));
							}
						} else {
							if (yIndex - cnt >= 0) {
								oneCandidates.add(new Position(yIndex - cnt, xIndex));
							}
							if (xIndex + cnt <= getXLength() - 1) {
								oneCandidates.add(new Position(yIndex, xIndex + cnt));
							}
							if (yIndex + cnt <= getYLength() - 1) {
								oneCandidates.add(new Position(yIndex + cnt, xIndex));
							}
							if (xIndex - cnt >= 0) {
								oneCandidates.add(new Position(yIndex, xIndex - cnt));
							}
						}
						candidates.put(new Position(yIndex, xIndex), oneCandidates);
					}
				}
			}
			// 先に他のfromとかぶる候補は消す。
			for (Entry<Position, Set<Position>> entry : candidates.entrySet()) {
				Position oneFrom = entry.getKey();
				for (Entry<Position, Set<Position>> target : candidates.entrySet()) {
					Position otherFrom = target.getKey();
					if (!oneFrom.equals(otherFrom)) {
						for (Iterator<Position> iterator = entry.getValue().iterator(); iterator
								.hasNext();) {
							Position oneTo = iterator.next();
							if (isCross(oneFrom, oneTo, otherFrom, otherFrom)) {
								iterator.remove();
							}
						}
					}
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			candidates = new HashMap<>();
			for (Entry<Position, Set<Position>> entry : other.candidates.entrySet()) {
				candidates.put(entry.getKey(), new HashSet<>(entry.getValue()));
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
								sb.append("□");
							} else {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							}
						} else {
							sb.append(numStr);
						}
					} else {
						sb.append("　");
						for (Entry<Position, Set<Position>> entry : candidates.entrySet()) {
							if (entry.getValue().size() == 1) {
								Position fixedFrom = entry.getKey();
								Position fixedTo = new ArrayList<>(entry.getValue()).get(0);
								if (fixedTo.equals(new Position(yIndex, xIndex))) {
									sb.setLength(sb.length() - 1);
									sb.append("■");
									break;
								} else if (isCross(fixedFrom, fixedTo, new Position(yIndex, xIndex),
										new Position(yIndex, xIndex))) {
									if (fixedTo.getyIndex() < fixedFrom.getyIndex()) {
										sb.setLength(sb.length() - 1);
										sb.append("↑");
									} else if (fixedTo.getxIndex() > fixedFrom.getxIndex()) {
										sb.setLength(sb.length() - 1);
										sb.append("→");
									} else if (fixedTo.getyIndex() > fixedFrom.getyIndex()) {
										sb.setLength(sb.length() - 1);
										sb.append("↓");
									} else if (fixedTo.getxIndex() < fixedFrom.getxIndex()) {
										sb.setLength(sb.length() - 1);
										sb.append("←");
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
			if (!rectSolve()) {
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
		 * 移動候補が確定している数字があるとき、その数字によって構成される領域が
		 * 四角形でない場合や、1マスのみの四角の場合はfalseを返す。
		 * また、上記の条件を回避できる移動候補が1通りに絞られる場合は確定する。
		 */
		private boolean rectSolve() {
			Set<Position> fixedPosSet = new HashSet<>();
			for (Entry<Position, Set<Position>> entry : candidates.entrySet()) {
				if (entry.getValue().size() == 1) {
					fixedPosSet.add(new ArrayList<>(entry.getValue()).get(0));
				}
			}
			while (!fixedPosSet.isEmpty()) {
				Position typicalWhitePos = new ArrayList<>(fixedPosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				setContinueFixedPosSet(typicalWhitePos, continuePosSet, fixedPosSet, null);
				if (continuePosSet.size() == 1) {
					// 四角形の孤立禁止
					Position pos = new ArrayList<>(continuePosSet).get(0);
					List<Position> canRectKeyList = new ArrayList<>();
					Position posUp = pos.getyIndex() == 0 ? null : new Position(pos.getyIndex() - 1, pos.getxIndex());
					Position posRight = pos.getxIndex() == getXLength() - 1 ? null
							: new Position(pos.getyIndex(), pos.getxIndex() + 1);
					Position posDown = pos.getyIndex() == getYLength() - 1 ? null
							: new Position(pos.getyIndex() + 1, pos.getxIndex());
					Position posLeft = pos.getxIndex() == 0 ? null : new Position(pos.getyIndex(), pos.getxIndex() - 1);
					Position usePos = null;
					if (posUp != null) {
						for (Entry<Position, Set<Position>> entry : candidates.entrySet()) {
							if (canRectKeyList.size() > 1) {
								break;
							}
							if (entry.getValue().contains(posUp)) {
								usePos = posUp;
								canRectKeyList.add(entry.getKey());
							}
						}
					}
					if (posRight != null) {
						for (Entry<Position, Set<Position>> entry : candidates.entrySet()) {
							if (canRectKeyList.size() > 1) {
								break;
							}
							if (entry.getValue().contains(posRight)) {
								usePos = posRight;
								canRectKeyList.add(entry.getKey());
							}
						}
					}
					if (posDown != null) {
						for (Entry<Position, Set<Position>> entry : candidates.entrySet()) {
							if (canRectKeyList.size() > 1) {
								break;
							}
							if (entry.getValue().contains(posDown)) {
								usePos = posDown;
								canRectKeyList.add(entry.getKey());
							}
						}
					}
					if (posLeft != null) {
						for (Entry<Position, Set<Position>> entry : candidates.entrySet()) {
							if (canRectKeyList.size() > 1) {
								break;
							}
							if (entry.getValue().contains(posLeft)) {
								usePos = posLeft;
								canRectKeyList.add(entry.getKey());
							}
						}
					}
					if (canRectKeyList.size() == 0) {
						return false;
					} else if (canRectKeyList.size() == 1) {
						candidates.get(canRectKeyList.get(0)).clear();
						candidates.get(canRectKeyList.get(0))
								.add(usePos);
					}
				} else {
					// 四角形の切りかけ禁止
					int minY = getYLength() - 1;
					int maxY = 0;
					int minX = getXLength() - 1;
					int maxX = 0;
					for (Position pos : continuePosSet) {
						if (pos.getyIndex() < minY) {
							minY = pos.getyIndex();
						}
						if (pos.getyIndex() > maxY) {
							maxY = pos.getyIndex();
						}
						if (pos.getxIndex() < minX) {
							minX = pos.getxIndex();
						}
						if (pos.getxIndex() > maxX) {
							maxX = pos.getxIndex();
						}
					}
					for (int yIndex = minY; yIndex <= maxY; yIndex++) {
						for (int xIndex = minX; xIndex <= maxX; xIndex++) {
							Position pos = new Position(yIndex, xIndex);
							if (!fixedPosSet.contains(pos)) {
								List<Position> canRectKeyList = new ArrayList<>();
								for (Entry<Position, Set<Position>> entry : candidates.entrySet()) {
									if (entry.getValue().contains(pos)) {
										canRectKeyList.add(entry.getKey());
										if (canRectKeyList.size() > 1) {
											break;
										}
									}
								}
								if (canRectKeyList.size() == 0) {
									return false;
								} else if (canRectKeyList.size() == 1) {
									candidates.get(canRectKeyList.get(0)).clear();
									candidates.get(canRectKeyList.get(0)).add(pos);
								}
							}
						}
					}
				}
				fixedPosSet.removeAll(continuePosSet);
			}
			return true;
		}

		/**
		 * posを起点に上下左右に確定マスを無制限につなげていく。
		 */
		private void setContinueFixedPosSet(Position pos, Set<Position> continuePosSet, Set<Position> fixedPosSet,
				Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (fixedPosSet.contains(nextPos) && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueFixedPosSet(nextPos, continuePosSet, fixedPosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (fixedPosSet.contains(nextPos) && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueFixedPosSet(nextPos, continuePosSet, fixedPosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (fixedPosSet.contains(nextPos) && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueFixedPosSet(nextPos, continuePosSet, fixedPosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (fixedPosSet.contains(nextPos) && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueFixedPosSet(nextPos, continuePosSet, fixedPosSet, Direction.RIGHT);
				}
			}
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
			for (Set<Position> oneCandidates : candidates.values()) {
				if (oneCandidates.size() != 1) {
					return false;
				}
			}
			return solveAndCheck();
		}
	}

	private final Field field;
	private int count = 0;

	public RectsliderSolver(int height, int width, String param) {
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
		System.out.println(new RectsliderSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 5).toString();
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
					count++;
					for (Iterator<Position> iterator = entry.getValue().iterator(); iterator
							.hasNext();) {
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
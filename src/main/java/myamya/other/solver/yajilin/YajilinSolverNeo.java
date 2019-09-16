package myamya.other.solver.yajilin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class YajilinSolverNeo implements Solver {

	/**
	 * 矢印のマス
	 */
	public static class Arrow {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final Direction direction;
		private final int count;

		public Arrow(Direction direction, int count) {
			this.direction = direction;
			this.count = count;
		}

		public Direction getDirection() {
			return direction;
		}

		public int getCount() {
			return count;
		}

		@Override
		public String toString() {
			return count == -1 ? "？" : (count >= 10 ? String.valueOf(count) : direction.toString() + count);
		}

		public String toStringWeb() {
			return count == -1 ? "？" : direction.getDirectString() + count;
		}
	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// 矢印のマス
		private final Arrow[][] arrows;
		// 数字の候補情報
		// 0矢印マス、1 └、2┌、、3┐、4┘、5│、6─、7■
		protected List<Integer>[][] numbersCand;
		// バリアントルール-ループ内黒ます禁止
		private final boolean out;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public Arrow[][] getArrows() {
			return arrows;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param, boolean out) {
			arrows = new Arrow[height][width];
			numbersCand = new ArrayList[height][width];
			this.out = out;
			int index = 0;
			Direction direction = null;
			boolean adjust = false;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				if (direction == null) {
					int interval = ALPHABET.indexOf(ch) + 1;
					if (interval != 0) {
						index = index + interval;
					} else {
						int val = Character.getNumericValue(ch);
						if (5 <= val && val <= 9) {
							val = val - 5;
							adjust = true;
						}
						direction = Direction.getByNum(val);
						if (direction == null) {
							if (adjust) {
								i++;
							} else {
								Position arrowPos = new Position(index / getXLength(), index % getXLength());
								arrows[arrowPos.getyIndex()][arrowPos.getxIndex()] = new Arrow(direction, -1);
							}
							index++;
							i++;
							adjust = false;
						}
					}
				} else {
					Position arrowPos = new Position(index / getXLength(), index % getXLength());
					Arrow arrow;
					if (adjust) {
						i++;
						arrow = new Arrow(direction, Character.getNumericValue(ch) * 16
								+ Character.getNumericValue(param.charAt(i)));
					} else {
						arrow = new Arrow(direction, Character.getNumericValue(ch));
					}
					arrows[arrowPos.getyIndex()][arrowPos.getxIndex()] = arrow;
					adjust = false;
					index++;
					direction = null;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					if (arrows[yIndex][xIndex] == null) {
						for (int i = 1; i <= 7; i++) {
							numbersCand[yIndex][xIndex].add(i);
						}
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			arrows = other.arrows;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			this.out = other.out;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "×└┌┐┘│─■";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 0) {
						if (arrows[yIndex][xIndex] == null) {
							sb.append("×");
						} else {
							sb.append(arrows[yIndex][xIndex]);
						}
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
						int index = HALF_NUMS.indexOf(numStr);
						if (index == 0) {
							sb.append("・");
						} else {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						}
					} else if (!numbersCand[yIndex][xIndex].contains(7)) {
						sb.append("・");
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
		 * 矢印に対し、指定した黒マス数を満たせなくなる場合falseを返す。
		 */
		private boolean arrowSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null) {
						Arrow arrow = arrows[yIndex][xIndex];
						Position pivot = new Position(yIndex, xIndex);
						int idx = 0;
						int blackCnt = 0;
						int spaceCnt = 0;
						boolean nextCanSpace = true;
						if (arrow.getDirection() == Direction.UP) {
							while (pivot.getyIndex() - 1 - idx >= 0) {
								Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
								List<Integer> oneCand = numbersCand[pos.getyIndex()][pos.getxIndex()];
								if (oneCand.size() == 1 && oneCand.contains(7)) {
									blackCnt++;
									if (arrow.getCount() < blackCnt) {
										return false;
									}
									nextCanSpace = false;
								} else if (oneCand.contains(7)) {
									if (nextCanSpace) {
										spaceCnt++;
										nextCanSpace = false;
									} else {
										nextCanSpace = true;
									}
								} else {
									nextCanSpace = true;
								}
								idx++;
							}
						}
						if (arrow.getDirection() == Direction.RIGHT) {
							while (pivot.getxIndex() + 1 + idx < getXLength()) {
								Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() + 1 + idx);
								List<Integer> oneCand = numbersCand[pos.getyIndex()][pos.getxIndex()];
								if (oneCand.size() == 1 && oneCand.contains(7)) {
									blackCnt++;
									if (arrow.getCount() < blackCnt) {
										return false;
									}
									nextCanSpace = false;
								} else if (oneCand.contains(7)) {
									if (nextCanSpace) {
										spaceCnt++;
										nextCanSpace = false;
									} else {
										nextCanSpace = true;
									}
								} else {
									nextCanSpace = true;
								}
								idx++;
							}
						}
						if (arrow.getDirection() == Direction.DOWN) {
							while (pivot.getyIndex() + 1 + idx < getYLength()) {
								Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex());
								List<Integer> oneCand = numbersCand[pos.getyIndex()][pos.getxIndex()];
								if (oneCand.size() == 1 && oneCand.contains(7)) {
									blackCnt++;
									if (arrow.getCount() < blackCnt) {
										return false;
									}
									nextCanSpace = false;
								} else if (oneCand.contains(7)) {
									if (nextCanSpace) {
										spaceCnt++;
										nextCanSpace = false;
									} else {
										nextCanSpace = true;
									}
								} else {
									nextCanSpace = true;
								}
								idx++;
							}
						}
						if (arrow.getDirection() == Direction.LEFT) {
							while (pivot.getxIndex() - 1 - idx >= 0) {
								Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
								List<Integer> oneCand = numbersCand[pos.getyIndex()][pos.getxIndex()];
								if (oneCand.size() == 1 && oneCand.contains(7)) {
									blackCnt++;
									if (arrow.getCount() < blackCnt) {
										return false;
									}
									nextCanSpace = false;
								} else if (oneCand.contains(7)) {
									if (nextCanSpace) {
										spaceCnt++;
										nextCanSpace = false;
									} else {
										nextCanSpace = true;
									}
								} else {
									nextCanSpace = true;
								}
								idx++;
							}
						}
						if (arrow.getCount() > blackCnt + spaceCnt) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 周囲のマスから候補を消す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] == null) {
						List<Integer> oneCand = numbersCand[yIndex][xIndex];
						List<Integer> upCand = null, rightCand = null, downCand = null, leftCand = null;
						if (yIndex != 0 && arrows[yIndex - 1][xIndex] == null) {
							upCand = numbersCand[yIndex - 1][xIndex];
						}
						if (xIndex != getXLength() - 1 && arrows[yIndex][xIndex + 1] == null) {
							rightCand = numbersCand[yIndex][xIndex + 1];
						}
						if (yIndex != getYLength() - 1 && arrows[yIndex + 1][xIndex] == null) {
							downCand = numbersCand[yIndex + 1][xIndex];
						}
						if (xIndex != 0 && arrows[yIndex][xIndex - 1] == null) {
							leftCand = numbersCand[yIndex][xIndex - 1];
						}
						// 黒マス連続禁止
						if ((upCand != null && upCand.size() == 1 && upCand.get(0) == 7)
								|| (rightCand != null && rightCand.size() == 1 && rightCand.get(0) == 7)
								|| (downCand != null && downCand.size() == 1 && downCand.get(0) == 7)
								|| (leftCand != null && leftCand.size() == 1 && leftCand.get(0) == 7)) {
							oneCand.remove(new Integer(7));
							if (oneCand.size() == 0) {
								return false;
							}
						}
						// 確実に上から来る
						if (upCand != null && !upCand.contains(1) && !upCand.contains(4) && !upCand.contains(6)
								&& !upCand.contains(7)) {
							oneCand.remove(new Integer(2));
							oneCand.remove(new Integer(3));
							oneCand.remove(new Integer(6));
							oneCand.remove(new Integer(7));
							if (oneCand.size() == 0) {
								return false;
							}
						}
						// 確実に上から来ない
						if (upCand == null || (!upCand.contains(2) && !upCand.contains(3) && !upCand.contains(5))) {
							oneCand.remove(new Integer(1));
							oneCand.remove(new Integer(4));
							oneCand.remove(new Integer(5));
							if (oneCand.size() == 0) {
								return false;
							}
						}
						// 確実に右から来る
						if (rightCand != null && !rightCand.contains(1) && !rightCand.contains(2)
								&& !rightCand.contains(5)
								&& !rightCand.contains(7)) {
							oneCand.remove(new Integer(3));
							oneCand.remove(new Integer(4));
							oneCand.remove(new Integer(5));
							oneCand.remove(new Integer(7));
							if (oneCand.size() == 0) {
								return false;
							}
						}
						// 確実に右から来ない
						if (rightCand == null
								|| (!rightCand.contains(3) && !rightCand.contains(4) && !rightCand.contains(6))) {
							oneCand.remove(new Integer(1));
							oneCand.remove(new Integer(2));
							oneCand.remove(new Integer(6));
							if (oneCand.size() == 0) {
								return false;
							}
						}
						// 確実に下から来る
						if (downCand != null && !downCand.contains(2) && !downCand.contains(3) && !downCand.contains(6)
								&& !downCand.contains(7)) {
							oneCand.remove(new Integer(1));
							oneCand.remove(new Integer(4));
							oneCand.remove(new Integer(6));
							oneCand.remove(new Integer(7));
							if (oneCand.size() == 0) {
								return false;
							}
						}
						// 確実に下から来ない
						if (downCand == null
								|| (!downCand.contains(1) && !downCand.contains(4) && !downCand.contains(5))) {
							oneCand.remove(new Integer(2));
							oneCand.remove(new Integer(3));
							oneCand.remove(new Integer(5));
							if (oneCand.size() == 0) {
								return false;
							}
						}
						// 確実に左から来る
						if (leftCand != null && !leftCand.contains(3) && !leftCand.contains(4) && !leftCand.contains(5)
								&& !leftCand.contains(7)) {
							oneCand.remove(new Integer(1));
							oneCand.remove(new Integer(2));
							oneCand.remove(new Integer(5));
							oneCand.remove(new Integer(7));
							if (oneCand.size() == 0) {
								return false;
							}

						}
						// 確実に左から来ない
						if (leftCand == null
								|| (!leftCand.contains(1) && !leftCand.contains(2) && !leftCand.contains(6))) {
							oneCand.remove(new Integer(3));
							oneCand.remove(new Integer(4));
							oneCand.remove(new Integer(6));
							if (oneCand.size() == 0) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] == null && !numbersCand[yIndex][xIndex].contains(7)) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.isEmpty()) {
							whitePosSet.add(whitePos);
							setContinuePosSet(whitePos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(whitePos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if ((numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(2) ||
						numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(3) ||
						numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(5))
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if ((numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(3) ||
						numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(4) ||
						numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(6))
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if ((numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(1) ||
						numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(4) ||
						numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(5))
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if ((numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(1) ||
						numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(2) ||
						numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(6))
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (out) {
					if (!outsideSolve()) {
						return false;
					}
				}
				if (!arrowSolve()) {
					return false;
				}
				if (!connectSolve()) {
					return false;
				}
				if (!oddSolve()) {
					return false;
				}
			}
			return true;
		}

		private boolean outsideSolve() {
			// TODO 自動生成されたメソッド・スタブ
			return true;
		}

		/**
		 * ヤジリンのルール上、各列をふさぐ壁は必ず偶数になる。
		 * 偶数になっていない場合falseを返す。
		 */
		private boolean oddSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				int notExistsCount = 0;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					List<Integer> oneCand = numbersCand[yIndex][xIndex];
					if (arrows[yIndex][xIndex] == null) {
						if (!oneCand.contains(1) && !oneCand.contains(4) && !oneCand.contains(6)
								&& !oneCand.contains(7)) {
							notExistsCount++;
						} else if (oneCand.contains(2) || oneCand.contains(3) || oneCand.contains(5)) {
							notExistsCount = 0;
							break;
						}
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
				int notExistsCount = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					List<Integer> oneCand = numbersCand[yIndex][xIndex];
					if (arrows[yIndex][xIndex] == null) {
						if (!oneCand.contains(3) && !oneCand.contains(4) && !oneCand.contains(5)
								&& !oneCand.contains(7)) {
							notExistsCount++;
						} else if (oneCand.contains(1) || oneCand.contains(2) || oneCand.contains(6)) {
							notExistsCount = 0;
							break;
						}
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] == null && numbersCand[yIndex][xIndex].size() != 1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public YajilinSolverNeo(int height, int width, String param, boolean out) {
		field = new Field(height, width, param, false);
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
		System.out.println(new YajilinSolverNeo(height, width, param, false).solve());
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
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.arrows[yIndex][xIndex] == null && field.numbersCand[yIndex][xIndex].size() != 1) {
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
		return true;
	}
}
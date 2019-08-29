package myamya.other.solver.fillmat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class FillmatSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// 固定数字(表示用)
		private final Integer[][] numbers;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;

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
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int number = 1; number <= 4; number++) {
						numbersCand[yIndex][xIndex].add(number);
					}
				}
			}
			int readPos = 0;
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET.indexOf(ch);
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
						} else {
							num = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
						numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(num);
					}
					index++;
				}
			}

		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
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
		 * 部屋に固定数字が2つあったり、サイズが自分の数字を超えてはならない。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1) {
						Position pivot = new Position(yIndex, xIndex);
						Integer size = numbersCand[yIndex][xIndex].get(0);
						boolean foundNum = numbers[pivot.getyIndex()][pivot.getxIndex()] != null;
						Set<Position> continueWhitePosSet = new HashSet<>();
						continueWhitePosSet.add(pivot);
						if (!(setContinuePosSetContainsDoubleNumber(pivot, continueWhitePosSet, null,
								size, foundNum))) {
							return false;
						}
						Set<Position> continueNotBlackPosSet = new HashSet<>();
						continueNotBlackPosSet.add(pivot);
						if (!setContinuePosSet(pivot, continueNotBlackPosSet, null, size, foundNum)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 壁なし確定マスをつなげる。想定サイズを超えたり、数字を2個以上取り込んだら-1を返す。
		 * それ以外の場合は取り込んだ数字を返す。
		 * @param foundNum
		 */
		private boolean setContinuePosSetContainsDoubleNumber(Position pos, Set<Position> continuePosSet,
				Direction from, int size, boolean foundNum) {
			if (continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && (from == Direction.DOWN || from == null)) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (foundNum) {
							return false;
						} else {
							continuePosSet.add(nextPos);
							if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.DOWN,
									size, true))) {
								return false;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.DOWN, size,
								foundNum))) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && (from == Direction.LEFT || from == null)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (foundNum) {
							return false;
						} else {
							continuePosSet.add(nextPos);
							if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.LEFT, size,
									true))) {
								return false;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.LEFT, size,
								foundNum))) {
							return false;
						}
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && (from == Direction.UP || from == null)) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (foundNum) {
							return false;
						} else {
							continuePosSet.add(nextPos);
							if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.UP, size,
									true))) {
								return false;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.UP, size,
								foundNum))) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != 0 && (from == Direction.RIGHT || from == null)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (foundNum) {
							return false;
						} else {
							continuePosSet.add(nextPos);
							if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.RIGHT, size,
									true))) {
								return false;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.RIGHT, size,
								foundNum))) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字を2個以上取り込まない範囲で壁未確定マスをつなげる。
		 * @param foundNum
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet,
				Direction from, int size, boolean foundNum) {
			if (continuePosSet.size() >= size) {
				return true;
			}
			if (pos.getyIndex() != 0 && (from == Direction.DOWN || from == null)) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (!foundNum) {
							continuePosSet.add(nextPos);
							if (setContinuePosSet(nextPos, continuePosSet, Direction.DOWN,
									size, true)) {
								return true;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (setContinuePosSet(nextPos, continuePosSet, Direction.DOWN, size, foundNum)) {
							return true;
						}
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && (from == Direction.LEFT || from == null)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (!foundNum) {
							continuePosSet.add(nextPos);
							if (setContinuePosSet(nextPos, continuePosSet, Direction.LEFT,
									size, true)) {
								return true;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (setContinuePosSet(nextPos, continuePosSet, Direction.LEFT, size, foundNum)) {
							return true;
						}
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && (from == Direction.UP || from == null)) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (!foundNum) {
							continuePosSet.add(nextPos);
							if (setContinuePosSet(nextPos, continuePosSet, Direction.UP,
									size, true)) {
								return true;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (setContinuePosSet(nextPos, continuePosSet, Direction.UP, size, foundNum)) {
							return true;
						}
					}
				}
			}
			if (pos.getxIndex() != 0 && (from == Direction.RIGHT || from == null)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (!foundNum) {
							continuePosSet.add(nextPos);
							if (setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT,
									size, true)) {
								return true;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT, size, foundNum)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		/**
		 * 同じ数字が斜めに来ることはない。
		 */
		private boolean wallSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1) {
						Integer num = numbersCand[yIndex][xIndex].get(0);
						List<Integer> urCands = yIndex == 0 || xIndex == getXLength() - 1 ? null
								: numbersCand[yIndex - 1][xIndex + 1];
						List<Integer> rdCands = xIndex == getXLength() - 1 || yIndex == getYLength() - 1 ? null
								: numbersCand[yIndex + 1][xIndex + 1];
						List<Integer> dlCands = yIndex == getYLength() - 1 || xIndex == 0 ? null
								: numbersCand[yIndex + 1][xIndex - 1];
						List<Integer> luCands = xIndex == 0 || yIndex == 0 ? null : numbersCand[yIndex - 1][xIndex - 1];
						if (urCands != null) {
							urCands.remove(num);
							if (urCands.isEmpty()) {
								return false;
							}
						}
						if (rdCands != null) {
							rdCands.remove(num);
							if (rdCands.isEmpty()) {
								return false;
							}
						}
						if (dlCands != null) {
							dlCands.remove(num);
							if (dlCands.isEmpty()) {
								return false;
							}
						}
						if (luCands != null) {
							luCands.remove(num);
							if (luCands.isEmpty()) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 2x2のマスのうち、どこか縦横に2マスは連続が必須。
		 */
		private boolean pileSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				outer: for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					List<Integer> cands1 = numbersCand[yIndex][xIndex];
					List<Integer> cands2 = numbersCand[yIndex][xIndex + 1];
					List<Integer> cands3 = numbersCand[yIndex + 1][xIndex + 1];
					List<Integer> cands4 = numbersCand[yIndex + 1][xIndex];
					for (Integer i : cands2) {
						if (cands1.contains(i)) {
							continue outer;
						}
					}
					for (Integer i : cands3) {
						if (cands2.contains(i)) {
							continue outer;
						}
					}
					for (Integer i : cands4) {
						if (cands3.contains(i)) {
							continue outer;
						}
					}
					for (Integer i : cands1) {
						if (cands4.contains(i)) {
							continue outer;
						}
					}
					return false;
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!wallSolve()) {
				return false;
			}
			if (!pileSolve()) {
				return false;
			}
			if (!roomSolve()) {
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

	public FillmatSolver(int height, int width, String param) {
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
		System.out.println(new FillmatSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 10));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 10).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
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
		return true;
	}
}
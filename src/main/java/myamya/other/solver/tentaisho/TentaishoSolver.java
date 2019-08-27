package myamya.other.solver.tentaisho;

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

public class TentaishoSolver implements Solver {

	public static class Field {

		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		private final int height;
		private final int width;

		// 確定数字の情報。ここでのPositionは長さ10(0-9)の場合は19通り(0-18)の候補を取ることに注意。
		private final Map<Integer, Position> numbers;
		// 確定数字が白か黒か。黒ならtrue
		private final Map<Integer, Boolean> isBlack;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;
		// 領域の確定した数字
		private Set<Integer> fixedNumber;

		public Map<Integer, Position> getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return height;
		}

		public int getXLength() {
			return width;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			this.height = height;
			this.width = width;
			numbersCand = new ArrayList[height][width];
			numbers = new HashMap<>();
			isBlack = new HashMap<>();
			int index = 0;
			int number = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					if (ch == '.') {
						//
					} else {
						Position pos = new Position(index / (getXLength() * 2 - 1), index % (getXLength() * 2 - 1));
						int numelicValue = Character.getNumericValue(ch);
						numbers.put(number, pos);
						isBlack.put(number, numelicValue % 2 != 0);
						numbersCand[pos.getyIndex() / 2][pos.getxIndex() / 2] = new ArrayList<>();
						numbersCand[pos.getyIndex() / 2][pos.getxIndex() / 2].add(number);
						if (pos.getyIndex() % 2 != 0) {
							numbersCand[(pos.getyIndex() + 1) / 2][pos.getxIndex() / 2] = new ArrayList<>();
							numbersCand[(pos.getyIndex() + 1) / 2][pos.getxIndex() / 2].add(number);
						}
						if (pos.getxIndex() % 2 != 0) {
							numbersCand[pos.getyIndex() / 2][(pos.getxIndex() + 1) / 2] = new ArrayList<>();
							numbersCand[pos.getyIndex() / 2][(pos.getxIndex() + 1) / 2].add(number);
						}
						if (pos.getyIndex() % 2 != 0 && pos.getxIndex() % 2 != 0) {
							numbersCand[(pos.getyIndex() + 1) / 2][(pos.getxIndex() + 1) / 2] = new ArrayList<>();
							numbersCand[(pos.getyIndex() + 1) / 2][(pos.getxIndex() + 1) / 2].add(number);
						}
						number++;
						index = index + numelicValue / 2;
					}
					index++;
				}
			}
			// 初期候補数字を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex] == null) {
						numbersCand[yIndex][xIndex] = new ArrayList<>();
						for (Integer i : numbers.keySet()) {
							numbersCand[yIndex][xIndex].add(i);
						}
					}
				}
			}
			fixedNumber = new HashSet<>();
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			isBlack = other.isBlack;
			height = other.height;
			width = other.width;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			fixedNumber = new HashSet<>(other.fixedNumber);
		}

		private static final String HALF_NUMS_36 = "0 1 2 3 4 5 6 7 8 9 a b c d e f g h i j k l m n o p q r s t u v w x y z ";
		private static final String FULL_NUMS_36 = "０１２３４５６７８９ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						String numStr = Integer.toString(numbersCand[yIndex][xIndex].get(0), 36);
						int index = HALF_NUMS_36.indexOf(numStr);
						if (index >= 0) {
							sb.append(FULL_NUMS_36.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(numStr);
						}
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
		 * ある数字が確定している場合、点対称のマスも確定する。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1) {
						Integer number = numbersCand[yIndex][xIndex].get(0);
						if (fixedNumber.contains(number)) {
							continue;
						}
						Position pivot = numbers.get(number);
						Position pos = new Position(yIndex, xIndex);
						Position anotherPos = new Position((pivot.getyIndex() * 2 - pos.getyIndex() * 2) / 2,
								(pivot.getxIndex() * 2 - pos.getxIndex() * 2) / 2);
						if (anotherPos.getyIndex() < 0 || anotherPos.getyIndex() >= getYLength()
								|| anotherPos.getxIndex() < 0 || anotherPos.getxIndex() >= getXLength()
								|| !numbersCand[anotherPos.getyIndex()][anotherPos.getxIndex()].contains(number)) {
							return false;
						} else {
							numbersCand[anotherPos.getyIndex()][anotherPos.getxIndex()].clear();
							numbersCand[anotherPos.getyIndex()][anotherPos.getxIndex()].add(number);
						}
					}
				}
			}
			return true;
		}

		/**
		 * 基準マスに対して点対称のマスが回収不可能の場合、もう一方も回収できない。
		 */
		public boolean targetSolve() {
			for (Entry<Integer, Position> entry : numbers.entrySet()) {
				Integer number = entry.getKey();
				if (fixedNumber.contains(number)) {
					continue;
				}
				Position pivot = entry.getValue();
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						Position pos = new Position(yIndex, xIndex);
						Position anotherPos = new Position((pivot.getyIndex() * 2 - pos.getyIndex() * 2) / 2,
								(pivot.getxIndex() * 2 - pos.getxIndex() * 2) / 2);
						if (anotherPos.getyIndex() < 0 || anotherPos.getyIndex() >= getYLength()
								|| anotherPos.getxIndex() < 0 || anotherPos.getxIndex() >= getXLength()
								|| !numbersCand[anotherPos.getyIndex()][anotherPos.getxIndex()].contains(number)) {
							numbersCand[yIndex][xIndex].remove(number);
						}
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字がつながっていないマスは回収できない。
		 */
		public boolean connectSolve() {
			for (Entry<Integer, Position> entry : numbers.entrySet()) {
				Integer number = entry.getKey();
				if (fixedNumber.contains(number)) {
					continue;
				}
				Set<Position> numberPosSet = new HashSet<>();
				Position numberPos = new Position(entry.getValue().getyIndex() / 2, entry.getValue().getxIndex() / 2);
				numberPosSet.add(numberPos);
				Set<Object> token = new HashSet<>();
				setContinuePosSet(number, numberPos, numberPosSet, null, token);
				if (token.isEmpty()) {
					// トークンが空なら、その数字の領域は確定。
					fixedNumber.add(number);
				}
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (!numberPosSet.contains(new Position(yIndex, xIndex))) {
							numbersCand[yIndex][xIndex].remove(number);
						}
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右にnumを含むマスを無制限につなげていく。
		 * 候補が限られない場合はtokenにダミー情報を入れる。
		 */
		private void setContinuePosSet(Integer number, Position pos, Set<Position> continuePosSet, Direction from,
				Set<Object> token) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(number))) {
					continuePosSet.add(nextPos);
					if (token.isEmpty() && numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() != 1) {
						token.add(0);
					}
					setContinuePosSet(number, nextPos, continuePosSet, Direction.DOWN, token);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(number))) {
					continuePosSet.add(nextPos);
					if (token.isEmpty() && numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() != 1) {
						token.add(0);
					}
					setContinuePosSet(number, nextPos, continuePosSet, Direction.LEFT, token);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(number))) {
					continuePosSet.add(nextPos);
					if (token.isEmpty() && numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() != 1) {
						token.add(0);
					}
					setContinuePosSet(number, nextPos, continuePosSet, Direction.UP, token);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(number))) {
					continuePosSet.add(nextPos);
					if (token.isEmpty() && numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() != 1) {
						token.add(0);
					}
					setContinuePosSet(number, nextPos, continuePosSet, Direction.RIGHT, token);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			if (!numberSolve()) {
				return false;
			}
			if (!targetSolve()) {
				return false;
			}
			if (!connectSolve()) {
				return false;
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

	public TentaishoSolver(int height, int width, String param) {
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
		System.out.println(new TentaishoSolver(height, width, param).solve());
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
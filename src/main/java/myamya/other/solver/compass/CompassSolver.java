package myamya.other.solver.compass;

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

public class CompassSolver implements Solver {

	public static class Compass {

		@Override
		public String toString() {
			return "[pos=" + pos + ", upCnt=" + upCnt + ", rightCnt=" + rightCnt + ", downCnt=" + downCnt
					+ ", leftCnt=" + leftCnt + "]";
		}

		private final Position pos;
		private final int upCnt;
		private final int rightCnt;
		private final int downCnt;
		private final int leftCnt;

		public Compass(Position pos, int upCnt, int rightCnt, int downCnt, int leftCnt) {
			this.pos = pos;
			this.upCnt = upCnt;
			this.rightCnt = rightCnt;
			this.downCnt = downCnt;
			this.leftCnt = leftCnt;
		}

		public Position getPos() {
			return pos;
		}

		public int getUpCnt() {
			return upCnt;
		}

		public int getRightCnt() {
			return rightCnt;
		}

		public int getDownCnt() {
			return downCnt;
		}

		public int getLeftCnt() {
			return leftCnt;
		}

	}

	public static class Field {

		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		private final int height;
		private final int width;

		// 確定コンパスの位置情報。キーは数字
		private final Map<Integer, Compass> compasses;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;

		public Map<Integer, Compass> getCompasses() {
			return compasses;
		}

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
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
			compasses = new HashMap<>();
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
					Position pos = new Position(index / getXLength(), index % getXLength());
					final int upCnt;
					if (ch == '.') {
						upCnt = -1;
					} else {
						if (ch == '-') {
							upCnt = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							upCnt = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							upCnt = Integer.parseInt(String.valueOf(ch), 16);
						}
					}
					i++;
					ch = param.charAt(i);
					final int downCnt;
					if (ch == '.') {
						downCnt = -1;
					} else {
						if (ch == '-') {
							downCnt = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							downCnt = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							downCnt = Integer.parseInt(String.valueOf(ch), 16);
						}
					}
					i++;
					ch = param.charAt(i);
					final int leftCnt;
					if (ch == '.') {
						leftCnt = -1;
					} else {
						if (ch == '-') {
							leftCnt = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							leftCnt = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							leftCnt = Integer.parseInt(String.valueOf(ch), 16);
						}
					}
					i++;
					ch = param.charAt(i);
					final int rightCnt;
					if (ch == '.') {
						rightCnt = -1;
					} else {
						if (ch == '-') {
							rightCnt = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							rightCnt = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							rightCnt = Integer.parseInt(String.valueOf(ch), 16);
						}
					}
					compasses.put(number, new Compass(pos, upCnt, rightCnt, downCnt, leftCnt));
					number++;
					index++;
				}

			}
			// 初期候補数字を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex] == null) {
						numbersCand[yIndex][xIndex] = new ArrayList<>();
						for (Integer i : compasses.keySet()) {
							numbersCand[yIndex][xIndex].add(i);
						}
					}
				}
			}
			// コンパスは自分の領土になる
			for (Entry<Integer, Compass> entry : compasses.entrySet()) {
				numbersCand[entry.getValue().getPos().getyIndex()][entry.getValue().getPos().getxIndex()].clear();
				numbersCand[entry.getValue().getPos().getyIndex()][entry.getValue().getPos().getxIndex()]
						.add(entry.getKey());
			}

		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			compasses = other.compasses;
			height = other.height;
			width = other.width;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
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
		 * コンパスから見て、自分と同じ数字が向き先の方向に同じ数だけある。
		 */
		private boolean numberSolve() {
			for (Entry<Integer, Compass> entry : compasses.entrySet()) {
				Integer number = entry.getKey();
				Compass compass = entry.getValue();
				if (compass.getUpCnt() != -1) {
					int useCnt = compass.getUpCnt();
					int fixedCnt = 0;
					int candCnt = 0;
					for (int yIndex = 0; yIndex < compass.getPos().getyIndex(); yIndex++) {
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (numbersCand[yIndex][xIndex].contains(number)) {
								if (numbersCand[yIndex][xIndex].size() == 1) {
									fixedCnt++;
								} else {
									candCnt++;
								}
							}
						}
					}
					if (fixedCnt > useCnt) {
						// マスが多すぎる
						return false;
					}
					if (fixedCnt + candCnt < useCnt) {
						// マスが少なすぎる
						return false;
					}
					if (fixedCnt == useCnt) {
						// 確定マス以外の領土がないことが確定
						for (int yIndex = 0; yIndex < compass.getPos().getyIndex(); yIndex++) {
							for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
								if (numbersCand[yIndex][xIndex].contains(number)
										&& (numbersCand[yIndex][xIndex].size() == 1)) {
									//
								} else {
									numbersCand[yIndex][xIndex].remove(number);
									if (numbersCand[yIndex][xIndex].isEmpty()) {
										return false;
									}
								}
							}
						}
					}
					if (fixedCnt + candCnt == useCnt) {
						// 未確定マスが領土に確定
						for (int yIndex = 0; yIndex < compass.getPos().getyIndex(); yIndex++) {
							for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
								if (numbersCand[yIndex][xIndex].contains(number)
										&& (numbersCand[yIndex][xIndex].size() != 1)) {
									numbersCand[yIndex][xIndex].clear();
									numbersCand[yIndex][xIndex].add(number);
								}
							}
						}
					}
				}
				if (compass.getRightCnt() != -1) {
					int useCnt = compass.getRightCnt();
					int fixedCnt = 0;
					int candCnt = 0;
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						for (int xIndex = compass.getPos().getxIndex() + 1; xIndex < getXLength(); xIndex++) {
							if (numbersCand[yIndex][xIndex].contains(number)) {
								if (numbersCand[yIndex][xIndex].size() == 1) {
									fixedCnt++;
								} else {
									candCnt++;
								}
							}
						}
					}
					if (fixedCnt > useCnt) {
						// マスが多すぎる
						return false;
					}
					if (fixedCnt + candCnt < useCnt) {
						// マスが少なすぎる
						return false;
					}
					if (fixedCnt == useCnt) {
						// 確定マス以外の領土がないことが確定
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							for (int xIndex = compass.getPos().getxIndex() + 1; xIndex < getXLength(); xIndex++) {
								if (numbersCand[yIndex][xIndex].contains(number)
										&& (numbersCand[yIndex][xIndex].size() == 1)) {
									//
								} else {
									numbersCand[yIndex][xIndex].remove(number);
									if (numbersCand[yIndex][xIndex].isEmpty()) {
										return false;
									}
								}
							}
						}
					}
					if (fixedCnt + candCnt == useCnt) {
						// 未確定マスが領土に確定
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							for (int xIndex = compass.getPos().getxIndex() + 1; xIndex < getXLength(); xIndex++) {
								if (numbersCand[yIndex][xIndex].contains(number)
										&& (numbersCand[yIndex][xIndex].size() != 1)) {
									numbersCand[yIndex][xIndex].clear();
									numbersCand[yIndex][xIndex].add(number);
								}
							}
						}
					}
				}
				if (compass.getDownCnt() != -1) {
					int useCnt = compass.getDownCnt();
					int fixedCnt = 0;
					int candCnt = 0;
					for (int yIndex = compass.getPos().getyIndex() + 1; yIndex < getYLength(); yIndex++) {
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (numbersCand[yIndex][xIndex].contains(number)) {
								if (numbersCand[yIndex][xIndex].size() == 1) {
									fixedCnt++;
								} else {
									candCnt++;
								}
							}
						}
					}
					if (fixedCnt > useCnt) {
						// マスが多すぎる
						return false;
					}
					if (fixedCnt + candCnt < useCnt) {
						// マスが少なすぎる
						return false;
					}
					if (fixedCnt == useCnt) {
						// 確定マス以外の領土がないことが確定
						for (int yIndex = compass.getPos().getyIndex() + 1; yIndex < getYLength(); yIndex++) {
							for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
								if (numbersCand[yIndex][xIndex].contains(number)
										&& (numbersCand[yIndex][xIndex].size() == 1)) {
									//
								} else {
									numbersCand[yIndex][xIndex].remove(number);
									if (numbersCand[yIndex][xIndex].isEmpty()) {
										return false;
									}
								}
							}
						}
					}
					if (fixedCnt + candCnt == useCnt) {
						// 未確定マスが領土に確定
						for (int yIndex = compass.getPos().getyIndex() + 1; yIndex < getYLength(); yIndex++) {
							for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
								if (numbersCand[yIndex][xIndex].contains(number)
										&& (numbersCand[yIndex][xIndex].size() != 1)) {
									numbersCand[yIndex][xIndex].clear();
									numbersCand[yIndex][xIndex].add(number);
								}
							}
						}
					}
				}
				if (compass.getLeftCnt() != -1) {
					int useCnt = compass.getLeftCnt();
					int fixedCnt = 0;
					int candCnt = 0;
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						for (int xIndex = 0; xIndex < compass.getPos().getxIndex(); xIndex++) {
							if (numbersCand[yIndex][xIndex].contains(number)) {
								if (numbersCand[yIndex][xIndex].size() == 1) {
									fixedCnt++;
								} else {
									candCnt++;
								}
							}
						}
					}
					if (fixedCnt > useCnt) {
						// マスが多すぎる
						return false;
					}
					if (fixedCnt + candCnt < useCnt) {
						// マスが少なすぎる
						return false;
					}
					if (fixedCnt == useCnt) {
						// 確定マス以外の領土がないことが確定
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							for (int xIndex = 0; xIndex < compass.getPos().getxIndex(); xIndex++) {
								if (numbersCand[yIndex][xIndex].contains(number)
										&& (numbersCand[yIndex][xIndex].size() == 1)) {
									//
								} else {
									numbersCand[yIndex][xIndex].remove(number);
									if (numbersCand[yIndex][xIndex].isEmpty()) {
										return false;
									}
								}
							}
						}
					}
					if (fixedCnt + candCnt == useCnt) {
						// 未確定マスが領土に確定
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							for (int xIndex = 0; xIndex < compass.getPos().getxIndex(); xIndex++) {
								if (numbersCand[yIndex][xIndex].contains(number)
										&& (numbersCand[yIndex][xIndex].size() != 1)) {
									numbersCand[yIndex][xIndex].clear();
									numbersCand[yIndex][xIndex].add(number);
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * コンパスから見て数字がつながっていないマスは回収できない。
		 */
		public boolean connectSolve() {
			for (Entry<Integer, Compass> entry : compasses.entrySet()) {
				Integer number = entry.getKey();
				Set<Position> numberPosSet = new HashSet<>();
				Position numberPos = new Position(entry.getValue().getPos().getyIndex(),
						entry.getValue().getPos().getxIndex());
				numberPosSet.add(numberPos);
				setContinuePosSet(number, numberPos, numberPosSet, null);
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (!numberPosSet.contains(new Position(yIndex, xIndex))) {
							numbersCand[yIndex][xIndex].remove(number);
							if (numbersCand[yIndex][xIndex].size() == 0) {
								return false;
							}
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
		private void setContinuePosSet(Integer number, Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(number))) {
					continuePosSet.add(nextPos);
					setContinuePosSet(number, nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(number))) {
					continuePosSet.add(nextPos);
					setContinuePosSet(number, nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(number))) {
					continuePosSet.add(nextPos);
					setContinuePosSet(number, nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(number))) {
					continuePosSet.add(nextPos);
					setContinuePosSet(number, nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
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

	public CompassSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		// https://puzz.link/p?compass/6/6/s6...3...l7386m..3.h...3i
		String url = "https://puzz.link/p?compass/5/5/g....p7386p3.3.g"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new CompassSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			count++;
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
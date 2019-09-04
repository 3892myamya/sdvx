package myamya.other.solver.tasquare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Solver;

public class TasquareSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の情報
		private final Integer[][] numbers;
		// 四角の配置の候補
		private List<Sikaku> squareCand;
		// 確定した四角候補
		private List<Sikaku> squareFixed;

		public Integer[][] getNumbers() {
			return numbers;
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
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int capacity;
					if (ch == '.') {
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
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
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
			squareCand = new ArrayList<>();
			squareFixed = new ArrayList<>();
			// 部屋の切り方の候補をあらかじめ決めておき、その候補を順次減らす方法を取る。
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					for (int ySize = 1; ySize <= getYLength(); ySize++) {
						for (int xSize = 1; xSize <= getXLength(); xSize++) {
							if (ySize != xSize) {
								continue;
							}
							int maxY = yIndex + ySize > getYLength() ? getYLength() - ySize : yIndex;
							int maxX = xIndex + xSize > getXLength() ? getXLength() - xSize : xIndex;
							for (int y = yIndex; y <= maxY; y++) {
								for (int x = xIndex; x <= maxX; x++) {
									Sikaku sikaku = new Sikaku(new Position(y, x),
											new Position(y + ySize - 1, x + xSize - 1));
									boolean addSikaku = true;
									// 他の部屋のpivotが含まれる候補をあらかじめ除外する。
									outer: for (int otherY = 0; otherY < getYLength(); otherY++) {
										for (int otherX = 0; otherX < getXLength(); otherX++) {
											if (numbers[otherY][otherX] != null) {
												Position otherPos = new Position(otherY, otherX);
												if (sikaku.isDuplicate(otherPos)) {
													addSikaku = false;
													break outer;
												}
											}
										}
									}
									if (addSikaku) {
										squareCand.add(sikaku);
									}
								}
							}
						}
					}
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			squareCand = new ArrayList<>(other.squareCand);
			squareFixed = new ArrayList<>(other.squareFixed);
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						} else if (numbers[yIndex][xIndex] == -1) {
							sb.append("□");
						} else {
							String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(capacityStr);
							if (index >= 0) {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							} else {
								sb.append(capacityStr);
							}
						}
					} else {
						boolean found = false;
						for (Sikaku square : squareFixed) {
							if (square.isDuplicate(new Position(yIndex, xIndex))) {
								found = true;
								break;
							}
						}
						if (found) {
							sb.append("■");
						} else {
							for (Sikaku square : squareCand) {
								if (square.isDuplicate(new Position(yIndex, xIndex))) {
									found = true;
									break;
								}
							}
							sb.append(found ? "　" : "・");
						}
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			sb.append(squareFixed.size() + ":" + squareCand.size());
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!sikakuSolve()) {
				return false;
			}
			if (!countSolve()) {
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
		 * 確定した四角がある場合、それとかぶる・接する候補を消す。
		 */
		private boolean sikakuSolve() {
			for (Sikaku fixed : squareFixed) {
				Sikaku removeSikaku1 = new Sikaku(
						new Position(fixed.getLeftUp().getyIndex() - 1, fixed.getLeftUp().getxIndex()),
						new Position(fixed.getRightDown().getyIndex() + 1, fixed.getRightDown().getxIndex()));
				Sikaku removeSikaku2 = new Sikaku(
						new Position(fixed.getLeftUp().getyIndex(), fixed.getLeftUp().getxIndex() - 1),
						new Position(fixed.getRightDown().getyIndex(), fixed.getRightDown().getxIndex() + 1));
				for (Iterator<Sikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
					Sikaku sikaku = iterator.next();
					if (sikaku.isDuplicate(removeSikaku1) || sikaku.isDuplicate(removeSikaku2)) {
						iterator.remove();
					}
				}
			}
			return true;
		}

		/**
		 * 四角を配置して数字の条件を満たせるかを調査する。
		 * 満たせない場合falseを返す。
		 */
		private boolean countSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int num = numbers[yIndex][xIndex];
						int fixCnt = 0;
						for (Sikaku fixed : squareFixed) {
							if (fixed.isDuplicate(new Position(yIndex - 1, xIndex)) ||
									fixed.isDuplicate(new Position(yIndex + 1, xIndex)) ||
									fixed.isDuplicate(new Position(yIndex, xIndex - 1)) ||
									fixed.isDuplicate(new Position(yIndex, xIndex + 1))) {
								fixCnt = fixCnt + fixed.getAreaSize();
							}
						}
						if (num != -1 && num < fixCnt) {
							return false;
						}
						if (num == -1 && 0 < fixCnt) {
							continue;
						}
						Set<Integer> upCand = new HashSet<>();
						upCand.add(0);
						Set<Integer> rightCand = new HashSet<>();
						rightCand.add(0);
						Set<Integer> downCand = new HashSet<>();
						downCand.add(0);
						Set<Integer> leftCand = new HashSet<>();
						leftCand.add(0);
						for (Sikaku cand : squareCand) {
							if (cand.isDuplicate(new Position(yIndex - 1, xIndex))) {
								upCand.add(cand.getAreaSize());
							}
							if (cand.isDuplicate(new Position(yIndex, xIndex + 1))) {
								rightCand.add(cand.getAreaSize());
							}
							if (cand.isDuplicate(new Position(yIndex + 1, xIndex))) {
								downCand.add(cand.getAreaSize());
							}
							if (cand.isDuplicate(new Position(yIndex, xIndex - 1))) {
								leftCand.add(cand.getAreaSize());
							}
						}
						if (num != -1) {
							boolean isOk = false;
							// TODO 4重for文(笑)
							outer: for (Integer up : upCand) {
								for (Integer right : rightCand) {
									for (Integer down : downCand) {
										for (Integer left : leftCand) {
											if (fixCnt + up + right + down + left == num) {
												isOk = true;
												break outer;
											}
										}
									}
								}
							}
							if (!isOk) {
								return false;
							}
						} else {
							if (upCand.size() == 1 && rightCand.size() == 1 && downCand.size() == 1
									&& leftCand.size() == 1) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 候補情報からマスを復元する
		 */
		public Masu[][] getMasu() {
			Masu[][] masu = new Masu[getYLength()][getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.NOT_BLACK;
				}
			}
			for (Sikaku fixed : squareFixed) {
				for (int yIndex = fixed.getLeftUp().getyIndex(); yIndex <= fixed.getRightDown().getyIndex(); yIndex++) {
					for (int xIndex = fixed.getLeftUp().getxIndex(); xIndex <= fixed.getRightDown()
							.getxIndex(); xIndex++) {
						masu[yIndex][xIndex] = Masu.BLACK;
					}
				}
			}
			for (Sikaku cand : squareCand) {
				for (int yIndex = cand.getLeftUp().getyIndex(); yIndex <= cand.getRightDown().getyIndex(); yIndex++) {
					for (int xIndex = cand.getLeftUp().getxIndex(); xIndex <= cand.getRightDown()
							.getxIndex(); xIndex++) {
						masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
			}
			return masu;
		}

		/**
		 * 白マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Masu[][] masu = getMasu();
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
							whitePosSet.add(whitePos);
							setContinuePosSet(masu, whitePos, whitePosSet, null);
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
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Masu[][] masu, Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(masu, nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(masu, nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(masu, nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(masu, nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		public boolean isSolved() {
			return squareCand.size() == 0 && solveAndCheck();
		}

	}

	private final Field field;
	private int count;

	public TasquareSolver(int height, int width, String param) {
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
		System.out.println(new TasquareSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				if (!candSolve(field, recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
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
		for (Iterator<Sikaku> iterator = field.squareCand.iterator(); iterator
				.hasNext();) {
			count++;
			Sikaku oneCand = iterator.next();
			Field virtual = new Field(field);
			virtual.squareCand.remove(oneCand);
			virtual.squareFixed.add(oneCand);
			boolean allowBlack = virtual.solveAndCheck();
			if (allowBlack && recursive > 0) {
				if (!candSolve(virtual, recursive - 1)) {
					allowBlack = false;
				}
			}
			Field virtual2 = new Field(field);
			virtual2.squareCand.remove(oneCand);
			boolean allowNotBlack = virtual2.solveAndCheck();
			if (allowNotBlack && recursive > 0) {
				if (!candSolve(virtual2, recursive - 1)) {
					allowNotBlack = false;
				}
			}
			if (!allowBlack && !allowNotBlack) {
				return false;
			} else if (!allowBlack) {
				field.squareCand = virtual2.squareCand;
				field.squareFixed = virtual2.squareFixed;
			} else if (!allowNotBlack) {
				field.squareCand = virtual.squareCand;
				field.squareFixed = virtual.squareFixed;
			}
		}
		return true;
	}

}
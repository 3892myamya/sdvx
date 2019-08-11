package myamya.other.solver.tasquare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class TasquareSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private final Integer[][] numbers;

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
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
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
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
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
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
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						} else if (numbers[yIndex][xIndex] == -1) {
							sb.append("？");
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
						sb.append(masu[yIndex][xIndex]);
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
					sb.append(masu[yIndex][xIndex]);
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
			if (!squareSolve()) {
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
		 * 白マスの前後左右にある連結黒マスおよび黒マス候補の数をカウントする。
		 * 超過や不足が確定したらfalseを返す。
		 */
		private boolean countSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						Position pivot = new Position(yIndex, xIndex);
						if (!setContinueNotWhitePosSet(pivot,
								numbers[yIndex][xIndex] == -1 ? 1 : numbers[yIndex][xIndex])) {
							// サイズ不足
							return false;
						}
						if (numbers[yIndex][xIndex] != -1) {
							Set<Position> continueBlackPosSet = new HashSet<>();
							if (!setContinueBlackPosSet(pivot, continueBlackPosSet, numbers[yIndex][xIndex], null)) {
								// サイズ超過
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスは正方形になるように配置する。
		 * 正方形にできなかった場合はfalseを返す。
		 */
		public boolean squareSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						whitePosSet.add(new Position(yIndex, xIndex));
					}
				}
			}
			while (!whitePosSet.isEmpty()) {
				Position typicalWhitePos = new ArrayList<>(whitePosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				setContinueBlackPosSet(typicalWhitePos, continuePosSet, 999, null);
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
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							return false;
						}
						masu[yIndex][xIndex] = Masu.BLACK;
					}
				}
				int ySize = maxY - minY + 1;
				int xSize = maxX - minX + 1;
				if (ySize > xSize) {
					int minCandX = 0;
					int maxCandX = getXLength() - 1;
					for (int candY = minY; candY <= maxY; candY++) {
						int targetX = minX;
						while (targetX - 1 >= 0 && masu[candY][targetX - 1] != Masu.NOT_BLACK) {
							targetX--;
						}
						if (minCandX < targetX) {
							minCandX = targetX;
						}
						targetX = maxX;
						while (targetX + 1 < getXLength() && masu[candY][targetX + 1] != Masu.NOT_BLACK) {
							targetX++;
						}
						if (maxCandX > targetX) {
							maxCandX = targetX;
						}
						if (ySize > maxCandX - minCandX + 1) {
							// 正方形にできない
							return false;
						}
					}
				} else if (ySize < xSize) {
					int minCandY = 0;
					int maxCandY = getXLength() - 1;
					for (int candX = minX; candX <= maxX; candX++) {
						int targetY = minY;
						while (targetY - 1 >= 0 && masu[targetY - 1][candX] != Masu.NOT_BLACK) {
							targetY--;
						}
						if (minCandY < targetY) {
							minCandY = targetY;
						}
						targetY = maxY;
						while (targetY + 1 < getYLength() && masu[targetY + 1][candX] != Masu.NOT_BLACK) {
							targetY++;
						}
						if (maxCandY > targetY) {
							maxCandY = targetY;
						}
						if (xSize > maxCandY - minCandY + 1) {
							// 正方形にできない
							return false;
						}
					}
				}
				whitePosSet.removeAll(continuePosSet);
			}
			return true;
		}

		/**
		 * posを起点に上下左右に白確定でないマスをつなげていく。
		 * sizeが不足しないと分かった時点でtrueを返す。
		 */
		private boolean setContinueNotWhitePosSet(Position pos, int size) {
			int maxSize = 0;
			int upCnt = 0;
			int adjust = 1;
			while (pos.getyIndex() - adjust >= 0) {
				if (masu[pos.getyIndex() - adjust][pos.getxIndex()] == Masu.NOT_BLACK) {
					break;
				}
				// 膨らませ調査
				int minX = 0;
				int maxX = getXLength() - 1;
				for (int candY = pos.getyIndex() - adjust; candY <= pos.getyIndex() - 1; candY++) {
					int candX = pos.getxIndex();
					while (candX - 1 >= 0 && masu[candY][candX - 1] != Masu.NOT_BLACK) {
						candX--;
					}
					if (minX < candX) {
						minX = candX;
					}
					candX = pos.getxIndex();
					while (candX + 1 < getXLength() && masu[candY][candX + 1] != Masu.NOT_BLACK) {
						candX++;
					}
					if (maxX > candX) {
						maxX = candX;
					}
				}
				if (maxX - minX + 1 < adjust) {
					break;
				}
				upCnt++;
				adjust++;
			}
			maxSize = maxSize + upCnt * upCnt;
			if (maxSize >= size) {
				return true;
			}

			int rightCnt = 0;
			adjust = 1;
			while (pos.getxIndex() + adjust < getXLength()) {
				if (masu[pos.getyIndex()][pos.getxIndex() + adjust] == Masu.NOT_BLACK) {
					break;
				}
				// 膨らませ調査
				int minY = 0;
				int maxY = getYLength() - 1;
				for (int candX = pos.getxIndex() + adjust; candX <= pos.getxIndex() + 1; candX++) {
					int candY = pos.getyIndex();
					while (candY - 1 >= 0 && masu[candY - 1][candX] != Masu.NOT_BLACK) {
						candY--;
					}
					if (minY < candY) {
						minY = candY;
					}
					candY = pos.getyIndex();
					while (candY + 1 < getYLength() && masu[candY + 1][candX] != Masu.NOT_BLACK) {
						candY++;
					}
					if (maxY > candY) {
						maxY = candY;
					}
				}
				if (maxY - minY + 1 < adjust) {
					break;
				}
				rightCnt++;
				adjust++;
			}
			maxSize = maxSize + rightCnt * rightCnt;
			if (maxSize >= size) {
				return true;
			}

			int downCnt = 0;
			adjust = 1;
			while (pos.getyIndex() + adjust < getYLength()) {
				if (masu[pos.getyIndex() + adjust][pos.getxIndex()] == Masu.NOT_BLACK) {
					break;
				}
				// 膨らませ調査
				int minX = 0;
				int maxX = getXLength() - 1;
				for (int candY = pos.getyIndex() + adjust; candY <= pos.getyIndex() + 1; candY++) {
					int candX = pos.getxIndex();
					while (candX - 1 >= 0 && masu[candY][candX - 1] != Masu.NOT_BLACK) {
						candX--;
					}
					if (minX < candX) {
						minX = candX;
					}
					candX = pos.getxIndex();
					while (candX + 1 < getXLength() && masu[candY][candX + 1] != Masu.NOT_BLACK) {
						candX++;
					}
					if (maxX > candX) {
						maxX = candX;
					}
				}
				if (maxX - minX + 1 < adjust) {
					break;
				}
				downCnt++;
				adjust++;
			}
			maxSize = maxSize + downCnt * downCnt;
			if (maxSize >= size) {
				return true;
			}

			int leftCnt = 0;
			adjust = 1;
			while (pos.getxIndex() - adjust >= 0) {
				if (masu[pos.getyIndex()][pos.getxIndex() - adjust] == Masu.NOT_BLACK) {
					break;
				}
				// 膨らませ調査
				int minY = 0;
				int maxY = getYLength() - 1;
				for (int candX = pos.getxIndex() + adjust; candX <= pos.getxIndex() - 1; candX++) {
					int candY = pos.getyIndex();
					while (candY - 1 >= 0 && masu[candY - 1][candX] != Masu.NOT_BLACK) {
						candY--;
					}
					if (minY < candY) {
						minY = candY;
					}
					candY = pos.getyIndex();
					while (candY + 1 < getYLength() && masu[candY + 1][candX] != Masu.NOT_BLACK) {
						candY++;
					}
					if (maxY > candY) {
						maxY = candY;
					}
				}
				if (maxY - minY + 1 < adjust) {
					break;
				}
				leftCnt++;
				adjust++;
			}
			maxSize = maxSize + leftCnt * leftCnt;
			if (maxSize >= size) {
				return true;
			}
			return false;
		}

		/**
		 * posを起点に上下左右に黒確定マスをつなげていく。
		 * sizeが超過すると分かった時点でfalseを返す。
		 */
		private boolean setContinueBlackPosSet(Position pos, Set<Position> continuePosSet, int size, Direction from) {
			if (continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinueBlackPosSet(nextPos, continuePosSet, size, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinueBlackPosSet(nextPos, continuePosSet, size, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinueBlackPosSet(nextPos, continuePosSet, size, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinueBlackPosSet(nextPos, continuePosSet, size, Direction.RIGHT)) {
						return false;
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
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
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
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	public static class Room {
		@Override
		public String toString() {
			return "Room [capacity=" + capacity + ", pivot=" + pivot + "]";
		}

		// 白マスが何マスあるか。
		private final int capacity;
		// 部屋に属するマスの集合
		private final Position pivot;

		public Room(int capacity, Position pivot) {
			this.capacity = capacity;
			this.pivot = pivot;
		}

		public int getCapacity() {
			return capacity;
		}

		public Position getPivot() {
			return pivot;
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
		// http://pzv.jp/p.html?tasquare/10/10/g2zw5g3i-13p.s1v5j2g1g2h./
		// http://pzv.jp/p.html?tasquare/10/10/g2zw5g3h-12-13p.s1v5j2g1g2h./
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
	 */
	private boolean candSolve(Field field, int recursive) {
		System.out.println(field);
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
		return true;
	}

	/**
	 * 1つのマスに対する仮置き調査
	 */
	private boolean oneCandSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.masu[yIndex][xIndex] = Masu.BLACK;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.masu[yIndex][xIndex] = Masu.NOT_BLACK;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.masu = virtual2.masu;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
		}
		return true;
	}
}
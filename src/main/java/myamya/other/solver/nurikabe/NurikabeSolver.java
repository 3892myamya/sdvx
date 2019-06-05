package myamya.other.solver.nurikabe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class NurikabeSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final int BLACK_CNT = 4;

		// マスの情報
		private Masu[][] masu;
		// 同一グループに属するマスの情報
		public final List<Room> rooms;

		public Masu[][] getMasu() {
			return masu;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			int index = 0;
			rooms = new ArrayList<>();
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int capacity;
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
					rooms.add(new Room(capacity, pos));
					index++;
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			rooms = other.rooms;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					boolean writeNumber = false;
					for (Room room : rooms) {
						if (room.getPivot().equals(new Position(yIndex, xIndex))) {
							if (room.getCapacity() > 99) {
								sb.append("99");
							}
							String capacityStr = String.valueOf(room.getCapacity());
							int index = HALF_NUMS.indexOf(capacityStr);
							if (index >= 0) {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							} else {
								sb.append(capacityStr);
							}
							writeNumber = true;
							break;
						}
					}
					if (!writeNumber) {
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
		 * 部屋のサイズが超過していたり、別部屋と繋がっていた場合、falseを返す。
		 * 部屋が既定サイズに到達している場合、周囲を黒で埋める。
		 */
		public boolean roomSolve() {
			for (int i = 0; i < rooms.size(); i++) {
				Room room = rooms.get(i);
				Position pivot = room.getPivot();
				Set<Position> continueNotBlackPosSet = new HashSet<>();
				continueNotBlackPosSet.add(pivot);
				setContinueNotBlackPosSet(pivot, continueNotBlackPosSet);
				if (room.getCapacity() > continueNotBlackPosSet.size()) {
					// サイズ不足
					return false;
				}
				Set<Position> continueWhitePosSet = new HashSet<>();
				continueWhitePosSet.add(pivot);
				setContinueWhitePosSet(pivot, continueWhitePosSet);
				if (room.getCapacity() < continueWhitePosSet.size()) {
					// サイズ超過
					return false;
				}
				for (int j = i + 1; j < rooms.size(); j++) {
					// 別部屋と連結
					if (continueWhitePosSet.contains(rooms.get(j).getPivot())) {
						return false;
					}
				}
				if (room.getCapacity() == continueWhitePosSet.size()) {
					for (Position pos : continueWhitePosSet) {
						if (pos.getyIndex() != 0) {
							if (masu[pos.getyIndex() - 1][pos.getxIndex()] == Masu.SPACE) {
								masu[pos.getyIndex() - 1][pos.getxIndex()] = Masu.BLACK;
							}
						}
						if (pos.getxIndex() != getXLength() - 1) {
							if (masu[pos.getyIndex()][pos.getxIndex() + 1] == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex() + 1] = Masu.BLACK;
							}
						}
						if (pos.getyIndex() != getYLength() - 1) {
							if (masu[pos.getyIndex() + 1][pos.getxIndex()] == Masu.SPACE) {
								masu[pos.getyIndex() + 1][pos.getxIndex()] = Masu.BLACK;
							}
						}
						if (pos.getxIndex() != 0) {
							if (masu[pos.getyIndex()][pos.getxIndex() - 1] == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex() - 1] = Masu.BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 置くと池ができるマスを白マスにする。
		 * 既に池ができている場合falseを返す。
		 */
		public boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE && masu4 == Masu.BLACK) {
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			Position typicalBlackPos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position blackPos = new Position(yIndex, xIndex);
						blackPosSet.add(blackPos);
						if (typicalBlackPos == null) {
							typicalBlackPos = blackPos;
						}
					}
				}
			}
			if (typicalBlackPos == null) {
				return true;
			} else {
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalBlackPos);
				setContinuePosSet(typicalBlackPos, continuePosSet);
				blackPosSet.removeAll(continuePosSet);
				return blackPosSet.isEmpty();
			}
		}

		/**
		 * posを起点に上下左右に白確定のマスを無制限につなげていく。
		 */
		private void setContinueWhitePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet);
				}
			}
		}

		/**
		 * posを起点に上下左右に白確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
		}

		/**
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
		 */
		private void setContinueNotBlackPosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueNotBlackPosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueNotBlackPosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueNotBlackPosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueNotBlackPosSet(nextPos, continuePosSet);
				}
			}
		}

		/**
		 * 遠すぎてどこからも届かないマスを黒にする。
		 * 該当マスが既に白だった場合Falseを返す。
		 * memo:大きな数字がやたらと多い盤面の場合はやらない方が早い場合がある。
		 */
		public boolean farSolve() {
			Set<Position> allPosSet = new HashSet<>();
			for (int i = 0; i < rooms.size(); i++) {
				Room room = rooms.get(i);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(room.getPivot());
				setContinuePosSetUseDistance(new HashSet<>(continuePosSet), continuePosSet, room.getCapacity() - 1);
				allPosSet.addAll(continuePosSet);
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (!allPosSet.contains(new Position(yIndex, xIndex))) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.BLACK;
						} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * pivotPosSetを起点に上下左右に黒確定でないマスをpivotPosSetからのdistanceだけつなげていく。
		 * memo:distanceが小さい(4ぐらい)の場合はpivotSetを用意しない方が早い場合がある。
		 */
		private void setContinuePosSetUseDistance(Set<Position> pivotPosSet, Set<Position> continuePosSet,
				int distance) {
			if (distance == 0 || pivotPosSet.isEmpty()) {
				return;
			}
			Set<Position> nextPivotPosSet = new HashSet<>();
			for (Position pos : pivotPosSet) {
				if (pos.getyIndex() != 0) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					if (!continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
						nextPivotPosSet.add(nextPos);
						continuePosSet.add(nextPos);
					}
				}
				if (pos.getxIndex() != getXLength() - 1) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					if (!continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
						nextPivotPosSet.add(nextPos);
						continuePosSet.add(nextPos);
					}
				}
				if (pos.getyIndex() != getYLength() - 1) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					if (!continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
						nextPivotPosSet.add(nextPos);
						continuePosSet.add(nextPos);
					}
				}
				if (pos.getxIndex() != 0) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					if (!continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
						nextPivotPosSet.add(nextPos);
						continuePosSet.add(nextPos);
					}
				}
			}
			setContinuePosSetUseDistance(nextPivotPosSet, continuePosSet, distance - 1);
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return true;
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

	public NurikabeSolver(int height, int width, String param) {
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
		System.out.println(new NurikabeSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		int difficulty = 0;
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!solveAndCheck(field)
					|| (!befStr.equals(field.getStateDump()) && !solveAndCheck(field))) {
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				difficulty = difficulty <= recursiveCnt ? recursiveCnt + 1 : difficulty;
				if (!candSolve(field, recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) +
				"ms.");
		System.out.println("難易度:" + difficulty);
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByVal(difficulty).toString();
	}

	/**
	 * 各種チェックを1セット実行
	 * @param recursive
	 */
	private static boolean solveAndCheck(Field field) {
		if (!field.roomSolve()) {
			return false;
		}
		if (!field.pondSolve()) {
			return false;
		}
		if (!field.connectSolve()) {
			return false;
		}
		if (!field.farSolve()) {
			return false;
		}
		return true;
	}

	/**
	 * 仮置きして調べる
	 */
	private static boolean candSolve(Field field, int recursive) {
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 1つのマスに対する仮置き調査
	 */
	private static boolean oneCandSolve(Field field, int yIndex, int xIndex, int recursive) {
		if (field.masu[yIndex][xIndex] == Masu.SPACE) {
			Field virtual = new Field(field);
			virtual.masu[yIndex][xIndex] = Masu.BLACK;
			String befStr = virtual.getStateDump();
			boolean allowBlack = solveAndCheck(virtual)
					&& (befStr.equals(virtual.getStateDump()) || solveAndCheck(virtual));
			if (allowBlack && recursive > 0) {
				if (!candSolve(virtual, recursive - 1)) {
					allowBlack = false;
				}
			}
			Field virtual2 = new Field(field);
			virtual2.masu[yIndex][xIndex] = Masu.NOT_BLACK;
			befStr = virtual2.getStateDump();
			boolean allowNotBlack = solveAndCheck(virtual2)
					&& (befStr.equals(virtual2.getStateDump()) || solveAndCheck(virtual2));
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
		}
		return true;
	}
}

package myamya.other.solver.mejilink;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class MejilinkSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,-1と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] yokoExtraWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、-1,0と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] tateExtraWall;

		private boolean[][] yokoHeyaWall;

		private boolean[][] tateHeyaWall;
		// 同一グループに属するマスの情報
		private final List<Set<Position>> rooms;

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			return sb.toString();
		}

		public String getHintCount() {
			StringBuilder sb = new StringBuilder();
			return sb.toString();
		}

		public int getYLength() {
			return yokoHeyaWall.length;
		}

		public int getXLength() {
			return tateHeyaWall[0].length;
		}

		public Wall[][] getYokoExtraWall() {
			return yokoExtraWall;
		}

		public Wall[][] getTateExtraWall() {
			return tateExtraWall;
		}

		public boolean[][] getYokoHeyaWall() {
			return yokoHeyaWall;
		}

		public boolean[][] getTateHeyaWall() {
			return tateHeyaWall;
		}

		public Field(int height, int width, String param) {
			yokoHeyaWall = new boolean[height][width + 1];
			tateHeyaWall = new boolean[height + 1][width];
			yokoExtraWall = new Wall[height][width + 1];
			tateExtraWall = new Wall[height + 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = Wall.SPACE;
					yokoHeyaWall[yIndex][xIndex] = true;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = Wall.SPACE;
					tateHeyaWall[yIndex][xIndex] = true;
				}
			}
			int indexBase = 0;
			// TODO
			// 処理が終わった後、最後の外壁の処理もするように。
			for (int readPos = 0; readPos < param.length(); readPos++) {
				int bit = Character.getNumericValue(param.charAt(readPos));
				boolean isTate;
				int yIndex;
				int xIndex;
				for (int i = 0; i < 5; i++) {
					if (indexBase >= (getYLength() * (getXLength() - 1)) + ((getYLength() - 1) * getXLength())) {
						int newIndexBase = indexBase - ((getYLength() * (getXLength() - 1))
								+ ((getYLength() - 1) * getXLength()));
						if (newIndexBase >= (2 * getXLength()) + (2 * getYLength())) {
							break;
						}
						isTate = newIndexBase < 2 * getXLength();
						if (isTate) {
							if (bit >= Math.pow(2, 4 - i)) {
								bit = (int) (bit - Math.pow(2, 4 - i));
								yIndex = newIndexBase < getXLength() ? 0 : getYLength();
								xIndex = newIndexBase % getXLength();
								tateHeyaWall[yIndex][xIndex] = false;
								tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
						} else {
							newIndexBase = newIndexBase - (2 * getXLength());
							if (bit >= Math.pow(2, 4 - i)) {
								bit = (int) (bit - Math.pow(2, 4 - i));
								yIndex = newIndexBase % getYLength();
								xIndex = newIndexBase < getYLength() ? 0 : getXLength();
								yokoHeyaWall[yIndex][xIndex] = false;
								yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
						}
					} else {
						isTate = indexBase >= getYLength() * (getXLength() - 1);
						yIndex = isTate ? (indexBase - (getYLength() * (getXLength() - 1))) / getXLength()
								: indexBase / (getXLength() - 1);
						xIndex = isTate ? (indexBase - (getYLength() * (getXLength() - 1))) % getXLength()
								: indexBase % (getXLength() - 1);
						if (bit >= Math.pow(2, 4 - i)) {
							bit = (int) (bit - Math.pow(2, 4 - i));
							if (isTate) {
								tateHeyaWall[yIndex + 1][xIndex] = false;
								tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
							} else {
								yokoHeyaWall[yIndex][xIndex + 1] = false;
								yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
						}
					}
					indexBase++;
				}
			}
			// 縦と横の壁の関係からにょろっと部屋を決めていく
			rooms = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean alreadyRoomed = false;
					for (Set<Position> room : rooms) {
						if (room.contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						if (setContinuePosSet(pos, continuePosSet)) {
							rooms.add(continuePosSet);
						}
					}
				}
			}
		}

		// posを起点に上下左右に部屋壁でないマスを無制限につなげていく。
		// 外に飛び出してしまったらfalseを返す。
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			Position nextPos;
			nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
			if (!continuePosSet.contains(nextPos) && !tateHeyaWall[pos.getyIndex()][pos.getxIndex()]) {
				if (pos.getyIndex() == 0) {
					return false;
				}
				continuePosSet.add(nextPos);
				if (!setContinuePosSet(nextPos, continuePosSet)) {
					return false;
				}
			}
			nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
			if (!continuePosSet.contains(nextPos) && !yokoHeyaWall[pos.getyIndex()][pos.getxIndex() + 1]) {
				if (pos.getxIndex() == getXLength() - 1) {
					return false;
				}
				continuePosSet.add(nextPos);
				if (!setContinuePosSet(nextPos, continuePosSet)) {
					return false;
				}
			}
			nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
			if (!continuePosSet.contains(nextPos) && !tateHeyaWall[pos.getyIndex() + 1][pos.getxIndex()]) {
				if (pos.getyIndex() == getYLength() - 1) {
					return false;
				}
				continuePosSet.add(nextPos);
				if (!setContinuePosSet(nextPos, continuePosSet)) {
					return false;
				}
			}
			nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
			if (!continuePosSet.contains(nextPos) && !yokoHeyaWall[pos.getyIndex()][pos.getxIndex()]) {
				if (pos.getxIndex() == 0) {
					return false;
				}
				continuePosSet.add(nextPos);
				if (!setContinuePosSet(nextPos, continuePosSet)) {
					return false;
				}
			}
			return true;
		}

		public Field(Field other) {
			tateHeyaWall = other.tateHeyaWall;
			yokoHeyaWall = other.yokoHeyaWall;
			rooms = other.rooms;
			yokoExtraWall = new Wall[other.getYLength()][other.getXLength() + 1];
			tateExtraWall = new Wall[other.getYLength() + 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = other.yokoExtraWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = other.tateExtraWall[yIndex][xIndex];
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
						if (tateHeyaWall[yIndex][xIndex]) {
							sb.append("┼");
						} else {
							sb.append(tateExtraWall[yIndex][xIndex]);
						}
					} else {
						sb.append(tateExtraWall[yIndex][xIndex]);
					}
					sb.append("□");
				}
				sb.append(System.lineSeparator());
				if (yIndex != getYLength()) {
					for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
						if (yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
							if (yokoHeyaWall[yIndex][xIndex]) {
								sb.append("┼");
							} else {
								sb.append(yokoExtraWall[yIndex][xIndex]);
							}
						} else {
							sb.append(yokoExtraWall[yIndex][xIndex]);
						}
						if (xIndex != getXLength()) {
							sb.append("　");
						}
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					sb.append(yokoExtraWall[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateExtraWall[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 各部屋の周りにある線が通らない壁の数が部屋の面積と等しくなる。矛盾したらfalseを返す。
		 */
		public boolean numberSolve() {
			for (Set<Position> room : rooms) {
				// 部屋の周りの壁の数
				Set<Position> yokoPosSet = new HashSet<>();
				Set<Position> tatePosSet = new HashSet<>();
				for (Position pos : room) {
					if (tateHeyaWall[pos.getyIndex()][pos.getxIndex()]) {
						tatePosSet.add(new Position(pos.getyIndex(), pos.getxIndex()));
					}
					if (yokoHeyaWall[pos.getyIndex()][pos.getxIndex() + 1]) {
						yokoPosSet.add(new Position(pos.getyIndex(), pos.getxIndex() + 1));
					}
					if (tateHeyaWall[pos.getyIndex() + 1][pos.getxIndex()]) {
						tatePosSet.add(new Position(pos.getyIndex() + 1, pos.getxIndex()));
					}
					if (yokoHeyaWall[pos.getyIndex()][pos.getxIndex()]) {
						yokoPosSet.add(new Position(pos.getyIndex(), pos.getxIndex()));
					}
				}
				int existsCount = 0;
				int notExistsCount = 0;
				for (Position pos : tatePosSet) {
					Wall wall = tateExtraWall[pos.getyIndex()][pos.getxIndex()];
					if (wall == Wall.EXISTS) {
						existsCount++;
					} else if (wall == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				for (Position pos : yokoPosSet) {
					Wall wall = yokoExtraWall[pos.getyIndex()][pos.getxIndex()];
					if (wall == Wall.EXISTS) {
						existsCount++;
					} else if (wall == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				// 部屋の周りに壁がありすぎる・なさすぎる場合はfalse
				if (notExistsCount > room.size() ||
						existsCount > (tatePosSet.size() + yokoPosSet.size() - room.size())) {
					return false;
				} else {
					if (notExistsCount == room.size()) {
						for (Position pos : tatePosSet) {
							Wall wall = tateExtraWall[pos.getyIndex()][pos.getxIndex()];
							if (wall == Wall.SPACE) {
								tateExtraWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
							}
						}
						for (Position pos : yokoPosSet) {
							Wall wall = yokoExtraWall[pos.getyIndex()][pos.getxIndex()];
							if (wall == Wall.SPACE) {
								yokoExtraWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
							}
						}
					}
					if (existsCount == (tatePosSet.size() + yokoPosSet.size() - room.size())) {
						for (Position pos : tatePosSet) {
							Wall wall = tateExtraWall[pos.getyIndex()][pos.getxIndex()];
							if (wall == Wall.SPACE) {
								tateExtraWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
							}
						}
						for (Position pos : yokoPosSet) {
							Wall wall = yokoExtraWall[pos.getyIndex()][pos.getxIndex()];
							if (wall == Wall.SPACE) {
								yokoExtraWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 柱につながる壁は必ず0か2になる。ならない場合falseを返す。
		 */
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					int existsCount = 0;
					int notExistsCount = 0;
					Wall wallUp = yIndex == 0 ? Wall.NOT_EXISTS : yokoExtraWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallDown = yIndex == getYLength() ? Wall.NOT_EXISTS
							: yokoExtraWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallRight = xIndex == getXLength() ? Wall.NOT_EXISTS
							: tateExtraWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.NOT_EXISTS
							: tateExtraWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					if (existsCount > 2 || (existsCount == 1 && notExistsCount == 3)) {
						return false;
					}
					if (existsCount == 2) {
						if (wallUp == Wall.SPACE) {
							yokoExtraWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
						}
					} else if (notExistsCount == 3) {
						if (wallUp == Wall.SPACE) {
							yokoExtraWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
						}
					} else if (existsCount == 1 && notExistsCount == 2) {
						if (wallUp == Wall.SPACE) {
							yokoExtraWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					}
				}
			}
			return true;
		}

		/**
		 * ルール上、各列をふさぐ壁は必ず偶数になる。
		 * 偶数になっていない場合falseを返す。
		 */
		private boolean oddSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				int existsCount = 0;
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
						existsCount = 0;
						break;
					} else if (yokoExtraWall[yIndex][xIndex] == Wall.EXISTS) {
						existsCount++;
					}
				}
				if (existsCount % 2 != 0) {
					return false;
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				int existsCount = 0;
				for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
						existsCount = 0;
						break;
					} else if (tateExtraWall[yIndex][xIndex] == Wall.EXISTS) {
						existsCount++;
					}
				}
				if (existsCount % 2 != 0) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 壁が1つながりになっていない場合falseを返す。
		 */
		public boolean connectWhiteSolve() {
			Set<Position> yokoBlackWallPosSet = new HashSet<>();
			Set<Position> tateBlackWallPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] == Wall.EXISTS) {
						Position existPos = new Position(yIndex, xIndex);
						if (yokoBlackWallPosSet.isEmpty()) {
							yokoBlackWallPosSet.add(existPos);
							setContinueExistWallPosSet(existPos, yokoBlackWallPosSet, tateBlackWallPosSet,
									true, null);
						} else {
							if (!yokoBlackWallPosSet.contains(existPos)) {
								return false;
							}
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.EXISTS) {
						Position existPos = new Position(yIndex, xIndex);
						if (tateBlackWallPosSet.isEmpty()) {
							tateBlackWallPosSet.add(existPos);
							setContinueExistWallPosSet(existPos, yokoBlackWallPosSet, tateBlackWallPosSet,
									false, null);
						} else {
							if (!tateBlackWallPosSet.contains(existPos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		private void setContinueExistWallPosSet(Position pos, Set<Position> continueYokoWallPosSet,
				Set<Position> continueTateWallPosSet, boolean isYoko, Direction from) {
			if (isYoko) {
				if (pos.getxIndex() != 0 && from != Direction.UP) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.RIGHT);
					}
				}
				if (pos.getyIndex() != 0 && from != Direction.UP) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.DOWN);
					}
				}
				if (pos.getxIndex() != getXLength() && from != Direction.UP) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex());
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.LEFT);
					}
				}
				if (pos.getxIndex() != getXLength() && from != Direction.DOWN) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.LEFT);
					}
				}
				if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.UP);
					}
				}
				if (pos.getxIndex() != 0 && from != Direction.DOWN) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.RIGHT);
					}
				}
			} else {
				if (pos.getyIndex() != 0 && from != Direction.LEFT) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.DOWN);
					}
				}
				if (pos.getyIndex() != 0 && from != Direction.RIGHT) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.DOWN);
					}
				}
				if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.LEFT);
					}
				}
				if (pos.getyIndex() != getYLength() && from != Direction.RIGHT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.UP);
					}
				}
				if (pos.getyIndex() != getYLength() && from != Direction.LEFT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex());
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.UP);
					}
				}
				if (pos.getxIndex() != 0 && from != Direction.LEFT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.RIGHT);
					}
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
			if (!nextSolve()) {
				return false;
			}
			if (!oddSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectWhiteSolve()) {
					return false;
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public MejilinkSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public MejilinkSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?mejilink/10/10/gtqkl9ail5aqlldaql110fvvhsvj0fuf1svv"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new MejilinkSolver(height, width, param).solve());
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
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + count * 25);
		System.out.println(field);
		int level = (int) Math.sqrt(count * 25 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 25).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
				if (field.yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandTateWallSolve(field, yIndex, xIndex, recursive)) {
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

	private boolean oneCandYokoWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.tateExtraWall = virtual2.tateExtraWall;
			field.yokoExtraWall = virtual2.yokoExtraWall;
		} else if (!allowNotBlack) {
			field.tateExtraWall = virtual.tateExtraWall;
			field.yokoExtraWall = virtual.yokoExtraWall;
		}
		return true;
	}

	private boolean oneCandTateWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.tateExtraWall = virtual2.tateExtraWall;
			field.yokoExtraWall = virtual2.yokoExtraWall;
		} else if (!allowNotBlack) {
			field.tateExtraWall = virtual.tateExtraWall;
			field.yokoExtraWall = virtual.yokoExtraWall;
		}
		return true;
	}
}
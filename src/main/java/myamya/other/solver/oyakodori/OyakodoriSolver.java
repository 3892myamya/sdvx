package myamya.other.solver.oyakodori;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class OyakodoriSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyz";

		// 巣の情報
		protected boolean[][] nest;
		// 丸の情報。1が○(親鳥)、2が●(ひな鳥)
		private final Integer[][] circles;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoRoomWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateRoomWall;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;
		// 同一グループに属するマスの情報
		protected final List<Set<Position>> rooms;

		public boolean[][] getNest() {
			return nest;
		}

		public Integer[][] getCircles() {
			return circles;
		}

		public boolean[][] getYokoRoomWall() {
			return yokoRoomWall;
		}

		public boolean[][] getTateRoomWall() {
			return tateRoomWall;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public List<Set<Position>> getRooms() {
			return rooms;
		}

		public int getYLength() {
			return nest.length;
		}

		public int getXLength() {
			return nest[0].length;
		}

		public Field(int height, int width, String param) {
			nest = new boolean[height][width];
			circles = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			// パラメータを解釈して壁の有無を入れる
			yokoRoomWall = new boolean[height][width - 1];
			tateRoomWall = new boolean[height - 1][width];
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * (getXLength() - 1); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * (getXLength() - 1)) - 1) {
					if (mod >= 0) {
						yokoRoomWall[(cnt - mod + 0) / (getXLength() - 1)][(cnt - mod + 0) % (getXLength() - 1)] = bit
								/ 16 % 2 == 1;
					}
					if (mod >= 1) {
						yokoRoomWall[(cnt - mod + 1) / (getXLength() - 1)][(cnt - mod + 1) % (getXLength() - 1)] = bit
								/ 8 % 2 == 1;
					}
					if (mod >= 2) {
						yokoRoomWall[(cnt - mod + 2) / (getXLength() - 1)][(cnt - mod + 2) % (getXLength() - 1)] = bit
								/ 4 % 2 == 1;
					}
					if (mod >= 3) {
						yokoRoomWall[(cnt - mod + 3) / (getXLength() - 1)][(cnt - mod + 3) % (getXLength() - 1)] = bit
								/ 2 % 2 == 1;
					}
					if (mod >= 4) {
						yokoRoomWall[(cnt - mod + 4) / (getXLength() - 1)][(cnt - mod + 4) % (getXLength() - 1)] = bit
								/ 1 % 2 == 1;
					}
				}
			}
			for (int cnt = 0; cnt < (getYLength() - 1) * getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == ((getYLength() - 1) * getXLength()) - 1) {
					if (mod >= 0) {
						tateRoomWall[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16
								% 2 == 1;
					}
					if (mod >= 1) {
						tateRoomWall[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1;
					}
					if (mod >= 2) {
						tateRoomWall[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1;
					}
					if (mod >= 3) {
						tateRoomWall[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1;
					}
					if (mod >= 4) {
						tateRoomWall[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1;
					}
				}
			}
			int index = 0;
			for (; readPos < param.length(); readPos++) {
				char ch = param.charAt(readPos);
				int bitInfo = Character.getNumericValue(ch);
				int pos1 = bitInfo / 9 % 3;
				int pos2 = bitInfo / 3 % 3;
				int pos3 = bitInfo % 3;
				if (index / getXLength() < getYLength()) {
					circles[index / getXLength()][index % getXLength()] = pos1;
				} else {
					// ここに来るということは今のreadposは巣のパラメータのため、++しない
					break;
				}
				index++;
				if (index / getXLength() < getYLength()) {
					circles[index / getXLength()][index % getXLength()] = pos2;
				} else {
					readPos++;
					break;
				}
				index++;
				if (index / getXLength() < getYLength()) {
					circles[index / getXLength()][index % getXLength()] = pos3;
				} else {
					readPos++;
					break;
				}
				index++;
			}
			for (int cnt = 0; cnt < getYLength() * getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * (getXLength())) - 1) {
					if (mod >= 0) {
						Position pos = new Position((cnt - mod + 0) / (getXLength()), (cnt - mod + 0) % (getXLength()));
						if (bit / 16 % 2 == 1) {
							nest[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
					if (mod >= 1) {
						Position pos = new Position((cnt - mod + 1) / (getXLength()), (cnt - mod + 1) % (getXLength()));
						if (bit / 8 % 2 == 1) {
							nest[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
					if (mod >= 2) {
						Position pos = new Position((cnt - mod + 2) / (getXLength()), (cnt - mod + 2) % (getXLength()));
						if (bit / 4 % 2 == 1) {
							nest[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
					if (mod >= 3) {
						Position pos = new Position((cnt - mod + 3) / (getXLength()), (cnt - mod + 3) % (getXLength()));
						if (bit / 2 % 2 == 1) {
							nest[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
					if (mod >= 4) {
						Position pos = new Position((cnt - mod + 4) / (getXLength()), (cnt - mod + 4) % (getXLength()));
						if (bit / 1 % 2 == 1) {
							nest[pos.getyIndex()][pos.getxIndex()] = true;
						}
					}
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
						setContinuePosSet(pos, continuePosSet);
						Set<Position> nestPosSet = new HashSet<>();
						for (Position continuePos : continuePosSet) {
							if (nest[continuePos.getyIndex()][continuePos.getxIndex()]) {
								nestPosSet.add(continuePos);
							}
						}
						rooms.add(continuePosSet);
					}
				}
			}
		}

		public Field(Field other) {
			nest = new boolean[other.getYLength()][other.getXLength()];
			circles = new Integer[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					nest[yIndex][xIndex] = other.nest[yIndex][xIndex];
					circles[yIndex][xIndex] = other.circles[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = other.yokoWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
				}
			}
			yokoRoomWall = other.yokoRoomWall;
			tateRoomWall = other.tateRoomWall;
			rooms = other.rooms;
		}

		// posを起点に上下左右に部屋壁でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoRoomWall[pos.getyIndex()][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateRoomWall[pos.getyIndex()][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (circles[yIndex][xIndex] != 0) {
						sb.append(circles[yIndex][xIndex] == 1 ? "○" : "●");
					} else if (nest[yIndex][xIndex]) {
						sb.append("◆");
					} else {
						sb.append("　");
					}
					if (xIndex != getXLength() - 1) {
						if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
							sb.append(yokoRoomWall[yIndex][xIndex] == true ? "？" : "　");
						} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							sb.append(yokoRoomWall[yIndex][xIndex] == true ? "＋" : "・");
						} else if (yokoWall[yIndex][xIndex] == Wall.EXISTS) {
							sb.append(yokoRoomWall[yIndex][xIndex] == true ? "□" : "×");
						}
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (tateWall[yIndex][xIndex] == Wall.SPACE) {
							sb.append(tateRoomWall[yIndex][xIndex] == true ? "？" : "　");
						} else if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							sb.append(tateRoomWall[yIndex][xIndex] == true ? "＋" : "・");
						} else if (tateWall[yIndex][xIndex] == Wall.EXISTS) {
							sb.append(tateRoomWall[yIndex][xIndex] == true ? "□" : "×");
						}
						if (xIndex != getXLength() - 1) {
							sb.append("□");
						}
					}
					sb.append("□");
					sb.append(System.lineSeparator());
				}
			}
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					sb.append(yokoWall[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateWall[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!oyadoriSolve()) {
					return false;
				}
				if (!hinadoriSolve()) {
					return false;
				}
				if (!nestSolve()) {
					return false;
				}
			}
			return true;
		}

		// 親鳥(●)とひな鳥(○)のマスは必ず壁が3個に、それ以外のマスは壁が2個or4個になる。
		// また、巣は必ず壁が3になる。ただし、例外的にひな鳥が巣の上にいる場合は、壁が4で確定する。
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int existsCount = 0;
					int notExistsCount = 0;
					Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					if (circles[yIndex][xIndex] != 0) {
						if (nest[yIndex][xIndex]) {
							if (notExistsCount != 0) {
								return false;
							}
							if (wallUp == Wall.SPACE) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
						} else {
							if (notExistsCount > 1 || existsCount > 3) {
								return false;
							}
							if (existsCount == 3) {
								if (wallUp == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
								}
							} else if (notExistsCount == 1) {
								if (wallUp == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
								}
							}
						}
					} else {
						if (nest[yIndex][xIndex]) {
							if (notExistsCount > 1 || existsCount > 3) {
								return false;
							}
							if (existsCount == 3) {
								if (wallUp == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
								}
							} else if (notExistsCount == 1) {
								if (wallUp == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
								}
							}
						} else {
							if ((existsCount == 3 && notExistsCount == 1) || notExistsCount > 2) {
								return false;
							}
							if (existsCount == 3) {
								if (wallUp == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
								}
							} else if (notExistsCount != 0) {
								if (existsCount > 2 || notExistsCount > 2) {
									return false;
								}
								if (notExistsCount == 2) {
									if (wallUp == Wall.SPACE) {
										tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
									}
									if (wallRight == Wall.SPACE) {
										yokoWall[yIndex][xIndex] = Wall.EXISTS;
									}
									if (wallDown == Wall.SPACE) {
										tateWall[yIndex][xIndex] = Wall.EXISTS;
									}
									if (wallLeft == Wall.SPACE) {
										yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
									}
								} else if (existsCount == 2) {
									if (wallUp == Wall.SPACE) {
										tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
									}
									if (wallRight == Wall.SPACE) {
										yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
									}
									if (wallDown == Wall.SPACE) {
										tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
									}
									if (wallLeft == Wall.SPACE) {
										yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
									}
								}
							}
						}
					}
				}
			}
			return true;
		}

		// 親鳥(○)から繋がるマスが国境を超えずに閉じていてはならない
		private boolean oyadoriSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (circles[yIndex][xIndex] == 1) {
						Set<Position> continuePosSet = new HashSet<>();
						Position targetPos = new Position(yIndex, xIndex);
						continuePosSet.add(new Position(yIndex, xIndex));
						if (!oyadoriCheck(targetPos, continuePosSet, null)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁あり確定でなく他の丸にぶつからないマスを繋ぐ。国境を超えた時点でtrueを返す。
		 */
		private boolean oyadoriCheck(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& circles[nextPos.getyIndex()][nextPos.getxIndex()] == 0
						&& !continuePosSet.contains(nextPos)) {
					if (tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()]) {
						return true;
					}
					continuePosSet.add(nextPos);
					if (oyadoriCheck(nextPos, continuePosSet, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& circles[nextPos.getyIndex()][nextPos.getxIndex()] == 0
						&& !continuePosSet.contains(nextPos)) {
					if (yokoRoomWall[pos.getyIndex()][pos.getxIndex()]) {
						return true;
					}
					continuePosSet.add(nextPos);
					if (oyadoriCheck(nextPos, continuePosSet, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& circles[nextPos.getyIndex()][nextPos.getxIndex()] == 0
						&& !continuePosSet.contains(nextPos)) {
					if (tateRoomWall[pos.getyIndex()][pos.getxIndex()]) {
						return true;
					}
					continuePosSet.add(nextPos);
					if (oyadoriCheck(nextPos, continuePosSet, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& circles[nextPos.getyIndex()][nextPos.getxIndex()] == 0
						&& !continuePosSet.contains(nextPos)) {
					if (yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1]) {
						return true;
					}
					continuePosSet.add(nextPos);
					if (oyadoriCheck(nextPos, continuePosSet, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		// ひな鳥(●)から繋がるマスが国境を超えてはならない
		private boolean hinadoriSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (circles[yIndex][xIndex] == 2) {
						Set<Position> continuePosSet = new HashSet<>();
						Position targetPos = new Position(yIndex, xIndex);
						continuePosSet.add(new Position(yIndex, xIndex));
						if (!hinadoriCheck(targetPos, continuePosSet, null)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁なし確定マスを繋ぎ、 国境を超えたり他の丸にぶつかった時点でfalseを返す。
		 */
		private boolean hinadoriCheck(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()]
							|| circles[nextPos.getyIndex()][nextPos.getxIndex()] != 0) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!hinadoriCheck(nextPos, continuePosSet, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (yokoRoomWall[pos.getyIndex()][pos.getxIndex()]
							|| circles[nextPos.getyIndex()][nextPos.getxIndex()] != 0) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!hinadoriCheck(nextPos, continuePosSet, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (tateRoomWall[pos.getyIndex()][pos.getxIndex()]
							|| circles[nextPos.getyIndex()][nextPos.getxIndex()] != 0) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!hinadoriCheck(nextPos, continuePosSet, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1]
							|| circles[nextPos.getyIndex()][nextPos.getxIndex()] != 0) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!hinadoriCheck(nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		// 並んだ巣に対し、親鳥とひな鳥が1マスずつ繋がるようにする。
		@SuppressWarnings("unused")
		private boolean nestSolve() {
			Set<Position> alreadyNestPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (nest[yIndex][xIndex]) {
						// 巣のセットを作る
						Position targetPos = new Position(yIndex, xIndex);
						if (alreadyNestPosSet.contains(targetPos)) {
							continue;
						}
						Position anotherTargetPos = null;
						if (yIndex != 0 && nest[yIndex - 1][xIndex]) {
							if (anotherTargetPos != null) {
								return false;
							}
							anotherTargetPos = new Position(yIndex - 1, xIndex);
						}
						if (xIndex != getXLength() - 1 && nest[yIndex][xIndex + 1]) {
							if (anotherTargetPos != null) {
								return false;
							}
							anotherTargetPos = new Position(yIndex, xIndex + 1);
						}
						if (yIndex != getYLength() - 1 && nest[yIndex + 1][xIndex]) {
							if (anotherTargetPos != null) {
								return false;
							}
							anotherTargetPos = new Position(yIndex + 1, xIndex);
						}
						if (xIndex != 0 && nest[yIndex][xIndex - 1]) {
							if (anotherTargetPos != null) {
								return false;
							}
							anotherTargetPos = new Position(yIndex, xIndex - 1);
						}
						// 巣が2個並んでなかったらfalse※本来は最初に1度だけやればいいはずだがとりあえずここに
						if (anotherTargetPos == null) {
							return false;
						}
						alreadyNestPosSet.add(targetPos);
						alreadyNestPosSet.add(anotherTargetPos);

						Set<Position> targetPosSet = new HashSet<>();
						targetPosSet.add(targetPos);
						if (!(nestCheck(targetPos, targetPosSet, null))) {
							return false;
						}
						Set<Position> anotherTargetPosSet = new HashSet<>();
						anotherTargetPosSet.add(anotherTargetPos);
						if (!(nestCheck(anotherTargetPos, anotherTargetPosSet, null))) {
							return false;
						}

						Set<Position> targetPosPairSet = new HashSet<>();
						targetPosPairSet.add(targetPos);
						int connectCircle = nestPairCheck(targetPos, targetPosPairSet, null);

						Set<Position> anotherTargetPosPairSet = new HashSet<>();
						anotherTargetPosPairSet.add(anotherTargetPos);
						int anotherConnectCircle = nestPairCheck(anotherTargetPos, anotherTargetPosPairSet, null);

						// 一つの巣に同じ鳥が入っているのはNG
						if (connectCircle != 0 && anotherConnectCircle != 0 && connectCircle == anotherConnectCircle) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁あり確定でないマスを繋ぐ。鳥が繋がった時点でtrueを返す。
		 */
		private boolean nestCheck(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (circles[nextPos.getyIndex()][nextPos.getxIndex()] != 0) {
						return true;
					}
					continuePosSet.add(nextPos);
					if (nestCheck(nextPos, continuePosSet, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					if (circles[nextPos.getyIndex()][nextPos.getxIndex()] != 0) {
						return true;
					}
					continuePosSet.add(nextPos);
					if (nestCheck(nextPos, continuePosSet, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					if (circles[nextPos.getyIndex()][nextPos.getxIndex()] != 0) {
						return true;
					}
					continuePosSet.add(nextPos);
					if (nestCheck(nextPos, continuePosSet, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (circles[nextPos.getyIndex()][nextPos.getxIndex()] != 0) {
						return true;
					}
					continuePosSet.add(nextPos);
					if (nestCheck(nextPos, continuePosSet, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右に壁なし確定マスを繋ぎ、 繋がった鳥の種類を返す。不明な場合は0を返す。
		 */
		private int nestPairCheck(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (circles[nextPos.getyIndex()][nextPos.getxIndex()] != 0) {
						return circles[nextPos.getyIndex()][nextPos.getxIndex()];
					}
					continuePosSet.add(nextPos);
					int result = nestPairCheck(nextPos, continuePosSet, Direction.DOWN);
					if (result != 0) {
						return result;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (circles[nextPos.getyIndex()][nextPos.getxIndex()] != 0) {
						return circles[nextPos.getyIndex()][nextPos.getxIndex()];
					}
					continuePosSet.add(nextPos);
					int result = nestPairCheck(nextPos, continuePosSet, Direction.LEFT);
					if (result != 0) {
						return result;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (circles[nextPos.getyIndex()][nextPos.getxIndex()] != 0) {
						return circles[nextPos.getyIndex()][nextPos.getxIndex()];
					}
					continuePosSet.add(nextPos);
					int result = nestPairCheck(nextPos, continuePosSet, Direction.UP);
					if (result != 0) {
						return result;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (circles[nextPos.getyIndex()][nextPos.getxIndex()] != 0) {
						return circles[nextPos.getyIndex()][nextPos.getxIndex()];
					}
					continuePosSet.add(nextPos);
					int result = nestPairCheck(nextPos, continuePosSet, Direction.RIGHT);
					if (result != 0) {
						return result;
					}
				}
			}
			return 0;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public OyakodoriSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public OyakodoriSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?oyakodori/4/4/9400f0i900i912h0"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new OyakodoriSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 3));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 3 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count / 3).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
				if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateWall[yIndex][xIndex] == Wall.SPACE) {
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
		virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

	private boolean oneCandTateWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

}
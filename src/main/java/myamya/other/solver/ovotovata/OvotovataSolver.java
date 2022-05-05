package myamya.other.solver.ovotovata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class OvotovataSolver implements Solver {

	public static class Field {
		// マスの情報
		private Masu[][] masu;
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
		private final List<Room> rooms;

		public Masu[][] getMasu() {
			return masu;
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

		public List<Room> getRooms() {
			return rooms;
		}

		public int getYLength() {
			return yokoWall.length;
		}

		public int getXLength() {
			return tateWall[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
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
			// 縦と横の壁の関係からにょろっと部屋を決めていく
			List<Integer> blackCntList = new ArrayList<>();
			List<Boolean> isGrayList = new ArrayList<>();
			for (; readPos < param.length(); readPos++) {
				char ch = param.charAt(readPos);
				// 16 - 255は '-'
				// 256 - 999は '+'
				int wkNum;
				if (ch == '-') {
					wkNum = Integer.parseInt("" + param.charAt(readPos + 1) + param.charAt(readPos + 2), 16);
					readPos++;
					readPos++;
				} else if (ch == '+') {
					wkNum = Integer.parseInt(
							"" + param.charAt(readPos + 1) + param.charAt(readPos + 2) + param.charAt(readPos + 3), 16);
					readPos++;
					readPos++;
					readPos++;
				} else {
					wkNum = Integer.parseInt(String.valueOf(ch), 16);
				}
				blackCntList.add(wkNum / 4 - 1);
				isGrayList.add(wkNum % 2 == 1);
			}
			rooms = new ArrayList<>();
			int blackCntListIndex = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean alreadyRoomed = false;
					for (Room room : rooms) {
						if (room.getMember().contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinueRoomPosSet(pos, continuePosSet, null);
						Set<Position> upWallPosSet = new HashSet<>();
						Set<Position> rightWallPosSet = new HashSet<>();
						Set<Position> downWallPosSet = new HashSet<>();
						Set<Position> leftWallPosSet = new HashSet<>();
						for (Position roomPos : continuePosSet) {
							if (roomPos.getyIndex() != 0
									&& tateRoomWall[roomPos.getyIndex() - 1][roomPos.getxIndex()]) {
								upWallPosSet.add(new Position(roomPos.getyIndex() - 1, roomPos.getxIndex()));
							}
							if (roomPos.getxIndex() != getXLength() - 1
									&& yokoRoomWall[roomPos.getyIndex()][roomPos.getxIndex()]) {
								rightWallPosSet.add(new Position(roomPos.getyIndex(), roomPos.getxIndex()));
							}
							if (roomPos.getyIndex() != getYLength() - 1
									&& tateRoomWall[roomPos.getyIndex()][roomPos.getxIndex()]) {
								downWallPosSet.add(new Position(roomPos.getyIndex(), roomPos.getxIndex()));
							}
							if (roomPos.getxIndex() != 0
									&& yokoRoomWall[roomPos.getyIndex()][roomPos.getxIndex() - 1]) {
								leftWallPosSet.add(new Position(roomPos.getyIndex(), roomPos.getxIndex() - 1));
							}
						}
						rooms.add(new Room(blackCntList.get(blackCntListIndex), isGrayList.get(blackCntListIndex),
								continuePosSet, upWallPosSet, rightWallPosSet, downWallPosSet, leftWallPosSet));
						blackCntListIndex++;
					}
				}
			}
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスをつなげていく。 国境をまたぐこともできないものとする。
		 */
		private void setContinueRoomPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()] && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueRoomPosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !yokoRoomWall[pos.getyIndex()][pos.getxIndex()] && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueRoomPosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !tateRoomWall[pos.getyIndex()][pos.getxIndex()] && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueRoomPosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1] && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueRoomPosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
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
					sb.append(masu[yIndex][xIndex]);
					if (xIndex != getXLength() - 1) {
						if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
							sb.append(yokoRoomWall[yIndex][xIndex] == true ? "？" : "　");
						} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							sb.append(yokoRoomWall[yIndex][xIndex] == true ? "＋" : "・");
						} else if (yokoWall[yIndex][xIndex] == Wall.EXISTS) {
							sb.append(yokoRoomWall[yIndex][xIndex] == true ? "□" : "○");
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
							sb.append(tateRoomWall[yIndex][xIndex] == true ? "□" : "○");
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
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
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
		 * 灰色マスは白マス1つ以上必須。また数字部屋から出ていったら数字だけ伸ばすの必須。
		 */
		public boolean roomSolve() {
			for (Room room : rooms) {
				if (room.isGray()) {
					int cnt = 0;
					for (Position pos : room.member) {
						if (masu[pos.getyIndex()][pos.getxIndex()] != Masu.BLACK) {
							cnt++;
							break;
						}
					}
					if (cnt == 0) {
						return false;
					}
//					int cnt = 0;
//					for (Position pos : room.member) {
//						if (masu[pos.getyIndex()][pos.getxIndex()] != Masu.BLACK) {
//							cnt++;
//						}
//					}
//					if (cnt == 0) {
//						return false;
//					} else if (cnt == 1) {
//						for (Position pos : room.member) {
//							if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
//								masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
//							}
//						}
//					}
				}
				if (room.getCurveCnt() != -1) {
					if (room.getCurveCnt() == 0) {
						// ？の場合は、どこかで長さが確定した出口があれば、それでチェックをする。
						int candCnt = 0;
						for (Position upWallPos : room.getUpWallPosSet()) {
							for (int i = 0; upWallPos.getyIndex() - i >= 0; i++) {
								if (tateWall[upWallPos.getyIndex() - i][upWallPos.getxIndex()] == Wall.NOT_EXISTS) {
									candCnt++;
								}
								if (tateWall[upWallPos.getyIndex() - i][upWallPos.getxIndex()] == Wall.EXISTS) {
									break;
								}
								if (tateWall[upWallPos.getyIndex() - i][upWallPos.getxIndex()] == Wall.SPACE) {
									candCnt = 0;
									break;
								}
							}
							if (candCnt != 0) {
								break;
							}
						}
						if (candCnt != 0) {
							if (!wallCheck(candCnt, room)) {
								return false;
							}
							continue;
						}
						for (Position rightWallPos : room.getRightWallPosSet()) {
							for (int i = 0; rightWallPos.getxIndex() + i < getXLength() - 1; i++) {
								if (yokoWall[rightWallPos.getyIndex()][rightWallPos.getxIndex()
										+ i] == Wall.NOT_EXISTS) {
									candCnt++;
								}
								if (yokoWall[rightWallPos.getyIndex()][rightWallPos.getxIndex() + i] == Wall.EXISTS) {
									break;
								}
								if (yokoWall[rightWallPos.getyIndex()][rightWallPos.getxIndex() + i] == Wall.SPACE) {
									candCnt = 0;
									break;
								}
							}
							if (candCnt != 0) {
								break;
							}
						}
						if (candCnt != 0) {
							if (!wallCheck(candCnt, room)) {
								return false;
							}
							continue;
						}
						for (Position downWallPos : room.getDownWallPosSet()) {
							for (int i = 0; downWallPos.getyIndex() + i < getYLength() - 1; i++) {
								if (tateWall[downWallPos.getyIndex() + i][downWallPos.getxIndex()] == Wall.NOT_EXISTS) {
									candCnt++;
								}
								if (tateWall[downWallPos.getyIndex() + i][downWallPos.getxIndex()] == Wall.EXISTS) {
									break;
								}
								if (tateWall[downWallPos.getyIndex() + i][downWallPos.getxIndex()] == Wall.SPACE) {
									candCnt = 0;
									break;
								}
							}
							if (candCnt != 0) {
								break;
							}
						}
						if (candCnt != 0) {
							if (!wallCheck(candCnt, room)) {
								return false;
							}
							continue;
						}
						for (Position leftWallPos : room.getLeftWallPosSet()) {
							for (int i = 0; leftWallPos.getxIndex() - i >= 0; i++) {
								if (yokoWall[leftWallPos.getyIndex()][leftWallPos.getxIndex() - i] == Wall.NOT_EXISTS) {
									candCnt++;
								}
								if (yokoWall[leftWallPos.getyIndex()][leftWallPos.getxIndex() - i] == Wall.EXISTS) {
									break;
								}
								if (yokoWall[leftWallPos.getyIndex()][leftWallPos.getxIndex() - i] == Wall.SPACE) {
									candCnt = 0;
									break;
								}
							}
							if (candCnt != 0) {
								break;
							}
						}
						if (candCnt != 0) {
							if (!wallCheck(candCnt, room)) {
								return false;
							}
							continue;
						}
					} else {
						if (!wallCheck(room.getCurveCnt(), room)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		private boolean wallCheck(int curveCnt, Room room) {
			for (Position upWallPos : room.getUpWallPosSet()) {
				if (tateWall[upWallPos.getyIndex()][upWallPos.getxIndex()] == Wall.NOT_EXISTS) {
					if (upWallPos.getyIndex() + 1 - curveCnt < 0) {
						// 盤面の外に出てしまう
						return false;
					}
					for (int i = 0; i < curveCnt - 1; i++) {
						if (tateWall[upWallPos.getyIndex() - i - 1][upWallPos.getxIndex()] == Wall.EXISTS) {
							return false;
						}
						tateWall[upWallPos.getyIndex() - i - 1][upWallPos.getxIndex()] = Wall.NOT_EXISTS;
					}
					if (upWallPos.getyIndex() + 1 - curveCnt != 0) {
						if (tateWall[upWallPos.getyIndex() - curveCnt][upWallPos.getxIndex()] == Wall.NOT_EXISTS) {
							return false;
						}
						tateWall[upWallPos.getyIndex() - curveCnt][upWallPos.getxIndex()] = Wall.EXISTS;
					}
				}
			}
			for (Position rightWallPos : room.getRightWallPosSet()) {
				if (yokoWall[rightWallPos.getyIndex()][rightWallPos.getxIndex()] == Wall.NOT_EXISTS) {
					if (rightWallPos.getxIndex() + curveCnt + 1 > getXLength()) {
						// 盤面の外に出てしまう
						return false;
					}
					for (int i = 0; i < curveCnt - 1; i++) {
						if (yokoWall[rightWallPos.getyIndex()][rightWallPos.getxIndex() + i + 1] == Wall.EXISTS) {
							return false;
						}
						yokoWall[rightWallPos.getyIndex()][rightWallPos.getxIndex() + i + 1] = Wall.NOT_EXISTS;
					}
					if (rightWallPos.getxIndex() + curveCnt + 1 != getXLength()) {
						if (yokoWall[rightWallPos.getyIndex()][rightWallPos.getxIndex()
								+ curveCnt] == Wall.NOT_EXISTS) {
							return false;
						}
						yokoWall[rightWallPos.getyIndex()][rightWallPos.getxIndex() + curveCnt] = Wall.EXISTS;
					}
				}
			}
			for (Position downWallPos : room.getDownWallPosSet()) {
				if (tateWall[downWallPos.getyIndex()][downWallPos.getxIndex()] == Wall.NOT_EXISTS) {
					if (downWallPos.getyIndex() + curveCnt + 1 > getYLength()) {
						// 盤面の外に出てしまう
						return false;
					}
					for (int i = 0; i < curveCnt - 1; i++) {
						if (tateWall[downWallPos.getyIndex() + i + 1][downWallPos.getxIndex()] == Wall.EXISTS) {
							return false;
						}
						tateWall[downWallPos.getyIndex() + i + 1][downWallPos.getxIndex()] = Wall.NOT_EXISTS;
					}
					if (downWallPos.getyIndex() + curveCnt + 1 != getYLength()) {
						if (tateWall[downWallPos.getyIndex() + curveCnt][downWallPos.getxIndex()] == Wall.NOT_EXISTS) {
							return false;
						}
						tateWall[downWallPos.getyIndex() + curveCnt][downWallPos.getxIndex()] = Wall.EXISTS;
					}
				}
			}
			for (Position leftWallPos : room.getLeftWallPosSet()) {
				if (yokoWall[leftWallPos.getyIndex()][leftWallPos.getxIndex()] == Wall.NOT_EXISTS) {
					if (leftWallPos.getxIndex() + 1 - curveCnt < 0) {
						// 盤面の外に出てしまう
						return false;
					}
					for (int i = 0; i < curveCnt - 1; i++) {
						if (yokoWall[leftWallPos.getyIndex()][leftWallPos.getxIndex() - i - 1] == Wall.EXISTS) {
							return false;
						}
						yokoWall[leftWallPos.getyIndex()][leftWallPos.getxIndex() - i - 1] = Wall.NOT_EXISTS;
					}
					if (leftWallPos.getxIndex() + 1 - curveCnt != 0) {
						if (yokoWall[leftWallPos.getyIndex()][leftWallPos.getxIndex() - curveCnt] == Wall.NOT_EXISTS) {
							return false;
						}
						yokoWall[leftWallPos.getyIndex()][leftWallPos.getxIndex() - curveCnt] = Wall.EXISTS;
					}
				}
			}
			return true;
		}

		/**
		 * 白マスは壁2、黒マスは壁4
		 */
		protected boolean nextSolve() {
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
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						// 自分が不確定マスなら壁は2マスか4マス
						if ((existsCount == 3 && notExistsCount == 1) || notExistsCount > 2) {
							return false;
						}
						if (existsCount > 2) {
							masu[yIndex][xIndex] = Masu.BLACK;
						} else if (notExistsCount != 0) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						if (notExistsCount > 0) {
							return false;
						}
						// 周囲の壁を閉鎖
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
					} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						// 自分が白マスなら壁は必ず2マス
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
			return true;
		}

		/**
		 * 白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						if (whitePosSet.size() == 0) {
							whitePosSet.add(pos);
							setContinuePosSet(pos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(pos)) {
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
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!roomSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
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

	public static class Room {
		@Override
		public String toString() {
			return "[" + (isGray ? "○" : "×") + "curveCnt=" + curveCnt + ", member=" + member + "]";
		}

		// 数字情報。数字がない場合は-1。？は0。
		private final int curveCnt;
		// 灰色(追加必須)領域ならtrue。
		private final boolean isGray;
		// 部屋に属するマスの集合
		private final Set<Position> member;
		// 国境となる壁の位置情報
		private final Set<Position> upWallPosSet;
		private final Set<Position> rightWallPosSet;
		private final Set<Position> downWallPosSet;
		private final Set<Position> leftWallPosSet;

		public Room(int capacity, boolean isGray, Set<Position> member, Set<Position> upWallPosSet,
				Set<Position> rightWallPosSet, Set<Position> downWallPosSet, Set<Position> leftWallPosSet) {
			this.curveCnt = capacity;
			this.isGray = isGray;
			this.member = member;
			this.upWallPosSet = upWallPosSet;
			this.rightWallPosSet = rightWallPosSet;
			this.downWallPosSet = downWallPosSet;
			this.leftWallPosSet = leftWallPosSet;
		}

		public int getCurveCnt() {
			return curveCnt;
		}

		public boolean isGray() {
			return isGray;
		}

		public Set<Position> getMember() {
			return member;
		}

		public Set<Position> getUpWallPosSet() {
			return upWallPosSet;
		}

		public Set<Position> getRightWallPosSet() {
			return rightWallPosSet;
		}

		public Set<Position> getDownWallPosSet() {
			return downWallPosSet;
		}

		public Set<Position> getLeftWallPosSet() {
			return leftWallPosSet;
		}

		// 一番左→一番上の位置を返す。画面の数字描画用。
		public Position getNumberMasuPos() {
			int yIndex = Integer.MAX_VALUE;
			int xIndex = Integer.MAX_VALUE;
			for (Position pos : member) {
				if (pos.getxIndex() < xIndex) {
					xIndex = pos.getxIndex();
				}
			}
			for (Position pos : member) {
				if (pos.getxIndex() == xIndex && pos.getyIndex() < yIndex) {
					yIndex = pos.getyIndex();
				}
			}
			return new Position(yIndex, xIndex);
		}

	}

	private final Field field;
	private int count = 0;

	public OvotovataSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://pzprxs.vercel.app/p?ovotovata/10/10/8400a3n6m0001hil24o31g6c1g7s00443o7s-211-21d1d91d-119d"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new OvotovataSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 5 * 3));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 5) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count / 5 * 3).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
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
			field.masu = virtual2.masu;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
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
			field.masu = virtual2.masu;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

}
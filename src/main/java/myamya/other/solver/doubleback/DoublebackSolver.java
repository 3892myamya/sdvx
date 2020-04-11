package myamya.other.solver.doubleback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class DoublebackSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		// 通過しないマス
		private final Set<Position> blackPosSet;
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
		public Set<Position> getBlackPosSet() {
			return blackPosSet;
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
			blackPosSet = new HashSet<>();
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
								/ 16
								% 2 == 1;
					}
					if (mod >= 1) {
						yokoRoomWall[(cnt - mod + 1) / (getXLength() - 1)][(cnt - mod + 1) % (getXLength() - 1)] = bit
								/ 8
								% 2 == 1;
					}
					if (mod >= 2) {
						yokoRoomWall[(cnt - mod + 2) / (getXLength() - 1)][(cnt - mod + 2) % (getXLength() - 1)] = bit
								/ 4
								% 2 == 1;
					}
					if (mod >= 3) {
						yokoRoomWall[(cnt - mod + 3) / (getXLength() - 1)][(cnt - mod + 3) % (getXLength() - 1)] = bit
								/ 2
								% 2 == 1;
					}
					if (mod >= 4) {
						yokoRoomWall[(cnt - mod + 4) / (getXLength() - 1)][(cnt - mod + 4) % (getXLength() - 1)] = bit
								/ 1
								% 2 == 1;
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
			if (readPos < param.length()) {
				// 固定黒マス
				for (int cnt = 0; cnt < getYLength() * (getXLength()); cnt++) {
					int mod = cnt % 5;
					if (mod == 0) {
						bit = Character.getNumericValue(param.charAt(readPos));
						readPos++;
					}
					if (mod == 4 || cnt == (getYLength() * (getXLength())) - 1) {
						if (mod >= 0) {
							if (bit / 16 % 2 == 1) {
								Position pos = new Position((cnt - mod + 0) / (getXLength()),
										(cnt - mod + 0) % (getXLength()));
								blackPosSet.add(pos);
								if (pos.getyIndex() != 0) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
									tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()] = true;
								}
								if (pos.getxIndex() != getXLength() - 1) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									yokoRoomWall[pos.getyIndex()][pos.getxIndex()] = true;
								}
								if (pos.getyIndex() != getYLength() - 1) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									tateRoomWall[pos.getyIndex()][pos.getxIndex()] = true;
								}
								if (pos.getxIndex() != 0) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
									yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1] = true;
								}
							}
						}
						if (mod >= 1) {
							if (bit / 8 % 2 == 1) {
								Position pos = new Position((cnt - mod + 1) / (getXLength()),
										(cnt - mod + 1) % (getXLength()));
								blackPosSet.add(pos);
								if (pos.getyIndex() != 0) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
									tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()] = true;
								}
								if (pos.getxIndex() != getXLength() - 1) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									yokoRoomWall[pos.getyIndex()][pos.getxIndex()] = true;
								}
								if (pos.getyIndex() != getYLength() - 1) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									tateRoomWall[pos.getyIndex()][pos.getxIndex()] = true;
								}
								if (pos.getxIndex() != 0) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
									yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1] = true;
								}
							}
						}
						if (mod >= 2) {
							if (bit / 4 % 2 == 1) {
								Position pos = new Position((cnt - mod + 2) / (getXLength()),
										(cnt - mod + 2) % (getXLength()));
								blackPosSet.add(pos);
								if (pos.getyIndex() != 0) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
									tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()] = true;
								}
								if (pos.getxIndex() != getXLength() - 1) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									yokoRoomWall[pos.getyIndex()][pos.getxIndex()] = true;
								}
								if (pos.getyIndex() != getYLength() - 1) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									tateRoomWall[pos.getyIndex()][pos.getxIndex()] = true;
								}
								if (pos.getxIndex() != 0) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
									yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1] = true;
								}
							}
						}
						if (mod >= 3) {
							if (bit / 2 % 2 == 1) {
								Position pos = new Position((cnt - mod + 3) / (getXLength()),
										(cnt - mod + 3) % (getXLength()));
								blackPosSet.add(pos);
								if (pos.getyIndex() != 0) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
									tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()] = true;
								}
								if (pos.getxIndex() != getXLength() - 1) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									yokoRoomWall[pos.getyIndex()][pos.getxIndex()] = true;
								}
								if (pos.getyIndex() != getYLength() - 1) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									tateRoomWall[pos.getyIndex()][pos.getxIndex()] = true;
								}
								if (pos.getxIndex() != 0) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
									yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1] = true;
								}
							}
						}
						if (mod >= 4) {
							if (bit / 1 % 2 == 1) {
								Position pos = new Position((cnt - mod + 4) / (getXLength()),
										(cnt - mod + 4) % (getXLength()));
								blackPosSet.add(pos);
								if (pos.getyIndex() != 0) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
									tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()] = true;
								}
								if (pos.getxIndex() != getXLength() - 1) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									yokoRoomWall[pos.getyIndex()][pos.getxIndex()] = true;
								}
								if (pos.getyIndex() != getYLength() - 1) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									tateRoomWall[pos.getyIndex()][pos.getxIndex()] = true;
								}
								if (pos.getxIndex() != 0) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
									yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1] = true;
								}
							}
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
					for (Room room : rooms) {
						if (room.getMember().contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinuePosSet(pos, continuePosSet);
						Set<Position> yokoWallPosSet = new HashSet<>();
						Set<Position> tateWallPosSet = new HashSet<>();
						for (Position roomPos : continuePosSet) {
							if (roomPos.getyIndex() != 0
									&& tateRoomWall[roomPos.getyIndex() - 1][roomPos.getxIndex()]) {
								tateWallPosSet.add(new Position(roomPos.getyIndex() - 1, roomPos.getxIndex()));
							}
							if (roomPos.getxIndex() != getXLength() - 1
									&& yokoRoomWall[roomPos.getyIndex()][roomPos.getxIndex()]) {
								yokoWallPosSet.add(new Position(roomPos.getyIndex(), roomPos.getxIndex()));
							}
							if (roomPos.getyIndex() != getYLength() - 1
									&& tateRoomWall[roomPos.getyIndex()][roomPos.getxIndex()]) {
								tateWallPosSet.add(new Position(roomPos.getyIndex(), roomPos.getxIndex()));
							}
							if (roomPos.getxIndex() != 0
									&& yokoRoomWall[roomPos.getyIndex()][roomPos.getxIndex() - 1]) {
								yokoWallPosSet.add(new Position(roomPos.getyIndex(), roomPos.getxIndex() - 1));
							}
						}
						rooms.add(new Room(continuePosSet, yokoWallPosSet,
								tateWallPosSet));
					}
				}
			}
			// 固定黒マスしか含まない部屋は部屋ではない
			for (Iterator<Room> iterator = rooms.iterator(); iterator.hasNext();) {
				Room room = iterator.next();
				boolean okRoom = false;
				for (Position roomPos : room.member) {
					if (!blackPosSet.contains(roomPos)) {
						okRoom = true;
						break;
					}
				}
				if (!okRoom) {
					iterator.remove();
				}
			}
		}

		public Field(Field other) {
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
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
			blackPosSet = other.blackPosSet;
			yokoRoomWall = other.yokoRoomWall;
			tateRoomWall = other.tateRoomWall;
			rooms = other.rooms;
		}

		// posを起点に上下左右に部屋壁または白確定でないマスを無制限につなげていく。
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
					if (blackPosSet.contains(new Position(yIndex, xIndex))) {
						sb.append("■");
					} else {
						sb.append("・");
					}
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
		 * 各マスの壁の数は2になる。ならない場合falseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (blackPosSet.contains(new Position(yIndex, xIndex))) {
						continue;
					}
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
			return true;
		}

		/**
		 * 国境をまたぐことが可能なマスは、1つの国につき必ず4マスになる。
		 * ルールに違反している場合falseを返す。
		 */
		public boolean countrySolve() {
			for (Room room : rooms) {
				int whiteCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room.getYokoWallPosSet()) {
					if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
						whiteCnt++;
					} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
						spaceCnt++;
					}
				}
				for (Position pos : room.getTateWallPosSet()) {
					if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
						whiteCnt++;
					} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
						spaceCnt++;
					}
				}
				if (whiteCnt + spaceCnt < 4) {
					// 通過数不足
					return false;
				}
				// 通過しなければならないマスの数
				int retainWhiteCnt = 4 - whiteCnt;
				if (retainWhiteCnt < 0) {
					// 通過数超過
					return false;
				} else if (retainWhiteCnt == 0) {
					// 通過数が既に4なら他は通過しない
					for (Position pos : room.getYokoWallPosSet()) {
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
					}
					for (Position pos : room.getTateWallPosSet()) {
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
					}
				} else if (spaceCnt == retainWhiteCnt) {
					// 未確定壁数が通過せねばならない数に等しければ、未確定壁は通過
					for (Position pos : room.getYokoWallPosSet()) {
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
					}
					for (Position pos : room.getTateWallPosSet()) {
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
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
					if (blackPosSet.contains(pos)) {
						continue;
					}
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
			return true;
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスをつなげていく。
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
		 * ルール上、各列をふさぐ壁は必ず偶数になる。
		 * 偶数になっていない場合falseを返す。
		 */
		private boolean oddSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				int notExistsCount = 0;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
				int notExistsCount = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
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
			if (!nextSolve()) {
				return false;
			}
			if (!countrySolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!oddSolve()) {
					return false;
				}
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
			return "Room [member=" +
					member + "]";
		}

		// 部屋に属するマスの集合
		private final Set<Position> member;
		// 国境となる壁の位置情報
		private final Set<Position> yokoWallPosSet;
		private final Set<Position> tateWallPosSet;

		public Room(Set<Position> member, Set<Position> yokoWallPosSet, Set<Position> tateWallPosSet) {
			this.member = member;
			this.yokoWallPosSet = yokoWallPosSet;
			this.tateWallPosSet = tateWallPosSet;
		}

		public Set<Position> getMember() {
			return member;
		}

		public Set<Position> getYokoWallPosSet() {
			return yokoWallPosSet;
		}

		public Set<Position> getTateWallPosSet() {
			return tateWallPosSet;
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

	public DoublebackSolver(int height, int width, String param) {
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
		System.out.println(new DoublebackSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 2));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 2).toString();
	}

	/**
	 * 仮置きして調べる
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
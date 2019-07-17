package myamya.other.solver.yregions;

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

public class YajilinRegionsSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

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
		// バリアントルール-ループ内黒ます禁止
		private final boolean out;

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
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param, boolean out) {
			masu = new Masu[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			this.out = out;
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
			// 縦と横の壁の関係からにょろっと部屋を決めていく
			List<Integer> blackCntList = new ArrayList<>();
			for (; readPos < param.length(); readPos++) {
				char ch = param.charAt(readPos);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					for (int i = 0; i < interval + 1; i++) {
						// 数字がない部屋の場合は、部屋の数字は-1として扱う。
						blackCntList.add(-1);
					}
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int blackCnt;
					if (ch == '-') {
						blackCnt = Integer.parseInt("" + param.charAt(readPos + 1) + param.charAt(readPos + 2), 16);
						readPos++;
						readPos++;
					} else if (ch == '+') {
						blackCnt = Integer.parseInt(
								"" + param.charAt(readPos + 1) + param.charAt(readPos + 2) + param.charAt(readPos + 3),
								16);
						readPos++;
						readPos++;
						readPos++;
					} else {
						blackCnt = Integer.parseInt(String.valueOf(ch), 16);
					}
					blackCntList.add(blackCnt);
				}
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
						rooms.add(new Room(blackCntList.get(blackCntListIndex), continuePosSet, yokoWallPosSet,
								tateWallPosSet));
						blackCntListIndex++;
					}
				}
			}
		}

		// posを起点に上下左右に部屋壁または白確定でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoRoomWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateRoomWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
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
			out = other.out;
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
		 * 部屋のマスを埋める。黒マス不足・過剰はfalseを返す。
		 */
		public boolean roomSolve() {
			for (Room room : rooms) {
				if (room.getBlackCnt() != -1) {
					// 部屋に対する調査
					int blackCnt = 0;
					int spaceCnt = 0;
					for (Position pos : room.getMember()) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
							blackCnt++;
						} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							spaceCnt++;
						}
					}
					if (blackCnt + spaceCnt < room.getBlackCnt()) {
						// 黒マス不足
						return false;
					}
					// 置かねばならない黒マスの数
					int retainBlackCnt = room.getBlackCnt() - blackCnt;
					if (retainBlackCnt < 0) {
						// 黒マス超過
						return false;
					} else if (retainBlackCnt == 0) {
						// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
						for (Position pos : room.getMember()) {
							if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
							}
						}
					} else if (spaceCnt == retainBlackCnt) {
						// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
						for (Position pos : room.getMember()) {
							if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスの周囲の壁を埋め、隣接セルを白マスにする
		 * 黒マス隣接セルの隣に黒マスがある場合はfalseを返す。
		 * また、白マス隣接セルの周辺の壁の数が2にならない場合もfalseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Masu masuUp, masuRight, masuDown, masuLeft;
						// 周囲の壁を閉鎖
						if (yIndex != 0) {
							if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							masuUp = masu[yIndex - 1][xIndex];
						} else {
							masuUp = Masu.NOT_BLACK;
						}
						if (xIndex != getXLength() - 1) {
							if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
							masuRight = masu[yIndex][xIndex + 1];
						} else {
							masuRight = Masu.NOT_BLACK;
						}
						if (yIndex != getYLength() - 1) {
							if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex] = Wall.EXISTS;
							masuDown = masu[yIndex + 1][xIndex];
						} else {
							masuDown = Masu.NOT_BLACK;
						}
						if (xIndex != 0) {
							if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							masuLeft = masu[yIndex][xIndex - 1];
						} else {
							masuLeft = Masu.NOT_BLACK;
						}
						if (masuUp == Masu.BLACK || masuRight == Masu.BLACK || masuDown == Masu.BLACK
								|| masuLeft == Masu.BLACK) {
							return false;
						}
						if (masuUp == Masu.SPACE) {
							masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masuRight == Masu.SPACE) {
							masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuDown == Masu.SPACE) {
							masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masuLeft == Masu.SPACE) {
							masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
						}
					} else {
						int existsCount = 0;
						int notExistsCount = 0;
						Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
						if (wallUp == Wall.EXISTS) {
							existsCount++;
						} else if (wallUp == Wall.NOT_EXISTS) {
							if (masu[yIndex - 1][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							notExistsCount++;
						}
						Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
						if (wallRight == Wall.EXISTS) {
							existsCount++;
						} else if (wallRight == Wall.NOT_EXISTS) {
							if (masu[yIndex][xIndex + 1] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							notExistsCount++;
						}
						Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
						if (wallDown == Wall.EXISTS) {
							existsCount++;
						} else if (wallDown == Wall.NOT_EXISTS) {
							if (masu[yIndex + 1][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							notExistsCount++;
						}
						Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
						if (wallLeft == Wall.EXISTS) {
							existsCount++;
						} else if (wallLeft == Wall.NOT_EXISTS) {
							if (masu[yIndex][xIndex - 1] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							notExistsCount++;
						}
						// 自分が白マスなら壁は必ず2マス
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
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
									if (masu[yIndex - 1][xIndex] == Masu.BLACK) {
										return false;
									}
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
									masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
								}
								if (wallRight == Wall.SPACE) {
									if (masu[yIndex][xIndex + 1] == Masu.BLACK) {
										return false;
									}
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
									masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
								}
								if (wallDown == Wall.SPACE) {
									if (masu[yIndex + 1][xIndex] == Masu.BLACK) {
										return false;
									}
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
									masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
								}
								if (wallLeft == Wall.SPACE) {
									if (masu[yIndex][xIndex - 1] == Masu.BLACK) {
										return false;
									}
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
									masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
								}
							}
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							// 自分が不確定マスなら壁は2マスか4マス
							if ((existsCount == 3 && notExistsCount == 1)
									|| notExistsCount > 2) {
								return false;
							}
							if (existsCount == 2) {
								// 壁が2個の場合、壁のない方の白マス確定(出口理論)
								if (wallUp != Wall.EXISTS) {
									if (masu[yIndex - 1][xIndex] == Masu.BLACK) {
										return false;
									}
									masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
								}
								if (wallRight != Wall.EXISTS) {
									if (masu[yIndex][xIndex + 1] == Masu.BLACK) {
										return false;
									}
									masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
								}
								if (wallDown != Wall.EXISTS) {
									if (masu[yIndex + 1][xIndex] == Masu.BLACK) {
										return false;
									}
									masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
								}
								if (wallLeft != Wall.EXISTS) {
									if (masu[yIndex][xIndex - 1] == Masu.BLACK) {
										return false;
									}
									masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
								}
							}
							if (existsCount > 2) {
								masu[yIndex][xIndex] = Masu.BLACK;
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
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
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
			Position typicalWhitePos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						whitePosSet.add(whitePos);
						if (typicalWhitePos == null) {
							typicalWhitePos = whitePos;
						}
					}
				}
			}
			if (typicalWhitePos == null) {
				return true;
			} else {
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				setContinuePosSet(typicalWhitePos, continuePosSet, null);
				whitePosSet.removeAll(continuePosSet);
				return whitePosSet.isEmpty();
			}
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
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
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
				if (out) {
					if (!outsideSolve()) {
						return false;
					}
				}
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * ヤジリンのルール上、各列をふさぐ壁は必ず偶数になる。
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
		 * ループ内黒マス禁止ヤリジン専用の解法
		 * 黒マスから前後左右に向かっていき、外壁か黒マスのいずれかにぶつかるまでに
		 * 壁の数が奇数個であれば失敗。
		 * また、黒マスで未確定壁が残り1マスの場合は、偶数個になるように壁の有無が確定する。
		 */
		private boolean outsideSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pivot = new Position(yIndex, xIndex);
					// 左側横壁上方向
					int idx = 0;
					int whiteCnt = 0;
					int spaceCnt = 0;
					Position spacePos = null;
					while (pivot.getyIndex() - 1 - idx >= 0 && pivot.getxIndex() != 0
							&& masu[pivot.getyIndex() - 1 - idx][pivot.getxIndex()] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex() - 1);
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, true)) {
						return false;
					}
					// 右側横壁上方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getyIndex() - 1 - idx >= 0 && pivot.getxIndex() != getXLength() - 1
							&& masu[pivot.getyIndex() - 1 - idx][pivot.getxIndex()] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, true)) {
						return false;
					}

					// 左側横壁下方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getyIndex() + 1 + idx < getYLength() && pivot.getxIndex() != 0
							&& masu[pivot.getyIndex() + 1 + idx][pivot.getxIndex()] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex() - 1);
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, true)) {
						return false;
					}

					// 右側横壁下方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getyIndex() + 1 + idx < getYLength() && pivot.getxIndex() != getXLength() - 1
							&& masu[pivot.getyIndex() + 1 + idx][pivot.getxIndex()] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex());
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, true)) {
						return false;
					}

					// 上側縦壁右方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getxIndex() + 1 + idx < getXLength() && pivot.getyIndex() != 0
							&& masu[pivot.getyIndex()][pivot.getxIndex() + 1 + idx] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex() - 1, pivot.getxIndex() + 1 + idx);
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, false)) {
						return false;
					}

					// 下側縦壁右方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getxIndex() + 1 + idx < getXLength() && pivot.getyIndex() != getYLength() - 1
							&& masu[pivot.getyIndex()][pivot.getxIndex() + 1 + idx] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() + 1 + idx);
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, false)) {
						return false;
					}

					// 上側縦壁左方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getxIndex() - 1 - idx >= 0 && pivot.getyIndex() != 0
							&& masu[pivot.getyIndex()][pivot.getxIndex() - 1 - idx] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex() - 1, pivot.getxIndex() - 1 - idx);
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, false)) {
						return false;
					}

					// 下側縦壁左方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getxIndex() - 1 - idx >= 0 && pivot.getyIndex() != getYLength() - 1
							&& masu[pivot.getyIndex()][pivot.getxIndex() - 1 - idx] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, false)) {
						return false;
					}
				}
			}
			return true;
		}

		private boolean oneOutSideCheck(Position pivot, int whiteCnt, int spaceCnt, Position spacePos, boolean isYoko) {
			if (spaceCnt == 0 && whiteCnt % 2 == 1) {
				if (masu[pivot.getyIndex()][pivot.getxIndex()] == Masu.BLACK) {
					return false;
				} else if (masu[pivot.getyIndex()][pivot.getxIndex()] == Masu.SPACE) {
					masu[pivot.getyIndex()][pivot.getxIndex()] = Masu.NOT_BLACK;
				}
			} else if (masu[pivot.getyIndex()][pivot.getxIndex()] == Masu.BLACK && spaceCnt == 1) {
				if (whiteCnt % 2 == 1) {
					if (isYoko) {
						yokoWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.NOT_EXISTS;
					} else {
						tateWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.NOT_EXISTS;
					}
				} else {
					if (isYoko) {
						yokoWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.EXISTS;
					} else {
						tateWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.EXISTS;
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
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
			return "Room [blackCnt=" + blackCnt + ", member=" +
					member + "]";
		}

		// 白マスが何マスあるか。数字がない場合は-1
		private final int blackCnt;
		// 部屋に属するマスの集合
		private final Set<Position> member;
		// 国境となる壁の位置情報
		private final Set<Position> yokoWallPosSet;
		private final Set<Position> tateWallPosSet;

		public Room(int capacity, Set<Position> member, Set<Position> yokoWallPosSet, Set<Position> tateWallPosSet) {
			this.blackCnt = capacity;
			this.member = member;
			this.yokoWallPosSet = yokoWallPosSet;
			this.tateWallPosSet = tateWallPosSet;
		}

		public int getBlackCnt() {
			return blackCnt;
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

	public YajilinRegionsSolver(int height, int width, String param, boolean out) {
		field = new Field(height, width, param, out);
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
		System.out.println(new YajilinRegionsSolver(height, width, param, false).solve());
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
		System.out.println(field);
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					// 周囲に空白が少ない個所を優先して調査
					Masu masuUp = yIndex == 0 ? Masu.BLACK
							: field.masu[yIndex - 1][xIndex];
					Masu masuRight = xIndex == field.getXLength() - 1 ? Masu.BLACK
							: field.masu[yIndex][xIndex + 1];
					Masu masuDown = yIndex == field.getYLength() - 1 ? Masu.BLACK
							: field.masu[yIndex + 1][xIndex];
					Masu masuLeft = xIndex == 0 ? Masu.BLACK
							: field.masu[yIndex][xIndex - 1];
					int whiteCnt = 0;
					if (masuUp == Masu.SPACE) {
						whiteCnt++;
					}
					if (masuRight == Masu.SPACE) {
						whiteCnt++;
					}
					if (masuDown == Masu.SPACE) {
						whiteCnt++;
					}
					if (masuLeft == Masu.SPACE) {
						whiteCnt++;
					}
					if (whiteCnt > 3) {
						continue;
					}
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		if (recursive > 0) {
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
					if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
						Masu masuLeft = field.masu[yIndex][xIndex];
						Masu masuRight = field.masu[yIndex][xIndex + 1];
						if (masuLeft == Masu.SPACE && masuRight == Masu.SPACE) {
							continue;
						}
						count++;
						if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive - 1)) {
							return false;
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.tateWall[yIndex][xIndex] == Wall.SPACE) {
						Masu masuUp = field.masu[yIndex][xIndex];
						Masu masuDown = field.masu[yIndex + 1][xIndex];
						if (masuUp == Masu.SPACE && masuDown == Masu.SPACE) {
							continue;
						}
						count++;
						if (!oneCandTateWallSolve(field, yIndex, xIndex, recursive - 1)) {
							return false;
						}
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
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
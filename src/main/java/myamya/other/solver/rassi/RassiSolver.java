package myamya.other.solver.rassi;

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

public class RassiSolver implements Solver {

	public static class Field {
		// 黒マス
		private Set<Position> blackPosSet;
		// 初期壁位置。画面表示用
		private final Set<Position> yokoRoomWallPosSet;;
		// 初期壁位置。画面表示用
		private final Set<Position> tateRoomWallPosSet;
		// マスの情報。このパズルでは端点のマスを黒マスとみなす。メモ項目でありクリア条件には含まない。
		private Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<Set<Position>> rooms;

		public Set<Position> getBlackPosSet() {
			return blackPosSet;
		}

		public Masu[][] getMasu() {
			return masu;
		}

		public Set<Position> getYokoRoomWallPosSet() {
			return yokoRoomWallPosSet;
		}

		public Set<Position> getTateRoomWallPosSet() {
			return tateRoomWallPosSet;
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
			return yokoWall.length;
		}

		public int getXLength() {
			return tateWall[0].length;
		}

		public Field(int height, int width, String param) {
			blackPosSet = new HashSet<>();
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
			yokoRoomWallPosSet = new HashSet<>();
			tateRoomWallPosSet = new HashSet<>();
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * (getXLength() - 1); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					if (param.length() <= readPos) {
						break;
					}
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * (getXLength() - 1)) - 1) {
					if (mod >= 0) {
						yokoWall[(cnt - mod + 0) / (getXLength() - 1)][(cnt - mod + 0)
								% (getXLength() - 1)] = bit / 16 % 2 == 1 ? Wall.EXISTS : Wall.SPACE;
						if (bit / 16 % 2 == 1) {
							yokoRoomWallPosSet.add(new Position((cnt - mod + 0) / (getXLength() - 1),
									(cnt - mod + 0) % (getXLength() - 1)));
						}
					}
					if (mod >= 1) {
						yokoWall[(cnt - mod + 1) / (getXLength() - 1)][(cnt - mod + 1)
								% (getXLength() - 1)] = bit / 8 % 2 == 1 ? Wall.EXISTS : Wall.SPACE;
						if (bit / 8 % 2 == 1) {
							yokoRoomWallPosSet.add(new Position((cnt - mod + 1) / (getXLength() - 1),
									(cnt - mod + 1) % (getXLength() - 1)));
						}
					}
					if (mod >= 2) {
						yokoWall[(cnt - mod + 2) / (getXLength() - 1)][(cnt - mod + 2)
								% (getXLength() - 1)] = bit / 4 % 2 == 1 ? Wall.EXISTS : Wall.SPACE;
						if (bit / 4 % 2 == 1) {
							yokoRoomWallPosSet.add(new Position((cnt - mod + 2) / (getXLength() - 1),
									(cnt - mod + 2) % (getXLength() - 1)));
						}
					}
					if (mod >= 3) {
						yokoWall[(cnt - mod + 3) / (getXLength() - 1)][(cnt - mod + 3)
								% (getXLength() - 1)] = bit / 2 % 2 == 1 ? Wall.EXISTS : Wall.SPACE;
						if (bit / 2 % 2 == 1) {
							yokoRoomWallPosSet.add(new Position((cnt - mod + 3) / (getXLength() - 1),
									(cnt - mod + 3) % (getXLength() - 1)));
						}
					}
					if (mod >= 4) {
						yokoWall[(cnt - mod + 4) / (getXLength() - 1)][(cnt - mod + 4)
								% (getXLength() - 1)] = bit / 1 % 2 == 1 ? Wall.EXISTS : Wall.SPACE;
						if (bit / 1 % 2 == 1) {
							yokoRoomWallPosSet.add(new Position((cnt - mod + 4) / (getXLength() - 1),
									(cnt - mod + 4) % (getXLength() - 1)));
						}
					}
				}
			}
			for (int cnt = 0; cnt < (getYLength() - 1) * getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					if (param.length() <= readPos) {
						break;
					}
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == ((getYLength() - 1) * getXLength()) - 1) {
					if (mod >= 0) {
						tateWall[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16 % 2 == 1
								? Wall.EXISTS
								: Wall.SPACE;
						if (bit / 16 % 2 == 1) {
							tateRoomWallPosSet
									.add(new Position((cnt - mod + 0) / getXLength(), (cnt - mod + 0) % getXLength()));
						}
					}
					if (mod >= 1) {
						tateWall[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1
								? Wall.EXISTS
								: Wall.SPACE;
						if (bit / 8 % 2 == 1) {
							tateRoomWallPosSet
									.add(new Position((cnt - mod + 1) / getXLength(), (cnt - mod + 1) % getXLength()));
						}
					}
					if (mod >= 2) {
						tateWall[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1
								? Wall.EXISTS
								: Wall.SPACE;
						if (bit / 4 % 2 == 1) {
							tateRoomWallPosSet
									.add(new Position((cnt - mod + 2) / getXLength(), (cnt - mod + 2) % getXLength()));
						}
					}
					if (mod >= 3) {
						tateWall[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1
								? Wall.EXISTS
								: Wall.SPACE;
						if (bit / 2 % 2 == 1) {
							tateRoomWallPosSet
									.add(new Position((cnt - mod + 3) / getXLength(), (cnt - mod + 3) % getXLength()));
						}
					}
					if (mod >= 4) {
						tateWall[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1
								? Wall.EXISTS
								: Wall.SPACE;
						if (bit / 1 % 2 == 1) {
							tateRoomWallPosSet
									.add(new Position((cnt - mod + 4) / getXLength(), (cnt - mod + 4) % getXLength()));
						}
					}
				}
			}
			for (int cnt = 0; cnt < getYLength() * getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					if (param.length() <= readPos) {
						break;
					}
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * getXLength()) - 1) {
					if (mod >= 0 && bit / 16 % 2 == 1) {
						blackPosSet.add(new Position((cnt - mod + 0) / getXLength(), (cnt - mod + 0) % getXLength()));
					}
					if (mod >= 1 && bit / 8 % 2 == 1) {
						blackPosSet.add(new Position((cnt - mod + 1) / getXLength(), (cnt - mod + 1) % getXLength()));
					}
					if (mod >= 2 && bit / 4 % 2 == 1) {
						blackPosSet.add(new Position((cnt - mod + 2) / getXLength(), (cnt - mod + 2) % getXLength()));
					}
					if (mod >= 3 && bit / 2 % 2 == 1) {
						blackPosSet.add(new Position((cnt - mod + 3) / getXLength(), (cnt - mod + 3) % getXLength()));
					}
					if (mod >= 4 && bit / 1 % 2 == 1) {
						blackPosSet.add(new Position((cnt - mod + 4) / getXLength(), (cnt - mod + 4) % getXLength()));
					}
				}
			}
			// 黒マスの周りの壁を埋める。これも初期壁扱いに
			for (Position blackPos : blackPosSet) {
				if (blackPos.getyIndex() != 0) {
					tateWall[blackPos.getyIndex() - 1][blackPos.getxIndex()] = Wall.EXISTS;
					tateRoomWallPosSet.add(new Position(blackPos.getyIndex() - 1, blackPos.getxIndex()));
				}
				if (blackPos.getxIndex() != getXLength() - 1) {
					yokoWall[blackPos.getyIndex()][blackPos.getxIndex()] = Wall.EXISTS;
					yokoRoomWallPosSet.add(new Position(blackPos.getyIndex(), blackPos.getxIndex()));
				}
				if (blackPos.getyIndex() != getYLength() - 1) {
					tateWall[blackPos.getyIndex()][blackPos.getxIndex()] = Wall.EXISTS;
					tateRoomWallPosSet.add(new Position(blackPos.getyIndex(), blackPos.getxIndex()));
				}
				if (blackPos.getxIndex() != 0) {
					yokoWall[blackPos.getyIndex()][blackPos.getxIndex() - 1] = Wall.EXISTS;
					yokoRoomWallPosSet.add(new Position(blackPos.getyIndex(), blackPos.getxIndex() - 1));
				}
			}

			// 縦と横の壁の関係からにょろっと部屋を決めていく
			rooms = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (blackPosSet.contains(pos)) {
						continue;
					}
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
						setContinueRoomPosSet(pos, continuePosSet, null);
						rooms.add(continuePosSet);
					}
				}
			}
		}

		public Field(Field other) {
			blackPosSet = other.blackPosSet;
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
			yokoRoomWallPosSet = other.yokoRoomWallPosSet;
			tateRoomWallPosSet = other.tateRoomWallPosSet;
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
					if (blackPosSet.contains(new Position(yIndex, xIndex))) {
						sb.append("×");
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex]);
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex]);
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
		 * 白マスは壁2、黒マスは壁3
		 */
		protected boolean nextSolve() {
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
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						// 自分が不確定マスなら壁は2マスか3マス
						if (existsCount > 3 || notExistsCount > 2) {
							return false;
						}
						if (existsCount == 3) {
							masu[yIndex][xIndex] = Masu.BLACK;
						} else if (notExistsCount == 2) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						if (existsCount > 3 || notExistsCount > 1) {
							return false;
						}
						if (notExistsCount == 1) {
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
						} else if (existsCount == 3) {
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
		 * 黒マスは縦横斜めに隣り合わない。また、各部屋の黒マスはちょうど2個になる。
		 */
		protected boolean blackSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (blackPosSet.contains(new Position(yIndex, xIndex))) {
						continue;
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
						Masu masuUpRight = yIndex == 0 || xIndex == getXLength() - 1 ? Masu.NOT_BLACK
								: masu[yIndex - 1][xIndex + 1];
						Masu masuRightDown = xIndex == getXLength() - 1 || yIndex == getYLength() - 1 ? Masu.NOT_BLACK
								: masu[yIndex + 1][xIndex + 1];
						Masu masuDownLeft = yIndex == getYLength() - 1 || xIndex == 0 ? Masu.NOT_BLACK
								: masu[yIndex + 1][xIndex - 1];
						Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex - 1];
						if (masuUp == Masu.BLACK || masuRight == Masu.BLACK || masuDown == Masu.BLACK
								|| masuLeft == Masu.BLACK || masuUpRight == Masu.BLACK || masuRightDown == Masu.BLACK
								|| masuDownLeft == Masu.BLACK || masuLeftUp == Masu.BLACK) {
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
						if (masuUpRight == Masu.SPACE) {
							masu[yIndex - 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuRightDown == Masu.SPACE) {
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuDownLeft == Masu.SPACE) {
							masu[yIndex + 1][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuLeftUp == Masu.SPACE) {
							masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
						}
					}
				}
			}
			for (Set<Position> room : rooms) {
				// 部屋に対する調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < 2) {
					// 黒マス不足
					return false;
				}
				// 置かねばならない黒マスの数
				int retainBlackCnt = 2 - blackCnt;
				if (retainBlackCnt < 0) {
					// 黒マス超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (Position pos : room) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
					for (Position pos : room) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 部屋ごとに白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			for (Set<Position> room : rooms) {
				Position onePos = new ArrayList<>(room).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(onePos);
				setContinueRoomPosSet(onePos, continuePosSet, null);
				if (room.size() != continuePosSet.size()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスをつなげていく。 国境をまたぐこともできないものとする。
		 */
		private void setContinueRoomPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueRoomPosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueRoomPosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueRoomPosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueRoomPosSet(nextPos, continuePosSet, Direction.RIGHT);
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
			if (!blackSolve()) {
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

	private final Field field;
	private int count = 0;

	public RassiSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://pzprxs.vercel.app/p?rassi/12/8/828162h18gk8a0100g801g1023v000u10001o0e021g000010oe03g0"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new RassiSolver(height, width, param).solve());
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
		int level = (int) Math.sqrt(count / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count).toString() + "(Lv:" + level + ")";
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
package myamya.other.solver.castle;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class CastleSolver implements Solver {
	/**
	 * 矢印のマス
	 */
	public static class CastleArrow {
		private final Direction direction;
		private final int count;
		private final Masu outSide;// 0不明、1内、2外

		public CastleArrow(Direction direction, int count, Masu outSide) {
			this.direction = direction;
			this.count = count;
			this.outSide = outSide;
		}

		public Direction getDirection() {
			return direction;
		}

		public int getCount() {
			return count;
		}

		public Masu outSide() {
			return outSide;
		}

		@Override
		public String toString() {
			return count == -1 ? "？"
					: (count >= 10 ? String.valueOf(count)
							: outSide == Masu.BLACK ? direction.toString().toUpperCase() + count
									: direction.toString() + count);
		}

		public String toStringWeb() {
			return count == -1 ? "？" : direction.getDirectString() + count;
		}

	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 矢印の情報
		private CastleArrow[][] arrows;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Masu[][] getMasu() {
			return masu;
		}

		public CastleArrow[][] getArrows() {
			return arrows;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width) {
			masu = new Masu[height][width];
			arrows = new CastleArrow[height][width];
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
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			arrows = new CastleArrow[height][width];
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
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET.indexOf(ch) + 1;
				if (interval != 0) {
					index = index + interval;
				} else {
					Position arrowPos = new Position(index / getXLength(), index % getXLength());
					Masu outSide = Character.getNumericValue(ch) == 2 ? Masu.BLACK
							: Character.getNumericValue(ch) == 1 ? Masu.NOT_BLACK : Masu.SPACE;
					i++;

					boolean adjust = false;
					ch = param.charAt(i);
					int val = Character.getNumericValue(ch);
					if (5 <= val && val <= 9) {
						val = val - 5;
						adjust = true;
					}
					Direction direction = Direction.getByNum(val);
					i++;

					ch = param.charAt(i);
					int count;
					if (direction == null) {
						count = -1;
					} else if (adjust) {
						i++;
						count = Character.getNumericValue(ch) * 16
								+ Character.getNumericValue(param.charAt(i));
					} else {
						count = Character.getNumericValue(ch);
					}
					masu[arrowPos.getyIndex()][arrowPos.getxIndex()] = Masu.BLACK;
					arrows[arrowPos.getyIndex()][arrowPos.getxIndex()] = new CastleArrow(direction, count, outSide);
					// 周囲の壁を閉鎖
					if (arrowPos.getyIndex() != 0) {
						tateWall[arrowPos.getyIndex() - 1][arrowPos.getxIndex()] = Wall.EXISTS;
					}
					if (arrowPos.getxIndex() != getXLength() - 1) {
						yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.EXISTS;
					}
					if (arrowPos.getyIndex() != getYLength() - 1) {
						tateWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.EXISTS;
					}
					if (arrowPos.getxIndex() != 0) {
						yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex() - 1] = Wall.EXISTS;
					}
					index++;
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			arrows = other.arrows;
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
					if (arrows[yIndex][xIndex] != null) {
						sb.append(arrows[yIndex][xIndex]);
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
		 * 矢印はその方向に空いた壁がいくつあるかを示す。
		 * 矛盾する場合はfalseを返す。
		 */
		private boolean arrowSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null) {
						CastleArrow arrow = arrows[yIndex][xIndex];
						if (arrow.getCount() == -1) {
							continue;
						}
						Position pivot = new Position(yIndex, xIndex);
						int idx = 0;
						int whiteCnt = 0;
						int spaceCnt = 0;
						if (arrow.getDirection() == Direction.UP) {
							while (pivot.getyIndex() - 1 - idx >= 0) {
								Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
								if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
									whiteCnt++;
								} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
									spaceCnt++;
								}
								idx++;
							}
						}
						if (arrow.getDirection() == Direction.RIGHT) {
							while (pivot.getxIndex() + 1 + idx < getXLength()) {
								Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() + idx);
								if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
									whiteCnt++;
								} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
									spaceCnt++;
								}
								idx++;
							}
						}
						if (arrow.getDirection() == Direction.DOWN) {
							while (pivot.getyIndex() + 1 + idx < getYLength()) {
								Position pos = new Position(pivot.getyIndex() + idx, pivot.getxIndex());
								if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
									whiteCnt++;
								} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
									spaceCnt++;
								}
								idx++;
							}
						}
						if (arrow.getDirection() == Direction.LEFT) {
							while (pivot.getxIndex() - 1 - idx >= 0) {
								Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
								if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
									whiteCnt++;
								} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
									spaceCnt++;
								}
								idx++;
							}
						}
						if (arrow.getCount() < whiteCnt) {
							return false;
						} else if (arrow.getCount() > whiteCnt + spaceCnt) {
							return false;
						} else if (arrow.getCount() == whiteCnt) {
							if (arrow.getDirection() == Direction.UP) {
								while (pivot.getyIndex() - 1 - idx >= 0) {
									Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
									if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
										tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									}
									idx++;
								}
							}
							if (arrow.getDirection() == Direction.RIGHT) {
								while (pivot.getxIndex() + 1 + idx < getXLength()) {
									Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() + idx);
									if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
										yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									}
									idx++;
								}
							}
							if (arrow.getDirection() == Direction.DOWN) {
								while (pivot.getyIndex() + 1 + idx < getYLength()) {
									Position pos = new Position(pivot.getyIndex() + idx, pivot.getxIndex());
									if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
										tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									}
									idx++;
								}
							}
							if (arrow.getDirection() == Direction.LEFT) {
								while (pivot.getxIndex() - 1 - idx >= 0) {
									Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
									if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
										yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									}
									idx++;
								}
							}
						} else if (arrow.getCount() == whiteCnt + spaceCnt) {
							if (arrow.getDirection() == Direction.UP) {
								while (pivot.getyIndex() - 1 - idx >= 0) {
									Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
									if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
										tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
									}
									idx++;
								}
							}
							if (arrow.getDirection() == Direction.RIGHT) {
								while (pivot.getxIndex() + 1 + idx < getXLength()) {
									Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() + idx);
									if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
										yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
									}
									idx++;
								}
							}
							if (arrow.getDirection() == Direction.DOWN) {
								while (pivot.getyIndex() + 1 + idx < getYLength()) {
									Position pos = new Position(pivot.getyIndex() + idx, pivot.getxIndex());
									if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
										tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
									}
									idx++;
								}
							}
							if (arrow.getDirection() == Direction.LEFT) {
								while (pivot.getxIndex() - 1 - idx >= 0) {
									Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
									if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
										yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
									}
									idx++;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスの周囲の壁を埋める。
		 * また、白マス隣接セルの周辺の壁の数が2にならない場合もfalseを返す。
		 */
		public boolean nextSolve() {
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
						if ((existsCount == 3 && notExistsCount == 1)
								|| notExistsCount > 2) {
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
			Set<Position> blackCandPosSet = new HashSet<>();
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
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						blackCandPosSet.add(pos);
					}
				}
			}
			if (!whitePosSet.isEmpty()) {
				blackCandPosSet.removeAll(whitePosSet);
				for (Position pos : blackCandPosSet) {
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
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
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
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
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!arrowSolve()) {
				return false;
			}
			if (!outsideSolve()) {
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
				if (!finalSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * ルール上、各列をふさぐ壁は必ず偶数になる。
		 * 偶数になっていない場合falseを返す。
		 */
		private boolean oddSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				int notExistsCount = 0;
				Position spacePos = null;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						if (spacePos != null) {
							notExistsCount = 0;
							spacePos = null;
							break;
						} else {
							spacePos = new Position(yIndex, xIndex);
						}
					} else if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					if (spacePos != null) {
						tateWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.NOT_EXISTS;
					} else {
						return false;
					}
				} else {
					if (spacePos != null) {
						tateWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.EXISTS;
					}
				}
			}
			for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
				int notExistsCount = 0;
				Position spacePos = null;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						if (spacePos != null) {
							notExistsCount = 0;
							spacePos = null;
							break;
						} else {
							spacePos = new Position(yIndex, xIndex);
						}
					} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					if (spacePos != null) {
						yokoWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.NOT_EXISTS;
					} else {
						return false;
					}
				} else {
					if (spacePos != null) {
						yokoWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.EXISTS;
					}
				}
			}
			return true;
		}

		/**
		 * 矢印から前後左右に向かっていき、外壁にぶつかるまでに
		 * 壁の数が奇数個であれば内、偶数であれば外。内外矛盾があればfalse。
		 * また、未確定壁が残り1マスの場合は、壁の有無が確定する。
		 */
		private boolean outsideSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null && arrows[yIndex][xIndex].outSide() != Masu.SPACE) {
						Position pivot = new Position(yIndex, xIndex);
						// 左側横壁上方向
						int idx = 0;
						int whiteCnt = 0;
						int spaceCnt = 0;
						Position spacePos = null;
						while (pivot.getyIndex() - 1 - idx >= 0 && pivot.getxIndex() != 0) {
							Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex() - 1);
							if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
								whiteCnt++;
							} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
								spacePos = pos;
								spaceCnt++;
							}
							idx++;
						}
						if (!oneOutSideCheck(arrows[yIndex][xIndex].outSide(), whiteCnt, spaceCnt, spacePos, true)) {
							return false;
						}
						// 右側横壁上方向
						idx = 0;
						whiteCnt = 0;
						spaceCnt = 0;
						spacePos = null;
						while (pivot.getyIndex() - 1 - idx >= 0 && pivot.getxIndex() != getXLength() - 1) {
							Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
							if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
								whiteCnt++;
							} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
								spacePos = pos;
								spaceCnt++;
							}
							idx++;
						}
						if (!oneOutSideCheck(arrows[yIndex][xIndex].outSide(), whiteCnt, spaceCnt, spacePos, true)) {
							return false;
						}

						// 左側横壁下方向
						idx = 0;
						whiteCnt = 0;
						spaceCnt = 0;
						spacePos = null;
						while (pivot.getyIndex() + 1 + idx < getYLength() && pivot.getxIndex() != 0) {
							Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex() - 1);
							if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
								whiteCnt++;
							} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
								spacePos = pos;
								spaceCnt++;
							}
							idx++;
						}
						if (!oneOutSideCheck(arrows[yIndex][xIndex].outSide(), whiteCnt, spaceCnt, spacePos, true)) {
							return false;
						}

						// 右側横壁下方向
						idx = 0;
						whiteCnt = 0;
						spaceCnt = 0;
						spacePos = null;
						while (pivot.getyIndex() + 1 + idx < getYLength() && pivot.getxIndex() != getXLength() - 1) {
							Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex());
							if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
								whiteCnt++;
							} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
								spacePos = pos;
								spaceCnt++;
							}
							idx++;
						}
						if (!oneOutSideCheck(arrows[yIndex][xIndex].outSide(), whiteCnt, spaceCnt, spacePos, true)) {
							return false;
						}

						// 上側縦壁右方向
						idx = 0;
						whiteCnt = 0;
						spaceCnt = 0;
						spacePos = null;
						while (pivot.getxIndex() + 1 + idx < getXLength() && pivot.getyIndex() != 0) {
							Position pos = new Position(pivot.getyIndex() - 1, pivot.getxIndex() + 1 + idx);
							if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
								whiteCnt++;
							} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
								spacePos = pos;
								spaceCnt++;
							}
							idx++;
						}
						if (!oneOutSideCheck(arrows[yIndex][xIndex].outSide(), whiteCnt, spaceCnt, spacePos, false)) {
							return false;
						}

						// 下側縦壁右方向
						idx = 0;
						whiteCnt = 0;
						spaceCnt = 0;
						spacePos = null;
						while (pivot.getxIndex() + 1 + idx < getXLength() && pivot.getyIndex() != getYLength() - 1) {
							Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() + 1 + idx);
							if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
								whiteCnt++;
							} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
								spacePos = pos;
								spaceCnt++;
							}
							idx++;
						}
						if (!oneOutSideCheck(arrows[yIndex][xIndex].outSide(), whiteCnt, spaceCnt, spacePos, false)) {
							return false;
						}

						// 上側縦壁左方向
						idx = 0;
						whiteCnt = 0;
						spaceCnt = 0;
						spacePos = null;
						while (pivot.getxIndex() - 1 - idx >= 0 && pivot.getyIndex() != 0) {
							Position pos = new Position(pivot.getyIndex() - 1, pivot.getxIndex() - 1 - idx);
							if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
								whiteCnt++;
							} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
								spacePos = pos;
								spaceCnt++;
							}
							idx++;
						}
						if (!oneOutSideCheck(arrows[yIndex][xIndex].outSide(), whiteCnt, spaceCnt, spacePos, false)) {
							return false;
						}

						// 下側縦壁左方向
						idx = 0;
						whiteCnt = 0;
						spaceCnt = 0;
						spacePos = null;
						while (pivot.getxIndex() - 1 - idx >= 0 && pivot.getyIndex() != getYLength() - 1) {
							Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
							if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
								whiteCnt++;
							} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
								spacePos = pos;
								spaceCnt++;
							}
							idx++;
						}
						if (!oneOutSideCheck(arrows[yIndex][xIndex].outSide(), whiteCnt, spaceCnt, spacePos, false)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		private boolean oneOutSideCheck(Masu outSide, int whiteCnt, int spaceCnt, Position spacePos,
				boolean isYoko) {
			if (spaceCnt == 0) {
				if ((whiteCnt % 2 == 1) == (outSide == Masu.BLACK)) {
					return false;
				}
			} else if (spaceCnt == 1) {
				if ((whiteCnt % 2 == 1) == (outSide == Masu.BLACK)) {
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

		/**
		 * フィールドに1つは白マスが必要。
		 */
		private boolean finalSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] != Masu.BLACK) {
						return true;
					}
				}
			}
			return false;
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

	protected final Field field;
	protected int count = 0;

	public CastleSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public CastleSolver(Field field) {
		this.field = new Field(field);
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
		System.out.println(new CastleSolver(height, width, param).solve());
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
		// int level = (int) Math.sqrt(count * 2 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 2).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
				if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
					//					Masu masuLeft = field.masu[yIndex][xIndex];
					//					Masu masuRight = field.masu[yIndex][xIndex + 1];
					//					if (masuLeft == Masu.SPACE && masuRight == Masu.SPACE) {
					//						continue;
					//					}
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
					//					Masu masuUp = field.masu[yIndex][xIndex];
					//					Masu masuDown = field.masu[yIndex + 1][xIndex];
					//					if (masuUp == Masu.SPACE && masuDown == Masu.SPACE) {
					//						continue;
					//					}
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
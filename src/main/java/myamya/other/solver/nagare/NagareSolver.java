package myamya.other.solver.nagare;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class NagareSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 矢印の情報
		private final Direction[][] nagare;
		// 風の情報
		private final Set<Direction>[][] wind;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Masu[][] getMasu() {
			return masu;
		}

		public Direction[][] getPearl() {
			return nagare;
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

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param, boolean ura) {
			masu = new Masu[height][width];
			nagare = new Direction[height][width];
			wind = new HashSet[height][width];
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					wind[yIndex][xIndex] = new HashSet<>();
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int oneChar = Character.getNumericValue(ch);
				if (oneChar <= 9) {
					Position pos = new Position(index / getXLength(), index % getXLength());
					Direction direction = Direction.getByNum(oneChar % 5);
					if (direction != null) {
						nagare[pos.getyIndex()][pos.getxIndex()] = direction;
					}
					if (oneChar >= 5) {
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						// 周りの壁が確定
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
						}
					} else {
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						// 流れの壁が確定
						if (pos.getyIndex() != 0 && (direction == Direction.RIGHT || direction == Direction.LEFT)) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1
								&& (direction == Direction.UP || direction == Direction.DOWN)) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1
								&& (direction == Direction.RIGHT || direction == Direction.LEFT)) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getxIndex() != 0 && (direction == Direction.UP || direction == Direction.DOWN)) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
						}
					}
				} else {
					index = index + oneChar - 10;
				}
				index++;
			}
			// 黒マスから出ている流れをもとに風を吹かせる。
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK && nagare[yIndex][xIndex] != null) {
						if (nagare[yIndex][xIndex] == Direction.UP) {
							for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
								if (masu[targetY][xIndex] == Masu.BLACK) {
									break;
								} else {
									wind[targetY][xIndex].add(nagare[yIndex][xIndex]);
								}
							}
						}
						if (nagare[yIndex][xIndex] == Direction.RIGHT) {
							for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
								if (masu[yIndex][targetX] == Masu.BLACK) {
									break;
								} else {
									wind[yIndex][targetX].add(nagare[yIndex][xIndex]);
								}
							}
						}
						if (nagare[yIndex][xIndex] == Direction.DOWN) {
							for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
								if (masu[targetY][xIndex] == Masu.BLACK) {
									break;
								} else {
									wind[targetY][xIndex].add(nagare[yIndex][xIndex]);
								}
							}
						}
						if (nagare[yIndex][xIndex] == Direction.LEFT) {
							for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
								if (masu[yIndex][targetX] == Masu.BLACK) {
									break;
								} else {
									wind[yIndex][targetX].add(nagare[yIndex][xIndex]);
								}
							}
						}
					}
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			nagare = other.nagare;
			wind = other.wind;
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
					if (nagare[yIndex][xIndex] != null) {
						sb.append(nagare[yIndex][xIndex].getDirectString());
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
		 * 風を横切るような移動の仕方は禁止。
		 */
		private boolean windSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (wind[yIndex][xIndex].size() != 0) {
						Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
						Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
						Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
						Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
						if (wind[yIndex][xIndex].contains(Direction.UP)) {
							if (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS) {
								return false;
							}
							if (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE && wallLeft == Wall.NOT_EXISTS) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
						}
						if (wind[yIndex][xIndex].contains(Direction.RIGHT)) {
							if (wallUp == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS) {
								return false;
							}
							if (wallUp == Wall.NOT_EXISTS && wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallUp == Wall.SPACE && wallDown == Wall.NOT_EXISTS) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
						}
						if (wind[yIndex][xIndex].contains(Direction.DOWN)) {
							if (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS) {
								return false;
							}
							if (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE && wallLeft == Wall.NOT_EXISTS) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
						}
						if (wind[yIndex][xIndex].contains(Direction.LEFT)) {
							if (wallUp == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS) {
								return false;
							}
							if (wallUp == Wall.NOT_EXISTS && wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallUp == Wall.SPACE && wallDown == Wall.NOT_EXISTS) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスの周囲の壁を埋め、隣接セルを白マスにする
		 * また、白マス隣接セルの周辺の壁の数が2にならない場合もfalseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						// 周囲の壁を閉鎖
						if (yIndex != 0) {
							if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (xIndex != getXLength() - 1) {
							if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (yIndex != getYLength() - 1) {
							if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (xIndex != 0) {
							if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
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
							if (existsCount > 2) {
								masu[yIndex][xIndex] = Masu.BLACK;
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
			Set<Position> allPosSet = new HashSet<>();
			Position typicalWhitePos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					allPosSet.add(pos);
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						if (typicalWhitePos == null) {
							typicalWhitePos = pos;
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
				allPosSet.removeAll(continuePosSet);
				for (Position pos : allPosSet) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
						return false;
					}
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
				}
				return true;
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
		 * 流れの通りにたどっていったとき、流れや風に対して逆に侵入したらfalseを返す。
		 */
		private boolean flowSolve() {
			Set<Position> continuePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK && nagare[yIndex][xIndex] != null) {
						Position originPos = new Position(yIndex, xIndex);
						if (continuePosSet.contains(originPos)) {
							continue;
						}
						continuePosSet.add(originPos);
						if (!checkFlow(originPos, continuePosSet, nagare[yIndex][xIndex].opposite())) {
							return false;
						}
					}
				}
			}
			return true;
		}

		private boolean checkFlow(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP
					&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (nagare[nextPos.getyIndex()][nextPos.getxIndex()] == Direction.DOWN ||
						wind[nextPos.getyIndex()][nextPos.getxIndex()].contains(Direction.DOWN)) {
					return false;
				} else {
					if (continuePosSet.contains(nextPos)) {
						return true;
					}
					continuePosSet.add(nextPos);
					return checkFlow(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT
					&& yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (nagare[nextPos.getyIndex()][nextPos.getxIndex()] == Direction.LEFT ||
						wind[nextPos.getyIndex()][nextPos.getxIndex()].contains(Direction.LEFT)) {
					return false;
				} else {
					if (continuePosSet.contains(nextPos)) {
						return true;
					}
					continuePosSet.add(nextPos);
					return checkFlow(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN
					&& tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (nagare[nextPos.getyIndex()][nextPos.getxIndex()] == Direction.UP ||
						wind[nextPos.getyIndex()][nextPos.getxIndex()].contains(Direction.UP)) {
					return false;
				} else {
					if (continuePosSet.contains(nextPos)) {
						return true;
					}
					continuePosSet.add(nextPos);
					return checkFlow(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT
					&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (nagare[nextPos.getyIndex()][nextPos.getxIndex()] == Direction.RIGHT ||
						wind[nextPos.getyIndex()][nextPos.getxIndex()].contains(Direction.RIGHT)) {
					return false;
				} else {
					if (continuePosSet.contains(nextPos)) {
						return true;
					}
					continuePosSet.add(nextPos);
					return checkFlow(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
			return true;
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
			if (!windSolve()) {
				return false;
			}
			if (!oddSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				if (!flowSolve()) {
					return false;
				}
				if (!connectSolve()) {
					return false;
				}
				return solveAndCheck();
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

	private final Field field;
	private int count = 0;

	public NagareSolver(int height, int width, String param, boolean ura) {
		field = new Field(height, width, param, ura);
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
		System.out.println(new NagareSolver(height, width, param, false).solve());
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
		System.out.println("難易度:" + (count / 2));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 2).toString();
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
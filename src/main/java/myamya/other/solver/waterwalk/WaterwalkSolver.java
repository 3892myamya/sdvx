package myamya.other.solver.waterwalk;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class WaterwalkSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";
		// アイスウォークの場合true
		private final boolean isIcewalk;
		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		protected final Integer[][] numbers;
		// 水の情報
		protected final boolean[][] circle;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected Wall[][] tateWall;

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public boolean[][] getCircle() {
			return circle;
		}

		public int getYLength() {
			return circle.length;
		}

		public int getXLength() {
			return circle[0].length;
		}

		public Field(int height, int width, boolean isIcewalk) {
			this.isIcewalk = isIcewalk;
			masu = new Masu[height][width];
			circle = new boolean[height][width];
			numbers = new Integer[height][width];
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

		public Field(int height, int width, String param, boolean isIcewalk) {
			this.isIcewalk = isIcewalk;
			masu = new Masu[height][width];
			circle = new boolean[height][width];
			numbers = new Integer[height][width];
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
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * getXLength()) - 1) {
					if (mod >= 0) {
						circle[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16 % 2 == 1;
					}
					if (mod >= 1) {
						circle[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1;
					}
					if (mod >= 2) {
						circle[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1;
					}
					if (mod >= 3) {
						circle[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1;
					}
					if (mod >= 4) {
						circle[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1;
					}
				}
			}
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					// 16 - 255は '-'
					// 256 - 999は '+'
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
							capacity = Integer
									.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3), 16);
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
			isIcewalk = other.isIcewalk;
			masu = new Masu[other.getYLength()][other.getXLength()];
			circle = new boolean[other.getYLength()][other.getXLength()];
			numbers = new Integer[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					circle[yIndex][xIndex] = other.circle[yIndex][xIndex];
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
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
						sb.append(masu[yIndex][xIndex] == Masu.BLACK ? masu[yIndex][xIndex]
								: circle[yIndex][xIndex] ? "○" : masu[yIndex][xIndex]);
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
		 * 壁の数は2か4のいずれかになる。違反の場合false。
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
						if (isIcewalk && circle[yIndex][xIndex]) {
							// アイスウォークで氷の場合は壁は0マスか2マス(直進)か4マス
							if ((notExistsCount == 1 && existsCount == 3)
									|| (notExistsCount == 3 && existsCount == 1)) {
								return false;
							}
							if (existsCount == 3) {
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
							}
							if (notExistsCount == 3) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
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
							// 直進させる
							if (wallDown == Wall.NOT_EXISTS) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
								if (wallUp == Wall.EXISTS) {
									return false;
								} else {
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (wallLeft == Wall.NOT_EXISTS) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
								if (wallRight == Wall.EXISTS) {
									return false;
								} else {
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (wallUp == Wall.NOT_EXISTS) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
								if (wallDown == Wall.EXISTS) {
									return false;
								} else {
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (wallRight == Wall.NOT_EXISTS) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
								if (wallLeft == Wall.EXISTS) {
									return false;
								} else {
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
								}
							}
						} else {
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
						if (isIcewalk && circle[yIndex][xIndex]) {
							// アイスウォークで氷の場合は壁は0マスか2マス(直進)
							if (existsCount > 2 || (notExistsCount == 3 && existsCount == 1)) {
								return false;
							}
							if (notExistsCount == 3) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
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
							// 直進させる
							if (wallDown == Wall.NOT_EXISTS || wallRight == Wall.EXISTS || wallLeft == Wall.EXISTS) {
								if (wallUp == Wall.EXISTS) {
									return false;
								} else {
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (wallLeft == Wall.NOT_EXISTS || wallDown == Wall.EXISTS || wallUp == Wall.EXISTS) {
								if (wallRight == Wall.EXISTS) {
									return false;
								} else {
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (wallUp == Wall.NOT_EXISTS || wallRight == Wall.EXISTS || wallLeft == Wall.EXISTS) {
								if (wallDown == Wall.EXISTS) {
									return false;
								} else {
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (wallRight == Wall.NOT_EXISTS || wallDown == Wall.EXISTS || wallUp == Wall.EXISTS) {
								if (wallLeft == Wall.EXISTS) {
									return false;
								} else {
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
								}
							}
						} else {
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
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK && !circle[yIndex][xIndex]) {
						if (whitePosSet.size() == 0) {
							whitePosSet.add(pos);
							if (isIcewalk) {
								Set<Position> checkPosSet = new HashSet<>();
								checkPosSet.add(pos);
								setContinuePosSetIce(pos, whitePosSet, checkPosSet, null);
							} else {
								setContinuePosSet(pos, whitePosSet, null);
							}
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
			blackCandPosSet.removeAll(whitePosSet);
			for (Position pos : blackCandPosSet) {
				masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
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
		 * アイスウォーク用。
		 * 
		 * @param checkPosSet
		 */
		private void setContinuePosSetIce(Position pos, Set<Position> continuePosSet, Set<Position> checkPosSet,
				Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				if (circle[pos.getyIndex()][pos.getxIndex()] && from != Direction.DOWN) {
//
				} else {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
							&& !checkPosSet.contains(nextPos)) {
						continuePosSet.add(nextPos);
						checkPosSet.add(nextPos);
						setContinuePosSetIce(nextPos, continuePosSet, checkPosSet, Direction.DOWN);
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				if (circle[pos.getyIndex()][pos.getxIndex()] && from != Direction.LEFT) {
//
				} else {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
							&& !checkPosSet.contains(circle[nextPos.getyIndex()][nextPos.getxIndex()]
									? new Position(nextPos.getyIndex() + 100, nextPos.getxIndex() + 100)
									: nextPos)) {
						continuePosSet.add(nextPos);
						checkPosSet.add(circle[nextPos.getyIndex()][nextPos.getxIndex()]
								? new Position(nextPos.getyIndex() + 100, nextPos.getxIndex() + 100)
								: nextPos);
						setContinuePosSetIce(nextPos, continuePosSet, checkPosSet, Direction.LEFT);
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				if (circle[pos.getyIndex()][pos.getxIndex()] && from != Direction.UP) {
//
				} else {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !checkPosSet.contains(nextPos)) {
						continuePosSet.add(nextPos);
						checkPosSet.add(nextPos);
						setContinuePosSetIce(nextPos, continuePosSet, checkPosSet, Direction.UP);
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				if (circle[pos.getyIndex()][pos.getxIndex()] && from != Direction.RIGHT) {
//
				} else {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
							&& !checkPosSet.contains(circle[nextPos.getyIndex()][nextPos.getxIndex()]
									? new Position(nextPos.getyIndex() + 100, nextPos.getxIndex() + 100)
									: nextPos)) {
						continuePosSet.add(nextPos);
						checkPosSet.add(circle[nextPos.getyIndex()][nextPos.getxIndex()]
								? new Position(nextPos.getyIndex() + 100, nextPos.getxIndex() + 100)
								: nextPos);
						setContinuePosSetIce(nextPos, continuePosSet, checkPosSet, Direction.RIGHT);
					}
				}
			}
		}

		/**
		 * ルール上、各列をふさぐ壁は必ず偶数になる。 偶数になっていない場合falseを返す。
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
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!numberSolve()) {
				return false;
			}
			if (!connectSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!isIcewalk && !waterSolve()) {
					return false;
				}
				if (!oddSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 部屋のサイズが超過していたり、別部屋と繋がっていた場合、falseを返す。 部屋が既定サイズに到達している場合、周囲を壁で埋める。
		 */
		public boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continueNotBlackPosSet = new HashSet<>();
						continueNotBlackPosSet.add(pivot);
						if (!setContinueNotBlackPosSet(numbers[yIndex][xIndex], pivot, continueNotBlackPosSet, null)) {
							if (continueNotBlackPosSet.size() != numbers[yIndex][xIndex]) {
								// サイズ不足
								return false;
							}
						}
						Set<Position> continueWhitePosSet = new HashSet<>();
						continueWhitePosSet.add(pivot);
						if (!setContinueWhitePosSet(numbers[yIndex][xIndex], pivot, continueWhitePosSet, null)) {
							// 別部屋と連結またはサイズ超過
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定でないマスをつなげていくが、 繋げたマスが水だったり、自分と違う数字マスの場合はつながない。
		 * サイズが不足しないと分かった時点でtrueを返す。
		 */
		private boolean setContinueNotBlackPosSet(int size, Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (continuePosSet.size() >= size) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (circle[nextPos.getyIndex()][nextPos.getxIndex()]
							|| (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
									&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
									&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size)) {
						// 水マスや別の数字とは繋がらない
					} else {
						continuePosSet.add(nextPos);
						if (setContinueNotBlackPosSet(size, nextPos, continuePosSet, Direction.DOWN)) {
							return true;
						}
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					if (circle[nextPos.getyIndex()][nextPos.getxIndex()]
							|| (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
									&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
									&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size)) {
						// 水マスや別の数字とは繋がらない
					} else {
						continuePosSet.add(nextPos);
						if (setContinueNotBlackPosSet(size, nextPos, continuePosSet, Direction.LEFT)) {
							return true;
						}
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					if (circle[nextPos.getyIndex()][nextPos.getxIndex()]
							|| (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
									&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
									&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size)) {
						// 水マスや別の数字とは繋がらない
					} else {
						continuePosSet.add(nextPos);
						if (setContinueNotBlackPosSet(size, nextPos, continuePosSet, Direction.UP)) {
							return true;
						}
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (circle[nextPos.getyIndex()][nextPos.getxIndex()]
							|| (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
									&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
									&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size)) {
						// 水マスや別の数字とは繋がらない
					} else {
						continuePosSet.add(nextPos);
						if (setContinueNotBlackPosSet(size, nextPos, continuePosSet, Direction.RIGHT)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右に白確定のマスを無制限につなぎ、違う数字にたどり着いたり想定サイズを超過したらfalseを返す。
		 */
		private boolean setContinueWhitePosSet(Integer size, Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (circle[nextPos.getyIndex()][nextPos.getxIndex()]) {
						// 繋がない
					} else if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size) {
						return false;
					} else {
						continuePosSet.add(nextPos);
						if (!setContinueWhitePosSet(size, nextPos, continuePosSet, Direction.DOWN)) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (circle[nextPos.getyIndex()][nextPos.getxIndex()]) {
						// 繋がない
					} else if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size) {
						return false;
					} else {
						continuePosSet.add(nextPos);
						if (!setContinueWhitePosSet(size, nextPos, continuePosSet, Direction.LEFT)) {
							return false;
						}
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (circle[nextPos.getyIndex()][nextPos.getxIndex()]) {
						// 繋がない
					} else if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size) {
						return false;
					} else {
						continuePosSet.add(nextPos);
						if (!setContinueWhitePosSet(size, nextPos, continuePosSet, Direction.UP)) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (circle[nextPos.getyIndex()][nextPos.getxIndex()]) {
						// 繋がない
					} else if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size) {
						return false;
					} else {
						continuePosSet.add(nextPos);
						if (!setContinueWhitePosSet(size, nextPos, continuePosSet, Direction.RIGHT)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 水を3マス以上連続して通過してはいけない。
		 */
		private boolean waterSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (circle[yIndex][xIndex]) {
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continueWhitePosSet = new HashSet<>();
						continueWhitePosSet.add(pivot);
						if (!setContinueWaterPosSet(pivot, continueWhitePosSet, null)) {
							// サイズ超過
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 水が3マス以上続いたらfalse
		 */
		private boolean setContinueWaterPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (continuePosSet.size() > 2) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (!circle[nextPos.getyIndex()][nextPos.getxIndex()]) {
						// 繋がない
					} else {
						continuePosSet.add(nextPos);
						if (!setContinueWaterPosSet(nextPos, continuePosSet, Direction.DOWN)) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (!circle[nextPos.getyIndex()][nextPos.getxIndex()]) {
						// 繋がない
					} else {
						continuePosSet.add(nextPos);
						if (!setContinueWaterPosSet(nextPos, continuePosSet, Direction.LEFT)) {
							return false;
						}
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (!circle[nextPos.getyIndex()][nextPos.getxIndex()]) {
						// 繋がない
					} else {
						continuePosSet.add(nextPos);
						if (!setContinueWaterPosSet(nextPos, continuePosSet, Direction.UP)) {
							return true;
						}
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (!circle[nextPos.getyIndex()][nextPos.getxIndex()]) {
						// 繋がない
					} else {
						continuePosSet.add(nextPos);
						if (!setContinueWaterPosSet(nextPos, continuePosSet, Direction.RIGHT)) {
							return false;
						}
					}
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

	protected final Field field;
	protected int count = 0;

	public WaterwalkSolver(int height, int width, String param, boolean isIcewalk) {
		field = new Field(height, width, param, isIcewalk);
	}

	public WaterwalkSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = ""; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new WaterwalkSolver(height, width, param, false).solve());
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

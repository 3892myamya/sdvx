package myamya.other.solver.firefly;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class FireflySolver implements Solver {

	/**
	 * ホタルのマス
	 */
	public static class Firefly {

		private final Position position;
		private final Direction direction;
		private final int count;

		public Firefly(Position position, Direction direction, int count) {
			this.position = position;
			this.direction = direction;
			this.count = count;
		}

		public Position getPosition() {
			return position;
		}

		public Direction getDirection() {
			return direction;
		}

		public int getCount() {
			return count;
		}

		@Override
		public String toString() {
			return count == -1 ? direction.getDirectString()
					: (count >= 10 ? String.valueOf(count) : direction.toString() + count);
		}

	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// ホタルの情報
		private Firefly[][] fireflies;

		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Masu[][] getMasu() {
			return masu;
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

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			fireflies = new Firefly[height][width];
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
			Direction direction = null;
			boolean adjust = false;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				if (direction == null) {
					int interval = ALPHABET.indexOf(ch) + 1;
					if (interval != 0) {
						index = index + interval;
					} else {
						int val = Character.getNumericValue(ch);
						if (5 <= val && val <= 9) {
							val = val - 5;
							adjust = true;
						}
						direction = Direction.getByNum(val);
						if (direction == null) {
							if (adjust) {
								i++;
							} else {
								Position arrowPos = new Position(index / getXLength(), index % getXLength());
								masu[arrowPos.getyIndex()][arrowPos.getxIndex()] = Masu.NOT_BLACK;
								fireflies[arrowPos.getyIndex()][arrowPos.getxIndex()] = new Firefly(arrowPos, direction,
										-1);
								// ホタルの尻は壁なし確定
								if (arrowPos.getyIndex() != 0 && direction == Direction.UP) {
									tateWall[arrowPos.getyIndex() - 1][arrowPos.getxIndex()] = Wall.NOT_EXISTS;
								}
								if (arrowPos.getxIndex() != getXLength() - 1 && direction == Direction.RIGHT) {
									yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.NOT_EXISTS;
								}
								if (arrowPos.getyIndex() != getYLength() - 1 && direction == Direction.DOWN) {
									tateWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.NOT_EXISTS;
								}
								if (arrowPos.getxIndex() != 0 && direction == Direction.LEFT) {
									yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex() - 1] = Wall.NOT_EXISTS;
								}
							}
							index++;
							i++;
							adjust = false;
						}
					}
				} else {
					Position arrowPos = new Position(index / getXLength(), index % getXLength());
					Firefly firefly;
					if (adjust) {
						i++;
						firefly = new Firefly(arrowPos, direction, Character.getNumericValue(ch) * 16
								+ Character.getNumericValue(param.charAt(i)));
					} else {
						firefly = new Firefly(arrowPos, direction, Character.getNumericValue(ch));
					}
					masu[arrowPos.getyIndex()][arrowPos.getxIndex()] = Masu.NOT_BLACK;
					fireflies[arrowPos.getyIndex()][arrowPos.getxIndex()] = firefly;
					// ホタルの尻は壁なし確定
					if (arrowPos.getyIndex() != 0 && direction == Direction.UP) {
						tateWall[arrowPos.getyIndex() - 1][arrowPos.getxIndex()] = Wall.NOT_EXISTS;
					}
					if (arrowPos.getxIndex() != getXLength() - 1 && direction == Direction.RIGHT) {
						yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.NOT_EXISTS;
					}
					if (arrowPos.getyIndex() != getYLength() - 1 && direction == Direction.DOWN) {
						tateWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.NOT_EXISTS;
					}
					if (arrowPos.getxIndex() != 0 && direction == Direction.LEFT) {
						yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex() - 1] = Wall.NOT_EXISTS;
					}
					adjust = false;
					index++;
					direction = null;
				}
			}
			// ホタルが2匹並んでいていずれも尻でない場合、間に壁を作る。
			// TODO これだけここでやるのが微妙だなあ…
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (fireflies[yIndex][xIndex] != null) {
						Firefly pivotFf = fireflies[yIndex][xIndex];
						Firefly ffU = yIndex == 0 ? null
								: fireflies[yIndex - 1][xIndex];
						Firefly ffR = xIndex == getXLength() - 1 ? null
								: fireflies[yIndex][xIndex + 1];
						Firefly ffD = yIndex == getYLength() - 1 ? null
								: fireflies[yIndex + 1][xIndex];
						Firefly ffL = xIndex == 0 ? null
								: fireflies[yIndex][xIndex - 1];
						if (ffU != null && pivotFf.getDirection() != Direction.UP
								&& ffU.getDirection() != Direction.DOWN) {
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (ffR != null && pivotFf.getDirection() != Direction.RIGHT
								&& ffR.getDirection() != Direction.LEFT) {
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (ffD != null && pivotFf.getDirection() != Direction.DOWN
								&& ffD.getDirection() != Direction.UP) {
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (ffL != null && pivotFf.getDirection() != Direction.LEFT
								&& ffL.getDirection() != Direction.RIGHT) {
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					}
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
			fireflies = other.fireflies;
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
					if (fireflies[yIndex][xIndex] != null) {
						sb.append(fireflies[yIndex][xIndex]);
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
		 * ホタルのマスについてカーブ確定箇所を調査し、
		 * 確定の結果、それ以上カーブできない場合は直進する。
		 * カーブを使い切る前に壁やホタルに衝突したり、
		 * 直進の結果、壁にぶつかったりホタルの尻にぶつかった場合はfalseを返す。
		 */
		private boolean fireflySolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (fireflies[yIndex][xIndex] != null) {
						Firefly firefly = fireflies[yIndex][xIndex];
						int retainCurve = fireflies[yIndex][xIndex].getCount();
						Direction to = firefly.getDirection();
						Position pivot = new Position(yIndex, xIndex);
						// 今向いている方向に1マス前進する。
						if (to == Direction.UP) {
							pivot = new Position(pivot.getyIndex() - 1, pivot.getxIndex());
						} else if (to == Direction.RIGHT) {
							pivot = new Position(pivot.getyIndex(), pivot.getxIndex() + 1);
						} else if (to == Direction.DOWN) {
							pivot = new Position(pivot.getyIndex() + 1, pivot.getxIndex());
						} else if (to == Direction.LEFT) {
							pivot = new Position(pivot.getyIndex(), pivot.getxIndex() - 1);
						}
						while (true) {
							if (fireflies[pivot.getyIndex()][pivot.getxIndex()] != null) {
								break;
							}
							// 各方向の壁の存在状況
							// TODO いける方向かどうかをもうちょい再帰で判定させたい
							Wall wallU = pivot.getyIndex() == 0 ? Wall.EXISTS
									: tateWall[pivot.getyIndex() - 1][pivot.getxIndex()];
							Wall wallR = pivot.getxIndex() == getXLength() - 1 ? Wall.EXISTS
									: yokoWall[pivot.getyIndex()][pivot.getxIndex()];
							Wall wallD = pivot.getyIndex() == getYLength() - 1 ? Wall.EXISTS
									: tateWall[pivot.getyIndex()][pivot.getxIndex()];
							Wall wallL = pivot.getxIndex() == 0 ? Wall.EXISTS
									: yokoWall[pivot.getyIndex()][pivot.getxIndex() - 1];
							// 行ける条件
							// ・進行方向と逆方向でないこと
							// ・行く先に壁がないこと
							// ・カーブ回数が残ってないとき、カーブしないこと
							// ・進行方向の逆以外に確定解放壁がないこと
							// ・行く先がホタルの尻でないこと
							// ・カーブ回数が1回以上残っているとき、直進した行く先がホタルでないこと
							// ・カーブ回数が2回以上残っているとき、カーブした行く先がホタルでないこと
							boolean canU = (to != Direction.DOWN && wallU != Wall.EXISTS
									&& (to != Direction.UP || (wallR != Wall.NOT_EXISTS
											&& wallL != Wall.NOT_EXISTS))
									&& !(retainCurve == 0 && to != Direction.UP)
									&& !(fireflies[pivot.getyIndex() - 1][pivot.getxIndex()] != null
											&& fireflies[pivot.getyIndex() - 1][pivot.getxIndex()]
													.getDirection() == Direction.DOWN))
									&& !(retainCurve > 0 && to == Direction.UP
											&& fireflies[pivot.getyIndex() - 1][pivot.getxIndex()] != null)
									&& !(retainCurve > 1 && to != Direction.UP
											&& fireflies[pivot.getyIndex() - 1][pivot.getxIndex()] != null);
							boolean canR = (to != Direction.LEFT && wallR != Wall.EXISTS
									&& (to != Direction.RIGHT || (wallU != Wall.NOT_EXISTS
											&& wallD != Wall.NOT_EXISTS))
									&& !(retainCurve == 0 && to != Direction.RIGHT)
									&& !(fireflies[pivot.getyIndex()][pivot.getxIndex() + 1] != null
											&& fireflies[pivot.getyIndex()][pivot.getxIndex() + 1]
													.getDirection() == Direction.LEFT))
									&& !(retainCurve > 0 && to == Direction.RIGHT
											&& fireflies[pivot.getyIndex()][pivot.getxIndex() + 1] != null)
									&& !(retainCurve > 1 && to != Direction.RIGHT
											&& fireflies[pivot.getyIndex()][pivot.getxIndex() + 1] != null);
							boolean canD = (to != Direction.UP && wallD != Wall.EXISTS
									&& (to != Direction.DOWN || (wallR != Wall.NOT_EXISTS
											&& wallL != Wall.NOT_EXISTS))
									&& !(retainCurve == 0 && to != Direction.DOWN)
									&& !(fireflies[pivot.getyIndex() + 1][pivot.getxIndex()] != null
											&& fireflies[pivot.getyIndex() + 1][pivot.getxIndex()]
													.getDirection() == Direction.UP))
									&& !(retainCurve > 0 && to == Direction.DOWN
											&& fireflies[pivot.getyIndex() + 1][pivot.getxIndex()] != null)
									&& !(retainCurve > 1 && to != Direction.DOWN
											&& fireflies[pivot.getyIndex() + 1][pivot.getxIndex()] != null);
							boolean canL = (to != Direction.RIGHT && wallL != Wall.EXISTS
									&& (to != Direction.LEFT || (wallU != Wall.NOT_EXISTS
											&& wallD != Wall.NOT_EXISTS))
									&& !(retainCurve == 0 && to != Direction.LEFT)
									&& !(fireflies[pivot.getyIndex()][pivot.getxIndex() - 1] != null
											&& fireflies[pivot.getyIndex()][pivot.getxIndex() - 1]
													.getDirection() == Direction.RIGHT))
									&& !(retainCurve > 0 && to == Direction.LEFT
											&& fireflies[pivot.getyIndex()][pivot.getxIndex() - 1] != null)
									&& !(retainCurve > 1 && to != Direction.LEFT
											&& fireflies[pivot.getyIndex()][pivot.getxIndex() - 1] != null);
							if (!canU && !canR && !canD && !canL) {
								return false;
							} else if (canU && (to == Direction.UP && retainCurve == 0) || (!canR && !canD && !canL)) {
								tateWall[pivot.getyIndex() - 1][pivot.getxIndex()] = Wall.NOT_EXISTS;
								if (wallR == Wall.SPACE) {
									yokoWall[pivot.getyIndex()][pivot.getxIndex()] = Wall.EXISTS;
								}
								if (wallL == Wall.SPACE) {
									yokoWall[pivot.getyIndex()][pivot.getxIndex() - 1] = Wall.EXISTS;
								}
								pivot = new Position(pivot.getyIndex() - 1, pivot.getxIndex());
								if (to != Direction.UP) {
									if (retainCurve == 0) {
										return false;
									}
									retainCurve--;
									to = Direction.UP;
								}
							} else if (canR && (to == Direction.RIGHT && retainCurve == 0)
									|| (!canU && !canD && !canL)) {
								yokoWall[pivot.getyIndex()][pivot.getxIndex()] = Wall.NOT_EXISTS;
								if (wallU == Wall.SPACE) {
									tateWall[pivot.getyIndex() - 1][pivot.getxIndex()] = Wall.EXISTS;
								}
								if (wallD == Wall.SPACE) {
									tateWall[pivot.getyIndex()][pivot.getxIndex()] = Wall.EXISTS;
								}
								pivot = new Position(pivot.getyIndex(), pivot.getxIndex() + 1);
								if (to != Direction.RIGHT) {
									if (retainCurve == 0) {
										return false;
									}
									retainCurve--;
									to = Direction.RIGHT;
								}
							} else if (canD && (to == Direction.DOWN && retainCurve == 0)
									|| (!canU && !canR && !canL)) {
								tateWall[pivot.getyIndex()][pivot.getxIndex()] = Wall.NOT_EXISTS;
								if (wallR == Wall.SPACE) {
									yokoWall[pivot.getyIndex()][pivot.getxIndex()] = Wall.EXISTS;
								}
								if (wallL == Wall.SPACE) {
									yokoWall[pivot.getyIndex()][pivot.getxIndex() - 1] = Wall.EXISTS;
								}
								pivot = new Position(pivot.getyIndex() + 1, pivot.getxIndex());
								if (to != Direction.DOWN) {
									if (retainCurve == 0) {
										return false;
									}
									retainCurve--;
									to = Direction.DOWN;
								}
							} else if (canL && (to == Direction.LEFT && retainCurve == 0)
									|| (!canU && !canR && !canD)) {
								yokoWall[pivot.getyIndex()][pivot.getxIndex() - 1] = Wall.NOT_EXISTS;
								if (wallU == Wall.SPACE) {
									tateWall[pivot.getyIndex() - 1][pivot.getxIndex()] = Wall.EXISTS;
								}
								if (wallD == Wall.SPACE) {
									tateWall[pivot.getyIndex()][pivot.getxIndex()] = Wall.EXISTS;
								}
								pivot = new Position(pivot.getyIndex(), pivot.getxIndex() - 1);
								if (to != Direction.LEFT) {
									if (retainCurve == 0) {
										return false;
									}
									retainCurve--;
									to = Direction.LEFT;
								}
							} else {
								break;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * ホタルでないマスについて、黒マスの周囲の壁を埋める。
		 * また、白マスの壁の数が2にならない場合もfalseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (fireflies[yIndex][xIndex] == null) {
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
							} else if (masu[yIndex][xIndex] == Masu.SPACE) {
								// 自分が不確定マスなら壁は2マスか4マス
								if ((existsCount == 3 && notExistsCount == 1)
										|| notExistsCount > 2) {
									return false;
								}
								if (existsCount > 3) {
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
		 * ホタルビームから孤立したマスを黒にする。
		 */
		public boolean connectSolve2() {
			Set<Position> continuePosSet = new HashSet<>();
			Set<Position> allPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					allPosSet.add(pos);
					if (fireflies[yIndex][xIndex] != null) {
						continuePosSet.add(pos);
						setFireFlyContinuePosSet(pos, continuePosSet, null);
					}
				}
			}
			allPosSet.removeAll(continuePosSet);
			for (Position pos : allPosSet) {
				if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
					return false;
				}
				masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスをつなげていく。
		 * ホタルのマスは尻の方向にしか向かないようにする。
		 */
		private void setFireFlyContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP && !(fireflies[pos.getyIndex()][pos.getxIndex()] != null
					&& fireflies[pos.getyIndex()][pos.getxIndex()].getDirection() != Direction.UP)) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setFireFlyContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT
					&& !(fireflies[pos.getyIndex()][pos.getxIndex()] != null
							&& fireflies[pos.getyIndex()][pos.getxIndex()].getDirection() != Direction.RIGHT)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setFireFlyContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN
					&& !(fireflies[pos.getyIndex()][pos.getxIndex()] != null
							&& fireflies[pos.getyIndex()][pos.getxIndex()].getDirection() != Direction.DOWN)) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setFireFlyContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT && !(fireflies[pos.getyIndex()][pos.getxIndex()] != null
					&& fireflies[pos.getyIndex()][pos.getxIndex()].getDirection() != Direction.LEFT)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setFireFlyContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!fireflySolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
				if (!connectSolve2()) {
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

	public FireflySolver(int height, int width, String param) {
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
		System.out.println(new FireflySolver(height, width, param).solve());
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
		//System.out.println(field);
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
				if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
					Masu masuLeft = field.masu[yIndex][xIndex];
					Masu masuRight = field.masu[yIndex][xIndex + 1];
					if (masuLeft == Masu.SPACE && masuRight == Masu.SPACE) {
						continue;
					}
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
					Masu masuUp = field.masu[yIndex][xIndex];
					Masu masuDown = field.masu[yIndex + 1][xIndex];
					if (masuUp == Masu.SPACE && masuDown == Masu.SPACE) {
						continue;
					}
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
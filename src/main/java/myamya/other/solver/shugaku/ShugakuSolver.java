package myamya.other.solver.shugaku;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class ShugakuSolver implements Solver {

	public static class Field {
		// マスの情報
		private Masu[][] masu;
		// 枕の情報。黒なら枕あり
		private Masu[][] makura;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;
		// 数字の情報
		private final Integer[][] numbers;

		public Masu[][] getMasu() {
			return masu;
		}

		public Masu[][] getMakura() {
			return makura;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			makura = new Masu[height][width];
			numbers = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					makura[yIndex][xIndex] = Masu.SPACE;
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
				int interval = Character.getNumericValue(ch) - 5;
				if (interval > 0) {
					index = index + interval;
				} else {
					int capacity = Integer.parseInt(String.valueOf(ch), 16);
					Position pos = new Position(index / getXLength(), index % getXLength());
					// 柱マスは周囲の壁＆枕なしが確定
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
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
					makura[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					index++;
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			masu = new Masu[other.getYLength()][other.getXLength()];
			makura = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					makura[yIndex][xIndex] = other.makura[yIndex][xIndex];
				}
			}
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
		}

		private static final String FULL_NUMS = "０１２３４５６７８９";

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
						if (numbers[yIndex][xIndex] == 5) {
							sb.append("○");
						} else {
							sb.append(FULL_NUMS.substring(numbers[yIndex][xIndex],
									numbers[yIndex][xIndex] + 1));
						}
					} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK && makura[yIndex][xIndex] == Masu.BLACK) {
						sb.append("枕");
					} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK && makura[yIndex][xIndex] == Masu.NOT_BLACK) {
						sb.append("ふ");
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
					sb.append(makura[yIndex][xIndex]);
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
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!wallSolve()) {
				return false;
			}
			if (!blackWhiteSolve()) {
				return false;
			}
			if (!poleSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
			if (!connectSolve()) {
				return false;
			}
			if (!aisleSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 枕があるマスは白であり、黒マスは枕がない。
		 * 違反した場合falseを返す。
		 */
		private boolean blackWhiteSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							if (makura[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							makura[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (makura[yIndex][xIndex] == Masu.BLACK) {
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 柱の数字は枕の数を表す。条件を満たさない場合falseを返す。
		 */
		private boolean poleSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != 5) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu makuraUp = yIndex == 0 ? Masu.NOT_BLACK
								: makura[yIndex - 1][xIndex];
						Masu makuraRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK
								: makura[yIndex][xIndex + 1];
						Masu makuraDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK
								: makura[yIndex + 1][xIndex];
						Masu makuraLeft = xIndex == 0 ? Masu.NOT_BLACK
								: makura[yIndex][xIndex - 1];
						if (makuraUp == Masu.BLACK) {
							blackCnt++;
						} else if (makuraUp == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (makuraRight == Masu.BLACK) {
							blackCnt++;
						} else if (makuraRight == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (makuraDown == Masu.BLACK) {
							blackCnt++;
						} else if (makuraDown == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (makuraLeft == Masu.BLACK) {
							blackCnt++;
						} else if (makuraLeft == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (numbers[yIndex][xIndex] < blackCnt) {
							// 枕過剰
							return false;
						}
						if (numbers[yIndex][xIndex] == blackCnt) {
							if (makuraUp == Masu.SPACE) {
								makura[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (makuraRight == Masu.SPACE) {
								makura[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							}
							if (makuraDown == Masu.SPACE) {
								makura[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							}
							if (makuraLeft == Masu.SPACE) {
								makura[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
						if (numbers[yIndex][xIndex] > 4 - whiteCnt) {
							// 枕不足
							return false;
						}
						if (numbers[yIndex][xIndex] == 4 - whiteCnt) {
							if (makuraUp == Masu.SPACE) {
								makura[yIndex - 1][xIndex] = Masu.BLACK;
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (makuraRight == Masu.SPACE) {
								makura[yIndex][xIndex + 1] = Masu.BLACK;
								masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							}
							if (makuraDown == Masu.SPACE) {
								makura[yIndex + 1][xIndex] = Masu.BLACK;
								masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							}
							if (makuraLeft == Masu.SPACE) {
								makura[yIndex][xIndex - 1] = Masu.BLACK;
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 置くと池ができるマスを白マスにする。
		 */
		private boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE && masu4 == Masu.BLACK) {
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
				}
			}
			return true;
		}

		/**
		 * 数字マスでない白マスは必ず壁の数が3個に、黒マスは壁の数が4枚になる。
		 * かつ、枕のある白マスは必ず下の壁が閉じる。違反した場合はfalseを返す。
		 */
		private boolean wallSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						int exists = 0;
						int notExists = 0;
						Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
						if (wallUp == Wall.EXISTS) {
							exists++;
						} else if (wallUp == Wall.NOT_EXISTS) {
							notExists++;
						}
						Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
						if (wallRight == Wall.EXISTS) {
							exists++;
						} else if (wallRight == Wall.NOT_EXISTS) {
							notExists++;
						}
						Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
						if (wallDown == Wall.EXISTS) {
							exists++;
						} else if (wallDown == Wall.NOT_EXISTS) {
							notExists++;
						}
						Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
						if (wallLeft == Wall.EXISTS) {
							exists++;
						} else if (wallLeft == Wall.NOT_EXISTS) {
							notExists++;
						}
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if (notExists > 1) {
								return false;
							} else if (notExists > 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							} else if (exists == 4) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							if (notExists > 1 || exists == 4) {
								return false;
							}
							if (wallDown == Wall.NOT_EXISTS && makura[yIndex][xIndex] == Masu.BLACK) {
								return false;
							}
							if (wallDown == Wall.SPACE && makura[yIndex][xIndex] == Masu.BLACK) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (notExists == 1) {
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
							if (notExists == 0 && exists == 3) {
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
						} else if (masu[yIndex][xIndex] == Masu.BLACK) {
							if (notExists > 0) {
								return false;
							} else if (exists != 4) {
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
					}
				}
			}
			return true;
		}

		/**
		 * 壁で区切られた白マスのサイズが2の場合は、そのマスの周囲に黒マスが最低1つ必要である。
		 * また、枕が1つだけ必要である。違反した場合falseを返す。
		 */
		private boolean aisleSolve() {
			Set<Position> resolvedWhitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null && masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (!resolvedWhitePosSet.contains(whitePos)) {
							Set<Position> whitePosSet = new HashSet<>();
							whitePosSet.add(whitePos);
							if (!setContinueWhitePosSet(whitePos, whitePosSet, null)) {
								if (whitePosSet.size() > 2) {
									// 3以上のサイズの部屋禁止
									return false;
								}
							} else {
								// 部屋が確定
								for (Position pos : whitePosSet) {
									masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
								}
								int spaceCnt = 0;
								for (Position pos : whitePosSet) {
									Masu masuUp = pos.getyIndex() == 0 ? Masu.NOT_BLACK
											: masu[pos.getyIndex() - 1][pos.getxIndex()];
									Masu masuRight = pos.getxIndex() == getXLength() - 1 ? Masu.NOT_BLACK
											: masu[pos.getyIndex()][pos.getxIndex() + 1];
									Masu masuDown = pos.getyIndex() == getYLength() - 1 ? Masu.NOT_BLACK
											: masu[pos.getyIndex() + 1][pos.getxIndex()];
									Masu masuLeft = pos.getxIndex() == 0 ? Masu.NOT_BLACK
											: masu[pos.getyIndex()][pos.getxIndex() - 1];
									if (masuUp == Masu.BLACK) {
										spaceCnt = -1;
										break;
									} else if (masuUp == Masu.SPACE) {
										spaceCnt++;
									}
									if (masuRight == Masu.BLACK) {
										spaceCnt = -1;
										break;
									} else if (masuRight == Masu.SPACE) {
										spaceCnt++;
									}
									if (masuDown == Masu.BLACK) {
										spaceCnt = -1;
										break;
									} else if (masuDown == Masu.SPACE) {
										spaceCnt++;
									}
									if (masuLeft == Masu.BLACK) {
										spaceCnt = -1;
										break;
									} else if (masuLeft == Masu.SPACE) {
										spaceCnt++;
									}
								}
								if (spaceCnt == 0) {
									// 部屋に黒マスが接しない
									return false;
								}
								if (spaceCnt == 1) {
									// 部屋に接する白マスでないマスが1マスのみならそこは黒マス。
									for (Position pos : whitePosSet) {
										Masu masuUp = pos.getyIndex() == 0 ? Masu.NOT_BLACK
												: masu[pos.getyIndex() - 1][pos.getxIndex()];
										Masu masuRight = pos.getxIndex() == getXLength() - 1 ? Masu.NOT_BLACK
												: masu[pos.getyIndex()][pos.getxIndex() + 1];
										Masu masuDown = pos.getyIndex() == getYLength() - 1 ? Masu.NOT_BLACK
												: masu[pos.getyIndex() + 1][pos.getxIndex()];
										Masu masuLeft = pos.getxIndex() == 0 ? Masu.NOT_BLACK
												: masu[pos.getyIndex()][pos.getxIndex() - 1];
										if (masuUp == Masu.SPACE) {
											masu[pos.getyIndex() - 1][pos.getxIndex()] = Masu.BLACK;
											break;
										}
										if (masuRight == Masu.SPACE) {
											masu[pos.getyIndex()][pos.getxIndex() + 1] = Masu.BLACK;
											break;
										}
										if (masuDown == Masu.SPACE) {
											masu[pos.getyIndex() + 1][pos.getxIndex()] = Masu.BLACK;
											break;
										}
										if (masuLeft == Masu.SPACE) {
											masu[pos.getyIndex()][pos.getxIndex() - 1] = Masu.BLACK;
											break;
										}
									}
								}
								int makuraBlackCnt = 0;
								int makuraWhiteCnt = 0;
								for (Position pos : whitePosSet) {
									if (makura[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
										makuraBlackCnt++;
									} else if (makura[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
										makuraWhiteCnt++;
									}
								}
								if (makuraBlackCnt == 2 || makuraWhiteCnt == 2) {
									// 枕が0個 or 2個
									return false;
								}
								// 枕あり確定の場合、もう一方は枕なし
								if (makuraBlackCnt == 1 && makuraWhiteCnt == 0) {
									for (Position pos : whitePosSet) {
										if (makura[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
											makura[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
											break;
										}
									}
								}
								// 枕なし確定の場合、もう一方は枕あり
								if (makuraWhiteCnt == 1 && makuraBlackCnt == 0) {
									for (Position pos : whitePosSet) {
										if (makura[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
											makura[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
											break;
										}
									}
								}
							}
							resolvedWhitePosSet.addAll(whitePosSet);
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右の壁なし確定のマスをつなぐ。サイズが2の場合のみtrueを返す。
		 */
		private boolean setContinueWhitePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS) {
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet(nextPos, continuePosSet, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet(nextPos, continuePosSet, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet(nextPos, continuePosSet, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS) {
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet(nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return (continuePosSet.size() == 2);
		}

		/**
		 * 黒マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position blackPos = new Position(yIndex, xIndex);
						if (blackPosSet.size() == 0) {
							blackPosSet.add(blackPos);
							setContinuePosSet(blackPos, blackPosSet, null);
						} else {
							if (!blackPosSet.contains(blackPos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に白確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE || makura[yIndex][xIndex] == Masu.SPACE) {
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
			return "Room [capacity=" + capacity + ", pivot=" + pivot + "]";
		}

		// 白マスが何マスあるか。
		private final int capacity;
		// 部屋に属するマスの集合
		private final Position pivot;

		public Room(int capacity, Position pivot) {
			this.capacity = capacity;
			this.pivot = pivot;
		}

		public int getCapacity() {
			return capacity;
		}

		public Position getPivot() {
			return pivot;
		}

	}

	private final Field field;
	private int count;

	public ShugakuSolver(int height, int width, String param) {
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
		System.out.println(new ShugakuSolver(height, width, param).solve());
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
				if (!candSolve(field, recursiveCnt)) {
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
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
				if (field.masu[yIndex][xIndex] == Masu.NOT_BLACK && field.makura[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandMakuraSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		String newStr = field.getStateDump();
		if (!newStr.equals(str)) {
			return candSolve(field, recursive);
		}
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
		if (!field.getStateDump().equals(newStr)) {
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
			field.makura = virtual2.makura;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.makura = virtual.makura;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

	/**
	 * 1つの枕に対する仮置き調査
	 */
	private boolean oneCandMakuraSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.makura[yIndex][xIndex] = Masu.BLACK;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.makura[yIndex][xIndex] = Masu.NOT_BLACK;
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
			field.makura = virtual2.makura;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.makura = virtual.makura;
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
			field.makura = virtual2.makura;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.makura = virtual.makura;
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
			field.makura = virtual2.makura;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.makura = virtual.makura;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

}
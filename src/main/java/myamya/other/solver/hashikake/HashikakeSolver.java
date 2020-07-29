package myamya.other.solver.hashikake;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class HashikakeSolver implements Solver {

	public static class Field {
		// マスの情報
		private Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;
		// 横をふさぐ壁がNOTEXISTSの時の橋の太さ。未確定は-1。
		private Integer[][] yokoWallGate;
		// 縦をふさぐ壁がNOTEXISTSの時の橋の太さ。未確定は-1。
		private Integer[][] tateWallGate;

		// 数字の情報
		private final Integer[][] numbers;

		public Masu[][] getMasu() {
			return masu;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public Integer[][] getYokoWallGate() {
			return yokoWallGate;
		}

		public Integer[][] getTateWallGate() {
			return tateWallGate;
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
			numbers = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			yokoWallGate = new Integer[height][width - 1];
			tateWallGate = new Integer[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.SPACE;
					yokoWallGate[yIndex][xIndex] = 0;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.SPACE;
					tateWallGate[yIndex][xIndex] = 0;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = Character.getNumericValue(ch) - 15;
				if (interval > 0) {
					index = index + interval;
				} else {
					Position pos = new Position(index / getXLength(), index % getXLength());
					if (ch == '.') {
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else {
						int capacity = Integer.parseInt(String.valueOf(ch), 16);
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					if (pos.getyIndex() != 0) {
						tateWallGate[pos.getyIndex() - 1][pos.getxIndex()] = -1;
					}
					if (pos.getxIndex() != getXLength() - 1) {
						yokoWallGate[pos.getyIndex()][pos.getxIndex()] = -1;
					}
					if (pos.getyIndex() != getYLength() - 1) {
						tateWallGate[pos.getyIndex()][pos.getxIndex()] = -1;
					}
					if (pos.getxIndex() != 0) {
						yokoWallGate[pos.getyIndex()][pos.getxIndex() - 1] = -1;
					}
					index++;
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			yokoWallGate = new Integer[other.getYLength()][other.getXLength() - 1];
			tateWallGate = new Integer[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = other.yokoWall[yIndex][xIndex];
					yokoWallGate[yIndex][xIndex] = other.yokoWallGate[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
					tateWallGate[yIndex][xIndex] = other.tateWallGate[yIndex][xIndex];
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
						if (numbers[yIndex][xIndex] == -1) {
							sb.append("？");
						} else {
							sb.append(FULL_NUMS.substring(numbers[yIndex][xIndex],
									numbers[yIndex][xIndex] + 1));
						}
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
					if (xIndex != getXLength() - 1) {
						if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							if (yokoWallGate[yIndex][xIndex] == 2) {
								sb.append("②");
							} else if (yokoWallGate[yIndex][xIndex] == 1) {
								sb.append("①");
							} else if (yokoWallGate[yIndex][xIndex] == -1) {
								sb.append("？");
							} else {
								sb.append(yokoWall[yIndex][xIndex]);
							}
						} else {
							sb.append(yokoWall[yIndex][xIndex]);
						}
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							if (tateWallGate[yIndex][xIndex] == 2) {
								sb.append("②");
							} else if (tateWallGate[yIndex][xIndex] == 1) {
								sb.append("①");
							} else if (tateWallGate[yIndex][xIndex] == -1) {
								sb.append("？");
							} else {
								sb.append(tateWall[yIndex][xIndex]);
							}
						} else {
							sb.append(tateWall[yIndex][xIndex]);
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
					sb.append(yokoWallGate[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateWall[yIndex][xIndex]);
					sb.append(tateWallGate[yIndex][xIndex]);
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
			if (!gateSolve()) {
				return false;
			}
			if (!isleSolve()) {
				return false;
			}
			if (!bridgeSolve()) {
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

		/**
		 * ゲートがあれば壁なし、壁があればゲートなし。
		 */
		private boolean gateSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.EXISTS) {
						if (yokoWallGate[yIndex][xIndex] > 0) {
							return false;
						}
						yokoWallGate[yIndex][xIndex] = 0;
					}
					if (yokoWallGate[yIndex][xIndex] > 0) {
						if (yokoWall[yIndex][xIndex] == Wall.EXISTS) {
							return false;
						}
						yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.EXISTS) {
						if (tateWallGate[yIndex][xIndex] > 0) {
							return false;
						}
						tateWallGate[yIndex][xIndex] = 0;
					}
					if (tateWallGate[yIndex][xIndex] > 0) {
						if (tateWall[yIndex][xIndex] == Wall.EXISTS) {
							return false;
						}
						tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
					}
				}
			}
			return true;
		}

		/**
		 * 数字は周りのゲートの合計値である。誤っている場合falseを返す。
		 * TODO 強化余地あり。2つはかからないが最低1本はかけるとかの判定を入れたい
		 */
		private boolean isleSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						int maxUp, minUp;
						if (yIndex == 0
								|| tateWall[yIndex - 1][xIndex] == Wall.EXISTS) {
							maxUp = 0;
							minUp = 0;
						} else if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
							if (tateWallGate[yIndex - 1][xIndex] == 2) {
								maxUp = 2;
								minUp = 2;
							} else if (tateWallGate[yIndex - 1][xIndex] == 1) {
								maxUp = 1;
								minUp = 1;
							} else {
								maxUp = 2;
								minUp = 1;
							}
						} else {
							if (tateWallGate[yIndex - 1][xIndex] == 1) {
								maxUp = 1;
								minUp = 0;
							} else {
								maxUp = 2;
								minUp = 0;
							}
						}
						int maxRight, minRight;
						if (xIndex == getXLength() - 1
								|| yokoWall[yIndex][xIndex] == Wall.EXISTS) {
							maxRight = 0;
							minRight = 0;
						} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							if (yokoWallGate[yIndex][xIndex] == 2) {
								maxRight = 2;
								minRight = 2;
							} else if (yokoWallGate[yIndex][xIndex] == 1) {
								maxRight = 1;
								minRight = 1;
							} else {
								maxRight = 2;
								minRight = 1;
							}
						} else {
							if (yokoWallGate[yIndex][xIndex] == 1) {
								maxRight = 1;
								minRight = 0;
							} else {
								maxRight = 2;
								minRight = 0;
							}
						}
						int maxDown, minDown;
						if (yIndex == getYLength() - 1
								|| tateWall[yIndex][xIndex] == Wall.EXISTS) {
							maxDown = 0;
							minDown = 0;
						} else if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							if (tateWallGate[yIndex][xIndex] == 2) {
								maxDown = 2;
								minDown = 2;
							} else if (tateWallGate[yIndex][xIndex] == 1) {
								maxDown = 1;
								minDown = 1;
							} else {
								maxDown = 2;
								minDown = 1;
							}
						} else {
							if (tateWallGate[yIndex][xIndex] == 1) {
								maxDown = 1;
								minDown = 0;
							} else {
								maxDown = 2;
								minDown = 0;
							}
						}
						int maxLeft, minLeft;
						if (xIndex == 0
								|| yokoWall[yIndex][xIndex - 1] == Wall.EXISTS) {
							maxLeft = 0;
							minLeft = 0;
						} else if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
							if (yokoWallGate[yIndex][xIndex - 1] == 2) {
								maxLeft = 2;
								minLeft = 2;
							} else if (yokoWallGate[yIndex][xIndex - 1] == 1) {
								maxLeft = 1;
								minLeft = 1;
							} else {
								maxLeft = 2;
								minLeft = 1;
							}
						} else {
							if (yokoWallGate[yIndex][xIndex - 1] == 1) {
								maxLeft = 1;
								minLeft = 0;
							} else {
								maxLeft = 2;
								minLeft = 0;
							}
						}
						int maxAll = maxUp + maxRight + maxDown + maxLeft;
						int minAll = minUp + minRight + minDown + minLeft;
						if (numbers[yIndex][xIndex] < minAll) {
							// 橋多すぎ
							return false;
						} else if (numbers[yIndex][xIndex] > maxAll) {
							// 橋少なすぎ
							return false;
						} else if (minAll == maxAll) {
							// もう現状でOK
							continue;
						} else if (numbers[yIndex][xIndex] == maxAll) {
							// 限界まで橋をかける
							if (yIndex != 0) {
								if (tateWall[yIndex - 1][xIndex] == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS
										&& tateWallGate[yIndex - 1][xIndex] == -1) {
									tateWallGate[yIndex - 1][xIndex] = 2;
								}
							}
							if (xIndex != getXLength() - 1) {
								if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS
										&& yokoWallGate[yIndex][xIndex] == -1) {
									yokoWallGate[yIndex][xIndex] = 2;
								}
							}
							if (yIndex != getYLength() - 1) {
								if (tateWall[yIndex][xIndex] == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS
										&& tateWallGate[yIndex][xIndex] == -1) {
									tateWallGate[yIndex][xIndex] = 2;
								}
							}
							if (xIndex != 0) {
								if (yokoWall[yIndex][xIndex - 1] == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
								}
								if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS
										&& yokoWallGate[yIndex][xIndex - 1] == -1) {
									yokoWallGate[yIndex][xIndex - 1] = 2;
								}
							}
						} else if (numbers[yIndex][xIndex] == maxAll - 1) {
							// 限界より一つ小さい数字の場合、とりあえず橋がかけられるところにかけることは確定
							if (yIndex != 0) {
								if (tateWall[yIndex - 1][xIndex] == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (xIndex != getXLength() - 1) {
								if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (yIndex != getYLength() - 1) {
								if (tateWall[yIndex][xIndex] == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (xIndex != 0) {
								if (yokoWall[yIndex][xIndex - 1] == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
								}
							}
						} else if (numbers[yIndex][xIndex] == minAll) {
							// 限界まで橋をかけない
							if (yIndex != 0) {
								if (tateWall[yIndex - 1][xIndex] == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
								}
								if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS
										&& tateWallGate[yIndex - 1][xIndex] == -1) {
									tateWallGate[yIndex - 1][xIndex] = 1;
								}
							}
							if (xIndex != getXLength() - 1) {
								if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS
										&& yokoWallGate[yIndex][xIndex] == -1) {
									yokoWallGate[yIndex][xIndex] = 1;
								}
							}
							if (yIndex != getYLength() - 1) {
								if (tateWall[yIndex][xIndex] == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS
										&& tateWallGate[yIndex][xIndex] == -1) {
									tateWallGate[yIndex][xIndex] = 1;
								}
							}
							if (xIndex != 0) {
								if (yokoWall[yIndex][xIndex - 1] == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
								}
								if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS &&
										yokoWallGate[yIndex][xIndex - 1] == -1) {
									yokoWallGate[yIndex][xIndex - 1] = 1;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字マスでない白マスは必ず壁の数が2個(直進)に、黒マスは必ず壁の数が4個になる。
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
							if (notExists > 2 || (notExists > 0 && exists > 2)) {
								return false;
							} else if (notExists > 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							} else if (exists > 2) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							if (notExists > 2 || exists > 2) {
								return false;
							}
							if ((wallUp == Wall.EXISTS && wallRight == Wall.EXISTS)
									|| (wallUp == Wall.EXISTS && wallLeft == Wall.EXISTS)
									|| (wallRight == Wall.EXISTS && wallDown == Wall.EXISTS)
									|| (wallDown == Wall.EXISTS && wallLeft == Wall.EXISTS)) {
								return false;
							}
							// TODO 直進判定系のロジックだとこれが一番優秀かな
							// 他のソルバーにも移植する
							if (wallUp == Wall.EXISTS || wallDown == Wall.EXISTS ||
									wallRight == Wall.NOT_EXISTS || wallLeft == Wall.NOT_EXISTS) {
								if (wallUp == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
								}
							}
							if (wallRight == Wall.EXISTS || wallLeft == Wall.EXISTS ||
									wallUp == Wall.NOT_EXISTS || wallDown == Wall.NOT_EXISTS) {
								if (wallUp == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
								}
							}
							if (exists == 2) {
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
							if (notExists == 2) {
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
		 * 数字マスで繋がるゲートの大きさは同じになる。
		 */
		private boolean bridgeSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (yIndex != 0
								&& tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
							for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
								if (numbers[targetY][xIndex] != null) {
									if ((tateWallGate[yIndex - 1][xIndex] == 2 && tateWallGate[targetY][xIndex] == 1)
											|| (tateWallGate[yIndex - 1][xIndex] == 1
													&& tateWallGate[targetY][xIndex] == 2)) {
										return false;
									}
									if (tateWallGate[yIndex - 1][xIndex] == 2) {
										tateWallGate[targetY][xIndex] = 2;
									} else if (tateWallGate[yIndex - 1][xIndex] == 1) {
										tateWallGate[targetY][xIndex] = 1;
									} else if (tateWallGate[targetY][xIndex] == 2) {
										tateWallGate[yIndex - 1][xIndex] = 2;
									} else if (tateWallGate[targetY][xIndex] == 1) {
										tateWallGate[yIndex - 1][xIndex] = 1;
									}
									break;
								}
							}
						}
						if (yIndex != getYLength() - 1
								&& tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
								if (numbers[targetY][xIndex] != null) {
									if ((tateWallGate[yIndex][xIndex] == 2 && tateWallGate[targetY - 1][xIndex] == 1)
											|| (tateWallGate[yIndex][xIndex] == 1
													&& tateWallGate[targetY - 1][xIndex] == 2)) {
										return false;
									}
									if (tateWallGate[yIndex][xIndex] == 2) {
										tateWallGate[targetY - 1][xIndex] = 2;
									} else if (tateWallGate[yIndex][xIndex] == 1) {
										tateWallGate[targetY - 1][xIndex] = 1;
									} else if (tateWallGate[targetY - 1][xIndex] == 2) {
										tateWallGate[yIndex][xIndex] = 2;
									} else if (tateWallGate[targetY - 1][xIndex] == 1) {
										tateWallGate[yIndex][xIndex] = 1;
									}
									break;
								}
							}
						}
						if (xIndex != getXLength() - 1
								&& yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
								if (numbers[yIndex][targetX] != null) {
									if ((yokoWallGate[yIndex][xIndex] == 2 && yokoWallGate[yIndex][targetX - 1] == 1)
											|| (yokoWallGate[yIndex][xIndex] == 1
													&& yokoWallGate[yIndex][targetX - 1] == 2)) {
										return false;
									}
									if (yokoWallGate[yIndex][xIndex] == 2) {
										yokoWallGate[yIndex][targetX - 1] = 2;
									} else if (yokoWallGate[yIndex][xIndex] == 1) {
										yokoWallGate[yIndex][targetX - 1] = 1;
									} else if (yokoWallGate[yIndex][targetX - 1] == 2) {
										yokoWallGate[yIndex][xIndex] = 2;
									} else if (yokoWallGate[yIndex][targetX - 1] == 1) {
										yokoWallGate[yIndex][xIndex] = 1;
									}
									break;
								}
							}
						}
						if (xIndex != 0
								&& yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
							for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
								if (numbers[yIndex][targetX] != null) {
									if ((yokoWallGate[yIndex][xIndex - 1] == 2 && yokoWallGate[yIndex][targetX] == 1)
											|| (yokoWallGate[yIndex][xIndex - 1] == 1
													&& yokoWallGate[yIndex][targetX] == 2)) {
										return false;
									}
									if (yokoWallGate[yIndex][xIndex - 1] == 2) {
										yokoWallGate[yIndex][targetX] = 2;
									} else if (yokoWallGate[yIndex][xIndex - 1] == 1) {
										yokoWallGate[yIndex][targetX] = 1;
									} else if (yokoWallGate[yIndex][targetX] == 2) {
										yokoWallGate[yIndex][xIndex - 1] = 2;
									} else if (yokoWallGate[yIndex][targetX] == 1) {
										yokoWallGate[yIndex][xIndex - 1] = 1;
									}
									break;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
							whitePosSet.add(whitePos);
							setContinuePosSet(whitePos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(whitePos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
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
					if (yokoWall[yIndex][xIndex] == Wall.SPACE || yokoWallGate[yIndex][xIndex] == -1) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE || tateWallGate[yIndex][xIndex] == -1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count;

	public HashikakeSolver(int height, int width, String param) {
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
		System.out.println(new HashikakeSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				System.out.println(field);
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				System.out.println(field);
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count * 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 5).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		//				 System.out.println(field);
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
				if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
				if (field.yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS && field.yokoWallGate[yIndex][xIndex] == -1) {
					count++;
					if (!oneCandYokoWallGateSolve(field, yIndex, xIndex, recursive)) {
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
				if (field.tateWall[yIndex][xIndex] == Wall.NOT_EXISTS && field.tateWallGate[yIndex][xIndex] == -1) {
					count++;
					if (!oneCandTateWallGateSolve(field, yIndex, xIndex, recursive)) {
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
			field.tateWallGate = virtual2.tateWallGate;
			field.yokoWallGate = virtual2.yokoWallGate;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
			field.tateWallGate = virtual.tateWallGate;
			field.yokoWallGate = virtual.yokoWallGate;
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
			field.tateWallGate = virtual2.tateWallGate;
			field.yokoWallGate = virtual2.yokoWallGate;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
			field.tateWallGate = virtual.tateWallGate;
			field.yokoWallGate = virtual.yokoWallGate;
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
			field.tateWallGate = virtual2.tateWallGate;
			field.yokoWallGate = virtual2.yokoWallGate;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
			field.tateWallGate = virtual.tateWallGate;
			field.yokoWallGate = virtual.yokoWallGate;
		}
		return true;
	}

	private boolean oneCandYokoWallGateSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.yokoWallGate[yIndex][xIndex] = 2;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoWallGate[yIndex][xIndex] = 1;
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
			field.tateWallGate = virtual2.tateWallGate;
			field.yokoWallGate = virtual2.yokoWallGate;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
			field.tateWallGate = virtual.tateWallGate;
			field.yokoWallGate = virtual.yokoWallGate;
		}
		return true;
	}

	private boolean oneCandTateWallGateSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateWallGate[yIndex][xIndex] = 2;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateWallGate[yIndex][xIndex] = 1;
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
			field.tateWallGate = virtual2.tateWallGate;
			field.yokoWallGate = virtual2.yokoWallGate;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
			field.tateWallGate = virtual.tateWallGate;
			field.yokoWallGate = virtual.yokoWallGate;
		}
		return true;
	}
}
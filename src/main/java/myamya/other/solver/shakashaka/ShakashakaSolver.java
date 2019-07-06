package myamya.other.solver.shakashaka;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class ShakashakaSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private Integer[][] numbers;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[][] getNumbers() {
			return numbers;
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
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == '.') {
					// 数字なし黒マス。周囲の壁が確定
					numbers[pos.getyIndex()][pos.getxIndex()] = -1;
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
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						// 数字あり黒マス。周囲の壁が確定
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							numbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
						}
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
						index++;
					}
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			numbers = other.numbers;
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
						if (numbers[yIndex][xIndex] == -1) {
							sb.append("○");
						} else {
							sb.append(FULL_NUMS.substring(numbers[yIndex][xIndex],
									numbers[yIndex][xIndex] + 1));
						}
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
		 * 各種チェックを1セット実行
		 * @param recursive
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
			if (!rectSolve()) {
				return false;
			}
			if (!wallSolve()) {
				return false;
			}
			if (!whiteWallSolve()) {
				return false;
			}
			if (!finalSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * つながった黒マスは長方形になるように配置する。
		 * 長方形にできなかった場合はfalseを返す。
		 */
		public boolean rectSolve() {
			boolean advance = false;
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.NOT_BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.NOT_BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.SPACE) {
						advance = true;
						masu[yIndex + 1][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE && masu4 == Masu.BLACK) {
						advance = true;
						masu[yIndex + 1][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						advance = true;
						masu[yIndex][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						advance = true;
						masu[yIndex][xIndex] = Masu.BLACK;
					}
				}
			}
			if (advance) {
				return rectSolve();
			}
			return true;
		}

		/**
		 * 柱から見て周りのマスがすべて白マスの場合、柱から伸びる壁は1にならない。
		 * 矛盾する場合falseを返す。
		 */
		private boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
						int exists = 0;
						int notExists = 0;
						Wall wall1 = tateWall[yIndex][xIndex];
						Wall wall2 = tateWall[yIndex][xIndex + 1];
						Wall wall3 = yokoWall[yIndex][xIndex];
						Wall wall4 = yokoWall[yIndex + 1][xIndex];
						if (wall1 == Wall.EXISTS) {
							exists++;
						} else if (wall1 == Wall.NOT_EXISTS) {
							notExists++;
						}
						if (wall2 == Wall.EXISTS) {
							exists++;
						} else if (wall2 == Wall.NOT_EXISTS) {
							notExists++;
						}
						if (wall3 == Wall.EXISTS) {
							exists++;
						} else if (wall3 == Wall.NOT_EXISTS) {
							notExists++;
						}
						if (wall4 == Wall.EXISTS) {
							exists++;
						} else if (wall4 == Wall.NOT_EXISTS) {
							notExists++;
						}
						if (exists == 1 && notExists == 3) {
							return false;
						}
						if (notExists == 3) {
							if (wall1 == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wall2 == Wall.SPACE) {
								tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
							if (wall3 == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wall4 == Wall.SPACE) {
								yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
							}
						}
						if (notExists == 2 && exists == 1) {
							if (wall1 == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wall2 == Wall.SPACE) {
								tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
							}
							if (wall3 == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wall4 == Wall.SPACE) {
								yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスと黒マスの間は必ず壁で隔て、黒マスと黒マスの間は必ず壁を隔てない。
		 * 違反した場合falseを返す。
		 */
		private boolean wallSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						Masu myMasu = masu[yIndex][xIndex];
						Masu masuUp = yIndex == 0 ? Masu.SPACE : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.SPACE : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.SPACE : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.SPACE : masu[yIndex][xIndex - 1];
						if ((myMasu == Masu.NOT_BLACK && masuUp == Masu.BLACK) ||
								(myMasu == Masu.BLACK && masuUp == Masu.NOT_BLACK)) {
							if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (myMasu == Masu.BLACK && masuUp == Masu.BLACK) {
							if (tateWall[yIndex - 1][xIndex] == Wall.EXISTS) {
								return false;
							}
							tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
						}

						if ((myMasu == Masu.NOT_BLACK && masuRight == Masu.BLACK) ||
								(myMasu == Masu.BLACK && masuRight == Masu.NOT_BLACK)) {
							if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (myMasu == Masu.BLACK && masuRight == Masu.BLACK) {
							if (yokoWall[yIndex][xIndex] == Wall.EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}

						if ((myMasu == Masu.NOT_BLACK && masuDown == Masu.BLACK) ||
								(myMasu == Masu.BLACK && masuDown == Masu.NOT_BLACK)) {
							if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (myMasu == Masu.BLACK && masuDown == Masu.BLACK) {
							if (tateWall[yIndex][xIndex] == Wall.EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}

						if ((myMasu == Masu.NOT_BLACK && masuLeft == Masu.BLACK) ||
								(myMasu == Masu.BLACK && masuLeft == Masu.NOT_BLACK)) {
							if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
						if (myMasu == Masu.BLACK && masuLeft == Masu.BLACK) {
							if (yokoWall[yIndex][xIndex - 1] == Wall.EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスの壁枚数は0枚、2枚(対面でないこと)のいずれかになる。
		 * そうでなければ黒マスが確定する。違反する場合はfalseを返す。
		 */
		private boolean whiteWallSolve() {
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
						if (exists > 2
								|| (exists == 1 && notExists == 3)) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						} else if (exists > 0) {
							// 壁2枚が確定。
							if ((wallUp == Wall.EXISTS && wallDown == Wall.EXISTS)
									|| (wallRight == Wall.EXISTS
											&& wallLeft == Wall.EXISTS)) {
								if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
									return false;
								}
								masu[yIndex][xIndex] = Masu.BLACK;
							} else if ((wallUp == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS)
									|| (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)) {
								if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
									return false;
								}
								masu[yIndex][xIndex] = Masu.BLACK;
							} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								if (wallUp == Wall.EXISTS && wallDown == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.EXISTS && wallLeft == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.EXISTS && wallUp == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.EXISTS && wallRight == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallUp == Wall.NOT_EXISTS && wallDown == Wall.SPACE) {
									tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.SPACE) {
									yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
								}
								if (wallDown == Wall.NOT_EXISTS && wallUp == Wall.SPACE) {
									tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
								}
								if (wallLeft == Wall.NOT_EXISTS && wallRight == Wall.SPACE) {
									yokoWall[yIndex][xIndex] = Wall.EXISTS;
								}
							}
						} else if (notExists > 2) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								// 壁なしが確定。
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
		 * 数字マスは上下左右にあるマスについて、白マスがいくつあるかを示している。
		 * 矛盾する場合はfalseを返す。
		 */
		public boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu masuUp = yIndex == 0 || numbers[yIndex - 1][xIndex] != null ? Masu.BLACK
								: masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 || numbers[yIndex][xIndex + 1] != null ? Masu.BLACK
								: masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 || numbers[yIndex + 1][xIndex] != null ? Masu.BLACK
								: masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 || numbers[yIndex][xIndex - 1] != null ? Masu.BLACK
								: masu[yIndex][xIndex - 1];
						if (masuUp == Masu.BLACK) {
							blackCnt++;
						} else if (masuUp == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuRight == Masu.BLACK) {
							blackCnt++;
						} else if (masuRight == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuDown == Masu.BLACK) {
							blackCnt++;
						} else if (masuDown == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuLeft == Masu.BLACK) {
							blackCnt++;
						} else if (masuLeft == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (numbers[yIndex][xIndex] < whiteCnt) {
							// 白マス過剰
							return false;
						}
						if (numbers[yIndex][xIndex] == whiteCnt) {
							if (masuUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.BLACK;
							}
							if (masuRight == Masu.SPACE) {
								masu[yIndex][xIndex + 1] = Masu.BLACK;
							}
							if (masuDown == Masu.SPACE) {
								masu[yIndex + 1][xIndex] = Masu.BLACK;
							}
							if (masuLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.BLACK;
							}
						}
						if (numbers[yIndex][xIndex] > 4 - blackCnt) {
							// 白マス不足
							return false;
						}
						if (numbers[yIndex][xIndex] == 4 - blackCnt) {
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
						}
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
					if (masu[yIndex][xIndex] != Masu.BLACK && numbers[yIndex][xIndex] == null) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE && numbers[yIndex][xIndex] == null) {
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

	public ShakashakaSolver(int height, int width, String param) {
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
		System.out.println(new ShakashakaSolver(height, width, param).solve());
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
				if (field.masu[yIndex][xIndex] == Masu.SPACE && field.numbers[yIndex][xIndex] == null) {
					Masu masuUp = yIndex == 0 || field.numbers[yIndex - 1][xIndex] != null ? Masu.BLACK
							: field.masu[yIndex - 1][xIndex];
					Masu masuRight = xIndex == field.getXLength() - 1 || field.numbers[yIndex][xIndex + 1] != null
							? Masu.BLACK
							: field.masu[yIndex][xIndex + 1];
					Masu masuDown = yIndex == field.getYLength() - 1 || field.numbers[yIndex + 1][xIndex] != null
							? Masu.BLACK
							: field.masu[yIndex + 1][xIndex];
					Masu masuLeft = xIndex == 0 || field.numbers[yIndex][xIndex - 1] != null ? Masu.BLACK
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
					if (whiteCnt > 2) {
						continue;
					}
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
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
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}
}
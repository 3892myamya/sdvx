package myamya.other.solver.nurimisaki;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class NurimisakiSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 岬かどうか
		private final boolean[][] misaki;
		// 数字の情報
		private final Integer[][] numbers;
		// ぬりみさ木モード
		private boolean tree;

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public boolean[][] getMisaki() {
			return misaki;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param, boolean tree) {
			masu = new Masu[height][width];
			misaki = new boolean[height][width];
			numbers = new Integer[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == '.') {
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					misaki[pos.getyIndex()][pos.getxIndex()] = true;
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						//16 - 255は '-'
						//256 - 999は '+'
						int cnt;
						if (ch == '-') {
							cnt = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							cnt = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							cnt = Integer.parseInt(String.valueOf(ch), 16);
						}
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = cnt;
						misaki[pos.getyIndex()][pos.getxIndex()] = true;
						index++;
					}
				}
			}
			this.tree = tree;
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			numbers = other.numbers;
			misaki = other.misaki;
			tree = other.tree;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(capacityStr);
						if (index >= 0) {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(capacityStr);
						}
					} else if (misaki[yIndex][xIndex]) {
						sb.append("○");
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 数字のマスから何マス伸ばせるか調べ、方向を確定する。
		 */
		public boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int upSpaceCnt = 0;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (masu[targetY][xIndex] == Masu.BLACK) {
								break;
							}
							upSpaceCnt++;
						}
						int rightSpaceCnt = 0;
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (masu[yIndex][targetX] == Masu.BLACK) {
								break;
							}
							rightSpaceCnt++;
						}
						int downSpaceCnt = 0;
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (masu[targetY][xIndex] == Masu.BLACK) {
								break;
							}
							downSpaceCnt++;
						}
						int leftSpaceCnt = 0;
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (masu[yIndex][targetX] == Masu.BLACK) {
								break;
							}
							leftSpaceCnt++;
						}
						int aroundSpaceCnt = 1 + upSpaceCnt + rightSpaceCnt + downSpaceCnt + leftSpaceCnt;
						if (aroundSpaceCnt < numbers[yIndex][xIndex]) {
							return false;
						} else {
							int fixedWhiteUp = numbers[yIndex][xIndex]
									- (1 + rightSpaceCnt + downSpaceCnt + leftSpaceCnt);
							int fixedWhiteRight = numbers[yIndex][xIndex]
									- (1 + upSpaceCnt + downSpaceCnt + leftSpaceCnt);
							int fixedWhiteDown = numbers[yIndex][xIndex]
									- (1 + upSpaceCnt + rightSpaceCnt + leftSpaceCnt);
							int fixedWhitetLeft = numbers[yIndex][xIndex]
									- (1 + upSpaceCnt + rightSpaceCnt + downSpaceCnt);
							if (fixedWhiteUp > 0) {
								for (int i = 1; i <= fixedWhiteUp; i++) {
									if (masu[yIndex - i][xIndex] == Masu.SPACE) {
										masu[yIndex - i][xIndex] = Masu.NOT_BLACK;
									}
								}
							}
							if (fixedWhiteRight > 0) {
								for (int i = 1; i <= fixedWhiteRight; i++) {
									if (masu[yIndex][xIndex + i] == Masu.SPACE) {
										masu[yIndex][xIndex + i] = Masu.NOT_BLACK;
									}
								}
							}
							if (fixedWhiteDown > 0) {
								for (int i = 1; i <= fixedWhiteDown; i++) {
									if (masu[yIndex + i][xIndex] == Masu.SPACE) {
										masu[yIndex + i][xIndex] = Masu.NOT_BLACK;
									}
								}
							}
							if (fixedWhitetLeft > 0) {
								for (int i = 1; i <= fixedWhitetLeft; i++) {
									if (masu[yIndex][xIndex - i] == Masu.SPACE) {
										masu[yIndex][xIndex - i] = Masu.NOT_BLACK;
									}
								}
							}
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int upWhiteCnt = 0;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (masu[targetY][xIndex] != Masu.NOT_BLACK) {
								break;
							}
							upWhiteCnt++;
						}
						int rightWhiteCnt = 0;
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (masu[yIndex][targetX] != Masu.NOT_BLACK) {
								break;
							}
							rightWhiteCnt++;
						}
						int downWhiteCnt = 0;
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (masu[targetY][xIndex] != Masu.NOT_BLACK) {
								break;
							}
							downWhiteCnt++;
						}
						int leftWhiteCnt = 0;
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (masu[yIndex][targetX] != Masu.NOT_BLACK) {
								break;
							}
							leftWhiteCnt++;
						}
						int aroundWhiteCnt = 1 + upWhiteCnt + rightWhiteCnt + downWhiteCnt + leftWhiteCnt;
						if (aroundWhiteCnt > numbers[yIndex][xIndex]) {
							return false;
						} else if (aroundWhiteCnt == numbers[yIndex][xIndex]) {
							if (yIndex - upWhiteCnt - 1 >= 0) {
								masu[yIndex - upWhiteCnt - 1][xIndex] = Masu.BLACK;
							}
							if (xIndex + rightWhiteCnt + 1 < getXLength()) {
								masu[yIndex][xIndex + rightWhiteCnt + 1] = Masu.BLACK;
							}
							if (yIndex + downWhiteCnt + 1 < getYLength()) {
								masu[yIndex + downWhiteCnt + 1][xIndex] = Masu.BLACK;
							}
							if (xIndex - leftWhiteCnt - 1 >= 0) {
								masu[yIndex][xIndex - leftWhiteCnt - 1] = Masu.BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 岬の周りは黒マス3個、岬でないマスかつ自身が白マスなら黒マス2個以下。
		 */
		private boolean misakiSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int blackCnt = 0;
					int whiteCnt = 0;
					Masu masuUp = yIndex == 0 ? Masu.BLACK : masu[yIndex - 1][xIndex];
					Masu masuRight = xIndex == getXLength() - 1 ? Masu.BLACK : masu[yIndex][xIndex + 1];
					Masu masuDown = yIndex == getYLength() - 1 ? Masu.BLACK : masu[yIndex + 1][xIndex];
					Masu masuLeft = xIndex == 0 ? Masu.BLACK : masu[yIndex][xIndex - 1];
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
					if (misaki[yIndex][xIndex]) {
						if (3 < blackCnt) {
							// 黒マス過剰
							return false;
						}
						if (3 == blackCnt) {
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
						if (3 > 4 - whiteCnt) {
							// 黒マス不足
							return false;
						}
						if (3 == 4 - whiteCnt) {
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
					} else {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							if (2 < blackCnt) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							if (2 < blackCnt) {
								return false;
							}
							if (2 == blackCnt) {
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
			}
			return true;

		}

		/**
		 * 黒2x2禁、白2x2禁
		 */
		public boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE
							&& masu4 == Masu.BLACK) {
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK
							&& masu4 == Masu.BLACK) {
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.SPACE
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex + 1][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.SPACE && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex] = Masu.BLACK;
					}
				}
			}
			return true;
		}

		/**
		 * 白マスがひとつながりにならない場合Falseを返す。
		 * 今までのロジックより高速に動きます。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (!misaki[yIndex][xIndex] && masu[yIndex][xIndex] == Masu.NOT_BLACK) {
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
				if (!continuePosSet.contains(nextPos) && !misaki[nextPos.getyIndex()][nextPos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !misaki[nextPos.getyIndex()][nextPos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !misaki[nextPos.getyIndex()][nextPos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !misaki[nextPos.getyIndex()][nextPos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 手筋による先読み
		 * TODO 実装中
		 */
		private boolean exSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (!misaki[yIndex][xIndex]) {
						// くぼみ禁止
						Masu masuUp = yIndex == 0 ? Masu.BLACK : masu[yIndex - 1][xIndex];
						Masu masuUR = yIndex == 0 || xIndex == getXLength() - 1 ? Masu.BLACK
								: masu[yIndex - 1][xIndex + 1];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.BLACK : masu[yIndex][xIndex + 1];
						Masu masuRD = xIndex == getXLength() - 1 || yIndex == getYLength() - 1 ? Masu.BLACK
								: masu[yIndex + 1][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.BLACK : masu[yIndex + 1][xIndex];
						Masu masuDL = yIndex == getYLength() - 1 || xIndex == 0 ? Masu.BLACK
								: masu[yIndex + 1][xIndex - 1];
						Masu masuLeft = xIndex == 0 ? Masu.BLACK : masu[yIndex][xIndex - 1];
						Masu masuLU = xIndex == 0 || yIndex == 0 ? Masu.BLACK : masu[yIndex - 1][xIndex - 1];

						if (masuUp == Masu.BLACK && masuLeft == Masu.BLACK && masuRD == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
						if (masuUp == Masu.BLACK && masuRight == Masu.BLACK && masuDL == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
						if (masuDown == Masu.BLACK && masuRight == Masu.BLACK && masuLU == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
						if (masuDown == Masu.BLACK && masuLeft == Masu.BLACK && masuUR == Masu.NOT_BLACK) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							}
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (!misaki[yIndex][xIndex]) {
						// カップ禁止
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuUR = yIndex == 0 || xIndex == getXLength() - 1 ? Masu.NOT_BLACK
								: masu[yIndex - 1][xIndex + 1];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuRD = xIndex == getXLength() - 1 || yIndex == getYLength() - 1 ? Masu.NOT_BLACK
								: masu[yIndex + 1][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuDL = yIndex == getYLength() - 1 || xIndex == 0 ? Masu.NOT_BLACK
								: masu[yIndex + 1][xIndex - 1];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
						Masu masuLU = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex - 1];
						boolean misakiU = yIndex == 0 ? false : misaki[yIndex - 1][xIndex];
						boolean misakiR = xIndex == getXLength() - 1 ? false : misaki[yIndex][xIndex + 1];
						boolean misakiD = yIndex == getYLength() - 1 ? false : misaki[yIndex + 1][xIndex];
						boolean misakiL = xIndex == 0 ? false : misaki[yIndex][xIndex - 1];
						Masu masuR2 = xIndex >= getXLength() - 2 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 2];
						Masu masuD2 = yIndex >= getYLength() - 2 ? Masu.NOT_BLACK : masu[yIndex + 2][xIndex];

						if (masuLeft == Masu.BLACK && masuDown == Masu.BLACK && masuRD == Masu.BLACK
								&& masuR2 == Masu.BLACK && !misakiR) {
							return false;
						}
						if (masuLeft == Masu.SPACE && masuDown == Masu.BLACK && masuRD == Masu.BLACK
								&& masuR2 == Masu.BLACK && !misakiR) {
							masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuLeft == Masu.BLACK && masuDown == Masu.SPACE && masuRD == Masu.BLACK
								&& masuR2 == Masu.BLACK && !misakiR) {
							masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masuLeft == Masu.BLACK && masuDown == Masu.BLACK && masuRD == Masu.SPACE
								&& masuR2 == Masu.BLACK && !misakiR) {
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuLeft == Masu.BLACK && masuDown == Masu.BLACK && masuRD == Masu.BLACK
								&& masuR2 == Masu.SPACE && !misakiR) {
							masu[yIndex][xIndex + 2] = Masu.NOT_BLACK;
						}
						if (masuLeft == Masu.BLACK && masuUp == Masu.BLACK && masuUR == Masu.BLACK
								&& masuR2 == Masu.BLACK && !misakiR) {
							return false;
						}
						if (masuLeft == Masu.SPACE && masuUp == Masu.BLACK && masuUR == Masu.BLACK
								&& masuR2 == Masu.BLACK && !misakiR) {
							masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuLeft == Masu.BLACK && masuUp == Masu.SPACE && masuUR == Masu.BLACK
								&& masuR2 == Masu.BLACK && !misakiR) {
							masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masuLeft == Masu.BLACK && masuUp == Masu.BLACK && masuUR == Masu.SPACE
								&& masuR2 == Masu.BLACK && !misakiR) {
							masu[yIndex - 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuLeft == Masu.BLACK && masuUp == Masu.BLACK && masuUR == Masu.BLACK
								&& masuR2 == Masu.SPACE && !misakiR) {
							masu[yIndex][xIndex + 2] = Masu.NOT_BLACK;
						}

						if (masuUp == Masu.BLACK && masuLeft == Masu.BLACK && masuDL == Masu.BLACK
								&& masuD2 == Masu.BLACK && !misakiD) {
							return false;
						}
						if (masuUp == Masu.SPACE && masuLeft == Masu.BLACK && masuDL == Masu.BLACK
								&& masuD2 == Masu.BLACK && !misakiD) {
							masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masuUp == Masu.BLACK && masuLeft == Masu.SPACE && masuDL == Masu.BLACK
								&& masuD2 == Masu.BLACK && !misakiD) {
							masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuUp == Masu.BLACK && masuLeft == Masu.BLACK && masuDL == Masu.SPACE
								&& masuD2 == Masu.BLACK && !misakiD) {
							masu[yIndex + 1][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuUp == Masu.BLACK && masuLeft == Masu.BLACK && masuDL == Masu.BLACK
								&& masuD2 == Masu.SPACE && !misakiD) {
							masu[yIndex + 2][xIndex] = Masu.NOT_BLACK;
						}

						if (masuUp == Masu.BLACK && masuRight == Masu.BLACK && masuRD == Masu.BLACK
								&& masuD2 == Masu.BLACK && !misakiD) {
							return false;
						}
						if (masuUp == Masu.SPACE && masuRight == Masu.BLACK && masuRD == Masu.BLACK
								&& masuD2 == Masu.BLACK && !misakiD) {
							masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masuUp == Masu.BLACK && masuRight == Masu.SPACE && masuRD == Masu.BLACK
								&& masuD2 == Masu.BLACK && !misakiD) {
							masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuUp == Masu.BLACK && masuRight == Masu.BLACK && masuRD == Masu.SPACE
								&& masuD2 == Masu.BLACK && !misakiD) {
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuUp == Masu.BLACK && masuRight == Masu.BLACK && masuRD == Masu.BLACK
								&& masuD2 == Masu.SPACE && !misakiD) {
							masu[yIndex + 2][xIndex] = Masu.NOT_BLACK;
						}

						if (masuDL == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.BLACK
								&& masuR2 == Masu.BLACK && !misakiR && !misakiD) {
							return false;
						}
						if (masuDL == Masu.SPACE && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.BLACK
								&& masuR2 == Masu.BLACK && !misakiR && !misakiD) {
							masu[yIndex + 1][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuDL == Masu.BLACK && masu[yIndex][xIndex] == Masu.SPACE && masuRD == Masu.BLACK
								&& masuR2 == Masu.BLACK && !misakiR && !misakiD) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuDL == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.SPACE
								&& masuR2 == Masu.BLACK && !misakiR && !misakiD) {
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuDL == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.BLACK
								&& masuR2 == Masu.SPACE && !misakiR && !misakiD) {
							masu[yIndex][xIndex + 2] = Masu.NOT_BLACK;
						}

						if (masuLU == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuUR == Masu.BLACK
								&& masuR2 == Masu.BLACK && !misakiR && !misakiU) {
							return false;
						}
						if (masuLU == Masu.SPACE && masu[yIndex][xIndex] == Masu.BLACK && masuUR == Masu.BLACK
								&& masuR2 == Masu.BLACK && !misakiR && !misakiU) {
							masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuLU == Masu.BLACK && masu[yIndex][xIndex] == Masu.SPACE && masuUR == Masu.BLACK
								&& masuR2 == Masu.BLACK && !misakiR && !misakiU) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuLU == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuUR == Masu.SPACE
								&& masuR2 == Masu.BLACK && !misakiR && !misakiU) {
							masu[yIndex - 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuLU == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuUR == Masu.BLACK
								&& masuR2 == Masu.SPACE && !misakiR && !misakiU) {
							masu[yIndex][xIndex + 2] = Masu.NOT_BLACK;
						}

						if (masuLU == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuDL == Masu.BLACK
								&& masuD2 == Masu.BLACK && !misakiL && !misakiD) {
							return false;
						}
						if (masuLU == Masu.SPACE && masu[yIndex][xIndex] == Masu.BLACK && masuDL == Masu.BLACK
								&& masuD2 == Masu.BLACK && !misakiL && !misakiD) {
							masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuLU == Masu.BLACK && masu[yIndex][xIndex] == Masu.SPACE && masuDL == Masu.BLACK
								&& masuD2 == Masu.BLACK && !misakiL && !misakiD) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuLU == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuDL == Masu.SPACE
								&& masuD2 == Masu.BLACK && !misakiL && !misakiD) {
							masu[yIndex + 1][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuLU == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuDL == Masu.BLACK
								&& masuD2 == Masu.SPACE && !misakiL && !misakiD) {
							masu[yIndex + 2][xIndex] = Masu.NOT_BLACK;
						}

						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.BLACK
								&& masuD2 == Masu.BLACK && !misakiR && !misakiD) {
							return false;
						}
						if (masuUR == Masu.SPACE && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.BLACK
								&& masuD2 == Masu.BLACK && !misakiR && !misakiD) {
							masu[yIndex - 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.SPACE && masuRD == Masu.BLACK
								&& masuD2 == Masu.BLACK && !misakiR && !misakiD) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.SPACE
								&& masuD2 == Masu.BLACK && !misakiR && !misakiD) {
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.BLACK
								&& masuD2 == Masu.SPACE && !misakiR && !misakiD) {
							masu[yIndex + 2][xIndex] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.BLACK
								&& masuDL == Masu.BLACK && !misakiR && !misakiD) {
							return false;
						}
						if (masuUR == Masu.SPACE && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.BLACK
								&& masuDL == Masu.BLACK && !misakiR && !misakiD) {
							masu[yIndex - 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.SPACE && masuRD == Masu.BLACK
								&& masuDL == Masu.BLACK && !misakiR && !misakiD) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.SPACE
								&& masuDL == Masu.BLACK && !misakiR && !misakiD) {
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.BLACK
								&& masuDL == Masu.SPACE && !misakiR && !misakiD) {
							masu[yIndex + 1][xIndex - 1] = Masu.NOT_BLACK;
						}

						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.BLACK
								&& masuLU == Masu.BLACK && !misakiR && !misakiU) {
							return false;
						}
						if (masuUR == Masu.SPACE && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.BLACK
								&& masuLU == Masu.BLACK && !misakiR && !misakiU) {
							masu[yIndex - 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.SPACE && masuRD == Masu.BLACK
								&& masuLU == Masu.BLACK && !misakiR && !misakiU) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.SPACE
								&& masuLU == Masu.BLACK && !misakiR && !misakiU) {
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuRD == Masu.BLACK
								&& masuLU == Masu.SPACE && !misakiR && !misakiU) {
							masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
						}

						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuDL == Masu.BLACK
								&& masuLU == Masu.BLACK && !misakiL && !misakiU) {
							return false;
						}
						if (masuUR == Masu.SPACE && masu[yIndex][xIndex] == Masu.BLACK && masuDL == Masu.BLACK
								&& masuLU == Masu.BLACK && !misakiL && !misakiU) {
							masu[yIndex - 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.SPACE && masuDL == Masu.BLACK
								&& masuLU == Masu.BLACK && !misakiL && !misakiU) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuDL == Masu.SPACE
								&& masuLU == Masu.BLACK && !misakiL && !misakiU) {
							masu[yIndex + 1][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuUR == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuDL == Masu.BLACK
								&& masuLU == Masu.SPACE && !misakiL && !misakiU) {
							masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
						}

						if (masuRD == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuDL == Masu.BLACK
								&& masuLU == Masu.BLACK && !misakiL && !misakiD) {
							return false;
						}
						if (masuRD == Masu.SPACE && masu[yIndex][xIndex] == Masu.BLACK && masuDL == Masu.BLACK
								&& masuLU == Masu.BLACK && !misakiL && !misakiD) {
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuRD == Masu.BLACK && masu[yIndex][xIndex] == Masu.SPACE && masuDL == Masu.BLACK
								&& masuLU == Masu.BLACK && !misakiL && !misakiD) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masuRD == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuDL == Masu.SPACE
								&& masuLU == Masu.BLACK && !misakiL && !misakiD) {
							masu[yIndex + 1][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masuRD == Masu.BLACK && masu[yIndex][xIndex] == Masu.BLACK && masuDL == Masu.BLACK
								&& masuLU == Masu.SPACE && !misakiL && !misakiD) {
							masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * ぬりみさ木モード特有の処理
		 * 分岐の数がみさきの数-2を超えていたら失敗
		 */
		public boolean branchSolve() {
			if (tree) {
				int misakiCnt = 0;
				int branchCnt = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (misaki[yIndex][xIndex]) {
							misakiCnt++;
						} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							int notBlackCnt = 0;
							if (yIndex == 0 || masu[yIndex - 1][xIndex] == Masu.NOT_BLACK) {
								notBlackCnt++;
							}
							if (xIndex == getXLength() - 1 || masu[yIndex][xIndex + 1] == Masu.NOT_BLACK) {
								notBlackCnt++;
							}
							if (yIndex == getYLength() - 1 || masu[yIndex + 1][xIndex] == Masu.NOT_BLACK) {
								notBlackCnt++;
							}
							if (xIndex == 0 || masu[yIndex][xIndex - 1] == Masu.NOT_BLACK) {
								notBlackCnt++;
							}
							if (notBlackCnt > 2) {
								branchCnt = notBlackCnt - 2;
							}
						}
					}
				}
				if (branchCnt + 2 > misakiCnt) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
				return false;
			}
			if (!misakiSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
//			if (!exSolve()) {
//				return false;
//			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
				if (tree && !branchSolve()) {
					return false;
				}
				if (tree && !loopSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * ぬりみさきモードではループ禁止
		 */
		private boolean loopSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (!whitePosSet.contains(whitePos)) {
							Set<Position> targetPosSet = new HashSet<>();
							targetPosSet.add(whitePos);
							if (!loopCheck(whitePos, targetPosSet, null)) {
								return false;
							}
							whitePosSet.addAll(targetPosSet);
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白確定マスをつなぎ、ループができてる場合falseを返す。
		 */
		private boolean loopCheck(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (continuePosSet.contains(nextPos)) {
					return false;
				}
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!loopCheck(nextPos, continuePosSet, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				}
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!loopCheck(nextPos, continuePosSet, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (continuePosSet.contains(nextPos)) {
					return false;
				}
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!loopCheck(nextPos, continuePosSet, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				}
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!loopCheck(nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
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
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public NurimisakiSolver(int height, int width, String param, boolean tree) {
		field = new Field(height, width, param, tree);
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
		System.out.println(new NurimisakiSolver(height, width, param, false).solve());
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
		System.out.println("難易度:" + (count * 2));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 2).toString();
	}

	/**
	 * 仮置きして調べる
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
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
		} else {
			// どちらにしても理論
			for (int y = 0; y < field.getYLength(); y++) {
				for (int x = 0; x < field.getXLength(); x++) {
					if (virtual2.masu[y][x] == virtual.masu[y][x]) {
						field.masu[y][x] = virtual.masu[y][x];
					}
				}
			}
		}
		return true;
	}
}
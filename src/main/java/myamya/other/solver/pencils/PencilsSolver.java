package myamya.other.solver.pencils;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class PencilsSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_K = "klmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private final Integer[][] numbers;
		// 表出芯の位置情報(画面表示用)
		private final Set<Position> fixedLeadPos;
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

		public Field(int height, int width, String param, boolean ura) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			fixedLeadPos = new HashSet<>();
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
				int interval = ALPHABET_FROM_K.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int capacity;
					Position pos = new Position(index / getXLength(), index % getXLength());
					if (ch == '.') {
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else if (ch == 'g' || ch == 'h' || ch == 'i' || ch == 'j') {
						Direction lead = Direction.getByNum(Character.getNumericValue(ch) - 15);
						fixedLeadPos.add(pos);
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						// 芯と逆方向の白マス+壁が一部確定
						if (lead == Direction.UP) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
							masu[pos.getyIndex() + 1][pos.getxIndex()] = Masu.NOT_BLACK;
						} else if (lead == Direction.RIGHT) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
							masu[pos.getyIndex()][pos.getxIndex() - 1] = Masu.NOT_BLACK;
						} else if (lead == Direction.DOWN) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
							masu[pos.getyIndex() - 1][pos.getxIndex()] = Masu.NOT_BLACK;
						} else if (lead == Direction.LEFT) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
							masu[pos.getyIndex()][pos.getxIndex() + 1] = Masu.NOT_BLACK;
						}
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							capacity = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			numbers = other.numbers;
			fixedLeadPos = other.fixedLeadPos;
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

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
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
						String numStr = String.valueOf(numbers[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(numStr);
						if (index == 0) {
							sb.append("・");
						} else {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
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
		 * 白マス、黒マスともに壁は2枚か3枚。また、白マスは壁が2枚なら必ず直進。
		 * 条件を満たさない場合falseを返す。
		 */
		private boolean wallSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
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
					if (notExists > 2 || exists == 4) {
						return false;
					}
					if (exists == 3) {
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
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						// カーブしてれば黒マス確定
						if ((wallUp == Wall.NOT_EXISTS && wallRight == Wall.NOT_EXISTS)
								|| (wallUp == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)
								|| (wallRight == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS)
								|| (wallDown == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)) {
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						if ((wallUp == Wall.NOT_EXISTS && wallRight == Wall.NOT_EXISTS)
								|| (wallUp == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)
								|| (wallRight == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS)
								|| (wallDown == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)) {
							return false;
						}
						// カーブ禁止。
						// 行き止まりがあるので必ずしも直進しない。
						if (wallRight == Wall.NOT_EXISTS || wallLeft == Wall.NOT_EXISTS) {
							if (wallUp == Wall.SPACE) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
						}
						if (wallUp == Wall.NOT_EXISTS || wallDown == Wall.NOT_EXISTS) {
							if (wallRight == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
						}
					}
				}

			}
			return true;
		}

		/**
		 * 数字は白マスの長さになる。伸ばす方向を調査し確定させる。
		 * どうやっても既定の長さを確保できない場合はfalseを返す。
		 */
		public boolean lengthSolve() {
			// 伸ばす候補の確認
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int upSpaceCnt = 0;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (numbers[targetY][xIndex] != null || tateWall[targetY][xIndex] == Wall.EXISTS
									|| masu[targetY][xIndex] == Masu.BLACK) {
								break;
							}
							upSpaceCnt++;
						}
						int rightSpaceCnt = 0;
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (numbers[yIndex][targetX] != null || yokoWall[yIndex][targetX - 1] == Wall.EXISTS
									|| masu[yIndex][targetX] == Masu.BLACK) {
								break;
							}
							rightSpaceCnt++;
						}
						int downSpaceCnt = 0;
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (numbers[targetY][xIndex] != null || tateWall[targetY - 1][xIndex] == Wall.EXISTS
									|| masu[targetY][xIndex] == Masu.BLACK) {
								break;
							}
							downSpaceCnt++;
						}
						int leftSpaceCnt = 0;
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (numbers[yIndex][targetX] != null || yokoWall[yIndex][targetX] == Wall.EXISTS
									|| masu[yIndex][targetX] == Masu.BLACK) {
								break;
							}
							leftSpaceCnt++;
						}
						int varticalCnt = 1 + upSpaceCnt + downSpaceCnt;
						int horizonalCnt = 1 + rightSpaceCnt + leftSpaceCnt;
						if (varticalCnt < numbers[yIndex][xIndex] && horizonalCnt < numbers[yIndex][xIndex]) {
							return false;
						} else if (varticalCnt < numbers[yIndex][xIndex]) {
							// 横伸ばし確定
							int fixedWhiteRight = numbers[yIndex][xIndex]
									- (1 + leftSpaceCnt);
							int fixedWhiteLeft = numbers[yIndex][xIndex]
									- (1 + rightSpaceCnt);
							if (fixedWhiteRight > 0) {
								for (int i = 1; i <= fixedWhiteRight; i++) {
									yokoWall[yIndex][xIndex + i - 1] = Wall.NOT_EXISTS;
									masu[yIndex][xIndex + i] = Masu.NOT_BLACK;
								}
							}
							if (fixedWhiteLeft > 0) {
								for (int i = 1; i <= fixedWhiteLeft; i++) {
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
									masu[yIndex][xIndex - i] = Masu.NOT_BLACK;
								}
							}
						} else if (horizonalCnt < numbers[yIndex][xIndex]) {
							// 縦伸ばし確定
							int fixedWhiteUp = numbers[yIndex][xIndex]
									- (1 + downSpaceCnt);
							int fixedWhiteDown = numbers[yIndex][xIndex]
									- (1 + upSpaceCnt);
							if (fixedWhiteUp > 0) {
								for (int i = 1; i <= fixedWhiteUp; i++) {
									tateWall[yIndex - i][xIndex] = Wall.NOT_EXISTS;
									masu[yIndex - i][xIndex] = Masu.NOT_BLACK;
								}
							}
							if (fixedWhiteDown > 0) {
								for (int i = 1; i <= fixedWhiteDown; i++) {
									tateWall[yIndex + i - 1][xIndex] = Wall.NOT_EXISTS;
									masu[yIndex + i][xIndex] = Masu.NOT_BLACK;
								}
							}
						}
					}
				}
			}
			// 確定鉛筆の確認
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int upWhiteCnt = 0;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (numbers[targetY][xIndex] != null || tateWall[targetY][xIndex] != Wall.NOT_EXISTS
									|| masu[targetY][xIndex] != Masu.NOT_BLACK) {
								break;
							}
							upWhiteCnt++;
						}
						int rightWhiteCnt = 0;
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (numbers[yIndex][targetX] != null || yokoWall[yIndex][targetX - 1] != Wall.NOT_EXISTS
									|| masu[yIndex][targetX] != Masu.NOT_BLACK) {
								break;
							}
							rightWhiteCnt++;
						}
						int downWhiteCnt = 0;
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (numbers[targetY][xIndex] != null || tateWall[targetY - 1][xIndex] != Wall.NOT_EXISTS
									|| masu[targetY][xIndex] != Masu.NOT_BLACK) {
								break;
							}
							downWhiteCnt++;
						}
						int leftWhiteCnt = 0;
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (numbers[yIndex][targetX] != null || yokoWall[yIndex][targetX] != Wall.NOT_EXISTS
									|| masu[yIndex][targetX] != Masu.NOT_BLACK) {
								break;
							}
							leftWhiteCnt++;
						}
						int varticalWhiteCnt = 1 + upWhiteCnt + downWhiteCnt;
						int horizonalWhiteCnt = 1 + rightWhiteCnt + leftWhiteCnt;
						if (varticalWhiteCnt > numbers[yIndex][xIndex] || horizonalWhiteCnt > numbers[yIndex][xIndex]) {
							// 縦もしくは横の長さがオーバー
							return false;
						}
						// 縦または横の長さが鉛筆の長さと等しければ、そこから1方向に芯(黒マス)を出す必要がある。
						// 1方向も黒ますが出せなかったり、2方向に黒マスが出たりしたらアウト。
						if (varticalWhiteCnt == numbers[yIndex][xIndex]
								|| horizonalWhiteCnt == numbers[yIndex][xIndex]) {
							boolean canUp = false, canRight = false, canDown = false, canLeft = false;
							int targetYUp = yIndex - upWhiteCnt - 1;
							int targetYDown = yIndex + downWhiteCnt + 1;
							int targetXRight = xIndex + rightWhiteCnt + 1;
							int targetXLeft = xIndex - leftWhiteCnt - 1;
							if (varticalWhiteCnt == numbers[yIndex][xIndex]) {
								// 穴が開いている方の黒マス・穴なし確定
								canUp = targetYUp >= 0
										&& numbers[targetYUp][xIndex] == null
										&& tateWall[targetYUp][xIndex] != Wall.EXISTS
										&& masu[targetYUp][xIndex] != Masu.NOT_BLACK
										&& (targetYDown >= getYLength()
												|| tateWall[targetYDown - 1][xIndex] != Wall.NOT_EXISTS)
										&& (targetXLeft < 0 || yokoWall[yIndex][targetXLeft] != Wall.NOT_EXISTS)
										&& (targetXRight >= getYLength()
												|| yokoWall[yIndex][targetXRight - 1] != Wall.NOT_EXISTS);

								canDown = targetYDown < getYLength()
										&& numbers[targetYDown][xIndex] == null
										&& tateWall[targetYDown - 1][xIndex] != Wall.EXISTS
										&& masu[targetYDown][xIndex] != Masu.NOT_BLACK
										&& (targetYUp < 0 || tateWall[targetYUp][xIndex] != Wall.NOT_EXISTS)
										&& (targetXLeft < 0 || yokoWall[yIndex][targetXLeft] != Wall.NOT_EXISTS)
										&& (targetXRight >= getYLength()
												|| yokoWall[yIndex][targetXRight - 1] != Wall.NOT_EXISTS);
							}
							if (horizonalWhiteCnt == numbers[yIndex][xIndex]) {
								// 穴が開いている方の黒マス・穴なし確定
								canRight = targetXRight < getYLength()
										&& numbers[yIndex][targetXRight] == null
										&& yokoWall[yIndex][targetXRight - 1] != Wall.EXISTS
										&& masu[yIndex][targetXRight] != Masu.NOT_BLACK
										&& (targetXLeft < 0 || yokoWall[yIndex][targetXLeft] != Wall.NOT_EXISTS)
										&& (targetYDown >= getYLength()
												|| tateWall[targetYDown - 1][xIndex] != Wall.NOT_EXISTS)
										&& (targetYUp < 0 || tateWall[targetYUp][xIndex] != Wall.NOT_EXISTS);
								canLeft = targetXLeft >= 0
										&& numbers[yIndex][targetXLeft] == null
										&& yokoWall[yIndex][targetXLeft] != Wall.EXISTS
										&& masu[yIndex][targetXLeft] != Masu.NOT_BLACK
										&& (targetXRight >= getYLength()
												|| yokoWall[yIndex][targetXRight - 1] != Wall.NOT_EXISTS)
										&& (targetYDown >= getYLength()
												|| tateWall[targetYDown - 1][xIndex] != Wall.NOT_EXISTS)
										&& (targetYUp < 0 || tateWall[targetYUp][xIndex] != Wall.NOT_EXISTS);
							}
							if (!canUp && !canRight && !canDown && !canLeft) {
								return false;
							}
							if (canUp && !canRight && !canDown && !canLeft) {
								tateWall[targetYUp][xIndex] = Wall.NOT_EXISTS;
								masu[targetYUp][xIndex] = Masu.BLACK;
							}
							if (!canUp && canRight && !canDown && !canLeft) {
								yokoWall[yIndex][targetXRight - 1] = Wall.NOT_EXISTS;
								masu[yIndex][targetXRight] = Masu.BLACK;
							}
							if (!canUp && !canRight && canDown && !canLeft) {
								tateWall[targetYDown - 1][xIndex] = Wall.NOT_EXISTS;
								masu[targetYDown][xIndex] = Masu.BLACK;
							}
							if (!canUp && !canRight && !canDown && canLeft) {
								yokoWall[yIndex][targetXLeft] = Wall.NOT_EXISTS;
								masu[yIndex][targetXLeft] = Masu.BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスと黒マスは必ず1箇所で接続し黒マスの長さは白マスの長さ+1になる。
		 * 白マスと黒マスが複数回入れ替わったり、長さの条件を満たさなければfalseを返す。
		 */
		public boolean pencilSolve() {
			Set<Position> alreadyServeyPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position pivot = new Position(yIndex, xIndex);
					} else if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position pivot = new Position(yIndex, xIndex);
					}
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
			if (!wallSolve()) {
				return false;
			}
			if (!lengthSolve()) {
				return false;
			}
			if (!pencilSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
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

	public PencilsSolver(int height, int width, String param, boolean ura) {
		field = new Field(height, width, param, ura);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?pencils/8/8/323t2n4q2q1t21u231m"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new PencilsSolver(height, width, param, false).solve());
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
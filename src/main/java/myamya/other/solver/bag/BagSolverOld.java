package myamya.other.solver.bag;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

/**
 * 最初に作ったバッグのソルバー
 * …だが、スリザー方式だと全然解けないので黒マス包囲網方式に変えることにした。
 */
public class BagSolverOld implements Solver {
	public enum Wall {
		SPACE("　"), NOT_EXISTS("・"), EXISTS("■");

		String str;

		Wall(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の情報
		private final Integer[][] numbers;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,-1と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] yokoExtraWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、-1,0と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] tateExtraWall;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			yokoExtraWall = new Wall[height][width + 1];
			tateExtraWall = new Wall[height + 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int capacity;
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
					Position pos = new Position(index / getXLength(), index % getXLength());
					numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					index++;
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			yokoExtraWall = new Wall[other.getYLength()][other.getXLength() + 1];
			tateExtraWall = new Wall[other.getYLength() + 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = other.yokoExtraWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = other.tateExtraWall[yIndex][xIndex];
				}
			}
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateExtraWall[yIndex][xIndex]);
					sb.append("□");
				}
				sb.append(System.lineSeparator());
				if (yIndex != getYLength()) {
					for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
						sb.append(yokoExtraWall[yIndex][xIndex]);
						if (xIndex != getXLength()) {
							if (numbers[yIndex][xIndex] != null) {
								if (numbers[yIndex][xIndex] > 99) {
									sb.append("99");
								}
								String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
								int index = HALF_NUMS.indexOf(capacityStr);
								if (index >= 0) {
									sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
								} else {
									sb.append(capacityStr);
								}
							} else {
								sb.append("　");
							}
						}
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					sb.append(yokoExtraWall[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateExtraWall[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 数字と壁の関係を調べる。
		 * 壁にぶつからないまま外に出てしまったり、矛盾した場合falseを返す。
		 */
		public boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int upSpaceCnt = 0;
						for (int targetY = yIndex; targetY >= 0; targetY--) {
							if (tateExtraWall[targetY][xIndex] == Wall.EXISTS) {
								break;
							}
							upSpaceCnt++;
						}
						int rightSpaceCnt = 0;
						for (int targetX = xIndex + 1; targetX <= getXLength(); targetX++) {
							if (yokoExtraWall[yIndex][targetX] == Wall.EXISTS) {
								break;
							}
							rightSpaceCnt++;
						}
						int downSpaceCnt = 0;
						for (int targetY = yIndex + 1; targetY <= getYLength(); targetY++) {
							if (tateExtraWall[targetY][xIndex] == Wall.EXISTS) {
								break;
							}
							downSpaceCnt++;
						}
						int leftSpaceCnt = 0;
						for (int targetX = xIndex; targetX >= 0; targetX--) {
							if (yokoExtraWall[yIndex][targetX] == Wall.EXISTS) {
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
									if (tateExtraWall[yIndex - i + 1][xIndex] == Wall.EXISTS) {
										return false;
									}
									tateExtraWall[yIndex - i + 1][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (fixedWhiteRight > 0) {
								for (int i = 1; i <= fixedWhiteRight; i++) {
									if (yokoExtraWall[yIndex][xIndex + i] == Wall.EXISTS) {
										return false;
									}
									yokoExtraWall[yIndex][xIndex + i] = Wall.NOT_EXISTS;
								}
							}
							if (fixedWhiteDown > 0) {
								for (int i = 1; i <= fixedWhiteDown; i++) {
									if (tateExtraWall[yIndex + i][xIndex] == Wall.EXISTS) {
										return false;
									}
									tateExtraWall[yIndex + i][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (fixedWhitetLeft > 0) {
								for (int i = 1; i <= fixedWhitetLeft; i++) {
									if (yokoExtraWall[yIndex][xIndex - i + 1] == Wall.EXISTS) {
										return false;
									}
									yokoExtraWall[yIndex][xIndex - i + 1] = Wall.NOT_EXISTS;
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
						for (int targetY = yIndex; targetY >= 0; targetY--) {
							if (tateExtraWall[targetY][xIndex] != Wall.NOT_EXISTS) {
								break;
							}
							if (targetY == 0) {
								return false;
							}
							upWhiteCnt++;
						}
						int rightWhiteCnt = 0;
						for (int targetX = xIndex + 1; targetX <= getXLength(); targetX++) {
							if (yokoExtraWall[yIndex][targetX] != Wall.NOT_EXISTS) {
								break;
							}
							if (targetX == getXLength()) {
								return false;
							}
							rightWhiteCnt++;
						}
						int downWhiteCnt = 0;
						for (int targetY = yIndex + 1; targetY <= getYLength(); targetY++) {
							if (tateExtraWall[targetY][xIndex] != Wall.NOT_EXISTS) {
								break;
							}
							if (targetY == getYLength()) {
								return false;
							}
							downWhiteCnt++;
						}
						int leftWhiteCnt = 0;
						for (int targetX = xIndex; targetX >= 0; targetX--) {
							if (yokoExtraWall[yIndex][targetX] != Wall.NOT_EXISTS) {
								break;
							}
							if (targetX == 0) {
								return false;
							}
							leftWhiteCnt++;
						}
						int aroundWhiteCnt = 1 + upWhiteCnt + rightWhiteCnt + downWhiteCnt + leftWhiteCnt;
						if (aroundWhiteCnt > numbers[yIndex][xIndex]) {
							return false;
						} else if (aroundWhiteCnt == numbers[yIndex][xIndex]) {
							if (yIndex - upWhiteCnt - 1 >= 0) {
								if (tateExtraWall[yIndex - upWhiteCnt][xIndex] == Wall.NOT_EXISTS) {
									return false;
								}
								tateExtraWall[yIndex - upWhiteCnt][xIndex] = Wall.EXISTS;
							}
							if (xIndex + rightWhiteCnt + 1 < getXLength()) {
								if (yokoExtraWall[yIndex][xIndex + rightWhiteCnt + 1] == Wall.NOT_EXISTS) {
									return false;
								}
								yokoExtraWall[yIndex][xIndex + rightWhiteCnt + 1] = Wall.EXISTS;
							}
							if (yIndex + downWhiteCnt + 1 < getYLength()) {
								if (tateExtraWall[yIndex + downWhiteCnt + 1][xIndex] == Wall.NOT_EXISTS) {
									return false;
								}
								tateExtraWall[yIndex + downWhiteCnt + 1][xIndex] = Wall.EXISTS;
							}
							if (xIndex - leftWhiteCnt - 1 >= 0) {
								if (yokoExtraWall[yIndex][xIndex - leftWhiteCnt] == Wall.NOT_EXISTS) {
									return false;
								}
								yokoExtraWall[yIndex][xIndex - leftWhiteCnt] = Wall.EXISTS;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 柱につながる壁は必ず0か2になる。ならない場合falseを返す。
		 */
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					int existsCount = 0;
					int notExistsCount = 0;
					Wall wallUp = yIndex == 0 ? Wall.NOT_EXISTS : yokoExtraWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallDown = yIndex == getYLength() ? Wall.NOT_EXISTS
							: yokoExtraWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallRight = xIndex == getXLength() ? Wall.NOT_EXISTS
							: tateExtraWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.NOT_EXISTS
							: tateExtraWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					if (existsCount > 2 || (existsCount == 1 && notExistsCount == 3)) {
						return false;
					}
					if (existsCount == 2) {
						if (wallUp == Wall.SPACE) {
							yokoExtraWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
						}
					} else if (notExistsCount == 3) {
						if (wallUp == Wall.SPACE) {
							yokoExtraWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
						}
					} else if (existsCount == 1 && notExistsCount == 2) {
						if (wallUp == Wall.SPACE) {
							yokoExtraWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					}
				}
			}
			return true;
		}

		/**
		 * ルール上、各列をふさぐ壁は必ず偶数になる。
		 * 偶数になっていない場合falseを返す。
		 */
		private boolean oddSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				int existsCount = 0;
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
						existsCount = 0;
						break;
					} else if (yokoExtraWall[yIndex][xIndex] == Wall.EXISTS) {
						existsCount++;
					}
				}
				if (existsCount % 2 != 0) {
					return false;
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				int existsCount = 0;
				for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
						existsCount = 0;
						break;
					} else if (tateExtraWall[yIndex][xIndex] == Wall.EXISTS) {
						existsCount++;
					}
				}
				if (existsCount % 2 != 0) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 壁が1つながりになっていない場合falseを返す。
		 */
		public boolean connectWhiteSolve() {
			Set<Position> yokoBlackWallPosSet = new HashSet<>();
			Set<Position> tateBlackWallPosSet = new HashSet<>();
			Position typicalExistPos = null;
			boolean isYokoWall = false;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] == Wall.EXISTS) {
						Position existPos = new Position(yIndex, xIndex);
						yokoBlackWallPosSet.add(existPos);
						if (typicalExistPos == null) {
							typicalExistPos = existPos;
							isYokoWall = true;
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.EXISTS) {
						Position existPos = new Position(yIndex, xIndex);
						tateBlackWallPosSet.add(existPos);
						if (typicalExistPos == null) {
							typicalExistPos = existPos;
						}
					}
				}
			}
			if (typicalExistPos == null) {
				return true;
			} else {
				Set<Position> continueYokoWallPosSet = new HashSet<>();
				Set<Position> continueTateWallPosSet = new HashSet<>();
				if (isYokoWall) {
					continueYokoWallPosSet.add(typicalExistPos);
				} else {
					continueTateWallPosSet.add(typicalExistPos);
				}
				setContinueExistWallPosSet(typicalExistPos, continueYokoWallPosSet, continueTateWallPosSet, isYokoWall,
						null);
				yokoBlackWallPosSet.removeAll(continueYokoWallPosSet);
				tateBlackWallPosSet.removeAll(continueTateWallPosSet);
				return yokoBlackWallPosSet.isEmpty() && tateBlackWallPosSet.isEmpty();
			}
		}

		private void setContinueExistWallPosSet(Position pos, Set<Position> continueYokoWallPosSet,
				Set<Position> continueTateWallPosSet, boolean isYoko, Direction from) {
			if (isYoko) {
				if (pos.getxIndex() != 0 && from != Direction.UP) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.RIGHT);
					}
				}
				if (pos.getyIndex() != 0 && from != Direction.UP) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.DOWN);
					}
				}
				if (pos.getxIndex() != getXLength() && from != Direction.UP) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex());
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.LEFT);
					}
				}
				if (pos.getxIndex() != getXLength() && from != Direction.DOWN) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.LEFT);
					}
				}
				if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.UP);
					}
				}
				if (pos.getxIndex() != 0 && from != Direction.DOWN) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.RIGHT);
					}
				}
			} else {
				if (pos.getyIndex() != 0 && from != Direction.LEFT) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.DOWN);
					}
				}
				if (pos.getyIndex() != 0 && from != Direction.RIGHT) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.DOWN);
					}
				}
				if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.LEFT);
					}
				}
				if (pos.getyIndex() != getYLength() && from != Direction.RIGHT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.UP);
					}
				}
				if (pos.getyIndex() != getYLength() && from != Direction.LEFT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex());
					if (!continueYokoWallPosSet.contains(nextPos)
							&& yokoExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueYokoWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, true,
								Direction.UP);
					}
				}
				if (pos.getxIndex() != 0 && from != Direction.LEFT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					if (!continueTateWallPosSet.contains(nextPos)
							&& tateExtraWall[nextPos.getyIndex()][nextPos.getxIndex()] != Wall.NOT_EXISTS) {
						continueTateWallPosSet.add(nextPos);
						setContinueExistWallPosSet(nextPos, continueYokoWallPosSet, continueTateWallPosSet, false,
								Direction.RIGHT);
					}
				}
			}
		}

		/**
		 * フィールドに1つは壁が必要。
		 */
		private boolean finalSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] != Wall.NOT_EXISTS) {
						return true;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] != Wall.NOT_EXISTS) {
						return true;
					}
				}
			}
			return false;
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
			if (!nextSolve()) {
				return false;
			}
			if (!oddSolve()) {
				return false;
			}
			if (!connectWhiteSolve()) {
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

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public BagSolverOld(int height, int width, String param) {
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
		System.out.println(new BagSolverOld(height, width, param).solve());
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
			while (field.getStateDump().equals(befStr) && recursiveCnt < 2) {
				if (!candSolve(field, recursiveCnt * 3)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 2 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + count);
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
				if (field.yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
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
		virtual.yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.tateExtraWall = virtual2.tateExtraWall;
			field.yokoExtraWall = virtual2.yokoExtraWall;
		} else if (!allowNotBlack) {
			field.tateExtraWall = virtual.tateExtraWall;
			field.yokoExtraWall = virtual.yokoExtraWall;
		}
		return true;
	}

	private boolean oneCandTateWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.tateExtraWall = virtual2.tateExtraWall;
			field.yokoExtraWall = virtual2.yokoExtraWall;
		} else if (!allowNotBlack) {
			field.tateExtraWall = virtual.tateExtraWall;
			field.yokoExtraWall = virtual.yokoExtraWall;
		}
		return true;
	}
}
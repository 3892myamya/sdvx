package myamya.other.solver.fillmat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class FillmatSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// 数字の情報
		private Integer[][] numbers;
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

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
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
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
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int num;
					if (ch == '.') {
						//
					} else {
						if (ch == '-') {
							num = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							num = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							num = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
					}
					index++;
				}
			}
		}

		public Field(Field other) {
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
						sb.append("　");
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
					sb.append(numbers[yIndex][xIndex]);
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
		 * 部屋に数字が2つあったり、サイズが4を超えてはならない。
		 */
		public boolean roomSolve() {
			List<Set<Position>> fixedPosSetList = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pivot = new Position(yIndex, xIndex);
					int number = numbers[pivot.getyIndex()][pivot.getxIndex()] != null
							? numbers[pivot.getyIndex()][pivot.getxIndex()]
							: 0;
					Set<Position> continueWhitePosSet = new HashSet<>();
					continueWhitePosSet.add(pivot);
					if (!setContinuePosSetContainsDoubleNumber(pivot, continueWhitePosSet, null, number)) {
						return false;
					}
					Set<Position> continueNotBlackPosSet = new HashSet<>();
					continueNotBlackPosSet.add(pivot);
					if (!setContinuePosSet(pivot, continueNotBlackPosSet, null, number)) {
						return false;
					}
					if (continueWhitePosSet.size() == continueNotBlackPosSet.size()) {
						fixedPosSetList.add(continueWhitePosSet);
					}
				}
			}
			// 同じ長さの畳の隣接チェック
			for (Set<Position> posSet : fixedPosSetList) {
				for (Set<Position> anotherPosSet : fixedPosSetList) {
					if (!posSet.equals(anotherPosSet) && posSet.size() == anotherPosSet.size()) {
						for (Position pos : posSet) {
							if (anotherPosSet.contains(new Position(pos.getyIndex() - 1, pos.getxIndex())) ||
									anotherPosSet.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1)) ||
									anotherPosSet.contains(new Position(pos.getyIndex() + 1, pos.getxIndex())) ||
									anotherPosSet.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 壁なし確定マスをつなげる。数字を2個以上取り込んだらfalseを返す。
		 */
		private boolean setContinuePosSetContainsDoubleNumber(Position pos, Set<Position> continuePosSet,
				Direction from, int number) {
			if (continuePosSet.size() > 4 || (number != 0 && continuePosSet.size() > number)) {
				return false;
			}
			if (pos.getyIndex() != 0 && (from == Direction.DOWN || from == null)) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (number != 0) {
							return false;
						} else {
							continuePosSet.add(nextPos);
							if (!setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.DOWN,
									numbers[nextPos.getyIndex()][nextPos.getxIndex()])) {
								return false;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (!setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.DOWN, number)) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && (from == Direction.LEFT || from == null)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (number != 0) {
							return false;
						} else {
							continuePosSet.add(nextPos);
							if (!setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.LEFT,
									numbers[nextPos.getyIndex()][nextPos.getxIndex()])) {
								return false;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (!setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.LEFT, number)) {
							return false;
						}
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && (from == Direction.UP || from == null)) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (number != 0) {
							return false;
						} else {
							continuePosSet.add(nextPos);
							if (!setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.UP,
									numbers[nextPos.getyIndex()][nextPos.getxIndex()])) {
								return false;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (!setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.UP, number)) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != 0 && (from == Direction.RIGHT || from == null)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (number != 0) {
							return false;
						} else {
							continuePosSet.add(nextPos);
							if (!setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.RIGHT,
									numbers[nextPos.getyIndex()][nextPos.getxIndex()])) {
								return false;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (!setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.RIGHT, number)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 壁未確定マスをつなげる。
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet,
				Direction from, int number) {
			if (number != 0 && continuePosSet.size() >= number) {
				return true;
			}
			if (pos.getyIndex() != 0 && (from == Direction.DOWN || from == null)) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (number != 0) {
							//
						} else {
							continuePosSet.add(nextPos);
							if (setContinuePosSet(nextPos, continuePosSet, Direction.DOWN,
									numbers[nextPos.getyIndex()][nextPos.getxIndex()])) {
								return true;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (setContinuePosSet(nextPos, continuePosSet, Direction.DOWN, number)) {
							return true;
						}
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && (from == Direction.LEFT || from == null)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (number != 0) {
							//
						} else {
							continuePosSet.add(nextPos);
							if (setContinuePosSet(nextPos, continuePosSet, Direction.LEFT,
									numbers[nextPos.getyIndex()][nextPos.getxIndex()])) {
								return true;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (setContinuePosSet(nextPos, continuePosSet, Direction.LEFT, number)) {
							return true;
						}
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && (from == Direction.UP || from == null)) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (number != 0) {
							//
						} else {
							continuePosSet.add(nextPos);
							if (setContinuePosSet(nextPos, continuePosSet, Direction.UP,
									numbers[nextPos.getyIndex()][nextPos.getxIndex()])) {
								return true;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (setContinuePosSet(nextPos, continuePosSet, Direction.UP, number)) {
							return true;
						}
					}
				}
			}
			if (pos.getxIndex() != 0 && (from == Direction.RIGHT || from == null)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (number != 0) {
							//
						} else {
							continuePosSet.add(nextPos);
							if (setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT,
									numbers[nextPos.getyIndex()][nextPos.getxIndex()])) {
								return true;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT, number)) {
							return true;
						}
					}
				}
			}
			return number == 0;
		}

		/**
		 * 壁の数は2個(直進)または3個以上になる。
		 */
		private boolean wallSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int notExists = 0;
					Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					if (wallUp == Wall.NOT_EXISTS) {
						notExists++;
					}
					Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					if (wallRight == Wall.NOT_EXISTS) {
						notExists++;
					}
					Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					if (wallDown == Wall.NOT_EXISTS) {
						notExists++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (notExists > 2) {
						return false;
					}
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
				}
			}
			return true;
		}

		/**
		 * 数字は直進マスの長さを示す。
		 */
		public boolean numberSolve() {
			List<List<Position>> fixedPosListList = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						List<Position> fixedPosList = new ArrayList<>();
						fixedPosList.add(new Position(yIndex, xIndex));
						int size = numbers[yIndex][xIndex];
						int fixUpCnt = 0;
						int fixRightCnt = 0;
						int fixDownCnt = 0;
						int fixLeftCnt = 0;
						int upCnt = 0;
						int rightCnt = 0;
						int downCnt = 0;
						int leftCnt = 0;
						for (int candY = yIndex - 1; candY >= 0; candY--) {
							if (tateWall[candY][xIndex] != Wall.EXISTS && numbers[candY][xIndex] == null) {
								upCnt++;
							} else {
								break;
							}
						}
						for (int candX = xIndex + 1; candX < getXLength(); candX++) {
							if (yokoWall[yIndex][candX - 1] != Wall.EXISTS && numbers[yIndex][candX] == null) {
								rightCnt++;
							} else {
								break;
							}
						}
						for (int candY = yIndex + 1; candY < getYLength(); candY++) {
							if (tateWall[candY - 1][xIndex] != Wall.EXISTS && numbers[candY][xIndex] == null) {
								downCnt++;
							} else {
								break;
							}
						}
						for (int candX = xIndex - 1; candX >= 0; candX--) {
							if (yokoWall[yIndex][candX] != Wall.EXISTS && numbers[yIndex][candX] == null) {
								leftCnt++;
							} else {
								break;
							}
						}
						for (int candY = yIndex - 1; candY >= 0; candY--) {
							if (tateWall[candY][xIndex] == Wall.NOT_EXISTS && numbers[candY][xIndex] == null) {
								fixedPosList.add(new Position(candY, xIndex));
								fixUpCnt++;
							} else {
								break;
							}
						}
						for (int candX = xIndex + 1; candX < getXLength(); candX++) {
							if (yokoWall[yIndex][candX - 1] == Wall.NOT_EXISTS && numbers[yIndex][candX] == null) {
								fixedPosList.add(new Position(yIndex, candX));
								fixRightCnt++;
							} else {
								break;
							}
						}
						for (int candY = yIndex + 1; candY < getYLength(); candY++) {
							if (tateWall[candY - 1][xIndex] == Wall.NOT_EXISTS && numbers[candY][xIndex] == null) {
								fixedPosList.add(new Position(candY, xIndex));
								fixDownCnt++;
							} else {
								break;
							}
						}
						for (int candX = xIndex - 1; candX >= 0; candX--) {
							if (yokoWall[yIndex][candX] == Wall.NOT_EXISTS && numbers[yIndex][candX] == null) {
								fixedPosList.add(new Position(yIndex, candX));
								fixLeftCnt++;
							} else {
								break;
							}
						}
						// 必ず縦につながる数
						int varticalFix = fixUpCnt + fixDownCnt;
						// 必ず横につながる数
						int horizonalFix = fixRightCnt + fixLeftCnt;
						// 縦につながることができる数
						int varticalCapacity = upCnt + downCnt;
						// 横につながることができる数
						int horizonalCapacity = rightCnt + leftCnt;
						if (varticalFix + 1 > size || horizonalFix + 1 > size) {
							return false;
						}
						if (varticalCapacity + 1 < size
								&& horizonalCapacity + 1 < size) {
							return false;
						}
						if (varticalFix == varticalCapacity && horizonalFix == horizonalCapacity) {
							fixedPosListList.add(fixedPosList);
						}
					}
				}
			}
			// 同じ長さの畳の隣接チェック
			for (List<Position> posList : fixedPosListList) {
				for (List<Position> anotherPosList : fixedPosListList) {
					if (posList != anotherPosList && posList.size() == anotherPosList.size()) {
						for (Position pos : posList) {
							if (anotherPosList.contains(new Position(pos.getyIndex() - 1, pos.getxIndex())) ||
									anotherPosList.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1)) ||
									anotherPosList.contains(new Position(pos.getyIndex() + 1, pos.getxIndex())) ||
									anotherPosList.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		private boolean sameSizeSolve() {
			// TODO 自動生成されたメソッド・スタブ
			return true;
		}

		/**
		 * 柱から伸びる壁は0、2、3のいずれか。
		 * 違反する場合はfalseを返す。
		 */
		private boolean pileSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
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
					// 壁枚数は1、4以外
					if ((exists == 1 && notExists == 3) || (exists == 4)) {
						return false;
					} else if (notExists == 3) {
						// 壁枚数0確定
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
					} else if (notExists == 2 && exists == 1) {
						// 壁枚数2確定
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
					} else if (exists == 3) {
						// 壁枚数3確定
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
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!wallSolve()) {
				return false;
			}
			if (!pileSolve()) {
				return false;
			}
			if (!roomSolve()) {
				return false;
			}
			//			if (!numberSolve()) {
			//				return false;
			//			}
			if (!sameSizeSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
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

	private final Field field;
	private int count;

	public FillmatSolver(int height, int width, String param) {
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
		System.out.println(new FillmatSolver(height, width, param).solve());
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
				System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
				System.out.println(field);
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count / 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 5).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
		System.out.println(field);
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
			field.numbers = virtual2.numbers;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.numbers = virtual.numbers;
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
			field.numbers = virtual2.numbers;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.numbers = virtual.numbers;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}
}
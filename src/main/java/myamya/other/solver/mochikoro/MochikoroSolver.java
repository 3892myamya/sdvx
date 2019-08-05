package myamya.other.solver.mochikoro;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class MochikoroSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private final Integer[][] numbers;
		// 確定した部屋の位置情報。再調査しないことでスピードアップ
		private Set<Position> fixedPosSet;

		public Masu[][] getMasu() {
			return masu;
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
			fixedPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
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
					if (ch == '.') {
						Position pos = new Position(index / getXLength(), index % getXLength());
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
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
						Position pos = new Position(index / getXLength(), index % getXLength());
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
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
			fixedPosSet = new HashSet<>(other.fixedPosSet);
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
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
		 * 部屋のサイズが超過していたり、別部屋と繋がっていた場合、falseを返す。
		 * 部屋が既定サイズに到達している場合、周囲を黒で埋める。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						Position pivot = new Position(yIndex, xIndex);
						if (fixedPosSet.contains(pivot)) {
							continue;
						}
						Set<Position> continueNotBlackPosSet = new HashSet<>();
						continueNotBlackPosSet.add(pivot);
						if (!setContinueNotBlackPosSet(numbers[yIndex][xIndex], pivot, continueNotBlackPosSet, null)) {
							// サイズ不足
							return false;
						}
						Set<Position> continueWhitePosSet = new HashSet<>();
						continueWhitePosSet.add(pivot);
						if (!setContinueWhitePosSet2(numbers[yIndex][xIndex], pivot, continueWhitePosSet, null)) {
							// 別部屋と連結またはサイズ超過
							return false;
						}
						if (numbers[yIndex][xIndex] == continueWhitePosSet.size()) {
							fixedPosSet.addAll(continueWhitePosSet);
							for (Position pos : continueWhitePosSet) {
								if (pos.getyIndex() != 0) {
									if (masu[pos.getyIndex() - 1][pos.getxIndex()] == Masu.SPACE) {
										masu[pos.getyIndex() - 1][pos.getxIndex()] = Masu.BLACK;
									}
								}
								if (pos.getxIndex() != getXLength() - 1) {
									if (masu[pos.getyIndex()][pos.getxIndex() + 1] == Masu.SPACE) {
										masu[pos.getyIndex()][pos.getxIndex() + 1] = Masu.BLACK;
									}
								}
								if (pos.getyIndex() != getYLength() - 1) {
									if (masu[pos.getyIndex() + 1][pos.getxIndex()] == Masu.SPACE) {
										masu[pos.getyIndex() + 1][pos.getxIndex()] = Masu.BLACK;
									}
								}
								if (pos.getxIndex() != 0) {
									if (masu[pos.getyIndex()][pos.getxIndex() - 1] == Masu.SPACE) {
										masu[pos.getyIndex()][pos.getxIndex() - 1] = Masu.BLACK;
									}
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定でないマスをつなげていくが、
		 * 繋げたマスの前後左右(自分が元いたマス以外)に数字マスを発見した場合はつながない。
		 * サイズが不足しないと分かった時点でtrueを返す。
		 */
		private boolean setContinueNotBlackPosSet(int size, Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (continuePosSet.size() >= size) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					Integer numberUp = nextPos.getyIndex() == 0 ? null
							: numbers[nextPos.getyIndex() - 1][nextPos.getxIndex()];
					Integer numberRight = nextPos.getxIndex() == getXLength() - 1 ? null
							: numbers[nextPos.getyIndex()][nextPos.getxIndex() + 1];
					Integer numberLeft = nextPos.getxIndex() == 0 ? null
							: numbers[nextPos.getyIndex()][nextPos.getxIndex() - 1];
					if (numberUp != null || numberRight != null || numberLeft != null) {
						// つながない
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
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					Integer numberUp = nextPos.getyIndex() == 0 ? null
							: numbers[nextPos.getyIndex() - 1][nextPos.getxIndex()];
					Integer numberRight = nextPos.getxIndex() == getXLength() - 1 ? null
							: numbers[nextPos.getyIndex()][nextPos.getxIndex() + 1];
					Integer numberDown = nextPos.getyIndex() == getYLength() - 1 ? null
							: numbers[nextPos.getyIndex() + 1][nextPos.getxIndex()];
					if (numberUp != null || numberRight != null || numberDown != null) {
						// つながない
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
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					Integer numberRight = nextPos.getxIndex() == getXLength() - 1 ? null
							: numbers[nextPos.getyIndex()][nextPos.getxIndex() + 1];
					Integer numberDown = nextPos.getyIndex() == getYLength() - 1 ? null
							: numbers[nextPos.getyIndex() + 1][nextPos.getxIndex()];
					Integer numberLeft = nextPos.getxIndex() == 0 ? null
							: numbers[nextPos.getyIndex()][nextPos.getxIndex() - 1];
					if (numberRight != null || numberDown != null || numberLeft != null) {
						// つながない
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
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					Integer numberUp = nextPos.getyIndex() == 0 ? null
							: numbers[nextPos.getyIndex() - 1][nextPos.getxIndex()];
					Integer numberDown = nextPos.getyIndex() == getYLength() - 1 ? null
							: numbers[nextPos.getyIndex() + 1][nextPos.getxIndex()];
					Integer numberLeft = nextPos.getxIndex() == 0 ? null
							: numbers[nextPos.getyIndex()][nextPos.getxIndex() - 1];
					if (numberUp != null || numberDown != null || numberLeft != null) {
						// つながない
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
		 * posを起点に上下左右に白確定のマスを無制限につなぎ、違う数字にたどり着いたり
		 * 想定サイズを超過したらfalseを返す。
		 */
		private boolean setContinueWhitePosSet2(Integer size, Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet2(size, nextPos, continuePosSet, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet2(size, nextPos, continuePosSet, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet2(size, nextPos, continuePosSet, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet2(size, nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 置くと池ができるマスを白マスにする。
		 * 既に池ができている場合falseを返す。
		 */
		public boolean pondSolve() {
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
		 * 餅は長方形になるように配置する。
		 * 長方形にできなかった場合はfalseを返す。
		 */
		public boolean rectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						whitePosSet.add(new Position(yIndex, xIndex));
					}
				}
			}
			while (!whitePosSet.isEmpty()) {
				Position typicalWhitePos = new ArrayList<>(whitePosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				setContinueWhitePosSet(typicalWhitePos, continuePosSet, null);
				int minY = getYLength() - 1;
				int maxY = 0;
				int minX = getXLength() - 1;
				int maxX = 0;
				for (Position pos : continuePosSet) {
					if (pos.getyIndex() < minY) {
						minY = pos.getyIndex();
					}
					if (pos.getyIndex() > maxY) {
						maxY = pos.getyIndex();
					}
					if (pos.getxIndex() < minX) {
						minX = pos.getxIndex();
					}
					if (pos.getxIndex() > maxX) {
						maxX = pos.getxIndex();
					}
				}
				// TODO 餅の幅・高さが決まっていたらそこから確定するとか、
				// 餅サイズが素数で2*2以上だったらダメとかその辺のロジック入れれば強くなりそう
				int number = 0;
				for (int yIndex = minY; yIndex <= maxY; yIndex++) {
					for (int xIndex = minX; xIndex <= maxX; xIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							return false;
						}
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
						if (numbers[yIndex][xIndex] != null) {
							if (number != 0) {
								return false;
							}
							number = numbers[yIndex][xIndex];
						}
					}
				}
				// 餅が数字を含む場合、このまま餅を膨らませて目的の数を満たせるか調査
				if (number != 0) {
					// 最大でどこまで膨らむか
					int maxYsize = getYLength();
					int maxXsize = getXLength();
					for (int candY = minY; candY <= maxY; candY++) {
						int hukurami = 0;
						int targetX = minX - 1;
						while (targetX >= 0 && masu[candY][targetX] != Masu.BLACK) {
							targetX--;
							hukurami++;
						}
						targetX = maxX + 1;
						while (targetX < getXLength() && masu[candY][targetX] != Masu.BLACK) {
							targetX++;
							hukurami++;
						}
						if (maxX - minX + 1 + hukurami < maxXsize) {
							maxXsize = maxX - minX + 1 + hukurami;
						}
					}
					for (int candX = minX; candX <= maxX; candX++) {
						int hukurami = 0;
						int targetY = minY - 1;
						while (targetY >= 0 && masu[targetY][candX] != Masu.BLACK) {
							targetY--;
							hukurami++;
						}
						targetY = maxY + 1;
						while (targetY < getYLength() && masu[targetY][candX] != Masu.BLACK) {
							targetY++;
							hukurami++;
						}
						if (maxY - minY + 1 + hukurami < maxYsize) {
							maxYsize = maxY - minY + 1 + hukurami;
						}
					}
					// 膨らむ候補との突合せ
					boolean isOkMochi = false;
					for (int candY = maxY - minY + 1; candY <= maxYsize; candY++) {
						for (int candX = maxX - minX + 1; candX <= maxXsize; candX++) {
							if (candY * candX == number) {
								isOkMochi = true;
								break;
							}
							if (candY * candX > number) {
								break;
							}
						}
						if (isOkMochi) {
							break;
						}
					}
					if (!isOkMochi) {
						return false;
					}
				}

				whitePosSet.removeAll(continuePosSet);
			}
			return true;
		}

		/**
		 * posを起点に上下左右に白確定マスを無制限につなげていく。
		 */
		private void setContinueWhitePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 餅を斜めにたどってもひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
							whitePosSet.add(whitePos);
							setContinueDiagonalPosSet(whitePos, whitePosSet, null);
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
		 * posを起点に上下左右ななめに黒確定でないマスを無制限につなげていく。
		 */
		private void setContinueDiagonalPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
			if (!rectSolve()) {
				return false;
			}
			if (!connectSolve()) {
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
	private int count = 0;

	public MochikoroSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "http://pzv.jp/p.html?mochikoro/8/9/h4l4j4w4k4p4s4g4l"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new MochikoroSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 2));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 2).toString();
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
		}

		return true;
	}
}
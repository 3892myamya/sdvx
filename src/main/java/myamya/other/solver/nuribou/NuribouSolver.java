package myamya.other.solver.nuribou;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class NuribouSolver implements Solver {

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
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
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
			farSolve();
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
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
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
						if (!setContinueWhitePosSet(numbers[yIndex][xIndex], pivot, continueWhitePosSet, null)) {
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
		 * 黒マスは必ず棒状になる。ならない場合falseを返す。
		 */
		public boolean stickSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Masu pivot = masu[yIndex][xIndex];
					Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
					Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
					Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
					Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
					if (pivot == Masu.SPACE) {
						if ((masuUp == Masu.BLACK || masuDown == Masu.BLACK)
								&& (masuRight == Masu.BLACK || masuLeft == Masu.BLACK)) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (pivot == Masu.BLACK) {
						if ((masuUp == Masu.BLACK || masuDown == Masu.BLACK)
								&& (masuRight == Masu.BLACK || masuLeft == Masu.BLACK)) {
							return false;
						}
						if (masuUp == Masu.BLACK || masuDown == Masu.BLACK) {
							if (masuRight == Masu.SPACE) {
								masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							}
							if (masuLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
						if (masuRight == Masu.BLACK || masuLeft == Masu.BLACK) {
							if (masuUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masuDown == Masu.SPACE) {
								masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 同じ長さの棒は角を共有しない。
		 */
		public boolean cornerSolve() {
			Set<Position> alreadyServeyPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position pos = new Position(yIndex, xIndex);
						if (alreadyServeyPosSet.contains(pos)) {
							continue;
						}
						Set<Position> continueBlackPosSet = new HashSet<>();
						continueBlackPosSet.add(pos);
						if (setContinueBlackPosSet(pos, continueBlackPosSet, null)) {
							// サイズが決まっている棒
							for (Position continueBlackPos : continueBlackPosSet) {
								Masu masuUpRight = continueBlackPos.getyIndex() == 0
										|| continueBlackPos.getxIndex() == getXLength() - 1 ? Masu.NOT_BLACK
												: masu[continueBlackPos.getyIndex() - 1][continueBlackPos.getxIndex()
														+ 1];
								Masu masuRightDown = continueBlackPos.getxIndex() == getXLength() - 1
										|| continueBlackPos.getyIndex() == getYLength() - 1
												? Masu.NOT_BLACK
												: masu[continueBlackPos.getyIndex() + 1][continueBlackPos.getxIndex()
														+ 1];
								Masu masuDownLeft = continueBlackPos.getyIndex() == getYLength() - 1
										|| continueBlackPos.getxIndex() == 0
												? Masu.NOT_BLACK
												: masu[continueBlackPos.getyIndex() + 1][continueBlackPos.getxIndex()
														- 1];
								Masu masuLeftUp = continueBlackPos.getxIndex() == 0 || yIndex == 0 ? Masu.NOT_BLACK
										: masu[continueBlackPos.getyIndex() - 1][continueBlackPos.getxIndex() - 1];
								if (masuUpRight == Masu.BLACK) {
									Position targetPos = new Position(continueBlackPos.getyIndex() - 1,
											continueBlackPos.getxIndex() + 1);
									if (!alreadyServeyPosSet.contains(targetPos)) {
										Set<Position> continueBlackTargetPosSet = new HashSet<>();
										continueBlackTargetPosSet.add(targetPos);
										if (setContinueBlackPosSet(targetPos, continueBlackTargetPosSet, null)) {
											if (continueBlackTargetPosSet.size() == continueBlackPosSet.size()) {
												return false;
											}
										}
									}
								}
								if (masuRightDown == Masu.BLACK) {
									Position targetPos = new Position(continueBlackPos.getyIndex() + 1,
											continueBlackPos.getxIndex() + 1);
									if (!alreadyServeyPosSet.contains(targetPos)) {
										Set<Position> continueBlackTargetPosSet = new HashSet<>();
										continueBlackTargetPosSet.add(targetPos);
										if (setContinueBlackPosSet(targetPos, continueBlackTargetPosSet, null)) {
											if (continueBlackTargetPosSet.size() == continueBlackPosSet.size()) {
												return false;
											}
										}
									}
								}
								if (masuDownLeft == Masu.BLACK) {
									Position targetPos = new Position(continueBlackPos.getyIndex() + 1,
											continueBlackPos.getxIndex() - 1);
									if (!alreadyServeyPosSet.contains(targetPos)) {
										Set<Position> continueBlackTargetPosSet = new HashSet<>();
										continueBlackTargetPosSet.add(targetPos);
										if (setContinueBlackPosSet(targetPos, continueBlackTargetPosSet, null)) {
											if (continueBlackTargetPosSet.size() == continueBlackPosSet.size()) {
												return false;
											}
										}
									}
								}
								if (masuLeftUp == Masu.BLACK) {
									Position targetPos = new Position(continueBlackPos.getyIndex() - 1,
											continueBlackPos.getxIndex() - 1);
									if (!alreadyServeyPosSet.contains(targetPos)) {
										Set<Position> continueBlackTargetPosSet = new HashSet<>();
										continueBlackTargetPosSet.add(targetPos);
										if (setContinueBlackPosSet(targetPos, continueBlackTargetPosSet, null)) {
											if (continueBlackTargetPosSet.size() == continueBlackPosSet.size()) {
												return false;
											}
										}
									}
								}
							}
						}
						alreadyServeyPosSet.addAll(continueBlackPosSet);
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定のマスを無制限につなぐが、
		 * 不確定マスに隣接したらfalseを返す。
		 */
		private boolean setContinueBlackPosSet(Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)) {
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE) {
						return false;
					} else if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
						continuePosSet.add(nextPos);
						if (!setContinueBlackPosSet(nextPos, continuePosSet, Direction.DOWN)) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)) {
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE) {
						return false;
					} else if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
						continuePosSet.add(nextPos);
						if (!setContinueBlackPosSet(nextPos, continuePosSet, Direction.LEFT)) {
							return false;
						}
					}
				}
			}
			if (pos.getyIndex() !=

			getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)) {
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE) {
						return false;
					} else if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
						continuePosSet.add(nextPos);
						if (!setContinueBlackPosSet(nextPos, continuePosSet, Direction.UP)) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)) {
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE) {
						return false;
					} else if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
						continuePosSet.add(nextPos);
						if (!setContinueBlackPosSet(nextPos, continuePosSet, Direction.RIGHT)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に白確定のマスを無制限につなぎ、違う数字にたどり着いたり
		 * 想定サイズを超過したらfalseを返す。
		 */
		private boolean setContinueWhitePosSet(Integer size, Position pos, Set<Position> continuePosSet,
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
					if (!setContinueWhitePosSet(size, nextPos, continuePosSet, Direction.DOWN)) {
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
					if (!setContinueWhitePosSet(size, nextPos, continuePosSet, Direction.LEFT)) {
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
					if (!setContinueWhitePosSet(size, nextPos, continuePosSet, Direction.UP)) {
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
					if (!setContinueWhitePosSet(size, nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていくが、
		 * 数字を見つけた時点でtrueを返す。
		 */
		private boolean setContinueNotBlackPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueNotBlackPosSet(nextPos, continuePosSet, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueNotBlackPosSet(nextPos, continuePosSet, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueNotBlackPosSet(nextPos, continuePosSet, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueNotBlackPosSet(nextPos, continuePosSet, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * 遠すぎてどこからも届かないマスを黒にする。
		 * 該当マスが既に白だった場合Falseを返す。
		 * memo:大きな数字がやたらと多い盤面の場合はやらない方が早い場合がある。
		 */
		public boolean farSolve() {
			Set<Position> allPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pivot);
						setContinuePosSetUseDistance(new HashSet<>(continuePosSet), continuePosSet,
								numbers[yIndex][xIndex] - 1);
						allPosSet.addAll(continuePosSet);
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (!allPosSet.contains(new Position(yIndex, xIndex))) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.BLACK;
						} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字が入らない孤立するマスができてはならない。
		 */
		private boolean notStandAloneSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null && masu[yIndex][xIndex] == Masu.NOT_BLACK
							&& !fixedPosSet.contains(new Position(yIndex, xIndex))) {
						whitePosSet.add(new Position(yIndex, xIndex));
					}
				}
			}
			while (!whitePosSet.isEmpty()) {
				Position typicalWhitePos = new ArrayList<>(whitePosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				if (!setContinueNotBlackPosSet(typicalWhitePos, continuePosSet, null)) {
					return false;
				}
				whitePosSet.removeAll(continuePosSet);
			}
			return true;
		}

		/**
		 * pivotPosSetを起点に上下左右に黒確定でないマスをpivotPosSetからのdistanceだけつなげていく。
		 * memo:distanceが小さい(4ぐらい)の場合はpivotSetを用意しない方が早い場合がある。
		 */
		private void setContinuePosSetUseDistance(Set<Position> pivotPosSet, Set<Position> continuePosSet,
				int distance) {
			if (distance == 0 || pivotPosSet.isEmpty()) {
				return;
			}
			Set<Position> nextPivotPosSet = new HashSet<>();
			for (Position pos : pivotPosSet) {
				if (pos.getyIndex() != 0) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					if (!continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
						nextPivotPosSet.add(nextPos);
						continuePosSet.add(nextPos);
					}
				}
				if (pos.getxIndex() != getXLength() - 1) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					if (!continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
						nextPivotPosSet.add(nextPos);
						continuePosSet.add(nextPos);
					}
				}
				if (pos.getyIndex() != getYLength() - 1) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					if (!continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
						nextPivotPosSet.add(nextPos);
						continuePosSet.add(nextPos);
					}
				}
				if (pos.getxIndex() != 0) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					if (!continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
						nextPivotPosSet.add(nextPos);
						continuePosSet.add(nextPos);
					}
				}
			}
			setContinuePosSetUseDistance(nextPivotPosSet, continuePosSet, distance - 1);
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
			if (!stickSolve()) {
				return false;
			}
			if (!notStandAloneSolve()) {
				return false;
			}
			if (!whiteCountSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!cornerSolve()) {
					return false;
				}
			}
			return true;
		}

		private boolean whiteCountSolve() {
			int fixWhiteCount = 0;
			int whiteCnt = 0;
			int blackCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						whiteCnt++;
					} else if (masu[yIndex][xIndex] == Masu.BLACK) {
						blackCnt++;
					}
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] == -1) {
							return true;
						} else {
							fixWhiteCount = fixWhiteCount + numbers[yIndex][xIndex];
						}
					}
				}
			}
			int fixBlackCount = getYLength() * getXLength() - fixWhiteCount;
			if (fixWhiteCount < whiteCnt) {
				return false;
			}
			if (fixWhiteCount == whiteCnt) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
			}
			if (fixBlackCount < blackCnt) {
				return false;
			}
			if (fixBlackCount == blackCnt) {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
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
	private int count;

	public NuribouSolver(int height, int width, String param) {
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
		System.out.println(new NuribouSolver(height, width, param).solve());
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
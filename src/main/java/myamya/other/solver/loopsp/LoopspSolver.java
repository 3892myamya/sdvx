package myamya.other.solver.loopsp;

import java.util.HashSet;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class LoopspSolver implements Solver {
	public static class Field {
		static final String ALPHABET_FROM_N = "nopqrstuvwxyz";
		// 数字の情報
		private final Integer[][] numbers;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return yokoWall.length;
		}

		public int getXLength() {
			return tateWall[0].length;
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
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_N.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					Position pos = new Position(index / getXLength(), index % getXLength());
					if (ch == 'g') {
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
					} else if (ch == 'h') {
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
					} else if (ch == 'i') {
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
					} else if (ch == 'j') {
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
					} else if (ch == 'k') {
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
					} else if (ch == 'l') {
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
					} else if (ch == 'm') {
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
					} else {
						//16 - 255は '-'
						//256 - 999は '+'
						int capacity;
						if (ch == '.') {
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
							numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
						}
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
		 * 数字マス以外では壁の数は0か2、数字マスでは壁の数は2になる。違反の場合false。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
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
					if (numbers[yIndex][xIndex] != null) {
						// 数字マス
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
					} else {
						// 数字マス以外
						if (existsCount > 2 || (notExistsCount == 3 && existsCount == 1)) {
							return false;
						}
						if (existsCount == 2) {
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
						} else if (existsCount == 1 && notExistsCount == 2) {
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
						} else if (notExistsCount == 3) {
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
			return true;
		}

		/**
		 * 同じ数字の丸は同じループ構造の中に含まれるようにする。
		 * 以下の場合はfalseを返す。
		 * ・別の数字の丸が同じループになる。
		 * ・同じ数字の丸が別のループになる。
		 * ・丸が含まれない。
		 */
		public boolean connectSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// TODO ここのコメントアウトを外せば早くできるが孤立マスがあった時に詰む…
					//										if (numbers[yIndex][xIndex] == null) {
					//											continue;
					//										}
					Position originPos = new Position(yIndex, xIndex);
					// 交差点の可能性があるマスから開始しないようにする
					int existsCount = 0;
					Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					}
					Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					}
					Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					}
					if (existsCount == 0) {
						continue;
					}
					int number = numbers[yIndex][xIndex] == null ? 0 : numbers[yIndex][xIndex];
					Set<Position> continuePosSet = new HashSet<>();
					continuePosSet.add(originPos);
					if (!setContinuePosSet(number, originPos, originPos, continuePosSet, null)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に壁なし確定のマスを、直線を優先してつないでいく。
		 * 出発マスまで戻ってきてnumberの数字をすべて回収できた場合や、
		 * 進む方向が確定できなかった場合はtrueを返す。
		 * 出発マスまで戻る前に別の数字に接続した場合はfalseを返す。
		 */
		private boolean setContinuePosSet(int number, Position originPos, Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (pos.getyIndex() != 0 && from == Direction.DOWN
					&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS) {
				// 直進して上へ
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
					if (number <= 0) {
						number = numbers[nextPos.getyIndex()][nextPos.getxIndex()];
					} else if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != number) {
						return false;
					}
				}
				if (originPos.equals(nextPos)) {
					return isAllCollect(number, continuePosSet);
				}
				continuePosSet.add(nextPos);
				return setContinuePosSet(number, originPos, nextPos, continuePosSet, Direction.DOWN);
			} else if (pos.getxIndex() != getXLength() - 1 && from == Direction.LEFT
					&& yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
				// 直進して右へ
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
					if (number <= 0) {
						number = numbers[nextPos.getyIndex()][nextPos.getxIndex()];
					} else if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != number) {
						return false;
					}
				}
				if (originPos.equals(nextPos)) {
					return isAllCollect(number, continuePosSet);
				}
				continuePosSet.add(nextPos);
				return setContinuePosSet(number, originPos, nextPos, continuePosSet, Direction.LEFT);
			} else if (pos.getyIndex() != getYLength() - 1 && from == Direction.UP
					&& tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
				// 直進して下へ
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
					if (number <= 0) {
						number = numbers[nextPos.getyIndex()][nextPos.getxIndex()];
					} else if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != number) {
						return false;
					}
				}
				if (originPos.equals(nextPos)) {
					return isAllCollect(number, continuePosSet);
				}
				continuePosSet.add(nextPos);
				return setContinuePosSet(number, originPos, nextPos, continuePosSet, Direction.UP);
			} else if (pos.getxIndex() != 0 && from == Direction.RIGHT
					&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS) {
				// 直進して左へ
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
					if (number <= 0) {
						number = numbers[nextPos.getyIndex()][nextPos.getxIndex()];
					} else if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != number) {
						return false;
					}
				}
				if (originPos.equals(nextPos)) {
					return isAllCollect(number, continuePosSet);
				}
				continuePosSet.add(nextPos);
				return setContinuePosSet(number, originPos, nextPos, continuePosSet, Direction.RIGHT);
			} else {
				// 直進可能性判定
				// 直進できる可能性が残っているときはtrueにする
				if (pos.getyIndex() != 0 && from == Direction.DOWN
						&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS) {
					return true;
				} else if (pos.getxIndex() != getXLength() - 1 && from == Direction.LEFT
						&& yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS) {
					return true;
				} else if (pos.getyIndex() != getYLength() - 1 && from == Direction.UP
						&& tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS) {
					return true;
				} else if (pos.getxIndex() != 0 && from == Direction.RIGHT
						&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS) {
					return true;
				} else {
					// 直進不可
					if (pos.getyIndex() != 0 && from != Direction.UP
							&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS) {
						// カーブして上へ
						Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
						if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
							if (number <= 0) {
								number = numbers[nextPos.getyIndex()][nextPos.getxIndex()];
							} else if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
									&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != number) {
								return false;
							}
						}
						if (originPos.equals(nextPos)) {
							return isAllCollect(number, continuePosSet);
						}
						continuePosSet.add(nextPos);
						return setContinuePosSet(number, originPos, nextPos, continuePosSet, Direction.DOWN);
					} else if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT
							&& yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
						// カーブして右へ
						Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
						if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
							if (number <= 0) {
								number = numbers[nextPos.getyIndex()][nextPos.getxIndex()];
							} else if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
									&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != number) {
								return false;
							}
						}
						if (originPos.equals(nextPos)) {
							return isAllCollect(number, continuePosSet);
						}
						continuePosSet.add(nextPos);
						return setContinuePosSet(number, originPos, nextPos, continuePosSet, Direction.LEFT);
					} else if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN
							&& tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
						// カーブして下へ
						Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
						if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
							if (number <= 0) {
								number = numbers[nextPos.getyIndex()][nextPos.getxIndex()];
							} else if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
									&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != number) {
								return false;
							}
						}
						if (originPos.equals(nextPos)) {
							return isAllCollect(number, continuePosSet);
						}
						continuePosSet.add(nextPos);
						return setContinuePosSet(number, originPos, nextPos, continuePosSet, Direction.UP);
					} else if (pos.getxIndex() != 0 && from != Direction.LEFT
							&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS) {
						// カーブして左へ
						Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
						if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
							if (number <= 0) {
								number = numbers[nextPos.getyIndex()][nextPos.getxIndex()];
							} else if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
									&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != number) {
								return false;
							}
						}
						if (originPos.equals(nextPos)) {
							return isAllCollect(number, continuePosSet);
						}
						continuePosSet.add(nextPos);
						return setContinuePosSet(number, originPos, nextPos, continuePosSet, Direction.RIGHT);
					}
					return true;
				}
			}
		}

		/**
		 * ループができたとき、ある数字に対する全回収が終わってなかったり、
		 * 1つも数字を含まなかった場合にfalseを返す。
		 */
		private boolean isAllCollect(int number, Set<Position> continuePosSet) {
			if (number == 0) {
				return false;
			}
			if (number != -1) {
				for (int targetY = 0; targetY < getYLength(); targetY++) {
					for (int targetX = 0; targetX < getXLength(); targetX++) {
						if (numbers[targetY][targetX] != null && numbers[targetY][targetX] == number) {
							if (!continuePosSet.contains(new Position(targetY, targetX))) {
								return false;
							}
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
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				int notExistsCount = 0;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
				int notExistsCount = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			// 環状線スペシャルは随時進行しないほうが早い
			//			String str = getStateDump();
			//			if (!getStateDump().equals(str)) {
			//				return solveAndCheck();
			//			} else {
			if (!nextSolve()) {
				return false;
			}
			if (!oddSolve()) {
				return false;
			}
			if (!connectSolve()) {
				return false;
			}
			//			}
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
	private int count = 0;

	public LoopspSolver(int height, int width, String param) {
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
		System.out.println(new LoopspSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 5).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		// 環状線スペシャルは随時進行しないほうが早い
		//		String str = field.getStateDump();
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
		//		if (!field.getStateDump().equals(str)) {
		//			return candSolve(field, recursive);
		//		}
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}
}

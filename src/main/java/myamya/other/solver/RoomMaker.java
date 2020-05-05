package myamya.other.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Common.Wall;

public class RoomMaker {

	public static class RoomMaker2 {
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final Wall[][] tateWall;

		public int getYLength() {
			return yokoWall.length;
		}

		public int getXLength() {
			return tateWall[0].length;
		}

		public boolean[][] getYokoWall() {
			boolean[][] result = new boolean[getYLength()][getXLength() - 1];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					result[yIndex][xIndex] = yokoWall[yIndex][xIndex] == Wall.EXISTS;
				}
			}
			return result;
		}

		public boolean[][] getTateWall() {
			boolean[][] result = new boolean[getYLength() - 1][getXLength()];
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					result[yIndex][xIndex] = tateWall[yIndex][xIndex] == Wall.EXISTS;
				}
			}
			return result;
		}

		/**
		 * オブジェクト生成と同時に部屋作成。
		 * num / denom が部屋の仕切り数の割合。1:1なら全部サイズ1の部屋になり、0:1なら1部屋になる。
		 */
		public RoomMaker2(int height, int width, int num, int denom, boolean tatami) {
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			init();
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < (height * (width - 1)) + ((height - 1) * width); i++) {
				indexList.add(i);
			}
			Collections.shuffle(indexList);
			int index = 0;

			while (!isSolved()) {
				int posBase = indexList.get(index);
				boolean toYokoWall;
				int yIndex, xIndex;
				if (posBase < height * (width - 1)) {
					toYokoWall = true;
					yIndex = posBase / (width - 1);
					xIndex = posBase % (width - 1);
				} else {
					toYokoWall = false;
					posBase = posBase - (height * (width - 1));
					yIndex = posBase / width;
					xIndex = posBase % width;
				}
				if ((toYokoWall && yokoWall[yIndex][xIndex] == Wall.SPACE)
						|| (!toYokoWall && tateWall[yIndex][xIndex] == Wall.SPACE)) {
					if (Math.random() * denom < num) {
						if (toYokoWall) {
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						} else {
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
					} else {
						if (toYokoWall) {
							yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						} else {
							tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
					}
					//					if (!baseCheck()) {
					//						// 破綻したら0から作り直す。
					//						init();
					//						Collections.shuffle(indexList);
					//						index = 0;
					//						continue;
					//					}
					if (!rectCheck()) {
						// 破綻したら0から作り直す。
						init();
						Collections.shuffle(indexList);
						index = 0;
						continue;
					}
					if (tatami) {
						if (!tatamiCheck()) {
							// 破綻したら0から作り直す。
							init();
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
				}
				index++;
			}
		}

		private void init() {
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
		}

		/**
		 * 柱から見て壁の数が1の場合falseを返す。
		 */
		private boolean baseCheck() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Wall wallUp = yokoWall[yIndex][xIndex];
					Wall wallRight = tateWall[yIndex][xIndex + 1];
					Wall wallDown = yokoWall[yIndex + 1][xIndex];
					Wall wallLeft = tateWall[yIndex][xIndex];
					if (wallUp == Wall.NOT_EXISTS && wallRight == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS) {
						if (wallLeft == Wall.EXISTS) {
							return false;
						}
						tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
					}
					if (wallUp == Wall.EXISTS && wallRight == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS) {
						if (wallLeft == Wall.NOT_EXISTS) {
							return false;
						}
						tateWall[yIndex][xIndex] = Wall.EXISTS;
					}
					if (wallUp == Wall.NOT_EXISTS && wallRight == Wall.EXISTS && wallDown == Wall.NOT_EXISTS) {
						if (wallLeft == Wall.NOT_EXISTS) {
							return false;
						}
						tateWall[yIndex][xIndex] = Wall.EXISTS;
					}
					if (wallUp == Wall.NOT_EXISTS && wallRight == Wall.NOT_EXISTS && wallDown == Wall.EXISTS) {
						if (wallLeft == Wall.NOT_EXISTS) {
							return false;
						}
						tateWall[yIndex][xIndex] = Wall.EXISTS;
					}
					if (wallRight == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS) {
						if (wallUp == Wall.EXISTS) {
							return false;
						}
						yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
					}
					if (wallRight == Wall.EXISTS && wallDown == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS) {
						if (wallUp == Wall.NOT_EXISTS) {
							return false;
						}
						yokoWall[yIndex][xIndex] = Wall.EXISTS;
					}
					if (wallRight == Wall.NOT_EXISTS && wallDown == Wall.EXISTS && wallLeft == Wall.NOT_EXISTS) {
						if (wallUp == Wall.NOT_EXISTS) {
							return false;
						}
						yokoWall[yIndex][xIndex] = Wall.EXISTS;
					}
					if (wallRight == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS && wallLeft == Wall.EXISTS) {
						if (wallUp == Wall.NOT_EXISTS) {
							return false;
						}
						yokoWall[yIndex][xIndex] = Wall.EXISTS;
					}
					if (wallDown == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS && wallUp == Wall.NOT_EXISTS) {
						if (wallRight == Wall.EXISTS) {
							return false;
						}
						tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
					}
					if (wallDown == Wall.EXISTS && wallLeft == Wall.NOT_EXISTS && wallUp == Wall.NOT_EXISTS) {
						if (wallRight == Wall.NOT_EXISTS) {
							return false;
						}
						tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
					}
					if (wallDown == Wall.NOT_EXISTS && wallLeft == Wall.EXISTS && wallUp == Wall.NOT_EXISTS) {
						if (wallRight == Wall.NOT_EXISTS) {
							return false;
						}
						tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
					}
					if (wallDown == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS && wallUp == Wall.EXISTS) {
						if (wallRight == Wall.NOT_EXISTS) {
							return false;
						}
						tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
					}
					if (wallLeft == Wall.NOT_EXISTS && wallUp == Wall.NOT_EXISTS && wallRight == Wall.NOT_EXISTS) {
						if (wallDown == Wall.EXISTS) {
							return false;
						}
						yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
					}
					if (wallLeft == Wall.EXISTS && wallUp == Wall.NOT_EXISTS && wallRight == Wall.NOT_EXISTS) {
						if (wallDown == Wall.NOT_EXISTS) {
							return false;
						}
						yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
					}
					if (wallLeft == Wall.NOT_EXISTS && wallUp == Wall.EXISTS && wallRight == Wall.NOT_EXISTS) {
						if (wallDown == Wall.NOT_EXISTS) {
							return false;
						}
						yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
					}
					if (wallLeft == Wall.NOT_EXISTS && wallUp == Wall.NOT_EXISTS && wallRight == Wall.EXISTS) {
						if (wallDown == Wall.NOT_EXISTS) {
							return false;
						}
						yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
					}
				}
			}
			return true;
		}

		/**
		 * 部屋が長方形でなくてはならない場合、当チェックを実施する。
		 * 柱から見て壁の数は0、2(直進)、3、4のいずれかになる。
		 */
		private boolean rectCheck() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Wall wallUp = yokoWall[yIndex][xIndex];
					Wall wallRight = tateWall[yIndex][xIndex + 1];
					Wall wallDown = yokoWall[yIndex + 1][xIndex];
					Wall wallLeft = tateWall[yIndex][xIndex];
					if (wallUp == Wall.EXISTS) {
						if (wallRight == Wall.EXISTS) {
							if (wallDown == Wall.EXISTS) {
								// OK
							}
							if (wallDown == Wall.NOT_EXISTS) {
								if (wallLeft == Wall.NOT_EXISTS) {
									return false;
								}
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallLeft == Wall.EXISTS) {
								// OK
							}
							if (wallLeft == Wall.NOT_EXISTS) {
								if (wallDown == Wall.NOT_EXISTS) {
									return false;
								}
								yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
							}
						}
						if (wallRight == Wall.NOT_EXISTS) {
							if (wallDown == Wall.EXISTS) {
								// OK
							}
							if (wallDown == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
						}
					}
					if (wallUp == Wall.NOT_EXISTS) {
						if (wallRight == Wall.EXISTS) {
							if (wallLeft == Wall.EXISTS) {
								// OK
							}
							if (wallLeft == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallRight == Wall.NOT_EXISTS) {
							if (wallDown == Wall.EXISTS) {
								return false;
							}
							if (wallLeft == Wall.EXISTS) {
								return false;
							}
							yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
							tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
					}
					if (wallRight == Wall.EXISTS) {
						if (wallDown == Wall.EXISTS) {
							if (wallLeft == Wall.EXISTS) {
								// OK
							}
							if (wallLeft == Wall.NOT_EXISTS) {
								if (wallUp == Wall.NOT_EXISTS) {
									return false;
								}
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallUp == Wall.EXISTS) {
								// OK
							}
							if (wallUp == Wall.NOT_EXISTS) {
								if (wallLeft == Wall.NOT_EXISTS) {
									return false;
								}
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
						}
						if (wallDown == Wall.NOT_EXISTS) {
							if (wallLeft == Wall.EXISTS) {
								// OK
							}
							if (wallLeft == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
					}
					if (wallRight == Wall.NOT_EXISTS) {
						if (wallDown == Wall.EXISTS) {
							if (wallUp == Wall.EXISTS) {
								// OK
							}
							if (wallUp == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallDown == Wall.NOT_EXISTS) {
							if (wallLeft == Wall.EXISTS) {
								return false;
							}
							if (wallUp == Wall.EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
					}
					if (wallDown == Wall.EXISTS) {
						if (wallLeft == Wall.EXISTS) {
							if (wallUp == Wall.EXISTS) {
								// OK
							}
							if (wallUp == Wall.NOT_EXISTS) {
								if (wallRight == Wall.NOT_EXISTS) {
									return false;
								}
								tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
							}
							if (wallRight == Wall.EXISTS) {
								// OK
							}
							if (wallRight == Wall.NOT_EXISTS) {
								if (wallUp == Wall.NOT_EXISTS) {
									return false;
								}
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
						}
						if (wallLeft == Wall.NOT_EXISTS) {
							if (wallUp == Wall.EXISTS) {
								// OK
							}
							if (wallUp == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
					}
					if (wallDown == Wall.NOT_EXISTS) {
						if (wallLeft == Wall.EXISTS) {
							if (wallRight == Wall.EXISTS) {
								// OK
							}
							if (wallRight == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
						}
						if (wallLeft == Wall.NOT_EXISTS) {
							if (wallUp == Wall.EXISTS) {
								return false;
							}
							if (wallRight == Wall.EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
						}
					}
					if (wallLeft == Wall.EXISTS) {
						if (wallUp == Wall.EXISTS) {
							if (wallRight == Wall.EXISTS) {
								// OK
							}
							if (wallRight == Wall.NOT_EXISTS) {
								if (wallDown == Wall.NOT_EXISTS) {
									return false;
								}
								yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
							}
							if (wallDown == Wall.EXISTS) {
								// OK
							}
							if (wallDown == Wall.NOT_EXISTS) {
								if (wallRight == Wall.NOT_EXISTS) {
									return false;
								}
								tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
							}
						}
						if (wallUp == Wall.NOT_EXISTS) {
							if (wallRight == Wall.EXISTS) {
								// OK
							}
							if (wallRight == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
						}
					}
					if (wallLeft == Wall.NOT_EXISTS) {
						if (wallUp == Wall.EXISTS) {
							if (wallDown == Wall.EXISTS) {
								// OK
							}
							if (wallDown == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
						}
						if (wallUp == Wall.NOT_EXISTS) {
							if (wallRight == Wall.EXISTS) {
								return false;
							}
							if (wallDown == Wall.EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 柱から見て壁の数が4の場合falseを返す。
		 */
		private boolean tatamiCheck() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Wall wallUp = yokoWall[yIndex][xIndex];
					Wall wallRight = tateWall[yIndex][xIndex + 1];
					Wall wallDown = yokoWall[yIndex + 1][xIndex];
					Wall wallLeft = tateWall[yIndex][xIndex];
					if (wallUp == Wall.EXISTS && wallRight == Wall.EXISTS && wallDown == Wall.EXISTS) {
						if (wallLeft == Wall.EXISTS) {
							return false;
						}
						tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
					}
					if (wallRight == Wall.EXISTS && wallDown == Wall.EXISTS && wallLeft == Wall.EXISTS) {
						if (wallUp == Wall.EXISTS) {
							return false;
						}
						yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
					}
					if (wallDown == Wall.EXISTS && wallLeft == Wall.EXISTS && wallUp == Wall.EXISTS) {
						if (wallRight == Wall.EXISTS) {
							return false;
						}
						tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
					}
					if (wallLeft == Wall.EXISTS && wallUp == Wall.EXISTS && wallRight == Wall.EXISTS) {
						if (wallDown == Wall.EXISTS) {
							return false;
						}
						yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
					}
				}
			}
			return true;
		}

		private boolean isSolved() {
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
			return true;
		}

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
					sb.append("　");
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

		/**
		 * posに対応したSikakuを返す。
		 * 部屋が長方形だとあらかじめわかってる時しか使えないので注意。
		 */
		public Sikaku getSikaku(Position pos) {
			int left = pos.getxIndex();
			int up = pos.getyIndex();
			int right = pos.getxIndex();
			int down = pos.getyIndex();
			while (left != 0 && yokoWall[pos.getyIndex()][left - 1] == Wall.NOT_EXISTS) {
				left--;
			}
			while (up != 0 && tateWall[up - 1][pos.getxIndex()] == Wall.NOT_EXISTS) {
				up--;
			}
			while (right != getXLength() - 1 && yokoWall[pos.getyIndex()][right] == Wall.NOT_EXISTS) {
				right++;
			}
			while (down != getXLength() - 1 && tateWall[down][pos.getxIndex()] == Wall.NOT_EXISTS) {
				down++;
			}
			return new Sikaku(new Position(up, left), new Position(down, right));
		}

	}

	public static void main(String[] args) {
		//		List<Set<Position>> rooms = RoomMaker.roomMake(10, 10);
		//		System.out.println(getString(10, 10, rooms));
		System.out.println(new RoomMaker2(10, 10, 2, 5, true).getSikaku(new Position(5, 5)));
	}

	public static String getString(int height, int width, List<Set<Position>> rooms) {
		StringBuilder sb = new StringBuilder();
		for (int xIndex = 0; xIndex < width * 2 + 1; xIndex++) {
			sb.append("□");
		}
		sb.append(System.lineSeparator());
		for (int yIndex = 0; yIndex < height; yIndex++) {
			sb.append("□");
			for (int xIndex = 0; xIndex < width; xIndex++) {
				sb.append("　");
				if (xIndex != width - 1) {
					sb.append(isExistYokoWall(yIndex, xIndex, rooms) ? "□" : "　");
				}
			}
			sb.append("□");
			sb.append(System.lineSeparator());
			if (yIndex != height - 1) {
				sb.append("□");
				for (int xIndex = 0; xIndex < width; xIndex++) {
					sb.append(isExistTateWall(yIndex, xIndex, rooms) ? "□" : "　");
					if (xIndex != width - 1) {
						sb.append("□");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
			}
		}
		for (int xIndex = 0; xIndex < width * 2 + 1; xIndex++) {
			sb.append("□");
		}
		sb.append(System.lineSeparator());
		return sb.toString();
	}

	private static boolean isExistYokoWall(int yIndex, int xIndex, List<Set<Position>> rooms) {
		Position pos = new Position(yIndex, xIndex);
		for (Set<Position> room : rooms) {
			if (room.contains(pos)) {
				Position rightPos = new Position(yIndex, xIndex + 1);
				if (room.contains(rightPos)) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean isExistTateWall(int yIndex, int xIndex, List<Set<Position>> rooms) {
		Position pos = new Position(yIndex, xIndex);
		for (Set<Position> room : rooms) {
			if (room.contains(pos)) {
				Position downPos = new Position(yIndex + 1, xIndex);
				if (room.contains(downPos)) {
					return false;
				}
			}
		}
		return true;
	}

	public static List<Set<Position>> roomMake(int height, int width) {
		List<Set<Position>> rooms = new ArrayList<>();
		while (true) {
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < height * width; i++) {
				indexList.add(i);
			}
			Collections.shuffle(indexList);
			for (int index : indexList) {
				int yIndex = indexList.get(index) / width;
				int xIndex = indexList.get(index) % width;
				Position pos = new Position(yIndex, xIndex);
				Collections.shuffle(rooms);
				boolean roomed = false;
				for (Set<Position> room : rooms) {
					Position upPos = new Position(yIndex - 1, xIndex);
					Position rightPos = new Position(yIndex, xIndex + 1);
					Position downPos = new Position(yIndex + 1, xIndex);
					Position leftPos = new Position(yIndex, xIndex - 1);
					if (room.contains(upPos) || room.contains(rightPos) || room.contains(downPos)
							|| room.contains(leftPos)) {
						room.add(pos);
						roomed = true;
						break;
					}
				}
				if (!roomed) {
					Set<Position> newRoom = new HashSet<>();
					newRoom.add(pos);
					rooms.add(newRoom);
				}
			}
			boolean isOk = true;
			for (Set<Position> room : rooms) {
				// ここを変えれば部屋のサイズを調整できる
				if (room.size() < 2) {
					isOk = false;
					break;
				}
			}
			if (!isOk) {
				rooms.clear();
			} else {
				break;
			}
		}
		return rooms;
	}
}
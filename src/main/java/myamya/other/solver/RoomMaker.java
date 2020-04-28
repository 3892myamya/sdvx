package myamya.other.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Position;
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

		public RoomMaker2(int height, int width) {
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
					if (Math.random() * 2 < 1) {
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
					if (!check()) {
						// 破綻したら0から作り直す。
						init();
						Collections.shuffle(indexList);
						index = 0;
						continue;
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
		 * 柱から見て壁の数は0、2(直進)、3、4のいずれか
		 */
		private boolean check() {
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
					if (notExists == 2 && exists == 2) {
						if (wall1 == Wall.EXISTS && wall2 == Wall.EXISTS) {
							return false;
						}
						if (wall1 == Wall.EXISTS && wall4 == Wall.EXISTS) {
							return false;
						}
						if (wall2 == Wall.EXISTS && wall3 == Wall.EXISTS) {
							return false;
						}
						if (wall3 == Wall.EXISTS && wall4 == Wall.EXISTS) {
							return false;
						}
					}
					if (notExists == 1 && exists == 2) {
						if (wall1 == Wall.EXISTS && wall2 == Wall.EXISTS) {
							if (wall3 == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wall4 == Wall.SPACE) {
								yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
							}
						}
						if (wall1 == Wall.EXISTS && wall4 == Wall.EXISTS) {
							if (wall2 == Wall.SPACE) {
								tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
							}
							if (wall3 == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
						}
						if (wall2 == Wall.EXISTS && wall3 == Wall.EXISTS) {
							if (wall1 == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wall4 == Wall.SPACE) {
								yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
							}
						}
						if (wall3 == Wall.EXISTS && wall4 == Wall.EXISTS) {
							if (wall1 == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wall2 == Wall.SPACE) {
								tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
							}
						}
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

	}

	public static void main(String[] args) {
		//		List<Set<Position>> rooms = RoomMaker.roomMake(10, 10);
		//		System.out.println(getString(10, 10, rooms));
		System.out.println(new RoomMaker2(10, 10));
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

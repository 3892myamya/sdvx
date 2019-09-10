package myamya.other.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Position;

public class RoomMaker {

	public static void main(String[] args) {
		List<Set<Position>> rooms = RoomMaker.roomMake(10, 10);
		System.out.println(getString(10, 10, rooms));
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

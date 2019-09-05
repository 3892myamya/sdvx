package myamya.other.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Position;

/**
 * ジェネレータ向けに部屋割りをランダム生成するモジュール
 * 実験中
 */
public class RoomMaker {

	public static void main(String[] args) {
		RoomMaker roomMaker = new RoomMaker(10, 10);
		roomMaker.roomMake();
		System.out.println(roomMaker);
	}

	private final List<Set<Position>> rooms;

	private final int height;
	private final int width;

	public RoomMaker(int height, int width) {
		rooms = new ArrayList<>();
		this.height = height;
		this.width = width;
	}

	@Override
	public String toString() {
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
					sb.append(isExistYokoWall(yIndex, xIndex) ? "□" : "　");
				}
			}
			sb.append("□");
			sb.append(System.lineSeparator());
			if (yIndex != height - 1) {
				sb.append("□");
				for (int xIndex = 0; xIndex < width; xIndex++) {
					sb.append(isExistTateWall(yIndex, xIndex) ? "□" : "　");
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

	private boolean isExistYokoWall(int yIndex, int xIndex) {
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

	private boolean isExistTateWall(int yIndex, int xIndex) {
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

	public void roomMake() {
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
	}
}

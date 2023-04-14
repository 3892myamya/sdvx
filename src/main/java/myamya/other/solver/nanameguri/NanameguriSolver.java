package myamya.other.solver.nanameguri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class NanameguriSolver implements Solver {
//	public static class NanameguriGenerator implements Generator {
//		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
//		private static final String FULL_NUMS = "０１２３４５６７８９";
//
//		static class ExtendedField extends NanameguriSolver.Field {
//			public ExtendedField(Field other) {
//				super(other);
//			}
//
//			public ExtendedField(int height, int width, List<Set<Position>> rooms) {
//				super(height, width, rooms);
//			}
//
//			/**
//			 * 部屋数チェックを加える
//			 */
//			@Override
//			public boolean roomSolve() {
//				if (rooms.size() != 1 && rooms.size() % 2 == 1) {
//					return false;
//				}
//				return super.roomSolve();
//			}
//		}
//
//		static class NanameguriSolverForGenerator extends NanameguriSolver {
//			private final int limit;
//
//			public NanameguriSolverForGenerator(Field field, int limit) {
//				super(field);
//				this.limit = limit;
//			}
//
//			public int solve2() {
//				try {
//					while (!field.isSolved()) {
//						String befStr = field.getStateDump();
//						if (!field.solveAndCheck()) {
//							return -1;
//						}
//						int recursiveCnt = 0;
//						while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
//							if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
//								return -1;
//							}
//							recursiveCnt++;
//						}
//						if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
//							return -1;
//						}
//					}
//				} catch (CountOverException e) {
//					return -1;
//				}
//				return count;
//			}
//
//			@Override
//			protected boolean candSolve(Field field, int recursive) {
//				if (this.count >= limit) {
//					throw new CountOverException();
//				} else {
//					return super.candSolve(field, recursive);
//				}
//			}
//		}
//
//		private final int height;
//		private final int width;
//
//		public NanameguriGenerator(int height, int width) {
//			this.height = height;
//			this.width = width;
//		}
//
//		public static void main(String[] args) {
//			new NanameguriGenerator(10, 10).generate();
//		}
//
//		@Override
//		public GeneratorResult generate() {
//			NanameguriSolver.Field wkField = new ExtendedField(height, width,
//					RoomMaker.roomMake(height, width, (int) (Math.sqrt(height)), -1));
//			List<Integer> indexList = new ArrayList<>();
//			for (int i = 0; i < (height * (width - 1)) + ((height - 1) * width); i++) {
//				indexList.add(i);
//			}
//			int level = 0;
//			int index = 0;
//			long start = System.nanoTime();
//			while (true) {
//				// 問題生成部
//				while (!wkField.isSolved()) {
//					int posBase = indexList.get(index);
//					boolean toYokoWall;
//					int yIndex, xIndex;
//					if (posBase < height * (width - 1)) {
//						toYokoWall = true;
//						yIndex = posBase / (width - 1);
//						xIndex = posBase % (width - 1);
//					} else {
//						toYokoWall = false;
//						posBase = posBase - (height * (width - 1));
//						yIndex = posBase / width;
//						xIndex = posBase % width;
//					}
//					if ((toYokoWall && wkField.yokoWall[yIndex][xIndex] == Wall.SPACE)
//							|| (!toYokoWall && wkField.tateWall[yIndex][xIndex] == Wall.SPACE)) {
//						boolean isOk = false;
//						List<Integer> numIdxList = new ArrayList<>();
//						for (int i = 0; i < 2; i++) {
//							numIdxList.add(i);
//						}
//						Collections.shuffle(numIdxList);
//						for (int masuNum : numIdxList) {
//							NanameguriSolver.Field virtual = new ExtendedField(wkField);
//							if (masuNum < 1) {
//								if (toYokoWall) {
//									virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
//								} else {
//									virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
//								}
//							} else if (masuNum < 2) {
//								if (toYokoWall) {
//									virtual.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
//								} else {
//									virtual.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
//								}
//							}
//							if (virtual.solveAndCheck()) {
//								isOk = true;
//								wkField.masu = virtual.masu;
//								wkField.yokoWall = virtual.yokoWall;
//								wkField.tateWall = virtual.tateWall;
//							}
//						}
//						if (!isOk) {
//							// 破綻したら0から作り直す。
//							wkField = new ExtendedField(height, width,
//									RoomMaker.roomMake(height, width, (int) (Math.sqrt(height)), -1));
//							index = 0;
//							continue;
//						}
//					}
//					index++;
//				}
//				// 部屋の数字を確定
//				List<Integer> roomIdxList = new ArrayList<>();
//				for (int i = 0; i < wkField.rooms.size(); i++) {
//					Room room = wkField.rooms.get(i);
//					int cnt = 0;
//					for (Position pos : room.member) {
//						if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
//							cnt++;
//						}
//					}
//					roomIdxList.add(i);
//					room.whiteCnt = cnt;
//				}
//
//				// マスを戻す
//				List<Position> fixedMasuList = new ArrayList<>();
//				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//						wkField.masu[yIndex][xIndex] = Masu.SPACE;
//						fixedMasuList.add(new Position(yIndex, xIndex));
//					}
//				}
//				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
//						wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
//					}
//				}
//				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
//					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//						wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
//					}
//				}
//				// 解けるかな？
//				level = new NanameguriSolverForGenerator(wkField, 200).solve2();
//				if (level == -1) {
//					// 解けなければやり直し
//					wkField = new ExtendedField(height, width,
//							RoomMaker.roomMake(height, width, (int) (Math.sqrt(height)), -1));
//					index = 0;
//				} else {
//					Collections.shuffle(roomIdxList);
//					for (Integer pos : roomIdxList) {
//						NanameguriSolver.Field virtual = new NanameguriSolver.Field(wkField, true);
//						virtual.rooms.get(pos).whiteCnt = -1;
//						int solveResult = new NanameguriSolverForGenerator(virtual, 5000).solve2();
//						if (solveResult != -1 && solveResult >= level) {
//							wkField.rooms.get(pos).whiteCnt = -1;
//							level = solveResult;
//						}
//					}
//					break;
//				}
//			}
//			System.out.println(level);
//			level = (int) Math.sqrt(level / 3) + 1;
//			String status = "Lv:" + level + "の問題を獲得！(部屋：" + wkField.getHintCount() + ")";
//			String url = wkField.getPuzPreURL();
//			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
//			StringBuilder sb = new StringBuilder();
//			int baseSize = 20;
//			int margin = 5;
//			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
//					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
//					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
//			// 横壁描画
//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
//					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1
//							|| wkField.getYokoRoomWall()[yIndex][xIndex];
//					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
//							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
//							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
//					if (oneYokoWall) {
//						sb.append("stroke=\"#000\" ");
//					} else {
//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
//					}
//					sb.append(">" + "</line>");
//				}
//			}
//			// 縦壁描画
//			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1
//							|| wkField.getTateRoomWall()[yIndex][xIndex];
//					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
//							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
//							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
//							+ "\" stroke-width=\"1\" fill=\"none\"");
//					if (oneTateWall) {
//						sb.append("stroke=\"#000\" ");
//					} else {
//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
//					}
//					sb.append(">" + "</line>");
//				}
//			}
//			// 数字描画
//			for (NanameguriSolver.Room room : wkField.getRooms()) {
//				int roomWhiteCount = room.getWhiteCnt();
//				if (roomWhiteCount != -1) {
//					String roomWhiteCountStr;
//					String wkstr = String.valueOf(roomWhiteCount);
//					int idx = HALF_NUMS.indexOf(wkstr);
//					if (idx >= 0) {
//						roomWhiteCountStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
//					} else {
//						roomWhiteCountStr = wkstr;
//					}
//					Position numberMasuPos = room.getNumberMasuPos();
//					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize - 5 + margin) + "\" x=\""
//							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + "black"
//							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
//							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomWhiteCountStr + "</text>");
//				}
//			}
//			sb.append("</svg>");
//			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
//			System.out.println(level);
//			System.out.println(wkField.getHintCount());
//			System.out.println(wkField);
//			System.out.println(url);
//			return new GeneratorResult(status, sb.toString(), link, url, level, "");
//		}
//
//	}
	public static class Room {
		@Override
		public String toString() {
			return "Room [member=" + member + "]";
		}

		// 部屋に属するマスの集合
		private final Set<Position> member;
		// 国境となる壁の位置情報
		private final Set<Position> yokoWallPosSet;
		private final Set<Position> tateWallPosSet;

		public Room(Set<Position> member, Set<Position> yokoWallPosSet, Set<Position> tateWallPosSet) {
			this.member = member;
			this.yokoWallPosSet = yokoWallPosSet;
			this.tateWallPosSet = tateWallPosSet;
		}

		public Set<Position> getMember() {
			return member;
		}

		public Set<Position> getYokoWallPosSet() {
			return yokoWallPosSet;
		}

		public Set<Position> getTateWallPosSet() {
			return tateWallPosSet;
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 斜め情報 0=なし、1=＼、2:／
		private int[][] naname;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoRoomWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateRoomWall;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;
		// 同一グループに属するマスの情報
		protected final List<Room> rooms;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getHintCount() {
			return String.valueOf(rooms.size());
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
//			sb.append("http://pzv.jp/p.html?Nanameguri/" + getXLength() + "/" + getYLength() + "/");
//			for (int i = 0; i < getYLength() * (getXLength() - 1); i++) {
//				int yIndex1 = i / (getXLength() - 1);
//				int xIndex1 = i % (getXLength() - 1);
//				i++;
//				int yIndex2 = -1;
//				int xIndex2 = -1;
//				if (i < getYLength() * (getXLength() - 1)) {
//					yIndex2 = i / (getXLength() - 1);
//					xIndex2 = i % (getXLength() - 1);
//				}
//				i++;
//				int yIndex3 = -1;
//				int xIndex3 = -1;
//				if (i < getYLength() * (getXLength() - 1)) {
//					yIndex3 = i / (getXLength() - 1);
//					xIndex3 = i % (getXLength() - 1);
//				}
//				i++;
//				int yIndex4 = -1;
//				int xIndex4 = -1;
//				if (i < getYLength() * (getXLength() - 1)) {
//					yIndex4 = i / (getXLength() - 1);
//					xIndex4 = i % (getXLength() - 1);
//				}
//				i++;
//				int yIndex5 = -1;
//				int xIndex5 = -1;
//				if (i < getYLength() * (getXLength() - 1)) {
//					yIndex5 = i / (getXLength() - 1);
//					xIndex5 = i % (getXLength() - 1);
//				}
//				int num = 0;
//				if (yIndex1 != -1 && xIndex1 != -1 && yokoRoomWall[yIndex1][xIndex1]) {
//					num = num + 16;
//				}
//				if (yIndex2 != -1 && xIndex2 != -1 && yokoRoomWall[yIndex2][xIndex2]) {
//					num = num + 8;
//				}
//				if (yIndex3 != -1 && xIndex3 != -1 && yokoRoomWall[yIndex3][xIndex3]) {
//					num = num + 4;
//				}
//				if (yIndex4 != -1 && xIndex4 != -1 && yokoRoomWall[yIndex4][xIndex4]) {
//					num = num + 2;
//				}
//				if (yIndex5 != -1 && xIndex5 != -1 && yokoRoomWall[yIndex5][xIndex5]) {
//					num = num + 1;
//				}
//				sb.append(ALPHABET_AND_NUMBER.substring(num, num + 1));
//			}
//			for (int i = 0; i < (getYLength() - 1) * getXLength(); i++) {
//				int yIndex1 = i / getXLength();
//				int xIndex1 = i % getXLength();
//				i++;
//				int yIndex2 = -1;
//				int xIndex2 = -1;
//				if (i < (getYLength() - 1) * getXLength()) {
//					yIndex2 = i / getXLength();
//					xIndex2 = i % getXLength();
//				}
//				i++;
//				int yIndex3 = -1;
//				int xIndex3 = -1;
//				if (i < (getYLength() - 1) * getXLength()) {
//					yIndex3 = i / getXLength();
//					xIndex3 = i % getXLength();
//				}
//				i++;
//				int yIndex4 = -1;
//				int xIndex4 = -1;
//				if (i < (getYLength() - 1) * getXLength()) {
//					yIndex4 = i / getXLength();
//					xIndex4 = i % getXLength();
//				}
//				i++;
//				int yIndex5 = -1;
//				int xIndex5 = -1;
//				if (i < (getYLength() - 1) * getXLength()) {
//					yIndex5 = i / getXLength();
//					xIndex5 = i % getXLength();
//				}
//				int num = 0;
//				if (yIndex1 != -1 && xIndex1 != -1 && tateRoomWall[yIndex1][xIndex1]) {
//					num = num + 16;
//				}
//				if (yIndex2 != -1 && xIndex2 != -1 && tateRoomWall[yIndex2][xIndex2]) {
//					num = num + 8;
//				}
//				if (yIndex3 != -1 && xIndex3 != -1 && tateRoomWall[yIndex3][xIndex3]) {
//					num = num + 4;
//				}
//				if (yIndex4 != -1 && xIndex4 != -1 && tateRoomWall[yIndex4][xIndex4]) {
//					num = num + 2;
//				}
//				if (yIndex5 != -1 && xIndex5 != -1 && tateRoomWall[yIndex5][xIndex5]) {
//					num = num + 1;
//				}
//				sb.append(ALPHABET_AND_NUMBER.substring(num, num + 1));
//			}
//			int interval = 0;
//			Set<Position> wkRooms = new HashSet<>();
//			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
//				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
//					Position pos = new Position(yIndex, xIndex);
//					if (!wkRooms.contains(pos)) {
//						Room useRoom = null;
//						for (Room room : rooms) {
//							if (room.member.contains(pos)) {
//								useRoom = room;
//								wkRooms.addAll(room.member);
//								break;
//							}
//						}
//						Integer num = useRoom.getWhiteCnt();
//						String numStr;
//						if (num == -1) {
//							interval++;
//							continue;
//						} else {
//							numStr = Integer.toHexString(num);
//							if (numStr.length() == 2) {
//								numStr = "-" + numStr;
//							} else if (numStr.length() == 3) {
//								numStr = "+" + numStr;
//							}
//						}
//						if (interval == 0) {
//							sb.append(numStr);
//						} else {
//							sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
//							sb.append(numStr);
//							interval = 0;
//						}
//					}
//				}
//			}
//			if (interval != 0) {
//				sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
//			}
//			if (sb.charAt(sb.length() - 1) == '.') {
//				sb.append("/");
//			}
			return sb.toString();
		}

		public int[][] getNaname() {
			return naname;
		}

		public boolean[][] getYokoRoomWall() {
			return yokoRoomWall;
		}

		public boolean[][] getTateRoomWall() {
			return tateRoomWall;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public List<Room> getRooms() {
			return rooms;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			naname = new int[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					naname[yIndex][xIndex] = 0;
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
			// パラメータを解釈して壁の有無を入れる
			yokoRoomWall = new boolean[height][width - 1];
			tateRoomWall = new boolean[height - 1][width];
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * (getXLength() - 1); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * (getXLength() - 1)) - 1) {
					if (mod >= 0) {
						yokoRoomWall[(cnt - mod + 0) / (getXLength() - 1)][(cnt - mod + 0) % (getXLength() - 1)] = bit
								/ 16 % 2 == 1;
					}
					if (mod >= 1) {
						yokoRoomWall[(cnt - mod + 1) / (getXLength() - 1)][(cnt - mod + 1) % (getXLength() - 1)] = bit
								/ 8 % 2 == 1;
					}
					if (mod >= 2) {
						yokoRoomWall[(cnt - mod + 2) / (getXLength() - 1)][(cnt - mod + 2) % (getXLength() - 1)] = bit
								/ 4 % 2 == 1;
					}
					if (mod >= 3) {
						yokoRoomWall[(cnt - mod + 3) / (getXLength() - 1)][(cnt - mod + 3) % (getXLength() - 1)] = bit
								/ 2 % 2 == 1;
					}
					if (mod >= 4) {
						yokoRoomWall[(cnt - mod + 4) / (getXLength() - 1)][(cnt - mod + 4) % (getXLength() - 1)] = bit
								/ 1 % 2 == 1;
					}
				}
			}
			for (int cnt = 0; cnt < (getYLength() - 1) * getXLength(); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == ((getYLength() - 1) * getXLength()) - 1) {
					if (mod >= 0) {
						tateRoomWall[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16
								% 2 == 1;
					}
					if (mod >= 1) {
						tateRoomWall[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1;
					}
					if (mod >= 2) {
						tateRoomWall[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1;
					}
					if (mod >= 3) {
						tateRoomWall[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1;
					}
					if (mod >= 4) {
						tateRoomWall[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1;
					}
				}
			}
			// 斜めを入れる
			for (int cnt = 0; cnt < getYLength() * getXLength(); cnt++) {
				int mod = cnt % 3;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 2 || cnt == (getYLength() * (getXLength())) - 1) {
					if (mod >= 0) {
						naname[(cnt - mod + 0) / (getXLength())][(cnt - mod + 0) % (getXLength())] = bit / 9 % 3;
					}
					if (mod >= 1) {
						naname[(cnt - mod + 1) / (getXLength())][(cnt - mod + 1) % (getXLength())] = bit / 3 % 3;
					}
					if (mod >= 2) {
						naname[(cnt - mod + 2) / (getXLength())][(cnt - mod + 2) % (getXLength())] = bit / 1 % 3;
					}
				}
			}
			// 斜めマスは白確定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (naname[yIndex][xIndex] != 0) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
				}
			}
			// 縦と横の壁の関係からにょろっと部屋を決めていく
			rooms = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (naname[yIndex][xIndex] == 0) {
						Position pos = new Position(yIndex, xIndex);
						Map<Position, Integer> continuePosMap = new HashMap<>();
						continuePosMap.put(pos, 0);
						Set<Position> yokoWallPosSet = new HashSet<>();
						Set<Position> tateWallPosSet = new HashSet<>();
						setContinueRoomPosSet(pos, continuePosMap, yokoWallPosSet, tateWallPosSet, null);
						boolean alreadyRoomed = false;
						for (Room room : rooms) {
							if (continuePosMap.keySet().containsAll(room.getMember())
									&& room.getMember().containsAll(continuePosMap.keySet())) {
								alreadyRoomed = true;
							}
						}
						if (!alreadyRoomed) {
							rooms.add(new Room(continuePosMap.keySet(), yokoWallPosSet, tateWallPosSet));
						}
					} else {
						// 斜めマスは上下に分けて処理
						Position pos = new Position(yIndex, xIndex);
						Map<Position, Integer> continuePosMap = new HashMap<>();
						continuePosMap.put(pos, 1);
						Set<Position> yokoWallPosSet = new HashSet<>();
						Set<Position> tateWallPosSet = new HashSet<>();
						if (naname[yIndex][xIndex] == 1) {
							setContinueRoomPosSet(pos, continuePosMap, yokoWallPosSet, tateWallPosSet, Direction.UP);
							setContinueRoomPosSet(pos, continuePosMap, yokoWallPosSet, tateWallPosSet, Direction.RIGHT);
						} else if (naname[yIndex][xIndex] == 2) {
							setContinueRoomPosSet(pos, continuePosMap, yokoWallPosSet, tateWallPosSet, Direction.UP);
							setContinueRoomPosSet(pos, continuePosMap, yokoWallPosSet, tateWallPosSet, Direction.LEFT);
						}
						boolean alreadyRoomed = false;
						for (Room room : rooms) {
							if (continuePosMap.keySet().containsAll(room.getMember())
									&& room.getMember().containsAll(continuePosMap.keySet())) {
								alreadyRoomed = true;
							}
						}
						if (!alreadyRoomed) {
							rooms.add(new Room(continuePosMap.keySet(), yokoWallPosSet, tateWallPosSet));
						}
						continuePosMap = new HashMap<>();
						continuePosMap.put(pos, 2);
						yokoWallPosSet = new HashSet<>();
						tateWallPosSet = new HashSet<>();
						if (naname[yIndex][xIndex] == 1) {
							setContinueRoomPosSet(pos, continuePosMap, yokoWallPosSet, tateWallPosSet, Direction.DOWN);
							setContinueRoomPosSet(pos, continuePosMap, yokoWallPosSet, tateWallPosSet, Direction.LEFT);
						} else if (naname[yIndex][xIndex] == 2) {
							setContinueRoomPosSet(pos, continuePosMap, yokoWallPosSet, tateWallPosSet, Direction.DOWN);
							setContinueRoomPosSet(pos, continuePosMap, yokoWallPosSet, tateWallPosSet, Direction.RIGHT);
						}
						alreadyRoomed = false;
						for (Room room : rooms) {
							if (continuePosMap.keySet().containsAll(room.getMember())
									&& room.getMember().containsAll(continuePosMap.keySet())) {
								alreadyRoomed = true;
							}
						}
						if (!alreadyRoomed) {
							rooms.add(new Room(continuePosMap.keySet(), yokoWallPosSet, tateWallPosSet));
						}
					}
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
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
			naname = other.naname;
			yokoRoomWall = other.yokoRoomWall;
			tateRoomWall = other.tateRoomWall;
			rooms = new ArrayList<>();
			for (Room room : other.rooms) {
				rooms.add(new Room(room.member, room.yokoWallPosSet, room.tateWallPosSet));
			}
		}

		// posを起点に上下左右に部屋壁でないマスをつなげていく。
		private void setContinueRoomPosSet(Position pos, Map<Position, Integer> continuePosMap,
				Set<Position> yokoWallPosSet, Set<Position> tateWallPosSet, Direction from) {
			int myNaname = naname[pos.getyIndex()][pos.getxIndex()];
			boolean wallUp = pos.getyIndex() == 0 ? false : tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()];
			boolean wallRight = pos.getxIndex() == getXLength() - 1 ? false
					: yokoRoomWall[pos.getyIndex()][pos.getxIndex()];
			boolean wallDown = pos.getyIndex() == getYLength() - 1 ? false
					: tateRoomWall[pos.getyIndex()][pos.getxIndex()];
			boolean wallLeft = pos.getxIndex() == 0 ? false : yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1];
			if (myNaname == 0) {
				if (wallUp) {
					tateWallPosSet.add(new Position(pos.getyIndex() - 1, pos.getxIndex()));
				}
				if (wallRight) {
					yokoWallPosSet.add(new Position(pos.getyIndex(), pos.getxIndex()));
				}
				if (wallDown) {
					tateWallPosSet.add(new Position(pos.getyIndex(), pos.getxIndex()));
				}
				if (wallLeft) {
					yokoWallPosSet.add(new Position(pos.getyIndex(), pos.getxIndex() - 1));
				}
			} else if (myNaname == 1) {
				if (wallUp && (from == Direction.UP || from == Direction.RIGHT)) {
					tateWallPosSet.add(new Position(pos.getyIndex() - 1, pos.getxIndex()));
				}
				if (wallRight && (from == Direction.UP || from == Direction.RIGHT)) {
					yokoWallPosSet.add(new Position(pos.getyIndex(), pos.getxIndex()));
				}
				if (wallDown && (from == Direction.DOWN || from == Direction.LEFT)) {
					tateWallPosSet.add(new Position(pos.getyIndex(), pos.getxIndex()));
				}
				if (wallLeft && (from == Direction.DOWN || from == Direction.LEFT)) {
					yokoWallPosSet.add(new Position(pos.getyIndex(), pos.getxIndex() - 1));
				}
			} else if (myNaname == 2) {
				if (wallUp && (from == Direction.UP || from == Direction.LEFT)) {
					tateWallPosSet.add(new Position(pos.getyIndex() - 1, pos.getxIndex()));
				}
				if (wallRight && (from == Direction.DOWN || from == Direction.RIGHT)) {
					yokoWallPosSet.add(new Position(pos.getyIndex(), pos.getxIndex()));
				}
				if (wallDown && (from == Direction.DOWN || from == Direction.RIGHT)) {
					tateWallPosSet.add(new Position(pos.getyIndex(), pos.getxIndex()));
				}
				if (wallLeft && (from == Direction.UP || from == Direction.LEFT)) {
					yokoWallPosSet.add(new Position(pos.getyIndex(), pos.getxIndex() - 1));
				}
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				if (myNaname == 1 && (from == Direction.DOWN || from == Direction.LEFT)) {
					//
				} else if (myNaname == 2 && (from == Direction.DOWN || from == Direction.RIGHT)) {
					//
				} else {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					int nextNaname = naname[pos.getyIndex() - 1][pos.getxIndex()];
					Integer nextInner = 0;
					if (nextNaname == 1) {
						if (from == Direction.UP || from == Direction.RIGHT) {
							nextInner = 1;
						} else {
							nextInner = 2;
						}
					} else if (nextNaname == 2) {
						if (from == Direction.UP || from == Direction.LEFT) {
							nextInner = 1;
						} else {
							nextInner = 2;
						}
					}
					if (continuePosMap.get(nextPos) != nextInner
							&& !tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()]) {
						continuePosMap.put(nextPos, nextInner);
						setContinueRoomPosSet(nextPos, continuePosMap, yokoWallPosSet, tateWallPosSet, Direction.DOWN);
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				if (myNaname == 1 && (from == Direction.DOWN || from == Direction.LEFT)) {
					//
				} else if (myNaname == 2 && (from == Direction.UP || from == Direction.LEFT)) {
					//
				} else {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					int nextNaname = naname[pos.getyIndex()][pos.getxIndex() + 1];
					Integer nextInner = 0;
					if (nextNaname == 1) {
						if (from == Direction.UP || from == Direction.RIGHT) {
							nextInner = 1;
						} else {
							nextInner = 2;
						}
					} else if (nextNaname == 2) {
						if (from == Direction.UP || from == Direction.LEFT) {
							nextInner = 1;
						} else {
							nextInner = 2;
						}
					}
					if (continuePosMap.get(nextPos) != nextInner && !yokoRoomWall[pos.getyIndex()][pos.getxIndex()]) {
						continuePosMap.put(nextPos, nextInner);
						setContinueRoomPosSet(nextPos, continuePosMap, yokoWallPosSet, tateWallPosSet, Direction.LEFT);
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				if (myNaname == 1 && (from == Direction.UP || from == Direction.RIGHT)) {
					//
				} else if (myNaname == 2 && (from == Direction.UP || from == Direction.LEFT)) {
					//
				} else {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					int nextNaname = naname[pos.getyIndex() + 1][pos.getxIndex()];
					Integer nextInner = 0;
					if (nextNaname == 1) {
						if (from == Direction.UP || from == Direction.RIGHT) {
							nextInner = 1;
						} else {
							nextInner = 2;
						}
					} else if (nextNaname == 2) {
						if (from == Direction.UP || from == Direction.LEFT) {
							nextInner = 1;
						} else {
							nextInner = 2;
						}
					}
					if (continuePosMap.get(nextPos) != nextInner && !tateRoomWall[pos.getyIndex()][pos.getxIndex()]) {
						continuePosMap.put(nextPos, nextInner);
						setContinueRoomPosSet(nextPos, continuePosMap, yokoWallPosSet, tateWallPosSet, Direction.UP);
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				if (myNaname == 1 && (from == Direction.UP || from == Direction.RIGHT)) {
					//
				} else if (myNaname == 2 && (from == Direction.DOWN || from == Direction.RIGHT)) {
					//
				} else {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					int nextNaname = naname[pos.getyIndex()][pos.getxIndex() - 1];
					Integer nextInner = 0;
					if (nextNaname == 1) {
						if (from == Direction.UP || from == Direction.RIGHT) {
							nextInner = 1;
						} else {
							nextInner = 2;
						}
					} else if (nextNaname == 2) {
						if (from == Direction.UP || from == Direction.LEFT) {
							nextInner = 1;
						} else {
							nextInner = 2;
						}
					}
					if (continuePosMap.get(nextPos) != nextInner
							&& !yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1]) {
						continuePosMap.put(nextPos, nextInner);
						setContinueRoomPosSet(nextPos, continuePosMap, yokoWallPosSet, tateWallPosSet, Direction.RIGHT);
					}
				}
			}
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
					if (naname[yIndex][xIndex] == 0) {
						sb.append(masu[yIndex][xIndex]);
					} else if (naname[yIndex][xIndex] == 1) {
						sb.append("＼");
					} else if (naname[yIndex][xIndex] == 2) {
						sb.append("／");
					}
					if (xIndex != getXLength() - 1) {
						if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
							sb.append(yokoRoomWall[yIndex][xIndex] == true ? "│" : "　");
						} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							sb.append(yokoRoomWall[yIndex][xIndex] == true ? "│" : "・");
						} else if (yokoWall[yIndex][xIndex] == Wall.EXISTS) {
							sb.append(yokoRoomWall[yIndex][xIndex] == true ? "┼" : "□");
						}
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (tateWall[yIndex][xIndex] == Wall.SPACE) {
							sb.append(tateRoomWall[yIndex][xIndex] == true ? "─" : "　");
						} else if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							sb.append(tateRoomWall[yIndex][xIndex] == true ? "─" : "・");
						} else if (tateWall[yIndex][xIndex] == Wall.EXISTS) {
							sb.append(tateRoomWall[yIndex][xIndex] == true ? "┼" : "□");
						}
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
		 * 白マス隣接セルの周辺の壁の数は2、黒マスは4。<br>
		 * また、斜めのマスはまたげない。
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
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						// 自分が不確定マスなら壁は2マスか4マス
						if ((existsCount == 3 && notExistsCount == 1) || notExistsCount > 2) {
							return false;
						}
						if (existsCount > 2) {
							masu[yIndex][xIndex] = Masu.BLACK;
						} else if (notExistsCount != 0) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						if (notExistsCount > 0) {
							return false;
						}
						// 周囲の壁を閉鎖
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
					} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						// 自分が白マスなら壁は必ず2マス
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
						if (naname[yIndex][xIndex] == 1) {
							if (wallUp == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS) {
								return false;
							}
							if (wallUp == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS) {
								return false;
							}
							if (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS) {
								return false;
							}
							if (wallRight == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS) {
								return false;
							}
							if (wallUp == Wall.EXISTS && wallDown == Wall.EXISTS) {
								return false;
							}
							if (wallUp == Wall.EXISTS && wallLeft == Wall.EXISTS) {
								return false;
							}
							if (wallRight == Wall.EXISTS && wallLeft == Wall.EXISTS) {
								return false;
							}
							if (wallRight == Wall.EXISTS && wallDown == Wall.EXISTS) {
								return false;
							}
							if (wallUp == Wall.SPACE && wallDown == Wall.NOT_EXISTS) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (wallUp == Wall.SPACE && wallLeft == Wall.NOT_EXISTS) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE && wallLeft == Wall.NOT_EXISTS) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE && wallDown == Wall.NOT_EXISTS) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallUp == Wall.SPACE && wallDown == Wall.EXISTS) {
								tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallUp == Wall.SPACE && wallLeft == Wall.EXISTS) {
								tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.SPACE && wallLeft == Wall.EXISTS) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.SPACE && wallDown == Wall.EXISTS) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallUp == Wall.NOT_EXISTS && wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallUp == Wall.NOT_EXISTS && wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
							if (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
							if (wallRight == Wall.NOT_EXISTS && wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallUp == Wall.EXISTS && wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallUp == Wall.EXISTS && wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.EXISTS && wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.EXISTS && wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
						} else if (naname[yIndex][xIndex] == 2) {
							if (wallUp == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS) {
								return false;
							}
							if (wallUp == Wall.NOT_EXISTS && wallRight == Wall.NOT_EXISTS) {
								return false;
							}
							if (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS) {
								return false;
							}
							if (wallLeft == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS) {
								return false;
							}
							if (wallUp == Wall.EXISTS && wallDown == Wall.EXISTS) {
								return false;
							}
							if (wallUp == Wall.EXISTS && wallRight == Wall.EXISTS) {
								return false;
							}
							if (wallRight == Wall.EXISTS && wallLeft == Wall.EXISTS) {
								return false;
							}
							if (wallLeft == Wall.EXISTS && wallDown == Wall.EXISTS) {
								return false;
							}
							if (wallUp == Wall.SPACE && wallDown == Wall.NOT_EXISTS) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (wallUp == Wall.SPACE && wallRight == Wall.NOT_EXISTS) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE && wallLeft == Wall.NOT_EXISTS) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallLeft == Wall.SPACE && wallDown == Wall.NOT_EXISTS) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
							if (wallUp == Wall.SPACE && wallDown == Wall.EXISTS) {
								tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallUp == Wall.SPACE && wallRight == Wall.EXISTS) {
								tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.SPACE && wallLeft == Wall.EXISTS) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.SPACE && wallDown == Wall.EXISTS) {
								yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
							}
							if (wallUp == Wall.NOT_EXISTS && wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallUp == Wall.NOT_EXISTS && wallRight == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
							if (wallLeft == Wall.NOT_EXISTS && wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallUp == Wall.EXISTS && wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallUp == Wall.EXISTS && wallRight == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.EXISTS && wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.EXISTS && wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 国境をまたぐマスは1つの国につき必ず2マスになる。 ルールに違反している場合falseを返す。
		 */
		public boolean countrySolve() {
			if (rooms.size() == 1) {
				// めったにないが、部屋が1つなら国境またがなくてもOK
				return true;
			}
			for (Room room : rooms) {
				int whiteCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room.getYokoWallPosSet()) {
					if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
						whiteCnt++;
					} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
						spaceCnt++;
					}
				}
				for (Position pos : room.getTateWallPosSet()) {
					if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
						whiteCnt++;
					} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
						spaceCnt++;
					}
				}
				if (whiteCnt + spaceCnt < 2) {
					// 通過数不足
					return false;
				}
				// 通過しなければならないマスの数
				int retainWhiteCnt = 2 - whiteCnt;
				if (retainWhiteCnt < 0) {
					// 通過数超過
					return false;
				} else if (retainWhiteCnt == 0) {
					// 通過数が既に2なら他は通過しない
					for (Position pos : room.getYokoWallPosSet()) {
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
					}
					for (Position pos : room.getTateWallPosSet()) {
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
					}
				} else if (spaceCnt == retainWhiteCnt) {
					// 未確定壁数が通過せねばならない数に等しければ、未確定壁は通過
					for (Position pos : room.getYokoWallPosSet()) {
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
					}
					for (Position pos : room.getTateWallPosSet()) {
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						if (whitePosSet.size() == 0) {
							whitePosSet.add(pos);
							setContinuePosSet(pos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(pos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!countrySolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
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

	protected final Field field;
	protected int count = 0;

	public NanameguriSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public NanameguriSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = ""; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new NanameguriSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 10));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 10 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 10).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		// System.out.println(field);
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
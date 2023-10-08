package myamya.other.solver.oneroom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common;
import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.RoomMaker;
import myamya.other.solver.Solver;

public class OneroomSolver implements Solver {
	public static class OneroomGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class OneroomSolverForGenerator extends OneroomSolver {
			private final int limit;

			public OneroomSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
				try {
					while (!field.isSolved()) {
						String befStr = field.getStateDump();
						if (!field.solveAndCheck()) {
							return -1;
						}
						int recursiveCnt = 0;
						while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
							if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
								return -1;
							}
							recursiveCnt++;
						}
						if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
							return -1;
						}
					}
				} catch (CountOverException e) {
					return -1;
				}
				return count;
			}

			@Override
			protected boolean candSolve(Field field, int recursive) {
				if (this.count >= limit) {
					throw new CountOverException();
				} else {
					return super.candSolve(field, recursive);
				}
			}
		}

		private final int height;
		private final int width;

		public OneroomGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new OneroomGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			OneroomSolver.Field wkField = new OneroomSolver.Field(height, width,
					RoomMaker.roomMake(height, width, -1, -1));
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < height * width; i++) {
				indexList.add(i);
			}
			Collections.shuffle(indexList);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				while (!wkField.isSolved()) {
					int yIndex = indexList.get(index) / width;
					int xIndex = indexList.get(index) % width;
					if (wkField.masu[yIndex][xIndex] == Masu.SPACE) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							OneroomSolver.Field virtual = new OneroomSolver.Field(wkField);
							if (masuNum < 1) {
								virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK;
							} else if (masuNum < 2) {
								virtual.masu[yIndex][xIndex] = Masu.BLACK;
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.masu = virtual.masu;
								break;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new OneroomSolver.Field(height, width, RoomMaker.roomMake(height, width, -1, -1));
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 部屋の数字を決める
				boolean existBlack = false;
				List<Integer> roomsIndexList = new ArrayList<>();
				for (int i = 0; i < wkField.getRooms().size(); i++) {
					roomsIndexList.add(i);
					Room room = wkField.getRooms().get(i);
					int blackCnt = 0;
					for (Position pos : room.getMember()) {
						if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
							existBlack = true;
							blackCnt++;
						}
					}
					room.blackCnt = blackCnt;
				}
				if (!existBlack) {
					// 全白ます問題は出ないようにする
					wkField = new OneroomSolver.Field(height, width, RoomMaker.roomMake(height, width, -1, -1));
					index = 0;
					continue;
				}
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new OneroomSolverForGenerator(wkField, 100).solve2();
				if (level < 0) {
					// 解けなければやり直し
					wkField = new OneroomSolver.Field(height, width, RoomMaker.roomMake(height, width, -1, -1));
					index = 0;
				} else {
					Collections.shuffle(roomsIndexList);
					for (int i : roomsIndexList) {
						OneroomSolver.Field virtual = new OneroomSolver.Field(wkField);
						virtual.getRooms().get(i).blackCnt = -1;
						int solveResult = new OneroomSolverForGenerator(virtual, 500).solve2();
						if (solveResult >= 0) {
							wkField.getRooms().get(i).blackCnt = -1;
							level = solveResult;
						}
					}
					break;
				}
			}
			// 横壁設定
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
					boolean isWall = true;
					Position pos = new Position(yIndex, xIndex);
					for (Room room : wkField.getRooms()) {
						if (room.getMember().contains(pos)) {
							Position rightPos = new Position(yIndex, xIndex + 1);
							if (room.getMember().contains(rightPos)) {
								isWall = false;
								break;
							}
						}
					}
					wkField.yokoWall[yIndex][xIndex] = isWall;
				}
			}
			// 縦壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean isWall = true;
					Position pos = new Position(yIndex, xIndex);
					for (Room room : wkField.getRooms()) {
						if (room.getMember().contains(pos)) {
							Position downPos = new Position(yIndex + 1, xIndex);
							if (room.getMember().contains(downPos)) {
								isWall = false;
								break;
							}
						}
					}
					wkField.tateWall[yIndex][xIndex] = isWall;
				}
			}
			level = (int) Math.sqrt(level * 4 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(部屋数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1
							|| wkField.getYokoWall()[yIndex][xIndex];
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1
							|| wkField.getTateWall()[yIndex][xIndex];
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 数字描画
			for (Room room : wkField.getRooms()) {
				int roomBlackCount = room.getBlackCnt();
				if (roomBlackCount != -1) {
					String roomBlackCountStr;
					String wkstr = String.valueOf(roomBlackCount);
					int idx = HALF_NUMS.indexOf(wkstr);
					if (idx >= 0) {
						roomBlackCountStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						roomBlackCountStr = wkstr;
					}
					Position numberMasuPos = room.getNumberMasuPos();
					String fillColor = wkField.getMasu()[numberMasuPos.getyIndex()][numberMasuPos
							.getxIndex()] == Common.Masu.BLACK ? "white" : "black";
					sb.append("<text y=\"" + (numberMasuPos.getyIndex() * baseSize + baseSize + margin - 5) + "\" x=\""
							+ (numberMasuPos.getxIndex() * baseSize + baseSize + 2) + "\" fill=\"" + fillColor
							+ "\" font-size=\"" + (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">" + roomBlackCountStr + "</text>");
				}
			}
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			System.out.println(wkField.getPuzPreURL());
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}

	}

	public static class Room {
		@Override
		public String toString() {
			return "Room [blackCnt=" + blackCnt + ", member=" + member + "]";
		}

		// 黒マスが何マスあるか。数字がない場合は-1
		private int blackCnt;
		// 部屋に属するマスの集合
		private final Set<Position> member;

		public Room(int capacity, Set<Position> member) {
			this.blackCnt = capacity;
			this.member = member;
		}

		public int getBlackCnt() {
			return blackCnt;
		}

		public Set<Position> getMember() {
			return member;
		}

		// 一番左→一番上の位置を返す。画面の数字描画用。
		public Position getNumberMasuPos() {
			int yIndex = Integer.MAX_VALUE;
			int xIndex = Integer.MAX_VALUE;
			for (Position pos : member) {
				if (pos.getxIndex() < xIndex) {
					xIndex = pos.getxIndex();
				}
			}
			for (Position pos : member) {
				if (pos.getxIndex() == xIndex && pos.getyIndex() < yIndex) {
					yIndex = pos.getyIndex();
				}
			}
			return new Position(yIndex, xIndex);
		}

	}

	public static class WallPos {
		@Override
		public String toString() {
			return "Room [pos=" + pos + ", isYokoWall=" + isYokoWall + "]";
		}

		// 黒マスが何マスあるか。数字がない場合は-1
		private final Position pos;
		// 部屋に属するマスの集合
		private final boolean isYokoWall;

		public WallPos(Position pos, boolean isYokoWall) {
			this.pos = pos;
			this.isYokoWall = isYokoWall;
		}

		public Position getPos() {
			return pos;
		}

		public boolean isYokoWall() {
			return isYokoWall;
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<Room> rooms;
		// 壁が隔てている部屋誤とのマップ
		private final Map<String, List<WallPos>> wallsMap;

		public Masu[][] getMasu() {
			return masu;
		}

		public List<Room> getRooms() {
			return rooms;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://puzz.link/p?oneroom/" + getXLength() + "/" + getYLength() + "/");
			for (int i = 0; i < getYLength() * (getXLength() - 1); i++) {
				int yIndex1 = i / (getXLength() - 1);
				int xIndex1 = i % (getXLength() - 1);
				i++;
				int yIndex2 = -1;
				int xIndex2 = -1;
				if (i < getYLength() * (getXLength() - 1)) {
					yIndex2 = i / (getXLength() - 1);
					xIndex2 = i % (getXLength() - 1);
				}
				i++;
				int yIndex3 = -1;
				int xIndex3 = -1;
				if (i < getYLength() * (getXLength() - 1)) {
					yIndex3 = i / (getXLength() - 1);
					xIndex3 = i % (getXLength() - 1);
				}
				i++;
				int yIndex4 = -1;
				int xIndex4 = -1;
				if (i < getYLength() * (getXLength() - 1)) {
					yIndex4 = i / (getXLength() - 1);
					xIndex4 = i % (getXLength() - 1);
				}
				i++;
				int yIndex5 = -1;
				int xIndex5 = -1;
				if (i < getYLength() * (getXLength() - 1)) {
					yIndex5 = i / (getXLength() - 1);
					xIndex5 = i % (getXLength() - 1);
				}
				int num = 0;
				if (yIndex1 != -1 && xIndex1 != -1 && yokoWall[yIndex1][xIndex1]) {
					num = num + 16;
				}
				if (yIndex2 != -1 && xIndex2 != -1 && yokoWall[yIndex2][xIndex2]) {
					num = num + 8;
				}
				if (yIndex3 != -1 && xIndex3 != -1 && yokoWall[yIndex3][xIndex3]) {
					num = num + 4;
				}
				if (yIndex4 != -1 && xIndex4 != -1 && yokoWall[yIndex4][xIndex4]) {
					num = num + 2;
				}
				if (yIndex5 != -1 && xIndex5 != -1 && yokoWall[yIndex5][xIndex5]) {
					num = num + 1;
				}
				sb.append(ALPHABET_AND_NUMBER.substring(num, num + 1));
			}
			for (int i = 0; i < (getYLength() - 1) * getXLength(); i++) {
				int yIndex1 = i / getXLength();
				int xIndex1 = i % getXLength();
				i++;
				int yIndex2 = -1;
				int xIndex2 = -1;
				if (i < (getYLength() - 1) * getXLength()) {
					yIndex2 = i / getXLength();
					xIndex2 = i % getXLength();
				}
				i++;
				int yIndex3 = -1;
				int xIndex3 = -1;
				if (i < (getYLength() - 1) * getXLength()) {
					yIndex3 = i / getXLength();
					xIndex3 = i % getXLength();
				}
				i++;
				int yIndex4 = -1;
				int xIndex4 = -1;
				if (i < (getYLength() - 1) * getXLength()) {
					yIndex4 = i / getXLength();
					xIndex4 = i % getXLength();
				}
				i++;
				int yIndex5 = -1;
				int xIndex5 = -1;
				if (i < (getYLength() - 1) * getXLength()) {
					yIndex5 = i / getXLength();
					xIndex5 = i % getXLength();
				}
				int num = 0;
				if (yIndex1 != -1 && xIndex1 != -1 && tateWall[yIndex1][xIndex1]) {
					num = num + 16;
				}
				if (yIndex2 != -1 && xIndex2 != -1 && tateWall[yIndex2][xIndex2]) {
					num = num + 8;
				}
				if (yIndex3 != -1 && xIndex3 != -1 && tateWall[yIndex3][xIndex3]) {
					num = num + 4;
				}
				if (yIndex4 != -1 && xIndex4 != -1 && tateWall[yIndex4][xIndex4]) {
					num = num + 2;
				}
				if (yIndex5 != -1 && xIndex5 != -1 && tateWall[yIndex5][xIndex5]) {
					num = num + 1;
				}
				sb.append(ALPHABET_AND_NUMBER.substring(num, num + 1));
			}
			int interval = 0;
			for (int i = 0; i < getRooms().size(); i++) {
				if (getRooms().get(i).blackCnt == -1) {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = getRooms().get(i).blackCnt;
					String numStr = Integer.toHexString(num);
					if (numStr.length() == 2) {
						numStr = "-" + numStr;
					} else if (numStr.length() == 3) {
						numStr = "+" + numStr;
					}
					if (interval == 0) {
						sb.append(numStr);
					} else {
						sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
						sb.append(numStr);
						interval = 0;
					}
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public String getHintCount() {
			return String.valueOf(getRooms().size());
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			// パラメータを解釈して壁の有無を入れる
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
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
						yokoWall[(cnt - mod + 0) / (getXLength() - 1)][(cnt - mod + 0) % (getXLength() - 1)] = bit / 16
								% 2 == 1;
					}
					if (mod >= 1) {
						yokoWall[(cnt - mod + 1) / (getXLength() - 1)][(cnt - mod + 1) % (getXLength() - 1)] = bit / 8
								% 2 == 1;
					}
					if (mod >= 2) {
						yokoWall[(cnt - mod + 2) / (getXLength() - 1)][(cnt - mod + 2) % (getXLength() - 1)] = bit / 4
								% 2 == 1;
					}
					if (mod >= 3) {
						yokoWall[(cnt - mod + 3) / (getXLength() - 1)][(cnt - mod + 3) % (getXLength() - 1)] = bit / 2
								% 2 == 1;
					}
					if (mod >= 4) {
						yokoWall[(cnt - mod + 4) / (getXLength() - 1)][(cnt - mod + 4) % (getXLength() - 1)] = bit / 1
								% 2 == 1;
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
						tateWall[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16 % 2 == 1;
					}
					if (mod >= 1) {
						tateWall[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1;
					}
					if (mod >= 2) {
						tateWall[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1;
					}
					if (mod >= 3) {
						tateWall[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1;
					}
					if (mod >= 4) {
						tateWall[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1;
					}
				}
			}
			// 縦と横の壁の関係からにょろっと部屋を決めていく
			List<Integer> blackCntList = new ArrayList<>();
			for (; readPos < param.length(); readPos++) {
				char ch = param.charAt(readPos);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					for (int i = 0; i < interval + 1; i++) {
						// 数字がない部屋の場合は、部屋の数字は-1として扱う。
						blackCntList.add(-1);
					}
				} else {
					// 16 - 255は '-'
					// 256 - 999は '+'
					int blackCnt;
					if (ch == '-') {
						blackCnt = Integer.parseInt("" + param.charAt(readPos + 1) + param.charAt(readPos + 2), 16);
						readPos++;
						readPos++;
					} else if (ch == '+') {
						blackCnt = Integer.parseInt(
								"" + param.charAt(readPos + 1) + param.charAt(readPos + 2) + param.charAt(readPos + 3),
								16);
						readPos++;
						readPos++;
						readPos++;
					} else {
						blackCnt = Integer.parseInt(String.valueOf(ch), 16);
					}
					blackCntList.add(blackCnt);
				}
			}
			rooms = new ArrayList<>();
			int blackCntListIndex = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean alreadyRoomed = false;
					for (Room room : getRooms()) {
						if (room.getMember().contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinuePosSet(pos, continuePosSet);
						getRooms().add(new Room(blackCntList.get(blackCntListIndex), continuePosSet));
						blackCntListIndex++;
					}
				}
			}
			wallsMap = makeWallsMap();
		}

		private Map<String, List<WallPos>> makeWallsMap() {
			Map<String, List<WallPos>> result = new HashMap<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoWall[yIndex][xIndex]) {
						String mapKey = "";
						for (int i = 0; i < getRooms().size(); i++) {
							Room room = getRooms().get(i);
							if (room.getMember().contains(new Position(yIndex, xIndex))
									|| room.getMember().contains(new Position(yIndex, xIndex + 1))) {
								mapKey = mapKey + i + "+";
							}
						}
						List<WallPos> wallPosList = result.get(mapKey);
						if (wallPosList == null) {
							wallPosList = new ArrayList<>();
							result.put(mapKey, wallPosList);
						}
						wallPosList.add(new WallPos(new Position(yIndex, xIndex), true));
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex]) {
						String mapKey = "";
						for (int i = 0; i < getRooms().size(); i++) {
							Room room = getRooms().get(i);
							if (room.getMember().contains(new Position(yIndex, xIndex))
									|| room.getMember().contains(new Position(yIndex + 1, xIndex))) {
								mapKey = mapKey + i + "+";
							}
						}
						List<WallPos> wallPosList = result.get(mapKey);
						if (wallPosList == null) {
							wallPosList = new ArrayList<>();
							result.put(mapKey, wallPosList);
						}
						wallPosList.add(new WallPos(new Position(yIndex, xIndex), false));
					}
				}
			}
			return result;
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			yokoWall = new boolean[other.getYLength()][other.getXLength() - 1];
			tateWall = new boolean[other.getYLength() - 1][other.getXLength()];
			rooms = new ArrayList<>();
			wallsMap = new HashMap<>();
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
			for (Room room : other.getRooms()) {
				getRooms().add(new Room(room.getBlackCnt(), new HashSet<>(room.getMember())));
			}
			for (Entry<String, List<WallPos>> e : other.wallsMap.entrySet()) {
				wallsMap.put(e.getKey(), new ArrayList<>(e.getValue()));
			}
		}

		public Field(int height, int width, List<Set<Position>> rooms) {
			masu = new Masu[height][width];
			this.rooms = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					for (Iterator<Set<Position>> iterator = rooms.iterator(); iterator.hasNext();) {
						Set<Position> roomPosSet = iterator.next();
						if (roomPosSet.contains(new Position(yIndex, xIndex))) {
							this.getRooms().add(new Room(-1, new HashSet<>(roomPosSet)));
							iterator.remove();
							break;
						}
					}
				}
			}
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			// 横壁設定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					boolean isWall = true;
					Position pos = new Position(yIndex, xIndex);
					for (Room room : this.getRooms()) {
						if (room.getMember().contains(pos)) {
							Position rightPos = new Position(yIndex, xIndex + 1);
							if (room.getMember().contains(rightPos)) {
								isWall = false;
								break;
							}
						}
					}
					yokoWall[yIndex][xIndex] = isWall;
				}
			}
			// 縦壁描画
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					boolean isWall = true;
					Position pos = new Position(yIndex, xIndex);
					for (Room room : this.getRooms()) {
						if (room.getMember().contains(pos)) {
							Position downPos = new Position(yIndex + 1, xIndex);
							if (room.getMember().contains(downPos)) {
								isWall = false;
								break;
							}
						}
					}
					tateWall[yIndex][xIndex] = isWall;
				}
			}
			wallsMap = makeWallsMap();
		}

		// posを起点に上下左右に壁または白確定でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
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
					sb.append(masu[yIndex][xIndex]);
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex] ? "□" : "　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex] ? "□" : "　");
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
			return sb.toString();
		}

		/**
		 * 部屋のマスを埋める。黒マス不足・過剰はfalseを返す。
		 */
		public boolean roomSolve() {
			for (Room room : getRooms()) {
				if (room.getBlackCnt() == -1) {
					continue;
				}
				// 部屋に対する調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room.getMember()) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < room.getBlackCnt()) {
					// 黒マス不足
					return false;
				}
				// 置かねばならない黒マスの数
				int retainBlackCnt = room.getBlackCnt() - blackCnt;
				if (retainBlackCnt < 0) {
					// 黒マス超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (Position pos : room.getMember()) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
					for (Position pos : room.getMember()) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 黒マス隣接セルを白マスにする。 黒マス隣接セルが黒マスの場合falseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
						if (masuUp == Masu.BLACK || masuRight == Masu.BLACK || masuDown == Masu.BLACK
								|| masuLeft == Masu.BLACK) {
							return false;
						}
						if (masuUp == Masu.SPACE) {
							masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masuRight == Masu.SPACE) {
							masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masuDown == Masu.SPACE) {
							masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masuLeft == Masu.SPACE) {
							masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!oneRoomSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
				if (!roomConectSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 部屋を隔てて両側が白マスは1つまで
		 */
		private boolean oneRoomSolve() {
			for (List<WallPos> wallPosList : wallsMap.values()) {
				boolean isExistDoor = false;
				Set<Position> blackCand = new HashSet<>();
				for (WallPos wallPos : wallPosList) {
					Masu masu1 = masu[wallPos.pos.getyIndex()][wallPos.pos.getxIndex()];
					Masu masu2;
					if (wallPos.isYokoWall()) {
						masu2 = masu[wallPos.pos.getyIndex()][wallPos.pos.getxIndex() + 1];
					} else {
						masu2 = masu[wallPos.pos.getyIndex() + 1][wallPos.pos.getxIndex()];
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK) {
						if (isExistDoor) {
							return false;
						} else {
							isExistDoor = true;
						}
					} else if (masu1 == Masu.SPACE && masu2 == Masu.NOT_BLACK) {
						blackCand.add(wallPos.pos);
					} else if (masu1 == Masu.NOT_BLACK && masu2 == Masu.SPACE) {
						if (wallPos.isYokoWall()) {
							blackCand.add(new Position(wallPos.pos.getyIndex(), wallPos.pos.getxIndex() + 1));
						} else {
							blackCand.add(new Position(wallPos.pos.getyIndex() + 1, wallPos.pos.getxIndex()));
						}
					}
				}
				if (isExistDoor) {
					for (Position blackCandPos : blackCand) {
						masu[blackCandPos.getyIndex()][blackCandPos.getxIndex()] = Masu.BLACK;
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
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
							whitePosSet.add(whitePos);
							setContinuePosSet(whitePos, whitePosSet, null);
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
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 部屋内で白マスが1つながりになっていない場合falseを返す。
		 */
		private boolean roomConectSolve() {
			for (Room room : getRooms()) {
				Set<Position> whitePosSet = new HashSet<>();
				for (Position pos : room.getMember()) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
						if (whitePosSet.size() == 0) {
							whitePosSet.add(pos);
							setContinueSameRoomPosSet(pos, whitePosSet, null);
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
		 * posを起点に上下左右に同じ部屋で黒確定でないマスを無制限につなげていく。
		 */
		private void setContinueSameRoomPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueSameRoomPosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueSameRoomPosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueSameRoomPosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueSameRoomPosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
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

	protected final Field field;
	protected int count = 0;

	public OneroomSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public OneroomSolver(Field field) {
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
		System.out.println(new OneroomSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 4));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 4 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 4).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
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
		} else {
			// どちらにしても理論
			for (int y = 0; y < field.getYLength(); y++) {
				for (int x = 0; x < field.getXLength(); x++) {
					if (virtual2.masu[y][x] == virtual.masu[y][x]) {
						field.masu[y][x] = virtual.masu[y][x];
					}
				}
			}
		}
		return true;
	}

}

package myamya.other.solver.mannequin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
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

public class MannequinSolver implements Solver {
	public static class MannequinGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class MannequinSolverForGenerator extends MannequinSolver {
			private final int limit;

			public MannequinSolverForGenerator(Field field, int limit) {
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

		public MannequinGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new MannequinGenerator(6, 6).generate();
		}

		@Override
		public GeneratorResult generate() {
			MannequinSolver.Field wkField = new MannequinSolver.Field(height, width,
					RoomMaker.roomMake(height, width, 2, height));
			List<Position> indexList = new ArrayList<>();
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					indexList.add(new Position(yIndex, xIndex));
				}
			}
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				// マスを配置
				while (true) {
					boolean isOk = false;
					// Collections.shuffle(indexList);
					for (Position pos : indexList) {
						if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							isOk = false;
							List<Integer> numIdxList = new ArrayList<>();
							for (int i = 0; i < 2; i++) {
								numIdxList.add(i);
							}
							Collections.shuffle(numIdxList);
							for (int masuNum : numIdxList) {
								MannequinSolver.Field virtual = new Field(wkField);
								if (masuNum < 1) {
									virtual.masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
								} else if (masuNum < 2) {
									virtual.masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
								}
								if (virtual.solveAndCheck()) {
									isOk = true;
									wkField.masu = virtual.masu;
									break;
								}
							}
							if (!isOk) {
								break;
							}
						}
					}
					if (!isOk) {
						// 破綻したら0から作り直す。
						wkField = new MannequinSolver.Field(height, width,
								RoomMaker.roomMake(height, width, 2, height));
					} else {
						break;
					}
				}
				// 部屋の数字を埋める
				List<Integer> roomIdxList = new ArrayList<>();
				for (Room room : wkField.rooms) {
					roomIdxList.add(roomIdxList.size());
					room.count = wkField.getMyDistance(room);
				}
				// System.out.println(wkField);
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new MannequinSolverForGenerator(wkField, 1000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new MannequinSolver.Field(height, width, RoomMaker.roomMake(height, width, 2, height));
				} else {
					Collections.shuffle(roomIdxList);
					for (Integer roomIdx : roomIdxList) {
						MannequinSolver.Field virtual = new MannequinSolver.Field(wkField);
						virtual.rooms.get(roomIdx).count = -1;
						int solveResult = new MannequinSolverForGenerator(virtual, 10000).solve2();
						if (solveResult != -1) {
							level = solveResult;
							wkField.rooms.get(roomIdx).count = -1;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 2 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字/部屋：" + wkField.getHintCount().split("/")[1] + "/"
					+ wkField.getHintCount().split("/")[0] + ")";
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
			for (MannequinSolver.Room room : wkField.getRooms()) {
				int roomBlackCount = room.getCount();
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
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			System.out.println(url);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}

	}

	public static class Room {
		@Override
		public String toString() {
			return "Room [count=" + count + ", member=" + member + "]";
		}

		// 黒マスの座標差分。数字がない場合は-1
		private int count;
		// 部屋に属するマスの集合
		private final List<Position> member;
		// 自分の部屋のマスから、他の自分の部屋のマスまでの距離
		private final Map<Position, Map<Integer, Set<Position>>> distanceMap;

		public Room(int capacity, List<Position> member, Map<Position, Map<Integer, Set<Position>>> distanceMap) {
			this.count = capacity;
			this.member = member;
			this.distanceMap = distanceMap;
			this.member.sort(new Comparator<Position>() {
				@Override
				public int compare(Position o1, Position o2) {
					return o1.getyIndex() * 100 + o1.getxIndex() - (o2.getyIndex() * 100 + o2.getxIndex());
				}
			});
		}

		public Room(Room other) {
			this.count = other.count;
			this.member = other.member;
			this.distanceMap = other.distanceMap;
			this.member.sort(new Comparator<Position>() {
				@Override
				public int compare(Position o1, Position o2) {
					return o1.getyIndex() * 100 + o1.getxIndex() - (o2.getyIndex() * 100 + o2.getxIndex());
				}
			});
		}

		public int getCount() {
			return count;
		}

		public List<Position> getMember() {
			return member;
		}

		public Map<Position, Map<Integer, Set<Position>>> getDistanceMap() {
			return distanceMap;
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

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";
		static final int BLACK_CNT = 1;
		// マスの情報
		private Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private List<Room> rooms;
		// 部屋の隣接情報
		private final Map<Integer, Set<Integer>> nextRoomMap;

		public String getHintCount() {
			int numberCnt = 0;
			for (Room room : rooms) {
				if (room.getCount() != -1) {
					numberCnt++;
				}
			}
			return String.valueOf(rooms.size() + "/" + numberCnt);
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://puzz.link/p?mannequin/" + getXLength() + "/" + getYLength() + "/");
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
			for (int i = 0; i < rooms.size(); i++) {
				if (rooms.get(i).getCount() == -1) {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = rooms.get(i).getCount();
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

		public Masu[][] getMasu() {
			return masu;
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
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
					for (Room room : rooms) {
						if (room.getMember().contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinuePosSet(pos, continuePosSet);
						// 距離情報を作る
						Map<Position, Map<Integer, Set<Position>>> distanceMap = new HashMap<>();
						for (Position onePos : continuePosSet) {
							Map<Integer, Set<Position>> wkMap = new HashMap<>();
							distanceMap.put(onePos, wkMap);
							Set<Position> wkSet = new HashSet<Position>();
							wkSet.add(onePos);
							wkMap.put(-1, wkSet);
							int nowTarget = -1;
							Set<Position> alreadyPosSet = new HashSet<Position>();
							alreadyPosSet.add(onePos);
							while (alreadyPosSet.size() != continuePosSet.size()) {
								wkSet = new HashSet<Position>();
								for (Position nowPos : wkMap.get(nowTarget)) {
									if (nowPos.getyIndex() != 0) {
										Position nextPos = new Position(nowPos.getyIndex() - 1, nowPos.getxIndex());
										if (!alreadyPosSet.contains(nextPos)
												&& !tateWall[nowPos.getyIndex() - 1][nowPos.getxIndex()]) {
											wkSet.add(nextPos);
											alreadyPosSet.add(nextPos);
										}
									}
									if (nowPos.getxIndex() != getXLength() - 1) {
										Position nextPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() + 1);
										if (!alreadyPosSet.contains(nextPos)
												&& !yokoWall[nowPos.getyIndex()][nowPos.getxIndex()]) {
											wkSet.add(nextPos);
											alreadyPosSet.add(nextPos);
										}
									}
									if (nowPos.getyIndex() != getYLength() - 1) {
										Position nextPos = new Position(nowPos.getyIndex() + 1, nowPos.getxIndex());
										if (!alreadyPosSet.contains(nextPos)
												&& !tateWall[nowPos.getyIndex()][nowPos.getxIndex()]) {
											wkSet.add(nextPos);
											alreadyPosSet.add(nextPos);
										}
									}
									if (nowPos.getxIndex() != 0) {
										Position nextPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() - 1);
										if (!alreadyPosSet.contains(nextPos)
												&& !yokoWall[nowPos.getyIndex()][nowPos.getxIndex() - 1]) {
											wkSet.add(nextPos);
											alreadyPosSet.add(nextPos);
										}
									}
								}
								nowTarget++;
								wkMap.put(nowTarget, wkSet);
							}
						}
						rooms.add(new Room(blackCntList.get(blackCntListIndex), new ArrayList<>(continuePosSet),
								distanceMap));
						blackCntListIndex++;
					}
				}
			}
			// 部屋の隣接情報
			nextRoomMap = new HashMap<>();
			for (int i = 0; i < rooms.size(); i++) {
				Room keyRoom = rooms.get(i);
				Set<Integer> nextRooms = new HashSet<>();
				for (int j = 0; j < rooms.size(); j++) {
					Room otherRoom = rooms.get(j);
					if (keyRoom == otherRoom) {
						continue;
					}
					for (Position pos : keyRoom.getMember()) {
						if (otherRoom.getMember().contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))
								|| otherRoom.getMember().contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))
								|| otherRoom.getMember().contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))
								|| otherRoom.getMember().contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
							nextRooms.add(j);
							break;
						}
					}
				}
				nextRoomMap.put(i, nextRooms);
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = new ArrayList<>();
			for (Room room : other.rooms) {
				rooms.add(new Room(room));
			}
			nextRoomMap = other.nextRoomMap;
		}

		public Field(int height, int width, List<Set<Position>> madeRooms) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			// パラメータを解釈して壁の有無を入れる
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			// 横壁設定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					boolean isWall = true;
					Position pos = new Position(yIndex, xIndex);
					for (Set<Position> room : madeRooms) {
						if (room.contains(pos)) {
							Position rightPos = new Position(yIndex, xIndex + 1);
							if (room.contains(rightPos)) {
								isWall = false;
								break;
							}
						}
					}
					yokoWall[yIndex][xIndex] = isWall;
				}
			}
			// 縦壁設定
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					boolean isWall = true;
					Position pos = new Position(yIndex, xIndex);
					for (Set<Position> room : madeRooms) {
						if (room.contains(pos)) {
							Position downPos = new Position(yIndex + 1, xIndex);
							if (room.contains(downPos)) {
								isWall = false;
								break;
							}
						}
					}
					tateWall[yIndex][xIndex] = isWall;
				}
			}
			rooms = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean alreadyRoomed = false;
					for (Room room : rooms) {
						if (room.getMember().contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinuePosSet(pos, continuePosSet);
						// 距離情報を作る
						Map<Position, Map<Integer, Set<Position>>> distanceMap = new HashMap<>();
						for (Position onePos : continuePosSet) {
							Map<Integer, Set<Position>> wkMap = new HashMap<>();
							distanceMap.put(onePos, wkMap);
							Set<Position> wkSet = new HashSet<Position>();
							wkSet.add(onePos);
							wkMap.put(-1, wkSet);
							int nowTarget = -1;
							Set<Position> alreadyPosSet = new HashSet<Position>();
							alreadyPosSet.add(onePos);
							while (alreadyPosSet.size() != continuePosSet.size()) {
								wkSet = new HashSet<Position>();
								for (Position nowPos : wkMap.get(nowTarget)) {
									if (nowPos.getyIndex() != 0) {
										Position nextPos = new Position(nowPos.getyIndex() - 1, nowPos.getxIndex());
										if (!alreadyPosSet.contains(nextPos)
												&& !tateWall[nowPos.getyIndex() - 1][nowPos.getxIndex()]) {
											wkSet.add(nextPos);
											alreadyPosSet.add(nextPos);
										}
									}
									if (nowPos.getxIndex() != getXLength() - 1) {
										Position nextPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() + 1);
										if (!alreadyPosSet.contains(nextPos)
												&& !yokoWall[nowPos.getyIndex()][nowPos.getxIndex()]) {
											wkSet.add(nextPos);
											alreadyPosSet.add(nextPos);
										}
									}
									if (nowPos.getyIndex() != getYLength() - 1) {
										Position nextPos = new Position(nowPos.getyIndex() + 1, nowPos.getxIndex());
										if (!alreadyPosSet.contains(nextPos)
												&& !tateWall[nowPos.getyIndex()][nowPos.getxIndex()]) {
											wkSet.add(nextPos);
											alreadyPosSet.add(nextPos);
										}
									}
									if (nowPos.getxIndex() != 0) {
										Position nextPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() - 1);
										if (!alreadyPosSet.contains(nextPos)
												&& !yokoWall[nowPos.getyIndex()][nowPos.getxIndex() - 1]) {
											wkSet.add(nextPos);
											alreadyPosSet.add(nextPos);
										}
									}
								}
								nowTarget++;
								wkMap.put(nowTarget, wkSet);
							}
						}
						rooms.add(new Room(-1, new ArrayList<>(continuePosSet), distanceMap));
					}
				}
			}
			// 部屋の隣接情報
			nextRoomMap = new HashMap<>();
			for (int i = 0; i < rooms.size(); i++) {
				Room keyRoom = rooms.get(i);
				Set<Integer> nextRooms = new HashSet<>();
				for (int j = 0; j < rooms.size(); j++) {
					Room otherRoom = rooms.get(j);
					if (keyRoom == otherRoom) {
						continue;
					}
					for (Position pos : keyRoom.getMember()) {
						if (otherRoom.getMember().contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))
								|| otherRoom.getMember().contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))
								|| otherRoom.getMember().contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))
								|| otherRoom.getMember().contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
							nextRooms.add(j);
							break;
						}
					}
				}
				nextRoomMap.put(i, nextRooms);
			}
		}

		// posを起点に上下左右に部屋壁でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex()][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]) {
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
						sb.append(yokoWall[yIndex][xIndex] == true ? "□" : "　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex] == true ? "□" : "　");
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
		 * 部屋の黒マスは2つ。違反はfalse
		 */
		public boolean roomSolve() {
			for (Room room : rooms) {
				int blackCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room.getMember()) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < 2) {
					// 黒マス不足
					return false;
				}
				if (blackCnt > 2) {
					// 黒マス超過
					return false;
				} else if (blackCnt == 2) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (Position pos : room.getMember()) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				} else if (blackCnt + spaceCnt == 2) {
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
		 * 数字がある部屋で黒マスがある場合距離に合わないマスは白マス。
		 */
		public boolean distanceSolve() {
			for (Room room : rooms) {
				if (room.getCount() != -1) {
					for (Position pos : room.getMember()) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
							for (Entry<Integer, Set<Position>> entry : room.getDistanceMap().get(pos).entrySet()) {
								if (entry.getKey() != -1 && entry.getKey() != room.getCount()) {
									for (Position otherPos : entry.getValue()) {
										if (masu[otherPos.getyIndex()][otherPos.getxIndex()] == Masu.BLACK) {
											return false;
										}
										masu[otherPos.getyIndex()][otherPos.getxIndex()] = Masu.NOT_BLACK;
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
		 * 白マスがひとつながりにならない場合Falseを返す。
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
		 * 同じ距離のマスが隣り合わないようにする。
		 */
		public boolean nextSolve() {
			for (int i = 0; i < rooms.size(); i++) {
				Room room = rooms.get(i);
				int myDistance = getMyDistance(room);
				if (myDistance != -1) {
					for (int j : nextRoomMap.get(i)) {
						Room otherRoom = rooms.get(j);
						int otherDistance = getMyDistance(otherRoom);
						if (myDistance == otherDistance) {
							return false;
						}
					}
				}
				// 隣の部屋の制約により禁止される距離から、黒マスが置けないマスを除外する
				Set<Integer> banDistanceList = new HashSet<>();
				for (int j : nextRoomMap.get(i)) {
					Room otherRoom = rooms.get(j);
					int otherDistance = getMyDistance(otherRoom);
					if (otherDistance != -1) {
						banDistanceList.add(otherDistance);
					}
				}
				for (Entry<Position, Map<Integer, Set<Position>>> entry : room.getDistanceMap().entrySet()) {
					Position pos = entry.getKey();
					boolean isOk = false;
					for (Integer myDistanceCand : entry.getValue().keySet()) {
						if (myDistanceCand != -1 && !banDistanceList.contains(myDistanceCand)) {
							isOk = true;
						}
					}
					if (!isOk) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
							return false;
						}
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					}
				}
			}
			return true;
		}

		/**
		 * roomの部屋の黒マス距離を返す。判定できない場合-1。
		 */
		private int getMyDistance(Room room) {
			int myDistance = room.getCount();
			if (myDistance == -1) {
				Position keepPos = null;
				for (Position pos : room.getMember()) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						if (keepPos == null) {
							keepPos = pos;
						} else {
							for (Entry<Integer, Set<Position>> entry : room.getDistanceMap().get(pos).entrySet()) {
								if (entry.getKey() != -1) {
									for (Position otherPos : entry.getValue()) {
										if (otherPos.equals(keepPos)) {
											return entry.getKey();
										}
									}
								}
							}
						}
					}
				}
			}
			return myDistance;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!distanceSolve()) {
				return false;
			}
			if (!nextSolve()) {
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
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public MannequinSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public MannequinSolver(Field field) {
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
		System.out.println(new MannequinSolver(height, width, param).solve());
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
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count / 2));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 2 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count / 2).toString() + "(Lv:" + level + ")";
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
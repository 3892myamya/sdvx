package myamya.other.solver.cocktail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common;
import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.PenpaEditGeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.PenpaEditLib;
import myamya.other.solver.PenpaEditLib.RoomWalls;
import myamya.other.solver.RoomMaker;
import myamya.other.solver.Solver;

public class CocktailSolver implements Solver {
	public static class CocktailGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class CocktailSolverForGenerator extends CocktailSolver {
			private final int limit;

			public CocktailSolverForGenerator(Field field, int limit) {
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
							if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt, 0, 0)) {
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
			protected boolean candSolve(Field field, int recursive, int initY, int initX) {
				if (this.count >= limit) {
					throw new CountOverException();
				} else {
					return super.candSolve(field, recursive, initY, initX);
				}
			}
		}

		private final int height;
		private final int width;

		public CocktailGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new CocktailGenerator(9, 9).generate();
		}

		@Override
		public GeneratorResult generate() {
			String solutionStr;
			CocktailSolver.Field wkField = new CocktailSolver.Field(height, width,
					RoomMaker.roomMake(height, width, -1, -1));
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
				while (true) {
					boolean isOk = false;
					Collections.shuffle(indexList);
					for (Position pos : indexList) {
						if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							isOk = false;
							List<Integer> numIdxList = new ArrayList<>();
							for (int i = 0; i < 3; i++) {
								numIdxList.add(i);
							}
							Collections.shuffle(numIdxList);
							for (int masuNum : numIdxList) {
								CocktailSolver.Field virtual = new Field(wkField);
								if (masuNum < 1) {
									virtual.masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
								} else if (masuNum < 3) {
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
						wkField = new CocktailSolver.Field(height, width, RoomMaker.roomMake(height, width, -1, -1));
					} else {
						break;
					}
				}
				// 解答の記憶
				solutionStr = PenpaEditLib.convertSolutionMasu(wkField.masu);
				// 数字を付与
				boolean existBlack = false;
				List<Integer> roomIdxList = new ArrayList<>();
				for (Room room : wkField.rooms) {
					int blackCnt = 0;
					for (Position pos : room.getMember()) {
						if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
							blackCnt++;
							existBlack = true;
						}
						wkField.masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
					}
					roomIdxList.add(roomIdxList.size());
					room.blackCnt = blackCnt;
				}
				if (!existBlack) {
					// 全白ます問題は出ないようにする
					wkField = new CocktailSolver.Field(height, width, RoomMaker.roomMake(height, width, -1, -1));
					continue;
				}
				// 解けるかな？
				level = new CocktailSolverForGenerator(wkField, 300).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new CocktailSolver.Field(height, width, RoomMaker.roomMake(height, width, -1, -1));
				} else {
					Collections.shuffle(roomIdxList);
					for (Integer roomIdx : roomIdxList) {
						CocktailSolver.Field virtual = new CocktailSolver.Field(wkField);
						virtual.rooms.get(roomIdx).blackCnt = -1;
						int solveResult = new CocktailSolverForGenerator(virtual, 3000).solve2();
						if (solveResult != -1) {
							wkField.rooms.get(roomIdx).blackCnt = -1;
							level = solveResult;
						}
					}
					break;
				}
			}
			String fieldStr = PenpaEditLib.convertNumbersRoomField(wkField.getNumbers(), wkField.yokoWall,
					wkField.tateWall);
//			System.out.println(fieldStr);
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字/部屋：" + wkField.getHintCount().split("/")[1] + "/"
					+ wkField.getHintCount().split("/")[0] + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">penpa-editで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					Common.Masu oneMasu = wkField.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" fill=\"" + "#555" + "\" width=\"" + (baseSize)
								+ "\" height=\"" + (baseSize) + "\">" + "</rect>");
					} else if (oneMasu.toString().equals("・")) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" fill=\"" + "palegreen" + "\" width=\""
								+ (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
				}
			}
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
			for (CocktailSolver.Room room : wkField.getRooms()) {
				int roomBlackCount = room.getBlackCnt();
				if (roomBlackCount != -1) {
					String roomBlackCountStr;
					String wkstr = String.valueOf(roomBlackCount);
					int index = HALF_NUMS.indexOf(wkstr);
					if (index >= 0) {
						roomBlackCountStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
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
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.(cocktail)");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new PenpaEditGeneratorResult(status, sb.toString(), link, level, "", fieldStr, solutionStr);
		}

	}

	public static class Room {
		@Override
		public String toString() {
			return "Room [blackCnt=" + blackCnt + ", member=" + member + "]";
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
	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		// マスの情報
		private Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<Room> rooms;

		public String getHintCount() {
			int numberCnt = 0;
			for (Room room : rooms) {
				if (room.getBlackCnt() != -1) {
					numberCnt++;
				}
			}
			return String.valueOf(rooms.size() + "/" + numberCnt);
		}

		public String getPuzPreURL() {
			return PenpaEditLib.PENPA_EDIT_DUMMY_URL;
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

		public Field(String fieldStr) {
			String[] fieldInfo = fieldStr.split("\n")[0].split(",");
			Integer yLength = Integer.valueOf(fieldInfo[2]);
			Integer xLength = Integer.valueOf(fieldInfo[1]);
			masu = new Masu[yLength][xLength];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			RoomWalls walls = PenpaEditLib.getRoomWalls(fieldStr);
			Integer[][] numbers = PenpaEditLib.getNumbers(fieldStr);
			yokoWall = walls.getYokoRoomWall();
			tateWall = walls.getTateRoomWall();
			rooms = new ArrayList<>();
			// 縦と横の壁の関係からにょろっと部屋を決めていく
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
						int capacity = -1;
						for (Position onePos : continuePosSet) {
							if (numbers[onePos.getyIndex()][onePos.getxIndex()] != null) {
								if (capacity == -1) {
									capacity = numbers[onePos.getyIndex()][onePos.getxIndex()];
								} else {
									throw new IllegalArgumentException();
								}
							}
						}
						rooms.add(new Room(capacity, continuePosSet));
					}
				}
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
				rooms.add(new Room(room.getBlackCnt(), room.getMember()));
			}
		}

		/**
		 * プレーンなフィールド生成。ジェネレータ用
		 */
		public Field(int height, int width, List<Set<Position>> rooms) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			// 横壁設定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					boolean isWall = true;
					Position pos = new Position(yIndex, xIndex);
					for (Set<Position> room : rooms) {
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
			// 縦壁描画
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					boolean isWall = true;
					Position pos = new Position(yIndex, xIndex);
					for (Set<Position> room : rooms) {
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
			this.rooms = new ArrayList<>();
			for (int i = 0; i < rooms.size(); i++) {
				this.rooms.add(new Room(-1, rooms.get(i)));
			}
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
						rooms.add(new Room(blackCntList.get(blackCntListIndex), continuePosSet));
						blackCntListIndex++;
					}
				}
			}
		}

		/**
		 * 部屋の数字情報を考慮して数字の位置情報配列を返す。ジェネレータ用
		 */
		public Integer[][] getNumbers() {
			Integer[][] numbers = new Integer[getYLength()][getXLength()];
			for (CocktailSolver.Room room : rooms) {
				int roomBlackCount = room.getBlackCnt();
				if (roomBlackCount != -1) {
					Position numberMasuPos = room.getNumberMasuPos();
					numbers[numberMasuPos.getyIndex()][numberMasuPos.getxIndex()] = roomBlackCount;
				}
			}
			return numbers;
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
						sb.append(yokoWall[yIndex][xIndex] == true ? "□" : "＊");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex] == true ? "□" : "＊");
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
			for (Room room : rooms) {
				if (room.getBlackCnt() != -1) {
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
			}
			return true;
		}

		/**
		 * 壁を挟んで黒マスが連続しない。
		 */
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					boolean wallUp = yIndex == 0 ? false : tateWall[yIndex - 1][xIndex];
					boolean wallRight = xIndex == getXLength() - 1 ? false : yokoWall[yIndex][xIndex];
					boolean wallDown = yIndex == getYLength() - 1 ? false : tateWall[yIndex][xIndex];
					boolean wallLeft = xIndex == 0 ? false : yokoWall[yIndex][xIndex - 1];
					if (wallUp) {
						if (masu[yIndex][xIndex] == Masu.BLACK && masu[yIndex - 1][xIndex] == Masu.BLACK) {
							return false;
						}
						if (masu[yIndex][xIndex] == Masu.BLACK && masu[yIndex - 1][xIndex] == Masu.SPACE) {
							masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masu[yIndex][xIndex] == Masu.SPACE && masu[yIndex - 1][xIndex] == Masu.BLACK) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (wallRight) {
						if (masu[yIndex][xIndex] == Masu.BLACK && masu[yIndex][xIndex + 1] == Masu.BLACK) {
							return false;
						}
						if (masu[yIndex][xIndex] == Masu.BLACK && masu[yIndex][xIndex + 1] == Masu.SPACE) {
							masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masu[yIndex][xIndex] == Masu.SPACE && masu[yIndex][xIndex + 1] == Masu.BLACK) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (wallDown) {
						if (masu[yIndex][xIndex] == Masu.BLACK && masu[yIndex + 1][xIndex] == Masu.BLACK) {
							return false;
						}
						if (masu[yIndex][xIndex] == Masu.BLACK && masu[yIndex + 1][xIndex] == Masu.SPACE) {
							masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masu[yIndex][xIndex] == Masu.SPACE && masu[yIndex + 1][xIndex] == Masu.BLACK) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (wallLeft) {
						if (masu[yIndex][xIndex] == Masu.BLACK && masu[yIndex][xIndex - 1] == Masu.BLACK) {
							return false;
						}
						if (masu[yIndex][xIndex] == Masu.BLACK && masu[yIndex][xIndex - 1] == Masu.SPACE) {
							masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
						}
						if (masu[yIndex][xIndex] == Masu.SPACE && masu[yIndex][xIndex - 1] == Masu.BLACK) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 置くと池ができるマスを白マスにする。 既に池ができている場合falseを返す。
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
		 * 部屋のマスは縦横ひとつながりになる。
		 */
		public boolean roomConnectSolve() {
			for (Room room : rooms) {
				Set<Position> blackPosSet = new HashSet<>();
				for (Position pos : room.getMember()) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						if (blackPosSet.size() == 0) {
							blackPosSet.add(pos);
							setContinuePosSet(pos, blackPosSet, room.getMember(), null);
						} else {
							if (!blackPosSet.contains(pos)) {
								return false;
							}
						}
					}

				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に白確定でないマスを自分の部屋限定でつなげる。
		 */
		protected void setContinuePosSet(Position pos, Set<Position> continuePosSet, Set<Position> roomMember,
				Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && roomMember.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, roomMember, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && roomMember.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, roomMember, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && roomMember.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, roomMember, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && roomMember.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, roomMember, Direction.RIGHT);
				}
			}
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
			if (!pondSolve()) {
				return false;
			}
			if (!roomConnectSolve()) {
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

		/**
		 * 黒マスを斜めにたどってもひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position blackPos = new Position(yIndex, xIndex);
						if (blackPosSet.size() == 0) {
							blackPosSet.add(blackPos);
							setContinueDiagonalPosSet(blackPos, blackPosSet, null);
						} else {
							if (!blackPosSet.contains(blackPos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右ななめに白確定でないマスを無制限につなげていく。
		 */
		protected void setContinueDiagonalPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
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

	public CocktailSolver(Field field) {
		this.field = new Field(field);
	}

	// penpa-edit向けコンストラクタ
	public CocktailSolver(String fieldStr) {
		field = new Field(fieldStr);
	}

	public CocktailSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String fieldStr = "square,10,10,42,0,1,1,462,462,286,286\n" + "[0,0,0,0]\n"
				+ "[\"1\",\"2\",\"1\"]~zS~[\"\",1]\n"
				+ "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{\"30\":[\"5\",1,\"1\"],\"46\":[\"5\",1,\"1\"],\"48\":[\"3\",1,\"1\"],\"51\":[\"3\",1,\"1\"],\"58\":[\"3\",1,\"1\"],\"88\":[\"3\",1,\"1\"],\"100\":[\"3\",1,\"1\"],\"104\":[\"3\",1,\"1\"],\"109\":[\"2\",1,\"1\"],\"128\":[\"3\",1,\"1\"],\"130\":[\"5\",1,\"1\"],\"133\":[\"3\",1,\"1\"],\"161\":[\"3\",1,\"1\"]},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{\"225,226\":2,\"226,227\":2,\"227,228\":2,\"228,229\":2,\"229,230\":2,\"230,231\":2,\"231,232\":2,\"232,233\":2,\"233,234\":2,\"220,234\":2,\"234,248\":2,\"248,262\":2,\"262,276\":2,\"276,290\":2,\"290,291\":2,\"290,304\":2,\"304,318\":2,\"318,332\":2,\"332,346\":2,\"346,347\":2,\"345,346\":2,\"344,345\":2,\"343,344\":2,\"342,343\":2,\"342,356\":2,\"356,357\":2,\"309,310\":2,\"310,311\":2,\"311,312\":2,\"312,313\":2,\"313,314\":2,\"314,328\":2,\"328,342\":2,\"311,325\":2,\"325,339\":2,\"339,353\":2,\"314,315\":2,\"315,316\":2,\"316,317\":2,\"317,318\":2,\"316,330\":2,\"330,344\":2,\"232,246\":2,\"246,260\":2,\"260,274\":2,\"274,288\":2,\"288,302\":2,\"302,316\":2,\"274,275\":2,\"275,276\":2,\"229,243\":2,\"243,257\":2,\"257,271\":2,\"271,285\":2,\"285,286\":2,\"286,287\":2,\"287,288\":2,\"285,299\":2,\"299,313\":2,\"227,241\":2,\"241,255\":2,\"255,269\":2,\"269,283\":2,\"283,297\":2,\"297,311\":2,\"239,240\":2,\"240,241\":2,\"282,283\":2,\"281,282\":2,\"270,271\":2,\"269,270\":2},zW:{},zC:{},z4:{}}\n"
				+ "\n"
				+ "[30,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1]";
		System.out.println(new CocktailSolver(fieldStr).solve());
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
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt, 0, 0)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive, int initY, int initX) {//
//		System.out.println(field);
		String str = field.getStateDump();
		for (int yIndex = initY; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = initX; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
			initX = 0;
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive, 0, 0);
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
			if (!candSolve(virtual, recursive - 1, yIndex, xIndex)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.masu[yIndex][xIndex] = Masu.NOT_BLACK;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1, yIndex, xIndex)) {
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
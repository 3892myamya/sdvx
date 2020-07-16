package myamya.other.solver.moonsun;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.RoomMaker;
import myamya.other.solver.Solver;

public class MoonsunSolver implements Solver {
	public static class MoonsunGenerator implements Generator {

		static class ExtendedField extends MoonsunSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width, List<Set<Position>> rooms) {
				super(height, width, rooms);
			}

			/**
			 * 作問中は月部屋・太陽部屋の意識は不要。
			 * ただし、部屋の数が奇数は即OUT
			 */
			@Override
			public boolean roomSolve() {
				if (rooms.size() != 1 && rooms.size() % 2 == 1) {
					return false;
				}
				for (int i = 0; i < rooms.size(); i++) {
					Room room = rooms.get(i);
					// 部屋に対する調査
					int whiteCnt = 0;
					int spaceCnt = 0;
					for (Position pos : room.getMember()) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
							whiteCnt++;
						} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							spaceCnt++;
						}
					}
					// 最低1マスは必要なので
					if (whiteCnt + spaceCnt < 1) {
						// 白マス不足
						return false;
					}
					if (whiteCnt == 0 && spaceCnt == 1) {
						for (Position pos : room.getMember()) {
							if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
							}
						}
					}
				}
				return true;
			}
		}

		static class MoonsunSolverForGenerator extends MoonsunSolver {
			private final int limit;

			public MoonsunSolverForGenerator(Field field, int limit) {
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

		public MoonsunGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new MoonsunGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			MoonsunSolver.Field wkField = new ExtendedField(height, width,
					RoomMaker.roomMake(height, width, (int) (Math.sqrt(height) + 1), -1));
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < (height * (width - 1)) + ((height - 1) * width); i++) {
				indexList.add(i);
			}
			int level = 0;
			int index = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				while (!wkField.isSolved()) {
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
					if ((toYokoWall && wkField.yokoWall[yIndex][xIndex] == Wall.SPACE)
							|| (!toYokoWall && wkField.tateWall[yIndex][xIndex] == Wall.SPACE)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							MoonsunSolver.Field virtual = new ExtendedField(wkField);
							if (masuNum < 1) {
								if (toYokoWall) {
									virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
								} else {
									virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
							} else if (masuNum < 2) {
								if (toYokoWall) {
									virtual.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								} else {
									virtual.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.masu = virtual.masu;
								wkField.yokoWall = virtual.yokoWall;
								wkField.tateWall = virtual.tateWall;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new ExtendedField(height, width,
									RoomMaker.roomMake(height, width, (int) (Math.sqrt(height) + 1), -1));
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 月と太陽を配置する。
				// ランダムに1マス白マスを選ぶ。
				List<Position> whitePosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							whitePosList.add(new Position(yIndex, xIndex));
						}
					}
				}
				Position nowPos = whitePosList.get((int) (Math.random() * whitePosList.size()));
				List<Integer> roomConnectList = new ArrayList<>();
				List<Position> alreadyPosList = new ArrayList<>();
				while (true) {
					// 今いる部屋を加える。
					for (int i = 0; i < wkField.rooms.size(); i++) {
						Set<Position> room = wkField.rooms.get(i).getMember();
						if (room.contains(nowPos)) {
							if (!roomConnectList.contains(i)) {
								roomConnectList.add(i);
							}
							break;
						}
					}
					if (roomConnectList.size() == wkField.rooms.size()) {
						break;
					}
					alreadyPosList.add(nowPos);
					if (nowPos.getyIndex() != 0
							&& wkField.tateWall[nowPos.getyIndex() - 1][nowPos.getxIndex()] == Wall.NOT_EXISTS) {
						Position nextPos = new Position(nowPos.getyIndex() - 1, nowPos.getxIndex());
						if (!alreadyPosList.contains(nextPos)) {
							nowPos = nextPos;
							continue;
						}
					}
					if (nowPos.getxIndex() != wkField.getXLength() - 1
							&& wkField.yokoWall[nowPos.getyIndex()][nowPos.getxIndex()] == Wall.NOT_EXISTS) {
						Position nextPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() + 1);
						if (!alreadyPosList.contains(nextPos)) {
							nowPos = nextPos;
							continue;
						}
					}
					if (nowPos.getyIndex() != wkField.getYLength() - 1
							&& wkField.tateWall[nowPos.getyIndex()][nowPos.getxIndex()] == Wall.NOT_EXISTS) {
						Position nextPos = new Position(nowPos.getyIndex() + 1, nowPos.getxIndex());
						if (!alreadyPosList.contains(nextPos)) {
							nowPos = nextPos;
							continue;
						}
					}
					if (nowPos.getxIndex() != 0
							&& wkField.yokoWall[nowPos.getyIndex()][nowPos.getxIndex() - 1] == Wall.NOT_EXISTS) {
						Position nextPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() - 1);
						if (!alreadyPosList.contains(nextPos)) {
							nowPos = nextPos;
							continue;
						}
					}
				}
				// そこから部屋のつながりを判定し、順次太陽→月としていく。
				int adjust = (int) (Math.random() * 2);
				for (int i = 0; i < roomConnectList.size(); i++) {
					for (Position pos : wkField.getRooms().get(i).getMember()) {
						if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
							wkField.moonsuns[pos.getyIndex()][pos.getxIndex()] = (i + adjust) % 2 == 0 ? 1 : 2;
						} else if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
							wkField.moonsuns[pos.getyIndex()][pos.getxIndex()] = (i + adjust) % 2 == 0 ? 2 : 1;
						}

					}
				}
				// マスを戻す
				List<Position> fixedMasuList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
						fixedMasuList.add(new Position(yIndex, xIndex));
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				// 解けるかな？
				level = new MoonsunSolverForGenerator(wkField, 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width,
							RoomMaker.roomMake(height, width, (int) (Math.sqrt(height) + 1), -1));
					index = 0;
				} else {
					Collections.shuffle(fixedMasuList);
					for (Position pos : fixedMasuList) {
						MoonsunSolver.Field virtual = new MoonsunSolver.Field(wkField, true);
						virtual.moonsuns[pos.getyIndex()][pos.getxIndex()] = 0;
						int solveResult = new MoonsunSolverForGenerator(virtual, 1500).solve2();
						if (solveResult != -1 && solveResult >= level) {
							wkField.moonsuns[pos.getyIndex()][pos.getxIndex()] = 0;
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(部屋/記号：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin)
							+ "\" width=\""
							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1
							|| wkField.getYokoRoomWall()[yIndex][xIndex];
					sb.append("<line y1=\""
							+ (yIndex * baseSize + margin)
							+ "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\""
							+ (xIndex * baseSize + 2 * baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">"
							+ "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1
							|| wkField.getTateRoomWall()[yIndex][xIndex];
					sb.append("<line y1=\""
							+ (yIndex * baseSize + baseSize + margin)
							+ "\" x1=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\""
							+ (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">"
							+ "</line>");
				}
			}
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			System.out.println(url);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		protected Masu[][] masu;
		// 月と太陽の情報。1が太陽、2が月
		private final Integer[][] moonsuns;
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
		// 月部屋・太陽部屋の情報。0は不明、1は太陽部屋、2は月部屋
		private Map<Integer, Integer> roomType;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getHintCount() {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		public String getPuzPreURL() {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		public Integer[][] getMoonSuns() {
			return moonsuns;
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
			moonsuns = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
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
								/ 16
								% 2 == 1;
					}
					if (mod >= 1) {
						yokoRoomWall[(cnt - mod + 1) / (getXLength() - 1)][(cnt - mod + 1) % (getXLength() - 1)] = bit
								/ 8
								% 2 == 1;
					}
					if (mod >= 2) {
						yokoRoomWall[(cnt - mod + 2) / (getXLength() - 1)][(cnt - mod + 2) % (getXLength() - 1)] = bit
								/ 4
								% 2 == 1;
					}
					if (mod >= 3) {
						yokoRoomWall[(cnt - mod + 3) / (getXLength() - 1)][(cnt - mod + 3) % (getXLength() - 1)] = bit
								/ 2
								% 2 == 1;
					}
					if (mod >= 4) {
						yokoRoomWall[(cnt - mod + 4) / (getXLength() - 1)][(cnt - mod + 4) % (getXLength() - 1)] = bit
								/ 1
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
			// 縦と横の壁の関係からにょろっと部屋を決めていく
			rooms = new ArrayList<>();
			roomType = new HashMap<>();
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
						Set<Position> yokoWallPosSet = new HashSet<>();
						Set<Position> tateWallPosSet = new HashSet<>();
						for (Position roomPos : continuePosSet) {
							if (roomPos.getyIndex() != 0
									&& tateRoomWall[roomPos.getyIndex() - 1][roomPos.getxIndex()]) {
								tateWallPosSet.add(new Position(roomPos.getyIndex() - 1, roomPos.getxIndex()));
							}
							if (roomPos.getxIndex() != getXLength() - 1
									&& yokoRoomWall[roomPos.getyIndex()][roomPos.getxIndex()]) {
								yokoWallPosSet.add(new Position(roomPos.getyIndex(), roomPos.getxIndex()));
							}
							if (roomPos.getyIndex() != getYLength() - 1
									&& tateRoomWall[roomPos.getyIndex()][roomPos.getxIndex()]) {
								tateWallPosSet.add(new Position(roomPos.getyIndex(), roomPos.getxIndex()));
							}
							if (roomPos.getxIndex() != 0
									&& yokoRoomWall[roomPos.getyIndex()][roomPos.getxIndex() - 1]) {
								yokoWallPosSet.add(new Position(roomPos.getyIndex(), roomPos.getxIndex() - 1));
							}
						}
						rooms.add(new Room(continuePosSet, yokoWallPosSet,
								tateWallPosSet));
						roomType.put(rooms.size() - 1, 0);
					}
				}
			}
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int bitInfo = Character.getNumericValue(ch);
				int pos1 = bitInfo / 9 % 3;
				int pos2 = bitInfo / 3 % 3;
				int pos3 = bitInfo % 3;
				if (index / getXLength() < getYLength()) {
					moonsuns[index / getXLength()][index % getXLength()] = pos1;
				}
				index++;
				if (index / getXLength() < getYLength()) {
					moonsuns[index / getXLength()][index % getXLength()] = pos2;
				}
				index++;
				if (index / getXLength() < getYLength()) {
					moonsuns[index / getXLength()][index % getXLength()] = pos3;
				}
				index++;
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
			roomType = new HashMap<>(other.roomType);
			moonsuns = other.moonsuns;
			yokoRoomWall = other.yokoRoomWall;
			tateRoomWall = other.tateRoomWall;
			rooms = other.rooms;
		}

		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			moonsuns = new Integer[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					moonsuns[yIndex][xIndex] = other.moonsuns[yIndex][xIndex];
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
			roomType = new HashMap<>();
			for (Entry<Integer, Integer> e : other.roomType.entrySet()) {
				roomType.put(e.getKey(), e.getValue());
			}
			yokoRoomWall = other.yokoRoomWall;
			tateRoomWall = other.tateRoomWall;
			rooms = other.rooms;
		}

		public Field(int height, int width, List<Set<Position>> rooms) {
			masu = new Masu[height][width];
			moonsuns = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					moonsuns[yIndex][xIndex] = 0;
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
			yokoRoomWall = new boolean[height][width - 1];
			tateRoomWall = new boolean[height - 1][width];
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
					yokoRoomWall[yIndex][xIndex] = isWall;
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
					tateRoomWall[yIndex][xIndex] = isWall;
				}
			}
			this.rooms = new ArrayList<>();
			roomType = new HashMap<>();
			for (int i = 0; i < rooms.size(); i++) {
				Set<Position> room = rooms.get(i);
				Set<Position> yokoWallPosSet = new HashSet<>();
				Set<Position> tateWallPosSet = new HashSet<>();
				for (Position roomPos : room) {
					if (roomPos.getyIndex() != 0
							&& tateRoomWall[roomPos.getyIndex() - 1][roomPos.getxIndex()]) {
						tateWallPosSet.add(new Position(roomPos.getyIndex() - 1, roomPos.getxIndex()));
					}
					if (roomPos.getxIndex() != getXLength() - 1
							&& yokoRoomWall[roomPos.getyIndex()][roomPos.getxIndex()]) {
						yokoWallPosSet.add(new Position(roomPos.getyIndex(), roomPos.getxIndex()));
					}
					if (roomPos.getyIndex() != getYLength() - 1
							&& tateRoomWall[roomPos.getyIndex()][roomPos.getxIndex()]) {
						tateWallPosSet.add(new Position(roomPos.getyIndex(), roomPos.getxIndex()));
					}
					if (roomPos.getxIndex() != 0
							&& yokoRoomWall[roomPos.getyIndex()][roomPos.getxIndex() - 1]) {
						yokoWallPosSet.add(new Position(roomPos.getyIndex(), roomPos.getxIndex() - 1));
					}
				}
				this.rooms.add(new Room(room, yokoWallPosSet, tateWallPosSet));
				roomType.put(i, 0);
			}

		}

		// posを起点に上下左右に部屋壁または白確定でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateRoomWall[pos.getyIndex() - 1][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoRoomWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateRoomWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoRoomWall[pos.getyIndex()][pos.getxIndex() - 1]
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
					if (moonsuns[yIndex][xIndex] != 0) {
						sb.append(moonsuns[yIndex][xIndex] == 1 ? "☆" : "★");
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
					if (xIndex != getXLength() - 1) {
						if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
							sb.append(yokoRoomWall[yIndex][xIndex] == true ? "？" : "　");
						} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							sb.append(yokoRoomWall[yIndex][xIndex] == true ? "＋" : "・");
						} else if (yokoWall[yIndex][xIndex] == Wall.EXISTS) {
							sb.append(yokoRoomWall[yIndex][xIndex] == true ? "□" : "○");
						}
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (tateWall[yIndex][xIndex] == Wall.SPACE) {
							sb.append(tateRoomWall[yIndex][xIndex] == true ? "？" : "　");
						} else if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							sb.append(tateRoomWall[yIndex][xIndex] == true ? "＋" : "・");
						} else if (tateWall[yIndex][xIndex] == Wall.EXISTS) {
							sb.append(tateRoomWall[yIndex][xIndex] == true ? "□" : "○");
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
		 * 黒マスの周囲の壁を埋める。
		 * また、白マス隣接セルの周辺の壁の数が2にならない場合もfalseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						// 周囲の壁を閉鎖
						if (yIndex != 0) {
							if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (xIndex != getXLength() - 1) {
							if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (yIndex != getYLength() - 1) {
							if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (xIndex != 0) {
							if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					} else {
						int existsCount = 0;
						int notExistsCount = 0;
						Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
						if (wallUp == Wall.EXISTS) {
							existsCount++;
						} else if (wallUp == Wall.NOT_EXISTS) {
							if (masu[yIndex - 1][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							notExistsCount++;
						}
						Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
						if (wallRight == Wall.EXISTS) {
							existsCount++;
						} else if (wallRight == Wall.NOT_EXISTS) {
							if (masu[yIndex][xIndex + 1] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							notExistsCount++;
						}
						Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
						if (wallDown == Wall.EXISTS) {
							existsCount++;
						} else if (wallDown == Wall.NOT_EXISTS) {
							if (masu[yIndex + 1][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							notExistsCount++;
						}
						Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
						if (wallLeft == Wall.EXISTS) {
							existsCount++;
						} else if (wallLeft == Wall.NOT_EXISTS) {
							if (masu[yIndex][xIndex - 1] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							notExistsCount++;
						}
						// 自分が白マスなら壁は必ず2マス
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
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
									if (masu[yIndex - 1][xIndex] == Masu.BLACK) {
										return false;
									}
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
									masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
								}
								if (wallRight == Wall.SPACE) {
									if (masu[yIndex][xIndex + 1] == Masu.BLACK) {
										return false;
									}
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
									masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
								}
								if (wallDown == Wall.SPACE) {
									if (masu[yIndex + 1][xIndex] == Masu.BLACK) {
										return false;
									}
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
									masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
								}
								if (wallLeft == Wall.SPACE) {
									if (masu[yIndex][xIndex - 1] == Masu.BLACK) {
										return false;
									}
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
									masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
								}
							}
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							// 自分が不確定マスなら壁は2マスか4マス
							if ((existsCount == 3 && notExistsCount == 1)
									|| notExistsCount > 2) {
								return false;
							}
							if (existsCount > 2) {
								masu[yIndex][xIndex] = Masu.BLACK;
								if (existsCount == 3) {
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
								}
							} else if (notExistsCount != 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 国境をまたぐことが可能なマスは、1つの国につき必ず2マスになる。(国が1つの場合は除く)
		 * ルールに違反している場合falseを返す。
		 */
		public boolean countrySolve() {
			if (rooms.size() == 1) {
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
			Set<Position> allPosSet = new HashSet<>();
			Position typicalWhitePos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					allPosSet.add(pos);
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						if (typicalWhitePos == null) {
							typicalWhitePos = pos;
						}
					}
				}
			}
			if (typicalWhitePos == null) {
				return true;
			} else {
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				setContinuePosSet(typicalWhitePos, continuePosSet, null);
				allPosSet.removeAll(continuePosSet);
				for (Position pos : allPosSet) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
						return false;
					}
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
				}
				return true;
			}
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスをつなげていく。
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
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!countrySolve()) {
				return false;
			}
			if (!moonSunSolve()) {
				return false;
			}
			if (!oddSolve()) {
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
		 * 部屋には最低1マスの黒マスが入り、
		 * 月か太陽で白(黒)マスが1つでもあれば同じ部屋の同じ星は全て白(黒)マス。
		 */
		protected boolean roomSolve() {
			for (int i = 0; i < rooms.size(); i++) {
				Room room = rooms.get(i);
				boolean sunRoom = false;
				boolean moonRoom = false;
				boolean sunCand = false;
				boolean moonCand = false;
				// 部屋に対する調査
				int whiteCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room.getMember()) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
						whiteCnt++;
						if (moonsuns[pos.getyIndex()][pos.getxIndex()] == 1) {
							sunRoom = true;
							sunCand = true;
						} else if (moonsuns[pos.getyIndex()][pos.getxIndex()] == 2) {
							moonRoom = true;
							moonCand = true;
						}
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						spaceCnt++;
						if (moonsuns[pos.getyIndex()][pos.getxIndex()] == 1) {
							sunCand = true;
						} else if (moonsuns[pos.getyIndex()][pos.getxIndex()] == 2) {
							moonCand = true;
						}
					}
				}
				if (sunRoom && moonRoom) {
					return false;
				}
				if (!sunCand && !moonCand) {
					return false;
				}
				if (sunRoom || (sunCand && !moonCand)) {
					if (!toSunRoom(i)) {
						return false;
					}
				}
				if (moonRoom || (moonCand && !sunCand)) {
					if (!toMoonRoom(i)) {
						return false;
					}
				}
				// 最低1マスは必要なので
				if (whiteCnt + spaceCnt < 1) {
					// 白マス不足
					return false;
				}
				if (whiteCnt == 0 && spaceCnt == 1) {
					for (Position pos : room.getMember()) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * roomIdxの部屋を太陽部屋に確定する。月部屋にできなかった場合false。
		 */
		private boolean toSunRoom(int roomIdx) {
			if (roomType.get(roomIdx) == 2) {
				return false;
			}
			roomType.put(roomIdx, 1);
			Room room = rooms.get(roomIdx);
			for (Position pos : room.getMember()) {
				if (moonsuns[pos.getyIndex()][pos.getxIndex()] == 1) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						return false;
					}
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
				}
				if (moonsuns[pos.getyIndex()][pos.getxIndex()] == 2) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
						return false;
					}
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
				}
			}
			return true;
		}

		/**
		 * roomIdxの部屋を月部屋に確定する。月部屋にできなかった場合false。
		 */
		private boolean toMoonRoom(int roomIdx) {
			if (roomType.get(roomIdx) == 1) {
				return false;
			}
			roomType.put(roomIdx, 2);
			Room room = rooms.get(roomIdx);
			for (Position pos : room.getMember()) {
				if (moonsuns[pos.getyIndex()][pos.getxIndex()] == 1) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
						return false;
					}
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
				}
				if (moonsuns[pos.getyIndex()][pos.getxIndex()] == 2) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						return false;
					}
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
				}
			}
			return true;
		}

		/**
		 * 太陽(月)部屋から国境を通ってつながった部屋は月(太陽)部屋になる。
		 * 逆に言えば、太陽部屋と太陽部屋、月部屋と月部屋の間は必ず壁ができる。
		 * 矛盾する場合falseを返す。
		 */
		private boolean moonSunSolve() {
			for (int i = 0; i < rooms.size(); i++) {
				Room myRoom = rooms.get(i);
				// 解放国境の調査
				for (Position pos : myRoom.getYokoWallPosSet()) {
					if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS) {
						Position connectCountryPos;
						if (myRoom.getMember().contains(pos)) {
							connectCountryPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
						} else {
							connectCountryPos = pos;
						}
						for (int j = 0; j < rooms.size(); j++) {
							Room otherRoom = rooms.get(j);
							if (otherRoom.getMember().contains(connectCountryPos)) {
								int myRoomType = roomType.get(i);
								int otherRoomType = roomType.get(j);
								if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
									if (myRoomType == 0) {
										if (otherRoomType == 1) {

											if (!toMoonRoom(i)) {
												return false;
											}
										} else if (otherRoomType == 2) {
											if (!toSunRoom(i)) {
												return false;
											}
										}
									} else {
										if (myRoomType == 1) {
											if (!toMoonRoom(j)) {
												return false;
											}
										} else if (myRoomType == 2) {
											if (!toSunRoom(j)) {
												return false;
											}
										}
									}
								} else {
									if (myRoomType != 0 && otherRoomType != 0 && myRoomType == otherRoomType) {
										yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									}
								}
								break;
							}
						}
					}
				}
				for (Position pos : myRoom.getTateWallPosSet()) {
					if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS) {
						Position connectCountryPos;
						if (myRoom.getMember().contains(pos)) {
							connectCountryPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
						} else {
							connectCountryPos = pos;
						}
						for (int j = 0; j < rooms.size(); j++) {
							Room otherRoom = rooms.get(j);
							if (otherRoom.getMember().contains(connectCountryPos)) {
								int myRoomType = roomType.get(i);
								int otherRoomType = roomType.get(j);
								if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
									if (myRoomType == 0) {
										if (otherRoomType == 1) {
											if (!toMoonRoom(i)) {
												return false;
											}
										} else if (otherRoomType == 2) {
											if (!toSunRoom(i)) {
												return false;
											}
										}
									} else {
										if (myRoomType == 1) {
											if (!toMoonRoom(j)) {
												return false;
											}
										} else if (myRoomType == 2) {
											if (!toSunRoom(j)) {
												return false;
											}
										}
									}
								} else {
									if (myRoomType != 0 && otherRoomType != 0 && myRoomType == otherRoomType) {
										tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
									}
								}
								break;
							}
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

	public static class Room {
		@Override
		public String toString() {
			return "Room [member=" +
					member + "]";
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

	protected final Field field;
	protected int count = 0;

	public MoonsunSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public MoonsunSolver(Field field) {
		this.field = new Field(field);
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
		System.out.println(new MoonsunSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 3));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 3).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
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
			field.masu = virtual2.masu;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
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
			field.masu = virtual2.masu;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

}
package myamya.other.solver.linedozen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.PenpaEditLib;
import myamya.other.solver.PenpaEditLib.RoomWalls;
import myamya.other.solver.RoomMaker;
import myamya.other.solver.Solver;

public class LinedozenSolver implements Solver {
	public static class LinedozenGenerator implements Generator {

		static class LinedozenSolverForGenerator extends LinedozenSolver {
			private final int limit;

			public LinedozenSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
				try {
					while (!field.isSolved()) {
						String befStr = field.getStateDump();
						if (!field.solveAndCheck()) {
							return -2;
						}
						if (field.getStateDump().equals(befStr)) {
							if (!candSolve(field, 0)) {
								return -2;
							}
							if (field.getStateDump().equals(befStr)) {
								if (!candSolve(field, 1)) {
									return -2;
								}
								if (field.getStateDump().equals(befStr)) {
									if (!candSolve(field, 2)) {
										return -2;
									}
									if (field.getStateDump().equals(befStr)) {
										return -1;
									}
								}
							}
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

		public LinedozenGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new LinedozenGenerator(6, 6).generate();
		}

		@Override
		public GeneratorResult generate() {
			LinedozenSolver.Field wkField = new LinedozenSolver.Field(height, width,
					RoomMaker.roomMake(height, width, 2, 3));
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
				// 数字を配置
				Collections.shuffle(indexList);
				boolean isOk = false;
				for (Position pos : indexList) {
					isOk = false;
					List<Integer> numIdxList = new ArrayList<>();
					for (int number : wkField.numbersCand[pos.getyIndex()][pos.getxIndex()]) {
						// for (int number = 1; number < 9; number++) {
						// for (int i = 0; i < 100 - (number * number); i++) {
						numIdxList.add(number);
						// }
					}
					Collections.shuffle(numIdxList);
					Set<Integer> alreadyNumIdx = new HashSet<>();
					for (Iterator<Integer> iterator = numIdxList.iterator(); iterator.hasNext();) {
						int numIdx = iterator.next();
						if (alreadyNumIdx.contains(numIdx)) {
							iterator.remove();
						} else {
							alreadyNumIdx.add(numIdx);
						}
					}
					for (int masuNum : numIdxList) {
						Field virtual = new Field(wkField);
						virtual.numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
						virtual.numbersCand[pos.getyIndex()][pos.getxIndex()].add(masuNum);
						if (-2 != new LinedozenSolverForGenerator(virtual, 5).solve2()) {
							isOk = true;
							wkField.numbersCand[pos.getyIndex()][pos
									.getxIndex()] = virtual.numbersCand[pos.getyIndex()][pos.getxIndex()];
							break;
						}
					}
					if (!isOk) {
						break;
					}
				}
				if (!isOk) {
					// 破綻したら0から作り直す。
					wkField = new Field(height, width, RoomMaker.roomMake(height, width, 2, 3));
					continue;
				}
				wkField.initLine();
				System.out.println(wkField);
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.numbersCand[yIndex][xIndex] = new ArrayList<>();
						for (int number = 1; number <= 9; number++) {
							wkField.numbersCand[yIndex][xIndex].add(number);
						}
					}
				}
				// 解けるかな？
				level = new LinedozenSolverForGenerator(wkField, 500).solve2();
				if (level < 0) {
					// 解けなければやり直し
					wkField = new LinedozenSolver.Field(height, width, RoomMaker.roomMake(height, width, 2, 3));
				} else {
					List<Set<Position>> linesCopy = new ArrayList<>();
					for (Set<Position> oneLine : wkField.lines) {
						linesCopy.add(new HashSet<>(oneLine));
					}
					Collections.shuffle(linesCopy);
					for (Set<Position> oneLinesCopy : linesCopy) {
						LinedozenSolver.Field virtual = new LinedozenSolver.Field(wkField);
						for (Iterator<Set<Position>> iterator = virtual.lines.iterator(); iterator.hasNext();) {
							Set<Position> line = (Set<Position>) iterator.next();
							oneLinesCopy.removeAll(line);
							if (oneLinesCopy.isEmpty()) {
								iterator.remove();
								break;
							}
						}
						int solveResult = new LinedozenSolverForGenerator(virtual, 1000).solve2();
						if (solveResult >= 0) {
							System.out.println(level);
							wkField.lines = virtual.lines;
							level = solveResult;
						}
					}
					break;
				}
			}
			System.out.println(level);
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(部屋/数字：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
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
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<Set<Position>> rooms;
		// 同一ラインに属するマスの情報
		private List<Set<Position>> lines;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public String getPuzPreURL() {
			return PenpaEditLib.PENPA_EDIT_DUMMY_URL;
		}

		public String getHintCount() {
			return rooms.size() + "/" + lines.size();
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public List<Set<Position>> getLines() {
			return lines;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(String fieldStr) {
			String[] fieldInfo = fieldStr.split("\n")[0].split(",");
			Integer yLength = Integer.valueOf(fieldInfo[2]);
			Integer xLength = Integer.valueOf(fieldInfo[1]);
			numbersCand = new ArrayList[yLength][xLength];
			// 初期候補数字を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int number = 1; number <= 9; number++) {
						numbersCand[yIndex][xIndex].add(number);
					}
				}
			}
			RoomWalls walls = PenpaEditLib.getRoomWalls(fieldStr);
			yokoWall = walls.getYokoRoomWall();
			tateWall = walls.getTateRoomWall();
			rooms = new ArrayList<>();
			// 縦と横の壁の関係からにょろっと部屋を決めていく
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean alreadyRoomed = false;
					for (Set<Position> room : rooms) {
						if (room.contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinuePosSet(pos, continuePosSet);
						rooms.add(continuePosSet);
					}
				}
			}
			lines = PenpaEditLib.getLines(fieldStr);
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

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
			lines = new ArrayList<>(other.lines);
		}

		/**
		 * プレーンなフィールド生成。ジェネレータ用
		 */
		@SuppressWarnings("unchecked")
		public Field(int height, int width, List<Set<Position>> rooms) {
			numbersCand = new ArrayList[height][width];
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			this.rooms = rooms;
			// 壁セット(表示のため)
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean isWall = true;
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
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean isWall = true;
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
			this.lines = new ArrayList<>();
			// 初期候補数字を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int number = 1; number <= 9; number++) {
						numbersCand[yIndex][xIndex].add(number);
					}
				}
			}
		}

		/**
		 * ジェネレータ用。合計が12になる線をランダムで引いていく。
		 */
		public void initLine() {
			List<Position> indexList = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					indexList.add(new Position(yIndex, xIndex));
				}
			}
			Collections.shuffle(indexList);
			for (Position pos : indexList) {
				boolean already = false;
				for (Set<Position> line : lines) {
					if (line.contains(pos)) {
						already = true;
						break;
					}
				}
				if (already) {
					continue;
				}
				int sum = numbersCand[pos.getyIndex()][pos.getxIndex()].get(0);
				Set<Position> lineCand = new HashSet<>();
				lineCand.add(pos);
				while (true) {
					List<Position> strongStretchPosList = new ArrayList<>();
					List<Position> stretchPosList = new ArrayList<>();
					for (Position lineCandPos : lineCand) {
						if (lineCandPos.getyIndex() != 0) {
							Position nextPos = new Position(lineCandPos.getyIndex() - 1, lineCandPos.getxIndex());
							if (!lineCand.contains(nextPos) && !stretchPosList.contains(nextPos)) {
								boolean innerAlready = false;
								for (Set<Position> line : lines) {
									if (line.contains(nextPos)) {
										innerAlready = true;
										break;
									}
								}
								if (!innerAlready) {
									if (tateWall[lineCandPos.getyIndex() - 1][lineCandPos.getxIndex()]) {
										strongStretchPosList.add(nextPos);
									} else {
										stretchPosList.add(nextPos);
									}
								}
							}
						}
						if (lineCandPos.getxIndex() != getXLength() - 1) {
							Position nextPos = new Position(lineCandPos.getyIndex(), lineCandPos.getxIndex() + 1);
							if (!lineCand.contains(nextPos) && !stretchPosList.contains(nextPos)) {
								boolean innerAlready = false;
								for (Set<Position> line : lines) {
									if (line.contains(nextPos)) {
										innerAlready = true;
										break;
									}
								}
								if (!innerAlready) {
									if (yokoWall[lineCandPos.getyIndex()][lineCandPos.getxIndex()]) {
										strongStretchPosList.add(nextPos);
									} else {
										stretchPosList.add(nextPos);
									}
								}
							}
						}
						if (lineCandPos.getyIndex() != getYLength() - 1) {
							Position nextPos = new Position(lineCandPos.getyIndex() + 1, lineCandPos.getxIndex());
							if (!lineCand.contains(nextPos) && !stretchPosList.contains(nextPos)) {
								boolean innerAlready = false;
								for (Set<Position> line : lines) {
									if (line.contains(nextPos)) {
										innerAlready = true;
										break;
									}
								}
								if (!innerAlready) {
									if (tateWall[lineCandPos.getyIndex()][lineCandPos.getxIndex()]) {
										strongStretchPosList.add(nextPos);
									} else {
										stretchPosList.add(nextPos);
									}
								}
							}
						}
						if (lineCandPos.getxIndex() != 0) {
							Position nextPos = new Position(lineCandPos.getyIndex(), lineCandPos.getxIndex() - 1);
							if (!lineCand.contains(nextPos) && !stretchPosList.contains(nextPos)) {
								boolean innerAlready = false;
								for (Set<Position> line : lines) {
									if (line.contains(nextPos)) {
										innerAlready = true;
										break;
									}
								}
								if (!innerAlready) {
									if (yokoWall[lineCandPos.getyIndex()][lineCandPos.getxIndex() - 1]) {
										strongStretchPosList.add(nextPos);
									} else {
										stretchPosList.add(nextPos);
									}
								}
							}
						}
					}
					Collections.shuffle(strongStretchPosList);
					Collections.shuffle(stretchPosList);
					strongStretchPosList.addAll(stretchPosList);
					boolean isOver = true;
					for (Position stretchPos : strongStretchPosList) {
						int stretchNum = numbersCand[stretchPos.getyIndex()][stretchPos.getxIndex()].get(0);
						if (sum + stretchNum <= 12) {
							sum = sum + stretchNum;
							lineCand.add(stretchPos);
							isOver = false;
							break;
						}
					}
					if (isOver) {
						break;
					}
					if (sum == 12) {
						lines.add(lineCand);
						break;
					}
				}

			}
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("lines:" + lines);
			sb.append(System.lineSeparator());
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
						int index = HALF_NUMS.indexOf(numStr);
						if (index >= 0) {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(numStr);
						}
					} else if (numbersCand[yIndex][xIndex].size() == 2) {
						sb.append(numbersCand[yIndex][xIndex].get(0));
						sb.append(numbersCand[yIndex][xIndex].get(1));
					} else {
						sb.append("　");
					}
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
					sb.append(numbersCand[yIndex][xIndex].size());
				}
			}
			return sb.toString();
		}

		/**
		 * 部屋の数字はすべて同じになる。
		 */
		public boolean roomSolve() {
			for (Set<Position> room : rooms) {
				int candNum = 0;
				for (Position pos : room) {
					if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 1) {
						candNum = numbersCand[pos.getyIndex()][pos.getxIndex()].get(0);
						break;
					}
				}
				if (candNum != 0) {
					// その数字以外の候補を消す
					for (Position pos : room) {
						for (Iterator<Integer> iterator = numbersCand[pos.getyIndex()][pos.getxIndex()]
								.iterator(); iterator.hasNext();) {
							int oneCand = iterator.next();
							if (oneCand != candNum) {
								iterator.remove();
							}
						}
						if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 0) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 壁を挟んで同じ数字は隣り合わない。
		 */
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					boolean wallUp = yIndex == 0 ? false : tateWall[yIndex - 1][xIndex];
					boolean wallRight = xIndex == getXLength() - 1 ? false : yokoWall[yIndex][xIndex];
					boolean wallDown = yIndex == getYLength() - 1 ? false : tateWall[yIndex][xIndex];
					boolean wallLeft = xIndex == 0 ? false : yokoWall[yIndex][xIndex - 1];
					if (wallUp) {
						if (numbersCand[yIndex][xIndex].size() == 1) {
							numbersCand[yIndex - 1][xIndex].remove(numbersCand[yIndex][xIndex].get(0));
							if (numbersCand[yIndex - 1][xIndex].size() == 0) {
								return false;
							}
						}
						if (numbersCand[yIndex - 1][xIndex].size() == 1) {
							numbersCand[yIndex][xIndex].remove(numbersCand[yIndex - 1][xIndex].get(0));
							if (numbersCand[yIndex][xIndex].size() == 0) {
								return false;
							}
						}
					}
					if (wallRight) {
						if (numbersCand[yIndex][xIndex].size() == 1) {
							numbersCand[yIndex][xIndex + 1].remove(numbersCand[yIndex][xIndex].get(0));
							if (numbersCand[yIndex][xIndex + 1].size() == 0) {
								return false;
							}
						}
						if (numbersCand[yIndex][xIndex + 1].size() == 1) {
							numbersCand[yIndex][xIndex].remove(numbersCand[yIndex][xIndex + 1].get(0));
							if (numbersCand[yIndex][xIndex].size() == 0) {
								return false;
							}
						}
					}
					if (wallDown) {
						if (numbersCand[yIndex][xIndex].size() == 1) {
							numbersCand[yIndex + 1][xIndex].remove(numbersCand[yIndex][xIndex].get(0));
							if (numbersCand[yIndex + 1][xIndex].size() == 0) {
								return false;
							}
						}
						if (numbersCand[yIndex + 1][xIndex].size() == 1) {
							numbersCand[yIndex][xIndex].remove(numbersCand[yIndex + 1][xIndex].get(0));
							if (numbersCand[yIndex][xIndex].size() == 0) {
								return false;
							}
						}
					}
					if (wallLeft) {
						if (numbersCand[yIndex][xIndex].size() == 1) {
							numbersCand[yIndex][xIndex - 1].remove(numbersCand[yIndex][xIndex].get(0));
							if (numbersCand[yIndex][xIndex - 1].size() == 0) {
								return false;
							}
						}
						if (numbersCand[yIndex][xIndex - 1].size() == 1) {
							numbersCand[yIndex][xIndex].remove(numbersCand[yIndex][xIndex - 1].get(0));
							if (numbersCand[yIndex][xIndex].size() == 0) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 線の上の数字を全部足すと12になる。12になる組み合わせがない場合false。
		 */
		private boolean dozenSolve() {
			for (Set<Position> line : lines) {
				List<List<Integer>> numbersCandList = new ArrayList<>();
				for (Position linePos : line) {
					numbersCandList.add(numbersCand[linePos.getyIndex()][linePos.getxIndex()]);
				}
				if (!canDozen(numbersCandList, 0, 0)) {
					return false;
				}
			}
			return true;
		}

		/**
		 * numbersCandListを足して12になる組み合わせを探す。 見つかった時点でtrueを返す。
		 */
		private boolean canDozen(List<List<Integer>> numbersCandList, int idx, int sum) {
			if (idx == numbersCandList.size()) {
				return sum == 12;
			}
			for (int oneNum : numbersCandList.get(idx)) {
				if (canDozen(numbersCandList, idx + 1, sum + oneNum)) {
					return true;
				}
			}
			return false;
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
			if (!dozenSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() != 1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public LinedozenSolver(Field field) {
		this.field = new Field(field);
	}

	// penpa-edit向けコンストラクタ
	public LinedozenSolver(String fieldStr) {
		field = new Field(fieldStr);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String fieldStr = "square,6,6,38,0,1,1,266,266,144,144\n" + "[0,0,0,0]\n" + "[\"1\",\"2\",\"1\"]~zS~[\"\",1]\n"
				+ "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{\"67,77\":5,\"76,77\":5,\"57,67\":5,\"44,54\":5,\"43,44\":5,\"53,54\":5,\"52,53\":5,\"27,37\":5,\"37,47\":5,\"23,33\":5,\"22,23\":5,\"23,24\":5,\"45,55\":5,\"55,56\":5,\"35,45\":5,\"74,75\":5,\"73,74\":5,\"72,73\":5,\"64,65\":5,\"63,64\":5,\"36,46\":5},zE:{\"113,123\":2,\"122,123\":2,\"121,122\":2,\"122,132\":2,\"132,142\":2,\"142,152\":2,\"151,152\":2,\"142,143\":2,\"143,153\":2,\"153,163\":2,\"162,163\":2,\"162,172\":2,\"163,164\":2,\"164,174\":2,\"164,165\":2,\"165,166\":2,\"166,167\":2,\"155,165\":2,\"145,155\":2,\"144,145\":2,\"132,133\":2,\"133,134\":2,\"134,135\":2,\"125,135\":2,\"115,125\":2,\"143,144\":2,\"135,145\":2,\"145,146\":2,\"136,146\":2,\"126,136\":2,\"126,127\":2,\"146,156\":2,\"156,157\":2},zW:{},zC:{},z4:{}}\n"
				+ "\n" + "[22,1,1,1,1,1,5,1,1,1,1,1,5,1,1,1,1,1,5,1,1,1,1,1,5,1,1,1,1,1,5,1,1,1,1,1]";
		System.out.println(new LinedozenSolver(fieldStr).solve());
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
			if (field.getStateDump().equals(befStr)) {
				if (!candSolve(field, 0)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				if (field.getStateDump().equals(befStr)) {
					if (!candSolve(field, 1)) {
						System.out.println(field);
						return "問題に矛盾がある可能性があります。途中経過を返します。";
					}
					if (field.getStateDump().equals(befStr)) {
						if (!candSolve(field, 2)) {
							System.out.println(field);
							return "問題に矛盾がある可能性があります。途中経過を返します。";
						}
						if (field.getStateDump().equals(befStr)) {
							System.out.println(field);
							return "解けませんでした。途中経過を返します。";
						}
					}
				}
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
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		while (true) {
			String befStr = field.getStateDump();
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.numbersCand[yIndex][xIndex].size() != 1) {
						for (Iterator<Integer> iterator = field.numbersCand[yIndex][xIndex].iterator(); iterator
								.hasNext();) {
							count++;
							int oneCand = iterator.next();
							Field virtual = new Field(field);
							virtual.numbersCand[yIndex][xIndex].clear();
							virtual.numbersCand[yIndex][xIndex].add(oneCand);
							boolean arrowCand = virtual.solveAndCheck();
							if (arrowCand && recursive > 0) {
								arrowCand = candSolve(virtual, recursive - 1);
							}
							if (!arrowCand) {
								iterator.remove();
							}
						}
						if (field.numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
					if (field.numbersCand[yIndex][xIndex].size() == 1) {
						if (!field.solveAndCheck()) {
							return false;
						}
					}
				}
			}
			if (field.getStateDump().equals(befStr)) {
				break;
			}
		}
		return true;
	}
}
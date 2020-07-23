package myamya.other.solver.renban;

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
import myamya.other.solver.RoomMaker;
import myamya.other.solver.Solver;

public class RenbanSolver implements Solver {
	public static class RenbanGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class RenbanSolverForGenerator extends RenbanSolver {
			private final int limit;

			public RenbanSolverForGenerator(Field field, int limit) {
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

		public RenbanGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new RenbanGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			RenbanSolver.Field wkField = new RenbanSolver.Field(height, width);
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				// 数字を配置
				for (int yIndex = 0; yIndex < height; yIndex++) {
					for (int xIndex = 0; xIndex < width; xIndex++) {
						if (wkField.numbersCand[yIndex][xIndex].size() != 1) {
							boolean isOk = false;
							List<Integer> numIdxList = new ArrayList<>();
							for (int number : wkField.numbersCand[yIndex][xIndex]) {
								numIdxList.add(number);
							}
							Collections.shuffle(numIdxList);
							for (int masuNum : numIdxList) {
								Field virtual = new Field(wkField);
								virtual.numbersCand[yIndex][xIndex].clear();
								virtual.numbersCand[yIndex][xIndex].add(masuNum);
								if (virtual.solveAndCheck()) {
									isOk = true;
									wkField.numbersCand = virtual.numbersCand;
									break;
								}
							}
							if (!isOk) {
								// 破綻したら0から作り直す。
								wkField = new Field(height, width);
							}
							yIndex = 0;
							xIndex = -1;
						}
					}
				}
				List<Position> fixedMasuList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.numbers[yIndex][xIndex] = wkField.numbersCand[yIndex][xIndex].get(0);
						fixedMasuList.add(new Position(yIndex, xIndex));
					}
				}
				// 解けるかな？
				level = new RenbanSolverForGenerator(wkField, 100).solve2();
				if (level < 0) {
					// 解けなければやり直し
					wkField = new RenbanSolver.Field(height, width);
				} else {
					Collections.shuffle(fixedMasuList);
					for (Position pos : fixedMasuList) {
						RenbanSolver.Field virtual = new RenbanSolver.Field(wkField, true);
						virtual.numbers[pos.getyIndex()][pos.getxIndex()] = null;
						virtual.numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						virtual.numbersCand[pos.getyIndex()][pos.getxIndex()].add(-1);
						int solveResult = new RenbanSolverForGenerator(virtual, 1000).solve2();
						if (solveResult >= 0) {
							wkField.numbers[pos.getyIndex()][pos.getxIndex()] = null;
							wkField.numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
							wkField.numbersCand[pos.getyIndex()][pos.getxIndex()].add(-1);
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 100 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(部屋/数字：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(wkField.getNumbersCand()[yIndex][xIndex].get(0));
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + 2)
								+ "\" font-size=\""
								+ (baseSize - 5)
								+ "\" textLength=\""
								+ (baseSize - 5)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr
								+ "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1
							|| wkField.getYokoWall()[yIndex][xIndex];
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
							|| wkField.getTateWall()[yIndex][xIndex];
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
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";
		// 固定数字(表示用)
		private Integer[][] numbers;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private List<Set<Position>> rooms;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?renban/" + getXLength() + "/" + getYLength() + "/");
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
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (numbers[yIndex][xIndex] == null) {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = numbers[yIndex][xIndex];
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

		public String getHintCount() {
			int numCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						numCnt++;
					}
				}
			}
			return rooms.size() + "/" + numCnt;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
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
			rooms = new ArrayList<>();
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
			// 初期候補数字はとりあえず-1
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					numbersCand[yIndex][xIndex].add(-1);
				}
			}
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int num;
					if (ch == '.') {
						//
					} else {
						if (ch == '-') {
							num = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							num = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							num = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(num);
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
					}
					index++;
				}
			}

		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
		}

		/**
		 * プレーンなフィールド生成。ジェネレータ用
		 */
		@SuppressWarnings("unchecked")
		public Field(int height, int width) {
			while (true) {
				numbers = new Integer[height][width];
				numbersCand = new ArrayList[height][width];
				yokoWall = new boolean[height][width - 1];
				tateWall = new boolean[height - 1][width];
				this.rooms = RoomMaker.roomMake(height, width, -1, (int) (height * 1.5));
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
				// ランダムに壁追加
				for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
						boolean wall1 = tateWall[yIndex][xIndex];
						boolean wall2 = tateWall[yIndex][xIndex + 1];
						boolean wall3 = yokoWall[yIndex][xIndex];
						boolean wall4 = yokoWall[yIndex + 1][xIndex];
						if (!wall1 && !wall2 && !wall3 && !wall4 && Math.random() < 0.7) {
							int baseNum = (int) (Math.random() * 4);
							if (baseNum == 0) {
								tateWall[yIndex][xIndex] = true;
							}
							if (baseNum == 1) {
								tateWall[yIndex][xIndex + 1] = true;
							}
							if (baseNum == 2) {
								yokoWall[yIndex][xIndex] = true;
							}
							if (baseNum == 3) {
								yokoWall[yIndex + 1][xIndex] = true;
							}
						}
					}
				}
				// 初期候補数字はとりあえず-1
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						numbersCand[yIndex][xIndex] = new ArrayList<>();
						numbersCand[yIndex][xIndex].add(-1);
					}
				}
				// ランダムに1箇所数字を決める。
				Position targetPos = new Position((int) (getYLength() * Math.random()),
						(int) (getXLength() * Math.random()));
				numbersCand[targetPos.getyIndex()][targetPos.getxIndex()].clear();
				for (Set<Position> room : rooms) {
					if (room.contains(targetPos)) {
						numbersCand[targetPos.getyIndex()][targetPos.getxIndex()]
								.add((int) (Math.random() * room.size() + 1));
						break;
					}
				}
				if (solveAndCheck()) {
					break;
				}
			}
		}

		/**
		 * イミュータブル。ジェネれーたよう
		 */
		@SuppressWarnings("unchecked")
		public Field(Field other, boolean flag) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
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

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

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
					sb.append(numbersCand[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 部屋の数字の確定状況から同じ部屋の他のマスの候補を決める。
		 */
		public boolean roomSolve() {

			for (Set<Position> room : rooms) {
				// 部屋ごとに確定数字の最大・最小を取得。
				int fixMin = Integer.MAX_VALUE;
				int fixMax = 0;
				for (Position pos : room) {
					if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 1
							&& numbersCand[pos.getyIndex()][pos.getxIndex()].get(0) != -1) {
						if (numbersCand[pos.getyIndex()][pos.getxIndex()].get(0) < fixMin) {
							fixMin = numbersCand[pos.getyIndex()][pos.getxIndex()].get(0);
						}
						if (numbersCand[pos.getyIndex()][pos.getxIndex()].get(0) > fixMax) {
							fixMax = numbersCand[pos.getyIndex()][pos.getxIndex()].get(0);
						}
					}
				}
				// それ以外の同部屋のマスは、既に確定している数字以外で、
				// 最大値 - 部屋サイズ + 1 ～ 最小値 + 部屋サイズ - 1 のどれかになる。
				if (fixMax != 0) {
					int candMin = fixMax - room.size() + 1;
					if (candMin <= 0) {
						candMin = 1;
					}
					int candMax = fixMin + room.size() - 1;
					Set<Integer> candNumSet = new HashSet<>();
					for (int i = candMin; i <= candMax; i++) {
						candNumSet.add(i);
					}
					for (Position pos : room) {
						if (numbersCand[pos.getyIndex()][pos.getxIndex()].get(0) == -1) {
							numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
							numbersCand[pos.getyIndex()][pos.getxIndex()].addAll(candNumSet);
						} else {
							numbersCand[pos.getyIndex()][pos.getxIndex()].retainAll(candNumSet);
						}
						if (numbersCand[pos.getyIndex()][pos.getxIndex()].isEmpty()) {
							return false;
						}
					}
				}
			}
			// 同じ数字の候補除外
			for (Set<Position> room : rooms) {
				for (Position pos : room) {
					if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 1
							&& numbersCand[pos.getyIndex()][pos.getxIndex()].get(0) != -1) {
						for (Position sameRoomPos : room) {
							if (!sameRoomPos.equals(pos)) {
								numbersCand[sameRoomPos.getyIndex()][sameRoomPos.getxIndex()]
										.remove(numbersCand[pos.getyIndex()][pos.getxIndex()].get(0));
							}
							if (numbersCand[sameRoomPos.getyIndex()][sameRoomPos.getxIndex()].size() == 0) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 周辺数字の条件を満たさない数字を候補から除外する。
		 */
		private boolean lineSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].get(0) != -1) {
						int upLength = getLineLength(false, yIndex - 1, xIndex);
						if (upLength != 0) {
							Set<Integer> candNumSet = new HashSet<>();
							for (Integer num : numbersCand[yIndex][xIndex]) {
								candNumSet.add(num + upLength);
								if (num - upLength > 0) {
									candNumSet.add(num - upLength);
								}
							}
							if (numbersCand[yIndex - 1][xIndex].get(0) == -1) {
								numbersCand[yIndex - 1][xIndex].clear();
								numbersCand[yIndex - 1][xIndex].addAll(candNumSet);
							} else {
								numbersCand[yIndex - 1][xIndex].retainAll(candNumSet);
							}
							if (numbersCand[yIndex - 1][xIndex].isEmpty()) {
								return false;
							}
						}
						int rightLength = getLineLength(true, yIndex, xIndex);
						if (rightLength != 0) {
							Set<Integer> candNumSet = new HashSet<>();
							for (Integer num : numbersCand[yIndex][xIndex]) {
								candNumSet.add(num + rightLength);
								if (num - rightLength > 0) {
									candNumSet.add(num - rightLength);
								}
							}
							if (numbersCand[yIndex][xIndex + 1].get(0) == -1) {
								numbersCand[yIndex][xIndex + 1].clear();
								numbersCand[yIndex][xIndex + 1].addAll(candNumSet);
							} else {
								numbersCand[yIndex][xIndex + 1].retainAll(candNumSet);
							}
							if (numbersCand[yIndex][xIndex + 1].isEmpty()) {
								return false;
							}
						}
						int downLength = getLineLength(false, yIndex, xIndex);
						if (downLength != 0) {
							Set<Integer> candNumSet = new HashSet<>();
							for (Integer num : numbersCand[yIndex][xIndex]) {
								candNumSet.add(num + downLength);
								if (num - downLength > 0) {
									candNumSet.add(num - downLength);
								}
							}
							if (numbersCand[yIndex + 1][xIndex].get(0) == -1) {
								numbersCand[yIndex + 1][xIndex].clear();
								numbersCand[yIndex + 1][xIndex].addAll(candNumSet);
							} else {
								numbersCand[yIndex + 1][xIndex].retainAll(candNumSet);
							}
							if (numbersCand[yIndex + 1][xIndex].isEmpty()) {
								return false;
							}
						}
						int leftLength = getLineLength(true, yIndex, xIndex - 1);
						if (leftLength != 0) {
							Set<Integer> candNumSet = new HashSet<>();
							for (Integer num : numbersCand[yIndex][xIndex]) {
								candNumSet.add(num + leftLength);
								if (num - leftLength > 0) {
									candNumSet.add(num - leftLength);
								}
							}
							if (numbersCand[yIndex][xIndex - 1].get(0) == -1) {
								numbersCand[yIndex][xIndex - 1].clear();
								numbersCand[yIndex][xIndex - 1].addAll(candNumSet);
							} else {
								numbersCand[yIndex][xIndex - 1].retainAll(candNumSet);
							}
							if (numbersCand[yIndex][xIndex - 1].isEmpty()) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 線の長さを取得
		 */
		private int getLineLength(boolean isYokoWall, int yIndex, int xIndex) {
			if (isYokoWall) {
				if (xIndex < 0 || xIndex >= getXLength() - 1 || !yokoWall[yIndex][xIndex]) {
					return 0;
				} else {
					int cnt = 1;
					for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
						if (yokoWall[targetY][xIndex]) {
							cnt++;
						} else {
							break;
						}
					}
					for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
						if (yokoWall[targetY][xIndex]) {
							cnt++;
						} else {
							break;
						}
					}
					return cnt;
				}
			} else {
				if (yIndex < 0 || yIndex >= getYLength() - 1 || !tateWall[yIndex][xIndex]) {
					return 0;
				} else {
					int cnt = 1;
					for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
						if (tateWall[yIndex][targetX]) {
							cnt++;
						} else {
							break;
						}
					}
					for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
						if (tateWall[yIndex][targetX]) {
							cnt++;
						} else {
							break;
						}
					}
					return cnt;
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
			if (!lineSolve()) {
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
					if (numbersCand[yIndex][xIndex].size() != 1 || numbersCand[yIndex][xIndex].get(0) == -1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public RenbanSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public RenbanSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?renban/4/4/1ng8o015..-10i.l./"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new RenbanSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 100));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 100 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 100).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
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
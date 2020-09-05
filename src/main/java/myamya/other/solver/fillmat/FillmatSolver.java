package myamya.other.solver.fillmat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class FillmatSolver implements Solver {
	public static class FillmatGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class FillmatSolverForGenerator extends FillmatSolver {
			private final int limit;

			public FillmatSolverForGenerator(Field field, int limit) {
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
						if (field.getStateDump().equals(befStr)) {
							if (!candSolve(field, 0)) {
								return -1;
							}
							if (field.getStateDump().equals(befStr)) {
								if (!candSolve(field, 1)) {
									return -1;
								}
								if (field.getStateDump().equals(befStr)) {
									if (!candSolve(field, 2)) {
										return -1;
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

		public FillmatGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new FillmatGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			Field wkField = new Field(height, width);
			List<Sikaku> candSikakuList = new ArrayList<>();
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					// 1*1
					candSikakuList.add(new Sikaku(new Position(yIndex, xIndex), new Position(yIndex, xIndex)));
					if (yIndex < wkField.getYLength() - 1) {
						// 2*1
						candSikakuList.add(new Sikaku(new Position(yIndex, xIndex), new Position(yIndex + 1, xIndex)));
					}
					if (yIndex < wkField.getYLength() - 2) {
						// 3*1
						candSikakuList.add(new Sikaku(new Position(yIndex, xIndex), new Position(yIndex + 2, xIndex)));
					}
					if (yIndex < wkField.getYLength() - 3) {
						// 4*1
						candSikakuList.add(new Sikaku(new Position(yIndex, xIndex), new Position(yIndex + 3, xIndex)));
					}
					if (xIndex < wkField.getXLength() - 1) {
						// 1*2
						candSikakuList.add(new Sikaku(new Position(yIndex, xIndex), new Position(yIndex, xIndex + 1)));
					}
					if (xIndex < wkField.getXLength() - 2) {
						// 1*3
						candSikakuList.add(new Sikaku(new Position(yIndex, xIndex), new Position(yIndex, xIndex + 2)));
					}
					if (xIndex < wkField.getXLength() - 3) {
						// 1*4
						candSikakuList.add(new Sikaku(new Position(yIndex, xIndex), new Position(yIndex, xIndex + 3)));
					}
				}
			}
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				List<Sikaku> filledSikakuList = new ArrayList<>();
				while (true) {
					// 問題生成
					// 左上から埋めていく。そうしないとまず唯一解にならない。
					boolean allFilled = true;
					outer: for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
						for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
							boolean already = false;
							for (Sikaku filledSikaku : filledSikakuList) {
								if (filledSikaku.isDuplicate(new Position(yIndex, xIndex))) {
									already = true;
									break;
								}
							}
							if (already) {
								continue;
							}
							List<Sikaku> useCandSikakuList = new ArrayList<>();
							for (Sikaku candSikaku : candSikakuList) {
								if (candSikaku.isDuplicate(new Position(yIndex, xIndex))) {
									useCandSikakuList.add(candSikaku);
								}
							}
							Collections.shuffle(useCandSikakuList);
							boolean filled = false;
							for (Sikaku candSikaku : useCandSikakuList) {
								boolean isOk = true;
								for (Sikaku filledSikaku : filledSikakuList) {
									// かぶっていないこと
									if (candSikaku.isDuplicate(filledSikaku)) {
										isOk = false;
										break;
									}
									// 同じ面積で接触していないこと
									if (candSikaku.getAreaSize() == filledSikaku.getAreaSize()) {
										Sikaku wkSikaku = new Sikaku(new Position(
												candSikaku.getLeftUp().getyIndex() - 1,
												candSikaku.getLeftUp().getxIndex() - 1),
												new Position(candSikaku.getRightDown().getyIndex() + 1,
														candSikaku.getRightDown().getxIndex() + 1));
										if (wkSikaku.isDuplicate(filledSikaku)) {
											isOk = false;
											break;
										}
									}
									// 畳になっていないこと
									if (candSikaku.getLeftUp().getyIndex() - 1 == filledSikaku.getRightDown()
											.getyIndex()
											&& candSikaku.getLeftUp().getxIndex() - 1 == filledSikaku.getRightDown()
													.getxIndex()) {
										isOk = false;
										break;
									}
									if (candSikaku.getLeftUp().getyIndex() - 1 == filledSikaku.getRightDown()
											.getyIndex()
											&& candSikaku.getRightDown().getxIndex() + 1 == filledSikaku.getLeftUp()
													.getxIndex()) {
										isOk = false;
										break;
									}
									if (candSikaku.getRightDown().getyIndex() + 1 == filledSikaku.getLeftUp()
											.getyIndex()
											&& candSikaku.getLeftUp().getxIndex() - 1 == filledSikaku.getRightDown()
													.getxIndex()) {
										isOk = false;
										break;
									}
									if (candSikaku.getRightDown().getyIndex() + 1 == filledSikaku.getLeftUp()
											.getyIndex()
											&& candSikaku.getRightDown().getxIndex() + 1 == filledSikaku.getLeftUp()
													.getxIndex()) {
										isOk = false;
										break;
									}
								}
								if (isOk) {
									filled = true;
									filledSikakuList.add(candSikaku);
									break;
								}
							}
							if (!filled) {
								// うめられてなければ抜ける。
								allFilled = false;
								break outer;
							}
						}
					}
					if (!allFilled) {
						// 埋めきれなければやり直し
						filledSikakuList.clear();
					} else {
						break;
					}
				}
				// ヒント設定
				// とりあえず、四角1つにつきランダムで1箇所設定
				List<Position> numberPosList = new ArrayList<>();
				for (Sikaku filledSikaku : filledSikakuList) {
					int yIndex = (int) (filledSikaku.getLeftUp().getyIndex() + (Math.random()
							* (filledSikaku.getRightDown().getyIndex() - filledSikaku.getLeftUp().getyIndex() + 1)));
					int xIndex = (int) (filledSikaku.getLeftUp().getxIndex() + (Math.random()
							* (filledSikaku.getRightDown().getxIndex() - filledSikaku.getLeftUp().getxIndex() + 1)));
					wkField.numbers[yIndex][xIndex] = filledSikaku.getAreaSize();
					numberPosList.add(new Position(yIndex, xIndex));
				}
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.numbersCand[yIndex][xIndex].clear();
						if (wkField.numbers[yIndex][xIndex] != null) {
							wkField.numbersCand[yIndex][xIndex].add(wkField.numbers[yIndex][xIndex]);
						} else {
							for (int number = 1; number <= 4; number++) {
								wkField.numbersCand[yIndex][xIndex].add(number);
							}
						}
					}
				}
				// 解けるかな？
				level = new FillmatSolverForGenerator(new FillmatSolverForGenerator.Field(wkField), 1000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new FillmatSolver.Field(height, width);
					filledSikakuList.clear();
				} else {
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						FillmatSolver.Field virtual = new FillmatSolver.Field(wkField, true);
						int yIndex = numberPos.getyIndex();
						int xIndex = numberPos.getxIndex();
						virtual.numbers[yIndex][xIndex] = null;
						virtual.numbersCand[yIndex][xIndex].clear();
						for (int number = 1; number <= 4; number++) {
							virtual.numbersCand[yIndex][xIndex].add(number);
						}
						int solveResult = new FillmatSolverForGenerator(new FillmatSolverForGenerator.Field(virtual),
								10000).solve2();
						if (solveResult != -1) {
							wkField.numbers[yIndex][xIndex] = null;
							wkField.numbersCand[yIndex][xIndex].clear();
							for (int number = 1; number <= 4; number++) {
								wkField.numbersCand[yIndex][xIndex].add(number);
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 5 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字:" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
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
								+ "\" fill=\""
								+ "black"
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
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
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
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
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
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// 固定数字(表示用)
		private final Integer[][] numbers;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public boolean[][] getYokoWall() {
			boolean[][] result = new boolean[getYLength()][getXLength() - 1];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					List<Integer> myCand = new ArrayList<>(numbersCand[yIndex][xIndex]);
					List<Integer> nextCand = new ArrayList<>(numbersCand[yIndex][xIndex + 1]);
					myCand.retainAll(nextCand);
					result[yIndex][xIndex] = myCand.isEmpty();
				}
			}
			return result;
		}

		public boolean[][] getTateWall() {
			boolean[][] result = new boolean[getYLength() - 1][getXLength()];
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					List<Integer> myCand = new ArrayList<>(numbersCand[yIndex][xIndex]);
					List<Integer> nextCand = new ArrayList<>(numbersCand[yIndex + 1][xIndex]);
					myCand.retainAll(nextCand);
					result[yIndex][xIndex] = myCand.isEmpty();
				}
			}
			return result;
		}

		public String getHintCount() {
			int cnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						cnt++;
					}
				}
			}
			return String.valueOf(cnt);
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?fillmat/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (numbers[yIndex][xIndex] == null) {
					interval++;
					if (interval == 26) {
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
						sb.append(ALPHABET.substring(interval - 1, interval));
						sb.append(numStr);
						interval = 0;
					}
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET.substring(interval - 1, interval));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int number = 1; number <= 4; number++) {
						numbersCand[yIndex][xIndex].add(number);
					}
				}
			}
			int readPos = 0;
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET.indexOf(ch);
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
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
						numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(num);
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
		}

		@SuppressWarnings("unchecked")
		public Field(Field other, boolean b) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int number = 0; number <= 4; number++) {
						numbersCand[yIndex][xIndex].add(number);
					}
				}
			}
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
						int index = HALF_NUMS.indexOf(numStr);
						if (index == 0) {
							sb.append("・");
						} else {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						}
					} else if (numbersCand[yIndex][xIndex].size() == 2) {
						sb.append(numbersCand[yIndex][xIndex].get(0));
						sb.append(numbersCand[yIndex][xIndex].get(1));
					} else {
						sb.append("　");
					}
				}
				sb.append(System.lineSeparator());
			}
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
		 * 部屋に固定数字が2つあったり、サイズが自分の数字を超えてはならない。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1) {
						Position pivot = new Position(yIndex, xIndex);
						Integer size = numbersCand[yIndex][xIndex].get(0);
						boolean foundNum = numbers[pivot.getyIndex()][pivot.getxIndex()] != null;
						Set<Position> continueWhitePosSet = new HashSet<>();
						continueWhitePosSet.add(pivot);
						if (!(setContinuePosSetContainsDoubleNumber(pivot, continueWhitePosSet, null,
								size, foundNum))) {
							return false;
						}
						Set<Position> continueNotBlackPosSet = new HashSet<>();
						continueNotBlackPosSet.add(pivot);
						if (!setContinuePosSet(pivot, continueNotBlackPosSet, null, size, foundNum)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 壁なし確定マスをつなげる。想定サイズを超えたり、数字を2個以上取り込んだら-1を返す。
		 * それ以外の場合は取り込んだ数字を返す。
		 * @param foundNum
		 */
		private boolean setContinuePosSetContainsDoubleNumber(Position pos, Set<Position> continuePosSet,
				Direction from, int size, boolean foundNum) {
			if (continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && (from == Direction.DOWN || from == null)) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (foundNum) {
							return false;
						} else {
							continuePosSet.add(nextPos);
							if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.DOWN,
									size, true))) {
								return false;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.DOWN, size,
								foundNum))) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && (from == Direction.LEFT || from == null)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (foundNum) {
							return false;
						} else {
							continuePosSet.add(nextPos);
							if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.LEFT, size,
									true))) {
								return false;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.LEFT, size,
								foundNum))) {
							return false;
						}
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && (from == Direction.UP || from == null)) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (foundNum) {
							return false;
						} else {
							continuePosSet.add(nextPos);
							if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.UP, size,
									true))) {
								return false;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.UP, size,
								foundNum))) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != 0 && (from == Direction.RIGHT || from == null)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (foundNum) {
							return false;
						} else {
							continuePosSet.add(nextPos);
							if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.RIGHT, size,
									true))) {
								return false;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (!(setContinuePosSetContainsDoubleNumber(nextPos, continuePosSet, Direction.RIGHT, size,
								foundNum))) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字を2個以上取り込まない範囲で壁未確定マスをつなげる。
		 * @param foundNum
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet,
				Direction from, int size, boolean foundNum) {
			if (continuePosSet.size() >= size) {
				return true;
			}
			if (pos.getyIndex() != 0 && (from == Direction.DOWN || from == null)) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (!foundNum) {
							continuePosSet.add(nextPos);
							if (setContinuePosSet(nextPos, continuePosSet, Direction.DOWN,
									size, true)) {
								return true;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (setContinuePosSet(nextPos, continuePosSet, Direction.DOWN, size, foundNum)) {
							return true;
						}
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && (from == Direction.LEFT || from == null)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (!foundNum) {
							continuePosSet.add(nextPos);
							if (setContinuePosSet(nextPos, continuePosSet, Direction.LEFT,
									size, true)) {
								return true;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (setContinuePosSet(nextPos, continuePosSet, Direction.LEFT, size, foundNum)) {
							return true;
						}
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && (from == Direction.UP || from == null)) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (!foundNum) {
							continuePosSet.add(nextPos);
							if (setContinuePosSet(nextPos, continuePosSet, Direction.UP,
									size, true)) {
								return true;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (setContinuePosSet(nextPos, continuePosSet, Direction.UP, size, foundNum)) {
							return true;
						}
					}
				}
			}
			if (pos.getxIndex() != 0 && (from == Direction.RIGHT || from == null)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(size)
						&& !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						if (!foundNum) {
							continuePosSet.add(nextPos);
							if (setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT,
									size, true)) {
								return true;
							}
						}
					} else {
						continuePosSet.add(nextPos);
						if (setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT, size, foundNum)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		/**
		 * 同じ数字が斜めに来ることはない。(隣接禁止、畳禁止)
		 */
		private boolean wallSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1) {
						Integer num = numbersCand[yIndex][xIndex].get(0);
						List<Integer> urCands = yIndex == 0 || xIndex == getXLength() - 1 ? null
								: numbersCand[yIndex - 1][xIndex + 1];
						List<Integer> rdCands = xIndex == getXLength() - 1 || yIndex == getYLength() - 1 ? null
								: numbersCand[yIndex + 1][xIndex + 1];
						List<Integer> dlCands = yIndex == getYLength() - 1 || xIndex == 0 ? null
								: numbersCand[yIndex + 1][xIndex - 1];
						List<Integer> luCands = xIndex == 0 || yIndex == 0 ? null : numbersCand[yIndex - 1][xIndex - 1];
						if (urCands != null) {
							urCands.remove(num);
							if (urCands.isEmpty()) {
								return false;
							}
						}
						if (rdCands != null) {
							rdCands.remove(num);
							if (rdCands.isEmpty()) {
								return false;
							}
						}
						if (dlCands != null) {
							dlCands.remove(num);
							if (dlCands.isEmpty()) {
								return false;
							}
						}
						if (luCands != null) {
							luCands.remove(num);
							if (luCands.isEmpty()) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 2x2のマスのうち、どこか縦横に2マスは連続が必須。(畳禁止)
		 */
		private boolean pileSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				outer: for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					List<Integer> cands1 = numbersCand[yIndex][xIndex];
					List<Integer> cands2 = numbersCand[yIndex][xIndex + 1];
					List<Integer> cands3 = numbersCand[yIndex + 1][xIndex + 1];
					List<Integer> cands4 = numbersCand[yIndex + 1][xIndex];
					for (Integer i : cands2) {
						if (cands1.contains(i)) {
							continue outer;
						}
					}
					for (Integer i : cands3) {
						if (cands2.contains(i)) {
							continue outer;
						}
					}
					for (Integer i : cands4) {
						if (cands3.contains(i)) {
							continue outer;
						}
					}
					for (Integer i : cands1) {
						if (cands4.contains(i)) {
							continue outer;
						}
					}
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
			if (!wallSolve()) {
				return false;
			}
			if (!pileSolve()) {
				return false;
			}
			if (!roomSolve()) {
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

	public FillmatSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public FillmatSolver(Field field) {
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
		System.out.println(new FillmatSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 5));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 5 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count / 5).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
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
		return true;
	}
}
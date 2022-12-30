package myamya.other.solver.dominion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class DominionSolver implements Solver {
	public static class DominionGenerator implements Generator {

		private static final HashMap<Integer, String> NUMBER_MAP = new HashMap<>();
		{
			NUMBER_MAP.put(-1, "？");
			NUMBER_MAP.put(1, "Ａ");
			NUMBER_MAP.put(2, "Ｂ");
			NUMBER_MAP.put(3, "Ｃ");
			NUMBER_MAP.put(4, "Ｄ");
			NUMBER_MAP.put(5, "Ｅ");
			NUMBER_MAP.put(6, "Ｆ");
			NUMBER_MAP.put(7, "Ｇ");
			NUMBER_MAP.put(8, "Ｈ");
			NUMBER_MAP.put(9, "Ｉ");
			NUMBER_MAP.put(10, "Ｊ");
			NUMBER_MAP.put(11, "Ｋ");
			NUMBER_MAP.put(12, "Ｌ");
			NUMBER_MAP.put(13, "Ｍ");
			NUMBER_MAP.put(14, "Ｎ");
			NUMBER_MAP.put(15, "Ｏ");
			NUMBER_MAP.put(16, "Ｐ");
			NUMBER_MAP.put(17, "Ｑ");
			NUMBER_MAP.put(18, "Ｒ");
			NUMBER_MAP.put(19, "Ｓ");
			NUMBER_MAP.put(20, "Ｔ");
			NUMBER_MAP.put(21, "Ｕ");
			NUMBER_MAP.put(22, "Ｖ");
			NUMBER_MAP.put(23, "Ｗ");
			NUMBER_MAP.put(24, "Ｘ");
			NUMBER_MAP.put(25, "Ｙ");
			NUMBER_MAP.put(26, "Ｚ");
			NUMBER_MAP.put(27, "ａ");
			NUMBER_MAP.put(28, "ｂ");
			NUMBER_MAP.put(29, "ｃ");
			NUMBER_MAP.put(30, "ｄ");
			NUMBER_MAP.put(31, "ｅ");
			NUMBER_MAP.put(32, "ｆ");
			NUMBER_MAP.put(33, "ｇ");
			NUMBER_MAP.put(34, "ｈ");
			NUMBER_MAP.put(35, "ｉ");
			NUMBER_MAP.put(36, "ｊ");
			NUMBER_MAP.put(37, "ｋ");
			NUMBER_MAP.put(38, "ｌ");
			NUMBER_MAP.put(39, "ｍ");
			NUMBER_MAP.put(40, "ｎ");
			NUMBER_MAP.put(41, "ｏ");
			NUMBER_MAP.put(42, "ｐ");
			NUMBER_MAP.put(43, "ｑ");
			NUMBER_MAP.put(44, "ｒ");
			NUMBER_MAP.put(45, "ｓ");
			NUMBER_MAP.put(46, "ｔ");
			NUMBER_MAP.put(47, "ｕ");
			NUMBER_MAP.put(48, "ｖ");
			NUMBER_MAP.put(49, "ｗ");
			NUMBER_MAP.put(50, "ｘ");
			NUMBER_MAP.put(51, "ｙ");
			NUMBER_MAP.put(52, "ｚ");
		}

		static class DominionSolverForGenerator extends DominionSolver {
			private final int limit;

			public DominionSolverForGenerator(Field field, int limit) {
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

		static class ExtendedField extends DominionSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width) {
				super(height, width);
			}

			@Override
			protected boolean notStandAloneSolve() {
				// 数字があとから決まるので、ここではじいてしまうとダメ。
				// 全通過させる
				// ただし、ユニークネスの観点で、盤面を分断していない黒マス=斜めの位置のマスには黒マスが1つ以上必要なのでチェック
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						Masu masu1 = masu[yIndex][xIndex];
						Masu masu2 = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masu3 = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						if (masu1 == Masu.BLACK && masu2 == Masu.BLACK) {
							// 横ドミノチェック
							Masu upLeft = (yIndex == 0 || xIndex == 0) ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex - 1];
							Masu upRight = (yIndex == 0 || xIndex >= getXLength() - 2) ? Masu.NOT_BLACK
									: masu[yIndex - 1][xIndex + 2];
							Masu downLeft = (yIndex == getYLength() - 1 || xIndex == 0) ? Masu.NOT_BLACK
									: masu[yIndex + 1][xIndex - 1];
							Masu downRight = (yIndex == getYLength() - 1 || xIndex >= getXLength() - 2) ? Masu.NOT_BLACK
									: masu[yIndex + 1][xIndex + 2];
							if (upLeft == Masu.NOT_BLACK && upRight == Masu.NOT_BLACK && downLeft == Masu.NOT_BLACK
									&& downRight == Masu.NOT_BLACK) {
								return false;
							}
							if (upLeft == Masu.SPACE && upRight == Masu.NOT_BLACK && downLeft == Masu.NOT_BLACK
									&& downRight == Masu.NOT_BLACK) {
								masu[yIndex - 1][xIndex - 1] = Masu.BLACK;
							}
							if (upLeft == Masu.NOT_BLACK && upRight == Masu.SPACE && downLeft == Masu.NOT_BLACK
									&& downRight == Masu.NOT_BLACK) {
								masu[yIndex - 1][xIndex + 2] = Masu.BLACK;
							}
							if (upLeft == Masu.NOT_BLACK && upRight == Masu.NOT_BLACK && downLeft == Masu.SPACE
									&& downRight == Masu.NOT_BLACK) {
								masu[yIndex + 1][xIndex - 1] = Masu.BLACK;
							}
							if (upLeft == Masu.NOT_BLACK && upRight == Masu.NOT_BLACK && downLeft == Masu.NOT_BLACK
									&& downRight == Masu.SPACE) {
								masu[yIndex + 1][xIndex + 2] = Masu.BLACK;
							}
						}
						if (masu1 == Masu.BLACK && masu3 == Masu.BLACK) {
							// 縦ドミノチェック
							Masu upLeft = (yIndex == 0 || xIndex == 0) ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex - 1];
							Masu upRight = (yIndex == 0 || xIndex == getXLength() - 1) ? Masu.NOT_BLACK
									: masu[yIndex - 1][xIndex + 1];
							Masu downLeft = (yIndex >= getYLength() - 2 || xIndex == 0) ? Masu.NOT_BLACK
									: masu[yIndex + 2][xIndex - 1];
							Masu downRight = (yIndex >= getYLength() - 2 || xIndex == getXLength() - 1) ? Masu.NOT_BLACK
									: masu[yIndex + 2][xIndex + 1];
							if (upLeft == Masu.NOT_BLACK && upRight == Masu.NOT_BLACK && downLeft == Masu.NOT_BLACK
									&& downRight == Masu.NOT_BLACK) {
								return false;
							}
							if (upLeft == Masu.SPACE && upRight == Masu.NOT_BLACK && downLeft == Masu.NOT_BLACK
									&& downRight == Masu.NOT_BLACK) {
								masu[yIndex - 1][xIndex - 1] = Masu.BLACK;
							}
							if (upLeft == Masu.NOT_BLACK && upRight == Masu.SPACE && downLeft == Masu.NOT_BLACK
									&& downRight == Masu.NOT_BLACK) {
								masu[yIndex - 1][xIndex + 1] = Masu.BLACK;
							}
							if (upLeft == Masu.NOT_BLACK && upRight == Masu.NOT_BLACK && downLeft == Masu.SPACE
									&& downRight == Masu.NOT_BLACK) {
								masu[yIndex + 2][xIndex - 1] = Masu.BLACK;
							}
							if (upLeft == Masu.NOT_BLACK && upRight == Masu.NOT_BLACK && downLeft == Masu.NOT_BLACK
									&& downRight == Masu.SPACE) {
								masu[yIndex + 2][xIndex + 1] = Masu.BLACK;
							}
						}
					}
				}
				return true;
			}
		}

		private final int height;
		private final int width;

		public DominionGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new DominionGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			ExtendedField wkField = new ExtendedField(height, width);
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
							ExtendedField virtual = new ExtendedField(wkField);
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
							wkField = new ExtendedField(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め
				// まず数字を埋める
				boolean existBlack = false;
				List<Position> notBlackPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							notBlackPosList.add(new Position(yIndex, xIndex));
						} else {
							existBlack = true;
						}
					}
				}
				if (!existBlack) {
					// 全白ます問題は出ないようにする
					wkField = new ExtendedField(height, width);
					index = 0;
					continue;
				}
				Collections.shuffle(notBlackPosList);
				int targetNumber = 0;
				Set<Set<Position>> alreadyPosSet = new HashSet<>();
				for (Position pos : notBlackPosList) {
					boolean alreadyNum = false;
					for (Set<Position> onePosSet : alreadyPosSet) {
						if (onePosSet.contains(pos)) {
							alreadyNum = true;
							break;
						}
					}
					if (alreadyNum) {
						continue;
					}
					targetNumber++;
					Set<Position> continueWhitePosSet = wkField.getContinueWhitePosSet(pos);
					for (Position targetPos : continueWhitePosSet) {
						wkField.numbers[targetPos.getyIndex()][targetPos.getxIndex()] = targetNumber;
					}
					alreadyPosSet.add(continueWhitePosSet);
				}
//				System.out.println(wkField);
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] == null) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				// 解けるかな？
				level = new DominionSolverForGenerator(new DominionSolver.Field(wkField), 30).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width);
					index = 0;
				} else {
					Collections.shuffle(notBlackPosList);
					for (Position numberPos : notBlackPosList) {
						boolean lastNum = false;
						for (Set<Position> onePosSet : alreadyPosSet) {
							if (onePosSet.contains(numberPos)) {
								if (onePosSet.size() == 1) {
									lastNum = true;
								}
								break;
							}
						}
						// 抜け番を防止する
						if (lastNum) {
							continue;
						}
//						System.out.println(level);
						DominionSolver.Field virtual = new DominionSolver.Field(wkField);
						virtual.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						int solveResult = new DominionSolverForGenerator(virtual, 10000).solve2();
						if (solveResult != -1) {
							wkField.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
							wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							level = solveResult;
							for (Set<Position> onePosSet : alreadyPosSet) {
								if (onePosSet.contains(numberPos)) {
									onePosSet.remove(numberPos);
								}
							}
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 3 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">pzprxsで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						if (xIndex == -1 || xIndex == wkField.getXLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						if (yIndex == -1 || yIndex == wkField.getYLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ NUMBER_MAP.get(wkField.getNumbers()[yIndex][xIndex]) + "</text>");
					}
				}
			}
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level + "(dominion)");
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}
	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		protected Masu[][] masu;
		protected Integer[][] numbers;

		public Masu[][] getMasu() {
			return masu;
		}

		/**
		 * あるマスからつながる白マスセットを返す。ジェネレータ用 posが白マスであることは事前にチェックしている前提
		 */
		protected Set<Position> getContinueWhitePosSet(Position pos) {
			Set<Position> result = new HashSet<>();
			result.add(pos);
			setContinueWhitePosSet(null, pos, result, null);
			return result;
		}

		public String getHintCount() {
			int numberCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						numberCnt++;
					}
				}
			}
			return String.valueOf(numberCnt);
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://pzprxs.vercel.app/p.html?dominion/" + getXLength() + "/" + getYLength() + "/");
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

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					// 16 - 255は '-'
					// 256 - 999は '+'
					int capacity;
					if (ch == '.') {
						Position pos = new Position(index / getXLength(), index % getXLength());
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							capacity = Integer
									.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3), 16);
							i++;
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			numbers = new Integer[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
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
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						} else if (numbers[yIndex][xIndex] == -1) {
							sb.append("？");
						} else {
							String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(capacityStr);
							if (index >= 0) {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							} else {
								sb.append(capacityStr);
							}
						}
					} else {
						sb.append(masu[yIndex][xIndex]);
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
					sb.append(masu[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 禁止系チェック
		 */
		public boolean roundSolve() {
			// 黒マスは必ず2マスつながる。3つながっている場合、1マス以上伸ばせない場合NG
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Masu masuPivot = masu[yIndex][xIndex];
					Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
					Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
					Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
					Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
					if (masuUp == Masu.BLACK && masuPivot == Masu.BLACK && masuDown == Masu.BLACK) {
						return false;
					}
					if (masuRight == Masu.BLACK && masuPivot == Masu.BLACK && masuLeft == Masu.BLACK) {
						return false;
					}
					if (masuPivot == Masu.BLACK && masuUp == Masu.NOT_BLACK && masuRight == Masu.NOT_BLACK
							&& masuDown == Masu.NOT_BLACK && masuLeft == Masu.NOT_BLACK) {
						return false;
					}
					if (masuUp == Masu.BLACK && masuPivot == Masu.SPACE && masuDown == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
					if (masuUp == Masu.SPACE && masuPivot == Masu.BLACK && masuDown == Masu.BLACK) {
						masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masuUp == Masu.BLACK && masuPivot == Masu.BLACK && masuDown == Masu.SPACE) {
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masuLeft == Masu.BLACK && masuPivot == Masu.SPACE && masuRight == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
					if (masuLeft == Masu.SPACE && masuPivot == Masu.BLACK && masuRight == Masu.BLACK) {
						masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
					}
					if (masuLeft == Masu.BLACK && masuPivot == Masu.BLACK && masuRight == Masu.SPACE) {
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masuPivot == Masu.SPACE && masuUp == Masu.NOT_BLACK && masuRight == Masu.NOT_BLACK
							&& masuDown == Masu.NOT_BLACK && masuLeft == Masu.NOT_BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
					if (masuPivot == Masu.BLACK && masuUp == Masu.SPACE && masuRight == Masu.NOT_BLACK
							&& masuDown == Masu.NOT_BLACK && masuLeft == Masu.NOT_BLACK) {
						masu[yIndex - 1][xIndex] = Masu.BLACK;
					}
					if (masuPivot == Masu.BLACK && masuUp == Masu.NOT_BLACK && masuRight == Masu.SPACE
							&& masuDown == Masu.NOT_BLACK && masuLeft == Masu.NOT_BLACK) {
						masu[yIndex][xIndex + 1] = Masu.BLACK;
					}
					if (masuPivot == Masu.BLACK && masuUp == Masu.NOT_BLACK && masuRight == Masu.NOT_BLACK
							&& masuDown == Masu.SPACE && masuLeft == Masu.NOT_BLACK) {
						masu[yIndex + 1][xIndex] = Masu.BLACK;
					}
					if (masuPivot == Masu.BLACK && masuUp == Masu.NOT_BLACK && masuRight == Masu.NOT_BLACK
							&& masuDown == Masu.NOT_BLACK && masuLeft == Masu.SPACE) {
						masu[yIndex][xIndex - 1] = Masu.BLACK;
					}
				}
			}
			// 黒マスが3つつながるパターンはダメ
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					int blackCnt = 0;
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK) {
						blackCnt++;
					}
					if (masu2 == Masu.BLACK) {
						blackCnt++;
					}
					if (masu3 == Masu.BLACK) {
						blackCnt++;
					}
					if (masu4 == Masu.BLACK) {
						blackCnt++;
					}
					if (blackCnt > 2) {
						return false;
					}
					if (blackCnt == 2) {
						if (masu1 == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
						if (masu2 == Masu.SPACE) {
							masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masu3 == Masu.SPACE) {
							masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masu4 == Masu.SPACE) {
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 違う数字が繋がっていたり同じ数字が分断されている場合はfalse。
		 */
		private boolean numberSolve() {
			// 違う数字つながりチェック
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continueWhitePosSet = new HashSet<>();
						continueWhitePosSet.add(pivot);
						if (!setContinueWhitePosSet(numbers[yIndex][xIndex], pivot, continueWhitePosSet, null)) {
							// 別部屋と連結
							return false;
						}
					}
				}
			}
			// 同じ数字分断チェック
			Set<Integer> checkedNumber = new HashSet<Integer>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1
							&& !checkedNumber.contains(numbers[yIndex][xIndex])) {
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continueNotBlackPosSet = new HashSet<>();
						continueNotBlackPosSet.add(pivot);
						setContinueNotBlackPosSet(numbers[yIndex][xIndex], pivot, continueNotBlackPosSet, null);
						for (int yIndexAnother = 0; yIndexAnother < getYLength(); yIndexAnother++) {
							for (int xIndexAnother = 0; xIndexAnother < getXLength(); xIndexAnother++) {
								if (numbers[yIndex][xIndex] == numbers[yIndexAnother][xIndexAnother]
										&& !continueNotBlackPosSet
												.contains(new Position(yIndexAnother, xIndexAnother))) {
									// 未回収マスあり
									return false;
								}
							}
						}
						checkedNumber.add(numbers[yIndex][xIndex]);
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定でないマスをつなげていくが、 繋げたマスの前後左右(自分が元いたマス以外)に違う数字を発見した場合はつながない。
		 */
		private void setContinueNotBlackPosSet(int originNumber, Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					Integer numberUp = nextPos.getyIndex() == 0 ? null
							: numbers[nextPos.getyIndex() - 1][nextPos.getxIndex()];
					Integer numberRight = nextPos.getxIndex() == getXLength() - 1 ? null
							: numbers[nextPos.getyIndex()][nextPos.getxIndex() + 1];
					Integer numberLeft = nextPos.getxIndex() == 0 ? null
							: numbers[nextPos.getyIndex()][nextPos.getxIndex() - 1];
					if ((numberUp != null && numberUp != -1 && numberUp != originNumber)
							|| (numberRight != null && numberRight != -1 && numberRight != originNumber)
							|| (numberLeft != null && numberLeft != -1 && numberLeft != originNumber)) {
						// つながない
					} else {
						continuePosSet.add(nextPos);
						setContinueNotBlackPosSet(originNumber, nextPos, continuePosSet, Direction.DOWN);
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					Integer numberUp = nextPos.getyIndex() == 0 ? null
							: numbers[nextPos.getyIndex() - 1][nextPos.getxIndex()];
					Integer numberRight = nextPos.getxIndex() == getXLength() - 1 ? null
							: numbers[nextPos.getyIndex()][nextPos.getxIndex() + 1];
					Integer numberDown = nextPos.getyIndex() == getYLength() - 1 ? null
							: numbers[nextPos.getyIndex() + 1][nextPos.getxIndex()];
					if ((numberUp != null && numberUp != -1 && numberUp != originNumber)
							|| (numberRight != null && numberRight != -1 && numberRight != originNumber)
							|| (numberDown != null && numberDown != -1 && numberDown != originNumber)) {
						// つながない
					} else {
						continuePosSet.add(nextPos);
						setContinueNotBlackPosSet(originNumber, nextPos, continuePosSet, Direction.LEFT);
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					Integer numberRight = nextPos.getxIndex() == getXLength() - 1 ? null
							: numbers[nextPos.getyIndex()][nextPos.getxIndex() + 1];
					Integer numberDown = nextPos.getyIndex() == getYLength() - 1 ? null
							: numbers[nextPos.getyIndex() + 1][nextPos.getxIndex()];
					Integer numberLeft = nextPos.getxIndex() == 0 ? null
							: numbers[nextPos.getyIndex()][nextPos.getxIndex() - 1];
					if ((numberLeft != null && numberLeft != -1 && numberLeft != originNumber)
							|| (numberRight != null && numberRight != -1 && numberRight != originNumber)
							|| (numberDown != null && numberDown != -1 && numberDown != originNumber)) {
						// つながない
					} else {
						continuePosSet.add(nextPos);
						setContinueNotBlackPosSet(originNumber, nextPos, continuePosSet, Direction.UP);
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					Integer numberUp = nextPos.getyIndex() == 0 ? null
							: numbers[nextPos.getyIndex() - 1][nextPos.getxIndex()];
					Integer numberDown = nextPos.getyIndex() == getYLength() - 1 ? null
							: numbers[nextPos.getyIndex() + 1][nextPos.getxIndex()];
					Integer numberLeft = nextPos.getxIndex() == 0 ? null
							: numbers[nextPos.getyIndex()][nextPos.getxIndex() - 1];
					if ((numberLeft != null && numberLeft != -1 && numberLeft != originNumber)
							|| (numberUp != null && numberUp != -1 && numberUp != originNumber)
							|| (numberDown != null && numberDown != -1 && numberDown != originNumber)) {
						// つながない
					} else {
						continuePosSet.add(nextPos);
						setContinueNotBlackPosSet(originNumber, nextPos, continuePosSet, Direction.RIGHT);
					}
				}
			}
		}

		/**
		 * posを起点に上下左右に白確定のマスを無制限につなぎ、違う数字にたどり着いたらfalseを返す。
		 */
		private boolean setContinueWhitePosSet(Integer originNumber, Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != originNumber) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet(originNumber, nextPos, continuePosSet, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != originNumber) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet(originNumber, nextPos, continuePosSet, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != originNumber) {

						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet(originNumber, nextPos, continuePosSet, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != -1
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != originNumber) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinueWhitePosSet(originNumber, nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 数字がない島はNG
		 */
		protected boolean notStandAloneSolve() {
			// 孤立チェック
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null && masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						whitePosSet.add(new Position(yIndex, xIndex));
					}
				}
			}
			while (!whitePosSet.isEmpty()) {
				Position typicalWhitePos = new ArrayList<>(whitePosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				if (!setContinueNotBlackPosSet(typicalWhitePos, continuePosSet, null)) {
					return false;
				}
				whitePosSet.removeAll(continuePosSet);
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていくが、 数字を見つけた時点でtrueを返す。
		 */
		private boolean setContinueNotBlackPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueNotBlackPosSet(nextPos, continuePosSet, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueNotBlackPosSet(nextPos, continuePosSet, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueNotBlackPosSet(nextPos, continuePosSet, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueNotBlackPosSet(nextPos, continuePosSet, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * 各種チェックを1セット実行
		 * 
		 * @param recursive
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!roundSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				if (!numberSolve()) {
					return false;
				}
				if (!notStandAloneSolve()) {
					return false;
				}
				return solveAndCheck();
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
	protected int count;

	public DominionSolver(Field field) {
		this.field = new Field(field);
	}

	public DominionSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://pzprxs.vercel.app/p?dominion/10/10/u3g1h1h1m2zv2m5h6h4g5u"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new DominionSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 3));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 3 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count / 3).toString() + "(Lv:" + level + ")";
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
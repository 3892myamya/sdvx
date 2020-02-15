package myamya.other.solver.balance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class BalanceSolver implements Solver {
	public static class BalanceGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class BalanceSolverForGenerator extends BalanceSolver {
			private final int limit;

			public BalanceSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
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
				return count;
			}

			@Override
			protected boolean candSolve(Field field, int recursive) {
				if (this.count >= limit) {
					return false;
				} else {
					return super.candSolve(field, recursive);
				}
			}
		}

		private final int height;
		private final int width;

		public BalanceGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new BalanceGenerator(5, 5).generate();
		}

		@Override
		public GeneratorResult generate() {
			BalanceSolver.Field wkField = new BalanceSolver.Field(height, width);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < (height * (width - 1)) + ((height - 1) * width); i++) {
				indexList.add(i);
			}
			Collections.shuffle(indexList);
			int index = 0;
			int level = 0;
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
							BalanceSolver.Field virtual = new BalanceSolver.Field(wkField);
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
								wkField.yokoWall = virtual.yokoWall;
								wkField.tateWall = virtual.tateWall;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new BalanceSolver.Field(height, width);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// できるだけ埋める
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							Position pos = new Position(yIndex, xIndex);
							// 白マス全表出
							int upSpaceCnt = 0;
							for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
								if (wkField.tateWall[targetY][xIndex] == Wall.EXISTS) {
									break;
								}
								upSpaceCnt++;
							}
							int rightSpaceCnt = 0;
							for (int targetX = xIndex + 1; targetX < wkField.getXLength(); targetX++) {
								if (wkField.yokoWall[yIndex][targetX - 1] == Wall.EXISTS) {
									break;
								}
								rightSpaceCnt++;
							}
							int downSpaceCnt = 0;
							for (int targetY = yIndex + 1; targetY < wkField.getYLength(); targetY++) {
								if (wkField.tateWall[targetY - 1][xIndex] == Wall.EXISTS) {
									break;
								}
								downSpaceCnt++;
							}
							int leftSpaceCnt = 0;
							for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
								if (wkField.yokoWall[yIndex][targetX] == Wall.EXISTS) {
									break;
								}
								leftSpaceCnt++;
							}
							wkField.numbers[yIndex][xIndex] = upSpaceCnt + rightSpaceCnt + downSpaceCnt + leftSpaceCnt;
							Set<Integer> wkSet = new HashSet<>();
							wkSet.add(upSpaceCnt);
							wkSet.add(rightSpaceCnt);
							wkSet.add(downSpaceCnt);
							wkSet.add(leftSpaceCnt);
							wkField.blackNum[yIndex][xIndex] = wkSet.size() == 3;
							numberPosList.add(pos);
						}
					}
				}
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] == null) {
							// 数字マスは白確定なので戻さない
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
						if (yIndex != 0) {
							wkField.tateWall[yIndex - 1][xIndex] = Wall.SPACE;
						}
						if (xIndex != wkField.getXLength() - 1) {
							wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
						}
						if (yIndex != wkField.getYLength() - 1) {
							wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
						}
						if (xIndex != 0) {
							wkField.yokoWall[yIndex][xIndex - 1] = Wall.SPACE;
						}
					}
				}
				// 解けるかな？
				level = new BalanceSolverForGenerator(new BalanceSolver.Field(wkField), 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new BalanceSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						BalanceSolver.Field virtual = new BalanceSolver.Field(wkField, true);
						int yIndex = numberPos.getyIndex();
						int xIndex = numberPos.getxIndex();
						virtual.numbers[yIndex][xIndex] = null;
						virtual.masu[yIndex][xIndex] = Masu.SPACE;
						int solveResult = new BalanceSolverForGenerator(virtual, 15000).solve2();
						if (solveResult != -1) {
							wkField.numbers[yIndex][xIndex] = null;
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
							level = solveResult;
						}
					}
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						BalanceSolver.Field virtual = new BalanceSolver.Field(wkField, true);
						int yIndex = numberPos.getyIndex();
						int xIndex = numberPos.getxIndex();
						if (virtual.numbers[yIndex][xIndex] != null) {
							virtual.numbers[yIndex][xIndex] = 0;
							int solveResult = new BalanceSolverForGenerator(virtual, 15000).solve2();
							if (solveResult != -1) {
								wkField.numbers[yIndex][xIndex] = 0;
								level = solveResult;
							}
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
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
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\""
								+ (wkField.isBlackNum()[yIndex][xIndex] ? "black": "white")
								+ "\", stroke=\"black\">"
								+ "</circle>");
						if (wkField.getNumbers()[yIndex][xIndex] != 0) {
							String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
							String masuStr = null;
							if (numberStr.equals("-1")) {
								masuStr = "？";
							} else {
								int numIdx = HALF_NUMS.indexOf(numberStr);
								if (numIdx >= 0) {
									masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
								} else {
									masuStr = numberStr;
								}
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize + 2)
									+ "\" font-size=\""
									+ (baseSize - 5)
									+ "\" textLength=\""
									+ (baseSize - 5)
									+ "\" fill=\""
									+ (wkField.isBlackNum()[yIndex][xIndex] ? "white": "black")
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr
									+ "</text>");
						}
					}
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
		// マスの情報
		protected Masu[][] masu;
		// 数字の情報
		protected final Integer[][] numbers;
		// 数字が白か黒か。trueなら黒
		protected final boolean[][] blackNum;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected Wall[][] tateWall;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public boolean[][] isBlackNum() {
			return blackNum;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://puzz.link/p?balance/" + getXLength() + "/" + getYLength() + "/");
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
					num = num * 2;
					if (blackNum[yIndex][xIndex]) {
						num++;
					}
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

		public Masu[][] getMasu() {
			return masu;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			blackNum = new boolean[height][width];
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
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int capacity;
					if (ch == '.') {
						//
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							capacity = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity / 2;
						blackNum[pos.getyIndex()][pos.getxIndex()] = capacity % 2 == 1;
					}
					index++;
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			numbers = other.numbers;
			blackNum = other.blackNum;
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
		}

		public Field(int height, int width) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			blackNum = new boolean[height][width];
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
		}

		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			numbers = new Integer[other.getYLength()][other.getXLength()];
			blackNum = new boolean[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
					blackNum[yIndex][xIndex] = other.blackNum[yIndex][xIndex];
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
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";
		private static final String FULL_NUMS_BLACK = "零一二三四五六七八九";

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
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						}
						String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(capacityStr);
						if (index >= 0) {
							if (blackNum[yIndex][xIndex]) {
								sb.append(FULL_NUMS_BLACK.substring(index / 2, index / 2 + 1));
							} else {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							}
						} else {
							sb.append(capacityStr);
						}
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex]);
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex]);
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
		 * 白マスの壁の数は2、黒マスの壁の数は4になる。違反の場合false。
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
						if ((existsCount == 3 && notExistsCount == 1)
								|| notExistsCount > 2) {
							return false;
						}
						if (existsCount > 2) {
							masu[yIndex][xIndex] = Masu.BLACK;
						} else if (notExistsCount != 0) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						// 自分が黒マスなら壁は4マス
						if (notExistsCount > 0) {
							return false;
						}
						// 周囲の壁を閉鎖
						if (yIndex != 0) {
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (xIndex != getXLength() - 1) {
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (yIndex != getYLength() - 1) {
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (xIndex != 0) {
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						// 自分が白マスなら壁は2マス
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
			Set<Position> blackCandPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						if (whitePosSet.isEmpty()) {
							whitePosSet.add(pos);
							setContinuePosSet(pos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(pos)) {
								return false;
							}
						}
					} else if (masu[yIndex][xIndex] == Masu.SPACE) {
						blackCandPosSet.add(pos);
					}
				}
			}
			blackCandPosSet.removeAll(whitePosSet);
			for (Position pos : blackCandPosSet) {
				masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
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
		 * 数字マスから自身+上下または左右にそれぞれ何マス白マスを伸ばせる可能性があるか調べる
		 * 伸ばしすぎ・伸ばせなさすぎはfalseを返す。
		 * さらに、数字の色が白ならバランス、黒ならアンバランス必須なのでそれを満たせない場合にfalseを返す。
		 */
		public boolean limitSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int upSpaceCnt = 0;
						int rightSpaceCnt = 0;
						int downSpaceCnt = 0;
						int leftSpaceCnt = 0;

						int upWhiteCnt = 0;
						int rightWhiteCnt = 0;
						int downWhiteCnt = 0;
						int leftWhiteCnt = 0;

						boolean upCounting = true;
						boolean rightCounting = true;
						boolean downCounting = true;
						boolean leftupCounting = true;

						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (tateWall[targetY][xIndex] == Wall.EXISTS) {
								break;
							}
							if (tateWall[targetY][xIndex] != Wall.NOT_EXISTS) {
								upCounting = false;
							}
							if (upCounting) {
								upWhiteCnt++;
							}
							upSpaceCnt++;
						}
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (yokoWall[yIndex][targetX - 1] == Wall.EXISTS) {
								break;
							}
							if (yokoWall[yIndex][targetX - 1] != Wall.NOT_EXISTS) {
								rightCounting = false;
							}
							if (rightCounting) {
								rightWhiteCnt++;
							}
							rightSpaceCnt++;
						}
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (tateWall[targetY - 1][xIndex] == Wall.EXISTS) {
								break;
							}
							if (tateWall[targetY - 1][xIndex] != Wall.NOT_EXISTS) {
								downCounting = false;
							}
							if (downCounting) {
								downWhiteCnt++;
							}
							downSpaceCnt++;
						}
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (yokoWall[yIndex][targetX] == Wall.EXISTS) {
								break;
							}
							if (yokoWall[yIndex][targetX] != Wall.NOT_EXISTS) {
								leftupCounting = false;
							}
							if (leftupCounting) {
								leftWhiteCnt++;
							}
							leftSpaceCnt++;
						}
						int spaceCount = upSpaceCnt + downSpaceCnt + rightSpaceCnt + leftSpaceCnt;
						int whiteCount = upWhiteCnt + downWhiteCnt + rightWhiteCnt + leftWhiteCnt;
						if (!blackNum[yIndex][xIndex]) {
							// 白の場合
							if (numbers[yIndex][xIndex] != 0) {
								// 手の長さが決まっている場合
								int hand = numbers[yIndex][xIndex] / 2;
								boolean upCanHand = upSpaceCnt >= hand;
								boolean rightCanHand = rightSpaceCnt >= hand;
								boolean downCanHand = downSpaceCnt >= hand;
								boolean leftCanHand = leftSpaceCnt >= hand;
								int canHandCnt = 0;
								canHandCnt = upCanHand ? canHandCnt + 1 : canHandCnt;
								canHandCnt = rightCanHand ? canHandCnt + 1 : canHandCnt;
								canHandCnt = downCanHand ? canHandCnt + 1 : canHandCnt;
								canHandCnt = leftCanHand ? canHandCnt + 1 : canHandCnt;
								if (canHandCnt <= 1) {
									// 伸ばせない
									return false;
								}
								if (upWhiteCnt > hand || downWhiteCnt > hand || rightWhiteCnt > hand
										|| leftWhiteCnt > hand) {
									// 伸ばしすぎ
									return false;
								}
								// 手の方向が確定
								if (canHandCnt == 2) {
									if (upCanHand) {
										for (int i = 1; i <= hand; i++) {
											if (tateWall[yIndex - i][xIndex] == Wall.EXISTS) {
												return false;
											}
											tateWall[yIndex - i][xIndex] = Wall.NOT_EXISTS;
										}
									}
									if (rightCanHand) {
										for (int i = 1; i <= hand; i++) {
											if (yokoWall[yIndex][xIndex + i - 1] == Wall.EXISTS) {
												return false;
											}
											yokoWall[yIndex][xIndex + i - 1] = Wall.NOT_EXISTS;
										}
									}
									if (downCanHand) {
										for (int i = 1; i <= hand; i++) {
											if (tateWall[yIndex + i - 1][xIndex] == Wall.EXISTS) {
												return false;
											}
											tateWall[yIndex + i - 1][xIndex] = Wall.NOT_EXISTS;
										}
									}
									if (leftCanHand) {
										for (int i = 1; i <= hand; i++) {
											if (yokoWall[yIndex][xIndex - i] == Wall.EXISTS) {
												return false;
											}
											yokoWall[yIndex][xIndex - i] = Wall.NOT_EXISTS;
										}
									}
								}
							} else {
								// 手の長さが未確定
								if (upWhiteCnt != 0) {
									if (downSpaceCnt < upWhiteCnt && rightSpaceCnt < upWhiteCnt
											&& leftSpaceCnt < upWhiteCnt) {
										// 伸ばせない、または伸ばしすぎ
										return false;
									}
								}
								if (downWhiteCnt != 0) {
									if (upSpaceCnt < downWhiteCnt && rightSpaceCnt < downWhiteCnt
											&& leftSpaceCnt < downWhiteCnt) {
										// 伸ばせない、または伸ばしすぎ
										return false;
									}
								}
								if (rightWhiteCnt != 0) {
									if (upSpaceCnt < rightWhiteCnt && downSpaceCnt < rightWhiteCnt
											&& leftSpaceCnt < rightWhiteCnt) {
										return false;
										// 伸ばせない、または伸ばしすぎ
									}

								}
								if (leftWhiteCnt != 0) {
									if (upSpaceCnt < leftWhiteCnt && downSpaceCnt < leftWhiteCnt
											&& rightSpaceCnt < leftWhiteCnt) {
										// 伸ばせない、または伸ばしすぎ
										return false;
									}
								}
							}
						} else {
							// 黒なのでバランス禁止
							if (upWhiteCnt != 0 && upWhiteCnt == upSpaceCnt) {
								if (downWhiteCnt == upWhiteCnt && downWhiteCnt == downSpaceCnt) {
									return false;
								}
								if (rightWhiteCnt == upWhiteCnt && rightWhiteCnt == rightSpaceCnt) {
									return false;
								}
								if (leftWhiteCnt == upWhiteCnt && leftWhiteCnt == leftSpaceCnt) {
									return false;
								}
							}
							if (downWhiteCnt != 0 && downWhiteCnt == downSpaceCnt) {
								if (rightWhiteCnt == downWhiteCnt && rightWhiteCnt == rightSpaceCnt) {
									return false;
								}
								if (leftWhiteCnt == downWhiteCnt && leftWhiteCnt == leftSpaceCnt) {
									return false;
								}
							}
							if (rightWhiteCnt != 0 && rightWhiteCnt == rightSpaceCnt) {
								if (leftWhiteCnt == rightWhiteCnt && leftWhiteCnt == leftSpaceCnt) {
									return false;
								}
							}
							if (numbers[yIndex][xIndex] != 0) {
								if (spaceCount < numbers[yIndex][xIndex]) {
									// 伸ばせない
									return false;
								}
								if (whiteCount > numbers[yIndex][xIndex]) {
									// 伸ばしすぎ
									return false;
								}
								// 確定分を伸ばす。
								int fixedWhiteUp = 0;
								int fixedWhiteRight = 0;
								int fixedWhiteDown = 0;
								int fixedWhitetLeft = 0;

								fixedWhiteUp = numbers[yIndex][xIndex]
										- (rightSpaceCnt + downSpaceCnt + leftSpaceCnt);
								fixedWhiteRight = numbers[yIndex][xIndex]
										- (upSpaceCnt + downSpaceCnt + leftSpaceCnt);
								fixedWhiteDown = numbers[yIndex][xIndex]
										- (upSpaceCnt + rightSpaceCnt + leftSpaceCnt);
								fixedWhitetLeft = numbers[yIndex][xIndex]
										- (upSpaceCnt + rightSpaceCnt + downSpaceCnt);

								if (fixedWhiteUp > 0) {
									for (int i = 1; i <= fixedWhiteUp; i++) {
										if (tateWall[yIndex - i][xIndex] == Wall.EXISTS) {
											return false;
										}
										tateWall[yIndex - i][xIndex] = Wall.NOT_EXISTS;
									}
								}
								if (fixedWhiteRight > 0) {
									for (int i = 1; i <= fixedWhiteRight; i++) {
										if (yokoWall[yIndex][xIndex + i - 1] == Wall.EXISTS) {
											return false;
										}
										yokoWall[yIndex][xIndex + i - 1] = Wall.NOT_EXISTS;
									}
								}
								if (fixedWhiteDown > 0) {
									for (int i = 1; i <= fixedWhiteDown; i++) {
										if (tateWall[yIndex + i - 1][xIndex] == Wall.EXISTS) {
											return false;
										}
										tateWall[yIndex + i - 1][xIndex] = Wall.NOT_EXISTS;
									}
								}
								if (fixedWhitetLeft > 0) {
									for (int i = 1; i <= fixedWhitetLeft; i++) {
										if (yokoWall[yIndex][xIndex - i] == Wall.EXISTS) {
											return false;
										}
										yokoWall[yIndex][xIndex - i] = Wall.NOT_EXISTS;
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
		 * フィールドに1つは白ますが必要。
		 */
		private boolean finalSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] != Masu.BLACK) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!limitSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!oddSolve()) {
					return false;
				}
				if (!connectSolve()) {
					return false;
				}
				if (!finalSolve()) {
					return false;
				}
			}
			return true;
		}

		public boolean isSolved() {
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

	public BalanceSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public BalanceSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		// http://puzz.link/p?balance/10/10/i9q0l1ld94h8g4g9j7g7k94p94o1h1u71g77h
		// http://puzz.link/p?balance/8/8/gdl7m1g10w0bh1i4i0g9kbidh
		String url = "https://puzz.link/p?balance/6/6/8m0m8h9m1m1"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new BalanceSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
				if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
					Masu masuLeft = field.masu[yIndex][xIndex];
					Masu masuRight = field.masu[yIndex][xIndex + 1];
					if (masuLeft == Masu.SPACE && masuRight == Masu.SPACE) {
						continue;
					}
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
					Masu masuUp = field.masu[yIndex][xIndex];
					Masu masuDown = field.masu[yIndex + 1][xIndex];
					if (masuUp == Masu.SPACE && masuDown == Masu.SPACE) {
						continue;
					}
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
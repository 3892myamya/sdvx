package myamya.other.solver.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class ContextSolver implements Solver {
	public static class ContextGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class ContextSolverForGenerator extends ContextSolver {
			private final int limit;

			public ContextSolverForGenerator(Field field, int limit) {
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

		public ContextGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new ContextGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			ContextSolver.Field wkField = new ContextSolver.Field(height, width);
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
						// 気持ち黒多め
						for (int i = 0; i < 5; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							ContextSolver.Field virtual = new ContextSolver.Field(wkField);
							if (masuNum < 1) {
								virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK;
							} else if (masuNum < 5) {
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
							wkField = new ContextSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め
				boolean existBlack = false;
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : wkField.masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == wkField.getXLength() - 1 ? Masu.NOT_BLACK
								: wkField.masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == wkField.getYLength() - 1 ? Masu.NOT_BLACK
								: wkField.masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : wkField.masu[yIndex][xIndex - 1];
						Masu masuUpRight = yIndex == 0 || xIndex == wkField.getXLength() - 1 ? Masu.NOT_BLACK
								: wkField.masu[yIndex - 1][xIndex + 1];
						Masu masuRightDown = xIndex == wkField.getXLength() - 1 || yIndex == wkField.getYLength() - 1
								? Masu.NOT_BLACK
								: wkField.masu[yIndex + 1][xIndex + 1];
						Masu masuDownLeft = yIndex == wkField.getYLength() - 1 || xIndex == 0 ? Masu.NOT_BLACK
								: wkField.masu[yIndex + 1][xIndex - 1];
						Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK
								: wkField.masu[yIndex - 1][xIndex - 1];
						int blackCnt = 0;
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							existBlack = true;
							if (masuUpRight == Masu.BLACK) {
								blackCnt++;
							}
							if (masuRightDown == Masu.BLACK) {
								blackCnt++;
							}
							if (masuDownLeft == Masu.BLACK) {
								blackCnt++;
							}
							if (masuLeftUp == Masu.BLACK) {
								blackCnt++;
							}
						} else if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							if (masuUp == Masu.BLACK) {
								blackCnt++;
							}
							if (masuRight == Masu.BLACK) {
								blackCnt++;
							}
							if (masuDown == Masu.BLACK) {
								blackCnt++;
							}
							if (masuLeft == Masu.BLACK) {
								blackCnt++;
							}
						}
						wkField.numbers[yIndex][xIndex] = blackCnt;
						numberPosList.add(new Position(yIndex, xIndex));
					}
				}
				if (!existBlack) {
					// 全白ます問題は出ないようにする
					wkField = new ContextSolver.Field(height, width);
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
				level = new ContextSolverForGenerator(new ContextSolver.Field(wkField), 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ContextSolver.Field(height, width);
					index = 0;
				} else {
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						ContextSolver.Field virtual = new ContextSolver.Field(wkField, true);
						virtual.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						int solveResult = new ContextSolverForGenerator(virtual, 5000).solve2();
						if (solveResult != -1) {
							wkField.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
							wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							level = solveResult;
							System.out.println(level);
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 2 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
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
						String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
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
		// 数字の情報
		protected final Integer[][] numbers;

		public Masu[][] getMasu() {
			return masu;
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
			sb.append("http://puzz.link/p?context/" + getXLength() + "/" + getYLength() + "/");
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
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
		}

		public Field(Field other, boolean flag) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			masu = new Masu[other.getYLength()][other.getXLength()];
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
		 * 白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			Position typicalWhitePos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						whitePosSet.add(whitePos);
						if (typicalWhitePos == null) {
							typicalWhitePos = whitePos;
						}
					}
				}
			}
			if (typicalWhitePos == null) {
				return true;
			} else {
				Set<Position> continuePosSet = new HashSet<>();
				setContinueWhitePosSet(typicalWhitePos, continuePosSet);
				whitePosSet.removeAll(continuePosSet);
				return whitePosSet.isEmpty();
			}
		}

		/**
		 * posを起点に上下左右に黒確定でないマスをつなげていく。壁は無視する。
		 */
		private void setContinueWhitePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet);
				}
			}
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
		 * 各種チェックを1セット実行
		 * 
		 * @param recursive
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
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

		/**
		 * 数字は自分が白マスの場合縦横の黒マス数。自分が黒マスの場合斜めの黒マス数
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
						Masu masuUpRight = yIndex == 0 || xIndex == getXLength() - 1 ? Masu.NOT_BLACK
								: masu[yIndex - 1][xIndex + 1];
						Masu masuRightDown = xIndex == getXLength() - 1 || yIndex == getYLength() - 1 ? Masu.NOT_BLACK
								: masu[yIndex + 1][xIndex + 1];
						Masu masuDownLeft = yIndex == getYLength() - 1 || xIndex == 0 ? Masu.NOT_BLACK
								: masu[yIndex + 1][xIndex - 1];
						Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex - 1];
						int blackCnt = 0;
						int blackNanameCnt = 0;
						int spaceCnt = 0;
						int spaceNanameCnt = 0;
						if (masuUp == Masu.BLACK) {
							blackCnt++;
						}
						if (masuRight == Masu.BLACK) {
							blackCnt++;
						}
						if (masuDown == Masu.BLACK) {
							blackCnt++;
						}
						if (masuLeft == Masu.BLACK) {
							blackCnt++;
						}
						if (masuUpRight == Masu.BLACK) {
							blackNanameCnt++;
						}
						if (masuRightDown == Masu.BLACK) {
							blackNanameCnt++;
						}
						if (masuDownLeft == Masu.BLACK) {
							blackNanameCnt++;
						}
						if (masuLeftUp == Masu.BLACK) {
							blackNanameCnt++;
						}
						if (masuUp == Masu.SPACE) {
							spaceCnt++;
						}
						if (masuRight == Masu.SPACE) {
							spaceCnt++;
						}
						if (masuDown == Masu.SPACE) {
							spaceCnt++;
						}
						if (masuLeft == Masu.SPACE) {
							spaceCnt++;
						}
						if (masuUpRight == Masu.SPACE) {
							spaceNanameCnt++;
						}
						if (masuRightDown == Masu.SPACE) {
							spaceNanameCnt++;
						}
						if (masuDownLeft == Masu.SPACE) {
							spaceNanameCnt++;
						}
						if (masuLeftUp == Masu.SPACE) {
							spaceNanameCnt++;
						}
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							// 黒マス過剰
							if (numbers[yIndex][xIndex] < blackCnt && numbers[yIndex][xIndex] < blackNanameCnt) {
								return false;
							}
							// 黒マス不足
							if (numbers[yIndex][xIndex] > blackCnt + spaceCnt
									&& numbers[yIndex][xIndex] > blackNanameCnt + spaceNanameCnt) {
								return false;
							}
							if (numbers[yIndex][xIndex] < blackCnt) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
							if (numbers[yIndex][xIndex] > blackNanameCnt + spaceNanameCnt) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							// 黒マス過剰
							if (numbers[yIndex][xIndex] < blackCnt) {
								return false;
							}
							// 黒マス不足
							if (numbers[yIndex][xIndex] > blackCnt + spaceCnt) {
								return false;
							}
							if (numbers[yIndex][xIndex] == blackCnt) {
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
							if (numbers[yIndex][xIndex] == blackCnt + spaceCnt) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.BLACK;
								}
							}
						} else if (masu[yIndex][xIndex] == Masu.BLACK) {
							// 黒マス過剰
							if (numbers[yIndex][xIndex] < blackNanameCnt) {
								return false;
							}
							// 黒マス不足
							if (numbers[yIndex][xIndex] > blackNanameCnt + spaceNanameCnt) {
								return false;
							}
							if (numbers[yIndex][xIndex] == blackNanameCnt) {
								if (masuUpRight == Masu.SPACE) {
									masu[yIndex - 1][xIndex + 1] = Masu.NOT_BLACK;
								}
								if (masuRightDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
								}
								if (masuDownLeft == Masu.SPACE) {
									masu[yIndex + 1][xIndex - 1] = Masu.NOT_BLACK;
								}
								if (masuLeftUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
								}
							}
							if (numbers[yIndex][xIndex] == blackNanameCnt + spaceNanameCnt) {
								if (masuUpRight == Masu.SPACE) {
									masu[yIndex - 1][xIndex + 1] = Masu.BLACK;
								}
								if (masuRightDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex + 1] = Masu.BLACK;
								}
								if (masuDownLeft == Masu.SPACE) {
									masu[yIndex + 1][xIndex - 1] = Masu.BLACK;
								}
								if (masuLeftUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex - 1] = Masu.BLACK;
								}
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
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public ContextSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public ContextSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?context/10/10/i1m2i2h2i1m2j2h3i4g1h1m3j1k3g2h3g3q2i1j1h1h"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new ContextSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 2));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 2 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 2).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
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
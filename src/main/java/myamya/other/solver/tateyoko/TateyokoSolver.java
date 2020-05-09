package myamya.other.solver.tateyoko;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.HintPattern;
import myamya.other.solver.Solver;

public class TateyokoSolver implements Solver {
	public static class TateyokoGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class AkariSolverForGenerator extends TateyokoSolver {

			private final int limit;

			public AkariSolverForGenerator(Field field, int limit) {
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
						if (!candSolve(field, recursiveCnt)) {
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

		/**
		 * 黒マス発生率。逆数なので多いほど少ない。いったん1/10で
		 */
		private static int BLACK_RATE = 10;

		private final int height;
		private final int width;
		private final HintPattern hintPattern;

		public TateyokoGenerator(int height, int width, HintPattern hintPattern) {
			this.height = height;
			this.width = width;
			this.hintPattern = hintPattern;
		}

		public static void main(String[] args) {
			new TateyokoGenerator(8, 8, HintPattern.getByVal(7, 8, 8)).generate();
		}

		@Override
		public GeneratorResult generate() {
			TateyokoSolver.Field wkField = new TateyokoSolver.Field(height, width, hintPattern, BLACK_RATE);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < height * width; i++) {
				indexList.add(i);
			}
			Collections.shuffle(indexList);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				while (!wkField.isSolved()) {
					int yIndex = indexList.get(index) / width;
					int xIndex = indexList.get(index) % width;
					if (!wkField.blackPosSet.contains(new Position(yIndex, xIndex))) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							TateyokoSolver.Field virtual = new TateyokoSolver.Field(wkField, true);
							if (masuNum < 1) {
								virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK;
							} else if (masuNum < 2) {
								virtual.masu[yIndex][xIndex] = Masu.BLACK;
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.masu = virtual.masu;
								wkField.numbers = virtual.numbers;
								break;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new TateyokoSolver.Field(height, width, hintPattern, BLACK_RATE);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// まず数字を埋める
				for (Position pos : wkField.blackPosSet) {
					int yesCnt = 0;
					Masu masuUp = pos.getyIndex() == 0 ? Masu.NOT_BLACK
							: wkField.blackPosSet.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))
									? Masu.NOT_BLACK
									: wkField.masu[pos.getyIndex() - 1][pos.getxIndex()];
					if (masuUp == Masu.BLACK) {
						yesCnt++;
					}
					Masu masuRight = pos.getxIndex() == wkField.getXLength() - 1 ? Masu.BLACK
							: wkField.blackPosSet.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))
									? Masu.BLACK
									: wkField.masu[pos.getyIndex()][pos.getxIndex() + 1];
					if (masuRight == Masu.NOT_BLACK) {
						yesCnt++;
					}
					Masu masuDown = pos.getyIndex() == wkField.getYLength() - 1 ? Masu.NOT_BLACK
							: wkField.blackPosSet.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))
									? Masu.NOT_BLACK
									: wkField.masu[pos.getyIndex() + 1][pos.getxIndex()];
					if (masuDown == Masu.BLACK) {
						yesCnt++;
					}
					Masu masuLeft = pos.getxIndex() == 0 ? Masu.BLACK
							: wkField.blackPosSet.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))
									? Masu.BLACK
									: wkField.masu[pos.getyIndex()][pos.getxIndex() - 1];
					if (masuLeft == Masu.NOT_BLACK) {
						yesCnt++;
					}
					wkField.numbers[pos.getyIndex()][pos.getxIndex()] = yesCnt;
				}
				List<List<Position>> connectPosList = new ArrayList<>();
				Set<Position> alreadyPosSet = new HashSet<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.blackPosSet.contains(new Position(yIndex, xIndex))) {
							continue;
						}
						if (alreadyPosSet.contains(new Position(yIndex, xIndex))) {
							continue;
						}
						List<Position> connectPos = new ArrayList<>();
						alreadyPosSet.add(new Position(yIndex, xIndex));
						connectPos.add(new Position(yIndex, xIndex));
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							for (int candY = yIndex - 1; candY >= 0; candY--) {
								if (wkField.masu[candY][xIndex] == Masu.BLACK) {
									alreadyPosSet.add(new Position(candY, xIndex));
									connectPos.add(new Position(candY, xIndex));
								} else {
									break;
								}
							}
							for (int candY = yIndex + 1; candY < wkField.getYLength(); candY++) {
								if (wkField.masu[candY][xIndex] == Masu.BLACK) {
									alreadyPosSet.add(new Position(candY, xIndex));
									connectPos.add(new Position(candY, xIndex));
								} else {
									break;
								}
							}
						} else if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							for (int candX = xIndex + 1; candX < wkField.getXLength(); candX++) {
								if (wkField.masu[yIndex][candX] == Masu.NOT_BLACK) {
									alreadyPosSet.add(new Position(yIndex, candX));
									connectPos.add(new Position(yIndex, candX));
								} else {
									break;
								}
							}
							for (int candX = xIndex - 1; candX >= 0; candX--) {
								if (wkField.masu[yIndex][candX] == Masu.NOT_BLACK) {
									alreadyPosSet.add(new Position(yIndex, candX));
									connectPos.add(new Position(yIndex, candX));
								} else {
									break;
								}
							}
						}
						connectPosList.add(connectPos);
					}
				}
				for (List<Position> connectPos : connectPosList) {
					Collections.shuffle(connectPos);
					Position pos = connectPos.get(0);
					wkField.numbers[pos.getyIndex()][pos.getxIndex()] = connectPos.size();
				}
				// マスを戻す
				List<Position> numberPosList = new ArrayList<>(wkField.blackPosSet);
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.blackPosSet.contains(new Position(yIndex, xIndex))) {
							numberPosList.add(new Position(yIndex, xIndex));
						} else {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
							if (wkField.numbers[yIndex][xIndex] != null) {
								numberPosList.add(new Position(yIndex, xIndex));
							}
						}
					}
				}
				// 解けるかな？
				level = new AkariSolverForGenerator(wkField, 200).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new TateyokoSolver.Field(height, width, hintPattern, BLACK_RATE);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						TateyokoSolver.Field virtual = new TateyokoSolver.Field(wkField, true);
						if (virtual.blackPosSet.contains(numberPos)) {
							virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = -1;
						} else {
							virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						}
						int solveResult = new AkariSolverForGenerator(virtual, 1000).solve2();
						if (solveResult != -1) {
							if (wkField.blackPosSet.contains(numberPos)) {
								wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = -1;
							} else {
								wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 5 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(黒数字/黒/白数字：" + wkField.getHintCount().split("/")[0] + "/"
					+ wkField.getHintCount().split("/")[1] + "/"
					+ wkField.getHintCount().split("/")[2] + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
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
			// 数字
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getBlackPosSet().contains(new Position(yIndex, xIndex))) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
						if (wkField.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
							int idx = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (idx >= 0) {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 5)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize + 2)
									+ "\" fill=\""
									+ "white"
									+ "\" font-size=\""
									+ (baseSize - 5)
									+ "\" textLength=\""
									+ (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ masuStr
									+ "</text>");
						}
					} else {
						if (wkField.getNumbers()[yIndex][xIndex] != null) {
							String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
							int idx = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (idx >= 0) {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 5)
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
			}
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}

	}

	public static class Field {
		// マスの情報
		// ここでは、白マスを─、黒マスを│として扱う。
		private Masu[][] masu;
		// 数字の情報
		private Integer[][] numbers;
		// 黒マスの位置
		private final Set<Position> blackPosSet;

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public Set<Position> getBlackPosSet() {
			return blackPosSet;
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			blackPosSet = new HashSet<>();
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == 'n') {
					// 白マス1つ
					index++;
				} else if (ch == 'i') {
					// 連続白マス
					i++;
					int interval = Character.getNumericValue(param.charAt(i));
					if (interval != -1) {
						index = index + interval;
					}
				} else if (ch == 'o' || ch == 'p' || ch == 'q' || ch == 'r' || ch == 's' || ch == 'x') {
					// 黒マス1つ
					blackPosSet.add(pos);
					if (ch == 'x') {
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else if (ch == 'o') {
						numbers[pos.getyIndex()][pos.getxIndex()] = 0;
					} else if (ch == 'p') {
						numbers[pos.getyIndex()][pos.getxIndex()] = 1;
					} else if (ch == 'q') {
						numbers[pos.getyIndex()][pos.getxIndex()] = 2;
					} else if (ch == 'r') {
						numbers[pos.getyIndex()][pos.getxIndex()] = 3;
					} else if (ch == 's') {
						numbers[pos.getyIndex()][pos.getxIndex()] = 4;
					}
					index++;
				} else {
					// 数字マス
					//16 - 255は '-'
					int capacity;
					if (ch == '.') {
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
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
			blackPosSet = other.blackPosSet;
		}

		/**
		 * numbersをイミュータブルにするためのコンストラクタ。flagはダミー
		 */
		public Field(Field other, boolean flag) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
				}
			}
			blackPosSet = other.blackPosSet;
		}

		/**
		 * プレーン盤面生成。seedは壁の発生率の逆数。(5だと1/5で壁発生)
		 */
		public Field(int height, int width, HintPattern hintPattern, int seed) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			List<Set<Position>> posSetList = hintPattern.getPosSetList();
			for (Set<Position> posSet : posSetList) {
				if (Math.random() * seed < 1) {
					for (Position pos : posSet) {
						blackPosSet.add(pos);
					}
				}
			}
		}

		//		if (ch == 'n') {
		//			// 白マス1つ
		//			index++;
		//		} else if (ch == 'i') {
		//			// 連続白マス
		//			i++;
		//			int interval = Character.getNumericValue(param.charAt(i));
		//			if (interval != -1) {
		//				index = index + interval;
		//			}
		//		} else if (ch == 'o' || ch == 'p' || ch == 'q' || ch == 'r' || ch == 's' || ch == 'x') {
		//			// 黒マス1つ
		//			blackPosSet.add(pos);
		//			if (ch == 'x') {
		//				numbers[pos.getyIndex()][pos.getxIndex()] = -1;
		//			} else if (ch == 'o') {
		//				numbers[pos.getyIndex()][pos.getxIndex()] = 0;
		//			} else if (ch == 'p') {
		//				numbers[pos.getyIndex()][pos.getxIndex()] = 1;
		//			} else if (ch == 'q') {
		//				numbers[pos.getyIndex()][pos.getxIndex()] = 2;
		//			} else if (ch == 'r') {
		//				numbers[pos.getyIndex()][pos.getxIndex()] = 3;
		//			} else if (ch == 's') {
		//				numbers[pos.getyIndex()][pos.getxIndex()] = 4;
		//			}
		//			index++;
		//		} else {
		//			// 数字マス
		//			//16 - 255は '-'
		//			int capacity;
		//			if (ch == '.') {
		//				numbers[pos.getyIndex()][pos.getxIndex()] = -1;
		//			} else {
		//				if (ch == '-') {
		//					capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
		//					i++;
		//					i++;
		//				} else {
		//					capacity = Integer.parseInt(String.valueOf(ch), 16);
		//				}
		//				numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
		//			}
		//			index++;
		//		}

		static final String NUMELIC = "123456789abcdef";

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?tateyoko/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (blackPosSet.contains(new Position(yIndex, xIndex))) {
					if (interval != 0) {
						if (interval == 1) {
							sb.append("n");
						} else {
							sb.append("i");
							sb.append(NUMELIC.substring(interval - 1, interval));
						}
						interval = 0;
					}
					if (numbers[yIndex][xIndex] == -1) {
						sb.append("x");
					} else if (numbers[yIndex][xIndex] == 0) {
						sb.append("o");
					} else if (numbers[yIndex][xIndex] == 1) {
						sb.append("p");
					} else if (numbers[yIndex][xIndex] == 2) {
						sb.append("q");
					} else if (numbers[yIndex][xIndex] == 3) {
						sb.append("r");
					} else if (numbers[yIndex][xIndex] == 4) {
						sb.append("s");
					}

				} else if (numbers[yIndex][xIndex] != null) {
					if (interval != 0) {
						if (interval == 1) {
							sb.append("n");
						} else {
							sb.append("i");
							sb.append(NUMELIC.substring(interval - 1, interval));
						}
						interval = 0;
					}
					Integer num = numbers[yIndex][xIndex];
					String numStr = Integer.toHexString(num);
					if (numStr.length() == 2) {
						numStr = "-" + numStr;
					} else if (numStr.length() == 3) {
						numStr = "+" + numStr;
					}
					sb.append(numStr);
				} else {
					interval++;
					if (interval == 15) {
						sb.append("if");
						interval = 0;
					}
				}
			}
			if (interval != 0) {
				if (interval == 1) {
					sb.append("n");
				} else {
					sb.append("i");
					sb.append(NUMELIC.substring(interval - 1, interval));
				}
				interval = 0;
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public String getHintCount() {
			int kuroCnt = 0;
			int kuroNumCnt = 0;
			int numberCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (blackPosSet.contains(new Position(yIndex, xIndex))) {
						kuroCnt++;
						if (numbers[yIndex][xIndex] != -1) {
							kuroNumCnt++;
						}
					} else {
						if (numbers[yIndex][xIndex] != null) {
							numberCnt++;
						}

					}
				}
			}
			return String.valueOf(kuroNumCnt + "/" + kuroCnt + "/" + numberCnt);
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";
		private static final String FULL_NUMS_KANJI = "×ＡＢＣＤ";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE && numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						} else if (numbers[yIndex][xIndex] == -1) {
							sb.append("？");
						} else {
							String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
							int index = HALF_NUMS.indexOf(capacityStr);
							if (index >= 0) {
								if (blackPosSet.contains(new Position(yIndex, xIndex))) {
									sb.append(FULL_NUMS_KANJI.substring(index / 2, index / 2 + 1));
								} else {
									sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
								}
							} else {
								sb.append(capacityStr);
							}
						}
					} else {
						sb.append(masu[yIndex][xIndex] == Masu.BLACK ? "│"
								: masu[yIndex][xIndex] == Masu.NOT_BLACK ? "─" : "　");
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
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 黒マスの数字は横に隣接する白マス+縦に隣接する黒マスの数を示す。
		 * 白マスの数字は横に連続する白マスか縦に連続する黒マスの数を示す
		 * 矛盾する場合falseを返す。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						if (blackPosSet.contains(new Position(yIndex, xIndex))) {
							int yesCnt = 0;
							int noCnt = 0;
							Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK
									: blackPosSet.contains(new Position(yIndex - 1, xIndex)) ? Masu.NOT_BLACK
											: masu[yIndex - 1][xIndex];
							if (masuUp == Masu.BLACK) {
								yesCnt++;
							} else if (masuUp == Masu.NOT_BLACK) {
								noCnt++;
							}
							Masu masuRight = xIndex == getXLength() - 1 ? Masu.BLACK
									: blackPosSet.contains(new Position(yIndex, xIndex + 1)) ? Masu.BLACK
											: masu[yIndex][xIndex + 1];
							if (masuRight == Masu.BLACK) {
								noCnt++;
							} else if (masuRight == Masu.NOT_BLACK) {
								yesCnt++;
							}
							Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK
									: blackPosSet.contains(new Position(yIndex + 1, xIndex)) ? Masu.NOT_BLACK
											: masu[yIndex + 1][xIndex];
							if (masuDown == Masu.BLACK) {
								yesCnt++;
							} else if (masuDown == Masu.NOT_BLACK) {
								noCnt++;
							}
							Masu masuLeft = xIndex == 0 ? Masu.BLACK
									: blackPosSet.contains(new Position(yIndex, xIndex - 1)) ? Masu.BLACK
											: masu[yIndex][xIndex - 1];
							if (masuLeft == Masu.BLACK) {
								noCnt++;
							} else if (masuLeft == Masu.NOT_BLACK) {
								yesCnt++;
							}
							if (numbers[yIndex][xIndex] < yesCnt) {
								// 自分向き過剰
								return false;
							}
							if (numbers[yIndex][xIndex] > 4 - noCnt) {
								// 自分向き不足
								return false;
							}
							if (numbers[yIndex][xIndex] == yesCnt) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.BLACK;
								}
							}
							if (numbers[yIndex][xIndex] == 4 - noCnt) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
								}
							}
						} else {
							int canConnect = numbers[yIndex][xIndex] - 1;
							int fixUpCnt = 0;
							int upCnt = 0;
							boolean fixUpContinue = true;
							boolean fixUpReachNumber = false;
							for (int candY = yIndex - 1; candY >= 0; candY--) {
								if (masu[candY][xIndex] == Masu.NOT_BLACK
										|| blackPosSet.contains(new Position(candY, xIndex))) {
									break;
								}
								if (masu[candY][xIndex] == Masu.BLACK && fixUpContinue) {
									fixUpCnt++;
									if (numbers[candY][xIndex] != null) {
										fixUpReachNumber = true;
									}
								} else {
									fixUpContinue = false;
								}
								upCnt++;
							}
							int fixRightCnt = 0;
							int rightCnt = 0;
							boolean fixRightContinue = true;
							boolean fixRightReachNumber = false;
							for (int candX = xIndex + 1; candX < getXLength(); candX++) {
								if (masu[yIndex][candX] == Masu.BLACK
										|| blackPosSet.contains(new Position(yIndex, candX))) {
									break;
								}
								if (masu[yIndex][candX] == Masu.NOT_BLACK && fixRightContinue) {
									fixRightCnt++;
									if (numbers[yIndex][candX] != null) {
										fixRightReachNumber = true;
									}
								} else {
									fixRightContinue = false;
								}
								rightCnt++;
							}
							int fixDownCnt = 0;
							int downCnt = 0;
							boolean fixDownContinue = true;
							boolean fixDownReachNumber = false;
							for (int candY = yIndex + 1; candY < getYLength(); candY++) {
								if (masu[candY][xIndex] == Masu.NOT_BLACK
										|| blackPosSet.contains(new Position(candY, xIndex))) {
									break;
								}
								if (masu[candY][xIndex] == Masu.BLACK && fixDownContinue) {
									fixDownCnt++;
									if (numbers[candY][xIndex] != null) {
										fixDownReachNumber = true;
									}
								} else {
									fixDownContinue = false;
								}
								downCnt++;
							}
							int fixLeftCnt = 0;
							int leftCnt = 0;
							boolean fixLeftContinue = true;
							boolean fixLeftReachNumber = false;
							for (int candX = xIndex - 1; candX >= 0; candX--) {
								if (masu[yIndex][candX] == Masu.BLACK
										|| blackPosSet.contains(new Position(yIndex, candX))) {
									break;
								}
								if (masu[yIndex][candX] == Masu.NOT_BLACK && fixLeftContinue) {
									fixLeftCnt++;
									if (numbers[yIndex][candX] != null) {
										fixLeftReachNumber = true;
									}
								} else {
									fixLeftContinue = false;
								}
								leftCnt++;
							}
							// 必ず縦につながる数
							int varticalFix = fixUpCnt + fixDownCnt;
							// 必ず横につながる数
							int horizonalFix = fixRightCnt + fixLeftCnt;
							// 縦につながることができる数
							int varticalCapacity = upCnt + downCnt;
							// 横につながることができる数
							int horizonalCapacity = rightCnt + leftCnt;
							// 縦につながると別の数字とぶつかる
							boolean varticalReachNumber = fixUpReachNumber || fixDownReachNumber;
							// 横につながると別の数字とぶつかる
							boolean horizonalReachNumber = fixRightReachNumber || fixLeftReachNumber;
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								if (canConnect > varticalCapacity
										&& canConnect > horizonalCapacity) {
									return false;
								}
								if (canConnect < varticalFix
										&& canConnect < horizonalFix) {
									return false;
								}
								if (varticalReachNumber
										&& horizonalReachNumber) {
									return false;
								}
								if (canConnect > horizonalCapacity || canConnect < horizonalFix
										|| horizonalReachNumber) {
									masu[yIndex][xIndex] = Masu.BLACK;
								}
								if (canConnect > varticalCapacity || canConnect < varticalFix || varticalReachNumber) {
									masu[yIndex][xIndex] = Masu.NOT_BLACK;
								}
							}
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								if (canConnect > varticalCapacity || canConnect < varticalFix || varticalReachNumber) {
									return false;
								}
								// 伸びる方向の確定処理
								int up = canConnect - downCnt;
								int cnt = 0;
								while (true) {
									if (cnt >= up) {
										break;
									}
									masu[yIndex - 1 - cnt][xIndex] = Masu.BLACK;
									cnt++;
								}
								int down = canConnect - upCnt;
								cnt = 0;
								while (true) {
									if (cnt >= down) {
										break;
									}
									masu[yIndex + 1 + cnt][xIndex] = Masu.BLACK;
									cnt++;
								}
							} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								if (canConnect > horizonalCapacity || canConnect < horizonalFix
										|| horizonalReachNumber) {
									return false;
								}
								// 伸びる方向の確定処理
								int right = canConnect - leftCnt;
								int cnt = 0;
								while (true) {
									if (cnt >= right) {
										break;
									}
									masu[yIndex][xIndex + 1 + cnt] = Masu.NOT_BLACK;
									cnt++;
								}
								int left = canConnect - rightCnt;
								cnt = 0;
								while (true) {
									if (cnt >= left) {
										break;
									}
									masu[yIndex][xIndex - 1 - cnt] = Masu.NOT_BLACK;
									cnt++;
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
					if (masu[yIndex][xIndex] == Masu.SPACE && !blackPosSet.contains(new Position(yIndex, xIndex))) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public TateyokoSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public TateyokoSolver(Field field) {
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
		System.out.println(new TateyokoSolver(height, width, param).solve());
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
				if (!candSolve(field, recursiveCnt)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				System.out.println(field);
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count * 5));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 5 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 5).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE
						&& !field.blackPosSet.contains(new Position(yIndex, xIndex))) {
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
		}
		return true;
	}
}
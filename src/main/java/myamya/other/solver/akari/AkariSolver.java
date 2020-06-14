package myamya.other.solver.akari;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.AkariBattleResult;
import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.HintPattern;
import myamya.other.solver.Solver;

public class AkariSolver implements Solver {

	public static class AkariSolverForBattle extends AkariSolver {

		public AkariSolverForBattle(String fieldStr, int height, int width) {
			super(fieldStr, height, width);
		}

		public AkariBattleResult solve3() {
			try {
				while (!field.isSolved()) {
					String befStr = field.getStateDump();
					if (!field.solveAndCheck()) {
						return new AkariBattleResult("矛盾発生！", field.getFieldStr(), true);
					}
					int recursiveCnt = 0;
					while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
						if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
							return new AkariBattleResult("矛盾発生！", field.getFieldStr(), true);
						}
						recursiveCnt++;
					}
					if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
						return new AkariBattleResult("OK", field.getFieldStr(), false);
					}
				}
			} catch (CountOverException e) {
				return new AkariBattleResult("OK", field.getFieldStr(), false);
			}
			return new AkariBattleResult("勝負あり！", field.getFieldStr(), true);
		}

		@Override
		protected boolean candSolve(Field field, int recursive) {
			if (this.count >= 5000) {
				throw new CountOverException();
			} else {
				return super.candSolve(field, recursive);
			}
		}
	}

	public static class AkariGenerator implements Generator {
		static class AkariSolverForGenerator extends AkariSolver {

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

		/**
		 * 黒マス発生率。逆数なので多いほど少ない。いったん1/8で
		 */
		private static int BLACK_RATE = 8;

		private final int height;
		private final int width;
		private final HintPattern hintPattern;

		public AkariGenerator(int height, int width, HintPattern hintPattern) {
			this.height = height;
			this.width = width;
			this.hintPattern = hintPattern;
		}

		public static void main(String[] args) {
			new AkariGenerator(5, 5, HintPattern.getByVal(7, 5, 5)).generate();
		}

		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public GeneratorResult generate() {
			AkariSolver.Field wkField = new AkariSolver.Field(height, width, hintPattern, BLACK_RATE);
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
					if (wkField.masu[yIndex][xIndex] == Masu.SPACE) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							AkariSolver.Field virtual = new AkariSolver.Field(wkField, true);
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
							wkField = new AkariSolver.Field(height, width, hintPattern, BLACK_RATE);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// まず数字を埋める
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] != null) {
							int blackCnt = 0;
							Masu masuUp = yIndex == 0 || wkField.numbers[yIndex - 1][xIndex] != null ? Masu.NOT_BLACK
									: wkField.masu[yIndex - 1][xIndex];
							Masu masuRight = xIndex == wkField.getXLength() - 1
									|| wkField.numbers[yIndex][xIndex + 1] != null ? Masu.NOT_BLACK
											: wkField.masu[yIndex][xIndex + 1];
							Masu masuDown = yIndex == wkField.getYLength() - 1
									|| wkField.numbers[yIndex + 1][xIndex] != null ? Masu.NOT_BLACK
											: wkField.masu[yIndex + 1][xIndex];
							Masu masuLeft = xIndex == 0 || wkField.numbers[yIndex][xIndex - 1] != null ? Masu.NOT_BLACK
									: wkField.masu[yIndex][xIndex - 1];
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
							wkField.numbers[yIndex][xIndex] = blackCnt;
							numberPosList.add(new Position(yIndex, xIndex));
						}
					}
				}
				// マスを戻す
				boolean isOk = false;
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] == null) {
							isOk = true;
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				// 解けるかな？
				level = new AkariSolverForGenerator(wkField, 200).solve2();
				if (!isOk || level == -1) {
					// 解けなければやり直し
					wkField = new AkariSolver.Field(height, width, hintPattern, BLACK_RATE);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						AkariSolver.Field virtual = new AkariSolver.Field(wkField, true);
						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = -1;
						int solveResult = new AkariSolverForGenerator(virtual, 1200).solve2();
						if (solveResult != -1) {
							wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = -1;
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 15 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字/黒：" + wkField.getHintCount().split("/")[0] + "/"
					+ wkField.getHintCount().split("/")[1] + ")";
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
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 5 + margin)
									+ "\" x=\""
									+ (xIndex * baseSize + baseSize + 2)
									+ "\" fill=\""
									+ "white"
									+ "\" font-size=\""
									+ (baseSize - 5)
									+ "\" textLength=\""
									+ (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ FULL_NUMS.substring(wkField.getNumbers()[yIndex][xIndex],
											wkField.getNumbers()[yIndex][xIndex] + 1)
									+ "</text>");
						}
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
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報。BLACKは明かりあり
		protected Masu[][] masu;
		// 数字の情報
		protected Integer[][] numbers;

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?akari/" + getXLength() + "/" + getYLength() + "/");
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
					String numStr = null;
					if (num == -1) {
						numStr = ".";
					} else {
						Integer numP1 = null;
						if (i + 1 < getYLength() * getXLength()) {
							int yIndexP1 = (i + 1) / getXLength();
							int xIndexP1 = (i + 1) % getXLength();
							numP1 = numbers[yIndexP1][xIndexP1];
						}
						Integer numP2 = null;
						if (numP1 == null && i + 2 < getYLength() * getXLength()) {
							int yIndexP2 = (i + 2) / getXLength();
							int xIndexP2 = (i + 2) % getXLength();
							numP2 = numbers[yIndexP2][xIndexP2];
						}
						if (numP1 == null && numP2 == null) {
							if (num == 0) {
								numStr = "a";
							} else if (num == 1) {
								numStr = "b";
							} else if (num == 2) {
								numStr = "c";
							} else if (num == 3) {
								numStr = "d";
							} else if (num == 4) {
								numStr = "e";
							}
							i++;
							i++;
						} else if (numP1 == null) {
							if (num == 0) {
								numStr = "5";
							} else if (num == 1) {
								numStr = "6";
							} else if (num == 2) {
								numStr = "7";
							} else if (num == 3) {
								numStr = "8";
							} else if (num == 4) {
								numStr = "9";
							}
							i++;
						} else {
							numStr = String.valueOf(num);
						}
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
			int kuroCnt = 0;
			int numberCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						kuroCnt++;
						if (numbers[yIndex][xIndex] != -1) {
							numberCnt++;
						}
					}
				}
			}
			return String.valueOf(numberCnt + "/" + kuroCnt);
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		/**
		 * プレーン盤面生成。seedは壁の発生率の逆数。(5だと1/5で壁発生)
		 */
		public Field(int height, int width, HintPattern hintPattern, int seed) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			List<Set<Position>> posSetList = hintPattern.getPosSetList();
			for (Set<Position> posSet : posSetList) {
				if (Math.random() * seed < 1) {
					for (Position pos : posSet) {
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					}
				}
			}
			// 1マスだけの閉空間ができないようにする。
			// 1マスだけの閉空間は照明を置くしかなく、解き味が下がるので。
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						boolean numUp = yIndex == 0 || numbers[yIndex - 1][xIndex] != null;
						boolean numRight = xIndex == getXLength() - 1 || numbers[yIndex][xIndex + 1] != null;
						boolean numDown = yIndex == getYLength() - 1 || numbers[yIndex + 1][xIndex] != null;
						boolean numLeft = xIndex == 0 || numbers[yIndex][xIndex - 1] != null;
						if (numUp && numRight && numDown && numLeft) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
							numbers[yIndex][xIndex] = -1;
						}
					}
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
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == '.') {
					numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							numbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
						}
						index++;
					}
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
			numbers = other.numbers;
		}

		/**
		 * numbersをイミュータブルにするためのコンストラクタ。flagはダミー
		 */
		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			numbers = new Integer[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
				}
			}
		}

		/**
		 * 美術館バトル用
		 */
		public Field(String fieldStr, int height, int width) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			for (int i = 0; i < fieldStr.length(); i++) {
				char fieldChar = fieldStr.charAt(i);
				int yIndex = i / width;
				int xIndex = i % width;
				if (fieldChar == 0) {
					// 未確定マス
					masu[yIndex][xIndex] = Masu.SPACE;
					numbers[yIndex][xIndex] = null;
				} else if (fieldChar == 1) {
					// 明かりじゃないことが確定したマス(再度チェックするので未確定と同じ扱い)
					masu[yIndex][xIndex] = Masu.SPACE;
					numbers[yIndex][xIndex] = null;
				} else if (fieldChar == 2) {
					// 明かりマス(再度チェックするので未確定と同じ扱い)
					masu[yIndex][xIndex] = Masu.SPACE;
					numbers[yIndex][xIndex] = null;
				} else if (fieldChar == 3) {
					// 数字なし黒マス
					masu[yIndex][xIndex] = Masu.SPACE;
					numbers[yIndex][xIndex] = -1;
				} else if (fieldChar == 4) {
					// 数字0
					masu[yIndex][xIndex] = Masu.SPACE;
					numbers[yIndex][xIndex] = 0;
				} else if (fieldChar == 5) {
					// 数字1
					masu[yIndex][xIndex] = Masu.SPACE;
					numbers[yIndex][xIndex] = 1;
				} else if (fieldChar == 6) {
					// 数字2
					masu[yIndex][xIndex] = Masu.SPACE;
					numbers[yIndex][xIndex] = 2;
				} else if (fieldChar == 7) {
					// 数字3
					masu[yIndex][xIndex] = Masu.SPACE;
					numbers[yIndex][xIndex] = 3;
				} else if (fieldChar == 8) {
					// 数字4
					masu[yIndex][xIndex] = Masu.SPACE;
					numbers[yIndex][xIndex] = 4;
				}
			}
		}

		/**
		 * 美術館バトル用
		 */
		public String getFieldStr() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						sb.append(numbers[yIndex][xIndex] + 5);
					} else {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							sb.append("0");
						} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							sb.append("1");
						} else if (masu[yIndex][xIndex] == Masu.BLACK) {
							sb.append("2");
						}
					}
				}
			}
			return sb.toString();
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] == -1) {
							sb.append("■");
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
						sb.append(masu[yIndex][xIndex] == Masu.BLACK ? "○" : masu[yIndex][xIndex]);
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
		 * 数字指定のあるマスのうち、確定する箇所を照明にする。
		 * 照明が過剰・不足の場合はfalseを返す。
		 */
		public boolean akariSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu masuUp = yIndex == 0 || numbers[yIndex - 1][xIndex] != null ? Masu.NOT_BLACK
								: masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 || numbers[yIndex][xIndex + 1] != null
								? Masu.NOT_BLACK
								: masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 || numbers[yIndex + 1][xIndex] != null
								? Masu.NOT_BLACK
								: masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 || numbers[yIndex][xIndex - 1] != null ? Masu.NOT_BLACK
								: masu[yIndex][xIndex - 1];
						if (masuUp == Masu.BLACK) {
							blackCnt++;
						} else if (masuUp == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuRight == Masu.BLACK) {
							blackCnt++;
						} else if (masuRight == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuDown == Masu.BLACK) {
							blackCnt++;
						} else if (masuDown == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuLeft == Masu.BLACK) {
							blackCnt++;
						} else if (masuLeft == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (numbers[yIndex][xIndex] < blackCnt) {
							// 明かり過剰
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
						if (numbers[yIndex][xIndex] > 4 - whiteCnt) {
							// 明かり不足
							return false;
						}
						if (numbers[yIndex][xIndex] == 4 - whiteCnt) {
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
					}
				}
			}
			return true;
		}

		/**
		 * 照明が照らしているマスを白マス確定にする。
		 * 照明が照らしているマスに既に照明があったらfalseを返す。
		 */
		public boolean lightSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						int targetY = yIndex - 1;
						while (targetY >= 0 && numbers[targetY][xIndex] == null) {
							if (masu[targetY][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[targetY][xIndex] = Masu.NOT_BLACK;
							targetY--;
						}
						targetY = yIndex + 1;
						while (targetY < getYLength() && numbers[targetY][xIndex] == null) {
							if (masu[targetY][xIndex] == Masu.BLACK) {
								return false;
							}
							masu[targetY][xIndex] = Masu.NOT_BLACK;
							targetY++;
						}
						int targetX = xIndex - 1;
						while (targetX >= 0 && numbers[yIndex][targetX] == null) {
							if (masu[yIndex][targetX] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][targetX] = Masu.NOT_BLACK;
							targetX--;
						}
						targetX = xIndex + 1;
						while (targetX < getXLength() && numbers[yIndex][targetX] == null) {
							if (masu[yIndex][targetX] == Masu.BLACK) {
								return false;
							}
							masu[yIndex][targetX] = Masu.NOT_BLACK;
							targetX++;
						}
					}
				}
			}
			return true;
		}

		/**
		 * どこからも光が当たる可能性がないマスを照明にする。
		 * どこからも光が当たる可能性がないマスが白マス確定マスだった場合falseを返す。
		 */
		public boolean shadowSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK || masu[yIndex][xIndex] == Masu.SPACE) {
						if (numbers[yIndex][xIndex] != null) {
							continue;
						}
						boolean isOk = false;
						int targetY = yIndex - 1;
						while (targetY >= 0 && numbers[targetY][xIndex] == null) {
							if (masu[targetY][xIndex] != Masu.NOT_BLACK) {
								isOk = true;
								break;
							}
							targetY--;
						}
						if (isOk) {
							continue;
						}
						targetY = yIndex + 1;
						while (targetY < getYLength() && numbers[targetY][xIndex] == null) {
							if (masu[targetY][xIndex] != Masu.NOT_BLACK) {
								isOk = true;
								break;
							}
							targetY++;
						}
						if (isOk) {
							continue;
						}
						int targetX = xIndex - 1;
						while (targetX >= 0 && numbers[yIndex][targetX] == null) {
							if (masu[yIndex][targetX] != Masu.NOT_BLACK) {
								isOk = true;
								break;
							}
							targetX--;
						}
						if (isOk) {
							continue;
						}
						targetX = xIndex + 1;
						while (targetX < getXLength() && numbers[yIndex][targetX] == null) {
							if (masu[yIndex][targetX] != Masu.NOT_BLACK) {
								isOk = true;
								break;
							}
							targetX++;
						}
						if (isOk) {
							continue;
						}
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							return false;
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!akariSolve()) {
				return false;
			}
			if (!lightSolve()) {
				return false;
			}
			if (!shadowSolve()) {
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
					if (masu[yIndex][xIndex] == Masu.SPACE && numbers[yIndex][xIndex] == null) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public AkariSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	/**
	 * 美術館バトル用
	 */
	public AkariSolver(String fieldStr, int height, int width) {
		field = new Field(fieldStr, height, width);
	}

	public AkariSolver(Field field) {
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
		System.out.println(new AkariSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 15));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 15 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 15).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE && field.numbers[yIndex][xIndex] == null) {
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
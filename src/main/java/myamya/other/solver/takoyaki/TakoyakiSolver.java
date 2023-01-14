package myamya.other.solver.takoyaki;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.HintPattern;
import myamya.other.solver.Solver;

public class TakoyakiSolver implements Solver {

//	public static class TakoyakiGenerator implements Generator {
//		static class TakoyakiSolverForGenerator extends TakoyakiSolver {
//
//			private final int limit;
//
//			public TakoyakiSolverForGenerator(Field field, int limit) {
//				super(field);
//				this.limit = limit;
//			}
//
//			public int solve2() {
//				while (!field.isSolved()) {
//					String befStr = field.getStateDump();
//					if (!field.solveAndCheck()) {
//						return -1;
//					}
//					int recursiveCnt = 0;
//					while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
//						if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
//							return -1;
//						}
//						recursiveCnt++;
//					}
//					if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
//						return -1;
//					}
//				}
//				return count;
//			}
//
//			@Override
//			protected boolean candSolve(Field field, int recursive) {
//				if (this.count >= limit) {
//					return false;
//				} else {
//					return super.candSolve(field, recursive);
//				}
//			}
//		}
//
//		/**
//		 * 黒マス発生率。逆数なので多いほど少ない。いったん1/8で
//		 */
//		private static int BLACK_RATE = 8;
//
//		private final int height;
//		private final int width;
//		private final HintPattern hintPattern;
//
//		public TakoyakiGenerator(int height, int width, HintPattern hintPattern) {
//			this.height = height;
//			this.width = width;
//			this.hintPattern = hintPattern;
//		}
//
//		public static void main(String[] args) {
//			new TakoyakiGenerator(5, 5, HintPattern.getByVal(7, 5, 5)).generate();
//		}
//
//		private static final String FULL_NUMS = "０１２３４５６７８９";
//
//		@Override
//		public GeneratorResult generate() {
//			TakoyakiSolver.Field wkField = new TakoyakiSolver.Field(height, width, hintPattern, BLACK_RATE);
//			List<Integer> indexList = new ArrayList<>();
//			for (int i = 0; i < height * width; i++) {
//				indexList.add(i);
//			}
//			Collections.shuffle(indexList);
//			int index = 0;
//			int level = 0;
//			long start = System.nanoTime();
//			while (true) {
//				while (!wkField.isSolved()) {
//					int yIndex = indexList.get(index) / width;
//					int xIndex = indexList.get(index) % width;
//					if (wkField.masu[yIndex][xIndex] == Masu.SPACE) {
//						boolean isOk = false;
//						List<Integer> numIdxList = new ArrayList<>();
//						for (int i = 0; i < 2; i++) {
//							numIdxList.add(i);
//						}
//						Collections.shuffle(numIdxList);
//						for (int masuNum : numIdxList) {
//							TakoyakiSolver.Field virtual = new TakoyakiSolver.Field(wkField);
//							if (masuNum < 1) {
//								virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK;
//							} else if (masuNum < 2) {
//								virtual.masu[yIndex][xIndex] = Masu.BLACK;
//							}
//							if (virtual.solveAndCheck()) {
//								isOk = true;
//								wkField.masu = virtual.masu;
//								wkField.numbers = virtual.numbers;
//								break;
//							}
//						}
//						if (!isOk) {
//							// 破綻したら0から作り直す。
//							wkField = new TakoyakiSolver.Field(height, width, hintPattern, BLACK_RATE);
//							index = 0;
//							continue;
//						}
//					}
//					index++;
//				}
//				// 数字埋め＆マス初期化
//				// まず数字を埋める
//				List<Position> numberPosList = new ArrayList<>();
//				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//						if (wkField.numbers[yIndex][xIndex] != null) {
//							int blackCnt = 0;
//							Masu masuUp = yIndex == 0 || wkField.numbers[yIndex - 1][xIndex] != null ? Masu.NOT_BLACK
//									: wkField.masu[yIndex - 1][xIndex];
//							Masu masuRight = xIndex == wkField.getXLength() - 1
//									|| wkField.numbers[yIndex][xIndex + 1] != null ? Masu.NOT_BLACK
//											: wkField.masu[yIndex][xIndex + 1];
//							Masu masuDown = yIndex == wkField.getYLength() - 1
//									|| wkField.numbers[yIndex + 1][xIndex] != null ? Masu.NOT_BLACK
//											: wkField.masu[yIndex + 1][xIndex];
//							Masu masuLeft = xIndex == 0 || wkField.numbers[yIndex][xIndex - 1] != null ? Masu.NOT_BLACK
//									: wkField.masu[yIndex][xIndex - 1];
//							if (masuUp == Masu.BLACK) {
//								blackCnt++;
//							}
//							if (masuRight == Masu.BLACK) {
//								blackCnt++;
//							}
//							if (masuDown == Masu.BLACK) {
//								blackCnt++;
//							}
//							if (masuLeft == Masu.BLACK) {
//								blackCnt++;
//							}
//							wkField.numbers[yIndex][xIndex] = blackCnt;
//							numberPosList.add(new Position(yIndex, xIndex));
//						}
//					}
//				}
//				// マスを戻す
//				boolean isOk = false;
//				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//						if (wkField.numbers[yIndex][xIndex] == null) {
//							isOk = true;
//							wkField.masu[yIndex][xIndex] = Masu.SPACE;
//						}
//					}
//				}
//				// 解けるかな？
//				level = new TakoyakiSolverForGenerator(wkField, 200).solve2();
//				if (!isOk || level == -1) {
//					// 解けなければやり直し
//					wkField = new TakoyakiSolver.Field(height, width, hintPattern, BLACK_RATE);
//					index = 0;
//				} else {
//					// ヒントを限界まで減らす
//					Collections.shuffle(numberPosList);
//					for (Position numberPos : numberPosList) {
//						TakoyakiSolver.Field virtual = new TakoyakiSolver.Field(wkField);
//						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = -1;
//						int solveResult = new TakoyakiSolverForGenerator(virtual, 1200).solve2();
//						if (solveResult != -1) {
//							wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = -1;
//							level = solveResult;
//						}
//					}
//					break;
//				}
//			}
//			level = (int) Math.sqrt(level * 15 / 3) + 1;
//			String status = "Lv:" + level + "の問題を獲得！(数字/黒：" + wkField.getHintCount().split("/")[0] + "/"
//					+ wkField.getHintCount().split("/")[1] + ")";
//			String url = wkField.getPuzPreURL();
//			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
//			StringBuilder sb = new StringBuilder();
//			int baseSize = 20;
//			int margin = 5;
//			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
//					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
//					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//					if (wkField.getNumbers()[yIndex][xIndex] != null) {
//						sb.append(
//								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
//										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
//						if (wkField.getNumbers()[yIndex][xIndex] != -1) {
//							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 5 + margin) + "\" x=\""
//									+ (xIndex * baseSize + baseSize + 2) + "\" fill=\"" + "white" + "\" font-size=\""
//									+ (baseSize - 5) + "\" textLength=\"" + (baseSize - 5)
//									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
//									+ FULL_NUMS.substring(wkField.getNumbers()[yIndex][xIndex],
//											wkField.getNumbers()[yIndex][xIndex] + 1)
//									+ "</text>");
//						}
//					}
//				}
//			}
//			// 横壁描画
//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
//					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
//					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
//							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
//							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
//					if (oneYokoWall) {
//						sb.append("stroke=\"#000\" ");
//					} else {
//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
//					}
//					sb.append(">" + "</line>");
//				}
//			}
//			// 縦壁描画
//			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
//					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
//							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
//							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
//							+ "\" stroke-width=\"1\" fill=\"none\"");
//					if (oneTateWall) {
//						sb.append("stroke=\"#000\" ");
//					} else {
//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
//					}
//					sb.append(">" + "</line>");
//				}
//			}
//			sb.append("</svg>");
//			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
//			System.out.println(level);
//			System.out.println(wkField.getHintCount());
//			System.out.println(wkField);
//			return new GeneratorResult(status, sb.toString(), link, url, level, "");
//
//		}
//
//	}

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected Wall[][] tateWall;
		// マスの候補情報。blackならたこ焼きあり
		protected Masu[][] masu;
		// 数字の情報
		protected Integer[][] numbers;

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?takoyaki/" + getXLength() + "/" + getYLength() + "/");
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
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		/**
		 * プレーン盤面生成。seedは壁の発生率の逆数。(5だと1/5で壁発生)
		 */
		public Field(int height, int width, HintPattern hintPattern, int seed) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
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
			List<Set<Position>> posSetList = hintPattern.getPosSetList();
			for (Set<Position> posSet : posSetList) {
				if (Math.random() * seed < 1) {
					for (Position pos : posSet) {
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
						// 周囲の壁を閉鎖
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
						}
					}
				}
			}

		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
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
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == '.') {
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					// 周囲の壁を閉鎖
					if (pos.getyIndex() != 0) {
						tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
					}
					if (pos.getxIndex() != getXLength() - 1) {
						yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
					}
					if (pos.getyIndex() != getYLength() - 1) {
						tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
					}
					if (pos.getxIndex() != 0) {
						yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
					}
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							numbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
						}
						// 周囲の壁を閉鎖
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
						}
						index++;
					}
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			numbers = new Integer[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
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
						if (numbers[yIndex][xIndex] == -1) {
							sb.append("□");
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
		 * 数字指定のあるマスのうち、確定する箇所をたこ焼きにする。 たこ焼きが過剰・不足の場合はfalseを返す。
		 */
		public boolean numberSolve() {
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
		 * たこ焼き隣接セルを白マスにする。 たこ焼き隣接セルがたこ焼きの場合falseを返す。
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
		 * たこ焼きになりうるセルの壁の数は2か3、ならないセルの壁の数は2。違反の場合false。
		 */
		public boolean takoyakiSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						int exists = 0;
						int notExists = 0;
						Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
						if (wallUp == Wall.EXISTS) {
							exists++;
						} else if (wallUp == Wall.NOT_EXISTS) {
							notExists++;
						}
						Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
						if (wallRight == Wall.EXISTS) {
							exists++;
						} else if (wallRight == Wall.NOT_EXISTS) {
							notExists++;
						}
						Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
						if (wallDown == Wall.EXISTS) {
							exists++;
						} else if (wallDown == Wall.NOT_EXISTS) {
							notExists++;
						}
						Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
						if (wallLeft == Wall.EXISTS) {
							exists++;
						} else if (wallLeft == Wall.NOT_EXISTS) {
							notExists++;
						}
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							if (exists > 2 || notExists > 2) {
								return false;
							}
							if (exists == 2) {
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
							} else if (notExists == 2) {
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
						} else {
							if (exists > 3 || notExists > 2) {
								return false;
							}
							if (exists == 3) {
								masu[yIndex][xIndex] = Masu.BLACK;
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
							} else if (notExists == 2) {
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
						}
					}
				}
			}
			return true;
		}

		/**
		 * 1つの閉じた領域にたこ焼きは必ず3個あり、さらに同じ行・列にたこ焼きはない。違反の場合false。
		 */
		public boolean connectSolve() {
			Set<Position> checkedPosSet = new HashSet<>();
			Set<Position> checkedCandPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						Position pivot = new Position(yIndex, xIndex);
						if (!checkedPosSet.contains(pivot)) {
							Set<Position> continuePosSet = new HashSet<>();
							Set<Position> takoyakiPosSet = new HashSet<>();
							continuePosSet.add(pivot);
							if (!checkAndSetContinueWhitePosSet(pivot, continuePosSet, null, takoyakiPosSet)) {
								return false;
							}
							checkedPosSet.addAll(continuePosSet);
						}
						if (!checkedCandPosSet.contains(pivot)) {
							Set<Position> continueCandPosSet = new HashSet<>();
							Set<Position> takoyakiCandPosSet = new HashSet<>();
							continueCandPosSet.add(pivot);
							if (!checkAndSetContinuePosSet(pivot, continueCandPosSet, null, takoyakiCandPosSet)) {
								return false;
							}
							checkedCandPosSet.addAll(continueCandPosSet);
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁なし確定マスをつなぎ、 たこ焼きの数が4個以上または同じ行・列になると分かった時点でfalseを返す。
		 */
		private boolean checkAndSetContinueWhitePosSet(Position pos, Set<Position> continuePosSet, Direction from,
				Set<Position> takoyakiPosSet) {
			if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
				for (Position takoyakiPos : takoyakiPosSet) {
					if (takoyakiPos.getxIndex() == pos.getxIndex() || takoyakiPos.getyIndex() == pos.getyIndex()) {
						return false;
					}
				}
				takoyakiPosSet.add(pos);
				if (takoyakiPosSet.size() > 3) {
					return false;
				}
			} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
				for (Position takoyakiPos : takoyakiPosSet) {
					if (takoyakiPos.getxIndex() == pos.getxIndex() || takoyakiPos.getyIndex() == pos.getyIndex()) {
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						break;
					}
				}
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.DOWN, takoyakiPosSet)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.LEFT, takoyakiPosSet)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.UP, takoyakiPosSet)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.RIGHT, takoyakiPosSet)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスをつなぎ、 たこ焼きの数がまだ3個以上置けると分かった時点でtrueを返す。
		 */
		private boolean checkAndSetContinuePosSet(Position pos, Set<Position> continueCandPosSet, Direction from,
				Set<Position> takoyakiCandPosSet) {
			if (masu[pos.getyIndex()][pos.getxIndex()] != Masu.NOT_BLACK) {
				takoyakiCandPosSet.add(pos);
			}
			if (takoyakiCandPosSet.size() > 2) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continueCandPosSet.contains(nextPos)) {
					continueCandPosSet.add(nextPos);
					if (checkAndSetContinuePosSet(nextPos, continueCandPosSet, Direction.DOWN, takoyakiCandPosSet)) {
						return true;
					}

				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continueCandPosSet.contains(nextPos)) {
					continueCandPosSet.add(nextPos);
					if (checkAndSetContinuePosSet(nextPos, continueCandPosSet, Direction.LEFT, takoyakiCandPosSet)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS
						&& !continueCandPosSet.contains(nextPos)) {
					continueCandPosSet.add(nextPos);
					if (checkAndSetContinuePosSet(nextPos, continueCandPosSet, Direction.UP, takoyakiCandPosSet)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continueCandPosSet.contains(nextPos)) {
					continueCandPosSet.add(nextPos);
					if (checkAndSetContinuePosSet(nextPos, continueCandPosSet, Direction.RIGHT, takoyakiCandPosSet)) {
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
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!takoyakiSolve()) {
				return false;
			}
			if (!connectSolve()) {
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

	protected final Field field;
	protected int count = 0;

	public TakoyakiSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public TakoyakiSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?takoyaki/9/9/.jcm.k6..gb.i.o7.g.m...ibrcgc"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new TakoyakiSolver(height, width, param).solve());
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
		return "解けました。推定難易度:" + Difficulty.getByCount(count).toString() + "(Lv:" + level + ")";
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
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
				if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive - 1)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandTateWallSolve(field, yIndex, xIndex, recursive - 1)) {
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
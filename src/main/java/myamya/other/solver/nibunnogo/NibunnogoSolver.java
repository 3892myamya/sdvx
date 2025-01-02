package myamya.other.solver.nibunnogo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.PenpaEditLib;
import myamya.other.solver.Solver;
import myamya.other.solver.akari.Main.Difficulty;

public class NibunnogoSolver implements Solver {

	public static class NibunnogoGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class NibunnogoSolverForGenerator extends NibunnogoSolver {
			private final int limit;

			public NibunnogoSolverForGenerator(Field field, int limit) {
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
							if (!candSolve(field, recursiveCnt)) {
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

		public NibunnogoGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new NibunnogoGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			NibunnogoSolver.Field wkField = new NibunnogoSolver.Field(height, width);
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
							NibunnogoSolver.Field virtual = new NibunnogoSolver.Field(wkField, true);
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
							wkField = new NibunnogoSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// まず数字を埋める
				for (int yIndex = 0; yIndex < wkField.getYLength() + 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() + 1; xIndex++) {
						int blackCnt = 0;
						Masu masuUpRight = yIndex == 0 || xIndex == wkField.getXLength() ? Masu.NOT_BLACK
								: wkField.masu[yIndex - 1][xIndex];
						if (masuUpRight == Masu.BLACK) {
							blackCnt++;
						}
						Masu masuRightDown = xIndex == wkField.getXLength() || yIndex == wkField.getYLength()
								? Masu.NOT_BLACK
								: wkField.masu[yIndex][xIndex];
						if (masuRightDown == Masu.BLACK) {
							blackCnt++;
						}
						Masu masuDownLeft = yIndex == wkField.getYLength() || xIndex == 0 ? Masu.NOT_BLACK
								: wkField.masu[yIndex][xIndex - 1];
						if (masuDownLeft == Masu.BLACK) {
							blackCnt++;
						}
						Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK
								: wkField.masu[yIndex - 1][xIndex - 1];
						if (masuLeftUp == Masu.BLACK) {
							blackCnt++;
						}
						wkField.extraNumbers[yIndex][xIndex] = blackCnt;
					}
				}
//				System.out.println(wkField);
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new NibunnogoSolverForGenerator(wkField, 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new NibunnogoSolver.Field(height, width);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					List<Set<Position>> numberPosSetList = new ArrayList<>();
					for (int i = 0; i < (wkField.getYLength() + 1) * (wkField.getXLength() + 1); i++) {
						Set<Position> set = new HashSet<>();
						int yIndex = i / (wkField.getXLength() + 1);
						int xIndex = i % (wkField.getXLength() + 1);
						set.add(new Position(yIndex, xIndex));
						numberPosSetList.add(set);
					}
					Collections.shuffle(numberPosSetList);
					for (Set<Position> numberPosSet : numberPosSetList) {
						NibunnogoSolver.Field virtual = new NibunnogoSolver.Field(wkField, true);
						for (Position numberPos : numberPosSet) {
							virtual.extraNumbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						}
						int solveResult = new NibunnogoSolverForGenerator(virtual, 10000).solve2();
						if (solveResult != -1) {
							for (Position numberPos : numberPosSet) {
								wkField.extraNumbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			// ヒント数字を含む盤面変換
//			System.out.println(fieldStr);
//			System.out.println(solutionStr);

			level = (int) Math.sqrt(level / 3) + 1;
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
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize / 2 + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize / 2 + 2 * baseSize) + "\" y2=\""
							+ (yIndex * baseSize + baseSize / 2 + baseSize + margin) + "\" x2=\""
							+ (xIndex * baseSize + baseSize / 2 + 2 * baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					sb.append("stroke=\"#000\" ");
					sb.append(">" + "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + baseSize / 2 + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize + baseSize / 2) + "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize / 2 + margin) + "\" x2=\""
							+ (xIndex * baseSize + baseSize + baseSize + baseSize / 2)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					sb.append("stroke=\"#000\" ");
					sb.append(">" + "</line>");
				}
			}
			// 数字描画
			for (int yIndex = 0; yIndex < wkField.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength() + 1; xIndex++) {
					Integer number = wkField.getExtraNumbers()[yIndex][xIndex];
					if (number != null) {
						String numberStr = String.valueOf(number);
						int numIdx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (numIdx >= 0) {
							masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 3)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 5) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 3) + "\" font-size=\"" + (baseSize - 6)
								+ "\" textLength=\"" + (baseSize - 6) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
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
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 数字の情報。外壁も考慮するため注意
		private final Integer[][] extraNumbers;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://pzprxs.vercel.app/p.html?nibunnogo/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < (getYLength() + 1) * (getXLength() + 1); i++) {
				int yIndex = i / (getXLength() + 1);
				int xIndex = i % (getXLength() + 1);
				if (extraNumbers[yIndex][xIndex] == null) {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = extraNumbers[yIndex][xIndex];
					String numStr = null;
					if (num == -1) {
						numStr = ".";
					} else {
						Integer numP1 = null;
						if (i + 1 < (getYLength() + 1) * (getXLength() + 1)) {
							int yIndexP1 = (i + 1) / (getXLength() + 1);
							int xIndexP1 = (i + 1) % (getXLength() + 1);
							numP1 = extraNumbers[yIndexP1][xIndexP1];
						}
						Integer numP2 = null;
						if (numP1 == null && i + 2 < (getYLength() + 1) * (getXLength() + 1)) {
							int yIndexP2 = (i + 2) / (getXLength() + 1);
							int xIndexP2 = (i + 2) % (getXLength() + 1);
							numP2 = extraNumbers[yIndexP2][xIndexP2];
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
			int numberCnt = 0;
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (extraNumbers[yIndex][xIndex] != null) {
						numberCnt++;
					}
				}
			}
			return String.valueOf(numberCnt);
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Integer[][] getExtraNumbers() {
			return extraNumbers;
		}

		/**
		 * プレーンなフィールド作成
		 */
		public Field(int height, int width) {
			masu = new Masu[height][width];
			extraNumbers = new Integer[height + 1][width + 1];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			extraNumbers = new Integer[height + 1][width + 1];
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / (getXLength() + 1), index % (getXLength() + 1));
				if (ch == '.') {
					extraNumbers[pos.getyIndex()][pos.getxIndex()] = -1;
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							extraNumbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							extraNumbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							extraNumbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
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
			extraNumbers = other.extraNumbers;
		}

		/**
		 * numbersをイミュータブルにするためのコンストラクタ。flagはダミー
		 */
		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			extraNumbers = new Integer[other.getYLength() + 1][other.getXLength() + 1];
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					extraNumbers[yIndex][xIndex] = other.extraNumbers[yIndex][xIndex];
				}
			}
		}

		public Field(String fieldStr) {
			masu = PenpaEditLib.getMasu(fieldStr);
			extraNumbers = PenpaEditLib.getExtraNumbers(fieldStr);
		}

		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (extraNumbers[yIndex][xIndex] == null) {
						sb.append("□");
					} else if (extraNumbers[yIndex][xIndex] == -1) {
						sb.append("○");
					} else {
						sb.append(FULL_NUMS.substring(extraNumbers[yIndex][xIndex], extraNumbers[yIndex][xIndex] + 1));
					}
					if (xIndex != getXLength()) {
						sb.append("□");
					}
				}
				sb.append(System.lineSeparator());
				if (yIndex != getYLength()) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(masu[yIndex][xIndex]);
						sb.append("□");
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
		 * 数字指定のあるマスの周囲の黒マス個数を数え、確定する箇所は埋める。 矛盾したらfalseを返す。
		 */
		public boolean aroundSolve() {
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (extraNumbers[yIndex][xIndex] != null && extraNumbers[yIndex][xIndex] != -1) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu masuUpRight = yIndex == 0 || xIndex == getXLength() ? Masu.NOT_BLACK
								: masu[yIndex - 1][xIndex];
						if (masuUpRight == Masu.BLACK) {
							blackCnt++;
						} else if (masuUpRight == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						Masu masuRightDown = xIndex == getXLength() || yIndex == getYLength() ? Masu.NOT_BLACK
								: masu[yIndex][xIndex];
						if (masuRightDown == Masu.BLACK) {
							blackCnt++;
						} else if (masuRightDown == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						Masu masuDownLeft = yIndex == getYLength() || xIndex == 0 ? Masu.NOT_BLACK
								: masu[yIndex][xIndex - 1];
						if (masuDownLeft == Masu.BLACK) {
							blackCnt++;
						} else if (masuDownLeft == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex - 1];
						if (masuLeftUp == Masu.BLACK) {
							blackCnt++;
						} else if (masuLeftUp == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (extraNumbers[yIndex][xIndex] < blackCnt) {
							// 黒マス過剰
							return false;
						}
						if (extraNumbers[yIndex][xIndex] > 4 - whiteCnt) {
							// 黒マス不足
							return false;
						}
						if (extraNumbers[yIndex][xIndex] == blackCnt) {
							if (masuUpRight == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masuRightDown == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
							if (masuDownLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
							if (masuLeftUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
						if (extraNumbers[yIndex][xIndex] == 4 - whiteCnt) {
							if (masuUpRight == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.BLACK;
							}
							if (masuRightDown == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
							if (masuDownLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.BLACK;
							}
							if (masuLeftUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex - 1] = Masu.BLACK;
							}
						}

					}
				}
			}
			return true;
		}

		/**
		 * 白マスが6個以上つながったらfalseを返す
		 */
		public boolean connectSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						Set<Position> continueWhitePosSet = new HashSet<>();
						continueWhitePosSet.add(whitePos);
						if (!checkAndSetContinueWhitePosSet(whitePos, continueWhitePosSet, null, Masu.NOT_BLACK)) {
							// サイズ超過
							return false;
						}
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						Set<Position> continueWhitePosSet = new HashSet<>();
						continueWhitePosSet.add(whitePos);
						if (!checkAndSetContinueWhitePosSet(whitePos, continueWhitePosSet, null, Masu.BLACK)) {
							// サイズ超過
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に同じcolorのマスをつなぎ、 6以上になると分かった時点でfalseを返す。
		 */
		private boolean checkAndSetContinueWhitePosSet(Position pos, Set<Position> continuePosSet, Direction from,
				Masu color) {
			if (continuePosSet.size() >= 6) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == color) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.DOWN, color)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == color) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.LEFT, color)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == color) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.UP, color)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == color) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.RIGHT, color)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * フィールドに1つは黒マスが必要。
		 */
		private boolean finalSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] != Masu.NOT_BLACK) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 * 
		 * @param recursive
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!aroundSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
				if (!finalSolve()) {
					return false;
				}
			}
			return true;
		}
	}

	protected final Field field;
	protected int count;

	public NibunnogoSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public NibunnogoSolver(Field field) {
		this.field = new Field(field);
	}

	// penpa-edit向けコンストラクタ
	public NibunnogoSolver(String fieldStr) {
		field = new Field(fieldStr);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		// http://pzv.jp/p.html?nibunnogo/8/8/haaoaicidjcibmehbgd3dhbbgag
		// http://pzv.jp/p.html?nibunnogo/10/10/hcbbqcg9cgcgbgdidiaambcidiagdgag7dgcqaba
		String url = "http://pzv.jp/p.html?nibunnogo/8/8/5aq9abnblcpah8dibnc"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new NibunnogoSolver(height, width, param).solve());
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
				if (!candSolve(field, recursiveCnt)) {
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
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 2).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					// 周囲に空白が少ない＆ヒントが多い個所を優先して調査
					Masu masuUp = yIndex == 0 ? Masu.BLACK : field.masu[yIndex - 1][xIndex];
					Masu masuRight = xIndex == field.getXLength() - 1 ? Masu.BLACK : field.masu[yIndex][xIndex + 1];
					Masu masuDown = yIndex == field.getYLength() - 1 ? Masu.BLACK : field.masu[yIndex + 1][xIndex];
					Masu masuLeft = xIndex == 0 ? Masu.BLACK : field.masu[yIndex][xIndex - 1];
					int whiteCnt = 0;
					if (masuUp == Masu.SPACE) {
						whiteCnt++;
					}
					if (masuRight == Masu.SPACE) {
						whiteCnt++;
					}
					if (masuDown == Masu.SPACE) {
						whiteCnt++;
					}
					if (masuLeft == Masu.SPACE) {
						whiteCnt++;
					}
					Integer numberUpRight = field.extraNumbers[yIndex][xIndex + 1];
					Integer numberRightDown = field.extraNumbers[yIndex + 1][xIndex + 1];
					Integer numberDownLeft = field.extraNumbers[yIndex + 1][xIndex];
					Integer numberLeftUp = field.extraNumbers[yIndex][xIndex];
					int noHintCnt = 0;
					if (numberUpRight == null || numberUpRight == -1) {
						noHintCnt++;
					}
					if (numberRightDown == null || numberRightDown == -1) {
						noHintCnt++;
					}
					if (numberDownLeft == null || numberDownLeft == -1) {
						noHintCnt++;
					}
					if (numberLeftUp == null || numberLeftUp == -1) {
						noHintCnt++;
					}
					if (noHintCnt + whiteCnt > 6) {
						// 4だと解けない問題が出ることがある。5と6はかなり微妙で問題により速度差が出る。
						continue;
					}
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
		} else if (!allowNotBlack) {
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

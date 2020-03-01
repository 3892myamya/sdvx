package myamya.other.solver.gokigen;

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
import myamya.other.solver.Generator;
import myamya.other.solver.HintPattern;
import myamya.other.solver.Solver;

public class GokigenSolver implements Solver {

	public static class GokigenGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class GokigenSolverForGenerator extends GokigenSolver {
			private final int limit;

			public GokigenSolverForGenerator(Field field, int limit) {
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
		private final HintPattern hintPattern;

		public GokigenGenerator(int height, int width, HintPattern hintPattern) {
			this.height = height;
			this.width = width;
			this.hintPattern = hintPattern;
		}

		public static void main(String[] args) {
			new GokigenGenerator(4, 4, HintPattern.getByVal(7, 5, 5)).generate();
		}

		@Override
		public GeneratorResult generate() {
			GokigenSolver.Field wkField = new GokigenSolver.Field(height, width);
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
							GokigenSolver.Field virtual = new GokigenSolver.Field(wkField, true);
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
							wkField = new GokigenSolver.Field(height, width);
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
						int yesCnt = 0;
						Masu masuUpRight = yIndex == 0 || xIndex == wkField.getXLength() ? Masu.BLACK
								: wkField.masu[yIndex - 1][xIndex];
						if (masuUpRight == Masu.NOT_BLACK) {
							yesCnt++;
						}
						Masu masuRightDown = xIndex == wkField.getXLength() || yIndex == wkField.getYLength()
								? Masu.NOT_BLACK
								: wkField.masu[yIndex][xIndex];
						if (masuRightDown == Masu.BLACK) {
							yesCnt++;
						}
						Masu masuDownLeft = yIndex == wkField.getYLength() || xIndex == 0 ? Masu.BLACK
								: wkField.masu[yIndex][xIndex - 1];
						if (masuDownLeft == Masu.NOT_BLACK) {
							yesCnt++;
						}
						Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK
								: wkField.masu[yIndex - 1][xIndex - 1];
						if (masuLeftUp == Masu.BLACK) {
							yesCnt++;
						}
						wkField.extraNumbers[yIndex][xIndex] = yesCnt;
					}
				}
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}

				// 解けるかな？
				level = new GokigenSolverForGenerator(wkField, 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new GokigenSolver.Field(height, width);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					List<Set<Position>> numberPosSetList = hintPattern.getPosSetList();
					Collections.shuffle(numberPosSetList);
					for (Set<Position> numberPosSet : numberPosSetList) {
						GokigenSolver.Field virtual = new GokigenSolver.Field(wkField, true);
						for (Position numberPos : numberPosSet) {
							virtual.extraNumbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						}
						int solveResult = new GokigenSolverForGenerator(virtual, 5000).solve2();
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
			level = (int) Math.sqrt(level * 5 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
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
					sb.append("<line y1=\""
							+ (yIndex * baseSize + baseSize / 2 + margin)
							+ "\" x1=\""
							+ (xIndex * baseSize + baseSize / 2 + 2 * baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize / 2 + baseSize + margin)
							+ "\" x2=\""
							+ (xIndex * baseSize + baseSize / 2 + 2 * baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					sb.append(">"
							+ "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					sb.append("<line y1=\""
							+ (yIndex * baseSize + baseSize + baseSize / 2 + margin)
							+ "\" x1=\""
							+ (xIndex * baseSize + baseSize + baseSize / 2)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize / 2 + margin)
							+ "\" x2=\""
							+ (xIndex * baseSize + baseSize + baseSize + baseSize / 2)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					sb.append(">"
							+ "</line>");
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
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 3)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 5)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize + 3)
								+ "\" font-size=\""
								+ (baseSize - 6)
								+ "\" textLength=\""
								+ (baseSize - 6)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr
								+ "</text>");
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

		// マスの情報。ここでは黒=＼、白=／とする
		private Masu[][] masu;
		// 数字の情報。外壁も考慮するため注意
		private final Integer[][] extraNumbers;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?gokigen/" + getXLength() + "/" + getYLength() + "/");
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
						sb.append(FULL_NUMS.substring(extraNumbers[yIndex][xIndex],
								extraNumbers[yIndex][xIndex] + 1));
					}
					if (xIndex != getXLength()) {
						sb.append("　");
					}
				}
				sb.append(System.lineSeparator());
				if (yIndex != getYLength()) {
					sb.append("　");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(masu[yIndex][xIndex] == Masu.BLACK ? "＼"
								: masu[yIndex][xIndex] == Masu.NOT_BLACK ? "／" : "　");
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
					sb.append(masu[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 数字指定のあるマスの周囲の自分に向いてる個数を数え、確定する箇所は埋める。
		 * 矛盾したらfalseを返す。
		 */
		public boolean aroundSolve() {
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (extraNumbers[yIndex][xIndex] != null && extraNumbers[yIndex][xIndex] != -1) {
						int yesCnt = 0;
						int noCnt = 0;
						Masu masuUpRight = yIndex == 0 || xIndex == getXLength() ? Masu.BLACK
								: masu[yIndex - 1][xIndex];
						if (masuUpRight == Masu.BLACK) {
							noCnt++;
						} else if (masuUpRight == Masu.NOT_BLACK) {
							yesCnt++;
						}
						Masu masuRightDown = xIndex == getXLength() || yIndex == getYLength() ? Masu.NOT_BLACK
								: masu[yIndex][xIndex];
						if (masuRightDown == Masu.BLACK) {
							yesCnt++;
						} else if (masuRightDown == Masu.NOT_BLACK) {
							noCnt++;
						}
						Masu masuDownLeft = yIndex == getYLength() || xIndex == 0 ? Masu.BLACK
								: masu[yIndex][xIndex - 1];
						if (masuDownLeft == Masu.BLACK) {
							noCnt++;
						} else if (masuDownLeft == Masu.NOT_BLACK) {
							yesCnt++;
						}
						Masu masuLeftUp = xIndex == 0 || yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex - 1];
						if (masuLeftUp == Masu.BLACK) {
							yesCnt++;
						} else if (masuLeftUp == Masu.NOT_BLACK) {
							noCnt++;
						}
						if (extraNumbers[yIndex][xIndex] < yesCnt) {
							// 自分向き過剰
							return false;
						}
						if (extraNumbers[yIndex][xIndex] > 4 - noCnt) {
							// 自分向き不足
							return false;
						}
						if (extraNumbers[yIndex][xIndex] == yesCnt) {
							if (masuUpRight == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.BLACK;
							}
							if (masuRightDown == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
							if (masuDownLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.BLACK;
							}
							if (masuLeftUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex - 1] = Masu.NOT_BLACK;
							}
						}
						if (extraNumbers[yIndex][xIndex] == 4 - noCnt) {
							if (masuUpRight == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masuRightDown == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
							if (masuDownLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
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
		 * 確定斜めがループしたらfalseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> resolvedPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (!resolvedPosSet.contains(pos)) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						if (!setContinuePosSet(pos, continuePosSet, null)) {
							return false;
						} else {
							resolvedPosSet.addAll(continuePosSet);
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に斜めにつなぐ。元のマスに戻ってきたらfalseを返す。
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			// 右上
			if (pos.getyIndex() != 0 && pos.getxIndex() != getXLength()
					&& masu[pos.getyIndex() - 1][pos.getxIndex()] == Masu.NOT_BLACK && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				} else {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.DOWN)) {
						return false;
					}
				}
			}
			// 右下
			if (pos.getyIndex() != getYLength() && pos.getxIndex() != getXLength()
					&& masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() + 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				} else {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.LEFT)) {
						return false;
					}
				}
			}
			// 左下
			if (pos.getyIndex() != getYLength() && pos.getxIndex() != 0
					&& masu[pos.getyIndex()][pos.getxIndex() - 1] == Masu.NOT_BLACK && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				} else {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.UP)) {
						return false;
					}
				}
			}
			// 左上
			if (pos.getyIndex() != 0 && pos.getxIndex() != 0
					&& masu[pos.getyIndex() - 1][pos.getxIndex() - 1] == Masu.BLACK && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() - 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				} else {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
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
			return true;
		}

		/**
		 * 各種チェックを1セット実行
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
			}
			return true;
		}

	}

	protected final Field field;
	protected int count;

	public GokigenSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public GokigenSolver(Field field) {
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
		System.out.println(new GokigenSolver(height, width, param).solve());
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
		}
		return true;
	}
}
package myamya.other.solver.kurotto;

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
import myamya.other.solver.Solver;

public class KurottoSolver implements Solver {
	public static class KurottoGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class KurottoSolverForGenerator extends KurottoSolver {

			private final int limit;

			public KurottoSolverForGenerator(Field field, int limit) {
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

		public KurottoGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new KurottoGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			KurottoSolver.Field wkField = new KurottoSolver.Field(height, width);
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
							KurottoSolver.Field virtual = new KurottoSolver.Field(wkField);
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
							wkField = new KurottoSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							// 2段階減らし(□表出→表出なし)に対応するため二重にする
							numberPosList.add(new Position(yIndex, xIndex));
							numberPosList.add(new Position(yIndex, xIndex));
						}
					}
				}
				wkField.setNumbers();
				// 数字のマス以外を戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] == null) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				// 解けるかな？
				level = new KurottoSolverForGenerator(new KurottoSolver.Field(wkField), 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new KurottoSolver.Field(height, width);
					index = 0;
				} else {
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						KurottoSolver.Field virtual = new KurottoSolver.Field(wkField, true);
						if (virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] == null) {
							continue;
						} else if (virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] != -1
								&& Math.random() * 2 < 1) {
							// 普通にすると数字のない白丸が多くなりがちなので、
							// 1/2の確率でしか白丸にならないようにする
							virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = -1;
						} else {
							virtual.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
							virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						}
						int solveResult = new KurottoSolverForGenerator(virtual, 10000).solve2();
						if (solveResult != -1) {
							wkField.masu[numberPos.getyIndex()][numberPos
									.getxIndex()] = virtual.masu[numberPos.getyIndex()][numberPos.getxIndex()];
							wkField.numbers[numberPos.getyIndex()][numberPos
									.getxIndex()] = virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()];
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字/丸：" + wkField.getHintCount().split("/")[0] + "/"
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
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");
						if (wkField.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
							int idx = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (idx >= 0) {
								masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin)
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
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.(kurotto)");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}
	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private final Integer[][] numbers;
		// 確定済み数字の位置情報
		private final Set<Position> alreadyPosSet;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?kurotto/" + getXLength() + "/" + getYLength() + "/");
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
					String numStr;
					if (num == -1) {
						numStr = ".";
					} else {
						numStr = Integer.toHexString(num);
					}
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
			int misakiCnt = 0;
			int numberCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						numberCnt++;
						if (numbers[yIndex][xIndex] != -1) {
							misakiCnt++;
						}
					}
				}
			}
			return String.valueOf(numberCnt + "/" + misakiCnt);
		}

		/**
		 * ジェネレータ用
		 */
		public void setNumbers() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continueBlackPosSet = new HashSet<>();
						setContinueBlackPosSet(pivot, continueBlackPosSet, Integer.MAX_VALUE, null);
						numbers[yIndex][xIndex] = continueBlackPosSet.size();
					}
				}
			}
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
			alreadyPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			alreadyPosSet = new HashSet<>();
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
					//16 - 255は '-'
					//256 - 999は '+'
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
			alreadyPosSet = new HashSet<>(other.alreadyPosSet);
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
			alreadyPosSet = new HashSet<>(other.alreadyPosSet);
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
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!countSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 白マスの前後左右にある連結黒マスおよび黒マス候補の数をカウントする。
		 * 超過や不足が確定したらfalseを返す。
		 */
		private boolean countSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						Position pivot = new Position(yIndex, xIndex);
						if (alreadyPosSet.contains(pivot)) {
							continue;
						}
						Set<Position> continueBlackPosSet = new HashSet<>();
						if (!(setContinueBlackPosSet(pivot, continueBlackPosSet, numbers[yIndex][xIndex], null))) {
							// サイズ超過
							return false;
						} else if (numbers[yIndex][xIndex] == continueBlackPosSet.size()) {
							// サイズ確定。周りを白に
							continueBlackPosSet.add(pivot);
							alreadyPosSet.add(pivot);
							for (Position pos : continueBlackPosSet) {
								if (pos.getyIndex() != 0 && !continueBlackPosSet
										.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
									masu[pos.getyIndex() - 1][pos.getxIndex()] = Masu.NOT_BLACK;
								}
								if (pos.getxIndex() != getXLength() - 1 && !continueBlackPosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
									masu[pos.getyIndex()][pos.getxIndex() + 1] = Masu.NOT_BLACK;
								}
								if (pos.getyIndex() != getYLength() - 1 && !continueBlackPosSet
										.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
									masu[pos.getyIndex() + 1][pos.getxIndex()] = Masu.NOT_BLACK;
								}
								if (pos.getxIndex() != 0 && !continueBlackPosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
									masu[pos.getyIndex()][pos.getxIndex() - 1] = Masu.NOT_BLACK;
								}
							}
						} else {
							Set<Position> continueNotWhitePosSet = new HashSet<>();
							if (!setContinueNotWhitePosSet(pivot, continueNotWhitePosSet, numbers[yIndex][xIndex],
									null)) {
								if (numbers[yIndex][xIndex] == continueNotWhitePosSet.size()) {
									alreadyPosSet.add(pivot);
									for (Position pos : continueNotWhitePosSet) {
										masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
									}
								} else {
									// サイズ不足
									return false;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に白確定でないマスをつなげていく。
		 * sizeが不足しないと分かった時点でtrueを返す。
		 */
		private boolean setContinueNotWhitePosSet(Position pos, Set<Position> continuePosSet, int size,
				Direction from) {
			if (continuePosSet.size() > size) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueNotWhitePosSet(nextPos, continuePosSet, size, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueNotWhitePosSet(nextPos, continuePosSet, size, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueNotWhitePosSet(nextPos, continuePosSet, size, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueNotWhitePosSet(nextPos, continuePosSet, size, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右に黒確定マスをつなげていく。
		 * sizeが超過すると分かった時点でfalseを返す。
		 */
		private boolean setContinueBlackPosSet(Position pos, Set<Position> continuePosSet, int size, Direction from) {
			if (continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinueBlackPosSet(nextPos, continuePosSet, size, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinueBlackPosSet(nextPos, continuePosSet, size, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinueBlackPosSet(nextPos, continuePosSet, size, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinueBlackPosSet(nextPos, continuePosSet, size, Direction.RIGHT)) {
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
			return solveAndCheck();
		}

	}

	public static class Room {
		@Override
		public String toString() {
			return "Room [capacity=" + capacity + ", pivot=" + pivot + "]";
		}

		// 白マスが何マスあるか。
		private final int capacity;
		// 部屋に属するマスの集合
		private final Position pivot;

		public Room(int capacity, Position pivot) {
			this.capacity = capacity;
			this.pivot = pivot;
		}

		public int getCapacity() {
			return capacity;
		}

		public Position getPivot() {
			return pivot;
		}

	}

	protected final Field field;
	protected int count;

	public KurottoSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public KurottoSolver(Field field) {
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
		System.out.println(new KurottoSolver(height, width, param).solve());
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
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					//					// 周囲に空白が少ない個所を優先して調査
					//					Masu masuUp = yIndex == 0 ? Masu.BLACK
					//							: field.masu[yIndex - 1][xIndex];
					//					Masu masuRight = xIndex == field.getXLength() - 1 ? Masu.BLACK
					//							: field.masu[yIndex][xIndex + 1];
					//					Masu masuDown = yIndex == field.getYLength() - 1 ? Masu.BLACK
					//							: field.masu[yIndex + 1][xIndex];
					//					Masu masuLeft = xIndex == 0 ? Masu.BLACK
					//							: field.masu[yIndex][xIndex - 1];
					//					int whiteCnt = 0;
					//					if (masuUp == Masu.SPACE) {
					//						whiteCnt++;
					//					}
					//					if (masuRight == Masu.SPACE) {
					//						whiteCnt++;
					//					}
					//					if (masuDown == Masu.SPACE) {
					//						whiteCnt++;
					//					}
					//					if (masuLeft == Masu.SPACE) {
					//						whiteCnt++;
					//					}
					//					if (whiteCnt > 3) {
					//						continue;
					//					}
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
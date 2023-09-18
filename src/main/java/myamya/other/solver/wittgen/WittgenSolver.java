package myamya.other.solver.wittgen;

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
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class WittgenSolver implements Solver {
	public static class WittgenGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class WittgenSolverForGenerator extends WittgenSolver {
			private final int limit;

			public WittgenSolverForGenerator(Field field, int limit) {
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

		public WittgenGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new WittgenGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			WittgenSolver.Field wkField = new WittgenSolver.Field(height, width);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				List<Integer> indexList = new ArrayList<>();
				List<Sikaku> useCand = new ArrayList<>(wkField.squareCand);
				for (int i = 0; i < useCand.size(); i++) {
					indexList.add(i);
				}
				Collections.shuffle(indexList);
				// 問題生成部
				while (!wkField.isSolved()) {
					Sikaku oneCand = useCand.get(index);
					if (wkField.squareCand.contains(oneCand)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							WittgenSolver.Field virtual = new WittgenSolver.Field(wkField, true);
							if (masuNum < 1) {
								virtual.squareCand.remove(oneCand);
							} else if (masuNum < 2) {
								virtual.squareCand.remove(oneCand);
								virtual.squareFixed.add(oneCand);
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.squareCand = virtual.squareCand;
								wkField.squareFixed = virtual.squareFixed;
								break;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new WittgenSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// まず数字を埋める
				List<Position> numberPosList = new ArrayList<>();
				Masu[][] masu = wkField.getMasu();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						int cnt = 0;
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							continue;
						}
						if (yIndex != 0 && masu[yIndex - 1][xIndex] == Masu.BLACK) {
							cnt++;
						}
						if (xIndex != wkField.getXLength() - 1 && masu[yIndex][xIndex + 1] == Masu.BLACK) {
							cnt++;
						}
						if (yIndex != wkField.getYLength() - 1 && masu[yIndex + 1][xIndex] == Masu.BLACK) {
							cnt++;
						}
						if (xIndex != 0 && masu[yIndex][xIndex - 1] == Masu.BLACK) {
							cnt++;
						}
						wkField.numbers[yIndex][xIndex] = cnt;
						numberPosList.add(new Position(yIndex, xIndex));
					}
				}
				// System.out.println(wkField);
				// マスを戻す
				wkField.initCand();
				// 解けるかな？
				level = new WittgenSolverForGenerator(wkField, 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new WittgenSolver.Field(height, width);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						WittgenSolver.Field virtual = new WittgenSolver.Field(wkField, true);
						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						virtual.initCand();
						int solveResult = new WittgenSolverForGenerator(virtual, 10000).solve2();
						if (solveResult != -1) {
							wkField.numbers[numberPos.getyIndex()][numberPos
									.getxIndex()] = virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()];
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 4 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");

			// 数字描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" fill=\"" + "white" + "\" width=\"" + (baseSize)
								+ "\" height=\"" + (baseSize) + "\">" + "</rect>");
						if (wkField.getNumbers()[yIndex][xIndex] != -1) {
							String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
							int wkIndex = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (wkIndex >= 0) {
								masuStr = FULL_NUMS.substring(wkIndex / 2, wkIndex / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
									+ "\" fill=\"" + "black" + "\" textLength=\"" + (baseSize - 5)
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
						}
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" stroke-width=\"2\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" stroke-width=\"1\" ");
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
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize) + "\" fill=\"none\" ");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" stroke-width=\"2\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" stroke-width=\"1\" ");
					}
					sb.append(">" + "</line>");
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

		// 数字の情報
		private final Integer[][] numbers;
		// 四角の配置の候補
		protected List<Sikaku> squareCand;
		// 確定した四角候補
		protected List<Sikaku> squareFixed;

		public List<Sikaku> getSquareFixed() {
			return squareFixed;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://puzz.link/p?wittgen/" + getXLength() + "/" + getYLength() + "/");
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

		public String getHintCount() {
			int kuroCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						kuroCnt++;
					}
				}
			}
			return String.valueOf(kuroCnt);
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public Field(int height, int width) {
			numbers = new Integer[height][width];
			initCand();
		}

		/**
		 * 部屋のきりかたの候補を初期化する。
		 */
		protected void initCand() {
			squareCand = makeSquareCandBase(getYLength(), getXLength());
			squareFixed = new ArrayList<>();
		}

		/**
		 * 部屋のきりかたの候補を初期化する。 キャッシュを使って高速化。
		 */
		protected void initCand(List<Sikaku> squareCandBase) {
			squareCand = new ArrayList<>(squareCandBase);
			squareFixed = new ArrayList<>();
		}

		/**
		 * 部屋の切り方の候補を作成する。
		 */
		protected List<Sikaku> makeSquareCandBase(int height, int width) {
			List<Sikaku> squareCandBase = new ArrayList<>();
			for (int yIndex = 0; yIndex < height; yIndex++) {
				for (int xIndex = 0; xIndex < width; xIndex++) {
					if (xIndex < width - 2) {
						Sikaku sikaku = new Sikaku(new Position(yIndex, xIndex), new Position(yIndex, xIndex + 2));
						if (numbers[yIndex][xIndex] == null && numbers[yIndex][xIndex + 1] == null
								&& numbers[yIndex][xIndex + 2] == null) {
							squareCandBase.add(sikaku);
						}
					}
					if (yIndex < height - 2) {
						Sikaku sikaku = new Sikaku(new Position(yIndex, xIndex), new Position(yIndex + 2, xIndex));
						if (numbers[yIndex][xIndex] == null && numbers[yIndex + 1][xIndex] == null
								&& numbers[yIndex + 2][xIndex] == null) {
							squareCandBase.add(sikaku);
						}
					}
				}
			}
			return squareCandBase;
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
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
						capacity = Integer.parseInt(String.valueOf(ch), 16) % 5;
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
						index = index + Integer.parseInt(String.valueOf(ch), 16) / 5;
					}
					index++;
				}
			}
			initCand();
		}

		public Field(Field other) {
			numbers = other.numbers;
			squareCand = new ArrayList<>(other.squareCand);
			squareFixed = new ArrayList<>(other.squareFixed);
		}

		public Field(Field other, boolean flag) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
				}
			}
			squareCand = new ArrayList<>(other.squareCand);
			squareFixed = new ArrayList<>(other.squareFixed);
		}

		/**
		 * 候補情報からマスを復元する
		 */
		public Masu[][] getMasu() {
			Masu[][] masu = new Masu[getYLength()][getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.NOT_BLACK;
				}
			}
			for (Sikaku fixed : squareFixed) {
				for (int yIndex = fixed.getLeftUp().getyIndex(); yIndex <= fixed.getRightDown().getyIndex(); yIndex++) {
					for (int xIndex = fixed.getLeftUp().getxIndex(); xIndex <= fixed.getRightDown()
							.getxIndex(); xIndex++) {
						masu[yIndex][xIndex] = Masu.BLACK;
					}
				}
			}
			for (Sikaku cand : squareCand) {
				for (int yIndex = cand.getLeftUp().getyIndex(); yIndex <= cand.getRightDown().getyIndex(); yIndex++) {
					for (int xIndex = cand.getLeftUp().getxIndex(); xIndex <= cand.getRightDown()
							.getxIndex(); xIndex++) {
						masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
			}
			return masu;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			Masu[][] masu = getMasu();
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						} else if (numbers[yIndex][xIndex] == -1) {
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
						sb.append(masu[yIndex][xIndex]);
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			sb.append(squareFixed.size() + ":" + squareCand.size());
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			if (!sikakuSolve()) {
				return false;
			}
			if (!countSolve()) {
				return false;
			}
			if (!connectSolve()) {
				return false;
			}
			return true;
		}

		/**
		 * 確定した四角がある場合、それとかぶる候補を消す。
		 */
		private boolean sikakuSolve() {
			for (Sikaku fixed : squareFixed) {
				for (Sikaku otherFixed : squareFixed) {
					if (fixed != otherFixed) {
						if (otherFixed.isDuplicate(fixed)) {
							return false;
						}
					}
				}
				for (Iterator<Sikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
					Sikaku sikaku = iterator.next();
					if (sikaku.isDuplicate(fixed)) {
						iterator.remove();
					}
				}
			}
			return true;
		}

		/**
		 * 四角を配置して数字の条件を満たせるかを調査する。 満たせない場合falseを返す。
		 */
		private boolean countSolve() {
			Masu[][] masu = getMasu();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						int blackCnt = 0;
						int spaceCnt = 0;
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
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
						// 黒マス過剰
						if (numbers[yIndex][xIndex] < blackCnt) {
							return false;
						}
						// 黒マス不足
						if (numbers[yIndex][xIndex] > blackCnt + spaceCnt) {
							return false;
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
			Masu[][] masu = getMasu();
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
				continuePosSet.add(typicalWhitePos);
				setContinueWhitePosSet(masu, typicalWhitePos, continuePosSet, null);
				whitePosSet.removeAll(continuePosSet);
				return whitePosSet.isEmpty();
			}
		}

		/**
		 * posを起点に上下左右に黒確定でないマスをつなげていく。
		 */
		private void setContinueWhitePosSet(Masu[][] masu, Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(masu, nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(masu, nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(masu, nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(masu, nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		public boolean isSolved() {
			return squareCand.size() == 0 && solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public WittgenSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public WittgenSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	//
	public static void main(String[] args) {
		String url = ""; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new WittgenSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 4));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 4 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count / 4).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (Iterator<Sikaku> iterator = field.squareCand.iterator(); iterator.hasNext();) {
			count++;
			Sikaku oneCand = iterator.next();
			Field virtual = new Field(field);
			virtual.squareCand.remove(oneCand);
			virtual.squareFixed.add(oneCand);
			boolean allowBlack = virtual.solveAndCheck();
			if (allowBlack && recursive > 0) {
				if (!candSolve(virtual, recursive - 1)) {
					allowBlack = false;
				}
			}
			Field virtual2 = new Field(field);
			virtual2.squareCand.remove(oneCand);
			boolean allowNotBlack = virtual2.solveAndCheck();
			if (allowNotBlack && recursive > 0) {
				if (!candSolve(virtual2, recursive - 1)) {
					allowNotBlack = false;
				}
			}
			if (!allowBlack && !allowNotBlack) {
				return false;
			} else if (!allowBlack) {
				field.squareCand = virtual2.squareCand;
				field.squareFixed = virtual2.squareFixed;
			} else if (!allowNotBlack) {
				field.squareCand = virtual.squareCand;
				field.squareFixed = virtual.squareFixed;
			}
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
		return true;
	}

}
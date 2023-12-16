package myamya.other.solver.squarejam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class SquarejamSolver implements Solver {
	public static class SquarejamGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class SquarejamSolverForGenerator extends SquarejamSolver {
			private final int limit;

			public SquarejamSolverForGenerator(Field field, int limit) {
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

		public SquarejamGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new SquarejamGenerator(12, 12).generate();
		}

		@Override
		public GeneratorResult generate() {
			SquarejamSolver.Field wkField = new SquarejamSolver.Field(height, width);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				List<Integer> indexList = new ArrayList<>();
				List<SquarejamSikaku> useCand = new ArrayList<>(wkField.squareCand);
				for (int i = 0; i < useCand.size(); i++) {
					indexList.add(i);
				}
				Collections.shuffle(indexList);
				// 問題生成部
				while (!wkField.isSolved()) {
					SquarejamSikaku oneCand = useCand.get(index);
					if (wkField.squareCand.contains(oneCand)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							SquarejamSolver.Field virtual = new SquarejamSolver.Field(wkField);
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
							wkField = new SquarejamSolver.Field(height, width, useCand);
							index = 0;
							continue;
						}
					}
					index++;
				}
				if (wkField.squareFixed.size() == 1) {
					// 分割できなければやり直し
					wkField = new SquarejamSolver.Field(height, width, useCand);
					index = 0;
					continue;
				}
				// 数字埋め＆マス初期化
				// まず数字を埋める
				List<Position> numberPosList = new ArrayList<>();
				for (SquarejamSikaku sikaku : wkField.squareFixed) {
					for (Position pos : sikaku.posList) {
						wkField.numbers[pos.getyIndex()][pos.getxIndex()] = sikaku.size;
						numberPosList.add(new Position(pos.getyIndex(), pos.getxIndex()));
					}
				}
				System.out.println(wkField);
				// マスを戻す
				wkField.initCand();
				// 解けるかな？
				level = new SquarejamSolverForGenerator(wkField, 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new SquarejamSolver.Field(height, width);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						SquarejamSolver.Field virtual = new SquarejamSolver.Field(wkField);
						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						virtual.initCand();
						int solveResult = new SquarejamSolverForGenerator(virtual, 1000).solve2();
						if (solveResult != -1) {
							wkField.numbers[numberPos.getyIndex()][numberPos
									.getxIndex()] = virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()];
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 3 / 3) + 1;
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

	// 2-3マスの集合体
	public static class SquarejamSikaku {
		private final int size;
		private final Sikaku sikaku;
		// 以下はその都度算出すると遅くなるのでコンストラクタ時点で覚えさせる
		private final List<Position> posList;
		private final Set<Position> yokoWall;
		private final Set<Position> tateWall;

		SquarejamSikaku(Sikaku sikaku, int size) {
			this.size = size;
			this.sikaku = sikaku;
			posList = new ArrayList<>(sikaku.getPosSet());
			yokoWall = new HashSet<>();
			for (int y = sikaku.getLeftUp().getyIndex(); y <= sikaku.getRightDown().getyIndex(); y++) {
				yokoWall.add(new Position(y, sikaku.getLeftUp().getxIndex() - 1));
				yokoWall.add(new Position(y, sikaku.getRightDown().getxIndex()));
			}
			tateWall = new HashSet<>();
			for (int x = sikaku.getLeftUp().getxIndex(); x <= sikaku.getRightDown().getxIndex(); x++) {
				tateWall.add(new Position(sikaku.getLeftUp().getyIndex() - 1, x));
				tateWall.add(new Position(sikaku.getRightDown().getyIndex(), x));
			}
		}

		// 自分が占めるマス一覧を返す
		public List<Position> getPosList() {
			return posList;
		}

		// 重複を判断して返す。Squarejamでは、自身とタタミの関係になる四角もだめ。
		public boolean isDuplicate(SquarejamSikaku other) {
			if (sikaku.isDuplicate(other.sikaku)) {
				return true;
			}
			if (sikaku.getLeftUp().getxIndex() - 1 == other.sikaku.getRightDown().getxIndex()
					&& sikaku.getLeftUp().getyIndex() - 1 == other.sikaku.getRightDown().getyIndex()) {
				return true;
			}
			if (sikaku.getRightDown().getxIndex() + 1 == other.sikaku.getLeftUp().getxIndex()
					&& sikaku.getLeftUp().getyIndex() - 1 == other.sikaku.getRightDown().getyIndex()) {
				return true;
			}
			if (sikaku.getRightDown().getxIndex() + 1 == other.sikaku.getLeftUp().getxIndex()
					&& sikaku.getRightDown().getyIndex() + 1 == other.sikaku.getLeftUp().getyIndex()) {
				return true;
			}
			if (sikaku.getLeftUp().getxIndex() - 1 == other.sikaku.getRightDown().getxIndex()
					&& sikaku.getRightDown().getyIndex() + 1 == other.sikaku.getLeftUp().getyIndex()) {
				return true;
			}
			return false;
		}

		/**
		 * 自分が作ることになる横壁の位置を返す。外壁(-1、xlength)も含むので注意
		 */
		public Set<Position> getYokoWall() {
			return yokoWall;
		}

		/**
		 * 自分が作ることになる縦壁の位置を返す。外壁(-1、ylength)も含むので注意
		 */
		public Set<Position> getTateWall() {
			return tateWall;
		}

		@Override
		public String toString() {
			return sikaku.toString();
		}

	}

	public static class Field {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";
		protected Integer[][] numbers;
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 四角の配置の候補
		protected List<SquarejamSikaku> squareCand;
		// 確定した四角候補
		protected List<SquarejamSikaku> squareFixed;

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://puzz.link/p?squarejam/" + getXLength() + "/" + getYLength() + "/");
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

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		/**
		 * プレーンなフィールド作成
		 */
		public Field(int height, int width) {
			numbers = new Integer[height][width];
			initCand();
		}

		public Field(int height, int width, List<SquarejamSikaku> cacheCand) {
			numbers = new Integer[height][width];
			squareCand = new ArrayList<>(cacheCand);
			squareFixed = new ArrayList<>();
		}

		/**
		 * 部屋のきりかたの候補を初期化する。
		 */
		protected void initCand() {
			squareCand = makeSquareCandBase();
			squareFixed = new ArrayList<>();
		}

		/**
		 * 部屋の切り方の候補を作成する。
		 */
		protected List<SquarejamSikaku> makeSquareCandBase() {
			List<SquarejamSikaku> squareCandBase = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					for (int size = 1; size <= getYLength() - yIndex && size <= getXLength() - xIndex; size++) {
						int minY = yIndex;
						int maxY = yIndex + size - 1;
						int minX = xIndex;
						int maxX = xIndex + size - 1;
						SquarejamSikaku sikaku = new SquarejamSikaku(
								new Sikaku(new Position(minY, minX), new Position(maxY, maxX)), size);
						// sikakuの中に数字が入った場合、sizeがそれに等しい必要がある
						boolean canAdd = true;
						for (Position pos : sikaku.posList) {
							if (numbers[pos.getyIndex()][pos.getxIndex()] != null
									&& numbers[pos.getyIndex()][pos.getxIndex()] != -1
									&& numbers[pos.getyIndex()][pos.getxIndex()] != size) {
								canAdd = false;
								break;
							}
						}
						if (!canAdd) {
							continue;
						}
						squareCandBase.add(sikaku);
					}
				}
			}
			return squareCandBase;
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			int readPos = 0;
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
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
			initCand();
		}

		public Field(Field other) {
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
		 * 候補情報から横壁を復元する
		 */
		public Wall[][] getYokoWall() {
			Wall[][] yokoWall = new Wall[getYLength()][getXLength() - 1];
			Set<Position> fixedWallPosSet = new HashSet<>();
			Set<Position> candWallPosSet = new HashSet<>();
			for (SquarejamSikaku fixed : squareFixed) {
				fixedWallPosSet.addAll(fixed.getYokoWall());
			}
			for (SquarejamSikaku cand : squareCand) {
				candWallPosSet.addAll(cand.getYokoWall());
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (fixedWallPosSet.contains(pos)) {
						yokoWall[yIndex][xIndex] = Wall.EXISTS;
					} else if (candWallPosSet.contains(pos)) {
						yokoWall[yIndex][xIndex] = Wall.SPACE;
					} else {
						yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
					}
				}
			}
			return yokoWall;
		}

		/**
		 * 候補情報から横壁を復元する
		 */
		public Wall[][] getTateWall() {
			Wall[][] tateWall = new Wall[getYLength() - 1][getXLength()];
			Set<Position> fixedWallPosSet = new HashSet<>();
			Set<Position> candWallPosSet = new HashSet<>();
			for (SquarejamSikaku fixed : squareFixed) {
				fixedWallPosSet.addAll(fixed.getTateWall());
			}
			for (SquarejamSikaku cand : squareCand) {
				candWallPosSet.addAll(cand.getTateWall());
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (fixedWallPosSet.contains(pos)) {
						tateWall[yIndex][xIndex] = Wall.EXISTS;
					} else if (candWallPosSet.contains(pos)) {
						tateWall[yIndex][xIndex] = Wall.SPACE;
					} else {
						tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
					}
				}
			}
			return tateWall;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			Wall[][] yokoWall = getYokoWall();
			Wall[][] tateWall = getTateWall();
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] == -1) {
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
						sb.append("　");
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
			sb.append(squareFixed.size() + ":" + squareCand.size());
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!sikakuSolve()) {
				return false;
			}
			if (!countSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 確定した四角がある場合、それとかぶる候補を消す。すでにかぶっていたらfalse。
		 */
		private boolean sikakuSolve() {
			for (SquarejamSikaku fixed : squareFixed) {
				for (SquarejamSikaku otherFixed : squareFixed) {
					if (fixed != otherFixed) {
						if (otherFixed.isDuplicate(fixed)) {
							return false;
						}
					}
				}
				for (Iterator<SquarejamSikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
					SquarejamSikaku oneCand = iterator.next();
					if (oneCand.isDuplicate(fixed)) {
						iterator.remove();
					}
				}
			}
			return true;
		}

		/**
		 * 埋まらないマスが出来てしまった場合falseを返す。 逆に、あるマスを埋められる候補が1つしかない場合、確定形に昇格する。
		 */
		private boolean countSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// 既に確定しているマスを除外
					Position pos = new Position(yIndex, xIndex);
					boolean isFixed = false;
					for (SquarejamSikaku fixed : squareFixed) {
						if (fixed.getPosList().contains(pos)) {
							isFixed = true;
							break;
						}
					}
					if (isFixed) {
						continue;
					}
					// そのマスに対する候補を検索
					SquarejamSikaku pickup = null;
					boolean only = true;
					for (SquarejamSikaku cand : squareCand) {
						if (cand.getPosList().contains(pos)) {
							if (pickup == null) {
								pickup = cand;
							} else {
								only = false;
								break;
							}
						}
					}
					// 候補が1つもなければfalse
					if (pickup == null) {
						return false;
					}
					// 候補が1つに定まっていれば確定
					if (only) {
						squareCand.remove(pickup);
						squareFixed.add(pickup);
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			return squareCand.size() == 0 && solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public SquarejamSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public SquarejamSolver(Field field) {
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
		System.out.println(new SquarejamSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 3));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 3 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 3).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (Iterator<SquarejamSikaku> iterator = field.squareCand.iterator(); iterator.hasNext();) {
			count++;
			SquarejamSikaku oneCand = iterator.next();
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

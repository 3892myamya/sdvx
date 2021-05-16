package myamya.other.solver.heteromino;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class HeterominoSolver implements Solver {
	//	public static class HeterominoGenerator implements Generator {
	//
	//		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
	//		private static final String FULL_NUMS = "０１２３４５６７８９";
	//
	//		static class HeterominoSolverForGenerator extends HeterominoSolver {
	//			private final int limit;
	//
	//			public HeterominoSolverForGenerator(Field field, int limit) {
	//				super(field);
	//				this.limit = limit;
	//			}
	//
	//			public int solve2() {
	//				try {
	//					while (!field.isSolved()) {
	//						String befStr = field.getStateDump();
	//						if (!field.solveAndCheck()) {
	//							return -1;
	//						}
	//						int recursiveCnt = 0;
	//						while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
	//							if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
	//								return -1;
	//							}
	//							recursiveCnt++;
	//						}
	//						if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
	//							return -1;
	//						}
	//					}
	//				} catch (CountOverException e) {
	//					return -1;
	//				}
	//				return count;
	//			}
	//
	//			@Override
	//			protected boolean candSolve(Field field, int recursive) {
	//				if (this.count >= limit) {
	//					throw new CountOverException();
	//				} else {
	//					return super.candSolve(field, recursive);
	//				}
	//			}
	//		}
	//
	//		private final int height;
	//		private final int width;
	//
	//		public HeterominoGenerator(int height, int width) {
	//			this.height = height;
	//			this.width = width;
	//		}
	//
	//		public static void main(String[] args) {
	//			new HeterominoGenerator(10, 10).generate();
	//		}
	//
	//		@Override
	//		public GeneratorResult generate() {
	//			List<Sikaku> shapeCandBase = HeterominoSolver.Field.makeshapeCandBase(height, width);
	//			HeterominoSolver.Field wkField = new HeterominoSolver.Field(height, width, shapeCandBase);
	//			int index = 0;
	//			int level = 0;
	//			long start = System.nanoTime();
	//			while (true) {
	//				for (Iterator<Sikaku> iterator = wkField.shapeCand.iterator(); iterator.hasNext();) {
	//					// 1x1の正方形＆巨大な正方形を発生しにくくする処理
	//					Sikaku sikaku = iterator.next();
	//					int sikakuLength = (int) Math.sqrt(sikaku.getAreaSize());
	//					int fieldLength = height < width ? height : width;
	//					sikakuLength = sikakuLength == 1 ? fieldLength / 4 : sikakuLength;
	//					int fieldSize = (int) Math.pow(fieldLength - 1, 4);
	//					int isOkRange = (int) Math.pow(fieldLength - sikakuLength, 4);
	//					if (isOkRange < Math.random() * fieldSize) {
	//						iterator.remove();
	//					}
	//				}
	//				List<Integer> indexList = new ArrayList<>();
	//				List<Sikaku> useCand = new ArrayList<>(wkField.shapeCand);
	//				for (int i = 0; i < useCand.size(); i++) {
	//					indexList.add(i);
	//				}
	//				Collections.shuffle(indexList);
	//				// 問題生成部
	//				while (!wkField.isSolved()) {
	//					Sikaku oneCand = useCand.get(index);
	//					if (wkField.shapeCand.contains(oneCand)) {
	//						boolean isOk = false;
	//						List<Integer> numIdxList = new ArrayList<>();
	//						for (int i = 0; i < 2; i++) {
	//							numIdxList.add(i);
	//						}
	//						Collections.shuffle(numIdxList);
	//						for (int masuNum : numIdxList) {
	//							HeterominoSolver.Field virtual = new HeterominoSolver.Field(wkField, true);
	//							if (masuNum < 1) {
	//								virtual.shapeCand.remove(oneCand);
	//							} else if (masuNum < 2) {
	//								virtual.shapeCand.remove(oneCand);
	//								virtual.shapeFixed.add(oneCand);
	//							}
	//							if (virtual.solveAndCheck()) {
	//								isOk = true;
	//								wkField.shapeCand = virtual.shapeCand;
	//								wkField.shapeFixed = virtual.shapeFixed;
	//								break;
	//							}
	//						}
	//						if (!isOk) {
	//							// 破綻したら0から作り直す。
	//							wkField = new HeterominoSolver.Field(height, width, shapeCandBase);
	//							index = 0;
	//							continue;
	//						}
	//					}
	//					index++;
	//				}
	//				// 数字埋め＆マス初期化
	//				// まず数字を埋める
	//				List<Position> numberPosList = new ArrayList<>();
	//				Masu[][] masu = wkField.getMasu();
	//				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
	//					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
	//						int cnt = 0;
	//						if (masu[yIndex][xIndex] == Masu.BLACK) {
	//							cnt++;
	//						}
	//						if (yIndex != 0 && masu[yIndex - 1][xIndex] == Masu.BLACK) {
	//							cnt++;
	//						}
	//						if (xIndex != wkField.getXLength() - 1 && masu[yIndex][xIndex + 1] == Masu.BLACK) {
	//							cnt++;
	//						}
	//						if (yIndex != wkField.getYLength() - 1 && masu[yIndex + 1][xIndex] == Masu.BLACK) {
	//							cnt++;
	//						}
	//						if (xIndex != 0 && masu[yIndex][xIndex - 1] == Masu.BLACK) {
	//							cnt++;
	//						}
	//						wkField.numbers[yIndex][xIndex] = cnt;
	//						numberPosList.add(new Position(yIndex, xIndex));
	//					}
	//				}
	//				// マスを戻す
	//				wkField.initCand(shapeCandBase);
	//				// 解けるかな？
	//				level = new HeterominoSolverForGenerator(wkField, 500).solve2();
	//				if (level == -1) {
	//					// 解けなければやり直し
	//					wkField = new HeterominoSolver.Field(height, width);
	//					index = 0;
	//				} else {
	//					// ヒントを限界まで減らす
	//					Collections.shuffle(numberPosList);
	//					for (Position numberPos : numberPosList) {
	//						System.out.println(wkField);
	//						HeterominoSolver.Field virtual = new HeterominoSolver.Field(wkField, true);
	//						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
	//						virtual.initCand(shapeCandBase);
	//						int solveResult = new HeterominoSolverForGenerator(virtual, 5000).solve2();
	//						if (solveResult != -1) {
	//							wkField.numbers[numberPos.getyIndex()][numberPos
	//									.getxIndex()] = virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()];
	//							level = solveResult;
	//						}
	//					}
	//					break;
	//				}
	//			}
	//			level = (int) Math.sqrt(level / 5 / 3);
	//			String status = "Lv:" + level + "の問題を獲得！(数字：" + wkField.getHintCount() + ")";
	//			String url = wkField.getPuzPreURL();
	//			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
	//			StringBuilder sb = new StringBuilder();
	//			int baseSize = 20;
	//			int margin = 5;
	//			sb.append(
	//					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
	//							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
	//							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
	//			// 数字描画
	//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
	//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
	//					if (wkField.getNumbers()[yIndex][xIndex] != null) {
	//						String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
	//						int numIdx = HALF_NUMS.indexOf(numberStr);
	//						String masuStr = null;
	//						if (numIdx >= 0) {
	//							masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
	//						} else {
	//							masuStr = numberStr;
	//						}
	//						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin)
	//								+ "\" x=\""
	//								+ (xIndex * baseSize + baseSize + 2)
	//								+ "\" font-size=\""
	//								+ (baseSize - 5)
	//								+ "\" textLength=\""
	//								+ (baseSize - 5)
	//								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
	//								+ masuStr
	//								+ "</text>");
	//
	//					}
	//				}
	//			}
	//			// 横壁描画
	//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
	//				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
	//					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
	//					sb.append("<line y1=\""
	//							+ (yIndex * baseSize + margin)
	//							+ "\" x1=\""
	//							+ (xIndex * baseSize + 2 * baseSize)
	//							+ "\" y2=\""
	//							+ (yIndex * baseSize + baseSize + margin)
	//							+ "\" x2=\""
	//							+ (xIndex * baseSize + 2 * baseSize)
	//							+ "\" stroke-width=\"1\" fill=\"none\"");
	//					if (oneYokoWall) {
	//						sb.append("stroke=\"#000\" ");
	//					} else {
	//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
	//					}
	//					sb.append(">"
	//							+ "</line>");
	//				}
	//			}
	//			// 縦壁描画
	//			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
	//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
	//					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
	//					sb.append("<line y1=\""
	//							+ (yIndex * baseSize + baseSize + margin)
	//							+ "\" x1=\""
	//							+ (xIndex * baseSize + baseSize)
	//							+ "\" y2=\""
	//							+ (yIndex * baseSize + baseSize + margin)
	//							+ "\" x2=\""
	//							+ (xIndex * baseSize + baseSize + baseSize)
	//							+ "\" stroke-width=\"1\" fill=\"none\"");
	//					if (oneTateWall) {
	//						sb.append("stroke=\"#000\" ");
	//					} else {
	//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
	//					}
	//					sb.append(">"
	//							+ "</line>");
	//				}
	//			}
	//			sb.append("</svg>");
	//			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	//			System.out.println(level);
	//			System.out.println(wkField.getHintCount());
	//			System.out.println(wkField);
	//			System.out.println(url);
	//			return new GeneratorResult(status, sb.toString(), link, url, level, "");
	//
	//		}
	//
	//	}

	// 3マスの集合体
	public static class Shape {
		// 1:右下欠け、2:左下欠け、3:右上欠け、4:左上欠け、5:縦I、6:横I
		private final int type;
		private final Set<Position> posSet;

		Shape(int type, Set<Position> posSet) {
			this.type = type;
			this.posSet = posSet;
		}

		public int getType() {
			return type;
		}

		public Set<Position> getPosSet() {
			return posSet;
		}

		// 自身が確定形であるとき、引数の形が禁止系の場合trueを返す。
		public boolean isBanned(Shape other) {
			for (Position otherPos : other.posSet) {
				// 重複はだめ
				if (posSet.contains(otherPos)) {
					return true;
				}
				// 同じ形状の時は隣接もだめ
				if (type == other.type) {
					if (posSet.contains(new Position(otherPos.getyIndex() + 1, otherPos.getxIndex()))
							|| posSet.contains(new Position(otherPos.getyIndex() - 1, otherPos.getxIndex()))
							|| posSet.contains(new Position(otherPos.getyIndex(), otherPos.getxIndex() + 1))
							|| posSet.contains(new Position(otherPos.getyIndex(), otherPos.getxIndex() - 1))) {
						return true;
					}
				}
			}
			return false;
		}

		// 自身の形によって生成される縦壁の位置を返す
		public Set<Position> getTateWallPosSet() {
			Set<Position> result = new HashSet<Position>();
			for (Position pos : posSet) {
				if (!posSet.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
					result.add(new Position(pos.getyIndex() - 1, pos.getxIndex()));
				}
				if (!posSet.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
					result.add(new Position(pos.getyIndex(), pos.getxIndex()));
				}
			}
			return result;
		}

		// 自身の形によって生成される横壁の位置を返す
		public Set<Position> getYokoWallPosSet() {
			Set<Position> result = new HashSet<Position>();
			for (Position pos : posSet) {
				if (!posSet.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
					result.add(new Position(pos.getyIndex(), pos.getxIndex() - 1));
				}
				if (!posSet.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
					result.add(new Position(pos.getyIndex(), pos.getxIndex()));
				}
			}
			return result;
		}
	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// マスの情報 trueなら黒
		protected boolean[][] masu;
		// 配置の候補
		protected List<Shape> shapeCand;
		// 確定した候補
		protected List<Shape> shapeFixed;

		public boolean[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://puzz.link/p?heteromino/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (masu[yIndex][xIndex]) {
					interval++;
					if (interval == 26) {
						sb.append("z");
						interval = 0;
					}
				} else {
					if (interval == 0) {
						sb.append(7);
					} else {
						sb.append(ALPHABET.substring(interval - 1, interval));
						sb.append(7);
						interval = 0;
					}
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET.substring(interval - 1, interval));
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
					if (masu[yIndex][xIndex]) {
						kuroCnt++;
					}
				}
			}
			return String.valueOf(kuroCnt);
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width) {
			masu = new boolean[height][width];
			initCand();
		}

		public Field(int height, int width, List<Shape> shapeCandBase) {
			masu = new boolean[height][width];
			initCand(shapeCandBase);
		}

		/**
		 * 部屋のきりかたの候補を初期化する。
		 */
		protected void initCand() {
			shapeCand = makeShapeCandBase(getYLength(), getXLength());
			shapeFixed = new ArrayList<>();
		}

		/**
		 * 部屋のきりかたの候補を初期化する。
		 * キャッシュを使って高速化。
		 */
		protected void initCand(List<Shape> shapeCandBase) {
			shapeCand = new ArrayList<>(shapeCandBase);
			shapeFixed = new ArrayList<>();
		}

		/**
		 * 部屋の切り方の候補を作成する。
		 * 黒マスを考慮するため、masuが既に設定されている状態で呼び出さなければならない。
		 */
		protected List<Shape> makeShapeCandBase(int height, int width) {
			List<Shape> shapeCandBase = new ArrayList<>();
			// L字型
			for (int yIndex = 0; yIndex < height - 1; yIndex++) {
				for (int xIndex = 0; xIndex < width - 1; xIndex++) {
					Position basePos = new Position(yIndex, xIndex);
					Position rightPos = new Position(yIndex, xIndex + 1);
					Position downPos = new Position(yIndex + 1, xIndex);
					Position oppositePos = new Position(yIndex + 1, xIndex + 1);
					if (!masu[basePos.getyIndex()][basePos.getxIndex()]
							&& !masu[rightPos.getyIndex()][rightPos.getxIndex()]
							&& !masu[downPos.getyIndex()][downPos.getxIndex()]) {
						shapeCandBase
								.add(new Shape(1, new HashSet<Position>(Arrays.asList(basePos, rightPos, downPos))));
					}
					if (!masu[basePos.getyIndex()][basePos.getxIndex()]
							&& !masu[rightPos.getyIndex()][rightPos.getxIndex()]
							&& !masu[oppositePos.getyIndex()][oppositePos.getxIndex()]) {
						shapeCandBase
								.add(new Shape(2,
										new HashSet<Position>(Arrays.asList(basePos, rightPos, oppositePos))));
					}
					if (!masu[basePos.getyIndex()][basePos.getxIndex()]
							&& !masu[oppositePos.getyIndex()][oppositePos.getxIndex()]
							&& !masu[downPos.getyIndex()][downPos.getxIndex()]) {
						shapeCandBase
								.add(new Shape(3, new HashSet<Position>(Arrays.asList(basePos, oppositePos, downPos))));
					}
					if (!masu[oppositePos.getyIndex()][oppositePos.getxIndex()]
							&& !masu[rightPos.getyIndex()][rightPos.getxIndex()]
							&& !masu[downPos.getyIndex()][downPos.getxIndex()]) {
						shapeCandBase
								.add(new Shape(4,
										new HashSet<Position>(Arrays.asList(oppositePos, rightPos, downPos))));
					}
				}
			}
			// 縦のI字型
			for (int yIndex = 0; yIndex < height - 2; yIndex++) {
				for (int xIndex = 0; xIndex < width - 0; xIndex++) {
					Position pos1 = new Position(yIndex, xIndex);
					Position pos2 = new Position(yIndex + 1, xIndex);
					Position pos3 = new Position(yIndex + 2, xIndex);
					if (!masu[pos1.getyIndex()][pos1.getxIndex()]
							&& !masu[pos2.getyIndex()][pos2.getxIndex()]
							&& !masu[pos3.getyIndex()][pos3.getxIndex()]) {
						shapeCandBase.add(new Shape(5, new HashSet<Position>(Arrays.asList(pos1, pos2, pos3))));
					}
				}
			}
			// 横のI字型
			for (int yIndex = 0; yIndex < height; yIndex++) {
				for (int xIndex = 0; xIndex < width - 2; xIndex++) {
					Position pos1 = new Position(yIndex, xIndex);
					Position pos2 = new Position(yIndex, xIndex + 1);
					Position pos3 = new Position(yIndex, xIndex + 2);
					if (!masu[pos1.getyIndex()][pos1.getxIndex()]
							&& !masu[pos2.getyIndex()][pos2.getxIndex()]
							&& !masu[pos3.getyIndex()][pos3.getxIndex()]) {
						shapeCandBase.add(new Shape(6, new HashSet<Position>(Arrays.asList(pos1, pos2, pos3))));
					}
				}
			}
			return shapeCandBase;
		}

		public Field(int height, int width, String param) {
			masu = new boolean[height][width];
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					Position pos = new Position(index / getXLength(), index % getXLength());
					masu[pos.getyIndex()][pos.getxIndex()] = true;
					index++;
				}
			}
			initCand();
		}

		public Field(Field other) {
			masu = other.masu;
			shapeCand = new ArrayList<>(other.shapeCand);
			shapeFixed = new ArrayList<>(other.shapeFixed);
		}

		public Field(Field other, boolean flag) {
			masu = new boolean[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			shapeCand = new ArrayList<>(other.shapeCand);
			shapeFixed = new ArrayList<>(other.shapeFixed);
		}

		@Override
		public String toString() {
			// 横をふさぐ壁が存在するか
			// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
			boolean[][] yokoWall = getYokoWall();
			// 縦をふさぐ壁が存在するか
			// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
			boolean[][] tateWall = getTateWall();

			StringBuilder sb = new StringBuilder();
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex] ? "■" : "・");
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex] ? "□" : "　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex] ? "□" : "　");
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

		// 候補情報から横壁を復元
		public boolean[][] getYokoWall() {
			boolean[][] result = new boolean[getYLength()][getXLength() - 1];
			Set<Position> yokoWallPosSet = new HashSet<>();
			for (Shape fixed : shapeFixed) {
				yokoWallPosSet.addAll(fixed.getYokoWallPosSet());
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoWallPosSet.contains(new Position(yIndex, xIndex))) {
						result[yIndex][xIndex] = true;
						continue;
					}
				}
			}
			return result;
		}

		// 候補情報から縦壁を復元
		public boolean[][] getTateWall() {
			boolean[][] result = new boolean[getYLength() - 1][getXLength()];
			Set<Position> tateWallPosSet = new HashSet<>();
			for (Shape fixed : shapeFixed) {
				tateWallPosSet.addAll(fixed.getTateWallPosSet());
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWallPosSet.contains(new Position(yIndex, xIndex))) {
						result[yIndex][xIndex] = true;
						continue;
					}
				}
			}
			return result;
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			sb.append(shapeFixed.size() + ":" + shapeCand.size());
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			if (!shapeSolve()) {
				return false;
			}
			if (!countSolve()) {
				return false;
			}
			return true;
		}

		/**
		 * 確定した形がある場合、それとかぶる候補を消す。
		 * また、それと同じ形の隣接している形状も消す。
		 */
		private boolean shapeSolve() {
			for (Shape fixed : shapeFixed) {
				for (Iterator<Shape> iterator = shapeCand.iterator(); iterator.hasNext();) {
					Shape cand = iterator.next();
					if (cand.isBanned(fixed)) {
						iterator.remove();
					}
				}
			}
			for (Shape fixed : shapeFixed) {
				for (Iterator<Shape> iterator = shapeFixed.iterator(); iterator.hasNext();) {
					Shape otherFixed = iterator.next();
					if (fixed != otherFixed && otherFixed.isBanned(fixed)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 埋まらないマスが出来てしまった場合falseを返す。
		 * 逆に、あるマスを埋められる候補が1つしかない場合、確定形に昇格する。
		 */
		private boolean countSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// 黒マスは除外
					if (masu[yIndex][xIndex]) {
						continue;
					}
					// 既に確定しているマスを除外
					boolean isFixed = false;
					for (Shape fixed : shapeFixed) {
						if (fixed.getPosSet().contains(new Position(yIndex, xIndex))) {
							isFixed = true;
							break;
						}
					}
					if (isFixed) {
						continue;
					}
					// そのマスに対する候補を検索
					Shape pickup = null;
					boolean only = true;
					for (Shape cand : shapeCand) {
						if (cand.getPosSet().contains(new Position(yIndex, xIndex))) {
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
						shapeCand.remove(pickup);
						shapeFixed.add(pickup);
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			return shapeCand.size() == 0 && solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public HeterominoSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public HeterominoSolver(Field field) {
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
		System.out.println(new HeterominoSolver(height, width, param).solve());
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
		for (Iterator<Shape> iterator = field.shapeCand.iterator(); iterator
				.hasNext();) {
			count++;
			Shape oneCand = iterator.next();
			Field virtual = new Field(field);
			virtual.shapeCand.remove(oneCand);
			virtual.shapeFixed.add(oneCand);
			boolean allowBlack = virtual.solveAndCheck();
			if (allowBlack && recursive > 0) {
				if (!candSolve(virtual, recursive - 1)) {
					allowBlack = false;
				}
			}
			Field virtual2 = new Field(field);
			virtual2.shapeCand.remove(oneCand);
			boolean allowNotBlack = virtual2.solveAndCheck();
			if (allowNotBlack && recursive > 0) {
				if (!candSolve(virtual2, recursive - 1)) {
					allowNotBlack = false;
				}
			}
			if (!allowBlack && !allowNotBlack) {
				return false;
			} else if (!allowBlack) {
				field.shapeCand = virtual2.shapeCand;
				field.shapeFixed = virtual2.shapeFixed;
			} else if (!allowNotBlack) {
				field.shapeCand = virtual.shapeCand;
				field.shapeFixed = virtual.shapeFixed;
			}
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
		return true;
	}

}
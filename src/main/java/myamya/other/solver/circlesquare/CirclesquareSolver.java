package myamya.other.solver.circlesquare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Solver;

public class CirclesquareSolver implements Solver {
//	public static class CirclesquareGenerator implements Generator {
//
//		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
//		private static final String FULL_NUMS = "０１２３４５６７８９";
//
//		static class CirclesquareSolverForGenerator extends CirclesquareSolver {
//			private final int limit;
//
//			public CirclesquareSolverForGenerator(Field field, int limit) {
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
//		public CirclesquareGenerator(int height, int width) {
//			this.height = height;
//			this.width = width;
//		}
//
//		public static void main(String[] args) {
//			new CirclesquareGenerator(10, 10).generate();
//		}
//
//		@Override
//		public GeneratorResult generate() {
//			List<Sikaku> squareCandBase = CirclesquareSolver.Field.makeSquareCandBase(height, width);
//			CirclesquareSolver.Field wkField = new CirclesquareSolver.Field(height, width, squareCandBase);
//			int index = 0;
//			int level = 0;
//			long start = System.nanoTime();
//			while (true) {
//				for (Iterator<Sikaku> iterator = wkField.squareCand.iterator(); iterator.hasNext();) {
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
//				List<Sikaku> useCand = new ArrayList<>(wkField.squareCand);
//				for (int i = 0; i < useCand.size(); i++) {
//					indexList.add(i);
//				}
//				Collections.shuffle(indexList);
//				// 問題生成部
//				while (!wkField.isSolved()) {
//					Sikaku oneCand = useCand.get(index);
//					if (wkField.squareCand.contains(oneCand)) {
//						boolean isOk = false;
//						List<Integer> numIdxList = new ArrayList<>();
//						for (int i = 0; i < 2; i++) {
//							numIdxList.add(i);
//						}
//						Collections.shuffle(numIdxList);
//						for (int masuNum : numIdxList) {
//							CirclesquareSolver.Field virtual = new CirclesquareSolver.Field(wkField, true);
//							if (masuNum < 1) {
//								virtual.squareCand.remove(oneCand);
//							} else if (masuNum < 2) {
//								virtual.squareCand.remove(oneCand);
//								virtual.squareFixed.add(oneCand);
//							}
//							if (virtual.solveAndCheck()) {
//								isOk = true;
//								wkField.squareCand = virtual.squareCand;
//								wkField.squareFixed = virtual.squareFixed;
//								break;
//							}
//						}
//						if (!isOk) {
//							// 破綻したら0から作り直す。
//							wkField = new CirclesquareSolver.Field(height, width, squareCandBase);
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
//				wkField.initCand(squareCandBase);
//				// 解けるかな？
//				level = new CirclesquareSolverForGenerator(wkField, 500).solve2();
//				if (level == -1) {
//					// 解けなければやり直し
//					wkField = new CirclesquareSolver.Field(height, width);
//					index = 0;
//				} else {
//					// ヒントを限界まで減らす
//					Collections.shuffle(numberPosList);
//					for (Position numberPos : numberPosList) {
//						System.out.println(wkField);
//						CirclesquareSolver.Field virtual = new CirclesquareSolver.Field(wkField, true);
//						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
//						virtual.initCand(squareCandBase);
//						int solveResult = new CirclesquareSolverForGenerator(virtual, 5000).solve2();
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

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;

		// 最初から決まっているマス
		private Set<Position> fixedPosSet;
		// 四角の配置の候補
		protected List<Sikaku> squareCand;
		// 確定した四角候補
		protected List<Sikaku> squareFixed;

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://puzz.link/p?circlesquare/" + getXLength() + "/" + getYLength() + "/");
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex1 = i / getXLength();
				int xIndex1 = i % getXLength();
				i++;
				int yIndex2 = i / getXLength();
				int xIndex2 = i % getXLength();
				i++;
				int yIndex3 = i / getXLength();
				int xIndex3 = i % getXLength();
				int bitInfo = 0;
				if (yIndex1 < getYLength()) {
					if (masu[yIndex1][xIndex1] == Masu.NOT_BLACK) {
						bitInfo = bitInfo + 9;
					} else if (masu[yIndex1][xIndex1] == Masu.BLACK) {
						bitInfo = bitInfo + 18;
					}
				}
				if (yIndex2 < getYLength()) {
					if (masu[yIndex2][xIndex2] == Masu.NOT_BLACK) {
						bitInfo = bitInfo + 3;
					} else if (masu[yIndex2][xIndex2] == Masu.BLACK) {
						bitInfo = bitInfo + 6;
					}
				}
				if (yIndex3 < getYLength()) {
					if (masu[yIndex3][xIndex3] == Masu.NOT_BLACK) {
						bitInfo = bitInfo + 1;
					} else if (masu[yIndex3][xIndex3] == Masu.BLACK) {
						bitInfo = bitInfo + 2;
					}
				}
				sb.append(Integer.toString(bitInfo, 36));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public String getHintCount() {
			return String.valueOf(fixedPosSet.size());
		}

		public Masu[][] getMasu() {
			return masu;
		}

		public Set<Position> getFixedPosSet() {
			return fixedPosSet;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
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
		static protected List<Sikaku> makeSquareCandBase(int height, int width) {
			List<Sikaku> squareCandBase = new ArrayList<>();
			for (int yIndex = 0; yIndex < height; yIndex++) {
				for (int xIndex = 0; xIndex < width; xIndex++) {
					for (int size = 1; size <= (width < height ? width : height); size++) {
						int maxY = yIndex + size > height ? height - size : yIndex;
						int maxX = xIndex + size > width ? width - size : xIndex;
						for (int y = yIndex; y <= maxY; y++) {
							for (int x = xIndex; x <= maxX; x++) {
								Sikaku sikaku = new Sikaku(new Position(y, x),
										new Position(y + size - 1, x + size - 1));
								squareCandBase.add(sikaku);
							}
						}
					}
				}
			}
			return squareCandBase;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			fixedPosSet = new HashSet<>();
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int bitInfo = Character.getNumericValue(ch);
				int pos1 = bitInfo / 9 % 3;
				int pos2 = bitInfo / 3 % 3;
				int pos3 = bitInfo % 3;
				if (index / getXLength() < getYLength()) {
					masu[index / getXLength()][index % getXLength()] = Masu.getByVal(pos1);
					if (pos1 > 0) {
						fixedPosSet.add(new Position(index / getXLength(), index % getXLength()));
					}
				}
				index++;
				if (index / getXLength() < getYLength()) {
					masu[index / getXLength()][index % getXLength()] = Masu.getByVal(pos2);
					if (pos2 > 0) {
						fixedPosSet.add(new Position(index / getXLength(), index % getXLength()));
					}
				}
				index++;
				if (index / getXLength() < getYLength()) {
					masu[index / getXLength()][index % getXLength()] = Masu.getByVal(pos3);
					if (pos3 > 0) {
						fixedPosSet.add(new Position(index / getXLength(), index % getXLength()));
					}
				}
				index++;
			}
			initCand();
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			squareCand = new ArrayList<>(other.squareCand);
			squareFixed = new ArrayList<>(other.squareFixed);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
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
		 * 黒マスと被る候補は削除。 <br>
		 * 候補が1つだけある白マスは確定に昇格。<br>
		 * 確定のマスは白マス確定＆隣接マスは黒マス確定。<br>
		 * 確定とかぶる候補四角は削除。<br>
		 * 候補がないマスは黒マス確定。<br>
		 */
		private boolean sikakuSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						// 既に確定しているマスを除外
						Position pos = new Position(yIndex, xIndex);
						boolean isFixed = false;
						for (Sikaku fixed : squareFixed) {
							if (fixed.getPosSet().contains(pos)) {
								isFixed = true;
								break;
							}
						}
						if (isFixed) {
							continue;
						}
						// そのマスに対する候補を検索
						Sikaku pickup = null;
						boolean only = true;
						for (Sikaku cand : squareCand) {
							if (cand.getPosSet().contains(pos)) {
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
			}
			for (Sikaku fixed : squareFixed) {
				for (Sikaku otherFixed : squareFixed) {
					if (fixed != otherFixed) {
						if (otherFixed.isDuplicateBig(fixed)) {
							return false;
						}
					}
				}
				for (Iterator<Sikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
					Sikaku cand = iterator.next();
					if (cand.isDuplicateBig(fixed)) {
						iterator.remove();
					}
				}
				for (Position fixedSikakuPos : fixed.getPosSet()) {
					if (masu[fixedSikakuPos.getyIndex()][fixedSikakuPos.getxIndex()] == Masu.BLACK) {
						return false;
					}
					masu[fixedSikakuPos.getyIndex()][fixedSikakuPos.getxIndex()] = Masu.NOT_BLACK;
				}
				for (Position fixedEdgePos : fixed.getEdgePosSet()) {
					if (fixedEdgePos.getyIndex() >= 0 && fixedEdgePos.getyIndex() < getYLength()
							&& fixedEdgePos.getxIndex() >= 0 && fixedEdgePos.getxIndex() < getXLength()) {
						if (masu[fixedEdgePos.getyIndex()][fixedEdgePos.getxIndex()] == Masu.NOT_BLACK) {
							return false;
						}
						masu[fixedEdgePos.getyIndex()][fixedEdgePos.getxIndex()] = Masu.BLACK;
					}
				}
			}
			for (Iterator<Sikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
				Sikaku cand = iterator.next();
				boolean removed = false;
				for (Position candSikakuPos : cand.getPosSet()) {
					if (masu[candSikakuPos.getyIndex()][candSikakuPos.getxIndex()] == Masu.BLACK) {
						iterator.remove();
						removed = true;
						break;
					}
				}
				if (!removed) {
					for (Position candEdgePos : cand.getEdgePosSet()) {
						if (candEdgePos.getyIndex() >= 0 && candEdgePos.getyIndex() < getYLength()
								&& candEdgePos.getxIndex() >= 0 && candEdgePos.getxIndex() < getXLength()) {
							if (masu[candEdgePos.getyIndex()][candEdgePos.getxIndex()] == Masu.NOT_BLACK) {
								iterator.remove();
								break;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 黒2x2禁
		 */
		public boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE && masu4 == Masu.BLACK) {
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position blackPos = new Position(yIndex, xIndex);
						if (blackPosSet.size() == 0) {
							blackPosSet.add(blackPos);
							setContinueBlackPosSet(blackPos, blackPosSet, null);
						} else {
							if (!blackPosSet.contains(blackPos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に白確定でないマスをつなげていく
		 */
		private void setContinueBlackPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueBlackPosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueBlackPosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueBlackPosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueBlackPosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * 
		 * @param i
		 * @param recursive
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!sikakuSolve()) {
				return false;
			}
			if (!pondSolve()) {
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
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public CirclesquareSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public CirclesquareSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?circlesquare/8/8/i30a560000b09093i39010"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new CirclesquareSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 10));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 10 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 10).toString() + "(Lv:" + level + ")";
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
			field.squareCand = virtual2.squareCand;
			field.squareFixed = virtual2.squareFixed;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.squareCand = virtual.squareCand;
			field.squareFixed = virtual.squareFixed;
		}
		return true;
	}

}
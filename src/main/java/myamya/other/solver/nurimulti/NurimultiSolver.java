package myamya.other.solver.nurimulti;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.PenpaEditGeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.PenpaEditLib;
import myamya.other.solver.Solver;

public class NurimultiSolver implements Solver {
	public static class NurimultiGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class NurimultiSolverForGenerator extends NurimultiSolver {
			private final int limit;

			public NurimultiSolverForGenerator(Field field, int limit) {
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
							if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt, 0, 0)) {
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
			protected boolean candSolve(Field field, int recursive, int initY, int initX) {
				if (this.count >= limit) {
					throw new CountOverException();
				} else {
					return super.candSolve(field, recursive, initY, initX);
				}
			}
		}

		static class ExtendedField extends NurimultiSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width, int blackSize) {
				super(height, width, blackSize);
			}

			@Override
			protected boolean notStandAloneSolve() {
				return true;
			}
		}

		private final int blackSize;
		private final int height;
		private final int width;

		public NurimultiGenerator(int height, int width, int blackSize) {
			this.blackSize = blackSize;
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new NurimultiGenerator(8, 8, 3).generate();
		}

		@Override
		public GeneratorResult generate() {
			String solutionStr;
			ExtendedField wkField = new ExtendedField(height, width, blackSize);
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
						numIdxList.add(1);
						numIdxList.add(0);
						for (int masuNum : numIdxList) {
							ExtendedField virtual = new ExtendedField(wkField);
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
							wkField = new ExtendedField(height, width, blackSize);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// まず数字を埋める
				List<Position> notBlackPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							notBlackPosList.add(new Position(yIndex, xIndex));
						}
					}
				}
				Collections.shuffle(notBlackPosList);
				Set<Position> alreadyPosSet = new HashSet<>();
				for (Position pos : notBlackPosList) {
					if (alreadyPosSet.contains(pos)) {
						continue;
					}
					Set<Position> continueWhitePosSet = wkField.getContinueWhitePosSet(pos);
					wkField.numbers[pos.getyIndex()][pos.getxIndex()] = continueWhitePosSet.size();
					alreadyPosSet.addAll(continueWhitePosSet);
				}
				// 解答の記憶
				solutionStr = PenpaEditLib.convertSolutionMasu(wkField.masu);
//				System.out.println(wkField);
				wkField.fixedPosSet.clear();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] == null) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				Field solvingField = new NurimultiSolver.Field(wkField);
				level = new NurimultiSolverForGenerator(solvingField, 3000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width, blackSize);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					break;
				}
			}
			// ヒント数字を含む盤面変換
			String fieldStr = PenpaEditLib.convertNumbersField(wkField.numbers);
//			System.out.println(fieldStr);
//			System.out.println(solutionStr);
			level = (int) Math.sqrt(level / 2 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">penpa-editで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
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
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.(nurimulti:" + blackSize + ")");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new PenpaEditGeneratorResult(status, sb.toString(), link, level, "", fieldStr, solutionStr);

		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 黒マスサイズ
		private final int blackSize;
		// マスの情報
		protected Masu[][] masu;
		// 数字の情報
		protected final Integer[][] numbers;
		// 確定した部屋の位置情報。再調査しないことでスピードアップ
		protected Set<Position> fixedPosSet;

		public Masu[][] getMasu() {
			return masu;
		}

		/**
		 * あるマスからつながる白マスセットを返す。ジェネレータ用 posが白マスであることは事前にチェックしている前提
		 */
		protected Set<Position> getContinueWhitePosSet(Position pos) {
			Set<Position> result = new HashSet<>();
			result.add(pos);
			setContinuePosSet(pos, result, Integer.MAX_VALUE, null);
			return result;
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

		public String getPuzPreURL() {
			return PenpaEditLib.PENPA_EDIT_DUMMY_URL;
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

		public Field(int height, int width, int blackSize) {
			this.blackSize = blackSize;
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			fixedPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
		}

		public Field(String fieldStr, int blackSize) {
			this.blackSize = blackSize;
			masu = PenpaEditLib.getMasu(fieldStr);
			numbers = PenpaEditLib.getNumbers(fieldStr);
			fixedPosSet = new HashSet<>();
			// 数字マスは白マスに
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
				}
			}
		}

		public Field(Field other) {
			blackSize = other.blackSize;
			numbers = other.numbers;
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			fixedPosSet = new HashSet<>(other.fixedPosSet);
		}

		public Field(int height, int width, String param, int blackSize) {
			this.blackSize = blackSize;
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			fixedPosSet = new HashSet<>();
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
		 * 部屋のサイズが超過していたり、別部屋と繋がっていた場合、falseを返す。 部屋が既定サイズに到達している場合、周囲を黒で埋める。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						Position pivot = new Position(yIndex, xIndex);
						if (fixedPosSet.contains(pivot)) {
							continue;
						}
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pivot);
						if (!(setContinuePosSet(pivot, continuePosSet, numbers[yIndex][xIndex], null))) {
							// サイズ超過
							return false;
						} else if (numbers[yIndex][xIndex] == continuePosSet.size()) {
							// サイズ確定。周りを黒に
							fixedPosSet.add(pivot);
							for (Position pos : continuePosSet) {
								if (pos.getyIndex() != 0 && !continuePosSet
										.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
									masu[pos.getyIndex() - 1][pos.getxIndex()] = Masu.BLACK;
								}
								if (pos.getxIndex() != getXLength() - 1 && !continuePosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
									masu[pos.getyIndex()][pos.getxIndex() + 1] = Masu.BLACK;
								}
								if (pos.getyIndex() != getYLength() - 1 && !continuePosSet
										.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
									masu[pos.getyIndex() + 1][pos.getxIndex()] = Masu.BLACK;
								}
								if (pos.getxIndex() != 0 && !continuePosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
									masu[pos.getyIndex()][pos.getxIndex() - 1] = Masu.BLACK;
								}
							}
						} else {
							Set<Position> continueCandPosSet = new HashSet<>();
							continueCandPosSet.add(pivot);
							if (!setContinueCandPosSet(pivot, continueCandPosSet, numbers[yIndex][xIndex], null)) {
								if (numbers[yIndex][xIndex] == continueCandPosSet.size()) {
									// サイズ確定。自分を白、周りを黒に
									fixedPosSet.add(pivot);
									for (Position pos : continueCandPosSet) {
										masu[pos.getyIndex()][pos.getxIndex()] = masu[yIndex][xIndex];
										if (pos.getyIndex() != 0) {
											masu[pos.getyIndex() - 1][pos.getxIndex()] = continueCandPosSet.contains(
													new Position(pos.getyIndex() - 1, pos.getxIndex())) ? Masu.NOT_BLACK
															: Masu.BLACK;
										}
										if (pos.getxIndex() != getXLength() - 1) {
											masu[pos.getyIndex()][pos.getxIndex() + 1] = continueCandPosSet.contains(
													new Position(pos.getyIndex(), pos.getxIndex() + 1)) ? Masu.NOT_BLACK
															: Masu.BLACK;
										}
										if (pos.getyIndex() != getYLength() - 1) {
											masu[pos.getyIndex() + 1][pos.getxIndex()] = continueCandPosSet.contains(
													new Position(pos.getyIndex() + 1, pos.getxIndex())) ? Masu.NOT_BLACK
															: Masu.BLACK;
										}
										if (pos.getxIndex() != 0) {
											masu[pos.getyIndex()][pos.getxIndex() - 1] = continueCandPosSet.contains(
													new Position(pos.getyIndex(), pos.getxIndex() - 1)) ? Masu.NOT_BLACK
															: Masu.BLACK;
										}
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
		 * posを起点に上下左右にtargetと同じ色になりうるマスをつなげていく。 sizeが確保可能とわかった時点でtrueを返す。
		 */
		private boolean setContinueCandPosSet(Position pos, Set<Position> continuePosSet, int size, Direction from) {
			if (size != -1 && continuePosSet.size() > size) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右にtargetと同じ色が確定しているマスをつなげていく。 sizeが超過すると分かった時点でfalseを返す。
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet, int size, Direction from) {
			if (size != -1 && continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスは必ず3マスつながる
		 */
		public boolean kuroSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position pivot = new Position(yIndex, xIndex);
						if (fixedPosSet.contains(pivot)) {
							continue;
						}
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pivot);
						if (!(setContinueBlackPosSet(pivot, continuePosSet, null))) {
							// サイズ超過
							return false;
						} else if (blackSize == continuePosSet.size()) {
							// サイズ確定。周りを白に
							fixedPosSet.add(pivot);
							for (Position pos : continuePosSet) {
								if (pos.getyIndex() != 0 && !continuePosSet
										.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
									masu[pos.getyIndex() - 1][pos.getxIndex()] = Masu.NOT_BLACK;
								}
								if (pos.getxIndex() != getXLength() - 1 && !continuePosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
									masu[pos.getyIndex()][pos.getxIndex() + 1] = Masu.NOT_BLACK;
								}
								if (pos.getyIndex() != getYLength() - 1 && !continuePosSet
										.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
									masu[pos.getyIndex() + 1][pos.getxIndex()] = Masu.NOT_BLACK;
								}
								if (pos.getxIndex() != 0 && !continuePosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
									masu[pos.getyIndex()][pos.getxIndex() - 1] = Masu.NOT_BLACK;
								}
							}
						} else {
							Set<Position> continueCandPosSet = new HashSet<>();
							continueCandPosSet.add(pivot);
							if (!setContinueCandBlackPosSet(pivot, continueCandPosSet, null)) {
								if (blackSize == continueCandPosSet.size()) {
									// サイズ確定。自分を黒、周りを白に
									fixedPosSet.add(pivot);
									for (Position pos : continueCandPosSet) {
										masu[pos.getyIndex()][pos.getxIndex()] = masu[yIndex][xIndex];
										if (pos.getyIndex() != 0) {
											masu[pos.getyIndex() - 1][pos.getxIndex()] = continueCandPosSet.contains(
													new Position(pos.getyIndex() - 1, pos.getxIndex())) ? Masu.BLACK
															: Masu.NOT_BLACK;
										}
										if (pos.getxIndex() != getXLength() - 1) {
											masu[pos.getyIndex()][pos.getxIndex() + 1] = continueCandPosSet.contains(
													new Position(pos.getyIndex(), pos.getxIndex() + 1)) ? Masu.BLACK
															: Masu.NOT_BLACK;
										}
										if (pos.getyIndex() != getYLength() - 1) {
											masu[pos.getyIndex() + 1][pos.getxIndex()] = continueCandPosSet.contains(
													new Position(pos.getyIndex() + 1, pos.getxIndex())) ? Masu.BLACK
															: Masu.NOT_BLACK;
										}
										if (pos.getxIndex() != 0) {
											masu[pos.getyIndex()][pos.getxIndex() - 1] = continueCandPosSet.contains(
													new Position(pos.getyIndex(), pos.getxIndex() - 1)) ? Masu.BLACK
															: Masu.NOT_BLACK;
										}
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
		 * posを起点に上下左右にtargetと同じ色になりうるマスをつなげていく。 sizeが確保可能とわかった時点でtrueを返す。
		 */
		private boolean setContinueCandBlackPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (continuePosSet.size() > blackSize) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandBlackPosSet(nextPos, continuePosSet, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandBlackPosSet(nextPos, continuePosSet, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandBlackPosSet(nextPos, continuePosSet, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandBlackPosSet(nextPos, continuePosSet, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右にtargetと同じ色が確定しているマスをつなげていく。 sizeが超過すると分かった時点でfalseを返す。
		 */
		private boolean setContinueBlackPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (continuePosSet.size() > blackSize) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinueBlackPosSet(nextPos, continuePosSet, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinueBlackPosSet(nextPos, continuePosSet, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinueBlackPosSet(nextPos, continuePosSet, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinueBlackPosSet(nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 数字が0個・2個以上入る島ができてはならない。
		 */
		protected boolean notStandAloneSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null && masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						whitePosSet.add(new Position(yIndex, xIndex));
					}
				}
			}
			while (!whitePosSet.isEmpty()) {
				Position pivot = new ArrayList<>(whitePosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(pivot);
				if (!setContinueCandPosSet(pivot, continuePosSet, null)) {
					return false;
				}
				continuePosSet = new HashSet<>();
				continuePosSet.add(pivot);
				if (!setContinuePosSet(pivot, continuePosSet, null, new HashSet<>())) {
					return false;
				}
				whitePosSet.removeAll(continuePosSet);
			}
			return true;
		}

		/**
		 * posを起点に上下左右に白になりうるマスをつなげていく。 数字を見つけた時点でtrueを返す。
		 */
		private boolean setContinueCandPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右にtargetと同じ色が確定しているマスをつなげていく。 数字を2個見つけた時点でfalseを返す。
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from,
				Set<Position> findNumber) {
			if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
				if (findNumber.isEmpty()) {
					findNumber.add(pos);
				} else {
					return false;
				}
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.DOWN, findNumber)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.LEFT, findNumber)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.UP, findNumber)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT, findNumber)) {
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
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!kuroSolve()) {
				return false;
			}
			if (!notStandAloneSolve()) {
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

	public NurimultiSolver(Field field) {
		this.field = new Field(field);
	}

	// penpa-edit向けコンストラクタ
	public NurimultiSolver(String fieldStr, int blackSize) {
		field = new Field(fieldStr, blackSize);
	}

	public NurimultiSolver(int height, int width, String param, int blackSize) {
		field = new Field(height, width, param, blackSize);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String fieldStr = "square,14,13,38,0,1,1,570,532,1070,1070\n" + "[0,0,0,0]\n"
				+ "[\"1\",\"2\",\"1\"]~zS~[\"\",1]\n"
				+ "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{\"38\":[\"6\",1,\"1\"],\"46\":[\"5\",1,\"1\"],\"49\":[\"4\",1,\"1\"],\"74\":[\"5\",1,\"1\"],\"77\":[\"4\",1,\"1\"],\"99\":[\"5\",1,\"1\"],\"123\":[\"6\",1,\"1\"],\"133\":[\"10\",1,\"1\"],\"140\":[\"2\",1,\"1\"],\"155\":[\"11\",1,\"1\"],\"164\":[\"8\",1,\"1\"],\"189\":[\"2\",1,\"1\"],\"191\":[\"5\",1,\"1\"],\"213\":[\"4\",1,\"1\"],\"238\":[\"10\",1,\"1\"],\"249\":[\"4\",1,\"1\"],\"257\":[\"10\",1,\"1\"],\"262\":[\"9\",1,\"1\"]},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n"
				+ "\n"
				+ "[38,1,1,1,1,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,1,1,1,1]"; // urlを入れれば試せる

		System.out.println(new NurimultiSolver(fieldStr, 2).solve());
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
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt, 0, 0)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count / 2));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 2 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count / 2).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive, int initY, int initX) {
		String str = field.getStateDump();
		for (int yIndex = initY; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = initX; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
			initX = 0;
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive, 0, 0);
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
			if (!candSolve(virtual, recursive - 1, yIndex, xIndex)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.masu[yIndex][xIndex] = Masu.NOT_BLACK;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1, yIndex, xIndex)) {
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
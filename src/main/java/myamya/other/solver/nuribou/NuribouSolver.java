package myamya.other.solver.nuribou;

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
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.PenpaEditLib;
import myamya.other.solver.Solver;

public class NuribouSolver implements Solver {
	public static class NuribouGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class NuribouSolverForGenerator extends NuribouSolver {
			private final int limit;

			public NuribouSolverForGenerator(Field field, int limit) {
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

		static class ExtendedField extends NuribouSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width, boolean isNuribou) {
				super(height, width, isNuribou);
			}

			@Override
			protected boolean notStandAloneSolve() {
				return true;
			}
		}

		private final boolean isNuribou;
		private final int height;
		private final int width;

		public NuribouGenerator(int height, int width, boolean isNuribou) {
			this.isNuribou = isNuribou;
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new NuribouGenerator(8, 8, false).generate();
		}

		/**
		 * いまのところ、ぬりぼうのみ対応
		 */
		@Override
		public GeneratorResult generate() {
			ExtendedField wkField = new ExtendedField(height, width, isNuribou);
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
							wkField = new ExtendedField(height, width, isNuribou);
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
					if (isNuribou) {
						wkField.numbers[pos.getyIndex()][pos.getxIndex()] = continueWhitePosSet.size();
					} else {
						int blackCnt = 0;
						Masu masuUp = pos.getyIndex() == 0 ? Masu.NOT_BLACK
								: wkField.masu[pos.getyIndex() - 1][pos.getxIndex()];
						Masu masuRight = pos.getxIndex() == wkField.getXLength() - 1 ? Masu.NOT_BLACK
								: wkField.masu[pos.getyIndex()][pos.getxIndex() + 1];
						Masu masuDown = pos.getyIndex() == wkField.getYLength() - 1 ? Masu.NOT_BLACK
								: wkField.masu[pos.getyIndex() + 1][pos.getxIndex()];
						Masu masuLeft = pos.getxIndex() == 0 ? Masu.NOT_BLACK
								: wkField.masu[pos.getyIndex()][pos.getxIndex() - 1];
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
						wkField.numbers[pos.getyIndex()][pos.getxIndex()] = blackCnt;
					}
					alreadyPosSet.addAll(continueWhitePosSet);
				}
//				System.out.println(wkField);
				wkField.fixedPosSet.clear();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] == null) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				Field solvingField = new NuribouSolver.Field(wkField);
				level = new NuribouSolverForGenerator(solvingField, 5000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width, isNuribou);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					break;
				}
			}
			// System.out.println(level);
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						if (xIndex == -1 || xIndex == wkField.getXLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
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
						if (yIndex == -1 || yIndex == wkField.getYLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
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
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level + "(nuribou)");
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			System.out.println(url);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}

	}

	public static class Field {
		// ぬりぼうならtrue、うめぼうならfalse
		private final boolean isNuribou;
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
			if (isNuribou) {
				StringBuilder sb = new StringBuilder();
				sb.append("http://pzv.jp/p.html?nuribou/" + getXLength() + "/" + getYLength() + "/");
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
			} else {
				return PenpaEditLib.PENPA_EDIT_DUMMY_URL;
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

		public Field(int height, int width, boolean isNuribou) {
			this.isNuribou = isNuribou;
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			fixedPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
		}

		public Field(String fieldStr) {
			isNuribou = false;
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
			isNuribou = other.isNuribou;
			numbers = other.numbers;
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			fixedPosSet = new HashSet<>(other.fixedPosSet);
		}

		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		public Field(int height, int width, String param) {
			isNuribou = true;
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
					// 16 - 255は '-'
					// 256 - 999は '+'
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
							capacity = Integer
									.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3), 16);
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
		 * ぬりぼうでは、部屋のサイズが超過していたり、別部屋と繋がっていた場合、falseを返す。 部屋が既定サイズに到達している場合、周囲を黒で埋める。
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
		 * 黒マスは必ず棒状になる。ならない場合falseを返す。
		 */
		public boolean stickSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu4 == Masu.BLACK) {
						if (masu2 == Masu.BLACK || masu3 == Masu.BLACK) {
							return false;
						}
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu4 == Masu.SPACE) {
						if (masu2 == Masu.BLACK || masu3 == Masu.BLACK) {
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
					}
					if (masu1 == Masu.SPACE && masu4 == Masu.BLACK) {
						if (masu2 == Masu.BLACK || masu3 == Masu.BLACK) {
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (masu2 == Masu.BLACK && masu3 == Masu.BLACK) {
						if (masu1 == Masu.BLACK || masu4 == Masu.BLACK) {
							return false;
						}
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
						masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu2 == Masu.BLACK && masu3 == Masu.SPACE) {
						if (masu1 == Masu.BLACK || masu4 == Masu.BLACK) {
							masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
						}
					}
					if (masu2 == Masu.SPACE && masu3 == Masu.BLACK) {
						if (masu1 == Masu.BLACK || masu4 == Masu.BLACK) {
							masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * うめぼうでは、数字は周りのくろますの数。矛盾する場合false。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
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
							// 黒マス過剰
							return false;
						}
						if (numbers[yIndex][xIndex] > 4 - whiteCnt) {
							// 黒マス不足
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
		 * 同じ長さの棒は角を共有しない。
		 */
		public boolean cornerSolve() {
			Set<Position> alreadyServeyPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position pos = new Position(yIndex, xIndex);
						if (alreadyServeyPosSet.contains(pos)) {
							continue;
						}
						Set<Position> continueBlackPosSet = new HashSet<>();
						continueBlackPosSet.add(pos);
						if (setContinueBlackPosSet(pos, continueBlackPosSet, null)) {
							// サイズが決まっている棒
							for (Position continueBlackPos : continueBlackPosSet) {
								Masu masuUpRight = continueBlackPos.getyIndex() == 0
										|| continueBlackPos.getxIndex() == getXLength() - 1 ? Masu.NOT_BLACK
												: masu[continueBlackPos.getyIndex() - 1][continueBlackPos.getxIndex()
														+ 1];
								Masu masuRightDown = continueBlackPos.getxIndex() == getXLength() - 1
										|| continueBlackPos.getyIndex() == getYLength() - 1 ? Masu.NOT_BLACK
												: masu[continueBlackPos.getyIndex() + 1][continueBlackPos.getxIndex()
														+ 1];
								Masu masuDownLeft = continueBlackPos.getyIndex() == getYLength() - 1
										|| continueBlackPos.getxIndex() == 0 ? Masu.NOT_BLACK
												: masu[continueBlackPos.getyIndex() + 1][continueBlackPos.getxIndex()
														- 1];
								Masu masuLeftUp = continueBlackPos.getxIndex() == 0 || yIndex == 0 ? Masu.NOT_BLACK
										: masu[continueBlackPos.getyIndex() - 1][continueBlackPos.getxIndex() - 1];
								if (masuUpRight == Masu.BLACK) {
									Position targetPos = new Position(continueBlackPos.getyIndex() - 1,
											continueBlackPos.getxIndex() + 1);
									if (!alreadyServeyPosSet.contains(targetPos)) {
										Set<Position> continueBlackTargetPosSet = new HashSet<>();
										continueBlackTargetPosSet.add(targetPos);
										if (setContinueBlackPosSet(targetPos, continueBlackTargetPosSet, null)) {
											if (continueBlackTargetPosSet.size() == continueBlackPosSet.size()) {
												return false;
											}
										}
									}
								}
								if (masuRightDown == Masu.BLACK) {
									Position targetPos = new Position(continueBlackPos.getyIndex() + 1,
											continueBlackPos.getxIndex() + 1);
									if (!alreadyServeyPosSet.contains(targetPos)) {
										Set<Position> continueBlackTargetPosSet = new HashSet<>();
										continueBlackTargetPosSet.add(targetPos);
										if (setContinueBlackPosSet(targetPos, continueBlackTargetPosSet, null)) {
											if (continueBlackTargetPosSet.size() == continueBlackPosSet.size()) {
												return false;
											}
										}
									}
								}
								if (masuDownLeft == Masu.BLACK) {
									Position targetPos = new Position(continueBlackPos.getyIndex() + 1,
											continueBlackPos.getxIndex() - 1);
									if (!alreadyServeyPosSet.contains(targetPos)) {
										Set<Position> continueBlackTargetPosSet = new HashSet<>();
										continueBlackTargetPosSet.add(targetPos);
										if (setContinueBlackPosSet(targetPos, continueBlackTargetPosSet, null)) {
											if (continueBlackTargetPosSet.size() == continueBlackPosSet.size()) {
												return false;
											}
										}
									}
								}
								if (masuLeftUp == Masu.BLACK) {
									Position targetPos = new Position(continueBlackPos.getyIndex() - 1,
											continueBlackPos.getxIndex() - 1);
									if (!alreadyServeyPosSet.contains(targetPos)) {
										Set<Position> continueBlackTargetPosSet = new HashSet<>();
										continueBlackTargetPosSet.add(targetPos);
										if (setContinueBlackPosSet(targetPos, continueBlackTargetPosSet, null)) {
											if (continueBlackTargetPosSet.size() == continueBlackPosSet.size()) {
												return false;
											}
										}
									}
								}
							}
						}
						alreadyServeyPosSet.addAll(continueBlackPosSet);
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定のマスを無制限につなぐが、 不確定マスに隣接したらfalseを返す。
		 */
		private boolean setContinueBlackPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)) {
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE) {
						return false;
					} else if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
						continuePosSet.add(nextPos);
						if (!setContinueBlackPosSet(nextPos, continuePosSet, Direction.DOWN)) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)) {
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE) {
						return false;
					} else if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
						continuePosSet.add(nextPos);
						if (!setContinueBlackPosSet(nextPos, continuePosSet, Direction.LEFT)) {
							return false;
						}
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)) {
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE) {
						return false;
					} else if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
						continuePosSet.add(nextPos);
						if (!setContinueBlackPosSet(nextPos, continuePosSet, Direction.UP)) {
							return false;
						}
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)) {
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE) {
						return false;
					} else if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
						continuePosSet.add(nextPos);
						if (!setContinueBlackPosSet(nextPos, continuePosSet, Direction.RIGHT)) {
							return false;
						}
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
			if (isNuribou && !roomSolve()) {
				return false;
			}
			if (!isNuribou && !numberSolve()) {
				return false;
			}
			if (!stickSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!cornerSolve()) {
					return false;
				}
				if (!notStandAloneSolve()) {
					return false;
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

	protected final Field field;
	protected int count;

	public NuribouSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public NuribouSolver(Field field) {
		this.field = new Field(field);
	}

	// penpa-edit向けコンストラクタ
	public NuribouSolver(String fieldStr) {
		field = new Field(fieldStr);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
//		String fieldStr = "square,9,9,38,0,1,1,380,380,84,84\n" + "[0,0,0,0]\n" + "[\"1\",\"2\",\"1\"]~zS~[\"\",1]\n"
//				+ "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{\"35\":[\"\",1,\"1\"],\"36\":[\"1\",1,\"1\"],\"45\":[\"4\",1,\"1\"],\"60\":[\"1\",1,\"1\"],\"70\":[\"3\",1,\"1\"],\"88\":[\"1\",1,\"1\"],\"95\":[\"1\",1,\"1\"],\"127\":[\"3\",1,\"1\"],\"133\":[\"3\",1,\"1\"],\"136\":[\"2\",1,\"1\"]},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n"
//				+ "\n"
//				+ "[28,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1]";
//		System.out.println(new NuribouSolver(fieldStr).solve());

		String url = "https://puzz.link/p?nuribou/20/15/h5o6zs6k3i3h6zg4p4zi.pbzl7h3zz4k4l9v7zn4h.l4k4o4q7i2"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new NuribouSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count).toString() + "(Lv:" + level + ")";
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
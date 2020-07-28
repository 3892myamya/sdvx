package myamya.other.solver.mochikoro;

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

public class MochikoroSolver implements Solver {

	public static class MochikoroGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		/**
		 * TODO スクリンは候補情報を使うのが解消できてないので未対応…
		 */
		static class ExtendedField extends MochikoroSolver.Field {

			// マスの情報
			protected Masu[][] masu;

			public ExtendedField(Field other) {
				super(other);
				masu = new Masu[other.getYLength()][other.getXLength()];
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						masu[yIndex][xIndex] = other.getMasu()[yIndex][xIndex];
					}
				}
			}

			public ExtendedField(int height, int width, boolean isMochinyoro, boolean isScrin) {
				super(height, width, isMochinyoro, isScrin);
				masu = new Masu[height][width];
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
			}

			public Masu[][] getMasu() {
				return this.masu;
			}

			/**
			 *  作問段階では候補情報は使わない代わりに切りかけ調査をする
			 */
			protected boolean sikakuSolve() {
				boolean advance = false;
				for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
						Masu masu1 = masu[yIndex][xIndex];
						Masu masu2 = masu[yIndex][xIndex + 1];
						Masu masu3 = masu[yIndex + 1][xIndex];
						Masu masu4 = masu[yIndex + 1][xIndex + 1];
						if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
								&& masu4 == Masu.BLACK) {
							return false;
						}
						if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.BLACK
								&& masu4 == Masu.NOT_BLACK) {
							return false;
						}
						if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.NOT_BLACK
								&& masu4 == Masu.NOT_BLACK) {
							return false;
						}
						if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
								&& masu4 == Masu.NOT_BLACK) {
							return false;
						}
						if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
								&& masu4 == Masu.SPACE) {
							advance = true;
							masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.SPACE
								&& masu4 == Masu.NOT_BLACK) {
							advance = true;
							masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
						}
						if (masu1 == Masu.NOT_BLACK && masu2 == Masu.SPACE && masu3 == Masu.NOT_BLACK
								&& masu4 == Masu.NOT_BLACK) {
							advance = true;
							masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
						}
						if (masu1 == Masu.SPACE && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
								&& masu4 == Masu.NOT_BLACK) {
							advance = true;
							masu[yIndex][xIndex] = Masu.NOT_BLACK;
						}
					}
				}
				if (advance) {
					return sikakuSolve();
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

			/**
			 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
			 */
			protected void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
				if (pos.getyIndex() != 0 && from != Direction.UP) {
					Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
					if (!continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
						continuePosSet.add(nextPos);
						setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
					}
				}
				if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
					if (!continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
						continuePosSet.add(nextPos);
						setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
					}
				}
				if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
					Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
					if (!continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
						continuePosSet.add(nextPos);
						setContinuePosSet(nextPos, continuePosSet, Direction.UP);
					}
				}
				if (pos.getxIndex() != 0 && from != Direction.LEFT) {
					Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
					if (!continuePosSet.contains(nextPos)
							&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
						continuePosSet.add(nextPos);
						setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
					}
				}
			}
		}

		static class MochikoroSolverForGenerator extends MochikoroSolver {
			private final int limit;

			public MochikoroSolverForGenerator(Field field, int limit) {
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
		private final boolean isMochinyoro;
		private final boolean isScrin;

		public MochikoroGenerator(int height, int width, boolean isMochinyoro, boolean isScrin) {
			this.height = height;
			this.width = width;
			this.isMochinyoro = isMochinyoro;
			this.isScrin = isScrin;
		}

		public static void main(String[] args) {
			new MochikoroGenerator(10, 10, false, false).generate();
		}

		@Override
		public GeneratorResult generate() {
			ExtendedField wkField = new ExtendedField(height, width, isMochinyoro, isScrin);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < height * width; i++) {
				indexList.add(i);
			}
			// Collections.shuffle(indexList);
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
							wkField = new ExtendedField(height, width, isMochinyoro, isScrin);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// まず数字を埋める
				Set<Position> alreadyPosSet = new HashSet<>();
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.getMasu()[yIndex][xIndex] == Masu.NOT_BLACK) {
							Position whitePos = new Position(yIndex, xIndex);
							if (alreadyPosSet.contains(whitePos)) {
								//
							} else {
								Set<Position> whitePosSet = new HashSet<>();
								whitePosSet.add(whitePos);
								wkField.setContinuePosSet(whitePos, whitePosSet, null);
								Position targetPos = new ArrayList<>(whitePosSet)
										.get((int) (Math.random() * whitePosSet.size()));
								wkField.numbers[targetPos.getyIndex()][targetPos.getxIndex()] = whitePosSet.size();
								numberPosList.add(targetPos);
								if (isScrin) {
									numberPosList.add(targetPos);
								}
								alreadyPosSet.addAll(whitePosSet);
							}
						}
					}
				}
				// マスを戻す
				wkField.initCand();
				// 解けるかな？
				level = new MochikoroSolverForGenerator(wkField, 200).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width, isMochinyoro, isScrin);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						MochikoroSolver.Field virtual = new MochikoroSolver.Field(wkField, true);
						if (isScrin && virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] != -1) {
							virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = -1;
						} else {
							virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						}
						virtual.initCand();
						int solveResult = new MochikoroSolverForGenerator(virtual, 5000).solve2();
						if (solveResult != -1) {
							wkField.numbers[numberPos.getyIndex()][numberPos
									.getxIndex()] = virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()];
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 3);
			String status = "Lv:" + level + "の問題を獲得！(数字：" + wkField.getHintCount().split("/")[0] + ")";
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
						if (xIndex == -1 || xIndex == wkField.getXLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
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
						if (yIndex == -1 || yIndex == wkField.getYLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"green\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">"
							+ "</line>");
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
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4)
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

		// もちにょろ
		final protected boolean isMochinyoro;
		// スクリン
		final protected boolean isScrin;
		// 数字の情報
		final protected Integer[][] numbers;
		// 四角の配置の候補
		protected List<Sikaku> squareCand;
		// 確定した四角候補
		protected List<Sikaku> squareFixed;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?" + (isMochinyoro ? "mochinyoro"
					: isScrin ? "scrin" : "mochikoro") + "/" + getXLength() + "/" + getYLength() + "/");
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
						numStr = Integer.toHexString(num);
						if (numStr.length() == 2) {
							numStr = "-" + numStr;
						} else if (numStr.length() == 3) {
							numStr = "+" + numStr;
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

		public Field(int height, int width, boolean isMochinyoro, boolean isScrin) {
			numbers = new Integer[height][width];
			this.isMochinyoro = isMochinyoro;
			this.isScrin = isScrin;
			initCand();
		}

		/**
		 * 部屋のきりかたの候補を初期化する。
		 */
		protected void initCand() {
			squareCand = new ArrayList<>();
			squareFixed = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int minY = yIndex;
					int minX = xIndex;
					for (int maxY = minY; maxY < getYLength(); maxY++) {
						for (int maxX = minX; maxX < getXLength(); maxX++) {
							Sikaku sikaku = new Sikaku(new Position(minY, minX), new Position(maxY, maxX));
							// 数字を2個以上取り込んだり、サイズが違う場合は候補から消す
							boolean alreadyNumber = false;
							boolean isOk = true;
							for (Position pos : sikaku.getPosSet()) {
								if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
									if (alreadyNumber
											|| (numbers[pos.getyIndex()][pos.getxIndex()] != -1
													&& numbers[pos.getyIndex()][pos.getxIndex()] != sikaku
															.getAreaSize())) {
										isOk = false;
										break;
									}
									alreadyNumber = true;
								}
							}
							// 数字にこする候補は消す。
							if (isOk && minY != 0) {
								for (int targetX = minX; targetX <= maxX; targetX++) {
									if (numbers[minY - 1][targetX] != null) {
										isOk = false;
										break;
									}
								}
							}
							if (isOk && maxY != getYLength() - 1) {
								for (int targetX = minX; targetX <= maxX; targetX++) {
									if (numbers[maxY + 1][targetX] != null) {
										isOk = false;
										break;
									}
								}
							}
							if (isOk && minX != 0) {
								for (int targetY = minY; targetY <= maxY; targetY++) {
									if (numbers[targetY][minX - 1] != null) {
										isOk = false;
										break;
									}
								}
							}
							if (isOk && maxX != getXLength() - 1) {
								for (int targetY = minY; targetY <= maxY; targetY++) {
									if (numbers[targetY][maxX + 1] != null) {
										isOk = false;
										break;
									}
								}
							}
							if (isOk) {
								squareCand.add(sikaku);
							}
						}
					}
				}
			}
		}

		public Field(int height, int width, String param, boolean isMochinyoro, boolean isScrin) {
			numbers = new Integer[height][width];
			this.isMochinyoro = isMochinyoro;
			this.isScrin = isScrin;
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
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
			initCand();
		}

		public Field(Field other) {
			numbers = other.numbers;
			isMochinyoro = other.isMochinyoro;
			isScrin = other.isScrin;
			squareCand = new ArrayList<>(other.squareCand);
			squareFixed = new ArrayList<>(other.squareFixed);
		}

		public Field(Field other, boolean flag) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			isMochinyoro = other.isMochinyoro;
			isScrin = other.isScrin;
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
					masu[yIndex][xIndex] = Masu.BLACK;
				}
			}
			for (Sikaku fixed : squareFixed) {
				for (int yIndex = fixed.getLeftUp().getyIndex(); yIndex <= fixed.getRightDown().getyIndex(); yIndex++) {
					for (int xIndex = fixed.getLeftUp().getxIndex(); xIndex <= fixed.getRightDown()
							.getxIndex(); xIndex++) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
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
			if (!connectSolve()) {
				return false;
			}
			return true;
		}

		/**
		 * 確定した四角がある場合、それとかぶる・接する候補を消す。
		 */
		protected boolean sikakuSolve() {
			for (Sikaku fixed : squareFixed) {
				Sikaku removeSikaku1 = new Sikaku(
						new Position(fixed.getLeftUp().getyIndex() - 1, fixed.getLeftUp().getxIndex()),
						new Position(fixed.getRightDown().getyIndex() + 1, fixed.getRightDown().getxIndex()));
				Sikaku removeSikaku2 = new Sikaku(
						new Position(fixed.getLeftUp().getyIndex(), fixed.getLeftUp().getxIndex() - 1),
						new Position(fixed.getRightDown().getyIndex(), fixed.getRightDown().getxIndex() + 1));
				for (Iterator<Sikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
					Sikaku sikaku = iterator.next();
					if (sikaku.isDuplicate(removeSikaku1) || sikaku.isDuplicate(removeSikaku2)) {
						iterator.remove();
					}
				}
			}
			return true;
		}

		/**
		 * マスを使ったチェック。
		 */
		public boolean connectSolve() {
			Masu[][] masu = getMasu();
			// 数字のマスの黒マス禁
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK && numbers[yIndex][xIndex] != null) {
						return false;
					}
				}
			}
			// 黒マス2x2禁
			if (!isScrin) {
				for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
						Masu masu1 = masu[yIndex][xIndex];
						Masu masu2 = masu[yIndex][xIndex + 1];
						Masu masu3 = masu[yIndex + 1][xIndex];
						Masu masu4 = masu[yIndex + 1][xIndex + 1];
						if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
							return false;
						}
					}
				}
			} else {
				//スクリン用
				for (Sikaku fixed : squareFixed) {
					int whiteCnt = 0;
					int spaceCnt = 0;
					int minY = fixed.getLeftUp().getyIndex();
					int minX = fixed.getLeftUp().getxIndex();
					int maxY = fixed.getRightDown().getyIndex();
					int maxX = fixed.getRightDown().getxIndex();
					Masu masuUL = minY == 0 || minX == 0 ? Masu.BLACK : masu[minY - 1][minX - 1];
					Masu masuUR = minY == 0 || maxX == getXLength() - 1 ? Masu.BLACK : masu[minY - 1][maxX + 1];
					Masu masuDR = maxY == getYLength() - 1 || maxX == getXLength() - 1 ? Masu.BLACK
							: masu[maxY + 1][maxX + 1];
					Masu masuDL = maxY == getYLength() - 1 || minX == 0 ? Masu.BLACK : masu[maxY + 1][minX - 1];
					if (masuUL == Masu.NOT_BLACK) {
						whiteCnt++;
					} else if (masuUL == Masu.SPACE) {
						spaceCnt++;
					}
					if (masuUR == Masu.NOT_BLACK) {
						whiteCnt++;
					} else if (masuUR == Masu.SPACE) {
						spaceCnt++;
					}
					if (masuDR == Masu.NOT_BLACK) {
						whiteCnt++;
					} else if (masuDR == Masu.SPACE) {
						spaceCnt++;
					}
					if (masuDL == Masu.NOT_BLACK) {
						whiteCnt++;
					} else if (masuDL == Masu.SPACE) {
						spaceCnt++;
					}
					if (whiteCnt > 2 || whiteCnt + spaceCnt < 2) {
						return false;
					}
				}
			}
			// 隣接チェック
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
							whitePosSet.add(whitePos);
							setContinueDiagonalPosSet(masu, whitePos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(whitePos)) {
								return false;
							}
						}
					}
				}
			}
			if (isMochinyoro) {
				// もちにょろ用
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							Position blackPos = new Position(yIndex, xIndex);
							Set<Position> blackPosSet = new HashSet<>();
							blackPosSet.add(blackPos);
							if (!setContinuePosSet(masu, blackPos, blackPosSet, null)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右ななめに黒確定でないマスを無制限につなげていく。
		 */
		protected void setContinueDiagonalPosSet(Masu[][] masu, Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(masu, nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(masu, nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(masu, nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(masu, nextPos, continuePosSet, Direction.RIGHT);
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(masu, nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(masu, nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(masu, nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(masu, nextPos, continuePosSet, null);
				}
			}
		}

		/**
		 * posを起点に上下左右に白確定でないマスを無制限につなげていく。
		 * pivotPosとy・xともに違うマスが見つかればtrueを返す。
		 */
		protected boolean setContinuePosSet(Masu[][] masu, Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					for (Position cand : continuePosSet) {
						if (nextPos.getyIndex() != cand.getyIndex() && nextPos.getxIndex() != cand.getxIndex()) {
							return true;
						}
					}
					continuePosSet.add(nextPos);
					if (setContinuePosSet(masu, nextPos, continuePosSet, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					for (Position cand : continuePosSet) {
						if (nextPos.getyIndex() != cand.getyIndex() && nextPos.getxIndex() != cand.getxIndex()) {
							return true;
						}
					}
					continuePosSet.add(nextPos);
					if (setContinuePosSet(masu, nextPos, continuePosSet, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					for (Position cand : continuePosSet) {
						if (nextPos.getyIndex() != cand.getyIndex() && nextPos.getxIndex() != cand.getxIndex()) {
							return true;
						}
					}
					continuePosSet.add(nextPos);
					if (setContinuePosSet(masu, nextPos, continuePosSet, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					for (Position cand : continuePosSet) {
						if (nextPos.getyIndex() != cand.getyIndex() && nextPos.getxIndex() != cand.getxIndex()) {
							return true;
						}
					}
					continuePosSet.add(nextPos);
					if (setContinuePosSet(masu, nextPos, continuePosSet, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		public boolean isSolved() {
			return squareCand.size() == 0 && solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public MochikoroSolver(int height, int width, String param, boolean isMochinyoro, boolean isScrin) {
		long start = System.nanoTime();
		field = new Field(height, width, param, isMochinyoro, isScrin);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public MochikoroSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "http://pzv.jp/p.html?mochikoro/10/10/o23scg1r3p1g1i2n2h2j1k2l2k3m"; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new MochikoroSolver(height, width, param, params[params.length - 4].contains("mochinyoro"),
				params[params.length - 4].contains("scrin")).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		System.out.println(field);
		while (!field.isSolved()) {
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				System.out.println(field);
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				System.out.println(field);
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
		for (Iterator<Sikaku> iterator = field.squareCand.iterator(); iterator
				.hasNext();) {
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
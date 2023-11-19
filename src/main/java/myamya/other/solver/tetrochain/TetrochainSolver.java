package myamya.other.solver.tetrochain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.FixedShape;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class TetrochainSolver implements Solver {

	public static class TetrochainGenerator implements Generator {

		static class TetrochainSolverForGenerator extends TetrochainSolver {

			private final int limit;

			public TetrochainSolverForGenerator(Field field, int limit) {
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

		public TetrochainGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new TetrochainGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			TetrochainSolver.Field wkField = new Field(height, width);
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
							TetrochainSolver.Field virtual = new Field(wkField);
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
							wkField = new Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め
				// ランダムな方向を向かせるが、壁際のマスは外に向かないようにする。
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							List<Direction> candDirection = new ArrayList<>();
							if (yIndex != 0) {
								candDirection.add(Direction.UP);
							}
							if (xIndex != wkField.getXLength() - 1) {
								candDirection.add(Direction.RIGHT);
							}
							if (yIndex != wkField.getYLength() - 1) {
								candDirection.add(Direction.DOWN);
							}
							if (xIndex != 0) {
								candDirection.add(Direction.LEFT);
							}
							int idx = 0;
							int blackCnt = 0;
							Direction direction = candDirection.get((int) (Math.random() * candDirection.size()));
							if (direction == Direction.UP) {
								while (yIndex - 1 - idx >= 0) {
									Position pos = new Position(yIndex - 1 - idx, xIndex);
									if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
										blackCnt++;
									}
									idx++;
								}
							}
							if (direction == Direction.RIGHT) {
								while (xIndex + 1 + idx < wkField.getXLength()) {
									Position pos = new Position(yIndex, xIndex + 1 + idx);
									if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
										blackCnt++;
									}
									idx++;
								}
							}
							if (direction == Direction.DOWN) {
								while (yIndex + 1 + idx < wkField.getYLength()) {
									Position pos = new Position(yIndex + 1 + idx, xIndex);
									if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
										blackCnt++;
									}
									idx++;
								}
							}
							if (direction == Direction.LEFT) {
								while (xIndex - 1 - idx >= 0) {
									Position pos = new Position(yIndex, xIndex - 1 - idx);
									if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
										blackCnt++;
									}
									idx++;
								}
							}
							wkField.arrows[yIndex][xIndex] = new Arrow(direction, blackCnt);
							numberPosList.add(new Position(yIndex, xIndex));
						}
					}
				}
				System.out.println(wkField);
				// マスを戻す
				boolean existBlack = false;
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							existBlack = true;
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				// 全白マス問題禁止
				if (!existBlack) {
					wkField = new TetrochainSolver.Field(height, width);
					index = 0;
					continue;
				}
				// 解けるかな？
				level = new TetrochainSolverForGenerator(new TetrochainSolver.Field(wkField), 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new TetrochainSolver.Field(height, width);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						TetrochainSolver.Field virtual = new TetrochainSolver.Field(wkField);
						virtual.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
						virtual.arrows[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						int solveResult = new TetrochainSolverForGenerator(virtual, 10000).solve2();
						if (solveResult != -1) {
							wkField.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
							wkField.arrows[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(矢印：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
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
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					Arrow oneArrow = wkField.getArrows()[yIndex][xIndex];
					if (oneArrow != null) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize - 2)
								+ "\" textLength=\"" + (baseSize - 2) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneArrow.toStringForweb() + "</text>");

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

	/**
	 * 矢印のマス
	 */
	public static class Arrow {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		private final Direction direction;
		private final int count;

		public Arrow(Direction direction, int count) {
			this.direction = direction;
			this.count = count;
		}

		public Direction getDirection() {
			return direction;
		}

		public int getCount() {
			return count;
		}

		@Override
		public String toString() {
			return (count >= 10 ? String.valueOf(count) : direction.toString() + count);
		}

		public String toStringForweb() {
			if (count == -1) {
				return "？";
			}
			String wkstr = String.valueOf(count);
			int index = HALF_NUMS.indexOf(wkstr);
			if (index >= 0) {
				wkstr = FULL_NUMS.substring(index / 2, index / 2 + 1);
			}
			return direction.getDirectString() + wkstr;
		}
	}

	public static class Field {

		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
		// マスの情報
		private Masu[][] masu;
		// 矢印の情報
		private final Arrow[][] arrows;

		public Arrow[][] getArrows() {
			return arrows;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://puzz.link/p?tetrochain/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (arrows[yIndex][xIndex] == null) {
					interval++;
					if (interval == 26) {
						sb.append("z");
						interval = 0;
					}
				} else {
					String numStr = null;
					int numTop;
					Arrow arrow = arrows[yIndex][xIndex];
					if (arrow != null) {
						numTop = arrow.getDirection().toNum();
						if (arrow.count >= 16) {
							numTop = numTop + 5;
						}
						numStr = numTop + Integer.toHexString(arrow.getCount());
					}
					if (interval == 0) {
						sb.append(numStr);
					} else {
						sb.append(ALPHABET.substring(interval - 1, interval));
						sb.append(numStr);
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
			int cnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null) {
						cnt++;
					}
				}
			}
			return String.valueOf(cnt);
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			arrows = new Arrow[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			int index = 0;
			Direction direction = null;
			boolean adjust = false;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				if (direction == null) {
					int interval = ALPHABET.indexOf(ch) + 1;
					if (interval != 0) {
						index = index + interval;
					} else {
						int val = Character.getNumericValue(ch);
						if (5 <= val && val <= 9) {
							val = val - 5;
							adjust = true;
						}
						direction = Direction.getByNum(val);
						if (direction == null) {
							if (adjust) {
								i++;
							}
							index++;
							i++;
							adjust = false;
						}
					}
				} else {
					Arrow arrow;
					if (adjust) {
						i++;
						arrow = new Arrow(direction,
								Character.getNumericValue(ch) * 16 + Character.getNumericValue(param.charAt(i)));
					} else {
						arrow = new Arrow(direction, Character.getNumericValue(ch));
					}
					arrows[index / getXLength()][index % getXLength()] = arrow;
					masu[index / getXLength()][index % getXLength()] = Masu.NOT_BLACK;
					adjust = false;
					index++;
					direction = null;
				}
			}
		}

		public Field(int height, int width) {
			masu = new Masu[height][width];
			arrows = new Arrow[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			arrows = new Arrow[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					arrows[yIndex][xIndex] = other.arrows[yIndex][xIndex];
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null) {
						sb.append(arrows[yIndex][xIndex]);
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
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!countSolve()) {
				return false;
			}
			if (!arrowSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();

			} else {
				if (!chblockSolve()) {
					return false;
				}
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 連結チェック。 超過や不足が確定したらfalseを返す。
		 */
		private boolean countSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pivot);
						if (!(setContinuePosSet(pivot, continuePosSet, 4, null))) {
							// サイズ超過
							return false;
						} else if (4 == continuePosSet.size()) {
							// サイズ確定。周りを白に
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
							if (!setContinueCandPosSet(pivot, continueCandPosSet, 4, null)) {
								if (4 == continueCandPosSet.size()) {
									// サイズ確定。自分を黒、周りを白に
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
		 * posを起点に黒になりうるマスをつなげていく。 sizeが確保可能とわかった時点でtrueを返す。
		 */
		private boolean setContinueCandPosSet(Position pos, Set<Position> continuePosSet, int size, Direction from) {
			if (size != -1 && continuePosSet.size() > size) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK)
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
		 * posを起点に上下左右に黒確定しているマスをつなげていく。sizeが超過すると分かった時点でfalseを返す。
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet, int size, Direction from) {
			if (size != -1 && continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 矢印は黒マスの数をさす。満たさない場合false。
		 */
		private boolean arrowSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null && arrows[yIndex][xIndex].getCount() != -1) {
						Arrow arrow = arrows[yIndex][xIndex];
						int idx = 0;
						int blackCnt = 0;
						int spaceCnt = 0;
						if (arrow.getDirection() == Direction.UP) {
							while (yIndex - 1 - idx >= 0) {
								if (masu[yIndex - 1 - idx][xIndex] == Masu.BLACK) {
									blackCnt++;
								}
								if (masu[yIndex - 1 - idx][xIndex] == Masu.SPACE) {
									spaceCnt++;
								}
								idx++;
							}
						} else if (arrow.getDirection() == Direction.RIGHT) {
							while (xIndex + 1 + idx < getXLength()) {
								if (masu[yIndex][xIndex + 1 + idx] == Masu.BLACK) {
									blackCnt++;
								}
								if (masu[yIndex][xIndex + 1 + idx] == Masu.SPACE) {
									spaceCnt++;
								}
								idx++;
							}
						} else if (arrow.getDirection() == Direction.DOWN) {
							while (yIndex + 1 + idx < getYLength()) {
								if (masu[yIndex + 1 + idx][xIndex] == Masu.BLACK) {
									blackCnt++;
								}
								if (masu[yIndex + 1 + idx][xIndex] == Masu.SPACE) {
									spaceCnt++;
								}
								idx++;
							}
						} else if (arrow.getDirection() == Direction.LEFT) {
							while (xIndex - 1 - idx >= 0) {
								if (masu[yIndex][xIndex - 1 - idx] == Masu.BLACK) {
									blackCnt++;
								}
								if (masu[yIndex][xIndex - 1 - idx] == Masu.SPACE) {
									spaceCnt++;
								}
								idx++;
							}
						}
						if (arrow.getCount() < blackCnt || arrow.getCount() > blackCnt + spaceCnt) {
							return false;
						}
						if (arrow.getCount() == blackCnt) {
							idx = 0;
							if (arrow.getDirection() == Direction.UP) {
								while (yIndex - 1 - idx >= 0) {
									if (masu[yIndex - 1 - idx][xIndex] == Masu.SPACE) {
										masu[yIndex - 1 - idx][xIndex] = Masu.NOT_BLACK;
									}
									idx++;
								}
							} else if (arrow.getDirection() == Direction.RIGHT) {
								while (xIndex + 1 + idx < getXLength()) {
									if (masu[yIndex][xIndex + 1 + idx] == Masu.SPACE) {
										masu[yIndex][xIndex + 1 + idx] = Masu.NOT_BLACK;
									}
									idx++;
								}
							} else if (arrow.getDirection() == Direction.DOWN) {
								while (yIndex + 1 + idx < getYLength()) {
									if (masu[yIndex + 1 + idx][xIndex] == Masu.SPACE) {
										masu[yIndex + 1 + idx][xIndex] = Masu.NOT_BLACK;
									}
									idx++;
								}
							} else if (arrow.getDirection() == Direction.LEFT) {
								while (xIndex - 1 - idx >= 0) {
									if (masu[yIndex][xIndex - 1 - idx] == Masu.SPACE) {
										masu[yIndex][xIndex - 1 - idx] = Masu.NOT_BLACK;
									}
									idx++;
								}
							}
						}
						if (arrow.getCount() == blackCnt + spaceCnt) {
							idx = 0;
							if (arrow.getDirection() == Direction.UP) {
								while (yIndex - 1 - idx >= 0) {
									if (masu[yIndex - 1 - idx][xIndex] == Masu.SPACE) {
										masu[yIndex - 1 - idx][xIndex] = Masu.BLACK;
									}
									idx++;
								}
							} else if (arrow.getDirection() == Direction.RIGHT) {
								while (xIndex + 1 + idx < getXLength()) {
									if (masu[yIndex][xIndex + 1 + idx] == Masu.SPACE) {
										masu[yIndex][xIndex + 1 + idx] = Masu.BLACK;
									}
									idx++;
								}
							} else if (arrow.getDirection() == Direction.DOWN) {
								while (yIndex + 1 + idx < getYLength()) {
									if (masu[yIndex + 1 + idx][xIndex] == Masu.SPACE) {
										masu[yIndex + 1 + idx][xIndex] = Masu.BLACK;
									}
									idx++;
								}
							} else if (arrow.getDirection() == Direction.LEFT) {
								while (xIndex - 1 - idx >= 0) {
									if (masu[yIndex][xIndex - 1 - idx] == Masu.SPACE) {
										masu[yIndex][xIndex - 1 - idx] = Masu.BLACK;
									}
									idx++;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 斜めにつながった島で同じ形があればfalseを返す。
		 */
		public boolean chblockSolve() {
			// 同一形状チェック
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						blackPosSet.add(new Position(yIndex, xIndex));
					}
				}
			}
			while (!blackPosSet.isEmpty()) {
				Position pivot = new ArrayList<>(blackPosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(pivot);
				setContinuePosSet(pivot, continuePosSet, 4, null);
				if (continuePosSet.size() == 4) {
					// つながったマスの周辺黒マス調査
					Set<Position> targetPosSet = new HashSet<>();
					for (Position searchPos : continuePosSet) {
						for (int i = 0; i < 4; i++) {
							Position targetMasu = null;
							if (i == 0) {
								if (searchPos.getyIndex() != 0 && searchPos.getxIndex() != 0) {
									targetMasu = new Position(searchPos.getyIndex() - 1, searchPos.getxIndex() - 1);
								} else {
									continue;
								}
							} else if (i == 1) {
								if (searchPos.getyIndex() != 0 && searchPos.getxIndex() != getXLength() - 1) {
									targetMasu = new Position(searchPos.getyIndex() - 1, searchPos.getxIndex() + 1);
								} else {
									continue;
								}
							} else if (i == 2) {
								if (searchPos.getyIndex() != getYLength() - 1
										&& searchPos.getxIndex() != getXLength() - 1) {
									targetMasu = new Position(searchPos.getyIndex() + 1, searchPos.getxIndex() + 1);
								} else {
									continue;
								}
							} else if (i == 3) {
								if (searchPos.getyIndex() != getYLength() - 1 && searchPos.getxIndex() != 0) {
									targetMasu = new Position(searchPos.getyIndex() + 1, searchPos.getxIndex() - 1);
								} else {
									continue;
								}
							}
							if (!continuePosSet.contains(targetMasu)
									&& masu[targetMasu.getyIndex()][targetMasu.getxIndex()] == Masu.BLACK) {
								targetPosSet.add(targetMasu);
							}
						}
					}
					// 島が同じ形だとfalse
					for (Position targetPos : targetPosSet) {
						Set<Position> otherPosSet = new HashSet<>();
						otherPosSet.add(targetPos);
						setContinuePosSet(targetPos, otherPosSet, 4, null);
						if (otherPosSet.size() == 4) {
							if (new FixedShape(continuePosSet).isSame(otherPosSet)) {
								return false;
							}
						}
					}
				}
				blackPosSet.removeAll(continuePosSet);
			}
			return true;
		}

		/**
		 * 黒マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position blackPos = new Position(yIndex, xIndex);
						if (blackPosSet.size() == 0) {
							blackPosSet.add(blackPos);
							setContinueDiagonalPosSet(blackPos, blackPosSet, null);
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
		 * posを起点に上下左右ななめに黒確定マスを無制限につなげていく。
		 */
		protected void setContinueDiagonalPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
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

		public Masu[][] getMasu() {
			return masu;
		}

	}

	protected final Field field;
	protected int count;

	public TetrochainSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public TetrochainSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = ""; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new TetrochainSolver(height, width, param).solve());
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
		return "解けました。推定難易度:" + Difficulty.getByCount(count).toString() + "(Lv:" + level + ")";
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

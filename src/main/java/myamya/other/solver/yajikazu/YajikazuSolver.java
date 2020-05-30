package myamya.other.solver.yajikazu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class YajikazuSolver implements Solver {

	public static class YajikazuGenerator implements Generator {

		static class YajikazuSolverForGenerator extends YajikazuSolver {

			private final int limit;

			public YajikazuSolverForGenerator(Field field, int limit) {
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

		public YajikazuGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new YajikazuGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			YajikazuSolver.Field wkField = new YajikazuSolver.Field(height, width);
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
							YajikazuSolver.Field virtual = new YajikazuSolver.Field(wkField);
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
							wkField = new YajikazuSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め
				// ランダムな方向を向かせるが、壁際のマスは外に向かないようにする。
				List<Position> numberPosList = new ArrayList<>();
				Map<Position, Arrow> blackTrueArrow = new HashMap<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
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
						int blackCntMax = 0;
						Direction direction = candDirection.get((int) (Math.random() * candDirection.size()));
						if (direction == Direction.UP) {
							blackCntMax = (yIndex + 1) / 2 + 1;
							while (yIndex - 1 - idx >= 0) {
								Position pos = new Position(yIndex - 1 - idx, xIndex);
								if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
									blackCnt++;
								}
								idx++;
							}
						}
						if (direction == Direction.RIGHT) {
							blackCntMax = (wkField.getXLength() - xIndex) / 2 + 1;
							while (xIndex + 1 + idx < wkField.getXLength()) {
								Position pos = new Position(yIndex, xIndex + 1 + idx);
								if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
									blackCnt++;
								}
								idx++;
							}
						}
						if (direction == Direction.DOWN) {
							blackCntMax = (wkField.getYLength() - yIndex) / 2 + 1;
							while (yIndex + 1 + idx < wkField.getYLength()) {
								Position pos = new Position(yIndex + 1 + idx, xIndex);
								if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
									blackCnt++;
								}
								idx++;
							}
						}
						if (direction == Direction.LEFT) {
							blackCntMax = (xIndex + 1) / 2 + 1;
							while (xIndex - 1 - idx >= 0) {
								Position pos = new Position(yIndex, xIndex - 1 - idx);
								if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
									blackCnt++;
								}
								idx++;
							}
						}
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							// 黒マスには嘘のヒントを初期設定する。
							// 本当のヒントはblackTrueArrowにとっておく
							// 嘘のヒントのマスは、本当のヒントにする→ヒントをなくす の2段構え
							while (true) {
								int falseCnt = (int) (Math.random() * (blackCntMax + 1));
								if (falseCnt != blackCnt) {
									wkField.arrows[yIndex][xIndex] = new Arrow(direction, falseCnt);
									break;
								}
							}
							blackTrueArrow.put(new Position(yIndex, xIndex), new Arrow(direction, blackCnt));
							numberPosList.add(new Position(yIndex, xIndex));
						} else {
							wkField.arrows[yIndex][xIndex] = new Arrow(direction, blackCnt);
						}
						numberPosList.add(new Position(yIndex, xIndex));
					}
				}
				if (blackTrueArrow.isEmpty()) {
					// 全白ます問題は出ないようにする
					wkField = new YajikazuSolver.Field(height, width);
					index = 0;
					continue;
				}
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new YajikazuSolverForGenerator(new YajikazuSolver.Field(wkField), 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new YajikazuSolver.Field(height, width);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						YajikazuSolver.Field virtual = new YajikazuSolver.Field(wkField, true);
						virtual.arrows[numberPos.getyIndex()][numberPos.getxIndex()] = blackTrueArrow.get(numberPos);
						int solveResult = new YajikazuSolverForGenerator(virtual, 8000).solve2();
						if (solveResult != -1) {
							wkField.arrows[numberPos.getyIndex()][numberPos.getxIndex()] = blackTrueArrow
									.get(numberPos);
							blackTrueArrow.remove(numberPos);
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(矢印：" + wkField.getHintCount() + ")";
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
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					Arrow oneArrow = wkField.getArrows()[yIndex][xIndex];
					if (oneArrow != null) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneArrow.toStringForweb()
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
			String wkstr = String.valueOf(count);
			int index = HALF_NUMS.indexOf(wkstr);
			if (index >= 0) {
				wkstr = FULL_NUMS.substring(index / 2,
						index / 2 + 1);
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

		public Masu[][] getMasu() {
			return masu;
		}

		public Arrow[][] getArrows() {
			return arrows;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?yajikazu/" + getXLength() + "/" + getYLength() + "/");
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

		public Field(int height, int width) {
			masu = new Masu[height][width];
			arrows = new Arrow[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
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
					if (Character.getNumericValue(ch) != -1) {
						Arrow arrow;
						if (adjust) {
							i++;
							arrow = new Arrow(direction, Character.getNumericValue(ch) * 16
									+ Character.getNumericValue(param.charAt(i)));
						} else {
							arrow = new Arrow(direction, Character.getNumericValue(ch));
						}
						arrows[index / getXLength()][index % getXLength()] = arrow;
					}
					adjust = false;
					index++;
					direction = null;
				}
			}

		}

		/**
		 * 現在の盤面の状況から矢印候補の絞り込みを行う、
		 * 候補がなくなった矢印のマスを黒マスにする。矢印が黒マスにできない場合、falseを返す。
		 */
		private boolean arrowSolve() {
			// 候補の絞り込み
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] != Masu.BLACK && arrows[yIndex][xIndex] != null) {
						Arrow arrow = arrows[yIndex][xIndex];
						int idx = 0;
						int blackCnt = 0;
						int spaceCnt = 0;
						boolean nextCanSpace = true;
						if (arrow.getDirection() == Direction.UP) {
							while (yIndex - 1 - idx >= 0) {
								Position pos = new Position(yIndex - 1 - idx, xIndex);
								if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
									blackCnt++;
									nextCanSpace = false;
								} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
									if (nextCanSpace) {
										spaceCnt++;
										nextCanSpace = false;
									} else {
										nextCanSpace = true;
									}
								} else {
									nextCanSpace = true;
								}
								idx++;
							}
						}
						if (arrow.getDirection() == Direction.RIGHT) {
							while (xIndex + 1 + idx < getXLength()) {
								Position pos = new Position(yIndex, xIndex + 1 + idx);
								if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
									blackCnt++;
									nextCanSpace = false;
								} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
									if (nextCanSpace) {
										spaceCnt++;
										nextCanSpace = false;
									} else {
										nextCanSpace = true;
									}
								} else {
									nextCanSpace = true;
								}
								idx++;
							}
						}
						if (arrow.getDirection() == Direction.DOWN) {
							while (yIndex + 1 + idx < getYLength()) {
								Position pos = new Position(yIndex + 1 + idx, xIndex);
								if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
									blackCnt++;
									nextCanSpace = false;
								} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
									if (nextCanSpace) {
										spaceCnt++;
										nextCanSpace = false;
									} else {
										nextCanSpace = true;
									}
								} else {
									nextCanSpace = true;
								}
								idx++;
							}
						}
						if (arrow.getDirection() == Direction.LEFT) {
							while (xIndex - 1 - idx >= 0) {
								Position pos = new Position(yIndex, xIndex - 1 - idx);
								if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
									blackCnt++;
									nextCanSpace = false;
								} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
									if (nextCanSpace) {
										spaceCnt++;
										nextCanSpace = false;
									} else {
										nextCanSpace = true;
									}
								} else {
									nextCanSpace = true;
								}
								idx++;
							}
						}
						if (arrow.getCount() < blackCnt || arrow.getCount() > blackCnt + spaceCnt) {
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								return false;
							} else if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			arrows = other.arrows;
		}

		public Field(Field other, boolean flag) {
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
					if (masu[yIndex][xIndex] != Masu.BLACK && arrows[yIndex][xIndex] != null) {
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
		 * 黒マス隣接セルを白マスにする。
		 * 黒マス隣接セルが黒マスの場合falseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
						if (masuUp == Masu.BLACK || masuRight == Masu.BLACK || masuDown == Masu.BLACK
								|| masuLeft == Masu.BLACK) {
							return false;
						}
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
				}
			}
			return true;
		}

		/**
		 * 白マスがひとつながりにならない場合Falseを返す。
		 * 今までのロジックより高速に動きます。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
							whitePosSet.add(whitePos);
							setContinuePosSet(whitePos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(whitePos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
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

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!arrowSolve()) {
				return false;
			}
			if (!nextSolve()) {
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
	protected int count = 0;

	public YajikazuSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public YajikazuSolver(Field field) {
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
		System.out.println(new YajikazuSolver(height, width, param).solve());
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

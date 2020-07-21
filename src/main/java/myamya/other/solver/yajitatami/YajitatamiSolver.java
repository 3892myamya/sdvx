package myamya.other.solver.yajitatami;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class YajitatamiSolver implements Solver {
	public static class YajitatamiGenerator implements Generator {

		static class YajitatamiSolverForGenerator extends YajitatamiSolver {

			private final int limit;

			public YajitatamiSolverForGenerator(Field field, int limit) {
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

		public YajitatamiGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new YajitatamiGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			YajitatamiSolver.Field wkField = new YajitatamiSolver.Field(height, width);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < (height * (width - 1)) + ((height - 1) * width); i++) {
				indexList.add(i);
			}
			Collections.shuffle(indexList);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				while (!wkField.isSolved()) {
					int posBase = indexList.get(index);
					boolean toYokoWall;
					int yIndex, xIndex;
					if (posBase < height * (width - 1)) {
						toYokoWall = true;
						yIndex = posBase / (width - 1);
						xIndex = posBase % (width - 1);
					} else {
						toYokoWall = false;
						posBase = posBase - (height * (width - 1));
						yIndex = posBase / width;
						xIndex = posBase % width;
					}
					if ((toYokoWall && wkField.yokoWall[yIndex][xIndex] == Wall.SPACE)
							|| (!toYokoWall && wkField.tateWall[yIndex][xIndex] == Wall.SPACE)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							YajitatamiSolver.Field virtual = new YajitatamiSolver.Field(wkField);
							if (masuNum < 1) {
								if (toYokoWall) {
									virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
								} else {
									virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
							} else if (masuNum < 2) {
								if (toYokoWall) {
									virtual.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								} else {
									virtual.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.yokoWall = virtual.yokoWall;
								wkField.tateWall = virtual.tateWall;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new YajitatamiSolver.Field(height, width);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め
				// 条件を満たす範囲でランダムな方向を向かせる
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						int fixUpCnt = 0;
						int wallUpCnt = 0;
						boolean fixUpContinue = true;
						for (int candY = yIndex - 1; candY >= 0; candY--) {
							if (wkField.tateWall[candY][xIndex] == Wall.NOT_EXISTS && fixUpContinue) {
								fixUpCnt++;
							} else {
								if (wkField.tateWall[candY][xIndex] == Wall.EXISTS) {
									wallUpCnt++;
								}
								fixUpContinue = false;
							}
						}
						int fixRightCnt = 0;
						int wallRightCnt = 0;
						boolean fixRightContinue = true;
						for (int candX = xIndex + 1; candX < wkField.getXLength(); candX++) {
							if (wkField.yokoWall[yIndex][candX - 1] == Wall.NOT_EXISTS && fixRightContinue) {
								fixRightCnt++;
							} else {
								if (wkField.yokoWall[yIndex][candX - 1] == Wall.EXISTS) {
									wallRightCnt++;
								}
								fixRightContinue = false;
							}
						}
						int fixDownCnt = 0;
						int wallDownCnt = 0;
						boolean fixDownContinue = true;
						for (int candY = yIndex + 1; candY < wkField.getYLength(); candY++) {
							if (wkField.tateWall[candY - 1][xIndex] == Wall.NOT_EXISTS && fixDownContinue) {
								fixDownCnt++;
							} else {
								if (wkField.tateWall[candY - 1][xIndex] == Wall.EXISTS) {
									wallDownCnt++;
								}
								fixDownContinue = false;
							}
						}
						int fixLeftCnt = 0;
						int wallLeftCnt = 0;
						boolean fixLeftContinue = true;
						for (int candX = xIndex - 1; candX >= 0; candX--) {
							if (wkField.yokoWall[yIndex][candX] == Wall.NOT_EXISTS && fixLeftContinue) {
								fixLeftCnt++;
							} else {
								if (wkField.yokoWall[yIndex][candX] == Wall.EXISTS) {
									wallLeftCnt++;
								}
								fixLeftContinue = false;
							}
						}
						int fixedVarticalCnt = fixUpCnt + fixDownCnt + 1;
						int fixedHorizonalCnt = fixRightCnt + fixLeftCnt + 1;
						List<Direction> candDirection = new ArrayList<>();
						int useCnt = 0;
						if (wallUpCnt != 1 && (fixedVarticalCnt == wallUpCnt || fixedHorizonalCnt == wallUpCnt)
								&& wkField.tateWall[yIndex - 1][xIndex] == Wall.EXISTS) {
							candDirection.add(Direction.UP);
							useCnt = wallUpCnt;
						}
						if (wallRightCnt != 1 && (fixedVarticalCnt == wallRightCnt || fixedHorizonalCnt == wallRightCnt)
								&& wkField.yokoWall[yIndex][xIndex] == Wall.EXISTS) {
							candDirection.add(Direction.RIGHT);
							useCnt = wallRightCnt;
						}
						if (wallDownCnt != 1 && (fixedVarticalCnt == wallDownCnt || fixedHorizonalCnt == wallDownCnt)
								&& wkField.tateWall[yIndex][xIndex] == Wall.EXISTS) {
							candDirection.add(Direction.DOWN);
							useCnt = wallDownCnt;
						}
						if (wallLeftCnt != 1 && (fixedVarticalCnt == wallLeftCnt || fixedHorizonalCnt == wallLeftCnt)
								&& wkField.yokoWall[yIndex][xIndex - 1] == Wall.EXISTS) {
							candDirection.add(Direction.LEFT);
							useCnt = wallLeftCnt;
						}
						if (candDirection.isEmpty()) {
							continue;
						}
						Direction direction = candDirection.get((int) (Math.random() * candDirection.size()));
						wkField.arrows[yIndex][xIndex] = new Arrow(direction, useCnt);
						numberPosList.add(new Position(yIndex, xIndex));
					}
				}
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				// 解けるかな？
				level = new YajitatamiSolverForGenerator(new YajitatamiSolver.Field(wkField), 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new YajitatamiSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						YajitatamiSolver.Field virtual = new YajitatamiSolver.Field(wkField, true);
						virtual.arrows[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						int solveResult = new YajitatamiSolverForGenerator(virtual, 2000).solve2();
						if (solveResult != -1) {
							wkField.arrows[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 10 / 3) + 1;
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
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 矢印の情報
		private final Arrow[][] arrows;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoFirstWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateFirstWall;

		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public boolean[][] getYokoFirstWall() {
			return yokoFirstWall;
		}

		public boolean[][] getTateFirstWall() {
			return tateFirstWall;
		}

		public Arrow[][] getArrows() {
			return arrows;
		}

		public int getYLength() {
			return arrows.length;
		}

		public int getXLength() {
			return arrows[0].length;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?yajitatami/" + getXLength() + "/" + getYLength() + "/");
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
			arrows = new Arrow[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			yokoFirstWall = new boolean[height][width - 1];
			tateFirstWall = new boolean[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
		}

		public Field(int height, int width, String param) {
			arrows = new Arrow[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			yokoFirstWall = new boolean[height][width - 1];
			tateFirstWall = new boolean[height - 1][width];
			int index = 0;
			int readPos = -1;
			Direction direction = null;
			boolean adjust = false;
			for (int i = 0; i < param.length(); i++) {
				if (index >= getXLength() * getYLength()) {
					readPos = i;
					break;
				}
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
			if (readPos != -1) {
				int bit = 0;
				for (int cnt = 0; cnt < getYLength() * (getXLength() - 1); cnt++) {
					int mod = cnt % 5;
					if (mod == 0) {
						bit = Character.getNumericValue(param.charAt(readPos));
						readPos++;
					}
					if (mod == 4 || cnt == (getYLength() * (getXLength() - 1)) - 1) {
						if (mod >= 0) {
							yokoFirstWall[(cnt - mod + 0) / (getXLength() - 1)][(cnt - mod + 0)
									% (getXLength() - 1)] = bit
											/ 16
											% 2 == 1;
						}
						if (mod >= 1) {
							yokoFirstWall[(cnt - mod + 1) / (getXLength() - 1)][(cnt - mod + 1)
									% (getXLength() - 1)] = bit
											/ 8
											% 2 == 1;
						}
						if (mod >= 2) {
							yokoFirstWall[(cnt - mod + 2) / (getXLength() - 1)][(cnt - mod + 2)
									% (getXLength() - 1)] = bit
											/ 4
											% 2 == 1;
						}
						if (mod >= 3) {
							yokoFirstWall[(cnt - mod + 3) / (getXLength() - 1)][(cnt - mod + 3)
									% (getXLength() - 1)] = bit
											/ 2
											% 2 == 1;
						}
						if (mod >= 4) {
							yokoFirstWall[(cnt - mod + 4) / (getXLength() - 1)][(cnt - mod + 4)
									% (getXLength() - 1)] = bit
											/ 1
											% 2 == 1;
						}
					}
				}
				for (int cnt = 0; cnt < (getYLength() - 1) * getXLength(); cnt++) {
					int mod = cnt % 5;
					if (mod == 0) {
						bit = Character.getNumericValue(param.charAt(readPos));
						readPos++;
					}
					if (mod == 4 || cnt == ((getYLength() - 1) * getXLength()) - 1) {
						if (mod >= 0) {
							tateFirstWall[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16
									% 2 == 1;
						}
						if (mod >= 1) {
							tateFirstWall[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8
									% 2 == 1;
						}
						if (mod >= 2) {
							tateFirstWall[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4
									% 2 == 1;
						}
						if (mod >= 3) {
							tateFirstWall[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2
									% 2 == 1;
						}
						if (mod >= 4) {
							tateFirstWall[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1
									% 2 == 1;
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = yokoFirstWall[yIndex][xIndex] ? Wall.EXISTS : Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = tateFirstWall[yIndex][xIndex] ? Wall.EXISTS : Wall.SPACE;
				}
			}
		}

		public Field(Field other) {
			arrows = other.arrows;
			yokoFirstWall = other.yokoFirstWall;
			tateFirstWall = other.tateFirstWall;
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = other.yokoWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
				}
			}
		}

		public Field(Field other, boolean flag) {
			arrows = new Arrow[other.getYLength()][other.getXLength()];
			yokoFirstWall = other.yokoFirstWall;
			tateFirstWall = other.tateFirstWall;
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					arrows[yIndex][xIndex] = other.arrows[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = other.yokoWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null) {
						sb.append(arrows[yIndex][xIndex]);
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					sb.append(yokoWall[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateWall[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 壁は3枚もしくは2枚(直進)である。違う場合はfalse。
		 */
		private boolean wallSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int exists = 0;
					int notExists = 0;
					Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						exists++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExists++;
					}
					Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						exists++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExists++;
					}
					Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						exists++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExists++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						exists++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExists++;
					}

					if (exists > 3 || notExists > 2 ||
							(wallUp == Wall.NOT_EXISTS && wallRight == Wall.NOT_EXISTS) ||
							(wallRight == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS) ||
							(wallDown == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS) ||
							(wallLeft == Wall.NOT_EXISTS && wallUp == Wall.NOT_EXISTS)) {
						return false;
					}
					if (exists == 3) {
						if (wallUp == Wall.SPACE) {
							tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
						}
					} else if (notExists == 2) {
						if (wallUp == Wall.SPACE) {
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					}
					if (wallUp == Wall.NOT_EXISTS || wallDown == Wall.NOT_EXISTS) {
						if (xIndex != getXLength() - 1) {
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (xIndex != 0) {
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					}
					if (wallRight == Wall.NOT_EXISTS || wallLeft == Wall.NOT_EXISTS) {
						if (yIndex != getYLength() - 1) {
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (yIndex != 0) {
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!wallSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
			if (!arrowSolve()) {
				return false;
			}
			if (!roomSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 柱から伸びる壁は2か3になる。
		 */
		private boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					int exists = 0;
					int notExists = 0;
					Wall wall1 = tateWall[yIndex][xIndex];
					Wall wall2 = tateWall[yIndex][xIndex + 1];
					Wall wall3 = yokoWall[yIndex][xIndex];
					Wall wall4 = yokoWall[yIndex + 1][xIndex];
					if (wall1 == Wall.EXISTS) {
						exists++;
					} else if (wall1 == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (wall2 == Wall.EXISTS) {
						exists++;
					} else if (wall2 == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (wall3 == Wall.EXISTS) {
						exists++;
					} else if (wall3 == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (wall4 == Wall.EXISTS) {
						exists++;
					} else if (wall4 == Wall.NOT_EXISTS) {
						notExists++;
					}
					if (exists > 3 || notExists > 2) {
						return false;
					}
					if (notExists == 2) {
						if (wall1 == Wall.SPACE) {
							tateWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wall2 == Wall.SPACE) {
							tateWall[yIndex][xIndex + 1] = Wall.EXISTS;
						}
						if (wall3 == Wall.SPACE) {
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wall4 == Wall.SPACE) {
							yokoWall[yIndex + 1][xIndex] = Wall.EXISTS;
						}
					} else if (exists == 3) {
						if (wall1 == Wall.SPACE) {
							tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wall2 == Wall.SPACE) {
							tateWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
						}
						if (wall3 == Wall.SPACE) {
							yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wall4 == Wall.SPACE) {
							yokoWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 矢印の数字は壁の枚数。また、直後には必ず壁がある。
		 */
		private boolean arrowSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null) {
						Arrow arrow = arrows[yIndex][xIndex];
						int idx = 0;
						int blackCnt = 0;
						int spaceCnt = 0;
						if (arrow.getDirection() == Direction.UP) {
							if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							while (yIndex - 1 - idx >= 0) {
								if (tateWall[yIndex - 1 - idx][xIndex] == Wall.EXISTS) {
									blackCnt++;
								}
								if (tateWall[yIndex - 1 - idx][xIndex] == Wall.SPACE) {
									spaceCnt++;
								}
								idx++;
							}
						} else if (arrow.getDirection() == Direction.RIGHT) {
							if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex] = Wall.EXISTS;
							while (xIndex + idx < getXLength() - 1) {
								if (yokoWall[yIndex][xIndex + idx] == Wall.EXISTS) {
									blackCnt++;
								}
								if (yokoWall[yIndex][xIndex + idx] == Wall.SPACE) {
									spaceCnt++;
								}
								idx++;
							}
						} else if (arrow.getDirection() == Direction.DOWN) {
							if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								return false;
							}
							tateWall[yIndex][xIndex] = Wall.EXISTS;
							while (yIndex + idx < getYLength() - 1) {
								if (tateWall[yIndex + idx][xIndex] == Wall.EXISTS) {
									blackCnt++;
								}
								if (tateWall[yIndex + idx][xIndex] == Wall.SPACE) {
									spaceCnt++;
								}
								idx++;
							}
						} else if (arrow.getDirection() == Direction.LEFT) {
							if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
								return false;
							}
							yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							while (xIndex - 1 - idx >= 0) {
								if (yokoWall[yIndex][xIndex - 1 - idx] == Wall.EXISTS) {
									blackCnt++;
								}
								if (yokoWall[yIndex][xIndex - 1 - idx] == Wall.SPACE) {
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
									if (tateWall[yIndex - 1 - idx][xIndex] == Wall.SPACE) {
										tateWall[yIndex - 1 - idx][xIndex] = Wall.NOT_EXISTS;
									}
									idx++;
								}
							} else if (arrow.getDirection() == Direction.RIGHT) {
								while (xIndex + idx < getXLength() - 1) {
									if (yokoWall[yIndex][xIndex + idx] == Wall.SPACE) {
										yokoWall[yIndex][xIndex + idx] = Wall.NOT_EXISTS;
									}
									idx++;
								}
							} else if (arrow.getDirection() == Direction.DOWN) {
								while (yIndex + idx < getYLength() - 1) {
									if (tateWall[yIndex + idx][xIndex] == Wall.SPACE) {
										tateWall[yIndex + idx][xIndex] = Wall.NOT_EXISTS;
									}
									idx++;
								}
							} else if (arrow.getDirection() == Direction.LEFT) {
								while (xIndex - 1 - idx >= 0) {
									if (yokoWall[yIndex][xIndex - 1 - idx] == Wall.SPACE) {
										yokoWall[yIndex][xIndex - 1 - idx] = Wall.NOT_EXISTS;
									}
									idx++;
								}
							}
						} else if (arrow.getCount() == blackCnt + spaceCnt) {
							idx = 0;
							if (arrow.getDirection() == Direction.UP) {
								while (yIndex - 1 - idx >= 0) {
									if (tateWall[yIndex - 1 - idx][xIndex] == Wall.SPACE) {
										tateWall[yIndex - 1 - idx][xIndex] = Wall.EXISTS;
									}
									idx++;
								}
							} else if (arrow.getDirection() == Direction.RIGHT) {
								while (xIndex + idx < getXLength() - 1) {
									if (yokoWall[yIndex][xIndex + idx] == Wall.SPACE) {
										yokoWall[yIndex][xIndex + idx] = Wall.EXISTS;
									}
									idx++;
								}
							} else if (arrow.getDirection() == Direction.DOWN) {
								while (yIndex + idx < getYLength() - 1) {
									if (tateWall[yIndex + idx][xIndex] == Wall.SPACE) {
										tateWall[yIndex + idx][xIndex] = Wall.EXISTS;
									}
									idx++;
								}
							} else if (arrow.getDirection() == Direction.LEFT) {
								while (xIndex - 1 - idx >= 0) {
									if (yokoWall[yIndex][xIndex - 1 - idx] == Wall.SPACE) {
										yokoWall[yIndex][xIndex - 1 - idx] = Wall.EXISTS;
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
		 * 矢印の数字は連続するマスの数。また、違う数の矢印とつながってはいけない。
		 */
		public boolean roomSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null) {
						int requiredConnect = arrows[yIndex][xIndex].getCount() - 1;
						int fixUpCnt = 0;
						int upCnt = 0;
						boolean fixUpContinue = true;
						for (int candY = yIndex - 1; candY >= 0; candY--) {
							if (tateWall[candY][xIndex] == Wall.EXISTS) {
								break;
							}
							if (tateWall[candY][xIndex] == Wall.NOT_EXISTS && fixUpContinue) {
								if (arrows[candY][xIndex] != null
										&& arrows[candY][xIndex].getCount() - 1 != requiredConnect) {
									return false;
								}
								fixUpCnt++;
							} else {
								fixUpContinue = false;
							}
							upCnt++;
						}
						int fixRightCnt = 0;
						int rightCnt = 0;
						boolean fixRightContinue = true;
						for (int candX = xIndex + 1; candX < getXLength(); candX++) {
							if (yokoWall[yIndex][candX - 1] == Wall.EXISTS) {
								break;
							}
							if (yokoWall[yIndex][candX - 1] == Wall.NOT_EXISTS && fixRightContinue) {
								if (arrows[yIndex][candX] != null
										&& arrows[yIndex][candX].getCount() - 1 != requiredConnect) {
									return false;
								}
								fixRightCnt++;
							} else {
								fixRightContinue = false;
							}
							rightCnt++;
						}
						int fixDownCnt = 0;
						int downCnt = 0;
						boolean fixDownContinue = true;
						for (int candY = yIndex + 1; candY < getYLength(); candY++) {
							if (tateWall[candY - 1][xIndex] == Wall.EXISTS) {
								break;
							}
							if (tateWall[candY - 1][xIndex] == Wall.NOT_EXISTS && fixDownContinue) {
								if (arrows[candY][xIndex] != null
										&& arrows[candY][xIndex].getCount() - 1 != requiredConnect) {
									return false;
								}
								fixDownCnt++;
							} else {
								fixDownContinue = false;
							}
							downCnt++;
						}
						int fixLeftCnt = 0;
						int leftCnt = 0;
						boolean fixLeftContinue = true;
						for (int candX = xIndex - 1; candX >= 0; candX--) {
							if (yokoWall[yIndex][candX] == Wall.EXISTS) {
								break;
							}
							if (yokoWall[yIndex][candX] == Wall.NOT_EXISTS && fixLeftContinue) {
								if (arrows[yIndex][candX] != null
										&& arrows[yIndex][candX].getCount() - 1 != requiredConnect) {
									return false;
								}
								fixLeftCnt++;
							} else {
								fixLeftContinue = false;
							}
							leftCnt++;
						}
						// 必ず縦につながる数
						int varticalFix = fixUpCnt + fixDownCnt;
						// 必ず横につながる数
						int horizonalFix = fixRightCnt + fixLeftCnt;
						// 縦につながることができる数
						int varticalCapacity = upCnt + downCnt;
						// 横につながることができる数
						int horizonalCapacity = rightCnt + leftCnt;
						if (requiredConnect < varticalFix || requiredConnect < horizonalFix) {
							return false;
						}
						if (requiredConnect > varticalCapacity && requiredConnect > horizonalCapacity) {
							return false;
						}

						if (requiredConnect > horizonalCapacity) {
							// 伸びる方向の確定処理
							int up = requiredConnect - downCnt;
							int cnt = 0;
							while (true) {
								if (cnt >= up) {
									break;
								}
								tateWall[yIndex - 1 - cnt][xIndex] = Wall.NOT_EXISTS;
								cnt++;
							}
							int down = requiredConnect - upCnt;
							cnt = 0;
							while (true) {
								if (cnt >= down) {
									break;
								}
								tateWall[yIndex + cnt][xIndex] = Wall.NOT_EXISTS;
								cnt++;
							}
						} else if (requiredConnect > varticalCapacity) {
							// 伸びる方向の確定処理
							int right = requiredConnect - leftCnt;
							int cnt = 0;
							while (true) {
								if (cnt >= right) {
									break;
								}
								yokoWall[yIndex][xIndex + cnt] = Wall.NOT_EXISTS;
								cnt++;
							}
							int left = requiredConnect - rightCnt;
							cnt = 0;
							while (true) {
								if (cnt >= left) {
									break;
								}
								yokoWall[yIndex][xIndex - 1 - cnt] = Wall.NOT_EXISTS;
								cnt++;
							}

						}
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public YajitatamiSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public YajitatamiSolver(Field field) {
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
		System.out.println(new YajitatamiSolver(height, width, param).solve());
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
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 10).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
				if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandTateWallSolve(field, yIndex, xIndex, recursive)) {
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

	private boolean oneCandYokoWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

	private boolean oneCandTateWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

}
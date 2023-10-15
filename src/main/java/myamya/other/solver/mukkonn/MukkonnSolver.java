package myamya.other.solver.mukkonn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class MukkonnSolver implements Solver {

	public static class Compass {

		@Override
		public String toString() {
			return "[upCnt=" + upCnt + ", rightCnt=" + rightCnt + ", downCnt=" + downCnt + ", leftCnt=" + leftCnt + "]";
		}

		private int upCnt;
		private int rightCnt;
		private int downCnt;
		private int leftCnt;

		public Compass(int upCnt, int rightCnt, int downCnt, int leftCnt) {
			this.upCnt = upCnt;
			this.rightCnt = rightCnt;
			this.downCnt = downCnt;
			this.leftCnt = leftCnt;
		}

		public Compass(Compass other) {
			this.upCnt = other.upCnt;
			this.rightCnt = other.rightCnt;
			this.downCnt = other.downCnt;
			this.leftCnt = other.leftCnt;
		}

		public int getUpCnt() {
			return upCnt;
		}

		public int getRightCnt() {
			return rightCnt;
		}

		public int getDownCnt() {
			return downCnt;
		}

		public int getLeftCnt() {
			return leftCnt;
		}

	}

	public static class PosAndDirection {

		@Override
		public String toString() {
			return "[pos=" + pos + ", direction=" + direction + "]";
		}

		private final Position pos;
		private final Direction direction;

		public PosAndDirection(Position pos, Direction direction) {
			this.pos = pos;
			this.direction = direction;
		}

		public Position getPos() {
			return pos;
		}

		public Direction getDirection() {
			return direction;
		}

	}

	public static class MukkonnGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class MukkonnSolverForGenerator extends MukkonnSolver {
			private final int limit;

			public MukkonnSolverForGenerator(Field field, int limit) {
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

		public MukkonnGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new MukkonnGenerator(6, 6).generate();
		}

		@Override
		public GeneratorResult generate() {
			Field wkField = new Field(height, width);
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
							Field virtual = new Field(wkField);
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
							wkField = new Field(height, width);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				System.out.println(wkField);
				List<Position> posList = new ArrayList<>();
				List<PosAndDirection> posAndDirectionList = new ArrayList<>();
				// 全部のマスにコンパスを置く
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						int upCnt = 0;
						int rightCnt = 0;
						int downCnt = 0;
						int leftCnt = 0;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (wkField.tateWall[targetY][xIndex] == Wall.EXISTS) {
								break;
							}
							upCnt++;
						}
						for (int targetX = xIndex + 1; targetX < wkField.getXLength(); targetX++) {
							if (wkField.yokoWall[yIndex][targetX - 1] == Wall.EXISTS) {
								break;
							}
							rightCnt++;
						}
						for (int targetY = yIndex + 1; targetY < wkField.getYLength(); targetY++) {
							if (wkField.tateWall[targetY - 1][xIndex] == Wall.EXISTS) {
								break;
							}
							downCnt++;
						}
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (wkField.yokoWall[yIndex][targetX] == Wall.EXISTS) {
								break;
							}
							leftCnt++;
						}
						if (upCnt == 0) {
							upCnt = (int) ((Math.random() * yIndex));
							if (upCnt == 0) {
								upCnt = -1;
							}
						}
						if (rightCnt == 0) {
							rightCnt = (int) ((Math.random() * (wkField.getXLength() - xIndex - 1)));
							if (rightCnt == 0) {
								rightCnt = -1;
							}
						}
						if (downCnt == 0) {
							downCnt = (int) ((Math.random() * (wkField.getYLength() - yIndex - 1)));
							if (downCnt == 0) {
								downCnt = -1;
							}
						}
						if (leftCnt == 0) {
							leftCnt = (int) ((Math.random() * xIndex));
							if (leftCnt == 0) {
								leftCnt = -1;
							}
						}
						posAndDirectionList.add(new PosAndDirection(new Position(yIndex, xIndex), Direction.UP));
						posAndDirectionList.add(new PosAndDirection(new Position(yIndex, xIndex), Direction.RIGHT));
						posAndDirectionList.add(new PosAndDirection(new Position(yIndex, xIndex), Direction.DOWN));
						posAndDirectionList.add(new PosAndDirection(new Position(yIndex, xIndex), Direction.LEFT));
						wkField.compasses[yIndex][xIndex] = new Compass(upCnt, rightCnt, downCnt, leftCnt);
						posList.add(new Position(yIndex, xIndex));
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
				level = new MukkonnSolverForGenerator(wkField, 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(posList);
					Collections.shuffle(posAndDirectionList);
					for (Position pos : posList) {
						if (Math.random() < 0.25) {
							continue;
						}
						Field virtual = new Field(wkField);
						virtual.compasses[pos.getyIndex()][pos.getxIndex()] = null;
						int solveResult = new MukkonnSolverForGenerator(virtual, 1500).solve2();
						if (solveResult != -1) {
							wkField.compasses[pos.getyIndex()][pos.getxIndex()] = null;
							level = solveResult;
						}
					}
					for (PosAndDirection posAndDirection : posAndDirectionList) {
						Position pos = posAndDirection.getPos();
						if (wkField.compasses[pos.getyIndex()][pos.getxIndex()] == null) {
							continue;
						}
						Field virtual = new Field(wkField);
						if (posAndDirection.getDirection() == Direction.UP) {
							virtual.compasses[pos.getyIndex()][pos.getxIndex()].upCnt = -1;
						} else if (posAndDirection.getDirection() == Direction.RIGHT) {
							virtual.compasses[pos.getyIndex()][pos.getxIndex()].rightCnt = -1;
						} else if (posAndDirection.getDirection() == Direction.DOWN) {
							virtual.compasses[pos.getyIndex()][pos.getxIndex()].downCnt = -1;
						} else if (posAndDirection.getDirection() == Direction.LEFT) {
							virtual.compasses[pos.getyIndex()][pos.getxIndex()].leftCnt = -1;
						}
						if (virtual.compasses[pos.getyIndex()][pos.getxIndex()].upCnt == -1
								&& virtual.compasses[pos.getyIndex()][pos.getxIndex()].rightCnt == -1
								&& virtual.compasses[pos.getyIndex()][pos.getxIndex()].downCnt == -1
								&& virtual.compasses[pos.getyIndex()][pos.getxIndex()].leftCnt == -1) {
							virtual.compasses[pos.getyIndex()][pos.getxIndex()] = null;
						}
						int solveResult = new MukkonnSolverForGenerator(virtual, 1500).solve2();
						if (solveResult != -1) {
							if (posAndDirection.getDirection() == Direction.UP) {
								wkField.compasses[pos.getyIndex()][pos.getxIndex()].upCnt = -1;
							} else if (posAndDirection.getDirection() == Direction.RIGHT) {
								wkField.compasses[pos.getyIndex()][pos.getxIndex()].rightCnt = -1;
							} else if (posAndDirection.getDirection() == Direction.DOWN) {
								wkField.compasses[pos.getyIndex()][pos.getxIndex()].downCnt = -1;
							} else if (posAndDirection.getDirection() == Direction.LEFT) {
								wkField.compasses[pos.getyIndex()][pos.getxIndex()].leftCnt = -1;
							}
							if (wkField.compasses[pos.getyIndex()][pos.getxIndex()].upCnt == -1
									&& wkField.compasses[pos.getyIndex()][pos.getxIndex()].rightCnt == -1
									&& wkField.compasses[pos.getyIndex()][pos.getxIndex()].downCnt == -1
									&& wkField.compasses[pos.getyIndex()][pos.getxIndex()].leftCnt == -1) {
								wkField.compasses[pos.getyIndex()][pos.getxIndex()] = null;
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 12 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			int baseSize = 27;
			int margin = 5;
			StringBuilder sb = new StringBuilder();
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
			// 記号描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					Compass compass = wkField.getCompasses()[yIndex][xIndex];
					if (compass != null) {
						sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
								+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
								+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
								+ "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
						sb.append(">" + "</line>");
						sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" y2=\""
								+ (yIndex * baseSize + baseSize + margin) + "\" x2=\"" + (xIndex * baseSize + baseSize)
								+ "\" stroke-width=\"1\" fill=\"none\"");
						sb.append("stroke=\"#000\" ");
						sb.append(">" + "</line>");
						if (compass.getLeftCnt() != -1) {
							String str;
							String numberStr = String.valueOf(compass.getLeftCnt());
							int idx = HALF_NUMS.indexOf(numberStr);
							if (idx >= 0) {
								str = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								str = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + (baseSize * 2 / 3) + margin - 1) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize) / 3
									+ "\" textLength=\"" + (baseSize) / 3 + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ str + "</text>");
						}
						if (compass.getUpCnt() != -1) {
							String str;
							String numberStr = String.valueOf(compass.getUpCnt());
							int idx = HALF_NUMS.indexOf(numberStr);
							if (idx >= 0) {
								str = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								str = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 3) + margin - 1) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 3)) + "\" font-size=\""
									+ (baseSize) / 3 + "\" textLength=\"" + (baseSize) / 3
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
						}
						if (compass.getDownCnt() != -1) {
							String str;
							String numberStr = String.valueOf(compass.getDownCnt());
							int idx = HALF_NUMS.indexOf(numberStr);
							if (idx >= 0) {
								str = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								str = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 1) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 3)) + "\" font-size=\""
									+ (baseSize) / 3 + "\" textLength=\"" + (baseSize) / 3
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
						}
						if (compass.getRightCnt() != -1) {
							String str;
							String numberStr = String.valueOf(compass.getRightCnt());
							int idx = HALF_NUMS.indexOf(numberStr);
							if (idx >= 0) {
								str = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
							} else {
								str = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + (baseSize * 2 / 3) + margin - 1) + "\" x=\""
									+ (xIndex * baseSize + baseSize + (baseSize * 2 / 3)) + "\" font-size=\""
									+ (baseSize) / 3 + "\" textLength=\"" + (baseSize) / 3
									+ "\" lengthAdjust=\"spacingAndGlyphs\">" + str + "</text>");
						}
					}
				}
			}
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			System.out.println(wkField.getPuzPreURL());
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// コンパス
		protected Compass[][] compasses;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected Wall[][] tateWall;

		public Compass[][] getCompasses() {
			return compasses;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://puzz.link/p?mukkonn/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (compasses[yIndex][xIndex] == null) {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Compass compass = compasses[yIndex][xIndex];
					String numStr = compass.getUpCnt() == -1 ? "." : String.valueOf(compass.getUpCnt());
					numStr = numStr + (compass.getDownCnt() == -1 ? "." : String.valueOf(compass.getDownCnt()));
					numStr = numStr + (compass.getLeftCnt() == -1 ? "." : String.valueOf(compass.getLeftCnt()));
					numStr = numStr + (compass.getRightCnt() == -1 ? "." : String.valueOf(compass.getRightCnt()));
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
			int kuro = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (compasses[yIndex][xIndex] != null) {
						kuro++;
					}
				}
			}
			return String.valueOf(kuro);
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return compasses.length;
		}

		public int getXLength() {
			return compasses[0].length;
		}

		public Field(int height, int width) {
			compasses = new Compass[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
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
			compasses = new Compass[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
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
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					// 16 - 255は '-'
					// 256 - 999は '+'
					Position pos = new Position(index / getXLength(), index % getXLength());
					final int upCnt;
					if (ch == '.') {
						upCnt = -1;
					} else {
						if (ch == '-') {
							upCnt = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							upCnt = Integer
									.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3), 16);
							i++;
							i++;
							i++;
						} else {
							upCnt = Integer.parseInt(String.valueOf(ch), 16);
						}
					}
					i++;
					ch = param.charAt(i);
					final int downCnt;
					if (ch == '.') {
						downCnt = -1;
					} else {
						if (ch == '-') {
							downCnt = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							downCnt = Integer
									.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3), 16);
							i++;
							i++;
							i++;
						} else {
							downCnt = Integer.parseInt(String.valueOf(ch), 16);
						}
					}
					i++;
					ch = param.charAt(i);
					final int leftCnt;
					if (ch == '.') {
						leftCnt = -1;
					} else {
						if (ch == '-') {
							leftCnt = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							leftCnt = Integer
									.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3), 16);
							i++;
							i++;
							i++;
						} else {
							leftCnt = Integer.parseInt(String.valueOf(ch), 16);
						}
					}
					i++;
					ch = param.charAt(i);
					final int rightCnt;
					if (ch == '.') {
						rightCnt = -1;
					} else {
						if (ch == '-') {
							rightCnt = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							rightCnt = Integer
									.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3), 16);
							i++;
							i++;
							i++;
						} else {
							rightCnt = Integer.parseInt(String.valueOf(ch), 16);
						}
					}
					compasses[pos.getyIndex()][pos.getxIndex()] = new Compass(upCnt, rightCnt, downCnt, leftCnt);
					index++;
				}

			}
		}

		public Field(Field other) {
			compasses = new Compass[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (other.compasses[yIndex][xIndex] != null) {
						compasses[yIndex][xIndex] = new Compass(other.compasses[yIndex][xIndex]);
					}
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
					sb.append(compasses[yIndex][xIndex] != null ? "×" : "　");
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
		 * 壁の数が2にならない場合falseを返す。
		 */
		protected boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// 壁は必ず2マス
					int existsCount = 0;
					int notExistsCount = 0;
					Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					if (existsCount > 2 || notExistsCount > 2) {
						return false;
					}
					if (notExistsCount == 2) {
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
					} else if (existsCount == 2) {
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
					}
				}
			}
			return true;
		}

		// コンパスの数字がついた方向の壁が空いているとき、その数だけ進む
		public boolean compassSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (compasses[yIndex][xIndex] != null) {
						Compass compass = compasses[yIndex][xIndex];
						if (compass.getUpCnt() != -1) {
							if (yIndex != 0 && tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
								// 壁が空いている場合伸ばす
								for (int i = 2; i <= compass.getUpCnt(); i++) {
									if (yIndex - i < 0 || tateWall[yIndex - i][xIndex] == Wall.EXISTS) {
										return false;
									} else {
										tateWall[yIndex - i][xIndex] = Wall.NOT_EXISTS;
									}
								}
								if (yIndex - compass.getUpCnt() - 1 >= 0) {
									if (tateWall[yIndex - compass.getUpCnt() - 1][xIndex] == Wall.NOT_EXISTS) {
										return false;
									} else {
										tateWall[yIndex - compass.getUpCnt() - 1][xIndex] = Wall.EXISTS;
									}
								}
							} else if (yIndex != 0 && tateWall[yIndex - 1][xIndex] == Wall.SPACE) {
								boolean canStretch = true;
								for (int i = 2; i <= compass.getUpCnt(); i++) {
									if (yIndex - i < 0 || tateWall[yIndex - i][xIndex] == Wall.EXISTS) {
										canStretch = false;
										break;
									}
								}
								if (canStretch && yIndex - compass.getUpCnt() - 1 >= 0) {
									if (tateWall[yIndex - compass.getUpCnt() - 1][xIndex] == Wall.NOT_EXISTS) {
										canStretch = false;
									}
								}
								if (!canStretch) {
									tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
								}
							}
						}
						if (compass.getRightCnt() != -1) {
							if (xIndex != getXLength() - 1 && yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								// 壁が空いている場合伸ばす
								for (int i = 2; i <= compass.getRightCnt(); i++) {
									if (xIndex - 1 + i >= getXLength() - 1
											|| yokoWall[yIndex][xIndex - 1 + i] == Wall.EXISTS) {
										return false;
									} else {
										yokoWall[yIndex][xIndex - 1 + i] = Wall.NOT_EXISTS;
									}
								}
								if (xIndex + compass.getRightCnt() < getXLength() - 1) {
									if (yokoWall[yIndex][xIndex + compass.getRightCnt()] == Wall.NOT_EXISTS) {
										return false;
									} else {
										yokoWall[yIndex][xIndex + compass.getRightCnt()] = Wall.EXISTS;
									}
								}
							} else if (xIndex != getXLength() - 1 && yokoWall[yIndex][xIndex] == Wall.SPACE) {
								boolean canStretch = true;
								for (int i = 2; i <= compass.getRightCnt(); i++) {
									if (xIndex - 1 + i >= getXLength() - 1
											|| yokoWall[yIndex][xIndex - 1 + i] == Wall.EXISTS) {
										canStretch = false;
										break;
									}
								}
								if (canStretch && xIndex + compass.getRightCnt() < getXLength() - 1) {
									if (yokoWall[yIndex][xIndex + compass.getRightCnt()] == Wall.NOT_EXISTS) {
										canStretch = false;
									}
								}
								if (!canStretch) {
									yokoWall[yIndex][xIndex] = Wall.EXISTS;
								}
							}
						}
						if (compass.getDownCnt() != -1) {
							if (yIndex != getYLength() - 1 && tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
								// 壁が空いている場合伸ばす
								for (int i = 2; i <= compass.getDownCnt(); i++) {
									if (yIndex - 1 + i >= getYLength() - 1
											|| tateWall[yIndex - 1 + i][xIndex] == Wall.EXISTS) {
										return false;
									} else {
										tateWall[yIndex - 1 + i][xIndex] = Wall.NOT_EXISTS;
									}
								}
								if (yIndex + compass.getDownCnt() < getYLength() - 1) {
									if (tateWall[yIndex + compass.getDownCnt()][xIndex] == Wall.NOT_EXISTS) {
										return false;
									} else {
										tateWall[yIndex + compass.getDownCnt()][xIndex] = Wall.EXISTS;
									}
								}
							} else if (yIndex != getYLength() - 1 && tateWall[yIndex][xIndex] == Wall.SPACE) {
								boolean canStretch = true;
								for (int i = 2; i <= compass.getDownCnt(); i++) {
									if (yIndex - 1 + i >= getYLength() - 1
											|| tateWall[yIndex - 1 + i][xIndex] == Wall.EXISTS) {
										canStretch = false;
										break;
									}
								}
								if (canStretch && yIndex + compass.getDownCnt() < getYLength() - 1) {
									if (tateWall[yIndex + compass.getDownCnt()][xIndex] == Wall.NOT_EXISTS) {
										canStretch = false;
									}
								}
								if (!canStretch) {
									tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
							}
						}
						if (compass.getLeftCnt() != -1) {
							if (xIndex != 0 && yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
								// 壁が空いている場合伸ばす
								for (int i = 2; i <= compass.getLeftCnt(); i++) {
									if (xIndex - i < 0 || yokoWall[yIndex][xIndex - i] == Wall.EXISTS) {
										return false;
									} else {
										yokoWall[yIndex][xIndex - i] = Wall.NOT_EXISTS;
									}
								}
								if (xIndex - compass.getLeftCnt() - 1 >= 0) {
									if (yokoWall[yIndex][xIndex - compass.getLeftCnt() - 1] == Wall.NOT_EXISTS) {
										return false;
									} else {
										yokoWall[yIndex][xIndex - compass.getLeftCnt() - 1] = Wall.EXISTS;
									}
								}
							} else if (xIndex != 0 && yokoWall[yIndex][xIndex - 1] == Wall.SPACE) {
								boolean canStretch = true;
								for (int i = 2; i <= compass.getLeftCnt(); i++) {
									if (xIndex - i < 0 || yokoWall[yIndex][xIndex - i] == Wall.EXISTS) {
										canStretch = false;
										break;
									}
								}
								if (canStretch && xIndex - compass.getLeftCnt() - 1 >= 0) {
									if (yokoWall[yIndex][xIndex - compass.getLeftCnt() - 1] == Wall.NOT_EXISTS) {
										canStretch = false;
									}
								}
								if (!canStretch) {
									yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
								}
							}
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
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (whitePosSet.size() == 0) {
						whitePosSet.add(pos);
						setContinuePosSet(pos, whitePosSet, null);
					} else {
						if (!whitePosSet.contains(pos)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * 
		 * @param recursive
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!compassSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!oddSolve()) {
					return false;
				}
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * ルール上、各列をふさぐ壁は必ず偶数になる。 偶数になっていない場合falseを返す。
		 */
		private boolean oddSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				int notExistsCount = 0;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
				int notExistsCount = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
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

	public MukkonnSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public MukkonnSolver(Field field) {
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
		System.out.println(new MukkonnSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 12));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 12 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 12).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
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
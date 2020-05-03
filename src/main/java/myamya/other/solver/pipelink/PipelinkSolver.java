package myamya.other.solver.pipelink;

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

public class PipelinkSolver implements Solver {
	public static class PipelinkGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class PipelinkSolverForGenerator extends PipelinkSolver {
			private final int limit;

			public PipelinkSolverForGenerator(Field field, int limit) {
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

		public PipelinkGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new PipelinkGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			PipelinkSolver.Field wkField = new PipelinkSolver.Field(height, width);
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
							PipelinkSolver.Field virtual = new PipelinkSolver.Field(wkField, true);
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
							wkField = new PipelinkSolver.Field(height, width);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// できるだけ埋める
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						Position pos = new Position(yIndex, xIndex);
						wkField.firstPosSet.add(pos);
						numberPosList.add(pos);
					}
				}
				// 解けるかな？
				level = new PipelinkSolverForGenerator(wkField, 50).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new PipelinkSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						PipelinkSolver.Field virtual = new PipelinkSolver.Field(wkField, true);
						virtual.firstPosSet.remove(numberPos);
						if (numberPos.getyIndex() != 0 && !virtual.firstPosSet
								.contains(new Position(numberPos.getyIndex() - 1, numberPos.getxIndex()))) {
							virtual.tateWall[numberPos.getyIndex() - 1][numberPos.getxIndex()] = Wall.SPACE;
						}
						if (numberPos.getxIndex() != width - 1 && !virtual.firstPosSet
								.contains(new Position(numberPos.getyIndex(), numberPos.getxIndex() + 1))) {
							virtual.yokoWall[numberPos.getyIndex()][numberPos.getxIndex()] = Wall.SPACE;
						}
						if (numberPos.getyIndex() != height - 1 && !virtual.firstPosSet
								.contains(new Position(numberPos.getyIndex() + 1, numberPos.getxIndex()))) {
							virtual.tateWall[numberPos.getyIndex()][numberPos.getxIndex()] = Wall.SPACE;
						}
						if (numberPos.getxIndex() != 0 && !virtual.firstPosSet
								.contains(new Position(numberPos.getyIndex(), numberPos.getxIndex() - 1))) {
							virtual.yokoWall[numberPos.getyIndex()][numberPos.getxIndex() - 1] = Wall.SPACE;
						}
						int solveResult = new PipelinkSolverForGenerator(virtual, 200).solve2();
						if (solveResult != -1) {
							wkField.firstPosSet.remove(numberPos);
							if (numberPos.getyIndex() != 0 && !wkField.firstPosSet
									.contains(new Position(numberPos.getyIndex() - 1, numberPos.getxIndex()))) {
								wkField.tateWall[numberPos.getyIndex() - 1][numberPos.getxIndex()] = Wall.SPACE;
							}
							if (numberPos.getxIndex() != width - 1 && !wkField.firstPosSet
									.contains(new Position(numberPos.getyIndex(), numberPos.getxIndex() + 1))) {
								wkField.yokoWall[numberPos.getyIndex()][numberPos.getxIndex()] = Wall.SPACE;
							}
							if (numberPos.getyIndex() != height - 1 && !wkField.firstPosSet
									.contains(new Position(numberPos.getyIndex() + 1, numberPos.getxIndex()))) {
								wkField.tateWall[numberPos.getyIndex()][numberPos.getxIndex()] = Wall.SPACE;
							}
							if (numberPos.getxIndex() != 0 && !wkField.firstPosSet
									.contains(new Position(numberPos.getyIndex(), numberPos.getxIndex() - 1))) {
								wkField.yokoWall[numberPos.getyIndex()][numberPos.getxIndex() - 1] = Wall.SPACE;
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 50 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
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
					if (wkField.firstPosSet.contains(new Position(yIndex, xIndex))) {
						String str = "";
						Wall up = yIndex == 0 ? Wall.EXISTS
								: wkField.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == wkField.getXLength() - 1 ? Wall.EXISTS
								: wkField.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == wkField.getYLength() - 1 ? Wall.EXISTS
								: wkField.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS
								: wkField.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.NOT_EXISTS &&
								left == Wall.NOT_EXISTS) {
							str = "┼";
						} else if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS) {
							str = "└";
						} else if (up == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
							str = "│";
						} else if (up == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
							str = "┘";
						} else if (right == Wall.NOT_EXISTS && down == Wall.NOT_EXISTS) {
							str = "┌";
						} else if (right == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
							str = "─";
						} else if (down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
							str = "┐";
						} else {
							str = "　";
						}
						String fillColor;
						fillColor = "black";
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize)
								+ "\" textLength=\""
								+ (baseSize)
								+ "\" fill=\""
								+ fillColor
								+ "\" stroke=\"" + fillColor + "\" stroke-width=\"1"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ str
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

	public static class Field {
		static final String ALPHABET_FROM_H = "hijklmnopqrstuvwxyz";

		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;
		// 表出マス情報 画面表示用
		private final Set<Position> firstPosSet;

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?pipelink/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (!firstPosSet.contains(new Position(yIndex, xIndex))) {
					interval++;
					if (interval == 19) {
						sb.append("z");
						interval = 0;
					}
				} else {
					String str = "";
					Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.NOT_EXISTS &&
							wallRight == Wall.NOT_EXISTS &&
							wallUp == Wall.NOT_EXISTS &&
							wallDown == Wall.NOT_EXISTS) {
						str = "a";
					} else if (wallLeft == Wall.EXISTS &&
							wallRight == Wall.EXISTS &&
							wallUp == Wall.NOT_EXISTS &&
							wallDown == Wall.NOT_EXISTS) {
						str = "b";
					} else if (wallLeft == Wall.NOT_EXISTS &&
							wallRight == Wall.NOT_EXISTS &&
							wallUp == Wall.EXISTS &&
							wallDown == Wall.EXISTS) {
						str = "c";
					} else if (wallLeft == Wall.EXISTS &&
							wallRight == Wall.NOT_EXISTS &&
							wallUp == Wall.NOT_EXISTS &&
							wallDown == Wall.EXISTS) {
						str = "d";
					} else if (wallLeft == Wall.NOT_EXISTS &&
							wallRight == Wall.EXISTS &&
							wallUp == Wall.NOT_EXISTS &&
							wallDown == Wall.EXISTS) {
						str = "e";
					} else if (wallLeft == Wall.NOT_EXISTS &&
							wallRight == Wall.EXISTS &&
							wallUp == Wall.EXISTS &&
							wallDown == Wall.NOT_EXISTS) {
						str = "f";
					} else if (wallLeft == Wall.EXISTS &&
							wallRight == Wall.NOT_EXISTS &&
							wallUp == Wall.EXISTS &&
							wallDown == Wall.NOT_EXISTS) {
						str = "g";
					}
					if (interval == 0) {
						sb.append(str);
					} else {
						sb.append(ALPHABET_FROM_H.substring(interval - 1, interval));
						sb.append(str);
						interval = 0;
					}
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET_FROM_H.substring(interval - 1, interval));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public String getHintCount() {
			return String.valueOf(firstPosSet.size());
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return yokoWall.length;
		}

		public int getXLength() {
			return tateWall[0].length;
		}

		public Set<Position> getFirstPosSet() {
			return firstPosSet;
		}

		public Field(int height, int width, String param) {
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			firstPosSet = new HashSet<>();
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
				int interval = ALPHABET_FROM_H.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					Position pos = new Position(index / getXLength(), index % getXLength());
					if (ch == '.') {
						//
					} else if (ch == 'a') {
						firstPosSet.add(pos);
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
					} else if (ch == 'b') {
						firstPosSet.add(pos);
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
					} else if (ch == 'c') {
						firstPosSet.add(pos);
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
					} else if (ch == 'd') {
						firstPosSet.add(pos);
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
					} else if (ch == 'e') {
						firstPosSet.add(pos);
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
					} else if (ch == 'f') {
						firstPosSet.add(pos);
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
					} else if (ch == 'g') {
						firstPosSet.add(pos);
						if (pos.getxIndex() != 0) {
							yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
						}
						if (pos.getxIndex() != getXLength() - 1) {
							yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
						if (pos.getyIndex() != 0) {
							tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
						}
						if (pos.getyIndex() != getYLength() - 1) {
							tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
						}
					}
					index++;
				}
			}

		}

		public Field(Field other) {
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
			firstPosSet = other.firstPosSet;
		}

		public Field(int height, int width) {
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			firstPosSet = new HashSet<>();
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

		public Field(Field other, boolean flag) {
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
			firstPosSet = new HashSet<>(other.firstPosSet);
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
					sb.append("　");
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
		 * 壁の数は0か2になる。違反の場合false。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
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

					if (existsCount > 2 || (notExistsCount == 3 && existsCount == 1)) {
						return false;
					}
					if (existsCount == 2) {
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
					} else if (existsCount == 1 && notExistsCount == 2) {
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
					} else if (notExistsCount == 3) {
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

		/**
		 * 白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// 交差点の可能性があるマスから開始しないようにする
					int existsCount = 0;
					Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					}
					Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					}
					Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					}
					if (existsCount == 0) {
						continue;
					}
					Position originPos = new Position(yIndex, xIndex);
					Set<Position> continuePosSet = new HashSet<>();
					continuePosSet.add(originPos);
					if (setContinuePosSet(originPos, originPos, continuePosSet, null)) {
						return continuePosSet.size() == getYLength() * getXLength();
					}
				}
			}
			return true;

		}

		/**
		 * posを起点に壁なし確定のマスを、直線を優先してつないでいく。
		 * 出発マスまで戻ってきたらtrueを返す。
		 * 進む方向が確定できなくなった時点でfalseを返す。
		 */
		private boolean setContinuePosSet(Position originPos, Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (pos.getyIndex() != 0 && from == Direction.DOWN
					&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS) {
				// 直進して上へ
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (originPos.equals(nextPos)) {
					return true;
				}
				continuePosSet.add(nextPos);
				return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.DOWN);
			} else if (pos.getxIndex() != getXLength() - 1 && from == Direction.LEFT
					&& yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
				// 直進して右へ
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (originPos.equals(nextPos)) {
					return true;
				}
				continuePosSet.add(nextPos);
				return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.LEFT);
			} else if (pos.getyIndex() != getYLength() - 1 && from == Direction.UP
					&& tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
				// 直進して下へ
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (originPos.equals(nextPos)) {
					return true;
				}
				continuePosSet.add(nextPos);
				return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.UP);
			} else if (pos.getxIndex() != 0 && from == Direction.RIGHT
					&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS) {
				// 直進して左へ
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (originPos.equals(nextPos)) {
					return true;
				}
				continuePosSet.add(nextPos);
				return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.RIGHT);
			} else {
				// 直進可能性判定
				// 直進できる可能性が残っているときはfalseにする
				if (pos.getyIndex() != 0 && from == Direction.DOWN
						&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS) {
					return false;
				} else if (pos.getxIndex() != getXLength() - 1 && from == Direction.LEFT
						&& yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS) {
					return false;
				} else if (pos.getyIndex() != getYLength() - 1 && from == Direction.UP
						&& tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS) {
					return false;
				} else if (pos.getxIndex() != 0 && from == Direction.RIGHT
						&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS) {
					return false;
				} else {
					// 直進不可
					if (pos.getyIndex() != 0 && from != Direction.UP
							&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS) {
						// カーブして上へ
						Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
						if (originPos.equals(nextPos)) {
							return true;
						}
						continuePosSet.add(nextPos);
						return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.DOWN);
					} else if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT
							&& yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
						// カーブして右へ
						Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
						if (originPos.equals(nextPos)) {
							return true;
						}
						continuePosSet.add(nextPos);
						return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.LEFT);
					} else if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN
							&& tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
						// カーブして下へ
						Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
						if (originPos.equals(nextPos)) {
							return true;
						}
						continuePosSet.add(nextPos);
						return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.UP);
					} else if (pos.getxIndex() != 0 && from != Direction.LEFT
							&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS) {
						// カーブして左へ
						Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
						if (originPos.equals(nextPos)) {
							return true;
						}
						continuePosSet.add(nextPos);
						return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.RIGHT);
					}
					return false;
				}
			}
		}

		/**
		 * ルール上、各列をふさぐ壁は必ず偶数になる。
		 * 偶数になっていない場合falseを返す。
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

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!oddSolve()) {
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

	public PipelinkSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public PipelinkSolver(Field field) {
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
		System.out.println(new PipelinkSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 50));
		System.out.println(field);
		int level = (int) Math.sqrt((count * 50) / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount((count * 50)).toString() + "(Lv:" + level + ")";
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

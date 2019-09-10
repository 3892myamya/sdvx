package myamya.other.solver.reflect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class ReflectSolver implements Solver {
	public static class ReflectGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class ExtendedField extends ReflectSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width) {
				super(height, width);
			}

			/**
			 * 作問中は白マスの壁の数が0になってもよいので。
			 */
			@Override
			public boolean nextSolve() {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (!crossPosSet.contains(new Position(yIndex, xIndex))) {
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
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								// 自分が不確定マスなら壁は0マスか2マスか4マス
								if ((existsCount == 3 && notExistsCount == 1)
										|| (existsCount == 1 && notExistsCount == 3)) {
									return false;
								}
								if (existsCount > 2) {
									masu[yIndex][xIndex] = Masu.BLACK;
								} else if (notExistsCount != 0) {
									masu[yIndex][xIndex] = Masu.NOT_BLACK;
								}
							}
							if (masu[yIndex][xIndex] == Masu.BLACK) {
								// 自分が黒マスなら壁は4マス
								if (notExistsCount > 0) {
									return false;
								}
								// 周囲の壁を閉鎖
								if (yIndex != 0) {
									tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
								}
								if (xIndex != getXLength() - 1) {
									yokoWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (yIndex != getYLength() - 1) {
									tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (xIndex != 0) {
									yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
								}
							} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								// 自分が白マスなら壁は0マスか2マス
								if (existsCount > 2) {
									return false;
								}
								if (notExistsCount > 2) {
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
								}
							}
						}
					}
				}
				return true;
			}
		}

		static class ReflectSolverForGenerator extends ReflectSolver {
			private final int limit;

			public ReflectSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
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
				return count;
			}

			@Override
			protected boolean candSolve(Field field, int recursive) {
				if (this.count >= limit) {
					return false;
				} else {
					return super.candSolve(field, recursive);
				}
			}
		}

		private final int height;
		private final int width;

		public ReflectGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new ReflectGenerator(17, 17).generate();
		}

		@Override
		public GeneratorResult generate() {
			ExtendedField wkField = new ExtendedField(height, width);
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
							ExtendedField virtual = new ExtendedField(wkField);
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
							wkField = new ExtendedField(height, width);
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
						int notExistsCount = 0;
						Wall wallUp = yIndex == 0 ? Wall.EXISTS : wkField.tateWall[yIndex - 1][xIndex];
						if (wallUp == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						Wall wallRight = xIndex == wkField.getXLength() - 1 ? Wall.EXISTS
								: wkField.yokoWall[yIndex][xIndex];
						if (wallRight == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						Wall wallDown = yIndex == wkField.getYLength() - 1 ? Wall.EXISTS
								: wkField.tateWall[yIndex][xIndex];
						if (wallDown == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						Wall wallLeft = xIndex == 0 ? Wall.EXISTS : wkField.yokoWall[yIndex][xIndex - 1];
						if (wallLeft == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						if (notExistsCount == 4) {
							// 十字情報
							wkField.crossPosSet.add(pos);
						} else if ((wallUp == Wall.NOT_EXISTS && wallRight == Wall.NOT_EXISTS)
								|| (wallRight == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS)
								|| (wallDown == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS)
								|| (wallLeft == Wall.NOT_EXISTS && wallUp == Wall.NOT_EXISTS)) {
							// カーブは数字全表出
							int upSpaceCnt = 0;
							for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
								if (wkField.tateWall[targetY][xIndex] == Wall.EXISTS) {
									break;
								}
								upSpaceCnt++;
							}
							int rightSpaceCnt = 0;
							for (int targetX = xIndex + 1; targetX < wkField.getXLength(); targetX++) {
								if (wkField.yokoWall[yIndex][targetX - 1] == Wall.EXISTS) {
									break;
								}
								rightSpaceCnt++;
							}
							int downSpaceCnt = 0;
							for (int targetY = yIndex + 1; targetY < wkField.getYLength(); targetY++) {
								if (wkField.tateWall[targetY - 1][xIndex] == Wall.EXISTS) {
									break;
								}
								downSpaceCnt++;
							}
							int leftSpaceCnt = 0;
							for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
								if (wkField.yokoWall[yIndex][targetX] == Wall.EXISTS) {
									break;
								}
								leftSpaceCnt++;
							}
							int aroundSpaceCnt = 1 + upSpaceCnt + rightSpaceCnt + downSpaceCnt + leftSpaceCnt;
							wkField.numbers[yIndex][xIndex] = aroundSpaceCnt;
							// 数字減らし・壁減らしの2段構え
							numberPosList.add(pos);
							numberPosList.add(pos);
						}
					}
				}
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] == null
								&& !wkField.getCrossPosSet().contains(new Position(yIndex, xIndex))) {
							// 十字マスや数字マスは白確定なので戻さない
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
							if (yIndex != 0 && wkField.numbers[yIndex - 1][xIndex] == null
									&& !wkField.getCrossPosSet().contains(new Position(yIndex - 1, xIndex))) {
								wkField.tateWall[yIndex - 1][xIndex] = Wall.SPACE;
							}
							if (xIndex != wkField.getXLength() - 1 && wkField.numbers[yIndex][xIndex + 1] == null
									&& !wkField.getCrossPosSet().contains(new Position(yIndex, xIndex + 1))) {
								wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
							}
							if (yIndex != wkField.getYLength() - 1 && wkField.numbers[yIndex + 1][xIndex] == null
									&& !wkField.getCrossPosSet().contains(new Position(yIndex + 1, xIndex))) {
								wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
							}
							if (xIndex != 0 && wkField.numbers[yIndex][xIndex - 1] == null
									&& !wkField.getCrossPosSet().contains(new Position(yIndex, xIndex - 1))) {
								wkField.yokoWall[yIndex][xIndex - 1] = Wall.SPACE;
							}
						}
					}
				}
				// 解けるかな？
				level = new ReflectSolverForGenerator(new ReflectSolver.Field(wkField), 50).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						ReflectSolver.Field virtual = new ReflectSolver.Field(wkField, true);
						int yIndex = numberPos.getyIndex();
						int xIndex = numberPos.getxIndex();
						if (virtual.numbers[yIndex][xIndex] != 0) {
							virtual.numbers[yIndex][xIndex] = 0;
						} else {
							virtual.numbers[yIndex][xIndex] = null;
							virtual.masu[yIndex][xIndex] = Masu.SPACE;
							if (yIndex != 0 && virtual.numbers[yIndex - 1][xIndex] == null
									&& !virtual.getCrossPosSet().contains(new Position(yIndex - 1, xIndex))) {
								virtual.tateWall[yIndex - 1][xIndex] = Wall.SPACE;
							}
							if (xIndex != virtual.getXLength() - 1 && virtual.numbers[yIndex][xIndex + 1] == null
									&& !virtual.getCrossPosSet().contains(new Position(yIndex, xIndex + 1))) {
								virtual.yokoWall[yIndex][xIndex] = Wall.SPACE;
							}
							if (yIndex != virtual.getYLength() - 1 && virtual.numbers[yIndex + 1][xIndex] == null
									&& !virtual.getCrossPosSet().contains(new Position(yIndex + 1, xIndex))) {
								virtual.tateWall[yIndex][xIndex] = Wall.SPACE;
							}
							if (xIndex != 0 && virtual.numbers[yIndex][xIndex - 1] == null
									&& !virtual.getCrossPosSet().contains(new Position(yIndex, xIndex - 1))) {
								virtual.yokoWall[yIndex][xIndex - 1] = Wall.SPACE;
							}
						}
						int solveResult = new ReflectSolverForGenerator(virtual, 1500).solve2();
						if (solveResult != -1) {
							if (wkField.numbers[yIndex][xIndex] != 0) {
								wkField.numbers[yIndex][xIndex] = 0;
							} else {
								wkField.numbers[yIndex][xIndex] = null;
								wkField.masu[yIndex][xIndex] = Masu.SPACE;
								if (yIndex != 0 && wkField.numbers[yIndex - 1][xIndex] == null
										&& !wkField.getCrossPosSet().contains(new Position(yIndex - 1, xIndex))) {
									wkField.tateWall[yIndex - 1][xIndex] = Wall.SPACE;
								}
								if (xIndex != wkField.getXLength() - 1 && wkField.numbers[yIndex][xIndex + 1] == null
										&& !wkField.getCrossPosSet().contains(new Position(yIndex, xIndex + 1))) {
									wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
								}
								if (yIndex != wkField.getYLength() - 1 && wkField.numbers[yIndex + 1][xIndex] == null
										&& !wkField.getCrossPosSet().contains(new Position(yIndex + 1, xIndex))) {
									wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
								}
								if (xIndex != 0 && wkField.numbers[yIndex][xIndex - 1] == null
										&& !wkField.getCrossPosSet().contains(new Position(yIndex, xIndex - 1))) {
									wkField.yokoWall[yIndex][xIndex - 1] = Wall.SPACE;
								}
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 20 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(十字:" + wkField.getHintCount().split("/")[0] + "、数字/三角："
					+ wkField.getHintCount().split("/")[1] + "/"
					+ wkField.getHintCount().split("/")[2] + ")";
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
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						String masuStr = null;
						if (wkField.getNumbers()[yIndex][xIndex] > 0) {
							String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
							int numIdx = HALF_NUMS.indexOf(numberStr);
							if (numIdx >= 0) {
								masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
						}
						Wall up = yIndex == 0 ? Wall.EXISTS : wkField.getTateWall()[yIndex - 1][xIndex];
						Wall right = xIndex == wkField.getXLength() - 1 ? Wall.EXISTS
								: wkField.getYokoWall()[yIndex][xIndex];
						Wall down = yIndex == wkField.getYLength() - 1 ? Wall.EXISTS
								: wkField.getTateWall()[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : wkField.getYokoWall()[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS && left == Wall.EXISTS) {
							sb.append("<path d=\"M "
									+ (xIndex * baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + margin)
									+ " L"
									+ (xIndex * baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + baseSize + margin)
									+ " L"
									+ (xIndex * baseSize + baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + baseSize + margin)
									+ " Z\" >"
									+ "</path>");
							if (masuStr != null) {
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize)
										+ "\" fill=\""
										+ "white"
										+ "\" font-size=\""
										+ (baseSize + 2) / 2
										+ "\" textLength=\""
										+ (baseSize + 2) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr
										+ "</text>");
							}
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.EXISTS && left == Wall.NOT_EXISTS) {
							sb.append("<path d=\"M "
									+ (xIndex * baseSize + baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + margin)
									+ " L"
									+ (xIndex * baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + baseSize + margin)
									+ " L"
									+ (xIndex * baseSize + baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + baseSize + margin)
									+ " Z\" >"
									+ "</path>");
							if (masuStr != null) {
								sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 2))
										+ "\" fill=\""
										+ "white"
										+ "\" font-size=\""
										+ (baseSize + 2) / 2
										+ "\" textLength=\""
										+ (baseSize + 2) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr
										+ "</text>");
							}
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.NOT_EXISTS && left == Wall.EXISTS) {
							sb.append("<path d=\"M "
									+ (xIndex * baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + margin)
									+ " L"
									+ (xIndex * baseSize + baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + margin)
									+ " L"
									+ (xIndex * baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + baseSize + margin)
									+ " Z\" >"
									+ "</path>");
							if (masuStr != null) {
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) + margin - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize)
										+ "\" fill=\""
										+ "white"
										+ "\" font-size=\""
										+ (baseSize + 2) / 2
										+ "\" textLength=\""
										+ (baseSize + 2) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr
										+ "</text>");
							}
						} else if (up == Wall.EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
							sb.append("<path d=\"M "
									+ (xIndex * baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + margin)
									+ " L"
									+ (xIndex * baseSize + baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + margin)
									+ " L"
									+ (xIndex * baseSize + baseSize + baseSize)
									+ " "
									+ (yIndex * baseSize + baseSize + margin)
									+ " Z\" >"
									+ "</path>");
							if (masuStr != null) {
								sb.append("<text y=\"" + (yIndex * baseSize + (baseSize / 2) + margin - 1)
										+ "\" x=\""
										+ (xIndex * baseSize + baseSize + (baseSize / 2))
										+ "\" fill=\""
										+ "white"
										+ "\" font-size=\""
										+ (baseSize + 2) / 2
										+ "\" textLength=\""
										+ (baseSize + 2) / 2
										+ "\" lengthAdjust=\"spacingAndGlyphs\">"
										+ masuStr
										+ "</text>");
							}
						}
					}
					if (wkField.getCrossPosSet().contains(new Position(yIndex, xIndex))) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 2)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize)
								+ "\" textLength=\""
								+ (baseSize)
								+ "\" fill=\""
								+ "black"
								+ "\" stroke=\"" + "black" + "\" stroke-width=\"1"
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ "┼"
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
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
		// マスの情報
		protected Masu[][] masu;
		// 数字の情報
		protected final Integer[][] numbers;
		// 十字マス
		protected final Set<Position> crossPosSet;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected Wall[][] tateWall;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?reflect/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (numbers[yIndex][xIndex] == null && !crossPosSet.contains(new Position(yIndex, xIndex))) {
					interval++;
					if (interval == 26) {
						sb.append("z");
						interval = 0;
					}
				} else {
					String numStr = null;
					int numTop;
					Integer num = numbers[yIndex][xIndex];
					if (num != null) {
						Wall up = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
						Wall right = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
						Wall down = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
						Wall left = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
						if (up == Wall.NOT_EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.EXISTS && left == Wall.EXISTS) {
							numTop = 1;
						} else if (up == Wall.NOT_EXISTS && right == Wall.EXISTS
								&& down == Wall.EXISTS && left == Wall.NOT_EXISTS) {
							numTop = 2;
						} else if (up == Wall.EXISTS && right == Wall.EXISTS
								&& down == Wall.NOT_EXISTS && left == Wall.NOT_EXISTS) {
							numTop = 3;
						} else if (up == Wall.EXISTS && right == Wall.NOT_EXISTS
								&& down == Wall.NOT_EXISTS && left == Wall.EXISTS) {
							numTop = 4;
						} else {
							throw new IllegalStateException();
						}
						if (num >= 16) {
							numTop = numTop + 5;
						}
						numStr = numTop + Integer.toHexString(num);
					} else if (crossPosSet.contains(new Position(yIndex, xIndex))) {
						numStr = "5";
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
			int kuroCnt = 0;
			int numberCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						kuroCnt++;
						if (numbers[yIndex][xIndex] != 0) {
							numberCnt++;
						}
					}
				}
			}
			return String.valueOf(crossPosSet.size() + "/" + numberCnt + "/" + kuroCnt);
		}

		public Set<Position> getCrossPosSet() {
			return crossPosSet;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			crossPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
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
			int wallPattern = 0;
			boolean adjust = false;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				if (wallPattern == 0) {
					int interval = ALPHABET.indexOf(ch) + 1;
					if (interval != 0) {
						index = index + interval;
					} else {
						int val = Character.getNumericValue(ch);
						if (val == 5) {
							Position crossPos = new Position(index / getXLength(), index % getXLength());
							crossPosSet.add(crossPos);
							masu[crossPos.getyIndex()][crossPos.getxIndex()] = Masu.NOT_BLACK;
							tateWall[crossPos.getyIndex() - 1][crossPos.getxIndex()] = Wall.NOT_EXISTS;
							yokoWall[crossPos.getyIndex()][crossPos.getxIndex()] = Wall.NOT_EXISTS;
							tateWall[crossPos.getyIndex()][crossPos.getxIndex()] = Wall.NOT_EXISTS;
							yokoWall[crossPos.getyIndex()][crossPos.getxIndex() - 1] = Wall.NOT_EXISTS;
							index++;
						} else {
							if (6 <= val && val <= 9) {
								val = val - 5;
								adjust = true;
							}
							wallPattern = val;
						}
					}
				} else {
					Position numberPos = new Position(index / getXLength(), index % getXLength());
					masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.NOT_BLACK;
					int oneNumber;
					if (adjust) {
						i++;
						oneNumber = Character.getNumericValue(ch) * 16
								+ Character.getNumericValue(param.charAt(i));
					} else {
						oneNumber = Character.getNumericValue(ch);
					}
					numbers[numberPos.getyIndex()][numberPos.getxIndex()] = oneNumber;
					// 周囲の壁を閉鎖
					if (numberPos.getyIndex() != 0) {
						if (wallPattern == 1 || wallPattern == 2) {
							tateWall[numberPos.getyIndex() - 1][numberPos.getxIndex()] = Wall.NOT_EXISTS;
						} else {
							tateWall[numberPos.getyIndex() - 1][numberPos.getxIndex()] = Wall.EXISTS;
						}
					}
					if (numberPos.getxIndex() != getXLength() - 1) {
						if (wallPattern == 1 || wallPattern == 4) {
							yokoWall[numberPos.getyIndex()][numberPos.getxIndex()] = Wall.NOT_EXISTS;
						} else {
							yokoWall[numberPos.getyIndex()][numberPos.getxIndex()] = Wall.EXISTS;
						}
					}
					if (numberPos.getyIndex() != getYLength() - 1) {
						if (wallPattern == 3 || wallPattern == 4) {
							tateWall[numberPos.getyIndex()][numberPos.getxIndex()] = Wall.NOT_EXISTS;
						} else {
							tateWall[numberPos.getyIndex()][numberPos.getxIndex()] = Wall.EXISTS;
						}
					}
					if (numberPos.getxIndex() != 0) {
						if (wallPattern == 2 || wallPattern == 3) {
							yokoWall[numberPos.getyIndex()][numberPos.getxIndex() - 1] = Wall.NOT_EXISTS;
						} else {
							yokoWall[numberPos.getyIndex()][numberPos.getxIndex() - 1] = Wall.EXISTS;
						}
					}
					adjust = false;
					index++;
					wallPattern = 0;
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			numbers = other.numbers;
			crossPosSet = other.crossPosSet;
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
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

		public Field(int height, int width) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			crossPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
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
			masu = new Masu[other.getYLength()][other.getXLength()];
			numbers = new Integer[other.getYLength()][other.getXLength()];
			crossPosSet = new HashSet<>(other.crossPosSet);
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
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

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

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
					if (crossPosSet.contains(new Position(yIndex, xIndex))) {
						sb.append("┼");
					} else if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						}
						String capacityStr = String.valueOf(numbers[yIndex][xIndex]);
						int index = HALF_NUMS.indexOf(capacityStr);
						if (index >= 0) {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						} else {
							sb.append(capacityStr);
						}
					} else {
						sb.append(masu[yIndex][xIndex]);
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
		 * 丸の上では壁の数は0か2(直進)になる。
		 * 丸でない場所では壁の数は2になる。違反の場合false。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (!crossPosSet.contains(new Position(yIndex, xIndex))) {
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
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							// 自分が不確定マスなら壁は2マスか4マス
							if ((existsCount == 3 && notExistsCount == 1)
									|| notExistsCount > 2) {
								return false;
							}
							if (existsCount > 2) {
								masu[yIndex][xIndex] = Masu.BLACK;
							} else if (notExistsCount != 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							// 自分が黒マスなら壁は4マス
							if (notExistsCount > 0) {
								return false;
							}
							// 周囲の壁を閉鎖
							if (yIndex != 0) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (xIndex != getXLength() - 1) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (yIndex != getYLength() - 1) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (xIndex != 0) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
						} else if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							// 自分が白マスなら壁は2マス
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
				}
			}
			return true;
		}

		/**
		 * 白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> allPosSet = new HashSet<>();
			Position typicalWhitePos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (!crossPosSet.contains(pos)) {
						allPosSet.add(pos);
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							if (typicalWhitePos == null) {
								typicalWhitePos = pos;
							}
						}
					}
				}
			}
			if (typicalWhitePos == null) {
				return true;
			} else {
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				setContinuePosSet(typicalWhitePos, continuePosSet, null);
				allPosSet.removeAll(continuePosSet);
				for (Position pos : allPosSet) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
						return false;
					}
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
				}
				return true;
			}
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスを無制限につなげていく。
		 * 十字マスは飛び越えて向こう側に行く。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					int i = 2;
					while (crossPosSet.contains(nextPos)) {
						nextPos = new Position(pos.getyIndex() - i, pos.getxIndex());
						i++;
					}
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);

				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					int i = 2;
					while (crossPosSet.contains(nextPos)) {
						nextPos = new Position(pos.getyIndex(), pos.getxIndex() + i);
						i++;
					}
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);

				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					int i = 2;
					while (crossPosSet.contains(nextPos)) {
						nextPos = new Position(pos.getyIndex() + i, pos.getxIndex());
						i++;
					}
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);

				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					int i = 2;
					while (crossPosSet.contains(nextPos)) {
						nextPos = new Position(pos.getyIndex(), pos.getxIndex() - i);
						i++;
					}
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);

				}
			}
		}

		/**
		 * 数字マスから自身+上下左右にそれぞれ何マス白マスを伸ばせる可能性があるか調べ、
		 * ある3方向を足しても満たない場合、残る1方向の不足分を伸ばす。
		 * 4方向を足しても数字が届かない場合falseを返す。
		 * また、ある3方向を足した数が確定白マスに等しければその先をふさぐ。
		 * この時は、確定白マスが数字を上回っていたらfalseを返す。
		 */
		public boolean limitSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] == 0) {
							continue;
						}
						int upSpaceCnt = 0;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (tateWall[targetY][xIndex] == Wall.EXISTS) {
								break;
							}
							upSpaceCnt++;
						}
						int rightSpaceCnt = 0;
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (yokoWall[yIndex][targetX - 1] == Wall.EXISTS) {
								break;
							}
							rightSpaceCnt++;
						}
						int downSpaceCnt = 0;
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (tateWall[targetY - 1][xIndex] == Wall.EXISTS) {
								break;
							}
							downSpaceCnt++;
						}
						int leftSpaceCnt = 0;
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (yokoWall[yIndex][targetX] == Wall.EXISTS) {
								break;
							}
							leftSpaceCnt++;
						}
						int aroundSpaceCnt = 1 + upSpaceCnt + rightSpaceCnt + downSpaceCnt + leftSpaceCnt;
						if (aroundSpaceCnt < numbers[yIndex][xIndex]) {
							return false;
						} else {
							int fixedWhiteUp = numbers[yIndex][xIndex]
									- (1 + rightSpaceCnt + downSpaceCnt + leftSpaceCnt);
							int fixedWhiteRight = numbers[yIndex][xIndex]
									- (1 + upSpaceCnt + downSpaceCnt + leftSpaceCnt);
							int fixedWhiteDown = numbers[yIndex][xIndex]
									- (1 + upSpaceCnt + rightSpaceCnt + leftSpaceCnt);
							int fixedWhitetLeft = numbers[yIndex][xIndex]
									- (1 + upSpaceCnt + rightSpaceCnt + downSpaceCnt);
							if (fixedWhiteUp > 0) {
								for (int i = 1; i <= fixedWhiteUp; i++) {
									if (tateWall[yIndex - i][xIndex] == Wall.EXISTS) {
										return false;
									}
									tateWall[yIndex - i][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (fixedWhiteRight > 0) {
								for (int i = 1; i <= fixedWhiteRight; i++) {
									if (yokoWall[yIndex][xIndex + i - 1] == Wall.EXISTS) {
										return false;
									}
									yokoWall[yIndex][xIndex + i - 1] = Wall.NOT_EXISTS;
								}
							}
							if (fixedWhiteDown > 0) {
								for (int i = 1; i <= fixedWhiteDown; i++) {
									if (tateWall[yIndex + i - 1][xIndex] == Wall.EXISTS) {
										return false;
									}
									tateWall[yIndex + i - 1][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (fixedWhitetLeft > 0) {
								for (int i = 1; i <= fixedWhitetLeft; i++) {
									if (yokoWall[yIndex][xIndex - i] == Wall.EXISTS) {
										return false;
									}
									yokoWall[yIndex][xIndex - i] = Wall.NOT_EXISTS;
								}
							}
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] == 0) {
							continue;
						}
						int upSpaceCnt = 0;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (tateWall[targetY][xIndex] != Wall.NOT_EXISTS) {
								break;
							}
							upSpaceCnt++;
						}
						int rightSpaceCnt = 0;
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (yokoWall[yIndex][targetX - 1] != Wall.NOT_EXISTS) {
								break;
							}
							rightSpaceCnt++;
						}
						int downSpaceCnt = 0;
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (tateWall[targetY - 1][xIndex] != Wall.NOT_EXISTS) {
								break;
							}
							downSpaceCnt++;
						}
						int leftSpaceCnt = 0;
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (yokoWall[yIndex][targetX] != Wall.NOT_EXISTS) {
								break;
							}
							leftSpaceCnt++;
						}
						int aroundSpaceCnt = 1 + upSpaceCnt + rightSpaceCnt + downSpaceCnt + leftSpaceCnt;
						if (aroundSpaceCnt > numbers[yIndex][xIndex]) {
							return false;
						} else if (aroundSpaceCnt == numbers[yIndex][xIndex]) {
							if (yIndex - upSpaceCnt > 0) {
								tateWall[yIndex - upSpaceCnt - 1][xIndex] = Wall.EXISTS;
							}
							if (xIndex + rightSpaceCnt < getXLength() - 1) {
								yokoWall[yIndex][xIndex + rightSpaceCnt] = Wall.EXISTS;
							}
							if (yIndex + downSpaceCnt < getYLength() - 1) {
								tateWall[yIndex + downSpaceCnt][xIndex] = Wall.EXISTS;
							}
							if (xIndex - leftSpaceCnt > 0) {
								yokoWall[yIndex][xIndex - leftSpaceCnt - 1] = Wall.EXISTS;
							}
						}
					}
				}
			}
			return true;
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
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!limitSolve()) {
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

	public ReflectSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public ReflectSolver(Field field) {
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
		System.out.println(new ReflectSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 20));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 20 / 3);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 20).toString() + "(Lv:" + level + ")";
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
			field.masu = virtual2.masu;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
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
			field.masu = virtual2.masu;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}
}
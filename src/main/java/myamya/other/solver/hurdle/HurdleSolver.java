package myamya.other.solver.hurdle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.PenpaEditGeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.PenpaEditLib;
import myamya.other.solver.PenpaEditLib.PuzzleType;
import myamya.other.solver.Solver;

public class HurdleSolver implements Solver {

	public static class HurdleGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class HurdleSolverForGenerator extends HurdleSolver {
			private final int limit;

			public HurdleSolverForGenerator(Field field, int limit) {
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
							if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt, 0, 0, true)) {
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
			protected boolean candSolve(Field field, int recursive, int initY, int initX, boolean isYoko) {
				if (this.count >= limit) {
					throw new CountOverException();
				} else {
					return super.candSolve(field, recursive, initY, initX, isYoko);
				}
			}
		}

		private final int height;
		private final int width;

		public HurdleGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new HurdleGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			String solutionStr;
			HurdleSolver.Field wkField = new Field(height, width);
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
							HurdleSolver.Field virtual = new Field(wkField);
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
				// 数字を配置
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						numberPosList.add(new Position(yIndex, xIndex));
						int idx = 0;
						int upBlackCnt = 0;
						int rightBlackCnt = 0;
						int downBlackCnt = 0;
						int leftBlackCnt = 0;
						while (yIndex - idx >= 0) {
							if (yIndex - idx == 0 || wkField.tateWall[yIndex - 1 - idx][xIndex] == Wall.EXISTS) {
								upBlackCnt++;
								idx++;
							} else {
								break;
							}
						}
						idx = 0;
						while (xIndex + idx <= wkField.getXLength() - 1) {
							if (xIndex + idx == wkField.getXLength() - 1
									|| wkField.yokoWall[yIndex][xIndex + idx] == Wall.EXISTS) {
								rightBlackCnt++;
								idx++;
							} else {
								break;
							}
						}
						idx = 0;
						while (yIndex + idx <= wkField.getYLength() - 1) {
							if (yIndex + idx == wkField.getYLength() - 1
									|| wkField.tateWall[yIndex + idx][xIndex] == Wall.EXISTS) {
								downBlackCnt++;
								idx++;
							} else {
								break;
							}
						}
						idx = 0;
						while (xIndex - idx >= 0) {
							if (xIndex - idx == 0 || wkField.yokoWall[yIndex][xIndex - 1 - idx] == Wall.EXISTS) {
								leftBlackCnt++;
								idx++;
							} else {
								break;
							}
						}
						wkField.numbers[yIndex][xIndex] = upBlackCnt;
						if (wkField.numbers[yIndex][xIndex] < rightBlackCnt) {
							wkField.numbers[yIndex][xIndex] = rightBlackCnt;
						}
						if (wkField.numbers[yIndex][xIndex] < downBlackCnt) {
							wkField.numbers[yIndex][xIndex] = downBlackCnt;
						}
						if (wkField.numbers[yIndex][xIndex] < leftBlackCnt) {
							wkField.numbers[yIndex][xIndex] = leftBlackCnt;
						}
					}
				}
				// System.out.println(wkField);
				// マスを戻す＆回答記憶
				solutionStr = PenpaEditLib.convertSolutionRoom(height, wkField.yokoWall, wkField.tateWall);
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
				level = new HurdleSolverForGenerator(wkField, 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						HurdleSolver.Field virtual = new Field(wkField);
						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						int solveResult = new HurdleSolverForGenerator(virtual, 8000).solve2();
						if (solveResult != -1) {
							wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							level = solveResult;
						}
					}
					break;
				}
			}
			// ヒント数字を含む盤面変換
			String fieldStr = PenpaEditLib.convertNumbersField(wkField.numbers, PuzzleType.EDGESUB_CIRCLE);
			System.out.println(fieldStr);
			System.out.println(solutionStr);
			level = (int) Math.sqrt(level * 8 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
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
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
						int wkIdx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (wkIdx >= 0) {
							masuStr = FULL_NUMS.substring(wkIdx / 2, wkIdx / 2 + 1);
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
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1
							|| wkField.getYokoWall()[yIndex][xIndex] == Wall.EXISTS;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						if (xIndex != -1 && xIndex != wkField.getXLength() - 1
								&& wkField.getYokoWall()[yIndex][xIndex] == Wall.EXISTS) {
							sb.append("stroke=\"green\" ");
						} else {
							sb.append("stroke=\"#000\" ");
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
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1
							|| wkField.getTateWall()[yIndex][xIndex] == Wall.EXISTS;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						if (yIndex != -1 && yIndex != wkField.getYLength() - 1
								&& wkField.getTateWall()[yIndex][xIndex] == Wall.EXISTS) {
							sb.append("stroke=\"green\" ");
						} else {
							sb.append("stroke=\"#000\" ");

						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
				}
			}
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new PenpaEditGeneratorResult(status, sb.toString(), link, level, "", fieldStr, solutionStr);
		}

	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";

		// 数字の情報
		private final Integer[][] numbers;
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
			return PenpaEditLib.PENPA_EDIT_DUMMY_URL;
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

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public Field(int height, int width) {
			numbers = new Integer[height][width];
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

		public Field(Field other) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
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

		public Field(String fieldStr) {
			String[] fieldInfo = fieldStr.split("\n")[0].split(",");
			Integer yLength = Integer.valueOf(fieldInfo[2]);
			Integer xLength = Integer.valueOf(fieldInfo[1]);
			numbers = PenpaEditLib.getNumbers(fieldStr);
			yokoWall = new Wall[yLength][xLength - 1];
			tateWall = new Wall[yLength - 1][xLength];
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
		 * 矢印の数字は壁の枚数。また、直後には必ず壁がある。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int oneNumber = numbers[yIndex][xIndex];
						int idx = 0;
						int upBlackCnt = 0;
						int upCanSpaceCnt = 0;
						int rightBlackCnt = 0;
						int rightCanSpaceCnt = 0;
						int downBlackCnt = 0;
						int downCanSpaceCnt = 0;
						int leftBlackCnt = 0;
						int leftCanSpaceCnt = 0;
						while (yIndex - idx >= 0) {
							if (yIndex - idx == 0) {
								if (upBlackCnt == upCanSpaceCnt) {
									upBlackCnt++;
								}
								upCanSpaceCnt++;
								break;
							}
							if (tateWall[yIndex - 1 - idx][xIndex] == Wall.NOT_EXISTS) {
								break;
							} else {
								if (tateWall[yIndex - 1 - idx][xIndex] == Wall.EXISTS && upBlackCnt == upCanSpaceCnt) {
									upBlackCnt++;
								}
								upCanSpaceCnt++;
							}
							idx++;
						}
						idx = 0;
						while (xIndex + idx <= getXLength() - 1) {
							if (xIndex + idx == getXLength() - 1) {
								if (rightBlackCnt == rightCanSpaceCnt) {
									rightBlackCnt++;
								}
								rightCanSpaceCnt++;
								break;
							}
							if (yokoWall[yIndex][xIndex + idx] == Wall.NOT_EXISTS) {
								break;
							} else {
								if (yokoWall[yIndex][xIndex + idx] == Wall.EXISTS
										&& rightBlackCnt == rightCanSpaceCnt) {
									rightBlackCnt++;
								}
								rightCanSpaceCnt++;
							}
							idx++;
						}
						idx = 0;
						while (yIndex + idx <= getYLength() - 1) {
							if (yIndex + idx == getYLength() - 1) {
								if (downBlackCnt == downCanSpaceCnt) {
									downBlackCnt++;
								}
								downCanSpaceCnt++;
								break;
							}
							if (tateWall[yIndex + idx][xIndex] == Wall.NOT_EXISTS) {
								break;
							} else {
								if (tateWall[yIndex + idx][xIndex] == Wall.EXISTS && downBlackCnt == downCanSpaceCnt) {
									downBlackCnt++;
								}
								downCanSpaceCnt++;
							}
							idx++;
						}
						idx = 0;
						while (xIndex - idx >= 0) {
							if (xIndex - idx == 0) {
								if (leftBlackCnt == leftCanSpaceCnt) {
									leftBlackCnt++;
								}
								leftCanSpaceCnt++;
								break;
							}
							if (yokoWall[yIndex][xIndex - 1 - idx] == Wall.NOT_EXISTS) {
								break;
							} else {
								if (yokoWall[yIndex][xIndex - 1 - idx] == Wall.EXISTS
										&& leftBlackCnt == leftCanSpaceCnt) {
									leftBlackCnt++;
								}
								leftCanSpaceCnt++;
							}
							idx++;
						}
						if (oneNumber < upBlackCnt || oneNumber < rightBlackCnt || oneNumber < downBlackCnt
								|| oneNumber < leftBlackCnt) {
							return false;
						}
						if (oneNumber > upCanSpaceCnt && oneNumber > rightCanSpaceCnt && oneNumber > downCanSpaceCnt
								&& oneNumber > leftCanSpaceCnt) {
							return false;
						}
						if (oneNumber == upBlackCnt) {
							if (yIndex - 1 - oneNumber > 0) {
								tateWall[yIndex - 1 - oneNumber][xIndex] = Wall.NOT_EXISTS;
							}
						}
						if (oneNumber == rightBlackCnt) {
							if (xIndex + oneNumber < getXLength() - 1) {
								yokoWall[yIndex][xIndex + oneNumber] = Wall.NOT_EXISTS;
							}
						}
						if (oneNumber == downBlackCnt) {
							if (yIndex + oneNumber < getYLength() - 1) {
								tateWall[yIndex + oneNumber][xIndex] = Wall.NOT_EXISTS;
							}
						}
						if (oneNumber == leftBlackCnt) {
							if (xIndex - 1 - oneNumber > 0) {
								yokoWall[yIndex][xIndex - 1 - oneNumber] = Wall.NOT_EXISTS;
							}
						}
						idx = 0;
						if (oneNumber == upCanSpaceCnt && oneNumber > rightCanSpaceCnt && oneNumber > downCanSpaceCnt
								&& oneNumber > leftCanSpaceCnt) {
							while (yIndex - 1 - idx >= 0) {
								if (tateWall[yIndex - 1 - idx][xIndex] != Wall.NOT_EXISTS) {
									tateWall[yIndex - 1 - idx][xIndex] = Wall.EXISTS;
								} else {
									break;
								}
								idx++;
							}
						} else if (oneNumber > upCanSpaceCnt && oneNumber == rightCanSpaceCnt
								&& oneNumber > downCanSpaceCnt && oneNumber > leftCanSpaceCnt) {
							while (xIndex + idx < getXLength() - 1) {
								if (yokoWall[yIndex][xIndex + idx] != Wall.NOT_EXISTS) {
									yokoWall[yIndex][xIndex + idx] = Wall.EXISTS;
								} else {
									break;
								}
								idx++;
							}
						} else if (oneNumber > upCanSpaceCnt && oneNumber > rightCanSpaceCnt
								&& oneNumber == downCanSpaceCnt && oneNumber > leftCanSpaceCnt) {
							while (yIndex + idx < getYLength() - 1) {
								if (tateWall[yIndex + idx][xIndex] != Wall.NOT_EXISTS) {
									tateWall[yIndex + idx][xIndex] = Wall.EXISTS;
								} else {
									break;
								}
								idx++;
							}
						} else if (oneNumber > upCanSpaceCnt && oneNumber > rightCanSpaceCnt
								&& oneNumber > downCanSpaceCnt && oneNumber == leftCanSpaceCnt) {
							while (xIndex - 1 - idx >= 0) {
								if (yokoWall[yIndex][xIndex - 1 - idx] != Wall.NOT_EXISTS) {
									yokoWall[yIndex][xIndex - 1 - idx] = Wall.EXISTS;
								} else {
									break;
								}
								idx++;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * マスから見て前後左右の壁は必ず2つになる。
		 */
		private boolean masuSolve() {
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

		/**
		 * マスが1つながりになっていない場合falseを返す。
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
			if (!numberSolve()) {
				return false;
			}
			if (!masuSolve()) {
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

	public HurdleSolver(Field field) {
		this.field = new Field(field);
	}

	// penpa-edit向けコンストラクタ
	public HurdleSolver(String fieldStr) {
		field = new Field(fieldStr);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String fieldStr = "square,10,10,38,0,1,1,418,418,286,286\n" + "[0,0,0,0]\n"
				+ "[\"1\",\"2\",\"1\"]~zS~[\"\",1]\n"
				+ "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{\"46\":[\"1\",1,\"1\"],\"64\":[\"3\",1,\"1\"],\"66\":[\"1\",1,\"1\"],\"73\":[\"1\",1,\"1\"],\"87\":[\"2\",1,\"1\"],\"89\":[\"3\",1,\"1\"],\"93\":[\"1\",1,\"1\"],\"94\":[\"1\",1,\"1\"],\"100\":[\"1\",1,\"1\"],\"105\":[\"1\",1,\"1\"],\"116\":[\"1\",1,\"1\"],\"117\":[\"4\",1,\"1\"],\"120\":[\"2\",1,\"1\"],\"123\":[\"2\",1,\"1\"],\"133\":[\"3\",1,\"1\"],\"137\":[\"1\",1,\"1\"],\"159\":[\"1\",1,\"1\"]},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n"
				+ "\n"
				+ "[30,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1]	";
		System.out.println(new HurdleSolver(fieldStr).solve());
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
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt, 0, 0, true)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count * 8));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 8 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 8).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive, int initY, int initX, boolean isYoko) {
		String str = field.getStateDump();
		if (isYoko) {
			for (int yIndex = initY; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = initX; xIndex < field.getXLength() - 1; xIndex++) {
					if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
						count++;
						if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
							return false;
						}
					}
				}
				initX = 0;
			}
			initY = 0;
		}
		for (int yIndex = initY; yIndex < field.getYLength() - 1; yIndex++) {
			for (int xIndex = initX; xIndex < field.getXLength(); xIndex++) {
				if (field.tateWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandTateWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
			initX = 0;
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive, 0, 0, true);
		}
		return true;
	}

	private boolean oneCandYokoWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1, yIndex, xIndex, true)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1, yIndex, xIndex, true)) {
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
			if (!candSolve(virtual, recursive - 1, yIndex, xIndex, false)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1, yIndex, xIndex, false)) {
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
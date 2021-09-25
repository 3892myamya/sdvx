package myamya.other.solver.wafusuma;

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
import myamya.other.solver.PenpaEditLib.TateYokoNumbers;
import myamya.other.solver.Solver;

public class WafusumaSolver implements Solver {
	public static class WafusumaGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class WafusumaSolverForGenerator extends WafusumaSolver {

			private final int limit;

			public WafusumaSolverForGenerator(Field field, int limit) {
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

		public WafusumaGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new WafusumaGenerator(5, 5).generate();
		}

		@Override
		public GeneratorResult generate() {
			String solutionStr;
			WafusumaSolver.Field wkField = new WafusumaSolver.Field(height, width);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < (height * (width - 1)) + ((height - 1) * width); i++) {
				indexList.add(i);
			}
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
						for (int i = 0; i < 4; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							WafusumaSolver.Field virtual = new WafusumaSolver.Field(wkField);
							if (masuNum < 3) {
								if (toYokoWall) {
									virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
								} else {
									virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
							} else {
								if (toYokoWall) {
									virtual.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								} else {
									virtual.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.numbers = virtual.numbers;
								wkField.yokoWall = virtual.yokoWall;
								wkField.tateWall = virtual.tateWall;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new WafusumaSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// フスマを作る。
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						if (wkField.yokoWall[yIndex][xIndex] == Wall.EXISTS) {
							wkField.yokoNumbers[yIndex][xIndex] = wkField.numbers[yIndex][xIndex]
									+ wkField.numbers[yIndex][xIndex + 1];
						}
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.tateWall[yIndex][xIndex] == Wall.EXISTS) {
							wkField.tateNumbers[yIndex][xIndex] = wkField.numbers[yIndex][xIndex]
									+ wkField.numbers[yIndex + 1][xIndex];
						}
					}
				}
				// 解答の記憶
				solutionStr = PenpaEditLib.convertSolutionRoom(height, wkField.yokoWall, wkField.tateWall);
				System.out.println(wkField);
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.numbers[yIndex][xIndex] = null;
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						if (wkField.yokoNumbers[yIndex][xIndex] == null) {
							wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
						}
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.tateNumbers[yIndex][xIndex] == null) {
							wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
						}
					}
				}
				// 解けるかな？
				level = new WafusumaSolverForGenerator(new WafusumaSolver.Field(wkField), 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new WafusumaSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(indexList);
					for (int posBase : indexList) {
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
						WafusumaSolver.Field virtual = new WafusumaSolver.Field(wkField, true);
						if (toYokoWall) {
							virtual.yokoWall[yIndex][xIndex] = Wall.SPACE;
							virtual.yokoNumbers[yIndex][xIndex] = null;
						} else {
							virtual.tateWall[yIndex][xIndex] = Wall.SPACE;
							virtual.tateNumbers[yIndex][xIndex] = null;
						}
						int solveResult = new WafusumaSolverForGenerator(virtual, 5000).solve2();
						if (solveResult != -1) {
							if (toYokoWall) {
								wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
								wkField.yokoNumbers[yIndex][xIndex] = null;
							} else {
								wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
								wkField.tateNumbers[yIndex][xIndex] = null;
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			// ヒント数字を含む盤面変換
			String fieldStr = PenpaEditLib.convertWafusumaField(height, wkField.yokoNumbers, wkField.tateNumbers);
			level = (int) Math.sqrt(level / 5 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">penpa-editで解く</a>";
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
			// 数字描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
					Integer number = wkField.getYokoNumbers()[yIndex][xIndex];
					if (number != null) {
						String numberStr = String.valueOf(number);
						int numIdx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (numIdx >= 0) {
							masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + (baseSize / 2 - 5)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 7) + "\" x=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2) + 5) + "\" font-size=\""
								+ (baseSize - 11) + "\" textLength=\"" + (baseSize - 11)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
					}
				}
			}
			// 数字描画
			for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					Integer number = wkField.getTateNumbers()[yIndex][xIndex];
					if (number != null) {
						String numberStr = String.valueOf(number);
						int numIdx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (numIdx >= 0) {
							masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 5)
								+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + (baseSize / 2) + margin - 7)
								+ "\" x=\"" + (xIndex * baseSize + baseSize + 5) + "\" font-size=\"" + (baseSize - 11)
								+ "\" textLength=\"" + (baseSize - 11) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
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
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 横にまたぐ数字
		private Integer[][] yokoNumbers;
		// 縦にまたぐ数字
		private Integer[][] tateNumbers;
		// 数字の情報。なくてもいいが、あったほうが早い
		private Integer[][] numbers;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;

		public Integer[][] getYokoNumbers() {
			return yokoNumbers;
		}

		public Integer[][] getTateNumbers() {
			return tateNumbers;
		}

		public String getHintCount() {
			int numberCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoNumbers[yIndex][xIndex] != null) {
						numberCnt++;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateNumbers[yIndex][xIndex] != null) {
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
			yokoNumbers = new Integer[height][width - 1];
			tateNumbers = new Integer[height - 1][width];
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
			yokoNumbers = other.yokoNumbers;
			tateNumbers = other.tateNumbers;
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

		public Field(Field other, boolean flag) {
			yokoNumbers = new Integer[other.getYLength()][other.getXLength() - 1];
			tateNumbers = new Integer[other.getYLength() - 1][other.getXLength()];
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
					yokoNumbers[yIndex][xIndex] = other.yokoNumbers[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
					tateNumbers[yIndex][xIndex] = other.tateNumbers[yIndex][xIndex];
				}
			}
		}

		public Field(String fieldStr) {
			String[] fieldInfo = fieldStr.split("\n")[0].split(",");
			Integer yLength = Integer.valueOf(fieldInfo[2]);
			Integer xLength = Integer.valueOf(fieldInfo[1]);

			TateYokoNumbers tateYokoNumbers = PenpaEditLib.getTateYokoNumbers(fieldStr);
			yokoNumbers = tateYokoNumbers.getYokoNumbers();
			tateNumbers = tateYokoNumbers.getTateNumbers();
			numbers = new Integer[yLength][xLength];
			yokoWall = new Wall[yLength][xLength - 1];
			tateWall = new Wall[yLength - 1][xLength];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoNumbers[yIndex][xIndex] != null) {
						yokoWall[yIndex][xIndex] = Wall.EXISTS;
					} else {
						yokoWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateNumbers[yIndex][xIndex] != null) {
						tateWall[yIndex][xIndex] = Wall.EXISTS;
					} else {
						tateWall[yIndex][xIndex] = Wall.SPACE;
					}
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
						if (yokoNumbers[yIndex][xIndex] != null) {
							if (yokoNumbers[yIndex][xIndex] > 99) {
								sb.append("99");
							} else if (yokoNumbers[yIndex][xIndex] == -1) {
								sb.append("？");
							} else {
								String capacityStr = String.valueOf(yokoNumbers[yIndex][xIndex]);
								int index = HALF_NUMS.indexOf(capacityStr);
								if (index >= 0) {
									sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
								} else {
									sb.append(capacityStr);
								}
							}
						} else {
							sb.append(yokoWall[yIndex][xIndex]);
						}
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (tateNumbers[yIndex][xIndex] != null) {
							if (tateNumbers[yIndex][xIndex] > 99) {
								sb.append("99");
							} else if (tateNumbers[yIndex][xIndex] == -1) {
								sb.append("？");
							} else {
								String capacityStr = String.valueOf(tateNumbers[yIndex][xIndex]);
								int index = HALF_NUMS.indexOf(capacityStr);
								if (index >= 0) {
									sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
								} else {
									sb.append(capacityStr);
								}
							}
						} else {
							sb.append(tateWall[yIndex][xIndex]);
						}
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
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(numbers[yIndex][xIndex]);
				}
			}
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
		 * 部屋のサイズが超過している場合falseを返す。 部屋が既定サイズに到達している場合、周囲を壁で埋める。
		 */
		public boolean roomSolve() {
			// 横フスマ
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoNumbers[yIndex][xIndex] != null) {
						Position pivotRight = new Position(yIndex, xIndex);
						Position pivotLeft = new Position(yIndex, xIndex + 1);
						Set<Position> continueNotBlackPosSet = new HashSet<>();
						continueNotBlackPosSet.add(pivotRight);
						continueNotBlackPosSet.add(pivotLeft);
						if (!checkAndSetContinuePosSet(pivotRight, continueNotBlackPosSet, null,
								yokoNumbers[yIndex][xIndex])) {
							if (!checkAndSetContinuePosSet(pivotLeft, continueNotBlackPosSet, null,
									yokoNumbers[yIndex][xIndex])) {
								if (continueNotBlackPosSet.size() == yokoNumbers[yIndex][xIndex]) {
									continue;
								} else {
									// サイズ不足
									return false;
								}
							}
						}
						Set<Position> continueWhitePosSet = new HashSet<>();
						continueWhitePosSet.add(pivotRight);
						continueWhitePosSet.add(pivotLeft);
						if (!checkAndSetContinueWhitePosSet(pivotRight, continueWhitePosSet, null,
								yokoNumbers[yIndex][xIndex])) {
							// サイズ超過
							return false;
						}
						if (!checkAndSetContinueWhitePosSet(pivotLeft, continueWhitePosSet, null,
								yokoNumbers[yIndex][xIndex])) {
							// サイズ超過
							return false;
						}
						for (Position pos : continueWhitePosSet) {
							if (yokoNumbers[yIndex][xIndex] == continueWhitePosSet.size()) {
								if (pos.getyIndex() != 0 && !continueWhitePosSet
										.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
								}
								if (pos.getxIndex() != getXLength() - 1 && !continueWhitePosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
								}
								if (pos.getyIndex() != getYLength() - 1 && !continueWhitePosSet
										.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
								}
								if (pos.getxIndex() != 0 && !continueWhitePosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
								}
							}
						}
					}
				}
			}
			// 縦フスマ
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateNumbers[yIndex][xIndex] != null) {
						Position pivotUp = new Position(yIndex, xIndex);
						Position pivotDown = new Position(yIndex + 1, xIndex);
						Set<Position> continueNotBlackPosSet = new HashSet<>();
						continueNotBlackPosSet.add(pivotUp);
						continueNotBlackPosSet.add(pivotDown);
						if (!checkAndSetContinuePosSet(pivotUp, continueNotBlackPosSet, null,
								tateNumbers[yIndex][xIndex])) {
							if (!checkAndSetContinuePosSet(pivotDown, continueNotBlackPosSet, null,
									tateNumbers[yIndex][xIndex])) {
								if (continueNotBlackPosSet.size() == tateNumbers[yIndex][xIndex]) {
									continue;
								} else {
									// サイズ不足
									return false;
								}
							}
						}
						Set<Position> continueWhitePosSet = new HashSet<>();
						continueWhitePosSet.add(pivotUp);
						continueWhitePosSet.add(pivotDown);
						if (!checkAndSetContinueWhitePosSet(pivotUp, continueWhitePosSet, null,
								tateNumbers[yIndex][xIndex])) {
							// サイズ超過
							return false;
						}
						if (!checkAndSetContinueWhitePosSet(pivotDown, continueWhitePosSet, null,
								tateNumbers[yIndex][xIndex])) {
							// サイズ超過
							return false;
						}
						for (Position pos : continueWhitePosSet) {
							if (tateNumbers[yIndex][xIndex] == continueWhitePosSet.size()) {
								if (pos.getyIndex() != 0 && !continueWhitePosSet
										.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
								}
								if (pos.getxIndex() != getXLength() - 1 && !continueWhitePosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
								}
								if (pos.getyIndex() != getYLength() - 1 && !continueWhitePosSet
										.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
								}
								if (pos.getxIndex() != 0 && !continueWhitePosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁なし確定マスをつなぎ、 サイズ超過になると分かった時点でfalseを返す。
		 */
		private boolean checkAndSetContinueWhitePosSet(Position pos, Set<Position> continuePosSet, Direction from,
				int size) {
			if (continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.DOWN, size)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.LEFT, size)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.UP, size)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.RIGHT, size)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 同じ数字の間は壁をなくし、違う数字の間は壁で埋める。 壁で埋められなかったらfalseを返す。
		 */
		public boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int number = numbers[yIndex][xIndex];
						if (yIndex != 0) {
							if (numbers[yIndex - 1][xIndex] != null) {
								if (numbers[yIndex - 1][xIndex] == number) {
									if (tateWall[yIndex - 1][xIndex] == Wall.EXISTS) {
										return false;
									}
									tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
								} else {
									if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
										return false;
									}
									tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
								}
							} else {
								if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
									numbers[yIndex - 1][xIndex] = number;
								}
							}
						}
						if (xIndex != getXLength() - 1) {
							if (numbers[yIndex][xIndex + 1] != null) {
								if (numbers[yIndex][xIndex + 1] == number) {
									if (yokoWall[yIndex][xIndex] == Wall.EXISTS) {
										return false;
									}
									yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								} else {
									if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
										return false;
									}
									yokoWall[yIndex][xIndex] = Wall.EXISTS;
								}
							} else {
								if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
									numbers[yIndex][xIndex + 1] = number;
								}
							}
						}
						if (yIndex != getYLength() - 1) {
							if (numbers[yIndex + 1][xIndex] != null) {
								if (numbers[yIndex + 1][xIndex] == number) {
									if (tateWall[yIndex][xIndex] == Wall.EXISTS) {
										return false;
									}
									tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;

								} else {
									if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
										return false;
									}
									tateWall[yIndex][xIndex] = Wall.EXISTS;
								}
							} else {
								if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
									numbers[yIndex][xIndex] = number;
								}
							}
						}
						if (xIndex != 0) {
							if (numbers[yIndex][xIndex - 1] != null) {
								if (numbers[yIndex][xIndex - 1] == number) {
									if (yokoWall[yIndex][xIndex - 1] == Wall.EXISTS) {
										return false;
									}
									yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
								} else {
									if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
										return false;
									}
									yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
								}
							} else {
								if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
									numbers[yIndex][xIndex - 1] = number;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスをつなぎ、 サイズ不足にならないと分かった時点でtrueを返す。
		 */
		private boolean checkAndSetContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from,
				int size) {
			if (continuePosSet.size() > size) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (checkAndSetContinuePosSet(nextPos, continuePosSet, Direction.DOWN, size)) {
						return true;
					}

				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (checkAndSetContinuePosSet(nextPos, continuePosSet, Direction.LEFT, size)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (checkAndSetContinuePosSet(nextPos, continuePosSet, Direction.UP, size)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (checkAndSetContinuePosSet(nextPos, continuePosSet, Direction.RIGHT, size)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * 柱から伸びる壁は1枚にならない。 違反する場合はfalseを返す。
		 */
		private boolean pileSolve() {
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
					// 壁枚数は1以外
					if (exists == 1 && notExists == 3) {
						return false;
					} else if (notExists == 3) {
						// 壁枚数0確定
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
					} else if (notExists == 2 && exists == 1) {
						// 壁枚数2確定
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
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
				return false;
			}
			if (!roomSolve()) {
				return false;
			}
			if (!pileSolve()) {
				return false;
			}
			if (!standAloneSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 孤立しているマスに数字が入っていない場合、数字を埋める。
		 */
		private boolean standAloneSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continueNotBlackPosSet = new HashSet<>();
						continueNotBlackPosSet.add(pivot);
						checkAndSetContinuePosSet(pivot, continueNotBlackPosSet, null, Integer.MAX_VALUE);
						Set<Position> continueWhitePosSet = new HashSet<>();
						continueWhitePosSet.add(pivot);
						if (checkAndSetContinueWhitePosSet(pivot, continueWhitePosSet, null,
								continueNotBlackPosSet.size())) {
							if (continueWhitePosSet.size() == continueNotBlackPosSet.size()) {
								for (Position pos : continueNotBlackPosSet) {
									if (numbers[pos.getyIndex()][pos.getxIndex()] == null) {
										numbers[pos.getyIndex()][pos.getxIndex()] = continueNotBlackPosSet.size();
									} else if (numbers[pos.getyIndex()][pos.getxIndex()] != continueNotBlackPosSet
											.size()) {
										return false;
									}
								}
							}
						}
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						return false;
					}
				}
			}
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
	protected int count;

	// penpa-edit向けコンストラクタ
	public WafusumaSolver(String fieldStr) {
		field = new Field(fieldStr);
	}

	public WafusumaSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String fieldStr = "square,4,4,38,0,1,1,190,190,91,91\n" + "[0,0,0,0]\n" + "[\"1\",\"2\",\"1\"]~zS~[\"\",1]\n"
				+ "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{\"147\":[\"3\",6,\"6\"],\"162\":[\"5\",6,\"6\"],\"165\":[\"7\",6,\"6\"],\"210\":[\"4\",6,\"6\"],\"220\":[\"6\",6,\"6\"],\"227\":[\"4\",6,\"6\"]},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n"
				+ "\n" + "[18,1,1,1,5,1,1,1,5,1,1,1,5,1,1,1]";
		System.out.println(new WafusumaSolver(fieldStr).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
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
				System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
				System.out.println(field);
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count / 5));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 5 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count / 5).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
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
			field.numbers = virtual2.numbers;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.numbers = virtual.numbers;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		} else {
			// どちらにしても理論
			for (int y = 0; y < field.getYLength(); y++) {
				for (int x = 0; x < field.getXLength(); x++) {
					if (virtual2.numbers[y][x] == virtual.numbers[y][x]) {
						field.numbers[y][x] = virtual.numbers[y][x];
					}
				}
			}
			for (int y = 0; y < field.getYLength(); y++) {
				for (int x = 0; x < field.getXLength() - 1; x++) {
					if (virtual2.yokoWall[y][x] == virtual.yokoWall[y][x]) {
						field.yokoWall[y][x] = virtual.yokoWall[y][x];
					}
				}
			}
			for (int y = 0; y < field.getYLength() - 1; y++) {
				for (int x = 0; x < field.getXLength(); x++) {
					if (virtual2.tateWall[y][x] == virtual.tateWall[y][x]) {
						field.tateWall[y][x] = virtual.tateWall[y][x];
					}
				}
			}
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
			field.numbers = virtual2.numbers;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.numbers = virtual.numbers;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		} else {
			// どちらにしても理論
			for (int y = 0; y < field.getYLength(); y++) {
				for (int x = 0; x < field.getXLength(); x++) {
					if (virtual2.numbers[y][x] == virtual.numbers[y][x]) {
						field.numbers[y][x] = virtual.numbers[y][x];
					}
				}
			}
			for (int y = 0; y < field.getYLength(); y++) {
				for (int x = 0; x < field.getXLength() - 1; x++) {
					if (virtual2.yokoWall[y][x] == virtual.yokoWall[y][x]) {
						field.yokoWall[y][x] = virtual.yokoWall[y][x];
					}
				}
			}
			for (int y = 0; y < field.getYLength() - 1; y++) {
				for (int x = 0; x < field.getXLength(); x++) {
					if (virtual2.tateWall[y][x] == virtual.tateWall[y][x]) {
						field.tateWall[y][x] = virtual.tateWall[y][x];
					}
				}
			}
		}
		return true;
	}
}
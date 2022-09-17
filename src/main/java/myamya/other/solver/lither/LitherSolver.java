package myamya.other.solver.lither;

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
import myamya.other.solver.HintPattern;
import myamya.other.solver.Solver;

public class LitherSolver implements Solver {
	public static class LitherGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class LitherSolverForGenerator extends LitherSolver {
			private final int limit;

			public LitherSolverForGenerator(Field field, int limit) {
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
		private final HintPattern hintPattern;

		public LitherGenerator(int height, int width, HintPattern hintPattern) {
			this.height = height;
			this.width = width;
			this.hintPattern = hintPattern;
		}

		public static void main(String[] args) {
			new LitherGenerator(10, 10, HintPattern.getByVal(0, 10, 10)).generate();
		}

		@Override
		public GeneratorResult generate() {
			LitherSolver.Field wkField = new LitherSolver.Field(height, width);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < (height * (width + 1)) + ((height + 1) * width); i++) {
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
					if (posBase < height * (width + 1)) {
						toYokoWall = true;
						yIndex = posBase / (width + 1);
						xIndex = posBase % (width + 1);
					} else {
						toYokoWall = false;
						posBase = posBase - (height * (width + 1));
						yIndex = posBase / width;
						xIndex = posBase % width;
					}
					if ((toYokoWall && wkField.yokoExtraWall[yIndex][xIndex] == Wall.SPACE)
							|| (!toYokoWall && wkField.tateExtraWall[yIndex][xIndex] == Wall.SPACE)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							LitherSolver.Field virtual = new LitherSolver.Field(wkField, true);
							if (masuNum < 1) {
								if (toYokoWall) {
									virtual.yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
								} else {
									virtual.tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
							} else if (masuNum < 2) {
								if (toYokoWall) {
									virtual.yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								} else {
									virtual.tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.yokoExtraWall = virtual.yokoExtraWall;
								wkField.tateExtraWall = virtual.tateExtraWall;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new LitherSolver.Field(height, width);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// できるだけ埋める
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						int existsCount = 0;
						Wall wallUp = wkField.tateExtraWall[yIndex][xIndex];
						if (wallUp == Wall.EXISTS) {
							existsCount++;
						}
						Wall wallRight = wkField.yokoExtraWall[yIndex][xIndex + 1];
						if (wallRight == Wall.EXISTS) {
							existsCount++;
						}
						Wall wallDown = wkField.tateExtraWall[yIndex + 1][xIndex];
						if (wallDown == Wall.EXISTS) {
							existsCount++;
						}
						Wall wallLeft = wkField.yokoExtraWall[yIndex][xIndex];
						if (wallLeft == Wall.EXISTS) {
							existsCount++;
						}
						wkField.numbers[yIndex][xIndex] = existsCount;
					}
				}
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() + 1; xIndex++) {
						wkField.yokoExtraWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength() + 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.tateExtraWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				// 解けるかな？
				level = new LitherSolverForGenerator(wkField, 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new LitherSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					List<Set<Position>> numberPosSetList = hintPattern.getPosSetList();
					Collections.shuffle(numberPosSetList);
					for (Set<Position> numberPosSet : numberPosSetList) {
						LitherSolver.Field virtual = new LitherSolver.Field(wkField, true);
						for (Position numberPos : numberPosSet) {
							virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						}
						// TODO 本当はもっとでかくできるが…
						int solveResult = new LitherSolverForGenerator(virtual, 4000).solve2();
						if (solveResult != -1) {
							for (Position numberPos : numberPosSet) {
								wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 8 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">pzprxsで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					Integer number = wkField.getNumbers()[yIndex][xIndex];
					if (number != null) {
						String numberStr = String.valueOf(number);
						int numIdx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (numIdx >= 0) {
							masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
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
			// 点描画
			for (int yIndex = 0; yIndex < wkField.getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength() + 1; xIndex++) {
					sb.append(
							"<circle cy=\"" + (yIndex * baseSize + margin) + "\" cx=\"" + (xIndex * baseSize + baseSize)
									+ "\" r=\"" + 1 + "\" fill=\"black\", stroke=\"black\">" + "</circle>");
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
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の情報
		private final Integer[][] numbers;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,-1と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] yokoExtraWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、-1,0と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] tateExtraWall;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://pzprxs.vercel.app/p.html?lither/" + getXLength() + "/" + getYLength() + "/");
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
						Integer numP1 = null;
						if (i + 1 < getYLength() * getXLength()) {
							int yIndexP1 = (i + 1) / getXLength();
							int xIndexP1 = (i + 1) % getXLength();
							numP1 = numbers[yIndexP1][xIndexP1];
						}
						Integer numP2 = null;
						if (numP1 == null && i + 2 < getYLength() * getXLength()) {
							int yIndexP2 = (i + 2) / getXLength();
							int xIndexP2 = (i + 2) % getXLength();
							numP2 = numbers[yIndexP2][xIndexP2];
						}
						if (numP1 == null && numP2 == null) {
							if (num == 0) {
								numStr = "a";
							} else if (num == 1) {
								numStr = "b";
							} else if (num == 2) {
								numStr = "c";
							} else if (num == 3) {
								numStr = "d";
							} else if (num == 4) {
								numStr = "e";
							}
							i++;
							i++;
						} else if (numP1 == null) {
							if (num == 0) {
								numStr = "5";
							} else if (num == 1) {
								numStr = "6";
							} else if (num == 2) {
								numStr = "7";
							} else if (num == 3) {
								numStr = "8";
							} else if (num == 4) {
								numStr = "9";
							}
							i++;
						} else {
							numStr = String.valueOf(num);
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						kuroCnt++;
					}
				}
			}
			return String.valueOf(kuroCnt);
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public Wall[][] getYokoExtraWall() {
			return yokoExtraWall;
		}

		public Wall[][] getTateExtraWall() {
			return tateExtraWall;
		}

		public Field(int height, int width) {
			numbers = new Integer[height][width];
			yokoExtraWall = new Wall[height][width + 1];
			tateExtraWall = new Wall[height + 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			yokoExtraWall = new Wall[height][width + 1];
			tateExtraWall = new Wall[height + 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == '.') {
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							numbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
						}
						index++;
					}
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			yokoExtraWall = new Wall[other.getYLength()][other.getXLength() + 1];
			tateExtraWall = new Wall[other.getYLength() + 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = other.yokoExtraWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = other.tateExtraWall[yIndex][xIndex];
				}
			}
		}

		public Field(Field other, boolean flag) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			yokoExtraWall = new Wall[other.getYLength()][other.getXLength() + 1];
			tateExtraWall = new Wall[other.getYLength() + 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = other.yokoExtraWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = other.tateExtraWall[yIndex][xIndex];
				}
			}
		}

		private static final String FULL_NUMS = "０１２３４";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateExtraWall[yIndex][xIndex]);
					sb.append("□");
				}
				sb.append(System.lineSeparator());
				if (yIndex != getYLength()) {
					for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
						sb.append(yokoExtraWall[yIndex][xIndex]);
						if (xIndex != getXLength()) {
							if (numbers[yIndex][xIndex] != null) {
								sb.append(FULL_NUMS.substring(numbers[yIndex][xIndex], numbers[yIndex][xIndex] + 1));
							} else {
								sb.append("　");
							}
						}
					}
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					sb.append(yokoExtraWall[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateExtraWall[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 数字の周りに壁がいつくあるか調べる。 矛盾したらfalseを返す。
		 */
		public boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int existsCount = 0;
						int notExistsCount = 0;
						Wall wallUp = tateExtraWall[yIndex][xIndex];
						if (wallUp == Wall.EXISTS) {
							existsCount++;
						} else if (wallUp == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						Wall wallRight = yokoExtraWall[yIndex][xIndex + 1];
						if (wallRight == Wall.EXISTS) {
							existsCount++;
						} else if (wallRight == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						Wall wallDown = tateExtraWall[yIndex + 1][xIndex];
						if (wallDown == Wall.EXISTS) {
							existsCount++;
						} else if (wallDown == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						Wall wallLeft = yokoExtraWall[yIndex][xIndex];
						if (wallLeft == Wall.EXISTS) {
							existsCount++;
						} else if (wallLeft == Wall.NOT_EXISTS) {
							notExistsCount++;
						}
						if (existsCount > numbers[yIndex][xIndex] || notExistsCount > 4 - numbers[yIndex][xIndex]) {
							return false;
						} else {
							if (existsCount == numbers[yIndex][xIndex]) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (notExistsCount == 4 - numbers[yIndex][xIndex]) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 柱につながる壁は必ず1、3、4になる。ならない場合falseを返す。
		 */
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					int existsCount = 0;
					int notExistsCount = 0;
					Wall wallUp = yIndex == 0 ? Wall.NOT_EXISTS : yokoExtraWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallDown = yIndex == getYLength() ? Wall.NOT_EXISTS : yokoExtraWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallRight = xIndex == getXLength() ? Wall.NOT_EXISTS : tateExtraWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.NOT_EXISTS : tateExtraWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					if (notExistsCount == 4 || (existsCount == 2 && notExistsCount == 2)) {
						return false;
					}
					if (existsCount == 2 && notExistsCount == 1) {
						if (wallUp == Wall.SPACE) {
							yokoExtraWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					} else if (notExistsCount == 3) {
						if (wallUp == Wall.SPACE) {
							yokoExtraWall[yIndex - 1][xIndex] = Wall.EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex - 1] = Wall.EXISTS;
						}
					} else if (existsCount == 1 && notExistsCount == 2) {
						if (wallUp == Wall.SPACE) {
							yokoExtraWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallDown == Wall.SPACE) {
							yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallRight == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallLeft == Wall.SPACE) {
							tateExtraWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
						}
					}
				}
			}
			return true;
		}

		/**
		 * ループができた場合falseを返す。posは格子点換算。
		 */
		public boolean connectWhiteSolve() {
			Set<Position> resolvedPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (!resolvedPosSet.contains(pos)) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						if (!setContinuePosSet(pos, continuePosSet, null)) {
							return false;
						} else {
							resolvedPosSet.addAll(continuePosSet);
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点につなぐ。元のマスに戻ってきたらfalseを返す。
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			// 上
			if (pos.getyIndex() != 0 && yokoExtraWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.EXISTS
					&& from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (continuePosSet.contains(nextPos)) {
					return false;
				} else {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.DOWN)) {
						return false;
					}
				}
			}
			// 右
			if (pos.getxIndex() != getXLength() && tateExtraWall[pos.getyIndex()][pos.getxIndex()] == Wall.EXISTS
					&& from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				} else {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.LEFT)) {
						return false;
					}
				}
			}
			// 下
			if (pos.getyIndex() != getYLength() && yokoExtraWall[pos.getyIndex()][pos.getxIndex()] == Wall.EXISTS
					&& from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (continuePosSet.contains(nextPos)) {
					return false;
				} else {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.UP)) {
						return false;
					}
				}
			}
			// 左
			if (pos.getxIndex() != 0 && tateExtraWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.EXISTS
					&& from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				} else {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * フィールドに1つは壁が必要。
		 */
		private boolean finalSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] != Wall.NOT_EXISTS) {
						return true;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] != Wall.NOT_EXISTS) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * 各種チェックを1セット実行
		 * 
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectWhiteSolve()) {
					return false;
				}
				if (!finalSolve()) {
					return false;
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public LitherSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public LitherSolver(Field field) {
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
		System.out.println(new LitherSolver(height, width, param).solve());
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
		System.out.println("難易度:" + count * 8);
		System.out.println(field);
		int level = (int) Math.sqrt(count * 8 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 8).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
				if (field.yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
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
		virtual.yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.tateExtraWall = virtual2.tateExtraWall;
			field.yokoExtraWall = virtual2.yokoExtraWall;
		} else if (!allowNotBlack) {
			field.tateExtraWall = virtual.tateExtraWall;
			field.yokoExtraWall = virtual.yokoExtraWall;
		} else {
			// どちらにしても理論
			for (int y = 0; y < field.getYLength(); y++) {
				for (int x = 0; x < field.getXLength() + 1; x++) {
					if (virtual2.yokoExtraWall[y][x] == virtual.yokoExtraWall[y][x]) {
						field.yokoExtraWall[y][x] = virtual.yokoExtraWall[y][x];
					}
				}
			}
			for (int y = 0; y < field.getYLength() + 1; y++) {
				for (int x = 0; x < field.getXLength(); x++) {
					if (virtual2.tateExtraWall[y][x] == virtual.tateExtraWall[y][x]) {
						field.tateExtraWall[y][x] = virtual.tateExtraWall[y][x];
					}
				}
			}
		}
		return true;
	}

	private boolean oneCandTateWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
		boolean allowNotBlack = virtual2.solveAndCheck();
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.tateExtraWall = virtual2.tateExtraWall;
			field.yokoExtraWall = virtual2.yokoExtraWall;
		} else if (!allowNotBlack) {
			field.tateExtraWall = virtual.tateExtraWall;
			field.yokoExtraWall = virtual.yokoExtraWall;
		} else {
			// どちらにしても理論
			for (int y = 0; y < field.getYLength(); y++) {
				for (int x = 0; x < field.getXLength() + 1; x++) {
					if (virtual2.yokoExtraWall[y][x] == virtual.yokoExtraWall[y][x]) {
						field.yokoExtraWall[y][x] = virtual.yokoExtraWall[y][x];
					}
				}
			}
			for (int y = 0; y < field.getYLength() + 1; y++) {
				for (int x = 0; x < field.getXLength(); x++) {
					if (virtual2.tateExtraWall[y][x] == virtual.tateExtraWall[y][x]) {
						field.tateExtraWall[y][x] = virtual.tateExtraWall[y][x];
					}
				}
			}
		}
		return true;
	}
}
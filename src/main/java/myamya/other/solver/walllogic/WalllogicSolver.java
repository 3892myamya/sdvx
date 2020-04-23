package myamya.other.solver.walllogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.HintPattern;
import myamya.other.solver.Solver;

public class WalllogicSolver implements Solver {

	public static class WalllogicGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class WalllogicSolverForGenerator extends WalllogicSolver {
			private final int limit;

			public WalllogicSolverForGenerator(Field field, int limit) {
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
						if (field.getStateDump().equals(befStr)) {
							if (!candSolve(field, 0)) {
								return -1;
							}
							if (field.getStateDump().equals(befStr)) {
								if (!candSolve(field, 1)) {
									return -1;
								}
								if (field.getStateDump().equals(befStr)) {
									if (!candSolve(field, 2)) {
										return -1;
									}
									if (field.getStateDump().equals(befStr)) {
										return -1;
									}
								}
							}
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

		public WalllogicGenerator(int height, int width, HintPattern hintPattern) {
			this.height = height;
			this.width = width;
			this.hintPattern = hintPattern;
		}

		public static void main(String[] args) {
			new WalllogicGenerator(10, 10, HintPattern.getByVal(1, 10, 10)).generate();
		}

		@Override
		public GeneratorResult generate() {
			// 方向決定用のリスト。1↑、2→、3↓、4←
			List<Integer> divisions = new ArrayList<>();
			divisions.add(1);
			divisions.add(2);
			divisions.add(3);
			divisions.add(4);

			Field wkField = new Field(height, width, hintPattern);
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] != null) {
							numberPosList.add(new Position(yIndex, xIndex));
						}
					}
				}
				if (numberPosList.isEmpty()) {
					// 数字マス0だったら作り直し
					wkField = new Field(height, width, hintPattern);
					continue;
				}
				Collections.shuffle(numberPosList);
				// 問題生成部
				// まず、数字と決まったマスからランダムで最低1マスずつ伸ばす
				// 1マスも伸ばせない数字があったら作り直し
				boolean isOk = true;
				for (Position pos : numberPosList) {
					isOk = false;
					Collections.shuffle(divisions);
					for (Integer division : divisions) {
						if (division == 1) {
							Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
							if (nextPos.getyIndex() >= 0 && !numberPosList.contains(nextPos)
									&& wkField.numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() != 1) {
								wkField.numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].clear();
								wkField.numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].add(1);
								wkField.numbers[pos.getyIndex()][pos
										.getxIndex()] = wkField.numbers[pos.getyIndex()][pos.getxIndex()] + 1;
								isOk = true;
								break;
							}
						} else if (division == 2) {
							Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
							if (nextPos.getxIndex() < width && !numberPosList.contains(nextPos)
									&& wkField.numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() != 1) {
								wkField.numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].clear();
								wkField.numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].add(2);
								wkField.numbers[pos.getyIndex()][pos
										.getxIndex()] = wkField.numbers[pos.getyIndex()][pos.getxIndex()] + 1;
								isOk = true;
								break;
							}
						} else if (division == 3) {
							Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
							if (nextPos.getyIndex() < height && !numberPosList.contains(nextPos)
									&& wkField.numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() != 1) {
								wkField.numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].clear();
								wkField.numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].add(3);
								wkField.numbers[pos.getyIndex()][pos
										.getxIndex()] = wkField.numbers[pos.getyIndex()][pos.getxIndex()] + 1;
								isOk = true;
								break;
							}
						} else if (division == 4) {
							Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
							if (nextPos.getxIndex() > 0 && !numberPosList.contains(nextPos)
									&& wkField.numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() != 1) {
								wkField.numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].clear();
								wkField.numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].add(4);
								wkField.numbers[pos.getyIndex()][pos
										.getxIndex()] = wkField.numbers[pos.getyIndex()][pos.getxIndex()] + 1;
								isOk = true;
								break;
							}
						}
					}
					if (!isOk) {
						break;
					}
				}
				if (!isOk) {
					wkField = new Field(height, width, hintPattern);
					continue;
				}
				// 次に、ランダムで数字マスから伸ばせるだけ伸ばす。
				while (!numberPosList.isEmpty()) {
					isOk = false;
					int index = (int) (Math.random() * numberPosList.size());
					Position pos = numberPosList.get(index);
					Collections.shuffle(divisions);
					for (Integer division : divisions) {
						if (division == 1) {
							for (int targetY = pos.getyIndex() - 1; targetY >= 0; targetY--) {
								if (wkField.numbersCand[targetY][pos.getxIndex()].size() != 1) {
									wkField.numbersCand[targetY][pos.getxIndex()].clear();
									wkField.numbersCand[targetY][pos.getxIndex()].add(1);
									wkField.numbers[pos.getyIndex()][pos
											.getxIndex()] = wkField.numbers[pos.getyIndex()][pos.getxIndex()] + 1;
									isOk = true;
									break;
								} else if (!wkField.numbersCand[targetY][pos.getxIndex()].contains(1)) {
									break;
								}
							}
						} else if (division == 2) {
							for (int targetX = pos.getxIndex() + 1; targetX < width; targetX++) {
								if (wkField.numbersCand[pos.getyIndex()][targetX].size() != 1) {
									wkField.numbersCand[pos.getyIndex()][targetX].clear();
									wkField.numbersCand[pos.getyIndex()][targetX].add(2);
									wkField.numbers[pos.getyIndex()][pos
											.getxIndex()] = wkField.numbers[pos.getyIndex()][pos.getxIndex()] + 1;
									isOk = true;
									break;
								} else if (!wkField.numbersCand[pos.getyIndex()][targetX].contains(2)) {
									break;
								}
							}
						} else if (division == 3) {
							for (int targetY = pos.getyIndex() + 1; targetY < height; targetY++) {
								if (wkField.numbersCand[targetY][pos.getxIndex()].size() != 1) {
									wkField.numbersCand[targetY][pos.getxIndex()].clear();
									wkField.numbersCand[targetY][pos.getxIndex()].add(3);
									wkField.numbers[pos.getyIndex()][pos
											.getxIndex()] = wkField.numbers[pos.getyIndex()][pos.getxIndex()] + 1;
									isOk = true;
									break;
								} else if (!wkField.numbersCand[targetY][pos.getxIndex()].contains(3)) {
									break;
								}
							}
						} else if (division == 4) {
							for (int targetX = pos.getxIndex() - 1; targetX >= 0; targetX--) {
								if (wkField.numbersCand[pos.getyIndex()][targetX].size() != 1) {
									wkField.numbersCand[pos.getyIndex()][targetX].clear();
									wkField.numbersCand[pos.getyIndex()][targetX].add(4);
									wkField.numbers[pos.getyIndex()][pos
											.getxIndex()] = wkField.numbers[pos.getyIndex()][pos.getxIndex()] + 1;
									isOk = true;
									break;
								} else if (!wkField.numbersCand[pos.getyIndex()][targetX].contains(4)) {
									break;
								}
							}
						}
						if (isOk) {
							break;
						}
					}
					if (!isOk) {
						numberPosList.remove(index);
					}
				}
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] == null) {
							wkField.numbersCand[yIndex][xIndex] = new ArrayList<>();
							for (int number = 0; number <= 4; number++) {
								wkField.numbersCand[yIndex][xIndex].add(number);
							}
						}
					}
				}
				// 解けるかな？
				level = new WalllogicSolverForGenerator(new Field(wkField), 1000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new Field(height, width, hintPattern);
					continue;
				} else {
					break;
				}
			}
			level = (int) Math.sqrt(level * 20 / 3) + 1;

			String status = "Lv:" + level + "の問題を獲得！(数字:" + wkField.getHintCount() + ")";
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
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の候補情報
		// ここでは、0何も置かない、1↑、2→、3↓、4←として扱う。
		protected List<Integer>[][] numbersCand;
		// 固定数字の情報
		private final Integer[][] numbers;
		// 黒マスの位置
		private final Set<Position> blackPosSet;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?walllogic/" + getXLength() + "/" + getYLength() + "/");
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
					String numStr = Integer.toHexString(num);
					if (numStr.length() == 2) {
						numStr = "-" + numStr;
					} else if (numStr.length() == 3) {
						numStr = "+" + numStr;
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

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int i = 0; i <= 4; i++) {
						numbersCand[yIndex][xIndex].add(i);
					}
				}
			}
			blackPosSet = new HashSet<>();
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
						numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else if (ch == '+') {
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
						blackPosSet.add(pos);
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
		}

		/**
		 * プレーン盤面生成。
		 */
		@SuppressWarnings("unchecked")
		public Field(int height, int width, HintPattern hintPattern) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int number = 0; number <= 4; number++) {
						numbersCand[yIndex][xIndex].add(number);
					}
				}
			}
			blackPosSet = new HashSet<>();
			List<Set<Position>> posSetList = hintPattern.getPosSetList();
			for (Set<Position> posSet : posSetList) {
				if (Math.random() * 4 < 1) {
					for (Position pos : posSet) {
						numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
						numbers[pos.getyIndex()][pos.getxIndex()] = 0;
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			blackPosSet = other.blackPosSet;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (blackPosSet.contains(new Position(yIndex, xIndex))) {
						sb.append("■");
					} else if (numbers[yIndex][xIndex] != null) {
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
					} else if (numbersCand[yIndex][xIndex].isEmpty()) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						int num = numbersCand[yIndex][xIndex].get(0);
						sb.append(num == 1 ? "↑" : num == 2 ? "→" : num == 3 ? "↓" : num == 4 ? "←" : "・");
					} else {
						sb.append("　");
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
					sb.append(numbersCand[yIndex][xIndex].size());
				}
			}
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
				return false;
			}
			if (!aloneSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 伸ばしても数字につながらない方向を消す。
		 */
		private boolean aloneSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						boolean notUp = true;
						boolean notRight = true;
						boolean notDown = true;
						boolean notLeft = true;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (numbers[targetY][xIndex] != null) {
								notDown = false;
								break;
							}
							if (!numbersCand[targetY][xIndex].contains(3)) {
								break;
							}
						}
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (numbers[yIndex][targetX] != null) {
								notLeft = false;
								break;
							}
							if (!numbersCand[yIndex][targetX].contains(4)) {
								break;
							}
						}
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (numbers[targetY][xIndex] != null) {
								notUp = false;
								break;
							}
							if (!numbersCand[targetY][xIndex].contains(1)) {
								break;
							}
						}
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (numbers[yIndex][targetX] != null) {
								notRight = false;
								break;
							}
							if (!numbersCand[yIndex][targetX].contains(2)) {
								break;
							}
						}
						if (notUp) {
							numbersCand[yIndex][xIndex].remove(new Integer(1));
						}
						if (notRight) {
							numbersCand[yIndex][xIndex].remove(new Integer(2));
						}
						if (notDown) {
							numbersCand[yIndex][xIndex].remove(new Integer(3));
						}
						if (notLeft) {
							numbersCand[yIndex][xIndex].remove(new Integer(4));
						}
						if (numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字は縦につながるマスと横につながるマスの合計になる。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						int upSpaceCnt = 0;
						int rightSpaceCnt = 0;
						int downSpaceCnt = 0;
						int leftSpaceCnt = 0;

						int upWhiteCnt = 0;
						int rightWhiteCnt = 0;
						int downWhiteCnt = 0;
						int leftWhiteCnt = 0;

						boolean upCounting = true;
						boolean rightCounting = true;
						boolean downCounting = true;
						boolean leftCounting = true;

						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (!numbersCand[targetY][xIndex].contains(1)) {
								break;
							}
							if (numbersCand[targetY][xIndex].size() != 1) {
								upCounting = false;
							}
							if (upCounting) {
								upWhiteCnt++;
							}
							upSpaceCnt++;
						}
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (!numbersCand[yIndex][targetX].contains(2)) {
								break;
							}
							if (numbersCand[yIndex][targetX].size() != 1) {
								rightCounting = false;
							}
							if (rightCounting) {
								rightWhiteCnt++;
							}
							rightSpaceCnt++;
						}
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (!numbersCand[targetY][xIndex].contains(3)) {
								break;
							}
							if (numbersCand[targetY][xIndex].size() != 1) {
								downCounting = false;
							}
							if (downCounting) {
								downWhiteCnt++;
							}
							downSpaceCnt++;
						}
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (!numbersCand[yIndex][targetX].contains(4)) {
								break;
							}
							if (numbersCand[yIndex][targetX].size() != 1) {
								leftCounting = false;
							}
							if (leftCounting) {
								leftWhiteCnt++;
							}
							leftSpaceCnt++;
						}
						int aroundSpaceCount = upSpaceCnt + downSpaceCnt + rightSpaceCnt + leftSpaceCnt;
						int aroundWhiteCnt = upWhiteCnt + downWhiteCnt + rightWhiteCnt + leftWhiteCnt;
						if (aroundSpaceCount < numbers[yIndex][xIndex]) {
							// 足りない
							return false;
						}
						if (aroundWhiteCnt > numbers[yIndex][xIndex]) {
							// 伸ばしすぎ
							return false;
						}
						// 確実に伸ばす数
						int fixedWhiteUp = numbers[yIndex][xIndex]
								- (rightSpaceCnt + downSpaceCnt + leftSpaceCnt);
						int fixedWhiteRight = numbers[yIndex][xIndex]
								- (upSpaceCnt + downSpaceCnt + leftSpaceCnt);
						int fixedWhiteDown = numbers[yIndex][xIndex]
								- (upSpaceCnt + rightSpaceCnt + leftSpaceCnt);
						int fixedWhiteLeft = numbers[yIndex][xIndex]
								- (upSpaceCnt + rightSpaceCnt + downSpaceCnt);
						if (fixedWhiteUp > 0) {
							for (int i = 1; i <= fixedWhiteUp; i++) {
								numbersCand[yIndex - i][xIndex].remove(new Integer(0));
								numbersCand[yIndex - i][xIndex].remove(new Integer(2));
								numbersCand[yIndex - i][xIndex].remove(new Integer(3));
								numbersCand[yIndex - i][xIndex].remove(new Integer(4));
								if (numbersCand[yIndex - i][xIndex].isEmpty()) {
									return false;
								}
							}
						}
						if (fixedWhiteRight > 0) {
							for (int i = 1; i <= fixedWhiteRight; i++) {
								numbersCand[yIndex][xIndex + i].remove(new Integer(0));
								numbersCand[yIndex][xIndex + i].remove(new Integer(1));
								numbersCand[yIndex][xIndex + i].remove(new Integer(3));
								numbersCand[yIndex][xIndex + i].remove(new Integer(4));
								if (numbersCand[yIndex][xIndex + i].isEmpty()) {
									return false;
								}
							}
						}
						if (fixedWhiteDown > 0) {
							for (int i = 1; i <= fixedWhiteDown; i++) {
								numbersCand[yIndex + i][xIndex].remove(new Integer(0));
								numbersCand[yIndex + i][xIndex].remove(new Integer(1));
								numbersCand[yIndex + i][xIndex].remove(new Integer(2));
								numbersCand[yIndex + i][xIndex].remove(new Integer(4));
								if (numbersCand[yIndex + i][xIndex].isEmpty()) {
									return false;
								}

							}
						}
						if (fixedWhiteLeft > 0) {
							for (int i = 1; i <= fixedWhiteLeft; i++) {
								numbersCand[yIndex][xIndex - i].remove(new Integer(0));
								numbersCand[yIndex][xIndex - i].remove(new Integer(1));
								numbersCand[yIndex][xIndex - i].remove(new Integer(2));
								numbersCand[yIndex][xIndex - i].remove(new Integer(3));
								if (numbersCand[yIndex][xIndex - i].isEmpty()) {
									return false;
								}
							}
						}
						if (aroundWhiteCnt == numbers[yIndex][xIndex]) {
							if (yIndex - upWhiteCnt - 1 >= 0) {
								numbersCand[yIndex - upWhiteCnt - 1][xIndex].remove(new Integer(1));
								if (numbersCand[yIndex - upWhiteCnt - 1][xIndex].isEmpty()) {
									return false;
								}
							}
							if (xIndex + rightWhiteCnt + 1 < getXLength()) {
								numbersCand[yIndex][xIndex + rightWhiteCnt + 1].remove(new Integer(2));
								if (numbersCand[yIndex][xIndex + rightWhiteCnt + 1].isEmpty()) {
									return false;
								}
							}
							if (yIndex + downWhiteCnt + 1 < getYLength()) {
								numbersCand[yIndex + downWhiteCnt + 1][xIndex].remove(new Integer(3));
								if (numbersCand[yIndex + downWhiteCnt + 1][xIndex].isEmpty()) {
									return false;
								}
							}
							if (xIndex - leftWhiteCnt - 1 >= 0) {
								numbersCand[yIndex][xIndex - leftWhiteCnt - 1].remove(new Integer(4));
								if (numbersCand[yIndex][xIndex - leftWhiteCnt - 1].isEmpty()) {
									return false;
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
					if (numbersCand[yIndex][xIndex].size() != 1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public WalllogicSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public WalllogicSolver(Field field) {
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
		System.out.println(new WalllogicSolver(height, width, param).solve());
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
			if (field.getStateDump().equals(befStr)) {
				if (!candSolve(field, 0)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				if (field.getStateDump().equals(befStr)) {
					if (!candSolve(field, 1)) {
						System.out.println(field);
						return "問題に矛盾がある可能性があります。途中経過を返します。";
					}
					if (field.getStateDump().equals(befStr)) {
						if (!candSolve(field, 2)) {
							System.out.println(field);
							return "問題に矛盾がある可能性があります。途中経過を返します。";
						}
						if (field.getStateDump().equals(befStr)) {
							System.out.println(field);
							return "解けませんでした。途中経過を返します。";
						}
					}
				}
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count * 20));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 20).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.numbersCand[yIndex][xIndex].size() != 1) {
					count++;
					for (Iterator<Integer> iterator = field.numbersCand[yIndex][xIndex].iterator(); iterator
							.hasNext();) {
						int oneCand = iterator.next();
						Field virtual = new Field(field);
						virtual.numbersCand[yIndex][xIndex].clear();
						virtual.numbersCand[yIndex][xIndex].add(oneCand);
						boolean arrowCand = virtual.solveAndCheck();
						if (arrowCand && recursive > 0) {
							arrowCand = candSolve(virtual, recursive - 1);
						}
						if (!arrowCand) {
							iterator.remove();
						}
					}
					if (field.numbersCand[yIndex][xIndex].size() == 0) {
						return false;
					}
				}
				if (field.numbersCand[yIndex][xIndex].size() == 1) {
					if (!field.solveAndCheck()) {
						return false;
					}
				}
			}
		}
		return true;
	}
}
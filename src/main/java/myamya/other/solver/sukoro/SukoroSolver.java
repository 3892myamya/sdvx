package myamya.other.solver.sukoro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.HintPattern;
import myamya.other.solver.Solver;

public class SukoroSolver implements Solver {
	public static class SukoroGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class SukoroSolverForGenerator extends SukoroSolver {
			private final int limit;

			public SukoroSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
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
		private final HintPattern hintPattern;

		public SukoroGenerator(int height, int width, HintPattern hintPattern) {
			this.height = height;
			this.width = width;
			this.hintPattern = hintPattern;
		}

		public static void main(String[] args) {
			new SukoroGenerator(7, 7, HintPattern.getByVal(1, 7, 7)).generate();
		}

		@Override
		public GeneratorResult generate() {
			Field wkField = new Field(height, width);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < (height * width); i++) {
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
					int yIndex = posBase / (width);
					int xIndex = posBase % (width);
					if (wkField.numbersCand[yIndex][xIndex].contains(0)
							&& wkField.numbersCand[yIndex][xIndex].size() != 1) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							Field virtual = new Field(wkField);
							if (masuNum < 1) {
								virtual.numbersCand[yIndex][xIndex].remove(new Integer(0));
							} else if (masuNum < 2) {
								virtual.numbersCand[yIndex][xIndex].remove(new Integer(1));
								virtual.numbersCand[yIndex][xIndex].remove(new Integer(2));
								virtual.numbersCand[yIndex][xIndex].remove(new Integer(3));
								virtual.numbersCand[yIndex][xIndex].remove(new Integer(4));
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.numbersCand = virtual.numbersCand;
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
				// マスを戻す
				List<Set<Position>> numberPosSetList = hintPattern.getPosSetList();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbersCand[yIndex][xIndex].get(0) != 0) {
							wkField.numbers[yIndex][xIndex] = wkField.numbersCand[yIndex][xIndex].get(0);
						} else {
							wkField.numbers[yIndex][xIndex] = null;
							wkField.numbersCand[yIndex][xIndex] = new ArrayList<>();
							for (int number = 0; number <= 4; number++) {
								wkField.numbersCand[yIndex][xIndex].add(number);
							}
							for (Iterator<Set<Position>> iterator = numberPosSetList.iterator(); iterator.hasNext();) {
								Set<Position> posSet = iterator.next();
								if (posSet.contains(new Position(yIndex, xIndex))) {
									for (Position pos : posSet) {
										wkField.numbers[pos.getyIndex()][pos.getxIndex()] = null;
										wkField.numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
										for (int number = 0; number <= 4; number++) {
											wkField.numbersCand[pos.getyIndex()][pos.getxIndex()].add(number);
										}
									}
									iterator.remove();
								}
							}
						}
					}
				}
				// 解けるかな？
				level = new SukoroSolverForGenerator(new Field(wkField), 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					boolean isOk = false;
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosSetList);
					for (Set<Position> numberPosSet : numberPosSetList) {
						Field virtual = new Field(wkField, true);
						for (Position pos : numberPosSet) {
							int yIndex = pos.getyIndex();
							int xIndex = pos.getxIndex();
							virtual.numbers[yIndex][xIndex] = null;
							virtual.numbersCand[yIndex][xIndex] = new ArrayList<>();
							for (int number = 0; number <= 4; number++) {
								virtual.numbersCand[yIndex][xIndex].add(number);
							}
						}
						int solveResult = new SukoroSolverForGenerator(virtual, 5000).solve2();
						if (solveResult != -1) {
							isOk = true;
							for (Position pos : numberPosSet) {
								int yIndex = pos.getyIndex();
								int xIndex = pos.getxIndex();
								wkField.numbers[yIndex][xIndex] = null;
								wkField.numbersCand[yIndex][xIndex] = new ArrayList<>();
								for (int number = 0; number <= 4; number++) {
									wkField.numbersCand[yIndex][xIndex].add(number);
								}
							}
							level = solveResult;
						}
					}
					if (isOk) {
						break;
					} else {
						// 一つも数字が削れなければやり直し
						wkField = new Field(height, width);
						Collections.shuffle(indexList);
						index = 0;
					}
				}
			}
			level = (int) Math.sqrt(level * 10 / 3) + 1;
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
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbersCand()[yIndex][xIndex].size() == 1
							&& wkField.getNumbersCand()[yIndex][xIndex].get(0) != 0) {
						String numberStr = String.valueOf(wkField.getNumbersCand()[yIndex][xIndex].get(0));
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

		// 表出数字
		private final Integer[][] numbers;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public String getHintCount() {
			int cnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						cnt++;
					}
				}
			}
			return String.valueOf(cnt);
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?sukoro/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (numbers[yIndex][xIndex] == null) {
					interval++;
					if (interval == 26) {
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
			// 0(空白)から4まで候補を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int number = 0; number <= 4; number++) {
						numbersCand[yIndex][xIndex].add(number);
					}
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int num;
					if (ch == '.') {
						//
					} else {
						if (ch == '-') {
							num = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							num = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							num = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(num);
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
					}
					index++;
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
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width) {
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
		}

		@SuppressWarnings("unchecked")
		public Field(Field other, boolean flag) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (other.numbers[yIndex][xIndex] != null) {
						numbers[yIndex][xIndex] = new Integer(other.numbers[yIndex][xIndex]);
					}
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
						int index = HALF_NUMS.indexOf(numStr);
						if (index == 0) {
							sb.append("・");
						} else {
							sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
						}
					} else if (numbersCand[yIndex][xIndex].size() == 2) {
						sb.append(numbersCand[yIndex][xIndex].get(0));
						sb.append(numbersCand[yIndex][xIndex].get(1));
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
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 数字が0以外の場合、自分の周りにある0以外の数は数字と同じになる。
		 * また、同じ数字は隣り合わない。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int numberCount = 0;
					int zeroCount = 0;
					List<Integer> upCands = yIndex == 0 ? null : numbersCand[yIndex - 1][xIndex];
					List<Integer> rightCands = xIndex == getXLength() - 1 ? null
							: numbersCand[yIndex][xIndex + 1];
					List<Integer> downCands = yIndex == getYLength() - 1 ? null
							: numbersCand[yIndex + 1][xIndex];
					List<Integer> leftCands = xIndex == 0 ? null : numbersCand[yIndex][xIndex - 1];
					if (upCands != null && !upCands.contains(0)) {
						numberCount++;
					} else if (upCands == null || (upCands.size() == 1 && upCands.get(0) == 0)) {
						zeroCount++;
					}
					if (rightCands != null && !rightCands.contains(0)) {
						numberCount++;
					} else if (rightCands == null || (rightCands.size() == 1 && rightCands.get(0) == 0)) {
						zeroCount++;
					}
					if (downCands != null && !downCands.contains(0)) {
						numberCount++;
					} else if (downCands == null || (downCands.size() == 1 && downCands.get(0) == 0)) {
						zeroCount++;
					}
					if (leftCands != null && !leftCands.contains(0)) {
						numberCount++;
					} else if (leftCands == null || (leftCands.size() == 1 && leftCands.get(0) == 0)) {
						zeroCount++;
					}
					for (Iterator<Integer> iterator = numbersCand[yIndex][xIndex].iterator(); iterator.hasNext();) {
						int number = iterator.next();
						if (number != 0 && (number < numberCount || number > 4 - zeroCount)) {
							// 0以外で、自分の数字が大きすぎる、または小さすぎる候補を消す。
							iterator.remove();
						}
					}
					if (numbersCand[yIndex][xIndex].isEmpty()) {
						return false;
					}
					if (numbersCand[yIndex][xIndex].size() == 1) {
						Integer number = numbersCand[yIndex][xIndex].get(0);
						if (number == 0) {
							continue;
						}
						// 同じ数字は隣り合わない
						if (upCands != null) {
							upCands.remove(number);
							if (upCands.isEmpty()) {
								return false;
							}
						}
						if (rightCands != null) {
							rightCands.remove(number);
							if (rightCands.isEmpty()) {
								return false;
							}
						}
						if (downCands != null) {
							downCands.remove(number);
							if (downCands.isEmpty()) {
								return false;
							}
						}
						if (leftCands != null) {
							leftCands.remove(number);
							if (leftCands.isEmpty()) {
								return false;
							}
						}
						// 自分が0以外の数字に確定している場合、周囲の候補を狭める
						if (number == numberCount) {
							// 数字の数が等しいことが分かれば、それ以外のマスは0確定
							if (upCands != null && upCands.size() > 1 && upCands.contains(0)) {
								upCands.clear();
								upCands.add(0);
							}
							if (rightCands != null && rightCands.size() > 1 && rightCands.contains(0)) {
								rightCands.clear();
								rightCands.add(0);
							}
							if (downCands != null && downCands.size() > 1 && downCands.contains(0)) {
								downCands.clear();
								downCands.add(0);
							}
							if (leftCands != null && leftCands.size() > 1 && leftCands.contains(0)) {
								leftCands.clear();
								leftCands.add(0);
							}
						}
						if (number == 4 - zeroCount) {
							// 0の数が等しいことが分かれば、それ以外のマスは0以外確定
							if (upCands != null && upCands.size() > 1 && upCands.contains(0)) {
								upCands.remove(new Integer(0));
							}
							if (rightCands != null && rightCands.size() > 1 && rightCands.contains(0)) {
								rightCands.remove(new Integer(0));
							}
							if (downCands != null && downCands.size() > 1 && downCands.contains(0)) {
								downCands.remove(new Integer(0));
							}
							if (leftCands != null && leftCands.size() > 1 && leftCands.contains(0)) {
								leftCands.remove(new Integer(0));
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> numberPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1 && numbersCand[yIndex][xIndex].get(0) != 0) {
						Position numberPos = new Position(yIndex, xIndex);
						if (numberPosSet.size() == 0) {
							numberPosSet.add(numberPos);
							setContinuePosSet(numberPos, numberPosSet, null);
						} else {
							if (!numberPosSet.contains(numberPos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に0確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& !(numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1 &&
								numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].get(0) == 0)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& !(numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1 &&
								numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].get(0) == 0)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& !(numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1 &&
								numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].get(0) == 0)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& !(numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1 &&
								numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].get(0) == 0)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
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
	protected int count = 0;

	public SukoroSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public SukoroSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "";//urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new SukoroSolver(height, width, param).solve());
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
			}
		}
		return true;
	}
}
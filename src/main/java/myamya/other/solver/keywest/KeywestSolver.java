package myamya.other.solver.keywest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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

public class KeywestSolver implements Solver {

	public static class KeywestGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class KeywestSolverForGenerator extends KeywestSolver {
			private final int limit;

			public KeywestSolverForGenerator(Field field, int limit) {
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

		public KeywestGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new KeywestGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			KeywestSolver.Field wkField = new KeywestSolver.Field(height, width);
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
					int posBase = indexList.get(index);
					int yIndex = posBase / (width);
					int xIndex = posBase % (width);
					if (wkField.numbersCand[yIndex][xIndex].contains(0)
							&& wkField.numbersCand[yIndex][xIndex].size() != 1) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 5; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							Field virtual = new Field(wkField);
							virtual.numbersCand[yIndex][xIndex].clear();
							virtual.numbersCand[yIndex][xIndex].add(new Integer(masuNum));
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.numbersCand[yIndex][xIndex].clear();
								wkField.numbersCand[yIndex][xIndex].add(new Integer(masuNum));
								wkField.tateWall = virtual.tateWall;
								wkField.yokoWall = virtual.yokoWall;
								break;
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
					if (index >= height * width) {
						// 数字が埋まったが、壁の埋まり方が一通りに決まらない場合があり、その場合は作り直し
						wkField = new Field(height, width);
						Collections.shuffle(indexList);
						index = 0;
						continue;
					}
				}
				// System.out.println(wkField);
				// 横壁
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				// 縦壁
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				// 解けるかな？
				level = new KeywestSolverForGenerator(new KeywestSolver.Field(wkField), 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new KeywestSolver.Field(height, width);
					index = 0;
				} else {
					boolean isOk = false;
					// ヒントを限界まで減らす
					List<Position> numberPosList = new ArrayList<>();
					for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
						for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
							numberPosList.add(new Position(yIndex, xIndex));
						}
					}
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						KeywestSolver.Field virtual = new KeywestSolver.Field(wkField);
						int yIndex = numberPos.getyIndex();
						int xIndex = numberPos.getxIndex();
						virtual.numbers[yIndex][xIndex] = null;
						virtual.numbersCand[yIndex][xIndex] = new ArrayList<>();
						for (int number = 0; number <= 4; number++) {
							virtual.numbersCand[yIndex][xIndex].add(number);
						}
						int solveResult = new KeywestSolverForGenerator(virtual, 2500).solve2();
						if (solveResult != -1) {
							isOk = true;
							wkField.numbers[yIndex][xIndex] = null;
							wkField.numbersCand[yIndex][xIndex] = new ArrayList<>();
							for (int number = 0; number <= 4; number++) {
								wkField.numbersCand[yIndex][xIndex].add(number);
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
			// 消さなかった数字を表出数字化
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.numbersCand[yIndex][xIndex].size() == 1) {
						wkField.numbers[yIndex][xIndex] = wkField.numbersCand[yIndex][xIndex].get(0);
					}
				}
			}
			level = (int) Math.sqrt(level * 15 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">pzprxsで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
							+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + (baseSize / 2 - 2)
							+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
					if (wkField.getNumbersCand()[yIndex][xIndex].size() == 1) {
						String numberStr = String.valueOf(wkField.getNumbersCand()[yIndex][xIndex].get(0));
						String masuStr;
						int idx = HALF_NUMS.indexOf(numberStr);
						if (idx >= 0) {
							masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 5) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
					}
				}
			}
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			System.out.println(url);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}
	}

	public static class Field {
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		// 表出数字
		private final Integer[][] numbers;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected Wall[][] tateWall;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzprxs.vercel.app/p?keywest/" + getXLength() + "/" + getYLength() + "/");
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

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width) {
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
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

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
			// 0から4まで候補を決定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>();
					for (int number = 0; number <= 4; number++) {
						numbersCand[yIndex][xIndex].add(number);
					}
				}
			}
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
				Position pos = new Position(index / getXLength(), index % getXLength());
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
						numbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
						numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(numbers[pos.getyIndex()][pos.getxIndex()]);
						index++;
						index++;
					} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
						numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
						numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(numbers[pos.getyIndex()][pos.getxIndex()]);
						index++;
					} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
						numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
						numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(numbers[pos.getyIndex()][pos.getxIndex()]);
					}
					index++;
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
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
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
						int index = HALF_NUMS.indexOf(numStr);
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					} else if (numbersCand[yIndex][xIndex].size() == 2) {
						sb.append(numbersCand[yIndex][xIndex].get(0));
						sb.append(numbersCand[yIndex][xIndex].get(1));
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
		 * 0のマス以外が1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			Set<Position> blackCandPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (!numbersCand[yIndex][xIndex].contains(0)) {
						if (whitePosSet.size() == 0) {
							whitePosSet.add(pos);
							setContinuePosSet(pos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(pos)) {
								return false;
							}
						}
					} else if (numbersCand[yIndex][xIndex].contains(0)) {
						blackCandPosSet.add(pos);
					}
				}
			}
			if (whitePosSet.size() != 0) {
				blackCandPosSet.removeAll(whitePosSet);
				for (Position pos : blackCandPosSet) {
					numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
					numbersCand[pos.getyIndex()][pos.getxIndex()].add(0);
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
		 */
		protected boolean solveAndCheck() {
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
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 自分の周りにある壁なし辺は数字と同じになる。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int nonWallCount = 0;
					int maxNonWallCount = 0;
					Wall upWall = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					Wall rightWall = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					Wall downWall = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					Wall leftWall = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (upWall == Wall.NOT_EXISTS) {
						nonWallCount++;
					}
					if (upWall != Wall.EXISTS) {
						maxNonWallCount++;
					}
					if (rightWall == Wall.NOT_EXISTS) {
						nonWallCount++;
					}
					if (rightWall != Wall.EXISTS) {
						maxNonWallCount++;
					}
					if (downWall == Wall.NOT_EXISTS) {
						nonWallCount++;
					}
					if (downWall != Wall.EXISTS) {
						maxNonWallCount++;
					}
					if (leftWall == Wall.NOT_EXISTS) {
						nonWallCount++;
					}
					if (leftWall != Wall.EXISTS) {
						maxNonWallCount++;
					}
					for (Iterator<Integer> iterator = numbersCand[yIndex][xIndex].iterator(); iterator.hasNext();) {
						int number = iterator.next();
						if (number < nonWallCount || number > maxNonWallCount) {
							// 0以外で、自分の数字が大きすぎる、または小さすぎる候補を消す。
							iterator.remove();
						}
					}
					if (numbersCand[yIndex][xIndex].isEmpty()) {
						return false;
					}
					// 数字の候補が決まったら、壁を確定する
					if (numbersCand[yIndex][xIndex].size() == 1) {
						Integer number = numbersCand[yIndex][xIndex].get(0);
						if (nonWallCount == number) {
							if (upWall == Wall.SPACE) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (rightWall == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (downWall == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (leftWall == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
						}
						if (maxNonWallCount == number) {
							if (upWall == Wall.SPACE) {
								tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (rightWall == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (downWall == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (leftWall == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 同じ数字は隣り合わない。
		 */
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1) {
						Integer number = numbersCand[yIndex][xIndex].get(0);
						List<Integer> upCands = yIndex == 0 ? null : numbersCand[yIndex - 1][xIndex];
						List<Integer> rightCands = xIndex == getXLength() - 1 ? null : numbersCand[yIndex][xIndex + 1];
						List<Integer> downCands = yIndex == getYLength() - 1 ? null : numbersCand[yIndex + 1][xIndex];
						List<Integer> leftCands = xIndex == 0 ? null : numbersCand[yIndex][xIndex - 1];
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

	public KeywestSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public KeywestSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://pzprxs.vercel.app/p?keywest/6/6/i85aidg52ag1842aj"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new KeywestSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 15));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 15 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 15).toString() + "(Lv:" + level + ")";
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
			field.numbersCand = virtual2.numbersCand;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.numbersCand = virtual.numbersCand;
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
			field.numbersCand = virtual2.numbersCand;
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.numbersCand = virtual.numbersCand;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}
}
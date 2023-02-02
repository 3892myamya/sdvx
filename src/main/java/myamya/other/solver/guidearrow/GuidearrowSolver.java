package myamya.other.solver.guidearrow;

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
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class GuidearrowSolver implements Solver {
	public static class GuidearrowGenerator implements Generator {

		static class GuidearrowSolverForGenerator extends GuidearrowSolver {
			private final int limit;

			public GuidearrowSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			public int solve2() {
				try {
					while (!field.isSolved()) {
						String befStr = field.getStateDump();
						if (!field.solveAndCheck()) {
							return -2;
						}
						if (field.getStateDump().equals(befStr)) {
							if (!candSolve(field, 0)) {
								return -2;
							}
							if (field.getStateDump().equals(befStr)) {
								if (!candSolve(field, 1)) {
									return -2;
								}
								if (field.getStateDump().equals(befStr)) {
									if (!candSolve(field, 2)) {
										return -2;
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

		public GuidearrowGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new GuidearrowGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			GuidearrowSolver.Field wkField = new GuidearrowSolver.Field(height, width);
			List<Position> indexList = new ArrayList<>();
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					indexList.add(new Position(yIndex, xIndex));
				}
			}
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				// 記号を配置
				Collections.shuffle(indexList);
				boolean isOk = false;
				for (Position pos : indexList) {
					isOk = false;
					List<Integer> numIdxList = new ArrayList<>();
					for (int number : wkField.numbersCand[pos.getyIndex()][pos.getxIndex()]) {
						numIdxList.add(number);
					}
					Collections.shuffle(numIdxList);
					for (int masuNum : numIdxList) {
						Field virtual = new Field(wkField);
						virtual.numbersCand[pos.getyIndex()][pos.getxIndex()].clear();
						virtual.numbersCand[pos.getyIndex()][pos.getxIndex()].add(masuNum);
						if (-2 != new GuidearrowSolverForGenerator(virtual, 5).solve2()) {
							isOk = true;
							wkField.numbersCand[pos.getyIndex()][pos
									.getxIndex()] = virtual.numbersCand[pos.getyIndex()][pos.getxIndex()];
							break;
						}
					}
					if (!isOk) {
						break;
					}
				}
				if (!isOk) {
					// 破綻したら0から作り直す。
					wkField = new Field(height, width);
					continue;
				}
				// マスを戻す
				List<Position> fixedMasuList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbersCand[yIndex][xIndex].get(0) != 0) {
							fixedMasuList.add(new Position(yIndex, xIndex));
							wkField.numbers[yIndex][xIndex] = wkField.numbersCand[yIndex][xIndex].get(0);
						} else {
							wkField.numbers[yIndex][xIndex] = null;
							wkField.numbersCand[yIndex][xIndex] = new ArrayList<>();
							for (int number = 0; number <= 4; number++) {
								wkField.numbersCand[yIndex][xIndex].add(number);
							}
						}
					}
				}
				// 解けるかな？
				level = new GuidearrowSolverForGenerator(wkField, 100).solve2();
				if (level < 0) {
					// 解けなければやり直し
					wkField = new GuidearrowSolver.Field(height, width);
				} else {
					Collections.shuffle(fixedMasuList);
					boolean isDeleted = false;
					for (Position pos : fixedMasuList) {
						GuidearrowSolver.Field virtual = new GuidearrowSolver.Field(wkField);
						virtual.numbers[pos.getyIndex()][pos.getxIndex()] = null;
						virtual.numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						for (int number = 0; number <= 4; number++) {
							virtual.numbersCand[pos.getyIndex()][pos.getxIndex()].add(number);
						}
						int solveResult = new GuidearrowSolverForGenerator(virtual, 1000).solve2();
						if (solveResult >= 0) {
							isDeleted = true;
							wkField.numbers[pos.getyIndex()][pos.getxIndex()] = null;
							wkField.numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
							for (int number = 0; number <= 4; number++) {
								wkField.numbersCand[pos.getyIndex()][pos.getxIndex()].add(number);
							}
							level = solveResult;
						}
					}
					if (!isDeleted) {
						// 1マスも消せないはアウト
						wkField = new GuidearrowSolver.Field(height, width);
					} else {
						break;
					}
				}
			}
			level = (int) Math.sqrt(level * 4 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(記号：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getNumbersCand()[yIndex][xIndex].size() == 1) {
						int number = wkField.getNumbersCand()[yIndex][xIndex].get(0);
						if (number == 0) {
							sb.append("<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" fill=\"" + "black" + "\" width=\""
									+ (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
						}
					}
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						int number = wkField.getNumbers()[yIndex][xIndex];
						if (number != 0) {
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 2 + margin) + "\" x=\""
									+ (xIndex * baseSize + baseSize) + "\" font-size=\"" + (baseSize)
									+ "\" textLength=\"" + (baseSize) + "\" lengthAdjust=\"spacingAndGlyphs\">"
									+ (number == 1 ? "↑"
											: number == 2 ? "↓"
													: number == 3 ? "←" : number == 4 ? "→" : number == 5 ? "★" : "？")
									+ "</text>");
						}
					}
				}
			}
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
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 固定数字(表示用)ここでは1が↑、2が↓、3が←、4が→、5が★、6が？として処理
		private final Integer[][] numbers;
		// 数字の候補情報、0は黒マス
		protected List<Integer>[][] numbersCand;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://puzz.link/p?guidearrow/" + getXLength() + "/" + getYLength() + "/");
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] == 5) {
						// TODO 盤面サイズが16x16以上を出力する場合改修が必要
						sb.append(Integer.toHexString(xIndex + 1));
						sb.append(Integer.toHexString(yIndex + 1));
						break;
					}
				}
			}
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (numbers[yIndex][xIndex] == null || numbers[yIndex][xIndex] == 5) {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				} else {
					Integer num = numbers[yIndex][xIndex] + 10;
					if ((i + 1) / getXLength() < getYLength()
							&& numbers[(i + 1) / getXLength()][(i + 1) % getXLength()] != null
							&& numbers[(i + 1) / getXLength()][(i + 1) % getXLength()] != 5) {
						num = num - 10;
						i = i - 2;
					} else if ((i + 2) / getXLength() < getYLength()
							&& numbers[(i + 2) / getXLength()][(i + 2) % getXLength()] != null
							&& numbers[(i + 2) / getXLength()][(i + 2) % getXLength()] != 5) {
						num = num - 5;
						i = i - 1;
					}
					i = i + 2;
					String numStr = Integer.toHexString(num);
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
		public Field(int height, int width) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<Integer>();
					numbersCand[yIndex][xIndex].add(0);
					numbersCand[yIndex][xIndex].add(1);
					numbersCand[yIndex][xIndex].add(2);
					numbersCand[yIndex][xIndex].add(3);
					numbersCand[yIndex][xIndex].add(4);
				}
			}
			// ランダムで1マス★にする
			int y = (int) (Math.random() * height);
			int x = (int) (Math.random() * width);
			numbers[y][x] = 5;
			numbersCand[y][x] = new ArrayList<Integer>();
			numbersCand[y][x].add(5);
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<Integer>();
					numbersCand[yIndex][xIndex].add(0);
					numbersCand[yIndex][xIndex].add(1);
					numbersCand[yIndex][xIndex].add(2);
					numbersCand[yIndex][xIndex].add(3);
					numbersCand[yIndex][xIndex].add(4);
				}
			}

			int readPos = 0;
			char wkch = param.charAt(readPos);
			int hosiX;
			if (wkch == '-') {
				hosiX = Integer.parseInt("" + param.charAt(readPos + 1) + param.charAt(readPos + 2), 16);
				readPos++;
				readPos++;
				readPos++;
			} else {
				hosiX = Integer.parseInt(String.valueOf(wkch), 16);
				readPos++;
			}
			wkch = param.charAt(readPos);
			int hosiY;
			if (wkch == '-') {
				hosiY = Integer.parseInt("" + param.charAt(readPos + 1) + param.charAt(readPos + 2), 16);
				readPos++;
				readPos++;
				readPos++;
			} else {
				hosiY = Integer.parseInt(String.valueOf(wkch), 16);
				readPos++;
			}
			numbersCand[hosiY - 1][hosiX - 1] = new ArrayList<>();
			numbersCand[hosiY - 1][hosiX - 1].add(5);
			numbers[hosiY - 1][hosiX - 1] = 5;
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					if (ch == '.') {
						// ?マスなので黒マスの候補を消す
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbersCand[pos.getyIndex()][pos.getxIndex()].remove(new Integer(0));
						numbers[pos.getyIndex()][pos.getxIndex()] = 6;
					} else {
						int wkNum = Integer.parseInt(String.valueOf(ch), 16);
						int num = wkNum % 5;
						int skip = wkNum / 5;
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(num);
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
						index = index + skip;
					}
					index++;
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						int number = numbersCand[yIndex][xIndex].get(0);
						sb.append(number == 0 ? "■"
								: number == 1 ? "↑" : number == 2 ? "↓" : number == 3 ? "←" : number == 4 ? "→" : "★");
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
		 * ★から矢印を逆にたどっていくと全ての白マスに行ける。いけないマスは黒マス確定
		 */
		public boolean connectSolve() {
			Set<Position> allCandPos = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1 && numbersCand[yIndex][xIndex].get(0) == 5) {
						Position starpos = new Position(yIndex, xIndex);
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(starpos);
						setContinuePosSet(starpos, continuePosSet, null);
						allCandPos.addAll(continuePosSet);
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (!allCandPos.contains(new Position(yIndex, xIndex))) {
						numbersCand[yIndex][xIndex].remove(new Integer(1));
						numbersCand[yIndex][xIndex].remove(new Integer(2));
						numbersCand[yIndex][xIndex].remove(new Integer(3));
						numbersCand[yIndex][xIndex].remove(new Integer(4));
						if (numbersCand[yIndex][xIndex].isEmpty()) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に行ける方向のマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(2)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(3)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(1)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].contains(4)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 自身が黒マスの場合、黒マスの隣接は禁止。自身が黒マスでない場合、流入も流出もしない矢印の隣接は禁止。
		 */
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					List<Integer> numbersPivot = numbersCand[yIndex][xIndex];
					if (numbersPivot.size() == 1 && numbersPivot.get(0) == 0) {
						if (yIndex != 0) {
							numbersCand[yIndex - 1][xIndex].remove(new Integer(0));
							if (numbersCand[yIndex - 1][xIndex].isEmpty()) {
								return false;
							}
						}
						if (xIndex != getXLength() - 1) {
							numbersCand[yIndex][xIndex + 1].remove(new Integer(0));
							if (numbersCand[yIndex][xIndex + 1].isEmpty()) {
								return false;
							}
						}
						if (yIndex != getYLength() - 1) {
							numbersCand[yIndex + 1][xIndex].remove(new Integer(0));
							if (numbersCand[yIndex + 1][xIndex].isEmpty()) {
								return false;
							}
						}
						if (xIndex != 0) {
							numbersCand[yIndex][xIndex - 1].remove(new Integer(0));
							if (numbersCand[yIndex][xIndex - 1].isEmpty()) {
								return false;
							}
						}
					} else if (!numbersPivot.contains(0)) {
						if (yIndex != 0 && !numbersPivot.contains(1)) {
							numbersCand[yIndex - 1][xIndex].remove(new Integer(1));
							numbersCand[yIndex - 1][xIndex].remove(new Integer(3));
							numbersCand[yIndex - 1][xIndex].remove(new Integer(4));
							if (numbersCand[yIndex - 1][xIndex].isEmpty()) {
								return false;
							}
						}
						if (xIndex != getXLength() - 1 && !numbersPivot.contains(4)) {
							numbersCand[yIndex][xIndex + 1].remove(new Integer(1));
							numbersCand[yIndex][xIndex + 1].remove(new Integer(2));
							numbersCand[yIndex][xIndex + 1].remove(new Integer(4));
							if (numbersCand[yIndex][xIndex + 1].isEmpty()) {
								return false;
							}
						}
						if (yIndex != getYLength() - 1 && !numbersPivot.contains(2)) {
							numbersCand[yIndex + 1][xIndex].remove(new Integer(2));
							numbersCand[yIndex + 1][xIndex].remove(new Integer(3));
							numbersCand[yIndex + 1][xIndex].remove(new Integer(4));
							if (numbersCand[yIndex + 1][xIndex].isEmpty()) {
								return false;
							}
						}
						if (xIndex != 0 && !numbersPivot.contains(3)) {
							numbersCand[yIndex][xIndex - 1].remove(new Integer(1));
							numbersCand[yIndex][xIndex - 1].remove(new Integer(2));
							numbersCand[yIndex][xIndex - 1].remove(new Integer(3));
							if (numbersCand[yIndex][xIndex - 1].isEmpty()) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!connectSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
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
	protected int count = 0;

	public GuidearrowSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public GuidearrowSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?guidearrow/10/10/39lcgcwcjeibcneezldke"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new GuidearrowSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 4));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 4 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 4).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
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

package myamya.other.solver.snake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class SnakeSolver implements Solver {
	public static class SnakeGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class SnakeSolverForGenerator extends SnakeSolver {

			private final int limit;

			public SnakeSolverForGenerator(Field field, int limit) {
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

		public SnakeGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new SnakeGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			SnakeSolver.Field wkField = new SnakeSolver.Field(height, width);
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
					int yIndex = indexList.get(index) / width;
					int xIndex = indexList.get(index) % width;
					if (wkField.masu[yIndex][xIndex] == Masu.SPACE) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							SnakeSolver.Field virtual = new SnakeSolver.Field(wkField);
							if (masuNum < 1) {
								virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK;
							} else if (masuNum < 2) {
								virtual.masu[yIndex][xIndex] = Masu.BLACK;
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.masu = virtual.masu;
								break;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new SnakeSolver.Field(height, width);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				List<Position> hintPosList = new ArrayList<Position>();
				// 数字・ヒント埋め
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							int blackCnt = 0;
							Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : wkField.masu[yIndex - 1][xIndex];
							Masu masuRight = xIndex == wkField.getXLength() - 1 ? Masu.NOT_BLACK
									: wkField.masu[yIndex][xIndex + 1];
							Masu masuDown = yIndex == wkField.getYLength() - 1 ? Masu.NOT_BLACK
									: wkField.masu[yIndex + 1][xIndex];
							Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : wkField.masu[yIndex][xIndex - 1];
							if (masuUp == Masu.BLACK) {
								blackCnt++;
							}
							if (masuRight == Masu.BLACK) {
								blackCnt++;
							}
							if (masuDown == Masu.BLACK) {
								blackCnt++;
							}
							if (masuLeft == Masu.BLACK) {
								blackCnt++;
							}
							if (blackCnt == 1) {
								wkField.endPosSet.add(new Position(yIndex, xIndex));
								hintPosList.add(new Position(yIndex, xIndex));
							} else if (blackCnt == 2) {
								wkField.onRoutePosSet.add(new Position(yIndex, xIndex));
								hintPosList.add(new Position(yIndex, xIndex));
							}
						} else {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				// 縦のヒント
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					int blackCnt = 0;
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							blackCnt++;
						}
					}
					wkField.leftHints[yIndex] = blackCnt;
					hintPosList.add(new Position(yIndex, -1));
				}
				// 横のヒント
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					int blackCnt = 0;
					for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							blackCnt++;
						}
					}
					wkField.upHints[xIndex] = blackCnt;
					hintPosList.add(new Position(-1, xIndex));
				}
				// 解けるかな？
				level = new SnakeSolverForGenerator(new SnakeSolver.Field(wkField), 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new SnakeSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					Collections.shuffle(hintPosList);
					for (Position pos : hintPosList) {
						SnakeSolver.Field virtual = new SnakeSolver.Field(wkField, true);
						if (pos.getyIndex() == -1) {
							virtual.upHints[pos.getxIndex()] = null;
						} else if (pos.getxIndex() == -1) {
							virtual.leftHints[pos.getyIndex()] = null;
						} else {
							virtual.getEndPosSet().remove(pos);
							virtual.getOnRoutePosSet().remove(pos);
							virtual.masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
						}
						int solveResult = new SnakeSolverForGenerator(virtual, 10000).solve2();
						if (solveResult != -1) {
							level = solveResult;
							if (pos.getyIndex() == -1) {
								wkField.upHints[pos.getxIndex()] = null;
							} else if (pos.getxIndex() == -1) {
								wkField.leftHints[pos.getyIndex()] = null;
							} else {
								wkField.getEndPosSet().remove(pos);
								wkField.getOnRoutePosSet().remove(pos);
								wkField.masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
							}
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字/白/黒："
					+ wkField.getHintCount().split("/")[0] + "/"
					+ wkField.getHintCount().split("/")[1] + "/"
					+ wkField.getHintCount().split("/")[2] + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + baseSize) + "\" width=\""
							+ (wkField.getXLength() * baseSize + 2 * baseSize + baseSize) + "\" >");
			for (int xIndex = 0; xIndex < wkField.getUpHints().length; xIndex++) {
				if (wkField.getUpHints()[xIndex] != null) {
					String numberStr = String.valueOf(wkField.getUpHints()[xIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (baseSize - 4)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize + baseSize + 2)
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ masuStr
							+ "</text>");
				}
			}
			for (int yIndex = 0; yIndex < wkField.getLeftHints().length; yIndex++) {
				if (wkField.getLeftHints()[yIndex] != null) {
					String numberStr = String.valueOf(wkField.getLeftHints()[yIndex]);
					String masuStr;
					int idx = HALF_NUMS.indexOf(numberStr);
					if (idx >= 0) {
						masuStr = FULL_NUMS.substring(idx / 2, idx / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + baseSize - 4)
							+ "\" x=\""
							+ (baseSize + 2)
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ masuStr
							+ "</text>");
				}
			}
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getOnRoutePosSet().contains(new Position(yIndex, xIndex))) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");
					} else if (wkField.getEndPosSet().contains(new Position(yIndex, xIndex))) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"black\", stroke=\"black\">"
								+ "</circle>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
					sb.append("<line y1=\""
							+ (yIndex * baseSize + baseSize)
							+ "\" x1=\""
							+ (xIndex * baseSize + 3 * baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize)
							+ "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize)
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
			for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
				for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
					sb.append("<line y1=\""
							+ (yIndex * baseSize + baseSize + baseSize)
							+ "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + baseSize)
							+ "\" x2=\""
							+ (xIndex * baseSize + 3 * baseSize)
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
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;

		// ヒント情報。
		protected Integer[] upHints;
		protected Integer[] leftHints;

		// 終点マス
		private Set<Position> endPosSet;
		// 途中マス
		private Set<Position> onRoutePosSet;

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://puzz.link/p?snake/" + getXLength() + "/" + getYLength() + "/");
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex1 = i / getXLength();
				int xIndex1 = i % getXLength();
				i++;
				int yIndex2 = i / getXLength();
				int xIndex2 = i % getXLength();
				i++;
				int yIndex3 = i / getXLength();
				int xIndex3 = i % getXLength();
				int bitInfo = 0;
				if (yIndex1 < getYLength()) {
					if (onRoutePosSet.contains(new Position(yIndex1, xIndex1))) {
						bitInfo = bitInfo + 9;
					} else if (endPosSet.contains(new Position(yIndex1, xIndex1))) {
						bitInfo = bitInfo + 18;
					}
				}
				if (yIndex2 < getYLength()) {
					if (onRoutePosSet.contains(new Position(yIndex2, xIndex2))) {
						bitInfo = bitInfo + 3;
					} else if (endPosSet.contains(new Position(yIndex2, xIndex2))) {
						bitInfo = bitInfo + 6;
					}
				}
				if (yIndex3 < getYLength()) {
					if (onRoutePosSet.contains(new Position(yIndex3, xIndex3))) {
						bitInfo = bitInfo + 1;
					} else if (endPosSet.contains(new Position(yIndex3, xIndex3))) {
						bitInfo = bitInfo + 2;
					}
				}
				sb.append(Integer.toString(bitInfo, 36));
			}
			int interval = 0;
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					Integer num = upHints[xIndex];
					String numStr = Integer.toHexString(num);
					if (numStr.length() == 2) {
						numStr = "-" + numStr;
					}
					if (interval == 0) {
						sb.append(numStr);
					} else {
						sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
						sb.append(numStr);
						interval = 0;
					}
				} else {
					interval++;
					if (interval == 20) {
						sb.append("z");
						interval = 0;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					Integer num = leftHints[yIndex];
					String numStr = Integer.toHexString(num);
					if (numStr.length() == 2) {
						numStr = "-" + numStr;
					}
					if (interval == 0) {
						sb.append(numStr);
					} else {
						sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
						sb.append(numStr);
						interval = 0;
					}
				} else {
					interval++;
					if (interval == 20) {
						sb.append("z");
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
			int hintCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					hintCnt++;
				}
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					hintCnt++;
				}
			}
			return hintCnt + "/" + onRoutePosSet.size() + "/" + endPosSet.size();
		}

		public Masu[][] getMasu() {
			return masu;
		}

		public Set<Position> getEndPosSet() {
			return endPosSet;
		}

		public Set<Position> getOnRoutePosSet() {
			return onRoutePosSet;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Integer[] getUpHints() {
			return upHints;
		}

		public Integer[] getLeftHints() {
			return leftHints;
		}

		public Field(int height, int width) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			endPosSet = new HashSet<>();
			onRoutePosSet = new HashSet<>();
			upHints = new Integer[width];
			leftHints = new Integer[height];
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			endPosSet = new HashSet<>();
			onRoutePosSet = new HashSet<>();
			int index = 0;
			int readPos = 0;
			while (index < getYLength() * getXLength()) {
				char ch = param.charAt(readPos);
				int bitInfo = Character.getNumericValue(ch);
				int pos1 = bitInfo / 9 % 3;
				int pos2 = bitInfo / 3 % 3;
				int pos3 = bitInfo % 3;
				if (index / getXLength() < getYLength()) {
					if (pos1 == 1) {
						onRoutePosSet.add(new Position(index / getXLength(), index % getXLength()));
						masu[index / getXLength()][index % getXLength()] = Masu.BLACK;
					} else if (pos1 == 2) {
						endPosSet.add(new Position(index / getXLength(), index % getXLength()));
						masu[index / getXLength()][index % getXLength()] = Masu.BLACK;
					}
				}
				index++;
				if (index / getXLength() < getYLength()) {
					if (pos2 == 1) {
						onRoutePosSet.add(new Position(index / getXLength(), index % getXLength()));
						masu[index / getXLength()][index % getXLength()] = Masu.BLACK;
					} else if (pos2 == 2) {
						endPosSet.add(new Position(index / getXLength(), index % getXLength()));
						masu[index / getXLength()][index % getXLength()] = Masu.BLACK;
					}
				}
				index++;
				if (index / getXLength() < getYLength()) {
					if (pos3 == 1) {
						onRoutePosSet.add(new Position(index / getXLength(), index % getXLength()));
						masu[index / getXLength()][index % getXLength()] = Masu.BLACK;
					} else if (pos3 == 2) {
						endPosSet.add(new Position(index / getXLength(), index % getXLength()));
						masu[index / getXLength()][index % getXLength()] = Masu.BLACK;
					}
				}
				index++;
				readPos++;
			}
			upHints = new Integer[width];
			leftHints = new Integer[height];
			index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int capacity;
					if (ch == '.') {
						//
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							capacity = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
						if (index < width) {
							upHints[index] = capacity;
						} else {
							leftHints[index - width] = capacity;
						}
					}
					index++;
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			onRoutePosSet = other.onRoutePosSet;
			endPosSet = other.endPosSet;
			upHints = other.upHints;
			leftHints = other.leftHints;
		}

		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			onRoutePosSet = new HashSet<>(other.onRoutePosSet);
			endPosSet = new HashSet<>(other.endPosSet);
			upHints = new Integer[other.getXLength()];
			leftHints = new Integer[other.getYLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				leftHints[yIndex] = other.leftHints[yIndex];
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				upHints[xIndex] = other.upHints[xIndex];
			}
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					String numStr = String.valueOf(upHints[xIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == -1) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
			}
			sb.append("　");
			sb.append(System.lineSeparator());
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				sb.append("↓");
			}
			sb.append("　");
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					String numStr = String.valueOf(leftHints[yIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == -1) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
				sb.append("→");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (onRoutePosSet.contains(new Position(yIndex, xIndex))) {
						sb.append("○");
					} else if (endPosSet.contains(new Position(yIndex, xIndex))) {
						sb.append("●");
					} else {
						sb.append(masu[yIndex][xIndex]);
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
					sb.append(masu[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 置くと池ができるマスを白にする。既に池ができている場合falseを返す。
		 * また、オセロの初期配置型になることもないので、
		 * それを見つけたらfalseを返す。
		 */
		public boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.NOT_BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE
							&& masu4 == Masu.BLACK) {
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK
							&& masu4 == Masu.BLACK) {
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.SPACE
							&& masu4 == Masu.BLACK) {
						masu[yIndex + 1][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.BLACK) {
						masu[yIndex][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}

				}
			}
			return true;
		}

		/**
		 * ヒントはその列の塗られる数である。数字を上回ったり下回ったりすることが確定したらfalseを返す。
		 */
		private boolean hintSolve() {
			// 縦のヒント
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					int blackCnt = 0;
					int spaceCnt = 0;
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							blackCnt++;
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							spaceCnt++;
						}
					}
					if (leftHints[yIndex] < blackCnt) {
						return false;
					}
					if (leftHints[yIndex] > blackCnt + spaceCnt) {
						return false;
					}
					if (leftHints[yIndex] == blackCnt) {
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
					if (leftHints[yIndex] == blackCnt + spaceCnt) {
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
			// 横のヒント
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					int blackCnt = 0;
					int spaceCnt = 0;
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							blackCnt++;
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							spaceCnt++;
						}
					}
					if (upHints[xIndex] < blackCnt) {
						return false;
					}
					if (upHints[xIndex] > blackCnt + spaceCnt) {
						return false;
					}
					if (upHints[xIndex] == blackCnt) {
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
					if (upHints[xIndex] == blackCnt + spaceCnt) {
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}

			return true;
		}

		/**
		 * ●は隣接黒が1つ、○は隣接黒が2つ。それ以外の黒マスは隣接黒が1か2。違ったらfalse。
		 * また、隣接黒が1つのマスが3箇所以上できてはいけない。
		 */
		public boolean masuSolve() {
			int endPointCnt = 0;
			int endPointCandCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int blackCnt = 0;
					int whiteCnt = 0;
					Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
					Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
					Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
					Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
					if (masuUp == Masu.BLACK) {
						blackCnt++;
					} else if (masuUp == Masu.NOT_BLACK) {
						whiteCnt++;
					}
					if (masuRight == Masu.BLACK) {
						blackCnt++;
					} else if (masuRight == Masu.NOT_BLACK) {
						whiteCnt++;
					}
					if (masuDown == Masu.BLACK) {
						blackCnt++;
					} else if (masuDown == Masu.NOT_BLACK) {
						whiteCnt++;
					}
					if (masuLeft == Masu.BLACK) {
						blackCnt++;
					} else if (masuLeft == Masu.NOT_BLACK) {
						whiteCnt++;
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						if (onRoutePosSet.contains(new Position(yIndex, xIndex))) {
							if (blackCnt > 2) {
								return false;
							}
							if (whiteCnt > 2) {
								return false;
							}
							if (blackCnt == 2) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
								}
							}
							if (whiteCnt == 2) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.BLACK;
								}
							}
						} else if (endPosSet.contains(new Position(yIndex, xIndex))) {
							endPointCnt++;
							if (blackCnt > 1) {
								return false;
							}
							if (whiteCnt > 3) {
								return false;
							}
							if (blackCnt == 1) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
								}
							}
							if (whiteCnt == 3) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.BLACK;
								}
							}
						} else {
							if (blackCnt > 2) {
								return false;
							}
							if (whiteCnt > 3) {
								return false;
							}
							if (blackCnt == 2) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
								}
							}
							if (whiteCnt == 3) {
								endPointCnt++;
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.BLACK;
								}
							}
						}
					}
					if (masu[yIndex][xIndex] != Masu.NOT_BLACK) {
						if (blackCnt < 2) {
							endPointCandCnt++;
						}
					}
				}
			}
			if (endPointCnt > 2) {
				return false;
			}
			if (endPointCandCnt < 2) {
				return false;
			}
			return true;
		}

		/**
		 * 黒マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectBlackSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position blackPos = new Position(yIndex, xIndex);
						if (blackPosSet.size() == 0) {
							blackPosSet.add(blackPos);
							setContinueBlackPosSet(blackPos, blackPosSet, null);
						} else {
							if (!blackPosSet.contains(blackPos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に白確定でないマスをつなげていく。壁は無視する。
		 */
		private void setContinueBlackPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueBlackPosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueBlackPosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueBlackPosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueBlackPosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * @param i
		 * @param recursive
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!pondSolve()) {
				return false;
			}
			if (!hintSolve()) {
				return false;
			}
			if (!masuSolve()) {
				return false;
			}
			if (!connectBlackSolve()) {
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
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public SnakeSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public SnakeSolver(Field field) {
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
		System.out.println(new SnakeSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		//System.out.println(field);
		// String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		//		if (!field.getStateDump().equals(str)) {
		//			return candSolve(field, recursive);
		//		}
		return true;
	}

	/**
	 * 1つのマスに対する仮置き調査
	 */
	private boolean oneCandSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.masu[yIndex][xIndex] = Masu.BLACK;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.masu[yIndex][xIndex] = Masu.NOT_BLACK;
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
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
		} else {
			// どちらにしても理論
			for (int y = 0; y < field.getYLength(); y++) {
				for (int x = 0; x < field.getXLength(); x++) {
					if (virtual2.masu[y][x] == virtual.masu[y][x]) {
						field.masu[y][x] = virtual.masu[y][x];
					}
				}
			}
		}
		return true;
	}
}

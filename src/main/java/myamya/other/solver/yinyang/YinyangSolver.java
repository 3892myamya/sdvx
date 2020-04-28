package myamya.other.solver.yinyang;

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

public class YinyangSolver implements Solver {
	public static class YinyangGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class YinyangSolverForGenerator extends YinyangSolver {

			private final int limit;

			public YinyangSolverForGenerator(Field field, int limit) {
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

		public YinyangGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new YinyangGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			YinyangSolver.Field wkField = new YinyangSolver.Field(height, width);
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
							YinyangSolver.Field virtual = new YinyangSolver.Field(wkField);
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
							wkField = new YinyangSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 解けるかな？
				level = new YinyangSolverForGenerator(new YinyangSolver.Field(wkField), 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new YinyangSolver.Field(height, width);
					index = 0;
				} else {
					Collections.shuffle(indexList);
					for (Integer indexBase : indexList) {
						int yIndex = indexBase / width;
						int xIndex = indexBase % width;
						YinyangSolver.Field virtual = new YinyangSolver.Field(wkField);
						virtual.masu[yIndex][xIndex] = Masu.SPACE;
						int solveResult = new YinyangSolverForGenerator(virtual, 6000).solve2();
						if (solveResult != -1) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
							level = solveResult;
						}
					}
					for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
						for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
							if (wkField.masu[yIndex][xIndex] != Masu.SPACE) {
								wkField.fixedPosSet.add(new Position(yIndex, xIndex));
							}
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level) + 1;
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
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					Masu oneMasu = wkField.getMasu()[yIndex][xIndex];
					if (oneMasu == Masu.NOT_BLACK) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
								+ "\" r=\""
								+ (baseSize / 2 - 2)
								+ "\" fill=\"white\", stroke=\"black\">"
								+ "</circle>");

					} else if (oneMasu == Masu.BLACK) {
						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
								+ "\" cx=\""
								+ (xIndex * baseSize + baseSize + (baseSize / 2))
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

		// マスの情報
		private Masu[][] masu;

		// 最初から決まっているマス
		private Set<Position> fixedPosSet;

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?yinyang/" + getXLength() + "/" + getYLength() + "/");
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
					if (masu[yIndex1][xIndex1] == Masu.NOT_BLACK) {
						bitInfo = bitInfo + 9;
					} else if (masu[yIndex1][xIndex1] == Masu.BLACK) {
						bitInfo = bitInfo + 18;
					}
				}
				if (yIndex2 < getYLength()) {
					if (masu[yIndex2][xIndex2] == Masu.NOT_BLACK) {
						bitInfo = bitInfo + 3;
					} else if (masu[yIndex2][xIndex2] == Masu.BLACK) {
						bitInfo = bitInfo + 6;
					}
				}
				if (yIndex3 < getYLength()) {
					if (masu[yIndex3][xIndex3] == Masu.NOT_BLACK) {
						bitInfo = bitInfo + 1;
					} else if (masu[yIndex3][xIndex3] == Masu.BLACK) {
						bitInfo = bitInfo + 2;
					}
				}
				sb.append(Integer.toString(bitInfo, 36));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public String getHintCount() {
			return String.valueOf(fixedPosSet.size());
		}

		public Masu[][] getMasu() {
			return masu;
		}

		public Set<Position> getFixedPosSet() {
			return fixedPosSet;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			fixedPosSet = new HashSet<>();
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			fixedPosSet = new HashSet<>();
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int bitInfo = Character.getNumericValue(ch);
				int pos1 = bitInfo / 9 % 3;
				int pos2 = bitInfo / 3 % 3;
				int pos3 = bitInfo % 3;
				if (index / getXLength() < getYLength()) {
					masu[index / getXLength()][index % getXLength()] = Masu.getByVal(pos1);
					if (pos1 > 0) {
						fixedPosSet.add(new Position(index / getXLength(), index % getXLength()));
					}
				}
				index++;
				if (index / getXLength() < getYLength()) {
					masu[index / getXLength()][index % getXLength()] = Masu.getByVal(pos2);
					if (pos2 > 0) {
						fixedPosSet.add(new Position(index / getXLength(), index % getXLength()));
					}
				}
				index++;
				if (index / getXLength() < getYLength()) {
					masu[index / getXLength()][index % getXLength()] = Masu.getByVal(pos3);
					if (pos3 > 0) {
						fixedPosSet.add(new Position(index / getXLength(), index % getXLength()));
					}
				}
				index++;
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
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
		 * 置くと池・逆池ができるマスを白・黒マスにする。
		 * 既に池・逆池ができている場合falseを返す。
		 * また、しろまるくろまるのルール上、オセロの初期配置型になることもないので、
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
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
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
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.SPACE
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex + 1][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.SPACE && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.NOT_BLACK && masu3 == Masu.NOT_BLACK
							&& masu4 == Masu.NOT_BLACK) {
						masu[yIndex][xIndex] = Masu.BLACK;
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
		 * しろまるくろまるのルール上、外周で石が3回以上別の石で分断されていたらfalseを返す。
		 */
		public boolean wallSolve() {
			List<Position> wallSet = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				wallSet.add(new Position(yIndex, 0));
			}
			for (int xIndex = 1; xIndex < getXLength(); xIndex++) {
				wallSet.add(new Position(getYLength() - 1, xIndex));
			}
			for (int yIndex = getYLength() - 2; yIndex >= 0; yIndex--) {
				wallSet.add(new Position(yIndex, getXLength() - 1));
			}
			for (int xIndex = getXLength() - 2; xIndex >= 1; xIndex--) {
				wallSet.add(new Position(0, xIndex));
			}
			int sprittedCnt = 0;
			Masu continueStone = null;
			for (Position pos : wallSet) {
				Masu oneMasu = masu[pos.getyIndex()][pos.getxIndex()];
				if (oneMasu != Masu.SPACE) {
					if (continueStone == null) {
						continueStone = masu[pos.getyIndex()][pos.getxIndex()];
					} else {
						if (continueStone != oneMasu) {
							// 違う石を発見
							sprittedCnt++;
							if (sprittedCnt >= 3) {
								// 3回以上分断で失敗
								return false;
							}
							continueStone = oneMasu;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスがひとつながりにならない場合Falseを返す。
		 * 今までのロジックより高速に動きます。
		 */
		public boolean connectWhiteSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
							whitePosSet.add(whitePos);
							setContinueWhitePosSet(whitePos, whitePosSet, null);
						} else {
							if (!whitePosSet.contains(whitePos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
		 */
		private void setContinueWhitePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueWhitePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
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
			if (!wallSolve()) {
				return false;
			}
			if (!connectWhiteSolve()) {
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

	public YinyangSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public YinyangSolver(Field field) {
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
		System.out.println(new YinyangSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 3));
		System.out.println(field);
		int level = (int) Math.sqrt(count) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 3).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
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

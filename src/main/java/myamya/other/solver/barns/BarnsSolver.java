package myamya.other.solver.barns;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class BarnsSolver implements Solver {
	public static class BarnsGenerator implements Generator {

		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";

		static class ExtendedField extends BarnsSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width) {
				super(height, width);
			}

			/**
			 * 作問中は各マスの壁の数が0になってもよいので。
			 */
			@Override
			public boolean nextSolve() {
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
						if (existsCount > 2 || (notExistsCount == 3 && existsCount == 1)) {
							return false;
						}
						if (existsCount == 2) {
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
						} else if (notExistsCount > 2) {
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
				return true;
			}

			public String getPuzPreURL() {
				StringBuilder sb = new StringBuilder();
				sb.append("http://pzv.jp/p.html?barns/" + getXLength() + "/" + getYLength() + "/");
				for (int i = 0; i < getYLength() * getXLength(); i++) {
					int yIndex1 = i / getXLength();
					int xIndex1 = i % getXLength();
					i++;
					int yIndex2 = -1;
					int xIndex2 = -1;
					if (i < getYLength() * getXLength()) {
						yIndex2 = i / getXLength();
						xIndex2 = i % getXLength();
					}
					i++;
					int yIndex3 = -1;
					int xIndex3 = -1;
					if (i < getYLength() * getXLength()) {
						yIndex3 = i / getXLength();
						xIndex3 = i % getXLength();
					}
					i++;
					int yIndex4 = -1;
					int xIndex4 = -1;
					if (i < getYLength() * getXLength()) {
						yIndex4 = i / getXLength();
						xIndex4 = i % getXLength();
					}
					i++;
					int yIndex5 = -1;
					int xIndex5 = -1;
					if (i < getYLength() * getXLength()) {
						yIndex5 = i / getXLength();
						xIndex5 = i % getXLength();
					}
					int num = 0;
					if (yIndex1 != -1 && xIndex1 != -1 && circle[yIndex1][xIndex1]) {
						num = num + 16;
					}
					if (yIndex2 != -1 && xIndex2 != -1 && circle[yIndex2][xIndex2]) {
						num = num + 8;
					}
					if (yIndex3 != -1 && xIndex3 != -1 && circle[yIndex3][xIndex3]) {
						num = num + 4;
					}
					if (yIndex4 != -1 && xIndex4 != -1 && circle[yIndex4][xIndex4]) {
						num = num + 2;
					}
					if (yIndex5 != -1 && xIndex5 != -1 && circle[yIndex5][xIndex5]) {
						num = num + 1;
					}
					sb.append(ALPHABET_AND_NUMBER.substring(num, num + 1));
				}
				for (int i = 0; i < getYLength() * (getXLength() - 1); i++) {
					int yIndex1 = i / (getXLength() - 1);
					int xIndex1 = i % (getXLength() - 1);
					i++;
					int yIndex2 = -1;
					int xIndex2 = -1;
					if (i < getYLength() * (getXLength() - 1)) {
						yIndex2 = i / (getXLength() - 1);
						xIndex2 = i % (getXLength() - 1);
					}
					i++;
					int yIndex3 = -1;
					int xIndex3 = -1;
					if (i < getYLength() * (getXLength() - 1)) {
						yIndex3 = i / (getXLength() - 1);
						xIndex3 = i % (getXLength() - 1);
					}
					i++;
					int yIndex4 = -1;
					int xIndex4 = -1;
					if (i < getYLength() * (getXLength() - 1)) {
						yIndex4 = i / (getXLength() - 1);
						xIndex4 = i % (getXLength() - 1);
					}
					i++;
					int yIndex5 = -1;
					int xIndex5 = -1;
					if (i < getYLength() * (getXLength() - 1)) {
						yIndex5 = i / (getXLength() - 1);
						xIndex5 = i % (getXLength() - 1);
					}
					int num = 0;
					if (yIndex1 != -1 && xIndex1 != -1 && yokoWall[yIndex1][xIndex1] == Wall.EXISTS) {
						num = num + 16;
					}
					if (yIndex2 != -1 && xIndex2 != -1 && yokoWall[yIndex2][xIndex2] == Wall.EXISTS) {
						num = num + 8;
					}
					if (yIndex3 != -1 && xIndex3 != -1 && yokoWall[yIndex3][xIndex3] == Wall.EXISTS) {
						num = num + 4;
					}
					if (yIndex4 != -1 && xIndex4 != -1 && yokoWall[yIndex4][xIndex4] == Wall.EXISTS) {
						num = num + 2;
					}
					if (yIndex5 != -1 && xIndex5 != -1 && yokoWall[yIndex5][xIndex5] == Wall.EXISTS) {
						num = num + 1;
					}
					sb.append(ALPHABET_AND_NUMBER.substring(num, num + 1));
				}
				for (int i = 0; i < (getYLength() - 1) * getXLength(); i++) {
					int yIndex1 = i / getXLength();
					int xIndex1 = i % getXLength();
					i++;
					int yIndex2 = -1;
					int xIndex2 = -1;
					if (i < (getYLength() - 1) * getXLength()) {
						yIndex2 = i / getXLength();
						xIndex2 = i % getXLength();
					}
					i++;
					int yIndex3 = -1;
					int xIndex3 = -1;
					if (i < (getYLength() - 1) * getXLength()) {
						yIndex3 = i / getXLength();
						xIndex3 = i % getXLength();
					}
					i++;
					int yIndex4 = -1;
					int xIndex4 = -1;
					if (i < (getYLength() - 1) * getXLength()) {
						yIndex4 = i / getXLength();
						xIndex4 = i % getXLength();
					}
					i++;
					int yIndex5 = -1;
					int xIndex5 = -1;
					if (i < (getYLength() - 1) * getXLength()) {
						yIndex5 = i / getXLength();
						xIndex5 = i % getXLength();
					}
					int num = 0;
					if (yIndex1 != -1 && xIndex1 != -1 && tateWall[yIndex1][xIndex1] == Wall.EXISTS) {
						num = num + 16;
					}
					if (yIndex2 != -1 && xIndex2 != -1 && tateWall[yIndex2][xIndex2] == Wall.EXISTS) {
						num = num + 8;
					}
					if (yIndex3 != -1 && xIndex3 != -1 && tateWall[yIndex3][xIndex3] == Wall.EXISTS) {
						num = num + 4;
					}
					if (yIndex4 != -1 && xIndex4 != -1 && tateWall[yIndex4][xIndex4] == Wall.EXISTS) {
						num = num + 2;
					}
					if (yIndex5 != -1 && xIndex5 != -1 && tateWall[yIndex5][xIndex5] == Wall.EXISTS) {
						num = num + 1;
					}
					sb.append(ALPHABET_AND_NUMBER.substring(num, num + 1));
				}
				return sb.toString();
			}

			public String getHintCount() {
				int barnCount = 0;
				int wallCnt = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (circle[yIndex][xIndex]) {
							barnCount++;
						}
					}
				}
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
						if (yokoWall[yIndex][xIndex] == Wall.EXISTS) {
							wallCnt++;
						}
					}
				}
				for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (tateWall[yIndex][xIndex] == Wall.EXISTS) {
							wallCnt++;
						}
					}
				}
				return String.valueOf(barnCount + "/" + wallCnt);
			}
		}

		static class BarnsSolverForGenerator extends BarnsSolver {
			private final int limit;

			public BarnsSolverForGenerator(Field field, int limit) {
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

		public BarnsGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new BarnsGenerator(10, 10).generate();
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
				// アイスバーン化
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
							// 十字マスは必ずアイスバーン化
							wkField.circle[pos.getyIndex()][pos.getxIndex()] = true;
						} else if ((wallUp == Wall.NOT_EXISTS && wallDown == Wall.NOT_EXISTS) ||
								((wallRight == Wall.NOT_EXISTS && wallLeft == Wall.NOT_EXISTS))) {
							if (Math.random() * 2 < 1) {
								// 直進マスは1/2の確率でアイスバーン化
								wkField.circle[pos.getyIndex()][pos.getxIndex()] = true;
							}
						}
					}
				}
				//壁なしマスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						if (wkField.yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
						}
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
						}
					}
				}
				// 解けるかな？
				level = new BarnsSolverForGenerator(new BarnsSolver.Field(wkField), 50).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width);
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
						BarnsSolver.Field virtual = new BarnsSolver.Field(wkField);
						if (toYokoWall) {
							if (virtual.yokoWall[yIndex][xIndex] == Wall.SPACE) {
								// すでに壁なし
								continue;
							}
							virtual.yokoWall[yIndex][xIndex] = Wall.SPACE;
						} else {
							if (virtual.tateWall[yIndex][xIndex] == Wall.SPACE) {
								// すでに壁なし
								continue;
							}
							virtual.tateWall[yIndex][xIndex] = Wall.SPACE;
						}
						int solveResult = new BarnsSolverForGenerator(virtual, 600).solve2();
						if (solveResult != -1) {
							if (toYokoWall) {
								wkField.yokoWall[yIndex][xIndex] = Wall.SPACE;
							} else {
								wkField.tateWall[yIndex][xIndex] = Wall.SPACE;
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 25 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(バーン:" + wkField.getHintCount().split("/")[0] + "、壁："
					+ wkField.getHintCount().split("/")[1] + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize) + "\" width=\""
							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					if (wkField.getCircle()[yIndex][xIndex]) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (baseSize)
								+ "\" fill=\""
								+ "lightblue"
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1
							|| wkField.getYokoWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneYokoWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + 2 * baseSize)
								+ "\" width=\""
								+ (1)
								+ "\" height=\""
								+ (baseSize)
								+ "\">"
								+ "</rect>");
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1
							|| wkField.getTateWall()[yIndex][xIndex] == Wall.EXISTS;
					if (oneTateWall) {
						sb.append("<rect y=\"" + (yIndex * baseSize + baseSize)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" width=\""
								+ (baseSize)
								+ "\" height=\""
								+ (1)
								+ "\">"
								+ "</rect>");
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
		static final String ALPHABET_FROM_H = "hijklmnopqrstuvwxyz";

		protected final boolean[][] circle;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected Wall[][] tateWall;

		// バーンズのみ 表出している横壁 画面表示用
		protected final Wall[][] firstYokoWall;
		// バーンズのみ 表出している縦壁 画面表示用
		protected final Wall[][] firstTateWall;
		// パイプリンクのみ 表出マス情報 画面表示用
		protected final Set<Position> firstPosSet;

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public Wall[][] getFirstYokoWall() {
			return firstYokoWall;
		}

		public Wall[][] getFirstTateWall() {
			return firstTateWall;
		}

		public Set<Position> getFirstPosSet() {
			return firstPosSet;
		}

		public boolean[][] getCircle() {
			return circle;
		}

		public int getYLength() {
			return circle.length;
		}

		public int getXLength() {
			return circle[0].length;
		}

		public Field(int height, int width) {
			circle = new boolean[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			firstYokoWall = new Wall[height][width - 1];
			firstTateWall = new Wall[height - 1][width];
			firstPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.SPACE;
					firstYokoWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.SPACE;
					firstTateWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
		}

		public Field(int height, int width, String param, boolean barns) {
			circle = new boolean[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			firstYokoWall = new Wall[height][width - 1];
			firstTateWall = new Wall[height - 1][width];
			firstPosSet = new HashSet<>();
			// バーンズと帰ってきたパイプリンクは作問者が1個だけ表出する壁を作れるかどうかだけで、
			// パズルとしては実質同じである。盤面解釈の方法だけ変更。
			if (barns) {
				int readPos = 0;
				int bit = 0;
				for (int cnt = 0; cnt < getYLength() * getXLength(); cnt++) {
					int mod = cnt % 5;
					if (mod == 0) {
						bit = Character.getNumericValue(param.charAt(readPos));
						readPos++;
					}
					if (mod == 4 || cnt == (getYLength() * getXLength()) - 1) {
						if (mod >= 0) {
							circle[(cnt - mod + 0) / getXLength()][(cnt - mod + 0)
									% getXLength()] = bit / 16 % 2 == 1;
						}
						if (mod >= 1) {
							circle[(cnt - mod + 1) / getXLength()][(cnt - mod + 1)
									% getXLength()] = bit / 8 % 2 == 1;
						}
						if (mod >= 2) {
							circle[(cnt - mod + 2) / getXLength()][(cnt - mod + 2)
									% getXLength()] = bit / 4 % 2 == 1;
						}
						if (mod >= 3) {
							circle[(cnt - mod + 3) / getXLength()][(cnt - mod + 3)
									% getXLength()] = bit / 2 % 2 == 1;
						}
						if (mod >= 4) {
							circle[(cnt - mod + 4) / getXLength()][(cnt - mod + 4)
									% getXLength()] = bit / 1 % 2 == 1;
						}
					}
				}
				for (int cnt = 0; cnt < getYLength() * (getXLength() - 1); cnt++) {
					int mod = cnt % 5;
					if (mod == 0) {
						bit = Character.getNumericValue(param.charAt(readPos));
						readPos++;
					}
					if (mod == 4 || cnt == (getYLength() * (getXLength() - 1)) - 1) {
						if (mod >= 0) {
							yokoWall[(cnt - mod + 0) / (getXLength() - 1)][(cnt - mod + 0)
									% (getXLength() - 1)] = bit / 16
											% 2 == 1 ? Wall.EXISTS : Wall.SPACE;
						}
						if (mod >= 1) {
							yokoWall[(cnt - mod + 1) / (getXLength() - 1)][(cnt - mod + 1)
									% (getXLength() - 1)] = bit / 8
											% 2 == 1 ? Wall.EXISTS : Wall.SPACE;
						}
						if (mod >= 2) {
							yokoWall[(cnt - mod + 2) / (getXLength() - 1)][(cnt - mod + 2)
									% (getXLength() - 1)] = bit / 4
											% 2 == 1 ? Wall.EXISTS : Wall.SPACE;
						}
						if (mod >= 3) {
							yokoWall[(cnt - mod + 3) / (getXLength() - 1)][(cnt - mod + 3)
									% (getXLength() - 1)] = bit / 2
											% 2 == 1 ? Wall.EXISTS : Wall.SPACE;
						}
						if (mod >= 4) {
							yokoWall[(cnt - mod + 4) / (getXLength() - 1)][(cnt - mod + 4)
									% (getXLength() - 1)] = bit / 1
											% 2 == 1 ? Wall.EXISTS : Wall.SPACE;
						}
					}
				}
				for (int cnt = 0; cnt < (getYLength() - 1) * getXLength(); cnt++) {
					int mod = cnt % 5;
					if (mod == 0) {
						bit = Character.getNumericValue(param.charAt(readPos));
						readPos++;
					}
					if (mod == 4 || cnt == ((getYLength() - 1) * getXLength()) - 1) {
						if (mod >= 0) {
							tateWall[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16 % 2 == 1
									? Wall.EXISTS
									: Wall.SPACE;
						}
						if (mod >= 1) {
							tateWall[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1
									? Wall.EXISTS
									: Wall.SPACE;
						}
						if (mod >= 2) {
							tateWall[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1
									? Wall.EXISTS
									: Wall.SPACE;
						}
						if (mod >= 3) {
							tateWall[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1
									? Wall.EXISTS
									: Wall.SPACE;
						}
						if (mod >= 4) {
							tateWall[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1
									? Wall.EXISTS
									: Wall.SPACE;
						}
					}
				}
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
						firstYokoWall[yIndex][xIndex] = yokoWall[yIndex][xIndex];
					}
				}
				for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						firstTateWall[yIndex][xIndex] = tateWall[yIndex][xIndex];
					}
				}
			} else {
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						circle[yIndex][xIndex] = false;
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
				for (int i = 0; i < param.length(); i++) {
					char ch = param.charAt(i);
					int interval = ALPHABET_FROM_H.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						int chVal = Character.getNumericValue(ch);
						if (chVal >= 0 && chVal <= 9) {
							circle[index / getXLength()][index % getXLength()] = true;
							for (int cnt = 0; cnt < chVal; cnt++) {
								index++;
								circle[index / getXLength()][index % getXLength()] = true;
							}
						} else {
							Position pos = new Position(index / getXLength(), index % getXLength());
							if (ch == '.') {
								//
							} else if (ch == 'a') {
								firstPosSet.add(pos);
								if (pos.getxIndex() != 0) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
								}
								if (pos.getxIndex() != getXLength() - 1) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
								}
								if (pos.getyIndex() != 0) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
								}
								if (pos.getyIndex() != getYLength() - 1) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
								}
							} else if (ch == 'b') {
								firstPosSet.add(pos);
								if (pos.getxIndex() != 0) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
								}
								if (pos.getxIndex() != getXLength() - 1) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
								}
								if (pos.getyIndex() != 0) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
								}
								if (pos.getyIndex() != getYLength() - 1) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
								}
							} else if (ch == 'c') {
								firstPosSet.add(pos);
								if (pos.getxIndex() != 0) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
								}
								if (pos.getxIndex() != getXLength() - 1) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
								}
								if (pos.getyIndex() != 0) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
								}
								if (pos.getyIndex() != getYLength() - 1) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
								}
							} else if (ch == 'd') {
								firstPosSet.add(pos);
								if (pos.getxIndex() != 0) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
								}
								if (pos.getxIndex() != getXLength() - 1) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
								}
								if (pos.getyIndex() != 0) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
								}
								if (pos.getyIndex() != getYLength() - 1) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
								}
							} else if (ch == 'e') {
								firstPosSet.add(pos);
								if (pos.getxIndex() != 0) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
								}
								if (pos.getxIndex() != getXLength() - 1) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
								}
								if (pos.getyIndex() != 0) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.NOT_EXISTS;
								}
								if (pos.getyIndex() != getYLength() - 1) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
								}
							} else if (ch == 'f') {
								firstPosSet.add(pos);
								if (pos.getxIndex() != 0) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.NOT_EXISTS;
								}
								if (pos.getxIndex() != getXLength() - 1) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.EXISTS;
								}
								if (pos.getyIndex() != 0) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
								}
								if (pos.getyIndex() != getYLength() - 1) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
								}
							} else if (ch == 'g') {
								firstPosSet.add(pos);
								if (pos.getxIndex() != 0) {
									yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = Wall.EXISTS;
								}
								if (pos.getxIndex() != getXLength() - 1) {
									yokoWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
								}
								if (pos.getyIndex() != 0) {
									tateWall[pos.getyIndex() - 1][pos.getxIndex()] = Wall.EXISTS;
								}
								if (pos.getyIndex() != getYLength() - 1) {
									tateWall[pos.getyIndex()][pos.getxIndex()] = Wall.NOT_EXISTS;
								}
							}
						}
						index++;
					}
				}
			}

		}

		public Field(Field other) {
			circle = other.circle;
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
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
			firstYokoWall = other.firstYokoWall;
			firstTateWall = other.firstTateWall;
			firstPosSet = other.firstPosSet;
		}

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
					sb.append(circle[yIndex][xIndex] ? "○" : "　");
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
					if (circle[yIndex][xIndex]) {
						if (existsCount > 2 || (notExistsCount == 3 && existsCount == 1)) {
							return false;
						}
						if ((wallUp == Wall.EXISTS && wallRight == Wall.EXISTS)
								|| (wallUp == Wall.EXISTS && wallLeft == Wall.EXISTS)
								|| (wallRight == Wall.EXISTS && wallDown == Wall.EXISTS)
								|| (wallDown == Wall.EXISTS && wallLeft == Wall.EXISTS)) {
							// カーブ禁止
							return false;
						}
						if (existsCount == 2) {
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
						} else if (notExistsCount == 3) {
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
						// 直進させる
						if (wallDown == Wall.NOT_EXISTS && wallUp == Wall.SPACE) {
							tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallLeft == Wall.NOT_EXISTS && wallRight == Wall.SPACE) {
							yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallUp == Wall.NOT_EXISTS && wallDown == Wall.SPACE) {
							tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
						}
						if (wallRight == Wall.NOT_EXISTS && wallLeft == Wall.SPACE) {
							yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
						}
						// 上下いずれかに壁があれば、横直進決定。
						if (wallUp == Wall.EXISTS || wallDown == Wall.EXISTS) {
							if (wallUp == Wall.SPACE) {
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
							}
						}
						// 左右いずれかに壁があれば、縦直進決定。
						if (wallRight == Wall.EXISTS || wallLeft == Wall.EXISTS) {
							if (wallUp == Wall.SPACE) {
								tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.SPACE) {
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
							}
							if (wallDown == Wall.SPACE) {
								tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.SPACE) {
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
							}
						}
					} else {
						if (existsCount > 2 || notExistsCount > 2) {
							return false;
						}
						if (existsCount == 2) {
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
						} else if (notExistsCount == 2) {
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
			return true;
		}

		/**
		 * 白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// 交差点の可能性があるマスから開始しないようにする
					int existsCount = 0;
					Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					}
					Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					}
					Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					}
					Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					}
					if (existsCount == 0) {
						continue;
					}
					Position originPos = new Position(yIndex, xIndex);
					Set<Position> continuePosSet = new HashSet<>();
					continuePosSet.add(originPos);
					if (setContinuePosSet(originPos, originPos, continuePosSet, null)) {
						return continuePosSet.size() == getYLength() * getXLength();
					}
				}
			}
			return true;

		}

		/**
		 * posを起点に壁なし確定のマスを、直線を優先してつないでいく。
		 * 出発マスまで戻ってきたらtrueを返す。
		 * 進む方向が確定できなくなった時点でfalseを返す。
		 */
		private boolean setContinuePosSet(Position originPos, Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (pos.getyIndex() != 0 && from == Direction.DOWN
					&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS) {
				// 直進して上へ
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (originPos.equals(nextPos)) {
					return true;
				}
				continuePosSet.add(nextPos);
				return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.DOWN);
			} else if (pos.getxIndex() != getXLength() - 1 && from == Direction.LEFT
					&& yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
				// 直進して右へ
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (originPos.equals(nextPos)) {
					return true;
				}
				continuePosSet.add(nextPos);
				return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.LEFT);
			} else if (pos.getyIndex() != getYLength() - 1 && from == Direction.UP
					&& tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
				// 直進して下へ
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (originPos.equals(nextPos)) {
					return true;
				}
				continuePosSet.add(nextPos);
				return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.UP);
			} else if (pos.getxIndex() != 0 && from == Direction.RIGHT
					&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS) {
				// 直進して左へ
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (originPos.equals(nextPos)) {
					return true;
				}
				continuePosSet.add(nextPos);
				return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.RIGHT);
			} else {
				// 直進可能性判定
				// 直進できる可能性が残っているときはfalseにする
				if (pos.getyIndex() != 0 && from == Direction.DOWN
						&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS) {
					return false;
				} else if (pos.getxIndex() != getXLength() - 1 && from == Direction.LEFT
						&& yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS) {
					return false;
				} else if (pos.getyIndex() != getYLength() - 1 && from == Direction.UP
						&& tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS) {
					return false;
				} else if (pos.getxIndex() != 0 && from == Direction.RIGHT
						&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS) {
					return false;
				} else {
					// 直進不可
					if (pos.getyIndex() != 0 && from != Direction.UP
							&& tateWall[pos.getyIndex() - 1][pos.getxIndex()] == Wall.NOT_EXISTS) {
						// カーブして上へ
						Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
						if (originPos.equals(nextPos)) {
							return true;
						}
						continuePosSet.add(nextPos);
						return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.DOWN);
					} else if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT
							&& yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
						// カーブして右へ
						Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
						if (originPos.equals(nextPos)) {
							return true;
						}
						continuePosSet.add(nextPos);
						return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.LEFT);
					} else if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN
							&& tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
						// カーブして下へ
						Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
						if (originPos.equals(nextPos)) {
							return true;
						}
						continuePosSet.add(nextPos);
						return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.UP);
					} else if (pos.getxIndex() != 0 && from != Direction.LEFT
							&& yokoWall[pos.getyIndex()][pos.getxIndex() - 1] == Wall.NOT_EXISTS) {
						// カーブして左へ
						Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
						if (originPos.equals(nextPos)) {
							return true;
						}
						continuePosSet.add(nextPos);
						return setContinuePosSet(originPos, nextPos, continuePosSet, Direction.RIGHT);
					}
					return false;
				}
			}
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
			if (!oddSolve()) {
				return false;
			}
			if (!connectSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
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

	public BarnsSolver(int height, int width, String param, boolean barns) {
		field = new Field(height, width, param, barns);
	}

	public BarnsSolver(Field field) {
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
		System.out.println(new BarnsSolver(height, width, param, true).solve());
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
		System.out.println("難易度:" + (count * 25));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 25 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 25).toString() + "(Lv:" + level + ")";
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}
}

package myamya.other.solver.patchwork;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class PatchworkSolver implements Solver {

	public static class PatchworkGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class PatchworkSolverForGenerator extends PatchworkSolver {
			private final int limit;

			public PatchworkSolverForGenerator(Field field, int limit) {
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

		public PatchworkGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new PatchworkGenerator(6, 6).generate();
		}

		@Override
		public GeneratorResult generate() {
			PatchworkSolver.Field wkField = new PatchworkSolver.Field(height, width);
			// 大きすぎる四角は禁止
			for (Iterator<PatchworkSikaku> iterator = wkField.squareCand.iterator(); iterator.hasNext();) {
				PatchworkSikaku patchworkSikaku = iterator.next();
				if (Math.sqrt(patchworkSikaku.posSet.size()) > (height / 2) + 1) {
					iterator.remove();
				}
			}
			List<PatchworkSikaku> sikakuCandBase = new ArrayList<>(wkField.squareCand);
			int sikakuIndex = 0;
			int masuIndex = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				List<Integer> sikakuIndexList = new ArrayList<>();
				List<PatchworkSikaku> useCand = new ArrayList<>(wkField.squareCand);
				for (int i = 0; i < useCand.size(); i++) {
					sikakuIndexList.add(i);
				}
				Collections.shuffle(sikakuIndexList);
				// 問題生成部
				// 部屋割り決め
				while (wkField.squareCand.size() != 0) {
					PatchworkSikaku oneCand = useCand.get(sikakuIndex);
					if (wkField.squareCand.contains(oneCand)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							PatchworkSolver.Field virtual = new PatchworkSolver.Field(wkField);
							if (masuNum < 1) {
								virtual.squareCand.remove(oneCand);
							} else if (masuNum < 2) {
								virtual.squareCand.remove(oneCand);
								virtual.squareFixed.add(oneCand);
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.squareCand = virtual.squareCand;
								wkField.squareFixed = virtual.squareFixed;
								break;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new PatchworkSolver.Field(height, width, sikakuCandBase);
							sikakuIndex = 0;
							masuIndex = 0;
							continue;
						}
					}
					sikakuIndex++;
				}
				// マス埋め
				List<Integer> masuIndexList = new ArrayList<>();
				for (int i = 0; i < height * width; i++) {
					masuIndexList.add(i);
				}
				// 数字マスは最初に決める、発生率いったん1/5
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (Math.random() * 5 < 1) {
							wkField.masu[yIndex][xIndex] = Masu.NOT_BLACK;
							wkField.numbers[yIndex][xIndex] = -1;
						}
					}
				}
				Collections.shuffle(masuIndexList);
				while (!wkField.isSolved()) {
					int yIndex = masuIndexList.get(masuIndex) / width;
					int xIndex = masuIndexList.get(masuIndex) % width;
					if (wkField.masu[yIndex][xIndex] == Masu.SPACE) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							PatchworkSolver.Field virtual = new PatchworkSolver.Field(wkField);
							if (masuNum < 1) {
								virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK;
								for (PatchworkSikaku sikaku : virtual.squareFixed) {
									if (sikaku.getPosSet().contains(new Position(yIndex, xIndex))) {
										if (sikaku.getInnerPosSet().contains(new Position(yIndex, xIndex))) {
											for (Position targetPos : sikaku.getInnerPosSet()) {
												if (wkField.numbers[targetPos.getyIndex()][targetPos
														.getxIndex()] == null) {
													virtual.masu[targetPos.getyIndex()][targetPos
															.getxIndex()] = Masu.NOT_BLACK;
												}
											}
										}
										break;
									}
								}
							} else if (masuNum < 2) {
								virtual.masu[yIndex][xIndex] = Masu.BLACK;
								for (PatchworkSikaku sikaku : virtual.squareFixed) {
									if (sikaku.getPosSet().contains(new Position(yIndex, xIndex))) {
										if (sikaku.getInnerPosSet().contains(new Position(yIndex, xIndex))) {
											for (Position targetPos : sikaku.getInnerPosSet()) {
												if (wkField.numbers[targetPos.getyIndex()][targetPos
														.getxIndex()] == null) {
													virtual.masu[targetPos.getyIndex()][targetPos
															.getxIndex()] = Masu.BLACK;
												}
											}
										}
										break;
									}
								}
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.masu = virtual.masu;
								break;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new PatchworkSolver.Field(height, width, sikakuCandBase);
							sikakuIndex = 0;
							masuIndex = 0;
							continue;
						}
					}
					masuIndex++;
				}
				// マスを埋める
				List<Position> numberPosList = new ArrayList<>();
				for (PatchworkSikaku fixed : wkField.squareFixed) {
					int blackCnt = 0;
					for (Position pos : fixed.getPosSet()) {
						if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
							blackCnt++;
						}
					}
					for (Position pos : fixed.getPosSet()) {
						if (wkField.masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK
								&& wkField.numbers[pos.getyIndex()][pos.getxIndex()] != null
								&& wkField.numbers[pos.getyIndex()][pos.getxIndex()] == -1) {
							wkField.numbers[pos.getyIndex()][pos.getxIndex()] = blackCnt;
							numberPosList.add(pos);
						}
					}
				}
				// System.out.println(wkField);
				// マスを戻す
				wkField.initCand();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new PatchworkSolverForGenerator(wkField, 200).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new PatchworkSolver.Field(height, width, sikakuCandBase);
					sikakuIndex = 0;
					masuIndex = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						PatchworkSolver.Field virtual = new PatchworkSolver.Field(wkField);
						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = -1;
						virtual.initCand();
						int solveResult = new PatchworkSolverForGenerator(virtual, 500).solve2();
						if (solveResult != -1) {
							wkField.numbers[numberPos.getyIndex()][numberPos
									.getxIndex()] = virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()];
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 2 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			StringBuilder sb = new StringBuilder();
//			int baseSize = 20;
//			int margin = 5;
//			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
//					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
//					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
//			// 数字描画
//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//					if (wkField.getNumbers()[yIndex][xIndex] != null) {
//						String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
//						int numIdx = HALF_NUMS.indexOf(numberStr);
//						String masuStr = null;
//						if (numIdx >= 0) {
//							masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
//						} else {
//							masuStr = numberStr;
//						}
//						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
//								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
//								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
//								+ masuStr + "</text>");
//
//					}
//				}
//			}
//			// 横壁描画
//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
//					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
//					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
//							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
//							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
//					if (oneYokoWall) {
//						sb.append("stroke=\"#000\" ");
//					} else {
//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
//					}
//					sb.append(">" + "</line>");
//				}
//			}
//			// 縦壁描画
//			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
//					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
//							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
//							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
//							+ "\" stroke-width=\"1\" fill=\"none\"");
//					if (oneTateWall) {
//						sb.append("stroke=\"#000\" ");
//					} else {
//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
//					}
//					sb.append(">" + "</line>");
//				}
//			}
//			sb.append("</svg>");
//			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
//			System.out.println(level);
//			System.out.println(wkField.getHintCount());
//			System.out.println(wkField);
//			System.out.println(url);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}

	}

	public static class PatchworkSikaku {
		private final Sikaku sikaku;
		private final Set<Position> posSet;
		private final Set<Position> innerPosSet;
		private final Set<Position> yokoWall;
		private final Set<Position> tateWall;
		private int number;
		private int capacity;

		PatchworkSikaku(Sikaku sikaku, Integer[][] numbers) {
			this.sikaku = sikaku;
			posSet = sikaku.getPosSet();
			innerPosSet = new HashSet<>();
			yokoWall = new HashSet<>();
			tateWall = new HashSet<>();
			number = -1;
			capacity = 0;
			for (Position pos : posSet) {
				boolean innerUp = pos.getyIndex() == 0
						|| posSet.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()));
				boolean innerRight = pos.getxIndex() == numbers[0].length - 1
						|| posSet.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1));
				boolean innerDown = pos.getyIndex() == numbers.length - 1
						|| posSet.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()));
				boolean innerLeft = pos.getxIndex() == 0
						|| posSet.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1));
				if (innerUp && innerRight && innerDown && innerLeft) {
					innerPosSet.add(pos);
				}
				if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
					if (number == -1) {
						number = numbers[pos.getyIndex()][pos.getxIndex()];
					} else if (numbers[pos.getyIndex()][pos.getxIndex()] != -1
							&& number != numbers[pos.getyIndex()][pos.getxIndex()]) {
						throw new IllegalArgumentException();
					}
				} else {
					capacity++;
				}
				if (!posSet.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
					tateWall.add(new Position(pos.getyIndex() - 1, pos.getxIndex()));
				}
				if (!posSet.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
					tateWall.add(new Position(pos.getyIndex(), pos.getxIndex()));
				}
				if (!posSet.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
					yokoWall.add(new Position(pos.getyIndex(), pos.getxIndex() - 1));
				}
				if (!posSet.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
					yokoWall.add(new Position(pos.getyIndex(), pos.getxIndex()));
				}
			}
		}

		// 自分が占めるマス一覧を返す
		public Set<Position> getPosSet() {
			return posSet;
		}

		// 自分が占めるマスのうち他の部屋に影響しないマス一覧を返す
		public Set<Position> getInnerPosSet() {
			return innerPosSet;
		}

		public int getNumber() {
			return number;
		}

		public int getCapacity() {
			return capacity;
		}

		@Override
		public String toString() {
			return sikaku.toString();
		}

		/**
		 * 自分が作ることになる横壁の位置を返す。外壁(-1、xlength)も含むので注意
		 */
		public Set<Position> getYokoWall() {
			return yokoWall;
		}

		/**
		 * 自分が作ることになる縦壁の位置を返す。外壁(-1、ylength)も含むので注意
		 */
		public Set<Position> getTateWall() {
			return tateWall;
		}

		public boolean isDuplicate(PatchworkSikaku fixed) {
			// TODO 自動生成されたメソッド・スタブ
			return sikaku.isDuplicate(fixed.getSikaku());
		}

		private Sikaku getSikaku() {
			return sikaku;
		}

	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		protected Integer[][] numbers;

		// 四角の配置の候補
		protected List<PatchworkSikaku> squareCand;
		// 確定した四角候補
		protected List<PatchworkSikaku> squareFixed;

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://puzz.link/p?patchwork/" + getXLength() + "/" + getYLength() + "/");
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
						numStr = Integer.toHexString(num);
						if (numStr.length() == 2) {
							numStr = "-" + numStr;
						} else if (numStr.length() == 3) {
							numStr = "+" + numStr;
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

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		/**
		 * 部屋のきりかたの候補を初期化する。
		 */
		protected void initCand() {
			squareCand = makeSquareCandBase(getYLength(), getXLength());
			squareFixed = new ArrayList<>();
		}

		/**
		 * 部屋のきりかたの候補を初期化する。 キャッシュを使って高速化。
		 */
		protected void initCand(List<PatchworkSikaku> squareCandBase) {
			squareCand = new ArrayList<>(squareCandBase);
			squareFixed = new ArrayList<>();
		}

		/**
		 * 部屋の切り方の候補を作成する。
		 */
		protected List<PatchworkSikaku> makeSquareCandBase(int height, int width) {
			List<PatchworkSikaku> squareCandBase = new ArrayList<>();
			for (int yIndex = 0; yIndex < height; yIndex++) {
				for (int xIndex = 0; xIndex < width; xIndex++) {
					for (int size = 1; size <= (width < height ? width : height); size++) {
						int maxY = yIndex + size > height ? height - size : yIndex;
						int maxX = xIndex + size > width ? width - size : xIndex;
						for (int y = yIndex; y <= maxY; y++) {
							for (int x = xIndex; x <= maxX; x++) {
								Sikaku wkSikaku = new Sikaku(new Position(y, x),
										new Position(y + size - 1, x + size - 1));
								try {
									PatchworkSikaku sikaku = new PatchworkSikaku(wkSikaku, numbers);
									squareCandBase.add(sikaku);
								} catch (IllegalArgumentException e) {
									//
								}
							}
						}
					}
				}
			}
			return squareCandBase;
		}

		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					// 16 - 255は '-'
					// 256 - 999は '+'
					int capacity;
					if (ch == '.') {
						Position pos = new Position(index / getXLength(), index % getXLength());
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
					} else {
						if (ch == '-') {
							capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							capacity = Integer
									.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3), 16);
							i++;
							i++;
							i++;
						} else {
							capacity = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
			initCand();
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			numbers = new Integer[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
				}
			}
			squareCand = new ArrayList<>(other.squareCand);
			squareFixed = new ArrayList<>(other.squareFixed);
		}

		public Field(int height, int width) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			initCand();
		}

		public Field(int height, int width, List<PatchworkSikaku> sikakuCandBase) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			initCand(sikakuCandBase);
		}

		/**
		 * 候補情報から横壁を復元する
		 */
		public Wall[][] getYokoWall() {
			Wall[][] yokoWall = new Wall[getYLength()][getXLength() - 1];
			Set<Position> fixedWallPosSet = new HashSet<>();
			Set<Position> candWallPosSet = new HashSet<>();
			for (PatchworkSikaku fixed : squareFixed) {
				fixedWallPosSet.addAll(fixed.getYokoWall());
			}
			for (PatchworkSikaku cand : squareCand) {
				candWallPosSet.addAll(cand.getYokoWall());
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (fixedWallPosSet.contains(pos)) {
						yokoWall[yIndex][xIndex] = Wall.EXISTS;
					} else if (candWallPosSet.contains(pos)) {
						yokoWall[yIndex][xIndex] = Wall.SPACE;
					} else {
						yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
					}
				}
			}
			return yokoWall;
		}

		/**
		 * 候補情報から横壁を復元する
		 */
		public Wall[][] getTateWall() {
			Wall[][] tateWall = new Wall[getYLength() - 1][getXLength()];
			Set<Position> fixedWallPosSet = new HashSet<>();
			Set<Position> candWallPosSet = new HashSet<>();
			for (PatchworkSikaku fixed : squareFixed) {
				fixedWallPosSet.addAll(fixed.getTateWall());
			}
			for (PatchworkSikaku cand : squareCand) {
				candWallPosSet.addAll(cand.getTateWall());
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (fixedWallPosSet.contains(pos)) {
						tateWall[yIndex][xIndex] = Wall.EXISTS;
					} else if (candWallPosSet.contains(pos)) {
						tateWall[yIndex][xIndex] = Wall.SPACE;
					} else {
						tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
					}
				}
			}
			return tateWall;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			Wall[][] yokoWall = getYokoWall();
			Wall[][] tateWall = getTateWall();
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] == -1) {
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
						sb.append(masu[yIndex][xIndex]);
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
			StringBuilder sb = new StringBuilder(squareFixed.size() + ":" + squareCand.size());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 確定した四角がある場合、それとかぶる・接する候補を消す。すでにかぶっていたらfalse。<br>
		 * 埋まらないマスが出来てしまった場合falseを返す。 逆に、あるマスを埋められる候補が1つしかない場合、確定形に昇格する。<br>
		 */
		private boolean sikakuSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// 既に確定しているマスを除外
					Position pos = new Position(yIndex, xIndex);
					boolean isFixed = false;
					for (PatchworkSikaku fixed : squareFixed) {
						if (fixed.getPosSet().contains(pos)) {
							isFixed = true;
							break;
						}
					}
					if (isFixed) {
						continue;
					}
					// そのマスに対する候補を検索
					PatchworkSikaku pickup = null;
					boolean only = true;
					for (PatchworkSikaku cand : squareCand) {
						if (cand.getPosSet().contains(pos)) {
							if (pickup == null) {
								pickup = cand;
							} else {
								only = false;
								break;
							}
						}
					}
					// 候補が1つもなければfalse
					if (pickup == null) {
						return false;
					}
					// 候補が1つに定まっていれば確定
					if (only) {
						squareCand.remove(pickup);
						squareFixed.add(pickup);
					}
				}
			}
			for (PatchworkSikaku fixed : squareFixed) {
				for (PatchworkSikaku otherFixed : squareFixed) {
					if (fixed != otherFixed) {
						if (otherFixed.isDuplicate(fixed)) {
							return false;
						}
					}
				}
				for (Iterator<PatchworkSikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
					PatchworkSikaku oneCand = iterator.next();
					if (oneCand.isDuplicate(fixed)) {
						iterator.remove();
					}
				}
			}
			return true;
		}

		/**
		 * 各種チェックを1セット実行
		 * 
		 * @param i
		 * @param recursive
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!sikakuSolve()) {
				return false;
			}
			if (!numberSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		// 四角の中の黒マスの個数を満たさない場合や国境をまたいだ同色マスはfalse
		private boolean numberSolve() {
			for (PatchworkSikaku fixed : squareFixed) {
				int blackCnt = 0;
				int whiteCnt = 0;
				for (Position pos : fixed.getPosSet()) {
					if (numbers[pos.getyIndex()][pos.getxIndex()] == null) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
							blackCnt++;
							Masu masuUp = fixed.getPosSet().contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))
									|| pos.getyIndex() == 0 || numbers[pos.getyIndex() - 1][pos.getxIndex()] != null
											? Masu.NOT_BLACK
											: masu[pos.getyIndex() - 1][pos.getxIndex()];
							if (masuUp == Masu.BLACK) {
								return false;
							}
							if (masuUp == Masu.SPACE) {
								masu[pos.getyIndex() - 1][pos.getxIndex()] = Masu.NOT_BLACK;
							}
							Masu masuRight = fixed.getPosSet()
									.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))
									|| pos.getxIndex() == getXLength() - 1
									|| numbers[pos.getyIndex()][pos.getxIndex() + 1] != null ? Masu.NOT_BLACK
											: masu[pos.getyIndex()][pos.getxIndex() + 1];
							if (masuRight == Masu.BLACK) {
								return false;
							}
							if (masuRight == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex() + 1] = Masu.NOT_BLACK;
							}
							Masu masuDown = fixed.getPosSet()
									.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))
									|| pos.getyIndex() == getYLength() - 1
									|| numbers[pos.getyIndex() + 1][pos.getxIndex()] != null ? Masu.NOT_BLACK
											: masu[pos.getyIndex() + 1][pos.getxIndex()];
							if (masuDown == Masu.BLACK) {
								return false;
							}
							if (masuDown == Masu.SPACE) {
								masu[pos.getyIndex() + 1][pos.getxIndex()] = Masu.NOT_BLACK;
							}
							Masu masuLeft = fixed.getPosSet()
									.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))
									|| pos.getxIndex() == 0 || numbers[pos.getyIndex()][pos.getxIndex() - 1] != null
											? Masu.NOT_BLACK
											: masu[pos.getyIndex()][pos.getxIndex() - 1];
							if (masuLeft == Masu.BLACK) {
								return false;
							}
							if (masuLeft == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex() - 1] = Masu.NOT_BLACK;
							}
						} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
							whiteCnt++;
							Masu masuUp = fixed.getPosSet().contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))
									|| pos.getyIndex() == 0 || numbers[pos.getyIndex() - 1][pos.getxIndex()] != null
											? Masu.BLACK
											: masu[pos.getyIndex() - 1][pos.getxIndex()];
							if (masuUp == Masu.NOT_BLACK) {
								return false;
							}
							if (masuUp == Masu.SPACE) {
								masu[pos.getyIndex() - 1][pos.getxIndex()] = Masu.BLACK;
							}
							Masu masuRight = fixed.getPosSet()
									.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))
									|| pos.getxIndex() == getXLength() - 1
									|| numbers[pos.getyIndex()][pos.getxIndex() + 1] != null ? Masu.BLACK
											: masu[pos.getyIndex()][pos.getxIndex() + 1];
							if (masuRight == Masu.NOT_BLACK) {
								return false;
							}
							if (masuRight == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex() + 1] = Masu.BLACK;
							}
							Masu masuDown = fixed.getPosSet()
									.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))
									|| pos.getyIndex() == getYLength() - 1
									|| numbers[pos.getyIndex() + 1][pos.getxIndex()] != null ? Masu.BLACK
											: masu[pos.getyIndex() + 1][pos.getxIndex()];
							if (masuDown == Masu.NOT_BLACK) {
								return false;
							}
							if (masuDown == Masu.SPACE) {
								masu[pos.getyIndex() + 1][pos.getxIndex()] = Masu.BLACK;
							}
							Masu masuLeft = fixed.getPosSet()
									.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))
									|| pos.getxIndex() == 0 || numbers[pos.getyIndex()][pos.getxIndex() - 1] != null
											? Masu.BLACK
											: masu[pos.getyIndex()][pos.getxIndex() - 1];
							if (masuLeft == Masu.NOT_BLACK) {
								return false;
							}
							if (masuLeft == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex() - 1] = Masu.BLACK;
							}
						}
					}
				}
				if (fixed.number != -1) {
					if (fixed.number < blackCnt) {
						// 黒マス過剰
						return false;
					}
					if (fixed.number == blackCnt) {
						for (Position pos : fixed.getPosSet()) {
							if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
							}
						}
					}

					if (fixed.number > fixed.getCapacity() - whiteCnt) {
						// 黒マス不足
						return false;
					}
					if (fixed.number == fixed.getCapacity() - whiteCnt) {
						for (Position pos : fixed.getPosSet()) {
							if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
								masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
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
					if (masu[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return squareCand.size() == 0 && solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public PatchworkSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public PatchworkSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = "https://puzz.link/p?patchwork/6/6/k1g111i101i111n2j1"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new PatchworkSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 2));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 2 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 2).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					count++;
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
						field.squareCand = virtual2.squareCand;
						field.squareFixed = virtual2.squareFixed;
					} else if (!allowNotBlack) {
						field.masu = virtual.masu;
						field.squareCand = virtual.squareCand;
						field.squareFixed = virtual.squareFixed;
					}
				}
			}
		}
		for (Iterator<PatchworkSikaku> iterator = field.squareCand.iterator(); iterator.hasNext();) {
			count++;
			PatchworkSikaku oneCand = iterator.next();
			Field virtual = new Field(field);
			virtual.squareCand.remove(oneCand);
			virtual.squareFixed.add(oneCand);
			boolean allowBlack = virtual.solveAndCheck();
			if (allowBlack && recursive > 0) {
				if (!candSolve(virtual, recursive - 1)) {
					allowBlack = false;
				}
			}
			Field virtual2 = new Field(field);
			virtual2.squareCand.remove(oneCand);
			boolean allowNotBlack = virtual2.solveAndCheck();
			if (allowNotBlack && recursive > 0) {
				if (!candSolve(virtual2, recursive - 1)) {
					allowNotBlack = false;
				}
			}
			if (!allowBlack && !allowNotBlack) {
				return false;
			} else if (!allowBlack) {
				field.squareCand = virtual2.squareCand;
				field.squareFixed = virtual2.squareFixed;
			} else if (!allowNotBlack) {
				field.squareCand = virtual.squareCand;
				field.squareFixed = virtual.squareFixed;
			}
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
		return true;
	}
}
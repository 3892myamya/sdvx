package myamya.other.solver.familyphoto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Solver;

public class FamilyphotoSolver implements Solver {
//	public static class FamilyphotoGenerator implements Generator {
//		static class FamilyphotoSolverForGenerator extends FamilyphotoSolver {
//			private final int limit;
//
//			public FamilyphotoSolverForGenerator(Field field, int limit) {
//				super(field);
//				this.limit = limit;
//			}
//
//			public int solve2() {
//				try {
//					while (!field.isSolved()) {
//						String befStr = field.getStateDump();
//						if (!field.solveAndCheck()) {
//							return -1;
//						}
//						int recursiveCnt = 0;
//						while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
//							if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
//								return -1;
//							}
//							recursiveCnt++;
//						}
//						if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
//							return -1;
//						}
//					}
//				} catch (CountOverException e) {
//					return -1;
//				}
//				return count;
//			}
//
//			@Override
//			protected boolean candSolve(Field field, int recursive) {
//				if (this.count >= limit) {
//					throw new CountOverException();
//				} else {
//					return super.candSolve(field, recursive);
//				}
//			}
//		}
//
//		private final int height;
//		private final int width;
//
//		public FamilyphotoGenerator(int height, int width) {
//			this.height = height;
//			this.width = width;
//		}
//
//		public static void main(String[] args) {
//			new FamilyphotoGenerator(4, 4).generate();
//		}
//
//		@Override
//		public GeneratorResult generate() {
//			FamilyphotoSolver.Field wkField = new FamilyphotoSolver.Field(height, width);
//			ArrayList<FamilyphotoSikaku> cacheCand = new ArrayList<>(wkField.squareCand);
//			int index = 0;
//			int level = 0;
//			long start = System.nanoTime();
//			while (true) {
//				List<Integer> indexList = new ArrayList<>();
//				for (int i = 0; i < cacheCand.size(); i++) {
//					indexList.add(i);
//				}
//				Collections.shuffle(indexList);
//				// 問題生成部
//				while (!wkField.isSolved()) {
//					FamilyphotoSikaku oneCand = cacheCand.get(index);
//					if (wkField.squareCand.contains(oneCand)) {
//						boolean isOk = false;
//						List<Integer> numIdxList = new ArrayList<>();
//						for (int i = 0; i < 2; i++) {
//							numIdxList.add(i);
//						}
//						Collections.shuffle(numIdxList);
//						for (int masuNum : numIdxList) {
//							FamilyphotoSolver.Field virtual = new FamilyphotoSolver.Field(wkField);
//							if (masuNum < 1) {
//								virtual.squareCand.remove(oneCand);
//							} else if (masuNum < 2) {
//								virtual.squareCand.remove(oneCand);
//								virtual.squareFixed.add(oneCand);
//							}
//							if (virtual.solveAndCheck()) {
//								isOk = true;
//								wkField.squareCand = virtual.squareCand;
//								wkField.squareFixed = virtual.squareFixed;
//								break;
//							}
//						}
//						if (!isOk) {
//							// 破綻したら0から作り直す。
//							wkField = new FamilyphotoSolver.Field(height, width, cacheCand);
//							index = 0;
//							continue;
//						}
//					}
//					index++;
//				}
//				// 初期横壁
//				List<Entry<Position, Boolean>> hintPosEntryList = new ArrayList<>();
//				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
//						Position pos = new Position(yIndex, xIndex);
//						int type = 0;
//						for (FamilyphotoSikaku fixed : wkField.squareFixed) {
//							if (fixed.getNotYokoWall().contains(pos)) {
//								break;
//							} else if (fixed.getYokoWall().contains(pos)) {
//								if (type == 0) {
//									type = fixed.getType();
//								} else {
//									if (type + fixed.getType() == 5) {
//										wkField.firstYokoWall.put(pos, 2);
//									} else if (type == fixed.getType()) {
//										wkField.firstYokoWall.put(pos, 4);
//									} else {
//										wkField.firstYokoWall.put(pos, 3);
//									}
//									hintPosEntryList.add(new SimpleEntry<>(pos, true));
//									hintPosEntryList.add(new SimpleEntry<>(pos, true));
//									break;
//								}
//							}
//						}
//					}
//				}
//				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
//					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//						Position pos = new Position(yIndex, xIndex);
//						int type = 0;
//						for (FamilyphotoSikaku fixed : wkField.squareFixed) {
//							if (fixed.getNotTateWall().contains(pos)) {
//								break;
//							} else if (fixed.getTateWall().contains(pos)) {
//								if (type == 0) {
//									type = fixed.getType();
//								} else {
//									if (type + fixed.getType() == 5) {
//										wkField.firstTateWall.put(pos, 2);
//									} else if (type == fixed.getType()) {
//										wkField.firstTateWall.put(pos, 4);
//									} else {
//										wkField.firstTateWall.put(pos, 3);
//									}
//									hintPosEntryList.add(new SimpleEntry<>(pos, false));
//									hintPosEntryList.add(new SimpleEntry<>(pos, false));
//									break;
//								}
//							}
//						}
//					}
//				}
//				// 候補を戻す
//				wkField.initCand();
//				// 解けるかな？
//				level = new FamilyphotoSolverForGenerator(wkField, 100).solve2();
//				if (level == -1) {
//					// 解けなければやり直し
//					wkField = new FamilyphotoSolver.Field(height, width, cacheCand);
//					index = 0;
//				} else {
//					// ヒントを限界まで減らす
//					Collections.shuffle(hintPosEntryList);
//					for (Entry<Position, Boolean> hintPosEntry : hintPosEntryList) {
//						FamilyphotoSolver.Field virtual = new FamilyphotoSolver.Field(wkField);
//						if (hintPosEntry.getValue()) {
//							if (virtual.firstYokoWall.get(hintPosEntry.getKey()) == 1) {
//								virtual.firstYokoWall.remove(hintPosEntry.getKey());
//							} else {
//								virtual.firstYokoWall.put(hintPosEntry.getKey(), 1);
//							}
//						} else {
//							if (virtual.firstTateWall.get(hintPosEntry.getKey()) == 1) {
//								virtual.firstTateWall.remove(hintPosEntry.getKey());
//							} else {
//								virtual.firstTateWall.put(hintPosEntry.getKey(), 1);
//							}
//						}
//						virtual.initCand();
//						int solveResult = new FamilyphotoSolverForGenerator(virtual, 500).solve2();
//						if (solveResult != -1) {
//							if (hintPosEntry.getValue()) {
//								if (wkField.firstYokoWall.get(hintPosEntry.getKey()) == 1) {
//									wkField.firstYokoWall.remove(hintPosEntry.getKey());
//								} else {
//									wkField.firstYokoWall.put(hintPosEntry.getKey(), 1);
//								}
//							} else {
//								if (wkField.firstTateWall.get(hintPosEntry.getKey()) == 1) {
//									wkField.firstTateWall.remove(hintPosEntry.getKey());
//								} else {
//									wkField.firstTateWall.put(hintPosEntry.getKey(), 1);
//								}
//							}
//							level = solveResult;
//						}
//					}
//					break;
//				}
//			}
//			level = (int) Math.sqrt(level * 2 / 3);
//			String status = "Lv:" + level + "の問題を獲得！(壁(白/灰/黒)：" + wkField.getHintCount() + ")";
//			String url = wkField.getPuzPreURL();
//			String link = "<a href=\"" + url + "\" target=\"_blank\">pzprxsで解く</a>";
//			StringBuilder sb = new StringBuilder();
//			int baseSize = 20;
//			int margin = 5;
//			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
//					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
//					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
//			// 横壁描画
//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
//					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1
//							|| wkField.getFirstYokoWall().get(new Position(yIndex, xIndex)) != null;
//					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
//							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
//							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
//					if (oneYokoWall) {
//						if (xIndex != -1 && xIndex != wkField.getXLength() - 1) {
//							sb.append("stroke=\"#000\" ");
//						} else {
//							sb.append("stroke=\"#000\" ");
//						}
//					} else {
//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
//					}
//					sb.append(">" + "</line>");
//					if (wkField.getFirstYokoWall().get(new Position(yIndex, xIndex)) != null) {
//						if (wkField.getFirstYokoWall().get(new Position(yIndex, xIndex)) == 2) {
//							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
//									+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 2
//									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
//						} else if (wkField.getFirstYokoWall().get(new Position(yIndex, xIndex)) == 3) {
//							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
//									+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 2
//									+ "\" fill=\"gray\", stroke=\"black\">" + "</circle>");
//						} else if (wkField.getFirstYokoWall().get(new Position(yIndex, xIndex)) == 4) {
//							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
//									+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 2
//									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
//						}
//					}
//				}
//			}
//			// 縦壁描画
//			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1
//							|| wkField.getFirstTateWall().get(new Position(yIndex, xIndex)) != null;
//					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
//							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
//							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
//							+ "\" stroke-width=\"1\" fill=\"none\"");
//					if (oneTateWall) {
//						if (yIndex != -1 && yIndex != wkField.getYLength() - 1) {
//							sb.append("stroke=\"#000\" ");
//						} else {
//							sb.append("stroke=\"#000\" ");
//						}
//					} else {
//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
//					}
//					sb.append(">" + "</line>");
//					if (wkField.getFirstTateWall().get(new Position(yIndex, xIndex)) != null) {
//						if (wkField.getFirstTateWall().get(new Position(yIndex, xIndex)) == 2) {
//							sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
//									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 2
//									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
//						} else if (wkField.getFirstTateWall().get(new Position(yIndex, xIndex)) == 3) {
//							sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
//									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 2
//									+ "\" fill=\"gray\", stroke=\"black\">" + "</circle>");
//						} else if (wkField.getFirstTateWall().get(new Position(yIndex, xIndex)) == 4) {
//							sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
//									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 2
//									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
//						}
//					}
//				}
//			}
//			sb.append("</svg>");
//			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
//			System.out.println(level);
//			System.out.println(wkField.getHintCount());
//			System.out.println(wkField);
//			System.out.println(url);
//			return new GeneratorResult(status, sb.toString(), link, url, level, "");
//
//		}
//
//	}

	// 2-3マスの集合体
	public static class FamilyphotoSikaku {
		private final Sikaku sikaku;
		// 以下はその都度算出すると遅くなるのでコンストラクタ時点で覚えさせる
		private final List<Position> posList;
		private final Set<Position> yokoWall;
		private final Set<Position> tateWall;

		FamilyphotoSikaku(Sikaku sikaku) {
			this.sikaku = sikaku;
			posList = new ArrayList<>(sikaku.getPosSet());
			yokoWall = new HashSet<>();
			for (int y = sikaku.getLeftUp().getyIndex(); y <= sikaku.getRightDown().getyIndex(); y++) {
				yokoWall.add(new Position(y, sikaku.getLeftUp().getxIndex() - 1));
				yokoWall.add(new Position(y, sikaku.getRightDown().getxIndex()));
			}
			tateWall = new HashSet<>();
			for (int x = sikaku.getLeftUp().getxIndex(); x <= sikaku.getRightDown().getxIndex(); x++) {
				tateWall.add(new Position(sikaku.getLeftUp().getyIndex() - 1, x));
				tateWall.add(new Position(sikaku.getRightDown().getyIndex(), x));
			}
		}

		// 自分が占めるマス一覧を返す
		public List<Position> getPosList() {
			return posList;
		}

		// 重複を判断して返す
		public boolean isDuplicate(FamilyphotoSikaku other) {
			return sikaku.isDuplicate(other.sikaku);
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

		@Override
		public String toString() {
			return sikaku.toString();
		}

	}

	public static class Field {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";
		protected boolean[][] family;
		protected Integer[][] numbers;
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 四角の配置の候補
		protected List<FamilyphotoSikaku> squareCand;
		// 確定した四角候補
		protected List<FamilyphotoSikaku> squareFixed;

//		public String getPuzPreURL() {
//			StringBuilder sb = new StringBuilder();
//			sb.append("https://puzz.link/p?lapaz/" + getXLength() + "/" + getYLength() + "/");
//			int interval = 0;
//			for (int i = 0; i < getYLength() * getXLength(); i++) {
//				int yIndex = i / getXLength();
//				int xIndex = i % getXLength();
//				if (numbers[yIndex][xIndex] == null) {
//					interval++;
//					if (interval == 20) {
//						sb.append("z");
//						interval = 0;
//					}
//				} else {
//					Integer num = numbers[yIndex][xIndex];
//					String numStr = Integer.toHexString(num);
//					if (numStr.length() == 2) {
//						numStr = "-" + numStr;
//					} else if (numStr.length() == 3) {
//						numStr = "+" + numStr;
//					}
//					if (interval == 0) {
//						sb.append(numStr);
//					} else {
//						sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
//						sb.append(numStr);
//						interval = 0;
//					}
//				}
//			}
//			if (interval != 0) {
//				sb.append(ALPHABET_FROM_G.substring(interval - 1, interval));
//			}
//			if (sb.charAt(sb.length() - 1) == '.') {
//				sb.append("/");
//			}
//			return sb.toString();
//		}
//
//		public String getHintCount() {
//			int numberCnt = 0;
//			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
//				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
//					if (numbers[yIndex][xIndex] != null) {
//						numberCnt++;
//					}
//				}
//			}
//			return String.valueOf(numberCnt);
//		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		/**
		 * プレーンなフィールド作成
		 */
		public Field(int height, int width) {
			numbers = new Integer[height][width];
			initCand();
		}

		public Field(int height, int width, ArrayList<FamilyphotoSikaku> cacheCand) {
			numbers = new Integer[height][width];
			squareCand = new ArrayList<>(cacheCand);
			squareFixed = new ArrayList<>();
		}

		/**
		 * 部屋のきりかたの候補を初期化する。
		 */
		protected void initCand() {
			squareCand = makeSquareCandBase();
			squareFixed = new ArrayList<>();
		}

		/**
		 * 部屋の切り方の候補を作成する。
		 */
		protected List<FamilyphotoSikaku> makeSquareCandBase() {
			List<FamilyphotoSikaku> squareCandBase = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					for (int ySize = 1; ySize <= getYLength() - yIndex; ySize++) {
						for (int xSize = 1; xSize <= getXLength() - xIndex; xSize++) {
							int minY = yIndex;
							int maxY = yIndex + ySize - 1;
							int minX = xIndex;
							int maxX = xIndex + xSize - 1;
							FamilyphotoSikaku sikaku = new FamilyphotoSikaku(
									new Sikaku(new Position(minY, minX), new Position(maxY, maxX)));
							// sikakuの中には数字が1個だけ入る。
							int number = -2;
							int familyCnt = 0;
							for (Position pos : sikaku.posList) {
								if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
									if (number == -2) {
										number = numbers[pos.getyIndex()][pos.getxIndex()];
									} else {
										number = -2;
										break;
									}
								}
								if (family[pos.getyIndex()][pos.getxIndex()]) {
									familyCnt++;
								}
							}
							if (number == -2) {
								continue;
							}
							// 数字の数と黒丸の数は等しくなる。
							if (number != -1 && number != familyCnt) {
								continue;
							}
							// 黒丸が隣り合っている場合壁は作れない。
							boolean familyDivide = false;
							for (Position tateWall : sikaku.getTateWall()) {
								if (tateWall.getyIndex() == -1 || tateWall.getyIndex() == getYLength() - 1) {
									continue;
								}
								if (family[tateWall.getyIndex()][tateWall.getxIndex()]
										&& family[tateWall.getyIndex() + 1][tateWall.getxIndex()]) {
									familyDivide = true;
									break;
								}
							}
							if (familyDivide) {
								continue;
							}
							for (Position yokoWall : sikaku.getYokoWall()) {
								if (yokoWall.getxIndex() == -1 || yokoWall.getxIndex() == getXLength() - 1) {
									continue;
								}
								if (family[yokoWall.getyIndex()][yokoWall.getxIndex()]
										&& family[yokoWall.getyIndex()][yokoWall.getxIndex() + 1]) {
									familyDivide = true;
									break;
								}
							}
							if (familyDivide) {
								continue;
							}
							squareCandBase.add(sikaku);
						}
					}
				}
			}
			return squareCandBase;
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			family = new boolean[height][width];
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
						family[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16 % 2 == 1;
					}
					if (mod >= 1) {
						family[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1;
					}
					if (mod >= 2) {
						family[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1;
					}
					if (mod >= 3) {
						family[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1;
					}
					if (mod >= 4) {
						family[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1;
					}
				}
			}
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
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
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
			initCand();
		}

		public Field(Field other) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			family = new boolean[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
					family[yIndex][xIndex] = other.family[yIndex][xIndex];
				}
			}
			squareCand = new ArrayList<>(other.squareCand);
			squareFixed = new ArrayList<>(other.squareFixed);
		}

		/**
		 * 候補情報から横壁を復元する
		 */
		public Wall[][] getYokoWall() {
			Wall[][] yokoWall = new Wall[getYLength()][getXLength() - 1];
			Set<Position> fixedWallPosSet = new HashSet<>();
			Set<Position> candWallPosSet = new HashSet<>();
			for (FamilyphotoSikaku fixed : squareFixed) {
				fixedWallPosSet.addAll(fixed.getYokoWall());
			}
			for (FamilyphotoSikaku cand : squareCand) {
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
			for (FamilyphotoSikaku fixed : squareFixed) {
				fixedWallPosSet.addAll(fixed.getTateWall());
			}
			for (FamilyphotoSikaku cand : squareCand) {
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

		public boolean[][] getFamily() {
			return family;
		}

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
					if (family[yIndex][xIndex]) {
						sb.append("●");
					} else if (numbers[yIndex][xIndex] != null) {
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
			sb.append(squareFixed.size() + ":" + squareCand.size());
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!sikakuSolve()) {
				return false;
			}
			if (!countSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		/**
		 * 確定した四角がある場合、それとかぶる・接する候補を消す。すでにかぶっていたらfalse。
		 */
		private boolean sikakuSolve() {
			for (FamilyphotoSikaku fixed : squareFixed) {
				for (FamilyphotoSikaku otherFixed : squareFixed) {
					if (fixed != otherFixed) {
						if (otherFixed.isDuplicate(fixed)) {
							return false;
						}
					}
				}
				for (Iterator<FamilyphotoSikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
					FamilyphotoSikaku oneCand = iterator.next();
					if (oneCand.isDuplicate(fixed)) {
						iterator.remove();
					}
				}
			}
			return true;
		}

		/**
		 * 埋まらないマスが出来てしまった場合falseを返す。 逆に、あるマスを埋められる候補が1つしかない場合、確定形に昇格する。
		 */
		private boolean countSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// 既に確定しているマスを除外
					Position pos = new Position(yIndex, xIndex);
					boolean isFixed = false;
					for (FamilyphotoSikaku fixed : squareFixed) {
						if (fixed.getPosList().contains(pos)) {
							isFixed = true;
							break;
						}
					}
					if (isFixed) {
						continue;
					}
					// そのマスに対する候補を検索
					FamilyphotoSikaku pickup = null;
					boolean only = true;
					for (FamilyphotoSikaku cand : squareCand) {
						if (cand.getPosList().contains(pos)) {
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
			return true;
		}

		public boolean isSolved() {
			return squareCand.size() == 0 && solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public FamilyphotoSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public FamilyphotoSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	//
	public static void main(String[] args) {
		String url = ""; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new FamilyphotoSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 10));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 10 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 10).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (Iterator<FamilyphotoSikaku> iterator = field.squareCand.iterator(); iterator.hasNext();) {
			count++;
			FamilyphotoSikaku oneCand = iterator.next();
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

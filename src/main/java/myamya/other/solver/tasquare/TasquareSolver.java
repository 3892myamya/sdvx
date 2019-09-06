package myamya.other.solver.tasquare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class TasquareSolver implements Solver {

	public static class TasquareGenerator implements Generator {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class TasquareSolverForGenerator extends TasquareSolver {
			private final int limit;

			public TasquareSolverForGenerator(Field field, int limit) {
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
						if (!candSolve(field, recursiveCnt)) {
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

		public TasquareGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new TasquareGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			TasquareSolver.Field wkField = new TasquareSolver.Field(height, width);
			List<Integer> indexList = new ArrayList<>();
			List<Sikaku> useCand = new ArrayList<>(wkField.squareCand);
			for (int i = 0; i < useCand.size(); i++) {
				indexList.add(i);
			}
			Collections.shuffle(indexList);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				while (!wkField.isSolved()) {
					Sikaku oneCand = useCand.get(index);
					if (wkField.squareCand.contains(oneCand)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							TasquareSolver.Field virtual = new TasquareSolver.Field(wkField, true);
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
							wkField = new TasquareSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// まず数字を埋める
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						Position pos = new Position(yIndex, xIndex);
						int cnt = 0;
						for (Sikaku fixed : wkField.squareFixed) {
							if (fixed.isDuplicate(pos)) {
								cnt = 0;
								break;
							} else if (fixed.isDuplicate(new Position(yIndex - 1, xIndex)) ||
									fixed.isDuplicate(new Position(yIndex + 1, xIndex)) ||
									fixed.isDuplicate(new Position(yIndex, xIndex - 1)) ||
									fixed.isDuplicate(new Position(yIndex, xIndex + 1))) {
								cnt = cnt + fixed.getAreaSize();
							}
						}
						if (cnt == 0) {
							continue;
						} else {
							wkField.numbers[yIndex][xIndex] = cnt;
							// 2段階減らし(□表出→表出なし)に対応するため二重にする
							numberPosList.add(pos);
							numberPosList.add(pos);
						}
					}
				}
				// マスを戻す
				wkField.initCand();
				// 解けるかな？
				level = new TasquareSolverForGenerator(wkField, 200).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new TasquareSolver.Field(height, width);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					wkField.initCand();
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						TasquareSolver.Field virtual = new TasquareSolver.Field(wkField, true);
						if (virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] != -1) {
							virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = -1;
						} else {
							virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							virtual.initCand();
						}
						int solveResult = new TasquareSolverForGenerator(virtual, 2500).solve2();
						if (solveResult != -1) {
							wkField.numbers[numberPos.getyIndex()][numberPos
									.getxIndex()] = virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()];
							level = solveResult;
							if (wkField.numbers[numberPos.getyIndex()][numberPos
									.getxIndex()] == null) {
								wkField.initCand();
							}
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 2 / 3);
			String status = "Lv:" + level + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			//			int baseSize = 20;
			//			int margin = 5;
			//			sb.append(
			//					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
			//							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
			//							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			//			// 横壁描画
			//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
			//				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
			//					sb.append("<line y1=\""
			//							+ (yIndex * baseSize + baseSize / 2 + margin)
			//							+ "\" x1=\""
			//							+ (xIndex * baseSize + baseSize / 2 + 2 * baseSize)
			//							+ "\" y2=\""
			//							+ (yIndex * baseSize + baseSize / 2 + baseSize + margin)
			//							+ "\" x2=\""
			//							+ (xIndex * baseSize + baseSize / 2 + 2 * baseSize)
			//							+ "\" stroke-width=\"1\" fill=\"none\"");
			//					sb.append("stroke=\"#000\" ");
			//					sb.append(">"
			//							+ "</line>");
			//				}
			//			}
			//			// 縦壁描画
			//			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
			//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
			//					sb.append("<line y1=\""
			//							+ (yIndex * baseSize + baseSize + baseSize / 2 + margin)
			//							+ "\" x1=\""
			//							+ (xIndex * baseSize + baseSize + baseSize / 2)
			//							+ "\" y2=\""
			//							+ (yIndex * baseSize + baseSize + baseSize / 2 + margin)
			//							+ "\" x2=\""
			//							+ (xIndex * baseSize + baseSize + baseSize + baseSize / 2)
			//							+ "\" stroke-width=\"1\" fill=\"none\"");
			//					sb.append("stroke=\"#000\" ");
			//					sb.append(">"
			//							+ "</line>");
			//				}
			//			}
			//			// 数字描画
			//			for (int yIndex = 0; yIndex < wkField.getYLength() + 1; yIndex++) {
			//				for (int xIndex = 0; xIndex < wkField.getXLength() + 1; xIndex++) {
			//					Integer number = wkField.getExtraNumbers()[yIndex][xIndex];
			//					if (number != null) {
			//						String numberStr = String.valueOf(number);
			//						int numIdx = HALF_NUMS.indexOf(numberStr);
			//						String masuStr = null;
			//						if (numIdx >= 0) {
			//							masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
			//						} else {
			//							masuStr = numberStr;
			//						}
			//						sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
			//								+ "\" cx=\""
			//								+ (xIndex * baseSize + baseSize + (baseSize / 2))
			//								+ "\" r=\""
			//								+ (baseSize / 2 - 3)
			//								+ "\" fill=\"white\", stroke=\"black\">"
			//								+ "</circle>");
			//						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 5)
			//								+ "\" x=\""
			//								+ (xIndex * baseSize + baseSize + 3)
			//								+ "\" font-size=\""
			//								+ (baseSize - 6)
			//								+ "\" textLength=\""
			//								+ (baseSize - 6)
			//								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
			//								+ masuStr
			//								+ "</text>");
			//					}
			//				}
			//			}
			//
			//			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 数字の情報
		private final Integer[][] numbers;
		// 四角の配置の候補
		protected List<Sikaku> squareCand;
		// 確定した四角候補
		protected List<Sikaku> squareFixed;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public String getPuzPreURL() {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		public String getHintCount() {
			// TODO 自動生成されたメソッド・スタブ
			return null;
		}

		public int getYLength() {
			return numbers.length;
		}

		public int getXLength() {
			return numbers[0].length;
		}

		public Field(int height, int width) {
			numbers = new Integer[height][width];
			initCand();
		}

		/**
		 * 部屋のきりかたの候補を初期化する。
		 */
		protected void initCand() {
			squareCand = new ArrayList<>();
			squareFixed = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					for (int size = 1; size <= (getXLength() < getYLength() ? getXLength() : getYLength()); size++) {
						int maxY = yIndex + size > getYLength() ? getYLength() - size : yIndex;
						int maxX = xIndex + size > getXLength() ? getXLength() - size : xIndex;
						for (int y = yIndex; y <= maxY; y++) {
							for (int x = xIndex; x <= maxX; x++) {
								Sikaku sikaku = new Sikaku(new Position(y, x),
										new Position(y + size - 1, x + size - 1));
								boolean addSikaku = true;
								// 他の部屋のpivotが含まれる候補をあらかじめ除外する。
								outer: for (int otherY = 0; otherY < getYLength(); otherY++) {
									for (int otherX = 0; otherX < getXLength(); otherX++) {
										if (numbers[otherY][otherX] != null) {
											Position otherPos = new Position(otherY, otherX);
											if (sikaku.isDuplicate(otherPos)) {
												addSikaku = false;
												break outer;
											}
										}
									}
								}
								if (addSikaku) {
									// 数字に隣接しており、明らかに候補にならない数字は消す
									Sikaku removeSikaku1 = new Sikaku(
											new Position(sikaku.getLeftUp().getyIndex() - 1,
													sikaku.getLeftUp().getxIndex()),
											new Position(sikaku.getRightDown().getyIndex() + 1,
													sikaku.getRightDown().getxIndex()));
									Sikaku removeSikaku2 = new Sikaku(
											new Position(sikaku.getLeftUp().getyIndex(),
													sikaku.getLeftUp().getxIndex() - 1),
											new Position(sikaku.getRightDown().getyIndex(),
													sikaku.getRightDown().getxIndex() + 1));
									outer: for (int otherY = 0; otherY < getYLength(); otherY++) {
										for (int otherX = 0; otherX < getXLength(); otherX++) {
											if (numbers[otherY][otherX] != null && numbers[otherY][otherX] != -1) {
												Position otherPos = new Position(otherY, otherX);
												if (removeSikaku1.isDuplicate(otherPos)
														|| removeSikaku2.isDuplicate(otherPos)) {
													if (sikaku.getAreaSize() > numbers[otherY][otherX]) {
														addSikaku = false;
														break outer;
													}
												}
											}
										}
									}
									if (addSikaku) {
										squareCand.add(sikaku);
									}
								}
							}
						}
					}
				}
			}
		}

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
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
						numbers[pos.getyIndex()][pos.getxIndex()] = -1;
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
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
			initCand();
		}

		public Field(Field other) {
			numbers = other.numbers;
			squareCand = new ArrayList<>(other.squareCand);
			squareFixed = new ArrayList<>(other.squareFixed);
		}

		public Field(Field other, boolean flag) {
			numbers = new Integer[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
				}
			}
			squareCand = new ArrayList<>(other.squareCand);
			squareFixed = new ArrayList<>(other.squareFixed);
		}

		/**
		 * 候補情報からマスを復元する
		 */
		public Masu[][] getMasu() {
			Masu[][] masu = new Masu[getYLength()][getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.NOT_BLACK;
				}
			}
			for (Sikaku fixed : squareFixed) {
				for (int yIndex = fixed.getLeftUp().getyIndex(); yIndex <= fixed.getRightDown().getyIndex(); yIndex++) {
					for (int xIndex = fixed.getLeftUp().getxIndex(); xIndex <= fixed.getRightDown()
							.getxIndex(); xIndex++) {
						masu[yIndex][xIndex] = Masu.BLACK;
					}
				}
			}
			for (Sikaku cand : squareCand) {
				for (int yIndex = cand.getLeftUp().getyIndex(); yIndex <= cand.getRightDown().getyIndex(); yIndex++) {
					for (int xIndex = cand.getLeftUp().getxIndex(); xIndex <= cand.getRightDown()
							.getxIndex(); xIndex++) {
						masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
			}
			return masu;
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			Masu[][] masu = getMasu();
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
						} else if (numbers[yIndex][xIndex] == -1) {
							sb.append("□");
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
				}
				sb.append(System.lineSeparator());
			}
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
		private boolean solveAndCheck() {
			if (!sikakuSolve()) {
				return false;
			}
			if (!countSolve()) {
				return false;
			}
			if (!connectSolve()) {
				return false;
			}
			return true;
		}

		/**
		 * 確定した四角がある場合、それとかぶる・接する候補を消す。
		 */
		private boolean sikakuSolve() {
			for (Sikaku fixed : squareFixed) {
				Sikaku removeSikaku1 = new Sikaku(
						new Position(fixed.getLeftUp().getyIndex() - 1, fixed.getLeftUp().getxIndex()),
						new Position(fixed.getRightDown().getyIndex() + 1, fixed.getRightDown().getxIndex()));
				Sikaku removeSikaku2 = new Sikaku(
						new Position(fixed.getLeftUp().getyIndex(), fixed.getLeftUp().getxIndex() - 1),
						new Position(fixed.getRightDown().getyIndex(), fixed.getRightDown().getxIndex() + 1));
				for (Iterator<Sikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
					Sikaku sikaku = iterator.next();
					if (sikaku.isDuplicate(removeSikaku1) || sikaku.isDuplicate(removeSikaku2)) {
						iterator.remove();
					}
				}
			}
			return true;
		}

		/**
		 * 四角を配置して数字の条件を満たせるかを調査する。
		 * 満たせない場合falseを返す。
		 */
		private boolean countSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						int num = numbers[yIndex][xIndex];
						int fixCnt = 0;
						for (Sikaku fixed : squareFixed) {
							if (fixed.isDuplicate(new Position(yIndex - 1, xIndex)) ||
									fixed.isDuplicate(new Position(yIndex + 1, xIndex)) ||
									fixed.isDuplicate(new Position(yIndex, xIndex - 1)) ||
									fixed.isDuplicate(new Position(yIndex, xIndex + 1))) {
								fixCnt = fixCnt + fixed.getAreaSize();
							}
						}
						if (num != -1 && num < fixCnt) {
							return false;
						}
						if (num != -1 && num == fixCnt) {
							continue;
						}
						if (num == -1 && 0 < fixCnt) {
							continue;
						}
						if (num != -1) {
							Set<Integer> upCand = new HashSet<>();
							Set<Integer> rightCand = new HashSet<>();
							Set<Integer> downCand = new HashSet<>();
							Set<Integer> leftCand = new HashSet<>();
							upCand.add(0);
							rightCand.add(0);
							downCand.add(0);
							leftCand.add(0);
							boolean isOk = false;
							outer: for (Sikaku cand : squareCand) {
								int areaSize = cand.getAreaSize();
								if (areaSize + fixCnt <= num) {
									boolean upDated = false;
									if (cand.isDuplicate(new Position(yIndex - 1, xIndex))) {
										upDated = upCand.add(areaSize);
									} else if (cand.isDuplicate(new Position(yIndex, xIndex + 1))) {
										upDated = rightCand.add(areaSize);
									} else if (cand.isDuplicate(new Position(yIndex + 1, xIndex))) {
										upDated = downCand.add(areaSize);
									} else if (cand.isDuplicate(new Position(yIndex, xIndex - 1))) {
										upDated = leftCand.add(areaSize);
									}
									if (upDated) {
										if (!isOk) {
											for (Integer up : upCand) {
												for (Integer right : rightCand) {
													for (Integer down : downCand) {
														if (fixCnt + up + right + down == num) {
															isOk = true;
															break outer;
														}
													}
													for (Integer left : leftCand) {
														if (fixCnt + up + right + left == num) {
															isOk = true;
															break outer;
														}
													}
												}
											}
										}
										if (!isOk) {
											for (Integer down : downCand) {
												for (Integer left : leftCand) {
													for (Integer up : upCand) {
														if (fixCnt + up + left + down == num) {
															isOk = true;
															break outer;
														}
													}
													for (Integer right : rightCand) {
														if (fixCnt + down + right + left == num) {
															isOk = true;
															break outer;
														}
													}
												}
											}
										}
									}
								}
							}
							if (!isOk) {
								return false;
							}
						} else {
							boolean isOk = false;
							for (Sikaku cand : squareCand) {
								if (cand.isDuplicate(new Position(yIndex - 1, xIndex)) ||
										cand.isDuplicate(new Position(yIndex + 1, xIndex)) ||
										cand.isDuplicate(new Position(yIndex, xIndex - 1)) ||
										cand.isDuplicate(new Position(yIndex, xIndex + 1))) {
									isOk = true;
									break;
								}
							}
							if (!isOk) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 白マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Masu[][] masu = getMasu();
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.size() == 0) {
							whitePosSet.add(whitePos);
							setContinuePosSet(masu, whitePos, whitePosSet, null);
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
		private void setContinuePosSet(Masu[][] masu, Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(masu, nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(masu, nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(masu, nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(masu, nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		public boolean isSolved() {
			return squareCand.size() == 0 && solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public TasquareSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public TasquareSolver(Field field) {
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
		System.out.println(new TasquareSolver(height, width, param).solve());
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
				if (!candSolve(field, recursiveCnt)) {
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
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 2).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (Iterator<Sikaku> iterator = field.squareCand.iterator(); iterator
				.hasNext();) {
			count++;
			Sikaku oneCand = iterator.next();
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
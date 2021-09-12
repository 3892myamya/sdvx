package myamya.other.solver.chocobanana;

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
import myamya.other.solver.Common.PenpaEditGeneratorResult;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.PenpaEditLib;
import myamya.other.solver.Solver;

public class ChocobananaSolver implements Solver {
	public static class ChocobananaGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class ChocobananaSolverForGenerator extends ChocobananaSolver {

			private final int limit;

			public ChocobananaSolverForGenerator(Field field, int limit) {
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

		public ChocobananaGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new ChocobananaGenerator(6, 6).generate();
		}

		@Override
		public GeneratorResult generate() {
			String solutionStr;
			ChocobananaSolver.Field wkField = new ChocobananaSolver.Field(height, width);
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
						for (int i = 0; i < 3; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							ChocobananaSolver.Field virtual = new ChocobananaSolver.Field(wkField);
							if (masuNum < 1) {
								virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK;
							} else if (masuNum < 3) {
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
							wkField = new ChocobananaSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め
				List<Position> numberPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						numberPosList.add(new Position(yIndex, xIndex));
					}
				}
				wkField.setNumbers();
				// 解答の記憶
				solutionStr = PenpaEditLib.convertSolutionMasu(wkField.masu);
				System.out.println(wkField);
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new ChocobananaSolverForGenerator(new ChocobananaSolver.Field(wkField), 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ChocobananaSolver.Field(height, width);
					index = 0;
				} else {
					Collections.shuffle(numberPosList);
					for (Position numberPos : numberPosList) {
						ChocobananaSolver.Field virtual = new ChocobananaSolver.Field(wkField, true);
						if (virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] == null) {
							continue;
						} else {
							virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						}
						int solveResult = new ChocobananaSolverForGenerator(virtual, 10000).solve2();
						if (solveResult != -1) {
							wkField.masu[numberPos.getyIndex()][numberPos
									.getxIndex()] = virtual.masu[numberPos.getyIndex()][numberPos.getxIndex()];
							wkField.numbers[numberPos.getyIndex()][numberPos
									.getxIndex()] = virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()];
							level = solveResult;
						}
					}
					break;
				}
			}
			// ヒント数字を含む盤面変換
			String fieldStr = PenpaEditLib.convertNumbersField(wkField.numbers);
			System.out.println(fieldStr);
			System.out.println(solutionStr);

			level = (int) Math.sqrt(Math.pow(level, 0.65) / 3) + 1;
			String status = "Lv:" + (int) (Math.pow(level, 0.65)) + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">penpa-editで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
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
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5)
								+ "\" textLength=\"" + (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ masuStr + "</text>");
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
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.(chocobanana)");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new PenpaEditGeneratorResult(status, sb.toString(), link, level, "", fieldStr, solutionStr);
		}
	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private final Integer[][] numbers;
		// 確定済み数字の位置情報
		private final Set<Position> alreadyPosSet;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			return PenpaEditLib.PENPA_EDIT_DUMMY_URL;
//			StringBuilder sb = new StringBuilder();
//			sb.append("http://pzv.jp/p.html?chocobanana/" + getXLength() + "/" + getYLength() + "/");
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
//					String numStr;
//					if (num == -1) {
//						numStr = ".";
//					} else {
//						numStr = Integer.toHexString(num);
//					}
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

		/**
		 * ジェネレータ用
		 */
		public void setNumbers() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null) {
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pivot);
						setContinuePosSet(masu[yIndex][xIndex], pivot, continuePosSet, Integer.MAX_VALUE, null);
						for (Position pos : continuePosSet) {
							numbers[pos.getyIndex()][pos.getxIndex()] = continuePosSet.size();
						}
					}
				}
			}
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

		public Field(int height, int width) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			alreadyPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			alreadyPosSet = new HashSet<>(other.alreadyPosSet);
		}

		/**
		 * numbersをイミュータブルにするためのコンストラクタ。flagはダミー
		 */
		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			numbers = new Integer[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					numbers[yIndex][xIndex] = other.numbers[yIndex][xIndex];
				}
			}
			alreadyPosSet = new HashSet<>(other.alreadyPosSet);
		}

		public Field(String fieldStr) {
			masu = PenpaEditLib.getMasu(fieldStr);
			numbers = PenpaEditLib.getNumbers(fieldStr);
			alreadyPosSet = new HashSet<>();
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && masu[yIndex][xIndex] == Masu.SPACE) {
						if (numbers[yIndex][xIndex] > 99) {
							sb.append("99");
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!nextSolve()) {
				return false;
			}
			if (!countSolve()) {
				return false;
			}
			if (!rectBlackSolve()) {
				return false;
			}
			if (!notRectWhiteSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		// 違う数字が隣り合う場合は必ず違う色になる。
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (masu[yIndex][xIndex] != Masu.SPACE) {
							if (yIndex != 0) {
								if (numbers[yIndex - 1][xIndex] != null
										&& numbers[yIndex - 1][xIndex] != numbers[yIndex][xIndex]) {
									if (masu[yIndex - 1][xIndex] != Masu.SPACE) {
										if (masu[yIndex - 1][xIndex] == masu[yIndex][xIndex]) {
											return false;
										}
									} else {
										masu[yIndex - 1][xIndex] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.NOT_BLACK
												: Masu.BLACK;
									}
								}
							}
							if (xIndex != getXLength() - 1) {
								if (numbers[yIndex][xIndex + 1] != null
										&& numbers[yIndex][xIndex + 1] != numbers[yIndex][xIndex]) {
									if (masu[yIndex][xIndex + 1] != Masu.SPACE) {
										if (masu[yIndex][xIndex + 1] == masu[yIndex][xIndex]) {
											return false;
										}
									} else {
										masu[yIndex][xIndex + 1] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.NOT_BLACK
												: Masu.BLACK;
									}
								}
							}
							if (yIndex != getYLength() - 1) {
								if (numbers[yIndex + 1][xIndex] != null
										&& numbers[yIndex + 1][xIndex] != numbers[yIndex][xIndex]) {
									if (masu[yIndex + 1][xIndex] != Masu.SPACE) {
										if (masu[yIndex + 1][xIndex] == masu[yIndex][xIndex]) {
											return false;
										}
									} else {
										masu[yIndex + 1][xIndex] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.NOT_BLACK
												: Masu.BLACK;
									}
								}
							}
							if (xIndex != 0) {
								if (numbers[yIndex][xIndex - 1] != null
										&& numbers[yIndex][xIndex - 1] != numbers[yIndex][xIndex]) {
									if (masu[yIndex][xIndex - 1] != Masu.SPACE) {
										if (masu[yIndex][xIndex - 1] == masu[yIndex][xIndex]) {
											return false;
										}
									} else {
										masu[yIndex][xIndex - 1] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.NOT_BLACK
												: Masu.BLACK;
									}
								}
							}
						}

					}
				}
			}
			return true;
		}

		/**
		 * 数字に対する連結チェック。 超過や不足が確定したらfalseを返す。
		 */
		private boolean countSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							continue;
						}
						Position pivot = new Position(yIndex, xIndex);
						if (alreadyPosSet.contains(pivot)) {
							continue;
						}
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pivot);
						if (!(setContinuePosSet(masu[yIndex][xIndex], pivot, continuePosSet, numbers[yIndex][xIndex],
								null))) {
							// サイズ超過
							return false;
						} else if (numbers[yIndex][xIndex] == continuePosSet.size()) {
							// サイズ確定。周りを自分と違う色に
							alreadyPosSet.add(pivot);
							for (Position pos : continuePosSet) {
								if (pos.getyIndex() != 0 && !continuePosSet
										.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
									masu[pos.getyIndex() - 1][pos.getxIndex()] = masu[yIndex][xIndex] == Masu.BLACK
											? Masu.NOT_BLACK
											: Masu.BLACK;
								}
								if (pos.getxIndex() != getXLength() - 1 && !continuePosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
									masu[pos.getyIndex()][pos.getxIndex() + 1] = masu[yIndex][xIndex] == Masu.BLACK
											? Masu.NOT_BLACK
											: Masu.BLACK;
								}
								if (pos.getyIndex() != getYLength() - 1 && !continuePosSet
										.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
									masu[pos.getyIndex() + 1][pos.getxIndex()] = masu[yIndex][xIndex] == Masu.BLACK
											? Masu.NOT_BLACK
											: Masu.BLACK;
								}
								if (pos.getxIndex() != 0 && !continuePosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
									masu[pos.getyIndex()][pos.getxIndex() - 1] = masu[yIndex][xIndex] == Masu.BLACK
											? Masu.NOT_BLACK
											: Masu.BLACK;
								}
							}
						} else {
							Set<Position> continueCandPosSet = new HashSet<>();
							continueCandPosSet.add(pivot);
							if (!setContinueCandPosSet(masu[yIndex][xIndex], pivot, continueCandPosSet,
									numbers[yIndex][xIndex], null)) {
								if (numbers[yIndex][xIndex] == continueCandPosSet.size()) {
									// サイズ確定。確定マスを自分と同じ色、周りを自分と違う色に
									alreadyPosSet.add(pivot);
									for (Position pos : continueCandPosSet) {
										masu[pos.getyIndex()][pos.getxIndex()] = masu[yIndex][xIndex];
										if (pos.getyIndex() != 0 && !continueCandPosSet
												.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
											masu[pos.getyIndex() - 1][pos
													.getxIndex()] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.NOT_BLACK
															: Masu.BLACK;
										}
										if (pos.getxIndex() != getXLength() - 1 && !continueCandPosSet
												.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
											masu[pos.getyIndex()][pos.getxIndex()
													+ 1] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.NOT_BLACK
															: Masu.BLACK;
										}
										if (pos.getyIndex() != getYLength() - 1 && !continueCandPosSet
												.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
											masu[pos.getyIndex() + 1][pos
													.getxIndex()] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.NOT_BLACK
															: Masu.BLACK;
										}
										if (pos.getxIndex() != 0 && !continueCandPosSet
												.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
											masu[pos.getyIndex()][pos.getxIndex()
													- 1] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.NOT_BLACK
															: Masu.BLACK;
										}
									}
								} else {
									// サイズ不足
									return false;
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右にtargetと同じ色になりうるマスをつなげていく。 sizeが確保可能とわかった時点でtrueを返す。
		 */
		private boolean setContinueCandPosSet(Masu target, Position pos, Set<Position> continuePosSet, int size,
				Direction from) {
			if (continuePosSet.size() > size) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] == target
						|| masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)
						&& !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(target, nextPos, continuePosSet, size, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] == target
						|| masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)
						&& !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(target, nextPos, continuePosSet, size, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] == target
						|| masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)
						&& !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(target, nextPos, continuePosSet, size, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] == target
						|| masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)
						&& !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(target, nextPos, continuePosSet, size, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右にtargetと同じ色が確定しているマスをつなげていく。 sizeが超過すると分かった時点でfalseを返す。
		 */
		private boolean setContinuePosSet(Masu target, Position pos, Set<Position> continuePosSet, int size,
				Direction from) {
			if (continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == target && !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(target, nextPos, continuePosSet, size, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == target && !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(target, nextPos, continuePosSet, size, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == target && !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(target, nextPos, continuePosSet, size, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == target && !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(target, nextPos, continuePosSet, size, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスは長方形に。矛盾した場合はfalse。
		 */
		public boolean rectBlackSolve() {
			boolean advance = false;
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.NOT_BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.NOT_BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.NOT_BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.NOT_BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.SPACE) {
						advance = true;
						masu[yIndex + 1][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE && masu4 == Masu.BLACK) {
						advance = true;
						masu[yIndex + 1][xIndex] = Masu.BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						advance = true;
						masu[yIndex][xIndex + 1] = Masu.BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						advance = true;
						masu[yIndex][xIndex] = Masu.BLACK;
					}
				}
			}
			if (advance) {
				return rectBlackSolve();
			} else {
//				Set<Position> already = new HashSet<>();
//				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
//					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
//						// ふくらましきったら、前後左右がすべて止まっている場合は周囲を白確定にする。
//						if (masu[yIndex][xIndex] == Masu.BLACK && !already.contains(new Position(yIndex, xIndex))) {
//							// 上下左右探索
//							int minY = yIndex;
//							int minX = xIndex;
//							int maxY = yIndex;
//							int maxX = xIndex;
//							for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
//								if (masu[targetY][xIndex] != Masu.BLACK) {
//									break;
//								}
//								minY = targetY;
//							}
//							for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
//								if (masu[targetY][xIndex] != Masu.BLACK) {
//									break;
//								}
//								maxY = targetY;
//							}
//							for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
//								if (masu[yIndex][targetX] != Masu.BLACK) {
//									break;
//								}
//								minX = targetX;
//							}
//							for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
//								if (masu[yIndex][targetX] != Masu.BLACK) {
//									break;
//								}
//								maxX = targetX;
//							}
//							// 周囲探索
//							if (minY != 0) {
//								boolean findWall = false;
//								for (int targetX = minX; targetX <= maxX; targetX++) {
//									if (masu[minY - 1][targetX] == Masu.NOT_BLACK) {
//										findWall = true;
//										break;
//									}
//								}
//								if (findWall) {
//									for (int targetX = minX; targetX <= maxX; targetX++) {
//										masu[minY - 1][targetX] = Masu.NOT_BLACK;
//									}
//								}
//							}
//							if (maxX != getXLength() - 1) {
//								boolean findWall = false;
//								for (int targetY = minY; targetY <= maxY; targetY++) {
//									if (masu[targetY][maxX + 1] == Masu.NOT_BLACK) {
//										break;
//									}
//								}
//								if (findWall) {
//									for (int targetY = minY; targetY <= maxY; targetY++) {
//										masu[targetY][maxX + 1] = Masu.NOT_BLACK;
//									}
//								}
//							}
//							if (maxY != getYLength() - 1) {
//								boolean findWall = false;
//								for (int targetX = minX; targetX <= maxX; targetX++) {
//									if (masu[maxY + 1][targetX] == Masu.NOT_BLACK) {
//										break;
//									}
//								}
//								if (findWall) {
//									for (int targetX = minX; targetX <= maxX; targetX++) {
//										masu[maxY + 1][targetX] = Masu.NOT_BLACK;
//									}
//								}
//							}
//							if (minX != 0) {
//								boolean findWall = false;
//								for (int targetY = minY; targetY <= maxY; targetY++) {
//									if (masu[targetY][minX - 1] == Masu.NOT_BLACK) {
//										break;
//									}
//								}
//								if (findWall) {
//									for (int targetY = minY; targetY <= maxY; targetY++) {
//										masu[targetY][minX - 1] = Masu.NOT_BLACK;
//									}
//								}
//							}
//							for (int targetY = minY; targetY <= maxY; targetY++) {
//								for (int targetX = minX; targetX <= maxX; targetX++) {
//									already.add(new Position(targetY, targetX));
//								}
//							}
//						}
//					}
//				}
			}
			return true;
		}

		/**
		 * 白マスは長方形以外に。矛盾した場合はfalse。
		 */
		public boolean notRectWhiteSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					// 自身の上下左右の白確定マスが確定しており、取り囲むマスがすべて黒ならアウト
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						// 上下左右探索
						boolean isSkip = false;
						int minY = yIndex;
						int minX = xIndex;
						int maxY = yIndex;
						int maxX = xIndex;
						for (int targetY = yIndex - 1; targetY >= 0; targetY--) {
							if (masu[targetY][xIndex] != Masu.NOT_BLACK) {
								if (masu[targetY][xIndex] == Masu.SPACE) {
									isSkip = true;
								}
								break;
							}
							minY = targetY;
						}
						if (isSkip) {
							continue;
						}
						for (int targetY = yIndex + 1; targetY < getYLength(); targetY++) {
							if (masu[targetY][xIndex] != Masu.NOT_BLACK) {
								if (masu[targetY][xIndex] == Masu.SPACE) {
									isSkip = true;
								}
								break;
							}
							maxY = targetY;
						}
						if (isSkip) {
							continue;
						}
						for (int targetX = xIndex - 1; targetX >= 0; targetX--) {
							if (masu[yIndex][targetX] != Masu.NOT_BLACK) {
								if (masu[yIndex][targetX] == Masu.SPACE) {
									isSkip = true;
								}
								break;
							}
							minX = targetX;
						}
						if (isSkip) {
							continue;
						}
						for (int targetX = xIndex + 1; targetX < getXLength(); targetX++) {
							if (masu[yIndex][targetX] != Masu.NOT_BLACK) {
								if (masu[yIndex][targetX] == Masu.SPACE) {
									isSkip = true;
								}
								break;
							}
							maxX = targetX;
						}
						if (isSkip) {
							continue;
						}
						// 範囲内に白未確定マスがあればOK
						for (int targetY = minY; targetY < maxY; targetY++) {
							for (int targetX = minX; targetX < maxX; targetX++) {
								if (masu[targetY][targetX] != Masu.NOT_BLACK) {
									isSkip = true;
									break;
								}
							}
						}
						if (isSkip) {
							continue;
						}
						// 周囲探索
						if (minY != 0) {
							for (int targetX = minX; targetX <= maxX; targetX++) {
								if (masu[minY - 1][targetX] != Masu.BLACK) {
									isSkip = true;
									break;
								}
							}
							if (isSkip) {
								continue;
							}
						}
						if (maxX != getXLength() - 1) {
							for (int targetY = minY; targetY <= maxY; targetY++) {
								if (masu[targetY][maxX + 1] != Masu.BLACK) {
									isSkip = true;
									break;
								}
							}
							if (isSkip) {
								continue;
							}
						}
						if (maxY != getYLength() - 1) {
							for (int targetX = minX; targetX <= maxX; targetX++) {
								if (masu[maxY + 1][targetX] != Masu.BLACK) {
									isSkip = true;
									break;
								}
							}
							if (isSkip) {
								continue;
							}
						}
						if (minX != 0) {
							for (int targetY = minY; targetY <= maxY; targetY++) {
								if (masu[targetY][minX - 1] != Masu.BLACK) {
									isSkip = true;
									break;
								}
							}
							if (isSkip) {
								continue;
							}
						}
						return false;
//						Position oneSpace = null;
//						if (minY != 0) {
//							for (int targetX = minX; targetX <= maxX; targetX++) {
//								if (masu[minY - 1][targetX] == Masu.NOT_BLACK) {
//									isSkip = true;
//									break;
//								}
//								if (masu[minY - 1][targetX] == Masu.SPACE) {
//									if (oneSpace == null) {
//										oneSpace = new Position(minY - 1, targetX);
//									} else {
//										isSkip = true;
//										break;
//									}
//								}
//							}
//							if (isSkip) {
//								continue;
//							}
//						}
//						if (maxX != getXLength() - 1) {
//							for (int targetY = minY; targetY <= maxY; targetY++) {
//								if (masu[targetY][maxX + 1] == Masu.NOT_BLACK) {
//									isSkip = true;
//									break;
//								}
//								if (masu[targetY][maxX + 1] == Masu.SPACE) {
//									if (oneSpace == null) {
//										oneSpace = new Position(targetY, maxX + 1);
//									} else {
//										isSkip = true;
//										break;
//									}
//								}
//							}
//							if (isSkip) {
//								continue;
//							}
//						}
//						if (maxY != getYLength() - 1) {
//							for (int targetX = minX; targetX <= maxX; targetX++) {
//								if (masu[maxY + 1][targetX] == Masu.NOT_BLACK) {
//									isSkip = true;
//									break;
//								}
//								if (masu[maxY + 1][targetX] == Masu.SPACE) {
//									if (oneSpace == null) {
//										oneSpace = new Position(minY + 1, targetX);
//									} else {
//										isSkip = true;
//										break;
//									}
//								}
//							}
//							if (isSkip) {
//								continue;
//							}
//						}
//						if (minX != 0) {
//							for (int targetY = minY; targetY <= maxY; targetY++) {
//								if (masu[targetY][minX - 1] == Masu.NOT_BLACK) {
//									isSkip = true;
//									break;
//								}
//								if (masu[targetY][minX - 1] == Masu.SPACE) {
//									if (oneSpace == null) {
//										oneSpace = new Position(targetY, maxX - 1);
//									} else {
//										isSkip = true;
//										break;
//									}
//								}
//							}
//							if (isSkip) {
//								continue;
//							}
//						}
//						if (oneSpace != null) {
//							masu[oneSpace.getyIndex()][oneSpace.getxIndex()] = Masu.NOT_BLACK;
//						} else {
//							return false;
//						}
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
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public ChocobananaSolver(Field field) {
		this.field = new Field(field);
	}

	// penpa-edit向けコンストラクタ
	public ChocobananaSolver(String fieldStr) {
		field = new Field(fieldStr);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String fieldStr = "square,10,10,38,0,1,1,418,418,286,286\n" + "[0,0,0,0]\n"
				+ "[\"1\",\"2\",\"1\"]~zS~[\"\",1]\n"
				+ "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{\"30\":[\"1\",1,\"1\"],\"46\":[\"1\",1,\"1\"],\"47\":[\"5\",1,\"1\"],\"58\":[\"4\",1,\"1\"],\"60\":[\"4\",1,\"1\"],\"63\":[\"4\",1,\"1\"],\"94\":[\"4\",1,\"1\"],\"95\":[\"4\",1,\"1\"],\"101\":[\"2\",1,\"1\"],\"117\":[\"5\",1,\"1\"],\"121\":[\"4\",1,\"1\"],\"129\":[\"3\",1,\"1\"],\"144\":[\"4\",1,\"1\"],\"149\":[\"3\",1,\"1\"],\"150\":[\"2\",1,\"1\"]},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n"
				+ "\n"
				+ "[30,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1]"; // urlを入れれば試せる
		System.out.println(new ChocobananaSolver(fieldStr).solve());
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
		System.out.println("難易度:" + Math.pow(count, 0.65));
		System.out.println(field);
		int level = (int) Math.sqrt(Math.pow(count, 0.65) / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount((int) Math.pow(count, 0.65)).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
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
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
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
			field.alreadyPosSet.addAll(virtual2.alreadyPosSet);
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.alreadyPosSet.addAll(virtual.alreadyPosSet);
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
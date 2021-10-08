package myamya.other.solver.archipelago;

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

public class ArchipelagoSolver implements Solver {
	public static class ArchipelagoGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class ArchipelagoSolverForGenerator extends ArchipelagoSolver {

			private final int limit;

			public ArchipelagoSolverForGenerator(Field field, int limit) {
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

		public ArchipelagoGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new ArchipelagoGenerator(7, 7).generate();
		}

		@Override
		public GeneratorResult generate() {
			String solutionStr;
			ArchipelagoSolver.Field wkField = new ArchipelagoSolver.Field(height, width);
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
							ArchipelagoSolver.Field virtual = new ArchipelagoSolver.Field(wkField);
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
							wkField = new ArchipelagoSolver.Field(height, width);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 数字埋め＆マス初期化
				// まず数字を埋める
				List<Position> posList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							posList.add(new Position(yIndex, xIndex));
						}
					}
				}
				Collections.shuffle(posList);
				Set<Position> alreadyPosSet = new HashSet<>();
				for (Position pos : posList) {
					if (alreadyPosSet.contains(pos)) {
						continue;
					}
					Set<Position> continueWhitePosSet = wkField.getContinuePosSet(pos);
					for (Position continueWhitePos : continueWhitePosSet) {
						wkField.numbers[continueWhitePos.getyIndex()][continueWhitePos
								.getxIndex()] = continueWhitePosSet.size();
					}
					alreadyPosSet.addAll(continueWhitePosSet);
				}
//				System.out.println(wkField);
				// 回答を記憶しながらマスを戻す
				Masu[][] solutionMasu = new Masu[height][width];
				wkField.fixedPosSet = new HashSet<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						solutionMasu[yIndex][xIndex] = Masu.SPACE;
						if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						}
					}
				}
				// 解けるかな？
				level = new ArchipelagoSolverForGenerator(new ArchipelagoSolver.Field(wkField), 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ArchipelagoSolver.Field(height, width);
					index = 0;
				} else {
					Collections.shuffle(posList);
					boolean eraseBlack = false;
					// まずは黒マスごと消せないかチャレンジ
					for (Position numberPos : posList) {
						ArchipelagoSolver.Field virtual = new ArchipelagoSolver.Field(wkField);
						virtual.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
						int solveResult = new ArchipelagoSolverForGenerator(virtual, 10000).solve2();
						if (solveResult != -1) {
							eraseBlack = true;
							solutionMasu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.BLACK;
							wkField.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
							wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
							level = solveResult;
						}
					}
					if (!eraseBlack) {
						// 1マスも黒マスが削れなければやり直し
						wkField = new ArchipelagoSolver.Field(height, width);
						index = 0;
					} else {
						// 黒マスを削れないマスは数字だけ削れるか試す
						solutionStr = PenpaEditLib.convertSolutionMasu(solutionMasu);
						for (Position numberPos : posList) {
							if (wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] != null) {
								ArchipelagoSolver.Field virtual = new ArchipelagoSolver.Field(wkField);
								virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
								int solveResult = new ArchipelagoSolverForGenerator(virtual, 10000).solve2();
								if (solveResult != -1) {
									wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
									level = solveResult;
								}
							}
						}
						break;
					}
				}
			}
			// ヒント数字を含む盤面変換
			String fieldStr = PenpaEditLib.convertNumbersMasuField(wkField.masu, wkField.numbers);
			System.out.println(fieldStr);
			System.out.println(solutionStr);
			level = (int) Math.sqrt(level / 20 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(数字/黒：" + wkField.getHintCount().split("/")[0] + "/"
					+ wkField.getHintCount().split("/")[1] + ")";
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
					Masu oneMasu = wkField.getMasu()[yIndex][xIndex];
					if (oneMasu.toString().equals("■")) {
						sb.append(
								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
					}
					if (wkField.getNumbers()[yIndex][xIndex] != null) {
						String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
						int numIdx = HALF_NUMS.indexOf(numberStr);
						String masuStr = null;
						if (numIdx >= 0) {
							masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
						} else {
							masuStr = numberStr;
						}
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5) + "\" fill=\""
								+ (oneMasu.toString().equals("■") ? "white" : "black") + "\" textLength=\""
								+ (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
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
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.(las)");
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
		// 確定した部屋の位置情報。再調査しないことでスピードアップ
		private Set<Set<Position>> fixedPosSet;

		public Masu[][] getMasu() {
			return masu;
		}

		/**
		 * あるマスからつながるマスセットを返す。ジェネレータ用
		 */
		public Set<Position> getContinuePosSet(Position pos) {
			Set<Position> result = new HashSet<>();
			result.add(pos);
			setContinuePosSet(pos, result, Integer.MAX_VALUE, null);
			return result;
		}

		public String getPuzPreURL() {
			return PenpaEditLib.PENPA_EDIT_DUMMY_URL;
		}

		public String getHintCount() {
			int kuroCnt = 0;
			int numberCnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						kuroCnt++;
						if (numbers[yIndex][xIndex] != null) {
							numberCnt++;
						}
					}
				}
			}
			return String.valueOf(numberCnt + "/" + kuroCnt);
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
						setContinuePosSet(pivot, continuePosSet, Integer.MAX_VALUE, null);
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			fixedPosSet = new HashSet<>();
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
			fixedPosSet = new HashSet<>(other.fixedPosSet);
		}

		public Field(String fieldStr) {
			masu = PenpaEditLib.getMasu(fieldStr);
			numbers = PenpaEditLib.getNumbers(fieldStr);
			fixedPosSet = new HashSet<>();
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
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
			if (!countSolve()) {
				return false;
			}
			if (!standAloneSolve()) {
				return false;
			}
			if (!archipelagoSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
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
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pivot);
						if (!(setContinuePosSet(pivot, continuePosSet, numbers[yIndex][xIndex], null))) {
							// サイズ超過
							return false;
						} else if (numbers[yIndex][xIndex] == continuePosSet.size()) {
							// サイズ確定。周りを自分と違う色に
							fixedPosSet.add(continuePosSet);
							for (Position pos : continuePosSet) {
								if (pos.getyIndex() != 0 && !continuePosSet
										.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
									masu[pos.getyIndex() - 1][pos.getxIndex()] = Masu.NOT_BLACK;
								}
								if (pos.getxIndex() != getXLength() - 1 && !continuePosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
									masu[pos.getyIndex()][pos.getxIndex() + 1] = Masu.NOT_BLACK;
								}
								if (pos.getyIndex() != getYLength() - 1 && !continuePosSet
										.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
									masu[pos.getyIndex() + 1][pos.getxIndex()] = Masu.NOT_BLACK;
								}
								if (pos.getxIndex() != 0 && !continuePosSet
										.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
									masu[pos.getyIndex()][pos.getxIndex() - 1] = Masu.NOT_BLACK;
								}
							}
						} else {
							Set<Position> continueCandPosSet = new HashSet<>();
							continueCandPosSet.add(pivot);
							if (!setContinueCandPosSet(pivot, continueCandPosSet, numbers[yIndex][xIndex], null)) {
								if (numbers[yIndex][xIndex] == continueCandPosSet.size()) {
									// サイズ確定。確定マスを自分と同じ色、周りを自分と違う色に
									for (Position pos : continueCandPosSet) {
										fixedPosSet.add(continueCandPosSet);
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
		 * posを起点に黒になりうるマスをつなげていく。 sizeが確保可能とわかった時点でtrueを返す。
		 */
		private boolean setContinueCandPosSet(Position pos, Set<Position> continuePosSet, int size, Direction from) {
			if (continuePosSet.size() > size) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右に黒確定しているマスをつなげていく。別部屋につながるかsizeが超過すると分かった時点でfalseを返す。
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet, int size, Direction from) {
			if (continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)
						&& (numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null
								|| numbers[nextPos.getyIndex()][nextPos.getxIndex()] == size)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null
							&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] != size) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 斜めにつながった群島が1-任意の数まで連なるようにする。すなわち同じサイズの群島が斜めにつながっていたり、
		 * 群島が確定して、1-任意の数までつながっていない場合はfalseを返す。
		 */
		public boolean archipelagoSolve() {
			for (Set<Position> fixedPos : fixedPosSet) {
				Set<Set<Position>> continueArchPosSet = new HashSet<>();
				continueArchPosSet.add(fixedPos);
				List<Integer> sizeList = new ArrayList<Integer>();
				boolean fixedArch = setContinueArchSet(fixedPos, continueArchPosSet);
				for (Set<Position> continueArch : continueArchPosSet) {
					if (sizeList.contains(continueArch.size())) {
						return false;
					} else {
						sizeList.add(continueArch.size());
					}
				}
				if (fixedArch) {
					for (int i = 1; i <= sizeList.size(); i++) {
						if (!sizeList.contains(i)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 確定した島をつなげる。まだ群島が完全確定しなければfalseを、確定していればtrueを返す。
		 */
		private boolean setContinueArchSet(Set<Position> posSet, Set<Set<Position>> continueArchPosSet) {
			boolean result = true;
			for (Position pos : posSet) {
				Position posUpRight = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
				Position posRightDown = new Position(pos.getyIndex() + 1, pos.getxIndex() + 1);
				Position posDownLeft = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
				Position posLeftUp = new Position(pos.getyIndex() - 1, pos.getxIndex() - 1);
				boolean fixUpRight = pos.getyIndex() == 0 || pos.getxIndex() == getXLength() - 1
						|| masu[pos.getyIndex() - 1][pos.getxIndex() + 1] == Masu.NOT_BLACK;
				boolean fixRightDown = pos.getxIndex() == getXLength() - 1 || pos.getyIndex() == getYLength() - 1
						|| masu[pos.getyIndex() + 1][pos.getxIndex() + 1] == Masu.NOT_BLACK;
				boolean fixDownLeft = pos.getyIndex() == getYLength() - 1 || pos.getxIndex() == 0
						|| masu[pos.getyIndex() + 1][pos.getxIndex() - 1] == Masu.NOT_BLACK;
				boolean fixLeftUp = pos.getxIndex() == 0 || pos.getyIndex() == 0
						|| masu[pos.getyIndex() - 1][pos.getxIndex() - 1] == Masu.NOT_BLACK;
				for (Set<Position> fixedPos : fixedPosSet) {
					if (fixedPos.contains(posUpRight)) {
						fixUpRight = true;
					}
					if (fixedPos.contains(posRightDown)) {
						fixRightDown = true;
					}
					if (fixedPos.contains(posDownLeft)) {
						fixDownLeft = true;
					}
					if (fixedPos.contains(posLeftUp)) {
						fixLeftUp = true;
					}
					if (fixedPos == posSet) {
						continue;
					}
					if (fixedPos.contains(posUpRight) || fixedPos.contains(posRightDown)
							|| fixedPos.contains(posDownLeft) || fixedPos.contains(posLeftUp)) {
						if (!continueArchPosSet.contains(fixedPos)) {
							continueArchPosSet.add(fixedPos);
							if (!setContinueArchSet(fixedPos, continueArchPosSet)) {
								result = false;
							}
						}
					}
				}
				if (!fixUpRight || !fixRightDown || !fixDownLeft || !fixLeftUp) {
					result = false;
				}

			}
			return result;
		}

		/**
		 * 孤立している黒マスを島リストに追加
		 */
		private boolean standAloneSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK && numbers[yIndex][xIndex] == null) {
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continueNotBlackPosSet = new HashSet<>();
						continueNotBlackPosSet.add(pivot);
						if (!setContinuePosSetContainsNumber(pivot, continueNotBlackPosSet, null)) {
							Set<Position> continueWhitePosSet = new HashSet<>();
							continueWhitePosSet.add(pivot);
							if (checkAndSetContinueWhitePosSet(pivot, continueWhitePosSet, null,
									continueNotBlackPosSet.size())) {
								if (continueWhitePosSet.size() == continueNotBlackPosSet.size()) {
									fixedPosSet.add(continueNotBlackPosSet);
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定マスをつなぎ、 サイズ超過になると分かった時点でfalseを返す。
		 */
		private boolean checkAndSetContinueWhitePosSet(Position pos, Set<Position> continuePosSet, Direction from,
				int size) {
			if (continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.DOWN, size)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.LEFT, size)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.UP, size)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!checkAndSetContinueWhitePosSet(nextPos, continuePosSet, Direction.RIGHT, size)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒になりうるマスを無制限につなぐが、 数字マスを見つけた時点で処理を打ち切る。
		 */
		private boolean setContinuePosSetContainsNumber(Position pos, Set<Position> continuePosSet, Direction from) {
			if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinuePosSetContainsNumber(nextPos, continuePosSet, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinuePosSetContainsNumber(nextPos, continuePosSet, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinuePosSetContainsNumber(nextPos, continuePosSet, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinuePosSetContainsNumber(nextPos, continuePosSet, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
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

	public ArchipelagoSolver(Field field) {
		this.field = new Field(field);
	}

	// penpa-edit向けコンストラクタ
	public ArchipelagoSolver(String fieldStr) {
		field = new Field(fieldStr);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String fieldStr = "square,6,6,38,0,1,1,266,266,144,144\n" + "[0,0,0,0]\n" + "[\"1\",\"2\",\"1\"]~zS~[\"\",1]\n"
				+ "{zR:{z_:[]},zU:{z_:[]},zS:{\"25\":4,\"27\":4,\"32\":4,\"44\":4,\"55\":4,\"73\":4,\"77\":4},zN:{\"25\":[\"2\",4,\"1\"],\"27\":[\"1\",4,\"1\"],\"32\":[\"4\",4,\"1\"],\"44\":[\"1\",4,\"1\"],\"73\":[\"5\",4,\"1\"],\"77\":[\"1\",4,\"1\"]},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n"
				+ "\n" + "[22,1,1,1,1,1,5,1,1,1,1,1,5,1,1,1,1,1,5,1,1,1,1,1,5,1,1,1,1,1,5,1,1,1,1,1]"; // urlを入れれば試せる
		System.out.println(new ArchipelagoSolver(fieldStr).solve());
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
		System.out.println("難易度:" + count / 20);
		System.out.println(field);
		int level = (int) Math.sqrt(count / 20 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount((int) count / 20).toString() + "(Lv:" + level + ")";
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
			field.fixedPosSet = virtual2.fixedPosSet;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.fixedPosSet = virtual.fixedPosSet;
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
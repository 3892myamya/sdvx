package myamya.other.solver.chblock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.FixedShape;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.PenpaEditLib;
import myamya.other.solver.Solver;

public class ChblockSolver implements Solver {
//	public static class ChblockGenerator implements Generator {
//		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
//		private static final String FULL_NUMS = "０１２３４５６７８９";
//
//		static class ChblockSolverForGenerator extends ChblockSolver {
//
//			private final int limit;
//
//			public ChblockSolverForGenerator(Field field, int limit) {
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
//		static class ExtendedField extends ChblockSolver.Field {
//			public ExtendedField(Field other) {
//				super(other);
//			}
//
//			public ExtendedField(int height, int width) {
//				super(height, width);
//			}
//
//			@Override
//			protected boolean notStandAloneSolve() {
//				return true;
//			}
//		}
//
//		private final int height;
//		private final int width;
//
//		public ChblockGenerator(int height, int width) {
//			this.height = height;
//			this.width = width;
//		}
//
//		public static void main(String[] args) {
//			new ChblockGenerator(7, 7).generate();
//		}
//
//		@Override
//		public GeneratorResult generate() {
//			String solutionStr;
//			ChblockSolver.Field wkField = new ExtendedField(height, width);
//			List<Integer> indexList = new ArrayList<>();
//			for (int i = 0; i < height * width; i++) {
//				indexList.add(i);
//			}
//			Collections.shuffle(indexList);
//			int index = 0;
//			int level = 0;
//			long start = System.nanoTime();
//			while (true) {
//				// 問題生成部
//				while (!wkField.isSolved()) {
//					int yIndex = indexList.get(index) / width;
//					int xIndex = indexList.get(index) % width;
//					if (wkField.masu[yIndex][xIndex] == Masu.SPACE) {
//						boolean isOk = false;
//						List<Integer> numIdxList = new ArrayList<>();
//						for (int i = 0; i < 2; i++) {
//							numIdxList.add(i);
//						}
//						Collections.shuffle(numIdxList);
//						for (int masuNum : numIdxList) {
//							ChblockSolver.Field virtual = new ExtendedField(wkField);
//							if (masuNum < 1) {
//								virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK;
//							} else if (masuNum < 2) {
//								virtual.masu[yIndex][xIndex] = Masu.BLACK;
//							}
//							if (virtual.solveAndCheck()) {
//								isOk = true;
//								wkField.masu = virtual.masu;
//								break;
//							}
//						}
//						if (!isOk) {
//							// 破綻したら0から作り直す。
//							wkField = new ExtendedField(height, width);
//							index = 0;
//							continue;
//						}
//					}
//					index++;
//				}
//				// 数字埋め＆マス初期化
//				// まず数字を埋める
//				List<Position> posList = new ArrayList<>();
//				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
//							posList.add(new Position(yIndex, xIndex));
//						}
//					}
//				}
//				Collections.shuffle(posList);
//				Set<Position> alreadyPosSet = new HashSet<>();
//				for (Position pos : posList) {
//					if (alreadyPosSet.contains(pos)) {
//						continue;
//					}
//					Set<Position> continueWhitePosSet = wkField.getContinuePosSet(pos);
//					for (Position continueWhitePos : continueWhitePosSet) {
//						wkField.numbers[continueWhitePos.getyIndex()][continueWhitePos
//								.getxIndex()] = continueWhitePosSet.size();
//					}
//					alreadyPosSet.addAll(continueWhitePosSet);
//				}
//				System.out.println(wkField);
//				// 回答を記憶しながらマスを戻す
//				Masu[][] solutionMasu = new Masu[height][width];
//				wkField.fixedPosSet = new HashSet<>();
//				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//						solutionMasu[yIndex][xIndex] = Masu.SPACE;
//						if (wkField.masu[yIndex][xIndex] == Masu.NOT_BLACK) {
//							wkField.masu[yIndex][xIndex] = Masu.SPACE;
//						}
//					}
//				}
//				// 解けるかな？
//				level = new ChblockSolverForGenerator(new ChblockSolver.Field(wkField), 500).solve2();
//				if (level == -1) {
//					// 解けなければやり直し
//					wkField = new ChblockSolver.Field(height, width);
//					index = 0;
//				} else {
//					Collections.shuffle(posList);
//					boolean eraseBlack = false;
//					// まずは黒マスごと消せないかチャレンジ
//					for (Position numberPos : posList) {
//						ChblockSolver.Field virtual = new ChblockSolver.Field(wkField);
//						virtual.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
//						virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
//						int solveResult = new ChblockSolverForGenerator(virtual, 10000).solve2();
//						if (solveResult != -1) {
//							eraseBlack = true;
//							solutionMasu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.BLACK;
//							wkField.masu[numberPos.getyIndex()][numberPos.getxIndex()] = Masu.SPACE;
//							wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
//							level = solveResult;
//						}
//					}
//					if (!eraseBlack) {
//						// 1マスも黒マスが削れなければやり直し
//						wkField = new ChblockSolver.Field(height, width);
//						index = 0;
//					} else {
//						// 黒マスを削れないマスは数字だけ削れるか試す
//						solutionStr = PenpaEditLib.convertSolutionMasu(solutionMasu);
//						for (Position numberPos : posList) {
//							if (wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] != null) {
//								ChblockSolver.Field virtual = new ChblockSolver.Field(wkField);
//								virtual.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
//								int solveResult = new ChblockSolverForGenerator(virtual, 10000).solve2();
//								if (solveResult != -1) {
//									wkField.numbers[numberPos.getyIndex()][numberPos.getxIndex()] = null;
//									level = solveResult;
//								}
//							}
//						}
//						break;
//					}
//				}
//			}
//			// ヒント数字を含む盤面変換
//			String fieldStr = PenpaEditLib.convertNumbersMasuField(wkField.masu, wkField.numbers);
//			System.out.println(fieldStr);
//			System.out.println(solutionStr);
//			level = (int) Math.sqrt(level / 20 / 3) + 1;
//			String status = "Lv:" + level + "の問題を獲得！(数字/黒：" + wkField.getHintCount().split("/")[0] + "/"
//					+ wkField.getHintCount().split("/")[1] + ")";
//			String url = wkField.getPuzPreURL();
//			String link = "<a href=\"" + url + "\" target=\"_blank\">penpa-editで解く</a>";
//			StringBuilder sb = new StringBuilder();
//			int baseSize = 20;
//			int margin = 5;
//			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
//					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
//					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
//					Masu oneMasu = wkField.getMasu()[yIndex][xIndex];
//					if (oneMasu.toString().equals("■")) {
//						sb.append(
//								"<rect y=\"" + (yIndex * baseSize + margin) + "\" x=\"" + (xIndex * baseSize + baseSize)
//										+ "\" width=\"" + (baseSize) + "\" height=\"" + (baseSize) + "\">" + "</rect>");
//					}
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
//								+ (xIndex * baseSize + baseSize + 2) + "\" font-size=\"" + (baseSize - 5) + "\" fill=\""
//								+ (oneMasu.toString().equals("■") ? "white" : "black") + "\" textLength=\""
//								+ (baseSize - 5) + "\" lengthAdjust=\"spacingAndGlyphs\">" + masuStr + "</text>");
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
//			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.(las)");
//			System.out.println(level);
//			System.out.println(wkField.getHintCount());
//			System.out.println(wkField);
//			return new PenpaEditGeneratorResult(status, sb.toString(), link, level, "", fieldStr, solutionStr);
//		}
//	}

	public static class Field {

		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private final Integer[][] numbers;
		// 確定した部屋の位置情報。
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
			String[] fieldInfo = fieldStr.split("\n")[0].split(",");
			Integer yLength = Integer.valueOf(fieldInfo[2]);
			Integer xLength = Integer.valueOf(fieldInfo[1]);
			masu = new Masu[yLength][xLength];
			numbers = PenpaEditLib.getNumbers(fieldStr);
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = numbers[yIndex][xIndex] == null ? Masu.SPACE : Masu.BLACK;
				}
			}
			fixedPosSet = new HashSet<>();
		}

		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			fixedPosSet = new HashSet<>();
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
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
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
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity;
					}
					index++;
				}
			}
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
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!notStandAloneSolve()) {
					return false;
				}
				if (!chblockSolve()) {
					return false;
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
						Position pivot = new Position(yIndex, xIndex);
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pivot);
						if (!(setContinuePosSet(pivot, continuePosSet, numbers[yIndex][xIndex], null))) {
							// サイズ超過
							return false;
						} else if (numbers[yIndex][xIndex] == continuePosSet.size()) {
							// サイズ確定。周りを白に
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
									// サイズ確定。自分を黒、周りを白に
									fixedPosSet.add(continueCandPosSet);
									for (Position pos : continueCandPosSet) {
										masu[pos.getyIndex()][pos.getxIndex()] = masu[yIndex][xIndex];
										if (pos.getyIndex() != 0) {
											masu[pos.getyIndex() - 1][pos.getxIndex()] = continueCandPosSet.contains(
													new Position(pos.getyIndex() - 1, pos.getxIndex())) ? Masu.BLACK
															: Masu.NOT_BLACK;
										}
										if (pos.getxIndex() != getXLength() - 1) {
											masu[pos.getyIndex()][pos.getxIndex() + 1] = continueCandPosSet.contains(
													new Position(pos.getyIndex(), pos.getxIndex() + 1)) ? Masu.BLACK
															: Masu.NOT_BLACK;
										}
										if (pos.getyIndex() != getYLength() - 1) {
											masu[pos.getyIndex() + 1][pos.getxIndex()] = continueCandPosSet.contains(
													new Position(pos.getyIndex() + 1, pos.getxIndex())) ? Masu.BLACK
															: Masu.NOT_BLACK;
										}
										if (pos.getxIndex() != 0) {
											masu[pos.getyIndex()][pos.getxIndex() - 1] = continueCandPosSet.contains(
													new Position(pos.getyIndex(), pos.getxIndex() - 1)) ? Masu.BLACK
															: Masu.NOT_BLACK;
										}
									}
								} else if (numbers[yIndex][xIndex] == -1) {
									// -1(？)の場合はsetContinueCandPosSetは必ずfalseになる。
									// その場合、候補マスと確定マスが同じだったら固定される。
									if (continuePosSet.size() == continueCandPosSet.size()) {
										fixedPosSet.add(continueCandPosSet);
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
			if (size != -1 && continuePosSet.size() > size) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, size, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右に黒確定しているマスをつなげていく。sizeが超過すると分かった時点でfalseを返す。
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet, int size, Direction from) {
			if (size != -1 && continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, size, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 斜めにつながった島で同じ形があったり、斜めにつながる島がなければfalseを返す。
		 */
		public boolean chblockSolve() {
			// 孤立チェック
			for (Set<Position> fixedPos : fixedPosSet) {
				if (!checkStandAloneArch(fixedPos)) {
					return false;
				}
			}
			// 同一形状チェック
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						blackPosSet.add(new Position(yIndex, xIndex));
					}
				}
			}
			while (!blackPosSet.isEmpty()) {
				Position pivot = new ArrayList<>(blackPosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(pivot);
				setContinueDiagonalPosSet(pivot, continuePosSet, null);
				// つながったマスのうち島単位で確定しているものを調査対象にする
				List<Set<Position>> checkArch = new ArrayList<>();
				for (Set<Position> fixedPos : fixedPosSet) {
					if (continuePosSet.containsAll(fixedPos)) {
						checkArch.add(fixedPos);
					}
				}
				// 島が同じ形だとfalse
				for (int i = 0; i < checkArch.size(); i++) {
					FixedShape shape = new FixedShape(checkArch.get(i));
					for (int j = i + 1; j < checkArch.size(); j++) {
						Set<Position> compareShape = checkArch.get(j);
						if (shape.isSame(compareShape)) {
							return false;
						}
					}
				}
				blackPosSet.removeAll(continuePosSet);
			}
			return true;
		}

		/**
		 * posSetから斜めにつながるマスが1つもなければfalse。
		 */
		private boolean checkStandAloneArch(Set<Position> posSet) {
			for (Position pos : posSet) {
				Position posUpRight = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
				Position posRightDown = new Position(pos.getyIndex() + 1, pos.getxIndex() + 1);
				Position posDownLeft = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
				Position posLeftUp = new Position(pos.getyIndex() - 1, pos.getxIndex() - 1);
				boolean cantUpRight = posSet.contains(posUpRight) || pos.getyIndex() == 0
						|| pos.getxIndex() == getXLength() - 1
						|| masu[pos.getyIndex() - 1][pos.getxIndex() + 1] == Masu.NOT_BLACK;
				boolean cantRightDown = posSet.contains(posRightDown) || pos.getxIndex() == getXLength() - 1
						|| pos.getyIndex() == getYLength() - 1
						|| masu[pos.getyIndex() + 1][pos.getxIndex() + 1] == Masu.NOT_BLACK;
				boolean cantDownLeft = posSet.contains(posDownLeft) || pos.getyIndex() == getYLength() - 1
						|| pos.getxIndex() == 0 || masu[pos.getyIndex() + 1][pos.getxIndex() - 1] == Masu.NOT_BLACK;
				boolean cantLeftUp = posSet.contains(posLeftUp) || pos.getxIndex() == 0 || pos.getyIndex() == 0
						|| masu[pos.getyIndex() - 1][pos.getxIndex() - 1] == Masu.NOT_BLACK;
				if (!cantUpRight || !cantRightDown || !cantDownLeft || !cantLeftUp) {
					return true;
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右ななめに黒確定マスを無制限につなげていく。
		 */
		protected void setContinueDiagonalPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != 0 && pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueDiagonalPosSet(nextPos, continuePosSet, null);
				}
			}
		}

		/**
		 * 数字が0個・2個以上入る島ができてはならない。
		 */
		protected boolean notStandAloneSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] == null && masu[yIndex][xIndex] == Masu.BLACK) {
						blackPosSet.add(new Position(yIndex, xIndex));
					}
				}
			}
			while (!blackPosSet.isEmpty()) {
				Position pivot = new ArrayList<>(blackPosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(pivot);
				if (!setContinueCandPosSet(pivot, continuePosSet, null)) {
					return false;
				}
				continuePosSet = new HashSet<>();
				continuePosSet.add(pivot);
				if (!setContinuePosSet(pivot, continuePosSet, null, new HashSet<>())) {
					return false;
				}
				blackPosSet.removeAll(continuePosSet);
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒になりうるマスをつなげていく。 数字を見つけた時点でtrueを返す。
		 */
		private boolean setContinueCandPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(nextPos, continuePosSet, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右に黒が確定しているマスをつなげていく。 数字を2個見つけた時点でfalseを返す。
		 */
		private boolean setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from,
				Set<Position> findNumber) {
			if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
				if (findNumber.isEmpty()) {
					findNumber.add(pos);
				} else {
					return false;
				}
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.DOWN, findNumber)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.LEFT, findNumber)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.UP, findNumber)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT, findNumber)) {
						return false;
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

	public ChblockSolver(Field field) {
		this.field = new Field(field);
	}

	// pzprxs向けコンストラクタ
	public ChblockSolver(int height, int width, String param) {
		this.field = new Field(height, width, param);
	}

	// penpa-edit向けコンストラクタ
	public ChblockSolver(String fieldStr) {
		field = new Field(fieldStr);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String fieldStr = "square,10,10,38,0,1,1,418,418,286,286\n" + "[0,0,0,0]\n"
				+ "[\"1\",\"2\",\"1\"]~zS~[\"\",1]\n"
				+ "{zR:{z_:[]},zU:{z_:[]},zS:{\"31\":4,\"33\":4,\"53\":4,\"60\":4,\"65\":4,\"77\":4,\"81\":4,\"89\":4,\"106\":4,\"114\":4,\"118\":4,\"130\":4,\"135\":4,\"142\":4,\"162\":4,\"164\":4},zN:{\"31\":[\"1\",4,\"1\"],\"33\":[\"1\",4,\"1\"],\"53\":[\"?\",4,\"1\"],\"60\":[\"1\",4,\"1\"],\"65\":[\"1\",4,\"1\"],\"77\":[\"3\",4,\"1\"],\"81\":[\"6\",4,\"1\"],\"89\":[\"3\",4,\"1\"],\"106\":[\"?\",4,\"1\"],\"114\":[\"?\",4,\"1\"],\"118\":[\"4\",4,\"1\"],\"130\":[\"3\",4,\"1\"],\"135\":[\"?\",4,\"1\"],\"142\":[\"3\",4,\"1\"],\"162\":[\"2\",4,\"1\"],\"164\":[\"2\",4,\"1\"]},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n"
				+ "\n"
				+ "[30,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1]"; // urlを入れれば試せる
		System.out.println(new ChblockSolver(fieldStr).solve());
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
		System.out.println("難易度:" + count / 5);
		System.out.println(field);
		int level = (int) Math.sqrt(count / 5 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount((int) count / 5).toString() + "(Lv:" + level + ")";
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

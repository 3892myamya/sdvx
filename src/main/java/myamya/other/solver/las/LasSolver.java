package myamya.other.solver.las;

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

public class LasSolver implements Solver {
	public static class LasGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class LasSolverForGenerator extends LasSolver {

			private final int limit;

			public LasSolverForGenerator(Field field, int limit) {
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

		static class ExtendedField extends LasSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			public ExtendedField(int height, int width) {
				super(height, width);
			}

			@Override
			protected boolean standAloneSolve() {
				// 数字があとから決まるので、ここではじいてしまうとダメ。
				// 全通過させる
				return true;
			}

		}

		private final int height;
		private final int width;

		public LasGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new LasGenerator(7, 7).generate();
		}

		@Override
		public GeneratorResult generate() {
			String solutionStr;
			ExtendedField wkField = new ExtendedField(height, width);
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
							ExtendedField virtual = new ExtendedField(wkField);
							if (masuNum < 1) {
								virtual.masu[yIndex][xIndex] = Masu.NOT_BLACK;
							} else if (masuNum < 2) {
								virtual.masu[yIndex][xIndex] = Masu.BLACK;
							}
							// 同じ色のマスが隣り合いやすくする処理
							if (yIndex != 0) {
								if (virtual.masu[yIndex - 1][xIndex] == Masu.SPACE && Math.random() * 5 < 1) {
									virtual.masu[yIndex - 1][xIndex] = virtual.masu[yIndex][xIndex];
								}
							}
							if (xIndex != width - 1) {
								if (virtual.masu[yIndex][xIndex + 1] == Masu.SPACE && Math.random() * 5 < 1) {
									virtual.masu[yIndex][xIndex + 1] = virtual.masu[yIndex][xIndex];
								}
							}
							if (yIndex != height - 1) {
								if (virtual.masu[yIndex + 1][xIndex] == Masu.SPACE && Math.random() * 5 < 1) {
									virtual.masu[yIndex + 1][xIndex] = virtual.masu[yIndex][xIndex];
								}
							}
							if (xIndex != 0) {
								if (virtual.masu[yIndex][xIndex - 1] == Masu.SPACE && Math.random() * 5 < 1) {
									virtual.masu[yIndex][xIndex - 1] = virtual.masu[yIndex][xIndex];
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
							wkField = new ExtendedField(height, width);
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
						posList.add(new Position(yIndex, xIndex));
					}
				}
				Collections.shuffle(posList);
				Set<Position> alreadyPosSet = new HashSet<>();
				for (Position pos : posList) {
					if (alreadyPosSet.contains(pos)) {
						continue;
					}
					Set<Position> continueWhitePosSet = wkField.getContinuePosSet(pos);
					wkField.numbers[pos.getyIndex()][pos.getxIndex()] = continueWhitePosSet.size();
					alreadyPosSet.addAll(continueWhitePosSet);
				}
				System.out.println(wkField);
				// 解答の記憶
				// 既存の黒マスが回答に含まれないようにする。
				boolean existBlack = false;
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] == Masu.BLACK) {
							if (wkField.numbers[yIndex][xIndex] != null) {
								wkField.masu[yIndex][xIndex] = Masu.SPACE;
							} else {
								existBlack = true;
							}
						}
					}
				}
				// 黒ますとしてぬれるマスが0の場合、問題不成立
				if (!existBlack) {
					wkField = new ExtendedField(height, width);
					index = 0;
					continue;
				}
				solutionStr = PenpaEditLib.convertSolutionMasu(wkField.masu);
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.numbers[yIndex][xIndex] == null) {
							wkField.masu[yIndex][xIndex] = Masu.SPACE;
						} else if (wkField.masu[yIndex][xIndex] == Masu.SPACE) {
							wkField.masu[yIndex][xIndex] = Masu.BLACK;
						}
					}
				}
				// 解けるかな？
				level = new LasSolverForGenerator(new LasSolver.Field(wkField), 1000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width);
					index = 0;
				} else {
					break;
				}
			}
			// ヒント数字を含む盤面変換
			String fieldStr = PenpaEditLib.convertNumbersMasuField(wkField.masu, wkField.numbers);
//			System.out.println(fieldStr);
//			System.out.println(solutionStr);

			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + (int) (level) + "の問題を獲得！(ヒント数：" + wkField.getHintCount() + ")";
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
		protected Masu[][] masu;
		// 数字の情報
		protected Integer[][] numbers;
		// 確定した島の位置情報
		protected final Set<Position> alreadyPosSet;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			return PenpaEditLib.PENPA_EDIT_DUMMY_URL;
		}

		/**
		 * あるマスからつながるマスセットを返す。ジェネレータ用
		 */
		public Set<Position> getContinuePosSet(Position pos) {
			Set<Position> result = new HashSet<>();
			result.add(pos);
			setContinuePosSet(masu[pos.getyIndex()][pos.getxIndex()], pos, result, Integer.MAX_VALUE, null);
			return result;
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
			// 余白の切り詰め処理。今は左側の余白が1の場合のみの暫定対応。
			// TODO 汎用化したいところだがそこまで使うことがあるかどうか
			String[] yohakuInfo = fieldStr.split("\n")[1].replaceAll("\\[", "").replaceAll("\\]", "").split(",");
			if (yohakuInfo[2].equals("1")) {
				Masu[][] wkMasu = new Masu[getYLength()][getXLength() - 1];
				Integer[][] wkNumbers = new Integer[getYLength()][getXLength() - 1];
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 1; xIndex < getXLength(); xIndex++) {
						wkMasu[yIndex][xIndex - 1] = masu[yIndex][xIndex];
						wkNumbers[yIndex][xIndex - 1] = numbers[yIndex][xIndex];
					}
				}
				masu = wkMasu;
				numbers = wkNumbers;
			}
			alreadyPosSet = new HashSet<>();
			// 数字のマスで黒でない場合、白確定
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && masu[yIndex][xIndex] == Masu.SPACE) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
				}
			}
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new Integer[height][width];
			alreadyPosSet = new HashSet<>();
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
					//16 - 255は '-'
					//256 - 999は '+'
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
						masu[pos.getyIndex()][pos.getxIndex()] = capacity % 2 == 0 ? Masu.NOT_BLACK : Masu.BLACK;
						numbers[pos.getyIndex()][pos.getxIndex()] = capacity / 2 == 0 ? -1 : capacity / 2;
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
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!countSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!standAloneSolve()) {
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
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
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
										if (pos.getyIndex() != 0) {
											if (continueCandPosSet
													.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
												masu[pos.getyIndex() - 1][pos
														.getxIndex()] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.BLACK
																: Masu.NOT_BLACK;
											} else {
												masu[pos.getyIndex() - 1][pos
														.getxIndex()] = masu[yIndex][xIndex] == Masu.BLACK
																? Masu.NOT_BLACK
																: Masu.BLACK;
											}
										}
										if (pos.getxIndex() != getXLength() - 1) {
											if (continueCandPosSet
													.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
												masu[pos.getyIndex()][pos.getxIndex()
														+ 1] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.BLACK
																: Masu.NOT_BLACK;
											} else {
												masu[pos.getyIndex()][pos.getxIndex()
														+ 1] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.NOT_BLACK
																: Masu.BLACK;
											}
										}
										if (pos.getyIndex() != getYLength() - 1) {
											if (continueCandPosSet
													.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
												masu[pos.getyIndex() + 1][pos
														.getxIndex()] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.BLACK
																: Masu.NOT_BLACK;
											} else {
												masu[pos.getyIndex() + 1][pos
														.getxIndex()] = masu[yIndex][xIndex] == Masu.BLACK
																? Masu.NOT_BLACK
																: Masu.BLACK;
											}
										}
										if (pos.getxIndex() != 0) {
											if (continueCandPosSet
													.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
												masu[pos.getyIndex()][pos.getxIndex()
														- 1] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.BLACK
																: Masu.NOT_BLACK;
											} else {
												masu[pos.getyIndex()][pos.getxIndex()
														- 1] = masu[yIndex][xIndex] == Masu.BLACK ? Masu.NOT_BLACK
																: Masu.BLACK;
											}
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
			if (size != -1 && continuePosSet.size() > size) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if ((masu[nextPos.getyIndex()][nextPos.getxIndex()] == target
						|| masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)
						&& !continuePosSet.contains(nextPos)
						&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null) {
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
						&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null) {
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
						&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null) {
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
						&& numbers[nextPos.getyIndex()][nextPos.getxIndex()] == null) {
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
			if (size != -1 && continuePosSet.size() > size) {
				return false;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == target && !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(target, nextPos, continuePosSet, size, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == target && !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(target, nextPos, continuePosSet, size, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == target && !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(target, nextPos, continuePosSet, size, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == target && !continuePosSet.contains(nextPos)) {
					if (numbers[nextPos.getyIndex()][nextPos.getxIndex()] != null) {
						return false;
					}
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(target, nextPos, continuePosSet, size, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右にtargetと同じ色になりうるマスをつなげていく。 数字を見つけた時点でtrueを返す。
		 */
		private boolean setContinueCandPosSet(Masu target, Position pos, Set<Position> continuePosSet, Direction from) {
			if (numbers[pos.getyIndex()][pos.getxIndex()] != null) {
				return true;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && (masu[nextPos.getyIndex()][nextPos.getxIndex()] == target
						|| masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(target, nextPos, continuePosSet, Direction.DOWN)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && (masu[nextPos.getyIndex()][nextPos.getxIndex()] == target
						|| masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(target, nextPos, continuePosSet, Direction.LEFT)) {
						return true;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && (masu[nextPos.getyIndex()][nextPos.getxIndex()] == target
						|| masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(target, nextPos, continuePosSet, Direction.UP)) {
						return true;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && (masu[nextPos.getyIndex()][nextPos.getxIndex()] == target
						|| masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.SPACE)) {
					continuePosSet.add(nextPos);
					if (setContinueCandPosSet(target, nextPos, continuePosSet, Direction.RIGHT)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * posを起点に上下左右にtargetと同じ色が確定しているマスをつなげていく。 数字を2個見つけた時点でfalseを返す。
		 */
		private boolean setContinuePosSet(Masu target, Position pos, Set<Position> continuePosSet, Direction from,
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
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == target) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(target, nextPos, continuePosSet, Direction.DOWN, findNumber)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == target) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(target, nextPos, continuePosSet, Direction.LEFT, findNumber)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == target) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(target, nextPos, continuePosSet, Direction.UP, findNumber)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == target) {
					continuePosSet.add(nextPos);
					if (!setContinuePosSet(target, nextPos, continuePosSet, Direction.RIGHT, findNumber)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * 数字が入らない・2個以上入るマスができてはならない。
		 */
		protected boolean standAloneSolve() {
			Set<Position> checkPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] != Masu.SPACE) {
						checkPosSet.add(new Position(yIndex, xIndex));
					}
				}
			}
			while (!checkPosSet.isEmpty()) {
				Position pivot = new ArrayList<>(checkPosSet).get(0);
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(pivot);
				if (!setContinueCandPosSet(masu[pivot.getyIndex()][pivot.getxIndex()], pivot, continuePosSet, null)) {
					return false;
				}
				continuePosSet = new HashSet<>();
				continuePosSet.add(pivot);
				if (!setContinuePosSet(masu[pivot.getyIndex()][pivot.getxIndex()], pivot, continuePosSet, null,
						new HashSet<>())) {
					return false;
				}
				checkPosSet.removeAll(continuePosSet);
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

	public LasSolver(Field field) {
		this.field = new Field(field);
	}

	// penpa-edit向けコンストラクタ
	public LasSolver(String fieldStr) {
		field = new Field(fieldStr);
	}

	public LasSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String fieldStr = "square,10,10,38,0,1,1,418,418,286,286\r\n" + "[0,0,0,0]\r\n"
				+ "[\"1\",\"2\",\"1\"]~zS~[\"\",1]\r\n"
				+ "{zR:{z_:[]},zU:{z_:[]},zS:{\"30\":1,\"32\":1,\"51\":1,\"77\":1,\"87\":1,\"108\":1,\"118\":1,\"144\":1,\"163\":1},zN:{\"30\":[\"2\",4,\"1\"],\"32\":[\"6\",4,\"1\"],\"37\":[\"6\",1,\"1\"],\"46\":[\"6\",1,\"1\"],\"51\":[\"6\",4,\"1\"],\"76\":[\"6\",1,\"1\"],\"77\":[\"6\",4,\"1\"],\"87\":[\"6\",4,\"1\"],\"94\":[\"6\",1,\"1\"],\"101\":[\"6\",1,\"1\"],\"108\":[\"6\",4,\"1\"],\"118\":[\"6\",4,\"1\"],\"119\":[\"6\",1,\"1\"],\"144\":[\"6\",4,\"1\"],\"149\":[\"6\",1,\"1\"],\"158\":[\"6\",1,\"1\"],\"163\":[\"6\",4,\"1\"],\"165\":[\"2\",1,\"1\"]},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\r\n"
				+ "\r\n"
				+ "[30,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1,5,1,1,1,1,1,1,1,1,1]"; // urlを入れれば試せる
		System.out.println(new LasSolver(fieldStr).solve());
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
		System.out.println("難易度:" + count);
		System.out.println(field);
		int level = (int) Math.sqrt(count / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount((int) count).toString() + "(Lv:" + level + ")";
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
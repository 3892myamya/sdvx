package myamya.other.solver.tren;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

public class TrenSolver implements Solver {
	public static class TrenGenerator implements Generator {
		static class TrenSolverForGenerator extends TrenSolver {
			private final int limit;

			public TrenSolverForGenerator(Field field, int limit) {
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

		static class ExtendedField extends TrenSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			// ヒントを無視して候補初期化
			public ExtendedField(int height, int width) {
				super(height, width);
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (yIndex + 1 < getYLength()) {
							TrenSikaku sikaku = new TrenSikaku(true,
									new Sikaku(new Position(yIndex, xIndex), new Position(yIndex + 1, xIndex)));
							squareCand.add(sikaku);
						}
						if (yIndex + 2 < getYLength()) {
							TrenSikaku sikaku = new TrenSikaku(true,
									new Sikaku(new Position(yIndex, xIndex), new Position(yIndex + 2, xIndex)));
							squareCand.add(sikaku);
						}
						if (xIndex + 1 < getXLength()) {
							TrenSikaku sikaku = new TrenSikaku(false,
									new Sikaku(new Position(yIndex, xIndex), new Position(yIndex, xIndex + 1)));
							squareCand.add(sikaku);
						}
						if (xIndex + 2 < getXLength()) {
							TrenSikaku sikaku = new TrenSikaku(false,
									new Sikaku(new Position(yIndex, xIndex), new Position(yIndex, xIndex + 2)));
							squareCand.add(sikaku);
						}
					}
				}
			}

			public ExtendedField(int height, int width, ArrayList<TrenSikaku> cacheCand) {
				super(height, width, cacheCand);
			}

			@Override
			public boolean allSolve() {
				// ヒントがあとから決まるので、ここではじいてしまうとダメ。
				// 全通過させる
				return true;
			}

			@Override
			public boolean countSolve() {
				// ヒントがあとから決まるので、ここではじいてしまうとダメ。
				// 全通過させる
				return true;
			}
		}

		private final int height;
		private final int width;

		public TrenGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new TrenGenerator(7, 7).generate();
		}

		@Override
		public GeneratorResult generate() {
			ExtendedField wkField = new ExtendedField(height, width);
			ArrayList<TrenSikaku> cacheCand = new ArrayList<>(wkField.squareCand);
			int candCount = cacheCand.size();
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				List<Integer> indexList = new ArrayList<>();
				for (int i = 0; i < cacheCand.size(); i++) {
					indexList.add(i);
				}
				Collections.shuffle(indexList);
				// 問題生成部
				while (!wkField.isSolved()) {
					TrenSikaku oneCand = cacheCand.get(index);
					if (wkField.squareCand.contains(oneCand)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						if (Math.random() * candCount >= height) {
							numIdxList.add(0);
							numIdxList.add(1);
						} else {
							numIdxList.add(1);
							numIdxList.add(0);
						}
						for (int masuNum : numIdxList) {
							ExtendedField virtual = new ExtendedField(wkField);
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
							wkField = new ExtendedField(height, width, cacheCand);
							index = 0;
							continue;
						}
					}
					index++;
				}
				if (wkField.squareFixed.isEmpty()) {
					// 車の数0を禁止する
					wkField = new ExtendedField(height, width, cacheCand);
					index = 0;
					continue;
				}
				// ヒントを埋める
				Masu[][] masuForCount = wkField.getMasu();
				for (TrenSikaku fixed : wkField.squareFixed) {
					int whiteCnt = 0;
					if (fixed.isVertical()) {
						for (int i = fixed.sikaku.getLeftUp().getyIndex() - 1; i >= 0; i--) {
							if (masuForCount[i][fixed.sikaku.getLeftUp().getxIndex()] == Masu.BLACK) {
								break;
							} else {
								whiteCnt++;
							}
						}
						for (int i = fixed.sikaku.getRightDown().getyIndex() + 1; i < wkField.getYLength(); i++) {
							if (masuForCount[i][fixed.sikaku.getRightDown().getxIndex()] == Masu.BLACK) {
								break;
							} else {
								whiteCnt++;
							}
						}
					} else {
						for (int i = fixed.sikaku.getLeftUp().getxIndex() - 1; i >= 0; i--) {
							if (masuForCount[fixed.sikaku.getLeftUp().getyIndex()][i] == Masu.BLACK) {
								break;
							} else {
								whiteCnt++;
							}
						}
						for (int i = fixed.sikaku.getRightDown().getxIndex() + 1; i < wkField.getXLength(); i++) {
							if (masuForCount[fixed.sikaku.getRightDown().getyIndex()][i] == Masu.BLACK) {
								break;
							} else {
								whiteCnt++;
							}
						}
					}
					List<Position> candPosList = new ArrayList<>(fixed.getPosSet());
					Position hintPos = candPosList.get((int) (Math.random() * candPosList.size()));
					wkField.numbersMap.put(hintPos, whiteCnt);
				}
				System.out.println(wkField);
				// 候補を戻す
				wkField.initCand();
				// 解けるかな？
				level = new TrenSolverForGenerator(wkField, 5000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width, cacheCand);
					index = 0;
				} else {
					break;
				}
			}
			level = (int) Math.sqrt(level / 2 / 3);
			String status = "Lv:" + level + "の問題を獲得！(壁(白/灰/黒)：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">pzprxsで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
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
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			System.out.println(url);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}

	}

	// 2-3マスの集合体
	public static class TrenSikaku {
		private final boolean vertical;
		private final Sikaku sikaku;
		// 以下はその都度算出すると遅くなるのでコンストラクタ時点で覚えさせる
		private final Set<Position> posSet;
		private final Set<Position> yokoWall;
		private final Set<Position> tateWall;

		TrenSikaku(boolean vertical, Sikaku sikaku) {
			this.vertical = vertical;
			this.sikaku = sikaku;
			posSet = sikaku.getPosSet();
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

		public boolean isVertical() {
			return vertical;
		}

		public Set<Position> getPosSet() {
			return posSet;
		}

		/**
		 * 自分が作ることになる横壁の位置を返す。画面表示用。外壁(-1、xlength)も含むので注意
		 */
		public Set<Position> getYokoWall() {
			return yokoWall;
		}

		/**
		 * 自分が作ることになる縦壁の位置を返す。画面表示用。外壁(-1、ylength)も含むので注意
		 */
		public Set<Position> getTateWall() {
			return tateWall;
		}

		// 重複を判断して返す
		public boolean isDuplicate(TrenSikaku other) {
			return sikaku.isDuplicate(other.sikaku);
		}

		@Override
		public String toString() {
			return sikaku.toString();
		}

	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		protected final int yLength;
		protected final int xLength;

		// 表出数字
		protected final Map<Position, Integer> numbersMap;

		public Map<Position, Integer> getNumbersMap() {
			return numbersMap;
		}

		// 四角の配置の候補
		protected List<TrenSikaku> squareCand;
		// 確定した四角候補
		protected List<TrenSikaku> squareFixed;

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
//			sb.append("https://pzprxs.vercel.app/p?voxas/" + getXLength() + "/" + getYLength() + "/");
//			// 横壁処理
//			int interval = 0;
//			int befWallType = 0;
//			for (int i = 0; i < getYLength() * (getXLength() - 1); i++) {
//				int yIndex = i / (getXLength() - 1);
//				int xIndex = i % (getXLength() - 1);
//				Integer wallType = firstYokoWall.get(new Position(yIndex, xIndex));
//				if (wallType == null) {
//					interval++;
//					// 今回壁が来なかった場合、壁接近判定中ならインターバル2、違えばインターバル20で精算
//					if (befWallType != 0 && interval == 2) {
//						sb.append(ALPHABET_AND_NUMBER.charAt(interval * 5 + (befWallType - 1)));
//						interval = 0;
//						befWallType = 0;
//					} else if (befWallType == 0 && interval == 20) {
//						sb.append("z");
//						interval = 0;
//						befWallType = 0;
//					}
//				} else {
//					// 今回壁が来た場合、インターバルのみ精算。
//					if (befWallType != 0) {
//						sb.append(ALPHABET_AND_NUMBER.charAt(interval * 5 + (befWallType - 1)));
//					} else {
//						if (interval != 0) {
//							sb.append(ALPHABET_AND_NUMBER.charAt(interval + 15));
//						}
//					}
//					interval = 0;
//					befWallType = wallType;
//				}
//			}
//			for (int i = 0; i < (getYLength() - 1) * getXLength(); i++) {
//				int yIndex = i / getXLength();
//				int xIndex = i % getXLength();
//				Integer wallType = firstTateWall.get(new Position(yIndex, xIndex));
//				if (wallType == null) {
//					interval++;
//					// 今回壁が来なかった場合、壁接近判定中ならインターバル2、違えばインターバル20で精算
//					if (befWallType != 0 && interval == 2) {
//						sb.append(ALPHABET_AND_NUMBER.charAt(interval * 5 + (befWallType - 1)));
//						interval = 0;
//						befWallType = 0;
//					} else if (befWallType == 0 && interval == 20) {
//						sb.append("z");
//						interval = 0;
//						befWallType = 0;
//					}
//				} else {
//					// 今回壁が来た場合、インターバルのみ精算。
//					if (befWallType != 0) {
//						sb.append(ALPHABET_AND_NUMBER.charAt(interval * 5 + (befWallType - 1)));
//					} else {
//						if (interval != 0) {
//							sb.append(ALPHABET_AND_NUMBER.charAt(interval + 15));
//						}
//					}
//					interval = 0;
//					befWallType = wallType;
//				}
//			}
//			// 最後の一文字
//			if (befWallType != 0) {
//				sb.append(ALPHABET_AND_NUMBER.charAt(10 + (befWallType - 1)));
//			} else {
//				if (interval != 0) {
//					sb.append(ALPHABET_AND_NUMBER.charAt(interval + 15));
//				}
//			}
//			if (sb.charAt(sb.length() - 1) == '.') {
//				sb.append("/");
//			}
			return sb.toString();
		}

		public String getHintCount() {
			int wallCnt = 0;
			int whiteCnt = 0;
			int grayCnt = 0;
			int blackCnt = 0;
//			for (Entry<Position, Integer> entry : firstYokoWall.entrySet()) {
//				wallCnt++;
//				if (entry.getValue() == 4) {
//					whiteCnt++;
//				} else if (entry.getValue() == 3) {
//					grayCnt++;
//				} else if (entry.getValue() == 2) {
//					blackCnt++;
//				}
//			}
//			for (Entry<Position, Integer> entry : firstTateWall.entrySet()) {
//				wallCnt++;
//				if (entry.getValue() == 4) {
//					whiteCnt++;
//				} else if (entry.getValue() == 3) {
//					grayCnt++;
//				} else if (entry.getValue() == 2) {
//					blackCnt++;
//				}
//			}
			return wallCnt + "(" + whiteCnt + "/" + grayCnt + "/" + blackCnt + ")";
		}

		public int getYLength() {
			return yLength;
		}

		public int getXLength() {
			return xLength;
		}

		/**
		 * プレーンなフィールド作成
		 */
		public Field(int height, int width) {
			yLength = height;
			xLength = width;
			numbersMap = new HashMap<>();
			initCand();
		}

		public Field(int height, int width, ArrayList<TrenSikaku> cacheCand) {
			yLength = height;
			xLength = width;
			numbersMap = new HashMap<>();
			squareCand = new ArrayList<>(cacheCand);
			squareFixed = new ArrayList<>();
		}

		/**
		 * 部屋のきりかたの候補を初期化する。 firstYokoWall、firstTateWallが決まった後に呼ぶ必要あり
		 */
		protected void initCand() {
			squareCand = makeSquareCandBase();
			squareFixed = new ArrayList<>();
		}

		/**
		 * 部屋の切り方の候補を作成する。
		 */
		protected List<TrenSikaku> makeSquareCandBase() {
			// 数字を含まなかったり、2個以上含んでたら消す
			List<TrenSikaku> squareCandBase = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (yIndex + 1 < getYLength()) {
						TrenSikaku sikaku = new TrenSikaku(true,
								new Sikaku(new Position(yIndex, xIndex), new Position(yIndex + 1, xIndex)));
						boolean isCand = false;
						for (Position pos : sikaku.getPosSet()) {
							if (numbersMap.containsKey(pos)) {
								if (isCand) {
									isCand = false;
									break;
								} else {
									isCand = true;
								}
							}
						}
						if (isCand) {
							squareCandBase.add(sikaku);
						}
					}
					if (yIndex + 2 < getYLength()) {
						TrenSikaku sikaku = new TrenSikaku(true,
								new Sikaku(new Position(yIndex, xIndex), new Position(yIndex + 2, xIndex)));
						boolean isCand = false;
						for (Position pos : sikaku.getPosSet()) {
							if (numbersMap.containsKey(pos)) {
								if (isCand) {
									isCand = false;
									break;
								} else {
									isCand = true;
								}
							}
						}
						if (isCand) {
							squareCandBase.add(sikaku);
						}
					}
					if (xIndex + 1 < getXLength()) {
						TrenSikaku sikaku = new TrenSikaku(false,
								new Sikaku(new Position(yIndex, xIndex), new Position(yIndex, xIndex + 1)));
						boolean isCand = false;
						for (Position pos : sikaku.getPosSet()) {
							if (numbersMap.containsKey(pos)) {
								if (isCand) {
									isCand = false;
									break;
								} else {
									isCand = true;
								}
							}
						}
						if (isCand) {
							squareCandBase.add(sikaku);
						}
					}
					if (xIndex + 2 < getXLength()) {
						TrenSikaku sikaku = new TrenSikaku(false,
								new Sikaku(new Position(yIndex, xIndex), new Position(yIndex, xIndex + 2)));
						boolean isCand = false;
						for (Position pos : sikaku.getPosSet()) {
							if (numbersMap.containsKey(pos)) {
								if (isCand) {
									isCand = false;
									break;
								} else {
									isCand = true;
								}
							}
						}
						if (isCand) {
							squareCandBase.add(sikaku);
						}
					}
				}
			}
			return squareCandBase;
		}

		public Field(int height, int width, String param) {
			yLength = height;
			xLength = width;
			numbersMap = new HashMap<>();
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
						numbersMap.put(pos, -1);
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
						numbersMap.put(pos, capacity);
					}
					index++;
				}
			}
			initCand();
		}

		public Field(Field other) {
			yLength = other.yLength;
			xLength = other.xLength;
			numbersMap = new HashMap<>(other.numbersMap);
			squareCand = new ArrayList<>(other.squareCand);
			squareFixed = new ArrayList<>(other.squareFixed);
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		/**
		 * 候補情報から横壁を復元する。画面表示用
		 */
		public Wall[][] getYokoWall() {
			Wall[][] yokoWall = new Wall[getYLength()][getXLength() - 1];
			Set<Position> fixedWallPosSet = new HashSet<>();
			Set<Position> candWallPosSet = new HashSet<>();
			for (TrenSikaku fixed : squareFixed) {
				fixedWallPosSet.addAll(fixed.getYokoWall());
			}
			for (TrenSikaku cand : squareCand) {
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
		 * 候補情報から横壁を復元する。画面表示用
		 */
		public Wall[][] getTateWall() {
			Wall[][] tateWall = new Wall[getYLength() - 1][getXLength()];
			Set<Position> fixedWallPosSet = new HashSet<>();
			Set<Position> candWallPosSet = new HashSet<>();
			for (TrenSikaku fixed : squareFixed) {
				fixedWallPosSet.addAll(fixed.getTateWall());
			}
			for (TrenSikaku cand : squareCand) {
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

		@Override
		public String toString() {
			Object[][] masu = getMasu();
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Integer number = numbersMap.get(new Position(yIndex, xIndex));
					if (number != null) {
						if (number > 99) {
							sb.append("99");
						} else if (number == -1) {
							sb.append("？");
						} else {
							String capacityStr = String.valueOf(number);
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
			for (TrenSikaku fixed : squareFixed) {
				for (Position pos : fixed.getPosSet()) {
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
				}

			}
			for (TrenSikaku cand : squareCand) {
				for (Position pos : cand.getPosSet()) {
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
				}
			}
			return masu;
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
			if (!allSolve()) {
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
			for (TrenSikaku fixed : squareFixed) {
				for (TrenSikaku otherFixed : squareFixed) {
					if (fixed != otherFixed) {
						if (otherFixed.isDuplicate(fixed)) {
							return false;
						}
					}
				}
				for (Iterator<TrenSikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
					TrenSikaku oneCand = iterator.next();
					if (oneCand.isDuplicate(fixed)) {
						iterator.remove();
					}
				}
			}
			return true;
		}

		/**
		 * 埋まらない数字が出来てしまった場合falseを返す。 逆に、ある数字を埋められる候補が1つしかない場合、確定形に昇格する。
		 */
		protected boolean allSolve() {
			for (Entry<Position, Integer> entry : numbersMap.entrySet()) {
				Position pos = entry.getKey();
				// 既に確定しているマスを除外
				boolean isFixed = false;
				for (TrenSikaku fixed : squareFixed) {
					if (fixed.getPosSet().contains(pos)) {
						isFixed = true;
						break;
					}
				}
				if (isFixed) {
					continue;
				}
				// そのマスに対する候補を検索
				TrenSikaku pickup = null;
				boolean only = true;
				for (TrenSikaku cand : squareCand) {
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
			return true;
		}

		/**
		 * 数字は前後にとりうるマスである。違反した場合falseを返す。
		 */
		protected boolean countSolve() {
			Masu[][] masu = getMasu();
			for (TrenSikaku fixed : squareFixed) {
				int number = -1;
				for (Position pos : fixed.getPosSet()) {
					if (numbersMap.containsKey(pos)) {
						number = numbersMap.get(pos);
						break;
					}
				}
				if (number != -1) {
					int whiteCnt = 0;
					int spaceCnt = 0;
					boolean countUpWhite = true;
					if (fixed.isVertical()) {
						for (int i = fixed.sikaku.getLeftUp().getyIndex() - 1; i >= 0; i--) {
							if (masu[i][fixed.sikaku.getLeftUp().getxIndex()] == Masu.BLACK) {
								break;
							} else if (masu[i][fixed.sikaku.getLeftUp().getxIndex()] == Masu.SPACE) {
								spaceCnt++;
								countUpWhite = false;
							} else if (masu[i][fixed.sikaku.getLeftUp().getxIndex()] == Masu.NOT_BLACK) {
								spaceCnt++;
								if (countUpWhite) {
									whiteCnt++;
								}
							}
						}
						countUpWhite = true;
						for (int i = fixed.sikaku.getRightDown().getyIndex() + 1; i < getYLength(); i++) {
							if (masu[i][fixed.sikaku.getRightDown().getxIndex()] == Masu.BLACK) {
								break;
							} else if (masu[i][fixed.sikaku.getRightDown().getxIndex()] == Masu.SPACE) {
								spaceCnt++;
								countUpWhite = false;
							} else if (masu[i][fixed.sikaku.getRightDown().getxIndex()] == Masu.NOT_BLACK) {
								spaceCnt++;
								if (countUpWhite) {
									whiteCnt++;
								}
							}
						}
						if (whiteCnt > number) {
							return false;
						}
						if (spaceCnt < number) {
							return false;
						}
					} else {
						for (int i = fixed.sikaku.getLeftUp().getxIndex() - 1; i >= 0; i--) {
							if (masu[fixed.sikaku.getLeftUp().getyIndex()][i] == Masu.BLACK) {
								break;
							} else if (masu[fixed.sikaku.getLeftUp().getyIndex()][i] == Masu.SPACE) {
								spaceCnt++;
								countUpWhite = false;
							} else if (masu[fixed.sikaku.getLeftUp().getyIndex()][i] == Masu.NOT_BLACK) {
								spaceCnt++;
								if (countUpWhite) {
									whiteCnt++;
								}
							}
						}
						countUpWhite = true;
						for (int i = fixed.sikaku.getRightDown().getxIndex() + 1; i < getXLength(); i++) {
							if (masu[fixed.sikaku.getRightDown().getyIndex()][i] == Masu.BLACK) {
								break;
							} else if (masu[fixed.sikaku.getRightDown().getyIndex()][i] == Masu.SPACE) {
								spaceCnt++;
								countUpWhite = false;
							} else if (masu[fixed.sikaku.getRightDown().getyIndex()][i] == Masu.NOT_BLACK) {
								spaceCnt++;
								if (countUpWhite) {
									whiteCnt++;
								}
							}
						}
						if (whiteCnt > number) {
							return false;
						}
						if (spaceCnt < number) {
							return false;
						}
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

	public TrenSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public TrenSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	//
	public static void main(String[] args) {
		String url = "https://pzprxs.vercel.app/p?tren/6/6/4o0i1i3o2m"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new TrenSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count / 2));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 2 / 3);
		return "解けました。推定難易度:" + Difficulty.getByCount(count / 2).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (Iterator<TrenSikaku> iterator = field.squareCand.iterator(); iterator.hasNext();) {
			count++;
			TrenSikaku oneCand = iterator.next();
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

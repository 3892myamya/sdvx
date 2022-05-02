package myamya.other.solver.voxas;

import java.util.AbstractMap.SimpleEntry;
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
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Sikaku;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class VoxasSolver implements Solver {
	public static class VoxasGenerator implements Generator {
		static class VoxasSolverForGenerator extends VoxasSolver {
			private final int limit;

			public VoxasSolverForGenerator(Field field, int limit) {
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

		public VoxasGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new VoxasGenerator(4, 4).generate();
		}

		@Override
		public GeneratorResult generate() {
			VoxasSolver.Field wkField = new VoxasSolver.Field(height, width);
			ArrayList<VoxasSikaku> cacheCand = new ArrayList<>(wkField.squareCand);
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
					VoxasSikaku oneCand = cacheCand.get(index);
					if (wkField.squareCand.contains(oneCand)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							VoxasSolver.Field virtual = new VoxasSolver.Field(wkField);
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
							wkField = new VoxasSolver.Field(height, width, cacheCand);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 初期横壁
				List<Entry<Position, Boolean>> hintPosEntryList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						Position pos = new Position(yIndex, xIndex);
						int type = 0;
						for (VoxasSikaku fixed : wkField.squareFixed) {
							if (fixed.getNotYokoWall().contains(pos)) {
								break;
							} else if (fixed.getYokoWall().contains(pos)) {
								if (type == 0) {
									type = fixed.getType();
								} else {
									if (type + fixed.getType() == 5) {
										wkField.firstYokoWall.put(pos, 2);
									} else if (type == fixed.getType()) {
										wkField.firstYokoWall.put(pos, 4);
									} else {
										wkField.firstYokoWall.put(pos, 3);
									}
									hintPosEntryList.add(new SimpleEntry<>(pos, true));
									hintPosEntryList.add(new SimpleEntry<>(pos, true));
									break;
								}
							}
						}
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						Position pos = new Position(yIndex, xIndex);
						int type = 0;
						for (VoxasSikaku fixed : wkField.squareFixed) {
							if (fixed.getNotTateWall().contains(pos)) {
								break;
							} else if (fixed.getTateWall().contains(pos)) {
								if (type == 0) {
									type = fixed.getType();
								} else {
									if (type + fixed.getType() == 5) {
										wkField.firstTateWall.put(pos, 2);
									} else if (type == fixed.getType()) {
										wkField.firstTateWall.put(pos, 4);
									} else {
										wkField.firstTateWall.put(pos, 3);
									}
									hintPosEntryList.add(new SimpleEntry<>(pos, false));
									hintPosEntryList.add(new SimpleEntry<>(pos, false));
									break;
								}
							}
						}
					}
				}
				// 候補を戻す
				wkField.initCand();
				// 解けるかな？
				level = new VoxasSolverForGenerator(wkField, 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new VoxasSolver.Field(height, width, cacheCand);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(hintPosEntryList);
					for (Entry<Position, Boolean> hintPosEntry : hintPosEntryList) {
						VoxasSolver.Field virtual = new VoxasSolver.Field(wkField);
						if (hintPosEntry.getValue()) {
							if (virtual.firstYokoWall.get(hintPosEntry.getKey()) == 1) {
								virtual.firstYokoWall.remove(hintPosEntry.getKey());
							} else {
								virtual.firstYokoWall.put(hintPosEntry.getKey(), 1);
							}
						} else {
							if (virtual.firstTateWall.get(hintPosEntry.getKey()) == 1) {
								virtual.firstTateWall.remove(hintPosEntry.getKey());
							} else {
								virtual.firstTateWall.put(hintPosEntry.getKey(), 1);
							}
						}
						virtual.initCand();
						int solveResult = new VoxasSolverForGenerator(virtual, 500).solve2();
						if (solveResult != -1) {
							if (hintPosEntry.getValue()) {
								if (wkField.firstYokoWall.get(hintPosEntry.getKey()) == 1) {
									wkField.firstYokoWall.remove(hintPosEntry.getKey());
								} else {
									wkField.firstYokoWall.put(hintPosEntry.getKey(), 1);
								}
							} else {
								if (wkField.firstTateWall.get(hintPosEntry.getKey()) == 1) {
									wkField.firstTateWall.remove(hintPosEntry.getKey());
								} else {
									wkField.firstTateWall.put(hintPosEntry.getKey(), 1);
								}
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level * 2 / 3);
			String status = "Lv:" + level + "の問題を獲得！(壁(白/灰/黒)：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">pzprxsで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1
							|| wkField.getFirstYokoWall().get(new Position(yIndex, xIndex)) != null;
					sb.append("<line y1=\"" + (yIndex * baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + 2 * baseSize) + "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						if (xIndex != -1 && xIndex != wkField.getXLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"#000\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
					if (wkField.getFirstYokoWall().get(new Position(yIndex, xIndex)) != null) {
						if (wkField.getFirstYokoWall().get(new Position(yIndex, xIndex)) == 2) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 2
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
						} else if (wkField.getFirstYokoWall().get(new Position(yIndex, xIndex)) == 3) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 2
									+ "\" fill=\"gray\", stroke=\"black\">" + "</circle>");
						} else if (wkField.getFirstYokoWall().get(new Position(yIndex, xIndex)) == 4) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + baseSize) + "\" r=\"" + 2
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						}
					}
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1
							|| wkField.getFirstTateWall().get(new Position(yIndex, xIndex)) != null;
					sb.append("<line y1=\"" + (yIndex * baseSize + baseSize + margin) + "\" x1=\""
							+ (xIndex * baseSize + baseSize) + "\" y2=\"" + (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\"" + (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						if (yIndex != -1 && yIndex != wkField.getYLength() - 1) {
							sb.append("stroke=\"#000\" ");
						} else {
							sb.append("stroke=\"#000\" ");
						}
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">" + "</line>");
					if (wkField.getFirstTateWall().get(new Position(yIndex, xIndex)) != null) {
						if (wkField.getFirstTateWall().get(new Position(yIndex, xIndex)) == 2) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 2
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
						} else if (wkField.getFirstTateWall().get(new Position(yIndex, xIndex)) == 3) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 2
									+ "\" fill=\"gray\", stroke=\"black\">" + "</circle>");
						} else if (wkField.getFirstTateWall().get(new Position(yIndex, xIndex)) == 4) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + baseSize + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize + (baseSize / 2)) + "\" r=\"" + 2
									+ "\" fill=\"white\", stroke=\"black\">" + "</circle>");
						}
					}
				}
			}
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
	public static class VoxasSikaku {
		// 1:縦2マス、2:横2マス、3:縦3マス、4:横3マス
		private final int type;
		private final Sikaku sikaku;
		// 以下はその都度算出すると遅くなるのでコンストラクタ時点で覚えさせる
		private final Set<Position> posSet;
		private final Set<Position> yokoWall;
		private final Set<Position> notYokoWall;
		private final Set<Position> tateWall;
		private final Set<Position> notTateWall;

		VoxasSikaku(int type, Sikaku sikaku) {
			this.type = type;
			this.sikaku = sikaku;
			posSet = sikaku.getPosSet();
			yokoWall = new HashSet<>();
			for (int y = sikaku.getLeftUp().getyIndex(); y <= sikaku.getRightDown().getyIndex(); y++) {
				yokoWall.add(new Position(y, sikaku.getLeftUp().getxIndex() - 1));
				yokoWall.add(new Position(y, sikaku.getRightDown().getxIndex()));
			}
			notYokoWall = new HashSet<>();
			for (int y = sikaku.getLeftUp().getyIndex(); y <= sikaku.getRightDown().getyIndex(); y++) {
				for (int x = sikaku.getLeftUp().getxIndex(); x < sikaku.getRightDown().getxIndex(); x++) {
					notYokoWall.add(new Position(y, x));
				}
			}
			tateWall = new HashSet<>();
			for (int x = sikaku.getLeftUp().getxIndex(); x <= sikaku.getRightDown().getxIndex(); x++) {
				tateWall.add(new Position(sikaku.getLeftUp().getyIndex() - 1, x));
				tateWall.add(new Position(sikaku.getRightDown().getyIndex(), x));
			}
			notTateWall = new HashSet<>();
			for (int x = sikaku.getLeftUp().getxIndex(); x <= sikaku.getRightDown().getxIndex(); x++) {
				for (int y = sikaku.getLeftUp().getyIndex(); y < sikaku.getRightDown().getyIndex(); y++) {
					notTateWall.add(new Position(y, x));
				}
			}
		}

		public int getType() {
			return type;
		}

		public Set<Position> getPosSet() {
			return posSet;
		}

		// 重複を判断して返す
		public boolean isDuplicate(VoxasSikaku other) {
			return sikaku.isDuplicate(other.sikaku);
		}

		/**
		 * 自分が作ることになる横壁の位置を返す。外壁(-1、xlength)も含むので注意
		 */
		public Set<Position> getYokoWall() {
			return yokoWall;
		}

		/**
		 * 自分が作ることに"ならない"横壁の位置を返す。
		 */
		public Set<Position> getNotYokoWall() {
			return notYokoWall;
		}

		/**
		 * 自分が作ることになる縦壁の位置を返す。外壁(-1、ylength)も含むので注意
		 */
		public Set<Position> getTateWall() {
			return tateWall;
		}

		/**
		 * 自分が作ることに"ならない"縦壁の位置を返す。
		 */
		public Set<Position> getNotTateWall() {
			return notTateWall;
		}

		@Override
		public String toString() {
			return sikaku.toString();
		}
	}

	public static class Field {
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";

		protected final int yLength;
		protected final int xLength;

		// 表出している横壁 0=なし、1=壁、2=黒、3=灰、4=白
		protected final Map<Position, Integer> firstYokoWall;
		// 表出している縦壁 0=なし、1=壁、2=黒、3=灰、4=白
		protected final Map<Position, Integer> firstTateWall;

		public Map<Position, Integer> getFirstYokoWall() {
			return firstYokoWall;
		}

		public Map<Position, Integer> getFirstTateWall() {
			return firstTateWall;
		}

		// 四角の配置の候補
		protected List<VoxasSikaku> squareCand;
		// 確定した四角候補
		protected List<VoxasSikaku> squareFixed;

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://pzprxs.vercel.app/p?voxas/" + getXLength() + "/" + getYLength() + "/");
			// 横壁処理
			int interval = 0;
			int befWallType = 0;
			for (int i = 0; i < getYLength() * (getXLength() - 1); i++) {
				int yIndex = i / (getXLength() - 1);
				int xIndex = i % (getXLength() - 1);
				Integer wallType = firstYokoWall.get(new Position(yIndex, xIndex));
				if (wallType == null) {
					interval++;
					// 今回壁が来なかった場合、壁接近判定中ならインターバル2、違えばインターバル20で精算
					if (befWallType != 0 && interval == 2) {
						sb.append(ALPHABET_AND_NUMBER.charAt(interval * 5 + (befWallType - 1)));
						interval = 0;
						befWallType = 0;
					} else if (befWallType == 0 && interval == 20) {
						sb.append("z");
						interval = 0;
						befWallType = 0;
					}
				} else {
					// 今回壁が来た場合、インターバルのみ精算。
					if (befWallType != 0) {
						sb.append(ALPHABET_AND_NUMBER.charAt(interval * 5 + (befWallType - 1)));
					} else {
						if (interval != 0) {
							sb.append(ALPHABET_AND_NUMBER.charAt(interval + 15));
						}
					}
					interval = 0;
					befWallType = wallType;
				}
			}
			for (int i = 0; i < (getYLength() - 1) * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				Integer wallType = firstTateWall.get(new Position(yIndex, xIndex));
				if (wallType == null) {
					interval++;
					// 今回壁が来なかった場合、壁接近判定中ならインターバル2、違えばインターバル20で精算
					if (befWallType != 0 && interval == 2) {
						sb.append(ALPHABET_AND_NUMBER.charAt(interval * 5 + (befWallType - 1)));
						interval = 0;
						befWallType = 0;
					} else if (befWallType == 0 && interval == 20) {
						sb.append("z");
						interval = 0;
						befWallType = 0;
					}
				} else {
					// 今回壁が来た場合、インターバルのみ精算。
					if (befWallType != 0) {
						sb.append(ALPHABET_AND_NUMBER.charAt(interval * 5 + (befWallType - 1)));
					} else {
						if (interval != 0) {
							sb.append(ALPHABET_AND_NUMBER.charAt(interval + 15));
						}
					}
					interval = 0;
					befWallType = wallType;
				}
			}
			// 最後の一文字
			if (befWallType != 0) {
				sb.append(ALPHABET_AND_NUMBER.charAt(10 + (befWallType - 1)));
			} else {
				if (interval != 0) {
					sb.append(ALPHABET_AND_NUMBER.charAt(interval + 15));
				}
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public String getHintCount() {
			int wallCnt = 0;
			int whiteCnt = 0;
			int grayCnt = 0;
			int blackCnt = 0;
			for (Entry<Position, Integer> entry : firstYokoWall.entrySet()) {
				wallCnt++;
				if (entry.getValue() == 4) {
					whiteCnt++;
				} else if (entry.getValue() == 3) {
					grayCnt++;
				} else if (entry.getValue() == 2) {
					blackCnt++;
				}
			}
			for (Entry<Position, Integer> entry : firstTateWall.entrySet()) {
				wallCnt++;
				if (entry.getValue() == 4) {
					whiteCnt++;
				} else if (entry.getValue() == 3) {
					grayCnt++;
				} else if (entry.getValue() == 2) {
					blackCnt++;
				}
			}
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
			firstYokoWall = new HashMap<>();
			firstTateWall = new HashMap<>();
			initCand();
		}

		public Field(int height, int width, ArrayList<VoxasSikaku> cacheCand) {
			yLength = height;
			xLength = width;
			firstYokoWall = new HashMap<>();
			firstTateWall = new HashMap<>();
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
		protected List<VoxasSikaku> makeSquareCandBase() {
			List<VoxasSikaku> squareCandBase = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (yIndex + 1 < getYLength()) {
						VoxasSikaku sikaku = new VoxasSikaku(1,
								new Sikaku(new Position(yIndex, xIndex), new Position(yIndex + 1, xIndex)));
						boolean isCand = true;
						for (Position pos : sikaku.getNotYokoWall()) {
							if (firstYokoWall.containsKey(pos)) {
								isCand = false;
								break;
							}
						}
						for (Position pos : sikaku.getNotTateWall()) {
							if (firstTateWall.containsKey(pos)) {
								isCand = false;
								break;
							}
						}
						if (isCand) {
							squareCandBase.add(sikaku);
						}
					}
					if (yIndex + 2 < getYLength()) {
						VoxasSikaku sikaku = new VoxasSikaku(3,
								new Sikaku(new Position(yIndex, xIndex), new Position(yIndex + 2, xIndex)));
						boolean isCand = true;
						for (Position pos : sikaku.getNotYokoWall()) {
							if (firstYokoWall.containsKey(pos)) {
								isCand = false;
								break;
							}
						}
						for (Position pos : sikaku.getNotTateWall()) {
							if (firstTateWall.containsKey(pos)) {
								isCand = false;
								break;
							}
						}
						if (isCand) {
							squareCandBase.add(sikaku);
						}
					}
					if (xIndex + 1 < getXLength()) {
						VoxasSikaku sikaku = new VoxasSikaku(2,
								new Sikaku(new Position(yIndex, xIndex), new Position(yIndex, xIndex + 1)));
						boolean isCand = true;
						for (Position pos : sikaku.getNotYokoWall()) {
							if (firstYokoWall.containsKey(pos)) {
								isCand = false;
								break;
							}
						}
						for (Position pos : sikaku.getNotTateWall()) {
							if (firstTateWall.containsKey(pos)) {
								isCand = false;
								break;
							}
						}
						if (isCand) {
							squareCandBase.add(sikaku);
						}
					}
					if (xIndex + 2 < getXLength()) {
						VoxasSikaku sikaku = new VoxasSikaku(4,
								new Sikaku(new Position(yIndex, xIndex), new Position(yIndex, xIndex + 2)));
						boolean isCand = true;
						for (Position pos : sikaku.getNotYokoWall()) {
							if (firstYokoWall.containsKey(pos)) {
								isCand = false;
								break;
							}
						}
						for (Position pos : sikaku.getNotTateWall()) {
							if (firstTateWall.containsKey(pos)) {
								isCand = false;
								break;
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
			firstYokoWall = new HashMap<>();
			firstTateWall = new HashMap<>();
			int index = 0;
			int yokoWallCandCount = (getYLength()) * (getXLength() - 1);
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int num = ALPHABET_AND_NUMBER.indexOf(ch);
				if (num >= 16) {
					// numが16(g-z)以上であれば、その分壁の間隔を開ける
					index = index + num - 16;
				} else {
					int interval = num / 5;
					int wallKind = (num % 5) + 1;
					// まず今の自分の場所の壁が決まる。
					if (yokoWallCandCount <= index) {
						int indexBase = index - yokoWallCandCount;
						firstTateWall.put(new Position(indexBase / getXLength(), indexBase % getXLength()), wallKind);
					} else {
						firstYokoWall.put(new Position(index / (getXLength() - 1), index % (getXLength() - 1)),
								wallKind);
					}
					// インターバルの分だけindex増加
					index = index + interval;
				}
				index++;
			}
			initCand();
		}

		public Field(Field other) {
			yLength = other.yLength;
			xLength = other.xLength;
			firstYokoWall = new HashMap<>(other.firstYokoWall);
			firstTateWall = new HashMap<>(other.firstTateWall);
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
			for (VoxasSikaku fixed : squareFixed) {
				fixedWallPosSet.addAll(fixed.getYokoWall());
			}
			for (VoxasSikaku cand : squareCand) {
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
			for (VoxasSikaku fixed : squareFixed) {
				fixedWallPosSet.addAll(fixed.getTateWall());
			}
			for (VoxasSikaku cand : squareCand) {
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
					sb.append("　");
					if (xIndex != getXLength() - 1) {
						if (firstYokoWall.get(new Position(yIndex, xIndex)) != null) {
							if (firstYokoWall.get(new Position(yIndex, xIndex)) == 1) {
								sb.append("□");
							} else if (firstYokoWall.get(new Position(yIndex, xIndex)) == 2) {
								sb.append("●");
							} else if (firstYokoWall.get(new Position(yIndex, xIndex)) == 3) {
								sb.append("◎");
							} else if (firstYokoWall.get(new Position(yIndex, xIndex)) == 4) {
								sb.append("○");
							}
						} else {
							sb.append(yokoWall[yIndex][xIndex]);
						}
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (firstTateWall.get(new Position(yIndex, xIndex)) != null) {
							if (firstTateWall.get(new Position(yIndex, xIndex)) == 1) {
								sb.append("□");
							} else if (firstTateWall.get(new Position(yIndex, xIndex)) == 2) {
								sb.append("●");
							} else if (firstTateWall.get(new Position(yIndex, xIndex)) == 3) {
								sb.append("◎");
							} else if (firstTateWall.get(new Position(yIndex, xIndex)) == 4) {
								sb.append("○");
							}
						} else {
							sb.append(tateWall[yIndex][xIndex]);
						}
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
			if (!typeSolve()) {
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
			for (VoxasSikaku fixed : squareFixed) {
				for (VoxasSikaku otherFixed : squareFixed) {
					if (fixed != otherFixed) {
						if (otherFixed.isDuplicate(fixed)) {
							return false;
						}
					}
				}
				for (Iterator<VoxasSikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
					VoxasSikaku oneCand = iterator.next();
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
					for (VoxasSikaku fixed : squareFixed) {
						if (fixed.getPosSet().contains(pos)) {
							isFixed = true;
							break;
						}
					}
					if (isFixed) {
						continue;
					}
					// そのマスに対する候補を検索
					VoxasSikaku pickup = null;
					boolean only = true;
					for (VoxasSikaku cand : squareCand) {
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
			return true;
		}

		/**
		 * 四角形の形状が条件を満たさない候補を消す。すでに満たさない状況であればfalseを返す。
		 */
		public boolean typeSolve() {
			for (VoxasSikaku fixed : squareFixed) {
				Set<Position> yokoWall = fixed.getYokoWall();
				for (Position oneYokoWall : yokoWall) {
					if (firstYokoWall.get(oneYokoWall) != null) {
						if (firstYokoWall.get(oneYokoWall) == 2) {
							// 黒丸=typeの合計が5でなければダメ
							for (VoxasSikaku otherFixed : squareFixed) {
								if (fixed != otherFixed && otherFixed.getYokoWall().contains(oneYokoWall)
										&& fixed.type + otherFixed.type != 5) {
									return false;
								}
							}
							for (Iterator<VoxasSikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
								VoxasSikaku oneCand = iterator.next();
								if (oneCand.getYokoWall().contains(oneYokoWall) && fixed.type + oneCand.type != 5) {
									iterator.remove();
								}
							}
						} else if (firstYokoWall.get(oneYokoWall) == 3) {
							// 灰丸=typeが同じ、または合計5はダメ
							for (VoxasSikaku otherFixed : squareFixed) {
								if (fixed != otherFixed && otherFixed.getYokoWall().contains(oneYokoWall)
										&& (fixed.type == otherFixed.type || fixed.type + otherFixed.type == 5)) {
									return false;
								}
							}
							for (Iterator<VoxasSikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
								VoxasSikaku oneCand = iterator.next();
								if (oneCand.getYokoWall().contains(oneYokoWall)
										&& (fixed.type == oneCand.type || fixed.type + oneCand.type == 5)) {
									iterator.remove();
								}
							}
						} else if (firstYokoWall.get(oneYokoWall) == 4) {
							// 白丸=typeが同じでないとダメ
							for (VoxasSikaku otherFixed : squareFixed) {
								if (fixed != otherFixed && otherFixed.getYokoWall().contains(oneYokoWall)
										&& fixed.type != otherFixed.type) {
									return false;
								}
							}
							for (Iterator<VoxasSikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
								VoxasSikaku oneCand = iterator.next();
								if (oneCand.getYokoWall().contains(oneYokoWall) && fixed.type != oneCand.type) {
									iterator.remove();
								}
							}
						}
					}
				}
				Set<Position> tateWall = fixed.getTateWall();
				for (Position oneTateWall : tateWall) {
					if (firstTateWall.get(oneTateWall) != null) {
						if (firstTateWall.get(oneTateWall) == 2) {
							// 黒丸=typeの合計が5でなければダメ
							for (VoxasSikaku otherFixed : squareFixed) {
								if (fixed != otherFixed && otherFixed.getTateWall().contains(oneTateWall)
										&& fixed.type + otherFixed.type != 5) {
									return false;
								}
							}
							for (Iterator<VoxasSikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
								VoxasSikaku oneCand = iterator.next();
								if (oneCand.getTateWall().contains(oneTateWall) && fixed.type + oneCand.type != 5) {
									iterator.remove();
								}
							}
						} else if (firstTateWall.get(oneTateWall) == 3) {
							// 灰丸=typeが同じ、または合計5はダメ
							for (VoxasSikaku otherFixed : squareFixed) {
								if (fixed != otherFixed && otherFixed.getTateWall().contains(oneTateWall)
										&& (fixed.type == otherFixed.type || fixed.type + otherFixed.type == 5)) {
									return false;
								}
							}
							for (Iterator<VoxasSikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
								VoxasSikaku oneCand = iterator.next();
								if (oneCand.getTateWall().contains(oneTateWall)
										&& (fixed.type == oneCand.type || fixed.type + oneCand.type == 5)) {
									iterator.remove();
								}
							}
						} else if (firstTateWall.get(oneTateWall) == 4) {
							// 白丸=typeが同じでないとダメ
							for (VoxasSikaku otherFixed : squareFixed) {
								if (fixed != otherFixed && otherFixed.getTateWall().contains(oneTateWall)
										&& fixed.type != otherFixed.type) {
									return false;
								}
							}
							for (Iterator<VoxasSikaku> iterator = squareCand.iterator(); iterator.hasNext();) {
								VoxasSikaku oneCand = iterator.next();
								if (oneCand.getTateWall().contains(oneTateWall) && fixed.type != oneCand.type) {
									iterator.remove();
								}
							}
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

	public VoxasSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public VoxasSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	//
	public static void main(String[] args) {
		String url = "https://pzprxs.vercel.app/p?voxas/8/8/ialdgddgdlbgcbgclaobh3dg3dgalag1bg2chdl"; // urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 2]);
		int width = Integer.parseInt(params[params.length - 3]);
		String param = params[params.length - 1];
		System.out.println(new VoxasSolver(height, width, param).solve());
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
		int level = (int) Math.sqrt(count * 2 / 3);
		return "解けました。推定難易度:" + Difficulty.getByCount(count * 2).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (Iterator<VoxasSikaku> iterator = field.squareCand.iterator(); iterator.hasNext();) {
			count++;
			VoxasSikaku oneCand = iterator.next();
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

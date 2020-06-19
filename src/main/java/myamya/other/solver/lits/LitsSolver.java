package myamya.other.solver.lits;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class LitsSolver implements Solver {

	enum LITS {
		/**
		 * ■
		 * ■■■
		 */
		L1(Arrays.asList(
				new Position[] { new Position(0, 0), new Position(1, 0), new Position(1, 1), new Position(1, 2) }), 1,
				2),
		/**
		 * ■■
		 * ■
		 * ■
		 */
		L2(Arrays.asList(
				new Position[] { new Position(0, 0), new Position(0, 1), new Position(1, 0), new Position(2, 0) }), 2,
				1),
		/**
		 * ■■■
		 * 　　■
		 */
		L3(Arrays.asList(
				new Position[] { new Position(0, 0), new Position(0, 1), new Position(0, 2), new Position(1, 2) }), 1,
				2),
		/**
		 * 　■
		 * 　■
		 * ■■
		 */
		L4(Arrays.asList(
				new Position[] { new Position(0, 1), new Position(1, 1), new Position(2, 1), new Position(2, 0) }), 2,
				1),
		/**
		 * 　　■
		 * ■■■
		 */
		L5(Arrays.asList(
				new Position[] { new Position(0, 2), new Position(1, 2), new Position(1, 1), new Position(1, 0) }), 1,
				2),
		/**
		 * ■
		 * ■
		 * ■■
		 */
		L6(Arrays.asList(
				new Position[] { new Position(0, 0), new Position(1, 0), new Position(2, 0), new Position(2, 1) }), 2,
				1),
		/**
		 * ■■■
		 * ■
		 */
		L7(Arrays.asList(
				new Position[] { new Position(0, 0), new Position(0, 1), new Position(0, 2), new Position(1, 0) }), 1,
				2),
		/**
		 * ■■
		 * 　■
		 * 　■
		 */
		L8(Arrays.asList(
				new Position[] { new Position(0, 0), new Position(0, 1), new Position(1, 1), new Position(2, 1) }), 2,
				1),

		/**
		 * ■
		 * ■
		 * ■
		 * ■
		 */
		I1(Arrays.asList(
				new Position[] { new Position(0, 0), new Position(1, 0), new Position(2, 0), new Position(3, 0) }), 3,
				0),

		/**
		 * ■■■■
		 */
		I2(Arrays.asList(
				new Position[] { new Position(0, 0), new Position(0, 1), new Position(0, 2), new Position(0, 3) }), 0,
				3),

		/**
		 * ■■■
		 * 　■
		 */
		T1(Arrays.asList(
				new Position[] { new Position(0, 0), new Position(0, 1), new Position(0, 2), new Position(1, 1) }), 1,
				2),

		/**
		 * 　■
		 * ■■
		 * 　■
		 */
		T2(Arrays.asList(
				new Position[] { new Position(0, 1), new Position(1, 0), new Position(1, 1), new Position(2, 1) }), 2,
				1),

		/**
		 * 　■
		 * ■■■
		 */
		T3(Arrays.asList(
				new Position[] { new Position(0, 1), new Position(1, 0), new Position(1, 1), new Position(1, 2) }), 1,
				2),

		/**
		 * ■
		 * ■■
		 * ■
		 */
		T4(Arrays.asList(
				new Position[] { new Position(0, 0), new Position(1, 0), new Position(1, 1), new Position(2, 0) }), 2,
				1),
		/**
		 * ■■
		 * 　■■
		 */
		S1(Arrays.asList(
				new Position[] { new Position(0, 0), new Position(0, 1), new Position(1, 1), new Position(1, 2) }), 1,
				2),

		/**
		 * 　■
		 * ■■
		 * ■　
		 */
		S2(Arrays.asList(
				new Position[] { new Position(0, 1), new Position(1, 1), new Position(1, 0), new Position(2, 0) }), 2,
				1),

		/**
		 * 　■■
		 * ■■
		 */
		S3(Arrays.asList(
				new Position[] { new Position(0, 2), new Position(0, 1), new Position(1, 1), new Position(1, 0) }), 1,
				2),

		/**
		 * ■　
		 * ■■
		 * 　■
		 */
		S4(Arrays.asList(
				new Position[] { new Position(0, 1), new Position(1, 1), new Position(1, 0), new Position(2, 0) }), 2,
				1),;

		List<Position> positionList; //基準点から見た位置リスト
		int plusY; // 基準点から見てY方向に何マス必要か
		int plusX; // 基準点から見てX方向に何マス必要か

		LITS(List<Position> positionList, int plusY, int plusX) {
			this.positionList = positionList;
			this.plusY = plusY;
			this.plusX = plusX;
		}
	}

	static class ExtendedField extends LitsSolver.Field {
		public ExtendedField(Field other) {
			super(other, true);
		}

		public ExtendedField(int height, int width) {
			super(height, width);
		}

		/**
		 * 作問段階では2x2チェック、同一形隣接チェックのみする
		 */
		protected boolean solveAndCheck() {
			if (!pondSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			return true;
		}
	}

	public static class LitsGenerator implements Generator {

		static class LitsSolverForGenerator extends LitsSolver {
			private final int limit;

			public LitsSolverForGenerator(Field field, int limit) {
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

		public LitsGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new LitsGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			ExtendedField wkField = new ExtendedField(height, width);
			// Litsの一覧作成
			List<Set<Position>> litsList = new ArrayList<>();
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					for (LITS lits : LITS.values()) {
						if (yIndex + lits.plusY < wkField.getYLength() && xIndex + lits.plusX < wkField.getXLength()) {
							Set<Position> wkSet = new HashSet<>();
							for (Position pos : lits.positionList) {
								wkSet.add(new Position(yIndex + pos.getyIndex(), xIndex + pos.getxIndex()));
							}
							litsList.add(wkSet);
						}
					}
				}
			}
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				while (true) {
					boolean isOk = false;
					Collections.shuffle(litsList);
					for (Set<Position> lits : litsList) {
						// ランダムにLITS配置
						//	if (Math.random() * 2 < 1) {
						isOk = true;
						for (Set<Position> room : wkField.rooms) {
							Set<Position> wkLits = new HashSet<>(lits);
							wkLits.retainAll(room);
							if (!wkLits.isEmpty()) {
								// 部屋ががかぶっている場合は置かない
								isOk = false;
								break;
							}
						}
						if (isOk) {
							ExtendedField virtual = new ExtendedField(wkField);
							virtual.rooms.add(new HashSet<>(lits));
							for (Position pos : lits) {
								virtual.masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
							}
							// 置いて矛盾がなければ、その部屋を確定
							if (virtual.solveAndCheck()) {
								wkField.masu = virtual.masu;
								wkField.rooms = virtual.rooms;
							}
						}
						//}

					}
					for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
						for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
							if (wkField.masu[yIndex][xIndex] != Masu.BLACK) {
								wkField.masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
					if (!wkField.connectSolve()) {
						// Lits置きが終わったら接続チェック。falseなら作りなおし
						wkField = new ExtendedField(height, width);
					} else {
						break;
					}
				}
				// 部屋拡張部
				List<Position> yetPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.masu[yIndex][xIndex] != Masu.BLACK) {
							yetPosList.add(new Position(yIndex, xIndex));
						}
					}
				}
				while (true) {
					if (yetPosList.isEmpty()) {
						break;
					}
					// 基準部屋をランダムで選ぶ
					Set<Position> room = wkField.rooms.get((int) (Math.random() * wkField.rooms.size()));
					// 基準マスをランダムで選ぶ
					Position targetPos = new ArrayList<Position>(room).get((int) (Math.random() * room.size()));
					// 基準マスからランダムで1マス伸ばす
					List<Position> candPosList = new ArrayList<>();
					if (targetPos.getyIndex() != 0) {
						Position nextPos = new Position(targetPos.getyIndex() - 1, targetPos.getxIndex());
						if (yetPosList.contains(nextPos)) {
							candPosList.add(nextPos);
						}
					}
					if (targetPos.getxIndex() != wkField.getXLength() - 1) {
						Position nextPos = new Position(targetPos.getyIndex(), targetPos.getxIndex() + 1);
						if (yetPosList.contains(nextPos)) {
							candPosList.add(nextPos);
						}
					}
					if (targetPos.getyIndex() != wkField.getYLength() - 1) {
						Position nextPos = new Position(targetPos.getyIndex() + 1, targetPos.getxIndex());
						if (yetPosList.contains(nextPos)) {
							candPosList.add(nextPos);
						}
					}
					if (targetPos.getxIndex() != 0) {
						Position nextPos = new Position(targetPos.getyIndex(), targetPos.getxIndex() - 1);
						if (yetPosList.contains(nextPos)) {
							candPosList.add(nextPos);
						}
					}
					// どの方向にも伸ばせなかったら基準マス選び直し。
					if (candPosList.isEmpty()) {
						continue;
					}
					Position nextPos = candPosList.get((int) (Math.random() * candPosList.size()));
					room.add(nextPos);
					yetPosList.remove(nextPos);
				}
				// 壁セット(問題を解くのに壁の情報も使ってるため)
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength() - 1; xIndex++) {
						Position pos = new Position(yIndex, xIndex);
						boolean isWall = true;
						for (Set<Position> room : wkField.rooms) {
							if (room.contains(pos)) {
								Position rightPos = new Position(yIndex, xIndex + 1);
								if (room.contains(rightPos)) {
									isWall = false;
									break;
								}
							}
						}
						wkField.yokoWall[yIndex][xIndex] = isWall;
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength() - 1; yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						Position pos = new Position(yIndex, xIndex);
						boolean isWall = true;
						for (Set<Position> room : wkField.rooms) {
							if (room.contains(pos)) {
								Position downPos = new Position(yIndex + 1, xIndex);
								if (room.contains(downPos)) {
									isWall = false;
									break;
								}
							}
						}
						wkField.tateWall[yIndex][xIndex] = isWall;
					}
				}
				System.out.println(wkField);
				// マス戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				// 解けるかな？
				level = new LitsSolverForGenerator(wkField, 1000).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width);
				} else {
					break;
				}
			}
			level = (int) Math.sqrt(level * 5 / 3);
			String status = "Lv:" + level + "の問題を獲得！(部屋数：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
			StringBuilder sb = new StringBuilder();
			//			int baseSize = 20;
			//			int margin = 5;
			//			sb.append(
			//					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
			//							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
			//							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			//			// 数字描画
			//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
			//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
			//					if (wkField.getNumbers()[yIndex][xIndex] != null) {
			//						sb.append("<rect y=\"" + (yIndex * baseSize + 2 + margin)
			//								+ "\" x=\""
			//								+ (xIndex * baseSize + baseSize + 2)
			//								+ "\" width=\""
			//								+ (baseSize - 4)
			//								+ "\" height=\""
			//								+ (baseSize - 4)
			//								+ "\" fill=\"white\" stroke-width=\"1\" stroke=\"black\">"
			//								+ "\">"
			//								+ "</rect>");
			//						if (wkField.getNumbers()[yIndex][xIndex] != -1) {
			//							String numberStr = String.valueOf(wkField.getNumbers()[yIndex][xIndex]);
			//							int numIdx = HALF_NUMS.indexOf(numberStr);
			//							String masuStr = null;
			//							if (numIdx >= 0) {
			//								masuStr = FULL_NUMS.substring(numIdx / 2, numIdx / 2 + 1);
			//							} else {
			//								masuStr = numberStr;
			//							}
			//							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin)
			//									+ "\" x=\""
			//									+ (xIndex * baseSize + baseSize + 2)
			//									+ "\" font-size=\""
			//									+ (baseSize - 5)
			//									+ "\" textLength=\""
			//									+ (baseSize - 5)
			//									+ "\" lengthAdjust=\"spacingAndGlyphs\">"
			//									+ masuStr
			//									+ "</text>");
			//						}
			//					}
			//				}
			//			}
			//			// 横壁描画
			//			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
			//				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
			//					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
			//					sb.append("<line y1=\""
			//							+ (yIndex * baseSize + margin)
			//							+ "\" x1=\""
			//							+ (xIndex * baseSize + 2 * baseSize)
			//							+ "\" y2=\""
			//							+ (yIndex * baseSize + baseSize + margin)
			//							+ "\" x2=\""
			//							+ (xIndex * baseSize + 2 * baseSize)
			//							+ "\" stroke-width=\"1\" fill=\"none\"");
			//					if (oneYokoWall) {
			//						sb.append("stroke=\"#000\" ");
			//					} else {
			//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
			//					}
			//					sb.append(">"
			//							+ "</line>");
			//				}
			//			}
			//			// 縦壁描画
			//			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
			//				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
			//					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
			//					sb.append("<line y1=\""
			//							+ (yIndex * baseSize + baseSize + margin)
			//							+ "\" x1=\""
			//							+ (xIndex * baseSize + baseSize)
			//							+ "\" y2=\""
			//							+ (yIndex * baseSize + baseSize + margin)
			//							+ "\" x2=\""
			//							+ (xIndex * baseSize + baseSize + baseSize)
			//							+ "\" stroke-width=\"1\" fill=\"none\"");
			//					if (oneTateWall) {
			//						sb.append("stroke=\"#000\" ");
			//					} else {
			//						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
			//					}
			//					sb.append(">"
			//							+ "</line>");
			//				}
			//			}
			//			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			System.out.println(url);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}

	}

	/**
	 * テトロミノを示す
	 */
	enum Tetro {
		L, I, T, S;

		/**
		 * テトロミノの形状を判定する。LITSのどれでもなかったらnullを返す。
		 */
		public static Tetro getByPositionSet(Set<Position> posSet) {
			if (posSet.size() != 4) {
				return null;
			}
			TreeMap<Integer, Integer> yKeyTreeMap = new TreeMap<>();
			TreeMap<Integer, Integer> xKeyTreeMap = new TreeMap<>();
			for (Position Pos : posSet) {
				Integer ycnt = yKeyTreeMap.get(Pos.getyIndex());
				if (ycnt == null) {
					yKeyTreeMap.put(Pos.getyIndex(), 1);
				} else {
					yKeyTreeMap.put(Pos.getyIndex(), ycnt + 1);
				}
				Integer xcnt = xKeyTreeMap.get(Pos.getxIndex());
				if (xcnt == null) {
					xKeyTreeMap.put(Pos.getxIndex(), 1);
				} else {
					xKeyTreeMap.put(Pos.getxIndex(), xcnt + 1);
				}
			}
			if (yKeyTreeMap.size() == 4 || xKeyTreeMap.size() == 4) {
				return I;
			} else if (yKeyTreeMap.size() == 3 && xKeyTreeMap.size() == 2) {
				int y0 = (int) yKeyTreeMap.values().toArray()[0];
				int y2 = (int) yKeyTreeMap.values().toArray()[2];
				if (y0 == 2 || y2 == 2) {
					return L;
				} else {
					int x0 = (int) xKeyTreeMap.values().toArray()[0];
					int x1 = (int) xKeyTreeMap.values().toArray()[1];
					if (x0 == 3 || x1 == 3) {
						return T;
					} else {
						return S;
					}
				}
			} else if (yKeyTreeMap.size() == 2 && xKeyTreeMap.size() == 3) {
				int x0 = (int) xKeyTreeMap.values().toArray()[0];
				int x2 = (int) xKeyTreeMap.values().toArray()[2];
				if (x0 == 2 || x2 == 2) {
					return L;
				} else {
					int y0 = (int) yKeyTreeMap.values().toArray()[0];
					int y1 = (int) yKeyTreeMap.values().toArray()[1];
					if (y0 == 3 || y1 == 3) {
						return T;
					} else {
						return S;
					}
				}
			}
			return null;
		}
	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final String ALPHABET_AND_NUMBER = "0123456789abcdefghijklmnopqrstuvwxyz";
		static final int BLACK_CNT = 4;

		// マスの情報
		protected Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		protected final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		protected final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		protected List<Set<Position>> rooms;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://pzv.jp/p.html?lits/" + getXLength() + "/" + getYLength() + "/");
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
				if (yIndex1 != -1 && xIndex1 != -1 && isExistYokoWall(yIndex1, xIndex1)) {
					num = num + 16;
				}
				if (yIndex2 != -1 && xIndex2 != -1 && isExistYokoWall(yIndex2, xIndex2)) {
					num = num + 8;
				}
				if (yIndex3 != -1 && xIndex3 != -1 && isExistYokoWall(yIndex3, xIndex3)) {
					num = num + 4;
				}
				if (yIndex4 != -1 && xIndex4 != -1 && isExistYokoWall(yIndex4, xIndex4)) {
					num = num + 2;
				}
				if (yIndex5 != -1 && xIndex5 != -1 && isExistYokoWall(yIndex5, xIndex5)) {
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
				if (yIndex1 != -1 && xIndex1 != -1 && isExistTateWall(yIndex1, xIndex1)) {
					num = num + 16;
				}
				if (yIndex2 != -1 && xIndex2 != -1 && isExistTateWall(yIndex2, xIndex2)) {
					num = num + 8;
				}
				if (yIndex3 != -1 && xIndex3 != -1 && isExistTateWall(yIndex3, xIndex3)) {
					num = num + 4;
				}
				if (yIndex4 != -1 && xIndex4 != -1 && isExistTateWall(yIndex4, xIndex4)) {
					num = num + 2;
				}
				if (yIndex5 != -1 && xIndex5 != -1 && isExistTateWall(yIndex5, xIndex5)) {
					num = num + 1;
				}
				sb.append(ALPHABET_AND_NUMBER.substring(num, num + 1));
			}
			return sb.toString();
		}

		public String getHintCount() {
			return String.valueOf(rooms.size());
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		private boolean isExistYokoWall(int yIndex, int xIndex) {
			Position pos = new Position(yIndex, xIndex);
			for (Set<Position> room : rooms) {
				if (room.contains(pos)) {
					Position rightPos = new Position(yIndex, xIndex + 1);
					if (room.contains(rightPos)) {
						return false;
					}
				}
			}
			return true;
		}

		private boolean isExistTateWall(int yIndex, int xIndex) {
			Position pos = new Position(yIndex, xIndex);
			for (Set<Position> room : rooms) {
				if (room.contains(pos)) {
					Position downPos = new Position(yIndex + 1, xIndex);
					if (room.contains(downPos)) {
						return false;
					}
				}
			}
			return true;
		}

		public Field(int height, int width, List<Set<Position>> rooms) {
			masu = new Masu[height][width];
			this.rooms = rooms;
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = isExistYokoWall(yIndex, xIndex);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = isExistTateWall(yIndex, xIndex);
				}
			}
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			// パラメータを解釈して壁の有無を入れる
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * (getXLength() - 1); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * (getXLength() - 1)) - 1) {
					if (mod >= 0) {
						yokoWall[(cnt - mod + 0) / (getXLength() - 1)][(cnt - mod + 0) % (getXLength() - 1)] = bit / 16
								% 2 == 1;
					}
					if (mod >= 1) {
						yokoWall[(cnt - mod + 1) / (getXLength() - 1)][(cnt - mod + 1) % (getXLength() - 1)] = bit / 8
								% 2 == 1;
					}
					if (mod >= 2) {
						yokoWall[(cnt - mod + 2) / (getXLength() - 1)][(cnt - mod + 2) % (getXLength() - 1)] = bit / 4
								% 2 == 1;
					}
					if (mod >= 3) {
						yokoWall[(cnt - mod + 3) / (getXLength() - 1)][(cnt - mod + 3) % (getXLength() - 1)] = bit / 2
								% 2 == 1;
					}
					if (mod >= 4) {
						yokoWall[(cnt - mod + 4) / (getXLength() - 1)][(cnt - mod + 4) % (getXLength() - 1)] = bit / 1
								% 2 == 1;
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
						tateWall[(cnt - mod + 0) / getXLength()][(cnt - mod + 0) % getXLength()] = bit / 16 % 2 == 1;
					}
					if (mod >= 1) {
						tateWall[(cnt - mod + 1) / getXLength()][(cnt - mod + 1) % getXLength()] = bit / 8 % 2 == 1;
					}
					if (mod >= 2) {
						tateWall[(cnt - mod + 2) / getXLength()][(cnt - mod + 2) % getXLength()] = bit / 4 % 2 == 1;
					}
					if (mod >= 3) {
						tateWall[(cnt - mod + 3) / getXLength()][(cnt - mod + 3) % getXLength()] = bit / 2 % 2 == 1;
					}
					if (mod >= 4) {
						tateWall[(cnt - mod + 4) / getXLength()][(cnt - mod + 4) % getXLength()] = bit / 1 % 2 == 1;
					}
				}
			}
			// 縦と横の壁の関係からにょろっと部屋を決めていく
			rooms = new ArrayList<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					boolean alreadyRoomed = false;
					for (Set<Position> room : rooms) {
						if (room.contains(pos)) {
							alreadyRoomed = true;
							break;
						}
					}
					if (!alreadyRoomed) {
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinuePosSet(pos, continuePosSet);
						rooms.add(continuePosSet);
					}
				}
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			// 壁・部屋は参照渡しで使い回し(一度Fieldができたら変化しないはずなので。)
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
		}

		/**
		 * プレーンなフィールド生成
		 */
		public Field(int height, int width) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			rooms = new ArrayList<>();
		}

		/**
		 * ジェネレータ用 roomsをイミュータブルに
		 */
		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			// 壁・部屋は参照渡しで使い回し(一度Fieldができたら変化しないはずなので。)
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = new ArrayList<>(other.rooms);
		}

		// posを起点に上下左右に壁または白確定でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
		}

		// posを起点に上下左右に壁または白確定でないマスをposからのdistanceだけつなげていく。
		private void setContinuePosSetUseDistance(Position pos, Set<Position> continuePosSet, int distance,
				Direction from) {
			if (distance == 0) {
				return;
			}
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!tateWall[pos.getyIndex() - 1][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSetUseDistance(nextPos, continuePosSet, distance - 1, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!yokoWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSetUseDistance(nextPos, continuePosSet, distance - 1, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!tateWall[pos.getyIndex()][pos.getxIndex()]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSetUseDistance(nextPos, continuePosSet, distance - 1, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!yokoWall[pos.getyIndex()][pos.getxIndex() - 1]
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSetUseDistance(nextPos, continuePosSet, distance - 1, Direction.RIGHT);
				}
			}
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
					sb.append(masu[yIndex][xIndex]);
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex] == true ? "□" : "　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex] == true ? "□" : "　");
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
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 部屋のマスを埋める。黒マス不足・過剰はfalseを返す。
		 */
		public boolean roomSolve() {
			for (Set<Position> room : rooms) {
				// 部屋に対する調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						blackCnt++;
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < BLACK_CNT) {
					// 黒マス不足
					return false;
				}
				// 置かねばならない黒マスの数
				int retainBlackCnt = BLACK_CNT - blackCnt;
				if (retainBlackCnt < 0) {
					// 黒マス超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 黒マス数が既に部屋の黒マス数に等しければ、部屋の他のマスは白マス
					for (Position pos : room) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない黒マスの数に等しければ、未確定マスは黒マス
					for (Position pos : room) {
						if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 置くと池ができるマスを白マスにする。
		 * 既に池ができている場合falseを返す。
		 */
		public boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE && masu4 == Masu.BLACK) {
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
				}
			}
			return true;
		}

		/**
		 * 既にある黒から届かない領域を白マスにする。
		 * 既にある黒から届かない場所に黒を見つけた場合falseを返す。
		 */
		public boolean capacitySolve() {
			for (Set<Position> room : rooms) {
				Set<Position> alreadySurvey = new HashSet<>();
				for (Position pos : room) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK && !alreadySurvey.contains(pos)) {
						if (!alreadySurvey.isEmpty()) {
							return false;
						}
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						setContinuePosSetUseDistance(pos, continuePosSet, BLACK_CNT - 1, null);
						alreadySurvey.addAll(continuePosSet);
					}
				}
				if (!alreadySurvey.isEmpty()) {
					for (Position pos : room) {
						if (!alreadySurvey.contains(pos)) {
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスがひとつながりにならない場合Falseを返す。
		 * 今までのロジックより高速に動きます。
		 */
		public boolean connectSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position blackPos = new Position(yIndex, xIndex);
						if (blackPosSet.size() == 0) {
							blackPosSet.add(blackPos);
							setContinuePosSet(blackPos, blackPosSet, null);
						} else {
							if (!blackPosSet.contains(blackPos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に白確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 別部屋に同じ形のテトロミノが隣接する場合Falseを返す。
		 */
		public boolean nextSolve() {
			for (Set<Position> room : rooms) {
				Set<Position> myRoomBlackSet = new HashSet<>();
				Set<Position> nextRoomPosSet = new HashSet<>();
				for (Position pos : room) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						myRoomBlackSet.add(pos);
						if (pos.getyIndex() != 0) {
							Position blackPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
							if (!room.contains(blackPos)
									&& masu[blackPos.getyIndex()][blackPos.getxIndex()] == Masu.BLACK) {
								// 隣接マスが黒マスの場合のみ調査
								nextRoomPosSet.add(blackPos);
							}
						}
						if (pos.getxIndex() != getXLength() - 1) {
							Position blackPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
							if (!room.contains(blackPos)
									&& masu[blackPos.getyIndex()][blackPos.getxIndex()] == Masu.BLACK) {
								nextRoomPosSet.add(blackPos);
							}
						}
						if (pos.getyIndex() != getYLength() - 1) {
							Position blackPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
							if (!room.contains(blackPos)
									&& masu[blackPos.getyIndex()][blackPos.getxIndex()] == Masu.BLACK) {
								nextRoomPosSet.add(blackPos);
							}
						}
						if (pos.getxIndex() != 0) {
							Position blackPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
							if (!room.contains(blackPos)
									&& masu[blackPos.getyIndex()][blackPos.getxIndex()] == Masu.BLACK) {
								nextRoomPosSet.add(blackPos);
							}
						}
					}
				}
				// 自分の部屋の黒が4マスの場合のみ調査
				if (myRoomBlackSet.size() == BLACK_CNT) {
					for (Position nextRoomPos : nextRoomPosSet) {
						for (Set<Position> otherRoom : rooms) {
							if (otherRoom.contains(nextRoomPos)) {
								Set<Position> otherRoomBlackSet = new HashSet<>();
								for (Position pos : otherRoom) {
									if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
										otherRoomBlackSet.add(pos);
									}
								}
								// 4マスで同じ形ならfalse
								if (otherRoomBlackSet.size() == BLACK_CNT
										&& Tetro.getByPositionSet(myRoomBlackSet) == Tetro
												.getByPositionSet(otherRoomBlackSet)) {
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
		 * 各種チェックを1セット実行
		 */
		protected boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
			if (!capacitySolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
				if (!nextSolve()) {
					return false;
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
	protected int count = 0;

	public LitsSolver(int height, int width, String param) {
		field = new Field(height, width, param);
	}

	public LitsSolver(Field field) {
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
		System.out.println(new LitsSolver(height, width, param).solve());
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
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 2).toString();
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
		String befStr = virtual.getStateDump();
		boolean allowBlack = virtual.solveAndCheck()
				&& (befStr.equals(virtual.getStateDump()) || virtual.solveAndCheck());
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.masu[yIndex][xIndex] = Masu.NOT_BLACK;
		befStr = virtual2.getStateDump();
		boolean allowNotBlack = virtual2.solveAndCheck()
				&& (befStr.equals(virtual2.getStateDump()) || virtual2.solveAndCheck());
		if (allowNotBlack && recursive > 0) {
			if (!candSolve(virtual2, recursive - 1)) {
				allowNotBlack = false;
			}
		}
		if (!allowBlack && !allowNotBlack) {
			return false;
		} else if (!allowBlack) {
			field.masu = virtual2.masu;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
		}
		return true;
	}

}
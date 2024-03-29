package myamya.other.solver.tajmahal;

import java.awt.geom.Line2D;
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
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class TajmahalSolver implements Solver {
	public static class TajmahalGenerator implements Generator {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static class TajmahalSolverForGenerator extends TajmahalSolver {
			private final int limit;

			public TajmahalSolverForGenerator(Field field, int limit) {
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

		static class ExtendedField extends TajmahalSolver.Field {
			public ExtendedField(Field other) {
				super(other);
			}

			int maxSize = 0;

			// ヒントを無視して候補初期化
			public ExtendedField(int height, int width) {
				super(height, width);
				for (int yIndex = 1; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 1; xIndex < getXLength(); xIndex++) {
						if ((yIndex + xIndex) % 2 != 0) {
							continue;
						}
						Position pos = new Position(yIndex, xIndex);
						if (pos.getyIndex() % 2 == 0) {
							// 格子点の建物は、2
							for (int y = 2; true; y = y + 2) {
								boolean isFirstBreak = false;
								for (int x = 0; true; x = x + 2) {
									if (pos.getyIndex() - y < 0 || pos.getyIndex() - x < 0
											|| pos.getyIndex() + y >= yLength || pos.getyIndex() + x >= yLength
											|| pos.getxIndex() - x < 0 || pos.getxIndex() + y >= xLength
											|| pos.getxIndex() + x >= xLength || pos.getxIndex() - y < 0) {
										if (x == 0) {
											isFirstBreak = true;
										}
										break;
									}
									List<Line2D> myLineList = new ArrayList<Line2D>();
									myLineList.add(new Line2D.Double(pos.getxIndex() - x, pos.getyIndex() - y,
											pos.getxIndex() + y, pos.getyIndex() - x));
									myLineList.add(new Line2D.Double(pos.getxIndex() + y, pos.getyIndex() - x,
											pos.getxIndex() + x, pos.getyIndex() + y));
									myLineList.add(new Line2D.Double(pos.getxIndex() + x, pos.getyIndex() + y,
											pos.getxIndex() - y, pos.getyIndex() + x));
									myLineList.add(new Line2D.Double(pos.getxIndex() - y, pos.getyIndex() + x,
											pos.getxIndex() - x, pos.getyIndex() - y));
									if (maxSize < y + x) {
										maxSize = y + x;
									}
									squareCand.add(new Tatemono(pos, myLineList, y + x));
								}
								if (isFirstBreak) {
									break;
								}
							}
						} else {
							for (int y = 1; true; y = y + 2) {
								boolean isFirstBreak = false;
								for (int x = 1; true; x = x + 2) {
									if (pos.getyIndex() - y < 0 || pos.getyIndex() - x < 0
											|| pos.getyIndex() + y >= yLength || pos.getyIndex() + x >= yLength
											|| pos.getxIndex() - x < 0 || pos.getxIndex() + y >= xLength
											|| pos.getxIndex() + x >= xLength || pos.getxIndex() - y < 0) {
										if (x == 1) {
											isFirstBreak = true;
										}
										break;
									}
									List<Line2D> myLineList = new ArrayList<Line2D>();
									myLineList.add(new Line2D.Double(pos.getxIndex() - x, pos.getyIndex() - y,
											pos.getxIndex() + y, pos.getyIndex() - x));
									myLineList.add(new Line2D.Double(pos.getxIndex() + y, pos.getyIndex() - x,
											pos.getxIndex() + x, pos.getyIndex() + y));
									myLineList.add(new Line2D.Double(pos.getxIndex() + x, pos.getyIndex() + y,
											pos.getxIndex() - y, pos.getyIndex() + x));
									myLineList.add(new Line2D.Double(pos.getxIndex() - y, pos.getyIndex() + x,
											pos.getxIndex() - x, pos.getyIndex() - y));
									if (maxSize < y + x) {
										maxSize = y + x;
									}
									squareCand.add(new Tatemono(pos, myLineList, y + x));
								}
								if (isFirstBreak) {
									break;
								}
							}
						}
					}
				}
			}

			public ExtendedField(int height, int width, ArrayList<Tatemono> cacheCand) {
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

		public TajmahalGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new TajmahalGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			ExtendedField wkField = new ExtendedField(height, width);
			ArrayList<Tatemono> cacheCand = new ArrayList<>(wkField.squareCand);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < cacheCand.size(); i++) {
				indexList.add(i);
			}
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				Collections.shuffle(indexList);
				// 問題生成部
				while (!wkField.isSolved()) {
					Tatemono oneCand = cacheCand.get(index);
					if (wkField.squareCand.contains(oneCand)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						if (Math.random() * (height + width) < oneCand.getSize()) {
							numIdxList.add(1);
							numIdxList.add(0);
						} else {
							numIdxList.add(0);
							numIdxList.add(1);
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
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// ヒントを埋める
				List<Position> posList = new ArrayList<>();
				for (Tatemono fixed : wkField.squareFixed) {
					int cnt = 0;
					for (Tatemono otherFixed : wkField.squareFixed) {
						if (fixed != otherFixed) {
							if (otherFixed.isCross(fixed)) {
								cnt++;
							}
						}
					}
					if (cnt == 0) {
						wkField.squareFixed.clear();
						break;
					}
					wkField.numbersMap.put(fixed.getCenterPos(), cnt);
					posList.add(fixed.getCenterPos());
				}
				if (wkField.squareFixed.isEmpty()) {
					// 建物の数0を禁止する
					wkField = new ExtendedField(height, width, cacheCand);
					Collections.shuffle(indexList);
					index = 0;
					continue;
				}
				// 候補を戻す
				wkField.initCand();
				// 解けるかな？
				level = new TajmahalSolverForGenerator(wkField, 500).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new ExtendedField(height, width, cacheCand);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(posList);
					for (Position pos : posList) {
						Field virtual = new Field(wkField);
						virtual.numbersMap.put(pos, 0);
						int solveResult = new TajmahalSolverForGenerator(virtual, 500).solve2();
						if (solveResult != -1) {
							wkField.numbersMap.put(pos, 0);
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(建物/数字：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">pzprxsで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 10;
			int margin = 5;
			sb.append("<svg xmlns=\"http://www.w3.org/2000/svg\" " + "height=\""
					+ (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
					+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 点・●描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					Integer number = wkField.getNumbersMap().get(new Position(yIndex, xIndex));
					if (number == null) {
						if (xIndex % 2 == 0 && yIndex % 2 == 0) {
							sb.append("<circle cy=\"" + (yIndex * baseSize + margin) + "\" cx=\""
									+ (xIndex * baseSize + baseSize) + "\" r=\"" + 1
									+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
						} else {
							sb.append("　");
						}
					} else {
						sb.append("<circle cy=\"" + (yIndex * baseSize + margin) + "\" cx=\""
								+ (xIndex * baseSize + baseSize) + "\" r=\"" + (baseSize - 2)
								+ "\" fill=\"black\", stroke=\"black\">" + "</circle>");
						if (number != 0) {
							String numberStr = String.valueOf(number);
							int wkIdx = HALF_NUMS.indexOf(numberStr);
							String masuStr = null;
							if (wkIdx >= 0) {
								masuStr = FULL_NUMS.substring(wkIdx / 2, wkIdx / 2 + 1);
							} else {
								masuStr = numberStr;
							}
							sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin) + "\" x=\""
									+ (xIndex * baseSize + 2) + "\" font-size=\"" + (baseSize + 5) + "\" textLength=\""
									+ (baseSize + 5) + "\" fill=\"white\", lengthAdjust=\"spacingAndGlyphs\">" + masuStr
									+ "</text>");
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

	// 建物
	public static class Tatemono {
		// 自身が作る直線のリスト。処理速度を考えて最初に生成
		private final List<Line2D> myLineList;

		public List<Line2D> getMyLineList() {
			return myLineList;
		}

		// 中心のマス
		private final Position centerPos;

		// サイズ。中心からのx、yの距離の合計であり厳密な面積ではないので注意
		private final int size;

		Tatemono(Position centerPos, List<Line2D> myLineList, int size) {
			this.centerPos = centerPos;
			this.myLineList = myLineList;
			this.size = size;
		}

		/**
		 * 自身とotherが重なっている、または辺で接している場合trueを返す。
		 */
		private boolean isDuplicate(Tatemono other) {
			for (Line2D myLine : myLineList) {
				for (Line2D otherLine : other.myLineList) {
					if (myLine.intersectsLine(otherLine)) {
						// 接していても、以下の条件の場合は頂点隣接のため継続。
						// ・myLineとotherLineに同じ座標の点がある。
						// ・myLineがotherの中心に隣接しない
						// ・myLine、otherLineともに、同じ座標でない方の点が、他方の線と隣接しない。
						Line2D myPoint = null;
						Line2D otherPoint = null;
						Line2D centerPoint = new Line2D.Double(other.getCenterPos().getxIndex(),
								other.getCenterPos().getyIndex(), other.getCenterPos().getxIndex(),
								other.getCenterPos().getyIndex());
						if (myLine.getY1() == otherLine.getY1() && myLine.getX1() == otherLine.getX1()) {
							myPoint = new Line2D.Double(myLine.getX2(), myLine.getY2(), myLine.getX2(), myLine.getY2());
							otherPoint = new Line2D.Double(otherLine.getX2(), otherLine.getY2(), otherLine.getX2(),
									otherLine.getY2());
						} else if (myLine.getY1() == otherLine.getY2() && myLine.getX1() == otherLine.getX2()) {
							myPoint = new Line2D.Double(myLine.getX2(), myLine.getY2(), myLine.getX2(), myLine.getY2());
							otherPoint = new Line2D.Double(otherLine.getX1(), otherLine.getY1(), otherLine.getX1(),
									otherLine.getY1());
						} else if (myLine.getY2() == otherLine.getY1() && myLine.getX2() == otherLine.getX1()) {
							myPoint = new Line2D.Double(myLine.getX1(), myLine.getY1(), myLine.getX1(), myLine.getY1());
							otherPoint = new Line2D.Double(otherLine.getX2(), otherLine.getY2(), otherLine.getX2(),
									otherLine.getY2());
						} else if (myLine.getY2() == otherLine.getY2() && myLine.getX2() == otherLine.getX2()) {
							myPoint = new Line2D.Double(myLine.getX1(), myLine.getY1(), myLine.getX1(), myLine.getY1());
							otherPoint = new Line2D.Double(otherLine.getX1(), otherLine.getY1(), otherLine.getX1(),
									otherLine.getY1());
						}
						if (myPoint == null || otherPoint == null || centerPoint.intersectsLine(myLine)
								|| myPoint.intersectsLine(otherLine) || otherPoint.intersectsLine(myLine)) {
							return true;
						}
					}
				}
			}
			return false;
		}

		/**
		 * 自身とotherが頂点で接している場合trueを返す。
		 */
		private boolean isCross(Tatemono other) {
			boolean crossFlag = false;
			for (Line2D myLine : myLineList) {
				for (Line2D otherLine : other.myLineList) {
					if (myLine.intersectsLine(otherLine)) {
						Line2D myPoint = null;
						Line2D otherPoint = null;
						Line2D centerPoint = new Line2D.Double(other.getCenterPos().getxIndex(),
								other.getCenterPos().getyIndex(), other.getCenterPos().getxIndex(),
								other.getCenterPos().getyIndex());
						if (myLine.getY1() == otherLine.getY1() && myLine.getX1() == otherLine.getX1()) {
							myPoint = new Line2D.Double(myLine.getX2(), myLine.getY2(), myLine.getX2(), myLine.getY2());
							otherPoint = new Line2D.Double(otherLine.getX2(), otherLine.getY2(), otherLine.getX2(),
									otherLine.getY2());
						} else if (myLine.getY1() == otherLine.getY2() && myLine.getX1() == otherLine.getX2()) {
							myPoint = new Line2D.Double(myLine.getX2(), myLine.getY2(), myLine.getX2(), myLine.getY2());
							otherPoint = new Line2D.Double(otherLine.getX1(), otherLine.getY1(), otherLine.getX1(),
									otherLine.getY1());
						} else if (myLine.getY2() == otherLine.getY1() && myLine.getX2() == otherLine.getX1()) {
							myPoint = new Line2D.Double(myLine.getX1(), myLine.getY1(), myLine.getX1(), myLine.getY1());
							otherPoint = new Line2D.Double(otherLine.getX2(), otherLine.getY2(), otherLine.getX2(),
									otherLine.getY2());
						} else if (myLine.getY2() == otherLine.getY2() && myLine.getX2() == otherLine.getX2()) {
							myPoint = new Line2D.Double(myLine.getX1(), myLine.getY1(), myLine.getX1(), myLine.getY1());
							otherPoint = new Line2D.Double(otherLine.getX1(), otherLine.getY1(), otherLine.getX1(),
									otherLine.getY1());
						}
						if (myPoint == null || otherPoint == null || centerPoint.intersectsLine(myLine)
								|| myPoint.intersectsLine(otherLine) || otherPoint.intersectsLine(myLine)) {
							return false;
						} else {
							crossFlag = true;
						}
					}
				}
			}
			return crossFlag;
		}

		public Position getCenterPos() {
			return centerPos;
		}

		public int getSize() {
			return size;
		}
	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		protected final int yLength;
		protected final int xLength;

		// 表出数字
		protected final Map<Position, Integer> numbersMap;

		public Map<Position, Integer> getNumbersMap() {
			return numbersMap;
		}

		// 四角の配置の候補
		protected List<Tatemono> squareCand;
		// 確定した四角候補
		protected List<Tatemono> squareFixed;

		public List<Tatemono> getSquareFixed() {
			return squareFixed;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("https://pzprxs.vercel.app/p?tajmahal/" + ((getXLength() - 1) / 2) + "/"
					+ ((getYLength() - 1) / 2) + "/");
			// 横壁処理
			int interval = -1;
			for (int yIndex = 1; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 1; xIndex < getXLength() - 1; xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					Integer number = numbersMap.get(pos);
					if (number != null) {
						if (interval >= 0) {
							sb.append(ALPHABET.charAt(interval));
							interval = -1;
						}
						sb.append(number);
					} else {
						if (interval == 25) {
							sb.append("z");
							interval = -1;
						}
						interval++;
					}
				}
			}
			// 最後の一文字
			if (interval >= 0) {
				sb.append(ALPHABET.charAt(interval));
				interval = -1;
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public String getHintCount() {
			int numberCnt = 0;
			for (Entry<Position, Integer> entry : numbersMap.entrySet()) {
				if (entry.getValue() > 0) {
					numberCnt++;
				}
			}
			return numbersMap.size() + "/" + numberCnt;
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
			yLength = height * 2 + 1;
			xLength = width * 2 + 1;
			numbersMap = new HashMap<>();
			squareCand = new ArrayList<>();
			squareFixed = new ArrayList<>();
		}

		public Field(int height, int width, ArrayList<Tatemono> cacheCand) {
			yLength = height * 2 + 1;
			xLength = width * 2 + 1;
			numbersMap = new HashMap<>();
			squareCand = new ArrayList<>(cacheCand);
			squareFixed = new ArrayList<>();
		}

		/**
		 * 部屋のきりかたの候補を初期化する。 numbersMaplが決まった後に呼ぶ必要あり
		 */
		protected void initCand() {
			squareCand = makeSquareCandBase();
			squareFixed = new ArrayList<>();
		}

		/**
		 * 部屋の切り方の候補を作成する。
		 */
		protected List<Tatemono> makeSquareCandBase() {
			List<Tatemono> squareCandBase = new ArrayList<>();
			for (Entry<Position, Integer> entry : numbersMap.entrySet()) {
				Position pos = entry.getKey();
				if (pos.getyIndex() % 2 == 0) {
					// 格子点の建物は、2
					for (int y = 2; true; y = y + 2) {
						boolean isFirstBreak = false;
						for (int x = 0; true; x = x + 2) {
							if (pos.getyIndex() - y < 0 || pos.getyIndex() - x < 0 || pos.getyIndex() + y >= yLength
									|| pos.getyIndex() + x >= yLength || pos.getxIndex() - x < 0
									|| pos.getxIndex() + y >= xLength || pos.getxIndex() + x >= xLength
									|| pos.getxIndex() - y < 0) {
								if (x == 0) {
									isFirstBreak = true;
								}
								break;
							}
							List<Line2D> myLineList = new ArrayList<Line2D>();
							myLineList.add(new Line2D.Double(pos.getxIndex() - x, pos.getyIndex() - y,
									pos.getxIndex() + y, pos.getyIndex() - x));
							myLineList.add(new Line2D.Double(pos.getxIndex() + y, pos.getyIndex() - x,
									pos.getxIndex() + x, pos.getyIndex() + y));
							myLineList.add(new Line2D.Double(pos.getxIndex() + x, pos.getyIndex() + y,
									pos.getxIndex() - y, pos.getyIndex() + x));
							myLineList.add(new Line2D.Double(pos.getxIndex() - y, pos.getyIndex() + x,
									pos.getxIndex() - x, pos.getyIndex() - y));
							squareCandBase.add(new Tatemono(pos, myLineList, y + x));
						}
						if (isFirstBreak) {
							break;
						}
					}
				} else {
					for (int y = 1; true; y = y + 2) {
						boolean isFirstBreak = false;
						for (int x = 1; true; x = x + 2) {
							if (pos.getyIndex() - y < 0 || pos.getyIndex() - x < 0 || pos.getyIndex() + y >= yLength
									|| pos.getyIndex() + x >= yLength || pos.getxIndex() - x < 0
									|| pos.getxIndex() + y >= xLength || pos.getxIndex() + x >= xLength
									|| pos.getxIndex() - y < 0) {
								if (x == 1) {
									isFirstBreak = true;
								}
								break;
							}
							List<Line2D> myLineList = new ArrayList<Line2D>();
							myLineList.add(new Line2D.Double(pos.getxIndex() - x, pos.getyIndex() - y,
									pos.getxIndex() + y, pos.getyIndex() - x));
							myLineList.add(new Line2D.Double(pos.getxIndex() + y, pos.getyIndex() - x,
									pos.getxIndex() + x, pos.getyIndex() + y));
							myLineList.add(new Line2D.Double(pos.getxIndex() + x, pos.getyIndex() + y,
									pos.getxIndex() - y, pos.getyIndex() + x));
							myLineList.add(new Line2D.Double(pos.getxIndex() - y, pos.getyIndex() + x,
									pos.getxIndex() - x, pos.getyIndex() - y));
							squareCandBase.add(new Tatemono(pos, myLineList, y + x));
						}
						if (isFirstBreak) {
							break;
						}
					}
				}
			}
			return squareCandBase;
		}

		public Field(int height, int width, String param) {
			yLength = height * 2 + 1;
			xLength = width * 2 + 1;
			numbersMap = new HashMap<>();
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET.indexOf(ch);
				if (interval != -1) {
					index = index + ((interval + 1) / 2);
				} else {
					// 16 - 255は '-'
					// 256 - 999は '+'
					int capacity = Integer.parseInt(String.valueOf(ch), 16);
					int wkPos = index / (width * 2 - 1);
					int wkSubPos = index % (width * 2 - 1);
					Position pos;
					if (wkSubPos < width) {
						pos = new Position(wkPos * 2 + 1, wkSubPos * 2 + 1);
					} else {
						pos = new Position(wkPos * 2 + 2, (wkSubPos - width) * 2 + 2);
					}
					numbersMap.put(pos, capacity);
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

		private static final String FULL_NUMS = "●１２３４５６７８";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Integer number = numbersMap.get(new Position(yIndex, xIndex));
					if (number == null) {
						if (xIndex % 2 == 0 && yIndex % 2 == 0) {
							sb.append("□");
						} else {
							sb.append("　");
						}
					} else {
						sb.append(FULL_NUMS.substring(number, number + 1));
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
				if (!finalSolve()) {
					return false;
				} else {
					return solveAndCheck();
				}
			}
			if (!finalSolve()) {
				return false;
			}
			return true;
		}

		/**
		 * 確定した四角は全部つながってないといけない。
		 */
		private boolean finalSolve() {
			Set<Tatemono> connectSikakuSet = new HashSet<>();
			for (Tatemono fixed : squareFixed) {
				if (connectSikakuSet.size() == 0) {
					connectSikakuSet.add(fixed);
					setContinueSikakuSet(fixed, connectSikakuSet);
				} else {
					if (!connectSikakuSet.contains(fixed)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * posを起点につなげられる箇所をつなぐ
		 */
		private void setContinueSikakuSet(Tatemono fixed, Set<Tatemono> connectSikakuSet) {
			for (Tatemono otherFixed : squareFixed) {
				if (fixed != otherFixed) {
					if (!connectSikakuSet.contains(otherFixed) && otherFixed.isCross(fixed)) {
						connectSikakuSet.add(otherFixed);
						setContinueSikakuSet(otherFixed, connectSikakuSet);
					}
				}
			}
			for (Tatemono otherFixed : squareCand) {
				if (!connectSikakuSet.contains(otherFixed) && otherFixed.isCross(fixed)) {
					connectSikakuSet.add(otherFixed);
					setContinueSikakuSet(otherFixed, connectSikakuSet);
				}
			}
		}

		/**
		 * 確定した四角がある場合、それとかぶる・接する候補を消す。すでにかぶっていたらfalse。
		 */
		private boolean sikakuSolve() {
			for (Tatemono fixed : squareFixed) {
				for (Tatemono otherFixed : squareFixed) {
					if (fixed != otherFixed) {
						if (otherFixed.isDuplicate(fixed)) {
							return false;
						}
					}
				}
				for (Iterator<Tatemono> iterator = squareCand.iterator(); iterator.hasNext();) {
					Tatemono oneCand = iterator.next();
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
				for (Tatemono fixed : squareFixed) {
					if (fixed.getCenterPos().equals(pos)) {
						if (!isFixed) {
							isFixed = true;
						} else {
							return false;
						}
					}
				}
				if (isFixed) {
					for (Tatemono cand : squareCand) {
						if (cand.getCenterPos().equals(pos)) {
							squareCand.remove(pos);
						}
					}
				} else {
					// そのマスに対する候補を検索
					Tatemono pickup = null;
					boolean only = true;
					for (Tatemono cand : squareCand) {
						if (cand.getCenterPos().equals(pos)) {
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
		 * 数字は接触する建物の数である。多すぎ・少なすぎはfalse。
		 */
		protected boolean countSolve() {
			for (Tatemono fixed : squareFixed) {
				int number = numbersMap.get(fixed.getCenterPos());
				if (number != 0) {
					int count = 0;
					for (Tatemono otherFixed : squareFixed) {
						if (fixed != otherFixed) {
							if (otherFixed.isCross(fixed)) {
								count++;
							}
						}
					}
					if (number < count) {
						return false;
					}
					// TODO 本当は頂点ごとに接続する・しないをカウントしたい
					for (Tatemono oneCand : squareCand) {
						if (oneCand.isCross(fixed)) {
							count++;
						}
					}
					if (number > count) {
						return false;
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

	public TajmahalSolver(int height, int width, String param) {
		long start = System.nanoTime();
		field = new Field(height, width, param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public TajmahalSolver(Field field) {
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
		System.out.println(new TajmahalSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count));
		System.out.println(field);
		int level = (int) Math.sqrt(count / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (Iterator<Tatemono> iterator = field.squareCand.iterator(); iterator.hasNext();) {
			count++;
			Tatemono oneCand = iterator.next();
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

package myamya.other.solver.yajilin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.CountOverException;
import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.GeneratorResult;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class YajilinSolver implements Solver {
	public static class YajilinGenerator implements Generator {

		static class YajilinSolverForGenerator extends YajilinSolver {

			private final int limit;

			public YajilinSolverForGenerator(Field field, int limit) {
				super(field);
				this.limit = limit;
			}

			/**
			 * -2:解なし
			 * -1:limit(多くの場合複数解)
			 * 0 >= 唯一解
			 */
			public int solve2() {
				try {
					while (!field.isSolved()) {
						String befStr = field.getStateDump();
						if (!field.solveAndCheck()) {
							return -2;
						}
						int recursiveCnt = 0;
						while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
							if (!candSolve(field, recursiveCnt)) {
								return -2;
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

		public YajilinGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new YajilinGenerator(10, 10).generate();
		}

		@Override
		public GeneratorResult generate() {
			YajilinSolver.Field wkField = new YajilinSolver.Field(height, width);
			int level = 0;
			int failCnt = 0;
			Map<Position, List<String>> wkArrowsInfo = new HashMap<>();
			long start = System.nanoTime();
			// 問題生成部
			while (true) {
				// 矢印を置く場所をランダムで決定する
				List<Position> candPosList = new ArrayList<>();
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.arrows[yIndex][xIndex] == null) {
							// 矢印を置くことにより、孤立するマスや3方向が囲まれるマス、
							// 矢印の固まりができるような置き方は起こりにくくする。
							int aroundArrow = 0;
							if (yIndex != 0) {
								int cnt = 0;
								Position pivot = new Position(yIndex - 1, xIndex);
								if (wkField.arrows[pivot.getyIndex()][pivot.getxIndex()] != null) {
									aroundArrow++;
								} else {
									if (pivot.getyIndex() == 0
											|| wkField.arrows[pivot.getyIndex() - 1][pivot.getxIndex()] != null) {
										cnt++;
									}
									if (pivot.getxIndex() == wkField.getXLength() - 1
											|| wkField.arrows[pivot.getyIndex()][pivot.getxIndex() + 1] != null) {
										cnt++;
									}
									if (pivot.getxIndex() == 0
											|| wkField.arrows[pivot.getyIndex()][pivot.getxIndex() - 1] != null) {
										cnt++;
									}
									if (cnt >= 2 && Math.random() < 0.8) {
										continue;
									}
								}
							}
							if (xIndex != wkField.getXLength() - 1) {
								int cnt = 0;
								Position pivot = new Position(yIndex, xIndex + 1);
								if (wkField.arrows[pivot.getyIndex()][pivot.getxIndex()] != null) {
									aroundArrow++;
								} else {
									if (pivot.getyIndex() == 0
											|| wkField.arrows[pivot.getyIndex() - 1][pivot.getxIndex()] != null) {
										cnt++;
									}
									if (pivot.getxIndex() == wkField.getXLength() - 1
											|| wkField.arrows[pivot.getyIndex()][pivot.getxIndex() + 1] != null) {
										cnt++;
									}
									if (pivot.getyIndex() == wkField.getYLength() - 1
											|| wkField.arrows[pivot.getyIndex() + 1][pivot.getxIndex()] != null) {
										cnt++;
									}
									if (cnt >= 2 && Math.random() < 0.8) {
										continue;
									}
								}
							}
							if (yIndex != wkField.getYLength() - 1) {
								int cnt = 0;
								Position pivot = new Position(yIndex + 1, xIndex);
								if (wkField.arrows[pivot.getyIndex()][pivot.getxIndex()] != null) {
									aroundArrow++;
								} else {
									if (pivot.getxIndex() == wkField.getXLength() - 1
											|| wkField.arrows[pivot.getyIndex()][pivot.getxIndex() + 1] != null) {
										cnt++;
									}
									if (pivot.getyIndex() == wkField.getYLength() - 1
											|| wkField.arrows[pivot.getyIndex() + 1][pivot.getxIndex()] != null) {
										cnt++;
									}
									if (pivot.getxIndex() == 0
											|| wkField.arrows[pivot.getyIndex()][pivot.getxIndex() - 1] != null) {
										cnt++;
									}
									if (cnt >= 2 && Math.random() < 0.8) {
										continue;
									}
								}
							}
							if (xIndex != 0) {
								int cnt = 0;
								Position pivot = new Position(yIndex, xIndex - 1);
								if (wkField.arrows[pivot.getyIndex()][pivot.getxIndex()] != null) {
									aroundArrow++;
								} else {
									if (pivot.getyIndex() == 0
											|| wkField.arrows[pivot.getyIndex() - 1][pivot.getxIndex()] != null) {
										cnt++;
									}
									if (pivot.getyIndex() == wkField.getYLength() - 1
											|| wkField.arrows[pivot.getyIndex() + 1][pivot.getxIndex()] != null) {
										cnt++;
									}
									if (pivot.getxIndex() == 0
											|| wkField.arrows[pivot.getyIndex()][pivot.getxIndex() - 1] != null) {
										cnt++;
									}
									if (cnt >= 2 && Math.random() < 0.8) {
										continue;
									}
								}
							}
							if (aroundArrow >= 1 && Math.random() < 0.95) {
								continue;
							}
							candPosList.add(new Position(yIndex, xIndex));
						}
					}
				}
				if (candPosList.isEmpty()) {
					// 置ける場所がないのに解けない→作り直し
					wkField = new YajilinSolver.Field(height, width);
					failCnt = 0;
					wkArrowsInfo.clear();
					continue;
				}
				Position arrowPos = candPosList.get((int) (Math.random() * candPosList.size()));
				// 矢印の向きと数をランダムで決定する
				// 外周が近い場合はできるだけ内向きにする
				List<Direction> candDirection = new ArrayList<>();
				if (arrowPos.getyIndex() != 0 && (arrowPos.getyIndex() != 1 || Math.random() < 0.5)) {
					candDirection.add(Direction.UP);
				}
				if (arrowPos.getxIndex() != wkField.getXLength() - 1
						&& (arrowPos.getxIndex() != wkField.getXLength() - 2 || Math.random() < 0.5)) {
					candDirection.add(Direction.RIGHT);
				}
				if (arrowPos.getyIndex() != wkField.getYLength() - 1
						&& (arrowPos.getyIndex() != wkField.getYLength() - 2 || Math.random() < 0.5)) {
					candDirection.add(Direction.DOWN);
				}
				if (arrowPos.getxIndex() != 0 && (arrowPos.getxIndex() != 1 || Math.random() < 0.5)) {
					candDirection.add(Direction.LEFT);
				}
				if (candDirection.isEmpty()) {
					continue;
				}
				Direction direction = candDirection.get((int) (Math.random() * candDirection.size()));
				int maxBlack = 0;
				if (direction == Direction.UP) {
					maxBlack = (arrowPos.getyIndex() + 1) / 2;
				}
				if (direction == Direction.RIGHT) {
					maxBlack = (wkField.getXLength() - arrowPos.getxIndex()) / 2;
				}
				if (direction == Direction.DOWN) {
					maxBlack = (wkField.getYLength() - arrowPos.getyIndex()) / 2;
				}
				if (direction == Direction.LEFT) {
					maxBlack = (arrowPos.getxIndex() + 1) / 2;
				}
				// 0はできにくくする。
				int count = (int) (Math.random() * (maxBlack + 0.25) + 0.75);
				// 解きチェック
				YajilinSolver.Field virtual = new YajilinSolver.Field(wkField, true);
				Arrow arrow = new Arrow(direction, count);
				virtual.setArrow(arrowPos, arrow);
				wkArrowsInfo.put(arrowPos,
						new ArrowSolver(virtual.getYLength(), virtual.getXLength(), arrowPos, arrow).solve());
				virtual.arrowsInfo = new HashMap<>(wkArrowsInfo);
				level = new YajilinSolverForGenerator(new YajilinSolver.Field(virtual), 450).solve2();
				if (level == -2) {
					// 解なしになった。矢印は破棄する
					failCnt++;
					if (failCnt == 100) {
						// 失敗回数がかさんだら作りなおし
						wkField = new YajilinSolver.Field(height, width);
						failCnt = 0;
						wkArrowsInfo.clear();
						continue;
					}
				} else if (level == -1) {
					// 解なしになってないが解けず。矢印は残す
					wkField.setArrow(arrowPos, new Arrow(direction, count));
				} else {
					// 唯一解になった！
					wkField.setArrow(arrowPos, new Arrow(direction, count));
					if (Math.random() < 0.9) {
						// 基本抜けるようにするが、低確率でそれ以上矢印が置けるかも試すようにする
						break;
					}
				}
			}
			level = (int) Math.sqrt(level * 6 / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(矢印：" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">puzz.linkで解く</a>";
			StringBuilder sb = new StringBuilder();
			int baseSize = 20;
			int margin = 5;
			sb.append(
					"<svg xmlns=\"http://www.w3.org/2000/svg\" "
							+ "height=\"" + (wkField.getYLength() * baseSize + 2 * baseSize + margin) + "\" width=\""
							+ (wkField.getXLength() * baseSize + 2 * baseSize) + "\" >");
			// 横壁描画
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = -1; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneYokoWall = xIndex == -1 || xIndex == wkField.getXLength() - 1;
					sb.append("<line y1=\""
							+ (yIndex * baseSize + margin)
							+ "\" x1=\""
							+ (xIndex * baseSize + 2 * baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\""
							+ (xIndex * baseSize + 2 * baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneYokoWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">"
							+ "</line>");
				}
			}
			// 縦壁描画
			for (int yIndex = -1; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					boolean oneTateWall = yIndex == -1 || yIndex == wkField.getYLength() - 1;
					sb.append("<line y1=\""
							+ (yIndex * baseSize + baseSize + margin)
							+ "\" x1=\""
							+ (xIndex * baseSize + baseSize)
							+ "\" y2=\""
							+ (yIndex * baseSize + baseSize + margin)
							+ "\" x2=\""
							+ (xIndex * baseSize + baseSize + baseSize)
							+ "\" stroke-width=\"1\" fill=\"none\"");
					if (oneTateWall) {
						sb.append("stroke=\"#000\" ");
					} else {
						sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
					}
					sb.append(">"
							+ "</line>");
				}
			}
			for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
					Arrow oneArrow = wkField.getArrows()[yIndex][xIndex];
					if (oneArrow != null) {
						sb.append("<text y=\"" + (yIndex * baseSize + baseSize - 4 + margin)
								+ "\" x=\""
								+ (xIndex * baseSize + baseSize)
								+ "\" font-size=\""
								+ (baseSize - 2)
								+ "\" textLength=\""
								+ (baseSize - 2)
								+ "\" lengthAdjust=\"spacingAndGlyphs\">"
								+ oneArrow.toStringWeb()
								+ "</text>");

					}
				}
			}
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");
		}
	}

	/**
	 * 矢印を候補を列挙して解くためのサブソルバー
	 */
	static class ArrowSolver {

		/**
		 * 最大候補利用数
		 * 0にすると候補を利用したトライをしなくなる
		 */
		private static final int CANDMAX = 100;

		private final int height;
		private final int width;
		private final Position pos;
		private final Arrow arrow;

		public ArrowSolver(int height, int width, Position pos, Arrow arrow) {
			this.height = height;
			this.width = width;
			this.pos = pos;
			this.arrow = arrow;
		}

		public static void main(String[] args) {
			List<String> result = new ArrowSolver(10, 10, new Position(9, 5), new Arrow(Direction.UP, '3'))
					.solve();
			System.out.println(result);
		}

		/**
		 * 盤面の高さ・幅・矢印の位置・矢印の内容からマスの候補を返却する。
		 */
		public List<String> solve() {
			List<String> result = new ArrayList<>();
			oneSolve(result, new StringBuilder(), pos, 0);
			return result.size() >= CANDMAX ? null : result;
		}

		private void oneSolve(List<String> result, StringBuilder sb,
				Position nowPos, int nowBlack) {
			if (result.size() >= CANDMAX) {
				return;
			}
			if (arrow.getCount() == nowBlack) {
				StringBuilder newSb = new StringBuilder(sb);
				if (arrow.getDirection() == Direction.UP) {
					for (int yIndex = nowPos.getyIndex() - 1; yIndex >= 0; yIndex--) {
						newSb.append(Masu.NOT_BLACK.toString());
					}
				}
				if (arrow.getDirection() == Direction.RIGHT) {
					for (int xIndex = nowPos.getxIndex() + 1; xIndex < width; xIndex++) {
						newSb.append(Masu.NOT_BLACK.toString());
					}
				}
				if (arrow.getDirection() == Direction.DOWN) {
					for (int yIndex = nowPos.getyIndex() + 1; yIndex < height; yIndex++) {
						newSb.append(Masu.NOT_BLACK.toString());
					}
				}
				if (arrow.getDirection() == Direction.LEFT) {
					for (int xIndex = nowPos.getxIndex() - 1; xIndex >= 0; xIndex--) {
						newSb.append(Masu.NOT_BLACK.toString());
					}
				}
				result.add(newSb.toString());
				return;
			} else {
				if (arrow.getDirection() == Direction.UP) {
					if (nowPos.getyIndex() == 0) {
						return;
					} else {
						if (nowPos.getyIndex() != 1) {
							sb.append(Masu.BLACK.toString());
							sb.append(Masu.NOT_BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex() - 2, nowPos.getxIndex()),
									nowBlack + 1);
							sb.setLength(sb.length() - 2);
						} else {
							sb.append(Masu.BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex() - 1, nowPos.getxIndex()),
									nowBlack + 1);
							sb.setLength(sb.length() - 1);
						}
						sb.append(Masu.NOT_BLACK.toString());
						oneSolve(result, sb, new Position(nowPos.getyIndex() - 1, nowPos.getxIndex()), nowBlack);
						sb.setLength(sb.length() - 1);
					}
				}
				if (arrow.getDirection() == Direction.RIGHT) {
					if (nowPos.getxIndex() == width - 1) {
						return;
					} else {
						if (nowPos.getxIndex() != width - 2) {
							sb.append(Masu.BLACK.toString());
							sb.append(Masu.NOT_BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex(), nowPos.getxIndex() + 2),
									nowBlack + 1);
							sb.setLength(sb.length() - 2);
						} else {
							sb.append(Masu.BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex(), nowPos.getxIndex() + 1),
									nowBlack + 1);
							sb.setLength(sb.length() - 1);
						}
						sb.append(Masu.NOT_BLACK.toString());
						oneSolve(result, sb, new Position(nowPos.getyIndex(), nowPos.getxIndex() + 1),
								nowBlack);
						sb.setLength(sb.length() - 1);
					}
				}
				if (arrow.getDirection() == Direction.DOWN) {
					if (nowPos.getyIndex() == height - 1) {
						return;
					} else {
						if (nowPos.getyIndex() != height - 2) {
							sb.append(Masu.BLACK.toString());
							sb.append(Masu.NOT_BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex() + 2, nowPos.getxIndex()),
									nowBlack + 1);
							sb.setLength(sb.length() - 2);
						} else {
							sb.append(Masu.BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex() + 1, nowPos.getxIndex()),
									nowBlack + 1);
							sb.setLength(sb.length() - 1);
						}
						sb.append(Masu.NOT_BLACK.toString());
						oneSolve(result, sb, new Position(nowPos.getyIndex() + 1, nowPos.getxIndex()), nowBlack);
						sb.setLength(sb.length() - 1);
					}
				}
				if (arrow.getDirection() == Direction.LEFT) {
					if (nowPos.getxIndex() == 0) {
						return;
					} else {
						if (nowPos.getxIndex() != 1) {
							sb.append(Masu.BLACK.toString());
							sb.append(Masu.NOT_BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex(), nowPos.getxIndex() - 2),
									nowBlack + 1);
							sb.setLength(sb.length() - 2);
						} else {
							sb.append(Masu.BLACK.toString());
							oneSolve(result, sb, new Position(nowPos.getyIndex(), nowPos.getxIndex() - 1),
									nowBlack + 1);
							sb.setLength(sb.length() - 1);
						}
						sb.append(Masu.NOT_BLACK.toString());
						oneSolve(result, sb, new Position(nowPos.getyIndex(), nowPos.getxIndex() - 1),
								nowBlack);
						sb.setLength(sb.length() - 1);
					}
				}
			}
		}

	}

	/**
	 * 矢印のマス
	 */
	public static class Arrow {
		private final Direction direction;
		private final int count;

		public Arrow(Direction direction, int count) {
			this.direction = direction;
			this.count = count;
		}

		public Direction getDirection() {
			return direction;
		}

		public int getCount() {
			return count;
		}

		@Override
		public String toString() {
			return count == -1 ? "？" : (count >= 10 ? String.valueOf(count) : direction.toString() + count);
		}

		public String toStringWeb() {
			return count == -1 ? "？" : direction.getDirectString() + count;
		}
	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 矢印の情報
		private Arrow[][] arrows;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private Wall[][] tateWall;
		// 矢印の候補情報
		private Map<Position, List<String>> arrowsInfo;
		// バリアントルール-ループ内黒ます禁止
		private final boolean out;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getPuzPreURL() {
			StringBuilder sb = new StringBuilder();
			sb.append("http://puzz.link/p?yajilin/" + getXLength() + "/" + getYLength() + "/");
			int interval = 0;
			for (int i = 0; i < getYLength() * getXLength(); i++) {
				int yIndex = i / getXLength();
				int xIndex = i % getXLength();
				if (arrows[yIndex][xIndex] == null) {
					interval++;
					if (interval == 26) {
						sb.append("z");
						interval = 0;
					}
				} else {
					String numStr = null;
					int numTop;
					Arrow arrow = arrows[yIndex][xIndex];
					if (arrow != null) {
						numTop = arrow.getDirection().toNum();
						if (arrow.count >= 16) {
							numTop = numTop + 5;
						}
						numStr = numTop + Integer.toHexString(arrow.getCount());
					}
					if (interval == 0) {
						sb.append(numStr);
					} else {
						sb.append(ALPHABET.substring(interval - 1, interval));
						sb.append(numStr);
						interval = 0;
					}
				}
			}
			if (interval != 0) {
				sb.append(ALPHABET.substring(interval - 1, interval));
			}
			if (sb.charAt(sb.length() - 1) == '.') {
				sb.append("/");
			}
			return sb.toString();
		}

		public String getHintCount() {
			int cnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null) {
						cnt++;
					}
				}
			}
			return String.valueOf(cnt);
		}

		public Arrow[][] getArrows() {
			return arrows;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param, boolean out) {
			masu = new Masu[height][width];
			arrows = new Arrow[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			arrowsInfo = new HashMap<>();
			this.out = out;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			int index = 0;
			Direction direction = null;
			boolean adjust = false;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				if (direction == null) {
					int interval = ALPHABET.indexOf(ch) + 1;
					if (interval != 0) {
						index = index + interval;
					} else {
						int val = Character.getNumericValue(ch);
						if (5 <= val && val <= 9) {
							val = val - 5;
							adjust = true;
						}
						direction = Direction.getByNum(val);
						if (direction == null) {
							if (adjust) {
								i++;
							} else {
								Position arrowPos = new Position(index / getXLength(), index % getXLength());
								masu[arrowPos.getyIndex()][arrowPos.getxIndex()] = Masu.NOT_BLACK;
								arrows[arrowPos.getyIndex()][arrowPos.getxIndex()] = new Arrow(direction, -1);
								// 周囲の壁を閉鎖
								if (arrowPos.getyIndex() != 0) {
									tateWall[arrowPos.getyIndex() - 1][arrowPos.getxIndex()] = Wall.EXISTS;
								}
								if (arrowPos.getxIndex() != getXLength() - 1) {
									yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.EXISTS;
								}
								if (arrowPos.getyIndex() != getYLength() - 1) {
									tateWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.EXISTS;
								}
								if (arrowPos.getxIndex() != 0) {
									yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex() - 1] = Wall.EXISTS;
								}
							}
							index++;
							i++;
							adjust = false;
						}
					}
				} else {
					Position arrowPos = new Position(index / getXLength(), index % getXLength());
					Arrow arrow;
					if (adjust) {
						i++;
						arrow = new Arrow(direction, Character.getNumericValue(ch) * 16
								+ Character.getNumericValue(param.charAt(i)));
					} else {
						arrow = new Arrow(direction, Character.getNumericValue(ch));
					}
					setArrow(arrowPos, arrow);
					arrowsInfo.put(arrowPos,
							new ArrowSolver(getYLength(), getXLength(), arrowPos, arrow)
									.solve());
					adjust = false;
					index++;
					direction = null;
				}
			}
		}

		/**
		 * 付随処理も含めた矢印セット処理
		 * ジェネレータでも使う
		 */
		protected void setArrow(Position arrowPos, Arrow arrow) {
			masu[arrowPos.getyIndex()][arrowPos.getxIndex()] = Masu.NOT_BLACK;
			arrows[arrowPos.getyIndex()][arrowPos.getxIndex()] = arrow;
			// 周囲の壁を閉鎖
			if (arrowPos.getyIndex() != 0) {
				tateWall[arrowPos.getyIndex() - 1][arrowPos.getxIndex()] = Wall.EXISTS;
			}
			if (arrowPos.getxIndex() != getXLength() - 1) {
				yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.EXISTS;
			}
			if (arrowPos.getyIndex() != getYLength() - 1) {
				tateWall[arrowPos.getyIndex()][arrowPos.getxIndex()] = Wall.EXISTS;
			}
			if (arrowPos.getxIndex() != 0) {
				yokoWall[arrowPos.getyIndex()][arrowPos.getxIndex() - 1] = Wall.EXISTS;
			}
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			arrows = other.arrows;
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			arrowsInfo = new HashMap<>();
			out = other.out;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = other.yokoWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
				}
			}
			for (Entry<Position, List<String>> entry : other.arrowsInfo.entrySet()) {
				arrowsInfo.put(entry.getKey(), entry.getValue() == null ? null : new ArrayList<>(entry.getValue()));
			}
		}

		/**
		 * プレーンなフィールド生成
		 */
		public Field(int height, int width) {
			masu = new Masu[height][width];
			arrows = new Arrow[height][width];
			yokoWall = new Wall[height][width - 1];
			tateWall = new Wall[height - 1][width];
			arrowsInfo = new HashMap<>();
			this.out = false;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = Wall.SPACE;
				}
			}
		}

		/**
		 * イミュータブルにするやつ。ジェネレータ用
		 */
		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			arrows = new Arrow[other.getYLength()][other.getXLength()];
			yokoWall = new Wall[other.getYLength()][other.getXLength() - 1];
			tateWall = new Wall[other.getYLength() - 1][other.getXLength()];
			arrowsInfo = new HashMap<>();
			out = other.out;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					arrows[yIndex][xIndex] = other.arrows[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					yokoWall[yIndex][xIndex] = other.yokoWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateWall[yIndex][xIndex] = other.tateWall[yIndex][xIndex];
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
					if (arrows[yIndex][xIndex] != null) {
						sb.append(arrows[yIndex][xIndex]);
					} else {
						sb.append(masu[yIndex][xIndex]);
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					sb.append(yokoWall[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateWall[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 矢印に対し、指定した黒マス数を満たせなくなる場合falseを返す。
		 */
		private boolean arrowSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] != null) {
						Arrow arrow = arrows[yIndex][xIndex];
						if (arrow.getCount() == -1) {
							continue;
						}
						//												int adjust = 0;
						//												int blackCount = 0;
						//												int notBlackCount = 0;
						//												while (true) {
						//													Position pos = null;
						//													if (arrow.getDirection() == Direction.UP) {
						//														if (yIndex - 1 - adjust < 0) {
						//															break;
						//														}
						//														pos = new Position(yIndex - 1 - adjust, xIndex);
						//													}
						//													if (arrow.getDirection() == Direction.RIGHT) {
						//														if (xIndex + 1 + adjust >= getXLength()) {
						//															break;
						//														}
						//														pos = new Position(yIndex, xIndex + 1 + adjust);
						//													}
						//													if (arrow.getDirection() == Direction.DOWN) {
						//														if (yIndex + 1 + adjust >= getYLength()) {
						//															break;
						//														}
						//														pos = new Position(yIndex + 1 + adjust, xIndex);
						//													}
						//													if (arrow.getDirection() == Direction.LEFT) {
						//														if (xIndex - 1 - adjust < 0) {
						//															break;
						//														}
						//														pos = new Position(yIndex, xIndex - 1 - adjust);
						//													}
						//													if (masu[pos.getyIndex()][pos.getxIndex()] instanceof Arrow) {
						//														Arrow anotherArrow = (Arrow) masu[pos.getyIndex()][pos.getxIndex()];
						//														if (anotherArrow.getDirection() == arrow.getDirection() && anotherArrow.getCount() != -1) {
						//															int betweenCnt = arrow.getCount() - anotherArrow.getCount();
						//															if (betweenCnt < blackCount) {
						//																return false;
						//															}
						//															if (betweenCnt > (adjust - notBlackCount)) {
						//																return false;
						//															}
						//															break;
						//														}
						//													} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						//														blackCount++;
						//													} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
						//														notBlackCount++;
						//													}
						//													adjust++;
						//												}
						List<String> candList = arrowsInfo.get(new Position(yIndex, xIndex));
						if (candList != null) {
							for (Iterator<String> iterator = candList.iterator(); iterator.hasNext();) {
								String state = iterator.next();
								for (int idx = 0; idx < state.length(); idx++) {
									Position pos = null;
									if (arrow.getDirection() == Direction.UP) {
										pos = new Position(yIndex - 1 - idx, xIndex);
									}
									if (arrow.getDirection() == Direction.RIGHT) {
										pos = new Position(yIndex, xIndex + 1 + idx);
									}
									if (arrow.getDirection() == Direction.DOWN) {
										pos = new Position(yIndex + 1 + idx, xIndex);
									}
									if (arrow.getDirection() == Direction.LEFT) {
										pos = new Position(yIndex, xIndex - 1 - idx);
									}
									if ((masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK
											&& state.charAt(idx) == '・')
											|| ((masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK)
													&& state.charAt(idx) == '■')) {
										iterator.remove();
										break;
									}
								}
							}
							if (candList.size() == 0) {
								return false;
							} else {
								StringBuilder fixState = new StringBuilder(candList.get(0));
								for (String cand : candList) {
									for (int idx = 0; idx < fixState.length(); idx++) {
										char a = fixState.charAt(idx);
										char b = cand.charAt(idx);
										if ((a == '■' && b == '・') || (a == '・' && b == '■')) {
											fixState.setCharAt(idx, '　');
										}
									}
								}
								for (int idx = 0; idx < fixState.length(); idx++) {
									Position pos = null;
									if (arrow.getDirection() == Direction.UP) {
										pos = new Position(yIndex - 1 - idx, xIndex);
									}
									if (arrow.getDirection() == Direction.RIGHT) {
										pos = new Position(yIndex, xIndex + 1 + idx);
									}
									if (arrow.getDirection() == Direction.DOWN) {
										pos = new Position(yIndex + 1 + idx, xIndex);
									}
									if (arrow.getDirection() == Direction.LEFT) {
										pos = new Position(yIndex, xIndex - 1 - idx);
									}
									if (fixState.charAt(idx) == '■') {
										masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
									} else if (fixState.charAt(idx) == '・') {
										masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
									} else {
										masu[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
									}
								}
							}
						} else {
							Position pivot = new Position(yIndex, xIndex);
							int idx = 0;
							int blackCnt = 0;
							int spaceCnt = 0;
							boolean nextCanSpace = true;
							if (arrow.getDirection() == Direction.UP) {
								while (pivot.getyIndex() - 1 - idx >= 0) {
									Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
									if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
										blackCnt++;
										if (arrow.getCount() < blackCnt) {
											return false;
										}
										nextCanSpace = false;
									} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
										if (nextCanSpace) {
											spaceCnt++;
											nextCanSpace = false;
										} else {
											nextCanSpace = true;
										}
									} else {
										nextCanSpace = true;
									}
									idx++;
								}
							}
							if (arrow.getDirection() == Direction.RIGHT) {
								while (pivot.getxIndex() + 1 + idx < getXLength()) {
									Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() + 1 + idx);
									if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
										blackCnt++;
										if (arrow.getCount() < blackCnt) {
											return false;
										}
										nextCanSpace = false;
									} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
										if (nextCanSpace) {
											spaceCnt++;
											nextCanSpace = false;
										} else {
											nextCanSpace = true;
										}
									} else {
										nextCanSpace = true;
									}
									idx++;
								}
							}
							if (arrow.getDirection() == Direction.DOWN) {
								while (pivot.getyIndex() + 1 + idx < getYLength()) {
									Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex());
									if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
										blackCnt++;
										if (arrow.getCount() < blackCnt) {
											return false;
										}
										nextCanSpace = false;
									} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
										if (nextCanSpace) {
											spaceCnt++;
											nextCanSpace = false;
										} else {
											nextCanSpace = true;
										}
									} else {
										nextCanSpace = true;
									}
									idx++;
								}
							}
							if (arrow.getDirection() == Direction.LEFT) {
								while (pivot.getxIndex() - 1 - idx >= 0) {
									Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
									if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
										blackCnt++;
										if (arrow.getCount() < blackCnt) {
											return false;
										}
										nextCanSpace = false;
									} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
										if (nextCanSpace) {
											spaceCnt++;
											nextCanSpace = false;
										} else {
											nextCanSpace = true;
										}
									} else {
										nextCanSpace = true;
									}
									idx++;
								}
							}
							if (arrow.getCount() > blackCnt + spaceCnt) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスの周囲の壁を埋め、隣接セルを白マスにする
		 * 黒マス隣接セルの隣に黒マスがある場合はfalseを返す。
		 * また、白マス隣接セルの周辺の壁の数が2にならない場合もfalseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] == null) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							Masu masuUp, masuRight, masuDown, masuLeft;
							// 周囲の壁を閉鎖
							if (yIndex != 0) {
								if (tateWall[yIndex - 1][xIndex] == Wall.NOT_EXISTS) {
									return false;
								}
								tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
								masuUp = masu[yIndex - 1][xIndex];
							} else {
								masuUp = Masu.NOT_BLACK;
							}
							if (xIndex != getXLength() - 1) {
								if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
									return false;
								}
								yokoWall[yIndex][xIndex] = Wall.EXISTS;
								masuRight = masu[yIndex][xIndex + 1];
							} else {
								masuRight = Masu.NOT_BLACK;
							}
							if (yIndex != getYLength() - 1) {
								if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
									return false;
								}
								tateWall[yIndex][xIndex] = Wall.EXISTS;
								masuDown = masu[yIndex + 1][xIndex];
							} else {
								masuDown = Masu.NOT_BLACK;
							}
							if (xIndex != 0) {
								if (yokoWall[yIndex][xIndex - 1] == Wall.NOT_EXISTS) {
									return false;
								}
								yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
								masuLeft = masu[yIndex][xIndex - 1];
							} else {
								masuLeft = Masu.NOT_BLACK;
							}
							if (masuUp == Masu.BLACK || masuRight == Masu.BLACK || masuDown == Masu.BLACK
									|| masuLeft == Masu.BLACK) {
								return false;
							}
							if (masuUp == Masu.SPACE) {
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masuRight == Masu.SPACE) {
								masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
							}
							if (masuDown == Masu.SPACE) {
								masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
							}
							if (masuLeft == Masu.SPACE) {
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
							}
						} else {
							int existsCount = 0;
							int notExistsCount = 0;
							Wall wallUp = yIndex == 0 ? Wall.EXISTS : tateWall[yIndex - 1][xIndex];
							if (wallUp == Wall.EXISTS) {
								existsCount++;
							} else if (wallUp == Wall.NOT_EXISTS) {
								if (masu[yIndex - 1][xIndex] == Masu.BLACK) {
									return false;
								}
								masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
								notExistsCount++;
							}
							Wall wallRight = xIndex == getXLength() - 1 ? Wall.EXISTS : yokoWall[yIndex][xIndex];
							if (wallRight == Wall.EXISTS) {
								existsCount++;
							} else if (wallRight == Wall.NOT_EXISTS) {
								if (masu[yIndex][xIndex + 1] == Masu.BLACK) {
									return false;
								}
								masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
								notExistsCount++;
							}
							Wall wallDown = yIndex == getYLength() - 1 ? Wall.EXISTS : tateWall[yIndex][xIndex];
							if (wallDown == Wall.EXISTS) {
								existsCount++;
							} else if (wallDown == Wall.NOT_EXISTS) {
								if (masu[yIndex + 1][xIndex] == Masu.BLACK) {
									return false;
								}
								masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
								notExistsCount++;
							}
							Wall wallLeft = xIndex == 0 ? Wall.EXISTS : yokoWall[yIndex][xIndex - 1];
							if (wallLeft == Wall.EXISTS) {
								existsCount++;
							} else if (wallLeft == Wall.NOT_EXISTS) {
								if (masu[yIndex][xIndex - 1] == Masu.BLACK) {
									return false;
								}
								masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
								notExistsCount++;
							}
							// 自分が白マスなら壁は必ず2マス
							if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
								if (existsCount > 2 || notExistsCount > 2) {
									return false;
								}
								if (notExistsCount == 2) {
									if (wallUp == Wall.SPACE) {
										tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
									}
									if (wallRight == Wall.SPACE) {
										yokoWall[yIndex][xIndex] = Wall.EXISTS;
									}
									if (wallDown == Wall.SPACE) {
										tateWall[yIndex][xIndex] = Wall.EXISTS;
									}
									if (wallLeft == Wall.SPACE) {
										yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
									}
								} else if (existsCount == 2) {
									if (wallUp == Wall.SPACE) {
										if (masu[yIndex - 1][xIndex] == Masu.BLACK) {
											return false;
										}
										tateWall[yIndex - 1][xIndex] = Wall.NOT_EXISTS;
										masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
									}
									if (wallRight == Wall.SPACE) {
										if (masu[yIndex][xIndex + 1] == Masu.BLACK) {
											return false;
										}
										yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
										masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
									}
									if (wallDown == Wall.SPACE) {
										if (masu[yIndex + 1][xIndex] == Masu.BLACK) {
											return false;
										}
										tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
										masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
									}
									if (wallLeft == Wall.SPACE) {
										if (masu[yIndex][xIndex - 1] == Masu.BLACK) {
											return false;
										}
										yokoWall[yIndex][xIndex - 1] = Wall.NOT_EXISTS;
										masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
									}
								}
							} else if (masu[yIndex][xIndex] == Masu.SPACE) {
								// 自分が不確定マスなら壁は2マスか4マス
								if ((existsCount == 3 && notExistsCount == 1)
										|| notExistsCount > 2) {
									return false;
								}
								if (existsCount == 2) {
									// 壁が2個の場合、壁のない方の白マス確定(出口理論)
									if (wallUp != Wall.EXISTS) {
										if (masu[yIndex - 1][xIndex] == Masu.BLACK) {
											return false;
										}
										masu[yIndex - 1][xIndex] = Masu.NOT_BLACK;
									}
									if (wallRight != Wall.EXISTS) {
										if (masu[yIndex][xIndex + 1] == Masu.BLACK) {
											return false;
										}
										masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
									}
									if (wallDown != Wall.EXISTS) {
										if (masu[yIndex + 1][xIndex] == Masu.BLACK) {
											return false;
										}
										masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
									}
									if (wallLeft != Wall.EXISTS) {
										if (masu[yIndex][xIndex - 1] == Masu.BLACK) {
											return false;
										}
										masu[yIndex][xIndex - 1] = Masu.NOT_BLACK;
									}
								}
								if (existsCount > 2) {
									masu[yIndex][xIndex] = Masu.BLACK;
									if (wallUp == Wall.SPACE) {
										tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
									}
									if (wallRight == Wall.SPACE) {
										yokoWall[yIndex][xIndex] = Wall.EXISTS;
									}
									if (wallDown == Wall.SPACE) {
										tateWall[yIndex][xIndex] = Wall.EXISTS;
									}
									if (wallLeft == Wall.SPACE) {
										yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
									}
								} else if (notExistsCount != 0) {
									masu[yIndex][xIndex] = Masu.NOT_BLACK;
									if (notExistsCount == 2) {
										if (wallUp == Wall.SPACE) {
											tateWall[yIndex - 1][xIndex] = Wall.EXISTS;
										}
										if (wallRight == Wall.SPACE) {
											yokoWall[yIndex][xIndex] = Wall.EXISTS;
										}
										if (wallDown == Wall.SPACE) {
											tateWall[yIndex][xIndex] = Wall.EXISTS;
										}
										if (wallLeft == Wall.SPACE) {
											yokoWall[yIndex][xIndex - 1] = Wall.EXISTS;
										}
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
		 * 白マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (arrows[yIndex][xIndex] == null && masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						if (whitePosSet.isEmpty()) {
							whitePosSet.add(whitePos);
							setContinuePosSet(whitePos, whitePosSet, null);
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
		 * posを起点に上下左右に壁で区切られていないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (tateWall[pos.getyIndex() - 1][pos.getxIndex()] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (tateWall[pos.getyIndex()][pos.getxIndex()] != Wall.EXISTS && !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (yokoWall[pos.getyIndex()][pos.getxIndex() - 1] != Wall.EXISTS
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!arrowSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!oddSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (out) {
					if (!outsideSolve()) {
						return false;
					}
				}
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * ヤジリンのルール上、各列をふさぐ壁は必ず偶数になる。
		 * 偶数になっていない場合falseを返す。
		 */
		private boolean oddSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				int notExistsCount = 0;
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (tateWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
				int notExistsCount = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						notExistsCount = 0;
						break;
					} else if (yokoWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
				}
				if (notExistsCount % 2 != 0) {
					return false;
				}
			}
			return true;
		}

		/**
		 * ループ内黒マス禁止ヤリジン専用の解法
		 * 黒マスから前後左右に向かっていき、外壁か黒マスのいずれかにぶつかるまでに
		 * 壁の数が奇数個であれば失敗。
		 * また、黒マスで未確定壁が残り1マスの場合は、偶数個になるように壁の有無が確定する。
		 */
		private boolean outsideSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pivot = new Position(yIndex, xIndex);
					// 左側横壁上方向
					int idx = 0;
					int whiteCnt = 0;
					int spaceCnt = 0;
					Position spacePos = null;
					while (pivot.getyIndex() - 1 - idx >= 0 && pivot.getxIndex() != 0
							&& masu[pivot.getyIndex() - 1 - idx][pivot.getxIndex()] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex() - 1);
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, true)) {
						return false;
					}
					// 右側横壁上方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getyIndex() - 1 - idx >= 0 && pivot.getxIndex() != getXLength() - 1
							&& masu[pivot.getyIndex() - 1 - idx][pivot.getxIndex()] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, true)) {
						return false;
					}

					// 左側横壁下方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getyIndex() + 1 + idx < getYLength() && pivot.getxIndex() != 0
							&& masu[pivot.getyIndex() + 1 + idx][pivot.getxIndex()] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex() - 1);
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, true)) {
						return false;
					}

					// 右側横壁下方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getyIndex() + 1 + idx < getYLength() && pivot.getxIndex() != getXLength() - 1
							&& masu[pivot.getyIndex() + 1 + idx][pivot.getxIndex()] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex());
						if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (yokoWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, true)) {
						return false;
					}

					// 上側縦壁右方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getxIndex() + 1 + idx < getXLength() && pivot.getyIndex() != 0
							&& masu[pivot.getyIndex()][pivot.getxIndex() + 1 + idx] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex() - 1, pivot.getxIndex() + 1 + idx);
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, false)) {
						return false;
					}

					// 下側縦壁右方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getxIndex() + 1 + idx < getXLength() && pivot.getyIndex() != getYLength() - 1
							&& masu[pivot.getyIndex()][pivot.getxIndex() + 1 + idx] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() + 1 + idx);
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, false)) {
						return false;
					}

					// 上側縦壁左方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getxIndex() - 1 - idx >= 0 && pivot.getyIndex() != 0
							&& masu[pivot.getyIndex()][pivot.getxIndex() - 1 - idx] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex() - 1, pivot.getxIndex() - 1 - idx);
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, false)) {
						return false;
					}

					// 下側縦壁左方向
					idx = 0;
					whiteCnt = 0;
					spaceCnt = 0;
					spacePos = null;
					while (pivot.getxIndex() - 1 - idx >= 0 && pivot.getyIndex() != getYLength() - 1
							&& masu[pivot.getyIndex()][pivot.getxIndex() - 1 - idx] != Masu.BLACK) {
						Position pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
						if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS) {
							whiteCnt++;
						} else if (tateWall[pos.getyIndex()][pos.getxIndex()] == Wall.SPACE) {
							spacePos = pos;
							spaceCnt++;
						}
						idx++;
					}
					if (!oneOutSideCheck(pivot, whiteCnt, spaceCnt, spacePos, false)) {
						return false;
					}
				}
			}
			return true;
		}

		private boolean oneOutSideCheck(Position pivot, int whiteCnt, int spaceCnt, Position spacePos, boolean isYoko) {
			if (spaceCnt == 0 && whiteCnt % 2 == 1) {
				if (masu[pivot.getyIndex()][pivot.getxIndex()] == Masu.BLACK) {
					return false;
				} else if (masu[pivot.getyIndex()][pivot.getxIndex()] == Masu.SPACE) {
					masu[pivot.getyIndex()][pivot.getxIndex()] = Masu.NOT_BLACK;
				}
			} else if (masu[pivot.getyIndex()][pivot.getxIndex()] == Masu.BLACK && spaceCnt == 1) {
				if (whiteCnt % 2 == 1) {
					if (isYoko) {
						yokoWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.NOT_EXISTS;
					} else {
						tateWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.NOT_EXISTS;
					}
				} else {
					if (isYoko) {
						yokoWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.EXISTS;
					} else {
						tateWall[spacePos.getyIndex()][spacePos.getxIndex()] = Wall.EXISTS;
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					if (yokoWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public YajilinSolver(int height, int width, String param, boolean out) {
		field = new Field(height, width, param, out);
	}

	public YajilinSolver(Field field) {
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
		System.out.println(new YajilinSolver(height, width, param, false).solve());
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
		System.out.println("難易度:" + (count * 6));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 6 / 3) + 1;
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 6).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					// 周囲に空白が少ない個所を優先して調査
					Masu masuUp = yIndex == 0 ? Masu.BLACK
							: field.masu[yIndex - 1][xIndex];
					Masu masuRight = xIndex == field.getXLength() - 1 ? Masu.BLACK
							: field.masu[yIndex][xIndex + 1];
					Masu masuDown = yIndex == field.getYLength() - 1 ? Masu.BLACK
							: field.masu[yIndex + 1][xIndex];
					Masu masuLeft = xIndex == 0 ? Masu.BLACK
							: field.masu[yIndex][xIndex - 1];
					int whiteCnt = 0;
					if (masuUp == Masu.SPACE) {
						whiteCnt++;
					}
					if (masuRight == Masu.SPACE) {
						whiteCnt++;
					}
					if (masuDown == Masu.SPACE) {
						whiteCnt++;
					}
					if (masuLeft == Masu.SPACE) {
						whiteCnt++;
					}
					if (whiteCnt > 3) {
						continue;
					}
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		if (recursive > 0) {
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength() - 1; xIndex++) {
					if (field.yokoWall[yIndex][xIndex] == Wall.SPACE) {
						count++;
						if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive - 1)) {
							return false;
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < field.getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.tateWall[yIndex][xIndex] == Wall.SPACE) {
						count++;
						if (!oneCandTateWallSolve(field, yIndex, xIndex, recursive - 1)) {
							return false;
						}
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

	private boolean oneCandYokoWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.yokoWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoWall[yIndex][xIndex] = Wall.NOT_EXISTS;
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}

	private boolean oneCandTateWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateWall[yIndex][xIndex] = Wall.NOT_EXISTS;
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
			field.tateWall = virtual2.tateWall;
			field.yokoWall = virtual2.yokoWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateWall = virtual.tateWall;
			field.yokoWall = virtual.yokoWall;
		}
		return true;
	}
}

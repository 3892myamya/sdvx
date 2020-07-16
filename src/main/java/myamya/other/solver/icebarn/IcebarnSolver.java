package myamya.other.solver.icebarn;

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
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import myamya.other.solver.Generator;
import myamya.other.solver.Solver;

public class IcebarnSolver implements Solver {
	static class WallPos {

		@Override
		public String toString() {
			return "[" + isYokoWall + ", " + yIndex + ", " + xIndex + "]";
		}

		final boolean isYokoWall;
		final int yIndex;
		final int xIndex;

		public WallPos(boolean isYokoWall, int yIndex, int xIndex) {
			super();
			this.isYokoWall = isYokoWall;
			this.yIndex = yIndex;
			this.xIndex = xIndex;
		}

	}

	public static class IcebarnGenerator implements Generator {

		static class IcebarnSolverForGenerator extends IcebarnSolver {
			private final int limit;

			public IcebarnSolverForGenerator(Field field, int limit) {
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

		public IcebarnGenerator(int height, int width) {
			this.height = height;
			this.width = width;
		}

		public static void main(String[] args) {
			new IcebarnGenerator(8, 8).generate();
		}

		@Override
		public GeneratorResult generate() {
			IcebarnSolver.Field wkField = new Field(height, width);
			List<Integer> indexList = new ArrayList<>();
			for (int i = 0; i < (height * (width - 1)) + ((height - 1) * width); i++) {
				indexList.add(i);
			}
			Collections.shuffle(indexList);
			int index = 0;
			int level = 0;
			long start = System.nanoTime();
			while (true) {
				// 問題生成部
				while (!wkField.isSolved()) {
					int posBase = indexList.get(index);
					boolean toYokoWall;
					int yIndex, xIndex;
					if (posBase < height * (width - 1)) {
						toYokoWall = true;
						yIndex = posBase / (width - 1);
						xIndex = posBase % (width - 1) + 1;
					} else {
						toYokoWall = false;
						posBase = posBase - (height * (width - 1));
						yIndex = posBase / width + 1;
						xIndex = posBase % width;
					}
					if ((toYokoWall && wkField.yokoExtraWall[yIndex][xIndex] == Wall.SPACE)
							|| (!toYokoWall && wkField.tateExtraWall[yIndex][xIndex] == Wall.SPACE)) {
						boolean isOk = false;
						List<Integer> numIdxList = new ArrayList<>();
						for (int i = 0; i < 2; i++) {
							numIdxList.add(i);
						}
						Collections.shuffle(numIdxList);
						for (int masuNum : numIdxList) {
							IcebarnSolver.Field virtual = new Field(wkField);
							if (masuNum < 1) {
								if (toYokoWall) {
									virtual.yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
								} else {
									virtual.tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
							} else if (masuNum < 2) {
								if (toYokoWall) {
									virtual.yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								} else {
									virtual.tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (virtual.solveAndCheck()) {
								isOk = true;
								wkField.masu = virtual.masu;
								wkField.yokoExtraWall = virtual.yokoExtraWall;
								wkField.tateExtraWall = virtual.tateExtraWall;
							}
						}
						if (!isOk) {
							// 破綻したら0から作り直す。
							wkField = new IcebarnSolver.Field(height, width);
							Collections.shuffle(indexList);
							index = 0;
							continue;
						}
					}
					index++;
				}
				// 矢印埋め
				// まずスタートの位置を探す
				Position nowPos = null;
				Direction from = null;
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					if (wkField.yokoExtraWallDirection[yIndex][0] == Direction.RIGHT) {
						nowPos = new Position(yIndex, 0);
						from = Direction.LEFT;
						break;
					} else if (wkField.yokoExtraWallDirection[yIndex][wkField.getXLength()] == Direction.LEFT) {
						nowPos = new Position(yIndex, wkField.getXLength() - 1);
						from = Direction.RIGHT;
						break;
					}
				}
				if (nowPos == null) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						if (wkField.tateExtraWallDirection[0][xIndex] == Direction.DOWN) {
							nowPos = new Position(0, xIndex);
							from = Direction.UP;
							break;
						} else if (wkField.tateExtraWallDirection[wkField.getYLength()][xIndex] == Direction.UP) {
							nowPos = new Position(wkField.getYLength() - 1, xIndex);
							from = Direction.DOWN;
							break;
						}
					}
				}
				// そこから1マスずつゴールまでつなげる
				List<WallPos> wallPosList = new ArrayList<>();
				while (true) {
					if (nowPos.getxIndex() == -1 || nowPos.getxIndex() == wkField.getXLength()
							|| nowPos.getyIndex() == -1 || nowPos.getyIndex() == wkField.getYLength()) {
						// 最後に追加した枠外の壁は削除判定対象外
						wallPosList.remove(wallPosList.size() - 1);
						break;
					}
					if (wkField.icebarnPosSet.contains(nowPos)) {
						if (from == Direction.UP) {
							wkField.tateExtraWallDirection[nowPos.getyIndex() + 1][nowPos.getxIndex()] = Direction.DOWN;
							wallPosList.add(new WallPos(false, nowPos.getyIndex() + 1, nowPos.getxIndex()));
							nowPos = new Position(nowPos.getyIndex() + 1, nowPos.getxIndex());
							continue;
						} else if (from == Direction.RIGHT) {
							wkField.yokoExtraWallDirection[nowPos.getyIndex()][nowPos.getxIndex()] = Direction.LEFT;
							wallPosList.add(new WallPos(true, nowPos.getyIndex(), nowPos.getxIndex()));
							nowPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() - 1);
							continue;
						} else if (from == Direction.DOWN) {
							wkField.tateExtraWallDirection[nowPos.getyIndex()][nowPos.getxIndex()] = Direction.UP;
							wallPosList.add(new WallPos(false, nowPos.getyIndex(), nowPos.getxIndex()));
							nowPos = new Position(nowPos.getyIndex() - 1, nowPos.getxIndex());
							continue;
						} else if (from == Direction.LEFT) {
							wkField.yokoExtraWallDirection[nowPos.getyIndex()][nowPos.getxIndex()
									+ 1] = Direction.RIGHT;
							wallPosList.add(new WallPos(true, nowPos.getyIndex(), nowPos.getxIndex() + 1));
							nowPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() + 1);
							continue;
						}
					} else {
						if (from != Direction.DOWN && wkField.tateExtraWall[nowPos.getyIndex() + 1][nowPos
								.getxIndex()] == Wall.NOT_EXISTS) {
							wkField.tateExtraWallDirection[nowPos.getyIndex() + 1][nowPos.getxIndex()] = Direction.DOWN;
							wallPosList.add(new WallPos(false, nowPos.getyIndex() + 1, nowPos.getxIndex()));
							nowPos = new Position(nowPos.getyIndex() + 1, nowPos.getxIndex());
							from = Direction.UP;
							continue;
						} else if (from != Direction.LEFT && wkField.yokoExtraWall[nowPos.getyIndex()][nowPos
								.getxIndex()] == Wall.NOT_EXISTS) {
							wkField.yokoExtraWallDirection[nowPos.getyIndex()][nowPos.getxIndex()] = Direction.LEFT;
							wallPosList.add(new WallPos(true, nowPos.getyIndex(), nowPos.getxIndex()));
							nowPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() - 1);
							from = Direction.RIGHT;
							continue;
						} else if (from != Direction.UP && wkField.tateExtraWall[nowPos.getyIndex()][nowPos
								.getxIndex()] == Wall.NOT_EXISTS) {
							wkField.tateExtraWallDirection[nowPos.getyIndex()][nowPos.getxIndex()] = Direction.UP;
							wallPosList.add(new WallPos(false, nowPos.getyIndex(), nowPos.getxIndex()));
							nowPos = new Position(nowPos.getyIndex() - 1, nowPos.getxIndex());
							from = Direction.DOWN;
							continue;
						} else if (from != Direction.RIGHT && wkField.yokoExtraWall[nowPos.getyIndex()][nowPos
								.getxIndex() + 1] == Wall.NOT_EXISTS) {
							wkField.yokoExtraWallDirection[nowPos.getyIndex()][nowPos.getxIndex()
									+ 1] = Direction.RIGHT;
							wallPosList.add(new WallPos(true, nowPos.getyIndex(), nowPos.getxIndex() + 1));
							from = Direction.LEFT;
							nowPos = new Position(nowPos.getyIndex(), nowPos.getxIndex() + 1);
							continue;
						}
					}
				}
				System.out.println(wkField);
				// マスを戻す
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 1; xIndex < wkField.getXLength(); xIndex++) {
						wkField.yokoExtraWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				for (int yIndex = 1; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.tateExtraWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
				for (int yIndex = 0; yIndex < wkField.getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < wkField.getXLength(); xIndex++) {
						wkField.masu[yIndex][xIndex] = Masu.SPACE;
					}
				}
				for (WallPos wallPos : wallPosList) {
					if (wallPos.isYokoWall) {
						wkField.yokoExtraWall[wallPos.yIndex][wallPos.xIndex] = Wall.NOT_EXISTS;
					} else {
						wkField.tateExtraWall[wallPos.yIndex][wallPos.xIndex] = Wall.NOT_EXISTS;
					}
				}
				// 解けるかな？
				level = new IcebarnSolverForGenerator(new IcebarnSolver.Field(wkField), 100).solve2();
				if (level == -1) {
					// 解けなければやり直し
					wkField = new IcebarnSolver.Field(height, width);
					Collections.shuffle(indexList);
					index = 0;
				} else {
					// ヒントを限界まで減らす
					Collections.shuffle(wallPosList);
					for (WallPos wallPos : wallPosList) {
						Field virtual = new IcebarnSolver.Field(wkField, true);
						if (wallPos.isYokoWall) {
							virtual.yokoExtraWall[wallPos.yIndex][wallPos.xIndex] = Wall.SPACE;
							virtual.yokoExtraWallDirection[wallPos.yIndex][wallPos.xIndex] = null;
						} else {
							virtual.tateExtraWall[wallPos.yIndex][wallPos.xIndex] = Wall.SPACE;
							virtual.tateExtraWallDirection[wallPos.yIndex][wallPos.xIndex] = null;
						}
						int solveResult = new IcebarnSolverForGenerator(virtual, 5000).solve2();
						if (solveResult != -1) {
							if (wallPos.isYokoWall) {
								wkField.yokoExtraWall[wallPos.yIndex][wallPos.xIndex] = Wall.SPACE;
								wkField.yokoExtraWallDirection[wallPos.yIndex][wallPos.xIndex] = null;
							} else {
								wkField.tateExtraWall[wallPos.yIndex][wallPos.xIndex] = Wall.SPACE;
								wkField.tateExtraWallDirection[wallPos.yIndex][wallPos.xIndex] = null;
							}
							level = solveResult;
						}
					}
					break;
				}
			}
			level = (int) Math.sqrt(level / 3) + 1;
			String status = "Lv:" + level + "の問題を獲得！(バーン/矢印:" + wkField.getHintCount() + ")";
			String url = wkField.getPuzPreURL();
			String link = "<a href=\"" + url + "\" target=\"_blank\">ぱずぷれv3で解く</a>";
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
			sb.append("</svg>");
			System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
			System.out.println(level);
			System.out.println(wkField.getHintCount());
			System.out.println(wkField);
			return new GeneratorResult(status, sb.toString(), link, url, level, "");

		}

	}

	public static class Field {
		static final String ALPHABET_FROM_I = "ijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,-1と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] yokoExtraWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、-1,0と0,0の間に壁があるという意味。外壁有無も考慮が必要なので注意
		private Wall[][] tateExtraWall;
		// マス間の矢印情報
		// 0,0 = trueなら、0,-1と0,0の間に矢印があるという意味。外壁有無も考慮が必要なので注意
		private final Direction[][] yokoExtraWallDirection;
		// マス間の矢印情報
		// 0,0 = trueなら、-1,0と0,0の間に矢印があるという意味。外壁有無も考慮が必要なので注意
		private final Direction[][] tateExtraWallDirection;
		// 個別のアイスバーン
		private final List<Set<Position>> icebarns;
		// アイスバーン全体
		private final Set<Position> icebarnPosSet;

		public Masu[][] getMasu() {
			return masu;
		}

		public String getHintCount() {
			int cnt = 0;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 1; xIndex < getXLength(); xIndex++) {
					if (yokoExtraWallDirection[yIndex][xIndex] != null) {
						cnt++;
					}
				}
			}
			for (int yIndex = 1; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWallDirection[yIndex][xIndex] != null) {
						cnt++;
					}
				}
			}
			return icebarnPosSet.size() + "/" + cnt;
		}

		public String getPuzPreURL() {
			return null;
		}

		public Wall[][] getYokoExtraWall() {
			return yokoExtraWall;
		}

		public Wall[][] getTateExtraWall() {
			return tateExtraWall;
		}

		public Direction[][] getYokoExtraWallDirection() {
			return yokoExtraWallDirection;
		}

		public Direction[][] getTateExtraWallDirection() {
			return tateExtraWallDirection;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Set<Position> getIcebarnPosSet() {
			return icebarnPosSet;
		}

		public Field(int height, int width, String param, int start, int goal) {
			masu = new Masu[height][width];
			yokoExtraWall = new Wall[height][width + 1];
			tateExtraWall = new Wall[height + 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (xIndex == 0 || xIndex == getXLength()) {
						yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
					} else {
						yokoExtraWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (yIndex == 0 || yIndex == getYLength()) {
						tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
					} else {
						tateExtraWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
			}
			// アイスバーンとそうでないマスの判別のため一時的にマスを色分け
			int readPos = 0;
			int bit = 0;
			for (int cnt = 0; cnt < getYLength() * (getXLength()); cnt++) {
				int mod = cnt % 5;
				if (mod == 0) {
					bit = Character.getNumericValue(param.charAt(readPos));
					readPos++;
				}
				if (mod == 4 || cnt == (getYLength() * (getXLength())) - 1) {
					if (mod >= 0) {
						masu[(cnt - mod + 0) / (getXLength())][(cnt - mod + 0) % (getXLength())] = bit
								/ 16
								% 2 == 1 ? Masu.BLACK : Masu.NOT_BLACK;
					}
					if (mod >= 1) {
						masu[(cnt - mod + 1) / (getXLength())][(cnt - mod + 1) % (getXLength())] = bit
								/ 8
								% 2 == 1 ? Masu.BLACK : Masu.NOT_BLACK;
					}
					if (mod >= 2) {
						masu[(cnt - mod + 2) / (getXLength())][(cnt - mod + 2) % (getXLength())] = bit
								/ 4
								% 2 == 1 ? Masu.BLACK : Masu.NOT_BLACK;
					}
					if (mod >= 3) {
						masu[(cnt - mod + 3) / (getXLength())][(cnt - mod + 3) % (getXLength())] = bit
								/ 2
								% 2 == 1 ? Masu.BLACK : Masu.NOT_BLACK;
					}
					if (mod >= 4) {
						masu[(cnt - mod + 4) / (getXLength())][(cnt - mod + 4) % (getXLength())] = bit
								/ 1
								% 2 == 1 ? Masu.BLACK : Masu.NOT_BLACK;
					}
				}
			}
			// 黒マスでつながってるところをアイスバーンにする
			icebarns = new ArrayList<>();
			icebarnPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position pos = new Position(yIndex, xIndex);
						boolean alreadyRoomed = false;
						for (Set<Position> posSet : icebarns) {
							if (posSet.contains(pos)) {
								alreadyRoomed = true;
								break;
							}
						}
						if (!alreadyRoomed) {
							Set<Position> continuePosSet = new HashSet<>();
							continuePosSet.add(pos);
							setContinuePosSet(pos, continuePosSet);
							icebarns.add(continuePosSet);
							icebarnPosSet.addAll(continuePosSet);
						}
					}
				}
			}
			// マスをもとに戻す
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			// 矢印の処理
			// TODO かなり無理やり…
			yokoExtraWallDirection = new Direction[height][width + 1];
			tateExtraWallDirection = new Direction[height + 1][width];
			int index = 0;
			boolean rightDown = false;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				index = index + Character.getNumericValue(ch);
				if (ch == 'z') {
					continue;
				}
				if (index < (height * (width - 1))) {
					// 左向き
					int wkIndex = index;
					yokoExtraWallDirection[wkIndex / (width - 1)][wkIndex % (width - 1) + 1] = Direction.LEFT;
					yokoExtraWall[wkIndex / (width - 1)][wkIndex % (width - 1) + 1] = Wall.NOT_EXISTS;
				} else if (index < (height * (width - 1)) + ((height - 1) * width)) {
					// 上向き
					int wkIndex = index - (height * (width - 1));
					tateExtraWallDirection[wkIndex / width + 1][wkIndex % width] = Direction.UP;
					tateExtraWall[wkIndex / width + 1][wkIndex % width] = Wall.NOT_EXISTS;
					if (index + 1 == (height * (width - 1)) + ((height - 1) * width)) {
						rightDown = true;
					}
				} else if (!rightDown) {
					rightDown = true;
					continue;
				} else if (index < (height * (width - 1)) + ((height - 1) * width) + (height * (width - 1))) {
					// 右向き
					int wkIndex = index - (height * (width - 1)) - ((height - 1) * width);
					yokoExtraWallDirection[wkIndex / (width - 1)][wkIndex % (width - 1) + 1] = Direction.RIGHT;
					yokoExtraWall[wkIndex / (width - 1)][wkIndex % (width - 1) + 1] = Wall.NOT_EXISTS;
				} else if (index < (height * (width - 1)) + ((height - 1) * width) + (height * (width - 1))
						+ ((height - 1) * width)) {
					// 下向き
					int wkIndex = index - (height * (width - 1)) - ((height - 1) * width) - (height * (width - 1));
					tateExtraWallDirection[wkIndex / width + 1][wkIndex % width] = Direction.DOWN;
					tateExtraWall[wkIndex / width + 1][wkIndex % width] = Wall.NOT_EXISTS;
				}
				index++;
			}
			// 入口と出口
			if (start < width) {
				tateExtraWallDirection[0][start] = Direction.DOWN;
				tateExtraWall[0][start] = Wall.NOT_EXISTS;
			} else if (start < width * 2) {
				tateExtraWallDirection[getYLength()][start - width] = Direction.UP;
				tateExtraWall[getYLength()][start - width] = Wall.NOT_EXISTS;
			} else if (start < width * 2 + height) {
				yokoExtraWallDirection[start - width - width][0] = Direction.RIGHT;
				yokoExtraWall[start - width - width][0] = Wall.NOT_EXISTS;
			} else {
				yokoExtraWallDirection[start - width - width - height][getXLength()] = Direction.LEFT;
				yokoExtraWall[start - width - width - height][getXLength()] = Wall.NOT_EXISTS;
			}

			if (goal < width) {
				tateExtraWallDirection[0][goal] = Direction.UP;
				tateExtraWall[0][goal] = Wall.NOT_EXISTS;
			} else if (goal < width * 2) {
				tateExtraWallDirection[getYLength()][goal - width] = Direction.DOWN;
				tateExtraWall[getYLength()][goal - width] = Wall.NOT_EXISTS;
			} else if (goal < width * 2 + height) {
				yokoExtraWallDirection[goal - width - width][0] = Direction.LEFT;
				yokoExtraWall[goal - width - width][0] = Wall.NOT_EXISTS;
			} else {
				yokoExtraWallDirection[goal - width - width - height][getXLength()] = Direction.RIGHT;
				yokoExtraWall[goal - width - width - height][getXLength()] = Wall.NOT_EXISTS;
			}

		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			yokoExtraWall = new Wall[other.getYLength()][other.getXLength() + 1];
			tateExtraWall = new Wall[other.getYLength() + 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = other.yokoExtraWall[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = other.tateExtraWall[yIndex][xIndex];
				}
			}
			icebarns = other.icebarns;
			icebarnPosSet = other.icebarnPosSet;
			yokoExtraWallDirection = other.yokoExtraWallDirection;
			tateExtraWallDirection = other.tateExtraWallDirection;
		}

		/**
		 * ジェネレータ用初期配置生成
		 */
		public Field(int height, int width) {
			masu = new Masu[height][width];
			yokoExtraWall = new Wall[height][width + 1];
			tateExtraWall = new Wall[height + 1][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (xIndex == 0 || xIndex == getXLength()) {
						yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
					} else {
						yokoExtraWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (yIndex == 0 || yIndex == getYLength()) {
						tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
					} else {
						tateExtraWall[yIndex][xIndex] = Wall.SPACE;
					}
				}
			}
			// アイスバーン生成のため一時的に黒マス化
			double barnRate = Math.random() / 2.5 + 0.15;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Math.random() < barnRate ? Masu.BLACK : Masu.SPACE;
				}
			}
			// 黒マスでつながってるところをアイスバーンにする
			icebarns = new ArrayList<>();
			icebarnPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Position pos = new Position(yIndex, xIndex);
						boolean alreadyRoomed = false;
						for (Set<Position> posSet : icebarns) {
							if (posSet.contains(pos)) {
								alreadyRoomed = true;
								break;
							}
						}
						if (!alreadyRoomed) {
							Set<Position> continuePosSet = new HashSet<>();
							continuePosSet.add(pos);
							setContinuePosSet(pos, continuePosSet);
							icebarns.add(continuePosSet);
							icebarnPosSet.addAll(continuePosSet);
						}
					}
				}
			}
			// マスをもとに戻す
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			// スタートとゴールをランダムで穴あけ
			yokoExtraWallDirection = new Direction[height][width + 1];
			tateExtraWallDirection = new Direction[height + 1][width];
			int start = (int) (Math.random() * ((height * 2) + (width * 2)));
			int goal = (int) (Math.random() * ((height * 2) + (width * 2)));
			while (start == goal) {
				goal = (int) (Math.random() * ((height * 2) + (width * 2)));
			}
			if (start < width) {
				tateExtraWallDirection[0][start] = Direction.DOWN;
				tateExtraWall[0][start] = Wall.NOT_EXISTS;
			} else if (start < width * 2) {
				tateExtraWallDirection[getYLength()][start - width] = Direction.UP;
				tateExtraWall[getYLength()][start - width] = Wall.NOT_EXISTS;
			} else if (start < width * 2 + height) {
				yokoExtraWallDirection[start - width - width][0] = Direction.RIGHT;
				yokoExtraWall[start - width - width][0] = Wall.NOT_EXISTS;
			} else {
				yokoExtraWallDirection[start - width - width - height][getXLength()] = Direction.LEFT;
				yokoExtraWall[start - width - width - height][getXLength()] = Wall.NOT_EXISTS;
			}
			if (goal < width) {
				tateExtraWallDirection[0][goal] = Direction.UP;
				tateExtraWall[0][goal] = Wall.NOT_EXISTS;
			} else if (goal < width * 2) {
				tateExtraWallDirection[getYLength()][goal - width] = Direction.DOWN;
				tateExtraWall[getYLength()][goal - width] = Wall.NOT_EXISTS;
			} else if (goal < width * 2 + height) {
				yokoExtraWallDirection[goal - width - width][0] = Direction.LEFT;
				yokoExtraWall[goal - width - width][0] = Wall.NOT_EXISTS;
			} else {
				yokoExtraWallDirection[goal - width - width - height][getXLength()] = Direction.RIGHT;
				yokoExtraWall[goal - width - width - height][getXLength()] = Wall.NOT_EXISTS;
			}
		}

		public Field(Field other, boolean flag) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			yokoExtraWall = new Wall[other.getYLength()][other.getXLength() + 1];
			tateExtraWall = new Wall[other.getYLength() + 1][other.getXLength()];
			yokoExtraWallDirection = new Direction[other.getYLength()][other.getXLength() + 1];
			tateExtraWallDirection = new Direction[other.getYLength() + 1][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					yokoExtraWall[yIndex][xIndex] = other.yokoExtraWall[yIndex][xIndex];
					yokoExtraWallDirection[yIndex][xIndex] = other.yokoExtraWallDirection[yIndex][xIndex];
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					tateExtraWall[yIndex][xIndex] = other.tateExtraWall[yIndex][xIndex];
					tateExtraWallDirection[yIndex][xIndex] = other.tateExtraWallDirection[yIndex][xIndex];
				}
			}
			icebarns = other.icebarns;
			icebarnPosSet = other.icebarnPosSet;
		}

		// posを起点に上下左右に黒マスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && masu[nextPos.getyIndex()][nextPos.getxIndex()] == Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWallDirection[yIndex][xIndex] != null) {
						sb.append(tateExtraWallDirection[yIndex][xIndex].getDirectString());
					} else {
						sb.append(tateExtraWall[yIndex][xIndex]);
					}
					sb.append("□");
				}
				sb.append(System.lineSeparator());
				if (yIndex != getYLength()) {
					for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
						if (yokoExtraWallDirection[yIndex][xIndex] != null) {
							sb.append(yokoExtraWallDirection[yIndex][xIndex].getDirectString());
						} else {
							sb.append(yokoExtraWall[yIndex][xIndex]);
						}
						if (xIndex != getXLength()) {
							if (icebarnPosSet.contains(new Position(yIndex, xIndex))) {
								sb.append("○");
							} else {
								sb.append(masu[yIndex][xIndex]);
							}
						}
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					sb.append(yokoExtraWall[yIndex][xIndex]);
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(tateExtraWall[yIndex][xIndex]);
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
			if (!icebarnSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!flowSolve()) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 流れの通りにたどっていったとき、流れに逆から侵入したらfalseを返す。
		 * また、流れに逆らってたどって行ったとき、流れの通りに侵入したらfalseを返す。
		 */
		private boolean flowSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position pos = new Position(yIndex, xIndex);
						Set<Position> continuePosSet = new HashSet<>();
						continuePosSet.add(pos);
						if (!checkAllFlow(pos, continuePosSet, null, 0)) {
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 流れをチェックする。0は流れ不確定、1は正方向、-1は逆方向。
		 */
		private boolean checkAllFlow(Position pos, Set<Position> continuePosSet, Direction from, int flow) {
			boolean isIceBarn = icebarnPosSet.contains(pos);
			if ((isIceBarn && from == Direction.DOWN) ||
					(!isIceBarn && from != Direction.UP
							&& tateExtraWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS)) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				int nextFlow = flow;
				if (tateExtraWallDirection[pos.getyIndex()][pos.getxIndex()] == Direction.DOWN) {
					if (nextFlow == -1) {
						return false;
					} else {
						nextFlow = 1;
					}
				}
				if (tateExtraWallDirection[pos.getyIndex()][pos.getxIndex()] == Direction.UP) {
					if (nextFlow == 1) {
						return false;
					} else {
						nextFlow = -1;
					}
				}
				if (!icebarnPosSet.contains(nextPos) && continuePosSet.contains(nextPos)) {
					return false;
				}
				if (nextPos.getyIndex() == -1) {
					return true;
				}
				continuePosSet.add(nextPos);
				return checkAllFlow(nextPos, continuePosSet, Direction.DOWN, nextFlow);

			}
			if ((isIceBarn && from == Direction.LEFT) || (!isIceBarn && from != Direction.RIGHT
					&& yokoExtraWall[pos.getyIndex()][pos.getxIndex() + 1] == Wall.NOT_EXISTS)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				int nextFlow = flow;
				if (yokoExtraWallDirection[pos.getyIndex()][pos.getxIndex() + 1] == Direction.LEFT) {
					if (nextFlow == -1) {
						return false;
					} else {
						nextFlow = 1;
					}
				}
				if (yokoExtraWallDirection[pos.getyIndex()][pos.getxIndex() + 1] == Direction.RIGHT) {
					if (nextFlow == 1) {
						return false;
					} else {
						nextFlow = -1;
					}
				}
				if (!icebarnPosSet.contains(nextPos) && continuePosSet.contains(nextPos)) {
					return false;
				}
				if (nextPos.getxIndex() == getXLength()) {
					return true;
				}
				continuePosSet.add(nextPos);
				return checkAllFlow(nextPos, continuePosSet, Direction.LEFT, nextFlow);

			}
			if ((isIceBarn && from == Direction.UP) || (!isIceBarn && from != Direction.DOWN
					&& tateExtraWall[pos.getyIndex() + 1][pos.getxIndex()] == Wall.NOT_EXISTS)) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				int nextFlow = flow;
				if (tateExtraWallDirection[pos.getyIndex() + 1][pos.getxIndex()] == Direction.UP) {
					if (nextFlow == -1) {
						return false;
					} else {
						nextFlow = 1;
					}
				}
				if (tateExtraWallDirection[pos.getyIndex() + 1][pos.getxIndex()] == Direction.DOWN) {
					if (nextFlow == 1) {
						return false;
					} else {
						nextFlow = -1;
					}
				}

				if (!icebarnPosSet.contains(nextPos) && continuePosSet.contains(nextPos)) {
					return false;
				}
				if (nextPos.getyIndex() == getYLength()) {
					return true;
				}
				continuePosSet.add(nextPos);
				return checkAllFlow(nextPos, continuePosSet, Direction.UP, nextFlow);

			}
			if ((isIceBarn && from == Direction.RIGHT) || (!isIceBarn && from != Direction.LEFT
					&& yokoExtraWall[pos.getyIndex()][pos.getxIndex()] == Wall.NOT_EXISTS)) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				int nextFlow = flow;
				if (yokoExtraWallDirection[pos.getyIndex()][pos.getxIndex()] == Direction.RIGHT) {
					if (nextFlow == -1) {
						return false;
					} else {
						nextFlow = 1;
					}
				}
				if (yokoExtraWallDirection[pos.getyIndex()][pos.getxIndex()] == Direction.LEFT) {
					if (nextFlow == 1) {
						return false;
					} else {
						nextFlow = -1;
					}
				}
				if (!icebarnPosSet.contains(nextPos) && continuePosSet.contains(nextPos)) {
					return false;
				}
				if (nextPos.getxIndex() == -1) {
					return true;
				}
				continuePosSet.add(nextPos);
				return checkAllFlow(nextPos, continuePosSet, Direction.RIGHT, nextFlow);

			}
			return true;
		}

		/**
		 * 黒マスの周囲の壁を埋める。
		 * また、白マス隣接セルの周辺の壁の数は、アイスバーンなら0または2(直進)、
		 * それ以外なら2になるので、矛盾する場合はfalseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					int existsCount = 0;
					int notExistsCount = 0;
					Wall wallUp = tateExtraWall[yIndex][xIndex];
					if (wallUp == Wall.EXISTS) {
						existsCount++;
					} else if (wallUp == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallRight = yokoExtraWall[yIndex][xIndex + 1];
					if (wallRight == Wall.EXISTS) {
						existsCount++;
					} else if (wallRight == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallDown = tateExtraWall[yIndex + 1][xIndex];
					if (wallDown == Wall.EXISTS) {
						existsCount++;
					} else if (wallDown == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					Wall wallLeft = yokoExtraWall[yIndex][xIndex];
					if (wallLeft == Wall.EXISTS) {
						existsCount++;
					} else if (wallLeft == Wall.NOT_EXISTS) {
						notExistsCount++;
					}
					if (icebarnPosSet.contains(new Position(yIndex, xIndex))) {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							// 氷の上の場合、自分が不確定マスなら壁は0マスか2マスか4マス
							if ((existsCount == 3 && notExistsCount == 1)
									|| (existsCount == 1 && notExistsCount == 3)) {
								return false;
							}
							if (existsCount > 2) {
								masu[yIndex][xIndex] = Masu.BLACK;
							} else if (notExistsCount != 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							// 氷の上の場合、自分が白マスなら壁は0マスか2マス
							if (existsCount > 2 || (existsCount == 1 && notExistsCount == 3)) {
								return false;
							}
							// カーブ禁止
							if ((wallUp == Wall.EXISTS && wallRight == Wall.EXISTS)
									|| (wallUp == Wall.EXISTS && wallLeft == Wall.EXISTS)
									|| (wallRight == Wall.EXISTS && wallDown == Wall.EXISTS)
									|| (wallDown == Wall.EXISTS && wallLeft == Wall.EXISTS)
									|| (wallUp == Wall.EXISTS && wallDown == Wall.NOT_EXISTS)
									|| (wallDown == Wall.EXISTS && wallUp == Wall.NOT_EXISTS)
									|| (wallRight == Wall.EXISTS && wallLeft == Wall.NOT_EXISTS)
									|| (wallLeft == Wall.EXISTS && wallRight == Wall.NOT_EXISTS)) {
								return false;
							}
							if (existsCount == 1 && notExistsCount == 2) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
							} else if (existsCount == 2) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							} else if (notExistsCount == 3) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							}
							if (wallUp == Wall.EXISTS) {
								yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								tateExtraWall[yIndex + 1][xIndex] = Wall.EXISTS;
								yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.EXISTS) {
								tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
								yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
								tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallDown == Wall.EXISTS) {
								yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
								yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.EXISTS) {
								tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
								yokoExtraWall[yIndex][xIndex + 1] = Wall.EXISTS;
								tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallUp == Wall.NOT_EXISTS) {
								tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallRight == Wall.NOT_EXISTS) {
								yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallDown == Wall.NOT_EXISTS) {
								tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
							}
							if (wallLeft == Wall.NOT_EXISTS) {
								yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
							}
						}
					} else {
						if (masu[yIndex][xIndex] == Masu.SPACE) {
							// 氷の上でない場合、自分が不確定マスなら壁は2マスか4マス
							if ((existsCount == 3 && notExistsCount == 1)
									|| notExistsCount > 2) {
								return false;
							}
							if (existsCount > 2) {
								masu[yIndex][xIndex] = Masu.BLACK;
							} else if (notExistsCount != 0) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
						if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
							// 氷の上でない場合、自分が白マスなら壁は必ず2マス
							if (existsCount > 2 || notExistsCount > 2) {
								return false;
							}
							if (existsCount == 2) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.NOT_EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.NOT_EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
								}
							} else if (notExistsCount == 2) {
								if (wallUp == Wall.SPACE) {
									tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
								if (wallRight == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex + 1] = Wall.EXISTS;
								}
								if (wallDown == Wall.SPACE) {
									tateExtraWall[yIndex + 1][xIndex] = Wall.EXISTS;
								}
								if (wallLeft == Wall.SPACE) {
									yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
								}
							}
						}
					}
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						// 周囲の壁を閉鎖
						if (tateExtraWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							return false;
						}
						tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
						if (yokoExtraWall[yIndex][xIndex + 1] == Wall.NOT_EXISTS) {
							return false;
						}
						yokoExtraWall[yIndex][xIndex + 1] = Wall.EXISTS;
						if (tateExtraWall[yIndex + 1][xIndex] == Wall.NOT_EXISTS) {
							return false;
						}
						tateExtraWall[yIndex + 1][xIndex] = Wall.EXISTS;
						if (yokoExtraWall[yIndex][xIndex] == Wall.NOT_EXISTS) {
							return false;
						}
						yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
					}
				}
			}
			return true;
		}

		/**
		 * 各アイスバーンには1マスは白マスが必要。
		 * 違反の場合はfalseを返す。
		 */
		private boolean icebarnSolve() {
			for (Set<Position> icebarn : icebarns) {
				boolean discoverWhite = false;
				Position spacePos = null;
				boolean spacePosSingle = true;
				for (Position pos : icebarn) {
					if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK) {
						discoverWhite = true;
						break;
					} else if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						if (spacePos != null) {
							spacePosSingle = false;
							break;
						}
						spacePos = pos;
					}
				}
				if (!discoverWhite) {
					if (spacePos == null) {
						return false;
					} else if (spacePosSingle) {
						masu[spacePos.getyIndex()][spacePos.getxIndex()] = Masu.NOT_BLACK;
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
				for (int xIndex = 0; xIndex < getXLength() + 1; xIndex++) {
					if (yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() + 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count = 0;

	public IcebarnSolver(int height, int width, String param, int start, int goal) {
		field = new Field(height, width, param, start, goal);
	}

	public IcebarnSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		String url = ""; //urlを入れれば試せる
		String[] params = url.split("/");
		int height = Integer.parseInt(params[params.length - 4]);
		int width = Integer.parseInt(params[params.length - 5]);
		String param = params[params.length - 3];
		int start = Integer.parseInt(params[params.length - 2]);
		int goal = Integer.parseInt(params[params.length - 1]);
		System.out.println(new IcebarnSolver(height, width, param, start, goal).solve());
	}

	@Override
	public String solve() {
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				System.out.println(field);
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				System.out.println(field);
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + count);
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength() + 1; xIndex++) {
				if (field.yokoExtraWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandYokoWallSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
			}
		}
		for (int yIndex = 0; yIndex < field.getYLength() + 1; yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.tateExtraWall[yIndex][xIndex] == Wall.SPACE) {
					count++;
					if (!oneCandTateWallSolve(field, yIndex, xIndex, recursive)) {
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

	private boolean oneCandYokoWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.yokoExtraWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.yokoExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
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
			field.tateExtraWall = virtual2.tateExtraWall;
			field.yokoExtraWall = virtual2.yokoExtraWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateExtraWall = virtual.tateExtraWall;
			field.yokoExtraWall = virtual.yokoExtraWall;
		}
		return true;
	}

	private boolean oneCandTateWallSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.tateExtraWall[yIndex][xIndex] = Wall.EXISTS;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.tateExtraWall[yIndex][xIndex] = Wall.NOT_EXISTS;
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
			field.tateExtraWall = virtual2.tateExtraWall;
			field.yokoExtraWall = virtual2.yokoExtraWall;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.tateExtraWall = virtual.tateExtraWall;
			field.yokoExtraWall = virtual.yokoExtraWall;
		}
		return true;
	}

}
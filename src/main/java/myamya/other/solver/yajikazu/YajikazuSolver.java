package myamya.other.solver.yajikazu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class YajikazuSolver implements Solver {

	/**
	 * 矢印を候補を列挙して解くためのサブソルバー
	 * …だったけど結局使わない方が早かったという。。。
	 */
	static class ArrowSolver {

		/**
		 * 最大候補利用数
		 * 0にすると候補を利用したトライをしなくなる
		 */
		private static final int CANDMAX = 0;

		private final int height;
		private final int width;
		private final Arrow arrow;

		public ArrowSolver(int height, int width, Arrow arrow) {
			this.height = height;
			this.width = width;
			this.arrow = arrow;
		}

		public static void main(String[] args) {
			List<String> result = new ArrowSolver(10, 10, new Arrow(new Position(9, 5), Direction.UP, '3'))
					.solve();
			System.out.println(result);
		}

		/**
		 * 盤面の高さ・幅・矢印の位置・矢印の内容からマスの候補を返却する。
		 */
		public List<String> solve() {
			List<String> result = new ArrayList<>();
			oneSolve(result, new StringBuilder(), arrow.getPosition(), 0);
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
		private final Position position;
		private final Direction direction;
		private final int count;

		public Arrow(Position position, Direction direction, int count) {
			this.position = position;
			this.direction = direction;
			this.count = count;
		}

		public Position getPosition() {
			return position;
		}

		public Direction getDirection() {
			return direction;
		}

		public int getCount() {
			return count;
		}

		@Override
		public String toString() {
			return (count >= 10 ? String.valueOf(count) : direction.toString() + count);
		}

		public String toStringForweb() {
			return direction.getDirectString() + count;
		}
	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 矢印の情報
		private final Arrow[][] arrows;
		// 矢印の候補情報
		private final Map<Arrow, List<String>> arrowsInfo;

		public Masu[][] getMasu() {
			return masu;
		}

		public Arrow[][] getArrows() {
			return arrows;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			arrows = new Arrow[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
				}
			}
			arrowsInfo = new HashMap<>();
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
							}
							index++;
							i++;
							adjust = false;
						}
					}
				} else {
					if (Character.getNumericValue(ch) != -1) {
						Arrow arrow;
						Position arrowPos = new Position(index / getXLength(), index % getXLength());
						if (adjust) {
							i++;
							arrow = new Arrow(arrowPos, direction, Character.getNumericValue(ch) * 16
									+ Character.getNumericValue(param.charAt(i)));
						} else {
							arrow = new Arrow(arrowPos, direction, Character.getNumericValue(ch));
						}
						arrows[index / getXLength()][index % getXLength()] = arrow;
						arrowsInfo.put(arrow,
								new ArrowSolver(getYLength(), getXLength(), arrow)
										.solve());
					}
					adjust = false;
					index++;
					direction = null;
				}
			}

		}

		/**
		 * 現在の盤面の状況から矢印候補の絞り込みを行う、
		 * 候補がなくなった矢印のマスを黒マスにする。矢印が黒マスにできない場合、falseを返す。
		 * また、白と決めた矢印のマスの共通候補のマスを確定する際、矛盾が出る場合もfalseを返す。
		 */
		private boolean arrowSolve() {
			// 候補の絞り込み
			for (Entry<Arrow, List<String>> entry : arrowsInfo.entrySet()) {
				Arrow arrow = entry.getKey();
				Position pivot = arrow.getPosition();
				if (entry.getValue() != null) {
					for (Iterator<String> iterator = entry.getValue().iterator(); iterator.hasNext();) {
						String state = iterator.next();
						for (int idx = 0; idx < state.length(); idx++) {
							Position pos = null;
							if (arrow.getDirection() == Direction.UP) {
								pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
							}
							if (arrow.getDirection() == Direction.RIGHT) {
								pos = new Position(pivot.getyIndex(), pivot.getxIndex() + 1 + idx);
							}
							if (arrow.getDirection() == Direction.DOWN) {
								pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex());
							}
							if (arrow.getDirection() == Direction.LEFT) {
								pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
							}
							if ((masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK
									&& state.charAt(idx) == '・')
									|| (masu[pos.getyIndex()][pos.getxIndex()] == Masu.NOT_BLACK
											&& state.charAt(idx) == '■')) {
								iterator.remove();
								break;
							}
						}
					}
					if (entry.getValue().size() == 0) {
						if (masu[pivot.getyIndex()][pivot.getxIndex()] == Masu.NOT_BLACK) {
							return false;
						} else if (masu[pivot.getyIndex()][pivot.getxIndex()] == Masu.SPACE) {
							masu[pivot.getyIndex()][pivot.getxIndex()] = Masu.BLACK;
						}
					} else {
						if (masu[pivot.getyIndex()][pivot.getxIndex()] == Masu.NOT_BLACK) {
							StringBuilder fixState = new StringBuilder(entry.getValue().get(0));
							for (String cand : entry.getValue()) {
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
									pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
								}
								if (arrow.getDirection() == Direction.RIGHT) {
									pos = new Position(pivot.getyIndex(), pivot.getxIndex() + 1 + idx);
								}
								if (arrow.getDirection() == Direction.DOWN) {
									pos = new Position(pivot.getyIndex() + 1 + idx, pivot.getxIndex());
								}
								if (arrow.getDirection() == Direction.LEFT) {
									pos = new Position(pivot.getyIndex(), pivot.getxIndex() - 1 - idx);
								}
								masu[pos.getyIndex()][pos.getxIndex()] = fixState.charAt(idx) == '■'
										? Masu.BLACK
										: fixState.charAt(idx) == '・'
												? Masu.NOT_BLACK
												: Masu.SPACE;
							}
						}
					}
				} else {
					int idx = 0;
					int blackCnt = 0;
					int spaceCnt = 0;
					boolean nextCanSpace = true;
					if (arrow.getDirection() == Direction.UP) {
						while (pivot.getyIndex() - 1 - idx >= 0) {
							Position pos = new Position(pivot.getyIndex() - 1 - idx, pivot.getxIndex());
							if (masu[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
								blackCnt++;
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
					if (arrow.getCount() < blackCnt || arrow.getCount() > blackCnt + spaceCnt) {
						if (masu[pivot.getyIndex()][pivot.getxIndex()] == Masu.NOT_BLACK) {
							return false;
						} else if (masu[pivot.getyIndex()][pivot.getxIndex()] == Masu.SPACE) {
							masu[pivot.getyIndex()][pivot.getxIndex()] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			arrowsInfo = new HashMap<>();
			for (Entry<Arrow, List<String>> entry : other.arrowsInfo.entrySet()) {
				arrowsInfo.put(entry.getKey(), entry.getValue() == null ? null : new ArrayList<>(entry.getValue()));
			}
			arrows = other.arrows;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] != Masu.BLACK && arrows[yIndex][xIndex] != null) {
						sb.append(arrows[yIndex][xIndex]);
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
		 * 黒マス隣接セルを白マスにする。
		 * 黒マス隣接セルが黒マスの場合falseを返す。
		 */
		public boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
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
					}
				}
			}
			return true;
		}

		/**
		 * 白マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			Position typicalWhitePos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						Position whitePos = new Position(yIndex, xIndex);
						whitePosSet.add(whitePos);
						if (typicalWhitePos == null) {
							typicalWhitePos = whitePos;
						}
					}
				}
			}
			if (typicalWhitePos == null) {
				return true;
			} else {
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				setContinueNotBlackPosSet(typicalWhitePos, continuePosSet);
				whitePosSet.removeAll(continuePosSet);
				return whitePosSet.isEmpty();
			}
		}

		/**
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
		 */
		private void setContinueNotBlackPosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueNotBlackPosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueNotBlackPosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueNotBlackPosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinueNotBlackPosSet(nextPos, continuePosSet);
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
			if (!connectSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
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

	private final Field field;

	public YajikazuSolver(int height, int width, String param) {
		field = new Field(height, width, param);
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
		System.out.println(new YajikazuSolver(height, width, param).solve());
	}

	@Override
	public String solve() {
		int difficulty = 0;
		long start = System.nanoTime();
		while (!field.isSolved()) {
			System.out.println(field);
			String befStr = field.getStateDump();
			if (!field.solveAndCheck()) {
				return "問題に矛盾がある可能性があります。途中経過を返します。";
			}
			int recursiveCnt = 0;
			while (field.getStateDump().equals(befStr) && recursiveCnt < 3) {
				difficulty = difficulty <= recursiveCnt ? recursiveCnt + 1 : difficulty;
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
		System.out.println("難易度:" + (difficulty));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByVal(difficulty).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private static boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
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
	private static boolean oneCandSolve(Field field, int yIndex, int xIndex, int recursive) {
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
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
		}
		return true;
	}
}

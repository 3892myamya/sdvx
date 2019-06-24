package myamya.other.solver.heyawake;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Position;

public class HeyaSolver {

	public enum Masu {
		SPACE("　"), BLACK("■"), WALL("□"), NOT_BLACK("・"), NOT_WALL("＊");

		String str;

		Masu(String str) {
			this.str = str;
		}

		@Override
		public String toString() {
			return str;
		}
	}

	// マスの情報
	private Masu[][] masu;

	private int roomBlackCnt;

	public Masu[][] getMasu() {
		return masu;
	}

	public int getYLength() {
		return masu.length;
	}

	public int getXLength() {
		return masu[0].length;
	}

	private final int limit;

	public HeyaSolver(int height, int width, int roomBlackCnt, boolean upWall, boolean rightWall, boolean downWall,
			boolean leftWall, int limit) {
		masu = new Masu[height + 2][width + 2];
		this.roomBlackCnt = roomBlackCnt;
		for (int yIndex = 1; yIndex < getYLength() - 1; yIndex++) {
			for (int xIndex = 1; xIndex < getXLength() - 1; xIndex++) {
				masu[yIndex][xIndex] = Masu.SPACE;
			}
		}
		for (int xIndex = 1; xIndex < getXLength() - 1; xIndex++) {
			if (upWall) {
				masu[0][xIndex] = Masu.WALL;
			} else {
				masu[0][xIndex] = Masu.NOT_WALL;
			}
			if (downWall) {
				masu[getYLength() - 1][xIndex] = Masu.WALL;
			} else {
				masu[getYLength() - 1][xIndex] = Masu.NOT_WALL;
			}
		}
		for (int yIndex = 1; yIndex < getYLength() - 1; yIndex++) {
			if (leftWall) {
				masu[yIndex][0] = Masu.WALL;
			} else {
				masu[yIndex][0] = Masu.NOT_WALL;
			}
			if (rightWall) {
				masu[yIndex][getXLength() - 1] = Masu.WALL;
			} else {
				masu[yIndex][getXLength() - 1] = Masu.NOT_WALL;
			}
		}
		masu[0][0] = !upWall && !leftWall ? Masu.NOT_WALL : Masu.WALL;
		masu[0][getXLength() - 1] = !upWall && !rightWall ? Masu.NOT_WALL : Masu.WALL;
		masu[getYLength() - 1][0] = !downWall && !leftWall ? Masu.NOT_WALL : Masu.WALL;
		masu[getYLength() - 1][getXLength() - 1] = !downWall && !rightWall ? Masu.NOT_WALL : Masu.WALL;
		this.limit = limit;
	}

	public HeyaSolver(HeyaSolver other) {
		masu = new Masu[other.getYLength()][other.getXLength()];
		for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
			}
		}
		roomBlackCnt = other.roomBlackCnt;
		limit = other.limit;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				sb.append(masu[yIndex][xIndex]);
			}
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}

	public String getStateDump() {
		StringBuilder sb = new StringBuilder();
		for (int yIndex = 1; yIndex < getYLength() - 1; yIndex++) {
			for (int xIndex = 1; xIndex < getXLength() - 1; xIndex++) {
				sb.append(masu[yIndex][xIndex]);
			}
		}
		return sb.toString();
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
			setContinueWhitePosSet(typicalWhitePos, continuePosSet);
			whitePosSet.removeAll(continuePosSet);
			return whitePosSet.isEmpty();
		}
	}

	/**
	 * posを起点に上下左右に黒確定でないマスをつなげていく。
	 */
	private void setContinueWhitePosSet(Position pos, Set<Position> continuePosSet) {
		if (pos.getyIndex() != 0) {
			Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
			if (!continuePosSet.contains(nextPos)
					&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK
					&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.WALL) {
				continuePosSet.add(nextPos);
				setContinueWhitePosSet(nextPos, continuePosSet);
			}
		}
		if (pos.getxIndex() != getXLength() - 1) {
			Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
			if (!continuePosSet.contains(nextPos)
					&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK
					&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.WALL) {
				continuePosSet.add(nextPos);
				setContinueWhitePosSet(nextPos, continuePosSet);
			}
		}
		if (pos.getyIndex() != getYLength() - 1) {
			Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
			if (!continuePosSet.contains(nextPos)
					&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK
					&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.WALL) {
				continuePosSet.add(nextPos);
				setContinueWhitePosSet(nextPos, continuePosSet);
			}
		}
		if (pos.getxIndex() != 0) {
			Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
			if (!continuePosSet.contains(nextPos)
					&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK
					&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.WALL) {
				continuePosSet.add(nextPos);
				setContinueWhitePosSet(nextPos, continuePosSet);
			}
		}
	}

	public boolean isSolved() {
		for (int yIndex = 1; yIndex < getYLength() - 1; yIndex++) {
			for (int xIndex = 1; xIndex < getXLength() - 1; xIndex++) {
				if (masu[yIndex][xIndex] == Masu.SPACE) {
					return false;
				}
			}
		}
		return true;
	}

	public static void main(String[] args) {
		long start = System.nanoTime();
		List<HeyaSolver> result = new HeyaSolver(5, 5, 9, true, true, true, true, 10000).solve();
		for (HeyaSolver field : result) {
			System.out.println(field);
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println(result.size());

	}

	public List<String> solveForSolver() {
		List<String> result = new ArrayList<>();
		List<HeyaSolver> AnswarFieldList = solve();
		if (AnswarFieldList.size() >= limit) {
			return result;
		}
		for (HeyaSolver answar : AnswarFieldList) {
			result.add(answar.getStateDump());
		}
		return result;
	}

	public List<HeyaSolver> solve() {
		List<HeyaSolver> AnswarFieldList = new ArrayList<>();
		oneCandSolve(this, 0, AnswarFieldList, 0, (getXLength() - 2) * (getYLength() - 2));
		return AnswarFieldList;
	}

	/**
	 * 1つのマスに対する仮置き調査
	 */
	private boolean oneCandSolve(HeyaSolver field, int index, List<HeyaSolver> answarMasuList, int blackCnt,
			int spaceCnt) {
		//		System.out.println(field);
		if (answarMasuList.size() >= limit) {
			// 1000通り以上見つかったら打ち切り
			return true;
		}
		if (field.isSolved()) {
			answarMasuList.add(field);
			return true;
		}
		int yIndex = index / (getXLength() - 2) + 1;
		int xIndex = index % (getXLength() - 2) + 1;
		if (field.masu[yIndex][xIndex] == Masu.SPACE) {
			HeyaSolver virtual = new HeyaSolver(field);
			virtual.masu[yIndex][xIndex] = Masu.BLACK;
			boolean allowBlack;
			if (roomBlackCnt - (blackCnt + 1) < 0) {
				// 黒マス超過
				allowBlack = false;
			} else {
				if (virtual.masu[yIndex - 1][xIndex] == Masu.BLACK || virtual.masu[yIndex][xIndex + 1] == Masu.BLACK
						|| virtual.masu[yIndex + 1][xIndex] == Masu.BLACK
						|| virtual.masu[yIndex][xIndex - 1] == Masu.BLACK) {
					allowBlack = false;
				} else {
					if (!virtual.connectSolve()) {
						allowBlack = false;
					} else {
						allowBlack = !oneCandSolve(virtual, index + 1, answarMasuList, blackCnt + 1, spaceCnt - 1);
					}
				}
			}
			HeyaSolver virtual2 = new HeyaSolver(field);
			virtual2.masu[yIndex][xIndex] = Masu.NOT_BLACK;
			boolean allowNotBlack;
			if (blackCnt + (spaceCnt / 2) < roomBlackCnt) {
				// 黒マス不足
				allowNotBlack = false;
			} else {
				allowNotBlack = !oneCandSolve(virtual2, index + 1, answarMasuList, blackCnt, spaceCnt - 1);
			}
			if (!allowBlack && !allowNotBlack) {
				return false;
			}
		}
		return true;
	}

}

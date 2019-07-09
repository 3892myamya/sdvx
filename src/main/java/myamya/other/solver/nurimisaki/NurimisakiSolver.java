package myamya.other.solver.nurimisaki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class NurimisakiSolver implements Solver {

	public interface Masu {
		boolean isNotBlack();
	}

	public enum MasuImpl implements Masu {
		SPACE("　", false), BLACK("■", false), NOT_BLACK("・", true);

		String str;
		boolean isNotBlack;

		MasuImpl(String str, boolean isNotBlack) {
			this.str = str;
			this.isNotBlack = isNotBlack;
		}

		@Override
		public String toString() {
			return str;
		}

		@Override
		public boolean isNotBlack() {
			return isNotBlack;
		}
	}

	public static class Misaki implements Masu {

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "○①②③④⑤⑥⑦⑧⑨";

		private int cnt;

		Misaki(int cnt) {
			this.cnt = cnt;
		}

		@Override
		public String toString() {
			if (cnt == -1) {
				return "○";
			}
			if (cnt > 99) {
				return "99";
			}
			String capacityStr = String.valueOf(cnt);
			int index = HALF_NUMS.indexOf(capacityStr);
			if (index >= 0) {
				return FULL_NUMS.substring(index / 2, index / 2 + 1);
			} else {
				return capacityStr;
			}
		}

		@Override
		public boolean isNotBlack() {
			return true;
		}

		public int getCnt() {
			return cnt;
		}
	}

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;

		// ぬりみさ木モード
		private boolean tree;

		public Masu[][] getMasu() {
			return masu;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		public Field(int height, int width, String param, boolean tree) {
			masu = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = MasuImpl.SPACE;
				}
			}
			int index = 0;
			for (int i = 0; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == '.') {
					masu[pos.getyIndex()][pos.getxIndex()] = new Misaki(-1);
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						//16 - 255は '-'
						//256 - 999は '+'
						int cnt;
						if (ch == '-') {
							cnt = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							cnt = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							cnt = Integer.parseInt(String.valueOf(ch), 16);
						}
						masu[pos.getyIndex()][pos.getxIndex()] = new Misaki(cnt);
						index++;
					}
				}
			}
			this.tree = tree;
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			tree = other.tree;
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
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 岬のマスから伸ばせる方向が一通りしかない場合、それ以外の方向を黒に、
		 * また、数字付きの岬であれば伸びる数だけ白に確定する。
		 * 伸ばせる方向が0通りまたは2通り以上になる場合はfalseを返す。
		 */
		public boolean misakiSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] instanceof Misaki) {
						Map<Direction, Masu> masuMap = new HashMap<>();
						masuMap.put(Direction.UP, yIndex == 0 ? MasuImpl.BLACK : masu[yIndex - 1][xIndex]);
						masuMap.put(Direction.RIGHT,
								xIndex == getXLength() - 1 ? MasuImpl.BLACK : masu[yIndex][xIndex + 1]);
						masuMap.put(Direction.DOWN,
								yIndex == getYLength() - 1 ? MasuImpl.BLACK : masu[yIndex + 1][xIndex]);
						masuMap.put(Direction.LEFT, xIndex == 0 ? MasuImpl.BLACK : masu[yIndex][xIndex - 1]);
						Set<Direction> misakiDirectionCand = new HashSet<>();
						Set<Direction> misakiDirectionStrongCand = new HashSet<>();
						for (Entry<Direction, Masu> entry : masuMap.entrySet()) {
							if (entry.getValue() == MasuImpl.SPACE) {
								misakiDirectionCand.add(entry.getKey());
							} else if (entry.getValue().isNotBlack()) {
								if (!misakiDirectionStrongCand.isEmpty()) {
									// 2方向に延びる場合、失敗
									return false;
								}
								misakiDirectionStrongCand.add(entry.getKey());
							}
						}
						Misaki oneMasu = (Misaki) masu[yIndex][xIndex];
						if (!misakiDirectionStrongCand.isEmpty()) {
							// 岬の方角が決まっていたら伸ばしていくが、伸ばせないことが分かったらfalseを返す
							if (!misakiStretch(yIndex, xIndex, new ArrayList<>(misakiDirectionStrongCand).get(0),
									oneMasu.getCnt())) {
								return false;
							}
						} else {
							// 岬の方向が未確定なら各方向に伸ばせるかを調査。
							if (misakiDirectionCand.contains(Direction.UP)) {
								Field virtual = new Field(this);
								if (!virtual.misakiStretch(yIndex, xIndex, Direction.UP, oneMasu.getCnt())) {
									misakiDirectionCand.remove(Direction.UP);
								}
							}
							if (misakiDirectionCand.contains(Direction.RIGHT)) {
								Field virtual = new Field(this);
								if (!virtual.misakiStretch(yIndex, xIndex, Direction.RIGHT, oneMasu.getCnt())) {
									misakiDirectionCand.remove(Direction.RIGHT);
								}
							}
							if (misakiDirectionCand.contains(Direction.DOWN)) {
								Field virtual = new Field(this);
								if (!virtual.misakiStretch(yIndex, xIndex, Direction.DOWN, oneMasu.getCnt())) {
									misakiDirectionCand.remove(Direction.DOWN);
								}
							}
							if (misakiDirectionCand.contains(Direction.LEFT)) {
								Field virtual = new Field(this);
								if (!virtual.misakiStretch(yIndex, xIndex, Direction.LEFT, oneMasu.getCnt())) {
									misakiDirectionCand.remove(Direction.LEFT);
								}
							}
							if (misakiDirectionCand.isEmpty()) {
								// どこにも伸ばせない
								return false;
							}
							if (misakiDirectionCand.size() == 1) {
								// 伸びる方向が確定
								misakiStretch(yIndex, xIndex, new ArrayList<>(misakiDirectionCand).get(0),
										oneMasu.getCnt());
							} else {
								// 伸びない方向が確定
								if (!misakiDirectionCand.contains(Direction.UP) && yIndex != 0) {
									masu[yIndex - 1][xIndex] = MasuImpl.BLACK;
								}
								if (!misakiDirectionCand.contains(Direction.RIGHT) && xIndex != getXLength() - 1) {
									masu[yIndex][xIndex + 1] = MasuImpl.BLACK;
								}
								if (!misakiDirectionCand.contains(Direction.DOWN) && yIndex != getYLength() - 1) {
									masu[yIndex + 1][xIndex] = MasuImpl.BLACK;
								}
								if (!misakiDirectionCand.contains(Direction.LEFT) && xIndex != 0) {
									masu[yIndex][xIndex - 1] = MasuImpl.BLACK;
								}
							}
						}
					} else if (masu[yIndex][xIndex] == MasuImpl.NOT_BLACK) {
						int blackCnt = 0;
						if (yIndex == 0 || masu[yIndex - 1][xIndex] == MasuImpl.BLACK) {
							blackCnt++;
						}
						if (xIndex == getXLength() - 1 || masu[yIndex][xIndex + 1] == MasuImpl.BLACK) {
							blackCnt++;
						}
						if (yIndex == getYLength() - 1 || masu[yIndex + 1][xIndex] == MasuImpl.BLACK) {
							blackCnt++;
						}
						if (xIndex == 0 || masu[yIndex][xIndex - 1] == MasuImpl.BLACK) {
							blackCnt++;
						}
						if (blackCnt >= 3) {
							// 岬予定じゃないのに岬になってる
							return false;
						}
					}
				}
			}
			return true;
		}

		/**
		 * yIndex-xIndexにある岬をdirectionの方向に白マスがcntの数になるだけ伸ばす。
		 * 伸ばせなかったらfalseを返す。
		 */
		private boolean misakiStretch(int yIndex, int xIndex, Direction direction, int cnt) {
			// 向き先以外を黒に。黒にできない場合はfalse。
			if (yIndex != 0 && direction != Direction.UP) {
				if (masu[yIndex - 1][xIndex].isNotBlack()) {
					return false;
				} else if (masu[yIndex - 1][xIndex] == MasuImpl.SPACE) {
					masu[yIndex - 1][xIndex] = MasuImpl.BLACK;
				}
			}
			if (xIndex != getXLength() - 1 && direction != Direction.RIGHT) {
				if (masu[yIndex][xIndex + 1].isNotBlack()) {
					return false;
				} else if (masu[yIndex][xIndex + 1] == MasuImpl.SPACE) {
					masu[yIndex][xIndex + 1] = MasuImpl.BLACK;
				}
			}
			if (yIndex != getYLength() - 1 && direction != Direction.DOWN) {
				if (masu[yIndex + 1][xIndex].isNotBlack()) {
					return false;
				} else if (masu[yIndex + 1][xIndex] == MasuImpl.SPACE) {
					masu[yIndex + 1][xIndex] = MasuImpl.BLACK;
				}
			}
			if (xIndex != 0 && direction != Direction.LEFT) {
				if (masu[yIndex][xIndex - 1].isNotBlack()) {
					return false;
				} else if (masu[yIndex][xIndex - 1] == MasuImpl.SPACE) {
					masu[yIndex][xIndex - 1] = MasuImpl.BLACK;
				}
			}
			if (cnt == -1) {
				// 数が決まってない場合、向き先を白に。白にできない場合false。
				if (direction == Direction.UP) {
					if (yIndex == 0 || masu[yIndex - 1][xIndex] == MasuImpl.BLACK) {
						return false;
					} else if (masu[yIndex - 1][xIndex] == MasuImpl.SPACE) {
						masu[yIndex - 1][xIndex] = MasuImpl.NOT_BLACK;
					}
				} else if (direction == Direction.RIGHT) {
					if (xIndex == getXLength() - 1 || masu[yIndex][xIndex + 1] == MasuImpl.BLACK) {
						return false;
					} else if (masu[yIndex][xIndex + 1] == MasuImpl.SPACE) {
						masu[yIndex][xIndex + 1] = MasuImpl.NOT_BLACK;
					}
				} else if (direction == Direction.DOWN) {
					if (yIndex == getYLength() - 1 || masu[yIndex + 1][xIndex] == MasuImpl.BLACK) {
						return false;
					} else if (masu[yIndex + 1][xIndex] == MasuImpl.SPACE) {
						masu[yIndex + 1][xIndex] = MasuImpl.NOT_BLACK;
					}
				} else if (direction == Direction.LEFT) {
					if (xIndex == 0 || masu[yIndex][xIndex - 1] == MasuImpl.BLACK) {
						return false;
					} else if (masu[yIndex][xIndex - 1] == MasuImpl.SPACE) {
						masu[yIndex][xIndex - 1] = MasuImpl.NOT_BLACK;
					}
				}
			} else {
				// 数が決まっている場合その数だけ向き先を白に。白にできない場合false。
				// また、白マスの途中で別の岬にぶつかった場合もfalse。
				for (int i = 1; i < cnt; i++) {
					if (direction == Direction.UP) {
						if (yIndex + 1 - i == 0 || masu[yIndex - i][xIndex] == MasuImpl.BLACK
								|| (i != cnt - 1 && masu[yIndex - i][xIndex] instanceof Misaki)) {
							return false;
						} else if (masu[yIndex - i][xIndex] == MasuImpl.SPACE) {
							masu[yIndex - i][xIndex] = MasuImpl.NOT_BLACK;
						}
					} else if (direction == Direction.RIGHT) {
						if (xIndex - 1 + i == getXLength() - 1 || masu[yIndex][xIndex + i] == MasuImpl.BLACK
								|| (i != cnt - 1 && masu[yIndex][xIndex + 1] instanceof Misaki)) {
							return false;
						} else if (masu[yIndex][xIndex + i] == MasuImpl.SPACE) {
							masu[yIndex][xIndex + i] = MasuImpl.NOT_BLACK;
						}
					} else if (direction == Direction.DOWN) {
						if (yIndex - 1 + i == getYLength() - 1 || masu[yIndex + i][xIndex] == MasuImpl.BLACK
								|| (i != cnt - 1 && masu[yIndex + i][xIndex] instanceof Misaki)) {
							return false;
						} else if (masu[yIndex + i][xIndex] == MasuImpl.SPACE) {
							masu[yIndex + i][xIndex] = MasuImpl.NOT_BLACK;
						}
					} else if (direction == Direction.LEFT) {
						if (xIndex + 1 - i == 0 || masu[yIndex][xIndex - i] == MasuImpl.BLACK
								|| (i != cnt - 1 && masu[yIndex][xIndex - 1] instanceof Misaki)) {
							return false;
						} else if (masu[yIndex][xIndex - i] == MasuImpl.SPACE) {
							masu[yIndex][xIndex - i] = MasuImpl.NOT_BLACK;
						}
					}
				}
				// 伸びた先に■を配置。置けなかったらfalse。
				if (direction == Direction.UP) {
					if (yIndex + 1 - cnt == 0) {
						//
					} else if (masu[yIndex - cnt][xIndex].isNotBlack()) {
						return false;
					} else if (masu[yIndex - cnt][xIndex] == MasuImpl.SPACE) {
						masu[yIndex - cnt][xIndex] = MasuImpl.BLACK;
					}

				} else if (direction == Direction.RIGHT) {
					if (xIndex - 1 + cnt == getXLength() - 1) {
						//
					} else if (masu[yIndex][xIndex + cnt].isNotBlack()) {
						return false;
					} else if (masu[yIndex][xIndex + cnt] == MasuImpl.SPACE) {
						masu[yIndex][xIndex + cnt] = MasuImpl.BLACK;
					}
				} else if (direction == Direction.DOWN) {
					if (yIndex - 1 + cnt == getYLength() - 1) {
						//
					} else if (masu[yIndex + cnt][xIndex].isNotBlack()) {
						return false;
					} else if (masu[yIndex + cnt][xIndex] == MasuImpl.SPACE) {
						masu[yIndex + cnt][xIndex] = MasuImpl.BLACK;
					}
				} else if (direction == Direction.LEFT) {
					if (xIndex + 1 - cnt == 0) {
						//
					} else if (masu[yIndex][xIndex - cnt].isNotBlack()) {
						return false;
					} else if (masu[yIndex][xIndex - cnt] == MasuImpl.SPACE) {
						masu[yIndex][xIndex - cnt] = MasuImpl.BLACK;
					}
				}
			}
			return true;
		}

		/**
		 * 置くと池・逆池ができるマスを白・黒マスにする。
		 * 既に池・逆池ができている場合falseを返す。
		 */
		public boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == MasuImpl.BLACK && masu2 == MasuImpl.BLACK && masu3 == MasuImpl.BLACK
							&& masu4 == MasuImpl.BLACK) {
						return false;
					}
					if (masu1.isNotBlack() && masu2.isNotBlack() && masu3.isNotBlack() && masu4.isNotBlack()) {
						return false;
					}
					if (masu1 == MasuImpl.BLACK && masu2 == MasuImpl.BLACK && masu3 == MasuImpl.BLACK
							&& masu4 == MasuImpl.SPACE) {
						masu[yIndex + 1][xIndex + 1] = MasuImpl.NOT_BLACK;
					}
					if (masu1 == MasuImpl.BLACK && masu2 == MasuImpl.BLACK && masu3 == MasuImpl.SPACE
							&& masu4 == MasuImpl.BLACK) {
						masu[yIndex + 1][xIndex] = MasuImpl.NOT_BLACK;
					}
					if (masu1 == MasuImpl.BLACK && masu2 == MasuImpl.SPACE && masu3 == MasuImpl.BLACK
							&& masu4 == MasuImpl.BLACK) {
						masu[yIndex][xIndex + 1] = MasuImpl.NOT_BLACK;
					}
					if (masu1 == MasuImpl.SPACE && masu2 == MasuImpl.BLACK && masu3 == MasuImpl.BLACK
							&& masu4 == MasuImpl.BLACK) {
						masu[yIndex][xIndex] = MasuImpl.NOT_BLACK;
					}
					if (masu1.isNotBlack() && masu2.isNotBlack() && masu3.isNotBlack() && masu4 == MasuImpl.SPACE) {
						masu[yIndex + 1][xIndex + 1] = MasuImpl.BLACK;
					}
					if (masu1.isNotBlack() && masu2.isNotBlack() && masu3 == MasuImpl.SPACE && masu4.isNotBlack()) {
						masu[yIndex + 1][xIndex] = MasuImpl.BLACK;
					}
					if (masu1.isNotBlack() && masu2 == MasuImpl.SPACE && masu3.isNotBlack() && masu4.isNotBlack()) {
						masu[yIndex][xIndex + 1] = MasuImpl.BLACK;
					}
					if (masu1 == MasuImpl.SPACE && masu2.isNotBlack() && masu3.isNotBlack() && masu4.isNotBlack()) {
						masu[yIndex][xIndex] = MasuImpl.BLACK;
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
					if (masu[yIndex][xIndex] == MasuImpl.NOT_BLACK) {
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
				if (tree) {
					// ぬりみさ木モードのループ禁判定
					HashSet<Position> serveyWhitePos = new HashSet<>(whitePosSet);
					while (!serveyWhitePos.isEmpty()) {
						Set<Position> continuePosSet = new HashSet<>();
						Position onePos = (Position) serveyWhitePos.toArray()[0];
						continuePosSet.add(onePos);
						if (!loopCheck(onePos, continuePosSet, null)) {
							return false;
						}
						serveyWhitePos.removeAll(continuePosSet);
					}
				}
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(typicalWhitePos);
				setContinueWhitePosSet(typicalWhitePos, continuePosSet, null);
				whitePosSet.removeAll(continuePosSet);
				return whitePosSet.isEmpty();
			}
		}

		/**
		 * ぬりみさ木モード特有の処理
		 * 分岐の数がみさきの数-2を超えていたら失敗
		 */
		public boolean branchSolve() {
			if (tree) {
				int misakiCnt = 0;
				int branchCnt = 0;
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] instanceof Misaki) {
							misakiCnt++;
						} else if (masu[yIndex][xIndex] == MasuImpl.NOT_BLACK) {
							int notBlackCnt = 0;
							if (yIndex == 0 || masu[yIndex - 1][xIndex].isNotBlack()) {
								notBlackCnt++;
							}
							if (xIndex == getXLength() - 1 || masu[yIndex][xIndex + 1].isNotBlack()) {
								notBlackCnt++;
							}
							if (yIndex == getYLength() - 1 || masu[yIndex + 1][xIndex].isNotBlack()) {
								notBlackCnt++;
							}
							if (xIndex == 0 || masu[yIndex][xIndex - 1].isNotBlack()) {
								notBlackCnt++;
							}
							if (notBlackCnt > 2) {
								branchCnt = notBlackCnt - 2;
							}
						}
					}
				}
				if (branchCnt + 2 > misakiCnt) {
					return false;
				}
			}
			return true;
		}

		/**
		 * 白確定マスをつなぎ、ループができてる場合falseを返す。
		 */
		private boolean loopCheck(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (continuePosSet.contains(nextPos)) {
					return false;
				}
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == MasuImpl.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!loopCheck(nextPos, continuePosSet, Direction.DOWN)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				}
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == MasuImpl.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!loopCheck(nextPos, continuePosSet, Direction.LEFT)) {
						return false;
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (continuePosSet.contains(nextPos)) {
					return false;
				}
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == MasuImpl.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!loopCheck(nextPos, continuePosSet, Direction.UP)) {
						return false;
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (continuePosSet.contains(nextPos)) {
					return false;
				}
				if (masu[nextPos.getyIndex()][nextPos.getxIndex()] == MasuImpl.NOT_BLACK) {
					continuePosSet.add(nextPos);
					if (!loopCheck(nextPos, continuePosSet, Direction.RIGHT)) {
						return false;
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に黒確定でないマスをつなげていく。壁は無視する。
		 */
		private void setContinueWhitePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != MasuImpl.BLACK) {
					continuePosSet.add(nextPos);
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] instanceof Misaki) {
						// 岬のマスからはもう伸ばせない
					} else {
						setContinueWhitePosSet(nextPos, continuePosSet, Direction.DOWN);
					}
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != MasuImpl.BLACK) {
					continuePosSet.add(nextPos);
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] instanceof Misaki) {
						// 岬のマスからはもう伸ばせない
					} else {
						setContinueWhitePosSet(nextPos, continuePosSet, Direction.LEFT);
					}
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != MasuImpl.BLACK) {
					continuePosSet.add(nextPos);
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] instanceof Misaki) {
						// 岬のマスからはもう伸ばせない
					} else {
						setContinueWhitePosSet(nextPos, continuePosSet, Direction.UP);
					}
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != MasuImpl.BLACK) {
					continuePosSet.add(nextPos);
					if (masu[nextPos.getyIndex()][nextPos.getxIndex()] instanceof Misaki) {
						// 岬のマスからはもう伸ばせない
					} else {
						setContinueWhitePosSet(nextPos, continuePosSet, Direction.RIGHT);
					}
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!misakiSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
			if (!branchSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == MasuImpl.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;

	public NurimisakiSolver(int height, int width, String param, boolean tree) {
		field = new Field(height, width, param, tree);
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
		System.out.println(new NurimisakiSolver(height, width, param, false).solve());
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
				if (!candSolve(field, recursiveCnt == 2 ? 999 : recursiveCnt)) {
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				recursiveCnt++;
			}
			if (recursiveCnt == 3 && field.getStateDump().equals(befStr)) {
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) +
				"ms.");
		System.out.println("難易度:" + difficulty);
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
				// 周囲に空白が少ない個所を優先して調査
				Masu masuUp = yIndex == 0 ? MasuImpl.BLACK
						: field.masu[yIndex - 1][xIndex];
				Masu masuRight = xIndex == field.getXLength() - 1 ? MasuImpl.BLACK
						: field.masu[yIndex][xIndex + 1];
				Masu masuDown = yIndex == field.getYLength() - 1 ? MasuImpl.BLACK
						: field.masu[yIndex + 1][xIndex];
				Masu masuLeft = xIndex == 0 ? MasuImpl.BLACK
						: field.masu[yIndex][xIndex - 1];
				int whiteCnt = 0;
				if (masuUp == MasuImpl.SPACE) {
					whiteCnt++;
				}
				if (masuRight == MasuImpl.SPACE) {
					whiteCnt++;
				}
				if (masuDown == MasuImpl.SPACE) {
					whiteCnt++;
				}
				if (masuLeft == MasuImpl.SPACE) {
					whiteCnt++;
				}
				if (whiteCnt > 3) {
					continue;
				}
				if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
					return false;
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
		if (field.masu[yIndex][xIndex] == MasuImpl.SPACE) {
			Field virtual = new Field(field);
			virtual.masu[yIndex][xIndex] = MasuImpl.BLACK;
			boolean allowBlack = virtual.solveAndCheck();
			if (allowBlack && recursive > 0) {
				if (!candSolve(virtual, recursive - 1)) {
					allowBlack = false;
				}
			}
			Field virtual2 = new Field(field);
			virtual2.masu[yIndex][xIndex] = MasuImpl.NOT_BLACK;
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
		}
		return true;
	}
}
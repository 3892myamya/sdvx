package myamya.other.solver.battleship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.FixedShape;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class BattleshipSolver implements Solver {

	// 船の置き方に相当する
	public static class ShipPosObj {
		@Override
		public String toString() {
			return posPropMap.toString();
		}

		private final FixedShape shapeBase;
		// 以下はその都度算出すると遅くなるのでコンストラクタ時点で覚えさせる
		private final TreeMap<Position, Integer> posPropMap;
		private final Set<Position> nextPosSet;

		ShipPosObj(FixedShape shapeBase, Map<Position, Integer> posPropMap, Set<Position> nextPosSet) {
			this.shapeBase = shapeBase;
			this.posPropMap = new TreeMap<>(new Comparator<Position>() {
				@Override
				public int compare(Position o1, Position o2) {
					return (o1.getyIndex() * 1000 + o1.getxIndex()) - (o2.getyIndex() * 1000 + o2.getxIndex());
				}

			});
			this.posPropMap.putAll(posPropMap);
			this.nextPosSet = nextPosSet;
		}

		// 自分が占めるマスと属性値のMapを返す
		public TreeMap<Position, Integer> getPosPropMap() {
			return posPropMap;
		}

		// 自分の縦横斜めに隣接するマス一覧を返す。
		public Set<Position> getNextPosSet() {
			return nextPosSet;
		}

		// 他の船の置き方が、自分と同じ形の船かどうかを返す。
		public boolean isSame(ShipPosObj other) {
			return shapeBase.isSame(other.shapeBase);
		}
	}

	public static class Field {
		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// マス属性の情報
		// null：属性なし
		// -1：確定黒マス(形状不定)
		// 0：≋
		// 1：▲
		// 2：▼
		// 3：◀
		// 4：▶
		// 5：■
		// 6：●
		// 7：◢
		// 8：◣
		// 9：◥
		// 10：◤
		protected final Integer[][] props;
		// ヒント情報
		protected Integer[] upHints;
		protected Integer[] leftHints;
		// 船の配置の候補
		protected Map<Integer, List<ShipPosObj>> shipCand;

		public Masu[][] getMasu() {
			return masu;
		}

		public Integer[] getUpHints() {
			return upHints;
		}

		public Integer[] getLeftHints() {
			return leftHints;
		}

		public Integer[][] getProps() {
			return props;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		/**
		 * 部屋のきりかたの候補を初期化する。
		 */
		protected void initCand(List<String> paramList) {
			shipCand = makeShipCandBase(paramList);
		}

		private Map<Integer, List<ShipPosObj>> makeShipCandBase(List<String> paramList) {
			int idx = 0;
			Map<Integer, List<ShipPosObj>> shipCandBase = new TreeMap<>();
			for (String oneParam : paramList) {
				Set<Position> basePosSet = new HashSet<>();
				int readPos = 0;
				int bit = 0;
				int shipYLength = Integer.parseInt(oneParam.substring(1, 2));
				int shipXLength = Integer.parseInt(oneParam.substring(0, 1));
				for (int cnt = 0; cnt < shipYLength * shipXLength; cnt++) {
					int mod = cnt % 5;
					if (mod == 0) {
						if (oneParam.substring(2).length() <= readPos) {
							break;
						}
						bit = Character.getNumericValue(oneParam.substring(2).charAt(readPos));
						readPos++;
					}
					if (mod == 4 || cnt == (shipYLength * (shipXLength)) - 1) {
						if (mod >= 0 && bit / 16 % 2 == 1) {
							basePosSet.add(
									new Position((cnt - mod + 0) / (shipXLength), (cnt - mod + 0) % (shipXLength)));
						}
						if (mod >= 1 && bit / 8 % 2 == 1) {
							basePosSet.add(
									new Position((cnt - mod + 1) / (shipXLength), (cnt - mod + 1) % (shipXLength)));
						}
						if (mod >= 2 && bit / 4 % 2 == 1) {
							basePosSet.add(
									new Position((cnt - mod + 2) / (shipXLength), (cnt - mod + 2) % (shipXLength)));
						}
						if (mod >= 3 && bit / 2 % 2 == 1) {
							basePosSet.add(
									new Position((cnt - mod + 3) / (shipXLength), (cnt - mod + 3) % (shipXLength)));
						}
						if (mod >= 4 && bit / 1 % 2 == 1) {
							basePosSet.add(
									new Position((cnt - mod + 4) / (shipXLength), (cnt - mod + 4) % (shipXLength)));
						}
					}
				}
				FixedShape ship = new FixedShape(basePosSet);
				List<ShipPosObj> shipPosObjList = new ArrayList<>();
				for (Set<Position> posSetBase : ship.getSamePosSetList()) {
					int useShipYLength = 0;
					int useShipXLength = 0;
					for (Position posBase : posSetBase) {
						if (useShipYLength < posBase.getyIndex()) {
							useShipYLength = posBase.getyIndex();
						}
						if (useShipXLength < posBase.getxIndex()) {
							useShipXLength = posBase.getxIndex();
						}
					}
					for (int yIndex = 0; yIndex < getYLength() - useShipYLength; yIndex++) {
						for (int xIndex = 0; xIndex < getXLength() - useShipXLength; xIndex++) {
							Map<Position, Integer> posPropMap = new HashMap<>();
							for (Position posBase : posSetBase) {
								boolean existUp = posSetBase
										.contains(new Position(posBase.getyIndex() - 1, posBase.getxIndex()));
								boolean existDown = posSetBase
										.contains(new Position(posBase.getyIndex() + 1, posBase.getxIndex()));
								boolean existLeft = posSetBase
										.contains(new Position(posBase.getyIndex(), posBase.getxIndex() - 1));
								boolean existRight = posSetBase
										.contains(new Position(posBase.getyIndex(), posBase.getxIndex() + 1));
								int prop = 0;
								if (existUp && existDown && existLeft && existRight) {
									prop = 5;
								} else if (existUp && existDown && existLeft && !existRight) {
									prop = 5;
								} else if (existUp && existDown && !existLeft && existRight) {
									prop = 5;
								} else if (existUp && !existDown && existLeft && existRight) {
									prop = 5;
								} else if (!existUp && existDown && existLeft && existRight) {
									prop = 5;
								} else if (existUp && existDown && !existLeft && !existRight) {
									prop = 5;
								} else if (existUp && !existDown && existLeft && !existRight) {
									prop = 10;
								} else if (!existUp && existDown && existLeft && !existRight) {
									prop = 8;
								} else if (existUp && !existDown && !existLeft && existRight) {
									prop = 9;
								} else if (!existUp && existDown && !existLeft && existRight) {
									prop = 7;
								} else if (!existUp && !existDown && existLeft && existRight) {
									prop = 5;
								} else if (existUp && !existDown && !existLeft && !existRight) {
									prop = 2;
								} else if (!existUp && existDown && !existLeft && !existRight) {
									prop = 1;
								} else if (!existUp && !existDown && existLeft && !existRight) {
									prop = 4;
								} else if (!existUp && !existDown && !existLeft && existRight) {
									prop = 3;
								} else if (!existUp && !existDown && !existLeft && !existRight) {
									prop = 6;
								}
								posPropMap.put(new Position(posBase.getyIndex() + yIndex, posBase.getxIndex() + xIndex),
										prop);
							}
							Set<Position> nextPosSet = new HashSet<Position>();
							for (Position pos : posPropMap.keySet()) {
								if (pos.getyIndex() != 0 && !posPropMap
										.containsKey(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
									nextPosSet.add(new Position(pos.getyIndex() - 1, pos.getxIndex()));
								}
								if (pos.getyIndex() != 0 && pos.getxIndex() != getXLength() - 1 && !posPropMap
										.containsKey(new Position(pos.getyIndex() - 1, pos.getxIndex() + 1))) {
									nextPosSet.add(new Position(pos.getyIndex() - 1, pos.getxIndex() + 1));
								}
								if (pos.getxIndex() != getXLength() - 1 && !posPropMap
										.containsKey(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
									nextPosSet.add(new Position(pos.getyIndex(), pos.getxIndex() + 1));
								}
								if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != getXLength() - 1
										&& !posPropMap
												.containsKey(new Position(pos.getyIndex() + 1, pos.getxIndex() + 1))) {
									nextPosSet.add(new Position(pos.getyIndex() + 1, pos.getxIndex() + 1));
								}
								if (pos.getyIndex() != getYLength() - 1 && !posPropMap
										.containsKey(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
									nextPosSet.add(new Position(pos.getyIndex() + 1, pos.getxIndex()));
								}
								if (pos.getyIndex() != getYLength() - 1 && pos.getxIndex() != 0 && !posPropMap
										.containsKey(new Position(pos.getyIndex() + 1, pos.getxIndex() - 1))) {
									nextPosSet.add(new Position(pos.getyIndex() + 1, pos.getxIndex() - 1));
								}
								if (pos.getxIndex() != 0 && !posPropMap
										.containsKey(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
									nextPosSet.add(new Position(pos.getyIndex(), pos.getxIndex() - 1));
								}
								if (pos.getyIndex() != 0 && pos.getxIndex() != 0 && !posPropMap
										.containsKey(new Position(pos.getyIndex() - 1, pos.getxIndex() - 1))) {
									nextPosSet.add(new Position(pos.getyIndex() - 1, pos.getxIndex() - 1));
								}
							}
							shipPosObjList.add(new ShipPosObj(ship, posPropMap, nextPosSet));
						}
					}
				}
				shipCandBase.put(idx, shipPosObjList);
				idx++;
			}
			return shipCandBase;
		}

		public Field(String url) {
			String paramBase = url.split("battleship/")[1];
			int height = Integer.parseInt(paramBase.split("/")[1]);
			int width = Integer.parseInt(paramBase.split("/")[0]);
			String param = paramBase.split("/")[2];
			masu = new Masu[height][width];
			props = new Integer[height][width];
			upHints = new Integer[width];
			leftHints = new Integer[height];
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
					int capacity;
					if (index < width + height) {
						// 16 - 255は '-'
						// 256 - 999は '+'
						if (ch == '.') {
							//
						} else {
							if (ch == '-') {
								capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
								i++;
								i++;
							} else if (ch == '+') {
								capacity = Integer.parseInt(
										"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3), 16);
								i++;
								i++;
								i++;
							} else {
								capacity = Integer.parseInt(String.valueOf(ch), 16);
							}
							if (index < width) {
								upHints[index] = capacity;
							} else {
								leftHints[index - width] = capacity;
							}
						}
					} else {
						// 16 - 255は '-'
						// 256 - 999は '+'
						int useIndex = index - width - height;
						if (ch == '.') {
							Position pos = new Position(useIndex / getXLength(), useIndex % getXLength());
							masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
							props[pos.getyIndex()][pos.getxIndex()] = -1;
						} else {
							if (ch == '-') {
								capacity = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
								i++;
								i++;
							} else if (ch == '+') {
								capacity = Integer.parseInt(
										"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3), 16);
								i++;
								i++;
								i++;
							} else {
								capacity = Integer.parseInt(String.valueOf(ch), 16);
							}
							Position pos = new Position(useIndex / getXLength(), useIndex % getXLength());
							if (capacity == 0) {
								masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
							} else {
								masu[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
							}
							props[pos.getyIndex()][pos.getxIndex()] = capacity;
						}
					}
					index++;
				}
			}
			String wkParam = paramBase.split("/", 4)[3].split("/", 2)[1];
			// プリセット文字の変換
			if (wkParam.equals("p")) {
				wkParam = "337k/15v/24as/24bo/23fg/337i/23rg/334u/335s/33bk/24bk/337p";
			} else if (wkParam.equals("e")) {
				wkParam = "11g/11g/11g/11g/11g/21o/21o/21o/21o/31s/31s/31s/41u/41u/51v";
			} else if (wkParam.equals("d")) {
				wkParam = "11g/11g/11g/11g/21o/21o/21o/31s/31s/41u";
			} else if (wkParam.equals("c")) {
				wkParam = "11g/11g/11g/21o/21o/31s";
			}
			initCand(new ArrayList<String>(Arrays.asList(wkParam.split("/"))));
		}

		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			props = new Integer[other.getYLength()][other.getXLength()];
			upHints = new Integer[other.getXLength()];
			leftHints = new Integer[other.getYLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
					props[yIndex][xIndex] = other.props[yIndex][xIndex];
				}

			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				leftHints[yIndex] = other.leftHints[yIndex];
			}
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				upHints[xIndex] = other.upHints[xIndex];
			}
			shipCand = new TreeMap<>();
			for (Entry<Integer, List<ShipPosObj>> entry : other.shipCand.entrySet()) {
				shipCand.put(entry.getKey(), new ArrayList<>(entry.getValue()));
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					String numStr = String.valueOf(upHints[xIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == -1) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
			}
			sb.append("　");
			sb.append(System.lineSeparator());
			sb.append("　　");
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				sb.append("↓");
			}
			sb.append("　");
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					String numStr = String.valueOf(leftHints[yIndex]);
					int index = HALF_NUMS.indexOf(numStr);
					if (index == -1) {
						sb.append(numStr);
					} else {
						sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
					}
				} else {
					sb.append("　");
				}
				sb.append("→");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					sb.append(masu[yIndex][xIndex]);
				}
				sb.append(System.lineSeparator());
			}
			return sb.toString();
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (List<ShipPosObj> shipPosObjList : shipCand.values()) {
				sb.append(shipPosObjList.size());
				sb.append(":");
			}
			return sb.toString();
		}

		/**
		 * 白マスと被る候補船は削除＆黒マスと隣接マスが被る候補船は削除。 <br>
		 * 確定船のマスは黒マス確定＆隣接マスは白マス確定。<br>
		 * 候補がないマスは白マス確定。<br>
		 */
		private boolean sikakuSolve() {
			Set<Position> canBlackPosSet = new HashSet<Position>();
			for (Entry<Integer, List<ShipPosObj>> entry : shipCand.entrySet()) {
				// 船が置けなくなっていたらだめ
				List<ShipPosObj> shipPosObjList = entry.getValue();
				if (shipPosObjList.isEmpty()) {
					return false;
				} else if (shipPosObjList.size() == 1) {
					// 船が1つなら確定
					for (Position blackPosSet : shipPosObjList.get(0).posPropMap.keySet()) {
						if (masu[blackPosSet.getyIndex()][blackPosSet.getxIndex()] == Masu.NOT_BLACK) {
							return false;
						}
						masu[blackPosSet.getyIndex()][blackPosSet.getxIndex()] = Masu.BLACK;
					}
					for (Position notBlackPosSet : shipPosObjList.get(0).nextPosSet) {
						if (masu[notBlackPosSet.getyIndex()][notBlackPosSet.getxIndex()] == Masu.BLACK) {
							return false;
						}
						masu[notBlackPosSet.getyIndex()][notBlackPosSet.getxIndex()] = Masu.NOT_BLACK;
					}
				}
				// 船が置ける場所のみ黒にできる
				for (ShipPosObj shipPosObj : shipPosObjList) {
					canBlackPosSet.addAll(shipPosObj.getPosPropMap().keySet());
				}
			}
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (!canBlackPosSet.contains(new Position(yIndex, xIndex))) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							return false;
						}
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
						for (Entry<Integer, List<ShipPosObj>> entry : shipCand.entrySet()) {
							for (Iterator<ShipPosObj> iterator = entry.getValue().iterator(); iterator.hasNext();) {
								ShipPosObj shipPosObj = iterator.next();
								if (shipPosObj.getPosPropMap().keySet().contains(new Position(yIndex, xIndex))) {
									iterator.remove();
								}
							}
						}
					} else if (masu[yIndex][xIndex] == Masu.BLACK) {
						int onlyShipKey = -1;
						ShipPosObj onlyShip = null;
						for (Entry<Integer, List<ShipPosObj>> entry : shipCand.entrySet()) {
							for (Iterator<ShipPosObj> iterator = entry.getValue().iterator(); iterator.hasNext();) {
								ShipPosObj shipPosObj = iterator.next();
								if (shipPosObj.getNextPosSet().contains(new Position(yIndex, xIndex))) {
									iterator.remove();
								} else if (shipPosObj.getPosPropMap().keySet().contains(new Position(yIndex, xIndex))) {
									if (onlyShip == null) {
										onlyShipKey = entry.getKey();
										onlyShip = shipPosObj;
									} else {
										onlyShipKey = -1;
									}
								}
							}
						}
						// 黒マスは必ず船になるので、黒マスに対して置ける船が1つだけならそれは確定
						if (onlyShipKey != -1) {
							shipCand.get(onlyShipKey).clear();
							shipCand.get(onlyShipKey).add(onlyShip);
						}
					}
					if (props[yIndex][xIndex] != null && props[yIndex][xIndex] != -1) {
						for (List<ShipPosObj> shipPosObjList : shipCand.values()) {
							for (Iterator<ShipPosObj> iterator = shipPosObjList.iterator(); iterator.hasNext();) {
								ShipPosObj shipPosObj = iterator.next();
								Integer targetProp = shipPosObj.getPosPropMap().get(new Position(yIndex, xIndex));
								if (targetProp != null && targetProp != props[yIndex][xIndex]) {
									iterator.remove();
								}
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * hintsは黒マスの数を示す。
		 */
		private boolean hintsSolve() {
			// 縦のヒント
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				if (leftHints[yIndex] != null) {
					int blackCnt = 0;
					int spaceCnt = 0;
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							blackCnt++;
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							spaceCnt++;
						}
					}
					if (leftHints[yIndex] < blackCnt) {
						return false;
					}
					if (leftHints[yIndex] > blackCnt + spaceCnt) {
						return false;
					}
					if (leftHints[yIndex] == blackCnt) {
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
					if (leftHints[yIndex] == blackCnt + spaceCnt) {
						for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
			// 横のヒント
			for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
				if (upHints[xIndex] != null) {
					int blackCnt = 0;
					int spaceCnt = 0;
					for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
						if (masu[yIndex][xIndex] == Masu.BLACK) {
							blackCnt++;
						} else if (masu[yIndex][xIndex] == Masu.SPACE) {
							spaceCnt++;
						}
					}
					if (upHints[xIndex] < blackCnt) {
						return false;
					}
					if (upHints[xIndex] > blackCnt + spaceCnt) {
						return false;
					}
					if (upHints[xIndex] == blackCnt) {
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
					}
					if (upHints[xIndex] == blackCnt + spaceCnt) {
						for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
							if (masu[yIndex][xIndex] == Masu.SPACE) {
								masu[yIndex][xIndex] = Masu.BLACK;
							}
						}
					}
				}
			}
			return true;
		}

		// 同じ形の船を置くときには、左上のマスが含まれるものから順番に置いていく。
		private boolean shipSolve() {
			for (Entry<Integer, List<ShipPosObj>> entry : shipCand.entrySet()) {
				if (entry.getValue().size() == 1) {
					for (Entry<Integer, List<ShipPosObj>> otherEntry : shipCand.entrySet()) {
						if (otherEntry.getValue().isEmpty()) {
							return false;
						}
						if (entry.getKey() != otherEntry.getKey()
								&& entry.getValue().get(0).isSame(otherEntry.getValue().get(0))) {
							Position pos = entry.getValue().get(0).getPosPropMap().firstKey();
							int posValue = pos.getyIndex() * 10000 + pos.getxIndex();
							for (Iterator<ShipPosObj> iterator = otherEntry.getValue().iterator(); iterator
									.hasNext();) {
								ShipPosObj shipPosObj = iterator.next();
								Position otherPos = shipPosObj.getPosPropMap().firstKey();
								int otherPosValue = otherPos.getyIndex() * 10000 + otherPos.getxIndex();
								if (entry.getKey() > otherEntry.getKey() && posValue <= otherPosValue) {
									iterator.remove();
								} else if (entry.getKey() < otherEntry.getKey() && posValue >= otherPosValue) {
									iterator.remove();
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
		 * 
		 * @param i
		 * @param recursive
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!sikakuSolve()) {
				return false;
			}
			if (!hintsSolve()) {
				return false;
			}
			if (!shipSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		public boolean isSolved() {
			for (List<ShipPosObj> shipPosObjList : shipCand.values()) {
				if (shipPosObjList.size() != 1) {
					return false;
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public BattleshipSolver(String param) {
		long start = System.nanoTime();
		field = new Field(param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public BattleshipSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		// String url =
		// "https://puzz.link/p?battleship/12/12/ag75g2g8h6j3g4g65hap0q0u0j0v0o0k0t00h0j0y0h0r0i0i/12/337k/15v/24as/24bo/23fg/337i/23rg/334u/335s/33bk/24bk/337p";
		// String url =
		// "https://puzz.link/p?battleship/6/6/g23h1g1g23gl0l0l0l0l0g/6/11g/11g/11g/21o/21o/31s";
		// // urlを入れれば試せる
		String url = "https://puzz.link/p?battleship/8/8/2g22m2g22go0g0g0j0i1h0g0g0l4j0g0g0j3s//d";
		System.out.println(new BattleshipSolver(url).solve());
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
		int level = (int) Math.sqrt(count / 2 / 3) + 1;
		return "解けました。推定難易度:" + Difficulty.getByCount(count / 2).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				count++;
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
					field.shipCand = virtual2.shipCand;
				} else if (!allowNotBlack) {
					field.masu = virtual.masu;
					field.shipCand = virtual.shipCand;
				}
			}
		}
		for (Entry<Integer, List<ShipPosObj>> entry : field.shipCand.entrySet()) {
			for (Iterator<ShipPosObj> iterator = entry.getValue().iterator(); iterator.hasNext();) {
				count++;
				ShipPosObj oneCand = iterator.next();
				Field virtual = new Field(field);
				virtual.shipCand.get(entry.getKey()).clear();
				virtual.shipCand.get(entry.getKey()).add(oneCand);
				boolean allowBlack = virtual.solveAndCheck();
				if (allowBlack && recursive > 0) {
					if (!candSolve(virtual, recursive - 1)) {
						allowBlack = false;
					}
				}
				Field virtual2 = new Field(field);
				virtual2.shipCand.get(entry.getKey()).remove(oneCand);
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
					field.shipCand = virtual2.shipCand;
				} else if (!allowNotBlack) {
					field.masu = virtual.masu;
					field.shipCand = virtual.shipCand;
				}
			}
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
		return true;
	}

}
package myamya.other.solver.kissing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.FixedShape;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class KissingSolver implements Solver {

	public static class ShipCand {

		private final int no;
		private final List<ShipPosObj> shipPosObjList;

		public String toString() {
			return shipPosObjList.toString();
		}

		public ShipCand(int no, List<ShipPosObj> shipPosObjList) {
			this.no = no;
			this.shipPosObjList = shipPosObjList;
		}

		public ShipCand(ShipCand other) {
			this.no = other.no;
			this.shipPosObjList = new ArrayList<>(other.shipPosObjList);
		}

		public int getNo() {
			return no;
		}

		// 候補一覧を返す
		public List<ShipPosObj> getShipPosObjList() {
			return shipPosObjList;
		}

		// 自分が確定しているかを返す。
		public boolean isFixed() {
			return shipPosObjList.size() == 1;
		}

		// 自分が占有可能なマス一覧を返す
		public Set<Position> getCandPosSet() {
			Set<Position> result = new HashSet<Position>();
			for (ShipPosObj shipPosObj : shipPosObjList) {
				result.addAll(shipPosObj.getPosSet());
			}
			return result;
		}

	}

	// 船の置き方に相当する
	public static class ShipPosObj {
		@Override
		public String toString() {
			return posSet.toString();
		}

		private final FixedShape shapeBase;
		// 以下はその都度算出すると遅くなるのでコンストラクタ時点で覚えさせる
		private final TreeSet<Position> posSet;

		ShipPosObj(FixedShape shapeBase, Set<Position> posSet) {
			this.shapeBase = shapeBase;
			this.posSet = new TreeSet<>(new Comparator<Position>() {
				@Override
				public int compare(Position o1, Position o2) {
					return (o1.getyIndex() * 1000 + o1.getxIndex()) - (o2.getyIndex() * 1000 + o2.getxIndex());
				}

			});
			this.posSet.addAll(posSet);
		}

		// 自分が占めるマスを返す
		public TreeSet<Position> getPosSet() {
			return posSet;
		}

		// 他の船の置き方が、自分と同じ形の船かどうかを返す。
		public boolean isSame(ShipPosObj other) {
			return shapeBase.isSame(other.shapeBase);
		}
	}

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 壁情報
		boolean[][] yokoWall;
		boolean[][] tateWall;
		// 配置不可能マス
		boolean[][] banned;
		// 船の配置の候補
		protected List<ShipCand> shipCandList;

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public boolean[][] getBanned() {
			return banned;
		}

		public int getYLength() {
			return banned.length;
		}

		public int getXLength() {
			return banned[0].length;
		}

		/**
		 * 部屋のきりかたの候補を初期化する。 wall、bannedも考慮する。
		 */
		protected void initCand(List<String> paramList) {
			shipCandList = makeShipCandBase(paramList);
		}

		private List<ShipCand> makeShipCandBase(List<String> paramList) {
			int idx = 0;
			List<ShipCand> shipCandBase = new ArrayList<>();
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
				// 壁伝いに連続しているマスは必ず1つの船になる。まずは全ての壁隣接マスをセット
				Set<Position> blackPosSet = new HashSet<>();
				for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						Position pos = new Position(yIndex, xIndex);
						if (pos.getyIndex() != 0 && tateWall[pos.getyIndex() - 1][pos.getxIndex()]) {
							blackPosSet.add(pos);
						} else if (pos.getxIndex() != getXLength() - 1 && yokoWall[pos.getyIndex()][pos.getxIndex()]) {
							blackPosSet.add(pos);
						} else if (pos.getyIndex() != getYLength() - 1 && tateWall[pos.getyIndex()][pos.getxIndex()]) {
							blackPosSet.add(pos);
						} else if (pos.getxIndex() != 0 && yokoWall[pos.getyIndex()][pos.getxIndex() - 1]) {
							blackPosSet.add(pos);
						}
					}
				}
				// 壁伝いに連続しているマスをリスト化
				Set<Position> fencesPosSet = new HashSet<>(blackPosSet);
				List<Set<Position>> continuePosSetList = new ArrayList<>();
				while (!fencesPosSet.isEmpty()) {
					Position targetPos = new ArrayList<>(fencesPosSet).get(0);
					Set<Position> whitePosSet = new HashSet<>();
					whitePosSet.add(targetPos);
					setContinuePosSet(fencesPosSet, targetPos, whitePosSet, null);
					continuePosSetList.add(whitePosSet);
					fencesPosSet.removeAll(whitePosSet);
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
							Set<Position> posSet = new HashSet<>();
							for (Position posBase : posSetBase) {
								posSet.add(new Position(posBase.getyIndex() + yIndex, posBase.getxIndex() + xIndex));
							}
							// 禁止マスを含んでいたり壁をまたぐ場合は追加しない
							boolean isCand = true;
							for (Position pos : posSet) {
								if (banned[pos.getyIndex()][pos.getxIndex()]) {
									isCand = false;
									break;
								}
								if (pos.getxIndex() != getXLength() - 1 && yokoWall[pos.getyIndex()][pos.getxIndex()]) {
									if (posSet.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
										isCand = false;
										break;

									}
								}
								if (pos.getyIndex() != getYLength() - 1 && tateWall[pos.getyIndex()][pos.getxIndex()]) {
									if (posSet.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
										isCand = false;
										break;
									}
								}
							}
							if (isCand) {
								// posSetが黒マス確定マスに壁を隔てずに「隣接」してはいけない。※壁を隔てたり、含むだけならOK
								Set<Position> nextPosSet = new HashSet<Position>();
								for (Position pos : posSet) {
									if (pos.getyIndex() != 0 && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]
											&& !posSet.contains(new Position(pos.getyIndex() - 1, pos.getxIndex()))) {
										nextPosSet.add(new Position(pos.getyIndex() - 1, pos.getxIndex()));
									}
									if (pos.getxIndex() != getXLength() - 1
											&& !yokoWall[pos.getyIndex()][pos.getxIndex()]
											&& !posSet.contains(new Position(pos.getyIndex(), pos.getxIndex() + 1))) {
										nextPosSet.add(new Position(pos.getyIndex(), pos.getxIndex() + 1));
									}
									if (pos.getyIndex() != getYLength() - 1
											&& !tateWall[pos.getyIndex()][pos.getxIndex()]
											&& !posSet.contains(new Position(pos.getyIndex() + 1, pos.getxIndex()))) {
										nextPosSet.add(new Position(pos.getyIndex() + 1, pos.getxIndex()));
									}
									if (pos.getxIndex() != 0 && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]
											&& !posSet.contains(new Position(pos.getyIndex(), pos.getxIndex() - 1))) {
										nextPosSet.add(new Position(pos.getyIndex(), pos.getxIndex() - 1));
									}
								}
								Set<Position> checkNextPosSet = new HashSet<>(blackPosSet);
								checkNextPosSet.removeAll(nextPosSet);
								if (checkNextPosSet.size() == blackPosSet.size()) {
									// 壁伝い連続マスについて、まったく含まない、または全部含むのみを許容
									for (Set<Position> continuePosSet : continuePosSetList) {
										Set<Position> checkPosSet = new HashSet<>(continuePosSet);
										checkPosSet.removeAll(posSet);
										if (checkPosSet.size() != 0 && checkPosSet.size() != continuePosSet.size()) {
											isCand = false;
											break;
										}
									}
									if (isCand) {
										shipPosObjList.add(new ShipPosObj(ship, posSet));
									}
								}
							}
						}
					}
				}
				shipCandBase.add(new ShipCand(idx, shipPosObjList));
				idx++;
			}
			return shipCandBase;
		}

		/**
		 * posを起点に上下左右に壁で区切られていないマスでfencesPosSetに含まれるマスをつなげていく。
		 */
		private void setContinuePosSet(Set<Position> fencesPosSet, Position pos, Set<Position> continuePosSet,
				Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!tateWall[pos.getyIndex() - 1][pos.getxIndex()] && fencesPosSet.contains(nextPos)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(fencesPosSet, nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!yokoWall[pos.getyIndex()][pos.getxIndex()] && fencesPosSet.contains(nextPos)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(fencesPosSet, nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!tateWall[pos.getyIndex()][pos.getxIndex()] && fencesPosSet.contains(nextPos)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(fencesPosSet, nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!yokoWall[pos.getyIndex()][pos.getxIndex() - 1] && fencesPosSet.contains(nextPos)
						&& !continuePosSet.contains(nextPos)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(fencesPosSet, nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		public Field(String url) {
			String paramBase = url.split("kissing/")[1];
			int height = Integer.parseInt(paramBase.split("/")[1]);
			int width = Integer.parseInt(paramBase.split("/")[0]);
			String param = paramBase.split("/")[2];
			yokoWall = new boolean[height][width - 1];
			tateWall = new boolean[height - 1][width];
			banned = new boolean[height][width];
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
			if (readPos != param.length()) {
				for (int cnt = 0; cnt < getYLength() * getXLength(); cnt++) {
					int mod = cnt % 5;
					if (mod == 0) {
						bit = Character.getNumericValue(param.charAt(readPos));
						readPos++;
					}
					if (mod == 4 || cnt == (getYLength() * (getXLength())) - 1) {
						if (mod >= 0) {
							banned[(cnt - mod + 0) / (getXLength())][(cnt - mod + 0) % (getXLength())] = bit / 16
									% 2 == 1;
						}
						if (mod >= 1) {
							banned[(cnt - mod + 1) / (getXLength())][(cnt - mod + 1) % (getXLength())] = bit / 8
									% 2 == 1;
						}
						if (mod >= 2) {
							banned[(cnt - mod + 2) / (getXLength())][(cnt - mod + 2) % (getXLength())] = bit / 4
									% 2 == 1;
						}
						if (mod >= 3) {
							banned[(cnt - mod + 3) / (getXLength())][(cnt - mod + 3) % (getXLength())] = bit / 2
									% 2 == 1;
						}
						if (mod >= 4) {
							banned[(cnt - mod + 4) / (getXLength())][(cnt - mod + 4) % (getXLength())] = bit / 1
									% 2 == 1;
						}
					}
				}
			}
			String wkParam = paramBase.split("/", 4)[3].split("/", 2)[1];
			// プリセット文字の変換
			if (wkParam.equals("p")) {
				wkParam = "337k/15v/24as/24bo/23fg/337i/23rg/334u/335s/33bk/24bk/337p";
			} else if (wkParam.equals("t")) {
				wkParam = "14u/23bg/22u/23f/23eg";
			} else if (wkParam.equals("d")) {
				wkParam = "14u/14u/23bg/23bg/22u/22u/23f/23f/23eg/23eg";
			}
			initCand(new ArrayList<String>(Arrays.asList(wkParam.split("/"))));
		}

		public Field(Field other) {
			banned = other.banned;
			yokoWall = new boolean[other.getYLength()][other.getXLength() - 1];
			tateWall = new boolean[other.getYLength() - 1][other.getXLength()];
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
			shipCandList = new ArrayList<>();
			for (ShipCand shipCand : other.shipCandList) {
				shipCandList.add(new ShipCand(shipCand));
			}
		}

		@Override
		public String toString() {
			Masu[][] masu = getMasu();
			StringBuilder sb = new StringBuilder();
			for (int xIndex = 0; xIndex < getXLength() * 2 + 1; xIndex++) {
				sb.append("□");
			}
			sb.append(System.lineSeparator());
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				sb.append("□");
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (banned[yIndex][xIndex]) {
						sb.append("×");
					} else {
						sb.append(masu[yIndex][xIndex]);
					}
					if (xIndex != getXLength() - 1) {
						sb.append(yokoWall[yIndex][xIndex] ? "□" : "　");
					}
				}
				sb.append("□");
				sb.append(System.lineSeparator());
				if (yIndex != getYLength() - 1) {
					sb.append("□");
					for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
						sb.append(tateWall[yIndex][xIndex] ? "□" : "　");
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

		/**
		 * 候補情報からマスを復元する
		 */
		public Masu[][] getMasu() {
			Set<Position> fixedPosSet = new HashSet<>();
			Set<Position> candPosSet = new HashSet<>();
			for (ShipCand shipCand : shipCandList) {
				if (shipCand.isFixed()) {
					fixedPosSet.addAll(shipCand.getCandPosSet());
				} else {
					candPosSet.addAll(shipCand.getCandPosSet());
				}
			}
			Masu[][] masu = new Masu[getYLength()][getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					Position pos = new Position(yIndex, xIndex);
					if (fixedPosSet.contains(pos)) {
						masu[yIndex][xIndex] = Masu.BLACK;
					} else if (candPosSet.contains(pos)) {
						masu[yIndex][xIndex] = Masu.SPACE;
					} else {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
				}
			}
			return masu;
		}

		public String getStateDump() {
			StringBuilder sb = new StringBuilder();
			for (ShipCand shipCand : shipCandList) {
				sb.append(shipCand.getShipPosObjList().size());
				sb.append(":");
			}
			return sb.toString();
		}

		/**
		 * 被る候補を消す。 壁の両側は必ず船が来る。壁がない場所で船が隣り合ってはだめ。 違反はfalse
		 */
		private boolean sikakuSolve() {
			// 被る候補
			for (ShipCand shipCand : shipCandList) {
				// 船が置けなくなっていたらだめ
				if (shipCand.getShipPosObjList().isEmpty()) {
					return false;
				} else if (shipCand.isFixed()) {
					// 船が1つなら確定し、他のそのマスを含む候補を削除
					Set<Position> candPosSet = shipCand.getCandPosSet();
					for (ShipCand otherShipCand : shipCandList) {
						if (shipCand.getNo() != otherShipCand.getNo()) {
							for (Iterator<ShipPosObj> iterator = otherShipCand.getShipPosObjList().iterator(); iterator
									.hasNext();) {
								ShipPosObj otherShipPosObj = iterator.next();
								boolean isContain = false;
								for (Position pos : otherShipPosObj.getPosSet()) {
									if (candPosSet.contains(pos)) {
										isContain = true;
										break;
									}
								}
								if (isContain) {
									iterator.remove();
								}
							}
						}
					}
				}
			}
			// 壁を使ったチェック
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					// 壁があれば、その壁の両側に置ける候補の船がなければダメ。置ける候補の船が1つならその船はそこに使う。
					if (yokoWall[yIndex][xIndex]) {
						List<ShipCand> wkShipCandList = new ArrayList<>();
						for (ShipCand shipCand : shipCandList) {
							if (shipCand.getCandPosSet().contains(new Position(yIndex, xIndex))) {
								wkShipCandList.add(shipCand);
							}
						}
						if (wkShipCandList.isEmpty()) {
							return false;
						}
						if (wkShipCandList.size() == 1) {
							for (Iterator<ShipPosObj> iterator = wkShipCandList.get(0).getShipPosObjList()
									.iterator(); iterator.hasNext();) {
								ShipPosObj shipPosObj = iterator.next();
								if (!shipPosObj.getPosSet().contains(new Position(yIndex, xIndex))) {
									iterator.remove();
								}
							}
						}
						wkShipCandList = new ArrayList<>();
						for (ShipCand shipCand : shipCandList) {
							if (shipCand.getCandPosSet().contains(new Position(yIndex, xIndex + 1))) {
								wkShipCandList.add(shipCand);
							}
						}
						if (wkShipCandList.isEmpty()) {
							return false;
						}
						if (wkShipCandList.size() == 1) {
							for (Iterator<ShipPosObj> iterator = wkShipCandList.get(0).getShipPosObjList()
									.iterator(); iterator.hasNext();) {
								ShipPosObj shipPosObj = iterator.next();
								if (!shipPosObj.getPosSet().contains(new Position(yIndex, xIndex + 1))) {
									iterator.remove();
								}
							}
						}
					} else {
						// 壁がなく、確定した船があるとき、隣接したマスに自分以外の船の候補は置けない。
						ShipCand shipFixed = null;
						for (ShipCand shipCand : shipCandList) {
							if (shipCand.isFixed() && shipCand.getCandPosSet().contains(new Position(yIndex, xIndex))) {
								shipFixed = shipCand;
								break;
							}
						}
						if (shipFixed != null) {
							for (ShipCand shipCand : shipCandList) {
								if (shipFixed != shipCand) {
									for (Iterator<ShipPosObj> iterator = shipCand.getShipPosObjList()
											.iterator(); iterator.hasNext();) {
										ShipPosObj shipPosObj = iterator.next();
										if (shipPosObj.getPosSet().contains(new Position(yIndex, xIndex + 1))) {
											iterator.remove();
										}
									}
									if (shipCand.getShipPosObjList().isEmpty()) {
										return false;
									}
								}
							}
						}
						shipFixed = null;
						for (ShipCand shipCand : shipCandList) {
							if (shipCand.isFixed()
									&& shipCand.getCandPosSet().contains(new Position(yIndex, xIndex + 1))) {
								shipFixed = shipCand;
								break;
							}
						}
						if (shipFixed != null) {
							for (ShipCand shipCand : shipCandList) {
								if (shipFixed != shipCand) {
									for (Iterator<ShipPosObj> iterator = shipCand.getShipPosObjList()
											.iterator(); iterator.hasNext();) {
										ShipPosObj shipPosObj = iterator.next();
										if (shipPosObj.getPosSet().contains(new Position(yIndex, xIndex))) {
											iterator.remove();
										}
									}
									if (shipCand.getShipPosObjList().isEmpty()) {
										return false;
									}
								}
							}
						}
					}
				}
			}
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (tateWall[yIndex][xIndex]) {
						List<ShipCand> wkShipCandList = new ArrayList<>();
						for (ShipCand shipCand : shipCandList) {
							if (shipCand.getCandPosSet().contains(new Position(yIndex, xIndex))) {
								wkShipCandList.add(shipCand);
							}
						}
						if (wkShipCandList.isEmpty()) {
							return false;
						}
						if (wkShipCandList.size() == 1) {
							for (Iterator<ShipPosObj> iterator = wkShipCandList.get(0).getShipPosObjList()
									.iterator(); iterator.hasNext();) {
								ShipPosObj shipPosObj = iterator.next();
								if (!shipPosObj.getPosSet().contains(new Position(yIndex, xIndex))) {
									iterator.remove();
								}
							}
						}
						wkShipCandList = new ArrayList<>();
						for (ShipCand shipCand : shipCandList) {
							if (shipCand.getCandPosSet().contains(new Position(yIndex + 1, xIndex))) {
								wkShipCandList.add(shipCand);
							}
						}
						if (wkShipCandList.isEmpty()) {
							return false;
						}
						if (wkShipCandList.size() == 1) {
							for (Iterator<ShipPosObj> iterator = wkShipCandList.get(0).getShipPosObjList()
									.iterator(); iterator.hasNext();) {
								ShipPosObj shipPosObj = iterator.next();
								if (!shipPosObj.getPosSet().contains(new Position(yIndex + 1, xIndex))) {
									iterator.remove();
								}
							}
						}
					} else {
						ShipCand shipFixed = null;
						for (ShipCand shipCand : shipCandList) {
							if (shipCand.isFixed() && shipCand.getCandPosSet().contains(new Position(yIndex, xIndex))) {
								shipFixed = shipCand;
								break;
							}
						}
						if (shipFixed != null) {
							for (ShipCand shipCand : shipCandList) {
								if (shipFixed != shipCand) {
									for (Iterator<ShipPosObj> iterator = shipCand.getShipPosObjList()
											.iterator(); iterator.hasNext();) {
										ShipPosObj shipPosObj = iterator.next();
										if (shipPosObj.getPosSet().contains(new Position(yIndex + 1, xIndex))) {
											iterator.remove();
										}
									}
									if (shipCand.getShipPosObjList().isEmpty()) {
										return false;
									}
								}
							}
						}
						shipFixed = null;
						for (ShipCand shipCand : shipCandList) {
							if (shipCand.isFixed()
									&& shipCand.getCandPosSet().contains(new Position(yIndex + 1, xIndex))) {
								shipFixed = shipCand;
								break;
							}
						}
						if (shipFixed != null) {
							for (ShipCand shipCand : shipCandList) {
								if (shipFixed != shipCand) {
									for (Iterator<ShipPosObj> iterator = shipCand.getShipPosObjList()
											.iterator(); iterator.hasNext();) {
										ShipPosObj shipPosObj = iterator.next();
										if (shipPosObj.getPosSet().contains(new Position(yIndex, xIndex))) {
											iterator.remove();
										}
									}
									if (shipCand.getShipPosObjList().isEmpty()) {
										return false;
									}
								}
							}
						}
					}
				}
			}
			return true;
		}

		// 同じ形の船を置くときには、左上のマスが含まれるものから順番に置いていく。
		private boolean shipSolve() {
			for (ShipCand shipPand : shipCandList) {
				if (shipPand.isFixed()) {
					for (ShipCand otherShipCand : shipCandList) {
						if (otherShipCand.getShipPosObjList().isEmpty()) {
							return false;
						}
						if (shipPand.getNo() != otherShipCand.getNo() && shipPand.getShipPosObjList().get(0)
								.isSame(otherShipCand.getShipPosObjList().get(0))) {
							Position pos = shipPand.getShipPosObjList().get(0).getPosSet().first();
							int posValue = pos.getyIndex() * 10000 + pos.getxIndex();
							for (Iterator<ShipPosObj> iterator = otherShipCand.getShipPosObjList().iterator(); iterator
									.hasNext();) {
								ShipPosObj shipPosObj = iterator.next();
								Position otherPos = shipPosObj.getPosSet().first();
								int otherPosValue = otherPos.getyIndex() * 10000 + otherPos.getxIndex();
								if (shipPand.getNo() > otherShipCand.getNo() && posValue <= otherPosValue) {
									iterator.remove();
								} else if (shipPand.getNo() < otherShipCand.getNo() && posValue >= otherPosValue) {
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
			if (!shipSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			}
			return true;
		}

		public boolean isSolved() {
			for (ShipCand shipCand : shipCandList) {
				if (!shipCand.isFixed()) {
					return false;
				}
			}
			return solveAndCheck();
		}

	}

	protected final Field field;
	protected int count;

	public KissingSolver(String param) {
		long start = System.nanoTime();
		field = new Field(param);
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
	}

	public KissingSolver(Field field) {
		this.field = new Field(field);
	}

	public Field getField() {
		return field;
	}

	public static void main(String[] args) {
		// // urlを入れれば試せる
		String url = "";
		System.out.println(new KissingSolver(url).solve());
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
		System.out.println("難易度:" + (count * 10));
		System.out.println(field);
		int level = (int) Math.sqrt(count * 10 / 3) + 1;
		return "解けました。";
//		return "解けました。推定難易度:" + Difficulty.getByCount(count * 10).toString() + "(Lv:" + level + ")";
	}

	/**
	 * 仮置きして調べる
	 * 
	 * @param posSet
	 */
	protected boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int i = 0; i < field.shipCandList.size(); i++) {
			ShipCand shipCand = field.shipCandList.get(i);
			for (Iterator<ShipPosObj> iterator = shipCand.getShipPosObjList().iterator(); iterator.hasNext();) {
				count++;
				ShipPosObj oneCand = iterator.next();
				Field virtual = new Field(field);
				virtual.shipCandList.get(i).getShipPosObjList().clear();
				virtual.shipCandList.get(i).getShipPosObjList().add(oneCand);
				boolean allowBlack = virtual.solveAndCheck();
				if (allowBlack && recursive > 0) {
					if (!candSolve(virtual, recursive - 1)) {
						allowBlack = false;
					}
				}
				Field virtual2 = new Field(field);
				virtual2.shipCandList.get(i).getShipPosObjList().remove(oneCand);
				boolean allowNotBlack = virtual2.solveAndCheck();
				if (allowNotBlack && recursive > 0) {
					if (!candSolve(virtual2, recursive - 1)) {
						allowNotBlack = false;
					}
				}
				if (!allowBlack && !allowNotBlack) {
					return false;
				} else if (!allowBlack) {
					field.shipCandList = virtual2.shipCandList;
				} else if (!allowNotBlack) {
					field.shipCandList = virtual.shipCandList;
				}
			}
		}
		if (!field.getStateDump().equals(str)) {
			return candSolve(field, recursive);
		}
		return true;
	}

}
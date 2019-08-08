package myamya.other.solver.nanro;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class NanroSolver implements Solver {

	public static class Field {
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// 固定数字(表示用)
		private final Integer[][] numbers;
		// 数字の候補情報
		protected List<Integer>[][] numbersCand;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<Set<Position>> rooms;

		public List<Integer>[][] getNumbersCand() {
			return numbersCand;
		}

		public Integer[][] getNumbers() {
			return numbers;
		}

		public boolean[][] getYokoWall() {
			return yokoWall;
		}

		public boolean[][] getTateWall() {
			return tateWall;
		}

		public int getYLength() {
			return numbersCand.length;
		}

		public int getXLength() {
			return numbersCand[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			numbersCand = new ArrayList[height][width];
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
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				int interval = ALPHABET_FROM_G.indexOf(ch);
				if (interval != -1) {
					index = index + interval + 1;
				} else {
					//16 - 255は '-'
					//256 - 999は '+'
					int num;
					if (ch == '.') {
						//
					} else {
						if (ch == '-') {
							num = Integer.parseInt("" + param.charAt(i + 1) + param.charAt(i + 2), 16);
							i++;
							i++;
						} else if (ch == '+') {
							num = Integer.parseInt(
									"" + param.charAt(i + 1) + param.charAt(i + 2) + param.charAt(i + 3),
									16);
							i++;
							i++;
							i++;
						} else {
							num = Integer.parseInt(String.valueOf(ch), 16);
						}
						Position pos = new Position(index / getXLength(), index % getXLength());
						numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						numbersCand[pos.getyIndex()][pos.getxIndex()].add(num);
						numbers[pos.getyIndex()][pos.getxIndex()] = num;
					}
					index++;
				}
			}
			// 部屋の大きさにより、初期候補数字を決定
			for (Set<Position> room : rooms) {
				for (Position pos : room) {
					if (numbers[pos.getyIndex()][pos.getxIndex()] == null) {
						numbersCand[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
						for (int number = 0; number <= room.size(); number++) {
							numbersCand[pos.getyIndex()][pos.getxIndex()].add(number);
						}
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			numbers = other.numbers;
			numbersCand = new ArrayList[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					numbersCand[yIndex][xIndex] = new ArrayList<>(other.numbersCand[yIndex][xIndex]);
				}
			}
			yokoWall = other.yokoWall;
			tateWall = other.tateWall;
			rooms = other.rooms;
		}

		// posを起点に上下左右に部屋壁でないマスを無制限につなげていく。
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet) {
			if (pos.getyIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex() - 1][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != getXLength() - 1) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getyIndex() != getYLength() - 1) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos) && !tateWall[pos.getyIndex()][pos.getxIndex()]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
			if (pos.getxIndex() != 0) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos) && !yokoWall[pos.getyIndex()][pos.getxIndex() - 1]) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet);
				}
			}
		}

		private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
		private static final String FULL_NUMS = "０１２３４５６７８９";

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
					if (numbersCand[yIndex][xIndex].size() == 0) {
						sb.append("×");
					} else if (numbersCand[yIndex][xIndex].size() == 1) {
						if (numbersCand[yIndex][xIndex].get(0) == 0) {
							sb.append("・");
						} else {
							String numStr = String.valueOf(numbersCand[yIndex][xIndex].get(0));
							int index = HALF_NUMS.indexOf(numStr);
							if (index >= 0) {
								sb.append(FULL_NUMS.substring(index / 2, index / 2 + 1));
							} else {
								sb.append(numStr);
							}
						}
					} else if (numbersCand[yIndex][xIndex].size() == 2) {
						sb.append(numbersCand[yIndex][xIndex].get(0));
						sb.append(numbersCand[yIndex][xIndex].get(1));
					} else {
						sb.append("　");
					}
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
					sb.append(numbersCand[yIndex][xIndex].size());
				}
			}
			return sb.toString();
		}

		/**
		 * 部屋には0以外の数字が同じ数だけ入る。また0だけの部屋は禁止。
		 */
		public boolean roomSolve() {
			for (Set<Position> room : rooms) {
				boolean zeroOnly = true;
				int candNum = 0;
				for (Position pos : room) {
					if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 1) {
						if (numbersCand[pos.getyIndex()][pos.getxIndex()].get(0) != 0) {
							zeroOnly = false;
							candNum = numbersCand[pos.getyIndex()][pos.getxIndex()].get(0);
							break;
						}
					} else {
						zeroOnly = false;
					}
				}
				if (zeroOnly) {
					return false;
				}
				if (candNum != 0) {
					// その数字と0以外の候補を消す
					for (Position pos : room) {
						for (Iterator<Integer> iterator = numbersCand[pos.getyIndex()][pos.getxIndex()]
								.iterator(); iterator.hasNext();) {
							int oneCand = iterator.next();
							if (oneCand != 0 && oneCand != candNum) {
								iterator.remove();
							}
						}
						if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 0) {
							return false;
						}
					}
					// 部屋に対する調査
					int blackCnt = 0;
					int spaceCnt = 0;
					for (Position pos : room) {
						if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 1
								&& numbersCand[pos.getyIndex()][pos.getxIndex()].get(0) != 0) {
							blackCnt++;
						} else if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 2) {
							spaceCnt++;
						}
					}
					if (blackCnt + spaceCnt < candNum) {
						// 数字マス不足
						return false;
					}
					// 置かねばならない数字マスの数
					int retainBlackCnt = candNum - blackCnt;
					if (retainBlackCnt < 0) {
						// 数字マス超過
						return false;
					} else if (retainBlackCnt == 0) {
						// 数字マス数が既に部屋の数字マス数に等しければ、部屋の他のマスは0
						for (Position pos : room) {
							if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 2) {
								numbersCand[pos.getyIndex()][pos.getxIndex()].remove(new Integer(candNum));
							}
						}
					} else if (spaceCnt == retainBlackCnt) {
						// 未確定マスが置かねばならない数字マスの数に等しければ、未確定マスは数字マス
						for (Position pos : room) {
							if (numbersCand[pos.getyIndex()][pos.getxIndex()].size() == 2) {
								numbersCand[pos.getyIndex()][pos.getxIndex()].remove(new Integer(0));
							}
						}
					}

				}
			}
			return true;
		}

		/**
		 * 壁を挟んで0以外の同じ数字は隣り合わない。
		 */
		private boolean nextSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					boolean wallUp = yIndex == 0 ? false : tateWall[yIndex - 1][xIndex];
					boolean wallRight = xIndex == getXLength() - 1 ? false : yokoWall[yIndex][xIndex];
					boolean wallDown = yIndex == getYLength() - 1 ? false : tateWall[yIndex][xIndex];
					boolean wallLeft = xIndex == 0 ? false : yokoWall[yIndex][xIndex - 1];
					if (wallUp) {
						if (numbersCand[yIndex][xIndex].size() == 1
								&& numbersCand[yIndex][xIndex].get(0) != 0) {
							numbersCand[yIndex - 1][xIndex].remove(numbersCand[yIndex][xIndex].get(0));
							if (numbersCand[yIndex - 1][xIndex].size() == 0) {
								return false;
							}
						}
						if (numbersCand[yIndex - 1][xIndex].size() == 1
								&& numbersCand[yIndex - 1][xIndex].get(0) != 0) {
							numbersCand[yIndex][xIndex].remove(numbersCand[yIndex - 1][xIndex].get(0));
							if (numbersCand[yIndex][xIndex].size() == 0) {
								return false;
							}
						}
					}
					if (wallRight) {
						if (numbersCand[yIndex][xIndex].size() == 1
								&& numbersCand[yIndex][xIndex].get(0) != 0) {
							numbersCand[yIndex][xIndex + 1].remove(numbersCand[yIndex][xIndex].get(0));
							if (numbersCand[yIndex][xIndex + 1].size() == 0) {
								return false;
							}
						}
						if (numbersCand[yIndex][xIndex + 1].size() == 1
								&& numbersCand[yIndex][xIndex + 1].get(0) != 0) {
							numbersCand[yIndex][xIndex].remove(numbersCand[yIndex][xIndex + 1].get(0));
							if (numbersCand[yIndex][xIndex].size() == 0) {
								return false;
							}
						}
					}
					if (wallDown) {
						if (numbersCand[yIndex][xIndex].size() == 1
								&& numbersCand[yIndex][xIndex].get(0) != 0) {
							numbersCand[yIndex + 1][xIndex].remove(numbersCand[yIndex][xIndex].get(0));
							if (numbersCand[yIndex + 1][xIndex].size() == 0) {
								return false;
							}
						}
						if (numbersCand[yIndex + 1][xIndex].size() == 1
								&& numbersCand[yIndex + 1][xIndex].get(0) != 0) {
							numbersCand[yIndex][xIndex].remove(numbersCand[yIndex + 1][xIndex].get(0));
							if (numbersCand[yIndex][xIndex].size() == 0) {
								return false;
							}
						}
					}
					if (wallLeft) {
						if (numbersCand[yIndex][xIndex].size() == 1
								&& numbersCand[yIndex][xIndex].get(0) != 0) {
							numbersCand[yIndex][xIndex - 1].remove(numbersCand[yIndex][xIndex].get(0));
							if (numbersCand[yIndex][xIndex - 1].size() == 0) {
								return false;
							}
						}
						if (numbersCand[yIndex][xIndex - 1].size() == 1
								&& numbersCand[yIndex][xIndex - 1].get(0) != 0) {
							numbersCand[yIndex][xIndex].remove(numbersCand[yIndex][xIndex - 1].get(0));
							if (numbersCand[yIndex][xIndex].size() == 0) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 数字マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> numberPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() == 1 && numbersCand[yIndex][xIndex].get(0) != 0) {
						Position numberPos = new Position(yIndex, xIndex);
						if (numberPosSet.size() == 0) {
							numberPosSet.add(numberPos);
							setContinuePosSet(numberPos, numberPosSet, null);
						} else {
							if (!numberPosSet.contains(numberPos)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * posを起点に上下左右に0確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& !(numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1 &&
								numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].get(0) == 0)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& !(numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1 &&
								numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].get(0) == 0)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& !(numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1 &&
								numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].get(0) == 0)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& !(numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].size() == 1 &&
								numbersCand[nextPos.getyIndex()][nextPos.getxIndex()].get(0) == 0)) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * 各種チェックを1セット実行
		 */
		private boolean solveAndCheck() {
			String str = getStateDump();
			if (!roomSolve()) {
				return false;
			}
			if (!nextSolve()) {
				return false;
			}
			if (!pondSolve()) {
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

		/**
		 * 0以外の数字が2x2になってはならない。
		 */
		private boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					List<Integer> cand1 = numbersCand[yIndex][xIndex];
					List<Integer> cand2 = numbersCand[yIndex][xIndex + 1];
					List<Integer> cand3 = numbersCand[yIndex + 1][xIndex];
					List<Integer> cand4 = numbersCand[yIndex + 1][xIndex + 1];
					if (!cand1.contains(0) && !cand2.contains(0) && !cand3.contains(0) && !cand4.contains(0)) {
						return false;
					}
					if (cand1.contains(0) && !cand2.contains(0) && !cand3.contains(0) && !cand4.contains(0)) {
						cand1.clear();
						cand1.add(0);
					}
					if (!cand1.contains(0) && cand2.contains(0) && !cand3.contains(0) && !cand4.contains(0)) {
						cand2.clear();
						cand2.add(0);
					}
					if (!cand1.contains(0) && !cand2.contains(0) && cand3.contains(0) && !cand4.contains(0)) {
						cand3.clear();
						cand3.add(0);
					}
					if (!cand1.contains(0) && !cand2.contains(0) && !cand3.contains(0) && cand4.contains(0)) {
						cand4.clear();
						cand4.add(0);
					}
				}
			}
			return true;
		}

		public boolean isSolved() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex].size() != 1) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public NanroSolver(int height, int width, String param) {
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
		System.out.println(new NanroSolver(height, width, param).solve());
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
			if (field.getStateDump().equals(befStr)) {
				if (!candSolve(field, 0)) {
					System.out.println(field);
					return "問題に矛盾がある可能性があります。途中経過を返します。";
				}
				if (field.getStateDump().equals(befStr)) {
					if (!candSolve(field, 1)) {
						System.out.println(field);
						return "問題に矛盾がある可能性があります。途中経過を返します。";
					}
					if (field.getStateDump().equals(befStr)) {
						if (!candSolve(field, 2)) {
							System.out.println(field);
							return "問題に矛盾がある可能性があります。途中経過を返します。";
						}
						if (field.getStateDump().equals(befStr)) {
							System.out.println(field);
							return "解けませんでした。途中経過を返します。";
						}
					}
				}
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count).toString();
	}

	/**
	 * 仮置きして調べる
	 * @param posSet
	 */
	private boolean candSolve(Field field, int recursive) {
		while (true) {
			String befStr = field.getStateDump();
			for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
					if (field.numbersCand[yIndex][xIndex].size() != 1) {
						for (Iterator<Integer> iterator = field.numbersCand[yIndex][xIndex].iterator(); iterator
								.hasNext();) {
							count++;
							int oneCand = iterator.next();
							Field virtual = new Field(field);
							virtual.numbersCand[yIndex][xIndex].clear();
							virtual.numbersCand[yIndex][xIndex].add(oneCand);
							boolean arrowCand = virtual.solveAndCheck();
							if (arrowCand && recursive > 0) {
								arrowCand = candSolve(virtual, recursive - 1);
							}
							if (!arrowCand) {
								iterator.remove();
							}
						}
						if (field.numbersCand[yIndex][xIndex].size() == 0) {
							return false;
						}
					}
				}
			}
			if (field.getStateDump().equals(befStr)) {
				break;
			}
		}
		return true;
	}
}
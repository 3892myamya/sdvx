package myamya.other.solver.usoone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class UsooneSolver implements Solver {

	public static class Field {
		static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";
		static final int BLACK_CNT = 1;
		// 固定数字
		private final Integer[][] numbers;
		// マスの情報
		private Masu[][] masu;
		// 嘘つきかどうか。
		private Masu[][] usos;
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateWall;
		// 同一グループに属するマスの情報
		private final List<Set<Position>> rooms;

		public Integer[][] getNumbers() {
			return numbers;
		}

		public Masu[][] getMasu() {
			return masu;
		}

		public Masu[][] getUsos() {
			return usos;
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

		public Field(int height, int width, String param) {
			numbers = new Integer[height][width];
			masu = new Masu[height][width];
			usos = new Masu[height][width];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = Masu.SPACE;
					usos[yIndex][xIndex] = Masu.NOT_BLACK;
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
			int index = 0;
			for (int i = readPos; i < param.length(); i++) {
				char ch = param.charAt(i);
				Position pos = new Position(index / getXLength(), index % getXLength());
				if (ch == '.') {
					index++;
				} else {
					int interval = ALPHABET_FROM_G.indexOf(ch);
					if (interval != -1) {
						index = index + interval + 1;
					} else {
						if (ch == 'a' || ch == 'b' || ch == 'c' || ch == 'd' || ch == 'e') {
							numbers[pos.getyIndex()][pos.getxIndex()] = ALPHABET.indexOf(ch);
							index++;
							index++;
						} else if (ch == '5' || ch == '6' || ch == '7' || ch == '8' || ch == '9') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch) - 5;
							index++;
						} else if (ch == '0' || ch == '1' || ch == '2' || ch == '3' || ch == '4') {
							numbers[pos.getyIndex()][pos.getxIndex()] = Character.getNumericValue(ch);
						}
						masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						usos[pos.getyIndex()][pos.getxIndex()] = Masu.SPACE;
						index++;
					}
				}
			}
		}

		public Field(Field other) {
			numbers = other.numbers;
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			usos = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					usos[yIndex][xIndex] = other.usos[yIndex][xIndex];
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
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						sb.append(usos[yIndex][xIndex] == Masu.BLACK ? "f"
								: usos[yIndex][xIndex] == Masu.NOT_BLACK ? "t" : "?");
						sb.append(numbers[yIndex][xIndex]);
					} else {
						sb.append(masu[yIndex][xIndex]);
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
					sb.append(masu[yIndex][xIndex]);
					sb.append(usos[yIndex][xIndex]);
				}
			}
			return sb.toString();
		}

		/**
		 * 部屋の嘘つきは1人のみ。
		 */
		public boolean roomSolve() {
			for (Set<Position> room : rooms) {
				// 部屋に対する調査
				int blackCnt = 0;
				int spaceCnt = 0;
				for (Position pos : room) {
					if (usos[pos.getyIndex()][pos.getxIndex()] == Masu.BLACK) {
						blackCnt++;
					} else if (usos[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
						spaceCnt++;
					}
				}
				if (blackCnt + spaceCnt < BLACK_CNT) {
					// 嘘つき不足
					return false;
				}
				// 置かねばならない嘘つきの数
				int retainBlackCnt = BLACK_CNT - blackCnt;
				if (retainBlackCnt < 0) {
					// 嘘つき超過
					return false;
				} else if (retainBlackCnt == 0) {
					// 嘘つき数が既に部屋の嘘つき数に等しければ、部屋の他のマスは正直
					for (Position pos : room) {
						if (usos[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							usos[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
						}
					}
				} else if (spaceCnt == retainBlackCnt) {
					// 未確定マスが置かねばならない嘘つきの数に等しければ、未確定マスは嘘つき
					for (Position pos : room) {
						if (usos[pos.getyIndex()][pos.getxIndex()] == Masu.SPACE) {
							usos[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
						}
					}
				}
			}
			return true;
		}

		/**
		 * 嘘つきでない場合、自分の周りにある黒マスの数は数字と同じになる。
		 * 逆に、嘘つきの場合自分の周りの黒ますは数字と違う数になる。
		 * 矛盾はfalseを返す。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null && numbers[yIndex][xIndex] != -1) {
						int blackCnt = 0;
						int whiteCnt = 0;
						Masu masuUp = yIndex == 0 ? Masu.NOT_BLACK : masu[yIndex - 1][xIndex];
						Masu masuRight = xIndex == getXLength() - 1 ? Masu.NOT_BLACK : masu[yIndex][xIndex + 1];
						Masu masuDown = yIndex == getYLength() - 1 ? Masu.NOT_BLACK : masu[yIndex + 1][xIndex];
						Masu masuLeft = xIndex == 0 ? Masu.NOT_BLACK : masu[yIndex][xIndex - 1];
						if (masuUp == Masu.BLACK) {
							blackCnt++;
						} else if (masuUp == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuRight == Masu.BLACK) {
							blackCnt++;
						} else if (masuRight == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuDown == Masu.BLACK) {
							blackCnt++;
						} else if (masuDown == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (masuLeft == Masu.BLACK) {
							blackCnt++;
						} else if (masuLeft == Masu.NOT_BLACK) {
							whiteCnt++;
						}
						if (usos[yIndex][xIndex] == Masu.SPACE) {
							if (numbers[yIndex][xIndex] < blackCnt) {
								// 黒マス過剰。嘘つき
								usos[yIndex][xIndex] = Masu.BLACK;
							}
							if (numbers[yIndex][xIndex] > 4 - whiteCnt) {
								// 黒マス不足。嘘つき
								usos[yIndex][xIndex] = Masu.BLACK;
							}
							if (numbers[yIndex][xIndex] == blackCnt && numbers[yIndex][xIndex] == 4 - whiteCnt) {
								// 正直者
								usos[yIndex][xIndex] = Masu.NOT_BLACK;
							}
						}
						if (usos[yIndex][xIndex] == Masu.NOT_BLACK) {
							if (numbers[yIndex][xIndex] < blackCnt) {
								// 黒マス過剰
								return false;
							}
							if (numbers[yIndex][xIndex] > 4 - whiteCnt) {
								// 黒マス不足
								return false;
							}
							if (numbers[yIndex][xIndex] == blackCnt) {
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
							if (numbers[yIndex][xIndex] == 4 - whiteCnt) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.BLACK;
								}
							}
						} else if (usos[yIndex][xIndex] == Masu.BLACK) {
							if (numbers[yIndex][xIndex] == blackCnt && numbers[yIndex][xIndex] == 4 - whiteCnt) {
								// 正直者
								return false;
							}
							if (numbers[yIndex][xIndex] == blackCnt && numbers[yIndex][xIndex] == 4 - whiteCnt - 1) {
								if (masuUp == Masu.SPACE) {
									masu[yIndex - 1][xIndex] = Masu.BLACK;
								}
								if (masuRight == Masu.SPACE) {
									masu[yIndex][xIndex + 1] = Masu.BLACK;
								}
								if (masuDown == Masu.SPACE) {
									masu[yIndex + 1][xIndex] = Masu.BLACK;
								}
								if (masuLeft == Masu.SPACE) {
									masu[yIndex][xIndex - 1] = Masu.BLACK;
								}
							}
							if (numbers[yIndex][xIndex] == blackCnt - 1 && numbers[yIndex][xIndex] == 4 - whiteCnt) {
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
				}
			}
			return true;
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
		 * 白マスがひとつながりにならない場合Falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> blackPosSet = new HashSet<>();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.NOT_BLACK) {
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
		 * posを起点に上下左右に黒確定でないマスを無制限につなげていく。
		 */
		private void setContinuePosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
					continuePosSet.add(nextPos);
					setContinuePosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.BLACK) {
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
			if (!numberSolve()) {
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
					if (usos[yIndex][xIndex] == Masu.SPACE) {
						return false;
					}
				}
			}
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public UsooneSolver(int height, int width, String param) {
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
		System.out.println(new UsooneSolver(height, width, param).solve());
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
				return "解けませんでした。途中経過を返します。";
			}
		}
		System.out.println(((System.nanoTime() - start) / 1000000) + "ms.");
		System.out.println("難易度:" + (count * 5));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 5).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
		String str = field.getStateDump();
		for (int yIndex = 0; yIndex < field.getYLength(); yIndex++) {
			for (int xIndex = 0; xIndex < field.getXLength(); xIndex++) {
				if (field.masu[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneCandSolve(field, yIndex, xIndex, recursive)) {
						return false;
					}
				}
				if (field.usos[yIndex][xIndex] == Masu.SPACE) {
					count++;
					if (!oneUsoCandSolve(field, yIndex, xIndex, recursive)) {
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
			field.usos = virtual2.usos;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.usos = virtual.usos;
		}
		return true;
	}

	/**
	 * 1つのマスに対する仮置き調査
	 */
	private boolean oneUsoCandSolve(Field field, int yIndex, int xIndex, int recursive) {
		Field virtual = new Field(field);
		virtual.usos[yIndex][xIndex] = Masu.BLACK;
		boolean allowBlack = virtual.solveAndCheck();
		if (allowBlack && recursive > 0) {
			if (!candSolve(virtual, recursive - 1)) {
				allowBlack = false;
			}
		}
		Field virtual2 = new Field(field);
		virtual2.usos[yIndex][xIndex] = Masu.NOT_BLACK;
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
			field.usos = virtual2.usos;
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
			field.usos = virtual.usos;
		}
		return true;
	}
}
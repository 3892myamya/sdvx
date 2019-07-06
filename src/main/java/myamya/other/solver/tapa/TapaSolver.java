package myamya.other.solver.tapa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import myamya.other.solver.Common.Difficulty;
import myamya.other.solver.Common.Direction;
import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Solver;

public class TapaSolver implements Solver {

	public static class Field {
		static final String NUMBERS_TO_8 = "012345678";
		static final String ALPHABET = "abcde";
		static final String ALPHABET_FROM_G = "ghijklmnopqrstuvwxyz";

		// マスの情報
		private Masu[][] masu;
		// 数字の情報
		private final List<Integer>[][] numbers;
		// 数字のマスに対する、置き方候補
		private final Set<String>[][] numbersCand;

		public Masu[][] getMasu() {
			return masu;
		}

		public List<Integer>[][] getNumbers() {
			return numbers;
		}

		public int getYLength() {
			return masu.length;
		}

		public int getXLength() {
			return masu[0].length;
		}

		@SuppressWarnings("unchecked")
		public Field(int height, int width, String param) {
			masu = new Masu[height][width];
			numbers = new ArrayList[height][width];
			numbersCand = new HashSet[height][width];
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
					Position pos = new Position(index / getXLength(), index % getXLength());
					masu[pos.getyIndex()][pos.getxIndex()] = Masu.NOT_BLACK;
					numbers[pos.getyIndex()][pos.getxIndex()] = new ArrayList<>();
					if (ch == '.') {
						numbers[pos.getyIndex()][pos.getxIndex()].add(-1);
					} else if (NUMBERS_TO_8.indexOf(ch) != -1) {
						numbers[pos.getyIndex()][pos.getxIndex()].add(NUMBERS_TO_8.indexOf(ch));
					} else if (ch == '9') {
						numbers[pos.getyIndex()][pos.getxIndex()].add(1);
						numbers[pos.getyIndex()][pos.getxIndex()].add(1);
						numbers[pos.getyIndex()][pos.getxIndex()].add(1);
						numbers[pos.getyIndex()][pos.getxIndex()].add(1);
					} else if (ch == 'a') {
						int seed = Character.getNumericValue(param.charAt(i + 1));
						if (seed / 6 == 0) {
							numbers[pos.getyIndex()][pos.getxIndex()].add(-1);
						} else {
							numbers[pos.getyIndex()][pos.getxIndex()].add(seed / 6);
						}
						if (seed % 6 == 0) {
							numbers[pos.getyIndex()][pos.getxIndex()].add(-1);
						} else {
							numbers[pos.getyIndex()][pos.getxIndex()].add(seed % 6);
						}
						i++;
					} else {
						int seed = Character.getNumericValue(param.charAt(i + 1));
						if (ch == 'c') {
							seed = seed + 36;
						}
						if (seed / 16 == 0) {
							numbers[pos.getyIndex()][pos.getxIndex()].add(-1);
						} else {
							numbers[pos.getyIndex()][pos.getxIndex()].add(seed / 16);
						}
						if (seed / 4 % 4 == 0) {
							numbers[pos.getyIndex()][pos.getxIndex()].add(-1);
						} else {
							numbers[pos.getyIndex()][pos.getxIndex()].add(seed / 4 % 4);
						}
						if (seed % 4 == 0) {
							numbers[pos.getyIndex()][pos.getxIndex()].add(-1);
						} else {
							numbers[pos.getyIndex()][pos.getxIndex()].add(seed % 4);
						}
						i++;
					}
					// 候補の列挙は、45*30の問題でも高々50ms程度。線形増加なのでまあええやろ
					numbersCand[pos.getyIndex()][pos.getxIndex()] = solveNumbersCand(
							numbers[pos.getyIndex()][pos.getxIndex()]);
					index++;
				}
			}
		}

		/**
		 * 数字の羅列から黒マスの置き方の候補を列挙した文字列リストを生成する。
		 * 文字列の長さは8。各文字は基準マスに対して以下の位置関係になる。
		 *
		 * ０１２
		 * ７＊３
		 * ６５４
		 */
		private Set<String> solveNumbersCand(List<Integer> numberList) {
			// TODO ソースが少し汚いけど大丈夫？
			List<List<Integer>> compareList = new ArrayList<>();
			if (numberList.contains(-1)) {
				List<Integer> compareBase = new ArrayList<>(numberList);
				int wildCnt = 0;
				while (compareBase.remove(new Integer(-1))) {
					wildCnt++;
				}
				if (wildCnt == 1) {
					for (int i = 0; i < compareBase.size() + 1; i++) {
						List<Integer> compare = new ArrayList<>(compareBase);
						compare.add(i, -1);
						compareList.add(compare);
					}
				} else if (wildCnt == 2) {
					for (int i = 0; i < compareBase.size() + 1; i++) {
						for (int j = i; j < compareBase.size() + 1; j++) {
							List<Integer> compare = new ArrayList<>(compareBase);
							compare.add(i, -1);
							compare.add(j, -1);
							compareList.add(compare);
						}
					}
				} else if (wildCnt == 3) {
					for (int i = 0; i < compareBase.size() + 1; i++) {
						for (int j = i; j < compareBase.size() + 1; j++) {
							for (int k = j; k < compareBase.size() + 1; k++) {
								List<Integer> compare = new ArrayList<>(compareBase);
								compare.add(i, -1);
								compare.add(j, -1);
								compare.add(k, -1);
								compareList.add(compare);
							}
						}
					}
				} else if (wildCnt == 4) {
					List<Integer> compare = new ArrayList<>(compareBase);
					compare.add(1);
					compare.add(1);
					compare.add(1);
					compare.add(1);
					compareList.add(compare);

				}
			} else {
				List<Integer> compare = new ArrayList<>(numberList);
				Collections.sort(compare);
				compareList.add(compare);
			}
			Set<String> result = new HashSet<>();
			solveOneNumbersCand(compareList, result, new StringBuilder());
			return result;
		}

		private void solveOneNumbersCand(List<List<Integer>> compareList, Set<String> result, StringBuilder sb) {
			if (sb.length() == 8) {
				List<Integer> work = new ArrayList<>();
				int wkInt = 0;
				for (int i = 0; i < sb.length(); i++) {
					char c = sb.charAt(i);
					if (c == '■') {
						wkInt++;
					} else {
						if (wkInt != 0) {
							work.add(wkInt);
							wkInt = 0;
						}
					}
				}
				if (sb.charAt(7) == '■') {
					if (sb.charAt(0) == '■' && work.size() != 0) {
						work.add(work.remove(0) + wkInt);
					} else {
						work.add(wkInt);
					}
				} else {
					if (work.size() == 0) {
						work.add(wkInt);
					}
				}
				if (compareList.get(0).size() == work.size()) {
					Collections.sort(work);
					for (List<Integer> compare : compareList) {
						boolean allEqual = true;
						for (int i = 0; i < work.size(); i++) {
							if (compare.get(i) != work.get(i) && compare.get(i) != -1) {
								allEqual = false;
								break;
							}
						}
						if (allEqual) {
							result.add(sb.toString());
						}
					}
				}
			} else {
				sb.append("■");
				solveOneNumbersCand(compareList, result, sb);
				sb.setLength(sb.length() - 1);
				sb.append("・");
				solveOneNumbersCand(compareList, result, sb);
				sb.setLength(sb.length() - 1);
			}
		}

		@SuppressWarnings("unchecked")
		public Field(Field other) {
			masu = new Masu[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					masu[yIndex][xIndex] = other.masu[yIndex][xIndex];
				}
			}
			numbers = other.numbers;
			numbersCand = new HashSet[other.getYLength()][other.getXLength()];
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (other.numbersCand[yIndex][xIndex] != null) {
						numbersCand[yIndex][xIndex] = new HashSet<>(other.numbersCand[yIndex][xIndex]);
					}
				}
			}
		}

		private static final String FULL_NUMS = "０１２３４５６７８９";

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbers[yIndex][xIndex] != null) {
						if (numbers[yIndex][xIndex].size() > 2) {
							sb.append("＊");
						} else if (numbers[yIndex][xIndex].size() == 2) {
							sb.append(numbers[yIndex][xIndex].get(0) == -1 ? "?" : numbers[yIndex][xIndex].get(0));
							sb.append(numbers[yIndex][xIndex].get(1) == -1 ? "?" : numbers[yIndex][xIndex].get(1));
						} else {
							if (numbers[yIndex][xIndex].get(0) == -1) {
								sb.append("？");
							} else {
								sb.append(FULL_NUMS.substring(numbers[yIndex][xIndex].get(0),
										numbers[yIndex][xIndex].get(0) + 1));
							}
						}
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
		 * 数字に対し、候補によって決まるマスを埋める。
		 * 指定した黒マスを満たせなくなる場合falseを返す。
		 */
		private boolean numberSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (numbersCand[yIndex][xIndex] != null) {
						Set<String> candSet = numbersCand[yIndex][xIndex];
						StringBuilder compareState = new StringBuilder();
						if (yIndex != 0 && xIndex != 0) {
							compareState.append(masu[yIndex - 1][xIndex - 1].toString());
						} else {
							compareState.append("・");
						}
						if (yIndex != 0) {
							compareState.append(masu[yIndex - 1][xIndex].toString());
						} else {
							compareState.append("・");
						}
						if (yIndex != 0 && xIndex != getXLength() - 1) {
							compareState.append(masu[yIndex - 1][xIndex + 1].toString());
						} else {
							compareState.append("・");
						}
						if (xIndex != getXLength() - 1) {
							compareState.append(masu[yIndex][xIndex + 1].toString());
						} else {
							compareState.append("・");
						}
						if (yIndex != getYLength() - 1 && xIndex != getXLength() - 1) {
							compareState.append(masu[yIndex + 1][xIndex + 1].toString());
						} else {
							compareState.append("・");
						}
						if (yIndex != getYLength() - 1) {
							compareState.append(masu[yIndex + 1][xIndex].toString());
						} else {
							compareState.append("・");
						}
						if (yIndex != getYLength() - 1 && xIndex != 0) {
							compareState.append(masu[yIndex + 1][xIndex - 1].toString());
						} else {
							compareState.append("・");
						}
						if (xIndex != 0) {
							compareState.append(masu[yIndex][xIndex - 1].toString());
						} else {
							compareState.append("・");
						}
						for (Iterator<String> iterator = candSet.iterator(); iterator.hasNext();) {
							String state = iterator.next();
							for (int idx = 0; idx < state.length(); idx++) {
								if ((compareState.charAt(idx) == '■'
										&& state.charAt(idx) == '・')
										|| (compareState.charAt(idx) == '・'
												&& state.charAt(idx) == '■')) {
									iterator.remove();
									break;
								}
							}
						}
						if (candSet.size() == 0) {
							return false;
						} else {
							StringBuilder fixState = new StringBuilder(new ArrayList<>(candSet).get(0));
							for (String cand : candSet) {
								for (int idx = 0; idx < fixState.length(); idx++) {
									char a = fixState.charAt(idx);
									char b = cand.charAt(idx);
									if ((a == '■' && b == '・') || (a == '・' && b == '■')) {
										fixState.setCharAt(idx, '　');
									}
								}
							}
							if (yIndex != 0 && xIndex != 0) {
								masu[yIndex - 1][xIndex - 1] = fixState.charAt(0) == '■' ? Masu.BLACK
										: fixState.charAt(0) == '・' ? Masu.NOT_BLACK : Masu.SPACE;
							}
							if (yIndex != 0) {
								masu[yIndex - 1][xIndex] = fixState.charAt(1) == '■' ? Masu.BLACK
										: fixState.charAt(1) == '・' ? Masu.NOT_BLACK : Masu.SPACE;
							}
							if (yIndex != 0 && xIndex != getXLength() - 1) {
								masu[yIndex - 1][xIndex + 1] = fixState.charAt(2) == '■' ? Masu.BLACK
										: fixState.charAt(2) == '・' ? Masu.NOT_BLACK : Masu.SPACE;
							}
							if (xIndex != getXLength() - 1) {
								masu[yIndex][xIndex + 1] = fixState.charAt(3) == '■' ? Masu.BLACK
										: fixState.charAt(3) == '・' ? Masu.NOT_BLACK : Masu.SPACE;
							}
							if (yIndex != getYLength() - 1 && xIndex != getXLength() - 1) {
								masu[yIndex + 1][xIndex + 1] = fixState.charAt(4) == '■' ? Masu.BLACK
										: fixState.charAt(4) == '・' ? Masu.NOT_BLACK : Masu.SPACE;
							}
							if (yIndex != getYLength() - 1) {
								masu[yIndex + 1][xIndex] = fixState.charAt(5) == '■' ? Masu.BLACK
										: fixState.charAt(5) == '・' ? Masu.NOT_BLACK : Masu.SPACE;
							}
							if (yIndex != getYLength() - 1 && xIndex != 0) {
								masu[yIndex + 1][xIndex - 1] = fixState.charAt(6) == '■' ? Masu.BLACK
										: fixState.charAt(6) == '・' ? Masu.NOT_BLACK : Masu.SPACE;
							}
							if (xIndex != 0) {
								masu[yIndex][xIndex - 1] = fixState.charAt(7) == '■' ? Masu.BLACK
										: fixState.charAt(7) == '・' ? Masu.NOT_BLACK : Masu.SPACE;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * 置くと池ができるマスを白マスにする。
		 * 既に池ができている場合falseを返す。
		 */
		public boolean pondSolve() {
			for (int yIndex = 0; yIndex < getYLength() - 1; yIndex++) {
				for (int xIndex = 0; xIndex < getXLength() - 1; xIndex++) {
					Masu masu1 = masu[yIndex][xIndex];
					Masu masu2 = masu[yIndex][xIndex + 1];
					Masu masu3 = masu[yIndex + 1][xIndex];
					Masu masu4 = masu[yIndex + 1][xIndex + 1];
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						return false;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.SPACE) {
						masu[yIndex + 1][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.BLACK && masu3 == Masu.SPACE && masu4 == Masu.BLACK) {
						masu[yIndex + 1][xIndex] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.BLACK && masu2 == Masu.SPACE && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex + 1] = Masu.NOT_BLACK;
					}
					if (masu1 == Masu.SPACE && masu2 == Masu.BLACK && masu3 == Masu.BLACK && masu4 == Masu.BLACK) {
						masu[yIndex][xIndex] = Masu.NOT_BLACK;
					}
				}
			}
			return true;
		}

		/**
		 * 黒マスが1つながりになっていない場合falseを返す。
		 */
		public boolean connectSolve() {
			Set<Position> whitePosSet = new HashSet<>();
			Position typicalWhitePos = null;
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] == Masu.BLACK) {
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
				setContinueBlackPosSet(typicalWhitePos, continuePosSet, null);
				whitePosSet.removeAll(continuePosSet);
				return whitePosSet.isEmpty();
			}
		}

		/**
		 * posを起点に上下左右に白確定でないマスをつなげていく。
		 */
		private void setContinueBlackPosSet(Position pos, Set<Position> continuePosSet, Direction from) {
			if (pos.getyIndex() != 0 && from != Direction.UP) {
				Position nextPos = new Position(pos.getyIndex() - 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueBlackPosSet(nextPos, continuePosSet, Direction.DOWN);
				}
			}
			if (pos.getxIndex() != getXLength() - 1 && from != Direction.RIGHT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() + 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueBlackPosSet(nextPos, continuePosSet, Direction.LEFT);
				}
			}
			if (pos.getyIndex() != getYLength() - 1 && from != Direction.DOWN) {
				Position nextPos = new Position(pos.getyIndex() + 1, pos.getxIndex());
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueBlackPosSet(nextPos, continuePosSet, Direction.UP);
				}
			}
			if (pos.getxIndex() != 0 && from != Direction.LEFT) {
				Position nextPos = new Position(pos.getyIndex(), pos.getxIndex() - 1);
				if (!continuePosSet.contains(nextPos)
						&& masu[nextPos.getyIndex()][nextPos.getxIndex()] != Masu.NOT_BLACK) {
					continuePosSet.add(nextPos);
					setContinueBlackPosSet(nextPos, continuePosSet, Direction.RIGHT);
				}
			}
		}

		/**
		 * フィールドに1つは黒マスが必要。
		 */
		private boolean finalSolve() {
			for (int yIndex = 0; yIndex < getYLength(); yIndex++) {
				for (int xIndex = 0; xIndex < getXLength(); xIndex++) {
					if (masu[yIndex][xIndex] != Masu.NOT_BLACK) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * 各種チェックを1セット実行
		 * @param recursive
		 */
		public boolean solveAndCheck() {
			String str = getStateDump();
			if (!numberSolve()) {
				return false;
			}
			if (!pondSolve()) {
				return false;
			}
			if (!getStateDump().equals(str)) {
				return solveAndCheck();
			} else {
				if (!connectSolve()) {
					return false;
				}
				if (!finalSolve()) {
					return false;
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
			return solveAndCheck();
		}

	}

	private final Field field;
	private int count = 0;

	public TapaSolver(int height, int width, String param) {
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
		System.out.println(new TapaSolver(height, width, param).solve());
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
		System.out.println("難易度:" + (count * 2));
		System.out.println(field);
		return "解けました。推定難易度:"
				+ Difficulty.getByCount(count * 2).toString();
	}

	/**
	 * 仮置きして調べる
	 */
	private boolean candSolve(Field field, int recursive) {
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
		} else if (!allowNotBlack) {
			field.masu = virtual.masu;
		}
		return true;
	}
}
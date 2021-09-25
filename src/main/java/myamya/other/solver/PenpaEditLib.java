package myamya.other.solver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import myamya.other.solver.Common.Masu;
import myamya.other.solver.Common.Pipemasu;
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import net.arnx.jsonic.JSON;

public class PenpaEditLib {

	public enum PuzzleType {
		NONE, YAJILIN, MAKEROOM, HINTS_BW;

	}

	public static final String PENPA_EDIT_DUMMY_URL = "penpa-edit-dummy-url";

	/**
	 * 縦壁と横壁を組み合わせた構造体
	 */
	public static class RoomWalls {
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final boolean[][] yokoRoomWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final boolean[][] tateRoomWall;

		public RoomWalls(boolean[][] yokoRoomWall, boolean[][] tateRoomWall) {
			this.yokoRoomWall = yokoRoomWall;
			this.tateRoomWall = tateRoomWall;
		}

		public boolean[][] getYokoRoomWall() {
			return yokoRoomWall;
		}

		public boolean[][] getTateRoomWall() {
			return tateRoomWall;
		}

	}

	/**
	 * パイプリンク表出向けの構造体
	 */
	public static class PipelinkWalls {
		// 横をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と0,1の間に壁があるという意味
		private final Wall[][] yokoWall;
		// 縦をふさぐ壁が存在するか
		// 0,0 = trueなら、0,0と1,0の間に壁があるという意味
		private final Wall[][] tateWall;
		// 表出マス情報 画面表示用
		private final Set<Position> firstPosSet;

		public PipelinkWalls(Wall[][] yokoWall, Wall[][] tateWall, Set<Position> firstPosSet) {
			this.yokoWall = yokoWall;
			this.tateWall = tateWall;
			this.firstPosSet = firstPosSet;
		}

		public Wall[][] getYokoWall() {
			return yokoWall;
		}

		public Wall[][] getTateWall() {
			return tateWall;
		}

		public Set<Position> getFirstPosSet() {
			return firstPosSet;
		}

	}

	/**
	 * 和フスマ向け構造体
	 */
	public static class TateYokoNumbers {
		private final Integer[][] yokoNumbers;
		private final Integer[][] tateNumbers;

		public TateYokoNumbers(Integer[][] yokoNumbers, Integer[][] tateNumbers) {
			this.yokoNumbers = yokoNumbers;
			this.tateNumbers = tateNumbers;
		}

		public Integer[][] getYokoNumbers() {
			return yokoNumbers;
		}

		public Integer[][] getTateNumbers() {
			return tateNumbers;
		}
	}

	/**
	 * 左と上にヒントがあるタイプのパズル向けのヒント構造体
	 */
	public static class UpLeftHints {
		private final Integer[] upHints;
		private final Integer[] leftHints;

		public UpLeftHints(Integer[] upHints, Integer[] leftHints) {
			this.upHints = upHints;
			this.leftHints = leftHints;
		}

		public Integer[] getUpHints() {
			return upHints;
		}

		public Integer[] getLeftHints() {
			return leftHints;
		}
	}

	/**
	 * 数字オンリー盤面のPenpaEdit向け文字列を返す。numbersは正方形である前提で、？は未対応
	 */
	public static String convertNumbersField(Integer[][] numbers) {
		int fieldSize = numbers.length;
		StringBuilder sb = new StringBuilder();
		int firstPos = 2 * (fieldSize + 5);
		for (int yIndex = 0; yIndex < numbers.length; yIndex++) {
			for (int xIndex = 0; xIndex < numbers[0].length; xIndex++) {
				if (numbers[yIndex][xIndex] != null) {
					if (sb.length() != 0) {
						sb.append(",");
					}
					sb.append("\"");
					sb.append(firstPos + yIndex * (numbers[0].length + 4) + xIndex);
					sb.append("\":[\"");
					sb.append(numbers[yIndex][xIndex]);
					sb.append("\",1,\"1\"]");
				}
			}
		}
		return convertFieldBefore(fieldSize, PuzzleType.NONE) + "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{" + sb.toString()
				+ "},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n\n"
				+ convertFieldAfter(fieldSize);
	}

	public static String convertHintsField(int size, Integer[] upHints, Integer[] leftHints) {
		StringBuilder sb = new StringBuilder();
		int firstPos = 2 * (size + 6);
		for (int xIndex = 1; xIndex < size + 1; xIndex++) {
			if (upHints[xIndex - 1] != null) {
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append("\"");
				sb.append(firstPos + xIndex);
				sb.append("\":[\"");
				sb.append(upHints[xIndex - 1]);
				sb.append("\",1,\"1\"]");
			}
		}
		for (int yIndex = 1; yIndex < size + 1; yIndex++) {
			if (leftHints[yIndex - 1] != null) {
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append("\"");
				sb.append(firstPos + yIndex * (size + 5));
				sb.append("\":[\"");
				sb.append(leftHints[yIndex - 1]);
				sb.append("\",1,\"1\"]");
			}
		}
		return convertFieldBefore(size, PuzzleType.HINTS_BW) + "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{" + sb.toString()
				+ "},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n\n"
				+ convertFieldAfterHintsBw(size);
	}

	/**
	 * 数字+白黒盤面のPenpaEdit向け文字列を返す。numbersは正方形である前提で、？は未対応
	 */
	public static String convertNumbersMasuField(Masu[][] masu, Integer[][] numbers) {
		int fieldSize = numbers.length;
		int firstPos = 2 * (fieldSize + 5);
		StringBuilder masuSb = new StringBuilder();
		for (int yIndex = 0; yIndex < masu.length; yIndex++) {
			for (int xIndex = 0; xIndex < masu[0].length; xIndex++) {
				if (masu[yIndex][xIndex] == Masu.BLACK) {
					if (masuSb.length() != 0) {
						masuSb.append(",");
					}
					masuSb.append("\"");
					masuSb.append(firstPos + yIndex * (masu[0].length + 4) + xIndex);
					masuSb.append("\":4");
				}
			}
		}
		StringBuilder numberSb = new StringBuilder();
		for (int yIndex = 0; yIndex < numbers.length; yIndex++) {
			for (int xIndex = 0; xIndex < numbers[0].length; xIndex++) {
				if (numbers[yIndex][xIndex] != null) {
					if (numberSb.length() != 0) {
						numberSb.append(",");
					}
					numberSb.append("\"");
					numberSb.append(firstPos + yIndex * (numbers[0].length + 4) + xIndex);
					numberSb.append("\":[\"");
					numberSb.append(numbers[yIndex][xIndex]);
					numberSb.append("\",");
					numberSb.append(masu[yIndex][xIndex] == Masu.BLACK ? "4" : "1");
					numberSb.append(",\"1\"]");
				}
			}
		}
		return convertFieldBefore(fieldSize, PuzzleType.NONE) + "{zR:{z_:[]},zU:{z_:[]},zS:{" + masuSb.toString()
				+ "},zN:{" + numberSb.toString()
				+ "},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n\n"
				+ convertFieldAfter(fieldSize);
	}

	/**
	 * クリーク型盤面のPenpaEdit向け文字列を返す。extraNumbersは正方形である前提
	 */
	public static String convertExtraNumbersField(Integer[][] extraNumbers) {
		int fieldSize = extraNumbers.length - 1;
		StringBuilder sb = new StringBuilder();
		int firstPos = fieldSize * fieldSize + 9 * fieldSize + 21;
		for (int yIndex = 0; yIndex < extraNumbers.length; yIndex++) {
			for (int xIndex = 0; xIndex < extraNumbers[0].length; xIndex++) {
				if (extraNumbers[yIndex][xIndex] != null) {
					if (sb.length() != 0) {
						sb.append(",");
					}
					sb.append("\"");
					sb.append(firstPos + yIndex * (extraNumbers[0].length + 3) + xIndex);
					sb.append("\":[\"");
					sb.append(extraNumbers[yIndex][xIndex]);
					sb.append("\",6,\"1\"]");
				}
			}
		}
		return convertFieldBefore(fieldSize, PuzzleType.NONE) + "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{" + sb.toString()
				+ "},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n\n"
				+ convertFieldAfter(fieldSize);
	}

	/**
	 * パイプリンク型盤面のPenpaEdit向け文字列を返す。盤面が正方形である前提
	 */
	public static String convertPipelinkField(int size, Wall[][] tateWall, Wall[][] yokoWall,
			Set<Position> firstPosSet) {
		StringBuilder sb = new StringBuilder();
		int firstPos = 2 * (size + 5);
		for (int yIndex = 0; yIndex < size; yIndex++) {
			for (int xIndex = 0; xIndex < size; xIndex++) {
				if (firstPosSet.contains(new Position(yIndex, xIndex))) {
					if (sb.length() != 0) {
						sb.append(",");
					}
					sb.append("\"");
					sb.append(firstPos + yIndex * (size + 4) + xIndex);
					sb.append("\":[[");
					Pipemasu pipe = Pipemasu.getByWall((yIndex == 0 || tateWall[yIndex - 1][xIndex] != Wall.NOT_EXISTS),
							(xIndex == size - 1 || yokoWall[yIndex][xIndex] != Wall.NOT_EXISTS),
							(yIndex == size - 1 || tateWall[yIndex][xIndex] != Wall.NOT_EXISTS),
							(xIndex == 0 || yokoWall[yIndex][xIndex - 1] != Wall.NOT_EXISTS));
					if (pipe == Pipemasu.UP_RIGHT) {
						sb.append("1,0,0,1");
					} else if (pipe == Pipemasu.UP_DOWN) {
						sb.append("0,1,0,1");
					} else if (pipe == Pipemasu.UP_LEFT) {
						sb.append("0,0,1,1");
					} else if (pipe == Pipemasu.RIGHT_DOWN) {
						sb.append("1,1,0,0");
					} else if (pipe == Pipemasu.RIGHT_LEFT) {
						sb.append("1,0,1,0");
					} else if (pipe == Pipemasu.DOWN_LEFT) {
						sb.append("0,1,1,0");
					}
					sb.append("],\"cross\",2]");
				}

			}
		}
		return convertFieldBefore(size, PuzzleType.YAJILIN) + "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{},z1:{},zY:{"
				+ sb.toString() + "},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n\n"
				+ convertFieldAfter(size);
	}

	/**
	 * 和フスマのPenpaEdit向け文字列を返す。盤面が正方形である前提
	 */
	public static String convertWafusumaField(int size, Integer[][] yokoNumbers, Integer[][] tateNumbers) {
		// 左上のインデックスを確定する処理
		int yokoFirstIndex = 58;
		yokoFirstIndex = yokoFirstIndex + (12 * size);
		yokoFirstIndex = yokoFirstIndex + (14 + size * 3) * size;
		Map<Position, Integer> yokoPositionMap = new HashMap<>();
		int yokoKeyIndex = yokoFirstIndex;
		for (int yIndex = 0; yIndex < size; yIndex++) {
			for (int xIndex = 0; xIndex < size - 1; xIndex++) {
				yokoPositionMap.put(new Position(yIndex, xIndex), yokoKeyIndex);
				yokoKeyIndex++;
			}
			yokoKeyIndex = yokoKeyIndex + 5;
		}
		int tateFirstIndex = 42;
		tateFirstIndex = tateFirstIndex + (8 * size);
		tateFirstIndex = tateFirstIndex + (10 + size * 2) * size;
		Map<Position, Integer> tatePositionMap = new HashMap<>();
		int tateKeyIndex = tateFirstIndex;
		for (int yIndex = 0; yIndex < size - 1; yIndex++) {
			for (int xIndex = 0; xIndex < size; xIndex++) {
				tatePositionMap.put(new Position(yIndex, xIndex), tateKeyIndex);
				tateKeyIndex++;
			}
			tateKeyIndex = tateKeyIndex + 4;
		}
		StringBuilder sb = new StringBuilder();
		for (int yIndex = 0; yIndex < yokoNumbers.length; yIndex++) {
			for (int xIndex = 0; xIndex < yokoNumbers[0].length; xIndex++) {
				if (yokoNumbers[yIndex][xIndex] != null) {
					if (sb.length() != 0) {
						sb.append(",");
					}
					sb.append("\"");
					sb.append(yokoPositionMap.get(new Position(yIndex, xIndex)));
					sb.append("\":[\"");
					sb.append(yokoNumbers[yIndex][xIndex]);
					sb.append("\",6,\"6\"]");
				}
			}
		}
		for (int yIndex = 0; yIndex < tateNumbers.length; yIndex++) {
			for (int xIndex = 0; xIndex < tateNumbers[0].length; xIndex++) {
				if (tateNumbers[yIndex][xIndex] != null) {
					if (sb.length() != 0) {
						sb.append(",");
					}
					sb.append("\"");
					sb.append(tatePositionMap.get(new Position(yIndex, xIndex)));
					sb.append("\":[\"");
					sb.append(tateNumbers[yIndex][xIndex]);
					sb.append("\",6,\"6\"]");
				}
			}
		}
		return convertFieldBefore(size, PuzzleType.MAKEROOM) + "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{" + sb.toString()
				+ "},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n\n"
				+ convertFieldAfter(size);
	}

	/**
	 * サイズを指定してPenpaEditの盤面向け文字列のヒントより手前の内容を返す。
	 */
	private static String convertFieldBefore(int fieldSize, PuzzleType type) {
		StringBuilder sb = new StringBuilder();
		sb.append("square,"); // 正方形
		if (type == PuzzleType.HINTS_BW) {
			fieldSize++;
		}
		sb.append(fieldSize + "," + fieldSize + ","); // 横縦の長さ
		sb.append("38,0,1,1,"); // 表示サイズ,theta、reflect。とりあえず固定値
		int canvasSize = 38 * (fieldSize + 1);
		sb.append(canvasSize + "," + canvasSize + ","); // キャンバスサイズ
		int centerBase = (fieldSize + 1) / 2;
		boolean isEven = fieldSize % 2 == 0;
		int center = isEven ? (6 * centerBase * centerBase + 23 * centerBase + 21)
				: 2 * (centerBase * centerBase + 3 * centerBase + 2);
		sb.append(center + "," + center); // センター座標
		sb.append("\n"); // ここまで1行目
		if (type == PuzzleType.HINTS_BW) {
			sb.append("[1,0,1,0]"); // 余白情報
		} else {
			sb.append("[0,0,0,0]"); // 余白情報
		}
		sb.append("\n"); // ここまで2行目
		sb.append("[\"1\",\"2\",\"1\"]"); // 実線・点線・クリック時の情報。とりあえず固定値
		if (type == PuzzleType.YAJILIN) {
			sb.append("~\"combi\"~[\"yajilin\",\"\"]");
		} else if (type == PuzzleType.MAKEROOM) {
			sb.append("~zE~[\"1\",3]");
		} else {
			sb.append("~zS~[\"\",1]");
		}
		sb.append("\n"); // ここまで3行目
		return sb.toString();
	}

	/**
	 * サイズを指定してPenpaEditの盤面向け文字列のヒントより後の内容を返す。
	 */
	private static String convertFieldAfter(int fieldSize) {
		StringBuilder sb = new StringBuilder();
		int firstPos = 2 * (fieldSize + 5);
		for (int yIndex = 0; yIndex < fieldSize; yIndex++) {
			if (sb.length() == 0) {
				sb.append(firstPos);
			} else {
				sb.append("," + 5);
			}
			for (int xIndex = 0; xIndex < fieldSize - 1; xIndex++) {
				sb.append(",");
				sb.append("1");
			}
		}
		return "[" + sb.toString() + "]";
	}

	/**
	 * FIXME ↑と統合したいな
	 */
	private static String convertFieldAfterHintsBw(int fieldSize) {
		StringBuilder sb = new StringBuilder();
		int firstPos = 3 * fieldSize + 18;
		for (int yIndex = 0; yIndex < fieldSize; yIndex++) {
			if (sb.length() == 0) {
				sb.append(firstPos);
			} else {
				sb.append("," + 6);
			}
			for (int xIndex = 0; xIndex < fieldSize - 1; xIndex++) {
				sb.append(",");
				sb.append("1");
			}
		}
		return "[" + sb.toString() + "]";
	}

	/**
	 * 黒マスの情報をPenpaEditの回答情報向け文字列に変換して返す。 Masuは正方形である前提
	 */
	public static String convertSolutionMasu(Masu[][] masu) {
		List<String> indexStrList = new ArrayList<>();
		int firstPos = 2 * (masu.length + 5);
		for (int yIndex = 0; yIndex < masu.length; yIndex++) {
			for (int xIndex = 0; xIndex < masu[0].length; xIndex++) {
				if (masu[yIndex][xIndex] == Masu.BLACK) {
					indexStrList.add(String.valueOf(firstPos + yIndex * (masu[0].length + 4) + xIndex));
				}
			}
		}
		// 文字列ソートがいるっぽい
		Collections.sort(indexStrList);
		StringBuilder sb = new StringBuilder();
		for (String indexStr : indexStrList) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			sb.append("\"");
			sb.append(indexStr);
			sb.append("\"");
		}
		return "[[" + sb.toString() + "],[],[],[],[],[]]";
	}

	/**
	 * FIXME ↑と統合したいな
	 */
	public static String convertSolutionMasuHintsBw(Masu[][] masu) {
		List<String> indexStrList = new ArrayList<>();
		int firstPos = 2 * (masu.length + 1 + 5);
		for (int yIndex = 1; yIndex < masu.length + 1; yIndex++) {
			for (int xIndex = 1; xIndex < masu[0].length + 1; xIndex++) {
				if (masu[yIndex - 1][xIndex - 1] == Masu.BLACK) {
					indexStrList.add(String.valueOf(firstPos + yIndex * (masu[0].length + 5) + xIndex));
				}
			}
		}
		// 文字列ソートがいるっぽい
		Collections.sort(indexStrList);
		StringBuilder sb = new StringBuilder();
		for (String indexStr : indexStrList) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			sb.append("\"");
			sb.append(indexStr);
			sb.append("\"");
		}
		return "[[" + sb.toString() + "],[],[],[],[],[]]";
	}

	/**
	 * ヤジリン系(ループ+黒マス)のPenpaEditの回答情報向け文字列に変換して返す。 sizeは正方形である前提
	 */
	public static String convertSolutionYajilin(int size, Map<Position, Pipemasu> pipeMap, Set<Position> firstPosSet) {
		List<String> indexStrList = new ArrayList<>();
		List<String> pipeStrList = new ArrayList<>();
		int firstPos = 2 * (size + 5);
		for (int yIndex = 0; yIndex < size; yIndex++) {
			for (int xIndex = 0; xIndex < size; xIndex++) {
				Position pos = new Position(yIndex, xIndex);
				Pipemasu pipe = pipeMap.get(pos);
				if (pipe != null) {
					int myIndex = firstPos + yIndex * (size + 4) + xIndex;
					if (pipe == Pipemasu.UP_RIGHT || pipe == Pipemasu.RIGHT_DOWN || pipe == Pipemasu.RIGHT_LEFT) {
						// 右伸び確定
						if (firstPosSet.contains(pos) && firstPosSet.contains(new Position(yIndex, xIndex + 1))) {
							// 伸びる先がいずれも表出マスであれば回答盤面から除外
						} else {
							int rightIndex = myIndex + 1;
							String pipeStr = myIndex + "," + rightIndex + ",1";
							if (!pipeStrList.contains(pipeStr)) {
								pipeStrList.add(pipeStr);
							}
						}
					}
					if (pipe == Pipemasu.UP_DOWN || pipe == Pipemasu.RIGHT_DOWN || pipe == Pipemasu.DOWN_LEFT) {
						// 下伸び確定
						if (firstPosSet.contains(pos) && firstPosSet.contains(new Position(yIndex + 1, xIndex))) {
							// 伸びる先がいずれも表出マスであれば回答盤面から除外
						} else {
							int downIndex = myIndex + size + 4;
							String pipeStr = myIndex + "," + downIndex + ",1";
							if (!pipeStrList.contains(pipeStr)) {
								pipeStrList.add(pipeStr);
							}
						}
					}
					// 黒マスを回答要件とする
					if (pipe == Pipemasu.BLACK) {
						indexStrList.add(String.valueOf(firstPos + yIndex * (size + 4) + xIndex));
					}
				}
			}
		}
		// 文字列ソートがいるっぽい
		Collections.sort(indexStrList);
		Collections.sort(pipeStrList);
		StringBuilder masuSb = new StringBuilder();
		StringBuilder pipeSb = new StringBuilder();
		for (String indexStr : indexStrList) {
			if (masuSb.length() != 0) {
				masuSb.append(",");
			}
			masuSb.append("\"");
			masuSb.append(indexStr);
			masuSb.append("\"");
		}
		for (String pipeStr : pipeStrList) {
			if (pipeSb.length() != 0) {
				pipeSb.append(",");
			}
			pipeSb.append("\"");
			pipeSb.append(pipeStr);
			pipeSb.append("\"");
		}
		return "[[" + masuSb.toString() + "],[" + pipeSb.toString() + "],[],[],[],[]]";
	}

	/**
	 * 領域分割系のPenpaEditの回答情報向け文字列に変換して返す。盤面は正方形が前提
	 */
	public static String convertSolutionRoom(int size, Wall[][] yokoWall, Wall[][] tateWall) {
		List<String> indexStrList = new ArrayList<>();
		int idx = (size * size) + (9 * size) + 22;
		for (int yIndex = 0; yIndex < size; yIndex++) {
			for (int xIndex = 0; xIndex < size - 1; xIndex++) {
				if (yokoWall[yIndex][xIndex] == Wall.EXISTS) {
					indexStrList.add(idx + "," + (idx + size + 4) + ",1");
				}
				idx++;
			}
			idx = idx + 5;
		}
		idx = (size + 5) * (size + 5);
		for (int yIndex = 0; yIndex < size - 1; yIndex++) {
			for (int xIndex = 0; xIndex < size; xIndex++) {
				if (tateWall[yIndex][xIndex] == Wall.EXISTS) {
					indexStrList.add(idx + "," + (idx + 1) + ",1");
				}
				idx++;
			}
			idx = idx + 4;
		}
		// 文字列ソートがいるっぽい
		Collections.sort(indexStrList);
		StringBuilder sb = new StringBuilder();
		for (String indexStr : indexStrList) {
			if (sb.length() != 0) {
				sb.append(",");
			}
			sb.append("\"");
			sb.append(indexStr);
			sb.append("\"");
		}
		return "[[],[],[" + sb.toString() + "],[],[],[]]";

	}

	/**
	 * fieldStrからmasuを復元。
	 */
	public static Masu[][] getMasu(String fieldStr) {
		String[] fieldInfo = fieldStr.split("\n")[0].split(",");
		Integer yLength = Integer.valueOf(fieldInfo[2]);
		Integer xLength = Integer.valueOf(fieldInfo[1]);
		Masu[][] result = new Masu[yLength][xLength];
		for (int yIndex = 0; yIndex < result.length; yIndex++) {
			for (int xIndex = 0; xIndex < result[yIndex].length; xIndex++) {
				result[yIndex][xIndex] = Masu.SPACE;
			}
		}
		// 左上のインデックスを確定する処理。横列の長さだけで決まる。
		int firstIndex = 2 * (xLength + 5);
		Map<Integer, Position> positionMap = new HashMap<>();
		int keyIndex = firstIndex;
		for (int yIndex = 0; yIndex < yLength; yIndex++) {
			for (int xIndex = 0; xIndex < xLength; xIndex++) {
				positionMap.put(keyIndex, new Position(yIndex, xIndex));
				keyIndex++;
			}
			keyIndex = keyIndex + 4;
		}

		Map<String, Map<String, List<Object>>> hintLine = JSON.decode(fieldStr.split("\n")[3]);
		Map<String, List<Object>> hintInfo = hintLine.get("zS");
		for (Entry<String, List<Object>> entry : hintInfo.entrySet()) {
			int idx = Integer.parseInt(entry.getKey());
			try {
				int number = ((BigDecimal) entry.getValue()).intValue();
				if (number == 1 || number == 4) {
					// 1=濃灰または4=黒を黒ますとみなす
					Position pos = positionMap.get(idx);
					result[pos.getyIndex()][pos.getxIndex()] = Masu.BLACK;
				}
			} catch (NumberFormatException e) {
				// たまに数字じゃないのが来る時があるようなのでその場合はスキップ
			}
		}
		return result;
	}

	/**
	 * fieldStrからクリーク型数字を復元。
	 */
	public static Integer[][] getExtraNumbers(String fieldStr) {
		String[] fieldInfo = fieldStr.split("\n")[0].split(",");

		Integer yLength = Integer.valueOf(fieldInfo[2]);
		Integer xLength = Integer.valueOf(fieldInfo[1]);
		Integer[][] result = new Integer[yLength + 1][xLength + 1];
		// 左上のインデックスを確定する処理
		int firstIndex = 31;
		firstIndex = firstIndex + (yLength - 1) * 5;
		firstIndex = firstIndex + (xLength - 1) * (5 + yLength);

		Map<Integer, Position> positionMap = new HashMap<>();
		int keyIndex = firstIndex;
		for (int yIndex = 0; yIndex < yLength + 1; yIndex++) {
			for (int xIndex = 0; xIndex < xLength + 1; xIndex++) {
				positionMap.put(keyIndex, new Position(yIndex, xIndex));
				keyIndex++;
			}
			keyIndex = keyIndex + 3;
		}
		Map<String, Map<String, List<Object>>> hintLine = JSON.decode(fieldStr.split("\n")[3]);
		Map<String, List<Object>> hintInfo = hintLine.get("zN");

		for (Entry<String, List<Object>> entry : hintInfo.entrySet()) {
			int idx = Integer.parseInt(entry.getKey());
			Position pos = positionMap.get(idx);
			try {
				int number = Integer.parseInt((String) entry.getValue().get(0));
				result[pos.getyIndex()][pos.getxIndex()] = number;
			} catch (NumberFormatException e) {
				if (entry.getValue().get(0).equals("?")) {
					result[pos.getyIndex()][pos.getxIndex()] = -1;
				}
			}
		}
		return result;
	}

	/**
	 * fieldStrから数字を復元。
	 */
	public static Integer[][] getNumbers(String fieldStr) {
		String[] fieldInfo = fieldStr.split("\n")[0].split(",");
		Integer yLength = Integer.valueOf(fieldInfo[2]);
		Integer xLength = Integer.valueOf(fieldInfo[1]);
		Integer[][] result = new Integer[yLength][xLength];

		// 左上のインデックスを確定する処理。横列の長さだけで決まる。
		int firstIndex = 2 * (xLength + 5);

		Map<Integer, Position> positionMap = new HashMap<>();
		int keyIndex = firstIndex;
		for (int yIndex = 0; yIndex < yLength; yIndex++) {
			for (int xIndex = 0; xIndex < xLength; xIndex++) {
				positionMap.put(keyIndex, new Position(yIndex, xIndex));
				keyIndex++;
			}
			keyIndex = keyIndex + 4;
		}
		Map<String, Map<String, List<Object>>> hintLine = JSON.decode(fieldStr.split("\n")[3]);
		Map<String, List<Object>> hintInfo = hintLine.get("zN");
		for (Entry<String, List<Object>> entry : hintInfo.entrySet()) {
			int idx = Integer.parseInt(entry.getKey());
			Position pos = positionMap.get(idx);
			try {
				int number = Integer.parseInt((String) entry.getValue().get(0));
				result[pos.getyIndex()][pos.getxIndex()] = number;
			} catch (NumberFormatException e) {
				if (entry.getValue().get(0).equals("?")) {
					result[pos.getyIndex()][pos.getxIndex()] = -1;
				}
			}
		}
		return result;
	}

	/**
	 * fieldStrから丸の復元。無=0、○=1、●=2として配列に詰める
	 */
	public static Integer[][] getCircles(String fieldStr) {
		String[] fieldInfo = fieldStr.split("\n")[0].split(",");
		Integer yLength = Integer.valueOf(fieldInfo[2]);
		Integer xLength = Integer.valueOf(fieldInfo[1]);
		Integer[][] result = new Integer[yLength][xLength];
		for (int yIndex = 0; yIndex < result.length; yIndex++) {
			for (int xIndex = 0; xIndex < result[yIndex].length; xIndex++) {
				result[yIndex][xIndex] = 0;
			}
		}
		// 左上のインデックスを確定する処理。横列の長さだけで決まる。
		int firstIndex = 2 * (xLength + 5);

		Map<Integer, Position> positionMap = new HashMap<>();
		int keyIndex = firstIndex;
		for (int yIndex = 0; yIndex < yLength; yIndex++) {
			for (int xIndex = 0; xIndex < xLength; xIndex++) {
				positionMap.put(keyIndex, new Position(yIndex, xIndex));
				keyIndex++;
			}
			keyIndex = keyIndex + 4;
		}
		Map<String, Map<String, List<Object>>> hintLine = JSON.decode(fieldStr.split("\n")[3]);
		Map<String, List<Object>> hintInfo = hintLine.get("zY");
		for (Entry<String, List<Object>> entry : hintInfo.entrySet()) {
			int idx = Integer.parseInt(entry.getKey());
			Position pos = positionMap.get(idx);
			int number = ((BigDecimal) entry.getValue().get(0)).intValue();
			String shape = (String) entry.getValue().get(1);
			// circle_l、circle_mなどの種別があるようだが、circleが含まれていればとりあえず丸として認識。
			if (shape.contains("circle")) {
				if (number == 1 || number == 8) {
					// 1と8は白丸認識。画面だと微妙に丸の太さが違うがどっちも同じとする
					result[pos.getyIndex()][pos.getxIndex()] = 1;
				} else if (number == 2) {
					result[pos.getyIndex()][pos.getxIndex()] = 2;
				}
			}
		}
		return result;
	}

	/**
	 * fieldStrから領域系盤面の復元。
	 */
	public static RoomWalls getRoomWalls(String fieldStr) {
		String[] fieldInfo = fieldStr.split("\n")[0].split(",");

		Integer yLength = Integer.valueOf(fieldInfo[2]);
		Integer xLength = Integer.valueOf(fieldInfo[1]);
		boolean[][] yokoRoomWall = new boolean[yLength][xLength - 1];
		boolean[][] tateRoomWall = new boolean[yLength - 1][xLength];

		// 左上のインデックスを確定する処理
		int firstIndex = 31;
		firstIndex = firstIndex + (yLength - 1) * 5;
		firstIndex = firstIndex + (xLength - 1) * (5 + yLength);

		Map<Integer, Position> positionMap = new HashMap<>();
		int keyIndex = firstIndex;
		for (int yIndex = 0; yIndex < yLength + 1; yIndex++) {
			for (int xIndex = 0; xIndex < xLength + 1; xIndex++) {
				positionMap.put(keyIndex, new Position(yIndex, xIndex));
				keyIndex++;
			}
			keyIndex = keyIndex + 3;
		}

		Map<String, Map<String, List<Object>>> hintLine = JSON.decode(fieldStr.split("\n")[3]);
		Map<String, List<Object>> hintInfo = hintLine.get("zE");

		for (Entry<String, List<Object>> entry : hintInfo.entrySet()) {
			try {
				String[] posPair = ((String) entry.getKey()).split(",");
				Position posFrom = positionMap.get(Integer.valueOf(posPair[0]));
				Position posTo = positionMap.get(Integer.valueOf(posPair[1]));
				if (posFrom.getyIndex() == posTo.getyIndex()) {
					tateRoomWall[posFrom.getyIndex() - 1][posFrom.getxIndex()] = true;
				}
				if (posFrom.getxIndex() == posTo.getxIndex()) {
					yokoRoomWall[posFrom.getyIndex()][posFrom.getxIndex() - 1] = true;
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				// 作問時に線の引きすぎでOOB発生がありうるので無視
			}
		}
		return new RoomWalls(yokoRoomWall, tateRoomWall);
	}

	/**
	 * fieldStrから和フスマヒント数字を復元。
	 */
	public static TateYokoNumbers getTateYokoNumbers(String fieldStr) {
		String[] fieldInfo = fieldStr.split("\n")[0].split(",");

		Integer yLength = Integer.valueOf(fieldInfo[2]);
		Integer xLength = Integer.valueOf(fieldInfo[1]);
		Integer[][] yokoNumbers = new Integer[yLength][xLength - 1];
		Integer[][] tateNumbers = new Integer[yLength - 1][xLength];

		// 左上のインデックスを確定する処理
		int yokoFirstIndex = 58;
		yokoFirstIndex = yokoFirstIndex + (12 * yLength);
		yokoFirstIndex = yokoFirstIndex + (14 + yLength * 3) * xLength;
		Map<Integer, Position> yokoPositionMap = new HashMap<>();
		int yokoKeyIndex = yokoFirstIndex;
		for (int yIndex = 0; yIndex < yLength; yIndex++) {
			for (int xIndex = 0; xIndex < xLength - 1; xIndex++) {
				yokoPositionMap.put(yokoKeyIndex, new Position(yIndex, xIndex));
				yokoKeyIndex++;
			}
			yokoKeyIndex = yokoKeyIndex + 5;
		}
		int tateFirstIndex = 42;
		tateFirstIndex = tateFirstIndex + (8 * yLength);
		tateFirstIndex = tateFirstIndex + (10 + yLength * 2) * xLength;
		Map<Integer, Position> tatePositionMap = new HashMap<>();
		int tateKeyIndex = tateFirstIndex;
		for (int yIndex = 0; yIndex < yLength - 1; yIndex++) {
			for (int xIndex = 0; xIndex < xLength; xIndex++) {
				tatePositionMap.put(tateKeyIndex, new Position(yIndex, xIndex));
				tateKeyIndex++;
			}
			tateKeyIndex = tateKeyIndex + 4;
		}
		Map<String, Map<String, List<Object>>> hintLine = JSON.decode(fieldStr.split("\n")[3]);
		Map<String, List<Object>> hintInfo = hintLine.get("zN");
		for (Entry<String, List<Object>> entry : hintInfo.entrySet()) {
			try {
				int number = Integer.parseInt((String) entry.getValue().get(0));
				int idx = Integer.parseInt(entry.getKey());
				Position pos = yokoPositionMap.get(idx);
				if (pos != null) {
					yokoNumbers[pos.getyIndex()][pos.getxIndex()] = number;
				} else {
					pos = tatePositionMap.get(idx);
					if (pos != null) {
						tateNumbers[pos.getyIndex()][pos.getxIndex()] = number;
					}
				}
			} catch (NumberFormatException e) {
				// ?を考慮するなら、実装が必要だが、今は握りつぶしている。
			}
		}
		return new TateYokoNumbers(yokoNumbers, tateNumbers);
	}

	/**
	 * fieldStrからパイプリンク系盤面を復元。 crossを使っている場合のみ読み取れる。
	 */
	public static PipelinkWalls getPipelinkWalls(String fieldStr) {
		String[] fieldInfo = fieldStr.split("\n")[0].split(",");

		Integer yLength = Integer.valueOf(fieldInfo[2]);
		Integer xLength = Integer.valueOf(fieldInfo[1]);
		Wall[][] yokoWall = new Wall[yLength][xLength - 1];
		Wall[][] tateWall = new Wall[yLength - 1][xLength];
		for (int yIndex = 0; yIndex < yLength; yIndex++) {
			for (int xIndex = 0; xIndex < xLength - 1; xIndex++) {
				yokoWall[yIndex][xIndex] = Wall.SPACE;
			}
		}
		for (int yIndex = 0; yIndex < yLength - 1; yIndex++) {
			for (int xIndex = 0; xIndex < xLength; xIndex++) {
				tateWall[yIndex][xIndex] = Wall.SPACE;
			}
		}

		// 左上のインデックスを確定する処理。横列の長さだけで決まる。
		int firstIndex = 2 * (xLength + 5);

		Map<Integer, Position> positionMap = new HashMap<>();
		int keyIndex = firstIndex;
		for (int yIndex = 0; yIndex < yLength; yIndex++) {
			for (int xIndex = 0; xIndex < xLength; xIndex++) {
				positionMap.put(keyIndex, new Position(yIndex, xIndex));
				keyIndex++;
			}
			keyIndex = keyIndex + 4;
		}
		Set<Position> firstPosSet = new HashSet<>();

		Map<String, Map<String, List<Object>>> hintLine = JSON.decode(fieldStr.split("\n")[3]);
		Map<String, List<Object>> hintInfo = hintLine.get("zY");

		for (Entry<String, List<Object>> entry : hintInfo.entrySet()) {
			int idx = Integer.parseInt(entry.getKey());
			try {
				Position pos = positionMap.get(idx);
				@SuppressWarnings("unchecked")
				List<BigDecimal> wallBase = (List<BigDecimal>) entry.getValue().get(0);
				Wall wallRight = wallBase.get(0).intValue() == 1 ? Wall.NOT_EXISTS : Wall.EXISTS;
				Wall wallDown = wallBase.get(1).intValue() == 1 ? Wall.NOT_EXISTS : Wall.EXISTS;
				Wall wallLeft = wallBase.get(2).intValue() == 1 ? Wall.NOT_EXISTS : Wall.EXISTS;
				Wall wallUp = wallBase.get(3).intValue() == 1 ? Wall.NOT_EXISTS : Wall.EXISTS;
				if (pos.getyIndex() != 0) {
					tateWall[pos.getyIndex() - 1][pos.getxIndex()] = wallUp;
				}
				if (pos.getxIndex() != xLength - 1) {
					yokoWall[pos.getyIndex()][pos.getxIndex()] = wallRight;
				}
				if (pos.getyIndex() != yLength - 1) {
					tateWall[pos.getyIndex()][pos.getxIndex()] = wallDown;
				}
				if (pos.getxIndex() != 0) {
					yokoWall[pos.getyIndex()][pos.getxIndex() - 1] = wallLeft;
				}
				firstPosSet.add(pos);
			} catch (NullPointerException e) {
			}
		}
		return new PipelinkWalls(yokoWall, tateWall, firstPosSet);
	}

	/**
	 * fieldStrからラインダース向けのライン情報を復元。
	 */
	public static List<Set<Position>> getLines(String fieldStr) {
		List<Set<Position>> result = new ArrayList<>();
		String[] fieldInfo = fieldStr.split("\n")[0].split(",");
		Integer yLength = Integer.valueOf(fieldInfo[2]);
		Integer xLength = Integer.valueOf(fieldInfo[1]);
		// 左上のインデックスを確定する処理。横列の長さだけで決まる。
		int firstIndex = 2 * (xLength + 5);
		Map<Integer, Position> positionMap = new HashMap<>();
		int keyIndex = firstIndex;
		for (int yIndex = 0; yIndex < yLength; yIndex++) {
			for (int xIndex = 0; xIndex < xLength; xIndex++) {
				positionMap.put(keyIndex, new Position(yIndex, xIndex));
				keyIndex++;
			}
			keyIndex = keyIndex + 4;
		}
		Map<String, Map<String, List<Object>>> hintLine = JSON.decode(fieldStr.split("\n")[3]);
		Map<String, List<Object>> hintInfo = hintLine.get("zL");
		class x {
			// posを起点にposMapに含まれる位置を順次continuePosSetに追加する。
			public void setContinuePosSet(Map<Position, Set<Position>> posMap, Position pos,
					Set<Position> continuePosSet) {
				for (Entry<Position, Set<Position>> entry : posMap.entrySet()) {
					if (entry.getKey().equals(pos)) {
						for (Position posTo : entry.getValue()) {
							if (!continuePosSet.contains(posTo)) {
								continuePosSet.add(posTo);
								setContinuePosSet(posMap, posTo, continuePosSet);
							}
						}
					} else if (entry.getValue().contains(pos)) {
						if (!continuePosSet.contains(entry.getKey())) {
							continuePosSet.add(entry.getKey());
							setContinuePosSet(posMap, entry.getKey(), continuePosSet);
						}
					}
				}
			}
		}
		Map<Position, Set<Position>> posMap = new HashMap<>();
		for (Entry<String, List<Object>> entry : hintInfo.entrySet()) {
			try {
				String[] posPair = ((String) entry.getKey()).split(",");
				Position posFrom = positionMap.get(Integer.valueOf(posPair[0]));
				Position posTo = positionMap.get(Integer.valueOf(posPair[1]));
				Set<Position> posToSet = posMap.get(posFrom);
				if (posToSet == null) {
					posToSet = new HashSet<>();
					posMap.put(posFrom, posToSet);
				}
				posToSet.add(posTo);
			} catch (ArrayIndexOutOfBoundsException e) {
				// 作問時に線の引きすぎでOOB発生がありうるので無視
			}
		}
		for (Entry<Position, Set<Position>> entry : posMap.entrySet()) {
			Position pos = entry.getKey();
			boolean alreadyLined = false;
			for (Set<Position> line : result) {
				if (line.contains(pos)) {
					alreadyLined = true;
					break;
				}
			}
			if (!alreadyLined) {
				Set<Position> continuePosSet = new HashSet<>();
				continuePosSet.add(pos);
				new x().setContinuePosSet(posMap, pos, continuePosSet);
				result.add(continuePosSet);
			}
		}

		return result;

	}

	public static UpLeftHints getUpLeftHints(String fieldStr) {
		String[] fieldInfo = fieldStr.split("\n")[0].split(",");
		Integer yLength = Integer.valueOf(fieldInfo[2]);
		Integer xLength = Integer.valueOf(fieldInfo[1]);
		Integer[] upHints = new Integer[xLength - 1];
		Integer[] leftHints = new Integer[yLength - 1];
		// 左上のインデックスを確定する処理。横列の長さだけで決まる。
		int firstIndex = 2 * (xLength + 5);

		Map<Integer, Position> positionMap = new HashMap<>();
		int keyIndex = firstIndex;
		for (int yIndex = 0; yIndex < yLength; yIndex++) {
			for (int xIndex = 0; xIndex < xLength; xIndex++) {
				positionMap.put(keyIndex, new Position(yIndex, xIndex));
				keyIndex++;
			}
			keyIndex = keyIndex + 4;
		}
		Map<String, Map<String, List<Object>>> hintLine = JSON.decode(fieldStr.split("\n")[3]);
		Map<String, List<Object>> hintInfo = hintLine.get("zN");
		for (Entry<String, List<Object>> entry : hintInfo.entrySet()) {
			int idx = Integer.parseInt(entry.getKey());
			Position pos = positionMap.get(idx);
			int number = Integer.parseInt((String) entry.getValue().get(0));
			if (pos.getxIndex() == 0) {
				leftHints[pos.getyIndex() - 1] = number;
			}
			if (pos.getyIndex() == 0) {
				upHints[pos.getxIndex() - 1] = number;
			}
		}
		return new UpLeftHints(upHints, leftHints);
	}

}

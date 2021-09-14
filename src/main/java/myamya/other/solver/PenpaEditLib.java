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
import myamya.other.solver.Common.Position;
import myamya.other.solver.Common.Wall;
import net.arnx.jsonic.JSON;

public class PenpaEditLib {

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
	 * 数字オンリー盤面のPenpaEdit向け文字列を返す。numbersは正方形である前提で、？は未対応 TODO まだ検証していません
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
		return convertFieldBefore(fieldSize) + "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{" + sb.toString()
				+ "},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n\n"
				+ convertFieldAfter(fieldSize);
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
		return convertFieldBefore(fieldSize) + "{zR:{z_:[]},zU:{z_:[]},zS:{" + masuSb.toString() + "},zN:{"
				+ numberSb.toString()
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
		return convertFieldBefore(fieldSize) + "{zR:{z_:[]},zU:{z_:[]},zS:{},zN:{" + sb.toString()
				+ "},z1:{},zY:{},zF:{},z2:{},zT:[],z3:[],zD:[],z0:[],z5:[],zL:{},zE:{},zW:{},zC:{},z4:{}}\n\n"
				+ convertFieldAfter(fieldSize);
	}

	/**
	 * サイズを指定してPenpaEditの盤面向け文字列のヒントより手前の内容を返す。
	 */
	private static String convertFieldBefore(int fieldSize) {
		StringBuilder sb = new StringBuilder();
		sb.append("square,"); // 正方形
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
		sb.append("[0,0,0,0]"); // 余白情報
		sb.append("\n"); // ここまで2行目
		sb.append("[\"1\",\"2\",\"1\"]~zS~[\"\",1]"); // 実線・点線・クリック時の色などの情報。とりあえず固定値
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
	 * fieldStrからパイプリンク系盤面を復元。
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

}

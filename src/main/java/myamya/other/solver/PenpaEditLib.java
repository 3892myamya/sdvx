package myamya.other.solver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import myamya.other.solver.Common.Masu;

public class PenpaEditLib {

	public static final String PENPA_EDIT_DUMMY_URL = "penpa-edit-dummy-url";

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
		sb.append(fieldSize + "," + fieldSize + ","); // 縦横の長さ
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
		sb.append("[\"1\",\"2\",\"1\"]~zS~[\"\",1]"); // 実線・点線などの情報。とりあえず固定値
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

}

package myamya.other.solver;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import myamya.other.solver.Common.AkariBattleResult;
import myamya.other.solver.akari.AkariSolver.AkariSolverForBattle;
import net.arnx.jsonic.JSON;

@WebServlet("/AkariBattle")
public class AkariBattleWeb extends HttpServlet {
	private static final String HALF_NUMS = "0 1 2 3 4 5 6 7 8 9";
	private static final String FULL_NUMS = "０１２３４５６７８９";

	static class AkariBattleThlead extends Thread {
		private AkariBattleResult result = new AkariBattleResult("申し訳ございません。内部エラーが発生しました。バトルを終了します。");
		protected final String fieldStr;
		protected final int height;
		protected final int width;

		AkariBattleThlead(String fieldStr, int height, int width) {
			this.fieldStr = fieldStr;
			this.height = height;
			this.width = width;
		}

		@Override
		public void run() {
			result = new AkariSolverForBattle(fieldStr, height, width).solve3();
		}

	}

	static final Map<Integer, String> AKARI_BATTLE_MAP = new HashMap<>();
	static final String INIT_FIELD_STRING = "000000000000000000000000000000000000";
	static final int SIZE = 6;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/javascript; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		Map<String, Object> resultMap = new HashMap<>();
		try {
			int div = Integer.parseInt(request.getParameter("div"));
			if (div == 0) {
				// ゲーム開始モード
				while (true) {
					int id = (int) (Math.random() * 10000000);
					if (!AKARI_BATTLE_MAP.containsKey(id)) {
						AKARI_BATTLE_MAP.put(id, INIT_FIELD_STRING);
						break;
					}
				}
				resultMap.put("result", toResultString(INIT_FIELD_STRING));
			} else {
				int id = Integer.parseInt(request.getParameter("id"));
				if (div == 1) {
					// 自分の手番の場合
					// 自分の打った手をもとに盤面を更新して返す
					AkariBattleThlead t;
					String fieldStr = request.getParameter("field");
					int y = Integer.parseInt(request.getParameter("y"));
					int x = Integer.parseInt(request.getParameter("x"));
					t = new AkariBattleThlead(fieldStr, SIZE, SIZE);
					t.start();
					t.join(28000);
					resultMap.put("status", t.result.getStatus());
					resultMap.put("field", t.result.getFieldStr());
					resultMap.put("result", t.result.getFieldStr());
					resultMap.put("end", t.result.isEnd());
					AKARI_BATTLE_MAP.put(id, fieldStr);
				} else if (div == 2) {
					// 相手の手番の場合
					// 相手の手が進んだかをチェックする。
					resultMap.put("field", AKARI_BATTLE_MAP.get(id));
					resultMap.put("result", toResultString(AKARI_BATTLE_MAP.get(id)));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("status", "予期せぬエラーが発生しました。");
			resultMap.put("result", "");
		}
		try (PrintWriter out = response.getWriter()) {
			out.print(JSON.encode(resultMap));
		}
	}

	/**
	 * fieldStringをキャンバスに変換
	 */
	private String toResultString(String fieldStr) {
		StringBuilder sb = new StringBuilder();
		int baseSize = 30;
		int margin = 5;
		sb.append(
				"<svg xmlns=\"http://www.w3.org/2000/svg\" "
						+ "height=\"" + (SIZE * baseSize + 2 * baseSize + margin) + "\" width=\""
						+ (SIZE * baseSize + 2 * baseSize) + "\" >");
		for (int i = 0; i < fieldStr.length(); i++) {
			int fieldInt = Character.getNumericValue(fieldStr.charAt(i));
			int yIndex = i / SIZE;
			int xIndex = i % SIZE;
			if (fieldInt >= 3) {
				sb.append("<rect y=\"" + (yIndex * baseSize + margin)
						+ "\" x=\""
						+ (xIndex * baseSize + baseSize)
						+ "\" width=\""
						+ (baseSize)
						+ "\" height=\""
						+ (baseSize)
						+ "\">"
						+ "</rect>");
				if (fieldInt >= 4) {
					String numberStr = String.valueOf(fieldInt - 4);
					int index = HALF_NUMS.indexOf(numberStr);
					String masuStr = null;
					if (index >= 0) {
						masuStr = FULL_NUMS.substring(index / 2, index / 2 + 1);
					} else {
						masuStr = numberStr;
					}
					sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 5)
							+ "\" x=\""
							+ (xIndex * baseSize + baseSize + 2)
							+ "\" fill=\""
							+ "white"
							+ "\" font-size=\""
							+ (baseSize - 5)
							+ "\" textLength=\""
							+ (baseSize - 5)
							+ "\" lengthAdjust=\"spacingAndGlyphs\">"
							+ masuStr
							+ "</text>");
				}
			} else if (fieldInt == 2) {
				sb.append("<circle cy=\"" + (yIndex * baseSize + (baseSize / 2) + margin)
						+ "\" cx=\""
						+ (xIndex * baseSize + baseSize + (baseSize / 2))
						+ "\" r=\""
						+ (baseSize / 2 - 2)
						+ "\" fill=\"white\", stroke=\"black\">"
						+ "</circle>");
			} else if (fieldInt == 1) {
				sb.append("<text y=\"" + (yIndex * baseSize + baseSize + margin - 4)
						+ "\" x=\""
						+ (xIndex * baseSize + baseSize)
						+ "\" font-size=\""
						+ (baseSize - 2)
						+ "\" textLength=\""
						+ (baseSize - 2)
						+ "\" lengthAdjust=\"spacingAndGlyphs\">"
						+ "・"
						+ "</text>");
			}
		}
		// 横壁描画
		for (int yIndex = 0; yIndex < SIZE; yIndex++) {
			for (int xIndex = -1; xIndex < SIZE; xIndex++) {
				boolean oneYokoWall = xIndex == -1 || xIndex == SIZE - 1;
				sb.append("<line y1=\""
						+ (yIndex * baseSize + margin)
						+ "\" x1=\""
						+ (xIndex * baseSize + 2 * baseSize)
						+ "\" y2=\""
						+ (yIndex * baseSize + baseSize + margin)
						+ "\" x2=\""
						+ (xIndex * baseSize + 2 * baseSize)
						+ "\" stroke-width=\"1\" fill=\"none\"");
				if (oneYokoWall) {
					sb.append("stroke=\"#000\" ");
				} else {
					sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
				}
				sb.append(">"
						+ "</line>");
			}
		}
		// 縦壁描画
		for (int yIndex = -1; yIndex < SIZE; yIndex++) {
			for (int xIndex = 0; xIndex < SIZE; xIndex++) {
				boolean oneTateWall = yIndex == -1 || yIndex == SIZE - 1;
				sb.append("<line y1=\""
						+ (yIndex * baseSize + baseSize + margin)
						+ "\" x1=\""
						+ (xIndex * baseSize + baseSize)
						+ "\" y2=\""
						+ (yIndex * baseSize + baseSize + margin)
						+ "\" x2=\""
						+ (xIndex * baseSize + baseSize + baseSize)
						+ "\" stroke-width=\"1\" fill=\"none\"");
				if (oneTateWall) {
					sb.append("stroke=\"#000\" ");
				} else {
					sb.append("stroke=\"#AAA\" stroke-dasharray=\"2\" ");
				}
				sb.append(">"
						+ "</line>");
			}
		}
		// 当たり判定パネル
		for (int i = 0; i < fieldStr.length(); i++) {
			int yIndex = i / SIZE;
			int xIndex = i % SIZE;
			sb.append("<rect y=\"" + (yIndex * baseSize + margin)
					+ "\" x=\""
					+ (xIndex * baseSize + baseSize)
					+ "\" fill=\""
					+ "none"
					+ "\" class=\""
					+ "canclick"
					+ "\" yIndex=\""
					+ yIndex
					+ "\" xIndex=\""
					+ xIndex
					+ "\" width=\""
					+ (baseSize)
					+ "\" height=\""
					+ (baseSize)
					+ "\">"
					+ "</rect>");
		}
		sb.append("</svg>");
		return sb.toString();
	}

}
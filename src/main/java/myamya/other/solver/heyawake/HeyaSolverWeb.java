package myamya.other.solver.heyawake;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.arnx.jsonic.JSON;

@WebServlet("/HeyaWeb")
public class HeyaSolverWeb extends HttpServlet {

	static class HeyaSolverThlead extends Thread {
		protected final HeyaSolver solver;
		private String res = "";
		private String status = "申し訳ございません。時間内に解析が完了しませんでした。";

		HeyaSolverThlead(int height, int width, int cnt, boolean up, boolean right, boolean down,
				boolean left, int limit) {
			solver = new HeyaSolver(height, width, cnt, up, right, down, left, 1000);
		}

		@Override
		public void run() {
			List<HeyaSolver> result = solver.solve();
			StringBuilder sb = new StringBuilder();
			for (HeyaSolver field : result) {
				sb.append(field.toString());
				sb.append(System.lineSeparator());
			}
			res = sb.toString();
			status = result.size() + "通り" + (result.size() == 1000 ? "以上" : "")
					+ "が見つかりました。" + (result.size() == 1000 ? "(最初の1000通りを表示しています。)" : "");
		}

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/javascript; charset=utf-8");
		Map<String, Object> resultMap = new HashMap<>();
		try {
			int height = Integer.parseInt(request.getParameter("height"));
			int width = Integer.parseInt(request.getParameter("width"));
			int cnt = Integer.parseInt(request.getParameter("cnt"));
			boolean up = new Boolean(request.getParameter("up")).booleanValue();
			boolean right = new Boolean(request.getParameter("right")).booleanValue();
			boolean down = new Boolean(request.getParameter("down")).booleanValue();
			boolean left = new Boolean(request.getParameter("left")).booleanValue();
			HeyaSolverThlead t = new HeyaSolverThlead(height, width, cnt, up, right, down, left, 1000);
			t.start();
			t.join(28000);
			resultMap.put("result", t.res);
			resultMap.put("status", t.status);
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("result", "");
			resultMap.put("status", "予期せぬエラーが発生しました。");
		}
		try (PrintWriter out = response.getWriter()) {
			out.print(JSON.encode(resultMap));
		}
	}

}

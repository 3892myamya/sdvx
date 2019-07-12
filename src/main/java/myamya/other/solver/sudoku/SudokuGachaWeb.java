package myamya.other.solver.sudoku;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import myamya.other.solver.sudoku.SudokuSolver.SudokuGenerator;
import net.arnx.jsonic.JSON;

@WebServlet("/SudokuGacha")
public class SudokuGachaWeb extends HttpServlet {

	static class SudokuGeneratorResult {

		private final String status;
		private final String result;
		private final String link;
		private final String url;
		private final int level;
		private final String txt;

		public SudokuGeneratorResult(String status) {
			this.status = status;
			result = "";
			link = "";
			url = "";
			level = 0;
			txt = "";
		}

		public SudokuGeneratorResult(String status, String result, String link, String url, int level,String txt) {
			this.status = status;
			this.result = result;
			this.link = link;
			this.url = url;
			this.level = level;
			this.txt = txt;
		}

		public String getStatus() {
			return status;
		}

		public String getResult() {
			return result;
		}

		public String getLink() {
			return link;
		}

		public String getUrl() {
			return url;
		}

		public int getLevel() {
			return level;
		}

		public String getTxt() {
			return txt;
		}

	}

	static class SudokuGeneratorThlead extends Thread {
		protected final SudokuGenerator generator;
		private SudokuGeneratorResult result = new SudokuGeneratorResult(
				"混雑しているようです。申し訳ありませんが時間をおいてお試しください。");

		SudokuGeneratorThlead(int pattern) {
			generator = new SudokuGenerator(pattern);
		}

		@Override
		public void run() {
			result = generator.generate();
		}

	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		response.setContentType("text/javascript; charset=utf-8");
		Map<String, Object> resultMap = new HashMap<>();
		try {
			int pattern = Integer.parseInt(request.getParameter("pattern"));
			SudokuGeneratorThlead t = new SudokuGeneratorThlead(pattern);
			t.start();
			t.join(28000);
			resultMap.put("status", t.result.getStatus());
			resultMap.put("result", t.result.getResult());
			resultMap.put("link", t.result.getLink());
			resultMap.put("url", t.result.getUrl());
			resultMap.put("txt", t.result.getTxt());
			resultMap.put("level", t.result.getLevel());
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("status", "予期せぬエラーが発生しました。");
			resultMap.put("result", "");
		}
		try (PrintWriter out = response.getWriter()) {
			out.print(JSON.encode(resultMap));
		}
	}

}
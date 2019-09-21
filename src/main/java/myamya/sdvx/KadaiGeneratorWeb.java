package myamya.sdvx;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import myamya.sdvx.KadaiGeneratorClasses.EstimateInfo;
import myamya.sdvx.KadaiGeneratorClasses.ResponseInfo;
import myamya.sdvx.KadaiGeneratorEnums.ClearLamp;
import myamya.sdvx.KadaiGeneratorEnums.Mode;
import myamya.sdvx.KadaiGeneratorEnums.ResponseDiv;
import myamya.sdvx.KadaiGeneratorEnums.Target;
import net.arnx.jsonic.JSON;

/**
 * Servlet implementation class TestWeb
 */
@WebServlet("/KadaiGeneratorWeb")
public class KadaiGeneratorWeb extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public KadaiGeneratorWeb() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		makeResponseInner(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		makeResponseInner(request, response);
	}

	private void makeResponseInner(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		response.setContentType("text/javascript; charset=utf-8");
		response.setHeader("Access-Control-Allow-Origin", "*");
		Map<String, Object> resultMap = new HashMap<>();
		List<Map<String, Object>> resultList = new ArrayList<>();
		try {
			String userId = request.getParameter("userid");
			Mode mode = Mode.getByValue(Integer.parseInt(request.getParameter("mode")));
			int lvlMin = Integer.parseInt(request.getParameter("lvl_min"));
			int lvlMax = mode == Mode.FOR_BORDER || mode == Mode.FOR_CLEAR
					? Integer.parseInt(request.getParameter("lvl_min"))
					: Integer.parseInt(request.getParameter("lvl_max"));
			int dispCnt = mode == Mode.FOR_BORDER || mode == Mode.FOR_CLEAR ? 1000
					: Integer.parseInt(request.getParameter("disp_cnt"));
			int border = mode != Mode.FOR_BORDER ? 0 : Integer.parseInt(request.getParameter("border"));
			Target target = mode != Mode.FOR_TARGET && mode != Mode.FOR_CLEAR ? ClearLamp.NOPLAY
					: Target.getByVal(Integer.parseInt(request.getParameter("clear")));
			System.out.println("sdvx:userId=" + userId + ",mode=" + mode + ",lvlMin=" + lvlMin + ",lvlMax=" + lvlMax
					+ ",dispCnt=" + dispCnt + ",border=" + border + ",target=" + target);
			if (userId == null || userId.equals("")) {
				resultMap.put("error_msg", ResponseDiv.NOT_INPUT_USER_ID.getErrorMsg());
			} else if (lvlMin > lvlMax) {
				resultMap.put("error_msg", ResponseDiv.REVERSE_LVL.getErrorMsg());
			} else {
				ResponseInfo responseInfo = new KadaiGenerator().execute(userId, lvlMin, lvlMax, mode, border, target);
				if (responseInfo.getResponseDiv() == ResponseDiv.SUCCESS) {
					responseInfo.getEstimateInfoList().forEach(new Consumer<EstimateInfo>() {
						// dispCntに達するまで表示
						// ラムダ式にしてしまうとcntがインクリメントできないので無名クラス
						int cnt = 0;

						@Override
						public void accept(EstimateInfo o) {
							if (cnt == dispCnt) {
								return;
							}
							cnt++;
							Map<String, Object> result = o.toMap();
							// 何番目かという情報を加える
							result.put("rank", cnt);
							resultList.add(result);
						}
					});
					if (mode == Mode.FOR_BORDER || mode == Mode.FOR_CLEAR) {
						int winCnt = 0;
						for (EstimateInfo estimateInfo : responseInfo.getEstimateInfoList()) {
							if (estimateInfo.getEstimateRate().compareTo(BigDecimal.ZERO) >= 0) {
								winCnt++;
							}
						}
						resultMap.put("achieve_info",
								winCnt + "/" + responseInfo.getEstimateInfoList().size() + " ("
										+ (BigDecimal.valueOf(winCnt))
												.divide(BigDecimal.valueOf(responseInfo.getEstimateInfoList().size()),
														4,
														RoundingMode.DOWN)
												.multiply(BigDecimal.valueOf(100)).setScale(2)
										+ "%)");
					}
				}
				resultMap.put("error_msg", responseInfo.getResponseDiv().getErrorMsg());
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("error_msg", ResponseDiv.UNEXPECTED_ERROR.getErrorMsg());
		}
		resultMap.put("result", resultList);
		try (PrintWriter out = response.getWriter()) {
			out.print(JSON.encode(resultMap));
		}
	}

}

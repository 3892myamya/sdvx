package myamya.sdvx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import myamya.sdvx.KadaiGeneratorClasses.RatePair;
import myamya.sdvx.KadaiGeneratorClasses.StatusInfo;
import myamya.sdvx.KadaiGeneratorEnums.ScoreDiv;

/**
 * 便利メソッドをまとめたクラスです。
 */
public class KadaiGeneratorUtil {

	private KadaiGeneratorUtil() {
		// インスタンス生成禁止
	}

	/**
	 * urlのリーダーを取得します。
	 */
	public static BufferedReader getReader(URL url) throws IOException, ProtocolException {
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setRequestMethod("GET");
		http.connect();
		return new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"));
	}

	/**
	 * 自身のスコアから、そのスコアがスコアツール登録者内で何%の位置にいるか推定して返す
	 */
	public static BigDecimal getEstimateRate(StatusInfo statusInfo, int score) {
		ScoreDiv scoreDiv = ScoreDiv.getByScore(score);
		int diff = score - scoreDiv.getMin();
		BigDecimal adjustRate = BigDecimal.valueOf(diff).divide(BigDecimal.valueOf(scoreDiv.getRange()), 3,
				RoundingMode.DOWN);
		RatePair ratePair = statusInfo.getRatePair(scoreDiv);
		BigDecimal range = ratePair.getSelfRate().subtract(ratePair.getNextRate());
		BigDecimal adjust = range.multiply(adjustRate);
		return ratePair.getSelfRate().subtract(adjust).setScale(2, RoundingMode.DOWN);
	}
}

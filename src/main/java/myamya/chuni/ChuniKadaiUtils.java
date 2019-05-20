package myamya.chuni;

import java.math.BigDecimal;
import java.math.RoundingMode;

import myamya.chuni.ChuniKadaiClasses.RatePair;
import myamya.chuni.ChuniKadaiClasses.StatusInfo;
import myamya.chuni.ChuniKadaiEnums.ScoreDiv;

/**
 * 便利メソッドをまとめたクラスです。
 */
public class ChuniKadaiUtils {

	private ChuniKadaiUtils() {
		// インスタンス生成禁止
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

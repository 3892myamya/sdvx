package myamya.sdvx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import myamya.sdvx.KadaiGeneratorClasses.EffectInfo;
import myamya.sdvx.KadaiGeneratorClasses.OneEffectInfo;
import myamya.sdvx.KadaiGeneratorClasses.RatePair;
import myamya.sdvx.KadaiGeneratorClasses.StatusInfo;
import myamya.sdvx.KadaiGeneratorClasses.TrackInfo;
import myamya.sdvx.KadaiGeneratorClasses.VolForceInfo;
import myamya.sdvx.KadaiGeneratorEnums.EffectDiv;
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

	/**
	 * ボルフォース情報の取得
	 */
	public static VolForceInfo getVolForceInfo(List<TrackInfo> trackList) {
		List<Integer> wk = new ArrayList<>();
		// ボルフォース下限を計算
		for (TrackInfo trackInfo : trackList) {
			for (EffectInfo effectInfo : trackInfo.getEffectMap().values()) {
				wk.add(effectInfo.getOneVolForce());
			}
		}
		Collections.sort(wk, Comparator.reverseOrder());
		int volForceTargetCount = wk.size() < 50 ? wk.size() : 50;
		int volForceMin = wk.get(volForceTargetCount - 1);
		// 下限でもう一回対象曲を調べる
		List<OneEffectInfo> volForceList = new ArrayList<>();
		for (TrackInfo trackInfo : trackList) {
			for (Entry<EffectDiv, EffectInfo> entry : trackInfo.getEffectMap().entrySet()) {
				if (entry.getValue().getOneVolForce() >= volForceMin) {
					volForceList.add(new OneEffectInfo(trackInfo.getId(), trackInfo.getTitle(), entry.getKey(),
							entry.getValue()));
				}
			}
		}
		Collections.sort(volForceList, new Comparator<OneEffectInfo>() {
			@Override
			public int compare(OneEffectInfo o1, OneEffectInfo o2) {
				return o2.getEffectInfo().getOneVolForce() - o1.getEffectInfo().getOneVolForce();
			}
		});
		int totalVolForce = 0;
		for (int i = 0; i < volForceTargetCount; i++) {
			totalVolForce = totalVolForce + volForceList.get(i).getEffectInfo().getOneVolForce();
		}
		return new VolForceInfo(totalVolForce, volForceMin, volForceList);
	}
}

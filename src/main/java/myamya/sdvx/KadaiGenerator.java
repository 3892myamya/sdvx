package myamya.sdvx;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import myamya.sdvx.KadaiGeneratorClasses.EffectInfo;
import myamya.sdvx.KadaiGeneratorClasses.EstimateInfo;
import myamya.sdvx.KadaiGeneratorClasses.ProfileInfo;
import myamya.sdvx.KadaiGeneratorClasses.RatePair;
import myamya.sdvx.KadaiGeneratorClasses.ResponseInfo;
import myamya.sdvx.KadaiGeneratorClasses.StatusInfo;
import myamya.sdvx.KadaiGeneratorClasses.StatusInfoAll;
import myamya.sdvx.KadaiGeneratorClasses.TrackInfo;
import myamya.sdvx.KadaiGeneratorEnums.EffectDiv;
import myamya.sdvx.KadaiGeneratorEnums.Mode;
import myamya.sdvx.KadaiGeneratorEnums.ResponseDiv;
import myamya.sdvx.KadaiGeneratorEnums.ScoreDiv;
import net.arnx.jsonic.JSON;

public class KadaiGenerator {

	private static final StatusInfoAll statusInfoAll = new StatusInfoAll();
	private static final Object locker = new Object();

	/**
	 * userIdを利用してスコアツールのデータを取得し、
	 * minLevel, maxLevelの条件に合致する分析結果を
	 * modeで指定した内容に沿ったソート順のListで返す。
	 */
	public ResponseInfo execute(String userId, int minLevel, int maxLevel, Mode mode, int boader)
			throws MalformedURLException, ProtocolException, IOException {
		// 自分の情報を取る
		Map<String, Object> profileMap = getProfileMap(userId);
		if (profileMap.containsKey("errormsg")) {
			System.err.println(profileMap.get("errormsg"));
			return new ResponseInfo(ResponseDiv.INVALID_USER_ID, new ArrayList<>());
		}
		// 自身の情報を整形
		ProfileInfo profileInfo = new ProfileInfo(profileMap);
		Map<String, Map<EffectDiv, StatusInfo>> statusInfoMap;
		synchronized (locker) {
			// 整形された統計情報を取得
			statusInfoMap = statusInfoAll.checkAndGetStatusInfoMap();
		}
		//曲ごとにマッチングして譜面ごとに上位何%か推定
		List<EstimateInfo> estimateInfoList = getEstimateInfoList(profileInfo, statusInfoMap, mode, boader);
		// estimateRateMapを降順ソートし、レベルでフィルター
		estimateInfoList = getEstimateInfoList(minLevel, maxLevel, estimateInfoList, mode);
		if (estimateInfoList.isEmpty()) {
			return new ResponseInfo(ResponseDiv.NOT_FOUND, estimateInfoList);
		} else {
			return new ResponseInfo(ResponseDiv.SUCCESS, estimateInfoList);
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getProfileMap(String userId)
			throws MalformedURLException, IOException, ProtocolException {
		StringBuilder sb = new StringBuilder();
		URL url = new URL("https://nearnoah.net/api/showUserData.json?username=" + userId);
		try (BufferedReader reader = KadaiGeneratorUtil.getReader(url)) {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		}
		return JSON.decode(sb.toString(), Map.class);
	}

	/**
	 * 自身の情報と統計情報より上位何%か推定
	 */
	private List<EstimateInfo> getEstimateInfoList(ProfileInfo profileInfo,
			Map<String, Map<EffectDiv, StatusInfo>> statusInfoMap, Mode mode, int border) {
		List<EstimateInfo> estimateInfoList = new ArrayList<>();
		for (TrackInfo trackInfo : profileInfo.getTrackList()) {
			Map<EffectDiv, StatusInfo> oneStatusInfoMap = statusInfoMap.get(trackInfo.getTitle());
			if (oneStatusInfoMap == null) {
				// XXX ダブルクオート含む曲の暫定対応
				String xxx = trackInfo.getTitle().replace("\"", "\\");
				oneStatusInfoMap = statusInfoMap.get(xxx);
				if (oneStatusInfoMap == null) {
					// TODO 曲の統計情報がない場合
					continue;
				}
			}
			for (Entry<EffectDiv, EffectInfo> entry : trackInfo.getEffectMap().entrySet()) {
				StatusInfo statusInfo = oneStatusInfoMap.get(entry.getKey());
				if (statusInfo == null) {
					// TODO 譜面の統計情報がない場合
				} else {
					if (entry.getValue().getScore() == 0) {
						// TODO 未プレイの場合
					} else {
						if (mode == Mode.FOR_BORDER) {
							int borderScore = getBorderScore(border, statusInfo);
							estimateInfoList.add(
									new EstimateInfo(trackInfo.getTitle(), statusInfo.getEffectDiv(),
											borderScore,
											entry.getValue().getLevel(),
											BigDecimal.valueOf(entry.getValue().getScore() - borderScore),
											String.valueOf(entry.getValue().getScore() - borderScore)));
						} else {
							BigDecimal estimateRate = mode.getEstimateRateCalculator().getEstimateRate(statusInfo,
									entry.getValue().getScore());
							estimateInfoList.add(
									new EstimateInfo(trackInfo.getTitle(), statusInfo.getEffectDiv(),
											entry.getValue().getScore(),
											entry.getValue().getLevel(), estimateRate,
											mode.getDispEstimateRate(estimateRate)));
						}
					}
				}
			}

		}
		return estimateInfoList;
	}

	/**
	 * 上位何%かという情報を検索条件でソート＆フィルタリング
	 */
	private List<EstimateInfo> getEstimateInfoList(int minLevel, int maxLevel,
			List<EstimateInfo> estimateInfoList, Mode mode) {
		/**
		 * 率でのソート
		 */
		Comparator<EstimateInfo> c = new Comparator<EstimateInfo>() {
			@Override
			public int compare(EstimateInfo o1, EstimateInfo o2) {
				return o1.getEstimateRate().compareTo(o2.getEstimateRate());
			}
		};

		/**
		 * 曲名と譜面名区分でのソート
		 * 今は使っていないが将来を見越して残す
		 */
		@SuppressWarnings("unused")
		Comparator<EstimateInfo> tc = new Comparator<EstimateInfo>() {
			@Override
			public int compare(EstimateInfo o1, EstimateInfo o2) {
				int titleCompareResult = o1.getTitle().compareTo(o2.getTitle());
				if (titleCompareResult != 0) {
					return titleCompareResult;
				} else {
					return Integer.valueOf(o1.getEffectDiv().ordinal()).compareTo(o2.getEffectDiv().ordinal());
				}
			}
		};

		List<EstimateInfo> result = estimateInfoList.stream().sorted(
				// 推定率のソート
				// 今回やりたいことをやるには先にComparatorを作っておかないとだめっぽい
				mode == Mode.FOR_BORDER ? c : mode == Mode.FOR_SCORE ? c.reversed() : c).filter(
						o -> {
							// レベルによる絞り込み
							boolean b = minLevel <= o.getLevel() && o.getLevel() <= maxLevel;
							if (mode == Mode.FOR_PERFECT) {
								// PER狙いの場合、指数が0(まだP者がいないもしくは既にPUC済み)は表示しない
								return b && o.getEstimateRate().compareTo(BigDecimal.ZERO) != 0;
							} else if (mode == Mode.FOR_SCORE) {
								// スコア狙いの場合、すでにPUC済みの曲は表示しない
								return b && o.getScore() != 10000000;
							} else {
								return b;
							}
						})
				.collect(Collectors.toList());
		return result;
	}

	private int getBorderScore(int borderInt, StatusInfo statusInfo) {
		ScoreDiv scoreDiv;
		BigDecimal border = BigDecimal.valueOf(borderInt);
		if (statusInfo.getPPer().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.PER;
		} else if (statusInfo.getPGrade998().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.S_998;
		} else if (statusInfo.getPGrade995().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.S_995;
		} else if (statusInfo.getPGradeS().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.S;
		} else if (statusInfo.getPGradeAAAp().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.AAA_PLUS;
		} else if (statusInfo.getPGradeAAA().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.AAA;
		} else if (statusInfo.getPGradeAAp().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.AA_PLUS;
		} else if (statusInfo.getPGradeAA().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.AA;
		} else if (statusInfo.getPGradeAp().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.A_PLUS;
		} else if (statusInfo.getPGradeA().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.A;
		} else {
			scoreDiv = ScoreDiv.UNDER_A;
		}
		RatePair ratePair = statusInfo.getRatePair(scoreDiv);
		// 自身のScoreDivと1つ上のScoreDivの最低点と%を使って、
		// 指定されたボーダー%を比例計算で割り出す。
		BigDecimal a = BigDecimal.valueOf(scoreDiv.getRange());
		BigDecimal b = ratePair.getSelfRate().subtract(ratePair.getNextRate());
		BigDecimal c = (a).divide(b, 3, RoundingMode.DOWN);
		BigDecimal d = ratePair.getSelfRate().subtract(border);
		BigDecimal adjustValue = c.multiply(d);
		return scoreDiv.getMin() + adjustValue.intValue();
	}
}

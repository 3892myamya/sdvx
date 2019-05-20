package myamya.chuni;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import myamya.chuni.ChuniKadaiClasses.EffectInfo;
import myamya.chuni.ChuniKadaiClasses.EstimateInfo;
import myamya.chuni.ChuniKadaiClasses.ProfileInfo;
import myamya.chuni.ChuniKadaiClasses.RatePair;
import myamya.chuni.ChuniKadaiClasses.ResponseInfo;
import myamya.chuni.ChuniKadaiClasses.StatusInfo;
import myamya.chuni.ChuniKadaiClasses.StatusInfoAll;
import myamya.chuni.ChuniKadaiClasses.TrackInfo;
import myamya.chuni.ChuniKadaiEnums.ClearLamp;
import myamya.chuni.ChuniKadaiEnums.EffectDiv;
import myamya.chuni.ChuniKadaiEnums.Mode;
import myamya.chuni.ChuniKadaiEnums.ResponseDiv;
import myamya.chuni.ChuniKadaiEnums.ScoreDiv;
import myamya.chuni.ChuniKadaiEnums.Target;
import net.arnx.jsonic.JSON;

public class ChuniKadaiGenerator {

	private static final StatusInfoAll statusInfoAll = new StatusInfoAll();
	private static final Object locker = new Object();

	/**
	 * userIdを利用してスコアツールのデータを取得し、
	 * minLevel, maxLevelの条件に合致する分析結果を
	 * modeで指定した内容に沿ったソート順のListで返す。
	 */
	public ResponseInfo execute(String userId, BigDecimal minLevel, BigDecimal maxLevel, Mode mode, int boader,
			Target target)
			throws MalformedURLException, ProtocolException, IOException {
		// 自分の情報を取る
		Map<String, Object> profileMap = getProfileMap(userId);
		if (profileMap.containsKey("errormsg")) {
			System.err.println(profileMap.get("errormsg"));
			return new ResponseInfo(ResponseDiv.INVALID_USER_ID, new ArrayList<>());
		}
		Map<String, Object> musicInfoMap = getMusicInfoMap();
		// 自身の情報を整形
		ProfileInfo profileInfo = new ProfileInfo(profileMap, musicInfoMap);
		Map<String, Map<EffectDiv, StatusInfo>> statusInfoMap;
		synchronized (locker) {
			// 整形された統計情報を取得
			statusInfoMap = statusInfoAll.checkAndGetStatusInfoMap();
		}
		//曲ごとにマッチングして譜面ごとに上位何%か推定
		List<EstimateInfo> estimateInfoList = getEstimateInfoList(profileInfo, statusInfoMap, mode, boader, target);
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
		URL url = new URL("https://chuniviewer.net/GetUserScore.php");
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setDoOutput(true);
		http.setUseCaches(false);
		http.setRequestMethod("POST");
		http.addRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		try (OutputStreamWriter out = new OutputStreamWriter(new BufferedOutputStream(http.getOutputStream()))) {
			out.write("username=" + URLEncoder.encode(userId, "UTF-8"));
			out.flush();
			http.connect();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"))) {
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
			}
		}
		return JSON.decode(sb.toString(), Map.class);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getMusicInfoMap() throws MalformedURLException, IOException, ProtocolException {
		StringBuilder sb = new StringBuilder();
		URL url = new URL("https://chuniviewer.net/GetMusicData.php");
		HttpURLConnection http = (HttpURLConnection) url.openConnection();
		http.setDoOutput(true);
		http.setUseCaches(false);
		http.setRequestMethod("POST");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream(), "UTF-8"))) {
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
			Map<String, Map<EffectDiv, StatusInfo>> statusInfoMap, Mode mode, int border, Target target) {
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
						if (mode == Mode.FOR_TARGET) {
							boolean isClear;
							if (target instanceof ClearLamp) {
								isClear = entry.getValue().getClear().isClear((ClearLamp) target);
							} else if (target instanceof ScoreDiv) {
								isClear = ((ScoreDiv) target).isClear(entry.getValue().getScore());
							} else {
								throw new IllegalArgumentException();
							}
							if (!isClear) {
								BigDecimal scoreBase = ChuniKadaiUtils
										.getEstimateRate(statusInfo, entry.getValue().getScore());
								if (scoreBase.compareTo(BigDecimal.ZERO) != 0) {
									BigDecimal rate = statusInfo.getPercent(target).multiply(new BigDecimal(100))
											.divide(scoreBase, 2, RoundingMode.DOWN);
									estimateInfoList.add(
											new EstimateInfo(trackInfo.getTitle(), statusInfo.getEffectDiv(),
													BigDecimal.valueOf(entry.getValue().getScore()),
													String.valueOf(entry.getValue().getScore()),
													entry.getValue().getLevel(), rate, String.valueOf(rate) + " p"));
								}
							}
						} else if (mode == Mode.FOR_BORDER) {
							int borderScore = getBorderScore(border, statusInfo);
							estimateInfoList.add(
									new EstimateInfo(trackInfo.getTitle(), statusInfo.getEffectDiv(),
											BigDecimal.valueOf(borderScore), String.valueOf(borderScore),
											entry.getValue().getLevel(),
											BigDecimal.valueOf(entry.getValue().getScore() - borderScore),
											String.valueOf(entry.getValue().getScore() - borderScore)));
						} else if (mode == Mode.FOR_CLEAR) {
							boolean isClear;
							if (target instanceof ClearLamp) {
								isClear = entry.getValue().getClear().isClear((ClearLamp) target);
							} else if (target instanceof ScoreDiv) {
								isClear = ((ScoreDiv) target).isClear(entry.getValue().getScore());
							} else {
								throw new IllegalArgumentException();
							}
							estimateInfoList.add(
									new EstimateInfo(trackInfo.getTitle(), statusInfo.getEffectDiv(),
											statusInfo.getPercent(target),
											statusInfo.getPercent(target).toPlainString() + "%",
											entry.getValue().getLevel(),
											isClear ? BigDecimal.valueOf(0) : BigDecimal.valueOf(-1),
											isClear ? "達成" : "未達成"));
						} else {
							BigDecimal estimateRate = ChuniKadaiUtils.getEstimateRate(statusInfo,
									entry.getValue().getScore());
							estimateInfoList.add(
									new EstimateInfo(trackInfo.getTitle(), statusInfo.getEffectDiv(),
											BigDecimal.valueOf(entry.getValue().getScore()),
											String.valueOf(entry.getValue().getScore()),
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
	private List<EstimateInfo> getEstimateInfoList(BigDecimal minLevel, BigDecimal maxLevel,
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
		 * スコアでの降順ソート
		 */
		Comparator<EstimateInfo> sc = new Comparator<EstimateInfo>() {
			@Override
			public int compare(EstimateInfo o1, EstimateInfo o2) {
				return o2.getScore().compareTo(o1.getScore());
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
				mode == Mode.FOR_CLEAR ? sc
						: mode == Mode.FOR_SCORE || mode == Mode.FOR_TARGET ? c.reversed()
								: c)
				.filter(
						o -> {
							// レベルによる絞り込み
							boolean b = minLevel.compareTo(o.getLevel()) <= 0 && o.getLevel().compareTo(maxLevel) <= 0;
							if (mode == Mode.FOR_SCORE) {
								// スコア狙いの場合、すでにPUC済みの曲は表示しない
								return b && o.getScore().compareTo(BigDecimal.valueOf(1010000)) != 0;
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
		if (statusInfo.getPMAX().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.MAX;
		} else if (statusInfo.getPSSSp().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.SSS_PLUS;
		} else if (statusInfo.getPSSS().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.SSS;
		} else if (statusInfo.getPSSp().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.SS_PLUS;
		} else if (statusInfo.getPSS().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.SS;
		} else if (statusInfo.getPS().compareTo(border) >= 0) {
			scoreDiv = ScoreDiv.S;
		} else {
			scoreDiv = ScoreDiv.UNDER_S;
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

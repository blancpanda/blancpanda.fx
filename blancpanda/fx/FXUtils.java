/**
 * 
 */
package blancpanda.fx;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.RegularTimePeriod;

import blancpanda.fx.timeperiod.Hour2;
import blancpanda.fx.timeperiod.Minute30;
import blancpanda.fx.timeperiod.Minute5;

/**
 * @author Kaoru
 * 
 */
public class FXUtils {
	
	public static final long M1_SEGMENT_SIZE = 1000 * 60;
	public static final long M5_SEGMENT_SIZE = M1_SEGMENT_SIZE * 5;
	public static final long M30_SEGMENT_SIZE = M1_SEGMENT_SIZE * 30;
	public static final long H1_SEGMENT_SIZE = M1_SEGMENT_SIZE * 60;
	public static final long H2_SEGMENT_SIZE = H1_SEGMENT_SIZE * 2;
	
	/**
	 * 取引時間チェック
	 * @param date 日本時間のDate
	 * @return true 取引時間内
	 * @return false 取引時間外
	 */
	public static boolean isMarketTime(Date date){
		boolean ret = false;

		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tokyo"));
		cal.setTime(date);
		
		// America/New_York ニューヨーク時間でサマータイムかどうかチェック
		TimeZone zone = TimeZone.getTimeZone("America/New_York");
		boolean summertime = zone.inDaylightTime(cal.getTime());
		int opentime = 7;
		int closetime = 7;
		if(summertime){
			closetime = 6;
		}
		//System.out.println(cal.getTime());
		if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY){
			// 土曜日
			if(cal.get(Calendar.HOUR_OF_DAY) >= closetime){
				//System.out.println("取引時間外です。");
				ret = false;
			} else {
				//System.out.println("取引時間内です。");
				ret = true;
			}
		}else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
			// 日曜日
			//System.out.println("取引時間外です。");
			ret = false;
		}else if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY){
			// 月曜日
			if(cal.get(Calendar.HOUR_OF_DAY) < opentime){
				//System.out.println("取引時間外です。");
				ret = false;
			}else{
				//System.out.println("取引時間内です。");
				ret = true;
			}
		}else{
			//System.out.println("取引時間内です。");
			ret = false;
		}
		
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap getRateData(){
		HashMap map = null;
		InputStream is = null;
		URL url;
		URLConnection urlconn;
		try {
			url = new URL("http://min-fx.jp/market/rate/var/rate.json");
			urlconn = url.openConnection();
			is = urlconn.getInputStream();
		} catch (MalformedURLException e) {
			//e.printStackTrace();
			System.out.println("みんなのFXへの接続に失敗しました");
			return null;
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("みんなのFXへの接続に失敗しました");
			return null;
		}
		try {
			map = JSON.decode(is, HashMap.class);
		} catch (JSONException e) {
			//e.printStackTrace();
			System.out.println("JSONの解析に失敗しました");
			return null;
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("JSONの解析に失敗しました");
			return null;
		}
		
		// JSONの解析に成功しても、timestampが取引時間外ならnullを返す
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(Long.parseLong((String) map.get("timestamp")));
		if(!isMarketTime(cal.getTime())){
			return null;
		}
		
		return map;
	}
	
	public static Date calcDateAxisMin(Date date, int period, int cnt){
		Date ret = null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		switch (period) {
		case CandleStick.M1:
			cal.setTimeInMillis(cal.getTimeInMillis() - (M1_SEGMENT_SIZE * cnt));
			ret = cal.getTime();
			break;
		case CandleStick.M5:
			cal.setTimeInMillis(cal.getTimeInMillis() - (M5_SEGMENT_SIZE * cnt));
			ret = cal.getTime();
			break;
		case CandleStick.M30:
			cal.setTimeInMillis(cal.getTimeInMillis() - (M30_SEGMENT_SIZE * cnt));
			ret = cal.getTime();
			break;
		case CandleStick.H1:
			cal.setTimeInMillis(cal.getTimeInMillis() - (H1_SEGMENT_SIZE * cnt));
			ret = cal.getTime();
			break;
		case CandleStick.H2:
			cal.setTimeInMillis(cal.getTimeInMillis() - (H2_SEGMENT_SIZE * cnt));
			ret = cal.getTime();
			break;
		default:
			System.out.println("ピリオドが不正です。");
			break;
		}
		return ret;
	}
	
	public static RegularTimePeriod getRegularTimePeriod(Date date, int period) {
		RegularTimePeriod ret = null;
		switch (period) {
		case CandleStick.M1:
			ret = new Minute(date);
			break;
		case CandleStick.M5:
			ret = new Minute5(date);
			break;
		case CandleStick.M30:
			ret = new Minute30(date);
			break;
		case CandleStick.H1:
			ret = new Hour(date);
			break;
		case CandleStick.H2:
			ret = new Hour2(date);
			break;
		default:
			System.out.println("ピリオドが不正です。");
			break;
		}
		return ret;
	}

	public static SegmentedTimeline getFXTimeline() {
		SegmentedTimeline st = new SegmentedTimeline(
				SegmentedTimeline.HOUR_SEGMENT_SIZE, 120, 48);
		st.setStartTime(SegmentedTimeline.firstMondayAfter1900()
				+ SegmentedTimeline.HOUR_SEGMENT_SIZE * 7);
		return st;
	}
}

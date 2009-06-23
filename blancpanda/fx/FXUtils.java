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
import org.jfree.data.time.Day;
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
	
	public static String[] STR_CURRENCY_PAIR = { "USD/CAD", "EUR/JPY", "NZD/JPY",
		"GBP/CHF", "USD/CHF", "ZAR/JPY", "NZD/USD", "CAD/JPY",
		"EUR/GBP", "USD/JPY", // 米ドル円
		"CHF/JPY", "GBP/JPY", // 英ポンド円
		"GBP/USD", "AUD/JPY", "EUR/USD", "AUD/USD" };
	
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
			ret = true;
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
		
		return map;
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
		case CandleStick.D1:
			ret = new Day(date);
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

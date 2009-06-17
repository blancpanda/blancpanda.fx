package blancpanda.fx;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.jfree.data.time.RegularTimePeriod;

public class CandleStick implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 20090530L;

	// period_cd の選択肢
	public static final int M1 = 0; // 1分
	public static final int M5 = 1; // 5分足
	public static final int M30 = 2; // 30分足
	public static final int H1 = 3; // 1時間足
	public static final int H2 = 4; // 2時間足
//	public static final int D1 = 5; // 日足

	// currency_pair の選択肢
	public static final int USDCAD = 0; 	//
	public static final int EURJPY = 1; 	//
	public static final int NZDJPY = 2; 	//
	public static final int GBPCHF = 3; 	//
	public static final int USDCHF = 4; 	//
	public static final int ZARJPY = 5; 	//
	public static final int NZDUSD = 6; 	//
	public static final int CADJPY = 7; 	//
	public static final int EURGBP = 8; 	//
	public static final int USDJPY = 9;		// 米ドル円
	public static final int CHFJPY = 10; 	//
	public static final int GBPJPY = 11; 	// 英ポンド円
	public static final int GBPUSD = 12; 	//
	public static final int AUDJPY = 13; 	//
	public static final int EURUSD = 14; 	//
	public static final int AUDUSD = 15; 	//

	private String csid;
	private String time;
	private int currency_pair;
	private int period_cd;
	private double bid_open;
	private double bid_close;
	private double bid_high;
	private double bid_low;
	private double ask_open;
	private double ask_close;
	private double ask_high;
	private double ask_low;

	private SimpleDateFormat sdf;
	private double bid;
	private double ask;

	public CandleStick(int currency_pair, int period_cd){
		super();
		sdf = new SimpleDateFormat("yyyyMMddHHmm");
		this.period_cd = period_cd;
		this.currency_pair = currency_pair;
		initRate();
	}

	public CandleStick(){
		this(CandleStick.USDJPY, CandleStick.M5);
	}

	/**
	 * @return csid
	 */
	public String getCsid() {
		return csid;
	}
	/**
	 * @param csid セットする csid
	 */
	public void setCsid(String csid) {
		this.csid = csid;
	}
	/**
	 * @return time
	 */
	public String getTime() {
		return time;
	}
	/**
	 * @param time セットする time
	 */
	public void setTime(String time) {
		this.time = time;
	}
	/**
	 * @return
	 */
	public int getCurrency_pair() {
		return currency_pair;
	}
	/**
	 * @param currency_pair
	 */
	public void setCurrency_pair(int currency_pair) {
		this.currency_pair = currency_pair;
	}
	/**
	 * @return period_cd
	 */
	public int getPeriod_cd() {
		return period_cd;
	}
	/**
	 * @param period_cd セットする period_cd
	 */
	public void setPeriod_cd(int period_cd) {
		this.period_cd = period_cd;
	}
	/**
	 * @return bid_open
	 */
	public double getBid_open() {
		return bid_open;
	}
	/**
	 * @param bid_open セットする bid_open
	 */
	public void setBid_open(double bid_open) {
		this.bid_open = bid_open;
	}
	/**
	 * @return bid_close
	 */
	public double getBid_close() {
		return bid_close;
	}
	/**
	 * @param bid_close セットする bid_close
	 */
	public void setBid_close(double bid_close) {
		this.bid_close = bid_close;
	}
	/**
	 * @return bid_high
	 */
	public double getBid_high() {
		return bid_high;
	}
	/**
	 * @param bid_high セットする bid_high
	 */
	public void setBid_high(double bid_high) {
		this.bid_high = bid_high;
	}
	/**
	 * @return bid_low
	 */
	public double getBid_low() {
		return bid_low;
	}
	/**
	 * @param bid_low セットする bid_low
	 */
	public void setBid_low(double bid_low) {
		this.bid_low = bid_low;
	}
	/**
	 * @return ask_open
	 */
	public double getAsk_open() {
		return ask_open;
	}
	/**
	 * @param ask_open セットする ask_open
	 */
	public void setAsk_open(double ask_open) {
		this.ask_open = ask_open;
	}
	/**
	 * @return ask_close
	 */
	public double getAsk_close() {
		return ask_close;
	}
	/**
	 * @param ask_close セットする ask_close
	 */
	public void setAsk_close(double ask_close) {
		this.ask_close = ask_close;
	}
	/**
	 * @return ask_high
	 */
	public double getAsk_high() {
		return ask_high;
	}
	/**
	 * @param ask_high セットする ask_high
	 */
	public void setAsk_high(double ask_high) {
		this.ask_high = ask_high;
	}
	/**
	 * @return ask_low
	 */
	public double getAsk_low() {
		return ask_low;
	}
	/**
	 * @param ask_low セットする ask_low
	 */
	public void setAsk_low(double ask_low) {
		this.ask_low = ask_low;
	}

	public String toString(){
		String str;
		str = "************************************************************\n";
		str += getTime() + " period_cd:" + getPeriod_cd() + "\n";
		str += "[BID] Open:" + getBid_open();
		str += " Close:" + getBid_close();
		str += " High:" + getBid_high();
		str += " Low:" + getBid_low() + "\n";
		str += "[ASK] Open:" + getAsk_open();
		str += " Close:" + getAsk_close();
		str += " High:" + getAsk_high();
		str += " Low:" + getAsk_low() + "\n";
		str += "************************************************************\n";

		return str;
	}

	public Date getDate(){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
		Date date = null;
		try {
			date = sdf.parse(getTime());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	@SuppressWarnings("unchecked")
	public RegularTimePeriod getCurrentRate(){
		// 最新のデータを取得してプロパティを変える
		HashMap map = FXUtils.getRateData();
		return getCurrentRate(map);
	}

	@SuppressWarnings("unchecked")
	public RegularTimePeriod getCurrentRate(HashMap map){
		// 最新のデータを取得してプロパティを変える

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(Long.parseLong((String) map.get("timestamp")));
		time = sdf.format(cal.getTime());
		DecimalFormat df = new DecimalFormat("00");	// 2桁数値にフォーマット
		csid = df.format(currency_pair) + period_cd + time;

		ArrayList<HashMap<String, String>> rates = (ArrayList<HashMap<String, String>>)map.get("rate");
		HashMap<String, String> rate = rates.get(currency_pair);
//		System.out.println(rate.get("currency_pair") + " BID:" + rate.get("bid") + " ASK:" + rate.get("ask"));
		bid = Double.parseDouble(rate.get("bid"));
		ask = Double.parseDouble(rate.get("ask"));
		bid_close = bid;
		ask_close = ask;
		if(bid > bid_high){
			bid_high = bid;
		}
		if(bid < bid_low){
			bid_low = bid;
		}
		if(ask > ask_high){
			ask_high = ask;
		}
		if(ask < ask_low){
			ask_low = ask;
		}
		
		return FXUtils.getRegularTimePeriod(getDate(), period_cd);
	}
	
	private void initRate() {
		getCurrentRate();
		bid_open = bid;
		bid_close = bid;
		bid_high = bid;
		bid_low = bid;
		ask_open = ask;
		ask_close = ask;
		ask_high = ask;
		ask_low = ask;
	}
	
	public void clearRate() {
		bid_open = bid;
		bid_close = bid;
		bid_high = bid;
		bid_low = bid;
		ask_open = ask;
		ask_close = ask;
		ask_high = ask;
		ask_low = ask;
	}
	
	public CandleStick clone(){
		CandleStick clone = new CandleStick(currency_pair, period_cd);
		clone.setCsid(csid);
		clone.setTime(time);
		clone.setAsk_open(ask_open);
		clone.setAsk_high(ask_high);
		clone.setAsk_low(ask_low);
		clone.setAsk_close(ask_close);
		clone.setBid_open(bid_open);
		clone.setBid_high(bid_high);
		clone.setBid_low(bid_low);
		clone.setBid_close(bid_close);
		return clone;
	}
}

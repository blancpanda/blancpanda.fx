/**
 * 
 */
package blancpanda.fx.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.List;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.time.ohlc.OHLCItem;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

import blancpanda.fx.CandleStick;
import blancpanda.fx.CandleStickDao;
import blancpanda.fx.FXUtils;

/**
 * @author Kaoru
 * 
 */
public class FXChartUtils {

	private static final int MAX = 0;
	private static final int MIN = 1;
	public static final int MID = 2;

	public static ValueMarker createMarker(double val) {
		ValueMarker marker = new ValueMarker(val, new Color(0, 102, 0),
				new BasicStroke(1, 1, 1));
		marker.setLabel(String.valueOf(val));
		marker.setLabelAnchor(RectangleAnchor.RIGHT);
		marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
		marker.setLabelPaint(new Color(0, 102, 0));
		return marker;
	}

	public static OHLCSeries loadCandleStick(OHLCSeries candle,
			int currency_pair, int period) {
		CandleStick cs;
		CandleStickDao csDao = new CandleStickDao();
		// List<CandleStick> list = csDao.getRecentList(period);
		List<CandleStick> list = csDao.getRecentList(period, currency_pair);
		int serice = list.size();
		RegularTimePeriod prd = null;
		for (int i = serice - 1; i >= 0; i--) { // 時間の降順で取得してくる
			cs = list.get(i);
			// DBに重複データができてしまっている可能性がある
			prd = FXUtils.getRegularTimePeriod(cs.getDate(), period);
			int index = candle.indexOf(prd);
			// System.out.println("index:" + index);
			if (index >= 0) {
				candle.remove(prd);
			}
			candle.add(prd, cs.getBid_open(), cs.getBid_high(),
					cs.getBid_low(), cs.getBid_close());
		}
		return candle;
	}

	public static CandlestickRenderer getCandleStickRenderer() {
		CandlestickRenderer cr;
		cr = new CandlestickRenderer(CandlestickRenderer.WIDTHMETHOD_AVERAGE);
		// ローソク足の色を変える
		// 陽線を白に
		cr.setUpPaint(Color.WHITE);
		// 陰線を ライトグレイに
		cr.setDownPaint(Color.LIGHT_GRAY);
		// ローソクの枠をダークグレイ、1ピクセルで描画
		cr.setUseOutlinePaint(true);
		cr.setBaseOutlinePaint(new Color(51, 51, 51));
		cr.setBaseOutlineStroke(new BasicStroke(1));
		return cr;
	}

	/**
	 * @param candle
	 * @param index
	 * @param count
	 * @return 
	 *         periodを含めた過去count本分のロウソク足の最高値、最安値、(最高値+最安値)/2を表すTimeSeriesDataItemを返す
	 */
	public static TimeSeriesDataItem[] getMaxMinItem(OHLCSeries candle,
			int index, int count) {
		OHLCItem tmp;
		double max = -1;
		double min = -1;
		// ArrayIndexOutOfBoundsException対策
		if(index < count){
			// ロウソクの本数が足りない場合は、存在する分のみで計算
			count = index + 1;
		}
		for (int i = 1; i <= count; i++) {
			tmp = (OHLCItem) candle.getDataItem(index - count + i);
			if (max < 0 || max < tmp.getHighValue()) {
				max = tmp.getHighValue();
			}
			if (min < 0 || min > tmp.getLowValue()) {
				min = tmp.getLowValue();
			}
		}
		TimeSeriesDataItem[] item = new TimeSeriesDataItem[3];
		item[MAX] = new TimeSeriesDataItem(candle.getPeriod(index), max);
		item[MIN] = new TimeSeriesDataItem(candle.getPeriod(index), min);
		item[MID] = new TimeSeriesDataItem(candle.getPeriod(index),
				(max + min) / 2);
		return item;
	}
	
	public static RegularTimePeriod addTimePeriod(RegularTimePeriod prd, int count){
		RegularTimePeriod ret = prd;
		if(count > 0){
			// 先行
			for (int c = 0; c < count; c++) {
				ret = ret.next();
			}			
		}else{
			// 遅行
			for (int c = 0; c < count * -1; c++) {
				ret = ret.previous();
			}
		}
		return ret;
	}

	/**
	 * indexのアイテムを含むcount本数分の終値の平均を返す
	 * @param candle
	 * @param index
	 * @param count
	 * @return
	 */
	public static double getCloseAvg(OHLCSeries candle, int index, int count){
		// ArrayIndexOutOfBoundsException対策
		if(index < count){
			// ロウソクの本数が足りない場合は、存在する分のみで計算
			count = index + 1;
		}
		double sum = 0.0;
		for(int i = index - count + 1; i <= index; i++){
			OHLCItem tmp = (OHLCItem) candle.getDataItem(i);
			sum += tmp.getCloseValue();
		}
		return sum / count;
	}
	
	/**
	 * indexのアイテムを含むcount本数分の終値の標準偏差を返す
	 * @param candle
	 * @param index
	 * @param count
	 * @param avg
	 * @return
	 */
	public static double getCloseSD(OHLCSeries candle, int index, int count, double avg){
		// ArrayIndexOutOfBoundsException対策
		if(index < count){
			// ロウソクの本数が足りない場合は、存在する分のみで計算
			count = index + 1;
		}
		double sum = 0.0;
		for(int i = index - count + 1; i <= index; i++){
			OHLCItem tmp = (OHLCItem) candle.getDataItem(i);
			sum += (tmp.getCloseValue() - avg ) * (tmp.getCloseValue() - avg );
		}
		return Math.sqrt(sum / count);
	}
}

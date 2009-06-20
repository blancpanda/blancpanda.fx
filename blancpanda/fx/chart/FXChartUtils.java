/**
 * 
 */
package blancpanda.fx.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.List;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
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

	private static final int SMA_CLOSE = 0;
	private static final int SMA_SHORTER = 1;
	private static final int SMA_LONGER = 2;

	/**
	 * 単純移動平均線の短期線のピリオド数
	 */
	private static final int SMA_SHORTER_PERIODS = 21;

	/**
	 * 単純移動平均線の長期線のピリオド数
	 */
	private static final int SMA_LONGER_PERIODS = 90;

	private static final int ICHIMOKU_KIJUN_PERIODS = 26;
	private static final int ICHIMOKU_TENKAN_PERIODS = 9;
	private static final int ICHIMOKU_KIJUN = 0;
	private static final int ICHIMOKU_TENKAN = 1;
	private static final int ICHIMOKU_SENKO1 = 0;
	private static final int ICHIMOKU_SENKO2 = 1;
	private static final int ICHIMOKU_CHIKO = 2;
	private static final int MAX = 0;
	private static final int MIN = 1;
	private static final int MID = 2;

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

	public static TimeSeriesCollection getSimpleMovingAverage(OHLCSeries candle) {
		TimeSeriesCollection tsc = new TimeSeriesCollection();
		// 終値のTimeSeriesを作成
		TimeSeries close = new TimeSeries("close");
		for (int i = 0; i < candle.getItemCount(); i++) {
			OHLCItem item = (OHLCItem) candle.getDataItem(i);
			close.add(item.getPeriod(), item.getCloseValue());
		}
		tsc.addSeries(close);

		// 短期線のTimeSeriesを作成
		TimeSeries sma_shorter = MovingAverage.createMovingAverage(close,
				"sma_shorter", SMA_SHORTER_PERIODS, SMA_SHORTER_PERIODS);
		tsc.addSeries(sma_shorter);

		// 長期線のTimeSeriesを作成
		TimeSeries sma_longer = MovingAverage.createMovingAverage(close,
				"sma_longer", SMA_LONGER_PERIODS, SMA_LONGER_PERIODS);
		tsc.addSeries(sma_longer);

		return tsc;
	}

	public static TimeSeriesCollection updatePartOfSMA(OHLCSeries candle,
			TimeSeriesCollection sma) {
		// 終値線の最後の方を更新または付け足し
		TimeSeries close = sma.getSeries(SMA_CLOSE);
		for (int i = close.getItemCount() - 1; i < candle.getItemCount(); i++) {
			if (i >= 0) {
				OHLCItem item = (OHLCItem) candle.getDataItem(i);
				close.addOrUpdate(item.getPeriod(), item.getCloseValue());
			}
		}
		// 短期線を更新
		TimeSeries sma_shorter = sma.getSeries(SMA_SHORTER);
		sma_shorter.addAndOrUpdate(MovingAverage.createMovingAverage(close,
				"sma_shorter", SMA_SHORTER_PERIODS, candle.getItemCount() - 1));
		// 長期線を更新
		TimeSeries sma_longer = sma.getSeries(SMA_LONGER);
		sma_longer.addAndOrUpdate(MovingAverage.createMovingAverage(close,
				"sma_longer", SMA_LONGER_PERIODS, candle.getItemCount() - 1));
		return sma;
	}

	public static TimeSeriesCollection updateAllOfSMA(OHLCSeries candle,
			TimeSeriesCollection sma) {
		// 終値線を作り直し
		TimeSeries close = sma.getSeries(SMA_CLOSE);
		close.clear();
		for (int i = 0; i < candle.getItemCount(); i++) {
			OHLCItem item = (OHLCItem) candle.getDataItem(i);
			close.add(item.getPeriod(), item.getCloseValue());
		}

		// 短期線、長期線を削除
		sma.removeSeries(SMA_LONGER);
		sma.removeSeries(SMA_SHORTER);

		// 短期線のTimeSeriesを作成
		TimeSeries sma_shorter = MovingAverage.createMovingAverage(close,
				"sma_shorter", SMA_SHORTER_PERIODS, SMA_SHORTER_PERIODS);
		sma.addSeries(sma_shorter);

		// 長期線のTimeSeriesを作成
		TimeSeries sma_longer = MovingAverage.createMovingAverage(close,
				"sma_longer", SMA_LONGER_PERIODS, SMA_LONGER_PERIODS);
		sma.addSeries(sma_longer);
		return sma;
	}

	public static XYLineAndShapeRenderer getSimpleMovingAverateRenderer() {
		XYLineAndShapeRenderer xyr = new XYLineAndShapeRenderer();
		xyr.setSeriesLinesVisible(SMA_CLOSE, false); // closeRateを非表示にする
		xyr.setSeriesShapesVisible(SMA_CLOSE, false);
		xyr.setSeriesShapesVisible(SMA_SHORTER, false); // shapeを非表示にする
		xyr.setSeriesStroke(SMA_SHORTER, new BasicStroke(1, 1, 1));
		xyr.setSeriesShapesVisible(SMA_LONGER, false);
		xyr.setSeriesStroke(SMA_LONGER, new BasicStroke(1, 1, 1));
		xyr.setSeriesPaint(SMA_SHORTER, Color.RED); // 短期線は赤
		xyr.setSeriesPaint(SMA_LONGER, Color.BLUE); // 長期線は青
		return xyr;
	}

	public static TimeSeriesCollection[] getIchimoku(OHLCSeries candle) {
		TimeSeriesCollection[] tsc = new TimeSeriesCollection[2];
		tsc[0] = new TimeSeriesCollection();
		tsc[1] = new TimeSeriesCollection();
		// 基準線
		TimeSeries kijun = new TimeSeries("kijun");
		for (int i = ICHIMOKU_KIJUN_PERIODS - 1; i < candle.getItemCount(); i++) {
			TimeSeriesDataItem[] item = getMaxMinItem(candle, i,
					ICHIMOKU_KIJUN_PERIODS);
			kijun.add(item[MID]);
		}

		// 転換線
		TimeSeries tenkan = new TimeSeries("tenkan");
		for (int i = ICHIMOKU_TENKAN_PERIODS - 1; i < candle.getItemCount(); i++) {
			TimeSeriesDataItem[] item = getMaxMinItem(candle, i,
					ICHIMOKU_TENKAN_PERIODS);
			tenkan.add(item[MID]);
		}

		// 先行スパン1
		TimeSeries senko1 = new TimeSeries("senko1");
		// 基準線と転換線の中値を取る（基準線の方が短いはず）
		for (int i = 0; i < kijun.getItemCount(); i++) {
			double val = (kijun.getValue(i).doubleValue() + tenkan.getValue(i)
					.doubleValue()) / 2;
			senko1.add(kijun.getTimePeriod(i), val);
		}
		// 26日先行させる
		senko1 = addTimePeriod(senko1, ICHIMOKU_KIJUN_PERIODS);

		// 先行スパン2
		TimeSeries senko2 = new TimeSeries("senko2");
		for (int i = ICHIMOKU_KIJUN_PERIODS * 2 - 1; i < candle.getItemCount(); i++) {
			TimeSeriesDataItem[] item = getMaxMinItem(candle, i,
					ICHIMOKU_KIJUN_PERIODS * 2);
			senko2.add(item[MID]);
		}
		// 26日先行させる
		senko2 = addTimePeriod(senko2, ICHIMOKU_KIJUN_PERIODS);

		// 遅行線
		TimeSeries chiko = new TimeSeries("chiko");
		// ロウソク足の終値を登録
		for (int i = 0; i < candle.getItemCount(); i++) {
			OHLCItem tmp = (OHLCItem) candle.getDataItem(i);
			chiko.add(candle.getPeriod(i), tmp.getCloseValue());
		}
		// 26日遅行させる
		chiko = addTimePeriod(chiko, ICHIMOKU_KIJUN_PERIODS * -1);

		// TimeSeriesCollectionに追加(Rendererが違うので、先行スパン1、2は別のシリーズに)
		tsc[0].addSeries(kijun);
		tsc[0].addSeries(tenkan);
		tsc[1].addSeries(senko1);
		tsc[1].addSeries(senko2);
		tsc[0].addSeries(chiko);

		return tsc;
	}
	
	public static void updatePartOfIchimoku(OHLCSeries candle, TimeSeriesCollection[] tsc){
		TimeSeries kijun = tsc[0].getSeries(ICHIMOKU_KIJUN);
		TimeSeries tenkan = tsc[0].getSeries(ICHIMOKU_TENKAN);
		TimeSeries senko1 = tsc[1].getSeries(ICHIMOKU_SENKO1);
		TimeSeries senko2 = tsc[1].getSeries(ICHIMOKU_SENKO2);
		TimeSeries chiko = tsc[0].getSeries(ICHIMOKU_CHIKO);
		
		int candle_index = candle.getItemCount() - 1;

		// 基準線
		TimeSeriesDataItem[] item = getMaxMinItem(candle, candle_index,
				ICHIMOKU_KIJUN_PERIODS);
		kijun.addOrUpdate(item[MID].getPeriod(), item[MID].getValue().doubleValue());
		
		int kijun_index = kijun.getItemCount() - 1;

		// 転換線
		item = getMaxMinItem(candle, candle_index,
				ICHIMOKU_TENKAN_PERIODS);
		tenkan.addOrUpdate(item[MID].getPeriod(), item[MID].getValue().doubleValue());

		// 先行スパン1
		// 基準線と転換線の中値を取る（基準線の方が短いはず）
		double val = (kijun.getValue(kijun_index).doubleValue() + kijun.getValue(kijun_index)
				.doubleValue()) / 2;
		// 26日先行させる
		RegularTimePeriod prd = addTimePeriod(kijun.getTimePeriod(kijun_index), ICHIMOKU_KIJUN_PERIODS);
		senko1.addOrUpdate(prd, val);

		// 先行スパン2
		item = getMaxMinItem(candle, candle_index,
				ICHIMOKU_KIJUN_PERIODS * 2);
		// 26日先行させる
		prd = addTimePeriod(item[MID].getPeriod(), ICHIMOKU_KIJUN_PERIODS);
		senko2.addOrUpdate(prd,
				item[MID].getValue().doubleValue());

		// 遅行線
		// ロウソク足の終値を登録
		OHLCItem tmp = (OHLCItem) candle.getDataItem(candle_index);
		// 26日遅行させる
		prd = addTimePeriod(tmp.getPeriod(), ICHIMOKU_KIJUN_PERIODS * -1);
		chiko.addOrUpdate(prd, tmp.getCloseValue());
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

	public static TimeSeries addTimePeriod(TimeSeries series, int count) {
		TimeSeries ret = new TimeSeries("ret");
		RegularTimePeriod tmp = null;
		if (!series.isEmpty()) {
			for (int i = 0; i < series.getItemCount(); i++) {
				tmp = series.getTimePeriod(i);
				tmp = addTimePeriod(tmp, count);
				ret.add(tmp, series.getValue(i));
			}
		}
		ret.setKey(series.getKey());
		return ret;
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

	public static XYLineAndShapeRenderer getIchimokuRenderer() {
		XYLineAndShapeRenderer xyr = new XYLineAndShapeRenderer();
		xyr.setSeriesShapesVisible(ICHIMOKU_KIJUN, false); // shapeを非表示にする
		xyr.setSeriesStroke(ICHIMOKU_KIJUN, new BasicStroke(1, 1, 1));
		xyr.setSeriesShapesVisible(ICHIMOKU_TENKAN, false); // shapeを非表示にする
		xyr.setSeriesStroke(ICHIMOKU_TENKAN, new BasicStroke(1, 1, 1));
		xyr.setSeriesShapesVisible(ICHIMOKU_CHIKO, false);
		xyr.setSeriesStroke(ICHIMOKU_CHIKO, new BasicStroke(1, 1, 1));
		xyr.setSeriesPaint(ICHIMOKU_KIJUN, Color.CYAN); // 基準線はシアン
		xyr.setSeriesPaint(ICHIMOKU_TENKAN, Color.MAGENTA); // 転換線はマゼンタ
		xyr.setSeriesPaint(ICHIMOKU_CHIKO, Color.ORANGE); // 遅行線はオレンジ
		return xyr;
	}

	public static XYDifferenceRenderer getIchimokuKumoRenderer() {
		XYDifferenceRenderer xdr = new XYDifferenceRenderer(new Color(102, 204, 255, 51),
				new Color(255, 102, 204, 51), false);
		xdr.setBaseStroke(new BasicStroke(1, 1, 1));
		xdr.setSeriesPaint(ICHIMOKU_SENKO1, new Color(153, 255, 153, 102));
		xdr.setSeriesPaint(ICHIMOKU_SENKO2, new Color(153, 0, 153, 102));
		return xdr;
	}

}

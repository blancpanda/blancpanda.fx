package blancpanda.fx.chart;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.ohlc.OHLCSeries;
import org.jfree.data.time.ohlc.OHLCSeriesCollection;

import blancpanda.fx.CandleStick;
import blancpanda.fx.FXUtils;

public class FXChart extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 20090614L;
	
	private static JComboBox cmb_currency_pair;
	private static JComboBox cmb_period;
	
	/**
	 * データベースへの接続の有無
	 */
	private boolean db;
	
	private int max_visible;
	
	/**
	 * 時間間隔
	 */
	private int period;
	
	/**
	 * 通貨ペア
	 */
	private int currency_pair;
	
	/**
	 * X軸(時間)
	 */
	private DateAxis domain;

	/**
	 * 直前のピリオド
	 */
	private RegularTimePeriod pre_period;
	
	/**
	 * リアルタイム処理用CandleStick
	 */
	private CandleStick cs;
	
	/**
	 * ロウソク足
	 */
	private OHLCSeries candle;
		
	/**
	 * 単純移動平均
	 */
	TimeSeriesCollection tscSMA;
//	private TimeSeries[] sma;
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("リアルタイム為替チャート");
		FXChart panel = new FXChart(false, CandleStick.USDJPY, CandleStick.M5, 60); 
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		
		String[] str_currency_pair = {
				"USD/CAD",
				"EUR/JPY",
				"NZD/JPY",
				"GBP/CHF",
				"USD/CHF",
				"ZAR/JPY",
				"NZD/USD",
				"CAD/JPY",
				"EUR/GBP",
				"USD/JPY",	// 米ドル円
				"CHF/JPY",
				"GBP/JPY", 	// 英ポンド円
				"GBP/USD",
				"AUD/JPY",
				"EUR/USD",
				"AUD/USD"};
		cmb_currency_pair = new JComboBox(str_currency_pair);
		cmb_currency_pair.setSelectedIndex(CandleStick.USDJPY);
		cmb_currency_pair.addActionListener(panel.new ChartChanger());
		
		String[] str_period = {"M1", "M5", "M30", "H1", "H2"};
		cmb_period = new JComboBox(str_period);
		cmb_period.setSelectedIndex(CandleStick.M5);
		cmb_period.addActionListener(panel.new ChartChanger());
		
		JPanel header = new JPanel();
		header.add(cmb_currency_pair);
		header.add(cmb_period);
		frame.getContentPane().add(header, BorderLayout.NORTH);
		
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.setBounds( 10, 10, 600, 480);
	    frame.setVisible(true);
	    
	    Timer timer = panel.new DataGenerator(1000);
		timer.start();
	}
	
	public FXChart(boolean db, int currency_pair, int period, int max){
		super(new BorderLayout());
		this.db = db;
		this.max_visible = max;
		this.currency_pair = currency_pair;
		this.period = period;
		cs = new CandleStick(currency_pair, period);
		candle = new OHLCSeries("CandleStick");
		ChartPanel chart = new ChartPanel(createChart());
		add(chart);
	}
	
	private JFreeChart createChart() {
		// 定義
		JFreeChart jfreechart;
		XYPlot plot;
		NumberAxis range;
		OHLCSeriesCollection osc;
		CandlestickRenderer cr;
		XYLineAndShapeRenderer xyr;
		SegmentedTimeline fxtimeline;
		
		// プロットの作成
		plot = new XYPlot();
		
		// X軸
		domain = new DateAxis(""); // Time
		// 取引時間に対応
		fxtimeline = FXUtils.getFXTimeline();
		domain.setTimeline(fxtimeline);
		
		// Y軸
		range = new NumberAxis(""); // Price
		// 0を含まずに自動調整
		range.setAutoRangeIncludesZero(false);
		
		// プロットに軸を追加
		plot.setDomainAxis(0, domain);
		plot.setRangeAxis(0, range);
		
		// 各データの作成
		// ロウソク足データ
		if(db){
			candle = FXChartUtils.loadCandleStick(candle, currency_pair, period);
		}
		osc = new OHLCSeriesCollection();
		osc.addSeries(candle);
		
		// テクニカル指標
		tscSMA = FXChartUtils.getSimpleMovingAverage(candle);
		
		// プロットにデータを追加
		plot.setDataset(0, osc);
		plot.setDataset(1, tscSMA);
		
		// データと軸の対応関係を設定
		plot.mapDatasetToDomainAxis(0, 0);
		plot.mapDatasetToRangeAxis(0, 0);
		plot.mapDatasetToDomainAxis(1, 0);
		plot.mapDatasetToRangeAxis(1, 0);		
		
		// 見た目の設定
		// ロウソク足
		cr = FXChartUtils.getCandleStickRenderer();
		plot.setRenderer(0, cr);

		xyr = FXChartUtils.getSimpleMovingAverateRenderer();
		plot.setRenderer(1, xyr);
		
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		
		jfreechart = new JFreeChart(null, null, plot, false);
		
		return jfreechart;
	}
	
	@SuppressWarnings("unchecked")
	private void updateSeries(){
		Date date = cs.getCurrentRate();
		// なぜか取得日時が逆戻りするので、戻った場合には何もしない。
		RegularTimePeriod prd = FXUtils.getRegularTimePeriod(date, period);
		if (prd.compareTo(pre_period) >= 0) {
			int index = candle.indexOf(prd);
			if (index >= 0) {
				candle.remove(prd);
			} else {
				cs.clearRate();
			}
			candle.add(prd, cs.getBid_open(), cs.getBid_high(),
					cs.getBid_low(), cs.getBid_close());
			// 移動平均
			tscSMA = FXChartUtils.updatePartOfSMA(candle, tscSMA);
			// 時間の表示範囲
			domain.setMinimumDate(FXUtils.calcDateAxisMin(prd.getEnd(), period, max_visible));
			domain.setMaximumDate(prd.getEnd());
			
			pre_period = prd;
		}	
	}
	
	private void updateChart() {
		boolean changed = false;
		if(period != cmb_period.getSelectedIndex()){
			// ピリオドが変わった
			period = cmb_period.getSelectedIndex();
			changed = true;
		}
		if(currency_pair != cmb_currency_pair.getSelectedIndex()){
			// 通貨ペアが変わった
			currency_pair = cmb_currency_pair.getSelectedIndex();
			changed = true;
		}
		if(changed){
			// ピリオドか通貨ペアが変わったら
			// CandleStickを書き換える
			cs = new CandleStick(currency_pair, period);
			// ロウソク足を全部消す
			candle.clear();
			if(db){
				// DBから読み込み直す
				candle = FXChartUtils.loadCandleStick(candle, currency_pair, period);
			}
			// 移動平均
			tscSMA = FXChartUtils.updateAllOfSMA(candle, tscSMA);
			// 時間の表示範囲
			if(candle.getItemCount() >= 1){
				domain.setMinimumDate(FXUtils.calcDateAxisMin(candle.getPeriod(candle.getItemCount() - 1).getEnd(), period, max_visible));
				domain.setMaximumDate(candle.getPeriod(candle.getItemCount() - 1).getEnd());
			}
			
			changed = false;
		}
	}


	class DataGenerator extends Timer implements ActionListener {

		public DataGenerator(int delay) {
			super(delay, null);
			addActionListener(this);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 20090615L;

		public void actionPerformed(ActionEvent e) {
			// 各Seriesを更新
			updateSeries();	
		}
		
	}
	
	class ChartChanger implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			// チャートを更新
			updateChart();
		}
		
	}
}

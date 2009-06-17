package blancpanda.fx.log;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.hibernate.Transaction;
import org.jfree.data.time.RegularTimePeriod;

import blancpanda.fx.CandleStick;
import blancpanda.fx.CandleStickDao;
import blancpanda.fx.FXUtils;

public class FXLogger {

	/**
	 * 各通貨ペア、ピリオドのリアルタイム処理用CandleStick
	 */
	private CandleStick[][] cs;

	/**
	 * 各通貨ペア、ピリオドのDB登録用CandleStick
	 */
	private CandleStick[][] insert_cs;

	/**
	 * 直前のRegularTimePeriod
	 */
	private RegularTimePeriod[] pre_period;

	// 通貨ペアは16種類
	private static final int CURRENCY_PAIRS = 16;
	// ピリオドは5種類
	private static final int PERIODS = 5;

	private static JTextPane console;

	public FXLogger() {
		Date date = new Date();
		// CandleStickの生成
		cs = new CandleStick[CURRENCY_PAIRS][PERIODS];
		insert_cs = new CandleStick[CURRENCY_PAIRS][PERIODS];
		pre_period = new RegularTimePeriod[PERIODS];
		for (int c = 0; c < 16; c++) {
			for (int p = 0; p < 5; p++) {
				cs[c][p] = new CandleStick(c, p);
			}
		}
		// pre_prdの初期化
		for (int p = 0; p < 5; p++) {
			// 現在日付で初期化
			pre_period[p] = FXUtils.getRegularTimePeriod(date, p);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		console = new JTextPane();
		JScrollPane sp = new JScrollPane(console);
		JFrame frame = new JFrame("リアルタイム為替レートログ");
		frame.getContentPane().add(sp, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(10, 10, 480, 320);
		frame.setVisible(true);
		printlog("レートの取得を開始");
		FXLogger fxlogger = new FXLogger();

		Timer timer = fxlogger.new DataGenerator(1000);
		timer.start();
	}

	private static void printlog(String str) {
		Document doc = console.getDocument();
		try {
			doc.insertString(doc.getLength(), new Date() + " : " + str + "\n",
					null);
			console.setCaretPosition(doc.getLength());
		} catch (BadLocationException e) {
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
			insertOrUpdateCandleStick();
		}
		
		@SuppressWarnings("unchecked")
		private void insertOrUpdateCandleStick(){
			CandleStickDao csDao = new CandleStickDao();
			RegularTimePeriod[] prd = new RegularTimePeriod[PERIODS];
			// 各CandleStickを更新
			HashMap map = FXUtils.getRateData();
			for (int p = 0; p < 5; p++) {
				for (int c = 0; c < 16; c++) {
					prd[p] = cs[c][p].getCurrentRate(map);
					if (prd[p].compareTo(pre_period[p]) > 0) {
						if (insert_cs[c][p] != null) {
							// DBに登録
							Transaction transaction = csDao.getSession()
							.beginTransaction();
							csDao.save(insert_cs[c][p]);
							
							// [重要] 分離オブジェクトにする ***************
							csDao.getSession().flush();
							csDao.getSession().evict(insert_cs[c][p]);
							//****************************************
							
							transaction.commit();
							printlog("Insert:" + insert_cs[c][p].getCsid());
						}
						// 次のピリオドに移行(レートを終値で初期化)
						cs[c][p].clearRate();
					}
					// 登録用のCandleStickに待避
					insert_cs[c][p] = cs[c][p];
				}
				if (prd[p].compareTo(pre_period[p]) > 0) {
					pre_period[p] = prd[p];
				}
			}
		}
	}
}

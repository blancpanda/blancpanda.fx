/**
 * 
 */
package blancpanda;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * @author Kaoru
 * 
 */
public class IndeterminateProgressBar extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JLabel msg;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		IndeterminateProgressBar progress = new IndeterminateProgressBar();
		progress.showProgress("チャートを更新しています");
	}
	
	public IndeterminateProgressBar(){
		super();
		UIManager.put("ProgressBar.repaintInterval", new Integer(100));
		UIManager.put("ProgressBar.cycleTime", new Integer(500));
		JProgressBar aJProgressBar = new JProgressBar(0, 100);
		aJProgressBar.setIndeterminate(true);
		Border border = new EmptyBorder( 10, 10, 10, 10);
		aJProgressBar.setBorder(border);
		msg = new JLabel("");
		border = new EmptyBorder( 10, 10, 10, 10);
		msg.setBorder(border);
		JPanel header = new JPanel();
		header.add(msg, BorderLayout.CENTER);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Container contentPane = getContentPane();
		contentPane.add(aJProgressBar, BorderLayout.CENTER);
		contentPane.add(header, BorderLayout.NORTH);
		setSize(300, 100);
		setVisible(false);
	}
	
	public void showProgress(final String text){
		 //非同期処理
        EventQueue.invokeLater(new Runnable() {
            public void run() {
        		setVisible(true);
        		msg.setText(text);
            }
        }); 
	}
	
	public void hideProgress(){
		setVisible(false);
	}
}

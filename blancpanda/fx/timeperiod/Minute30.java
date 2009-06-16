package blancpanda.fx.timeperiod;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.jfree.data.time.Day;
import org.jfree.data.time.RegularTimePeriod;

/*
 * 30分刻みのTimePeriod
 * 0-29,30-59
 */
public class Minute30 extends RegularTimePeriod implements Serializable {

	private static final long serialVersionUID = 20090612L;
	private Date date;
	private Calendar cal;
	private long firstMillisecond;
	private long lastMillisecond;
	
	private Day day;
    private byte hour;
    private byte minute;

	public Minute30(Date date) {
		this.date = date;
		cal = Calendar.getInstance();
		cal.setTime(date);
		peg(cal);
		cal.setTimeInMillis(getFirstMillisecond());
		minute = (byte)cal.get(Calendar.MINUTE);
		hour = (byte)cal.get(Calendar.HOUR_OF_DAY);
		day = new Day(date);
	}

	private long calcStart(Calendar cal) {
		long start;
		cal.setTime(date);
		if (0 <= cal.get(Calendar.MINUTE) && cal.get(Calendar.MINUTE) <= 29) {
			cal = getStartDate(cal, 0);
		} else {
			cal = getStartDate(cal, 30);
		}
		start = cal.getTimeInMillis();
		return start;
	}

	private long calcEnd(Calendar cal) {
		long end;
		cal.setTime(date);
		if (0 <= cal.get(Calendar.MINUTE) && cal.get(Calendar.MINUTE) <= 29) {
			cal = getEndDate(cal, 29);
		} else {
			cal = getEndDate(cal, 59);
		}
		end = cal.getTimeInMillis();
		return end;
	}

	private Calendar getStartDate(Calendar cal, int minute) {
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	private Calendar getEndDate(Calendar cal, int minute) {
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 999);
		return cal;
	}

	@Override
	public long getFirstMillisecond() {
		return firstMillisecond;
	}

	@Override
	public long getFirstMillisecond(Calendar cal) {
		long first = this.calcStart(cal);
		return first;
	}

	@Override
	public long getLastMillisecond() {
		return lastMillisecond;
	}

	@Override
	public long getLastMillisecond(Calendar cal) {
		long last = this.calcEnd(cal);
		return last;
	}

	@Override
	public long getSerialIndex() {
        long hourIndex = this.day.getSerialIndex() * 24L + this.hour;
        return hourIndex * (long)(60 / 30) + (long)(this.minute / 30);
	}

	@Override
	public RegularTimePeriod next() {
		cal.setTime(date);
		cal.add(Calendar.MINUTE, 30);
		return new Minute30(cal.getTime());
	}

	@Override
	public void peg(Calendar cal) {
		this.firstMillisecond = getFirstMillisecond(cal);
		this.lastMillisecond = getLastMillisecond(cal);
	}

	@Override
	public RegularTimePeriod previous() {
		cal.setTime(date);
		cal.add(Calendar.MINUTE, -30);
		return new Minute30(cal.getTime());
	}

	public int compareTo(Object obj) {
		int result;

		// CASE 1 : 他のMinute30との比較
		// -------------------------------------------
		if (obj instanceof Minute30) {
			Minute30 m30 = (Minute30) obj;
			if (this.getFirstMillisecond() == m30.getFirstMillisecond()
					&& this.getLastMillisecond() == m30.getLastMillisecond()) {
				result = 0;
			} else {
				result = (int) (this.getLastMillisecond() - m30.getLastMillisecond());
			}
		}

		// CASE 2 : 他のTimePeriodオブジェクトとの比較
		// -----------------------------------------------
		else if (obj instanceof RegularTimePeriod) {
			// 難しいので保留
			result = 0;
		}

		// CASE 3 : TimePeriodオブジェクトではないものと比較
		// ---------------------------------------------
		else {
			// time period は他の一般的なオブジェクトの後にくると考える
			result = 1;
		}

		return result;
	}
}

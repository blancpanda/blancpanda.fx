package blancpanda;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Sessionを安全に使うためのHibernate Sessionユーティリティクラス
 *
 * @author: http://www.syboos.jp
 */
public class HibernateUtils {
	private static SessionFactory sessionThreadLocalFactory = null;

	/**
	 * <code>sessionThreadLocal</code>
	 */
	@SuppressWarnings("unchecked")
	private static final ThreadLocal sessionThreadLocal = new ThreadLocal();

	private HibernateUtils() {

	}

	/**
	 * 初期化処理
	 * （アプリケーションスコープで一回しか呼ばない、明示的に呼ばれたことがなかった場合、currentSession()から自動的に呼ばれます）
	 *
	 * @throws HibernateException
	 */
	public static void initialize() throws HibernateException {
		_init(null);
	}

	/**
	 * initialize the Hibernate environment
	 *
	 * @throws HibernateException
	 */
	public static void initialize(String resource) throws HibernateException {
		_init(resource);
	}

	// リソースからSessionFactoryを取得する（アプリケーションスコープで一回しか呼ばない）
	private static void _init(String resource) throws HibernateException {
		try {
			// Create the SessionFactory
			if (resource != null && !resource.equals("")) {
				sessionThreadLocalFactory = new Configuration().configure(
						resource).buildSessionFactory();
			} else {
				sessionThreadLocalFactory = new Configuration().configure()
						.buildSessionFactory();
			}

		} catch (Throwable ex) {
			throw new HibernateException(
					"Can't build hibernation SessionFactory", ex);
		} // end of the try - catch block
	}

	/**
	 * スレッド内の一意Sessionインスタンスを取得する
	 *
	 * @return スレッド内で共有されるSessionのインスタンス
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static Session currentSession() throws HibernateException {

		if (sessionThreadLocalFactory == null)
			initialize();

		// ThreadLocal変数からSessionを取得する
		Session s = (Session) sessionThreadLocal.get();

		// Open a new Session, if this Thread has none yet
		if (s == null) {
			s = sessionThreadLocalFactory.openSession();
			sessionThreadLocal.set(s);
		}
		return s;
	}

	/**
	 * Sessionを閉じる
	 *
	 * @throws HibernateException
	 */
	@SuppressWarnings("unchecked")
	public static void closeSession() throws HibernateException {
		if (sessionThreadLocalFactory == null || sessionThreadLocal == null)
			return;

		Session s = (Session) sessionThreadLocal.get();
		sessionThreadLocal.set(null);
		if (s != null)
			s.close();
	} // end of the method

} // end of the class

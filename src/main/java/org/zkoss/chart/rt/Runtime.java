package org.zkoss.chart.rt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.prefs.Preferences;

import javax.security.auth.x500.X500Principal;

import org.zkoss.chart.Charts;
import org.zkoss.chart.lic.CipherParam;
import org.zkoss.chart.lic.KeyStoreParam;
import org.zkoss.chart.lic.LicenseContent;
import org.zkoss.chart.lic.LicenseParam;
import org.zkoss.chart.lic.util.ObfuscatedString;
import org.zkoss.io.Files;
import org.zkoss.lang.Library;
import org.zkoss.lang.Strings;
import org.zkoss.util.Dates;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.DesktopCleanup;
import org.zkoss.zk.ui.util.DesktopInit;
import org.zkoss.zk.ui.util.EventInterceptor;
import org.zkoss.zk.ui.util.ExecutionInit;
import org.zkoss.zul.Window;

public class Runtime {
	static final String ACTIVE_CODE = new ObfuscatedString(
			new long[]{2927261568102559820L, 5003116366233426486L, -19977163214018644L}).toString();
	static final String LICENSE_SUBJECT = new ObfuscatedString(
			new long[]{3902422651215371349L, 8624385350820619417L, -642393240400619653L}).toString();
	static final String UP_TIME = "1";
	static final String SESSION_COUNT = new ObfuscatedString(
			new long[]{3229559844559651902L, 4507837234696327046L, 7727269190283665611L}).toString();
	static final String USER_NAME = new ObfuscatedString(
			new long[]{-7156956517562311570L, -4569457626895068567L, -9130454531809692735L}).toString();
	static final String COMPANY_ID = new ObfuscatedString(
			new long[]{-3429614419320347732L, 5284083613633888871L, -2962259370126176133L}).toString();
	static final String COMPANY_UNIT = new ObfuscatedString(
			new long[]{4535911707703337843L, -5339041739265159617L, -8006648055853745059L}).toString();
	static final String COMPANY_NAME = new ObfuscatedString(
			new long[]{-7340139527016707886L, -5203243759892677212L, 8714822623115369524L}).toString();
	static final String COMPANY_CITY = new ObfuscatedString(
			new long[]{-6925611054058810462L, 2143563004714040551L, 1994584055053707741L}).toString();
	static final String COMPANY_ADDRESS = new ObfuscatedString(
			new long[]{-8691848786421899489L, -4370996558863340632L, -2709933490946981238L}).toString();
	static final String COMPANY_ZIPCODE = new ObfuscatedString(
			new long[]{-4934501068656857753L, -6913828373145765012L, 1063193634233753528L}).toString();
	static final String COUNTRY = new ObfuscatedString(new long[]{-4504969373906269801L, 5248137891553083335L})
			.toString();
	static final String PROJECT_NAME = new ObfuscatedString(
			new long[]{830623621279886032L, 459360910074040759L, 4178520520701877821L}).toString();
	static final String PRODUCT_NAME = new ObfuscatedString(
			new long[]{5504882338648710617L, -4761731334749763195L, 996375918662338065L}).toString();
	static final String PACKAGE = new ObfuscatedString(new long[]{-8439029564924938530L, -4878278112849633009L})
			.toString();
	static final String VERSION = new ObfuscatedString(new long[]{-4847689528984584834L, 2493253216426408014L})
			.toString();
	static final String ISSUE_DATE = new ObfuscatedString(
			new long[]{-4228764154858292882L, -6898004159031332466L, 6328666951570048917L}).toString();
	static final String EXPIRY_DATE = new ObfuscatedString(
			new long[]{-7233890858958970371L, -3423973165832030856L, -5810612950970282077L}).toString();
	static final String TERM = new ObfuscatedString(new long[]{-6725301182108235475L, -8691110408124621856L})
			.toString();
	static final String VERIFICATION_NUMBER = new ObfuscatedString(
			new long[]{3823288740853721680L, -6436340937747658512L, 891079956768101415L, 751513662528431611L})
					.toString();
	static final String INFORMATION = new ObfuscatedString(
			new long[]{378870925371295609L, 1418863983102047429L, -5017007170548372422L}).toString();
	static final String KEY_SIGNATURE = new ObfuscatedString(
			new long[]{-2573177027008659676L, 5066716785755217927L, 5769746383701090690L}).toString();
	static final String CHECK_PERIOD = new ObfuscatedString(
			new long[]{-2439022525501632135L, 6139476070014855270L, -297657911147084449L}).toString();
	static final String LICENSE_VERSION = new ObfuscatedString(
			new long[]{-7080462743270045357L, 6928867389785115158L, -154565539896996742L}).toString();
	static final String WARNING_EXPIRY ="2";
	static final String WARNING_PACKAGE = "3";
	static final String WARNING_VERSION = "4";
	static final String WARNING_COUNT = new ObfuscatedString(
			new long[]{-1510608780643214737L, 6313704540210276937L, 4115504365890483558L}).toString();
	static final String WARNING_NUMBER = new ObfuscatedString(
			new long[]{8367990676393660796L, -7163797910637480555L, -8349581027556623805L}).toString();
	public static final String WARNING_EVALUATION = "5";
	static final String ZKCHARTS = new ObfuscatedString(
			new long[]{7687308498378989512L, 3363155612692898862L, -4411539046136182894L}).toString();
	private static final String PUB_STORE = new ObfuscatedString(
			new long[]{-467598638084582726L, 6535119678915292211L, -5761942064796554413L, -2140330853237991898L})
					.toString();
	private static final String ALIAS = new ObfuscatedString(new long[]{-2751357802016299199L, 4066217211348802619L,
			-2064662869185498634L, 4043793295118034741L, 6227175189710674534L}).toString();
	private static final String STORE_PASS = new ObfuscatedString(new long[]{-8677088790027852212L,
			4602056908258993522L, 8019503246186939872L, -1944004741470673738L, -7033589056015316549L}).toString();
	private static final ThreadLocal<Boolean> _pass = new ThreadLocal();
	private static boolean _ck;
	private static LicenseParam _licenseParam;
	private static KeyStoreParam _keystoreParam;
	private static CipherParam _cipherParam;
	private static final RuntimeLicenseManager _licManager;
	private static WebApp _wapp;
	private static final String V0;
	private static final String V1;
	private static final String MD5STR;
	public static final String EVAL_ONLY;
	static final String UNIVERSAL_ACTIVE_CODE;
	static final String ZK_NOTICE;
	static final String LICENSE_DIRECTORY_PROPERTY;
	private static final String DEFAULT_LICENSE_DIRECTORY;

	private static final String read(String path) {
		try {
			InputStream is = Runtime.class.getResourceAsStream("/metainfo/chart/" + path);
			if (is != null) {
				return new String(Files.readAll((InputStream) is));
			}
		} catch (Throwable is) {
			// empty catch block
		}
		return null;
	}

	public static final boolean init(WebApp wapp, boolean ck) {
		boolean b = true;
		if (ck && !_ck) {
			boolean hasLicenseFieOrFolder;
			_ck = true;
			boolean charts = "ZK Charts".equals(Runtime.read("zkcharts"));
			boolean init = true;
			boolean bl = hasLicenseFieOrFolder = Library.getProperty((String) LICENSE_DIRECTORY_PROPERTY) != null
					|| Runtime.class.getResource(DEFAULT_LICENSE_DIRECTORY) != null;
			if (charts && !hasLicenseFieOrFolder) {
				init = false;
			}
			if (init) {
				wapp.setAttribute(ACTIVE_CODE, (Object) Runtime.getActiveCode());
				try {
					b = Runtime.init1(wapp);
					wapp.getConfiguration().addListener(Init.class);
					b = b && _pass.get() != false;
					_pass.remove();
				} catch (Exception e) {
					b = false;
				}
			}
			if (charts) {
				wapp = null;
			}
			if (!b && wapp != null) {
				wapp.setAttribute(ZK_NOTICE, (Object) Runtime.getEvalNotice(wapp));
			}
		}
		return b;
	}

	private static final String getActiveCode() {
		Preferences pref = _licenseParam.getPreferences();
		if (pref != null) {
			long leastv = pref.getLong(V0, 0L);
			long mostv = pref.getLong(V1, 0L);
			if (leastv == 0L && mostv == 0L) {
				UUID uuid = UUID.randomUUID();
				long most = uuid.getMostSignificantBits();
				long least = uuid.getLeastSignificantBits();
				pref.putLong(V0, least);
				pref.putLong(V1, most);
				if (least == pref.getLong(V0, 0L) && most == pref.getLong(V1, 0L)) {
					return Runtime.uuidToMD5(most, least);
				}
			} else {
				return Runtime.uuidToMD5(mostv, leastv);
			}
		}
		return null;
	}

	private static final String uuidToMD5(long most, long least) {
		String hostname;
		hostname = null;
		try {
			Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
			while (nis.hasMoreElements()) {
				InetAddress ia;
				NetworkInterface ni = nis.nextElement();
				if (!ni.isUp() || ni.isVirtual())
					continue;
				Enumeration<InetAddress> ias = ni.getInetAddresses();
				while (ias.hasMoreElements() && "localhost".equals(hostname = (ia = ias.nextElement()).getHostName())) {
					hostname = null;
				}
				if (hostname == null)
					continue;
				break;
			}
		} catch (SocketException nis) {
			// empty catch block
		}
		try {
			int j;
			long mostsalt2 = -8556922120573852888L;
			long leastsalt = -7110043422976597898L;
			if (hostname == null) {
				hostname = "ZK Host";
			}
			byte[] hostbytes = hostname.getBytes("UTF-8");
			long host = 0L;
			for (j = 0; j < hostbytes.length && j < 8; ++j) {
				host |= ((long) hostbytes[j] & 255L) << j * 8;
			}
			leastsalt ^= host;
			if (hostbytes.length > 8) {
				host = 0L;
				int len = hostbytes.length - 8;
				for (j = 0; j < len && j < 8; ++j) {
					host |= ((long) hostbytes[j + 8] & 255L) << j * 8;
				}
				mostsalt2 ^= host;
			}
			String uuidStr = new UUID(most ^ mostsalt2, least ^ leastsalt).toString();
			MessageDigest digest = MessageDigest.getInstance(MD5STR);
			if (digest != null) {
				digest.reset();
				digest.update(uuidStr.getBytes("UTF-8"));
				byte[] digested = digest.digest();
				BigInteger bigInt = new BigInteger(1, digested);
				String result = bigInt.toString(36);
				StringBuffer sb = new StringBuffer(32);
				for (int j2 = 0; j2 < result.length(); ++j2) {
					if (j2 > 0 && j2 % 5 == 0) {
						sb.append("-");
					}
					sb.append(result.charAt(j2));
				}
				return sb.toString().toUpperCase();
			}
		} catch (UnsupportedEncodingException mostsalt2) {
		} catch (NoSuchAlgorithmException mostsalt2) {
			// empty catch block
		}
		return null;
	}

	static String getEvalNotice(WebApp wapp) {
		StringBuffer sb = new StringBuffer();
		Object o = wapp.getAttribute(ZK_NOTICE);
		if (o != null) {
			sb.append(o);
		}
		sb.append(" -->\n").append("<!-- ").append(ZKCHARTS).append(" ").append("7.2.1.1").append(" ");
		sb.append(EVAL_ONLY);
		return sb.toString();
	}

	private static final boolean init1(WebApp wapp) {
		_wapp = wapp;
		String dir = Library.getProperty((String) LICENSE_DIRECTORY_PROPERTY);
		boolean isScheduled = false;
		if (dir != null) {
			isScheduled = _licManager.install(dir);
		} else {
			URL url = Runtime.class.getResource(DEFAULT_LICENSE_DIRECTORY);
			if (url != null) {
				isScheduled = _licManager.install(url.getFile());
			}
		}
		if (isScheduled) {
			_licManager.setWapp(wapp);
			_licManager.startScheduler();
			return true;
		}
		Runtime.init1().init2(null);
		return false;
	}

	private static Init1 init1() {
		return Init1._init1;
	}

	public static final void token(Object exec, Object exec2) {
		Runtime.token0(exec, exec2);
	}

	public static final boolean token(Object exec) {
		return Runtime.token0(exec);
	}

	private static final boolean token0(Object exec) {
		if (exec instanceof Execution) {
			return ((Execution) exec).getAttribute(Runtime.token1(exec)) != null;
		}
		if (exec instanceof Session) {
			return ((Session) exec).getAttribute(Runtime.token1(exec)) != null;
		}
		return false;
	}

	private static final void token0(Object exec, Object exec2) {
		if (exec instanceof Execution) {
			((Execution) exec2).setAttribute(Runtime.token1(exec), (Object) Boolean.TRUE);
		} else if (exec instanceof Session) {
			((Session) exec2).setAttribute(Runtime.token1(exec), (Object) Boolean.TRUE);
		}
	}

	private static final String token1(Object exec) {
		Desktop desktop;
		MessageDigest digest;
		String plaintext = "mgws"
				+ (exec instanceof Execution
						? ((Execution) exec).getDesktop().getId()
						: (exec instanceof Desktop ? ((Desktop) exec).getId() : ((Session) exec).getRemoteAddr()))
				+ "wysb";
		Execution exec0 = Executions.getCurrent();
		if (exec0 != null && (digest = (MessageDigest) (desktop = exec0.getDesktop())
				.getAttribute("md_" + desktop.getId())) != null) {
			digest.reset();
			try {
				digest.update(plaintext.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException unsupportedEncodingException) {
				// empty catch block
			}
			byte[] digested = digest.digest();
			BigInteger bigInt = new BigInteger(1, digested);
			return bigInt.toString(36);
		}
		return plaintext;
	}

	private static int stringHash(String value) {
		if (value == null) {
			return 0;
		}
		int off = 0;
		int h = 0;
		int len = value.length();
		for (int i = 0; i < len; ++i) {
			h = 31 * h + value.charAt(off++);
		}
		return h;
	}

	public static long getCheckSum(RuntimeInfo result) {
		long checksum = 0L;
		X500Principal hobj = result.getHolder();
		checksum += hobj == null ? 0L : (long) Runtime.stringHash(hobj.getName());
		X500Principal iobj = result.getIssuer();
		checksum += iobj == null ? 0L : (long) Runtime.stringHash(iobj.getName());
		int count = result.getConsumerAmount();
		checksum += (long) count;
		String type = result.getConsumerType();
		checksum += (long) Runtime.stringHash(type);
		String info = result.getInfo();
		checksum += (long) Runtime.stringHash(info);
		Date issued = result.getIssued();
		checksum += issued.getTime();
		Date start = result.getNotBefore();
		checksum += start == null ? 0L : start.getTime();
		Date end = result.getNotAfter();
		checksum += end == null ? 0L : end.getTime();
		String subject = result.getSubject();
		checksum += (long) Runtime.stringHash(subject);
		String licId = result.getLicId();
		checksum += (long) Runtime.stringHash(licId);
		String licVer = result.getLicVer();
		checksum += (long) Runtime.stringHash(licVer);
		String userId = result.getUserId();
		checksum += (long) Runtime.stringHash(userId);
		String userName = result.getUserName();
		checksum += (long) Runtime.stringHash(userName);
		String companyId = result.getCompanyId();
		checksum += (long) Runtime.stringHash(companyId);
		String companyName = result.getCompanyName();
		checksum += (long) Runtime.stringHash(companyName);
		long sessionCount = result.getSessionCount();
		checksum += sessionCount;
		String zkchartsVer = result.getZkChartsVer();
		return checksum += (long) Runtime.stringHash(zkchartsVer);
	}

	static {
		_licenseParam = new LicenseParam() {

			public String getSubject() {
				return Runtime.ZKCHARTS;
			}

			public Preferences getPreferences() {
				return null;
			}

			public KeyStoreParam getKeyStoreParam() {
				return _keystoreParam;
			}

			public CipherParam getCipherParam() {
				return _cipherParam;
			}
		};
		_keystoreParam = new KeyStoreParam() {

			public InputStream getStream() throws IOException {
				InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(PUB_STORE);
				if (in == null) {
					throw new FileNotFoundException(PUB_STORE);
				}
				return in;
			}

			public String getAlias() {
				return ALIAS;
			}

			public String getStorePwd() {
				return STORE_PASS;
			}

			public String getKeyPwd() {
				return null;
			}
		};
		_cipherParam = new CipherParam() {

			public String getKeyPwd() {
				return new ObfuscatedString(new long[]{-9017617134232705315L, -3067316756544620689L,
						-7174741455541659722L, 9223059116147577819L, -7389013047307896124L}).toString();
			}
		};
		_licManager = RuntimeLicenseManager.getInstance((LicenseParam) _licenseParam, (Refresh) new Refresh() {

			public boolean checkVersion(Map<String, Object> map) {
				String version = (String) map.get(Runtime.VERSION);
				return Strings.isBlank((String) version) || "7.2.1.1".startsWith(version);
			}

			public boolean isTargetSubject(Map<String, Object> map) {
				String subject = (String) map.get(Runtime.LICENSE_SUBJECT);
				return Runtime.ZKCHARTS.equals(subject);
			}

			public Object refresh(List<LicenseContent> contents) {
				Runtime.init1().init2(contents);
				this.printInfo(contents);
				return null;
			}

			private void printInfo(List<LicenseContent> contents) {
				for (int i = 0; i < contents.size(); ++i) {
					Map map;
					LicenseContent lc = contents.get(i);
					Object mapObj = lc.getExtra();
					if (!(mapObj instanceof Map) || !this.isTargetSubject(map = (Map) mapObj))
						continue;
					this.info((String) map.get(Runtime.INFORMATION));
				}
			}

			private void info(String msg) {
				RuntimeLicenseManager.info((String) msg);
			}
		});
		V0 = new ObfuscatedString(new long[]{6780048183396145217L, -3514785424911510459L}).toString();
		V1 = new ObfuscatedString(new long[]{-8556922120573852888L, 708543790670807158L}).toString();
		MD5STR = new ObfuscatedString(new long[]{-5121064899839768052L, 3334273322282769989L}).toString();
		EVAL_ONLY = new ObfuscatedString(new long[]{8330515038476062730L, -435229286498387605L, -3853053404258091660L})
				.toString();
		UNIVERSAL_ACTIVE_CODE = new ObfuscatedString(new long[]{-1128797753529339020L, -733110981657037353L,
				4491611533734142717L, -1250450028158659062L, -1301458517696305400L, -5349951335757801720L}).toString();
		ZK_NOTICE = new ObfuscatedString(
				new long[]{-8347842002405430398L, 3574087500300642980L, -6273607823570371127L, -5524402949239665762L})
						.toString();
		LICENSE_DIRECTORY_PROPERTY = new ObfuscatedString(new long[]{6736365164940027295L, -1302475928255385955L,
				-7342664994090783108L, 4397684651610525166L, -3739603956555900055L, -6887437132615268471L}).toString();
		DEFAULT_LICENSE_DIRECTORY = new ObfuscatedString(
				new long[]{7798525131307045020L, 9120904666555939754L, -7964755973142850032L, -422867193761513395L})
						.toString();
	}

	static final class LicenseEvent extends Event {
		private static final long serialVersionUID = 201011241551L;

		private LicenseEvent(Event event) {
			super("onLicense", event.getTarget());
		}
	}

	private static abstract class Init0 implements ExecutionInit, DesktopInit, DesktopCleanup, EventInterceptor {
		private Init0() {
			this.init1();
		}

		protected final void init0(Desktop desktop, Object request) throws Exception {
			this.init1().init0(desktop, request);
		}

		protected final void cleanup0(Desktop desktop) throws Exception {
			this.init1().cleanup0(desktop);
		}

		protected final void init2(Execution exec, Execution parent) throws Exception {
			this.init1().init2(exec, parent);
		}

		private final Init1 init1() {
			return Init1._init1;
		}

		protected final void afterProcessEvent0(Event event) {
			this.init1().afterProcessEvent0(event);
		}

		protected final Event beforePostEvent0(Event event) {
			return this.init1().beforePostEvent0(event);
		}

		protected final Event beforeProcessEvent0(Event event) {
			return this.init1().beforeProcessEvent0(event);
		}

		protected final Event beforeSendEvent0(Event event) {
			return this.init1().beforeSendEvent0(event);
		}
	}

	public static final class Init extends Init0 {
		public void init(Desktop desktop, Object request) throws Exception {
			this.init0(desktop, request);
		}

		public void cleanup(Desktop desktop) throws Exception {
			this.cleanup0(desktop);
		}

		public void init(Execution exec, Execution parent) throws Exception {
			this.init2(exec, parent);
		}

		public void afterProcessEvent(Event event) {
			this.afterProcessEvent0(event);
		}

		public Event beforePostEvent(Event event) {
			return this.beforePostEvent0(event);
		}

		public Event beforeProcessEvent(Event event) {
			return this.beforeProcessEvent0(event);
		}

		public Event beforeSendEvent(Event event) {
			return this.beforeSendEvent0(event);
		}
	}

	private static final class Init2 {
		private static final BigInteger _pkey = new BigInteger("123");
		private static final String EVAL_LIC_ID = new ObfuscatedString(
				new long[]{-2799362229550136139L, -8991339801923478651L, 2054766831045471090L}).toString();
		private static final String EVAL_LIC_VER = new ObfuscatedString(
				new long[]{4515837249784318634L, -483965117545718936L}).toString();
		private static final String EVAL_USER_NAME = new ObfuscatedString(
				new long[]{6700787382705499563L, 2359114071391082446L, 1949693015831576717L}).toString();
		private static final String EVAL_COMPANY_ID = new ObfuscatedString(
				new long[]{8997629646135421336L, -7459597106373371040L, 3324566901651534592L}).toString();
		private static final String EVAL_COMPANY_NAME = new ObfuscatedString(
				new long[]{-7105953592593293452L, -8116579615415252408L, -3117127378177258899L, 9036340616621476990L})
						.toString();
		private SimpleDateFormat _dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
		private Map<String, Object> _keys = new HashMap<String, Object>();
		private Object _licid;
		private Object _licversion;
		private Object _username;
		private Object _companyid;
		private Object _companyname;
		private Object _validbegin;
		private Object _validend;
		private Object _zkchartsversion;
		private Object _uptime;

		private Init2() {
		}

		private Date getNotBefore(Map map) {
			try {
				return this._dateFormat.parse((String) map.get(Runtime.ISSUE_DATE));
			} catch (ParseException e) {
				return new Date(Long.MAX_VALUE);
			}
		}

		private Date getExpiryDate(Map map) {
			try {
				return this._dateFormat.parse((String) map.get(Runtime.EXPIRY_DATE));
			} catch (ParseException e) {
				return new Date(Long.MIN_VALUE);
			}
		}

		private long getUpTime(Map<String, Object> map) {
			Long utime = (Long) map.get(Runtime.UP_TIME);
			return utime == null ? 0L : utime;
		}

		private void init2(List<LicenseContent> contents) {
			if (contents == null) {
				this.installEval();
				return;
			}
			Date today = Dates.today();
			boolean valid = false;
			Date mindate = new Date(Long.MAX_VALUE);
			Date maxdate = new Date(Long.MIN_VALUE);
			Date maxIssueDate = new Date(Long.MIN_VALUE);
			long uptime = 0L;
			String machine = (String) _wapp.getAttribute(Runtime.ACTIVE_CODE);
			for (LicenseContent lc : contents) {
				Object mapobj = lc.getExtra();
				if (!(mapobj instanceof Map))
					continue;
				Map map = (Map) mapobj;
				Date bdate = this.getNotBefore(map);
				Date edate = this.getExpiryDate(map);
				if (today.before(bdate) || today.after(edate))
					continue;
				if (bdate.before(mindate)) {
					mindate = bdate;
				}
				if (edate.after(maxdate)) {
					maxdate = edate;
				}
				String activecode = (String) map.get(Runtime.ACTIVE_CODE);
				Date issueDate = lc.getIssued();
				if (!issueDate.after(maxIssueDate) || !Runtime.UNIVERSAL_ACTIVE_CODE.equals(activecode)
						&& (machine == null || !machine.equals(activecode)))
					continue;
				valid = true;
				maxIssueDate = issueDate;
				this._zkchartsversion = map.get(Runtime.VERSION);
				this._username = map.get(Runtime.USER_NAME);
				this._companyid = map.get(Runtime.COMPANY_ID);
				this._companyname = map.get(Runtime.COMPANY_NAME);
				this._keys = map;
				this._licid = map.get(Runtime.VERIFICATION_NUMBER);
				this._licversion = map.get(Runtime.LICENSE_VERSION);
				long utime = this.getUpTime(map);
				if (uptime >= utime)
					continue;
				uptime = utime;
			}
			if (valid) {
				this._validbegin = mindate;
				this._validend = maxdate;
				this._uptime = uptime <= 0L
						? Long.valueOf(maxdate.getTime() - mindate.getTime()
								+ (long) new Random(new Date().getTime()).nextInt(1800000))
						: uptime;
			}
			if (!valid || !this.validateLicenseFile()) {
				this.installEval();
			}
		}

		private final void installEval() {
			this._username = EVAL_USER_NAME;
			this._companyid = EVAL_COMPANY_ID;
			this._companyname = EVAL_COMPANY_NAME;
			this._validbegin = new Date();
			this._uptime = Long.MAX_VALUE;
			this._validend = new Date(Long.MAX_VALUE);
			this._keys = new HashMap<String, Object>();
			this._keys.put(Runtime.COMPANY_ID, this._companyid);
		}

		private final boolean validateLicenseFile() {
			if (this._licid == null || this._licversion == null || this._username == null || this._companyid == null
					|| this._companyname == null || this._validbegin == null || this._validend == null
					|| this._zkchartsversion == null || this._uptime == null) {
				return false;
			}
			return true;
		}

		private final String encode(byte[] lic) {
			try {
				return new String(lic, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				return "";
			}
		}

		private final byte[] decryption(byte[] lic) {
			return lic;
		}
	}

	private static final class Init1 {
		private static final int FREQ = 3;
		private static final String WIN = new ObfuscatedString(
				new long[]{5321153595986820916L, 6399668227042605266L, -5602491487233658872L, 2066377234886365691L,
						203726272017906087L, -3409963818121804467L, -290525610284193589L, 5360349214446654186L,
						7126235572874868104L, 6443646263932522040L, -2888496393462639896L}).toString()
				+ new ObfuscatedString(new long[]{8156116825536686571L, -7933011286180755110L, 8429279178335537173L,
						5140564363110783572L, -4587992236270359981L, -1850871503295801036L, -7368992273418428298L,
						-9139769631895394533L, 901440478462063480L, 1371905052599390164L, -5107498036593385759L,
						-5563648405980080527L, 2015340681356193425L, 916270472538173639L, 7391668993988648096L,
						5028903049363742715L, -2699427368479864385L, -2350362590362555178L}).toString();
		private static final Init1 _init1 = new Init1();
		private boolean _stoplicense;
		private boolean _stopuptime;
		private int _inModal;
		private final long _uptime = 0;
		private final Init2 _init2 = new Init2();
		private static final String MD5STR = new ObfuscatedString(
				new long[]{-5121064899839768052L, 3334273322282769989L}).toString();
		private static final String MD = new ObfuscatedString(new long[]{2345590606122015185L, -5069608179148319545L})
				.toString();
		private static final String TITLE = new ObfuscatedString(new long[]{6290325967589686282L, 5925669707962544096L})
				.toString();
		private static final String BORDER = new ObfuscatedString(
				new long[]{1517003640222100403L, -5881717338632855477L}).toString();
		private static final String MODE = new ObfuscatedString(
				new long[]{2063230648775789892L, -7765346573662599107L, -3183841938540764871L}).toString();
		private static final String WIDTH = new ObfuscatedString(
				new long[]{-2028303217529921622L, 6478417485293582031L}).toString();
		private static final String HEIGHT = new ObfuscatedString(
				new long[]{2974696421778732584L, -3701582788618461277L}).toString();
		private static final String ARG = new ObfuscatedString(new long[]{3689490926743112102L, -8583085698292004623L})
				.toString();
		private static final String ZKCHARTS_LOCK = "6";
		private static final String UPTIME_EXP = new ObfuscatedString(
				new long[]{2943335330455776128L, -4848668867510060470L, 6602582347860445238L, -7236343006869602260L,
						7976610064108166951L, -6993221016480970242L, 6472492144499702799L}).toString();
		private static final String TRIAL_EXP = new ObfuscatedString(
				new long[]{1520189818443795303L, 5930700064994299126L, 1008791950702918034L, -3953667963320530015L,
						3514689977611942643L, 7420440463667609306L}).toString();
		private static final String UPTIME_INFO = new ObfuscatedString(
				new long[]{-140629977948033438L, 8411039821423448906L, -3474650949739896324L, 2772224587032503194L})
						.toString();
		private static final String LIC_INFO = new ObfuscatedString(
				new long[]{-1327671782532263310L, 840490956016971870L, 3061901067660749900L}).toString();
		private static final String MASK_HEAD = new ObfuscatedString(
				new long[]{2610432342261686015L, 2813741495629532862L, -1181939021078443460L, -7375419926652952589L})
						.toString();
		private static final String MASK_BODY = new ObfuscatedString(new long[]{-7924049674768767620L,
				-5640807885409979664L, 7029203202109863500L, 8437967762489256364L, -309579538571070286L}).toString();
		private static final String MASK_FOOT = new ObfuscatedString(
				new long[]{812264288056794049L, -2332289316366425421L}).toString();

		private Init1() {
		}

		private void init2(List<LicenseContent> contents) {
			this._init2.init2(contents);
			_pass.set(!this._init2._keys.isEmpty());
		}

		private final void afterProcessEvent0(Event event) {
			if (!this.validate(event)) {
				++this._inModal;
			}
		}

		private final Event beforePostEvent0(Event event) {
			return event;
		}

		private final Event beforeProcessEvent0(Event event) {
			Component comp = event.getTarget();
			if (comp instanceof Charts) {
				if (this._stopuptime) {
					this.maskPvtUptime(comp.getUuid());
					return null;
				}
				if (this._stoplicense) {
					this.maskPvtLicense(comp.getUuid());
					return null;
				}
			}
			return event;
		}

		private final Event beforeSendEvent0(Event event) {
			return event;
		}

		private final void init0(Desktop desktop, Object request) {
			try {
				MessageDigest digest = MessageDigest.getInstance(MD5STR);
				desktop.setAttribute(MD + desktop.getId(), (Object) digest);
			} catch (NoSuchAlgorithmException digest) {
				// empty catch block
			}
			Init init = new Init();
			desktop.setAttribute(Runtime.token1((Object) desktop), (Object) init);
			desktop.addListener((Object) init);
		}

		private final void cleanup0(Desktop desktop) {
			Object init = desktop.getAttribute(Runtime.token1((Object) desktop));
			desktop.removeListener(init);
		}

		private final void init2(Execution exec, Execution parent) {
			Runtime.token0((Object) exec, (Object) exec);
		}

		private final boolean validate(Event event) {
			return !this.validUptime(event) || !this.validLicense(event);
		}

		private final boolean validLicense(Event event) {
			if (this._init2._keys.isEmpty()) {
				this.complainLicense(event);
				return false;
			}
			long time = new Date().getTime();
			if (((Date) this._init2._validbegin).getTime() > time || time > ((Date) this._init2._validend).getTime()) {
				this.complainLicense(event);
				return false;
			}
			return true;
		}

		private final boolean validUptime(Event event) {
			if (this._init2._keys.isEmpty() || this._init2._uptime == null) {
				this.complainLicense(event);
				return false;
			}
			long time = new Date().getTime();
			if (this._uptime > time || time > this._uptime + (Long) this._init2._uptime) {
				this.complainUptime(event);
				return false;
			}
			return true;
		}

		private final void showWin(Page pg, String message) {
			Window win = new Window(TITLE, BORDER, true);
			Library.setProperty((String) ZKCHARTS_LOCK, null);
			win.setMode(MODE);
			win.setWidth(WIDTH);
			win.setHeight(HEIGHT);
			win.setPage(pg);
			HashMap<String, String> arg = new HashMap<String, String>();
			arg.put(ARG, message);
			Component[] comps = Executions.getCurrent().createComponentsDirectly(WIN, "zul", arg);
			for (int j = 0; j < comps.length; ++j) {
				comps[j].setParent((Component) win);
			}
		}

		private final void complainLicense(Event event) {
			Component comp;
			Page pg;
			++this._inModal;
			if (!this._stoplicense && (this._inModal & 3) == 0 && (comp = event.getTarget()) instanceof Charts
					&& (pg = comp.getPage()) != null) {
				this.showWin(pg, TRIAL_EXP);
				this._stoplicense = true;
				this.maskPvtLicense(comp.getUuid());
			}
		}

		private final void complainUptime(Event event) {
			Component comp;
			Page pg;
			++this._inModal;
			if (!this._stopuptime && (this._inModal & 3) == 0 && (comp = event.getTarget()) instanceof Charts
					&& (pg = comp.getPage()) != null) {
				this.showWin(pg, UPTIME_EXP);
				this._stopuptime = true;
				this.maskPvtUptime(comp.getUuid());
			}
		}

		private final void maskPvtUptime(String uuid) {
			this.maskPvt(uuid, UPTIME_INFO);
		}

		private final void maskPvtLicense(String uuid) {
			this.maskPvt(uuid, LIC_INFO);
		}

		private final void maskPvt(String uuid, String txt) {
			Clients.evalJavaScript((String) (MASK_HEAD + uuid + MASK_BODY + txt + MASK_FOOT));
		}
	}

}
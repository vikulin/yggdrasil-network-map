package org.zkoss.zkex.rt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.io.Files;
import org.zkoss.json.JSONObject;
import org.zkoss.json.JSONValue;
import org.zkoss.json.JSONs;
import org.zkoss.lang.Classes;
import org.zkoss.lang.Library;
import org.zkoss.util.resource.Locator;
import org.zkoss.util.resource.Locators;
import org.zkoss.zk.au.AuResponse;
import org.zkoss.zk.au.AuService;
import org.zkoss.zk.au.out.AuAlert;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zk.ui.impl.AbstractWebApp;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zkex.license.CipherParam;
import org.zkoss.zkex.license.KeyStoreParam;
import org.zkoss.zkex.license.LicenseParam;
import org.zkoss.zkex.util.Base64Coder;
import org.zkoss.zkex.util.ObfuscatedString;

public final class Runtime {
	public static final String COMPANY_NAME;
	public static final String COMPANY_ADDRESS;
	public static final String COMPANY_ZIPCODE;
	public static final String COUNTRY;
	public static final String PROJECT_NAME;
	public static final String PRODUCT_NAME;
	public static final String PACKAGE;
	public static final String VERSION;
	public static final String ISSUE_DATE;
	public static final String EXPIRY_DATE;
	public static final String TERM;
	public static final String VERIFICATION_NUMBER;
	public static final String INFORMATION;
	public static final String KEY_SIGNATURE;
	public static final String CHECK_PERIOD;
	public static final String LICENSE_DIRECTORY_PROPERTY;
	public static final String LICENSE_VERSION;
	public static final String WARNING_EXPIRY;
	public static final String WARNING_PACKAGE;
	public static final String WARNING_VERSION;
	public static final String WARNING_COUNT;
	public static final String WARNING_NUMBER;
	public static final String EVALUATION_VERSION;
	public static final String EVALUATION_LICENSE_DIRECTORY_PROPERTY;
	@Deprecated
	public static final String ENABLE_QUOTA_SERVER;
	public static final String WARNING_EVALUATION;
	public static final String GENERAL_WARNING_EVALUATION;
	private static boolean _ck;
	private static final String PUB_STORE;
	private static final String SUBJECT;
	private static final String KEY_NODE;
	private static final String ALIAS;
	private static final String STORE_PASS;
	private static final String EVAL_PUB_STORE;
	private static volatile long _uptime;
	private static final String SCHEDULE_DISABLED;
	private static final String UPTIME_INFO;
	private static final String UPTIME_EXP;
	private static final String ZK_BINARY_WARNING;
	private static final String ZK_ERROR_REPROT_URL;
	private static final String UTIL_IMPL;
	private static KeyStoreParam _keystoreParam;
	private static CipherParam _cipherParam;
	private static LicenseParam _licenseParam;
	private static RuntimeLicenseManager _licManager;
	private static KeyStoreParam _evalKeystoreParam;
	private static LicenseParam _evalLicenseParam;
	private static RuntimeLicenseManager _evalLicManager;
	private static final String RT_CL;
	static final String RT_PREFS;
	private static final String RT_TIMESTAMP;
	private static final Locator LOCATOR;
	private static final String _version;
	private static final byte NORMAL_MODE = 0;
	@Deprecated
	private static final byte SPECIAL_MODE = 1;
	private static final byte EVAL_MODE = 2;
	private static byte MODE;

	public static final Object init(Object object) {
		return Runtime.init(object, null);
	}

	public static final Object init(Object object, Object object2) {
		Object execution = null;
		Object object3;
		if (!_ck && WebApps.getCurrent() != null) {
			Runtime.error(ZK_BINARY_WARNING);
			if (_licManager != null) {
				_licManager.stopScheduler();
			}
			System.exit(-1);
		}
		if (_uptime > 0L && _uptime < System.currentTimeMillis()) {
			object3 = WebApps.getFeature((String) "ee") ? "EE" : "PE";
			object3 = UPTIME_EXP.replaceAll("\\{0\\}", (String) object3);
			Runtime.error((String) object3);
			execution = Executions.getCurrent();
			if (execution != null) {
				((Execution)execution).addAuResponse(UPTIME_EXP,
						(AuResponse) new AuAlert((String) object3, "ZK Eval Notice", "z-msgbox z-uptime"));
			}
		}
		if (!"error".equals(object2) && MODE != 0 && (object3 = Executions.getCurrent()) != null) {
			try {
				if (!(MODE != 2 || !(object instanceof AuService)
						|| object2 != null && ("onTimer".equals(object2) || "onRender".equals(object2)
								|| "onDataLoading".equals(object2) || "onScrollPos".equals(object2)
								|| "onTopPad".equals(object2) || "onAnchorPos".equals(object2)))) {
					RuntimeUtil.getInstance().updatePrefs();
				}
				if (((Execution) object3).getAttribute(RT_CL) == null) {
					 ((Execution) object3).setAttribute(RT_CL, Boolean.TRUE);
					execution = WebApps.getCurrent();
					if (((WebApp)execution).hasAttribute(RT_PREFS)) {
						Object l;
						boolean bl = false;
						if (((WebApp)execution).hasAttribute(RT_TIMESTAMP)) {
							l = (Long) ((WebApp)execution).getAttribute(RT_TIMESTAMP);
							boolean bl2 = l != null ? ((Long)l) > System.currentTimeMillis() : (bl = false);
						}
						if (!bl) {
							l = (JSONObject) JSONValue.parse((String) Base64Coder
									.decodeString((String) ((String) ((WebApp)execution).getAttribute(RT_PREFS))));
							String string = (String)((JSONObject) l).get((Object) "msg");
							if (string != null) {
								Runtime.error(string);
							}
							if (((Execution) object3).isAsyncUpdate(null)) {
								String string2;
								Preferences preferences;
								if (MODE == 2 && ((JSONObject) l).containsKey((Object) "script")
										&& ((string2 = (preferences = Preferences.userNodeForPackage(Runtime.class))
												.get("d.s.g", null)) == null
												|| !Runtime.isInSameDay(JSONs.j2d((String) string2), new Date()))) {
									Clients.evalJavaScript((String) ((String) ((JSONObject) l).get((Object) "script")));
									preferences.put("d.s.g", JSONs.d2j((Date) new Date()));
								}
								((WebApp)execution).setAttribute(RT_TIMESTAMP, (Object) (System.currentTimeMillis() + 600000L));
							}
						}
					}
				}
			} catch (Throwable throwable) {
				Runtime.sendError(throwable);
			}
		}
		return new RtInfo() {

			public void verify(Execution execution) {
			}

			public void verify(Session session) {
			}
		};
	}

	public static boolean isInSameDay(Date date, Date date2) {
		Calendar calendar = Calendar.getInstance();
		Calendar calendar2 = Calendar.getInstance();
		calendar.setTime(date);
		calendar2.setTime(date2);
		return calendar.get(1) == calendar2.get(1) && calendar.get(6) == calendar2.get(6);
	}

	private static final void error(String string) {
		Logger logger = LoggerFactory.getLogger((String) "global");
		if (logger.isErrorEnabled()) {
			logger.error(string);
		} else {
			System.err.println(string);
		}
	}

	private static final String read(String string) {
		try {
			InputStream inputStream = Runtime.class.getResourceAsStream("/metainfo/zk/" + string);
			if (inputStream != null) {
				return new String(Files.readAll((InputStream) inputStream));
			}
		} catch (Throwable throwable) {
			// empty catch block
		}
		return null;
	}

	public static final boolean init(WebApp webApp, boolean bl) {
		boolean bl2 = true;
		if (bl && !_ck) {
			URL uRL;
			_ck = true;
			bl2 = WebApps.getFeature((String) "ee") && "ZK EE".equals(Runtime.read("ee"))
					|| "ZK PE".equals(Runtime.read("pe"));
			String string = Library.getProperty((String) LICENSE_DIRECTORY_PROPERTY);
			boolean bl3 = false;
			URL uRL2 = uRL = string != null
					? Runtime.getURLFromString(string)
					: LOCATOR.getResource("/metainfo/zk/license/");
			if (uRL != null) {
				bl3 = _licManager.install(uRL);
			}
			if (bl2) {
				webApp = null;
			}
			if (bl3) {
				_licManager.setWapp(webApp);
				if (!"true".equals(Library.getProperty((String) SCHEDULE_DISABLED, (String) "false"))) {
					_licManager.startScheduler();
				}
				return true;
			}
			if (webApp != null) {
				MODE = (byte) 2;
				webApp.setAttribute("org.zkoss.zk.ui.notice", (Object) " Evaluation Only");
				Runtime.enableUptimeLimit();
				Runtime.runQuotaServer();
			}
		}
		return bl2;
	}

	static URL getURLFromString(String string) {
		try {
			return new File(string).toURI().toURL();
		} catch (IOException iOException) {
			Runtime.error("getURLFromString: " + iOException.getMessage());
			return null;
		}
	}

	static final void sendError(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		throwable.printStackTrace(printWriter);
		Runtime.sendError(stringWriter.toString());
	}

	static final void sendError(String string) {
		block6 : {
			String string2 = RuntimeUtil.getInstance().getUFlag();
			if (string2 != null) {
				string = System.getProperty("os.name") + " - " + System.getProperty("os.version") + ':' + _version + ":"
						+ string2 + "\n" + string;
				if (string.length() > 600) {
					string = string.substring(0, 600);
				}
				BufferedReader bufferedReader = null;
				try {
					URLConnection uRLConnection = new URL(ZK_ERROR_REPROT_URL + URLEncoder.encode(string, "UTF-8"))
							.openConnection();
					bufferedReader = new BufferedReader(new InputStreamReader(uRLConnection.getInputStream()));
					bufferedReader.close();
				} catch (Throwable throwable) {
					if (bufferedReader == null)
						break block6;
					try {
						bufferedReader.close();
					} catch (IOException iOException) {
						// empty catch block
					}
				}
			}
		}
	}

	public static final void enableUptimeLimit() {
		_uptime = Long.MAX_VALUE;
	}

	public static final boolean isQuotaServerEnabled() {
		return RuntimeUtil.getInstance().isEnabled();
	}

	public static final void runQuotaServer() {
		RuntimeUtil.getInstance().run((RuntimeLicense) _evalLicManager);
	}

	static {
		String string;
		COMPANY_NAME = new ObfuscatedString(
				new long[]{-7340139527016707886L, -5203243759892677212L, 8714822623115369524L}).toString();
		COMPANY_ADDRESS = new ObfuscatedString(
				new long[]{-8691848786421899489L, -4370996558863340632L, -2709933490946981238L}).toString();
		COMPANY_ZIPCODE = new ObfuscatedString(
				new long[]{-4934501068656857753L, -6913828373145765012L, 1063193634233753528L}).toString();
		COUNTRY = new ObfuscatedString(new long[]{-4504969373906269801L, 5248137891553083335L}).toString();
		PROJECT_NAME = new ObfuscatedString(new long[]{830623621279886032L, 459360910074040759L, 4178520520701877821L})
				.toString();
		PRODUCT_NAME = new ObfuscatedString(
				new long[]{5504882338648710617L, -4761731334749763195L, 996375918662338065L}).toString();
		PACKAGE = new ObfuscatedString(new long[]{-8439029564924938530L, -4878278112849633009L}).toString();
		VERSION = new ObfuscatedString(new long[]{-4847689528984584834L, 2493253216426408014L}).toString();
		ISSUE_DATE = new ObfuscatedString(
				new long[]{-4228764154858292882L, -6898004159031332466L, 6328666951570048917L}).toString();
		EXPIRY_DATE = new ObfuscatedString(
				new long[]{-7233890858958970371L, -3423973165832030856L, -5810612950970282077L}).toString();
		TERM = new ObfuscatedString(new long[]{-6725301182108235475L, -8691110408124621856L}).toString();
		VERIFICATION_NUMBER = new ObfuscatedString(
				new long[]{3823288740853721680L, -6436340937747658512L, 891079956768101415L, 751513662528431611L})
						.toString();
		INFORMATION = new ObfuscatedString(new long[]{378870925371295609L, 1418863983102047429L, -5017007170548372422L})
				.toString();
		KEY_SIGNATURE = new ObfuscatedString(
				new long[]{-2573177027008659676L, 5066716785755217927L, 5769746383701090690L}).toString();
		CHECK_PERIOD = new ObfuscatedString(
				new long[]{-2439022525501632135L, 6139476070014855270L, -297657911147084449L}).toString();
		LICENSE_DIRECTORY_PROPERTY = new ObfuscatedString(new long[]{6388899238000244134L, -5180024805664342415L,
				-367736596541156579L, -5744200612985226406L, -3419809629710828214L, -3830138366774401580L}).toString();
		LICENSE_VERSION = new ObfuscatedString(
				new long[]{-7080462743270045357L, 6928867389785115158L, -154565539896996742L}).toString();
		WARNING_EXPIRY = new ObfuscatedString(
				new long[]{-2088056424898980973L, -3616911578495445651L, -8353968737700076168L}).toString();
		WARNING_PACKAGE = new ObfuscatedString(
				new long[]{7436618834759965309L, 8220698497085578148L, -6394078374620879850L}).toString();
		WARNING_VERSION = new ObfuscatedString(
				new long[]{7417971821667979026L, 7464186339852802771L, 7986314911006223431L}).toString();
		WARNING_COUNT = new ObfuscatedString(
				new long[]{-1510608780643214737L, 6313704540210276937L, 4115504365890483558L}).toString();
		WARNING_NUMBER = new ObfuscatedString(
				new long[]{8367990676393660796L, -7163797910637480555L, -8349581027556623805L}).toString();
		EVALUATION_VERSION = new ObfuscatedString(
				new long[]{-8254333082681995594L, 2133816849348739600L, 569888272338784702L, 2363756875261694331L})
						.toString();
		EVALUATION_LICENSE_DIRECTORY_PROPERTY = new ObfuscatedString(
				new long[]{5224700506273109669L, -4821082329883784793L, 4483789110796661303L, -4130582028120586740L,
						6738877940215938826L, 5062964898350986461L}).toString();
		ENABLE_QUOTA_SERVER = new ObfuscatedString(
				new long[]{-8625977702787672529L, 512796179395572189L, -315657241020468189L, -5391257847076299574L})
						.toString();
		WARNING_EVALUATION = "1";
		GENERAL_WARNING_EVALUATION = "2";
		PUB_STORE = new ObfuscatedString(
				new long[]{5347140503694695672L, -2050078472222786559L, -1090939667477166182L, -1614887116429751062L})
						.toString();
		SUBJECT = new ObfuscatedString(new long[]{-1712853941495412010L, 7139314103872750678L, 1708647545312167373L})
				.toString();
		KEY_NODE = new ObfuscatedString(new long[]{-1178633844166344050L, 942064108562773571L, 5467556404179583502L})
				.toString();
		ALIAS = new ObfuscatedString(new long[]{6186746913238977211L, 1118071448919910242L, 3268266759092267502L,
				8415914926935764836L, 8050900615525030165L}).toString();
		STORE_PASS = new ObfuscatedString(new long[]{8215178821005386738L, 3606545547696508380L, 8105569695056259631L,
				8978067179702718063L, 8980164237678180832L}).toString();
		EVAL_PUB_STORE = new ObfuscatedString(
				new long[]{4483574995589002284L, -1877769693911683148L, 6537753203970748177L, 7870286962621092528L})
						.toString();
		_uptime = -1L;
		SCHEDULE_DISABLED = new ObfuscatedString(new long[]{5671306788677542365L, -5885525372458450988L,
				-4407396626133081282L, -1537455835363693899L, -6031102665156057524L, -1478170211770868218L}).toString();
		UPTIME_INFO = new ObfuscatedString(
				new long[]{-140629977948033438L, 8411039821423448906L, -3474650949739896324L, 2772224587032503194L})
						.toString();
		UPTIME_EXP = "3";
		ZK_BINARY_WARNING = "4";
		ZK_ERROR_REPROT_URL = "5";
		UTIL_IMPL = new ObfuscatedString(new long[]{-9119357793812075433L, -281094700249698240L, 5406917946962033774L,
				-7188106790788417186L, -7484080138269982934L, -5119145590716415229L}).toString();
		_keystoreParam = new KeyStoreParam() {

			public InputStream getStream() throws IOException {
				InputStream inputStream = Classes.getContextClassLoader(Runtime.class).getResourceAsStream(PUB_STORE);
				if (inputStream == null) {
					throw new FileNotFoundException(PUB_STORE);
				}
				return inputStream;
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
		_licenseParam = new LicenseParam() {

			public String getSubject() {
				return SUBJECT;
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
		_licManager = RuntimeLicenseManager.getInstance((LicenseParam) _licenseParam);
		_evalKeystoreParam = new KeyStoreParam() {

			public InputStream getStream() throws IOException {
				InputStream inputStream = Classes.getContextClassLoader(Runtime.class)
						.getResourceAsStream(EVAL_PUB_STORE);
				if (inputStream == null) {
					throw new FileNotFoundException(EVAL_PUB_STORE);
				}
				return inputStream;
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
		_evalLicenseParam = new EvalLicenseParam() {

			public String getSubject() {
				return SUBJECT;
			}

			public Preferences getPreferences() {
				return null;
			}

			public KeyStoreParam getKeyStoreParam() {
				return _evalKeystoreParam;
			}

			public CipherParam getCipherParam() {
				return _cipherParam;
			}
		};
		_evalLicManager = RuntimeLicenseManager.getInstance((LicenseParam) _evalLicenseParam);
		RT_CL = new ObfuscatedString(
				new long[]{5369801822528592945L, 4131644222152454727L, -8568743324321778912L, 2410573768741921741L})
						.toString();
		RT_PREFS = new ObfuscatedString(
				new long[]{-565558667013027914L, 2706949423404623976L, -1185472294175643547L, 2700952870953613515L})
						.toString();
		RT_TIMESTAMP = new ObfuscatedString(new long[]{-1400397395496866872L, -6076048055011238918L,
				2610382205060226760L, 1861152423740986487L, 3180211639163912568L}).toString();
		LOCATOR = Locators.getDefault();
		try {
			string = Integer.toString(Integer.parseInt("9.1.0".replace(".", "")), 36) + ":"
					+ Integer.toString(Integer.parseInt(AbstractWebApp.getBuildStamp().replace("\\.", "")), 36);
		} catch (Exception exception) {
			string = "9.1.0:" + AbstractWebApp.getBuildStamp();
		}
		_version = string;
		try {
			Classes.forNameByThread((String) UTIL_IMPL);
		} catch (Throwable throwable) {
			// empty catch block
		}
		MODE = 0;
	}

	static interface EvalLicenseParam extends LicenseParam {
	}

}
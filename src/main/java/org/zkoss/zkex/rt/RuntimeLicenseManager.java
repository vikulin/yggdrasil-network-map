package org.zkoss.zkex.rt;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.Cleanups;
import org.zkoss.util.Cleanups.Cleanup;
import org.zkoss.util.Dates;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.WebApps;
import org.zkoss.zkex.license.LicenseContent;
import org.zkoss.zkex.license.LicenseManager;
import org.zkoss.zkex.license.LicenseParam;
import org.zkoss.zkex.util.ObfuscatedString;

final class RuntimeLicenseManager extends LicenseManager implements RuntimeLicense {
	private final Object _lock = new Object();
	private volatile Timer _timer;
	private volatile WebApp _wapp;
	private List<LicenseContent> _contents;
	private volatile URL _dirUrl;
	private volatile long _latest;
	private static SimpleDateFormat _dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
	private static int HOUR = 3600000;

	private RuntimeLicenseManager(LicenseParam paramLicenseParam) {
		super(paramLicenseParam);
	}

	public boolean install(URL paramURL) {
		URLConnection localURLConnection = null;
		try {
			localURLConnection = createConnection(paramURL);
			Key[] arrayOfKey = getKeys(localURLConnection, paramURL);
			boolean bool;
			if (arrayOfKey == null) {
				bool = false;
				return bool;
			}
			_dirUrl = paramURL;
			install0(arrayOfKey);
			bool = true;
			return bool;
		} catch (IOException localIOException) {
			log("Create connection failed: " + localIOException.getMessage());
			boolean bool = false;
			return bool;
		} finally {
			closeConnection(localURLConnection);
		}
	}

	private URLConnection createConnection(URL paramURL) throws IOException {
		if ("jar".equals(paramURL.getProtocol())) {
			return paramURL.openConnection();
		}
		return null;
	}

	private void closeConnection(URLConnection paramURLConnection) {
		try {
			if ((paramURLConnection instanceof JarURLConnection)) {
				JarURLConnection localJarURLConnection = (JarURLConnection) paramURLConnection;
				if (!localJarURLConnection.getUseCaches()) {
					localJarURLConnection.getJarFile().close();
				}
			}
		} catch (IOException localIOException) {
		}
	}

	private Key[] getKeys(URLConnection paramURLConnection, URL paramURL) {
		if ((paramURLConnection instanceof JarURLConnection)) {
			try {
				return getKeysFromJar((JarURLConnection) paramURLConnection);
			} catch (IOException localIOException) {
				log("Get keys from jar error: " + localIOException.getMessage());
				return null;
			}
		}
		return getKeysFromFileSystem(paramURL.getPath());
	}

	private Key[] getKeysFromJar(JarURLConnection paramJarURLConnection) throws IOException {
		JarFile localJarFile = paramJarURLConnection.getJarFile();
		JarEntry localJarEntry1 = paramJarURLConnection.getJarEntry();
		String str = localJarEntry1.getName();
		ArrayList localArrayList = new ArrayList(2);
		Enumeration localEnumeration = localJarFile.entries();
		while (localEnumeration.hasMoreElements()) {
			JarEntry localJarEntry2 = (JarEntry) localEnumeration.nextElement();
			if (!localJarEntry2.isDirectory()) {
				if (localJarEntry2.getName().startsWith(str)) {
					localArrayList.add(new JarKey(localJarFile, localJarEntry2));
				}
			}
		}
		return (Key[]) localArrayList.toArray(new Key[localArrayList.size()]);
	}

	private Key[] getKeysFromFileSystem(String paramString) {
		File localFile = new File(paramString);
		File[] arrayOfFile = localFile.listFiles();
		if (arrayOfFile == null) {
			return null;
		}
		Key[] arrayOfKey = new Key[arrayOfFile.length];
		for (int i = 0; i < arrayOfFile.length; i++) {
			arrayOfKey[i] = new FileKey(arrayOfFile[i]);
		}
		return arrayOfKey;
	}

	private void info(String paramString) {
		paramString = "\n" + paramString + "\n";
		Logger localLogger = LoggerFactory.getLogger("global");
		if (localLogger.isInfoEnabled()) {
			localLogger.info(paramString);
		} else {
			System.out.println(paramString);
		}
	}

	private void log(String paramString) {
		
	}

	private void install0(Key[] paramArrayOfKey) {
		synchronized (_lock) {
			_contents = new ArrayList(paramArrayOfKey.length);
			String str1 = getPackage();
			HashMap localHashMap = new HashMap();
			_latest = 0L;
			for (Key localKey : paramArrayOfKey) {
				_latest += localKey.lastModified() + localKey.length();
				try {
					LicenseContent localLicenseContent = install(localKey);
					if (localLicenseContent != null) {
						Map localMap = (Map) localLicenseContent.getExtra();
						if (!localMap.containsKey(Runtime.EVALUATION_VERSION)) {
							String str2 = (String) localMap.get(Runtime.VERIFICATION_NUMBER);
							if ((str2 == null) || (localHashMap.containsKey(str2))) {
								log(localMap.get(Runtime.WARNING_NUMBER) + localKey.getPath());
								continue;
							}
							if (!isValidPackage(str1, (String) localMap.get(Runtime.PACKAGE))) {
								log(localMap.get(Runtime.WARNING_PACKAGE) + localKey.getPath());
								continue;
							}
							String str3 = (String) localMap.get(Runtime.VERSION);
							if ((str3 != null) && (!"9.1.0".startsWith(str3))) {
								log(localMap.get(Runtime.WARNING_VERSION) + localKey.getPath());
								continue;
							}
							if (Dates.now().after(getExpiryDate(localLicenseContent))) {
								log(localMap.get(Runtime.WARNING_EXPIRY) + localKey.getPath());
								continue;
							}
							localHashMap.put(str2, Boolean.TRUE);
						} else {
							if (Dates.now().after(getExpiryDate(localLicenseContent))) {
								continue;
							}
						}
						_contents.add(localLicenseContent);
					}
				} catch (Exception localException) {
					log(new ObfuscatedString(new long[]{4307748939090133289L, 3763454470649753422L,
							-6872730590590579063L, -4639536792232415440L, -5784189857893158816L, 7307901420180268778L,
							-627643376646394353L, -7420280774293424959L, 2656306477204072694L, -2499488659900337288L,
							-8059070027533673028L, 7471492367409620715L, 822853263747496262L, -2451111032939294087L,
							5181597043837345221L, -5675751368559367806L, 5049843703446154799L, 9141315134085574909L,
							-6165825001738499911L, -8027923838117731411L, -6842157638029734249L}).toString()
							+ localKey.getPath());
				}
			}
			_latest *= paramArrayOfKey.length;
			if (_contents.isEmpty()) {
				if (_latest == 0L) {
					log(new ObfuscatedString(new long[]{-6899918244677316866L, 2946753050424552565L,
							-1166500401249160465L, -6361152391287683152L, -1012658505712928448L, 2102941868302252760L,
							3901913952577343105L, 1861727708660677834L, -3196033244540681827L, -1179475304557705732L,
							-8292713655681148352L, 1846152504906808191L, -5915587415450046371L, 7423933818392029259L,
							-8624164244204572166L, -1535967842794712539L, 8072868225759836711L, -3862751912709119543L})
									.toString());
				}
			} else {
				printInfo();
			}
		}
	}

	private synchronized LicenseContent install(Key paramKey) throws Exception {
		return install(loadLicenseKey(paramKey), getLicenseNotary());
	}

	private static byte[] loadLicenseKey(Key paramKey) throws IOException {
		int i = Math.min((int) paramKey.length(), 1048576);
		byte[] arrayOfByte = new byte[i];
		InputStream localInputStream = paramKey.inputStream();
		try {
			int j = 0;
			int k = arrayOfByte.length;
			do {
				int m = localInputStream.read(arrayOfByte, j, k - j);
				if (m < 0) {
					throw new EOFException();
				}
				j += m;
			} while (j < k);
		} finally {
			localInputStream.close();
		}
		return arrayOfByte;
	}

	public long getDelay() {
		return Long.MAX_VALUE;
	}

	public void startScheduler() {
		if (_timer != null) {
			return;
		}
		synchronized (_lock) {
			if (_timer == null) {
				_timer = new Timer();
				Cleanups.add(new Cleanups.Cleanup() {
					public void cleanup() {
						stopScheduler();
					}
				});
			}
		}
		check();
	}

	public boolean isScheduled() {
		return _timer != null;
	}

	public void stopScheduler() {
		synchronized (_lock) {
			if (_timer != null) {
				_timer.cancel();
				_timer = null;
			}
		}
	}

	LicenseContent getMaximum() {
		Object localObject1 = null;
		synchronized (_lock) {
			for (int i = 0; i < _contents.size(); i++) {
				LicenseContent localLicenseContent = (LicenseContent) _contents.get(i);
				if ((localObject1 == null)
						|| (getExpiryDate((LicenseContent) localObject1).before(getExpiryDate(localLicenseContent)))) {
					localObject1 = localLicenseContent;
				}
			}
		}
		return (LicenseContent) localObject1;
	}

	public Map<String, LicenseContent> getAllMaximums() {
		synchronized (_lock) {
			if (_contents.isEmpty()) {
				return Collections.EMPTY_MAP;
			}
			HashMap localHashMap = new HashMap(5);
			for (int i = 0; i < _contents.size(); i++) {
				LicenseContent localLicenseContent1 = (LicenseContent) _contents.get(i);
				Map localMap = (Map) localLicenseContent1.getExtra();
				if (localMap != null) {
					String str = (String) localMap.get(Runtime.PRODUCT_NAME);
					LicenseContent localLicenseContent2 = (LicenseContent) localHashMap.get(str);
					if ((localLicenseContent2 == null)
							|| (getExpiryDate(localLicenseContent2).before(getExpiryDate(localLicenseContent1)))) {
						localHashMap.put(str, localLicenseContent1);
					}
				}
			}
			return localHashMap;
		}
	}

	private Date getNotBefore(LicenseContent paramLicenseContent) {
		try {
			return _dateFormat.parse((String) ((Map) paramLicenseContent.getExtra()).get(Runtime.ISSUE_DATE));
		} catch (ParseException localParseException) {
		}
		return null;
	}

	private Date getExpiryDate(LicenseContent paramLicenseContent) {
		return new Date(Long.MAX_VALUE);
	}

	private LicenseContent checkLatest()
  {
    URLConnection localURLConnection = null;
    try
    {
      localURLConnection = createConnection(_dirUrl);
      Key[] arrayOfKey = getKeys(localURLConnection, _dirUrl);
      if (arrayOfKey == null)
      {
        LicenseContent localLicenseContent1 = null;
        return localLicenseContent1;
      }
      long l = 0L;
      for (Object localObject3 : arrayOfKey) {
        l += ((Key)localObject3).lastModified() + ((Key)localObject3).length();
      }
      l *= arrayOfKey.length;
      LicenseContent tmp = getMaximum();
      if ((_latest != l) || ((tmp != null) && (Dates.now().after(getExpiryDate((LicenseContent)tmp)))))
      {
        install0(arrayOfKey);
        LicenseContent localObject2 = getMaximum();
        return (LicenseContent)localObject2;
      }
      Object localObject2 = tmp;
      return (LicenseContent)localObject2;
    }
    catch (IOException localIOException)
    {
      log("Create connection failed: " + localIOException.getMessage());
      LicenseContent localLicenseContent2 = null;
      return localLicenseContent2;
    }
    finally
    {
      closeConnection(localURLConnection);
    }
  }

	private void printInfo() {
		synchronized (_lock) {
			for (int i = 0; i < _contents.size(); i++) {
				LicenseContent localLicenseContent = (LicenseContent) _contents.get(i);
				info((String) ((Map) localLicenseContent.getExtra()).get(Runtime.INFORMATION));
			}
		}
	}

	private boolean isValidPackage(String paramString1, String paramString2) {
		if (paramString2 == null) {
			return false;
		}
		return (paramString1.equals(paramString2)) || (paramString2.equals("EP"))
				|| ((paramString2.equals("PP")) && (!paramString1.equals("EP")));
	}

	private String getPackage() {
		String str = getPackageVersion();
		return str.substring(0, str.length() - 1) + "P";
	}

	private String getPackageVersion() {
		return WebApps.getEdition();
	}

	private void check() {
		checkLatest();
	}

	public static RuntimeLicenseManager getInstance(LicenseParam paramLicenseParam) {
		return new RuntimeLicenseManager(paramLicenseParam);
	}

	public void setWapp(WebApp paramWebApp) {
		_wapp = paramWebApp;
	}

	private static class JarKey implements RuntimeLicenseManager.Key {
		private final JarFile jar;
		private final JarEntry entry;

		public JarKey(JarFile paramJarFile, JarEntry paramJarEntry) {
			jar = paramJarFile;
			entry = paramJarEntry;
		}

		public InputStream inputStream() throws IOException {
			return jar.getInputStream(entry);
		}

		public long lastModified() {
			return entry.getTime();
		}

		public long length() {
			return entry.getSize();
		}

		public String getPath() {
			return jar.getName() + "/" + entry.getName();
		}
	}

	private static class FileKey implements RuntimeLicenseManager.Key {
		private final File file;

		public FileKey(File paramFile) {
			file = paramFile;
		}

		public InputStream inputStream() throws IOException {
			return new FileInputStream(file);
		}

		public long lastModified() {
			return file.lastModified();
		}

		public long length() {
			return file.length();
		}

		public String getPath() {
			return file.getPath();
		}
	}

	private static abstract interface Key {
		public abstract InputStream inputStream() throws IOException;

		public abstract long lastModified();

		public abstract long length();

		public abstract String getPath();
	}
}
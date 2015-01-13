package util.https;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class HttpHelper {

	private static final String CHARSET_UTF8 = HTTP.UTF_8;
	private static HttpClient customerHttpClient;

	private HttpHelper() {

	}

	public static String GetResponse(final Context context, final String url,
			final NameValuePair... nameValuePairs) {
		String strResult;

		strResult = "";

		FutureTask<String> task = new FutureTask<String>(
				new Callable<String>() {
					@Override
					public String call() throws Exception {
						List<NameValuePair> params = new ArrayList<NameValuePair>();
						if (nameValuePairs != null) {
							for (int i = 0; i < nameValuePairs.length; i++) {
								params.add(nameValuePairs[i]);

								System.out.println(nameValuePairs[i]);
							}
						}

						UrlEncodedFormEntity urlEncoded = new UrlEncodedFormEntity(
								params, CHARSET_UTF8);

						System.out.println(url);

						HttpPost httpPost = new HttpPost(url);
						httpPost.setEntity(urlEncoded);

						if (isNetWorkAvailable(context) == true) {
							HttpClient client = getHttpClient(context);// >>>
							HttpResponse response = client.execute(httpPost);// >>>
							int res = response.getStatusLine().getStatusCode();
							if (res != HttpStatus.SC_OK) {
								Log.i("fanjishuo____GetResponse", "res"+res);
								throw new RuntimeException("����ʧ��");
							}
							HttpEntity resEntity = response.getEntity();

							return (resEntity == null) ? null : EntityUtils
									.toString(resEntity, CHARSET_UTF8);
						} else {
							return "false";
						}
					}
				});
		new Thread(task).start();
		try {
			strResult = task.get();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strResult;
	}

	private static HttpClient getHttpClient(Context context) {
		if (null == customerHttpClient) {
			HttpParams params = new BasicHttpParams();
			// ����һЩ��������
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, CHARSET_UTF8);
			HttpProtocolParams.setUseExpectContinue(params, true);
			HttpProtocolParams
					.setUserAgent(
							params,
							"Mozilla/5.0(Linux;U;Android 2.2.1;en-us;Nexus One Build.FRG83) "
									+ "AppleWebKit/553.1(KHTML,like Gecko) Version/4.0 Mobile Safari/533.1");
			// ��ʱ����
			/* �����ӳ���ȡ���ӵĳ�ʱʱ�� */
			ConnManagerParams.setTimeout(params, 1000);
			/* ���ӳ�ʱ */
			int ConnectionTimeOut = 3000;

			HttpConnectionParams
					.setConnectionTimeout(params, ConnectionTimeOut);
			/* ����ʱ */
			HttpConnectionParams.setSoTimeout(params, 4000);
			// �������ǵ�HttpClient֧��HTTP��HTTPS����ģʽ
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			schReg.register(new Scheme("https", SSLSocketFactory
					.getSocketFactory(), 443));

			// ʹ���̰߳�ȫ�����ӹ���������HttpClient
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
					params, schReg);
			customerHttpClient = new DefaultHttpClient(conMgr, params);
		}
		return customerHttpClient;
	}

	public static boolean isNetWorkAvailable(Context context) {
		// ConnectivityManager:��Ҫ����������������صĲ���
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);// ��ȡϵͳ�����ӷ���
		// ��ȡ��������״̬��NetWorkInfo����
		NetworkInfo info = cm.getActiveNetworkInfo();
		boolean status = info != null && info.isConnected();
		return status;
	}
}
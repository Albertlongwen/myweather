package util;//从服务器端获取全国所有的省市县的数据

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.util.Log;

public class HttpUtil {
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener)
	{
		new Thread(new Runnable(){

			@Override
			public void run()
			{
				// TODO Auto-generated method stub
				HttpURLConnection connection=null;
				try
				{
					URL url=new URL(address);//把地址字符串转换为网址
					connection=(HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					InputStream in=connection.getInputStream();
					BufferedReader reader=new BufferedReader(new InputStreamReader(in));
					StringBuilder response=new StringBuilder();
					String line;
					while((line=reader.readLine())!=null)
					{
						response.append(line);
					}
					if(listener!=null)
					{
						//回调onFinish()方法
						listener.onFinish(response.toString());
						//Log.i("guiugtufguhiuygfut", "httputil");
					}
				}
				catch(Exception e)
				{
					if(listener!=null)
					{
						//回调onError()方法
						listener.onError(e);
					}
				}
				finally
				{
					if(connection!=null)
					{
						connection.disconnect();
					}
				}
			}
			
		}).start();
	}
}

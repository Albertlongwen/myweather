package activity;

import java.util.ArrayList;
import java.util.List;

import com.example.myweather.R;//正确地导入本地R包的方式

//import android.R;//开始导错包，所以会出现无法调用R包的变量的情况
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import model.City;
import model.County;
import model.MyWeatherDB;
import model.Province;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;

public class ChooseAreaActivity extends Activity {
	
	public static final String tag = "ChooseAreaActivity";
	
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private MyWeatherDB myWeatherDB;
	private List<String> dataList=new ArrayList<String>();
	
	//省列表
	private List<Province> provinceList;
	
	private List<City> cityList;
	
	private List<County> countyList;
	
	//选中的省份
	private Province selectedProvince;
	private City selectedCity;
	//当前选中的级别
	private int currentLevel;
	
	private boolean isFromWeatherActivity;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		isFromWeatherActivity=getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		
		if(prefs.getBoolean("city_selected", false)&&!isFromWeatherActivity)
		{
			Intent intent=new Intent(this,WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView =(ListView)findViewById(R.id.list_view);
		
		titleText=(TextView) findViewById(R.id.title_text);
		adapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		myWeatherDB=MyWeatherDB.getInstance(this);
		listView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
				// TODO Auto-generated method stub
				if(currentLevel==LEVEL_PROVINCE)
				{
					selectedProvince=provinceList.get(index);
					queryCities();
				}
				else if(currentLevel==LEVEL_CITY)
				{
					selectedCity=cityList.get(index);
					queryCounties();
				}else if(currentLevel==LEVEL_COUNTY)
				{
					String countyCode=countyList.get(index).getCountyCode();
					Intent intent=new Intent(ChooseAreaActivity.this,WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
				}
			}		
		});
		queryProvinces();//加载省级数据
	}
	
	//查询全国所有的省，优先从数据库中查找，如果没有再从服务器上查询
	private void queryProvinces()
	{
		provinceList=myWeatherDB.loadProvinces();
		if(provinceList.size()>0)
		{
			dataList.clear();
			for(Province province:provinceList)
			{
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		}
		else
		{
			queryFromServer(null,"Province");
		}
	}
	
	private void queryCities()
	{
		cityList=myWeatherDB.loadCities(selectedProvince.getId());
		if(cityList.size()>0)
		{
			dataList.clear();
			for(City city:cityList)
			{
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel=LEVEL_CITY;
		}
		else
		{
			queryFromServer(selectedProvince.getProvinceCode(),"City");
		}
	}
	
	private void queryCounties()
	{
		countyList=myWeatherDB.loadCounties(selectedCity.getId());
		if(countyList.size()>0)
		{
			dataList.clear();
			for(County county:countyList)
			{
				dataList.add(county.getCountyName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel=LEVEL_COUNTY;
		}else
		{
			queryFromServer(selectedCity.getCityCode(),"County");
		}
	}
	
	//从服务器加载数据
	private void queryFromServer(final String code,final String type)
	{
		//中国天气网新的API接口地址http://m.weather.com.cn/atad/101010100.html 
		
		String address;
		if(!TextUtils.isEmpty(code))
		{
			
			address="http://www.weather.com.cn/data/list3/city"+code+".xml";
		}
		else
		{
			address="http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		//连接服务器获取数据是在新的线程中进行的
		HttpUtil.sendHttpRequest(address, 
				new HttpCallbackListener(){
			@Override
			public void onFinish(String response) 
			{
				// TODO Auto-generated method stub
				boolean result=false;
				if("Province".equals(type))
				{
					//Log.i("guiugtufguhiuygfut", "province");
					result=Utility.handleProvincesResponse(myWeatherDB, response);
				}
				else if("City".equals(type))
				{
					//Log.i("guiugtufguhiuygfut", "city");
					result=Utility.handleCitiesResponse(myWeatherDB, response, selectedProvince.getId());
				}
				else if("County".equals(type))
				{
					//Log.i("guiugtufguhiuygfut", "county");
					result=Utility.handleCountiesResponse(myWeatherDB, response, selectedCity.getId());
				}
				//Log.i("guiugtufguhiuygfut", "complete");
				if(result)
				{
					//通过runOnUIThread（）方法回到UI主线程处理逻辑
					runOnUiThread(new Runnable(){
						public void run()
						{
							closeProgressDialog();
							if("Province".equals(type))
							{
								queryProvinces();
							}
							else if("City".equals(type))
							{
								queryCities();
							}
							else if("County".equals(type))
							{
								queryCounties();
							}
						}
					});
				}
			}

			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				//通过runOnUIThread（）方法回到主线程处理逻辑
				runOnUiThread(new Runnable(){
					public void run()
					{
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}	
		});//new 一个对象的结尾，调用sendHttpRequest结束
	}
	
	//显示进度对话框
	private void showProgressDialog()
	{
		if(progressDialog==null)
		{
			progressDialog=new ProgressDialog(this);
			progressDialog.setMessage("正在加载……");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	//关闭进度对话框
	private void closeProgressDialog()
	{
		if(progressDialog!=null)
		{
			progressDialog.dismiss();
		}
	}
	//捕获Back按键
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(currentLevel==LEVEL_COUNTY)
		{
			queryCities();
		}
		else if(currentLevel==LEVEL_CITY)
		{
			queryProvinces();
		}
		else
		{
			if(isFromWeatherActivity)
			{
				Intent intent=new Intent(this,WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}	
}

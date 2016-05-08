package db;//该包主要保存所有数据库的相关代码

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class myWeatherOpenHelper extends SQLiteOpenHelper {

	//省份的建表语句
	public static final String CREATE_PROVINCE="create table Province(id integer primary key autoincrement,"
												+"province_name text,province_code text)";
	
	//城市(市级)的建表语句
	public static final String CREATE_CITY="create table City(id integer primary key autoincrement,"
			+"city_name text,city_code text,province_id integer)";
	
	//县级城市的建表语句
	public static final String CREATE_COUNTY="create table County(id integer primary key autoincrement,"
			+"county_name text,county_code text,city_id integer)";
	
	public myWeatherOpenHelper(Context context,String name,CursorFactory factory,int version)//必须要有构造函数，不然会报错
	{
		super(context,name,factory,version);
		
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL(CREATE_PROVINCE);//创建省份表
		db.execSQL(CREATE_CITY);
		db.execSQL(CREATE_COUNTY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}

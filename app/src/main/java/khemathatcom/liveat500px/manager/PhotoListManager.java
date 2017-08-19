package khemathatcom.liveat500px.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.gson.Gson;
import com.inthecheesefactory.thecheeselibrary.manager.Contextor;

import java.util.ArrayList;

import khemathatcom.liveat500px.dao.PhotoItemCollectionDao;
import khemathatcom.liveat500px.dao.PhotoItemDao;

/**
 * Created by nuuneoi on 11/16/2014.
 */
public class PhotoListManager {

    private Context mContext;
    private PhotoItemCollectionDao dao;

    public PhotoListManager() {
        mContext = Contextor.getInstance().getContext();
        //Load to Persistent Storage
        loadCache();
    }


    public PhotoItemCollectionDao getDao() {
        return dao;
    }

    public void setDao(PhotoItemCollectionDao dao) {
        this.dao = dao;
        //Save to Persistent Storage
        saveCache();
    }

    public void insertDaoAtTopPosition(PhotoItemCollectionDao newdao){
        if (dao == null)
            dao = new PhotoItemCollectionDao();
        if (dao.getData() == null)
            dao.setData(new ArrayList<PhotoItemDao>());
        dao.getData().addAll(0 , newdao.getData());
        saveCache();
    }

    public void appendDaoAtBottomPosition(PhotoItemCollectionDao newdao){
        if (dao == null)
            dao = new PhotoItemCollectionDao();
        if (dao.getData() == null)
            dao.setData(new ArrayList<PhotoItemDao>());
        dao.getData().addAll(dao.getData().size() , newdao.getData());
        saveCache();
    }

    public int MaximumId(){
        if (dao == null)
            return 0;
        if (dao.getData() == null)
            return 0;
        if (dao.getData().size() == 0)
            return 0;
        int minId = dao.getData().get(0).getId();
        for (int i = 0; i< dao.getData().size();i++){
            minId = Math.min(minId,dao.getData().get(i).getId());
        }
        return minId;
    }

    public int MinimumId(){
        if (dao == null)
            return 0;
        if (dao.getData() == null)
            return 0;
        if (dao.getData().size() == 0)
            return 0;
        int maxId = dao.getData().get(0).getId();
        for (int i = 0; i< dao.getData().size();i++){
            maxId = Math.max(maxId,dao.getData().get(i).getId());
        }
        return maxId;
    }

    public  int getCount(){
        if (dao == null)
            return 0;
        if (dao.getData() == null)
            return 0;
        return dao.getData().size();
    }

    public Bundle onSavedInstanceState(){
        Bundle bundle = new Bundle();
        bundle.putParcelable("dao", dao);
        return  bundle;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState){
        dao = savedInstanceState.getParcelable("dao");
    }

    private void saveCache(){
        PhotoItemCollectionDao cachesDao = new PhotoItemCollectionDao();
        if (dao != null && dao.getData() != null)
            cachesDao.setData(dao.getData().subList(0 , Math.min(20, dao.getData().size())));
        String json = new Gson().toJson(cachesDao);

        SharedPreferences prefs = mContext.getSharedPreferences("photos",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        //Add /Edit /Delete
        editor.putString("json",json);
        editor.apply();
    }

    private void loadCache(){
        SharedPreferences prefs = mContext.getSharedPreferences("photos",
                Context.MODE_PRIVATE);
        String json = prefs.getString("json", null);
        if (json == null)
            return;
        dao = new Gson().fromJson(json , PhotoItemCollectionDao.class);
    }
}

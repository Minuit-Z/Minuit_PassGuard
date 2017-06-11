package fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import adapters.MyAdapter;
import objects.PassDesc;
import ui.QuickIndexBar;
import utils.DBOperation;
import ziye.minuit_passguard.R;

import static android.content.ContentValues.TAG;


public class HomeFragment extends Fragment {

    QuickIndexBar qb;
    ListView lv;
    Handler handler=new Handler();
    private TextView tv_currentWord;
    private TextView tv_nodata;
    List<PassDesc> lists;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_home, container, false);

        initView(view);
        initListener(view);

        initData();
        return view;
    }



    //从数据库获取数据
    private void initData() {
        ArrayList list=new DBOperation(getActivity()).queryAllDesc();
        if (list.size()==0){
            //本地数据没有数据
            tv_nodata.setVisibility(View.VISIBLE);
            lv.setVisibility(View.INVISIBLE);
            qb.setVisibility(View.INVISIBLE);
        }else {
            tv_nodata.setVisibility(View.INVISIBLE);
            lv.setVisibility(View.VISIBLE);
            qb.setVisibility(View.VISIBLE);

            lists=new ArrayList<>();
            for (int i = 0; i < list.size(); ++i)
                lists.add(new PassDesc((String)list.get(i)));
            Collections.sort(lists);

            //排序，并展示出来
            Log.e(TAG, "initData: "+lists.size() );
            MyAdapter adapter=new MyAdapter(getActivity(),lists);
            lv.setAdapter(adapter);
        }
    }

    private void initListener(View view) {
        qb.setOnTouchLetterListener(new QuickIndexBar.OnTouchLetterListener() {
            @Override
            public void onTouchLetter(String letter) {
                Log.e(TAG, "onTouchLetter: "+letter);
                //根据当前触摸的字母，去集合中找item的首字母
                for (int i=0;i<lists.size();i++){
                    String firstword= lists.get(i).getMyPinyin();

                    if (letter.equals(firstword)){
                        //找到特定字母了，将当前item
                        lv.setSelection(i);
                        break;
                    }
                }
                showCurrentWord(letter);
            }
        });
    }

    /**
    *@description  显示当前字母
    *@author   张子扬
    *create at 2017/3/20 0020 13:25
    */
    private void showCurrentWord(String letter) {
        tv_currentWord.setVisibility(View.VISIBLE);
        tv_currentWord.setText(letter);
        //移除之前的任务
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //此处为主线程
                tv_currentWord.setVisibility(View.INVISIBLE);
            }
        },500);
    }

    private void initView(View view) {
        qb= (QuickIndexBar) view.findViewById(R.id.qk_index);
        lv= (ListView) view.findViewById(R.id.lv_data);
        tv_currentWord= (TextView) view.findViewById(R.id.tv_current_word);
        tv_nodata= (TextView) view.findViewById(R.id.tv_no_data);
    }

}

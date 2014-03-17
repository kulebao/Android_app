package com.djc.logintest.activities;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.djc.logintest.R;
import com.djc.logintest.adapter.ChildListAdapter;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.ChildInfo;

public class SelectChildActivity extends UmengStatisticsActivity {

    private ListView list;
    private ChildListAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notice_list);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.select_child);
        initListAdapter();
    }

    private void initListAdapter() {
        List<ChildInfo> listinfo = DataMgr.getInstance().getAllChildrenInfo();
        adapter = new ChildListAdapter(this, listinfo);
        list = (ListView) findViewById(R.id.notice_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ChildInfo info = (ChildInfo) adapter.getItem(position);
                String wantedid = info.getServer_id();
                String currentid = DataMgr.getInstance().getSelectedChild().getServer_id();
                if (!currentid.equals(wantedid)) {
                    DataMgr.getInstance().setSelectedChild(wantedid);
                }
                Toast.makeText(SelectChildActivity.this, SelectChildActivity.this.getResources()
                        .getString(R.string.select_success), Toast.LENGTH_SHORT).show();
                SelectChildActivity.this.finish();
            }
        });
    }
}
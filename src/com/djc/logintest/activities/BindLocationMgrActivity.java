package com.djc.logintest.activities;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.djc.logintest.R;
import com.djc.logintest.adapter.BindedNumInfoListAdapter;
import com.djc.logintest.constant.EventType;
import com.djc.logintest.dbmgr.DataMgr;
import com.djc.logintest.dbmgr.info.BindedNumInfo;
import com.djc.logintest.dlgmgr.DlgMgr;
import com.djc.logintest.utils.Utils;

public class BindLocationMgrActivity extends Activity {
    protected static final int MAX_BIND_INFO = 4;
    private BindedNumInfoListAdapter adapter;
    private ListView list;
    private List<BindedNumInfo> listinfo;
    private AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bind_location);
        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.bind_location);
        initView();
    }

    private void initView() {
        Button addLocationNumBtn = (Button) findViewById(R.id.add_location_num);
        addLocationNumBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DataMgr dataMgr = DataMgr.getInstance();
                if (dataMgr.getAllBindedNumInfo().size() > MAX_BIND_INFO) {
                    Toast.makeText(BindLocationMgrActivity.this, String.format(getResources()
                            .getString(R.string.max_binded_num_alert), MAX_BIND_INFO),
                            Toast.LENGTH_SHORT);
                    return;
                }
                showAddDialog();
            }
        });
        initListAdapter();
    }

    private void showAddDialog() {
        LayoutInflater factory = LayoutInflater.from(this);
        final View textEntryView = factory.inflate(R.layout.location_text_entry, null);
        final EditText nicknameEdit = (EditText) textEntryView.findViewById(R.id.nichname_edit);
        final EditText numEdit = (EditText) textEntryView.findViewById(R.id.location_num_edit);
        dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.add_location_num)
                .setView(textEntryView)
                .setPositiveButton(R.string.add,
                        new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handleClickAddBtn(nicknameEdit, numEdit);
                            }
                        })
                .setNegativeButton(R.string.back,
                        new android.content.DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DlgMgr.cancelDialog(BindLocationMgrActivity.this.dialog);
                            }
                        }).create();
        dialog.show();
        DlgMgr.disableDlgDismiss(dialog);
    }

    private void handleClickAddBtn(final EditText nicknameEdit, final EditText numEdit) {
        String nickName = nicknameEdit.getText().toString();
        String num = numEdit.getText().toString();
        if (Utils.checkPhoneNum(num)) {
            BindedNumInfo info = new BindedNumInfo();
            info.setNickname(nickName);
            info.setPhone_num(num);
            DataMgr.getInstance().addBindedNumInfo(info);
            listinfo.add(info);
            adapter.notifyDataSetChanged();
            DlgMgr.cancelDialog(dialog);
        } else {
            Utils.showSingleBtnEventDlg(EventType.PHONE_NUM_INPUT_ERROR,
                    BindLocationMgrActivity.this);
        }
    }

    private void initListAdapter() {
        listinfo = DataMgr.getInstance().getAllBindedNumInfo();
        adapter = new BindedNumInfoListAdapter(this, listinfo);
        list = (ListView) findViewById(R.id.location_num_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
    }
}
